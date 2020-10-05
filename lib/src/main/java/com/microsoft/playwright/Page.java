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

public interface Page {
  class Viewport {
    private final int width;
    private final int height;

    public Viewport(int width, int height) {
      this.width = width;
      this.height = height;
    }

    public int width() {
      return width;
    }

    public int height() {
      return height;
    }
  }

  interface Function {
    Object call(Object... args);
  }

  interface Binding {
    interface Source {
      BrowserContext context();
      Page page();
      Frame frame();
    }

    Object call(Source source, Object... args);
  }

  enum LoadState { DOMCONTENTLOADED, LOAD, NETWORKIDLE }
  class CloseOptions {
    public Boolean runBeforeUnload;

    public CloseOptions withRunBeforeUnload(Boolean runBeforeUnload) {
      this.runBeforeUnload = runBeforeUnload;
      return this;
    }
  }
  class AddScriptTagOptions {
    public String url;
    public String path;
    public String content;
    public String type;

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
    public String url;
    public String path;
    public String content;

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
    public class Position {
      public int x;
      public int y;

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
    public Position setPosition() {
      this.position = new Position();
      return this.position;
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
    public class Position {
      public int x;
      public int y;

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
    public Position setPosition() {
      this.position = new Position();
      return this.position;
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
  class EmulateMediaOptions {
    public enum Media { PRINT, SCREEN }
    public enum ColorScheme { DARK, LIGHT, NO_PREFERENCE }
    public Media media;
    public ColorScheme colorScheme;

    public EmulateMediaOptions withMedia(Media media) {
      this.media = media;
      return this;
    }
    public EmulateMediaOptions withColorScheme(ColorScheme colorScheme) {
      this.colorScheme = colorScheme;
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
  class GoBackOptions {
    public enum WaitUntil { DOMCONTENTLOADED, LOAD, NETWORKIDLE }
    public Integer timeout;
    public WaitUntil waitUntil;

    public GoBackOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
    public GoBackOptions withWaitUntil(WaitUntil waitUntil) {
      this.waitUntil = waitUntil;
      return this;
    }
  }
  class GoForwardOptions {
    public enum WaitUntil { DOMCONTENTLOADED, LOAD, NETWORKIDLE }
    public Integer timeout;
    public WaitUntil waitUntil;

    public GoForwardOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
    public GoForwardOptions withWaitUntil(WaitUntil waitUntil) {
      this.waitUntil = waitUntil;
      return this;
    }
  }
  class NavigateOptions {
    public enum WaitUntil { DOMCONTENTLOADED, LOAD, NETWORKIDLE }
    public Integer timeout;
    public WaitUntil waitUntil;
    public String referer;

    public NavigateOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
    public NavigateOptions withWaitUntil(WaitUntil waitUntil) {
      this.waitUntil = waitUntil;
      return this;
    }
    public NavigateOptions withReferer(String referer) {
      this.referer = referer;
      return this;
    }
  }
  class HoverOptions {
    public class Position {
      public int x;
      public int y;

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
    public Position position;
    public Set<Keyboard.Modifier> modifiers;
    public Boolean force;
    public Integer timeout;

    public Position setPosition() {
      this.position = new Position();
      return this.position;
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
  class PdfOptions {
    public class Margin {
      public String top;
      public String right;
      public String bottom;
      public String left;

      Margin() {
      }
      public PdfOptions done() {
        return PdfOptions.this;
      }

      public Margin withTop(String top) {
        this.top = top;
        return this;
      }
      public Margin withRight(String right) {
        this.right = right;
        return this;
      }
      public Margin withBottom(String bottom) {
        this.bottom = bottom;
        return this;
      }
      public Margin withLeft(String left) {
        this.left = left;
        return this;
      }
    }
    public String path;
    public Integer scale;
    public Boolean displayHeaderFooter;
    public String headerTemplate;
    public String footerTemplate;
    public Boolean printBackground;
    public Boolean landscape;
    public String pageRanges;
    public String format;
    public String width;
    public String height;
    public Margin margin;
    public Boolean preferCSSPageSize;

    public PdfOptions withPath(String path) {
      this.path = path;
      return this;
    }
    public PdfOptions withScale(Integer scale) {
      this.scale = scale;
      return this;
    }
    public PdfOptions withDisplayHeaderFooter(Boolean displayHeaderFooter) {
      this.displayHeaderFooter = displayHeaderFooter;
      return this;
    }
    public PdfOptions withHeaderTemplate(String headerTemplate) {
      this.headerTemplate = headerTemplate;
      return this;
    }
    public PdfOptions withFooterTemplate(String footerTemplate) {
      this.footerTemplate = footerTemplate;
      return this;
    }
    public PdfOptions withPrintBackground(Boolean printBackground) {
      this.printBackground = printBackground;
      return this;
    }
    public PdfOptions withLandscape(Boolean landscape) {
      this.landscape = landscape;
      return this;
    }
    public PdfOptions withPageRanges(String pageRanges) {
      this.pageRanges = pageRanges;
      return this;
    }
    public PdfOptions withFormat(String format) {
      this.format = format;
      return this;
    }
    public PdfOptions withWidth(String width) {
      this.width = width;
      return this;
    }
    public PdfOptions withHeight(String height) {
      this.height = height;
      return this;
    }
    public Margin setMargin() {
      this.margin = new Margin();
      return this.margin;
    }
    public PdfOptions withPreferCSSPageSize(Boolean preferCSSPageSize) {
      this.preferCSSPageSize = preferCSSPageSize;
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
  class ReloadOptions {
    public enum WaitUntil { DOMCONTENTLOADED, LOAD, NETWORKIDLE }
    public Integer timeout;
    public WaitUntil waitUntil;

    public ReloadOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
    public ReloadOptions withWaitUntil(WaitUntil waitUntil) {
      this.waitUntil = waitUntil;
      return this;
    }
  }
  class ScreenshotOptions {
    public enum Type { JPEG, PNG }
    public class Clip {
      public int x;
      public int y;
      public int width;
      public int height;

      Clip() {
      }
      public ScreenshotOptions done() {
        return ScreenshotOptions.this;
      }

      public Clip withX(int x) {
        this.x = x;
        return this;
      }
      public Clip withY(int y) {
        this.y = y;
        return this;
      }
      public Clip withWidth(int width) {
        this.width = width;
        return this;
      }
      public Clip withHeight(int height) {
        this.height = height;
        return this;
      }
    }
    public String path;
    public Type type;
    public Integer quality;
    public Boolean fullPage;
    public Clip clip;
    public Boolean omitBackground;
    public Integer timeout;

    public ScreenshotOptions withPath(String path) {
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
    public ScreenshotOptions withFullPage(Boolean fullPage) {
      this.fullPage = fullPage;
      return this;
    }
    public Clip setClip() {
      this.clip = new Clip();
      return this.clip;
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
    public enum WaitUntil { DOMCONTENTLOADED, LOAD, NETWORKIDLE }
    public Integer timeout;
    public WaitUntil waitUntil;

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
    public double polling;
    public Integer timeout;

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
    public Integer timeout;

    public WaitForLoadStateOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForNavigationOptions {
    public enum WaitUntil { DOMCONTENTLOADED, LOAD, NETWORKIDLE }
    public Integer timeout;
    public String url;
    public WaitUntil waitUntil;

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
  class WaitForRequestOptions {
    public Integer timeout;

    public WaitForRequestOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForResponseOptions {
    public Integer timeout;

    public WaitForResponseOptions withTimeout(Integer timeout) {
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
  void addConsoleListener(Listener<ConsoleMessage> listener);
  void removeConsoleListener(Listener<ConsoleMessage> listener);
  Deferred<Page> waitForPopup();
  default void close() {
    close(null);
  }
  void close(CloseOptions options);
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
  Accessibility accessibility();
  default void addInitScript(String script) {
    addInitScript(script, null);
  }
  void addInitScript(String script, Object arg);
  ElementHandle addScriptTag(AddScriptTagOptions options);
  ElementHandle addStyleTag(AddStyleTagOptions options);
  void bringToFront();
  default void check(String selector) {
    check(selector, null);
  }
  void check(String selector, CheckOptions options);
  default void click(String selector) {
    click(selector, null);
  }
  void click(String selector, ClickOptions options);
  String content();
  BrowserContext context();
  ChromiumCoverage coverage();
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
  void emulateMedia(EmulateMediaOptions options);
  default Object evaluate(String pageFunction) {
    return evaluate(pageFunction, null);
  }
  Object evaluate(String pageFunction, Object arg);
  default JSHandle evaluateHandle(String pageFunction) {
    return evaluateHandle(pageFunction, null);
  }
  JSHandle evaluateHandle(String pageFunction, Object arg);
  void exposeBinding(String name, Binding playwrightBinding);
  void exposeFunction(String name, Function playwrightFunction);
  default void fill(String selector, String value) {
    fill(selector, value, null);
  }
  void fill(String selector, String value, FillOptions options);
  default void focus(String selector) {
    focus(selector, null);
  }
  void focus(String selector, FocusOptions options);
  Frame frame(String options);
  List<Frame> frames();
  default String getAttribute(String selector, String name) {
    return getAttribute(selector, name, null);
  }
  String getAttribute(String selector, String name, GetAttributeOptions options);
  default Response goBack() {
    return goBack(null);
  }
  Response goBack(GoBackOptions options);
  default Response goForward() {
    return goForward(null);
  }
  Response goForward(GoForwardOptions options);
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
  boolean isClosed();
  Keyboard keyboard();
  Frame mainFrame();
  Mouse mouse();
  Page opener();
  default byte[] pdf() {
    return pdf(null);
  }
  byte[] pdf(PdfOptions options);
  default void press(String selector, String key) {
    press(selector, key, null);
  }
  void press(String selector, String key, PressOptions options);
  default Response reload() {
    return reload(null);
  }
  Response reload(ReloadOptions options);
  void route(String url, BiConsumer<Route, Request> handler);
  default byte[] screenshot() {
    return screenshot(null);
  }
  byte[] screenshot(ScreenshotOptions options);
  default List<String> selectOption(String selector, String values) {
    return selectOption(selector, values, null);
  }
  List<String> selectOption(String selector, String values, SelectOptionOptions options);
  default void setContent(String html) {
    setContent(html, null);
  }
  void setContent(String html, SetContentOptions options);
  void setDefaultNavigationTimeout(int timeout);
  void setDefaultTimeout(int timeout);
  void setExtraHTTPHeaders(Map<String, String> headers);
  default void setInputFiles(String selector, String files) {
    setInputFiles(selector, files, null);
  }
  void setInputFiles(String selector, String files, SetInputFilesOptions options);
  void setViewportSize(int width, int height);
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
  default void unroute(String url) {
    unroute(url, null);
  }
  void unroute(String url, BiConsumer<Route, Request> handler);
  String url();
  Viewport viewportSize();
  default Object waitForEvent(String event) {
    return waitForEvent(event, null);
  }
  Object waitForEvent(String event, String optionsOrPredicate);
  default JSHandle waitForFunction(String pageFunction, Object arg) {
    return waitForFunction(pageFunction, arg, null);
  }
  default JSHandle waitForFunction(String pageFunction) {
    return waitForFunction(pageFunction, null);
  }
  JSHandle waitForFunction(String pageFunction, Object arg, WaitForFunctionOptions options);
  default void waitForLoadState(LoadState state) {
    waitForLoadState(state, null);
  }
  default void waitForLoadState() {
    waitForLoadState(null);
  }
  void waitForLoadState(LoadState state, WaitForLoadStateOptions options);
  default Response waitForNavigation() {
    return waitForNavigation(null);
  }
  Response waitForNavigation(WaitForNavigationOptions options);
  default Request waitForRequest(String urlOrPredicate) {
    return waitForRequest(urlOrPredicate, null);
  }
  Request waitForRequest(String urlOrPredicate, WaitForRequestOptions options);
  default Response waitForResponse(String urlOrPredicate) {
    return waitForResponse(urlOrPredicate, null);
  }
  Response waitForResponse(String urlOrPredicate, WaitForResponseOptions options);
  default ElementHandle waitForSelector(String selector) {
    return waitForSelector(selector, null);
  }
  ElementHandle waitForSelector(String selector, WaitForSelectorOptions options);
  void waitForTimeout(int timeout);
  List<Worker> workers();
}

