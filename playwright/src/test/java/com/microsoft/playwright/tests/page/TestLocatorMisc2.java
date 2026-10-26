/*
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

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestInfo;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.BoundingBox;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.CompletableFuture;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.async;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/locator-misc-2.spec.ts. */
public class TestLocatorMisc2 {
  /** Java: BoundingBox has no equals(), this is {@code expect(box).toEqual({ x, y, width, height })}. */
  private static void assertBoundingBox(double x, double y, double width, double height, BoundingBox box) {
    assertEquals(x, box.x);
    assertEquals(y, box.y);
    assertEquals(width, box.width);
    assertEquals(height, box.height);
  }

  @PlaywrightTest
  @DisplayName("LO-140 should press @smoke")
  void shouldPress(Page page) {
    page.setContent("<input type='text' />");
    page.locator("input").press("h");
    assertEquals("h", page.evalOnSelector("input", "input => input.value"));
  }

  @PlaywrightTest
  @DisplayName("LO-141 should scroll into view")
  void shouldScrollIntoView(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/offscreenbuttons.html");
    for (int i = 0; i < 11; ++i) {
      Locator button = page.locator("#btn" + i);
      Object before = button.evaluate("button => {\n" +
        "  return button.getBoundingClientRect().right - window.innerWidth;\n" +
        "}");
      assertEquals(10 * i, before);
      button.scrollIntoViewIfNeeded();
      Object after = button.evaluate("button => {\n" +
        "  return button.getBoundingClientRect().right - window.innerWidth;\n" +
        "}");
      assertEquals(true, ((Number) after).doubleValue() <= 0);
      page.evaluate("() => window.scrollTo(0, 0)");
    }
  }

  @PlaywrightTest
  @DisplayName("LO-142 should scroll zero-sized element into view")
  void shouldScrollZeroSizedElementIntoView(Page page) {
    page.setContent("\n" +
      "    <style>\n" +
      "      html,body { margin: 0; padding: 0; }\n" +
      "      ::-webkit-scrollbar { display: none; }\n" +
      "      * { scrollbar-width: none; }\n" +
      "    </style>\n" +
      "    <div style=\"height: 2000px; text-align: center; border: 10px solid blue;\">\n" +
      "      <h1>SCROLL DOWN</h1>\n" +
      "    </div>\n" +
      "    <div id=lazyload style=\"font-size:75px; background-color: green;\"></div>\n" +
      "    <script>\n" +
      "      const lazyLoadElement = document.querySelector('#lazyload');\n" +
      "      const observer = new IntersectionObserver((entries) => {\n" +
      "        if (entries.some(entry => entry.isIntersecting)) {\n" +
      "          lazyLoadElement.textContent = 'LAZY LOADED CONTENT';\n" +
      "          lazyLoadElement.style.height = '20px';\n" +
      "          observer.disconnect();\n" +
      "        }\n" +
      "      });\n" +
      "      observer.observe(lazyLoadElement);\n" +
      "    </script>\n" +
      "  ");
    assertBoundingBox(0, 2020, 1280, 0, page.locator("#lazyload").boundingBox());
    page.locator("#lazyload").scrollIntoViewIfNeeded();
    assertThat(page.locator("#lazyload")).hasText("LAZY LOADED CONTENT");
    assertBoundingBox(0, 720, 1280, 20, page.locator("#lazyload").boundingBox());
  }

