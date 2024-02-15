package com.microsoft.playwright;

import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class TestPageAddLocatorHandler extends TestBase {
  @Test
  void shouldWork() {
    page.navigate(server.PREFIX + "/input/handle-locator.html");

    int[] beforeCount = {0};
    int[] afterCount = {0};

    page.addLocatorHandler(page.getByText("This interstitial covers the button"), () -> {
      ++beforeCount[0];
      page.locator("#close").click();
      ++afterCount[0];
    });

    String[][] argsList = {
      {"mouseover", "1"},
      {"mouseover", "1", "capture"},
      {"mouseover", "2"},
      {"mouseover", "2", "capture"},
      {"pointerover", "1"},
      {"pointerover", "1", "capture"},
      {"none", "1"},
      {"remove", "1"},
      {"hide", "1"},
    };

    for (String[] args : argsList) {
      page.locator("#aside").hover();
      beforeCount[0] = 0;
      afterCount[0] = 0;

      page.evaluate("(args) => {\n" +
        "  window.clicked = 0;\n" +
        "  window.setupAnnoyingInterstitial(...args);\n" +
        "}", args);

      assertEquals(0, beforeCount[0]);
      assertEquals(0, afterCount[0]);

      page.locator("#target").click();

      assertEquals(Integer.parseInt(args[1]), beforeCount[0]);
      assertEquals(Integer.parseInt(args[1]), afterCount[0]);
      assertEquals(1, page.evaluate("window.clicked"));
      assertThat(page.locator("#interstitial")).not().isVisible();
    }
  }

  @Test
  void shouldWorkWithCustomCheck() {
    page.navigate(server.PREFIX + "/input/handle-locator.html");

    page.addLocatorHandler(page.locator("body"), () -> {
      if (page.getByText("This interstitial covers the button").isVisible())
        page.locator("#close").click();
    });

    String[][] argsList = {
      {"mouseover", "2"},
      {"none", "1"},
      {"remove", "1"},
      {"hide", "1"},
    };

    for (String[] args : argsList) {
      page.hover("#aside");
      page.evaluate("(args) => {\n" +
        "  window.clicked = 0;\n" +
        "  window.setupAnnoyingInterstitial(...args);\n" +
        "}", args);

      page.locator("#target").click();

      assertEquals(1, page.evaluate("window.clicked"));
      assertThat(page.locator("#interstitial")).not().isVisible();
    }
  }

  @Test
  void shouldWorkWithLocatorHover() {
    page.navigate(server.PREFIX + "/input/handle-locator.html");

    page.addLocatorHandler(page.getByText("This interstitial covers the button"), () -> {
      page.locator("#close").click();
    });

    page.locator("#aside").hover();
    page.evaluate("() => {\n" +
      "    window.setupAnnoyingInterstitial('pointerover', 1, 'capture');\n" +
      "  }");
    page.locator("#target").hover();
    assertThat(page.locator("#interstitial")).not().isVisible();
    assertEquals("rgb(255, 255, 0)", page.evalOnSelector("#target", "element => getComputedStyle(element).backgroundColor"));
  }

  @Test
  void shouldNotWorkWithForceTrue() {
    page.navigate(server.PREFIX + "/input/handle-locator.html");

    page.addLocatorHandler(page.getByText("This interstitial covers the button"), () -> {
      page.locator("#close").click();
    });

    page.locator("#aside").hover();
    page.evaluate("() => {\n" +
      "    window.setupAnnoyingInterstitial('none', 1);\n" +
      "  }");

    page.locator("#target").click(new Locator.ClickOptions().setForce(true).setTimeout(2000));
    assertTrue(page.locator("#interstitial").isVisible());
    assertNull(page.evaluate("window.clicked"));
  }

  @Test
  void shouldThrowWhenPageCloses() {
    page.navigate(server.PREFIX + "/input/handle-locator.html");

    page.addLocatorHandler(page.getByText("This interstitial covers the button"), () -> {
      page.close();
    });

    page.locator("#aside").hover();
    page.evaluate("() => {\n" +
      "    window.clicked = 0;\n" +
      "    window.setupAnnoyingInterstitial('mouseover', 1);\n" +
      "  }");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.locator("#target").click());
    assertTrue(e.getMessage().contains("Target page, context or browser has been closed"), e.getMessage());
  }

  @Test
  void shouldWorkWithToBeVisible() {
    page.navigate(server.PREFIX + "/input/handle-locator.html");

    int[] called = {0};
    page.addLocatorHandler(page.getByText("This interstitial covers the button"), () -> {
      ++called[0];
      page.locator("#close").click();
    });

    page.evaluate("() => {\n" +
      "  window.clicked = 0;\n" +
      "  window.setupAnnoyingInterstitial('remove', 1);\n" +
      "}");
    assertThat(page.locator("#target")).isVisible();
    assertThat(page.locator("#interstitial")).not().isVisible();
    assertEquals(1, called[0]);
 }
}
