package com.microsoft.playwright.junit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static com.microsoft.playwright.Utils.getBrowserTypeFromEnv;
import static org.junit.jupiter.api.Assertions.*;

@UsePlaywright(TestOptionsFactories.BasicOptionsFactory.class)
public class TestBrowser1 {

  @Test
  void shouldCreateNewPage(Browser browser) {
    Page page1 = browser.newPage();
    assertEquals(1, browser.contexts().size());

    Page page2 = browser.newPage();
    assertEquals(2, browser.contexts().size());

    page1.close();
    assertEquals(1, browser.contexts().size());

    page2.close();
    assertEquals(0, browser.contexts().size());
  }

  @Test
  void shouldThrowUponSecondCreateNewPage(Browser browser) {
    Page page = browser.newPage();
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.context().newPage());
    assertTrue(e.getMessage().contains("Please use browser.newContext()"));
    page.close();
  }

  @Test
  void versionShouldWork(Browser browser) {
    switch (browser.browserType().name()) {
      case "chromium":
        assertTrue(Pattern.matches("^\\d+\\.\\d+\\.\\d+\\.\\d+$", browser.version()));
        break;
      case "webkit":
        assertTrue(Pattern.matches("^\\d+\\.\\d+", browser.version()));
        break;
      case "firefox":
        // It can be 85.0b1 in Firefox.
        assertTrue(Pattern.matches("^\\d+\\.\\d+.*", browser.version()));
        break;
      default:
        fail("Unknown browser");
    }
  }

  @Test
  void shouldReturnBrowserType(Playwright playwright, Browser browser) {
    assertEquals(getBrowserTypeFromEnv(playwright), browser.browserType());
  }

  @Test
  @EnabledIf(value = "com.microsoft.playwright.junit.TestOptionsFactories#isChromium",
             disabledReason = "Chrome Devtools Protocol supported by chromium only")
  void shouldWorkWithNewBrowserCDPSession(Browser browser) {
    CDPSession session = browser.newBrowserCDPSession();

    JsonElement response = session.send("Browser.getVersion");
    assertNotNull(response.getAsJsonObject().get("userAgent").toString());

    AtomicReference<Boolean> gotEvent = new AtomicReference<>(false);

    session.on("Target.targetCreated", jsonElement -> {
      gotEvent.set(true);
    });

    JsonObject params = new JsonObject();
    params.addProperty("discover", true);
    session.send("Target.setDiscoverTargets", params);

    Page page = browser.newPage();
    assertTrue(gotEvent.get());
    page.close();

    session.detach();
  }

  @Test
  void shouldPropagateCloseReasonToPendingActions(Browser browser) {
    BrowserContext context = browser.newContext();
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> context.waitForPage(() -> {
      browser.close(new Browser.CloseOptions().setReason("The reason."));
    }));
    assertTrue(e.getMessage().contains("The reason."), e.getMessage());
  }

}
