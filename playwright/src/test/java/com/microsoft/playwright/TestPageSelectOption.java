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

import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.options.SelectOption;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;

public class TestPageSelectOption {
  @PlaywrightTest
  void shouldSelectSingleOption(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.selectOption("select", "blue");
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onChange"));
  }

  @PlaywrightTest
  void shouldSelectSingleOptionByValue(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.selectOption("select", new SelectOption().setValue("blue"));
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onChange"));
  }

  @PlaywrightTest
  void shouldFallBackToSelectingByLabel(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.selectOption("select", "Blue");
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onChange"));
  }

  @PlaywrightTest
  void shouldSelectSingleOptionByLabel(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.selectOption("select", new SelectOption().setLabel("Indigo"));
    assertEquals(asList("indigo"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("indigo"), page.evaluate("() => window['result'].onChange"));
  }

  @PlaywrightTest
  void shouldSelectSingleOptionByHandle(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.selectOption("select", page.querySelector("[id=whiteOption]"));
    assertEquals(asList("white"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("white"), page.evaluate("() => window['result'].onChange"));
  }

  @PlaywrightTest
  void shouldSelectSingleOptionByIndex(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.selectOption("select", new SelectOption().setIndex(2));
    assertEquals(asList("brown"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("brown"), page.evaluate("() => window['result'].onChange"));
  }

  @PlaywrightTest
  void shouldSelectSingleOptionByMultipleAttributes(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.selectOption("select", new SelectOption().setValue("green").setLabel("Green"));
    assertEquals(asList("green"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("green"), page.evaluate("() => window['result'].onChange"));
  }

  @PlaywrightTest
  void shouldNotSelectSingleOptionWhenSomeAttributesDoNotMatch(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.evalOnSelector("select", "s => s.value = undefined");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.selectOption("select", new SelectOption()
        .setValue("green").setLabel("Brown"), new Page.SelectOptionOptions().setTimeout(300));
    });
    assertTrue(e.getMessage().contains("Timeout"));
    assertEquals("", page.evaluate("() => document.querySelector('select').value"));
  }

  @PlaywrightTest
  void shouldSelectOnlyFirstOption(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.selectOption("select", new String[]{"blue", "green", "red"});
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onChange"));
  }

  @PlaywrightTest
  void shouldNotThrowWhenSelectCausesNavigation(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.evalOnSelector("select", "select => select.addEventListener('input', () => window.location.href = '/empty.html')");
    page.waitForNavigation(() -> page.selectOption("select", "blue"));
    assertTrue(page.url().contains("empty.html"));
  }

  @PlaywrightTest
  void shouldSelectMultipleOptions(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.evaluate("() => window['makeMultiple']()");
    page.selectOption("select", new String[]{"blue", "green", "red"});
    assertEquals(asList("blue", "green", "red"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("blue", "green", "red"), page.evaluate("() => window['result'].onChange"));
  }

  @PlaywrightTest
  void shouldSelectMultipleOptionsWithAttributes(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.evaluate("() => window['makeMultiple']()");
    page.selectOption("select", new SelectOption[] {
      new SelectOption().setValue("blue"),
      new SelectOption().setLabel("Green"),
      new SelectOption().setIndex(4),
    });
    assertEquals(asList("blue", "gray", "green"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("blue", "gray", "green"), page.evaluate("() => window['result'].onChange"));
  }

  @PlaywrightTest
  void shouldRespectEventBubbling(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.selectOption("select", "blue");
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onBubblingInput"));
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onBubblingChange"));
  }

  @PlaywrightTest
  void shouldThrowWhenElementIsNotASelect(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/select.html");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.selectOption("body", ""));
    assertTrue(e.getMessage().contains("Element is not a <select> element"), e.getMessage());
  }

  @PlaywrightTest
  void shouldReturnOnNoMatchedValues(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/select.html");
    List<String> result = page.selectOption("select", new String[]{});
    assertEquals(emptyList(), result);
  }

  @PlaywrightTest
  void shouldReturnAnArrayOfMatchedValues(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.evaluate("() => window['makeMultiple']()");
    List<String> result = page.selectOption("select", new String[]{"blue", "black", "magenta"});
    Collections.sort(result);
    List<String> expected = asList("blue","black","magenta");
    Collections.sort(expected);
    assertEquals(expected, result);
  }

  @PlaywrightTest
  void shouldReturnAnArrayOfOneElementWhenMultipleIsNotSet(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/select.html");
    List<String> result = page.selectOption("select", new String[]{"42", "blue", "black", "magenta"});
    assertEquals(1, result.size());
  }

  @PlaywrightTest
  void shouldReturnOnNoValues(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/select.html");
    Object result = page.selectOption("select", new String[0]);
    assertEquals(emptyList(), result);
  }

//  @Test
  void shouldNotAllowNullItems(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.evaluate("() => window['makeMultiple']()");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.selectOption("select", new String[]{"blue", null, "black","magenta"});
    });
    assertTrue(e.getMessage().contains("options.get(1): expected object, got null"));
  }

  @PlaywrightTest
  void shouldUnselectWithNull(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.evaluate("() => window['makeMultiple']()");
    List<String> result = page.selectOption("select", new String[]{"blue", "black", "magenta"});
    Collections.sort(result);
    List<String> expected = asList("blue","black","magenta");
    Collections.sort(expected);
    assertEquals(expected, result);
    page.selectOption("select", (ElementHandle[]) null);
    assertEquals(true, page.evalOnSelector("select", "select => Array.from(select.options).every(option => !option.selected)"));
    page.selectOption("select", (String[]) null);
    assertEquals(true, page.evalOnSelector("select", "select => Array.from(select.options).every(option => !option.selected)"));
    page.selectOption("select", (SelectOption[]) null);
    assertEquals(true, page.evalOnSelector("select", "select => Array.from(select.options).every(option => !option.selected)"));
  }

  @PlaywrightTest
  void shouldDeselectAllOptionsWhenPassedNoValuesForAMultipleSelect(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.evaluate("() => window['makeMultiple']()");
    page.selectOption("select", new String[]{"blue", "black", "magenta"});
    page.selectOption("select", new String[0]);
    assertEquals(true, page.evalOnSelector("select", "select => Array.from(select.options).every(option => !option.selected)"));
  }

  @PlaywrightTest
  void shouldDeselectAllOptionsWhenPassedNoValuesForASelectWithoutMultiple(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.selectOption("select", new String[]{"blue", "black", "magenta"});
    page.selectOption("select", new String[0]);
    assertEquals(true, page.evalOnSelector("select", "select => Array.from(select.options).every(option => !option.selected)"));
  }

  void shouldThrowIfPassedWrongTypes() {
    // Checked by compiler in Java.
  }

  // @see https://github.com/GoogleChrome/puppeteer/issues/3327
  @PlaywrightTest
  void shouldWorkWhenReDefiningTopLevelEventClass(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.evaluate("() => window.Event = null");
    page.selectOption("select", "blue");
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onChange"));
  }

  @PlaywrightTest
  void shouldWaitForOptionToBeEnabled(Page page) {
    page.setContent(
      "<select disabled>\n" +
      "  <option>one</option>\n" +
      "  <option>two</option>\n" +
      "</select>\n" +
      "\n" +
      "<script>\n" +
      "function hydrate() {\n" +
      "  const select = document.querySelector('select');\n" +
      "  select.removeAttribute('disabled');\n" +
      "  select.addEventListener('change', () => {\n" +
      "    window['result'] = select.value;\n" +
      "  });\n" +
      "}\n" +
      "</script>");

    page.evaluate("() => setTimeout(hydrate, 1000)");
    page.locator("select").selectOption("two");

    assertEquals("two", page.evaluate("window['result']"));
    assertThat(page.locator("select")).hasValue("two");
  }

  @PlaywrightTest
  void shouldWaitForSelectToBeSwapped(Page page) {
    page.setContent(
      "<select disabled>\n" +
      "  <option>one</option>\n" +
      "  <option>two</option>\n" +
      "</select>\n" +
      "\n" +
      "<script>\n" +
      "function hydrate() {\n" +
      "  const select = document.querySelector('select');\n" +
      "  select.remove();\n" +
      "\n" +
      "  const newSelect = document.createElement('select');\n" +
      "  const option1 = document.createElement('option');\n" +
      "  option1.textContent = 'one';\n" +
      "  newSelect.appendChild(option1);\n" +
      "  const option2 = document.createElement('option');\n" +
      "  option2.textContent = 'two';\n" +
      "  newSelect.appendChild(option2);\n" +
      "\n" +
      "  document.body.appendChild(newSelect);\n" +
      "\n" +
      "  newSelect.addEventListener('change', () => {\n" +
      "    window['result'] = newSelect.value;\n" +
      "  });\n" +
      "}\n" +
      "</script>");

    page.evaluate("() => setTimeout(window.hydrate, 1000)");
    page.locator("select").selectOption("two");

    assertThat(page.locator("select")).hasValue("two");
    assertEquals("two", page.evaluate("window['result']"));
  }
}
