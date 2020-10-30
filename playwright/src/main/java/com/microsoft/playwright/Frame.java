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
import java.util.function.Predicate;
import java.util.regex.Pattern;

public interface Frame {
  enum LoadState { DOMCONTENTLOADED, LOAD, NETWORKIDLE }
  class AddScriptTagOptions {
    public String url;
    public Path path;
    public String content;
    public String type;

    public AddScriptTagOptions withUrl(String url) {
      this.url = url;
      return this;
    }
    public AddScriptTagOptions withPath(Path path) {
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
    public String url;
    public Path path;
    public String content;

    public AddStyleTagOptions withUrl(String url) {
      this.url = url;
      return this;
    }
    public AddStyleTagOptions withPath(Path path) {
      this.path = path;
      return this;
    }
    public AddStyleTagOptions withContent(String content) {
      this.content = content;
      return this;
    }
  }
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
  class DispatchEventOptions {
    public Integer timeout;

    public DispatchEventOptions withTimeout(Integer timeout) {
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
  class FocusOptions {
    public Integer timeout;

    public FocusOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class GetAttributeOptions {
    public Integer timeout;

    public GetAttributeOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class NavigateOptions {
    public Integer timeout;
    public LoadState waitUntil;
    public String referer;

    public NavigateOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
    public NavigateOptions withWaitUntil(LoadState waitUntil) {
      this.waitUntil = waitUntil;
      return this;
    }
    public NavigateOptions withReferer(String referer) {
      this.referer = referer;
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
  class InnerHTMLOptions {
    public Integer timeout;

    public InnerHTMLOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class InnerTextOptions {
    public Integer timeout;

    public InnerTextOptions withTimeout(Integer timeout) {
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
  class SetContentOptions {
    public Integer timeout;
    public LoadState waitUntil;

    public SetContentOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
    public SetContentOptions withWaitUntil(LoadState waitUntil) {
      this.waitUntil = waitUntil;
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
  class TextContentOptions {
    public Integer timeout;

    public TextContentOptions withTimeout(Integer timeout) {
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
  class WaitForFunctionOptions {
    public Integer pollingInterval;
    public Integer timeout;

    public WaitForFunctionOptions withRequestAnimationFrame() {
      this.pollingInterval = null;
      return this;
    }
    public WaitForFunctionOptions withPollingInterval(int millis) {
      this.pollingInterval = millis;
      return this;
    }
    public WaitForFunctionOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForLoadStateOptions {
    public Integer timeout;

    public WaitForLoadStateOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForNavigationOptions {
    public Integer timeout;
    public String glob;
    public Pattern pattern;
    public Predicate<String> predicate;
    public LoadState waitUntil;

    public WaitForNavigationOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
    public WaitForNavigationOptions withUrl(String glob) {
      this.glob = glob;
      return this;
    }
    public WaitForNavigationOptions withUrl(Pattern pattern) {
      this.pattern = pattern;
      return this;
    }
    public WaitForNavigationOptions withUrl(Predicate<String> predicate) {
      this.predicate = predicate;
      return this;
    }
    public WaitForNavigationOptions withWaitUntil(LoadState waitUntil) {
      this.waitUntil = waitUntil;
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
  ElementHandle addScriptTag(AddScriptTagOptions options);
  ElementHandle addStyleTag(AddStyleTagOptions options);
  default void check(String selector) {
    check(selector, null);
  }
  void check(String selector, CheckOptions options);
  List<Frame> childFrames();
  default void click(String selector) {
    click(selector, null);
  }
  void click(String selector, ClickOptions options);
  String content();
  default void dblclick(String selector) {
    dblclick(selector, null);
  }
  void dblclick(String selector, DblclickOptions options);
  default void dispatchEvent(String selector, String type, Object eventInit) {
    dispatchEvent(selector, type, eventInit, null);
  }
  default void dispatchEvent(String selector, String type) {
    dispatchEvent(selector, type, null);
  }
  void dispatchEvent(String selector, String type, Object eventInit, DispatchEventOptions options);
  default Object evaluate(String pageFunction) {
    return evaluate(pageFunction, null);
  }
  Object evaluate(String pageFunction, Object arg);
  default JSHandle evaluateHandle(String pageFunction) {
    return evaluateHandle(pageFunction, null);
  }
  JSHandle evaluateHandle(String pageFunction, Object arg);
  default void fill(String selector, String value) {
    fill(selector, value, null);
  }
  void fill(String selector, String value, FillOptions options);
  default void focus(String selector) {
    focus(selector, null);
  }
  void focus(String selector, FocusOptions options);
  ElementHandle frameElement();
  default String getAttribute(String selector, String name) {
    return getAttribute(selector, name, null);
  }
  String getAttribute(String selector, String name, GetAttributeOptions options);
  default Response navigate(String url) {
    return navigate(url, null);
  }
  Response navigate(String url, NavigateOptions options);
  default void hover(String selector) {
    hover(selector, null);
  }
  void hover(String selector, HoverOptions options);
  default String innerHTML(String selector) {
    return innerHTML(selector, null);
  }
  String innerHTML(String selector, InnerHTMLOptions options);
  default String innerText(String selector) {
    return innerText(selector, null);
  }
  String innerText(String selector, InnerTextOptions options);
  boolean isDetached();
  String name();
  Page page();
  Frame parentFrame();
  default void press(String selector, String key) {
    press(selector, key, null);
  }
  void press(String selector, String key, PressOptions options);
  default List<String> selectOption(String selector, String value) {
    return selectOption(selector, value, null);
  }
  default List<String> selectOption(String selector, String value, SelectOptionOptions options) {
    String[] values = value == null ? null : new String[]{ value };
    return selectOption(selector, values, options);
  }
  default List<String> selectOption(String selector, String[] values) {
    return selectOption(selector, values, null);
  }
  default List<String> selectOption(String selector, String[] values, SelectOptionOptions options) {
    if (values == null) {
      return selectOption(selector, new ElementHandle.SelectOption[0], options);
    }
    return selectOption(selector, Arrays.asList(values).stream().map(
      v -> new ElementHandle.SelectOption().withValue(v)).toArray(ElementHandle.SelectOption[]::new), options);
  }
  default List<String> selectOption(String selector, ElementHandle.SelectOption value) {
    return selectOption(selector, value, null);
  }
  default List<String> selectOption(String selector, ElementHandle.SelectOption value, SelectOptionOptions options) {
    ElementHandle.SelectOption[] values = value == null ? null : new ElementHandle.SelectOption[]{value};
    return selectOption(selector, values, options);
  }
  default List<String> selectOption(String selector, ElementHandle.SelectOption[] values) {
    return selectOption(selector, values, null);
  }
  List<String> selectOption(String selector, ElementHandle.SelectOption[] values, SelectOptionOptions options);
  default List<String> selectOption(String selector, ElementHandle value) {
    return selectOption(selector, value, null);
  }
  default List<String> selectOption(String selector, ElementHandle value, SelectOptionOptions options) {
    ElementHandle[] values = value == null ? null : new ElementHandle[]{value};
    return selectOption(selector, values, options);
  }
  default List<String> selectOption(String selector, ElementHandle[] values) {
    return selectOption(selector, values, null);
  }
  List<String> selectOption(String selector, ElementHandle[] values, SelectOptionOptions options);
  default void setContent(String html) {
    setContent(html, null);
  }
  void setContent(String html, SetContentOptions options);
  default void setInputFiles(String selector, Path file) { setInputFiles(selector, file, null); }
  default void setInputFiles(String selector, Path file, SetInputFilesOptions options) { setInputFiles(selector, new Path[]{ file }, options); }
  default void setInputFiles(String selector, Path[] files) { setInputFiles(selector, files, null); }
  void setInputFiles(String selector, Path[] files, SetInputFilesOptions options);
  default void setInputFiles(String selector, FileChooser.FilePayload file) { setInputFiles(selector, file, null); }
  default void setInputFiles(String selector, FileChooser.FilePayload file, SetInputFilesOptions options)  { setInputFiles(selector, new FileChooser.FilePayload[]{ file }, options); }
  default void setInputFiles(String selector, FileChooser.FilePayload[] files) { setInputFiles(selector, files, null); }
  void setInputFiles(String selector, FileChooser.FilePayload[] files, SetInputFilesOptions options);
  default String textContent(String selector) {
    return textContent(selector, null);
  }
  String textContent(String selector, TextContentOptions options);
  String title();
  default void type(String selector, String text) {
    type(selector, text, null);
  }
  void type(String selector, String text, TypeOptions options);
  default void uncheck(String selector) {
    uncheck(selector, null);
  }
  void uncheck(String selector, UncheckOptions options);
  String url();
  default Deferred<JSHandle> waitForFunction(String pageFunction, Object arg) {
    return waitForFunction(pageFunction, arg, null);
  }
  default Deferred<JSHandle> waitForFunction(String pageFunction) {
    return waitForFunction(pageFunction, null);
  }
  Deferred<JSHandle> waitForFunction(String pageFunction, Object arg, WaitForFunctionOptions options);
  default Deferred<Void> waitForLoadState(LoadState state) {
    return waitForLoadState(state, null);
  }
  default Deferred<Void> waitForLoadState() {
    return waitForLoadState(null);
  }
  Deferred<Void> waitForLoadState(LoadState state, WaitForLoadStateOptions options);
  default Deferred<Response> waitForNavigation() {
    return waitForNavigation(null);
  }
  Deferred<Response> waitForNavigation(WaitForNavigationOptions options);
  default Deferred<ElementHandle> waitForSelector(String selector) {
    return waitForSelector(selector, null);
  }
  Deferred<ElementHandle> waitForSelector(String selector, WaitForSelectorOptions options);
  Deferred<Void> waitForTimeout(int timeout);
}

