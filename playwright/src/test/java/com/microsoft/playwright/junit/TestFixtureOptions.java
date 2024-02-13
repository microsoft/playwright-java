package com.microsoft.playwright.junit;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.junit.ServerLifecycle.serverMap;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

@FixtureTest
@UsePlaywright(TestFixtureOptions.CustomOptions.class)
public class TestFixtureOptions {

  public static class CustomOptions implements OptionsFactory {
    @Override
    public Options getOptions() {
      return new Options()
        .setBaseUrl(serverMap.get(TestFixtureOptions.class).EMPTY_PAGE)
        .setBrowserName("webkit")
        .setTestIdAttribute("data-my-custom-testid");
    }
  }

  @Test
  public void testCustomBrowser(Browser browser) {
    assertEquals(browser.browserType().name(), "webkit");
  }

  @Test
  public void testBaseUrl(Page page) {
    page.navigate("/");
    assertThat(page).hasURL(Pattern.compile("localhost"));
  }

  @Test
  void testCustomTestId(Page page) {
    page.setContent("<div><div data-my-custom-testid='Hello'>Hello world</div></div>");
    assertThat(page.getByTestId("Hello")).hasText("Hello world");
    assertThat(page.mainFrame().getByTestId("Hello")).hasText("Hello world");
    assertThat(page.locator("div").getByTestId("Hello")).hasText("Hello world");
  }
}
