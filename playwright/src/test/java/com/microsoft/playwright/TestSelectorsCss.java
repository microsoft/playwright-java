package com.microsoft.playwright;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestSelectorsCss extends TestBase {

  @Test
  void shouldWorkWithLargeDOM() {
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
        "span"
    );

    boolean measure = false;
    for (String selector : selectors) {
      List<Integer> counts1 = new ArrayList<>();
      long time1 = System.currentTimeMillis();
      for (int i = 0; i < (measure ? 10 : 1); i++) {
        counts1.add((Integer) page.evalOnSelectorAll(selector, "els => els.length"));
      }
      if (measure) {
        System.out.println("pw: " + (System.currentTimeMillis() - time1) + " ms");
      }
      long time2 = System.currentTimeMillis();
      List<Integer> counts2 = new ArrayList<>();
      for (int i = 0; i < (measure ? 10 : 1); i++) {
        counts2.add((Integer) page.evaluate("selector => document.querySelectorAll(selector).length", selector));
      }
      if (measure) {
        System.out.println("qs: " + (System.currentTimeMillis() - time2) + " ms");
      }
      assertEquals(counts1, counts2);
    }
  }

  @Test
  void shouldWorkForOpenShadowRoots() {
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
    JSHandle root2Shadow = root2.evaluateHandle("r => r.shadowRoot");
    assertEquals("Hello from root2", root2Shadow.asElement().evalOnSelector("css:light=#target", "e => e.textContent"));

    ElementHandle root3 = page.querySelectorAll("css=div div").get(1);
    assertEquals("Hello from root3", page.evalOnSelector("text=root3", "e => e.textContent"));
    assertEquals("Hello from root3 #2", page.evalOnSelector("css=[attr*=\"value\"]", "e => e.textContent"));
    assertNull(root3.querySelector("css:light=[attr*=\"value\"]"));
  }

  @Test
  void shouldWorkWithGreaterThanCombinatorAndSpaces() {
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

  @Test
  void shouldWorkWithCommaSeparatedList() {
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

  @Test
  void shouldKeepDomOrderWithCommaSeparatedList() {
    page.setContent("<section><span><div><x></x><y></y></div></span></section>");
    assertEquals("SPAN,DIV", page.evalOnSelectorAll("css=span,div", "els => els.map(e => e.nodeName).join(',')"));
    assertEquals("SPAN,DIV", page.evalOnSelectorAll("css=div,span", "els => els.map(e => e.nodeName).join(',')"));
    assertEquals("DIV", page.evalOnSelectorAll("css=span div, div", "els => els.map(e => e.nodeName).join(',')"));
    assertEquals("SECTION", page.evalOnSelectorAll("*css=section >> css=div,span", "els => els.map(e => e.nodeName).join(',')"));
    assertEquals("DIV", page.evalOnSelectorAll("css=section >> *css=div >> css=x,y", "els => els.map(e => e.nodeName).join(',')"));
    assertEquals("SPAN,DIV", page.evalOnSelectorAll("css=section >> *css=div,span >> css=x,y", "els => els.map(e => e.nodeName).join(',')"));
    assertEquals("SPAN,DIV", page.evalOnSelectorAll("css=section >> *css=div,span >> css=y", "els => els.map(e => e.nodeName).join(',')"));
  }

  @Test
  void shouldWorkWithCommaSeparatedListInVariousPosition() {
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

  @Test
  void shouldWorkWithCommaInsideText() {
    page.setContent("<span></span><div attr=\"hello,world!\"></div>");
    assertEquals("<div attr=\"hello,world!\"></div>", page.evalOnSelector("css=div[attr=\"hello,world!\"]", "e => e.outerHTML"));
    assertEquals("<div attr=\"hello,world!\"></div>", page.evalOnSelector("css=[attr=\"hello,world!\"]", "e => e.outerHTML"));
    assertEquals("<div attr=\"hello,world!\"></div>", page.evalOnSelector("css=div[attr='hello,world!']", "e => e.outerHTML"));
    assertEquals("<div attr=\"hello,world!\"></div>", page.evalOnSelector("css=[attr='hello,world!']", "e => e.outerHTML"));
    assertEquals("<span></span>", page.evalOnSelector("css=div[attr=\"hello,world!\"],span", "e => e.outerHTML"));
  }

  @Test
  void shouldWorkWithAttributeSelectors() {
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
      "[attr2 $=\"foo=bar[]\"]"
    );
    for (String selector:selectors) {
      assertTrue((Boolean) page.evalOnSelector(selector, "e => e === window['div']"));
    }
    assertTrue((Boolean) page.evalOnSelector("[attr*=hello] span", "e => e.parentNode === window['div']"));
    assertTrue((Boolean) page.evalOnSelector("[attr*=hello] >> span", "e => e.parentNode === window['div']"));
    assertTrue((Boolean) page.evalOnSelector("[attr3=\"] span\"] >> span", "e => e.parentNode === window['div']"));
  }

  @Test
  void shouldNotMatchRootAfterGreaterGreaterThan() {
    page.setContent("<section><div>test</div></section>");
    ElementHandle element = page.querySelector("css=section >> css=section");
    assertNull(element);
  }

  @Test
  void shouldWorkWithNumericalId() {
    page.setContent("<section id=\"123\"></section>");
    ElementHandle element = page.querySelector("#\\31\\32\\33");
    assertNotNull(element);
  }

  @Test
  void shouldWorkWithWrongCaseId() {
    page.setContent("<section id=\"Hello\"></section>");
    assertEquals("SECTION", page.evalOnSelector("#Hello", "e => e.tagName"));
    assertEquals("SECTION", page.evalOnSelector("#hello", "e => e.tagName"));
    assertEquals("SECTION", page.evalOnSelector("#HELLO", "e => e.tagName"));
    assertEquals("SECTION", page.evalOnSelector("#helLO", "e => e.tagName"));
  }

  @Test
  void shouldWorkWithAsterisk() {
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

  @Test
  void shouldWorkWithColonNthChild(){
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

  @Test
  void shouldWorkWithColonNot() {
    page.navigate(server.PREFIX + "/deep-shadow.html");
    assertEquals(2, page.evalOnSelectorAll("css=div:not(#root1)", "els => els.length"));
    assertEquals(4, page.evalOnSelectorAll("css=body :not(span)", "els => els.length"));
    assertEquals(0, page.evalOnSelectorAll("css=div > :not(span):not(div)", "els => els.length"));
  }

  @Test
  void shouldWorkWithTilde() {
    page.setContent(
      "<div id=div1></div>\n" +
      "<div id=div2></div>\n" +
      "<div id=div3></div>\n" +
      "<div id=div4></div>\n" +
      "<div id=div5></div>\n" +
      "<div id=div6></div>"
    );
    assertEquals(1, page.evalOnSelectorAll("css=#div1 ~ div ~ #div6", "els => els.length"));
    assertEquals(4, page.evalOnSelectorAll("css=#div1 ~ div ~ div", "els => els.length"));
    assertEquals(2, page.evalOnSelectorAll("css=#div3 ~ div ~ div", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("css=#div4 ~ div ~ div", "els => els.length"));
    assertEquals(0, page.evalOnSelectorAll("css=#div5 ~ div ~ div", "els => els.length"));
    assertEquals(0, page.evalOnSelectorAll("css=#div3 ~ #div2 ~ #div6", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("css=#div3 ~ #div4 ~ #div5", "els => els.length"));
  }

  @Test
  void shouldWorkWithPlus() {
    page.setContent(
      "<section>\n" +
      "  <div id=div1></div>\n" +
      "  <div id=div2></div>\n" +
      "  <div id=div3></div>\n" +
      "  <div id=div4></div>\n" +
      "  <div id=div5></div>\n" +
      "  <div id=div6></div>\n" +
      "</section>"
    );
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
  }

  @Test
  void shouldWorkWithSpacesInColonNthChildAndColonNot() {
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

  @Test
  void shouldWorkWithColonIs() {
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

  @Test
  void shouldWorkWithColonHas() {
    page.navigate(server.PREFIX + "/deep-shadow.html");
    assertEquals(2, page.evalOnSelectorAll("css=div:has(#target)", "els => els.length"));
    assertEquals(3, page.evalOnSelectorAll("css=div:has([data-testid=foo])", "els => els.length"));
    assertEquals(2, page.evalOnSelectorAll("css=div:has([attr*=value])", "els => els.length"));
  }

  @Test
  void shouldWorkWithColonScope() {
    page.navigate(server.PREFIX + "/deep-shadow.html");
    // 'is' does not change the scope, so it remains 'html'.
    assertEquals(0, page.evalOnSelectorAll("css=div:is(:scope#root1)", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("css=div:is(:scope #root1)", "els => els.length"));
    // 'has' does change the scope, so it becomes the 'div' we are querying.
    assertEquals(1, page.evalOnSelectorAll("css=div:has(:scope > #target)", "els => els.length"));

    ElementHandle handle = page.querySelector("css=span");
    for (Object scope : Arrays.asList(page, handle)) {
      assertEquals(1, evalOnSelectorAllBaseOnContext(scope, "css=:scope"));
      assertEquals(0, evalOnSelectorAllBaseOnContext(scope, "css=* :scope"));
      assertEquals(0, evalOnSelectorAllBaseOnContext(scope, "css=* + :scope"));
      assertEquals(0, evalOnSelectorAllBaseOnContext(scope, "css=* > :scope"));
      assertEquals(0, evalOnSelectorAllBaseOnContext(scope, "css=* ~ :scope"));
    }
  }

  private int evalOnSelectorAllBaseOnContext(Object context, String selector) {
    if (context instanceof Page) {
      return (int) ((Page) context).evalOnSelectorAll(selector, "els => els.length");
    } else if (context instanceof ElementHandle) {
      return (int) ((ElementHandle) context).evalOnSelectorAll(selector, "els => els.length");
    } else {
      return -1;
    }
  }

}
