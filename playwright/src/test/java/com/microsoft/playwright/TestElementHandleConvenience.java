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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestElementHandleConvenience extends TestBase {
  @Test
  void shouldHaveANicePreview() {
    page.navigate(server.PREFIX + "/dom.html");
    ElementHandle outer = page.querySelector("#outer");
    ElementHandle inner = page.querySelector("#inner");
    ElementHandle check = page.querySelector("#check");
    JSHandle text = inner.evaluateHandle("e => e.firstChild");
    page.evaluate("() => 1");  // Give them a chance to calculate the preview.
    assertEquals("JSHandle@<div id=\"outer\" name=\"value\">…</div>", outer.toString());
    assertEquals("JSHandle@<div id=\"inner\">Text,↵more text</div>", inner.toString());
    assertEquals("JSHandle@#text=Text,↵more text", text.toString());
    assertEquals("JSHandle@<input checked id=\"check\" foo=\"bar\"\" type=\"checkbox\"/>", check.toString());
  }

  @Test
  void getAttributeShouldWork() {
    page.navigate(server.PREFIX + "/dom.html");
    ElementHandle handle = page.querySelector("#outer");
    assertEquals("value", handle.getAttribute("name"));
    assertNull(handle.getAttribute("foo"));
    assertEquals("value", page.getAttribute("#outer", "name"));
    assertNull(page.getAttribute("#outer", "foo"));
  }

  @Test
  void inputValueShouldWork() {
    page.navigate(server.PREFIX + "/dom.html");

    page.fill("#textarea", "text value");
    assertEquals("text value", page.inputValue("#textarea"));

    page.fill("#input", "input value");
    assertEquals("input value", page.inputValue("#input"));
    ElementHandle handle = page.querySelector("#input");
    assertEquals("input value", handle.inputValue());

    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.inputValue("#inner"));
    assertTrue(e.getMessage().contains("Node is not an <input>, <textarea> or <select> element"), e.getMessage());

    ElementHandle handle2 = page.querySelector("#inner");
    e = assertThrows(PlaywrightException.class, () -> handle2.inputValue());
    assertTrue(e.getMessage().contains("Node is not an <input>, <textarea> or <select> element"), e.getMessage());
  }

  @Test
  void innerHTMLShouldWork() {
    page.navigate(server.PREFIX + "/dom.html");
    ElementHandle handle = page.querySelector("#outer");
    assertEquals("<div id=\"inner\">Text,\nmore text</div>", handle.innerHTML());
    assertEquals("<div id=\"inner\">Text,\nmore text</div>", page.innerHTML("#outer"));
  }

  @Test
  void innerTextShouldWork() {
    page.navigate(server.PREFIX + "/dom.html");
    ElementHandle handle = page.querySelector("#inner");
    assertEquals("Text, more text", handle.innerText());
    assertEquals("Text, more text", page.innerText("#inner"));
  }

  @Test
  void innerTextShouldThrow() {
    page.setContent("<svg>text</svg>");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.innerText("svg"));
    assertTrue(e.getMessage().contains("Node is not an HTMLElement"), e.getMessage());
    ElementHandle handle = page.querySelector("svg");
    e = assertThrows(PlaywrightException.class, () -> handle.innerText());
    assertTrue(e.getMessage().contains("Node is not an HTMLElement"), e.getMessage());
  }

  @Test
  void textContentShouldWork() {
    page.navigate(server.PREFIX + "/dom.html");
    ElementHandle handle = page.querySelector("#inner");
    assertEquals("Text,\nmore text", handle.textContent());
    assertEquals("Text,\nmore text", page.textContent("#inner"));
  }

  @Test
  void textContentShouldBeAtomic() {
    String createDummySelector = "{\n" +
      "  query(root, selector) {\n" +
      "    const result = root.querySelector(selector);\n" +
      "    if (result)\n" +
      "      Promise.resolve().then(() => result.textContent = 'modified');\n" +
      "    return result;\n" +
      "  },\n" +
      "  queryAll(root, selector) {\n" +
      "    const result = Array.from(root.querySelectorAll(selector));\n" +
      "    for (const e of result)\n" +
      "      Promise.resolve().then(() => e.textContent = 'modified');\n" +
      "    return result;\n" +
      "  }\n" +
      "}\n";
    playwright.selectors().register("textContent", createDummySelector);
    page.setContent("<div>Hello</div>");
    String tc = page.textContent("textContent=div");
    assertEquals("Hello", tc);
    assertEquals("modified", page.evaluate("() => document.querySelector('div').textContent"));
  }

  @Test
  void innerTextShouldBeAtomic() {
    String createDummySelector = "{\n" +
      "  query(root, selector) {\n" +
      "    const result = root.querySelector(selector);\n" +
      "    if (result)\n" +
      "      Promise.resolve().then(() => result.textContent = 'modified');\n" +
      "    return result;\n" +
      "  },\n" +
      "  queryAll(root, selector) {\n" +
      "    const result = Array.from(root.querySelectorAll(selector));\n" +
      "    for (const e of result)\n" +
      "      Promise.resolve().then(() => e.textContent = 'modified');\n" +
      "    return result;\n" +
      "  }\n" +
      "}\n";
    playwright.selectors().register("innerText", createDummySelector);
    page.setContent("<div>Hello</div>");
    String tc = page.innerText("innerText=div");
    assertEquals("Hello", tc);
    assertEquals("modified", page.evaluate("() => document.querySelector('div').innerText"));
  }

  @Test
  void innerHTMLShouldBeAtomic() {
    String createDummySelector = "{\n" +
      "  query(root, selector) {\n" +
      "    const result = root.querySelector(selector);\n" +
      "    if (result)\n" +
      "      Promise.resolve().then(() => result.textContent = 'modified');\n" +
      "    return result;\n" +
      "  },\n" +
      "  queryAll(root, selector) {\n" +
      "    const result = Array.from(root.querySelectorAll(selector));\n" +
      "    for (const e of result)\n" +
      "      Promise.resolve().then(() => e.textContent = 'modified');\n" +
      "    return result;\n" +
      "  }\n" +
      "}\n";
    playwright.selectors().register("innerHTML", createDummySelector);
    page.setContent("<div>Hello<span>world</span></div>");
    String tc = page.innerHTML("innerHTML=div");
    assertEquals("Hello<span>world</span>", tc);
    assertEquals("modified", page.evaluate("() => document.querySelector('div').innerHTML"));
  }

  @Test
  void getAttributeShouldBeAtomic() {
    String createDummySelector = "{\n" +
      "  query(root, selector) {\n" +
      "    const result = root.querySelector(selector);\n" +
      "    if (result)\n" +
      "      Promise.resolve().then(() => result.setAttribute('foo', 'modified'));\n" +
      "    return result;\n" +
      "  },\n" +
      "  queryAll(root, selector) {\n" +
      "    const result = Array.from(root.querySelectorAll(selector));\n" +
      "    for (const e of result)\n" +
      "      Promise.resolve().then(() => e.setAttribute('foo', 'modified'));\n" +
      "    return result;\n" +
      "  }\n" +
      "}\n";
    playwright.selectors().register("getAttribute", createDummySelector);
    page.setContent("<div foo=hello></div>");
    String tc = page.getAttribute("getAttribute=div", "foo");
    assertEquals("hello", tc);
    assertEquals("modified", page.evaluate("() => document.querySelector('div').getAttribute('foo')"));
  }

  @Test
  void isVisibleAndIsHiddenShouldWork() {
    page.setContent("<div>Hi</div><span></span>");
    ElementHandle div = page.querySelector("div");
    assertTrue(div.isVisible());
    assertFalse(div.isHidden());
    assertTrue(page.isVisible("div"));
    assertFalse(page.isHidden("div"));
    ElementHandle span = page.querySelector("span");
    assertFalse(span.isVisible());
    assertTrue(span.isHidden());
    assertFalse(page.isVisible("span"));
    assertTrue(page.isHidden("span"));
  }

  @Test
  void isEnabledAndIsDisabledShouldWork() {
    page.setContent(" <button disabled>button1</button>\n" +
      "<button>button2</button>\n" +
      "<div>div</div>");
    ElementHandle div = page.querySelector("div");
    assertTrue(div.isEnabled());
    assertFalse(div.isDisabled());
    assertTrue(page.isEnabled("div"));
    assertFalse(page.isDisabled("div"));
    ElementHandle button1 = page.querySelector(":text('button1')");
    assertFalse(button1.isEnabled());
    assertTrue(button1.isDisabled());
    assertFalse(page.isEnabled(":text('button1')"));
    assertTrue(page.isDisabled(":text('button1')"));
    ElementHandle button2 = page.querySelector(":text('button2')");
    assertTrue(button2.isEnabled());
    assertFalse(button2.isDisabled());
    assertTrue(page.isEnabled(":text('button2')"));
    assertFalse(page.isDisabled(":text('button2')"));
  }

  @Test
  void isEditableShouldWork() {
    page.setContent("<input id=input1 disabled><textarea></textarea><input id=input2>");
    page.evalOnSelector("textarea", "t => t.readOnly = true");
    ElementHandle input1 = page.querySelector("#input1");
    assertFalse(input1.isEditable());
    assertFalse(page.isEditable("#input1"));
    ElementHandle input2 = page.querySelector("#input2");
    assertTrue(input2.isEditable());
    assertTrue(page.isEditable("#input2"));
    ElementHandle textarea = page.querySelector("textarea");
    assertFalse(textarea.isEditable());
    assertFalse(page.isEditable("textarea"));
  }

  @Test
  void isCheckedShouldWork() {
    page.setContent("<input type='checkbox' checked><div>Not a checkbox</div>");
    ElementHandle handle = page.querySelector("input");
    assertTrue(handle.isChecked());
    assertTrue(page.isChecked("input"));
    handle.evaluate("input => input.checked = false");
    assertFalse(handle.isChecked());
    assertFalse(page.isChecked("input"));
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.isChecked("div"));
    assertTrue(e.getMessage().contains("Not a checkbox or radio button"));
  }
}
