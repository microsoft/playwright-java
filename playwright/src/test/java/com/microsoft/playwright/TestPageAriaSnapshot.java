package com.microsoft.playwright;

import com.microsoft.playwright.junit.FixtureTest;
import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@FixtureTest
@UsePlaywright
public class TestPageAriaSnapshot {
  private static void checkAndMatchSnapshot(Locator locator, String snapshot) {
    assertEquals(snapshot, locator.ariaSnapshot());
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
    checkAndMatchSnapshot(page.locator("body"), "- heading \"title\" [level=1]\n- heading \"title 2\" [level=1]");
  }

  @Test
  void shouldSnapshotListWithAccessibleName(Page page) {
    page.setContent("<ul aria-label=\"my list\"><li>one</li><li>two</li></ul>");
    checkAndMatchSnapshot(page.locator("body"), "- list \"my list\":\n  - listitem: one\n  - listitem: two");
  }

  @Test
  void shouldSnapshotComplex(Page page) {
    page.setContent("<ul><li><a href='about:blank'>link</a></li></ul>");
    checkAndMatchSnapshot(page.locator("body"), "- list:\n  - listitem:\n    - link \"link\"");
  }

  @Test
  void shouldAllowTextNodes(Page page) {
    page.setContent("<h1>Microsoft</h1><div>Open source projects and samples from Microsoft</div>");
    checkAndMatchSnapshot(page.locator("body"), "- heading \"Microsoft\" [level=1]\n- text: Open source projects and samples from Microsoft");
  }

  @Test
  void shouldSnapshotDetailsVisibility(Page page) {
    page.setContent("<details><summary>Summary</summary><div>Details</div></details>");
    checkAndMatchSnapshot(page.locator("body"), "- group: Summary");
  }
}
