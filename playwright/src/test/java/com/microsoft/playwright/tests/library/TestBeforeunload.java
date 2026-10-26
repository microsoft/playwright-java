/*
 * Copyright 2017 Google Inc. All rights reserved.
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
import com.microsoft.playwright.Dialog;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.DisplayName;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/library/beforeunload.spec.ts. */
public class TestBeforeunload {
  // Java: stands for Promise.all([page.waitForEvent('dialog'), action]); the dialog is delivered while waiting.
  private static Dialog waitForDialog(Page page, Runnable action) {
    Dialog[] dialog = {null};
    page.onceDialog(d -> dialog[0] = d);
    action.run();
    page.waitForCondition(() -> dialog[0] != null);
    return dialog[0];
  }

  @PlaywrightTest
  @DisplayName("BU-001 should close browser with beforeunload page")
  void shouldCloseBrowserWithBeforeunloadPage(TestServer server, BrowserType browserType) {
    Browser browser = browserType.launch(Env.launchOptions());
    Page page = browser.newPage();
    page.navigate(server.PREFIX + "/beforeunload.html");
    // We have to interact with a page so that 'beforeunload' handlers
    // fire.
    page.click("body");
    browser.close();
  }

  @PlaywrightTest
  @DisplayName("BU-002 should close browsercontext with beforeunload page")
  void shouldCloseBrowsercontextWithBeforeunloadPage(TestServer server, Page page, BrowserContext context) {
    page.navigate(server.PREFIX + "/beforeunload.html");
    // We have to interact with a page so that 'beforeunload' handlers
    // fire.
    page.click("body");
    context.close();
  }

  @PlaywrightTest
  @DisplayName("BU-003 should be able to navigate away from page with beforeunload")
  void shouldBeAbleToNavigateAwayFromPageWithBeforeunload(TestServer server, Page page, BrowserContext context) {
    page.navigate(server.PREFIX + "/beforeunload.html");
    // We have to interact with a page so that 'beforeunload' handlers
    // fire.
    page.click("body");
    page.navigate(server.EMPTY_PAGE);
  }

  @PlaywrightTest
  @DisplayName("BU-004 should close page with beforeunload listener")
  void shouldClosePageWithBeforeunloadListener(BrowserContext context, TestServer server) {
    Page newPage = context.newPage();
    newPage.navigate(server.PREFIX + "/beforeunload.html");
    // We have to interact with a page so that 'beforeunload' handlers
    // fire.
    newPage.click("body");
    newPage.close();
  }

  @PlaywrightTest
  @DisplayName("BU-005 should run beforeunload if asked for @smoke")
  void shouldRunBeforeunloadIfAskedFor(BrowserContext context, TestServer server, BrowserName browserName) {
    Page newPage = context.newPage();
    newPage.navigate(server.PREFIX + "/beforeunload.html");
    // We have to interact with a page so that 'beforeunload' handlers
    // fire.
    newPage.click("body");
    Dialog dialog = waitForDialog(newPage, () -> newPage.close(new Page.CloseOptions().setRunBeforeUnload(true)));
    assertEquals("beforeunload", dialog.type());
    assertEquals("", dialog.defaultValue());
    if (browserName == BrowserName.CHROMIUM) {
      assertEquals("", dialog.message());
    } else if (browserName == BrowserName.WEBKIT) {
      assertEquals("Leave?", dialog.message());
    } else {
      assertTrue(dialog.message().contains("This page is asking you to confirm that you want to leave"));
    }
    newPage.waitForClose(() -> dialog.accept());
  }

  @PlaywrightTest
  @DisplayName("BU-006 should access page after beforeunload")
  void shouldAccessPageAfterBeforeunload(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/beforeunload.html");
    // We have to interact with a page so that 'beforeunload' handlers
    // fire.
    page.click("body");
    Dialog dialog = waitForDialog(page, () -> page.close(new Page.CloseOptions().setRunBeforeUnload(true)));
    dialog.dismiss();
    page.evaluate("() => document.title");
  }

