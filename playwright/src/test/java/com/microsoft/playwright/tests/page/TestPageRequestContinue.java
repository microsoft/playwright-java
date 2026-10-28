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

import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-request-continue.spec.ts. */
public class TestPageRequestContinue {
  private static boolean truthy(String value) {
    return value != null && !value.isEmpty();
  }

  @PlaywrightTest
  @DisplayName("RO-057 should work")
  void shouldWork(Page page, TestServer server) {
    page.route("**/*", route -> route.resume());
    page.navigate(server.EMPTY_PAGE);
  }

  @PlaywrightTest
  @DisplayName("RO-058 should amend HTTP headers")
  void shouldAmendHTTPHeaders(Page page, TestServer server) throws Exception {
    page.route("**/*", route -> {
      Map<String, String> headers = new HashMap<>(route.request().headers());
      headers.put("FOO", "bar");
      route.resume(new Route.ResumeOptions().setHeaders(headers));
    });
    page.navigate(server.EMPTY_PAGE);
    CompletableFuture<TestServer.Request> request = server.waitForRequest("/sleep.zzz");
    page.evaluate("() => fetch('/sleep.zzz')");
    assertEquals("bar", request.get().headers.get("foo"));
  }

  @PlaywrightTest
  @DisplayName("RO-059 should not allow to override unsafe HTTP headers")
  void shouldNotAllowToOverrideUnsafeHTTPHeaders(Page page, TestServer server, BrowserName browserName) throws Exception {
    CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/empty.html");
    // Java: the route handler runs the code that upstream runs after the pending navigation.
    page.route("**/*", route -> {
      Map<String, String> headers = new HashMap<>(route.request().headers());
      headers.put("host", "bar");
      headers.put("trailer", "baz");
      route.resume(new Route.ResumeOptions().setHeaders(headers));
    });
    page.navigate(server.EMPTY_PAGE);
    TestServer.Request serverRequest = serverRequestPromise.get();
    assertNull(serverRequest.headers.get("trailer"));
    assertEquals(URI.create(server.EMPTY_PAGE).getAuthority(), serverRequest.headers.get("host"));
  }

  @PlaywrightTest
  @DisplayName("RO-060 should delete header with undefined value")
  @Fixme("java: a null header value in route.resume() headers throws PlaywrightException 'Value cannot be null' instead of deleting the header")
  void shouldDeleteHeaderWithUndefinedValue(Page page, TestServer server, BrowserName browserName) throws Exception {
    page.navigate(server.PREFIX + "/empty.html");
    server.setRoute("/something", (request, response) -> {
      response.writeHead(200, "Access-Control-Allow-Origin", "*");
      response.end("done");
    });
    Request[] interceptedRequest = {null};
    page.route(server.PREFIX + "/something", route -> {
      Request request = route.request();
      interceptedRequest[0] = request;
      Map<String, String> headers = new HashMap<>(request.allHeaders());
      // Java: undefined is null.
      headers.put("foo", null);
      route.resume(new Route.ResumeOptions().setHeaders(headers));
    });

    CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/something");
    Object text = page.evaluate("async url => {\n" +
      "  const data = await fetch(url, {\n" +
      "    headers: {\n" +
      "      foo: 'a',\n" +
      "      bar: 'b',\n" +
      "    }\n" +
      "  });\n" +
      "  return data.text();\n" +
      "}", server.PREFIX + "/something");
    TestServer.Request serverRequest = serverRequestPromise.get();
    assertEquals("done", text);
    assertNull(interceptedRequest[0].headers().get("foo"));
    assertFalse(truthy(serverRequest.headers.get("foo")));
    assertEquals("b", serverRequest.headers.get("bar"));
  }

  @PlaywrightTest
  @DisplayName("RO-061 should amend method")
  void shouldAmendMethod(Page page, TestServer server) throws Exception {
    CompletableFuture<TestServer.Request> sRequest = server.waitForRequest("/sleep.zzz");
    page.navigate(server.EMPTY_PAGE);
    page.route("**/*", route -> route.resume(new Route.ResumeOptions().setMethod("POST")));
    CompletableFuture<TestServer.Request> request = server.waitForRequest("/sleep.zzz");
    page.evaluate("() => fetch('/sleep.zzz')");
    assertEquals("POST", request.get().method);
    assertEquals("POST", sRequest.get().method);
  }

  @PlaywrightTest
  @DisplayName("RO-062 should override request url")
  void shouldOverrideRequestUrl(Page page, TestServer server) throws Exception {
    CompletableFuture<TestServer.Request> serverRequest = server.waitForRequest("/global-var.html");
    page.route("**/foo", route -> {
      route.resume(new Route.ResumeOptions().setUrl(server.PREFIX + "/global-var.html"));
    });
    Response response = page.navigate(server.PREFIX + "/foo");
    assertEquals(server.PREFIX + "/global-var.html", response.request().url());
    assertEquals(server.PREFIX + "/global-var.html", response.url());
    assertEquals(123, page.evaluate("() => window['globalVar']"));
    assertEquals("GET", serverRequest.get().method);
  }

