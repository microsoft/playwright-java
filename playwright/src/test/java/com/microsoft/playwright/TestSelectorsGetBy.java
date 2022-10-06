package com.microsoft.playwright;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestSelectorsGetBy extends TestBase {
  @Test
  void getByTestIdShouldWork() {
    page.setContent("<div><div data-testid='Hello'>Hello world</div></div>");
    assertThat(page.getByTestId("Hello")).hasText("Hello world");
    assertThat(page.mainFrame().getByTestId("Hello")).hasText("Hello world");
    assertThat(page.locator("div").getByTestId("Hello")).hasText("Hello world");
  }

  @Test
  void getByTestIdShouldEscapeId() {
    page.setContent("<div><div data-testid='He\"llo'>Hello world</div></div>");
    assertThat(page.getByTestId("He\"llo")).hasText("Hello world");
  }

  @Test
  void getByTextShouldWork() {
    page.setContent("<div>yo</div><div>ya</div><div>\nye  </div>");
    assertTrue(((String) page.getByText("ye").evaluate("e => e.outerHTML")).contains(">\nye  </div>"));
    assertTrue(((String) page.getByText(Pattern.compile("ye")).evaluate("e => e.outerHTML")).contains(">\nye  </div>"));
    assertTrue(((String) page.getByText(Pattern.compile("e")).evaluate("e => e.outerHTML")).contains(">\nye  </div>"));

    page.setContent("<div> ye </div><div>ye</div>");
    assertTrue(((String) page.getByText("ye", new Page.GetByTextOptions().setExact(true)).first().evaluate("e => e.outerHTML")).contains("> ye </div>"));

    page.setContent("<div>Hello world</div><div>Hello</div>");
    assertEquals("<div>Hello</div>", page.getByText("Hello", new Page.GetByTextOptions().setExact(true)).evaluate("e => e.outerHTML"));
  }

  @Test
  void getByLabelShouldWork() {
    page.setContent("<div><label for=target>Name</label><input id=target type=text></div>");
    assertEquals("LABEL", page.getByText("Name").evaluate("e => e.nodeName"));
    assertEquals("INPUT", page.getByLabel("Name").evaluate("e => e.nodeName"));
    assertEquals("INPUT", page.mainFrame().getByLabel("Name").evaluate("e => e.nodeName"));
    assertEquals("INPUT", page.locator("div").getByLabel("Name").evaluate("e => e.nodeName"));
  }
}
