package com.microsoft.playwright;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED;
import static org.junit.jupiter.api.Assertions.*;

public class TestElementHandleQuerySelector extends TestBase {
  @Test
  void shouldQueryExistingElement() {
    page.navigate(server.PREFIX + "/playground.html");
    page.setContent("<html><body><div class=\"second\"><div class=\"inner\">A</div></div></body></html>");
    ElementHandle html = page.querySelector("html");
    ElementHandle second = html.querySelector(".second");
    ElementHandle inner = second.querySelector(".inner");
    String content = (String) page.evaluate("e => e.textContent", inner);
    assertEquals( "A", content);
  }

  @Test
  void shouldReturnNullForNonExistingElement() {
    page.setContent("<html><body><div class=\"second\"><div class=\"inner\">B</div></div></body></html>");
    ElementHandle html = page.querySelector("html");
    ElementHandle second = html.querySelector(".third");
    assertNull(second);
  }

  @Test
  void shouldWorkForAdoptedElements() {
    page.navigate(server.EMPTY_PAGE);
    Page popup = page.waitForPopup(() -> page.evaluate(
      "url => window['__popup'] = window.open(url)", server.EMPTY_PAGE));
    // Test JSHandle
    JSHandle divHandle = page.evaluateHandle("() => {\n" +
      "    const div = document.createElement('div');\n" +
      "    document.body.appendChild(div);\n" +
      "    const span = document.createElement('span');\n" +
      "    span.textContent = 'hello';\n" +
      "    div.appendChild(span);\n" +
      "    return div;" +
      "}");
    assertNotNull(divHandle.asElement().querySelector("span"));
    assertEquals("hello", divHandle.asElement().querySelector("span").evaluate( "e => e.textContent"));
    // Test Popup
    popup.waitForLoadState(DOMCONTENTLOADED);
    page.evaluate("() => {\n" +
      "    const div = document.querySelector('div');\n" +
      "    window['__popup'].document.body.appendChild(div);\n" +
      "  }");
    assertNotNull(divHandle.asElement().querySelector("span"));
    assertEquals("hello", divHandle.asElement().querySelector("span").evaluate( "e => e.textContent"));
    assertNotNull(popup.querySelector("span"));
    assertEquals("hello", popup.querySelector("span").evaluate("e => e.textContent"));
  }

  @Test
  void shouldQueryExistingElements() {
    page.setContent("<html><body><div>A</div><br/><div>B</div></body></html>");
    ElementHandle html = page.querySelector("html");
    List<ElementHandle> elements = html.querySelectorAll("div");
    assertEquals(2, elements.size());
    List<String> result = new ArrayList<>();
    elements.stream().forEach(element -> result.add((String) page.evaluate("e => e.textContent", element)));
    assertTrue(Arrays.asList("A", "B").equals(result));
  }

  @Test
  void shouldReturnEmptyArrayForNonExistingElement() {
    page.setContent("<html><body><span>A</span><br/><span>B</span></body></html>");
    ElementHandle html = page.querySelector("html");
    List<ElementHandle> elements = html.querySelectorAll("div");
    assertEquals(0, elements.size());
  }

  @Test
  void xpathShouldQueryExistingElement() {
    page.navigate(server.PREFIX + "/playground.html");
    page.setContent("<html><body><div class=\"second\"><div class=\"inner\">A</div></div></body></html>");
    ElementHandle html = page.querySelector("html");
    List<ElementHandle> second = html.querySelectorAll("xpath=./body/div[contains(@class, 'second')]");
    List<ElementHandle> inner = second.get(0).querySelectorAll("xpath=./div[contains(@class, 'inner')]");
    String content = (String) page.evaluate("e => e.textContent", inner.get(0));
    assertEquals("A", content);
  }

  @Test
  void xpathShouldReturnNullForNonExistingElement() {
    page.setContent("<html><body><div class=\"second\"><div class=\"inner\">B</div></div></body></html>");
    ElementHandle html = page.querySelector("html");
    List<ElementHandle> second = html.querySelectorAll("xpath=/div[contains(@class, 'third')]");
    assertTrue(second.isEmpty());
  }
}
