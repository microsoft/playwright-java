package com.microsoft.playwright.junit;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;

class DefaultBrowserContextFactory implements BrowserContextFactory {
  @Override
  public BrowserContext newBrowserContext(Browser browser) {
    return browser.newContext();
  }
}
