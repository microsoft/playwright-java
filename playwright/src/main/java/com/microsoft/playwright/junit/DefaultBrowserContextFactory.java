package com.microsoft.playwright.junit;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;

public class DefaultBrowserContextFactory implements BrowserContextFactory {
  @Override
  public BrowserContext newBrowserContext(Browser browser) {
    return browser.newContext();
  }
}
