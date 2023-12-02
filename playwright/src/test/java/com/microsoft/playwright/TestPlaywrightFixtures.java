package com.microsoft.playwright;

import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@UsePlaywright
public class TestPlaywrightFixtures {
  private static Playwright playwrightFromBeforeAll;
  private static Browser browserFromBeforeAll;
  private BrowserContext browserContextFromBeforeEach;
  private Page pageFromBeforeEach;
  private static APIRequestContext apiRequestContextFromBeforeAll;
  private APIRequestContext apiRequestContextFromBeforeEach;

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
    assertNotEquals(apiRequestContextFromBeforeAll, apiRequestContext);

    assertNotNull(browserContext);
    assertNotNull(page);
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
