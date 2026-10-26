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
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.function.Supplier;

import static com.microsoft.playwright.fixtures.Utils.attachFrame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Port of tests/library/browsercontext-csp.spec.ts. */
public class TestBrowsercontextCsp {
  // Java: expect.poll(() => value).toBe(expected); the value is evaluated in the page, so it polls with waitForTimeout().
  private static void expectPollToBe(Page page, Supplier<Object> value, Object expected) {
    for (int i = 0; i < 100 && !expected.equals(value.get()); i++)
      page.waitForTimeout(100);
    assertEquals(expected, value.get());
  }

  @PlaywrightTest
  @DisplayName("CS-001 should bypass CSP meta tag @smoke")
  void shouldBypassCSPMetaTag(Browser browser, TestServer server) {
    // Make sure CSP prohibits addScriptTag.
    {
      BrowserContext context = browser.newContext();
      Page page = context.newPage();
      page.navigate(server.PREFIX + "/csp.html");
      assertNull(page.evaluate("window[\"__inlineScriptValue\"]"));
      try {
        page.addScriptTag(new Page.AddScriptTagOptions().setContent("window[\"__injected\"] = 42;"));
      } catch (PlaywrightException e) {
        // void e
      }
      assertNull(page.evaluate("window[\"__injected\"]"));
      context.close();
    }

    // By-pass CSP and try one more time.
    {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setBypassCSP(true));
      Page page = context.newPage();
      page.navigate(server.PREFIX + "/csp.html");
      assertEquals(42, page.evaluate("window[\"__inlineScriptValue\"]"));
      page.addScriptTag(new Page.AddScriptTagOptions().setContent("window[\"__injected\"] = 42;"));
      assertEquals(42, page.evaluate("window[\"__injected\"]"));
      context.close();
    }
  }

  @PlaywrightTest
  @DisplayName("CS-002 should bypass CSP header")
  void shouldBypassCSPHeader(Browser browser, TestServer server) {
    // Make sure CSP prohibits addScriptTag.
    server.setRoute("/empty.html", (req, res) -> {
      res.setHeader("Content-Security-Policy", "default-src \"self\"");
      res.setHeader("Content-Type", "text/html");
      res.end("<script type='text/javascript'>window.__inlineScriptValue = 42;</script>");
    });
    {
      BrowserContext context = browser.newContext();
      Page page = context.newPage();
      page.navigate(server.EMPTY_PAGE);
      assertNull(page.evaluate("window[\"__inlineScriptValue\"]"));
      try {
        page.addScriptTag(new Page.AddScriptTagOptions().setContent("window[\"__injected\"] = 42;"));
      } catch (PlaywrightException e) {
        // void e
      }
      assertNull(page.evaluate("window[\"__injected\"]"));
      context.close();
    }

    // By-pass CSP and try one more time.
    {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setBypassCSP(true));
      Page page = context.newPage();
      page.navigate(server.EMPTY_PAGE);
      assertEquals(42, page.evaluate("window[\"__inlineScriptValue\"]"));
      page.addScriptTag(new Page.AddScriptTagOptions().setContent("window[\"__injected\"] = 42;"));
      assertEquals(42, page.evaluate("window[\"__injected\"]"));
      context.close();
    }
  }

  @PlaywrightTest
  @DisplayName("CS-003 should bypass after cross-process navigation")
  void shouldBypassAfterCrossProcessNavigation(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setBypassCSP(true));
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/csp.html");
    assertEquals(42, page.evaluate("window[\"__inlineScriptValue\"]"));
    page.addScriptTag(new Page.AddScriptTagOptions().setContent("window[\"__injected\"] = 42;"));
    assertEquals(42, page.evaluate("window[\"__injected\"]"));

    page.navigate(server.CROSS_PROCESS_PREFIX + "/csp.html");
    assertEquals(42, page.evaluate("window[\"__inlineScriptValue\"]"));
    page.addScriptTag(new Page.AddScriptTagOptions().setContent("window[\"__injected\"] = 42;"));
    assertEquals(42, page.evaluate("window[\"__injected\"]"));
    context.close();
  }

  @PlaywrightTest
  @DisplayName("CS-004 should bypass CSP in iframes as well")
  void shouldBypassCSPInIframesAsWell(Browser browser, TestServer server) {
    // Make sure CSP prohibits addScriptTag in an iframe.
    {
      BrowserContext context = browser.newContext();
      Page page = context.newPage();
      page.navigate(server.EMPTY_PAGE);
      Frame frame = attachFrame(page, "frame1", server.PREFIX + "/csp.html");
      assertNull(frame.evaluate("window[\"__inlineScriptValue\"]"));
      try {
        frame.addScriptTag(new Frame.AddScriptTagOptions().setContent("window[\"__injected\"] = 42;"));
      } catch (PlaywrightException e) {
        // void e
      }
      assertNull(frame.evaluate("window[\"__injected\"]"));
      context.close();
    }

    // By-pass CSP and try one more time.
    {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setBypassCSP(true));
      Page page = context.newPage();
      page.navigate(server.EMPTY_PAGE);
      Frame frame = attachFrame(page, "frame1", server.PREFIX + "/csp.html");
      assertEquals(42, frame.evaluate("window[\"__inlineScriptValue\"]"));
      try {
        frame.addScriptTag(new Frame.AddScriptTagOptions().setContent("window[\"__injected\"] = 42;"));
      } catch (PlaywrightException e) {
        // void e
      }
      assertEquals(42, frame.evaluate("window[\"__injected\"]"));
      context.close();
    }
  }

  @PlaywrightTest
  @DisplayName("CS-005 should not crash on javascript: navigation")
  @Fixme(value = "driver: page.click('#a') on a javascript: link times out in firefox: 'Timeout 30000ms exceeded' while 'waiting for scheduled navigations to finish'; test added by upstream #43457, newer than driver 1.64.1", browsers = BrowserName.FIREFOX)
  void shouldNotCrashOnJavascriptNavigation(Browser browser, TestServer server) {
    // it.info().annotations.push({ type: 'issue', description: 'https://github.com/microsoft/playwright/issues/43213' });
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setBypassCSP(true));
    Page page = context.newPage();
    boolean[] crashed = {false};
    page.onCrash(p -> crashed[0] = true);
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a id=\"a\" href=\"javascript:window.__clicked = true\">click</a>");
    page.click("#a");
    expectPollToBe(page, () -> page.evaluate("() => window['__clicked']"), true);
    assertEquals(false, crashed[0]);
    context.close();
  }
}
