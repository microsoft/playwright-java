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
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/selectors-text.spec.ts. */
public class TestSelectorsText {

  @PlaywrightTest
  @DisplayName("SE-028 should work @smoke")
  void shouldWork(Page page) {
    page.setContent("<div>yo</div><div>ya</div><div>\nye  </div>");
    assertEquals("<div>ya</div>", page.evalOnSelector("text=ya", "e => e.outerHTML"));
    assertEquals("<div>ya</div>", page.evalOnSelector("text=\"ya\"", "e => e.outerHTML"));
    assertEquals("<div>ya</div>", page.evalOnSelector("text=/^[ay]+$/", "e => e.outerHTML"));
    assertEquals("<div>ya</div>", page.evalOnSelector("text=/Ya/i", "e => e.outerHTML"));
    assertEquals("<div>\nye  </div>", page.evalOnSelector("text=ye", "e => e.outerHTML"));
    assertTrue(((String) page.getByText("ye").evaluate("e => e.outerHTML")).contains(">\nye  </div>"));

    page.setContent("<div> ye </div><div>ye</div>");
    assertEquals("<div> ye </div>", page.evalOnSelector("text=\"ye\"", "e => e.outerHTML"));
    assertTrue(((String) page.getByText("ye", new Page.GetByTextOptions().setExact(true)).first().evaluate("e => e.outerHTML")).contains("> ye </div>"));

    page.setContent("<div>yo</div><div>\"ya</div><div> hello world! </div>");
    assertEquals("<div>\"ya</div>", page.evalOnSelector("text=\"\\\"ya\"", "e => e.outerHTML"));
    assertEquals("<div> hello world! </div>", page.evalOnSelector("text=/hello/", "e => e.outerHTML"));
    assertEquals("<div> hello world! </div>", page.evalOnSelector("text=/^\\s*heLLo/i", "e => e.outerHTML"));

    page.setContent("<div>yo<div>ya</div>hey<div>hey</div></div>");
    assertEquals("<div>hey</div>", page.evalOnSelector("text=hey", "e => e.outerHTML"));
    assertEquals("<div>ya</div>", page.evalOnSelector("text=yo>>text=\"ya\"", "e => e.outerHTML"));
    assertEquals("<div>ya</div>", page.evalOnSelector("text=yo>> text=\"ya\"", "e => e.outerHTML"));
    assertEquals("<div>ya</div>", page.evalOnSelector("text=yo >>text='ya'", "e => e.outerHTML"));
    assertEquals("<div>ya</div>", page.evalOnSelector("text=yo >> text='ya'", "e => e.outerHTML"));
    assertEquals("<div>ya</div>", page.evalOnSelector("'yo'>>\"ya\"", "e => e.outerHTML"));
    assertEquals("<div>ya</div>", page.evalOnSelector("\"yo\" >> 'ya'", "e => e.outerHTML"));

    page.setContent("<div>yo<span id=\"s1\"></span></div><div>yo<span id=\"s2\"></span><span id=\"s3\"></span></div>");
    assertEquals("<div>yo<span id=\"s1\"></span></div>\n<div>yo<span id=\"s2\"></span><span id=\"s3\"></span></div>", page.evalOnSelectorAll("text=yo", "es => es.map(e => e.outerHTML).join('\\n')"));

    page.setContent("<div>'</div><div>\"</div><div>\\</div><div>x</div>");
    assertEquals("<div>'</div>", page.evalOnSelector("text='\\''", "e => e.outerHTML"));
    assertEquals("<div>\"</div>", page.evalOnSelector("text='\"'", "e => e.outerHTML"));
    assertEquals("<div>\"</div>", page.evalOnSelector("text=\"\\\"\"", "e => e.outerHTML"));
    assertEquals("<div>'</div>", page.evalOnSelector("text=\"'\"", "e => e.outerHTML"));
    assertEquals("<div>x</div>", page.evalOnSelector("text=\"\\x\"", "e => e.outerHTML"));
    assertEquals("<div>x</div>", page.evalOnSelector("text='\\x'", "e => e.outerHTML"));
    assertEquals("<div>\\</div>", page.evalOnSelector("text='\\\\'", "e => e.outerHTML"));
    assertEquals("<div>\\</div>", page.evalOnSelector("text=\"\\\\\"", "e => e.outerHTML"));
    assertEquals("<div>\"</div>", page.evalOnSelector("text=\"", "e => e.outerHTML"));
    assertEquals("<div>'</div>", page.evalOnSelector("text='", "e => e.outerHTML"));
    assertEquals("<div>x</div>", page.evalOnSelector("\"x\"", "e => e.outerHTML"));
    assertEquals("<div>x</div>", page.evalOnSelector("'x'", "e => e.outerHTML"));
    assertThrows(PlaywrightException.class, () -> page.querySelector("\""));
    assertThrows(PlaywrightException.class, () -> page.querySelector("'"));

    page.setContent("<div> ' </div><div> \" </div>");
    assertEquals("<div> \" </div>", page.evalOnSelector("text=\"", "e => e.outerHTML"));
    assertEquals("<div> ' </div>", page.evalOnSelector("text='", "e => e.outerHTML"));

    page.setContent("<div>Hi''&gt;&gt;foo=bar</div>");
    assertEquals("<div>Hi''&gt;&gt;foo=bar</div>", page.evalOnSelector("text=\"Hi''>>foo=bar\"", "e => e.outerHTML"));
    page.setContent("<div>Hi'\"&gt;&gt;foo=bar</div>");
    assertEquals("<div>Hi'\"&gt;&gt;foo=bar</div>", page.evalOnSelector("text=\"Hi'\\\">>foo=bar\"", "e => e.outerHTML"));

    page.setContent("<div>Hi&gt;&gt;<span></span></div>");
    assertEquals("<span></span>", page.evalOnSelector("text=\"Hi>>\">>span", "e => e.outerHTML"));
    assertEquals("<span></span>", page.evalOnSelector("text=/Hi\\>\\>/ >> span", "e => e.outerHTML"));

    page.setContent("<div>a<br>b</div><div>a</div>");
    assertEquals("<div>a<br>b</div>", page.evalOnSelector("text=a", "e => e.outerHTML"));
    assertEquals("<div>a<br>b</div>", page.evalOnSelector("text=b", "e => e.outerHTML"));
    assertEquals("<div>a<br>b</div>", page.evalOnSelector("text=ab", "e => e.outerHTML"));
    assertNull(page.querySelector("text=abc"));
    assertEquals(2, page.evalOnSelectorAll("text=a", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("text=b", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("text=ab", "els => els.length"));
    assertEquals(0, page.evalOnSelectorAll("text=abc", "els => els.length"));

    page.setContent("<div></div><span></span>");
    page.evalOnSelector("div", "div => {\n" +
      "  div.appendChild(document.createTextNode('hello'));\n" +
      "  div.appendChild(document.createTextNode('world'));\n" +
      "}");
    page.evalOnSelector("span", "span => {\n" +
      "  span.appendChild(document.createTextNode('hello'));\n" +
      "  span.appendChild(document.createTextNode('world'));\n" +
      "}");
    assertEquals("<div>helloworld</div>", page.evalOnSelector("text=lowo", "e => e.outerHTML"));
    assertEquals("<div>helloworld</div><span>helloworld</span>", page.evalOnSelectorAll("text=lowo", "els => els.map(e => e.outerHTML).join('')"));

    page.setContent("<span>Sign&nbsp;in</span><span>Hello\n \nworld</span>");
    assertEquals("<span>Sign&nbsp;in</span>", page.evalOnSelector("text=Sign in", "e => e.outerHTML"));
    assertEquals(1, page.querySelectorAll("text=Sign \tin").size());
    assertEquals(1, page.querySelectorAll("text=\"Sign in\"").size());
    assertEquals("<span>Hello\n \nworld</span>", page.evalOnSelector("text=lo wo", "e => e.outerHTML"));
    assertEquals("<span>Hello\n \nworld</span>", page.evalOnSelector("text=\"Hello world\"", "e => e.outerHTML"));
    assertNull(page.querySelector("text=\"lo wo\""));
    assertEquals(1, page.querySelectorAll("text=lo \nwo").size());
    assertEquals(0, page.querySelectorAll("text=\"lo \nwo\"").size());

    page.setContent("<div>let's<span>hello</span></div>");
    assertEquals("<span>hello</span>", page.evalOnSelector("text=/let's/i >> span", "e => e.outerHTML"));
    assertEquals("<span>hello</span>", page.evalOnSelector("text=/let\\'s/i >> span", "e => e.outerHTML"));
  }

  @PlaywrightTest
  @DisplayName("SE-029 should work with :text")
  void shouldWorkWithText(Page page) {
    page.setContent("<div>yo</div><div>ya</div><div>\nHELLO   \n world  </div>");
    assertEquals("<div>ya</div>", page.evalOnSelector(":text(\"ya\")", "e => e.outerHTML"));
    assertEquals("<div>ya</div>", page.evalOnSelector(":text-is(\"ya\")", "e => e.outerHTML"));
    assertEquals("<div>yo</div>", page.evalOnSelector(":text(\"y\")", "e => e.outerHTML"));
    assertNull(page.querySelector(":text-is(\"Y\")"));
    assertEquals("<div>\nHELLO   \n world  </div>", page.evalOnSelector(":text(\"hello world\")", "e => e.outerHTML"));
    assertEquals("<div>\nHELLO   \n world  </div>", page.evalOnSelector(":text-is(\"HELLO world\")", "e => e.outerHTML"));
    assertEquals("<div>\nHELLO   \n world  </div>", page.evalOnSelector(":text(\"lo wo\")", "e => e.outerHTML"));
    assertNull(page.querySelector(":text-is(\"lo wo\")"));
    assertEquals("<div>ya</div>", page.evalOnSelector(":text-matches(\"^[ay]+$\")", "e => e.outerHTML"));
    assertEquals("<div>yo</div>", page.evalOnSelector(":text-matches(\"y\", \"g\")", "e => e.outerHTML"));
    assertEquals("<div>yo</div>", page.evalOnSelector(":text-matches(\"Y\", \"i\")", "e => e.outerHTML"));
    assertNull(page.querySelector(":text-matches(\"^y$\")"));

    PlaywrightException error1 = assertThrows(PlaywrightException.class, () -> page.querySelector(":text(\"foo\", \"bar\")"));
    assertTrue(error1.getMessage().contains("\"text\" engine expects a single string"));
    PlaywrightException error2 = assertThrows(PlaywrightException.class, () -> page.querySelector(":text(foo > bar)"));
    assertTrue(error2.getMessage().contains("\"text\" engine expects a single string"));
  }

  @PlaywrightTest
  @DisplayName("SE-030 should support empty string")
  void shouldSupportEmptyString(Page page) {
    page.setContent("<div></div><div>ya</div><div>\nHELLO   \n world  </div>");
    assertEquals("<div></div>", page.evalOnSelector("div:text-is(\"\")", "e => e.outerHTML"));
    assertEquals(1, page.evalOnSelectorAll("div:text-is(\"\")", "els => els.length"));
    assertEquals("<div></div>", page.evalOnSelector("div:text(\"\")", "e => e.outerHTML"));
    assertEquals(3, page.evalOnSelectorAll("div:text(\"\")", "els => els.length"));
    assertEquals("<div></div>", page.evalOnSelector("div >> text=\"\"", "e => e.outerHTML"));
    assertEquals(1, page.evalOnSelectorAll("div >> text=\"\"", "els => els.length"));
    assertEquals("<div></div>", page.evalOnSelector("div >> text=/^$/", "e => e.outerHTML"));
    assertEquals(1, page.evalOnSelectorAll("div >> text=/^$/", "els => els.length"));
    assertEquals("<div></div>", page.evalOnSelector("div:text-matches(\"\")", "e => e.outerHTML"));
    assertEquals(3, page.evalOnSelectorAll("div:text-matches(\"\")", "els => els.length"));
  }

  @PlaywrightTest
  @DisplayName("SE-031 should work across nodes")
  void shouldWorkAcrossNodes(Page page) {
    page.setContent("<div id=target1>Hello<i>,</i> <span id=target2>world</span><b>!</b></div>");

    assertEquals("target1", page.evalOnSelector(":text(\"Hello, world!\")", "e => e.id"));
    assertEquals("target1", page.evalOnSelector(":text(\"Hello\")", "e => e.id"));
    assertEquals("target2", page.evalOnSelector(":text(\"world\")", "e => e.id"));
    assertEquals(1, page.evalOnSelectorAll(":text(\"world\")", "els => els.length"));
    assertNull(page.querySelector(":text(\"hello world\")"));
    assertNull(page.querySelector("div:text(\"world\")"));
    assertEquals("target1", page.evalOnSelector("text=Hello, world!", "e => e.id"));
    assertEquals("target1", page.evalOnSelector("text=Hello", "e => e.id"));
    assertEquals("target2", page.evalOnSelector("text=world", "e => e.id"));
    assertEquals(1, page.evalOnSelectorAll("text=world", "els => els.length"));
    assertNull(page.querySelector("text=hello world"));

    assertNull(page.querySelector(":text-is(\"Hello, world!\")"));
    assertEquals("target1", page.evalOnSelector(":text-is(\"Hello\")", "e => e.id"));
    assertEquals("target2", page.evalOnSelector(":text-is(\"world\")", "e => e.id"));
    assertEquals(1, page.evalOnSelectorAll(":text-is(\"world\")", "els => els.length"));
    assertNull(page.querySelector("text=\"Hello, world!\""));
    assertEquals("target1", page.evalOnSelector("text=\"Hello\"", "e => e.id"));
    assertEquals("target2", page.evalOnSelector("text=\"world\"", "e => e.id"));
    assertEquals(1, page.evalOnSelectorAll("text=\"world\"", "els => els.length"));

    assertEquals("I", page.evalOnSelector(":text-matches(\".*\")", "e => e.nodeName"));
    assertEquals("target2", page.evalOnSelector(":text-matches(\"world?\")", "e => e.id"));
    assertEquals(1, page.evalOnSelectorAll(":text-matches(\"world\")", "els => els.length"));
    assertNull(page.querySelector("div:text(\".*\")"));
    assertEquals("I", page.evalOnSelector("text=/.*/", "e => e.nodeName"));
    assertEquals("target2", page.evalOnSelector("text=/world?/", "e => e.id"));
    assertEquals(1, page.evalOnSelectorAll("text=/world/", "els => els.length"));
  }

  @PlaywrightTest
  @DisplayName("SE-032 text-is() should ignore comments")
  void textisShouldIgnoreComments(Page page) {
    page.setContent("<div id=me>hel<!-- comment -->lo\n" +
      "  <!-- comment -->\n" +
      "  world</div>");
    assertEquals("me", page.evalOnSelector(":text-is(\"hello world\")", "e => e.id"));
    assertEquals("me", page.locator("div", new Page.LocatorOptions().setHasText("hello world")).getAttribute("id"));
    assertEquals("me", page.getByText("hello world", new Page.GetByTextOptions().setExact(true)).getAttribute("id"));
  }

  @PlaywrightTest
  @DisplayName("SE-033 should work with text nodes in quoted mode")
  void shouldWorkWithTextNodesInQuotedMode(Page page) {
    page.setContent("<div id=target1>Hello<span id=target2>wo  rld  </span>  Hi again  </div>");
    assertEquals("target1", page.evalOnSelector("text=\"Hello\"", "e => e.id"));
    assertEquals("target1", page.evalOnSelector("text=\"Hi again\"", "e => e.id"));
    assertEquals("target2", page.evalOnSelector("text=\"wo rld\"", "e => e.id"));
    assertNull(page.querySelector("text=\"Hellowo rld Hi again\""));
    assertNull(page.querySelector("text=\"Hellowo\""));
    assertNull(page.querySelector("text=\"Hellowo rld\""));
    assertNull(page.querySelector("text=\"wo rld Hi ag\""));
    assertNull(page.querySelector("text=\"again\""));
    assertNull(page.querySelector("text=\"hi again\""));
    assertEquals("target1", page.evalOnSelector("text=hi again", "e => e.id"));
  }

  @PlaywrightTest
  @DisplayName("SE-034 should clear caches")
  void shouldClearCaches(Page page) {
    page.setContent("<div id=target1>text</div><div id=target2>text</div>");
    ElementHandle div = page.querySelector("#target1");

    div.evaluate("div => div.textContent = 'text'");
    assertEquals("target1", page.evalOnSelector("text=text", "e => e.id"));
    div.evaluate("div => div.textContent = 'foo'");
    assertEquals("target2", page.evalOnSelector("text=text", "e => e.id"));

    div.evaluate("div => div.textContent = 'text'");
    assertEquals("target1", page.evalOnSelector(":text(\"text\")", "e => e.id"));
    div.evaluate("div => div.textContent = 'foo'");
    assertEquals("target2", page.evalOnSelector(":text(\"text\")", "e => e.id"));

    div.evaluate("div => div.textContent = 'text'");
    assertEquals(2, page.evalOnSelectorAll("text=text", "els => els.length"));
    div.evaluate("div => div.textContent = 'foo'");
    assertEquals(1, page.evalOnSelectorAll("text=text", "els => els.length"));

    div.evaluate("div => div.textContent = 'text'");
    assertEquals(2, page.evalOnSelectorAll(":text(\"text\")", "els => els.length"));
    div.evaluate("div => div.textContent = 'foo'");
    assertEquals(1, page.evalOnSelectorAll(":text(\"text\")", "els => els.length"));
  }

  @PlaywrightTest
  @DisplayName("SE-035 should work with :has-text")
  void shouldWorkWithHastext(Page page) {
    page.setContent("\n" +
      "    <input id=input2>\n" +
      "    <div id=div1>\n" +
      "      <span>  Find me  </span>\n" +
      "      or\n" +
      "      <wrap><span id=span2>maybe me  </span></wrap>\n" +
      "      <div><input id=input1></div>\n" +
      "    </div>\n" +
      "  ");
    assertEquals("HTML", page.evalOnSelector(":has-text(\"find me\")", "e => e.tagName"));
    assertEquals("<span>  Find me  </span>", page.evalOnSelector("span:has-text(\"find me\")", "e => e.outerHTML"));
    assertEquals("div1", page.evalOnSelector("div:has-text(\"find me\")", "e => e.id"));
    assertEquals("input1", page.evalOnSelector("div:has-text(\"find me\") input", "e => e.id"));
    assertEquals("input2", page.evalOnSelector(":has-text(\"find me\") input", "e => e.id"));
    assertEquals("div1", page.evalOnSelector("div:has-text(\"find me or maybe me\")", "e => e.id"));
    assertNull(page.querySelector("div:has-text(\"find noone\")"));
    assertEquals("div1;span2", page.evalOnSelectorAll(":is(div,span):has-text(\"maybe\")", "els => els.map(e => e.id).join(';')"));
    assertEquals("WRAP", page.evalOnSelector("div:has-text(\"find me\") :has-text(\"maybe me\")", "e => e.tagName"));
    assertEquals("span2", page.evalOnSelector("div:has-text(\"find me\") span:has-text(\"maybe me\")", "e => e.id"));

    page.setContent("<div id=me>hello\n" +
      "  wo\"r>>ld</div>");
    assertEquals("me", page.evalOnSelector("div:has-text(\"hello wo\\\"r>>ld\")", "e => e.id"));
    assertEquals("me", page.evalOnSelector("div:has-text(\"hello\\a wo\\\"r>>ld\")", "e => e.id"));
    assertEquals("me", page.locator("div", new Page.LocatorOptions().setHasText("hello\nwo\"r>>ld")).getAttribute("id"));

    PlaywrightException error1 = assertThrows(PlaywrightException.class, () -> page.querySelector(":has-text(\"foo\", \"bar\")"));
    assertTrue(error1.getMessage().contains("\"has-text\" engine expects a single string"));
    PlaywrightException error2 = assertThrows(PlaywrightException.class, () -> page.querySelector(":has-text(foo > bar)"));
    assertTrue(error2.getMessage().contains("\"has-text\" engine expects a single string"));
  }

  @PlaywrightTest
  @DisplayName("SE-036 should work with large DOM")
  void shouldWorkWithLargeDOM(Page page) {
    page.evaluate("() => {\n" +
      "  let id = 0;\n" +
      "  const next = (tag) => {\n" +
      "    const e = document.createElement(tag);\n" +
      "    const eid = ++id;\n" +
      "    e.textContent = 'id' + eid;\n" +
      "    e.id = 'id' + eid;\n" +
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
      ":has-text(\"id18\")",
      ":has-text(\"id12345\")",
      ":has-text(\"id\")",
      ":text(\"id18\")",
      ":text(\"id12345\")",
      ":text(\"id\")",
      ":text-matches(\"id12345\", \"i\")",
      "text=id18",
      "text=id12345",
      "text=id",
      "#id18",
      "#id12345",
      "*");

    boolean measure = false;
    for (String selector : selectors) {
      long time1 = System.currentTimeMillis();
      for (int i = 0; i < (measure ? 10 : 1); i++)
        page.evalOnSelectorAll(selector, "els => els.length");
      if (measure) {
        // console.log(`pw("${selector}"): ` + (Date.now() - time1));
      }

      if (measure && !selector.contains("text")) {
        long time2 = System.currentTimeMillis();
        for (int i = 0; i < (measure ? 10 : 1); i++)
          page.evaluate("selector => document.querySelectorAll(selector).length", selector);
        // console.log(`qs("${selector}"): ` + (Date.now() - time2));
      }
    }
  }

  @PlaywrightTest
  @DisplayName("SE-037 should be case sensitive if quotes are specified")
  void shouldBeCaseSensitiveIfQuotesAreSpecified(Page page) {
    page.setContent("<div>yo</div><div>ya</div><div>\nye  </div>");
    assertEquals("<div>ya</div>", page.evalOnSelector("text=yA", "e => e.outerHTML"));
    assertNull(page.querySelector("text=\"yA\""));
    assertNull(page.querySelector("text= \"ya\""));
  }

  @PlaywrightTest
  @DisplayName("SE-038 should search for a substring without quotes")
  void shouldSearchForASubstringWithoutQuotes(Page page) {
    page.setContent("<div>textwithsubstring</div>");
    assertEquals("<div>textwithsubstring</div>", page.evalOnSelector("text=with", "e => e.outerHTML"));
    assertNull(page.querySelector("text=\"with\""));
  }

  @PlaywrightTest
  @DisplayName("SE-039 should skip head, script and style")
  void shouldSkipHeadScriptAndStyle(Page page) {
    page.setContent("\n" +
      "    <head>\n" +
      "      <title>title</title>\n" +
      "      <script>var script</script>\n" +
      "      <style>.style {}</style>\n" +
      "    </head>\n" +
      "    <body>\n" +
      "      <script>var script</script>\n" +
      "      <style>.style {}</style>\n" +
      "      <div>title script style</div>\n" +
      "    </body>");
    ElementHandle head = page.querySelector("head");
    ElementHandle title = page.querySelector("title");
    ElementHandle script = page.querySelector("body script");
    ElementHandle style = page.querySelector("body style");
    for (String text : Arrays.asList("title", "script", "style")) {
      assertEquals("DIV", page.evalOnSelector("text=" + text, "e => e.nodeName"));
      assertEquals("DIV", page.evalOnSelectorAll("text=" + text, "els => els.map(e => e.nodeName).join('|')"));
      for (ElementHandle root : Arrays.asList(head, title, script, style)) {
        assertNull(root.querySelector("text=" + text));
        assertEquals(0, root.evalOnSelectorAll("text=" + text, "els => els.length"));
      }
    }
  }

  @PlaywrightTest
  @DisplayName("SE-040 should match input[type=button|submit|reset]")
  void shouldMatchInputtypebuttonsubmitreset(Page page) {
    page.setContent("<input type=\"submit\" value=\"hello\"><input type=\"button\" value=\"world\"><input type=\"reset\" value=\"clear\">");
    assertEquals("<input type=\"submit\" value=\"hello\">", page.evalOnSelector("text=hello", "e => e.outerHTML"));
    assertEquals("<input type=\"button\" value=\"world\">", page.evalOnSelector("text=world", "e => e.outerHTML"));
    assertEquals("<input type=\"reset\" value=\"clear\">", page.evalOnSelector("text=clear", "e => e.outerHTML"));
  }

  @PlaywrightTest
  @DisplayName("SE-041 should work for open shadow roots")
  void shouldWorkForOpenShadowRoots(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/deep-shadow.html");
    assertEquals("Hello from root1", page.evalOnSelector("text=root1", "e => e.textContent"));
    assertEquals("Hello from root2", page.evalOnSelector("text=root2", "e => e.textContent"));
    assertEquals("Hello from root3", page.evalOnSelector("text=root3", "e => e.textContent"));
    assertEquals("Hello from root3", page.evalOnSelector("#root1 >> text=from root3", "e => e.textContent"));
    assertEquals("Hello from root2", page.evalOnSelector("#target >> text=from root2", "e => e.textContent"));
    assertNull(page.querySelector("text:light=root1"));
    assertNull(page.querySelector("text:light=root2"));
    assertNull(page.querySelector("text:light=root3"));
  }

  @PlaywrightTest
  @DisplayName("SE-042 should prioritize light dom over shadow dom in the same parent")
  void shouldPrioritizeLightDomOverShadowDomInTheSameParent(Page page, TestServer server) {
    page.evaluate("() => {\n" +
      "  const div = document.createElement('div');\n" +
      "  document.body.appendChild(div);\n" +
      "\n" +
      "  div.attachShadow({ mode: 'open' });\n" +
      "  const shadowSpan = document.createElement('span');\n" +
      "  shadowSpan.textContent = 'Hello from shadow';\n" +
      "  div.shadowRoot.appendChild(shadowSpan);\n" +
      "\n" +
      "  const lightSpan = document.createElement('span');\n" +
      "  lightSpan.textContent = 'Hello from light';\n" +
      "  div.appendChild(lightSpan);\n" +
      "}");
    assertEquals("Hello from light", page.evalOnSelector("div >> text=Hello", "e => e.textContent"));
  }

  @PlaywrightTest
  @DisplayName("SE-043 should waitForSelector with distributed elements")
  void shouldWaitForSelectorWithDistributedElements(Page page, TestServer server) throws Exception {
    page.evaluate("() => {\n" +
      "  const div = document.createElement('div');\n" +
      "  document.body.appendChild(div);\n" +
      "\n" +
      "  div.attachShadow({ mode: 'open' });\n" +
      "  const shadowSpan = document.createElement('span');\n" +
      "  shadowSpan.textContent = 'Hello from shadow';\n" +
      "  div.shadowRoot.appendChild(shadowSpan);\n" +
      "  div.shadowRoot.appendChild(document.createElement('slot'));\n" +
      "\n" +
      "  const lightSpan = document.createElement('span');\n" +
      "  lightSpan.textContent = 'Hello from light';\n" +
      "  div.appendChild(lightSpan);\n" +
      "}");
    // Java: waitForSelector keeps its result once the element exists, so it runs after the evaluate.
    ElementHandle handle = page.waitForSelector("div >> text=Hello");
    assertEquals("Hello from light", handle.textContent());
  }

  @PlaywrightTest
  @DisplayName("SE-044 should match root after >>")
  void shouldMatchRootAfter(Page page, TestServer server) {
    page.setContent("<section>test</section>");
    ElementHandle element = page.querySelector("css=section >> text=test");
    assertNotNull(element);
    ElementHandle element2 = page.querySelector("text=test >> text=test");
    assertNotNull(element2);
  }

  @PlaywrightTest
  @DisplayName("SE-045 should match root after >> with *")
  void shouldMatchRootAfterWith(Page page) {
    page.setContent("<button> hello world </button> <button> hellow <span> world </span> </button>");
    assertEquals(2, page.evalOnSelectorAll("*css=button >> text=hello >> text=world", "els => els.length"));
  }

  @PlaywrightTest
  @DisplayName("SE-046 should work with leading and trailing spaces")
  void shouldWorkWithLeadingAndTrailingSpaces(Page page) {
    page.setContent("<button> Add widget </button>");
    assertThat(page.locator("text=Add widget")).isVisible();
    assertThat(page.locator("text= Add widget ")).isVisible();
  }

  @PlaywrightTest
  @DisplayName("SE-047 should work with unpaired quotes when not at the start")
  void shouldWorkWithUnpairedQuotesWhenNotAtTheStart(Page page) {
    page.setContent("\n" +
      "    <div>hello\"world<span>yay</span></div>\n" +
      "    <div>hello'world<span>nay</span></div>\n" +
      "    <div>hello`world<span>oh</span></div>\n" +
      "    <div>hello`world<span>oh2</span></div>\n" +
      "  ");
    assertEquals("<span>yay</span>", page.evalOnSelector("text=lo\" >> span", "e => e.outerHTML"));
    assertEquals("<span>yay</span>", page.evalOnSelector("  text=lo\" >> span", "e => e.outerHTML"));
    assertEquals("<span>yay</span>", page.evalOnSelector("text  =lo\" >> span", "e => e.outerHTML"));
    assertEquals("<span>yay</span>", page.evalOnSelector("text=  lo\" >> span", "e => e.outerHTML"));
    assertEquals("<span>yay</span>", page.evalOnSelector(" text = lo\" >> span", "e => e.outerHTML"));
    assertEquals("<span>yay</span>", page.evalOnSelector("text=o\"wor >> span", "e => e.outerHTML"));

    assertEquals("<span>nay</span>", page.evalOnSelector("text=lo'wor >> span", "e => e.outerHTML"));
    assertEquals("<span>nay</span>", page.evalOnSelector("text=o' >> span", "e => e.outerHTML"));

    assertEquals("<span>oh</span>", page.evalOnSelector("text=ello`wor >> span", "e => e.outerHTML"));
    assertThat(page.locator("text=ello`wor").locator("span").first()).hasText("oh");
    assertThat(page.locator("text=ello`wor").locator("span").nth(1)).hasText("oh2");

    assertNull(page.querySelector("text='wor >> span"));
    assertNull(page.querySelector("text=\" >> span"));
    assertNull(page.querySelector("text=` >> span"));
  }

  @PlaywrightTest
  @DisplayName("SE-048 should work with paired quotes in the middle of selector")
  void shouldWorkWithPairedQuotesInTheMiddleOfSelector(Page page) {
    page.setContent("<div>pattern \"^-?\\d+$\"</div>");
    page.locator("div >> text=pattern \"^-?\\d+$").isVisible();
    page.locator("div >> text=pattern \"^-?\\d+$\"").isVisible();
    // Should double escape inside quoted text.
    page.locator("div >> text='pattern \"^-?\\\\d+$\"'").isVisible();
    assertThat(page.locator("div >> text=pattern \"^-?\\d+$")).isVisible();
    assertThat(page.locator("div >> text=pattern \"^-?\\d+$\"")).isVisible();
    // Should double escape inside quoted text.
    assertThat(page.locator("div >> text='pattern \"^-?\\\\d+$\"'")).isVisible();
  }

  @PlaywrightTest
  @DisplayName("SE-049 hasText and internal:text should match full node text in strict mode")
  void hasTextAndInternaltextShouldMatchFullNodeTextInStrictMode(Page page) {
    page.setContent("\n" +
      "    <div id=div1>hello<span>world</span></div>\n" +
      "    <div id=div2>hello</div>\n" +
      "  ");
    assertThat(page.getByText("helloworld", new Page.GetByTextOptions().setExact(true))).hasId("div1");
    assertThat(page.getByText("hello", new Page.GetByTextOptions().setExact(true))).hasId("div2");
    assertThat(page.locator("div", new Page.LocatorOptions().setHasText(Pattern.compile("^helloworld$")))).hasId("div1");
    assertThat(page.locator("div", new Page.LocatorOptions().setHasText(Pattern.compile("^hello$")))).hasId("div2");

    page.setContent("\n" +
      "    <div id=div1><span id=span1>hello</span>world</div>\n" +
      "    <div id=div2><span id=span2>hello</span></div>\n" +
      "  ");
    assertThat(page.getByText("helloworld", new Page.GetByTextOptions().setExact(true))).hasId("div1");
    assertEquals(Arrays.asList("span1", "span2"), page.getByText("hello", new Page.GetByTextOptions().setExact(true)).evaluateAll("els => els.map(e => e.id)"));
    assertThat(page.locator("div", new Page.LocatorOptions().setHasText(Pattern.compile("^helloworld$")))).hasId("div1");
    assertThat(page.locator("div", new Page.LocatorOptions().setHasText(Pattern.compile("^hello$")))).hasId("div2");
  }

}
