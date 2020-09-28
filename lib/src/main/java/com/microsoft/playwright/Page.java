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

interface Page {
  enum LoadState { DOMCONTENTLOADED, LOAD, NETWORKIDLE}
  class CloseOptions {
    Boolean runBeforeUnload;
  }
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
  class EmulateMediaOptions {
    enum Media { PRINT, SCREEN}
    enum ColorScheme { DARK, LIGHT, NO_PREFERENCE}
    Media media;
    ColorScheme colorScheme;
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
  class GoBackOptions {
    enum WaitUntil { DOMCONTENTLOADED, LOAD, NETWORKIDLE}
    Integer timeout;
    WaitUntil waitUntil;
  }
  class GoForwardOptions {
    enum WaitUntil { DOMCONTENTLOADED, LOAD, NETWORKIDLE}
    Integer timeout;
    WaitUntil waitUntil;
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
  class PdfOptions {
    class Margin {
      String top;
      String right;
      String bottom;
      String left;
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
  }
  class PressOptions {
    Integer delay;
    Boolean noWaitAfter;
    Integer timeout;
  }
  class ReloadOptions {
    enum WaitUntil { DOMCONTENTLOADED, LOAD, NETWORKIDLE}
    Integer timeout;
    WaitUntil waitUntil;
  }
  class ScreenshotOptions {
    enum Type { JPEG, PNG}
    class Clip {
      int x;
      int y;
      int width;
      int height;
    }
    String path;
    Type type;
    Integer quality;
    Boolean fullPage;
    Clip clip;
    Boolean omitBackground;
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
  class SetViewportSizeViewportSize {
    int width;
    int height;
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
  class WaitForRequestOptions {
    Integer timeout;
  }
  class WaitForResponseOptions {
    Integer timeout;
  }
  class WaitForSelectorOptions {
    enum State { ATTACHED, DETACHED, HIDDEN, VISIBLE}
    State state;
    Integer timeout;
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

