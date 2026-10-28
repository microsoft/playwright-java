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
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Screencast;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.ScreencastActionStyle;
import org.junit.jupiter.api.DisplayName;

import java.util.Map;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/library/screencast-actions.spec.ts. */
public class TestScreencastActions {
  private static ScreencastActionStyle style() {
    return new ScreencastActionStyle()
      .setPoint("width: 20px; height: 20px; border-radius: 50%; background: red")
      .setHighlight("outline: 2px solid #333");
  }

  // Java: `promise.catch(() => {})` of an action that is not awaited; the action runs inline.
  private static void ignoringErrors(Runnable action) {
    try {
      action.run();
    } catch (PlaywrightException e) {
    }
  }

  // Java: expect.poll(() => value).not.toEqual(unexpected); waitForCondition() dispatches the events while waiting.
  private static void expectPollNotToEqual(Page page, java.util.function.Supplier<Object> value, Object unexpected) {
    try {
      page.waitForCondition(() -> !unexpected.equals(value.get()), new Page.WaitForConditionOptions().setTimeout(5000));
    } catch (TimeoutError e) {
      // The assertion below reports the last value.
    }
    assertNotEquals(unexpected, value.get());
  }

  @PlaywrightTest
  @DisplayName("SC-019 should clear annotation after duration")
  void shouldClearAnnotationAfterDuration(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/input/button.html");

    page.screencast().showActions(new Screencast.ShowActionsOptions().setDuration(1000).setStyle(style()));
    page.click("button");

    assertThat(page.locator("x-pw-screencast-point")).hasCount(0);
    assertThat(page.locator("x-pw-screencast-highlight")).hasCount(0);
    assertThat(page.locator("x-pw-title")).isHidden();

    context.close();
  }

  @PlaywrightTest
  @DisplayName("SC-021 should stop showing actions after dispose")
  void shouldStopShowingActionsAfterDispose(Browser browser, TestServer server) throws Exception {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/input/button.html");

    AutoCloseable actions = page.screencast().showActions(new Screencast.ShowActionsOptions().setDuration(1000));
    page.click("button");
    actions.close();

    page.navigate(server.PREFIX + "/input/button.html");
    page.click("button");

    assertThat(page.locator("x-pw-title")).isHidden();

    context.close();
  }

  @PlaywrightTest
  @DisplayName("SC-022 should stop showing actions after hideActions")
  void shouldStopShowingActionsAfterHideActions(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/input/button.html");

    page.screencast().showActions(new Screencast.ShowActionsOptions().setDuration(1000));
    page.click("button");
    page.screencast().hideActions();

    page.navigate(server.PREFIX + "/input/button.html");
    page.click("button");

    assertThat(page.locator("x-pw-title")).isHidden();

    context.close();
  }

  @PlaywrightTest
  @DisplayName("SC-023 should render an action cursor that animates to the click point")
  void shouldRenderAnActionCursorThatAnimatesToTheClickPoint(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/input/button.html");

    page.screencast().showActions(new Screencast.ShowActionsOptions().setDuration(5000));
    ignoringErrors(() -> page.click("button"));

    Locator cursor = page.locator("x-pw-action-cursor");
    assertThat(cursor).isVisible();

    @SuppressWarnings("unchecked")
    Map<String, Object> initial = (Map<String, Object>) cursor.evaluate("(el) => ({\n" +
      "  top: el.style.top,\n" +
      "  left: el.style.left,\n" +
      "})");
    assertTrue(Pattern.compile("\\d+px").matcher((String) initial.get("top")).find());
    assertTrue(Pattern.compile("\\d+px").matcher((String) initial.get("left")).find());

    context.close();
  }

  @PlaywrightTest
  @DisplayName("SC-024 cursor moves between two pointer actions")
  void cursorMovesBetweenTwoPointerActions(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.setContent("\n" +
      "    <div style=\"position: fixed; top: 20px; left: 20px; width: 60px; height: 60px;\" id=\"a\">A</div>\n" +
      "    <div style=\"position: fixed; bottom: 20px; right: 20px; width: 60px; height: 60px;\" id=\"b\">B</div>\n" +
      "  ");

    page.screencast().showActions(new Screencast.ShowActionsOptions().setDuration(5000));
    Locator cursor = page.locator("x-pw-action-cursor");

    ignoringErrors(() -> page.click("#a", new Page.ClickOptions().setForce(true)));
    assertThat(cursor).isVisible();
    Object first = cursor.evaluate("(el) => ({ top: el.style.top, left: el.style.left })");

    ignoringErrors(() -> page.click("#b", new Page.ClickOptions().setForce(true)));
    expectPollNotToEqual(page, () -> {
      return cursor.evaluate("(el) => ({ top: el.style.top, left: el.style.left })");
    }, first);

    context.close();
  }

  @PlaywrightTest
  @DisplayName("SC-026 cursor stays at the last action point until hideActions")
  void cursorStaysAtTheLastActionPointUntilHideActions(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/input/button.html");

    page.screencast().showActions(new Screencast.ShowActionsOptions().setDuration(100));
    page.click("button");
    assertThat(page.locator("x-pw-title")).isHidden();
    Locator cursor = page.locator("x-pw-action-cursor");
    assertThat(cursor).isVisible();
    Object position = cursor.evaluate("(el) => ({ top: el.style.top, left: el.style.left })");

    page.navigate(server.EMPTY_PAGE);
    assertThat(cursor).isVisible();
    assertEquals(position, cursor.evaluate("(el) => ({ top: el.style.top, left: el.style.left })"));

    page.screencast().hideActions();
    assertThat(cursor).isHidden();

    context.close();
  }

}
