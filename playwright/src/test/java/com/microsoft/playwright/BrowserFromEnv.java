package com.microsoft.playwright;

import com.microsoft.playwright.junit.BrowserFactory;

import static com.microsoft.playwright.Utils.getBrowserChannelFromEnv;
import static com.microsoft.playwright.Utils.getBrowserNameFromEnv;

public class BrowserFromEnv implements BrowserFactory {
  static final boolean headful;

  static {
    String headfulEnv = System.getenv("HEADFUL");
    headful = headfulEnv != null && !"0".equals(headfulEnv) && !"false".equals(headfulEnv);
  }

  @Override
  public Browser newBrowser(Playwright playwright) {
    BrowserType.LaunchOptions launchOptions = createLaunchOptions();

    switch (getBrowserNameFromEnv()) {
      case "webkit":
        return playwright.webkit().launch(launchOptions);
      case "firefox":
        return playwright.firefox().launch(launchOptions);
      default:
        return playwright.chromium().launch(launchOptions);
    }
  }

  static BrowserType.LaunchOptions createLaunchOptions() {
    return new BrowserType.LaunchOptions()
      .setHeadless(!headful);
  }
}