  @PlaywrightTest
  @DisplayName("LO-143 should select textarea")
  void shouldSelectTextarea(Page page, TestServer server, BrowserName browserName) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    Locator textarea = page.locator("textarea");
    textarea.evaluate("textarea => textarea.value = 'some value'");
    textarea.selectText();
    assertEquals("some value", page.evaluate("() => window.getSelection().toString()"));
  }

  @PlaywrightTest
  @DisplayName("LO-144 should type")
  void shouldType(Page page) {
    page.setContent("<input type='text' />");
    page.locator("input").type("hello");
    assertEquals("hello", page.evalOnSelector("input", "input => input.value"));
  }

  @PlaywrightTest
  @DisplayName("LO-145 should pressSequentially")
  void shouldPressSequentially(Page page) {
    page.setContent("<input type='text' />");
    page.locator("input").pressSequentially("hello");
    assertEquals("hello", page.evalOnSelector("input", "input => input.value"));
  }

  @PlaywrightTest
  @DisplayName("LO-146 should take screenshot")
  @DisabledOnBrowser(value = BrowserName.FIREFOX, headed = true)
  void shouldTakeScreenshot(Page page, TestServer server, BrowserName browserName, TestInfo testInfo) {
    page.setViewportSize(500, 500);
    page.navigate(server.PREFIX + "/grid.html");
    page.evaluate("() => window.scrollBy(50, 100)");
    Locator element = page.locator(".box:nth-of-type(3)");
    byte[] screenshot = element.screenshot();
    testInfo.expectToMatchSnapshot(screenshot, "screenshot-element-bounding-box.png");
  }

  @PlaywrightTest
  @DisplayName("LO-147 should return bounding box")
  void shouldReturnBoundingBox(Page page, TestServer server, BrowserName browserName) {
    page.setViewportSize(500, 500);
    page.navigate(server.PREFIX + "/grid.html");
    Locator element = page.locator(".box:nth-of-type(13)");
    BoundingBox box = element.boundingBox();
    assertBoundingBox(100, 50, 50, 50, box);
  }

  @PlaywrightTest
  @DisplayName("LO-148 should waitFor")
  void shouldWaitFor(Page page) throws Exception {
    page.setContent("<div></div>");
    Locator locator = page.locator("span");
    CompletableFuture<Void> promise = async(() -> {
      locator.waitFor();
      return null;
    });
    page.evalOnSelector("div", "div => div.innerHTML = '<span>target</span>'");
    promise.get();
    assertThat(locator).hasText("target");
  }

  @PlaywrightTest
  @DisplayName("LO-149 should waitFor hidden")
  void shouldWaitForHidden(Page page) throws Exception {
    page.setContent("<div><span>target</span></div>");
    Locator locator = page.locator("span");
    CompletableFuture<Void> promise = async(() -> {
      locator.waitFor(new Locator.WaitForOptions().setState(com.microsoft.playwright.options.WaitForSelectorState.HIDDEN));
      return null;
    });
    page.evalOnSelector("div", "div => div.innerHTML = ''");
    promise.get();
  }

  @PlaywrightTest
  @DisplayName("LO-150 should combine visible with other selectors")
  void shouldCombineVisibleWithOtherSelectors(Page page) {
    page.setContent("<div>\n" +
      "  <div class=\"item\" style=\"display: none\">Hidden data0</div>\n" +
      "  <div class=\"item\">visible data1</div>\n" +
      "  <div class=\"item\" style=\"display: none\">Hidden data1</div>\n" +
      "  <div class=\"item\">visible data2</div>\n" +
      "  <div class=\"item\" style=\"display: none\">Hidden data1</div>\n" +
      "  <div class=\"item\">visible data3</div>\n" +
      "  </div>");
    Locator locator = page.locator(".item >> visible=true").nth(1);
    assertThat(locator).hasText("visible data2");
    assertThat(page.locator(".item >> visible=true >> text=data3")).hasText("visible data3");
  }

  @PlaywrightTest
  @DisplayName("LO-151 should support filter(visible)")
  void shouldSupportFilterVisible(Page page) {
    page.setContent("<div>\n" +
      "    <div class=\"item\" style=\"display: none\">Hidden data0</div>\n" +
      "    <div class=\"item\">visible data1</div>\n" +
      "    <div class=\"item\" style=\"display: none\">Hidden data1</div>\n" +
      "    <div class=\"item\">visible data2</div>\n" +
      "    <div class=\"item\" style=\"display: none\">Hidden data2</div>\n" +
      "    <div class=\"item\">visible data3</div>\n" +
      "    </div>\n" +
      "  ");
    Locator locator = page.locator(".item").filter(new Locator.FilterOptions().setVisible(true)).nth(1);
    assertThat(locator).hasText("visible data2");
    assertThat(page.locator(".item").filter(new Locator.FilterOptions().setVisible(true)).getByText("data3")).hasText("visible data3");
    assertThat(page.locator(".item").filter(new Locator.FilterOptions().setVisible(false)).getByText("data1")).hasText("Hidden data1");
  }

  @PlaywrightTest
  @DisplayName("LO-152 should support visible()")
  void shouldSupportVisible(Page page) {
    page.setContent("<div>\n" +
      "    <div class=\"item\" style=\"display: none\">Hidden data0</div>\n" +
      "    <div class=\"item\">visible data1</div>\n" +
      "    <div class=\"item\" style=\"display: none\">Hidden data1</div>\n" +
      "    <div class=\"item\">visible data2</div>\n" +
      "    <div class=\"item\" style=\"display: none\">Hidden data2</div>\n" +
      "    <div class=\"item\">visible data3</div>\n" +
      "    </div>\n" +
      "  ");
    Locator locator = page.locator(".item").visible().nth(1);
    assertThat(locator).hasText("visible data2");
    assertThat(page.locator(".item").visible().getByText("data3")).hasText("visible data3");
    assertThat(page.locator(".item").visible()).hasCount(3);
  }

  @PlaywrightTest
  @DisplayName("LO-153 locator.count should work with deleted Map in main world")
  void locatorCountShouldWorkWithDeletedMapInMainWorld(Page page) {
    page.evaluate("Map = 1");
    page.locator("#searchResultTableDiv .x-grid3-row").count();
    assertThat(page.locator("#searchResultTableDiv .x-grid3-row")).hasCount(0);
  }

  @PlaywrightTest
  @DisplayName("LO-154 Locator.locator() and FrameLocator.locator() should accept locator")
  void locatorLocatorAndFrameLocatorLocatorShouldAcceptLocator(Page page) {
    page.setContent("\n" +
      "    <div><input value=outer></div>\n" +
      "    <iframe srcdoc=\"<div><input value=inner></div>\"></iframe>\n" +
      "  ");

    Locator inputLocator = page.locator("input");
    assertEquals("outer", inputLocator.inputValue());
    assertEquals("outer", page.locator("div").locator(inputLocator).inputValue());
    assertEquals("inner", page.frameLocator("iframe").locator(inputLocator).inputValue());
    assertEquals("inner", page.frameLocator("iframe").locator("div").locator(inputLocator).inputValue());

    Locator divLocator = page.locator("div");
    assertEquals("outer", divLocator.locator("input").inputValue());
    assertEquals("inner", page.frameLocator("iframe").locator(divLocator).locator("input").inputValue());
  }

  @PlaywrightTest
  @DisplayName("LO-155 should fill programmatically enabled textarea")
  void shouldFillProgrammaticallyEnabledTextarea(Page page) {
    page.setContent("\n" +
      "    <button>Enable</button>\n" +
      "    <form>\n" +
      "      <textarea id=\"text\" disabled></textarea>\n" +
      "    </form>\n" +
      "    <script>\n" +
      "      document.querySelector('button').addEventListener('click', () => {\n" +
      "        document.querySelector('#text').disabled = false;\n" +
      "      });\n" +
      "    </script>\n" +
      "  ");
    page.locator("button").click();
    page.locator("#text").fill("Hello");
    assertThat(page.locator("#text")).hasValue("Hello");
  }

  @PlaywrightTest
  @DisplayName("LO-156 press should throw on unknown keys")
  void pressShouldThrowOnUnknownKeys(Page page, TestServer server) {
    page.setContent("<input type='text' value='hello' />");
    Locator locator = page.getByRole(AriaRole.TEXTBOX);
    PlaywrightException error1 = assertThrows(PlaywrightException.class, () -> locator.press("NotARealKey"));
    assertTrue(error1.getMessage().contains("Unknown key: \"NotARealKey\""));
    PlaywrightException error2 = assertThrows(PlaywrightException.class, () -> locator.press("ё"));
    assertTrue(error2.getMessage().contains("Unknown key: \"ё\""));
    PlaywrightException error3 = assertThrows(PlaywrightException.class, () -> locator.press("😊"));
    assertTrue(error3.getMessage().contains("Unknown key: \"😊\""));
  }
}
