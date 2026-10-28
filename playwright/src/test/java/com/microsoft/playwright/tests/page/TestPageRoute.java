/**
 * Copyright 2018 Google Inc. All rights reserved.
 * Modifications copyright (c) Microsoft Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.EnabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.browserMajorVersion;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/page/page-route.spec.ts. */
public class TestPageRoute {
  private static boolean truthy(String value) {
    return value != null && !value.isEmpty();
  }

  @PlaywrightTest
  @DisplayName("RO-001 should intercept @smoke")
  void shouldIntercept(Page page, TestServer server) {
    boolean[] intercepted = {false};
    page.route("**/empty.html", route -> {
      Request request = route.request();
      assertEquals(request, route.request());
      assertTrue(request.url().contains("empty.html"));
      assertTrue(truthy(request.headers().get("user-agent")));
      assertEquals("GET", request.method());
      assertNull(request.postData());
      assertEquals(true, request.isNavigationRequest());
      assertEquals("document", request.resourceType());
      assertEquals(true, request.frame() == page.mainFrame());
      assertEquals("about:blank", request.frame().url());
      route.resume();
      intercepted[0] = true;
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(true, response.ok());
    assertEquals(true, intercepted[0]);
  }

  @PlaywrightTest
  @DisplayName("RO-002 should unroute")
  void shouldUnroute(Page page, TestServer server) {
    List<Integer> intercepted = new ArrayList<>();
    page.route("**/*", route -> {
      intercepted.add(1);
      route.fallback();
    });
    page.route("**/empty.html", route -> {
      intercepted.add(2);
      route.fallback();
    });
    page.route("**/empty.html", route -> {
      intercepted.add(3);
      route.fallback();
    });
    Consumer<Route> handler4 = route -> {
      intercepted.add(4);
      route.fallback();
    };
    page.route(Pattern.compile("empty.html"), handler4);
    page.navigate(server.EMPTY_PAGE);
    assertEquals(Arrays.asList(4, 3, 2, 1), intercepted);

    intercepted.clear();
    page.unroute(Pattern.compile("empty.html"), handler4);
    page.navigate(server.EMPTY_PAGE);
    assertEquals(Arrays.asList(3, 2, 1), intercepted);

    intercepted.clear();
    page.unroute("**/empty.html");
    page.navigate(server.EMPTY_PAGE);
    assertEquals(Arrays.asList(1), intercepted);
  }

  @PlaywrightTest
  @DisplayName("RO-003 should not support ? in glob pattern")
  void shouldNotSupportInGlobPattern(Page page, TestServer server) {
    server.setRoute("/index", (req, res) -> res.end("index-no-hello"));
    server.setRoute("/index123hello", (req, res) -> res.end("index123hello"));
    server.setRoute("/index?hello", (req, res) -> res.end("index?hello"));
    server.setRoute("/index1hello", (req, res) -> res.end("index1hello"));

    page.route("**/index?hello", route -> {
      route.fulfill(new Route.FulfillOptions().setBody("intercepted any character"));
    });

    page.route("**/index\\?hello", route -> {
      route.fulfill(new Route.FulfillOptions().setBody("intercepted question mark"));
    });

    page.navigate(server.PREFIX + "/index?hello");
    assertTrue(page.content().contains("intercepted question mark"));

    page.navigate(server.PREFIX + "/index");
    assertTrue(page.content().contains("index-no-hello"));

    page.navigate(server.PREFIX + "/index1hello");
    assertTrue(page.content().contains("index1hello"));

    page.navigate(server.PREFIX + "/index123hello");
    assertTrue(page.content().contains("index123hello"));
  }

  @PlaywrightTest
  @DisplayName("RO-004 should work when POST is redirected with 302")
  void shouldWorkWhenPOSTIsRedirectedWith302(Page page, TestServer server) {
    server.setRedirect("/rredirect", "/empty.html");
    page.navigate(server.EMPTY_PAGE);
    page.route("**/*", route -> route.resume());
    page.setContent("\n" +
      "    <form action='/rredirect' method='post'>\n" +
      "      <input type=\"hidden\" id=\"foo\" name=\"foo\" value=\"FOOBAR\">\n" +
      "    </form>\n" +
      "  ");
    page.waitForNavigation(() -> page.evalOnSelector("form", "form => form.submit()"));
  }

  // @see https://github.com/GoogleChrome/puppeteer/issues/3973
  @PlaywrightTest
  @DisplayName("RO-005 should work when header manipulation headers with redirect")
  void shouldWorkWhenHeaderManipulationHeadersWithRedirect(Page page, TestServer server) {
    server.setRedirect("/rrredirect", "/empty.html");
    page.route("**/*", route -> {
      Map<String, String> headers = new HashMap<>(route.request().headers());
      headers.put("foo", "bar");
      route.resume(new Route.ResumeOptions().setHeaders(headers));
    });
    page.navigate(server.PREFIX + "/rrredirect");
  }

  // @see https://github.com/GoogleChrome/puppeteer/issues/4743
  @PlaywrightTest
  @DisplayName("RO-006 should be able to remove headers")
  void shouldBeAbleToRemoveHeaders(Page page, TestServer server) throws Exception {
    page.navigate(server.EMPTY_PAGE);
    page.route("**/*", route -> {
      Map<String, String> headers = new HashMap<>(route.request().headers());
      headers.remove("foo");
      route.resume(new Route.ResumeOptions().setHeaders(headers));
    });

    CompletableFuture<TestServer.Request> serverRequest = server.waitForRequest("/title.html");
    page.evaluate("url => fetch(url, { headers: { foo: 'bar' } })", server.PREFIX + "/title.html");
    assertNull(serverRequest.get().headers.get("foo"));
  }

  @PlaywrightTest
  @DisplayName("RO-007 should contain referer header")
  void shouldContainRefererHeader(Page page, TestServer server) {
    List<Request> requests = new ArrayList<>();
    page.route("**/*", route -> {
      requests.add(route.request());
      route.resume();
    });
    page.navigate(server.PREFIX + "/one-style.html");
    assertTrue(requests.get(1).url().contains("/one-style.css"));
    assertTrue(requests.get(1).headers().get("referer").contains("/one-style.html"));
  }

  @PlaywrightTest
  @DisplayName("RO-008 should properly return navigation response when URL has cookies")
  void shouldProperlyReturnNavigationResponseWhenURLHasCookies(Page page, TestServer server) {
    // Setup cookie.
    page.navigate(server.EMPTY_PAGE);
    page.context().addCookies(Arrays.asList(new Cookie("foo", "bar").setUrl(server.EMPTY_PAGE)));

    // Setup request interception.
    page.route("**/*", route -> route.resume());
    Response response = page.reload();
    assertEquals(200, response.status());
  }

  @PlaywrightTest
  @DisplayName("RO-009 should not override cookie header")
  void shouldNotOverrideCookieHeader(Page page, TestServer server, BrowserName browserName) throws Exception {
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("() => document.cookie = 'original=value'");
    String[] cookieValueInRoute = {null};
    page.route("**", route -> {
      Map<String, String> headers = new HashMap<>(route.request().allHeaders());
      cookieValueInRoute[0] = headers.get("cookie");
      headers.put("cookie", "overridden=value");
      route.resume(new Route.ResumeOptions().setHeaders(headers));
    });
    CompletableFuture<TestServer.Request> serverReq = server.waitForRequest("/empty.html");
    page.navigate(server.EMPTY_PAGE);

    // expect.soft
    assertAll(
      () -> {
        if (browserName != BrowserName.WEBKIT)
          assertEquals("original=value", cookieValueInRoute[0]);
      },
      () -> assertEquals("original=value", serverReq.get().headers.get("cookie")));
  }

  @PlaywrightTest
  @DisplayName("RO-010 should show custom HTTP headers")
  void shouldShowCustomHTTPHeaders(Page page, TestServer server) {
    page.setExtraHTTPHeaders(Collections.singletonMap("foo", "bar"));
    page.route("**/*", route -> {
      assertEquals("bar", route.request().headers().get("foo"));
      route.resume();
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(true, response.ok());
  }

  // @see https://github.com/GoogleChrome/puppeteer/issues/4337
  @PlaywrightTest
  @DisplayName("RO-011 should work with redirect inside sync XHR")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "fixme: No Network.requestIntercepted for the request")
  void shouldWorkWithRedirectInsideSyncXHR(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    server.setRedirect("/logo.png", "/pptr.png");
    // Java: route.resume() is synchronous, so the flag stands for the promise that upstream awaits.
    boolean[] continuePromise = {false};
    page.route("**/*", route -> {
      route.resume();
      continuePromise[0] = true;
    });
    Object status = page.evaluate("async () => {\n" +
      "  const request = new XMLHttpRequest();\n" +
      "  request.open('GET', '/logo.png', false);  // `false` makes the request synchronous\n" +
      "  request.send(null);\n" +
      "  return request.status;\n" +
      "}");
    assertEquals(200, status);
    assertTrue(continuePromise[0]);
  }

  @PlaywrightTest
  @DisplayName("RO-012 should pause intercepted XHR until continue")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "fixme: Redirected request is not paused in WebKit")
  void shouldPauseInterceptedXHRUntilContinue(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Route[] routePromise = {null};
    page.route("**/global-var.html", route -> routePromise[0] = route);
    boolean[] xhrFinished = {false};
    page.onRequestFinished(request -> {
      if (request.url().endsWith("/global-var.html"))
        xhrFinished[0] = true;
    });
    // The synchronous request blocks the page, so it is started from a timer.
    page.evaluate("() => {\n" +
      "  window.__statusPromise = new Promise(resolve => setTimeout(() => {\n" +
      "    const request = new XMLHttpRequest();\n" +
      "    request.open('GET', '/global-var.html', false);  // `false` makes the request synchronous\n" +
      "    request.send(null);\n" +
      "    resolve(request.status);\n" +
      "  }, 0));\n" +
      "}");
    page.waitForCondition(() -> routePromise[0] != null);
    // Check that intercepted request is actually paused.
    page.waitForTimeout(500);
    assertFalse(xhrFinished[0]);
    routePromise[0].resume();
    assertEquals(200, page.evaluate("() => window.__statusPromise"));
  }

  @PlaywrightTest
  @DisplayName("RO-013 should pause intercepted fetch request until continue")
  void shouldPauseInterceptedFetchRequestUntilContinue(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Route[] routePromise = {null};
    page.route("**/global-var.html", route -> routePromise[0] = route);
    page.evaluate("() => {\n" +
      "  window.__fetchFinished = false;\n" +
      "  window.__statusPromise = fetch('/global-var.html').then(response => {\n" +
      "    window.__fetchFinished = true;\n" +
      "    return response.status;\n" +
      "  });\n" +
      "}");
    page.waitForCondition(() -> routePromise[0] != null);
    // Check that intercepted request is actually paused.
    page.waitForTimeout(500);
    assertEquals(false, page.evaluate("() => window.__fetchFinished"));
    routePromise[0].resume();
    assertEquals(200, page.evaluate("() => window.__statusPromise"));
  }

  @PlaywrightTest
  @DisplayName("RO-014 should work with custom referer headers")
  void shouldWorkWithCustomRefererHeaders(Page page, TestServer server, BrowserName browserName, Browser browser) {
    page.setExtraHTTPHeaders(Collections.singletonMap("referer", server.EMPTY_PAGE));
    page.route("**/*", route -> {
      // See https://github.com/microsoft/playwright/issues/8999
      if (browserName == BrowserName.CHROMIUM && browserMajorVersion(browser) < 154)
        assertEquals(server.EMPTY_PAGE + ", " + server.EMPTY_PAGE, route.request().headers().get("referer"));
      else
        assertEquals(server.EMPTY_PAGE, route.request().headers().get("referer"));
      route.resume();
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(true, response.ok());
  }

  @PlaywrightTest
  @DisplayName("RO-015 should be abortable")
  void shouldBeAbortable(Page page, TestServer server) {
    page.route(Pattern.compile("\\.css$"), route -> route.abort());
    boolean[] failed = {false};
    page.onRequestFailed(request -> {
      if (request.url().contains(".css"))
        failed[0] = true;
    });
    Response response = page.navigate(server.PREFIX + "/one-style.html");
    assertEquals(true, response.ok());
    assertNull(response.request().failure());
    assertEquals(true, failed[0]);
  }

  @PlaywrightTest
  @DisplayName("RO-016 should be abortable with custom error codes")
  void shouldBeAbortableWithCustomErrorCodes(Page page, TestServer server, BrowserName browserName) {
    page.route("**/*", route -> route.abort("internetdisconnected"));
    Request[] failedRequest = {null};
    page.onRequestFailed(request -> failedRequest[0] = request);
    try {
      page.navigate(server.EMPTY_PAGE);
    } catch (PlaywrightException e) {
    }
    assertNotNull(failedRequest[0]);
    if (browserName == BrowserName.WEBKIT)
      assertEquals(Env.isMac && Env.macVersion() < 11 ? "Request intercepted" : "Blocked by Web Inspector", failedRequest[0].failure());
    else if (browserName == BrowserName.FIREFOX)
      assertEquals("NS_ERROR_OFFLINE", failedRequest[0].failure());
    else
      assertEquals("net::ERR_INTERNET_DISCONNECTED", failedRequest[0].failure());
  }

  @PlaywrightTest
  @DisplayName("RO-017 should not throw if request was cancelled by the page")
  void shouldNotThrowIfRequestWasCancelledByThePage(Page page, TestServer server) throws Exception {
    // Java: there is no page.waitForRequestFailed(), the event is collected with a handler.
    CompletableFuture<Request> failurePromise = new CompletableFuture<>();
    page.onRequestFailed(request -> failurePromise.complete(request));
    AtomicReference<Throwable> abortError = new AtomicReference<>();
    // Java: the route handler runs the code that upstream runs after the pending evaluate: the page aborts the request, then the route is aborted.
    page.route("**/data.json", route -> {
      page.evaluate("() => globalThis.controller.abort()");
      page.context().waitForCondition(() -> failurePromise.isDone());
      try {
        route.abort(); // Should not throw.
      } catch (RuntimeException e) {
        abortError.set(e);
      }
    });
    page.navigate(server.EMPTY_PAGE);
    try {
      page.evaluate("url => {\n" +
        "  globalThis.controller = new AbortController();\n" +
        "  return fetch(url, { signal: globalThis.controller.signal });\n" +
        "}", server.PREFIX + "/data.json");
    } catch (PlaywrightException e) {
      // Java: the aborted fetch rejects the evaluation; upstream does not await it.
    }
    Request cancelledRequest = failurePromise.get();
    assertTrue(Pattern.compile("cancelled|aborted", Pattern.CASE_INSENSITIVE).matcher(cancelledRequest.failure()).find());
    assertNull(abortError.get());
  }

  @PlaywrightTest
  @DisplayName("RO-018 should send referer")
  void shouldSendReferer(Page page, TestServer server) throws Exception {
    page.setExtraHTTPHeaders(Collections.singletonMap("referer", "http://google.com/"));
    page.route("**/*", route -> route.resume());
    CompletableFuture<TestServer.Request> request = server.waitForRequest("/grid.html");
    page.navigate(server.PREFIX + "/grid.html");
    assertEquals("http://google.com/", request.get().headers.get("referer"));
  }

  @PlaywrightTest
  @DisplayName("RO-019 should fail navigation when aborting main resource")
  void shouldFailNavigationWhenAbortingMainResource(Page page, TestServer server, BrowserName browserName) {
    page.route("**/*", route -> route.abort());
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.navigate(server.EMPTY_PAGE));
    if (browserName == BrowserName.WEBKIT)
      assertTrue(error.getMessage().contains(Env.isMac && Env.macVersion() < 11 ? "Request intercepted" : "Blocked by Web Inspector"));
    else if (browserName == BrowserName.FIREFOX && Env.isBidi())
      assertTrue(error.getMessage().contains("NS_ERROR_ABORT"));
    else if (browserName == BrowserName.FIREFOX)
      assertTrue(error.getMessage().contains("NS_ERROR_FAILURE"));
    else
      assertTrue(error.getMessage().contains("net::ERR_FAILED"));
  }

  @PlaywrightTest
  @DisplayName("RO-020 should not work with redirects")
  void shouldNotWorkWithRedirects(Page page, TestServer server) {
    List<Request> intercepted = new ArrayList<>();
    page.route("**/*", route -> {
      route.resume();
      intercepted.add(route.request());
    });
    server.setRedirect("/non-existing-page.html", "/non-existing-page-2.html");
    server.setRedirect("/non-existing-page-2.html", "/non-existing-page-3.html");
    server.setRedirect("/non-existing-page-3.html", "/non-existing-page-4.html");
    server.setRedirect("/non-existing-page-4.html", "/empty.html");

    Response response = page.navigate(server.PREFIX + "/non-existing-page.html");
    assertEquals(200, response.status());
    assertTrue(response.url().contains("empty.html"));

    assertEquals(1, intercepted.size());
    assertEquals("document", intercepted.get(0).resourceType());
    assertEquals(true, intercepted.get(0).isNavigationRequest());
    assertTrue(intercepted.get(0).url().contains("/non-existing-page.html"));

    List<Request> chain = new ArrayList<>();
    for (Request r = response.request(); r != null; r = r.redirectedFrom()) {
      chain.add(r);
      assertEquals(true, r.isNavigationRequest());
    }
    assertEquals(5, chain.size());
    assertTrue(chain.get(0).url().contains("/empty.html"));
    assertTrue(chain.get(1).url().contains("/non-existing-page-4.html"));
    assertTrue(chain.get(2).url().contains("/non-existing-page-3.html"));
    assertTrue(chain.get(3).url().contains("/non-existing-page-2.html"));
    assertTrue(chain.get(4).url().contains("/non-existing-page.html"));
    for (int i = 0; i < chain.size(); i++)
      assertEquals(i != 0 ? chain.get(i - 1) : null, chain.get(i).redirectedTo());
  }

  @PlaywrightTest
  @DisplayName("RO-021 should chain fallback w/ dynamic URL")
  void shouldChainFallbackWDynamicURL(Page page, TestServer server) {
    List<Integer> intercepted = new ArrayList<>();
    page.route("**/bar", route -> {
      intercepted.add(1);
      route.fallback(new Route.FallbackOptions().setUrl(server.EMPTY_PAGE));
    });
    page.route("**/foo", route -> {
      intercepted.add(2);
      route.fallback(new Route.FallbackOptions().setUrl("http://localhost/bar"));
    });

    page.route("**/empty.html", route -> {
      intercepted.add(3);
      route.fallback(new Route.FallbackOptions().setUrl("http://localhost/foo"));
    });

    page.navigate(server.EMPTY_PAGE);
    assertEquals(Arrays.asList(3, 2, 1), intercepted);
  }

  @PlaywrightTest
  @DisplayName("RO-022 should work with redirects for subresources")
  void shouldWorkWithRedirectsForSubresources(Page page, TestServer server) {
    List<Request> intercepted = new ArrayList<>();
    page.route("**/*", route -> {
      route.resume();
      intercepted.add(route.request());
    });
    server.setRedirect("/one-style.css", "/two-style.css");
    server.setRedirect("/two-style.css", "/three-style.css");
    server.setRedirect("/three-style.css", "/four-style.css");
    server.setRoute("/four-style.css", (req, res) -> res.end("body {box-sizing: border-box; }"));

    Response response = page.navigate(server.PREFIX + "/one-style.html");
    assertEquals(200, response.status());
    assertTrue(response.url().contains("one-style.html"));

    assertEquals(2, intercepted.size());
    assertEquals("document", intercepted.get(0).resourceType());
    assertTrue(intercepted.get(0).url().contains("one-style.html"));

    Request r = intercepted.get(1);
    for (String url : Arrays.asList("/one-style.css", "/two-style.css", "/three-style.css", "/four-style.css")) {
      assertEquals("stylesheet", r.resourceType());
      assertTrue(r.url().contains(url));
      r = r.redirectedTo();
    }
    assertNull(r);
  }

  @PlaywrightTest
  @DisplayName("RO-023 should work with equal requests")
  void shouldWorkWithEqualRequests(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    int[] responseCount = {1};
    server.setRoute("/zzz", (req, res) -> res.end((responseCount[0]++) * 11 + ""));

    boolean[] spinner = {false};
    // Cancel 2nd request.
    page.route("**/*", route -> {
      if (spinner[0])
        route.abort();
      else
        route.resume();
      spinner[0] = !spinner[0];
    });
    List<Object> results = new ArrayList<>();
    for (int i = 0; i < 3; i++)
      results.add(page.evaluate("() => fetch('/zzz').then(response => response.text()).catch(e => 'FAILED')"));
    assertEquals(Arrays.asList("11", "FAILED", "22"), results);
  }

  @PlaywrightTest
  @DisplayName("RO-024 should navigate to dataURL and not fire dataURL requests")
  void shouldNavigateToDataURLAndNotFireDataURLRequests(Page page, TestServer server) {
    List<Request> requests = new ArrayList<>();
    page.route("**/*", route -> {
      requests.add(route.request());
      route.resume();
    });
    String dataURL = "data:text/html,<div>yo</div>";
    Response response = page.navigate(dataURL);
    assertNull(response);
    assertEquals(0, requests.size());
  }

  @PlaywrightTest
  @DisplayName("RO-025 should be able to fetch dataURL and not fire dataURL requests")
  void shouldBeAbleToFetchDataURLAndNotFireDataURLRequests(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    List<Request> requests = new ArrayList<>();
    page.route("**/*", route -> {
      requests.add(route.request());
      route.resume();
    });
    String dataURL = "data:text/html,<div>yo</div>";
    Object text = page.evaluate("url => fetch(url).then(r => r.text())", dataURL);
    assertEquals("<div>yo</div>", text);
    assertEquals(0, requests.size());
  }

  @PlaywrightTest
  @DisplayName("RO-026 should navigate to URL with hash and and fire requests without hash")
  void shouldNavigateToURLWithHashAndAndFireRequestsWithoutHash(Page page, TestServer server) {
    List<Request> requests = new ArrayList<>();
    page.route("**/*", route -> {
      requests.add(route.request());
      route.resume();
    });
    Response response = page.navigate(server.EMPTY_PAGE + "#hash");
    assertEquals(200, response.status());
    assertEquals(server.EMPTY_PAGE, response.url());
    assertEquals(1, requests.size());
    assertEquals(server.EMPTY_PAGE, requests.get(0).url());
  }

  @PlaywrightTest
  @DisplayName("RO-027 should work with encoded server")
  void shouldWorkWithEncodedServer(Page page, TestServer server) {
    // The requestWillBeSent will report encoded URL, whereas interception will
    // report URL as-is. @see crbug.com/759388
    page.route("**/*", route -> route.resume());
    Response response = page.navigate(server.PREFIX + "/some nonexisting page");
    assertEquals(404, response.status());
  }

  @PlaywrightTest
  @DisplayName("RO-028 should work with badly encoded server")
  void shouldWorkWithBadlyEncodedServer(Page page, TestServer server) {
    server.setRoute("/malformed?rnd=%911", (req, res) -> res.end());
    page.route("**/*", route -> route.resume());
    Response response = page.navigate(server.PREFIX + "/malformed?rnd=%911");
    assertEquals(200, response.status());
  }

  @PlaywrightTest
  @DisplayName("RO-029 should work with encoded server - 2")
  void shouldWorkWithEncodedServer2(Page page, TestServer server, BrowserName browserName) {
    // The requestWillBeSent will report URL as-is, whereas interception will
    // report encoded URL for stylesheet. @see crbug.com/759388
    page.navigate(server.EMPTY_PAGE);
    List<Request> requests = new ArrayList<>();
    page.route("**/*", route -> {
      route.resume();
      requests.add(route.request());
    });
    page.setContent("<link rel=\"stylesheet\" href=\"" + server.PREFIX + "/fonts?helvetica|arial\"/>");
    assertEquals(1, requests.size());
    assertEquals(404, requests.get(0).response().status());
  }

  @PlaywrightTest
  @DisplayName("RO-030 should not throw \"Invalid Interception Id\" if the request was cancelled")
  void shouldNotThrowInvalidInterceptionIdIfTheRequestWasCancelled(Page page, TestServer server) {
    page.setContent("<iframe></iframe>");
    Route[] route = {null};
    page.route("**/*", r -> route[0] = r);
    // Wait for request interception.
    page.waitForRequest(request -> true, () -> {
      page.evalOnSelector("iframe", "(frame, url) => frame.src = url", server.EMPTY_PAGE);
    });
    // Delete frame to cause request to be canceled.
    page.evalOnSelector("iframe", "frame => frame.remove()");
    PlaywrightException error = null;
    try {
      route[0].resume();
    } catch (PlaywrightException e) {
      error = e;
    }
    assertNull(error);
  }

  @PlaywrightTest
  @DisplayName("RO-031 should intercept main resource during cross-process navigation")
  void shouldInterceptMainResourceDuringCrossProcessNavigation(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    boolean[] intercepted = {false};
    page.route(server.CROSS_PROCESS_PREFIX + "/empty.html", route -> {
      intercepted[0] = true;
      route.resume();
    });
    Response response = page.navigate(server.CROSS_PROCESS_PREFIX + "/empty.html");
    assertEquals(true, response.ok());
    assertEquals(true, intercepted[0]);
  }

  @PlaywrightTest
  @DisplayName("RO-032 should fulfill with redirect status")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "fixme: in WebKit the redirects are handled by the network stack and we intercept before")
  void shouldFulfillWithRedirectStatus(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/title.html");
    server.setRoute("/final", (req, res) -> res.end("foo"));
    page.route("**/*", route -> {
      if (!route.request().url().equals(server.PREFIX + "/redirect_this")) {
        route.resume();
        return;
      }
      route.fulfill(new Route.FulfillOptions()
        .setStatus(301)
        .setHeaders(Collections.singletonMap("location", "/final")));
    });

    Object text = page.evaluate("async url => {\n" +
      "  const data = await fetch(url);\n" +
      "  return data.text();\n" +
      "}", server.PREFIX + "/redirect_this");
    assertEquals("foo", text);
  }

  @PlaywrightTest
  @DisplayName("RO-033 should not fulfill with redirect status")
  @EnabledOnBrowser(value = BrowserName.WEBKIT, reason = "we should support fulfill with redirect in webkit and delete this test")
  @Fixme("java: route.fulfill() does not wait for the driver's reply, so 'Cannot fulfill with redirect status' is never thrown (AssertionError: fulfill didn't throw)")
  void shouldNotFulfillWithRedirectStatus(Page page, TestServer server) throws Exception {
    page.navigate(server.PREFIX + "/empty.html");

    int[] status = {0};
    AtomicReference<CompletableFuture<Throwable>> exceptionPromise = new AtomicReference<>();
    page.route("**/*", route -> {
      if (!route.request().url().equals(server.PREFIX + "/redirect_this")) {
        route.resume();
        return;
      }
      try {
        route.fulfill(new Route.FulfillOptions()
          .setStatus(status[0])
          .setHeaders(Collections.singletonMap("location", "/empty.html")));
        exceptionPromise.get().completeExceptionally(new AssertionError("fulfill didn't throw"));
      } catch (PlaywrightException e) {
        exceptionPromise.get().complete(e);
      }
    });

    for (status[0] = 300; status[0] < 310; status[0]++) {
      exceptionPromise.set(new CompletableFuture<>());
      page.evaluate("url => location.href = url", server.PREFIX + "/redirect_this");
      // Java: the handler runs while the page is being driven, wait for it to settle the promise.
      page.context().waitForCondition(() -> exceptionPromise.get().isDone());
      Throwable exception = exceptionPromise.get().get();
      assertNotNull(exception);
      assertEquals(true, exception.getMessage().contains("Cannot fulfill with redirect status"));
    }
  }

  @PlaywrightTest
  @DisplayName("RO-034 should support cors with GET")
  void shouldSupportCorsWithGET(Page page, TestServer server, BrowserName browserName) {
    page.navigate(server.EMPTY_PAGE);
    page.route("**/cars*", (route) -> {
      Map<String, String> headers = Collections.singletonMap("access-control-allow-origin", route.request().url().endsWith("allow") ? "*" : "none");
      route.fulfill(new Route.FulfillOptions()
        .setContentType("application/json")
        .setHeaders(headers)
        .setStatus(200)
        .setBody("[\"electric\",\"gas\"]"));
    });
    {
      // Should succeed
      Object resp = page.evaluate("async () => {\n" +
        "  const response = await fetch('https://example.com/cars?allow', { mode: 'cors' });\n" +
        "  return response.json();\n" +
        "}");
      assertEquals(Arrays.asList("electric", "gas"), resp);
    }
    {
      // Should be rejected
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.evaluate("async () => {\n" +
        "  const response = await fetch('https://example.com/cars?reject', { mode: 'cors' });\n" +
        "  return response.json();\n" +
        "}"));
      if (browserName == BrowserName.CHROMIUM)
        assertTrue(error.getMessage().contains("Failed"));
      if (browserName == BrowserName.WEBKIT)
        assertTrue(error.getMessage().contains("TypeError"));
      if (browserName == BrowserName.FIREFOX)
        assertTrue(error.getMessage().contains("NetworkError"));
    }
  }

  @PlaywrightTest
  @DisplayName("RO-035 should add Access-Control-Allow-Origin by default when fulfill")
  void shouldAddAccessControlAllowOriginByDefaultWhenFulfill(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.route("**/cars", route -> {
      route.fulfill(new Route.FulfillOptions()
        .setContentType("application/json")
        .setStatus(200)
        .setBody("[\"electric\",\"gas\"]"));
    });

    Object[] result = {null};
    Response response = page.waitForResponse("https://example.com/cars", () -> {
      result[0] = page.evaluate("async () => {\n" +
        "  const response = await fetch('https://example.com/cars', {\n" +
        "    method: 'POST',\n" +
        "    headers: { 'Content-Type': 'application/json' },\n" +
        "    mode: 'cors',\n" +
        "    body: JSON.stringify({ 'number': 1 })\n" +
        "  });\n" +
        "  return response.json();\n" +
        "}");
    });
    assertEquals(Arrays.asList("electric", "gas"), result[0]);
    assertEquals(server.PREFIX, response.headerValue("Access-Control-Allow-Origin"));
  }

  @PlaywrightTest
  @DisplayName("RO-036 should allow null origin for about:blank")
  void shouldAllowNullOriginForAboutBlank(Page page, TestServer server, BrowserName browserName) {
    page.route("**/something", route -> {
      route.fulfill(new Route.FulfillOptions()
        .setContentType("text/plain")
        .setStatus(200)
        .setBody("done"));
    });

    Object[] text = {null};
    Response response = page.waitForResponse(server.CROSS_PROCESS_PREFIX + "/something", () -> {
      text[0] = page.evaluate("async url => {\n" +
        "  const data = await fetch(url, {\n" +
        "    method: 'GET',\n" +
        "    headers: { 'X-PINGOTHER': 'pingpong' }\n" +
        "  });\n" +
        "  return data.text();\n" +
        "}", server.CROSS_PROCESS_PREFIX + "/something");
    });
    assertEquals("done", text[0]);
    assertEquals("null", response.headerValue("Access-Control-Allow-Origin"));
  }

  @PlaywrightTest
  @DisplayName("RO-037 should respect cors overrides")
  void shouldRespectCorsOverrides(Page page, TestServer server, BrowserName browserName) {
    page.navigate(server.EMPTY_PAGE);
    server.setRoute("/something", (request, response) -> {
      if (request.method.equals("OPTIONS")) {
        response.writeHead(204,
          "Access-Control-Allow-Origin", "*",
          "Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE",
          "Access-Control-Allow-Headers", "*",
          "Cache-Control", "no-cache");
        response.end();
        return;
      }
      response.writeHead(404, "Access-Control-Allow-Origin", "*");
      response.end("NOT FOUND");
    });
    // Fetch request should fail when CORS header doesn't include the origin.
    {
      page.route("**/something", route -> {
        route.fulfill(new Route.FulfillOptions()
          .setContentType("text/plain")
          .setStatus(200)
          .setHeaders(Collections.singletonMap("Access-Control-Allow-Origin", "http://non-existent"))
          .setBody("done"));
      });

      PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.evaluate("async url => {\n" +
        "  const data = await fetch(url, {\n" +
        "    method: 'GET',\n" +
        "    headers: { 'X-PINGOTHER': 'pingpong' }\n" +
        "  });\n" +
        "  return data.text();\n" +
        "}", server.CROSS_PROCESS_PREFIX + "/something"));
      if (browserName == BrowserName.CHROMIUM)
        assertTrue(error.getMessage().contains("Failed to fetch"));
      else if (browserName == BrowserName.WEBKIT)
        assertTrue(error.getMessage().contains("Load failed"));
      else if (browserName == BrowserName.FIREFOX)
        assertTrue(error.getMessage().contains("NetworkError when attempting to fetch resource."));
    }
  }

