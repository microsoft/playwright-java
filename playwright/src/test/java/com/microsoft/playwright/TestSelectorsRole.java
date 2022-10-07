package com.microsoft.playwright;

import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class TestSelectorsRole extends TestBase {
  @Test
  void shouldDetectRoles() {
    page.setContent("<button>Hello</button>\n" +
      "    <select multiple=\"\" size=\"2\"></select>\n" +
      "    <select></select>\n" +
      "    <h3>Heading</h3>\n" +
      "    <details><summary>Hello</summary></details>\n" +
      "    <div role='dialog'>I am a dialog</div>");
    assertIterableEquals(asList("<button>Hello</button>"), (List) page.locator("role=button").evaluateAll("els => els.map(e => e.outerHTML)"));

    assertIterableEquals(asList("<select multiple=\"\" size=\"2\"></select>"), (List) page.locator("role=listbox").evaluateAll("els => els.map(e => e.outerHTML)"));

    assertIterableEquals(asList("<select></select>"), (List) page.locator("role=combobox").evaluateAll("els => els.map(e => e.outerHTML)"));

    assertIterableEquals(asList("<h3>Heading</h3>"), (List) page.locator("role=heading").evaluateAll("els => els.map(e => e.outerHTML)"));

    assertIterableEquals(asList("<details><summary>Hello</summary></details>"), (List) page.locator("role=group").evaluateAll("els => els.map(e => e.outerHTML)"));

    assertIterableEquals(asList("<div role=\"dialog\">I am a dialog</div>"), (List) page.locator("role=dialog").evaluateAll("els => els.map(e => e.outerHTML)"));

    assertIterableEquals(emptyList(), (List) page.locator("role=menuitem").evaluateAll("els => els.map(e => e.outerHTML)"));

    assertIterableEquals(emptyList(), (List) page.getByRole(AriaRole.MENUITEM).evaluateAll("els => els.map(e => e.outerHTML)"));
  }
}
