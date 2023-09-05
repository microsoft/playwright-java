package com.microsoft.playwright.junit;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;

// This default factory was created to simplify the API for users of this extension.
// Without this, users that want to use the default configuration would have to register the extensions with
// @ExtendWith({PlaywrightExtension.class, BrowserExtension.class, PlaywrightCleanupExtension.class})
// And users that want to use a custom browser would need to use
// @UseBrowserFactory(SomeCustomBrowserFactory.class)
class DefaultBrowserFactory implements BrowserFactory {
  @Override
  public Browser newBrowser(Playwright playwright) {
    return playwright.chromium().launch();
  }
}
