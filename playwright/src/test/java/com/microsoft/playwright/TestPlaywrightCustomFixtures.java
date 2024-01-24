package com.microsoft.playwright;

import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;
import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.ServerLifecycle.serverMap;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@FixtureTest
@UsePlaywright(TestPlaywrightCustomFixtures.CustomOptions.class)
public class TestPlaywrightCustomFixtures {

  public static class CustomOptions implements OptionsFactory {
    @Override
    public Options getOptions() {
      return new Options().setBaseUrl(serverMap.get(TestPlaywrightCustomFixtures.class).EMPTY_PAGE).setBrowserName("firefox");
    }
  }

  @Test
  public void testCustomBrowser(Browser browser) {
    assertEquals(browser.browserType().name(), "firefox");
  }

  @Test
  public void testBaseUrl(Page page) {
    page.navigate("/");
    assertThat(page).hasURL(Pattern.compile("localhost"));
  }
}
