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

interface Frame {
  enum LoadState { DOMCONTENTLOADED, LOAD, NETWORKIDLE}
  class AddScriptTagOptions {
    String url;
    String path;
    String content;
    String type;
  }
  class AddStyleTagOptions {
    String url;
    String path;
    String content;
  }
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
  class DispatchEventOptions {
    Integer timeout;
  }
  class FillOptions {
    Boolean noWaitAfter;
    Integer timeout;
  }
  class FocusOptions {
    Integer timeout;
  }
  class GetAttributeOptions {
    Integer timeout;
  }
  class GotoOptions {
    enum WaitUntil { DOMCONTENTLOADED, LOAD, NETWORKIDLE}
    Integer timeout;
    WaitUntil waitUntil;
    String referer;
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
  class InnerHTMLOptions {
    Integer timeout;
  }
  class InnerTextOptions {
    Integer timeout;
  }
  class PressOptions {
    Integer delay;
    Boolean noWaitAfter;
    Integer timeout;
  }
  class SelectOptionOptions {
    Boolean noWaitAfter;
    Integer timeout;
  }
  class SetContentOptions {
    enum WaitUntil { DOMCONTENTLOADED, LOAD, NETWORKIDLE}
    Integer timeout;
    WaitUntil waitUntil;
  }
  class SetInputFilesOptions {
    Boolean noWaitAfter;
    Integer timeout;
  }
  class TextContentOptions {
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
  class WaitForFunctionOptions {
    double polling;
    Integer timeout;
  }
  class WaitForLoadStateOptions {
    Integer timeout;
  }
  class WaitForNavigationOptions {
    enum WaitUntil { DOMCONTENTLOADED, LOAD, NETWORKIDLE}
    Integer timeout;
    String url;
    WaitUntil waitUntil;
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
  ElementHandle addScriptTag(AddScriptTagOptions options);
  ElementHandle addStyleTag(AddStyleTagOptions options);
  void check(String selector, CheckOptions options);
  List<Frame> childFrames();
  void click(String selector, ClickOptions options);
  String content();
  void dblclick(String selector, DblclickOptions options);
  void dispatchEvent(String selector, String type, Object eventInit, DispatchEventOptions options);
  Object evaluate(String pageFunction, Object arg);
  JSHandle evaluateHandle(String pageFunction, Object arg);
  void fill(String selector, String value, FillOptions options);
  void focus(String selector, FocusOptions options);
  ElementHandle frameElement();
  String getAttribute(String selector, String name, GetAttributeOptions options);
  Response navigate(String url, GotoOptions options);
  void hover(String selector, HoverOptions options);
  String innerHTML(String selector, InnerHTMLOptions options);
  String innerText(String selector, InnerTextOptions options);
  boolean isDetached();
  String name();
  Page page();
  Frame parentFrame();
  void press(String selector, String key, PressOptions options);
  List<String> selectOption(String selector, String values, SelectOptionOptions options);
  void setContent(String html, SetContentOptions options);
  void setInputFiles(String selector, String files, SetInputFilesOptions options);
  String textContent(String selector, TextContentOptions options);
  String title();
  void type(String selector, String text, TypeOptions options);
  void uncheck(String selector, UncheckOptions options);
  String url();
  JSHandle waitForFunction(String pageFunction, Object arg, WaitForFunctionOptions options);
  void waitForLoadState(LoadState state, WaitForLoadStateOptions options);
  Response waitForNavigation(WaitForNavigationOptions options);
  ElementHandle waitForSelector(String selector, WaitForSelectorOptions options);
  void waitForTimeout(int timeout);
}

