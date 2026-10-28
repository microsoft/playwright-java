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

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Screencast;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.fixtures.ImageComparator;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Port of tests/library/screencast-overlay.spec.ts. */
public class TestScreencastOverlay {
  private static List<Integer> pixel(byte[] screenshot, int x, int y) {
    int[] rgba = ImageComparator.decode(screenshot).pixel(x, y);
    return Arrays.asList(rgba[0], rgba[1], rgba[2]);
  }

  // Java: expect.poll(() => value).toEqual(expected); waitForCondition() dispatches the events while waiting.
  private static <T> void expectPollToEqual(Page page, Supplier<T> value, T expected) {
    try {
      page.waitForCondition(() -> expected.equals(value.get()), new Page.WaitForConditionOptions().setTimeout(5000));
    } catch (TimeoutError e) {
      // The assertion below reports the last value.
    }
    assertEquals(expected, value.get());
  }

  @PlaywrightTest
  @DisplayName("SC-028 should add and remove overlay")
  void shouldAddAndRemoveOverlay(Browser browser, TestServer server) throws Exception {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);

    AutoCloseable disposable = page.screencast().showOverlay("<div id=\"my-overlay\">Hello Overlay</div>");
    assertThat(page.locator("x-pw-user-overlays")).isVisible();
    assertThat(page.locator(".x-pw-user-overlay")).hasCount(1);
    assertThat(page.locator("#my-overlay")).hasText("Hello Overlay");

    disposable.close();
    assertThat(page.locator(".x-pw-user-overlay")).hasCount(0);

