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
import com.microsoft.playwright.ConsoleMessage;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/library/browsercontext-timezone-id.spec.ts. */
public class TestBrowsercontextTimezoneId {
  @PlaywrightTest
  @DisplayName("TZ-001 should work @smoke")
  void shouldWork(Browser browser, BrowserName browserName) {
    // Note: timezone names are rendered differently between browsers and platforms,
    // so we only check the GMT offset.
    String func = "() => new Date(1479579154987).toString()";
    {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setLocale("en-US").setTimezoneId("America/Jamaica"));
      Page page = context.newPage();
      assertTrue(((String) page.evaluate(func)).contains("Sat Nov 19 2016 13:12:34 GMT-0500"));
      context.close();
    }
    {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setLocale("en-US").setTimezoneId("Pacific/Honolulu"));
      Page page = context.newPage();
      assertTrue(((String) page.evaluate(func)).contains("Sat Nov 19 2016 08:12:34 GMT-1000"));
      context.close();
    }
    {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setLocale("en-US").setTimezoneId(browserName == BrowserName.FIREFOX ? "America/Argentina/Buenos_Aires" : "America/Buenos_Aires"));
      Page page = context.newPage();
      assertTrue(((String) page.evaluate(func)).contains("Sat Nov 19 2016 15:12:34 GMT-0300"));
      context.close();
    }
    {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setLocale("en-US").setTimezoneId("Europe/Berlin"));
      Page page = context.newPage();
      assertTrue(((String) page.evaluate(func)).contains("Sat Nov 19 2016 19:12:34 GMT+0100"));
      context.close();
    }
  }

  @PlaywrightTest
  @DisplayName("TZ-002 should throw for invalid timezone IDs when creating pages")
  void shouldThrowForInvalidTimezoneIDsWhenCreatingPages(Browser browser, BrowserName browserName) {
    for (String timezoneId : asList("Foo/Bar", "Baz/Qux")) {
      if (Env.isBidi()) {
        PlaywrightException error = assertThrows(PlaywrightException.class, () -> browser.newContext(new Browser.NewContextOptions().setTimezoneId(timezoneId)));
        if (browserName == BrowserName.CHROMIUM)
          assertTrue(error.getMessage().contains("Invalid timezone \"" + timezoneId + "\""));
        else if (browserName == BrowserName.FIREFOX)
          assertTrue(error.getMessage().contains("Expected \"timezone\" to be a valid timezone ID (e.g., \"Europe/Berlin\") or a valid timezone offset (e.g., \"+01:00\"), got " + timezoneId));
      } else {
        PlaywrightException error = null;
        BrowserContext context = null;
        try {
          context = browser.newContext(new Browser.NewContextOptions().setTimezoneId(timezoneId));
          context.newPage();
        } catch (PlaywrightException e) {
          error = e;
        }
        assertTrue(error.getMessage().contains("Invalid timezone ID: " + timezoneId));
        if (context != null)
          context.close();
      }
    }
  }

  @PlaywrightTest
  @DisplayName("TZ-003 should work for multiple pages sharing same process")
  void shouldWorkForMultiplePagesSharingSameProcess(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setTimezoneId("Europe/Moscow"));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    Page popup = page.waitForPopup(() -> page.evaluate("url => { window.open(url); }", server.EMPTY_PAGE));
    Page popup2 = popup.waitForPopup(() -> popup.evaluate("url => { window.open(url); }", server.EMPTY_PAGE));
    context.close();
  }

  private static Object getContextTimezone(BrowserContext context) {
    Page page = context.newPage();
    return page.evaluate("() => Intl.DateTimeFormat().resolvedOptions().timeZone");
  }

  @PlaywrightTest
  @DisplayName("TZ-004 should not change default timezone in another context")
  void shouldNotChangeDefaultTimezoneInAnotherContext(Browser browser, TestServer server) {
    Object defaultTimezone;
    {
      BrowserContext context = browser.newContext();
      defaultTimezone = getContextTimezone(context);
      context.close();
    }
    String timezoneOverride = "Europe/Moscow".equals(defaultTimezone) ? "America/Los_Angeles" : "Europe/Moscow";
    {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setTimezoneId(timezoneOverride));
      assertEquals(timezoneOverride, getContextTimezone(context));
      context.close();
    }
    {
      BrowserContext context = browser.newContext();
      assertEquals(defaultTimezone, getContextTimezone(context));
      context.close();
    }
  }

  @PlaywrightTest
  @DisplayName("TZ-005 should affect Intl.DateTimeFormat().resolvedOptions().timeZone")
  void shouldAffectIntlDateTimeFormatResolvedOptionsTimeZone(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setTimezoneId("America/Jamaica"));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    assertEquals("America/Jamaica", page.evaluate("() => (new Intl.DateTimeFormat()).resolvedOptions().timeZone"));
    context.close();
  }

  @PlaywrightTest
  @DisplayName("TZ-006 should propagate timezone to workers")
  void shouldPropagateTimezoneToWorkers(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setTimezoneId("America/Jamaica"));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    // Java: only a log message, Chrome on Windows can log the 404 error of /favicon.ico first.
    ConsoleMessage msg = page.waitForConsoleMessage(new Page.WaitForConsoleMessageOptions().setPredicate(m -> "log".equals(m.type())), () ->
      page.evaluate("() => new Worker(URL.createObjectURL(new Blob(['console.log(Intl.DateTimeFormat().resolvedOptions().timeZone)'], { type: 'application/javascript' })))"));
    assertEquals("America/Jamaica", msg.text());
    context.close();
  }
}
