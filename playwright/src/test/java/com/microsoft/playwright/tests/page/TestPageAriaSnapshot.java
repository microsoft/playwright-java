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

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.expectToContainYaml;
import static com.microsoft.playwright.fixtures.Utils.unshift;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-aria-snapshot.spec.ts. */
public class TestPageAriaSnapshot {
  private static void checkAndMatchSnapshot(Locator locator, String snapshot) {
    assertEquals(unshift(snapshot), locator.ariaSnapshot());
    assertThat(locator).matchesAriaSnapshot(snapshot);
  }

  @PlaywrightTest
  @DisplayName("AR-001 should snapshot")
  void shouldSnapshot(Page page) {
    page.setContent("<h1>title</h1>");
    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - heading \"title\" [level=1]\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-002 should snapshot list")
  void shouldSnapshotList(Page page) {
    page.setContent("\n" +
        "    <h1>title</h1>\n" +
        "    <h1>title 2</h1>\n" +
        "  ");
    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - heading \"title\" [level=1]\n" +
        "    - heading \"title 2\" [level=1]\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-003 should snapshot list with accessible name")
  void shouldSnapshotListWithAccessibleName(Page page) {
    page.setContent("\n" +
        "    <ul aria-label=\"my list\">\n" +
        "      <li>one</li>\n" +
        "      <li>two</li>\n" +
        "    </ul>\n" +
        "  ");
    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - list \"my list\":\n" +
        "      - listitem: one\n" +
        "      - listitem: two\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-004 should quote names that look like regexes")
  void shouldQuoteNamesThatLookLikeRegexes(Page page) {
    page.setContent("\n" +
        "    <nav>\n" +
        "      <a href=\"/\">/</a>\n" +
        "      <a href=\"/docs/\">/docs/</a>\n" +
        "      <a href=\"/a(b/\">/a(b/</a>\n" +
        "    </nav>\n" +
        "  ");
    checkAndMatchSnapshot(page.locator("nav"), "\n" +
        "    - navigation:\n" +
        "      - link \"/\":\n" +
        "        - /url: /\n" +
        "      - link \"/docs/\":\n" +
        "        - /url: /docs/\n" +
        "      - link \"/a(b/\":\n" +
        "        - /url: /a(b/\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-005 should snapshot complex")
  void shouldSnapshotComplex(Page page) {
    page.setContent("\n" +
        "    <ul>\n" +
        "      <li>\n" +
        "        <a href='about:blank'>link</a>\n" +
        "      </li>\n" +
        "    </ul>\n" +
        "  ");
    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - list:\n" +
        "      - listitem:\n" +
        "        - link \"link\":\n" +
        "          - /url: about:blank\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-006 should allow text nodes")
  void shouldAllowTextNodes(Page page) {
    page.setContent("\n" +
        "    <h1>Microsoft</h1>\n" +
        "    <div>Open source projects and samples from Microsoft</div>\n" +
        "  ");

    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - heading \"Microsoft\" [level=1]\n" +
        "    - text: Open source projects and samples from Microsoft\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-007 should snapshot details visibility")
  void shouldSnapshotDetailsVisibility(Page page) {
    page.setContent("\n" +
        "    <details>\n" +
        "      <summary>Summary</summary>\n" +
        "      <div>Details</div>\n" +
        "    </details>\n" +
        "  ");

    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - group: Summary\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-008 should not snapshot text directly inside closed details")
  @Fixme("driver: the bundled driver includes text inside closed details, snapshot is - group: Summary Details")
  void shouldNotSnapshotTextDirectlyInsideClosedDetails(Page page) {

    page.setContent("\n" +
        "    <details>\n" +
        "      <summary>Summary</summary>\n" +
        "      Details\n" +
        "    </details>\n" +
        "  ");

    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - group: Summary\n" +
        "  ");

    page.evalOnSelector("details", "details => details.open = true");
    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - group: Summary Details\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-009 should not snapshot text that is not assigned to a slot")
  @Fixme("driver: the bundled driver includes text not assigned to a slot, snapshot is - button \"Light Shadow\": - text: Light - paragraph: Shadow")
  void shouldNotSnapshotTextThatIsNotAssignedToASlot(Page page) {

    page.setContent("\n" +
        "    <div role=button>Light</div>\n" +
        "    <script>\n" +
        "      document.querySelector('div').attachShadow({ mode: 'open' }).innerHTML = '<p>Shadow</p>';\n" +
        "    </script>\n" +
        "  ");

    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - button \"Shadow\":\n" +
        "      - paragraph: Shadow\n" +
        "  ");
    assertThat(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Shadow").setExact(true))).isVisible();
  }

  @PlaywrightTest
  @DisplayName("AR-010 should snapshot integration")
  void shouldSnapshotIntegration(Page page) {
    page.setContent("\n" +
        "    <h1>Microsoft</h1>\n" +
        "    <div>Open source projects and samples from Microsoft</div>\n" +
        "    <ul>\n" +
        "      <li>\n" +
        "        <details>\n" +
        "          <summary>\n" +
        "            Verified\n" +
        "          </summary>\n" +
        "          <div>\n" +
        "            <div>\n" +
        "              <p>\n" +
        "                We've verified that the organization <strong>microsoft</strong> controls the domain:\n" +
        "              </p>\n" +
        "              <ul>\n" +
        "                <li class=\"mb-1\">\n" +
        "                  <strong>opensource.microsoft.com</strong>\n" +
        "                </li>\n" +
        "              </ul>\n" +
        "              <div>\n" +
        "                <a href=\"about: blank\">Learn more about verified organizations</a>\n" +
        "              </div>\n" +
        "            </div>\n" +
        "          </div>\n" +
        "        </details>\n" +
        "      </li>\n" +
        "      <li>\n" +
        "        <a href=\"about:blank\">\n" +
        "          <summary title=\"Label: GitHub Sponsor\">Sponsor</summary>\n" +
        "        </a>\n" +
        "      </li>\n" +
        "    </ul>");

    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - heading \"Microsoft\" [level=1]\n" +
        "    - text: Open source projects and samples from Microsoft\n" +
        "    - list:\n" +
        "      - listitem:\n" +
        "        - group: Verified\n" +
        "      - listitem:\n" +
        "        - link \"Sponsor\":\n" +
        "          - /url: about:blank\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-011 should support multiline text")
  void shouldSupportMultilineText(Page page) {
    page.setContent("\n" +
        "    <p>\n" +
        "      Line 1\n" +
        "      Line 2\n" +
        "      Line 3\n" +
        "    </p>\n" +
        "  ");

    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - paragraph: Line 1 Line 2 Line 3\n" +
        "  ");
    assertThat(page.locator("body")).matchesAriaSnapshot("\n" +
        "    - paragraph: |\n" +
        "          Line 1\n" +
        "          Line 2\n" +
        "          Line 3\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-012 should concatenate span text")
  void shouldConcatenateSpanText(Page page) {
    page.setContent("\n" +
        "    <span>One</span> <span>Two</span> <span>Three</span>\n" +
        "  ");

    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - text: One Two Three\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-013 should concatenate span text 2")
  void shouldConcatenateSpanText2(Page page) {
    page.setContent("\n" +
        "    <span>One </span><span>Two </span><span>Three</span>\n" +
        "  ");

    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - text: One Two Three\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-014 should concatenate div text with spaces")
  void shouldConcatenateDivTextWithSpaces(Page page) {
    page.setContent("\n" +
        "    <div>One</div><div>Two</div><div>Three</div>\n" +
        "  ");

    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - text: One Two Three\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-015 should include pseudo in text")
  void shouldIncludePseudoInText(Page page) {
    page.setContent("\n" +
        "    <style>\n" +
        "      span:before {\n" +
        "        content: 'world';\n" +
        "      }\n" +
        "      div:after {\n" +
        "        content: 'bye';\n" +
        "      }\n" +
        "    </style>\n" +
        "    <a href=\"about:blank\">\n" +
        "      <span>hello</span>\n" +
        "      <div>hello</div>\n" +
        "    </a>\n" +
        "  ");

    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - link \"worldhello hellobye\":\n" +
        "      - /url: about:blank\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-016 should not include hidden pseudo in text")
  void shouldNotIncludeHiddenPseudoInText(Page page) {
    page.setContent("\n" +
        "    <style>\n" +
        "      span:before {\n" +
        "        content: 'world';\n" +
        "        display: none;\n" +
        "      }\n" +
        "      div:after {\n" +
        "        content: 'bye';\n" +
        "        visibility: hidden;\n" +
        "      }\n" +
        "    </style>\n" +
        "    <a href=\"about:blank\">\n" +
        "      <span>hello</span>\n" +
        "      <div>hello</div>\n" +
        "    </a>\n" +
        "  ");

    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - link \"hello hello\":\n" +
        "      - /url: about:blank\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-017 should include new line for block pseudo")
  void shouldIncludeNewLineForBlockPseudo(Page page) {
    page.setContent("\n" +
        "    <style>\n" +
        "      span:before {\n" +
        "        content: 'world';\n" +
        "        display: block;\n" +
        "      }\n" +
        "      div:after {\n" +
        "        content: 'bye';\n" +
        "        display: block;\n" +
        "      }\n" +
        "    </style>\n" +
        "    <a href=\"about:blank\">\n" +
        "      <span>hello</span>\n" +
        "      <div>hello</div>\n" +
        "    </a>\n" +
        "  ");

    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - link \"world hello hello bye\":\n" +
        "      - /url: about:blank\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-018 should work with slots")
  void shouldWorkWithSlots(Page page) {
    // Text "foo" is assigned to the slot, should not be used twice.
    page.setContent("\n" +
        "    <button><div>foo</div></button>\n" +
        "    <script>\n" +
        "      (() => {\n" +
        "        const container = document.querySelector('div');\n" +
        "        const shadow = container.attachShadow({ mode: 'open' });\n" +
        "        const slot = document.createElement('slot');\n" +
        "        shadow.appendChild(slot);\n" +
        "      })();\n" +
        "    </script>\n" +
        "  ");
    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - button \"foo\"\n" +
        "  ");

    // Text "foo" is assigned to the slot, should be used instead of slot content.
    page.setContent("\n" +
        "    <div>foo</div>\n" +
        "    <script>\n" +
        "      (() => {\n" +
        "        const container = document.querySelector('div');\n" +
        "        const shadow = container.attachShadow({ mode: 'open' });\n" +
        "        const button = document.createElement('button');\n" +
        "        shadow.appendChild(button);\n" +
        "        const slot = document.createElement('slot');\n" +
        "        button.appendChild(slot);\n" +
        "        const span = document.createElement('span');\n" +
        "        span.textContent = 'pre';\n" +
        "        slot.appendChild(span);\n" +
        "      })();\n" +
        "    </script>\n" +
        "  ");
    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - button \"foo\"\n" +
        "  ");

    // Nothing is assigned to the slot, should use slot content.
    page.setContent("\n" +
        "    <div></div>\n" +
        "    <script>\n" +
        "      (() => {\n" +
        "        const container = document.querySelector('div');\n" +
        "        const shadow = container.attachShadow({ mode: 'open' });\n" +
        "        const button = document.createElement('button');\n" +
        "        shadow.appendChild(button);\n" +
        "        const slot = document.createElement('slot');\n" +
        "        button.appendChild(slot);\n" +
        "        const span = document.createElement('span');\n" +
        "        span.textContent = 'pre';\n" +
        "        slot.appendChild(span);\n" +
        "      })();\n" +
        "    </script>\n" +
        "  ");
    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - button \"pre\"\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-019 should snapshot inner text")
  void shouldSnapshotInnerText(Page page) {
    page.setContent("\n" +
        "    <div role=\"listitem\">\n" +
        "      <div>\n" +
        "        <div>\n" +
        "          <span title=\"a.test.ts\">a.test.ts</span>\n" +
        "        </div>\n" +
        "        <div>\n" +
        "          <button title=\"Run\"></button>\n" +
        "          <button title=\"Show source\"></button>\n" +
        "          <button title=\"Watch\"></button>\n" +
        "        </div>\n" +
        "      </div>\n" +
        "    </div>\n" +
        "    <div role=\"listitem\">\n" +
        "      <div>\n" +
        "        <div>\n" +
        "          <span title=\"snapshot\">snapshot</span>\n" +
        "        </div>\n" +
        "        <div class=\"ui-mode-list-item-time\">30ms</div>\n" +
        "        <div>\n" +
        "          <button title=\"Run\"></button>\n" +
        "          <button title=\"Show source\"></button>\n" +
        "          <button title=\"Watch\"></button>\n" +
        "        </div>\n" +
        "      </div>\n" +
        "    </div>\n" +
        "  ");

    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - listitem:\n" +
        "      - text: a.test.ts\n" +
        "      - button \"Run\"\n" +
        "      - button \"Show source\"\n" +
        "      - button \"Watch\"\n" +
        "    - listitem:\n" +
        "      - text: snapshot 30ms\n" +
        "      - button \"Run\"\n" +
        "      - button \"Show source\"\n" +
        "      - button \"Watch\"\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-020 should include pseudo codepoints")
  void shouldIncludePseudoCodepoints(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("\n" +
        "    <link href=\"codicon.css\" rel=\"stylesheet\" />\n" +
        "    <p class='codicon codicon-check'>hello</p>\n" +
        "  ");

    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - paragraph: \ueab2hello\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-021 check aria-hidden text")
  void checkAriahiddenText(Page page) {
    page.setContent("\n" +
        "    <p>\n" +
        "      <span>hello</span>\n" +
        "      <span aria-hidden=\"true\">world</span>\n" +
        "    </p>\n" +
        "  ");

    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - paragraph: hello\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-022 should ignore presentation and none roles")
  void shouldIgnorePresentationAndNoneRoles(Page page) {
    page.setContent("\n" +
        "    <ul>\n" +
        "      <li role='presentation'>hello</li>\n" +
        "      <li role='none'>world</li>\n" +
        "    </ul>\n" +
        "  ");

    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - list: hello world\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-023 should treat input value as text in templates, but not for checkbox/radio/file")
  void shouldTreatInputValueAsTextInTemplatesButNotForCheckboxradiofile(Page page) {
    page.setContent("\n" +
        "    <input value='hello world'>\n" +
        "    <input type=file>\n" +
        "    <input type=checkbox checked>\n" +
        "    <input type=radio checked>\n" +
        "  ");

    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - textbox: hello world\n" +
        "    - button \"Choose File\"\n" +
        "    - checkbox [checked]\n" +
        "    - radio [checked]\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-024 should not use on as checkbox value")
  void shouldNotUseOnAsCheckboxValue(Page page) {
    page.setContent("\n" +
        "    <input type='checkbox'>\n" +
        "    <input type='radio'>\n" +
        "  ");

    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - checkbox\n" +
        "    - radio\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-025 should respect aria-owns")
  void shouldRespectAriaowns(Page page) {
    page.setContent("\n" +
        "    <a href='about:blank' aria-owns='input p'>\n" +
        "      <div role='region'>Link 1</div>\n" +
        "    </a>\n" +
        "    <a href='about:blank' aria-owns='input p'>\n" +
        "      <div role='region'>Link 2</div>\n" +
        "    </a>\n" +
        "    <input id='input' value='Value'>\n" +
        "    <p id='p'>Paragraph</p>\n" +
        "  ");

    // - Different from Chrome DevTools which attributes ownership to the last element.
    // - CDT also does not include non-owned children in accessible name.
    // - Disregarding these as aria-owns can't suggest multiple parts by spec.
    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - link \"Link 1 Value Paragraph\":\n" +
        "      - /url: about:blank\n" +
        "      - region: Link 1\n" +
        "      - textbox: Value\n" +
        "      - paragraph: Paragraph\n" +
        "    - link \"Link 2 Value Paragraph\":\n" +
        "      - /url: about:blank\n" +
        "      - region: Link 2\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-026 should be ok with circular ownership")
  void shouldBeOkWithCircularOwnership(Page page) {
    page.setContent("\n" +
        "    <a href='about:blank' id='parent'>\n" +
        "      <div role='region' aria-owns='parent'>Hello</div>\n" +
        "    </a>\n" +
        "  ");

    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - link \"Hello\":\n" +
        "      - /url: about:blank\n" +
        "      - region: Hello\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-027 should escape yaml text in text nodes")
  void shouldEscapeYamlTextInTextNodes(Page page) {
    page.setContent("\n" +
        "    <details>\n" +
        "      <summary>one: <a href=\"#\">link1</a> \"two <a href=\"#\">link2</a> 'three <a href=\"#\">link3</a> `four</summary>\n" +
        "    </details>\n" +
        "    <ul>\n" +
        "      <a href=\"#\">one</a>,<a href=\"#\">two</a>\n" +
        "      (<a href=\"#\">three</a>)\n" +
        "      {<a href=\"#\">four</a>}\n" +
        "      [<a href=\"#\">five</a>]\n" +
        "    </ul>\n" +
        "    <div>[Select all]</div>\n" +
        "  ");

    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - group:\n" +
        "      - text: \"one:\"\n" +
        "      - link \"link1\":\n" +
        "        - /url: \"#\"\n" +
        "      - text: \"\\\"two\"\n" +
        "      - link \"link2\":\n" +
        "        - /url: \"#\"\n" +
        "      - text: \"'three\"\n" +
        "      - link \"link3\":\n" +
        "        - /url: \"#\"\n" +
        "      - text: \"`four\"\n" +
        "    - list:\n" +
        "      - link \"one\":\n" +
        "        - /url: \"#\"\n" +
        "      - text: \",\"\n" +
        "      - link \"two\":\n" +
        "        - /url: \"#\"\n" +
        "      - text: (\n" +
        "      - link \"three\":\n" +
        "        - /url: \"#\"\n" +
        "      - text: \") {\"\n" +
        "      - link \"four\":\n" +
        "        - /url: \"#\"\n" +
        "      - text: \"} [\"\n" +
        "      - link \"five\":\n" +
        "        - /url: \"#\"\n" +
        "      - text: \"]\"\n" +
        "    - text: \"[Select all]\"\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-028 should normalize whitespace")
  void shouldNormalizeWhitespace(Page page) {
    page.setContent("\n" +
        "    <details>\n" +
        "      <summary> one  \n two <a href=\"#\"> link &nbsp;\n  1 </a> </summary>\n" +
        "    </details>\n" +
        "    <input value='  hello   &nbsp; world '>\n" +
        "    <button>hello\u00ad\u200bworld</button>\n" +
        "  ");

    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - group:\n" +
        "      - text: one two\n" +
        "      - link \"link 1\":\n" +
        "        - /url: \"#\"\n" +
        "    - textbox: hello world\n" +
        "    - button \"helloworld\"\n" +
        "  ");

    // Weird whitespace in the template should be normalized.
    assertThat(page.locator("body")).matchesAriaSnapshot("\n" +
        "    - group:\n" +
        "      - text: |\n" +
        "          one\n" +
        "          two\n" +
        "      - link \"  link     1 \":\n" +
        "        - /url: \"#\"\n" +
        "    - textbox:        hello  world\n" +
        "    - button \"he\u00adlloworld\u200b\"\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-029 should handle long strings")
  void shouldHandleLongStrings(Page page) {
    String s = String.join("", java.util.Collections.nCopies(10000, "a"));
    page.setContent("\n" +
        "    <a href='about:blank'>\n" +
        "      <div role='region'>" + s + "</div>\n" +
        "    </a>\n" +
        "  ");

    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - link:\n" +
        "      - /url: about:blank\n" +
        "      - region: " + s + "\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-030 should escape special yaml characters")
  void shouldEscapeSpecialYamlCharacters(Page page) {
    page.setContent("\n" +
        "    <a href=\"#\">@hello</a>@hello\n" +
        "    <a href=\"#\">]hello</a>]hello\n" +
        "    <a href=\"#\">hello\n</a>\n" +
        "    hello\n<a href=\"#\">\n hello</a>\n hello\n" +
        "    <a href=\"#\">#hello</a>#hello\n" +
        "  ");

    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - link \"@hello\":\n" +
        "      - /url: \"#\"\n" +
        "    - text: \"@hello\"\n" +
        "    - link \"]hello\":\n" +
        "      - /url: \"#\"\n" +
        "    - text: \"]hello\"\n" +
        "    - link \"hello\":\n" +
        "      - /url: \"#\"\n" +
        "    - text: hello\n" +
        "    - link \"hello\":\n" +
        "      - /url: \"#\"\n" +
        "    - text: hello\n" +
        "    - link \"#hello\":\n" +
        "      - /url: \"#\"\n" +
        "    - text: \"#hello\"\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-031 should escape special yaml values")
  @Fixme("driver: the bundled driver does not quote the special yaml values ~ and .NaN, it prints - text: ~ and - text: .NaN")
  void shouldEscapeSpecialYamlValues(Page page) {
    page.setContent("\n" +
        "    <a href=\"#\">true</a>False\n" +
        "    <a href=\"#\">NO</a>yes\n" +
        "    <a href=\"#\">y</a>N\n" +
        "    <a href=\"#\">on</a>Off\n" +
        "    <a href=\"#\">null</a>NULL\n" +
        "    <a href=\"#\">123</a>123\n" +
        "    <a href=\"#\">-1.2</a>-1.2\n" +
        "    <a href=\"#\">-</a>-\n" +
        "    <a href=\"#\">~</a>~\n" +
        "    <a href=\"#\">.inf</a>.NaN\n" +
        "    <input type=text value=\"555\">\n" +
        "  ");

    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - link \"true\":\n" +
        "      - /url: \"#\"\n" +
        "    - text: \"False\"\n" +
        "    - link \"NO\":\n" +
        "      - /url: \"#\"\n" +
        "    - text: \"yes\"\n" +
        "    - link \"y\":\n" +
        "      - /url: \"#\"\n" +
        "    - text: \"N\"\n" +
        "    - link \"on\":\n" +
        "      - /url: \"#\"\n" +
        "    - text: \"Off\"\n" +
        "    - link \"null\":\n" +
        "      - /url: \"#\"\n" +
        "    - text: \"NULL\"\n" +
        "    - link \"123\":\n" +
        "      - /url: \"#\"\n" +
        "    - text: \"123\"\n" +
        "    - link \"-1.2\":\n" +
        "      - /url: \"#\"\n" +
        "    - text: \"-1.2\"\n" +
        "    - link \"-\":\n" +
        "      - /url: \"#\"\n" +
        "    - text: \"-\"\n" +
        "    - link \"~\":\n" +
        "      - /url: \"#\"\n" +
        "    - text: \"~\"\n" +
        "    - link \".inf\":\n" +
        "      - /url: \"#\"\n" +
        "    - text: \".NaN\"\n" +
        "    - textbox: \"555\"\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-032 should not report textarea textContent")
  void shouldNotReportTextareaTextContent(Page page) {
    page.setContent("<textarea>Before</textarea>");
    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - textbox: Before\n" +
        "  ");
    page.evaluate("() => {\n" +
        "  document.querySelector('textarea').value = 'After';\n" +
        "}");
    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - textbox: After\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-033 should not show visible children of hidden elements")
  void shouldNotShowVisibleChildrenOfHiddenElements(Page page) {
    page.setContent("\n" +
        "    <div style=\"visibility: hidden;\">\n" +
        "      <div style=\"visibility: visible;\">\n" +
        "        <button>Button</button>\n" +
        "      </div>\n" +
        "    </div>\n" +
        "  ");

    assertEquals("", page.locator("body").ariaSnapshot());
  }

  @PlaywrightTest
  @DisplayName("AR-034 should not show unhidden children of aria-hidden elements")
  void shouldNotShowUnhiddenChildrenOfAriahiddenElements(Page page) {
    page.setContent("\n" +
        "    <div aria-hidden=\"true\">\n" +
        "      <div aria-hidden=\"false\">\n" +
        "        <button>Button</button>\n" +
        "      </div>\n" +
        "    </div>\n" +
        "  ");

    assertEquals("", page.locator("body").ariaSnapshot());
  }

  @PlaywrightTest
  @DisplayName("AR-035 should snapshot placeholder when different from the name")
  void shouldSnapshotPlaceholderWhenDifferentFromTheName(Page page) {
    page.setContent("\n" +
        "    <input placeholder=\"Placeholder\">\n" +
        "  ");
    expectToContainYaml(page.locator("body").ariaSnapshot(), "\n" +
        "    - textbox \"Placeholder\"\n" +
        "  ");

    page.setContent("\n" +
        "    <input placeholder=\"Placeholder\" aria-label=\"Label\">\n" +
        "  ");
    expectToContainYaml(page.locator("body").ariaSnapshot(), "\n" +
        "    - textbox \"Label\":\n" +
        "      - /placeholder: Placeholder\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-036 match values both against regex and string")
  void matchValuesBothAgainstRegexAndString(Page page) {
    page.setContent("<a href=\"/auth?r=/\">Log in</a>");
    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - link \"Log in\":\n" +
        "      - /url: /auth?r=/\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-038 should limit depth")
  void shouldLimitDepth(Page page) {
    page.setContent("\n" +
        "    <ul id=target>\n" +
        "      <li>item2</li>\n" +
        "      <li>\n" +
        "        <ul>\n" +
        "          <li>item3</li>\n" +
        "        </ul>\n" +
        "      </li>\n" +
        "    </ul>\n" +
        "  ");

    String snapshot = page.locator("#target").ariaSnapshot(new Locator.AriaSnapshotOptions().setDepth(1));
    expectToContainYaml(snapshot, "\n" +
        "    - list:\n" +
        "      - listitem: item2\n" +
        "      - listitem\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-039 should snapshot a locator inside an iframe")
  void shouldSnapshotALocatorInsideAnIframe(Page page) {
    page.setContent("\n" +
        "    <h1>Main Page</h1>\n" +
        "    <iframe srcdoc=\"<ul><li>Item 1</li><li>Item 2</li></ul>\"></iframe>\n" +
        "  ");

    Locator list = page.frames().get(1).locator("ul");
    String snapshot = list.ariaSnapshot();
    expectToContainYaml(snapshot, "\n" +
        "    - list:\n" +
        "      - listitem: Item 1\n" +
        "      - listitem: Item 2\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-040 should include frames on frameset pages")
  void shouldIncludeFramesOnFramesetPages(Page page, TestServer server) {
    server.setRoute("/frameset.html", (req, res) -> {
      res.setHeader("Content-Type", "text/html");
      res.end("<frameset cols=\"50%,50%\"><frame src=\"/frames/frame.html\"><frame src=\"/frames/frame.html\"></frameset>");
    });
    page.navigate(server.PREFIX + "/frameset.html");
    // Like iframes, frames are listed in the snapshot, but their content is not included.
    assertEquals(unshift("\n" +
        "    - iframe\n" +
        "    - iframe\n" +
        "  "), page.ariaSnapshot(new Page.AriaSnapshotOptions().setTimeout(3000)));
  }

  @PlaywrightTest
  @DisplayName("AR-041 should snapshot a locator inside a frameset frame")
  void shouldSnapshotALocatorInsideAFramesetFrame(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/frames/frameset.html");
    checkAndMatchSnapshot(page.frameLocator("frame").first().locator("body"), "\n" +
        "    - text: Hi, I'm frame\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-042 should snapshot with box from page")
  void shouldSnapshotWithBoxFromPage(Page page) {
    page.setContent("\n" +
        "    <button style=\"position:absolute;left:100px;top:50px;width:80px;height:40px;margin:0;padding:0;border:0;\">click</button>\n" +
        "  ");

    String snapshot = page.ariaSnapshot(new Page.AriaSnapshotOptions().setBoxes(true));
    assertEquals("- button \"click\" [box=100,50,80,40]", snapshot);
  }

  @PlaywrightTest
  @DisplayName("AR-043 should snapshot with box from locator")
  void shouldSnapshotWithBoxFromLocator(Page page) {
    page.setContent("\n" +
        "    <div style=\"position:absolute;left:10px;top:20px;width:200px;height:100px;\">\n" +
        "      <button style=\"position:absolute;left:5px;top:5px;width:60px;height:30px;margin:0;padding:0;border:0;\">ok</button>\n" +
        "    </div>\n" +
        "  ");

    String snapshot = page.locator("div").ariaSnapshot(new Locator.AriaSnapshotOptions().setBoxes(true));
    assertEquals("- button \"ok\" [box=15,25,60,30]", snapshot);
  }

  @PlaywrightTest
  @DisplayName("AR-044 should not include box when option is omitted")
  void shouldNotIncludeBoxWhenOptionIsOmitted(Page page) {
    page.setContent("<button>click</button>");
    String snapshot = page.ariaSnapshot();
    assertFalse(Pattern.compile("\\[box=").matcher(snapshot).find());
  }

  @PlaywrightTest
  @DisplayName("AR-045 should not include hidden iframes")
  void shouldNotIncludeHiddenIframes(Page page) {
    page.setContent("\n" +
        "    <iframe name=\"visible\" srcdoc=\"<button>Visible</button>\"></iframe>\n" +
        "    <iframe name=\"hidden\" style=\"visibility: hidden\" srcdoc=\"<button>Hidden</button>\"></iframe>\n" +
        "    <iframe name=\"none\" style=\"display: none\" srcdoc=\"<button>None</button>\"></iframe>\n" +
        "  ");
    assertEquals(unshift("\n" +
        "    - iframe\n" +
        "  "), page.ariaSnapshot());
  }

  @PlaywrightTest
  @DisplayName("AR-046 should not include inert elements")
  @Fixme("driver: the bundled driver does not skip inert elements, snapshot has - button \"Hello\", - button \"Yay\", - paragraph: Some text, - paragraph: Visible inert text, - button \"Save\"")
  void shouldNotIncludeInertElements(Page page) {
    page.setContent("\n" +
        "    <button>Hi</button>\n" +
        "    <button inert>Hello</button>\n" +
        "    <div inert>\n" +
        "      <button>Yay</button>\n" +
        "      <p>Some text</p>\n" +
        "    </div>\n" +
        "    <p>Visible <span inert>inert</span> text</p>\n" +
        "    <button><span inert>Save</span></button>\n" +
        "  ");

    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - button \"Hi\"\n" +
        "    - paragraph: Visible text\n" +
        "    - button\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-047 should only include the modal dialog content")
  @Fixme("driver: the bundled driver does not limit the snapshot to the modal dialog, it also has - heading \"Title\", - button \"Outside\" and - button \"Inert inside\"")
  void shouldOnlyIncludeTheModalDialogContent(Page page) {
    page.setContent("\n" +
        "    <h1>Title</h1>\n" +
        "    <button>Outside</button>\n" +
        "    <dialog id=dialog>\n" +
        "      <h2>Dialog</h2>\n" +
        "      <button>Inside</button>\n" +
        "      <div inert><button>Inert inside</button></div>\n" +
        "    </dialog>\n" +
        "    <script>\n" +
        "      document.getElementById('dialog').showModal();\n" +
        "    </script>\n" +
        "  ");

    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - dialog:\n" +
        "      - heading \"Dialog\" [level=2]\n" +
        "      - button \"Inside\"\n" +
        "  ");

    page.locator("#dialog").evaluate("e => e.close()");
    checkAndMatchSnapshot(page.locator("body"), "\n" +
        "    - heading \"Title\" [level=1]\n" +
        "    - button \"Outside\"\n" +
        "  ");
  }
}
