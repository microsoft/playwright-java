/*
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

package com.microsoft.playwright;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.microsoft.playwright.Utils.assertJsonEquals;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

public class TestBrowserContextAddCookies extends TestBase {
  @Test
  void shouldWork() {
    page.navigate(server.EMPTY_PAGE);
    context.addCookies(asList(
      new BrowserContext.AddCookie("password", "123456").withUrl(server.EMPTY_PAGE)));
    assertEquals("password=123456", page.evaluate("document.cookie"));
  }

  @Test
  void shouldRoundtripCookie() {
    page.navigate(server.EMPTY_PAGE);
    // @see https://en.wikipedia.org/wiki/Year_2038_problem
    Object documentCookie = page.evaluate("() => {\n" +
      "  const date = new Date('1/1/2038');\n" +
      "  document.cookie = `username=John Doe;expires=${date.toUTCString()}`;\n" +
      "  return document.cookie;\n" +
      "}");
    assertEquals("username=John Doe", documentCookie);
    List<BrowserContext.Cookie> cookies = context.cookies();
    context.clearCookies();
    assertEquals(emptyList(), context.cookies());
    context.addCookies(asList(new BrowserContext.AddCookie(cookies.get(0).name(), cookies.get(0).value())
      .withDomain(cookies.get(0).domain())
      .withPath(cookies.get(0).path())
      .withExpires(cookies.get(0).expires())
    ));
    assertJsonEquals(new Gson().toJson(cookies), context.cookies());
  }

  @Test
  void shouldSendCookieHeader() throws ExecutionException, InterruptedException {
    Future<Server.Request> request = server.futureRequest("/empty.html");
    context.addCookies(asList(
      new BrowserContext.AddCookie("cookie", "value").withUrl(server.EMPTY_PAGE)));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    List<String> cookies = request.get().headers.get("cookie");
    assertEquals(singletonList("cookie=value"), cookies);
  }

  @Test
  void shouldIsolateCookiesInBrowserContexts() {
    BrowserContext anotherContext = browser.newContext();
    context.addCookies(asList(
      new BrowserContext.AddCookie("isolatecookie", "page1value").withUrl(server.EMPTY_PAGE)));
    anotherContext.addCookies(asList(
      new BrowserContext.AddCookie("isolatecookie", "page2value").withUrl(server.EMPTY_PAGE)));
    List<BrowserContext.Cookie> cookies1 = context.cookies();
    List<BrowserContext.Cookie> cookies2 = anotherContext.cookies();
    assertEquals(1, cookies1.size());
    assertEquals(1, cookies2.size());
    assertEquals("isolatecookie", cookies1.get(0).name());
    assertEquals("page1value", cookies1.get(0).value());
    assertEquals("isolatecookie", cookies2.get(0).name());
    assertEquals("page2value", cookies2.get(0).value());
    anotherContext.close();
  }

  @Test
  void shouldIsolateSessionCookies() {
    server.setRoute("/setcookie.html", exchange -> {
      exchange.getResponseHeaders().add("Set-Cookie", "session=value");
      exchange.sendResponseHeaders(200, 0);
      exchange.getResponseBody().close();
    });
    {
      Page page = context.newPage();
      page.navigate(server.PREFIX + "/setcookie.html");
    }
    {
      Page page = context.newPage();
      page.navigate(server.EMPTY_PAGE);
      List<BrowserContext.Cookie> cookies = context.cookies();
      assertEquals(1, cookies.size());
      assertEquals("value", cookies.get(0).value());
    }
    {
      BrowserContext context2 = browser.newContext();
      Page page = context2.newPage();
      page.navigate(server.EMPTY_PAGE);
      List<BrowserContext.Cookie> cookies = context2.cookies();
      assertEquals(0, cookies.size());
      context2.close();
    }
  }

  @Test
  void shouldIsolatePersistentCookies() {
    server.setRoute("/setcookie.html", exchange -> {
      exchange.getResponseHeaders().add("Set-Cookie", "persistent=persistent-value; max-age=3600");
      exchange.sendResponseHeaders(200, 0);
      exchange.getResponseBody().close();
    });
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/setcookie.html");

    BrowserContext context1 = context;
    BrowserContext context2 = browser.newContext();
    Page page1 = context1.newPage();
    Page page2 = context2.newPage();
    page1.navigate(server.EMPTY_PAGE);
    page2.navigate(server.EMPTY_PAGE);
    List<BrowserContext.Cookie> cookies1 = context1.cookies();
    List<BrowserContext.Cookie> cookies2 = context2.cookies();
    assertEquals(1, cookies1.size());
    assertEquals("persistent", cookies1.get(0).name());
    assertEquals("persistent-value", cookies1.get(0).value());
    assertEquals(0, cookies2.size());
    context2.close();
  }

  @Test
  void shouldIsolateSendCookieHeader() throws ExecutionException, InterruptedException {
    context.addCookies(asList(
      new BrowserContext.AddCookie("sendcookie", "value").withUrl(server.EMPTY_PAGE)));
    {
      Page page = context.newPage();
      Future<Server.Request> request = server.futureRequest("/empty.html");
      page.navigate(server.EMPTY_PAGE);
      List<String> cookies = request.get().headers.get("cookie");
      assertEquals(asList("sendcookie=value"), cookies);
    }
    {
      BrowserContext context = browser.newContext();
      Page page = context.newPage();
      Future<Server.Request> request = server.futureRequest("/empty.html");
      page.navigate(server.EMPTY_PAGE);
      List<String> cookies = request.get().headers.get("cookie");
      assertNull(cookies);
      context.close();
    }
  }
  @Test
  void shouldIsolateCookiesBetweenLaunches() {
//    test.slow();
// TODO:  const browser1 = browserType.launch(browserOptions);
    Browser browser1 = browserType.launch();
    BrowserContext context1 = browser1.newContext();
    context1.addCookies(asList(new BrowserContext.AddCookie("cookie-in-context-1", "value")
      .withUrl(server.EMPTY_PAGE)
      .withExpires(Instant.now().getEpochSecond() +  + 10000)));
    browser1.close();

//  const browser2 = browserType.launch(browserOptions);
    Browser browser2 = browserType.launch();
    BrowserContext context2 = browser2.newContext();
    List<BrowserContext.Cookie> cookies = context2.cookies();
    assertEquals(0, cookies.size());
    browser2.close();
  }

  @Test
  void shouldSetMultipleCookies() {
    page.navigate(server.EMPTY_PAGE);
    context.addCookies(asList(
      new BrowserContext.AddCookie("multiple-1", "123456").withUrl(server.EMPTY_PAGE),
      new BrowserContext.AddCookie("multiple-2", "bar").withUrl(server.EMPTY_PAGE)
    ));
    assertEquals(asList("multiple-1=123456", "multiple-2=bar"), page.evaluate("() => {\n" +
      "  const cookies = document.cookie.split(';');\n" +
      "  return cookies.map(cookie => cookie.trim()).sort();\n" +
      "}"));
  }

  @Test
  void shouldHaveExpiresSetTo1ForSessionCookies() {
    context.addCookies(asList(
      new BrowserContext.AddCookie("expires", "123456").withUrl(server.EMPTY_PAGE)));
    List<BrowserContext.Cookie> cookies = context.cookies();
    assertEquals(-1, cookies.get(0).expires());
  }

  @Test
  void shouldSetCookieWithReasonableDefaults() {
    context.addCookies(asList(
      new BrowserContext.AddCookie("defaults", "123456").withUrl(server.EMPTY_PAGE)));
    List<BrowserContext.Cookie> cookies = context.cookies();
    assertJsonEquals("[{\n" +
      "  name: 'defaults',\n" +
      "  value: '123456',\n" +
      "  domain: 'localhost',\n" +
      "  path: '/',\n" +
      "  expires: -1,\n" +
      "  httpOnly: false,\n" +
      "  secure: false,\n" +
      "  sameSite: 'NONE'\n" +
      "}]", cookies);
  }

  @Test
  void shouldSetACookieWithAPath() {
    page.navigate(server.PREFIX + "/grid.html");
    context.addCookies(asList(new BrowserContext.AddCookie("gridcookie", "GRID")
      .withDomain("localhost")
      .withPath("/grid.html")));
    List<BrowserContext.Cookie> cookies = context.cookies();
    assertJsonEquals("[{\n" +
      "  name: 'gridcookie',\n" +
      "  value: 'GRID',\n" +
      "  domain: 'localhost',\n" +
      "  path: '/grid.html',\n" +
      "  expires: -1,\n" +
      "  httpOnly: false,\n" +
      "  secure: false,\n" +
      "  sameSite: 'NONE'\n" +
      "}]", cookies);
    assertEquals("gridcookie=GRID", page.evaluate("document.cookie"));
    page.navigate(server.EMPTY_PAGE);
    assertEquals("", page.evaluate("document.cookie"));
    page.navigate(server.PREFIX + "/grid.html");
    assertEquals("gridcookie=GRID", page.evaluate("document.cookie"));
  }

  @Test
  void shouldNotSetACookieWithBlankPageURL() {
    try {
      context.addCookies(asList(
        new BrowserContext.AddCookie("example-cookie", "best").withUrl(server.EMPTY_PAGE),
        new BrowserContext.AddCookie("example-cookie-blank", "best").withUrl("about:blank")
      ));
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("Blank page can not have cookie \"example-cookie-blank\""));
    }
  }

  @Test
  void shouldNotSetACookieOnADataURLPage() {
    try {
      context.addCookies(asList(
        new BrowserContext.AddCookie("example-cookie", "best").withUrl("data:,Hello%2C%20World!")
      ));
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("Data URL page can not have cookie \"example-cookie\""));
    }
  }

  @Test
  void shouldDefaultToSettingSecureCookieForHTTPSWebsites() {
    page.navigate(server.EMPTY_PAGE);
    String SECURE_URL = "https://example.com";
    context.addCookies(asList(
      new BrowserContext.AddCookie("foo", "bar").withUrl(SECURE_URL)
    ));
    List<BrowserContext.Cookie> cookies = context.cookies(SECURE_URL);
    assertEquals(1, cookies.size());
    assertTrue(cookies.get(0).secure());
  }

  @Test
  void shouldBeAbleToSetUnsecureCookieForHTTPWebsite() {
    page.navigate(server.EMPTY_PAGE);
    String HTTP_URL = "http://example.com";
    context.addCookies(asList(
      new BrowserContext.AddCookie("foo", "bar").withUrl(HTTP_URL)
    ));
    List<BrowserContext.Cookie> cookies = context.cookies(HTTP_URL);
    assertEquals(1, cookies.size());
    assertFalse(cookies.get(0).secure());
  }

  @Test
  void shouldSetACookieOnADifferentDomain() {
    page.navigate(server.EMPTY_PAGE);
    context.addCookies(asList(
      new BrowserContext.AddCookie("example-cookie", "best").withUrl("https://www.example.com")
    ));
    assertEquals("", page.evaluate("document.cookie"));
    assertJsonEquals("[{\n" +
      "  name: 'example-cookie',\n" +
      "  value: 'best',\n" +
      "  domain: 'www.example.com',\n" +
      "  path: '/',\n" +
      "  expires: -1,\n" +
      "  httpOnly: false,\n" +
      "  secure: true,\n" +
      "  sameSite: 'NONE'\n" +
      "}]", context.cookies("https://www.example.com"));
  }

  @Test
  void shouldSetCookiesForAFrame() {
    page.navigate(server.EMPTY_PAGE);
    context.addCookies(asList(
      new BrowserContext.AddCookie("frame-cookie", "value").withUrl(server.PREFIX)
    ));
    page.evaluate("src => {\n" +
      "  let fulfill;\n" +
      "  const promise = new Promise(x => fulfill = x);\n" +
      "  const iframe = document.createElement('iframe');\n" +
      "  document.body.appendChild(iframe);\n" +
      "  iframe.onload = fulfill;\n" +
      "  iframe.src = src;\n" +
      "  return promise;\n" +
      "}", server.PREFIX + "/grid.html");
    assertEquals("frame-cookie=value", page.frames().get(1).evaluate("document.cookie"));
  }

  @Test
  void shouldNotBlockThirdPartyCookies() {
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("src => {\n" +
      "  let fulfill;\n" +
      "  const promise = new Promise(x => fulfill = x);\n" +
      "  const iframe = document.createElement('iframe');\n" +
      "  document.body.appendChild(iframe);\n" +
      "  iframe.onload = fulfill;\n" +
      "  iframe.src = src;\n" +
      "  return promise;\n" +
      "}", server.CROSS_PROCESS_PREFIX + "/grid.html");
    page.frames().get(1).evaluate("document.cookie = 'username=John Doe'");
    page.waitForTimeout(2000);
    boolean allowsThirdParty = isChromium() || isFirefox();
    List<BrowserContext.Cookie> cookies = context.cookies(server.CROSS_PROCESS_PREFIX + "/grid.html");
    if (allowsThirdParty) {
      assertJsonEquals("[{\n" +
        "  'domain': '127.0.0.1',\n" +
        "  'expires': -1,\n" +
        "  'httpOnly': false,\n" +
        "  'name': 'username',\n" +
        "  'path': '/',\n" +
        "  'sameSite': 'NONE',\n" +
        "  'secure': false,\n" +
        "  'value': 'John Doe'\n" +
        "}]", cookies);
    } else {
      assertEquals(0, cookies.size());
    }
  }
}
