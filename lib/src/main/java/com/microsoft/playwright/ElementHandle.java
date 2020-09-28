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

package com.microsoft.playwright;

import java.util.*;
import java.util.function.BiConsumer;

interface ElementHandle {
  enum ElementState { DISABLED, ENABLED, HIDDEN, STABLE, VISIBLE}
  class CheckOptions {
    Boolean force;
    Boolean noWaitAfter;
    Integer timeout;
  }
  class ClickOptions {
    enum Button { LEFT, MIDDLE, RIGHT}
    enum Modifier { ALT, CONTROL, META, SHIFT}
    class Position {
      int x;
      int y;
    }
    Button button;
    Integer clickCount;
    Integer delay;
    Position position;
    Set<Modifier> modifiers;
    Boolean force;
    Boolean noWaitAfter;
    Integer timeout;
  }
  class DblclickOptions {
    enum Button { LEFT, MIDDLE, RIGHT}
    enum Modifier { ALT, CONTROL, META, SHIFT}
    class Position {
      int x;
      int y;
    }
    Button button;
    Integer delay;
    Position position;
    Set<Modifier> modifiers;
    Boolean force;
    Boolean noWaitAfter;
    Integer timeout;
  }
  class FillOptions {
    Boolean noWaitAfter;
    Integer timeout;
  }
  class HoverOptions {
    enum Modifier { ALT, CONTROL, META, SHIFT}
    class Position {
      int x;
      int y;
    }
    Position position;
    Set<Modifier> modifiers;
    Boolean force;
    Integer timeout;
  }
  class PressOptions {
    Integer delay;
    Boolean noWaitAfter;
    Integer timeout;
  }
  class ScreenshotOptions {
    enum Type { JPEG, PNG}
    String path;
    Type type;
    Integer quality;
    Boolean omitBackground;
    Integer timeout;
  }
  class ScrollIntoViewIfNeededOptions {
    Integer timeout;
  }
  class SelectOptionOptions {
    Boolean noWaitAfter;
    Integer timeout;
  }
  class SelectTextOptions {
    Integer timeout;
  }
  class SetInputFilesOptions {
    Boolean noWaitAfter;
    Integer timeout;
  }
  class TypeOptions {
    Integer delay;
    Boolean noWaitAfter;
    Integer timeout;
  }
  class UncheckOptions {
    Boolean force;
    Boolean noWaitAfter;
    Integer timeout;
  }
  class WaitForElementStateOptions {
    Integer timeout;
  }
  class WaitForSelectorOptions {
    enum State { ATTACHED, DETACHED, HIDDEN, VISIBLE}
    State state;
    Integer timeout;
  }
  ElementHandle querySelector(String selector);
  List<ElementHandle> querySelectorAll(String selector);
  Object evalOnSelector(String selector, String pageFunction, Object arg);
  Object evalOnSelectorAll(String selector, String pageFunction, Object arg);
  Object boundingBox();
  void check(CheckOptions options);
  void click(ClickOptions options);
  Frame contentFrame();
  void dblclick(DblclickOptions options);
  void dispatchEvent(String type, Object eventInit);
  void fill(String value, FillOptions options);
  void focus();
  String getAttribute(String name);
  void hover(HoverOptions options);
  String innerHTML();
  String innerText();
  Frame ownerFrame();
  void press(String key, PressOptions options);
  byte[] screenshot(ScreenshotOptions options);
  void scrollIntoViewIfNeeded(ScrollIntoViewIfNeededOptions options);
  List<String> selectOption(String values, SelectOptionOptions options);
  void selectText(SelectTextOptions options);
  void setInputFiles(String files, SetInputFilesOptions options);
  String textContent();
  String toString();
  void type(String text, TypeOptions options);
  void uncheck(UncheckOptions options);
  void waitForElementState(ElementState state, WaitForElementStateOptions options);
  ElementHandle waitForSelector(String selector, WaitForSelectorOptions options);
  ElementHandle asElement();
  void dispose();
  Object evaluate(String pageFunction, Object arg);
  JSHandle evaluateHandle(String pageFunction, Object arg);
  Map<String, JSHandle> getProperties();
  JSHandle getProperty(String propertyName);
  Object jsonValue();
}

