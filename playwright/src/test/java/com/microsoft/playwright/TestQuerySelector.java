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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestQuerySelector {

  @PlaywrightTest
  void shouldThrowForNonStringSelector(Page page) {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.querySelector(null));
    assertTrue(e.getMessage().contains("selector: expected string, got undefined"));
  }

  @PlaywrightTest
  void shouldQueryExistingElementWithCssSelector(Page page) {
    page.setContent("<section>test</section>");
    ElementHandle element = page.querySelector("css=section");
    assertNotNull(element);
  }

  @PlaywrightTest
  void shouldQueryExistingElementWithTextSelector(Page page) {
    page.setContent("<section>test</section>");
    ElementHandle element = page.querySelector("text='test'");
    assertNotNull(element);
  }

  @PlaywrightTest
  void shouldQueryExistingElementWithXpathSelector(Page page) {
    page.setContent("<section>test</section>");
    ElementHandle element = page.querySelector("xpath=/html/body/section");
    assertNotNull(element);
  }

  @PlaywrightTest
  void shouldReturnNullForNonExistingElement(Page page) {
    ElementHandle element = page.querySelector("non-existing-element");
    assertNull(element);
  }

  @PlaywrightTest
  void shouldAutoDetectXpathSelector(Page page) {
    page.setContent("<section>test</section>");
    ElementHandle element = page.querySelector("//html/body/section");
    assertNotNull(element);
  }

  @PlaywrightTest
  void shouldAutoDetectXpathSelectorWithStartingParenthesis(Page page) {
    page.setContent("<section>test</section>");
    ElementHandle element = page.querySelector("(//section)[1]");
    assertNotNull(element);
  }

  @PlaywrightTest
  void shouldAutoDetectTextSelector(Page page) {
    page.setContent("<section>test</section>");
    ElementHandle element = page.querySelector("'test'");
    assertNotNull(element);
  }

  @PlaywrightTest
  void shouldAutoDetectCssSelector(Page page) {
    page.setContent("<section>test</section>");
    ElementHandle element = page.querySelector("section");
    assertNotNull(element);
  }

  @PlaywrightTest
  void shouldSupportSyntax(Page page) {
    page.setContent("<section><div>test</div></section>");
    ElementHandle element = page.querySelector("css=section >> css=div");
    assertNotNull(element);
  }

  @PlaywrightTest
  void shouldQueryExistingElements(Page page) {
    page.setContent("<div>A</div><br/><div>B</div>");
    List<ElementHandle> elements = page.querySelectorAll("div");
    assertEquals(2, elements.size());
    List<Object> results = new ArrayList<>();
    for (ElementHandle element : elements) {
      results.add(page.evaluate("e => e.textContent", element));
    }
    assertEquals(Arrays.asList("A", "B"), results);
  }

  @PlaywrightTest
  void shouldReturnEmptyArrayIfNothingIsFound(Page page, Server server) {
    page.navigate(server.EMPTY_PAGE);
    List<ElementHandle> elements = page.querySelectorAll("div");
    assertEquals(0, elements.size());
  }

  @PlaywrightTest
  void xpathShouldQueryExistingElement(Page page) {
    page.setContent("<section>test</section>");
    List<ElementHandle> elements = page.querySelectorAll("xpath=/html/body/section");
    assertNotNull(elements.get(0));
    assertEquals(1, elements.size());
  }

  @PlaywrightTest
  void xpathShouldReturnEmptyArrayForNonExistingElement(Page page) {
    List<ElementHandle> elements = page.querySelectorAll("//html/body/non-existing-element");
    assertEquals(Collections.emptyList(), elements);
  }

  @PlaywrightTest
  void xpathShouldReturnMultipleElements(Page page) {
    page.setContent("<div></div><div></div>");
    List<ElementHandle> elements = page.querySelectorAll("xpath=/html/body/div");
    assertEquals(2, elements.size());
  }

  @PlaywrightTest
  void querySelectorAllShouldWorkWithBogusArrayFrom(Page page) throws InterruptedException {
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
