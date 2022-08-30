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

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class TestLocatorConvenience extends TestBase {
  @Test
  void shouldHaveANicePreview() {
    page.navigate(server.PREFIX + "/dom.html");
    Locator outer = page.locator("#outer");
    Locator inner = outer.locator("#inner");
    Locator check = page.locator("#check");
    JSHandle text = inner.evaluateHandle("e => e.firstChild");
    page.evaluate("() => 1");  // Give them a chance to calculate the preview.
    assertEquals("Locator@#outer", outer.toString());
    assertEquals("Locator@#outer >> #inner", inner.toString());
    assertEquals("JSHandle@#text=Text,↵more text", text.toString());
    assertEquals("Locator@#check", check.toString());
  }

  @Test
  void getAttributeShouldWork() {
    page.navigate(server.PREFIX + "/dom.html");
    Locator locator = page.locator("#outer");
    assertEquals("value", locator.getAttribute("name"));
    assertNull(locator.getAttribute("foo"));
    assertEquals("value", page.getAttribute("#outer", "name"));
    assertNull(page.getAttribute("#outer", "foo"));
  }

  @Test
  void inputValueShouldWork() {
    page.navigate(server.PREFIX + "/dom.html");

    page.selectOption("#select", "foo");
    assertEquals("foo", page.inputValue("#select"));

    page.fill("#textarea", "text value");
    assertEquals("text value", page.inputValue("#textarea"));

    page.fill("#input", "input value");
    assertEquals("input value", page.inputValue("#input"));
    Locator locator = page.locator("#input");
    assertEquals("input value", locator.inputValue());

    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.inputValue("#inner"));
    assertTrue(e.getMessage().contains("Node is not an <input>, <textarea> or <select> element"), e.getMessage());
    e = assertThrows(PlaywrightException.class, () -> {
      Locator locator2 = page.locator("#inner");
      locator2.inputValue();
    });
    assertTrue(e.getMessage().contains("Node is not an <input>, <textarea> or <select> element"), e.getMessage());
  }

  @Test
  void innerHTMLShouldWork() {
    page.navigate(server.PREFIX + "/dom.html");
    Locator locator = page.locator("#outer");
    assertEquals("<div id=\"inner\">Text,\nmore text</div>", locator.innerHTML());
    assertEquals("<div id=\"inner\">Text,\nmore text</div>", page.innerHTML("#outer"));
  }

  @Test
  void innerTextShouldWork() {
    page.navigate(server.PREFIX + "/dom.html");
    Locator locator = page.locator("#inner");
    assertEquals("Text, more text", locator.innerText());
    assertEquals("Text, more text", page.innerText("#inner"));
  }

  @Test
  void innerTextShouldThrow() {
    page.setContent("<svg>text</svg>");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.innerText("svg"));
    assertTrue(e.getMessage().contains("Node is not an HTMLElement"), e.getMessage());

    Locator locator = page.locator("svg");
    e = assertThrows(PlaywrightException.class, () -> locator.innerText());
    assertTrue(e.getMessage().contains("Node is not an HTMLElement"), e.getMessage());
  }

  @Test
  void textContentShouldWork() {
    page.navigate(server.PREFIX + "/dom.html");
    Locator locator = page.locator("#inner");
    assertEquals("Text,\nmore text", locator.textContent());
    assertEquals("Text,\nmore text", page.textContent("#inner"));
  }

  @Test
  void isVisibleAndIsHiddenShouldWork() {
    page.setContent("<div>Hi</div><span></span>");

    Locator div = page.locator("div");
    assertTrue(div.isVisible());
    assertFalse(div.isHidden());
    assertTrue(page.isVisible("div"));
    assertFalse(page.isHidden("div"));

    Locator span = page.locator("span");
    assertFalse(span.isVisible());
    assertTrue(span.isHidden());
    assertFalse(page.isVisible("span"));
    assertTrue(page.isHidden("span"));

    assertFalse(page.isVisible("no-such-element"));
    assertTrue(page.isHidden("no-such-element"));
  }

  @Test
  void isEnabledAndIsDisabledShouldWork() {
    page.setContent("<button disabled>button1</button>\n" +
      "<button>button2</button>\n" +
      "<div>div</div>");
    Locator div = page.locator("div");
    assertTrue(div.isEnabled());
    assertFalse(div.isDisabled());
    assertTrue(page.isEnabled("div"));
    assertEquals(false, page.isDisabled("div"));
    Locator button1 = page.locator(":text('button1')");
    assertEquals(false, button1.isEnabled());
    assertTrue(button1.isDisabled());
    assertEquals(false, page.isEnabled(":text('button1')"));
    assertTrue(page.isDisabled(":text('button1')"));
    Locator button2 = page.locator(":text('button2')");
    assertTrue(button2.isEnabled());
    assertEquals(false, button2.isDisabled());
    assertTrue(page.isEnabled(":text('button2')"));
    assertEquals(false, page.isDisabled(":text('button2')"));
  }

  @Test
  void isEditableShouldWork() {
    page.setContent("<input id=input1 disabled><textarea></textarea><input id=input2>");
    page.evalOnSelector("textarea", "t => t.readOnly = true");
    Locator input1 = page.locator("#input1");
    assertFalse(input1.isEditable());
    assertFalse(page.isEditable("#input1"));
    Locator input2 = page.locator("#input2");
    assertTrue(input2.isEditable());
    assertTrue(page.isEditable("#input2"));
    Locator textarea = page.locator("textarea");
    assertFalse(textarea.isEditable());
    assertFalse(page.isEditable("textarea"));
  }

  @Test
  void isCheckedShouldWork() {
    page.setContent("<input type='checkbox' checked><div>Not a checkbox</div>");
    Locator element = page.locator("input");
    assertTrue(element.isChecked());
    assertTrue(page.isChecked("input"));
    element.evaluate("input => input.checked = false");
    assertFalse(element.isChecked());
    assertFalse(page.isChecked("input"));
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.isChecked("div"));
    assertTrue(e.getMessage().contains("Not a checkbox or radio button"));
  }

  @Test
  void allTextContentsShouldWork() {
    page.setContent("<div>A</div><div>B</div><div>C</div>");
    assertEquals(asList("A", "B", "C"), page.locator("div").allTextContents());
  }

  @Test
  void allInnerTextsShouldWork() {
    page.setContent("<div>A</div><div>B</div><div>C</div>");
    assertEquals(asList("A", "B", "C"), page.locator("div").allInnerTexts());
  }
}
