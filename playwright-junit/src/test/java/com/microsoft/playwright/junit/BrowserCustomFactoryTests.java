package com.microsoft.playwright.junit;

import com.microsoft.playwright.Browser;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UseBrowserFactory(CustomBrowserFactory.class)
public class BrowserCustomFactoryTests {
  private Browser browserFromBeforeEach;
  private static Browser browserFromBeforeAll;

  @BeforeAll
  static void beforeAll(Browser browser) {
    assert browser != null;
    System.out.println("BeforeAll " + browser);
    browserFromBeforeAll = browser;
    assertEquals("firefox", browser.browserType().name());
  }

  @AfterAll
  static void afterAll(Browser browser) {
    assert browser != null;
    System.out.println("AfterAll " + browser);
    assertEquals(browserFromBeforeAll, browser, "Static Browser is not equal to others that were created");
  }

  @BeforeEach
  void beforeEach(Browser browser) {
    assert browser != null;
    System.out.println("BeforeEach " + browser);
    browserFromBeforeEach = browser;
  }

  @AfterEach
  void afterEach(Browser browser) {
    assert browser != null;
    System.out.println("AfterEach " + browser);
  }


  @Test
  void test1(Browser browser) {
    assert browser != null;
    System.out.println("Test1" + browser);
    assertEquals(browserFromBeforeEach, browser, "Browser parameter is not equal to others that were created");
    assertEquals(browser, browserFromBeforeAll, "Browser parameter is not equal to static Browser from Before/AfterAll hooks");
  }

  @Test
  void test2(Browser browser) {
    assert browser != null;
    System.out.println("Test2" + browser);
    assertEquals(browserFromBeforeEach, browser, "Browser parameter is not equal to others that were created");
    assertEquals(browser, browserFromBeforeAll, "Browser parameter is not equal to static Browser from Before/AfterAll hooks");
  }
}
