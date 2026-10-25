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

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.async;
import static com.microsoft.playwright.fixtures.Utils.rafraf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/page/page-fill.spec.ts. */
public class TestPageFill {
  private static void giveItAChanceToFill(Page page) {
    rafraf(page, 5);
  }

  @PlaywrightTest
  @DisplayName("FI-001 should fill textarea @smoke")
  void shouldFillTextarea(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.fill("textarea", "some value");
    assertEquals("some value", page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("FI-002 should fill input")
  void shouldFillInput(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.fill("input", "some value");
    assertEquals("some value", page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("FI-003 should throw on unsupported inputs")
  void shouldThrowOnUnsupportedInputs(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    for (String type : new String[]{"button", "checkbox", "file", "image", "radio", "reset", "submit"}) {
      page.evalOnSelector("input", "(input, type) => input.setAttribute('type', type)", type);
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.fill("input", ""));
      assertTrue(error.getMessage().contains("Input of type \"" + type + "\" cannot be filled"));
    }
  }

  @PlaywrightTest
  @DisplayName("FI-004 should fill different input types")
  void shouldFillDifferentInputTypes(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    for (String type : new String[]{"password", "search", "tel", "text", "url", "invalid-type"}) {
      page.evalOnSelector("input", "(input, type) => input.setAttribute('type', type)", type);
      page.fill("input", "text " + type);
      assertEquals("text " + type, page.evaluate("() => window['result']"));
    }
  }

  @PlaywrightTest
  @DisplayName("FI-005 should fill range input")
  @Fixme(value = "driver: the driver (1.64.1) predates the upstream fix of issue 43328: page.fill(\"input\", \"42.0\") on input[type=range] fails with \"Error: Malformed value\"", browsers = {BrowserName.CHROMIUM, BrowserName.WEBKIT})
  void shouldFillRangeInput(Page page) {
    page.setContent("<input type=range min=0 max=100 value=50>");
    page.fill("input", "42");
    assertEquals("42", page.evalOnSelector("input", "input => input.value"));
    // Valid numbers that the browser may serialize differently.
    String[] values = {"42.0", "1e1"};
    int[] expecteds = {42, 10};
    for (int i = 0; i < values.length; i++) {
      page.fill("input", values[i]);
      assertEquals(expecteds[i], page.evalOnSelector("input", "input => input.valueAsNumber"));
    }
  }

  @PlaywrightTest
  @DisplayName("FI-006 should fill color input")
  void shouldFillColorInput(Page page) {
    page.setContent("<input type=color value=\"#e66465\">");
    page.fill("input", "#aaaaaa");
    assertEquals("#aaaaaa", page.evalOnSelector("input", "input => input.value"));
  }

  @PlaywrightTest
  @DisplayName("FI-007 should fill color input case insensitive")
  void shouldFillColorInputCaseInsensitive(Page page, BrowserName browserName) {
    page.setContent("<input type=color value=\"#e66465\">");
    page.fill("input", "#AbCd00");
    if (browserName == BrowserName.WEBKIT && Env.isWindows && !"webkit-wsl".equals(Env.channel())) {
      assertEquals("#AbCd00", page.evalOnSelector("input", "input => input.value"));
    } else {
      assertEquals("#abcd00", page.evalOnSelector("input", "input => input.value"));
    }
  }

  @PlaywrightTest
  @DisplayName("FI-008 should fill date input after clicking")
  void shouldFillDateInputAfterClicking(Page page, TestServer server) {
    page.setContent("<input type=date>");
    page.click("input");
    page.fill("input", "2020-03-02");
    assertEquals("2020-03-02", page.evalOnSelector("input", "input => input.value"));
  }

  @Nested
  @ParameterizedClass
  @CsvSource({
    "color,#aaaaaa",
    "date,2020-03-02",
    "time,13:15",
    "datetime-local,2020-03-02T13:15:30",
    "month,2020-03",
    "range,42",
    "week,2020-W50"
  })
  class InputEventComposedShouldBeTrueAndCrossShadowDomBoundary {
    @Parameter(0)
    String type;
    @Parameter(1)
    String value;

    @PlaywrightTest
    @DisplayName("FI-009 input event.composed should be true and cross shadow dom boundary - ${type}")
    void inputEventComposedShouldBeTrueAndCrossShadowDomBoundaryType(Page page, TestServer server, BrowserName browserName) {
      assumeFalse(browserName != BrowserName.CHROMIUM && Arrays.asList("month", "week").contains(type), "Some browser/platforms do not implement certain input types");
      assumeFalse(browserName == BrowserName.WEBKIT && Env.isWindows && Arrays.asList("color", "date", "time", "datetime-local").contains(type), "Some browser/platforms do not implement certain input types");
      page.navigate(server.EMPTY_PAGE);
      page.setContent("<body><script>\n" +
        "    const div = document.createElement('div');\n" +
        "    const shadowRoot = div.attachShadow({mode: 'open'});\n" +
        "    shadowRoot.innerHTML = '<input type=" + type + "></input>';\n" +
        "    document.body.appendChild(div);\n" +
        "  </script></body>");
      page.locator("body").evaluate("select => {\n" +
        "      window.firedBodyEvents = [];\n" +
        "      for (const event of ['input', 'change']) {\n" +
        "        select.addEventListener(event, e => {\n" +
        "          window.firedBodyEvents.push(e.type + ':' + e.composed);\n" +
        "        }, false);\n" +
        "      }\n" +
        "    }");

      page.locator("input").evaluate("select => {\n" +
        "      window.firedEvents = [];\n" +
        "      for (const event of ['input', 'change']) {\n" +
        "        select.addEventListener(event, e => {\n" +
        "          window.firedEvents.push(e.type + ':' + e.composed);\n" +
        "        }, false);\n" +
        "      }\n" +
        "    }");
      page.locator("input").fill(value);

      assertEquals(Arrays.asList("input:true", "change:false"), page.evaluate("() => window['firedEvents']"));
      assertEquals(Arrays.asList("input:true"), page.evaluate("() => window['firedBodyEvents']"));
    }
  }

  @PlaywrightTest
  @DisplayName("FI-010 should fill time input")
  void shouldFillTimeInput(Page page) {
    page.setContent("<input type=time>");
    page.fill("input", "13:15");
    assertEquals("13:15", page.evalOnSelector("input", "input => input.value"));
  }

  @PlaywrightTest
  @DisplayName("FI-011 should fill month input")
  void shouldFillMonthInput(Page page) {
    page.setContent("<input type=month>");
    page.fill("input", "2020-07");
    assertEquals("2020-07", page.evalOnSelector("input", "input => input.value"));
  }

  @PlaywrightTest
  @DisplayName("FI-012 should fill week input")
  void shouldFillWeekInput(Page page) {
    page.setContent("<input type=week>");
    page.fill("input", "2020-W50");
    assertEquals("2020-W50", page.evalOnSelector("input", "input => input.value"));
  }

  @PlaywrightTest
  @DisplayName("FI-013 should fill datetime-local input")
  @Fixme("driver: the driver (1.64.1) predates the upstream fix of issue 43370: page.fill on input[type=datetime-local] fails with \"Error: Malformed value\"")
  void shouldFillDatetimeLocalInput(Page page, TestServer server, BrowserName browserName) {
    page.setContent("<input type=datetime-local>");
    page.fill("input", "2020-03-02T05:15");
    assertEquals("2020-03-02T05:15", page.evalOnSelector("input", "input => input.value"));
    // The browser omits zero seconds. WebKit on Windows does not implement datetime-local inputs and keeps the value as is.
    page.fill("input", "2020-03-02T06:20:00");
    if (browserName == BrowserName.WEBKIT && Env.isWindows && !"webkit-wsl".equals(Env.channel())) {
      assertEquals("2020-03-02T06:20:00", page.evalOnSelector("input", "input => input.value"));
    } else {
      assertEquals("2020-03-02T06:20", page.evalOnSelector("input", "input => input.value"));
    }
  }

  @PlaywrightTest
  @DisplayName("FI-014 should fill contenteditable")
  void shouldFillContenteditable(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.fill("div[contenteditable]", "some value");
    assertEquals("some value", page.evalOnSelector("div[contenteditable]", "div => div.textContent"));
  }

  @PlaywrightTest
  @DisplayName("FI-015 should fill contenteditable with new lines")
  void shouldFillContenteditableWithNewLines(Page page, TestServer server, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.FIREFOX && !Env.isBidi(), "fixme: Firefox does not handle new lines in contenteditable");

    page.navigate(server.EMPTY_PAGE);
    page.setContent("<div contenteditable=\"true\"></div>");
    page.locator("div[contenteditable]").fill("John\nDoe");
    assertEquals("John\nDoe", page.locator("div[contenteditable]").innerText());
  }

  @PlaywrightTest
  @DisplayName("FI-016 should not double-fill in contenteditable with beforeinput handler in Firefox")
  void shouldNotDoubleFillInContenteditableWithBeforeinputHandlerInFirefox(Page page, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.FIREFOX && !Env.isBidi(), "fixme: https://github.com/microsoft/playwright/issues/36715");

    page.setContent("\n" +
      "    <div id=\"editor\" contenteditable=\"true\"></div>\n" +
      "    <script>\n" +
      "      const editor = document.getElementById('editor');\n" +
      "      editor.addEventListener('beforeinput', (event) => {\n" +
      "        event.preventDefault();\n" +
      "        editor.textContent = event.data;\n" +
      "      });\n" +
      "    </script>\n" +
      "  ");

    Locator locator = page.locator("#editor");
    String testValue = "Playwright";
    locator.fill(testValue);
    assertThat(locator).hasText(testValue);
  }

  @PlaywrightTest
  @DisplayName("FI-017 should fill elements with existing value and selection")
  void shouldFillElementsWithExistingValueAndSelection(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/textarea.html");

    page.evalOnSelector("input", "input => input.value = 'value one'");
    page.fill("input", "another value");
    assertEquals("another value", page.evaluate("() => window['result']"));

    page.evalOnSelector("input", "input => {\n" +
      "    input.selectionStart = 1;\n" +
      "    input.selectionEnd = 2;\n" +
      "  }");
    page.fill("input", "maybe this one");
    assertEquals("maybe this one", page.evaluate("() => window['result']"));

    page.evalOnSelector("div[contenteditable]", "div => {\n" +
      "    div.innerHTML = 'some text <span>some more text<span> and even more text';\n" +
      "    const range = document.createRange();\n" +
      "    range.selectNodeContents(div.querySelector('span'));\n" +
      "    const selection = window.getSelection();\n" +
      "    selection.removeAllRanges();\n" +
      "    selection.addRange(range);\n" +
      "  }");
    page.fill("div[contenteditable]", "replace with this");
    assertEquals("replace with this", page.evalOnSelector("div[contenteditable]", "div => div.textContent"));
  }

  @PlaywrightTest
  @DisplayName("FI-018 should throw nice error without injected script stack when element is not an <input>")
  @Fixme("java: the message is \"Error {\\n  message='Error: Element is not an <input>, <textarea> or [contenteditable] element\\n  name='Error\\n  stack=...\" with an injected script stack, without the \"page.fill: \" prefix and the call log")
  void shouldThrowNiceErrorWithoutInjectedScriptStackWhenElementIsNotAnInput(Page page, TestServer server) {
    page.setContent("<select><option>value1</option></select>");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.fill("select", ""));
    assertTrue(error.getMessage().contains("page.fill: Error: Element is not an <input>, <textarea> or [contenteditable] element\nCall log:"));
  }

