/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/selectors-get-by.spec.ts. */
public class TestSelectorsGetBy {
  @PlaywrightTest
  @DisplayName("SE-071 getByTestId should work")
  void getByTestIdShouldWork(Page page) {
    page.setContent("<div><div data-testid=\"Hello\">Hello world</div></div>");
    assertThat(page.getByTestId("Hello")).hasText("Hello world");
    assertThat(page.mainFrame().getByTestId("Hello")).hasText("Hello world");
    assertThat(page.locator("div").getByTestId("Hello")).hasText("Hello world");
  }

  @PlaywrightTest
  @DisplayName("SE-072 getByTestId with custom testId should work")
  void getByTestIdWithCustomTestIdShouldWork(Page page, Playwright playwright) {
    page.setContent("<div><div data-my-custom-testid=\"Hello\">Hello world</div></div>");
    playwright.selectors().setTestIdAttribute("data-my-custom-testid");
    assertThat(page.getByTestId("Hello")).hasText("Hello world");
    assertThat(page.mainFrame().getByTestId("Hello")).hasText("Hello world");
    assertThat(page.locator("div").getByTestId("Hello")).hasText("Hello world");
  }

  @PlaywrightTest
  @DisplayName("SE-073 getByTestId with comma-separated testIdAttributes should match any")
  void getByTestIdWithCommaSeparatedTestIdAttributesShouldMatchAny(Page page, Playwright playwright) {
    page.setContent("\n" +
      "    <section>\n" +
      "      <div data-pw=\"Hello\">first</div>\n" +
      "      <div data-ti=\"Hello\">second</div>\n" +
      "      <div data-testid=\"Hello\">third</div>\n" +
      "    </section>\n" +
      "  ");
    playwright.selectors().setTestIdAttribute("data-pw,data-ti");
    assertThat(page.getByTestId("Hello")).hasCount(2);
    assertThat(page.getByTestId("Hello")).hasText(new String[]{"first", "second"});
    assertThat(page.mainFrame().getByTestId("Hello")).hasCount(2);
    assertThat(page.locator("section").getByTestId("Hello")).hasCount(2);
  }

  @PlaywrightTest
  @DisplayName("SE-074 getByTestId should escape id")
  void getByTestIdShouldEscapeId(Page page) {
    page.setContent("<div><div data-testid='He\"llo'>Hello world</div></div>");
    assertThat(page.getByTestId("He\"llo")).hasText("Hello world");
  }

  @PlaywrightTest
  @DisplayName("SE-075 getByTestId should work for regex")
  void getByTestIdShouldWorkForRegex(Page page) {
    page.setContent("<div><div data-testid=\"Hello\">Hello world</div></div>");
    assertThat(page.getByTestId(Pattern.compile("He[l]*o"))).hasText("Hello world");
    assertThat(page.mainFrame().getByTestId("Hello")).hasText("Hello world");
    assertThat(page.locator("div").getByTestId("Hello")).hasText("Hello world");
  }

  @PlaywrightTest
  @DisplayName("SE-076 getByText should work")
  void getByTextShouldWork(Page page) {
    page.setContent("<div>yo</div><div>ya</div><div>\nye  </div>");
    assertTrue(((String) page.getByText("ye").evaluate("e => e.outerHTML")).contains(">\nye  </div>"));
    assertTrue(((String) page.getByText(Pattern.compile("ye")).evaluate("e => e.outerHTML")).contains(">\nye  </div>"));
    assertTrue(((String) page.getByText(Pattern.compile("e")).evaluate("e => e.outerHTML")).contains(">\nye  </div>"));

    page.setContent("<div> ye </div><div>ye</div>");
    assertTrue(((String) page.getByText("ye", new Page.GetByTextOptions().setExact(true)).first().evaluate("e => e.outerHTML")).contains("> ye </div>"));

    page.setContent("<div>Hello world</div><div>Hello</div>");
    assertTrue(((String) page.getByText("Hello", new Page.GetByTextOptions().setExact(true)).evaluate("e => e.outerHTML")).contains(">Hello</div>"));
  }

