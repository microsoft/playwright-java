package com.microsoft.playwright;

import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;

public class TestOptionsFactories {

  public static class BasicOptionsFactory implements OptionsFactory {
    @Override
    public Options getOptions() {
      return new Options().setBrowserName(getBrowserName());
    }
  }

  public static String getBrowserChannelFromEnv() {
    return System.getenv("BROWSER_CHANNEL");
  }

  public static BrowserType.LaunchOptions createLaunchOptions() {
    BrowserType.LaunchOptions options;
    options = new BrowserType.LaunchOptions();
    options.headless = !getHeadful();
    return options;
  }

  private static boolean getHeadful() {
    String headfulEnv = System.getenv("HEADFUL");
    return headfulEnv != null && !"0".equals(headfulEnv) && !"false".equals(headfulEnv);
  }

  private static String getBrowserName() {
    String browserName = System.getenv("BROWSER");
    if (browserName == null) {
      browserName = "chromium";
    }
    return browserName;
  }

  public static boolean isChromium() {
    return getBrowserName().equals("chromium");
  }
}
