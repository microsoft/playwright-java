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
  private static APIRequestContext apiRequestContextFromBeforeAll;

  @BeforeAll
  public static void beforeAll(Playwright playwright, Browser browser, APIRequestContext apiRequestContext) {
    assertNotNull(playwright);
    assertNotNull(browser);
    assertNotNull(apiRequestContext);

    playwrightFromBeforeAll = playwright;
    browserFromBeforeAll = browser;
    apiRequestContextFromBeforeAll = apiRequestContext;
  }

  @BeforeEach
  public void beforeEach(Playwright playwright, Browser browser, BrowserContext browserContext, Page page, APIRequestContext apiRequestContext) {
    assertEquals(playwrightFromBeforeAll, playwright);
    assertEquals(browserFromBeforeAll, browser);
    assertEquals(apiRequestContextFromBeforeAll, apiRequestContext);
    assertNotNull(browserContext);
    assertNotNull(page);
    browserContextFromBeforeEach = browserContext;
    pageFromBeforeEach = page;
  }

  @Test
  public void objectShouldBeSameAsBeforeAll(Playwright playwright, Browser browser, APIRequestContext apiRequestContext) {
    assertEquals(playwrightFromBeforeAll, playwright);
    assertEquals(browserFromBeforeAll, browser);
    assertEquals(apiRequestContextFromBeforeAll, apiRequestContext);
  }

  @Test
  public void objectShouldBeSameAsBeforeEach(BrowserContext browserContext, Page page) {
    assertEquals(browserContextFromBeforeEach, browserContext);
    assertEquals(pageFromBeforeEach, page);
  }
}
