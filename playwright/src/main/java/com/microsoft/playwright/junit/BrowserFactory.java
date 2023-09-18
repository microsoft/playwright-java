package com.microsoft.playwright.junit;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;

public interface BrowserFactory {
  Browser newBrowser(Playwright playwright);
}