  @PlaywrightTest
  @DisplayName("RO-038 should not auto-intercept non-preflight OPTIONS without network interception")
  void shouldNotAutoInterceptNonPreflightOPTIONSWithoutNetworkInterception(Page page, TestServer server, BrowserName browserName) {
    page.navigate(server.EMPTY_PAGE);

    List<String> requests = Collections.synchronizedList(new ArrayList<>());
    server.setRoute("/something", (request, response) -> {
      requests.add(request.method + ":" + request.url);
      if (request.method.equals("OPTIONS")) {
        response.writeHead(200,
          "Access-Control-Allow-Origin", "*",
          "Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE",
          "Access-Control-Allow-Headers", "*",
          "Cache-Control", "no-cache");
        response.end("Hello");
        return;
      }
      response.writeHead(200, "Access-Control-Allow-Origin", "*");
      response.end("World");
    });

    // Without interception.
    {
      requests.clear();
      List<?> texts = (List<?>) page.evaluate("async url => {\n" +
        "  const response1 = await fetch(url, { method: 'OPTIONS' });\n" +
        "  const text1 = await response1.text();\n" +
        "  const response2 = await fetch(url, { method: 'GET' });\n" +
        "  const text2 = await response2.text();\n" +
        "  return [text1, text2];\n" +
        "}", server.CROSS_PROCESS_PREFIX + "/something");
      // expect.soft
      assertAll(
        () -> assertEquals("Hello", texts.get(0)),
        () -> assertEquals("World", texts.get(1)),
        // Preflight for OPTIONS, then OPTIONS, then GET without preflight.
        () -> assertEquals(Arrays.asList("OPTIONS:/something", "OPTIONS:/something", "GET:/something"), requests));
    }
  }

