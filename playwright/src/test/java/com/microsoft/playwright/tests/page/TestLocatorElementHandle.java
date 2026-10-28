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

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Port of tests/page/locator-element-handle.spec.ts. */
public class TestLocatorElementHandle {
  @PlaywrightTest
  @DisplayName("LO-079 should query existing element @smoke")
  void shouldQueryExistingElement(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/playground.html");
    page.setContent("<html><body><div class=\"second\"><div class=\"inner\">A</div></div></body></html>");
    Locator html = page.locator("html");
    Locator second = html.locator(".second");
    Locator inner = second.locator(".inner");
    Object content = page.evaluate("e => e.textContent", inner.elementHandle());
    assertEquals("A", content);
  }

  @PlaywrightTest
  @DisplayName("LO-080 should query existing elements")
  void shouldQueryExistingElements(Page page, TestServer server) {
    page.setContent("<html><body><div>A</div><br/><div>B</div></body></html>");
    Locator html = page.locator("html");
    List<ElementHandle> elements = html.locator("div").elementHandles();
    assertEquals(2, elements.size());
    List<Object> promises = new ArrayList<>();
    for (ElementHandle element : elements) {
      promises.add(page.evaluate("e => e.textContent", element));
    }
    assertEquals(asList("A", "B"), promises);
  }

  @PlaywrightTest
  @DisplayName("LO-081 should return empty array for non-existing elements")
  void shouldReturnEmptyArrayForNonExistingElements(Page page, TestServer server) {
    page.setContent("<html><body><span>A</span><br/><span>B</span></body></html>");
    Locator html = page.locator("html");
    List<ElementHandle> elements = html.locator("div").elementHandles();
    assertEquals(0, elements.size());
  }

  @PlaywrightTest
  @DisplayName("LO-082 xpath should query existing element")
  void xpathShouldQueryExistingElement(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/playground.html");
    page.setContent("<html><body><div class=\"second\"><div class=\"inner\">A</div></div></body></html>");
    Locator html = page.locator("html");
    Locator second = html.locator("xpath=./body/div[contains(@class, 'second')]");
    Locator inner = second.locator("xpath=./div[contains(@class, 'inner')]");
    Object content = page.evaluate("e => e.textContent", inner.elementHandle());
    assertEquals("A", content);
  }

  @PlaywrightTest
  @DisplayName("LO-083 xpath should return null for non-existing element")
  void xpathShouldReturnNullForNonExistingElement(Page page, TestServer server) {
    page.setContent("<html><body><div class=\"second\"><div class=\"inner\">B</div></div></body></html>");
    Locator html = page.locator("html");
    List<ElementHandle> second = html.locator("xpath=/div[contains(@class, 'third')]").elementHandles();
    assertEquals(Collections.emptyList(), second);
  }
}
