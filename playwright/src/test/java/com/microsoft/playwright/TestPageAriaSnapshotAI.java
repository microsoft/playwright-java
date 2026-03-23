package com.microsoft.playwright;

import com.microsoft.playwright.junit.FixtureTest;
import com.microsoft.playwright.junit.UsePlaywright;
import com.microsoft.playwright.options.AriaSnapshotMode;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@FixtureTest
@UsePlaywright
public class TestPageAriaSnapshotAI {
  private static String aiSnapshot(Page page) {
    return page.ariaSnapshot(new Page.AriaSnapshotOptions().setMode(AriaSnapshotMode.AI));
  }

  @Test
  void shouldGenerateRefs(Page page) {
    page.setContent("<button>One</button><button>Two</button><button>Three</button>");

    String snapshot1 = aiSnapshot(page);
    assertTrue(snapshot1.contains("button \"One\" [ref=e2]"), snapshot1);
    assertTrue(snapshot1.contains("button \"Two\" [ref=e3]"), snapshot1);
    assertTrue(snapshot1.contains("button \"Three\" [ref=e4]"), snapshot1);
    assertThat(page.locator("aria-ref=e2")).hasText("One");
    assertThat(page.locator("aria-ref=e3")).hasText("Two");
    assertThat(page.locator("aria-ref=e4")).hasText("Three");

    page.locator("aria-ref=e3").evaluate("e => e.textContent = 'Not Two'");

    String snapshot2 = aiSnapshot(page);
    assertTrue(snapshot2.contains("button \"One\" [ref=e2]"), snapshot2);
    assertTrue(snapshot2.contains("button \"Not Two\" [ref=e5]"), snapshot2);
    assertTrue(snapshot2.contains("button \"Three\" [ref=e4]"), snapshot2);
  }

  @Test
  void shouldListIframes(Page page) {
    page.setContent(
      "<h1>Hello</h1>" +
      "<iframe name=\"foo\" src=\"data:text/html,<h1>World</h1>\">");

    String snapshot = aiSnapshot(page);
    assertTrue(snapshot.contains("- iframe"), snapshot);

    String frameSnapshot = page.frameLocator("iframe").locator("body").ariaSnapshot();
    assertEquals("- heading \"World\" [level=1]", frameSnapshot);
  }

  @Test
  void shouldSnapshotLocatorInsideIframe(Page page) {
    page.setContent(
      "<h1>Main Page</h1>" +
      "<iframe srcdoc=\"<ul><li>Item 1</li><li>Item 2</li></ul>\"></iframe>");

    Locator list = page.frames().get(1).locator("ul");
    String snapshot = list.ariaSnapshot(new Locator.AriaSnapshotOptions().setMode(AriaSnapshotMode.AI));
    assertTrue(snapshot.contains("list [ref=f1e1]"), snapshot);
    assertTrue(snapshot.contains("listitem [ref=f1e2]: Item 1"), snapshot);
    assertTrue(snapshot.contains("listitem [ref=f1e3]: Item 2"), snapshot);
  }

  @Test
  void shouldCollapseGenericNodes(Page page) {
    page.setContent("<div><div><div><button>Button</button></div></div></div>");
    String snapshot = aiSnapshot(page);
    assertTrue(snapshot.contains("button \"Button\" [ref=e5]"), snapshot);
  }

  @Test
  void shouldIncludeCursorPointerHint(Page page) {
    page.setContent("<button style=\"cursor: pointer\">Button</button>");
    String snapshot = aiSnapshot(page);
    assertTrue(snapshot.contains("button \"Button\" [ref=e2] [cursor=pointer]"), snapshot);
  }

  @Test
  void shouldNotNestCursorPointerHints(Page page) {
    page.setContent(
      "<a style=\"cursor: pointer\" href=\"about:blank\">" +
      "Link with a button <button style=\"cursor: pointer\">Button</button>" +
      "</a>");
    String snapshot = aiSnapshot(page);
    assertTrue(snapshot.contains("link \"Link with a button Button\" [ref=e2] [cursor=pointer]"), snapshot);
    // The button inside a cursor-pointer link should not get a redundant [cursor=pointer]
    assertTrue(snapshot.contains("button \"Button\" [ref=e3]"), snapshot);
    assertFalse(snapshot.contains("button \"Button\" [ref=e3] [cursor=pointer]"), snapshot);
  }

  @Test
  void shouldShowVisibleChildrenOfHiddenElements(Page page) {
    page.setContent(
      "<div style=\"visibility: hidden\">" +
      "  <div style=\"visibility: visible\"><button>Visible</button></div>" +
      "  <div style=\"visibility: hidden\"><button style=\"visibility: visible\">Visible</button></div>" +
      "  <div>" +
      "    <div style=\"visibility: visible\"><button style=\"visibility: hidden\">Hidden</button></div>" +
      "    <button>Hidden</button>" +
      "  </div>" +
      "</div>");
    String snapshot = aiSnapshot(page);
    assertEquals(
      "- generic [active] [ref=e1]:\n" +
      "  - button \"Visible\" [ref=e3]\n" +
      "  - button \"Visible\" [ref=e4]",
      snapshot);
  }

