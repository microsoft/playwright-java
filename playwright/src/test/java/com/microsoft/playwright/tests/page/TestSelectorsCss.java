/**
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

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.browserMajorVersion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/page/selectors-css.spec.ts. */
public class TestSelectorsCss {

  @PlaywrightTest
  @DisplayName("SE-001 should work with large DOM @smoke")
  void shouldWorkWithLargeDOM(Page page, TestServer server) {
    page.evaluate("() => {\n" +
      "  let id = 0;\n" +
      "  const next = (tag) => {\n" +
      "    const e = document.createElement(tag);\n" +
      "    const eid = ++id;\n" +
      "    e.textContent = 'id' + eid;\n" +
      "    e.id = '' + eid;\n" +
      "    return e;\n" +
      "  };\n" +
      "  const generate = (depth) => {\n" +
      "    const div = next('div');\n" +
      "    const span1 = next('span');\n" +
      "    const span2 = next('span');\n" +
      "    div.appendChild(span1);\n" +
      "    div.appendChild(span2);\n" +
      "    if (depth > 0) {\n" +
      "      div.appendChild(generate(depth - 1));\n" +
      "      div.appendChild(generate(depth - 1));\n" +
      "    }\n" +
      "    return div;\n" +
      "  };\n" +
      "  document.body.appendChild(generate(12));\n" +
      "}");
    List<String> selectors = Arrays.asList(
      "div div div span",
      "div > div div > span",
      "div + div div div span + span",
      "div ~ div div > span ~ span",
      "div > div > div + div > div + div > span ~ span",
      "div div div div div div div div div div span",
      "div > div > div > div > div > div > div > div > div > div > span",
      "div ~ div div ~ div div ~ div div ~ div div ~ div span",
      "span");

    boolean measure = false;
    for (String selector : selectors) {
      List<Object> counts1 = new ArrayList<>();
      long time1 = System.currentTimeMillis();
      for (int i = 0; i < (measure ? 10 : 1); i++)
        counts1.add(page.evalOnSelectorAll(selector, "els => els.length"));
      if (measure) {
        // console.log('pw: ' + (Date.now() - time1));
      }

      long time2 = System.currentTimeMillis();
      List<Object> counts2 = new ArrayList<>();
      for (int i = 0; i < (measure ? 10 : 1); i++)
        counts2.add(page.evaluate("selector => document.querySelectorAll(selector).length", selector));
      if (measure) {
        // console.log('qs: ' + (Date.now() - time2));
      }

      assertEquals(counts2, counts1);
    }
  }

  @PlaywrightTest
  @DisplayName("SE-002 should work for open shadow roots")
  void shouldWorkForOpenShadowRoots(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/deep-shadow.html");
    assertEquals("Hello from root1", page.evalOnSelector("css=span", "e => e.textContent"));
    assertEquals("Hello from root3 #2", page.evalOnSelector("css=[attr=\"value\\ space\"]", "e => e.textContent"));
    assertEquals("Hello from root3 #2", page.evalOnSelector("css=[attr='value\\ \\space']", "e => e.textContent"));
    assertEquals("Hello from root2", page.evalOnSelector("css=div div span", "e => e.textContent"));
    assertEquals("Hello from root3 #2", page.evalOnSelector("css=div span + span", "e => e.textContent"));
    assertEquals("Hello from root3 #2", page.evalOnSelector("css=span + [attr*=\"value\"]", "e => e.textContent"));
    assertEquals("Hello from root3 #2", page.evalOnSelector("css=[data-testid=\"foo\"] + [attr*=\"value\"]", "e => e.textContent"));
    assertEquals("Hello from root2", page.evalOnSelector("css=#target", "e => e.textContent"));
    assertEquals("Hello from root2", page.evalOnSelector("css=div #target", "e => e.textContent"));
    assertEquals("Hello from root2", page.evalOnSelector("css=div div #target", "e => e.textContent"));
    assertNull(page.querySelector("css=div div div #target"));
    assertEquals("Hello from root2", page.evalOnSelector("css=section > div div span", "e => e.textContent"));
    assertEquals("Hello from root3 #2", page.evalOnSelector("css=section > div div span:nth-child(2)", "e => e.textContent"));
    assertNull(page.querySelector("css=section div div div div"));

    ElementHandle root2 = page.querySelector("css=div div");
    assertEquals("Hello from root2", root2.evalOnSelector("css=#target", "e => e.textContent"));
    assertNull(root2.querySelector("css:light=#target"));
    ElementHandle root2Shadow = root2.evaluateHandle("r => r.shadowRoot").asElement();
    assertEquals("Hello from root2", root2Shadow.evalOnSelector("css:light=#target", "e => e.textContent"));
    ElementHandle root3 = page.querySelectorAll("css=div div").get(1);
    assertEquals("Hello from root3", root3.evalOnSelector("text=root3", "e => e.textContent"));
    assertEquals("Hello from root3 #2", root3.evalOnSelector("css=[attr*=\"value\"]", "e => e.textContent"));
    assertNull(root3.querySelector("css:light=[attr*=\"value\"]"));
  }

