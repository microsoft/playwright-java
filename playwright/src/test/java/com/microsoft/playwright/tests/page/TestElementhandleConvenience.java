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

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/elementhandle-convenience.spec.ts. */
public class TestElementhandleConvenience {
  private static String repeat(String s, int count) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < count; i++) {
      sb.append(s);
    }
    return sb.toString();
  }

  @PlaywrightTest
  @DisplayName("EH-024 should have a nice preview")
  void shouldHaveANicePreview(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/dom.html");
    ElementHandle outer = page.querySelector("#outer");
    ElementHandle inner = page.querySelector("#inner");
    ElementHandle check = page.querySelector("#check");
    JSHandle text = inner.evaluateHandle("e => e.firstChild");
    page.evaluate("() => 1");  // Give them a chance to calculate the preview.
    assertEquals("JSHandle@<div id=\"outer\" name=\"value\">…</div>", String.valueOf(outer));
    assertEquals("JSHandle@<div id=\"inner\">Text,↵more text</div>", String.valueOf(inner));
    assertEquals("JSHandle@#text=Text,↵more text", String.valueOf(text));
    assertEquals("JSHandle@<input checked id=\"check\" foo=\"bar\"\" type=\"checkbox\"/>", String.valueOf(check));
  }

  @PlaywrightTest
  @DisplayName("EH-025 should have a nice preview for non-ascii attributes/children")
  void shouldHaveANicePreviewForNonAsciiAttributesChildren(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<div title=\"" + repeat("😛", 100) + "\">" + repeat("😛", 100));
    ElementHandle handle = page.querySelector("div");
    String expected = "JSHandle@<div title=\"" + repeat("😛", 100) + "\">" + repeat("😛", 49) + "…</div>";
    // Java: expect.poll(() => String(handle)).toBe(expected)
    for (int i = 0; i < 50 && !expected.equals(String.valueOf(handle)); i++) {
      page.waitForTimeout(100);
    }
    assertEquals(expected, String.valueOf(handle));
  }

  @PlaywrightTest
  @DisplayName("EH-026 getAttribute should work")
  void getAttributeShouldWork(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/dom.html");
    ElementHandle handle = page.querySelector("#outer");
    assertEquals("value", handle.getAttribute("name"));
    assertNull(handle.getAttribute("foo"));
    assertEquals("value", page.getAttribute("#outer", "name"));
    assertNull(page.getAttribute("#outer", "foo"));
  }

  @PlaywrightTest
  @DisplayName("EH-027 inputValue should work")
  void inputValueShouldWork(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/dom.html");

    page.selectOption("#select", "foo");
    assertEquals("foo", page.inputValue("#select"));

    page.fill("#textarea", "text value");
    assertEquals("text value", page.inputValue("#textarea"));

    page.fill("#input", "input value");
    assertEquals("input value", page.inputValue("#input"));
    ElementHandle handle = page.querySelector("#input");
    assertEquals("input value", handle.inputValue());

    PlaywrightException error1 = assertThrows(PlaywrightException.class, () -> page.inputValue("#inner"));
    assertTrue(error1.getMessage().contains("Node is not an <input>, <textarea> or <select> element"));
    ElementHandle handle2 = page.querySelector("#inner");
    PlaywrightException error2 = assertThrows(PlaywrightException.class, () -> handle2.inputValue());
    assertTrue(error2.getMessage().contains("Node is not an <input>, <textarea> or <select> element"));
  }

  @PlaywrightTest
  @DisplayName("EH-028 innerHTML should work")
  void innerHTMLShouldWork(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/dom.html");
    ElementHandle handle = page.querySelector("#outer");
    assertEquals("<div id=\"inner\">Text,\nmore text</div>", handle.innerHTML());
    assertEquals("<div id=\"inner\">Text,\nmore text</div>", page.innerHTML("#outer"));
  }

  @PlaywrightTest
  @DisplayName("EH-029 innerText should work")
  void innerTextShouldWork(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/dom.html");
    ElementHandle handle = page.querySelector("#inner");
    assertEquals("Text, more text", handle.innerText());
    assertEquals("Text, more text", page.innerText("#inner"));
  }

  @PlaywrightTest
  @DisplayName("EH-030 innerText should throw")
  void innerTextShouldThrow(Page page, TestServer server) {
    page.setContent("<svg>text</svg>");
    PlaywrightException error1 = assertThrows(PlaywrightException.class, () -> page.innerText("svg"));
    assertTrue(error1.getMessage().contains("Node is not an HTMLElement"));
    ElementHandle handle = page.querySelector("svg");
    PlaywrightException error2 = assertThrows(PlaywrightException.class, () -> handle.innerText());
    assertTrue(error2.getMessage().contains("Node is not an HTMLElement"));
  }

  @PlaywrightTest
  @DisplayName("EH-031 textContent should work")
  void textContentShouldWork(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/dom.html");
    ElementHandle handle = page.querySelector("#inner");
    assertEquals("Text,\nmore text", handle.textContent());
    assertEquals("Text,\nmore text", page.textContent("#inner"));
  }

  @PlaywrightTest
  @DisplayName("EH-032 textContent should work on ShadowRoot")
  void textContentShouldWorkOnShadowRoot(Page page, TestServer server) {
    page.setContent("\n" +
      "    <div></div>\n" +
      "    <script>\n" +
      "      document.querySelector('div').attachShadow({ mode: 'open' }).innerHTML = '<div>hello</div>';\n" +
      "    </script>\n" +
      "  ");
    ElementHandle div = page.querySelector("div");
    ElementHandle root = div.evaluateHandle("div => div.shadowRoot").asElement();
    assertEquals("hello", root.textContent());
    // We do not match ShadowRoot as ":scope".
    assertEquals(Collections.emptyList(), root.querySelectorAll(":scope div"));
  }

  @PlaywrightTest
  @DisplayName("EH-033 isVisible and isHidden should work")
  void isVisibleAndIsHiddenShouldWork(Page page) {
    page.setContent("<div>Hi</div><span></span>");

    ElementHandle div = page.querySelector("div");
    assertEquals(true, div.isVisible());
    assertEquals(false, div.isHidden());
    assertEquals(true, page.isVisible("div"));
    assertEquals(false, page.isHidden("div"));

    ElementHandle span = page.querySelector("span");
    assertEquals(false, span.isVisible());
    assertEquals(true, span.isHidden());
    assertEquals(false, page.isVisible("span"));
    assertEquals(true, page.isHidden("span"));

    assertEquals(false, page.isVisible("no-such-element"));
    assertEquals(true, page.isHidden("no-such-element"));
  }

  @PlaywrightTest
  @DisplayName("EH-034 isVisible should not throw when the DOM element is not connected")
  void isVisibleShouldNotThrowWhenTheDOMElementIsNotConnected(Page page) {
    page.setContent("<div id=\"root\"></div>");
    page.evaluate("() => {\n" +
      "    function insert() {\n" +
      "      document.getElementById('root').innerHTML = '<div id=\"problem\">Problem</div>';\n" +
      "      window.requestAnimationFrame(remove);\n" +
      "    }\n" +
      "    function remove() {\n" +
      "      const node = document.getElementById('problem');\n" +
      "      node?.parentNode?.removeChild(node);\n" +
      "      window.requestAnimationFrame(insert);\n" +
      "    }\n" +
      "    window.requestAnimationFrame(insert);\n" +
      "  }");

    for (int i = 0; i < 10; i++) {
      page.isVisible("#problem");
    }
  }

  @PlaywrightTest
  @DisplayName("EH-035 isEnabled and isDisabled should work")
  void isEnabledAndIsDisabledShouldWork(Page page) {
    page.setContent("\n" +
      "    <button disabled>button1</button>\n" +
      "    <button>button2</button>\n" +
      "    <div>div</div>\n" +
      "  ");
    ElementHandle div = page.querySelector("div");
    assertEquals(true, div.isEnabled());
    assertEquals(false, div.isDisabled());
    assertEquals(true, page.isEnabled("div"));
    assertEquals(false, page.isDisabled("div"));
    ElementHandle button1 = page.querySelector(":text(\"button1\")");
    assertEquals(false, button1.isEnabled());
    assertEquals(true, button1.isDisabled());
    assertEquals(false, page.isEnabled(":text(\"button1\")"));
    assertEquals(true, page.isDisabled(":text(\"button1\")"));
    ElementHandle button2 = page.querySelector(":text(\"button2\")");
    assertEquals(true, button2.isEnabled());
    assertEquals(false, button2.isDisabled());
    assertEquals(true, page.isEnabled(":text(\"button2\")"));
    assertEquals(false, page.isDisabled(":text(\"button2\")"));
  }

  @PlaywrightTest
  @DisplayName("EH-036 isEnabled and isDisabled should work with <select/> option/optgroup correctly")
  void isEnabledAndIsDisabledShouldWorkWithSelectOptionOptgroupCorrectly(Page page) {
    page.setContent("\n" +
      "    <select name=\"select\">\n" +
      "      <option id=\"enabled1\" value=\"1\">Enabled</option>\n" +
      "      <option id=\"disabled1\" value=\"2\" disabled>Disabled</option>\n" +
      "      <optgroup label=\"Foo1\">\n" +
      "        <option value=\"mercedes\">Mercedes</option>\n" +
      "      </optgroup>\n" +
      "      <optgroup label=\"Foo2\" disabled>\n" +
      "        <option value=\"mercedes\">Mercedes</option>\n" +
      "      </optgroup>\n" +
      "    </select>\n" +
      "  ");
    assertEquals(true, page.querySelector("#enabled1").isEnabled());
    assertEquals(false, page.querySelector("#enabled1").isDisabled());
    assertEquals(false, page.querySelector("#disabled1").isEnabled());
    assertEquals(true, page.querySelector("#disabled1").isDisabled());
    assertEquals(true, page.querySelector("optgroup >> nth=0").isEnabled());
    assertEquals(false, page.querySelector("optgroup >> nth=0").isDisabled());
    assertEquals(false, page.querySelector("optgroup >> nth=1").isEnabled());
    assertEquals(true, page.querySelector("optgroup >> nth=1").isDisabled());
  }

  @PlaywrightTest
  @DisplayName("EH-037 isEditable should work")
  void isEditableShouldWork(Page page) {
    page.setContent("<input id=input1 disabled><textarea></textarea><input id=input2>");
    page.evalOnSelector("textarea", "t => t.readOnly = true");
    ElementHandle input1 = page.querySelector("#input1");
    assertEquals(false, input1.isEditable());
    assertEquals(false, page.isEditable("#input1"));
    ElementHandle input2 = page.querySelector("#input2");
    assertEquals(true, input2.isEditable());
    assertEquals(true, page.isEditable("#input2"));
    ElementHandle textarea = page.querySelector("textarea");
    assertEquals(false, textarea.isEditable());
    assertEquals(false, page.isEditable("textarea"));
  }

  @PlaywrightTest
  @DisplayName("EH-038 isChecked should work")
  void isCheckedShouldWork(Page page) {
    page.setContent("<input type='checkbox' checked><div>Not a checkbox</div>");
    ElementHandle handle = page.querySelector("input");
    assertEquals(true, handle.isChecked());
    assertEquals(true, page.isChecked("input"));
    handle.evaluate("input => input.checked = false");
    assertEquals(false, handle.isChecked());
    assertEquals(false, page.isChecked("input"));
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.isChecked("div"));
    assertTrue(error.getMessage().contains("Not a checkbox or radio button"));
  }
}
