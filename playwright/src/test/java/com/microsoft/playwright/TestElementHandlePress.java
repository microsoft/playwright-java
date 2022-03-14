package com.microsoft.playwright;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestElementHandlePress extends TestBase {

  @Test
  void shouldWork() {
    page.setContent("<input type='text' />");
    page.press("input", "h");
    assertEquals("h", page.evalOnSelector("input", "input => input.value"));
  }

  @Test
  void shouldNotSelectExistingValue() {
    page.setContent("<input type='text' value='hello' />");
    page.press("input", "w");
    assertEquals("whello", page.evalOnSelector("input", "input => input.value"));
  }

  @Test
  void shouldResetSelectionWhenNotFocused() {
    page.setContent("<input type='text' value='hello' /><div tabIndex=2>text</div>");
    page.evalOnSelector("input", "input => {\n" +
      "    input.selectionStart = 2;\n" +
      "    input.selectionEnd = 4;\n" +
      "    document.querySelector('div').focus();\n" +
      "  }");
    page.press("input", "w");
    assertEquals("whello", page.evalOnSelector("input", "input => input.value"));
  }

  @Test
  void shouldNotModifySelectionWhenFocused() {
    page.setContent("<input type='text' value='hello' />");
    page.evalOnSelector("input", "input => {\n" +
      "    input.focus();\n" +
      "    input.selectionStart = 2;\n" +
      "    input.selectionEnd = 4;\n" +
      "  }");
    page.press("input", "w");
    assertEquals("hewo", page.evalOnSelector("input", "input => input.value"));
  }

  @Test
  void shouldWorkWithNumberInput() {
    page.setContent("<input type='number' value=2 />");
    page.press("input", "1");
    assertEquals(isWebKit() ? "1" : "12", page.evalOnSelector("input", "input => input.value"));
  }
}
