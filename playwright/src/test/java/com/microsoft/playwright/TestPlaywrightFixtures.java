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
  private BrowserContext browserContextFromBeforeEach;
  private Page pageFromBeforeEach;
  private APIRequestContext apiRequestContextFromBeforeEach;

  @BeforeAll
  public static void beforeAll(Playwright playwright, Browser browser) {
    assertNotNull(playwright);
    assertNotNull(browser);

    playwrightFromBeforeAll = playwright;
    browserFromBeforeAll = browser;
  }

  @BeforeEach
  public void beforeEach(Playwright playwright, Browser browser, BrowserContext browserContext, Page page, APIRequestContext apiRequestContext) {
    assertEquals(playwrightFromBeforeAll, playwright);
    assertEquals(browserFromBeforeAll, browser);
    assertNotNull(browserContext);
    assertNotNull(page);
    assertNotNull(apiRequestContext);
    browserContextFromBeforeEach = browserContext;
    pageFromBeforeEach = page;
    apiRequestContextFromBeforeEach = apiRequestContext;
  }

  @Test
  public void objectShouldBeSameAsBeforeAll(Playwright playwright, Browser browser) {
    assertEquals(playwrightFromBeforeAll, playwright);
    assertEquals(browserFromBeforeAll, browser);
  }

  @Test
  public void objectShouldBeSameAsBeforeEach(BrowserContext browserContext, Page page, APIRequestContext apiRequestContext) {
    assertEquals(browserContextFromBeforeEach, browserContext);
    assertEquals(pageFromBeforeEach, page);
    assertEquals(apiRequestContextFromBeforeEach, apiRequestContext);
  }
}
