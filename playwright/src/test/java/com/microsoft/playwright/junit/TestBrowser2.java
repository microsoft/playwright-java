package com.microsoft.playwright.junit;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.options.BrowserChannel;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.junit.TestOptionsFactories.ChannelOptionsFactory.getBrowserChannelEnumFromEnv;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@UsePlaywright(TestOptionsFactories.ChannelOptionsFactory.class)
public class TestBrowser2 {

  @Test
  void shouldSupportDeprecatedChannelEnum(Browser browser) {
    BrowserChannel channel = getBrowserChannelEnumFromEnv();
    Assumptions.assumeTrue(channel != null);
    assertNotNull(browser);
  }
}
