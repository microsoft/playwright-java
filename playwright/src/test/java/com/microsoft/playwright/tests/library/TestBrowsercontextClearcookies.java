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
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.EnabledOnBrowser;
import com.microsoft.playwright.fixtures.HttpsTestServer;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.SameSiteAttribute;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.OS;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Port of tests/library/browsercontext-clearcookies.spec.ts. */
public class TestBrowsercontextClearcookies {
  // Java: new URL(url).hostname
  private static String hostname(String url) {
    return URI.create(url).getHost();
  }

  // Java: expect.poll(() => value).toEqual(expected); the value is read in a loop, with waitForTimeout() between the reads.
  private static void expectPollToEqual(Page page, Supplier<Object> value, Object expected) {
    for (int i = 0; i < 100 && !expected.equals(value.get()); i++)
      page.waitForTimeout(100);
    assertEquals(expected, value.get());
  }

  @PlaywrightTest
  @DisplayName("CO-042 should clear cookies")
  void shouldClearCookies(BrowserContext context, Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    context.addCookies(Arrays.asList(
      new Cookie("cookie1", "1").setUrl(server.EMPTY_PAGE)));
    assertEquals("cookie1=1", page.evaluate("document.cookie"));
    context.clearCookies();
    assertEquals(Collections.emptyList(), context.cookies());
    page.reload();
    assertEquals("", page.evaluate("document.cookie"));
  }

  @PlaywrightTest
  @DisplayName("CO-043 should isolate cookies when clearing")
  void shouldIsolateCookiesWhenClearing(BrowserContext context, TestServer server, Browser browser) {
    BrowserContext anotherContext = browser.newContext();
    context.addCookies(Arrays.asList(new Cookie("page1cookie", "page1value").setUrl(server.EMPTY_PAGE)));
    anotherContext.addCookies(Arrays.asList(new Cookie("page2cookie", "page2value").setUrl(server.EMPTY_PAGE)));

    assertEquals(1, context.cookies().size());
    assertEquals(1, anotherContext.cookies().size());

    context.clearCookies();
    assertEquals(0, context.cookies().size());
    assertEquals(1, anotherContext.cookies().size());

    anotherContext.clearCookies();
    assertEquals(0, context.cookies().size());
    assertEquals(0, anotherContext.cookies().size());
    anotherContext.close();
  }

  @PlaywrightTest
  @DisplayName("CO-044 should remove cookies by name")
  void shouldRemoveCookiesByName(BrowserContext context, Page page, TestServer server) {
    context.addCookies(Arrays.asList(
      new Cookie("cookie1", "1")
        .setDomain(hostname(server.PREFIX))
        .setPath("/"),
      new Cookie("cookie2", "2")
        .setDomain(hostname(server.PREFIX))
        .setPath("/")));
    page.navigate(server.PREFIX);
    assertEquals("cookie1=1; cookie2=2", page.evaluate("document.cookie"));
    context.clearCookies(new BrowserContext.ClearCookiesOptions().setName("cookie1"));
    assertEquals("cookie2=2", page.evaluate("document.cookie"));
  }

  @PlaywrightTest
  @DisplayName("CO-045 should remove cookies by name regex")
  void shouldRemoveCookiesByNameRegex(BrowserContext context, Page page, TestServer server) {
    context.addCookies(Arrays.asList(
      new Cookie("cookie1", "1")
        .setDomain(hostname(server.PREFIX))
        .setPath("/"),
      new Cookie("cookie2", "2")
        .setDomain(hostname(server.PREFIX))
        .setPath("/")));
    page.navigate(server.PREFIX);
    assertEquals("cookie1=1; cookie2=2", page.evaluate("document.cookie"));
    context.clearCookies(new BrowserContext.ClearCookiesOptions().setName(Pattern.compile("coo.*1")));
    assertEquals("cookie2=2", page.evaluate("document.cookie"));
  }

  @PlaywrightTest
  @DisplayName("CO-046 should remove cookies by domain")
  void shouldRemoveCookiesByDomain(BrowserContext context, Page page, TestServer server) {
    context.addCookies(Arrays.asList(
      new Cookie("cookie1", "1")
        .setDomain(hostname(server.PREFIX))
        .setPath("/"),
      new Cookie("cookie2", "2")
        .setDomain(hostname(server.CROSS_PROCESS_PREFIX))
        .setPath("/")));
    page.navigate(server.PREFIX);
    assertEquals("cookie1=1", page.evaluate("document.cookie"));
    page.navigate(server.CROSS_PROCESS_PREFIX);
    assertEquals("cookie2=2", page.evaluate("document.cookie"));
    context.clearCookies(new BrowserContext.ClearCookiesOptions().setDomain(hostname(server.CROSS_PROCESS_PREFIX)));
    assertEquals("", page.evaluate("document.cookie"));
    page.navigate(server.PREFIX);
    assertEquals("cookie1=1", page.evaluate("document.cookie"));
  }

