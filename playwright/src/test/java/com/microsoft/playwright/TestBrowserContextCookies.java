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

import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.SameSiteAttribute;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.microsoft.playwright.Utils.assertJsonEquals;
import static com.microsoft.playwright.Utils.getOS;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class TestBrowserContextCookies extends TestBase {
  @Test
  void shouldGetACookie() {
    page.navigate(server.EMPTY_PAGE);
    Object documentCookie = page.evaluate("() => {\n" +
      "  document.cookie = 'username=John Doe';\n" +
      "  return document.cookie;\n" +
      "}");
    assertEquals("username=John Doe", documentCookie);
    List<Cookie> cookies = context.cookies();
    assertJsonEquals("[{\n" +
      "    name: 'username',\n" +
      "    value: 'John Doe',\n" +
      "    domain: 'localhost',\n" +
      "    path: '/',\n" +
      "    expires: -1,\n" +
      "    httpOnly: false,\n" +
      "    secure: false,\n" +
      "    sameSite: '" + (isChromium() ? "LAX" : "NONE") +"'\n" +
      "  }]", cookies);
  }


  @Test
  void shouldGetANonSessionCookie() {
    page.navigate(server.EMPTY_PAGE);
    // @see https://en.wikipedia.org/wiki/Year_2038_problem
    Object documentCookie = page.evaluate("() => {\n" +
      "  const date = new Date('1/1/2038');\n" +
      "  document.cookie = `username=John Doe;expires=${date.toUTCString()}`;\n" +
      "  return document.cookie;\n" +
      "}");
    assertEquals("username=John Doe", documentCookie);
    List<Cookie> cookies = context.cookies();
    assertEquals(1, cookies.size());
    assertEquals("username", cookies.get(0).name);
    assertEquals("John Doe", cookies.get(0).value);
    assertEquals("localhost", cookies.get(0).domain);
    assertEquals("/", cookies.get(0).path);
    assertFalse(cookies.get(0).httpOnly);
    assertFalse(cookies.get(0).secure);
    assertEquals(defaultSameSiteCookieValue, cookies.get(0).sameSite);

    // Browsers start to cap cookies with 400 days max expires value.
    // See https://github.com/httpwg/http-extensions/pull/1732
    // Chromium patch: https://chromium.googlesource.com/chromium/src/+/aaa5d2b55478eac2ee642653dcd77a50ac3faff6
    // We want to make sure that expires date is at least 400 days in future.
    int FOUR_HUNDRED_DAYS = 1000 * 60 * 60 * 24 * 400;
    int FIVE_MINUTES = 1000 * 60 * 5; // relax condition a bit to make sure test is not flaky.
    assertTrue(cookies.get(0).expires > ((System.currentTimeMillis() + FOUR_HUNDRED_DAYS - FIVE_MINUTES) / 1000));
  }

  @Test
  void shouldProperlyReportHttpOnlyCookie() {
    server.setRoute("/empty.html", exchange -> {
      exchange.getResponseHeaders().add("Set-Cookie", "name=value;HttpOnly; Path=/");
      exchange.sendResponseHeaders(200, 0);
      exchange.getResponseBody().close();
    });
    page.navigate(server.EMPTY_PAGE);
    List<Cookie> cookies = context.cookies();
    assertEquals(1, cookies.size());
    assertTrue(cookies.get(0).httpOnly);
  }

  static boolean isWebKitWindows() {
    return isWebKit() && getOS() == Utils.OS.WINDOWS;
  }

  @Test
  @DisabledIf(value="isWebKitWindows", disabledReason="fail")
  void shouldProperlyReportStrictSameSiteCookie() {
    server.setRoute("/empty.html", exchange -> {
      exchange.getResponseHeaders().add("Set-Cookie", "name=value;SameSite=Strict");
      exchange.sendResponseHeaders(200, 0);
      exchange.getResponseBody().close();
    });
    page.navigate(server.EMPTY_PAGE);
    List<Cookie> cookies = context.cookies();
    assertEquals(1, cookies.size());
    assertEquals(SameSiteAttribute.STRICT, cookies.get(0).sameSite);
  }

  @Test
  @DisabledIf(value="isWebKitWindows", disabledReason="fail")
  void shouldProperlyReportLaxSameSiteCookie() {
    server.setRoute("/empty.html", exchange -> {
      exchange.getResponseHeaders().add("Set-Cookie", "name=value;SameSite=Lax");
      exchange.sendResponseHeaders(200, 0);
      exchange.getResponseBody().close();
    });
    page.navigate(server.EMPTY_PAGE);
    List<Cookie> cookies = context.cookies();
    assertEquals(1, cookies.size());
    assertEquals(SameSiteAttribute.LAX, cookies.get(0).sameSite);
  }

  @Test
  void shouldGetMultipleCookies() {
    page.navigate(server.EMPTY_PAGE);
    Object documentCookie = page.evaluate("() => {\n" +
      "  document.cookie = 'username=John Doe';\n" +
      "  document.cookie = 'password=1234';\n" +
      "  return document.cookie.split('; ').sort().join('; ');\n" +
      "}");
    List<Cookie> cookies = context.cookies();
    cookies.sort(Comparator.comparing(c -> c.name));
    assertEquals("password=1234; username=John Doe", documentCookie);
    assertJsonEquals("[\n" +
      "  {\n" +
      "    name: 'password',\n" +
      "    value: '1234',\n" +
      "    domain: 'localhost',\n" +
      "    path: '/',\n" +
      "    expires: -1,\n" +
      "    httpOnly: false,\n" +
      "    secure: false,\n" +
      "    sameSite: '" + (isChromium() ? "LAX" : "NONE") +"'\n" +
      "  },\n" +
      "  {\n" +
      "    name: 'username',\n" +
      "    value: 'John Doe',\n" +
      "    domain: 'localhost',\n" +
      "    path: '/',\n" +
      "    expires: -1,\n" +
      "    httpOnly: false,\n" +
      "    secure: false,\n" +
      "    sameSite: '" + (isChromium() ? "LAX" : "NONE") +"'\n" +
      "  }\n" +
      "]", cookies);
  }

  @Test
  void shouldGetCookiesFromMultipleUrls() {
    context.addCookies(asList(
      new Cookie("doggo", "woofs").setUrl("https://foo.com"),
      new Cookie("catto", "purrs").setUrl("https://bar.com"),
      new Cookie("birdo", "tweets").setUrl("https://baz.com")));
    List<Cookie> cookies = context.cookies(asList("https://foo.com", "https://baz.com"));
    cookies.sort(Comparator.comparing(c -> c.name));
    assertJsonEquals("[{\n" +
      "  name: 'birdo',\n" +
      "  value: 'tweets',\n" +
      "  domain: 'baz.com',\n" +
      "  path: '/',\n" +
      "  expires: -1.0,\n" +
      "  httpOnly: false,\n" +
      "  secure: true,\n" +
      "  sameSite: '" + (isChromium() ? "LAX" : "NONE") +"'\n" +
      "}, {\n" +
      "  name: 'doggo',\n" +
      "  value: 'woofs',\n" +
      "  domain: 'foo.com',\n" +
      "  path: '/',\n" +
      "  expires: -1.0,\n" +
      "  httpOnly: false,\n" +
      "  secure: true,\n" +
      "  sameSite: '" + (isChromium() ? "LAX" : "NONE") +"'\n" +
      "}]", cookies);
  }

  static boolean isWebkitWindows() {
    return isWebKit() && isWindows;
  }

  @Test
  @DisabledIf(value="isWebkitWindows", disabledReason="Same site is not implemented in curl")
  void shouldAcceptSameSiteAttribute() {
    context.addCookies(asList(
      new Cookie("one", "uno").setUrl(server.EMPTY_PAGE).setSameSite(SameSiteAttribute.LAX),
      new Cookie("two", "dos").setUrl(server.EMPTY_PAGE).setSameSite(SameSiteAttribute.STRICT),
      new Cookie("three", "tres").setUrl(server.EMPTY_PAGE).setSameSite(SameSiteAttribute.NONE)));

    page.navigate(server.EMPTY_PAGE);
    Object documentCookie = page.evaluate("document.cookie.split('; ').sort().join('; ')");
    if (isChromium()) {
      assertEquals("one=uno; two=dos", documentCookie);
    } else {
      assertEquals("one=uno; three=tres; two=dos", documentCookie);
    }

    List<SameSiteAttribute> list = context.cookies().stream().map(c -> c.sameSite).sorted().collect(Collectors.toList());
    if (isChromium()) {
      assertEquals(asList(SameSiteAttribute.STRICT, SameSiteAttribute.LAX), list);
    } else {
      assertEquals(asList(SameSiteAttribute.STRICT, SameSiteAttribute.LAX, SameSiteAttribute.NONE), list);
    }
  }
}
