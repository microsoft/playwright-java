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

import java.io.File;
import java.util.*;

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
    public File path;
    public Type type;
    public Integer quality;
    public Boolean omitBackground;
    public Integer timeout;

    public ScreenshotOptions withPath(File path) {
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
  ElementHandle querySelector(String selector);
  List<ElementHandle> querySelectorAll(String selector);
  default Object evalOnSelector(String selector, String pageFunction) {
    return evalOnSelector(selector, pageFunction, null);
  }
  Object evalOnSelector(String selector, String pageFunction, Object arg);
  default Object evalOnSelectorAll(String selector, String pageFunction) {
    return evalOnSelectorAll(selector, pageFunction, null);
  }
  Object evalOnSelectorAll(String selector, String pageFunction, Object arg);
  BoundingBox boundingBox();
  default void check() {
    check(null);
  }
  void check(CheckOptions options);
  default void click() {
    click(null);
  }
  void click(ClickOptions options);
  Frame contentFrame();
  default void dblclick() {
    dblclick(null);
  }
  void dblclick(DblclickOptions options);
  default void dispatchEvent(String type) {
    dispatchEvent(type, null);
  }
  void dispatchEvent(String type, Object eventInit);
  default void fill(String value) {
    fill(value, null);
  }
  void fill(String value, FillOptions options);
  void focus();
  String getAttribute(String name);
  default void hover() {
    hover(null);
  }
  void hover(HoverOptions options);
  String innerHTML();
  String innerText();
  Frame ownerFrame();
  default void press(String key) {
    press(key, null);
  }
  void press(String key, PressOptions options);
  default byte[] screenshot() {
    return screenshot(null);
  }
  byte[] screenshot(ScreenshotOptions options);
  default void scrollIntoViewIfNeeded() {
    scrollIntoViewIfNeeded(null);
  }
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
  List<String> selectOption(ElementHandle[] values, SelectOptionOptions options);
  default void selectText() {
    selectText(null);
  }
  void selectText(SelectTextOptions options);
  default void setInputFiles(File file) { setInputFiles(file, null); }
  default void setInputFiles(File file, SetInputFilesOptions options) { setInputFiles(new File[]{ file }, options); }
  default void setInputFiles(File[] files) { setInputFiles(files, null); }
  void setInputFiles(File[] files, SetInputFilesOptions options);
  default void setInputFiles(FileChooser.FilePayload file) { setInputFiles(file, null); }
  default void setInputFiles(FileChooser.FilePayload file, SetInputFilesOptions options)  { setInputFiles(new FileChooser.FilePayload[]{ file }, options); }
  default void setInputFiles(FileChooser.FilePayload[] files) { setInputFiles(files, null); }
  void setInputFiles(FileChooser.FilePayload[] files, SetInputFilesOptions options);
  String textContent();
  String toString();
  default void type(String text) {
    type(text, null);
  }
  void type(String text, TypeOptions options);
  default void uncheck() {
    uncheck(null);
  }
  void uncheck(UncheckOptions options);
  default Deferred<Void> waitForElementState(ElementState state) {
    return waitForElementState(state, null);
  }
  Deferred<Void> waitForElementState(ElementState state, WaitForElementStateOptions options);
  default Deferred<ElementHandle> waitForSelector(String selector) {
    return waitForSelector(selector, null);
  }
  Deferred<ElementHandle> waitForSelector(String selector, WaitForSelectorOptions options);
}

