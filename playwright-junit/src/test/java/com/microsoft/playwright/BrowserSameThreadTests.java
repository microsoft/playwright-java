package com.microsoft.playwright;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.MethodName.class) // this is here to customBrowserTest1 to run before customBrowserTest2
@Execution(ExecutionMode.SAME_THREAD)
@UseBrowserFactory
public class BrowserSameThreadTests {
  private Browser browserFromBeforeEach;
  private static Browser browserFromBeforeAll;

  // this is static because in order to test that the custom browser created in the customBrowserTest1
  // is the same as the browser being used in customBrowserTest2
  // JUnit creates a new instance of a test class for each test so we need this static to test that the same browser
  // is being used.
  private static Browser customBrowser;

  @BeforeAll
  static void beforeAll(Browser browser) {
    assert browser != null;
    System.out.println("BeforeAll " + browser);
    browserFromBeforeAll = browser;
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

  @UseBrowserFactory(CustomBrowserFactory.class)
  @Test
  void customBrowserTest1(Browser browser) {
    assert browser != null;
    assertEquals("firefox", browser.getName());
    customBrowser = browser;
  }

  @UseBrowserFactory(CustomBrowserFactory.class)
  @Test
  void customBrowserTest2(Browser browser) {
    assert browser != null;
    assertEquals("firefox", browser.getName());
    assertEquals(customBrowser, browser, "Custom browser is not the same as the one created in the first test that uses this browser");
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
