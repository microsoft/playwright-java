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

import com.microsoft.playwright.options.SelectOption;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;

public class TestPageSelectOption extends TestBase {
  @Test
  void shouldSelectSingleOption() {
    page.navigate(server.PREFIX + "/input/select.html");
    page.selectOption("select", "blue");
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onChange"));
  }

  @Test
  void shouldSelectSingleOptionByValue() {
    page.navigate(server.PREFIX + "/input/select.html");
    page.selectOption("select", new SelectOption().setValue("blue"));
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onChange"));
  }

  @Test
  void shouldFallBackToSelectingByLabel() {
    page.navigate(server.PREFIX + "/input/select.html");
    page.selectOption("select", "Blue");
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onChange"));
  }

  @Test
  void shouldSelectSingleOptionByLabel() {
    page.navigate(server.PREFIX + "/input/select.html");
    page.selectOption("select", new SelectOption().setLabel("Indigo"));
    assertEquals(asList("indigo"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("indigo"), page.evaluate("() => window['result'].onChange"));
  }

  @Test
  void shouldSelectSingleOptionByHandle() {
    page.navigate(server.PREFIX + "/input/select.html");
    page.selectOption("select", page.querySelector("[id=whiteOption]"));
    assertEquals(asList("white"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("white"), page.evaluate("() => window['result'].onChange"));
  }

  @Test
  void shouldSelectSingleOptionByIndex() {
    page.navigate(server.PREFIX + "/input/select.html");
    page.selectOption("select", new SelectOption().setIndex(2));
    assertEquals(asList("brown"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("brown"), page.evaluate("() => window['result'].onChange"));
  }

  @Test
  void shouldSelectSingleOptionByMultipleAttributes() {
    page.navigate(server.PREFIX + "/input/select.html");
    page.selectOption("select", new SelectOption().setValue("green").setLabel("Green"));
    assertEquals(asList("green"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("green"), page.evaluate("() => window['result'].onChange"));
  }

  @Test
  void shouldNotSelectSingleOptionWhenSomeAttributesDoNotMatch() {
    page.navigate(server.PREFIX + "/input/select.html");
    page.evalOnSelector("select", "s => s.value = undefined");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.selectOption("select", new SelectOption()
        .setValue("green").setLabel("Brown"), new Page.SelectOptionOptions().setTimeout(300));
    });
    assertTrue(e.getMessage().contains("Timeout"));
    assertEquals("", page.evaluate("() => document.querySelector('select').value"));
  }

  @Test
  void shouldSelectOnlyFirstOption() {
    page.navigate(server.PREFIX + "/input/select.html");
    page.selectOption("select", new String[]{"blue", "green", "red"});
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onChange"));
  }

  @Test
  void shouldNotThrowWhenSelectCausesNavigation() {
    page.navigate(server.PREFIX + "/input/select.html");
    page.evalOnSelector("select", "select => select.addEventListener('input', () => window.location.href = '/empty.html')");
    page.waitForNavigation(() -> page.selectOption("select", "blue"));
    assertTrue(page.url().contains("empty.html"));
  }

  @Test
  void shouldSelectMultipleOptions() {
    page.navigate(server.PREFIX + "/input/select.html");
    page.evaluate("() => window['makeMultiple']()");
    page.selectOption("select", new String[]{"blue", "green", "red"});
    assertEquals(asList("blue", "green", "red"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("blue", "green", "red"), page.evaluate("() => window['result'].onChange"));
  }

  @Test
  void shouldSelectMultipleOptionsWithAttributes() {
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

  @Test
  void shouldRespectEventBubbling() {
    page.navigate(server.PREFIX + "/input/select.html");
    page.selectOption("select", "blue");
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onBubblingInput"));
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onBubblingChange"));
  }

  @Test
  void shouldThrowWhenElementIsNotASelect() {
    page.navigate(server.PREFIX + "/input/select.html");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.selectOption("body", ""));
    assertTrue(e.getMessage().contains("Element is not a <select> element"), e.getMessage());
  }

  @Test
  void shouldReturnOnNoMatchedValues() {
    page.navigate(server.PREFIX + "/input/select.html");
    List<String> result = page.selectOption("select", new String[]{});
    assertEquals(emptyList(), result);
  }

  @Test
  void shouldReturnAnArrayOfMatchedValues() {
    page.navigate(server.PREFIX + "/input/select.html");
    page.evaluate("() => window['makeMultiple']()");
    List<String> result = page.selectOption("select", new String[]{"blue", "black", "magenta"});
    Collections.sort(result);
    List<String> expected = asList("blue","black","magenta");
    Collections.sort(expected);
    assertEquals(expected, result);
  }

  @Test
  void shouldReturnAnArrayOfOneElementWhenMultipleIsNotSet() {
    page.navigate(server.PREFIX + "/input/select.html");
    List<String> result = page.selectOption("select", new String[]{"42", "blue", "black", "magenta"});
    assertEquals(1, result.size());
  }

  @Test
  void shouldReturnOnNoValues() {
    page.navigate(server.PREFIX + "/input/select.html");
    Object result = page.selectOption("select", new String[0]);
    assertEquals(emptyList(), result);
  }

//  @Test
  void shouldNotAllowNullItems() {
    page.navigate(server.PREFIX + "/input/select.html");
    page.evaluate("() => window['makeMultiple']()");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.selectOption("select", new String[]{"blue", null, "black","magenta"});
    });
    assertTrue(e.getMessage().contains("options.get(1): expected object, got null"));
  }

  @Test
  void shouldUnselectWithNull() {
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

  @Test
  void shouldDeselectAllOptionsWhenPassedNoValuesForAMultipleSelect() {
    page.navigate(server.PREFIX + "/input/select.html");
    page.evaluate("() => window['makeMultiple']()");
    page.selectOption("select", new String[]{"blue", "black", "magenta"});
    page.selectOption("select", new String[0]);
    assertEquals(true, page.evalOnSelector("select", "select => Array.from(select.options).every(option => !option.selected)"));
  }

  @Test
  void shouldDeselectAllOptionsWhenPassedNoValuesForASelectWithoutMultiple() {
    page.navigate(server.PREFIX + "/input/select.html");
    page.selectOption("select", new String[]{"blue", "black", "magenta"});
    page.selectOption("select", new String[0]);
    assertEquals(true, page.evalOnSelector("select", "select => Array.from(select.options).every(option => !option.selected)"));
  }

  void shouldThrowIfPassedWrongTypes() {
    // Checked by compiler in Java.
  }

  // @see https://github.com/GoogleChrome/puppeteer/issues/3327
  @Test
  void shouldWorkWhenReDefiningTopLevelEventClass() {
    page.navigate(server.PREFIX + "/input/select.html");
    page.evaluate("() => window.Event = null");
    page.selectOption("select", "blue");
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onChange"));
  }

}
