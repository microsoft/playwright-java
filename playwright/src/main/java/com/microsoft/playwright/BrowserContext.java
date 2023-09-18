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
 * BrowserContexts provide a way to operate multiple independent browser sessions.
 *
 * <p> If a page opens another page, e.g. with a {@code window.open} call, the popup will belong to the parent page's browser
 * context.
 *
 * <p> Playwright allows creating "incognito" browser contexts with {@link Browser#newContext Browser.newContext()} method.
 * "Incognito" browser contexts don't write any browsing data to disk.
 * <pre>{@code
 * // Create a new incognito browser context
 * BrowserContext context = browser.newContext();
 * // Create a new page inside context.
 * Page page = context.newPage();
 * page.navigate("https://example.com");
 * // Dispose context once it is no longer needed.
 * context.close();
 * }</pre>
 */
public interface BrowserContext extends AutoCloseable {

  /**
   * Emitted when Browser context gets closed. This might happen because of one of the following:
   * <ul>
   * <li> Browser context is closed.</li>
   * <li> Browser application is closed or crashed.</li>
   * <li> The {@link Browser#close Browser.close()} method was called.</li>
   * </ul>
   */
  void onClose(Consumer<BrowserContext> handler);
  /**
   * Removes handler that was previously added with {@link #onClose onClose(handler)}.
   */
  void offClose(Consumer<BrowserContext> handler);

  /**
   * Emitted when JavaScript within the page calls one of console API methods, e.g. {@code console.log} or {@code
   * console.dir}. Also emitted if the page throws an error or a warning.
   *
   * <p> The arguments passed into {@code console.log} and the page are available on the {@code ConsoleMessage} event handler
   * argument.
   *
   * <p> **Usage**
   * <pre>{@code
   * context.onConsoleMessage(msg -> {
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
   * Emitted when a JavaScript dialog appears, such as {@code alert}, {@code prompt}, {@code confirm} or {@code
   * beforeunload}. Listener **must** either {@link Dialog#accept Dialog.accept()} or {@link Dialog#dismiss Dialog.dismiss()}
   * the dialog - otherwise the page will <a
   * href="https://developer.mozilla.org/en-US/docs/Web/JavaScript/EventLoop#never_blocking">freeze</a> waiting for the
   * dialog, and actions like click will never finish.
   *
   * <p> **Usage**
   * <pre>{@code
   * context.onDialog(dialog -> {
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
   * The event is emitted when a new Page is created in the BrowserContext. The page may still be loading. The event will
   * also fire for popup pages. See also {@link Page#onPopup Page.onPopup()} to receive events about popups relevant to a
   * specific page.
   *
   * <p> The earliest moment that page is available is when it has navigated to the initial url. For example, when opening a
   * popup with {@code window.open('http://example.com')}, this event will fire when the network request to
   * "http://example.com" is done and its response has started loading in the popup.
   * <pre>{@code
   * Page newPage = context.waitForPage(() -> {
   *   page.getByText("open new page").click();
   * });
   * System.out.println(newPage.evaluate("location.href"));
   * }</pre>
   *
   * <p> <strong>NOTE:</strong> Use {@link Page#waitForLoadState Page.waitForLoadState()} to wait until the page gets to a particular state (you should
   * not need it in most cases).
   */
  void onPage(Consumer<Page> handler);
  /**
   * Removes handler that was previously added with {@link #onPage onPage(handler)}.
   */
  void offPage(Consumer<Page> handler);

  /**
   * Emitted when exception is unhandled in any of the pages in this context. To listen for errors from a particular page,
   * use {@link Page#onPageError Page.onPageError()} instead.
   */
  void onWebError(Consumer<WebError> handler);
  /**
   * Removes handler that was previously added with {@link #onWebError onWebError(handler)}.
   */
  void offWebError(Consumer<WebError> handler);

  /**
   * Emitted when a request is issued from any pages created through this context. The [request] object is read-only. To only
   * listen for requests from a particular page, use {@link Page#onRequest Page.onRequest()}.
   *
   * <p> In order to intercept and mutate requests, see {@link BrowserContext#route BrowserContext.route()} or {@link Page#route
   * Page.route()}.
   */
  void onRequest(Consumer<Request> handler);
  /**
   * Removes handler that was previously added with {@link #onRequest onRequest(handler)}.
   */
  void offRequest(Consumer<Request> handler);

  /**
   * Emitted when a request fails, for example by timing out. To only listen for failed requests from a particular page, use
   * {@link Page#onRequestFailed Page.onRequestFailed()}.
   *
   * <p> <strong>NOTE:</strong> HTTP Error responses, such as 404 or 503, are still successful responses from HTTP standpoint, so request will complete
   * with {@link BrowserContext#onRequestFinished BrowserContext.onRequestFinished()} event and not with {@link
   * BrowserContext#onRequestFailed BrowserContext.onRequestFailed()}.
   */
  void onRequestFailed(Consumer<Request> handler);
  /**
   * Removes handler that was previously added with {@link #onRequestFailed onRequestFailed(handler)}.
   */
  void offRequestFailed(Consumer<Request> handler);

  /**
   * Emitted when a request finishes successfully after downloading the response body. For a successful response, the
   * sequence of events is {@code request}, {@code response} and {@code requestfinished}. To listen for successful requests
   * from a particular page, use {@link Page#onRequestFinished Page.onRequestFinished()}.
   */
  void onRequestFinished(Consumer<Request> handler);
  /**
   * Removes handler that was previously added with {@link #onRequestFinished onRequestFinished(handler)}.
   */
  void offRequestFinished(Consumer<Request> handler);

  /**
   * Emitted when [response] status and headers are received for a request. For a successful response, the sequence of events
   * is {@code request}, {@code response} and {@code requestfinished}. To listen for response events from a particular page,
   * use {@link Page#onResponse Page.onResponse()}.
   */
  void onResponse(Consumer<Response> handler);
  /**
   * Removes handler that was previously added with {@link #onResponse onResponse(handler)}.
   */
  void offResponse(Consumer<Response> handler);

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
  class GrantPermissionsOptions {
    /**
     * The [origin] to grant permissions to, e.g. "https://example.com".
     */
    public String origin;

    /**
     * The [origin] to grant permissions to, e.g. "https://example.com".
     */
    public GrantPermissionsOptions setOrigin(String origin) {
      this.origin = origin;
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
     * <li> If set to 'fallback' falls through to the next route handler in the handler chain.</li>
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
     * minimal}.
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
     * <li> If set to 'fallback' falls through to the next route handler in the handler chain.</li>
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
     * minimal}.
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
  class StorageStateOptions {
    /**
     * The file path to save the storage state to. If {@code path} is a relative path, then it is resolved relative to current
     * working directory. If no path is provided, storage state is still returned, but won't be saved to the disk.
     */
    public Path path;

    /**
     * The file path to save the storage state to. If {@code path} is a relative path, then it is resolved relative to current
     * working directory. If no path is provided, storage state is still returned, but won't be saved to the disk.
     */
    public StorageStateOptions setPath(Path path) {
      this.path = path;
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
  class WaitForPageOptions {
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
    public WaitForPageOptions setPredicate(Predicate<Page> predicate) {
      this.predicate = predicate;
      return this;
    }
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The
     * default value can be changed by using the {@link BrowserContext#setDefaultTimeout BrowserContext.setDefaultTimeout()}.
     */
    public WaitForPageOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  /**
   * Adds cookies into this browser context. All pages within this context will have these cookies installed. Cookies can be
   * obtained via {@link BrowserContext#cookies BrowserContext.cookies()}.
   *
   * <p> **Usage**
   * <pre>{@code
   * browserContext.addCookies(Arrays.asList(cookieObject1, cookieObject2));
   * }</pre>
   *
   * @param cookies Adds cookies to the browser context.
   *
   * <p> For the cookie to apply to all subdomains as well, prefix domain with a dot, like this: ".example.com".
   * @since v1.8
   */
  void addCookies(List<Cookie> cookies);
  /**
   * Adds a script which would be evaluated in one of the following scenarios:
   * <ul>
   * <li> Whenever a page is created in the browser context or is navigated.</li>
   * <li> Whenever a child frame is attached or navigated in any page in the browser context. In this case, the script is
   * evaluated in the context of the newly attached frame.</li>
   * </ul>
   *
   * <p> The script is evaluated after the document was created but before any of its scripts were run. This is useful to amend
   * the JavaScript environment, e.g. to seed {@code Math.random}.
   *
   * <p> **Usage**
   *
   * <p> An example of overriding {@code Math.random} before the page loads:
   * <pre>{@code
   * // In your playwright script, assuming the preload.js file is in same directory.
   * browserContext.addInitScript(Paths.get("preload.js"));
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
   * <li> Whenever a page is created in the browser context or is navigated.</li>
   * <li> Whenever a child frame is attached or navigated in any page in the browser context. In this case, the script is
   * evaluated in the context of the newly attached frame.</li>
   * </ul>
   *
   * <p> The script is evaluated after the document was created but before any of its scripts were run. This is useful to amend
   * the JavaScript environment, e.g. to seed {@code Math.random}.
   *
   * <p> **Usage**
   *
   * <p> An example of overriding {@code Math.random} before the page loads:
   * <pre>{@code
   * // In your playwright script, assuming the preload.js file is in same directory.
   * browserContext.addInitScript(Paths.get("preload.js"));
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
   * Returns the browser instance of the context. If it was launched as a persistent context null gets returned.
   *
   * @since v1.8
   */
  Browser browser();
  /**
   * Clears context cookies.
   *
   * @since v1.8
   */
  void clearCookies();
  /**
   * Clears all permission overrides for the browser context.
   *
   * <p> **Usage**
   * <pre>{@code
   * BrowserContext context = browser.newContext();
   * context.grantPermissions(Arrays.asList("clipboard-read"));
   * // do stuff ..
   * context.clearPermissions();
   * }</pre>
   *
   * @since v1.8
   */
  void clearPermissions();
  /**
   * Closes the browser context. All the pages that belong to the browser context will be closed.
   *
   * <p> <strong>NOTE:</strong> The default browser context cannot be closed.
   *
   * @since v1.8
   */
  void close();
  /**
   * If no URLs are specified, this method returns all cookies. If URLs are specified, only cookies that affect those URLs
   * are returned.
   *
   * @since v1.8
   */
  default List<Cookie> cookies() {
    return cookies((String) null);
  }
  /**
   * If no URLs are specified, this method returns all cookies. If URLs are specified, only cookies that affect those URLs
   * are returned.
   *
   * @param urls Optional list of URLs.
   * @since v1.8
   */
  List<Cookie> cookies(String urls);
  /**
   * If no URLs are specified, this method returns all cookies. If URLs are specified, only cookies that affect those URLs
   * are returned.
   *
   * @param urls Optional list of URLs.
   * @since v1.8
   */
  List<Cookie> cookies(List<String> urls);
  /**
   * The method adds a function called {@code name} on the {@code window} object of every frame in every page in the context.
   * When called, the function executes {@code callback} and returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a> which
   * resolves to the return value of {@code callback}. If the {@code callback} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, it will be
   * awaited.
   *
   * <p> The first argument of the {@code callback} function contains information about the caller: {@code { browserContext:
   * BrowserContext, page: Page, frame: Frame }}.
   *
   * <p> See {@link Page#exposeBinding Page.exposeBinding()} for page-only version.
   *
   * <p> **Usage**
   *
   * <p> An example of exposing page URL to all frames in all pages in the context:
   * <pre>{@code
   * import com.microsoft.playwright.*;
   *
   * public class Example {
   *   public static void main(String[] args) {
   *     try (Playwright playwright = Playwright.create()) {
   *       BrowserType webkit = playwright.webkit()
   *       Browser browser = webkit.launch(new BrowserType.LaunchOptions().setHeadless(false));
   *       BrowserContext context = browser.newContext();
   *       context.exposeBinding("pageURL", (source, args) -> source.page().url());
   *       Page page = context.newPage();
   *       page.setContent("<script>\n" +
   *         "  async function onClick() {\n" +
   *         "    document.querySelector('div').textContent = await window.pageURL();\n" +
   *         "  }\n" +
   *         "</script>\n" +
   *         "<button onclick=\"onClick()\">Click me</button>\n" +
   *         "<div></div>");
   *       page.getByRole(AriaRole.BUTTON).click();
   *     }
   *   }
   * }
   * }</pre>
   *
   * <p> An example of passing an element handle:
   * <pre>{@code
   * context.exposeBinding("clicked", (source, args) -> {
   *   ElementHandle element = (ElementHandle) args[0];
   *   System.out.println(element.textContent());
   *   return null;
   * }, new BrowserContext.ExposeBindingOptions().setHandle(true));
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
   * The method adds a function called {@code name} on the {@code window} object of every frame in every page in the context.
   * When called, the function executes {@code callback} and returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a> which
   * resolves to the return value of {@code callback}. If the {@code callback} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, it will be
   * awaited.
   *
   * <p> The first argument of the {@code callback} function contains information about the caller: {@code { browserContext:
   * BrowserContext, page: Page, frame: Frame }}.
   *
   * <p> See {@link Page#exposeBinding Page.exposeBinding()} for page-only version.
   *
   * <p> **Usage**
   *
   * <p> An example of exposing page URL to all frames in all pages in the context:
   * <pre>{@code
   * import com.microsoft.playwright.*;
   *
   * public class Example {
   *   public static void main(String[] args) {
   *     try (Playwright playwright = Playwright.create()) {
   *       BrowserType webkit = playwright.webkit()
   *       Browser browser = webkit.launch(new BrowserType.LaunchOptions().setHeadless(false));
   *       BrowserContext context = browser.newContext();
   *       context.exposeBinding("pageURL", (source, args) -> source.page().url());
   *       Page page = context.newPage();
   *       page.setContent("<script>\n" +
   *         "  async function onClick() {\n" +
   *         "    document.querySelector('div').textContent = await window.pageURL();\n" +
   *         "  }\n" +
   *         "</script>\n" +
   *         "<button onclick=\"onClick()\">Click me</button>\n" +
   *         "<div></div>");
   *       page.getByRole(AriaRole.BUTTON).click();
   *     }
   *   }
   * }
   * }</pre>
   *
   * <p> An example of passing an element handle:
   * <pre>{@code
   * context.exposeBinding("clicked", (source, args) -> {
   *   ElementHandle element = (ElementHandle) args[0];
   *   System.out.println(element.textContent());
   *   return null;
   * }, new BrowserContext.ExposeBindingOptions().setHandle(true));
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
   * The method adds a function called {@code name} on the {@code window} object of every frame in every page in the context.
   * When called, the function executes {@code callback} and returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a> which
   * resolves to the return value of {@code callback}.
   *
   * <p> If the {@code callback} returns a <a
   * href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Promise'>Promise</a>, it will be
   * awaited.
   *
   * <p> See {@link Page#exposeFunction Page.exposeFunction()} for page-only version.
   *
   * <p> **Usage**
   *
   * <p> An example of adding a {@code sha256} function to all pages in the context:
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
   *       BrowserType webkit = playwright.webkit()
   *       Browser browser = webkit.launch(new BrowserType.LaunchOptions().setHeadless(false));
   *       context.exposeFunction("sha256", args -> {
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
   *       Page page = context.newPage();
   *       page.setContent("<script>\n" +
   *         "  async function onClick() {\n" +
   *         "    document.querySelector('div').textContent = await window.sha256('PLAYWRIGHT');\n" +
   *         "  }\n" +
   *         "</script>\n" +
   *         "<button onclick=\"onClick()\">Click me</button>\n" +
   *         "<div></div>\n");
   *       page.getByRole(AriaRole.BUTTON).click();
   *     }
   *   }
   * }
   * }</pre>
   *
   * @param name Name of the function on the window object.
   * @param callback Callback function that will be called in the Playwright's context.
   * @since v1.8
   */
  void exposeFunction(String name, FunctionCallback callback);
  /**
   * Grants specified permissions to the browser context. Only grants corresponding permissions to the given origin if
   * specified.
   *
   * @param permissions A permission or an array of permissions to grant. Permissions can be one of the following values:
   * <ul>
   * <li> {@code "geolocation"}</li>
   * <li> {@code "midi"}</li>
   * <li> {@code "midi-sysex"} (system-exclusive midi)</li>
   * <li> {@code "notifications"}</li>
   * <li> {@code "camera"}</li>
   * <li> {@code "microphone"}</li>
   * <li> {@code "background-sync"}</li>
   * <li> {@code "ambient-light-sensor"}</li>
   * <li> {@code "accelerometer"}</li>
   * <li> {@code "gyroscope"}</li>
   * <li> {@code "magnetometer"}</li>
   * <li> {@code "accessibility-events"}</li>
   * <li> {@code "clipboard-read"}</li>
   * <li> {@code "clipboard-write"}</li>
   * <li> {@code "payment-handler"}</li>
   * </ul>
   * @since v1.8
   */
  default void grantPermissions(List<String> permissions) {
    grantPermissions(permissions, null);
  }
  /**
   * Grants specified permissions to the browser context. Only grants corresponding permissions to the given origin if
   * specified.
   *
   * @param permissions A permission or an array of permissions to grant. Permissions can be one of the following values:
   * <ul>
   * <li> {@code "geolocation"}</li>
   * <li> {@code "midi"}</li>
   * <li> {@code "midi-sysex"} (system-exclusive midi)</li>
   * <li> {@code "notifications"}</li>
   * <li> {@code "camera"}</li>
   * <li> {@code "microphone"}</li>
   * <li> {@code "background-sync"}</li>
   * <li> {@code "ambient-light-sensor"}</li>
   * <li> {@code "accelerometer"}</li>
   * <li> {@code "gyroscope"}</li>
   * <li> {@code "magnetometer"}</li>
   * <li> {@code "accessibility-events"}</li>
   * <li> {@code "clipboard-read"}</li>
   * <li> {@code "clipboard-write"}</li>
   * <li> {@code "payment-handler"}</li>
   * </ul>
   * @since v1.8
   */
  void grantPermissions(List<String> permissions, GrantPermissionsOptions options);
  /**
   * <strong>NOTE:</strong> CDP sessions are only supported on Chromium-based browsers.
   *
   * <p> Returns the newly created session.
   *
   * @param page Target to create new session for. For backwards-compatibility, this parameter is named {@code page}, but it can be a
   * {@code Page} or {@code Frame} type.
   * @since v1.11
   */
  CDPSession newCDPSession(Page page);
  /**
   * <strong>NOTE:</strong> CDP sessions are only supported on Chromium-based browsers.
   *
   * <p> Returns the newly created session.
   *
   * @param page Target to create new session for. For backwards-compatibility, this parameter is named {@code page}, but it can be a
   * {@code Page} or {@code Frame} type.
   * @since v1.11
   */
  CDPSession newCDPSession(Frame page);
  /**
   * Creates a new page in the browser context.
   *
   * @since v1.8
   */
  Page newPage();
  /**
   * Returns all open pages in the context.
   *
   * @since v1.8
   */
  List<Page> pages();
  /**
   * API testing helper associated with this context. Requests made with this API will use context cookies.
   *
   * @since v1.16
   */
  APIRequestContext request();
  /**
   * Routing provides the capability to modify network requests that are made by any page in the browser context. Once route
   * is enabled, every request matching the url pattern will stall unless it's continued, fulfilled or aborted.
   *
   * <p> <strong>NOTE:</strong> {@link BrowserContext#route BrowserContext.route()} will not intercept requests intercepted by Service Worker. See <a
   * href="https://github.com/microsoft/playwright/issues/1090">this</a> issue. We recommend disabling Service Workers when
   * using request interception by setting {@code Browser.newContext.serviceWorkers} to {@code "block"}.
   *
   * <p> **Usage**
   *
   * <p> An example of a naive handler that aborts all image requests:
   * <pre>{@code
   * BrowserContext context = browser.newContext();
   * context.route("**\/*.{png,jpg,jpeg}", route -> route.abort());
   * Page page = context.newPage();
   * page.navigate("https://example.com");
   * browser.close();
   * }</pre>
   *
   * <p> or the same snippet using a regex pattern instead:
   * <pre>{@code
   * BrowserContext context = browser.newContext();
   * context.route(Pattern.compile("(\\.png$)|(\\.jpg$)"), route -> route.abort());
   * Page page = context.newPage();
   * page.navigate("https://example.com");
   * browser.close();
   * }</pre>
   *
   * <p> It is possible to examine the request to decide the route action. For example, mocking all requests that contain some
   * post data, and leaving all other requests as is:
   * <pre>{@code
   * context.route("/api/**", route -> {
   *   if (route.request().postData().contains("my-string"))
   *     route.fulfill(new Route.FulfillOptions().setBody("mocked-data"));
   *   else
   *     route.resume();
   * });
   * }</pre>
   *
   * <p> Page routes (set up with {@link Page#route Page.route()}) take precedence over browser context routes when request
   * matches both handlers.
   *
   * <p> To remove a route with its handler you can use {@link BrowserContext#unroute BrowserContext.unroute()}.
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
   * Routing provides the capability to modify network requests that are made by any page in the browser context. Once route
   * is enabled, every request matching the url pattern will stall unless it's continued, fulfilled or aborted.
   *
   * <p> <strong>NOTE:</strong> {@link BrowserContext#route BrowserContext.route()} will not intercept requests intercepted by Service Worker. See <a
   * href="https://github.com/microsoft/playwright/issues/1090">this</a> issue. We recommend disabling Service Workers when
   * using request interception by setting {@code Browser.newContext.serviceWorkers} to {@code "block"}.
   *
   * <p> **Usage**
   *
   * <p> An example of a naive handler that aborts all image requests:
   * <pre>{@code
   * BrowserContext context = browser.newContext();
   * context.route("**\/*.{png,jpg,jpeg}", route -> route.abort());
   * Page page = context.newPage();
   * page.navigate("https://example.com");
   * browser.close();
   * }</pre>
   *
   * <p> or the same snippet using a regex pattern instead:
   * <pre>{@code
   * BrowserContext context = browser.newContext();
   * context.route(Pattern.compile("(\\.png$)|(\\.jpg$)"), route -> route.abort());
   * Page page = context.newPage();
   * page.navigate("https://example.com");
   * browser.close();
   * }</pre>
   *
   * <p> It is possible to examine the request to decide the route action. For example, mocking all requests that contain some
   * post data, and leaving all other requests as is:
   * <pre>{@code
   * context.route("/api/**", route -> {
   *   if (route.request().postData().contains("my-string"))
   *     route.fulfill(new Route.FulfillOptions().setBody("mocked-data"));
   *   else
   *     route.resume();
   * });
   * }</pre>
   *
   * <p> Page routes (set up with {@link Page#route Page.route()}) take precedence over browser context routes when request
   * matches both handlers.
   *
   * <p> To remove a route with its handler you can use {@link BrowserContext#unroute BrowserContext.unroute()}.
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
   * Routing provides the capability to modify network requests that are made by any page in the browser context. Once route
   * is enabled, every request matching the url pattern will stall unless it's continued, fulfilled or aborted.
   *
   * <p> <strong>NOTE:</strong> {@link BrowserContext#route BrowserContext.route()} will not intercept requests intercepted by Service Worker. See <a
   * href="https://github.com/microsoft/playwright/issues/1090">this</a> issue. We recommend disabling Service Workers when
   * using request interception by setting {@code Browser.newContext.serviceWorkers} to {@code "block"}.
   *
   * <p> **Usage**
   *
   * <p> An example of a naive handler that aborts all image requests:
   * <pre>{@code
   * BrowserContext context = browser.newContext();
   * context.route("**\/*.{png,jpg,jpeg}", route -> route.abort());
   * Page page = context.newPage();
   * page.navigate("https://example.com");
   * browser.close();
   * }</pre>
   *
   * <p> or the same snippet using a regex pattern instead:
   * <pre>{@code
   * BrowserContext context = browser.newContext();
   * context.route(Pattern.compile("(\\.png$)|(\\.jpg$)"), route -> route.abort());
   * Page page = context.newPage();
   * page.navigate("https://example.com");
   * browser.close();
   * }</pre>
   *
   * <p> It is possible to examine the request to decide the route action. For example, mocking all requests that contain some
   * post data, and leaving all other requests as is:
   * <pre>{@code
   * context.route("/api/**", route -> {
   *   if (route.request().postData().contains("my-string"))
   *     route.fulfill(new Route.FulfillOptions().setBody("mocked-data"));
   *   else
   *     route.resume();
   * });
   * }</pre>
   *
   * <p> Page routes (set up with {@link Page#route Page.route()}) take precedence over browser context routes when request
   * matches both handlers.
   *
   * <p> To remove a route with its handler you can use {@link BrowserContext#unroute BrowserContext.unroute()}.
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
   * Routing provides the capability to modify network requests that are made by any page in the browser context. Once route
   * is enabled, every request matching the url pattern will stall unless it's continued, fulfilled or aborted.
   *
   * <p> <strong>NOTE:</strong> {@link BrowserContext#route BrowserContext.route()} will not intercept requests intercepted by Service Worker. See <a
   * href="https://github.com/microsoft/playwright/issues/1090">this</a> issue. We recommend disabling Service Workers when
   * using request interception by setting {@code Browser.newContext.serviceWorkers} to {@code "block"}.
   *
   * <p> **Usage**
   *
   * <p> An example of a naive handler that aborts all image requests:
   * <pre>{@code
   * BrowserContext context = browser.newContext();
   * context.route("**\/*.{png,jpg,jpeg}", route -> route.abort());
   * Page page = context.newPage();
   * page.navigate("https://example.com");
   * browser.close();
   * }</pre>
   *
   * <p> or the same snippet using a regex pattern instead:
   * <pre>{@code
   * BrowserContext context = browser.newContext();
   * context.route(Pattern.compile("(\\.png$)|(\\.jpg$)"), route -> route.abort());
   * Page page = context.newPage();
   * page.navigate("https://example.com");
   * browser.close();
   * }</pre>
   *
   * <p> It is possible to examine the request to decide the route action. For example, mocking all requests that contain some
   * post data, and leaving all other requests as is:
   * <pre>{@code
   * context.route("/api/**", route -> {
   *   if (route.request().postData().contains("my-string"))
   *     route.fulfill(new Route.FulfillOptions().setBody("mocked-data"));
   *   else
   *     route.resume();
   * });
   * }</pre>
   *
   * <p> Page routes (set up with {@link Page#route Page.route()}) take precedence over browser context routes when request
   * matches both handlers.
   *
   * <p> To remove a route with its handler you can use {@link BrowserContext#unroute BrowserContext.unroute()}.
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
   * Routing provides the capability to modify network requests that are made by any page in the browser context. Once route
   * is enabled, every request matching the url pattern will stall unless it's continued, fulfilled or aborted.
   *
   * <p> <strong>NOTE:</strong> {@link BrowserContext#route BrowserContext.route()} will not intercept requests intercepted by Service Worker. See <a
   * href="https://github.com/microsoft/playwright/issues/1090">this</a> issue. We recommend disabling Service Workers when
   * using request interception by setting {@code Browser.newContext.serviceWorkers} to {@code "block"}.
   *
   * <p> **Usage**
   *
   * <p> An example of a naive handler that aborts all image requests:
   * <pre>{@code
   * BrowserContext context = browser.newContext();
   * context.route("**\/*.{png,jpg,jpeg}", route -> route.abort());
   * Page page = context.newPage();
   * page.navigate("https://example.com");
   * browser.close();
   * }</pre>
   *
   * <p> or the same snippet using a regex pattern instead:
   * <pre>{@code
   * BrowserContext context = browser.newContext();
   * context.route(Pattern.compile("(\\.png$)|(\\.jpg$)"), route -> route.abort());
   * Page page = context.newPage();
   * page.navigate("https://example.com");
   * browser.close();
   * }</pre>
   *
   * <p> It is possible to examine the request to decide the route action. For example, mocking all requests that contain some
   * post data, and leaving all other requests as is:
   * <pre>{@code
   * context.route("/api/**", route -> {
   *   if (route.request().postData().contains("my-string"))
   *     route.fulfill(new Route.FulfillOptions().setBody("mocked-data"));
   *   else
   *     route.resume();
   * });
   * }</pre>
   *
   * <p> Page routes (set up with {@link Page#route Page.route()}) take precedence over browser context routes when request
   * matches both handlers.
   *
   * <p> To remove a route with its handler you can use {@link BrowserContext#unroute BrowserContext.unroute()}.
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
   * Routing provides the capability to modify network requests that are made by any page in the browser context. Once route
   * is enabled, every request matching the url pattern will stall unless it's continued, fulfilled or aborted.
   *
   * <p> <strong>NOTE:</strong> {@link BrowserContext#route BrowserContext.route()} will not intercept requests intercepted by Service Worker. See <a
   * href="https://github.com/microsoft/playwright/issues/1090">this</a> issue. We recommend disabling Service Workers when
   * using request interception by setting {@code Browser.newContext.serviceWorkers} to {@code "block"}.
   *
   * <p> **Usage**
   *
   * <p> An example of a naive handler that aborts all image requests:
   * <pre>{@code
   * BrowserContext context = browser.newContext();
   * context.route("**\/*.{png,jpg,jpeg}", route -> route.abort());
   * Page page = context.newPage();
   * page.navigate("https://example.com");
   * browser.close();
   * }</pre>
   *
   * <p> or the same snippet using a regex pattern instead:
   * <pre>{@code
   * BrowserContext context = browser.newContext();
   * context.route(Pattern.compile("(\\.png$)|(\\.jpg$)"), route -> route.abort());
   * Page page = context.newPage();
   * page.navigate("https://example.com");
   * browser.close();
   * }</pre>
   *
   * <p> It is possible to examine the request to decide the route action. For example, mocking all requests that contain some
   * post data, and leaving all other requests as is:
   * <pre>{@code
   * context.route("/api/**", route -> {
   *   if (route.request().postData().contains("my-string"))
   *     route.fulfill(new Route.FulfillOptions().setBody("mocked-data"));
   *   else
   *     route.resume();
   * });
   * }</pre>
   *
   * <p> Page routes (set up with {@link Page#route Page.route()}) take precedence over browser context routes when request
   * matches both handlers.
   *
   * <p> To remove a route with its handler you can use {@link BrowserContext#unroute BrowserContext.unroute()}.
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
   * If specified the network requests that are made in the context will be served from the HAR file. Read more about <a
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
   * If specified the network requests that are made in the context will be served from the HAR file. Read more about <a
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
   * This setting will change the default maximum navigation time for the following methods and related shortcuts:
   * <ul>
   * <li> {@link Page#goBack Page.goBack()}</li>
   * <li> {@link Page#goForward Page.goForward()}</li>
   * <li> {@link Page#navigate Page.navigate()}</li>
   * <li> {@link Page#reload Page.reload()}</li>
   * <li> {@link Page#setContent Page.setContent()}</li>
   * <li> {@link Page#waitForNavigation Page.waitForNavigation()}</li>
   * </ul>
   *
   * <p> <strong>NOTE:</strong> {@link Page#setDefaultNavigationTimeout Page.setDefaultNavigationTimeout()} and {@link Page#setDefaultTimeout
   * Page.setDefaultTimeout()} take priority over {@link BrowserContext#setDefaultNavigationTimeout
   * BrowserContext.setDefaultNavigationTimeout()}.
   *
   * @param timeout Maximum navigation time in milliseconds
   * @since v1.8
   */
  void setDefaultNavigationTimeout(double timeout);
  /**
   * This setting will change the default maximum time for all the methods accepting {@code timeout} option.
   *
   * <p> <strong>NOTE:</strong> {@link Page#setDefaultNavigationTimeout Page.setDefaultNavigationTimeout()}, {@link Page#setDefaultTimeout
   * Page.setDefaultTimeout()} and {@link BrowserContext#setDefaultNavigationTimeout
   * BrowserContext.setDefaultNavigationTimeout()} take priority over {@link BrowserContext#setDefaultTimeout
   * BrowserContext.setDefaultTimeout()}.
   *
   * @param timeout Maximum time in milliseconds
   * @since v1.8
   */
  void setDefaultTimeout(double timeout);
  /**
   * The extra HTTP headers will be sent with every request initiated by any page in the context. These headers are merged
   * with page-specific extra HTTP headers set with {@link Page#setExtraHTTPHeaders Page.setExtraHTTPHeaders()}. If page
   * overrides a particular header, page-specific header value will be used instead of the browser context header value.
   *
   * <p> <strong>NOTE:</strong> {@link BrowserContext#setExtraHTTPHeaders BrowserContext.setExtraHTTPHeaders()} does not guarantee the order of headers
   * in the outgoing requests.
   *
   * @param headers An object containing additional HTTP headers to be sent with every request. All header values must be strings.
   * @since v1.8
   */
  void setExtraHTTPHeaders(Map<String, String> headers);
  /**
   * Sets the context's geolocation. Passing {@code null} or {@code undefined} emulates position unavailable.
   *
   * <p> **Usage**
   * <pre>{@code
   * browserContext.setGeolocation(new Geolocation(59.95, 30.31667));
   * }</pre>
   *
   * <p> <strong>NOTE:</strong> Consider using {@link BrowserContext#grantPermissions BrowserContext.grantPermissions()} to grant permissions for the
   * browser context pages to read its geolocation.
   *
   * @since v1.8
   */
  void setGeolocation(Geolocation geolocation);
  /**
   *
   *
   * @param offline Whether to emulate network being offline for the browser context.
   * @since v1.8
   */
  void setOffline(boolean offline);
  /**
   * Returns storage state for this browser context, contains current cookies and local storage snapshot.
   *
   * @since v1.8
   */
  default String storageState() {
    return storageState(null);
  }
  /**
   * Returns storage state for this browser context, contains current cookies and local storage snapshot.
   *
   * @since v1.8
   */
  String storageState(StorageStateOptions options);
  /**
   *
   *
   * @since v1.12
   */
  Tracing tracing();
  /**
   * Removes a route created with {@link BrowserContext#route BrowserContext.route()}. When {@code handler} is not specified,
   * removes all routes for the {@code url}.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] used to register a routing with {@link BrowserContext#route
   * BrowserContext.route()}.
   * @since v1.8
   */
  default void unroute(String url) {
    unroute(url, null);
  }
  /**
   * Removes a route created with {@link BrowserContext#route BrowserContext.route()}. When {@code handler} is not specified,
   * removes all routes for the {@code url}.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] used to register a routing with {@link BrowserContext#route
   * BrowserContext.route()}.
   * @param handler Optional handler function used to register a routing with {@link BrowserContext#route BrowserContext.route()}.
   * @since v1.8
   */
  void unroute(String url, Consumer<Route> handler);
  /**
   * Removes a route created with {@link BrowserContext#route BrowserContext.route()}. When {@code handler} is not specified,
   * removes all routes for the {@code url}.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] used to register a routing with {@link BrowserContext#route
   * BrowserContext.route()}.
   * @since v1.8
   */
  default void unroute(Pattern url) {
    unroute(url, null);
  }
  /**
   * Removes a route created with {@link BrowserContext#route BrowserContext.route()}. When {@code handler} is not specified,
   * removes all routes for the {@code url}.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] used to register a routing with {@link BrowserContext#route
   * BrowserContext.route()}.
   * @param handler Optional handler function used to register a routing with {@link BrowserContext#route BrowserContext.route()}.
   * @since v1.8
   */
  void unroute(Pattern url, Consumer<Route> handler);
  /**
   * Removes a route created with {@link BrowserContext#route BrowserContext.route()}. When {@code handler} is not specified,
   * removes all routes for the {@code url}.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] used to register a routing with {@link BrowserContext#route
   * BrowserContext.route()}.
   * @since v1.8
   */
  default void unroute(Predicate<String> url) {
    unroute(url, null);
  }
  /**
   * Removes a route created with {@link BrowserContext#route BrowserContext.route()}. When {@code handler} is not specified,
   * removes all routes for the {@code url}.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] used to register a routing with {@link BrowserContext#route
   * BrowserContext.route()}.
   * @param handler Optional handler function used to register a routing with {@link BrowserContext#route BrowserContext.route()}.
   * @since v1.8
   */
  void unroute(Predicate<String> url, Consumer<Route> handler);
  /**
   * The method will block until the condition returns true. All Playwright events will be dispatched while the method is
   * waiting for the condition.
   *
   * <p> **Usage**
   *
   * <p> Use the method to wait for a condition that depends on page events:
   * <pre>{@code
   * List<String> failedUrls = new ArrayList<>();
   * context.onResponse(response -> {
   *   if (!response.ok()) {
   *     failedUrls.add(response.url());
   *   }
   * });
   * page1.getByText("Create user").click();
   * page2.getByText("Submit button").click();
   * context.waitForCondition(() -> failedUrls.size() > 3);
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
   * List<String> failedUrls = new ArrayList<>();
   * context.onResponse(response -> {
   *   if (!response.ok()) {
   *     failedUrls.add(response.url());
   *   }
   * });
   * page1.getByText("Create user").click();
   * page2.getByText("Submit button").click();
   * context.waitForCondition(() -> failedUrls.size() > 3);
   * }</pre>
   *
   * @param condition Condition to wait for.
   * @since v1.32
   */
  void waitForCondition(BooleanSupplier condition, WaitForConditionOptions options);
  /**
   * Performs action and waits for a {@code ConsoleMessage} to be logged by in the pages in the context. If predicate is
   * provided, it passes {@code ConsoleMessage} value into the {@code predicate} function and waits for {@code
   * predicate(message)} to return a truthy value. Will throw an error if the page is closed before the {@link
   * BrowserContext#onConsoleMessage BrowserContext.onConsoleMessage()} event is fired.
   *
   * @param callback Callback that performs the action triggering the event.
   * @since v1.34
   */
  default ConsoleMessage waitForConsoleMessage(Runnable callback) {
    return waitForConsoleMessage(null, callback);
  }
  /**
   * Performs action and waits for a {@code ConsoleMessage} to be logged by in the pages in the context. If predicate is
   * provided, it passes {@code ConsoleMessage} value into the {@code predicate} function and waits for {@code
   * predicate(message)} to return a truthy value. Will throw an error if the page is closed before the {@link
   * BrowserContext#onConsoleMessage BrowserContext.onConsoleMessage()} event is fired.
   *
   * @param callback Callback that performs the action triggering the event.
   * @since v1.34
   */
  ConsoleMessage waitForConsoleMessage(WaitForConsoleMessageOptions options, Runnable callback);
  /**
   * Performs action and waits for a new {@code Page} to be created in the context. If predicate is provided, it passes
   * {@code Page} value into the {@code predicate} function and waits for {@code predicate(event)} to return a truthy value.
   * Will throw an error if the context closes before new {@code Page} is created.
   *
   * @param callback Callback that performs the action triggering the event.
   * @since v1.9
   */
  default Page waitForPage(Runnable callback) {
    return waitForPage(null, callback);
  }
  /**
   * Performs action and waits for a new {@code Page} to be created in the context. If predicate is provided, it passes
   * {@code Page} value into the {@code predicate} function and waits for {@code predicate(event)} to return a truthy value.
   * Will throw an error if the context closes before new {@code Page} is created.
   *
   * @param callback Callback that performs the action triggering the event.
   * @since v1.9
   */
  Page waitForPage(WaitForPageOptions options, Runnable callback);
}

