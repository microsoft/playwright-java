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

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/page/page-add-locator-handler.spec.ts. */
public class TestPageAddLocatorHandler {
  @PlaywrightTest
  @DisplayName("LH-001 should work")
  void shouldWork(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/handle-locator.html");

    int[] beforeCount = {0};
    int[] afterCount = {0};
    Locator originalLocator = page.getByText("This interstitial covers the button");
    page.addLocatorHandler(originalLocator, locatorArgument -> {
      assertEquals(originalLocator, locatorArgument);
      ++beforeCount[0];
      page.locator("#close").click();
      ++afterCount[0];
    });

    for (List<Object> args : asList(
      asList((Object) "mouseover", 1),
      asList((Object) "mouseover", 1, "capture"),
      asList((Object) "mouseover", 2),
      asList((Object) "mouseover", 2, "capture"),
      asList((Object) "pointerover", 1),
      asList((Object) "pointerover", 1, "capture"),
      asList((Object) "none", 1),
      asList((Object) "remove", 1),
      asList((Object) "hide", 1))) {
      // it.step(`${args[0]}${args[2] === 'capture' ? ' with capture' : ''} ${args[1]} times`)
      page.locator("#aside").hover();
      beforeCount[0] = 0;
      afterCount[0] = 0;
      page.evaluate("args => {\n" +
        "        window.clicked = 0;\n" +
        "        window.setupAnnoyingInterstitial(...args);\n" +
        "      }", args);
      assertEquals(0, beforeCount[0]);
      assertEquals(0, afterCount[0]);
      page.locator("#target").click();
      assertEquals(args.get(1), beforeCount[0]);
      assertEquals(args.get(1), afterCount[0]);
      assertEquals(1, page.evaluate("window.clicked"));
      assertThat(page.locator("#interstitial")).not().isVisible();
    }
  }

  @PlaywrightTest
  @DisplayName("LH-002 should work with a custom check")
  void shouldWorkWithACustomCheck(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/handle-locator.html");

    page.addLocatorHandler(page.locator("body"), locator -> {
      if (page.getByText("This interstitial covers the button").isVisible()) {
        page.locator("#close").click();
      }
    }, new Page.AddLocatorHandlerOptions().setNoWaitAfter(true));

    for (List<Object> args : asList(
      asList((Object) "mouseover", 2),
      asList((Object) "none", 1),
      asList((Object) "remove", 1),
      asList((Object) "hide", 1))) {
      // it.step(`${args[0]}${args[2] === 'capture' ? ' with capture' : ''} ${args[1]} times`)
      page.locator("#aside").hover();
      page.evaluate("args => {\n" +
        "        window.clicked = 0;\n" +
        "        window.setupAnnoyingInterstitial(...args);\n" +
        "      }", args);
      page.locator("#target").click();
      assertEquals(1, page.evaluate("window.clicked"));
      assertThat(page.locator("#interstitial")).not().isVisible();
    }
  }

  @PlaywrightTest
  @DisplayName("LH-003 should work with locator.hover()")
  void shouldWorkWithLocatorHover(Page page, TestServer server) {
    assumeFalse(Env.headed(), "Stray hovers in headed mode");

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
    assertEquals("rgb(255, 255, 0)", page.evalOnSelector("#target", "e => window.getComputedStyle(e).backgroundColor"));
  }

  @PlaywrightTest
  @DisplayName("LH-004 should not work with force:true")
  void shouldNotWorkWithForceTrue(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/handle-locator.html");

    page.addLocatorHandler(page.getByText("This interstitial covers the button"), locator -> {
      page.locator("#close").click();
    });

    page.locator("#aside").hover();
    page.evaluate("() => {\n" +
      "    window.setupAnnoyingInterstitial('none', 1);\n" +
      "  }");
    page.locator("#target").click(new Locator.ClickOptions().setForce(true).setTimeout(2000));
    assertEquals(true, page.locator("#interstitial").isVisible());
    assertNull(page.evaluate("window.clicked"));
  }

  @PlaywrightTest
  @DisplayName("LH-006 should work with toBeVisible")
  void shouldWorkWithToBeVisible(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/handle-locator.html");

    int[] called = {0};
    page.addLocatorHandler(page.getByText("This interstitial covers the button"), locator -> {
      ++called[0];
      page.locator("#close").click();
    });

    page.evaluate("() => {\n" +
      "    window.clicked = 0;\n" +
      "    window.setupAnnoyingInterstitial('remove', 1);\n" +
      "  }");
    assertThat(page.locator("#target")).isVisible();
    assertThat(page.locator("#interstitial")).not().isVisible();
    assertEquals(1, called[0]);
  }

  @PlaywrightTest
  @DisplayName("LH-007 should work with locator.waitFor")
  void shouldWorkWithLocatorWaitFor(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/handle-locator.html");

    int[] called = {0};
    page.addLocatorHandler(page.getByText("This interstitial covers the button"), locator -> {
      ++called[0];
      page.locator("#close").click();
    });

    page.evaluate("() => {\n" +
      "    window.clicked = 0;\n" +
      "    window.setupAnnoyingInterstitial('remove', 1);\n" +
      "  }");
    page.locator("#target").waitFor();
    assertThat(page.locator("#interstitial")).not().isVisible();
    assertEquals(1, called[0]);
  }

