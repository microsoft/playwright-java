package com.microsoft.playwright.junit;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

public class DefaultBrowserFactory implements BrowserFactory {
  private final boolean headed;
  private final String browserEnv;

  public DefaultBrowserFactory() {
    headed = isHeaded();
    browserEnv = getBrowserNameFromEnv();
  }

  @Override
  public Browser newBrowser(Playwright playwright) {
    BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(!headed);

    switch (browserEnv) {
      case "webkit":
        return playwright.webkit().launch(launchOptions);
      case "firefox":
        return playwright.firefox().launch(launchOptions);
      default:
        return playwright.chromium().launch(launchOptions);
    }
  }

  private static String getBrowserNameFromEnv() {
    String browserName = System.getenv("BROWSER");
    if (browserName == null) {
      browserName = "chromium";
    }
    return browserName;
  }

  private static boolean isHeaded() {
    String headedEnv = System.getenv("HEADED");
    return headedEnv != null && !"0".equals(headedEnv) && !"false".equals(headedEnv);
  }
}