  @Test
  void shouldIncludeActiveElementInformation(Page page) {
    page.setContent(
      "<button id=\"btn1\">Button 1</button>" +
      "<button id=\"btn2\" autofocus>Button 2</button>" +
      "<div>Not focusable</div>");
    page.waitForFunction("document.activeElement?.id === 'btn2'");

    String snapshot = aiSnapshot(page);
    assertTrue(snapshot.contains("button \"Button 2\" [active] [ref=e3]"), snapshot);
    assertFalse(snapshot.contains("button \"Button 1\" [active]"), snapshot);
  }

  @Test
  void shouldUpdateActiveElementOnFocus(Page page) {
    page.setContent(
      "<input id=\"input1\" placeholder=\"First input\">" +
      "<input id=\"input2\" placeholder=\"Second input\">");

    String initialSnapshot = aiSnapshot(page);
    assertTrue(initialSnapshot.contains("textbox \"First input\" [ref=e2]"), initialSnapshot);
    assertTrue(initialSnapshot.contains("textbox \"Second input\" [ref=e3]"), initialSnapshot);
    assertFalse(initialSnapshot.contains("textbox \"First input\" [active]"), initialSnapshot);
    assertFalse(initialSnapshot.contains("textbox \"Second input\" [active]"), initialSnapshot);

    page.locator("#input2").focus();

    String afterFocusSnapshot = aiSnapshot(page);
    assertTrue(afterFocusSnapshot.contains("textbox \"Second input\" [active] [ref=e3]"), afterFocusSnapshot);
    assertFalse(afterFocusSnapshot.contains("textbox \"First input\" [active]"), afterFocusSnapshot);
  }

  @Test
  void shouldCollapseInlineGenericNodes(Page page) {
    page.setContent(
      "<ul>" +
      "<li><b>3</b> <abbr>bds</abbr></li>" +
      "<li><b>2</b> <abbr>ba</abbr></li>" +
      "<li><b>1,200</b> <abbr>sqft</abbr></li>" +
      "</ul>");
    String snapshot = aiSnapshot(page);
    assertTrue(snapshot.contains("listitem [ref=e3]: 3 bds"), snapshot);
    assertTrue(snapshot.contains("listitem [ref=e4]: 2 ba"), snapshot);
    assertTrue(snapshot.contains("listitem [ref=e5]: 1,200 sqft"), snapshot);
  }

  @Test
  void shouldNotRemoveGenericNodesWithTitle(Page page) {
    page.setContent("<div title=\"Element title\">Element content</div>");
    String snapshot = aiSnapshot(page);
    assertTrue(snapshot.contains("generic \"Element title\" [ref=e2]"), snapshot);
  }

  @Test
  void shouldLimitDepth(Page page) {
    page.setContent(
      "<ul>" +
      "<li>item1</li>" +
      "<a href=\"about:blank\" style=\"cursor:pointer\">link</a>" +
      "<li><ul id=\"target\"><li>item2</li><li><ul><li>item3</li></ul></li></ul></li>" +
      "</ul>");

    String snapshot1 = page.ariaSnapshot(new Page.AriaSnapshotOptions().setMode(AriaSnapshotMode.AI).setDepth(1));
    assertTrue(snapshot1.contains("listitem [ref=e3]: item1"), snapshot1);
    assertFalse(snapshot1.contains("item2"), snapshot1);
    assertFalse(snapshot1.contains("item3"), snapshot1);

    String snapshot2 = page.ariaSnapshot(new Page.AriaSnapshotOptions().setMode(AriaSnapshotMode.AI).setDepth(3));
    assertTrue(snapshot2.contains("item1"), snapshot2);
    assertTrue(snapshot2.contains("item2"), snapshot2);
    assertFalse(snapshot2.contains("item3"), snapshot2);

    String snapshot3 = page.ariaSnapshot(new Page.AriaSnapshotOptions().setMode(AriaSnapshotMode.AI).setDepth(100));
    assertTrue(snapshot3.contains("item1"), snapshot3);
    assertTrue(snapshot3.contains("item2"), snapshot3);
    assertTrue(snapshot3.contains("item3"), snapshot3);

    String snapshot4 = page.locator("#target").ariaSnapshot(new Locator.AriaSnapshotOptions().setMode(AriaSnapshotMode.AI).setDepth(1));
    assertTrue(snapshot4.contains("listitem [ref=e7]: item2"), snapshot4);
    assertFalse(snapshot4.contains("item3"), snapshot4);
  }
}
