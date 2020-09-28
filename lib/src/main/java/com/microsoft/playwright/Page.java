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
  enum LoadState { DOMCONTENTLOADED, LOAD, NETWORKIDLE}
  class CloseOptions {
    Boolean runBeforeUnload;

    public CloseOptions withRunBeforeUnload(Boolean runBeforeUnload) {
      this.runBeforeUnload = runBeforeUnload;
      return this;
    }
  }
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
  class EmulateMediaOptions {
    enum Media { PRINT, SCREEN}
    enum ColorScheme { DARK, LIGHT, NO_PREFERENCE}
    Media media;
    ColorScheme colorScheme;

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
  class GoBackOptions {
    enum WaitUntil { DOMCONTENTLOADED, LOAD, NETWORKIDLE}
    Integer timeout;
    WaitUntil waitUntil;

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
    enum WaitUntil { DOMCONTENTLOADED, LOAD, NETWORKIDLE}
    Integer timeout;
    WaitUntil waitUntil;

    public GoForwardOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
    public GoForwardOptions withWaitUntil(WaitUntil waitUntil) {
      this.waitUntil = waitUntil;
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
  class PdfOptions {
    public class Margin {
      String top;
      String right;
      String bottom;
      String left;

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
    String path;
    Integer scale;
    Boolean displayHeaderFooter;
    String headerTemplate;
    String footerTemplate;
    Boolean printBackground;
    Boolean landscape;
    String pageRanges;
    String format;
    String width;
    String height;
    Margin margin;
    Boolean preferCSSPageSize;

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
  class ReloadOptions {
    enum WaitUntil { DOMCONTENTLOADED, LOAD, NETWORKIDLE}
    Integer timeout;
    WaitUntil waitUntil;

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
    enum Type { JPEG, PNG}
    public class Clip {
      int x;
      int y;
      int width;
      int height;

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
    String path;
    Type type;
    Integer quality;
    Boolean fullPage;
    Clip clip;
    Boolean omitBackground;
    Integer timeout;

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
  class SetViewportSizeViewportSize {
    int width;
    int height;

    public SetViewportSizeViewportSize withWidth(int width) {
      this.width = width;
      return this;
    }
    public SetViewportSizeViewportSize withHeight(int height) {
      this.height = height;
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
  class WaitForRequestOptions {
    Integer timeout;

    public WaitForRequestOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForResponseOptions {
    Integer timeout;

    public WaitForResponseOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
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
  void close(CloseOptions options);
  ElementHandle querySelector(String selector);
  List<ElementHandle> querySelectorAll(String selector);
  Object evalOnSelector(String selector, String pageFunction, Object arg);
  Object evalOnSelectorAll(String selector, String pageFunction, Object arg);
  void addInitScript(String script, Object arg);
  ElementHandle addScriptTag(AddScriptTagOptions options);
  ElementHandle addStyleTag(AddStyleTagOptions options);
  void bringToFront();
  void check(String selector, CheckOptions options);
  void click(String selector, ClickOptions options);
  String content();
  BrowserContext context();
  void dblclick(String selector, DblclickOptions options);
  void dispatchEvent(String selector, String type, Object eventInit, DispatchEventOptions options);
  void emulateMedia(EmulateMediaOptions options);
  Object evaluate(String pageFunction, Object arg);
  JSHandle evaluateHandle(String pageFunction, Object arg);
  void exposeBinding(String name, String playwrightBinding);
  void exposeFunction(String name, String playwrightFunction);
  void fill(String selector, String value, FillOptions options);
  void focus(String selector, FocusOptions options);
  Frame frame(String options);
  List<Frame> frames();
  String getAttribute(String selector, String name, GetAttributeOptions options);
  Response goBack(GoBackOptions options);
  Response goForward(GoForwardOptions options);
  Response navigate(String url, GotoOptions options);
  void hover(String selector, HoverOptions options);
  String innerHTML(String selector, InnerHTMLOptions options);
  String innerText(String selector, InnerTextOptions options);
  boolean isClosed();
  Frame mainFrame();
  Page opener();
  byte[] pdf(PdfOptions options);
  void press(String selector, String key, PressOptions options);
  Response reload(ReloadOptions options);
  void route(String url, BiConsumer<Route, Request> handler);
  byte[] screenshot(ScreenshotOptions options);
  List<String> selectOption(String selector, String values, SelectOptionOptions options);
  void setContent(String html, SetContentOptions options);
  void setDefaultNavigationTimeout(int timeout);
  void setDefaultTimeout(int timeout);
  void setExtraHTTPHeaders(Map<String, String> headers);
  void setInputFiles(String selector, String files, SetInputFilesOptions options);
  void setViewportSize(SetViewportSizeViewportSize viewportSize);
  String textContent(String selector, TextContentOptions options);
  String title();
  void type(String selector, String text, TypeOptions options);
  void uncheck(String selector, UncheckOptions options);
  void unroute(String url, BiConsumer<Route, Request> handler);
  String url();
  Object viewportSize();
  Object waitForEvent(String event, String optionsOrPredicate);
  JSHandle waitForFunction(String pageFunction, Object arg, WaitForFunctionOptions options);
  void waitForLoadState(LoadState state, WaitForLoadStateOptions options);
  Response waitForNavigation(WaitForNavigationOptions options);
  Request waitForRequest(String urlOrPredicate, WaitForRequestOptions options);
  Response waitForResponse(String urlOrPredicate, WaitForResponseOptions options);
  ElementHandle waitForSelector(String selector, WaitForSelectorOptions options);
  void waitForTimeout(int timeout);
  List<Worker> workers();
}

