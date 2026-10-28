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
import com.microsoft.playwright.PlaywrightException;
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
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/page/expect-to-have-text.spec.ts. */
public class TestExpectToHaveText {
  @Nested
  class ToHaveTextWithRegex {
    @PlaywrightTest
    @DisplayName("EX-274 pass")
    void pass(Page page) {
      page.setContent("<div id=node>Text   content</div>");
      Locator locator = page.locator("#node");
      assertThat(locator).hasText(Pattern.compile("Text"));

      // Should not normalize whitespace.
      assertThat(locator).hasText(Pattern.compile("Text   content"));
      // Should respect ignoreCase.
      assertThat(locator).hasText(Pattern.compile("text   content"), new LocatorAssertions.HasTextOptions().setIgnoreCase(true));
      // Should override regex flag with ignoreCase.
      assertThat(locator).not().hasText(Pattern.compile("text   content", Pattern.CASE_INSENSITIVE), new LocatorAssertions.HasTextOptions().setIgnoreCase(false));
    }

    @PlaywrightTest
    @DisplayName("EX-275 fail")
    @Fixme("java: the assertion error has another format: 'Locator expected to have text matching regex\nExpected: Text 2\nReceived: Text content\n\nCall log:\n- Assert \"hasText\" locator(\"#node\") with timeout 1000ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
    void fail(Page page) {
      page.setContent("<div id=node>Text content</div>");
      Locator locator = page.locator("#node");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).hasText(Pattern.compile("Text 2"), new LocatorAssertions.HasTextOptions().setTimeout(1000)));
      assertTrue(stripAnsi(error.getMessage()).contains("expect(locator).toHaveText(expected) failed\n" +
        "\n" +
        "Locator: locator('#node')\n" +
        "Expected pattern: /Text 2/\n" +
        "Received string:  \"Text content\"\n" +
        "Timeout: 1000ms\n" +
        "\n" +
        "Call log:\n"));
    }
  }

  @Nested
  class ToContainTextWithRegex {
    @PlaywrightTest
    @DisplayName("EX-276 pass")
    void pass(Page page) {
      page.setContent("<div id=node>Text   content</div>");
      Locator locator = page.locator("#node");
      assertThat(locator).containsText(Pattern.compile("ex"));

      // Should not normalize whitespace.
      assertThat(locator).containsText(Pattern.compile("ext   cont"));
    }

    @PlaywrightTest
    @DisplayName("EX-277 fail")
    @Fixme("java: the assertion error has another format: 'Locator expected to contain regex\nExpected: ex2\nReceived: Text content\n\nCall log:\n- Assert \"containsText\" locator(\"#node\") with timeout 1000ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
    void fail(Page page) {
      page.setContent("<div id=node>Text content</div>");
      Locator locator = page.locator("#node");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).containsText(Pattern.compile("ex2"), new LocatorAssertions.ContainsTextOptions().setTimeout(1000)));
      assertTrue(stripAnsi(error.getMessage()).contains("Expected pattern: /ex2/"));
      assertTrue(stripAnsi(error.getMessage()).contains("Received string:  \"Text content\""));
    }
  }

  @Nested
  class ToContainTextWithString {
    @PlaywrightTest
    @DisplayName("EX-278 pass")
    void pass(Page page) {
      page.setContent("<div id=node>Text   content</div>");
      Locator locator = page.locator("#node");
      assertThat(locator).containsText("content");
    }

    @PlaywrightTest
    @DisplayName("EX-279 fail")
    @Fixme("java: the assertion error has another format: 'Locator expected to contain text\nExpected: foo\nReceived: Text content\n\nCall log:\n- Assert \"containsText\" locator(\"#node\") with timeout 1000ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
    void fail(Page page) {
      page.setContent("<div id=node>Text content</div>");
      Locator locator = page.locator("#node");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).containsText("foo", new LocatorAssertions.ContainsTextOptions().setTimeout(1000)));
      assertTrue(stripAnsi(error.getMessage()).contains("expect(locator).toContainText(expected) failed\n" +
        "\n" +
        "Locator: locator('#node')\n" +
        "Expected substring: \"foo\"\n" +
        "Received string:    \"Text content\"\n" +
        "Timeout: 1000ms\n" +
        "\n" +
        "Call log:\n"));
    }
  }

  @Nested
  class ToHaveTextWithText {
    @PlaywrightTest
    @DisplayName("EX-280 pass")
    void pass(Page page) {
      page.setContent("<div id=node><span></span>Text \ncontent&nbsp;    </div>");
      Locator locator = page.locator("#node");
      // Should normalize whitespace.
      assertThat(locator).hasText("Text                        content");
      // Should normalize zero width whitespace.
      assertThat(locator).hasText("T​e​x​t content");
      // Should support ignoreCase.
      assertThat(locator).hasText("text CONTENT", new LocatorAssertions.HasTextOptions().setIgnoreCase(true));
      // Should support falsy ignoreCase.
      assertThat(locator).not().hasText("TEXT", new LocatorAssertions.HasTextOptions().setIgnoreCase(false));
      // Should normalize soft hyphens.
      assertThat(locator).hasText("T­e­xt content");
    }

    @PlaywrightTest
    @DisplayName("EX-281 pass contain")
    void passContain(Page page) {
      page.setContent("<div id=node>Text content</div>");
      Locator locator = page.locator("#node");
      assertThat(locator).containsText("Text");
      // Should normalize whitespace.
      assertThat(locator).containsText("   ext        cont\n  ");
      // Should support ignoreCase.
      assertThat(locator).containsText("EXT", new LocatorAssertions.ContainsTextOptions().setIgnoreCase(true));
      // Should support falsy ignoreCase.
      assertThat(locator).not().containsText("TEXT", new LocatorAssertions.ContainsTextOptions().setIgnoreCase(false));
    }

    @PlaywrightTest
    @DisplayName("EX-282 fail")
    @Fixme("java: the assertion error has another format: 'Locator expected to have text\nExpected: Text\nReceived: Text content\n\nCall log:\n- Assert \"hasText\" locator(\"#node\") with timeout 1000ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
    void fail(Page page) {
      page.setContent("<div id=node>Text content</div>");
      Locator locator = page.locator("#node");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).hasText("Text", new LocatorAssertions.HasTextOptions().setTimeout(1000)));
      assertTrue(stripAnsi(error.getMessage()).contains("Expected: \"Text\""));
      assertTrue(stripAnsi(error.getMessage()).contains("Received: \"Text content\""));
    }

    @PlaywrightTest
    @DisplayName("EX-283 pass eventually")
    void passEventually(Page page) throws Exception {
      page.setContent("<div id=node>Text content</div>");
      Locator locator = page.locator("#node");
      // Promise.all
      page.waitForTimeout(1000);
      locator.evaluate("element => element.textContent = 'Text 2 content'");
      // Java: the matcher stays satisfied once the text is changed, so it runs after the change.
      assertThat(locator).hasText(Pattern.compile("Text 2"));
    }

    @PlaywrightTest
    @DisplayName("EX-284 with userInnerText")
    void withUserInnerText(Page page) {
      page.setContent("<div id=node>Text <span hidden>garbage</span> content</div>");
      Locator locator = page.locator("#node");
      assertThat(locator).hasText("Text content", new LocatorAssertions.HasTextOptions().setUseInnerText(true));
    }

    @PlaywrightTest
    @DisplayName("EX-285 in shadow dom")
    void inShadowDom(Page page) {
      page.setContent("\n" +
        "      <div></div>\n" +
        "      <script>\n" +
        "        const div = document.querySelector('div');\n" +
        "        const span = document.createElement('span');\n" +
        "        span.textContent = 'some text';\n" +
        "        div.attachShadow({ mode: 'open' }).appendChild(span);\n" +
        "      </script>\n" +
        "    ");
      assertThat(page.locator("span")).hasText("some text");
      assertThat(page.locator("span")).containsText("text");
      assertThat(page.locator("div")).hasText("some text");
      assertThat(page.locator("div")).containsText("text");
      assertThat(page.locator("span")).hasText("some text", new LocatorAssertions.HasTextOptions().setUseInnerText(true));
      assertThat(page.locator("span")).containsText("text", new LocatorAssertions.ContainsTextOptions().setUseInnerText(true));
      // Playwright intentionally does not perform innerText piercing on shadow dom.
      assertThat(page.locator("div")).not().hasText("some text", new LocatorAssertions.HasTextOptions().setUseInnerText(true));
      assertThat(page.locator("div")).not().containsText("text", new LocatorAssertions.ContainsTextOptions().setUseInnerText(true));
    }

    @PlaywrightTest
    @DisplayName("EX-286 fail with impossible timeout")
    @Fixme("java: the assertion error has another format: 'Locator expected to have text\nExpected: Text\nReceived: Text content\n\nCall log:\n- Assert \"hasText\" locator(\"#node\") with timeout 1ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
    void failWithImpossibleTimeout(Page page) {
      page.setContent("<div id=node>Text content</div>");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(page.locator("#node")).hasText("Text", new LocatorAssertions.HasTextOptions().setTimeout(1)));
      assertTrue(stripAnsi(error.getMessage()).contains("Expected: \"Text\""));
      assertTrue(stripAnsi(error.getMessage()).contains("Received: \"Text content\""));
    }

    @PlaywrightTest
    @DisplayName("EX-287 do not show \"element(s) not found\" when the real failure is a string mismatch")
    @Fixme("java: the assertion error has another format: 'Locator expected to have text\nExpected: Something\nReceived: Final value\n\nCall log:\n- Assert \"hasText\" locator(\"#field\") with timeout 3000ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
    void doNotShowElementSNotFoundWhenTheRealFailureIsAStringMismatch(Page page) {
      assumeFalse("frozen".equals(System.getenv("PW_CLOCK")), "The element is attached after 1 second.");
      page.setContent("\n" +
        "      <div>Initial</div>\n" +
        "      <script>\n" +
        "        const field = document.querySelector('div');\n" +
        "        setTimeout(() => {\n" +
        "          field.id = 'field';\n" +
        "          field.textContent = 'Final value';\n" +
        "        }, 1000);\n" +
        "      </script>\n" +
        "    ");

      Locator cell = page.locator("#field");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(cell).hasText("Something", new LocatorAssertions.HasTextOptions().setTimeout(3000)));
      assertTrue(stripAnsi(error.getMessage()).contains("Expected: \"Something\""));
      assertTrue(stripAnsi(error.getMessage()).contains("Received: \"Final value\""));
    }
  }

  @Nested
  class NotToHaveText {
    @PlaywrightTest
    @DisplayName("EX-288 pass")
    void pass(Page page) {
      page.setContent("<div id=node>Text content</div>");
      Locator locator = page.locator("#node");
      assertThat(locator).not().hasText("Text2");
      // Should be case-sensitive by default.
      assertThat(locator).not().hasText("TEXT");
    }

    @PlaywrightTest
    @DisplayName("EX-289 fail")
    @Fixme("java: the assertion error has another format: 'Locator expected not to have text\nExpected: Text content\nReceived: Text content\n\nCall log:\n- Assert \"hasText\" locator(\"#node\") with timeout 1000ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
    void fail(Page page) {
      page.setContent("<div id=node>Text content</div>");
      Locator locator = page.locator("#node");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).not().hasText("Text content", new LocatorAssertions.HasTextOptions().setTimeout(1000)));
      assertTrue(stripAnsi(error.getMessage()).contains("Expected: not \"Text content\""));
      assertTrue(stripAnsi(error.getMessage()).contains("Received: \"Text content"));
    }

    @PlaywrightTest
    @DisplayName("EX-290 should work when selector does not match")
    @Fixme("java: the assertion error has another format: 'Locator expected not to have text\nError: element(s) not found\nExpected: hello\nReceived: - text: hello\n\nCall log:\n- Assert \"hasText\" locator(\"span\") with timeout 1000ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
    void shouldWorkWhenSelectorDoesNotMatch(Page page) {
      page.setContent("<div>hello</div>");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(page.locator("span")).not().hasText("hello", new LocatorAssertions.HasTextOptions().setTimeout(1000)));
      assertTrue(stripAnsi(error.getMessage()).contains("Expected: not \"hello\""));
      assertTrue(stripAnsi(error.getMessage()).contains("Error: element(s) not found"));
      assertTrue(stripAnsi(error.getMessage()).contains("waiting for locator('span')"));
    }
  }

  @Nested
  class ToHaveTextWithArray {
    // EX-291 pass: not ported, see upstream_not_ported.txt.

    @PlaywrightTest
    @DisplayName("EX-292 pass lazy")
    void passLazy(Page page) {
      page.setContent("<div id=div></div>");
      Locator locator = page.locator("p");
      page.waitForTimeout(500);
      try {
        page.evaluate("() => {\n" +
          "        document.querySelector('div').innerHTML = '<p>Text 1</p><p>Text 2</p>';\n" +
          "      }");
      } catch (PlaywrightException e) {
        // .catch(() => {})
      }
      // Java: the matcher stays satisfied once the change is made, so it runs after the change.
      assertThat(locator).hasText(new String[]{"Text 1", "Text 2"});
    }

    @PlaywrightTest
    @DisplayName("EX-293 pass empty")
    void passEmpty(Page page) {
      page.setContent("<div></div>");
      Locator locator = page.locator("p");
      assertThat(locator).hasText(new String[]{});
    }

    @PlaywrightTest
    @DisplayName("EX-294 pass not empty")
    void passNotEmpty(Page page) {
      page.setContent("<div><p>Test</p></div>");
      Locator locator = page.locator("p");
      assertThat(locator).not().hasText(new String[]{});
    }

    @PlaywrightTest
    @DisplayName("EX-295 pass on empty")
    void passOnEmpty(Page page) {
      page.setContent("<div></div>");
      Locator locator = page.locator("p");
      assertThat(locator).not().hasText(new String[]{"Test"});
    }

    @PlaywrightTest
    @DisplayName("EX-296 fail on not+empty")
    @Fixme("java: the assertion error has another format: 'Locator expected not to have text\nExpected: []\nReceived: []\n\nCall log:\n- Assert \"hasText\" locator(\"p\") with timeout 1000ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
    void failOnNotEmpty(Page page) {
      page.setContent("<div></div>");
      Locator locator = page.locator("p");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).not().hasText(new String[]{}, new LocatorAssertions.HasTextOptions().setTimeout(1000)));
      assertTrue(stripAnsi(error.getMessage()).contains("expect(locator).not.toHaveText(expected)"));
      assertTrue(stripAnsi(error.getMessage()).contains("Timeout:  1000ms"));
      assertTrue(stripAnsi(error.getMessage()).contains("- Expect \"not toHaveText\" locator('p') with timeout 1000ms"));
    }

    @PlaywrightTest
    @DisplayName("EX-297 pass eventually empty")
    void passEventuallyEmpty(Page page) {
      page.setContent("<div id=div><p>Text</p></div>");
      Locator locator = page.locator("p");
      // Java: the matcher already holds before the page changes and no longer holds after it, so it runs first.
      assertThat(locator).not().hasText(new String[]{});
      page.waitForTimeout(500);
      try {
        page.evaluate("() => document.querySelector('div').innerHTML = ''");
      } catch (PlaywrightException e) {
        // .catch(() => {})
      }
    }

    // EX-298 fail: not ported, see upstream_not_ported.txt.

    @PlaywrightTest
    @DisplayName("EX-299 fail on repeating array matchers")
    void failOnRepeatingArrayMatchers(Page page) {
      page.setContent("<div>KekFoo</div>");
      Locator locator = page.locator("div");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).containsText(new String[]{"KekFoo", "KekFoo", "KekFoo"}, new LocatorAssertions.ContainsTextOptions().setTimeout(1000)));
      assertTrue(error.getMessage().contains("locator resolved to 1 element"));
    }
  }

  @Nested
  class ToContainTextWithArray {
    // EX-300 pass: not ported, see upstream_not_ported.txt.

    @PlaywrightTest
    @DisplayName("EX-301 fail")
    @Fixme("java: the assertion error has another format: 'Locator expected to contain text\nExpected: [Text 2]\nReceived: [Text 1, Text 3]\n\nCall log:\n- Assert \"containsText\" locator(\"div\") with timeout 1000ms ...' (no 'expect(locator)... failed' header, no 'Locator:' line, unquoted values, double quoted locators)")
    void fail(Page page) {
      page.setContent("<div>Text 1</div><div>Text 3</div>");
      Locator locator = page.locator("div");
      AssertionFailedError error = assertThrows(AssertionFailedError.class,
        () -> assertThat(locator).containsText(new String[]{"Text 2"}, new LocatorAssertions.ContainsTextOptions().setTimeout(1000)));
      assertTrue(stripAnsi(error.getMessage()).contains("-   \"Text 2\""));
    }
  }
}
