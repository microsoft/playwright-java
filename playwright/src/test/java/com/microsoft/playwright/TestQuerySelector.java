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

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestQuerySelector extends TestBase {

  @Test
  void shouldThrowForNonStringSelector() {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.querySelector(null));
    assertTrue(e.getMessage().contains("selector: expected string, got undefined"));
  }

  @Test
  void shouldQueryExistingElementWithCssSelector() {
    page.setContent("<section>test</section>");
    ElementHandle element = page.querySelector("css=section");
    assertNotNull(element);
  }

  @Test
  void shouldQueryExistingElementWithTextSelector() {
    page.setContent("<section>test</section>");
    ElementHandle element = page.querySelector("text='test'");
    assertNotNull(element);
  }

  @Test
  void shouldQueryExistingElementWithXpathSelector() {
    page.setContent("<section>test</section>");
    ElementHandle element = page.querySelector("xpath=/html/body/section");
    assertNotNull(element);
  }

  @Test
  void shouldReturnNullForNonExistingElement() {
    ElementHandle element = page.querySelector("non-existing-element");
    assertNull(element);
  }

  @Test
  void shouldAutoDetectXpathSelector() {
    page.setContent("<section>test</section>");
    ElementHandle element = page.querySelector("//html/body/section");
    assertNotNull(element);
  }

  @Test
  void shouldAutoDetectXpathSelectorWithStartingParenthesis() {
    page.setContent("<section>test</section>");
    ElementHandle element = page.querySelector("(//section)[1]");
    assertNotNull(element);
  }

  @Test
  void shouldAutoDetectTextSelector() {
    page.setContent("<section>test</section>");
    ElementHandle element = page.querySelector("'test'");
    assertNotNull(element);
  }

  @Test
  void shouldAutoDetectCssSelector() {
    page.setContent("<section>test</section>");
    ElementHandle element = page.querySelector("section");
    assertNotNull(element);
  }

  @Test
  void shouldSupportSyntax() {
    page.setContent("<section><div>test</div></section>");
    ElementHandle element = page.querySelector("css=section >> css=div");
    assertNotNull(element);
  }

  @Test
  void shouldQueryExistingElements() {
    page.setContent("<div>A</div><br/><div>B</div>");
    List<ElementHandle> elements = page.querySelectorAll("div");
    assertEquals(2, elements.size());
    List<Object> results = new ArrayList<>();
    for (ElementHandle element : elements) {
      results.add(page.evaluate("e => e.textContent", element));
    }
    assertEquals(Arrays.asList("A", "B"), results);
  }

  @Test
  void shouldReturnEmptyArrayIfNothingIsFound() {
    page.navigate(server.EMPTY_PAGE);
    List<ElementHandle> elements = page.querySelectorAll("div");
    assertEquals(0, elements.size());
  }

  @Test
  void xpathShouldQueryExistingElement() {
    page.setContent("<section>test</section>");
    List<ElementHandle> elements = page.querySelectorAll("xpath=/html/body/section");
    assertNotNull(elements.get(0));
    assertEquals(1, elements.size());
  }

  @Test
  void xpathShouldReturnEmptyArrayForNonExistingElement() {
    List<ElementHandle> elements = page.querySelectorAll("//html/body/non-existing-element");
    assertEquals(Collections.emptyList(), elements);
  }

  @Test
  void xpathShouldReturnMultipleElements() {
    page.setContent("<div></div><div></div>");
    List<ElementHandle> elements = page.querySelectorAll("xpath=/html/body/div");
    assertEquals(2, elements.size());
  }

  @Test
  void querySelectorAllShouldWorkWithBogusArrayFrom() throws InterruptedException {
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
