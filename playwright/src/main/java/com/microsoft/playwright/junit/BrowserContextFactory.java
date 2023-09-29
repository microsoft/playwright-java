package com.microsoft.playwright.junit;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;

public interface BrowserContextFactory {
  BrowserContext newBrowserContext(Browser browser);
}
