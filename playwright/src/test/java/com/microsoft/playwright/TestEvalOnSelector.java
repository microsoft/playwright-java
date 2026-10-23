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

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class TestEvalOnSelector {

  @PlaywrightTest
  void shouldWorkWithCssSelector(Page page) {
    page.setContent("<section id='testAttribute'>43543</section>");
    Object idAttribute = page.evalOnSelector("css=section", "e => e.id");
    assertEquals("testAttribute", idAttribute);
  }

  @PlaywrightTest
  void shouldWorkWithIdSelector(Page page) {
    page.setContent("<section id='testAttribute'>43543</section>");
    Object idAttribute = page.evalOnSelector("id=testAttribute", "e => e.id");
    assertEquals("testAttribute", idAttribute);
  }

  @PlaywrightTest
  void shouldWorkWithDataTestSelector(Page page) {
    page.setContent("<section data-test=foo id='testAttribute'>43543</section>");
    Object idAttribute = page.evalOnSelector("data-test=foo", "e => e.id");
    assertEquals("testAttribute", idAttribute);
  }

  @PlaywrightTest
  void shouldWorkWithDataTestidSelector(Page page) {
    page.setContent("<section data-testid=foo id='testAttribute'>43543</section>");
    Object idAttribute = page.evalOnSelector("data-testid=foo", "e => e.id");
    assertEquals("testAttribute", idAttribute);
  }

  @PlaywrightTest
  void shouldWorkWithDataTestIdSelector(Page page) {
    page.setContent("<section data-test-id=foo id='testAttribute'>43543</section>");
    Object idAttribute = page.evalOnSelector("data-test-id=foo", "e => e.id");
    assertEquals("testAttribute", idAttribute);
  }

  @PlaywrightTest
  void shouldWorkWithTextSelector1(Page page) {
    page.setContent("<section id='testAttribute'>43543</section>");
    Object idAttribute = page.evalOnSelector("text='43543'", "e => e.id");
    assertEquals("testAttribute", idAttribute);
  }

  @PlaywrightTest
  void shouldWorkWithXpathSelector(Page page) {
    page.setContent("<section id='testAttribute'>43543</section>");
    Object idAttribute = page.evalOnSelector("xpath=/html/body/section", "e => e.id");
    assertEquals("testAttribute", idAttribute);
  }

  @PlaywrightTest
  void shouldWorkWithTextSelector2(Page page) {
    page.setContent("<section id='testAttribute'>43543</section>");
    Object idAttribute = page.evalOnSelector("text=43543", "e => e.id");
    assertEquals("testAttribute", idAttribute);
  }

  @PlaywrightTest
  void shouldAutoDetectCssSelector(Page page) {
    page.setContent("<section id='testAttribute'>43543</section>");
    Object idAttribute = page.evalOnSelector("section", "e => e.id");
    assertEquals("testAttribute", idAttribute);
  }

  @PlaywrightTest
  void shouldAutoDetectCssSelectorWithAttributes(Page page) {
    page.setContent("<section id='testAttribute'>43543</section>");
    Object idAttribute = page.evalOnSelector("section[id='testAttribute']", "e => e.id");
    assertEquals("testAttribute", idAttribute);
  }

  @PlaywrightTest
  void shouldAutoDetectNestedSelectors(Page page) {
    page.setContent("<div foo=bar><section>43543<span>Hello<div id=target></div></span></section></div>");
    Object idAttribute = page.evalOnSelector("div[foo=bar] > section >> 'Hello' >> div", "e => e.id");
    assertEquals("target", idAttribute);
  }

  @PlaywrightTest
  void shouldAcceptArguments(Page page) {
    page.setContent("<section>hello</section>");
    Object text = page.evalOnSelector("section", "(e, suffix) => e.textContent + suffix", " world!");
    assertEquals("hello world!", text);
  }

  @PlaywrightTest
  void shouldAcceptElementHandlesAsArguments(Page page) {
    page.setContent("<section>hello</section><div> world</div>");
    ElementHandle divHandle = page.querySelector("div");
    Object text = page.evalOnSelector("section", "(e, div) => e.textContent + div.textContent", divHandle);
    assertEquals("hello world", text);
  }

  @PlaywrightTest
  void shouldThrowErrorIfNoElementIsFound(Page page) {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.evalOnSelector("section", "e => e.id");
    });
    assertTrue(e.getMessage().contains("Failed to find element matching selector \"section\""), e.getMessage());
  }

  @PlaywrightTest
  void shouldSupportSyntax(Page page) {
    page.setContent("<section><div>hello</div></section>");
    Object text = page.evalOnSelector("css=section >> css=div", "(e, suffix) => e.textContent + suffix", " world!");
    assertEquals("hello world!", text);
  }

  @PlaywrightTest
  void shouldSupportSyntaxWithDifferentEngines(Page page) {
    page.setContent("<section><div><span>hello</span></div></section>");
    Object text = page.evalOnSelector("xpath=/html/body/section >> css=div >> text='hello'", "(e, suffix) => e.textContent + suffix", " world!");
    assertEquals("hello world!", text);
  }

  @PlaywrightTest
  void shouldSupportSpacesWithSyntax(Page page, Server server) {
    page.navigate(server.PREFIX + "/deep-shadow.html");
    Object text = page.evalOnSelector(" css = div >>css=div>>css   = span  ", "e => e.textContent");
    assertEquals("Hello from root2", text);
  }

  @PlaywrightTest
  void shouldNotStopAtFirstFailureWithSyntax(Page page) {
    page.setContent("<div><span>Next</span><button>Previous</button><button>Next</button></div>");
    Object html = page.evalOnSelector("button >> 'Next'", "e => e.outerHTML");
    assertEquals("<button>Next</button>", html);
  }

  @PlaywrightTest
  void shouldSupportCapture(Page page) {
    page.setContent("<section><div><span>a</span></div></section><section><div><span>b</span></div></section>");
    assertEquals("<div><span>b</span></div>", page.evalOnSelector("*css=div >> 'b'", "e => e.outerHTML"));
    assertEquals("<div><span>b</span></div>", page.evalOnSelector("section >> *css=div >> 'b'", "e => e.outerHTML"));
    assertEquals("<span>b</span>", page.evalOnSelector("css=div >> *text='b'", "e => e.outerHTML"));
    assertNotNull(page.querySelector("*"));
  }

  @PlaywrightTest
  void shouldThrowOnMultipleCaptures(Page page) {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.evalOnSelector("*css=div >> *css=span", "e => e.outerHTML");
    });
    assertTrue(e.getMessage().contains("Only one of the selectors can capture using * modifier"));
  }

  @PlaywrightTest
  void shouldThrowOnMalformedCapture(Page page) {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.evalOnSelector("*=div", "e => e.outerHTML");
    });
    assertTrue(e.getMessage().contains("Unknown engine \"\" while parsing selector *=div"));
  }

  @PlaywrightTest
  void shouldWorkWithSpacesInCssAttributes(Page page) {
    page.setContent("<div><input placeholder='Select date'></div>");
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
  void shouldWorkWithQuotesInCssAttributes(Page page) {
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
  void shouldWorkWithSpacesInCssAttributesWhenMissing(Page page) {
    assertNull(page.querySelector("[placeholder='Select date']"));
    page.setContent("<div><input placeholder='Select date'></div>");
    page.waitForSelector("[placeholder='Select date']");
  }

  @PlaywrightTest
  void shouldWorkWithQuotesInCssAttributesWhenMissing(Page page) {
    assertNull(page.querySelector("[placeholder='Select\\\"date']"));
    page.setContent("<div><input placeholder='Select&quot;date'></div>");
    page.waitForSelector("[placeholder='Select\\\"date']");
  }

  @PlaywrightTest
  void shouldReturnComplexValues(Page page) {
    page.setContent("<section id='testAttribute'>43543</section>");
    Object idAttribute = page.evalOnSelector("css=section", "e => [{ id: e.id }]");
    assertEquals(Arrays.asList(Collections.singletonMap("id", "testAttribute")), idAttribute);
  }

}