  @PlaywrightTest
  @DisplayName("SE-003 should work with > combinator and spaces")
  void shouldWorkWithCombinatorAndSpaces(Page page, TestServer server) {
    page.setContent("<div foo=\"bar\" bar=\"baz\"><span></span></div>");
    assertEquals("<span></span>", page.evalOnSelector("div[foo=\"bar\"] > span", "e => e.outerHTML"));
    assertEquals("<span></span>", page.evalOnSelector("div[foo=\"bar\"]> span", "e => e.outerHTML"));
    assertEquals("<span></span>", page.evalOnSelector("div[foo=\"bar\"] >span", "e => e.outerHTML"));
    assertEquals("<span></span>", page.evalOnSelector("div[foo=\"bar\"]>span", "e => e.outerHTML"));
    assertEquals("<span></span>", page.evalOnSelector("div[foo=\"bar\"]   >    span", "e => e.outerHTML"));
    assertEquals("<span></span>", page.evalOnSelector("div[foo=\"bar\"]>    span", "e => e.outerHTML"));
    assertEquals("<span></span>", page.evalOnSelector("div[foo=\"bar\"]     >span", "e => e.outerHTML"));
    assertEquals("<span></span>", page.evalOnSelector("div[foo=\"bar\"][bar=\"baz\"] > span", "e => e.outerHTML"));
    assertEquals("<span></span>", page.evalOnSelector("div[foo=\"bar\"][bar=\"baz\"]> span", "e => e.outerHTML"));
    assertEquals("<span></span>", page.evalOnSelector("div[foo=\"bar\"][bar=\"baz\"] >span", "e => e.outerHTML"));
    assertEquals("<span></span>", page.evalOnSelector("div[foo=\"bar\"][bar=\"baz\"]>span", "e => e.outerHTML"));
    assertEquals("<span></span>", page.evalOnSelector("div[foo=\"bar\"][bar=\"baz\"]   >    span", "e => e.outerHTML"));
    assertEquals("<span></span>", page.evalOnSelector("div[foo=\"bar\"][bar=\"baz\"]>    span", "e => e.outerHTML"));
    assertEquals("<span></span>", page.evalOnSelector("div[foo=\"bar\"][bar=\"baz\"]     >span", "e => e.outerHTML"));
  }

  @PlaywrightTest
  @DisplayName("SE-004 should work with comma separated list")
  void shouldWorkWithCommaSeparatedList(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/deep-shadow.html");
    assertEquals(5, page.evalOnSelectorAll("css=span,section #root1", "els => els.length"));
    assertEquals(5, page.evalOnSelectorAll("css=section #root1, div span", "els => els.length"));
    assertEquals("root1", page.evalOnSelector("css=doesnotexist , section #root1", "e => e.id"));
    assertEquals(1, page.evalOnSelectorAll("css=doesnotexist ,section #root1", "els => els.length"));
    assertEquals(4, page.evalOnSelectorAll("css=span,div span", "els => els.length"));
    assertEquals(4, page.evalOnSelectorAll("css=span,div span,div div span", "els => els.length"));
    assertEquals(2, page.evalOnSelectorAll("css=#target,[attr=\"value\\ space\"]", "els => els.length"));
    assertEquals(4, page.evalOnSelectorAll("css=#target,[data-testid=\"foo\"],[attr=\"value\\ space\"]", "els => els.length"));
    assertEquals(4, page.evalOnSelectorAll("css=#target,[data-testid=\"foo\"],[attr=\"value\\ space\"],span", "els => els.length"));
  }

