/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
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
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Port of tests/page/selectors-register.spec.ts. */
public class TestSelectorsRegister {
  // it.skip(!!process.env.PW_TEST_CONNECT_WS_ENDPOINT, 'selectors.register does not support reuse') is dropped, the tests never reuse a browser.

  // Java: selectors.register() takes the script of the engine instead of a function, createDummySelector is the script.
  private static String createDummySelector(String modify) {
    return "{\n" +
      "    query(root, selector) {\n" +
      "      const result = root.querySelector(selector);\n" +
      "      if (result)\n" +
      "        void Promise.resolve().then(() => " + modify.replace("$e", "result") + ");\n" +
      "      return result;\n" +
      "    },\n" +
      "    queryAll(root, selector) {\n" +
      "      const result = Array.from(root.querySelectorAll(selector));\n" +
      "      for (const e of result)\n" +
      "        void Promise.resolve().then(() => " + modify.replace("$e", "e") + ");\n" +
      "      return result;\n" +
      "    }\n" +
      "  }";
  }

  @PlaywrightTest
  @DisplayName("SE-154 textContent should be atomic")
  void textContentShouldBeAtomic(Playwright playwright, Page page) {
    playwright.selectors().register("textContent", createDummySelector("$e.textContent = 'modified'"));
    page.navigate("data:text/html,<div>Hello</div>");
    String tc = page.textContent("textContent=div");
    assertEquals("Hello", tc);
    assertEquals("modified", page.evaluate("() => document.querySelector('div').textContent"));
  }

  @PlaywrightTest
  @DisplayName("SE-155 innerText should be atomic")
  void innerTextShouldBeAtomic(Playwright playwright, Page page) {
    playwright.selectors().register("innerText", createDummySelector("$e.textContent = 'modified'"));
    page.navigate("data:text/html,<div>Hello</div>");
    String tc = page.innerText("innerText=div");
    assertEquals("Hello", tc);
    assertEquals("modified", page.evaluate("() => document.querySelector('div').innerText"));
  }

  @PlaywrightTest
  @DisplayName("SE-156 innerHTML should be atomic")
  void innerHTMLShouldBeAtomic(Playwright playwright, Page page) {
    playwright.selectors().register("innerHTML", createDummySelector("$e.textContent = 'modified'"));
    page.navigate("data:text/html,<div>Hello<span>world</span></div>");
    String tc = page.innerHTML("innerHTML=div");
    assertEquals("Hello<span>world</span>", tc);
    assertEquals("modified", page.evaluate("() => document.querySelector('div').innerHTML"));
  }

  @PlaywrightTest
  @DisplayName("SE-157 getAttribute should be atomic")
  void getAttributeShouldBeAtomic(Playwright playwright, Page page) {
    playwright.selectors().register("getAttribute", createDummySelector("$e.setAttribute('foo', 'modified')"));
    page.navigate("data:text/html,<div foo=hello></div>");
    String tc = page.getAttribute("getAttribute=div", "foo");
    assertEquals("hello", tc);
    assertEquals("modified", page.evaluate("() => document.querySelector('div').getAttribute('foo')"));
  }

  @PlaywrightTest
  @DisplayName("SE-158 isVisible should be atomic")
  void isVisibleShouldBeAtomic(Playwright playwright, Page page) {
    playwright.selectors().register("isVisible", createDummySelector("$e.style.display = 'none'"));
    page.navigate("data:text/html,<div>Hello</div>");
    boolean result = page.isVisible("isVisible=div");
    assertEquals(true, result);
    assertEquals("none", page.evaluate("() => document.querySelector('div').style.display"));
  }

  @PlaywrightTest
  @DisplayName("SE-159 should take java-style string")
  void shouldTakeJavaStyleString(Playwright playwright, Page page) {
    String createDummySelector = "{\n" +
      "    query(root, selector) {\n" +
      "      return root.querySelector(selector);\n" +
      "    },\n" +
      "    queryAll(root, selector) {\n" +
      "      return root.querySelectorAll(selector);\n" +
      "    }\n" +
      "  }";
    playwright.selectors().register("objectLiteral", createDummySelector);
    page.navigate("data:text/html,<div>Hello</div>");
    page.textContent("objectLiteral=div");
  }
}
