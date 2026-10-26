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

package com.microsoft.playwright.tests.library;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.SameSiteAttribute;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/browsercontext-cookies.spec.ts. */
public class TestBrowsercontextCookies {
  // Java: the defaultSameSiteCookieValue fixture of tests/config/browserTest.ts.
  private static SameSiteAttribute defaultSameSiteCookieValue(BrowserName browserName) {
    if (browserName == BrowserName.CHROMIUM || Env.isBidi())
      return SameSiteAttribute.LAX;
    if (browserName == BrowserName.WEBKIT && (Env.platform().equals("linux") || "webkit-wsl".equals(Env.channel())))
      return SameSiteAttribute.LAX;
    if (browserName == BrowserName.WEBKIT)
      return SameSiteAttribute.NONE; // Windows + older macOS
    if (browserName == BrowserName.FIREFOX)
      return SameSiteAttribute.NONE;
    throw new IllegalStateException("unknown browser - " + browserName);
  }

  // Java: a cookie as the object of toEqual({ name, value, domain, path, expires, httpOnly, secure, sameSite }).
  private static Cookie cookie(String name, String value, String domain, String path, Double expires, boolean httpOnly, boolean secure, SameSiteAttribute sameSite) {
    Cookie cookie = new Cookie(name, value).setDomain(domain).setPath(path).setHttpOnly(httpOnly).setSecure(secure).setSameSite(sameSite);
    cookie.expires = expires;
    return cookie;
  }

  // Java: toEqual() of cookies; an expected expires of null stands for expect.anything().
  private static void assertCookies(List<Cookie> expected, List<Cookie> actual) {
    assertEquals(expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
      Cookie e = expected.get(i);
      Cookie a = actual.get(i);
      assertEquals(e.name, a.name);
      assertEquals(e.value, a.value);
      assertEquals(e.domain, a.domain);
      assertEquals(e.path, a.path);
      if (e.expires == null)
        assertNotNull(a.expires);
      else
        assertEquals(e.expires, a.expires);
      assertEquals(e.httpOnly, a.httpOnly);
      assertEquals(e.secure, a.secure);
      assertEquals(e.sameSite, a.sameSite);
      assertNull(a.partitionKey);
    }
  }

  // Java: toEqual(new Set([...])); the cookies are compared sorted by name.
  private static void assertCookiesAnyOrder(List<Cookie> expected, List<Cookie> actual) {
    List<Cookie> sortedExpected = new ArrayList<>(expected);
    List<Cookie> sortedActual = new ArrayList<>(actual);
    sortedExpected.sort(Comparator.comparing(c -> c.name));
    sortedActual.sort(Comparator.comparing(c -> c.name));
    assertCookies(sortedExpected, sortedActual);
  }

  @PlaywrightTest
  @DisplayName("CO-001 should return no cookies in pristine browser context")
  void shouldReturnNoCookiesInPristineBrowserContext(BrowserContext context, Page page, TestServer server) {
    assertEquals(Collections.emptyList(), context.cookies());
  }

  @PlaywrightTest
  @DisplayName("CO-002 should get a cookie @smoke")
  void shouldGetACookie(BrowserContext context, Page page, TestServer server, BrowserName browserName) {
    SameSiteAttribute defaultSameSiteCookieValue = defaultSameSiteCookieValue(browserName);
    page.navigate(server.EMPTY_PAGE);
    Object documentCookie = page.evaluate("() => {\n" +
      "    document.cookie = 'username=John Doe';\n" +
      "    return document.cookie;\n" +
      "  }");
    assertEquals("username=John Doe", documentCookie);
    assertCookies(Arrays.asList(cookie("username", "John Doe", server.HOSTNAME, "/", -1.0, false, false, defaultSameSiteCookieValue)), context.cookies());
  }

