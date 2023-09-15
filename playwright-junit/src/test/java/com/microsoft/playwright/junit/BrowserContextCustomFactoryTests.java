package com.microsoft.playwright.junit;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@UsePlaywright(browserContextFactory = CustomBrowserContextFactory.class)
public class BrowserContextCustomFactoryTests {
  private static BrowserContext browserContextFromBeforeAll;
  private BrowserContext browserContextFromBeforeEach;

  @BeforeAll
  public static void beforeAll(BrowserContext browserContext) {
    assert browserContext != null;
    Page page = browserContext.newPage();
    page.navigate("/");
    assertThat(page).hasURL(Pattern.compile("bing.com"));
    page.close();
    browserContextFromBeforeAll = browserContext;
  }

  @BeforeEach
  public void beforeEach(BrowserContext browserContext) {
    assert browserContext != null;
    browserContextFromBeforeEach = browserContext;
    assertNotEquals(browserContext, browserContextFromBeforeAll);
  }

  @Test
  public void test1(BrowserContext browserContext) {
    assert browserContext != null;
    assertEquals(browserContext, browserContextFromBeforeEach);
  }
}
