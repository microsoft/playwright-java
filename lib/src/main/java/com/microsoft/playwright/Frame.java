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

public interface Frame {
  enum LoadState { DOMCONTENTLOADED, LOAD, NETWORKIDLE}
  class AddScriptTagOptions {
    String url;
    String path;
    String content;
    String type;

    public AddScriptTagOptions withUrl(String url) {
      this.url = url;
      return this;
    }
    public AddScriptTagOptions withPath(String path) {
      this.path = path;
      return this;
    }
    public AddScriptTagOptions withContent(String content) {
      this.content = content;
      return this;
    }
    public AddScriptTagOptions withType(String type) {
      this.type = type;
      return this;
    }
  }
  class AddStyleTagOptions {
    String url;
    String path;
    String content;

    public AddStyleTagOptions withUrl(String url) {
      this.url = url;
      return this;
    }
    public AddStyleTagOptions withPath(String path) {
      this.path = path;
      return this;
    }
    public AddStyleTagOptions withContent(String content) {
      this.content = content;
      return this;
    }
  }
  class CheckOptions {
    Boolean force;
    Boolean noWaitAfter;
    Integer timeout;

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
    enum Button { LEFT, MIDDLE, RIGHT}
    enum Modifier { ALT, CONTROL, META, SHIFT}
    public class Position {
      int x;
      int y;

      Position() {
      }
      public ClickOptions done() {
        return ClickOptions.this;
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
    Button button;
    Integer clickCount;
    Integer delay;
    Position position;
    Set<Modifier> modifiers;
    Boolean force;
    Boolean noWaitAfter;
    Integer timeout;

    public ClickOptions withButton(Button button) {
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
    public Position setPosition() {
      this.position = new Position();
      return this.position;
    }
    public ClickOptions withModifiers(Set<Modifier> modifiers) {
      this.modifiers = modifiers;
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
    enum Button { LEFT, MIDDLE, RIGHT}
    enum Modifier { ALT, CONTROL, META, SHIFT}
    public class Position {
      int x;
      int y;

      Position() {
      }
      public DblclickOptions done() {
        return DblclickOptions.this;
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
    Button button;
    Integer delay;
    Position position;
    Set<Modifier> modifiers;
    Boolean force;
    Boolean noWaitAfter;
    Integer timeout;

    public DblclickOptions withButton(Button button) {
      this.button = button;
      return this;
    }
    public DblclickOptions withDelay(Integer delay) {
      this.delay = delay;
      return this;
    }
    public Position setPosition() {
      this.position = new Position();
      return this.position;
    }
    public DblclickOptions withModifiers(Set<Modifier> modifiers) {
      this.modifiers = modifiers;
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
  class DispatchEventOptions {
    Integer timeout;

    public DispatchEventOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class FillOptions {
    Boolean noWaitAfter;
    Integer timeout;

    public FillOptions withNoWaitAfter(Boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public FillOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class FocusOptions {
    Integer timeout;

    public FocusOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class GetAttributeOptions {
    Integer timeout;

    public GetAttributeOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class GotoOptions {
    enum WaitUntil { DOMCONTENTLOADED, LOAD, NETWORKIDLE}
    Integer timeout;
    WaitUntil waitUntil;
    String referer;

    public GotoOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
    public GotoOptions withWaitUntil(WaitUntil waitUntil) {
      this.waitUntil = waitUntil;
      return this;
    }
    public GotoOptions withReferer(String referer) {
      this.referer = referer;
      return this;
    }
  }
  class HoverOptions {
    enum Modifier { ALT, CONTROL, META, SHIFT}
    public class Position {
      int x;
      int y;

      Position() {
      }
      public HoverOptions done() {
        return HoverOptions.this;
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
    Position position;
    Set<Modifier> modifiers;
    Boolean force;
    Integer timeout;

    public Position setPosition() {
      this.position = new Position();
      return this.position;
    }
    public HoverOptions withModifiers(Set<Modifier> modifiers) {
      this.modifiers = modifiers;
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
  class InnerHTMLOptions {
    Integer timeout;

    public InnerHTMLOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class InnerTextOptions {
    Integer timeout;

    public InnerTextOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class PressOptions {
    Integer delay;
    Boolean noWaitAfter;
    Integer timeout;

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
  class SelectOptionOptions {
    Boolean noWaitAfter;
    Integer timeout;

    public SelectOptionOptions withNoWaitAfter(Boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public SelectOptionOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class SetContentOptions {
    enum WaitUntil { DOMCONTENTLOADED, LOAD, NETWORKIDLE}
    Integer timeout;
    WaitUntil waitUntil;

    public SetContentOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
    public SetContentOptions withWaitUntil(WaitUntil waitUntil) {
      this.waitUntil = waitUntil;
      return this;
    }
  }
  class SetInputFilesOptions {
    Boolean noWaitAfter;
    Integer timeout;

    public SetInputFilesOptions withNoWaitAfter(Boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public SetInputFilesOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class TextContentOptions {
    Integer timeout;

    public TextContentOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class TypeOptions {
    Integer delay;
    Boolean noWaitAfter;
    Integer timeout;

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
    Boolean force;
    Boolean noWaitAfter;
    Integer timeout;

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
  class WaitForFunctionOptions {
    double polling;
    Integer timeout;

    public WaitForFunctionOptions withPolling(double polling) {
      this.polling = polling;
      return this;
    }
    public WaitForFunctionOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForLoadStateOptions {
    Integer timeout;

    public WaitForLoadStateOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForNavigationOptions {
    enum WaitUntil { DOMCONTENTLOADED, LOAD, NETWORKIDLE}
    Integer timeout;
    String url;
    WaitUntil waitUntil;

    public WaitForNavigationOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
    public WaitForNavigationOptions withUrl(String url) {
      this.url = url;
      return this;
    }
    public WaitForNavigationOptions withWaitUntil(WaitUntil waitUntil) {
      this.waitUntil = waitUntil;
      return this;
    }
  }
  class WaitForSelectorOptions {
    enum State { ATTACHED, DETACHED, HIDDEN, VISIBLE}
    State state;
    Integer timeout;

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

