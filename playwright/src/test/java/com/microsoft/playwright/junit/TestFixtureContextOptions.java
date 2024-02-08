package com.microsoft.playwright.junit;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.junit.ServerLifecycle.serverMap;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@FixtureTest
@UsePlaywright(TestFixtureContextOptions.CustomOptions.class)
public class TestFixtureContextOptions {

  public static class CustomOptions implements OptionsFactory {
    @Override
    public Options getOptions() {
      return new Options()
        .setApiRequestOptions(new APIRequest.NewContextOptions()
          .setBaseURL(serverMap.get(TestFixtureContextOptions.class).EMPTY_PAGE))
        .setContextOption(new Browser.NewContextOptions()
          .setBaseURL(serverMap.get(TestFixtureContextOptions.class).EMPTY_PAGE));
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
