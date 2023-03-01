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
import com.microsoft.playwright.assertions.PlaywrightAssertions;
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
  void containsTextWRegexCaseInsensitivePass() {
    page.setContent("<div id=node>Text   content</div>");
    Locator locator = page.locator("#node");
    assertThat(locator).containsText(Pattern.compile("text", Pattern.CASE_INSENSITIVE));
  }

  @Test
  void containsTextWRegexMultilinePass() {
    page.setContent("<div id=node>Text \nContent</div>");
    Locator locator = page.locator("#node");
    assertThat(locator).containsText(Pattern.compile("^Content", Pattern.MULTILINE));
  }

  @Test
  void containsTextWRegexDotAllPass() {
    page.setContent("<div id=node>foo\nbar</div>");
    Locator locator = page.locator("#node");
    assertThat(locator).containsText(Pattern.compile("foo.bar", Pattern.DOTALL));
  }

  @Test
  void containsTextWRegexFail() {
    page.setContent("<div id=node>Text   content</div>");
    Locator locator = page.locator("#node");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).containsText(Pattern.compile("ex2"), new LocatorAssertions.ContainsTextOptions().setTimeout(1000));
    });
    assertEquals("ex2", e.getExpected().getStringRepresentation());
    assertEquals("Text   content", e.getActual().getValue());
    assertTrue(e.getMessage().contains("Locator expected to contain regex"), e.getMessage());
  }

  @Test
  void containsTextWTextPass() {
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

  @Test
  void containsTextWTextArrayPass() {
    page.setContent("<div>Text \n1</div><div>Text2</div><div>Text3</div>");
    Locator locator = page.locator("div");
    assertThat(locator).containsText(new String[] {"ext     1", "ext3"});
    // Should support ignoreCase.
    assertThat(locator).containsText(new String[] {"EXT 1", "eXt3"}, new LocatorAssertions.ContainsTextOptions().setIgnoreCase(true));
  }

  @Test
  void hasTextWRegexPass() {
    page.setContent("<div id=node>Text   content</div>");
    Locator locator = page.locator("#node");
    assertThat(locator).hasText(Pattern.compile("Te.t"));
    // Should not normalize whitespace.
    assertThat(locator).hasText(Pattern.compile("Text.+content"));
    // Should respect ignoreCase.
    assertThat(locator).hasText(Pattern.compile("text   content"), new LocatorAssertions.HasTextOptions().setIgnoreCase(true));
    // Should override regex flag with ignoreCase.
    assertThat(locator).not().hasText(Pattern.compile("text   content", Pattern.CASE_INSENSITIVE), new LocatorAssertions.HasTextOptions().setIgnoreCase(false));
  }

  @Test
  void hasTextWRegexFail() {
    page.setContent("<div id=node>Text   content</div>");
    Locator locator = page.locator("#node");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).hasText(Pattern.compile("Text 2"), new LocatorAssertions.HasTextOptions().setTimeout(1000));
    });
    assertEquals("Text 2", e.getExpected().getStringRepresentation());
    assertEquals("Text   content", e.getActual().getValue());
    assertTrue(e.getMessage().contains("Locator expected to have text matching regex"), e.getMessage());
  }

  @Test
  void hasTextWTextPass() {
    page.setContent("<div id=node><span></span>Text \ncontent&nbsp;    </div>");
    Locator locator = page.locator("#node");
    // Should normalize whitespace.
    assertThat(locator).hasText("Text                        content");
    // Should support ignoreCase.
    assertThat(locator).hasText("text CONTENT", new LocatorAssertions.HasTextOptions().setIgnoreCase(true));
    // Should support falsy ignoreCase.
    assertThat(locator).not().hasText("TEXT", new LocatorAssertions.HasTextOptions().setIgnoreCase(false));
  }

  @Test
  void hasTextWTextFail() {
    page.setContent("<div id=node>Text content</div>");
    Locator locator = page.locator("#node");
    // Should normalize whitespace.
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).hasText("Text", new LocatorAssertions.HasTextOptions().setTimeout(1000));
    });
    assertEquals("Text", e.getExpected().getStringRepresentation());
    assertEquals("Text content", e.getActual().getValue());
    assertTrue(e.getMessage().contains("Locator expected to have text"), e.getMessage());
  }

  @Test
  void hasTextWTextInnerTextPass() {
    page.setContent("<div id=node>Text <span hidden>garbage</span> content</div>");
    Locator locator = page.locator("#node");
    assertThat(locator).hasText("Text content", new LocatorAssertions.HasTextOptions().setUseInnerText(true));
  }

  @Test
  void hasTextWTextArrayPass() {
    page.setContent("<div>Text    \n1</div><div>Text   2a</div>");
    Locator locator = page.locator("div");
    // Should normalize whitespace.
    assertThat(locator).hasText(new String[] {"Text  1", "Text   2a"});
    // Should support ignoreCase.
    assertThat(locator).hasText(new String[] {"tEXT 1", "TExt 2A"}, new LocatorAssertions.HasTextOptions().setIgnoreCase(true));
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
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).not().hasText(new String[] {}, new LocatorAssertions.HasTextOptions().setTimeout(1000));
    });
    assertEquals("[]", e.getExpected().getStringRepresentation());
    assertEquals("[]", e.getActual().getStringRepresentation());
    assertTrue(e.getMessage().contains("Locator expected not to have text"), e.getMessage());
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
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      // Should normalize whitespace.
      assertThat(locator).hasText(new String[] {"Text 1", "Text 3", "Extra"}, new LocatorAssertions.HasTextOptions().setTimeout(1000));
    });
    assertEquals("[Text 1, Text 3, Extra]", e.getExpected().getStringRepresentation());
    assertEquals("[Text 1, Text 3]", e.getActual().getStringRepresentation());
    assertTrue(e.getMessage().contains("Locator expected to have text: [Text 1, Text 3, Extra]"), e.getMessage());
    assertTrue(e.getMessage().contains("Received: [Text 1, Text 3]"), e.getMessage());
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
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      // Should normalize whitespace.
      assertThat(locator).hasText(new Pattern[] {Pattern.compile( "Text 1"), Pattern.compile("Text   \\d"), Pattern.compile("Extra")}, new LocatorAssertions.HasTextOptions().setTimeout(1000));
    });
    assertEquals("[Text 1, Text   \\d, Extra]", e.getExpected().getStringRepresentation());
    assertEquals("[Text 1, Text 3]", e.getActual().getStringRepresentation());
    assertTrue(e.getMessage().contains("Locator expected to have text"), e.getMessage());
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
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).hasAttribute("id", "foo", new LocatorAssertions.HasAttributeOptions().setTimeout(1000));
    });
    assertEquals("foo", e.getExpected().getStringRepresentation());
    assertEquals("node", e.getActual().getStringRepresentation());
    assertTrue(e.getMessage().contains("Locator expected to have attribute 'id': foo\nReceived: node"), e.getMessage());
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
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).hasAttribute("id", Pattern.compile(".Nod.."), new LocatorAssertions.HasAttributeOptions().setTimeout(1000));
    });
    assertEquals(".Nod..", e.getExpected().getStringRepresentation());
    assertEquals("node", e.getActual().getStringRepresentation());
    assertTrue(e.getMessage().contains("Locator expected to have attribute 'id' matching regex: .Nod..\nReceived: node"), e.getMessage());
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
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).hasClass("foo bar baz", new LocatorAssertions.HasClassOptions().setTimeout(1000));
    });
    assertEquals("foo bar baz", e.getExpected().getStringRepresentation());
    assertEquals("bar baz", e.getActual().getStringRepresentation());
    assertTrue(e.getMessage().contains("Locator expected to have class"), e.getMessage());
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
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).hasClass(Pattern.compile("foo Z.*"), new LocatorAssertions.HasClassOptions().setTimeout(1000));
    });
    assertEquals("foo Z.*", e.getExpected().getStringRepresentation());
    assertEquals("bar baz", e.getActual().getStringRepresentation());
    assertTrue(e.getMessage().contains("Locator expected to have class matching regex"), e.getMessage());
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
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).hasClass(new String[] {"foo", "bar", "missing"}, new LocatorAssertions.HasClassOptions().setTimeout(1000));
    });
    assertEquals("[foo, bar, missing]", e.getExpected().getStringRepresentation());
    assertEquals("[foo, bar, baz]", e.getActual().getStringRepresentation());
    assertTrue(e.getMessage().contains("Locator expected to have class"), e.getMessage());
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
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).hasClass(new Pattern[] {Pattern.compile("fo.*"), Pattern.compile(".ar"), Pattern.compile("baz"), Pattern.compile("extra")}, new LocatorAssertions.HasClassOptions().setTimeout(1000));
    });
    assertEquals("[fo.*, .ar, baz, extra]", e.getExpected().getStringRepresentation());
    assertEquals("[foo, bar, baz]", e.getActual().getStringRepresentation());
    assertTrue(e.getMessage().contains("Locator expected to have class matching regex"), e.getMessage());
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
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).hasCount(1, new LocatorAssertions.HasCountOptions().setTimeout(1000));
    });
    assertEquals("1", e.getExpected().getStringRepresentation());
    assertEquals("2", e.getActual().getStringRepresentation());
    assertTrue(e.getMessage().contains("Locator expected to have count"), e.getMessage());
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
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).hasCSS("color", "red", new LocatorAssertions.HasCSSOptions().setTimeout(1000));
    });
    assertEquals("red", e.getExpected().getStringRepresentation());
    assertEquals("rgb(255, 0, 0)", e.getActual().getStringRepresentation());
    assertTrue(e.getMessage().contains("Locator expected to have CSS property 'color'"), e.getMessage());
  }

  @Test
  void hasCSSRegExPass() {
    page.setContent("<div id=node style='color: rgb(255, 0, 0)'>Text content</div>");
    Locator locator = page.locator("#node");
    assertThat(locator).hasCSS("color", Pattern.compile("rgb.*"));
  }

  @Test
  void hasCSSRegExFail() {
    page.setContent("<div id=node style='color: rgb(255, 0, 0)'>Text content</div>");
    Locator locator = page.locator("#node");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).hasCSS("color", Pattern.compile("red"), new LocatorAssertions.HasCSSOptions().setTimeout(1000));
    });
    assertEquals("red", e.getExpected().getStringRepresentation());
    assertEquals("rgb(255, 0, 0)", e.getActual().getStringRepresentation());
    assertTrue(e.getMessage().contains("Locator expected to have CSS property 'color' matching regex"), e.getMessage());
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
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).hasId("foo", new LocatorAssertions.HasIdOptions().setTimeout(1000));
    });
    assertEquals("foo", e.getExpected().getStringRepresentation());
    assertEquals("node", e.getActual().getStringRepresentation());
    assertTrue(e.getMessage().contains("Locator expected to have ID"), e.getMessage());
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
    page.evalOnSelector("div", "e => e.foo = 2021");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).hasJSProperty("foo", 1, new LocatorAssertions.HasJSPropertyOptions().setTimeout(1000));
    });
    assertEquals("1", e.getExpected().getStringRepresentation());
    assertEquals("2021", e.getActual().getStringRepresentation());
    assertTrue(e.getMessage().contains("Locator expected to have JavaScript property 'foo'"), e.getMessage());
  }

  @Test
  void hasJSPropertyObjectFail() {
    page.setContent("<div id=node>Text content</div>");
    Locator locator = page.locator("#node");
    page.evalOnSelector("div", "e => e.foo = { a: 1, b: 'string' }");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).hasJSProperty("foo", mapOf("a", 2), new LocatorAssertions.HasJSPropertyOptions().setTimeout(1000));
    });
    assertEquals("{a=2}", e.getExpected().getStringRepresentation());
    assertEquals("{a=1, b=string}", e.getActual().getStringRepresentation());
    assertTrue(e.getMessage().contains("Locator expected to have JavaScript property 'foo'"), e.getMessage());
  }

  @Test
  void hasJSPropertyStringFail() {
    page.setContent("<div id=node>Text content</div>");
    Locator locator = page.locator("#node");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).hasJSProperty("id", "foo", new LocatorAssertions.HasJSPropertyOptions().setTimeout(1000));
    });
    assertEquals("foo", e.getExpected().getStringRepresentation());
    assertEquals("node", e.getActual().getStringRepresentation());
    assertTrue(e.getMessage().contains("Locator expected to have JavaScript property 'id'"), e.getMessage());
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
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).hasValue("Text2", new LocatorAssertions.HasValueOptions().setTimeout(1000));
    });
    assertEquals("Text2", e.getExpected().getStringRepresentation());
    assertEquals("Text content", e.getActual().getStringRepresentation());
    assertTrue(e.getMessage().contains("Locator expected to have value"), e.getMessage());
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
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).hasValue(Pattern.compile("Text2"), new LocatorAssertions.HasValueOptions().setTimeout(1000));
    });
    assertEquals("Text2", e.getExpected().getStringRepresentation());
    assertEquals("Text content", e.getActual().getStringRepresentation());
    assertTrue(e.getMessage().contains("Locator expected to have value matching regex"), e.getMessage());
  }

  @Test
  void hasValuesWorksWithText() {
    page.setContent("<select multiple>\n" +
      "              <option value=\"R\">Red</option>\n" +
      "              <option value=\"G\">Green</option>\n" +
      "              <option value=\"B\">Blue</option>\n" +
      "            </select>");
    Locator locator = page.locator("select");
    locator.selectOption(new String[] {"R", "G"});
    assertThat(locator).hasValues(new String[]{"R", "G"});
  }

  @Test
  void hasValuesFollowsLabels() {
    page.setContent("<label for=\"colors\">Pick a Color</label>\n" +
      "            <select id=\"colors\" multiple>\n" +
      "              <option value=\"R\">Red</option>\n" +
      "              <option value=\"G\">Green</option>\n" +
      "              <option value=\"B\">Blue</option>\n" +
      "            </select>");
    Locator locator = page.locator("text=Pick a Color");
    locator.selectOption(new String[] {"R", "G"});
    assertThat(locator).hasValues(new String[]{"R", "G"});
  }

  @Test
  void hasValuesExactMatchWithText() {
    page.setContent("<select multiple>\n" +
      "              <option value=\"RR\">Red</option>\n" +
      "              <option value=\"GG\">Green</option>\n" +
      "            </select>");
    Locator locator = page.locator("select");
    locator.selectOption(new String[] {"RR", "GG"});
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).hasValues(new String[]{"R", "G"}, new LocatorAssertions.HasValuesOptions().setTimeout(1000));
    });
    assertEquals("[R, G]", e.getExpected().getStringRepresentation());
    assertEquals("[RR, GG]", e.getActual().getStringRepresentation());
    assertTrue(e.getMessage().contains("Locator expected to have values"), e.getMessage());
  }

  @Test
  void hasValuesWorksWithRegex() {
      page.setContent("<select multiple>\n" +
        "              <option value=\"R\">Red</option>\n" +
        "              <option value=\"G\">Green</option>\n" +
        "              <option value=\"B\">Blue</option>\n" +
        "            </select>");
    Locator locator = page.locator("select");
    locator.selectOption(new String[] {"R", "G"});
    assertThat(locator).hasValues(new Pattern[]{ Pattern.compile("R"), Pattern.compile("G")});
  }

  @Test
  void hasValuesFailsWhenItemsNotSelected() {
    page.setContent("<select multiple>\n" +
      "              <option value=\"R\">Red</option>\n" +
      "              <option value=\"G\">Green</option>\n" +
      "              <option value=\"B\">Blue</option>\n" +
      "            </select>");
    Locator locator = page.locator("select");
    locator.selectOption(new String[] {"B"}, new Locator.SelectOptionOptions().setTimeout(1000));
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).hasValues(new Pattern[]{ Pattern.compile("R"), Pattern.compile("G")});
    });
    assertEquals("[R, G]", e.getExpected().getStringRepresentation());
    assertEquals("[B]", e.getActual().getStringRepresentation());
    assertTrue(e.getMessage().contains("Locator expected to have values matching regex"), e.getMessage());
  }

  @Test
  void hasValuesFailsWhenMultipleNotSpecified() {
    page.setContent("<select>\n" +
      "              <option value=\"R\">Red</option>\n" +
      "              <option value=\"G\">Green</option>\n" +
      "              <option value=\"B\">Blue</option>\n" +
      "            </select>");
    Locator locator = page.locator("select");
    locator.selectOption(new String[] {"B"});
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      assertThat(locator).hasValues(new Pattern[]{ Pattern.compile("R"), Pattern.compile("G")});
    });
    assertTrue(e.getMessage().contains("Not a select element with a multiple attribute"), e.getMessage());
  }

  @Test
  void hasValuesFailsWhenNotASelectElement() {
    page.setContent("<input value=\"foo\" />");
    Locator locator = page.locator("input");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      assertThat(locator).hasValues(new Pattern[]{ Pattern.compile("R"), Pattern.compile("G")}, new LocatorAssertions.HasValuesOptions().setTimeout(1000));
    });
    assertTrue(e.getMessage().contains("Not a select element with a multiple attribute"), e.getMessage());
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
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).isChecked(new LocatorAssertions.IsCheckedOptions().setTimeout(1000));
    });
    assertNull(e.getExpected());
    assertNull(e.getActual());
    assertTrue(e.getMessage().contains("Locator expected to be checked"), e.getMessage());
  }

  @Test
  void notIsCheckedFail() {
    page.setContent("<input type=checkbox checked></input>");
    Locator locator = page.locator("input");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).not().isChecked(new LocatorAssertions.IsCheckedOptions().setTimeout(1000));
    });
    assertNull(e.getExpected());
    assertNull(e.getActual());
    assertTrue(e.getMessage().contains("Locator expected not to be checked"), e.getMessage());
  }

  @Test
  void isCheckedFalsePass() {
    page.setContent("<input type=checkbox></input>");
    Locator locator = page.locator("input");
    assertThat(locator).isChecked(new LocatorAssertions.IsCheckedOptions().setChecked(false));
  }

  @Test
  void isCheckedFalseFail() {
    page.setContent("<input checked type=checkbox></input>");
    Locator locator = page.locator("input");
    AssertionFailedError error = assertThrows(AssertionFailedError.class,
      () -> assertThat(locator).isChecked(new LocatorAssertions.IsCheckedOptions().setChecked(false).setTimeout(1000)));
    assertTrue(error.getMessage().contains("Locator expected to be unchecked"), error.getMessage());
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
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).isDisabled(new LocatorAssertions.IsDisabledOptions().setTimeout(1000));
    });
    assertNull(e.getExpected());
    assertNull(e.getActual());
    assertTrue(e.getMessage().contains("Locator expected to be disabled"), e.getMessage());
  }

  @Test
  void notIsDisabledFail() {
    page.setContent("<button disabled>Text</button>");
    Locator locator = page.locator("button");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).not().isDisabled(new LocatorAssertions.IsDisabledOptions().setTimeout(1000));
    });
    assertNull(e.getExpected());
    assertNull(e.getActual());
    assertTrue(e.getMessage().contains("Locator expected not to be disabled"), e.getMessage());
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
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).isEditable(new LocatorAssertions.IsEditableOptions().setTimeout(1000));
    });
    assertNull(e.getExpected());
    assertNull(e.getActual());
    assertTrue(e.getMessage().contains("Locator expected to be editable"), e.getMessage());
  }

  @Test
  void isEditableFalseFail() {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    AssertionFailedError error = assertThrows(AssertionFailedError.class,
      () -> assertThat(locator).isEditable(new LocatorAssertions.IsEditableOptions().setEditable(false).setTimeout(1000)));
    assertTrue(error.getMessage().contains("Locator expected to be readonly"), error.getMessage());
  }

  @Test
  void notIsEditableFail() {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).not().isEditable(new LocatorAssertions.IsEditableOptions().setTimeout(1000));
    });
    assertNull(e.getExpected());
    assertNull(e.getActual());
    assertTrue(e.getMessage().contains("Locator expected not to be editable"), e.getMessage());
  }

  @Test
  void isEditableWithNot() {
    page.setContent("<input readonly></input>");
    Locator locator = page.locator("input");
    assertThat(locator).not().isEditable();
  }

  @Test
  void isEditableWithEditableTrue() {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    assertThat(locator).isEditable(new LocatorAssertions.IsEditableOptions().setEditable(true));
  }

  @Test
  void isEditableWithEditableFalse() {
    page.setContent("<input readonly></input>");
    Locator locator = page.locator("input");
    assertThat(locator).isEditable(new LocatorAssertions.IsEditableOptions().setEditable(false));
  }

  @Test
  void isEditableWithNotAndEditableFalse() {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    assertThat(locator).not().isEditable(new LocatorAssertions.IsEditableOptions().setEditable(false));
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
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).isEmpty(new LocatorAssertions.IsEmptyOptions().setTimeout(1000));
    });
    assertNull(e.getExpected());
    assertNull(e.getActual());
    assertTrue(e.getMessage().contains("Locator expected to be empty"), e.getMessage());
  }

  @Test
  void notIsEmptyFail() {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).not().isEmpty(new LocatorAssertions.IsEmptyOptions().setTimeout(1000));
    });
    assertNull(e.getExpected());
    assertNull(e.getActual());
    assertTrue(e.getMessage().contains("Locator expected not to be empty"), e.getMessage());
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
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).isEnabled(new LocatorAssertions.IsEnabledOptions().setTimeout(1000));
    });
    assertNull(e.getExpected());
    assertNull(e.getActual());
    assertTrue(e.getMessage().contains("Locator expected to be enabled"), e.getMessage());
  }

  @Test
  void notIsEnabledFail() {
    page.setContent("<button>Text</button>");
    Locator locator = page.locator("button");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).not().isEnabled(new LocatorAssertions.IsEnabledOptions().setTimeout(1000));
    });
    assertNull(e.getExpected());
    assertNull(e.getActual());
    assertTrue(e.getMessage().contains("Locator expected not to be enabled"), e.getMessage());
  }

  @Test
  void isEnabledTrue() {
    page.setContent("<button>Text</button>");
    Locator locator = page.locator("button");
    assertThat(locator).isEnabled(new LocatorAssertions.IsEnabledOptions().setEnabled(true));
  }

  @Test
  void isEnabledFalse() {
    page.setContent("<button disabled>Text</button>");
    Locator locator = page.locator("button");
    assertThat(locator).isEnabled(new LocatorAssertions.IsEnabledOptions().setEnabled(false));
  }

  @Test
  void isEnabledFalseFail() {
    page.setContent("<button>Text</button>");
    Locator locator = page.locator("button");
    AssertionFailedError error = assertThrows(AssertionFailedError.class,
      () -> assertThat(locator).isEnabled(new LocatorAssertions.IsEnabledOptions().setEnabled(false).setTimeout(1000)));
    assertTrue(error.getMessage().contains("Locator expected to be disabled"), error.getMessage());
  }

  @Test
  void isEnabledEventually() {
    page.setContent("<button disabled>Text</button>");
    Locator locator = page.locator("button");
    locator.evaluate("e => setTimeout(() => {\n" +
      "  e.removeAttribute('disabled');\n" +
      "}, 500);\n");
    assertThat(locator).isEnabled();
  }

  @Test
  void isEnabledEventuallyWithNot() {
    page.setContent("<button>Text</button>");
    Locator locator = page.locator("button");
    locator.evaluate("e => setTimeout(() => {\n" +
      "  e.setAttribute('disabled', '');\n" +
      "}, 500);\n");
    assertThat(locator).not().isEnabled();
  }

  @Test
  void isEnabledWithNotAndEnabledFalse() {
    page.setContent("<button>Text</button>");
    Locator locator = page.locator("button");
    assertThat(locator).not().isEnabled(new LocatorAssertions.IsEnabledOptions().setEnabled(false));
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
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).isFocused(new LocatorAssertions.IsFocusedOptions().setTimeout(1000));
    });
    assertNull(e.getExpected());
    assertNull(e.getActual());
    assertTrue(e.getMessage().contains("Locator expected to be focused"), e.getMessage());
  }

  @Test
  void notIsFocusedFail() {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    locator.focus();
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).not().isFocused(new LocatorAssertions.IsFocusedOptions().setTimeout(1000));
    });
    assertNull(e.getExpected());
    assertNull(e.getActual());
    assertTrue(e.getMessage().contains("Locator expected not to be focused"), e.getMessage());
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
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).isHidden(new LocatorAssertions.IsHiddenOptions().setTimeout(1000));
    });
    assertNull(e.getExpected());
    assertNull(e.getActual());
    assertTrue(e.getMessage().contains("Locator expected to be hidden"), e.getMessage());
  }

  @Test
  void notIsHiddenFail() {
    page.setContent("<button style='display: none'></button>");
    Locator locator = page.locator("button");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).not().isHidden(new LocatorAssertions.IsHiddenOptions().setTimeout(1000));
    });
    assertNull(e.getExpected());
    assertNull(e.getActual());
    assertTrue(e.getMessage().contains("Locator expected not to be hidden"), e.getMessage());
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
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(1000));
    });
    assertNull(e.getExpected());
    assertNull(e.getActual());
    assertTrue(e.getMessage().contains("Locator expected to be visible"), e.getMessage());
  }

  @Test
  void isVisibleFalseFail() {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    AssertionFailedError error = assertThrows(AssertionFailedError.class,
      () -> assertThat(locator).isVisible(new LocatorAssertions.IsVisibleOptions().setVisible(false).setTimeout(1000)));
    assertTrue(error.getMessage().contains("Locator expected to be hidden"), error.getMessage());
  }

  @Test
  void notIsVisibleFail() {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).not().isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(1000));
    });
    assertNull(e.getExpected());
    assertNull(e.getActual());
    assertTrue(e.getMessage().contains("Locator expected not to be visible"), e.getMessage());
  }

  @Test
  void isVisibleWithTrue() {
    page.setContent("<button>hello</button>");
    Locator locator = page.locator("button");
    assertThat(locator).isVisible(new LocatorAssertions.IsVisibleOptions().setVisible(true));
  }

  @Test
  void isVisibleWithFalse() {
    page.setContent("<button hidden>hello</button>");
    Locator locator = page.locator("button");
    assertThat(locator).isVisible(new LocatorAssertions.IsVisibleOptions().setVisible(false));
  }

  @Test
  void isVisibleWithNotAndFalse() {
    page.setContent("<button>hello</button>");
    Locator locator = page.locator("button");
    assertThat(locator).not().isVisible(new LocatorAssertions.IsVisibleOptions().setVisible(false));
  }

  @Test
  void isVisibleEventually() {
    page.setContent("<div></div>");
    Locator locator = page.locator("span");
    page.evalOnSelector("div", "div => setTimeout(() => {\n" +
      "      div.innerHTML = '<span>Hello</span>';\n" +
      "    }, 10);");
    assertThat(locator).isVisible();
  }

  @Test
  void isVisibleEventuallyWithNot() {
    page.setContent("<div><span>Hello</span></div>");
    Locator locator = page.locator("span");
    page.evalOnSelector("span", "span => setTimeout(() => {\n" +
      "      span.textContent = '';\n" +
      "    }, 10);");
    assertThat(locator).not().isVisible();
  }

  @Test
  void locatorCountShouldWorkWithDeletedMapInMainWorld() {
    page.evaluate("Map = 1");
    page.locator("#searchResultTableDiv .x-grid3-row").count();
    assertThat(page.locator("#searchResultTableDiv .x-grid3-row")).hasCount(0);
  }

  @Test
  void defaultTimeoutHasTextFail() {
    page.setContent("<div></div>");
    Locator locator = page.locator("div");
    PlaywrightAssertions.setDefaultAssertionTimeout(1000);
    AssertionFailedError exception = assertThrows(AssertionFailedError.class, () -> assertThat(locator).hasText("foo"));
    assertTrue(exception.getMessage().contains("Locator.expect with timeout 1000ms"), exception.getMessage());
    // Restore default.
    PlaywrightAssertions.setDefaultAssertionTimeout(5_000);
  }

  @Test
  void defaultTimeoutHasTextPass() {
    page.setContent("<div>foo</div>");
    Locator locator = page.locator("div");
    PlaywrightAssertions.setDefaultAssertionTimeout(1000);
    assertThat(locator).hasText("foo");
    // Restore default.
    PlaywrightAssertions.setDefaultAssertionTimeout(5_000);
  }

  @Test
  void defaultTimeoutZeroHasTextPass() {
    page.setContent("<div>foo</div>");
    Locator locator = page.locator("div");
    PlaywrightAssertions.setDefaultAssertionTimeout(0);
    assertThat(locator).hasText("foo");
    // Restore default.
    PlaywrightAssertions.setDefaultAssertionTimeout(5_000);
  }
}
