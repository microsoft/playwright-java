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

interface ElementHandle{
  ElementHandle querySelector(String selector);
  List<ElementHandle> querySelectorAll(String selector);
  Object evalOnSelector(String selector, String pageFunction, Object arg);
  Object evalOnSelectorAll(String selector, String pageFunction, Object arg);
  Object boundingBox();

  class CheckOptions {
    Boolean force;
    Boolean noWaitAfter;
    Integer timeout;
  }
  void check(CheckOptions options);

  class ClickOptions {
    enum Button { LEFT, MIDDLE, RIGHT }
    Button button;
    Integer clickCount;
    Integer delay;
    Object position;
    enum Modifier { ALT, CONTROL, META, SHIFT }
    Set<Modifier> modifiers;
    Boolean force;
    Boolean noWaitAfter;
    Integer timeout;
  }
  void click(ClickOptions options);
  Frame contentFrame();

  class DblclickOptions {
    enum Button { LEFT, MIDDLE, RIGHT }
    Button button;
    Integer delay;
    Object position;
    enum Modifier { ALT, CONTROL, META, SHIFT }
    Set<Modifier> modifiers;
    Boolean force;
    Boolean noWaitAfter;
    Integer timeout;
  }
  void dblclick(DblclickOptions options);
  void dispatchEvent(String type, Object eventInit);

  class FillOptions {
    Boolean noWaitAfter;
    Integer timeout;
  }
  void fill(String value, FillOptions options);
  void focus();
  String getAttribute(String name);

  class HoverOptions {
    Object position;
    enum Modifier { ALT, CONTROL, META, SHIFT }
    Set<Modifier> modifiers;
    Boolean force;
    Integer timeout;
  }
  void hover(HoverOptions options);
  String innerHTML();
  String innerText();
  Frame ownerFrame();

  class PressOptions {
    Integer delay;
    Boolean noWaitAfter;
    Integer timeout;
  }
  void press(String key, PressOptions options);

  class ScreenshotOptions {
    String path;
    enum Type { JPEG, PNG }
    Type type;
    Integer quality;
    Boolean omitBackground;
    Integer timeout;
  }
  byte[] screenshot(ScreenshotOptions options);

  class ScrollIntoViewIfNeededOptions {
    Integer timeout;
  }
  void scrollIntoViewIfNeeded(ScrollIntoViewIfNeededOptions options);

  class SelectOptionOptions {
    Boolean noWaitAfter;
    Integer timeout;
  }
  List<String> selectOption(String values, SelectOptionOptions options);

  class SelectTextOptions {
    Integer timeout;
  }
  void selectText(SelectTextOptions options);

  class SetInputFilesOptions {
    Boolean noWaitAfter;
    Integer timeout;
  }
  void setInputFiles(String files, SetInputFilesOptions options);
  String textContent();
  String toString();

  class TypeOptions {
    Integer delay;
    Boolean noWaitAfter;
    Integer timeout;
  }
  void type(String text, TypeOptions options);

  class UncheckOptions {
    Boolean force;
    Boolean noWaitAfter;
    Integer timeout;
  }
  void uncheck(UncheckOptions options);
  enum ElementState { DISABLED, ENABLED, HIDDEN, STABLE, VISIBLE }

  class WaitForElementStateOptions {
    Integer timeout;
  }
  void waitForElementState(ElementState state, WaitForElementStateOptions options);

  class WaitForSelectorOptions {
    enum State { ATTACHED, DETACHED, HIDDEN, VISIBLE }
    State state;
    Integer timeout;
  }
  ElementHandle waitForSelector(String selector, WaitForSelectorOptions options);
  ElementHandle asElement();
  void dispose();
  Object evaluate(String pageFunction, Object arg);
  JSHandle evaluateHandle(String pageFunction, Object arg);
  Map<String, JSHandle> getProperties();
  JSHandle getProperty(String propertyName);
  Object jsonValue();
}

