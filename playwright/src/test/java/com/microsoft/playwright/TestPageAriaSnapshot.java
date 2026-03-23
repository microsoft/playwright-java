package com.microsoft.playwright;

import com.microsoft.playwright.junit.FixtureTest;
import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@FixtureTest
@UsePlaywright
public class TestPageAriaSnapshot {
  public static String unshift(String snapshot) {
    List<String> lines = Arrays.asList(snapshot.split("\n"));
    int whitespacePrefixLength = 100;
    Pattern pattern = Pattern.compile("^(\\s*).*");
    for (String line : lines) {
      if (line.trim().isEmpty())
        continue;
      Matcher matcher = pattern.matcher(line);
      if (!matcher.matches()) {
        continue;
      }
      String match = matcher.group(1);
      if (match.length() < whitespacePrefixLength) {
        whitespacePrefixLength = match.length();
      }
    }
    final int prefixLength = whitespacePrefixLength;
    return lines.stream()
      .filter(line -> !line.trim().isEmpty())
      .map(line -> line.substring(prefixLength))
      .collect(Collectors.joining("\n"));
  }

  private static void checkAndMatchSnapshot(Locator locator, String snapshot) {
    assertEquals(unshift(snapshot), locator.ariaSnapshot());
    assertThat(locator).matchesAriaSnapshot(snapshot);
  }

  @Test
  void shouldSnapshot(Page page) {
    page.setContent("<h1>title</h1>");
    checkAndMatchSnapshot(page.locator("body"), "- heading \"title\" [level=1]");
  }

  @Test
  void shouldSnapshotList(Page page) {
    page.setContent("<h1>title</h1><h1>title 2</h1>");
    checkAndMatchSnapshot(page.locator("body"), "" +
      "  - heading \"title\" [level=1]\n" +
      "  - heading \"title 2\" [level=1]");
  }

  @Test
  void shouldSnapshotListWithAccessibleName(Page page) {
    page.setContent("<ul aria-label=\"my list\"><li>one</li><li>two</li></ul>");
    checkAndMatchSnapshot(page.locator("body"), "- list \"my list\":\n  - listitem: one\n  - listitem: two");
  }

  @Test
  void shouldSnapshotComplex(Page page) {
    page.setContent("<ul><li><a href='about:blank'>link</a></li></ul>");
    checkAndMatchSnapshot(page.locator("body"), "- list:\n  - listitem:\n    - link \"link\":\n      - /url: about:blank");
  }

  @Test
  void shouldAllowTextNodes(Page page) {
    page.setContent("<h1>Microsoft</h1><div>Open source projects and samples from Microsoft</div>");
    checkAndMatchSnapshot(page.locator("body"), "" +
      "    - heading \"Microsoft\" [level=1]\n" +
      "    - text: Open source projects and samples from Microsoft");
  }

  @Test
  void shouldSnapshotDetailsVisibility(Page page) {
    page.setContent("<details><summary>Summary</summary><div>Details</div></details>");
    checkAndMatchSnapshot(page.locator("body"), "- group: Summary");
  }

  @Test
  void shouldSnapshotChildren(Page page) {
    page.setContent("<ul><li><img />One</li><li>Two</li><li>Three</li></ul>");
    assertThat(page.locator("body")).matchesAriaSnapshot("- list:\n  - /children: equal\n  - listitem\n  - listitem: Two\n  - listitem: Three");
    assertThat(page.locator("body")).not().matchesAriaSnapshot("- list:\n  - /children: equal\n  - listitem\n  - listitem: Two");

    assertThat(page.locator("body")).matchesAriaSnapshot("- list:\n  - /children: deep-equal\n  - listitem:\n    - img\n    - text: One\n  - listitem: Two\n  - listitem: Three");
    assertThat(page.locator("body")).not().matchesAriaSnapshot("- list:\n  - /children: deep-equal\n  - listitem:\n    - text: One\n  - listitem: Two\n  - listitem: Three");
    assertThat(page.locator("body")).matchesAriaSnapshot("- list:\n  - /children: deep-equal\n  - listitem:\n    - /children: contain\n    - text: One\n  - listitem: Two\n  - listitem: Three");
  }

  @Test
  void shouldMatchUrl(Page page) {
    page.setContent("<a href='https://example.com'>Link</a>");
    assertThat(page.locator("body")).matchesAriaSnapshot("" +
      "- link:\n" +
      "  - /url: /.*example.com/");
  }