    context.close();
  }

  @PlaywrightTest
  @DisplayName("SC-029 should add multiple overlays")
  void shouldAddMultipleOverlays(Browser browser, TestServer server) throws Exception {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);

    AutoCloseable d1 = page.screencast().showOverlay("<div id=\"overlay-1\">First</div>");
    AutoCloseable d2 = page.screencast().showOverlay("<div id=\"overlay-2\">Second</div>");
    assertThat(page.locator(".x-pw-user-overlay")).hasCount(2);
    assertThat(page.locator("#overlay-1")).hasText("First");
    assertThat(page.locator("#overlay-2")).hasText("Second");

    d1.close();
    assertThat(page.locator(".x-pw-user-overlay")).hasCount(1);
    assertThat(page.locator("#overlay-2")).hasText("Second");

    d2.close();
    assertThat(page.locator(".x-pw-user-overlay")).hasCount(0);

    context.close();
  }

  @PlaywrightTest
  @DisplayName("SC-030 should hide and show overlays")
  void shouldHideAndShowOverlays(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);

    page.screencast().showOverlay("<div id=\"my-overlay\">Visible</div>");
    assertThat(page.locator("x-pw-user-overlays")).isVisible();

    page.screencast().hideOverlays();
    assertThat(page.locator("x-pw-user-overlays")).isHidden();

    page.screencast().showOverlays();
    assertThat(page.locator("x-pw-user-overlays")).isVisible();
    assertThat(page.locator("#my-overlay")).hasText("Visible");

    context.close();
  }

  @PlaywrightTest
  @DisplayName("SC-031 should survive navigation")
  void shouldSurviveNavigation(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);

    page.screencast().showOverlay("<div id=\"persistent\">Survives Reload</div>");
    assertThat(page.locator("#persistent")).hasText("Survives Reload");

    page.navigate(server.EMPTY_PAGE);
    assertThat(page.locator("#persistent")).hasText("Survives Reload");

    page.reload();
    assertThat(page.locator("#persistent")).hasText("Survives Reload");

    context.close();
  }

  @PlaywrightTest
  @DisplayName("SC-032 should remove overlay and not restore after navigation")
  void shouldRemoveOverlayAndNotRestoreAfterNavigation(Browser browser, TestServer server) throws Exception {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);

    AutoCloseable disposable = page.screencast().showOverlay("<div id=\"temp\">Temporary</div>");
    assertThat(page.locator("#temp")).hasText("Temporary");

    disposable.close();
    assertThat(page.locator(".x-pw-user-overlay")).hasCount(0);

    page.reload();
    assertThat(page.locator(".x-pw-user-overlay")).hasCount(0);

    context.close();
  }

  @PlaywrightTest
  @DisplayName("SC-033 should sanitize scripts from overlay html")
  void shouldSanitizeScriptsFromOverlayHtml(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);

    page.screencast().showOverlay("<div id=\"safe\">Safe</div><script>window.__injected = true</script>");
    assertThat(page.locator("#safe")).hasText("Safe");
    assertNull(page.evaluate("() => window.__injected"));

    context.close();
  }

  @PlaywrightTest
  @DisplayName("SC-034 should strip event handlers from overlay html")
  void shouldStripEventHandlersFromOverlayHtml(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);

    page.screencast().showOverlay("<div id=\"clean\" onclick=\"window.__clicked=true\">Click me</div>");
    assertThat(page.locator("#clean")).hasText("Click me");
    Object hasOnclick = page.locator("#clean").evaluate("el => el.hasAttribute('onclick')");
    assertEquals(false, hasOnclick);

    context.close();
  }

  @PlaywrightTest
  @DisplayName("SC-035 should auto-remove overlay after timeout")
  void shouldAutoRemoveOverlayAfterTimeout(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);

    page.screencast().showOverlay("<div id=\"timed\">Temporary</div>", new Screencast.ShowOverlayOptions().setDuration(1));
    assertThat(page.locator(".x-pw-user-overlay")).hasCount(0);

    context.close();
  }

  @PlaywrightTest
  @DisplayName("SC-036 should allow styles in overlay html")
  void shouldAllowStylesInOverlayHtml(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);

    page.screencast().showOverlay("<div id=\"styled\" style=\"color: red; font-size: 20px;\">Styled</div>");
    assertThat(page.locator("#styled")).hasText("Styled");
    Object color = page.locator("#styled").evaluate("el => getComputedStyle(el).color");
    assertEquals("rgb(255, 0, 0)", color);

    context.close();
  }

  private static final String redOverlay = "<div style=\"position: absolute; top: 50px; left: 50px; width: 200px; height: 100px; background: rgb(255, 0, 0);\"></div>";

  @Nested
  @ParameterizedClass
  @ValueSource(strings = {"dialog", "popover"})
  class TopLayerPages {
    @Parameter
    String kind;

    // The topLayerPages object of the upstream file.
    String content() {
      if (kind.equals("dialog")) {
        return "\n" +
          "    <dialog id=\"target\" style=\"position: fixed; inset: 0; margin: 0; width: 100vw; height: 100vh; padding: 0; border: 0; background: rgb(200, 200, 200);\"></dialog>\n" +
          "    <button onclick=\"target.showModal()\">Open</button>\n" +
          "  ";
      }
      return "\n" +
        "    <div id=\"target\" popover=\"manual\" style=\"inset: 0; margin: 0; width: 100%; height: 100%; max-width: none; max-height: none; padding: 0; border: 0; background: rgb(200, 200, 200);\"></div>\n" +
        "    <button onclick=\"target.showPopover()\">Open</button>\n" +
        "  ";
    }

    @PlaywrightTest
    @DisplayName("SC-037 should show overlay above ${kind} opened before it")
    void shouldShowOverlayAboveKindOpenedBeforeIt(Browser browser) throws Exception {
      BrowserContext context = browser.newContext();
      Page page = context.newPage();
      page.setContent(content());
      AutoCloseable disposable = page.screencast().showOverlay("<div></div>");
      disposable.close();

      page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Open")).click();
      assertEquals(Arrays.asList(200, 200, 200), pixel(page.screenshot(), 100, 100));

      page.screencast().showOverlay(redOverlay);
      assertEquals(Arrays.asList(255, 0, 0), pixel(page.screenshot(), 100, 100));

      context.close();
    }

    @PlaywrightTest
    @DisplayName("SC-038 should keep overlay above ${kind} opened after it")
    void shouldKeepOverlayAboveKindOpenedAfterIt(Browser browser) {
      BrowserContext context = browser.newContext();
      Page page = context.newPage();
      page.setContent(content());
      page.screencast().showOverlay(redOverlay);
      assertEquals(Arrays.asList(255, 0, 0), pixel(page.screenshot(), 100, 100));

      page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Open")).click();
      expectPollToEqual(page, () -> pixel(page.screenshot(), 100, 100), Arrays.asList(255, 0, 0));

      context.close();
    }
  }
}
