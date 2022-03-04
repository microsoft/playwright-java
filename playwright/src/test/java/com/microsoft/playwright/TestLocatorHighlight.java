package com.microsoft.playwright;

import com.google.gson.Gson;
import com.microsoft.playwright.options.BoundingBox;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestLocatorHighlight extends TestBase {
  @Disabled("Requires isUnderTest to be true https://github.com/microsoft/playwright/pull/12420")
  @Test
  void shouldHighlightLocator() {
    page.setContent("<input type='text' />");
    page.locator("input").highlight();
    assertThat(page.locator("x-pw-tooltip")).hasText("input");
    assertThat(page.locator("x-pw-highlight")).isVisible();
    BoundingBox box1 = page.locator("input").boundingBox();
    BoundingBox box2 = page.locator("x-pw-highlight").boundingBox();
    assertEquals(new Gson().toJson(box2), new Gson().toJson(box1));
  }
}