  // Make sure this runs in a new context as preflight results could be cached.
  @PlaywrightTest
  @DisplayName("RO-039 should not auto-intercept non-preflight OPTIONS with network interception")
  void shouldNotAutoInterceptNonPreflightOPTIONSWithNetworkInterception(Page page, TestServer server, BrowserName browserName) {
    page.navigate(server.EMPTY_PAGE);

    List<String> requests = Collections.synchronizedList(new ArrayList<>());
    server.setRoute("/something", (request, response) -> {
      requests.add(request.method + ":" + request.url);
      if (request.method.equals("OPTIONS")) {
        response.writeHead(200,
          "Access-Control-Allow-Origin", "*",
          "Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE",
          "Access-Control-Allow-Headers", "*",
          "Cache-Control", "no-cache");
        response.end("Hello");
        return;
      }
      response.writeHead(200, "Access-Control-Allow-Origin", "*");
      response.end("World");
    });

    // With interception.
    {
      page.route("**/something", route -> route.resume());

      requests.clear();
      List<?> texts = (List<?>) page.evaluate("async url => {\n" +
        "  const response1 = await fetch(url, { method: 'OPTIONS' });\n" +
        "  const text1 = await response1.text();\n" +
        "  const response2 = await fetch(url, { method: 'GET' });\n" +
        "  const text2 = await response2.text();\n" +
        "  return [text1, text2];\n" +
        "}", server.CROSS_PROCESS_PREFIX + "/something");
      // expect.soft
      assertAll(
        () -> assertEquals("Hello", texts.get(0)),
        () -> assertEquals("World", texts.get(1)),
        // Preflight for OPTIONS is auto-fulfilled, then OPTIONS, then GET without preflight.
        () -> {
          if (browserName == BrowserName.CHROMIUM || Env.isBidi())
            assertEquals(Arrays.asList("OPTIONS:/something", "GET:/something"), requests);
          else
            assertEquals(Arrays.asList("OPTIONS:/something", "OPTIONS:/something", "GET:/something"), requests);
        });
    }
  }

