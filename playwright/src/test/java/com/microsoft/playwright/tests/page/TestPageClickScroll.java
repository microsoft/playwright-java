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

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.Position;
import com.microsoft.playwright.options.ScrollMode;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static com.microsoft.playwright.fixtures.Utils.browserMajorVersion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/page/page-click-scroll.spec.ts. */
public class TestPageClickScroll {
  @PlaywrightTest
  @DisplayName("CL-094 should not hit scroll bar")
  void shouldNotHitScrollBar(Page page) {
    page.setContent("\n" +
      "    <style>\n" +
      "      .categories { width: 180px; display: flex; overflow-x: scroll; }\n" +
      "      button { flex: none; height: 28px; }\n" +
      "    </style>\n" +
      "    <div class=\"categories\">\n" +
      "      <button>One</button>\n" +
      "      <button>Two</button>\n" +
      "      <button>Three</button>\n" +
      "      <button>Story</button>\n" +
      "      <button>More</button>\n" +
      "      <button>Items</button>\n" +
      "      <button>Here</button>\n" +
      "    </div>\n" +
      "    ");
    page.click("text=Story", new Page.ClickOptions().setTimeout(2000));
  }

  @PlaywrightTest
  @DisplayName("CL-095 should scroll into view display:contents")
  void shouldScrollIntoViewDisplayContents(Page page, Browser browser, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.CHROMIUM && browserMajorVersion(browser) < 105, "Needs https://chromium-review.googlesource.com/c/chromium/src/+/3758670");

