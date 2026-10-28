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

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/selectors-misc.spec.ts. */
public class TestSelectorsMisc {
  @PlaywrightTest
  @DisplayName("SE-133 should work for open shadow roots")
  void shouldWorkForOpenShadowRoots(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/deep-shadow.html");
    assertEquals("Hello from root2", page.evalOnSelector("id=target", "e => e.textContent"));
    assertEquals("Hello from root1", page.evalOnSelector("data-testid=foo", "e => e.textContent"));
    assertEquals(3, page.evalOnSelectorAll("data-testid=foo", "els => els.length"));
    assertNull(page.querySelector("id:light=target"));
    assertNull(page.querySelector("data-testid:light=foo"));
    assertEquals(new ArrayList<>(), page.querySelectorAll("data-testid:light=foo"));
  }

  @PlaywrightTest
  @DisplayName("SE-134 should click on links in shadow dom")
  void shouldClickOnLinksInShadowDom(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/shadow-dom-link.html");
    assertEquals(0, page.evaluate("() => window.clickCount"));
    page.click("#inner-link");
    assertEquals(1, page.evaluate("() => window.clickCount"));
  }

  @PlaywrightTest
  @DisplayName("SE-135 should work with :visible")
  void shouldWorkWithVisible(Page page) throws Exception {
    page.setContent("\n" +
      "    <section>\n" +
      "      <div id=target1></div>\n" +
      "      <div id=target2></div>\n" +
      "    </section>\n" +
      "  ");
    assertNull(page.querySelector("div:visible"));

    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.waitForSelector("div:visible", new Page.WaitForSelectorOptions().setTimeout(1000)));
    assertTrue(error.getMessage().contains("1000ms"));

