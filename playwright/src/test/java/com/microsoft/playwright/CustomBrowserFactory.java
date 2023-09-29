package com.microsoft.playwright;

import com.microsoft.playwright.junit.BrowserFactory;

public class CustomBrowserFactory implements BrowserFactory {
  @Override
  public Browser newBrowser(Playwright playwright) {
    return playwright.firefox().launch();
  }
}
