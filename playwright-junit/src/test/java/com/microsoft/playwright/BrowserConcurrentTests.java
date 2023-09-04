package com.microsoft.playwright;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Execution(ExecutionMode.CONCURRENT)
@UseBrowserFactory
public class BrowserConcurrentTests {
  private Browser browserFromBeforeEach;

  @BeforeEach
  void beforeEach(Browser browser) {
    assert browser != null;
    browserFromBeforeEach = browser;
  }

  @AfterEach
  void afterEach(Browser browser) {
    assert browser != null;
    assertEquals(browserFromBeforeEach, browser, "Browser parameter is not equal to the one created in the hooks");
  }

  @UseBrowserFactory(CustomBrowserFactory.class)
  @Test
  void customBrowserTest(Browser browser) {
    assert browser != null;
    assertEquals("firefox", browser.getName());
  }

  @Test
  void test1(Browser browser) {
    assert browser != null;
    assertEquals(browser.getName(), "chromium");
    assertEquals(browserFromBeforeEach, browser, "Browser parameter is not equal to the one created in the hooks");
  }

  @Test
  void test2(Browser browser) {
    assert browser != null;
    assertEquals(browser.getName(), "chromium");
    assertEquals(browserFromBeforeEach, browser, "Browser parameter is not equal to the one created in the hooks");
  }
}