  @PlaywrightTest
  @DisplayName("SE-005 should keep dom order with comma separated list")
  void shouldKeepDomOrderWithCommaSeparatedList(Page page) {
    page.setContent("<section><span><div><x></x><y></y></div></span></section>");
    assertEquals("SPAN,DIV", page.evalOnSelectorAll("css=span,div", "els => els.map(e => e.nodeName).join(',')"));
    assertEquals("SPAN,DIV", page.evalOnSelectorAll("css=div,span", "els => els.map(e => e.nodeName).join(',')"));
    assertEquals("DIV", page.evalOnSelectorAll("css=span div, div", "els => els.map(e => e.nodeName).join(',')"));
    assertEquals("SECTION", page.evalOnSelectorAll("*css=section >> css=div,span", "els => els.map(e => e.nodeName).join(',')"));
    assertEquals("DIV", page.evalOnSelectorAll("css=section >> *css=div >> css=x,y", "els => els.map(e => e.nodeName).join(',')"));
    assertEquals("SPAN,DIV", page.evalOnSelectorAll("css=section >> *css=div,span >> css=x,y", "els => els.map(e => e.nodeName).join(',')"));
    assertEquals("SPAN,DIV", page.evalOnSelectorAll("css=section >> *css=div,span >> css=y", "els => els.map(e => e.nodeName).join(',')"));
  }

  @PlaywrightTest
  @DisplayName("SE-006 should return multiple captures for the same node")
  void shouldReturnMultipleCapturesForTheSameNode(Page page) {
    page.setContent("<div><div><div><span></span></div></div></div>");
    assertEquals("DIV,DIV,DIV", page.evalOnSelectorAll("*css=div >> span", "els => els.map(e => e.nodeName).join(',')"));
  }

  @PlaywrightTest
  @DisplayName("SE-007 should return multiple captures when going up the hierarchy")
  void shouldReturnMultipleCapturesWhenGoingUpTheHierarchy(Page page) {
    page.setContent("<section>Hello<ul><li></li><li></li></ul></section>");
    assertEquals("LI,LI", page.evalOnSelectorAll("*css=li >> ../.. >> text=Hello", "els => els.map(e => e.nodeName).join(',')"));
  }

  @PlaywrightTest
  @DisplayName("SE-008 should work with comma separated list in various positions")
  void shouldWorkWithCommaSeparatedListInVariousPositions(Page page) {
    page.setContent("<section><span><div><x></x><y></y></div></span></section>");
    assertEquals("X,Y", page.evalOnSelectorAll("css=span,div >> css=x,y", "els => els.map(e => e.nodeName).join(',')"));
    assertEquals("X", page.evalOnSelectorAll("css=span,div >> css=x", "els => els.map(e => e.nodeName).join(',')"));
    assertEquals("X,Y", page.evalOnSelectorAll("css=div >> css=x,y", "els => els.map(e => e.nodeName).join(',')"));
    assertEquals("X", page.evalOnSelectorAll("css=div >> css=x", "els => els.map(e => e.nodeName).join(',')"));

    assertEquals("X", page.evalOnSelectorAll("css=section >> css=div >> css=x", "els => els.map(e => e.nodeName).join(',')"));
    assertEquals("Y", page.evalOnSelectorAll("css=section >> css=span >> css=div >> css=y", "els => els.map(e => e.nodeName).join(',')"));
    assertEquals("X,Y", page.evalOnSelectorAll("css=section >> css=div >> css=x,y", "els => els.map(e => e.nodeName).join(',')"));
    assertEquals("X,Y", page.evalOnSelectorAll("css=section >> css=div,span >> css=x,y", "els => els.map(e => e.nodeName).join(',')"));
    assertEquals("X,Y", page.evalOnSelectorAll("css=section >> css=span >> css=x,y", "els => els.map(e => e.nodeName).join(',')"));
  }

  @PlaywrightTest
  @DisplayName("SE-009 should work with comma inside text")
  void shouldWorkWithCommaInsideText(Page page) {
    page.setContent("<span></span><div attr=\"hello,world!\"></div>");
    assertEquals("<div attr=\"hello,world!\"></div>", page.evalOnSelector("css=div[attr=\"hello,world!\"]", "e => e.outerHTML"));
    assertEquals("<div attr=\"hello,world!\"></div>", page.evalOnSelector("css=[attr=\"hello,world!\"]", "e => e.outerHTML"));
    assertEquals("<div attr=\"hello,world!\"></div>", page.evalOnSelector("css=div[attr='hello,world!']", "e => e.outerHTML"));
    assertEquals("<div attr=\"hello,world!\"></div>", page.evalOnSelector("css=[attr='hello,world!']", "e => e.outerHTML"));
    assertEquals("<span></span>", page.evalOnSelector("css=div[attr=\"hello,world!\"],span", "e => e.outerHTML"));
  }

