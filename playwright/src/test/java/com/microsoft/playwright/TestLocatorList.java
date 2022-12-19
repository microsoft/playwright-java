package com.microsoft.playwright;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestLocatorList extends TestBase {
  @Test
  void locatorAllShouldWork() {
    page.setContent("<div><p>A</p><p>B</p><p>C</p></div>");
    List<String> texts = new ArrayList<>();
    for (Locator p : page.locator("div >> p").all()) {
      texts.add(p.textContent());
    }
    assertEquals(asList("A", "B", "C"), texts);
  }

}
