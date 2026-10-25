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
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/** Port of tests/page/expect-to-have-accessible.spec.ts. */
public class TestExpectToHaveAccessible {
  @PlaywrightTest
  @DisplayName("EX-259 toHaveAccessibleName")
  void toHaveAccessibleName(Page page) {
    page.setContent("\n" +
      "    <div role=\"button\" aria-label=\"Hello\"></div>\n" +
      "  ");
    assertThat(page.locator("div")).hasAccessibleName("Hello");
    assertThat(page.locator("div")).not().hasAccessibleName("hello");
    assertThat(page.locator("div")).hasAccessibleName("hello", new LocatorAssertions.HasAccessibleNameOptions().setIgnoreCase(true));
    assertThat(page.locator("div")).hasAccessibleName(Pattern.compile("ell\\w"));
    assertThat(page.locator("div")).not().hasAccessibleName(Pattern.compile("hello"));
    assertThat(page.locator("div")).hasAccessibleName(Pattern.compile("hello"), new LocatorAssertions.HasAccessibleNameOptions().setIgnoreCase(true));

    page.setContent("<button>foo&nbsp;bar\nbaz</button>");
    assertThat(page.locator("button")).hasAccessibleName("foo bar baz");

    page.setContent("\n" +
      "    <select>\n" +
      "      <option>&nbsp;HTML</option>\n" +
      "    </select>\n" +
      "  ");
    assertThat(page.locator("option")).hasAccessibleName("HTML");
  }

  @PlaywrightTest
  @DisplayName("EX-260 toHaveAccessibleDescription")
  void toHaveAccessibleDescription(Page page) {
    page.setContent("\n" +
      "    <div role=\"button\" aria-description=\"Hello\"></div>\n" +
      "  ");
    assertThat(page.locator("div")).hasAccessibleDescription("Hello");
    assertThat(page.locator("div")).not().hasAccessibleDescription("hello");
    assertThat(page.locator("div")).hasAccessibleDescription("hello", new LocatorAssertions.HasAccessibleDescriptionOptions().setIgnoreCase(true));
    assertThat(page.locator("div")).hasAccessibleDescription(Pattern.compile("ell\\w"));
    assertThat(page.locator("div")).not().hasAccessibleDescription(Pattern.compile("hello"));
    assertThat(page.locator("div")).hasAccessibleDescription(Pattern.compile("hello"), new LocatorAssertions.HasAccessibleDescriptionOptions().setIgnoreCase(true));

    page.setContent("\n" +
      "    <div role=\"button\" aria-describedby=\"desc\"></div>\n" +
      "    <span id=\"desc\">foo&nbsp;bar\nbaz</span>\n" +
      "  ");
    assertThat(page.locator("div")).hasAccessibleDescription("foo bar baz");
  }

  @PlaywrightTest
  @DisplayName("EX-261 toHaveAccessibleErrorMessage")
  void toHaveAccessibleErrorMessage(Page page) {
    page.setContent("\n" +
      "    <form>\n" +
      "      <input role=\"textbox\" aria-invalid=\"true\" aria-errormessage=\"error-message\" />\n" +
      "      <div id=\"error-message\">Hello</div>\n" +
      "      <div id=\"irrelevant-error\">This should not be considered.</div>\n" +
      "    </form>\n" +
      "  ");

    Locator locator = page.locator("input[role=\"textbox\"]");
    assertThat(locator).hasAccessibleErrorMessage("Hello");
    assertThat(locator).not().hasAccessibleErrorMessage("hello");
    assertThat(locator).hasAccessibleErrorMessage("hello", new LocatorAssertions.HasAccessibleErrorMessageOptions().setIgnoreCase(true));
    assertThat(locator).hasAccessibleErrorMessage(Pattern.compile("ell\\w"));
    assertThat(locator).not().hasAccessibleErrorMessage(Pattern.compile("hello"));
    assertThat(locator).hasAccessibleErrorMessage(Pattern.compile("hello"), new LocatorAssertions.HasAccessibleErrorMessageOptions().setIgnoreCase(true));
    assertThat(locator).not().hasAccessibleErrorMessage("This should not be considered.");
  }

  @PlaywrightTest
  @DisplayName("EX-262 toHaveAccessibleErrorMessage should handle multiple aria-errormessage references")
  void toHaveAccessibleErrorMessageShouldHandleMultipleAriaErrormessageReferences(Page page) {
    page.setContent("\n" +
      "    <form>\n" +
      "      <input role=\"textbox\" aria-invalid=\"true\" aria-errormessage=\"error1 error2\" />\n" +
      "      <div id=\"error1\">First error message.</div>\n" +
      "      <div id=\"error2\">Second error message.</div>\n" +
      "      <div id=\"irrelevant-error\">This should not be considered.</div>\n" +
      "    </form>\n" +
      "  ");

    Locator locator = page.locator("input[role=\"textbox\"]");

    assertThat(locator).hasAccessibleErrorMessage("First error message. Second error message.");
    assertThat(locator).hasAccessibleErrorMessage(Pattern.compile("first error message.", Pattern.CASE_INSENSITIVE));
    assertThat(locator).hasAccessibleErrorMessage(Pattern.compile("second error message.", Pattern.CASE_INSENSITIVE));
    assertThat(locator).not().hasAccessibleErrorMessage(Pattern.compile("This should not be considered.", Pattern.CASE_INSENSITIVE));
  }

  @Nested
  class ToHaveAccessibleErrorMessageShouldHandleAriaInvalidAttribute {
    private final String errorMessageText = "Error message";

