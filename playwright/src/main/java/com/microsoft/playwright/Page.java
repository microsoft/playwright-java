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

import com.microsoft.playwright.options.*;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Page provides methods to interact with a single tab in a {@code Browser}, or an <a
 * href="https://developer.chrome.com/extensions/background_pages">extension background page</a> in Chromium. One {@code
 * Browser} instance might have multiple {@code Page} instances.
 *
 * <p> This example creates a page, navigates it to a URL, and then saves a screenshot:
 * <pre>{@code
 * import com.microsoft.playwright.*;
 *
 * public class Example {
 *   public static void main(String[] args) {
 *     try (Playwright playwright = Playwright.create()) {
 *       BrowserType webkit = playwright.webkit();
 *       Browser browser = webkit.launch();
 *       BrowserContext context = browser.newContext();
 *       Page page = context.newPage();
 *       page.navigate("https://example.com");
 *       page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("screenshot.png")));
 *       browser.close();
 *     }
 *   }
 * }
 * }</pre>
 *
 * <p> The Page class emits various events (described below) which can be handled using any of Node's native <a
 * href="https://nodejs.org/api/events.html#events_class_eventemitter">{@code EventEmitter}</a> methods, such as {@code
 * on}, {@code once} or {@code removeListener}.
 *
 * <p> This example logs a message for a single page {@code load} event:
 * <pre>{@code
 * page.onLoad(p -> System.out.println("Page loaded!"));
 * }</pre>
 *
 * <p> To unsubscribe from events use the {@code removeListener} method:
 * <pre>{@code
 * Consumer<Request> logRequest = interceptedRequest -> {
 *   System.out.println("A request was made: " + interceptedRequest.url());
 * };
 * page.onRequest(logRequest);
 * // Sometime later...
 * page.offRequest(logRequest);
 * }</pre>
 */
public interface Page extends AutoCloseable {

  /**
   * Emitted when the page closes.
   */
  void onClose(Consumer<Page> handler);
  /**
   * Removes handler that was previously added with {@link #onClose onClose(handler)}.
   */
  void offClose(Consumer<Page> handler);

  /**
   * Emitted when JavaScript within the page calls one of console API methods, e.g. {@code console.log} or {@code
   * console.dir}. Also emitted if the page throws an error or a warning.
   *
   * <p> The arguments passed into {@code console.log} are available on the {@code ConsoleMessage} event handler argument.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.onConsoleMessage(msg -> {
   *   for (int i = 0; i < msg.args().size(); ++i)
   *     System.out.println(i + ": " + msg.args().get(i).jsonValue());
   * });
   * page.evaluate("() => console.log('hello', 5, { foo: 'bar' })");
   * }</pre>
   */
  void onConsoleMessage(Consumer<ConsoleMessage> handler);
  /**
   * Removes handler that was previously added with {@link #onConsoleMessage onConsoleMessage(handler)}.
   */
  void offConsoleMessage(Consumer<ConsoleMessage> handler);

  /**
   * Emitted when the page crashes. Browser pages might crash if they try to allocate too much memory. When the page crashes,
   * ongoing and subsequent operations will throw.
   *
   * <p> The most common way to deal with crashes is to catch an exception:
   * <pre>{@code
   * try {
   *   // Crash might happen during a click.
   *   page.click("button");
   *   // Or while waiting for an event.
   *   page.waitForPopup(() -> {});
   * } catch (PlaywrightException e) {
   *   // When the page crashes, exception message contains "crash".
   * }
   * }</pre>
   */
  void onCrash(Consumer<Page> handler);
  /**
   * Removes handler that was previously added with {@link #onCrash onCrash(handler)}.
   */
  void offCrash(Consumer<Page> handler);

  /**
   * Emitted when a JavaScript dialog appears, such as {@code alert}, {@code prompt}, {@code confirm} or {@code
   * beforeunload}. Listener **must** either {@link Dialog#accept Dialog.accept()} or {@link Dialog#dismiss Dialog.dismiss()}
   * the dialog - otherwise the page will <a
   * href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/EventLoop#never_blocking">freeze</a> waiting for the
   * dialog, and actions like click will never finish.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.onDialog(dialog -> {
   *   dialog.accept();
   * });
   * }</pre>
   *
   * <p> <strong>NOTE:</strong> When no {@link Page#onDialog Page.onDialog()} or {@link BrowserContext#onDialog BrowserContext.onDialog()} listeners are
   * present, all dialogs are automatically dismissed.
   */
  void onDialog(Consumer<Dialog> handler);
  /**
   * Removes handler that was previously added with {@link #onDialog onDialog(handler)}.
   */
  void offDialog(Consumer<Dialog> handler);

  /**
   * Emitted when the JavaScript <a href="https://developer.mozilla.org/en-US/docs/Web/Events/DOMContentLoaded">{@code
   * DOMContentLoaded}</a> event is dispatched.
   */
  void onDOMContentLoaded(Consumer<Page> handler);
  /**
   * Removes handler that was previously added with {@link #onDOMContentLoaded onDOMContentLoaded(handler)}.
   */
  void offDOMContentLoaded(Consumer<Page> handler);

  /**
   * Emitted when attachment download started. User can access basic file operations on downloaded content via the passed
   * {@code Download} instance.
   */
  void onDownload(Consumer<Download> handler);
  /**
   * Removes handler that was previously added with {@link #onDownload onDownload(handler)}.
   */
  void offDownload(Consumer<Download> handler);

  /**
   * Emitted when a file chooser is supposed to appear, such as after clicking the  {@code <input type=file>}. Playwright can
   * respond to it via setting the input files using {@link FileChooser#setFiles FileChooser.setFiles()} that can be uploaded
   * after that.
   * <pre>{@code
   * page.onFileChooser(fileChooser -> {
   *   fileChooser.setFiles(Paths.get("/tmp/myfile.pdf"));
   * });
   * }</pre>
   */
  void onFileChooser(Consumer<FileChooser> handler);
  /**
   * Removes handler that was previously added with {@link #onFileChooser onFileChooser(handler)}.
   */
  void offFileChooser(Consumer<FileChooser> handler);

  /**
   * Emitted when a frame is attached.
   */
  void onFrameAttached(Consumer<Frame> handler);
  /**
   * Removes handler that was previously added with {@link #onFrameAttached onFrameAttached(handler)}.
   */
  void offFrameAttached(Consumer<Frame> handler);

  /**
   * Emitted when a frame is detached.
   */
  void onFrameDetached(Consumer<Frame> handler);
  /**
   * Removes handler that was previously added with {@link #onFrameDetached onFrameDetached(handler)}.
   */
  void offFrameDetached(Consumer<Frame> handler);

  /**
   * Emitted when a frame is navigated to a new url.
   */
  void onFrameNavigated(Consumer<Frame> handler);
  /**
   * Removes handler that was previously added with {@link #onFrameNavigated onFrameNavigated(handler)}.
   */
  void offFrameNavigated(Consumer<Frame> handler);

  /**
   * Emitted when the JavaScript <a href="https://developer.mozilla.org/en-US/docs/Web/Events/load">{@code load}</a> event is
   * dispatched.
   */
  void onLoad(Consumer<Page> handler);
  /**
   * Removes handler that was previously added with {@link #onLoad onLoad(handler)}.
   */
  void offLoad(Consumer<Page> handler);

  /**
   * Emitted when an uncaught exception happens within the page.
   * <pre>{@code
   * // Log all uncaught errors to the terminal
   * page.onPageError(exception -> {
   *   System.out.println("Uncaught exception: " + exception);
   * });
   *
   * // Navigate to a page with an exception.
   * page.navigate("data:text/html,<script>throw new Error('Test')</script>");
   * }</pre>
   */
  void onPageError(Consumer<String> handler);
  /**
   * Removes handler that was previously added with {@link #onPageError onPageError(handler)}.
   */
  void offPageError(Consumer<String> handler);

  /**
   * Emitted when the page opens a new tab or window. This event is emitted in addition to the {@link BrowserContext#onPage
   * BrowserContext.onPage()}, but only for popups relevant to this page.
   *
   * <p> The earliest moment that page is available is when it has navigated to the initial url. For example, when opening a
   * popup with {@code window.open('http://example.com')}, this event will fire when the network request to
   * "http://example.com" is done and its response has started loading in the popup.
   * <pre>{@code
   * Page popup = page.waitForPopup(() -> {
   *   page.getByText("open the popup").click();
   * });
   * System.out.println(popup.evaluate("location.href"));
   * }</pre>
   *
   * <p> <strong>NOTE:</strong> Use {@link Page#waitForLoadState Page.waitForLoadState()} to wait until the page gets to a particular state (you should
   * not need it in most cases).
   */
  void onPopup(Consumer<Page> handler);
  /**
   * Removes handler that was previously added with {@link #onPopup onPopup(handler)}.
   */
  void offPopup(Consumer<Page> handler);

  /**
   * Emitted when a page issues a request. The [request] object is read-only. In order to intercept and mutate requests, see
   * {@link Page#route Page.route()} or {@link BrowserContext#route BrowserContext.route()}.
   */
  void onRequest(Consumer<Request> handler);
  /**
   * Removes handler that was previously added with {@link #onRequest onRequest(handler)}.
   */
  void offRequest(Consumer<Request> handler);

  /**
   * Emitted when a request fails, for example by timing out.
   * <pre>{@code
   * page.onRequestFailed(request -> {
   *   System.out.println(request.url() + " " + request.failure());
   * });
   * }</pre>
   *
   * <p> <strong>NOTE:</strong> HTTP Error responses, such as 404 or 503, are still successful responses from HTTP standpoint, so request will complete
   * with {@link Page#onRequestFinished Page.onRequestFinished()} event and not with {@link Page#onRequestFailed
   * Page.onRequestFailed()}. A request will only be considered failed when the client cannot get an HTTP response from the
   * server, e.g. due to network error net::ERR_FAILED.
   */
  void onRequestFailed(Consumer<Request> handler);
  /**
   * Removes handler that was previously added with {@link #onRequestFailed onRequestFailed(handler)}.
   */
  void offRequestFailed(Consumer<Request> handler);

  /**
   * Emitted when a request finishes successfully after downloading the response body. For a successful response, the
   * sequence of events is {@code request}, {@code response} and {@code requestfinished}.
   */
  void onRequestFinished(Consumer<Request> handler);
  /**
   * Removes handler that was previously added with {@link #onRequestFinished onRequestFinished(handler)}.
   */
  void offRequestFinished(Consumer<Request> handler);

  /**
   * Emitted when [response] status and headers are received for a request. For a successful response, the sequence of events
   * is {@code request}, {@code response} and {@code requestfinished}.
   */
  void onResponse(Consumer<Response> handler);
  /**
   * Removes handler that was previously added with {@link #onResponse onResponse(handler)}.
   */
  void offResponse(Consumer<Response> handler);

  /**
   * Emitted when {@code WebSocket} request is sent.
   */
  void onWebSocket(Consumer<WebSocket> handler);
  /**
   * Removes handler that was previously added with {@link #onWebSocket onWebSocket(handler)}.
   */
  void offWebSocket(Consumer<WebSocket> handler);

  /**
   * Emitted when a dedicated <a href="https://developer.mozilla.org/en-US/docs/Web/API/Web_Workers_API">WebWorker</a> is
   * spawned by the page.
   */
  void onWorker(Consumer<Worker> handler);
  /**
   * Removes handler that was previously added with {@link #onWorker onWorker(handler)}.
   */
  void offWorker(Consumer<Worker> handler);

  class AddScriptTagOptions {
    /**
     * Raw JavaScript content to be injected into frame.
     */
    public String content;
    /**
     * Path to the JavaScript file to be injected into frame. If {@code path} is a relative path, then it is resolved relative
     * to the current working directory.
     */
    public Path path;
    /**
     * Script type. Use 'module' in order to load a Javascript ES6 module. See <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/script">script</a> for more details.
     */
    public String type;
    /**
     * URL of a script to be added.
     */
    public String url;

    /**
     * Raw JavaScript content to be injected into frame.
     */
    public AddScriptTagOptions setContent(String content) {
      this.content = content;
      return this;
    }
    /**
     * Path to the JavaScript file to be injected into frame. If {@code path} is a relative path, then it is resolved relative
     * to the current working directory.
     */
    public AddScriptTagOptions setPath(Path path) {
      this.path = path;
      return this;
    }
    /**
     * Script type. Use 'module' in order to load a Javascript ES6 module. See <a
     * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/script">script</a> for more details.
     */
    public AddScriptTagOptions setType(String type) {
      this.type = type;
      return this;
    }
    /**
     * URL of a script to be added.
     */
    public AddScriptTagOptions setUrl(String url) {
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

    /**
     * Raw CSS content to be injected into frame.
     */
    public AddStyleTagOptions setContent(String content) {
      this.content = content;
      return this;
    }
    /**
     * Path to the CSS file to be injected into frame. If {@code path} is a relative path, then it is resolved relative to the
     * current working directory.
     */
    public AddStyleTagOptions setPath(Path path) {
      this.path = path;
      return this;
    }
    /**
     * URL of the {@code <link>} tag.
     */
    public AddStyleTagOptions setUrl(String url) {
      this.url = url;
      return this;
    }
  }
  class CheckOptions {
    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public Boolean force;
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;
    /**
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public Boolean trial;

    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public CheckOptions setForce(boolean force) {
      this.force = force;
      return this;
    }
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public CheckOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public CheckOptions setPosition(double x, double y) {
      return setPosition(new Position(x, y));
    }
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public CheckOptions setPosition(Position position) {
      this.position = position;
      return this;
    }
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public CheckOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public CheckOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public CheckOptions setTrial(boolean trial) {
      this.trial = trial;
      return this;
    }
  }
  class ClickOptions {
    /**
     * Defaults to {@code left}.
     */
    public MouseButton button;
    /**
     * defaults to 1. See [UIEvent.detail].
     */
    public Integer clickCount;
    /**
     * Time to wait between {@code mousedown} and {@code mouseup} in milliseconds. Defaults to 0.
     */
    public Double delay;
    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public Boolean force;
    /**
     * Modifier keys to press. Ensures that only these modifiers are pressed during the operation, and then restores current
     * modifiers back. If not specified, currently pressed modifiers are used.
     */
    public List<KeyboardModifier> modifiers;
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;
    /**
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public Boolean trial;

    /**
     * Defaults to {@code left}.
     */
    public ClickOptions setButton(MouseButton button) {
      this.button = button;
      return this;
    }
    /**
     * defaults to 1. See [UIEvent.detail].
     */
    public ClickOptions setClickCount(int clickCount) {
      this.clickCount = clickCount;
      return this;
    }
    /**
     * Time to wait between {@code mousedown} and {@code mouseup} in milliseconds. Defaults to 0.
     */
    public ClickOptions setDelay(double delay) {
      this.delay = delay;
      return this;
    }
    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public ClickOptions setForce(boolean force) {
      this.force = force;
      return this;
    }
    /**
     * Modifier keys to press. Ensures that only these modifiers are pressed during the operation, and then restores current
     * modifiers back. If not specified, currently pressed modifiers are used.
     */
    public ClickOptions setModifiers(List<KeyboardModifier> modifiers) {
      this.modifiers = modifiers;
      return this;
    }
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public ClickOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public ClickOptions setPosition(double x, double y) {
      return setPosition(new Position(x, y));
    }
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public ClickOptions setPosition(Position position) {
      this.position = position;
      return this;
    }
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public ClickOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public ClickOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public ClickOptions setTrial(boolean trial) {
      this.trial = trial;
      return this;
    }
  }
  class CloseOptions {
    /**
     * Defaults to {@code false}. Whether to run the <a
     * href="https://developer.mozilla.org/en-US/docs/Web/Events/beforeunload">before unload</a> page handlers.
     */
    public Boolean runBeforeUnload;