  // FI-019 should throw if passed a non-string value: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("FI-020 should retry on disabled element")
  void shouldRetryOnDisabledElement(Page page, TestServer server) throws Exception {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.evalOnSelector("input", "i => i.disabled = true");
    AtomicBoolean done = new AtomicBoolean(false);

    CompletableFuture<Void> promise = async(() -> {
      page.fill("input", "some value");
      done.set(true);
      return null;
    });
    giveItAChanceToFill(page);
    assertEquals(false, done.get());
    assertEquals("", page.evaluate("() => window['result']"));

    page.evalOnSelector("input", "i => i.disabled = false");
    promise.get();
    assertEquals("some value", page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("FI-021 should retry on readonly element")
  void shouldRetryOnReadonlyElement(Page page, TestServer server) throws Exception {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.evalOnSelector("textarea", "i => i.readOnly = true");
    AtomicBoolean done = new AtomicBoolean(false);

    CompletableFuture<Void> promise = async(() -> {
      page.fill("textarea", "some value");
      done.set(true);
      return null;
    });
    giveItAChanceToFill(page);
    assertEquals(false, done.get());
    assertEquals("", page.evaluate("() => window['result']"));

    page.evalOnSelector("textarea", "i => i.readOnly = false");
    promise.get();
    assertEquals("some value", page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("FI-022 should retry on invisible element")
  void shouldRetryOnInvisibleElement(Page page, TestServer server) throws Exception {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.evalOnSelector("input", "i => i.style.display = 'none'");
    AtomicBoolean done = new AtomicBoolean(false);

    CompletableFuture<Void> promise = async(() -> {
      page.fill("input", "some value");
      done.set(true);
      return null;
    });
    giveItAChanceToFill(page);
    assertEquals(false, done.get());
    assertEquals("", page.evaluate("() => window['result']"));

    page.evalOnSelector("input", "i => i.style.display = 'inline'");
    promise.get();
    assertEquals("some value", page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("FI-023 should be able to fill the body")
  void shouldBeAbleToFillTheBody(Page page) {
    page.setContent("<body contentEditable=\"true\"></body>");
    page.fill("body", "some value");
    assertEquals("some value", page.evaluate("() => document.body.textContent"));
  }

  @PlaywrightTest
  @DisplayName("FI-024 should fill fixed position input")
  void shouldFillFixedPositionInput(Page page) {
    page.setContent("<input style='position: fixed;' />");
    page.fill("input", "some value");
    assertEquals("some value", page.evaluate("() => document.querySelector('input').value"));
  }

  @PlaywrightTest
  @DisplayName("FI-025 should be able to fill when focus is in the wrong frame")
  @Fixme("driver: the driver (1.64.1) predates the upstream fix of issue 43483: after page.focus(iframe) the fill of the paragraph in the frame leaves the text \"old\" (expected \"new\")")
  void shouldBeAbleToFillWhenFocusIsInTheWrongFrame(Page page) {
    page.setContent("\n" +
      "    <div contentEditable=\"true\"></div>\n" +
      "    <iframe srcdoc=\"<body contenteditable><p>old</p></body>\"></iframe>\n" +
      "  ");
    page.focus("iframe");
    page.fill("div", "some value");
    assertEquals("some value", page.evalOnSelector("div", "d => d.textContent"));
    Locator paragraph = page.frameLocator("iframe").locator("p");
    paragraph.fill("new");
    assertThat(paragraph).hasText("new");
  }

  @PlaywrightTest
  @DisplayName("FI-026 should be able to fill the input[type=number]")
  void shouldBeAbleToFillTheInputTypeNumber(Page page) {
    page.setContent("<input id=\"input\" type=\"number\"></input>");
    page.fill("input", "42");
    assertEquals("42", page.evaluate("() => window['input'].value"));
  }

  @PlaywrightTest
  @DisplayName("FI-027 should be able to fill exponent into the input[type=number]")
  void shouldBeAbleToFillExponentIntoTheInputTypeNumber(Page page) {
    page.setContent("<input id=\"input\" type=\"number\"></input>");
    page.fill("input", "-10e5");
    assertEquals("-10e5", page.evaluate("() => window['input'].value"));
  }

  @PlaywrightTest
  @DisplayName("FI-028 should be able to fill input[type=number] with empty string")
  void shouldBeAbleToFillInputTypeNumberWithEmptyString(Page page) {
    page.setContent("<input id=\"input\" type=\"number\" value=\"123\"></input>");
    page.fill("input", "");
    assertEquals("", page.evaluate("() => window['input'].value"));
  }

  @PlaywrightTest
  @DisplayName("FI-029 should not be able to fill text into the input[type=number]")
  void shouldNotBeAbleToFillTextIntoTheInputTypeNumber(Page page) {
    page.setContent("<input id=\"input\" type=\"number\"></input>");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.fill("input", "abc"));
    assertTrue(error.getMessage().contains("Cannot type text into input[type=number]"));
  }

  @PlaywrightTest
  @DisplayName("FI-030 should be able to clear using fill()")
  void shouldBeAbleToClearUsingFill(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.fill("input", "some value");
    assertEquals("some value", page.evaluate("() => window['result']"));
    page.fill("input", "");
    assertEquals("", page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("FI-031 should not throw when fill causes navigation")
  void shouldNotThrowWhenFillCausesNavigation(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.setContent("<input type=date>");
    page.evalOnSelector("input", "select => select.addEventListener('input', () => window.location.href = '/empty.html')");
    page.waitForNavigation(() -> page.fill("input", "2020-03-02"));
    assertTrue(page.url().contains("empty.html"));
  }

  @PlaywrightTest
  @DisplayName("FI-032 fill back to back")
  void fillBackToBack(Page page) {
    page.setContent("<input id=\"one\"></input><input id=\"two\"></input>");
    page.fill("id=one", "first value");
    page.fill("id=two", "second value");
    assertThat(page.locator("id=one")).hasValue("first value");
    assertThat(page.locator("id=two")).hasValue("second value");
  }

  @PlaywrightTest
  @DisplayName("FI-033 should fill contenteditable with focus handler that collapses selection")
  void shouldFillContenteditableWithFocusHandlerThatCollapsesSelection(Page page) {
    page.setContent("\n" +
      "    <div contenteditable=\"true\">initial text</div>\n" +
      "    <script>\n" +
      "      const editor = document.querySelector('[contenteditable]');\n" +
      "      editor.addEventListener('focus', () => {\n" +
      "        const selection = window.getSelection();\n" +
      "        if (selection.rangeCount > 0)\n" +
      "          selection.collapseToEnd();\n" +
      "      });\n" +
      "    </script>\n" +
      "  ");

    page.fill("div[contenteditable]", "some value");
    assertEquals("some value", page.locator("div[contenteditable]").textContent());
  }

  @PlaywrightTest
  @DisplayName("FI-034 should retry on inert element")
  @Fixme("driver: the driver (1.64.1) predates the upstream fix of issue 42967: page.fill does not wait for the inert element, it returns before the inert attribute is removed")
  void shouldRetryOnInertElement(Page page) throws Exception {
    page.setContent("\n" +
      "    <div inert id=container>\n" +
      "      <input id=target>\n" +
      "    </div>\n" +
      "    <input id=outside>\n" +
      "  ");
    page.focus("#outside");
    AtomicBoolean done = new AtomicBoolean(false);

    CompletableFuture<Void> promise = async(() -> {
      page.fill("#target", "some value");
      done.set(true);
      return null;
    });
    giveItAChanceToFill(page);
    assertEquals(false, done.get());
    assertEquals("", page.evalOnSelector("#target", "i => i.value"));
    assertEquals("", page.evalOnSelector("#outside", "i => i.value"));

    page.evalOnSelector("#container", "e => e.removeAttribute('inert')");
    promise.get();
    assertEquals("some value", page.evalOnSelector("#target", "i => i.value"));
    assertEquals("", page.evalOnSelector("#outside", "i => i.value"));
  }

  @PlaywrightTest
  @DisplayName("FI-035 should retry on element outside of a modal dialog")
  @Fixme("driver: the driver (1.64.1) predates the upstream fix of issue 42967: page.fill does not wait for an element outside of a modal dialog")
  void shouldRetryOnElementOutsideOfAModalDialog(Page page) throws Exception {
    page.setContent("\n" +
      "    <input id=target>\n" +
      "    <dialog id=dialog><input id=inside></dialog>\n" +
      "    <script>\n" +
      "      document.getElementById('dialog').showModal();\n" +
      "    </script>\n" +
      "  ");
    AtomicBoolean done = new AtomicBoolean(false);

    page.fill("#inside", "inside value");
    assertEquals("inside value", page.evalOnSelector("#inside", "i => i.value"));

    CompletableFuture<Void> promise = async(() -> {
      page.fill("#target", "some value");
      done.set(true);
      return null;
    });
    giveItAChanceToFill(page);
    assertEquals(false, done.get());
    assertEquals("", page.evalOnSelector("#target", "i => i.value"));

    page.evalOnSelector("#dialog", "e => e.close()");
    promise.get();
    assertEquals("some value", page.evalOnSelector("#target", "i => i.value"));
  }

  @PlaywrightTest
  @DisplayName("FI-036 should fail with a nice error on inert element")
  @Fixme("driver: the driver (1.64.1) predates the upstream fix of issue 42967: page.fill on an inert element does not time out")
  void shouldFailWithANiceErrorOnInertElement(Page page) {
    page.setContent("<div inert><input></div>");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.fill("input", "some value", new Page.FillOptions().setTimeout(1000)));
    assertTrue(error.getMessage().contains("Timeout 1000ms exceeded"));
    assertTrue(error.getMessage().contains("element is inert"));
  }
}
