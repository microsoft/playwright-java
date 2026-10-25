/*
 * Copyright Microsoft Corporation. All rights reserved.
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

import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/page/page-focus.spec.ts. */
public class TestPageFocus {
  @PlaywrightTest
  @DisplayName("FO-001 should work @smoke")
  void shouldWork(Page page, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.FIREFOX && !Env.isBidi());

    page.setContent("<div id=d1 tabIndex=0></div>");
    assertEquals("BODY", page.evaluate("() => document.activeElement.nodeName"));
    page.focus("#d1");
    assertEquals("d1", page.evaluate("() => document.activeElement.id"));
  }

  @PlaywrightTest
  @DisplayName("FO-002 should emit focus event")
  void shouldEmitFocusEvent(Page page) {
    page.setContent("<div id=d1 tabIndex=0></div>");
    boolean[] focused = {false};
    page.exposeFunction("focusEvent", args -> focused[0] = true);
    page.evalOnSelector("#d1", "d1 => d1.addEventListener('focus', window['focusEvent'])");
    page.focus("#d1");
    assertEquals(true, focused[0]);
  }

  @PlaywrightTest
  @DisplayName("FO-003 should emit blur event")
  void shouldEmitBlurEvent(Page page) {
    page.setContent("<div id=d1 tabIndex=0>DIV1</div><div id=d2 tabIndex=0>DIV2</div>");
    page.focus("#d1");
    boolean[] focused = {false};
    boolean[] blurred = {false};
    page.exposeFunction("focusEvent", args -> focused[0] = true);
    page.exposeFunction("blurEvent", args -> blurred[0] = true);
    page.evalOnSelector("#d1", "d1 => d1.addEventListener('blur', window['blurEvent'])");
    page.evalOnSelector("#d2", "d2 => d2.addEventListener('focus', window['focusEvent'])");
    page.focus("#d2");
    assertEquals(true, focused[0]);
    assertEquals(true, blurred[0]);
  }

  @PlaywrightTest
  @DisplayName("FO-004 should traverse focus")
  void shouldTraverseFocus(Page page) {
    page.setContent("<input id=\"i1\"><input id=\"i2\">");
    boolean[] focused = {false};
    page.exposeFunction("focusEvent", args -> focused[0] = true);
    page.evalOnSelector("#i2", "i2 => i2.addEventListener('focus', window['focusEvent'])");

    page.focus("#i1");
    page.keyboard().type("First");
    page.keyboard().press("Tab");
    page.keyboard().type("Last");

    assertEquals(true, focused[0]);
    assertEquals("First", page.evalOnSelector("#i1", "e => e.value"));
    assertEquals("Last", page.evalOnSelector("#i2", "e => e.value"));
  }

  @PlaywrightTest
  @DisplayName("FO-005 should traverse focus in all directions")
  void shouldTraverseFocusInAllDirections(Page page) {
    page.setContent("<input value=\"1\"><input value=\"2\"><input value=\"3\">");
    page.keyboard().press("Tab");
    assertEquals("1", page.evaluate("() => document.activeElement.value"));
    page.keyboard().press("Tab");
    assertEquals("2", page.evaluate("() => document.activeElement.value"));
    page.keyboard().press("Tab");
    assertEquals("3", page.evaluate("() => document.activeElement.value"));
    page.keyboard().press("Shift+Tab");
    assertEquals("2", page.evaluate("() => document.activeElement.value"));
    page.keyboard().press("Shift+Tab");
    assertEquals("1", page.evaluate("() => document.activeElement.value"));
  }

  @PlaywrightTest
  @DisplayName("FO-006 should traverse only form elements")
  void shouldTraverseOnlyFormElements(Page page, BrowserName browserName) {
    assumeFalse(!"darwin".equals(Env.platform()) || browserName != BrowserName.WEBKIT,
      "Chromium and WebKit both have settings for tab traversing all links, but it is only on by default in WebKit.");

    page.setContent("\n" +
      "    <input id=\"input-1\">\n" +
      "    <button id=\"button\">button</button>\n" +
      "    <a href id=\"link\">link</a>\n" +
      "    <input id=\"input-2\">\n" +
      "  ");
    page.keyboard().press("Tab");
    assertEquals("input-1", page.evaluate("() => document.activeElement.id"));
    page.keyboard().press("Tab");
    assertEquals("input-2", page.evaluate("() => document.activeElement.id"));
    page.keyboard().press("Shift+Tab");
    assertEquals("input-1", page.evaluate("() => document.activeElement.id"));
    page.keyboard().press("Alt+Tab");
    assertEquals("button", page.evaluate("() => document.activeElement.id"));
    page.keyboard().press("Alt+Tab");
    assertEquals("link", page.evaluate("() => document.activeElement.id"));
    page.keyboard().press("Alt+Tab");
    assertEquals("input-2", page.evaluate("() => document.activeElement.id"));
    page.keyboard().press("Alt+Shift+Tab");
    assertEquals("link", page.evaluate("() => document.activeElement.id"));
    page.keyboard().press("Alt+Shift+Tab");
    assertEquals("button", page.evaluate("() => document.activeElement.id"));
    page.keyboard().press("Alt+Shift+Tab");
    assertEquals("input-1", page.evaluate("() => document.activeElement.id"));
  }

  @PlaywrightTest
  @DisplayName("FO-007 clicking checkbox should activate it")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "Safari does not focus on click")
  void clickingCheckboxShouldActivateIt(Page page) {
    page.setContent("<input type=checkbox></input>");
    page.click("input");
    Object nodeName = page.evaluate("() => document.activeElement.nodeName");
    assertEquals("INPUT", nodeName);
  }

  @PlaywrightTest
  @DisplayName("FO-008 tab should cycle between single input and browser")
  @DisabledOnBrowser(value = BrowserName.FIREFOX, reason = "fixme: ")
  void tabShouldCycleBetweenSingleInputAndBrowser(Page page, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.CHROMIUM && !Env.isHeadlessShell(browserName), "Chromium keeps input focused.");
    page.setContent("<label for=\"input1\">input1</label>\n" +
      "    <input id=\"input1\">\n" +
      "    <script>\n" +
      "    {\n" +
      "      window.focusEvents = [];\n" +
      "      const input = document.getElementById('input1');\n" +
      "      input.addEventListener('blur', () => focusEvents.push('blur'));\n" +
      "      input.addEventListener('focus', () => focusEvents.push('focus'));\n" +
      "    }\n" +
      "    </script>");
    assertEquals("BODY", page.evaluate("() => document.activeElement.tagName"));
    page.keyboard().press("Tab");
    assertEquals("input1", page.evaluate("() => document.activeElement.id"));
    assertEquals(Arrays.asList("focus"), page.evaluate("() => window.focusEvents"));
    page.keyboard().press("Tab");
    assertEquals("BODY", page.evaluate("() => document.activeElement.tagName"));
    assertEquals(Arrays.asList("focus", "blur"), page.evaluate("() => window.focusEvents"));
    page.keyboard().press("Tab");
    assertEquals("input1", page.evaluate("() => document.activeElement.id"));
    assertEquals(Arrays.asList("focus", "blur", "focus"), page.evaluate("() => window.focusEvents"));
  }

  @PlaywrightTest
  @DisplayName("FO-009 tab should cycle between document elements and browser")
  @DisabledOnBrowser(value = BrowserName.FIREFOX, reason = "fixme: ")
  void tabShouldCycleBetweenDocumentElementsAndBrowser(Page page, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.CHROMIUM && !Env.isHeadlessShell(browserName), "Chromium keeps last input focused.");
    page.setContent("\n" +
      "    <input id=\"input1\">\n" +
      "    <input id=\"input2\">\n" +
      "    <script>\n" +
      "      window.focusEvents = [];\n" +
      "      {\n" +
      "        const input = document.getElementById('input1');\n" +
      "        input.addEventListener('blur', () => focusEvents.push('blur1'));\n" +
      "        input.addEventListener('focus', () => focusEvents.push('focus1'));\n" +
      "      }\n" +
      "      {\n" +
      "        const input = document.getElementById('input2');\n" +
      "        input.addEventListener('blur', () => focusEvents.push('blur2'));\n" +
      "        input.addEventListener('focus', () => focusEvents.push('focus2'));\n" +
      "      }\n" +
      "    </script>");
    assertEquals("BODY", page.evaluate("() => document.activeElement.tagName"));
    page.keyboard().press("Tab");
    assertEquals("input1", page.evaluate("() => document.activeElement.id"));
    assertEquals(Arrays.asList("focus1"), page.evaluate("() => window.focusEvents"));
    page.keyboard().press("Tab");
    assertEquals("input2", page.evaluate("() => document.activeElement.id"));
    assertEquals(Arrays.asList("focus1", "blur1", "focus2"), page.evaluate("() => window.focusEvents"));
    page.keyboard().press("Tab");
    assertEquals("BODY", page.evaluate("() => document.activeElement.tagName"));
    assertEquals(Arrays.asList("focus1", "blur1", "focus2", "blur2"), page.evaluate("() => window.focusEvents"));
    page.keyboard().press("Tab");
    assertEquals("input1", page.evaluate("() => document.activeElement.id"));
    assertEquals(Arrays.asList("focus1", "blur1", "focus2", "blur2", "focus1"), page.evaluate("() => window.focusEvents"));
  }

  @PlaywrightTest
  @DisplayName("FO-010 keeps focus on element when attempting to focus a non-focusable element")
  void keepsFocusOnElementWhenAttemptingToFocusANonFocusableElement(Page page) {
    page.setContent("\n" +
      "      <div id=\"focusable\" tabindex=\"0\">focusable</div>\n" +
      "      <div id=\"non-focusable\">not focusable</div>\n" +
      "      <script>\n" +
      "        window.eventLog = [];\n" +
      "\n" +
      "        const focusable = document.getElementById(\"focusable\");\n" +
      "\n" +
      "        focusable.addEventListener('blur', () => window.eventLog.push('blur focusable'));\n" +
      "        focusable.addEventListener('focus', () => window.eventLog.push('focus focusable'));\n" +
      "\n" +
      "        const nonFocusable = document.getElementById(\"non-focusable\");\n" +
      "        nonFocusable.addEventListener('blur', () => window.eventLog.push('blur non-focusable'));\n" +
      "        nonFocusable.addEventListener('focus', () => window.eventLog.push('focus non-focusable'));\n" +
      "      </script>\n" +
      "    ");
    page.locator("#focusable").click();
    // expect.soft
    assertEquals("focusable", page.evaluate("() => document.activeElement?.id"));
    page.locator("#non-focusable").focus();
    // expect.soft
    assertEquals("focusable", page.evaluate("() => document.activeElement?.id"));
    // expect.soft
    assertEquals(Arrays.asList("focus focusable"), page.evaluate("() => window['eventLog']"));
  }

  @PlaywrightTest
  @DisplayName("FO-011 should move focus from an iframe to its parent")
  @Fixme(value = "driver: the focus stays in the iframe: toBeFocused on #outer gets unexpected value \"inactive\" until timeout", browsers = BrowserName.FIREFOX)
  void shouldMoveFocusFromAnIframeToItsParent(Page page, TestServer server) {
    server.setRoute("/frame.html", (req, res) -> {
      res.setHeader("content-type", "text/html");
      res.end("<input id=inner>");
    });
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<iframe src=\"" + server.PREFIX + "/frame.html\"></iframe><input id=outer>");
    page.frameLocator("iframe").locator("#inner").focus();
    assertEquals(true, page.frameLocator("iframe").locator("#inner").evaluate("e => e === document.activeElement"));
    page.evaluate("() => document.getElementById('outer').focus()");
    assertThat(page.locator("#outer")).isFocused();
    assertEquals(false, page.frameLocator("iframe").locator("#inner").evaluate("e => e === document.activeElement"));
  }

  @PlaywrightTest
  @DisplayName("FO-012 should tab from an iframe to its parent")
  @Fixme(value = "driver: the focus stays in the iframe: toBeFocused on #outer gets unexpected value \"inactive\" until timeout", browsers = BrowserName.FIREFOX)
  void shouldTabFromAnIframeToItsParent(Page page, TestServer server) {
    server.setRoute("/frame.html", (req, res) -> {
      res.setHeader("content-type", "text/html");
      res.end("<input id=inner>");
    });
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<iframe src=\"" + server.PREFIX + "/frame.html\"></iframe><input id=outer>");
    page.frameLocator("iframe").locator("#inner").click();
    assertEquals(true, page.frameLocator("iframe").locator("#inner").evaluate("e => e === document.activeElement"));
    page.keyboard().press("Tab");
    assertThat(page.locator("#outer")).isFocused();
  }
}