  @PlaywrightTest
  @DisplayName("BU-007 should not stall on evaluate when dismissing beforeunload")
  void shouldNotStallOnEvaluateWhenDismissingBeforeunload(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/beforeunload.html");
    // We have to interact with a page so that 'beforeunload' handlers fire.
    page.click("body");

    // Java: waitForEvent('dialog').then(dialog => dialog.dismiss()) is a dialog handler.
    page.onceDialog(dialog -> dialog.dismiss());
    page.evaluate("() => {\n" +
      "  window.location.reload();\n" +
      "}");
  }

  @PlaywrightTest
  @DisplayName("BU-008 should not stall on click when dismissing beforeunload")
  void shouldNotStallOnClickWhenDismissingBeforeunload(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a href=\"" + server.PREFIX + "/frames/one-frame.html\">click me</a>");

    page.evaluate("() => {\n" +
      "  window.onbeforeunload = () => false;\n" +
      "}");
    page.onDialog(dialog -> dialog.dismiss());

    page.getByRole(AriaRole.LINK).click(new Locator.ClickOptions().setNoWaitAfter(true));
    page.evaluate("() => {\n" +
      "  window.onbeforeunload = null;\n" +
      "}");

    // This line should not timeout.
    page.getByRole(AriaRole.LINK).click(new Locator.ClickOptions().setTimeout(5000));
    assertThat(page).hasURL(server.PREFIX + "/frames/one-frame.html");
  }

  @PlaywrightTest
  @DisplayName("BU-009 should support dismissing the dialog multiple times")
  void shouldSupportDismissingTheDialogMultipleTimes(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/beforeunload.html");

    // We have to interact with a page so that 'beforeunload' handlers
    // fire (in Firefox).
    page.click("body");

    Dialog dialog = waitForDialog(page, () -> page.close(new Page.CloseOptions().setRunBeforeUnload(true)));

    dialog.dismiss();

    Dialog dialog2 = waitForDialog(page, () -> page.close(new Page.CloseOptions().setRunBeforeUnload(true)));

    dialog2.dismiss();
  }

  @PlaywrightTest
  @DisplayName("BU-010 should support closing the page after a previous dismiss")
  void shouldSupportClosingThePageAfterAPreviousDismiss(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/beforeunload.html");

    // We have to interact with a page so that 'beforeunload' handlers
    // fire (in Firefox).
    page.click("body");

    Dialog dialog = waitForDialog(page, () -> page.close(new Page.CloseOptions().setRunBeforeUnload(true)));

    dialog.dismiss();

    page.close();
    assertEquals(true, page.isClosed());
  }

  @PlaywrightTest
  @DisplayName("BU-011 should support closing the page via a subsequent onbeforeunload dialog")
  void shouldSupportClosingThePageViaASubsequentOnbeforeunloadDialog(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/beforeunload.html");

    // We have to interact with a page so that 'beforeunload' handlers
    // fire (in Firefox).
    page.click("body");

    Dialog dialog = waitForDialog(page, () -> page.close(new Page.CloseOptions().setRunBeforeUnload(true)));

    dialog.accept();

    page.waitForClose(() -> {});
    assertEquals(true, page.isClosed());
  }

  @PlaywrightTest
  @DisplayName("BU-012 does not get stalled by beforeUnload")
  void doesNotGetStalledByBeforeUnload(Page page, TestServer server) {
    page.navigate(server.HELLO_WORLD);

    page.evaluate("() => {\n" +
      "  window.addEventListener('beforeunload', event => {\n" +
      "    event.preventDefault();\n" +
      "  });\n" +
      "}");
    page.onDialog(dialog -> dialog.dismiss());

    // We have to interact with a page so that 'beforeunload' handlers
    // fire.
    page.click("body");

    page.route("**/api", route -> route.fulfill(new Route.FulfillOptions().setStatus(200).setBody("ok")));
    page.evaluate("async () => fetch(new URL('/api', window.location.href))");

    page.close(new Page.CloseOptions().setRunBeforeUnload(true));

    page.evaluate("async () => fetch(new URL('/api', window.location.href))");
  }
}
