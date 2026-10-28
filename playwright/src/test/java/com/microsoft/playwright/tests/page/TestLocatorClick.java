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

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Port of tests/page/locator-click.spec.ts. */
public class TestLocatorClick {
  @PlaywrightTest
  @DisplayName("LO-052 should work @smoke")
  void shouldWork(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    Locator button = page.locator("button");
    button.click();
    assertEquals("Clicked", page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("LO-053 should work with Node removed")
  void shouldWorkWithNodeRemoved(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evaluate("() => delete window['Node']");
    Locator button = page.locator("button");
    button.click();
    assertEquals("Clicked", page.evaluate("() => window['result']"));
  }

  @PlaywrightTest
  @DisplayName("LO-054 should double click the button")
  void shouldDoubleClickTheButton(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/button.html");
    page.evaluate("() => {\n" +
      "    window['double'] = false;\n" +
      "    const button = document.querySelector('button');\n" +
      "    button.addEventListener('dblclick', event => {\n" +
      "      window['double'] = true;\n" +
      "    });\n" +
      "  }");
    Locator button = page.locator("button");
    button.dblclick();
    assertEquals(true, page.evaluate("double"));
    assertEquals("Clicked", page.evaluate("result"));
  }

  @PlaywrightTest
  @DisplayName("LO-055 should click if the target element is removed in pointerup event")
  void shouldClickIfTheTargetElementIsRemovedInPointerupEvent(Page page) {
    page.setContent("<button id=clickme>Clickable</button>");
    page.evalOnSelector("#clickme", "element => element.addEventListener('pointerup', () => element.remove(), false)");
    page.locator("#clickme").click();
  }

  @PlaywrightTest
  @DisplayName("LO-056 should click if the target element is removed in pointerdown event")
  void shouldClickIfTheTargetElementIsRemovedInPointerdownEvent(Page page) {
    page.setContent("<button id=clickme>Clickable</button>");
    page.evalOnSelector("#clickme", "element => element.addEventListener('pointerdown', () => element.remove(), false)");
    page.locator("#clickme").click();
  }
}
