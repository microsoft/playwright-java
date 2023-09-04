package com.microsoft.playwright;

public interface BrowserFactory {
  Browser newBrowser(Playwright playwright);
}
