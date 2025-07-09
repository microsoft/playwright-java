/*
 * Copyright (c) Microsoft Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright;

import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class TestPageAddLocatorHandler extends TestBase {
  @Test
  void shouldWork() {
    page.navigate(server.PREFIX + "/input/handle-locator.html");

    int[] beforeCount = {0};
    int[] afterCount = {0};

    Locator originalLocator = page.getByText("This interstitial covers the button");
    page.addLocatorHandler(originalLocator, locator -> {
      assertEquals(originalLocator, locator);
      ++beforeCount[0];
      page.locator("#close").click();
      ++afterCount[0];
    }, new Page.AddLocatorHandlerOptions().setNoWaitAfter(true));

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

    page.addLocatorHandler(page.locator("body"), locator -> {
      if (page.getByText("This interstitial covers the button").isVisible())
        page.locator("#close").click();
    }, new Page.AddLocatorHandlerOptions().setNoWaitAfter(true));

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

    page.addLocatorHandler(page.getByText("This interstitial covers the button"), locator -> {
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

    page.addLocatorHandler(page.getByText("This interstitial covers the button"), locator -> {
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

    page.addLocatorHandler(page.getByText("This interstitial covers the button"), locator -> {
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
    page.addLocatorHandler(page.getByText("This interstitial covers the button"), locator -> {
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

  @Test
  public void shouldWorkWhenOwnerFrameDetaches() {
    Page page = browser.newPage();
    page.navigate(server.EMPTY_PAGE);

    page.evaluate("() => {\n" +
      "    const iframe = document.createElement('iframe');\n" +
      "    iframe.src = 'data:text/html,<body>hello from iframe</body>';\n" +
      "    document.body.append(iframe);\n" +
      "\n" +
      "    const target = document.createElement('button');\n" +
      "    target.textContent = 'Click me';\n" +
      "    target.id = 'target';\n" +
      "    target.addEventListener('click', () => window._clicked = true);\n" +
      "    document.body.appendChild(target);\n" +
      "\n" +
      "    const closeButton = document.createElement('button');\n" +
      "    closeButton.textContent = 'close';\n" +
      "    closeButton.id = 'close';\n" +
      "    closeButton.addEventListener('click', () => iframe.remove());\n" +
      "    document.body.appendChild(closeButton);\n" +
      "  }");

    page.addLocatorHandler(page.frameLocator("iframe").locator("body"), locator -> {
      page.locator("#close").click();
    });

    page.locator("#target").click();
    assertNull(page.querySelector("iframe"));
    assertTrue((Boolean) page.evaluate("window._clicked"));
  }

  @Test
  public void shouldWorkWithTimesOption() {
    Page page = browser.newPage();
    page.navigate(server.PREFIX + "/input/handle-locator.html");

    int[] called = {0};
    page.addLocatorHandler(page.locator("body"), locator -> {
      ++called[0];
    }, new Page.AddLocatorHandlerOptions().setNoWaitAfter(true).setTimes(2));

    page.locator("#aside").hover();
    page.evaluate("() => {\n" +
      "    window.clicked = 0;\n" +
      "    window.setupAnnoyingInterstitial('mouseover', 4);\n" +
      "  }");

    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.locator("#target").click(new Locator.ClickOptions().setTimeout(3_000)));
    assertEquals(2, called[0]);
    assertEquals(0, (int) page.evaluate("window.clicked"));
    assertTrue(page.locator("#interstitial").isVisible());
    assertTrue(e.getMessage().contains("Timeout 3000ms exceeded"), e.getMessage());
    assertTrue(e.getMessage().contains("<div>This interstitial covers the button</div> from <div class=\"visible\" id=\"interstitial\">…</div> subtree intercepts pointer events"), e.getMessage());
  }

  @Test
  public void shouldWaitForHiddenByDefault() {
    Page page = browser.newPage();
    page.navigate(server.PREFIX + "/input/handle-locator.html");

    int[] called = {0};
    page.addLocatorHandler(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("close")), button -> {
      ++called[0];
      button.click();
    });

    page.locator("#aside").hover();
    page.evaluate("() => {" +
      "window.clicked = 0;" +
      "window.setupAnnoyingInterstitial('timeout', 1);" +
      "}");

    page.locator("#target").click();
    assertEquals(1, (int) page.evaluate("window.clicked"));
    assertFalse(page.locator("#interstitial").isVisible());
    assertEquals(1, called[0]);
  }

  @Test
  public void shouldWaitForHiddenByDefault2() {
    Page page = browser.newPage();
    page.navigate(server.PREFIX + "/input/handle-locator.html");

    int[] called = {0};
    page.addLocatorHandler(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("close")), button -> {
      ++called[0];
    });

    page.locator("#aside").hover();
    page.evaluate("() => {" +
      "window.clicked = 0;" +
      "window.setupAnnoyingInterstitial('hide', 1);" +
      "}");

    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.locator("#target").click(new Locator.ClickOptions().setTimeout(3_000)));
    assertEquals(0, (int) page.evaluate("window.clicked"));
    assertTrue(page.locator("#interstitial").isVisible());
    assertEquals(1, called[0]);
    assertTrue(e.getMessage().contains("locator handler has finished, waiting for getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(\"close\")) to be hidden"), e.getMessage());
  }

  @Test
  public void shouldWorkWithNoWaitAfter() {
    Page page = browser.newPage();
    page.navigate(server.PREFIX + "/input/handle-locator.html");

    int[] called = {0};
    page.addLocatorHandler(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("close")), button -> {
      ++called[0];
      if (called[0] == 1) {
        button.click();
      } else {
        page.locator("#interstitial").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
      }
    }, new Page.AddLocatorHandlerOptions().setNoWaitAfter(true));

    page.locator("#aside").hover();
    page.evaluate("() => {" +
      "window.clicked = 0;" +
      "window.setupAnnoyingInterstitial('timeout', 1);" +
      "}");

    page.locator("#target").click();
    assertEquals(1, (int) page.evaluate("window.clicked"));
    assertThat(page.locator("#interstitial")).not().isVisible();
    assertEquals(2, called[0]);
  }

  @Test
  public void shouldRemoveLocatorHandler() {
    Page page = browser.newPage();
    page.navigate(server.PREFIX + "/input/handle-locator.html");

    int[] called = {0};
    page.addLocatorHandler(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("close")), button -> {
      ++called[0];
      button.click();
    });

    page.evaluate("() => {" +
      "window.clicked = 0;" +
      "window.setupAnnoyingInterstitial('hide', 1);" +
      "}");

    page.locator("#target").click();
    assertEquals(1, called[0]);
    assertEquals(1, (int) page.evaluate("window.clicked"));
    assertThat(page.locator("#interstitial")).not().isVisible();

    page.evaluate("() => {" +
      "window.clicked = 0;" +
      "window.setupAnnoyingInterstitial('hide', 1);" +
      "}");

    page.removeLocatorHandler(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("close")));

    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.locator("#target").click(new Locator.ClickOptions().setTimeout(3_000)));
    assertEquals(1, called[0]);
    assertEquals(0, (int) page.evaluate("window.clicked"));
    assertThat(page.locator("#interstitial")).isVisible();
    assertTrue(e.getMessage().contains("Timeout 3000ms exceeded"));
  }
}
