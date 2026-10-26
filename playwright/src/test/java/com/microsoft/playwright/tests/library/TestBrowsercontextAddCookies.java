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

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.ContextFactory;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.HttpsTestServer;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.SameSiteAttribute;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.function.Executable;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/library/browsercontext-add-cookies.spec.ts. */
public class TestBrowsercontextAddCookies {
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

  // Java: toEqual() of cookies.
  private static void assertCookies(List<Cookie> expected, List<Cookie> actual) {
    assertEquals(expected.size(), actual.size());
    for (int i = 0; i < expected.size(); i++) {
      Cookie e = expected.get(i);
      Cookie a = actual.get(i);
      assertEquals(e.name, a.name);
      assertEquals(e.value, a.value);
      assertEquals(e.domain, a.domain);
      assertEquals(e.path, a.path);
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
  @DisplayName("CO-019 should work @smoke")
  void shouldWork(BrowserContext context, Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    context.addCookies(Arrays.asList(
      new Cookie("password", "123456").setUrl(server.EMPTY_PAGE)));
    assertEquals("password=123456", page.evaluate("() => document.cookie"));
  }

  @PlaywrightTest
  @DisplayName("CO-020 should work with expires=-1")
  void shouldWorkWithExpires1(BrowserContext context, Page page) {
    context.addCookies(Arrays.asList(
      new Cookie("username", "John Doe")
        .setDomain("www.example.com")
        .setPath("/")
        .setExpires(-1)
        .setHttpOnly(false)
        .setSecure(false)
        .setSameSite(SameSiteAttribute.LAX)));
    page.route("**/*", route -> {
      try {
        route.fulfill(new com.microsoft.playwright.Route.FulfillOptions().setBody("<html></html>"));
      } catch (PlaywrightException e) {
      }
    });
    page.navigate("https://www.example.com");
    assertEquals("username=John Doe", page.evaluate("() => document.cookie"));
  }

  @PlaywrightTest
  @DisplayName("CO-021 should add cookies with empty value")
  void shouldAddCookiesWithEmptyValue(BrowserContext context, Page page, TestServer server) {
    context.addCookies(Arrays.asList(
      new Cookie("marker", "")
        .setDomain("www.example.com")
        .setPath("/")
        .setExpires(-1)
        .setHttpOnly(false)
        .setSecure(false)
        .setSameSite(SameSiteAttribute.LAX)));
    page.route("**/*", route -> {
      try {
        route.fulfill(new com.microsoft.playwright.Route.FulfillOptions().setBody("<html></html>"));
      } catch (PlaywrightException e) {
      }
    });
    page.navigate("https://www.example.com");
    assertEquals("marker=", page.evaluate("() => document.cookie"));
  }

  @PlaywrightTest
  @DisplayName("CO-022 should set cookies with SameSite attribute and no secure attribute")
  void shouldSetCookiesWithSameSiteAttributeAndNoSecureAttribute(BrowserContext context, BrowserName browserName) {
    SameSiteAttribute defaultSameSiteCookieValue = defaultSameSiteCookieValue(browserName);
    // Use domain instead of URL to ensure that the `secure` attribute is not set.
    context.addCookies(Arrays.asList(
      new Cookie("same-site-unset", "1")
        .setDomain("foo.com")
        .setPath("/"),
      new Cookie("same-site-none", "1")
        .setDomain("foo.com")
        .setPath("/")
        .setSameSite(SameSiteAttribute.NONE),
      new Cookie("same-site-lax", "1")
        .setDomain("foo.com")
        .setPath("/")
        .setSameSite(SameSiteAttribute.LAX),
      new Cookie("same-site-strict", "1")
        .setDomain("foo.com")
        .setPath("/")
        .setSameSite(SameSiteAttribute.STRICT)));
    List<Cookie> cookies = context.cookies(Arrays.asList("https://foo.com"));
    List<Cookie> expected = new ArrayList<>();
    expected.add(cookie("same-site-unset", "1", "foo.com", "/", -1.0, false, false, defaultSameSiteCookieValue));
    if (!(browserName == BrowserName.CHROMIUM || (browserName == BrowserName.WEBKIT && (Env.isLinux || "webkit-wsl".equals(Env.channel()))) || Env.isBidi()))
      expected.add(cookie("same-site-none", "1", "foo.com", "/", -1.0, false, false, SameSiteAttribute.NONE));
    expected.add(cookie("same-site-lax", "1", "foo.com", "/", -1.0, false, false,
      (browserName == BrowserName.WEBKIT && Env.isWindows && !"webkit-wsl".equals(Env.channel())) ? SameSiteAttribute.NONE : SameSiteAttribute.LAX));
    expected.add(cookie("same-site-strict", "1", "foo.com", "/", -1.0, false, false,
      (browserName == BrowserName.WEBKIT && Env.isWindows && !"webkit-wsl".equals(Env.channel())) ? SameSiteAttribute.NONE : SameSiteAttribute.STRICT));
    assertCookiesAnyOrder(expected, cookies);
  }

  // Java: (cookies: Cookie[]) => cookies.map(c => ({ ...c, expires: Math.floor(c.expires) }))
  private static List<Cookie> normalizedExpires(List<Cookie> cookies) {
    List<Cookie> result = new ArrayList<>();
    for (Cookie c : cookies) {
      Cookie copy = cookie(c.name, c.value, c.domain, c.path, Math.floor(c.expires), c.httpOnly, c.secure, c.sameSite);
      copy.partitionKey = c.partitionKey;
      result.add(copy);
    }
    return result;
  }

  @PlaywrightTest
  @DisplayName("CO-023 should roundtrip cookie")
  void shouldRoundtripCookie(BrowserContext context, Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    // @see https://en.wikipedia.org/wiki/Year_2038_problem
    // Java: a long is not a supported evaluate argument, a double is.
    double date = new GregorianCalendar(2038, Calendar.JANUARY, 1).getTimeInMillis();
    Object documentCookie = page.evaluate("timestamp => {\n" +
      "    const date = new Date(timestamp);\n" +
      "    document.cookie = `username=John Doe;expires=${date.toUTCString()}`;\n" +
      "    return document.cookie;\n" +
      "  }", date);
    assertEquals("username=John Doe", documentCookie);
    List<Cookie> cookies = context.cookies();
    context.clearCookies();
    assertEquals(Collections.emptyList(), context.cookies());
    context.addCookies(cookies);
    // Slightly different rounding on chromium win.
    assertCookies(normalizedExpires(cookies), normalizedExpires(context.cookies()));
  }

  @PlaywrightTest
  @DisplayName("CO-024 should send cookie header")
  void shouldSendCookieHeader(TestServer server, BrowserContext context) {
    AtomicReference<String> cookie = new AtomicReference<>("");
    server.setRoute("/empty.html", (req, res) -> {
      cookie.set(req.headers.get("cookie"));
      res.end();
    });
    context.addCookies(Arrays.asList(new Cookie("cookie", "value").setUrl(server.EMPTY_PAGE)));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    assertEquals("cookie=value", cookie.get());
  }

  @PlaywrightTest
  @DisplayName("CO-025 should isolate cookies in browser contexts")
  void shouldIsolateCookiesInBrowserContexts(BrowserContext context, TestServer server, Browser browser) {
    BrowserContext anotherContext = browser.newContext();
    context.addCookies(Arrays.asList(new Cookie("isolatecookie", "page1value").setUrl(server.EMPTY_PAGE)));
    anotherContext.addCookies(Arrays.asList(new Cookie("isolatecookie", "page2value").setUrl(server.EMPTY_PAGE)));

    List<Cookie> cookies1 = context.cookies();
    List<Cookie> cookies2 = anotherContext.cookies();
    assertEquals(1, cookies1.size());
    assertEquals(1, cookies2.size());
    assertEquals("isolatecookie", cookies1.get(0).name);
    assertEquals("page1value", cookies1.get(0).value);
    assertEquals("isolatecookie", cookies2.get(0).name);
    assertEquals("page2value", cookies2.get(0).value);
    anotherContext.close();
  }

  @PlaywrightTest
  @DisplayName("CO-026 should isolate session cookies")
  void shouldIsolateSessionCookies(BrowserContext context, TestServer server, Browser browser) {
    server.setRoute("/setcookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", "session=value");
      res.end();
    });
    {
      Page page = context.newPage();
      page.navigate(server.PREFIX + "/setcookie.html");
    }
    {
      Page page = context.newPage();
      page.navigate(server.EMPTY_PAGE);
      List<Cookie> cookies = context.cookies();
      assertEquals(1, cookies.size());
      assertEquals("value", cookies.stream().map(c -> c.value).collect(Collectors.joining(",")));
    }
    {
      BrowserContext context2 = browser.newContext();
      Page page = context2.newPage();
      page.navigate(server.EMPTY_PAGE);
      List<Cookie> cookies = context2.cookies();
      assertNull(cookies.isEmpty() ? null : cookies.get(0).name);
      context2.close();
    }
  }

