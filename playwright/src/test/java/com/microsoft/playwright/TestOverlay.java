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

import com.microsoft.playwright.Overlay;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// Note: The overlay elements are rendered inside a closed shadow root in driver mode,
// so locator-based DOM assertions are not possible in Java. These tests verify that
// the protocol calls succeed without errors.
public class TestOverlay extends TestBase {
  @Test
  void shouldAddAndRemoveOverlay() throws Exception {
    page.navigate(server.EMPTY_PAGE);
    AutoCloseable disposable = page.overlay().show("<div id=\"my-overlay\">Hello Overlay</div>");
    assertNotNull(disposable);
    disposable.close();
  }

  @Test
  void shouldAddMultipleOverlays() throws Exception {
    page.navigate(server.EMPTY_PAGE);
    AutoCloseable d1 = page.overlay().show("<div id=\"overlay-1\">First</div>");
    AutoCloseable d2 = page.overlay().show("<div id=\"overlay-2\">Second</div>");
    d1.close();
    d2.close();
  }

  @Test
  void shouldHideAndShowOverlays() throws Exception {
    page.navigate(server.EMPTY_PAGE);
    page.overlay().show("<div id=\"my-overlay\">Visible</div>");
    page.overlay().setVisible(false);
    page.overlay().setVisible(true);
  }

  @Test
  void shouldSurviveNavigation() {
    page.navigate(server.EMPTY_PAGE);
    page.overlay().show("<div id=\"persistent\">Survives Reload</div>");
    page.navigate(server.EMPTY_PAGE);
    page.reload();
  }

  @Test
  void shouldRemoveOverlayAndNotRestoreAfterNavigation() throws Exception {
    page.navigate(server.EMPTY_PAGE);
    AutoCloseable disposable = page.overlay().show("<div id=\"temp\">Temporary</div>");
    disposable.close();
    page.reload();
  }

  @Test
  void shouldSanitizeScriptsFromOverlayHtml() {
    page.navigate(server.EMPTY_PAGE);
    page.overlay().show("<div id=\"safe\">Safe</div><script>window.__injected = true</script>");
  }

  @Test
  void shouldStripEventHandlersFromOverlayHtml() {
    page.navigate(server.EMPTY_PAGE);
    page.overlay().show("<div id=\"clean\" onclick=\"window.__clicked=true\">Click me</div>");
  }

  @Test
  void shouldAutoRemoveOverlayAfterTimeout() {
    page.navigate(server.EMPTY_PAGE);
    page.overlay().show("<div id=\"timed\">Temporary</div>", new Overlay.ShowOptions().setDuration(1));
  }

  @Test
  void shouldAllowStylesInOverlayHtml() {
    page.navigate(server.EMPTY_PAGE);
    page.overlay().show("<div id=\"styled\" style=\"color: red; font-size: 20px;\">Styled</div>");
  }

  @Test
  void shouldShowChapter() {
    page.navigate(server.EMPTY_PAGE);
    page.overlay().chapter("Chapter Title");
    page.overlay().chapter("With Description", new Overlay.ChapterOptions().setDescription("Some details").setDuration(100));
  }
}
