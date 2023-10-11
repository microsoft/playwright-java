package com.microsoft.playwright;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.microsoft.playwright.Utils.attachFrame;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class TestPageKeyboard extends TestBase {

  @Test
  void shouldTypeIntoATextarea() {
    page.evaluate("() => {\n" +
      "  const textarea = document.createElement('textarea');\n" +
      "  document.body.appendChild(textarea);\n" +
      "  textarea.focus();\n" +
      "}");
    String text = "Hello world. I am the text that was typed!";
    page.keyboard().type(text);
    assertEquals(text, page.evaluate("() => document.querySelector('textarea').value"));
  }

  @Test
  void shouldMoveWithTheArrowKeys() {
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

  @Test
  void shouldSendACharacterWithElementHandlePress() {
    page.navigate(server.PREFIX + "/input/textarea.html");
    ElementHandle textarea = page.querySelector("textarea");
    textarea.press("a");
    assertEquals("a", page.evaluate("() => document.querySelector('textarea').value"));
    page.evaluate("() => window.addEventListener('keydown', e => e.preventDefault(), true)");
    textarea.press("b");
    assertEquals("a", page.evaluate("() => document.querySelector('textarea').value"));
  }

  @Test
  void shouldSendACharacterWithInsertText() {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.focus("textarea");
    page.keyboard().insertText("嗨");
    assertEquals("嗨", page.evaluate("() => document.querySelector('textarea').value"));
    page.evaluate("() => window.addEventListener('keydown', e => e.preventDefault(), true)");
    page.keyboard().insertText("a");
    assertEquals("嗨a", page.evaluate("() => document.querySelector('textarea').value"));
  }

  @Test
  void insertTextShouldOnlyEmitInputEvent() {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.focus("textarea");
    JSHandle events = page.evaluateHandle("() => {\n" +
      "  const events = [];\n" +
      "  document.addEventListener('keydown', e => events.push(e.type));\n" +
      "  document.addEventListener('keyup', e => events.push(e.type));\n" +
      "  document.addEventListener('keypress', e => events.push(e.type));\n" +
      "  document.addEventListener('input', e => events.push(e.type));\n" +
      "  return events;\n" +
      "}");
    page.keyboard().insertText("hello world");
    assertEquals(asList("input"), events.jsonValue());
  }

  @Test
  void shouldReportShiftKey() {
    // Do NOT test on MacOS Firefox
    Assumptions.assumeFalse( isFirefox() && isMac);
    page.navigate(server.PREFIX + "/input/keyboard.html");
    Keyboard keyboard = page.keyboard();
    Map<String, Integer> codeForKey = new HashMap<>();
    codeForKey.put("Shift", 16);
    codeForKey.put("Alt", 18);
    codeForKey.put("Control", 17);
    codeForKey.forEach((modifierKey, modifierValue) -> {
      keyboard.down(modifierKey);
      assertEquals("Keydown: " + modifierKey + " " + modifierKey + "Left " + modifierValue + " [" + modifierKey + "]", page.evaluate("getResult()"));
      keyboard.down("!");
      if ("Shift".equals(modifierKey)) {
        assertEquals("Keydown: ! Digit1 49 [" + modifierKey + "]\nKeypress: ! Digit1 33 33 [" + modifierKey + "]", page.evaluate("getResult()"));
      } else {
        assertEquals("Keydown: ! Digit1 49 [" + modifierKey + "]", page.evaluate("getResult()"));
      }
      keyboard.up("!");
      assertEquals("Keyup: ! Digit1 49 [" + modifierKey + "]", page.evaluate("getResult()"));
      keyboard.up(modifierKey);
      assertEquals("Keyup: " + modifierKey + " " + modifierKey + "Left " + modifierValue + " []", page.evaluate("getResult()"));
    });
  }

  @Test
  void shouldReportMultipleModifiers() {
    page.navigate(server.PREFIX + "/input/keyboard.html");
    Keyboard keyboard = page.keyboard();
    keyboard.down("Control");
    assertEquals("Keydown: Control ControlLeft 17 [Control]", page.evaluate("getResult()"));
    keyboard.down("Alt");
    assertEquals("Keydown: Alt AltLeft 18 [Alt Control]", page.evaluate("getResult()"));
    keyboard.down(";");
    assertEquals("Keydown: ; Semicolon 186 [Alt Control]", page.evaluate("getResult()"));
    keyboard.up(";");
    assertEquals("Keyup: ; Semicolon 186 [Alt Control]", page.evaluate("getResult()"));
    keyboard.up("Control");
    assertEquals("Keyup: Control ControlLeft 17 [Alt]", page.evaluate("getResult()"));
    keyboard.up("Alt");
    assertEquals("Keyup: Alt AltLeft 18 []", page.evaluate("getResult()"));
  }

  @Test
  void shouldSendProperCodesWhileTyping() {
    page.navigate(server.PREFIX + "/input/keyboard.html");
    page.keyboard().type("!");
    assertEquals(String.join("\n",
      new String[] {"Keydown: ! Digit1 49 []",
        "Keypress: ! Digit1 33 33 []",
        "Keyup: ! Digit1 49 []"}),
      page.evaluate("getResult()"));
    page.keyboard().type("^");
    assertEquals(String.join("\n",
      new String[] {"Keydown: ^ Digit6 54 []",
        "Keypress: ^ Digit6 94 94 []",
        "Keyup: ^ Digit6 54 []"}),
      page.evaluate("getResult()"));
  }

  @Test
  void shouldSendProperCodesWhileTypingWithShift() {
    page.navigate(server.PREFIX + "/input/keyboard.html");
    Keyboard keyboard = page.keyboard();
    keyboard.down("Shift");
    page.keyboard().type("~");
    assertEquals(String.join("\n",
      new String[]  {"Keydown: Shift ShiftLeft 16 [Shift]",
        "Keydown: ~ Backquote 192 [Shift]", // 192 is ` keyCode
        "Keypress: ~ Backquote 126 126 [Shift]", // 126 is ~ charCode
        "Keyup: ~ Backquote 192 [Shift]"}),
      page.evaluate("getResult()"));
    keyboard.up("Shift");
  }

  @Test
  void shouldNotTypeCanceledEvents() {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.focus("textarea");
    page.evaluate("() => {\n" +
      "  window.addEventListener('keydown', event => {\n" +
      "    event.stopPropagation();\n" +
      "    event.stopImmediatePropagation();\n" +
      "    if (event.key === 'l')\n" +
      "      event.preventDefault();\n" +
      "    if (event.key === 'o')\n" +
      "      event.preventDefault();\n" +
      "  }, false);\n" +
      "}");
    page.keyboard().type("Hello World!");
    assertEquals("He Wrd!", page.evalOnSelector("textarea", "textarea => textarea.value"));
  }

  @Test
  void shouldPressPlus() {
    page.navigate(server.PREFIX + "/input/keyboard.html");
    page.keyboard().press("+");
    assertEquals(String.join("\n",
      new String[] {"Keydown: + Equal 187 []", // 192 is ` keyCode
        "Keypress: + Equal 43 43 []", // 126 is ~ charCode
        "Keyup: + Equal 187 []"}),
        page.evaluate("getResult()"));
  }

  @Test
  void shouldPressShiftPlus() {
    page.navigate(server.PREFIX + "/input/keyboard.html");
    page.keyboard().press("Shift++");
    assertEquals(String.join("\n",
      new String[] {"Keydown: Shift ShiftLeft 16 [Shift]",
        "Keydown: + Equal 187 [Shift]", // 192 is ` keyCode
        "Keypress: + Equal 43 43 [Shift]", // 126 is ~ charCode
        "Keyup: + Equal 187 [Shift]",
        "Keyup: Shift ShiftLeft 16 []"}),
      page.evaluate("getResult()"));
  }

  @Test
  void shouldSupportPlusSeparatedModifier(){
    page.navigate(server.PREFIX + "/input/keyboard.html");
    page.keyboard().press("Shift+~");
    assertEquals(String.join("\n",
      new String[] {"Keydown: Shift ShiftLeft 16 [Shift]",
        "Keydown: ~ Backquote 192 [Shift]", // 192 is ` keyCode
        "Keypress: ~ Backquote 126 126 [Shift]", // 126 is ~ charCode
        "Keyup: ~ Backquote 192 [Shift]",
        "Keyup: Shift ShiftLeft 16 []"}),
      page.evaluate("getResult()"));
  }

  @Test
  void shouldSupportMultiplePlusSeparatedModifier() {
    page.navigate(server.PREFIX + "/input/keyboard.html");
    page.keyboard().press("Control+Shift+~");
    assertEquals(String.join("\n",
      new String[] {"Keydown: Control ControlLeft 17 [Control]",
        "Keydown: Shift ShiftLeft 16 [Control Shift]",
        "Keydown: ~ Backquote 192 [Control Shift]", // 192 is ` keyCode
        "Keyup: ~ Backquote 192 [Control Shift]",
        "Keyup: Shift ShiftLeft 16 [Control]",
        "Keyup: Control ControlLeft 17 []"}),
      page.evaluate("getResult()"));
  }

  @Test
  void shouldShiftRawCodes() {
    page.navigate(server.PREFIX + "/input/keyboard.html");
    page.keyboard().press("Shift+Digit3");
    assertEquals(String.join("\n",
      new String[] {"Keydown: Shift ShiftLeft 16 [Shift]",
        "Keydown: # Digit3 51 [Shift]", // 51 is # keyCode
        "Keypress: # Digit3 35 35 [Shift]", // 35 is # charCode
        "Keyup: # Digit3 51 [Shift]",
        "Keyup: Shift ShiftLeft 16 []"}),
      page.evaluate("getResult()"));
  }

  @Test
  void shouldSpecifyRepeatProperty() {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.focus("textarea");
    JSHandle lastEvent = captureLastKeyDown();
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

  @Test
  void shouldTypeAllKindsOfCharacters() {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.focus("textarea");
    final String text = "This text goes onto two lines.\\nThis character is 嗨.";
    page.keyboard().type(text);
    assertEquals(text, page.evalOnSelector("textarea", "t => t.value"));
  }

  @Test
  void shouldSpecifyLocation() {
    page.navigate(server.PREFIX + "/input/textarea.html");
    JSHandle lastEvent = captureLastKeyDown();
    ElementHandle textarea = page.querySelector("textarea");
    textarea.press("Digit5");
    assertEquals(0, lastEvent.evaluate("e => e.location"));
    textarea.press("ControlLeft");
    assertEquals(1, lastEvent.evaluate("e => e.location"));
    textarea.press("ControlRight");
    assertEquals(2, lastEvent.evaluate("e => e.location"));
    textarea.press("NumpadSubtract");
    assertEquals(3, lastEvent.evaluate("e => e.location"));
  }

  @Test
  void shouldPressEnter() {
    page.setContent("<textarea></textarea>");
    page.focus("textarea");
    JSHandle lastEvent = captureLastKeyDown();
    testEnterKey(lastEvent, "Enter", "Enter", "Enter");
    testEnterKey(lastEvent, "NumpadEnter", "Enter", "NumpadEnter");
    testEnterKey(lastEvent, "\n", "Enter", "Enter");
    testEnterKey(lastEvent,"\r", "Enter", "Enter");
  }

  @Test
  void shouldThrowOnUnknownKeys() {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.keyboard().press("NotARealKey"));
    assertTrue(e.getMessage().contains("Unknown key: \"NotARealKey\""), "Expecting Exception: " + e.getMessage() + " contain: Unknown key: \"NotARealKey\"");

    e = assertThrows(PlaywrightException.class, () -> page.keyboard().press("ё"));
    assertTrue(e.getMessage().contains("Unknown key: \"ё\""), "Expecting Exception: " + e.getMessage() + " contain: Unknown key: \"ё\"");

    e = assertThrows(PlaywrightException.class, () -> page.keyboard().press("😊"));
    assertTrue(e.getMessage().contains("Unknown key: \"😊\""), "Expecting Exception: " + e.getMessage() + " contain: Unknown key: \"😊\"");
  }

  @Test
  void shouldTypeEmoji() {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.type("textarea", "👹 Tokyo street Japan 🇯🇵");
    assertEquals("👹 Tokyo street Japan 🇯🇵", page.evalOnSelector("textarea", "textarea => textarea.value"));
  }

  @Test
  void shouldTypeEmojiIntoAnIframe() {
    page.navigate(server.EMPTY_PAGE);
    attachFrame(page, "emoji-test", server.PREFIX + "/input/textarea.html");
    Frame frame = page.frames().get(1);
    ElementHandle textarea = frame.querySelector("textarea");
    textarea.type("👹 Tokyo street Japan 🇯🇵");
    assertEquals("👹 Tokyo street Japan 🇯🇵", frame.evalOnSelector("textarea", "textarea => textarea.value"));
  }

  @Test
  void shouldHandleSelectAll() {
    page.navigate(server.PREFIX + "/input/textarea.html");
    ElementHandle textarea = page.querySelector("textarea");
    textarea.type("some text");
    String modifier = isMac ? "Meta" : "Control";
    page.keyboard().down(modifier);
    page.keyboard().press("a");
    page.keyboard().up(modifier);
    page.keyboard().press("Backspace");
    assertTrue(((String)page.evalOnSelector("textarea", "textarea => textarea.value")).isEmpty());
  }

  @Test
  void shouldBeAbleToPreventSelectAll() {
    page.navigate(server.PREFIX + "/input/textarea.html");
    ElementHandle textarea = page.querySelector("textarea");
    textarea.type("some text");
    page.evalOnSelector("textarea", "textarea => {\n" +
      "  textarea.addEventListener('keydown', event => {\n" +
      "    if (event.key === 'a' && (event.metaKey || event.ctrlKey))\n" +
      "      event.preventDefault();\n" +
      "  }, false);\n" +
      "}");
    String modifier = isMac ? "Meta" : "Control";
    page.keyboard().down(modifier);
    page.keyboard().press("a");
    page.keyboard().up(modifier);
    page.keyboard().press("Backspace");
    assertEquals("some tex", page.evalOnSelector("textarea", "textarea => textarea.value"));
  }

  @Test
  void shouldSupportMacOSShortcuts() {
    // Test for MacOS only
    Assumptions.assumeTrue(isMac);
    // @see https://github.com/microsoft/playwright/issues/5721
    Assumptions.assumeFalse(isFirefox());
    page.navigate(server.PREFIX + "/input/textarea.html");
    ElementHandle textarea = page.querySelector("textarea");
    textarea.type("some text");
    // select one word backwards
    page.keyboard().press("Shift+Control+Alt+KeyB");
    page.keyboard().press("Backspace");
    assertEquals("some ", page.evalOnSelector("textarea", "textarea => textarea.value"));
  }

  @Test
  void shouldPressMetaKey() {
    JSHandle lastEvent = captureLastKeyDown();
    page.keyboard().press("Meta");
    LinkedHashMap eventData = (LinkedHashMap) lastEvent.jsonValue();
    if (isFirefox() && !isMac) {
      assertEquals("OS", eventData.get("key"));
    } else {
      assertEquals("Meta", eventData.get("key"));
    }
    assertEquals("MetaLeft", eventData.get("code"));
    if (isFirefox() && !isMac) {
      assertFalse((Boolean) eventData.get("metaKey"), eventData.toString());
    } else {
      assertTrue((Boolean) eventData.get("metaKey"), eventData.toString());
    }
  }

  @Test
  void shouldWorkAfterACrossOriginNavigation() {
    page.navigate(server.PREFIX + "/empty.html");
    page.navigate(server.CROSS_PROCESS_PREFIX + "/empty.html");
    JSHandle lastEvent = captureLastKeyDown();
    page.keyboard().press("a");
    assertEquals("a", lastEvent.evaluate("l => l.key"));
  }

  @Test
  void shouldExposeKeyIdentifierInWebKit() {
    // Test for webkit only
    Assumptions.assumeTrue(isWebKit());
    JSHandle lastEvent = captureLastKeyDown();
    Map<String, String> keyMap = new HashMap<>();
    keyMap.put("ArrowUp", "Up");
    keyMap.put("ArrowDown", "Down");
    keyMap.put("ArrowLeft", "Left");
    keyMap.put("ArrowRight", "Right");
    keyMap.put("Backspace", "U+0008");
    keyMap.put("Tab", "U+0009");
    keyMap.put("Delete", "U+007F");
    keyMap.put("a", "U+0041");
    keyMap.put("b", "U+0042");
    keyMap.put("F12", "F12");
    keyMap.forEach((key, keyIdentifier) -> {
      page.keyboard().press(key);
      assertEquals(keyIdentifier, lastEvent.evaluate("e => e.keyIdentifier"));
    });
  }

  @Test
  void shouldScrollWithPageDown() {
    page.navigate(server.PREFIX + "/input/scrollable.html");
    // A click is required for WebKit to send the event into the body.
    page.click("body");
    page.keyboard().press("PageDown");
    // We can't wait for the scroll to finish, so just wait for it to start.
    page.waitForFunction("() => scrollY > 0");
  }

  private JSHandle captureLastKeyDown() {
    JSHandle lastEvent = page.evaluateHandle("() => {\n" +
      "  const lastEvent = {\n" +
      "    repeat: false,\n" +
      "    location: -1,\n" +
      "    code: '',\n" +
      "    key: '',\n" +
      "    metaKey: false,\n" +
      "    keyIdentifier: 'unsupported'\n" +
      "  };\n" +
      "  document.addEventListener('keydown', e => {\n" +
      "    lastEvent.repeat = e.repeat;\n" +
      "    lastEvent.location = e.location;\n" +
      "    lastEvent.key = e.key;\n" +
      "    lastEvent.code = e.code;\n" +
      "    lastEvent.metaKey = e.metaKey;\n" +
      "    // keyIdentifier only exists in WebKit, and isn't in TypeScript's lib.\n" +
      "    lastEvent.keyIdentifier = 'keyIdentifier' in e && e['keyIdentifier'];\n" +
      "  }, true);\n" +
      "  return lastEvent;\n" +
      "}");
    return lastEvent;
  }

  private void testEnterKey(JSHandle lastEventHandle, String key, String expectedKey, String expectedCode) {
    page.keyboard().press(key);
    LinkedHashMap lastEvent = (LinkedHashMap) lastEventHandle.jsonValue();
    assertEquals(expectedKey, lastEvent.get("key"), lastEvent.toString());
    assertEquals(expectedCode, lastEvent.get("code"), String.join(",", lastEvent.values().toString()));
    String value = (String) page.evalOnSelector("textarea", "t => t.value");
    assertEquals("\n", value);
    page.evalOnSelector("textarea", "t => t.value = ''");
  }

}
