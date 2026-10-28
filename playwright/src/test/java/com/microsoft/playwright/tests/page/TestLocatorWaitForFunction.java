/*
 * Copyright (c) Microsoft Corporation.
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
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/locator-wait-for-function.spec.ts. */
public class TestLocatorWaitForFunction {
  @PlaywrightTest
  @DisplayName("LO-183 should wait for an attribute to appear")
  void shouldWaitForAnAttributeToAppear(Page page) {
    page.setContent("<button id=toggle>Menu</button>");
    page.evaluate("() => window.builtins.setTimeout(() => document.querySelector('#toggle').setAttribute('aria-expanded', 'true'), 1000)");
    page.locator("#toggle").waitForFunction("element => element.hasAttribute('aria-expanded')");
  }

  @PlaywrightTest
  @DisplayName("LO-184 should return immediately when already truthy")
  void shouldReturnImmediatelyWhenAlreadyTruthy(Page page) {
    page.setContent("<div id=target>yes</div>");
    // Java: waitForFunction() returns void, the undefined result is not checked.
    page.locator("#target").waitForFunction("element => element.textContent === 'yes'");
  }

  @PlaywrightTest
  @DisplayName("LO-185 should accept ElementHandle arguments")
  void shouldAcceptElementHandleArguments(Page page) {
    page.setContent("<div id=a></div><div id=b>value</div>");
    ElementHandle handle = page.querySelector("#b");
    page.locator("#a").waitForFunction("(element, other) => other.textContent === 'value'", handle);
  }

  @PlaywrightTest
  @DisplayName("LO-186 should accept string expression")
  void shouldAcceptStringExpression(Page page) {
    page.setContent("<div id=target>yes</div>");
    page.locator("#target").waitForFunction("element => element.textContent === 'yes'");
  }

  @PlaywrightTest
  @DisplayName("LO-187 should resolve a promise returned by the predicate")
  void shouldResolveAPromiseReturnedByThePredicate(Page page) {
    page.setContent("<div id=target>yes</div>");
    page.locator("#target").waitForFunction("async element => element.textContent === 'yes'");
  }

  @PlaywrightTest
  @DisplayName("LO-188 should wait for element to appear and survive rerender")
  void shouldWaitForElementToAppearAndSurviveRerender(Page page) {
    page.setContent("<span>nothing here</span>");
    page.evaluate("() => {\n" +
      "  let count = 0;\n" +
      "  let prev = null;\n" +
      "  const tick = () => {\n" +
      "    ++count;\n" +
      "    const next = document.createElement('div');\n" +
      "    next.id = 'target';\n" +
      "    next.textContent = String(count);\n" +
      "    if (prev)\n" +
      "      prev.remove();\n" +
      "    document.body.appendChild(next);\n" +
      "    prev = next;\n" +
      "    if (count < 3)\n" +
      "      window.builtins.setTimeout(tick, 500);\n" +
      "  };\n" +
      "  window.builtins.setTimeout(tick, 500);\n" +
      "}");
    page.locator("#target").waitForFunction("element => element.textContent === '3'");
  }

  @PlaywrightTest
  @DisplayName("LO-189 should throw when predicate throws")
  void shouldThrowWhenPredicateThrows(Page page) {
    page.setContent("<div id=target>no</div>");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.locator("#target").waitForFunction("() => {\n" +
      "  throw new Error('oh my');\n" +
      "}"));
    assertTrue(error.getMessage().contains("oh my"));
  }

  @PlaywrightTest
  @DisplayName("LO-190 should throw on strict mode violation")
  void shouldThrowOnStrictModeViolation(Page page) {
    page.setContent("<div class=x>1</div><div class=x>2</div>");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.locator("div.x").waitForFunction("() => true"));
    assertTrue(error.getMessage().contains("strict mode violation"));
  }
}
