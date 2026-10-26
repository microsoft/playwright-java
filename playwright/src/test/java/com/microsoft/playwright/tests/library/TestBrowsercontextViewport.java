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
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.ContextFactory;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.ViewportSize;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;
import java.util.function.Supplier;

import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static com.microsoft.playwright.fixtures.Utils.verifyViewport;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/browsercontext-viewport.spec.ts. */
public class TestBrowsercontextViewport {
  @PlaywrightTest
  @DisplayName("VP-001 should get the proper default viewport size")
  void shouldGetTheProperDefaultViewportSize(Page page, TestServer server) {
    verifyViewport(page, 1280, 720);
  }

  @PlaywrightTest
  @DisplayName("VP-002 should set the proper viewport size")
  void shouldSetTheProperViewportSize(Page page, TestServer server) {
    verifyViewport(page, 1280, 720);
    page.setViewportSize(345, 456);
    verifyViewport(page, 345, 456);
  }

  @PlaywrightTest
  @DisplayName("VP-003 should return correct outerWidth and outerHeight")
  void shouldReturnCorrectOuterWidthAndOuterHeight(Page page) {
    page.setViewportSize(410, 420);
    Map<String, Object> size = (Map<String, Object>) page.evaluate("() => {\n" +
      "    return {\n" +
      "      innerWidth: window.innerWidth,\n" +
      "      innerHeight: window.innerHeight,\n" +
      "      outerWidth: window.outerWidth,\n" +
      "      outerHeight: window.outerHeight,\n" +
      "    };\n" +
      "  }");
    assertEquals(410, size.get("innerWidth"));
    assertEquals(420, size.get("innerHeight"));
    assertTrue((int) size.get("outerWidth") >= (int) size.get("innerWidth"));
    assertTrue((int) size.get("outerHeight") >= (int) size.get("innerHeight"));
  }

  @PlaywrightTest
  @DisplayName("VP-005 should emulate device width")
  void shouldEmulateDeviceWidth(Page page, TestServer server) {
    assertEquals(1280, page.viewportSize().width);
    assertEquals(720, page.viewportSize().height);
    page.setViewportSize(300, 300);
    assertEquals(300, page.evaluate("() => window.screen.width"));
    assertEquals(true, page.evaluate("() => matchMedia('(min-device-width: 200px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(min-device-width: 400px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(max-device-width: 200px)').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('(max-device-width: 400px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(device-width: 600px)').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('(device-width: 300px)').matches"));
    page.setViewportSize(500, 500);
    assertEquals(500, page.evaluate("() => window.screen.width"));
    assertEquals(true, page.evaluate("() => matchMedia('(min-device-width: 400px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(min-device-width: 600px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(max-device-width: 400px)').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('(max-device-width: 600px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(device-width: 200px)').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('(device-width: 500px)').matches"));
  }

  @PlaywrightTest
  @DisplayName("VP-006 should emulate device height")
  void shouldEmulateDeviceHeight(Page page, TestServer server) {
    assertEquals(1280, page.viewportSize().width);
    assertEquals(720, page.viewportSize().height);
    page.setViewportSize(300, 300);
    assertEquals(300, page.evaluate("() => window.screen.height"));
    assertEquals(true, page.evaluate("() => matchMedia('(min-device-height: 200px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(min-device-height: 400px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(max-device-height: 200px)').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('(max-device-height: 400px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(device-height: 600px)').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('(device-height: 300px)').matches"));
    page.setViewportSize(500, 500);
    assertEquals(500, page.evaluate("() => window.screen.height"));
    assertEquals(true, page.evaluate("() => matchMedia('(min-device-height: 400px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(min-device-height: 600px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(max-device-height: 400px)').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('(max-device-height: 600px)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(device-height: 200px)').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('(device-height: 500px)').matches"));
  }

  @PlaywrightTest
  @DisplayName("VP-007 should emulate availWidth and availHeight")
  void shouldEmulateAvailWidthAndAvailHeight(Page page) {
    page.setViewportSize(500, 600);
    assertEquals(500, page.evaluate("() => window.screen.availWidth"));
    assertEquals(600, page.evaluate("() => window.screen.availHeight"));
  }

  @PlaywrightTest
  @DisplayName("VP-008 should not have touch by default")
  void shouldNotHaveTouchByDefault(Page page, TestServer server, BrowserName browserName) {
    page.navigate(server.PREFIX + "/mobile.html");
    assertEquals(false, page.evaluate("() => 'ontouchstart' in window"));
  }

