package com.microsoft.playwright;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * This test simply makes sure that all 4 main interfaces implement AutoCloseable. If it compiles, then it passes.
 */
public class TestAutoClose {
  @Test
  void shouldAllowUsingTryWithResources() throws Exception {
    try (Playwright playwright = Playwright.create();
         Browser browser = Utils.getBrowserTypeFromEnv(playwright).launch();
         BrowserContext context = browser.newContext();
         Page page = context.newPage()) {
      assertEquals(2021, page.evaluate("() => 2021"));
    }
  }
}
