/**
 * Copyright (c) Microsoft Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright.tests.library;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.ContextFactory;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.HttpsTestServer;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.SameSiteAttribute;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/browsercontext-route.spec.ts. */
public class TestBrowsercontextRoute {
  // Java: the defaultSameSiteCookieValue fixture of tests/config/browserTest.ts.
  private static SameSiteAttribute defaultSameSiteCookieValue(BrowserName browserName) {
    if (browserName == BrowserName.CHROMIUM || Env.isBidi()) {
      return SameSiteAttribute.LAX;
    }
    if (browserName == BrowserName.WEBKIT && (Env.platform().equals("linux") || "webkit-wsl".equals(Env.channel()))) {
      return SameSiteAttribute.LAX;
    }
    return SameSiteAttribute.NONE;
  }

  // Java: a Cookie has no equals(), the tests compare its fields.
  private static Map<String, Object> cookieMap(Cookie c) {
    Map<String, Object> map = new LinkedHashMap<>();
    map.put("sameSite", c.sameSite);
    map.put("name", c.name);
    map.put("value", c.value);
    map.put("domain", c.domain);
    map.put("path", c.path);
    map.put("expires", c.expires);
    map.put("httpOnly", c.httpOnly);
    map.put("secure", c.secure);
    return map;
  }

