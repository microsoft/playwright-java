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

import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.options.AriaRole;

import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.isomorphic.LocatorGenerators.asLocator;
import static com.microsoft.playwright.impl.isomorphic.LocatorGenerators.asLocatorDescription;
import static com.microsoft.playwright.impl.isomorphic.LocatorGenerators.asLocators;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

// The java expectations of upstream tests/library/locator-generator.spec.ts.
public class TestLocatorGenerators {
  private static String generate(Locator locator) {
    return locator.toString();
  }

  @PlaywrightTest
  void reverseEngineerLocators(Page page) {
    assertEquals("getByTestId(\"Hello\")", generate(page.getByTestId("Hello")));
    assertEquals("getByTestId(\"He\\\"llo\")", generate(page.getByTestId("He\"llo")));
    assertEquals("getByTestId(Pattern.compile(\"He\\\"llo\"))", generate(page.getByTestId(Pattern.compile("He\"llo"))));
    assertEquals("getByTestId(Pattern.compile(\"He\\\\\\\\\\\"llo\"))", generate(page.getByTestId(Pattern.compile("He\\\\\"llo"))));
    assertEquals("getByText(\"Hello\", new Page.GetByTextOptions().setExact(true))", generate(page.getByText("Hello", new Page.GetByTextOptions().setExact(true))));
    assertEquals("getByText(\"Hello\")", generate(page.getByText("Hello")));
    assertEquals("getByText(Pattern.compile(\"Hello\"))", generate(page.getByText(Pattern.compile("Hello"))));
    assertEquals("getByLabel(\"Name\")", generate(page.getByLabel("Name")));
    assertEquals("getByLabel(\"Last Name\", new Page.GetByLabelOptions().setExact(true))", generate(page.getByLabel("Last Name", new Page.GetByLabelOptions().setExact(true))));
    assertEquals("getByLabel(Pattern.compile(\"Last\\\\s+name\", Pattern.CASE_INSENSITIVE))", generate(page.getByLabel(Pattern.compile("Last\\s+name", Pattern.CASE_INSENSITIVE))));
    // Only JavaScript can express these flags, other languages drop them.
    for (String flags : asList("u", "s", "y", "d", "v", "gm")) {
      for (String[] engineAndMethod : asList(new String[] {"text", "getByText"}, new String[] {"label", "getByLabel"})) {
        String selector = "internal:" + engineAndMethod[0] + "=/Hello/" + flags;
        String suffix = "";
        if (flags.contains("s"))
          suffix = ", Pattern.DOTALL";
        if (flags.contains("m"))
          suffix = ", Pattern.MULTILINE";
        assertEquals(engineAndMethod[1] + "(Pattern.compile(\"Hello\"" + suffix + "))", asLocator("java", selector), selector);
      }
    }

    assertEquals("getByPlaceholder(\"hello\")", generate(page.getByPlaceholder("hello")));
    assertEquals("getByPlaceholder(\"Hello\", new Page.GetByPlaceholderOptions().setExact(true))", generate(page.getByPlaceholder("Hello", new Page.GetByPlaceholderOptions().setExact(true))));
    assertEquals("getByPlaceholder(Pattern.compile(\"wor\", Pattern.CASE_INSENSITIVE))", generate(page.getByPlaceholder(Pattern.compile("wor", Pattern.CASE_INSENSITIVE))));

    assertEquals("getByAltText(\"hello\")", generate(page.getByAltText("hello")));
    assertEquals("getByAltText(\"Hello\", new Page.GetByAltTextOptions().setExact(true))", generate(page.getByAltText("Hello", new Page.GetByAltTextOptions().setExact(true))));
    assertEquals("getByAltText(Pattern.compile(\"wor\", Pattern.CASE_INSENSITIVE))", generate(page.getByAltText(Pattern.compile("wor", Pattern.CASE_INSENSITIVE))));

    assertEquals("getByTitle(\"hello\")", generate(page.getByTitle("hello")));
    assertEquals("getByTitle(\"Hello\", new Page.GetByTitleOptions().setExact(true))", generate(page.getByTitle("Hello", new Page.GetByTitleOptions().setExact(true))));
    assertEquals("getByTitle(Pattern.compile(\"wor\", Pattern.CASE_INSENSITIVE))", generate(page.getByTitle(Pattern.compile("wor", Pattern.CASE_INSENSITIVE))));
    assertEquals("getByPlaceholder(\"hello my\\nwo\\\"rld\")", generate(page.getByPlaceholder("hello my\nwo\"rld")));
    assertEquals("getByAltText(\"hello my\\nwo\\\"rld\")", generate(page.getByAltText("hello my\nwo\"rld")));
    assertEquals("getByTitle(\"hello my\\nwo\\\"rld\")", generate(page.getByTitle("hello my\nwo\"rld")));
  }

