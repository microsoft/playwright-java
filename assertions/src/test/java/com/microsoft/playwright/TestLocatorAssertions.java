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

import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.assertions.PageAssertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class TestLocatorAssertions extends TestBase {
  @Test
  void shouldSupportContainsTextWRegexPass() {
    page.setContent("<div id=node>Text   content</div>");
    Locator locator = page.locator("#node");
    assertThat(locator).containsText(Pattern.compile("ex"));
    // Should not normalize whitespace.
    assertThat(locator).containsText(Pattern.compile("ext   cont"));
  }

  @Test
  void shouldSupportContainsTextWRegexFail() {
    page.setContent("<div id=node>Text   content</div>");
    Locator locator = page.locator("#node");
    try {
      assertThat(locator).containsText(Pattern.compile("ex2"), new LocatorAssertions.ContainsTextOptions().setTimeout(100));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertEquals("ex2", e.getExpected().getStringRepresentation());
      assertEquals("Text   content", e.getActual().getValue());
      assertTrue(e.getMessage().contains("Locator expected to contain text"), e.getMessage());
    }
  }

  @Test
  void shouldSupportHasTextWRegexPass() {
    page.setContent("<div id=node>Text   content</div>");
    Locator locator = page.locator("#node");
    assertThat(locator).hasText(Pattern.compile("Text"));
    // Should not normalize whitespace.
    assertThat(locator).hasText(Pattern.compile("Text   content"));
  }

  @Test
  void shouldSupportHasTextWRegexFail() {
    page.setContent("<div id=node>Text   content</div>");
    Locator locator = page.locator("#node");
    try {
      assertThat(locator).hasText(Pattern.compile("Text 2"), new LocatorAssertions.HasTextOptions().setTimeout(100));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertEquals("Text 2", e.getExpected().getStringRepresentation());
      assertEquals("Text   content", e.getActual().getValue());
      assertTrue(e.getMessage().contains("Locator expected to have text"), e.getMessage());
    }
  }

  @Test
  void shouldSupportHasTextWTextPass() {
    page.setContent("<div id=node><span></span>Text \ncontent&nbsp;    </div>");
    Locator locator = page.locator("#node");
    // Should normalize whitespace.
    assertThat(locator).hasText("Text                        content");
  }

  @Test
  void shouldSupportHasTextWTextFail() {
    page.setContent("<div id=node>Text content</div>");
    Locator locator = page.locator("#node");
    // Should normalize whitespace.
    try {
      assertThat(locator).hasText("Text", new LocatorAssertions.HasTextOptions().setTimeout(100));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertEquals("Text", e.getExpected().getStringRepresentation());
      assertEquals("Text content", e.getActual().getValue());
      assertTrue(e.getMessage().contains("Locator expected to have text"), e.getMessage());
    }
  }

  @Test
  void shouldSupportToHaveTextWTextArrayPass() {
    page.setContent("<div>Text    \n1</div><div>Text   2a</div>");
    Locator locator = page.locator("div");
    // Should normalize whitespace.
    assertThat(locator).hasText(new String[] {"Text  1", "Text   2a"});
  }

  @Test
  void shouldSupportToHaveTextWTextArrayPassEmpty() {
    page.setContent("<div></div>");
    Locator locator = page.locator("p");
    // Should normalize whitespace.
    assertThat(locator).hasText(new String[] {});
  }

  @Test
  void shouldSupportToHaveTextWTextArrayPassNotEmpty() {
    page.setContent("<div><p>Test</p></div>");
    Locator locator = page.locator("div");
    // Should normalize whitespace.
    assertThat(locator).not().hasText(new String[] {});
  }

  @Test
  void shouldSupportToHaveTextWTextArrayPassOnEmpty() {
    page.setContent("<div></div>");
    Locator locator = page.locator("p");
    // Should normalize whitespace.
    assertThat(locator).not().hasText(new String[] {"Test"});
  }

  @Test
  void shouldSupportToHaveTextWTextArrayFailOnNotEmpty() {
    page.setContent("<div></div>");
    Locator locator = page.locator("p");
    // Should normalize whitespace.
    try {
      assertThat(locator).not().hasText(new String[] {}, new LocatorAssertions.HasTextOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertEquals("[]", e.getExpected().getStringRepresentation());
      assertEquals("null", e.getActual().getStringRepresentation());
      assertTrue(e.getMessage().contains("Locator expected to have text"), e.getMessage());
    }
  }

  @Test
  void shouldSupportToHaveTextWTextArrayPassLazyPass() {
    page.setContent("<div id=div></div>");
    Locator locator = page.locator("p");
    page.evaluate("setTimeout(() => {\n" +
      "  div.innerHTML = \"<p>Text 1</p><p>Text 2</p>\";\n" +
      "}, 500);");
    // Should normalize whitespace.
    assertThat(locator).hasText(new String[] {"Text  1", "Text   2"}, new LocatorAssertions.HasTextOptions().setTimeout(1000));
  }

  @Test
  void shouldSupportToHaveTextWTextArrayFail() {
    page.setContent("<div>Text 1</div><div>Text 3</div>");
    Locator locator = page.locator("div");
    page.evaluate("setTimeout(() => {\n" +
      "  div.innerHTML = \"<p>Text 1</p><p>Text 2</p>\";\n" +
      "}, 500);");
    try {
      // Should normalize whitespace.
      assertThat(locator).hasText(new String[] {"Text 1", "Text 3", "Extra"}, new LocatorAssertions.HasTextOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertEquals("[Text 1, Text 3, Extra]", e.getExpected().getStringRepresentation());
      assertEquals("[Text 1, Text 3]", e.getActual().getStringRepresentation());
      assertTrue(e.getMessage().contains("Locator expected to have text"), e.getMessage());
    }
  }

  @Test
  void shouldSupportToHaveTextWRegExArrayPass() {
    page.setContent("<div>Text    \n1</div><div>Text   2a</div>");
    Locator locator = page.locator("div");
    // Should normalize whitespace.
    assertThat(locator).hasText(new Pattern[] {Pattern.compile( "Text    \n1"), Pattern.compile("Text   \\d+a")});
  }

  @Test
  void shouldSupportToHaveTextWRegExArrayFail() {
    page.setContent("<div>Text 1</div><div>Text 3</div>");
    Locator locator = page.locator("div");
    try {
      // Should normalize whitespace.
      assertThat(locator).hasText(new Pattern[] {Pattern.compile( "Text 1"), Pattern.compile("Text   \\d"), Pattern.compile("Extra")}, new LocatorAssertions.HasTextOptions().setTimeout(100));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertEquals("[Text 1, Text   \\d, Extra]", e.getExpected().getStringRepresentation());
      assertEquals("[Text 1, Text 3]", e.getActual().getStringRepresentation());
      assertTrue(e.getMessage().contains("Locator expected to have text"), e.getMessage());
    }
  }

  @Test
  void shouldSupportToHaveAttributeTextPass() {
    page.setContent("<div id=node>Text content</div>");
    Locator locator = page.locator("#node");
    assertThat(locator).hasAttribute("id", "node");
  }

  @Test
  void shouldSupportToHaveAttributeTextFail() {
    page.setContent("<div id=node>Text content</div>");
    Locator locator = page.locator("#node");
    try {
      assertThat(locator).hasAttribute("id", "foo", new LocatorAssertions.HasAttributeOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertEquals("foo", e.getExpected().getStringRepresentation());
      assertEquals("node", e.getActual().getStringRepresentation());
      assertTrue(e.getMessage().contains("Locator expected to have attribute"), e.getMessage());
    }
  }
}
