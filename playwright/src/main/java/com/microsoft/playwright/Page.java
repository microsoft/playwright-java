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
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Page provides methods to interact with a single tab in a Browser, or an extension background page in Chromium. One Browser instance might have multiple Page instances.
 * <p>
 * The Page class emits various events (described below) which can be handled using any of Node's native {@code EventEmitter} methods, such as {@code on}, {@code once} or {@code removeListener}.
 * <p>
 * To unsubscribe from events use the {@code removeListener} method:
 * <p>
 */
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

  interface Error {
    String message();
    String name();
    String stack();
  }

  class WaitForEventOptions {
    public Integer timeout;
    public Predicate<Event<EventType>> predicate;
    public WaitForEventOptions withTimeout(int millis) {
      timeout = millis;
      return this;
    }
    public WaitForEventOptions withPredicate(Predicate<Event<EventType>> predicate) {
      this.predicate = predicate;
      return this;
    }
  }

  enum EventType {
    CLOSE,
    CONSOLE,
    CRASH,
    DIALOG,
    DOMCONTENTLOADED,
    DOWNLOAD,
    FILECHOOSER,
    FRAMEATTACHED,
    FRAMEDETACHED,
    FRAMENAVIGATED,
    LOAD,
    PAGEERROR,
    POPUP,
    REQUEST,
    REQUESTFAILED,
    REQUESTFINISHED,
    RESPONSE,
    WEBSOCKET,
    WORKER,
  }

  void addListener(EventType type, Listener<EventType> listener);
  void removeListener(EventType type, Listener<EventType> listener);
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
  class CloseOptions {
    public Boolean runBeforeUnload;

    public CloseOptions withRunBeforeUnload(Boolean runBeforeUnload) {
      this.runBeforeUnload = runBeforeUnload;
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
  class EmulateMediaOptions {
    public enum Media { PRINT, SCREEN }
    public Optional<Media> media;
    public Optional<ColorScheme> colorScheme;

    public EmulateMediaOptions withMedia(Media media) {
      this.media = Optional.ofNullable(media);
      return this;
    }
    public EmulateMediaOptions withColorScheme(ColorScheme colorScheme) {
      this.colorScheme = Optional.ofNullable(colorScheme);
      return this;
    }
  }
  class ExposeBindingOptions {
    public Boolean handle;

    public ExposeBindingOptions withHandle(Boolean handle) {
      this.handle = handle;
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
    public Integer timeout;
    public Frame.LoadState waitUntil;

    public GoBackOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
    public GoBackOptions withWaitUntil(Frame.LoadState waitUntil) {
      this.waitUntil = waitUntil;
      return this;
    }
  }
  class GoForwardOptions {
    public Integer timeout;
    public Frame.LoadState waitUntil;

    public GoForwardOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
    public GoForwardOptions withWaitUntil(Frame.LoadState waitUntil) {
      this.waitUntil = waitUntil;
      return this;
    }
  }
  class NavigateOptions {
    public Integer timeout;
    public Frame.LoadState waitUntil;
    public String referer;

    public NavigateOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
    public NavigateOptions withWaitUntil(Frame.LoadState waitUntil) {
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
    public Path path;
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

    public PdfOptions withPath(Path path) {
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
    public Integer timeout;
    public Frame.LoadState waitUntil;

    public ReloadOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
    public ReloadOptions withWaitUntil(Frame.LoadState waitUntil) {
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
    public Path path;
    public Type type;
    public Integer quality;
    public Boolean fullPage;
    public Clip clip;
    public Boolean omitBackground;
    public Integer timeout;

    public ScreenshotOptions withPath(Path path) {
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
    public Integer timeout;
    public Frame.LoadState waitUntil;

    public SetContentOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
    public SetContentOptions withWaitUntil(Frame.LoadState waitUntil) {
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
  class TapOptions {
    public class Position {
      public int x;
      public int y;

      Position() {
      }
      public TapOptions done() {
        return TapOptions.this;
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
    public Boolean noWaitAfter;
    public Boolean force;
    public Integer timeout;

    public Position setPosition() {
      this.position = new Position();
      return this.position;
    }
    public TapOptions withModifiers(Keyboard.Modifier... modifiers) {
      this.modifiers = new HashSet<>(Arrays.asList(modifiers));
      return this;
    }
    public TapOptions withNoWaitAfter(Boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public TapOptions withForce(Boolean force) {
      this.force = force;
      return this;
    }
    public TapOptions withTimeout(Integer timeout) {
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
    public Frame.LoadState waitUntil;

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
    public WaitForNavigationOptions withWaitUntil(Frame.LoadState waitUntil) {
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
  /**
   * The method finds an element matching the specified selector within the page. If no elements match the selector, the return value resolves to {@code null}.
   * <p>
   * Shortcut for page.mainFrame().$(selector).
   * @param selector A selector to query page for. See working with selectors for more details.
   */
  ElementHandle querySelector(String selector);
  /**
   * The method finds all elements matching the specified selector within the page. If no elements match the selector, the return value resolves to {@code []}.
   * <p>
   * Shortcut for page.mainFrame().$$(selector).
   * @param selector A selector to query page for. See working with selectors for more details.
   */
  List<ElementHandle> querySelectorAll(String selector);
  default Object evalOnSelector(String selector, String pageFunction) {
    return evalOnSelector(selector, pageFunction, null);
  }
  /**
   * The method finds an element matching the specified selector within the page and passes it as a first argument to {@code pageFunction}. If no elements match the selector, the method throws an error.
   * <p>
   * If {@code pageFunction} returns a Promise, then {@code page.$eval} would wait for the promise to resolve and return its value.
   * <p>
   * Examples:
   * <p>
   * Shortcut for page.mainFrame().$eval(selector, pageFunction).
   * @param selector A selector to query page for. See working with selectors for more details.
   * @param pageFunction Function to be evaluated in browser context
   * @param arg Optional argument to pass to {@code pageFunction}
   * @return Promise which resolves to the return value of {@code pageFunction}
   */
  Object evalOnSelector(String selector, String pageFunction, Object arg);
  default Object evalOnSelectorAll(String selector, String pageFunction) {
    return evalOnSelectorAll(selector, pageFunction, null);
  }
  /**
   * The method finds all elements matching the specified selector within the page and passes an array of matched elements as a first argument to {@code pageFunction}.
   * <p>
   * If {@code pageFunction} returns a Promise, then {@code page.$$eval} would wait for the promise to resolve and return its value.
   * <p>
   * Examples:
   * <p>
   * 
   * @param selector A selector to query page for. See working with selectors for more details.
   * @param pageFunction Function to be evaluated in browser context
   * @param arg Optional argument to pass to {@code pageFunction}
   * @return Promise which resolves to the return value of {@code pageFunction}
   */
  Object evalOnSelectorAll(String selector, String pageFunction, Object arg);
  default void addInitScript(String script) {
    addInitScript(script, null);
  }
  /**
   * Adds a script which would be evaluated in one of the following scenarios:
   * <p>
   * Whenever the page is navigated.
   * <p>
   * Whenever the child frame is attached or navigated. In this case, the script is evaluated in the context of the newly attached frame.
   * <p>
   * The script is evaluated after the document was created but before any of its scripts were run. This is useful to amend  the JavaScript environment, e.g. to seed {@code Math.random}.
   * <p>
   * <strong>NOTE</strong> The order of evaluation of multiple scripts installed via browserContext.addInitScript(script[, arg]) and page.addInitScript(script[, arg]) is not defined.
   * @param script Script to be evaluated in the page.
   * @param arg Optional argument to pass to {@code script} (only supported when passing a function).
   */
  void addInitScript(String script, Object arg);
  /**
   * Adds a {@code <script>} tag into the page with the desired url or content.
   * <p>
   * Shortcut for page.mainFrame().addScriptTag(options).
   * @return which resolves to the added tag when the script's onload fires or when the script content was injected into frame.
   */
  ElementHandle addScriptTag(AddScriptTagOptions options);
  /**
   * Adds a {@code <link rel="stylesheet">} tag into the page with the desired url or a {@code <style type="text/css">} tag with the content.
   * <p>
   * Shortcut for page.mainFrame().addStyleTag(options).
   * @return which resolves to the added tag when the stylesheet's onload fires or when the CSS content was injected into frame.
   */
  ElementHandle addStyleTag(AddStyleTagOptions options);
  /**
   * Brings page to front (activates tab).
   */
  void bringToFront();
  default void check(String selector) {
    check(selector, null);
  }
  /**
   * This method checks an element matching {@code selector} by performing the following steps:
   * <p>
   * Find an element match matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.
   * <p>
   * Ensure that matched element is a checkbox or a radio input. If not, this method rejects. If the element is already checked, this method returns immediately.
   * <p>
   * Wait for actionability checks on the matched element, unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.
   * <p>
   * Scroll the element into view if needed.
   * <p>
   * Use page.mouse to click in the center of the element.
   * <p>
   * Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   * <p>
   * Ensure that the element is now checked. If not, this method rejects.
   * <p>
   * When all steps combined have not finished during the specified {@code timeout}, this method rejects with a TimeoutError. Passing zero timeout disables this.
   * <p>
   * Shortcut for page.mainFrame().check(selector[, options]).
   * @param selector A selector to search for checkbox or radio button to check. If there are multiple elements satisfying the selector, the first will be checked. See working with selectors for more details.
   * @return Promise that resolves when the element matching {@code selector} is successfully checked.
   */
  void check(String selector, CheckOptions options);
  default void click(String selector) {
    click(selector, null);
  }
  /**
   * This method clicks an element matching {@code selector} by performing the following steps:
   * <p>
   * Find an element match matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.
   * <p>
   * Wait for actionability checks on the matched element, unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.
   * <p>
   * Scroll the element into view if needed.
   * <p>
   * Use page.mouse to click in the center of the element, or the specified {@code position}.
   * <p>
   * Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   * <p>
   * When all steps combined have not finished during the specified {@code timeout}, this method rejects with a TimeoutError. Passing zero timeout disables this.
   * <p>
   * Shortcut for page.mainFrame().click(selector[, options]).
   * @param selector A selector to search for element to click. If there are multiple elements satisfying the selector, the first will be clicked. See working with selectors for more details.
   * @return Promise that resolves when the element matching {@code selector} is successfully clicked.
   */
  void click(String selector, ClickOptions options);
  default void close() {
    close(null);
  }
  /**
   * If {@code runBeforeUnload} is {@code false} the result will resolve only after the page has been closed.
   * <p>
   * If {@code runBeforeUnload} is {@code true} the method will **not** wait for the page to close.
   * <p>
   * By default, {@code page.close()} **does not** run beforeunload handlers.
   * <p>
   * <strong>NOTE</strong> if {@code runBeforeUnload} is passed as true, a {@code beforeunload} dialog might be summoned
   * <p>
   * and should be handled manually via page's 'dialog' event.
   */
  void close(CloseOptions options);
  /**
   * Gets the full HTML contents of the page, including the doctype.
   */
  String content();
  /**
   * Get the browser context that the page belongs to.
   */
  BrowserContext context();
  default void dblclick(String selector) {
    dblclick(selector, null);
  }
  /**
   * This method double clicks an element matching {@code selector} by performing the following steps:
   * <p>
   * Find an element match matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.
   * <p>
   * Wait for actionability checks on the matched element, unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.
   * <p>
   * Scroll the element into view if needed.
   * <p>
   * Use page.mouse to double click in the center of the element, or the specified {@code position}.
   * <p>
   * Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set. Note that if the first click of the {@code dblclick()} triggers a navigation event, this method will reject.
   * <p>
   * When all steps combined have not finished during the specified {@code timeout}, this method rejects with a TimeoutError. Passing zero timeout disables this.
   * <p>
   * <strong>NOTE</strong> {@code page.dblclick()} dispatches two {@code click} events and a single {@code dblclick} event.
   * <p>
   * Shortcut for page.mainFrame().dblclick(selector[, options]).
   * @param selector A selector to search for element to double click. If there are multiple elements satisfying the selector, the first will be double clicked. See working with selectors for more details.
   * @return Promise that resolves when the element matching {@code selector} is successfully double clicked.
   */
  void dblclick(String selector, DblclickOptions options);
  default void dispatchEvent(String selector, String type, Object eventInit) {
    dispatchEvent(selector, type, eventInit, null);
  }
  default void dispatchEvent(String selector, String type) {
    dispatchEvent(selector, type, null);
  }
  /**
   * The snippet below dispatches the {@code click} event on the element. Regardless of the visibility state of the elment, {@code click} is dispatched. This is equivalend to calling {@code element.click()}.
   * <p>
   * Under the hood, it creates an instance of an event based on the given {@code type}, initializes it with {@code eventInit} properties and dispatches it on the element. Events are {@code composed}, {@code cancelable} and bubble by default.
   * <p>
   * Since {@code eventInit} is event-specific, please refer to the events documentation for the lists of initial properties:
   * <p>
   * DragEvent
   * <p>
   * FocusEvent
   * <p>
   * KeyboardEvent
   * <p>
   * MouseEvent
   * <p>
   * PointerEvent
   * <p>
   * TouchEvent
   * <p>
   * Event
   * <p>
   * You can also specify {@code JSHandle} as the property value if you want live objects to be passed into the event:
   * <p>
   * 
   * @param selector A selector to search for element to use. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
   * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
   * @param eventInit event-specific initialization properties.
   */
  void dispatchEvent(String selector, String type, Object eventInit, DispatchEventOptions options);
  /**
   * 
   * <p>
   */
  void emulateMedia(EmulateMediaOptions options);
  default Object evaluate(String pageFunction) {
    return evaluate(pageFunction, null);
  }
  /**
   * If the function passed to the {@code page.evaluate} returns a Promise, then {@code page.evaluate} would wait for the promise to resolve and return its value.
   * <p>
   * If the function passed to the {@code page.evaluate} returns a non-Serializable value, then {@code page.evaluate} resolves to {@code undefined}. DevTools Protocol also supports transferring some additional values that are not serializable by {@code JSON}: {@code -0}, {@code NaN}, {@code Infinity}, {@code -Infinity}, and bigint literals.
   * <p>
   * Passing argument to {@code pageFunction}:
   * <p>
   * A string can also be passed in instead of a function:
   * <p>
   * ElementHandle instances can be passed as an argument to the {@code page.evaluate}:
   * <p>
   * Shortcut for page.mainFrame().evaluate(pageFunction[, arg]).
   * @param pageFunction Function to be evaluated in the page context
   * @param arg Optional argument to pass to {@code pageFunction}
   * @return Promise which resolves to the return value of {@code pageFunction}
   */
  Object evaluate(String pageFunction, Object arg);
  default JSHandle evaluateHandle(String pageFunction) {
    return evaluateHandle(pageFunction, null);
  }
  /**
   * The only difference between {@code page.evaluate} and {@code page.evaluateHandle} is that {@code page.evaluateHandle} returns in-page object (JSHandle).
   * <p>
   * If the function passed to the {@code page.evaluateHandle} returns a Promise, then {@code page.evaluateHandle} would wait for the promise to resolve and return its value.
   * <p>
   * A string can also be passed in instead of a function:
   * <p>
   * JSHandle instances can be passed as an argument to the {@code page.evaluateHandle}:
   * <p>
   * 
   * @param pageFunction Function to be evaluated in the page context
   * @param arg Optional argument to pass to {@code pageFunction}
   * @return Promise which resolves to the return value of {@code pageFunction} as in-page object (JSHandle)
   */
  JSHandle evaluateHandle(String pageFunction, Object arg);
  default void exposeBinding(String name, Binding playwrightBinding) {
    exposeBinding(name, playwrightBinding, null);
  }
  /**
   * The method adds a function called {@code name} on the {@code window} object of every frame in this page.
   * <p>
   * When called, the function executes {@code playwrightBinding} in Node.js and returns a Promise which resolves to the return value of {@code playwrightBinding}.
   * <p>
   * If the {@code playwrightBinding} returns a Promise, it will be awaited.
   * <p>
   * The first argument of the {@code playwrightBinding} function contains information about the caller:
   * <p>
   * {@code { browserContext: BrowserContext, page: Page, frame: Frame }}.
   * <p>
   * See browserContext.exposeBinding(name, playwrightBinding) for the context-wide version.
   * <p>
   * <strong>NOTE</strong> Functions installed via {@code page.exposeBinding} survive navigations.
   * <p>
   * 
   * @param name Name of the function on the window object.
   * @param playwrightBinding Callback function that will be called in the Playwright's context.
   */
  void exposeBinding(String name, Binding playwrightBinding, ExposeBindingOptions options);
  /**
   * The method adds a function called {@code name} on the {@code window} object of every frame in the page.
   * <p>
   * When called, the function executes {@code playwrightFunction} in Node.js and returns a Promise which resolves to the return value of {@code playwrightFunction}.
   * <p>
   * If the {@code playwrightFunction} returns a Promise, it will be awaited.
   * <p>
   * See browserContext.exposeFunction(name, playwrightFunction) for context-wide exposed function.
   * <p>
   * <strong>NOTE</strong> Functions installed via {@code page.exposeFunction} survive navigations.
   * <p>
   * 
   * @param name Name of the function on the window object
   * @param playwrightFunction Callback function which will be called in Playwright's context.
   */
  void exposeFunction(String name, Function playwrightFunction);
  default void fill(String selector, String value) {
    fill(selector, value, null);
  }
  /**
   * This method waits for an element matching {@code selector}, waits for actionability checks, focuses the element, fills it and triggers an {@code input} event after filling.
   * <p>
   * If the element matching {@code selector} is not an {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element, this method throws an error.
   * <p>
   * Note that you can pass an empty string to clear the input field.
   * <p>
   * To send fine-grained keyboard events, use {@code page.type}.
   * <p>
   * Shortcut for page.mainFrame().fill()
   * @param selector A selector to query page for. See working with selectors for more details.
   * @param value Value to fill for the {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element.
   */
  void fill(String selector, String value, FillOptions options);
  default void focus(String selector) {
    focus(selector, null);
  }
  /**
   * This method fetches an element with {@code selector} and focuses it.
   * <p>
   * If there's no element matching {@code selector}, the method waits until a matching element appears in the DOM.
   * <p>
   * Shortcut for page.mainFrame().focus(selector).
   * @param selector A selector of an element to focus. If there are multiple elements satisfying the selector, the first will be focused. See working with selectors for more details.
   * @return Promise which resolves when the element matching {@code selector} is successfully focused. The promise will be rejected if there is no element matching {@code selector}.
   */
  void focus(String selector, FocusOptions options);
  Frame frameByName(String name);
  Frame frameByUrl(String glob);
  Frame frameByUrl(Pattern pattern);
  /**
   * 
   * <p>
   * Returns frame matching the specified criteria. Either {@code name} or {@code url} must be specified.
   * @param options Frame name or other frame lookup options.
   * @return frame matching the criteria. Returns {@code null} if no frame matches.
   */
  Frame frameByUrl(Predicate<String> predicate);
  /**
   * 
   * @return An array of all frames attached to the page.
   */
  List<Frame> frames();
  default String getAttribute(String selector, String name) {
    return getAttribute(selector, name, null);
  }
  /**
   * Returns element attribute value.
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be picked. See working with selectors for more details.
   * @param name Attribute name to get the value for.
   */
  String getAttribute(String selector, String name, GetAttributeOptions options);
  default Response goBack() {
    return goBack(null);
  }
  /**
   * Navigate to the previous page in history.
   * @param options Navigation parameters which might have the following properties:
   * @return Promise which resolves to the main resource response. In case of multiple redirects, the navigation will resolve with the response of the last redirect. If
   * can not go back, resolves to {@code null}.
   */
  Response goBack(GoBackOptions options);
  default Response goForward() {
    return goForward(null);
  }
  /**
   * Navigate to the next page in history.
   * @param options Navigation parameters which might have the following properties:
   * @return Promise which resolves to the main resource response. In case of multiple redirects, the navigation will resolve with the response of the last redirect. If
   * can not go forward, resolves to {@code null}.
   */
  Response goForward(GoForwardOptions options);
  default Response navigate(String url) {
    return navigate(url, null);
  }
  /**
   * {@code page.goto} will throw an error if:
   * <p>
   * there's an SSL error (e.g. in case of self-signed certificates).
   * <p>
   * target URL is invalid.
   * <p>
   * the {@code timeout} is exceeded during navigation.
   * <p>
   * the remote server does not respond or is unreachable.
   * <p>
   * the main resource failed to load.
   * <p>
   * {@code page.goto} will not throw an error when any valid HTTP status code is returned by the remote server, including 404 "Not Found" and 500 "Internal Server Error".  The status code for such responses can be retrieved by calling response.status().
   * <p>
   * <strong>NOTE</strong> {@code page.goto} either throws an error or returns a main resource response. The only exceptions are navigation to {@code about:blank} or navigation to the same URL with a different hash, which would succeed and return {@code null}.
   * <p>
   * 
   * <p>
   * <strong>NOTE</strong> Headless mode doesn't support navigation to a PDF document. See the upstream issue.
   * <p>
   * Shortcut for page.mainFrame().goto(url[, options])
   * @param url URL to navigate page to. The url should include scheme, e.g. {@code https://}.
   * @param options Navigation parameters which might have the following properties:
   * @return Promise which resolves to the main resource response. In case of multiple redirects, the navigation will resolve with the response of the last redirect.
   */
  Response navigate(String url, NavigateOptions options);
  default void hover(String selector) {
    hover(selector, null);
  }
  /**
   * This method hovers over an element matching {@code selector} by performing the following steps:
   * <p>
   * Find an element match matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.
   * <p>
   * Wait for actionability checks on the matched element, unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.
   * <p>
   * Scroll the element into view if needed.
   * <p>
   * Use page.mouse to hover over the center of the element, or the specified {@code position}.
   * <p>
   * Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   * <p>
   * When all steps combined have not finished during the specified {@code timeout}, this method rejects with a TimeoutError. Passing zero timeout disables this.
   * <p>
   * Shortcut for page.mainFrame().hover(selector[, options]).
   * @param selector A selector to search for element to hover. If there are multiple elements satisfying the selector, the first will be hovered. See working with selectors for more details.
   * @return Promise that resolves when the element matching {@code selector} is successfully hovered.
   */
  void hover(String selector, HoverOptions options);
  default String innerHTML(String selector) {
    return innerHTML(selector, null);
  }
  /**
   * Resolves to the {@code element.innerHTML}.
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be picked. See working with selectors for more details.
   */
  String innerHTML(String selector, InnerHTMLOptions options);
  default String innerText(String selector) {
    return innerText(selector, null);
  }
  /**
   * Resolves to the {@code element.innerText}.
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be picked. See working with selectors for more details.
   */
  String innerText(String selector, InnerTextOptions options);
  /**
   * Indicates that the page has been closed.
   */
  boolean isClosed();
  /**
   * Page is guaranteed to have a main frame which persists during navigations.
   * @return The page's main frame.
   */
  Frame mainFrame();
  /**
   * 
   * @return Promise which resolves to the opener for popup pages and {@code null} for others. If the opener has been closed already the promise may resolve to {@code null}.
   */
  Page opener();
  default byte[] pdf() {
    return pdf(null);
  }
  /**
   * <strong>NOTE</strong> Generating a pdf is currently only supported in Chromium headless.
   * <p>
   * {@code page.pdf()} generates a pdf of the page with {@code print} css media. To generate a pdf with {@code screen} media, call page.emulateMedia({ media: 'screen' }) before calling {@code page.pdf()}:
   * <p>
   * <strong>NOTE</strong> By default, {@code page.pdf()} generates a pdf with modified colors for printing. Use the {@code -webkit-print-color-adjust} property to force rendering of exact colors.
   * <p>
   * 
   * <p>
   * The {@code width}, {@code height}, and {@code margin} options accept values labeled with units. Unlabeled values are treated as pixels.
   * <p>
   * A few examples:
   * <p>
   * {@code page.pdf({width: 100})} - prints with width set to 100 pixels
   * <p>
   * {@code page.pdf({width: '100px'})} - prints with width set to 100 pixels
   * <p>
   * {@code page.pdf({width: '10cm'})} - prints with width set to 10 centimeters.
   * <p>
   * All possible units are:
   * <p>
   * {@code px} - pixel
   * <p>
   * {@code in} - inch
   * <p>
   * {@code cm} - centimeter
   * <p>
   * {@code mm} - millimeter
   * <p>
   * The {@code format} options are:
   * <p>
   * {@code Letter}: 8.5in x 11in
   * <p>
   * {@code Legal}: 8.5in x 14in
   * <p>
   * {@code Tabloid}: 11in x 17in
   * <p>
   * {@code Ledger}: 17in x 11in
   * <p>
   * {@code A0}: 33.1in x 46.8in
   * <p>
   * {@code A1}: 23.4in x 33.1in
   * <p>
   * {@code A2}: 16.54in x 23.4in
   * <p>
   * {@code A3}: 11.7in x 16.54in
   * <p>
   * {@code A4}: 8.27in x 11.7in
   * <p>
   * {@code A5}: 5.83in x 8.27in
   * <p>
   * {@code A6}: 4.13in x 5.83in
   * <p>
   * 
   * <p>
   * <strong>NOTE</strong> {@code headerTemplate} and {@code footerTemplate} markup have the following limitations:
   * <p>
   * Script tags inside templates are not evaluated.
   * <p>
   * Page styles are not visible inside templates.
   * @param options Options object which might have the following properties:
   * @return Promise which resolves with PDF buffer.
   */
  byte[] pdf(PdfOptions options);
  default void press(String selector, String key) {
    press(selector, key, null);
  }
  /**
   * Focuses the element, and then uses {@code keyboard.down} and {@code keyboard.up}.
   * <p>
   * {@code key} can specify the intended keyboardEvent.key value or a single character to generate the text for. A superset of the {@code key} values can be found here. Examples of the keys are:
   * <p>
   * {@code F1} - {@code F12}, {@code Digit0}- {@code Digit9}, {@code KeyA}- {@code KeyZ}, {@code Backquote}, {@code Minus}, {@code Equal}, {@code Backslash}, {@code Backspace}, {@code Tab}, {@code Delete}, {@code Escape}, {@code ArrowDown}, {@code End}, {@code Enter}, {@code Home}, {@code Insert}, {@code PageDown}, {@code PageUp}, {@code ArrowRight}, {@code ArrowUp}, etc.
   * <p>
   * Following modification shortcuts are also suported: {@code Shift}, {@code Control}, {@code Alt}, {@code Meta}, {@code ShiftLeft}.
   * <p>
   * Holding down {@code Shift} will type the text that corresponds to the {@code key} in the upper case.
   * <p>
   * If {@code key} is a single character, it is case-sensitive, so the values {@code a} and {@code A} will generate different respective texts.
   * <p>
   * Shortcuts such as {@code key: "Control+o"} or {@code key: "Control+Shift+T"} are supported as well. When speficied with the modifier, modifier is pressed and being held while the subsequent key is being pressed.
   * <p>
   * 
   * @param selector A selector of an element to type into. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
   * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
   */
  void press(String selector, String key, PressOptions options);
  default Response reload() {
    return reload(null);
  }
  /**
   * 
   * @param options Navigation parameters which might have the following properties:
   * @return Promise which resolves to the main resource response. In case of multiple redirects, the navigation will resolve with the response of the last redirect.
   */
  Response reload(ReloadOptions options);
  void route(String url, Consumer<Route> handler);
  void route(Pattern url, Consumer<Route> handler);
  /**
   * Routing provides the capability to modify network requests that are made by a page.
   * <p>
   * Once routing is enabled, every request matching the url pattern will stall unless it's continued, fulfilled or aborted.
   * <p>
   * <strong>NOTE</strong> The handler will only be called for the first url if the response is a redirect.
   * <p>
   * or the same snippet using a regex pattern instead:
   * <p>
   * Page routes take precedence over browser context routes (set up with browserContext.route(url, handler)) when request matches both handlers.
   * <p>
   * <strong>NOTE</strong> Enabling routing disables http cache.
   * @param url A glob pattern, regex pattern or predicate receiving URL to match while routing.
   * @param handler handler function to route the request.
   * @return .
   */
  void route(Predicate<String> url, Consumer<Route> handler);
  default byte[] screenshot() {
    return screenshot(null);
  }
  /**
   * <strong>NOTE</strong> Screenshots take at least 1/6 second on Chromium OS X and Chromium Windows. See https://crbug.com/741689 for discussion.
   * @param options Options object which might have the following properties:
   * @return Promise which resolves to buffer with the captured screenshot.
   */
  byte[] screenshot(ScreenshotOptions options);
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
  /**
   * Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   * <p>
   * If there's no {@code <select>} element matching {@code selector}, the method throws an error.
   * <p>
   * Shortcut for page.mainFrame().selectOption()
   * @param selector A selector to query page for. See working with selectors for more details.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option is considered matching if all specified properties match.
   * @return An array of option values that have been successfully selected.
   */
  List<String> selectOption(String selector, ElementHandle[] values, SelectOptionOptions options);
  default void setContent(String html) {
    setContent(html, null);
  }
  /**
   * 
   * @param html HTML markup to assign to the page.
   * @param options Parameters which might have the following properties:
   */
  void setContent(String html, SetContentOptions options);
  /**
   * This setting will change the default maximum navigation time for the following methods and related shortcuts:
   * <p>
   * page.goBack([options])
   * <p>
   * page.goForward([options])
   * <p>
   * page.goto(url[, options])
   * <p>
   * page.reload([options])
   * <p>
   * page.setContent(html[, options])
   * <p>
   * page.waitForNavigation([options])
   * <p>
   * 
   * <p>
   * <strong>NOTE</strong> {@code page.setDefaultNavigationTimeout} takes priority over {@code page.setDefaultTimeout}, {@code browserContext.setDefaultTimeout} and {@code browserContext.setDefaultNavigationTimeout}.
   * @param timeout Maximum navigation time in milliseconds
   */
  void setDefaultNavigationTimeout(int timeout);
  /**
   * This setting will change the default maximum time for all the methods accepting {@code timeout} option.
   * <p>
   * <strong>NOTE</strong> {@code page.setDefaultNavigationTimeout} takes priority over {@code page.setDefaultTimeout}.
   * @param timeout Maximum time in milliseconds
   */
  void setDefaultTimeout(int timeout);
  /**
   * The extra HTTP headers will be sent with every request the page initiates.
   * <p>
   * <strong>NOTE</strong> page.setExtraHTTPHeaders does not guarantee the order of headers in the outgoing requests.
   * @param headers An object containing additional HTTP headers to be sent with every request. All header values must be strings.
   */
  void setExtraHTTPHeaders(Map<String, String> headers);
  default void setInputFiles(String selector, Path file) { setInputFiles(selector, file, null); }
  default void setInputFiles(String selector, Path file, SetInputFilesOptions options) { setInputFiles(selector, new Path[]{ file }, options); }
  default void setInputFiles(String selector, Path[] files) { setInputFiles(selector, files, null); }
  void setInputFiles(String selector, Path[] files, SetInputFilesOptions options);
  default void setInputFiles(String selector, FileChooser.FilePayload file) { setInputFiles(selector, file, null); }
  default void setInputFiles(String selector, FileChooser.FilePayload file, SetInputFilesOptions options)  { setInputFiles(selector, new FileChooser.FilePayload[]{ file }, options); }
  default void setInputFiles(String selector, FileChooser.FilePayload[] files) { setInputFiles(selector, files, null); }
  /**
   * This method expects {@code selector} to point to an input element.
   * <p>
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they are resolved relative to the current working directory. For empty array, clears the selected files.
   * @param selector A selector to search for element to click. If there are multiple elements satisfying the selector, the first will be clicked. See working with selectors for more details.
   */
  void setInputFiles(String selector, FileChooser.FilePayload[] files, SetInputFilesOptions options);
  /**
   * In the case of multiple pages in a single browser, each page can have its own viewport size. However, browser.newContext([options]) allows to set viewport size (and more) for all pages in the context at once.
   * <p>
   * {@code page.setViewportSize} will resize the page. A lot of websites don't expect phones to change size, so you should set the viewport size before navigating to the page.
   * <p>
   */
  void setViewportSize(int width, int height);
  default void tap(String selector) {
    tap(selector, null);
  }
  /**
   * This method taps an element matching {@code selector} by performing the following steps:
   * <p>
   * Find an element match matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.
   * <p>
   * Wait for actionability checks on the matched element, unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.
   * <p>
   * Scroll the element into view if needed.
   * <p>
   * Use page.touchscreen to tap the center of the element, or the specified {@code position}.
   * <p>
   * Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   * <p>
   * When all steps combined have not finished during the specified {@code timeout}, this method rejects with a TimeoutError. Passing zero timeout disables this.
   * <p>
   * <strong>NOTE</strong> {@code page.tap()} requires that the {@code hasTouch} option of the browser context be set to true.
   * <p>
   * Shortcut for page.mainFrame().tap().
   * @param selector A selector to search for element to tap. If there are multiple elements satisfying the selector, the first will be tapped. See working with selectors for more details.
   * @return Promise that resolves when the element matching {@code selector} is successfully tapped.
   */
  void tap(String selector, TapOptions options);
  default String textContent(String selector) {
    return textContent(selector, null);
  }
  /**
   * Resolves to the {@code element.textContent}.
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be picked. See working with selectors for more details.
   */
  String textContent(String selector, TextContentOptions options);
  /**
   * Shortcut for page.mainFrame().title().
   * @return The page's title.
   */
  String title();
  default void type(String selector, String text) {
    type(selector, text, null);
  }
  /**
   * Sends a {@code keydown}, {@code keypress}/{@code input}, and {@code keyup} event for each character in the text. {@code page.type} can be used to send fine-grained keyboard events. To fill values in form fields, use {@code page.fill}.
   * <p>
   * To press a special key, like {@code Control} or {@code ArrowDown}, use {@code keyboard.press}.
   * <p>
   * Shortcut for page.mainFrame().type(selector, text[, options]).
   * @param selector A selector of an element to type into. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
   * @param text A text to type into a focused element.
   */
  void type(String selector, String text, TypeOptions options);
  default void uncheck(String selector) {
    uncheck(selector, null);
  }
  /**
   * This method unchecks an element matching {@code selector} by performing the following steps:
   * <p>
   * Find an element match matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.
   * <p>
   * Ensure that matched element is a checkbox or a radio input. If not, this method rejects. If the element is already unchecked, this method returns immediately.
   * <p>
   * Wait for actionability checks on the matched element, unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.
   * <p>
   * Scroll the element into view if needed.
   * <p>
   * Use page.mouse to click in the center of the element.
   * <p>
   * Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   * <p>
   * Ensure that the element is now unchecked. If not, this method rejects.
   * <p>
   * When all steps combined have not finished during the specified {@code timeout}, this method rejects with a TimeoutError. Passing zero timeout disables this.
   * <p>
   * Shortcut for page.mainFrame().uncheck(selector[, options]).
   * @param selector A selector to search for uncheckbox to check. If there are multiple elements satisfying the selector, the first will be checked. See working with selectors for more details.
   * @return Promise that resolves when the element matching {@code selector} is successfully unchecked.
   */
  void uncheck(String selector, UncheckOptions options);
  default void unroute(String url) { unroute(url, null); }
  default void unroute(Pattern url) { unroute(url, null); }
  default void unroute(Predicate<String> url) { unroute(url, null); }
  void unroute(String url, Consumer<Route> handler);
  void unroute(Pattern url, Consumer<Route> handler);
  /**
   * Removes a route created with page.route(url, handler). When {@code handler} is not specified, removes all routes for the {@code url}.
   * @param url A glob pattern, regex pattern or predicate receiving URL to match while routing.
   * @param handler Handler function to route the request.
   */
  void unroute(Predicate<String> url, Consumer<Route> handler);
  /**
   * This is a shortcut for page.mainFrame().url()
   */
  String url();
  /**
   * Video object associated with this page.
   */
  Video video();
  Viewport viewportSize();
  default Deferred<Event<EventType>> waitForEvent(EventType event) {
    return waitForEvent(event, (WaitForEventOptions) null);
  }
  default Deferred<Event<EventType>> waitForEvent(EventType event, Predicate<Event<EventType>> predicate) {
    WaitForEventOptions options = new WaitForEventOptions();
    options.predicate = predicate;
    return waitForEvent(event, options);
  }
  /**
   * Waits for event to fire and passes its value into the predicate function. Resolves when the predicate returns truthy value. Will throw an error if the page is closed before the event
   * <p>
   * is fired.
   * @param event Event name, same one would pass into {@code page.on(event)}.
   * @param optionsOrPredicate Either a predicate that receives an event or an options object.
   * @return Promise which resolves to the event data value.
   */
  Deferred<Event<EventType>> waitForEvent(EventType event, WaitForEventOptions options);
  default Deferred<JSHandle> waitForFunction(String pageFunction, Object arg) {
    return waitForFunction(pageFunction, arg, null);
  }
  default Deferred<JSHandle> waitForFunction(String pageFunction) {
    return waitForFunction(pageFunction, null);
  }
  /**
   * The {@code waitForFunction} can be used to observe viewport size change:
   * <p>
   * To pass an argument from Node.js to the predicate of {@code page.waitForFunction} function:
   * <p>
   * Shortcut for page.mainFrame().waitForFunction(pageFunction[, arg, options]).
   * @param pageFunction Function to be evaluated in browser context
   * @param arg Optional argument to pass to {@code pageFunction}
   * @param options Optional waiting parameters
   * @return Promise which resolves when the {@code pageFunction} returns a truthy value. It resolves to a JSHandle of the truthy value.
   */
  Deferred<JSHandle> waitForFunction(String pageFunction, Object arg, WaitForFunctionOptions options);
  default Deferred<Void> waitForLoadState(LoadState state) {
    return waitForLoadState(state, null);
  }
  default Deferred<Void> waitForLoadState() {
    return waitForLoadState(null);
  }
  /**
   * This resolves when the page reaches a required load state, {@code load} by default. The navigation must have been committed when this method is called. If current document has already reached the required state, resolves immediately.
   * <p>
   * 
   * <p>
   * Shortcut for page.mainFrame().waitForLoadState([options]).
   * @param state Load state to wait for, defaults to {@code load}. If the state has been already reached while loading current document, the method resolves immediately.
   *  - {@code 'load'} - wait for the {@code load} event to be fired.
   *  - {@code 'domcontentloaded'} - wait for the {@code DOMContentLoaded} event to be fired.
   *  - {@code 'networkidle'} - wait until there are no network connections for at least {@code 500} ms.
   * @return Promise which resolves when the required load state has been reached.
   */
  Deferred<Void> waitForLoadState(LoadState state, WaitForLoadStateOptions options);
  default Deferred<Response> waitForNavigation() {
    return waitForNavigation(null);
  }
  /**
   * This resolves when the page navigates to a new URL or reloads. It is useful for when you run code
   * <p>
   * which will indirectly cause the page to navigate. e.g. The click target has an {@code onclick} handler that triggers navigation from a {@code setTimeout}. Consider this example:
   * <p>
   * <strong>NOTE</strong> Usage of the History API to change the URL is considered a navigation.
   * <p>
   * Shortcut for page.mainFrame().waitForNavigation(options).
   * @param options Navigation parameters which might have the following properties:
   * @return Promise which resolves to the main resource response. In case of multiple redirects, the navigation will resolve with the response of the last redirect. In case of navigation to a different anchor or navigation due to History API usage, the navigation will resolve with {@code null}.
   */
  Deferred<Response> waitForNavigation(WaitForNavigationOptions options);
  default Deferred<Request> waitForRequest(String urlOrPredicate) {
    return waitForRequest(urlOrPredicate, null);
  }
  /**
   * 
   * <p>
   * 
   * @param urlOrPredicate Request URL string, regex or predicate receiving Request object.
   * @param options Optional waiting parameters
   * @return Promise which resolves to the matched request.
   */
  Deferred<Request> waitForRequest(String urlOrPredicate, WaitForRequestOptions options);
  default Deferred<Response> waitForResponse(String urlOrPredicate) {
    return waitForResponse(urlOrPredicate, null);
  }
  /**
   * 
   * @param urlOrPredicate Request URL string, regex or predicate receiving Response object.
   * @param options Optional waiting parameters
   * @return Promise which resolves to the matched response.
   */
  Deferred<Response> waitForResponse(String urlOrPredicate, WaitForResponseOptions options);
  default Deferred<ElementHandle> waitForSelector(String selector) {
    return waitForSelector(selector, null);
  }
  /**
   * Wait for the {@code selector} to satisfy {@code state} option (either appear/disappear from dom, or become visible/hidden). If at the moment of calling the method {@code selector} already satisfies the condition, the method will return immediately. If the selector doesn't satisfy the condition for the {@code timeout} milliseconds, the function will throw.
   * <p>
   * This method works across navigations:
   * <p>
   * Shortcut for page.mainFrame().waitForSelector(selector[, options]).
   * @param selector A selector of an element to wait for. See working with selectors for more details.
   * @return Promise which resolves when element specified by selector satisfies {@code state} option. Resolves to {@code null} if waiting for {@code hidden} or {@code detached}.
   */
  Deferred<ElementHandle> waitForSelector(String selector, WaitForSelectorOptions options);
  /**
   * Returns a promise that resolves after the timeout.
   * <p>
   * Note that {@code page.waitForTimeout()} should only be used for debugging. Tests using the timer in production are going to be flaky. Use signals such as network events, selectors becoming visible and others instead.
   * <p>
   * Shortcut for page.mainFrame().waitForTimeout(timeout).
   * @param timeout A timeout to wait for
   */
  Deferred<Void> waitForTimeout(int timeout);
  /**
   * <strong>NOTE</strong> This does not contain ServiceWorkers
   * @return This method returns all of the dedicated WebWorkers associated with the page.
   */
  List<Worker> workers();
  Accessibility accessibility();
  /**
   * Browser-specific Coverage implementation, only available for Chromium atm. See ChromiumCoverage for more details.
   */
  ChromiumCoverage coverage();
  Keyboard keyboard();
  Mouse mouse();
  Touchscreen touchscreen();
}

