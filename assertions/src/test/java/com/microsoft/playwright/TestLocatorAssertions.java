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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.regex.Pattern;

import static com.microsoft.playwright.Utils.mapOf;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class TestLocatorAssertions extends TestBase {
  @Test
  void containsTextWRegexPass() {
    page.setContent("<div id=node>Text   content</div>");
    Locator locator = page.locator("#node");
    assertThat(locator).containsText(Pattern.compile("ex"));
    // Should not normalize whitespace.
    assertThat(locator).containsText(Pattern.compile("ext   cont"));
  }

  @Test
  void containsTextWRegexFail() {
    page.setContent("<div id=node>Text   content</div>");
    Locator locator = page.locator("#node");
    try {
      assertThat(locator).containsText(Pattern.compile("ex2"), new LocatorAssertions.ContainsTextOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertEquals("ex2", e.getExpected().getStringRepresentation());
      assertEquals("Text   content", e.getActual().getValue());
      assertTrue(e.getMessage().contains("Locator expected to contain text"), e.getMessage());
    }
  }

  @Test
  void hasTextWRegexPass() {
    page.setContent("<div id=node>Text   content</div>");
    Locator locator = page.locator("#node");
    assertThat(locator).hasText(Pattern.compile("Text"));
    // Should not normalize whitespace.
    assertThat(locator).hasText(Pattern.compile("Text   content"));
  }

  @Test
  void hasTextWRegexFail() {
    page.setContent("<div id=node>Text   content</div>");
    Locator locator = page.locator("#node");
    try {
      assertThat(locator).hasText(Pattern.compile("Text 2"), new LocatorAssertions.HasTextOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertEquals("Text 2", e.getExpected().getStringRepresentation());
      assertEquals("Text   content", e.getActual().getValue());
      assertTrue(e.getMessage().contains("Locator expected to have text"), e.getMessage());
    }
  }

  @Test
  void hasTextWTextPass() {
    page.setContent("<div id=node><span></span>Text \ncontent&nbsp;    </div>");
    Locator locator = page.locator("#node");
    // Should normalize whitespace.
    assertThat(locator).hasText("Text                        content");
  }

  @Test
  void hasTextWTextFail() {
    page.setContent("<div id=node>Text content</div>");
    Locator locator = page.locator("#node");
    // Should normalize whitespace.
    try {
      assertThat(locator).hasText("Text", new LocatorAssertions.HasTextOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertEquals("Text", e.getExpected().getStringRepresentation());
      assertEquals("Text content", e.getActual().getValue());
      assertTrue(e.getMessage().contains("Locator expected to have text"), e.getMessage());
    }
  }

  @Test
  void hasTextWTextArrayPass() {
    page.setContent("<div>Text    \n1</div><div>Text   2a</div>");
    Locator locator = page.locator("div");
    // Should normalize whitespace.
    assertThat(locator).hasText(new String[] {"Text  1", "Text   2a"});
  }

  @Test
  void hasTextWTextArrayPassEmpty() {
    page.setContent("<div></div>");
    Locator locator = page.locator("p");
    // Should normalize whitespace.
    assertThat(locator).hasText(new String[] {});
  }

  @Test
  void hasTextWTextArrayPassNotEmpty() {
    page.setContent("<div><p>Test</p></div>");
    Locator locator = page.locator("div");
    // Should normalize whitespace.
    assertThat(locator).not().hasText(new String[] {});
  }

  @Test
  void hasTextWTextArrayPassOnEmpty() {
    page.setContent("<div></div>");
    Locator locator = page.locator("p");
    // Should normalize whitespace.
    assertThat(locator).not().hasText(new String[] {"Test"});
  }

  @Test
  void hasTextWTextArrayFailOnNotEmpty() {
    page.setContent("<div></div>");
    Locator locator = page.locator("p");
    // Should normalize whitespace.
    try {
      assertThat(locator).not().hasText(new String[] {}, new LocatorAssertions.HasTextOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertEquals("[]", e.getExpected().getStringRepresentation());
      assertEquals("null", e.getActual().getStringRepresentation());
      assertTrue(e.getMessage().contains("Locator expected not to have text"), e.getMessage());
    }
  }

  @Test
  void hasTextWTextArrayPassLazyPass() {
    page.setContent("<div id=div></div>");
    Locator locator = page.locator("p");
    page.evaluate("setTimeout(() => {\n" +
      "  div.innerHTML = \"<p>Text 1</p><p>Text 2</p>\";\n" +
      "}, 100);");
    // Should normalize whitespace.
    assertThat(locator).hasText(new String[] {"Text  1", "Text   2"}, new LocatorAssertions.HasTextOptions().setTimeout(1000));
  }

  @Test
  void hasTextWTextArrayFail() {
    page.setContent("<div>Text 1</div><div>Text 3</div>");
    Locator locator = page.locator("div");
    page.evaluate("setTimeout(() => {\n" +
      "  div.innerHTML = \"<p>Text 1</p><p>Text 2</p>\";\n" +
      "}, 100);");
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
  void hasTextWRegExArrayPass() {
    page.setContent("<div>Text    \n1</div><div>Text   2a</div>");
    Locator locator = page.locator("div");
    // Should normalize whitespace.
    assertThat(locator).hasText(new Pattern[] {Pattern.compile( "Text    \n1"), Pattern.compile("Text   \\d+a")});
  }

  @Test
  void hasTextWRegExArrayFail() {
    page.setContent("<div>Text 1</div><div>Text 3</div>");
    Locator locator = page.locator("div");
    try {
      // Should normalize whitespace.
      assertThat(locator).hasText(new Pattern[] {Pattern.compile( "Text 1"), Pattern.compile("Text   \\d"), Pattern.compile("Extra")}, new LocatorAssertions.HasTextOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertEquals("[Text 1, Text   \\d, Extra]", e.getExpected().getStringRepresentation());
      assertEquals("[Text 1, Text 3]", e.getActual().getStringRepresentation());
      assertTrue(e.getMessage().contains("Locator expected to have text"), e.getMessage());
    }
  }

  @Test
  void hasAttributeTextPass() {
    page.setContent("<div id=node>Text content</div>");
    Locator locator = page.locator("#node");
    assertThat(locator).hasAttribute("id", "node");
  }

  @Test
  void hasAttributeTextFail() {
    page.setContent("<div id=node>Text content</div>");
    Locator locator = page.locator("#node");
    try {
      assertThat(locator).hasAttribute("id", "foo", new LocatorAssertions.HasAttributeOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertEquals("foo", e.getExpected().getStringRepresentation());
      assertEquals("node", e.getActual().getStringRepresentation());
      assertTrue(e.getMessage().contains("Locator expected to have attribute 'id'"), e.getMessage());
    }
  }

  @Test
  void hasAttributeRegExpPass() {
    page.setContent("<div id=node>Text content</div>");
    Locator locator = page.locator("#node");
    assertThat(locator).hasAttribute("id", Pattern.compile("n..e"));
  }

  @Test
  void hasAttributeRegExpFail() {
    page.setContent("<div id=node>Text content</div>");
    Locator locator = page.locator("#node");
    try {
      assertThat(locator).hasAttribute("id", ".Nod..", new LocatorAssertions.HasAttributeOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertEquals(".Nod..", e.getExpected().getStringRepresentation());
      assertEquals("node", e.getActual().getStringRepresentation());
      assertTrue(e.getMessage().contains("Locator expected to have attribute"), e.getMessage());
    }
  }

  @Test
  void hasClassTextPass() {
    page.setContent("<div class=\"foo bar baz\"></div>");
    Locator locator = page.locator("div");
    assertThat(locator).hasClass("foo bar baz");
  }

  @Test
  void hasClassTextFail() {
    page.setContent("<div class=\"bar baz\"></div>");
    Locator locator = page.locator("div");
    try {
      assertThat(locator).hasClass("foo bar baz", new LocatorAssertions.HasClassOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertEquals("foo bar baz", e.getExpected().getStringRepresentation());
      assertEquals("bar baz", e.getActual().getStringRepresentation());
      assertTrue(e.getMessage().contains("Locator expected to have class"), e.getMessage());
    }
  }

  @Test
  void hasClassRegExpPass() {
    page.setContent("<div class=\"foo bar baz\"></div>");
    Locator locator = page.locator("div");
    assertThat(locator).hasClass(Pattern.compile("foo.* baz"));
  }

  @Test
  void hasClassRegExpFail() {
    page.setContent("<div class=\"bar baz\"></div>");
    Locator locator = page.locator("div");
    try {
      assertThat(locator).hasClass("foo Z.*", new LocatorAssertions.HasClassOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertEquals("foo Z.*", e.getExpected().getStringRepresentation());
      assertEquals("bar baz", e.getActual().getStringRepresentation());
      assertTrue(e.getMessage().contains("Locator expected to have class"), e.getMessage());
    }
  }

  @Test
  void hasClassTextArrayPass() {
    page.setContent("<div class=\"foo\"></div><div class=\"bar\"></div><div class=\"baz\"></div>");
    Locator locator = page.locator("div");
    assertThat(locator).hasClass(new String[] {"foo", "bar", "baz"});
  }

  @Test
  void hasClassTextArrayFail() {
    page.setContent("<div class=\"foo\"></div><div class=\"bar\"></div><div class=\"baz\"></div>");
    Locator locator = page.locator("div");
    try {
      assertThat(locator).hasClass(new String[] {"foo", "bar", "missing"}, new LocatorAssertions.HasClassOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertEquals("[foo, bar, missing]", e.getExpected().getStringRepresentation());
      assertEquals("[foo, bar, baz]", e.getActual().getStringRepresentation());
      assertTrue(e.getMessage().contains("Locator expected to have class"), e.getMessage());
    }
  }

  @Test
  void hasClassRegExpArrayPass() {
    page.setContent("<div class=\"foo\"></div><div class=\"bar\"></div><div class=\"baz\"></div>");
    Locator locator = page.locator("div");
    assertThat(locator).hasClass(new Pattern[] {Pattern.compile("fo.*"), Pattern.compile(".ar"), Pattern.compile("baz")});
  }

  @Test
  void hasClassRegExpArrayFail() {
    page.setContent("<div class=\"foo\"></div><div class=\"bar\"></div><div class=\"baz\"></div>");
    Locator locator = page.locator("div");
    try {
      assertThat(locator).hasClass(new Pattern[] {Pattern.compile("fo.*"), Pattern.compile(".ar"), Pattern.compile("baz"), Pattern.compile("extra")}, new LocatorAssertions.HasClassOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertEquals("[fo.*, .ar, baz, extra]", e.getExpected().getStringRepresentation());
      assertEquals("[foo, bar, baz]", e.getActual().getStringRepresentation());
      assertTrue(e.getMessage().contains("Locator expected to have class"), e.getMessage());
    }
  }

  @Test
  void hasCountPass() {
    page.setContent("<select><option>One</option><option>Two</option></select>");
    Locator locator = page.locator("option");
    assertThat(locator).hasCount(2);
  }

  @Test
  void hasCountFail() {
    page.setContent("<select><option>One</option><option>Two</option></select>");
    Locator locator = page.locator("option");
    try {
      assertThat(locator).hasCount(1, new LocatorAssertions.HasCountOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertEquals("1", e.getExpected().getStringRepresentation());
      assertEquals("2", e.getActual().getStringRepresentation());
      assertTrue(e.getMessage().contains("Locator expected to have count"), e.getMessage());
    }
  }

  @Test
  void hasCountPassZero() {
    page.setContent("<div></div>");
    Locator locator = page.locator("span");
    assertThat(locator).hasCount(0);
    assertThat(locator).not().hasCount(1);
  }

  @Test
  void hasCSSPass() {
    page.setContent("<div id=node style='color: rgb(255, 0, 0)'>Text content</div>");
    Locator locator = page.locator("#node");
    assertThat(locator).hasCSS("color", "rgb(255, 0, 0)");
  }

  @Test
  void hasCSSFail() {
    page.setContent("<div id=node style='color: rgb(255, 0, 0)'>Text content</div>");
    Locator locator = page.locator("#node");
    try {
      assertThat(locator).hasCSS("color", "red", new LocatorAssertions.HasCSSOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertEquals("red", e.getExpected().getStringRepresentation());
      assertEquals("rgb(255, 0, 0)", e.getActual().getStringRepresentation());
      assertTrue(e.getMessage().contains("Locator expected to have CSS property 'color'"), e.getMessage());
    }
  }

  @Test
  void hasIdPass() {
    page.setContent("<div id=node>Text content</div>");
    Locator locator = page.locator("#node");
    assertThat(locator).hasId("node");
  }

  @Test
  void hasIdFail() {
    page.setContent("<div id=node>Text content</div>");
    Locator locator = page.locator("#node");
    try {
      assertThat(locator).hasId("foo", new LocatorAssertions.HasIdOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertEquals("foo", e.getExpected().getStringRepresentation());
      assertEquals("node", e.getActual().getStringRepresentation());
      assertTrue(e.getMessage().contains("Locator expected to have ID"), e.getMessage());
    }
  }

  @Test
  void hasJSPropertyPass() {
    page.setContent("<div></div>");
    page.evalOnSelector("div", "e => e.foo = { a: 1, b: 'string' }");
    Locator locator = page.locator("div");
    assertThat(locator).hasJSProperty("foo", mapOf("a", 1, "b", "string"));
  }

  @Test
  void hasJSPropertyNumberFail() {
    page.setContent("<div id=node>Text content</div>");
    Locator locator = page.locator("#node");
    page.evalOnSelector("div", "e => e.foo = { a: 1, b: 'string' }");
    try {
      assertThat(locator).hasJSProperty("foo", 1, new LocatorAssertions.HasJSPropertyOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertEquals("1", e.getExpected().getStringRepresentation());
      assertEquals("{a=1, b=string}", e.getActual().getStringRepresentation());
      assertTrue(e.getMessage().contains("Locator expected to have JavaScript property 'foo'"), e.getMessage());
    }
  }

  @Test
  void hasJSPropertyObjectFail() {
    page.setContent("<div id=node>Text content</div>");
    Locator locator = page.locator("#node");
    page.evalOnSelector("div", "e => e.foo = { a: 1, b: 'string' }");
    try {
      assertThat(locator).hasJSProperty("foo", mapOf("a", 2), new LocatorAssertions.HasJSPropertyOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertEquals("{a=2}", e.getExpected().getStringRepresentation());
      assertEquals("{a=1, b=string}", e.getActual().getStringRepresentation());
      assertTrue(e.getMessage().contains("Locator expected to have JavaScript property 'foo'"), e.getMessage());
    }
  }

  @Test
  @Disabled("https://github.com/microsoft/playwright/pull/9865")
  void hasJSPropertyStringFail() {
    page.setContent("<div id=node>Text content</div>");
    Locator locator = page.locator("#node");
    try {
      assertThat(locator).hasJSProperty("id", "foo", new LocatorAssertions.HasJSPropertyOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertEquals("foo", e.getExpected().getStringRepresentation());
      assertEquals("node", e.getActual().getStringRepresentation());
      assertTrue(e.getMessage().contains("Locator expected to have JavaScript property 'id'"), e.getMessage());
      throw e;
    }
  }
  @Test
  void hasValueTextPass() {
    page.setContent("<input id=node></input>");
    Locator locator = page.locator("#node");
    locator.fill("Text content");
    assertThat(locator).hasValue("Text content");
  }

  @Test
  void hasValueTextFail() {
    page.setContent("<input id=node></input>");
    Locator locator = page.locator("#node");
    locator.fill("Text content");
    try {
      assertThat(locator).hasValue("Text2", new LocatorAssertions.HasValueOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertEquals("Text2", e.getExpected().getStringRepresentation());
      assertEquals("Text content", e.getActual().getStringRepresentation());
      assertTrue(e.getMessage().contains("Locator expected to have value"), e.getMessage());
    }
  }

  @Test
  void hasValueRegExpPass() {
    page.setContent("<input id=node></input>");
    Locator locator = page.locator("#node");
    locator.fill("Text content");
    assertThat(locator).hasValue(Pattern.compile("Text"));
  }

  @Test
  void hasValueRegExpPassWithNot() {
    page.setContent("<input id=node></input>");
    Locator locator = page.locator("#node");
    locator.fill("Text content");
    assertThat(locator).not().hasValue(Pattern.compile("Text2"));
  }

  @Test
  void hasValueRegExpFail() {
    page.setContent("<input id=node></input>");
    Locator locator = page.locator("#node");
    locator.fill("Text content");
    try {
      assertThat(locator).hasValue(Pattern.compile("Text2"), new LocatorAssertions.HasValueOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertEquals("Text2", e.getExpected().getStringRepresentation());
      assertEquals("Text content", e.getActual().getStringRepresentation());
      assertTrue(e.getMessage().contains("Locator expected to have value"), e.getMessage());
    }
  }

  @Test
  void isCheckedPass() {
    page.setContent("<input type=checkbox checked></input>");
    Locator locator = page.locator("input");
    assertThat(locator).isChecked();
  }

  @Test
  void isCheckedFail() {
    page.setContent("<input type=checkbox></input>");
    Locator locator = page.locator("input");
    try {
      assertThat(locator).isChecked(new LocatorAssertions.IsCheckedOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertNull(e.getExpected());
      assertNull(e.getActual());
      assertTrue(e.getMessage().contains("Locator expected to be checked"), e.getMessage());
    }
  }

  @Test
  void notIsCheckedFail() {
    page.setContent("<input type=checkbox checked></input>");
    Locator locator = page.locator("input");
    try {
      assertThat(locator).not().isChecked(new LocatorAssertions.IsCheckedOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertNull(e.getExpected());
      assertNull(e.getActual());
      assertTrue(e.getMessage().contains("Locator expected not to be checked"), e.getMessage());
    }
  }

  @Test
  void isDisabledPass() {
    page.setContent("<button disabled>Text</button>");
    Locator locator = page.locator("button");
    assertThat(locator).isDisabled();
  }

  @Test
  void isDisabledFail() {
    page.setContent("<button>Text</button>");
    Locator locator = page.locator("button");
    try {
      assertThat(locator).isDisabled(new LocatorAssertions.IsDisabledOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertNull(e.getExpected());
      assertNull(e.getActual());
      assertTrue(e.getMessage().contains("Locator expected to be disabled"), e.getMessage());
    }
  }

  @Test
  void notIsDisabledFail() {
    page.setContent("<button disabled>Text</button>");
    Locator locator = page.locator("button");
    try {
      assertThat(locator).not().isDisabled(new LocatorAssertions.IsDisabledOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertNull(e.getExpected());
      assertNull(e.getActual());
      assertTrue(e.getMessage().contains("Locator expected not to be disabled"), e.getMessage());
    }
  }

  @Test
  void isEditablePass() {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    assertThat(locator).isEditable();
  }

  @Test
  void isEditableFail() {
    page.setContent("<input disabled></input>");
    Locator locator = page.locator("input");
    try {
      assertThat(locator).isEditable(new LocatorAssertions.IsEditableOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertNull(e.getExpected());
      assertNull(e.getActual());
      assertTrue(e.getMessage().contains("Locator expected to be editable"), e.getMessage());
    }
  }

  @Test
  void notIsEditableFail() {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    try {
      assertThat(locator).not().isEditable(new LocatorAssertions.IsEditableOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertNull(e.getExpected());
      assertNull(e.getActual());
      assertTrue(e.getMessage().contains("Locator expected not to be editable"), e.getMessage());
    }
  }


  @Test
  void isEmptyPass() {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    assertThat(locator).isEmpty();
  }

  @Test
  void isEmptyFail() {
    page.setContent("<input value=text></input>");
    Locator locator = page.locator("input");
    try {
      assertThat(locator).isEmpty(new LocatorAssertions.IsEmptyOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertNull(e.getExpected());
      assertNull(e.getActual());
      assertTrue(e.getMessage().contains("Locator expected to be empty"), e.getMessage());
    }
  }

  @Test
  void notIsEmptyFail() {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    try {
      assertThat(locator).not().isEmpty(new LocatorAssertions.IsEmptyOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertNull(e.getExpected());
      assertNull(e.getActual());
      assertTrue(e.getMessage().contains("Locator expected not to be empty"), e.getMessage());
    }
  }

  @Test
  void isEnabledPass() {
    page.setContent("<button>Text</button>");
    Locator locator = page.locator("button");
    assertThat(locator).isEnabled();
  }

  @Test
  void isEnabledFail() {
    page.setContent("<button disabled>Text</button>");
    Locator locator = page.locator("button");
    try {
      assertThat(locator).isEnabled(new LocatorAssertions.IsEnabledOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertNull(e.getExpected());
      assertNull(e.getActual());
      assertTrue(e.getMessage().contains("Locator expected to be enabled"), e.getMessage());
    }
  }

  @Test
  void notIsEnabledFail() {
    page.setContent("<button>Text</button>");
    Locator locator = page.locator("button");
    try {
      assertThat(locator).not().isEnabled(new LocatorAssertions.IsEnabledOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertNull(e.getExpected());
      assertNull(e.getActual());
      assertTrue(e.getMessage().contains("Locator expected not to be enabled"), e.getMessage());
    }
  }

  @Test
  void isFocusedPass() {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    locator.focus();
    assertThat(locator).isFocused();
  }

  @Test
  void isFocusedFail() {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    try {
      assertThat(locator).isFocused(new LocatorAssertions.IsFocusedOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertNull(e.getExpected());
      assertNull(e.getActual());
      assertTrue(e.getMessage().contains("Locator expected to be focused"), e.getMessage());
    }
  }

  @Test
  void notIsFocusedFail() {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    locator.focus();
    try {
      assertThat(locator).not().isFocused(new LocatorAssertions.IsFocusedOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertNull(e.getExpected());
      assertNull(e.getActual());
      assertTrue(e.getMessage().contains("Locator expected not to be focused"), e.getMessage());
    }
  }

  @Test
  void isHiddenPass() {
    page.setContent("<button style='display: none'></button>");
    Locator locator = page.locator("button");
    assertThat(locator).isHidden();
  }

  @Test
  void isHiddenFail() {
    page.setContent("<button></button>");
    Locator locator = page.locator("button");
    try {
      assertThat(locator).isHidden(new LocatorAssertions.IsHiddenOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertNull(e.getExpected());
      assertNull(e.getActual());
      assertTrue(e.getMessage().contains("Locator expected to be hidden"), e.getMessage());
    }
  }

  @Test
  void notIsHiddenFail() {
    page.setContent("<button style='display: none'></button>");
    Locator locator = page.locator("button");
    try {
      assertThat(locator).not().isHidden(new LocatorAssertions.IsHiddenOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertNull(e.getExpected());
      assertNull(e.getActual());
      assertTrue(e.getMessage().contains("Locator expected not to be hidden"), e.getMessage());
    }
  }

  @Test
  void isVisiblePass() {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    assertThat(locator).isVisible();
  }

  @Test
  void isVisibleFail() {
    page.setContent("<input style='display: none'></input>");
    Locator locator = page.locator("input");
    try {
      assertThat(locator).isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertNull(e.getExpected());
      assertNull(e.getActual());
      assertTrue(e.getMessage().contains("Locator expected to be visible"), e.getMessage());
    }
  }

  @Test
  void notIsVisibleFail() {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    try {
      assertThat(locator).not().isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(1000));
      fail("did not throw");
    } catch (AssertionFailedError e) {
      assertNull(e.getExpected());
      assertNull(e.getActual());
      assertTrue(e.getMessage().contains("Locator expected not to be visible"), e.getMessage());
    }
  }
}
