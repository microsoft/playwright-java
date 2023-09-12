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

import com.microsoft.playwright.options.*;

/**
 * Keyboard provides an api for managing a virtual keyboard. The high level api is {@link Keyboard#type Keyboard.type()},
 * which takes raw characters and generates proper {@code keydown}, {@code keypress}/{@code input}, and {@code keyup}
 * events on your page.
 *
 * <p> For finer control, you can use {@link Keyboard#down Keyboard.down()}, {@link Keyboard#up Keyboard.up()}, and {@link
 * Keyboard#insertText Keyboard.insertText()} to manually fire events as if they were generated from a real keyboard.
 *
 * <p> An example of holding down {@code Shift} in order to select and delete some text:
 * <pre>{@code
 * page.keyboard().type("Hello World!");
 * page.keyboard().press("ArrowLeft");
 * page.keyboard().down("Shift");
 * for (int i = 0; i < " World".length(); i++)
 *   page.keyboard().press("ArrowLeft");
 * page.keyboard().up("Shift");
 * page.keyboard().press("Backspace");
 * // Result text will end up saying "Hello!"
 * }</pre>
 *
 * <p> An example of pressing uppercase {@code A}
 * <pre>{@code
 * page.keyboard().press("Shift+KeyA");
 * // or
 * page.keyboard().press("Shift+A");
 * }</pre>
 *
 * <p> An example to trigger select-all with the keyboard
 * <pre>{@code
 * // on Windows and Linux
 * page.keyboard().press("Control+A");
 * // on macOS
 * page.keyboard().press("Meta+A");
 * }</pre>
 */
public interface Keyboard {
  class PressOptions {
    /**
     * Time to wait between {@code keydown} and {@code keyup} in milliseconds. Defaults to 0.
     */
    public Double delay;

    /**
     * Time to wait between {@code keydown} and {@code keyup} in milliseconds. Defaults to 0.
     */
    public PressOptions setDelay(double delay) {
      this.delay = delay;
      return this;
    }
  }
  class TypeOptions {
    /**
     * Time to wait between key presses in milliseconds. Defaults to 0.
     */
    public Double delay;

