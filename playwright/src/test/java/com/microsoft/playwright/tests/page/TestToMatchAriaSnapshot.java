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

import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.assertions.PageAssertions;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.DisplayName;
import org.opentest4j.AssertionFailedError;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.stripAnsi;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/to-match-aria-snapshot.spec.ts. */
public class TestToMatchAriaSnapshot {
  @PlaywrightTest
  @DisplayName("AR-107 should match page")
  void shouldMatchPage(Page page) {
    page.setContent("<h1>title</h1>");
    assertThat(page).matchesAriaSnapshot("\n" +
        "    - heading \"title\"\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-108 should match page complex")
  void shouldMatchPageComplex(Page page) {
    page.setContent("\n" +
        "    <h1>Microsoft</h1>\n" +
        "    <div>Open source projects and samples from Microsoft</div>\n" +
        "    <ul>\n" +
        "      <li>\n" +
        "        <a href=\"about:blank\">Playwright</a>\n" +
        "      </li>\n" +
        "    </ul>");

    assertThat(page).matchesAriaSnapshot("\n" +
        "    - heading \"Microsoft\"\n" +
        "    - text: Open source projects and samples from Microsoft\n" +
        "    - list:\n" +
        "      - listitem:\n" +
        "        - link \"Playwright\"\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-109 should match page with not")
  void shouldMatchPageWithNot(Page page) {
    page.setContent("<h1>title</h1>");
    assertThat(page).not().matchesAriaSnapshot("\n" +
        "    - heading \"wrong\"\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-110 should fail page with timeout")
  @Fixme("java: the assertion error message starts with \"Page expected to match Aria snapshot\" (for a locator \"Locator expected to match Aria snapshot\"), not \"expect(page).toMatchAriaSnapshot(expected) failed\" and has no \"Timeout:  Nms\" line")
  void shouldFailPageWithTimeout(Page page) {
    page.setContent("<h1>title</h1>");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
        "    - heading \"wrong\"\n" +
        "  ", new PageAssertions.MatchesAriaSnapshotOptions().setTimeout(2000)));
    assertTrue(stripAnsi(e.getMessage()).contains("expect(page).toMatchAriaSnapshot(expected) failed"));
    assertTrue(stripAnsi(e.getMessage()).contains("Timeout:  2000ms"));
    assertFalse(stripAnsi(e.getMessage()).contains("Locator"));
  }

  @PlaywrightTest
  @DisplayName("AR-111 should match")
  void shouldMatch(Page page) {
    page.setContent("<h1>title</h1>");
    assertThat(page).matchesAriaSnapshot("\n" +
        "    - heading \"title\"\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-112 should match in list")
  void shouldMatchInList(Page page) {
    page.setContent("\n" +
        "    <h1>title</h1>\n" +
        "    <h1>title 2</h1>\n" +
        "  ");
    assertThat(page).matchesAriaSnapshot("\n" +
        "    - heading \"title\"\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-113 should match list with accessible name")
  void shouldMatchListWithAccessibleName(Page page) {
    page.setContent("\n" +
        "    <ul aria-label=\"my list\">\n" +
        "      <li>one</li>\n" +
        "      <li>two</li>\n" +
        "    </ul>\n" +
        "  ");
    assertThat(page).matchesAriaSnapshot("\n" +
        "    - list \"my list\":\n" +
        "      - listitem: \"one\"\n" +
        "      - listitem: \"two\"\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-114 should match deep item")
  void shouldMatchDeepItem(Page page) {
    page.setContent("\n" +
        "    <div>\n" +
        "      <h1>title</h1>\n" +
        "      <h1>title 2</h1>\n" +
        "    </div>\n" +
        "  ");
    assertThat(page).matchesAriaSnapshot("\n" +
        "    - heading \"title\"\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-115 should match complex")
  void shouldMatchComplex(Page page) {
    page.setContent("\n" +
        "    <ul>\n" +
        "      <li>\n" +
        "        <a href='about:blank'>link</a>\n" +
        "      </li>\n" +
        "    </ul>\n" +
        "  ");
    assertThat(page).matchesAriaSnapshot("\n" +
        "    - list:\n" +
        "      - listitem:\n" +
        "        - link \"link\"\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-116 should match regex")
  void shouldMatchRegex(Page page) {
    {
      page.setContent("<h1>Issues 12</h1>");
      assertThat(page).matchesAriaSnapshot("\n" +
          // Java: a RegExp interpolated into a template is its source between slashes.
          "      - heading /Issues \\d+/\n" +
          "    ");
    }
    {
      page.setContent("<h1>Issues 1/2</h1>");
      assertThat(page).matchesAriaSnapshot("\n" +
          // Java: a RegExp interpolated into a template is its source between slashes.
          "      - heading /Issues 1[/]2/\n" +
          "    ");
    }
    {
      page.setContent("<h1>Issues 1[</h1>");
      assertThat(page).matchesAriaSnapshot("\n" +
          // Java: a RegExp interpolated into a template is its source between slashes.
          "      - heading /Issues 1\\[/\n" +
          "    ");
    }
    {
      page.setContent("<h1>Issues 1]]2</h1>");
      assertThat(page).matchesAriaSnapshot("\n" +
          // Java: a RegExp interpolated into a template is its source between slashes.
          "      - heading /Issues 1[\\]]]2/\n" +
          "    ");
    }
  }

  @PlaywrightTest
  @DisplayName("AR-117 should allow text nodes")
  void shouldAllowTextNodes(Page page) {
    page.setContent("\n" +
        "    <h1>Microsoft</h1>\n" +
        "    <div>Open source projects and samples from Microsoft</div>\n" +
        "  ");

    assertThat(page).matchesAriaSnapshot("\n" +
        "    - heading \"Microsoft\"\n" +
        "    - text: \"Open source projects and samples from Microsoft\"\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-118 details visibility")
  void detailsVisibility(Page page) {
    page.setContent("\n" +
        "    <details>\n" +
        "      <summary>Summary</summary>\n" +
        "      <div>Details</div>\n" +
        "    </details>\n" +
        "  ");

    assertThat(page).matchesAriaSnapshot("\n" +
        "    - group: \"Summary\"\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-119 checked attribute")
  @Fixme("java: the assertion error message starts with \"Page expected to match Aria snapshot\" (for a locator \"Locator expected to match Aria snapshot\"), not \"expect(page).toMatchAriaSnapshot(expected) failed\" and has no \"Timeout:  Nms\" line")
  void checkedAttribute(Page page) {
    page.setContent("\n" +
        "    <input type='checkbox' checked />\n" +
        "  ");

    assertThat(page).matchesAriaSnapshot("\n" +
        "    - checkbox\n" +
        "  ");

    assertThat(page).matchesAriaSnapshot("\n" +
        "    - checkbox [checked]\n" +
        "  ");

    assertThat(page).matchesAriaSnapshot("\n" +
        "    - checkbox [checked=true]\n" +
        "  ");

    {
      AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
          "      - checkbox [checked=false]\n" +
          "    ", new PageAssertions.MatchesAriaSnapshotOptions().setTimeout(1000)));
      assertTrue(stripAnsi(e.getMessage()).contains("expect(page).toMatchAriaSnapshot(expected) failed"));
      assertTrue(stripAnsi(e.getMessage()).contains("Timeout:  1000ms"));
    }

    {
      AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
          "      - checkbox [checked=mixed]\n" +
          "    ", new PageAssertions.MatchesAriaSnapshotOptions().setTimeout(1000)));
      assertTrue(stripAnsi(e.getMessage()).contains("expect(page).toMatchAriaSnapshot(expected) failed"));
      assertTrue(stripAnsi(e.getMessage()).contains("Timeout:  1000ms"));
    }

    {
      AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
          "      - checkbox [checked=5]\n" +
          "    ", new PageAssertions.MatchesAriaSnapshotOptions().setTimeout(1000)));
      assertTrue(stripAnsi(e.getMessage()).contains(" attribute must be a boolean or \"mixed\""));
    }
  }

  @PlaywrightTest
  @DisplayName("AR-120 disabled attribute")
  @Fixme("java: the assertion error message starts with \"Page expected to match Aria snapshot\" (for a locator \"Locator expected to match Aria snapshot\"), not \"expect(page).toMatchAriaSnapshot(expected) failed\" and has no \"Timeout:  Nms\" line")
  void disabledAttribute(Page page) {
    page.setContent("\n" +
        "    <button disabled>Click me</button>\n" +
        "  ");

    assertThat(page).matchesAriaSnapshot("\n" +
        "    - button\n" +
        "  ");

    assertThat(page).matchesAriaSnapshot("\n" +
        "    - button [disabled]\n" +
        "  ");

    assertThat(page).matchesAriaSnapshot("\n" +
        "    - button [disabled=true]\n" +
        "  ");

    {
      AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
          "      - button [disabled=false]\n" +
          "    ", new PageAssertions.MatchesAriaSnapshotOptions().setTimeout(1000)));
      assertTrue(stripAnsi(e.getMessage()).contains("expect(page).toMatchAriaSnapshot(expected) failed"));
      assertTrue(stripAnsi(e.getMessage()).contains("Timeout:  1000ms"));
    }

    {
      AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
          "      - button [disabled=invalid]\n" +
          "    ", new PageAssertions.MatchesAriaSnapshotOptions().setTimeout(1000)));
      assertTrue(stripAnsi(e.getMessage()).contains(" attribute must be a boolean"));
    }
  }

  @PlaywrightTest
  @DisplayName("AR-121 expanded attribute")
  @Fixme("java: the assertion error message starts with \"Page expected to match Aria snapshot\" (for a locator \"Locator expected to match Aria snapshot\"), not \"expect(page).toMatchAriaSnapshot(expected) failed\" and has no \"Timeout:  Nms\" line")
  void expandedAttribute(Page page) {
    page.setContent("\n" +
        "    <button aria-expanded=\"true\">Toggle</button>\n" +
        "  ");

    assertThat(page).matchesAriaSnapshot("\n" +
        "    - button\n" +
        "  ");

    assertThat(page).matchesAriaSnapshot("\n" +
        "    - button [expanded]\n" +
        "  ");

    assertThat(page).matchesAriaSnapshot("\n" +
        "    - button [expanded=true]\n" +
        "  ");

    {
      AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
          "      - button [expanded=false]\n" +
          "    ", new PageAssertions.MatchesAriaSnapshotOptions().setTimeout(1000)));
      assertTrue(stripAnsi(e.getMessage()).contains("expect(page).toMatchAriaSnapshot(expected) failed"));
      assertTrue(stripAnsi(e.getMessage()).contains("Timeout:  1000ms"));
    }

    {
      AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
          "      - button [expanded=invalid]\n" +
          "    ", new PageAssertions.MatchesAriaSnapshotOptions().setTimeout(1000)));
      assertTrue(stripAnsi(e.getMessage()).contains(" attribute must be a boolean"));
    }
  }

  @PlaywrightTest
  @DisplayName("AR-122 level attribute")
  @Fixme("java: the assertion error message starts with \"Page expected to match Aria snapshot\" (for a locator \"Locator expected to match Aria snapshot\"), not \"expect(page).toMatchAriaSnapshot(expected) failed\" and has no \"Timeout:  Nms\" line")
  void levelAttribute(Page page) {
    page.setContent("\n" +
        "    <h2>Section Title</h2>\n" +
        "  ");

    assertThat(page).matchesAriaSnapshot("\n" +
        "    - heading\n" +
        "  ");

    assertThat(page).matchesAriaSnapshot("\n" +
        "    - heading [level=2]\n" +
        "  ");

    {
      AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
          "      - heading [level=3]\n" +
          "    ", new PageAssertions.MatchesAriaSnapshotOptions().setTimeout(1000)));
      assertTrue(stripAnsi(e.getMessage()).contains("expect(page).toMatchAriaSnapshot(expected) failed"));
      assertTrue(stripAnsi(e.getMessage()).contains("Timeout:  1000ms"));
    }

    {
      AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
          "      - heading [level=two]\n" +
          "    ", new PageAssertions.MatchesAriaSnapshotOptions().setTimeout(1000)));
      assertTrue(stripAnsi(e.getMessage()).contains(" attribute must be a number"));
    }
  }

  @PlaywrightTest
  @DisplayName("AR-123 pressed attribute")
  @Fixme("java: the assertion error message starts with \"Page expected to match Aria snapshot\" (for a locator \"Locator expected to match Aria snapshot\"), not \"expect(page).toMatchAriaSnapshot(expected) failed\" and has no \"Timeout:  Nms\" line")
  void pressedAttribute(Page page) {
    page.setContent("\n" +
        "    <button aria-pressed=\"true\">Like</button>\n" +
        "  ");

    assertThat(page).matchesAriaSnapshot("\n" +
        "    - button\n" +
        "  ");

    assertThat(page).matchesAriaSnapshot("\n" +
        "    - button [pressed]\n" +
        "  ");

    assertThat(page).matchesAriaSnapshot("\n" +
        "    - button [pressed=true]\n" +
        "  ");

    {
      AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
          "      - button [pressed=false]\n" +
          "    ", new PageAssertions.MatchesAriaSnapshotOptions().setTimeout(1000)));
      assertTrue(stripAnsi(e.getMessage()).contains("expect(page).toMatchAriaSnapshot(expected) failed"));
      assertTrue(stripAnsi(e.getMessage()).contains("Timeout:  1000ms"));
    }

    // Test for 'mixed' state
    page.setContent("\n" +
        "    <button aria-pressed=\"mixed\">Like</button>\n" +
        "  ");

    assertThat(page).matchesAriaSnapshot("\n" +
        "    - button [pressed=mixed]\n" +
        "  ");

    {
      AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
          "      - button [pressed=true]\n" +
          "    ", new PageAssertions.MatchesAriaSnapshotOptions().setTimeout(1000)));
      assertTrue(stripAnsi(e.getMessage()).contains("expect(page).toMatchAriaSnapshot(expected) failed"));
      assertTrue(stripAnsi(e.getMessage()).contains("Timeout:  1000ms"));
    }

    {
      AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
          "      - button [pressed=5]\n" +
          "    ", new PageAssertions.MatchesAriaSnapshotOptions().setTimeout(1000)));
      assertTrue(stripAnsi(e.getMessage()).contains(" attribute must be a boolean or \"mixed\""));
    }
  }

  @PlaywrightTest
  @DisplayName("AR-124 selected attribute")
  @Fixme("java: the assertion error message starts with \"Page expected to match Aria snapshot\" (for a locator \"Locator expected to match Aria snapshot\"), not \"expect(page).toMatchAriaSnapshot(expected) failed\" and has no \"- Expected  - N / + Received  + M\" diff, only the Expected: and Received: dumps")
  void selectedAttribute(Page page) {
    page.setContent("\n" +
        "    <table>\n" +
        "      <tr aria-selected=\"true\">\n" +
        "        <td>Row</td>\n" +
        "      </tr>\n" +
        "      <tr>\n" +
        "        <td>Row 2</td>\n" +
        "      </tr>\n" +
        "    </table>\n" +
        "  ");

    assertThat(page).matchesAriaSnapshot("\n" +
        "    - row\n" +
        "    - row\n" +
        "  ");

    assertThat(page).matchesAriaSnapshot("\n" +
        "    - row [selected]\n" +
        "    - row\n" +
        "  ");

    assertThat(page).matchesAriaSnapshot("\n" +
        "    - row [selected=true]\n" +
        "    - row [selected=false]\n" +
        "  ");

    assertThat(page.locator("table")).matchesAriaSnapshot("\n" +
        "    - table:\n" +
        "      - rowgroup:\n" +
        "        - row [selected=true]\n" +
        "        - row [selected=false]\n" +
        "  ");

    {
      AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
          "      - row [selected=false]\n" +
          "      - row [selected=false]\n" +
          "    ", new PageAssertions.MatchesAriaSnapshotOptions().setTimeout(1000)));
      assertTrue(stripAnsi(e.getMessage()).contains("expect(page).toMatchAriaSnapshot(expected) failed\n" +
          "\n" +
          "Timeout: 1000ms\n" +
          "- Expected  - 2\n" +
          "+ Received  + 6\n" +
          "\n" +
          "- - row [selected=false]\n" +
          "- - row [selected=false]\n" +
          "+ - table:\n" +
          "+   - rowgroup:\n" +
          "+     - row \"Row\" [selected]:\n" +
          "+       - cell \"Row\"\n" +
          "+     - row \"Row 2\":\n" +
          "+       - cell \"Row 2\"\n" +
          "\n" +
          "Call log:\n"));
    }

    {
      AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
          "      - row [selected=invalid]\n" +
          "    ", new PageAssertions.MatchesAriaSnapshotOptions().setTimeout(1000)));
      assertTrue(stripAnsi(e.getMessage()).contains(" attribute must be a boolean"));
    }
  }

  @PlaywrightTest
  @DisplayName("AR-125 integration test")
  void integrationTest(Page page) {
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

    assertThat(page).matchesAriaSnapshot("\n" +
        "    - heading \"Microsoft\"\n" +
        "    - text: Open source projects and samples from Microsoft\n" +
        "    - list:\n" +
        "      - listitem:\n" +
        "        - group: Verified\n" +
        "      - listitem:\n" +
        "        - link \"Sponsor\"\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-126 integration test 2")
  void integrationTest2(Page page) {
    page.setContent("\n" +
        "    <div>\n" +
        "      <header>\n" +
        "        <h1>todos</h1>\n" +
        "        <input placeholder=\"What needs to be done?\">\n" +
        "      </header>\n" +
        "    </div>");
    assertThat(page).matchesAriaSnapshot("\n" +
        "    - heading \"todos\"\n" +
        "    - textbox \"What needs to be done?\"\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-127 expected formatter")
  @Fixme("java: the assertion error message starts with \"Page expected to match Aria snapshot\" (for a locator \"Locator expected to match Aria snapshot\"), not \"expect(page).toMatchAriaSnapshot(expected) failed\" and has no \"- Expected  - N / + Received  + M\" diff, only the Expected: and Received: dumps")
  void expectedFormatter(Page page) {
    page.setContent("\n" +
        "    <div>\n" +
        "      <header>\n" +
        "        <h1>todos</h1>\n" +
        "        <input placeholder=\"What needs to be done?\">\n" +
        "        <button>Time 15:30</button>\n" +
        "      </header>\n" +
        "    </div>");
    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
        "    - heading \"todos\"\n" +
        "    - textbox \"Wrong text\"\n" +
        "  ", new PageAssertions.MatchesAriaSnapshotOptions().setTimeout(1)));

    // Note that error message should not contain any regular expressions,
    // unlike the baseline generated by --update-snapshots.
    assertTrue(stripAnsi(error.getMessage()).contains("expect(page).toMatchAriaSnapshot(expected) failed\n" +
        "\n" +
        "Timeout: 1ms\n" +
        "- Expected  - 2\n" +
        "+ Received  + 4\n" +
        "\n" +
        "- - heading \"todos\"\n" +
        "- - textbox \"Wrong text\"\n" +
        "+ - banner:\n" +
        "+   - heading \"todos\" [level=1]\n" +
        "+   - textbox \"What needs to be done?\"\n" +
        "+   - button \"Time 15:30\"\n" +
        "\n" +
        "Call log:\n"));
  }

  @PlaywrightTest
  @DisplayName("AR-128 should unpack escaped names")
  void shouldUnpackEscapedNames(Page page) {
    {
      page.setContent("\n" +
          "      <button>Click: me</button>\n" +
          "    ");
      assertThat(page).matchesAriaSnapshot("\n" +
          "      - 'button \"Click: me\"'\n" +
          "    ");
      assertThat(page).matchesAriaSnapshot("\n" +
          "      - 'button /Click: me/'\n" +
          "    ");
    }

    {
      page.setContent("\n" +
          "      <button>Click / me</button>\n" +
          "    ");
      assertThat(page).matchesAriaSnapshot("\n" +
          "      - button \"Click / me\"\n" +
          "    ");
      assertThat(page).matchesAriaSnapshot("\n" +
          "      - button /Click \\/ me/\n" +
          "    ");
      assertThat(page).matchesAriaSnapshot("\n" +
          "      - 'button /Click \\/ me/'\n" +
          "    ");
    }

    {
      page.setContent("\n" +
          "      <button>Click \" me</button>\n" +
          "    ");
      assertThat(page).matchesAriaSnapshot("\n" +
          "      - button \"Click \\\" me\"\n" +
          "    ");
      assertThat(page).matchesAriaSnapshot("\n" +
          "      - button /Click \" me/\n" +
          "    ");
      assertThat(page).matchesAriaSnapshot("\n" +
          "      - button /Click \\\" me/\n" +
          "    ");
    }

    {
      page.setContent("\n" +
          "      <button>Click \\ me</button>\n" +
          "    ");
      assertThat(page).matchesAriaSnapshot("\n" +
          "      - button \"Click \\\\ me\"\n" +
          "    ");
      assertThat(page).matchesAriaSnapshot("\n" +
          "      - button /Click \\\\ me/\n" +
          "    ");
      assertThat(page).matchesAriaSnapshot("\n" +
          "      - 'button /Click \\\\ me/'\n" +
          "    ");
    }

    {
      page.setContent("\n" +
          "      <button>Click ' me</button>\n" +
          "    ");
      assertThat(page).matchesAriaSnapshot("\n" +
          "      - 'button \"Click '' me\"'\n" +
          "    ");
    }

    {
      page.setContent("\n" +
          "      <h1>heading \"name\" [level=1]</h1>\n" +
          "    ");
      assertThat(page).matchesAriaSnapshot("\n" +
          "      - heading \"heading \\\"name\\\" [level=1]\" [level=1]\n" +
          "    ");
    }

    {
      page.setContent("\n" +
          "      <h1>heading \\\" [level=2]</h1>\n" +
          "    ");
      assertThat(page).matchesAriaSnapshot("\n" +
          "      - |\n" +
          "          heading    \"heading \\\\\\\" [level=2]\" [\n" +
          "             level  =   1   ]\n" +
          "    ");
    }
  }

  @PlaywrightTest
  @DisplayName("AR-129 should report error in YAML")
  @Fixme("java: the error message is \"Page expected to match Aria snapshot\\nError: Aria snapshot must be a YAML sequence, elements starting with \\\" -\\\"\\nExpected: ...\" with the call log \"Assert \\\"matchesAriaSnapshot\\\" with timeout 5000ms\", not \"expect(page).toMatchAriaSnapshot(expected) failed\\n\\nExpected: ...\" with the call log \"Expect \\\"toMatchAriaSnapshot\\\" with timeout 10000ms\"")
  void shouldReportErrorInYAML(Page page) {
    page.setContent("<h1>title</h1>");

    {
      AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
          "      heading \"title\"\n" +
          "    "));
      assertEquals("expect(page).toMatchAriaSnapshot(expected) failed\n" +
          "\n" +
          "Expected: \"heading \\\"title\\\"\"\n" +
          "Error: Aria snapshot must be a YAML sequence, elements starting with \" -\"\n" +
          "\n" +
          "Call log:\n" +
          "  - Expect \"toMatchAriaSnapshot\" with timeout 10000ms\n", stripAnsi(error.getMessage()));
    }

    {
      AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
          "      - heading: a:\n" +
          "    "));
      assertEquals("expect(page).toMatchAriaSnapshot(expected) failed\n" +
          "\n" +
          "Expected: \"- heading: a:\"\n" +
          "Error: Nested mappings are not allowed in compact mappings at line 1, column 12:\n" +
          "\n" +
          "- heading: a:\n" +
          "           ^\n" +
          "\n" +
          "Call log:\n" +
          "  - Expect \"toMatchAriaSnapshot\" with timeout 10000ms\n", stripAnsi(error.getMessage()));
    }
  }

  @PlaywrightTest
  @DisplayName("AR-130 should report error in YAML keys")
  @Fixme("java: the error message is \"Page expected to match Aria snapshot\\nError: Unterminated string:\\n...\" with the call log \"Assert \\\"matchesAriaSnapshot\\\" with timeout 5000ms\", not \"expect(page).toMatchAriaSnapshot(expected) failed\\n\\nExpected: ...\" with the call log \"Expect \\\"toMatchAriaSnapshot\\\" with timeout 10000ms\"")
  void shouldReportErrorInYAMLKeys(Page page) {
    page.setContent("<h1>title</h1>");

    {
      AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
          "      - heading \"title\n" +
          "    "));
      assertEquals("expect(page).toMatchAriaSnapshot(expected) failed\n" +
          "\n" +
          "Expected: \"- heading \\\"title\"\n" +
          "Error: Unterminated string:\n" +
          "\n" +
          "heading \"title\n" +
          "              ^\n" +
          "\n" +
          "Call log:\n" +
          "  - Expect \"toMatchAriaSnapshot\" with timeout 10000ms\n", stripAnsi(error.getMessage()));
    }

    {
      AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
          "      - heading /title\n" +
          "    "));
      assertEquals("expect(page).toMatchAriaSnapshot(expected) failed\n" +
          "\n" +
          "Expected: \"- heading /title\"\n" +
          "Error: Unterminated regex:\n" +
          "\n" +
          "heading /title\n" +
          "              ^\n" +
          "\n" +
          "Call log:\n" +
          "  - Expect \"toMatchAriaSnapshot\" with timeout 10000ms\n", stripAnsi(error.getMessage()));
    }

    {
      AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
          "      - heading [level=a]\n" +
          "    "));
      assertEquals("expect(page).toMatchAriaSnapshot(expected) failed\n" +
          "\n" +
          "Expected: \"- heading [level=a]\"\n" +
          "Error: Value of \"level\" attribute must be a number:\n" +
          "\n" +
          "heading [level=a]\n" +
          "               ^\n" +
          "\n" +
          "Call log:\n" +
          "  - Expect \"toMatchAriaSnapshot\" with timeout 10000ms\n", stripAnsi(error.getMessage()));
    }

    {
      AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
          "      - heading [expanded=FALSE]\n" +
          "    "));
      assertEquals("expect(page).toMatchAriaSnapshot(expected) failed\n" +
          "\n" +
          "Expected: \"- heading [expanded=FALSE]\"\n" +
          "Error: Value of \"expanded\" attribute must be a boolean:\n" +
          "\n" +
          "heading [expanded=FALSE]\n" +
          "                  ^\n" +
          "\n" +
          "Call log:\n" +
          "  - Expect \"toMatchAriaSnapshot\" with timeout 10000ms\n", stripAnsi(error.getMessage()));
    }

    {
      AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
          "      - heading [checked=foo]\n" +
          "    "));
      assertEquals("expect(page).toMatchAriaSnapshot(expected) failed\n" +
          "\n" +
          "Expected: \"- heading [checked=foo]\"\n" +
          "Error: Value of \"checked\" attribute must be a boolean or \"mixed\":\n" +
          "\n" +
          "heading [checked=foo]\n" +
          "                 ^\n" +
          "\n" +
          "Call log:\n" +
          "  - Expect \"toMatchAriaSnapshot\" with timeout 10000ms\n", stripAnsi(error.getMessage()));
    }

    {
      AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
          "      - heading [level=]\n" +
          "    "));
      assertEquals("expect(page).toMatchAriaSnapshot(expected) failed\n" +
          "\n" +
          "Expected: \"- heading [level=]\"\n" +
          "Error: Value of \"level\" attribute must be a number:\n" +
          "\n" +
          "heading [level=]\n" +
          "               ^\n" +
          "\n" +
          "Call log:\n" +
          "  - Expect \"toMatchAriaSnapshot\" with timeout 10000ms\n", stripAnsi(error.getMessage()));
    }

    {
      AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
          "      - heading [bogus]\n" +
          "    "));
      assertEquals("expect(page).toMatchAriaSnapshot(expected) failed\n" +
          "\n" +
          "Expected: \"- heading [bogus]\"\n" +
          "Error: Unsupported attribute [bogus]:\n" +
          "\n" +
          "heading [bogus]\n" +
          "         ^\n" +
          "\n" +
          "Call log:\n" +
          "  - Expect \"toMatchAriaSnapshot\" with timeout 10000ms\n", stripAnsi(error.getMessage()));
    }

    {
      AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
          "      - heading invalid\n" +
          "    "));
      assertEquals("expect(page).toMatchAriaSnapshot(expected) failed\n" +
          "\n" +
          "Expected: \"- heading invalid\"\n" +
          "Error: Unexpected input:\n" +
          "\n" +
          "heading invalid\n" +
          "        ^\n" +
          "\n" +
          "Call log:\n" +
          "  - Expect \"toMatchAriaSnapshot\" with timeout 10000ms\n", stripAnsi(error.getMessage()));
    }
  }

  @PlaywrightTest
  @DisplayName("AR-131 call log should contain actual snapshot")
  void callLogShouldContainActualSnapshot(Page page) {
    page.setContent("<h1>todos</h1>");
    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
        "    - heading \"wrong\"\n" +
        "  ", new PageAssertions.MatchesAriaSnapshotOptions().setTimeout(3000)));

    assertTrue(stripAnsi(error.getMessage()).contains("unexpected value \"- heading \"todos\" [level=1]\""));
  }

  @PlaywrightTest
  @DisplayName("AR-132 should parse attributes")
  void shouldParseAttributes(Page page) {
    {
      page.setContent("\n" +
          "      <button aria-pressed=\"mixed\">hello world</button>\n" +
          "    ");
      assertThat(page).matchesAriaSnapshot("\n" +
          "      - button [pressed=mixed ]\n" +
          "    ");
    }

    {
      page.setContent("\n" +
          "      <h2>hello world</h2>\n" +
          "    ");
      assertThat(page.locator("body")).not().matchesAriaSnapshot("\n" +
          "      - heading [level =  -3 ]\n" +
          "    ");
    }
  }

  @PlaywrightTest
  @DisplayName("AR-133 should not unshift actual template text")
  @Fixme("java: the assertion error message has no \"- Expected  - N / + Received  + M\" diff with the \"+ - heading ...\" lines, only the Expected: and Received: dumps")
  void shouldNotUnshiftActualTemplateText(Page page) {
    page.setContent("\n" +
        "    <h1>title</h1>\n" +
        "    <h1>title 2</h1>\n" +
        "  ");
    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
        "        - heading \"title\" [level=1]\n" +
        "    - heading \"title 2\" [level=1]\n" +
        "  ", new PageAssertions.MatchesAriaSnapshotOptions().setTimeout(1000)));
    assertTrue(stripAnsi(error.getMessage()).contains("\n" +
        "    - heading \"title\" [level=1]\n" +
        "- heading \"title 2\" [level=1]"));
  }

  @PlaywrightTest
  @DisplayName("AR-134 should not match what is not matched")
  @Fixme("java: the assertion error message has no \"- Expected  - N / + Received  + M\" diff with the \"+ - heading ...\" lines, only the Expected: and Received: dumps")
  void shouldNotMatchWhatIsNotMatched(Page page) {
    page.setContent("<p>Text</p>");
    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
        "    - paragraph:\n" +
        "      - button \"bogus\"\n" +
        "  "));
    assertTrue(stripAnsi(error.getMessage()).contains("\n" +
        "- - paragraph:\n" +
        "-   - button \"bogus\"\n" +
        "+ - paragraph: Text"));
  }

  @PlaywrightTest
  @DisplayName("AR-135 should match url")
  void shouldMatchUrl(Page page) {
    page.setContent("\n" +
        "    <a href='https://example.com'>Link</a>\n" +
        "  ");

    assertThat(page).matchesAriaSnapshot("\n" +
        "    - link:\n" +
        "      - /url: /.*example.com/\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-136 should detect unexpected children: equal")
  @Fixme("java: the assertion error message starts with \"Page expected to match Aria snapshot\" (for a locator \"Locator expected to match Aria snapshot\"), not \"expect(page).toMatchAriaSnapshot(expected) failed\" and has no \"- Expected  - N / + Received  + M\" diff, only the Expected: and Received: dumps")
  void shouldDetectUnexpectedChildrenEqual(Page page) {
    page.setContent("\n" +
        "    <ul>\n" +
        "      <li>One</li>\n" +
        "      <li>Two</li>\n" +
        "      <li>Three</li>\n" +
        "    </ul>\n" +
        "  ");

    assertThat(page).matchesAriaSnapshot("\n" +
        "    - list:\n" +
        "      - listitem: \"One\"\n" +
        "      - listitem: \"Three\"\n" +
        "  ");

    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
        "    - list:\n" +
        "      - /children: equal\n" +
        "      - listitem: \"One\"\n" +
        "      - listitem: \"Three\"\n" +
        "  ", new PageAssertions.MatchesAriaSnapshotOptions().setTimeout(1000)));

    assertTrue(stripAnsi(e.getMessage()).contains("expect(page).toMatchAriaSnapshot(expected) failed"));
    assertTrue(stripAnsi(e.getMessage()).contains("+   - listitem: Two"));
  }

  @PlaywrightTest
  @DisplayName("AR-137 should detect unexpected children: deep-equal")
  @Fixme("java: the assertion error message starts with \"Page expected to match Aria snapshot\" (for a locator \"Locator expected to match Aria snapshot\"), not \"expect(page).toMatchAriaSnapshot(expected) failed\" and has no \"- Expected  - N / + Received  + M\" diff, only the Expected: and Received: dumps")
  void shouldDetectUnexpectedChildrenDeepequal(Page page) {
    page.setContent("\n" +
        "    <ul>\n" +
        "      <li>\n" +
        "        <ul>\n" +
        "          <li>1.1</li>\n" +
        "          <li>1.2</li>\n" +
        "        </ul>\n" +
        "      </li>\n" +
        "    </ul>\n" +
        "  ");

    assertThat(page).matchesAriaSnapshot("\n" +
        "    - list:\n" +
        "      - listitem:\n" +
        "        - list:\n" +
        "          - listitem: 1.1\n" +
        "  ");

    assertThat(page).matchesAriaSnapshot("\n" +
        "    - list:\n" +
        "      - /children: equal\n" +
        "      - listitem:\n" +
        "        - list:\n" +
        "          - listitem: 1.1\n" +
        "  ");

    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
        "    - list:\n" +
        "      - /children: deep-equal\n" +
        "      - listitem:\n" +
        "        - list:\n" +
        "          - listitem: 1.1\n" +
        "  ", new PageAssertions.MatchesAriaSnapshotOptions().setTimeout(1000)));

    assertTrue(stripAnsi(e.getMessage()).contains("expect(page).toMatchAriaSnapshot(expected) failed"));
    assertTrue(stripAnsi(e.getMessage()).contains("+       - listitem: \"1.2\""));
  }

  @PlaywrightTest
  @DisplayName("AR-138 should allow restoring contain mode inside deep-equal")
  @Fixme("java: the assertion error message starts with \"Page expected to match Aria snapshot\" (for a locator \"Locator expected to match Aria snapshot\"), not \"expect(page).toMatchAriaSnapshot(expected) failed\" and has no \"- Expected  - N / + Received  + M\" diff, only the Expected: and Received: dumps")
  void shouldAllowRestoringContainModeInsideDeepequal(Page page) {
    page.setContent("\n" +
        "    <ul>\n" +
        "      <li>\n" +
        "        <ul>\n" +
        "          <li>1.1</li>\n" +
        "          <li>1.2</li>\n" +
        "        </ul>\n" +
        "      </li>\n" +
        "    </ul>\n" +
        "  ");

    AssertionFailedError e = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
        "    - list:\n" +
        "      - /children: deep-equal\n" +
        "      - listitem:\n" +
        "        - list:\n" +
        "          - listitem: 1.1\n" +
        "  ", new PageAssertions.MatchesAriaSnapshotOptions().setTimeout(1000)));

    assertTrue(stripAnsi(e.getMessage()).contains("expect(page).toMatchAriaSnapshot(expected) failed"));
    assertTrue(stripAnsi(e.getMessage()).contains("+       - listitem: \"1.2\""));

    assertThat(page).matchesAriaSnapshot("\n" +
        "    - list:\n" +
        "      - /children: deep-equal\n" +
        "      - listitem:\n" +
        "        - list:\n" +
        "          - /children: contain\n" +
        "          - listitem: 1.1\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-139 top-level deep-equal")
  @Fixme("java: the assertion error message has no \"- Expected  - N / + Received  + M\" diff with the \"+ - heading ...\" lines, only the Expected: and Received: dumps")
  void toplevelDeepequal(Page page) {
    page.setContent("\n" +
        "    <ul>\n" +
        "      <li>\n" +
        "        <ul>\n" +
        "          <li>1.1</li>\n" +
        "          <li>1.2</li>\n" +
        "        </ul>\n" +
        "      </li>\n" +
        "    </ul>\n" +
        "  ");

    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
        "    - /children: deep-equal\n" +
        "    - list\n" +
        "  ", new PageAssertions.MatchesAriaSnapshotOptions().setTimeout(1000)));

    assertTrue(stripAnsi(error.getMessage()).contains(("\n" +
        "- - /children: deep-equal\n" +
        "- - list\n" +
        "+ - list:\n" +
        "+   - listitem:\n" +
        "+     - list:\n" +
        "+       - listitem: \"1.1\"\n" +
        "+       - listitem: \"1.2\"\n" +
        "  ").trim()));
  }

  @PlaywrightTest
  @DisplayName("AR-140 generated snapshot includes text children when name is longer than text")
  void generatedSnapshotIncludesTextChildrenWhenNameIsLongerThanText(Page page) {
    page.setContent("\n" +
        "    <section>\n" +
        "      <div role=\"progressbar\" aria-label=\"Alpha Beta\" aria-valuenow=\"7\" aria-valuemin=\"0\" aria-valuemax=\"10\">\n" +
        "        <span>Alpha</span>\n" +
        "        <span>7</span>\n" +
        "      </div>\n" +
        "    </section>\n" +
        "  ");
    assertThat(page.locator("section")).matchesAriaSnapshot("\n" +
        "    - /children: deep-equal\n" +
        "    - progressbar \"Alpha Beta\": Alpha 7\n" +
        "  ");
  }

  @PlaywrightTest
  @DisplayName("AR-141 treat bad regex as a string")
  @Fixme("java: the assertion error message starts with \"Page expected to match Aria snapshot\" (for a locator \"Locator expected to match Aria snapshot\"), not \"expect(page).toMatchAriaSnapshot(expected) failed\" and has no \"- Expected  - N / + Received  + M\" diff, only the Expected: and Received: dumps")
  void treatBadRegexAsAString(Page page) {
    page.setContent("<a href=\"/foo\">Log in</a>");
    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
        "    - link \"Log in\":\n" +
        "      - /url: /[a/\n" +
        "  ", new PageAssertions.MatchesAriaSnapshotOptions().setTimeout(1)));
    assertTrue(stripAnsi(error.getMessage()).contains("expect(page).toMatchAriaSnapshot(expected) failed"));
    assertTrue(stripAnsi(error.getMessage()).contains("-   - /url: /[a/"));
    assertTrue(stripAnsi(error.getMessage()).contains("+   - /url: /foo"));
  }

  @PlaywrightTest
  @DisplayName("AR-142 invalid attribute")
  void invalidAttribute(Page page) {
    page.setContent("\n" +
        "    <input type=\"text\" aria-label=\"Email\" aria-invalid=\"true\" value=\"not-an-email\">\n" +
        "    <input type=\"text\" aria-label=\"Name\" value=\"Alice\">\n" +
        "  ");

    assertThat(page).matchesAriaSnapshot("\n" +
        "    - textbox \"Email\" [invalid]: not-an-email\n" +
        "    - textbox \"Name\": Alice\n" +
        "  ");

    assertThat(page).matchesAriaSnapshot("\n" +
        "    - textbox \"Email\" [invalid=true]: not-an-email\n" +
        "    - textbox \"Name\" [invalid=false]: Alice\n" +
        "  ");

    // `aria-invalid="grammar"` and `aria-invalid="spelling"` surface their underlying value.
    page.setContent("\n" +
        "    <input type=\"text\" aria-label=\"Bio\" aria-invalid=\"grammar\">\n" +
        "    <input type=\"text\" aria-label=\"Note\" aria-invalid=\"spelling\">\n" +
        "  ");
    assertThat(page).matchesAriaSnapshot("\n" +
        "    - textbox \"Bio\" [invalid=grammar]\n" +
        "    - textbox \"Note\" [invalid=spelling]\n" +
        "  ");

    // A `grammar` value is distinct from a plain `true` invalid state.
    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertThat(page).matchesAriaSnapshot("\n" +
        "    - textbox \"Bio\" [invalid]\n" +
        "  ", new PageAssertions.MatchesAriaSnapshotOptions().setTimeout(1)));
    assertTrue(stripAnsi(error.getMessage()).contains("[invalid=grammar]"));

    // Any other non-false value falls back to `true`.
    page.setContent("<input type=\"text\" aria-label=\"Zip\" aria-invalid=\"garbage\">");
    assertThat(page).matchesAriaSnapshot("\n" +
        "    - textbox \"Zip\" [invalid]\n" +
        "  ");
  }
}
