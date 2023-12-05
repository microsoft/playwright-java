package com.microsoft.playwright;

import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@UsePlaywright(options = TestPlaywrightCustomFixtures.CustomOptions.class)
public class TestPlaywrightCustomFixtures {
  public static class CustomOptions extends Options {
    @Override
    public String getBaseUrl() {
      return "https://bing.com";
    }

    @Override
    public String getBrowserName() {
      return "firefox";
    }
  }

  @Test
  public void testCustomBrowser(Browser browser) {
    assertEquals(browser.browserType().name(), "firefox");
  }

  @Test
  public void testBaseUrl(Page page) {
    page.navigate("/");
    assertThat(page).hasURL(Pattern.compile("bing"));
  }
}
