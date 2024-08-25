package com.microsoft.playwright.junit;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.impl.junit.MultiBrowser;
import org.junit.jupiter.api.Test;

@UsePlaywright
public class MultiBrowserTest {


  @MultiBrowser
  public void allBrowsers(Browser browser) {
    // Expected 3 lines, one for each browser.
    logBrowser(browser);
  }


  @Test
  public void singleBrowser(Browser browser) {
    // Expected chrome instance to be same as in other test.
    logBrowser(browser);
  }

  private void logBrowser(Browser browser) {
    System.out.println(browser + " " + browser.browserType().name());
  }

}