  @PlaywrightTest
  @DisplayName("SE-010 should work with attribute selectors")
  void shouldWorkWithAttributeSelectors(Page page) {
    page.setContent("<div attr=\"hello world\" attr2=\"hello-''>>foo=bar[]\" attr3=\"] span\"><span></span></div>");
    page.evaluate("() => window['div'] = document.querySelector('div')");
    List<String> selectors = Arrays.asList(
      "[attr=\"hello world\"]",
      "[attr = \"hello world\"]",
      "[attr ~= world]",
      "[attr ^=hello ]",
      "[attr $= world ]",
      "[attr *= \"llo wor\" ]",
      "[attr2 |= hello]",
      "[attr = \"Hello World\" i ]",
      "[attr *= \"llo WOR\"i]",
      "[attr $= woRLD i]",
      "[attr2 = \"hello-''>>foo=bar[]\"]",
      "[attr2 $=\"foo=bar[]\"]");
    for (String selector : selectors)
      assertEquals(true, page.evalOnSelector(selector, "e => e === window['div']"));
    assertEquals(true, page.evalOnSelector("[attr*=hello] span", "e => e.parentNode === window['div']"));
    assertEquals(true, page.evalOnSelector("[attr*=hello] >> span", "e => e.parentNode === window['div']"));
    assertEquals(true, page.evalOnSelector("[attr3=\"] span\"] >> span", "e => e.parentNode === window['div']"));
  }

  @PlaywrightTest
  @DisplayName("SE-011 should not match root after >>")
  void shouldNotMatchRootAfter(Page page, TestServer server) {
    page.setContent("<section><div>test</div></section>");
    ElementHandle element = page.querySelector("css=section >> css=section");
    assertNull(element);
  }

  @PlaywrightTest
  @DisplayName("SE-012 should work with numerical id")
  void shouldWorkWithNumericalId(Page page, TestServer server) {
    page.setContent("<section id=\"123\"></section>");
    ElementHandle element = page.querySelector("#\\31\\32\\33");
    assertNotNull(element);
  }

  @PlaywrightTest
  @DisplayName("SE-013 should work with wrong-case id")
  void shouldWorkWithWrongcaseId(Page page) {
    page.setContent("<section id=\"Hello\"></section>");
    assertEquals("SECTION", page.evalOnSelector("#Hello", "e => e.tagName"));
    assertEquals("SECTION", page.evalOnSelector("#hello", "e => e.tagName"));
    assertEquals("SECTION", page.evalOnSelector("#HELLO", "e => e.tagName"));
    assertEquals("SECTION", page.evalOnSelector("#helLO", "e => e.tagName"));
  }

