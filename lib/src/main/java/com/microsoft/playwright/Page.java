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

interface Page{

  class CloseOptions {
    Boolean runBeforeUnload;
  }
  void close(CloseOptions options);
  ElementHandle querySelector(String selector);
  List<ElementHandle> querySelectorAll(String selector);
  Object evalOnSelector(String selector, String pageFunction, Object arg);
  Object evalOnSelectorAll(String selector, String pageFunction, Object arg);

  class AddInitScriptArg {
  }
  void addInitScript(String script, AddInitScriptArg arg);

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
  void bringToFront();

  class CheckOptions {
    Boolean force;
    Boolean noWaitAfter;
    Integer timeout;
  }
  void check(String selector, CheckOptions options);

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
  BrowserContext context();

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

  class EmulateMediaOptions {
    enum Media { PRINT, SCREEN }
    Media media;
    enum ColorScheme { DARK, LIGHT, NO_PREFERENCE }
    ColorScheme colorScheme;
  }
  void emulateMedia(EmulateMediaOptions options);
  Object evaluate(String pageFunction, Object arg);
  JSHandle evaluateHandle(String pageFunction, Object arg);
  void exposeBinding(String name, String playwrightBinding);
  void exposeFunction(String name, String playwrightFunction);

  class FillOptions {
    Boolean noWaitAfter;
    Integer timeout;
  }
  void fill(String selector, String value, FillOptions options);

  class FocusOptions {
    Integer timeout;
  }
  void focus(String selector, FocusOptions options);
  Frame frame(String options);
  List<Frame> frames();

  class GetAttributeOptions {
    Integer timeout;
  }
  String getAttribute(String selector, String name, GetAttributeOptions options);

  class GoBackOptions {
    Integer timeout;
    enum WaitUntil { DOMCONTENTLOADED, LOAD, NETWORKIDLE }
    WaitUntil waitUntil;
  }
  Response goBack(GoBackOptions options);

  class GoForwardOptions {
    Integer timeout;
    enum WaitUntil { DOMCONTENTLOADED, LOAD, NETWORKIDLE }
    WaitUntil waitUntil;
  }
  Response goForward(GoForwardOptions options);

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
  boolean isClosed();
  Frame mainFrame();
  Page opener();

  class PdfOptions {
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
    Object margin;
    Boolean preferCSSPageSize;
  }
  byte[] pdf(PdfOptions options);

  class PressOptions {
    Integer delay;
    Boolean noWaitAfter;
    Integer timeout;
  }
  void press(String selector, String key, PressOptions options);

  class ReloadOptions {
    Integer timeout;
    enum WaitUntil { DOMCONTENTLOADED, LOAD, NETWORKIDLE }
    WaitUntil waitUntil;
  }
  Response reload(ReloadOptions options);
  void route(String url, BiConsumer<Route, Request> handler);

  class ScreenshotOptions {
    String path;
    enum Type { JPEG, PNG }
    Type type;
    Integer quality;
    Boolean fullPage;
    Object clip;
    Boolean omitBackground;
    Integer timeout;
  }
  byte[] screenshot(ScreenshotOptions options);

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
  void setDefaultNavigationTimeout(int timeout);
  void setDefaultTimeout(int timeout);
  void setExtraHTTPHeaders(Map<String, String> headers);

  class SetInputFilesOptions {
    Boolean noWaitAfter;
    Integer timeout;
  }
  void setInputFiles(String selector, String files, SetInputFilesOptions options);

  class SetViewportSizeViewportSize {
    Integer width;
    Integer height;
  }
  void setViewportSize(SetViewportSizeViewportSize viewportSize);

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
  void unroute(String url, BiConsumer<Route, Request> handler);
  String url();
  Object viewportSize();
  Object waitForEvent(String event, String optionsOrPredicate);

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

  class WaitForRequestOptions {
    Integer timeout;
  }
  Request waitForRequest(String urlOrPredicate, WaitForRequestOptions options);

  class WaitForResponseOptions {
    Integer timeout;
  }
  Response waitForResponse(String urlOrPredicate, WaitForResponseOptions options);

  class WaitForSelectorOptions {
    enum State { ATTACHED, DETACHED, HIDDEN, VISIBLE }
    State state;
    Integer timeout;
  }
  ElementHandle waitForSelector(String selector, WaitForSelectorOptions options);
  void waitForTimeout(int timeout);
  List<Worker> workers();
}

