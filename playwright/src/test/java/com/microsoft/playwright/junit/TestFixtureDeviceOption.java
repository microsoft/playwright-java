package com.microsoft.playwright.junit;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Server;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.junit.ServerLifecycle.serverMap;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@FixtureTest
@UsePlaywright(TestFixtureDeviceOption.CustomOptions.class)
public class TestFixtureDeviceOption {

  public static class CustomOptions implements OptionsFactory {
    @Override
    public Options getOptions() {
      return new Options().setDeviceName("iPhone 14");
    }
  }

  @Test
  public void testPredefinedDeviceParameters(Server server, Page page) {
    page.navigate(server.EMPTY_PAGE);
    assertEquals("webkit", page.context().browser().browserType().name());
    assertEquals(3, page.evaluate("window.devicePixelRatio"));
    assertEquals(980, page.evaluate("window.innerWidth"));
    assertEquals(1668, page.evaluate("window.innerHeight"));
  }
}
