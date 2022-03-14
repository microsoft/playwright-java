package com.microsoft.playwright;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestElementHandleType extends TestBase {

  @Test
  void shouldWork() {
    page.setContent("<input type='text' />");
    page.type("input", "hello");
    assertEquals("hello", page.evalOnSelector("input", "input => input.value"));
  }

  @Test
  void shouldNotSelectExistingValue() {
    page.setContent("<input type='text' value='hello' />");
    page.type("input", "world");
    assertEquals("worldhello", page.evalOnSelector("input", "input => input.value"));
  }

  @Test
  void shouldResetSelectionWhenNotFocus() {
    page.setContent("<input type='text' value='hello' /><div tabIndex=2>text</div>");
    page.evalOnSelector("input", "input => {\n" +
      "    input.selectionStart = 2;\n" +
      "    input.selectionEnd = 4;\n" +
      "    document.querySelector('div').focus();\n" +
      "  }");
    page.type("input", "world");
    assertEquals("worldhello", page.evalOnSelector("input", "input => input.value"));
  }

  @Test
  void shouldNotModifySelectionWhenFocus() {
    page.setContent("<input type='text' value='hello' />");
    page.evalOnSelector("input", "input => {\n" +
      "    input.focus();\n" +
      "    input.selectionStart = 2;\n" +
      "    input.selectionEnd = 4;\n" +
      "  }");
    page.type("input", "world");
    assertEquals("heworldo", page.evalOnSelector("input", "input => input.value"));
  }

  @Test
  void shouldWorkWithNumberInput() {
    page.setContent("<input type='number' value=2 />");
    page.type("input", "13");
    assertEquals(isWebKit() ? "13" : "132", page.evalOnSelector("input", "input => input.value"));
  }
}