  @PlaywrightTest
  @DisplayName("RO-040 should support cors with POST")
  void shouldSupportCorsWithPOST(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.route("**/cars", route -> {
      route.fulfill(new Route.FulfillOptions()
        .setContentType("application/json")
        .setHeaders(Collections.singletonMap("Access-Control-Allow-Origin", "*"))
        .setStatus(200)
        .setBody("[\"electric\",\"gas\"]"));
    });
    Object resp = page.evaluate("async () => {\n" +
      "  const response = await fetch('https://example.com/cars', {\n" +
      "    method: 'POST',\n" +
      "    headers: { 'Content-Type': 'application/json' },\n" +
      "    mode: 'cors',\n" +
      "    body: JSON.stringify({ 'number': 1 })\n" +
      "  });\n" +
      "  return response.json();\n" +
      "}");
    assertEquals(Arrays.asList("electric", "gas"), resp);
  }

  @PlaywrightTest
  @DisplayName("RO-041 should support cors with credentials")
  void shouldSupportCorsWithCredentials(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.route("**/cars", route -> {
      Map<String, String> headers = new LinkedHashMap<>();
      headers.put("Access-Control-Allow-Origin", server.PREFIX);
      headers.put("Access-Control-Allow-Credentials", "true");
      route.fulfill(new Route.FulfillOptions()
        .setContentType("application/json")
        .setHeaders(headers)
        .setStatus(200)
        .setBody("[\"electric\",\"gas\"]"));
    });
    Object resp = page.evaluate("async () => {\n" +
      "  const response = await fetch('https://example.com/cars', {\n" +
      "    method: 'POST',\n" +
      "    headers: { 'Content-Type': 'application/json' },\n" +
      "    mode: 'cors',\n" +
      "    body: JSON.stringify({ 'number': 1 }),\n" +
      "    credentials: 'include'\n" +
      "  });\n" +
      "  return response.json();\n" +
      "}");
    assertEquals(Arrays.asList("electric", "gas"), resp);
  }

