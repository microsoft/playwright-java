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
import com.microsoft.playwright.assertions.LocatorAssertions.ContainsClassOptions;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.opentest4j.AssertionFailedError;

import java.util.regex.Pattern;

import static com.microsoft.playwright.Utils.mapOf;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class TestLocatorAssertions {
  @PlaywrightTest
  void containsTextWRegexPass(Page page) {
    page.setContent("<div id=node>Text   content</div>");
    Locator locator = page.locator("#node");
    assertThat(locator).containsText(Pattern.compile("ex"));
    // Should not normalize whitespace.
    assertThat(locator).containsText(Pattern.compile("ext   cont"));
  }

  @PlaywrightTest
  void containsTextWRegexCaseInsensitivePass(Page page) {
    page.setContent("<div id=node>Text   content</div>");
    Locator locator = page.locator("#node");
    assertThat(locator).containsText(Pattern.compile("text", Pattern.CASE_INSENSITIVE));
  }

  @PlaywrightTest
  void containsTextWRegexMultilinePass(Page page) {
    page.setContent("<div id=node>Text \nContent</div>");
    Locator locator = page.locator("#node");
    assertThat(locator).containsText(Pattern.compile("^Content", Pattern.MULTILINE));
  }

  @PlaywrightTest
  void containsTextWRegexDotAllPass(Page page) {
    page.setContent("<div id=node>foo\nbar</div>");
    Locator locator = page.locator("#node");
    assertThat(locator).containsText(Pattern.compile("foo.bar", Pattern.DOTALL));
  }

  @PlaywrightTest
  void containsTextWRegexFail(Page page) {
    page.setContent("<div id=node>Text   content</div>");
    Locator locator = page.locator("#node");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).containsText(Pattern.compile("ex2"), new LocatorAssertions.ContainsTextOptions().setTimeout(1000));
    });
    assertEquals("ex2", e.getExpected().getStringRepresentation());
    assertEquals("Text   content", e.getActual().getValue());
    assertTrue(e.getMessage().contains("Locator expected to contain regex"), e.getMessage());
  }

  @PlaywrightTest
  void containsTextWTextPass(Page page) {
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
  void containsTextWTextArrayPass(Page page) {
    page.setContent("<div>Text \n1</div><div>Text2</div><div>Text3</div>");
    Locator locator = page.locator("div");
    assertThat(locator).containsText(new String[] {"ext     1", "ext3"});
    // Should support ignoreCase.
    assertThat(locator).containsText(new String[] {"EXT 1", "eXt3"}, new LocatorAssertions.ContainsTextOptions().setIgnoreCase(true));
  }

  @PlaywrightTest
  void hasTextWRegexPass(Page page) {
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

  @PlaywrightTest
  void hasTextWRegexFail(Page page) {
    page.setContent("<div id=node>Text   content</div>");
    Locator locator = page.locator("#node");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).hasText(Pattern.compile("Text 2"), new LocatorAssertions.HasTextOptions().setTimeout(1000));
    });
    assertEquals("Text 2", e.getExpected().getStringRepresentation());
    assertEquals("Text   content", e.getActual().getValue());
    assertTrue(e.getMessage().contains("Locator expected to have text matching regex"), e.getMessage());
  }

  @PlaywrightTest
  void hasTextWTextPass(Page page) {
    page.setContent("<div id=node><span></span>Text \ncontent&nbsp;    </div>");
    Locator locator = page.locator("#node");
    // Should normalize whitespace.
    assertThat(locator).hasText("Text                        content");
    // Should support ignoreCase.
    assertThat(locator).hasText("text CONTENT", new LocatorAssertions.HasTextOptions().setIgnoreCase(true));
    // Should support falsy ignoreCase.
    assertThat(locator).not().hasText("TEXT", new LocatorAssertions.HasTextOptions().setIgnoreCase(false));
  }

  @PlaywrightTest
  void hasTextWTextFail(Page page) {
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

  @PlaywrightTest
  void hasTextWTextInnerTextPass(Page page) {
    page.setContent("<div id=node>Text <span hidden>garbage</span> content</div>");
    Locator locator = page.locator("#node");
    assertThat(locator).hasText("Text content", new LocatorAssertions.HasTextOptions().setUseInnerText(true));
  }

  @PlaywrightTest
  void hasTextWTextArrayPass(Page page) {
    page.setContent("<div>Text    \n1</div><div>Text   2a</div>");
    Locator locator = page.locator("div");
    // Should normalize whitespace.
    assertThat(locator).hasText(new String[] {"Text  1", "Text   2a"});
    // Should support ignoreCase.
    assertThat(locator).hasText(new String[] {"tEXT 1", "TExt 2A"}, new LocatorAssertions.HasTextOptions().setIgnoreCase(true));
  }

  @PlaywrightTest
  void hasTextWTextArrayPassEmpty(Page page) {
    page.setContent("<div></div>");
    Locator locator = page.locator("p");
    // Should normalize whitespace.
    assertThat(locator).hasText(new String[] {});
  }

  @PlaywrightTest
  void hasTextWTextArrayPassNotEmpty(Page page) {
    page.setContent("<div><p>Test</p></div>");
    Locator locator = page.locator("div");
    // Should normalize whitespace.
    assertThat(locator).not().hasText(new String[] {});
  }

  @PlaywrightTest
  void hasTextWTextArrayPassOnEmpty(Page page) {
    page.setContent("<div></div>");
    Locator locator = page.locator("p");
    // Should normalize whitespace.
    assertThat(locator).not().hasText(new String[] {"Test"});
  }

  @PlaywrightTest
  void hasTextWTextArrayFailOnNotEmpty(Page page) {
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

  @PlaywrightTest
  void hasTextWTextArrayPassLazyPass(Page page) {
    page.setContent("<div id=div></div>");
    Locator locator = page.locator("p");
    page.evaluate("setTimeout(() => {\n" +
      "  div.innerHTML = \"<p>Text 1</p><p>Text 2</p>\";\n" +
      "}, 100);");
    // Should normalize whitespace.
    assertThat(locator).hasText(new String[] {"Text  1", "Text   2"}, new LocatorAssertions.HasTextOptions().setTimeout(1000));
  }

  @PlaywrightTest
  void hasTextWTextArrayFail(Page page) {
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
    assertTrue(e.getMessage().contains("Locator expected to have text\nExpected: [Text 1, Text 3, Extra]"), e.getMessage());
    assertTrue(e.getMessage().contains("Received: [Text 1, Text 3]"), e.getMessage());
  }

  @PlaywrightTest
  void hasTextWRegExArrayPass(Page page) {
    page.setContent("<div>Text    \n1</div><div>Text   2a</div>");
    Locator locator = page.locator("div");
    // Should normalize whitespace.
    assertThat(locator).hasText(new Pattern[] {Pattern.compile( "Text    \n1"), Pattern.compile("Text   \\d+a")});
  }

  @PlaywrightTest
  void hasTextWRegExArrayFail(Page page) {
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

  @PlaywrightTest
  void hasAttributeTextIgnoreCase(Page page) {
    page.setContent("<div id=NoDe>Text content</div>");
    Locator locator = page.locator("#NoDe");
    assertThat(locator).hasAttribute("id", "node", new LocatorAssertions.HasAttributeOptions().setIgnoreCase(true));
    assertThat(locator).not().hasAttribute("id", "node");
  }

  @PlaywrightTest
  void hasAttributeTextPass(Page page) {
    page.setContent("<div id=node>Text content</div>");
    Locator locator = page.locator("#node");
    assertThat(locator).hasAttribute("id", "node");
  }

  @PlaywrightTest
  void hasAttributeTextFail(Page page) {
    page.setContent("<div id=node>Text content</div>");
    Locator locator = page.locator("#node");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).hasAttribute("id", "foo", new LocatorAssertions.HasAttributeOptions().setTimeout(1000));
    });
    assertEquals("foo", e.getExpected().getStringRepresentation());
    assertEquals("node", e.getActual().getStringRepresentation());
    assertTrue(e.getMessage().contains("Locator expected to have attribute 'id'\nExpected: foo\nReceived: node"), e.getMessage());
  }

  @PlaywrightTest
  void hasAttributeRegExpPass(Page page) {
    page.setContent("<div id=node>Text content</div>");
    Locator locator = page.locator("#node");
    assertThat(locator).hasAttribute("id", Pattern.compile("n..e"));
  }

  @PlaywrightTest
  void hasAttributeRegExpFail(Page page) {
    page.setContent("<div id=node>Text content</div>");
    Locator locator = page.locator("#node");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).hasAttribute("id", Pattern.compile(".Nod.."), new LocatorAssertions.HasAttributeOptions().setTimeout(1000));
    });
    assertEquals(".Nod..", e.getExpected().getStringRepresentation());
    assertEquals("node", e.getActual().getStringRepresentation());
    assertTrue(e.getMessage().contains("Locator expected to have attribute 'id' matching regex\nExpected: .Nod..\nReceived: node"), e.getMessage());
  }

  @PlaywrightTest
  void hasClassTextPass(Page page) {
    page.setContent("<div class=\"foo bar baz\"></div>");
    Locator locator = page.locator("div");
    assertThat(locator).hasClass("foo bar baz");
  }

  @PlaywrightTest
  void hasClassTextFail(Page page) {
    page.setContent("<div class=\"bar baz\"></div>");
    Locator locator = page.locator("div");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).hasClass("foo bar baz", new LocatorAssertions.HasClassOptions().setTimeout(1000));
    });
    assertEquals("foo bar baz", e.getExpected().getStringRepresentation());
    assertEquals("bar baz", e.getActual().getStringRepresentation());
    assertTrue(e.getMessage().contains("Locator expected to have class"), e.getMessage());
  }

  @PlaywrightTest
  void hasClassRegExpPass(Page page) {
    page.setContent("<div class=\"foo bar baz\"></div>");
    Locator locator = page.locator("div");
    assertThat(locator).hasClass(Pattern.compile("foo.* baz"));
  }

  @PlaywrightTest
  void hasClassRegExpFail(Page page) {
    page.setContent("<div class=\"bar baz\"></div>");
    Locator locator = page.locator("div");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).hasClass(Pattern.compile("foo Z.*"), new LocatorAssertions.HasClassOptions().setTimeout(1000));
    });
    assertEquals("foo Z.*", e.getExpected().getStringRepresentation());
    assertEquals("bar baz", e.getActual().getStringRepresentation());
    assertTrue(e.getMessage().contains("Locator expected to have class matching regex"), e.getMessage());
  }

  @PlaywrightTest
  void hasClassTextArrayPass(Page page) {
    page.setContent("<div class=\"foo\"></div><div class=\"bar\"></div><div class=\"baz\"></div>");
    Locator locator = page.locator("div");
    assertThat(locator).hasClass(new String[] {"foo", "bar", "baz"});
  }

  @PlaywrightTest
  void hasClassTextArrayFail(Page page) {
    page.setContent("<div class=\"foo\"></div><div class=\"bar\"></div><div class=\"baz\"></div>");
    Locator locator = page.locator("div");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).hasClass(new String[] {"foo", "bar", "missing"}, new LocatorAssertions.HasClassOptions().setTimeout(1000));
    });
    assertEquals("[foo, bar, missing]", e.getExpected().getStringRepresentation());
    assertEquals("[foo, bar, baz]", e.getActual().getStringRepresentation());
    assertTrue(e.getMessage().contains("Locator expected to have class"), e.getMessage());
  }

  @PlaywrightTest
  void hasClassRegExpArrayPass(Page page) {
    page.setContent("<div class=\"foo\"></div><div class=\"bar\"></div><div class=\"baz\"></div>");
    Locator locator = page.locator("div");
    assertThat(locator).hasClass(new Pattern[] {Pattern.compile("fo.*"), Pattern.compile(".ar"), Pattern.compile("baz")});
  }

  @PlaywrightTest
  void hasClassRegExpArrayFail(Page page) {
    page.setContent("<div class=\"foo\"></div><div class=\"bar\"></div><div class=\"baz\"></div>");
    Locator locator = page.locator("div");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).hasClass(new Pattern[] {Pattern.compile("fo.*"), Pattern.compile(".ar"), Pattern.compile("baz"), Pattern.compile("extra")}, new LocatorAssertions.HasClassOptions().setTimeout(1000));
    });
    assertEquals("[fo.*, .ar, baz, extra]", e.getExpected().getStringRepresentation());
    assertEquals("[foo, bar, baz]", e.getActual().getStringRepresentation());
    assertTrue(e.getMessage().contains("Locator expected to have class matching regex"), e.getMessage());
  }

  @PlaywrightTest
  void hasCountPass(Page page) {
    page.setContent("<select><option>One</option><option>Two</option></select>");
    Locator locator = page.locator("option");
    assertThat(locator).hasCount(2);
  }

  @PlaywrightTest
  void hasCountFail(Page page) {
    page.setContent("<select><option>One</option><option>Two</option></select>");
    Locator locator = page.locator("option");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).hasCount(1, new LocatorAssertions.HasCountOptions().setTimeout(1000));
    });
    assertEquals("1", e.getExpected().getStringRepresentation());
    assertEquals("2", e.getActual().getStringRepresentation());
    assertTrue(e.getMessage().contains("Locator expected to have count"), e.getMessage());
  }

  @PlaywrightTest
  void hasCountPassZero(Page page) {
    page.setContent("<div></div>");
    Locator locator = page.locator("span");
    assertThat(locator).hasCount(0);
    assertThat(locator).not().hasCount(1);
  }

  @PlaywrightTest
  void hasCSSPass(Page page) {
    page.setContent("<div id=node style='color: rgb(255, 0, 0)'>Text content</div>");
    Locator locator = page.locator("#node");
    assertThat(locator).hasCSS("color", "rgb(255, 0, 0)");
  }

  @PlaywrightTest
  void hasCSSFail(Page page) {
    page.setContent("<div id=node style='color: rgb(255, 0, 0)'>Text content</div>");
    Locator locator = page.locator("#node");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).hasCSS("color", "red", new LocatorAssertions.HasCSSOptions().setTimeout(1000));
    });
    assertEquals("red", e.getExpected().getStringRepresentation());
    assertEquals("rgb(255, 0, 0)", e.getActual().getStringRepresentation());
    assertTrue(e.getMessage().contains("Locator expected to have CSS property 'color'"), e.getMessage());
  }

  @PlaywrightTest
  void hasCSSRegExPass(Page page) {
    page.setContent("<div id=node style='color: rgb(255, 0, 0)'>Text content</div>");
    Locator locator = page.locator("#node");
    assertThat(locator).hasCSS("color", Pattern.compile("rgb.*"));
  }

  @PlaywrightTest
  void hasCSSRegExFail(Page page) {
    page.setContent("<div id=node style='color: rgb(255, 0, 0)'>Text content</div>");
    Locator locator = page.locator("#node");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).hasCSS("color", Pattern.compile("red"), new LocatorAssertions.HasCSSOptions().setTimeout(1000));
    });
    assertEquals("red", e.getExpected().getStringRepresentation());
    assertEquals("rgb(255, 0, 0)", e.getActual().getStringRepresentation());
    assertTrue(e.getMessage().contains("Locator expected to have CSS property 'color' matching regex"), e.getMessage());
  }

  @PlaywrightTest
  void hasIdPass(Page page) {
    page.setContent("<div id=node>Text content</div>");
    Locator locator = page.locator("#node");
    assertThat(locator).hasId("node");
  }

  @PlaywrightTest
  void hasIdFail(Page page) {
    page.setContent("<div id=node>Text content</div>");
    Locator locator = page.locator("#node");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).hasId("foo", new LocatorAssertions.HasIdOptions().setTimeout(1000));
    });
    assertEquals("foo", e.getExpected().getStringRepresentation());
    assertEquals("node", e.getActual().getStringRepresentation());
    assertTrue(e.getMessage().contains("Locator expected to have ID"), e.getMessage());
  }

  @PlaywrightTest
  void hasJSPropertyPass(Page page) {
    page.setContent("<div></div>");
    page.evalOnSelector("div", "e => e.foo = { a: 1, b: 'string' }");
    Locator locator = page.locator("div");
    assertThat(locator).hasJSProperty("foo", mapOf("a", 1, "b", "string"));
  }

  @PlaywrightTest
  void hasJSPropertyNumberFail(Page page) {
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

  @PlaywrightTest
  void hasJSPropertyObjectFail(Page page) {
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

  @PlaywrightTest
  void hasJSPropertyStringFail(Page page) {
    page.setContent("<div id=node>Text content</div>");
    Locator locator = page.locator("#node");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).hasJSProperty("id", "foo", new LocatorAssertions.HasJSPropertyOptions().setTimeout(1000));
    });
    assertEquals("foo", e.getExpected().getStringRepresentation());
    assertEquals("node", e.getActual().getStringRepresentation());
    assertTrue(e.getMessage().contains("Locator expected to have JavaScript property 'id'"), e.getMessage());
  }

  @PlaywrightTest
  void hasValueTextPass(Page page) {
    page.setContent("<input id=node></input>");
    Locator locator = page.locator("#node");
    locator.fill("Text content");
    assertThat(locator).hasValue("Text content");
  }

  @PlaywrightTest
  void hasValueTextFail(Page page) {
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

  @PlaywrightTest
  void hasValueRegExpPass(Page page) {
    page.setContent("<input id=node></input>");
    Locator locator = page.locator("#node");
    locator.fill("Text content");
    assertThat(locator).hasValue(Pattern.compile("Text"));
  }

  @PlaywrightTest
  void hasValueRegExpPassWithNot(Page page) {
    page.setContent("<input id=node></input>");
    Locator locator = page.locator("#node");
    locator.fill("Text content");
    assertThat(locator).not().hasValue(Pattern.compile("Text2"));
  }

  @PlaywrightTest
  void hasValueRegExpFail(Page page) {
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

  @PlaywrightTest
  void hasValuesWorksWithText(Page page) {
    page.setContent("<select multiple>\n" +
      "              <option value=\"R\">Red</option>\n" +
      "              <option value=\"G\">Green</option>\n" +
      "              <option value=\"B\">Blue</option>\n" +
      "            </select>");
    Locator locator = page.locator("select");
    locator.selectOption(new String[] {"R", "G"});
    assertThat(locator).hasValues(new String[]{"R", "G"});
  }

  @PlaywrightTest
  void hasValuesFollowsLabels(Page page) {
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

  @PlaywrightTest
  void hasValuesExactMatchWithText(Page page) {
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

  @PlaywrightTest
  void hasValuesWorksWithRegex(Page page) {
      page.setContent("<select multiple>\n" +
        "              <option value=\"R\">Red</option>\n" +
        "              <option value=\"G\">Green</option>\n" +
        "              <option value=\"B\">Blue</option>\n" +
        "            </select>");
    Locator locator = page.locator("select");
    locator.selectOption(new String[] {"R", "G"});
    assertThat(locator).hasValues(new Pattern[]{ Pattern.compile("R"), Pattern.compile("G")});
  }

  @PlaywrightTest
  void hasValuesFailsWhenItemsNotSelected(Page page) {
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

  @PlaywrightTest
  void hasValuesFailsWhenMultipleNotSpecified(Page page) {
    page.setContent("<select>\n" +
      "              <option value=\"R\">Red</option>\n" +
      "              <option value=\"G\">Green</option>\n" +
      "              <option value=\"B\">Blue</option>\n" +
      "            </select>");
    Locator locator = page.locator("select");
    locator.selectOption(new String[] {"B"});
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).hasValues(new Pattern[]{ Pattern.compile("R"), Pattern.compile("G")});
    });
    assertTrue(e.getMessage().contains("Not a select element with a multiple attribute"), e.getMessage());
  }

  @PlaywrightTest
  void hasValuesFailsWhenNotASelectElement(Page page) {
    page.setContent("<input value=\"foo\" />");
    Locator locator = page.locator("input");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).hasValues(new Pattern[]{ Pattern.compile("R"), Pattern.compile("G")}, new LocatorAssertions.HasValuesOptions().setTimeout(1000));
    });
    assertTrue(e.getMessage().contains("Not a select element with a multiple attribute"), e.getMessage());
  }

  @PlaywrightTest
  void isCheckedPass(Page page) {
    page.setContent("<input type=checkbox checked></input>");
    Locator locator = page.locator("input");
    assertThat(locator).isChecked();
  }

  @PlaywrightTest
  void isCheckedFail(Page page) {
    page.setContent("<input type=checkbox></input>");
    Locator locator = page.locator("input");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).isChecked(new LocatorAssertions.IsCheckedOptions().setTimeout(1000));
    });
    assertEquals("checked", e.getExpected().getStringRepresentation());
    assertEquals("unchecked", e.getActual().getStringRepresentation());
    assertTrue(e.getMessage().contains("Locator expected to be\nExpected: checked"), e.getMessage());
  }

  @PlaywrightTest
  void notIsCheckedFail(Page page) {
    page.setContent("<input type=checkbox checked></input>");
    Locator locator = page.locator("input");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).not().isChecked(new LocatorAssertions.IsCheckedOptions().setTimeout(1000));
    });

    assertEquals("checked", e.getExpected().getStringRepresentation());
    assertEquals("checked", e.getActual().getStringRepresentation());
    assertTrue(e.getMessage().contains("Locator expected not to be\nExpected: checked"), e.getMessage());
  }

  @PlaywrightTest
  void isCheckedFalsePass(Page page) {
    page.setContent("<input type=checkbox></input>");
    Locator locator = page.locator("input");
    assertThat(locator).isChecked(new LocatorAssertions.IsCheckedOptions().setChecked(false));
  }

  @PlaywrightTest
  void isCheckedFalseFail(Page page) {
    page.setContent("<input checked type=checkbox></input>");
    Locator locator = page.locator("input");
    AssertionFailedError error = assertThrows(AssertionFailedError.class,
      () -> assertThat(locator).isChecked(new LocatorAssertions.IsCheckedOptions().setChecked(false).setTimeout(1000)));
    assertTrue(error.getMessage().contains("Locator expected to be\nExpected: unchecked"), error.getMessage());
  }

  @PlaywrightTest
  void isDisabledPass(Page page) {
    page.setContent("<button disabled>Text</button>");
    Locator locator = page.locator("button");
    assertThat(locator).isDisabled();
  }

  @PlaywrightTest
  void isDisabledFail(Page page) {
    page.setContent("<button>Text</button>");
    Locator locator = page.locator("button");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).isDisabled(new LocatorAssertions.IsDisabledOptions().setTimeout(1000));
    });
    assertNull(e.getExpected());
    assertNull(e.getActual());
    assertTrue(e.getMessage().contains("Locator expected to be disabled"), e.getMessage());
  }

  @PlaywrightTest
  void notIsDisabledFail(Page page) {
    page.setContent("<button disabled>Text</button>");
    Locator locator = page.locator("button");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).not().isDisabled(new LocatorAssertions.IsDisabledOptions().setTimeout(1000));
    });
    assertNull(e.getExpected());
    assertNull(e.getActual());
    assertTrue(e.getMessage().contains("Locator expected not to be disabled"), e.getMessage());
  }

  @PlaywrightTest
  void isEditablePass(Page page) {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    assertThat(locator).isEditable();
  }

  @PlaywrightTest
  void isEditableFail(Page page) {
    page.setContent("<input disabled></input>");
    Locator locator = page.locator("input");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).isEditable(new LocatorAssertions.IsEditableOptions().setTimeout(1000));
    });
    assertNull(e.getExpected());
    assertNull(e.getActual());
    assertTrue(e.getMessage().contains("Locator expected to be editable"), e.getMessage());
  }

  @PlaywrightTest
  void isEditableFalseFail(Page page) {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    AssertionFailedError error = assertThrows(AssertionFailedError.class,
      () -> assertThat(locator).isEditable(new LocatorAssertions.IsEditableOptions().setEditable(false).setTimeout(1000)));
    assertTrue(error.getMessage().contains("Locator expected to be readonly"), error.getMessage());
  }

  @PlaywrightTest
  void notIsEditableFail(Page page) {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).not().isEditable(new LocatorAssertions.IsEditableOptions().setTimeout(1000));
    });
    assertNull(e.getExpected());
    assertNull(e.getActual());
    assertTrue(e.getMessage().contains("Locator expected not to be editable"), e.getMessage());
  }

  @PlaywrightTest
  void isEditableWithNot(Page page) {
    page.setContent("<input readonly></input>");
    Locator locator = page.locator("input");
    assertThat(locator).not().isEditable();
  }

  @PlaywrightTest
  void isEditableWithEditableTrue(Page page) {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    assertThat(locator).isEditable(new LocatorAssertions.IsEditableOptions().setEditable(true));
  }

  @PlaywrightTest
  void isEditableWithEditableFalse(Page page) {
    page.setContent("<input readonly></input>");
    Locator locator = page.locator("input");
    assertThat(locator).isEditable(new LocatorAssertions.IsEditableOptions().setEditable(false));
  }

  @PlaywrightTest
  void isEditableWithNotAndEditableFalse(Page page) {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    assertThat(locator).not().isEditable(new LocatorAssertions.IsEditableOptions().setEditable(false));
  }

  @PlaywrightTest
  void isEditableThrowsOnNonInputElement(Page page) {
    page.setContent("<button>");
    Locator locator = page.locator("button");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> assertThat(locator).isEditable());
    assertTrue(e.getMessage().contains("Element is not an <input>, <textarea>, <select> or [contenteditable] and does not have a role allowing [aria-readonly]"), e.getMessage());
  }

  @PlaywrightTest
  void isEmptyPass(Page page) {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    assertThat(locator).isEmpty();
  }

  @PlaywrightTest
  void isEmptyFail(Page page) {
    page.setContent("<input value=text></input>");
    Locator locator = page.locator("input");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).isEmpty(new LocatorAssertions.IsEmptyOptions().setTimeout(1000));
    });
    assertNull(e.getExpected());
    assertNull(e.getActual());
    assertTrue(e.getMessage().contains("Locator expected to be empty"), e.getMessage());
  }

  @PlaywrightTest
  void notIsEmptyFail(Page page) {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).not().isEmpty(new LocatorAssertions.IsEmptyOptions().setTimeout(1000));
    });
    assertNull(e.getExpected());
    assertNull(e.getActual());
    assertTrue(e.getMessage().contains("Locator expected not to be empty"), e.getMessage());
  }

  @PlaywrightTest
  void isEnabledPass(Page page) {
    page.setContent("<button>Text</button>");
    Locator locator = page.locator("button");
    assertThat(locator).isEnabled();
  }

  @PlaywrightTest
  void isEnabledFail(Page page) {
    page.setContent("<button disabled>Text</button>");
    Locator locator = page.locator("button");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).isEnabled(new LocatorAssertions.IsEnabledOptions().setTimeout(1000));
    });
    assertNull(e.getExpected());
    assertNull(e.getActual());
    assertTrue(e.getMessage().contains("Locator expected to be enabled"), e.getMessage());
  }

  @PlaywrightTest
  void notIsEnabledFail(Page page) {
    page.setContent("<button>Text</button>");
    Locator locator = page.locator("button");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).not().isEnabled(new LocatorAssertions.IsEnabledOptions().setTimeout(1000));
    });
    assertNull(e.getExpected());
    assertNull(e.getActual());
    assertTrue(e.getMessage().contains("Locator expected not to be enabled"), e.getMessage());
  }

  @PlaywrightTest
  void isEnabledTrue(Page page) {
    page.setContent("<button>Text</button>");
    Locator locator = page.locator("button");
    assertThat(locator).isEnabled(new LocatorAssertions.IsEnabledOptions().setEnabled(true));
  }

  @PlaywrightTest
  void isEnabledFalse(Page page) {
    page.setContent("<button disabled>Text</button>");
    Locator locator = page.locator("button");
    assertThat(locator).isEnabled(new LocatorAssertions.IsEnabledOptions().setEnabled(false));
  }

  @PlaywrightTest
  void isEnabledFalseFail(Page page) {
    page.setContent("<button>Text</button>");
    Locator locator = page.locator("button");
    AssertionFailedError error = assertThrows(AssertionFailedError.class,
      () -> assertThat(locator).isEnabled(new LocatorAssertions.IsEnabledOptions().setEnabled(false).setTimeout(1000)));
    assertTrue(error.getMessage().contains("Locator expected to be disabled"), error.getMessage());
  }

  @PlaywrightTest
  void isEnabledEventually(Page page) {
    page.setContent("<button disabled>Text</button>");
    Locator locator = page.locator("button");
    locator.evaluate("e => setTimeout(() => {\n" +
      "  e.removeAttribute('disabled');\n" +
      "}, 500);\n");
    assertThat(locator).isEnabled();
  }

  @PlaywrightTest
  void isEnabledEventuallyWithNot(Page page) {
    page.setContent("<button>Text</button>");
    Locator locator = page.locator("button");
    locator.evaluate("e => setTimeout(() => {\n" +
      "  e.setAttribute('disabled', '');\n" +
      "}, 500);\n");
    assertThat(locator).not().isEnabled();
  }

  @PlaywrightTest
  void isEnabledWithNotAndEnabledFalse(Page page) {
    page.setContent("<button>Text</button>");
    Locator locator = page.locator("button");
    assertThat(locator).not().isEnabled(new LocatorAssertions.IsEnabledOptions().setEnabled(false));
  }

  @PlaywrightTest
  void isFocusedPass(Page page) {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    locator.focus();
    assertThat(locator).isFocused();
  }

  @PlaywrightTest
  void isFocusedFail(Page page) {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).isFocused(new LocatorAssertions.IsFocusedOptions().setTimeout(1000));
    });
    assertNull(e.getExpected());
    assertNull(e.getActual());
    assertTrue(e.getMessage().contains("Locator expected to be focused"), e.getMessage());
  }

  @PlaywrightTest
  void notIsFocusedFail(Page page) {
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

  @PlaywrightTest
  void isHiddenPass(Page page) {
    page.setContent("<button style='display: none'></button>");
    Locator locator = page.locator("button");
    assertThat(locator).isHidden();
  }

  @PlaywrightTest
  void isHiddenFail(Page page) {
    page.setContent("<button></button>");
    Locator locator = page.locator("button");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).isHidden(new LocatorAssertions.IsHiddenOptions().setTimeout(1000));
    });
    assertNull(e.getExpected());
    assertNull(e.getActual());
    assertTrue(e.getMessage().contains("Locator expected to be hidden"), e.getMessage());
  }

  @PlaywrightTest
  void notIsHiddenFail(Page page) {
    page.setContent("<button style='display: none'></button>");
    Locator locator = page.locator("button");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).not().isHidden(new LocatorAssertions.IsHiddenOptions().setTimeout(1000));
    });
    assertNull(e.getExpected());
    assertNull(e.getActual());
    assertTrue(e.getMessage().contains("Locator expected not to be hidden"), e.getMessage());
  }

  @PlaywrightTest
  void isVisiblePass(Page page) {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    assertThat(locator).isVisible();
  }

  @PlaywrightTest
  void isVisibleFail(Page page) {
    page.setContent("<input style='display: none'></input>");
    Locator locator = page.locator("input");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(1000));
    });
    assertNull(e.getExpected());
    assertNull(e.getActual());
    assertTrue(e.getMessage().contains("Locator expected to be visible"), e.getMessage());
  }

  @PlaywrightTest
  void isVisibleFalseFail(Page page) {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    AssertionFailedError error = assertThrows(AssertionFailedError.class,
      () -> assertThat(locator).isVisible(new LocatorAssertions.IsVisibleOptions().setVisible(false).setTimeout(1000)));
    assertTrue(error.getMessage().contains("Locator expected to be hidden"), error.getMessage());
  }

  @PlaywrightTest
  void notIsVisibleFail(Page page) {
    page.setContent("<input></input>");
    Locator locator = page.locator("input");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(locator).not().isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(1000));
    });
    assertNull(e.getExpected());
    assertNull(e.getActual());
    assertTrue(e.getMessage().contains("Locator expected not to be visible"), e.getMessage());
  }

  @PlaywrightTest
  void isVisibleWithTrue(Page page) {
    page.setContent("<button>hello</button>");
    Locator locator = page.locator("button");
    assertThat(locator).isVisible(new LocatorAssertions.IsVisibleOptions().setVisible(true));
  }

  @PlaywrightTest
  void isVisibleWithFalse(Page page) {
    page.setContent("<button hidden>hello</button>");
    Locator locator = page.locator("button");
    assertThat(locator).isVisible(new LocatorAssertions.IsVisibleOptions().setVisible(false));
  }

  @PlaywrightTest
  void isVisibleWithNotAndFalse(Page page) {
    page.setContent("<button>hello</button>");
    Locator locator = page.locator("button");
    assertThat(locator).not().isVisible(new LocatorAssertions.IsVisibleOptions().setVisible(false));
  }

  @PlaywrightTest
  void isVisibleEventually(Page page) {
    page.setContent("<div></div>");
    Locator locator = page.locator("span");
    page.evalOnSelector("div", "div => setTimeout(() => {\n" +
      "      div.innerHTML = '<span>Hello</span>';\n" +
      "    }, 10);");
    assertThat(locator).isVisible();
  }

  @PlaywrightTest
  void isVisibleEventuallyWithNot(Page page) {
    page.setContent("<div><span>Hello</span></div>");
    Locator locator = page.locator("span");
    page.evalOnSelector("span", "span => setTimeout(() => {\n" +
      "      span.textContent = '';\n" +
      "    }, 10);");
    assertThat(locator).not().isVisible();
  }

  @PlaywrightTest
  void locatorCountShouldWorkWithDeletedMapInMainWorld(Page page) {
    page.evaluate("Map = 1");
    page.locator("#searchResultTableDiv .x-grid3-row").count();
    assertThat(page.locator("#searchResultTableDiv .x-grid3-row")).hasCount(0);
  }

  @PlaywrightTest
  void defaultTimeoutHasTextFail(Page page) {
    page.setContent("<div></div>");
    Locator locator = page.locator("div");
    PlaywrightAssertions.setDefaultAssertionTimeout(1000);
    AssertionFailedError exception = assertThrows(AssertionFailedError.class, () -> assertThat(locator).hasText("foo"));
    assertTrue(exception.getMessage().contains("Assert \"hasText\" locator(\"div\") with timeout 1000ms"), exception.getMessage());
    // Restore default.
    PlaywrightAssertions.setDefaultAssertionTimeout(5_000);
  }

  @PlaywrightTest
  void defaultTimeoutHasTextPass(Page page) {
    page.setContent("<div>foo</div>");
    Locator locator = page.locator("div");
    PlaywrightAssertions.setDefaultAssertionTimeout(1000);
    assertThat(locator).hasText("foo");
    // Restore default.
    PlaywrightAssertions.setDefaultAssertionTimeout(5_000);
  }

  @PlaywrightTest
  void defaultTimeoutZeroHasTextPass(Page page) {
    page.setContent("<div>foo</div>");
    Locator locator = page.locator("div");
    PlaywrightAssertions.setDefaultAssertionTimeout(0);
    assertThat(locator).hasText("foo");
    // Restore default.
    PlaywrightAssertions.setDefaultAssertionTimeout(5_000);
  }

  @PlaywrightTest
  void containsClassPass(Page page) {
    page.setContent("<div class='foo bar baz'></div>");
    Locator locator = page.locator("div");
    assertThat(locator).containsClass("");
    assertThat(locator).containsClass("bar");
    assertThat(locator).containsClass("baz bar");
    assertThat(locator).containsClass("  bar   foo ");
    assertThat(locator).not().containsClass("  baz not-matching");
  }

  @PlaywrightTest
  void containsClassPassWithSvgs(Page page) {
    page.setContent("<svg class='c1 c2' role='img' xmlns='http://www.w3.org/2000/svg' viewBox='0 0 512 512'></svg>");
    assertThat(page.locator("svg")).containsClass("c1");
    assertThat(page.locator("svg")).containsClass("c2 c1");
  }

  @PlaywrightTest
  void containsClassFail(Page page) {
    page.setContent("<div class='bar baz'></div>");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(page.locator("div")).containsClass("does-not-exist", new ContainsClassOptions().setTimeout(1000));
    });
    assertTrue(e.getMessage().contains("Assert \"containsClass\" locator(\"div\") with timeout 1000ms"), e.getMessage());
  }

  @PlaywrightTest
  void containsClassPassWithArray(Page page) {
    page.setContent("<div class='foo'></div><div class='hello bar'></div><div class='baz'></div>");
    Locator locator = page.locator("div");
    assertThat(locator).containsClass(asList("foo", "hello", "baz"));
    assertThat(locator).not().hasClass(new String[]{"not-there", "hello", "baz"}); // Class not there
    assertThat(locator).not().hasClass(new String[]{"foo", "hello"}); // length mismatch
  }

  @PlaywrightTest
  void containsClassFailWithArray(Page page) {
    page.setContent("<div class='foo'></div><div class='bar'></div><div class='bar'></div>");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> {
      assertThat(page.locator("div")).containsClass(asList("foo", "bar", "baz"), new ContainsClassOptions().setTimeout(1000));
    });
    assertTrue(e.getMessage().contains("Assert \"containsClass\" locator(\"div\") with timeout 1000ms"), e.getMessage());
  }
}
