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

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.SelectOption;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.async;
import static com.microsoft.playwright.fixtures.Utils.rafraf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-select-option.spec.ts. */
public class TestPageSelectOption {
  private static void giveItAChanceToResolve(Page page) {
    rafraf(page, 5);
  }

  @PlaywrightTest
  @DisplayName("SO-001 should select single option @smoke")
  void shouldSelectSingleOption(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.selectOption("select", "blue");
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onChange"));
  }

  @PlaywrightTest
  @DisplayName("SO-002 should select single option by value")
  void shouldSelectSingleOptionByValue(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.selectOption("select", new SelectOption().setValue("blue"));
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onChange"));
  }

  @PlaywrightTest
  @DisplayName("SO-003 should fall back to selecting by label")
  void shouldFallBackToSelectingByLabel(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.selectOption("select", "Blue");
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onChange"));
  }

  @PlaywrightTest
  @DisplayName("SO-004 should select single option by label")
  void shouldSelectSingleOptionByLabel(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.selectOption("select", new SelectOption().setLabel("Indigo"));
    assertEquals(asList("indigo"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("indigo"), page.evaluate("() => window['result'].onChange"));
  }

  @PlaywrightTest
  @DisplayName("SO-005 should select single option by label with html whitespace")
  void shouldSelectSingleOptionByLabelWithHtmlWhitespace(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.selectOption("select", new SelectOption().setLabel("HTML"));
    assertEquals(asList("html"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("html"), page.evaluate("() => window['result'].onChange"));
  }

  @PlaywrightTest
  @DisplayName("SO-006 should select single option by handle")
  void shouldSelectSingleOptionByHandle(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.selectOption("select", page.querySelector("[id=whiteOption]"));
    assertEquals(asList("white"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("white"), page.evaluate("() => window['result'].onChange"));
  }

  @PlaywrightTest
  @DisplayName("SO-007 should select single option by index")
  void shouldSelectSingleOptionByIndex(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.selectOption("select", new SelectOption().setIndex(2));
    assertEquals(asList("brown"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("brown"), page.evaluate("() => window['result'].onChange"));
  }

  @PlaywrightTest
  @DisplayName("SO-008 should select single option by multiple attributes")
  void shouldSelectSingleOptionByMultipleAttributes(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.selectOption("select", new SelectOption().setValue("green").setLabel("Green"));
    assertEquals(asList("green"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("green"), page.evaluate("() => window['result'].onChange"));
  }

  @PlaywrightTest
  @DisplayName("SO-009 should not select single option when some attributes do not match")
  void shouldNotSelectSingleOptionWhenSomeAttributesDoNotMatch(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.evalOnSelector("select", "s => s.value = undefined");
    try {
      page.selectOption("select", new SelectOption().setValue("green").setLabel("Brown"), new Page.SelectOptionOptions().setTimeout(1000));
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("Timeout"));
    }
    assertEquals("", page.evaluate("() => document.querySelector('select').value"));
  }

  @PlaywrightTest
  @DisplayName("SO-010 should select only first option")
  void shouldSelectOnlyFirstOption(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.selectOption("select", new String[]{"blue", "green", "red"});
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onChange"));
  }

  @PlaywrightTest
  @DisplayName("SO-011 should not throw when select causes navigation")
  void shouldNotThrowWhenSelectCausesNavigation(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.evalOnSelector("select", "select => select.addEventListener('input', () => window.location.href = '/empty.html')");
    page.waitForNavigation(() -> page.selectOption("select", "blue"));
    assertTrue(page.url().contains("empty.html"));
  }

  @PlaywrightTest
  @DisplayName("SO-012 should select multiple options")
  void shouldSelectMultipleOptions(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.evaluate("() => window['makeMultiple']()");
    page.selectOption("select", new String[]{"blue", "green", "red"});
    assertEquals(asList("blue", "green", "red"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("blue", "green", "red"), page.evaluate("() => window['result'].onChange"));
  }

  @PlaywrightTest
  @DisplayName("SO-013 should select multiple options with attributes")
  void shouldSelectMultipleOptionsWithAttributes(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.evaluate("() => window['makeMultiple']()");
    page.selectOption("select", new SelectOption[]{new SelectOption().setValue("blue"), new SelectOption().setLabel("Green"), new SelectOption().setIndex(4)});
    assertEquals(asList("blue", "gray", "green"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("blue", "gray", "green"), page.evaluate("() => window['result'].onChange"));
  }

  @PlaywrightTest
  @DisplayName("SO-014 should respect event bubbling")
  void shouldRespectEventBubbling(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.selectOption("select", "blue");
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onBubblingInput"));
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onBubblingChange"));
  }

  @PlaywrightTest
  @DisplayName("SO-015 should throw when element is not a <select>")
  void shouldThrowWhenElementIsNotASelect(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/select.html");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.selectOption("body", ""));
    assertTrue(error.getMessage().contains("Element is not a <select> element"));
  }

  @PlaywrightTest
  @DisplayName("SO-016 should return [] on no matched values")
  void shouldReturnOnNoMatchedValues(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/select.html");
    List<String> result = page.selectOption("select", new String[]{});
    assertEquals(new ArrayList<>(), result);
  }

  @PlaywrightTest
  @DisplayName("SO-017 should return an array of matched values")
  void shouldReturnAnArrayOfMatchedValues(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.evaluate("() => window['makeMultiple']()");
    List<String> result = page.selectOption("select", new String[]{"blue", "black", "magenta"});
    // Java: reduce() is a loop with the same accumulator.
    boolean accumulator = true;
    for (String current : result) {
      accumulator = asList("blue", "black", "magenta").contains(current) && accumulator;
    }
    assertEquals(true, accumulator);
  }

  @PlaywrightTest
  @DisplayName("SO-018 should return an array of one element when multiple is not set")
  void shouldReturnAnArrayOfOneElementWhenMultipleIsNotSet(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/select.html");
    List<String> result = page.selectOption("select", new String[]{"42", "blue", "black", "magenta"});
    assertEquals(1, result.size());
  }

  @PlaywrightTest
  @DisplayName("SO-019 should return [] on no values")
  void shouldReturnOnNoValues(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/select.html");
    List<String> result = page.selectOption("select", new String[]{});
    assertEquals(new ArrayList<>(), result);
  }

  @PlaywrightTest
  @DisplayName("SO-020 should not allow null items")
  @Fixme("java: selectOption() with a null item in the array does not throw, the expected error 'options[1]: expected object, got null' is not reported")
  void shouldNotAllowNullItems(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.evaluate("() => window['makeMultiple']()");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.selectOption("select", new String[]{"blue", null, "black", "magenta"}));
    assertTrue(error.getMessage().contains("options[1]: expected object, got null"));
  }

  @PlaywrightTest
  @DisplayName("SO-021 should unselect with null")
  void shouldUnselectWithNull(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.evaluate("() => window['makeMultiple']()");
    List<String> result = page.selectOption("select", new String[]{"blue", "black", "magenta"});
    boolean accumulator = true;
    for (String current : result) {
      accumulator = asList("blue", "black", "magenta").contains(current) && accumulator;
    }
    assertEquals(true, accumulator);
    page.selectOption("select", (String[]) null);
    assertEquals(true, page.evalOnSelector("select", "select => Array.from(select.options).every(option => !option.selected)"));
  }

  @PlaywrightTest
  @DisplayName("SO-022 should deselect all options when passed no values for a multiple select")
  void shouldDeselectAllOptionsWhenPassedNoValuesForAMultipleSelect(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.evaluate("() => window['makeMultiple']()");
    page.selectOption("select", new String[]{"blue", "black", "magenta"});
    page.selectOption("select", new String[]{});
    assertEquals(true, page.evalOnSelector("select", "select => Array.from(select.options).every(option => !option.selected)"));
  }

  @PlaywrightTest
  @DisplayName("SO-023 should deselect all options when passed no values for a select without multiple")
  void shouldDeselectAllOptionsWhenPassedNoValuesForASelectWithoutMultiple(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.selectOption("select", new String[]{"blue", "black", "magenta"});
    page.selectOption("select", new String[]{});
    assertEquals(true, page.evalOnSelector("select", "select => Array.from(select.options).every(option => !option.selected)"));
  }

  // @see https://github.com/GoogleChrome/puppeteer/issues/3327
  @PlaywrightTest
  @DisplayName("SO-025 should work when re-defining top-level Event class")
  void shouldWorkWhenReDefiningTopLevelEventClass(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.evaluate("() => window.Event = null");
    page.selectOption("select", "blue");
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onChange"));
  }

  @PlaywrightTest
  @DisplayName("SO-026 should wait for option to be present")
  void shouldWaitForOptionToBePresent(Page page, TestServer server) throws Exception {
    page.navigate(server.PREFIX + "/input/select.html");
    CompletableFuture<List<String>> selectPromise = async(() -> page.selectOption("select", "scarlet"));
    AtomicBoolean didSelect = new AtomicBoolean(false);
    selectPromise.thenAccept(result -> didSelect.set(true));
    giveItAChanceToResolve(page);
    assertEquals(false, didSelect.get());
    page.evalOnSelector("select", "select => {\n" +
      "    const option = document.createElement('option');\n" +
      "    option.value = 'scarlet';\n" +
      "    option.textContent = 'Scarlet';\n" +
      "    select.appendChild(option);\n" +
      "  }");
    List<String> items = selectPromise.get();
    assertEquals(asList("scarlet"), items);
  }

  @PlaywrightTest
  @DisplayName("SO-027 should wait for option index to be present")
  void shouldWaitForOptionIndexToBePresent(Page page, TestServer server) throws Exception {
    page.navigate(server.PREFIX + "/input/select.html");
    int len = (Integer) page.evalOnSelector("select", "select => select.options.length");
    CompletableFuture<List<String>> selectPromise = async(() -> page.selectOption("select", new SelectOption().setIndex(len)));
    AtomicBoolean didSelect = new AtomicBoolean(false);
    selectPromise.thenAccept(result -> didSelect.set(true));
    giveItAChanceToResolve(page);
    assertEquals(false, didSelect.get());
    page.evalOnSelector("select", "select => {\n" +
      "    const option = document.createElement('option');\n" +
      "    option.value = 'scarlet';\n" +
      "    option.textContent = 'Scarlet';\n" +
      "    select.appendChild(option);\n" +
      "  }");
    List<String> items = selectPromise.get();
    assertEquals(asList("scarlet"), items);
  }

  @PlaywrightTest
  @DisplayName("SO-028 should wait for multiple options to be present")
  void shouldWaitForMultipleOptionsToBePresent(Page page, TestServer server) throws Exception {
    page.navigate(server.PREFIX + "/input/select.html");
    page.evaluate("() => window['makeMultiple']()");
    CompletableFuture<List<String>> selectPromise = async(() -> page.selectOption("select", new String[]{"green", "scarlet"}));
    AtomicBoolean didSelect = new AtomicBoolean(false);
    selectPromise.thenAccept(result -> didSelect.set(true));
    giveItAChanceToResolve(page);
    assertEquals(false, didSelect.get());
    page.evalOnSelector("select", "select => {\n" +
      "    const option = document.createElement('option');\n" +
      "    option.value = 'scarlet';\n" +
      "    option.textContent = 'Scarlet';\n" +
      "    select.appendChild(option);\n" +
      "  }");
    List<String> items = selectPromise.get();
    assertEquals(asList("green", "scarlet"), items);
  }

  @PlaywrightTest
  @DisplayName("SO-029 input event.composed should be true and cross shadow dom boundary")
  void inputEventComposedShouldBeTrueAndCrossShadowDomBoundary(Page page, TestServer server) {
    // it.info().annotations.push({ type: 'issue', description: 'https://github.com/microsoft/playwright/issues/28726' });
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<body><script>\n" +
      "  const div = document.createElement('div');\n" +
      "  const shadowRoot = div.attachShadow({mode: 'open'});\n" +
      "  shadowRoot.innerHTML = `<select>\n" +
      "    <option value=\"black\">Black</option>\n" +
      "    <option value=\"blue\">Blue</option>\n" +
      "  </select>`;\n" +
      "  document.body.appendChild(div);\n" +
      "</script></body>");
    page.locator("body").evaluate("select => {\n" +
      "    window.firedBodyEvents = [];\n" +
      "    for (const event of ['input', 'change']) {\n" +
      "      select.addEventListener(event, e => {\n" +
      "        window.firedBodyEvents.push(e.type + ':' + e.composed);\n" +
      "      }, false);\n" +
      "    }\n" +
      "  }");

    page.locator("select").evaluate("select => {\n" +
      "    window.firedEvents = [];\n" +
      "    for (const event of ['input', 'change']) {\n" +
      "      select.addEventListener(event, e => {\n" +
      "        window.firedEvents.push(e.type + ':' + e.composed);\n" +
      "      }, false);\n" +
      "    }\n" +
      "  }");
    page.selectOption("select", "blue");
    assertEquals(asList("input:true", "change:false"), page.evaluate("() => window['firedEvents']"));
    assertEquals(asList("input:true"), page.evaluate("() => window['firedBodyEvents']"));
  }

  @PlaywrightTest
  @DisplayName("SO-030 should wait for select to be enabled")
  void shouldWaitForSelectToBeEnabled(Page page) throws Exception {
    page.setContent("\n" +
      "    <select disabled>\n" +
      "      <option>one</option>\n" +
      "      <option>two</option>\n" +
      "    </select>\n" +
      "\n" +
      "    <script>\n" +
      "    function hydrate() {\n" +
      "      const select = document.querySelector('select');\n" +
      "      select.removeAttribute('disabled');\n" +
      "      select.addEventListener('change', () => {\n" +
      "        window['result'] = select.value;\n" +
      "      });\n" +
      "    }\n" +
      "    </script>\n" +
      "  ");

    CompletableFuture<List<String>> selectPromise = async(() -> page.locator("select").selectOption("two"));
    page.waitForTimeout(1000);
    page.evaluate("() => window.hydrate()");
    selectPromise.get();
    assertEquals("two", page.evaluate("() => window['result']"));
    assertThat(page.locator("select")).hasValue("two");
  }

  @PlaywrightTest
  @DisplayName("SO-031 should wait for option to be enabled")
  void shouldWaitForOptionToBeEnabled(Page page) throws Exception {
    page.setContent("\n" +
      "    <select>\n" +
      "      <option>one</option>\n" +
      "      <option disabled id=myoption>two</option>\n" +
      "    </select>\n" +
      "\n" +
      "    <script>\n" +
      "    function hydrate() {\n" +
      "      const option = document.querySelector('#myoption');\n" +
      "      option.removeAttribute('disabled');\n" +
      "      const select = document.querySelector('select');\n" +
      "      select.addEventListener('change', () => {\n" +
      "        window['result'] = select.value;\n" +
      "      });\n" +
      "    }\n" +
      "    </script>\n" +
      "  ");

    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.locator("select").selectOption("two", new Locator.SelectOptionOptions().setTimeout(1000)));
    assertTrue(error.getMessage().contains("option being selected is not enabled"));

    CompletableFuture<List<String>> selectPromise = async(() -> page.locator("select").selectOption("two"));
    page.waitForTimeout(1000);
    page.evaluate("() => window.hydrate()");
    selectPromise.get();
    assertEquals("two", page.evaluate("() => window['result']"));
    assertThat(page.locator("select")).hasValue("two");
  }

  @PlaywrightTest
  @DisplayName("SO-032 should wait for optgroup to be enabled")
  void shouldWaitForOptgroupToBeEnabled(Page page) throws Exception {
    page.setContent("\n" +
      "    <select>\n" +
      "      <option>one</option>\n" +
      "      <optgroup label=\"Group\" disabled id=mygroup>\n" +
      "        <option>two</option>\n" +
      "      </optgroup>\n" +
      "    </select>\n" +
      "\n" +
      "    <script>\n" +
      "    function hydrate() {\n" +
      "      const group = document.querySelector('#mygroup');\n" +
      "      group.removeAttribute('disabled');\n" +
      "      const select = document.querySelector('select');\n" +
      "      select.addEventListener('change', () => {\n" +
      "        window['result'] = select.value;\n" +
      "      });\n" +
      "    }\n" +
      "    </script>\n" +
      "  ");

    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.locator("select").selectOption("two", new Locator.SelectOptionOptions().setTimeout(1000)));
    assertTrue(error.getMessage().contains("option being selected is not enabled"));

    CompletableFuture<List<String>> selectPromise = async(() -> page.locator("select").selectOption("two"));
    page.waitForTimeout(1000);
    page.evaluate("() => window.hydrate()");
    selectPromise.get();
    assertEquals("two", page.evaluate("() => window['result']"));
    assertThat(page.locator("select")).hasValue("two");
  }

  @PlaywrightTest
  @DisplayName("SO-033 should wait for select to be swapped")
  void shouldWaitForSelectToBeSwapped(Page page) throws Exception {
    page.setContent("\n" +
      "    <select disabled>\n" +
      "      <option>one</option>\n" +
      "      <option>two</option>\n" +
      "    </select>\n" +
      "\n" +
      "    <script>\n" +
      "    function hydrate() {\n" +
      "      const select = document.querySelector('select');\n" +
      "      select.remove();\n" +
      "\n" +
      "      const newSelect = document.createElement('select');\n" +
      "      const option1 = document.createElement('option');\n" +
      "      option1.textContent = 'one';\n" +
      "      newSelect.appendChild(option1);\n" +
      "      const option2 = document.createElement('option');\n" +
      "      option2.textContent = 'two';\n" +
      "      newSelect.appendChild(option2);\n" +
      "\n" +
      "      document.body.appendChild(newSelect);\n" +
      "\n" +
      "      newSelect.addEventListener('change', () => {\n" +
      "        window['result'] = newSelect.value;\n" +
      "      });\n" +
      "    }\n" +
      "    </script>\n" +
      "  ");

    CompletableFuture<List<String>> selectPromise = async(() -> page.locator("select").selectOption("two"));
    page.waitForTimeout(1000);
    page.evaluate("() => window.hydrate()");
    selectPromise.get();
    assertThat(page.locator("select")).hasValue("two");
    assertEquals("two", page.evaluate("() => window['result']"));
  }
}