  private static boolean allowCors(TestServer.Request request, TestServer.Response response) {
    String origin = request.headers.get("origin");
    response.setHeader("Access-Control-Allow-Origin", origin != null && !origin.isEmpty() ? origin : "*");
    response.setHeader("Access-Control-Allow-Credentials", "true");
    response.setHeader("Access-Control-Allow-Headers", "x-app-version");
    if (!request.method.equals("OPTIONS"))
      return false;
    response.writeHead(204);
    response.end();
    return true;
  }

  @PlaywrightTest
  @DisplayName("RO-042 should not abort preflighted cross-origin redirect when a route is registered")
  void shouldNotAbortPreflightedCrossOriginRedirectWhenARouteIsRegistered(Page page, TestServer server) {
    server.setRoute("/download", (request, response) -> {
      if (allowCors(request, response))
        return;
      response.writeHead(302, "location", server.PREFIX + "/file");
      response.end();
    });
    server.setRoute("/file", (request, response) -> {
      if (allowCors(request, response))
        return;
      response.end("file contents");
    });

    page.navigate(server.EMPTY_PAGE);
    page.route("**/never-matches", route -> route.resume());
    Object result = page.evaluate("async url => {\n" +
      "  const response = await fetch(url, { credentials: 'include', headers: { 'X-App-Version': '1.2.3' } });\n" +
      "  return `${response.status} ${await response.text()}`;\n" +
      "}", server.CROSS_PROCESS_PREFIX + "/download");
    assertEquals("200 file contents", result);
  }

