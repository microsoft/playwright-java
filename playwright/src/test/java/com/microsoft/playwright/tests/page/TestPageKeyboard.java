/*
 * Copyright 2018 Google Inc. All rights reserved.
 * Modifications copyright (c) Microsoft Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Keyboard;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Mouse;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.EnabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.MouseButton;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.condition.OS;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.attachFrame;
import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Port of tests/page/page-keyboard.spec.ts. */
public class TestPageKeyboard {
  // Java: expect.poll(fn).toEqual(expected) with the defaults of expect.poll: 5000ms timeout, intervals 100, 250, 500, 1000ms
  private static void expectPollToEqual(Page page, Supplier<Object> fn, Object expected) {
    int[] intervals = {100, 250, 500, 1000};
    long deadline = System.currentTimeMillis() + 5000;
    Object last = fn.get();
    for (int i = 0; !Objects.equals(expected, last) && System.currentTimeMillis() < deadline; i++) {
      page.waitForTimeout(intervals[Math.min(i, intervals.length - 1)]);
      last = fn.get();
    }
    assertEquals(expected, last);
  }

  @PlaywrightTest
  @DisplayName("KB-001 should type into a textarea @smoke")
  void shouldTypeIntoATextarea(Page page) {
    page.evaluate("() => {\n" +
      "    const textarea = document.createElement('textarea');\n" +
      "    document.body.appendChild(textarea);\n" +
      "    textarea.focus();\n" +
      "  }");
    String text = "Hello world. I am the text that was typed!";
    page.keyboard().type(text);
    assertEquals(text, page.evaluate("() => document.querySelector('textarea').value"));
  }

  @PlaywrightTest
  @DisplayName("KB-002 should move with the arrow keys")
  void shouldMoveWithTheArrowKeys(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.type("textarea", "Hello World!");
    assertEquals("Hello World!", page.evaluate("() => document.querySelector('textarea').value"));
    for (int i = 0; i < "World!".length(); i++) {
      page.keyboard().press("ArrowLeft");
    }
    page.keyboard().type("inserted ");
    assertEquals("Hello inserted World!", page.evaluate("() => document.querySelector('textarea').value"));
    page.keyboard().down("Shift");
    for (int i = 0; i < "inserted ".length(); i++) {
      page.keyboard().press("ArrowLeft");
    }
    page.keyboard().up("Shift");
    page.keyboard().press("Backspace");
    assertEquals("Hello World!", page.evaluate("() => document.querySelector('textarea').value"));
  }

  @PlaywrightTest
  @DisplayName("KB-003 should send a character with ElementHandle.press")
  void shouldSendACharacterWithElementHandlePress(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    ElementHandle textarea = page.querySelector("textarea");
    textarea.press("a");
    assertEquals("a", page.evaluate("() => document.querySelector('textarea').value"));

    page.evaluate("() => window.addEventListener('keydown', e => e.preventDefault(), true)");

    textarea.press("b");
    assertEquals("a", page.evaluate("() => document.querySelector('textarea').value"));
  }

  @PlaywrightTest
  @DisplayName("KB-004 should send a character with insertText")
  void shouldSendACharacterWithInsertText(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.focus("textarea");
    page.keyboard().insertText("嗨");
    assertEquals("嗨", page.evaluate("() => document.querySelector('textarea').value"));
    page.evaluate("() => window.addEventListener('keydown', e => e.preventDefault(), true)");
    page.keyboard().insertText("a");
    assertEquals("嗨a", page.evaluate("() => document.querySelector('textarea').value"));
  }

  @PlaywrightTest
  @DisplayName("KB-005 should not type text with Alt modifier")
  void shouldNotTypeTextWithAltModifier(Page page) {
    page.setContent("<input><textarea></textarea><div contenteditable></div>");
    for (Locator locator : asList(page.locator("input"), page.locator("textarea"), page.locator("div"))) {
      locator.press("Alt+KeyA");
      locator.press("Alt+Digit1");
      locator.press("Alt+Shift+KeyA");
      page.keyboard().down("Alt");
      page.keyboard().press("b");
      page.keyboard().up("Alt");
      locator.press("c");
    }
    assertThat(page.locator("input")).hasValue("c");
    assertThat(page.locator("textarea")).hasValue("c");
    assertThat(page.locator("div")).hasText("c");
  }

  @PlaywrightTest
  @DisplayName("KB-006 insertText should only emit input event")
  void insertTextShouldOnlyEmitInputEvent(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.focus("textarea");
    // page.on('console', m => console.log(m.text()));
    JSHandle events = page.evaluateHandle("() => {\n" +
      "    const events = [];\n" +
      "    document.addEventListener('keydown', e => events.push(e.type));\n" +
      "    document.addEventListener('keyup', e => events.push(e.type));\n" +
      "    document.addEventListener('keypress', e => events.push(e.type));\n" +
      "    document.addEventListener('input', e => events.push(e.type));\n" +
      "    return events;\n" +
      "  }");
    page.keyboard().insertText("hello world");
    assertEquals(asList("input"), events.jsonValue());
  }