    private Locator setupPage(Page page, String ariaInvalidValue) {
      String ariaInvalidAttr = ariaInvalidValue == null ? "" : "aria-invalid=\"" + ariaInvalidValue + "\"";
      page.setContent("\n" +
        "        <form>\n" +
        "          <input id=\"node\" role=\"textbox\" " + ariaInvalidAttr + " aria-errormessage=\"error-msg\" />\n" +
        "          <div id=\"error-msg\">" + errorMessageText + "</div>\n" +
        "        </form>\n" +
        "      ");
      return page.locator("#node");
    }

    @Nested
    class EvaluatedInFalse {
      @PlaywrightTest
      @DisplayName("EX-263 no aria-invalid attribute")
      void noAriaInvalidAttribute(Page page) {
        Locator locator = setupPage(page, null);
        assertThat(locator).not().hasAccessibleErrorMessage(errorMessageText);
      }

      @PlaywrightTest
      @DisplayName("EX-264 aria-invalid=\"false\"")
      void ariaInvalidFalse(Page page) {
        Locator locator = setupPage(page, "false");
        assertThat(locator).not().hasAccessibleErrorMessage(errorMessageText);
      }

      @PlaywrightTest
      @DisplayName("EX-265 aria-invalid=\"\" (empty string)")
      void ariaInvalidEmptyString(Page page) {
        Locator locator = setupPage(page, "");
        assertThat(locator).not().hasAccessibleErrorMessage(errorMessageText);
      }
    }

    @Nested
    class EvaluatedInTrue {
      @PlaywrightTest
      @DisplayName("EX-266 aria-invalid=\"true\"")
      void ariaInvalidTrue(Page page) {
        Locator locator = setupPage(page, "true");
        assertThat(locator).hasAccessibleErrorMessage(errorMessageText);
      }

      @PlaywrightTest
      @DisplayName("EX-267 aria-invalid=\"foo\" (unrecognized value)")
      void ariaInvalidFooUnrecognizedValue(Page page) {
        Locator locator = setupPage(page, "foo");
        assertThat(locator).hasAccessibleErrorMessage(errorMessageText);
      }
    }
  }

  private static final String errorMessageText = "Error message";

  @PlaywrightTest
  @DisplayName("EX-268 should show error message when validity is false and aria-invalid is true")
  void shouldShowErrorMessageWhenValidityIsFalseAndAriaInvalidIsTrue(Page page) {
    page.setContent("\n" +
      "    <form>\n" +
      "      <input id=\"node\" role=\"textbox\" type=\"number\" min=\"1\" max=\"100\" aria-invalid=\"true\" aria-errormessage=\"error-msg\" />\n" +
      "      <div id=\"error-msg\">" + errorMessageText + "</div>\n" +
      "    </form>\n" +
      "  ");
    Locator locator = page.locator("#node");
    locator.fill("101");
    assertThat(locator).hasAccessibleErrorMessage(errorMessageText);
  }

  @PlaywrightTest
  @DisplayName("EX-269 should show error message when validity is true and aria-invalid is true")
  void shouldShowErrorMessageWhenValidityIsTrueAndAriaInvalidIsTrue(Page page) {
    page.setContent("\n" +
      "    <form>\n" +
      "      <input id=\"node\" role=\"textbox\" type=\"number\" min=\"1\" max=\"100\" aria-invalid=\"true\" aria-errormessage=\"error-msg\" />\n" +
      "      <div id=\"error-msg\">" + errorMessageText + "</div>\n" +
      "    </form>\n" +
      "  ");
    Locator locator = page.locator("#node");
    locator.fill("99");
    assertThat(locator).hasAccessibleErrorMessage(errorMessageText);
  }

  @PlaywrightTest
  @DisplayName("EX-270 should show error message when validity is false and aria-invalid is false")
  void shouldShowErrorMessageWhenValidityIsFalseAndAriaInvalidIsFalse(Page page) {
    page.setContent("\n" +
      "    <form>\n" +
      "      <input id=\"node\" role=\"textbox\" type=\"number\" min=\"1\" max=\"100\" aria-invalid=\"false\" aria-errormessage=\"error-msg\" />\n" +
      "      <div id=\"error-msg\">" + errorMessageText + "</div>\n" +
      "    </form>\n" +
      "  ");
    Locator locator = page.locator("#node");
    locator.fill("101");
    assertThat(locator).hasAccessibleErrorMessage(errorMessageText);
  }

  @PlaywrightTest
  @DisplayName("EX-271 should not show error message when validity is true and aria-invalid is false")
  void shouldNotShowErrorMessageWhenValidityIsTrueAndAriaInvalidIsFalse(Page page) {
    page.setContent("\n" +
      "    <form>\n" +
      "      <input id=\"node\" role=\"textbox\" type=\"number\" min=\"1\" max=\"100\" aria-invalid=\"false\" aria-errormessage=\"error-msg\" />\n" +
      "      <div id=\"error-msg\">" + errorMessageText + "</div>\n" +
      "    </form>\n" +
      "  ");
    Locator locator = page.locator("#node");
    locator.fill("99");
    assertThat(locator).not().hasAccessibleErrorMessage(errorMessageText);
  }

  @PlaywrightTest
  @DisplayName("EX-272 should show error message for all roles")
  void shouldShowErrorMessageForAllRoles(Page page) {
    page.setContent("\n" +
      "    <label for=\"file\">File input</label>\n" +
      "    <input id=\"file\" aria-invalid=\"true\" aria-errormessage=\"file-error-id\" type=\"file\" />\n" +
      "    <p id=\"file-error-id\" role=\"alert\">File is incorrect</p>\n" +
      "  ");
    Locator locator = page.getByLabel("File input");
    assertThat(locator).hasAccessibleErrorMessage("File is incorrect");
  }

  // EX-273 toHaveRole: not ported, see upstream_not_ported.txt.
}
