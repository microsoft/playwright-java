package com.microsoft.playwright.junit;

import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.options.BrowserChannel;

public class TestOptionsFactories {
  private static String getBrowserChannelFromEnv() {
    return System.getenv("BROWSER_CHANNEL");
  }

  private static BrowserType.LaunchOptions createLaunchOptions() {
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

  public static class BasicOptionsFactory implements OptionsFactory {
    @Override
    public Options getOptions() {
      return new Options().setBrowserName(getBrowserName());
    }
  }

  public static class ChannelOptionsFactory implements OptionsFactory {
    @Override
    public Options getOptions() {
      BrowserChannel channel = getBrowserChannelEnumFromEnv();
      BrowserType.LaunchOptions launchOptions = createLaunchOptions();
      launchOptions.channel = channel;
      return new Options().setLaunchOptions(launchOptions);
    }

    public static BrowserChannel getBrowserChannelEnumFromEnv() {
      String channel = getBrowserChannelFromEnv();
      if (channel == null) {
        return null;
      }
      switch (channel) {
        case "chrome":
          return BrowserChannel.CHROME;
        case "chrome-beta":
          return BrowserChannel.CHROME_BETA;
        case "chrome-dev":
          return BrowserChannel.CHROME_DEV;
        case "chrome-canary":
          return BrowserChannel.CHROME_CANARY;
        case "msedge":
          return BrowserChannel.MSEDGE;
        case "msedge-beta":
          return BrowserChannel.MSEDGE_BETA;
        case "msedge-dev":
          return BrowserChannel.MSEDGE_DEV;
        case "msedge-canary":
          return BrowserChannel.MSEDGE_CANARY;
        default:
          throw new IllegalArgumentException("Unknown BROWSER_CHANNEL " + channel);
      }
    }
  }

}