    /**
     * Time to wait between key presses in milliseconds. Defaults to 0.
     */
    public TypeOptions setDelay(double delay) {
      this.delay = delay;
      return this;
    }
  }
  /**
   * Dispatches a {@code keydown} event.
   *
   * <p> {@code key} can specify the intended <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key">keyboardEvent.key</a> value or a single
   * character to generate the text for. A superset of the {@code key} values can be found <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key/Key_Values">here</a>. Examples of the keys are:
   *
   * <p> {@code F1} - {@code F12}, {@code Digit0}- {@code Digit9}, {@code KeyA}- {@code KeyZ}, {@code Backquote}, {@code Minus},
   * {@code Equal}, {@code Backslash}, {@code Backspace}, {@code Tab}, {@code Delete}, {@code Escape}, {@code ArrowDown},
   * {@code End}, {@code Enter}, {@code Home}, {@code Insert}, {@code PageDown}, {@code PageUp}, {@code ArrowRight}, {@code
   * ArrowUp}, etc.
   *
   * <p> Following modification shortcuts are also supported: {@code Shift}, {@code Control}, {@code Alt}, {@code Meta}, {@code
   * ShiftLeft}.
   *
   * <p> Holding down {@code Shift} will type the text that corresponds to the {@code key} in the upper case.
   *
   * <p> If {@code key} is a single character, it is case-sensitive, so the values {@code a} and {@code A} will generate
   * different respective texts.
   *
   * <p> If {@code key} is a modifier key, {@code Shift}, {@code Meta}, {@code Control}, or {@code Alt}, subsequent key presses
   * will be sent with that modifier active. To release the modifier key, use {@link Keyboard#up Keyboard.up()}.
   *
   * <p> After the key is pressed once, subsequent calls to {@link Keyboard#down Keyboard.down()} will have <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/repeat">repeat</a> set to true. To release the key,
   * use {@link Keyboard#up Keyboard.up()}.
   *
   * <p> <strong>NOTE:</strong> Modifier keys DO influence {@code keyboard.down}. Holding down {@code Shift} will type the text in upper case.
   *
   * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
   * @since v1.8
   */
  void down(String key);
  /**
   * Dispatches only {@code input} event, does not emit the {@code keydown}, {@code keyup} or {@code keypress} events.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.keyboard().insertText("嗨");
   * }</pre>
   *
   * <p> <strong>NOTE:</strong> Modifier keys DO NOT effect {@code keyboard.insertText}. Holding down {@code Shift} will not type the text in upper
   * case.
   *
   * @param text Sets input to the specified text value.
   * @since v1.8
   */
  void insertText(String text);
  /**
   * <strong>NOTE:</strong> In most cases, you should use {@link Locator#press Locator.press()} instead.
   *
   * <p> {@code key} can specify the intended <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key">keyboardEvent.key</a> value or a single
   * character to generate the text for. A superset of the {@code key} values can be found <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key/Key_Values">here</a>. Examples of the keys are:
   *
   * <p> {@code F1} - {@code F12}, {@code Digit0}- {@code Digit9}, {@code KeyA}- {@code KeyZ}, {@code Backquote}, {@code Minus},
   * {@code Equal}, {@code Backslash}, {@code Backspace}, {@code Tab}, {@code Delete}, {@code Escape}, {@code ArrowDown},
   * {@code End}, {@code Enter}, {@code Home}, {@code Insert}, {@code PageDown}, {@code PageUp}, {@code ArrowRight}, {@code
   * ArrowUp}, etc.
   *
   * <p> Following modification shortcuts are also supported: {@code Shift}, {@code Control}, {@code Alt}, {@code Meta}, {@code
   * ShiftLeft}.
   *
   * <p> Holding down {@code Shift} will type the text that corresponds to the {@code key} in the upper case.
   *
   * <p> If {@code key} is a single character, it is case-sensitive, so the values {@code a} and {@code A} will generate
   * different respective texts.
   *
   * <p> Shortcuts such as {@code key: "Control+o"} or {@code key: "Control+Shift+T"} are supported as well. When specified with
   * the modifier, modifier is pressed and being held while the subsequent key is being pressed.
   *
   * <p> **Usage**
   * <pre>{@code
   * Page page = browser.newPage();
   * page.navigate("https://keycode.info");
   * page.keyboard().press("A");
   * page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("A.png"));
   * page.keyboard().press("ArrowLeft");
   * page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("ArrowLeft.png")));
   * page.keyboard().press("Shift+O");
   * page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("O.png")));
   * browser.close();
   * }</pre>
   *
   * <p> Shortcut for {@link Keyboard#down Keyboard.down()} and {@link Keyboard#up Keyboard.up()}.
   *
   * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
   * @since v1.8
   */
  default void press(String key) {
    press(key, null);
  }
  /**
   * <strong>NOTE:</strong> In most cases, you should use {@link Locator#press Locator.press()} instead.
   *
   * <p> {@code key} can specify the intended <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key">keyboardEvent.key</a> value or a single
   * character to generate the text for. A superset of the {@code key} values can be found <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key/Key_Values">here</a>. Examples of the keys are:
   *
   * <p> {@code F1} - {@code F12}, {@code Digit0}- {@code Digit9}, {@code KeyA}- {@code KeyZ}, {@code Backquote}, {@code Minus},
   * {@code Equal}, {@code Backslash}, {@code Backspace}, {@code Tab}, {@code Delete}, {@code Escape}, {@code ArrowDown},
   * {@code End}, {@code Enter}, {@code Home}, {@code Insert}, {@code PageDown}, {@code PageUp}, {@code ArrowRight}, {@code
   * ArrowUp}, etc.
   *
   * <p> Following modification shortcuts are also supported: {@code Shift}, {@code Control}, {@code Alt}, {@code Meta}, {@code
   * ShiftLeft}.
   *
   * <p> Holding down {@code Shift} will type the text that corresponds to the {@code key} in the upper case.
   *
   * <p> If {@code key} is a single character, it is case-sensitive, so the values {@code a} and {@code A} will generate
   * different respective texts.
   *
   * <p> Shortcuts such as {@code key: "Control+o"} or {@code key: "Control+Shift+T"} are supported as well. When specified with
   * the modifier, modifier is pressed and being held while the subsequent key is being pressed.
   *
   * <p> **Usage**
   * <pre>{@code
   * Page page = browser.newPage();
   * page.navigate("https://keycode.info");
   * page.keyboard().press("A");
   * page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("A.png"));
   * page.keyboard().press("ArrowLeft");
   * page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("ArrowLeft.png")));
   * page.keyboard().press("Shift+O");
   * page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("O.png")));
   * browser.close();
   * }</pre>
   *
   * <p> Shortcut for {@link Keyboard#down Keyboard.down()} and {@link Keyboard#up Keyboard.up()}.
   *
   * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
   * @since v1.8
   */
  void press(String key, PressOptions options);
  /**
   * <strong>NOTE:</strong> In most cases, you should use {@link Locator#fill Locator.fill()} instead. You only need to press keys one by one if
   * there is special keyboard handling on the page - in this case use {@link Locator#pressSequentially
   * Locator.pressSequentially()}.
   *
   * <p> Sends a {@code keydown}, {@code keypress}/{@code input}, and {@code keyup} event for each character in the text.
   *
   * <p> To press a special key, like {@code Control} or {@code ArrowDown}, use {@link Keyboard#press Keyboard.press()}.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Types instantly
   * page.keyboard().type("Hello");
   * // Types slower, like a user
   * page.keyboard().type("World", new Keyboard.TypeOptions().setDelay(100));
   * }</pre>
   *
   * <p> <strong>NOTE:</strong> Modifier keys DO NOT effect {@code keyboard.type}. Holding down {@code Shift} will not type the text in upper case.
   *
   * <p> <strong>NOTE:</strong> For characters that are not on a US keyboard, only an {@code input} event will be sent.
   *
   * @param text A text to type into a focused element.
   * @since v1.8
   */
  default void type(String text) {
    type(text, null);
  }
  /**
   * <strong>NOTE:</strong> In most cases, you should use {@link Locator#fill Locator.fill()} instead. You only need to press keys one by one if
   * there is special keyboard handling on the page - in this case use {@link Locator#pressSequentially
   * Locator.pressSequentially()}.
   *
   * <p> Sends a {@code keydown}, {@code keypress}/{@code input}, and {@code keyup} event for each character in the text.
   *
   * <p> To press a special key, like {@code Control} or {@code ArrowDown}, use {@link Keyboard#press Keyboard.press()}.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Types instantly
   * page.keyboard().type("Hello");
   * // Types slower, like a user
   * page.keyboard().type("World", new Keyboard.TypeOptions().setDelay(100));
   * }</pre>
   *
   * <p> <strong>NOTE:</strong> Modifier keys DO NOT effect {@code keyboard.type}. Holding down {@code Shift} will not type the text in upper case.
   *
   * <p> <strong>NOTE:</strong> For characters that are not on a US keyboard, only an {@code input} event will be sent.
   *
   * @param text A text to type into a focused element.
   * @since v1.8
   */
  void type(String text, TypeOptions options);
  /**
   * Dispatches a {@code keyup} event.
   *
   * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
   * @since v1.8
   */
  void up(String key);
}

