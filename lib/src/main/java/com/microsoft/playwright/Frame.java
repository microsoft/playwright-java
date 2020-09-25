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

interface Frame{
  ElementHandle querySelector(String selector);
  List<ElementHandle> querySelectorAll(String selector);
  Object evalOnSelector(String selector, String pageFunction, Object arg);
  Object evalOnSelectorAll(String selector, String pageFunction, Object arg);

  class AddScriptTagOptions {
    String url;
    String path;
    String content;
    String type;
  }
  ElementHandle addScriptTag(AddScriptTagOptions options);

  class AddStyleTagOptions {
    String url;
    String path;
    String content;
  }
  ElementHandle addStyleTag(AddStyleTagOptions options);

  class CheckOptions {
    Boolean force;
    Boolean noWaitAfter;
    Integer timeout;
  }
  void check(String selector, CheckOptions options);
  List<Frame> childFrames();

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
  void click(String selector, ClickOptions options);
  String content();

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
  void dblclick(String selector, DblclickOptions options);

  class DispatchEventOptions {
    Integer timeout;
  }
  void dispatchEvent(String selector, String type, Object eventInit, DispatchEventOptions options);
  Object evaluate(String pageFunction, Object arg);
  JSHandle evaluateHandle(String pageFunction, Object arg);

  class FillOptions {
    Boolean noWaitAfter;
    Integer timeout;
  }
  void fill(String selector, String value, FillOptions options);

  class FocusOptions {
    Integer timeout;
  }
  void focus(String selector, FocusOptions options);
  ElementHandle frameElement();

  class GetAttributeOptions {
    Integer timeout;
  }
  String getAttribute(String selector, String name, GetAttributeOptions options);

  class GotoOptions {
    Integer timeout;
    enum WaitUntil { DOMCONTENTLOADED, LOAD, NETWORKIDLE }
    WaitUntil waitUntil;
    String referer;
  }
  Response navigate(String url, GotoOptions options);

  class HoverOptions {
    Object position;
    enum Modifier { ALT, CONTROL, META, SHIFT }
    Set<Modifier> modifiers;
    Boolean force;
    Integer timeout;
  }
  void hover(String selector, HoverOptions options);

  class InnerHTMLOptions {
    Integer timeout;
  }
  String innerHTML(String selector, InnerHTMLOptions options);

  class InnerTextOptions {
    Integer timeout;
  }
  String innerText(String selector, InnerTextOptions options);
  boolean isDetached();
  String name();
  Page page();
  Frame parentFrame();

  class PressOptions {
    Integer delay;
    Boolean noWaitAfter;
    Integer timeout;
  }
  void press(String selector, String key, PressOptions options);

  class SelectOptionOptions {
    Boolean noWaitAfter;
    Integer timeout;
  }
  List<String> selectOption(String selector, String values, SelectOptionOptions options);

  class SetContentOptions {
    Integer timeout;
    enum WaitUntil { DOMCONTENTLOADED, LOAD, NETWORKIDLE }
    WaitUntil waitUntil;
  }
  void setContent(String html, SetContentOptions options);

  class SetInputFilesOptions {
    Boolean noWaitAfter;
    Integer timeout;
  }
  void setInputFiles(String selector, String files, SetInputFilesOptions options);

  class TextContentOptions {
    Integer timeout;
  }
  String textContent(String selector, TextContentOptions options);
  String title();

  class TypeOptions {
    Integer delay;
    Boolean noWaitAfter;
    Integer timeout;
  }
  void type(String selector, String text, TypeOptions options);

  class UncheckOptions {
    Boolean force;
    Boolean noWaitAfter;
    Integer timeout;
  }
  void uncheck(String selector, UncheckOptions options);
  String url();

  class WaitForFunctionOptions {
    enum Polling { UMBE, RAF }
    Polling polling;
    Integer timeout;
  }
  JSHandle waitForFunction(String pageFunction, Object arg, WaitForFunctionOptions options);
  enum LoadState { DOMCONTENTLOADED, LOAD, NETWORKIDLE }

  class WaitForLoadStateOptions {
    Integer timeout;
  }
  void waitForLoadState(LoadState state, WaitForLoadStateOptions options);

  class WaitForNavigationOptions {
    Integer timeout;
    String url;
    enum WaitUntil { DOMCONTENTLOADED, LOAD, NETWORKIDLE }
    WaitUntil waitUntil;
  }
  Response waitForNavigation(WaitForNavigationOptions options);

  class WaitForSelectorOptions {
    enum State { ATTACHED, DETACHED, HIDDEN, VISIBLE }
    State state;
    Integer timeout;
  }
  ElementHandle waitForSelector(String selector, WaitForSelectorOptions options);
  void waitForTimeout(int timeout);
}

