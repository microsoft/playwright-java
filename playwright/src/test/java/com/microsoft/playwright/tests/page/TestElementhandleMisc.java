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

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/** Port of tests/page/elementhandle-misc.spec.ts. */
public class TestElementhandleMisc {
  @PlaywrightTest
  @DisplayName("EH-045 should hover")
  void shouldHover(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/scrollable.html");
    ElementHandle button = page.querySelector("#button-6");
    button.hover();
    assertEquals("button-6", page.evaluate("() => document.querySelector('button:hover').id"));
  }

  @PlaywrightTest
  @DisplayName("EH-046 should hover when Node is removed")
  void shouldHoverWhenNodeIsRemoved(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/scrollable.html");
    page.evaluate("() => delete window['Node']");
    ElementHandle button = page.querySelector("#button-6");
    button.hover();
    assertEquals("button-6", page.evaluate("() => document.querySelector('button:hover').id"));
  }

  @PlaywrightTest
  @DisplayName("EH-047 should fill input")
  void shouldFillInput(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    ElementHandle handle = page.querySelector("input");
    handle.fill("some value");
    assertEquals("some value", page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("EH-048 should fill input when Node is removed")
  void shouldFillInputWhenNodeIsRemoved(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.evaluate("() => delete window['Node']");
    ElementHandle handle = page.querySelector("input");
    handle.fill("some value");
    assertEquals("some value", page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("EH-049 should check the box")
  void shouldCheckTheBox(Page page) {
    page.setContent("<input id='checkbox' type='checkbox'></input>");
    ElementHandle input = page.querySelector("input");
    input.check();
    assertEquals(true, page.evaluate("checkbox.checked"));
  }

  @PlaywrightTest
  @DisplayName("EH-050 should check the box using setChecked")
  void shouldCheckTheBoxUsingSetChecked(Page page) {
    page.setContent("<input id='checkbox' type='checkbox'></input>");
    ElementHandle input = page.querySelector("input");
    input.setChecked(true);
    assertEquals(true, page.evaluate("checkbox.checked"));
    input.setChecked(false);
    assertEquals(false, page.evaluate("checkbox.checked"));
  }

  @PlaywrightTest
  @DisplayName("EH-051 should uncheck the box")
  void shouldUncheckTheBox(Page page) {
    page.setContent("<input id='checkbox' type='checkbox' checked></input>");
    ElementHandle input = page.querySelector("input");
    input.uncheck();
    assertEquals(false, page.evaluate("checkbox.checked"));
  }

  @PlaywrightTest
  @DisplayName("EH-052 should select single option")
  void shouldSelectSingleOption(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/select.html");
    ElementHandle select = page.querySelector("select");
    select.selectOption("blue");
    assertEquals(Arrays.asList("blue"), page.evaluate("() => window['result'].onInput"));
    assertEquals(Arrays.asList("blue"), page.evaluate("() => window['result'].onChange"));
  }

  @PlaywrightTest
  @DisplayName("EH-053 should focus a button")
  void shouldFocusAButton(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    ElementHandle button = page.querySelector("button");
    assertEquals(false, button.evaluate("button => document.activeElement === button"));
    button.focus();
    assertEquals(true, button.evaluate("button => document.activeElement === button"));
  }

  @PlaywrightTest
  @DisplayName("EH-054 should allow disposing twice")
  void shouldAllowDisposingTwice(Page page) {
    page.setContent("<section>39</section>");
    ElementHandle element = page.querySelector("section");
    assertNotNull(element);
    element.dispose();
    element.dispose();
  }
}
