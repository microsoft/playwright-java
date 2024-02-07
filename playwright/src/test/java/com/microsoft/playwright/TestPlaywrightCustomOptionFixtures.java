package com.microsoft.playwright;

import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;
import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.ServerLifecycle.serverMap;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@FixtureTest
@UsePlaywright(TestPlaywrightCustomOptionFixtures.CustomOptions.class)
public class TestPlaywrightCustomOptionFixtures {
  public static class CustomOptions implements OptionsFactory {
    @Override
    public Options getOptions() {
      return new Options().setApiRequestOptions(new APIRequest.NewContextOptions().setBaseURL(serverMap.get(TestPlaywrightCustomOptionFixtures.class).EMPTY_PAGE)).setContextOption(new Browser.NewContextOptions().setBaseURL(serverMap.get(TestPlaywrightCustomOptionFixtures.class).EMPTY_PAGE));
    }
  }

  @Test
  void testCustomBrowserContext(Page page) {
    page.navigate("/");
    assertThat(page).hasURL(Pattern.compile("localhost"));
  }

  @Test
  void testCustomAPIRequestOptions(APIRequestContext apiRequestContext) {
    APIResponse response = apiRequestContext.get("/");
    assertTrue(response.url().contains("localhost"));
  }
}
