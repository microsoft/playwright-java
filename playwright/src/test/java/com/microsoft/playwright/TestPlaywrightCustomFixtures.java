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
import static org.junit.jupiter.api.Assertions.assertEquals;

@UsePlaywright(TestPlaywrightCustomFixtures.CustomOptions.class)
public class TestPlaywrightCustomFixtures {
  private static Server server;

  public static class CustomOptions implements OptionsFactory {
    @Override
    public Options getOptions() {
      return new Options().setBaseUrl(server.EMPTY_PAGE).setBrowserName("firefox");
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
  public void testCustomBrowser(Browser browser) {
    assertEquals(browser.browserType().name(), "firefox");
  }

  @Test
  public void testBaseUrl(Page page) {
    page.navigate("/");
    assertThat(page).hasURL(Pattern.compile("localhost"));
  }
}
