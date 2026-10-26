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

import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.DisplayName;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/locator-convenience.spec.ts. */
public class TestLocatorConvenience {
  @PlaywrightTest
  @DisplayName("LO-057 should have a nice preview")
  @Fixme("java: Locator.toString() is 'Locator@#outer', not \"locator('#outer')\"; the JSHandle preview is 'JSHandle@node'")
  void shouldHaveANicePreview(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/dom.html");
    Locator outer = page.locator("#outer");
    Locator inner = outer.locator("#inner");
    Locator check = page.locator("#check");
    JSHandle text = inner.evaluateHandle("e => e.firstChild");
    page.evaluate("() => 1");  // Give them a chance to calculate the preview.
    // expect.soft
    assertAll(
      () -> assertEquals("locator('#outer')", String.valueOf(outer)),
      () -> assertEquals("locator('#outer').locator('#inner')", String.valueOf(inner)),
      () -> assertEquals("JSHandle@#text=Text,↵more text", String.valueOf(text)),
      () -> assertEquals("locator('#check')", String.valueOf(check)));
  }

  @PlaywrightTest
  @DisplayName("LO-058 getAttribute should work")
  void getAttributeShouldWork(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/dom.html");
    Locator locator = page.locator("#outer");
    assertEquals("value", locator.getAttribute("name"));
    assertNull(locator.getAttribute("foo"));
    assertEquals("value", page.getAttribute("#outer", "name"));
    assertNull(page.getAttribute("#outer", "foo"));
  }

  @PlaywrightTest
  @DisplayName("LO-059 inputValue should work")
  void inputValueShouldWork(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/dom.html");

    page.selectOption("#select", "foo");
    assertEquals("foo", page.inputValue("#select"));

    page.fill("#textarea", "text value");
    assertEquals("text value", page.inputValue("#textarea"));

    page.fill("#input", "input value");
    assertEquals("input value", page.inputValue("#input"));
    Locator locator = page.locator("#input");
    assertEquals("input value", locator.inputValue());

    PlaywrightException error1 = assertThrows(PlaywrightException.class, () -> page.inputValue("#inner"));
    assertTrue(error1.getMessage().contains("Node is not an <input>, <textarea> or <select> element"));
    Locator locator2 = page.locator("#inner");
    PlaywrightException error2 = assertThrows(PlaywrightException.class, () -> locator2.inputValue());
    assertTrue(error2.getMessage().contains("Node is not an <input>, <textarea> or <select> element"));
  }

  @PlaywrightTest
  @DisplayName("LO-060 innerHTML should work")
  void innerHTMLShouldWork(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/dom.html");
    Locator locator = page.locator("#outer");
    assertEquals("<div id=\"inner\">Text,\nmore text</div>", locator.innerHTML());
    assertEquals("<div id=\"inner\">Text,\nmore text</div>", page.innerHTML("#outer"));
  }

  @PlaywrightTest
  @DisplayName("LO-061 innerText should work")
  void innerTextShouldWork(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/dom.html");
    Locator locator = page.locator("#inner");
    assertEquals("Text, more text", locator.innerText());
    assertEquals("Text, more text", page.innerText("#inner"));
  }

  @PlaywrightTest
  @DisplayName("LO-062 innerText should throw")
  void innerTextShouldThrow(Page page, TestServer server) {
    page.setContent("<svg>text</svg>");
    PlaywrightException error1 = assertThrows(PlaywrightException.class, () -> page.innerText("svg"));
    assertTrue(error1.getMessage().contains("Node is not an HTMLElement"));
    Locator locator = page.locator("svg");
    PlaywrightException error2 = assertThrows(PlaywrightException.class, () -> locator.innerText());
    assertTrue(error2.getMessage().contains("Node is not an HTMLElement"));
  }