  @PlaywrightTest
  @DisplayName("VP-009 should throw on tap if hasTouch is not enabled")
  void shouldThrowOnTapIfHasTouchIsNotEnabled(Page page) {
    page.setContent("<div>a</div>");
    {
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.tap("div"));
      assertTrue(error.getMessage().contains("The page does not support tap"));
    }
    {
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.locator("div").tap());
      assertTrue(error.getMessage().contains("The page does not support tap"));
    }
  }

  @PlaywrightTest
  @DisplayName("VP-010 should support touch with null viewport")
  void shouldSupportTouchWithNullViewport(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize((ViewportSize) null).setHasTouch(true));
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/mobile.html");
    assertEquals(true, page.evaluate("() => 'ontouchstart' in window"));
    context.close();
  }

  @PlaywrightTest
  @DisplayName("VP-011 should keep touch emulation after screenshot beyond viewport")
  void shouldKeepTouchEmulationAfterScreenshotBeyondViewport(Browser browser, TestServer server) {
    // { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/42607' }
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(393, 727).setHasTouch(true));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<div id=\"tall\" style=\"height:3000px\">tall</div>");
    Supplier<Object> probe = () -> page.evaluate("() => ({\n" +
      "    ontouchstart: 'ontouchstart' in window,\n" +
      "    maxTouchPoints: navigator.maxTouchPoints,\n" +
      "    coarse: matchMedia('(pointer: coarse)').matches,\n" +
      "  })");
    Map<String, Object> expected = (Map<String, Object>) probe.get();
    assertEquals(true, expected.get("ontouchstart"));
    page.screenshot(new Page.ScreenshotOptions().setFullPage(true));
    assertEquals(expected, probe.get());
    page.locator("#tall").screenshot();
    assertEquals(expected, probe.get());
    page.navigate(server.EMPTY_PAGE);
    assertEquals(expected, probe.get());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("VP-012 should set both screen and viewport options")
  @Fixme("java: newContext() sends the screenSize option under the key 'screenSize' while the protocol expects 'screen', so the option is ignored (screen is 1000x600 in chromium and webkit, 1366x768 in firefox instead of 1280x720)")
  void shouldSetBothScreenAndViewportOptions(ContextFactory contextFactory, BrowserName browserName) {
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions()
      .setScreenSize(1280, 720)
      .setViewportSize(1000, 600));
    Page page = context.newPage();
    Object screen = page.evaluate("() => ({ w: screen.width, h: screen.height })");
    assertEquals(mapOf("w", 1280, "h", 720), screen);
    Object inner = page.evaluate("() => ({ w: window.innerWidth, h: window.innerHeight })");
    assertEquals(mapOf("w", 1000, "h", 600), inner);
  }

  @PlaywrightTest
  @DisplayName("VP-013 should report null viewportSize when given null viewport")
  void shouldReportNullViewportSizeWhenGivenNullViewport(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize((ViewportSize) null));
    Page page = context.newPage();
    assertNull(page.viewportSize());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("VP-014 should drag with high dpi")
  @DisabledOnBrowser(value = {BrowserName.CHROMIUM, BrowserName.FIREFOX, BrowserName.WEBKIT}, headed = true, reason = "Stray mouse events mess up the test")
  void shouldDragWithHighDpi(Browser browser, TestServer server) {
    Page page = browser.newPage(new Browser.NewPageOptions().setDeviceScaleFactor(2));
    page.navigate(server.PREFIX + "/drag-n-drop.html");
    page.hover("#source");
    page.mouse().down();
    page.hover("#target");
    page.mouse().up();
    assertEquals(true, page.evalOnSelector("#target", "target => target.contains(document.querySelector('#source'))")); // could not find source in target
    page.close();
  }

  @PlaywrightTest
  @DisplayName("VP-015 WebKit Windows headed should have a minimal viewport")
  void webKitWindowsHeadedShouldHaveAMinimalViewport(ContextFactory contextFactory, BrowserName browserName) {
    assumeFalse(browserName != BrowserName.WEBKIT || !Env.headed() || !Env.isWindows, "Not relevant for this browser");

    PlaywrightException error = assertThrows(PlaywrightException.class, () -> contextFactory.newContext(new Browser.NewContextOptions()
      .setViewportSize(100, 100)));
    assertTrue(error.getMessage().contains("WebKit on Windows has a minimal viewport of 250x240."));

    BrowserContext context = contextFactory.newContext();
    Page page = context.newPage();
    PlaywrightException error2 = assertThrows(PlaywrightException.class, () -> page.setViewportSize(100, 100));
    assertTrue(error2.getMessage().contains("WebKit on Windows has a minimal viewport of 250x240."));
    context.close();
  }

  @PlaywrightTest
  @DisplayName("VP-016 should be able to get correct orientation angle on non-mobile devices")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "Desktop webkit dont support orientation API")
  void shouldBeAbleToGetCorrectOrientationAngleOnNonMobileDevices(Browser browser, BrowserName browserName, TestServer server) {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(300, 400).setIsMobile(false));
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/index.html");
    assertEquals(0, page.evaluate("() => window.screen.orientation.angle"));
    page.setViewportSize(400, 300);
    assertEquals(0, page.evaluate("() => window.screen.orientation.angle"));
    context.close();
  }
}
