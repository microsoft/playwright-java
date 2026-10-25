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

import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.BoundingBox;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-check.spec.ts. */
public class TestPageCheck {
  @PlaywrightTest
  @DisplayName("CH-001 should check the box @smoke")
  void shouldCheckTheBox(Page page) {
    page.setContent("<input id='checkbox' type='checkbox'></input>");
    page.check("input");
    assertEquals(true, page.evaluate("() => window['checkbox'].checked"));
  }

  @PlaywrightTest
  @DisplayName("CH-002 should not check the checked box")
  void shouldNotCheckTheCheckedBox(Page page) {
    page.setContent("<input id='checkbox' type='checkbox' checked></input>");
    page.check("input");
    assertEquals(true, page.evaluate("() => window['checkbox'].checked"));
  }

  @PlaywrightTest
  @DisplayName("CH-003 should uncheck the box")
  void shouldUncheckTheBox(Page page) {
    page.setContent("<input id='checkbox' type='checkbox' checked></input>");
    page.uncheck("input");
    assertEquals(false, page.evaluate("() => window['checkbox'].checked"));
  }

  @PlaywrightTest
  @DisplayName("CH-004 should not uncheck the unchecked box")
  void shouldNotUncheckTheUncheckedBox(Page page) {
    page.setContent("<input id='checkbox' type='checkbox'></input>");
    page.uncheck("input");
    assertEquals(false, page.evaluate("() => window['checkbox'].checked"));
  }

  @PlaywrightTest
  @DisplayName("CH-005 should check radio")
  void shouldCheckRadio(Page page) {
    page.setContent("\n" +
      "    <input type='radio'>one</input>\n" +
      "    <input id='two' type='radio'>two</input>\n" +
      "    <input type='radio'>three</input>");
    page.check("#two");
    assertEquals(true, page.evaluate("() => window['two'].checked"));
  }

  @PlaywrightTest
  @DisplayName("CH-006 should check radio by aria role")
  void shouldCheckRadioByAriaRole(Page page) {
    page.setContent("<div role='radio' id='checkbox'>CHECKBOX</div>\n" +
      "    <script>\n" +
      "      checkbox.addEventListener('click', () => checkbox.setAttribute('aria-checked', 'true'));\n" +
      "    </script>");
    page.check("div");
    assertEquals("true", page.evaluate("() => window['checkbox'].getAttribute('aria-checked')"));
  }

  @PlaywrightTest
  @DisplayName("CH-007 should uncheck radio by aria role")
  void shouldUncheckRadioByAriaRole(Page page) {
    page.setContent("<div role='radio' id='checkbox' aria-checked=\"true\">CHECKBOX</div>\n" +
      "    <script>\n" +
      "      checkbox.addEventListener('click', () => checkbox.setAttribute('aria-checked', 'false'));\n" +
      "    </script>");
    page.uncheck("div");
    assertEquals("false", page.evaluate("() => window['checkbox'].getAttribute('aria-checked')"));
  }

  @PlaywrightTest
  @DisplayName("CH-008 should check the box by aria role")
  void shouldCheckTheBoxByAriaRole(Page page) {
    for (String role : new String[]{"checkbox", "menuitemcheckbox", "option", "radio", "switch", "menuitemradio", "treeitem"}) {
      // it.step(`role=${role}`)
      page.setContent("<div role='" + role + "' id='checkbox'>CHECKBOX</div>\n" +
        "        <script>\n" +
        "          checkbox.addEventListener('click', () => checkbox.setAttribute('aria-checked', 'true'));\n" +
        "        </script>");
      page.check("div");
      assertEquals("true", page.evaluate("() => window['checkbox'].getAttribute('aria-checked')"));
    }
  }