  @PlaywrightTest
  @DisplayName("CO-027 should isolate persistent cookies")
  void shouldIsolatePersistentCookies(BrowserContext context, TestServer server, Browser browser) {
    server.setRoute("/setcookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", "persistent=persistent-value; max-age=3600");
      res.end();
    });
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/setcookie.html");

    BrowserContext context1 = context;
    BrowserContext context2 = browser.newContext();
    // Java: the Promise.all calls run one after the other.
    Page page1 = context1.newPage();
    Page page2 = context2.newPage();
    page1.navigate(server.EMPTY_PAGE);
    page2.navigate(server.EMPTY_PAGE);
    List<Cookie> cookies1 = context1.cookies();
    List<Cookie> cookies2 = context2.cookies();
    assertEquals(1, cookies1.size());
    assertEquals("persistent", cookies1.get(0).name);
    assertEquals("persistent-value", cookies1.get(0).value);
    assertEquals(0, cookies2.size());
    context2.close();
  }

  @PlaywrightTest
  @DisplayName("CO-028 should isolate send cookie header")
  void shouldIsolateSendCookieHeader(TestServer server, BrowserContext context, Browser browser) {
    AtomicReference<String> cookie = new AtomicReference<>("");
    server.setRoute("/empty.html", (req, res) -> {
      String header = req.headers.get("cookie");
      cookie.set(header != null ? header : "");
      res.end();
    });
    context.addCookies(Arrays.asList(new Cookie("sendcookie", "value").setUrl(server.EMPTY_PAGE)));
    {
      Page page = context.newPage();
      page.navigate(server.EMPTY_PAGE);
      assertEquals("sendcookie=value", cookie.get());
    }
    {
      BrowserContext context3 = browser.newContext();
      Page page = context3.newPage();
      page.navigate(server.EMPTY_PAGE);
      assertEquals("", cookie.get());
      context3.close();
    }
  }

  @PlaywrightTest
  @DisplayName("CO-029 should isolate cookies between launches")
  void shouldIsolateCookiesBetweenLaunches(BrowserType browserType, TestServer server) {
    // playwrightTest.slow();

    Browser browser1 = browserType.launch(Env.launchOptions());
    BrowserContext context1 = browser1.newContext();
    context1.addCookies(Arrays.asList(new Cookie("cookie-in-context-1", "value").setUrl(server.EMPTY_PAGE).setExpires(System.currentTimeMillis() / 1000.0 + 10000)));
    browser1.close();

    Browser browser2 = browserType.launch(Env.launchOptions());
    BrowserContext context2 = browser2.newContext();
    List<Cookie> cookies = context2.cookies();
    assertEquals(0, cookies.size());
    browser2.close();
  }

  @PlaywrightTest
  @DisplayName("CO-030 should set multiple cookies")
  void shouldSetMultipleCookies(BrowserContext context, Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    context.addCookies(Arrays.asList(
      new Cookie("multiple-1", "123456").setUrl(server.EMPTY_PAGE),
      new Cookie("multiple-2", "bar").setUrl(server.EMPTY_PAGE)));
    assertEquals(Arrays.asList(
      "multiple-1=123456",
      "multiple-2=bar"), page.evaluate("() => {\n" +
      "    const cookies = document.cookie.split(';');\n" +
      "    return cookies.map(cookie => cookie.trim()).sort();\n" +
      "  }"));
  }

  @PlaywrightTest
  @DisplayName("CO-031 should have |expires| set to |-1| for session cookies")
  void shouldHaveExpiresSetTo1ForSessionCookies(BrowserContext context, TestServer server) {
    context.addCookies(Arrays.asList(
      new Cookie("expires", "123456").setUrl(server.EMPTY_PAGE)));
    List<Cookie> cookies = context.cookies();
    assertEquals(Double.valueOf(-1), cookies.get(0).expires);
  }

  @PlaywrightTest
  @DisplayName("CO-032 should set cookie with reasonable defaults")
  void shouldSetCookieWithReasonableDefaults(BrowserContext context, TestServer server, BrowserName browserName) {
    SameSiteAttribute defaultSameSiteCookieValue = defaultSameSiteCookieValue(browserName);
    context.addCookies(Arrays.asList(
      new Cookie("defaults", "123456").setUrl(server.EMPTY_PAGE)));
    List<Cookie> cookies = context.cookies();
    cookies.sort((a, b) -> a.name.compareTo(b.name));
    assertCookies(Arrays.asList(cookie("defaults", "123456", server.HOSTNAME, "/", -1.0, false, false, defaultSameSiteCookieValue)), cookies);
  }

  @PlaywrightTest
  @DisplayName("CO-033 should set a cookie with a path")
  void shouldSetACookieWithAPath(BrowserContext context, Page page, TestServer server, BrowserName browserName) {
    page.navigate(server.PREFIX + "/grid.html");
    context.addCookies(Arrays.asList(
      new Cookie("gridcookie", "GRID")
        .setDomain(server.HOSTNAME)
        .setPath("/grid.html")
        .setSameSite(SameSiteAttribute.LAX)));
    assertCookies(Arrays.asList(
      cookie("gridcookie", "GRID", server.HOSTNAME, "/grid.html", -1.0, false, false,
        (browserName == BrowserName.WEBKIT && Env.isWindows && !"webkit-wsl".equals(Env.channel())) ? SameSiteAttribute.NONE : SameSiteAttribute.LAX)),
      context.cookies());
    assertEquals("gridcookie=GRID", page.evaluate("document.cookie"));
    page.navigate(server.EMPTY_PAGE);
    assertEquals("", page.evaluate("document.cookie"));
    page.navigate(server.PREFIX + "/grid.html");
    assertEquals("gridcookie=GRID", page.evaluate("document.cookie"));
  }

  @PlaywrightTest
  @DisplayName("CO-034 should not set a cookie with blank page URL")
  void shouldNotSetACookieWithBlankPageURL(BrowserContext context, TestServer server) {
    PlaywrightException error = null;
    try {
      context.addCookies(Arrays.asList(
        new Cookie("example-cookie", "best").setUrl(server.EMPTY_PAGE),
        new Cookie("example-cookie-blank", "best").setUrl("about:blank")));
    } catch (PlaywrightException e) {
      error = e;
    }
    assertTrue(error.getMessage().contains("Blank page can not have cookie \"example-cookie-blank\""));
  }

  @PlaywrightTest
  @DisplayName("CO-035 should not set a cookie on a data URL page")
  void shouldNotSetACookieOnADataURLPage(BrowserContext context) {
    PlaywrightException error = null;
    try {
      context.addCookies(Arrays.asList(new Cookie("example-cookie", "best").setUrl("data:,Hello%2C%20World!")));
    } catch (PlaywrightException e) {
      error = e;
    }
    assertTrue(error.getMessage().contains("Data URL page can not have cookie \"example-cookie\""));
  }

  @PlaywrightTest
  @DisplayName("CO-036 should default to setting secure cookie for HTTPS websites")
  void shouldDefaultToSettingSecureCookieForHTTPSWebsites(BrowserContext context, Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    String SECURE_URL = "https://example.com";
    context.addCookies(Arrays.asList(
      new Cookie("foo", "bar").setUrl(SECURE_URL)));
    Cookie cookie = context.cookies(SECURE_URL).get(0);
    assertEquals(true, cookie.secure);
  }

  @PlaywrightTest
  @DisplayName("CO-037 should be able to set unsecure cookie for HTTP website")
  void shouldBeAbleToSetUnsecureCookieForHTTPWebsite(BrowserContext context, Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    String HTTP_URL = "http://example.com";
    context.addCookies(Arrays.asList(
      new Cookie("foo", "bar").setUrl(HTTP_URL)));
    Cookie cookie = context.cookies(HTTP_URL).get(0);
    assertEquals(false, cookie.secure);
  }

  @PlaywrightTest
  @DisplayName("CO-038 should set a cookie on a different domain")
  void shouldSetACookieOnADifferentDomain(BrowserContext context, Page page, TestServer server, BrowserName browserName) {
    page.navigate(server.EMPTY_PAGE);
    context.addCookies(Arrays.asList(
      new Cookie("example-cookie", "best")
        .setUrl("https://www.example.com")
        .setSameSite(SameSiteAttribute.LAX)));
    assertEquals("", page.evaluate("document.cookie"));
    assertCookies(Arrays.asList(
      cookie("example-cookie", "best", "www.example.com", "/", -1.0, false, true,
        (browserName == BrowserName.WEBKIT && Env.isWindows && !"webkit-wsl".equals(Env.channel())) ? SameSiteAttribute.NONE : SameSiteAttribute.LAX)),
      context.cookies("https://www.example.com"));
  }

  @PlaywrightTest
  @DisplayName("CO-039 should set cookies for a frame")
  void shouldSetCookiesForAFrame(BrowserContext context, Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    context.addCookies(Arrays.asList(
      new Cookie("frame-cookie", "value").setUrl(server.PREFIX)));
    page.evaluate("src => {\n" +
      "    let fulfill;\n" +
      "    const promise = new Promise(x => fulfill = x);\n" +
      "    const iframe = document.createElement('iframe');\n" +
      "    document.body.appendChild(iframe);\n" +
      "    iframe.onload = fulfill;\n" +
      "    iframe.src = src;\n" +
      "    return promise;\n" +
      "  }", server.PREFIX + "/grid.html");

    assertEquals("frame-cookie=value", page.frames().get(1).evaluate("document.cookie"));
  }

  @PlaywrightTest
  @DisplayName("CO-040 should allow unnamed cookies")
  void shouldAllowUnnamedCookies(Page page, BrowserContext context, TestServer server, BrowserName browserName) {
    // Java: expect.soft(): the failures are collected and reported at the end.
    List<Executable> soft = new ArrayList<>();
    server.setRoute("/cookies", (req, res) -> {
      String cookie = req.headers.get("cookie");
      res.write(cookie != null ? cookie : "undefined-on-server");
      res.end();
    });
    context.addCookies(Arrays.asList(
      new Cookie("", "unnamed-via-add-cookies").setUrl(server.EMPTY_PAGE)));
    // Round-trip behavior
    Response resp = page.navigate(server.PREFIX + "/cookies");
    String respText = resp.text();
    Object documentCookie = page.evaluate("document.cookie");
    if ((browserName == BrowserName.WEBKIT && Env.platform().equals("darwin")) || (browserName == BrowserName.FIREFOX && Env.isBidi())) {
      soft.add(() -> assertEquals("undefined-on-server", respText));
      soft.add(() -> assertEquals("", documentCookie));
    } else {
      soft.add(() -> assertEquals("unnamed-via-add-cookies", respText));
      soft.add(() -> assertEquals("unnamed-via-add-cookies", documentCookie));
    }
    // Within PW behavior
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("() => document.cookie = '=unnamed-via-js;'");
    context.addCookies(context.cookies());
    Object documentCookie2 = page.evaluate("document.cookie");
    if ((browserName == BrowserName.WEBKIT && Env.platform().equals("darwin")) || (browserName == BrowserName.FIREFOX && Env.isBidi()))
      soft.add(() -> assertEquals("", documentCookie2));
    else
      soft.add(() -> assertEquals("unnamed-via-js", documentCookie2));
    assertAll(soft);
  }

  @PlaywrightTest
  @DisplayName("CO-041 should set secure cookies on secure WebSocket")
  void shouldSetSecureCookiesOnSecureWebSocket(ContextFactory contextFactory, HttpsTestServer httpsServer) throws Exception {
    CompletableFuture<Map<String, String>> receivedWebSocketHeaders = new CompletableFuture<>();
    httpsServer.onceWebSocketConnection((ws, req) -> receivedWebSocketHeaders.complete(req.headers));
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions().setIgnoreHTTPSErrors(true));
    Page page = context.newPage();
    page.navigate(httpsServer.EMPTY_PAGE);
    context.addCookies(Arrays.asList(
      new Cookie("foo", "bar").setDomain(new URL(httpsServer.PREFIX).getHost()).setPath("/").setSecure(true)));
    page.evaluate("hostname => new WebSocket(`wss://${hostname}/ws`)", httpsServer.HOST);
    Map<String, String> headers = receivedWebSocketHeaders.get();
    assertEquals("foo=bar", headers.get("cookie"));
  }
}
