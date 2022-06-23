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

import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestBrowserContextLocale extends TestBase {
  @Test
  void shouldAffectAcceptLanguageHeader() throws ExecutionException, InterruptedException {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setLocale("fr-FR"));
    Page page = context.newPage();
    Future<Server.Request> request = server.futureRequest("/empty.html");
    page.navigate(server.EMPTY_PAGE);
    assertEquals("fr-FR", request.get().headers.get("accept-language").get(0).substring(0, 5));
    context.close();
  }

  @Test
  void shouldAffectNavigatorLanguage() {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setLocale("fr-FR"));
    Page page = context.newPage();
    assertEquals("fr-FR", page.evaluate("() => navigator.language"));
    context.close();
  }


  @Test
  void shouldFormatNumber() {
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

  @Test
  void shouldFormatDate() {
    {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions()
        .setLocale("en-US").setTimezoneId("America/Los_Angeles"));
      Page page = context.newPage();
      page.navigate(server.EMPTY_PAGE);
      String formatted = "Sat Nov 19 2016 10:12:34 GMT-0800 (Pacific Standard Time)";
      assertEquals(formatted, page.evaluate("new Date(1479579154987).toString()"));
      context.close();
    }
    {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions()
        .setLocale("de-DE").setTimezoneId("Europe/Berlin"));
      Page page = context.newPage();
      page.navigate(server.EMPTY_PAGE);
      assertEquals("Sat Nov 19 2016 19:12:34 GMT+0100 (Mitteleuropäische Normalzeit)",
        page.evaluate("new Date(1479579154987).toString()"));
      context.close();
    }
  }

  @Test
  void shouldFormatNumberInPopups() {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setLocale("fr-FR"));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    Page popup = page.waitForPopup(() -> page.evaluate(
      "url => window.open(url)", server.PREFIX + "/formatted-number.html"));
    popup.waitForLoadState(LoadState.DOMCONTENTLOADED);
    Object result = popup.evaluate("window['result']");
    assertEquals("1 000 000,5", result);
    context.close();
  }

  @Test
  void shouldAffectNavigatorLanguageInPopups() {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setLocale("fr-FR"));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    Page popup = page.waitForPopup(() -> page.evaluate(
      "url => window.open(url)", server.PREFIX + "/formatted-number.html"));
    popup.waitForLoadState(LoadState.DOMCONTENTLOADED);
    Object result = popup.evaluate("window.initialNavigatorLanguage");
    assertEquals("fr-FR", result);
    context.close();
  }

  @Test
  void shouldWorkForMultiplePagesSharingSameProcess() {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setLocale("ru-RU"));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    Page popup = page.waitForPopup(() -> page.evaluate(
      "url => window.open(url)", server.EMPTY_PAGE));
    popup = page.waitForPopup(() -> page.evaluate(
      "url => window.open(url)", server.EMPTY_PAGE));
    context.close();
  }

  @Test
  void shouldBeIsolatedBetweenContexts() {
    BrowserContext context1 = browser.newContext(new Browser.NewContextOptions().setLocale("en-US"));
    // By default firefox limits number of child web processes to 8.
    for (int i = 0; i < 8; i++)
      context1.newPage();

    BrowserContext context2 = browser.newContext(new Browser.NewContextOptions().setLocale("ru-RU"));
    Page page2 = context2.newPage();

    String localeNumber = "(1000000.50).toLocaleString()";
     for (Page page : context1.pages()) {
       assertEquals("1,000,000.5", page.evaluate(localeNumber));
     }
    assertEquals("1 000 000,5", page2.evaluate(localeNumber));
    context1.close();
    context2.close();
  }

  @Test
  void shouldNotChangeDefaultLocaleInAnotherContext() {
    Function<BrowserContext, String> getContextLocale = (context) -> {
      Page page = context.newPage();
      return (String) page.evaluate("(new Intl.NumberFormat()).resolvedOptions().locale");
    };

    String defaultLocale;
    {
      BrowserContext context = browser.newContext();
      defaultLocale = getContextLocale.apply(context);
      context.close();
    }
    String localeOverride = "es-MX".equals(defaultLocale) ? "de-DE" : "es-MX";
    {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setLocale(localeOverride));
      assertEquals(localeOverride, getContextLocale.apply(context));
      context.close();
    }
    {
      BrowserContext context = browser.newContext();
      assertEquals(defaultLocale, getContextLocale.apply(context));
      context.close();
    }
  }
}