  @PlaywrightTest
  void reverseEngineerGetByRole(Page page) {
    assertEquals("getByRole(AriaRole.BUTTON)", generate(page.getByRole(AriaRole.BUTTON)));
    assertEquals("getByRole(AriaRole.HEADING)", generate(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions())));
    assertEquals("getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(\"Hello\"))", generate(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Hello"))));
    assertEquals("getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile(\"Hello\")))", generate(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("Hello")))));
    assertEquals("getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(\"He\\\"llo\").setExact(true))", generate(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("He\"llo").setExact(true))));
    assertEquals("getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setChecked(true).setLevel(3).setPressed(false))", generate(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setChecked(true).setPressed(false).setLevel(3))));
    assertEquals("getByRole(AriaRole.ALERT, new Page.GetByRoleOptions().setName(\"Upload\").setDescription(\"doc.pdf\"))", generate(page.getByRole(AriaRole.ALERT, new Page.GetByRoleOptions().setName("Upload").setDescription("doc.pdf"))));
    assertEquals("getByRole(AriaRole.ALERT, new Page.GetByRoleOptions().setDescription(\"doc.pdf\"))", generate(page.getByRole(AriaRole.ALERT, new Page.GetByRoleOptions().setDescription("doc.pdf"))));
    assertEquals("getByRole(AriaRole.ALERT, new Page.GetByRoleOptions().setDescription(Pattern.compile(\"doc\\\\.pdf\")))", generate(page.getByRole(AriaRole.ALERT, new Page.GetByRoleOptions().setDescription(Pattern.compile("doc\\.pdf")))));
    assertEquals("getByRole(AriaRole.ALERT, new Page.GetByRoleOptions().setName(\"Upload\").setDescription(\"doc.pdf\").setExact(true))", generate(page.getByRole(AriaRole.ALERT, new Page.GetByRoleOptions().setName("Upload").setDescription("doc.pdf").setExact(true))));
  }

  @PlaywrightTest
  void refusesToTranslateInternalRoleWithConflictingNameDescriptionExactness() {
    // https://github.com/microsoft/playwright/issues/41032
    String conflicting = "internal:role=row[name=\"abc\"i][description=\"d\"s]";
    String conflictingReversed = "internal:role=row[name=\"abc\"s][description=\"d\"i]";
    assertEquals(conflicting, asLocator("java", conflicting));
    assertEquals(conflictingReversed, asLocator("java", conflictingReversed));
  }

  @PlaywrightTest
  void reverseEngineerInternalRoleWithRegexAndStringNameDescription() {
    assertEquals("getByRole(AriaRole.ALERT, new Page.GetByRoleOptions().setName(Pattern.compile(\"Upload\")).setDescription(\"doc.pdf\"))",
      asLocator("java", "internal:role=alert[name=/Upload/][description=\"doc.pdf\"i]"));
    assertEquals("getByRole(AriaRole.ALERT, new Page.GetByRoleOptions().setName(\"Upload\").setDescription(Pattern.compile(\"doc\\\\.pdf\")))",
      asLocator("java", "internal:role=alert[name=\"Upload\"i][description=/doc\\.pdf/]"));
  }

  @PlaywrightTest
  void reverseEngineerIgnoreCaseLocators(Page page) {
    assertEquals("getByText(\"hello my\\nwo\\\"rld\")", generate(page.getByText("hello my\nwo\"rld")));
    assertEquals("getByText(\"hello       my     wo\\\"rld\")", generate(page.getByText("hello       my     wo\"rld")));
    assertEquals("getByLabel(\"hello my\\nwo\\\"rld\")", generate(page.getByLabel("hello my\nwo\"rld")));
  }

  @PlaywrightTest
  void reverseEngineerOrderedLocators(Page page) {
    assertEquals("locator(\"div\").nth(3).first().last()", generate(page.locator("div").nth(3).first().last()));
  }

  @PlaywrightTest
  void reverseEngineerLocatorsWithRegex(Page page) {
    assertEquals("getByText(Pattern.compile(\"he\\\\/\\\\sl\\\\nlo\"))", generate(page.getByText(Pattern.compile("he\\/\\sl\\nlo"))));
    assertEquals("getByPlaceholder(Pattern.compile(\"he\\\\/\\\\sl\\\\nlo\"))", generate(page.getByPlaceholder(Pattern.compile("he\\/\\sl\\nlo"))));
    assertEquals("getByText(Pattern.compile(\"hel\\\"lo\"))", generate(page.getByText(Pattern.compile("hel\"lo"))));
    assertEquals("getByPlaceholder(Pattern.compile(\"hel\\\"lo\"))", generate(page.getByPlaceholder(Pattern.compile("hel\"lo"))));
  }

  @PlaywrightTest
  void reverseEngineerHasText(Page page) {
    assertEquals("getByText(\"Hello\").filter(new Locator.FilterOptions().setHasText(\"wo\\\"rld\\n\"))",
      generate(page.getByText("Hello").filter(new Locator.FilterOptions().setHasText("wo\"rld\n"))));
    assertEquals("getByText(\"Hello\").filter(new Locator.FilterOptions().setHasText(Pattern.compile(\"wo\\\\/\\\\srld\\\\n\")))",
      generate(page.getByText("Hello").filter(new Locator.FilterOptions().setHasText(Pattern.compile("wo\\/\\srld\\n")))));
    assertEquals("getByText(\"Hello\").filter(new Locator.FilterOptions().setHasText(Pattern.compile(\"wor\\\"ld\")))",
      generate(page.getByText("Hello").filter(new Locator.FilterOptions().setHasText(Pattern.compile("wor\"ld")))));
  }

  @PlaywrightTest
  void reverseEngineerHasNotText(Page page) {
    assertEquals("getByText(\"Hello\").filter(new Locator.FilterOptions().setHasNotText(\"wo\\\"rld\\n\"))",
      generate(page.getByText("Hello").filter(new Locator.FilterOptions().setHasNotText("wo\"rld\n"))));
  }

  @PlaywrightTest
  void reverseEngineerVisible(Page page) {
    assertEquals("getByText(\"Hello\").visible().locator(\"div\")", generate(page.getByText("Hello").visible().locator("div")));
    assertEquals("getByText(\"Hello\").filter(new Locator.FilterOptions().setVisible(false)).locator(\"div\")",
      generate(page.getByText("Hello").filter(new Locator.FilterOptions().setVisible(false)).locator("div")));
  }

  @PlaywrightTest
  void reverseEngineerHas(Page page) {
    assertEquals("getByText(\"Hello\").filter(new Locator.FilterOptions().setHas(locator(\"div\").getByText(\"bye\")))",
      generate(page.getByText("Hello").filter(new Locator.FilterOptions().setHas(page.locator("div").getByText("bye")))));

    Locator locator = page
      .locator("section")
      .filter(new Locator.FilterOptions().setHas(page.locator("div").filter(new Locator.FilterOptions().setHas(page.locator("span")))))
      .filter(new Locator.FilterOptions().setHasText("foo"))
      .filter(new Locator.FilterOptions().setHas(page.locator("a")));
    assertEquals("locator(\"section\").filter(new Locator.FilterOptions().setHas(locator(\"div\").filter(new Locator.FilterOptions().setHas(locator(\"span\"))))).filter(new Locator.FilterOptions().setHasText(\"foo\")).filter(new Locator.FilterOptions().setHas(locator(\"a\")))",
      generate(locator));
  }

  @PlaywrightTest
  void reverseEngineerHasNot(Page page) {
    assertEquals("getByText(\"Hello\").filter(new Locator.FilterOptions().setHasNot(locator(\"div\").getByText(\"bye\")))",
      generate(page.getByText("Hello").filter(new Locator.FilterOptions().setHasNot(page.locator("div").getByText("bye")))));

    Locator locator = page
      .locator("section")
      .filter(new Locator.FilterOptions().setHas(page.locator("div").filter(new Locator.FilterOptions().setHasNot(page.locator("span")))))
      .filter(new Locator.FilterOptions().setHasText("foo"))
      .filter(new Locator.FilterOptions().setHasNot(page.locator("a")));
    assertEquals("locator(\"section\").filter(new Locator.FilterOptions().setHas(locator(\"div\").filter(new Locator.FilterOptions().setHasNot(locator(\"span\"))))).filter(new Locator.FilterOptions().setHasText(\"foo\")).filter(new Locator.FilterOptions().setHasNot(locator(\"a\")))",
      generate(locator));
  }

  @PlaywrightTest
  void reverseEngineerHasAndHasText(Page page) {
    Locator locator = page.locator("section").filter(new Locator.FilterOptions().setHasText("foo").setHas(page.locator("div"))).locator("a");
    assertEquals("locator(\"section\").filter(new Locator.FilterOptions().setHasText(\"foo\")).filter(new Locator.FilterOptions().setHas(locator(\"div\"))).locator(\"a\")",
      generate(locator));
  }

  @PlaywrightTest
  void reverseEngineerFrameLocator(Page page) {
    Locator locator = page
      .frameLocator("iframe")
      .getByText("foo", new FrameLocator.GetByTextOptions().setExact(true))
      .frameLocator("frame").first()
      .frameLocator("iframe")
      .locator("span");
    assertEquals("locator(\"iframe\").contentFrame().getByText(\"foo\", new FrameLocator.GetByTextOptions().setExact(true)).locator(\"frame\").first().contentFrame().locator(\"iframe\").contentFrame().locator(\"span\")",
      generate(locator));

    // Note that frame locators with ">>" are not restored back due to ambiguity.
    assertEquals("locator(\"div\").locator(\"iframe\").contentFrame().locator(\"span\")", generate(page.frameLocator("div >> iframe").locator("span")));

    assertEquals(asList("getByTitle(\"iframe title\").contentFrame()"), asLocators("java", "internal:attr=[title=\"iframe title\"i] >> internal:control=enter-frame"));
  }

  @PlaywrightTest
  void reverseEngineerFrameLocatorWithoutASelector(Page page) {
    assertEquals("frameLocator().getByText(\"foo\").locator(\"span\")", generate(page.frameLocator().getByText("foo").locator("span")));
    assertEquals("frameLocator()", asLocator("java", "internal:control=any-frame"));
  }

  @PlaywrightTest
  void generateMultipleLocators(Page page) {
    String selector = "div >> internal:has-text=\"foo\"i >> nth=0 >> internal:has=\"span >> internal:has-not-text=\\\"bar\\\"i >> nth=-1\"";
    Locator locator = page.locator("div", new Page.LocatorOptions().setHasText("foo")).nth(0).filter(new Locator.FilterOptions().setHas(page.locator("span", new Page.LocatorOptions().setHasNotText("bar")).nth(-1)));
    assertEquals(asLocator("java", selector), generate(locator));
    assertEquals(asList(
      "locator(\"div\").filter(new Locator.FilterOptions().setHasText(\"foo\")).first().filter(new Locator.FilterOptions().setHas(locator(\"span\").filter(new Locator.FilterOptions().setHasNotText(\"bar\")).last()))",
      "locator(\"div\").filter(new Locator.FilterOptions().setHasText(\"foo\")).first().filter(new Locator.FilterOptions().setHas(locator(\"span\").filter(new Locator.FilterOptions().setHasNotText(\"bar\")).nth(-1)))",
      "locator(\"div\").filter(new Locator.FilterOptions().setHasText(\"foo\")).first().filter(new Locator.FilterOptions().setHas(locator(\"span\", new Page.LocatorOptions().setHasNotText(\"bar\")).last()))",
      "locator(\"div\").filter(new Locator.FilterOptions().setHasText(\"foo\")).first().filter(new Locator.FilterOptions().setHas(locator(\"span\", new Page.LocatorOptions().setHasNotText(\"bar\")).nth(-1)))",
      "locator(\"div\").filter(new Locator.FilterOptions().setHasText(\"foo\")).nth(0).filter(new Locator.FilterOptions().setHas(locator(\"span\").filter(new Locator.FilterOptions().setHasNotText(\"bar\")).last()))",
      "locator(\"div\").filter(new Locator.FilterOptions().setHasText(\"foo\")).nth(0).filter(new Locator.FilterOptions().setHas(locator(\"span\").filter(new Locator.FilterOptions().setHasNotText(\"bar\")).nth(-1)))",
      "locator(\"div\").filter(new Locator.FilterOptions().setHasText(\"foo\")).nth(0).filter(new Locator.FilterOptions().setHas(locator(\"span\", new Page.LocatorOptions().setHasNotText(\"bar\")).last()))",
      "locator(\"div\").filter(new Locator.FilterOptions().setHasText(\"foo\")).nth(0).filter(new Locator.FilterOptions().setHas(locator(\"span\", new Page.LocatorOptions().setHasNotText(\"bar\")).nth(-1)))",
      "locator(\"div\", new Page.LocatorOptions().setHasText(\"foo\")).first().filter(new Locator.FilterOptions().setHas(locator(\"span\").filter(new Locator.FilterOptions().setHasNotText(\"bar\")).last()))",
      "locator(\"div\", new Page.LocatorOptions().setHasText(\"foo\")).first().filter(new Locator.FilterOptions().setHas(locator(\"span\").filter(new Locator.FilterOptions().setHasNotText(\"bar\")).nth(-1)))",
      "locator(\"div\", new Page.LocatorOptions().setHasText(\"foo\")).first().filter(new Locator.FilterOptions().setHas(locator(\"span\", new Page.LocatorOptions().setHasNotText(\"bar\")).last()))",
      "locator(\"div\", new Page.LocatorOptions().setHasText(\"foo\")).first().filter(new Locator.FilterOptions().setHas(locator(\"span\", new Page.LocatorOptions().setHasNotText(\"bar\")).nth(-1)))",
      "locator(\"div\", new Page.LocatorOptions().setHasText(\"foo\")).nth(0).filter(new Locator.FilterOptions().setHas(locator(\"span\").filter(new Locator.FilterOptions().setHasNotText(\"bar\")).last()))",
      "locator(\"div\", new Page.LocatorOptions().setHasText(\"foo\")).nth(0).filter(new Locator.FilterOptions().setHas(locator(\"span\").filter(new Locator.FilterOptions().setHasNotText(\"bar\")).nth(-1)))",
      "locator(\"div\", new Page.LocatorOptions().setHasText(\"foo\")).nth(0).filter(new Locator.FilterOptions().setHas(locator(\"span\", new Page.LocatorOptions().setHasNotText(\"bar\")).last()))",
      "locator(\"div\", new Page.LocatorOptions().setHasText(\"foo\")).nth(0).filter(new Locator.FilterOptions().setHas(locator(\"span\", new Page.LocatorOptions().setHasNotText(\"bar\")).nth(-1)))"
    ), asLocators("java", selector));
  }

  @PlaywrightTest
  void reverseEngineerInternalHasTextLocators() {
    assertEquals("locator(\"div\").filter(new Locator.FilterOptions().setHasText(\"Goodbye world\")).locator(\"span\")", asLocator("java", "div >> internal:has-text=\"Goodbye world\"i >> span"));
    assertEquals("locator(\"div\").locator(\"internal:has-text=\\\"foo\\\"s\")", asLocator("java", "div >> internal:has-text=\"foo\"s"));
    assertEquals("locator(\"div\").locator(\"internal:has-not-text=\\\"foo\\\"s\")", asLocator("java", "div >> internal:has-not-text=\"foo\"s"));
  }

  @PlaywrightTest
  void asLocatorInternalAnd() {
    assertEquals("locator(\"div\").and(locator(\"span\").locator(\"article\"))", asLocator("java", "div >> internal:and=\"span >> article\""));
  }

  @PlaywrightTest
  void asLocatorInternalOr() {
    assertEquals("locator(\"div\").or(locator(\"span\").locator(\"article\"))", asLocator("java", "div >> internal:or=\"span >> article\""));
  }

  @PlaywrightTest
  void asLocatorInternalChain() {
    assertEquals("locator(\"div\").locator(locator(\"span\").locator(\"article\"))", asLocator("java", "div >> internal:chain=\"span >> article\""));
  }

  @PlaywrightTest
  void reverseEngineerNestedLocators(Page page) {
    assertEquals("getByRole(AriaRole.ROW).locator(getByRole(AriaRole.CELL).nth(2))", generate(page.getByRole(AriaRole.ROW).locator(page.getByRole(AriaRole.CELL).nth(2))));
    assertEquals("locator(\"div\").and(getByText(\"foo\")).or(locator(\"span\").locator(locator(\"a\"))).first()",
      generate(page.locator("div").and(page.getByText("foo")).or(page.locator("span").locator(page.locator("a"))).first()));
  }

  @PlaywrightTest
  void asLocatorXpath() {
    String selector = "//*[contains(normalizer-text(), 'foo']";
    assertEquals("locator(\"//*[contains(normalizer-text(), 'foo']\")", asLocator("java", selector));
  }

  @PlaywrightTest
  void reverseEngineerRegexFlags() {
    assertEquals("getByText(Pattern.compile(\"a.b\", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL))", asLocator("java", "internal:text=/a.b/ims"));
    assertEquals("getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile(\"a.b\", Pattern.DOTALL)))", asLocator("java", "internal:role=button[name=/a.b/s]"));
  }

  @PlaywrightTest
  void asLocatorDescriptionInvalidInput() {
    assertEquals("desc", asLocatorDescription("java", "body >> internal:describe=\"desc\""));
    assertEquals("locator(\"body\")", asLocatorDescription("java", "body >> internal:describe=12"));
    assertEquals("div >> internal:has=bad", asLocatorDescription("java", "div >> internal:has=bad"));
    // The client does not parse css, so an invalid css selector is not rejected.
    assertEquals("locator(\"following-sibling::*[1]\")", asLocatorDescription("java", "following-sibling::*[1]"));
    assertEquals("locator(\"body\").locator(\"div\")", asLocatorDescription("java", "body >> internal:describe=\"desc\" >> div"));
  }

  @PlaywrightTest
  void toStringShouldPrintTheDescription(Page page) {
    assertEquals("Submit button", page.locator("#submit").describe("Submit button").toString());
    assertEquals("getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(\"Submit\"))", page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Submit")).toString());
  }
}