  @PlaywrightTest
  @DisplayName("KB-007 should emit keydown, keypress, textInput and input when typing a character")
  void shouldEmitKeydownKeypressTextInputAndInputWhenTypingACharacter(Page page) {
    page.setContent("<input>");
    JSHandle events = page.evaluateHandle("() => {\n" +
      "    const events = [];\n" +
      "    for (const type of ['keydown', 'keypress', 'textInput', 'input', 'keyup'])\n" +
      "      document.querySelector('input').addEventListener(type, () => events.push(type));\n" +
      "    return events;\n" +
      "  }");
    page.focus("input");
    page.keyboard().press("f");
    assertEquals(asList("keydown", "keypress", "textInput", "input", "keyup"), events.jsonValue());
  }

  @PlaywrightTest
  @DisplayName("KB-008 should dispatch key events in separate tasks")
  void shouldDispatchKeyEventsInSeparateTasks(Page page, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.FIREFOX && !Env.isBidi(), "Firefox/Juggler dispatches keydown and keypress in the same task");
    page.setContent("<input>");
    JSHandle log = page.evaluateHandle("() => {\n" +
      "    const log = [];\n" +
      "    const input = document.querySelector('input');\n" +
      "    for (const type of ['keydown', 'keypress'])\n" +
      "      input.addEventListener(type, () => { log.push(type); queueMicrotask(() => log.push('microtask-' + type)); });\n" +
      "    input.focus();\n" +
      "    return log;\n" +
      "  }");
    page.keyboard().press("a");
    // A microtask scheduled in the keydown handler must run before keypress,
    // proving each event is delivered in its own task rather than synchronously.
    assertEquals(asList("keydown", "microtask-keydown", "keypress", "microtask-keypress"), log.jsonValue());
  }

  @PlaywrightTest
  @DisplayName("KB-009 should report shiftKey")
  void shouldReportShiftKey(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/keyboard.html");
    Keyboard keyboard = page.keyboard();
    // const codeForKey = { 'Shift': 16, 'Alt': 18, 'Control': 17 };
    for (String modifierKey : new String[]{"Shift", "Alt", "Control"}) {
      keyboard.down(modifierKey);
      assertEquals("Keydown: " + modifierKey + " " + modifierKey + "Left LEFT [" + modifierKey + "]", page.evaluate("getResult()"));
      keyboard.down("!");
      // Shift+! will generate a keypress
      if (modifierKey.equals("Shift")) {
        assertEquals("Keydown: ! Digit1 STANDARD [" + modifierKey + "]\nKeypress: ! Digit1 STANDARD 33 [" + modifierKey + "]", page.evaluate("getResult()"));
      } else {
        assertEquals("Keydown: ! Digit1 STANDARD [" + modifierKey + "]", page.evaluate("getResult()"));
      }

      keyboard.up("!");
      assertEquals("Keyup: ! Digit1 STANDARD [" + modifierKey + "]", page.evaluate("getResult()"));
      keyboard.up(modifierKey);
      assertEquals("Keyup: " + modifierKey + " " + modifierKey + "Left LEFT []", page.evaluate("getResult()"));
    }
  }

  @PlaywrightTest
  @DisplayName("KB-010 should report multiple modifiers")
  void shouldReportMultipleModifiers(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/keyboard.html");
    Keyboard keyboard = page.keyboard();
    keyboard.down("Control");
    assertEquals("Keydown: Control ControlLeft LEFT [Control]", page.evaluate("getResult()"));
    keyboard.down("Alt");
    assertEquals("Keydown: Alt AltLeft LEFT [Alt Control]", page.evaluate("getResult()"));
    keyboard.down(";");
    assertEquals("Keydown: ; Semicolon STANDARD [Alt Control]", page.evaluate("getResult()"));
    keyboard.up(";");
    assertEquals("Keyup: ; Semicolon STANDARD [Alt Control]", page.evaluate("getResult()"));
    keyboard.up("Control");
    assertEquals("Keyup: Control ControlLeft LEFT [Alt]", page.evaluate("getResult()"));
    keyboard.up("Alt");
    assertEquals("Keyup: Alt AltLeft LEFT []", page.evaluate("getResult()"));
  }

  @PlaywrightTest
  @DisplayName("KB-011 should send proper codes while typing")
  void shouldSendProperCodesWhileTyping(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/keyboard.html");
    page.keyboard().type("!");
    assertEquals(
      String.join("\n", asList("Keydown: ! Digit1 STANDARD []",
        "Keypress: ! Digit1 STANDARD 33 []",
        "Keyup: ! Digit1 STANDARD []")), page.evaluate("getResult()"));
    page.keyboard().type("^");
    assertEquals(
      String.join("\n", asList("Keydown: ^ Digit6 STANDARD []",
        "Keypress: ^ Digit6 STANDARD 94 []",
        "Keyup: ^ Digit6 STANDARD []")), page.evaluate("getResult()"));
  }

  @PlaywrightTest
  @DisplayName("KB-012 should send proper codes while typing with shift")
  void shouldSendProperCodesWhileTypingWithShift(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/keyboard.html");
    Keyboard keyboard = page.keyboard();
    keyboard.down("Shift");
    page.keyboard().type("~");
    assertEquals(
      String.join("\n", asList("Keydown: Shift ShiftLeft LEFT [Shift]",
        "Keydown: ~ Backquote STANDARD [Shift]",
        "Keypress: ~ Backquote STANDARD 126 [Shift]",
        "Keyup: ~ Backquote STANDARD [Shift]")), page.evaluate("getResult()"));
    keyboard.up("Shift");
  }

  @PlaywrightTest
  @DisplayName("KB-013 should not type canceled events")
  void shouldNotTypeCanceledEvents(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.focus("textarea");
    page.evaluate("() => {\n" +
      "    window.addEventListener('keydown', event => {\n" +
      "      event.stopPropagation();\n" +
      "      event.stopImmediatePropagation();\n" +
      "      if (event.key === 'l')\n" +
      "        event.preventDefault();\n" +
      "      if (event.key === 'o')\n" +
      "        event.preventDefault();\n" +
      "    }, false);\n" +
      "  }");
    page.keyboard().type("Hello World!");
    assertEquals("He Wrd!", page.evalOnSelector("textarea", "textarea => textarea.value"));
  }

  @PlaywrightTest
  @DisplayName("KB-014 should press plus")
  void shouldPressPlus(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/keyboard.html");
    page.keyboard().press("+");
    assertEquals(
      String.join("\n", asList("Keydown: + Equal STANDARD []",
        "Keypress: + Equal STANDARD 43 []",
        "Keyup: + Equal STANDARD []")), page.evaluate("getResult()"));
  }

  @PlaywrightTest
  @DisplayName("KB-015 should press shift plus")
  void shouldPressShiftPlus(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/keyboard.html");
    page.keyboard().press("Shift++");
    assertEquals(
      String.join("\n", asList("Keydown: Shift ShiftLeft LEFT [Shift]",
        "Keydown: + Equal STANDARD [Shift]",
        "Keypress: + Equal STANDARD 43 [Shift]",
        "Keyup: + Equal STANDARD [Shift]",
        "Keyup: Shift ShiftLeft LEFT []")), page.evaluate("getResult()"));
  }

  @PlaywrightTest
  @DisplayName("KB-016 should support plus-separated modifiers")
  void shouldSupportPlusSeparatedModifiers(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/keyboard.html");
    page.keyboard().press("Shift+~");
    assertEquals(
      String.join("\n", asList("Keydown: Shift ShiftLeft LEFT [Shift]",
        "Keydown: ~ Backquote STANDARD [Shift]",
        "Keypress: ~ Backquote STANDARD 126 [Shift]",
        "Keyup: ~ Backquote STANDARD [Shift]",
        "Keyup: Shift ShiftLeft LEFT []")), page.evaluate("getResult()"));
  }

  @PlaywrightTest
  @DisplayName("KB-017 should support multiple plus-separated modifiers")
  void shouldSupportMultiplePlusSeparatedModifiers(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/keyboard.html");
    page.keyboard().press("Control+Shift+~");
    assertEquals(
      String.join("\n", asList("Keydown: Control ControlLeft LEFT [Control]",
        "Keydown: Shift ShiftLeft LEFT [Control Shift]",
        "Keydown: ~ Backquote STANDARD [Control Shift]",
        "Keyup: ~ Backquote STANDARD [Control Shift]",
        "Keyup: Shift ShiftLeft LEFT [Control]",
        "Keyup: Control ControlLeft LEFT []")), page.evaluate("getResult()"));
  }

  @PlaywrightTest
  @DisplayName("KB-018 should shift raw codes")
  void shouldShiftRawCodes(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/keyboard.html");
    page.keyboard().press("Shift+Digit3");
    assertEquals(
      String.join("\n", asList("Keydown: Shift ShiftLeft LEFT [Shift]",
        "Keydown: # Digit3 STANDARD [Shift]",
        "Keypress: # Digit3 STANDARD 35 [Shift]",
        "Keyup: # Digit3 STANDARD [Shift]",
        "Keyup: Shift ShiftLeft LEFT []")), page.evaluate("getResult()"));
  }

  @PlaywrightTest
  @DisplayName("KB-019 should specify repeat property")
  void shouldSpecifyRepeatProperty(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.focus("textarea");
    JSHandle lastEvent = captureLastKeydown(page);
    page.keyboard().down("a");
    assertEquals(false, lastEvent.evaluate("e => e.repeat"));
    page.keyboard().press("a");
    assertEquals(true, lastEvent.evaluate("e => e.repeat"));

    page.keyboard().down("b");
    assertEquals(false, lastEvent.evaluate("e => e.repeat"));
    page.keyboard().down("b");
    assertEquals(true, lastEvent.evaluate("e => e.repeat"));

    page.keyboard().up("a");
    page.keyboard().down("a");
    assertEquals(false, lastEvent.evaluate("e => e.repeat"));
  }

  @PlaywrightTest
  @DisplayName("KB-020 should type all kinds of characters")
  void shouldTypeAllKindsOfCharacters(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.focus("textarea");
    String text = "This text goes onto two lines.\nThis character is 嗨.";
    page.keyboard().type(text);
    assertEquals(text, page.evalOnSelector("textarea", "t => t.value"));
  }

  @PlaywrightTest
  @DisplayName("KB-021 should specify location")
  void shouldSpecifyLocation(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    JSHandle lastEvent = captureLastKeydown(page);
    ElementHandle textarea = page.querySelector("textarea");

    textarea.press("Digit5");
    // expect.soft
    assertAll(
      () -> assertEquals(0, lastEvent.evaluate("e => e.location")),
      () -> assertEquals("5", lastEvent.evaluate("e => e.key")),
      () -> assertEquals("Digit5", lastEvent.evaluate("e => e.code")));

    textarea.press("ControlLeft");
    assertAll(
      () -> assertEquals(1, lastEvent.evaluate("e => e.location")),
      () -> assertEquals("Control", lastEvent.evaluate("e => e.key")),
      () -> assertEquals("ControlLeft", lastEvent.evaluate("e => e.code")));

    textarea.press("ControlRight");
    assertAll(
      () -> assertEquals(2, lastEvent.evaluate("e => e.location")),
      () -> assertEquals("Control", lastEvent.evaluate("e => e.key")),
      () -> assertEquals("ControlRight", lastEvent.evaluate("e => e.code")));

    textarea.press("NumpadSubtract");
    assertAll(
      () -> assertEquals(3, lastEvent.evaluate("e => e.location")),
      () -> assertEquals("-", lastEvent.evaluate("e => e.key")),
      () -> assertEquals("NumpadSubtract", lastEvent.evaluate("e => e.code")));
  }

  @PlaywrightTest
  @DisplayName("KB-022 should specify numpad location on keyup")
  void shouldSpecifyNumpadLocationOnKeyup(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/keyboard.html");
    page.keyboard().press("NumpadSubtract");
    assertEquals(
      String.join("\n", asList("Keydown: - NumpadSubtract NUMPAD []",
        "Keypress: - NumpadSubtract NUMPAD 45 []",
        "Keyup: - NumpadSubtract NUMPAD []")), page.evaluate("getResult()"));
  }

  @PlaywrightTest
  @DisplayName("KB-023 should press NumpadDecimal as Delete")
  void shouldPressNumpadDecimalAsDelete(Page page) {
    page.setContent("<textarea></textarea>");
    JSHandle lastEvent = captureLastKeydown(page);
    Locator textarea = page.locator("textarea");

    textarea.press("NumpadDecimal");
    assertEquals("Delete", lastEvent.evaluate("e => e.key"));
    assertEquals("NumpadDecimal", lastEvent.evaluate("e => e.code"));
    assertEquals(3, lastEvent.evaluate("e => e.location"));
    assertThat(textarea).hasValue("");
  }

  @PlaywrightTest
  @DisplayName("KB-024 should type numpad digits with Shift")
  void shouldTypeNumpadDigitsWithShift(Page page) {
    page.setContent("<textarea></textarea>");
    Locator textarea = page.locator("textarea");
    textarea.press("Shift+Numpad1");
    textarea.press("Shift+NumpadDecimal");
    textarea.press("Shift+Numpad9");
    assertThat(textarea).hasValue("1.9");
  }

  @PlaywrightTest
  @DisplayName("KB-025 should press Enter")
  void shouldPressEnter(Page page, TestServer server) {
    page.setContent("<textarea></textarea>");
    page.focus("textarea");
    JSHandle lastEventHandle = captureLastKeydown(page);
    testEnterKey(page, lastEventHandle, "Enter", "Enter", "Enter");
    testEnterKey(page, lastEventHandle, "NumpadEnter", "Enter", "NumpadEnter");
    testEnterKey(page, lastEventHandle, "\n", "Enter", "Enter");
    testEnterKey(page, lastEventHandle, "\r", "Enter", "Enter");
  }

  private static void testEnterKey(Page page, JSHandle lastEventHandle, String key, String expectedKey, String expectedCode) {
    page.keyboard().press(key);
    Map<?, ?> lastEvent = (Map<?, ?>) lastEventHandle.jsonValue();
    assertEquals(expectedKey, lastEvent.get("key")); // had the wrong key
    assertEquals(expectedCode, lastEvent.get("code")); // had the wrong code
    Object value = page.evalOnSelector("textarea", "t => t.value");
    assertEquals("\n", value); // failed to create a newline
    page.evalOnSelector("textarea", "t => t.value = ''");
  }

  @PlaywrightTest
  @DisplayName("KB-026 should press audio and media control keys")
  void shouldPressAudioAndMediaControlKeys(Page page, BrowserName browserName) {
    page.setContent("<input autofocus>");
    page.focus("input");
    JSHandle lastEvent = captureLastKeydown(page);
    List<Map<String, Object>> mediaKeys = asList(
      mapOf("key", "AudioVolumeMute", "code", browserName == BrowserName.FIREFOX ? "VolumeMute" : "AudioVolumeMute"),
      mapOf("key", "AudioVolumeDown", "code", browserName == BrowserName.FIREFOX ? "VolumeDown" : "AudioVolumeDown"),
      mapOf("key", "AudioVolumeUp", "code", browserName == BrowserName.FIREFOX ? "VolumeUp" : "AudioVolumeUp"),
      mapOf("key", "MediaTrackNext", "code", "MediaTrackNext"),
      mapOf("key", "MediaTrackPrevious", "code", "MediaTrackPrevious"),
      mapOf("key", "MediaPlayPause", "code", "MediaPlayPause"));

    for (Map<String, Object> mediaKey : mediaKeys) {
      page.keyboard().press((String) mediaKey.get("key"));
      assertAll(
        () -> assertEquals(mediaKey.get("key"), lastEvent.evaluate("e => e.key")),
        () -> assertEquals(mediaKey.get("code"), lastEvent.evaluate("e => e.code")),
        () -> assertEquals(0, lastEvent.evaluate("e => e.location")));
    }
  }

  @PlaywrightTest
  @DisplayName("KB-027 should throw on unknown keys")
  void shouldThrowOnUnknownKeys(Page page, TestServer server) {
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.keyboard().press("NotARealKey"));
    assertTrue(error.getMessage().contains("Unknown key: \"NotARealKey\""));

    error = assertThrows(PlaywrightException.class, () -> page.keyboard().press("ё"));
    assertTrue(error.getMessage().contains("Unknown key: \"ё\""));

    error = assertThrows(PlaywrightException.class, () -> page.keyboard().press("😊"));
    assertTrue(error.getMessage().contains("Unknown key: \"😊\""));
  }

  @PlaywrightTest
  @DisplayName("KB-028 should type emoji")
  void shouldTypeEmoji(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.type("textarea", "👹 Tokyo street Japan 🇯🇵");
    assertEquals("👹 Tokyo street Japan 🇯🇵", page.evalOnSelector("textarea", "textarea => textarea.value"));
  }

  @PlaywrightTest
  @DisplayName("KB-029 should type emoji into an iframe")
  void shouldTypeEmojiIntoAnIframe(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    attachFrame(page, "emoji-test", server.PREFIX + "/input/textarea.html");
    Frame frame = page.frames().get(1);
    ElementHandle textarea = frame.querySelector("textarea");
    textarea.type("👹 Tokyo street Japan 🇯🇵");
    assertEquals("👹 Tokyo street Japan 🇯🇵", frame.evalOnSelector("textarea", "textarea => textarea.value"));
  }

  @PlaywrightTest
  @DisplayName("KB-030 should handle selectAll")
  void shouldHandleSelectAll(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    ElementHandle textarea = page.querySelector("textarea");
    textarea.type("some text");
    page.keyboard().down("ControlOrMeta");
    page.keyboard().press("a");
    page.keyboard().up("ControlOrMeta");
    page.keyboard().press("Backspace");
    assertEquals("", page.evalOnSelector("textarea", "textarea => textarea.value"));
  }

  @PlaywrightTest
  @DisplayName("KB-031 pressing Meta should not result in any text insertion on any platform")
  void pressingMetaShouldNotResultInAnyTextInsertionOnAnyPlatform(Page page, TestServer server) {
    page.setContent("<input type=\"text\" value=\"hello world\">");
    Locator input = page.locator("input");
    assertThat(input).hasValue("hello world");
    input.focus();
    page.keyboard().press("Meta");
    assertThat(input).hasValue("hello world");
  }

  @PlaywrightTest
  @DisplayName("KB-032 should be able to prevent selectAll")
  void shouldBeAbleToPreventSelectAll(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    ElementHandle textarea = page.querySelector("textarea");
    textarea.type("some text");
    page.evalOnSelector("textarea", "textarea => {\n" +
      "    textarea.addEventListener('keydown', event => {\n" +
      "      if (event.key === 'a' && (event.metaKey || event.ctrlKey))\n" +
      "        event.preventDefault();\n" +
      "    }, false);\n" +
      "  }");
    page.keyboard().down("ControlOrMeta");
    page.keyboard().press("a");
    page.keyboard().up("ControlOrMeta");
    page.keyboard().press("Backspace");
    assertEquals("some tex", page.evalOnSelector("textarea", "textarea => textarea.value"));
  }

  @PlaywrightTest
  @DisplayName("KB-033 should support MacOS shortcuts")
  @DisabledOnBrowser(value = BrowserName.FIREFOX, os = OS.MAC, reason = "fixme: https://github.com/microsoft/playwright/issues/5721")
  void shouldSupportMacOSShortcuts(Page page, TestServer server) {
    assumeTrue(Env.platform().equals("darwin"));
    // @see https://github.com/microsoft/playwright/issues/5721

    page.navigate(server.PREFIX + "/input/textarea.html");
    ElementHandle textarea = page.querySelector("textarea");
    textarea.type("some text");
    // select one word backwards
    page.keyboard().press("Shift+Control+Alt+KeyB");
    page.keyboard().press("Backspace");
    assertEquals("some ", page.evalOnSelector("textarea", "textarea => textarea.value"));
  }

  @PlaywrightTest
  @DisplayName("KB-034 should press the meta key")
  void shouldPressTheMetaKey(Page page) {
    JSHandle lastEvent = captureLastKeydown(page);
    page.keyboard().press("Meta");
    Map<?, ?> event = (Map<?, ?>) lastEvent.jsonValue();
    Object key = event.get("key");
    Object code = event.get("code");
    Object metaKey = event.get("metaKey");
    assertEquals("Meta", key);
    assertEquals("MetaLeft", code);
    assertEquals(true, metaKey);
  }

  @PlaywrightTest
  @DisplayName("KB-035 should work with keyboard events with empty.html")
  void shouldWorkWithKeyboardEventsWithEmptyHtml(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/empty.html");
    JSHandle lastEvent = captureLastKeydown(page);
    page.keyboard().press("a");
    assertEquals("a", lastEvent.evaluate("l => l.key"));
  }

  @PlaywrightTest
  @DisplayName("KB-036 should work after a cross origin navigation")
  void shouldWorkAfterACrossOriginNavigation(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/empty.html");
    page.navigate(server.CROSS_PROCESS_PREFIX + "/empty.html");
    JSHandle lastEvent = captureLastKeydown(page);
    page.keyboard().press("a");
    assertEquals("a", lastEvent.evaluate("l => l.key"));
  }

  @PlaywrightTest
  @DisplayName("KB-037 should expose keyIdentifier in webkit")
  @EnabledOnBrowser(value = BrowserName.WEBKIT, reason = "event.keyIdentifier has been removed from all browsers except WebKit")
  void shouldExposeKeyIdentifierInWebkit(Page page) {
    JSHandle lastEvent = captureLastKeydown(page);
    Map<String, Object> keyMap = mapOf(
      "ArrowUp", "Up",
      "ArrowDown", "Down",
      "ArrowLeft", "Left",
      "ArrowRight", "Right",
      "Backspace", "U+0008",
      "Tab", "U+0009",
      "Delete", "U+007F",
      "a", "U+0041",
      "b", "U+0042",
      "F12", "F12");
    for (Map.Entry<String, Object> entry : keyMap.entrySet()) {
      String key = entry.getKey();
      Object keyIdentifier = entry.getValue();
      page.keyboard().press(key);
      assertEquals(keyIdentifier, lastEvent.evaluate("e => e.keyIdentifier"));
    }
  }

  @PlaywrightTest
  @DisplayName("KB-038 should scroll with PageDown")
  void shouldScrollWithPageDown(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/scrollable.html");
    // A click is required for WebKit to send the event into the body.
    page.click("body");
    page.keyboard().press("PageDown");
    // We can't wait for the scroll to finish, so just wait for it to start.
    page.waitForFunction("() => scrollY > 0");
  }

  @PlaywrightTest
  @DisplayName("KB-039 should move around the selection in a contenteditable")
  void shouldMoveAroundTheSelectionInAContenteditable(Page page) {
    page.setContent("<div contenteditable></div>");
    page.focus("div");
    String modifier = Env.isMac ? "Alt" : "Control";
    page.keyboard().type("Hello World");
    page.keyboard().down(modifier);
    page.keyboard().down("Shift");
    page.keyboard().press("ArrowLeft");
    page.keyboard().up("Shift");
    page.keyboard().up(modifier);
    assertEquals("World", page.evaluate("() => window.getSelection().toString()"));
  }

  @PlaywrightTest
  @DisplayName("KB-040 should move to the start of the document")
  void shouldMoveToTheStartOfTheDocument(Page page) {
    assumeTrue(Env.isMac);
    page.setContent("<div contenteditable></div>");
    page.focus("div");
    page.keyboard().type("1\n2\n3\n");
    page.keyboard().press("Shift+Meta+ArrowUp");
    assertEquals("1\n2\n3\n", page.evaluate("() => window.getSelection().toString()"));
  }

  @PlaywrightTest
  @DisplayName("KB-041 should dispatch a click event on a button when Space gets pressed")
  void shouldDispatchAClickEventOnAButtonWhenSpaceGetsPressed(Page page) {
    page.setContent("<button type=\"button\">a11y</button>");
    JSHandle actual = page.evaluateHandle("() => {\n" +
      "    const actual = { clicked: false };\n" +
      "    document.querySelector('button').addEventListener('click', () => (actual.clicked = true));\n" +
      "    return actual;\n" +
      "  }");
    page.focus("button");
    page.keyboard().press("Space");
    assertEquals(true, ((Map<?, ?>) actual.jsonValue()).get("clicked"));
  }

  @PlaywrightTest
  @DisplayName("KB-042 should dispatch a click event on a button when Enter gets pressed")
  void shouldDispatchAClickEventOnAButtonWhenEnterGetsPressed(Page page) {
    page.setContent("<button type=\"button\">a11y</button>");
    JSHandle actual = page.evaluateHandle("() => {\n" +
      "    const actual = { clicked: false };\n" +
      "    document.querySelector('button').addEventListener('click', () => (actual.clicked = true));\n" +
      "    return actual;\n" +
      "  }");
    page.focus("button");
    page.keyboard().press("Enter");
    assertEquals(true, ((Map<?, ?>) actual.jsonValue()).get("clicked"));
  }

  @PlaywrightTest
  @DisplayName("KB-043 should support simple copy-pasting")
  void shouldSupportSimpleCopyPasting(Page page) {
    page.setContent("<div contenteditable>123</div>");
    page.focus("div");
    page.keyboard().press("ControlOrMeta+KeyA");
    page.keyboard().press("ControlOrMeta+KeyC");
    page.keyboard().press("ControlOrMeta+KeyV");
    page.keyboard().press("ControlOrMeta+KeyV");
    assertEquals("123123", page.evaluate("() => document.querySelector('div').textContent"));
  }

  @PlaywrightTest
  @DisplayName("KB-044 should support simple cut-pasting")
  void shouldSupportSimpleCutPasting(Page page) {
    page.setContent("<div contenteditable>123</div>");
    page.focus("div");
    page.keyboard().press("ControlOrMeta+KeyA");
    page.keyboard().press("ControlOrMeta+KeyX");
    page.keyboard().press("ControlOrMeta+KeyV");
    page.keyboard().press("ControlOrMeta+KeyV");
    assertEquals("123123", page.evaluate("() => document.querySelector('div').textContent"));
  }

  @PlaywrightTest
  @DisplayName("KB-045 should support undo-redo")
  void shouldSupportUndoRedo(Page page, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.WEBKIT && Env.isLinux || "webkit-wsl".equals(Env.channel()), "fixme: https://github.com/microsoft/playwright/issues/12000");
    page.setContent("<div contenteditable></div>");
    Locator div = page.locator("div");
    assertThat(div).hasText("");
    div.type("123");
    assertThat(div).hasText("123");
    page.keyboard().press("ControlOrMeta+KeyZ");
    assertThat(div).hasText("");
    page.keyboard().press("Shift+ControlOrMeta+KeyZ");
    assertThat(div).hasText("123");
  }

  @PlaywrightTest
  @DisplayName("KB-046 should type repeatedly in contenteditable in shadow dom")
  void shouldTypeRepeatedlyInContenteditableInShadowDom(Page page) {

    page.setContent("\n" +
      "    <html>\n" +
      "      <body>\n" +
      "        <shadow-element></shadow-element>\n" +
      "        <script>\n" +
      "          customElements.define('shadow-element', class extends HTMLElement {\n" +
      "            constructor() {\n" +
      "              super();\n" +
      "              this.attachShadow({ mode: 'open' });\n" +
      "            }\n" +
      "\n" +
      "            connectedCallback() {\n" +
      "              this.shadowRoot.innerHTML = `\n" +
      "                <style>\n" +
      "                  .editor { padding: 1rem; margin: 1rem; border: 1px solid #ccc; }\n" +
      "                </style>\n" +
      "                <div class=editor contenteditable id=foo></div>\n" +
      "                <hr>\n" +
      "                <section>\n" +
      "                  <div class=editor contenteditable id=bar></div>\n" +
      "                </section>\n" +
      "              `;\n" +
      "            }\n" +
      "          });\n" +
      "        </script>\n" +
      "      </body>\n" +
      "    </html>\n" +
      "  ");

    Locator editor = page.locator("shadow-element > .editor").first();
    editor.type("This is the first box.");

    Locator sectionEditor = page.locator("section .editor");
    sectionEditor.type("This is the second box.");

    assertEquals("This is the first box.", editor.textContent());
    assertEquals("This is the second box.", sectionEditor.textContent());
  }

  @PlaywrightTest
  @DisplayName("KB-047 should type repeatedly in contenteditable in shadow dom with nested elements")
  void shouldTypeRepeatedlyInContenteditableInShadowDomWithNestedElements(Page page) {
    page.setContent("\n" +
      "    <html>\n" +
      "      <body>\n" +
      "        <shadow-element></shadow-element>\n" +
      "        <script>\n" +
      "          customElements.define('shadow-element', class extends HTMLElement {\n" +
      "            constructor() {\n" +
      "              super();\n" +
      "              this.attachShadow({ mode: 'open' });\n" +
      "            }\n" +
      "\n" +
      "            connectedCallback() {\n" +
      "              this.shadowRoot.innerHTML = `\n" +
      "                <style>\n" +
      "                  .editor { padding: 1rem; margin: 1rem; border: 1px solid #ccc; }\n" +
      "                </style>\n" +
      "                <div class=editor contenteditable id=foo><p>hello</p></div>\n" +
      "                <hr>\n" +
      "                <section>\n" +
      "                  <div class=editor contenteditable id=bar><p>world</p></div>\n" +
      "                </section>\n" +
      "              `;\n" +
      "            }\n" +
      "          });\n" +
      "        </script>\n" +
      "      </body>\n" +
      "    </html>\n" +
      "  ");

    Locator editor = page.locator("shadow-element > .editor").first();
    editor.type("This is the first box: ");

    Locator sectionEditor = page.locator("section .editor");
    sectionEditor.type("This is the second box: ");

    assertEquals("This is the first box: hello", editor.textContent());
    assertEquals("This is the second box: world", sectionEditor.textContent());
  }

  @PlaywrightTest
  @DisplayName("KB-048 should type repeatedly in input in shadow dom")
  void shouldTypeRepeatedlyInInputInShadowDom(Page page) {
    page.setContent("\n" +
      "    <html>\n" +
      "      <body>\n" +
      "        <shadow-element></shadow-element>\n" +
      "        <script>\n" +
      "          customElements.define('shadow-element', class extends HTMLElement {\n" +
      "            constructor() {\n" +
      "              super();\n" +
      "              this.attachShadow({ mode: 'open' });\n" +
      "            }\n" +
      "\n" +
      "            connectedCallback() {\n" +
      "              this.shadowRoot.innerHTML = `\n" +
      "                <style>\n" +
      "                  .editor { padding: 1rem; margin: 1rem; border: 1px solid #ccc; }\n" +
      "                </style>\n" +
      "                <input class=editor id=foo>\n" +
      "                <hr>\n" +
      "                <section>\n" +
      "                  <input class=editor id=bar>\n" +
      "                </section>\n" +
      "              `;\n" +
      "            }\n" +
      "          });\n" +
      "        </script>\n" +
      "      </body>\n" +
      "    </html>\n" +
      "  ");

    Locator editor = page.locator("shadow-element > .editor").first();
    editor.type("This is the first box.");

    Locator sectionEditor = page.locator("section .editor");
    sectionEditor.type("This is the second box.");

    assertEquals("This is the first box.", editor.inputValue());
    assertEquals("This is the second box.", sectionEditor.inputValue());
  }

  @PlaywrightTest
  @DisplayName("KB-049 type to non-focusable element should maintain old focus")
  void typeToNonFocusableElementShouldMaintainOldFocus(Page page) {
    page.setContent("\n" +
      "    <div id=\"focusable\" tabindex=\"0\">focusable div</div>\n" +
      "    <div id=\"non-focusable-and-non-editable\">non-editable, non-focusable</div>\n" +
      "  ");

    page.locator("#focusable").focus();
    assertEquals("focusable", page.evaluate("() => document.activeElement?.id"));
    page.locator("#non-focusable-and-non-editable").type("foo");
    assertEquals("focusable", page.evaluate("() => document.activeElement?.id"));
  }

  private static JSHandle captureLastKeydown(Page page) {
    JSHandle lastEvent = page.evaluateHandle("() => {\n" +
      "    const lastEvent = {\n" +
      "      repeat: false,\n" +
      "      location: -1,\n" +
      "      code: '',\n" +
      "      key: '',\n" +
      "      metaKey: false,\n" +
      "      keyIdentifier: 'unsupported'\n" +
      "    };\n" +
      "    document.addEventListener('keydown', e => {\n" +
      "      lastEvent.repeat = e.repeat;\n" +
      "      lastEvent.location = e.location;\n" +
      "      lastEvent.key = e.key;\n" +
      "      lastEvent.code = e.code;\n" +
      "      lastEvent.metaKey = e.metaKey;\n" +
      "      lastEvent.keyIdentifier = 'keyIdentifier' in e && typeof e['keyIdentifier'] === 'string' && e['keyIdentifier'];\n" +
      "    }, true);\n" +
      "    return lastEvent;\n" +
      "  }");
    return lastEvent;
  }

  @PlaywrightTest
  @DisplayName("KB-050 should dispatch insertText after context menu was opened")
  @DisabledOnBrowser(value = BrowserName.CHROMIUM, os = OS.WINDOWS, reason = "context menu support is best-effort for Linux and MacOS")
  void shouldDispatchInsertTextAfterContextMenuWasOpened(TestServer server, Page page) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.evaluate("() => {\n" +
      "    window['contextMenuPromise'] = new Promise(x => {\n" +
      "      window.addEventListener('contextmenu', x, false);\n" +
      "    });\n" +
      "  }");

    BoundingBox box = page.locator("textarea").boundingBox();
    double cx = box.x + box.width / 2;
    double cy = box.y + box.height / 2;
    page.mouse().click(cx, cy, new Mouse.ClickOptions().setButton(MouseButton.RIGHT));
    page.evaluate("() => window['contextMenuPromise']");

    page.keyboard().insertText("嗨");
    // Java: expect.poll(() => page.locator('textarea').inputValue()).toBe('嗨') is the retrying matcher hasValue()
    assertThat(page.locator("textarea")).hasValue("嗨");
  }

  @PlaywrightTest
  @DisplayName("KB-051 should type after context menu was opened")
  @DisabledOnBrowser(value = BrowserName.CHROMIUM, os = OS.WINDOWS, reason = "context menu support is best-effort for Linux and MacOS")
  void shouldTypeAfterContextMenuWasOpened(TestServer server, Page page) {
    page.evaluate("() => {\n" +
      "    window['keys'] = [];\n" +
      "    window.addEventListener('keydown', event => window['keys'].push(event.key));\n" +
      "    window['contextMenuPromise'] = new Promise(x => {\n" +
      "      window.addEventListener('contextmenu', x, false);\n" +
      "    });\n" +
      "  }");


    page.mouse().move(100, 100);
    page.mouse().down(new Mouse.DownOptions().setButton(MouseButton.RIGHT));
    page.evaluate("() => window['contextMenuPromise']");

    page.keyboard().down("ArrowDown");

    expectPollToEqual(page, () -> page.evaluate("window.keys"), asList("ArrowDown"));
  }

  @PlaywrightTest
  @DisplayName("KB-052 should have correct Keydown/Keyup order when pressing Escape key")
  void shouldHaveCorrectKeydownKeyupOrderWhenPressingEscapeKey(Page page, TestServer server) {

    page.navigate(server.PREFIX + "/input/keyboard.html");
    page.keyboard().press("Escape");
    assertEquals("Keydown: Escape Escape STANDARD []\n" +
      "Keyup: Escape Escape STANDARD []", page.evaluate("getResult()"));
  }

  @PlaywrightTest
  @DisplayName("KB-053 should close dialog on Escape key press in contenteditable")
  void shouldCloseDialogOnEscapeKeyPressInContenteditable(Page page) {
    page.setContent("\n" +
      "    <dialog>\n" +
      "      <div contenteditable>Edit Me</div>\n" +
      "    </dialog>\n" +
      "  ");

    Locator dialog = page.locator("dialog");
    Locator widget = dialog.locator("[contenteditable]");
    dialog.evaluate("node => node.showModal()");
    assertThat(dialog).hasJSProperty("open", true);
    assertThat(widget).isVisible();

    widget.press("Escape");
    assertThat(dialog).hasJSProperty("open", false);
    assertThat(widget).not().isVisible();
  }
}
