/**
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

package com.microsoft.playwright.tests.library;

import com.microsoft.playwright.Dialog;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.CompletableFuture;

import static com.microsoft.playwright.fixtures.Utils.kTargetClosedErrorMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/library/page-close.spec.ts. */
public class TestPageClose {
  @PlaywrightTest
  @DisplayName("PA-039 should close page with active dialog")
  void shouldClosePageWithActiveDialog(Page page) {
    page.evaluate("\"trigger builtins.setTimeout\"");
    page.setContent("<button onclick=\"builtins.setTimeout(() => alert(1))\">alert</button>");
    // Java: the dialog is awaited with a handler that completes a future.
    CompletableFuture<Dialog> dialog = new CompletableFuture<>();
    page.onDialog(dialog::complete);
    // Java: void page.click('button').catch(() => {}) runs inline, the alert comes from a timeout.
    try {
      page.click("button");
    } catch (PlaywrightException e) {
    }
    page.waitForCondition(dialog::isDone);
    page.close();
  }

  // PA-040 expect should not print timed out error message when page closes: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("PA-041 addLocatorHandler should throw when page closes")
  void addLocatorHandlerShouldThrowWhenPageCloses(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/handle-locator.html");

    page.addLocatorHandler(page.getByText("This interstitial covers the button"), locator -> {
      page.close(new Page.CloseOptions().setReason("custom reason"));
    });

    page.locator("#aside").hover();
    page.evaluate("() => {\n" +
      "  window.clicked = 0;\n" +
      "  window.setupAnnoyingInterstitial('mouseover', 1);\n" +
      "}");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.locator("#target").click());
    assertTrue(error.getMessage().contains("custom reason"));
  }

  // PA-042 should reject all promises when page is closed: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("PA-043 should set the page close state")
  void shouldSetThePageCloseState(Page page) {
    assertEquals(false, page.isClosed());
    page.close();
    assertEquals(true, page.isClosed());
  }

  @PlaywrightTest
  @DisplayName("PA-044 should pass page to close event")
  void shouldPassPageToCloseEvent(Page page) {
    Page closedPage = page.waitForClose(() -> page.close());
    assertSame(page, closedPage);
  }

  @PlaywrightTest
  @DisplayName("PA-045 should terminate network waiters")
  void shouldTerminateNetworkWaiters(Page page, TestServer server) {
    PlaywrightException[] results = new PlaywrightException[2];
    // Java: the waiters are nested callbacks, the last one closes the page.
    try {
      page.waitForRequest(server.EMPTY_PAGE, () -> {
        try {
          page.waitForResponse(server.EMPTY_PAGE, () -> {
            page.close();
          });
        } catch (PlaywrightException e) {
          results[1] = e;
        }
      });
    } catch (PlaywrightException e) {
      results[0] = e;
    }
    for (int i = 0; i < 2; i++) {
      assertNotNull(results[i]);
      String message = results[i].getMessage();
      assertTrue(message.contains(kTargetClosedErrorMessage));
      assertFalse(message.contains("Timeout"));
    }
  }

  // PA-046 should be callable twice: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("PA-047 should return null if parent page has been closed")
  void shouldReturnNullIfParentPageHasBeenClosed(Page page) {
    Page popup = page.waitForPopup(() -> {
      page.evaluate("() => window.open('about:blank')");
    });
    page.close();
    Page opener = popup.opener();
    assertNull(opener);
  }

  @PlaywrightTest
  @DisplayName("PA-048 should fail with error upon disconnect")
  void shouldFailWithErrorUponDisconnect(Page page) {
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.waitForDownload(() -> page.close()));
    assertTrue(error.getMessage().contains(kTargetClosedErrorMessage));
  }

  @PlaywrightTest
  @DisplayName("PA-049 page.close should work with window.close")
  void pageCloseShouldWorkWithWindowClose(Page page) {
    CompletableFuture<Page> closedPromise = new CompletableFuture<>();
    page.onClose(closedPromise::complete);
    page.close();
    page.waitForCondition(closedPromise::isDone);
  }

  // PA-050 should not throw UnhandledPromiseRejection when page closes: not ported, see upstream_not_ported.txt.

  // PA-051 interrupt request.response() and request.allHeaders() on page.close: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("PA-052 should not treat navigations as new popups")
  void shouldNotTreatNavigationsAsNewPopups(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a target=_blank rel=noopener href=\"/one-style.html\">yo</a>");
    Page popup = page.waitForPopup(() -> {
      page.click("a");
    });
    boolean[] badSecondPopup = {false};
    page.onPopup(p -> badSecondPopup[0] = true);
    popup.navigate(server.CROSS_PROCESS_PREFIX + "/empty.html");
    page.close();
    assertFalse(badSecondPopup[0]);
  }

  @PlaywrightTest
  @DisplayName("PA-053 should not result in unhandled rejection")
  void shouldNotResultInUnhandledRejection(Page page) {
    page.waitForClose(() -> {
      page.exposeFunction("foo", args -> {
        page.close();
        return null;
      });
      page.evaluate("() => {\n" +
        "  window.builtins.setTimeout(() => window.foo(), 0);\n" +
        "  return undefined;\n" +
        "}");
    });
    // Make a round-trip to be sure we did not throw immediately after closing.
    assertThrows(PlaywrightException.class, () -> page.evaluate("1 + 1"));
  }

  // PA-054 should reject response.finished if page closes: not ported, see upstream_not_ported.txt.

  // PA-055 should not throw when continuing while page is closing: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("PA-056 should not throw when continuing after page is closed")
  void shouldNotThrowWhenContinuingAfterPageIsClosed(Page page, TestServer server) {
    page.route("**/*", route -> {
      page.close();
      // Should not throw.
      route.resume();
    });
    PlaywrightException error = null;
    try {
      page.navigate(server.EMPTY_PAGE);
    } catch (PlaywrightException e) {
      error = e;
    }
    assertNotNull(error);
  }
}
