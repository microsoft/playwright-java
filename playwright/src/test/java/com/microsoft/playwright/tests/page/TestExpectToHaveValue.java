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

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.opentest4j.AssertionFailedError;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.stripAnsi;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/expect-to-have-value.spec.ts. */
public class TestExpectToHaveValue {
  @PlaywrightTest
  @DisplayName("EX-302 should work")
  void shouldWork(Page page) {
    page.setContent("<input id=node></input>");
    Locator locator = page.locator("#node");
    locator.fill("Text content");
    assertThat(locator).hasValue("Text content");
  }

  @PlaywrightTest
  @DisplayName("EX-303 should work with label")
  void shouldWorkWithLabel(Page page) {
    page.setContent("<label><input></input></label>");
    page.locator("label input").fill("Text content");
    assertThat(page.locator("label")).hasValue("Text content");
  }

  @PlaywrightTest
  @DisplayName("EX-304 should work with regex")
  void shouldWorkWithRegex(Page page) {
    page.setContent("<input id=node></input>");
    Locator locator = page.locator("#node");
    locator.fill("Text content");
    assertThat(locator).hasValue(Pattern.compile("Text"));
  }

  @PlaywrightTest
  @DisplayName("EX-305 should support failure")
  void shouldSupportFailure(Page page) {
    page.setContent("<input id=node></input>");
    Locator locator = page.locator("#node");
    locator.fill("Text content");
    AssertionFailedError error = assertThrows(AssertionFailedError.class,
      () -> assertThat(locator).hasValue(Pattern.compile("Text2"), new LocatorAssertions.HasValueOptions().setTimeout(1000)));
    assertTrue(stripAnsi(error.getMessage()).contains("\"Text content\""));
  }

  @Nested
  class ToHaveValuesWithMultiSelect {
    @PlaywrightTest
    @DisplayName("EX-306 works with text")
    void worksWithText(Page page) {
      page.setContent("\n" +
        "      <select multiple>\n" +
        "        <option value=\"R\">Red</option>\n" +
        "        <option value=\"G\">Green</option>\n" +
        "        <option value=\"B\">Blue</option>\n" +
        "      </select>\n" +
        "    ");
      Locator locator = page.locator("select");
      locator.selectOption(new String[]{"R", "G"});
      assertThat(locator).hasValues(new String[]{"R", "G"});
    }

    @PlaywrightTest
    @DisplayName("EX-307 follows labels")
    void followsLabels(Page page) {
      page.setContent("\n" +
        "      <label for=\"colors\">Pick a Color</label>\n" +
        "      <select id=\"colors\" multiple>\n" +
        "        <option value=\"R\">Red</option>\n" +
        "        <option value=\"G\">Green</option>\n" +
        "        <option value=\"B\">Blue</option>\n" +
        "      </select>\n" +
        "    ");
      Locator locator = page.locator("text=Pick a Color");
      locator.selectOption(new String[]{"R", "G"});
      assertThat(locator).hasValues(new String[]{"R", "G"});
    }

    @PlaywrightTest
    @DisplayName("EX-308 exact match with text failure")
    @Fixme("java: the assertion error has another format: 'Locator expected to have values\nExpected: [R, G]\nReceived: [RR, GG]\n\nCall log:\n- Assert \"hasValues\" locator(\"select\") with timeout 1000ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
    void exactMatchWithTextFailure(Page page) {
      page.setContent("\n" +
        "      <select multiple>\n" +
        "        <option value=\"RR\">Red</option>\n" +
        "        <option value=\"GG\">Green</option>\n" +
        "      </select>\n" +
        "    ");
      Locator locator = page.locator("select");
      locator.selectOption(new String[]{"RR", "GG"});
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).hasValues(new String[]{"R", "G"}, new LocatorAssertions.HasValuesOptions().setTimeout(1000)));
      assertTrue(stripAnsi(error.getMessage()).contains("-   \"R\""));
      assertTrue(stripAnsi(error.getMessage()).contains("+   \"RR\""));
    }

    @PlaywrightTest
    @DisplayName("EX-309 works with regex")
    void worksWithRegex(Page page) {
      page.setContent("\n" +
        "      <select multiple>\n" +
        "        <option value=\"R\">Red</option>\n" +
        "        <option value=\"G\">Green</option>\n" +
        "        <option value=\"B\">Blue</option>\n" +
        "      </select>\n" +
        "    ");
      Locator locator = page.locator("select");
      locator.selectOption(new String[]{"R", "G"});
      assertThat(locator).hasValues(new Pattern[]{Pattern.compile("R"), Pattern.compile("G")});
    }

    @PlaywrightTest
    @DisplayName("EX-310 fails when items not selected")
    @Fixme("java: the assertion error has another format: 'Locator expected to have values matching regex\nExpected: [R, G]\nReceived: [B]\n\nCall log:\n- Assert \"hasValues\" locator(\"select\") with timeout 1000ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
    void failsWhenItemsNotSelected(Page page) {
      page.setContent("\n" +
        "      <select multiple>\n" +
        "        <option value=\"R\">Red</option>\n" +
        "        <option value=\"G\">Green</option>\n" +
        "        <option value=\"B\">Blue</option>\n" +
        "      </select>\n" +
        "    ");
      Locator locator = page.locator("select");
      locator.selectOption(new String[]{"B"});
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).hasValues(new Pattern[]{Pattern.compile("R"), Pattern.compile("G")}, new LocatorAssertions.HasValuesOptions().setTimeout(1000)));
      assertTrue(stripAnsi(error.getMessage()).contains("+   \"B\""));
    }

    @PlaywrightTest
    @DisplayName("EX-311 fails when multiple not specified")
    void failsWhenMultipleNotSpecified(Page page) {
      page.setContent("\n" +
        "      <select>\n" +
        "        <option value=\"R\">Red</option>\n" +
        "        <option value=\"G\">Green</option>\n" +
        "        <option value=\"B\">Blue</option>\n" +
        "      </select>\n" +
        "    ");
      Locator locator = page.locator("select");
      locator.selectOption(new String[]{"B"});
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).hasValues(new Pattern[]{Pattern.compile("R"), Pattern.compile("G")}, new LocatorAssertions.HasValuesOptions().setTimeout(1000)));
      assertTrue(error.getMessage().contains("Not a select element with a multiple attribute"));
    }

    @PlaywrightTest
    @DisplayName("EX-312 fails when not a select element")
    void failsWhenNotASelectElement(Page page) {
      page.setContent("\n" +
        "      <input value=\"foo\" />\n" +
        "    ");
      Locator locator = page.locator("input");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).hasValues(new Pattern[]{Pattern.compile("R"), Pattern.compile("G")}, new LocatorAssertions.HasValuesOptions().setTimeout(1000)));
      assertTrue(error.getMessage().contains("Not a select element with a multiple attribute"));
    }
  }
}
