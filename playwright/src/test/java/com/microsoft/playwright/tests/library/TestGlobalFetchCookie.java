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
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.HttpsTestServer;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.ProxyServer;
import com.microsoft.playwright.fixtures.TestInfo;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.SameSiteAttribute;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/library/global-fetch-cookie.spec.ts. */
public class TestGlobalFetchCookie {
  // The request fixture.
  APIRequestContext request;
  // Java: the request fixture for the tests that upstream passes { __testHookLookup } to. The hook resolves
  // the host names of the tests to 127.0.0.1; a function can not be passed to the driver, so the requests of
  // this context go through a proxy that forwards everything to the test server.
  APIRequestContext lookupRequest;

  @BeforeEach
  void setUpRequest(Playwright playwright, TestServer server, ProxyServer proxyServer) {
    request = playwright.request().newContext(new APIRequest.NewContextOptions().setIgnoreHTTPSErrors(true));
    proxyServer.forwardTo(server.PORT, new ProxyServer.ForwardOptions().setAllowConnectRequests(true));
    lookupRequest = playwright.request().newContext(new APIRequest.NewContextOptions().setIgnoreHTTPSErrors(true).setProxy(proxyServer.URL));
  }

  @AfterEach
  void tearDownRequest() {
    request.dispose();
    lookupRequest.dispose();
  }

  // Java: StorageStateType is the JSON string of the storage state; this is the parsed value, for toEqual.
  private static Object parseJson(String json) {
    return new Gson().fromJson(json, Object.class);
  }

  // Java: the JSON of an object literal of the test, parsed the same way as parseJson().
  private static Object json(Object value) {
    return parseJson(new Gson().toJson(value));
  }