  @PlaywrightTest
  @DisplayName("SE-077 getByLabel should work")
  void getByLabelShouldWork(Page page) {
    page.setContent("<div><label for=target>Name</label><input id=target type=text></div>");
    assertEquals("LABEL", page.getByText("Name").evaluate("e => e.nodeName"));
    assertEquals("INPUT", page.getByLabel("Name").evaluate("e => e.nodeName"));
    assertEquals("INPUT", page.mainFrame().getByLabel("Name").evaluate("e => e.nodeName"));
    assertEquals("INPUT", page.locator("div").getByLabel("Name").evaluate("e => e.nodeName"));
  }

  @PlaywrightTest
  @DisplayName("SE-078 getByLabel should work with nested elements")
  void getByLabelShouldWorkWithNestedElements(Page page) {
    page.setContent("<label for=target>Last <span>Name</span></label><input id=target type=text>");

    assertThat(page.getByLabel("last name")).hasAttribute("id", "target");
    assertThat(page.getByLabel("st na")).hasAttribute("id", "target");
    assertThat(page.getByLabel("Name")).hasAttribute("id", "target");
    assertThat(page.getByLabel("Last Name", new Page.GetByLabelOptions().setExact(true))).hasAttribute("id", "target");
    assertThat(page.getByLabel(Pattern.compile("Last\\s+name", Pattern.CASE_INSENSITIVE))).hasAttribute("id", "target");

    assertEquals(new ArrayList<>(), page.getByLabel("Last", new Page.GetByLabelOptions().setExact(true)).elementHandles());
    assertEquals(new ArrayList<>(), page.getByLabel("last name", new Page.GetByLabelOptions().setExact(true)).elementHandles());
    assertEquals(new ArrayList<>(), page.getByLabel("Name", new Page.GetByLabelOptions().setExact(true)).elementHandles());
    assertEquals(new ArrayList<>(), page.getByLabel("what?").elementHandles());
    assertEquals(new ArrayList<>(), page.getByLabel(Pattern.compile("last name")).elementHandles());
  }

  @PlaywrightTest
  @DisplayName("SE-079 getByLabel should work with multiply-labelled input")
  void getByLabelShouldWorkWithMultiplyLabelledInput(Page page) {
    page.setContent("<label for=target>Name</label><input id=target type=text><label for=target>First or Last</label>");
    assertEquals("target", page.getByLabel("Name").evaluate("e => e.id"));
    assertEquals("target", page.getByLabel("First or Last").evaluate("e => e.id"));
  }

  @PlaywrightTest
  @DisplayName("SE-080 getByLabel should work with ancestor label and multiple controls")
  void getByLabelShouldWorkWithAncestorLabelAndMultipleControls(Page page) {
    page.setContent("<label>Name<button id=target>Click me</button><input type=text></label>");
    assertEquals("target", page.getByLabel("Name").evaluate("e => e.id"));
  }

  @PlaywrightTest
  @DisplayName("SE-081 getByLabel should work with ancestor label and for")
  void getByLabelShouldWorkWithAncestorLabelAndFor(Page page) {
    page.setContent("\n" +
      "    <label for=target>Name<input type=text id=nontarget></label>\n" +
      "    <input type=text id=target>\n" +
      "  ");
    assertEquals("target", page.getByLabel("Name").evaluate("e => e.id"));
  }

  @PlaywrightTest
  @DisplayName("SE-082 getByLabel should work with aria-labelledby")
  void getByLabelShouldWorkWithAriaLabelledby(Page page) {
    page.setContent("<label id=name-label>Name</label><button aria-labelledby=name-label>Click me</button>");
    assertEquals("Click me", page.getByLabel("Name").evaluate("e => e.textContent"));
  }

  @PlaywrightTest
  @DisplayName("SE-083 getByLabel should prioritize aria-labelledby over native label")
  void getByLabelShouldPrioritizeAriaLabelledbyOverNativeLabel(Page page) {
    page.setContent("\n" +
      "    <label id=name-label>Name</label>\n" +
      "    <label>Wrong<button aria-labelledby=name-label>Click me</button></label>\n" +
      "  ");
    assertEquals("Click me", page.getByLabel("Name").evaluate("e => e.textContent"));
  }

