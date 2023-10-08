package com.microsoft.playwright;

import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

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
  void getByTestIdShouldWorkForRegex() {
    page.setContent("<div><div data-testid='Hello'>Hello world</div></div>");
    assertThat(page.getByTestId(Pattern.compile("He[l]*o"))).hasText("Hello world");
    assertThat(page.mainFrame().getByTestId("Hello")).hasText("Hello world");
    assertThat(page.locator("div").getByTestId("Hello")).hasText("Hello world");
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

  @Test
  void getByLabelShouldWorkWithNestedElements() {
    page.setContent("<label for=target>Last <span>Name</span></label><input id=target type=text>");

    assertThat(page.getByLabel("last name")).hasAttribute("id", "target");
    assertThat(page.getByLabel("st na")).hasAttribute("id", "target");
    assertThat(page.getByLabel("Name")).hasAttribute("id", "target");
    assertThat(page.getByLabel("Last Name", new Page.GetByLabelOptions().setExact(true))).hasAttribute("id", "target");
    assertThat(page.getByLabel(Pattern.compile("Last\\s+name", Pattern.CASE_INSENSITIVE))).hasAttribute("id", "target");

    assertEquals(Collections.emptyList(), page.getByLabel("Last", new Page.GetByLabelOptions().setExact(true)).elementHandles());
    assertEquals(Collections.emptyList(), page.getByLabel("last name", new Page.GetByLabelOptions().setExact(true)).elementHandles());
    assertEquals(Collections.emptyList(), page.getByLabel("Name", new Page.GetByLabelOptions().setExact(true)).elementHandles());
    assertEquals(Collections.emptyList(), page.getByLabel("what?").elementHandles());
    assertEquals(Collections.emptyList(), page.getByLabel(Pattern.compile("last name")).elementHandles());
  }

  @Test
  void getByPlaceholderShouldWork() {
    page.setContent("<div>\n" +
      "    <input placeholder='Hello'>\n" +
      "    <input placeholder='Hello World'>\n" +
      "  </div>");
    assertThat(page.getByPlaceholder("hello")).hasCount(2);
    assertThat(page.getByPlaceholder("Hello", new Page.GetByPlaceholderOptions().setExact(true))).hasCount(1);
    assertThat(page.getByPlaceholder(Pattern.compile("wor", Pattern.CASE_INSENSITIVE))).hasCount(1);

    // Coverage
    assertThat(page.mainFrame().getByPlaceholder("hello")).hasCount(2);
    assertThat(page.locator("div").getByPlaceholder("hello")).hasCount(2);
  }

  @Test
  void getByAltTextShouldWork() {
    page.setContent("<div>\n" +
      "    <input alt='Hello'>\n" +
      "    <input alt='Hello World'>\n" +
      "  </div>");
    assertThat(page.getByAltText("hello")).hasCount(2);
    assertThat(page.getByAltText("Hello", new Page.GetByAltTextOptions().setExact(true))).hasCount(1);
    assertThat(page.getByAltText(Pattern.compile("wor", Pattern.CASE_INSENSITIVE))).hasCount(1);

    // Coverage
    assertThat(page.mainFrame().getByAltText("hello")).hasCount(2);
    assertThat(page.locator("div").getByAltText("hello")).hasCount(2);
  }

  @Test
  void getByTitleShouldWork() {
    page.setContent("<div>\n" +
      "    <input title='Hello'>\n" +
      "    <input title='Hello World'>\n" +
      "  </div>");
    assertThat(page.getByTitle("hello")).hasCount(2);
    assertThat(page.getByTitle("Hello", new Page.GetByTitleOptions().setExact(true))).hasCount(1);
    assertThat(page.getByTitle(Pattern.compile("wor", Pattern.CASE_INSENSITIVE))).hasCount(1);

    // Coverage
    assertThat(page.mainFrame().getByTitle("hello")).hasCount(2);
    assertThat(page.locator("div").getByTitle("hello")).hasCount(2);
  }

  @Test
  void getByEscaping() {
    page.setContent("<label id=label for=control>Hello my\n" +
      "wo\"rld</label><input id=control />");
    page.evalOnSelector("input", "input => {\n" +
      "    input.setAttribute('placeholder', 'hello my\\nwo\"rld');\n" +
      "    input.setAttribute('title', 'hello my\\nwo\"rld');\n" +
      "    input.setAttribute('alt', 'hello my\\nwo\"rld');\n" +
      "  }");
    assertThat(page.getByText("hello my\nwo\"rld")).hasAttribute("id", "label");
    assertThat(page.getByText("hello       my     wo\"rld")).hasAttribute("id", "label");
    assertThat(page.getByLabel("hello my\nwo\"rld")).hasAttribute("id", "control");
    assertThat(page.getByPlaceholder("hello my\nwo\"rld")).hasAttribute("id", "control");
    assertThat(page.getByAltText("hello my\nwo\"rld")).hasAttribute("id", "control");
    assertThat(page.getByTitle("hello my\nwo\"rld")).hasAttribute("id", "control");

    page.setContent("<label id=label for=control>Hello my\n" +
      "world</label><input id=control />");
    page.evalOnSelector("input", "input => {\n" +
      "    input.setAttribute('placeholder', 'hello my\\nworld');\n" +
      "    input.setAttribute('title', 'hello my\\nworld');\n" +
      "    input.setAttribute('alt', 'hello my\\nworld');\n" +
      "  }");
    assertThat(page.getByText("hello my\nworld")).hasAttribute("id", "label");
    assertThat(page.getByText("hello        my    world")).hasAttribute("id", "label");
    assertThat(page.getByLabel("hello my\nworld")).hasAttribute("id", "control");
    assertThat(page.getByPlaceholder("hello my\nworld")).hasAttribute("id", "control");
    assertThat(page.getByAltText("hello my\nworld")).hasAttribute("id", "control");
    assertThat(page.getByTitle("hello my\nworld")).hasAttribute("id", "control");

    page.setContent("<div id=target title='my title'>Text here</div>");
    assertThat(page.getByTitle("my title", new Page.GetByTitleOptions().setExact(true))).hasCount(1, new LocatorAssertions.HasCountOptions().setTimeout(500));
    assertThat(page.getByTitle("my t\\itle", new Page.GetByTitleOptions().setExact(true))).hasCount(0, new LocatorAssertions.HasCountOptions().setTimeout(500));
    assertThat(page.getByTitle("my t\\\\itle", new Page.GetByTitleOptions().setExact(true))).hasCount(0, new LocatorAssertions.HasCountOptions().setTimeout(500));

    page.setContent("<label for=target>foo &gt;&gt; bar</label><input id=target>");
    page.evalOnSelector("input", "input => {\n" +
      "    input.setAttribute('placeholder', 'foo >> bar');\n" +
      "    input.setAttribute('title', 'foo >> bar');\n" +
      "    input.setAttribute('alt', 'foo >> bar');\n" +
      "  }");
    assertEquals("foo >> bar", page.getByText("foo >> bar").textContent());
    assertThat(page.locator("label")).hasText("foo >> bar");
    assertThat(page.getByText("foo >> bar")).hasText("foo >> bar");
    assertEquals("foo >> bar", page.getByText(Pattern.compile("foo >> bar")).textContent());
    assertThat(page.getByLabel("foo >> bar")).hasAttribute("id", "target");
    assertThat(page.getByLabel(Pattern.compile("foo >> bar"))).hasAttribute("id", "target");
    assertThat(page.getByPlaceholder("foo >> bar")).hasAttribute("id", "target");
    assertThat(page.getByAltText("foo >> bar")).hasAttribute("id", "target");
    assertThat(page.getByTitle("foo >> bar")).hasAttribute("id", "target");
    assertThat(page.getByPlaceholder(Pattern.compile("foo >> bar"))).hasAttribute("id", "target");
    assertThat(page.getByAltText(Pattern.compile("foo >> bar"))).hasAttribute("id", "target");
    assertThat(page.getByTitle(Pattern.compile("foo >> bar"))).hasAttribute("id", "target");
  }

  @Test
  void getByRoleEscaping() {
    page.setContent("<a href=\"https://playwright.dev\">issues 123</a>\n" +
      "    <a href=\"https://playwright.dev\">he llo 56</a>\n" +
      "    <button>Click me</button>");
    assertEquals(
      asList("<button>Click me</button>"),
      page.getByRole(AriaRole.BUTTON).evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(
      asList("<a href=\"https://playwright.dev\">issues 123</a>", "<a href=\"https://playwright.dev\">he llo 56</a>"),
      page.getByRole(AriaRole.LINK).evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(
      asList("<a href=\"https://playwright.dev\">issues 123</a>"),
      page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("issues 123")).evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(
      asList("<a href=\"https://playwright.dev\">issues 123</a>"),
      page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("sues")).evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(
      asList("<a href=\"https://playwright.dev\">he llo 56</a>"),
        page.getByRole( AriaRole.LINK, new Page.GetByRoleOptions().setName("  he    \n  llo ")).evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(
      asList(),
      page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("issues")).evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(
      asList(),
      page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("sues").setExact(true)).evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(
      asList("<a href=\"https://playwright.dev\">he llo 56</a>"),
      page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("   he \n llo 56 ").setExact(true)).evaluateAll("els => els.map(e => e.outerHTML)"));

    assertEquals(
      asList("<button>Click me</button>"),
      page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Click me").setExact(true)).evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(
      asList(),
      page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Click \\me").setExact(true)).evaluateAll("els => els.map(e => e.outerHTML)"));
    assertEquals(
      asList(),
      page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Click \\\\me").setExact(true)).evaluateAll("els => els.map(e => e.outerHTML)"));
  }

  @Test
  void locatorGetByRole() {
    page.setContent("<div><button>Click me</button></div>");
    assertEquals(
      asList("<button>Click me</button>"),
      page.locator("div").getByRole(AriaRole.BUTTON).evaluateAll("els => els.map(e => e.outerHTML)"));
  }
}
