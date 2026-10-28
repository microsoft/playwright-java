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
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.DisplayName;
import org.opentest4j.AssertionFailedError;

import java.util.HashMap;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-strict.spec.ts. */
public class TestPageStrict {
  @PlaywrightTest
  @DisplayName("SM-001 should fail page.textContent in strict mode")
  @Fixme("java: the driver prints Java locator syntax with double quotes: '1) <span>span1</span> aka getByText(\"span1\")'")
  void shouldFailPageTextContentInStrictMode(Page page) {
    page.setContent("<span>span1</span><div><span>target</span></div>");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.textContent("span", new Page.TextContentOptions().setStrict(true)));
    assertTrue(error.getMessage().contains("strict mode violation"));
    assertTrue(error.getMessage().contains("1) <span>span1</span> aka getByText('span1')"));
    assertTrue(error.getMessage().contains("2) <span>target</span> aka getByText('target')"));
  }

  @PlaywrightTest
  @DisplayName("SM-002 should fail page.getAttribute in strict mode")
  void shouldFailPageGetAttributeInStrictMode(Page page) {
    page.setContent("<span>span1</span><div><span>target</span></div>");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.getAttribute("span", "id", new Page.GetAttributeOptions().setStrict(true)));
    assertTrue(error.getMessage().contains("strict mode violation"));
  }

  @PlaywrightTest
  @DisplayName("SM-003 should fail page.fill in strict mode")
  @Fixme("java: the driver prints Java locator syntax: '1) <input/> aka getByRole(AriaRole.TEXTBOX).first()'")
  void shouldFailPageFillInStrictMode(Page page) {
    page.setContent("<input></input><div><input></input></div>");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.fill("input", "text", new Page.FillOptions().setStrict(true)));
    assertTrue(error.getMessage().contains("strict mode violation"));
    assertTrue(error.getMessage().contains("1) <input/> aka getByRole('textbox').first()"));
    assertTrue(error.getMessage().contains("2) <input/> aka locator('div').getByRole('textbox')"));
  }

  @PlaywrightTest
  @DisplayName("SM-004 should fail page.$ in strict mode")
  void shouldFailPageInStrictMode(Page page) {
    page.setContent("<span>span1</span><div><span>target</span></div>");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.querySelector("span", new Page.QuerySelectorOptions().setStrict(true)));
    assertTrue(error.getMessage().contains("strict mode violation"));
  }

  @PlaywrightTest
  @DisplayName("SM-005 should fail page.waitForSelector in strict mode")
  void shouldFailPageWaitForSelectorInStrictMode(Page page) {
    page.setContent("<span>span1</span><div><span>target</span></div>");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.waitForSelector("span", new Page.WaitForSelectorOptions().setStrict(true)));
    assertTrue(error.getMessage().contains("strict mode violation"));
  }

  @PlaywrightTest
  @DisplayName("SM-006 should fail page.dispatchEvent in strict mode")
  @Fixme("java: the driver prints Java locator syntax with double quotes: '1) <span></span> aka locator(\"span\").first()'")
  void shouldFailPageDispatchEventInStrictMode(Page page) {
    page.setContent("<span></span><div><span></span></div>");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.dispatchEvent("span", "click", new HashMap<>(), new Page.DispatchEventOptions().setStrict(true)));
    assertTrue(error.getMessage().contains("strict mode violation"));
    assertTrue(error.getMessage().contains("1) <span></span> aka locator('span').first()"));
    assertTrue(error.getMessage().contains("2) <span></span> aka locator('div span')"));
  }

  @PlaywrightTest
  @DisplayName("SM-007 should properly format :nth-child() in strict mode message")
  void shouldProperlyFormatNthChildInStrictModeMessage(Page page) {
    page.setContent("\n" +
      "  <div>\n" +
      "    <div>\n" +
      "    </div>\n" +
      "    <div>\n" +
      "      <div>\n" +
      "      </div>\n" +
      "      <div>\n" +
      "      </div>\n" +
      "    </div>\n" +
      "  </div>\n" +
      "  <div>\n" +
      "    <div class='foo'>\n" +
      "    </div>\n" +
      "    <div class='foo'>\n" +
      "    </div>\n" +
      "  </div>\n" +
      "  ");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.locator(".foo").hover());
    assertTrue(error.getMessage().contains("strict mode violation"));
    // Second div has body > div:nth-child(2) > div:nth-child(2) selector which would be ambiguous
    // if '>' were ' '.
    assertTrue(error.getMessage().contains("body > div:nth-child(2) > div:nth-child(2)"));
  }

  @PlaywrightTest
  @DisplayName("SM-008 should escape class names")
  void shouldEscapeClassNames(Page page) {
    page.setContent("\n" +
      "  <div>\n" +
      "    <div></div>\n" +
      "    <div>\n" +
      "      <div></div>\n" +
      "      <div></div>\n" +
      "    </div>\n" +
      "  </div>\n" +
      "  <div>\n" +
      "    <div class='foo bar:0'>\n" +
      "    </div>\n" +
      "    <div class='foo bar:1'>\n" +
      "    </div>\n" +
      "  </div>\n" +
      "  ");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.locator(".foo").hover());
    assertTrue(error.getMessage().contains("strict mode violation"));
    assertTrue(error.getMessage().contains("<div class=\"foo bar:0"));
    assertTrue(error.getMessage().contains("<div class=\"foo bar:1"));
  }

  @PlaywrightTest
  @DisplayName("SM-009 should escape tag names")
  @Fixme("java: the driver prints Java locator syntax: 'aka getByText(\"special test description\").first()' and 'aka locator(\"q\\\\:template\").filter(new Locator.FilterOptions().setHasText(\"special test description\")).locator(\"span\")'")
  void shouldEscapeTagNames(Page page) {
    page.setContent("\n" +
      "    <q:template> </q:template>\n" +
      "    <span>special test description</span>\n" +
      "    <q:template hidden=\"\" aria-hidden=\"true\">\n" +
      "      <span>special test description</span>\n" +
      "    </q:template>\n" +
      "  ");
    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertThat(page.getByText("special test description")).isVisible());
    assertTrue(error.getMessage().contains("strict mode violation"));
    assertTrue(error.getMessage().contains("getByText('special test description').first()"));
    assertTrue(error.getMessage().contains("locator('q\\\\:template').filter({ hasText: 'special test description' })"));
  }

  @PlaywrightTest
  @DisplayName("SM-010 should keep the engine name for a \"text=..\" selector in strict mode")
  @Fixme("java: the driver prints Java locator syntax with double quotes: 'strict mode violation: locator(\"text=..loading\") resolved to 2 elements'")
  void shouldKeepTheEngineNameForATextSelectorInStrictMode(Page page) {
    page.setContent("<div>..loading</div><div>..loading</div>");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.locator("text=..loading").hover());
    assertTrue(error.getMessage().contains("strict mode violation"));
    assertTrue(error.getMessage().contains("locator('text=..loading')"));
  }
}
