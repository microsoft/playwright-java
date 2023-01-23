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
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.EnabledIf;

import static org.junit.jupiter.api.Assertions.*;

public class TestPageFill extends TestBase {

  @Test
  void shouldFillTextarea() {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.fill("textarea", "some value");
    assertEquals("some value", page.evaluate("() => window['result']"));
  }

  @Test
  void shouldFillInput() {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.fill("input", "some value");
    assertEquals("some value", page.evaluate("() => window['result']"));
  }

  @Test
  void shouldThrowOnUnsupportedInputs() {
    page.navigate(server.PREFIX + "/input/textarea.html");
    for (String type : new String[]{"button", "checkbox", "file", "image", "radio", "reset", "submit"}) {
      page.evalOnSelector("input", "(input, type) => input.setAttribute('type', type)", type);
      PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.fill("input", ""));
      assertTrue(e.getMessage().contains("Error: Input of type \"" + type + "\" cannot be filled"), "type = " + type + e.getMessage());
    }
  }

  @Test
  void shouldFillRangeInput() {
    page.setContent("<input type=range min=0 max=100 value=50>");
    page.fill("input", "42");
    assertEquals("42", page.evalOnSelector("input", "input => input.value"));
  }

  @Test
  void shouldThrowOnIncorrectRangeValue() {
    page.setContent("<input type=range min=0 max=100 value=50>");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.fill("input", "foo"));
    assertTrue(e.getMessage().contains("Malformed value"), e.getMessage());

    e = assertThrows(PlaywrightException.class, () -> page.fill("input", "200"));
    assertTrue(e.getMessage().contains("Malformed value"), e.getMessage());

    e = assertThrows(PlaywrightException.class, () ->  page.fill("input", "15.43"));
    assertTrue(e.getMessage().contains("Malformed value"), e.getMessage());
  }


  @Test
  void shouldFillDifferentInputTypes() {
    page.navigate(server.PREFIX + "/input/textarea.html");
    for (String type : new String[]{"password", "search", "tel", "text", "url"}) {
      page.evalOnSelector("input", "(input, type) => input.setAttribute('type', type)", type);
      page.fill("input", "text " + type);
      assertEquals("text " + type, page.evaluate("() => window['result']"));
    }
  }

  @Test
  void shouldFillDateInputAfterClicking() {
    page.setContent("<input type=date>");
    page.click("input");
    page.fill("input", "2020-03-02");
    assertEquals("2020-03-02", page.evalOnSelector("input", "input => input.value"));
  }

  @Test
  @DisabledIf(value="com.microsoft.playwright.TestBase#isWebKit", disabledReason="skip")
  void shouldThrowOnIncorrectDate() {
    page.setContent("<input type=date>");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.fill("input", "2020-13-05"));
    assertTrue(e.getMessage().contains("Malformed value"));
  }

  @Test
  void shouldFillTimeInput() {
    page.setContent("<input type=time>");
    page.fill("input", "13:15");
    assertEquals("13:15", page.evalOnSelector("input", "input => input.value"));
  }

  @Test
  @DisabledIf(value="com.microsoft.playwright.TestBase#isWebKit", disabledReason="skip")
  void shouldThrowOnIncorrectTime() {
    page.setContent("<input type=time>");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.fill("input", "25:05"));
    assertTrue(e.getMessage().contains("Malformed value"));
  }

  @Test
  void shouldFillDatetimeLocalInput() {
    page.setContent("<input type=datetime-local>");
    page.fill("input", "2020-03-02T05:15");
    assertEquals(page.evalOnSelector("input", "input => input.value"), "2020-03-02T05:15");
  }

  @Test
  @EnabledIf(value="com.microsoft.playwright.TestBase#isChromium", disabledReason="skip")
  void shouldThrowOnIncorrectDatetimeLocal() {
    page.setContent("<input type=datetime-local>");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.fill("input", "abc"));
    assertTrue(e.getMessage().contains("Malformed value"));
  }

  @Test
  void shouldFillContenteditable() {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.fill("div[contenteditable]", "some value");
    assertEquals(page.evalOnSelector("div[contenteditable]", "div => div.textContent"), "some value");
  }

  @Test
  void shouldFillElementsWithExistingValueAndSelection() {
    page.navigate(server.PREFIX + "/input/textarea.html");

    page.evalOnSelector("input", "input => input.value = 'value one'");
    page.fill("input", "another value");
    assertEquals("another value", page.evaluate("() => window['result']"));

    page.evalOnSelector("input", "input => {\n" +
      "  input.selectionStart = 1;\n" +
      "  input.selectionEnd = 2;\n" +
      "}");
    page.fill("input", "maybe this one");
    assertEquals("maybe this one", page.evaluate("() => window['result']"));

    page.evalOnSelector("div[contenteditable]", "div => {\n" +
      "  div.innerHTML = 'some text <span>some more text<span> and even more text';\n" +
      "  const range = document.createRange();\n" +
      "  range.selectNodeContents(div.querySelector('span'));\n" +
      "  const selection = window.getSelection();\n" +
      "  selection.removeAllRanges();\n" +
      "  selection.addRange(range);\n" +
      "}");
    page.fill("div[contenteditable]", "replace with this");
    assertEquals("replace with this", page.evalOnSelector("div[contenteditable]", "div => div.textContent"));
  }

  @Test
  void shouldThrowWhenElementIsNotAnInputTextareaOrContenteditable() {
    page.navigate(server.PREFIX + "/input/textarea.html");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.fill("body", ""));
    assertTrue(e.getMessage().contains("Element is not an <input>"));
  }

  void shouldThrowIfPassedANonStringValue() {
    // Doesn't make sense in a strongly typed language
  }

  // TODO: not supported in sync api, tests internals
  void shouldRetryOnDisabledElement() {
  }
  void shouldRetryOnInvisibleElement() {
  }
  @Test
  void shouldBeAbleToFillTheBody() {
    page.setContent("<body contentEditable='true'></body>");
    page.fill("body", "some value");
    assertEquals("some value", page.evaluate("() => document.body.textContent"));
  }

  @Test
  void shouldFillFixedPositionInput() {
    page.setContent("<input style='position: fixed;' />");
    page.fill("input", "some value");
    assertEquals("some value", page.evaluate("() => document.querySelector('input').value"));
  }

  @Test
  void shouldBeAbleToFillWhenFocusIsInTheWrongFrame() {
    page.setContent("<div contentEditable='true'></div>\n" +
      "<iframe></iframe>");
    page.focus("iframe");
    page.fill("div", "some value");
    assertEquals("some value", page.evalOnSelector("div", "d => d.textContent"));
  }

  @Test
  void shouldBeAbleToFillTheInputTypeNumber() {
    page.setContent("<input id='input' type='number'></input>");
    page.fill("input", "42");
    assertEquals("42", page.evaluate("() => window['input'].value"));
  }

  @Test
  void shouldBeAbleToFillExponentIntoTheInputTypeNumber() {
    page.setContent("<input id='input' type='number'></input>");
    page.fill("input", "-10e5");
    assertEquals("-10e5", page.evaluate("() => window['input'].value"));
  }

  @Test
  void shouldBeAbleToFillInputTypeNumberWithEmptyString() {
    page.setContent("<input id='input' type='number' value='123'></input>");
    page.fill("input", "");
    assertEquals("", page.evaluate("() => window['input'].value"));
  }

  @Test
  void shouldNotBeAbleToFillTextIntoTheInputTypeNumber() {
    page.setContent("<input id='input' type='number'></input>");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.fill("input", "abc"));
    assertTrue(e.getMessage().contains("Cannot type text into input[type=number]"));
  }

  @Test
  void shouldBeAbleToClear() {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.fill("input", "some value");
    assertEquals("some value", page.evaluate("() => window['result']"));
    page.fill("input", "");
    assertEquals("", page.evaluate("() => window['result']"));
  }

  @Test
  void inputValueShouldWork() {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.fill("input", "my-text-content");
    assertEquals("my-text-content", page.inputValue("input"));
    page.fill("input", "");
    assertEquals("", page.inputValue("input"));
  }

  @Test
  void shouldBeAbleToClearUsingFill() {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.fill("input", "some value");
    assertEquals("some value", page.evaluate("() => window['result']"));
    page.fill("input", "");
    assertEquals("", page.evaluate("() => window['result']"));
  }
}
