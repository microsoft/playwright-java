/*
 * Copyright 2018 Google Inc. All rights reserved.
 * Modifications copyright (c) Microsoft Corporation.
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

import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.DisplayName;
import org.opentest4j.AssertionFailedError;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/locator-query.spec.ts. */
public class TestLocatorQuery {
  @PlaywrightTest
  @DisplayName("LO-157 should respect first() and last() @smoke")
  void shouldRespectFirstAndLast(Page page) {
    page.setContent("\n" +
      "  <section>\n" +
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
  @DisplayName("LO-158 should respect nth()")
  void shouldRespectNth(Page page) {
    page.setContent("\n" +
      "  <section>\n" +
      "    <div><p>A</p></div>\n" +
      "    <div><p>A</p><p>A</p></div>\n" +
      "    <div><p>A</p><p>A</p><p>A</p></div>\n" +
      "  </section>");
    assertEquals(1, page.locator("div >> p").nth(0).count());
    assertEquals(2, page.locator("div").nth(1).locator("p").count());
    assertEquals(3, page.locator("div").nth(2).locator("p").count());
  }

  @PlaywrightTest
  @DisplayName("LO-159 should throw on capture w/ nth()")
  void shouldThrowOnCaptureWNth(Page page) {
    page.setContent("<section><div><p>A</p></div></section>");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.locator("*css=div >> p").nth(1).click());
    assertTrue(e.getMessage().contains("Can't query n-th element"));
  }

  @PlaywrightTest
  @DisplayName("LO-160 should throw on due to strictness")
  void shouldThrowOnDueToStrictness(Page page) {
    page.setContent("<div>A</div><div>B</div>");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.locator("div").isVisible());
    assertTrue(e.getMessage().contains("strict mode violation"));
  }

