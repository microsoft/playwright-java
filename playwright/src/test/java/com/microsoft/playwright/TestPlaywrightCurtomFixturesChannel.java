package com.microsoft.playwright;

import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;
import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import static com.microsoft.playwright.ServerLifecycle.serverMap;
import static org.junit.jupiter.api.Assertions.*;

@FixtureTest
@UsePlaywright(TestPlaywrightCurtomFixturesChannel.CustomOptions.class)
@EnabledIf("isChannelSpecified")
public class TestPlaywrightCurtomFixturesChannel {

  public static class CustomOptions implements OptionsFactory {
    @Override
    public Options getOptions() {
      return new Options().setChannel(channel());
    }
  }

  public static boolean isChannelSpecified() {
    return channel() != null;
  }

  public static String channel() {
    return System.getenv("BROWSER_CHANNEL");
  }

  private Server server() {
    return serverMap.get(this.getClass());
  }

  @Test
  public void testBrowserChannel(Browser browser, Page page) {
    assertEquals(browser.browserType().name(), "chromium");
    page.navigate(server().EMPTY_PAGE);
    String brands = (String) page.evaluate("navigator.userAgentData?.brands.map(b => b.brand).join(', ') || ''");
    if (channel().contains("chrome")) {
      assertTrue(brands.contains("Google Chrome") || brands.contains("HeadlessChrome"), brands);
    } else if (channel().contains("msedge")) {
      assertTrue(brands.contains("Microsoft Edge") || brands.contains("HeadlessEdg"), brands);
    } else {
      fail("Unknown channel: " + channel());
    }
  }
}
