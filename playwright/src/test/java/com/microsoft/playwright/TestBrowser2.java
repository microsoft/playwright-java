package com.microsoft.playwright;

import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;
import com.microsoft.playwright.junit.UsePlaywright;
import com.microsoft.playwright.options.BrowserChannel;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.TestBrowser2.ChannelOptionsFactory.getBrowserChannelEnumFromEnv;
import static com.microsoft.playwright.TestOptionsFactories.createLaunchOptions;
import static com.microsoft.playwright.TestOptionsFactories.getBrowserChannelFromEnv;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@UsePlaywright(TestBrowser2.ChannelOptionsFactory.class)
public class TestBrowser2 {

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

  @Test
  void shouldSupportDeprecatedChannelEnum(Playwright playwright) {
    BrowserChannel channel = getBrowserChannelEnumFromEnv();
    Assumptions.assumeTrue(channel != null);
    BrowserType.LaunchOptions options = createLaunchOptions();
    options.setChannel(channel);
    BrowserType browserType = Utils.getBrowserTypeFromEnv(playwright);
    Browser browser = browserType.launch(options);
    assertNotNull(browser);
    browser.close();
  }
}
