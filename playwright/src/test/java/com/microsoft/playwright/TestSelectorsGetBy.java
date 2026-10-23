/*
 * Copyright (c) Microsoft Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright;

import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.AfterEach;

import java.util.Collections;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class TestSelectorsGetBy {

  @AfterEach
  void resetTestId(Playwright playwright) {
    playwright.selectors().setTestIdAttribute("data-testid");
  }
  @PlaywrightTest
  void getByTestIdShouldWork(Page page) {
    page.setContent("<div><div data-testid='Hello'>Hello world</div></div>");
    assertThat(page.getByTestId("Hello")).hasText("Hello world");
    assertThat(page.mainFrame().getByTestId("Hello")).hasText("Hello world");
    assertThat(page.locator("div").getByTestId("Hello")).hasText("Hello world");
  }

  @PlaywrightTest
  void getByTestIdWithCustomTestIdShouldWork(Page page, Playwright playwright) {
    page.setContent("<div><div data-my-custom-testid='Hello'>Hello world</div></div>");
    playwright.selectors().setTestIdAttribute("data-my-custom-testid");
    assertThat(page.getByTestId("Hello")).hasText("Hello world");
    assertThat(page.mainFrame().getByTestId("Hello")).hasText("Hello world");
    assertThat(page.locator("div").getByTestId("Hello")).hasText("Hello world");
  }

  @PlaywrightTest
  void getByTestIdWithCommaSeparatedTestIdAttributesShouldMatchAny(Page page, Playwright playwright) {
    page.setContent("<section>\n" +
      "  <div data-pw='Hello'>first</div>\n" +
      "  <div data-ti='Hello'>second</div>\n" +
      "  <div data-testid='Hello'>third</div>\n" +
      "</section>");
    playwright.selectors().setTestIdAttribute("data-pw,data-ti");
    assertThat(page.getByTestId("Hello")).hasCount(2);
    assertThat(page.getByTestId("Hello")).hasText(new String[]{"first", "second"});
    assertThat(page.mainFrame().getByTestId("Hello")).hasCount(2);
    assertThat(page.locator("section").getByTestId("Hello")).hasCount(2);
  }

  @PlaywrightTest
  void shouldUseDataTestidInStrictErrors(Page page, Playwright playwright) {
    playwright.selectors().setTestIdAttribute("data-custom-id");
    page.setContent("" +
      "      <div>\n" +
      "      <div></div>\n" +
      "        <div>\n" +
      "          <div></div>\n" +
      "          <div></div>\n" +
      "        </div>\n" +
      "      </div>\n" +
      "      <div>\n" +
      "        <div class='foo bar:0' data-custom-id='One'>\n" +
      "        </div>\n" +
      "        <div class='foo bar:1' data-custom-id='Two'>\n" +
      "        </div>\n" +
      "      </div>");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.locator(".foo").hover());
    assertTrue(e.getMessage().contains("strict mode violation"), e.getMessage());
    assertTrue(e.getMessage().contains("<div class=\"foo bar:0"), e.getMessage());
    assertTrue(e.getMessage().contains("<div class=\"foo bar:1"), e.getMessage());
    assertTrue(e.getMessage().contains("aka getByTestId(\"One\")"), e.getMessage());
    assertTrue(e.getMessage().contains("aka getByTestId(\"Two\")"), e.getMessage());
  }


  @PlaywrightTest
  void getByTestIdShouldEscapeId(Page page) {
    page.setContent("<div><div data-testid='He\"llo'>Hello world</div></div>");
    assertThat(page.getByTestId("He\"llo")).hasText("Hello world");
  }

  @PlaywrightTest
  void getByTestIdShouldWorkForRegex(Page page) {
    page.setContent("<div><div data-testid='Hello'>Hello world</div></div>");
    assertThat(page.getByTestId(Pattern.compile("He[l]*o"))).hasText("Hello world");
    assertThat(page.mainFrame().getByTestId("Hello")).hasText("Hello world");
    assertThat(page.locator("div").getByTestId("Hello")).hasText("Hello world");
  }

  @PlaywrightTest
  void getByTextShouldWork(Page page) {
    page.setContent("<div>yo</div><div>ya</div><div>\nye  </div>");
    assertTrue(((String) page.getByText("ye").evaluate("e => e.outerHTML")).contains(">\nye  </div>"));
    assertTrue(((String) page.getByText(Pattern.compile("ye")).evaluate("e => e.outerHTML")).contains(">\nye  </div>"));
    assertTrue(((String) page.getByText(Pattern.compile("e")).evaluate("e => e.outerHTML")).contains(">\nye  </div>"));

    page.setContent("<div> ye </div><div>ye</div>");
    assertTrue(((String) page.getByText("ye", new Page.GetByTextOptions().setExact(true)).first().evaluate("e => e.outerHTML")).contains("> ye </div>"));

    page.setContent("<div>Hello world</div><div>Hello</div>");
    assertEquals("<div>Hello</div>", page.getByText("Hello", new Page.GetByTextOptions().setExact(true)).evaluate("e => e.outerHTML"));
  }

  @PlaywrightTest
  void getByLabelShouldWork(Page page) {
    page.setContent("<div><label for=target>Name</label><input id=target type=text></div>");
    assertEquals("LABEL", page.getByText("Name").evaluate("e => e.nodeName"));
    assertEquals("INPUT", page.getByLabel("Name").evaluate("e => e.nodeName"));
    assertEquals("INPUT", page.mainFrame().getByLabel("Name").evaluate("e => e.nodeName"));
    assertEquals("INPUT", page.locator("div").getByLabel("Name").evaluate("e => e.nodeName"));
  }

  @PlaywrightTest
  void getByLabelShouldWorkWithNestedElements(Page page) {
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

  @PlaywrightTest
  void getByPlaceholderShouldWork(Page page) {
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

  @PlaywrightTest
  void getByAltTextShouldWork(Page page) {
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

  @PlaywrightTest
  void getByTitleShouldWork(Page page) {
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

  @PlaywrightTest
  void getByEscaping(Page page) {
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

  @PlaywrightTest
  void getByRoleEscaping(Page page) {
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

  @PlaywrightTest
  void locatorGetByRole(Page page) {
    page.setContent("<div><button>Click me</button></div>");
    assertEquals(
      asList("<button>Click me</button>"),
      page.locator("div").getByRole(AriaRole.BUTTON).evaluateAll("els => els.map(e => e.outerHTML)"));
  }
}
