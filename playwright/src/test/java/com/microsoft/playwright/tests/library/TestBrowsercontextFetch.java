/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
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

package com.microsoft.playwright.tests.library;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.ContextFactory;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.EnabledOnBrowser;
import com.microsoft.playwright.fixtures.HttpsTestServer;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.ProxyServer;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.impl.PlaywrightImpl;
import com.microsoft.playwright.impl.driver.Driver;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.FilePayload;
import com.microsoft.playwright.options.FormData;
import com.microsoft.playwright.options.HttpCredentials;
import com.microsoft.playwright.options.HttpCredentialsSend;
import com.microsoft.playwright.options.Proxy;
import com.microsoft.playwright.options.RequestOptions;
import com.microsoft.playwright.options.SameSiteAttribute;
import com.microsoft.playwright.options.Timing;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.ChildProcessFactory.findFreePort;
import static com.microsoft.playwright.fixtures.Utils.asset;
import static com.microsoft.playwright.fixtures.Utils.kTargetClosedErrorMessage;
import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/library/browsercontext-fetch.spec.ts. */
public class TestBrowsercontextFetch {
  // Java: there is no __testHookLookup option. The tests that pass it resolve the host names through a
  // proxy that forwards every request to the test server.
  private static BrowserContext contextWithResolvingProxy(ContextFactory contextFactory, ProxyServer proxyServer, TestServer server) {
    proxyServer.forwardTo(server.PORT, new ProxyServer.ForwardOptions().setAllowConnectRequests(true));
    return contextFactory.newContext(new Browser.NewContextOptions().setProxy(new Proxy(proxyServer.URL)));
  }

  @PlaywrightTest
  @DisplayName("FE-088 get should work @smoke")
  void getShouldWork(BrowserContext context, TestServer server) {
    APIResponse response = context.request().get(server.PREFIX + "/simple.json");
    assertEquals(server.PREFIX + "/simple.json", response.url());
    assertEquals(200, response.status());
    assertEquals("OK", response.statusText());
    assertTrue(response.ok());
    assertEquals("application/json; charset=utf-8", response.headers().get("content-type"));
    assertTrue(response.headersArray().stream().anyMatch(h -> h.name.equals("Content-Type") && h.value.equals("application/json; charset=utf-8")));
    assertEquals("{\"foo\": \"bar\"}\n", response.text());
  }

  @PlaywrightTest
  @DisplayName("FE-089 fetch should work")
  void fetchShouldWork(BrowserContext context, TestServer server) {
    APIResponse response = context.request().fetch(server.PREFIX + "/simple.json");
    assertEquals(server.PREFIX + "/simple.json", response.url());
    assertEquals(200, response.status());
    assertEquals("OK", response.statusText());
    assertTrue(response.ok());
    assertEquals("application/json; charset=utf-8", response.headers().get("content-type"));
    assertTrue(response.headersArray().stream().anyMatch(h -> h.name.equals("Content-Type") && h.value.equals("application/json; charset=utf-8")));
    assertEquals("{\"foo\": \"bar\"}\n", response.text());
  }

  @PlaywrightTest
  @DisplayName("FE-090 should return timing")
  void shouldReturnTiming(BrowserContext context) throws Exception {
    // Create a fresh server to guarantee a new connection, because keep-alive
    // sockets from other tests do not have dns/connect timings.
    TestServer httpServer = TestServer.createHttp(asset(""), 0);
    httpServer.setRoute("/", (req, res) -> res.end("Hello"));
    int port = httpServer.PORT;
    try {
      APIResponse response = context.request().get("http://localhost:" + port + "/");
      assertTrue(response.ok());
      Timing timing = response.timing();
      assertTrue(Math.abs(timing.startTime - System.currentTimeMillis()) < 5000);
      assertEquals(0, timing.domainLookupStart);
      assertTrue(timing.domainLookupEnd >= timing.domainLookupStart);
      assertEquals(timing.domainLookupEnd, timing.connectStart);
      assertEquals(-1, timing.secureConnectionStart);
      assertTrue(timing.connectEnd >= timing.connectStart);
      assertEquals(timing.connectEnd, timing.requestStart);
      assertTrue(timing.responseStart >= timing.requestStart);
      assertTrue(timing.responseEnd >= timing.responseStart);
      assertTrue(timing.responseEnd < 60_000);
    } finally {
      httpServer.stop();
    }
  }

  @PlaywrightTest
  @DisplayName("FE-091 should return timing for https")
  void shouldReturnTimingForHttps(BrowserContext context) throws Exception {
    // Create a fresh server to guarantee a new connection, because keep-alive
    // sockets from other tests do not have dns/connect timings.
    HttpsTestServer httpsServer = TestServer.createHttps(asset(""), 0);
    httpsServer.setRoute("/", (req, res) -> res.end("Hello"));
    int port = httpsServer.PORT;
    try {
      APIResponse response = context.request().get("https://localhost:" + port + "/", RequestOptions.create().setIgnoreHTTPSErrors(true));
      assertTrue(response.ok());
      Timing timing = response.timing();
      assertTrue(Math.abs(timing.startTime - System.currentTimeMillis()) < 5000);
      assertEquals(0, timing.domainLookupStart);
      assertTrue(timing.domainLookupEnd >= timing.domainLookupStart);
      assertEquals(timing.domainLookupEnd, timing.connectStart);
      assertTrue(timing.secureConnectionStart >= timing.connectStart);
      assertTrue(timing.connectEnd >= timing.secureConnectionStart);
      assertEquals(timing.connectEnd, timing.requestStart);
      assertTrue(timing.responseStart >= timing.requestStart);
      assertTrue(timing.responseEnd >= timing.responseStart);
    } finally {
      httpsServer.stop();
    }
  }