  @PlaywrightTest
  @DisplayName("CH-009 should uncheck the box by aria role")
  void shouldUncheckTheBoxByAriaRole(Page page) {
    for (String role : new String[]{"checkbox", "menuitemcheckbox", "option", "radio", "switch", "menuitemradio", "treeitem"}) {
      // it.step(`role=${role}`)
      page.setContent("<div role='" + role + "' id='checkbox' aria-checked=\"true\">CHECKBOX</div>\n" +
        "        <script>\n" +
        "          checkbox.addEventListener('click', () => checkbox.setAttribute('aria-checked', 'false'));\n" +
        "        </script>");
      page.uncheck("div");
      assertEquals("false", page.evaluate("() => window['checkbox'].getAttribute('aria-checked')"));
    }
  }

  @PlaywrightTest
  @DisplayName("CH-010 should throw when not a checkbox")
  void shouldThrowWhenNotACheckbox(Page page) {
    page.setContent("<div>Check me</div>");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.check("div"));
    assertTrue(error.getMessage().contains("Not a checkbox or radio button"));
  }

  @PlaywrightTest
  @DisplayName("CH-011 should throw when not a checkbox 2")
  void shouldThrowWhenNotACheckbox2(Page page) {
    page.setContent("<div role=button>Check me</div>");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.check("div"));
    assertTrue(error.getMessage().contains("Not a checkbox or radio button"));
  }

  @PlaywrightTest
  @DisplayName("CH-012 should check the box inside a button")
  void shouldCheckTheBoxInsideAButton(Page page) {
    page.setContent("<div role='button'><input type='checkbox'></div>");
    page.check("input");
    assertEquals(true, page.evalOnSelector("input", "input => input.checked"));
    assertEquals(true, page.isChecked("input"));
    assertEquals(true, page.querySelector("input").isChecked());
  }

  @PlaywrightTest
  @DisplayName("CH-013 should check the label with position")
  void shouldCheckTheLabelWithPosition(Page page, TestServer server) {
    page.setContent("\n" +
      "    <input id='checkbox' type='checkbox' style='width: 5px; height: 5px;'>\n" +
      "    <label for='checkbox'>\n" +
      "      <a href=\"" + server.EMPTY_PAGE + "\">I am a long link that goes away so that nothing good will happen if you click on me</a>\n" +
      "      Click me\n" +
      "    </label>");
    BoundingBox box = page.querySelector("text=Click me").boundingBox();
    page.check("text=Click me", new Page.CheckOptions().setPosition(box.width - 10, 2));
    assertEquals(true, page.evalOnSelector("input", "input => input.checked"));
  }

  @PlaywrightTest
  @DisplayName("CH-014 trial run should not check")
  void trialRunShouldNotCheck(Page page) {
    page.setContent("<input id='checkbox' type='checkbox'></input>");
    page.check("input", new Page.CheckOptions().setTrial(true));
    assertEquals(false, page.evaluate("() => window['checkbox'].checked"));
  }

  @PlaywrightTest
  @DisplayName("CH-015 trial run should not uncheck")
  void trialRunShouldNotUncheck(Page page) {
    page.setContent("<input id='checkbox' type='checkbox' checked></input>");
    page.uncheck("input", new Page.UncheckOptions().setTrial(true));
    assertEquals(true, page.evaluate("() => window['checkbox'].checked"));
  }

  @PlaywrightTest
  @DisplayName("CH-016 should check the box using setChecked")
  void shouldCheckTheBoxUsingSetChecked(Page page) {
    page.setContent("<input id='checkbox' type='checkbox'></input>");
    page.setChecked("input", true);
    assertEquals(true, page.evaluate("() => window['checkbox'].checked"));
    page.setChecked("input", false);
    assertEquals(false, page.evaluate("() => window['checkbox'].checked"));
  }

  @PlaywrightTest
  @DisplayName("CH-017 should throw when trying to uncheck radio button")
  void shouldThrowWhenTryingToUncheckRadioButton(Page page) {
    page.setContent("<input type='radio' name='test' checked id='radio'>");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.uncheck("#radio"));
    assertTrue(error.getMessage().contains("Cannot uncheck radio button"));
  }
}
