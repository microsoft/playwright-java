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

import java.nio.file.Path;
import java.util.*;

/**
 * ElementHandle represents an in-page DOM element. ElementHandles can be created with the page.$ method.
 * <p>
 * ElementHandle prevents DOM element from garbage collection unless the handle is disposed. ElementHandles are auto-disposed when their origin frame gets navigated.
 * <p>
 * ElementHandle instances can be used as an argument in {@code page.$eval()} and {@code page.evaluate()} methods.
 */
public interface ElementHandle extends JSHandle {
  class BoundingBox {
    public double x;
    public double y;
    public double width;
    public double height;
  }

  class SelectOption {
    public String value;
    public String label;
    public Integer index;

    public SelectOption withValue(String value) {
      this.value = value;
      return this;
    }
    public SelectOption withLabel(String label) {
      this.label = label;
      return this;
    }
    public SelectOption withIndex(int index) {
      this.index = index;
      return this;
    }
  }

  enum ElementState { DISABLED, ENABLED, HIDDEN, STABLE, VISIBLE }
  class CheckOptions {
    public Boolean force;
    public Boolean noWaitAfter;
    public Integer timeout;

    public CheckOptions withForce(Boolean force) {
      this.force = force;
      return this;
    }
    public CheckOptions withNoWaitAfter(Boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public CheckOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class ClickOptions {
    public Mouse.Button button;
    public Integer clickCount;
    public Integer delay;
    public Position position;
    public Set<Keyboard.Modifier> modifiers;
    public Boolean force;
    public Boolean noWaitAfter;
    public Integer timeout;

    public ClickOptions withButton(Mouse.Button button) {
      this.button = button;
      return this;
    }
    public ClickOptions withClickCount(Integer clickCount) {
      this.clickCount = clickCount;
      return this;
    }
    public ClickOptions withDelay(Integer delay) {
      this.delay = delay;
      return this;
    }
    public ClickOptions withPosition(Position position) {
      this.position = position;
      return this;
    }
    public ClickOptions withPosition(int x, int y) {
      return withPosition(new Position(x, y));
    }
    public ClickOptions withModifiers(Keyboard.Modifier... modifiers) {
      this.modifiers = new HashSet<>(Arrays.asList(modifiers));
      return this;
    }
    public ClickOptions withForce(Boolean force) {
      this.force = force;
      return this;
    }
    public ClickOptions withNoWaitAfter(Boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public ClickOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class DblclickOptions {
    public Mouse.Button button;
    public Integer delay;
    public Position position;
    public Set<Keyboard.Modifier> modifiers;
    public Boolean force;
    public Boolean noWaitAfter;
    public Integer timeout;

    public DblclickOptions withButton(Mouse.Button button) {
      this.button = button;
      return this;
    }
    public DblclickOptions withDelay(Integer delay) {
      this.delay = delay;
      return this;
    }
    public DblclickOptions withPosition(Position position) {
      this.position = position;
      return this;
    }
    public DblclickOptions withPosition(int x, int y) {
      return withPosition(new Position(x, y));
    }
    public DblclickOptions withModifiers(Keyboard.Modifier... modifiers) {
      this.modifiers = new HashSet<>(Arrays.asList(modifiers));
      return this;
    }
    public DblclickOptions withForce(Boolean force) {
      this.force = force;
      return this;
    }
    public DblclickOptions withNoWaitAfter(Boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public DblclickOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class FillOptions {
    public Boolean noWaitAfter;
    public Integer timeout;

    public FillOptions withNoWaitAfter(Boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public FillOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class HoverOptions {
    public Position position;
    public Set<Keyboard.Modifier> modifiers;
    public Boolean force;
    public Integer timeout;

    public HoverOptions withPosition(Position position) {
      this.position = position;
      return this;
    }
    public HoverOptions withPosition(int x, int y) {
      return withPosition(new Position(x, y));
    }
    public HoverOptions withModifiers(Keyboard.Modifier... modifiers) {
      this.modifiers = new HashSet<>(Arrays.asList(modifiers));
      return this;
    }
    public HoverOptions withForce(Boolean force) {
      this.force = force;
      return this;
    }
    public HoverOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class PressOptions {
    public Integer delay;
    public Boolean noWaitAfter;
    public Integer timeout;

    public PressOptions withDelay(Integer delay) {
      this.delay = delay;
      return this;
    }
    public PressOptions withNoWaitAfter(Boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public PressOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class ScreenshotOptions {
    public enum Type { JPEG, PNG }
    public Path path;
    public Type type;
    public Integer quality;
    public Boolean omitBackground;
    public Integer timeout;

    public ScreenshotOptions withPath(Path path) {
      this.path = path;
      return this;
    }
    public ScreenshotOptions withType(Type type) {
      this.type = type;
      return this;
    }
    public ScreenshotOptions withQuality(Integer quality) {
      this.quality = quality;
      return this;
    }
    public ScreenshotOptions withOmitBackground(Boolean omitBackground) {
      this.omitBackground = omitBackground;
      return this;
    }
    public ScreenshotOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class ScrollIntoViewIfNeededOptions {
    public Integer timeout;

    public ScrollIntoViewIfNeededOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class SelectOptionOptions {
    public Boolean noWaitAfter;
    public Integer timeout;

    public SelectOptionOptions withNoWaitAfter(Boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public SelectOptionOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class SelectTextOptions {
    public Integer timeout;

    public SelectTextOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class SetInputFilesOptions {
    public Boolean noWaitAfter;
    public Integer timeout;

    public SetInputFilesOptions withNoWaitAfter(Boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public SetInputFilesOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class TapOptions {
    public class Position {
      public int x;
      public int y;

      Position() {
      }
      public TapOptions done() {
        return TapOptions.this;
      }

      public Position withX(int x) {
        this.x = x;
        return this;
      }
      public Position withY(int y) {
        this.y = y;
        return this;
      }
    }
    public Position position;
    public Set<Keyboard.Modifier> modifiers;
    public Boolean force;
    public Boolean noWaitAfter;
    public Integer timeout;

    public Position setPosition() {
      this.position = new Position();
      return this.position;
    }
    public TapOptions withModifiers(Keyboard.Modifier... modifiers) {
      this.modifiers = new HashSet<>(Arrays.asList(modifiers));
      return this;
    }
    public TapOptions withForce(Boolean force) {
      this.force = force;
      return this;
    }
    public TapOptions withNoWaitAfter(Boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public TapOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class TypeOptions {
    public Integer delay;
    public Boolean noWaitAfter;
    public Integer timeout;

    public TypeOptions withDelay(Integer delay) {
      this.delay = delay;
      return this;
    }
    public TypeOptions withNoWaitAfter(Boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public TypeOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class UncheckOptions {
    public Boolean force;
    public Boolean noWaitAfter;
    public Integer timeout;

    public UncheckOptions withForce(Boolean force) {
      this.force = force;
      return this;
    }
    public UncheckOptions withNoWaitAfter(Boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public UncheckOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForElementStateOptions {
    public Integer timeout;

    public WaitForElementStateOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForSelectorOptions {
    public enum State { ATTACHED, DETACHED, HIDDEN, VISIBLE }
    public State state;
    public Integer timeout;

    public WaitForSelectorOptions withState(State state) {
      this.state = state;
      return this;
    }
    public WaitForSelectorOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  /**
   * The method finds an element matching the specified selector in the {@code ElementHandle}'s subtree. See Working with selectors for more details. If no elements match the selector, the return value resolves to {@code null}.
   * @param selector A selector to query element for. See working with selectors for more details.
   */
  ElementHandle querySelector(String selector);
  /**
   * The method finds all elements matching the specified selector in the {@code ElementHandle}s subtree. See Working with selectors for more details. If no elements match the selector, the return value resolves to {@code []}.
   * @param selector A selector to query element for. See working with selectors for more details.
   */
  List<ElementHandle> querySelectorAll(String selector);
  default Object evalOnSelector(String selector, String pageFunction) {
    return evalOnSelector(selector, pageFunction, null);
  }
  /**
   * The method finds an element matching the specified selector in the {@code ElementHandle}s subtree and passes it as a first argument to {@code pageFunction}. See Working with selectors for more details. If no elements match the selector, the method throws an error.
   * <p>
   * If {@code pageFunction} returns a Promise, then {@code frame.$eval} would wait for the promise to resolve and return its value.
   * <p>
   * Examples:
   * <p>
   * 
   * @param selector A selector to query element for. See working with selectors for more details.
   * @param pageFunction Function to be evaluated in browser context
   * @param arg Optional argument to pass to {@code pageFunction}
   * @return Promise which resolves to the return value of {@code pageFunction}
   */
  Object evalOnSelector(String selector, String pageFunction, Object arg);
  default Object evalOnSelectorAll(String selector, String pageFunction) {
    return evalOnSelectorAll(selector, pageFunction, null);
  }
  /**
   * The method finds all elements matching the specified selector in the {@code ElementHandle}'s subtree and passes an array of matched elements as a first argument to {@code pageFunction}. See Working with selectors for more details.
   * <p>
   * If {@code pageFunction} returns a Promise, then {@code frame.$$eval} would wait for the promise to resolve and return its value.
   * <p>
   * Examples:
   * <p>
   * 
   * @param selector A selector to query element for. See working with selectors for more details.
   * @param pageFunction Function to be evaluated in browser context
   * @param arg Optional argument to pass to {@code pageFunction}
   * @return Promise which resolves to the return value of {@code pageFunction}
   */
  Object evalOnSelectorAll(String selector, String pageFunction, Object arg);
  /**
   * This method returns the bounding box of the element (relative to the main frame), or {@code null} if the element is not visible.
   */
  BoundingBox boundingBox();
  default void check() {
    check(null);
  }
  /**
   * This method checks the element by performing the following steps:
   * <p>
   * Ensure that element is a checkbox or a radio input. If not, this method rejects. If the element is already checked, this method returns immediately.
   * <p>
   * Wait for actionability checks on the element, unless {@code force} option is set.
   * <p>
   * Scroll the element into view if needed.
   * <p>
   * Use page.mouse to click in the center of the element.
   * <p>
   * Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   * <p>
   * Ensure that the element is now checked. If not, this method rejects.
   * <p>
   * If the element is detached from the DOM at any moment during the action, this method rejects.
   * <p>
   * When all steps combined have not finished during the specified {@code timeout}, this method rejects with a TimeoutError. Passing zero timeout disables this.
   * @return Promise that resolves when the element is successfully checked.
   */
  void check(CheckOptions options);
  default void click() {
    click(null);
  }
  /**
   * This method clicks the element by performing the following steps:
   * <p>
   * Wait for actionability checks on the element, unless {@code force} option is set.
   * <p>
   * Scroll the element into view if needed.
   * <p>
   * Use page.mouse to click in the center of the element, or the specified {@code position}.
   * <p>
   * Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   * <p>
   * If the element is detached from the DOM at any moment during the action, this method rejects.
   * <p>
   * When all steps combined have not finished during the specified {@code timeout}, this method rejects with a TimeoutError. Passing zero timeout disables this.
   * @return Promise that resolves when the element is successfully clicked.
   */
  void click(ClickOptions options);
  /**
   * 
   * @return Resolves to the content frame for element handles referencing iframe nodes, or {@code null} otherwise
   */
  Frame contentFrame();
  default void dblclick() {
    dblclick(null);
  }
  /**
   * This method double clicks the element by performing the following steps:
   * <p>
   * Wait for actionability checks on the element, unless {@code force} option is set.
   * <p>
   * Scroll the element into view if needed.
   * <p>
   * Use page.mouse to double click in the center of the element, or the specified {@code position}.
   * <p>
   * Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set. Note that if the first click of the {@code dblclick()} triggers a navigation event, this method will reject.
   * <p>
   * If the element is detached from the DOM at any moment during the action, this method rejects.
   * <p>
   * When all steps combined have not finished during the specified {@code timeout}, this method rejects with a TimeoutError. Passing zero timeout disables this.
   * <p>
   * <strong>NOTE</strong> {@code elementHandle.dblclick()} dispatches two {@code click} events and a single {@code dblclick} event.
   * @return Promise that resolves when the element is successfully double clicked.
   */
  void dblclick(DblclickOptions options);
  default void dispatchEvent(String type) {
    dispatchEvent(type, null);
  }
  /**
   * The snippet below dispatches the {@code click} event on the element. Regardless of the visibility state of the elment, {@code click} is dispatched. This is equivalend to calling {@code element.click()}.
   * <p>
   * Under the hood, it creates an instance of an event based on the given {@code type}, initializes it with {@code eventInit} properties and dispatches it on the element. Events are {@code composed}, {@code cancelable} and bubble by default.
   * <p>
   * Since {@code eventInit} is event-specific, please refer to the events documentation for the lists of initial properties:
   * <p>
   * DragEvent
   * <p>
   * FocusEvent
   * <p>
   * KeyboardEvent
   * <p>
   * MouseEvent
   * <p>
   * PointerEvent
   * <p>
   * TouchEvent
   * <p>
   * Event
   * <p>
   * You can also specify {@code JSHandle} as the property value if you want live objects to be passed into the event:
   * <p>
   * 
   * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
   * @param eventInit event-specific initialization properties.
   */
  void dispatchEvent(String type, Object eventInit);
  default void fill(String value) {
    fill(value, null);
  }
  /**
   * This method waits for actionability checks, focuses the element, fills it and triggers an {@code input} event after filling.
   * <p>
   * If the element is not an {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element, this method throws an error.
   * <p>
   * Note that you can pass an empty string to clear the input field.
   * @param value Value to set for the {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element.
   */
  void fill(String value, FillOptions options);
  /**
   * Calls focus on the element.
   */
  void focus();
  /**
   * Returns element attribute value.
   * @param name Attribute name to get the value for.
   */
  String getAttribute(String name);
  default void hover() {
    hover(null);
  }
  /**
   * This method hovers over the element by performing the following steps:
   * <p>
   * Wait for actionability checks on the element, unless {@code force} option is set.
   * <p>
   * Scroll the element into view if needed.
   * <p>
   * Use page.mouse to hover over the center of the element, or the specified {@code position}.
   * <p>
   * Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   * <p>
   * If the element is detached from the DOM at any moment during the action, this method rejects.
   * <p>
   * When all steps combined have not finished during the specified {@code timeout}, this method rejects with a TimeoutError. Passing zero timeout disables this.
   * @return Promise that resolves when the element is successfully hovered.
   */
  void hover(HoverOptions options);
  /**
   * 
   * @return Resolves to the {@code element.innerHTML}.
   */
  String innerHTML();
  /**
   * 
   * @return Resolves to the {@code element.innerText}.
   */
  String innerText();
  /**
   * 
   * @return Returns the frame containing the given element.
   */
  Frame ownerFrame();
  default void press(String key) {
    press(key, null);
  }
  /**
   * Focuses the element, and then uses {@code keyboard.down} and {@code keyboard.up}.
   * <p>
   * {@code key} can specify the intended keyboardEvent.key value or a single character to generate the text for. A superset of the {@code key} values can be found here. Examples of the keys are:
   * <p>
   * {@code F1} - {@code F12}, {@code Digit0}- {@code Digit9}, {@code KeyA}- {@code KeyZ}, {@code Backquote}, {@code Minus}, {@code Equal}, {@code Backslash}, {@code Backspace}, {@code Tab}, {@code Delete}, {@code Escape}, {@code ArrowDown}, {@code End}, {@code Enter}, {@code Home}, {@code Insert}, {@code PageDown}, {@code PageUp}, {@code ArrowRight}, {@code ArrowUp}, etc.
   * <p>
   * Following modification shortcuts are also suported: {@code Shift}, {@code Control}, {@code Alt}, {@code Meta}, {@code ShiftLeft}.
   * <p>
   * Holding down {@code Shift} will type the text that corresponds to the {@code key} in the upper case.
   * <p>
   * If {@code key} is a single character, it is case-sensitive, so the values {@code a} and {@code A} will generate different respective texts.
   * <p>
   * Shortcuts such as {@code key: "Control+o"} or {@code key: "Control+Shift+T"} are supported as well. When speficied with the modifier, modifier is pressed and being held while the subsequent key is being pressed.
   * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
   */
  void press(String key, PressOptions options);
  default byte[] screenshot() {
    return screenshot(null);
  }
  /**
   * This method waits for the actionability checks, then scrolls element into view before taking a screenshot. If the element is detached from DOM, the method throws an error.
   * @param options Screenshot options.
   * @return Promise which resolves to buffer with the captured screenshot.
   */
  byte[] screenshot(ScreenshotOptions options);
  default void scrollIntoViewIfNeeded() {
    scrollIntoViewIfNeeded(null);
  }
  /**
   * This method waits for actionability checks, then tries to scroll element into view, unless it is completely visible as defined by IntersectionObserver's {@code ratio}.
   * <p>
   * Throws when {@code elementHandle} does not point to an element connected to a Document or a ShadowRoot.
   */
  void scrollIntoViewIfNeeded(ScrollIntoViewIfNeededOptions options);
  default List<String> selectOption(String value) {
    return selectOption(value, null);
  }
  default List<String> selectOption(String value, SelectOptionOptions options) {
    String[] values = value == null ? null : new String[]{ value };
    return selectOption(values, options);
  }
  default List<String> selectOption(String[] values) {
    return selectOption(values, null);
  }
  default List<String> selectOption(String[] values, SelectOptionOptions options) {
    if (values == null) {
      return selectOption(new SelectOption[0], options);
    }
    return selectOption(Arrays.asList(values).stream().map(
      v -> new SelectOption().withValue(v)).toArray(SelectOption[]::new), options);
  }
  default List<String> selectOption(SelectOption value) {
    return selectOption(value, null);
  }
  default List<String> selectOption(SelectOption value, SelectOptionOptions options) {
    SelectOption[] values = value == null ? null : new SelectOption[]{value};
    return selectOption(values, options);
  }
  default List<String> selectOption(SelectOption[] values) {
    return selectOption(values, null);
  }
  List<String> selectOption(SelectOption[] values, SelectOptionOptions options);
  default List<String> selectOption(ElementHandle value) {
    return selectOption(value, null);
  }
  default List<String> selectOption(ElementHandle value, SelectOptionOptions options) {
    ElementHandle[] values = value == null ? null : new ElementHandle[]{value};
    return selectOption(values, options);
  }
  default List<String> selectOption(ElementHandle[] values) {
    return selectOption(values, null);
  }
  /**
   * Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   * <p>
   * If element is not a {@code <select>} element, the method throws an error.
   * <p>
   * 
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option is considered matching if all specified properties match.
   * @return An array of option values that have been successfully selected.
   */
  List<String> selectOption(ElementHandle[] values, SelectOptionOptions options);
  default void selectText() {
    selectText(null);
  }
  /**
   * This method waits for actionability checks, then focuses the element and selects all its text content.
   */
  void selectText(SelectTextOptions options);
  default void setInputFiles(Path file) { setInputFiles(file, null); }
  default void setInputFiles(Path file, SetInputFilesOptions options) { setInputFiles(new Path[]{ file }, options); }
  default void setInputFiles(Path[] files) { setInputFiles(files, null); }
  void setInputFiles(Path[] files, SetInputFilesOptions options);
  default void setInputFiles(FileChooser.FilePayload file) { setInputFiles(file, null); }
  default void setInputFiles(FileChooser.FilePayload file, SetInputFilesOptions options)  { setInputFiles(new FileChooser.FilePayload[]{ file }, options); }
  default void setInputFiles(FileChooser.FilePayload[] files) { setInputFiles(files, null); }
  /**
   * This method expects {@code elementHandle} to point to an input element.
   * <p>
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they are resolved relative to the current working directory. For empty array, clears the selected files.
   */
  void setInputFiles(FileChooser.FilePayload[] files, SetInputFilesOptions options);
  default void tap() {
    tap(null);
  }
  /**
   * This method taps the element by performing the following steps:
   * <p>
   * Wait for actionability checks on the element, unless {@code force} option is set.
   * <p>
   * Scroll the element into view if needed.
   * <p>
   * Use page.touchscreen to tap in the center of the element, or the specified {@code position}.
   * <p>
   * Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   * <p>
   * If the element is detached from the DOM at any moment during the action, this method rejects.
   * <p>
   * When all steps combined have not finished during the specified {@code timeout}, this method rejects with a TimeoutError. Passing zero timeout disables this.
   * <p>
   * <strong>NOTE</strong> {@code elementHandle.tap()} requires that the {@code hasTouch} option of the browser context be set to true.
   * @return Promise that resolves when the element is successfully tapped.
   */
  void tap(TapOptions options);
  /**
   * 
   * @return Resolves to the {@code node.textContent}.
   */
  String textContent();
  String toString();
  default void type(String text) {
    type(text, null);
  }
  /**
   * Focuses the element, and then sends a {@code keydown}, {@code keypress}/{@code input}, and {@code keyup} event for each character in the text.
   * <p>
   * To press a special key, like {@code Control} or {@code ArrowDown}, use {@code elementHandle.press}.
   * <p>
   * 
   * @param text A text to type into a focused element.
   */
  void type(String text, TypeOptions options);
  default void uncheck() {
    uncheck(null);
  }
  /**
   * This method checks the element by performing the following steps:
   * <p>
   * Ensure that element is a checkbox or a radio input. If not, this method rejects. If the element is already unchecked, this method returns immediately.
   * <p>
   * Wait for actionability checks on the element, unless {@code force} option is set.
   * <p>
   * Scroll the element into view if needed.
   * <p>
   * Use page.mouse to click in the center of the element.
   * <p>
   * Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   * <p>
   * Ensure that the element is now unchecked. If not, this method rejects.
   * <p>
   * If the element is detached from the DOM at any moment during the action, this method rejects.
   * <p>
   * When all steps combined have not finished during the specified {@code timeout}, this method rejects with a TimeoutError. Passing zero timeout disables this.
   * @return Promise that resolves when the element is successfully unchecked.
   */
  void uncheck(UncheckOptions options);
  default Deferred<Void> waitForElementState(ElementState state) {
    return waitForElementState(state, null);
  }
  /**
   * Depending on the {@code state} parameter, this method waits for one of the actionability checks to pass. This method throws when the element is detached while waiting, unless waiting for the {@code "hidden"} state.
   * <p>
   * {@code "visible"} Wait until the element is visible.
   * <p>
   * {@code "hidden"} Wait until the element is not visible or not attached. Note that waiting for hidden does not throw when the element detaches.
   * <p>
   * {@code "stable"} Wait until the element is both visible and stable.
   * <p>
   * {@code "enabled"} Wait until the element is enabled.
   * <p>
   * {@code "disabled"} Wait until the element is not enabled.
   * <p>
   * If the element does not satisfy the condition for the {@code timeout} milliseconds, this method will throw.
   * @param state A state to wait for, see below for more details.
   * @return Promise that resolves when the element satisfies the {@code state}.
   */
  Deferred<Void> waitForElementState(ElementState state, WaitForElementStateOptions options);
  default Deferred<ElementHandle> waitForSelector(String selector) {
    return waitForSelector(selector, null);
  }
  /**
   * Wait for the {@code selector} relative to the element handle to satisfy {@code state} option (either appear/disappear from dom, or become visible/hidden). If at the moment of calling the method {@code selector} already satisfies the condition, the method will return immediately. If the selector doesn't satisfy the condition for the {@code timeout} milliseconds, the function will throw.
   * <p>
   * 
   * <p>
   * <strong>NOTE</strong> This method does not work across navigations, use page.waitForSelector(selector[, options]) instead.
   * @param selector A selector of an element to wait for, relative to the element handle. See working with selectors for more details.
   * @return Promise that resolves when element specified by selector satisfies {@code state} option. Resolves to {@code null} if waiting for {@code hidden} or {@code detached}.
   */
  Deferred<ElementHandle> waitForSelector(String selector, WaitForSelectorOptions options);
}