  @PlaywrightTest
  @DisplayName("SE-014 should work with *")
  void shouldWorkWithAsterisk(Page page) {
    page.setContent("<div id=div1></div><div id=div2><span><span></span></span></div>");
    // Includes html, head and body.
    assertEquals(7, page.evalOnSelectorAll("*", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("*#div1", "els => els.length"));
    assertEquals(6, page.evalOnSelectorAll("*:not(#div1)", "els => els.length"));
    assertEquals(5, page.evalOnSelectorAll("*:not(div)", "els => els.length"));
    assertEquals(5, page.evalOnSelectorAll("*:not(span)", "els => els.length"));
    assertEquals(0, page.evalOnSelectorAll("*:not(*)", "els => els.length"));
    assertEquals(7, page.evalOnSelectorAll("*:is(*)", "els => els.length"));
    assertEquals(6, page.evalOnSelectorAll("* *", "els => els.length"));
    assertEquals(4, page.evalOnSelectorAll("* *:not(span)", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("div > *", "els => els.length"));
    assertEquals(2, page.evalOnSelectorAll("div *", "els => els.length"));
    assertEquals(6, page.evalOnSelectorAll("* > *", "els => els.length"));

    ElementHandle body = page.querySelector("body");
    // Does not include html, head or body.
    assertEquals(4, body.evalOnSelectorAll("*", "els => els.length"));
    assertEquals(1, body.evalOnSelectorAll("*#div1", "els => els.length"));
    assertEquals(3, body.evalOnSelectorAll("*:not(#div1)", "els => els.length"));
    assertEquals(2, body.evalOnSelectorAll("*:not(div)", "els => els.length"));
    assertEquals(2, body.evalOnSelectorAll("*:not(span)", "els => els.length"));
    assertEquals(0, body.evalOnSelectorAll("*:not(*)", "els => els.length"));
    assertEquals(4, body.evalOnSelectorAll("*:is(*)", "els => els.length"));
    assertEquals(1, body.evalOnSelectorAll("div > *", "els => els.length"));
    assertEquals(2, body.evalOnSelectorAll("div *", "els => els.length"));
    // Selectors v2 matches jquery in the sense that matching starts with the element scope,
    // not the document scope.
    assertEquals(2, body.evalOnSelectorAll("* > *", "els => els.length"));
    // Adding scope makes querySelectorAll work like jquery.
    assertEquals(2, body.evalOnSelectorAll(":scope * > *", "els => els.length"));
    // Note that the following two selectors are following jquery logic even
    // with selectors v1. Just running `body.querySelectorAll` returns 4 and 2 respectively.
    // That's probably a bug in v1, but oh well.
    assertEquals(2, body.evalOnSelectorAll("* *", "els => els.length"));
    assertEquals(0, body.evalOnSelectorAll("* *:not(span)", "els => els.length"));
  }

  @PlaywrightTest
  @DisplayName("SE-015 should work with :nth-child")
  void shouldWorkWithNthchild(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/deep-shadow.html");
    assertEquals(3, page.evalOnSelectorAll("css=span:nth-child(odd)", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("css=span:nth-child(even)", "els => els.length"));
    assertEquals(4, page.evalOnSelectorAll("css=span:nth-child(n+1)", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("css=span:nth-child(n+2)", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("css=span:nth-child(2n)", "els => els.length"));
    assertEquals(3, page.evalOnSelectorAll("css=span:nth-child(2n+1)", "els => els.length"));
    assertEquals(0, page.evalOnSelectorAll("css=span:nth-child(-n)", "els => els.length"));
    assertEquals(3, page.evalOnSelectorAll("css=span:nth-child(-n+1)", "els => els.length"));
    assertEquals(4, page.evalOnSelectorAll("css=span:nth-child(-n+2)", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("css=span:nth-child(23n+2)", "els => els.length"));
  }

  @PlaywrightTest
  @DisplayName("SE-016 should work with :nth-child(of) notation with nested functions")
  void shouldWorkWithNthchildofNotationWithNestedFunctions(Page page, BrowserName browserName, Browser browser) {
    assumeFalse(browserName == BrowserName.CHROMIUM && browserMajorVersion(browser) < 111, "https://caniuse.com/css-nth-child-of");

    page.setContent("\n" +
      "    <div>\n" +
      "      <span>span1</span>\n" +
      "      <span class=foo>span2<dd></dd></span>\n" +
      "      <span class=foo>span3<dd class=marker></dd></span>\n" +
      "      <span class=foo>span4<dd class=marker></dd></span>\n" +
      "      <span class=foo>span5<dd></dd></span>\n" +
      "      <span>span6</span>\n" +
      "    </div>\n" +
      "  ");
    assertEquals(Arrays.asList("span1"), page.evalOnSelectorAll("css=span:nth-child(1)", "els => els.map(e => e.textContent)"));
    assertEquals(Arrays.asList("span2"), page.evalOnSelectorAll("css=span:nth-child(1 of .foo)", "els => els.map(e => e.textContent)"));
    assertEquals(Arrays.asList("span3"), page.evalOnSelectorAll("css=span:nth-child(1 of .foo:has(dd.marker))", "els => els.map(e => e.textContent)"));
    assertEquals(Arrays.asList("span4"), page.evalOnSelectorAll("css=span:nth-last-child(1 of .foo:has(dd.marker))", "els => els.map(e => e.textContent)"));
    assertEquals(Arrays.asList("span5"), page.evalOnSelectorAll("css=span:nth-last-child(1 of .foo)", "els => els.map(e => e.textContent)"));
    assertEquals(Arrays.asList("span6"), page.evalOnSelectorAll("css=span:nth-last-child(  1  )", "els => els.map(e => e.textContent)"));

    assertEquals(Arrays.asList("span3"), page.evalOnSelectorAll("css=span:nth-child(1 of .foo:nth-child(3))", "els => els.map(e => e.textContent)"));
    assertEquals(Arrays.asList(), page.evalOnSelectorAll("css=span:nth-child(1 of .foo:nth-child(6))", "els => els.map(e => e.textContent)"));
  }

  @PlaywrightTest
  @DisplayName("SE-017 should work with :not")
  void shouldWorkWithNot(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/deep-shadow.html");
    assertEquals(2, page.evalOnSelectorAll("css=div:not(#root1)", "els => els.length"));
    assertEquals(4, page.evalOnSelectorAll("css=body :not(span)", "els => els.length"));
    assertEquals(0, page.evalOnSelectorAll("css=div > :not(span):not(div)", "els => els.length"));
  }

  @PlaywrightTest
  @DisplayName("SE-018 should work with ~")
  void shouldWorkWithTilde(Page page) {
    page.setContent("\n" +
      "    <div id=div1></div>\n" +
      "    <div id=div2></div>\n" +
      "    <div id=div3></div>\n" +
      "    <div id=div4></div>\n" +
      "    <div id=div5></div>\n" +
      "    <div id=div6></div>\n" +
      "  ");
    assertEquals(Arrays.asList("div4", "div5", "div6"), page.evalOnSelectorAll("#div3 >> :scope ~ div", "els => els.map(e => e.id)"));
    assertEquals(Arrays.asList("div4", "div5", "div6"), page.evalOnSelectorAll("#div3 >> :scope ~ *", "els => els.map(e => e.id)"));
    assertEquals(Arrays.asList("div4", "div5", "div6"), page.evalOnSelectorAll("#div3 >> ~ div", "els => els.map(e => e.id)"));
    assertEquals(Arrays.asList("div4", "div5", "div6"), page.evalOnSelectorAll("#div3 >> ~ *", "els => els.map(e => e.id)"));
    assertEquals(Arrays.asList("div3"), page.evalOnSelectorAll("#div3 >> #div1 ~ :scope", "els => els.map(e => e.id)"));
    assertEquals(Arrays.asList(), page.evalOnSelectorAll("#div3 >> #div4 ~ :scope", "els => els.map(e => e.id)"));
    assertEquals(1, page.evalOnSelectorAll("css=#div1 ~ div ~ #div6", "els => els.length"));
    assertEquals(4, page.evalOnSelectorAll("css=#div1 ~ div ~ div", "els => els.length"));
    assertEquals(2, page.evalOnSelectorAll("css=#div3 ~ div ~ div", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("css=#div4 ~ div ~ div", "els => els.length"));
    assertEquals(0, page.evalOnSelectorAll("css=#div5 ~ div ~ div", "els => els.length"));
    assertEquals(0, page.evalOnSelectorAll("css=#div3 ~ #div2 ~ #div6", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("css=#div3 ~ #div4 ~ #div5", "els => els.length"));
  }

  @PlaywrightTest
  @DisplayName("SE-019 should work with +")
  void shouldWorkWithPlus(Page page) {
    page.setContent("\n" +
      "    <section>\n" +
      "      <div id=div1></div>\n" +
      "      <div id=div2></div>\n" +
      "      <div id=div3></div>\n" +
      "      <div id=div4></div>\n" +
      "      <div id=div5></div>\n" +
      "      <div id=div6></div>\n" +
      "    </section>\n" +
      "  ");
    assertEquals(Arrays.asList("div2"), page.evalOnSelectorAll("#div1 >> :scope+div", "els => els.map(e => e.id)"));
    assertEquals(Arrays.asList("div2"), page.evalOnSelectorAll("#div1 >> :scope+*", "els => els.map(e => e.id)"));
    assertEquals(Arrays.asList("div2"), page.evalOnSelectorAll("#div1 >> + div", "els => els.map(e => e.id)"));
    assertEquals(Arrays.asList("div2"), page.evalOnSelectorAll("#div1 >> + *", "els => els.map(e => e.id)"));
    assertEquals(Arrays.asList("div3"), page.evalOnSelectorAll("#div3 >> div + :scope", "els => els.map(e => e.id)"));
    assertEquals(Arrays.asList(), page.evalOnSelectorAll("#div3 >> #div1 + :scope", "els => els.map(e => e.id)"));
    assertEquals(1, page.evalOnSelectorAll("css=#div1 ~ div + #div6", "els => els.length"));
    assertEquals(4, page.evalOnSelectorAll("css=#div1 ~ div + div", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("css=#div3 + div + div", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("css=#div4 ~ #div5 + div", "els => els.length"));
    assertEquals(0, page.evalOnSelectorAll("css=#div5 + div + div", "els => els.length"));
    assertEquals(0, page.evalOnSelectorAll("css=#div3 ~ #div2 + #div6", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("css=#div3 + #div4 + #div5", "els => els.length"));
    assertEquals(0, page.evalOnSelectorAll("css=div + #div1", "els => els.length"));
    assertEquals(4, page.evalOnSelectorAll("css=section > div + div ~ div", "els => els.length"));
    assertEquals(2, page.evalOnSelectorAll("css=section > div + #div4 ~ div", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("css=section:has(:scope > div + #div2)", "els => els.length"));
    assertEquals(0, page.evalOnSelectorAll("css=section:has(:scope > div + #div1)", "els => els.length"));
    assertEquals("div4", page.evalOnSelector("css=div:has(:scope + #div5)", "e => e.id"));
  }

  @PlaywrightTest
  @DisplayName("SE-020 should work with spaces in :nth-child and :not")
  void shouldWorkWithSpacesInNthchildAndNot(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/deep-shadow.html");
    assertEquals(1, page.evalOnSelectorAll("css=span:nth-child(23n +2)", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("css=span:nth-child(23n+ 2)", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("css=span:nth-child( 23n + 2 )", "els => els.length"));
    assertEquals(3, page.evalOnSelectorAll("css=span:not(#root1 #target)", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("css=span:not(:not(#root1 #target))", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("css=span:not(span:not(#root1 #target))", "els => els.length"));
    assertEquals(2, page.evalOnSelectorAll("css=div > :not(span)", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("css=body :not(span, div)", "els => els.length"));
    assertEquals(5, page.evalOnSelectorAll("css=span, section:not(span, div)", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("span:nth-child(23n+ 2) >> xpath=.", "els => els.length"));
  }

  @PlaywrightTest
  @DisplayName("SE-021 should work with :is")
  void shouldWorkWithIs(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/deep-shadow.html");
    assertEquals(1, page.evalOnSelectorAll("css=div:is(#root1)", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("css=div:is(#root1, #target)", "els => els.length"));
    assertEquals(0, page.evalOnSelectorAll("css=div:is(span, #target)", "els => els.length"));
    assertEquals(2, page.evalOnSelectorAll("css=div:is(span, #root1 > *)", "els => els.length"));
    assertEquals(3, page.evalOnSelectorAll("css=div:is(section div)", "els => els.length"));
    assertEquals(7, page.evalOnSelectorAll("css=:is(div, span)", "els => els.length"));
    assertEquals(3, page.evalOnSelectorAll("css=section:is(section) div:is(section div)", "els => els.length"));
    assertEquals(6, page.evalOnSelectorAll("css=:is(div, span) > *", "els => els.length"));
    assertEquals(0, page.evalOnSelectorAll("css=#root1:has(:is(#root1))", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("css=#root1:has(:is(:scope, #root1))", "els => els.length"));
  }

  @PlaywrightTest
  @DisplayName("SE-022 should work with :has")
  void shouldWorkWithHas(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/deep-shadow.html");
    assertEquals(2, page.evalOnSelectorAll("css=div:has(#target)", "els => els.length"));
    assertEquals(3, page.evalOnSelectorAll("css=div:has([data-testid=foo])", "els => els.length"));
    assertEquals(2, page.evalOnSelectorAll("css=div:has([attr*=value])", "els => els.length"));

    page.setContent("<section><span></span><div></div></section><section><br></section>");
    assertEquals(1, page.evalOnSelectorAll("section:has(span, div)", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("section:has(span, div)", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("section:has(br)", "els => els.length"));
    assertEquals(2, page.evalOnSelectorAll("section:has(span, br)", "els => els.length"));
    assertEquals(2, page.evalOnSelectorAll("section:has(span, br, div)", "els => els.length"));
  }

  @PlaywrightTest
  @DisplayName("SE-023 should work with :scope")
  void shouldWorkWithScope(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/deep-shadow.html");
    // 'is' does not change the scope, so it remains 'html'.
    assertEquals(0, page.evalOnSelectorAll("css=div:is(:scope#root1)", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("css=div:is(:scope #root1)", "els => els.length"));
    // 'has' does change the scope, so it becomes the 'div' we are querying.
    assertEquals(1, page.evalOnSelectorAll("css=div:has(:scope > #target)", "els => els.length"));

    ElementHandle handle = page.querySelector("css=span");
    // Java: Page and ElementHandle have no common type, the scopes are their evalOnSelectorAll methods.
    List<BiFunction<String, String, Object>> scopes = Arrays.asList(page::evalOnSelectorAll, handle::evalOnSelectorAll);
    for (BiFunction<String, String, Object> scope : scopes) {
      assertEquals(1, scope.apply("css=:scope", "els => els.length"));
      assertEquals(0, scope.apply("css=* :scope", "els => els.length"));
      assertEquals(0, scope.apply("css=* + :scope", "els => els.length"));
      assertEquals(0, scope.apply("css=* > :scope", "els => els.length"));
      assertEquals(0, scope.apply("css=* ~ :scope", "els => els.length"));
    }

    page.setContent("<article><div class=target>hello<span></span></div></article>");
    // 'scope' should allow additional native css modifiers
    assertEquals("hello", page.evalOnSelector("div >> :scope.target", "e => e.textContent"));
    assertEquals("hello", page.evalOnSelector("div >> :scope:nth-child(1)", "e => e.textContent"));
    assertEquals("hello", page.evalOnSelector("div >> :scope.target:has(span)", "e => e.textContent"));
    assertEquals("HTML", page.evalOnSelector("html:scope", "e => e.nodeName"));

    page.setContent("<section><span id=span1><span id=inner></span></span><span id=span2></span></section>");
    assertEquals(Arrays.asList("inner"), page.evalOnSelectorAll("#span1 >> span:not(:has(:scope > div))", "els => els.map(e => e.id)"));
    assertEquals(Arrays.asList("span1", "inner"), page.evalOnSelectorAll("#span1 >> #inner,:scope", "els => els.map(e => e.id)"));
    assertEquals(Arrays.asList("span1", "inner"), page.evalOnSelectorAll("#span1 >> span,:scope", "els => els.map(e => e.id)"));
    assertEquals(Arrays.asList("inner"), page.evalOnSelectorAll("#span1 >> span:not(:scope)", "els => els.map(e => e.id)"));
    // TODO: the following two do not work. We do not expand the context for the inner :scope,
    // because we should only expand for one clause of :is() that contains :scope, but not the other.
    // assertEquals(Arrays.asList("span1"), page.evalOnSelectorAll("#span1 >> span:is(:scope)", "els => els.map(e => e.id)"));
    // assertEquals(Arrays.asList("span1", "inner"), page.evalOnSelectorAll("#span1 >> span:is(:scope,#inner)", "els => els.map(e => e.id)"));
  }

  @PlaywrightTest
  @DisplayName("SE-024 should work with :scope and class")
  void shouldWorkWithScopeAndClass(Page page) {
    page.setContent("<div class=\"apple\"></div>\n" +
      "                         <div class=\"apple selected\"></div>");
    Locator apples = page.locator(".apple");
    Locator selectedApples = apples.locator(":scope.selected");
    assertThat(selectedApples).hasCount(1);
  }

  @PlaywrightTest
  @DisplayName("SE-025 should absolutize relative selectors")
  void shouldAbsolutizeRelativeSelectors(Page page, TestServer server) {
    page.setContent("<div><span>Hi</span></div>");
    assertEquals("Hi", page.evalOnSelector("div >> >span", "e => e.textContent"));
    assertEquals("Hi", page.locator("div").locator(">span").textContent());
    assertEquals("<div><span>Hi</span></div>", page.evalOnSelector("div:has(> span)", "e => e.outerHTML"));
    assertNull(page.querySelector("div:has(> div)"));
  }

  @PlaywrightTest
  @DisplayName("SE-026 css on the handle should be relative")
  void cssOnTheHandleShouldBeRelative(Page page) {
    page.setContent("\n" +
      "    <span class=\"find-me\" id=target1>1</span>\n" +
      "    <div>\n" +
      "      <span class=\"find-me\" id=target2>2</span>\n" +
      "    </div>\n" +
      "  ");
    assertEquals("target1", page.evalOnSelector(".find-me", "e => e.id"));

    ElementHandle div = page.querySelector("div");
    assertEquals("target2", div.evalOnSelector(".find-me", "e => e.id"));
    assertEquals("target2", page.evalOnSelector("div >> .find-me", "e => e.id"));
  }

  @PlaywrightTest
  @DisplayName("SE-027 should use light DOM structure for child combinator with slotted content")
  void shouldUseLightDOMStructureForChildCombinatorWithSlottedContent(Page page) {
    page.setContent("\n" +
      "    <my-button>\n" +
      "      <template shadowrootmode=\"open\">\n" +
      "        <button><slot></slot></button>\n" +
      "      </template>\n" +
      "      <div class=\"content\">Foo</div>\n" +
      "    </my-button>\n" +
      "  ");
    assertEquals("content", page.evalOnSelector("my-button > div", "e => e.className"));
    assertEquals("Foo", page.evalOnSelector("my-button > .content", "e => e.textContent"));
  }

}