  @PlaywrightTest
  @DisplayName("RO-165 should intercept")
  void shouldIntercept(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    AtomicBoolean intercepted = new AtomicBoolean(false);
    // Java: the handler reads `page`, which upstream declares after it.
    AtomicReference<Page> pageRef = new AtomicReference<>();
    context.route("**/empty.html", route -> {
      intercepted.set(true);
      Request request = route.request();
      assertTrue(request.url().contains("empty.html"));
      assertNotNull(request.headers().get("user-agent"));
      assertEquals("GET", request.method());
      assertNull(request.postData());
      assertEquals(true, request.isNavigationRequest());
      assertEquals("document", request.resourceType());
      assertEquals(true, request.frame() == pageRef.get().mainFrame());
      assertEquals("about:blank", request.frame().url());
      route.resume();
    });
    Page page = context.newPage();
    pageRef.set(page);
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(true, response.ok());
    assertEquals(true, intercepted.get());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("RO-166 should unroute")
  void shouldUnroute(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();

    List<Integer> intercepted = new ArrayList<>();
    context.route("**/*", route -> {
      intercepted.add(1);
      route.fallback();
    });
    context.route("**/empty.html", route -> {
      intercepted.add(2);
      route.fallback();
    });
    context.route("**/empty.html", route -> {
      intercepted.add(3);
      route.fallback();
    });
    Consumer<Route> handler4 = route -> {
      intercepted.add(4);
      route.fallback();
    };
    context.route(Pattern.compile("empty.html"), handler4);
    page.navigate(server.EMPTY_PAGE);
    assertEquals(asList(4, 3, 2, 1), intercepted);

    intercepted.clear();
    context.unroute(Pattern.compile("empty.html"), handler4);
    page.navigate(server.EMPTY_PAGE);
    assertEquals(asList(3, 2, 1), intercepted);

    intercepted.clear();
    context.unroute("**/empty.html");
    page.navigate(server.EMPTY_PAGE);
    assertEquals(asList(1), intercepted);

    context.close();
  }

  @PlaywrightTest
  @DisplayName("RO-167 should yield to page.route")
  void shouldYieldToPageRoute(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    context.route("**/empty.html", route -> {
      route.fulfill(new Route.FulfillOptions().setStatus(200).setBody("context"));
    });
    Page page = context.newPage();
    page.route("**/empty.html", route -> {
      route.fulfill(new Route.FulfillOptions().setStatus(200).setBody("page"));
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(true, response.ok());
    assertEquals("page", response.text());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("RO-168 should fall back to context.route")
  void shouldFallBackToContextRoute(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    context.route("**/empty.html", route -> {
      route.fulfill(new Route.FulfillOptions().setStatus(200).setBody("context"));
    });
    Page page = context.newPage();
    page.route("**/non-empty.html", route -> {
      route.fulfill(new Route.FulfillOptions().setStatus(200).setBody("page"));
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(true, response.ok());
    assertEquals("context", response.text());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("RO-169 should support Set-Cookie header")
  void shouldSupportSetCookieHeader(ContextFactory contextFactory, BrowserName browserName) {
    BrowserContext context = contextFactory.newContext();
    Page page = context.newPage();
    page.route("https://example.com/", route -> {
      route.fulfill(new Route.FulfillOptions()
        .setHeaders(java.util.Collections.singletonMap("Set-Cookie", "name=value; domain=.example.com; Path=/"))
        .setContentType("text/html")
        .setBody("done"));
    });
    page.navigate("https://example.com");
    List<Map<String, Object>> cookies = new ArrayList<>();
    for (Cookie cookie : context.cookies()) {
      cookies.add(cookieMap(cookie));
    }
    assertEquals(asList(mapOf(
      "sameSite", defaultSameSiteCookieValue(browserName),
      "name", "name",
      "value", "value",
      "domain", ".example.com",
      "path", "/",
      "expires", -1.0,
      "httpOnly", false,
      "secure", false)), cookies);
  }

  @PlaywrightTest
  @DisplayName("RO-170 should ignore secure Set-Cookie header for insecure requests")
  void shouldIgnoreSecureSetCookieHeaderForInsecureRequests(ContextFactory contextFactory, TestServer server, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.WEBKIT && !Env.isMac, "fixme: ");

    BrowserContext context = contextFactory.newContext();
    Page page = context.newPage();
    page.route("http://example.com/", route -> {
      route.fulfill(new Route.FulfillOptions()
        .setHeaders(java.util.Collections.singletonMap("Set-Cookie", "name=value; domain=.example.com; Path=/; Secure"))
        .setContentType("text/html")
        .setBody("done"));
    });
    page.navigate("http://example.com");
    assertEquals(new ArrayList<Cookie>(), context.cookies());
  }

  @PlaywrightTest
  @DisplayName("RO-171 should use Set-Cookie header in future requests")
  void shouldUseSetCookieHeaderInFutureRequests(ContextFactory contextFactory, TestServer server, BrowserName browserName) {
    BrowserContext context = contextFactory.newContext();
    Page page = context.newPage();

    page.route(server.EMPTY_PAGE, route -> {
      route.fulfill(new Route.FulfillOptions()
        .setHeaders(java.util.Collections.singletonMap("Set-Cookie", "name=value"))
        .setContentType("text/html")
        .setBody("done"));
    });
    page.navigate(server.EMPTY_PAGE);
    List<Map<String, Object>> cookies = new ArrayList<>();
    for (Cookie cookie : context.cookies()) {
      cookies.add(cookieMap(cookie));
    }
    assertEquals(asList(mapOf(
      "sameSite", defaultSameSiteCookieValue(browserName),
      "name", "name",
      "value", "value",
      "domain", server.HOSTNAME,
      "path", "/",
      "expires", -1.0,
      "httpOnly", false,
      "secure", false)), cookies);

    AtomicReference<String> cookie = new AtomicReference<>("");
    server.setRoute("/foo.html", (req, res) -> {
      cookie.set(req.headers.get("cookie"));
      res.end();
    });
    page.navigate(server.PREFIX + "/foo.html");
    assertEquals("name=value", cookie.get());
  }

  @PlaywrightTest
  @DisplayName("RO-172 should work with ignoreHTTPSErrors")
  void shouldWorkWithIgnoreHTTPSErrors(Browser browser, HttpsTestServer httpsServer) {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setIgnoreHTTPSErrors(true));
    Page page = context.newPage();

    page.route("**/*", route -> route.resume());
    Response response = page.navigate(httpsServer.EMPTY_PAGE);
    assertEquals(200, response.status());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("RO-173 should support the times parameter with route matching")
  void shouldSupportTheTimesParameterWithRouteMatching(BrowserContext context, Page page, TestServer server) {
    List<Integer> intercepted = new ArrayList<>();
    context.route("**/empty.html", route -> {
      intercepted.add(1);
      route.resume();
    }, new BrowserContext.RouteOptions().setTimes(1));
    page.navigate(server.EMPTY_PAGE);
    page.navigate(server.EMPTY_PAGE);
    page.navigate(server.EMPTY_PAGE);
    assertEquals(1, intercepted.size());
  }

  @PlaywrightTest
  @DisplayName("RO-174 should work if handler with times parameter was removed from another handler")
  @Fixme("java: Router.handle() iterates the handlers it started with and does not skip one that unroute() removed meanwhile (upstream checks this._routes.indexOf(routeHandler)); the intercepted list is [second, first]")
  void shouldWorkIfHandlerWithTimesParameterWasRemovedFromAnotherHandler(BrowserContext context, Page page, TestServer server) {
    List<String> intercepted = new ArrayList<>();
    Consumer<Route> handler = route -> {
      intercepted.add("first");
      route.resume();
    };
    context.route("**/*", handler, new BrowserContext.RouteOptions().setTimes(1));
    context.route("**/*", route -> {
      intercepted.add("second");
      context.unroute("**/*", handler);
      route.fallback();
    });
    page.navigate(server.EMPTY_PAGE);
    assertEquals(asList("second"), intercepted);
    intercepted.clear();
    page.navigate(server.EMPTY_PAGE);
    assertEquals(asList("second"), intercepted);
  }

  @PlaywrightTest
  @DisplayName("RO-175 should support async handler w/ times")
  void shouldSupportAsyncHandlerWTimes(BrowserContext context, Page page, TestServer server) {
    context.route("**/empty.html", route -> {
      page.waitForTimeout(100);
      route.fulfill(new Route.FulfillOptions()
        .setBody("<html>intercepted</html>")
        .setContentType("text/html"));
    }, new BrowserContext.RouteOptions().setTimes(1));
    page.navigate(server.EMPTY_PAGE);
    assertThat(page.locator("body")).hasText("intercepted");
    page.navigate(server.EMPTY_PAGE);
    assertThat(page.locator("body")).not().hasText("intercepted");
  }

  @PlaywrightTest
  @DisplayName("RO-176 should overwrite post body with empty string")
  void shouldOverwritePostBodyWithEmptyString(BrowserContext context, TestServer server, Page page, BrowserName browserName) throws Exception {
    context.route("**/empty.html", route -> {
      route.resume(new Route.ResumeOptions().setPostData(""));
    });

    CompletableFuture<TestServer.Request> reqFuture = server.waitForRequest("/empty.html");
    page.setContent("\n" +
      "      <script>\n" +
      "        (async () => {\n" +
      "            await fetch('" + server.EMPTY_PAGE + "', {\n" +
      "              method: 'POST',\n" +
      "              body: 'original',\n" +
      "            });\n" +
      "        })()\n" +
      "      </script>\n" +
      "    ");
    // Java: the route handler runs while the test thread is inside a Playwright call.
    page.waitForCondition(reqFuture::isDone);
    TestServer.Request req = reqFuture.get();

    String body = new String(req.postBody());
    assertEquals("", body);
  }

  @PlaywrightTest
  @DisplayName("RO-177 should chain fallback")
  void shouldChainFallback(BrowserContext context, Page page, TestServer server) {
    List<Integer> intercepted = new ArrayList<>();
    context.route("**/empty.html", route -> {
      intercepted.add(1);
      route.fallback();
    });
    context.route("**/empty.html", route -> {
      intercepted.add(2);
      route.fallback();
    });
    context.route("**/empty.html", route -> {
      intercepted.add(3);
      route.fallback();
    });
    page.navigate(server.EMPTY_PAGE);
    assertEquals(asList(3, 2, 1), intercepted);
  }

  @PlaywrightTest
  @DisplayName("RO-178 should chain fallback w/ dynamic URL")
  void shouldChainFallbackWDynamicURL(BrowserContext context, Page page, TestServer server) {
    List<Integer> intercepted = new ArrayList<>();
    context.route("**/bar", route -> {
      intercepted.add(1);
      route.fallback(new Route.FallbackOptions().setUrl(server.EMPTY_PAGE));
    });
    context.route("**/foo", route -> {
      intercepted.add(2);
      route.fallback(new Route.FallbackOptions().setUrl("http://localhost/bar"));
    });

    context.route("**/empty.html", route -> {
      intercepted.add(3);
      route.fallback(new Route.FallbackOptions().setUrl("http://localhost/foo"));
    });

    page.navigate(server.EMPTY_PAGE);
    assertEquals(asList(3, 2, 1), intercepted);
  }

  @PlaywrightTest
  @DisplayName("RO-179 should not chain fulfill")
  void shouldNotChainFulfill(BrowserContext context, Page page, TestServer server) {
    AtomicBoolean failed = new AtomicBoolean(false);
    context.route("**/empty.html", route -> {
      failed.set(true);
    });
    context.route("**/empty.html", route -> {
      route.fulfill(new Route.FulfillOptions().setStatus(200).setBody("fulfilled"));
    });
    context.route("**/empty.html", route -> {
      route.fallback();
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    byte[] body = response.body();
    assertEquals("fulfilled", new String(body));
    assertFalse(failed.get());
  }

  @PlaywrightTest
  @DisplayName("RO-180 should not chain abort")
  void shouldNotChainAbort(BrowserContext context, Page page, TestServer server) {
    AtomicBoolean failed = new AtomicBoolean(false);
    context.route("**/empty.html", route -> {
      failed.set(true);
    });
    context.route("**/empty.html", route -> {
      route.abort();
    });
    context.route("**/empty.html", route -> {
      route.fallback();
    });
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.navigate(server.EMPTY_PAGE));
    assertNotNull(e);
    assertFalse(failed.get());
  }

  @PlaywrightTest
  @DisplayName("RO-181 should chain fallback into page")
  void shouldChainFallbackIntoPage(BrowserContext context, Page page, TestServer server) {
    List<Integer> intercepted = new ArrayList<>();
    context.route("**/empty.html", route -> {
      intercepted.add(1);
      route.fallback();
    });
    context.route("**/empty.html", route -> {
      intercepted.add(2);
      route.fallback();
    });
    context.route("**/empty.html", route -> {
      intercepted.add(3);
      route.fallback();
    });
    page.route("**/empty.html", route -> {
      intercepted.add(4);
      route.fallback();
    });
    page.route("**/empty.html", route -> {
      intercepted.add(5);
      route.fallback();
    });
    page.route("**/empty.html", route -> {
      intercepted.add(6);
      route.fallback();
    });
    page.navigate(server.EMPTY_PAGE);
    assertEquals(asList(6, 5, 4, 3, 2, 1), intercepted);
  }

  @PlaywrightTest
  @DisplayName("RO-182 should fall back async")
  void shouldFallBackAsync(Page page, BrowserContext context, TestServer server) {
    List<Integer> intercepted = new ArrayList<>();
    context.route("**/empty.html", route -> {
      intercepted.add(1);
      page.waitForTimeout(100);
      route.fallback();
    });
    context.route("**/empty.html", route -> {
      intercepted.add(2);
      page.waitForTimeout(100);
      route.fallback();
    });
    context.route("**/empty.html", route -> {
      intercepted.add(3);
      page.waitForTimeout(100);
      route.fallback();
    });
    page.navigate(server.EMPTY_PAGE);
    assertEquals(asList(3, 2, 1), intercepted);
  }

  @PlaywrightTest
  @DisplayName("RO-183 should bypass disk cache when context interception is enabled")
  void shouldBypassDiskCacheWhenContextInterceptionIsEnabled(Page page, TestServer server) {
    page.context().route("**/api*", route -> route.resume());
    page.navigate(server.PREFIX + "/frames/one-frame.html");
    {
      List<TestServer.Request> requests = new CopyOnWriteArrayList<>();
      server.setRoute("/api", (req, res) -> {
        requests.add(req);
        res.statusCode = 200;
        res.setHeader("content-type", "text/plain");
        res.setHeader("cache-control", "public, max-age=31536000");
        res.end("Hello");
      });
      for (int i = 0; i < 3; i++) {
        // it.step(`main frame iteration ${i}`)
        Response response = page.waitForResponse("**/api", () -> {
          page.evaluate("async () => {\n" +
            "  const response = await fetch('/api');\n" +
            "  return response.status;\n" +
            "}");
        });
        assertEquals(200, response.status());
        assertEquals(i + 1, requests.size());
      }
    }
  }

  @PlaywrightTest
  @DisplayName("RO-184 should fulfill popup main request using alias")
  void shouldFulfillPopupMainRequestUsingAlias(Page page, TestServer server) {
    page.context().route("**/*", route -> {
      APIResponse response = route.fetch();
      route.fulfill(new Route.FulfillOptions().setResponse(response).setBody("hello"));
    });
    page.setContent("<a target=_blank href=\"" + server.EMPTY_PAGE + "\">click me</a>");
    Page popup = page.waitForPopup(() -> page.getByText("click me").click());
    assertThat(popup.locator("body")).hasText("hello");
  }

  // RO-185 should respect URLPattern ignoreCase: not ported, see upstream_not_ported.txt.
}
