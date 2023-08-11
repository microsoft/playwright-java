package com.microsoft.playwright;

import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class TestSelectorsMisc extends TestBase {

  @Test
  void shouldWorkWithLayoutSelectors() {
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
    Object[][] boxes = {
      // x, y, width, height
      {0, 0, 150, 150},
      {100, 200, 50, 50},
      {200, 200, 50, 50},
      {100, 150, 50, 50},
      {201, 150, 50, 50},
      {200, 100, 50, 50},
      {50, 50, 50, 50},
      {150, 50, 50, 50},
      {150, -51, 50, 50},
      {201, -101, 50, 50},
    };
    page.setContent("<container style='width: 500px; height: 500px; position: relative;'></container>");
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

    assertEquals("id7", page.evalOnSelector("div:right-of(#id6)", "e => e.id"));
//    assertEquals("id7", page.evalOnSelector("div >> right-of=\"#id6\"", "e => e.id"));
//    assertEquals("id7", page.locator("div", new Page.LocatorOptions().setRightOf(page.locator("#id6")))
//      .first().evaluate("e => e.id"));
    assertEquals("id2", page.evalOnSelector("div:right-of(#id1)", "e => e.id"));
//    assertEquals("id2", page.evalOnSelector("div >> right-of=\"#id1\"", "e => e.id"));
    assertEquals("id4", page.evalOnSelector("div:right-of(#id3)", "e => e.id"));
//    assertEquals("id4", page.evalOnSelector("div >> right-of=\"#id3\"", "e => e.id"));
    assertNull(page.querySelector("div:right-of(#id4)"));
//    assertNull(page.querySelector("div >> right-of=\"#id4\""));
    assertEquals("id7", page.evalOnSelector("div:right-of(#id0)", "e => e.id"));
//    assertEquals("id7", page.evalOnSelector("div >> right-of=\"#id0\"", "e => e.id"));
    assertEquals("id9", page.evalOnSelector("div:right-of(#id8)", "e => e.id"));
//    assertEquals("id9", page.evalOnSelector("div >> right-of=\"#id8\"", "e => e.id"));
    assertEquals("id4,id2,id5,id7,id8,id9", page.evalOnSelectorAll("div:right-of(#id3)", "els => els.map(e => e.id).join(',')"));
//    assertEquals("id4,id2,id5,id7,id8,id9", page.evalOnSelectorAll("div >> right-of=\"#id3\"", "els => els.map(e => e.id).join(',')"));
//    assertEquals("4,2,5,7,8,9", page.locator("div",
//      new Page.LocatorOptions().setRightOf(page.locator("#id3"))).locator("span")
//      .evaluateAll("els => els.map(e => e.textContent).join(',')"));
    assertEquals("id2,id5,id7,id8", page.evalOnSelectorAll("div:right-of(#id3, 50)", "els => els.map(e => e.id).join(',')"));
//    assertEquals("id2,id5,id7,id8", page.evalOnSelectorAll("div >> right-of=\"#id3\",50", "els => els.map(e => e.id).join(',')"));
//    assertEquals("2,5,7,8", page.evalOnSelectorAll("div >> right-of=\"#id3\",50 >> span", "els => els.map(e => e.textContent).join(',')"));
//    assertEquals("4,2,5,7,8,9", page.locator("div", new Page.LocatorOptions().setRightOf(page.locator("#id3")))
//      .locator("span").evaluateAll("els => els.map(e => e.textContent).join(',')"));
    assertEquals("id7,id8", page.evalOnSelectorAll("div:right-of(#id3, 49)", "els => els.map(e => e.id).join(',')"));
//    assertEquals("id7,id8", page.evalOnSelectorAll("div >> right-of=\"#id3\",49", "els => els.map(e => e.id).join(',')"));
//    assertEquals("7,8", page.evalOnSelectorAll("div >> right-of=\"#id3\",49 >> span", "els => els.map(e => e.textContent).join(',')"));
//    assertEquals("4,2,5,7,8,9", page.locator("div", new Page.LocatorOptions().setRightOf(page.locator("#id3")))
//      .locator("span").evaluateAll("els => els.map(e => e.textContent).join(',')"));

    assertEquals("id1", page.evalOnSelector("div:left-of(#id2)", "e => e.id"));
//    assertEquals("id1", page.evalOnSelector("div >> left-of=\"#id2\"", "e => e.id"));
//    assertEquals("id1", page.locator("div", new Page.LocatorOptions().setLeftOf(page.locator("#id2"))).first().evaluate("e => e.id"));
    assertNull(page.querySelector("div:left-of(#id0)"));
//    assertNull(page.querySelector("div >> left-of=\"#id0\""));
    assertEquals("id0", page.evalOnSelector("div:left-of(#id5)", "e => e.id"));
//    assertEquals("id0", page.evalOnSelector("div >> left-of=\"#id5\"", "e => e.id"));
    assertEquals("id8", page.evalOnSelector("div:left-of(#id9)", "e => e.id"));
//    assertEquals("id8", page.evalOnSelector("div >> left-of=\"#id9\"", "e => e.id"));
    assertEquals("id3", page.evalOnSelector("div:left-of(#id4)", "e => e.id"));
//    assertEquals("id3", page.evalOnSelector("div >> left-of=\"#id4\"", "e => e.id"));
    assertEquals("id0,id7,id3,id1,id6,id8", page.evalOnSelectorAll("div:left-of(#id5)", "els => els.map(e => e.id).join(',')"));
//    assertEquals("id0,id7,id3,id1,id6,id8", page.evalOnSelectorAll("div >> left-of=\"#id5\"", "els => els.map(e => e.id).join(',')"));
    assertEquals("id7,id8", page.evalOnSelectorAll("div:left-of(#id5, 3)", "els => els.map(e => e.id).join(',')"));
//    assertEquals("id7,id8", page.evalOnSelectorAll("div >> left-of=\"#id5\",3", "els => els.map(e => e.id).join(',')"));
//    assertEquals("7,8", page.evalOnSelectorAll("div >> left-of=\"#id5\",3 >> span", "els => els.map(e => e.textContent).join(',')"));

    assertEquals("id3", page.evalOnSelector("div:above(#id0)", "e => e.id"));
//    assertEquals("id3", page.evalOnSelector("div >> above=\"#id0\"", "e => e.id"));
//    assertEquals("id3", page.locator("div", new Page.LocatorOptions().setAbove(page.locator("#id0")))
//      .first().evaluate("e => e.id"));
    assertEquals("id4", page.evalOnSelector("div:above(#id5)", "e => e.id"));
//    assertEquals("id4", page.evalOnSelector("div >> above=\"#id5\"", "e => e.id"));
    assertEquals("id5", page.evalOnSelector("div:above(#id7)", "e => e.id"));
//    assertEquals("id5", page.evalOnSelector("div >> above=\"#id7\"", "e => e.id"));
    assertEquals("id0", page.evalOnSelector("div:above(#id8)", "e => e.id"));
//    assertEquals("id0", page.evalOnSelector("div >> above=\"#id8\"", "e => e.id"));
    assertEquals("id8", page.evalOnSelector("div:above(#id9)", "e => e.id"));
//    assertEquals("id8", page.evalOnSelector("div >> above=\"#id9\"", "e => e.id"));
    assertNull(page.querySelector("div:above(#id2)"));
//    assertNull(page.querySelector("div >> above=\"#id2\""));
    assertEquals("id4,id2,id3,id1", page.evalOnSelectorAll("div:above(#id5)", "els => els.map(e => e.id).join(',')"));
//    assertEquals("id4,id2,id3,id1", page.evalOnSelectorAll("div >> above=\"#id5\"", "els => els.map(e => e.id).join(',')"));
    assertEquals("id4,id3", page.evalOnSelectorAll("div:above(#id5, 20)", "els => els.map(e => e.id).join(',')"));
//    assertEquals("id4,id3", page.evalOnSelectorAll("div >> above=\"#id5\",20", "els => els.map(e => e.id).join(',')"));

    assertEquals("id5", page.evalOnSelector("div:below(#id4)", "e => e.id"));
//    assertEquals("id5", page.evalOnSelector("div >> below=\"#id4\"", "e => e.id"));
//    assertEquals("id5", page.locator("div", new Page.LocatorOptions().setBelow(page.locator("#id4")))
//      .first().evaluate("e => e.id"));
    assertEquals("id0", page.evalOnSelector("div:below(#id3)", "e => e.id"));
//    assertEquals("id0", page.evalOnSelector("div >> below=\"#id3\"", "e => e.id"));
    assertEquals("id4", page.evalOnSelector("div:below(#id2)", "e => e.id"));
//    assertEquals("id4", page.evalOnSelector("div >> below=\"#id2\"", "e => e.id"));
    assertEquals("id8", page.evalOnSelector("div:below(#id6)", "e => e.id"));
//    assertEquals("id8", page.evalOnSelector("div >> below=\"#id6\"", "e => e.id"));
    assertEquals("id8", page.evalOnSelector("div:below(#id7)", "e => e.id"));
//    assertEquals("id8", page.evalOnSelector("div >> below=\"#id7\"", "e => e.id"));
    assertEquals("id9", page.evalOnSelector("div:below(#id8)", "e => e.id"));
//    assertEquals("id9", page.evalOnSelector("div >> below=\"#id8\"", "e => e.id"));
    assertNull(page.querySelector("div:below(#id9)"));
//    assertNull(page.querySelector("div >> below=\"#id9\""));
    assertEquals("id0,id5,id6,id7,id8,id9", page.evalOnSelectorAll("div:below(#id3)", "els => els.map(e => e.id).join(',')"));
//    assertEquals("id0,id5,id6,id7,id8,id9", page.evalOnSelectorAll("div >> below=\"#id3\"", "els => els.map(e => e.id).join(',')"));
    assertEquals("id0,id5,id6,id7", page.evalOnSelectorAll("div:below(#id3, 105)", "els => els.map(e => e.id).join(',')"));
//    assertEquals("id0,id5,id6,id7", page.evalOnSelectorAll("div >> below=\"#id3\" , 105", "els => els.map(e => e.id).join(',')"));

    assertEquals("id3", page.evalOnSelector("div:near(#id0)", "e => e.id"));
//    assertEquals("id3", page.evalOnSelector("div >> near=\"#id0\"", "e => e.id"));
//    assertEquals("id3", page.locator("div", new Page.LocatorOptions().setNear(page.locator("#id0")))
//      .first().evaluate("e => e.id"));
    assertEquals("id0,id5,id3,id6", page.evalOnSelectorAll("div:near(#id7)", "els => els.map(e => e.id).join(',')"));
//    assertEquals("id0,id5,id3,id6", page.evalOnSelectorAll("div >> near=\"#id7\"", "els => els.map(e => e.id).join(',')"));
    assertEquals("id3,id6,id7,id8,id1,id5", page.evalOnSelectorAll("div:near(#id0)", "els => els.map(e => e.id).join(',')"));
//    assertEquals("id3,id6,id7,id8,id1,id5", page.evalOnSelectorAll("div >> near=\"#id0\"", "els => els.map(e => e.id).join(',')"));
    assertEquals("id0,id3,id7", page.evalOnSelectorAll("div:near(#id6)", "els => els.map(e => e.id).join(',')"));
//    assertEquals("id0,id3,id7", page.evalOnSelectorAll("div >> near=\"#id6\"", "els => els.map(e => e.id).join(',')"));
    assertEquals("id0", page.evalOnSelectorAll("div:near(#id6, 10)", "els => els.map(e => e.id).join(',')"));
//    assertEquals("id0", page.evalOnSelectorAll("div >> near=\"#id6\",10", "els => els.map(e => e.id).join(',')"));
    assertEquals("id3,id6,id7,id8,id1,id5,id4,id2", page.evalOnSelectorAll("div:near(#id0, 100)", "els => els.map(e => e.id).join(',')"));
//    assertEquals("id3,id6,id7,id8,id1,id5,id4,id2", page.evalOnSelectorAll("div >> near=\"#id0\",100", "els => els.map(e => e.id).join(',')"));

    assertEquals("id7,id6", page.evalOnSelectorAll("div:below(#id5):above(#id8)", "els => els.map(e => e.id).join(',')"));
//    assertEquals("id7,id6", page.evalOnSelectorAll("div >> below=\"#id5\" >> above=\"#id8\"", "els => els.map(e => e.id).join(',')"));
    assertEquals("id7", page.evalOnSelector("div:below(#id5):above(#id8)", "e => e.id"));
//    assertEquals("id7", page.evalOnSelector("div >> below=\"#id5\" >> above=\"#id8\"", "e => e.id"));
//    assertEquals("id7", page.locator("div", new Page.LocatorOptions()
//      .setBelow(page.locator("#id5"))
//      .setAbove(page.locator("#id8"))).first().evaluate("e => e.id"));

    assertEquals("id5,id6,id3", page.evalOnSelectorAll("div:right-of(#id0) + div:above(#id8)", "els => els.map(e => e.id).join(',')"));

    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.querySelector(":near(50)"));
    assertTrue(e.getMessage().contains("\"near\" engine expects a selector list and optional maximum distance in pixels"), e.getMessage());

    e = assertThrows(PlaywrightException.class, () -> page.querySelector("div >> left-of=abc"));
    assertTrue(e.getMessage().contains("Malformed selector: left-of=abc"));

    e = assertThrows(PlaywrightException.class, () -> page.querySelector("left-of=\"div\""));
    assertTrue(e.getMessage().contains("\"left-of\" selector cannot be first"), e.getMessage());

    e = assertThrows(PlaywrightException.class, () -> page.querySelector("div >> left-of=33"));
    assertTrue(e.getMessage().contains("Malformed selector: left-of=33"));

    e = assertThrows(PlaywrightException.class, () -> page.querySelector("div >> left-of='span','foo'"));
    assertTrue(e.getMessage().contains("Malformed selector: left-of='span','foo'"));

    e = assertThrows(PlaywrightException.class, () -> page.querySelector("div >> left-of='span',3,4"));
    assertTrue(e.getMessage().contains("Malformed selector: left-of='span',3,4"));
  }

  @Test
  void shouldWorkWithInternalHasNot() {
    page.setContent("<section><span></span><div></div></section><section><br></section>");
    assertEquals(1, page.evalOnSelectorAll("section >> internal:has-not=\"span\"", "els => els.length"));
    assertEquals(0, page.evalOnSelectorAll("section >> internal:has-not=\"span, div, br\"", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("section >> internal:has-not=\"br\"", "els => els.length"));
    assertEquals(1, page.evalOnSelectorAll("section >> internal:has-not=\"span, div\"", "els => els.length"));
    assertEquals(2, page.evalOnSelectorAll("section >> internal:has-not=\"article\"", "els => els.length"));
  }

  @Test
  void shouldWorkWithInternalAnd() {
    page.setContent("<div class=foo>hello</div><div class=bar>world</div>\n" +
      "    <span class=foo>hello2</span><span class=bar>world2</span>");
    assertEquals(asList(), page.evalOnSelectorAll("div >> internal:and=\"span\"", "els => els.map(e => e.textContent)"));
    assertEquals(asList("hello"), page.evalOnSelectorAll("div >> internal:and=\".foo\"", "els => els.map(e => e.textContent)"));
    assertEquals(asList("world"), page.evalOnSelectorAll("div >> internal:and=\".bar\"", "els => els.map(e => e.textContent)"));
    assertEquals(asList("hello2", "world2"), page.evalOnSelectorAll("span >> internal:and=\"span\"", "els => els.map(e => e.textContent)"));
    assertEquals(asList("hello"), page.evalOnSelectorAll(".foo >> internal:and=\"div\"", "els => els.map(e => e.textContent)"));
    assertEquals(asList("world2"), page.evalOnSelectorAll(".bar >> internal:and=\"span\"", "els => els.map(e => e.textContent)"));
  }

  @Test
  void shouldWorkWithInternalOr() {
    page.setContent("<div>hello</div>\n" +
      "    <span>world</span>");
    assertEquals(asList("hello", "world"), page.evalOnSelectorAll("div >> internal:or=\"span\"", "els => els.map(e => e.textContent)"));
    assertEquals(asList("hello", "world"), page.evalOnSelectorAll("span >> internal:or=\"div\"", "els => els.map(e => e.textContent)"));
    assertEquals(0, page.evalOnSelectorAll("article >> internal:or=\"something\"", "els => els.length"));
    assertEquals("hello", page.locator("article >> internal:or=\"div\"").textContent());
    assertEquals("world", page.locator("article >> internal:or=\"span\"").textContent());
    assertEquals("hello", page.locator("div >> internal:or=\"article\"").textContent());
    assertEquals("world", page.locator("span >> internal:or=\"article\"").textContent());
  }
  @Test
  void shouldWorkWithInternalChain() {
    page.setContent("<div>one <span>two</span> <button>three</button> </div>\n" +
      "    <span>four</span>\n" +
      "    <button>five</button>");
    assertEquals(asList("three"), page.evalOnSelectorAll("div >> internal:chain=\"button\"", "els => els.map(e => e.textContent)"));
    assertEquals(asList("two", "three"), page.evalOnSelectorAll("div >> internal:chain=\"span >> internal:or=\\\"button\\\"\"", "els => els.map(e => e.textContent)"));
  }

}