  @PlaywrightTest
  @DisplayName("RO-043 should reject cors with disallowed credentials")
  void shouldRejectCorsWithDisallowedCredentials(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.route("**/cars", route -> {
      route.fulfill(new Route.FulfillOptions()
        .setContentType("application/json")
        // Should fail without this line below!
        // 'Access-Control-Allow-Credentials': 'true'
        .setHeaders(Collections.singletonMap("Access-Control-Allow-Origin", server.PREFIX))
        .setStatus(200)
        .setBody("[\"electric\",\"gas\"]"));
    });
    assertThrows(PlaywrightException.class, () -> page.evaluate("async () => {\n" +
      "  const response = await fetch('https://example.com/cars', {\n" +
      "    method: 'POST',\n" +
      "    headers: { 'Content-Type': 'application/json' },\n" +
      "    mode: 'cors',\n" +
      "    body: JSON.stringify({ 'number': 1 }),\n" +
      "    credentials: 'include'\n" +
      "  });\n" +
      "  return response.json();\n" +
      "}"));
  }

  @PlaywrightTest
  @DisplayName("RO-044 should support cors for different methods")
  void shouldSupportCorsForDifferentMethods(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.route("**/cars", route -> {
      route.fulfill(new Route.FulfillOptions()
        .setContentType("application/json")
        .setHeaders(Collections.singletonMap("Access-Control-Allow-Origin", "*"))
        .setStatus(200)
        .setBody("[\"" + route.request().method() + "\",\"electric\",\"gas\"]"));
    });
    // First POST
    {
      Object resp = page.evaluate("async () => {\n" +
        "  const response = await fetch('https://example.com/cars', {\n" +
        "    method: 'POST',\n" +
        "    headers: { 'Content-Type': 'application/json' },\n" +
        "    mode: 'cors',\n" +
        "    body: JSON.stringify({ 'number': 1 })\n" +
        "  });\n" +
        "  return response.json();\n" +
        "}");
      assertEquals(Arrays.asList("POST", "electric", "gas"), resp);
    }
    // Then DELETE
    {
      Object resp = page.evaluate("async () => {\n" +
        "  const response = await fetch('https://example.com/cars', {\n" +
        "    method: 'DELETE',\n" +
        "    headers: {},\n" +
        "    mode: 'cors',\n" +
        "    body: ''\n" +
        "  });\n" +
        "  return response.json();\n" +
        "}");
      assertEquals(Arrays.asList("DELETE", "electric", "gas"), resp);
    }
  }