  @Test
  void shouldHandleTopLevelDeepEqual(Page page) {
    // https://github.com/microsoft/playwright/issues/36456
    page.setContent("" +
      "<ul>\n" +
      "  <li>\n" +
      "    <ul>\n" +
      "      <li>1.1</li>\n" +
      "      <li>1.2</li>\n" +
      "    </ul>\n" +
      "  </li>\n" +
      "</ul>");

    assertThrows(AssertionFailedError.class, () -> {
      assertThat(page.locator("body")).matchesAriaSnapshot("" +
        "- /children: deep-equal\n" +
        "- list:\n" +
        "  - listitem:\n" +
        "    - listitem: \"1.1\"\n" +
        "    - listitem: \"1.2\"");
    });
  }

  @Test
  void matchValuesBothAgainstRegexAndString(Page page) {
    page.setContent("<a href=\"/auth?r=/\">Log in</a>");
    checkAndMatchSnapshot(page.locator("body"),
      "- link \"Log in\":\n" +
      "  - /url: /auth?r=/");
  }

  @Test
  void shouldSnapshotIntegration(Page page) {
    page.setContent(
      "<h1>Microsoft</h1>" +
      "<div>Open source projects and samples from Microsoft</div>" +
      "<ul>" +
      "<li><details><summary>Verified</summary><div><div>" +
      "<p>We've verified that the organization <strong>microsoft</strong> controls the domain:</p>" +
      "<ul><li class=\"mb-1\"><strong>opensource.microsoft.com</strong></li></ul>" +
      "<div><a href=\"about: blank\">Learn more about verified organizations</a></div>" +
      "</div></div></details></li>" +
      "<li><a href=\"about:blank\"><summary title=\"Label: GitHub Sponsor\">Sponsor</summary></a></li>" +
      "</ul>");
    checkAndMatchSnapshot(page.locator("body"),
      "- heading \"Microsoft\" [level=1]\n" +
      "- text: Open source projects and samples from Microsoft\n" +
      "- list:\n" +
      "  - listitem:\n" +
      "    - group: Verified\n" +
      "  - listitem:\n" +
      "    - link \"Sponsor\":\n" +
      "      - /url: about:blank");
  }

  @Test
  void shouldSupportMultilineText(Page page) {
    page.setContent("<p>\n      Line 1\n      Line 2\n      Line 3\n    </p>");
    checkAndMatchSnapshot(page.locator("body"), "- paragraph: Line 1 Line 2 Line 3");
    assertThat(page.locator("body")).matchesAriaSnapshot(
      "    - paragraph: |\n" +
      "        Line 1\n" +
      "        Line 2\n" +
      "        Line 3");
  }

  @Test
  void shouldConcatenateSpanText(Page page) {
    page.setContent("<span>One</span> <span>Two</span> <span>Three</span>");
    checkAndMatchSnapshot(page.locator("body"), "- text: One Two Three");
  }

  @Test
  void shouldConcatenateSpanText2(Page page) {
    page.setContent("<span>One </span><span>Two </span><span>Three</span>");
    checkAndMatchSnapshot(page.locator("body"), "- text: One Two Three");
  }

  @Test
  void shouldConcatenateDivTextWithSpaces(Page page) {
    page.setContent("<div>One</div><div>Two</div><div>Three</div>");
    checkAndMatchSnapshot(page.locator("body"), "- text: One Two Three");
  }

  @Test
  void shouldIncludePseudoInText(Page page) {
    page.setContent(
      "<style>span:before { content: 'world'; } div:after { content: 'bye'; }</style>" +
      "<a href=\"about:blank\"><span>hello</span><div>hello</div></a>");
    checkAndMatchSnapshot(page.locator("body"),
      "- link \"worldhello hellobye\":\n" +
      "  - /url: about:blank");
  }

  @Test
  void shouldNotIncludeHiddenPseudoInText(Page page) {
    page.setContent(
      "<style>span:before { content: 'world'; display: none; } div:after { content: 'bye'; visibility: hidden; }</style>" +
      "<a href=\"about:blank\"><span>hello</span><div>hello</div></a>");
    checkAndMatchSnapshot(page.locator("body"),
      "- link \"hello hello\":\n" +
      "  - /url: about:blank");
  }

  @Test
  void shouldIncludeNewLineForBlockPseudo(Page page) {
    page.setContent(
      "<style>span:before { content: 'world'; display: block; } div:after { content: 'bye'; display: block; }</style>" +
      "<a href=\"about:blank\"><span>hello</span><div>hello</div></a>");
    checkAndMatchSnapshot(page.locator("body"),
      "- link \"world hello hello bye\":\n" +
      "  - /url: about:blank");
  }

