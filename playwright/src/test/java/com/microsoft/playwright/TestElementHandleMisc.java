package com.microsoft.playwright;

import org.junit.jupiter.api.Test;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestElementHandleMisc extends TestBase {
  @Test
  void shouldHover() {
    page.navigate(server.PREFIX + "/input/scrollable.html");
    ElementHandle button = page.querySelector("#button-6");
    button.hover();
    assertEquals("button-6", page.evaluate("document.querySelector('button:hover').id"));
  }

  @Test
  void shouldHoverWhenNodeIsRemoved() {
    page.navigate(server.PREFIX + "/input/scrollable.html");
    page.evaluate("() => delete window['Node']");
    ElementHandle button = page.querySelector("#button-6");
    button.hover();
    assertEquals("button-6", page.evaluate("document.querySelector('button:hover').id"));
  }

  @Test
  void shouldFillInput() {
    page.navigate(server.PREFIX + "/input/textarea.html");
    ElementHandle handle = page.querySelector("input");
    handle.fill("some value");
    assertEquals("some value", page.evaluate("window['result']"));
  }

  @Test
  void shouldFillInputWhenNodeIsRemoved() {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.evaluate("() => delete window['Node']");
    ElementHandle handle = page.querySelector("input");
    handle.fill("some value");
    assertEquals("some value", page.evaluate("window['result']"));
  }

  @Test
  void shouldCheckTheBox() {
    page.setContent("<input id='checkbox' type='checkbox'></input>");
    ElementHandle input = page.querySelector("input");
    input.check();
    assertEquals(true, page.evaluate("checkbox.checked"));
  }

  @Test
  void shouldUncheckTheBox() {
    page.setContent("<input id='checkbox' type='checkbox' checked></input>");
    ElementHandle input = page.querySelector("input");
    input.uncheck();
    assertEquals(false, page.evaluate("checkbox.checked"));
  }

  @Test
  void shouldSelectSingleOption() {
    page.navigate(server.PREFIX + "/input/select.html");
    ElementHandle select = page.querySelector("select");
    select.selectOption("blue");
    assertEquals(asList("blue"), page.evaluate("window['result'].onInput"));
    assertEquals(asList("blue"), page.evaluate("window['result'].onChange"));
  }

  @Test
  void shouldFocusAButton() {
    page.navigate(server.PREFIX + "/input/button.html");
    ElementHandle button = page.querySelector("button");
    assertEquals(false, button.evaluate("button => document.activeElement === button"));
    button.focus();
    assertEquals(true, button.evaluate("button => document.activeElement === button"));
  }

  @Test
  void shouldCheckTheBoxUsingSetChecked() {
    page.setContent("<input id='checkbox' type='checkbox'></input>");
    ElementHandle input = page.querySelector("input");
    input.setChecked(true);
    assertEquals(true, page.evaluate("checkbox.checked"));
    input.setChecked(false);
    assertEquals(false, page.evaluate("checkbox.checked"));
  }

  @Test
  void shouldFallBackToSelectingByLabel() {
    page.navigate(server.PREFIX + "/input/select.html");
    page.querySelector("select").selectOption("Blue");
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onInput"));
    assertEquals(asList("blue"), page.evaluate("() => window['result'].onChange"));
  }

}