  @PlaywrightTest
  @DisplayName("CO-047 should remove cookies by path")
  void shouldRemoveCookiesByPath(BrowserContext context, Page page, TestServer server) {
    context.addCookies(Arrays.asList(
      new Cookie("cookie1", "1")
        .setDomain(hostname(server.PREFIX))
        .setPath("/api/v1"),
      new Cookie("cookie2", "2")
        .setDomain(hostname(server.PREFIX))
        .setPath("/api/v2"),
      new Cookie("cookie3", "3")
        .setDomain(hostname(server.PREFIX))
        .setPath("/")));
    page.navigate(server.PREFIX + "/api/v1");
    assertEquals("cookie1=1; cookie3=3", page.evaluate("document.cookie"));
    context.clearCookies(new BrowserContext.ClearCookiesOptions().setPath("/api/v1"));
    assertEquals("cookie3=3", page.evaluate("document.cookie"));
    page.navigate(server.PREFIX + "/api/v2");
    assertEquals("cookie2=2; cookie3=3", page.evaluate("document.cookie"));
    page.navigate(server.PREFIX + "/");
    assertEquals("cookie3=3", page.evaluate("document.cookie"));
  }

  @PlaywrightTest
  @DisplayName("CO-048 should remove cookies by name and domain")
  void shouldRemoveCookiesByNameAndDomain(BrowserContext context, Page page, TestServer server) {
    context.addCookies(Arrays.asList(
      new Cookie("cookie1", "1")
        .setDomain(hostname(server.PREFIX))
        .setPath("/"),
      new Cookie("cookie1", "1")
        .setDomain(hostname(server.CROSS_PROCESS_PREFIX))
        .setPath("/")));
    page.navigate(server.PREFIX);
    assertEquals("cookie1=1", page.evaluate("document.cookie"));
    context.clearCookies(new BrowserContext.ClearCookiesOptions().setName("cookie1").setDomain(hostname(server.PREFIX)));
    assertEquals("", page.evaluate("document.cookie"));
    page.navigate(server.CROSS_PROCESS_PREFIX);
    assertEquals("cookie1=1", page.evaluate("document.cookie"));
  }

  @PlaywrightTest
  @DisplayName("CO-049 should not transiently delete non-matching cookies when filtering")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, os = OS.WINDOWS, reason = "cookieStore change events not supported on WebKit/Windows (curl backend lacks cookie change notifications)")
  void shouldNotTransientlyDeleteNonMatchingCookiesWhenFiltering(BrowserContext context, Page page, TestServer server) {
    // { annotation: { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/40953' } }
    context.addCookies(Arrays.asList(
      new Cookie("keep_me", "1")
        .setDomain(hostname(server.PREFIX))
        .setPath("/"),
      new Cookie("delete_me", "2")
        .setDomain(hostname(server.PREFIX))
        .setPath("/")));
    page.navigate(server.PREFIX);

    JSHandle eventsHandle = page.evaluateHandle("() => {\n" +
      "    const events = [];\n" +
      "    cookieStore.addEventListener('change', event => {\n" +
      "      for (const changed of event.changed)\n" +
      "        events.push(`changed ${changed.name}`);\n" +
      "      for (const deleted of event.deleted)\n" +
      "        events.push(`deleted ${deleted.name}`);\n" +
      "    });\n" +
      "    return events;\n" +
      "  }");

    context.clearCookies(new BrowserContext.ClearCookiesOptions().setName("delete_me"));
    expectPollToEqual(page, () -> eventsHandle.jsonValue(), Arrays.asList("deleted delete_me"));
    assertEquals("keep_me=1", page.evaluate("document.cookie"));
  }

  @PlaywrightTest
  @DisplayName("CO-050 should remove partitioned cookies by name")
  @EnabledOnBrowser(value = BrowserName.CHROMIUM, reason = "Partitioned cookies (CHIPS) are Chromium-specific")
  void shouldRemovePartitionedCookiesByName(Browser browser, HttpsTestServer httpsServer) {
    // { annotation: { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/40953' } }
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setIgnoreHTTPSErrors(true));
    context.addCookies(Arrays.asList(
      new Cookie("delete_me", "1")
        .setDomain(httpsServer.HOSTNAME)
        .setPath("/")
        .setSecure(true)
        .setSameSite(SameSiteAttribute.NONE)
        .setPartitionKey("https://" + httpsServer.HOSTNAME),
      new Cookie("keep_me", "2")
        .setDomain(httpsServer.HOSTNAME)
        .setPath("/")
        .setSecure(true)
        .setSameSite(SameSiteAttribute.NONE)
        .setPartitionKey("https://" + httpsServer.HOSTNAME)));
    List<Cookie> before = context.cookies();
    assertEquals(2, before.size());
    assertEquals("delete_me", before.get(0).name);
    assertEquals("https://" + httpsServer.HOSTNAME, before.get(0).partitionKey);
    assertEquals("keep_me", before.get(1).name);
    assertEquals("https://" + httpsServer.HOSTNAME, before.get(1).partitionKey);

    context.clearCookies(new BrowserContext.ClearCookiesOptions().setName("delete_me"));
    List<Cookie> after = context.cookies();
    assertEquals(1, after.size());
    assertEquals("keep_me", after.get(0).name);
    assertEquals("https://" + httpsServer.HOSTNAME, after.get(0).partitionKey);
    context.close();
  }

  @PlaywrightTest
  @DisplayName("CO-051 should remove nameless cookies by empty name")
  void shouldRemoveNamelessCookiesByEmptyName(BrowserContext context) {
    // { annotation: { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/43112' } }
    context.addCookies(Arrays.asList(
      new Cookie("", "nameless").setDomain("example.com").setPath("/"),
      new Cookie("session", "secret").setDomain("example.com").setPath("/")));
    context.clearCookies(new BrowserContext.ClearCookiesOptions().setName(""));
    assertEquals(Arrays.asList("session"), context.cookies().stream().map(cookie -> cookie.name).collect(Collectors.toList()));
  }
}
