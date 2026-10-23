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

import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.options.AriaRole;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class TestPageLocatorQuery {
  @PlaywrightTest
  void shouldRespectFirstAndLast(Page page) {
    page.setContent("<section>\n" +
      "    <div><p>A</p></div>\n" +
      "    <div><p>A</p><p>A</p></div>\n" +
      "    <div><p>A</p><p>A</p><p>A</p></div>\n" +
      "  </section>");
    assertEquals(6, page.locator("div >> p").count());
    assertEquals(6, page.locator("div").locator("p").count());
    assertEquals(1, page.locator("div").first().locator("p").count());
    assertEquals(3, page.locator("div").last().locator("p").count());
  }

  @PlaywrightTest
  void shouldRespectNth(Page page) {
    page.setContent("<section>\n" +
      "    <div><p>A</p></div>\n" +
      "    <div><p>A</p><p>A</p></div>\n" +
      "    <div><p>A</p><p>A</p><p>A</p></div>\n" +
      "  </section>");
    assertEquals(1, page.locator("div >> p").nth(0).count());
    assertEquals(2, page.locator("div").nth(1).locator("p").count());
    assertEquals(3, page.locator("div").nth(2).locator("p").count());
  }

  @PlaywrightTest
  void shouldThrowOnCaptureWNth(Page page) {
    page.setContent("<section><div><p>A</p></div></section>");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.locator("*css=div >> p").nth(1).click();
    });
    assertTrue(e.getMessage().contains("Can't query n-th element"), e.getMessage());
  }

  @PlaywrightTest
  void shouldThrowOnDueToStrictness(Page page) {
    page.setContent("<div>A</div><div>B</div>");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.locator("div").isVisible();
    });
    assertTrue(e.getMessage().contains("strict mode violation"), e.getMessage());
  }

  @PlaywrightTest
  void shouldThrowOnDueToStrictness2(Page page) {
    page.setContent("<select><option>One</option><option>Two</option></select>");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.locator("option").evaluate("e => {}");
    });
    assertTrue(e.getMessage().contains("strict mode violation"), e.getMessage());
  }

  @PlaywrightTest
  void shouldFilterByText(Page page) {
    page.setContent("<div>Foobar</div><div>Bar</div>");
    assertEquals("Foobar", page.locator("div", new Page.LocatorOptions().setHasText("Foo")).textContent());
  }

  @PlaywrightTest
  void shouldFilterByText2(Page page) {
    page.setContent("<div>foo <span>hello world</span> bar</div>");
    assertEquals("foo hello world bar", page.locator("div", new Page.LocatorOptions().setHasText("hello world")).textContent());
  }

  @PlaywrightTest
  void shouldFilterByRegex(Page page) {
    page.setContent("<div>Foobar</div><div>Bar</div>");
    assertEquals("Foobar", page.locator("div", new Page.LocatorOptions().setHasText(Pattern.compile("Foo.*"))).textContent());
  }

  @PlaywrightTest
  void shouldFilterByTextWithQuotes(Page page) {
    page.setContent("<div>Hello \"world\"</div><div>Hello world</div>");
    assertEquals("Hello \"world\"", page.locator("div", new Page.LocatorOptions().setHasText("Hello \"world\"")).textContent());
  }

  @PlaywrightTest
  void shouldFilterByRegexWithQuotes(Page page) {
    page.setContent("<div>Hello \"world\"</div><div>Hello world</div>");
    assertEquals("Hello \"world\"", page.locator("div", new Page.LocatorOptions().setHasText(Pattern.compile("Hello \"world\""))).textContent());
  }

  @PlaywrightTest
  void shouldFilterByRegexWithASingleQuote(Page page) {
    page.setContent("<button>let's let's<span>hello</span></button>");
    assertThat(page.locator("button", new Page.LocatorOptions().setHasText(Pattern.compile("let's", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("let's", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.locator("button", new Page.LocatorOptions().setHasText(Pattern.compile("let\'s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.getByRole(AriaRole.BUTTON,  new Page.GetByRoleOptions().setName(Pattern.compile("let\'s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.locator("button", new Page.LocatorOptions().setHasText(Pattern.compile("'s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.getByRole(AriaRole.BUTTON,  new Page.GetByRoleOptions().setName(Pattern.compile("'s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.locator("button", new Page.LocatorOptions().setHasText(Pattern.compile("\'s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.getByRole(AriaRole.BUTTON,  new Page.GetByRoleOptions().setName(Pattern.compile("\'s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.locator("button", new Page.LocatorOptions().setHasText(Pattern.compile("let['abc]s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.getByRole(AriaRole.BUTTON,  new Page.GetByRoleOptions().setName(Pattern.compile("let['abc]s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.locator("button", new Page.LocatorOptions().setHasText(Pattern.compile("let\\\\'s", Pattern.CASE_INSENSITIVE)))).not().isVisible();
    assertThat(page.getByRole(AriaRole.BUTTON,  new Page.GetByRoleOptions().setName(Pattern.compile("let\\\\'s", Pattern.CASE_INSENSITIVE)))).not().isVisible();
    assertThat(page.locator("button", new Page.LocatorOptions().setHasText(Pattern.compile("let's let\\'s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.getByRole(AriaRole.BUTTON,  new Page.GetByRoleOptions().setName(Pattern.compile("let's let\\'s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.locator("button", new Page.LocatorOptions().setHasText(Pattern.compile("let\\'s let's", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.getByRole(AriaRole.BUTTON,  new Page.GetByRoleOptions().setName(Pattern.compile("let\\'s let's", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");

    page.setContent("<button>let\\'s let\\'s<span>hello</span></button>");
    assertThat(page.locator("button", new Page.LocatorOptions().setHasText(Pattern.compile("let\\'s", Pattern.CASE_INSENSITIVE)))).not().isVisible();
    assertThat(page.getByRole(AriaRole.BUTTON,  new Page.GetByRoleOptions().setName(Pattern.compile("let\\'s", Pattern.CASE_INSENSITIVE)))).not().isVisible();
    assertThat(page.locator("button", new Page.LocatorOptions().setHasText(Pattern.compile("let\\\\'s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.getByRole(AriaRole.BUTTON,  new Page.GetByRoleOptions().setName(Pattern.compile("let\\\\'s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.locator("button", new Page.LocatorOptions().setHasText(Pattern.compile("let\\\\\\'s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.getByRole(AriaRole.BUTTON,  new Page.GetByRoleOptions().setName(Pattern.compile("let\\\\\\'s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.locator("button", new Page.LocatorOptions().setHasText(Pattern.compile("let\\\\'s let\\\\\\'s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.getByRole(AriaRole.BUTTON,  new Page.GetByRoleOptions().setName(Pattern.compile("let\\\\'s let\\\\\\'s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.locator("button", new Page.LocatorOptions().setHasText(Pattern.compile("let\\\\\\'s let\\\\'s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.getByRole(AriaRole.BUTTON,  new Page.GetByRoleOptions().setName(Pattern.compile("let\\\\\\'s let\\\\'s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
  }

  @PlaywrightTest
  void shouldFilterByRegexAndRegexpFlags(Page page) {
    page.setContent("<div>Hello \"world\"</div><div>Hello world</div>");
    Pattern pattern = Pattern.compile("hElLo \"world\"", Pattern.CASE_INSENSITIVE);
    assertEquals("Hello \"world\"", page.locator("div", new Page.LocatorOptions().setHasText(pattern)).textContent());
  }

  @PlaywrightTest
  void shouldFilterByCaseInsensitiveRegexInAChild(Page page) {
    page.setContent("<div class=\"test\"><h5>Title Text</h5></div>");
    Pattern pattern = Pattern.compile("^title text$", Pattern.CASE_INSENSITIVE);
    assertThat(page.locator("div", new Page.LocatorOptions().setHasText(pattern))).hasText("Title Text");
  }

  @PlaywrightTest
  void shouldFilterByCaseInsensitiveRegexInMultipleChildren(Page page) {
    page.setContent("<div class=\"test\"><h5>Title</h5> <h2><i>Text</i></h2></div>`");
    Pattern pattern = Pattern.compile("^title text$", Pattern.CASE_INSENSITIVE);
    assertThat(page.locator("div", new Page.LocatorOptions().setHasText(pattern))).hasClass("test");
  }

  @PlaywrightTest
  void shouldFilterByRegexWithSpecialSymbols(Page page) {
    page.setContent("<div class=\"test\"><h5>First/\"and\"</h5><h2><i>Second\\</i></h2></div>");
    Pattern pattern = Pattern.compile("first\\/\".*\"second\\\\$", Pattern.CASE_INSENSITIVE);
    assertThat(page.locator("div", new Page.LocatorOptions().setHasText(pattern))).hasClass("test");
  }
  @PlaywrightTest
  void shouldFilterByTextWithAmpersand(Page page) {
    page.setContent("<div>Save & Continue</div>");
    assertEquals("Save & Continue", page.locator("div",
      new Page.LocatorOptions().setHasText("Save & Continue")).textContent());
  }

  private static String removeHighlight(String markup) {
    return markup.replaceAll("\\s__playwright_target__=\"[^\"]+\"", "");
  }
  @PlaywrightTest
  void shouldSupportHasLocator(Page page) {
    page.setContent("<div><span>hello</span></div><div><span>world</span></div>");
    assertThat(page.locator("div", new Page.LocatorOptions().setHas(page.locator("text=world")))).hasCount(1);
    assertEquals("<div><span>world</span></div>", removeHighlight((String) page.locator("div", new Page.LocatorOptions().setHas(page.locator("text=world"))).evaluate("e => e.outerHTML")));
    assertThat(page.locator("div", new Page.LocatorOptions().setHas(page.locator("text='hello'")))).hasCount(1);
    assertEquals("<div><span>hello</span></div>", removeHighlight((String) page.locator("div", new Page.LocatorOptions().setHas(page.locator("text='hello'"))).evaluate("e => e.outerHTML")));
    assertThat(page.locator("div", new Page.LocatorOptions().setHas(page.locator("xpath=./span")))).hasCount(2);
    assertThat(page.locator("div", new Page.LocatorOptions().setHas(page.locator("span")))).hasCount(2);
    assertThat(page.locator("div", new Page.LocatorOptions().setHas(page.locator("span", new Page.LocatorOptions().setHasText("wor"))))).hasCount(1);
    assertEquals("<div><span>world</span></div>", removeHighlight((String) page.locator("div", new Page.LocatorOptions().setHas(
      page.locator("span", new Page.LocatorOptions().setHasText("wor")))).evaluate("e => e.outerHTML")));
    assertThat(page.locator("div", new Page.LocatorOptions()
        .setHas(page.locator("span")).setHasText("wor"))).hasCount(1);
  }

  @PlaywrightTest
  void shouldSupportLocatorFilter(Page page) {
    page.setContent("<section><div><span>hello</span></div><div><span>world</span></div></section>");
    assertThat(page.locator("div").filter(new Locator.FilterOptions().setHasText("hello"))).hasCount(1);
    assertThat(page.locator("div", new Page.LocatorOptions().setHasText("hello")).filter(new Locator.FilterOptions().setHasText("hello"))).hasCount(1);
    assertThat(page.locator("div", new Page.LocatorOptions().setHasText("hello")).filter(new Locator.FilterOptions().setHasText("world"))).hasCount(0);
    assertThat(page.locator("section", new Page.LocatorOptions().setHasText("hello")).filter(new Locator.FilterOptions().setHasText("world"))).hasCount(1);
    assertThat(page.locator("div").filter(new Locator.FilterOptions().setHasText("hello")).locator("span")).hasCount(1);
    assertThat(page.locator("div").filter(new Locator.FilterOptions().setHas(page.locator("span", new Page.LocatorOptions().setHasText("world"))))).hasCount(1);
    assertThat(page.locator("div").filter(new Locator.FilterOptions().setHas(page.locator("span")))).hasCount(2);
    assertThat(page.locator("div").filter(new Locator.FilterOptions()
      .setHas(page.locator("span"))
      .setHasText("world"))).hasCount(1);
    assertThat(page.locator("div").filter(new Locator.FilterOptions()
      .setHasNot(page.locator("span", new Page.LocatorOptions().setHasText("world"))))).hasCount(1);
    assertThat(page.locator("div").filter(new Locator.FilterOptions()
      .setHasNot(page.locator("section")))).hasCount(2);
    assertThat(page.locator("div").filter(new Locator.FilterOptions()
      .setHasNot(page.locator("span")))).hasCount(0);
    assertThat(page.locator("div").filter(new Locator.FilterOptions().setHasNotText("hello"))).hasCount(1);
    assertThat(page.locator("div").filter(new Locator.FilterOptions().setHasNotText("foo"))).hasCount(2);
  }


  @PlaywrightTest
  void shouldSupportLocatorAnd(Page page) {
    page.setContent("<div data-testid=foo>hello</div><div data-testid=bar>world</div>\n" +
      "    <span data-testid=foo>hello2</span><span data-testid=bar>world2</span>");
    assertThat(page.locator("div").and(page.locator("div"))).hasCount(2);
    assertThat(page.locator("div").and(page.getByTestId("foo"))).hasText(new String[] { "hello" });
    assertThat(page.locator("div").and(page.getByTestId("bar"))).hasText(new String[] { "world" });
    assertThat(page.getByTestId("foo").and(page.locator("div"))).hasText(new String[] { "hello" });
    assertThat(page.getByTestId("bar").and(page.locator("span"))).hasText(new String[] { "world2" });
    assertThat(page.locator("span").and(page.getByTestId(Pattern.compile("bar|foo")))).hasCount(2);
  }

  @PlaywrightTest
  void shouldSupportLocatorOr(Page page) {
    page.setContent("<div>hello</div><span>world</span>");
    assertThat(page.locator("div").or(page.locator("span"))).hasCount(2);
    assertThat(page.locator("div").or(page.locator("span"))).hasText(new String[]{"hello", "world"});
    assertThat(page.locator("span").or(page.locator("article")).or(page.locator("div"))).hasText(new String[]{"hello", "world"});
    assertThat(page.locator("article").or(page.locator("someting"))).hasCount(0);
    assertThat(page.locator("article").or(page.locator("div"))).hasText("hello");
    assertThat(page.locator("article").or(page.locator("span"))).hasText("world");
    assertThat(page.locator("div").or(page.locator("article"))).hasText("hello");
    assertThat(page.locator("span").or(page.locator("article"))).hasText("world");
  }

  @PlaywrightTest
  void shouldSupportLocatorLocatorWithAndOr(Page page) {
    page.setContent("\n" +
      "    <div>one <span>two</span> <button>three</button> </div>\n" +
      "    <span>four</span>\n" +
      "    <button>five</button>\n" +
      "  ");

    assertThat(page.locator("div").locator(page.locator("button"))).hasText(new String[] {"three"});
    assertThat(page.locator("div").locator(page.locator("button").or(page.locator("span")))).hasText(new String[]{"two", "three"});
    assertThat(page.locator("button").or(page.locator("span"))).hasText(new String[]{"two", "three", "four", "five"});

    assertThat(page.locator("div").locator(page.locator("button").and(page.getByRole(AriaRole.BUTTON)))).hasText(new String[]{"three"});
    assertThat(page.locator("button").and(page.getByRole(AriaRole.BUTTON))).hasText(new String[]{"three", "five"});
  }

  @PlaywrightTest
  void shouldSupportLocatorWithin(Page page) {
    page.setContent("\n" +
      "    <table>\n" +
      "      <tr><td>a1</td><td>a2</td><td>a3</td></tr>\n" +
      "      <tr><td>b1</td><td>b2</td><td>b3</td></tr>\n" +
      "      <tr><td>c1</td><td>c2</td><td>c3</td></tr>\n" +
      "    </table>\n" +
      "    <span>outside</span>\n" +
      "  ");

    assertThat(page.getByRole(AriaRole.CELL).within(page.getByRole(AriaRole.ROW))).hasText(new String[]{"a1", "a2", "a3", "b1", "b2", "b3", "c1", "c2", "c3"});
    assertThat(page.getByRole(AriaRole.CELL).nth(1).within(page.getByRole(AriaRole.ROW))).hasText(new String[]{"a2", "b2", "c2"});
    assertThat(page.getByRole(AriaRole.CELL).last().within(page.getByRole(AriaRole.ROW))).hasText(new String[]{"a3", "b3", "c3"});
    assertThat(page.getByRole(AriaRole.CELL).nth(1).within(page.getByRole(AriaRole.ROW).nth(2))).hasText(new String[]{"c2"});
    assertThat(page.locator("span").within(page.getByRole(AriaRole.ROW))).hasCount(0);
    assertThat(page.getByRole(AriaRole.CELL).nth(1).within(page.getByRole(AriaRole.ROW)).nth(1)).hasText("b2");
  }

}
