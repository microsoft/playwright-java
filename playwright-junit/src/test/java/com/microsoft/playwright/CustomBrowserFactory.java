package com.microsoft.playwright;

public class CustomBrowserFactory implements BrowserFactory {
  @Override
  public Browser newBrowser(Playwright playwright) {
    return playwright.firefox().launch();
  }
}
