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
import com.microsoft.playwright.options.LoadState;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Port of tests/page/elementhandle-query-selector.spec.ts. */
public class TestElementhandleQuerySelector {
  @PlaywrightTest
  @DisplayName("EH-067 should query existing element")
  void shouldQueryExistingElement(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/playground.html");
    page.setContent("<html><body><div class=\"second\"><div class=\"inner\">A</div></div></body></html>");
    ElementHandle html = page.querySelector("html");
    ElementHandle second = html.querySelector(".second");
    ElementHandle inner = second.querySelector(".inner");
    Object content = page.evaluate("e => e.textContent", inner);
    assertEquals("A", content);
  }

  @PlaywrightTest
  @DisplayName("EH-068 should return null for non-existing element")
  void shouldReturnNullForNonExistingElement(Page page, TestServer server) {
    page.setContent("<html><body><div class=\"second\"><div class=\"inner\">B</div></div></body></html>");
    ElementHandle html = page.querySelector("html");
    ElementHandle second = html.querySelector(".third");
    assertNull(second);
  }

  @PlaywrightTest
  @DisplayName("EH-069 should work for adopted elements")
  void shouldWorkForAdoptedElements(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Page popup = page.waitForPopup(() -> page.evaluate("url => window['__popup'] = window.open(url)", server.EMPTY_PAGE));
    ElementHandle divHandle = page.evaluateHandle("() => {\n" +
      "    const div = document.createElement('div');\n" +
      "    document.body.appendChild(div);\n" +
      "    const span = document.createElement('span');\n" +
      "    span.textContent = 'hello';\n" +
      "    div.appendChild(span);\n" +
      "    return div;\n" +
      "  }").asElement();
    assertNotNull(divHandle.querySelector("span"));
    assertEquals("hello", divHandle.evalOnSelector("span", "e => e.textContent"));

    popup.waitForLoadState(LoadState.DOMCONTENTLOADED);
    page.evaluate("() => {\n" +
      "    const div = document.querySelector('div');\n" +
      "    window['__popup'].document.body.appendChild(div);\n" +
      "  }");
    assertNotNull(divHandle.querySelector("span"));
    assertEquals("hello", divHandle.evalOnSelector("span", "e => e.textContent"));
  }

  @PlaywrightTest
  @DisplayName("EH-070 should query existing elements")
  void shouldQueryExistingElements(Page page, TestServer server) {
    page.setContent("<html><body><div>A</div><br/><div>B</div></body></html>");
    ElementHandle html = page.querySelector("html");
    List<ElementHandle> elements = html.querySelectorAll("div");
    assertEquals(2, elements.size());
    List<Object> results = new ArrayList<>();
    for (ElementHandle element : elements) {
      results.add(page.evaluate("e => e.textContent", element));
    }
    assertEquals(Arrays.asList("A", "B"), results);
  }

  @PlaywrightTest
  @DisplayName("EH-071 should return empty array for non-existing elements")
  void shouldReturnEmptyArrayForNonExistingElements(Page page, TestServer server) {
    page.setContent("<html><body><span>A</span><br/><span>B</span></body></html>");
    ElementHandle html = page.querySelector("html");
    List<ElementHandle> elements = html.querySelectorAll("div");
    assertEquals(0, elements.size());
  }

  @PlaywrightTest
  @DisplayName("EH-072 xpath should query existing element")
  void xpathShouldQueryExistingElement(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/playground.html");
    page.setContent("<html><body><div class=\"second\"><div class=\"inner\">A</div></div></body></html>");
    ElementHandle html = page.querySelector("html");
    List<ElementHandle> second = html.querySelectorAll("xpath=./body/div[contains(@class, 'second')]");
    List<ElementHandle> inner = second.get(0).querySelectorAll("xpath=./div[contains(@class, 'inner')]");
    Object content = page.evaluate("e => e.textContent", inner.get(0));
    assertEquals("A", content);
  }

  @PlaywrightTest
  @DisplayName("EH-073 xpath should return null for non-existing element")
  void xpathShouldReturnNullForNonExistingElement(Page page, TestServer server) {
    page.setContent("<html><body><div class=\"second\"><div class=\"inner\">B</div></div></body></html>");
    ElementHandle html = page.querySelector("html");
    List<ElementHandle> second = html.querySelectorAll("xpath=/div[contains(@class, 'third')]");
    assertEquals(Collections.emptyList(), second);
  }
}
