package com.microsoft.playwright;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestLocatorElementHandle extends TestBase {
  @Test
  void shouldQueryExistingElement() {
    page.navigate(server.PREFIX + "/playground.html");
    page.setContent("<html><body><div class='second'><div class='inner'>A</div></div></body></html>");
    Locator html = page.locator("html");
    Locator second = html.locator(".second");
    Locator inner = second.locator(".inner");
    Object content = page.evaluate("e => e.textContent", inner.elementHandle());
    assertEquals("A", content);
  }

  @Test
  void shouldQueryExistingElements() {
    page.setContent("<html><body><div>A</div><br/><div>B</div></body></html>");
    Locator html = page.locator("html");
    List<ElementHandle> elements = html.locator("div").elementHandles();
    assertEquals(2, elements.size());
    List<Object> texts = elements.stream().map(element -> page.evaluate("e => e.textContent", element))
      .collect(Collectors.toList());
    assertEquals(asList("A", "B"), texts);
  }

  @Test
  void shouldReturnEmptyArrayForNonExistingElements() {
    page.setContent("<html><body><span>A</span><br/><span>B</span></body></html>");
    Locator html = page.locator("html");
    List<ElementHandle> elements = html.locator("div").elementHandles();
    assertEquals(0, elements.size());
  }


  @Test
  void xpathShouldQueryExistingElement() {
    page.navigate(server.PREFIX + "/playground.html");
    page.setContent("<html><body><div class='second'><div class='inner'>A</div></div></body></html>");
    Locator html = page.locator("html");
    Locator second = html.locator("xpath=./body/div[contains(@class, 'second')]");
    Locator inner = second.locator("xpath=./div[contains(@class, 'inner')]");
    Object content = page.evaluate("e => e.textContent", inner.elementHandle());
    assertEquals("A", content);
  }

  @Test
  void xpathShouldReturnNullForNonExistingElement() {
    page.setContent("<html><body><div class='second'><div class='inner'>B</div></div></body></html>");
    Locator html = page.locator("html");
    List<ElementHandle> second = html.locator("xpath=/div[contains(@class, 'third')]").elementHandles();
    assertEquals(asList(), second);
  }
}