  @PlaywrightTest
  @DisplayName("LO-063 innerText should produce log")
  @Fixme("java: the call log says 'waiting for locator(\"span\")' with double quotes, not \"waiting for locator('span')\"")
  void innerTextShouldProduceLog(Page page, TestServer server) {
    page.setContent("<div>Hello</div>");
    Locator locator = page.locator("span");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> locator.innerText(new Locator.InnerTextOptions().setTimeout(1000)));
    assertTrue(error.getMessage().contains("waiting for locator('span')"));
  }

  @PlaywrightTest
  @DisplayName("LO-064 textContent should work")
  void textContentShouldWork(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/dom.html");
    Locator locator = page.locator("#inner");
    assertEquals("Text,\nmore text", locator.textContent());
    assertEquals("Text,\nmore text", page.textContent("#inner"));
  }

  @PlaywrightTest
  @DisplayName("LO-065 isEnabled and isDisabled should work")
  void isEnabledAndIsDisabledShouldWork(Page page) {
    page.setContent("\n" +
      "    <button disabled>button1</button>\n" +
      "    <button>button2</button>\n" +
      "    <div>div</div>\n" +
      "  ");
    Locator div = page.locator("div");
    assertEquals(true, div.isEnabled());
    assertEquals(false, div.isDisabled());
    assertEquals(true, page.isEnabled("div"));
    assertEquals(false, page.isDisabled("div"));
    Locator button1 = page.locator(":text(\"button1\")");
    assertEquals(false, button1.isEnabled());
    assertEquals(true, button1.isDisabled());
    assertEquals(false, page.isEnabled(":text(\"button1\")"));
    assertEquals(true, page.isDisabled(":text(\"button1\")"));
    Locator button2 = page.locator(":text(\"button2\")");
    assertEquals(true, button2.isEnabled());
    assertEquals(false, button2.isDisabled());
    assertEquals(true, page.isEnabled(":text(\"button2\")"));
    assertEquals(false, page.isDisabled(":text(\"button2\")"));
  }

  @PlaywrightTest
  @DisplayName("LO-066 isEditable should work")
  void isEditableShouldWork(Page page) {
    page.setContent("\n" +
      "    <input id=input1 disabled>\n" +
      "    <textarea></textarea>\n" +
      "    <input id=input2>\n" +
      "    <div contenteditable=\"true\"></div>\n" +
      "    <span id=span1 role=textbox aria-readonly=true></span>\n" +
      "    <span id=span2 role=textbox></span>\n" +
      "    <button>button</button>\n" +
      "  ");
    page.evalOnSelector("textarea", "t => t.readOnly = true");
    Locator input1 = page.locator("#input1");
    assertEquals(false, input1.isEditable());
    assertEquals(false, page.isEditable("#input1"));
    Locator input2 = page.locator("#input2");
    assertEquals(true, input2.isEditable());
    assertEquals(true, page.isEditable("#input2"));
    Locator textarea = page.locator("textarea");
    assertEquals(false, textarea.isEditable());
    assertEquals(false, page.isEditable("textarea"));
    assertEquals(true, page.locator("div").isEditable());
    assertEquals(false, page.locator("#span1").isEditable());
    assertEquals(true, page.locator("#span2").isEditable());
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.locator("button").isEditable());
    assertTrue(error.getMessage().contains("Element is not an <input>, <textarea>, <select> or [contenteditable] and does not have a role allowing [aria-readonly]"));
  }

  @PlaywrightTest
  @DisplayName("LO-067 isChecked should work")
  void isCheckedShouldWork(Page page) {
    page.setContent("<input type='checkbox' checked><div>Not a checkbox</div>");
    Locator element = page.locator("input");
    assertEquals(true, element.isChecked());
    assertEquals(true, page.isChecked("input"));
    element.evaluate("input => input.checked = false");
    assertEquals(false, element.isChecked());
    assertEquals(false, page.isChecked("input"));
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.isChecked("div"));
    assertTrue(error.getMessage().contains("Not a checkbox or radio button"));
  }

  @PlaywrightTest
  @DisplayName("LO-068 isChecked should work for indeterminate input")
  void isCheckedShouldWorkForIndeterminateInput(Page page) {

    page.setContent("<input type=\"checkbox\" checked>");
    page.locator("input").evaluate("e => e.indeterminate = true");

    assertEquals(true, page.locator("input").isChecked());
    assertThat(page.locator("input")).isChecked();

    page.locator("input").uncheck();

    assertEquals(false, page.locator("input").isChecked());
    assertThat(page.locator("input")).not().isChecked();
  }

  @PlaywrightTest
  @DisplayName("LO-069 allTextContents should work")
  void allTextContentsShouldWork(Page page) {
    page.setContent("<div>A</div><div>B</div><div>C</div>");
    assertEquals(asList("A", "B", "C"), page.locator("div").allTextContents());
  }

  @PlaywrightTest
  @DisplayName("LO-070 allInnerTexts should work")
  void allInnerTextsShouldWork(Page page) {
    page.setContent("<div>A</div><div>B</div><div>C</div>");
    assertEquals(asList("A", "B", "C"), page.locator("div").allInnerTexts());
  }

  @PlaywrightTest
  @DisplayName("LO-071 should return page")
  void shouldReturnPage(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/frames/two-frames.html");
    Locator outer = page.locator("#outer");
    assertEquals(page, outer.page());

    Locator inner = outer.locator("#inner");
    assertEquals(page, inner.page());

    Locator inFrame = page.frames().get(1).locator("div");
    assertEquals(page, inFrame.page());
  }

  @PlaywrightTest
  @DisplayName("LO-072 description should return null for locator without description")
  void descriptionShouldReturnNullForLocatorWithoutDescription(Page page) {
    Locator locator = page.locator("button");
    assertNull(locator.description());
  }

  @PlaywrightTest
  @DisplayName("LO-073 description should return description for locator with simple description")
  void descriptionShouldReturnDescriptionForLocatorWithSimpleDescription(Page page) {
    Locator locator = page.locator("button").describe("Submit button");
    assertEquals("Submit button", locator.description());
  }

  @PlaywrightTest
  @DisplayName("LO-074 description should return description with special characters")
  void descriptionShouldReturnDescriptionWithSpecialCharacters(Page page) {
    Locator locator = page.locator("div").describe("Button with \"quotes\" and 'apostrophes'");
    assertEquals("Button with \"quotes\" and 'apostrophes'", locator.description());
  }

  @PlaywrightTest
  @DisplayName("LO-075 description should return description for chained locators")
  void descriptionShouldReturnDescriptionForChainedLocators(Page page) {
    Locator locator = page.locator("form").locator("input").describe("Form input field");
    assertEquals("Form input field", locator.description());
  }

  @PlaywrightTest
  @DisplayName("LO-076 description should return description for locator with multiple describe calls")
  void descriptionShouldReturnDescriptionForLocatorWithMultipleDescribeCalls(Page page) {
    Locator locator1 = page.locator("foo").describe("First description");
    assertEquals("First description", locator1.description());
    Locator locator2 = locator1.locator("button").describe("Second description");
    assertEquals("Second description", locator2.description());
    Locator locator3 = locator2.locator("button");
    assertNull(locator3.description());
  }

  @PlaywrightTest
  @DisplayName("LO-077 toString() returns formatted locator")
  @Fixme("java: Locator.toString() is 'Locator@internal:role=button[name=\"Submit\"i]', not \"getByRole('button', { name: 'Submit' })\"")
  void toStringReturnsFormattedLocator(Page page) {
    Locator locator = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Submit"));
    assertEquals("getByRole('button', { name: 'Submit' })", locator.toString());
    assertNull(locator.description());
  }

  @PlaywrightTest
  @DisplayName("LO-078 toString() prefers description")
  void toStringPrefersDescription(Page page) {
    Locator locator = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Submit")).describe("Submit button");
    assertEquals("Submit button", locator.toString());
    assertEquals(locator.description(), locator.toString());
  }
}