  @PlaywrightTest
  @DisplayName("RO-045 should support the times parameter with route matching")
  void shouldSupportTheTimesParameterWithRouteMatching(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Supplier<Object> fetchBody = () -> page.evaluate("() => fetch('/empty.html').then(response => response.text())");

    List<Integer> intercepted = new ArrayList<>();
    page.route("**/empty.html", route -> {
      intercepted.add(1);
      route.fulfill(new Route.FulfillOptions().setBody("first"));
    }, new Page.RouteOptions().setTimes(1));
    assertEquals("first", fetchBody.get());
    page.route("**/empty.html", route -> route.fulfill(new Route.FulfillOptions().setBody("second")), new Page.RouteOptions().setTimes(1));
    assertEquals("second", fetchBody.get());
    fetchBody.get();
    fetchBody.get();
    assertEquals(1, intercepted.size());
  }

  @PlaywrightTest
  @DisplayName("RO-046 should work if handler with times parameter was removed from another handler")
  @Fixme("java: the Router keeps iterating over the handlers it had before page.unroute() ran in a handler, so the removed handler still runs: expected: <[second]> but was: <[second, first]>")
  void shouldWorkIfHandlerWithTimesParameterWasRemovedFromAnotherHandler(Page page, TestServer server) {
    List<String> intercepted = new ArrayList<>();
    Consumer<Route> handler = route -> {
      intercepted.add("first");
      route.resume();
    };
    page.route("**/*", handler, new Page.RouteOptions().setTimes(1));
    page.route("**/*", route -> {
      intercepted.add("second");
      page.unroute("**/*", handler);
      route.fallback();
    });
    page.navigate(server.EMPTY_PAGE);
    assertEquals(Arrays.asList("second"), intercepted);
    intercepted.clear();
    page.navigate(server.EMPTY_PAGE);
    assertEquals(Arrays.asList("second"), intercepted);
  }

  @PlaywrightTest
  @DisplayName("RO-047 should support async handler w/ times")
  void shouldSupportAsyncHandlerWTimes(Page page, TestServer server) {
    page.route("**/empty.html", route -> {
      page.waitForTimeout(100);
      route.fulfill(new Route.FulfillOptions()
        .setBody("<html>intercepted</html>")
        .setContentType("text/html"));
    }, new Page.RouteOptions().setTimes(1));
    page.navigate(server.EMPTY_PAGE);
    assertThat(page.locator("body")).hasText("intercepted");
    page.navigate(server.EMPTY_PAGE);
    assertThat(page.locator("body")).not().hasText("intercepted");
  }

  @PlaywrightTest
  @DisplayName("RO-048 route abort with times: 1 should not affect second sequential fetch")
  void routeAbortWithTimes1ShouldNotAffectSecondSequentialFetch(Page page, TestServer server, BrowserName browserName, Browser browser) {
    assumeFalse(browserName == BrowserName.CHROMIUM && browserMajorVersion(browser) < 155, "Chromium drops a request that is intercepted while Fetch.disable is being processed; fix is not rolled yet");

    server.setRoute("/data", (req, res) -> {
      res.writeHead(200, "Content-Type", "text/plain");
      res.end("ok");
    });

    page.navigate(server.EMPTY_PAGE);
    page.route("**/data", route -> route.abort("timedout"), new Page.RouteOptions().setTimes(1));

    Object results = page.evaluate("async () => {\n" +
      "  async function fetchOrHung(url) {\n" +
      "    const timeout = new Promise(resolve => setTimeout(() => resolve('hung'), 3000));\n" +
      "    const request = fetch(url).then(r => String(r.status)).catch(() => 'aborted');\n" +
      "    return Promise.race([request, timeout]);\n" +
      "  }\n" +
      "  const first = await fetchOrHung('/data');\n" +
      "  const second = await fetchOrHung('/data');\n" +
      "  return [first, second];\n" +
      "}");

    assertEquals(Arrays.asList("aborted", "200"), results);
  }

  @PlaywrightTest
  @DisplayName("RO-049 should contain raw request header")
  void shouldContainRawRequestHeader(Page page, TestServer server) {
    Map<String, String>[] headers = new Map[]{null};
    page.route("**/*", route -> {
      headers[0] = route.request().allHeaders();
      route.resume();
    });
    page.navigate(server.PREFIX + "/empty.html");
    assertTrue(truthy(headers[0].get("accept")));
  }