  // Java: Date.prototype.toUTCString().
  private static String toUTCString(Instant date) {
    return DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US).withZone(ZoneOffset.UTC).format(date);
  }

  private static List<String> names(List<Cookie> cookies) {
    List<String> result = new ArrayList<>();
    for (Cookie cookie : cookies) {
      result.add(cookie.name);
    }
    return result;
  }

  private static List<Object> cookiesOf(String storageState) {
    @SuppressWarnings("unchecked")
    Map<String, Object> state = (Map<String, Object>) parseJson(storageState);
    @SuppressWarnings("unchecked")
    List<Object> cookies = (List<Object>) state.get("cookies");
    return cookies;
  }

  private static void assertFalsy(String value) {
    assertTrue(value == null || value.isEmpty());
  }

  @PlaywrightTest
  @DisplayName("FE-054 should store cookie from Set-Cookie header")
  void shouldStoreCookieFromSetCookieHeader(TestServer server) throws Exception {
    server.setRoute("/setcookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("a=b", "c=d; max-age=3600; domain=b.one.com; path=/input", "e=f; domain=b.one.com; path=/input/subfolder"));
      res.end();
    });
    lookupRequest.get("http://a.b.one.com:" + server.PORT + "/setcookie.html");
    CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/input/button.html");
    lookupRequest.get("http://b.one.com:" + server.PORT + "/input/button.html");
    TestServer.Request serverRequest = serverRequestPromise.get();
    assertEquals("c=d", serverRequest.headers.get("cookie"));
  }

  @PlaywrightTest
  @DisplayName("FE-055 addCookies should add cookies to the cookie jar")
  void addCookiesShouldAddCookiesToTheCookieJar(TestServer server) throws Exception {
    request.addCookies(Arrays.asList(
      new Cookie("a", "b").setUrl(server.EMPTY_PAGE),
      new Cookie("c", "d").setDomain("localhost").setPath("/input").setHttpOnly(true).setSameSite(SameSiteAttribute.STRICT),
      new Cookie("e", "f").setDomain("other.com").setPath("/")));
    CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/input/button.html");
    request.get(server.PREFIX + "/input/button.html");
    TestServer.Request serverRequest = serverRequestPromise.get();
    assertEquals("a=b; c=d", serverRequest.headers.get("cookie"));
    String state = request.storageState();
    assertEquals(json(Arrays.asList(
      mapOf("name", "a", "value", "b", "domain", "localhost", "path", "/", "expires", -1, "httpOnly", false, "secure", false, "sameSite", "Lax"),
      mapOf("name", "c", "value", "d", "domain", "localhost", "path", "/input", "expires", -1, "httpOnly", true, "secure", false, "sameSite", "Strict"),
      mapOf("name", "e", "value", "f", "domain", "other.com", "path", "/", "expires", -1, "httpOnly", false, "secure", false, "sameSite", "Lax"))),
      cookiesOf(state));
  }

  @PlaywrightTest
  @DisplayName("FE-056 addCookies should validate cookies")
  void addCookiesShouldValidateCookies() {
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> request.addCookies(Arrays.asList(new Cookie("a", "b"))));
    assertTrue(error.getMessage().contains("Cookie should have a url or a domain/path pair"));
  }

  @PlaywrightTest
  @DisplayName("FE-057 cookies should return cookies filtered by urls")
  void cookiesShouldReturnCookiesFilteredByUrls(TestServer server) {
    request.addCookies(Arrays.asList(
      new Cookie("a", "b").setDomain("localhost").setPath("/"),
      new Cookie("c", "d").setDomain("localhost").setPath("/input"),
      new Cookie("e", "f").setDomain(".one.com").setPath("/"),
      new Cookie("g", "h").setDomain("two.com").setPath("/").setSecure(true)));
    assertEquals(Arrays.asList("a", "c", "e", "g"), names(request.cookies()));
    assertEquals(Arrays.asList("a"), names(request.cookies(server.EMPTY_PAGE)));
    assertEquals(Arrays.asList("a", "c"), names(request.cookies(server.PREFIX + "/input/button.html")));
    assertEquals(Arrays.asList("e"), names(request.cookies(Arrays.asList("http://sub.one.com/", "http://two.com/"))));
    assertEquals(Arrays.asList("e", "g"), names(request.cookies(Arrays.asList("http://sub.one.com/", "https://two.com/"))));
    assertEquals(Collections.emptyList(), request.cookies("http://other.com/"));
  }

  @PlaywrightTest
  @DisplayName("FE-058 cookies should only return cookies that apply to the url")
  @Fixme("driver: 1.64.1 request.cookies(url) returns hostonly and subdomains for http://one.com/apiv2 (no RFC 6265 path matching); fixed by microsoft/playwright#43148 (fix(cookies): follow RFC 6265 domain and path matching in cookies(urls))")
  void cookiesShouldOnlyReturnCookiesThatApplyToTheUrl() {
    request.addCookies(Arrays.asList(
      new Cookie("hostonly", "v").setDomain("one.com").setPath("/api"),
      new Cookie("subdomains", "v").setDomain(".one.com").setPath("/api"),
      new Cookie("slash", "v").setDomain("one.com").setPath("/foo/")));
    assertEquals(Arrays.asList("hostonly", "subdomains"), names(request.cookies("http://one.com/api")));
    assertEquals(Arrays.asList("hostonly", "subdomains"), names(request.cookies("http://one.com/api/x")));
    assertEquals(Collections.emptyList(), names(request.cookies("http://one.com/apiv2")));
    assertEquals(Arrays.asList("subdomains"), names(request.cookies("http://sub.one.com/api")));
    assertEquals(Collections.emptyList(), names(request.cookies("http://notone.com/api")));
    assertEquals(Collections.emptyList(), names(request.cookies("http://one.com/foo")));
    assertEquals(Arrays.asList("slash"), names(request.cookies("http://one.com/foo/")));
    assertEquals(Arrays.asList("slash"), names(request.cookies("http://one.com/foo/bar")));
  }

  @PlaywrightTest
  @DisplayName("FE-059 should only send cookies that apply to the url")
  void shouldOnlySendCookiesThatApplyToTheUrl(TestServer server) throws Exception {
    server.setRoute("/setcookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("hostonly=v; path=/api", "subdomains=v; domain=one.com; path=/api", "slash=v; path=/foo/"));
      res.end();
    });
    lookupRequest.get("http://one.com:" + server.PORT + "/setcookie.html");
    // Java: the sent() function of the test.
    java.util.function.BiFunction<String, String, String> sent = (host, path) -> {
      try {
        CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest(path);
        lookupRequest.get("http://" + host + ":" + server.PORT + path);
        TestServer.Request serverRequest = serverRequestPromise.get();
        return serverRequest.headers.get("cookie");
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    };
    assertEquals("hostonly=v; subdomains=v", sent.apply("one.com", "/api"));
    assertEquals("hostonly=v; subdomains=v", sent.apply("one.com", "/api/x"));
    assertNull(sent.apply("one.com", "/apiv2"));
    assertEquals("subdomains=v", sent.apply("sub.one.com", "/api"));
    assertNull(sent.apply("one.com", "/foo"));
    assertEquals("slash=v", sent.apply("one.com", "/foo/"));
    assertEquals("slash=v", sent.apply("one.com", "/foo/bar"));
  }

  @PlaywrightTest
  @DisplayName("FE-060 cookies should include cookies from Set-Cookie header")
  void cookiesShouldIncludeCookiesFromSetCookieHeader(TestServer server) {
    server.setRoute("/setcookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("a=b; HttpOnly; SameSite=Strict", "c=d; path=/input"));
      res.end();
    });
    request.get(server.PREFIX + "/setcookie.html");
    // Java: Cookie has no equals(), the cookies of the storage state are compared.
    assertEquals(json(Arrays.asList(
      mapOf("name", "a", "value", "b", "domain", "localhost", "path", "/", "expires", -1, "httpOnly", true, "secure", false, "sameSite", "Strict"),
      mapOf("name", "c", "value", "d", "domain", "localhost", "path", "/input", "expires", -1, "httpOnly", false, "secure", false, "sameSite", "Lax"))),
      cookiesOf(request.storageState()));
  }

  @PlaywrightTest
  @DisplayName("FE-061 clearCookies should remove all cookies")
  void clearCookiesShouldRemoveAllCookies(TestServer server) throws Exception {
    request.addCookies(Arrays.asList(
      new Cookie("a", "b").setUrl(server.EMPTY_PAGE),
      new Cookie("c", "d").setDomain("one.com").setPath("/")));
    request.clearCookies();
    assertEquals(Collections.emptyList(), request.cookies());
    CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/empty.html");
    request.get(server.EMPTY_PAGE);
    TestServer.Request serverRequest = serverRequestPromise.get();
    assertNull(serverRequest.headers.get("cookie"));
  }

  @PlaywrightTest
  @DisplayName("FE-062 clearCookies should filter by name, domain and path")
  void clearCookiesShouldFilterByNameDomainAndPath() {
    request.addCookies(Arrays.asList(
      new Cookie("session", "1").setDomain("one.com").setPath("/"),
      new Cookie("session", "2").setDomain("two.com").setPath("/"),
      new Cookie("session", "3").setDomain("two.com").setPath("/api"),
      new Cookie("other", "4").setDomain("one.com").setPath("/")));
    // Java: the values() function of the test.
    java.util.function.Supplier<List<String>> values = () -> {
      List<String> result = new ArrayList<>();
      for (Cookie cookie : request.cookies()) {
        result.add(cookie.value);
      }
      Collections.sort(result);
      return result;
    };

    request.clearCookies(new APIRequestContext.ClearCookiesOptions().setName("session").setDomain("two.com").setPath("/api"));
    assertEquals(Arrays.asList("1", "2", "4"), values.get());

    request.clearCookies(new APIRequestContext.ClearCookiesOptions().setDomain(Pattern.compile("one\\.com$")));
    assertEquals(Arrays.asList("2"), values.get());

    request.clearCookies(new APIRequestContext.ClearCookiesOptions().setName(Pattern.compile("^sess")));
    assertEquals(Collections.emptyList(), values.get());
  }

  @PlaywrightTest
  @DisplayName("FE-063 clearCookies should remove nameless cookies by empty name")
  void clearCookiesShouldRemoveNamelessCookiesByEmptyName() {
    request.addCookies(Arrays.asList(
      new Cookie("", "nameless").setDomain("one.com").setPath("/"),
      new Cookie("session", "secret").setDomain("one.com").setPath("/")));
    request.clearCookies(new APIRequestContext.ClearCookiesOptions().setName(""));
    assertEquals(Arrays.asList("session"), names(request.cookies()));
  }

  @PlaywrightTest
  @DisplayName("FE-064 should send nameless cookies as value only")
  @Fixme("driver: 1.64.1 sends a nameless cookie as '=nameless' instead of 'nameless'; fixed by microsoft/playwright#43168 (fix(fetch): serialize and parse nameless cookies per RFC 6265bis)")
  void shouldSendNamelessCookiesAsValueOnly(TestServer server) throws Exception {
    request.addCookies(Arrays.asList(
      new Cookie("", "nameless").setUrl(server.EMPTY_PAGE),
      new Cookie("a", "b").setUrl(server.EMPTY_PAGE)));
    CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/empty.html");
    request.get(server.EMPTY_PAGE);
    TestServer.Request serverRequest = serverRequestPromise.get();
    assertEquals("nameless; a=b", serverRequest.headers.get("cookie"));
  }

  @PlaywrightTest
  @DisplayName("FE-065 should store Set-Cookie without equals sign as nameless cookie")
  @Fixme("driver: 1.64.1 stores a Set-Cookie without equals sign as name=token, value= instead of a nameless cookie; fixed by microsoft/playwright#43168 (fix(fetch): serialize and parse nameless cookies per RFC 6265bis)")
  void shouldStoreSetCookieWithoutEqualsSignAsNamelessCookie(TestServer server) throws Exception {
    server.setRoute("/setcookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("token; path=/", "a=; path=/"));
      res.end();
    });
    request.get(server.PREFIX + "/setcookie.html");
    List<Map<String, Object>> nameValues = new ArrayList<>();
    for (Cookie c : request.cookies()) {
      nameValues.add(mapOf("name", c.name, "value", c.value));
    }
    assertEquals(Arrays.asList(
      mapOf("name", "", "value", "token"),
      mapOf("name", "a", "value", "")), nameValues);
    CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/empty.html");
    request.get(server.EMPTY_PAGE);
    TestServer.Request serverRequest = serverRequestPromise.get();
    assertEquals("token; a=", serverRequest.headers.get("cookie"));
  }

  @PlaywrightTest
  @DisplayName("FE-066 should filter outgoing cookies by path")
  void shouldFilterOutgoingCookiesByPath(TestServer server) throws Exception {
    server.setRoute("/setcookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("a=v; path=/input/subfolder", "b=v; path=/input", "c=v;"));
      res.end();
    });
    request.get(server.PREFIX + "/setcookie.html");
    CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/input/button.html");
    request.get(server.PREFIX + "/input/button.html");
    TestServer.Request serverRequest = serverRequestPromise.get();
    assertEquals("b=v; c=v", serverRequest.headers.get("cookie"));
  }

  @PlaywrightTest
  @DisplayName("FE-067 should filter outgoing cookies by domain")
  void shouldFilterOutgoingCookiesByDomain(TestServer server) throws Exception {
    server.setRoute("/setcookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("a=v; domain=one.com", "b=v; domain=.b.one.com", "c=v; domain=other.com"));
      res.end();
    });
    lookupRequest.get("http://a.b.one.com:" + server.PORT + "/setcookie.html");
    CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/empty.html");
    lookupRequest.get("http://www.b.one.com:" + server.PORT + "/empty.html");
    TestServer.Request serverRequest = serverRequestPromise.get();
    assertEquals("a=v; b=v", serverRequest.headers.get("cookie"));

    CompletableFuture<TestServer.Request> serverRequest2Promise = server.waitForRequest("/empty.html");
    lookupRequest.get("http://two.com:" + server.PORT + "/empty.html");
    TestServer.Request serverRequest2 = serverRequest2Promise.get();
    assertFalsy(serverRequest2.headers.get("cookie"));
  }

  @PlaywrightTest
  @DisplayName("FE-068 should do case-insensitive match of cookie domain")
  void shouldDoCaseInsensitiveMatchOfCookieDomain(TestServer server) throws Exception {
    server.setRoute("/setcookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("a=v; domain=One.com", "b=v; domain=.B.oNe.com"));
      res.end();
    });
    lookupRequest.get("http://a.b.one.com:" + server.PORT + "/setcookie.html");
    CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/empty.html");
    lookupRequest.get("http://www.b.one.com:" + server.PORT + "/empty.html");
    TestServer.Request serverRequest = serverRequestPromise.get();
    assertEquals("a=v; b=v", serverRequest.headers.get("cookie"));
  }

  @PlaywrightTest
  @DisplayName("FE-069 should do case-insensitive match of request domain")
  void shouldDoCaseInsensitiveMatchOfRequestDomain(TestServer server) throws Exception {
    server.setRoute("/setcookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("a=v; domain=one.com", "b=v; domain=.b.one.com"));
      res.end();
    });
    lookupRequest.get("http://a.b.one.com:" + server.PORT + "/setcookie.html");
    CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/empty.html");
    lookupRequest.get("http://WWW.B.ONE.COM:" + server.PORT + "/empty.html");
    TestServer.Request serverRequest = serverRequestPromise.get();
    assertEquals("a=v; b=v", serverRequest.headers.get("cookie"));
  }

  @PlaywrightTest
  @DisplayName("FE-070 should send secure cookie over https")
  void shouldSendSecureCookieOverHttps(TestServer server, HttpsTestServer httpsServer) throws Exception {
    server.setRoute("/setcookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("a=v; secure", "b=v"));
      res.end();
    });
    request.get(server.PREFIX + "/setcookie.html");
    CompletableFuture<TestServer.Request> serverRequestPromise = httpsServer.waitForRequest("/empty.html");
    request.get(httpsServer.EMPTY_PAGE);
    TestServer.Request serverRequest = serverRequestPromise.get();
    assertEquals("a=v; b=v", serverRequest.headers.get("cookie"));
  }

  @PlaywrightTest
  @DisplayName("FE-071 should send secure cookie over http for localhost")
  void shouldSendSecureCookieOverHttpForLocalhost(TestServer server) throws Exception {
    server.setRoute("/setcookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("a=v; secure", "b=v"));
      res.end();
    });
    request.get(server.PREFIX + "/setcookie.html");
    CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/empty.html");
    request.get(server.EMPTY_PAGE);
    TestServer.Request serverRequest = serverRequestPromise.get();
    assertEquals("a=v; b=v", serverRequest.headers.get("cookie"));
  }

  @PlaywrightTest
  @DisplayName("FE-072 should send secure cookie over http for subdomains of localhost")
  void shouldSendSecureCookieOverHttpForSubdomainsOfLocalhost(TestServer server) throws Exception {
    server.setRoute("/setcookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("a=v; secure", "b=v"));
      res.end();
    });
    String prefix = "http://a.b.localhost:" + server.PORT;
    lookupRequest.get(prefix + "/setcookie.html");
    CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/empty.html");
    lookupRequest.get(prefix + "/empty.html");
    TestServer.Request serverRequest = serverRequestPromise.get();
    assertEquals("a=v; b=v", serverRequest.headers.get("cookie"));
  }

  @Nested
  @ParameterizedClass
  @ValueSource(strings = {"127.0.0.1", "[::1]"})
  class SendSecureCookieOverHttpForHost {
    @Parameter
    String host;

    @PlaywrightTest
    @DisplayName("FE-073 should send secure cookie over http for ${host}")
    void shouldSendSecureCookieOverHttpForHost(TestServer server) throws Exception {
      server.setRoute("/setcookie.html", (req, res) -> {
        res.setHeader("Set-Cookie", Arrays.asList("a=v; secure", "b=v"));
        res.end();
      });
      String prefix = "http://" + host + ":" + server.PORT;
      request.get(prefix + "/setcookie.html");
      CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/empty.html");
      request.get(prefix + "/empty.html");
      TestServer.Request serverRequest = serverRequestPromise.get();
      assertEquals("a=v; b=v", serverRequest.headers.get("cookie"));
      assertEquals(Arrays.asList("a", "b"), names(request.cookies(prefix + "/")));
    }
  }

  @PlaywrightTest
  @DisplayName("FE-074 should send not expired cookies")
  void shouldSendNotExpiredCookies(TestServer server) throws Exception {
    server.setRoute("/setcookie.html", (req, res) -> {
      Instant tomorrow = ZonedDateTime.now().plusDays(1).toInstant();
      res.setHeader("Set-Cookie", Arrays.asList("a=v", "b=v; expires=" + toUTCString(tomorrow)));
      res.end();
    });
    request.get(server.PREFIX + "/setcookie.html");
    CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/empty.html");
    request.get(server.EMPTY_PAGE);
    TestServer.Request serverRequest = serverRequestPromise.get();
    assertEquals("a=v; b=v", serverRequest.headers.get("cookie"));
  }

  @PlaywrightTest
  @DisplayName("FE-075 should remove expired cookies")
  void shouldRemoveExpiredCookies(TestServer server) throws Exception {
    server.setRoute("/setcookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("a=v", "b=v; expires=" + toUTCString(Instant.now())));
      res.end();
    });
    request.get(server.PREFIX + "/setcookie.html");
    CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/empty.html");
    request.get(server.EMPTY_PAGE);
    TestServer.Request serverRequest = serverRequestPromise.get();
    assertEquals("a=v", serverRequest.headers.get("cookie"));
  }

  @PlaywrightTest
  @DisplayName("FE-076 should remove cookie with negative max-age")
  void shouldRemoveCookieWithNegativeMaxAge(TestServer server) throws Exception {
    server.setRoute("/setcookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("a=v; max-age=100000", "b=v; max-age=100000", "c=v"));
      res.end();
    });
    server.setRoute("/removecookie.html", (req, res) -> {
      long maxAge = -2 * System.currentTimeMillis();
      res.setHeader("Set-Cookie", Arrays.asList("a=v; max-age=" + maxAge, "b=v; max-age=-1"));
      res.end();
    });
    request.get(server.PREFIX + "/setcookie.html");
    request.get(server.PREFIX + "/removecookie.html");
    CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/empty.html");
    request.get(server.EMPTY_PAGE);
    TestServer.Request serverRequest = serverRequestPromise.get();
    assertEquals("c=v", serverRequest.headers.get("cookie"));
  }

  @PlaywrightTest
  @DisplayName("FE-077 should prefer max-age over expires regardless of the order")
  void shouldPreferMaxAgeOverExpiresRegardlessOfTheOrder(TestServer server) throws Exception {
    String past = "Thu, 01 Jan 1970 00:00:00 GMT";
    String future = toUTCString(Instant.now().plusMillis(3600_000));
    server.setRoute("/setcookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList(
        "a=v; max-age=3600; expires=" + past,
        "b=v; expires=" + past + "; max-age=3600",
        "c=v; max-age=-1; expires=" + future,
        "d=v; expires=" + future + "; max-age=-1"));
      res.end();
    });
    request.get(server.PREFIX + "/setcookie.html");
    CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/empty.html");
    request.get(server.EMPTY_PAGE);
    TestServer.Request serverRequest = serverRequestPromise.get();
    assertEquals("a=v; b=v", serverRequest.headers.get("cookie"));
  }

  @PlaywrightTest
  @DisplayName("FE-078 should ignore max-age that is not a number")
  void shouldIgnoreMaxAgeThatIsNotANumber(TestServer server) throws Exception {
    server.setRoute("/setcookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("a=v; max-age=-1abc", "b=v; max-age=3600abc", "c=v; max-age="));
      res.end();
    });
    request.get(server.PREFIX + "/setcookie.html");
    CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/empty.html");
    request.get(server.EMPTY_PAGE);
    TestServer.Request serverRequest = serverRequestPromise.get();
    assertEquals("a=v; b=v; c=v", serverRequest.headers.get("cookie"));
    List<Object> cookies = cookiesOf(request.storageState());
    List<Object> expires = new ArrayList<>();
    for (Object cookie : cookies) {
      expires.add(((Map<?, ?>) cookie).get("expires"));
    }
    assertEquals(json(Arrays.asList(-1, -1, -1)), expires);
  }

  @PlaywrightTest
  @DisplayName("FE-079 should remove cookie with expires far in the past")
  void shouldRemoveCookieWithExpiresFarInThePast(TestServer server) throws Exception {
    server.setRoute("/setcookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("a=v; max-age=1000000"));
      res.end();
    });
    server.setRoute("/removecookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("a=v; expires=1 Jan 1000 00:00:00 +0000 (UTC)"));
      res.end();
    });
    request.get(server.PREFIX + "/setcookie.html");
    request.get(server.PREFIX + "/removecookie.html");
    CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/empty.html");
    request.get(server.EMPTY_PAGE);
    TestServer.Request serverRequest = serverRequestPromise.get();
    assertFalsy(serverRequest.headers.get("cookie"));
  }

  @PlaywrightTest
  @DisplayName("FE-080 should store cookie from Set-Cookie header even if it contains equal signs")
  void shouldStoreCookieFromSetCookieHeaderEvenIfItContainsEqualSigns(TestServer server) {
    server.setRoute("/setcookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("f=value == value=; secure; httpOnly; path=/some=value"));
      res.end();
    });

    lookupRequest.get("http://a.b.one.com:" + server.PORT + "/setcookie.html");
    String state = lookupRequest.storageState();
    assertEquals(json(mapOf(
      "cookies", Arrays.asList(
        mapOf(
          "domain", "a.b.one.com",
          "expires", -1,
          "name", "f",
          "path", "/some=value",
          "sameSite", "Lax",
          "httpOnly", true,
          "secure", true,
          "value", "value == value=")),
      "origins", Collections.emptyList())), parseJson(state));
  }

  @PlaywrightTest
  @DisplayName("FE-081 should override cookie from Set-Cookie header")
  void shouldOverrideCookieFromSetCookieHeader(TestServer server) {
    ZonedDateTime tomorrow = ZonedDateTime.now().plusDays(1);

    server.setRoute("/setcookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("a=old; expires=" + toUTCString(tomorrow.toInstant())));
      res.end();
    });

    ZonedDateTime dayAfterTomorrow = tomorrow.plusDays(1);
    long dayAfterTomorrowInSeconds = Math.floorDiv(dayAfterTomorrow.toInstant().toEpochMilli(), 1000L);
    server.setRoute("/updatecookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("a=new; expires=" + toUTCString(dayAfterTomorrow.toInstant())));
      res.end();
    });

    request.get(server.PREFIX + "/setcookie.html");
    request.get(server.PREFIX + "/updatecookie.html");

    List<Object> cookies = cookiesOf(request.storageState());

    assertEquals(1, cookies.size());
    Map<?, ?> cookie = (Map<?, ?>) cookies.get(0);
    assertEquals("a", cookie.get("name"));
    assertEquals("new", cookie.get("value"));
    assertEquals((double) dayAfterTomorrowInSeconds, cookie.get("expires"));
  }

  @PlaywrightTest
  @DisplayName("FE-082 should override cookie from Set-Cookie header even if it expired")
  void shouldOverrideCookieFromSetCookieHeaderEvenIfItExpired(TestServer server) throws Exception {
    ZonedDateTime tomorrow = ZonedDateTime.now().plusDays(1);

    server.setRoute("/setcookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("a=ok", "b=ok; expires=" + toUTCString(tomorrow.toInstant())));
      res.end();
    });

    server.setRoute("/unsetsetcookie.html", (req, res) -> {
      String pastDateString = toUTCString(ZonedDateTime.of(1970, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant());
      res.setHeader("Set-Cookie", Arrays.asList("a=; expires=" + pastDateString, "b=; expires=" + pastDateString));
      res.end();
    });

    request.get(server.PREFIX + "/setcookie.html");
    request.get(server.PREFIX + "/unsetsetcookie.html");

    CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/empty.html");
    request.get(server.EMPTY_PAGE);
    TestServer.Request serverRequest = serverRequestPromise.get();

    assertFalsy(serverRequest.headers.get("cookie"));
  }

  @PlaywrightTest
  @DisplayName("FE-083 should export cookies to storage state")
  void shouldExportCookiesToStorageState(TestServer server) {
    // new Date('12/31/2100 PST')
    Instant expires = Instant.parse("2100-12-31T08:00:00Z");
    server.setRoute("/setcookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("a=b", "c=d; expires=" + toUTCString(expires) + "; domain=b.one.com; path=/input", "e=f; domain=b.one.com; path=/input/subfolder"));
      res.end();
    });
    lookupRequest.get("http://a.b.one.com:" + server.PORT + "/setcookie.html");
    String state = lookupRequest.storageState();
    assertEquals(json(mapOf(
      "cookies", Arrays.asList(
        mapOf(
          "name", "a",
          "value", "b",
          "domain", "a.b.one.com",
          "path", "/",
          "expires", -1,
          "httpOnly", false,
          "secure", false,
          "sameSite", "Lax"),
        mapOf(
          "name", "c",
          "value", "d",
          "domain", ".b.one.com",
          "path", "/input",
          "expires", expires.toEpochMilli() / 1000.0,
          "httpOnly", false,
          "secure", false,
          "sameSite", "Lax"),
        mapOf(
          "name", "e",
          "value", "f",
          "domain", ".b.one.com",
          "path", "/input/subfolder",
          "expires", -1,
          "httpOnly", false,
          "secure", false,
          "sameSite", "Lax")),
      "origins", Collections.emptyList())), parseJson(state));
  }

  @PlaywrightTest
  @DisplayName("FE-084 should preserve origin storage on import/export of storage state")
  void shouldPreserveOriginStorageOnImportExportOfStorageState(Playwright playwright, TestServer server) {
    // Java: StorageStateType is passed as its JSON.
    Map<String, Object> storageState = mapOf(
      "cookies", Arrays.asList(
        mapOf(
          "name", "a",
          "value", "b",
          "domain", "a.b.one.com",
          "path", "/",
          "expires", -1,
          "httpOnly", false,
          "secure", false,
          "sameSite", "Lax")),
      "origins", Arrays.asList(
        mapOf(
          "origin", "https://www.example.com",
          "localStorage", Arrays.asList(mapOf(
            "name", "name1",
            "value", "value1")),
          "opfs", Arrays.asList(
            mapOf(
              "path", "hello.txt",
              "type", "file",
              "base64", "SGVsbG8sIHdvcmxkIQ=="),
            mapOf(
              "path", "empty",
              "type", "directory")),
          "indexedDB", Arrays.asList(
            mapOf(
              "name", "db",
              "version", 5,
              "stores", Arrays.asList(
                mapOf(
                  "name", "store",
                  "keyPath", "id",
                  "autoIncrement", false,
                  "indexes", Collections.emptyList(),
                  "records", Arrays.asList(
                    mapOf("value", mapOf("id", "foo", "name", "John Doe"))))))))));
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions().setStorageState(new Gson().toJson(storageState)));
    request.get(server.EMPTY_PAGE);
    String exportedState = request.storageState(new APIRequestContext.StorageStateOptions().setIndexedDB(true).setOpfs(true));
    assertEquals(json(storageState), parseJson(exportedState));
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-085 should send cookies from storage state")
  void shouldSendCookiesFromStorageState(Playwright playwright, TestServer server, ProxyServer proxyServer) throws Exception {
    // new Date('12/31/2099 PST')
    Instant expires = Instant.parse("2099-12-31T08:00:00Z");
    Map<String, Object> storageState = mapOf(
      "cookies", Arrays.asList(
        mapOf(
          "name", "a",
          "value", "b",
          "domain", "a.b.one.com",
          "path", "/",
          "expires", -1,
          "httpOnly", false,
          "secure", false,
          "sameSite", "Lax"),
        mapOf(
          "name", "c",
          "value", "d",
          "domain", ".b.one.com",
          "path", "/first/",
          "expires", expires.toEpochMilli() / 1000.0,
          "httpOnly", false,
          "secure", false,
          "sameSite", "Lax"),
        mapOf(
          "name", "e",
          "value", "f",
          "domain", ".b.one.com",
          "path", "/first/second",
          "expires", -1,
          "httpOnly", false,
          "secure", false,
          "sameSite", "Lax")),
      "origins", Collections.emptyList());
    // Java: the request goes through the proxy of lookupRequest.
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions()
      .setStorageState(new Gson().toJson(storageState)).setProxy(proxyServer.URL));
    CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/first/second/third/not_found.html");
    request.get("http://www.a.b.one.com:" + server.PORT + "/first/second/third/not_found.html");
    TestServer.Request serverRequest = serverRequestPromise.get();
    assertEquals("c=d; e=f", serverRequest.headers.get("cookie"));
  }

  @PlaywrightTest
  @DisplayName("FE-086 storage state should round-trip through file")
  @Fixme("java: storageState({ path }) writes the compact JSON of the state, the JavaScript client writes JSON.stringify(state, undefined, 2)")
  void storageStateShouldRoundTripThroughFile(Playwright playwright, TestServer server, TestInfo testInfo) throws IOException {
    Map<String, Object> storageState = mapOf(
      "cookies", Arrays.asList(
        mapOf(
          "name", "a",
          "value", "b",
          "domain", "a.b.one.com",
          "path", "/",
          "expires", -1,
          "httpOnly", false,
          "secure", false,
          "sameSite", "Lax")),
      "origins", Collections.emptyList());

    APIRequestContext request1 = playwright.request().newContext(new APIRequest.NewContextOptions().setStorageState(new Gson().toJson(storageState)));
    Path path = testInfo.outputPath("storage-state.json");
    String state1 = request1.storageState(new APIRequestContext.StorageStateOptions().setPath(path));
    assertEquals(json(storageState), parseJson(state1));

    String written = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    // Java: JSON.stringify(state1, undefined, 2).
    assertEquals(new GsonBuilder().setPrettyPrinting().create().toJson(JsonParser.parseString(state1)), written);

    APIRequestContext request2 = playwright.request().newContext(new APIRequest.NewContextOptions().setStorageStatePath(path));
    String state2 = request2.storageState();
    assertEquals(json(storageState), parseJson(state2));
  }

  @PlaywrightTest
  @DisplayName("FE-087 should work with empty storage state")
  void shouldWorkWithEmptyStorageState(Playwright playwright, TestServer server, TestInfo testInfo) throws IOException {
    Path storageState = testInfo.outputPath("storage-state.json");
    Files.write(storageState, "{}".getBytes(StandardCharsets.UTF_8));
    APIRequestContext request1 = playwright.request().newContext(new APIRequest.NewContextOptions().setStorageStatePath(storageState));

    String state1 = request1.storageState();
    assertEquals(json(mapOf("cookies", Collections.emptyList(), "origins", Collections.emptyList())), parseJson(state1));
    assertThat(request1.get(server.EMPTY_PAGE)).isOK();
    request1.dispose();
  }
}
