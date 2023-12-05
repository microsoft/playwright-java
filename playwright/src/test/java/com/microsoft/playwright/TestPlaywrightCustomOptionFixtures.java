package com.microsoft.playwright;

import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UsePlaywright(options = TestPlaywrightCustomOptionFixtures.CustomOptions.class)
public class TestPlaywrightCustomOptionFixtures {
  public static class CustomOptions extends Options {
    @Override
    public Browser.NewContextOptions getContextOption() {
      return new Browser.NewContextOptions().setBaseURL("https://microsoft.com");
    }

    @Override
    public APIRequest.NewContextOptions getApiRequestOptions() {
      return new APIRequest.NewContextOptions().setBaseURL("https://bing.com");
    }

    @Override
    public String getChannel() {
      return "chrome";
    }
  }

  @Test
  void testCustomBrowserContext(Page page) {
    page.navigate("/");
    assertThat(page).hasURL(Pattern.compile("microsoft"));
  }

  @Test
  void testCustomAPIRequestOptions(APIRequestContext apiRequestContext) {
    APIResponse response = apiRequestContext.get("/");
    assertTrue(response.url().contains("bing"));
  }
}
