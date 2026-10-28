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
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.ContextFactory;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.BoundingBox;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.regex.Pattern;

import static com.microsoft.playwright.fixtures.Utils.attachFrame;
import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/browsercontext-pages.spec.ts. */
public class TestBrowsercontextPages {
  @PlaywrightTest
  @DisplayName("BC-051 should not be visible in context.pages")
  void shouldNotBeVisibleInContextPages(ContextFactory contextFactory) {
    BrowserContext context = contextFactory.newContext();
    Page page = context.newPage();
    assertTrue(context.pages().contains(page));
    page.close();
    assertFalse(context.pages().contains(page));
  }

  @PlaywrightTest
  @DisplayName("BC-052 page.context should return the correct instance")
  void pageContextShouldReturnTheCorrectInstance(ContextFactory contextFactory) {
    BrowserContext context = contextFactory.newContext();
    Page page = context.newPage();
    assertEquals(context, page.context());
  }

  @PlaywrightTest
  @DisplayName("BC-053 frame.focus should work multiple times")
  void frameFocusShouldWorkMultipleTimes(ContextFactory contextFactory) {
    BrowserContext context = contextFactory.newContext();
    Page page1 = context.newPage();
    Page page2 = context.newPage();
    for (Page page : Arrays.asList(page1, page2)) {
      page.setContent("<button id=\"foo\" onfocus=\"window.gotFocus=true\"></button>");
      page.focus("#foo");
      assertEquals(true, page.evaluate("() => !!(window['gotFocus'])"));
    }
  }

  @PlaywrightTest
  @DisplayName("BC-054 should click with disabled javascript")
  void shouldClickWithDisabledJavascript(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setJavaScriptEnabled(false));
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/wrappedlink.html");
    page.waitForNavigation(() -> page.click("a"));
    assertEquals(server.PREFIX + "/wrappedlink.html#clicked", page.url());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("BC-056 should click the button with deviceScaleFactor set")
  void shouldClickTheButtonWithDeviceScaleFactorSet(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(400, 400).setDeviceScaleFactor(5));
    Page page = context.newPage();
    assertEquals(5, page.evaluate("() => window.devicePixelRatio"));
    page.setContent("<div style=\"width:100px;height:100px\">spacer</div>");
    attachFrame(page, "button-test", server.PREFIX + "/input/button.html");
    Frame frame = page.frames().get(1);
    ElementHandle button = frame.querySelector("button");
    button.click();
    assertEquals("Clicked", frame.evaluate("() => window['result']"));
    context.close();
  }

  @PlaywrightTest
  @DisplayName("BC-057 should click the button with offset with page scale")
  void shouldClickTheButtonWithOffsetWithPageScale(Browser browser, TestServer server, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.FIREFOX && !Env.isBidi());

    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(400, 400).setIsMobile(true));
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/input/button.html");
    page.evalOnSelector("button", "button => {\n" +
      "  button.style.borderWidth = '8px';\n" +
      "  document.body.style.margin = '0';\n" +
      "}");
    page.click("button", new Page.ClickOptions().setPosition(20, 10));
    assertEquals("Clicked", page.evaluate("result"));
    // Expect 20;10 + 8px of border in each direction. Allow some delta as different
    // browsers round up or down differently during css -> dip -> css conversion.
    expectCloseTo(28, ((Number) page.evaluate("pageX")).doubleValue());
    expectCloseTo(18, ((Number) page.evaluate("pageY")).doubleValue());
    context.close();
  }

  private static void expectCloseTo(double expected, double actual) {
    if (Math.abs(expected - actual) > 2) {
      throw new AssertionError("Expected: " + expected + ", received: " + actual);
    }
  }

  @PlaywrightTest
  @DisplayName("BC-058 should return bounding box with page scale")
  void shouldReturnBoundingBoxWithPageScale(Browser browser, TestServer server, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.FIREFOX && !Env.isBidi());

    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(400, 400).setIsMobile(true));
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/input/button.html");
    ElementHandle button = page.querySelector("button");
    button.evaluate("button => {\n" +
      "  document.body.style.margin = '0';\n" +
      "  button.style.borderWidth = '0';\n" +
      "  button.style.width = '200px';\n" +
      "  button.style.height = '20px';\n" +
      "  button.style.marginLeft = '17px';\n" +
      "  button.style.marginTop = '23px';\n" +
      "}");
    BoundingBox box = button.boundingBox();
    assertEquals(17 * 100, Math.round(box.x * 100));
    assertEquals(23 * 100, Math.round(box.y * 100));
    assertEquals(200 * 100, Math.round(box.width * 100));
    assertEquals(20 * 100, Math.round(box.height * 100));
    context.close();
  }

  @PlaywrightTest
  @DisplayName("BC-060 should close page while a reload is committing")
  @DisabledOnBrowser(value = BrowserName.CHROMIUM, reason = "fixme: Chromium loses the close when the reload commits into a new RenderFrameHost; fix is not rolled yet")
  void shouldClosePageWhileAReloadIsCommitting(BrowserContext context) {
    // The evaluate never settles on its own, so it only rejects once the reload
    // commits. Closing right after that races the commit. Repeat a few times,
    // since whether the close or the commit wins is timing-dependent.
    for (int i = 0; i < 10; i++) {
      Page page = context.newPage();
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.evaluate("() => {\n" +
        "  location.reload();\n" +
        "  return new Promise(() => {});\n" +
        "}"));
      assertTrue(Pattern.compile("navigation").matcher(error.getMessage()).find());
      page.close();
    }
  }

  @PlaywrightTest
  @DisplayName("BC-061 should keep selection in multiple pages")
  void shouldKeepSelectionInMultiplePages(BrowserContext context) {
    Page page1 = context.newPage();
    Page page2 = context.newPage();
    for (Page page : Arrays.asList(page1, page2)) {
      page.setContent("<div id=\"container\">lorem ipsum dolor sit amet</div>");
      page.evaluate("() => {\n" +
        "  const element = document.getElementById('container');\n" +
        "  const textNode = element.firstChild;\n" +
        "\n" +
        "  const range = document.createRange();\n" +
        "  range.setStart(textNode, 6);\n" +
        "  range.setEnd(textNode, 11);\n" +
        "\n" +
        "  const selection = document.getSelection();\n" +
        "  selection?.removeAllRanges();\n" +
        "  selection?.addRange(range);\n" +
        "}");
    }
    // In WebKit, selection is updated via IPC, give it enough time to take effect.
    page1.waitForTimeout(1000);
    for (Page page : Arrays.asList(page1, page2)) {
      Object range = page.evaluate("() => {\n" +
        "  const selection = document.getSelection();\n" +
        "  const range = selection?.getRangeAt(0);\n" +
        "  return {\n" +
        "    rangeCount: selection?.rangeCount,\n" +
        "    startOffset: range?.startOffset,\n" +
        "    endOffset: range?.endOffset,\n" +
        "  };\n" +
        "}");
      assertEquals(mapOf("rangeCount", 1, "startOffset", 6, "endOffset", 11), range);
    }
  }
}