    /**
     * Defaults to {@code false}. Whether to run the <a
     * href="https://developer.mozilla.org/en-US/docs/Web/Events/beforeunload">before unload</a> page handlers.
     */
    public CloseOptions setRunBeforeUnload(boolean runBeforeUnload) {
      this.runBeforeUnload = runBeforeUnload;
      return this;
    }
  }
  class DblclickOptions {
    /**
     * Defaults to {@code left}.
     */
    public MouseButton button;
    /**
     * Time to wait between {@code mousedown} and {@code mouseup} in milliseconds. Defaults to 0.
     */
    public Double delay;
    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public Boolean force;
    /**
     * Modifier keys to press. Ensures that only these modifiers are pressed during the operation, and then restores current
     * modifiers back. If not specified, currently pressed modifiers are used.
     */
    public List<KeyboardModifier> modifiers;
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;
    /**
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public Boolean trial;

    /**
     * Defaults to {@code left}.
     */
    public DblclickOptions setButton(MouseButton button) {
      this.button = button;
      return this;
    }
    /**
     * Time to wait between {@code mousedown} and {@code mouseup} in milliseconds. Defaults to 0.
     */
    public DblclickOptions setDelay(double delay) {
      this.delay = delay;
      return this;
    }
    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public DblclickOptions setForce(boolean force) {
      this.force = force;
      return this;
    }
    /**
     * Modifier keys to press. Ensures that only these modifiers are pressed during the operation, and then restores current
     * modifiers back. If not specified, currently pressed modifiers are used.
     */
    public DblclickOptions setModifiers(List<KeyboardModifier> modifiers) {
      this.modifiers = modifiers;
      return this;
    }
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public DblclickOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public DblclickOptions setPosition(double x, double y) {
      return setPosition(new Position(x, y));
    }
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public DblclickOptions setPosition(Position position) {
      this.position = position;
      return this;
    }
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public DblclickOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public DblclickOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public DblclickOptions setTrial(boolean trial) {
      this.trial = trial;
      return this;
    }
  }
  class DispatchEventOptions {
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public DispatchEventOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public DispatchEventOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class DragAndDropOptions {
    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public Boolean force;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * Clicks on the source element at this point relative to the top-left corner of the element's padding box. If not
     * specified, some visible point of the element is used.
     */
    public Position sourcePosition;
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;
    /**
     * Drops on the target element at this point relative to the top-left corner of the element's padding box. If not
     * specified, some visible point of the element is used.
     */
    public Position targetPosition;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;
    /**
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public Boolean trial;

    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public DragAndDropOptions setForce(boolean force) {
      this.force = force;
      return this;
    }
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public DragAndDropOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    /**
     * Clicks on the source element at this point relative to the top-left corner of the element's padding box. If not
     * specified, some visible point of the element is used.
     */
    public DragAndDropOptions setSourcePosition(double x, double y) {
      return setSourcePosition(new Position(x, y));
    }
    /**
     * Clicks on the source element at this point relative to the top-left corner of the element's padding box. If not
     * specified, some visible point of the element is used.
     */
    public DragAndDropOptions setSourcePosition(Position sourcePosition) {
      this.sourcePosition = sourcePosition;
      return this;
    }
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public DragAndDropOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
    /**
     * Drops on the target element at this point relative to the top-left corner of the element's padding box. If not
     * specified, some visible point of the element is used.
     */
    public DragAndDropOptions setTargetPosition(double x, double y) {
      return setTargetPosition(new Position(x, y));
    }
    /**
     * Drops on the target element at this point relative to the top-left corner of the element's padding box. If not
     * specified, some visible point of the element is used.
     */
    public DragAndDropOptions setTargetPosition(Position targetPosition) {
      this.targetPosition = targetPosition;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public DragAndDropOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public DragAndDropOptions setTrial(boolean trial) {
      this.trial = trial;
      return this;
    }
  }
  class EmulateMediaOptions {
    /**
     * Emulates {@code "prefers-colors-scheme"} media feature, supported values are {@code "light"}, {@code "dark"}, {@code
     * "no-preference"}. Passing {@code null} disables color scheme emulation.
     */
    public Optional<ColorScheme> colorScheme;
    /**
     * Emulates {@code "forced-colors"} media feature, supported values are {@code "active"} and {@code "none"}. Passing {@code
     * null} disables forced colors emulation.
     */
    public Optional<ForcedColors> forcedColors;
    /**
     * Changes the CSS media type of the page. The only allowed values are {@code "screen"}, {@code "print"} and {@code null}.
     * Passing {@code null} disables CSS media emulation.
     */
    public Optional<Media> media;
    /**
     * Emulates {@code "prefers-reduced-motion"} media feature, supported values are {@code "reduce"}, {@code "no-preference"}.
     * Passing {@code null} disables reduced motion emulation.
     */
    public Optional<ReducedMotion> reducedMotion;

    /**
     * Emulates {@code "prefers-colors-scheme"} media feature, supported values are {@code "light"}, {@code "dark"}, {@code
     * "no-preference"}. Passing {@code null} disables color scheme emulation.
     */
    public EmulateMediaOptions setColorScheme(ColorScheme colorScheme) {
      this.colorScheme = Optional.ofNullable(colorScheme);
      return this;
    }
    /**
     * Emulates {@code "forced-colors"} media feature, supported values are {@code "active"} and {@code "none"}. Passing {@code
     * null} disables forced colors emulation.
     */
    public EmulateMediaOptions setForcedColors(ForcedColors forcedColors) {
      this.forcedColors = Optional.ofNullable(forcedColors);
      return this;
    }
    /**
     * Changes the CSS media type of the page. The only allowed values are {@code "screen"}, {@code "print"} and {@code null}.
     * Passing {@code null} disables CSS media emulation.
     */
    public EmulateMediaOptions setMedia(Media media) {
      this.media = Optional.ofNullable(media);
      return this;
    }
    /**
     * Emulates {@code "prefers-reduced-motion"} media feature, supported values are {@code "reduce"}, {@code "no-preference"}.
     * Passing {@code null} disables reduced motion emulation.
     */
    public EmulateMediaOptions setReducedMotion(ReducedMotion reducedMotion) {
      this.reducedMotion = Optional.ofNullable(reducedMotion);
      return this;
    }
  }
  class EvalOnSelectorOptions {
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;

    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public EvalOnSelectorOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
  }
  class ExposeBindingOptions {
    /**
     * Whether to pass the argument as a handle, instead of passing by value. When passing a handle, only one argument is
     * supported. When passing by value, multiple arguments are supported.
     */
    public Boolean handle;

    /**
     * Whether to pass the argument as a handle, instead of passing by value. When passing a handle, only one argument is
     * supported. When passing by value, multiple arguments are supported.
     */
    public ExposeBindingOptions setHandle(boolean handle) {
      this.handle = handle;
      return this;
    }
  }
  class FillOptions {
    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public Boolean force;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public FillOptions setForce(boolean force) {
      this.force = force;
      return this;
    }
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public FillOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public FillOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public FillOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class FocusOptions {
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public FocusOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public FocusOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class GetAttributeOptions {
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public GetAttributeOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public GetAttributeOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class GetByAltTextOptions {
    /**
     * Whether to find an exact match: case-sensitive and whole-string. Default to false. Ignored when locating by a regular
     * expression. Note that exact match still trims whitespace.
     */
    public Boolean exact;

    /**
     * Whether to find an exact match: case-sensitive and whole-string. Default to false. Ignored when locating by a regular
     * expression. Note that exact match still trims whitespace.
     */
    public GetByAltTextOptions setExact(boolean exact) {
      this.exact = exact;
      return this;
    }
  }
  class GetByLabelOptions {
    /**
     * Whether to find an exact match: case-sensitive and whole-string. Default to false. Ignored when locating by a regular
     * expression. Note that exact match still trims whitespace.
     */
    public Boolean exact;

    /**
     * Whether to find an exact match: case-sensitive and whole-string. Default to false. Ignored when locating by a regular
     * expression. Note that exact match still trims whitespace.
     */
    public GetByLabelOptions setExact(boolean exact) {
      this.exact = exact;
      return this;
    }
  }
  class GetByPlaceholderOptions {
    /**
     * Whether to find an exact match: case-sensitive and whole-string. Default to false. Ignored when locating by a regular
     * expression. Note that exact match still trims whitespace.
     */
    public Boolean exact;

    /**
     * Whether to find an exact match: case-sensitive and whole-string. Default to false. Ignored when locating by a regular
     * expression. Note that exact match still trims whitespace.
     */
    public GetByPlaceholderOptions setExact(boolean exact) {
      this.exact = exact;
      return this;
    }
  }
  class GetByRoleOptions {
    /**
     * An attribute that is usually set by {@code aria-checked} or native {@code <input type=checkbox>} controls.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-checked">{@code aria-checked}</a>.
     */
    public Boolean checked;
    /**
     * An attribute that is usually set by {@code aria-disabled} or {@code disabled}.
     *
     * <p> <strong>NOTE:</strong> Unlike most other attributes, {@code disabled} is inherited through the DOM hierarchy. Learn more about <a
     * href="https://www.w3.org/TR/wai-aria-1.2/#aria-disabled">{@code aria-disabled}</a>.
     */
    public Boolean disabled;
    /**
     * Whether {@code name} is matched exactly: case-sensitive and whole-string. Defaults to false. Ignored when {@code name}
     * is a regular expression. Note that exact match still trims whitespace.
     */
    public Boolean exact;
    /**
     * An attribute that is usually set by {@code aria-expanded}.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-expanded">{@code aria-expanded}</a>.
     */
    public Boolean expanded;
    /**
     * Option that controls whether hidden elements are matched. By default, only non-hidden elements, as <a
     * href="https://www.w3.org/TR/wai-aria-1.2/#tree_exclusion">defined by ARIA</a>, are matched by role selector.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-hidden">{@code aria-hidden}</a>.
     */
    public Boolean includeHidden;
    /**
     * A number attribute that is usually present for roles {@code heading}, {@code listitem}, {@code row}, {@code treeitem},
     * with default values for {@code <h1>-<h6>} elements.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-level">{@code aria-level}</a>.
     */
    public Integer level;
    /**
     * Option to match the <a href="https://w3c.github.io/accname/#dfn-accessible-name">accessible name</a>. By default,
     * matching is case-insensitive and searches for a substring, use {@code exact} to control this behavior.
     *
     * <p> Learn more about <a href="https://w3c.github.io/accname/#dfn-accessible-name">accessible name</a>.
     */
    public Object name;
    /**
     * An attribute that is usually set by {@code aria-pressed}.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-pressed">{@code aria-pressed}</a>.
     */
    public Boolean pressed;
    /**
     * An attribute that is usually set by {@code aria-selected}.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-selected">{@code aria-selected}</a>.
     */
    public Boolean selected;

    /**
     * An attribute that is usually set by {@code aria-checked} or native {@code <input type=checkbox>} controls.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-checked">{@code aria-checked}</a>.
     */
    public GetByRoleOptions setChecked(boolean checked) {
      this.checked = checked;
      return this;
    }
    /**
     * An attribute that is usually set by {@code aria-disabled} or {@code disabled}.
     *
     * <p> <strong>NOTE:</strong> Unlike most other attributes, {@code disabled} is inherited through the DOM hierarchy. Learn more about <a
     * href="https://www.w3.org/TR/wai-aria-1.2/#aria-disabled">{@code aria-disabled}</a>.
     */
    public GetByRoleOptions setDisabled(boolean disabled) {
      this.disabled = disabled;
      return this;
    }
    /**
     * Whether {@code name} is matched exactly: case-sensitive and whole-string. Defaults to false. Ignored when {@code name}
     * is a regular expression. Note that exact match still trims whitespace.
     */
    public GetByRoleOptions setExact(boolean exact) {
      this.exact = exact;
      return this;
    }
    /**
     * An attribute that is usually set by {@code aria-expanded}.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-expanded">{@code aria-expanded}</a>.
     */
    public GetByRoleOptions setExpanded(boolean expanded) {
      this.expanded = expanded;
      return this;
    }
    /**
     * Option that controls whether hidden elements are matched. By default, only non-hidden elements, as <a
     * href="https://www.w3.org/TR/wai-aria-1.2/#tree_exclusion">defined by ARIA</a>, are matched by role selector.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-hidden">{@code aria-hidden}</a>.
     */
    public GetByRoleOptions setIncludeHidden(boolean includeHidden) {
      this.includeHidden = includeHidden;
      return this;
    }
    /**
     * A number attribute that is usually present for roles {@code heading}, {@code listitem}, {@code row}, {@code treeitem},
     * with default values for {@code <h1>-<h6>} elements.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-level">{@code aria-level}</a>.
     */
    public GetByRoleOptions setLevel(int level) {
      this.level = level;
      return this;
    }
    /**
     * Option to match the <a href="https://w3c.github.io/accname/#dfn-accessible-name">accessible name</a>. By default,
     * matching is case-insensitive and searches for a substring, use {@code exact} to control this behavior.
     *
     * <p> Learn more about <a href="https://w3c.github.io/accname/#dfn-accessible-name">accessible name</a>.
     */
    public GetByRoleOptions setName(String name) {
      this.name = name;
      return this;
    }
    /**
     * Option to match the <a href="https://w3c.github.io/accname/#dfn-accessible-name">accessible name</a>. By default,
     * matching is case-insensitive and searches for a substring, use {@code exact} to control this behavior.
     *
     * <p> Learn more about <a href="https://w3c.github.io/accname/#dfn-accessible-name">accessible name</a>.
     */
    public GetByRoleOptions setName(Pattern name) {
      this.name = name;
      return this;
    }
    /**
     * An attribute that is usually set by {@code aria-pressed}.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-pressed">{@code aria-pressed}</a>.
     */
    public GetByRoleOptions setPressed(boolean pressed) {
      this.pressed = pressed;
      return this;
    }
    /**
     * An attribute that is usually set by {@code aria-selected}.
     *
     * <p> Learn more about <a href="https://www.w3.org/TR/wai-aria-1.2/#aria-selected">{@code aria-selected}</a>.
     */
    public GetByRoleOptions setSelected(boolean selected) {
      this.selected = selected;
      return this;
    }
  }
  class GetByTextOptions {
    /**
     * Whether to find an exact match: case-sensitive and whole-string. Default to false. Ignored when locating by a regular
     * expression. Note that exact match still trims whitespace.
     */
    public Boolean exact;

    /**
     * Whether to find an exact match: case-sensitive and whole-string. Default to false. Ignored when locating by a regular
     * expression. Note that exact match still trims whitespace.
     */
    public GetByTextOptions setExact(boolean exact) {
      this.exact = exact;
      return this;
    }
  }
  class GetByTitleOptions {
    /**
     * Whether to find an exact match: case-sensitive and whole-string. Default to false. Ignored when locating by a regular
     * expression. Note that exact match still trims whitespace.
     */
    public Boolean exact;

    /**
     * Whether to find an exact match: case-sensitive and whole-string. Default to false. Ignored when locating by a regular
     * expression. Note that exact match still trims whitespace.
     */
    public GetByTitleOptions setExact(boolean exact) {
      this.exact = exact;
      return this;
    }
  }
  class GoBackOptions {
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can
     * be changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * <ul>
     * <li> {@code "domcontentloaded"} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.</li>
     * <li> {@code "load"} - consider operation to be finished when the {@code load} event is fired.</li>
     * <li> {@code "networkidle"} - **DISCOURAGED** consider operation to be finished when there are no network connections for at
     * least {@code 500} ms. Don't use this method for testing, rely on web assertions to assess readiness instead.</li>
     * <li> {@code "commit"} - consider operation to be finished when network response is received and the document started loading.</li>
     * </ul>
     */
    public WaitUntilState waitUntil;

    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can
     * be changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public GoBackOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * <ul>
     * <li> {@code "domcontentloaded"} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.</li>
     * <li> {@code "load"} - consider operation to be finished when the {@code load} event is fired.</li>
     * <li> {@code "networkidle"} - **DISCOURAGED** consider operation to be finished when there are no network connections for at
     * least {@code 500} ms. Don't use this method for testing, rely on web assertions to assess readiness instead.</li>
     * <li> {@code "commit"} - consider operation to be finished when network response is received and the document started loading.</li>
     * </ul>
     */
    public GoBackOptions setWaitUntil(WaitUntilState waitUntil) {
      this.waitUntil = waitUntil;
      return this;
    }
  }
  class GoForwardOptions {
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can
     * be changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * <ul>
     * <li> {@code "domcontentloaded"} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.</li>
     * <li> {@code "load"} - consider operation to be finished when the {@code load} event is fired.</li>
     * <li> {@code "networkidle"} - **DISCOURAGED** consider operation to be finished when there are no network connections for at
     * least {@code 500} ms. Don't use this method for testing, rely on web assertions to assess readiness instead.</li>
     * <li> {@code "commit"} - consider operation to be finished when network response is received and the document started loading.</li>
     * </ul>
     */
    public WaitUntilState waitUntil;

    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can
     * be changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public GoForwardOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * <ul>
     * <li> {@code "domcontentloaded"} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.</li>
     * <li> {@code "load"} - consider operation to be finished when the {@code load} event is fired.</li>
     * <li> {@code "networkidle"} - **DISCOURAGED** consider operation to be finished when there are no network connections for at
     * least {@code 500} ms. Don't use this method for testing, rely on web assertions to assess readiness instead.</li>
     * <li> {@code "commit"} - consider operation to be finished when network response is received and the document started loading.</li>
     * </ul>
     */
    public GoForwardOptions setWaitUntil(WaitUntilState waitUntil) {
      this.waitUntil = waitUntil;
      return this;
    }
  }
  class NavigateOptions {
    /**
     * Referer header value. If provided it will take preference over the referer header value set by {@link
     * Page#setExtraHTTPHeaders Page.setExtraHTTPHeaders()}.
     */
    public String referer;
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can
     * be changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * <ul>
     * <li> {@code "domcontentloaded"} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.</li>
     * <li> {@code "load"} - consider operation to be finished when the {@code load} event is fired.</li>
     * <li> {@code "networkidle"} - **DISCOURAGED** consider operation to be finished when there are no network connections for at
     * least {@code 500} ms. Don't use this method for testing, rely on web assertions to assess readiness instead.</li>
     * <li> {@code "commit"} - consider operation to be finished when network response is received and the document started loading.</li>
     * </ul>
     */
    public WaitUntilState waitUntil;

    /**
     * Referer header value. If provided it will take preference over the referer header value set by {@link
     * Page#setExtraHTTPHeaders Page.setExtraHTTPHeaders()}.
     */
    public NavigateOptions setReferer(String referer) {
      this.referer = referer;
      return this;
    }
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can
     * be changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public NavigateOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * <ul>
     * <li> {@code "domcontentloaded"} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.</li>
     * <li> {@code "load"} - consider operation to be finished when the {@code load} event is fired.</li>
     * <li> {@code "networkidle"} - **DISCOURAGED** consider operation to be finished when there are no network connections for at
     * least {@code 500} ms. Don't use this method for testing, rely on web assertions to assess readiness instead.</li>
     * <li> {@code "commit"} - consider operation to be finished when network response is received and the document started loading.</li>
     * </ul>
     */
    public NavigateOptions setWaitUntil(WaitUntilState waitUntil) {
      this.waitUntil = waitUntil;
      return this;
    }
  }
  class HoverOptions {
    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public Boolean force;
    /**
     * Modifier keys to press. Ensures that only these modifiers are pressed during the operation, and then restores current
     * modifiers back. If not specified, currently pressed modifiers are used.
     */
    public List<KeyboardModifier> modifiers;
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;
    /**
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public Boolean trial;

    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public HoverOptions setForce(boolean force) {
      this.force = force;
      return this;
    }
    /**
     * Modifier keys to press. Ensures that only these modifiers are pressed during the operation, and then restores current
     * modifiers back. If not specified, currently pressed modifiers are used.
     */
    public HoverOptions setModifiers(List<KeyboardModifier> modifiers) {
      this.modifiers = modifiers;
      return this;
    }
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public HoverOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public HoverOptions setPosition(double x, double y) {
      return setPosition(new Position(x, y));
    }
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public HoverOptions setPosition(Position position) {
      this.position = position;
      return this;
    }
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public HoverOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public HoverOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public HoverOptions setTrial(boolean trial) {
      this.trial = trial;
      return this;
    }
  }
  class InnerHTMLOptions {
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public InnerHTMLOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public InnerHTMLOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class InnerTextOptions {
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public InnerTextOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public InnerTextOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class InputValueOptions {
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public InputValueOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public InputValueOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsCheckedOptions {
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public IsCheckedOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public IsCheckedOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsDisabledOptions {
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public IsDisabledOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public IsDisabledOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsEditableOptions {
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public IsEditableOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public IsEditableOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsEnabledOptions {
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public IsEnabledOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public IsEnabledOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsHiddenOptions {
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;
    /**
     * @deprecated This option is ignored. {@link Page#isHidden Page.isHidden()} does not wait for the element to become hidden and returns
     * immediately.
     */
    public Double timeout;

    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public IsHiddenOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
    /**
     * @deprecated This option is ignored. {@link Page#isHidden Page.isHidden()} does not wait for the element to become hidden and returns
     * immediately.
     */
    public IsHiddenOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class IsVisibleOptions {
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;
    /**
     * @deprecated This option is ignored. {@link Page#isVisible Page.isVisible()} does not wait for the element to become visible and
     * returns immediately.
     */
    public Double timeout;

    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public IsVisibleOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
    /**
     * @deprecated This option is ignored. {@link Page#isVisible Page.isVisible()} does not wait for the element to become visible and
     * returns immediately.
     */
    public IsVisibleOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class LocatorOptions {
    /**
     * Matches elements containing an element that matches an inner locator. Inner locator is queried against the outer one.
     * For example, {@code article} that has {@code text=Playwright} matches {@code <article><div>Playwright</div></article>}.
     *
     * <p> Note that outer and inner locators must belong to the same frame. Inner locator must not contain {@code FrameLocator}s.
     */
    public Locator has;
    /**
     * Matches elements that do not contain an element that matches an inner locator. Inner locator is queried against the
     * outer one. For example, {@code article} that does not have {@code div} matches {@code
     * <article><span>Playwright</span></article>}.
     *
     * <p> Note that outer and inner locators must belong to the same frame. Inner locator must not contain {@code FrameLocator}s.
     */
    public Locator hasNot;
    /**
     * Matches elements that do not contain specified text somewhere inside, possibly in a child or a descendant element. When
     * passed a [string], matching is case-insensitive and searches for a substring.
     */
    public Object hasNotText;
    /**
     * Matches elements containing specified text somewhere inside, possibly in a child or a descendant element. When passed a
     * [string], matching is case-insensitive and searches for a substring. For example, {@code "Playwright"} matches {@code
     * <article><div>Playwright</div></article>}.
     */
    public Object hasText;

    /**
     * Matches elements containing an element that matches an inner locator. Inner locator is queried against the outer one.
     * For example, {@code article} that has {@code text=Playwright} matches {@code <article><div>Playwright</div></article>}.
     *
     * <p> Note that outer and inner locators must belong to the same frame. Inner locator must not contain {@code FrameLocator}s.
     */
    public LocatorOptions setHas(Locator has) {
      this.has = has;
      return this;
    }
    /**
     * Matches elements that do not contain an element that matches an inner locator. Inner locator is queried against the
     * outer one. For example, {@code article} that does not have {@code div} matches {@code
     * <article><span>Playwright</span></article>}.
     *
     * <p> Note that outer and inner locators must belong to the same frame. Inner locator must not contain {@code FrameLocator}s.
     */
    public LocatorOptions setHasNot(Locator hasNot) {
      this.hasNot = hasNot;
      return this;
    }
    /**
     * Matches elements that do not contain specified text somewhere inside, possibly in a child or a descendant element. When
     * passed a [string], matching is case-insensitive and searches for a substring.
     */
    public LocatorOptions setHasNotText(String hasNotText) {
      this.hasNotText = hasNotText;
      return this;
    }
    /**
     * Matches elements that do not contain specified text somewhere inside, possibly in a child or a descendant element. When
     * passed a [string], matching is case-insensitive and searches for a substring.
     */
    public LocatorOptions setHasNotText(Pattern hasNotText) {
      this.hasNotText = hasNotText;
      return this;
    }
    /**
     * Matches elements containing specified text somewhere inside, possibly in a child or a descendant element. When passed a
     * [string], matching is case-insensitive and searches for a substring. For example, {@code "Playwright"} matches {@code
     * <article><div>Playwright</div></article>}.
     */
    public LocatorOptions setHasText(String hasText) {
      this.hasText = hasText;
      return this;
    }
    /**
     * Matches elements containing specified text somewhere inside, possibly in a child or a descendant element. When passed a
     * [string], matching is case-insensitive and searches for a substring. For example, {@code "Playwright"} matches {@code
     * <article><div>Playwright</div></article>}.
     */
    public LocatorOptions setHasText(Pattern hasText) {
      this.hasText = hasText;
      return this;
    }
  }
  class PdfOptions {
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
     * <ul>
     * <li> {@code "date"} formatted print date</li>
     * <li> {@code "title"} document title</li>
     * <li> {@code "url"} document location</li>
     * <li> {@code "pageNumber"} current page number</li>
     * <li> {@code "totalPages"} total pages in the document</li>
     * </ul>
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
     * The file path to save the PDF to. If {@code path} is a relative path, then it is resolved relative to the current
     * working directory. If no path is provided, the PDF won't be saved to the disk.
     */
    public Path path;
    /**
     * Give any CSS {@code @page} size declared in the page priority over what is declared in {@code width} and {@code height}
     * or {@code format} options. Defaults to {@code false}, which will scale the content to fit the paper size.
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

    /**
     * Display header and footer. Defaults to {@code false}.
     */
    public PdfOptions setDisplayHeaderFooter(boolean displayHeaderFooter) {
      this.displayHeaderFooter = displayHeaderFooter;
      return this;
    }
    /**
     * HTML template for the print footer. Should use the same format as the {@code headerTemplate}.
     */
    public PdfOptions setFooterTemplate(String footerTemplate) {
      this.footerTemplate = footerTemplate;
      return this;
    }
    /**
     * Paper format. If set, takes priority over {@code width} or {@code height} options. Defaults to 'Letter'.
     */
    public PdfOptions setFormat(String format) {
      this.format = format;
      return this;
    }
    /**
     * HTML template for the print header. Should be valid HTML markup with following classes used to inject printing values
     * into them:
     * <ul>
     * <li> {@code "date"} formatted print date</li>
     * <li> {@code "title"} document title</li>
     * <li> {@code "url"} document location</li>
     * <li> {@code "pageNumber"} current page number</li>
     * <li> {@code "totalPages"} total pages in the document</li>
     * </ul>
     */
    public PdfOptions setHeaderTemplate(String headerTemplate) {
      this.headerTemplate = headerTemplate;
      return this;
    }
    /**
     * Paper height, accepts values labeled with units.
     */
    public PdfOptions setHeight(String height) {
      this.height = height;
      return this;
    }
    /**
     * Paper orientation. Defaults to {@code false}.
     */
    public PdfOptions setLandscape(boolean landscape) {
      this.landscape = landscape;
      return this;
    }
    /**
     * Paper margins, defaults to none.
     */
    public PdfOptions setMargin(Margin margin) {
      this.margin = margin;
      return this;
    }
    /**
     * Paper ranges to print, e.g., '1-5, 8, 11-13'. Defaults to the empty string, which means print all pages.
     */
    public PdfOptions setPageRanges(String pageRanges) {
      this.pageRanges = pageRanges;
      return this;
    }
    /**
     * The file path to save the PDF to. If {@code path} is a relative path, then it is resolved relative to the current
     * working directory. If no path is provided, the PDF won't be saved to the disk.
     */
    public PdfOptions setPath(Path path) {
      this.path = path;
      return this;
    }
    /**
     * Give any CSS {@code @page} size declared in the page priority over what is declared in {@code width} and {@code height}
     * or {@code format} options. Defaults to {@code false}, which will scale the content to fit the paper size.
     */
    public PdfOptions setPreferCSSPageSize(boolean preferCSSPageSize) {
      this.preferCSSPageSize = preferCSSPageSize;
      return this;
    }
    /**
     * Print background graphics. Defaults to {@code false}.
     */
    public PdfOptions setPrintBackground(boolean printBackground) {
      this.printBackground = printBackground;
      return this;
    }
    /**
     * Scale of the webpage rendering. Defaults to {@code 1}. Scale amount must be between 0.1 and 2.
     */
    public PdfOptions setScale(double scale) {
      this.scale = scale;
      return this;
    }
    /**
     * Paper width, accepts values labeled with units.
     */
    public PdfOptions setWidth(String width) {
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Time to wait between {@code keydown} and {@code keyup} in milliseconds. Defaults to 0.
     */
    public PressOptions setDelay(double delay) {
      this.delay = delay;
      return this;
    }
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public PressOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public PressOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public PressOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class QuerySelectorOptions {
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;

    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public QuerySelectorOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
  }
  class ReloadOptions {
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can
     * be changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * <ul>
     * <li> {@code "domcontentloaded"} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.</li>
     * <li> {@code "load"} - consider operation to be finished when the {@code load} event is fired.</li>
     * <li> {@code "networkidle"} - **DISCOURAGED** consider operation to be finished when there are no network connections for at
     * least {@code 500} ms. Don't use this method for testing, rely on web assertions to assess readiness instead.</li>
     * <li> {@code "commit"} - consider operation to be finished when network response is received and the document started loading.</li>
     * </ul>
     */
    public WaitUntilState waitUntil;

    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can
     * be changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public ReloadOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * <ul>
     * <li> {@code "domcontentloaded"} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.</li>
     * <li> {@code "load"} - consider operation to be finished when the {@code load} event is fired.</li>
     * <li> {@code "networkidle"} - **DISCOURAGED** consider operation to be finished when there are no network connections for at
     * least {@code 500} ms. Don't use this method for testing, rely on web assertions to assess readiness instead.</li>
     * <li> {@code "commit"} - consider operation to be finished when network response is received and the document started loading.</li>
     * </ul>
     */
    public ReloadOptions setWaitUntil(WaitUntilState waitUntil) {
      this.waitUntil = waitUntil;
      return this;
    }
  }
  class RouteOptions {
    /**
     * How often a route should be used. By default it will be used every time.
     */
    public Integer times;

    /**
     * How often a route should be used. By default it will be used every time.
     */
    public RouteOptions setTimes(int times) {
      this.times = times;
      return this;
    }
  }
  class RouteFromHAROptions {
    /**
     * <ul>
     * <li> If set to 'abort' any request not found in the HAR file will be aborted.</li>
     * <li> If set to 'fallback' missing requests will be sent to the network.</li>
     * </ul>
     *
     * <p> Defaults to abort.
     */
    public HarNotFound notFound;
    /**
     * If specified, updates the given HAR with the actual network information instead of serving from file. The file is
     * written to disk when {@link BrowserContext#close BrowserContext.close()} is called.
     */
    public Boolean update;
    /**
     * Optional setting to control resource content management. If {@code attach} is specified, resources are persisted as
     * separate files or entries in the ZIP archive. If {@code embed} is specified, content is stored inline the HAR file.
     */
    public RouteFromHarUpdateContentPolicy updateContent;
    /**
     * When set to {@code minimal}, only record information necessary for routing from HAR. This omits sizes, timing, page,
     * cookies, security and other types of HAR information that are not used when replaying from HAR. Defaults to {@code
     * full}.
     */
    public HarMode updateMode;
    /**
     * A glob pattern, regular expression or predicate to match the request URL. Only requests with URL matching the pattern
     * will be served from the HAR file. If not specified, all requests are served from the HAR file.
     */
    public Object url;

    /**
     * <ul>
     * <li> If set to 'abort' any request not found in the HAR file will be aborted.</li>
     * <li> If set to 'fallback' missing requests will be sent to the network.</li>
     * </ul>
     *
     * <p> Defaults to abort.
     */
    public RouteFromHAROptions setNotFound(HarNotFound notFound) {
      this.notFound = notFound;
      return this;
    }
    /**
     * If specified, updates the given HAR with the actual network information instead of serving from file. The file is
     * written to disk when {@link BrowserContext#close BrowserContext.close()} is called.
     */
    public RouteFromHAROptions setUpdate(boolean update) {
      this.update = update;
      return this;
    }
    /**
     * Optional setting to control resource content management. If {@code attach} is specified, resources are persisted as
     * separate files or entries in the ZIP archive. If {@code embed} is specified, content is stored inline the HAR file.
     */
    public RouteFromHAROptions setUpdateContent(RouteFromHarUpdateContentPolicy updateContent) {
      this.updateContent = updateContent;
      return this;
    }
    /**
     * When set to {@code minimal}, only record information necessary for routing from HAR. This omits sizes, timing, page,
     * cookies, security and other types of HAR information that are not used when replaying from HAR. Defaults to {@code
     * full}.
     */
    public RouteFromHAROptions setUpdateMode(HarMode updateMode) {
      this.updateMode = updateMode;
      return this;
    }
    /**
     * A glob pattern, regular expression or predicate to match the request URL. Only requests with URL matching the pattern
     * will be served from the HAR file. If not specified, all requests are served from the HAR file.
     */
    public RouteFromHAROptions setUrl(String url) {
      this.url = url;
      return this;
    }
    /**
     * A glob pattern, regular expression or predicate to match the request URL. Only requests with URL matching the pattern
     * will be served from the HAR file. If not specified, all requests are served from the HAR file.
     */
    public RouteFromHAROptions setUrl(Pattern url) {
      this.url = url;
      return this;
    }
  }
  class ScreenshotOptions {
    /**
     * When set to {@code "disabled"}, stops CSS animations, CSS transitions and Web Animations. Animations get different
     * treatment depending on their duration:
     * <ul>
     * <li> finite animations are fast-forwarded to completion, so they'll fire {@code transitionend} event.</li>
     * <li> infinite animations are canceled to initial state, and then played over after the screenshot.</li>
     * </ul>
     *
     * <p> Defaults to {@code "allow"} that leaves animations untouched.
     */
    public ScreenshotAnimations animations;
    /**
     * When set to {@code "hide"}, screenshot will hide text caret. When set to {@code "initial"}, text caret behavior will not
     * be changed.  Defaults to {@code "hide"}.
     */
    public ScreenshotCaret caret;
    /**
     * An object which specifies clipping of the resulting image.
     */
    public Clip clip;
    /**
     * When true, takes a screenshot of the full scrollable page, instead of the currently visible viewport. Defaults to {@code
     * false}.
     */
    public Boolean fullPage;
    /**
     * Specify locators that should be masked when the screenshot is taken. Masked elements will be overlaid with a pink box
     * {@code #FF00FF} (customized by {@code maskColor}) that completely covers its bounding box.
     */
    public List<Locator> mask;
    /**
     * Specify the color of the overlay box for masked elements, in <a
     * href="https://developer.mozilla.org/en-US/docs/Web/CSS/color_value">CSS color format</a>. Default color is pink {@code
     * #FF00FF}.
     */
    public String maskColor;
    /**
     * Hides default white background and allows capturing screenshots with transparency. Not applicable to {@code jpeg}
     * images. Defaults to {@code false}.
     */
    public Boolean omitBackground;
    /**
     * The file path to save the image to. The screenshot type will be inferred from file extension. If {@code path} is a
     * relative path, then it is resolved relative to the current working directory. If no path is provided, the image won't be
     * saved to the disk.
     */
    public Path path;
    /**
     * The quality of the image, between 0-100. Not applicable to {@code png} images.
     */
    public Integer quality;
    /**
     * When set to {@code "css"}, screenshot will have a single pixel per each css pixel on the page. For high-dpi devices,
     * this will keep screenshots small. Using {@code "device"} option will produce a single pixel per each device pixel, so
     * screenshots of high-dpi devices will be twice as large or even larger.
     *
     * <p> Defaults to {@code "device"}.
     */
    public ScreenshotScale scale;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;
    /**
     * Specify screenshot type, defaults to {@code png}.
     */
    public ScreenshotType type;

    /**
     * When set to {@code "disabled"}, stops CSS animations, CSS transitions and Web Animations. Animations get different
     * treatment depending on their duration:
     * <ul>
     * <li> finite animations are fast-forwarded to completion, so they'll fire {@code transitionend} event.</li>
     * <li> infinite animations are canceled to initial state, and then played over after the screenshot.</li>
     * </ul>
     *
     * <p> Defaults to {@code "allow"} that leaves animations untouched.
     */
    public ScreenshotOptions setAnimations(ScreenshotAnimations animations) {
      this.animations = animations;
      return this;
    }
    /**
     * When set to {@code "hide"}, screenshot will hide text caret. When set to {@code "initial"}, text caret behavior will not
     * be changed.  Defaults to {@code "hide"}.
     */
    public ScreenshotOptions setCaret(ScreenshotCaret caret) {
      this.caret = caret;
      return this;
    }
    /**
     * An object which specifies clipping of the resulting image.
     */
    public ScreenshotOptions setClip(double x, double y, double width, double height) {
      return setClip(new Clip(x, y, width, height));
    }
    /**
     * An object which specifies clipping of the resulting image.
     */
    public ScreenshotOptions setClip(Clip clip) {
      this.clip = clip;
      return this;
    }
    /**
     * When true, takes a screenshot of the full scrollable page, instead of the currently visible viewport. Defaults to {@code
     * false}.
     */
    public ScreenshotOptions setFullPage(boolean fullPage) {
      this.fullPage = fullPage;
      return this;
    }
    /**
     * Specify locators that should be masked when the screenshot is taken. Masked elements will be overlaid with a pink box
     * {@code #FF00FF} (customized by {@code maskColor}) that completely covers its bounding box.
     */
    public ScreenshotOptions setMask(List<Locator> mask) {
      this.mask = mask;
      return this;
    }
    /**
     * Specify the color of the overlay box for masked elements, in <a
     * href="https://developer.mozilla.org/en-US/docs/Web/CSS/color_value">CSS color format</a>. Default color is pink {@code
     * #FF00FF}.
     */
    public ScreenshotOptions setMaskColor(String maskColor) {
      this.maskColor = maskColor;
      return this;
    }
    /**
     * Hides default white background and allows capturing screenshots with transparency. Not applicable to {@code jpeg}
     * images. Defaults to {@code false}.
     */
    public ScreenshotOptions setOmitBackground(boolean omitBackground) {
      this.omitBackground = omitBackground;
      return this;
    }
    /**
     * The file path to save the image to. The screenshot type will be inferred from file extension. If {@code path} is a
     * relative path, then it is resolved relative to the current working directory. If no path is provided, the image won't be
     * saved to the disk.
     */
    public ScreenshotOptions setPath(Path path) {
      this.path = path;
      return this;
    }
    /**
     * The quality of the image, between 0-100. Not applicable to {@code png} images.
     */
    public ScreenshotOptions setQuality(int quality) {
      this.quality = quality;
      return this;
    }
    /**
     * When set to {@code "css"}, screenshot will have a single pixel per each css pixel on the page. For high-dpi devices,
     * this will keep screenshots small. Using {@code "device"} option will produce a single pixel per each device pixel, so
     * screenshots of high-dpi devices will be twice as large or even larger.
     *
     * <p> Defaults to {@code "device"}.
     */
    public ScreenshotOptions setScale(ScreenshotScale scale) {
      this.scale = scale;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public ScreenshotOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * Specify screenshot type, defaults to {@code png}.
     */
    public ScreenshotOptions setType(ScreenshotType type) {
      this.type = type;
      return this;
    }
  }
  class SelectOptionOptions {
    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public Boolean force;
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public Boolean noWaitAfter;
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public SelectOptionOptions setForce(boolean force) {
      this.force = force;
      return this;
    }
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public SelectOptionOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public SelectOptionOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public SelectOptionOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class SetCheckedOptions {
    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public Boolean force;
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;
    /**
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public Boolean trial;

    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public SetCheckedOptions setForce(boolean force) {
      this.force = force;
      return this;
    }
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public SetCheckedOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public SetCheckedOptions setPosition(double x, double y) {
      return setPosition(new Position(x, y));
    }
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public SetCheckedOptions setPosition(Position position) {
      this.position = position;
      return this;
    }
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public SetCheckedOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public SetCheckedOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public SetCheckedOptions setTrial(boolean trial) {
      this.trial = trial;
      return this;
    }
  }
  class SetContentOptions {
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can
     * be changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * <ul>
     * <li> {@code "domcontentloaded"} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.</li>
     * <li> {@code "load"} - consider operation to be finished when the {@code load} event is fired.</li>
     * <li> {@code "networkidle"} - **DISCOURAGED** consider operation to be finished when there are no network connections for at
     * least {@code 500} ms. Don't use this method for testing, rely on web assertions to assess readiness instead.</li>
     * <li> {@code "commit"} - consider operation to be finished when network response is received and the document started loading.</li>
     * </ul>
     */
    public WaitUntilState waitUntil;

    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can
     * be changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public SetContentOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * <ul>
     * <li> {@code "domcontentloaded"} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.</li>
     * <li> {@code "load"} - consider operation to be finished when the {@code load} event is fired.</li>
     * <li> {@code "networkidle"} - **DISCOURAGED** consider operation to be finished when there are no network connections for at
     * least {@code 500} ms. Don't use this method for testing, rely on web assertions to assess readiness instead.</li>
     * <li> {@code "commit"} - consider operation to be finished when network response is received and the document started loading.</li>
     * </ul>
     */
    public SetContentOptions setWaitUntil(WaitUntilState waitUntil) {
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public SetInputFilesOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public SetInputFilesOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public SetInputFilesOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class TapOptions {
    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public Boolean force;
    /**
     * Modifier keys to press. Ensures that only these modifiers are pressed during the operation, and then restores current
     * modifiers back. If not specified, currently pressed modifiers are used.
     */
    public List<KeyboardModifier> modifiers;
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;
    /**
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public Boolean trial;

    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public TapOptions setForce(boolean force) {
      this.force = force;
      return this;
    }
    /**
     * Modifier keys to press. Ensures that only these modifiers are pressed during the operation, and then restores current
     * modifiers back. If not specified, currently pressed modifiers are used.
     */
    public TapOptions setModifiers(List<KeyboardModifier> modifiers) {
      this.modifiers = modifiers;
      return this;
    }
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public TapOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public TapOptions setPosition(double x, double y) {
      return setPosition(new Position(x, y));
    }
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public TapOptions setPosition(Position position) {
      this.position = position;
      return this;
    }
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public TapOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public TapOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public TapOptions setTrial(boolean trial) {
      this.trial = trial;
      return this;
    }
  }
  class TextContentOptions {
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public TextContentOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public TextContentOptions setTimeout(double timeout) {
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Time to wait between key presses in milliseconds. Defaults to 0.
     */
    public TypeOptions setDelay(double delay) {
      this.delay = delay;
      return this;
    }
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public TypeOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public TypeOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public TypeOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class UncheckOptions {
    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public Boolean force;
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
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;
    /**
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public Boolean trial;

    /**
     * Whether to bypass the <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks. Defaults to
     * {@code false}.
     */
    public UncheckOptions setForce(boolean force) {
      this.force = force;
      return this;
    }
    /**
     * Actions that initiate navigations are waiting for these navigations to happen and for pages to start loading. You can
     * opt out of waiting via setting this flag. You would only need this option in the exceptional cases such as navigating to
     * inaccessible pages. Defaults to {@code false}.
     */
    public UncheckOptions setNoWaitAfter(boolean noWaitAfter) {
      this.noWaitAfter = noWaitAfter;
      return this;
    }
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public UncheckOptions setPosition(double x, double y) {
      return setPosition(new Position(x, y));
    }
    /**
     * A point to use relative to the top-left corner of element padding box. If not specified, uses some visible point of the
     * element.
     */
    public UncheckOptions setPosition(Position position) {
      this.position = position;
      return this;
    }
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public UncheckOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public UncheckOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * When set, this method only performs the <a href="https://playwright.dev/java/docs/actionability">actionability</a>
     * checks and skips the action. Defaults to {@code false}. Useful to wait until the element is ready for the action without
     * performing it.
     */
    public UncheckOptions setTrial(boolean trial) {
      this.trial = trial;
      return this;
    }
  }
  class WaitForCloseOptions {
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The
     * default value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    public Double timeout;

    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The
     * default value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    public WaitForCloseOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForConsoleMessageOptions {
    /**
     * Receives the {@code ConsoleMessage} object and resolves to truthy value when the waiting should resolve.
     */
    public Predicate<ConsoleMessage> predicate;
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The
     * default value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    public Double timeout;

    /**
     * Receives the {@code ConsoleMessage} object and resolves to truthy value when the waiting should resolve.
     */
    public WaitForConsoleMessageOptions setPredicate(Predicate<ConsoleMessage> predicate) {
      this.predicate = predicate;
      return this;
    }
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The
     * default value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    public WaitForConsoleMessageOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForDownloadOptions {
    /**
     * Receives the {@code Download} object and resolves to truthy value when the waiting should resolve.
     */
    public Predicate<Download> predicate;
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The
     * default value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    public Double timeout;

    /**
     * Receives the {@code Download} object and resolves to truthy value when the waiting should resolve.
     */
    public WaitForDownloadOptions setPredicate(Predicate<Download> predicate) {
      this.predicate = predicate;
      return this;
    }
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The
     * default value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    public WaitForDownloadOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForFileChooserOptions {
    /**
     * Receives the {@code FileChooser} object and resolves to truthy value when the waiting should resolve.
     */
    public Predicate<FileChooser> predicate;
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The
     * default value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    public Double timeout;

    /**
     * Receives the {@code FileChooser} object and resolves to truthy value when the waiting should resolve.
     */
    public WaitForFileChooserOptions setPredicate(Predicate<FileChooser> predicate) {
      this.predicate = predicate;
      return this;
    }
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The
     * default value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    public WaitForFileChooserOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForFunctionOptions {
    /**
     * If specified, then it is treated as an interval in milliseconds at which the function would be executed. By default if
     * the option is not specified {@code expression} is executed in {@code requestAnimationFrame} callback.
     */
    public Double pollingInterval;
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The
     * default value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or
     * {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * If specified, then it is treated as an interval in milliseconds at which the function would be executed. By default if
     * the option is not specified {@code expression} is executed in {@code requestAnimationFrame} callback.
     */
    public WaitForFunctionOptions setPollingInterval(double pollingInterval) {
      this.pollingInterval = pollingInterval;
      return this;
    }
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The
     * default value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or
     * {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public WaitForFunctionOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForLoadStateOptions {
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can
     * be changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can
     * be changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public WaitForLoadStateOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForNavigationOptions {
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can
     * be changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;
    /**
     * A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation. Note that if the
     * parameter is a string without wildcard characters, the method will wait for navigation to URL that is exactly equal to
     * the string.
     */
    public Object url;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * <ul>
     * <li> {@code "domcontentloaded"} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.</li>
     * <li> {@code "load"} - consider operation to be finished when the {@code load} event is fired.</li>
     * <li> {@code "networkidle"} - **DISCOURAGED** consider operation to be finished when there are no network connections for at
     * least {@code 500} ms. Don't use this method for testing, rely on web assertions to assess readiness instead.</li>
     * <li> {@code "commit"} - consider operation to be finished when network response is received and the document started loading.</li>
     * </ul>
     */
    public WaitUntilState waitUntil;

    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can
     * be changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public WaitForNavigationOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation. Note that if the
     * parameter is a string without wildcard characters, the method will wait for navigation to URL that is exactly equal to
     * the string.
     */
    public WaitForNavigationOptions setUrl(String url) {
      this.url = url;
      return this;
    }
    /**
     * A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation. Note that if the
     * parameter is a string without wildcard characters, the method will wait for navigation to URL that is exactly equal to
     * the string.
     */
    public WaitForNavigationOptions setUrl(Pattern url) {
      this.url = url;
      return this;
    }
    /**
     * A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation. Note that if the
     * parameter is a string without wildcard characters, the method will wait for navigation to URL that is exactly equal to
     * the string.
     */
    public WaitForNavigationOptions setUrl(Predicate<String> url) {
      this.url = url;
      return this;
    }
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * <ul>
     * <li> {@code "domcontentloaded"} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.</li>
     * <li> {@code "load"} - consider operation to be finished when the {@code load} event is fired.</li>
     * <li> {@code "networkidle"} - **DISCOURAGED** consider operation to be finished when there are no network connections for at
     * least {@code 500} ms. Don't use this method for testing, rely on web assertions to assess readiness instead.</li>
     * <li> {@code "commit"} - consider operation to be finished when network response is received and the document started loading.</li>
     * </ul>
     */
    public WaitForNavigationOptions setWaitUntil(WaitUntilState waitUntil) {
      this.waitUntil = waitUntil;
      return this;
    }
  }
  class WaitForPopupOptions {
    /**
     * Receives the {@code Page} object and resolves to truthy value when the waiting should resolve.
     */
    public Predicate<Page> predicate;
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The
     * default value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    public Double timeout;

    /**
     * Receives the {@code Page} object and resolves to truthy value when the waiting should resolve.
     */
    public WaitForPopupOptions setPredicate(Predicate<Page> predicate) {
      this.predicate = predicate;
      return this;
    }
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The
     * default value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    public WaitForPopupOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForRequestOptions {
    /**
     * Maximum wait time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable the timeout. The default value can
     * be changed by using the {@link Page#setDefaultTimeout Page.setDefaultTimeout()} method.
     */
    public Double timeout;

    /**
     * Maximum wait time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable the timeout. The default value can
     * be changed by using the {@link Page#setDefaultTimeout Page.setDefaultTimeout()} method.
     */
    public WaitForRequestOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForRequestFinishedOptions {
    /**
     * Receives the {@code Request} object and resolves to truthy value when the waiting should resolve.
     */
    public Predicate<Request> predicate;
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The
     * default value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    public Double timeout;

    /**
     * Receives the {@code Request} object and resolves to truthy value when the waiting should resolve.
     */
    public WaitForRequestFinishedOptions setPredicate(Predicate<Request> predicate) {
      this.predicate = predicate;
      return this;
    }
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The
     * default value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    public WaitForRequestFinishedOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForResponseOptions {
    /**
     * Maximum wait time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable the timeout. The default value can
     * be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Maximum wait time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable the timeout. The default value can
     * be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public WaitForResponseOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForSelectorOptions {
    /**
     * Defaults to {@code "visible"}. Can be either:
     * <ul>
     * <li> {@code "attached"} - wait for element to be present in DOM.</li>
     * <li> {@code "detached"} - wait for element to not be present in DOM.</li>
     * <li> {@code "visible"} - wait for element to have non-empty bounding box and no {@code visibility:hidden}. Note that element
     * without any content or with {@code display:none} has an empty bounding box and is not considered visible.</li>
     * <li> {@code "hidden"} - wait for element to be either detached from DOM, or have an empty bounding box or {@code
     * visibility:hidden}. This is opposite to the {@code "visible"} option.</li>
     * </ul>
     */
    public WaitForSelectorState state;
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public Boolean strict;
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Defaults to {@code "visible"}. Can be either:
     * <ul>
     * <li> {@code "attached"} - wait for element to be present in DOM.</li>
     * <li> {@code "detached"} - wait for element to not be present in DOM.</li>
     * <li> {@code "visible"} - wait for element to have non-empty bounding box and no {@code visibility:hidden}. Note that element
     * without any content or with {@code display:none} has an empty bounding box and is not considered visible.</li>
     * <li> {@code "hidden"} - wait for element to be either detached from DOM, or have an empty bounding box or {@code
     * visibility:hidden}. This is opposite to the {@code "visible"} option.</li>
     * </ul>
     */
    public WaitForSelectorOptions setState(WaitForSelectorState state) {
      this.state = state;
      return this;
    }
    /**
     * When true, the call requires selector to resolve to a single element. If given selector resolves to more than one
     * element, the call throws an exception.
     */
    public WaitForSelectorOptions setStrict(boolean strict) {
      this.strict = strict;
      return this;
    }
    /**
     * Maximum time in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or {@link
     * Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public WaitForSelectorOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForConditionOptions {
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The
     * default value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or
     * {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;

    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The
     * default value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()} or
     * {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public WaitForConditionOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForURLOptions {
    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can
     * be changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public Double timeout;
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * <ul>
     * <li> {@code "domcontentloaded"} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.</li>
     * <li> {@code "load"} - consider operation to be finished when the {@code load} event is fired.</li>
     * <li> {@code "networkidle"} - **DISCOURAGED** consider operation to be finished when there are no network connections for at
     * least {@code 500} ms. Don't use this method for testing, rely on web assertions to assess readiness instead.</li>
     * <li> {@code "commit"} - consider operation to be finished when network response is received and the document started loading.</li>
     * </ul>
     */
    public WaitUntilState waitUntil;

    /**
     * Maximum operation time in milliseconds, defaults to 30 seconds, pass {@code 0} to disable timeout. The default value can
     * be changed by using the {@link BrowserContext#setDefaultNavigationTimeout BrowserContext.setDefaultNavigationTimeout()},
     * {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}, {@link Page#setDefaultNavigationTimeout
     * Page.setDefaultNavigationTimeout()} or {@link Page#setDefaultTimeout Page.setDefaultTimeout()} methods.
     */
    public WaitForURLOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * When to consider operation succeeded, defaults to {@code load}. Events can be either:
     * <ul>
     * <li> {@code "domcontentloaded"} - consider operation to be finished when the {@code DOMContentLoaded} event is fired.</li>
     * <li> {@code "load"} - consider operation to be finished when the {@code load} event is fired.</li>
     * <li> {@code "networkidle"} - **DISCOURAGED** consider operation to be finished when there are no network connections for at
     * least {@code 500} ms. Don't use this method for testing, rely on web assertions to assess readiness instead.</li>
     * <li> {@code "commit"} - consider operation to be finished when network response is received and the document started loading.</li>
     * </ul>
     */
    public WaitForURLOptions setWaitUntil(WaitUntilState waitUntil) {
      this.waitUntil = waitUntil;
      return this;
    }
  }
  class WaitForWebSocketOptions {
    /**
     * Receives the {@code WebSocket} object and resolves to truthy value when the waiting should resolve.
     */
    public Predicate<WebSocket> predicate;
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The
     * default value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    public Double timeout;

    /**
     * Receives the {@code WebSocket} object and resolves to truthy value when the waiting should resolve.
     */
    public WaitForWebSocketOptions setPredicate(Predicate<WebSocket> predicate) {
      this.predicate = predicate;
      return this;
    }
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The
     * default value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    public WaitForWebSocketOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class WaitForWorkerOptions {
    /**
     * Receives the {@code Worker} object and resolves to truthy value when the waiting should resolve.
     */
    public Predicate<Worker> predicate;
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The
     * default value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    public Double timeout;

    /**
     * Receives the {@code Worker} object and resolves to truthy value when the waiting should resolve.
     */
    public WaitForWorkerOptions setPredicate(Predicate<Worker> predicate) {
      this.predicate = predicate;
      return this;
    }
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The
     * default value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    public WaitForWorkerOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  /**
   * Adds a script which would be evaluated in one of the following scenarios:
   * <ul>
   * <li> Whenever the page is navigated.</li>
   * <li> Whenever the child frame is attached or navigated. In this case, the script is evaluated in the context of the newly
   * attached frame.</li>
   * </ul>
   *
   * <p> The script is evaluated after the document was created but before any of its scripts were run. This is useful to amend
   * the JavaScript environment, e.g. to seed {@code Math.random}.
   *
   * <p> **Usage**
   *
   * <p> An example of overriding {@code Math.random} before the page loads:
   * <pre>{@code
   * // In your playwright script, assuming the preload.js file is in same directory
   * page.addInitScript(Paths.get("./preload.js"));
   * }</pre>
   *
   * <p> <strong>NOTE:</strong> The order of evaluation of multiple scripts installed via {@link BrowserContext#addInitScript
   * BrowserContext.addInitScript()} and {@link Page#addInitScript Page.addInitScript()} is not defined.
   *
   * @param script Script to be evaluated in all pages in the browser context.
   * @since v1.8
   */
  void addInitScript(String script);
  /**
   * Adds a script which would be evaluated in one of the following scenarios:
   * <ul>
   * <li> Whenever the page is navigated.</li>
   * <li> Whenever the child frame is attached or navigated. In this case, the script is evaluated in the context of the newly
   * attached frame.</li>
   * </ul>
   *
   * <p> The script is evaluated after the document was created but before any of its scripts were run. This is useful to amend
   * the JavaScript environment, e.g. to seed {@code Math.random}.
   *
   * <p> **Usage**
   *
   * <p> An example of overriding {@code Math.random} before the page loads:
   * <pre>{@code
   * // In your playwright script, assuming the preload.js file is in same directory
   * page.addInitScript(Paths.get("./preload.js"));
   * }</pre>
   *
   * <p> <strong>NOTE:</strong> The order of evaluation of multiple scripts installed via {@link BrowserContext#addInitScript
   * BrowserContext.addInitScript()} and {@link Page#addInitScript Page.addInitScript()} is not defined.
   *
   * @param script Script to be evaluated in all pages in the browser context.
   * @since v1.8
   */
  void addInitScript(Path script);
  /**
   * Adds a {@code <script>} tag into the page with the desired url or content. Returns the added tag when the script's
   * onload fires or when the script content was injected into frame.
   *
   * @since v1.8
   */
  default ElementHandle addScriptTag() {
    return addScriptTag(null);
  }
  /**
   * Adds a {@code <script>} tag into the page with the desired url or content. Returns the added tag when the script's
   * onload fires or when the script content was injected into frame.
   *
   * @since v1.8
   */
  ElementHandle addScriptTag(AddScriptTagOptions options);
  /**
   * Adds a {@code <link rel="stylesheet">} tag into the page with the desired url or a {@code <style type="text/css">} tag
   * with the content. Returns the added tag when the stylesheet's onload fires or when the CSS content was injected into
   * frame.
   *
   * @since v1.8
   */
  default ElementHandle addStyleTag() {
    return addStyleTag(null);
  }
  /**
   * Adds a {@code <link rel="stylesheet">} tag into the page with the desired url or a {@code <style type="text/css">} tag
   * with the content. Returns the added tag when the stylesheet's onload fires or when the CSS content was injected into
   * frame.
   *
   * @since v1.8
   */
  ElementHandle addStyleTag(AddStyleTagOptions options);
  /**
   * Brings page to front (activates tab).
   *
   * @since v1.8
   */
  void bringToFront();
  /**
   * This method checks an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Ensure that matched element is a checkbox or a radio input. If not, this method throws. If the element is already
   * checked, this method returns immediately.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * <li> Ensure that the element is now checked. If not, this method throws.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default void check(String selector) {
    check(selector, null);
  }
  /**
   * This method checks an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Ensure that matched element is a checkbox or a radio input. If not, this method throws. If the element is already
   * checked, this method returns immediately.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * <li> Ensure that the element is now checked. If not, this method throws.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  void check(String selector, CheckOptions options);
  /**
   * This method clicks an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default void click(String selector) {
    click(selector, null);
  }
  /**
   * This method clicks an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  void click(String selector, ClickOptions options);
  /**
   * If {@code runBeforeUnload} is {@code false}, does not run any unload handlers and waits for the page to be closed. If
   * {@code runBeforeUnload} is {@code true} the method will run unload handlers, but will **not** wait for the page to
   * close.
   *
   * <p> By default, {@code page.close()} **does not** run {@code beforeunload} handlers.
   *
   * <p> <strong>NOTE:</strong> if {@code runBeforeUnload} is passed as true, a {@code beforeunload} dialog might be summoned and should be handled
   * manually via {@link Page#onDialog Page.onDialog()} event.
   *
   * @since v1.8
   */
  default void close() {
    close(null);
  }
  /**
   * If {@code runBeforeUnload} is {@code false}, does not run any unload handlers and waits for the page to be closed. If
   * {@code runBeforeUnload} is {@code true} the method will run unload handlers, but will **not** wait for the page to
   * close.
   *
   * <p> By default, {@code page.close()} **does not** run {@code beforeunload} handlers.
   *
   * <p> <strong>NOTE:</strong> if {@code runBeforeUnload} is passed as true, a {@code beforeunload} dialog might be summoned and should be handled
   * manually via {@link Page#onDialog Page.onDialog()} event.
   *
   * @since v1.8
   */
  void close(CloseOptions options);
  /**
   * Gets the full HTML contents of the page, including the doctype.
   *
   * @since v1.8
   */
  String content();
  /**
   * Get the browser context that the page belongs to.
   *
   * @since v1.8
   */
  BrowserContext context();
  /**
   * This method double clicks an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to double click in the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set. Note that if the
   * first click of the {@code dblclick()} triggers a navigation event, this method will throw.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * <p> <strong>NOTE:</strong> {@code page.dblclick()} dispatches two {@code click} events and a single {@code dblclick} event.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default void dblclick(String selector) {
    dblclick(selector, null);
  }
  /**
   * This method double clicks an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to double click in the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set. Note that if the
   * first click of the {@code dblclick()} triggers a navigation event, this method will throw.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * <p> <strong>NOTE:</strong> {@code page.dblclick()} dispatches two {@code click} events and a single {@code dblclick} event.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  void dblclick(String selector, DblclickOptions options);
  /**
   * The snippet below dispatches the {@code click} event on the element. Regardless of the visibility state of the element,
   * {@code click} is dispatched. This is equivalent to calling <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/click">element.click()</a>.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.dispatchEvent("button#submit", "click");
   * }</pre>
   *
   * <p> Under the hood, it creates an instance of an event based on the given {@code type}, initializes it with {@code
   * eventInit} properties and dispatches it on the element. Events are {@code composed}, {@code cancelable} and bubble by
   * default.
   *
   * <p> Since {@code eventInit} is event-specific, please refer to the events documentation for the lists of initial properties:
   * <ul>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/DragEvent/DragEvent">DragEvent</a></li>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/FocusEvent/FocusEvent">FocusEvent</a></li>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/KeyboardEvent">KeyboardEvent</a></li>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/MouseEvent/MouseEvent">MouseEvent</a></li>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/PointerEvent/PointerEvent">PointerEvent</a></li>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/TouchEvent/TouchEvent">TouchEvent</a></li>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/Event/Event">Event</a></li>
   * </ul>
   *
   * <p> You can also specify {@code JSHandle} as the property value if you want live objects to be passed into the event:
   * <pre>{@code
   * // Note you can only create DataTransfer in Chromium and Firefox
   * JSHandle dataTransfer = page.evaluateHandle("() => new DataTransfer()");
   * Map<String, Object> arg = new HashMap<>();
   * arg.put("dataTransfer", dataTransfer);
   * page.dispatchEvent("#source", "dragstart", arg);
   * }</pre>
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
   * @param eventInit Optional event-specific initialization properties.
   * @since v1.8
   */
  default void dispatchEvent(String selector, String type, Object eventInit) {
    dispatchEvent(selector, type, eventInit, null);
  }
  /**
   * The snippet below dispatches the {@code click} event on the element. Regardless of the visibility state of the element,
   * {@code click} is dispatched. This is equivalent to calling <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/click">element.click()</a>.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.dispatchEvent("button#submit", "click");
   * }</pre>
   *
   * <p> Under the hood, it creates an instance of an event based on the given {@code type}, initializes it with {@code
   * eventInit} properties and dispatches it on the element. Events are {@code composed}, {@code cancelable} and bubble by
   * default.
   *
   * <p> Since {@code eventInit} is event-specific, please refer to the events documentation for the lists of initial properties:
   * <ul>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/DragEvent/DragEvent">DragEvent</a></li>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/FocusEvent/FocusEvent">FocusEvent</a></li>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/KeyboardEvent">KeyboardEvent</a></li>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/MouseEvent/MouseEvent">MouseEvent</a></li>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/PointerEvent/PointerEvent">PointerEvent</a></li>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/TouchEvent/TouchEvent">TouchEvent</a></li>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/Event/Event">Event</a></li>
   * </ul>
   *
   * <p> You can also specify {@code JSHandle} as the property value if you want live objects to be passed into the event:
   * <pre>{@code
   * // Note you can only create DataTransfer in Chromium and Firefox
   * JSHandle dataTransfer = page.evaluateHandle("() => new DataTransfer()");
   * Map<String, Object> arg = new HashMap<>();
   * arg.put("dataTransfer", dataTransfer);
   * page.dispatchEvent("#source", "dragstart", arg);
   * }</pre>
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
   * @since v1.8
   */
  default void dispatchEvent(String selector, String type) {
    dispatchEvent(selector, type, null);
  }
  /**
   * The snippet below dispatches the {@code click} event on the element. Regardless of the visibility state of the element,
   * {@code click} is dispatched. This is equivalent to calling <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLElement/click">element.click()</a>.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.dispatchEvent("button#submit", "click");
   * }</pre>
   *
   * <p> Under the hood, it creates an instance of an event based on the given {@code type}, initializes it with {@code
   * eventInit} properties and dispatches it on the element. Events are {@code composed}, {@code cancelable} and bubble by
   * default.
   *
   * <p> Since {@code eventInit} is event-specific, please refer to the events documentation for the lists of initial properties:
   * <ul>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/DragEvent/DragEvent">DragEvent</a></li>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/FocusEvent/FocusEvent">FocusEvent</a></li>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/KeyboardEvent">KeyboardEvent</a></li>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/MouseEvent/MouseEvent">MouseEvent</a></li>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/PointerEvent/PointerEvent">PointerEvent</a></li>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/TouchEvent/TouchEvent">TouchEvent</a></li>
   * <li> <a href="https://developer.mozilla.org/en-US/docs/Web/API/Event/Event">Event</a></li>
   * </ul>
   *
   * <p> You can also specify {@code JSHandle} as the property value if you want live objects to be passed into the event:
   * <pre>{@code
   * // Note you can only create DataTransfer in Chromium and Firefox
   * JSHandle dataTransfer = page.evaluateHandle("() => new DataTransfer()");
   * Map<String, Object> arg = new HashMap<>();
   * arg.put("dataTransfer", dataTransfer);
   * page.dispatchEvent("#source", "dragstart", arg);
   * }</pre>
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param type DOM event type: {@code "click"}, {@code "dragstart"}, etc.
   * @param eventInit Optional event-specific initialization properties.
   * @since v1.8
   */
  void dispatchEvent(String selector, String type, Object eventInit, DispatchEventOptions options);
  /**
   * This method drags the source element to the target element. It will first move to the source element, perform a {@code
   * mousedown}, then move to the target element and perform a {@code mouseup}.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.dragAndDrop("#source", '#target');
   * // or specify exact positions relative to the top-left corners of the elements:
   * page.dragAndDrop("#source", '#target', new Page.DragAndDropOptions()
   *   .setSourcePosition(34, 7).setTargetPosition(10, 20));
   * }</pre>
   *
   * @param source A selector to search for an element to drag. If there are multiple elements satisfying the selector, the first will be
   * used.
   * @param target A selector to search for an element to drop onto. If there are multiple elements satisfying the selector, the first will
   * be used.
   * @since v1.13
   */
  default void dragAndDrop(String source, String target) {
    dragAndDrop(source, target, null);
  }
  /**
   * This method drags the source element to the target element. It will first move to the source element, perform a {@code
   * mousedown}, then move to the target element and perform a {@code mouseup}.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.dragAndDrop("#source", '#target');
   * // or specify exact positions relative to the top-left corners of the elements:
   * page.dragAndDrop("#source", '#target', new Page.DragAndDropOptions()
   *   .setSourcePosition(34, 7).setTargetPosition(10, 20));
   * }</pre>
   *
   * @param source A selector to search for an element to drag. If there are multiple elements satisfying the selector, the first will be
   * used.
   * @param target A selector to search for an element to drop onto. If there are multiple elements satisfying the selector, the first will
   * be used.
   * @since v1.13
   */
  void dragAndDrop(String source, String target, DragAndDropOptions options);
  /**
   * This method changes the {@code CSS media type} through the {@code media} argument, and/or the {@code
   * "prefers-colors-scheme"} media feature, using the {@code colorScheme} argument.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.evaluate("() => matchMedia('screen').matches");
   * // → true
   * page.evaluate("() => matchMedia('print').matches");
   * // → false
   *
   * page.emulateMedia(new Page.EmulateMediaOptions().setMedia(Media.PRINT));
   * page.evaluate("() => matchMedia('screen').matches");
   * // → false
   * page.evaluate("() => matchMedia('print').matches");
   * // → true
   *
   * page.emulateMedia(new Page.EmulateMediaOptions());
   * page.evaluate("() => matchMedia('screen').matches");
   * // → true
   * page.evaluate("() => matchMedia('print').matches");
   * // → false
   * }</pre>
   * <pre>{@code
   * page.emulateMedia(new Page.EmulateMediaOptions().setColorScheme(ColorScheme.DARK));
   * page.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches");
   * // → true
   * page.evaluate("() => matchMedia('(prefers-color-scheme: light)').matches");
   * // → false
   * page.evaluate("() => matchMedia('(prefers-color-scheme: no-preference)').matches");
   * // → false
   * }</pre>
   *
   * @since v1.8
   */
  default void emulateMedia() {
    emulateMedia(null);
  }
  /**
   * This method changes the {@code CSS media type} through the {@code media} argument, and/or the {@code
   * "prefers-colors-scheme"} media feature, using the {@code colorScheme} argument.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.evaluate("() => matchMedia('screen').matches");
   * // → true
   * page.evaluate("() => matchMedia('print').matches");
   * // → false
   *
   * page.emulateMedia(new Page.EmulateMediaOptions().setMedia(Media.PRINT));
   * page.evaluate("() => matchMedia('screen').matches");
   * // → false
   * page.evaluate("() => matchMedia('print').matches");
   * // → true
   *
   * page.emulateMedia(new Page.EmulateMediaOptions());
   * page.evaluate("() => matchMedia('screen').matches");
   * // → true
   * page.evaluate("() => matchMedia('print').matches");
   * // → false
   * }</pre>
   * <pre>{@code
   * page.emulateMedia(new Page.EmulateMediaOptions().setColorScheme(ColorScheme.DARK));
   * page.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches");
   * // → true
   * page.evaluate("() => matchMedia('(prefers-color-scheme: light)').matches");
   * // → false
   * page.evaluate("() => matchMedia('(prefers-color-scheme: no-preference)').matches");
   * // → false
   * }</pre>
   *
   * @since v1.8
   */
  void emulateMedia(EmulateMediaOptions options);
  /**
   * The method finds an element matching the specified selector within the page and passes it as a first argument to {@code
   * expression}. If no elements match the selector, the method throws an error. Returns the value of {@code expression}.
   *
   * <p> If {@code expression} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * Page#evalOnSelector Page.evalOnSelector()} would wait for the promise to resolve and return its value.
   *
   * <p> **Usage**
   * <pre>{@code
   * String searchValue = (String) page.evalOnSelector("#search", "el => el.value");
   * String preloadHref = (String) page.evalOnSelector("link[rel=preload]", "el => el.href");
   * String html = (String) page.evalOnSelector(".main-container", "(e, suffix) => e.outerHTML + suffix", "hello");
   * }</pre>
   *
   * @param selector A selector to query for.
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @param arg Optional argument to pass to {@code expression}.
   * @since v1.9
   */
  default Object evalOnSelector(String selector, String expression, Object arg) {
    return evalOnSelector(selector, expression, arg, null);
  }
  /**
   * The method finds an element matching the specified selector within the page and passes it as a first argument to {@code
   * expression}. If no elements match the selector, the method throws an error. Returns the value of {@code expression}.
   *
   * <p> If {@code expression} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * Page#evalOnSelector Page.evalOnSelector()} would wait for the promise to resolve and return its value.
   *
   * <p> **Usage**
   * <pre>{@code
   * String searchValue = (String) page.evalOnSelector("#search", "el => el.value");
   * String preloadHref = (String) page.evalOnSelector("link[rel=preload]", "el => el.href");
   * String html = (String) page.evalOnSelector(".main-container", "(e, suffix) => e.outerHTML + suffix", "hello");
   * }</pre>
   *
   * @param selector A selector to query for.
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @since v1.9
   */
  default Object evalOnSelector(String selector, String expression) {
    return evalOnSelector(selector, expression, null);
  }
  /**
   * The method finds an element matching the specified selector within the page and passes it as a first argument to {@code
   * expression}. If no elements match the selector, the method throws an error. Returns the value of {@code expression}.
   *
   * <p> If {@code expression} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * Page#evalOnSelector Page.evalOnSelector()} would wait for the promise to resolve and return its value.
   *
   * <p> **Usage**
   * <pre>{@code
   * String searchValue = (String) page.evalOnSelector("#search", "el => el.value");
   * String preloadHref = (String) page.evalOnSelector("link[rel=preload]", "el => el.href");
   * String html = (String) page.evalOnSelector(".main-container", "(e, suffix) => e.outerHTML + suffix", "hello");
   * }</pre>
   *
   * @param selector A selector to query for.
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @param arg Optional argument to pass to {@code expression}.
   * @since v1.9
   */
  Object evalOnSelector(String selector, String expression, Object arg, EvalOnSelectorOptions options);
  /**
   * The method finds all elements matching the specified selector within the page and passes an array of matched elements as
   * a first argument to {@code expression}. Returns the result of {@code expression} invocation.
   *
   * <p> If {@code expression} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * Page#evalOnSelectorAll Page.evalOnSelectorAll()} would wait for the promise to resolve and return its value.
   *
   * <p> **Usage**
   * <pre>{@code
   * boolean divCounts = (boolean) page.evalOnSelectorAll("div", "(divs, min) => divs.length >= min", 10);
   * }</pre>
   *
   * @param selector A selector to query for.
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @since v1.9
   */
  default Object evalOnSelectorAll(String selector, String expression) {
    return evalOnSelectorAll(selector, expression, null);
  }
  /**
   * The method finds all elements matching the specified selector within the page and passes an array of matched elements as
   * a first argument to {@code expression}. Returns the result of {@code expression} invocation.
   *
   * <p> If {@code expression} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * Page#evalOnSelectorAll Page.evalOnSelectorAll()} would wait for the promise to resolve and return its value.
   *
   * <p> **Usage**
   * <pre>{@code
   * boolean divCounts = (boolean) page.evalOnSelectorAll("div", "(divs, min) => divs.length >= min", 10);
   * }</pre>
   *
   * @param selector A selector to query for.
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @param arg Optional argument to pass to {@code expression}.
   * @since v1.9
   */
  Object evalOnSelectorAll(String selector, String expression, Object arg);
  /**
   * Returns the value of the {@code expression} invocation.
   *
   * <p> If the function passed to the {@link Page#evaluate Page.evaluate()} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * Page#evaluate Page.evaluate()} would wait for the promise to resolve and return its value.
   *
   * <p> If the function passed to the {@link Page#evaluate Page.evaluate()} returns a non-[Serializable] value, then {@link
   * Page#evaluate Page.evaluate()} resolves to {@code undefined}. Playwright also supports transferring some additional
   * values that are not serializable by {@code JSON}: {@code -0}, {@code NaN}, {@code Infinity}, {@code -Infinity}.
   *
   * <p> **Usage**
   *
   * <p> Passing argument to {@code expression}:
   * <pre>{@code
   * Object result = page.evaluate("([x, y]) => {\n" +
   *   "  return Promise.resolve(x * y);\n" +
   *   "}", Arrays.asList(7, 8));
   * System.out.println(result); // prints "56"
   * }</pre>
   *
   * <p> A string can also be passed in instead of a function:
   * <pre>{@code
   * System.out.println(page.evaluate("1 + 2")); // prints "3"
   * }</pre>
   *
   * <p> {@code ElementHandle} instances can be passed as an argument to the {@link Page#evaluate Page.evaluate()}:
   * <pre>{@code
   * ElementHandle bodyHandle = page.evaluate("document.body");
   * String html = (String) page.evaluate("([body, suffix]) => body.innerHTML + suffix", Arrays.asList(bodyHandle, "hello"));
   * bodyHandle.dispose();
   * }</pre>
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @since v1.8
   */
  default Object evaluate(String expression) {
    return evaluate(expression, null);
  }
  /**
   * Returns the value of the {@code expression} invocation.
   *
   * <p> If the function passed to the {@link Page#evaluate Page.evaluate()} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * Page#evaluate Page.evaluate()} would wait for the promise to resolve and return its value.
   *
   * <p> If the function passed to the {@link Page#evaluate Page.evaluate()} returns a non-[Serializable] value, then {@link
   * Page#evaluate Page.evaluate()} resolves to {@code undefined}. Playwright also supports transferring some additional
   * values that are not serializable by {@code JSON}: {@code -0}, {@code NaN}, {@code Infinity}, {@code -Infinity}.
   *
   * <p> **Usage**
   *
   * <p> Passing argument to {@code expression}:
   * <pre>{@code
   * Object result = page.evaluate("([x, y]) => {\n" +
   *   "  return Promise.resolve(x * y);\n" +
   *   "}", Arrays.asList(7, 8));
   * System.out.println(result); // prints "56"
   * }</pre>
   *
   * <p> A string can also be passed in instead of a function:
   * <pre>{@code
   * System.out.println(page.evaluate("1 + 2")); // prints "3"
   * }</pre>
   *
   * <p> {@code ElementHandle} instances can be passed as an argument to the {@link Page#evaluate Page.evaluate()}:
   * <pre>{@code
   * ElementHandle bodyHandle = page.evaluate("document.body");
   * String html = (String) page.evaluate("([body, suffix]) => body.innerHTML + suffix", Arrays.asList(bodyHandle, "hello"));
   * bodyHandle.dispose();
   * }</pre>
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @param arg Optional argument to pass to {@code expression}.
   * @since v1.8
   */
  Object evaluate(String expression, Object arg);
  /**
   * Returns the value of the {@code expression} invocation as a {@code JSHandle}.
   *
   * <p> The only difference between {@link Page#evaluate Page.evaluate()} and {@link Page#evaluateHandle Page.evaluateHandle()}
   * is that {@link Page#evaluateHandle Page.evaluateHandle()} returns {@code JSHandle}.
   *
   * <p> If the function passed to the {@link Page#evaluateHandle Page.evaluateHandle()} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * Page#evaluateHandle Page.evaluateHandle()} would wait for the promise to resolve and return its value.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Handle for the window object.
   * JSHandle aWindowHandle = page.evaluateHandle("() => Promise.resolve(window)");
   * }</pre>
   *
   * <p> A string can also be passed in instead of a function:
   * <pre>{@code
   * JSHandle aHandle = page.evaluateHandle("document"); // Handle for the "document".
   * }</pre>
   *
   * <p> {@code JSHandle} instances can be passed as an argument to the {@link Page#evaluateHandle Page.evaluateHandle()}:
   * <pre>{@code
   * JSHandle aHandle = page.evaluateHandle("() => document.body");
   * JSHandle resultHandle = page.evaluateHandle("([body, suffix]) => body.innerHTML + suffix", Arrays.asList(aHandle, "hello"));
   * System.out.println(resultHandle.jsonValue());
   * resultHandle.dispose();
   * }</pre>
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @since v1.8
   */
  default JSHandle evaluateHandle(String expression) {
    return evaluateHandle(expression, null);
  }
  /**
   * Returns the value of the {@code expression} invocation as a {@code JSHandle}.
   *
   * <p> The only difference between {@link Page#evaluate Page.evaluate()} and {@link Page#evaluateHandle Page.evaluateHandle()}
   * is that {@link Page#evaluateHandle Page.evaluateHandle()} returns {@code JSHandle}.
   *
   * <p> If the function passed to the {@link Page#evaluateHandle Page.evaluateHandle()} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, then {@link
   * Page#evaluateHandle Page.evaluateHandle()} would wait for the promise to resolve and return its value.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Handle for the window object.
   * JSHandle aWindowHandle = page.evaluateHandle("() => Promise.resolve(window)");
   * }</pre>
   *
   * <p> A string can also be passed in instead of a function:
   * <pre>{@code
   * JSHandle aHandle = page.evaluateHandle("document"); // Handle for the "document".
   * }</pre>
   *
   * <p> {@code JSHandle} instances can be passed as an argument to the {@link Page#evaluateHandle Page.evaluateHandle()}:
   * <pre>{@code
   * JSHandle aHandle = page.evaluateHandle("() => document.body");
   * JSHandle resultHandle = page.evaluateHandle("([body, suffix]) => body.innerHTML + suffix", Arrays.asList(aHandle, "hello"));
   * System.out.println(resultHandle.jsonValue());
   * resultHandle.dispose();
   * }</pre>
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @param arg Optional argument to pass to {@code expression}.
   * @since v1.8
   */
  JSHandle evaluateHandle(String expression, Object arg);
  /**
   * The method adds a function called {@code name} on the {@code window} object of every frame in this page. When called,
   * the function executes {@code callback} and returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a> which
   * resolves to the return value of {@code callback}. If the {@code callback} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, it will be
   * awaited.
   *
   * <p> The first argument of the {@code callback} function contains information about the caller: {@code { browserContext:
   * BrowserContext, page: Page, frame: Frame }}.
   *
   * <p> See {@link BrowserContext#exposeBinding BrowserContext.exposeBinding()} for the context-wide version.
   *
   * <p> <strong>NOTE:</strong> Functions installed via {@link Page#exposeBinding Page.exposeBinding()} survive navigations.
   *
   * <p> **Usage**
   *
   * <p> An example of exposing page URL to all frames in a page:
   * <pre>{@code
   * import com.microsoft.playwright.*;
   *
   * public class Example {
   *   public static void main(String[] args) {
   *     try (Playwright playwright = Playwright.create()) {
   *       BrowserType webkit = playwright.webkit();
   *       Browser browser = webkit.launch({ headless: false });
   *       BrowserContext context = browser.newContext();
   *       Page page = context.newPage();
   *       page.exposeBinding("pageURL", (source, args) -> source.page().url());
   *       page.setContent("<script>\n" +
   *         "  async function onClick() {\n" +
   *         "    document.querySelector('div').textContent = await window.pageURL();\n" +
   *         "  }\n" +
   *         "</script>\n" +
   *         "<button onclick=\"onClick()\">Click me</button>\n" +
   *         "<div></div>");
   *       page.click("button");
   *     }
   *   }
   * }
   * }</pre>
   *
   * <p> An example of passing an element handle:
   * <pre>{@code
   * page.exposeBinding("clicked", (source, args) -> {
   *   ElementHandle element = (ElementHandle) args[0];
   *   System.out.println(element.textContent());
   *   return null;
   * }, new Page.ExposeBindingOptions().setHandle(true));
   * page.setContent("" +
   *   "<script>\n" +
   *   "  document.addEventListener('click', event => window.clicked(event.target));\n" +
   *   "</script>\n" +
   *   "<div>Click me</div>\n" +
   *   "<div>Or click me</div>\n");
   * }</pre>
   *
   * @param name Name of the function on the window object.
   * @param callback Callback function that will be called in the Playwright's context.
   * @since v1.8
   */
  default void exposeBinding(String name, BindingCallback callback) {
    exposeBinding(name, callback, null);
  }
  /**
   * The method adds a function called {@code name} on the {@code window} object of every frame in this page. When called,
   * the function executes {@code callback} and returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a> which
   * resolves to the return value of {@code callback}. If the {@code callback} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, it will be
   * awaited.
   *
   * <p> The first argument of the {@code callback} function contains information about the caller: {@code { browserContext:
   * BrowserContext, page: Page, frame: Frame }}.
   *
   * <p> See {@link BrowserContext#exposeBinding BrowserContext.exposeBinding()} for the context-wide version.
   *
   * <p> <strong>NOTE:</strong> Functions installed via {@link Page#exposeBinding Page.exposeBinding()} survive navigations.
   *
   * <p> **Usage**
   *
   * <p> An example of exposing page URL to all frames in a page:
   * <pre>{@code
   * import com.microsoft.playwright.*;
   *
   * public class Example {
   *   public static void main(String[] args) {
   *     try (Playwright playwright = Playwright.create()) {
   *       BrowserType webkit = playwright.webkit();
   *       Browser browser = webkit.launch({ headless: false });
   *       BrowserContext context = browser.newContext();
   *       Page page = context.newPage();
   *       page.exposeBinding("pageURL", (source, args) -> source.page().url());
   *       page.setContent("<script>\n" +
   *         "  async function onClick() {\n" +
   *         "    document.querySelector('div').textContent = await window.pageURL();\n" +
   *         "  }\n" +
   *         "</script>\n" +
   *         "<button onclick=\"onClick()\">Click me</button>\n" +
   *         "<div></div>");
   *       page.click("button");
   *     }
   *   }
   * }
   * }</pre>
   *
   * <p> An example of passing an element handle:
   * <pre>{@code
   * page.exposeBinding("clicked", (source, args) -> {
   *   ElementHandle element = (ElementHandle) args[0];
   *   System.out.println(element.textContent());
   *   return null;
   * }, new Page.ExposeBindingOptions().setHandle(true));
   * page.setContent("" +
   *   "<script>\n" +
   *   "  document.addEventListener('click', event => window.clicked(event.target));\n" +
   *   "</script>\n" +
   *   "<div>Click me</div>\n" +
   *   "<div>Or click me</div>\n");
   * }</pre>
   *
   * @param name Name of the function on the window object.
   * @param callback Callback function that will be called in the Playwright's context.
   * @since v1.8
   */
  void exposeBinding(String name, BindingCallback callback, ExposeBindingOptions options);
  /**
   * The method adds a function called {@code name} on the {@code window} object of every frame in the page. When called, the
   * function executes {@code callback} and returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a> which
   * resolves to the return value of {@code callback}.
   *
   * <p> If the {@code callback} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, it will be
   * awaited.
   *
   * <p> See {@link BrowserContext#exposeFunction BrowserContext.exposeFunction()} for context-wide exposed function.
   *
   * <p> <strong>NOTE:</strong> Functions installed via {@link Page#exposeFunction Page.exposeFunction()} survive navigations.
   *
   * <p> **Usage**
   *
   * <p> An example of adding a {@code sha256} function to the page:
   * <pre>{@code
   * import com.microsoft.playwright.*;
   *
   * import java.nio.charset.StandardCharsets;
   * import java.security.MessageDigest;
   * import java.security.NoSuchAlgorithmException;
   * import java.util.Base64;
   *
   * public class Example {
   *   public static void main(String[] args) {
   *     try (Playwright playwright = Playwright.create()) {
   *       BrowserType webkit = playwright.webkit();
   *       Browser browser = webkit.launch({ headless: false });
   *       Page page = browser.newPage();
   *       page.exposeFunction("sha256", args -> {
   *         String text = (String) args[0];
   *         MessageDigest crypto;
   *         try {
   *           crypto = MessageDigest.getInstance("SHA-256");
   *         } catch (NoSuchAlgorithmException e) {
   *           return null;
   *         }
   *         byte[] token = crypto.digest(text.getBytes(StandardCharsets.UTF_8));
   *         return Base64.getEncoder().encodeToString(token);
   *       });
   *       page.setContent("<script>\n" +
   *         "  async function onClick() {\n" +
   *         "    document.querySelector('div').textContent = await window.sha256('PLAYWRIGHT');\n" +
   *         "  }\n" +
   *         "</script>\n" +
   *         "<button onclick=\"onClick()\">Click me</button>\n" +
   *         "<div></div>\n");
   *       page.click("button");
   *     }
   *   }
   * }
   * }</pre>
   *
   * @param name Name of the function on the window object
   * @param callback Callback function which will be called in Playwright's context.
   * @since v1.8
   */
  void exposeFunction(String name, FunctionCallback callback);
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability">actionability</a> checks, focuses the element, fills it and
   * triggers an {@code input} event after filling. Note that you can pass an empty string to clear the input field.
   *
   * <p> If the target element is not an {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element, this method
   * throws an error. However, if the element is inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be filled
   * instead.
   *
   * <p> To send fine-grained keyboard events, use {@link Locator#pressSequentially Locator.pressSequentially()}.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param value Value to fill for the {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element.
   * @since v1.8
   */
  default void fill(String selector, String value) {
    fill(selector, value, null);
  }
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability">actionability</a> checks, focuses the element, fills it and
   * triggers an {@code input} event after filling. Note that you can pass an empty string to clear the input field.
   *
   * <p> If the target element is not an {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element, this method
   * throws an error. However, if the element is inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be filled
   * instead.
   *
   * <p> To send fine-grained keyboard events, use {@link Locator#pressSequentially Locator.pressSequentially()}.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param value Value to fill for the {@code <input>}, {@code <textarea>} or {@code [contenteditable]} element.
   * @since v1.8
   */
  void fill(String selector, String value, FillOptions options);
  /**
   * This method fetches an element with {@code selector} and focuses it. If there's no element matching {@code selector},
   * the method waits until a matching element appears in the DOM.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default void focus(String selector) {
    focus(selector, null);
  }
  /**
   * This method fetches an element with {@code selector} and focuses it. If there's no element matching {@code selector},
   * the method waits until a matching element appears in the DOM.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  void focus(String selector, FocusOptions options);
  /**
   * Returns frame matching the specified criteria. Either {@code name} or {@code url} must be specified.
   *
   * <p> **Usage**
   * <pre>{@code
   * Frame frame = page.frame("frame-name");
   * }</pre>
   * <pre>{@code
   * Frame frame = page.frameByUrl(Pattern.compile(".*domain.*");
   * }</pre>
   *
   * @param name Frame name specified in the {@code iframe}'s {@code name} attribute.
   * @since v1.8
   */
  Frame frame(String name);
  /**
   * Returns frame with matching URL.
   *
   * @param url A glob pattern, regex pattern or predicate receiving frame's {@code url} as a [URL] object.
   * @since v1.9
   */
  Frame frameByUrl(String url);
  /**
   * Returns frame with matching URL.
   *
   * @param url A glob pattern, regex pattern or predicate receiving frame's {@code url} as a [URL] object.
   * @since v1.9
   */
  Frame frameByUrl(Pattern url);
  /**
   * Returns frame with matching URL.
   *
   * @param url A glob pattern, regex pattern or predicate receiving frame's {@code url} as a [URL] object.
   * @since v1.9
   */
  Frame frameByUrl(Predicate<String> url);
  /**
   * When working with iframes, you can create a frame locator that will enter the iframe and allow selecting elements in
   * that iframe.
   *
   * <p> **Usage**
   *
   * <p> Following snippet locates element with text "Submit" in the iframe with id {@code my-frame}, like {@code <iframe
   * id="my-frame">}:
   * <pre>{@code
   * Locator locator = page.frameLocator("#my-iframe").getByText("Submit");
   * locator.click();
   * }</pre>
   *
   * @param selector A selector to use when resolving DOM element.
   * @since v1.17
   */
  FrameLocator frameLocator(String selector);
  /**
   * An array of all frames attached to the page.
   *
   * @since v1.8
   */
  List<Frame> frames();
  /**
   * Returns element attribute value.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param name Attribute name to get the value for.
   * @since v1.8
   */
  default String getAttribute(String selector, String name) {
    return getAttribute(selector, name, null);
  }
  /**
   * Returns element attribute value.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param name Attribute name to get the value for.
   * @since v1.8
   */
  String getAttribute(String selector, String name, GetAttributeOptions options);
  /**
   * Allows locating elements by their alt text.
   *
   * <p> **Usage**
   *
   * <p> For example, this method will find the image by alt text "Playwright logo":
   * <pre>{@code
   * page.getByAltText("Playwright logo").click();
   * }</pre>
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  default Locator getByAltText(String text) {
    return getByAltText(text, null);
  }
  /**
   * Allows locating elements by their alt text.
   *
   * <p> **Usage**
   *
   * <p> For example, this method will find the image by alt text "Playwright logo":
   * <pre>{@code
   * page.getByAltText("Playwright logo").click();
   * }</pre>
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  Locator getByAltText(String text, GetByAltTextOptions options);
  /**
   * Allows locating elements by their alt text.
   *
   * <p> **Usage**
   *
   * <p> For example, this method will find the image by alt text "Playwright logo":
   * <pre>{@code
   * page.getByAltText("Playwright logo").click();
   * }</pre>
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  default Locator getByAltText(Pattern text) {
    return getByAltText(text, null);
  }
  /**
   * Allows locating elements by their alt text.
   *
   * <p> **Usage**
   *
   * <p> For example, this method will find the image by alt text "Playwright logo":
   * <pre>{@code
   * page.getByAltText("Playwright logo").click();
   * }</pre>
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  Locator getByAltText(Pattern text, GetByAltTextOptions options);
  /**
   * Allows locating input elements by the text of the associated {@code <label>} or {@code aria-labelledby} element, or by
   * the {@code aria-label} attribute.
   *
   * <p> **Usage**
   *
   * <p> For example, this method will find inputs by label "Username" and "Password" in the following DOM:
   * <pre>{@code
   * page.getByLabel("Username").fill("john");
   * page.getByLabel("Password").fill("secret");
   * }</pre>
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  default Locator getByLabel(String text) {
    return getByLabel(text, null);
  }
  /**
   * Allows locating input elements by the text of the associated {@code <label>} or {@code aria-labelledby} element, or by
   * the {@code aria-label} attribute.
   *
   * <p> **Usage**
   *
   * <p> For example, this method will find inputs by label "Username" and "Password" in the following DOM:
   * <pre>{@code
   * page.getByLabel("Username").fill("john");
   * page.getByLabel("Password").fill("secret");
   * }</pre>
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  Locator getByLabel(String text, GetByLabelOptions options);
  /**
   * Allows locating input elements by the text of the associated {@code <label>} or {@code aria-labelledby} element, or by
   * the {@code aria-label} attribute.
   *
   * <p> **Usage**
   *
   * <p> For example, this method will find inputs by label "Username" and "Password" in the following DOM:
   * <pre>{@code
   * page.getByLabel("Username").fill("john");
   * page.getByLabel("Password").fill("secret");
   * }</pre>
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  default Locator getByLabel(Pattern text) {
    return getByLabel(text, null);
  }
  /**
   * Allows locating input elements by the text of the associated {@code <label>} or {@code aria-labelledby} element, or by
   * the {@code aria-label} attribute.
   *
   * <p> **Usage**
   *
   * <p> For example, this method will find inputs by label "Username" and "Password" in the following DOM:
   * <pre>{@code
   * page.getByLabel("Username").fill("john");
   * page.getByLabel("Password").fill("secret");
   * }</pre>
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  Locator getByLabel(Pattern text, GetByLabelOptions options);
  /**
   * Allows locating input elements by the placeholder text.
   *
   * <p> **Usage**
   *
   * <p> For example, consider the following DOM structure.
   *
   * <p> You can fill the input after locating it by the placeholder text:
   * <pre>{@code
   * page.getByPlaceholder("name@example.com").fill("playwright@microsoft.com");
   * }</pre>
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  default Locator getByPlaceholder(String text) {
    return getByPlaceholder(text, null);
  }
  /**
   * Allows locating input elements by the placeholder text.
   *
   * <p> **Usage**
   *
   * <p> For example, consider the following DOM structure.
   *
   * <p> You can fill the input after locating it by the placeholder text:
   * <pre>{@code
   * page.getByPlaceholder("name@example.com").fill("playwright@microsoft.com");
   * }</pre>
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  Locator getByPlaceholder(String text, GetByPlaceholderOptions options);
  /**
   * Allows locating input elements by the placeholder text.
   *
   * <p> **Usage**
   *
   * <p> For example, consider the following DOM structure.
   *
   * <p> You can fill the input after locating it by the placeholder text:
   * <pre>{@code
   * page.getByPlaceholder("name@example.com").fill("playwright@microsoft.com");
   * }</pre>
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  default Locator getByPlaceholder(Pattern text) {
    return getByPlaceholder(text, null);
  }
  /**
   * Allows locating input elements by the placeholder text.
   *
   * <p> **Usage**
   *
   * <p> For example, consider the following DOM structure.
   *
   * <p> You can fill the input after locating it by the placeholder text:
   * <pre>{@code
   * page.getByPlaceholder("name@example.com").fill("playwright@microsoft.com");
   * }</pre>
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  Locator getByPlaceholder(Pattern text, GetByPlaceholderOptions options);
  /**
   * Allows locating elements by their <a href="https://www.w3.org/TR/wai-aria-1.2/#roles">ARIA role</a>, <a
   * href="https://www.w3.org/TR/wai-aria-1.2/#aria-attributes">ARIA attributes</a> and <a
   * href="https://w3c.github.io/accname/#dfn-accessible-name">accessible name</a>.
   *
   * <p> **Usage**
   *
   * <p> Consider the following DOM structure.
   *
   * <p> You can locate each element by it's implicit role:
   * <pre>{@code
   * assertThat(page
   *     .getByRole(AriaRole.HEADING,
   *                new Page.GetByRoleOptions().setName("Sign up")))
   *     .isVisible();
   *
   * page.getByRole(AriaRole.CHECKBOX,
   *                new Page.GetByRoleOptions().setName("Subscribe"))
   *     .check();
   *
   * page.getByRole(AriaRole.BUTTON,
   *                new Page.GetByRoleOptions().setName(
   *                    Pattern.compile("submit", Pattern.CASE_INSENSITIVE)))
   *     .click();
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> Role selector **does not replace** accessibility audits and conformance tests, but rather gives early feedback about the
   * ARIA guidelines.
   *
   * <p> Many html elements have an implicitly <a href="https://w3c.github.io/html-aam/#html-element-role-mappings">defined
   * role</a> that is recognized by the role selector. You can find all the <a
   * href="https://www.w3.org/TR/wai-aria-1.2/#role_definitions">supported roles here</a>. ARIA guidelines **do not
   * recommend** duplicating implicit roles and attributes by setting {@code role} and/or {@code aria-*} attributes to
   * default values.
   *
   * @param role Required aria role.
   * @since v1.27
   */
  default Locator getByRole(AriaRole role) {
    return getByRole(role, null);
  }
  /**
   * Allows locating elements by their <a href="https://www.w3.org/TR/wai-aria-1.2/#roles">ARIA role</a>, <a
   * href="https://www.w3.org/TR/wai-aria-1.2/#aria-attributes">ARIA attributes</a> and <a
   * href="https://w3c.github.io/accname/#dfn-accessible-name">accessible name</a>.
   *
   * <p> **Usage**
   *
   * <p> Consider the following DOM structure.
   *
   * <p> You can locate each element by it's implicit role:
   * <pre>{@code
   * assertThat(page
   *     .getByRole(AriaRole.HEADING,
   *                new Page.GetByRoleOptions().setName("Sign up")))
   *     .isVisible();
   *
   * page.getByRole(AriaRole.CHECKBOX,
   *                new Page.GetByRoleOptions().setName("Subscribe"))
   *     .check();
   *
   * page.getByRole(AriaRole.BUTTON,
   *                new Page.GetByRoleOptions().setName(
   *                    Pattern.compile("submit", Pattern.CASE_INSENSITIVE)))
   *     .click();
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> Role selector **does not replace** accessibility audits and conformance tests, but rather gives early feedback about the
   * ARIA guidelines.
   *
   * <p> Many html elements have an implicitly <a href="https://w3c.github.io/html-aam/#html-element-role-mappings">defined
   * role</a> that is recognized by the role selector. You can find all the <a
   * href="https://www.w3.org/TR/wai-aria-1.2/#role_definitions">supported roles here</a>. ARIA guidelines **do not
   * recommend** duplicating implicit roles and attributes by setting {@code role} and/or {@code aria-*} attributes to
   * default values.
   *
   * @param role Required aria role.
   * @since v1.27
   */
  Locator getByRole(AriaRole role, GetByRoleOptions options);
  /**
   * Locate element by the test id.
   *
   * <p> **Usage**
   *
   * <p> Consider the following DOM structure.
   *
   * <p> You can locate the element by it's test id:
   * <pre>{@code
   * page.getByTestId("directions").click();
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> By default, the {@code data-testid} attribute is used as a test id. Use {@link Selectors#setTestIdAttribute
   * Selectors.setTestIdAttribute()} to configure a different test id attribute if necessary.
   *
   * @param testId Id to locate the element by.
   * @since v1.27
   */
  Locator getByTestId(String testId);
  /**
   * Locate element by the test id.
   *
   * <p> **Usage**
   *
   * <p> Consider the following DOM structure.
   *
   * <p> You can locate the element by it's test id:
   * <pre>{@code
   * page.getByTestId("directions").click();
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> By default, the {@code data-testid} attribute is used as a test id. Use {@link Selectors#setTestIdAttribute
   * Selectors.setTestIdAttribute()} to configure a different test id attribute if necessary.
   *
   * @param testId Id to locate the element by.
   * @since v1.27
   */
  Locator getByTestId(Pattern testId);
  /**
   * Allows locating elements that contain given text.
   *
   * <p> See also {@link Locator#filter Locator.filter()} that allows to match by another criteria, like an accessible role, and
   * then filter by the text content.
   *
   * <p> **Usage**
   *
   * <p> Consider the following DOM structure:
   *
   * <p> You can locate by text substring, exact string, or a regular expression:
   * <pre>{@code
   * // Matches <span>
   * page.getByText("world")
   *
   * // Matches first <div>
   * page.getByText("Hello world")
   *
   * // Matches second <div>
   * page.getByText("Hello", new Page.GetByTextOptions().setExact(true))
   *
   * // Matches both <div>s
   * page.getByText(Pattern.compile("Hello"))
   *
   * // Matches second <div>
   * page.getByText(Pattern.compile("^hello$", Pattern.CASE_INSENSITIVE))
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> Matching by text always normalizes whitespace, even with exact match. For example, it turns multiple spaces into one,
   * turns line breaks into spaces and ignores leading and trailing whitespace.
   *
   * <p> Input elements of the type {@code button} and {@code submit} are matched by their {@code value} instead of the text
   * content. For example, locating by text {@code "Log in"} matches {@code <input type=button value="Log in">}.
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  default Locator getByText(String text) {
    return getByText(text, null);
  }
  /**
   * Allows locating elements that contain given text.
   *
   * <p> See also {@link Locator#filter Locator.filter()} that allows to match by another criteria, like an accessible role, and
   * then filter by the text content.
   *
   * <p> **Usage**
   *
   * <p> Consider the following DOM structure:
   *
   * <p> You can locate by text substring, exact string, or a regular expression:
   * <pre>{@code
   * // Matches <span>
   * page.getByText("world")
   *
   * // Matches first <div>
   * page.getByText("Hello world")
   *
   * // Matches second <div>
   * page.getByText("Hello", new Page.GetByTextOptions().setExact(true))
   *
   * // Matches both <div>s
   * page.getByText(Pattern.compile("Hello"))
   *
   * // Matches second <div>
   * page.getByText(Pattern.compile("^hello$", Pattern.CASE_INSENSITIVE))
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> Matching by text always normalizes whitespace, even with exact match. For example, it turns multiple spaces into one,
   * turns line breaks into spaces and ignores leading and trailing whitespace.
   *
   * <p> Input elements of the type {@code button} and {@code submit} are matched by their {@code value} instead of the text
   * content. For example, locating by text {@code "Log in"} matches {@code <input type=button value="Log in">}.
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  Locator getByText(String text, GetByTextOptions options);
  /**
   * Allows locating elements that contain given text.
   *
   * <p> See also {@link Locator#filter Locator.filter()} that allows to match by another criteria, like an accessible role, and
   * then filter by the text content.
   *
   * <p> **Usage**
   *
   * <p> Consider the following DOM structure:
   *
   * <p> You can locate by text substring, exact string, or a regular expression:
   * <pre>{@code
   * // Matches <span>
   * page.getByText("world")
   *
   * // Matches first <div>
   * page.getByText("Hello world")
   *
   * // Matches second <div>
   * page.getByText("Hello", new Page.GetByTextOptions().setExact(true))
   *
   * // Matches both <div>s
   * page.getByText(Pattern.compile("Hello"))
   *
   * // Matches second <div>
   * page.getByText(Pattern.compile("^hello$", Pattern.CASE_INSENSITIVE))
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> Matching by text always normalizes whitespace, even with exact match. For example, it turns multiple spaces into one,
   * turns line breaks into spaces and ignores leading and trailing whitespace.
   *
   * <p> Input elements of the type {@code button} and {@code submit} are matched by their {@code value} instead of the text
   * content. For example, locating by text {@code "Log in"} matches {@code <input type=button value="Log in">}.
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  default Locator getByText(Pattern text) {
    return getByText(text, null);
  }
  /**
   * Allows locating elements that contain given text.
   *
   * <p> See also {@link Locator#filter Locator.filter()} that allows to match by another criteria, like an accessible role, and
   * then filter by the text content.
   *
   * <p> **Usage**
   *
   * <p> Consider the following DOM structure:
   *
   * <p> You can locate by text substring, exact string, or a regular expression:
   * <pre>{@code
   * // Matches <span>
   * page.getByText("world")
   *
   * // Matches first <div>
   * page.getByText("Hello world")
   *
   * // Matches second <div>
   * page.getByText("Hello", new Page.GetByTextOptions().setExact(true))
   *
   * // Matches both <div>s
   * page.getByText(Pattern.compile("Hello"))
   *
   * // Matches second <div>
   * page.getByText(Pattern.compile("^hello$", Pattern.CASE_INSENSITIVE))
   * }</pre>
   *
   * <p> **Details**
   *
   * <p> Matching by text always normalizes whitespace, even with exact match. For example, it turns multiple spaces into one,
   * turns line breaks into spaces and ignores leading and trailing whitespace.
   *
   * <p> Input elements of the type {@code button} and {@code submit} are matched by their {@code value} instead of the text
   * content. For example, locating by text {@code "Log in"} matches {@code <input type=button value="Log in">}.
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  Locator getByText(Pattern text, GetByTextOptions options);
  /**
   * Allows locating elements by their title attribute.
   *
   * <p> **Usage**
   *
   * <p> Consider the following DOM structure.
   *
   * <p> You can check the issues count after locating it by the title text:
   * <pre>{@code
   * assertThat(page.getByTitle("Issues count")).hasText("25 issues");
   * }</pre>
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  default Locator getByTitle(String text) {
    return getByTitle(text, null);
  }
  /**
   * Allows locating elements by their title attribute.
   *
   * <p> **Usage**
   *
   * <p> Consider the following DOM structure.
   *
   * <p> You can check the issues count after locating it by the title text:
   * <pre>{@code
   * assertThat(page.getByTitle("Issues count")).hasText("25 issues");
   * }</pre>
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  Locator getByTitle(String text, GetByTitleOptions options);
  /**
   * Allows locating elements by their title attribute.
   *
   * <p> **Usage**
   *
   * <p> Consider the following DOM structure.
   *
   * <p> You can check the issues count after locating it by the title text:
   * <pre>{@code
   * assertThat(page.getByTitle("Issues count")).hasText("25 issues");
   * }</pre>
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  default Locator getByTitle(Pattern text) {
    return getByTitle(text, null);
  }
  /**
   * Allows locating elements by their title attribute.
   *
   * <p> **Usage**
   *
   * <p> Consider the following DOM structure.
   *
   * <p> You can check the issues count after locating it by the title text:
   * <pre>{@code
   * assertThat(page.getByTitle("Issues count")).hasText("25 issues");
   * }</pre>
   *
   * @param text Text to locate the element for.
   * @since v1.27
   */
  Locator getByTitle(Pattern text, GetByTitleOptions options);
  /**
   * Returns the main resource response. In case of multiple redirects, the navigation will resolve with the response of the
   * last redirect. If can not go back, returns {@code null}.
   *
   * <p> Navigate to the previous page in history.
   *
   * @since v1.8
   */
  default Response goBack() {
    return goBack(null);
  }
  /**
   * Returns the main resource response. In case of multiple redirects, the navigation will resolve with the response of the
   * last redirect. If can not go back, returns {@code null}.
   *
   * <p> Navigate to the previous page in history.
   *
   * @since v1.8
   */
  Response goBack(GoBackOptions options);
  /**
   * Returns the main resource response. In case of multiple redirects, the navigation will resolve with the response of the
   * last redirect. If can not go forward, returns {@code null}.
   *
   * <p> Navigate to the next page in history.
   *
   * @since v1.8
   */
  default Response goForward() {
    return goForward(null);
  }
  /**
   * Returns the main resource response. In case of multiple redirects, the navigation will resolve with the response of the
   * last redirect. If can not go forward, returns {@code null}.
   *
   * <p> Navigate to the next page in history.
   *
   * @since v1.8
   */
  Response goForward(GoForwardOptions options);
  /**
   * Returns the main resource response. In case of multiple redirects, the navigation will resolve with the first
   * non-redirect response.
   *
   * <p> The method will throw an error if:
   * <ul>
   * <li> there's an SSL error (e.g. in case of self-signed certificates).</li>
   * <li> target URL is invalid.</li>
   * <li> the {@code timeout} is exceeded during navigation.</li>
   * <li> the remote server does not respond or is unreachable.</li>
   * <li> the main resource failed to load.</li>
   * </ul>
   *
   * <p> The method will not throw an error when any valid HTTP status code is returned by the remote server, including 404 "Not
   * Found" and 500 "Internal Server Error".  The status code for such responses can be retrieved by calling {@link
   * Response#status Response.status()}.
   *
   * <p> <strong>NOTE:</strong> The method either throws an error or returns a main resource response. The only exceptions are navigation to {@code
   * about:blank} or navigation to the same URL with a different hash, which would succeed and return {@code null}.
   *
   * <p> <strong>NOTE:</strong> Headless mode doesn't support navigation to a PDF document. See the <a
   * href="https://bugs.chromium.org/p/chromium/issues/detail?id=761295">upstream issue</a>.
   *
   * @param url URL to navigate page to. The url should include scheme, e.g. {@code https://}. When a {@code baseURL} via the context
   * options was provided and the passed URL is a path, it gets merged via the <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/URL/URL">{@code new URL()}</a> constructor.
   * @since v1.8
   */
  default Response navigate(String url) {
    return navigate(url, null);
  }
  /**
   * Returns the main resource response. In case of multiple redirects, the navigation will resolve with the first
   * non-redirect response.
   *
   * <p> The method will throw an error if:
   * <ul>
   * <li> there's an SSL error (e.g. in case of self-signed certificates).</li>
   * <li> target URL is invalid.</li>
   * <li> the {@code timeout} is exceeded during navigation.</li>
   * <li> the remote server does not respond or is unreachable.</li>
   * <li> the main resource failed to load.</li>
   * </ul>
   *
   * <p> The method will not throw an error when any valid HTTP status code is returned by the remote server, including 404 "Not
   * Found" and 500 "Internal Server Error".  The status code for such responses can be retrieved by calling {@link
   * Response#status Response.status()}.
   *
   * <p> <strong>NOTE:</strong> The method either throws an error or returns a main resource response. The only exceptions are navigation to {@code
   * about:blank} or navigation to the same URL with a different hash, which would succeed and return {@code null}.
   *
   * <p> <strong>NOTE:</strong> Headless mode doesn't support navigation to a PDF document. See the <a
   * href="https://bugs.chromium.org/p/chromium/issues/detail?id=761295">upstream issue</a>.
   *
   * @param url URL to navigate page to. The url should include scheme, e.g. {@code https://}. When a {@code baseURL} via the context
   * options was provided and the passed URL is a path, it gets merged via the <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/URL/URL">{@code new URL()}</a> constructor.
   * @since v1.8
   */
  Response navigate(String url, NavigateOptions options);
  /**
   * This method hovers over an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to hover over the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default void hover(String selector) {
    hover(selector, null);
  }
  /**
   * This method hovers over an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to hover over the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  void hover(String selector, HoverOptions options);
  /**
   * Returns {@code element.innerHTML}.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default String innerHTML(String selector) {
    return innerHTML(selector, null);
  }
  /**
   * Returns {@code element.innerHTML}.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  String innerHTML(String selector, InnerHTMLOptions options);
  /**
   * Returns {@code element.innerText}.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default String innerText(String selector) {
    return innerText(selector, null);
  }
  /**
   * Returns {@code element.innerText}.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  String innerText(String selector, InnerTextOptions options);
  /**
   * Returns {@code input.value} for the selected {@code <input>} or {@code <textarea>} or {@code <select>} element.
   *
   * <p> Throws for non-input elements. However, if the element is inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, returns the value of the
   * control.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.13
   */
  default String inputValue(String selector) {
    return inputValue(selector, null);
  }
  /**
   * Returns {@code input.value} for the selected {@code <input>} or {@code <textarea>} or {@code <select>} element.
   *
   * <p> Throws for non-input elements. However, if the element is inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, returns the value of the
   * control.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.13
   */
  String inputValue(String selector, InputValueOptions options);
  /**
   * Returns whether the element is checked. Throws if the element is not a checkbox or radio input.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default boolean isChecked(String selector) {
    return isChecked(selector, null);
  }
  /**
   * Returns whether the element is checked. Throws if the element is not a checkbox or radio input.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  boolean isChecked(String selector, IsCheckedOptions options);
  /**
   * Indicates that the page has been closed.
   *
   * @since v1.8
   */
  boolean isClosed();
  /**
   * Returns whether the element is disabled, the opposite of <a
   * href="https://playwright.dev/java/docs/actionability#enabled">enabled</a>.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default boolean isDisabled(String selector) {
    return isDisabled(selector, null);
  }
  /**
   * Returns whether the element is disabled, the opposite of <a
   * href="https://playwright.dev/java/docs/actionability#enabled">enabled</a>.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  boolean isDisabled(String selector, IsDisabledOptions options);
  /**
   * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability#editable">editable</a>.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default boolean isEditable(String selector) {
    return isEditable(selector, null);
  }
  /**
   * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability#editable">editable</a>.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  boolean isEditable(String selector, IsEditableOptions options);
  /**
   * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability#enabled">enabled</a>.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default boolean isEnabled(String selector) {
    return isEnabled(selector, null);
  }
  /**
   * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability#enabled">enabled</a>.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  boolean isEnabled(String selector, IsEnabledOptions options);
  /**
   * Returns whether the element is hidden, the opposite of <a
   * href="https://playwright.dev/java/docs/actionability#visible">visible</a>.  {@code selector} that does not match any
   * elements is considered hidden.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default boolean isHidden(String selector) {
    return isHidden(selector, null);
  }
  /**
   * Returns whether the element is hidden, the opposite of <a
   * href="https://playwright.dev/java/docs/actionability#visible">visible</a>.  {@code selector} that does not match any
   * elements is considered hidden.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  boolean isHidden(String selector, IsHiddenOptions options);
  /**
   * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability#visible">visible</a>. {@code
   * selector} that does not match any elements is considered not visible.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default boolean isVisible(String selector) {
    return isVisible(selector, null);
  }
  /**
   * Returns whether the element is <a href="https://playwright.dev/java/docs/actionability#visible">visible</a>. {@code
   * selector} that does not match any elements is considered not visible.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  boolean isVisible(String selector, IsVisibleOptions options);
  /**
   *
   *
   * @since v1.8
   */
  Keyboard keyboard();
  /**
   * The method returns an element locator that can be used to perform actions on this page / frame. Locator is resolved to
   * the element immediately before performing an action, so a series of actions on the same locator can in fact be performed
   * on different DOM elements. That would happen if the DOM structure between those actions has changed.
   *
   * <p> <a href="https://playwright.dev/java/docs/locators">Learn more about locators</a>.
   *
   * @param selector A selector to use when resolving DOM element.
   * @since v1.14
   */
  default Locator locator(String selector) {
    return locator(selector, null);
  }
  /**
   * The method returns an element locator that can be used to perform actions on this page / frame. Locator is resolved to
   * the element immediately before performing an action, so a series of actions on the same locator can in fact be performed
   * on different DOM elements. That would happen if the DOM structure between those actions has changed.
   *
   * <p> <a href="https://playwright.dev/java/docs/locators">Learn more about locators</a>.
   *
   * @param selector A selector to use when resolving DOM element.
   * @since v1.14
   */
  Locator locator(String selector, LocatorOptions options);
  /**
   * The page's main frame. Page is guaranteed to have a main frame which persists during navigations.
   *
   * @since v1.8
   */
  Frame mainFrame();
  /**
   *
   *
   * @since v1.8
   */
  Mouse mouse();
  /**
   * Adds one-off {@code Dialog} handler. The handler will be removed immediately after next {@code Dialog} is created.
   * <pre>{@code
   * page.onceDialog(dialog -> {
   *   dialog.accept("foo");
   * });
   *
   * // prints 'foo'
   * System.out.println(page.evaluate("prompt('Enter string:')"));
   *
   * // prints 'null' as the dialog will be auto-dismissed because there are no handlers.
   * System.out.println(page.evaluate("prompt('Enter string:')"));
   * }</pre>
   *
   * <p> This code above is equivalent to:
   * <pre>{@code
   * Consumer<Dialog> handler = new Consumer<Dialog>() {
   *   @Override
   *   public void accept(Dialog dialog) {
   *     dialog.accept("foo");
   *     page.offDialog(this);
   *   }
   * };
   * page.onDialog(handler);
   *
   * // prints 'foo'
   * System.out.println(page.evaluate("prompt('Enter string:')"));
   *
   * // prints 'null' as the dialog will be auto-dismissed because there are no handlers.
   * System.out.println(page.evaluate("prompt('Enter string:')"));
   * }</pre>
   *
   * @param handler Receives the {@code Dialog} object, it **must** either {@link Dialog#accept Dialog.accept()} or {@link Dialog#dismiss
   * Dialog.dismiss()} the dialog - otherwise the page will <a
   * href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/EventLoop#never_blocking">freeze</a> waiting for the
   * dialog, and actions like click will never finish.
   * @since v1.10
   */
  void onceDialog(Consumer<Dialog> handler);
  /**
   * Returns the opener for popup pages and {@code null} for others. If the opener has been closed already the returns {@code
   * null}.
   *
   * @since v1.8
   */
  Page opener();
  /**
   * Pauses script execution. Playwright will stop executing the script and wait for the user to either press 'Resume' button
   * in the page overlay or to call {@code playwright.resume()} in the DevTools console.
   *
   * <p> User can inspect selectors or perform manual steps while paused. Resume will continue running the original script from
   * the place it was paused.
   *
   * <p> <strong>NOTE:</strong> This method requires Playwright to be started in a headed mode, with a falsy {@code headless} value in the {@link
   * BrowserType#launch BrowserType.launch()}.
   *
   * @since v1.9
   */
  void pause();
  /**
   * Returns the PDF buffer.
   *
   * <p> <strong>NOTE:</strong> Generating a pdf is currently only supported in Chromium headless.
   *
   * <p> {@code page.pdf()} generates a pdf of the page with {@code print} css media. To generate a pdf with {@code screen}
   * media, call {@link Page#emulateMedia Page.emulateMedia()} before calling {@code page.pdf()}:
   *
   * <p> <strong>NOTE:</strong> By default, {@code page.pdf()} generates a pdf with modified colors for printing. Use the <a
   * href="https://developer.mozilla.org/en-US/docs/Web/CSS/-webkit-print-color-adjust">{@code
   * -webkit-print-color-adjust}</a> property to force rendering of exact colors.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Generates a PDF with "screen" media type.
   * page.emulateMedia(new Page.EmulateMediaOptions().setMedia(Media.SCREEN));
   * page.pdf(new Page.PdfOptions().setPath(Paths.get("page.pdf")));
   * }</pre>
   *
   * <p> The {@code width}, {@code height}, and {@code margin} options accept values labeled with units. Unlabeled values are
   * treated as pixels.
   *
   * <p> A few examples:
   * <ul>
   * <li> {@code page.pdf({width: 100})} - prints with width set to 100 pixels</li>
   * <li> {@code page.pdf({width: '100px'})} - prints with width set to 100 pixels</li>
   * <li> {@code page.pdf({width: '10cm'})} - prints with width set to 10 centimeters.</li>
   * </ul>
   *
   * <p> All possible units are:
   * <ul>
   * <li> {@code px} - pixel</li>
   * <li> {@code in} - inch</li>
   * <li> {@code cm} - centimeter</li>
   * <li> {@code mm} - millimeter</li>
   * </ul>
   *
   * <p> The {@code format} options are:
   * <ul>
   * <li> {@code Letter}: 8.5in x 11in</li>
   * <li> {@code Legal}: 8.5in x 14in</li>
   * <li> {@code Tabloid}: 11in x 17in</li>
   * <li> {@code Ledger}: 17in x 11in</li>
   * <li> {@code A0}: 33.1in x 46.8in</li>
   * <li> {@code A1}: 23.4in x 33.1in</li>
   * <li> {@code A2}: 16.54in x 23.4in</li>
   * <li> {@code A3}: 11.7in x 16.54in</li>
   * <li> {@code A4}: 8.27in x 11.7in</li>
   * <li> {@code A5}: 5.83in x 8.27in</li>
   * <li> {@code A6}: 4.13in x 5.83in</li>
   * </ul>
   *
   * <p> <strong>NOTE:</strong> {@code headerTemplate} and {@code footerTemplate} markup have the following limitations: > 1. Script tags inside
   * templates are not evaluated. > 2. Page styles are not visible inside templates.
   *
   * @since v1.8
   */
  default byte[] pdf() {
    return pdf(null);
  }
  /**
   * Returns the PDF buffer.
   *
   * <p> <strong>NOTE:</strong> Generating a pdf is currently only supported in Chromium headless.
   *
   * <p> {@code page.pdf()} generates a pdf of the page with {@code print} css media. To generate a pdf with {@code screen}
   * media, call {@link Page#emulateMedia Page.emulateMedia()} before calling {@code page.pdf()}:
   *
   * <p> <strong>NOTE:</strong> By default, {@code page.pdf()} generates a pdf with modified colors for printing. Use the <a
   * href="https://developer.mozilla.org/en-US/docs/Web/CSS/-webkit-print-color-adjust">{@code
   * -webkit-print-color-adjust}</a> property to force rendering of exact colors.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Generates a PDF with "screen" media type.
   * page.emulateMedia(new Page.EmulateMediaOptions().setMedia(Media.SCREEN));
   * page.pdf(new Page.PdfOptions().setPath(Paths.get("page.pdf")));
   * }</pre>
   *
   * <p> The {@code width}, {@code height}, and {@code margin} options accept values labeled with units. Unlabeled values are
   * treated as pixels.
   *
   * <p> A few examples:
   * <ul>
   * <li> {@code page.pdf({width: 100})} - prints with width set to 100 pixels</li>
   * <li> {@code page.pdf({width: '100px'})} - prints with width set to 100 pixels</li>
   * <li> {@code page.pdf({width: '10cm'})} - prints with width set to 10 centimeters.</li>
   * </ul>
   *
   * <p> All possible units are:
   * <ul>
   * <li> {@code px} - pixel</li>
   * <li> {@code in} - inch</li>
   * <li> {@code cm} - centimeter</li>
   * <li> {@code mm} - millimeter</li>
   * </ul>
   *
   * <p> The {@code format} options are:
   * <ul>
   * <li> {@code Letter}: 8.5in x 11in</li>
   * <li> {@code Legal}: 8.5in x 14in</li>
   * <li> {@code Tabloid}: 11in x 17in</li>
   * <li> {@code Ledger}: 17in x 11in</li>
   * <li> {@code A0}: 33.1in x 46.8in</li>
   * <li> {@code A1}: 23.4in x 33.1in</li>
   * <li> {@code A2}: 16.54in x 23.4in</li>
   * <li> {@code A3}: 11.7in x 16.54in</li>
   * <li> {@code A4}: 8.27in x 11.7in</li>
   * <li> {@code A5}: 5.83in x 8.27in</li>
   * <li> {@code A6}: 4.13in x 5.83in</li>
   * </ul>
   *
   * <p> <strong>NOTE:</strong> {@code headerTemplate} and {@code footerTemplate} markup have the following limitations: > 1. Script tags inside
   * templates are not evaluated. > 2. Page styles are not visible inside templates.
   *
   * @since v1.8
   */
  byte[] pdf(PdfOptions options);
  /**
   * Focuses the element, and then uses {@link Keyboard#down Keyboard.down()} and {@link Keyboard#up Keyboard.up()}.
   *
   * <p> {@code key} can specify the intended <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key">keyboardEvent.key</a> value or a single
   * character to generate the text for. A superset of the {@code key} values can be found <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key/Key_Values">here</a>. Examples of the keys are:
   *
   * <p> {@code F1} - {@code F12}, {@code Digit0}- {@code Digit9}, {@code KeyA}- {@code KeyZ}, {@code Backquote}, {@code Minus},
   * {@code Equal}, {@code Backslash}, {@code Backspace}, {@code Tab}, {@code Delete}, {@code Escape}, {@code ArrowDown},
   * {@code End}, {@code Enter}, {@code Home}, {@code Insert}, {@code PageDown}, {@code PageUp}, {@code ArrowRight}, {@code
   * ArrowUp}, etc.
   *
   * <p> Following modification shortcuts are also supported: {@code Shift}, {@code Control}, {@code Alt}, {@code Meta}, {@code
   * ShiftLeft}.
   *
   * <p> Holding down {@code Shift} will type the text that corresponds to the {@code key} in the upper case.
   *
   * <p> If {@code key} is a single character, it is case-sensitive, so the values {@code a} and {@code A} will generate
   * different respective texts.
   *
   * <p> Shortcuts such as {@code key: "Control+o"} or {@code key: "Control+Shift+T"} are supported as well. When specified with
   * the modifier, modifier is pressed and being held while the subsequent key is being pressed.
   *
   * <p> **Usage**
   * <pre>{@code
   * Page page = browser.newPage();
   * page.navigate("https://keycode.info");
   * page.press("body", "A");
   * page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("A.png")));
   * page.press("body", "ArrowLeft");
   * page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("ArrowLeft.png" )));
   * page.press("body", "Shift+O");
   * page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("O.png" )));
   * }</pre>
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
   * @since v1.8
   */
  default void press(String selector, String key) {
    press(selector, key, null);
  }
  /**
   * Focuses the element, and then uses {@link Keyboard#down Keyboard.down()} and {@link Keyboard#up Keyboard.up()}.
   *
   * <p> {@code key} can specify the intended <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key">keyboardEvent.key</a> value or a single
   * character to generate the text for. A superset of the {@code key} values can be found <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/KeyboardEvent/key/Key_Values">here</a>. Examples of the keys are:
   *
   * <p> {@code F1} - {@code F12}, {@code Digit0}- {@code Digit9}, {@code KeyA}- {@code KeyZ}, {@code Backquote}, {@code Minus},
   * {@code Equal}, {@code Backslash}, {@code Backspace}, {@code Tab}, {@code Delete}, {@code Escape}, {@code ArrowDown},
   * {@code End}, {@code Enter}, {@code Home}, {@code Insert}, {@code PageDown}, {@code PageUp}, {@code ArrowRight}, {@code
   * ArrowUp}, etc.
   *
   * <p> Following modification shortcuts are also supported: {@code Shift}, {@code Control}, {@code Alt}, {@code Meta}, {@code
   * ShiftLeft}.
   *
   * <p> Holding down {@code Shift} will type the text that corresponds to the {@code key} in the upper case.
   *
   * <p> If {@code key} is a single character, it is case-sensitive, so the values {@code a} and {@code A} will generate
   * different respective texts.
   *
   * <p> Shortcuts such as {@code key: "Control+o"} or {@code key: "Control+Shift+T"} are supported as well. When specified with
   * the modifier, modifier is pressed and being held while the subsequent key is being pressed.
   *
   * <p> **Usage**
   * <pre>{@code
   * Page page = browser.newPage();
   * page.navigate("https://keycode.info");
   * page.press("body", "A");
   * page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("A.png")));
   * page.press("body", "ArrowLeft");
   * page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("ArrowLeft.png" )));
   * page.press("body", "Shift+O");
   * page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("O.png" )));
   * }</pre>
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param key Name of the key to press or a character to generate, such as {@code ArrowLeft} or {@code a}.
   * @since v1.8
   */
  void press(String selector, String key, PressOptions options);
  /**
   * The method finds an element matching the specified selector within the page. If no elements match the selector, the
   * return value resolves to {@code null}. To wait for an element on the page, use {@link Locator#waitFor
   * Locator.waitFor()}.
   *
   * @param selector A selector to query for.
   * @since v1.9
   */
  default ElementHandle querySelector(String selector) {
    return querySelector(selector, null);
  }
  /**
   * The method finds an element matching the specified selector within the page. If no elements match the selector, the
   * return value resolves to {@code null}. To wait for an element on the page, use {@link Locator#waitFor
   * Locator.waitFor()}.
   *
   * @param selector A selector to query for.
   * @since v1.9
   */
  ElementHandle querySelector(String selector, QuerySelectorOptions options);
  /**
   * The method finds all elements matching the specified selector within the page. If no elements match the selector, the
   * return value resolves to {@code []}.
   *
   * @param selector A selector to query for.
   * @since v1.9
   */
  List<ElementHandle> querySelectorAll(String selector);
  /**
   * This method reloads the current page, in the same way as if the user had triggered a browser refresh. Returns the main
   * resource response. In case of multiple redirects, the navigation will resolve with the response of the last redirect.
   *
   * @since v1.8
   */
  default Response reload() {
    return reload(null);
  }
  /**
   * This method reloads the current page, in the same way as if the user had triggered a browser refresh. Returns the main
   * resource response. In case of multiple redirects, the navigation will resolve with the response of the last redirect.
   *
   * @since v1.8
   */
  Response reload(ReloadOptions options);
  /**
   * API testing helper associated with this page. This method returns the same instance as {@link BrowserContext#request
   * BrowserContext.request()} on the page's context. See {@link BrowserContext#request BrowserContext.request()} for more
   * details.
   *
   * @since v1.16
   */
  APIRequestContext request();
  /**
   * Routing provides the capability to modify network requests that are made by a page.
   *
   * <p> Once routing is enabled, every request matching the url pattern will stall unless it's continued, fulfilled or aborted.
   *
   * <p> <strong>NOTE:</strong> The handler will only be called for the first url if the response is a redirect.
   *
   * <p> <strong>NOTE:</strong> {@link Page#route Page.route()} will not intercept requests intercepted by Service Worker. See <a
   * href="https://github.com/microsoft/playwright/issues/1090">this</a> issue. We recommend disabling Service Workers when
   * using request interception by setting {@code Browser.newContext.serviceWorkers} to {@code "block"}.
   *
   * <p> **Usage**
   *
   * <p> An example of a naive handler that aborts all image requests:
   * <pre>{@code
   * Page page = browser.newPage();
   * page.route("**\/*.{png,jpg,jpeg}", route -> route.abort());
   * page.navigate("https://example.com");
   * browser.close();
   * }</pre>
   *
   * <p> or the same snippet using a regex pattern instead:
   * <pre>{@code
   * Page page = browser.newPage();
   * page.route(Pattern.compile("(\\.png$)|(\\.jpg$)"),route -> route.abort());
   * page.navigate("https://example.com");
   * browser.close();
   * }</pre>
   *
   * <p> It is possible to examine the request to decide the route action. For example, mocking all requests that contain some
   * post data, and leaving all other requests as is:
   * <pre>{@code
   * page.route("/api/**", route -> {
   *   if (route.request().postData().contains("my-string"))
   *     route.fulfill(new Route.FulfillOptions().setBody("mocked-data"));
   *   else
   *     route.resume();
   * });
   * }</pre>
   *
   * <p> Page routes take precedence over browser context routes (set up with {@link BrowserContext#route
   * BrowserContext.route()}) when request matches both handlers.
   *
   * <p> To remove a route with its handler you can use {@link Page#unroute Page.unroute()}.
   *
   * <p> <strong>NOTE:</strong> Enabling routing disables http cache.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while routing. When a {@code baseURL} via the
   * context options was provided and the passed URL is a path, it gets merged via the <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/URL/URL">{@code new URL()}</a> constructor.
   * @param handler handler function to route the request.
   * @since v1.8
   */
  default void route(String url, Consumer<Route> handler) {
    route(url, handler, null);
  }
  /**
   * Routing provides the capability to modify network requests that are made by a page.
   *
   * <p> Once routing is enabled, every request matching the url pattern will stall unless it's continued, fulfilled or aborted.
   *
   * <p> <strong>NOTE:</strong> The handler will only be called for the first url if the response is a redirect.
   *
   * <p> <strong>NOTE:</strong> {@link Page#route Page.route()} will not intercept requests intercepted by Service Worker. See <a
   * href="https://github.com/microsoft/playwright/issues/1090">this</a> issue. We recommend disabling Service Workers when
   * using request interception by setting {@code Browser.newContext.serviceWorkers} to {@code "block"}.
   *
   * <p> **Usage**
   *
   * <p> An example of a naive handler that aborts all image requests:
   * <pre>{@code
   * Page page = browser.newPage();
   * page.route("**\/*.{png,jpg,jpeg}", route -> route.abort());
   * page.navigate("https://example.com");
   * browser.close();
   * }</pre>
   *
   * <p> or the same snippet using a regex pattern instead:
   * <pre>{@code
   * Page page = browser.newPage();
   * page.route(Pattern.compile("(\\.png$)|(\\.jpg$)"),route -> route.abort());
   * page.navigate("https://example.com");
   * browser.close();
   * }</pre>
   *
   * <p> It is possible to examine the request to decide the route action. For example, mocking all requests that contain some
   * post data, and leaving all other requests as is:
   * <pre>{@code
   * page.route("/api/**", route -> {
   *   if (route.request().postData().contains("my-string"))
   *     route.fulfill(new Route.FulfillOptions().setBody("mocked-data"));
   *   else
   *     route.resume();
   * });
   * }</pre>
   *
   * <p> Page routes take precedence over browser context routes (set up with {@link BrowserContext#route
   * BrowserContext.route()}) when request matches both handlers.
   *
   * <p> To remove a route with its handler you can use {@link Page#unroute Page.unroute()}.
   *
   * <p> <strong>NOTE:</strong> Enabling routing disables http cache.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while routing. When a {@code baseURL} via the
   * context options was provided and the passed URL is a path, it gets merged via the <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/URL/URL">{@code new URL()}</a> constructor.
   * @param handler handler function to route the request.
   * @since v1.8
   */
  void route(String url, Consumer<Route> handler, RouteOptions options);
  /**
   * Routing provides the capability to modify network requests that are made by a page.
   *
   * <p> Once routing is enabled, every request matching the url pattern will stall unless it's continued, fulfilled or aborted.
   *
   * <p> <strong>NOTE:</strong> The handler will only be called for the first url if the response is a redirect.
   *
   * <p> <strong>NOTE:</strong> {@link Page#route Page.route()} will not intercept requests intercepted by Service Worker. See <a
   * href="https://github.com/microsoft/playwright/issues/1090">this</a> issue. We recommend disabling Service Workers when
   * using request interception by setting {@code Browser.newContext.serviceWorkers} to {@code "block"}.
   *
   * <p> **Usage**
   *
   * <p> An example of a naive handler that aborts all image requests:
   * <pre>{@code
   * Page page = browser.newPage();
   * page.route("**\/*.{png,jpg,jpeg}", route -> route.abort());
   * page.navigate("https://example.com");
   * browser.close();
   * }</pre>
   *
   * <p> or the same snippet using a regex pattern instead:
   * <pre>{@code
   * Page page = browser.newPage();
   * page.route(Pattern.compile("(\\.png$)|(\\.jpg$)"),route -> route.abort());
   * page.navigate("https://example.com");
   * browser.close();
   * }</pre>
   *
   * <p> It is possible to examine the request to decide the route action. For example, mocking all requests that contain some
   * post data, and leaving all other requests as is:
   * <pre>{@code
   * page.route("/api/**", route -> {
   *   if (route.request().postData().contains("my-string"))
   *     route.fulfill(new Route.FulfillOptions().setBody("mocked-data"));
   *   else
   *     route.resume();
   * });
   * }</pre>
   *
   * <p> Page routes take precedence over browser context routes (set up with {@link BrowserContext#route
   * BrowserContext.route()}) when request matches both handlers.
   *
   * <p> To remove a route with its handler you can use {@link Page#unroute Page.unroute()}.
   *
   * <p> <strong>NOTE:</strong> Enabling routing disables http cache.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while routing. When a {@code baseURL} via the
   * context options was provided and the passed URL is a path, it gets merged via the <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/URL/URL">{@code new URL()}</a> constructor.
   * @param handler handler function to route the request.
   * @since v1.8
   */
  default void route(Pattern url, Consumer<Route> handler) {
    route(url, handler, null);
  }
  /**
   * Routing provides the capability to modify network requests that are made by a page.
   *
   * <p> Once routing is enabled, every request matching the url pattern will stall unless it's continued, fulfilled or aborted.
   *
   * <p> <strong>NOTE:</strong> The handler will only be called for the first url if the response is a redirect.
   *
   * <p> <strong>NOTE:</strong> {@link Page#route Page.route()} will not intercept requests intercepted by Service Worker. See <a
   * href="https://github.com/microsoft/playwright/issues/1090">this</a> issue. We recommend disabling Service Workers when
   * using request interception by setting {@code Browser.newContext.serviceWorkers} to {@code "block"}.
   *
   * <p> **Usage**
   *
   * <p> An example of a naive handler that aborts all image requests:
   * <pre>{@code
   * Page page = browser.newPage();
   * page.route("**\/*.{png,jpg,jpeg}", route -> route.abort());
   * page.navigate("https://example.com");
   * browser.close();
   * }</pre>
   *
   * <p> or the same snippet using a regex pattern instead:
   * <pre>{@code
   * Page page = browser.newPage();
   * page.route(Pattern.compile("(\\.png$)|(\\.jpg$)"),route -> route.abort());
   * page.navigate("https://example.com");
   * browser.close();
   * }</pre>
   *
   * <p> It is possible to examine the request to decide the route action. For example, mocking all requests that contain some
   * post data, and leaving all other requests as is:
   * <pre>{@code
   * page.route("/api/**", route -> {
   *   if (route.request().postData().contains("my-string"))
   *     route.fulfill(new Route.FulfillOptions().setBody("mocked-data"));
   *   else
   *     route.resume();
   * });
   * }</pre>
   *
   * <p> Page routes take precedence over browser context routes (set up with {@link BrowserContext#route
   * BrowserContext.route()}) when request matches both handlers.
   *
   * <p> To remove a route with its handler you can use {@link Page#unroute Page.unroute()}.
   *
   * <p> <strong>NOTE:</strong> Enabling routing disables http cache.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while routing. When a {@code baseURL} via the
   * context options was provided and the passed URL is a path, it gets merged via the <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/URL/URL">{@code new URL()}</a> constructor.
   * @param handler handler function to route the request.
   * @since v1.8
   */
  void route(Pattern url, Consumer<Route> handler, RouteOptions options);
  /**
   * Routing provides the capability to modify network requests that are made by a page.
   *
   * <p> Once routing is enabled, every request matching the url pattern will stall unless it's continued, fulfilled or aborted.
   *
   * <p> <strong>NOTE:</strong> The handler will only be called for the first url if the response is a redirect.
   *
   * <p> <strong>NOTE:</strong> {@link Page#route Page.route()} will not intercept requests intercepted by Service Worker. See <a
   * href="https://github.com/microsoft/playwright/issues/1090">this</a> issue. We recommend disabling Service Workers when
   * using request interception by setting {@code Browser.newContext.serviceWorkers} to {@code "block"}.
   *
   * <p> **Usage**
   *
   * <p> An example of a naive handler that aborts all image requests:
   * <pre>{@code
   * Page page = browser.newPage();
   * page.route("**\/*.{png,jpg,jpeg}", route -> route.abort());
   * page.navigate("https://example.com");
   * browser.close();
   * }</pre>
   *
   * <p> or the same snippet using a regex pattern instead:
   * <pre>{@code
   * Page page = browser.newPage();
   * page.route(Pattern.compile("(\\.png$)|(\\.jpg$)"),route -> route.abort());
   * page.navigate("https://example.com");
   * browser.close();
   * }</pre>
   *
   * <p> It is possible to examine the request to decide the route action. For example, mocking all requests that contain some
   * post data, and leaving all other requests as is:
   * <pre>{@code
   * page.route("/api/**", route -> {
   *   if (route.request().postData().contains("my-string"))
   *     route.fulfill(new Route.FulfillOptions().setBody("mocked-data"));
   *   else
   *     route.resume();
   * });
   * }</pre>
   *
   * <p> Page routes take precedence over browser context routes (set up with {@link BrowserContext#route
   * BrowserContext.route()}) when request matches both handlers.
   *
   * <p> To remove a route with its handler you can use {@link Page#unroute Page.unroute()}.
   *
   * <p> <strong>NOTE:</strong> Enabling routing disables http cache.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while routing. When a {@code baseURL} via the
   * context options was provided and the passed URL is a path, it gets merged via the <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/URL/URL">{@code new URL()}</a> constructor.
   * @param handler handler function to route the request.
   * @since v1.8
   */
  default void route(Predicate<String> url, Consumer<Route> handler) {
    route(url, handler, null);
  }
  /**
   * Routing provides the capability to modify network requests that are made by a page.
   *
   * <p> Once routing is enabled, every request matching the url pattern will stall unless it's continued, fulfilled or aborted.
   *
   * <p> <strong>NOTE:</strong> The handler will only be called for the first url if the response is a redirect.
   *
   * <p> <strong>NOTE:</strong> {@link Page#route Page.route()} will not intercept requests intercepted by Service Worker. See <a
   * href="https://github.com/microsoft/playwright/issues/1090">this</a> issue. We recommend disabling Service Workers when
   * using request interception by setting {@code Browser.newContext.serviceWorkers} to {@code "block"}.
   *
   * <p> **Usage**
   *
   * <p> An example of a naive handler that aborts all image requests:
   * <pre>{@code
   * Page page = browser.newPage();
   * page.route("**\/*.{png,jpg,jpeg}", route -> route.abort());
   * page.navigate("https://example.com");
   * browser.close();
   * }</pre>
   *
   * <p> or the same snippet using a regex pattern instead:
   * <pre>{@code
   * Page page = browser.newPage();
   * page.route(Pattern.compile("(\\.png$)|(\\.jpg$)"),route -> route.abort());
   * page.navigate("https://example.com");
   * browser.close();
   * }</pre>
   *
   * <p> It is possible to examine the request to decide the route action. For example, mocking all requests that contain some
   * post data, and leaving all other requests as is:
   * <pre>{@code
   * page.route("/api/**", route -> {
   *   if (route.request().postData().contains("my-string"))
   *     route.fulfill(new Route.FulfillOptions().setBody("mocked-data"));
   *   else
   *     route.resume();
   * });
   * }</pre>
   *
   * <p> Page routes take precedence over browser context routes (set up with {@link BrowserContext#route
   * BrowserContext.route()}) when request matches both handlers.
   *
   * <p> To remove a route with its handler you can use {@link Page#unroute Page.unroute()}.
   *
   * <p> <strong>NOTE:</strong> Enabling routing disables http cache.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while routing. When a {@code baseURL} via the
   * context options was provided and the passed URL is a path, it gets merged via the <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/URL/URL">{@code new URL()}</a> constructor.
   * @param handler handler function to route the request.
   * @since v1.8
   */
  void route(Predicate<String> url, Consumer<Route> handler, RouteOptions options);
  /**
   * If specified the network requests that are made in the page will be served from the HAR file. Read more about <a
   * href="https://playwright.dev/java/docs/mock#replaying-from-har">Replaying from HAR</a>.
   *
   * <p> Playwright will not serve requests intercepted by Service Worker from the HAR file. See <a
   * href="https://github.com/microsoft/playwright/issues/1090">this</a> issue. We recommend disabling Service Workers when
   * using request interception by setting {@code Browser.newContext.serviceWorkers} to {@code "block"}.
   *
   * @param har Path to a <a href="http://www.softwareishard.com/blog/har-12-spec">HAR</a> file with prerecorded network data. If {@code
   * path} is a relative path, then it is resolved relative to the current working directory.
   * @since v1.23
   */
  default void routeFromHAR(Path har) {
    routeFromHAR(har, null);
  }
  /**
   * If specified the network requests that are made in the page will be served from the HAR file. Read more about <a
   * href="https://playwright.dev/java/docs/mock#replaying-from-har">Replaying from HAR</a>.
   *
   * <p> Playwright will not serve requests intercepted by Service Worker from the HAR file. See <a
   * href="https://github.com/microsoft/playwright/issues/1090">this</a> issue. We recommend disabling Service Workers when
   * using request interception by setting {@code Browser.newContext.serviceWorkers} to {@code "block"}.
   *
   * @param har Path to a <a href="http://www.softwareishard.com/blog/har-12-spec">HAR</a> file with prerecorded network data. If {@code
   * path} is a relative path, then it is resolved relative to the current working directory.
   * @since v1.23
   */
  void routeFromHAR(Path har, RouteFromHAROptions options);
  /**
   * Returns the buffer with the captured screenshot.
   *
   * @since v1.8
   */
  default byte[] screenshot() {
    return screenshot(null);
  }
  /**
   * Returns the buffer with the captured screenshot.
   *
   * @since v1.8
   */
  byte[] screenshot(ScreenshotOptions options);
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside
   * the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Single selection matching the value or label
   * page.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * page.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * page.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  default List<String> selectOption(String selector, String values) {
    return selectOption(selector, values, null);
  }
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside
   * the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Single selection matching the value or label
   * page.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * page.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * page.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  List<String> selectOption(String selector, String values, SelectOptionOptions options);
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside
   * the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Single selection matching the value or label
   * page.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * page.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * page.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  default List<String> selectOption(String selector, ElementHandle values) {
    return selectOption(selector, values, null);
  }
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside
   * the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Single selection matching the value or label
   * page.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * page.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * page.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  List<String> selectOption(String selector, ElementHandle values, SelectOptionOptions options);
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside
   * the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Single selection matching the value or label
   * page.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * page.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * page.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  default List<String> selectOption(String selector, String[] values) {
    return selectOption(selector, values, null);
  }
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside
   * the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Single selection matching the value or label
   * page.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * page.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * page.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  List<String> selectOption(String selector, String[] values, SelectOptionOptions options);
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside
   * the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Single selection matching the value or label
   * page.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * page.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * page.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  default List<String> selectOption(String selector, SelectOption values) {
    return selectOption(selector, values, null);
  }
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside
   * the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Single selection matching the value or label
   * page.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * page.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * page.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  List<String> selectOption(String selector, SelectOption values, SelectOptionOptions options);
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside
   * the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Single selection matching the value or label
   * page.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * page.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * page.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  default List<String> selectOption(String selector, ElementHandle[] values) {
    return selectOption(selector, values, null);
  }
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside
   * the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Single selection matching the value or label
   * page.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * page.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * page.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  List<String> selectOption(String selector, ElementHandle[] values, SelectOptionOptions options);
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside
   * the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Single selection matching the value or label
   * page.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * page.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * page.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  default List<String> selectOption(String selector, SelectOption[] values) {
    return selectOption(selector, values, null);
  }
  /**
   * This method waits for an element matching {@code selector}, waits for <a
   * href="https://playwright.dev/java/docs/actionability">actionability</a> checks, waits until all specified options are
   * present in the {@code <select>} element and selects these options.
   *
   * <p> If the target element is not a {@code <select>} element, this method throws an error. However, if the element is inside
   * the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, the control will be used
   * instead.
   *
   * <p> Returns the array of option values that have been successfully selected.
   *
   * <p> Triggers a {@code change} and {@code input} event once all the provided options have been selected.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Single selection matching the value or label
   * page.selectOption("select#colors", "blue");
   * // single selection matching both the value and the label
   * page.selectOption("select#colors", new SelectOption().setLabel("Blue"));
   * // multiple selection
   * page.selectOption("select#colors", new String[] {"red", "green", "blue"});
   * }</pre>
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param values Options to select. If the {@code <select>} has the {@code multiple} attribute, all matching options are selected,
   * otherwise only the first option matching one of the passed options is selected. String values are matching both values
   * and labels. Option is considered matching if all specified properties match.
   * @since v1.8
   */
  List<String> selectOption(String selector, SelectOption[] values, SelectOptionOptions options);
  /**
   * This method checks or unchecks an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Ensure that matched element is a checkbox or a radio input. If not, this method throws.</li>
   * <li> If the element already has the right checked state, this method returns immediately.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * <li> Ensure that the element is now checked or unchecked. If not, this method throws.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param checked Whether to check or uncheck the checkbox.
   * @since v1.15
   */
  default void setChecked(String selector, boolean checked) {
    setChecked(selector, checked, null);
  }
  /**
   * This method checks or unchecks an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Ensure that matched element is a checkbox or a radio input. If not, this method throws.</li>
   * <li> If the element already has the right checked state, this method returns immediately.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * <li> Ensure that the element is now checked or unchecked. If not, this method throws.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param checked Whether to check or uncheck the checkbox.
   * @since v1.15
   */
  void setChecked(String selector, boolean checked, SetCheckedOptions options);
  /**
   * This method internally calls <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/Document/write">document.write()</a>, inheriting all its specific
   * characteristics and behaviors.
   *
   * @param html HTML markup to assign to the page.
   * @since v1.8
   */
  default void setContent(String html) {
    setContent(html, null);
  }
  /**
   * This method internally calls <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/Document/write">document.write()</a>, inheriting all its specific
   * characteristics and behaviors.
   *
   * @param html HTML markup to assign to the page.
   * @since v1.8
   */
  void setContent(String html, SetContentOptions options);
  /**
   * This setting will change the default maximum navigation time for the following methods and related shortcuts:
   * <ul>
   * <li> {@link Page#goBack Page.goBack()}</li>
   * <li> {@link Page#goForward Page.goForward()}</li>
   * <li> {@link Page#navigate Page.navigate()}</li>
   * <li> {@link Page#reload Page.reload()}</li>
   * <li> {@link Page#setContent Page.setContent()}</li>
   * <li> {@link Page#waitForNavigation Page.waitForNavigation()}</li>
   * <li> {@link Page#waitForURL Page.waitForURL()}</li>
   * </ul>
   *
   * <p> <strong>NOTE:</strong> {@link Page#setDefaultNavigationTimeout Page.setDefaultNavigationTimeout()} takes priority over {@link
   * Page#setDefaultTimeout Page.setDefaultTimeout()}, {@link BrowserContext#setDefaultTimeout
   * BrowserContext.setDefaultTimeout()} and {@link BrowserContext#setDefaultNavigationTimeout
   * BrowserContext.setDefaultNavigationTimeout()}.
   *
   * @param timeout Maximum navigation time in milliseconds
   * @since v1.8
   */
  void setDefaultNavigationTimeout(double timeout);
  /**
   * This setting will change the default maximum time for all the methods accepting {@code timeout} option.
   *
   * <p> <strong>NOTE:</strong> {@link Page#setDefaultNavigationTimeout Page.setDefaultNavigationTimeout()} takes priority over {@link
   * Page#setDefaultTimeout Page.setDefaultTimeout()}.
   *
   * @param timeout Maximum time in milliseconds
   * @since v1.8
   */
  void setDefaultTimeout(double timeout);
  /**
   * The extra HTTP headers will be sent with every request the page initiates.
   *
   * <p> <strong>NOTE:</strong> {@link Page#setExtraHTTPHeaders Page.setExtraHTTPHeaders()} does not guarantee the order of headers in the outgoing
   * requests.
   *
   * @param headers An object containing additional HTTP headers to be sent with every request. All header values must be strings.
   * @since v1.8
   */
  void setExtraHTTPHeaders(Map<String, String> headers);
  /**
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then
   * they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code selector} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default void setInputFiles(String selector, Path files) {
    setInputFiles(selector, files, null);
  }
  /**
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then
   * they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code selector} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  void setInputFiles(String selector, Path files, SetInputFilesOptions options);
  /**
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then
   * they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code selector} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default void setInputFiles(String selector, Path[] files) {
    setInputFiles(selector, files, null);
  }
  /**
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then
   * they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code selector} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  void setInputFiles(String selector, Path[] files, SetInputFilesOptions options);
  /**
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then
   * they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code selector} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default void setInputFiles(String selector, FilePayload files) {
    setInputFiles(selector, files, null);
  }
  /**
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then
   * they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code selector} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  void setInputFiles(String selector, FilePayload files, SetInputFilesOptions options);
  /**
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then
   * they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code selector} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default void setInputFiles(String selector, FilePayload[] files) {
    setInputFiles(selector, files, null);
  }
  /**
   * Sets the value of the file input to these file paths or files. If some of the {@code filePaths} are relative paths, then
   * they are resolved relative to the current working directory. For empty array, clears the selected files.
   *
   * <p> This method expects {@code selector} to point to an <a
   * href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input">input element</a>. However, if the element is
   * inside the {@code <label>} element that has an associated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/HTMLLabelElement/control">control</a>, targets the control
   * instead.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  void setInputFiles(String selector, FilePayload[] files, SetInputFilesOptions options);
  /**
   * In the case of multiple pages in a single browser, each page can have its own viewport size. However, {@link
   * Browser#newContext Browser.newContext()} allows to set viewport size (and more) for all pages in the context at once.
   *
   * <p> {@link Page#setViewportSize Page.setViewportSize()} will resize the page. A lot of websites don't expect phones to
   * change size, so you should set the viewport size before navigating to the page. {@link Page#setViewportSize
   * Page.setViewportSize()} will also reset {@code screen} size, use {@link Browser#newContext Browser.newContext()} with
   * {@code screen} and {@code viewport} parameters if you need better control of these properties.
   *
   * <p> **Usage**
   * <pre>{@code
   * Page page = browser.newPage();
   * page.setViewportSize(640, 480);
   * page.navigate("https://example.com");
   * }</pre>
   *
   * @since v1.8
   */
  void setViewportSize(int width, int height);
  /**
   * This method taps an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#touchscreen Page.touchscreen()} to tap the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * <p> <strong>NOTE:</strong> {@link Page#tap Page.tap()} the method will throw if {@code hasTouch} option of the browser context is false.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default void tap(String selector) {
    tap(selector, null);
  }
  /**
   * This method taps an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#touchscreen Page.touchscreen()} to tap the center of the element, or the specified {@code position}.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * <p> <strong>NOTE:</strong> {@link Page#tap Page.tap()} the method will throw if {@code hasTouch} option of the browser context is false.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  void tap(String selector, TapOptions options);
  /**
   * Returns {@code element.textContent}.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default String textContent(String selector) {
    return textContent(selector, null);
  }
  /**
   * Returns {@code element.textContent}.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  String textContent(String selector, TextContentOptions options);
  /**
   * Returns the page's title.
   *
   * @since v1.8
   */
  String title();
  /**
   *
   *
   * @since v1.8
   */
  Touchscreen touchscreen();
  /**
   * @deprecated In most cases, you should use {@link Locator#fill Locator.fill()} instead. You only need to press keys one by one if
   * there is special keyboard handling on the page - in this case use {@link Locator#pressSequentially
   * Locator.pressSequentially()}.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param text A text to type into a focused element.
   * @since v1.8
   */
  default void type(String selector, String text) {
    type(selector, text, null);
  }
  /**
   * @deprecated In most cases, you should use {@link Locator#fill Locator.fill()} instead. You only need to press keys one by one if
   * there is special keyboard handling on the page - in this case use {@link Locator#pressSequentially
   * Locator.pressSequentially()}.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @param text A text to type into a focused element.
   * @since v1.8
   */
  void type(String selector, String text, TypeOptions options);
  /**
   * This method unchecks an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Ensure that matched element is a checkbox or a radio input. If not, this method throws. If the element is already
   * unchecked, this method returns immediately.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * <li> Ensure that the element is now unchecked. If not, this method throws.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  default void uncheck(String selector) {
    uncheck(selector, null);
  }
  /**
   * This method unchecks an element matching {@code selector} by performing the following steps:
   * <ol>
   * <li> Find an element matching {@code selector}. If there is none, wait until a matching element is attached to the DOM.</li>
   * <li> Ensure that matched element is a checkbox or a radio input. If not, this method throws. If the element is already
   * unchecked, this method returns immediately.</li>
   * <li> Wait for <a href="https://playwright.dev/java/docs/actionability">actionability</a> checks on the matched element,
   * unless {@code force} option is set. If the element is detached during the checks, the whole action is retried.</li>
   * <li> Scroll the element into view if needed.</li>
   * <li> Use {@link Page#mouse Page.mouse()} to click in the center of the element.</li>
   * <li> Wait for initiated navigations to either succeed or fail, unless {@code noWaitAfter} option is set.</li>
   * <li> Ensure that the element is now unchecked. If not, this method throws.</li>
   * </ol>
   *
   * <p> When all steps combined have not finished during the specified {@code timeout}, this method throws a {@code
   * TimeoutError}. Passing zero timeout disables this.
   *
   * @param selector A selector to search for an element. If there are multiple elements satisfying the selector, the first will be used.
   * @since v1.8
   */
  void uncheck(String selector, UncheckOptions options);
  /**
   * Removes a route created with {@link Page#route Page.route()}. When {@code handler} is not specified, removes all routes
   * for the {@code url}.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while routing.
   * @since v1.8
   */
  default void unroute(String url) {
    unroute(url, null);
  }
  /**
   * Removes a route created with {@link Page#route Page.route()}. When {@code handler} is not specified, removes all routes
   * for the {@code url}.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while routing.
   * @param handler Optional handler function to route the request.
   * @since v1.8
   */
  void unroute(String url, Consumer<Route> handler);
  /**
   * Removes a route created with {@link Page#route Page.route()}. When {@code handler} is not specified, removes all routes
   * for the {@code url}.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while routing.
   * @since v1.8
   */
  default void unroute(Pattern url) {
    unroute(url, null);
  }
  /**
   * Removes a route created with {@link Page#route Page.route()}. When {@code handler} is not specified, removes all routes
   * for the {@code url}.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while routing.
   * @param handler Optional handler function to route the request.
   * @since v1.8
   */
  void unroute(Pattern url, Consumer<Route> handler);
  /**
   * Removes a route created with {@link Page#route Page.route()}. When {@code handler} is not specified, removes all routes
   * for the {@code url}.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while routing.
   * @since v1.8
   */
  default void unroute(Predicate<String> url) {
    unroute(url, null);
  }
  /**
   * Removes a route created with {@link Page#route Page.route()}. When {@code handler} is not specified, removes all routes
   * for the {@code url}.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while routing.
   * @param handler Optional handler function to route the request.
   * @since v1.8
   */
  void unroute(Predicate<String> url, Consumer<Route> handler);
  /**
   *
   *
   * @since v1.8
   */
  String url();
  /**
   * Video object associated with this page.
   *
   * @since v1.8
   */
  Video video();
  /**
   *
   *
   * @since v1.8
   */
  ViewportSize viewportSize();
  /**
   * Performs action and waits for the Page to close.
   *
   * @param callback Callback that performs the action triggering the event.
   * @since v1.11
   */
  default Page waitForClose(Runnable callback) {
    return waitForClose(null, callback);
  }
  /**
   * Performs action and waits for the Page to close.
   *
   * @param callback Callback that performs the action triggering the event.
   * @since v1.11
   */
  Page waitForClose(WaitForCloseOptions options, Runnable callback);
  /**
   * Performs action and waits for a {@code ConsoleMessage} to be logged by in the page. If predicate is provided, it passes
   * {@code ConsoleMessage} value into the {@code predicate} function and waits for {@code predicate(message)} to return a
   * truthy value. Will throw an error if the page is closed before the {@link Page#onConsoleMessage Page.onConsoleMessage()}
   * event is fired.
   *
   * @param callback Callback that performs the action triggering the event.
   * @since v1.9
   */
  default ConsoleMessage waitForConsoleMessage(Runnable callback) {
    return waitForConsoleMessage(null, callback);
  }
  /**
   * Performs action and waits for a {@code ConsoleMessage} to be logged by in the page. If predicate is provided, it passes
   * {@code ConsoleMessage} value into the {@code predicate} function and waits for {@code predicate(message)} to return a
   * truthy value. Will throw an error if the page is closed before the {@link Page#onConsoleMessage Page.onConsoleMessage()}
   * event is fired.
   *
   * @param callback Callback that performs the action triggering the event.
   * @since v1.9
   */
  ConsoleMessage waitForConsoleMessage(WaitForConsoleMessageOptions options, Runnable callback);
  /**
   * Performs action and waits for a new {@code Download}. If predicate is provided, it passes {@code Download} value into
   * the {@code predicate} function and waits for {@code predicate(download)} to return a truthy value. Will throw an error
   * if the page is closed before the download event is fired.
   *
   * @param callback Callback that performs the action triggering the event.
   * @since v1.9
   */
  default Download waitForDownload(Runnable callback) {
    return waitForDownload(null, callback);
  }
  /**
   * Performs action and waits for a new {@code Download}. If predicate is provided, it passes {@code Download} value into
   * the {@code predicate} function and waits for {@code predicate(download)} to return a truthy value. Will throw an error
   * if the page is closed before the download event is fired.
   *
   * @param callback Callback that performs the action triggering the event.
   * @since v1.9
   */
  Download waitForDownload(WaitForDownloadOptions options, Runnable callback);
  /**
   * Performs action and waits for a new {@code FileChooser} to be created. If predicate is provided, it passes {@code
   * FileChooser} value into the {@code predicate} function and waits for {@code predicate(fileChooser)} to return a truthy
   * value. Will throw an error if the page is closed before the file chooser is opened.
   *
   * @param callback Callback that performs the action triggering the event.
   * @since v1.9
   */
  default FileChooser waitForFileChooser(Runnable callback) {
    return waitForFileChooser(null, callback);
  }
  /**
   * Performs action and waits for a new {@code FileChooser} to be created. If predicate is provided, it passes {@code
   * FileChooser} value into the {@code predicate} function and waits for {@code predicate(fileChooser)} to return a truthy
   * value. Will throw an error if the page is closed before the file chooser is opened.
   *
   * @param callback Callback that performs the action triggering the event.
   * @since v1.9
   */
  FileChooser waitForFileChooser(WaitForFileChooserOptions options, Runnable callback);
  /**
   * Returns when the {@code expression} returns a truthy value. It resolves to a JSHandle of the truthy value.
   *
   * <p> **Usage**
   *
   * <p> The {@link Page#waitForFunction Page.waitForFunction()} can be used to observe viewport size change:
   * <pre>{@code
   * import com.microsoft.playwright.*;
   *
   * public class Example {
   *   public static void main(String[] args) {
   *     try (Playwright playwright = Playwright.create()) {
   *       BrowserType webkit = playwright.webkit();
   *       Browser browser = webkit.launch();
   *       Page page = browser.newPage();
   *       page.setViewportSize(50,  50);
   *       page.waitForFunction("() => window.innerWidth < 100");
   *       browser.close();
   *     }
   *   }
   * }
   * }</pre>
   *
   * <p> To pass an argument to the predicate of {@link Page#waitForFunction Page.waitForFunction()} function:
   * <pre>{@code
   * String selector = ".foo";
   * page.waitForFunction("selector => !!document.querySelector(selector)", selector);
   * }</pre>
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @param arg Optional argument to pass to {@code expression}.
   * @since v1.8
   */
  default JSHandle waitForFunction(String expression, Object arg) {
    return waitForFunction(expression, arg, null);
  }
  /**
   * Returns when the {@code expression} returns a truthy value. It resolves to a JSHandle of the truthy value.
   *
   * <p> **Usage**
   *
   * <p> The {@link Page#waitForFunction Page.waitForFunction()} can be used to observe viewport size change:
   * <pre>{@code
   * import com.microsoft.playwright.*;
   *
   * public class Example {
   *   public static void main(String[] args) {
   *     try (Playwright playwright = Playwright.create()) {
   *       BrowserType webkit = playwright.webkit();
   *       Browser browser = webkit.launch();
   *       Page page = browser.newPage();
   *       page.setViewportSize(50,  50);
   *       page.waitForFunction("() => window.innerWidth < 100");
   *       browser.close();
   *     }
   *   }
   * }
   * }</pre>
   *
   * <p> To pass an argument to the predicate of {@link Page#waitForFunction Page.waitForFunction()} function:
   * <pre>{@code
   * String selector = ".foo";
   * page.waitForFunction("selector => !!document.querySelector(selector)", selector);
   * }</pre>
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @since v1.8
   */
  default JSHandle waitForFunction(String expression) {
    return waitForFunction(expression, null);
  }
  /**
   * Returns when the {@code expression} returns a truthy value. It resolves to a JSHandle of the truthy value.
   *
   * <p> **Usage**
   *
   * <p> The {@link Page#waitForFunction Page.waitForFunction()} can be used to observe viewport size change:
   * <pre>{@code
   * import com.microsoft.playwright.*;
   *
   * public class Example {
   *   public static void main(String[] args) {
   *     try (Playwright playwright = Playwright.create()) {
   *       BrowserType webkit = playwright.webkit();
   *       Browser browser = webkit.launch();
   *       Page page = browser.newPage();
   *       page.setViewportSize(50,  50);
   *       page.waitForFunction("() => window.innerWidth < 100");
   *       browser.close();
   *     }
   *   }
   * }
   * }</pre>
   *
   * <p> To pass an argument to the predicate of {@link Page#waitForFunction Page.waitForFunction()} function:
   * <pre>{@code
   * String selector = ".foo";
   * page.waitForFunction("selector => !!document.querySelector(selector)", selector);
   * }</pre>
   *
   * @param expression JavaScript expression to be evaluated in the browser context. If the expression evaluates to a function, the function is
   * automatically invoked.
   * @param arg Optional argument to pass to {@code expression}.
   * @since v1.8
   */
  JSHandle waitForFunction(String expression, Object arg, WaitForFunctionOptions options);
  /**
   * Returns when the required load state has been reached.
   *
   * <p> This resolves when the page reaches a required load state, {@code load} by default. The navigation must have been
   * committed when this method is called. If current document has already reached the required state, resolves immediately.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.getByRole(AriaRole.BUTTON).click(); // Click triggers navigation.
   * page.waitForLoadState(); // The promise resolves after "load" event.
   * }</pre>
   * <pre>{@code
   * Page popup = page.waitForPopup(() -> {
   *   page.getByRole(AriaRole.BUTTON).click(); // Click triggers a popup.
   * });
   * // Wait for the "DOMContentLoaded" event
   * popup.waitForLoadState(LoadState.DOMCONTENTLOADED);
   * System.out.println(popup.title()); // Popup is ready to use.
   * }</pre>
   *
   * @param state Optional load state to wait for, defaults to {@code load}. If the state has been already reached while loading current
   * document, the method resolves immediately. Can be one of:
   * <ul>
   * <li> {@code "load"} - wait for the {@code load} event to be fired.</li>
   * <li> {@code "domcontentloaded"} - wait for the {@code DOMContentLoaded} event to be fired.</li>
   * <li> {@code "networkidle"} - **DISCOURAGED** wait until there are no network connections for at least {@code 500} ms. Don't
   * use this method for testing, rely on web assertions to assess readiness instead.</li>
   * </ul>
   * @since v1.8
   */
  default void waitForLoadState(LoadState state) {
    waitForLoadState(state, null);
  }
  /**
   * Returns when the required load state has been reached.
   *
   * <p> This resolves when the page reaches a required load state, {@code load} by default. The navigation must have been
   * committed when this method is called. If current document has already reached the required state, resolves immediately.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.getByRole(AriaRole.BUTTON).click(); // Click triggers navigation.
   * page.waitForLoadState(); // The promise resolves after "load" event.
   * }</pre>
   * <pre>{@code
   * Page popup = page.waitForPopup(() -> {
   *   page.getByRole(AriaRole.BUTTON).click(); // Click triggers a popup.
   * });
   * // Wait for the "DOMContentLoaded" event
   * popup.waitForLoadState(LoadState.DOMCONTENTLOADED);
   * System.out.println(popup.title()); // Popup is ready to use.
   * }</pre>
   *
   * @since v1.8
   */
  default void waitForLoadState() {
    waitForLoadState(null);
  }
  /**
   * Returns when the required load state has been reached.
   *
   * <p> This resolves when the page reaches a required load state, {@code load} by default. The navigation must have been
   * committed when this method is called. If current document has already reached the required state, resolves immediately.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.getByRole(AriaRole.BUTTON).click(); // Click triggers navigation.
   * page.waitForLoadState(); // The promise resolves after "load" event.
   * }</pre>
   * <pre>{@code
   * Page popup = page.waitForPopup(() -> {
   *   page.getByRole(AriaRole.BUTTON).click(); // Click triggers a popup.
   * });
   * // Wait for the "DOMContentLoaded" event
   * popup.waitForLoadState(LoadState.DOMCONTENTLOADED);
   * System.out.println(popup.title()); // Popup is ready to use.
   * }</pre>
   *
   * @param state Optional load state to wait for, defaults to {@code load}. If the state has been already reached while loading current
   * document, the method resolves immediately. Can be one of:
   * <ul>
   * <li> {@code "load"} - wait for the {@code load} event to be fired.</li>
   * <li> {@code "domcontentloaded"} - wait for the {@code DOMContentLoaded} event to be fired.</li>
   * <li> {@code "networkidle"} - **DISCOURAGED** wait until there are no network connections for at least {@code 500} ms. Don't
   * use this method for testing, rely on web assertions to assess readiness instead.</li>
   * </ul>
   * @since v1.8
   */
  void waitForLoadState(LoadState state, WaitForLoadStateOptions options);
  /**
   * @deprecated This method is inherently racy, please use {@link Page#waitForURL Page.waitForURL()} instead.
   *
   * @param callback Callback that performs the action triggering the event.
   * @since v1.8
   */
  default Response waitForNavigation(Runnable callback) {
    return waitForNavigation(null, callback);
  }
  /**
   * @deprecated This method is inherently racy, please use {@link Page#waitForURL Page.waitForURL()} instead.
   *
   * @param callback Callback that performs the action triggering the event.
   * @since v1.8
   */
  Response waitForNavigation(WaitForNavigationOptions options, Runnable callback);
  /**
   * Performs action and waits for a popup {@code Page}. If predicate is provided, it passes [Popup] value into the {@code
   * predicate} function and waits for {@code predicate(page)} to return a truthy value. Will throw an error if the page is
   * closed before the popup event is fired.
   *
   * @param callback Callback that performs the action triggering the event.
   * @since v1.9
   */
  default Page waitForPopup(Runnable callback) {
    return waitForPopup(null, callback);
  }
  /**
   * Performs action and waits for a popup {@code Page}. If predicate is provided, it passes [Popup] value into the {@code
   * predicate} function and waits for {@code predicate(page)} to return a truthy value. Will throw an error if the page is
   * closed before the popup event is fired.
   *
   * @param callback Callback that performs the action triggering the event.
   * @since v1.9
   */
  Page waitForPopup(WaitForPopupOptions options, Runnable callback);
  /**
   * Waits for the matching request and returns it. See <a
   * href="https://playwright.dev/java/docs/events#waiting-for-event">waiting for event</a> for more details about events.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Waits for the next request with the specified url
   * Request request = page.waitForRequest("https://example.com/resource", () -> {
   *   // Triggers the request
   *   page.getByText("trigger request").click();
   * });
   *
   * // Waits for the next request matching some conditions
   * Request request = page.waitForRequest(request -> "https://example.com".equals(request.url()) && "GET".equals(request.method()), () -> {
   *   // Triggers the request
   *   page.getByText("trigger request").click();
   * });
   * }</pre>
   *
   * @param urlOrPredicate Request URL string, regex or predicate receiving {@code Request} object. When a {@code baseURL} via the context options
   * was provided and the passed URL is a path, it gets merged via the <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/URL/URL">{@code new URL()}</a> constructor.
   * @param callback Callback that performs the action triggering the event.
   * @since v1.8
   */
  default Request waitForRequest(String urlOrPredicate, Runnable callback) {
    return waitForRequest(urlOrPredicate, null, callback);
  }
  /**
   * Waits for the matching request and returns it. See <a
   * href="https://playwright.dev/java/docs/events#waiting-for-event">waiting for event</a> for more details about events.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Waits for the next request with the specified url
   * Request request = page.waitForRequest("https://example.com/resource", () -> {
   *   // Triggers the request
   *   page.getByText("trigger request").click();
   * });
   *
   * // Waits for the next request matching some conditions
   * Request request = page.waitForRequest(request -> "https://example.com".equals(request.url()) && "GET".equals(request.method()), () -> {
   *   // Triggers the request
   *   page.getByText("trigger request").click();
   * });
   * }</pre>
   *
   * @param urlOrPredicate Request URL string, regex or predicate receiving {@code Request} object. When a {@code baseURL} via the context options
   * was provided and the passed URL is a path, it gets merged via the <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/URL/URL">{@code new URL()}</a> constructor.
   * @param callback Callback that performs the action triggering the event.
   * @since v1.8
   */
  Request waitForRequest(String urlOrPredicate, WaitForRequestOptions options, Runnable callback);
  /**
   * Waits for the matching request and returns it. See <a
   * href="https://playwright.dev/java/docs/events#waiting-for-event">waiting for event</a> for more details about events.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Waits for the next request with the specified url
   * Request request = page.waitForRequest("https://example.com/resource", () -> {
   *   // Triggers the request
   *   page.getByText("trigger request").click();
   * });
   *
   * // Waits for the next request matching some conditions
   * Request request = page.waitForRequest(request -> "https://example.com".equals(request.url()) && "GET".equals(request.method()), () -> {
   *   // Triggers the request
   *   page.getByText("trigger request").click();
   * });
   * }</pre>
   *
   * @param urlOrPredicate Request URL string, regex or predicate receiving {@code Request} object. When a {@code baseURL} via the context options
   * was provided and the passed URL is a path, it gets merged via the <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/URL/URL">{@code new URL()}</a> constructor.
   * @param callback Callback that performs the action triggering the event.
   * @since v1.8
   */
  default Request waitForRequest(Pattern urlOrPredicate, Runnable callback) {
    return waitForRequest(urlOrPredicate, null, callback);
  }
  /**
   * Waits for the matching request and returns it. See <a
   * href="https://playwright.dev/java/docs/events#waiting-for-event">waiting for event</a> for more details about events.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Waits for the next request with the specified url
   * Request request = page.waitForRequest("https://example.com/resource", () -> {
   *   // Triggers the request
   *   page.getByText("trigger request").click();
   * });
   *
   * // Waits for the next request matching some conditions
   * Request request = page.waitForRequest(request -> "https://example.com".equals(request.url()) && "GET".equals(request.method()), () -> {
   *   // Triggers the request
   *   page.getByText("trigger request").click();
   * });
   * }</pre>
   *
   * @param urlOrPredicate Request URL string, regex or predicate receiving {@code Request} object. When a {@code baseURL} via the context options
   * was provided and the passed URL is a path, it gets merged via the <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/URL/URL">{@code new URL()}</a> constructor.
   * @param callback Callback that performs the action triggering the event.
   * @since v1.8
   */
  Request waitForRequest(Pattern urlOrPredicate, WaitForRequestOptions options, Runnable callback);
  /**
   * Waits for the matching request and returns it. See <a
   * href="https://playwright.dev/java/docs/events#waiting-for-event">waiting for event</a> for more details about events.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Waits for the next request with the specified url
   * Request request = page.waitForRequest("https://example.com/resource", () -> {
   *   // Triggers the request
   *   page.getByText("trigger request").click();
   * });
   *
   * // Waits for the next request matching some conditions
   * Request request = page.waitForRequest(request -> "https://example.com".equals(request.url()) && "GET".equals(request.method()), () -> {
   *   // Triggers the request
   *   page.getByText("trigger request").click();
   * });
   * }</pre>
   *
   * @param urlOrPredicate Request URL string, regex or predicate receiving {@code Request} object. When a {@code baseURL} via the context options
   * was provided and the passed URL is a path, it gets merged via the <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/URL/URL">{@code new URL()}</a> constructor.
   * @param callback Callback that performs the action triggering the event.
   * @since v1.8
   */
  default Request waitForRequest(Predicate<Request> urlOrPredicate, Runnable callback) {
    return waitForRequest(urlOrPredicate, null, callback);
  }
  /**
   * Waits for the matching request and returns it. See <a
   * href="https://playwright.dev/java/docs/events#waiting-for-event">waiting for event</a> for more details about events.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Waits for the next request with the specified url
   * Request request = page.waitForRequest("https://example.com/resource", () -> {
   *   // Triggers the request
   *   page.getByText("trigger request").click();
   * });
   *
   * // Waits for the next request matching some conditions
   * Request request = page.waitForRequest(request -> "https://example.com".equals(request.url()) && "GET".equals(request.method()), () -> {
   *   // Triggers the request
   *   page.getByText("trigger request").click();
   * });
   * }</pre>
   *
   * @param urlOrPredicate Request URL string, regex or predicate receiving {@code Request} object. When a {@code baseURL} via the context options
   * was provided and the passed URL is a path, it gets merged via the <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/URL/URL">{@code new URL()}</a> constructor.
   * @param callback Callback that performs the action triggering the event.
   * @since v1.8
   */
  Request waitForRequest(Predicate<Request> urlOrPredicate, WaitForRequestOptions options, Runnable callback);
  /**
   * Performs action and waits for a {@code Request} to finish loading. If predicate is provided, it passes {@code Request}
   * value into the {@code predicate} function and waits for {@code predicate(request)} to return a truthy value. Will throw
   * an error if the page is closed before the {@link Page#onRequestFinished Page.onRequestFinished()} event is fired.
   *
   * @param callback Callback that performs the action triggering the event.
   * @since v1.12
   */
  default Request waitForRequestFinished(Runnable callback) {
    return waitForRequestFinished(null, callback);
  }
  /**
   * Performs action and waits for a {@code Request} to finish loading. If predicate is provided, it passes {@code Request}
   * value into the {@code predicate} function and waits for {@code predicate(request)} to return a truthy value. Will throw
   * an error if the page is closed before the {@link Page#onRequestFinished Page.onRequestFinished()} event is fired.
   *
   * @param callback Callback that performs the action triggering the event.
   * @since v1.12
   */
  Request waitForRequestFinished(WaitForRequestFinishedOptions options, Runnable callback);
  /**
   * Returns the matched response. See <a href="https://playwright.dev/java/docs/events#waiting-for-event">waiting for
   * event</a> for more details about events.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Waits for the next response with the specified url
   * Response response = page.waitForResponse("https://example.com/resource", () -> {
   *   // Triggers the response
   *   page.getByText("trigger response").click();
   * });
   *
   * // Waits for the next response matching some conditions
   * Response response = page.waitForResponse(response -> "https://example.com".equals(response.url()) && response.status() == 200, () -> {
   *   // Triggers the response
   *   page.getByText("trigger response").click();
   * });
   * }</pre>
   *
   * @param urlOrPredicate Request URL string, regex or predicate receiving {@code Response} object. When a {@code baseURL} via the context options
   * was provided and the passed URL is a path, it gets merged via the <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/URL/URL">{@code new URL()}</a> constructor.
   * @param callback Callback that performs the action triggering the event.
   * @since v1.8
   */
  default Response waitForResponse(String urlOrPredicate, Runnable callback) {
    return waitForResponse(urlOrPredicate, null, callback);
  }
  /**
   * Returns the matched response. See <a href="https://playwright.dev/java/docs/events#waiting-for-event">waiting for
   * event</a> for more details about events.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Waits for the next response with the specified url
   * Response response = page.waitForResponse("https://example.com/resource", () -> {
   *   // Triggers the response
   *   page.getByText("trigger response").click();
   * });
   *
   * // Waits for the next response matching some conditions
   * Response response = page.waitForResponse(response -> "https://example.com".equals(response.url()) && response.status() == 200, () -> {
   *   // Triggers the response
   *   page.getByText("trigger response").click();
   * });
   * }</pre>
   *
   * @param urlOrPredicate Request URL string, regex or predicate receiving {@code Response} object. When a {@code baseURL} via the context options
   * was provided and the passed URL is a path, it gets merged via the <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/URL/URL">{@code new URL()}</a> constructor.
   * @param callback Callback that performs the action triggering the event.
   * @since v1.8
   */
  Response waitForResponse(String urlOrPredicate, WaitForResponseOptions options, Runnable callback);
  /**
   * Returns the matched response. See <a href="https://playwright.dev/java/docs/events#waiting-for-event">waiting for
   * event</a> for more details about events.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Waits for the next response with the specified url
   * Response response = page.waitForResponse("https://example.com/resource", () -> {
   *   // Triggers the response
   *   page.getByText("trigger response").click();
   * });
   *
   * // Waits for the next response matching some conditions
   * Response response = page.waitForResponse(response -> "https://example.com".equals(response.url()) && response.status() == 200, () -> {
   *   // Triggers the response
   *   page.getByText("trigger response").click();
   * });
   * }</pre>
   *
   * @param urlOrPredicate Request URL string, regex or predicate receiving {@code Response} object. When a {@code baseURL} via the context options
   * was provided and the passed URL is a path, it gets merged via the <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/URL/URL">{@code new URL()}</a> constructor.
   * @param callback Callback that performs the action triggering the event.
   * @since v1.8
   */
  default Response waitForResponse(Pattern urlOrPredicate, Runnable callback) {
    return waitForResponse(urlOrPredicate, null, callback);
  }
  /**
   * Returns the matched response. See <a href="https://playwright.dev/java/docs/events#waiting-for-event">waiting for
   * event</a> for more details about events.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Waits for the next response with the specified url
   * Response response = page.waitForResponse("https://example.com/resource", () -> {
   *   // Triggers the response
   *   page.getByText("trigger response").click();
   * });
   *
   * // Waits for the next response matching some conditions
   * Response response = page.waitForResponse(response -> "https://example.com".equals(response.url()) && response.status() == 200, () -> {
   *   // Triggers the response
   *   page.getByText("trigger response").click();
   * });
   * }</pre>
   *
   * @param urlOrPredicate Request URL string, regex or predicate receiving {@code Response} object. When a {@code baseURL} via the context options
   * was provided and the passed URL is a path, it gets merged via the <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/URL/URL">{@code new URL()}</a> constructor.
   * @param callback Callback that performs the action triggering the event.
   * @since v1.8
   */
  Response waitForResponse(Pattern urlOrPredicate, WaitForResponseOptions options, Runnable callback);
  /**
   * Returns the matched response. See <a href="https://playwright.dev/java/docs/events#waiting-for-event">waiting for
   * event</a> for more details about events.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Waits for the next response with the specified url
   * Response response = page.waitForResponse("https://example.com/resource", () -> {
   *   // Triggers the response
   *   page.getByText("trigger response").click();
   * });
   *
   * // Waits for the next response matching some conditions
   * Response response = page.waitForResponse(response -> "https://example.com".equals(response.url()) && response.status() == 200, () -> {
   *   // Triggers the response
   *   page.getByText("trigger response").click();
   * });
   * }</pre>
   *
   * @param urlOrPredicate Request URL string, regex or predicate receiving {@code Response} object. When a {@code baseURL} via the context options
   * was provided and the passed URL is a path, it gets merged via the <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/URL/URL">{@code new URL()}</a> constructor.
   * @param callback Callback that performs the action triggering the event.
   * @since v1.8
   */
  default Response waitForResponse(Predicate<Response> urlOrPredicate, Runnable callback) {
    return waitForResponse(urlOrPredicate, null, callback);
  }
  /**
   * Returns the matched response. See <a href="https://playwright.dev/java/docs/events#waiting-for-event">waiting for
   * event</a> for more details about events.
   *
   * <p> **Usage**
   * <pre>{@code
   * // Waits for the next response with the specified url
   * Response response = page.waitForResponse("https://example.com/resource", () -> {
   *   // Triggers the response
   *   page.getByText("trigger response").click();
   * });
   *
   * // Waits for the next response matching some conditions
   * Response response = page.waitForResponse(response -> "https://example.com".equals(response.url()) && response.status() == 200, () -> {
   *   // Triggers the response
   *   page.getByText("trigger response").click();
   * });
   * }</pre>
   *
   * @param urlOrPredicate Request URL string, regex or predicate receiving {@code Response} object. When a {@code baseURL} via the context options
   * was provided and the passed URL is a path, it gets merged via the <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/URL/URL">{@code new URL()}</a> constructor.
   * @param callback Callback that performs the action triggering the event.
   * @since v1.8
   */
  Response waitForResponse(Predicate<Response> urlOrPredicate, WaitForResponseOptions options, Runnable callback);
  /**
   * Returns when element specified by selector satisfies {@code state} option. Returns {@code null} if waiting for {@code
   * hidden} or {@code detached}.
   *
   * <p> <strong>NOTE:</strong> Playwright automatically waits for element to be ready before performing an action. Using {@code Locator} objects and
   * web-first assertions makes the code wait-for-selector-free.
   *
   * <p> Wait for the {@code selector} to satisfy {@code state} option (either appear/disappear from dom, or become
   * visible/hidden). If at the moment of calling the method {@code selector} already satisfies the condition, the method
   * will return immediately. If the selector doesn't satisfy the condition for the {@code timeout} milliseconds, the
   * function will throw.
   *
   * <p> **Usage**
   *
   * <p> This method works across navigations:
   * <pre>{@code
   * import com.microsoft.playwright.*;
   *
   * public class Example {
   *   public static void main(String[] args) {
   *     try (Playwright playwright = Playwright.create()) {
   *       BrowserType chromium = playwright.chromium();
   *       Browser browser = chromium.launch();
   *       Page page = browser.newPage();
   *       for (String currentURL : Arrays.asList("https://google.com", "https://bbc.com")) {
   *         page.navigate(currentURL);
   *         ElementHandle element = page.waitForSelector("img");
   *         System.out.println("Loaded image: " + element.getAttribute("src"));
   *       }
   *       browser.close();
   *     }
   *   }
   * }
   * }</pre>
   *
   * @param selector A selector to query for.
   * @since v1.8
   */
  default ElementHandle waitForSelector(String selector) {
    return waitForSelector(selector, null);
  }
  /**
   * Returns when element specified by selector satisfies {@code state} option. Returns {@code null} if waiting for {@code
   * hidden} or {@code detached}.
   *
   * <p> <strong>NOTE:</strong> Playwright automatically waits for element to be ready before performing an action. Using {@code Locator} objects and
   * web-first assertions makes the code wait-for-selector-free.
   *
   * <p> Wait for the {@code selector} to satisfy {@code state} option (either appear/disappear from dom, or become
   * visible/hidden). If at the moment of calling the method {@code selector} already satisfies the condition, the method
   * will return immediately. If the selector doesn't satisfy the condition for the {@code timeout} milliseconds, the
   * function will throw.
   *
   * <p> **Usage**
   *
   * <p> This method works across navigations:
   * <pre>{@code
   * import com.microsoft.playwright.*;
   *
   * public class Example {
   *   public static void main(String[] args) {
   *     try (Playwright playwright = Playwright.create()) {
   *       BrowserType chromium = playwright.chromium();
   *       Browser browser = chromium.launch();
   *       Page page = browser.newPage();
   *       for (String currentURL : Arrays.asList("https://google.com", "https://bbc.com")) {
   *         page.navigate(currentURL);
   *         ElementHandle element = page.waitForSelector("img");
   *         System.out.println("Loaded image: " + element.getAttribute("src"));
   *       }
   *       browser.close();
   *     }
   *   }
   * }
   * }</pre>
   *
   * @param selector A selector to query for.
   * @since v1.8
   */
  ElementHandle waitForSelector(String selector, WaitForSelectorOptions options);
  /**
   * The method will block until the condition returns true. All Playwright events will be dispatched while the method is
   * waiting for the condition.
   *
   * <p> **Usage**
   *
   * <p> Use the method to wait for a condition that depends on page events:
   * <pre>{@code
   * List<String> messages = new ArrayList<>();
   * page.onConsoleMessage(m -> messages.add(m.text()));
   * page.getByText("Submit button").click();
   * page.waitForCondition(() -> messages.size() > 3);
   * }</pre>
   *
   * @param condition Condition to wait for.
   * @since v1.32
   */
  default void waitForCondition(BooleanSupplier condition) {
    waitForCondition(condition, null);
  }
  /**
   * The method will block until the condition returns true. All Playwright events will be dispatched while the method is
   * waiting for the condition.
   *
   * <p> **Usage**
   *
   * <p> Use the method to wait for a condition that depends on page events:
   * <pre>{@code
   * List<String> messages = new ArrayList<>();
   * page.onConsoleMessage(m -> messages.add(m.text()));
   * page.getByText("Submit button").click();
   * page.waitForCondition(() -> messages.size() > 3);
   * }</pre>
   *
   * @param condition Condition to wait for.
   * @since v1.32
   */
  void waitForCondition(BooleanSupplier condition, WaitForConditionOptions options);
  /**
   * Waits for the given {@code timeout} in milliseconds.
   *
   * <p> Note that {@code page.waitForTimeout()} should only be used for debugging. Tests using the timer in production are going
   * to be flaky. Use signals such as network events, selectors becoming visible and others instead.
   *
   * <p> **Usage**
   * <pre>{@code
   * // wait for 1 second
   * page.waitForTimeout(1000);
   * }</pre>
   *
   * @param timeout A timeout to wait for
   * @since v1.8
   */
  void waitForTimeout(double timeout);
  /**
   * Waits for the main frame to navigate to the given URL.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
   * page.waitForURL("**\/target.html");
   * }</pre>
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation. Note that if the
   * parameter is a string without wildcard characters, the method will wait for navigation to URL that is exactly equal to
   * the string.
   * @since v1.11
   */
  default void waitForURL(String url) {
    waitForURL(url, null);
  }
  /**
   * Waits for the main frame to navigate to the given URL.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
   * page.waitForURL("**\/target.html");
   * }</pre>
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation. Note that if the
   * parameter is a string without wildcard characters, the method will wait for navigation to URL that is exactly equal to
   * the string.
   * @since v1.11
   */
  void waitForURL(String url, WaitForURLOptions options);
  /**
   * Waits for the main frame to navigate to the given URL.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
   * page.waitForURL("**\/target.html");
   * }</pre>
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation. Note that if the
   * parameter is a string without wildcard characters, the method will wait for navigation to URL that is exactly equal to
   * the string.
   * @since v1.11
   */
  default void waitForURL(Pattern url) {
    waitForURL(url, null);
  }
  /**
   * Waits for the main frame to navigate to the given URL.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
   * page.waitForURL("**\/target.html");
   * }</pre>
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation. Note that if the
   * parameter is a string without wildcard characters, the method will wait for navigation to URL that is exactly equal to
   * the string.
   * @since v1.11
   */
  void waitForURL(Pattern url, WaitForURLOptions options);
  /**
   * Waits for the main frame to navigate to the given URL.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
   * page.waitForURL("**\/target.html");
   * }</pre>
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation. Note that if the
   * parameter is a string without wildcard characters, the method will wait for navigation to URL that is exactly equal to
   * the string.
   * @since v1.11
   */
  default void waitForURL(Predicate<String> url) {
    waitForURL(url, null);
  }
  /**
   * Waits for the main frame to navigate to the given URL.
   *
   * <p> **Usage**
   * <pre>{@code
   * page.click("a.delayed-navigation"); // Clicking the link will indirectly cause a navigation
   * page.waitForURL("**\/target.html");
   * }</pre>
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while waiting for the navigation. Note that if the
   * parameter is a string without wildcard characters, the method will wait for navigation to URL that is exactly equal to
   * the string.
   * @since v1.11
   */
  void waitForURL(Predicate<String> url, WaitForURLOptions options);
  /**
   * Performs action and waits for a new {@code WebSocket}. If predicate is provided, it passes {@code WebSocket} value into
   * the {@code predicate} function and waits for {@code predicate(webSocket)} to return a truthy value. Will throw an error
   * if the page is closed before the WebSocket event is fired.
   *
   * @param callback Callback that performs the action triggering the event.
   * @since v1.9
   */
  default WebSocket waitForWebSocket(Runnable callback) {
    return waitForWebSocket(null, callback);
  }
  /**
   * Performs action and waits for a new {@code WebSocket}. If predicate is provided, it passes {@code WebSocket} value into
   * the {@code predicate} function and waits for {@code predicate(webSocket)} to return a truthy value. Will throw an error
   * if the page is closed before the WebSocket event is fired.
   *
   * @param callback Callback that performs the action triggering the event.
   * @since v1.9
   */
  WebSocket waitForWebSocket(WaitForWebSocketOptions options, Runnable callback);
  /**
   * Performs action and waits for a new {@code Worker}. If predicate is provided, it passes {@code Worker} value into the
   * {@code predicate} function and waits for {@code predicate(worker)} to return a truthy value. Will throw an error if the
   * page is closed before the worker event is fired.
   *
   * @param callback Callback that performs the action triggering the event.
   * @since v1.9
   */
  default Worker waitForWorker(Runnable callback) {
    return waitForWorker(null, callback);
  }
  /**
   * Performs action and waits for a new {@code Worker}. If predicate is provided, it passes {@code Worker} value into the
   * {@code predicate} function and waits for {@code predicate(worker)} to return a truthy value. Will throw an error if the
   * page is closed before the worker event is fired.
   *
   * @param callback Callback that performs the action triggering the event.
   * @since v1.9
   */
  Worker waitForWorker(WaitForWorkerOptions options, Runnable callback);
  /**
   * This method returns all of the dedicated <a
   * href="https://developer.mozilla.org/en-US/docs/Web/API/Web_Workers_API">WebWorkers</a> associated with the page.
   *
   * <p> <strong>NOTE:</strong> This does not contain ServiceWorkers
   *
   * @since v1.8
   */
  List<Worker> workers();
}

