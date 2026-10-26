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
import com.microsoft.playwright.options.AnnotatePosition;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.ScreencastActionStyle;
import com.microsoft.playwright.options.ScreencastCursor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Map;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.async;
import static com.microsoft.playwright.fixtures.Utils.mapOf;
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

  // Java: `promise.catch(() => {})` of an action that is not awaited.
  private static void asyncIgnoringErrors(Runnable action) {
    async(() -> {
      try {
        action.run();
      } catch (PlaywrightException e) {
      }
      return null;
    });
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
  @DisplayName("SC-016 should show annotation on click")
  void shouldShowAnnotationOnClick(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/input/button.html");

    page.screencast().showActions(new Screencast.ShowActionsOptions().setDuration(5000));
    asyncIgnoringErrors(() -> page.click("button"));

    assertThat(page.locator("x-pw-title")).isVisible();
    assertThat(page.locator("x-pw-title")).hasText(Pattern.compile("click", Pattern.CASE_INSENSITIVE));
    // Point and highlight are only shown when styled.
    assertThat(page.locator("x-pw-screencast-highlight")).hasCount(0);
    assertThat(page.locator("x-pw-screencast-point")).hasCount(0);

    context.close();
  }

  @PlaywrightTest
  @DisplayName("SC-017 should render annotation styles")
  void shouldRenderAnnotationStyles(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/input/button.html");

    page.screencast().showActions(new Screencast.ShowActionsOptions().setDuration(5000).setFontSize(32)
      .setStyle(style().setTitle("top: 100px; background-color: rgb(1, 2, 3)")));
    BoundingBox buttonBox = page.locator("button").boundingBox();
    asyncIgnoringErrors(() -> page.click("button"));

    // Highlight: styled box that covers the target.
    Locator highlight = page.locator("x-pw-screencast-highlight");
    assertThat(highlight).isVisible();
    assertEquals("2px", highlight.evaluate("(el) => getComputedStyle(el).outlineWidth"));
    BoundingBox box = highlight.boundingBox();
    assertTrue(Math.abs(box.x - buttonBox.x) < 0.5);
    assertTrue(Math.abs(box.y - buttonBox.y) < 0.5);
    assertTrue(Math.abs(box.width - buttonBox.width) < 0.5);
    assertTrue(Math.abs(box.height - buttonBox.height) < 0.5);

    // Point: styled marker centered on the click point, Firefox rounds the click point.
    BoundingBox pointBox = page.locator("x-pw-screencast-point").boundingBox();
    assertEquals(20, pointBox.width);
    assertTrue(Math.abs(pointBox.x + 10 - (buttonBox.x + buttonBox.width / 2)) <= 1);
    assertTrue(Math.abs(pointBox.y + 10 - (buttonBox.y + buttonBox.height / 2)) <= 1);

    // Title: style applies on top of deprecated fontSize, position wins over style.
    Locator title = page.locator("x-pw-title");
    assertThat(title).isVisible();
    @SuppressWarnings("unchecked")
    Map<String, Object> titleStyle = (Map<String, Object>) title.evaluate("(el) => {\n" +
      "  const cs = getComputedStyle(el);\n" +
      "  return {\n" +
      "    color: cs.color, borderRadius: cs.borderRadius, padding: cs.padding, backgroundColor: cs.backgroundColor,\n" +
      "    top: el.style.top, right: el.style.right, fontSize: el.style.fontSize,\n" +
      "  };\n" +
      "}");
    assertEquals("rgb(1, 2, 3)", titleStyle.get("backgroundColor"));
    assertEquals("rgb(255, 255, 255)", titleStyle.get("color"));
    assertEquals("6px", titleStyle.get("borderRadius"));
    assertEquals("6px", titleStyle.get("padding"));
    assertEquals("6px", titleStyle.get("top"));
    assertEquals("6px", titleStyle.get("right"));
    assertEquals("32px", titleStyle.get("fontSize"));

    context.close();
  }

  @Nested
  @ParameterizedClass
  @CsvSource({
    "TOP_LEFT, top, 6px, left, 6px",
    "TOP, top, 6px, left, 50%",
    "BOTTOM_LEFT, bottom, 6px, left, 6px",
    "BOTTOM, bottom, 6px, left, 50%",
    "BOTTOM_RIGHT, bottom, 6px, right, 6px"
  })
  class Position {
    @Parameter(0)
    AnnotatePosition position;
    @Parameter(1)
    String expectedKey1;
    @Parameter(2)
    String expectedValue1;
    @Parameter(3)
    String expectedKey2;
    @Parameter(4)
    String expectedValue2;

    @PlaywrightTest
    @DisplayName("SC-018 should position title at ${position}")
    void shouldPositionTitleAtPosition(Browser browser, TestServer server) {
      Map<String, String> expected = new java.util.LinkedHashMap<>();
      expected.put(expectedKey1, expectedValue1);
      expected.put(expectedKey2, expectedValue2);
      BrowserContext context = browser.newContext();
      Page page = context.newPage();
      page.navigate(server.PREFIX + "/input/button.html");

      page.screencast().showActions(new Screencast.ShowActionsOptions().setDuration(5000).setPosition(position));
      asyncIgnoringErrors(() -> page.click("button"));

      Locator title = page.locator("x-pw-title");
      assertThat(title).isVisible();

      @SuppressWarnings("unchecked")
      Map<String, Object> titleStyle = (Map<String, Object>) title.evaluate("(el) => ({\n" +
        "  top: el.style.top,\n" +
        "  bottom: el.style.bottom,\n" +
        "  left: el.style.left,\n" +
        "  right: el.style.right,\n" +
        "})");

      for (Map.Entry<String, String> entry : expected.entrySet()) {
        assertEquals(entry.getValue(), titleStyle.get(entry.getKey()));
      }

      context.close();
    }
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
  @DisplayName("SC-020 should annotate fill action")
  void shouldAnnotateFillAction(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/input/textarea.html");

    page.screencast().showActions(new Screencast.ShowActionsOptions().setDuration(5000));
    asyncIgnoringErrors(() -> page.fill("textarea", "hello"));

    Locator title = page.locator("x-pw-title");
    assertThat(title).isVisible();
    assertThat(title).hasText(Pattern.compile("fill", Pattern.CASE_INSENSITIVE));

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
    asyncIgnoringErrors(() -> page.click("button"));

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

    asyncIgnoringErrors(() -> page.click("#a", new Page.ClickOptions().setForce(true)));
    assertThat(cursor).isVisible();
    Object first = cursor.evaluate("(el) => ({ top: el.style.top, left: el.style.left })");

    asyncIgnoringErrors(() -> page.click("#b", new Page.ClickOptions().setForce(true)));
    expectPollNotToEqual(page, () -> {
      return cursor.evaluate("(el) => ({ top: el.style.top, left: el.style.left })");
    }, first);

    context.close();
  }

  @PlaywrightTest
  @DisplayName("SC-025 cursor: \"none\" suppresses the action cursor decoration")
  void cursorNoneSuppressesTheActionCursorDecoration(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/input/button.html");

    page.screencast().showActions(new Screencast.ShowActionsOptions().setDuration(5000).setCursor(ScreencastCursor.NONE));
    asyncIgnoringErrors(() -> page.click("button"));

    // The title still renders, but the cursor does not.
    assertThat(page.locator("x-pw-title")).isVisible();
    assertThat(page.locator("x-pw-action-cursor")).isHidden();

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

  @PlaywrightTest
  @DisplayName("SC-027 should survive navigation")
  void shouldSurviveNavigation(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/input/button.html");

    page.screencast().showActions(new Screencast.ShowActionsOptions().setDuration(5000));
    asyncIgnoringErrors(() -> page.click("button"));

    assertThat(page.locator("x-pw-title")).isVisible();

    page.navigate(server.PREFIX + "/input/button.html");
    asyncIgnoringErrors(() -> page.click("button"));

    assertThat(page.locator("x-pw-title")).isVisible();

    context.close();
  }
}