  @PlaywrightTest
  @DisplayName("RO-050 should contain sec-fetch headers in route")
  @DisabledOnBrowser(value = BrowserName.CHROMIUM, reason = "fail: Fetch.requestPaused fires before Chromium attaches Fetch Metadata headers")
  void shouldContainSecFetchHeadersInRoute(Page page, TestServer server, BrowserName browserName) throws Exception {
    List<String> modes = Arrays.asList("cors", "no-cors", "same-origin");

    page.navigate(server.EMPTY_PAGE);

    Map<String, Map<String, Map<String, String>>> routed = new HashMap<>();
    page.route("**/probe-*", route -> {
      Request request = route.request();
      String path = URI.create(request.url()).getPath();
      Map<String, Map<String, String>> entry = new HashMap<>();
      entry.put("headers", secFetch(request.headers()));
      entry.put("allHeaders", secFetch(request.allHeaders()));
      routed.put(path, entry);
      route.resume();
    });

    List<CompletableFuture<TestServer.Request>> serverRequestPromises = new ArrayList<>();
    for (String mode : modes)
      serverRequestPromises.add(server.waitForRequest("/probe-" + mode));
    page.evaluate("async modes => {\n" +
      "  for (const mode of modes)\n" +
      "    await (await fetch(`/probe-${mode}`, { mode: mode })).text();\n" +
      "}", modes);
    List<TestServer.Request> serverRequests = new ArrayList<>();
    for (CompletableFuture<TestServer.Request> promise : serverRequestPromises)
      serverRequests.add(promise.get());

    List<Executable> soft = new ArrayList<>();
    for (int i = 0; i < modes.size(); i++) {
      String path = "/probe-" + modes.get(i);
      Map<String, String> onServer = secFetch(serverRequests.get(i).headers);
      // The server does receive the browser-computed Fetch Metadata.
      Map<String, String> expected = new HashMap<>();
      expected.put("sec-fetch-site", "same-origin");
      expected.put("sec-fetch-mode", modes.get(i));
      expected.put("sec-fetch-dest", "empty");
      soft.add(() -> assertEquals(expected, onServer, path + " on server"));
      // The route handler should see the same headers.
      soft.add(() -> assertEquals(onServer, routed.get(path).get("headers"), path + " route.request().headers()"));
      soft.add(() -> assertEquals(onServer, routed.get(path).get("allHeaders"), path + " route.request().allHeaders()"));
    }
    assertAll(soft);
  }

  private static Map<String, String> secFetch(Map<String, String> headers) {
    Map<String, String> result = new HashMap<>();
    for (Map.Entry<String, String> entry : headers.entrySet()) {
      if (entry.getKey().toLowerCase().startsWith("sec-fetch-"))
        result.put(entry.getKey(), entry.getValue());
    }
    return result;
  }

  @PlaywrightTest
  @DisplayName("RO-051 should contain raw response header")
  void shouldContainRawResponseHeader(Page page, TestServer server) {
    Request[] request = {null};
    page.route("**/*", route -> {
      request[0] = route.request();
      route.resume();
    });
    page.navigate(server.PREFIX + "/empty.html");
    Response response = request[0].response();
    Map<String, String> headers = response.allHeaders();
    assertTrue(truthy(headers.get("content-type")));
  }

  @PlaywrightTest
  @DisplayName("RO-052 should contain raw response header after fulfill")
  void shouldContainRawResponseHeaderAfterFulfill(Page page, TestServer server) {
    Request[] request = {null};
    page.route("**/*", route -> {
      request[0] = route.request();
      route.fulfill(new Route.FulfillOptions()
        .setStatus(200)
        .setBody("Hello")
        .setContentType("text/html"));
    });
    page.navigate(server.PREFIX + "/empty.html");
    Response response = request[0].response();
    Map<String, String> headers = response.allHeaders();
    assertTrue(truthy(headers.get("content-type")));
  }

  @Nested
  @ParameterizedClass
  @ValueSource(strings = {"fulfill", "continue", "fallback", "abort"})
  class RouteMethodShouldThrowIfCalledTwice {
    @Parameter
    String method;

    // Java: route[method]() is a switch on the method name.
    private void call(Route route) {
      switch (method) {
        case "fulfill": route.fulfill(); break;
        case "continue": route.resume(); break;
        case "fallback": route.fallback(); break;
        case "abort": route.abort(); break;
        default: throw new IllegalStateException(method);
      }
    }

    @PlaywrightTest
    @DisplayName("RO-053 route.${method} should throw if called twice")
    void shouldThrowIfCalledTwice(Page page, TestServer server) throws Exception {
      AtomicReference<Route> resolvePromise = new AtomicReference<>();
      // Java: the second call comes after the navigation, as route.fallback() in the handler does not mark the route handled until the handler returns.
      page.route("**/*", route -> {
        resolvePromise.set(route);
        call(route);
      });
      try {
        page.navigate(server.PREFIX + "/empty.html");
      } catch (PlaywrightException e) {
        // Java: aborting the route fails the navigation; upstream does not await it.
      }
      Route route = resolvePromise.get();
      PlaywrightException e = assertThrows(PlaywrightException.class, () -> call(route));
      assertTrue(e.getMessage().contains("Route is already handled!"));
    }
  }

  @PlaywrightTest
  @DisplayName("RO-054 should intercept when postData is more than 1MB")
  void shouldInterceptWhenPostDataIsMoreThan1MB(Page page, TestServer server) throws Exception {
    page.navigate(server.EMPTY_PAGE);
    CompletableFuture<String> interceptionPromise = new CompletableFuture<>();
    String POST_BODY = new String(new char[2 * 1024 * 1024]).replace('\0', '0'); // 2MB
    page.route("**/404.html", route -> {
      route.abort();
      interceptionPromise.complete(route.request().postData());
    });
    page.evaluate("POST_BODY => fetch('/404.html', {\n" +
      "  method: 'POST',\n" +
      "  body: POST_BODY,\n" +
      "}).catch(e => {})", POST_BODY);
    assertEquals(POST_BODY, interceptionPromise.get());
  }

  @PlaywrightTest
  @DisplayName("RO-055 should be able to intercept every navigation to a page controlled by service worker")
  void shouldBeAbleToInterceptEveryNavigationToAPageControlledByServiceWorker(Page page, TestServer server, BrowserName browserName) {
    int[] interceptions = {0};
    String URL = server.PREFIX + "/serviceworkers/bug-33561/index.html";
    page.route(URL, route -> {
      ++interceptions[0];
      route.resume();
    });

    page.navigate(URL);
    page.evaluate("() => window['activationPromise']");
    page.navigate(URL);
    assertEquals(2, interceptions[0]);
  }
}
