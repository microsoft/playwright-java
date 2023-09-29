package com.microsoft.playwright;

import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@UsePlaywright
public class TestFixturesWithDefaultFactories {
  private static Playwright playwrightFromBeforeAll;
  private static Browser browserFromBeforeAll;
  private BrowserContext browserContextFromBeforeEach;
  private Page pageFromBeforeEach;

  @BeforeAll
  static void beforeAll(Playwright playwright, Browser browser) {
    assertNotNull(playwright);
    assertNotNull(browser);
    String browserName = System.getenv("BROWSER") == null ? "chromium" : System.getenv("BROWSER");
    assertEquals(browserName, browser.browserType().name());
    playwrightFromBeforeAll = playwright;
    browserFromBeforeAll = browser;
  }

  @AfterAll
  static void afterAll(Playwright playwright, Browser browser) {
    assertNotNull(playwright);
    assertNotNull(browser);
    assertEquals(playwrightFromBeforeAll, playwright);
    assertEquals(browserFromBeforeAll, browser);
  }

  @BeforeEach
  void beforeEach(Playwright playwright, Browser browser, BrowserContext browserContext, Page page) {
    assertNotNull(playwright);
    assertNotNull(browser);
    assertNotNull(browserContext);
    assertNotNull(page);
    assertEquals(playwrightFromBeforeAll, playwright);
    assertEquals(browserFromBeforeAll, browser);

    this.browserContextFromBeforeEach = browserContext;
    this.pageFromBeforeEach = page;
  }

  @AfterEach
  void afterEach(Playwright playwright, Browser browser, BrowserContext browserContext, Page page) {
    assertNotNull(playwright);
    assertNotNull(browser);
    assertNotNull(browserContext);
    assertNotNull(page);
    assertEquals(playwrightFromBeforeAll, playwright);
    assertEquals(browserFromBeforeAll, browser);
  }

  @Test
  void objectsAreSameFromBeforeAll(Playwright playwright, Browser browser) {
    assertNotNull(playwright);
    assertNotNull(browser);
    assertEquals(playwrightFromBeforeAll, playwright);
    assertEquals(browserFromBeforeAll, browser);
  }

  @Test
  void objectsAreSameFromBeforeEach(BrowserContext browserContext, Page page) {
    assertNotNull(browserContext);
    assertNotNull(page);
    assertEquals(this.browserContextFromBeforeEach, browserContext);
    assertEquals(this.pageFromBeforeEach, page);
  }

  @Test
  void duplicatePlaywrightReturnTheSameObject(Playwright playwright1, Playwright playwright2) {
    assertEquals(playwright1, playwright2);
  }

  @Test
  void duplicateBrowserReturnTheSameObject(Browser browser1, Browser browser2) {
    assertEquals(browser1, browser2);
  }

  @Test
  void duplicateBrowserContextReturnTheSameObject(BrowserContext browserContext1, BrowserContext browserContext2) {
    assertEquals(browserContext1, browserContext2);
  }

  @Test
  void duplicatePageReturnTheSameObject(Page page1, Page page2) {
    assertEquals(page1, page2);
  }
}