  @Test
  void shouldWorkWithSlots(Page page) {
    // Text "foo" is assigned to the slot, should not be used twice.
    page.setContent(
      "<button><div>foo</div></button>" +
      "<script>(() => {" +
      "  const container = document.querySelector('div');" +
      "  const shadow = container.attachShadow({ mode: 'open' });" +
      "  const slot = document.createElement('slot');" +
      "  shadow.appendChild(slot);" +
      "})()</script>");
    checkAndMatchSnapshot(page.locator("body"), "- button \"foo\"");

    // Text "foo" is assigned to the slot, should be used instead of slot content.
    page.setContent(
      "<div>foo</div>" +
      "<script>(() => {" +
      "  const container = document.querySelector('div');" +
      "  const shadow = container.attachShadow({ mode: 'open' });" +
      "  const button = document.createElement('button');" +
      "  shadow.appendChild(button);" +
      "  const slot = document.createElement('slot');" +
      "  button.appendChild(slot);" +
      "  const span = document.createElement('span');" +
      "  span.textContent = 'pre';" +
      "  slot.appendChild(span);" +
      "})()</script>");
    checkAndMatchSnapshot(page.locator("body"), "- button \"foo\"");

    // Nothing is assigned to the slot, should use slot content.
    page.setContent(
      "<div></div>" +
      "<script>(() => {" +
      "  const container = document.querySelector('div');" +
      "  const shadow = container.attachShadow({ mode: 'open' });" +
      "  const button = document.createElement('button');" +
      "  shadow.appendChild(button);" +
      "  const slot = document.createElement('slot');" +
      "  button.appendChild(slot);" +
      "  const span = document.createElement('span');" +
      "  span.textContent = 'pre';" +
      "  slot.appendChild(span);" +
      "})()</script>");
    checkAndMatchSnapshot(page.locator("body"), "- button \"pre\"");
  }

  @Test
  void shouldSnapshotInnerText(Page page) {
    page.setContent(
      "<div role=\"listitem\"><div><div><span title=\"a.test.ts\">a.test.ts</span></div>" +
      "<div><button title=\"Run\"></button><button title=\"Show source\"></button><button title=\"Watch\"></button></div></div></div>" +
      "<div role=\"listitem\"><div><div><span title=\"snapshot\">snapshot</span></div>" +
      "<div class=\"ui-mode-list-item-time\">30ms</div>" +
      "<div><button title=\"Run\"></button><button title=\"Show source\"></button><button title=\"Watch\"></button></div></div></div>");
    checkAndMatchSnapshot(page.locator("body"),
      "    - listitem:\n" +
      "      - text: a.test.ts\n" +
      "      - button \"Run\"\n" +
      "      - button \"Show source\"\n" +
      "      - button \"Watch\"\n" +
      "    - listitem:\n" +
      "      - text: snapshot 30ms\n" +
      "      - button \"Run\"\n" +
      "      - button \"Show source\"\n" +
      "      - button \"Watch\"");
  }

  @Test
  void checkAriaHiddenText(Page page) {
    page.setContent("<p><span>hello</span><span aria-hidden=\"true\">world</span></p>");
    checkAndMatchSnapshot(page.locator("body"), "- paragraph: hello");
  }

  @Test
  void shouldIgnorePresentationAndNoneRoles(Page page) {
    page.setContent("<ul><li role=\"presentation\">hello</li><li role=\"none\">world</li></ul>");
    checkAndMatchSnapshot(page.locator("body"), "- list: hello world");
  }

  @Test
  void shouldNotUseOnAsCheckboxValue(Page page) {
    page.setContent("<input type=\"checkbox\"><input type=\"radio\">");
    checkAndMatchSnapshot(page.locator("body"), "- checkbox\n- radio");
  }

  @Test
  void shouldNotReportTextareaTextContent(Page page) {
    page.setContent("<textarea>Before</textarea>");
    checkAndMatchSnapshot(page.locator("body"), "- textbox: Before");
    page.evaluate("document.querySelector('textarea').value = 'After'");
    checkAndMatchSnapshot(page.locator("body"), "- textbox: After");
  }

  @Test
  void shouldNotShowVisibleChildrenOfHiddenElements(Page page) {
    page.setContent(
      "<div style=\"visibility: hidden;\">" +
      "<div style=\"visibility: visible;\"><button>Button</button></div>" +
      "</div>");
    assertEquals("", page.locator("body").ariaSnapshot());
  }

  @Test
  void shouldNotShowUnhiddenChildrenOfAriaHiddenElements(Page page) {
    page.setContent(
      "<div aria-hidden=\"true\">" +
      "<div aria-hidden=\"false\"><button>Button</button></div>" +
      "</div>");
    assertEquals("", page.locator("body").ariaSnapshot());
  }

  @Test
  void shouldSnapshotPlaceholderWhenDifferentFromName(Page page) {
    page.setContent("<input placeholder=\"Placeholder\">");
    assertThat(page.locator("body")).matchesAriaSnapshot("- textbox \"Placeholder\"");

    page.setContent("<input placeholder=\"Placeholder\" aria-label=\"Label\">");
    assertThat(page.locator("body")).matchesAriaSnapshot(
      "- textbox \"Label\":\n" +
      "  - /placeholder: Placeholder");
  }

}
