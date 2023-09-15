package com.microsoft.playwright.junit;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;

public class CustomBrowserContextFactory implements BrowserContextFactory {
  @Override
  public BrowserContext newBrowserContext(Browser browser) {
    return browser.newContext(new Browser.NewContextOptions().setBaseURL("https://bing.com"));
  }
}