  @PlaywrightTest
  @DisplayName("LO-161 should throw on due to strictness 2")
  void shouldThrowOnDueToStrictness2(Page page) {
    page.setContent("<select><option>One</option><option>Two</option></select>");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.locator("option").evaluate("e => {}"));
    assertTrue(e.getMessage().contains("strict mode violation"));
  }

  @PlaywrightTest
  @DisplayName("LO-162 should filter by text")
  void shouldFilterByText(Page page) {
    page.setContent("<div>Foobar</div><div>Bar</div>");
    assertThat(page.locator("div", new Page.LocatorOptions().setHasText("Foo"))).hasText("Foobar");
  }

  @PlaywrightTest
  @DisplayName("LO-163 should filter by text 2")
  void shouldFilterByText2(Page page) {
    page.setContent("<div>foo <span>hello world</span> bar</div>");
    assertThat(page.locator("div", new Page.LocatorOptions().setHasText("hello world"))).hasText("foo hello world bar");
  }

  @PlaywrightTest
  @DisplayName("LO-164 should filter by regex")
  void shouldFilterByRegex(Page page) {
    page.setContent("<div>Foobar</div><div>Bar</div>");
    assertThat(page.locator("div", new Page.LocatorOptions().setHasText(Pattern.compile("Foo.*")))).hasText("Foobar");
  }

  @PlaywrightTest
  @DisplayName("LO-165 should filter by text with quotes")
  void shouldFilterByTextWithQuotes(Page page) {
    page.setContent("<div>Hello \"world\"</div><div>Hello world</div>");
    assertThat(page.locator("div", new Page.LocatorOptions().setHasText("Hello \"world\""))).hasText("Hello \"world\"");
  }

  @PlaywrightTest
  @DisplayName("LO-166 should filter by regex with quotes")
  void shouldFilterByRegexWithQuotes(Page page) {
    page.setContent("<div>Hello \"world\"</div><div>Hello world</div>");
    assertThat(page.locator("div", new Page.LocatorOptions().setHasText(Pattern.compile("Hello \"world\"")))).hasText("Hello \"world\"");
  }

  @PlaywrightTest
  @DisplayName("LO-167 should filter by regex with a single quote")
  @Fixme("java: the 'u' flag has no Pattern flag the client sends: 'Unexpected RegEx flag, only CASE_INSENSITIVE, DOTALL and MULTILINE are supported.'")
  void shouldFilterByRegexWithASingleQuote(Page page) {
    page.setContent("<button>let's let's<span>hello</span></button>");
    // Java: expect.soft is a plain assertion, the first failure ends the test.
    assertThat(page.locator("button", new Page.LocatorOptions().setHasText(Pattern.compile("let's", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("let's", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.locator("button", new Page.LocatorOptions().setHasText(Pattern.compile("let\\'s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("let\\'s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.locator("button", new Page.LocatorOptions().setHasText(Pattern.compile("'s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("'s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.locator("button", new Page.LocatorOptions().setHasText(Pattern.compile("\\'s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("\\'s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.locator("button", new Page.LocatorOptions().setHasText(Pattern.compile("let['abc]s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("let['abc]s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.locator("button", new Page.LocatorOptions().setHasText(Pattern.compile("let\\\\'s", Pattern.CASE_INSENSITIVE)))).not().isVisible();
    assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("let\\\\'s", Pattern.CASE_INSENSITIVE)))).not().isVisible();
    assertThat(page.locator("button", new Page.LocatorOptions().setHasText(Pattern.compile("let's let\\'s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("let's let\\'s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.locator("button", new Page.LocatorOptions().setHasText(Pattern.compile("let\\'s let's", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("let\\'s let's", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");

    page.setContent("<button>let\\'s let\\'s<span>hello</span></button>");
    assertThat(page.locator("button", new Page.LocatorOptions().setHasText(Pattern.compile("let\\'s", Pattern.CASE_INSENSITIVE)))).not().isVisible();
    assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("let\\'s", Pattern.CASE_INSENSITIVE)))).not().isVisible();
    assertThat(page.locator("button", new Page.LocatorOptions().setHasText(Pattern.compile("let\\\\'s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("let\\\\'s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.locator("button", new Page.LocatorOptions().setHasText(Pattern.compile("let\\\\\\'s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("let\\\\\\'s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.locator("button", new Page.LocatorOptions().setHasText(Pattern.compile("let\\\\'s let\\\\\\'s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("let\\\\'s let\\\\\\'s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.locator("button", new Page.LocatorOptions().setHasText(Pattern.compile("let\\\\\\'s let\\\\'s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");
    assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("let\\\\\\'s let\\\\'s", Pattern.CASE_INSENSITIVE))).locator("span")).hasText("hello");

    page.setContent("<button>let's hello</button>");
    // Java: the 'u' flag is Pattern.UNICODE_CASE.
    assertThat(page.locator("button", new Page.LocatorOptions().setHasText(Pattern.compile("let's", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)))).hasText("let's hello");
    assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("let's", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE)))).hasText("let's hello");
  }

  @PlaywrightTest
  @DisplayName("LO-168 should filter by regex and regexp flags")
  void shouldFilterByRegexAndRegexpFlags(Page page) {
    page.setContent("<div>Hello \"world\"</div><div>Hello world</div>");
    assertThat(page.locator("div", new Page.LocatorOptions().setHasText(Pattern.compile("hElLo \"world\"", Pattern.CASE_INSENSITIVE)))).hasText("Hello \"world\"");
  }

  @PlaywrightTest
  @DisplayName("LO-169 should filter by case-insensitive regex in a child")
  void shouldFilterByCaseInsensitiveRegexInAChild(Page page) {
    page.setContent("<div class=\"test\"><h5>Title Text</h5></div>");
    assertThat(page.locator("div", new Page.LocatorOptions().setHasText(Pattern.compile("^title text$", Pattern.CASE_INSENSITIVE)))).hasText("Title Text");
  }

  @PlaywrightTest
  @DisplayName("LO-170 should filter by case-insensitive regex in multiple children")
  void shouldFilterByCaseInsensitiveRegexInMultipleChildren(Page page) {
    page.setContent("<div class=\"test\"><h5>Title</h5> <h2><i>Text</i></h2></div>");
    assertThat(page.locator("div", new Page.LocatorOptions().setHasText(Pattern.compile("^title text$", Pattern.CASE_INSENSITIVE)))).hasClass("test");
  }

  @PlaywrightTest
  @DisplayName("LO-171 should filter by regex with special symbols")
  void shouldFilterByRegexWithSpecialSymbols(Page page) {
    page.setContent("<div class=\"test\"><h5>First/\"and\"</h5><h2><i>Second\\</i></h2></div>");
    assertThat(page.locator("div", new Page.LocatorOptions().setHasText(Pattern.compile("^first\\/\".*\"second\\\\$", Pattern.DOTALL | Pattern.CASE_INSENSITIVE)))).hasClass("test");
  }

  @PlaywrightTest
  @DisplayName("LO-172 should support has:locator")
  void shouldSupportHasLocator(Page page) {
    page.setContent("<div><span>hello</span></div><div><span>world</span></div>");
    assertThat(page.locator("div", new Page.LocatorOptions()
      .setHas(page.locator("text=world")))).hasCount(1);
    assertEquals("<div><span>world</span></div>", page.locator("div", new Page.LocatorOptions()
      .setHas(page.locator("text=world"))).evaluate("e => e.outerHTML"));
    assertThat(page.locator("div", new Page.LocatorOptions()
      .setHas(page.locator("text=\"hello\"")))).hasCount(1);
    assertEquals("<div><span>hello</span></div>", page.locator("div", new Page.LocatorOptions()
      .setHas(page.locator("text=\"hello\""))).evaluate("e => e.outerHTML"));
    assertThat(page.locator("div", new Page.LocatorOptions()
      .setHas(page.locator("xpath=./span")))).hasCount(2);
    assertThat(page.locator("div", new Page.LocatorOptions()
      .setHas(page.locator("span")))).hasCount(2);
    assertThat(page.locator("div", new Page.LocatorOptions()
      .setHas(page.locator("span", new Page.LocatorOptions().setHasText("wor"))))).hasCount(1);
    assertEquals("<div><span>world</span></div>", page.locator("div", new Page.LocatorOptions()
      .setHas(page.locator("span", new Page.LocatorOptions().setHasText("wor")))).evaluate("e => e.outerHTML"));
    assertThat(page.locator("div", new Page.LocatorOptions()
      .setHas(page.locator("span"))
      .setHasText("wor"))).hasCount(1);
  }

  @PlaywrightTest
  @DisplayName("LO-173 should support locator.filter")
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
    assertThat(page.locator("div").filter(new Locator.FilterOptions().setHasNot(page.locator("span", new Page.LocatorOptions().setHasText("world"))))).hasCount(1);
    assertThat(page.locator("div").filter(new Locator.FilterOptions().setHasNot(page.locator("section")))).hasCount(2);
    assertThat(page.locator("div").filter(new Locator.FilterOptions().setHasNot(page.locator("span")))).hasCount(0);
    assertThat(page.locator("div").filter(new Locator.FilterOptions().setHasNotText("hello"))).hasCount(1);
    assertThat(page.locator("div").filter(new Locator.FilterOptions().setHasNotText("foo"))).hasCount(2);
  }

  @PlaywrightTest
  @DisplayName("LO-174 should support locator.and")
  void shouldSupportLocatorAnd(Page page) {
    page.setContent("\n" +
      "    <div data-testid=foo>hello</div><div data-testid=bar>world</div>\n" +
      "    <span data-testid=foo>hello2</span><span data-testid=bar>world2</span>\n" +
      "  ");
    assertThat(page.locator("div").and(page.locator("div"))).hasCount(2);
    assertThat(page.locator("div").and(page.getByTestId("foo"))).hasText(new String[]{"hello"});
    assertThat(page.locator("div").and(page.getByTestId("bar"))).hasText(new String[]{"world"});
    assertThat(page.getByTestId("foo").and(page.locator("div"))).hasText(new String[]{"hello"});
    assertThat(page.getByTestId("bar").and(page.locator("span"))).hasText(new String[]{"world2"});
    assertThat(page.locator("span").and(page.getByTestId(Pattern.compile("bar|foo")))).hasCount(2);
  }

  @PlaywrightTest
  @DisplayName("LO-175 should support locator.or")
  void shouldSupportLocatorOr(Page page) {
    page.setContent("<div>hello</div><span>world</span>");
    assertThat(page.locator("div").or(page.locator("span"))).hasCount(2);
    assertThat(page.locator("div").or(page.locator("span"))).hasText(new String[]{"hello", "world"});
    assertThat(page.locator("span").or(page.locator("article")).or(page.locator("div"))).hasText(new String[]{"hello", "world"});
    assertThat(page.locator("article").or(page.locator("something"))).hasCount(0);
    assertThat(page.locator("article").or(page.locator("div"))).hasText("hello");
    assertThat(page.locator("article").or(page.locator("span"))).hasText("world");
    assertThat(page.locator("div").or(page.locator("article"))).hasText("hello");
    assertThat(page.locator("span").or(page.locator("article"))).hasText("world");
  }

  @PlaywrightTest
  @DisplayName("LO-176 should support locator.locator with and/or")
  void shouldSupportLocatorLocatorWithAndOr(Page page) {
    page.setContent("\n" +
      "    <div>one <span>two</span> <button>three</button> </div>\n" +
      "    <span>four</span>\n" +
      "    <button>five</button>\n" +
      "  ");

    assertThat(page.locator("div").locator(page.locator("button"))).hasText(new String[]{"three"});
    assertThat(page.locator("div").locator(page.locator("button").or(page.locator("span")))).hasText(new String[]{"two", "three"});
    assertThat(page.locator("button").or(page.locator("span"))).hasText(new String[]{"two", "three", "four", "five"});

    assertThat(page.locator("div").locator(page.locator("button").and(page.getByRole(AriaRole.BUTTON)))).hasText(new String[]{"three"});
    assertThat(page.locator("button").and(page.getByRole(AriaRole.BUTTON))).hasText(new String[]{"three", "five"});
  }

  @PlaywrightTest
  @DisplayName("LO-178 should allow some, but not all nested frameLocators")
  @Fixme("java: the message quotes the selector with double quotes: 'while querying \"locator(\"iframe\").contentFrame().locator(\"div\").or(...'")
  void shouldAllowSomeButNotAllNestedFrameLocators(Page page) {
    page.setContent("<iframe srcdoc=\"<span id=target>world</span>\"></iframe><span>hello</span>");
    assertThat(page.frameLocator("iframe").locator("span").or(page.frameLocator("iframe").locator("article"))).hasText("world");
    assertThat(page.frameLocator("iframe").locator("article").or(page.frameLocator("iframe").locator("span"))).hasText("world");
    assertThat(page.frameLocator("iframe").locator("span").and(page.frameLocator("iframe").locator("#target"))).hasText("world");
    AssertionFailedError error1 = assertThrows(AssertionFailedError.class,
      () -> assertThat(page.frameLocator("iframe").locator("div").or(page.frameLocator("#iframe").locator("span"))).hasText("world"));
    assertTrue(error1.getMessage().contains("Frame locators are not allowed inside composite locators, while querying \"locator('iframe').contentFrame().locator('div').or(locator('#iframe').contentFrame().locator('span'))"));
    AssertionFailedError error2 = assertThrows(AssertionFailedError.class,
      () -> assertThat(page.frameLocator("iframe").locator("div").and(page.frameLocator("#iframe").locator("span"))).hasText("world"));
    assertTrue(error2.getMessage().contains("Frame locators are not allowed inside composite locators, while querying \"locator('iframe').contentFrame().locator('div').and(locator('#iframe').contentFrame().locator('span'))"));
  }

  @PlaywrightTest
  @DisplayName("LO-179 should keep the capture when removing a common frame prefix")
  void shouldKeepTheCaptureWhenRemovingACommonFramePrefix(Page page) {
    page.setContent("<iframe id=f srcdoc=\"<section id=target><span>hi</span></section>\"></iframe>");
    Locator inner = page.frameLocator("#f").locator("*css=section >> span");
    assertThat(page.frameLocator("#f").locator("body").locator(inner)).hasAttribute("id", "target");

    Locator captureFrame = page.locator("*css=#f").contentFrame().locator("span");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.frameLocator("#f").locator("body").locator(captureFrame).count());
    assertTrue(error.getMessage().contains("Can not capture the selector before diving into the frame"));
  }

  @PlaywrightTest
  @DisplayName("LO-180 should enforce same frame for has/leftOf/rightOf/above/below/near")
  @Fixme("java: the message is \"Inner 'has' locator must belong to the same frame.\", with single quotes")
  void shouldEnforceSameFrameForHasLeftOfRightOfAboveBelowNear(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/frames/two-frames.html");
    Frame child = page.frames().get(1);
    for (String option : new String[]{"has"}) {
      PlaywrightException error = null;
      try {
        // Java: the computed key [option] is the has option.
        page.locator("div", new Page.LocatorOptions().setHas(child.locator("span")));
      } catch (PlaywrightException e) {
        error = e;
      }
      assertTrue(error.getMessage().contains("Inner \"" + option + "\" locator must belong to the same frame."));
    }
  }

  @PlaywrightTest
  @DisplayName("LO-181 alias methods coverage")
  void aliasMethodsCoverage(Page page) {
    page.setContent("<div><button>Submit</button></div>");
    assertThat(page.locator("button")).hasCount(1);
    assertThat(page.locator("div").locator("button")).hasCount(1);
    assertThat(page.locator("div").getByRole(AriaRole.BUTTON)).hasCount(1);
    assertThat(page.mainFrame().locator("button")).hasCount(1);
  }

  @PlaywrightTest
  @DisplayName("LO-182 count() should not throw during navigation")
  void countShouldNotThrowDuringNavigation(Playwright playwright, Page page, TestServer server) {
    // Java: a selector engine function is registered by its source, '(' + fn.toString() + ')()'.
    String createDummySelector = "(() => ({\n" +
      "  query(root, selector) {\n" +
      "    window.location.href = './frames/one-frame.html';\n" +
      "    return document.querySelector(selector);\n" +
      "  },\n" +
      "  queryAll(root, selector) {\n" +
      "    window.location.href = './frames/one-frame.html';\n" +
      "    return Array.from(document.querySelectorAll(selector));\n" +
      "  }\n" +
      "}))()";
    playwright.selectors().register("locatorCountHelper", createDummySelector);
    page.navigate(server.PREFIX + "/frames/one-frame.html");
    page.frames().get(1).setContent("<div>A</div>");
    assertEquals(0, page.frameLocator("locatorCountHelper=iframe").locator("locatorCountHelper=div").count());
  }
}
