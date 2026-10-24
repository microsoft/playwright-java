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

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestElementHandleMisc {
  @PlaywrightTest
  void shouldHover(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/scrollable.html");
    ElementHandle button = page.querySelector("#button-6");
    button.hover();
    assertEquals("button-6", page.evaluate("document.querySelector('button:hover').id"));
  }

  @PlaywrightTest
  void shouldHoverWhenNodeIsRemoved(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/scrollable.html");
    page.evaluate("() => delete window['Node']");
    ElementHandle button = page.querySelector("#button-6");
    button.hover();
    assertEquals("button-6", page.evaluate("document.querySelector('button:hover').id"));
  }

  @PlaywrightTest
  void shouldFillInput(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    ElementHandle handle = page.querySelector("input");
    handle.fill("some value");
    assertEquals("some value", page.evaluate("window['result']"));
  }

  @PlaywrightTest
  void shouldFillInputWhenNodeIsRemoved(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.evaluate("() => delete window['Node']");
    ElementHandle handle = page.querySelector("input");
    handle.fill("some value");
    assertEquals("some value", page.evaluate("window['result']"));
  }

  @PlaywrightTest
  void shouldCheckTheBox(Page page) {
    page.setContent("<input id='checkbox' type='checkbox'></input>");
    ElementHandle input = page.querySelector("input");
    input.check();
    assertEquals(true, page.evaluate("checkbox.checked"));
  }

  @PlaywrightTest
  void shouldUncheckTheBox(Page page) {
    page.setContent("<input id='checkbox' type='checkbox' checked></input>");
    ElementHandle input = page.querySelector("input");
    input.uncheck();
    assertEquals(false, page.evaluate("checkbox.checked"));
  }

  @PlaywrightTest
  void shouldSelectSingleOption(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/select.html");
    ElementHandle select = page.querySelector("select");
    select.selectOption("blue");
    assertEquals(asList("blue"), page.evaluate("window['result'].onInput"));
    assertEquals(asList("blue"), page.evaluate("window['result'].onChange"));
  }

  @PlaywrightTest
  void shouldFocusAButton(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/button.html");
    ElementHandle button = page.querySelector("button");
    assertEquals(false, button.evaluate("button => document.activeElement === button"));
    button.focus();
    assertEquals(true, button.evaluate("button => document.activeElement === button"));
  }

  @PlaywrightTest
  void shouldCheckTheBoxUsingSetChecked(Page page) {
    page.setContent("<input id='checkbox' type='checkbox'></input>");
    ElementHandle input = page.querySelector("input");
    input.setChecked(true);
    assertEquals(true, page.evaluate("checkbox.checked"));
    input.setChecked(false);
    assertEquals(false, page.evaluate("checkbox.checked"));
  }

  @PlaywrightTest
  void shouldFallBackToSelectingByLabel(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/select.html");
    page.querySelector("select").selectOption("Blue");
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onChange"));
  }

  @PlaywrightTest
  void shouldAllowDisposingTwice(Page page) {
    page.setContent("<section>39</section>");
    ElementHandle element = page.querySelector("section");
    assertNotNull(element);
    element.dispose();
    element.dispose();
  }
}
