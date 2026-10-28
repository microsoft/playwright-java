/**
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

package com.microsoft.playwright.tests.library;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.ConsoleMessage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.microsoft.playwright.fixtures.Utils.browserMajorVersion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/browsercontext-locale.spec.ts. */
public class TestBrowsercontextLocale {
  @PlaywrightTest
  @DisplayName("LC-001 should affect accept-language header @smoke")
  void shouldAffectAcceptLanguageHeader(Browser browser, TestServer server) throws Exception {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setLocale("fr-CH"));
    Page page = context.newPage();
    CompletableFuture<TestServer.Request> requestFuture = server.waitForRequest("/empty.html");
    page.navigate(server.EMPTY_PAGE);
    TestServer.Request request = requestFuture.get();
    assertEquals("fr-CH", request.headers.get("accept-language").substring(0, 5));
    context.close();
  }

  @PlaywrightTest
  @DisplayName("LC-002 should affect navigator.language")
  void shouldAffectNavigatorLanguage(Browser browser) {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setLocale("fr-FR"));
    Page page = context.newPage();
    assertEquals("fr-FR", page.evaluate("() => navigator.language"));
    context.close();
  }

  @PlaywrightTest
  @DisplayName("LC-003 should format number")
  void shouldFormatNumber(Browser browser, TestServer server) {
    {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setLocale("en-US"));
      Page page = context.newPage();
      page.navigate(server.EMPTY_PAGE);
      assertEquals("1,000,000.5", page.evaluate("() => (1000000.50).toLocaleString()"));
      context.close();
    }
    {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setLocale("fr-FR"));
      Page page = context.newPage();
      page.navigate(server.EMPTY_PAGE);
      assertEquals("1 000 000,5", page.evaluate("() => (1000000.50).toLocaleString().replace(/\\s/g, ' ')"));
      context.close();
    }
  }

  @PlaywrightTest
  @DisplayName("LC-004 should format date")
  void shouldFormatDate(Browser browser, TestServer server, BrowserName browserName) {
    {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setLocale("en-US").setTimezoneId("America/Los_Angeles"));
      Page page = context.newPage();
      page.navigate(server.EMPTY_PAGE);
      String formatted = "Sat Nov 19 2016 10:12:34 GMT-0800 (Pacific Standard Time)";
      assertEquals(formatted, page.evaluate("() => new Date(1479579154987).toString()"));
      context.close();
    }
    {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setLocale("de-DE").setTimezoneId("Europe/Berlin"));
      Page page = context.newPage();
      page.navigate(server.EMPTY_PAGE);
      String formatted = "Sat Nov 19 2016 19:12:34 GMT+0100 (Mitteleuropäische Normalzeit)";
      assertEquals(formatted, page.evaluate("() => new Date(1479579154987).toString()"));
      context.close();
    }
  }

  @PlaywrightTest
  @DisplayName("LC-005 should format number in popups")
  void shouldFormatNumberInPopups(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setLocale("fr-FR"));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);

    Page popup = page.waitForPopup(() -> {
      page.evaluate("url => window.open(url)", server.PREFIX + "/formatted-number.html");
    });
    popup.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
    Object result = popup.evaluate("window[\"result\"]");
    assertEquals("1 000 000,5", result);
    context.close();
  }

  @PlaywrightTest
  @DisplayName("LC-006 should affect navigator.language in popups")
  void shouldAffectNavigatorLanguageInPopups(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setLocale("fr-FR"));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    Page popup = page.waitForPopup(() -> {
      page.evaluate("url => window.open(url)", server.PREFIX + "/formatted-number.html");
    });
    popup.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);
    Object result = popup.evaluate("window.initialNavigatorLanguage");
    assertEquals("fr-FR", result);
    context.close();
  }

  @PlaywrightTest
  @DisplayName("LC-007 should work for multiple pages sharing same process")
  void shouldWorkForMultiplePagesSharingSameProcess(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setLocale("ru-RU"));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    Page popup = page.waitForPopup(() -> {
      page.evaluate("url => { window.open(url); }", server.EMPTY_PAGE);
    });
    Page popup2 = popup.waitForPopup(() -> {
      popup.evaluate("url => { window.open(url); }", server.EMPTY_PAGE);
    });
    context.close();
  }

  @PlaywrightTest
  @DisplayName("LC-008 should be isolated between contexts")
  void shouldBeIsolatedBetweenContexts(Browser browser, TestServer server) {
    // it.slow()

    BrowserContext context1 = browser.newContext(new Browser.NewContextOptions().setLocale("en-US"));
    // By default firefox limits number of child web processes to 8.
    for (int i = 0; i < 8; i++)
      context1.newPage();

    BrowserContext context2 = browser.newContext(new Browser.NewContextOptions().setLocale("ru-RU"));
    Page page2 = context2.newPage();

    String localeNumber = "() => (1000000.50).toLocaleString()";
    List<Object> numbers = new ArrayList<>();
    for (Page page : context1.pages())
      numbers.add(page.evaluate(localeNumber));

    numbers.forEach(value -> assertEquals("1,000,000.5", value));
    assertEquals("1 000 000,5", page2.evaluate(localeNumber));

    context1.close();
    context2.close();
  }

  private static Object getContextLocale(BrowserContext context) {
    Page page = context.newPage();
    return page.evaluate("() => (new Intl.NumberFormat()).resolvedOptions().locale");
  }

  @PlaywrightTest
  @DisplayName("LC-009 should not change default locale in another context")
  void shouldNotChangeDefaultLocaleInAnotherContext(Browser browser) {
    Object defaultLocale;
    {
      BrowserContext context = browser.newContext();
      defaultLocale = getContextLocale(context);
      context.close();
    }
    String localeOverride = "es-MX".equals(defaultLocale) ? "de-DE" : "es-MX";
    {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setLocale(localeOverride));
      assertEquals(localeOverride, getContextLocale(context));
      context.close();
    }
    {
      BrowserContext context = browser.newContext();
      assertEquals(defaultLocale, getContextLocale(context));
      context.close();
    }
  }

  @PlaywrightTest
  @DisplayName("LC-010 should propagate locale to workers")
  @Fixme(value = "driver: 1.64.1 does not apply the locale to dedicated workers in Chromium (navigator.languages is 'languages:en-US' instead of 'languages:ru-RU'), fixed by microsoft/playwright#43488", browsers = BrowserName.CHROMIUM)
  void shouldPropagateLocaleToWorkers(Browser browser, BrowserName browserName, TestServer server) throws Exception {
    // issue: https://github.com/microsoft/playwright/issues/43484
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setLocale("ru-RU"));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    CompletableFuture<TestServer.Request> requestFuture = server.waitForRequest("/title.html");
    ConsoleMessage[] languagesHolder = new ConsoleMessage[1];
    ConsoleMessage msg = page.waitForConsoleMessage(new Page.WaitForConsoleMessageOptions().setPredicate(e -> e.text().startsWith("locale:")), () -> {
      languagesHolder[0] = page.waitForConsoleMessage(new Page.WaitForConsoleMessageOptions().setPredicate(e -> e.text().startsWith("languages:")), () -> {
        page.evaluate("url => new Worker(URL.createObjectURL(new Blob([`\n" +
          "  console.log(\"locale:\" + Intl.NumberFormat().resolvedOptions().locale);\n" +
          "  console.log(\"languages:\" + navigator.languages);\n" +
          "  fetch(${JSON.stringify(url)});\n" +
          "`], { type: 'application/javascript' })))", server.PREFIX + "/title.html");
      });
    });
    ConsoleMessage languages = languagesHolder[0];
    TestServer.Request request = requestFuture.get();
    if (browserName == BrowserName.WEBKIT) {
      // Webkit on Ubuntu is "ru-RU", and on other platforms is "ru"
      assertTrue(msg.text().contains("locale:ru"));
      assertTrue(languages.text().contains("languages:ru"));
    } else {
      assertEquals("locale:ru-RU", msg.text());
      assertEquals("languages:ru-RU", languages.text());
    }
    assertTrue(request.headers.get("accept-language").contains("ru-RU"));
    context.close();
  }

  @PlaywrightTest
  @DisplayName("LC-011 should affect Intl.DateTimeFormat().resolvedOptions().locale")
  void shouldAffectIntlDateTimeFormatResolvedOptionsLocale(Browser browser, TestServer server) {
    // issue: https://github.com/microsoft/playwright/issues/27802
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setLocale("en-GB"));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    assertEquals("en-GB", page.evaluate("() => (new Intl.DateTimeFormat()).resolvedOptions().locale"));
    context.close();
  }

  @PlaywrightTest
  @DisplayName("LC-012 should send user Accept-Language header")
  void shouldSendUserAcceptLanguageHeader(Browser browser, TestServer server) throws Exception {
    // issue: https://github.com/microsoft/playwright/issues/23732
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setLocale("en-GB"));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    {
      CompletableFuture<TestServer.Request> reqPromise = server.waitForRequest("/empty.html");
      page.evaluate("async url => {\n" +
        "  await fetch(url, {\n" +
        "    headers: {\n" +
        "      'Content-Type': 'application/json',\n" +
        "      'Accept-Language': 'de'\n" +
        "    },\n" +
        "  });\n" +
        "}", server.EMPTY_PAGE);
      TestServer.Request req = reqPromise.get();
      assertEquals("de", req.headers.get("accept-language"));
    }
    {
      CompletableFuture<TestServer.Request> reqPromise = server.waitForRequest("/empty.html");
      page.evaluate("async url => {\n" +
        "  await fetch(url, {\n" +
        "    headers: {\n" +
        "      'Content-Type': 'application/json'\n" +
        "    },\n" +
        "  });\n" +
        "}", server.EMPTY_PAGE);
      TestServer.Request req = reqPromise.get();
      assertTrue(req.headers.get("accept-language").contains("en-GB"));
    }
    context.close();
  }

  @PlaywrightTest
  @DisplayName("LC-013 should send Accept-Language header on WebSocket handshake")
  void shouldSendAcceptLanguageHeaderOnWebSocketHandshake(Browser browser, TestServer server, BrowserName browserName) throws Exception {
    assumeFalse(browserName == BrowserName.CHROMIUM && browserMajorVersion(browser) < 151, "fixme: Chromium before 151 sends the browser Accept-Language instead of the emulated locale on WebSocket handshake");
    // issue: https://github.com/microsoft/playwright/issues/23732

    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setLocale("en-GB"));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    CompletableFuture<TestServer.Request> reqPromise = server.waitForWebSocketConnectionRequest();
    page.evaluate("port => { new WebSocket(`ws://localhost:${port}/ws`); }", server.PORT);
    TestServer.Request req = reqPromise.get();
    assertTrue(req.headers.get("accept-language").contains("en-GB"));
    context.close();
  }

  @PlaywrightTest
  @DisplayName("LC-014 should send Accept-Language header on WebSocket handshake from a worker")
  void shouldSendAcceptLanguageHeaderOnWebSocketHandshakeFromAWorker(Browser browser, TestServer server, BrowserName browserName) throws Exception {
    assumeFalse(browserName == BrowserName.CHROMIUM && browserMajorVersion(browser) < 151, "fixme: Chromium before 151 sends the browser Accept-Language instead of the emulated locale on WebSocket handshake");
    // issue: https://github.com/microsoft/playwright/issues/13919

    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setLocale("en-GB"));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    CompletableFuture<TestServer.Request> reqPromise = server.waitForWebSocketConnectionRequest();
    page.evaluate("port => {\n" +
      "  const code = `new WebSocket(${JSON.stringify(`ws://localhost:${port}/ws`)});`;\n" +
      "  new Worker(URL.createObjectURL(new Blob([code], { type: 'text/javascript' })));\n" +
      "}", server.PORT);
    TestServer.Request req = reqPromise.get();
    assertTrue(req.headers.get("accept-language").contains("en-GB"));
    context.close();
  }
}
