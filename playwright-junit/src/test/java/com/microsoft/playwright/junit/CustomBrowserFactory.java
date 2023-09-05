package com.microsoft.playwright.junit;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.junit.BrowserFactory;

public class CustomBrowserFactory implements BrowserFactory {
  @Override
  public Browser newBrowser(Playwright playwright) {
    return playwright.firefox().launch();
  }
}
