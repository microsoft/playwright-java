package com.microsoft.playwright;

import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@UsePlaywright
public class TestPlaywrightFixtures {
  private static Playwright playwrightFromBeforeAll;
  private static Browser browserFromBeforeAll;

  @BeforeAll
  public static void beforeAll(Playwright playwright, Browser browser) {
    assertNotNull(playwright);
    assertNotNull(browser);

    playwrightFromBeforeAll = playwright;
    browserFromBeforeAll = browser;
  }

  @BeforeEach
  public void beforeEach(Playwright playwright, Browser browser) {
    assertEquals(playwrightFromBeforeAll, playwright);
    assertEquals(browserFromBeforeAll, browser);
  }

  @Test
  public void objectShouldBeSameAsBeforeAll(Playwright playwright, Browser browser) {
    assertEquals(playwrightFromBeforeAll, playwright);
    assertEquals(browserFromBeforeAll, browser);
  }
}