  @PlaywrightTest
  @DisplayName("FE-092 should throw on network error")
  @Fixme("java: the driver error is \"socket hang up (ECONNRESET)\", without the \"apiRequestContext.get: \" prefix of the Node.js client")
  void shouldThrowOnNetworkError(BrowserContext context, TestServer server) {
    server.setRoute("/test", (req, res) -> {
      req.socket().destroy();
    });
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> context.request().get(server.PREFIX + "/test"));
    assertTrue(error.getMessage().contains("apiRequestContext.get: socket hang up"));
  }

  @PlaywrightTest
  @DisplayName("FE-093 should throw on network error after redirect")
  @Fixme("java: the driver error is \"socket hang up (ECONNRESET)\", without the \"apiRequestContext.get: \" prefix of the Node.js client")
  void shouldThrowOnNetworkErrorAfterRedirect(BrowserContext context, TestServer server) {
    server.setRedirect("/redirect", "/test");
    server.setRoute("/test", (req, res) -> {
      req.socket().destroy();
    });
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> context.request().get(server.PREFIX + "/redirect"));
    assertTrue(error.getMessage().contains("apiRequestContext.get: socket hang up"));
  }

  @PlaywrightTest
  @DisplayName("FE-094 should throw on network error when sending body")
  @Fixme("java: the driver error is \"aborted\", without the \"apiRequestContext.get: \" prefix of the Node.js client")
  void shouldThrowOnNetworkErrorWhenSendingBody(BrowserContext context, TestServer server) {
    server.setRoute("/test", (req, res) -> {
      res.writeHead(200, "content-length", "4096", "content-type", "text/html");
      res.write("<title>A");
      res.uncork();
      req.socket().destroy();
    });
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> context.request().get(server.PREFIX + "/test"));
    assertTrue(error.getMessage().contains("apiRequestContext.get: aborted"));
  }

  @PlaywrightTest
  @DisplayName("FE-095 should throw on network error when sending body after redirect")
  @Fixme("java: the driver error is \"aborted\", without the \"apiRequestContext.get: \" prefix of the Node.js client")
  void shouldThrowOnNetworkErrorWhenSendingBodyAfterRedirect(BrowserContext context, TestServer server) {
    server.setRedirect("/redirect", "/test");
    server.setRoute("/test", (req, res) -> {
      res.writeHead(200, "content-length", "4096", "content-type", "text/html");
      res.write("<title>A");
      res.uncork();
      req.socket().destroy();
    });
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> context.request().get(server.PREFIX + "/redirect"));
    assertTrue(error.getMessage().contains("apiRequestContext.get: aborted"));
  }

  @PlaywrightTest
  @DisplayName("FE-096 should add session cookies to request")
  void shouldAddSessionCookiesToRequest(ContextFactory contextFactory, ProxyServer proxyServer, TestServer server) throws Exception {
    // Java: the context resolves www.my.playwright.dev through the proxy, instead of __testHookLookup.
    BrowserContext context = contextWithResolvingProxy(contextFactory, proxyServer, server);
    context.addCookies(Arrays.asList(new Cookie("username", "John Doe")
      .setDomain(".my.playwright.dev")
      .setPath("/")
      .setExpires(-1)
      .setHttpOnly(false)
      .setSecure(false)
      .setSameSite(SameSiteAttribute.LAX)));
    CompletableFuture<TestServer.Request> reqFuture = server.waitForRequest("/simple.json");
    context.request().get("http://www.my.playwright.dev:" + server.PORT + "/simple.json");
    TestServer.Request req = reqFuture.get();
    assertEquals("username=John Doe", req.headers.get("cookie"));
  }

  @PlaywrightTest
  @DisplayName("FE-097 should filter cookies by domain")
  void shouldFilterCookiesByDomain(ContextFactory contextFactory, ProxyServer proxyServer, TestServer server) throws Exception {
    // Java: the context resolves my.playwright.dev through the proxy, instead of __testHookLookup.
    BrowserContext context = contextWithResolvingProxy(contextFactory, proxyServer, server);
    context.addCookies(Arrays.asList(new Cookie("first", "1")
      .setDomain("playwright.dev")
      .setPath("/")
      .setExpires(-1)
      .setHttpOnly(false)
      .setSecure(false)
      .setSameSite(SameSiteAttribute.LAX), new Cookie("second", "2")
      .setDomain(".playwright.dev")
      .setPath("/")
      .setExpires(-1)
      .setHttpOnly(false)
      .setSecure(false)
      .setSameSite(SameSiteAttribute.LAX)));
    CompletableFuture<TestServer.Request> reqFuture = server.waitForRequest("/simple.json");
    context.request().get("http://my.playwright.dev:" + server.PORT + "/simple.json");
    TestServer.Request req = reqFuture.get();
    assertEquals("second=2", req.headers.get("cookie"));
  }

  @Nested
  @ParameterizedClass
  @ValueSource(strings = {"fetch", "delete", "get", "head", "patch", "post", "put"})
  class Methods {
    @Parameter
    String method;

    @PlaywrightTest
    @DisplayName("FE-098 ${method} should support params passed as object")
    void methodShouldSupportParamsPassedAsObject(BrowserContext context, TestServer server) throws Exception {
      URL url = new URL(server.EMPTY_PAGE);
      SearchParams searchParams = new SearchParams("");
      searchParams.set("param1", "value1");
      searchParams.set("парам2", "знач2");

      CompletableFuture<TestServer.Request> requestFuture = server.waitForRequest(url.getPath() + "?" + searchParams);
      // Java: context.request[method](...) is the call of the method that has this name.
      APIResponse response = request(context.request(), method, server.EMPTY_PAGE, RequestOptions.create()
        .setQueryParam("param1", "value1")
        .setQueryParam("парам2", "знач2"));
      TestServer.Request request = requestFuture.get();

      SearchParams requestParams = new SearchParams(request.url.substring(request.url.indexOf('?')));
      assertEquals("value1", requestParams.get("param1"));
      assertEquals("знач2", requestParams.get("парам2"));

      SearchParams responseParams = new SearchParams(new URL(response.url()).getQuery());
      assertEquals("value1", responseParams.get("param1"));
      assertEquals("знач2", responseParams.get("парам2"));
    }

    // FE-099 ${method} should support params passed as URLSearchParams: not ported, see upstream_not_ported.txt.
    // FE-100 ${method} should support params passed as string: not ported, see upstream_not_ported.txt.
    // FE-101 ${method} should append string and URLSearchParams params to the URL query: not ported, see upstream_not_ported.txt.

    @PlaywrightTest
    @DisplayName("FE-102 ${method} should support failOnStatusCode")
    void methodShouldSupportFailOnStatusCode(BrowserContext context, TestServer server) {
      PlaywrightException error = assertThrows(PlaywrightException.class, () ->
        request(context.request(), method, server.PREFIX + "/does-not-exist.html", RequestOptions.create().setFailOnStatusCode(true)));
      assertTrue(error.getMessage().contains("404 Not Found"));
      if (!method.equals("head")) {
        assertTrue(error.getMessage().contains("Response text:\nFile not found:"));
      }
    }

    @PlaywrightTest
    @DisplayName("FE-103 ${method}should support ignoreHTTPSErrors option")
    void methodshouldSupportIgnoreHTTPSErrorsOption(BrowserContext context, HttpsTestServer httpsServer) {
      APIResponse response = request(context.request(), method, httpsServer.EMPTY_PAGE, RequestOptions.create().setIgnoreHTTPSErrors(true));
      assertEquals(200, response.status());
    }
  }

  @PlaywrightTest
  @DisplayName("FE-104 should not add context cookie if cookie header passed as a parameter")
  void shouldNotAddContextCookieIfCookieHeaderPassedAsAParameter(ContextFactory contextFactory, ProxyServer proxyServer, TestServer server) throws Exception {
    // Java: the context resolves www.my.playwright.dev through the proxy, instead of __testHookLookup.
    BrowserContext context = contextWithResolvingProxy(contextFactory, proxyServer, server);
    context.addCookies(Arrays.asList(new Cookie("username", "John Doe")
      .setDomain(".my.playwright.dev")
      .setPath("/")
      .setExpires(-1)
      .setHttpOnly(false)
      .setSecure(false)
      .setSameSite(SameSiteAttribute.LAX)));
    CompletableFuture<TestServer.Request> reqFuture = server.waitForRequest("/empty.html");
    context.request().get("http://www.my.playwright.dev:" + server.PORT + "/empty.html", RequestOptions.create()
      .setHeader("Cookie", "foo=bar"));
    TestServer.Request req = reqFuture.get();
    assertEquals("foo=bar", req.headers.get("cookie"));
  }

  @PlaywrightTest
  @DisplayName("FE-105 should follow redirects")
  void shouldFollowRedirects(ContextFactory contextFactory, ProxyServer proxyServer, TestServer server) throws Exception {
    // Java: the context resolves www.my.playwright.dev through the proxy, instead of __testHookLookup.
    BrowserContext context = contextWithResolvingProxy(contextFactory, proxyServer, server);
    server.setRedirect("/redirect1", "/redirect2");
    server.setRedirect("/redirect2", "/simple.json");
    context.addCookies(Arrays.asList(new Cookie("username", "John Doe")
      .setDomain(".my.playwright.dev")
      .setPath("/")
      .setExpires(-1)
      .setHttpOnly(false)
      .setSecure(false)
      .setSameSite(SameSiteAttribute.LAX)));
    CompletableFuture<TestServer.Request> reqFuture = server.waitForRequest("/simple.json");
    APIResponse response = context.request().get("http://www.my.playwright.dev:" + server.PORT + "/redirect1");
    TestServer.Request req = reqFuture.get();
    assertEquals("username=John Doe", req.headers.get("cookie"));
    assertEquals("http://www.my.playwright.dev:" + server.PORT + "/simple.json", response.url());
    // Java: APIResponse has no json(); the text is parsed with Gson.
    assertEquals(mapOf("foo", "bar"), new Gson().fromJson(response.text(), Map.class));
  }

  @PlaywrightTest
  @DisplayName("FE-106 should follow redirects correctly when Location header contains UTF-8 characters")
  void shouldFollowRedirectsCorrectlyWhenLocationHeaderContainsUTF8Characters(BrowserContext context, TestServer server) {
    server.setRoute("/redirect", (req, res) -> {
      // Node.js only allows US-ASCII, so we can't send invalid headers directly. Sending it as a raw response instead.
      res.socket().write("HTTP/1.1 301 Moved Permanently\r\n");
      res.socket().write("Location: " + server.PREFIX + "/empty.html?message=マスクПривет\r\n");
      res.socket().write("\r\n");
      res.socket().uncork();
      res.socket().end();
    });
    APIResponse response = context.request().get(server.PREFIX + "/redirect");
    SearchParams searchParams = new SearchParams("");
    searchParams.set("message", "マスクПривет");
    assertEquals(server.PREFIX + "/empty.html?" + searchParams, response.url());
  }

  @PlaywrightTest
  @DisplayName("FE-107 should add cookies from Set-Cookie header")
  void shouldAddCookiesFromSetCookieHeader(BrowserContext context, Page page, TestServer server) {
    server.setRoute("/setcookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("session=value", "foo=bar; max-age=3600"));
      res.end();
    });
    context.request().get(server.PREFIX + "/setcookie.html");
    List<Cookie> cookies = context.cookies();
    assertEquals(new HashSet<>(Arrays.asList(
      mapOf("name", "session", "value", "value"),
      mapOf("name", "foo", "value", "bar"))),
      cookies.stream().map(c -> mapOf("name", c.name, "value", c.value)).collect(Collectors.toSet()));
    page.navigate(server.EMPTY_PAGE);
    List<String> documentCookie = Arrays.stream(((String) page.evaluate("() => document.cookie")).split(";")).map(String::trim).sorted().collect(Collectors.toList());
    assertEquals(Arrays.asList("foo=bar", "session=value"), documentCookie);
  }

  @PlaywrightTest
  @DisplayName("FE-108 page.request.addCookies should add cookies to the browser context")
  void pageRequestAddCookiesShouldAddCookiesToTheBrowserContext(BrowserContext context, Page page, TestServer server) throws Exception {
    page.request().addCookies(Arrays.asList(
      new Cookie("a", "b").setUrl(server.EMPTY_PAGE),
      new Cookie("c", "d").setDomain("localhost").setPath("/").setExpires(System.currentTimeMillis() / 1000.0 + 3600)));
    assertEquals(Arrays.asList(mapOf("name", "a", "value", "b"), mapOf("name", "c", "value", "d")),
      context.cookies().stream().map(c -> mapOf("name", c.name, "value", c.value)).sorted((a, b) -> ((String) a.get("name")).compareTo((String) b.get("name"))).collect(Collectors.toList()));
    CompletableFuture<TestServer.Request> reqFuture = server.waitForRequest("/empty.html");
    context.request().get(server.EMPTY_PAGE);
    TestServer.Request req = reqFuture.get();
    assertEquals(Arrays.asList("a=b", "c=d"), Arrays.stream(req.headers.get("cookie").split(";")).map(String::trim).sorted().collect(Collectors.toList()));
    page.navigate(server.EMPTY_PAGE);
    assertEquals(Arrays.asList("a=b", "c=d"), Arrays.stream(((String) page.evaluate("() => document.cookie")).split(";")).map(String::trim).sorted().collect(Collectors.toList()));
  }

  @PlaywrightTest
  @DisplayName("FE-109 page.request.cookies should return browser context cookies")
  void pageRequestCookiesShouldReturnBrowserContextCookies(BrowserContext context, Page page, TestServer server) {
    context.addCookies(Arrays.asList(
      new Cookie("a", "b").setUrl(server.EMPTY_PAGE),
      new Cookie("c", "d").setDomain("example.com").setPath("/")));
    assertEquals(Arrays.asList("a", "c"), page.request().cookies().stream().map(c -> c.name).sorted().collect(Collectors.toList()));
    assertEquals(Arrays.asList("a"), page.request().cookies(server.EMPTY_PAGE).stream().map(c -> c.name).collect(Collectors.toList()));
    // Java: Cookie has no equals(); the cookies are compared field by field.
    assertEquals(context.cookies(server.EMPTY_PAGE).stream().map(TestBrowsercontextFetch::cookieFields).collect(Collectors.toList()),
      page.request().cookies(server.EMPTY_PAGE).stream().map(TestBrowsercontextFetch::cookieFields).collect(Collectors.toList()));
  }

  @PlaywrightTest
  @DisplayName("FE-110 page.request.clearCookies should clear browser context cookies")
  void pageRequestClearCookiesShouldClearBrowserContextCookies(BrowserContext context, Page page, TestServer server) throws Exception {
    context.addCookies(Arrays.asList(
      new Cookie("a", "b").setUrl(server.EMPTY_PAGE),
      new Cookie("c", "d").setUrl(server.EMPTY_PAGE)));
    page.request().clearCookies(new APIRequestContext.ClearCookiesOptions().setName("a"));
    assertEquals(Arrays.asList("c"), context.cookies().stream().map(c -> c.name).collect(Collectors.toList()));
    page.request().clearCookies();
    assertEquals(Collections.emptyList(), context.cookies());
    CompletableFuture<TestServer.Request> reqFuture = server.waitForRequest("/empty.html");
    page.request().get(server.EMPTY_PAGE);
    TestServer.Request req = reqFuture.get();
    assertNull(req.headers.get("cookie"));
  }

  @PlaywrightTest
  @DisplayName("FE-111 should preserve cookie order from Set-Cookie header")
  void shouldPreserveCookieOrderFromSetCookieHeader(BrowserContext context, Page page, TestServer server) throws Exception {
    server.setRoute("/setcookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("cookie.0=foo", "cookie.1=bar"));
      res.end();
    });
    page.request().get(server.PREFIX + "/setcookie.html");
    List<Cookie> cookies = context.cookies();
    assertEquals(Arrays.asList(mapOf("name", "cookie.0", "value", "foo"), mapOf("name", "cookie.1", "value", "bar")),
      cookies.stream().map(c -> mapOf("name", c.name, "value", c.value)).collect(Collectors.toList()));
    page.navigate(server.EMPTY_PAGE);
    assertEquals("cookie.0=foo; cookie.1=bar", page.evaluate("() => document.cookie"));
    CompletableFuture<TestServer.Request> requestFuture = server.waitForRequest("/empty.html");
    page.request().get(server.EMPTY_PAGE);
    TestServer.Request request = requestFuture.get();
    assertEquals("cookie.0=foo; cookie.1=bar", request.headers.get("cookie"));
  }

  @PlaywrightTest
  @DisplayName("FE-112 should support cookie with empty value")
  void shouldSupportCookieWithEmptyValue(BrowserContext context, Page page, TestServer server) {
    server.setRoute("/setcookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("first="));
      res.end();
    });
    context.request().get(server.PREFIX + "/setcookie.html");
    page.navigate(server.EMPTY_PAGE);
    assertEquals("first=", page.evaluate("() => document.cookie"));
    List<Cookie> cookies = context.cookies();
    assertEquals(Arrays.asList(mapOf("name", "first", "value", "")),
      cookies.stream().map(c -> mapOf("name", c.name, "value", c.value)).collect(Collectors.toList()));
  }

  @PlaywrightTest
  @DisplayName("FE-113 should not lose body while handling Set-Cookie header")
  void shouldNotLoseBodyWhileHandlingSetCookieHeader(BrowserContext context, TestServer server) {
    server.setRoute("/setcookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("session=value", "foo=bar; max-age=3600"));
      res.end("text content");
    });
    APIResponse response = context.request().get(server.PREFIX + "/setcookie.html");
    assertEquals("text content", response.text());
  }

  @PlaywrightTest
  @DisplayName("FE-114 should remove cookie with negative max-age")
  void shouldRemoveCookieWithNegativeMaxAge(Page page, TestServer server) throws Exception {
    server.setRoute("/setcookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("a=v; max-age=100000", "b=v; max-age=100000", "c=v"));
      res.end();
    });
    server.setRoute("/removecookie.html", (req, res) -> {
      long maxAge = -2 * System.currentTimeMillis();
      res.setHeader("Set-Cookie", Arrays.asList("a=v; max-age=" + maxAge, "b=v; max-age=-1"));
      res.end();
    });
    page.request().get(server.PREFIX + "/setcookie.html");
    page.request().get(server.PREFIX + "/removecookie.html");
    CompletableFuture<TestServer.Request> serverRequestFuture = server.waitForRequest("/empty.html");
    page.request().get(server.EMPTY_PAGE);
    TestServer.Request serverRequest = serverRequestFuture.get();
    assertEquals("c=v", serverRequest.headers.get("cookie"));
  }

  @PlaywrightTest
  @DisplayName("FE-115 should remove cookie with expires far in the past")
  void shouldRemoveCookieWithExpiresFarInThePast(Page page, TestServer server) throws Exception {
    server.setRoute("/setcookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("a=v; max-age=1000000"));
      res.end();
    });
    server.setRoute("/removecookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("a=v; expires=Wed, 01 Jan 1000 00:00:00 GMT"));
      res.end();
    });
    page.request().get(server.PREFIX + "/setcookie.html");
    page.request().get(server.PREFIX + "/removecookie.html");
    CompletableFuture<TestServer.Request> serverRequestFuture = server.waitForRequest("/empty.html");
    page.request().get(server.EMPTY_PAGE);
    TestServer.Request serverRequest = serverRequestFuture.get();
    assertTrue(serverRequest.headers.get("cookie") == null || serverRequest.headers.get("cookie").isEmpty());
  }

  @PlaywrightTest
  @DisplayName("FE-116 should handle cookies on redirects")
  void shouldHandleCookiesOnRedirects(BrowserContext context, TestServer server, BrowserName browserName) throws Exception {
    server.setRoute("/redirect1", (req, res) -> {
      res.setHeader("Set-Cookie", "r1=v1;SameSite=Lax");
      res.writeHead(301, "location", "/a/b/redirect2");
      res.end();
    });
    server.setRoute("/a/b/redirect2", (req, res) -> {
      res.setHeader("Set-Cookie", "r2=v2;SameSite=Lax");
      res.writeHead(302, "location", "/title.html");
      res.end();
    });
    {
      CompletableFuture<TestServer.Request> req1Future = server.waitForRequest("/redirect1");
      CompletableFuture<TestServer.Request> req2Future = server.waitForRequest("/a/b/redirect2");
      CompletableFuture<TestServer.Request> req3Future = server.waitForRequest("/title.html");
      context.request().get(server.PREFIX + "/redirect1");
      TestServer.Request req1 = req1Future.get();
      TestServer.Request req2 = req2Future.get();
      TestServer.Request req3 = req3Future.get();
      assertTrue(req1.headers.get("cookie") == null || req1.headers.get("cookie").isEmpty());
      assertEquals("r1=v1", req2.headers.get("cookie"));
      assertEquals("r1=v1", req3.headers.get("cookie"));
    }
    {
      CompletableFuture<TestServer.Request> req1Future = server.waitForRequest("/redirect1");
      CompletableFuture<TestServer.Request> req2Future = server.waitForRequest("/a/b/redirect2");
      CompletableFuture<TestServer.Request> req3Future = server.waitForRequest("/title.html");
      context.request().get(server.PREFIX + "/redirect1");
      TestServer.Request req1 = req1Future.get();
      TestServer.Request req2 = req2Future.get();
      TestServer.Request req3 = req3Future.get();
      assertEquals("r1=v1", req1.headers.get("cookie"));
      assertEquals(Arrays.asList("r1=v1", "r2=v2"), Arrays.stream(req2.headers.get("cookie").split(";")).map(String::trim).sorted().collect(Collectors.toList()));
      assertEquals("r1=v1", req3.headers.get("cookie"));
    }
    List<Cookie> cookies = context.cookies();
    SameSiteAttribute sameSite = (browserName == BrowserName.WEBKIT && Env.isWindows && !"webkit-wsl".equals(Env.channel())) ? SameSiteAttribute.NONE : SameSiteAttribute.LAX;
    assertEquals(new HashSet<>(Arrays.asList(
      mapOf("sameSite", sameSite, "name", "r2", "value", "v2", "domain", server.HOSTNAME, "path", "/a/b", "expires", -1.0, "httpOnly", false, "secure", false),
      mapOf("sameSite", sameSite, "name", "r1", "value", "v1", "domain", server.HOSTNAME, "path", "/", "expires", -1.0, "httpOnly", false, "secure", false))),
      cookies.stream().map(TestBrowsercontextFetch::cookieFields).collect(Collectors.toSet()));
  }

  @PlaywrightTest
  @DisplayName("FE-117 should return raw headers")
  void shouldReturnRawHeaders(BrowserContext context, Page page, TestServer server) {
    server.setRoute("/headers", (req, res) -> {
      // Headers array is only supported since Node v14.14.0 so we write directly to the socket.
      // res.writeHead(200, ['name-a', 'v1','name-b', 'v4','Name-a', 'v2', 'name-A', 'v3']);
      TestServer.Connection conn = res.connection();
      conn.write("HTTP/1.1 200 OK\r\n");
      conn.write("Name-A: v1\r\n");
      conn.write("name-b: v4\r\n");
      conn.write("Name-a: v2\r\n");
      conn.write("name-A: v3\r\n");
      conn.write("\r\n");
      conn.uncork();
      conn.end();
    });
    APIResponse response = context.request().get(server.PREFIX + "/headers");
    assertEquals(200, response.status());
    List<Map<String, Object>> headers = response.headersArray().stream().filter(h -> h.name.toLowerCase().contains("name-"))
      .map(h -> mapOf("name", h.name, "value", h.value)).collect(Collectors.toList());
    assertEquals(Arrays.asList(mapOf("name", "Name-A", "value", "v1"), mapOf("name", "name-b", "value", "v4"), mapOf("name", "Name-a", "value", "v2"), mapOf("name", "name-A", "value", "v3")), headers);
    // Comma separated values, this matches Response.headers()
    assertEquals("v1, v2, v3", response.headers().get("name-a"));
    assertEquals("v4", response.headers().get("name-b"));
  }

  @PlaywrightTest
  @DisplayName("FE-118 should work with http credentials")
  void shouldWorkWithHttpCredentials(BrowserContext context, TestServer server) throws Exception {
    server.setAuth("/empty.html", "user", "pass");

    CompletableFuture<TestServer.Request> requestFuture = server.waitForRequest("/empty.html");
    APIResponse response = context.request().get(server.EMPTY_PAGE, RequestOptions.create()
      .setHeader("authorization", "Basic " + Base64.getEncoder().encodeToString("user:pass".getBytes(StandardCharsets.UTF_8))));
    TestServer.Request request = requestFuture.get();
    assertEquals(200, response.status());
    assertEquals("/empty.html", request.url);
  }

  // FE-119 should work with setHTTPCredentials: not ported, see upstream_not_ported.txt.
  // FE-120 should return error with wrong credentials: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("FE-121 should support multiple httpCredentials")
  void shouldSupportMultipleHttpCredentials(ContextFactory contextFactory, TestServer server, BrowserName browserName) {
    server.setAuth("/empty.html", "user1", "pass1");
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions().setHttpCredentials(Arrays.asList(
      new HttpCredentials("user1", "pass1").setOrigin(server.PREFIX),
      new HttpCredentials("user2", "pass2").setOrigin(server.CROSS_PROCESS_PREFIX))));
    APIResponse response1 = context.request().get(server.EMPTY_PAGE);
    assertEquals(200, response1.status());
    // Wrong credentials are picked for the other origin.
    APIResponse response2 = context.request().get(server.CROSS_PROCESS_PREFIX + "/empty.html");
    assertEquals(401, response2.status());
  }

  @PlaywrightTest
  @DisplayName("FE-122 should support HTTPCredentials.send for newContext")
  void shouldSupportHTTPCredentialsSendForNewContext(ContextFactory contextFactory, TestServer server) throws Exception {
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions().setHttpCredentials(
      new HttpCredentials("user", "pass").setOrigin(server.PREFIX.toUpperCase()).setSend(HttpCredentialsSend.ALWAYS)));
    {
      CompletableFuture<TestServer.Request> serverRequestFuture = server.waitForRequest("/empty.html");
      APIResponse response = context.request().get(server.EMPTY_PAGE);
      TestServer.Request serverRequest = serverRequestFuture.get();
      assertEquals("Basic " + Base64.getEncoder().encodeToString("user:pass".getBytes(StandardCharsets.UTF_8)), serverRequest.headers.get("authorization"));
      assertEquals(200, response.status());
    }
    {
      CompletableFuture<TestServer.Request> serverRequestFuture = server.waitForRequest("/empty.html");
      APIResponse response = context.request().get(server.CROSS_PROCESS_PREFIX + "/empty.html");
      TestServer.Request serverRequest = serverRequestFuture.get();
      // Not sent to another origin.
      assertNull(serverRequest.headers.get("authorization"));
      assertEquals(200, response.status());
    }
  }

  @PlaywrightTest
  @DisplayName("FE-123 should support HTTPCredentials.send for browser.newPage")
  void shouldSupportHTTPCredentialsSendForBrowserNewPage(ContextFactory contextFactory, TestServer server, Browser browser) throws Exception {
    Page page = browser.newPage(new Browser.NewPageOptions().setHttpCredentials(
      new HttpCredentials("user", "pass").setOrigin(server.PREFIX.toUpperCase()).setSend(HttpCredentialsSend.ALWAYS)));
    {
      CompletableFuture<TestServer.Request> serverRequestFuture = server.waitForRequest("/empty.html");
      APIResponse response = page.request().get(server.EMPTY_PAGE);
      TestServer.Request serverRequest = serverRequestFuture.get();
      assertEquals("Basic " + Base64.getEncoder().encodeToString("user:pass".getBytes(StandardCharsets.UTF_8)), serverRequest.headers.get("authorization"));
      assertEquals(200, response.status());
    }
    {
      CompletableFuture<TestServer.Request> serverRequestFuture = server.waitForRequest("/empty.html");
      APIResponse response = page.request().get(server.CROSS_PROCESS_PREFIX + "/empty.html");
      TestServer.Request serverRequest = serverRequestFuture.get();
      // Not sent to another origin.
      assertNull(serverRequest.headers.get("authorization"));
      assertEquals(200, response.status());
    }
    page.close();
  }

  @PlaywrightTest
  @DisplayName("FE-124 delete should support post data")
  void deleteShouldSupportPostData(BrowserContext context, TestServer server) throws Exception {
    CompletableFuture<TestServer.Request> requestFuture = server.waitForRequest("/simple.json");
    APIResponse response = context.request().delete(server.PREFIX + "/simple.json", RequestOptions.create().setData("My request"));
    TestServer.Request request = requestFuture.get();
    assertEquals("DELETE", request.method);
    assertEquals("My request", request.postBodyText());
    assertEquals(200, response.status());
    assertEquals("/simple.json", request.url);
  }

  @PlaywrightTest
  @DisplayName("FE-125 get should support post data")
  void getShouldSupportPostData(BrowserContext context, TestServer server) throws Exception {
    CompletableFuture<TestServer.Request> requestFuture = server.waitForRequest("/simple.json");
    APIResponse response = context.request().get(server.PREFIX + "/simple.json", RequestOptions.create().setData("My request"));
    TestServer.Request request = requestFuture.get();
    assertEquals("GET", request.method);
    assertEquals("My request", request.postBodyText());
    assertEquals(200, response.status());
    assertEquals("/simple.json", request.url);
  }

  @PlaywrightTest
  @DisplayName("FE-126 head should support post data")
  void headShouldSupportPostData(BrowserContext context, TestServer server) throws Exception {
    CompletableFuture<TestServer.Request> requestFuture = server.waitForRequest("/simple.json");
    APIResponse response = context.request().head(server.PREFIX + "/simple.json", RequestOptions.create().setData("My request"));
    TestServer.Request request = requestFuture.get();
    assertEquals("HEAD", request.method);
    assertEquals("My request", request.postBodyText());
    assertEquals(200, response.status());
    assertEquals("/simple.json", request.url);
  }

  @PlaywrightTest
  @DisplayName("FE-127 patch should support post data")
  void patchShouldSupportPostData(BrowserContext context, TestServer server) throws Exception {
    CompletableFuture<TestServer.Request> requestFuture = server.waitForRequest("/simple.json");
    APIResponse response = context.request().patch(server.PREFIX + "/simple.json", RequestOptions.create().setData("My request"));
    TestServer.Request request = requestFuture.get();
    assertEquals("PATCH", request.method);
    assertEquals("My request", request.postBodyText());
    assertEquals(200, response.status());
    assertEquals("/simple.json", request.url);
  }

  @PlaywrightTest
  @DisplayName("FE-128 post should support post data")
  void postShouldSupportPostData(BrowserContext context, TestServer server) throws Exception {
    CompletableFuture<TestServer.Request> requestFuture = server.waitForRequest("/simple.json");
    APIResponse response = context.request().post(server.PREFIX + "/simple.json", RequestOptions.create().setData("My request"));
    TestServer.Request request = requestFuture.get();
    assertEquals("POST", request.method);
    assertEquals("My request", request.postBodyText());
    assertEquals(200, response.status());
    assertEquals("/simple.json", request.url);
  }

  @PlaywrightTest
  @DisplayName("FE-129 put should support post data")
  void putShouldSupportPostData(BrowserContext context, TestServer server) throws Exception {
    CompletableFuture<TestServer.Request> requestFuture = server.waitForRequest("/simple.json");
    APIResponse response = context.request().put(server.PREFIX + "/simple.json", RequestOptions.create().setData("My request"));
    TestServer.Request request = requestFuture.get();
    assertEquals("PUT", request.method);
    assertEquals("My request", request.postBodyText());
    assertEquals(200, response.status());
    assertEquals("/simple.json", request.url);
  }

  @PlaywrightTest
  @DisplayName("FE-130 should add default headers")
  void shouldAddDefaultHeaders(BrowserContext context, TestServer server, Page page) throws Exception {
    CompletableFuture<TestServer.Request> requestFuture = server.waitForRequest("/empty.html");
    context.request().get(server.EMPTY_PAGE);
    TestServer.Request request = requestFuture.get();
    assertEquals("*/*", request.headers.get("accept"));
    Object userAgent = page.evaluate("() => navigator.userAgent");
    assertEquals(userAgent, request.headers.get("user-agent"));
    assertEquals("gzip,deflate,br", request.headers.get("accept-encoding"));
  }

  @PlaywrightTest
  @DisplayName("FE-131 should send content-length")
  void shouldSendContentLength(BrowserContext context, TestServer server) throws Exception {
    byte[] bytes = new byte[256];
    for (int i = 0; i < 256; i++) {
      bytes[i] = (byte) i;
    }
    CompletableFuture<TestServer.Request> requestFuture = server.waitForRequest("/empty.html");
    context.request().post(server.EMPTY_PAGE, RequestOptions.create().setData(bytes));
    TestServer.Request request = requestFuture.get();
    assertEquals("256", request.headers.get("content-length"));
    assertEquals("application/octet-stream", request.headers.get("content-type"));
  }

  @PlaywrightTest
  @DisplayName("FE-132 should add default headers to redirects")
  void shouldAddDefaultHeadersToRedirects(BrowserContext context, TestServer server, Page page) throws Exception {
    server.setRedirect("/redirect", "/empty.html");
    CompletableFuture<TestServer.Request> requestFuture = server.waitForRequest("/empty.html");
    context.request().get(server.PREFIX + "/redirect");
    TestServer.Request request = requestFuture.get();
    assertEquals("*/*", request.headers.get("accept"));
    Object userAgent = page.evaluate("() => navigator.userAgent");
    assertEquals(userAgent, request.headers.get("user-agent"));
    assertEquals("gzip,deflate,br", request.headers.get("accept-encoding"));
  }

  @PlaywrightTest
  @DisplayName("FE-133 should allow to override default headers")
  void shouldAllowToOverrideDefaultHeaders(BrowserContext context, TestServer server, Page page) throws Exception {
    CompletableFuture<TestServer.Request> requestFuture = server.waitForRequest("/empty.html");
    context.request().get(server.EMPTY_PAGE, RequestOptions.create()
      .setHeader("User-Agent", "Playwright")
      .setHeader("Accept", "text/html")
      .setHeader("Accept-Encoding", "br"));
    TestServer.Request request = requestFuture.get();
    assertEquals("text/html", request.headers.get("accept"));
    assertEquals("Playwright", request.headers.get("user-agent"));
    assertEquals("br", request.headers.get("accept-encoding"));
  }

  @PlaywrightTest
  @DisplayName("FE-134 should propagate custom headers with redirects")
  void shouldPropagateCustomHeadersWithRedirects(BrowserContext context, TestServer server) throws Exception {
    server.setRedirect("/a/redirect1", "/b/c/redirect2");
    server.setRedirect("/b/c/redirect2", "/simple.json");
    CompletableFuture<TestServer.Request> req1Future = server.waitForRequest("/a/redirect1");
    CompletableFuture<TestServer.Request> req2Future = server.waitForRequest("/b/c/redirect2");
    CompletableFuture<TestServer.Request> req3Future = server.waitForRequest("/simple.json");
    context.request().get(server.PREFIX + "/a/redirect1", RequestOptions.create().setHeader("foo", "bar"));
    TestServer.Request req1 = req1Future.get();
    TestServer.Request req2 = req2Future.get();
    TestServer.Request req3 = req3Future.get();
    assertEquals("bar", req1.headers.get("foo"));
    assertEquals("bar", req2.headers.get("foo"));
    assertEquals("bar", req3.headers.get("foo"));
  }

  @PlaywrightTest
  @DisplayName("FE-135 should propagate extra http headers with redirects")
  void shouldPropagateExtraHttpHeadersWithRedirects(BrowserContext context, TestServer server) throws Exception {
    server.setRedirect("/a/redirect1", "/b/c/redirect2");
    server.setRedirect("/b/c/redirect2", "/simple.json");
    context.setExtraHTTPHeaders(mapOfStrings("My-Secret", "Value"));
    CompletableFuture<TestServer.Request> req1Future = server.waitForRequest("/a/redirect1");
    CompletableFuture<TestServer.Request> req2Future = server.waitForRequest("/b/c/redirect2");
    CompletableFuture<TestServer.Request> req3Future = server.waitForRequest("/simple.json");
    context.request().get(server.PREFIX + "/a/redirect1");
    TestServer.Request req1 = req1Future.get();
    TestServer.Request req2 = req2Future.get();
    TestServer.Request req3 = req3Future.get();
    assertEquals("Value", req1.headers.get("my-secret"));
    assertEquals("Value", req2.headers.get("my-secret"));
    assertEquals("Value", req3.headers.get("my-secret"));
  }

  @PlaywrightTest
  @DisplayName("FE-136 should throw on invalid header value")
  void shouldThrowOnInvalidHeaderValue(BrowserContext context, TestServer server) {
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> context.request().get(server.PREFIX + "/a/redirect1", RequestOptions.create()
      .setHeader("foo", "недопустимое значение")));
    assertTrue(error.getMessage().contains("Invalid character in header content"));
  }

  @PlaywrightTest
  @DisplayName("FE-137 should throw on non-http(s) protocol")
  void shouldThrowOnNonHttpSProtocol(BrowserContext context) {
    PlaywrightException error1 = assertThrows(PlaywrightException.class, () -> context.request().get("data:text/plain,test"));
    assertTrue(error1.getMessage().contains("Protocol \"data:\" not supported"));
    PlaywrightException error2 = assertThrows(PlaywrightException.class, () -> context.request().get("file:///tmp/foo"));
    assertTrue(error2.getMessage().contains("Protocol \"file:\" not supported"));
  }

  @PlaywrightTest
  @DisplayName("FE-138 should support https")
  void shouldSupportHttps(BrowserName browserName, HttpsTestServer httpsServer) {
    // Java: upstream sets NODE_TLS_REJECT_UNAUTHORIZED in the process of the test, which is the process of the
    // Playwright server. The driver of the Java client is another process that reads the environment when it
    // starts, so the test starts a new driver with the variable set and uses a context of that one.
    // suppressCertificateWarning() is NODE_NO_WARNINGS.
    Map<String, String> env = new java.util.HashMap<>();
    env.put("PWTEST_UNDER_TEST", "1");
    env.put("NODE_TLS_REJECT_UNAUTHORIZED", "0");
    env.put("NODE_NO_WARNINGS", "1");
    Playwright playwright;
    // Java: creating drivers at the same time fails sometimes when the driver files are extracted; one at a time.
    synchronized (Driver.class) {
      playwright = PlaywrightImpl.createImpl(new Playwright.CreateOptions().setEnv(env), true);
    }
    try {
      Browser browser = browserName.browserType(playwright).launch(Env.launchOptions());
      BrowserContext context = browser.newContext();
      APIResponse response = context.request().get(httpsServer.EMPTY_PAGE);
      assertEquals(200, response.status());
      browser.close();
    } finally {
      playwright.close();
    }
  }

  @PlaywrightTest
  @DisplayName("FE-139 should inherit ignoreHTTPSErrors from context")
  void shouldInheritIgnoreHTTPSErrorsFromContext(ContextFactory contextFactory, HttpsTestServer httpsServer) {
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions().setIgnoreHTTPSErrors(true));
    APIResponse response = context.request().get(httpsServer.EMPTY_PAGE);
    assertEquals(200, response.status());
  }

  @PlaywrightTest
  @DisplayName("FE-140 should resolve url relative to baseURL")
  void shouldResolveUrlRelativeToBaseURL(TestServer server, ContextFactory contextFactory) {
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions().setBaseURL(server.PREFIX));
    APIResponse response = context.request().get("/empty.html");
    assertEquals(server.EMPTY_PAGE, response.url());
  }

  @PlaywrightTest
  @DisplayName("FE-141 should support gzip compression")
  void shouldSupportGzipCompression(BrowserContext context, TestServer server) {
    server.setRoute("/compressed", (req, res) -> {
      res.writeHead(200, "Content-Encoding", "gzip", "Content-Type", "text/plain");
      // Java: the body is compressed at once and written to the response.
      res.write(gzip("Hello, world!"));
      res.end();
    });

    APIResponse response = context.request().get(server.PREFIX + "/compressed");
    assertEquals("Hello, world!", response.text());
  }

  @PlaywrightTest
  @DisplayName("FE-142 should support case-insensitive content-encoding")
  void shouldSupportCaseInsensitiveContentEncoding(BrowserContext context, TestServer server) {
    server.setRoute("/compressed-uppercase", (req, res) -> {
      res.writeHead(200, "Content-Encoding", "GZIP", "Content-Type", "text/plain");
      res.write(gzip("Hello, world!"));
      res.end();
    });

    APIResponse response = context.request().get(server.PREFIX + "/compressed-uppercase");
    assertEquals("Hello, world!", response.text());
  }

  @PlaywrightTest
  @DisplayName("FE-143 should throw informative error on corrupted gzip body")
  void shouldThrowInformativeErrorOnCorruptedGzipBody(BrowserContext context, TestServer server) {
    server.setRoute("/corrupted", (req, res) -> {
      res.writeHead(200, "Content-Encoding", "gzip", "Content-Type", "text/plain");
      res.write("Hello, world!");
      res.end();
    });

    PlaywrightException error = assertThrows(PlaywrightException.class, () -> context.request().get(server.PREFIX + "/corrupted"));
    assertTrue(error.getMessage().contains("failed to decompress 'gzip' encoding"));
  }

  @PlaywrightTest
  @DisplayName("FE-144 should support brotli compression")
  void shouldSupportBrotliCompression(BrowserContext context, TestServer server) {
    server.setRoute("/compressed", (req, res) -> {
      res.writeHead(200, "Content-Encoding", "br", "Content-Type", "text/plain");
      // Java: the JDK has no brotli; these are the bytes of zlib.brotliCompressSync('Hello, world!').
      res.write(new byte[]{11, 6, -128, 72, 101, 108, 108, 111, 44, 32, 119, 111, 114, 108, 100, 33, 3});
      res.end();
    });

    APIResponse response = context.request().get(server.PREFIX + "/compressed");
    assertEquals("Hello, world!", response.text());
  }

  @PlaywrightTest
  @DisplayName("FE-145 should throw informative error on corrupted brotli body")
  void shouldThrowInformativeErrorOnCorruptedBrotliBody(BrowserContext context, TestServer server) {
    server.setRoute("/corrupted", (req, res) -> {
      res.writeHead(200, "Content-Encoding", "br", "Content-Type", "text/plain");
      res.write("Hello, world!");
      res.end();
    });

    PlaywrightException error = assertThrows(PlaywrightException.class, () -> context.request().get(server.PREFIX + "/corrupted"));
    assertTrue(error.getMessage().contains("failed to decompress 'br' encoding"));
  }

  @PlaywrightTest
  @DisplayName("FE-146 should support deflate compression")
  void shouldSupportDeflateCompression(BrowserContext context, TestServer server) {
    server.setRoute("/compressed", (req, res) -> {
      res.writeHead(200, "Content-Encoding", "deflate", "Content-Type", "text/plain");
      res.write(deflate("Hello, world!"));
      res.end();
    });

    APIResponse response = context.request().get(server.PREFIX + "/compressed");
    assertEquals("Hello, world!", response.text());
  }

  @PlaywrightTest
  @DisplayName("FE-147 should throw informative error on corrupted deflate body")
  void shouldThrowInformativeErrorOnCorruptedDeflateBody(BrowserContext context, TestServer server) {
    server.setRoute("/corrupted", (req, res) -> {
      res.writeHead(200, "Content-Encoding", "deflate", "Content-Type", "text/plain");
      res.write("Hello, world!");
      res.end();
    });

    PlaywrightException error = assertThrows(PlaywrightException.class, () -> context.request().get(server.PREFIX + "/corrupted"));
    assertTrue(error.getMessage().contains("failed to decompress 'deflate' encoding"));
  }

  @PlaywrightTest
  @DisplayName("FE-148 should support timeout option")
  @Fixme("java: the driver error is \"Timeout 10ms exceeded.\", without the \"apiRequestContext.get: \" prefix of the Node.js client")
  void shouldSupportTimeoutOption(BrowserContext context, TestServer server) {
    server.setRoute("/slow", (req, res) -> {
      res.writeHead(200, "content-length", "4096", "content-type", "text/html");
    });

    PlaywrightException error = assertThrows(PlaywrightException.class, () -> context.request().get(server.PREFIX + "/slow", RequestOptions.create().setTimeout(10)));
    assertTrue(error.getMessage().contains("apiRequestContext.get: Timeout 10ms exceeded"));
  }

  @PlaywrightTest
  @DisplayName("FE-149 should support a timeout of 0")
  void shouldSupportATimeoutOf0(BrowserContext context, TestServer server) {
    server.setRoute("/slow", (req, res) -> {
      res.writeHead(200, "content-length", "4", "content-type", "text/html");
      // Java: setTimeout(() => res.end('done'), 50) is a sleep in the handler.
      Thread.sleep(50);
      res.end("done");
    });

    APIResponse response = context.request().get(server.PREFIX + "/slow", RequestOptions.create().setTimeout(0));
    assertEquals("done", response.text());
  }

  @PlaywrightTest
  @DisplayName("FE-150 should respect timeout after redirects")
  @Fixme("java: the driver error is \"Timeout 100ms exceeded.\", without the \"apiRequestContext.get: \" prefix of the Node.js client")
  void shouldRespectTimeoutAfterRedirects(BrowserContext context, TestServer server) {
    server.setRedirect("/redirect", "/slow");
    server.setRoute("/slow", (req, res) -> {
      res.writeHead(200, "content-length", "4096", "content-type", "text/html");
    });

    context.setDefaultTimeout(100);
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> context.request().get(server.PREFIX + "/redirect"));
    assertTrue(error.getMessage().contains("apiRequestContext.get: Timeout 100ms exceeded"));
  }

  @PlaywrightTest
  @DisplayName("FE-151 should not hang on a brotli encoded Range request")
  void shouldNotHangOnABrotliEncodedRangeRequest(BrowserContext context, TestServer server) {
    // Java: the JDK has no brotli; these are the bytes of zlib.brotliCompressSync(Buffer.from('A')).
    byte[] encodedRequestPayload = new byte[]{11, 0, -128, 65, 3};
    server.setRoute("/brotli", (req, res) -> {
      res.writeHead(206, "Content-Type", "text/plain", "content-length", "1", "Content-Encoding", "br",
        "content-range", "bytes 0-2/" + encodedRequestPayload.length, "Accept-Ranges", "bytes");
      res.write(Arrays.copyOfRange(encodedRequestPayload, 0, 2));
    });

    PlaywrightException error = assertThrows(PlaywrightException.class, () -> context.request().get(server.PREFIX + "/brotli", RequestOptions.create()
      .setHeader("range", "bytes=0-2")));
    assertTrue(Pattern.compile("Parse Error: Expected HTTP").matcher(error.getMessage()).find());
  }

  @PlaywrightTest
  @DisplayName("FE-152 should dispose")
  void shouldDispose(BrowserContext context, TestServer server) {
    APIResponse response = context.request().get(server.PREFIX + "/simple.json");
    assertEquals(mapOf("foo", "bar"), new Gson().fromJson(response.text(), Map.class));
    response.dispose();
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> response.body());
    assertTrue(error.getMessage().contains("Response has been disposed"));
  }

  @PlaywrightTest
  @DisplayName("FE-153 should dispose when context closes")
  void shouldDisposeWhenContextCloses(BrowserContext context, TestServer server) {
    APIResponse response = context.request().get(server.PREFIX + "/simple.json");
    assertEquals(mapOf("foo", "bar"), new Gson().fromJson(response.text(), Map.class));
    context.close();
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> response.body());
    assertTrue(error.getMessage().contains("Response has been disposed"));
  }

  @PlaywrightTest
  @DisplayName("FE-154 should override request parameters")
  void shouldOverrideRequestParameters(BrowserContext context, Page page, TestServer server) throws Exception {
    com.microsoft.playwright.Request pageReq = page.waitForRequest("**/*", () -> page.navigate(server.EMPTY_PAGE));
    CompletableFuture<TestServer.Request> reqFuture = server.waitForRequest("/empty.html");
    context.request().fetch(pageReq, RequestOptions.create()
      .setMethod("POST")
      .setHeader("foo", "bar")
      .setData("data"));
    TestServer.Request req = reqFuture.get();
    assertEquals("POST", req.method);
    assertEquals("bar", req.headers.get("foo"));
    assertEquals("data", req.postBodyText());
  }

  @PlaywrightTest
  @DisplayName("FE-155 should support application/x-www-form-urlencoded")
  void shouldSupportApplicationXWwwFormUrlencoded(BrowserContext context, Page page, TestServer server) throws Exception {
    CompletableFuture<TestServer.Request> reqFuture = server.waitForRequest("/empty.html");
    context.request().post(server.EMPTY_PAGE, RequestOptions.create().setForm(FormData.create()
      .set("firstName", "John")
      .set("lastName", "Doe")
      .set("file", "f.js")));
    TestServer.Request req = reqFuture.get();
    assertEquals("POST", req.method);
    assertEquals("application/x-www-form-urlencoded", req.headers.get("content-type"));
    String body = req.postBodyText();
    SearchParams params = new SearchParams(body);
    assertEquals(String.valueOf(params.toString().length()), req.headers.get("content-length"));
    assertEquals("John", params.get("firstName"));
    assertEquals("Doe", params.get("lastName"));
    assertEquals("f.js", params.get("file"));
  }

  @PlaywrightTest
  @DisplayName("FE-156 should support application/x-www-form-urlencoded with param lists")
  void shouldSupportApplicationXWwwFormUrlencodedWithParamLists(BrowserContext context, Page page, TestServer server) throws Exception {
    FormData form = FormData.create();
    form.append("foo", "1");
    form.append("foo", "2");
    CompletableFuture<TestServer.Request> reqFuture = server.waitForRequest("/empty.html");
    context.request().post(server.EMPTY_PAGE, RequestOptions.create().setForm(form));
    TestServer.Request req = reqFuture.get();
    assertEquals("POST", req.method);
    assertEquals("application/x-www-form-urlencoded", req.headers.get("content-type"));
    String body = req.postBodyText();
    SearchParams params = new SearchParams(body);
    assertEquals(String.valueOf(params.toString().length()), req.headers.get("content-length"));
    assertEquals(Arrays.asList("1", "2"), params.getAll("foo"));
  }

  @PlaywrightTest
  @DisplayName("FE-157 should encode to application/json by default")
  void shouldEncodeToApplicationJsonByDefault(BrowserContext context, Page page, TestServer server) throws Exception {
    Map<String, Object> data = mapOf(
      "firstName", "John",
      "lastName", "Doe",
      "file", mapOf("name", "f.js"));
    CompletableFuture<TestServer.Request> reqFuture = server.waitForRequest("/empty.html");
    context.request().post(server.EMPTY_PAGE, RequestOptions.create().setData(data));
    TestServer.Request req = reqFuture.get();
    assertEquals("POST", req.method);
    assertEquals("application/json", req.headers.get("content-type"));
    String body = req.postBodyText();
    Object json = new Gson().fromJson(body, Map.class);
    assertEquals(data, json);
  }

  // FE-158 should support multipart/form-data: not ported, see upstream_not_ported.txt.
  // FE-159 should escape quotes and newlines in multipart field and file names: not ported, see upstream_not_ported.txt.
  // FE-160 should support multipart/form-data with ReadStream values: not ported, see upstream_not_ported.txt.
  // FE-161 should support multipart/form-data and keep the order: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("FE-162 should support repeating names in multipart/form-data")
  void shouldSupportRepeatingNamesInMultipartFormData(BrowserContext context, TestServer server) throws Exception {
    CompletableFuture<String> postBodyPromise = new CompletableFuture<>();
    server.setRoute("/empty.html", (req, res) -> {
      postBodyPromise.complete(req.postBodyText());
      res.writeHead(200, "content-type", "text/plain");
      res.end("OK.");
    });
    // Java: there is no File or Blob; a file is a FilePayload with its file name, a Blob gets the file name "blob".
    FormData formData = FormData.create();
    formData.set("name", "John");
    formData.append("name", "Doe");
    formData.append("file", new FilePayload("f1.js", "text/javascript", "var x = 10;\r\n;console.log(x);".getBytes(StandardCharsets.UTF_8)));
    formData.append("file", new FilePayload("custom_f2.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8)));
    formData.append("file", new FilePayload("blob", "text/plain", "boo".getBytes(StandardCharsets.UTF_8)));
    APIResponse response = context.request().post(server.EMPTY_PAGE, RequestOptions.create().setMultipart(formData));
    String postBody = postBodyPromise.get();
    assertTrue(postBody.contains("content-disposition: form-data; name=\"name\"\r\n\r\nJohn"));
    assertTrue(postBody.contains("content-disposition: form-data; name=\"name\"\r\n\r\nDoe"));
    assertTrue(postBody.contains("content-disposition: form-data; name=\"file\"; filename=\"f1.js\"\r\ncontent-type: text/javascript\r\n\r\nvar x = 10;\r\n;console.log(x);"));
    assertTrue(postBody.contains("content-disposition: form-data; name=\"file\"; filename=\"custom_f2.txt\"\r\ncontent-type: text/plain\r\n\r\nhello"));
    assertTrue(postBody.contains("content-disposition: form-data; name=\"file\"; filename=\"blob\"\r\ncontent-type: text/plain\r\n\r\nboo"));
    assertEquals(200, response.status());
  }

  @PlaywrightTest
  @DisplayName("FE-163 should serialize data to json regardless of content-type")
  void shouldSerializeDataToJsonRegardlessOfContentType(BrowserContext context, TestServer server) throws Exception {
    Map<String, Object> data = mapOf(
      "firstName", "John",
      "lastName", "Doe");
    CompletableFuture<TestServer.Request> reqFuture = server.waitForRequest("/empty.html");
    context.request().post(server.EMPTY_PAGE, RequestOptions.create()
      .setHeader("content-type", "unknown")
      .setData(data));
    TestServer.Request req = reqFuture.get();
    assertEquals("POST", req.method);
    assertEquals("unknown", req.headers.get("content-type"));
    String body = req.postBodyText();
    // JSON.stringify(data)
    assertEquals("{\"firstName\":\"John\",\"lastName\":\"Doe\"}", body);
  }

  // FE-164 should throw nice error on unsupported data type: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("FE-165 context request should export same storage state as context")
  void contextRequestShouldExportSameStorageStateAsContext(BrowserContext context, Page page, TestServer server) {
    server.setRoute("/setcookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("a=b", "c=d"));
      res.end();
    });
    context.request().get(server.PREFIX + "/setcookie.html");
    // Java: storageState() returns the JSON text; it is parsed to compare the states.
    JsonObject contextState = JsonParser.parseString(context.storageState()).getAsJsonObject();
    assertEquals(2, contextState.getAsJsonArray("cookies").size());
    JsonObject requestState = JsonParser.parseString(context.request().storageState()).getAsJsonObject();
    assertEquals(contextState, requestState);
    JsonObject pageState = JsonParser.parseString(page.request().storageState()).getAsJsonObject();
    assertEquals(contextState, pageState);
  }

  @PlaywrightTest
  @DisplayName("FE-166 should send secure cookie over http for localhost")
  void shouldSendSecureCookieOverHttpForLocalhost(Page page, TestServer server) throws Exception {
    org.junit.jupiter.api.Assumptions.assumeFalse("webkit-wsl".equals(Env.channel()));
    server.setRoute("/setcookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("a=v; secure"));
      res.end();
    });
    page.request().get(server.PREFIX + "/setcookie.html");
    CompletableFuture<TestServer.Request> serverRequestFuture = server.waitForRequest("/empty.html");
    page.request().get(server.EMPTY_PAGE);
    TestServer.Request serverRequest = serverRequestFuture.get();
    assertEquals("a=v", serverRequest.headers.get("cookie"));
  }

  @PlaywrightTest
  @DisplayName("FE-167 should accept bool and numeric params and filter out undefined")
  void shouldAcceptBoolAndNumericParamsAndFilterOutUndefined(Page page, TestServer server) throws Exception {
    AtomicReference<TestServer.Request> request = new AtomicReference<>();
    URL url = new URL(server.EMPTY_PAGE);
    SearchParams searchParams = new SearchParams("");
    searchParams.set("str", "s");
    searchParams.set("num", "10");
    searchParams.set("bool", "true");
    searchParams.set("bool2", "false");
    server.setRoute(url.getPath() + "?" + searchParams, (req, res) -> {
      request.set(req);
      server.serveFile(req, res);
    });
    // Java: there is no undefined value for the 'none' param; it is left out.
    page.request().get(server.EMPTY_PAGE, RequestOptions.create()
      .setQueryParam("str", "s")
      .setQueryParam("num", 10)
      .setQueryParam("bool", true)
      .setQueryParam("bool2", false));
    SearchParams params = new SearchParams(request.get().url.substring(request.get().url.indexOf('?')));
    assertEquals("s", params.get("str"));
    assertEquals("10", params.get("num"));
    assertEquals("true", params.get("bool"));
    assertEquals("false", params.get("bool2"));
    assertFalse(params.has("none"));
  }

  // FE-168 should abort requests when browser context closes: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("FE-169 should work with connectOverCDP")
  @EnabledOnBrowser(BrowserName.CHROMIUM)
  void shouldWorkWithConnectOverCDP(BrowserName browserName, com.microsoft.playwright.BrowserType browserType, TestServer server) {
    // Java: a free port instead of 9339 + testInfo.workerIndex, the test classes run in parallel in one JVM.
    int port = findFreePort();
    Browser browserServer = browserType.launch(Env.launchOptions().setArgs(Arrays.asList("--remote-debugging-port=" + port)));
    try {
      Browser cdpBrowser = browserType.connectOverCDP("http://127.0.0.1:" + port + "/");
      BrowserContext context = cdpBrowser.contexts().get(0);
      APIResponse response = context.request().get(server.PREFIX + "/simple.json");
      assertEquals(server.PREFIX + "/simple.json", response.url());
      assertEquals(200, response.status());
      assertEquals("{\"foo\": \"bar\"}\n", response.text());
    } finally {
      browserServer.close();
    }
  }

  @PlaywrightTest
  @DisplayName("FE-170 should support SameSite cookie attribute over https")
  void shouldSupportSameSiteCookieAttributeOverHttps(ContextFactory contextFactory, HttpsTestServer httpsServer, BrowserName browserName) {
    // Cookies with SameSite=None must also specify the Secure attribute. WebKit navigation
    // to HTTP url will fail if the response contains a cookie with Secure attribute, so
    // we do HTTPS navigation.
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions().setIgnoreHTTPSErrors(true));
    Page page = context.newPage();
    for (String value : new String[]{"None", "Lax", "Strict"}) {
      // it.step(`SameSite=${value}`)
      httpsServer.setRoute("/empty.html", (req, res) -> {
        res.setHeader("Set-Cookie", "SID=2022; Path=/; Secure; SameSite=" + value);
        res.end();
      });
      page.request().get(httpsServer.EMPTY_PAGE);
      Cookie cookie = first(page.context().cookies());
      if (browserName == BrowserName.WEBKIT && Env.isWindows && !"webkit-wsl".equals(Env.channel())) {
        assertEquals(SameSiteAttribute.NONE, cookie.sameSite);
      } else {
        assertEquals(SameSiteAttribute.valueOf(value.toUpperCase()), cookie.sameSite);
      }
    }
  }

  @PlaywrightTest
  @DisplayName("FE-171 should set domain=localhost cookie")
  void shouldSetDomainLocalhostCookie(BrowserContext context, TestServer server, BrowserName browserName) {
    server.setRoute("/empty.html", (req, res) -> {
      res.setHeader("Set-Cookie", "name=val; Domain=" + server.HOSTNAME + "; Path=/;");
      res.end();
    });
    context.request().get(server.EMPTY_PAGE);
    Cookie cookie = first(context.cookies());
    assertNotNull(cookie);
    assertEquals("name", cookie.name);
    assertEquals("val", cookie.value);
  }

  @PlaywrightTest
  @DisplayName("FE-172 fetch should not throw on long set-cookie value")
  void fetchShouldNotThrowOnLongSetCookieValue(BrowserContext context, TestServer server) {
    server.setRoute("/empty.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("foo=" + String.join("", Collections.nCopies(4100, "a")) + "; path=/;", "bar=val"));
      res.end();
    });
    context.request().get(server.EMPTY_PAGE, RequestOptions.create().setTimeout(5000));
    List<Cookie> cookies = context.cookies();
    assertTrue(cookies.stream().map(c -> c.name).collect(Collectors.toList()).contains("bar"));
  }

  @PlaywrightTest
  @DisplayName("FE-173 should support set-cookie with SameSite and without Secure attribute over HTTP")
  void shouldSupportSetCookieWithSameSiteAndWithoutSecureAttributeOverHTTP(Page page, TestServer server, BrowserName browserName) {
    for (String value : new String[]{"None", "Lax", "Strict"}) {
      // it.step(`SameSite=${value}`)
      server.setRoute("/empty.html", (req, res) -> {
        res.setHeader("Set-Cookie", "SID=2022; Path=/; SameSite=" + value);
        res.end();
      });
      page.request().get(server.EMPTY_PAGE);
      Cookie cookie = first(page.context().cookies());
      if ((browserName == BrowserName.CHROMIUM || Env.isBidi()) && value.equals("None")) {
        assertNull(cookie);
      } else if (browserName == BrowserName.WEBKIT && (Env.isLinux || "webkit-wsl".equals(Env.channel())) && value.equals("None")) {
        assertNull(cookie);
      } else if (browserName == BrowserName.WEBKIT && Env.isWindows && !"webkit-wsl".equals(Env.channel())) {
        assertEquals(SameSiteAttribute.NONE, cookie.sameSite);
      } else {
        assertEquals(SameSiteAttribute.valueOf(value.toUpperCase()), cookie.sameSite);
      }
    }
  }

  @PlaywrightTest
  @DisplayName("FE-174 should update host header on redirect")
  void shouldUpdateHostHeaderOnRedirect(BrowserContext context, TestServer server) throws Exception {
    AtomicInteger redirectCount = new AtomicInteger();
    server.setRoute("/redirect", (req, res) -> {
      redirectCount.incrementAndGet();
      String path = req.headers.get("host").equals(new URL(server.PREFIX).getAuthority()) ? "/redirect" : "/test";
      res.writeHead(302, "host", new URL(server.CROSS_PROCESS_PREFIX).getAuthority(), "location", server.CROSS_PROCESS_PREFIX + path);
      res.end();
    });
    server.setRoute("/test", (req, res) -> {
      res.writeHead(200, "content-type", "text/plain");
      res.end("Hello!");
    });
    CompletableFuture<TestServer.Request> reqPromise = server.waitForRequest("/test");
    APIResponse response = context.request().get(server.PREFIX + "/redirect", RequestOptions.create()
      .setHeader("HosT", new URL(server.PREFIX).getAuthority()));
    assertEquals(2, redirectCount.get());
    assertThat(response).isOK();
    assertEquals("Hello!", response.text());

    assertEquals(new URL(server.CROSS_PROCESS_PREFIX).getAuthority(), reqPromise.get().headers.get("host"));
  }

  @PlaywrightTest
  @DisplayName("FE-175 should not work after dispose")
  void shouldNotWorkAfterDispose(BrowserContext context, TestServer server) {
    context.request().dispose();
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> context.request().get(server.EMPTY_PAGE));
    assertTrue(error.getMessage().contains(kTargetClosedErrorMessage));
  }

  @PlaywrightTest
  @DisplayName("FE-176 should not work after context dispose")
  void shouldNotWorkAfterContextDispose(BrowserContext context, TestServer server) {
    context.close(new BrowserContext.CloseOptions().setReason("Test ended."));
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> context.request().get(server.EMPTY_PAGE));
    assertTrue(error.getMessage().contains("Test ended."));
  }

  @PlaywrightTest
  @DisplayName("FE-177 should retry on ECONNRESET")
  void shouldRetryOnECONNRESET(BrowserContext context, TestServer server) {
    AtomicInteger requestCount = new AtomicInteger();
    server.setRoute("/test", (req, res) -> {
      if (requestCount.getAndIncrement() < 3) {
        req.socket().destroy();
        return;
      }
      res.writeHead(200, "content-type", "text/plain");
      res.end("Hello!");
    });
    APIResponse response = context.request().get(server.PREFIX + "/test", RequestOptions.create().setMaxRetries(3));
    assertEquals(200, response.status());
    assertEquals("Hello!", response.text());
    assertEquals(4, requestCount.get());
  }

  @PlaywrightTest
  @DisplayName("FE-178 should retry ECONNRESET on compressed response")
  void shouldRetryECONNRESETOnCompressedResponse(BrowserContext context, TestServer server) {
    AtomicInteger requestCount = new AtomicInteger();
    server.setRoute("/test-gzip", (req, res) -> {
      if (requestCount.getAndIncrement() < 2) {
        req.socket().destroy();
        return;
      }
      res.writeHead(200, "Content-Encoding", "gzip", "Content-Type", "text/plain");
      res.write(gzip("compressed-retry-ok"));
      res.end();
    });
    APIResponse response = context.request().get(server.PREFIX + "/test-gzip", RequestOptions.create().setMaxRetries(3));
    assertEquals(200, response.status());
    assertEquals("compressed-retry-ok", response.text());
    assertEquals(3, requestCount.get());
  }

  @PlaywrightTest
  @DisplayName("FE-179 should retry ECONNRESET mid-stream during gzip decompression")
  void shouldRetryECONNRESETMidStreamDuringGzipDecompression(BrowserContext context, TestServer server) {
    AtomicInteger requestCount = new AtomicInteger();
    server.setRoute("/test-gzip-midstream", (req, res) -> {
      int count = requestCount.incrementAndGet();
      if (count <= 2) {
        // Send response headers to make client enter the decompression pipeline,
        // then destroy the socket. This exercises the fix: without it, the
        // pipeline error callback wraps the error, stripping .code for retry.
        res.writeHead(200, "Content-Encoding", "gzip", "Content-Type", "text/plain");
        res.flushHeaders();
        req.socket().destroy();
        return;
      }
      res.writeHead(200, "Content-Encoding", "gzip", "Content-Type", "text/plain");
      res.write(gzip("midstream-retry-ok"));
      res.end();
    });
    APIResponse response = context.request().get(server.PREFIX + "/test-gzip-midstream", RequestOptions.create().setMaxRetries(3));
    assertEquals(200, response.status());
    assertEquals("midstream-retry-ok", response.text());
    assertEquals(3, requestCount.get());
  }

  // Java: context.request[method](url, options).
  private static APIResponse request(APIRequestContext request, String method, String url, RequestOptions options) {
    switch (method) {
      case "fetch":
        return request.fetch(url, options);
      case "delete":
        return request.delete(url, options);
      case "get":
        return request.get(url, options);
      case "head":
        return request.head(url, options);
      case "patch":
        return request.patch(url, options);
      case "post":
        return request.post(url, options);
      case "put":
        return request.put(url, options);
      default:
        throw new IllegalArgumentException(method);
    }
  }

  private static Map<String, String> mapOfStrings(String name, String value) {
    Map<String, String> map = new java.util.LinkedHashMap<>();
    map.put(name, value);
    return map;
  }

  private static Cookie first(List<Cookie> cookies) {
    return cookies.isEmpty() ? null : cookies.get(0);
  }

  private static Map<String, Object> cookieFields(Cookie c) {
    return mapOf("sameSite", c.sameSite, "name", c.name, "value", c.value, "domain", c.domain, "path", c.path,
      "expires", c.expires, "httpOnly", c.httpOnly, "secure", c.secure);
  }

  private static byte[] gzip(String text) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
        gzip.write(text.getBytes(StandardCharsets.UTF_8));
      }
      return out.toByteArray();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static byte[] deflate(String text) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      try (DeflaterOutputStream deflate = new DeflaterOutputStream(out)) {
        deflate.write(text.getBytes(StandardCharsets.UTF_8));
      }
      return out.toByteArray();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  /** The URLSearchParams class of JavaScript. */
  private static final class SearchParams {
    private final List<String[]> entries = new ArrayList<>();

    SearchParams(String query) {
      String text = query == null ? "" : query;
      if (text.startsWith("?")) {
        text = text.substring(1);
      }
      if (text.isEmpty()) {
        return;
      }
      for (String pair : text.split("&")) {
        int eq = pair.indexOf('=');
        String name = eq == -1 ? pair : pair.substring(0, eq);
        String value = eq == -1 ? "" : pair.substring(eq + 1);
        entries.add(new String[]{decode(name), decode(value)});
      }
    }

    void set(String name, String value) {
      for (int i = 0; i < entries.size(); i++) {
        if (entries.get(i)[0].equals(name)) {
          entries.set(i, new String[]{name, value});
          return;
        }
      }
      entries.add(new String[]{name, value});
    }

    String get(String name) {
      for (String[] entry : entries) {
        if (entry[0].equals(name)) {
          return entry[1];
        }
      }
      return null;
    }

    List<String> getAll(String name) {
      List<String> result = new ArrayList<>();
      for (String[] entry : entries) {
        if (entry[0].equals(name)) {
          result.add(entry[1]);
        }
      }
      return result;
    }

    boolean has(String name) {
      return get(name) != null;
    }

    @Override
    public String toString() {
      return entries.stream().map(e -> encode(e[0]) + "=" + encode(e[1])).collect(Collectors.joining("&"));
    }

    private static String decode(String text) {
      try {
        return URLDecoder.decode(text, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        throw new IllegalStateException(e);
      }
    }

    private static String encode(String text) {
      try {
        return URLEncoder.encode(text, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        throw new IllegalStateException(e);
      }
    }
  }
}
