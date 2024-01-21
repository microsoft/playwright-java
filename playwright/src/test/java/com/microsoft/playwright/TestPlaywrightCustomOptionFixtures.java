package com.microsoft.playwright;

import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.OptionsFactory;
import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.regex.Pattern;

import static com.microsoft.playwright.Utils.nextFreePort;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UsePlaywright(TestPlaywrightCustomOptionFixtures.CustomOptions.class)
public class TestPlaywrightCustomOptionFixtures {
  private static Server server;

  public static class CustomOptions implements OptionsFactory {
    @Override
    public Options getOptions() {
      return new Options().setChannel("chrome").setApiRequestOptions(new APIRequest.NewContextOptions().setBaseURL(server.EMPTY_PAGE)).setContextOption(new Browser.NewContextOptions().setBaseURL(server.EMPTY_PAGE));
    }
  }

  @BeforeAll
  static void beforeAll() throws IOException {
    server = Server.createHttp(nextFreePort());
  }

  @AfterAll
  static void afterAll() {
    if (server != null) {
      server.stop();
      server = null;
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
