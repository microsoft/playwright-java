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
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;

import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/eval-on-selector.spec.ts. */
public class TestEvalOnSelector {
  @PlaywrightTest
  @DisplayName("ES-001 should work with css selector")
  void shouldWorkWithCssSelector(Page page, TestServer server) {
    page.setContent("<section id=\"testAttribute\">43543</section>");
    Object idAttribute = page.evalOnSelector("css=section", "e => e.id");
    assertEquals("testAttribute", idAttribute);
  }

  @PlaywrightTest
  @DisplayName("ES-002 should work with id selector")
  void shouldWorkWithIdSelector(Page page, TestServer server) {
    page.setContent("<section id=\"testAttribute\">43543</section>");
    Object idAttribute = page.evalOnSelector("id=testAttribute", "e => e.id");
    assertEquals("testAttribute", idAttribute);
  }

  @PlaywrightTest
  @DisplayName("ES-003 should work with data-test selector")
  void shouldWorkWithDataTestSelector(Page page, TestServer server) {
    page.setContent("<section data-test=foo id=\"testAttribute\">43543</section>");
    Object idAttribute = page.evalOnSelector("data-test=foo", "e => e.id");
    assertEquals("testAttribute", idAttribute);
  }

  @PlaywrightTest
  @DisplayName("ES-004 should work with data-testid selector")
  void shouldWorkWithDataTestidSelector(Page page, TestServer server) {
    page.setContent("<section data-testid=foo id=\"testAttribute\">43543</section>");
    Object idAttribute = page.evalOnSelector("data-testid=foo", "e => e.id");
    assertEquals("testAttribute", idAttribute);
  }

  @PlaywrightTest
  @DisplayName("ES-005 should work with data-test-id selector")
  void shouldWorkWithDataTestIdSelector(Page page, TestServer server) {
    page.setContent("<section data-test-id=foo id=\"testAttribute\">43543</section>");
    Object idAttribute = page.evalOnSelector("data-test-id=foo", "e => e.id");
    assertEquals("testAttribute", idAttribute);
  }

  @PlaywrightTest
  @DisplayName("ES-006 should work with text selector in quotes")
  void shouldWorkWithTextSelectorInQuotes(Page page, TestServer server) {
    page.setContent("<section id=\"testAttribute\">43543</section>");
    Object idAttribute = page.evalOnSelector("text=\"43543\"", "e => e.id");
    assertEquals("testAttribute", idAttribute);
  }

  @PlaywrightTest
  @DisplayName("ES-007 should work with xpath selector")
  void shouldWorkWithXpathSelector(Page page, TestServer server) {
    page.setContent("<section id=\"testAttribute\">43543</section>");
    Object idAttribute = page.evalOnSelector("xpath=/html/body/section", "e => e.id");
    assertEquals("testAttribute", idAttribute);
  }

  @PlaywrightTest
  @DisplayName("ES-008 should work with text selector")
  void shouldWorkWithTextSelector(Page page, TestServer server) {
    page.setContent("<section id=\"testAttribute\">43543</section>");
    Object idAttribute = page.evalOnSelector("text=43543", "e => e.id");
    assertEquals("testAttribute", idAttribute);
  }

  @PlaywrightTest
  @DisplayName("ES-009 should auto-detect css selector")
  void shouldAutoDetectCssSelector(Page page, TestServer server) {
    page.setContent("<section id=\"testAttribute\">43543</section>");
    Object idAttribute = page.evalOnSelector("section", "e => e.id");
    assertEquals("testAttribute", idAttribute);
  }

  @PlaywrightTest
  @DisplayName("ES-010 should auto-detect css selector with attributes")
  void shouldAutoDetectCssSelectorWithAttributes(Page page, TestServer server) {
    page.setContent("<section id=\"testAttribute\">43543</section>");
    Object idAttribute = page.evalOnSelector("section[id=\"testAttribute\"]", "e => e.id");
    assertEquals("testAttribute", idAttribute);
  }

  @PlaywrightTest
  @DisplayName("ES-011 should auto-detect nested selectors")
  void shouldAutoDetectNestedSelectors(Page page, TestServer server) {
    page.setContent("<div foo=bar><section>43543<span>Hello<div id=target></div></span></section></div>");
    Object idAttribute = page.evalOnSelector("div[foo=bar] > section >> \"Hello\" >> div", "e => e.id");
    assertEquals("target", idAttribute);
  }

