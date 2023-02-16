package com.microsoft.playwright;

import com.microsoft.playwright.assertions.LocatorAssertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

// Copied from expect-misc.spec.ts > toBeInViewport
public class TestAssertThatIsInViewport extends TestBase {
  @Test
  void shouldWork() {
    page.setContent("<div id=big style=\"height: 10000px;\"></div>\n" +
      "      <div id=small>foo</div>");
    assertThat(page.locator("#big")).isInViewport();
    assertThat(page.locator("#small")).not().isInViewport();
    page.locator("#small").scrollIntoViewIfNeeded();
    assertThat(page.locator("#small")).isInViewport();
    assertThat(page.locator("#small")).isInViewport(new LocatorAssertions.IsInViewportOptions().setRatio(1));
  }

  @Test
  void shouldRespectRatioOption() {
    page.setContent("<style>body, div, html { padding: 0; margin: 0; }</style>\n" +
      "      <div id=big style=\"height: 400vh;\"></div>");
    assertThat(page.locator("div")).isInViewport();
    assertThat(page.locator("div")).isInViewport(new LocatorAssertions.IsInViewportOptions().setRatio(0.1));
    assertThat(page.locator("div")).isInViewport(new LocatorAssertions.IsInViewportOptions().setRatio(0.2));

    assertThat(page.locator("div")).isInViewport(new LocatorAssertions.IsInViewportOptions().setRatio(0.24));
    // In this test, element's ratio is 0.25.
    assertThat(page.locator("div")).isInViewport(new LocatorAssertions.IsInViewportOptions().setRatio(0.25));
    assertThat(page.locator("div")).not().isInViewport(new LocatorAssertions.IsInViewportOptions().setRatio(0.26));

    assertThat(page.locator("div")).not().isInViewport(new LocatorAssertions.IsInViewportOptions().setRatio(0.3));
    assertThat(page.locator("div")).not().isInViewport(new LocatorAssertions.IsInViewportOptions().setRatio(0.7));
    assertThat(page.locator("div")).not().isInViewport(new LocatorAssertions.IsInViewportOptions().setRatio(0.8));
  }

  @Test
  void shouldHaveGoodStack() {
    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertThat(page.locator("body")).not().isInViewport(new LocatorAssertions.IsInViewportOptions().setTimeout(100)));
    assertNotNull(error);
    assertTrue(error.getMessage().contains("Locator expected not to be in viewport"), error.getMessage());
  }

  @Test
  void shouldReportIntersectionEvenIfFullyCoveredByOtherElement() {
    page.setContent("<h1>hello</h1>\n" +
      "      <div style=\"position: relative; height: 10000px; top: -5000px;></div>");
    assertThat(page.locator("h1")).isInViewport();
  }
}