  @PlaywrightTest
  @DisplayName("SE-084 getByLabel should work with aria-label")
  void getByLabelShouldWorkWithAriaLabel(Page page) {
    page.setContent("<input id=target aria-label=\"Name\">");
    assertEquals("target", page.getByLabel("Name").evaluate("e => e.id"));
  }

  @PlaywrightTest
  @DisplayName("SE-085 getByLabel should ignore empty aria-label")
  void getByLabelShouldIgnoreEmptyAriaLabel(Page page) {
    page.setContent("<label for=target>Last Name</label><input id=target type=text aria-label>");
    assertEquals("target", page.getByLabel("Last Name").evaluate("e => e.id"));
  }

  @PlaywrightTest
  @DisplayName("SE-086 getByLabel should prioritize aria-labelledby over aria-label")
  void getByLabelShouldPrioritizeAriaLabelledbyOverAriaLabel(Page page) {
    page.setContent("<label id=other-label>Other</label><input id=target aria-label=\"Name\" aria-labelledby=other-label>");
    assertEquals("target", page.getByLabel("Other").evaluate("e => e.id"));
  }

  @PlaywrightTest
  @DisplayName("SE-087 getByPlaceholder should work")
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
  @DisplayName("SE-088 getByAltText should work")
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
  @DisplayName("SE-089 getByTitle should work")
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
  @DisplayName("SE-090 getBy escaping")
  void getByEscaping(Page page) {
    page.setContent("<label id=label for=control>Hello my\n" +
      "wo\"rld</label><input id=control />");
    page.evalOnSelector("input", "input => {\n" +
      "    input.setAttribute('placeholder', 'hello my\\nwo\"rld');\n" +
      "    input.setAttribute('title', 'hello my\\nwo\"rld');\n" +
      "    input.setAttribute('alt', 'hello my\\nwo\"rld');\n" +
      "  }");
    // Java: expect.soft is a plain assertion, the first failure stops the test.
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

    page.setContent("<div id=target title=\"my title\">Text here</div>");
    // Java: '\i' is 'i' in a JavaScript string, so the Java literals have no backslash there.
    assertThat(page.getByTitle("my title", new Page.GetByTitleOptions().setExact(true))).hasCount(1, new LocatorAssertions.HasCountOptions().setTimeout(500));
    assertThat(page.getByTitle("my title", new Page.GetByTitleOptions().setExact(true))).hasCount(1, new LocatorAssertions.HasCountOptions().setTimeout(500));
    assertThat(page.getByTitle("my t\\itle", new Page.GetByTitleOptions().setExact(true))).hasCount(0, new LocatorAssertions.HasCountOptions().setTimeout(500));
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
  @DisplayName("SE-091 getByRole escaping")
  void getByRoleEscaping(Page page) {
    page.setContent("\n" +
      "    <a href=\"https://playwright.dev\">issues 123</a>\n" +
      "    <a href=\"https://playwright.dev\">he llo 56</a>\n" +
      "    <button>Click me</button>\n" +
      "  ");
    String outerHTML = "els => els.map(e => e.outerHTML)";
    assertEquals(asList("<button>Click me</button>"), page.getByRole(AriaRole.BUTTON).evaluateAll(outerHTML));
    assertEquals(asList(
      "<a href=\"https://playwright.dev\">issues 123</a>",
      "<a href=\"https://playwright.dev\">he llo 56</a>"),
      page.getByRole(AriaRole.LINK).evaluateAll(outerHTML));

    assertEquals(asList("<a href=\"https://playwright.dev\">issues 123</a>"),
      page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("issues 123")).evaluateAll(outerHTML));
    assertEquals(asList("<a href=\"https://playwright.dev\">issues 123</a>"),
      page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("sues")).evaluateAll(outerHTML));
    assertEquals(asList("<a href=\"https://playwright.dev\">he llo 56</a>"),
      page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("  he    \n  llo ")).evaluateAll(outerHTML));
    assertEquals(asList(),
      page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("issues")).evaluateAll(outerHTML));

    assertEquals(asList(),
      page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("sues").setExact(true)).evaluateAll(outerHTML));
    assertEquals(asList("<a href=\"https://playwright.dev\">he llo 56</a>"),
      page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("   he \n llo 56 ").setExact(true)).evaluateAll(outerHTML));

    assertEquals(asList("<button>Click me</button>"),
      page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Click me").setExact(true)).evaluateAll(outerHTML));
    // Java: '\m' is 'm' in a JavaScript string, so this literal has no backslash.
    assertEquals(asList("<button>Click me</button>"),
      page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Click me").setExact(true)).evaluateAll(outerHTML));
    assertEquals(asList(),
      page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Click \\me").setExact(true)).evaluateAll(outerHTML));
    assertEquals(asList(),
      page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Click \\me").setExact(true)).evaluateAll(outerHTML));
    assertEquals(asList(),
      page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Click \\\\me").setExact(true)).evaluateAll(outerHTML));
  }

  @PlaywrightTest
  @DisplayName("SE-092 getByRole should accept regexp with v flag")
  void getByRoleShouldAcceptRegexpWithVFlag(Page page) {
    // Regression test for https://github.com/microsoft/playwright/issues/41457
    page.setContent("<button>Click me</button><button>Submit</button>");
    // Java: java.util.regex has no v flag, so the patterns have no flag for it.
    assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("Click me")))).hasCount(1);
    assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("click me", Pattern.CASE_INSENSITIVE)))).hasCount(1);
    assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("Missing")))).hasCount(0, new LocatorAssertions.HasCountOptions().setTimeout(1000));
  }

  @PlaywrightTest
  @DisplayName("SE-093 getByRole with description")
  void getByRoleWithDescription(Page page) {
    page.setContent("\n" +
      "    <div role=\"alert\" aria-label=\"Upload successful\" aria-description=\"File doc-2025.pdf was uploaded successfully\">Alert 1</div>\n" +
      "    <div role=\"alert\" aria-label=\"Upload successful\" aria-description=\"File report-2026.pdf was uploaded successfully\">Alert 2</div>\n" +
      "    <div role=\"alert\" aria-label=\"Invalid file\" aria-description=\"File demo.doc has an invalid file format\">Alert 3</div>\n" +
      "  ");
    String textContent = "els => els.map(e => e.textContent)";

    // description substring match (default)
    assertEquals(asList("Alert 1"),
      page.getByRole(AriaRole.ALERT, new Page.GetByRoleOptions().setDescription("doc-2025")).evaluateAll(textContent));
    assertEquals(asList("Alert 2"),
      page.getByRole(AriaRole.ALERT, new Page.GetByRoleOptions().setDescription("report-2026")).evaluateAll(textContent));

    // description with name combined
    assertEquals(asList("Alert 1"),
      page.getByRole(AriaRole.ALERT, new Page.GetByRoleOptions().setName("Upload successful").setDescription("doc-2025")).evaluateAll(textContent));
    assertEquals(asList("Alert 2"),
      page.getByRole(AriaRole.ALERT, new Page.GetByRoleOptions().setName("Upload successful").setDescription("report-2026")).evaluateAll(textContent));
    assertEquals(asList(),
      page.getByRole(AriaRole.ALERT, new Page.GetByRoleOptions().setName("Invalid file").setDescription("doc-2025")).evaluateAll(textContent));

    // description exact match
    assertEquals(asList(),
      page.getByRole(AriaRole.ALERT, new Page.GetByRoleOptions().setDescription("doc-2025").setExact(true)).evaluateAll(textContent));
    assertEquals(asList("Alert 1"),
      page.getByRole(AriaRole.ALERT, new Page.GetByRoleOptions().setDescription("File doc-2025.pdf was uploaded successfully").setExact(true)).evaluateAll(textContent));

    // description regex match
    assertEquals(asList("Alert 2"),
      page.getByRole(AriaRole.ALERT, new Page.GetByRoleOptions().setDescription(Pattern.compile("report-\\d+"))).evaluateAll(textContent));
    assertEquals(asList("Alert 1", "Alert 2"),
      page.getByRole(AriaRole.ALERT, new Page.GetByRoleOptions().setDescription(Pattern.compile("uploaded successfully$"))).evaluateAll(textContent));
  }

  @PlaywrightTest
  @DisplayName("SE-094 getByRole with description via aria-describedby")
  void getByRoleWithDescriptionViaAriaDescribedby(Page page) {
    page.setContent("\n" +
      "    <button aria-describedby=\"desc1\">Submit</button>\n" +
      "    <span id=\"desc1\">Submits the form data</span>\n" +
      "    <button aria-describedby=\"desc2\">Submit</button>\n" +
      "    <span id=\"desc2\">Saves as draft</span>\n" +
      "  ");

    assertEquals(asList("Submit"),
      page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Submit").setDescription("form data")).evaluateAll("els => els.map(e => e.textContent)"));
    assertEquals(asList("Submit"),
      page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Submit").setDescription("draft")).evaluateAll("els => els.map(e => e.textContent)"));
  }

  @PlaywrightTest
  @DisplayName("SE-095 getByRole with description via title fallback")
  void getByRoleWithDescriptionViaTitleFallback(Page page) {
    page.setContent("\n" +
      "    <button title=\"Submits the form\">Submit</button>\n" +
      "    <button title=\"Resets the form\">Reset</button>\n" +
      "  ");

    assertEquals(asList("Submit"),
      page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setDescription("Submits")).evaluateAll("els => els.map(e => e.textContent)"));
    assertEquals(asList("Reset"),
      page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setDescription("Resets")).evaluateAll("els => els.map(e => e.textContent)"));
  }

  @PlaywrightTest
  @DisplayName("SE-096 getByRole with description whitespace normalization")
  void getByRoleWithDescriptionWhitespaceNormalization(Page page) {
    page.setContent("\n" +
      "    <div role=\"alert\" aria-description=\"File  doc-2025.pdf   was uploaded   successfully\">Alert</div>\n" +
      "  ");

    assertEquals(asList("Alert"),
      page.getByRole(AriaRole.ALERT, new Page.GetByRoleOptions().setDescription("  doc-2025.pdf \n was  uploaded ")).evaluateAll("els => els.map(e => e.textContent)"));
  }

  @PlaywrightTest
  @DisplayName("SE-097 locators should not be affected by global and sticky regex flags")
  void locatorsShouldNotBeAffectedByGlobalAndStickyRegexFlags(Page page) {
    page.setContent("\n" +
      "    <div>foo</div><div>foo</div><div>foo</div>\n" +
      "    <label>foo<input></label><label>foo<input></label><label>foo<input></label>\n" +
      "    <button>foo</button><button>foo</button><button>foo</button>\n" +
      "    <input placeholder=foo><input placeholder=foo><input placeholder=foo>\n" +
      "  ");
    // Java: java.util.regex has no g and y flags, so /foo/g and /foo/y are the same pattern as /foo/.
    assertThat(page.getByText(Pattern.compile("foo"))).hasCount(9);
    assertThat(page.getByText(Pattern.compile("foo"))).hasCount(9);
    assertThat(page.getByText(Pattern.compile("foo"))).hasCount(9);
    assertThat(page.getByLabel(Pattern.compile("foo"))).hasCount(3);
    assertThat(page.getByLabel(Pattern.compile("foo"))).hasCount(3);
    assertThat(page.getByLabel(Pattern.compile("foo"))).hasCount(3);
    assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("foo")))).hasCount(3);
    assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("foo")))).hasCount(3);
    assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("foo")))).hasCount(3);
    assertThat(page.getByPlaceholder(Pattern.compile("foo"))).hasCount(3);
    assertThat(page.getByPlaceholder(Pattern.compile("foo"))).hasCount(3);
    assertThat(page.getByPlaceholder(Pattern.compile("foo"))).hasCount(3);
  }
}
