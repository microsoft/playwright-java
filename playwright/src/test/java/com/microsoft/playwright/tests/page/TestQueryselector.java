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
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/queryselector.spec.ts. */
public class TestQueryselector {
  @PlaywrightTest
  @DisplayName("QS-001 should throw for non-string selector")
  @Fixme("java: the driver message is 'selector: expected string, got undefined', because null is not sent")
  void shouldThrowForNonStringSelector(Page page) {
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.querySelector(null));
    assertTrue(error.getMessage().contains("selector: expected string, got object"));
  }

  @PlaywrightTest
  @DisplayName("QS-002 should query existing element with css selector @smoke")
  void shouldQueryExistingElementWithCssSelector(Page page, TestServer server) {
    page.setContent("<section>test</section>");
    ElementHandle element = page.querySelector("css=section");
    assertNotNull(element);
  }

  @PlaywrightTest
  @DisplayName("QS-003 should query existing element with text selector")
  void shouldQueryExistingElementWithTextSelector(Page page, TestServer server) {
    page.setContent("<section>test</section>");
    ElementHandle element = page.querySelector("text=\"test\"");
    assertNotNull(element);
  }

  @PlaywrightTest
  @DisplayName("QS-004 should query existing element with xpath selector")
  void shouldQueryExistingElementWithXpathSelector(Page page, TestServer server) {
    page.setContent("<section>test</section>");
    ElementHandle element = page.querySelector("xpath=/html/body/section");
    assertNotNull(element);
  }

  @PlaywrightTest
  @DisplayName("QS-005 should return null for non-existing element")
  void shouldReturnNullForNonExistingElement(Page page, TestServer server) {
    ElementHandle element = page.querySelector("non-existing-element");
    assertNull(element);
  }

  @PlaywrightTest
  @DisplayName("QS-006 should auto-detect xpath selector")
  void shouldAutoDetectXpathSelector(Page page, TestServer server) {
    page.setContent("<section>test</section>");
    ElementHandle element = page.querySelector("//html/body/section");
    assertNotNull(element);
  }

  @PlaywrightTest
  @DisplayName("QS-007 should auto-detect xpath selector with starting parenthesis")
  void shouldAutoDetectXpathSelectorWithStartingParenthesis(Page page, TestServer server) {
    page.setContent("<section>test</section>");
    ElementHandle element = page.querySelector("(//section)[1]");
    assertNotNull(element);
  }

  @PlaywrightTest
  @DisplayName("QS-008 should auto-detect xpath selector starting with ..")
  void shouldAutoDetectXpathSelectorStartingWith(Page page, TestServer server) {
    page.setContent("<div><section>test</section><span></span></div>");
    ElementHandle span = page.querySelector("\"test\" >> ../span");
    assertEquals("SPAN", span.evaluate("e => e.nodeName"));
    ElementHandle div = page.querySelector("\"test\" >> ..");
    assertEquals("DIV", div.evaluate("e => e.nodeName"));
  }

  @PlaywrightTest
  @DisplayName("QS-009 should auto-detect text selector")
  void shouldAutoDetectTextSelector(Page page, TestServer server) {
    page.setContent("<section>test</section>");
    ElementHandle element = page.querySelector("\"test\"");
    assertNotNull(element);
  }

  @PlaywrightTest
  @DisplayName("QS-010 should auto-detect css selector")
  void shouldAutoDetectCssSelector(Page page, TestServer server) {
    page.setContent("<section>test</section>");
    ElementHandle element = page.querySelector("section");
    assertNotNull(element);
  }

  @PlaywrightTest
  @DisplayName("QS-011 should support >> syntax")
  void shouldSupportSyntax(Page page, TestServer server) {
    page.setContent("<section><div>test</div></section>");
    ElementHandle element = page.querySelector("css=section >> css=div");
    assertNotNull(element);
  }

  @PlaywrightTest
  @DisplayName("QS-012 should query existing elements")
  void shouldQueryExistingElements(Page page, TestServer server) {
    page.setContent("<div>A</div><br/><div>B</div>");
    List<ElementHandle> elements = page.querySelectorAll("div");
    assertEquals(2, elements.size());
    List<Object> results = new ArrayList<>();
    for (ElementHandle element : elements)
      results.add(page.evaluate("e => e.textContent", element));
    assertEquals(asList("A", "B"), results);
  }

  @PlaywrightTest
  @DisplayName("QS-013 should return empty array if nothing is found")
  void shouldReturnEmptyArrayIfNothingIsFound(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    List<ElementHandle> elements = page.querySelectorAll("div");
    assertEquals(0, elements.size());
  }

  @PlaywrightTest
  @DisplayName("QS-014 xpath should query existing element")
  void xpathShouldQueryExistingElement(Page page, TestServer server) {
    page.setContent("<section>test</section>");
    List<ElementHandle> elements = page.querySelectorAll("xpath=/html/body/section");
    assertNotNull(elements.get(0));
    assertEquals(1, elements.size());
  }

  @PlaywrightTest
  @DisplayName("QS-015 xpath should return empty array for non-existing element")
  void xpathShouldReturnEmptyArrayForNonExistingElement(Page page, TestServer server) {
    List<ElementHandle> element = page.querySelectorAll("//html/body/non-existing-element");
    assertEquals(Collections.emptyList(), element);
  }

  @PlaywrightTest
  @DisplayName("QS-016 xpath should return multiple elements")
  void xpathShouldReturnMultipleElements(Page page, TestServer server) {
    page.setContent("<div></div><div></div>");
    List<ElementHandle> elements = page.querySelectorAll("xpath=/html/body/div");
    assertEquals(2, elements.size());
  }

  @PlaywrightTest
  @DisplayName("QS-017 $$ should work with bogus Array.from")
  void shouldWorkWithBogusArrayFrom(Page page, TestServer server) {
    page.setContent("<div>hello</div><div></div>");
    JSHandle div1 = page.evaluateHandle("() => {\n" +
      "  Array.from = () => [];\n" +
      "  return document.querySelector('div');\n" +
      "}");
    List<ElementHandle> elements = page.querySelectorAll("div");
    assertEquals(2, elements.size());
    // Check that element handle is functional and belongs to the main world.
    assertEquals(true, elements.get(0).evaluate("(div, div1) => div === div1", div1));
  }
}