  @PlaywrightTest
  @DisplayName("RO-063 should not allow changing protocol when overriding url")
  @Fixme("java: route.resume() does not wait for the driver's reply, so 'New URL must have same protocol as overridden URL' is never thrown (error is null)")
  void shouldNotAllowChangingProtocolWhenOverridingUrl(Page page, TestServer server) throws Exception {
    CompletableFuture<Throwable> errorPromise = new CompletableFuture<>();
    page.route("**/*", route -> {
      try {
        route.resume(new Route.ResumeOptions().setUrl("file:///tmp/foo"));
        errorPromise.complete(null);
      } catch (Exception e) {
        errorPromise.complete(e);
        // Java: upstream leaves the navigation pending, it is aborted here so that the inline navigate returns.
        route.abort();
      }
    });
    try {
      page.navigate(server.EMPTY_PAGE);
    } catch (PlaywrightException e) {
      // Java: upstream does not await the navigation.
    }
    Throwable error = errorPromise.get();
    assertNotNull(error);
    assertTrue(error.getMessage().contains("New URL must have same protocol as overridden URL"));
  }

  @PlaywrightTest
  @DisplayName("RO-064 should not throw if request was cancelled by the page")
  void shouldNotThrowIfRequestWasCancelledByThePage(Page page, TestServer server) throws Exception {
    // Java: there is no page.waitForRequestFailed(), the event is collected with a handler.
    CompletableFuture<Request> failurePromise = new CompletableFuture<>();
    page.onRequestFailed(request -> failurePromise.complete(request));
    AtomicReference<Throwable> resumeError = new AtomicReference<>();
    // Java: the route handler runs the code that upstream runs after the pending evaluate: the page aborts the request, then the route is resumed.
    page.route("**/data.json", route -> {
      page.evaluate("() => globalThis.controller.abort()");
      page.context().waitForCondition(() -> failurePromise.isDone());
      try {
        route.resume(); // Should not throw.
      } catch (RuntimeException e) {
        resumeError.set(e);
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
    assertNull(resumeError.get());
  }

  @PlaywrightTest
  @DisplayName("RO-065 should override method along with url")
  void shouldOverrideMethodAlongWithUrl(Page page, TestServer server) throws Exception {
    CompletableFuture<TestServer.Request> request = server.waitForRequest("/empty.html");
    page.route("**/foo", route -> {
      route.resume(new Route.ResumeOptions()
        .setUrl(server.EMPTY_PAGE)
        .setMethod("POST"));
    });
    page.navigate(server.PREFIX + "/foo");
    assertEquals("POST", request.get().method);
  }

  @PlaywrightTest
  @DisplayName("RO-066 should amend method on main request")
  void shouldAmendMethodOnMainRequest(Page page, TestServer server) throws Exception {
    CompletableFuture<TestServer.Request> request = server.waitForRequest("/empty.html");
    page.route("**/*", route -> route.resume(new Route.ResumeOptions().setMethod("POST")));
    page.navigate(server.EMPTY_PAGE);
    assertEquals("POST", request.get().method);
  }

  @Nested
  class PostData {
    @PlaywrightTest
    @DisplayName("RO-067 should amend post data")
    void shouldAmendPostData(Page page, TestServer server) throws Exception {
      page.navigate(server.EMPTY_PAGE);
      page.route("**/*", route -> {
        route.resume(new Route.ResumeOptions().setPostData("doggo"));
      });
      CompletableFuture<TestServer.Request> serverRequest = server.waitForRequest("/sleep.zzz");
      page.evaluate("() => fetch('/sleep.zzz', { method: 'POST', body: 'birdy' })");
      assertEquals("doggo", new String(serverRequest.get().postBody(), StandardCharsets.UTF_8));
    }

    @PlaywrightTest
    @DisplayName("RO-068 should compute content-length from post data")
    void shouldComputeContentLengthFromPostData(Page page, TestServer server) throws Exception {
      page.navigate(server.EMPTY_PAGE);
      String data = new String(new char[7500]).replace('\0', 'a');
      page.route("**/*", route -> {
        Map<String, String> headers = new HashMap<>(route.request().headers());
        headers.put("content-type", "application/json");
        route.resume(new Route.ResumeOptions().setPostData(data).setHeaders(headers));
      });
      CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/sleep.zzz");
      page.evaluate("() => fetch('/sleep.zzz', { method: 'PATCH', body: 'birdy' })");
      TestServer.Request serverRequest = serverRequestPromise.get();
      assertEquals(data, new String(serverRequest.postBody(), StandardCharsets.UTF_8));
      assertEquals(String.valueOf(data.length()), serverRequest.headers.get("content-length"));
      assertEquals("application/json", serverRequest.headers.get("content-type"));
    }

    @PlaywrightTest
    @DisplayName("RO-069 should amend method and post data")
    void shouldAmendMethodAndPostData(Page page, TestServer server) throws Exception {
      page.navigate(server.EMPTY_PAGE);
      page.route("**/*", route -> {
        route.resume(new Route.ResumeOptions().setMethod("POST").setPostData("doggo"));
      });
      CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/sleep.zzz");
      page.evaluate("() => fetch('/sleep.zzz', { method: 'GET' })");
      TestServer.Request serverRequest = serverRequestPromise.get();
      assertEquals("POST", serverRequest.method);
      assertEquals("doggo", new String(serverRequest.postBody(), StandardCharsets.UTF_8));
    }

    @PlaywrightTest
    @DisplayName("RO-070 should amend utf8 post data")
    void shouldAmendUtf8PostData(Page page, TestServer server) throws Exception {
      page.navigate(server.EMPTY_PAGE);
      page.route("**/*", route -> {
        route.resume(new Route.ResumeOptions().setPostData("пушкин"));
      });
      CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/sleep.zzz");
      page.evaluate("() => fetch('/sleep.zzz', { method: 'POST', body: 'birdy' })");
      TestServer.Request serverRequest = serverRequestPromise.get();
      assertEquals("POST", serverRequest.method);
      assertEquals("пушкин", new String(serverRequest.postBody(), StandardCharsets.UTF_8));
    }

    @PlaywrightTest
    @DisplayName("RO-071 should amend longer post data")
    void shouldAmendLongerPostData(Page page, TestServer server) throws Exception {
      page.navigate(server.EMPTY_PAGE);
      page.route("**/*", route -> {
        route.resume(new Route.ResumeOptions().setPostData("doggo-is-longer-than-birdy"));
      });
      CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/sleep.zzz");
      page.evaluate("() => fetch('/sleep.zzz', { method: 'POST', body: 'birdy' })");
      TestServer.Request serverRequest = serverRequestPromise.get();
      assertEquals("POST", serverRequest.method);
      assertEquals("doggo-is-longer-than-birdy", new String(serverRequest.postBody(), StandardCharsets.UTF_8));
    }

    @PlaywrightTest
    @DisplayName("RO-072 should amend binary post data")
    void shouldAmendBinaryPostData(Page page, TestServer server) throws Exception {
      page.navigate(server.EMPTY_PAGE);
      // Java: Buffer.from(arr) is a byte[].
      byte[] arr = new byte[256];
      for (int i = 0; i < arr.length; i++)
        arr[i] = (byte) i;
      page.route("**/*", route -> {
        route.resume(new Route.ResumeOptions().setPostData(arr));
      });
      CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/sleep.zzz");
      page.evaluate("() => fetch('/sleep.zzz', { method: 'POST', body: 'birdy' })");
      TestServer.Request serverRequest = serverRequestPromise.get();
      assertEquals("POST", serverRequest.method);
      byte[] buffer = serverRequest.postBody();
      assertEquals(arr.length, buffer.length);
      for (int i = 0; i < arr.length; ++i)
        assertEquals(buffer[i], arr[i]);
    }

    @PlaywrightTest
    @DisplayName("RO-073 should use content-type from original request")
    void shouldUseContentTypeFromOriginalRequest(Page page, TestServer server, BrowserName browserName) throws Exception {
      page.navigate(server.EMPTY_PAGE);
      page.route(server.PREFIX + "/title.html", route -> route.resume(new Route.ResumeOptions().setPostData("{\"b\":2}")));
      CompletableFuture<TestServer.Request> requestPromise = server.waitForRequest("/title.html");
      page.evaluate("async url => {\n" +
        "  await fetch(url, {\n" +
        "    method: 'POST',\n" +
        "    body: '{\"a\":1}',\n" +
        "    headers: { 'content-type': 'application/json' },\n" +
        "  });\n" +
        "}", server.PREFIX + "/title.html");
      TestServer.Request request = requestPromise.get();
      assertEquals("application/json", request.headers.get("content-type"));
      assertEquals("{\"b\":2}", new String(request.postBody(), StandardCharsets.UTF_8));
    }
  }

  @PlaywrightTest
  @DisplayName("RO-074 should work with Cross-Origin-Opener-Policy")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "fail: https://github.com/microsoft/playwright/issues/8796")
  void shouldWorkWithCrossOriginOpenerPolicy(Page page, TestServer server, BrowserName browserName) throws Exception {
    AtomicReference<Map<String, String>> serverHeaders = new AtomicReference<>();
    List<String> serverRequests = Collections.synchronizedList(new ArrayList<>());
    server.setRoute("/empty.html", (req, res) -> {
      serverRequests.add(req.url);
      serverHeaders.compareAndSet(null, req.headers);
      res.setHeader("Cross-Origin-Opener-Policy", "same-origin");
      // Note: without 'onload', Firefox sometimes does not fire the load event
      // over the protocol. The reason is unclear.
      res.end("\n" +
        "      <div>Hello there!</div>\n" +
        "      <script>window.onload = () => console.log('onload')</script>\n" +
        "    ");
    });

    List<String> intercepted = new ArrayList<>();
    page.route("**/*", route -> {
      intercepted.add(route.request().url());
      route.resume(new Route.ResumeOptions().setHeaders(Collections.singletonMap("foo", "bar")));
    });
    Set<Request> requests = new HashSet<>();
    List<String> events = new ArrayList<>();
    page.onRequest(r -> {
      events.add("request");
      requests.add(r);
    });
    page.onRequestFailed(r -> {
      events.add("requestfailed");
      requests.add(r);
    });
    page.onRequestFinished(r -> {
      events.add("requestfinished");
      requests.add(r);
    });
    page.onResponse(r -> {
      events.add("response");
      requests.add(r.request());
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(Arrays.asList(server.EMPTY_PAGE), intercepted);
    // There should be only one request to the server.
    if (browserName == BrowserName.WEBKIT)
      assertEquals(Arrays.asList("/empty.html", "/empty.html"), serverRequests);
    else
      assertEquals(Arrays.asList("/empty.html"), serverRequests);
    assertEquals("bar", serverHeaders.get().get("foo"));
    assertEquals(server.EMPTY_PAGE, page.url());
    response.finished();
    assertEquals(Arrays.asList("request", "response", "requestfinished"), events);
    assertEquals(1, requests.size());
    assertNull(response.request().failure());
  }

  @PlaywrightTest
  @DisplayName("RO-075 should not delete the origin header")
  void shouldNotDeleteTheOriginHeader(Page page, TestServer server) throws Exception {
    page.navigate(server.PREFIX + "/empty.html");
    server.setRoute("/something", (request, response) -> {
      response.writeHead(200, "Access-Control-Allow-Origin", "*");
      response.end("done");
    });
    String[] interceptedOrigin = {null};
    page.route(server.CROSS_PROCESS_PREFIX + "/something", route -> {
      Map<String, String> headers = new HashMap<>(route.request().allHeaders());
      interceptedOrigin[0] = headers.get("origin");
      headers.remove("origin");
      route.resume(new Route.ResumeOptions().setHeaders(headers));
    });

    CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/something");
    Object text = page.evaluate("async url => {\n" +
      "  const data = await fetch(url);\n" +
      "  return data.text();\n" +
      "}", server.CROSS_PROCESS_PREFIX + "/something");
    TestServer.Request serverRequest = serverRequestPromise.get();
    assertEquals("done", text);
    assertEquals(server.PREFIX, interceptedOrigin[0]);
    assertEquals(server.PREFIX, serverRequest.headers.get("origin"));
  }

  @PlaywrightTest
  @DisplayName("RO-076 should continue preload link requests")
  void shouldContinuePreloadLinkRequests(Page page, TestServer server) throws Exception {
    boolean[] intercepted = {false};
    page.route("**/one-style.css", route -> {
      intercepted[0] = true;
      Map<String, String> headers = new HashMap<>(route.request().headers());
      headers.put("custom", "value");
      route.resume(new Route.ResumeOptions().setHeaders(headers));
    });
    CompletableFuture<TestServer.Request> serverRequest = server.waitForRequest("/one-style.css");
    page.navigate(server.PREFIX + "/preload.html");
    // Java: the route handler only runs while a call is pending, so wait for the request to reach the server.
    page.context().waitForCondition(() -> serverRequest.isDone());
    assertEquals("value", serverRequest.get().headers.get("custom"));
    page.waitForFunction("() => window.preloadedStyles");
    assertEquals(true, intercepted[0]);
    Object color = page.evaluate("() => window.getComputedStyle(document.body).backgroundColor");
    assertEquals("rgb(255, 192, 203)", color);
  }

  @PlaywrightTest
  @DisplayName("RO-077 should respect set-cookie in redirect response")
  void shouldRespectSetCookieInRedirectResponse(Page page, TestServer server) throws Exception {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a href=\"/set-cookie-redirect\">Set cookie</a>");
    server.setRoute("/set-cookie-redirect", (request, response) -> {
      response.writeHead(302,
        "set-cookie", "foo=bar;  max-age=36000",
        "location", "/empty.html");
      response.end();
    });
    page.route("**/set-cookie-redirect", route -> {
      route.resume(new Route.ResumeOptions().setHeaders(new HashMap<>(route.request().headers())));
    });
    CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/empty.html");
    page.navigate(server.PREFIX + "/set-cookie-redirect");
    TestServer.Request serverRequest = serverRequestPromise.get();
    // expect.soft
    assertAll(
      () -> assertEquals("foo=bar", serverRequest.headers.get("cookie")),
      () -> assertEquals("foo=bar", page.evaluate("() => document.cookie")));
  }

  @PlaywrightTest
  @DisplayName("RO-078 continue should not propagate cookie override to redirects")
  void continueShouldNotPropagateCookieOverrideToRedirects(Page page, TestServer server, BrowserName browserName) throws Exception {
    server.setRoute("/set-cookie", (request, response) -> {
      response.writeHead(200, "Set-Cookie", "foo=bar;");
      response.end();
    });
    page.navigate(server.PREFIX + "/set-cookie");
    assertEquals("foo=bar", page.evaluate("() => document.cookie"));
    server.setRedirect("/redirect", server.PREFIX + "/empty.html");
    page.route("**/redirect", route -> {
      Map<String, String> headers = new HashMap<>(route.request().headers());
      headers.put("cookie", "override");
      route.resume(new Route.ResumeOptions().setHeaders(headers));
    });
    CompletableFuture<TestServer.Request> serverRequest = server.waitForRequest("/empty.html");
    page.navigate(server.PREFIX + "/redirect");
    assertEquals("foo=bar", serverRequest.get().headers.get("cookie"));
  }

  @PlaywrightTest
  @DisplayName("RO-079 continue should not override cookie")
  void continueShouldNotOverrideCookie(Page page, TestServer server) throws Exception {
    server.setRoute("/set-cookie", (request, response) -> {
      response.writeHead(200, "Set-Cookie", "foo=bar;");
      response.end();
    });
    page.navigate(server.PREFIX + "/set-cookie");
    assertEquals("foo=bar", page.evaluate("() => document.cookie"));
    page.route("**", route -> {
      Map<String, String> headers = new HashMap<>(route.request().headers());
      headers.put("cookie", "override");
      headers.put("custom", "value");
      route.resume(new Route.ResumeOptions().setHeaders(headers));
    });
    CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/empty.html");
    page.navigate(server.EMPTY_PAGE);
    TestServer.Request serverRequest = serverRequestPromise.get();
    // Original cookie from the browser's cookie jar should be sent.
    assertEquals("foo=bar", serverRequest.headers.get("cookie"));
    assertEquals("value", serverRequest.headers.get("custom"));
  }

  @PlaywrightTest
  @DisplayName("RO-080 continue with headers should send fresh cookie from the browser cookie store")
  void continueWithHeadersShouldSendFreshCookieFromTheBrowserCookieStore(Page page, TestServer server) throws Exception {
    server.setRoute("/set-cookie", (request, response) -> {
      response.writeHead(200, "Set-Cookie", "foo=v1;");
      response.end();
    });
    page.navigate(server.PREFIX + "/set-cookie");
    assertEquals("foo=v1", page.evaluate("() => document.cookie"));

    page.route("**/empty.html", route -> {
      // Cookie store changes between interception and continuation.
      page.context().addCookies(Arrays.asList(new Cookie("foo", "v2").setUrl(server.PREFIX)));
      route.resume(new Route.ResumeOptions().setHeaders(route.request().headers()));
    });

    CompletableFuture<TestServer.Request> serverRequest = server.waitForRequest("/empty.html");
    page.navigate(server.EMPTY_PAGE);
    // The fresh cookie from the browser cookie store should be sent, not the stale captured value.
    assertEquals("foo=v2", serverRequest.get().headers.get("cookie"));
  }

  @PlaywrightTest
  @DisplayName("RO-081 continue with headers should send fresh cookie after a redirect")
  void continueWithHeadersShouldSendFreshCookieAfterARedirect(Page page, TestServer server) throws Exception {
    server.setRoute("/set-cookie", (request, response) -> {
      response.writeHead(200, "Set-Cookie", "foo=v1;");
      response.end();
    });
    page.navigate(server.PREFIX + "/set-cookie");
    assertEquals("foo=v1", page.evaluate("() => document.cookie"));

    // The redirect updates the cookie before bouncing to the final destination.
    server.setRoute("/redirect", (request, response) -> {
      response.writeHead(302,
        "Set-Cookie", "foo=v2;",
        "location", server.PREFIX + "/empty.html");
      response.end();
    });

    page.route("**/redirect", route -> {
      route.resume(new Route.ResumeOptions().setHeaders(route.request().headers()));
    });

    CompletableFuture<TestServer.Request> serverRequest = server.waitForRequest("/empty.html");
    page.navigate(server.PREFIX + "/redirect");
    // The redirected request should carry the cookie updated by the redirect, not the stale captured value.
    assertEquals("foo=v2", serverRequest.get().headers.get("cookie"));
  }

  @PlaywrightTest
  @DisplayName("RO-082 redirect after continue should be able to delete cookie")
  void redirectAfterContinueShouldBeAbleToDeleteCookie(Page page, TestServer server) throws Exception {
    server.setRoute("/set-cookie", (request, response) -> {
      response.writeHead(200, "Set-Cookie", "foo=bar;");
      response.end();
    });
    page.navigate(server.PREFIX + "/set-cookie");
    assertEquals("foo=bar", page.evaluate("() => document.cookie"));

    server.setRoute("/delete-cookie", (request, response) -> {
      response.writeHead(200, "Set-Cookie", "foo=bar; expires=Thu, 01 Jan 1970 00:00:00 GMT");
      response.end();
    });
    server.setRedirect("/redirect", "/delete-cookie");
    page.route("**/redirect", route -> {
      route.resume(new Route.ResumeOptions().setHeaders(new HashMap<>(route.request().headers())));
    });
    page.navigate(server.PREFIX + "/redirect");
    CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/empty.html");
    page.navigate(server.EMPTY_PAGE);
    TestServer.Request serverRequest = serverRequestPromise.get();
    assertFalse(truthy(serverRequest.headers.get("cookie")));
  }

  @PlaywrightTest
  @DisplayName("RO-083 continue should propagate headers to redirects")
  void continueShouldPropagateHeadersToRedirects(Page page, TestServer server) throws Exception {
    server.setRedirect("/redirect", "/empty.html");
    page.route("**/redirect", route -> {
      Map<String, String> headers = new HashMap<>(route.request().headers());
      headers.put("custom", "value");
      route.resume(new Route.ResumeOptions().setHeaders(headers));
    });
    CompletableFuture<TestServer.Request> serverRequest = server.waitForRequest("/empty.html");
    page.navigate(server.PREFIX + "/redirect");
    assertEquals("value", serverRequest.get().headers.get("custom"));
  }

  @PlaywrightTest
  @DisplayName("RO-084 continue should drop content-length on redirects")
  void continueShouldDropContentLengthOnRedirects(Page page, TestServer server) throws Exception {
    page.navigate(server.EMPTY_PAGE);

    server.setRedirect("/redirect", "/empty.html");
    page.route("**/redirect", route -> {
      Map<String, String> headers = new HashMap<>(route.request().headers());
      headers.put("custom", "value");
      route.resume(new Route.ResumeOptions().setHeaders(headers));
    });
    CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/empty.html");
    page.evaluate("url => fetch(url, { method: 'POST', body: 'foo' })", server.PREFIX + "/redirect");
    TestServer.Request serverRequest = serverRequestPromise.get();
    // expect.soft
    assertAll(
      () -> assertEquals("GET", serverRequest.method),
      () -> assertNull(serverRequest.headers.get("content-length")),
      () -> assertNull(serverRequest.headers.get("content-type")),
      () -> assertEquals("value", serverRequest.headers.get("custom")));
  }

  @PlaywrightTest
  @DisplayName("RO-085 redirected requests should report overridden headers")
  void redirectedRequestsShouldReportOverriddenHeaders(Page page, TestServer server) throws Exception {
    server.setRedirect("/redirect", "/empty.html");
    page.route("**/redirect", route -> {
      Map<String, String> headers = route.request().headers();
      headers = new HashMap<>(headers);
      headers.put("custom", "value");
      route.fallback(new Route.FallbackOptions().setHeaders(headers));
    });

    CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/empty.html");
    Response response = page.navigate(server.PREFIX + "/redirect");
    TestServer.Request serverRequest = serverRequestPromise.get();
    assertEquals("value", serverRequest.headers.get("custom"));
    assertEquals(server.EMPTY_PAGE, response.request().url());
    assertEquals("value", response.request().headers().get("custom"));
    assertEquals("value", response.request().allHeaders().get("custom"));
  }

  @PlaywrightTest
  @DisplayName("RO-086 continue should delete headers on redirects")
  @Fixme("java: a null header value in route.resume() headers throws PlaywrightException 'Value cannot be null' instead of deleting the header")
  void continueShouldDeleteHeadersOnRedirects(Page page, TestServer server) throws Exception {
    page.navigate(server.PREFIX + "/empty.html");
    server.setRoute("/something", (request, response) -> {
      response.writeHead(200, "Access-Control-Allow-Origin", "*");
      response.end("done");
    });
    server.setRedirect("/redirect", "/something");
    page.route("**/redirect", route -> {
      Map<String, String> headers = new HashMap<>(route.request().headers());
      // Java: undefined is null.
      headers.put("foo", null);
      route.resume(new Route.ResumeOptions().setHeaders(headers));
    });
    CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/something");
    Object text = page.evaluate("async url => {\n" +
      "  const data = await fetch(url, {\n" +
      "    headers: {\n" +
      "      foo: 'a',\n" +
      "    }\n" +
      "  });\n" +
      "  return data.text();\n" +
      "}", server.PREFIX + "/redirect");
    TestServer.Request serverRequest = serverRequestPromise.get();
    assertEquals("done", text);
    assertFalse(truthy(serverRequest.headers.get("foo")));
  }

  @PlaywrightTest
  @DisplayName("RO-087 propagate headers same origin redirect")
  void propagateHeadersSameOriginRedirect(Page page, TestServer server) throws Exception {
    page.navigate(server.PREFIX + "/empty.html");
    CompletableFuture<TestServer.Request> serverRequestPromise = new CompletableFuture<>();
    server.setRoute("/something", (request, response) -> {
      if (request.method.equals("OPTIONS")) {
        response.writeHead(204,
          "Access-Control-Allow-Origin", server.PREFIX,
          "Access-Control-Allow-Credentials", "true",
          "Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE",
          "Access-Control-Allow-Headers", "authorization,cookie,custom");
        response.end();
        return;
      }
      serverRequestPromise.complete(request);
      response.writeHead(200);
      response.end("done");
    });
    server.setRedirect("/redirect", "/something");
    page.evaluate("() => document.cookie = 'a=b'");
    Object text = page.evaluate("async url => {\n" +
      "  const data = await fetch(url, {\n" +
      "    headers: {\n" +
      "      authorization: 'credentials',\n" +
      "      custom: 'foo'\n" +
      "    },\n" +
      "    credentials: 'include',\n" +
      "  });\n" +
      "  return data.text();\n" +
      "}", server.PREFIX + "/redirect");
    assertEquals("done", text);
    TestServer.Request serverRequest = serverRequestPromise.get();
    // expect.soft
    assertAll(
      () -> assertEquals("credentials", serverRequest.headers.get("authorization")),
      () -> assertEquals("a=b", serverRequest.headers.get("cookie")),
      () -> assertEquals("foo", serverRequest.headers.get("custom")));
  }

  @PlaywrightTest
  @DisplayName("RO-088 propagate headers cross origin")
  void propagateHeadersCrossOrigin(Page page, TestServer server) throws Exception {
    page.navigate(server.PREFIX + "/empty.html");
    CompletableFuture<TestServer.Request> serverRequestPromise = new CompletableFuture<>();
    server.setRoute("/something", (request, response) -> {
      if (request.method.equals("OPTIONS")) {
        response.writeHead(204,
          "Access-Control-Allow-Origin", server.PREFIX,
          "Access-Control-Allow-Credentials", "true",
          "Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE",
          "Access-Control-Allow-Headers", "authorization,custom");
        response.end();
        return;
      }
      serverRequestPromise.complete(request);
      response.writeHead(200,
        "Access-Control-Allow-Origin", server.PREFIX,
        "Access-Control-Allow-Credentials", "true");
      response.end("done");
    });
    Object text = page.evaluate("async url => {\n" +
      "  const data = await fetch(url, {\n" +
      "    headers: {\n" +
      "      authorization: 'credentials',\n" +
      "      custom: 'foo'\n" +
      "    },\n" +
      "    credentials: 'include',\n" +
      "  });\n" +
      "  return data.text();\n" +
      "}", server.CROSS_PROCESS_PREFIX + "/something");
    assertEquals("done", text);
    TestServer.Request serverRequest = serverRequestPromise.get();
    // expect.soft
    assertAll(
      () -> assertEquals("credentials", serverRequest.headers.get("authorization")),
      () -> assertEquals("foo", serverRequest.headers.get("custom")));
  }

  @PlaywrightTest
  @DisplayName("RO-089 propagate headers cross origin redirect")
  void propagateHeadersCrossOriginRedirect(Page page, TestServer server) throws Exception {
    page.navigate(server.PREFIX + "/empty.html");
    CompletableFuture<TestServer.Request> serverRequestPromise = new CompletableFuture<>();
    server.setRoute("/something", (request, response) -> {
      if (request.method.equals("OPTIONS")) {
        response.writeHead(204,
          "Access-Control-Allow-Origin", server.PREFIX,
          "Access-Control-Allow-Credentials", "true",
          "Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE",
          "Access-Control-Allow-Headers", "authorization,cookie,custom");
        response.end();
        return;
      }
      serverRequestPromise.complete(request);
      response.writeHead(200,
        "Access-Control-Allow-Origin", server.PREFIX,
        "Access-Control-Allow-Credentials", "true");
      response.end("done");
    });
    server.setRoute("/redirect", (request, response) -> {
      response.writeHead(301, "location", server.CROSS_PROCESS_PREFIX + "/something");
      response.end();
    });
    page.evaluate("() => document.cookie = 'a=b'");
    Object text = page.evaluate("async url => {\n" +
      "  const data = await fetch(url, {\n" +
      "    headers: {\n" +
      "      authorization: 'credentials',\n" +
      "      custom: 'foo'\n" +
      "    },\n" +
      "    credentials: 'include',\n" +
      "  });\n" +
      "  return data.text();\n" +
      "}", server.PREFIX + "/redirect");
    assertEquals("done", text);
    TestServer.Request serverRequest = serverRequestPromise.get();
    // Authorization header not propagated to cross-origin redirect.
    // expect.soft
    assertAll(
      () -> assertFalse(truthy(serverRequest.headers.get("authorization"))),
      () -> assertFalse(truthy(serverRequest.headers.get("cookie"))),
      () -> assertEquals("foo", serverRequest.headers.get("custom")));
  }

  @PlaywrightTest
  @DisplayName("RO-090 propagate headers cross origin redirect after interception")
  void propagateHeadersCrossOriginRedirectAfterInterception(Page page, TestServer server, BrowserName browserName) throws Exception {
    page.navigate(server.PREFIX + "/empty.html");
    CompletableFuture<TestServer.Request> serverRequestPromise = new CompletableFuture<>();
    server.setRoute("/something", (request, response) -> {
      if (request.method.equals("OPTIONS")) {
        response.writeHead(204,
          "Access-Control-Allow-Origin", server.PREFIX,
          "Access-Control-Allow-Credentials", "true",
          "Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE",
          "Access-Control-Allow-Headers", "authorization,cookie,custom");
        response.end();
        return;
      }
      serverRequestPromise.complete(request);
      response.writeHead(200,
        "Access-Control-Allow-Origin", server.PREFIX,
        "Access-Control-Allow-Credentials", "true");
      response.end("done");
    });
    server.setRoute("/redirect", (request, response) -> {
      response.writeHead(301, "location", server.CROSS_PROCESS_PREFIX + "/something");
      response.end();
    });
    page.evaluate("() => document.cookie = 'a=b'");
    page.route("**/redirect", route -> {
      Map<String, String> headers = new HashMap<>(route.request().headers());
      headers.put("authorization", "credentials");
      headers.put("custom", "foo");
      route.resume(new Route.ResumeOptions().setHeaders(headers));
    });
    Object text = page.evaluate("async url => {\n" +
      "  const data = await fetch(url, {\n" +
      "    headers: {\n" +
      "      authorization: 'none',\n" +
      "    },\n" +
      "    credentials: 'include',\n" +
      "  });\n" +
      "  return data.text();\n" +
      "}", server.PREFIX + "/redirect");
    assertEquals("done", text);
    TestServer.Request serverRequest = serverRequestPromise.get();
    // expect.soft
    assertAll(
      () -> {
        if (browserName == BrowserName.WEBKIT)
          assertFalse(truthy(serverRequest.headers.get("authorization")));
        else
          assertEquals("credentials", serverRequest.headers.get("authorization"));
      },
      () -> assertFalse(truthy(serverRequest.headers.get("cookie"))),
      () -> assertEquals("foo", serverRequest.headers.get("custom")));
  }

  @PlaywrightTest
  @DisplayName("RO-091 continue should pass on 307 cross-origin redirect")
  void continueShouldPassOn307CrossOriginRedirect(Page page, TestServer server) throws Exception {
    server.setRoute("/final", (request, response) -> {
      response.writeHead(200, "content-type", "text/html");
      response.end("<!doctype html><title>final</title><p>ok</p>");
    });
    // Cross-origin 307 redirect that preserves the POST method.
    server.setRoute("/redirect307", (request, response) -> {
      response.writeHead(307, "location", server.PREFIX + "/final");
      response.end();
    });

    page.navigate(server.PREFIX + "/empty.html");
    page.setContent("\n" +
      "    <form id=\"f\" method=\"POST\" action=\"" + server.CROSS_PROCESS_PREFIX + "/redirect307\">\n" +
      "      <input type=\"submit\">\n" +
      "    </form>");

    page.route("**/*", route -> route.resume());
    page.locator("input").click();
    // Java: the wait comes after the click, the URL stays reached.
    page.waitForURL(server.PREFIX + "/final");
    assertThat(page.locator("p")).hasText("ok");
  }

  @PlaywrightTest
  @DisplayName("RO-092 should intercept css variable with background url")
  void shouldInterceptCssVariableWithBackgroundUrl(Page page, TestServer server) throws Exception {
    server.setRoute("/test.html", (request, response) -> {
      response.setHeader("Content-Type", "text/html");
      response.end("\n" +
        "    <style>\n" +
        "      @keyframes JNDzq {\n" +
        "        0% { background-position: 0 0 }\n" +
        "        to { background-position: 100 0 }\n" +
        "      }\n" +
        "      div {\n" +
        "        --background: url(/pptr.png);\n" +
        "        background-image: var(--background);\n" +
        "        animation: JNDzq 1s linear infinite;\n" +
        "      }\n" +
        "    </style>\n" +
        "    <div>Yo!</div>");
    });
    CompletableFuture<Void> interceptPromise = new CompletableFuture<>();
    int[] interceptedRequests = {0};
    page.route(server.PREFIX + "/pptr.png", route -> {
      ++interceptedRequests[0];
      interceptPromise.complete(null);
      route.resume();
    });
    page.navigate(server.PREFIX + "/test.html");
    assertEquals("Yo!", page.locator("div").textContent());
    // Java: the route handler only runs while a call is pending, so wait for it to settle the promise.
    page.context().waitForCondition(() -> interceptPromise.isDone());
    page.waitForTimeout(1000);
    assertEquals(1, interceptedRequests[0]);
  }

  private static TestServer.Request sendFormData(Page page, TestServer server) throws Exception {
    CompletableFuture<TestServer.Request> reqPromise = server.waitForRequest("/upload");
    Object status = page.evaluate("async () => {\n" +
      "  const newFile = new File(['file content'], 'file.txt');\n" +
      "  const formData = new FormData();\n" +
      "  formData.append('file', newFile);\n" +
      "  const response = await fetch('/upload', {\n" +
      "    method: 'POST',\n" +
      "    credentials: 'include',\n" +
      "    body: formData,\n" +
      "  });\n" +
      "  return response.status;\n" +
      "}");
    TestServer.Request req = reqPromise.get();
    assertEquals(200, status);
    return req;
  }

  @PlaywrightTest
  @DisplayName("RO-093 continue should not change multipart/form-data body")
  void continueShouldNotChangeMultipartFormDataBody(Page page, TestServer server, BrowserName browserName) throws Exception {
    page.navigate(server.EMPTY_PAGE);
    server.setRoute("/upload", (request, response) -> {
      response.writeHead(200, "Content-Type", "text/plain");
      response.end("done");
    });
    TestServer.Request reqBefore = sendFormData(page, server);
    page.route("**/*", route -> {
      route.resume();
    });
    TestServer.Request reqAfter = sendFormData(page, server);
    String fileContent = String.join("\r\n", Arrays.asList(
      "Content-Disposition: form-data; name=\"file\"; filename=\"file.txt\"",
      "Content-Type: application/octet-stream",
      "",
      "file content",
      "------"));
    // expect.soft
    assertAll(
      () -> assertTrue(new String(reqBefore.postBody(), StandardCharsets.UTF_8).contains(fileContent)),
      () -> assertTrue(new String(reqAfter.postBody(), StandardCharsets.UTF_8).contains(fileContent)));
  }

  @PlaywrightTest
  @DisplayName("RO-094 should not forward Host header on cross-origin redirect")
  void shouldNotForwardHostHeaderOnCrossOriginRedirect(Page page, TestServer server, BrowserName browserName) {
    String redirectTargetPath = "/final";
    String redirectSourcePath = "/redirect";

    String[] redirectedHost = {null};
    server.setRoute(redirectTargetPath, (req, res) -> {
      redirectedHost[0] = req.headers.get("host");
      res.end("OK");
    });

    String[] firstHost = {null};
    server.setRoute(redirectSourcePath, (req, res) -> {
      firstHost[0] = req.headers.get("host");
      res.writeHead(302, "location", server.CROSS_PROCESS_PREFIX + redirectTargetPath);
      res.end();
    });

    page.route("**/*", route -> {
      Map<String, String> headers = route.request().headers();
      if (browserName == BrowserName.FIREFOX)
        assertTrue(headers.containsKey("host"));
      else
        assertFalse(headers.containsKey("host"));
      route.resume(new Route.ResumeOptions().setHeaders(headers));
    });

    Response response = page.navigate(server.PREFIX + redirectSourcePath);
    assertEquals(200, response.status());
    assertEquals(URI.create(server.PREFIX).getAuthority(), firstHost[0]);
    assertEquals(URI.create(server.CROSS_PROCESS_PREFIX).getAuthority(), redirectedHost[0]);
  }

  @PlaywrightTest
  @DisplayName("RO-095 postData should return empty string when overriding body with empty string")
  void postDataShouldReturnEmptyStringWhenOverridingBodyWithEmptyString(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.route("**/*", route -> {
      route.resume(new Route.ResumeOptions().setPostData(""));
    });
    Request request = page.waitForRequest("**", () -> {
      page.evaluate("({ url }) => fetch(url, { method: 'POST', body: 'original' })", Collections.singletonMap("url", server.PREFIX + "/sleep.zzz"));
    });
    assertEquals("", request.postData());
  }
}
