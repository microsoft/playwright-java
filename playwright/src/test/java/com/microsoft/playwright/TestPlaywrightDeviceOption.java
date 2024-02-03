package com.microsoft.playwright;

import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;
import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.ServerLifecycle.serverMap;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@FixtureTest
@UsePlaywright(TestPlaywrightDeviceOption.CustomOptions.class)
public class TestPlaywrightDeviceOption {

  public static class CustomOptions implements OptionsFactory {
    @Override
    public Options getOptions() {
      return new Options().setDeviceName("iPhone 14");
    }
  }

  private Server server() {
    return serverMap.get(this.getClass());
  }

  @Test
  public void testPredifinedDeviceParameters(Page page) {
    page.navigate(server().EMPTY_PAGE);
    assertEquals("webkit", page.context().browser().browserType().name());
    assertEquals(3, page.evaluate("window.devicePixelRatio"));
    assertEquals(980, page.evaluate("window.innerWidth"));
    assertEquals(1668, page.evaluate("window.innerHeight"));
  }
}