    page.setContent("\n" +
      "    <div style=\"background:red;height:2000px\">filler</div>\n" +
      "    <div>\n" +
      "      Example text, and button here:\n" +
      "      <button style=\"display: contents\" onclick=\"window._clicked=true;\">click me</button>\n" +
      "    </div>\n" +
      "  ");
    page.click("text=click me", new Page.ClickOptions().setTimeout(5000));
    assertEquals(true, page.evaluate("window._clicked"));
  }

  @PlaywrightTest
  @DisplayName("CL-096 should scroll into view display:contents with a child")
  void shouldScrollIntoViewDisplayContentsWithAChild(Page page, Browser browser, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.CHROMIUM && browserMajorVersion(browser) < 105, "Needs https://chromium-review.googlesource.com/c/chromium/src/+/3758670");

    page.setContent("\n" +
      "    <div style=\"background:red;height:2000px\">filler</div>\n" +
      "    Example text, and button here:\n" +
      "    <button style=\"display: contents\" onclick=\"window._clicked=true;\"><div>click me</div></button>\n" +
      "  ");
    page.click("text=click me", new Page.ClickOptions().setTimeout(5000));
    assertEquals(true, page.evaluate("window._clicked"));
  }

  @PlaywrightTest
  @DisplayName("CL-097 should scroll into view display:contents with position")
  @DisabledOnBrowser(value = BrowserName.CHROMIUM, reason = "fixme: DOM.getBoxModel does not work for display:contents")
  void shouldScrollIntoViewDisplayContentsWithPosition(Page page) {
    page.setContent("\n" +
      "    <div style=\"background:red;height:2000px\">filler</div>\n" +
      "    <div>\n" +
      "      Example text, and button here:\n" +
      "      <button style=\"display: contents\" onclick=\"window._clicked=true;\">click me</button>\n" +
      "    </div>\n" +
      "  ");
    page.click("text=click me", new Page.ClickOptions().setPosition(5, 5).setTimeout(5000));
    assertEquals(true, page.evaluate("window._clicked"));
  }

  @PlaywrightTest
  @DisplayName("CL-098 should not crash when force-clicking hidden input")
  void shouldNotCrashWhenForceClickingHiddenInput(Page page) {
    page.setContent("<input type=hidden>");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.locator("input").click(new Locator.ClickOptions().setForce(true).setTimeout(2000)));
    assertTrue(error.getMessage().contains("Element is not visible"));
  }

  @PlaywrightTest
  @DisplayName("CL-099 should scroll into view span element")
  void shouldScrollIntoViewSpanElement(Page page) {
    page.setContent("\n" +
      "    <div id=big style=\"height: 10000px;\"></div>\n" +
      "    <span id=small>foo</span>\n" +
      "  ");
    page.locator("#small").scrollIntoViewIfNeeded();
    assertTrue(((Number) page.evaluate("() => window.scrollY")).doubleValue() > 9000);
  }

  @PlaywrightTest
  @DisplayName("CL-100 should scroll into view element in iframe")
  void shouldScrollIntoViewElementInIframe(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("\n" +
      "    <div id=big style=\"height: 10000px;\"></div>\n" +
      "    <iframe src='" + server.CROSS_PROCESS_PREFIX + "/input/button.html'></iframe>\n" +
      "  ");
    page.frameLocator("iframe").getByRole(AriaRole.BUTTON).click(new Locator.ClickOptions().setTimeout(5000));
  }

  @PlaywrightTest
  @DisplayName("CL-101 should not scroll the page when scroll is \"none\"")
  void shouldNotScrollThePageWhenScrollIsNone(Page page) {
    page.setContent("\n" +
      "    <div style=\"height: 2000px;\"></div>\n" +
      "    <button onclick=\"window._clicked=true\">click me</button>\n" +
      "  ");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.locator("button").click(new Locator.ClickOptions().setScroll(ScrollMode.NONE).setTimeout(2000)));
    assertTrue(error.getMessage().contains("element is outside of the viewport"));
    // Java: toBeFalsy() on a value that is undefined in the page.
    assertTrue(isFalsy(page.evaluate("window._clicked")));
    assertEquals(0, page.evaluate("() => window.scrollY"));
  }

  @PlaywrightTest
  @DisplayName("CL-102 should click in-viewport element when scroll is \"none\"")
  void shouldClickInViewportElementWhenScrollIsNone(Page page) {
    page.setContent("\n" +
      "    <button onclick=\"window._clicked=true\">click me</button>\n" +
      "    <div style=\"height: 2000px;\"></div>\n" +
      "  ");
    page.locator("button").click(new Locator.ClickOptions().setScroll(ScrollMode.NONE).setTimeout(2000));
    assertEquals(true, page.evaluate("window._clicked"));
    assertEquals(0, page.evaluate("() => window.scrollY"));
  }

  @PlaywrightTest
  @DisplayName("CL-103 should not scroll nested container when scroll is \"none\"")
  void shouldNotScrollNestedContainerWhenScrollIsNone(Page page) {
    page.setContent("\n" +
      "    <div style=\"height: 100px; width: 100px; overflow-y: scroll;\">\n" +
      "      <div style=\"height: 50px;\">A</div>\n" +
      "      <div style=\"height: 50px;\">B</div>\n" +
      "      <button style=\"height: 50px; width: 100px;\" onclick=\"window._clicked=true\">C</button>\n" +
      "    </div>\n" +
      "  ");
    Locator button = page.locator("button");
    // The button is scrolled out of the nested overflow container.
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> button.click(new Locator.ClickOptions().setScroll(ScrollMode.NONE).setTimeout(2000)));
    assertNotNull(error);
    // Java: toBeFalsy() on a value that is undefined in the page.
    assertTrue(isFalsy(page.evaluate("window._clicked")));
    // Default behavior scrolls the nested container into view and succeeds.
    button.click(new Locator.ClickOptions().setTimeout(2000));
    assertEquals(true, page.evaluate("window._clicked"));
  }

  @PlaywrightTest
  @DisplayName("CL-104 should not scroll on hover when scroll is \"none\"")
  void shouldNotScrollOnHoverWhenScrollIsNone(Page page) {
    page.setContent("\n" +
      "    <div style=\"height: 2000px;\"></div>\n" +
      "    <div onmouseover=\"window._hovered=true\" style=\"width: 50px; height: 50px;\">hover me</div>\n" +
      "  ");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.locator("div >> text=hover me").hover(new Locator.HoverOptions().setScroll(ScrollMode.NONE).setTimeout(2000)));
    assertTrue(error.getMessage().contains("element is outside of the viewport"));
    // Java: toBeFalsy() on a value that is undefined in the page.
    assertTrue(isFalsy(page.evaluate("window._hovered")));
    assertEquals(0, page.evaluate("() => window.scrollY"));
  }

  @PlaywrightTest
  @DisplayName("CL-105 should scroll instantly on retry when scroll-behavior is smooth")
  void shouldScrollInstantlyOnRetryWhenScrollBehaviorIsSmooth(Page page) {
    // The fixed header covers everything but the bottom strip of the viewport.
    // The first attempt centers the button under the header, so the click is
    // retried with a different scroll alignment using the native scrollIntoView().
    page.setContent("\n" +
      "    <style>\n" +
      "      html { scroll-behavior: smooth; }\n" +
      "      body { margin: 0; }\n" +
      "      .spacer { height: 2000px; }\n" +
      "      #header { position: fixed; top: 0; left: 0; right: 0; bottom: 50px; background: rgba(0, 0, 0, 0.1); }\n" +
      "      button { height: 30px; }\n" +
      "    </style>\n" +
      "    <div id=\"header\"></div>\n" +
      "    <div class=\"spacer\"></div>\n" +
      "    <button onclick=\"window.clicked = true\">Target</button>\n" +
      "    <div class=\"spacer\"></div>\n" +
      "    <script>\n" +
      "      window.scrolls = [];\n" +
      "      addEventListener('scroll', () => window.scrolls.push(window.scrollY));\n" +
      "    </script>\n" +
      "  ");
    page.click("button");
    assertEquals(true, page.evaluate("window.clicked"));
    // Every scroll performed by Playwright should be a single instant jump,
    // and never a smooth animation that fires a scroll event per frame.
    List<?> scrolls = (List<?>) page.evaluate("() => window['scrolls']");
    assertTrue(scrolls.size() <= 2);
  }

  // Java: the JavaScript truthiness of a value that comes from the page.
  private static boolean isFalsy(Object value) {
    return value == null || Boolean.FALSE.equals(value) || (value instanceof Number && ((Number) value).doubleValue() == 0) || "".equals(value);
  }
}