  @PlaywrightTest
  @DisplayName("CO-003 should get a non-session cookie")
  void shouldGetANonSessionCookie(BrowserContext context, Page page, TestServer server, BrowserName browserName) {
    SameSiteAttribute defaultSameSiteCookieValue = defaultSameSiteCookieValue(browserName);
    page.navigate(server.EMPTY_PAGE);
    // @see https://en.wikipedia.org/wiki/Year_2038_problem
    // Java: new Date('1/1/2038') is midnight in the local time zone.
    // Java: a long is not a supported evaluate argument, a double is.
    double date = new GregorianCalendar(2038, Calendar.JANUARY, 1).getTimeInMillis();
    Object documentCookie = page.evaluate("timestamp => {\n" +
      "    const date = new Date(timestamp);\n" +
      "    document.cookie = `username=John Doe;expires=${date.toUTCString()}`;\n" +
      "    return document.cookie;\n" +
      "  }", date);
    assertEquals("username=John Doe", documentCookie);
    List<Cookie> cookies = context.cookies();
    assertEquals(1, cookies.size());
    assertCookies(Arrays.asList(cookie("username", "John Doe", server.HOSTNAME, "/",
      // We will check this separately.
      null,
      false, false, defaultSameSiteCookieValue)), cookies);
    // Browsers start to cap cookies with 400 days max expires value.
    // See https://github.com/httpwg/http-extensions/pull/1732
    // Chromium patch: https://chromium.googlesource.com/chromium/src/+/aaa5d2b55478eac2ee642653dcd77a50ac3faff6
    // We want to make sure that expires date is at least 400 days in future.
    long FOUR_HUNDRED_DAYS = 1000L * 60 * 60 * 24 * 400;
    long FIVE_MINUTES = 1000 * 60 * 5; // relax condition a bit to make sure test is not flaky.
    assertTrue(cookies.get(0).expires > (System.currentTimeMillis() + FOUR_HUNDRED_DAYS - FIVE_MINUTES) / 1000.0);
  }

  @PlaywrightTest
  @DisplayName("CO-004 should allow adding cookies with >400 days expiration")
  void shouldAllowAddingCookiesWith400DaysExpiration(BrowserContext context, TestServer server) {
    // { annotation: { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/37903' } }
    // Browsers start to cap cookies with 400 days max expires value.
    // See https://github.com/httpwg/http-extensions/pull/1732
    // Chromium patch: https://chromium.googlesource.com/chromium/src/+/aaa5d2b55478eac2ee642653dcd77a50ac3faff6
    double expire = System.currentTimeMillis() / 1000.0 + 401 * 24 * 3600;
    context.addCookies(Arrays.asList(
      new Cookie("username", "John Doe")
        .setDomain(server.HOSTNAME)
        .setPath("/")
        .setExpires(expire)
        .setHttpOnly(false)
        .setSecure(false)
        .setSameSite(SameSiteAttribute.LAX)));

    List<Cookie> cookies = context.cookies();
    assertEquals(1, cookies.size());
    assertEquals("username", cookies.get(0).name);
    assertEquals("John Doe", cookies.get(0).value);
    assertEquals(server.HOSTNAME, cookies.get(0).domain);
    assertEquals("/", cookies.get(0).path);
    assertTrue(cookies.get(0).expires > 0);
    assertTrue(cookies.get(0).expires <= expire);
  }

  @PlaywrightTest
  @DisplayName("CO-005 should properly report httpOnly cookie")
  void shouldProperlyReportHttpOnlyCookie(BrowserContext context, Page page, TestServer server) {
    server.setRoute("/empty.html", (req, res) -> {
      res.setHeader("Set-Cookie", "name=value;HttpOnly; Path=/");
      res.end();
    });
    page.navigate(server.EMPTY_PAGE);
    List<Cookie> cookies = context.cookies();
    assertEquals(1, cookies.size());
    assertEquals(true, cookies.get(0).httpOnly);
  }

  @PlaywrightTest
  @DisplayName("CO-006 should properly report \"Strict\" sameSite cookie")
  void shouldProperlyReportStrictSameSiteCookie(BrowserContext context, Page page, TestServer server, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.WEBKIT && Env.platform().equals("win32") && !"webkit-wsl".equals(Env.channel()), "fail: webkit on Windows reports the sameSite attribute differently");

