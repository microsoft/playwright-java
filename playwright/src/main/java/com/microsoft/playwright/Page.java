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
 * Page provides methods to interact with a single tab in a Browser, or an extension background
 * <p>
 * page in Chromium. One Browser instance might have multiple
 * <p>
 * Page instances.
 * <p>
 * The Page class emits various events (described below) which can be handled using any of Node's native
 * <p>
 * {@code EventEmitter} methods, such as {@code on}, {@code once} or
 * <p>
 * {@code removeListener}.
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
  class AddScriptTagScript {
    /**
     * URL of a script to be added.
     */
    public String url;
    /**
     * Path to the JavaScript file to be injected into frame. If {@code path} is a relative path, then it is resolved relative to current working directory.
     */
    public Path path;
    /**
     * Raw JavaScript content to be injected into frame.
     */
    public String content;
    /**
     * Script type. Use 'module' in order to load a Javascript ES6 module. See script for more details.
     */
    public String type;

    public AddScriptTagScript withUrl(String url) {
      this.url = url;
      return this;
    }
    public AddScriptTagScript withPath(Path path) {
      this.path = path;
      return this;
    }
    public AddScriptTagScript withContent(String content) {
      this.content = content;
      return this;
    }
    public AddScriptTagScript withType(String type) {
      this.type = type;
      return this;
    }
  }
  class AddStyleTagStyle {
    /**
     * URL of the {@code <link>} tag.
     */
    public String url;
    /**
     * Path to the CSS file to be injected into frame. If {@code path} is a relative path, then it is resolved relative to current working directory.
     */
    public Path path;
    /**
     * Raw CSS content to be injected into frame.
     */
    public String content;

    public AddStyleTagStyle withUrl(String url) {
      this.url = url;
      return this;
    }
    public AddStyleTagStyle withPath(Path path) {
      this.path = path;
      return this;
    }
    public AddStyleTagStyle withContent(String content) {
      this.content = content;
      return this;
    }
  }
  class CheckOptions {
    /**
     * Whether to bypass the actionability checks. Defaults to {@code false}.
     */
    public Boolean force;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the browserContext.setDefaultTimeout(timeout) or page.setDefaultTimeout(timeout) methods.
     */
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
    /**
     * Defaults to {@code left}.
     */
    public Mouse.Button button;
    /**
     * defaults to 1. See UIEvent.detail.
     */
    public Integer clickCount;
    /**
     * Time to wait between {@code mousedown} and {@code mouseup} in milliseconds. Defaults to 0.
     */
    public Integer delay;
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the element.
     */
    public Position position;
    /**
     * Modifier keys to press. Ensures that only these modifiers are pressed during the operation, and then restores current modifiers back. If not specified, currently pressed modifiers are used.
     */
    public Set<Keyboard.Modifier> modifiers;
    /**
     * Whether to bypass the actionability checks. Defaults to {@code false}.
     */
    public Boolean force;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the browserContext.setDefaultTimeout(timeout) or page.setDefaultTimeout(timeout) methods.
     */
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
    /**
     * Defaults to {@code false}. Whether to run the before unload page handlers.
     */
    public Boolean runBeforeUnload;

    public CloseOptions withRunBeforeUnload(Boolean runBeforeUnload) {
      this.runBeforeUnload = runBeforeUnload;
      return this;
    }
  }
  class DblclickOptions {
    /**
     * Defaults to {@code left}.
     */
    public Mouse.Button button;
    /**
     * Time to wait between {@code mousedown} and {@code mouseup} in milliseconds. Defaults to 0.
     */
    public Integer delay;
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the element.
     */
    public Position position;
    /**
     * Modifier keys to press. Ensures that only these modifiers are pressed during the operation, and then restores current modifiers back. If not specified, currently pressed modifiers are used.
     */
    public Set<Keyboard.Modifier> modifiers;
    /**
     * Whether to bypass the actionability checks. Defaults to {@code false}.
     */
    public Boolean force;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the browserContext.setDefaultTimeout(timeout) or page.setDefaultTimeout(timeout) methods.
     */
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
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the browserContext.setDefaultTimeout(timeout) or page.setDefaultTimeout(timeout) methods.
     */
    public Integer timeout;

    public DispatchEventOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class EmulateMediaParams {
    public enum Media { PRINT, SCREEN }
    /**
     * Changes the CSS media type of the page. The only allowed values are {@code 'screen'}, {@code 'print'} and {@code null}. Passing {@code null} disables CSS media emulation. Omitting {@code media} or passing {@code undefined} does not change the emulated value.
     */
    public Optional<Media> media;
    /**
     * Emulates {@code 'prefers-colors-scheme'} media feature, supported values are {@code 'light'}, {@code 'dark'}, {@code 'no-preference'}. Passing {@code null} disables color scheme emulation. Omitting {@code colorScheme} or passing {@code undefined} does not change the emulated value.
     */
    public Optional<ColorScheme> colorScheme;

    public EmulateMediaParams withMedia(Media media) {
      this.media = Optional.ofNullable(media);
      return this;
    }
    public EmulateMediaParams withColorScheme(ColorScheme colorScheme) {
      this.colorScheme = Optional.ofNullable(colorScheme);
      return this;
    }
  }
  class ExposeBindingOptions {
    /**
     * Whether to pass the argument as a handle, instead of passing by value. When passing a handle, only one argument is supported. When passing by value, multiple arguments are supported.
     */
    public Boolean handle;

    public ExposeBindingOptions withHandle(Boolean handle) {
      this.handle = handle;
      return this;
    }
  }
  class FillOptions {
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the browserContext.setDefaultTimeout(timeout) or page.setDefaultTimeout(timeout) methods.
     */
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
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the browserContext.setDefaultTimeout(timeout) or page.setDefaultTimeout(timeout) methods.
     */
    public Integer timeout;

    public FocusOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class GetAttributeOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the browserContext.setDefaultTimeout(timeout) or page.setDefaultTimeout(timeout) methods.
     */
    public Integer timeout;

    public GetAttributeOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class GoBackOptions {
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the browserContext.setDefaultNavigationTimeout(timeout), browserContext.setDefaultTimeout(timeout), page.setDefaultNavigationTimeout(timeout) or page.setDefaultTimeout(timeout) methods.
     */
    public Integer timeout;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     *  - {@code 'domcontentloaded'} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.
     *  - {@code 'load'} - consider operation to be finished when the {@code load} event is fired.
     *  - {@code 'networkidle'} - consider operation to be finished when there are no network connections for at least {@code 500} ms.
     */
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
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the browserContext.setDefaultNavigationTimeout(timeout), browserContext.setDefaultTimeout(timeout), page.setDefaultNavigationTimeout(timeout) or page.setDefaultTimeout(timeout) methods.
     */
    public Integer timeout;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     *  - {@code 'domcontentloaded'} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.
     *  - {@code 'load'} - consider operation to be finished when the {@code load} event is fired.
     *  - {@code 'networkidle'} - consider operation to be finished when there are no network connections for at least {@code 500} ms.
     */
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
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the browserContext.setDefaultNavigationTimeout(timeout), browserContext.setDefaultTimeout(timeout), page.setDefaultNavigationTimeout(timeout) or page.setDefaultTimeout(timeout) methods.
     */
    public Integer timeout;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     *  - {@code 'domcontentloaded'} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.
     *  - {@code 'load'} - consider operation to be finished when the {@code load} event is fired.
     *  - {@code 'networkidle'} - consider operation to be finished when there are no network connections for at least {@code 500} ms.
     */
    public Frame.LoadState waitUntil;
    /**
     * Referer header value. If provided it will take preference over the referer header value set by page.setExtraHTTPHeaders(headers).
     */
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
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the element.
     */
    public Position position;
    /**
     * Modifier keys to press. Ensures that only these modifiers are pressed during the operation, and then restores current modifiers back. If not specified, currently pressed modifiers are used.
     */
    public Set<Keyboard.Modifier> modifiers;
    /**
     * Whether to bypass the actionability checks. Defaults to {@code false}.
     */
    public Boolean force;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the browserContext.setDefaultTimeout(timeout) or page.setDefaultTimeout(timeout) methods.
     */
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
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the browserContext.setDefaultTimeout(timeout) or page.setDefaultTimeout(timeout) methods.
     */
    public Integer timeout;

    public InnerHTMLOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class InnerTextOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the browserContext.setDefaultTimeout(timeout) or page.setDefaultTimeout(timeout) methods.
     */
    public Integer timeout;

    public InnerTextOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class PdfOptions {
    public class Margin {
      /**
       * Top margin, accepts values labeled with units. Defaults to {@code 0}.
       */
      public String top;
      /**
       * Right margin, accepts values labeled with units. Defaults to {@code 0}.
       */
      public String right;
      /**
       * Bottom margin, accepts values labeled with units. Defaults to {@code 0}.
       */
      public String bottom;
      /**
       * Left margin, accepts values labeled with units. Defaults to {@code 0}.
       */
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
    /**
     * The file path to save the PDF to. If {@code path} is a relative path, then it is resolved relative to current working directory. If no path is provided, the PDF won't be saved to the disk.
     */
    public Path path;
    /**
     * Scale of the webpage rendering. Defaults to {@code 1}. Scale amount must be between 0.1 and 2.
     */
    public Integer scale;
    /**
     * Display header and footer. Defaults to {@code false}.
     */
    public Boolean displayHeaderFooter;
    /**
     * HTML template for the print header. Should be valid HTML markup with following classes used to inject printing values into them:
     *  - {@code 'date'} formatted print date
     *  - {@code 'title'} document title
     *  - {@code 'url'} document location
     *  - {@code 'pageNumber'} current page number
     *  - {@code 'totalPages'} total pages in the document
     */
    public String headerTemplate;
    /**
     * HTML template for the print footer. Should use the same format as the {@code headerTemplate}.
     */
    public String footerTemplate;
    /**
     * Print background graphics. Defaults to {@code false}.
     */
    public Boolean printBackground;
    /**
     * Paper orientation. Defaults to {@code false}.
     */
    public Boolean landscape;
    /**
     * Paper ranges to print, e.g., '1-5, 8, 11-13'. Defaults to the empty string, which means print all pages.
     */
    public String pageRanges;
    /**
     * Paper format. If set, takes priority over {@code width} or {@code height} options. Defaults to 'Letter'.
     */
    public String format;
    /**
     * Paper width, accepts values labeled with units.
     */
    public String width;
    /**
     * Paper height, accepts values labeled with units.
     */
    public String height;
    /**
     * Paper margins, defaults to none.
     */
    public Margin margin;
    /**
     * Give any CSS {@code @page} size declared in the page priority over what is declared in {@code width} and {@code height} or {@code format} options. Defaults to {@code false}, which will scale the content to fit the paper size.
     */
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
    /**
     * Time to wait between {@code keydown} and {@code keyup} in milliseconds. Defaults to 0.
     */
    public Integer delay;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the browserContext.setDefaultTimeout(timeout) or page.setDefaultTimeout(timeout) methods.
     */
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
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the browserContext.setDefaultNavigationTimeout(timeout), browserContext.setDefaultTimeout(timeout), page.setDefaultNavigationTimeout(timeout) or page.setDefaultTimeout(timeout) methods.
     */
    public Integer timeout;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     *  - {@code 'domcontentloaded'} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.
     *  - {@code 'load'} - consider operation to be finished when the {@code load} event is fired.
     *  - {@code 'networkidle'} - consider operation to be finished when there are no network connections for at least {@code 500} ms.
     */
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
      /**
       * x-coordinate of top-left corner of clip area
       */
      public int x;
      /**
       * y-coordinate of top-left corner of clip area
       */
      public int y;
      /**
       * width of clipping area
       */
      public int width;
      /**
       * height of clipping area
       */
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
    /**
     * The file path to save the image to. The screenshot type will be inferred from file extension. If {@code path} is a relative path, then it is resolved relative to current working directory. If no path is provided, the image won't be saved to the disk.
     */
    public Path path;
    /**
     * Specify screenshot type, defaults to {@code png}.
     */
    public Type type;
    /**
     * The quality of the image, between 0-100. Not applicable to {@code png} images.
     */
    public Integer quality;
    /**
     * When true, takes a screenshot of the full scrollable page, instead of the currently visible viewport. Defaults to {@code false}.
     */
    public Boolean fullPage;
    /**
     * An object which specifies clipping of the resulting image. Should have the following fields:
     */
    public Clip clip;
    /**
     * Hides default white background and allows capturing screenshots with transparency. Not applicable to {@code jpeg} images. Defaults to {@code false}.
     */
    public Boolean omitBackground;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the browserContext.setDefaultTimeout(timeout) or page.setDefaultTimeout(timeout) methods.
     */
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
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the browserContext.setDefaultTimeout(timeout) or page.setDefaultTimeout(timeout) methods.
     */
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
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the browserContext.setDefaultNavigationTimeout(timeout), browserContext.setDefaultTimeout(timeout), page.setDefaultNavigationTimeout(timeout) or page.setDefaultTimeout(timeout) methods.
     */
    public Integer timeout;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     *  - {@code 'domcontentloaded'} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.
     *  - {@code 'load'} - consider operation to be finished when the {@code load} event is fired.
     *  - {@code 'networkidle'} - consider operation to be finished when there are no network connections for at least {@code 500} ms.
     */
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
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the browserContext.setDefaultTimeout(timeout) or page.setDefaultTimeout(timeout) methods.
     */
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
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the element.
     */
    public Position position;
    /**
     * Modifier keys to press. Ensures that only these modifiers are pressed during the operation, and then restores current modifiers back. If not specified, currently pressed modifiers are used.
     */
    public Set<Keyboard.Modifier> modifiers;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Whether to bypass the actionability checks. Defaults to {@code false}.
     */
    public Boolean force;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the browserContext.setDefaultTimeout(timeout) or page.setDefaultTimeout(timeout) methods.
     */
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
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the browserContext.setDefaultTimeout(timeout) or page.setDefaultTimeout(timeout) methods.
     */
    public Integer timeout;

    public TextContentOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class TypeOptions {
    /**
     * Time to wait between key presses in milliseconds. Defaults to 0.
     */
    public Integer delay;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the browserContext.setDefaultTimeout(timeout) or page.setDefaultTimeout(timeout) methods.
     */
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
    /**
     * Whether to bypass the actionability checks. Defaults to {@code false}.
     */
    public Boolean force;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the browserContext.setDefaultTimeout(timeout) or page.setDefaultTimeout(timeout) methods.
     */
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
    /**
     * If {@code polling} is {@code 'raf'}, then {@code pageFunction} is constantly executed in {@code requestAnimationFrame} callback. If {@code polling} is a number, then it is treated as an interval in milliseconds at which the function would be executed. Defaults to {@code raf}.
     */
    public Integer pollingInterval;
    /**
     * maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default value can be changed by using the browserContext.setDefaultTimeout(timeout).
     */
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
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the browserContext.setDefaultNavigationTimeout(timeout), browserContext.setDefaultTimeout(timeout), page.setDefaultNavigationTimeout(timeout) or page.setDefaultTimeout(timeout) methods.
     */
    public Integer timeout;

    public WaitForLoadStateOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForNavigationOptions {
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the browserContext.setDefaultNavigationTimeout(timeout), browserContext.setDefaultTimeout(timeout), page.setDefaultNavigationTimeout(timeout) or page.setDefaultTimeout(timeout) methods.
     */
    public Integer timeout;
    /**
     * A glob pattern, regex pattern or predicate receiving URL to match while waiting for the navigation.
     */
    public String glob;
    public Pattern pattern;
    public Predicate<String> predicate;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     *  - {@code 'domcontentloaded'} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.
     *  - {@code 'load'} - consider operation to be finished when the {@code load} event is fired.
     *  - {@code 'networkidle'} - consider operation to be finished when there are no network connections for at least {@code 500} ms.
     */
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
    /**
     * Maximum wait time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable the timeout. The default value can be changed by using the page.setDefaultTimeout(timeout) method.
     */
    public Integer timeout;

    public WaitForRequestOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForResponseOptions {
    /**
     * Maximum wait time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable the timeout. The default value can be changed by using the browserContext.setDefaultTimeout(timeout) or page.setDefaultTimeout(timeout) methods.
     */
    public Integer timeout;

    public WaitForResponseOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForSelectorOptions {
    public enum State { ATTACHED, DETACHED, HIDDEN, VISIBLE }
    /**
     * Defaults to {@code 'visible'}. Can be either:
     *  - {@code 'attached'} - wait for element to be present in DOM.
     *  - {@code 'detached'} - wait for element to not be present in DOM.
     *  - {@code 'visible'} - wait for element to have non-empty bounding box and no {@code visibility:hidden}. Note that element without any content or with {@code display:none} has an empty bounding box and is not considered visible.
     *  - {@code 'hidden'} - wait for element to be either detached from DOM, or have an empty bounding box or {@code visibility:hidden}. This is opposite to the {@code 'visible'} option.
     */
    public State state;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by using the browserContext.setDefaultTimeout(timeout) or page.setDefaultTimeout(timeout) methods.
     */
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
   * The method finds an element matching the specified selector within the page. If no elements match the selector, the
   * <p>
   * return value resolves to {@code null}.
   * <p>
   * Shortcut for main frame's frame.$(selector).
   * @param selector A selector to query for. See working with selectors for more details.
   */
  ElementHandle querySelector(String selector);
  /**
   * The method finds all elements matching the specified selector within the page. If no elements match the selector, the
   * <p>
   * return value resolves to {@code []}.
   * <p>
   * Shortcut for main frame's frame.$$(selector).
   * @param selector A selector to query for. See working with selectors for more details.
   */
  List<ElementHandle> querySelectorAll(String selector);
  default Object evalOnSelector(String selector, String pageFunction) {
    return evalOnSelector(selector, pageFunction, null);
  }
  /**
   * The method finds an element matching the specified selector within the page and passes it as a first argument to
   * <p>
   * {@code pageFunction}. If no elements match the selector, the method throws an error. Returns the value of {@code pageFunction}.
   * <p>
   * If {@code pageFunction} returns a Promise, then {@code page.$eval} would wait for the promise to resolve and return its value.
   * <p>
   * Examples:
   * <p>
   * Shortcut for main frame's frame.$eval(selector, pageFunction[, arg]).
   * @param selector A selector to query for. See working with selectors for more details.
   * @param pageFunction Function to be evaluated in browser context
   * @param arg Optional argument to pass to {@code pageFunction}
   */
  Object evalOnSelector(String selector, String pageFunction, Object arg);
  default Object evalOnSelectorAll(String selector, String pageFunction) {
    return evalOnSelectorAll(selector, pageFunction, null);
  }
  /**
   * The method finds all elements matching the specified selector within the page and passes an array of matched elements as
   * <p>
   * a first argument to {@code pageFunction}. Returns the result of {@code pageFunction} invocation.
   * <p>
   * If {@code pageFunction} returns a Promise, then {@code page.$$eval} would wait for the promise to resolve and return its value.
   * <p>
   * Examples:
   * <p>
   * 
   * @param selector A selector to query for. See working with selectors for more details.
   * @param pageFunction Function to be evaluated in browser context
   * @param arg Optional argument to pass to {@code pageFunction}
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
   * The script is evaluated after the document was created but before any of its scripts were run. This is useful to amend
   * <p>
   * the JavaScript environment, e.g. to seed {@code Math.random}.
   * <p>
   * <strong>NOTE</strong> The order of evaluation of multiple scripts installed via browserContext.addInitScript(script[, arg]) and
   * <p>
   * page.addInitScript(script[, arg]) is not defined.
   * @param script Script to be evaluated in the page.
   * @param arg Optional argument to pass to {@code script} (only supported when passing a function).
   */
  void addInitScript(String script, Object arg);
  /**
   * Adds a {@code <script>} tag into the page with the desired url or content. Returns the added tag when the script's onload
   * <p>
   * fires or when the script content was injected into frame.
   * <p>
   * Shortcut for main frame's frame.addScriptTag(script).
   */
  ElementHandle addScriptTag(AddScriptTagScript script);
  /**
   * Adds a {@code <link rel="stylesheet">} tag into the page with the desired url or a {@code <style type="text/css">} tag with the
   * <p>
   * content. Returns the added tag when the stylesheet's onload fires or when the CSS content was injected into frame.
   * <p>
   * Shortcut for main frame's frame.addStyleTag(style).
   */
  ElementHandle addStyleTag(AddStyleTagStyle style);
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
   * When all steps combined have not finished during the specified {@code timeout}, this method rejects with a TimeoutError.
   * <p>
   * Passing zero timeout disables this.
   * <p>
   * Shortcut for main frame's frame.check(selector[, options]).
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
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
   * When all steps combined have not finished during the specified {@code timeout}, this method rejects with a TimeoutError.
   * <p>
   * Passing zero timeout disables this.
   * <p>
   * Shortcut for main frame's frame.click(selector[, options]).
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
   */
  void click(String selector, ClickOptions options);
  default void close() {
    close(null);
  }
  /**
   * If {@code runBeforeUnload} is {@code false} the result will resolve only after the page has been closed. If {@code runBeforeUnload} is
   * <p>
   * {@code true} the method will **not** wait for the page to close. By default, {@code page.close()} **does not** run beforeunload
   * <p>
   * handlers.
   * <p>
   * <strong>NOTE</strong> if {@code runBeforeUnload} is passed as true, a {@code beforeunload} dialog might be summoned
   * <p>
   * and should be handled manually via page.on('dialog') event.
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
   * When all steps combined have not finished during the specified {@code timeout}, this method rejects with a TimeoutError.
   * <p>
   * Passing zero timeout disables this.
   * <p>
   * <strong>NOTE</strong> {@code page.dblclick()} dispatches two {@code click} events and a single {@code dblclick} event.
   * <p>
   * Shortcut for main frame's frame.dblclick(selector[, options]).
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
   */
  void dblclick(String selector, DblclickOptions options);
  default void dispatchEvent(String selector, String type, Object eventInit) {
    dispatchEvent(selector, type, eventInit, null);
  }
  default void dispatchEvent(String selector, String type) {
    dispatchEvent(selector, type, null);
  }
  /**
   * The snippet below dispatches the {@code click} event on the element. Regardless of the visibility state of the elment, {@code click}
   * <p>
   * is dispatched. This is equivalend to calling
   * <p>
   * element.click().
   * <p>
   * Under the hood, it creates an instance of an event based on the given {@code type}, initializes it with {@code eventInit} properties
   * <p>
   * and dispatches it on the element. Events are {@code composed}, {@code cancelable} and bubble by default.
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
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
   * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
   * @param eventInit Optional event-specific initialization properties.
   */
  void dispatchEvent(String selector, String type, Object eventInit, DispatchEventOptions options);
  /**
   * 
   * <p>
   */
  void emulateMedia(EmulateMediaParams params);
  default Object evaluate(String pageFunction) {
    return evaluate(pageFunction, null);
  }
  /**
   * Returns the value of the {@code pageFunction} invacation.
   * <p>
   * If the function passed to the {@code page.evaluate} returns a Promise, then {@code page.evaluate} would wait for the promise to
   * <p>
   * resolve and return its value.
   * <p>
   * If the function passed to the {@code page.evaluate} returns a non-Serializable value, then {@code page.evaluate} resolves to
   * <p>
   * {@code undefined}. DevTools Protocol also supports transferring some additional values that are not serializable by {@code JSON}:
   * <p>
   * {@code -0}, {@code NaN}, {@code Infinity}, {@code -Infinity}, and bigint literals.
   * <p>
   * Passing argument to {@code pageFunction}:
   * <p>
   * A string can also be passed in instead of a function:
   * <p>
   * ElementHandle instances can be passed as an argument to the {@code page.evaluate}:
   * <p>
   * Shortcut for main frame's frame.evaluate(pageFunction[, arg]).
   * @param pageFunction Function to be evaluated in the page context
   * @param arg Optional argument to pass to {@code pageFunction}
   */
  Object evaluate(String pageFunction, Object arg);
  default JSHandle evaluateHandle(String pageFunction) {
    return evaluateHandle(pageFunction, null);
  }
  /**
   * Returns the value of the {@code pageFunction} invacation as in-page object (JSHandle).
   * <p>
   * The only difference between {@code page.evaluate} and {@code page.evaluateHandle} is that {@code page.evaluateHandle} returns in-page
   * <p>
   * object (JSHandle).
   * <p>
   * If the function passed to the {@code page.evaluateHandle} returns a Promise, then {@code page.evaluateHandle} would wait for the
   * <p>
   * promise to resolve and return its value.
   * <p>
   * A string can also be passed in instead of a function:
   * <p>
   * JSHandle instances can be passed as an argument to the {@code page.evaluateHandle}:
   * <p>
   * 
   * @param pageFunction Function to be evaluated in the page context
   * @param arg Optional argument to pass to {@code pageFunction}
   */
  JSHandle evaluateHandle(String pageFunction, Object arg);
  default void exposeBinding(String name, Binding playwrightBinding) {
    exposeBinding(name, playwrightBinding, null);
  }
  /**
   * The method adds a function called {@code name} on the {@code window} object of every frame in this page. When called, the function
   * <p>
   * executes {@code playwrightBinding} in Node.js and returns a Promise which resolves to the return value of
   * <p>
   * {@code playwrightBinding}. If the {@code playwrightBinding} returns a Promise, it will be awaited.
   * <p>
   * The first argument of the {@code playwrightBinding} function contains information about the caller: {@code { browserContext: BrowserContext, page: Page, frame: Frame }}.
   * <p>
   * See browserContext.exposeBinding(name, playwrightBinding[, options]) for the context-wide version.
   * <p>
   * <strong>NOTE</strong> Functions installed via {@code page.exposeBinding} survive navigations.
   * <p>
   * 
   * @param name Name of the function on the window object.
   * @param playwrightBinding Callback function that will be called in the Playwright's context.
   */
  void exposeBinding(String name, Binding playwrightBinding, ExposeBindingOptions options);
  /**
   * The method adds a function called {@code name} on the {@code window} object of every frame in the page. When called, the function
   * <p>
   * executes {@code playwrightFunction} in Node.js and returns a Promise which resolves to the return value of
   * <p>
   * {@code playwrightFunction}.
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
   * This method waits for an element matching {@code selector}, waits for actionability checks, focuses the
   * <p>
   * element, fills it and triggers an {@code input} event after filling. If the element matching {@code selector} is not an {@code <input>},
   * <p>
   * {@code <textarea>} or {@code [contenteditable]} element, this method throws an error. Note that you can pass an empty string to
   * <p>
   * clear the input field.
   * <p>
   * To send fine-grained keyboard events, use page.type(selector, text[, options]).
   * <p>
   * Shortcut for main frame's frame.fill(selector, value[, options])
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
   * @param value Value to fill for the {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element.
   */
  void fill(String selector, String value, FillOptions options);
  default void focus(String selector) {
    focus(selector, null);
  }
  /**
   * This method fetches an element with {@code selector} and focuses it. If there's no element matching {@code selector}, the method
   * <p>
   * waits until a matching element appears in the DOM.
   * <p>
   * Shortcut for main frame's frame.focus(selector[, options]).
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
   */
  void focus(String selector, FocusOptions options);
  Frame frameByName(String name);
  Frame frameByUrl(String glob);
  Frame frameByUrl(Pattern pattern);
  /**
   * Returns frame matching the specified criteria. Either {@code name} or {@code url} must be specified.
   * <p>
   * 
   * @param frameSelector Frame name or other frame lookup options.
   */
  Frame frameByUrl(Predicate<String> predicate);
  /**
   * An array of all frames attached to the page.
   */
  List<Frame> frames();
  default String getAttribute(String selector, String name) {
    return getAttribute(selector, name, null);
  }
  /**
   * Returns element attribute value.
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
   * @param name Attribute name to get the value for.
   */
  String getAttribute(String selector, String name, GetAttributeOptions options);
  default Response goBack() {
    return goBack(null);
  }
  /**
   * Returns the main resource response. In case of multiple redirects, the navigation will resolve with the response of the
   * <p>
   * last redirect. If can not go back, resolves to {@code null}.
   * <p>
   * Navigate to the previous page in history.
   */
  Response goBack(GoBackOptions options);
  default Response goForward() {
    return goForward(null);
  }
  /**
   * Returns the main resource response. In case of multiple redirects, the navigation will resolve with the response of the
   * <p>
   * last redirect. If can not go forward, resolves to {@code null}.
   * <p>
   * Navigate to the next page in history.
   */
  Response goForward(GoForwardOptions options);
  default Response navigate(String url) {
    return navigate(url, null);
  }
  /**
   * Returns the main resource response. In case of multiple redirects, the navigation will resolve with the response of the
   * <p>
   * last redirect.
   * <p>
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
   * {@code page.goto} will not throw an error when any valid HTTP status code is returned by the remote server, including 404 "Not
   * <p>
   * Found" and 500 "Internal Server Error".  The status code for such responses can be retrieved by calling
   * <p>
   * response.status().
   * <p>
   * <strong>NOTE</strong> {@code page.goto} either throws an error or returns a main resource response. The only exceptions are navigation to
   * <p>
   * {@code about:blank} or navigation to the same URL with a different hash, which would succeed and return {@code null}.
   * <p>
   * <strong>NOTE</strong> Headless mode doesn't support navigation to a PDF document. See the upstream
   * <p>
   * issue.
   * <p>
   * Shortcut for main frame's frame.goto(url[, options])
   * @param url URL to navigate page to. The url should include scheme, e.g. {@code https://}.
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
   * When all steps combined have not finished during the specified {@code timeout}, this method rejects with a TimeoutError.
   * <p>
   * Passing zero timeout disables this.
   * <p>
   * Shortcut for main frame's frame.hover(selector[, options]).
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
   */
  void hover(String selector, HoverOptions options);
  default String innerHTML(String selector) {
    return innerHTML(selector, null);
  }
  /**
   * Returns {@code element.innerHTML}.
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
   */
  String innerHTML(String selector, InnerHTMLOptions options);
  default String innerText(String selector) {
    return innerText(selector, null);
  }
  /**
   * Returns {@code element.innerText}.
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
   */
  String innerText(String selector, InnerTextOptions options);
  /**
   * Indicates that the page has been closed.
   */
  boolean isClosed();
  /**
   * The page's main frame. Page is guaranteed to have a main frame which persists during navigations.
   */
  Frame mainFrame();
  /**
   * Returns the opener for popup pages and {@code null} for others. If the opener has been closed already the promise may resolve
   * <p>
   * to {@code null}.
   */
  Page opener();
  default byte[] pdf() {
    return pdf(null);
  }
  /**
   * Returns the PDF buffer.
   * <p>
   * <strong>NOTE</strong> Generating a pdf is currently only supported in Chromium headless.
   * <p>
   * {@code page.pdf()} generates a pdf of the page with {@code print} css media. To generate a pdf with {@code screen} media, call
   * <p>
   * page.emulateMedia(params) before calling {@code page.pdf()}:
   * <p>
   * <strong>NOTE</strong> By default, {@code page.pdf()} generates a pdf with modified colors for printing. Use the
   * <p>
   * {@code -webkit-print-color-adjust} property to
   * <p>
   * force rendering of exact colors.
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
   */
  byte[] pdf(PdfOptions options);
  default void press(String selector, String key) {
    press(selector, key, null);
  }
  /**
   * Focuses the element, and then uses keyboard.down(key) and keyboard.up(key).
   * <p>
   * {@code key} can specify the intended keyboardEvent.key
   * <p>
   * value or a single character to generate the text for. A superset of the {@code key} values can be found
   * <p>
   * here. Examples of the keys are:
   * <p>
   * {@code F1} - {@code F12}, {@code Digit0}- {@code Digit9}, {@code KeyA}- {@code KeyZ}, {@code Backquote}, {@code Minus}, {@code Equal}, {@code Backslash}, {@code Backspace}, {@code Tab},
   * <p>
   * {@code Delete}, {@code Escape}, {@code ArrowDown}, {@code End}, {@code Enter}, {@code Home}, {@code Insert}, {@code PageDown}, {@code PageUp}, {@code ArrowRight}, {@code ArrowUp}, etc.
   * <p>
   * Following modification shortcuts are also suported: {@code Shift}, {@code Control}, {@code Alt}, {@code Meta}, {@code ShiftLeft}.
   * <p>
   * Holding down {@code Shift} will type the text that corresponds to the {@code key} in the upper case.
   * <p>
   * If {@code key} is a single character, it is case-sensitive, so the values {@code a} and {@code A} will generate different respective
   * <p>
   * texts.
   * <p>
   * Shortcuts such as {@code key: "Control+o"} or {@code key: "Control+Shift+T"} are supported as well. When speficied with the
   * <p>
   * modifier, modifier is pressed and being held while the subsequent key is being pressed.
   * <p>
   * 
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
   * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
   */
  void press(String selector, String key, PressOptions options);
  default Response reload() {
    return reload(null);
  }
  /**
   * Returns the main resource response. In case of multiple redirects, the navigation will resolve with the response of the
   * <p>
   * last redirect.
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
   * Page routes take precedence over browser context routes (set up with browserContext.route(url, handler)) when request matches
   * <p>
   * both handlers.
   * <p>
   * <strong>NOTE</strong> Enabling routing disables http cache.
   * @param url A glob pattern, regex pattern or predicate receiving URL to match while routing.
   * @param handler handler function to route the request.
   */
  void route(Predicate<String> url, Consumer<Route> handler);
  default byte[] screenshot() {
    return screenshot(null);
  }
  /**
   * Returns the buffer with the captured screenshot.
   * <p>
   * <strong>NOTE</strong> Screenshots take at least 1/6 second on Chromium OS X and Chromium Windows. See https://crbug.com/741689 for
   * <p>
   * discussion.
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
   * Returns the array of option values that have been successfully selected.
   * <p>
   * Triggers a {@code change} and {@code input} event once all the provided options have been selected. If there's no {@code <select>} element
   * <p>
   * matching {@code selector}, the method throws an error.
   * <p>
   * Shortcut for main frame's frame.selectOption(selector, values[, options])
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option is considered matching if all specified properties match.
   */
  List<String> selectOption(String selector, ElementHandle[] values, SelectOptionOptions options);
  default void setContent(String html) {
    setContent(html, null);
  }
  /**
   * 
   * @param html HTML markup to assign to the page.
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
   * <strong>NOTE</strong> page.setDefaultNavigationTimeout(timeout) takes priority over page.setDefaultTimeout(timeout),
   * <p>
   * browserContext.setDefaultTimeout(timeout) and browserContext.setDefaultNavigationTimeout(timeout).
   * @param timeout Maximum navigation time in milliseconds
   */
  void setDefaultNavigationTimeout(int timeout);
  /**
   * This setting will change the default maximum time for all the methods accepting {@code timeout} option.
   * <p>
   * <strong>NOTE</strong> page.setDefaultNavigationTimeout(timeout) takes priority over page.setDefaultTimeout(timeout).
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
   * This method expects {@code selector} to point to an input
   * <p>
   * element.
   * <p>
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
   * <p>
   * are resolved relative to the current working directory. For
   * <p>
   * empty array, clears the selected files.
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
   */
  void setInputFiles(String selector, FileChooser.FilePayload[] files, SetInputFilesOptions options);
  /**
   * In the case of multiple pages in a single browser, each page can have its own viewport size. However,
   * <p>
   * browser.newContext([options]) allows to set viewport size (and more) for all pages in the context at once.
   * <p>
   * {@code page.setViewportSize} will resize the page. A lot of websites don't expect phones to change size, so you should set the
   * <p>
   * viewport size before navigating to the page.
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
   * When all steps combined have not finished during the specified {@code timeout}, this method rejects with a TimeoutError.
   * <p>
   * Passing zero timeout disables this.
   * <p>
   * <strong>NOTE</strong> {@code page.tap()} requires that the {@code hasTouch} option of the browser context be set to true.
   * <p>
   * Shortcut for main frame's frame.tap(selector[, options]).
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
   */
  void tap(String selector, TapOptions options);
  default String textContent(String selector) {
    return textContent(selector, null);
  }
  /**
   * Returns {@code element.textContent}.
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
   */
  String textContent(String selector, TextContentOptions options);
  /**
   * Returns the page's title. Shortcut for main frame's frame.title().
   */
  String title();
  default void type(String selector, String text) {
    type(selector, text, null);
  }
  /**
   * Sends a {@code keydown}, {@code keypress}/{@code input}, and {@code keyup} event for each character in the text. {@code page.type} can be used to send
   * <p>
   * fine-grained keyboard events. To fill values in form fields, use page.fill(selector, value[, options]).
   * <p>
   * To press a special key, like {@code Control} or {@code ArrowDown}, use keyboard.press(key[, options]).
   * <p>
   * Shortcut for main frame's frame.type(selector, text[, options]).
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
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
   * When all steps combined have not finished during the specified {@code timeout}, this method rejects with a TimeoutError.
   * <p>
   * Passing zero timeout disables this.
   * <p>
   * Shortcut for main frame's frame.uncheck(selector[, options]).
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See working with selectors for more details.
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
   * @param handler Optional handler function to route the request.
   */
  void unroute(Predicate<String> url, Consumer<Route> handler);
  /**
   * Shortcut for main frame's frame.url().
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
   * Returns the event data value.
   * <p>
   * Waits for event to fire and passes its value into the predicate function. Resolves when the predicate returns truthy
   * <p>
   * value. Will throw an error if the page is closed before the event is fired.
   * @param event Event name, same one would pass into {@code page.on(event)}.
   */
  Deferred<Event<EventType>> waitForEvent(EventType event, WaitForEventOptions options);
  default Deferred<JSHandle> waitForFunction(String pageFunction, Object arg) {
    return waitForFunction(pageFunction, arg, null);
  }
  default Deferred<JSHandle> waitForFunction(String pageFunction) {
    return waitForFunction(pageFunction, null);
  }
  /**
   * Returns when the {@code pageFunction} returns a truthy value. It resolves to a JSHandle of the truthy value.
   * <p>
   * The {@code waitForFunction} can be used to observe viewport size change:
   * <p>
   * To pass an argument from Node.js to the predicate of {@code page.waitForFunction} function:
   * <p>
   * Shortcut for main frame's frame.waitForFunction(pageFunction[, arg, options]).
   * @param pageFunction Function to be evaluated in browser context
   * @param arg Optional argument to pass to {@code pageFunction}
   */
  Deferred<JSHandle> waitForFunction(String pageFunction, Object arg, WaitForFunctionOptions options);
  default Deferred<Void> waitForLoadState(LoadState state) {
    return waitForLoadState(state, null);
  }
  default Deferred<Void> waitForLoadState() {
    return waitForLoadState(null);
  }
  /**
   * Returns when the required load state has been reached.
   * <p>
   * This resolves when the page reaches a required load state, {@code load} by default. The navigation must have been committed
   * <p>
   * when this method is called. If current document has already reached the required state, resolves immediately.
   * <p>
   * 
   * <p>
   * Shortcut for main frame's frame.waitForLoadState([state, options]).
   * @param state Load state to wait for, defaults to {@code load}. If the state has been already reached while loading current document, the method resolves immediately. Optional.
   *  - {@code 'load'} - wait for the {@code load} event to be fired.
   *  - {@code 'domcontentloaded'} - wait for the {@code DOMContentLoaded} event to be fired.
   *  - {@code 'networkidle'} - wait until there are no network connections for at least {@code 500} ms.
   */
  Deferred<Void> waitForLoadState(LoadState state, WaitForLoadStateOptions options);
  default Deferred<Response> waitForNavigation() {
    return waitForNavigation(null);
  }
  /**
   * Returns the main resource response. In case of multiple redirects, the navigation will resolve with the response of the
   * <p>
   * last redirect. In case of navigation to a different anchor or navigation due to History API usage, the navigation will
   * <p>
   * resolve with {@code null}.
   * <p>
   * This resolves when the page navigates to a new URL or reloads. It is useful for when you run code which will indirectly
   * <p>
   * cause the page to navigate. e.g. The click target has an {@code onclick} handler that triggers navigation from a {@code setTimeout}.
   * <p>
   * Consider this example:
   * <p>
   * <strong>NOTE</strong> Usage of the History API to change the URL is
   * <p>
   * considered a navigation.
   * <p>
   * Shortcut for main frame's frame.waitForNavigation([options]).
   */
  Deferred<Response> waitForNavigation(WaitForNavigationOptions options);
  default Deferred<Request> waitForRequest(String urlGlob) { return waitForRequest(urlGlob, null); }
  default Deferred<Request> waitForRequest(Pattern urlPattern) { return waitForRequest(urlPattern, null); }
  default Deferred<Request> waitForRequest(Predicate<String> urlPredicate) { return waitForRequest(urlPredicate, null); }
  Deferred<Request> waitForRequest(String urlGlob, WaitForRequestOptions options);
  Deferred<Request> waitForRequest(Pattern urlPattern, WaitForRequestOptions options);
  Deferred<Request> waitForRequest(Predicate<String> urlPredicate, WaitForRequestOptions options);
  default Deferred<Response> waitForResponse(String urlGlob) { return waitForResponse(urlGlob, null); }
  default Deferred<Response> waitForResponse(Pattern urlPattern) { return waitForResponse(urlPattern, null); }
  default Deferred<Response> waitForResponse(Predicate<String> urlPredicate) { return waitForResponse(urlPredicate, null); }
  Deferred<Response> waitForResponse(String urlGlob, WaitForResponseOptions options);
  Deferred<Response> waitForResponse(Pattern urlPattern, WaitForResponseOptions options);
  Deferred<Response> waitForResponse(Predicate<String> urlPredicate, WaitForResponseOptions options);
  default Deferred<ElementHandle> waitForSelector(String selector) {
    return waitForSelector(selector, null);
  }
  /**
   * Returns when element specified by selector satisfies {@code state} option. Resolves to {@code null} if waiting for {@code hidden} or
   * <p>
   * {@code detached}.
   * <p>
   * Wait for the {@code selector} to satisfy {@code state} option (either appear/disappear from dom, or become visible/hidden). If at
   * <p>
   * the moment of calling the method {@code selector} already satisfies the condition, the method will return immediately. If the
   * <p>
   * selector doesn't satisfy the condition for the {@code timeout} milliseconds, the function will throw.
   * <p>
   * This method works across navigations:
   * <p>
   * 
   * @param selector A selector to query for. See working with selectors for more details.
   */
  Deferred<ElementHandle> waitForSelector(String selector, WaitForSelectorOptions options);
  /**
   * Returns a promise that resolves after the timeout.
   * <p>
   * Note that {@code page.waitForTimeout()} should only be used for debugging. Tests using the timer in production are going to be
   * <p>
   * flaky. Use signals such as network events, selectors becoming visible and others instead.
   * <p>
   * Shortcut for main frame's frame.waitForTimeout(timeout).
   * @param timeout A timeout to wait for
   */
  Deferred<Void> waitForTimeout(int timeout);
  /**
   * This method returns all of the dedicated WebWorkers
   * <p>
   * associated with the page.
   * <p>
   * <strong>NOTE</strong> This does not contain ServiceWorkers
   */
  List<Worker> workers();
  Accessibility accessibility();
  Keyboard keyboard();
  Mouse mouse();
  Touchscreen touchscreen();
}

