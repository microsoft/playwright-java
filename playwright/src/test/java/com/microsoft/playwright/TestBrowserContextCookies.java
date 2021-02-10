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

import com.microsoft.playwright.options.SameSiteAttribute;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import java.util.Comparator;
import java.util.List;

import static com.microsoft.playwright.Utils.assertJsonEquals;
import static com.microsoft.playwright.Utils.getOS;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestBrowserContextCookies extends TestBase {
  @Test
  void shouldGetACookie() {
    page.navigate(server.EMPTY_PAGE);
    Object documentCookie = page.evaluate("() => {\n" +
      "  document.cookie = 'username=John Doe';\n" +
      "  return document.cookie;\n" +
      "}");
    assertEquals("username=John Doe", documentCookie);
    List<BrowserContext.Cookie> cookies = context.cookies();
    assertJsonEquals("[{\n" +
      "    name: 'username',\n" +
      "    value: 'John Doe',\n" +
      "    domain: 'localhost',\n" +
      "    path: '/',\n" +
      "    expires: -1,\n" +
      "    httpOnly: false,\n" +
      "    secure: false,\n" +
      "    sameSite: 'NONE'\n" +
      "  }]", cookies);
  }


  @Test
  void shouldGetANonSessionCookie() {
    page.navigate(server.EMPTY_PAGE);
    // @see https://en.wikipedia.org/wiki/Year_2038_problem
    Object documentCookie = page.evaluate("() => {\n" +
      "    const date= new Date('1/1/2038');\n" +
      "    document.cookie = `username=John Doe;expires=${date.toUTCString()}`;\n" +
      "    return document.cookie;\n" +
      "  }");
    assertEquals("username=John Doe", documentCookie);
    int timestamp = (Integer) page.evaluate("+(new Date('1/1/2038'))/1000");
    BrowserContext.Cookie cookie = context.cookies().get(0);
    assertEquals("username", cookie.name());
    assertEquals("John Doe", cookie.value());
    assertEquals("localhost", cookie.domain());
    assertEquals("/", cookie.path());
    assertEquals(timestamp, cookie.expires());
    assertEquals(false, cookie.httpOnly());
    assertEquals(false, cookie.secure());
    assertEquals(SameSiteAttribute.NONE, cookie.sameSite());
  }

  @Test
  void shouldProperlyReportHttpOnlyCookie() {
    server.setRoute("/empty.html", exchange -> {
      exchange.getResponseHeaders().add("Set-Cookie", "name=value;HttpOnly; Path=/");
      exchange.sendResponseHeaders(200, 0);
      exchange.getResponseBody().close();
    });
    page.navigate(server.EMPTY_PAGE);
    List<BrowserContext.Cookie> cookies = context.cookies();
    assertEquals(1, cookies.size());
    assertTrue(cookies.get(0).httpOnly());
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
    List<BrowserContext.Cookie> cookies = context.cookies();
    assertEquals(1, cookies.size());
    assertEquals(SameSiteAttribute.STRICT, cookies.get(0).sameSite());
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
    List<BrowserContext.Cookie> cookies = context.cookies();
    assertEquals(1, cookies.size());
    assertEquals(SameSiteAttribute.LAX, cookies.get(0).sameSite());
  }

  @Test
  void shouldGetMultipleCookies() {
    page.navigate(server.EMPTY_PAGE);
    Object documentCookie = page.evaluate("() => {\n" +
      "  document.cookie = 'username=John Doe';\n" +
      "  document.cookie = 'password=1234';\n" +
      "  return document.cookie.split('; ').sort().join('; ');\n" +
      "}");
    List<BrowserContext.Cookie> cookies = context.cookies();
    cookies.sort(Comparator.comparing(BrowserContext.Cookie::name));
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
      "    sameSite: 'NONE'\n" +
      "  },\n" +
      "  {\n" +
      "    name: 'username',\n" +
      "    value: 'John Doe',\n" +
      "    domain: 'localhost',\n" +
      "    path: '/',\n" +
      "    expires: -1,\n" +
      "    httpOnly: false,\n" +
      "    secure: false,\n" +
      "    sameSite: 'NONE'\n" +
      "  }\n" +
      "]", cookies);
  }

  @Test
  void shouldGetCookiesFromMultipleUrls() {
    context.addCookies(asList(
      new BrowserContext.AddCookie("doggo", "woofs").withUrl("https://foo.com"),
      new BrowserContext.AddCookie("catto", "purrs").withUrl("https://bar.com"),
      new BrowserContext.AddCookie("birdo", "tweets").withUrl("https://baz.com")));
    List<BrowserContext.Cookie> cookies = context.cookies(asList("https://foo.com", "https://baz.com"));
    cookies.sort(Comparator.comparing(BrowserContext.Cookie::name));
    assertJsonEquals("[{\n" +
      "  name: 'birdo',\n" +
      "  value: 'tweets',\n" +
      "  domain: 'baz.com',\n" +
      "  path: '/',\n" +
      "  expires: -1,\n" +
      "  httpOnly: false,\n" +
      "  secure: true,\n" +
      "  sameSite: 'NONE'\n" +
      "}, {\n" +
      "  name: 'doggo',\n" +
      "  value: 'woofs',\n" +
      "  domain: 'foo.com',\n" +
      "  path: '/',\n" +
      "  expires: -1,\n" +
      "  httpOnly: false,\n" +
      "  secure: true,\n" +
      "  sameSite: 'NONE'\n" +
      "}]", cookies);
  }
}