    // Java: the wait runs after the page change, it cannot be pending while the page is driven from the same thread.
    page.evalOnSelector("#target2", "div => div.textContent = 'Now visible'");
    ElementHandle element = page.waitForSelector("div:visible", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED));
    assertEquals("target2", element.evaluate("e => e.id"));

    assertEquals("target2", page.evalOnSelector("div:visible", "div => div.id"));
  }

  @PlaywrightTest
  @DisplayName("SE-136 should work with >> visible=")
  void shouldWorkWithVisible2(Page page) throws Exception {
    page.setContent("\n" +
      "    <section>\n" +
      "      <div id=target1></div>\n" +
      "      <div id=target2></div>\n" +
      "    </section>\n" +
      "  ");
    assertNull(page.querySelector("div >> visible=true"));

    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.waitForSelector("div >> visible=true", new Page.WaitForSelectorOptions().setTimeout(1000)));
    assertTrue(error.getMessage().contains("1000ms"));

    // Java: the wait runs after the page change, it cannot be pending while the page is driven from the same thread.
    page.evalOnSelector("#target2", "div => div.textContent = 'Now visible'");
    ElementHandle element = page.waitForSelector("div >> visible=true", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED));
    assertEquals("target2", element.evaluate("e => e.id"));

    assertEquals("target2", page.evalOnSelector("div >> visible=true", "div => div.id"));
  }

  @PlaywrightTest
  @DisplayName("SE-137 should work with >> visible=false")
  void shouldWorkWithVisibleFalse(Page page) {
    page.setContent("\n" +
      "    <section>\n" +
      "      <div id=target1></div>\n" +
      "      <div id=target2></div>\n" +
      "    </section>\n" +
      "  ");
    assertThat(page.locator("div >> visible=false")).hasCount(2);
    page.locator("#target2").evaluate("div => div.textContent = 'Now visible'");
    assertThat(page.locator("div >> visible=false")).hasCount(1);
  }

  @PlaywrightTest
  @DisplayName("SE-138 should work with :nth-match")
  void shouldWorkWithNthMatch(Page page) throws Exception {
    page.setContent("\n" +
      "    <section>\n" +
      "      <div id=target1></div>\n" +
      "      <div id=target2></div>\n" +
      "    </section>\n" +
      "  ");
    assertNull(page.querySelector(":nth-match(div, 3)"));
    assertEquals("target1", page.evalOnSelector(":nth-match(div, 1)", "e => e.id"));
    assertEquals("target2", page.evalOnSelector(":nth-match(div, 2)", "e => e.id"));
    assertEquals("target2", page.evalOnSelector(":nth-match(section > div, 2)", "e => e.id"));
    assertEquals("target1", page.evalOnSelector(":nth-match(section, div, 2)", "e => e.id"));
    assertEquals("target2", page.evalOnSelector(":nth-match(div, section, 3)", "e => e.id"));
    assertEquals(2, page.evalOnSelectorAll(":is(:nth-match(div, 1), :nth-match(div, 2))", "els => els.length"));

    PlaywrightException error;
    error = assertThrows(PlaywrightException.class, () -> page.querySelector(":nth-match(div, bar, 0)"));
    assertTrue(error.getMessage().contains("\"nth-match\" engine expects a one-based index as the last argument"));

    error = assertThrows(PlaywrightException.class, () -> page.querySelector(":nth-match(2)"));
    assertTrue(error.getMessage().contains("\"nth-match\" engine expects non-empty selector list and an index argument"));

    error = assertThrows(PlaywrightException.class, () -> page.querySelector(":nth-match(div, bar, foo)"));
    assertTrue(error.getMessage().contains("\"nth-match\" engine expects a one-based index as the last argument"));

    // Java: the wait runs after the page change, it cannot be pending while the page is driven from the same thread.
    page.evalOnSelector("section", "section => {\n" +
      "    const div = document.createElement('div');\n" +
      "    div.setAttribute('id', 'target3');\n" +
      "    section.appendChild(div);\n" +
      "  }");
    ElementHandle element = page.waitForSelector(":nth-match(div, 3)", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED));
    assertEquals("target3", element.evaluate("e => e.id"));
  }

  @PlaywrightTest
  @DisplayName("SE-139 should work with nth=")
  void shouldWorkWithNth(Page page) throws Exception {
    page.setContent("\n" +
      "    <section>\n" +
      "      <div id=target1></div>\n" +
      "      <div id=target2></div>\n" +
      "    </section>\n" +
      "  ");
    assertNull(page.querySelector("div >> nth=2"));
    assertEquals("target1", page.evalOnSelector("div >> nth=0", "e => e.id"));
    assertEquals("target2", page.evalOnSelector("div >> nth=1", "e => e.id"));
    assertEquals("target2", page.evalOnSelector("section > div >> nth=1", "e => e.id"));
    assertEquals("target1", page.evalOnSelector("section, div >> nth=1", "e => e.id"));
    assertEquals("target2", page.evalOnSelector("div, section >> nth=2", "e => e.id"));

    // Java: the wait runs after the page change, it cannot be pending while the page is driven from the same thread.
    page.evalOnSelector("section", "section => {\n" +
      "    const div = document.createElement('div');\n" +
      "    div.setAttribute('id', 'target3');\n" +
      "    section.appendChild(div);\n" +
      "  }");
    ElementHandle element = page.waitForSelector("div >> nth=2", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.ATTACHED));
    assertEquals("target3", element.evaluate("e => e.id"));

    page.setContent("\n" +
      "    <div>\n" +
      "      <div>\n" +
      "        <div>\n" +
      "          <span>hi</span>\n" +
      "          <span>hello</span>\n" +
      "        </div>\n" +
      "      </div>\n" +
      "    </div>\n" +
      "  ");
    assertEquals("hello", page.locator("div >> div >> span >> nth=1").textContent());
  }

  @PlaywrightTest
  @DisplayName("SE-140 should work with strict mode and chaining")
  void shouldWorkWithStrictModeAndChaining(Page page) {
    page.setContent("\n" +
      "    <div>\n" +
      "      <div>\n" +
      "        <div>\n" +
      "          <span>hi</span>\n" +
      "        </div>\n" +
      "      </div>\n" +
      "    </div>\n" +
      "  ");
    assertEquals("hi", page.locator("div >> div >> span").textContent());
  }

  @PlaywrightTest
  @DisplayName("SE-141 should work with layout selectors")
  void shouldWorkWithLayoutSelectors(Page page) {
    // Java: it.skip(trace === 'on') is dropped, tests never run with tracing on.

    /*

         +--+  +--+
         | 1|  | 2|
         +--+  ++-++
         | 3|   | 4|
    +-------+  ++-++
    |   0   |  | 5|
    | +--+  +--+--+
    | | 6|  | 7|
    | +--+  +--+
    |       |
    O-------+
            +--+
            | 8|
            +--++--+
                | 9|
                +--+

    */

    List<List<Integer>> boxes = asList(
      // x, y, width, height
      asList(0, 0, 150, 150),
      asList(100, 200, 50, 50),
      asList(200, 200, 50, 50),
      asList(100, 150, 50, 50),
      asList(201, 150, 50, 50),
      asList(200, 100, 50, 50),
      asList(50, 50, 50, 50),
      asList(150, 50, 50, 50),
      asList(150, -51, 50, 50),
      asList(201, -101, 50, 50));
    page.setContent("<container style=\"width: 500px; height: 500px; position: relative;\"></container>");
    page.evalOnSelector("container", "(container, boxes) => {\n" +
      "    for (let i = 0; i < boxes.length; i++) {\n" +
      "      const div = document.createElement('div');\n" +
      "      div.style.position = 'absolute';\n" +
      "      div.style.overflow = 'hidden';\n" +
      "      div.style.boxSizing = 'border-box';\n" +
      "      div.style.border = '1px solid black';\n" +
      "      div.id = 'id' + i;\n" +
      "      div.textContent = 'id' + i;\n" +
      "      const box = boxes[i];\n" +
      "      div.style.left = box[0] + 'px';\n" +
      "      // Note that top is a flipped y coordinate.\n" +
      "      div.style.top = (250 - box[1] - box[3]) + 'px';\n" +
      "      div.style.width = box[2] + 'px';\n" +
      "      div.style.height = box[3] + 'px';\n" +
      "      container.appendChild(div);\n" +
      "      const span = document.createElement('span');\n" +
      "      span.textContent = '' + i;\n" +
      "      div.appendChild(span);\n" +
      "    }\n" +
      "  }", boxes);

    String ids = "els => els.map(e => e.id).join(',')";
    assertEquals("id7", page.evalOnSelector("div:right-of(#id6)", "e => e.id"));
    assertEquals("id2", page.evalOnSelector("div:right-of(#id1)", "e => e.id"));
    assertEquals("id4", page.evalOnSelector("div:right-of(#id3)", "e => e.id"));
    assertNull(page.querySelector("div:right-of(#id4)"));
    assertEquals("id7", page.evalOnSelector("div:right-of(#id0)", "e => e.id"));
    assertEquals("id9", page.evalOnSelector("div:right-of(#id8)", "e => e.id"));
    assertEquals("id4,id2,id5,id7,id8,id9", page.evalOnSelectorAll("div:right-of(#id3)", ids));
    assertEquals("id2,id5,id7,id8", page.evalOnSelectorAll("div:right-of(#id3, 50)", ids));
    assertEquals("id7,id8", page.evalOnSelectorAll("div:right-of(#id3, 49)", ids));

    assertEquals("id1", page.evalOnSelector("div:left-of(#id2)", "e => e.id"));
    assertNull(page.querySelector("div:left-of(#id0)"));
    assertEquals("id0", page.evalOnSelector("div:left-of(#id5)", "e => e.id"));
    assertEquals("id8", page.evalOnSelector("div:left-of(#id9)", "e => e.id"));
    assertEquals("id3", page.evalOnSelector("div:left-of(#id4)", "e => e.id"));
    assertEquals("id0,id7,id3,id1,id6,id8", page.evalOnSelectorAll("div:left-of(#id5)", ids));
    assertEquals("id7,id8", page.evalOnSelectorAll("div:left-of(#id5, 3)", ids));

    assertEquals("id3", page.evalOnSelector("div:above(#id0)", "e => e.id"));
    assertEquals("id4", page.evalOnSelector("div:above(#id5)", "e => e.id"));
    assertEquals("id5", page.evalOnSelector("div:above(#id7)", "e => e.id"));
    assertEquals("id0", page.evalOnSelector("div:above(#id8)", "e => e.id"));
    assertEquals("id8", page.evalOnSelector("div:above(#id9)", "e => e.id"));
    assertNull(page.querySelector("div:above(#id2)"));
    assertEquals("id4,id2,id3,id1", page.evalOnSelectorAll("div:above(#id5)", ids));
    assertEquals("id4,id3", page.evalOnSelectorAll("div:above(#id5, 20)", ids));

    assertEquals("id5", page.evalOnSelector("div:below(#id4)", "e => e.id"));
    assertEquals("id0", page.evalOnSelector("div:below(#id3)", "e => e.id"));
    assertEquals("id4", page.evalOnSelector("div:below(#id2)", "e => e.id"));
    assertEquals("id8", page.evalOnSelector("div:below(#id6)", "e => e.id"));
    assertEquals("id8", page.evalOnSelector("div:below(#id7)", "e => e.id"));
    assertEquals("id9", page.evalOnSelector("div:below(#id8)", "e => e.id"));
    assertNull(page.querySelector("div:below(#id9)"));
    assertEquals("id0,id5,id6,id7,id8,id9", page.evalOnSelectorAll("div:below(#id3)", ids));
    assertEquals("id0,id5,id6,id7", page.evalOnSelectorAll("div:below(#id3, 105)", ids));

    assertEquals("id3", page.evalOnSelector("div:near(#id0)", "e => e.id"));
    assertEquals("id0,id5,id3,id6", page.evalOnSelectorAll("div:near(#id7)", ids));
    assertEquals("id3,id6,id7,id8,id1,id5", page.evalOnSelectorAll("div:near(#id0)", ids));
    assertEquals("id0,id3,id7", page.evalOnSelectorAll("div:near(#id6)", ids));
    assertEquals("id0", page.evalOnSelectorAll("div:near(#id6, 10)", ids));
    assertEquals("id3,id6,id7,id8,id1,id5,id4,id2", page.evalOnSelectorAll("div:near(#id0, 100)", ids));

    assertEquals("id7,id6", page.evalOnSelectorAll("div:below(#id5):above(#id8)", ids));
    assertEquals("id7", page.evalOnSelector("div:below(#id5):above(#id8)", "e => e.id"));

    assertEquals("id5,id6,id3", page.evalOnSelectorAll("div:right-of(#id0) + div:above(#id8)", ids));

    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.querySelector(":near(50)"));
    assertTrue(error.getMessage().contains("\"near\" engine expects a selector list and optional maximum distance in pixels"));
  }

  @PlaywrightTest
  @DisplayName("SE-142 should escape the scope with >>")
  void shouldEscapeTheScopeWith(Page page) {
    page.setContent("<div><label>Test</label><input id='myinput'></div>");
    assertEquals("myinput", page.evalOnSelector("label >> xpath=.. >> input", "e => e.id"));
  }

  @PlaywrightTest
  @DisplayName("SE-143 xpath should be relative")
  void xpathShouldBeRelative(Page page) {
    page.setContent("\n" +
      "    <span class=\"find-me\" id=target1>1</span>\n" +
      "    <div>\n" +
      "      <span class=\"find-me\" id=target2>2</span>\n" +
      "    </div>\n" +
      "  ");
    assertEquals("target1", page.evalOnSelector("//*[@class=\"find-me\"]", "e => e.id"));

    ElementHandle div = page.querySelector("div");
    assertEquals("target2", div.evalOnSelector("xpath=./*[@class=\"find-me\"]", "e => e.id"));
    assertEquals("target2", div.evalOnSelector("xpath=.//*[@class=\"find-me\"]", "e => e.id"));
    assertEquals("target2", div.evalOnSelector("//*[@class=\"find-me\"]", "e => e.id"));
    assertEquals("target2", div.evalOnSelector("xpath=/*[@class=\"find-me\"]", "e => e.id"));

    assertEquals("target2", page.evalOnSelector("div >> xpath=./*[@class=\"find-me\"]", "e => e.id"));
    assertEquals("target2", page.evalOnSelector("div >> xpath=.//*[@class=\"find-me\"]", "e => e.id"));
    assertEquals("target2", page.evalOnSelector("div >> //*[@class=\"find-me\"]", "e => e.id"));
    assertEquals("target2", page.evalOnSelector("div >> xpath=/*[@class=\"find-me\"]", "e => e.id"));
  }

  @PlaywrightTest
  @DisplayName("SE-144 should work with pipe in xpath")
  void shouldWorkWithPipeInXpath(Page page, TestServer server) {
    page.setContent("\n" +
      "    <span class=\"find-me\" id=t1>1</span>\n" +
      "    <div>\n" +
      "      <span class=\"find-me\" id=t2>2</span>\n" +
      "    </div>\n" +
      "    <div id=t3>3</span>\n" +
      "  ");
    assertEquals(2, page.evalOnSelectorAll("//*[@id=\"t1\"]|//*[@id=\"t3\"]", "els => els.length"));

    ElementHandle e1 = page.waitForSelector("//*[@id=\"t1\"]|//*[@id=\"t3\"]");
    assertNotNull(e1);
    assertEquals("t1", e1.evaluate("e => e.id"));

    ElementHandle e2 = page.waitForSelector("//*[@id=\"unknown\"]|//*[@id=\"t2\"]");
    assertNotNull(e2);
    assertEquals("t2", e2.evaluate("e => e.id"));

    page.click("//code|//span[@id=\"t2\"]");
  }

  @PlaywrightTest
  @DisplayName("SE-145 should print original xpath in error")
  @Fixme("java: the message has no call log with the escaped xpath, it is 'SyntaxError: Failed to execute 'evaluate' on 'Document': The string '//*[contains(@Class, 'foo']' is not a valid XPath expression.'")
  void shouldPrintOriginalXpathInError(Page page) {
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.locator("//*[contains(@Class, 'foo']").isVisible());
    assertTrue(error.getMessage().contains("//*[contains(@Class, \\'foo\\']"));
    assertFalse(error.getMessage().contains(".//*[contains(@Class, 'foo']"));
  }

  @PlaywrightTest
  @DisplayName("SE-146 data-testid on the handle should be relative")
  void dataTestidOnTheHandleShouldBeRelative(Page page) {
    page.setContent("\n" +
      "    <span data-testid=\"find-me\" id=target1>1</span>\n" +
      "    <div>\n" +
      "      <span data-testid=\"find-me\" id=target2>2</span>\n" +
      "    </div>\n" +
      "  ");
    assertEquals("target1", page.evalOnSelector("data-testid=find-me", "e => e.id"));

    ElementHandle div = page.querySelector("div");
    assertEquals("target2", div.evalOnSelector("data-testid=find-me", "e => e.id"));
    assertEquals("target2", page.evalOnSelector("div >> data-testid=find-me", "e => e.id"));
  }

  @PlaywrightTest
  @DisplayName("SE-147 should properly determine visibility of display:contents elements")
  void shouldProperlyDetermineVisibilityOfDisplayContentsElements(Page page) {
    // it.info().annotations.push({ type: 'issue', description: 'https://github.com/microsoft/playwright/issues/11202' });

    page.setContent("\n" +
      "    <div>\n" +
      "      <p style=\"display:contents\">DISPLAY CONTENTS</p>\n" +
      "    </div>");
    page.waitForSelector("\"DISPLAY CONTENTS\"");

    page.setContent("\n" +
      "    <div>\n" +
      "      <article style=\"display:contents\"><div>DISPLAY CONTENTS</div></article>\n" +
      "    </div>");
    page.waitForSelector("article");

    page.setContent("\n" +
      "    <div>\n" +
      "      <article style=\"display:contents\"><div style=\"display:contents\">DISPLAY CONTENTS</div></article>\n" +
      "    </div>");
    page.waitForSelector("article");

    page.setContent("\n" +
      "    <div>\n" +
      "      <article style=\"display:contents\"><div></div>DISPLAY CONTENTS<span></span></article>\n" +
      "    </div>");
    page.waitForSelector("article");

    page.setContent("\n" +
      "    <div>\n" +
      "      <article style=\"display:contents\"><div></div></article>\n" +
      "    </div>");
    page.waitForSelector("article", new Page.WaitForSelectorOptions().setState(WaitForSelectorState.HIDDEN));
  }

  @PlaywrightTest
  @DisplayName("SE-148 should work with internal:has=")
  void shouldWorkWithInternalHas(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/deep-shadow.html");
    assertEquals(2, page.evalOnSelectorAll("div >> internal:has=\"#target\"", "els => els.length"));
    assertEquals(3, page.evalOnSelectorAll("div >> internal:has=\"[data-testid=foo]\"", "els => els.length"));
    assertEquals(2, page.evalOnSelectorAll("div >> internal:has=\"[attr*=value]\"", "els => els.length"));

    page.setContent("<section><span></span><div></div></section><section><br></section>");
    assertEquals(1, page.evalOnSelectorAll("section >> internal:has=\"span, div\"", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("section >> internal:has=\"span, div\"", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("section >> internal:has=\"br\"", "els => els.length"));
    assertEquals(2, page.evalOnSelectorAll("section >> internal:has=\"span, br\"", "els => els.length"));
    assertEquals(2, page.evalOnSelectorAll("section >> internal:has=\"span, br, div\"", "els => els.length"));

    page.setContent("<div><span>hello</span></div><div><span>world</span></div>");
    assertEquals(1, page.evalOnSelectorAll("div >> internal:has=\"text=world\"", "els => els.length"));
    assertEquals("<div><span>world</span></div>", page.evalOnSelector("div >> internal:has=\"text=world\"", "e => e.outerHTML"));
    assertEquals(1, page.evalOnSelectorAll("div >> internal:has=\"text=\\\"hello\\\"\"", "els => els.length"));
    assertEquals("<div><span>hello</span></div>", page.evalOnSelector("div >> internal:has=\"text=\\\"hello\\\"\"", "e => e.outerHTML"));
    assertEquals(2, page.evalOnSelectorAll("div >> internal:has=\"xpath=./span\"", "els => els.length"));
    assertEquals(2, page.evalOnSelectorAll("div >> internal:has=\"span\"", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("div >> internal:has=\"span >> text=wor\"", "els => els.length"));
    assertEquals("<div><span>world</span></div>", page.evalOnSelector("div >> internal:has=\"span >> text=wor\"", "e => e.outerHTML"));
    assertEquals("<span>world</span>", page.evalOnSelector("div >> internal:has=\"span >> text=wor\" >> span", "e => e.outerHTML"));

    PlaywrightException error1 = assertThrows(PlaywrightException.class, () -> page.querySelector("div >> internal:has=abc"));
    assertTrue(error1.getMessage().contains("Malformed selector: internal:has=abc"));
    PlaywrightException error2 = assertThrows(PlaywrightException.class, () -> page.querySelector("internal:has=\"div\""));
    assertTrue(error2.getMessage().contains("\"internal:has\" selector cannot be first"));
    PlaywrightException error3 = assertThrows(PlaywrightException.class, () -> page.querySelector("div >> internal:has=33"));
    assertTrue(error3.getMessage().contains("Malformed selector: internal:has=33"));
    PlaywrightException error4 = assertThrows(PlaywrightException.class, () -> page.querySelector("div >> internal:has=\"span!\""));
    assertTrue(error4.getMessage().contains("Unexpected token \"!\" while parsing css selector \"span!\""));
  }

  @PlaywrightTest
  @DisplayName("SE-149 should work with internal:has-not=")
  void shouldWorkWithInternalHasNot(Page page) {
    page.setContent("<section><span></span><div></div></section><section><br></section>");
    assertEquals(1, page.evalOnSelectorAll("section >> internal:has-not=\"span\"", "els => els.length"));
    assertEquals(0, page.evalOnSelectorAll("section >> internal:has-not=\"span, div, br\"", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("section >> internal:has-not=\"br\"", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("section >> internal:has-not=\"span, div\"", "els => els.length"));
    assertEquals(2, page.evalOnSelectorAll("section >> internal:has-not=\"article\"", "els => els.length"));
  }

  @PlaywrightTest
  @DisplayName("SE-150 should work with internal:and=")
  void shouldWorkWithInternalAnd(Page page, TestServer server) {
    page.setContent("\n" +
      "    <div class=foo>hello</div><div class=bar>world</div>\n" +
      "    <span class=foo>hello2</span><span class=bar>world2</span>\n" +
      "  ");
    String texts = "els => els.map(e => e.textContent)";
    assertEquals(asList(), page.evalOnSelectorAll("div >> internal:and=\"span\"", texts));
    assertEquals(asList("hello"), page.evalOnSelectorAll("div >> internal:and=\".foo\"", texts));
    assertEquals(asList("world"), page.evalOnSelectorAll("div >> internal:and=\".bar\"", texts));
    assertEquals(asList("hello2", "world2"), page.evalOnSelectorAll("span >> internal:and=\"span\"", texts));
    assertEquals(asList("hello"), page.evalOnSelectorAll(".foo >> internal:and=\"div\"", texts));
    assertEquals(asList("world2"), page.evalOnSelectorAll(".bar >> internal:and=\"span\"", texts));
  }

  @PlaywrightTest
  @DisplayName("SE-151 should work with internal:or=")
  void shouldWorkWithInternalOr(Page page, TestServer server) {
    page.setContent("\n" +
      "    <div>hello</div>\n" +
      "    <span>world</span>\n" +
      "  ");
    String texts = "els => els.map(e => e.textContent)";
    assertEquals(asList("hello", "world"), page.evalOnSelectorAll("div >> internal:or=\"span\"", texts));
    assertEquals(asList("hello", "world"), page.evalOnSelectorAll("span >> internal:or=\"div\"", texts));
    assertEquals(0, page.evalOnSelectorAll("article >> internal:or=\"something\"", "els => els.length"));
    assertEquals("hello", page.locator("article >> internal:or=\"div\"").textContent());
    assertEquals("world", page.locator("article >> internal:or=\"span\"").textContent());
    assertEquals("hello", page.locator("div >> internal:or=\"article\"").textContent());
    assertEquals("world", page.locator("span >> internal:or=\"article\"").textContent());
  }

  @PlaywrightTest
  @DisplayName("SE-152 should work with internal:chain=")
  void shouldWorkWithInternalChain(Page page, TestServer server) {
    page.setContent("\n" +
      "    <div>one <span>two</span> <button>three</button> </div>\n" +
      "    <span>four</span>\n" +
      "    <button>five</button>\n" +
      "  ");
    String texts = "els => els.map(e => e.textContent)";
    assertEquals(asList("three"), page.evalOnSelectorAll("div >> internal:chain=\"button\"", texts));
    assertEquals(asList("two", "three"), page.evalOnSelectorAll("div >> internal:chain=\"span >> internal:or=\\\"button\\\"\"", texts));
  }

  @PlaywrightTest
  @DisplayName("SE-153 chaining should work with large DOM @smoke")
  void chainingShouldWorkWithLargeDom(Page page, TestServer server) {
    page.evaluate("() => {\n" +
      "    let last = document.body;\n" +
      "    for (let i = 0; i < 100; i++) {\n" +
      "      const e = document.createElement('div');\n" +
      "      last.appendChild(e);\n" +
      "      last = e;\n" +
      "    }\n" +
      "    const target = document.createElement('span');\n" +
      "    target.textContent = 'Found me!';\n" +
      "    last.appendChild(target);\n" +
      "  }");

    // Naive implementation generates C(100, 9) ~= 1.9*10^12 entries.
    List<String> selectors = asList(
      "div >> div >> div >> div >> div >> div >> div >> div >> span",
      "div div div div div div div div span",
      "div div >> div div >> div div >> div div >> span");

    List<Object> counts = new ArrayList<>();
    List<Object> times = new ArrayList<>();
    for (String selector : selectors) {
      long time = System.currentTimeMillis();
      counts.add(page.evalOnSelectorAll(selector, "els => els.length"));
      times.add(mapOf("selector", selector, "time", System.currentTimeMillis() - time));
    }
    assertEquals(asList(1, 1, 1), counts);
    // Uncomment to see performance results.
    // console.log(times);
  }
}