  @PlaywrightTest
  @DisplayName("ES-012 should accept arguments")
  void shouldAcceptArguments(Page page, TestServer server) {
    page.setContent("<section>hello</section>");
    Object text = page.evalOnSelector("section", "(e, suffix) => e.textContent + suffix", " world!");
    assertEquals("hello world!", text);
  }

  @PlaywrightTest
  @DisplayName("ES-013 should accept ElementHandles as arguments")
  void shouldAcceptElementHandlesAsArguments(Page page, TestServer server) {
    page.setContent("<section>hello</section><div> world</div>");
    ElementHandle divHandle = page.querySelector("div");
    Object text = page.evalOnSelector("section", "(e, div) => e.textContent + div.textContent", divHandle);
    assertEquals("hello world", text);
  }

  @PlaywrightTest
  @DisplayName("ES-014 should throw error if no element is found")
  void shouldThrowErrorIfNoElementIsFound(Page page, TestServer server) {
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.evalOnSelector("section", "e => e.id"));
    assertTrue(error.getMessage().contains("Failed to find element matching selector \"section\""));
  }

  @PlaywrightTest
  @DisplayName("ES-015 should support >> syntax")
  void shouldSupportSyntax(Page page, TestServer server) {
    page.setContent("<section><div>hello</div></section>");
    Object text = page.evalOnSelector("css=section >> css=div", "(e, suffix) => e.textContent + suffix", " world!");
    assertEquals("hello world!", text);
  }

  @PlaywrightTest
  @DisplayName("ES-016 should support >> syntax with different engines")
  void shouldSupportSyntaxWithDifferentEngines(Page page, TestServer server) {
    page.setContent("<section><div><span>hello</span></div></section>");
    Object text = page.evalOnSelector("xpath=/html/body/section >> css=div >> text=\"hello\"", "(e, suffix) => e.textContent + suffix", " world!");
    assertEquals("hello world!", text);
  }

  @PlaywrightTest
  @DisplayName("ES-017 should support spaces with >> syntax")
  void shouldSupportSpacesWithSyntax(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/deep-shadow.html");
    Object text = page.evalOnSelector(" css = div >>css=div>>css   = span  ", "e => e.textContent");
    assertEquals("Hello from root2", text);
  }

  @PlaywrightTest
  @DisplayName("ES-018 should not stop at first failure with >> syntax")
  void shouldNotStopAtFirstFailureWithSyntax(Page page, TestServer server) {
    page.setContent("<div><span>Next</span><button>Previous</button><button>Next</button></div>");
    Object html = page.evalOnSelector("button >> \"Next\"", "e => e.outerHTML");
    assertEquals("<button>Next</button>", html);
  }

  @PlaywrightTest
  @DisplayName("ES-019 should support * capture")
  void shouldSupportCapture(Page page, TestServer server) {
    page.setContent("<section><div><span>a</span></div></section><section><div><span>b</span></div></section>");
    assertEquals("<div><span>b</span></div>", page.evalOnSelector("*css=div >> \"b\"", "e => e.outerHTML"));
    assertEquals("<div><span>b</span></div>", page.evalOnSelector("section >> *css=div >> \"b\"", "e => e.outerHTML"));
    assertEquals("<span>b</span>", page.evalOnSelector("css=div >> *text=\"b\"", "e => e.outerHTML"));
    assertNotNull(page.querySelector("*"));
  }

  @PlaywrightTest
  @DisplayName("ES-020 should throw on multiple * captures")
  void shouldThrowOnMultipleCaptures(Page page, TestServer server) {
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.evalOnSelector("*css=div >> *css=span", "e => e.outerHTML"));
    assertTrue(error.getMessage().contains("Only one of the selectors can capture using * modifier"));
  }

  @PlaywrightTest
  @DisplayName("ES-021 should throw on malformed * capture")
  void shouldThrowOnMalformedCapture(Page page, TestServer server) {
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.evalOnSelector("*=div", "e => e.outerHTML"));
    assertTrue(error.getMessage().contains("Unknown engine \"\" while parsing selector *=div"));
  }

  @PlaywrightTest
  @DisplayName("ES-022 should work with spaces in css attributes")
  void shouldWorkWithSpacesInCssAttributes(Page page, TestServer server) {
    page.setContent("<div><input placeholder=\"Select date\"></div>");
    assertNotNull(page.waitForSelector("[placeholder=\"Select date\"]"));
    assertNotNull(page.waitForSelector("[placeholder='Select date']"));
    assertNotNull(page.waitForSelector("input[placeholder=\"Select date\"]"));
    assertNotNull(page.waitForSelector("input[placeholder='Select date']"));
    assertNotNull(page.querySelector("[placeholder=\"Select date\"]"));
    assertNotNull(page.querySelector("[placeholder='Select date']"));
    assertNotNull(page.querySelector("input[placeholder=\"Select date\"]"));
    assertNotNull(page.querySelector("input[placeholder='Select date']"));
    assertEquals("<input placeholder=\"Select date\">", page.evalOnSelector("[placeholder=\"Select date\"]", "e => e.outerHTML"));
    assertEquals("<input placeholder=\"Select date\">", page.evalOnSelector("[placeholder='Select date']", "e => e.outerHTML"));
    assertEquals("<input placeholder=\"Select date\">", page.evalOnSelector("input[placeholder=\"Select date\"]", "e => e.outerHTML"));
    assertEquals("<input placeholder=\"Select date\">", page.evalOnSelector("input[placeholder='Select date']", "e => e.outerHTML"));
    assertEquals("<input placeholder=\"Select date\">", page.evalOnSelector("css=[placeholder=\"Select date\"]", "e => e.outerHTML"));
    assertEquals("<input placeholder=\"Select date\">", page.evalOnSelector("css=[placeholder='Select date']", "e => e.outerHTML"));
    assertEquals("<input placeholder=\"Select date\">", page.evalOnSelector("css=input[placeholder=\"Select date\"]", "e => e.outerHTML"));
    assertEquals("<input placeholder=\"Select date\">", page.evalOnSelector("css=input[placeholder='Select date']", "e => e.outerHTML"));
    assertEquals("<input placeholder=\"Select date\">", page.evalOnSelector("div >> [placeholder=\"Select date\"]", "e => e.outerHTML"));
    assertEquals("<input placeholder=\"Select date\">", page.evalOnSelector("div >> [placeholder='Select date']", "e => e.outerHTML"));
  }

  @PlaywrightTest
  @DisplayName("ES-023 should work with quotes in css attributes")
  void shouldWorkWithQuotesInCssAttributes(Page page, TestServer server) {
    page.setContent("<div><input placeholder=\"Select&quot;date\"></div>");
    assertNotNull(page.querySelector("[placeholder=\"Select\\\"date\"]"));
    assertNotNull(page.querySelector("[placeholder='Select\"date']"));
    page.setContent("<div><input placeholder=\"Select &quot; date\"></div>");
    assertNotNull(page.querySelector("[placeholder=\"Select \\\" date\"]"));
    assertNotNull(page.querySelector("[placeholder='Select \" date']"));
    page.setContent("<div><input placeholder=\"Select&apos;date\"></div>");
    assertNotNull(page.querySelector("[placeholder=\"Select'date\"]"));
    assertNotNull(page.querySelector("[placeholder='Select\\'date']"));
    page.setContent("<div><input placeholder=\"Select &apos; date\"></div>");
    assertNotNull(page.querySelector("[placeholder=\"Select ' date\"]"));
    assertNotNull(page.querySelector("[placeholder='Select \\' date']"));
  }

  @PlaywrightTest
  @DisplayName("ES-024 should work with spaces in css attributes when missing")
  void shouldWorkWithSpacesInCssAttributesWhenMissing(Page page, TestServer server) throws Exception {
    assertNull(page.querySelector("[placeholder=\"Select date\"]"));
    page.setContent("<div><input placeholder=\"Select date\"></div>");
    // Java: the wait comes after setContent, the input stays attached.
    page.waitForSelector("[placeholder=\"Select date\"]");
  }

  @PlaywrightTest
  @DisplayName("ES-025 should work with quotes in css attributes when missing")
  void shouldWorkWithQuotesInCssAttributesWhenMissing(Page page, TestServer server) throws Exception {
    assertNull(page.querySelector("[placeholder=\"Select\\\"date\"]"));
    page.setContent("<div><input placeholder=\"Select&quot;date\"></div>");
    // Java: the wait comes after setContent, the input stays attached.
    page.waitForSelector("[placeholder=\"Select\\\"date\"]");
  }

  @PlaywrightTest
  @DisplayName("ES-026 should return complex values")
  void shouldReturnComplexValues(Page page, TestServer server) {
    page.setContent("<section id=\"testAttribute\">43543</section>");
    Object idAttribute = page.evalOnSelector("css=section", "e => [{ id: e.id }]");
    assertEquals(Arrays.asList(mapOf("id", "testAttribute")), idAttribute);
  }
}
