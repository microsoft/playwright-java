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

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestEvalOnSelectorAll extends TestBase {

  @Test
  void shouldWorkWithCssSelector() {
    page.setContent("<div>hello</div><div>beautiful</div><div>world!</div>");
    Object divsCount = page.evalOnSelectorAll("css=div", "divs => divs.length");
    assertEquals(3, divsCount);
  }

  @Test
  void shouldWorkWithTextSelector() {
    page.setContent("<div>hello</div><div>beautiful</div><div>beautiful</div><div>world!</div>");
    Object divsCount = page.evalOnSelectorAll("text='beautiful'", "divs => divs.length");
    assertEquals(2, divsCount);
  }

  @Test
  void shouldWorkWithXpathSelector() {
    page.setContent("<div>hello</div><div>beautiful</div><div>world!</div>");
    Object divsCount = page.evalOnSelectorAll("xpath=/html/body/div", "divs => divs.length");
    assertEquals(3, divsCount);
  }

  @Test
  void shouldAutoDetectCssSelector() {
    page.setContent("<div>hello</div><div>beautiful</div><div>world!</div>");
    Object divsCount = page.evalOnSelectorAll("div", "divs => divs.length");
    assertEquals(3, divsCount);
  }

  @Test
  void shouldSupportSyntax() {
    page.setContent("<div><span>hello</span></div><div>beautiful</div><div><span>wo</span><span>rld!</span></div><span>Not this one</span>");
    Object spansCount = page.evalOnSelectorAll("css=div >> css=span", "spans => spans.length");
    assertEquals(3, spansCount);
  }
  @Test
  void shouldSupportCapture() {
    page.setContent("<section><div><span>a</span></div></section><section><div><span>b</span></div></section>");
    assertEquals(1, page.evalOnSelectorAll("*css=div >> 'b'", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("section >> *css=div >> 'b'", "els => els.length"));
    assertEquals(4, page.evalOnSelectorAll("section >> *", "els => els.length"));

    page.setContent("<section><div><span>a</span><span>a</span></div></section>");
    assertEquals(1, page.evalOnSelectorAll("*css=div >> 'a'", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("section >> *css=div >> 'a'", "els => els.length"));

    page.setContent("<div><span>a</span></div><div><span>a</span></div><section><div><span>a</span></div></section>");
    assertEquals(3, page.evalOnSelectorAll("*css=div >> 'a'", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("section >> *css=div >> 'a'", "els => els.length"));
  }

  @Test
  void shouldSupportCaptureWhenMultiplePathsMatch() {
    page.setContent("<div><div><span></span></div></div><div></div>");
    assertEquals(2, page.evalOnSelectorAll("*css=div >> span", "els => els.length"));
    page.setContent("<div><div><span></span></div><span></span><span></span></div><div></div>");
    assertEquals(2, page.evalOnSelectorAll("*css=div >> span", "els => els.length"));
  }

  @Test
  void shouldReturnComplexValues() {
    page.setContent("<div>hello</div><div>beautiful</div><div>world!</div>");
    Object texts = page.evalOnSelectorAll("css=div", "divs => divs.map(div => div.textContent)");
    assertEquals(Arrays.asList("hello", "beautiful", "world!"), texts);
  }
}
