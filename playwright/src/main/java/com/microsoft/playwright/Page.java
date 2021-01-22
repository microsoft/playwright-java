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
 * - extends: [EventEmitter]
 *
 * <p> Page provides methods to interact with a single tab in a {@code Browser}, or an
 * [extension background page](https://developer.chrome.com/extensions/background_pages) in Chromium. One [Browser]
 * instance might have multiple [Page] instances.
 *
 * <p> The Page class emits various events (described below) which can be handled using any of Node's native
 * [{@code EventEmitter}](https://nodejs.org/api/events.html#events_class_eventemitter) methods, such as {@code on}, {@code once} or
 * {@code removeListener}.
 *
 * <p> To unsubscribe from events use the {@code removeListener} method:
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


  void onClose(Runnable handler);
  void offClose(Runnable handler);

  void onConsole(Consumer<ConsoleMessage> handler);
  void offConsole(Consumer<ConsoleMessage> handler);

  void onCrash(Runnable handler);
  void offCrash(Runnable handler);

  void onDialog(Consumer<Dialog> handler);
  void offDialog(Consumer<Dialog> handler);

  void onDomContentLoaded(Runnable handler);
  void offDomContentLoaded(Runnable handler);

  void onDownload(Consumer<Download> handler);
  void offDownload(Consumer<Download> handler);

  void onFileChooser(Consumer<FileChooser> handler);
  void offFileChooser(Consumer<FileChooser> handler);

  void onFrameAttached(Consumer<Frame> handler);
  void offFrameAttached(Consumer<Frame> handler);

  void onFrameDetached(Consumer<Frame> handler);
  void offFrameDetached(Consumer<Frame> handler);

  void onFrameNavigated(Consumer<Frame> handler);
  void offFrameNavigated(Consumer<Frame> handler);

  void onLoad(Runnable handler);
  void offLoad(Runnable handler);

  void onPageError(Consumer<Error> handler);
  void offPageError(Consumer<Error> handler);

  void onPopup(Consumer<Page> handler);
  void offPopup(Consumer<Page> handler);

  void onRequest(Consumer<Request> handler);
  void offRequest(Consumer<Request> handler);

  void onRequestFailed(Consumer<Request> handler);
  void offRequestFailed(Consumer<Request> handler);

  void onRequestFinished(Consumer<Request> handler);
  void offRequestFinished(Consumer<Request> handler);

  void onResponse(Consumer<Response> handler);
  void offResponse(Consumer<Response> handler);

  void onWebSocket(Consumer<WebSocket> handler);
  void offWebSocket(Consumer<WebSocket> handler);

  void onWorker(Consumer<Worker> handler);
  void offWorker(Consumer<Worker> handler);


  class WaitForCloseOptions {
    public Double timeout;
    public WaitForCloseOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  Page waitForClose(Runnable code, WaitForCloseOptions options);
  default Page waitForClose(Runnable code) { return waitForClose(code, null); }

  class WaitForConsoleOptions {
    public Double timeout;
    public WaitForConsoleOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  ConsoleMessage waitForConsole(Runnable code, WaitForConsoleOptions options);
  default ConsoleMessage waitForConsole(Runnable code) { return waitForConsole(code, null); }

  class WaitForDownloadOptions {
    public Double timeout;
    public WaitForDownloadOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  Download waitForDownload(Runnable code, WaitForDownloadOptions options);
  default Download waitForDownload(Runnable code) { return waitForDownload(code, null); }

  class WaitForFileChooserOptions {
    public Double timeout;
    public WaitForFileChooserOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  FileChooser waitForFileChooser(Runnable code, WaitForFileChooserOptions options);
  default FileChooser waitForFileChooser(Runnable code) { return waitForFileChooser(code, null); }

  class WaitForFrameAttachedOptions {
    public Double timeout;
    public WaitForFrameAttachedOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  Frame waitForFrameAttached(Runnable code, WaitForFrameAttachedOptions options);
  default Frame waitForFrameAttached(Runnable code) { return waitForFrameAttached(code, null); }

  class WaitForFrameDetachedOptions {
    public Double timeout;
    public WaitForFrameDetachedOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  Frame waitForFrameDetached(Runnable code, WaitForFrameDetachedOptions options);
  default Frame waitForFrameDetached(Runnable code) { return waitForFrameDetached(code, null); }

  class WaitForFrameNavigatedOptions {
    public Double timeout;
    public WaitForFrameNavigatedOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  Frame waitForFrameNavigated(Runnable code, WaitForFrameNavigatedOptions options);
  default Frame waitForFrameNavigated(Runnable code) { return waitForFrameNavigated(code, null); }

  class WaitForPageErrorOptions {
    public Double timeout;
    public WaitForPageErrorOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  Error waitForPageError(Runnable code, WaitForPageErrorOptions options);
  default Error waitForPageError(Runnable code) { return waitForPageError(code, null); }

  class WaitForPopupOptions {
    public Double timeout;
    public WaitForPopupOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  Page waitForPopup(Runnable code, WaitForPopupOptions options);
  default Page waitForPopup(Runnable code) { return waitForPopup(code, null); }

  class WaitForRequestFailedOptions {
    public Double timeout;
    public WaitForRequestFailedOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  Request waitForRequestFailed(Runnable code, WaitForRequestFailedOptions options);
  default Request waitForRequestFailed(Runnable code) { return waitForRequestFailed(code, null); }

  class WaitForRequestFinishedOptions {
    public Double timeout;
    public WaitForRequestFinishedOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  Request waitForRequestFinished(Runnable code, WaitForRequestFinishedOptions options);
  default Request waitForRequestFinished(Runnable code) { return waitForRequestFinished(code, null); }

  class WaitForWebSocketOptions {
    public Double timeout;
    public WaitForWebSocketOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  WebSocket waitForWebSocket(Runnable code, WaitForWebSocketOptions options);
  default WebSocket waitForWebSocket(Runnable code) { return waitForWebSocket(code, null); }

  class WaitForWorkerOptions {
    public Double timeout;
    public WaitForWorkerOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  Worker waitForWorker(Runnable code, WaitForWorkerOptions options);
  default Worker waitForWorker(Runnable code) { return waitForWorker(code, null); }

  enum LoadState { LOAD, DOMCONTENTLOADED, NETWORKIDLE }
  class AddScriptTagOptions {
    /**
     * Raw JavaScript content to be injected into frame.
     */
    public String content;
    /**
     * Path to the JavaScript file to be injected into frame. If {@code path} is a relative path, then it is resolved relative to the
     * current working directory.
     */
    public Path path;
    /**
     * Script type. Use 'module' in order to load a Javascript ES6 module. See
     * [script](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/script) for more details.
     */
    public String type;
    /**
     * URL of a script to be added.
     */
    public String url;

    public AddScriptTagOptions withContent(String content) {
      this.content = content;
      return this;
    }
    public AddScriptTagOptions withPath(Path path) {
      this.path = path;
      return this;
    }
    public AddScriptTagOptions withType(String type) {
      this.type = type;
      return this;
    }
    public AddScriptTagOptions withUrl(String url) {
      this.url = url;
      return this;
    }
  }
  class AddStyleTagOptions {
    /**
     * Raw CSS content to be injected into frame.
     */
    public String content;
    /**
     * Path to the CSS file to be injected into frame. If {@code path} is a relative path, then it is resolved relative to the
     * current working directory.
     */
    public Path path;
    /**
     * URL of the {@code <link>} tag.
     */
    public String url;

    public AddStyleTagOptions withContent(String content) {
      this.content = content;
      return this;
    }
    public AddStyleTagOptions withPath(Path path) {
      this.path = path;
      return this;
    }
    public AddStyleTagOptions withUrl(String url) {
      this.url = url;
      return this;
    }
  }
  class CheckOptions {
    /**
     * Whether to bypass the [actionability](./actionability.md) checks. Defaults to {@code false}.
     */
    public Boolean force;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public CheckOptions withForce(boolean force) {
      this.force = force;
      return this;
    }
    public CheckOptions withNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public CheckOptions withTimeout(double timeout) {
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
     * defaults to 1. See [UIEvent.detail].
     */
    public Integer clickCount;
    /**
     * Time to wait between {@code mousedown} and {@code mouseup} in milliseconds. Defaults to 0.
     */
    public Double delay;
    /**
     * Whether to bypass the [actionability](./actionability.md) checks. Defaults to {@code false}.
     */
    public Boolean force;
    /**
     * Modifier keys to press. Ensures that only these modifiers are pressed during the operation, and then restores current
     * modifiers back. If not specified, currently pressed modifiers are used.
     */
    public Set<Keyboard.Modifier> modifiers;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public Position position;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public ClickOptions withButton(Mouse.Button button) {
      this.button = button;
      return this;
    }
    public ClickOptions withClickCount(int clickCount) {
      this.clickCount = clickCount;
      return this;
    }
    public ClickOptions withDelay(double delay) {
      this.delay = delay;
      return this;
    }
    public ClickOptions withForce(boolean force) {
      this.force = force;
      return this;
    }
    public ClickOptions withModifiers(Keyboard.Modifier... modifiers) {
      this.modifiers = new HashSet<>(Arrays.asList(modifiers));
      return this;
    }
    public ClickOptions withNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public ClickOptions withPosition(Position position) {
      this.position = position;
      return this;
    }
    public ClickOptions withPosition(int x, int y) {
      return withPosition(new Position(x, y));
    }
    public ClickOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class CloseOptions {
    /**
     * Defaults to {@code false}. Whether to run the
     * [before unload](https://developer.mozilla.org/en-US/docs/Web/Events/beforeunload) page handlers.
     */
    public Boolean runBeforeUnload;

    public CloseOptions withRunBeforeUnload(boolean runBeforeUnload) {
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
    public Double delay;
    /**
     * Whether to bypass the [actionability](./actionability.md) checks. Defaults to {@code false}.
     */
    public Boolean force;
    /**
     * Modifier keys to press. Ensures that only these modifiers are pressed during the operation, and then restores current
     * modifiers back. If not specified, currently pressed modifiers are used.
     */
    public Set<Keyboard.Modifier> modifiers;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public Position position;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public DblclickOptions withButton(Mouse.Button button) {
      this.button = button;
      return this;
    }
    public DblclickOptions withDelay(double delay) {
      this.delay = delay;
      return this;
    }
    public DblclickOptions withForce(boolean force) {
      this.force = force;
      return this;
    }
    public DblclickOptions withModifiers(Keyboard.Modifier... modifiers) {
      this.modifiers = new HashSet<>(Arrays.asList(modifiers));
      return this;
    }
    public DblclickOptions withNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public DblclickOptions withPosition(Position position) {
      this.position = position;
      return this;
    }
    public DblclickOptions withPosition(int x, int y) {
      return withPosition(new Position(x, y));
    }
    public DblclickOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class DispatchEventOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public DispatchEventOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class EmulateMediaParams {
    public enum Media { SCREEN, PRINT }
    /**
     * Changes the CSS media type of the page. The only allowed values are {@code 'screen'}, {@code 'print'} and {@code null}. Passing {@code null}
     * disables CSS media emulation. Omitting {@code media} or passing {@code undefined} does not change the emulated value. Optional.
     */
    public Optional<Media> media;
    /**
     * Emulates {@code 'prefers-colors-scheme'} media feature, supported values are {@code 'light'}, {@code 'dark'}, {@code 'no-preference'}. Passing
     * {@code null} disables color scheme emulation. Omitting {@code colorScheme} or passing {@code undefined} does not change the emulated
     * value. Optional.
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
     * Whether to pass the argument as a handle, instead of passing by value. When passing a handle, only one argument is
     * supported. When passing by value, multiple arguments are supported.
     */
    public Boolean handle;

    public ExposeBindingOptions withHandle(boolean handle) {
      this.handle = handle;
      return this;
    }
  }
  class FillOptions {
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public FillOptions withNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public FillOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class FocusOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public FocusOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class GetAttributeOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public GetAttributeOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class GoBackOptions {
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be
     * changed by using the [{@code method: BrowserContext.setDefaultNavigationTimeout}],
     * [{@code method: BrowserContext.setDefaultTimeout}], [{@code method: Page.setDefaultNavigationTimeout}] or
     * [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * - {@code 'domcontentloaded'} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.
     * - {@code 'load'} - consider operation to be finished when the {@code load} event is fired.
     * - {@code 'networkidle'} - consider operation to be finished when there are no network connections for at least {@code 500} ms.
     */
    public Frame.LoadState waitUntil;

    public GoBackOptions withTimeout(double timeout) {
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
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be
     * changed by using the [{@code method: BrowserContext.setDefaultNavigationTimeout}],
     * [{@code method: BrowserContext.setDefaultTimeout}], [{@code method: Page.setDefaultNavigationTimeout}] or
     * [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * - {@code 'domcontentloaded'} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.
     * - {@code 'load'} - consider operation to be finished when the {@code load} event is fired.
     * - {@code 'networkidle'} - consider operation to be finished when there are no network connections for at least {@code 500} ms.
     */
    public Frame.LoadState waitUntil;

    public GoForwardOptions withTimeout(double timeout) {
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
     * Referer header value. If provided it will take preference over the referer header value set by
     * [{@code method: Page.setExtraHTTPHeaders}].
     */
    public String referer;
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be
     * changed by using the [{@code method: BrowserContext.setDefaultNavigationTimeout}],
     * [{@code method: BrowserContext.setDefaultTimeout}], [{@code method: Page.setDefaultNavigationTimeout}] or
     * [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * - {@code 'domcontentloaded'} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.
     * - {@code 'load'} - consider operation to be finished when the {@code load} event is fired.
     * - {@code 'networkidle'} - consider operation to be finished when there are no network connections for at least {@code 500} ms.
     */
    public Frame.LoadState waitUntil;

    public NavigateOptions withReferer(String referer) {
      this.referer = referer;
      return this;
    }
    public NavigateOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    public NavigateOptions withWaitUntil(Frame.LoadState waitUntil) {
      this.waitUntil = waitUntil;
      return this;
    }
  }
  class HoverOptions {
    /**
     * Whether to bypass the [actionability](./actionability.md) checks. Defaults to {@code false}.
     */
    public Boolean force;
    /**
     * Modifier keys to press. Ensures that only these modifiers are pressed during the operation, and then restores current
     * modifiers back. If not specified, currently pressed modifiers are used.
     */
    public Set<Keyboard.Modifier> modifiers;
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public Position position;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public HoverOptions withForce(boolean force) {
      this.force = force;
      return this;
    }
    public HoverOptions withModifiers(Keyboard.Modifier... modifiers) {
      this.modifiers = new HashSet<>(Arrays.asList(modifiers));
      return this;
    }
    public HoverOptions withPosition(Position position) {
      this.position = position;
      return this;
    }
    public HoverOptions withPosition(int x, int y) {
      return withPosition(new Position(x, y));
    }
    public HoverOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class InnerHTMLOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public InnerHTMLOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class InnerTextOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public InnerTextOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsCheckedOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public IsCheckedOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsDisabledOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public IsDisabledOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsEditableOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public IsEditableOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsEnabledOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public IsEnabledOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsHiddenOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public IsHiddenOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsVisibleOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public IsVisibleOptions withTimeout(double timeout) {
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
     * Display header and footer. Defaults to {@code false}.
     */
    public Boolean displayHeaderFooter;
    /**
     * HTML template for the print footer. Should use the same format as the {@code headerTemplate}.
     */
    public String footerTemplate;
    /**
     * Paper format. If set, takes priority over {@code width} or {@code height} options. Defaults to 'Letter'.
     */
    public String format;
    /**
     * HTML template for the print header. Should be valid HTML markup with following classes used to inject printing values
     * into them:
     * - {@code 'date'} formatted print date
     * - {@code 'title'} document title
     * - {@code 'url'} document location
     * - {@code 'pageNumber'} current page number
     * - {@code 'totalPages'} total pages in the document
     */
    public String headerTemplate;
    /**
     * Paper height, accepts values labeled with units.
     */
    public String height;
    /**
     * Paper orientation. Defaults to {@code false}.
     */
    public Boolean landscape;
    /**
     * Paper margins, defaults to none.
     */
    public Margin margin;
    /**
     * Paper ranges to print, e.g., '1-5, 8, 11-13'. Defaults to the empty string, which means print all pages.
     */
    public String pageRanges;
    /**
     * The file path to save the PDF to. If {@code path} is a relative path, then it is resolved relative to the current working
     * directory. If no path is provided, the PDF won't be saved to the disk.
     */
    public Path path;
    /**
     * Give any CSS {@code @page} size declared in the page priority over what is declared in {@code width} and {@code height} or {@code format}
     * options. Defaults to {@code false}, which will scale the content to fit the paper size.
     */
    public Boolean preferCSSPageSize;
    /**
     * Print background graphics. Defaults to {@code false}.
     */
    public Boolean printBackground;
    /**
     * Scale of the webpage rendering. Defaults to {@code 1}. Scale amount must be between 0.1 and 2.
     */
    public Double scale;
    /**
     * Paper width, accepts values labeled with units.
     */
    public String width;

    public PdfOptions withDisplayHeaderFooter(boolean displayHeaderFooter) {
      this.displayHeaderFooter = displayHeaderFooter;
      return this;
    }
    public PdfOptions withFooterTemplate(String footerTemplate) {
      this.footerTemplate = footerTemplate;
      return this;
    }
    public PdfOptions withFormat(String format) {
      this.format = format;
      return this;
    }
    public PdfOptions withHeaderTemplate(String headerTemplate) {
      this.headerTemplate = headerTemplate;
      return this;
    }
    public PdfOptions withHeight(String height) {
      this.height = height;
      return this;
    }
    public PdfOptions withLandscape(boolean landscape) {
      this.landscape = landscape;
      return this;
    }
    public Margin setMargin() {
      this.margin = new Margin();
      return this.margin;
    }
    public PdfOptions withPageRanges(String pageRanges) {
      this.pageRanges = pageRanges;
      return this;
    }
    public PdfOptions withPath(Path path) {
      this.path = path;
      return this;
    }
    public PdfOptions withPreferCSSPageSize(boolean preferCSSPageSize) {
      this.preferCSSPageSize = preferCSSPageSize;
      return this;
    }
    public PdfOptions withPrintBackground(boolean printBackground) {
      this.printBackground = printBackground;
      return this;
    }
    public PdfOptions withScale(double scale) {
      this.scale = scale;
      return this;
    }
    public PdfOptions withWidth(String width) {
      this.width = width;
      return this;
    }
  }
  class PressOptions {
    /**
     * Time to wait between {@code keydown} and {@code keyup} in milliseconds. Defaults to 0.
     */
    public Double delay;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public PressOptions withDelay(double delay) {
      this.delay = delay;
      return this;
    }
    public PressOptions withNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public PressOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class ReloadOptions {
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be
     * changed by using the [{@code method: BrowserContext.setDefaultNavigationTimeout}],
     * [{@code method: BrowserContext.setDefaultTimeout}], [{@code method: Page.setDefaultNavigationTimeout}] or
     * [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * - {@code 'domcontentloaded'} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.
     * - {@code 'load'} - consider operation to be finished when the {@code load} event is fired.
     * - {@code 'networkidle'} - consider operation to be finished when there are no network connections for at least {@code 500} ms.
     */
    public Frame.LoadState waitUntil;

    public ReloadOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    public ReloadOptions withWaitUntil(Frame.LoadState waitUntil) {
      this.waitUntil = waitUntil;
      return this;
    }
  }
  class ScreenshotOptions {
    public enum Type { PNG, JPEG }
    public class Clip {
      /**
       * x-coordinate of top-left corner of clip area
       */
      public double x;
      /**
       * y-coordinate of top-left corner of clip area
       */
      public double y;
      /**
       * width of clipping area
       */
      public double width;
      /**
       * height of clipping area
       */
      public double height;

      Clip() {
      }
      public ScreenshotOptions done() {
        return ScreenshotOptions.this;
      }

      public Clip withX(double x) {
        this.x = x;
        return this;
      }
      public Clip withY(double y) {
        this.y = y;
        return this;
      }
      public Clip withWidth(double width) {
        this.width = width;
        return this;
      }
      public Clip withHeight(double height) {
        this.height = height;
        return this;
      }
    }
    /**
     * An object which specifies clipping of the resulting image. Should have the following fields:
     */
    public Clip clip;
    /**
     * When true, takes a screenshot of the full scrollable page, instead of the currently visible viewport. Defaults to
     * {@code false}.
     */
    public Boolean fullPage;
    /**
     * Hides default white background and allows capturing screenshots with transparency. Not applicable to {@code jpeg} images.
     * Defaults to {@code false}.
     */
    public Boolean omitBackground;
    /**
     * The file path to save the image to. The screenshot type will be inferred from file extension. If {@code path} is a relative
     * path, then it is resolved relative to the current working directory. If no path is provided, the image won't be saved to
     * the disk.
     */
    public Path path;
    /**
     * The quality of the image, between 0-100. Not applicable to {@code png} images.
     */
    public Integer quality;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;
    /**
     * Specify screenshot type, defaults to {@code png}.
     */
    public Type type;

    public Clip setClip() {
      this.clip = new Clip();
      return this.clip;
    }
    public ScreenshotOptions withFullPage(boolean fullPage) {
      this.fullPage = fullPage;
      return this;
    }
    public ScreenshotOptions withOmitBackground(boolean omitBackground) {
      this.omitBackground = omitBackground;
      return this;
    }
    public ScreenshotOptions withPath(Path path) {
      this.path = path;
      return this;
    }
    public ScreenshotOptions withQuality(int quality) {
      this.quality = quality;
      return this;
    }
    public ScreenshotOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    public ScreenshotOptions withType(Type type) {
      this.type = type;
      return this;
    }
  }
  class SelectOptionOptions {
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public SelectOptionOptions withNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public SelectOptionOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class SetContentOptions {
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be
     * changed by using the [{@code method: BrowserContext.setDefaultNavigationTimeout}],
     * [{@code method: BrowserContext.setDefaultTimeout}], [{@code method: Page.setDefaultNavigationTimeout}] or
     * [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * - {@code 'domcontentloaded'} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.
     * - {@code 'load'} - consider operation to be finished when the {@code load} event is fired.
     * - {@code 'networkidle'} - consider operation to be finished when there are no network connections for at least {@code 500} ms.
     */
    public Frame.LoadState waitUntil;

    public SetContentOptions withTimeout(double timeout) {
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
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public SetInputFilesOptions withNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public SetInputFilesOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class TapOptions {
    public class Position {
      public double x;
      public double y;

      Position() {
      }
      public TapOptions done() {
        return TapOptions.this;
      }

      public Position withX(double x) {
        this.x = x;
        return this;
      }
      public Position withY(double y) {
        this.y = y;
        return this;
      }
    }
    /**
     * Whether to bypass the [actionability](./actionability.md) checks. Defaults to {@code false}.
     */
    public Boolean force;
    /**
     * Modifier keys to press. Ensures that only these modifiers are pressed during the operation, and then restores current
     * modifiers back. If not specified, currently pressed modifiers are used.
     */
    public Set<Keyboard.Modifier> modifiers;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public Position position;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public TapOptions withForce(boolean force) {
      this.force = force;
      return this;
    }
    public TapOptions withModifiers(Keyboard.Modifier... modifiers) {
      this.modifiers = new HashSet<>(Arrays.asList(modifiers));
      return this;
    }
    public TapOptions withNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public Position setPosition() {
      this.position = new Position();
      return this.position;
    }
    public TapOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class TextContentOptions {
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public TextContentOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class TypeOptions {
    /**
     * Time to wait between key presses in milliseconds. Defaults to 0.
     */
    public Double delay;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public TypeOptions withDelay(double delay) {
      this.delay = delay;
      return this;
    }
    public TypeOptions withNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public TypeOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class UncheckOptions {
    /**
     * Whether to bypass the [actionability](./actionability.md) checks. Defaults to {@code false}.
     */
    public Boolean force;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public UncheckOptions withForce(boolean force) {
      this.force = force;
      return this;
    }
    public UncheckOptions withNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    public UncheckOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForFunctionOptions {
    /**
     * If {@code polling} is {@code 'raf'}, then {@code pageFunction} is constantly executed in {@code requestAnimationFrame} callback. If {@code polling} is
     * a number, then it is treated as an interval in milliseconds at which the function would be executed. Defaults to {@code raf}.
     */
    public Integer pollingInterval;
    /**
     * maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the [{@code method: BrowserContext.setDefaultTimeout}].
     */
    public Double timeout;

    public WaitForFunctionOptions withRequestAnimationFrame() {
      this.pollingInterval = null;
      return this;
    }
    public WaitForFunctionOptions withPollingInterval(int millis) {
      this.pollingInterval = millis;
      return this;
    }
    public WaitForFunctionOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForLoadStateOptions {
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be
     * changed by using the [{@code method: BrowserContext.setDefaultNavigationTimeout}],
     * [{@code method: BrowserContext.setDefaultTimeout}], [{@code method: Page.setDefaultNavigationTimeout}] or
     * [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public WaitForLoadStateOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForNavigationOptions {
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be
     * changed by using the [{@code method: BrowserContext.setDefaultNavigationTimeout}],
     * [{@code method: BrowserContext.setDefaultTimeout}], [{@code method: Page.setDefaultNavigationTimeout}] or
     * [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;
    /**
     * A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation.
     */
    public String glob;
    public Pattern pattern;
    public Predicate<String> predicate;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * - {@code 'domcontentloaded'} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.
     * - {@code 'load'} - consider operation to be finished when the {@code load} event is fired.
     * - {@code 'networkidle'} - consider operation to be finished when there are no network connections for at least {@code 500} ms.
     */
    public Frame.LoadState waitUntil;

    public WaitForNavigationOptions withTimeout(double timeout) {
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
     * Maximum wait time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable the timeout. The default value can be
     * changed by using the [{@code method: Page.setDefaultTimeout}] method.
     */
    public Double timeout;

    public WaitForRequestOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForResponseOptions {
    /**
     * Maximum wait time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable the timeout. The default value can be
     * changed by using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public WaitForResponseOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForSelectorOptions {
    public enum State { ATTACHED, DETACHED, VISIBLE, HIDDEN }
    /**
     * Defaults to {@code 'visible'}. Can be either:
     * - {@code 'attached'} - wait for element to be present in DOM.
     * - {@code 'detached'} - wait for element to not be present in DOM.
     * - {@code 'visible'} - wait for element to have non-empty bounding box and no {@code visibility:hidden}. Note that element without
     *   any content or with {@code display:none} has an empty bounding box and is not considered visible.
     * - {@code 'hidden'} - wait for element to be either detached from DOM, or have an empty bounding box or {@code visibility:hidden}.
     *   This is opposite to the {@code 'visible'} option.
     */
    public State state;
    /**
     * Maximum time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can be changed by
     * using the [{@code method: BrowserContext.setDefaultTimeout}] or [{@code method: Page.setDefaultTimeout}] methods.
     */
    public Double timeout;

    public WaitForSelectorOptions withState(State state) {
      this.state = state;
      return this;
    }
    public WaitForSelectorOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  /**
   * The method finds an element matching the specified selector within the page. If no elements match the selector, the
   * return value resolves to {@code null}.
   *
   * <p> Shortcut for main frame's [{@code method: Frame.$}].
   *
   * @param selector A selector to query for. See [working with selectors](./selectors.md#working-with-selectors) for more details.
   */
  ElementHandle querySelector(String selector);
  /**
   * The method finds all elements matching the specified selector within the page. If no elements match the selector, the
   * return value resolves to {@code []}.
   *
   * <p> Shortcut for main frame's [{@code method: Frame.$$}].
   *
   * @param selector A selector to query for. See [working with selectors](./selectors.md#working-with-selectors) for more details.
   */
  List<ElementHandle> querySelectorAll(String selector);
  default Object evalOnSelector(String selector, String pageFunction) {
    return evalOnSelector(selector, pageFunction, null);
  }
  /**
   * The method finds an element matching the specified selector within the page and passes it as a first argument to
   * {@code pageFunction}. If no elements match the selector, the method throws an error. Returns the value of {@code pageFunction}.
   *
   * <p> If {@code pageFunction} returns a [Promise], then [{@code method: Page.$eval}] would wait for the promise to resolve and return its
   * value.
   *
   * <p> Shortcut for main frame's [{@code method: Frame.$eval}].
   *
   * @param selector A selector to query for. See [working with selectors](./selectors.md#working-with-selectors) for more details.
   * @param pageFunction Function to be evaluated in browser context
   * @param arg Optional argument to pass to {@code pageFunction}
   */
  Object evalOnSelector(String selector, String pageFunction, Object arg);
  default Object evalOnSelectorAll(String selector, String pageFunction) {
    return evalOnSelectorAll(selector, pageFunction, null);
  }
  /**
   * The method finds all elements matching the specified selector within the page and passes an array of matched elements as
   * a first argument to {@code pageFunction}. Returns the result of {@code pageFunction} invocation.
   *
   * <p> If {@code pageFunction} returns a [Promise], then [{@code method: Page.$$eval}] would wait for the promise to resolve and return its
   * value.
   *
   *
   * @param selector A selector to query for. See [working with selectors](./selectors.md#working-with-selectors) for more details.
   * @param pageFunction Function to be evaluated in browser context
   * @param arg Optional argument to pass to {@code pageFunction}
   */
  Object evalOnSelectorAll(String selector, String pageFunction, Object arg);
  Accessibility accessibility();
  default void addInitScript(String script) {
    addInitScript(script, null);
  }
  /**
   * Adds a script which would be evaluated in one of the following scenarios:
   * - Whenever the page is navigated.
   * - Whenever the child frame is attached or navigated. In this case, the script is evaluated in the context of the newly
   *   attached frame.
   *
   * <p> The script is evaluated after the document was created but before any of its scripts were run. This is useful to amend
   * the JavaScript environment, e.g. to seed {@code Math.random}.
   *
   * <p> <strong>NOTE:</strong> The order of evaluation of multiple scripts installed via [{@code method: BrowserContext.addInitScript}] and
   * [{@code method: Page.addInitScript}] is not defined.
   *
   * @param script Script to be evaluated in the page.
   * @param arg Optional argument to pass to {@code script} (only supported when passing a function).
   */
  void addInitScript(String script, Object arg);
  default ElementHandle addScriptTag() {
    return addScriptTag(null);
  }
  /**
   * Adds a {@code <script>} tag into the page with the desired url or content. Returns the added tag when the script's onload
   * fires or when the script content was injected into frame.
   *
   * <p> Shortcut for main frame's [{@code method: Frame.addScriptTag}].
   */
  ElementHandle addScriptTag(AddScriptTagOptions options);
  default ElementHandle addStyleTag() {
    return addStyleTag(null);
  }
  /**
   * Adds a {@code <link rel="stylesheet">} tag into the page with the desired url or a {@code <style type="text/css">} tag with the
   * content. Returns the added tag when the stylesheet's onload fires or when the CSS content was injected into frame.
   *
   * <p> Shortcut for main frame's [{@code method: Frame.addStyleTag}].
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
   * 1. Find an element match matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.
   * 1. Ensure that matched element is a checkbox or a radio input. If not, this method rejects. If the element is already
   *    checked, this method returns immediately.
   * 1. Wait for [actionability](./actionability.md) checks on the matched element, unless {@code force} option is set. If the
   *    element is detached during the checks, the whole action is retried.
   * 1. Scroll the element into view if needed.
   * 1. Use [{@code property: Page.mouse}] to click in the center of the element.
   * 1. Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   * 1. Ensure that the element is now checked. If not, this method rejects.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method rejects with a {@code TimeoutError}.
   * Passing zero timeout disables this.
   *
   * <p> Shortcut for main frame's [{@code method: Frame.check}].
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md#working-with-selectors) for more details.
   */
  void check(String selector, CheckOptions options);
  default void click(String selector) {
    click(selector, null);
  }
  /**
   * This method clicks an element matching {@code selector} by performing the following steps:
   * 1. Find an element match matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.
   * 1. Wait for [actionability](./actionability.md) checks on the matched element, unless {@code force} option is set. If the
   *    element is detached during the checks, the whole action is retried.
   * 1. Scroll the element into view if needed.
   * 1. Use [{@code property: Page.mouse}] to click in the center of the element, or the specified {@code position}.
   * 1. Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method rejects with a {@code TimeoutError}.
   * Passing zero timeout disables this.
   *
   * <p> Shortcut for main frame's [{@code method: Frame.click}].
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md#working-with-selectors) for more details.
   */
  void click(String selector, ClickOptions options);
  default void close() {
    close(null);
  }
  /**
   * If {@code runBeforeUnload} is {@code false}, does not run any unload handlers and waits for the page to be closed. If
   * {@code runBeforeUnload} is {@code true} the method will run unload handlers, but will **not** wait for the page to close.
   *
   * <p> By default, {@code page.close()} **does not** run {@code beforeunload} handlers.
   *
   * <p> <strong>NOTE:</strong> if {@code runBeforeUnload} is passed as true, a {@code beforeunload} dialog might be summoned and should be handled manually
   * via [{@code event: Page.dialog}] event.
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
   * 1. Find an element match matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.
   * 1. Wait for [actionability](./actionability.md) checks on the matched element, unless {@code force} option is set. If the
   *    element is detached during the checks, the whole action is retried.
   * 1. Scroll the element into view if needed.
   * 1. Use [{@code property: Page.mouse}] to double click in the center of the element, or the specified {@code position}.
   * 1. Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set. Note that if the
   *    first click of the {@code dblclick()} triggers a navigation event, this method will reject.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method rejects with a {@code TimeoutError}.
   * Passing zero timeout disables this.
   *
   * <p> <strong>NOTE:</strong> {@code page.dblclick()} dispatches two {@code click} events and a single {@code dblclick} event.
   *
   * <p> Shortcut for main frame's [{@code method: Frame.dblclick}].
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md#working-with-selectors) for more details.
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
   * is dispatched. This is equivalend to calling
   * [element.click()](https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/click).
   *
   * <p> Under the hood, it creates an instance of an event based on the given {@code type}, initializes it with {@code eventInit} properties
   * and dispatches it on the element. Events are {@code composed}, {@code cancelable} and bubble by default.
   *
   * <p> Since {@code eventInit} is event-specific, please refer to the events documentation for the lists of initial properties:
   * - [DragEvent](https://developer.mozilla.org/en-US/docs/Web/API/DragEvent/DragEvent)
   * - [FocusEvent](https://developer.mozilla.org/en-US/docs/Web/API/FocusEvent/FocusEvent)
   * - [KeyboardEvent](https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/KeyboardEvent)
   * - [MouseEvent](https://developer.mozilla.org/en-US/docs/Web/API/MouseEvent/MouseEvent)
   * - [PointerEvent](https://developer.mozilla.org/en-US/docs/Web/API/PointerEvent/PointerEvent)
   * - [TouchEvent](https://developer.mozilla.org/en-US/docs/Web/API/TouchEvent/TouchEvent)
   * - [Event](https://developer.mozilla.org/en-US/docs/Web/API/Event/Event)
   *
   * <p> You can also specify {@code JSHandle} as the property value if you want live objects to be passed into the event:
   *
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md#working-with-selectors) for more details.
   * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
   * @param eventInit Optional event-specific initialization properties.
   */
  void dispatchEvent(String selector, String type, Object eventInit, DispatchEventOptions options);
  void emulateMedia(EmulateMediaParams params);
  default Object evaluate(String pageFunction) {
    return evaluate(pageFunction, null);
  }
  /**
   * Returns the value of the {@code pageFunction} invocation.
   *
   * <p> If the function passed to the [{@code method: Page.evaluate}] returns a [Promise], then [{@code method: Page.evaluate}] would wait
   * for the promise to resolve and return its value.
   *
   * <p> If the function passed to the [{@code method: Page.evaluate}] returns a non-[Serializable] value,
   * then[ method: {@code Page.evaluate}] resolves to {@code undefined}. DevTools Protocol also supports transferring some additional
   * values that are not serializable by {@code JSON}: {@code -0}, {@code NaN}, {@code Infinity}, {@code -Infinity}, and bigint literals.
   *
   * <p> Passing argument to {@code pageFunction}:
   *
   * <p> A string can also be passed in instead of a function:
   *
   * <p> {@code ElementHandle} instances can be passed as an argument to the [{@code method: Page.evaluate}]:
   *
   * <p> Shortcut for main frame's [{@code method: Frame.evaluate}].
   *
   * @param pageFunction Function to be evaluated in the page context
   * @param arg Optional argument to pass to {@code pageFunction}
   */
  Object evaluate(String pageFunction, Object arg);
  default JSHandle evaluateHandle(String pageFunction) {
    return evaluateHandle(pageFunction, null);
  }
  /**
   * Returns the value of the {@code pageFunction} invocation as in-page object (JSHandle).
   *
   * <p> The only difference between [{@code method: Page.evaluate}] and [{@code method: Page.evaluateHandle}] is that
   * [{@code method: Page.evaluateHandle}] returns in-page object (JSHandle).
   *
   * <p> If the function passed to the [{@code method: Page.evaluateHandle}] returns a [Promise], then [{@code method: Page.evaluateHandle}]
   * would wait for the promise to resolve and return its value.
   *
   * <p> A string can also be passed in instead of a function:
   *
   * <p> {@code JSHandle} instances can be passed as an argument to the [{@code method: Page.evaluateHandle}]:
   *
   *
   * @param pageFunction Function to be evaluated in the page context
   * @param arg Optional argument to pass to {@code pageFunction}
   */
  JSHandle evaluateHandle(String pageFunction, Object arg);
  default void exposeBinding(String name, Binding callback) {
    exposeBinding(name, callback, null);
  }
  /**
   * The method adds a function called {@code name} on the {@code window} object of every frame in this page. When called, the function
   * executes {@code callback} and returns a [Promise] which resolves to the return value of {@code callback}. If the {@code callback} returns
   * a [Promise], it will be awaited.
   *
   * <p> The first argument of the {@code callback} function contains information about the caller: `{ browserContext: BrowserContext,
   * page: Page, frame: Frame }`.
   *
   * <p> See [{@code method: BrowserContext.exposeBinding}] for the context-wide version.
   *
   * <p> <strong>NOTE:</strong> Functions installed via [{@code method: Page.exposeBinding}] survive navigations.
   *
   *
   * @param name Name of the function on the window object.
   * @param callback Callback function that will be called in the Playwright's context.
   */
  void exposeBinding(String name, Binding callback, ExposeBindingOptions options);
  /**
   * The method adds a function called {@code name} on the {@code window} object of every frame in the page. When called, the function
   * executes {@code callback} and returns a [Promise] which resolves to the return value of {@code callback}.
   *
   * <p> If the {@code callback} returns a [Promise], it will be awaited.
   *
   * <p> See [{@code method: BrowserContext.exposeFunction}] for context-wide exposed function.
   *
   * <p> <strong>NOTE:</strong> Functions installed via [{@code method: Page.exposeFunction}] survive navigations.
   *
   *
   * @param name Name of the function on the window object
   * @param callback Callback function which will be called in Playwright's context.
   */
  void exposeFunction(String name, Function callback);
  default void fill(String selector, String value) {
    fill(selector, value, null);
  }
  /**
   * This method waits for an element matching {@code selector}, waits for [actionability](./actionability.md) checks, focuses the
   * element, fills it and triggers an {@code input} event after filling. If the element matching {@code selector} is not an {@code <input>},
   * {@code <textarea>} or {@code [contenteditable]} element, this method throws an error. Note that you can pass an empty string to
   * clear the input field.
   *
   * <p> To send fine-grained keyboard events, use [{@code method: Page.type}].
   *
   * <p> Shortcut for main frame's [{@code method: Frame.fill}]
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md#working-with-selectors) for more details.
   * @param value Value to fill for the {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element.
   */
  void fill(String selector, String value, FillOptions options);
  default void focus(String selector) {
    focus(selector, null);
  }
  /**
   * This method fetches an element with {@code selector} and focuses it. If there's no element matching {@code selector}, the method
   * waits until a matching element appears in the DOM.
   *
   * <p> Shortcut for main frame's [{@code method: Frame.focus}].
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md#working-with-selectors) for more details.
   */
  void focus(String selector, FocusOptions options);
  Frame frameByName(String name);
  Frame frameByUrl(String glob);
  Frame frameByUrl(Pattern pattern);
  /**
   * Returns frame matching the specified criteria. Either {@code name} or {@code url} must be specified.
   *
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
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md#working-with-selectors) for more details.
   * @param name Attribute name to get the value for.
   */
  String getAttribute(String selector, String name, GetAttributeOptions options);
  default Response goBack() {
    return goBack(null);
  }
  /**
   * Returns the main resource response. In case of multiple redirects, the navigation will resolve with the response of the
   * last redirect. If can not go back, returns {@code null}.
   *
   * <p> Navigate to the previous page in history.
   */
  Response goBack(GoBackOptions options);
  default Response goForward() {
    return goForward(null);
  }
  /**
   * Returns the main resource response. In case of multiple redirects, the navigation will resolve with the response of the
   * last redirect. If can not go forward, returns {@code null}.
   *
   * <p> Navigate to the next page in history.
   */
  Response goForward(GoForwardOptions options);
  default Response navigate(String url) {
    return navigate(url, null);
  }
  /**
   * Returns the main resource response. In case of multiple redirects, the navigation will resolve with the response of the
   * last redirect.
   *
   * <p> {@code page.goto} will throw an error if:
   * - there's an SSL error (e.g. in case of self-signed certificates).
   * - target URL is invalid.
   * - the {@code timeout} is exceeded during navigation.
   * - the remote server does not respond or is unreachable.
   * - the main resource failed to load.
   *
   * <p> {@code page.goto} will not throw an error when any valid HTTP status code is returned by the remote server, including 404 "Not
   * Found" and 500 "Internal Server Error".  The status code for such responses can be retrieved by calling
   * [{@code method: Response.status}].
   *
   * <p> <strong>NOTE:</strong> {@code page.goto} either throws an error or returns a main resource response. The only exceptions are navigation to
   * {@code about:blank} or navigation to the same URL with a different hash, which would succeed and return {@code null}.
   * <strong>NOTE:</strong> Headless mode doesn't support navigation to a PDF document. See the
   * [upstream issue](https://bugs.chromium.org/p/chromium/issues/detail?id=761295).
   *
   * <p> Shortcut for main frame's [{@code method: Frame.goto}]
   *
   * @param url URL to navigate page to. The url should include scheme, e.g. {@code https://}.
   */
  Response navigate(String url, NavigateOptions options);
  default void hover(String selector) {
    hover(selector, null);
  }
  /**
   * This method hovers over an element matching {@code selector} by performing the following steps:
   * 1. Find an element match matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.
   * 1. Wait for [actionability](./actionability.md) checks on the matched element, unless {@code force} option is set. If the
   *    element is detached during the checks, the whole action is retried.
   * 1. Scroll the element into view if needed.
   * 1. Use [{@code property: Page.mouse}] to hover over the center of the element, or the specified {@code position}.
   * 1. Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method rejects with a {@code TimeoutError}.
   * Passing zero timeout disables this.
   *
   * <p> Shortcut for main frame's [{@code method: Frame.hover}].
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md#working-with-selectors) for more details.
   */
  void hover(String selector, HoverOptions options);
  default String innerHTML(String selector) {
    return innerHTML(selector, null);
  }
  /**
   * Returns {@code element.innerHTML}.
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md#working-with-selectors) for more details.
   */
  String innerHTML(String selector, InnerHTMLOptions options);
  default String innerText(String selector) {
    return innerText(selector, null);
  }
  /**
   * Returns {@code element.innerText}.
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md#working-with-selectors) for more details.
   */
  String innerText(String selector, InnerTextOptions options);
  default boolean isChecked(String selector) {
    return isChecked(selector, null);
  }
  /**
   * Returns whether the element is checked. Throws if the element is not a checkbox or radio input.
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md#working-with-selectors) for more details.
   */
  boolean isChecked(String selector, IsCheckedOptions options);
  /**
   * Indicates that the page has been closed.
   */
  boolean isClosed();
  default boolean isDisabled(String selector) {
    return isDisabled(selector, null);
  }
  /**
   * Returns whether the element is disabled, the opposite of [enabled](./actionability.md#enabled).
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md#working-with-selectors) for more details.
   */
  boolean isDisabled(String selector, IsDisabledOptions options);
  default boolean isEditable(String selector) {
    return isEditable(selector, null);
  }
  /**
   * Returns whether the element is [editable](./actionability.md#editable).
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md#working-with-selectors) for more details.
   */
  boolean isEditable(String selector, IsEditableOptions options);
  default boolean isEnabled(String selector) {
    return isEnabled(selector, null);
  }
  /**
   * Returns whether the element is [enabled](./actionability.md#enabled).
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md#working-with-selectors) for more details.
   */
  boolean isEnabled(String selector, IsEnabledOptions options);
  default boolean isHidden(String selector) {
    return isHidden(selector, null);
  }
  /**
   * Returns whether the element is hidden, the opposite of [visible](./actionability.md#visible).
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md#working-with-selectors) for more details.
   */
  boolean isHidden(String selector, IsHiddenOptions options);
  default boolean isVisible(String selector) {
    return isVisible(selector, null);
  }
  /**
   * Returns whether the element is [visible](./actionability.md#visible).
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md#working-with-selectors) for more details.
   */
  boolean isVisible(String selector, IsVisibleOptions options);
  Keyboard keyboard();
  /**
   * The page's main frame. Page is guaranteed to have a main frame which persists during navigations.
   */
  Frame mainFrame();
  Mouse mouse();
  /**
   * Returns the opener for popup pages and {@code null} for others. If the opener has been closed already the returns {@code null}.
   */
  Page opener();
  default byte[] pdf() {
    return pdf(null);
  }
  /**
   * Returns the PDF buffer.
   *
   * <p> <strong>NOTE:</strong> Generating a pdf is currently only supported in Chromium headless.
   *
   * <p> {@code page.pdf()} generates a pdf of the page with {@code print} css media. To generate a pdf with {@code screen} media, call
   * [{@code method: Page.emulateMedia}] before calling {@code page.pdf()}:
   *
   * <p> <strong>NOTE:</strong> By default, {@code page.pdf()} generates a pdf with modified colors for printing. Use the
   * [{@code -webkit-print-color-adjust}](https://developer.mozilla.org/en-US/docs/Web/CSS/-webkit-print-color-adjust) property to
   * force rendering of exact colors.
   *
   * <p> The {@code width}, {@code height}, and {@code margin} options accept values labeled with units. Unlabeled values are treated as pixels.
   *
   * <p> A few examples:
   * - {@code page.pdf({width: 100})} - prints with width set to 100 pixels
   * - {@code page.pdf({width: '100px'})} - prints with width set to 100 pixels
   * - {@code page.pdf({width: '10cm'})} - prints with width set to 10 centimeters.
   *
   * <p> All possible units are:
   * - {@code px} - pixel
   * - {@code in} - inch
   * - {@code cm} - centimeter
   * - {@code mm} - millimeter
   *
   * <p> The {@code format} options are:
   * - {@code Letter}: 8.5in x 11in
   * - {@code Legal}: 8.5in x 14in
   * - {@code Tabloid}: 11in x 17in
   * - {@code Ledger}: 17in x 11in
   * - {@code A0}: 33.1in x 46.8in
   * - {@code A1}: 23.4in x 33.1in
   * - {@code A2}: 16.54in x 23.4in
   * - {@code A3}: 11.7in x 16.54in
   * - {@code A4}: 8.27in x 11.7in
   * - {@code A5}: 5.83in x 8.27in
   * - {@code A6}: 4.13in x 5.83in
   *
   * <p> <strong>NOTE:</strong> {@code headerTemplate} and {@code footerTemplate} markup have the following limitations: > 1. Script tags inside templates
   * are not evaluated. > 2. Page styles are not visible inside templates.
   */
  byte[] pdf(PdfOptions options);
  default void press(String selector, String key) {
    press(selector, key, null);
  }
  /**
   * Focuses the element, and then uses [{@code method: Keyboard.down}] and [{@code method: Keyboard.up}].
   *
   * <p> {@code key} can specify the intended [keyboardEvent.key](https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key)
   * value or a single character to generate the text for. A superset of the {@code key} values can be found
   * [here](https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key/Key_Values). Examples of the keys are:
   *
   * <p> {@code F1} - {@code F12}, {@code Digit0}- {@code Digit9}, {@code KeyA}- {@code KeyZ}, {@code Backquote}, {@code Minus}, {@code Equal}, {@code Backslash}, {@code Backspace}, {@code Tab},
   * {@code Delete}, {@code Escape}, {@code ArrowDown}, {@code End}, {@code Enter}, {@code Home}, {@code Insert}, {@code PageDown}, {@code PageUp}, {@code ArrowRight}, {@code ArrowUp}, etc.
   *
   * <p> Following modification shortcuts are also supported: {@code Shift}, {@code Control}, {@code Alt}, {@code Meta}, {@code ShiftLeft}.
   *
   * <p> Holding down {@code Shift} will type the text that corresponds to the {@code key} in the upper case.
   *
   * <p> If {@code key} is a single character, it is case-sensitive, so the values {@code a} and {@code A} will generate different respective
   * texts.
   *
   * <p> Shortcuts such as {@code key: "Control+o"} or {@code key: "Control+Shift+T"} are supported as well. When speficied with the
   * modifier, modifier is pressed and being held while the subsequent key is being pressed.
   *
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md#working-with-selectors) for more details.
   * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
   */
  void press(String selector, String key, PressOptions options);
  default Response reload() {
    return reload(null);
  }
  /**
   * Returns the main resource response. In case of multiple redirects, the navigation will resolve with the response of the
   * last redirect.
   */
  Response reload(ReloadOptions options);
  void route(String url, Consumer<Route> handler);
  void route(Pattern url, Consumer<Route> handler);
  /**
   * Routing provides the capability to modify network requests that are made by a page.
   *
   * <p> Once routing is enabled, every request matching the url pattern will stall unless it's continued, fulfilled or aborted.
   *
   * <p> <strong>NOTE:</strong> The handler will only be called for the first url if the response is a redirect.
   *
   * <p> or the same snippet using a regex pattern instead:
   *
   * <p> Page routes take precedence over browser context routes (set up with [{@code method: BrowserContext.route}]) when request
   * matches both handlers.
   *
   * <p> <strong>NOTE:</strong> Enabling routing disables http cache.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while routing.
   * @param handler handler function to route the request.
   */
  void route(Predicate<String> url, Consumer<Route> handler);
  default byte[] screenshot() {
    return screenshot(null);
  }
  /**
   * Returns the buffer with the captured screenshot.
   *
   * <p> <strong>NOTE:</strong> Screenshots take at least 1/6 second on Chromium OS X and Chromium Windows. See https://crbug.com/741689 for
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
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected. If there's no {@code <select>} element
   * matching {@code selector}, the method throws an error.
   *
   * <p> Will wait until all specified options are present in the {@code <select>} element.
   *
   * <p> Shortcut for main frame's [{@code method: Frame.selectOption}]
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md#working-with-selectors) for more details.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected, otherwise only the
   * first option matching one of the passed options is selected. String values are equivalent to {@code {value:'string'}}. Option
   * is considered matching if all specified properties match.
   */
  List<String> selectOption(String selector, ElementHandle[] values, SelectOptionOptions options);
  default void setContent(String html) {
    setContent(html, null);
  }
  /**
   *
   *
   * @param html HTML markup to assign to the page.
   */
  void setContent(String html, SetContentOptions options);
  /**
   * This setting will change the default maximum navigation time for the following methods and related shortcuts:
   * - [{@code method: Page.goBack}]
   * - [{@code method: Page.goForward}]
   * - [{@code method: Page.goto}]
   * - [{@code method: Page.reload}]
   * - [{@code method: Page.setContent}]
   * - [{@code method: Page.waitForNavigation}]
   *
   * <p> <strong>NOTE:</strong> [{@code method: Page.setDefaultNavigationTimeout}] takes priority over [{@code method: Page.setDefaultTimeout}],
   * [{@code method: BrowserContext.setDefaultTimeout}] and [{@code method: BrowserContext.setDefaultNavigationTimeout}].
   *
   * @param timeout Maximum navigation time in milliseconds
   */
  void setDefaultNavigationTimeout(double timeout);
  /**
   * This setting will change the default maximum time for all the methods accepting {@code timeout} option.
   *
   * <p> <strong>NOTE:</strong> [{@code method: Page.setDefaultNavigationTimeout}] takes priority over [{@code method: Page.setDefaultTimeout}].
   *
   * @param timeout Maximum time in milliseconds
   */
  void setDefaultTimeout(double timeout);
  /**
   * The extra HTTP headers will be sent with every request the page initiates.
   *
   * <p> <strong>NOTE:</strong> [{@code method: Page.setExtraHTTPHeaders}] does not guarantee the order of headers in the outgoing requests.
   *
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
   * This method expects {@code selector} to point to an
   * [input element](https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input).
   *
   * <p> Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then they
   * are resolved relative to the the current working directory. For empty array, clears the selected files.
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md#working-with-selectors) for more details.
   */
  void setInputFiles(String selector, FileChooser.FilePayload[] files, SetInputFilesOptions options);
  /**
   * In the case of multiple pages in a single browser, each page can have its own viewport size. However,
   * [{@code method: Browser.newContext}] allows to set viewport size (and more) for all pages in the context at once.
   *
   * <p> {@code page.setViewportSize} will resize the page. A lot of websites don't expect phones to change size, so you should set the
   * viewport size before navigating to the page.
   */
  void setViewportSize(int width, int height);
  default void tap(String selector) {
    tap(selector, null);
  }
  /**
   * This method taps an element matching {@code selector} by performing the following steps:
   * 1. Find an element match matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.
   * 1. Wait for [actionability](./actionability.md) checks on the matched element, unless {@code force} option is set. If the
   *    element is detached during the checks, the whole action is retried.
   * 1. Scroll the element into view if needed.
   * 1. Use [{@code property: Page.touchscreen}] to tap the center of the element, or the specified {@code position}.
   * 1. Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method rejects with a {@code TimeoutError}.
   * Passing zero timeout disables this.
   *
   * <p> <strong>NOTE:</strong> [{@code method: Page.tap}] requires that the {@code hasTouch} option of the browser context be set to true.
   *
   * <p> Shortcut for main frame's [{@code method: Frame.tap}].
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md#working-with-selectors) for more details.
   */
  void tap(String selector, TapOptions options);
  default String textContent(String selector) {
    return textContent(selector, null);
  }
  /**
   * Returns {@code element.textContent}.
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md#working-with-selectors) for more details.
   */
  String textContent(String selector, TextContentOptions options);
  /**
   * Returns the page's title. Shortcut for main frame's [{@code method: Frame.title}].
   */
  String title();
  Touchscreen touchscreen();
  default void type(String selector, String text) {
    type(selector, text, null);
  }
  /**
   * Sends a {@code keydown}, {@code keypress}/{@code input}, and {@code keyup} event for each character in the text. {@code page.type} can be used to send
   * fine-grained keyboard events. To fill values in form fields, use [{@code method: Page.fill}].
   *
   * <p> To press a special key, like {@code Control} or {@code ArrowDown}, use [{@code method: Keyboard.press}].
   *
   * <p> Shortcut for main frame's [{@code method: Frame.type}].
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md#working-with-selectors) for more details.
   * @param text A text to type into a focused element.
   */
  void type(String selector, String text, TypeOptions options);
  default void uncheck(String selector) {
    uncheck(selector, null);
  }
  /**
   * This method unchecks an element matching {@code selector} by performing the following steps:
   * 1. Find an element match matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.
   * 1. Ensure that matched element is a checkbox or a radio input. If not, this method rejects. If the element is already
   *    unchecked, this method returns immediately.
   * 1. Wait for [actionability](./actionability.md) checks on the matched element, unless {@code force} option is set. If the
   *    element is detached during the checks, the whole action is retried.
   * 1. Scroll the element into view if needed.
   * 1. Use [{@code property: Page.mouse}] to click in the center of the element.
   * 1. Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.
   * 1. Ensure that the element is now unchecked. If not, this method rejects.
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method rejects with a {@code TimeoutError}.
   * Passing zero timeout disables this.
   *
   * <p> Shortcut for main frame's [{@code method: Frame.uncheck}].
   *
   * @param selector A selector to search for element. If there are multiple elements satisfying the selector, the first will be used. See
   * [working with selectors](./selectors.md#working-with-selectors) for more details.
   */
  void uncheck(String selector, UncheckOptions options);
  default void unroute(String url) { unroute(url, null); }
  default void unroute(Pattern url) { unroute(url, null); }
  default void unroute(Predicate<String> url) { unroute(url, null); }
  void unroute(String url, Consumer<Route> handler);
  void unroute(Pattern url, Consumer<Route> handler);
  /**
   * Removes a route created with [{@code method: Page.route}]. When {@code handler} is not specified, removes all routes for the {@code url}.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while routing.
   * @param handler Optional handler function to route the request.
   */
  void unroute(Predicate<String> url, Consumer<Route> handler);
  /**
   * Shortcut for main frame's [{@code method: Frame.url}].
   */
  String url();
  /**
   * Video object associated with this page.
   */
  Video video();
  Viewport viewportSize();
  default JSHandle waitForFunction(String pageFunction, Object arg) {
    return waitForFunction(pageFunction, arg, null);
  }
  default JSHandle waitForFunction(String pageFunction) {
    return waitForFunction(pageFunction, null);
  }
  /**
   * Returns when the {@code pageFunction} returns a truthy value. It resolves to a JSHandle of the truthy value.
   *
   * <p> The {@code waitForFunction} can be used to observe viewport size change:
   *
   * <p> To pass an argument to the predicate of [{@code method: Page.waitForFunction}] function:
   *
   * <p> Shortcut for main frame's [{@code method: Frame.waitForFunction}].
   *
   * @param pageFunction Function to be evaluated in browser context
   * @param arg Optional argument to pass to {@code pageFunction}
   */
  JSHandle waitForFunction(String pageFunction, Object arg, WaitForFunctionOptions options);
  default void waitForLoadState(LoadState state) {
    waitForLoadState(state, null);
  }
  default void waitForLoadState() {
    waitForLoadState(null);
  }
  /**
   * Returns when the required load state has been reached.
   *
   * <p> This resolves when the page reaches a required load state, {@code load} by default. The navigation must have been committed
   * when this method is called. If current document has already reached the required state, resolves immediately.
   *
   * <p> Shortcut for main frame's [{@code method: Frame.waitForLoadState}].
   *
   * @param state Optional load state to wait for, defaults to {@code load}. If the state has been already reached while loading current
   * document, the method resolves immediately. Can be one of:
   * - {@code 'load'} - wait for the {@code load} event to be fired.
   * - {@code 'domcontentloaded'} - wait for the {@code DOMContentLoaded} event to be fired.
   * - {@code 'networkidle'} - wait until there are no network connections for at least {@code 500} ms.
   */
  void waitForLoadState(LoadState state, WaitForLoadStateOptions options);
  default Response waitForNavigation(Runnable code) { return waitForNavigation(code, null); }
  /**
   * Waits for the main frame navigation and returns the main resource response. In case of multiple redirects, the
   * navigation will resolve with the response of the last redirect. In case of navigation to a different anchor or
   * navigation due to History API usage, the navigation will resolve with {@code null}.
   *
   * <p> This resolves when the page navigates to a new URL or reloads. It is useful for when you run code which will indirectly
   * cause the page to navigate. e.g. The click target has an {@code onclick} handler that triggers navigation from a {@code setTimeout}.
   * Consider this example:
   *
   * <p> <strong>NOTE:</strong> Usage of the [History API](https://developer.mozilla.org/en-US/docs/Web/API/History_API) to change the URL is
   * considered a navigation.
   *
   * <p> Shortcut for main frame's [{@code method: Frame.waitForNavigation}].
   */
  Response waitForNavigation(Runnable code, WaitForNavigationOptions options);
  default Request waitForRequest(Runnable code) { return waitForRequest(code, (Predicate<Request>) null, null); }
  default Request waitForRequest(Runnable code, String urlGlob) { return waitForRequest(code, urlGlob, null); }
  default Request waitForRequest(Runnable code, Pattern urlPattern) { return waitForRequest(code, urlPattern, null); }
  default Request waitForRequest(Runnable code, Predicate<Request> predicate) { return waitForRequest(code, predicate, null); }
  Request waitForRequest(Runnable code, String urlGlob, WaitForRequestOptions options);
  Request waitForRequest(Runnable code, Pattern urlPattern, WaitForRequestOptions options);
  Request waitForRequest(Runnable code, Predicate<Request> predicate, WaitForRequestOptions options);
  default Response waitForResponse(Runnable code) { return waitForResponse(code, (Predicate<Response>) null, null); }
  default Response waitForResponse(Runnable code, String urlGlob) { return waitForResponse(code, urlGlob, null); }
  default Response waitForResponse(Runnable code, Pattern urlPattern) { return waitForResponse(code, urlPattern, null); }
  default Response waitForResponse(Runnable code, Predicate<Response> predicate) { return waitForResponse(code, predicate, null); }
  Response waitForResponse(Runnable code, String urlGlob, WaitForResponseOptions options);
  Response waitForResponse(Runnable code, Pattern urlPattern, WaitForResponseOptions options);
  Response waitForResponse(Runnable code, Predicate<Response> predicate, WaitForResponseOptions options);
  default ElementHandle waitForSelector(String selector) {
    return waitForSelector(selector, null);
  }
  /**
   * Returns when element specified by selector satisfies {@code state} option. Returns {@code null} if waiting for {@code hidden} or
   * {@code detached}.
   *
   * <p> Wait for the {@code selector} to satisfy {@code state} option (either appear/disappear from dom, or become visible/hidden). If at
   * the moment of calling the method {@code selector} already satisfies the condition, the method will return immediately. If the
   * selector doesn't satisfy the condition for the {@code timeout} milliseconds, the function will throw.
   *
   * <p> This method works across navigations:
   *
   *
   * @param selector A selector to query for. See [working with selectors](./selectors.md#working-with-selectors) for more details.
   */
  ElementHandle waitForSelector(String selector, WaitForSelectorOptions options);
  /**
   * Waits for the given {@code timeout} in milliseconds.
   *
   * <p> Note that {@code page.waitForTimeout()} should only be used for debugging. Tests using the timer in production are going to be
   * flaky. Use signals such as network events, selectors becoming visible and others instead.
   *
   * <p> Shortcut for main frame's [{@code method: Frame.waitForTimeout}].
   *
   * @param timeout A timeout to wait for
   */
  void waitForTimeout(double timeout);
  /**
   * This method returns all of the dedicated [WebWorkers](https://developer.mozilla.org/en-US/docs/Web/API/Web_Workers_API)
   * associated with the page.
   *
   * <p> <strong>NOTE:</strong> This does not contain ServiceWorkers
   */
  List<Worker> workers();
}

