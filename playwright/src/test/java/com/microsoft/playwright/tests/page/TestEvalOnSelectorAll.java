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

import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Port of tests/page/eval-on-selector-all.spec.ts. */
public class TestEvalOnSelectorAll {
  @PlaywrightTest
  @DisplayName("ES-027 should work with css selector")
  void shouldWorkWithCssSelector(Page page, TestServer server) {
    page.setContent("<div>hello</div><div>beautiful</div><div>world!</div>");
    Object divsCount = page.evalOnSelectorAll("css=div", "divs => divs.length");
    assertEquals(3, divsCount);
  }

  @PlaywrightTest
  @DisplayName("ES-028 should work with text selector")
  void shouldWorkWithTextSelector(Page page, TestServer server) {
    page.setContent("<div>hello</div><div>beautiful</div><div>beautiful</div><div>world!</div>");
    Object divsCount = page.evalOnSelectorAll("text=\"beautiful\"", "divs => divs.length");
    assertEquals(2, divsCount);
  }

  @PlaywrightTest
  @DisplayName("ES-029 should work with xpath selector")
  void shouldWorkWithXpathSelector(Page page, TestServer server) {
    page.setContent("<div>hello</div><div>beautiful</div><div>world!</div>");
    Object divsCount = page.evalOnSelectorAll("xpath=/html/body/div", "divs => divs.length");
    assertEquals(3, divsCount);
  }

  @PlaywrightTest
  @DisplayName("ES-030 should auto-detect css selector")
  void shouldAutoDetectCssSelector(Page page, TestServer server) {
    page.setContent("<div>hello</div><div>beautiful</div><div>world!</div>");
    Object divsCount = page.evalOnSelectorAll("div", "divs => divs.length");
    assertEquals(3, divsCount);
  }

  @PlaywrightTest
  @DisplayName("ES-031 should support >> syntax")
  void shouldSupportSyntax(Page page, TestServer server) {
    page.setContent("<div><span>hello</span></div><div>beautiful</div><div><span>wo</span><span>rld!</span></div><span>Not this one</span>");
    Object spansCount = page.evalOnSelectorAll("css=div >> css=span", "spans => spans.length");
    assertEquals(3, spansCount);
  }

  @PlaywrightTest
  @DisplayName("ES-032 should support * capture")
  void shouldSupportCapture(Page page, TestServer server) {
    page.setContent("<section><div><span>a</span></div></section><section><div><span>b</span></div></section>");
    assertEquals(1, page.evalOnSelectorAll("*css=div >> \"b\"", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("section >> *css=div >> \"b\"", "els => els.length"));
    assertEquals(4, page.evalOnSelectorAll("section >> *", "els => els.length"));

    page.setContent("<section><div><span>a</span><span>a</span></div></section>");
    assertEquals(1, page.evalOnSelectorAll("*css=div >> \"a\"", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("section >> *css=div >> \"a\"", "els => els.length"));

    page.setContent("<div><span>a</span></div><div><span>a</span></div><section><div><span>a</span></div></section>");
    assertEquals(3, page.evalOnSelectorAll("*css=div >> \"a\"", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("section >> *css=div >> \"a\"", "els => els.length"));
  }

  @PlaywrightTest
  @DisplayName("ES-033 should support * capture when multiple paths match")
  void shouldSupportCaptureWhenMultiplePathsMatch(Page page, TestServer server) {
    page.setContent("<div><div><span></span></div></div><div></div>");
    assertEquals(2, page.evalOnSelectorAll("*css=div >> span", "els => els.length"));
    page.setContent("<div><div><span></span></div><span></span><span></span></div><div></div>");
    assertEquals(2, page.evalOnSelectorAll("*css=div >> span", "els => els.length"));
  }

  @PlaywrightTest
  @DisplayName("ES-034 should return complex values")
  void shouldReturnComplexValues(Page page, TestServer server) {
    page.setContent("<div>hello</div><div>beautiful</div><div>world!</div>");
    Object texts = page.evalOnSelectorAll("css=div", "divs => divs.map(div => div.textContent)");
    assertEquals(Arrays.asList("hello", "beautiful", "world!"), texts);
  }

  @PlaywrightTest
  @DisplayName("ES-035 should work with bogus Array.from")
  void shouldWorkWithBogusArrayFrom(Page page, TestServer server) {
    page.setContent("<div>hello</div><div>beautiful</div><div>world!</div>");
    page.evaluate("() => {\n" +
      "    Array.from = () => [];\n" +
      "  }");
    Object divsCount = page.evalOnSelectorAll("css=div", "divs => divs.length");
    assertEquals(3, divsCount);
  }
}
