package com.microsoft.playwright;

// This default factory was created to simplify the API for users of this extension.
// Without this, users that want to use the default configuration would have to register the extension with
// @ExtendWith(PlaywrightExtension.class)
// And users that want to use a custom browser would need to use
// @UseBrowserFactory(SomeCustomBrowserFactory.class)
class DefaultBrowserFactory implements BrowserFactory {
  @Override
  public Browser newBrowser(Playwright playwright) {
    return playwright.chromium().launch();
  }
}