  @PlaywrightTest
  @DisplayName("LH-009 should work when owner frame detaches")
  void shouldWorkWhenOwnerFrameDetaches(Page page, TestServer server) {
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
    assertEquals(true, page.evaluate("window._clicked"));
  }

  @PlaywrightTest
  @DisplayName("LH-010 should work with times: option")
  void shouldWorkWithTimesOption(Page page, TestServer server) {
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
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.locator("#target").click(new Locator.ClickOptions().setTimeout(3000)));
    assertEquals(2, called[0]);
    assertEquals(0, page.evaluate("window.clicked"));
    assertThat(page.locator("#interstitial")).isVisible();
    assertTrue(error.getMessage().contains("Timeout 3000ms exceeded"));
    assertTrue(error.getMessage().contains("<div>This interstitial covers the button</div> from <div class=\"visible\" id=\"interstitial\">…</div> subtree intercepts pointer events"));
  }

  @PlaywrightTest
  @DisplayName("LH-011 should wait for hidden by default")
  void shouldWaitForHiddenByDefault(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/handle-locator.html");

    int[] called = {0};
    page.addLocatorHandler(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("close")), button -> {
      called[0]++;
      button.click();
    });

    page.locator("#aside").hover();
    page.evaluate("() => {\n" +
      "    window.clicked = 0;\n" +
      "    window.setupAnnoyingInterstitial('timeout', 1);\n" +
      "  }");
    page.locator("#target").click();
    assertEquals(1, page.evaluate("window.clicked"));
    assertThat(page.locator("#interstitial")).not().isVisible();
    assertEquals(1, called[0]);
  }

  @PlaywrightTest
  @DisplayName("LH-012 should wait for hidden by default 2")
  @Fixme("java: the call log names the locator as 'getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(\"close\"))', not \"getByRole('button', { name: 'close' })\"")
  void shouldWaitForHiddenByDefault2(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/handle-locator.html");

    int[] called = {0};
    page.addLocatorHandler(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("close")), button -> {
      called[0]++;
    });

    page.locator("#aside").hover();
    page.evaluate("() => {\n" +
      "    window.clicked = 0;\n" +
      "    window.setupAnnoyingInterstitial('hide', 1);\n" +
      "  }");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.locator("#target").click(new Locator.ClickOptions().setTimeout(3000)));
    assertEquals(0, page.evaluate("window.clicked"));
    assertEquals(true, page.locator("#interstitial").isVisible());
    assertEquals(1, called[0]);
    assertTrue(error.getMessage().contains("locator handler has finished, waiting for getByRole('button', { name: 'close' }) to be hidden"));
  }

  @PlaywrightTest
  @DisplayName("LH-013 should work with noWaitAfter")
  void shouldWorkWithNoWaitAfter(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/handle-locator.html");

    int[] called = {0};
    page.addLocatorHandler(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("close")), button -> {
      called[0]++;
      if (called[0] == 1) {
        button.click();
      } else {
        page.locator("#interstitial").waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.HIDDEN));
      }
    }, new Page.AddLocatorHandlerOptions().setNoWaitAfter(true));

    page.locator("#aside").hover();
    page.evaluate("() => {\n" +
      "    window.clicked = 0;\n" +
      "    window.setupAnnoyingInterstitial('timeout', 1);\n" +
      "  }");
    page.locator("#target").click();
    assertEquals(1, page.evaluate("window.clicked"));
    assertThat(page.locator("#interstitial")).not().isVisible();
    assertEquals(2, called[0]);
  }

  @PlaywrightTest
  @DisplayName("LH-014 should removeLocatorHandler")
  void shouldRemoveLocatorHandler(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/handle-locator.html");

    int[] called = {0};
    page.addLocatorHandler(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("close")), locator -> {
      ++called[0];
      locator.click();
    });

    page.evaluate("() => {\n" +
      "    window.clicked = 0;\n" +
      "    window.setupAnnoyingInterstitial('hide', 1);\n" +
      "  }");
    page.locator("#target").click();
    assertEquals(1, called[0]);
    assertEquals(1, page.evaluate("window.clicked"));
    assertThat(page.locator("#interstitial")).not().isVisible();

    page.evaluate("() => {\n" +
      "    window.clicked = 0;\n" +
      "    window.setupAnnoyingInterstitial('hide', 1);\n" +
      "  }");
    page.removeLocatorHandler(page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("close")));

    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.locator("#target").click(new Locator.ClickOptions().setTimeout(3000)));
    assertEquals(1, called[0]);
    assertEquals(0, page.evaluate("window.clicked"));
    assertThat(page.locator("#interstitial")).isVisible();
    assertTrue(error.getMessage().contains("Timeout 3000ms exceeded"));
  }
}