    server.setRoute("/empty.html", (req, res) -> {
      res.setHeader("Set-Cookie", "name=value;SameSite=Strict");
      res.end();
    });
    page.navigate(server.EMPTY_PAGE);
    List<Cookie> cookies = context.cookies();
    assertEquals(1, cookies.size());
    assertEquals(SameSiteAttribute.STRICT, cookies.get(0).sameSite);
  }

  @PlaywrightTest
  @DisplayName("CO-007 should properly report \"Lax\" sameSite cookie")
  void shouldProperlyReportLaxSameSiteCookie(BrowserContext context, Page page, TestServer server, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.WEBKIT && Env.platform().equals("win32") && !"webkit-wsl".equals(Env.channel()), "fail: webkit on Windows reports the sameSite attribute differently");

    server.setRoute("/empty.html", (req, res) -> {
      res.setHeader("Set-Cookie", "name=value;SameSite=Lax");
      res.end();
    });
    page.navigate(server.EMPTY_PAGE);
    List<Cookie> cookies = context.cookies();
    assertEquals(1, cookies.size());
    assertEquals(SameSiteAttribute.LAX, cookies.get(0).sameSite);
  }

  @PlaywrightTest
  @DisplayName("CO-008 should get multiple cookies")
  void shouldGetMultipleCookies(BrowserContext context, Page page, TestServer server, BrowserName browserName) {
    SameSiteAttribute defaultSameSiteCookieValue = defaultSameSiteCookieValue(browserName);
    page.navigate(server.EMPTY_PAGE);
    Object documentCookie = page.evaluate("() => {\n" +
      "    document.cookie = 'username=John Doe';\n" +
      "    document.cookie = 'password=1234';\n" +
      "    return document.cookie.split('; ').sort().join('; ');\n" +
      "  }");
    List<Cookie> cookies = context.cookies();
    assertEquals("password=1234; username=John Doe", documentCookie);
    assertCookiesAnyOrder(Arrays.asList(
      cookie("password", "1234", server.HOSTNAME, "/", -1.0, false, false, defaultSameSiteCookieValue),
      cookie("username", "John Doe", server.HOSTNAME, "/", -1.0, false, false, defaultSameSiteCookieValue)), cookies);
  }

  @PlaywrightTest
  @DisplayName("CO-009 should get cookies from multiple urls")
  void shouldGetCookiesFromMultipleUrls(BrowserContext context, BrowserName browserName) {
    context.addCookies(Arrays.asList(
      new Cookie("doggo", "woofs").setUrl("https://foo.com").setSameSite(SameSiteAttribute.NONE),
      new Cookie("catto", "purrs").setUrl("https://bar.com").setSameSite(SameSiteAttribute.LAX),
      new Cookie("birdo", "tweets").setUrl("https://baz.com").setSameSite(SameSiteAttribute.LAX)));
    List<Cookie> cookies = context.cookies(Arrays.asList("https://foo.com", "https://baz.com"));
    assertCookiesAnyOrder(Arrays.asList(
      cookie("birdo", "tweets", "baz.com", "/", -1.0, false, true,
        (browserName == BrowserName.WEBKIT && Env.isWindows && !"webkit-wsl".equals(Env.channel())) ? SameSiteAttribute.NONE : SameSiteAttribute.LAX),
      cookie("doggo", "woofs", "foo.com", "/", -1.0, false, true, SameSiteAttribute.NONE)), cookies);
  }

  @PlaywrightTest
  @DisplayName("CO-010 should work with subdomain cookie")
  void shouldWorkWithSubdomainCookie(BrowserContext context, BrowserName browserName) {
    context.addCookies(Arrays.asList(
      new Cookie("doggo", "woofs")
        .setDomain(".foo.com")
        .setPath("/")
        .setSameSite(SameSiteAttribute.LAX)
        .setSecure(true)));
    assertCookies(Arrays.asList(
      cookie("doggo", "woofs", ".foo.com", "/", -1.0, false, true,
        (browserName == BrowserName.WEBKIT && Env.isWindows && !"webkit-wsl".equals(Env.channel())) ? SameSiteAttribute.NONE : SameSiteAttribute.LAX)),
      context.cookies("https://foo.com"));
    assertCookies(Arrays.asList(
      cookie("doggo", "woofs", ".foo.com", "/", -1.0, false, true,
        (browserName == BrowserName.WEBKIT && Env.isWindows && !"webkit-wsl".equals(Env.channel())) ? SameSiteAttribute.NONE : SameSiteAttribute.LAX)),
      context.cookies("https://sub.foo.com"));
  }

  @PlaywrightTest
  @DisplayName("CO-011 should only return cookies that apply to the url")
  @Fixme("driver: context.cookies(url) returns [hostonly, subdomains] for http://foo.com/apiv2, the RFC 6265 domain and path matching is upstream #43148, newer than driver 1.64.1")
  void shouldOnlyReturnCookiesThatApplyToTheUrl(BrowserContext context) {
    // { annotation: { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/43078' } }
    context.addCookies(Arrays.asList(
      new Cookie("hostonly", "v").setDomain("foo.com").setPath("/api"),
      new Cookie("subdomains", "v").setDomain(".foo.com").setPath("/api"),
      new Cookie("slash", "v").setDomain("foo.com").setPath("/foo/")));
    java.util.function.Function<String, List<String>> names = url ->
      context.cookies(url).stream().map(c -> c.name).sorted().collect(Collectors.toList());
    assertEquals(Arrays.asList("hostonly", "subdomains"), names.apply("http://foo.com/api"));
    assertEquals(Arrays.asList("hostonly", "subdomains"), names.apply("http://foo.com/api/x"));
    assertEquals(Collections.emptyList(), names.apply("http://foo.com/apiv2"));
    assertEquals(Arrays.asList("subdomains"), names.apply("http://sub.foo.com/api"));
    assertEquals(Collections.emptyList(), names.apply("http://notfoo.com/api"));
    assertEquals(Collections.emptyList(), names.apply("http://foo.com/foo"));
    assertEquals(Arrays.asList("slash"), names.apply("http://foo.com/foo/"));
    assertEquals(Arrays.asList("slash"), names.apply("http://foo.com/foo/bar"));
  }

  @PlaywrightTest
  @DisplayName("CO-012 should return cookies with empty value")
  void shouldReturnCookiesWithEmptyValue(BrowserContext context, Page page, TestServer server) {
    server.setRoute("/empty.html", (req, res) -> {
      res.setHeader("Set-Cookie", "name=;Path=/");
      res.end();
    });
    page.navigate(server.EMPTY_PAGE);
    List<Cookie> cookies = context.cookies();
    assertEquals(1, cookies.size());
    assertEquals("name", cookies.get(0).name);
    assertEquals("", cookies.get(0).value);
  }

  @PlaywrightTest
  @DisplayName("CO-013 should return secure cookies based on HTTP(S) protocol")
  void shouldReturnSecureCookiesBasedOnHTTPSProtocol(BrowserContext context, BrowserName browserName) {
    context.addCookies(Arrays.asList(
      new Cookie("doggo", "woofs").setUrl("https://foo.com").setSameSite(SameSiteAttribute.LAX).setSecure(true),
      new Cookie("catto", "purrs").setUrl("http://foo.com").setSameSite(SameSiteAttribute.LAX).setSecure(false)));
    List<Cookie> cookies = context.cookies("https://foo.com");
    assertCookiesAnyOrder(Arrays.asList(
      cookie("catto", "purrs", "foo.com", "/", -1.0, false, false,
        (browserName == BrowserName.WEBKIT && Env.isWindows && !"webkit-wsl".equals(Env.channel())) ? SameSiteAttribute.NONE : SameSiteAttribute.LAX),
      cookie("doggo", "woofs", "foo.com", "/", -1.0, false, true,
        (browserName == BrowserName.WEBKIT && Env.isWindows && !"webkit-wsl".equals(Env.channel())) ? SameSiteAttribute.NONE : SameSiteAttribute.LAX)), cookies);
    assertCookies(Arrays.asList(
      cookie("catto", "purrs", "foo.com", "/", -1.0, false, false,
        (browserName == BrowserName.WEBKIT && Env.isWindows && !"webkit-wsl".equals(Env.channel())) ? SameSiteAttribute.NONE : SameSiteAttribute.LAX)),
      context.cookies("http://foo.com/"));
  }

  @Nested
  @ParameterizedClass
  @ValueSource(strings = {"127.0.0.1", "[::1]"})
  class ShouldReturnSecureCookiesForHttpHost {
    @Parameter
    String host;

    @PlaywrightTest
    @DisplayName("CO-014 should return secure cookies for http ${host}")
    void shouldReturnSecureCookiesForHttpHost(BrowserContext context) {
      // { annotation: { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/43115' } }
      context.addCookies(Arrays.asList(new Cookie("a", "v").setDomain(host).setPath("/").setSecure(true)));
      assertEquals(Arrays.asList("a"), context.cookies("http://" + host + "/").stream().map(c -> c.name).collect(Collectors.toList()));
    }
  }

  @PlaywrightTest
  @DisplayName("CO-015 should add cookies with an expiration")
  void shouldAddCookiesWithAnExpiration(BrowserContext context) {
    double expires = Math.floor(System.currentTimeMillis() / 1000.0) + 3600;
    context.addCookies(Arrays.asList(
      new Cookie("doggo", "woofs").setUrl("https://foo.com").setSameSite(SameSiteAttribute.NONE).setExpires(expires)));
    List<Cookie> cookies = context.cookies(Arrays.asList("https://foo.com"));
    assertEquals(1, cookies.size());
    assertCookies(Arrays.asList(cookie("doggo", "woofs", "foo.com", "/", expires, false, true, SameSiteAttribute.NONE)), cookies);
    {
      // Rollover to 5-digit year
      context.addCookies(Arrays.asList(
        new Cookie("doggo", "woofs").setUrl("https://foo.com").setSameSite(SameSiteAttribute.NONE)
          .setExpires(253402300799.0))); // Fri, 31 Dec 9999 23:59:59 +0000 (UTC)
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> context.addCookies(Arrays.asList(
        new Cookie("doggo", "woofs").setUrl("https://foo.com").setSameSite(SameSiteAttribute.NONE)
          .setExpires(253402300800.0)))); // Sat,  1 Jan 1000 00:00:00 +0000 (UTC)
      assertTrue(Pattern.compile("Cookie should have a valid expires").matcher(error.getMessage()).find());
    }

    PlaywrightException error = assertThrows(PlaywrightException.class, () -> context.addCookies(Arrays.asList(
      new Cookie("doggo", "woofs").setUrl("https://foo.com").setSameSite(SameSiteAttribute.NONE)
        .setExpires(-42))));
    assertTrue(Pattern.compile("Cookie should have a valid expires").matcher(error.getMessage()).find());
  }

  @PlaywrightTest
  @DisplayName("CO-016 should support requestStorageAccess")
  @DisabledOnBrowser(value = BrowserName.CHROMIUM, reason = "requestStorageAccess API is not available in Chromium")
  void shouldSupportRequestStorageAccess(Page page, TestServer server, BrowserName browserName) throws Exception {
    // it.info().annotations.push({ type: 'issue', description: 'https://github.com/microsoft/playwright/issues/17285' });

    server.setRoute("/set-cookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", "name=value; Path=/");
      res.end();
    });
    // Navigate once to the domain as top level.
    page.navigate(server.CROSS_PROCESS_PREFIX + "/set-cookie.html");
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<iframe src=\"" + (server.CROSS_PROCESS_PREFIX + "/empty.html") + "\"></iframe>");

    com.microsoft.playwright.Frame frame = page.frames().get(1);
    if (browserName == BrowserName.FIREFOX) {
      assertEquals(true, frame.evaluate("() => document.hasStorageAccess()"));
      {
        CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/title.html");
        frame.evaluate("() => fetch('/title.html')");
        TestServer.Request serverRequest = serverRequestPromise.get();
        assertEquals("name=value", serverRequest.headers.get("cookie"));
      }
    } else {
      if (browserName == BrowserName.WEBKIT && (Env.isLinux || "webkit-wsl".equals(Env.channel())))
        assertEquals(true, frame.evaluate("() => document.hasStorageAccess()"));
      else
        assertEquals(false, frame.evaluate("() => document.hasStorageAccess()"));
      {
        CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/title.html");
        frame.evaluate("() => fetch('/title.html')");
        TestServer.Request serverRequest = serverRequestPromise.get();
        if (Env.isWindows && browserName == BrowserName.WEBKIT && !"webkit-wsl".equals(Env.channel()))
          assertEquals("name=value", serverRequest.headers.get("cookie"));
        else
          assertTrue(serverRequest.headers.get("cookie") == null || serverRequest.headers.get("cookie").isEmpty());
      }
      assertEquals(true, frame.evaluate("() => document.requestStorageAccess().then(() => true, e => false)"));
      assertEquals(true, frame.evaluate("() => document.hasStorageAccess()"));
      {
        CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/title.html");
        frame.evaluate("() => fetch('/title.html')");
        TestServer.Request serverRequest = serverRequestPromise.get();
        if (browserName == BrowserName.WEBKIT && (Env.isLinux || "webkit-wsl".equals(Env.channel())))
          assertNull(serverRequest.headers.get("cookie"));
        else
          assertEquals("name=value", serverRequest.headers.get("cookie"));
      }
    }
  }

  @PlaywrightTest
  @DisplayName("CO-017 should parse cookie with large Max-Age correctly")
  void shouldParseCookieWithLargeMaxAgeCorrectly(TestServer server, Page page, BrowserName browserName) {
    SameSiteAttribute defaultSameSiteCookieValue = defaultSameSiteCookieValue(browserName);
    // it.info().annotations.push({ type: 'issue', description: 'https://github.com/microsoft/playwright/issues/30305' });

    server.setRoute("/foobar", (req, res) -> {
      res.setHeader("set-cookie", Arrays.asList(
        "cookie1=value1; Path=/; Expires=Thu, 08 Sep 2270 15:06:12 GMT; Max-Age=7776000000"));
      res.statusCode = 200;
      res.end();
    });
    page.navigate(server.PREFIX + "/foobar");
    assertEquals("cookie1=value1", page.evaluate("() => document.cookie"));
    assertCookies(Arrays.asList(
      cookie("cookie1", "value1", server.HOSTNAME, "/", null, false, false, defaultSameSiteCookieValue)),
      page.context().cookies());
  }

  @PlaywrightTest
  @DisplayName("CO-018 iframe should inherit cookies from parent")
  void iframeShouldInheritCookiesFromParent(Page page) {
    // { annotation: { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/35439' } }
    page.route("**/*", route -> {
      if (route.request().url().contains("sub.example.test")) {
        route.fulfill(new Route.FulfillOptions()
          .setBody("\n" +
            "            <p id=\"result\"></p>\n" +
            "            <script>document.getElementById('result').textContent = document.cookie || 'no cookies';</script>\n" +
            "        ")
          .setContentType("text/html"));
        return;
      }
      route.fulfill(new Route.FulfillOptions()
        .setHeaders(Collections.singletonMap("set-cookie", "testCookie=value; SameSite=Lax; Domain=example.test"))
        .setContentType("text/html")
        .setBody("\n" +
          "          <p id=\"result\"></p>\n" +
          "          <script>document.getElementById('result').textContent = document.cookie || 'no cookies';</script>\n" +
          "          <iframe src=\"http://sub.example.test\"></iframe>\n" +
          "      "));
    });

    page.navigate("http://example.test");

    assertThat(page.locator("body")).containsText("testCookie=value");
    assertThat(page.frameLocator("iframe").locator("body")).containsText("testCookie=value");
  }
}
