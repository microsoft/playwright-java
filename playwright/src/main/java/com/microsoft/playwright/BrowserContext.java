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
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * - extends: [EventEmitter]
 *
 * <p> BrowserContexts provide a way to operate multiple independent browser sessions.
 *
 * <p> If a page opens another page, e.g. with a {@code window.open} call, the popup will belong to the parent page's browser
 * context.
 *
 * <p> Playwright allows creation of "incognito" browser contexts with {@code browser.newContext()} method. "Incognito" browser
 * contexts don't write any browsing data to disk.
 */
public interface BrowserContext extends AutoCloseable {

  void onClose(Consumer<BrowserContext> handler);
  void offClose(Consumer<BrowserContext> handler);

  void onPage(Consumer<Page> handler);
  void offPage(Consumer<Page> handler);

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
  class GrantPermissionsOptions {
    /**
     * The [origin] to grant permissions to, e.g. "https://example.com".
     */
    public String origin;

    public GrantPermissionsOptions withOrigin(String origin) {
      this.origin = origin;
      return this;
    }
  }
  class StorageStateOptions {
    /**
     * The file path to save the storage state to. If {@code path} is a relative path, then it is resolved relative to current
     * working directory. If no path is provided, storage state is still returned, but won't be saved to the disk.
     */
    public Path path;

    public StorageStateOptions withPath(Path path) {
      this.path = path;
      return this;
    }
  }
  class WaitForPageOptions {
    /**
     * Receives the {@code Page} object and resolves to truthy value when the waiting should resolve.
     */
    public Predicate<Page> predicate;
    /**
     * Maximum time to wait for in milliseconds. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout. The default
     * value can be changed by using the [{@code method: BrowserContext.setDefaultTimeout}].
     */
    public Double timeout;

    public WaitForPageOptions withPredicate(Predicate<Page> predicate) {
      this.predicate = predicate;
      return this;
    }
    public WaitForPageOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  /**
   * Adds cookies into this browser context. All pages within this context will have these cookies installed. Cookies can be
   * obtained via [{@code method: BrowserContext.cookies}].
   */
  void addCookies(List<Cookie> cookies);
  /**
   * Adds a script which would be evaluated in one of the following scenarios:
   * - Whenever a page is created in the browser context or is navigated.
   * - Whenever a child frame is attached or navigated in any page in the browser context. In this case, the script is
   *   evaluated in the context of the newly attached frame.
   *
   * <p> The script is evaluated after the document was created but before any of its scripts were run. This is useful to amend
   * the JavaScript environment, e.g. to seed {@code Math.random}.
   *
   * <p> <strong>NOTE:</strong> The order of evaluation of multiple scripts installed via [{@code method: BrowserContext.addInitScript}] and
   * [{@code method: Page.addInitScript}] is not defined.
   *
   * @param script Script to be evaluated in all pages in the browser context.
   */
  void addInitScript(String script);
  /**
   * Adds a script which would be evaluated in one of the following scenarios:
   * - Whenever a page is created in the browser context or is navigated.
   * - Whenever a child frame is attached or navigated in any page in the browser context. In this case, the script is
   *   evaluated in the context of the newly attached frame.
   *
   * <p> The script is evaluated after the document was created but before any of its scripts were run. This is useful to amend
   * the JavaScript environment, e.g. to seed {@code Math.random}.
   *
   * <p> <strong>NOTE:</strong> The order of evaluation of multiple scripts installed via [{@code method: BrowserContext.addInitScript}] and
   * [{@code method: Page.addInitScript}] is not defined.
   *
   * @param script Script to be evaluated in all pages in the browser context.
   */
  void addInitScript(Path script);
  /**
   * Returns the browser instance of the context. If it was launched as a persistent context null gets returned.
   */
  Browser browser();
  /**
   * Clears context cookies.
   */
  void clearCookies();
  /**
   * Clears all permission overrides for the browser context.
   */
  void clearPermissions();
  /**
   * Closes the browser context. All the pages that belong to the browser context will be closed.
   *
   * <p> <strong>NOTE:</strong> The default browser context cannot be closed.
   */
  void close();
  /**
   * If no URLs are specified, this method returns all cookies. If URLs are specified, only cookies that affect those URLs
   * are returned.
   */
  default List<Cookie> cookies() {
    return cookies((String) null);
  }
  /**
   * If no URLs are specified, this method returns all cookies. If URLs are specified, only cookies that affect those URLs
   * are returned.
   *
   * @param urls Optional list of URLs.
   */
  List<Cookie> cookies(String urls);
  /**
   * If no URLs are specified, this method returns all cookies. If URLs are specified, only cookies that affect those URLs
   * are returned.
   *
   * @param urls Optional list of URLs.
   */
  List<Cookie> cookies(List<String> urls);
  /**
   * The method adds a function called {@code name} on the {@code window} object of every frame in every page in the context. When
   * called, the function executes {@code callback} and returns a [Promise] which resolves to the return value of {@code callback}. If
   * the {@code callback} returns a [Promise], it will be awaited.
   *
   * <p> The first argument of the {@code callback} function contains information about the caller: `{ browserContext: BrowserContext,
   * page: Page, frame: Frame }`.
   *
   * <p> See [{@code method: Page.exposeBinding}] for page-only version.
   *
   *
   * @param name Name of the function on the window object.
   * @param callback Callback function that will be called in the Playwright's context.
   */
  default void exposeBinding(String name, BindingCallback callback) {
    exposeBinding(name, callback, null);
  }
  /**
   * The method adds a function called {@code name} on the {@code window} object of every frame in every page in the context. When
   * called, the function executes {@code callback} and returns a [Promise] which resolves to the return value of {@code callback}. If
   * the {@code callback} returns a [Promise], it will be awaited.
   *
   * <p> The first argument of the {@code callback} function contains information about the caller: `{ browserContext: BrowserContext,
   * page: Page, frame: Frame }`.
   *
   * <p> See [{@code method: Page.exposeBinding}] for page-only version.
   *
   *
   * @param name Name of the function on the window object.
   * @param callback Callback function that will be called in the Playwright's context.
   */
  void exposeBinding(String name, BindingCallback callback, ExposeBindingOptions options);
  /**
   * The method adds a function called {@code name} on the {@code window} object of every frame in every page in the context. When
   * called, the function executes {@code callback} and returns a [Promise] which resolves to the return value of {@code callback}.
   *
   * <p> If the {@code callback} returns a [Promise], it will be awaited.
   *
   * <p> See [{@code method: Page.exposeFunction}] for page-only version.
   *
   *
   * @param name Name of the function on the window object.
   * @param callback Callback function that will be called in the Playwright's context.
   */
  void exposeFunction(String name, FunctionCallback callback);
  /**
   * Grants specified permissions to the browser context. Only grants corresponding permissions to the given origin if
   * specified.
   *
   * @param permissions A permission or an array of permissions to grant. Permissions can be one of the following values:
   * - {@code 'geolocation'}
   * - {@code 'midi'}
   * - {@code 'midi-sysex'} (system-exclusive midi)
   * - {@code 'notifications'}
   * - {@code 'push'}
   * - {@code 'camera'}
   * - {@code 'microphone'}
   * - {@code 'background-sync'}
   * - {@code 'ambient-light-sensor'}
   * - {@code 'accelerometer'}
   * - {@code 'gyroscope'}
   * - {@code 'magnetometer'}
   * - {@code 'accessibility-events'}
   * - {@code 'clipboard-read'}
   * - {@code 'clipboard-write'}
   * - {@code 'payment-handler'}
   */
  default void grantPermissions(List<String> permissions) {
    grantPermissions(permissions, null);
  }
  /**
   * Grants specified permissions to the browser context. Only grants corresponding permissions to the given origin if
   * specified.
   *
   * @param permissions A permission or an array of permissions to grant. Permissions can be one of the following values:
   * - {@code 'geolocation'}
   * - {@code 'midi'}
   * - {@code 'midi-sysex'} (system-exclusive midi)
   * - {@code 'notifications'}
   * - {@code 'push'}
   * - {@code 'camera'}
   * - {@code 'microphone'}
   * - {@code 'background-sync'}
   * - {@code 'ambient-light-sensor'}
   * - {@code 'accelerometer'}
   * - {@code 'gyroscope'}
   * - {@code 'magnetometer'}
   * - {@code 'accessibility-events'}
   * - {@code 'clipboard-read'}
   * - {@code 'clipboard-write'}
   * - {@code 'payment-handler'}
   */
  void grantPermissions(List<String> permissions, GrantPermissionsOptions options);
  /**
   * Creates a new page in the browser context.
   */
  Page newPage();
  /**
   * Returns all open pages in the context. 
   */
  List<Page> pages();
  /**
   * Routing provides the capability to modify network requests that are made by any page in the browser context. Once route
   * is enabled, every request matching the url pattern will stall unless it's continued, fulfilled or aborted.
   *
   * <p> or the same snippet using a regex pattern instead:
   *
   * <p> Page routes (set up with [{@code method: Page.route}]) take precedence over browser context routes when request matches both
   * handlers.
   *
   * <p> <strong>NOTE:</strong> Enabling routing disables http cache.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while routing.
   * @param handler handler function to route the request.
   */
  void route(String url, Consumer<Route> handler);
  /**
   * Routing provides the capability to modify network requests that are made by any page in the browser context. Once route
   * is enabled, every request matching the url pattern will stall unless it's continued, fulfilled or aborted.
   *
   * <p> or the same snippet using a regex pattern instead:
   *
   * <p> Page routes (set up with [{@code method: Page.route}]) take precedence over browser context routes when request matches both
   * handlers.
   *
   * <p> <strong>NOTE:</strong> Enabling routing disables http cache.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while routing.
   * @param handler handler function to route the request.
   */
  void route(Pattern url, Consumer<Route> handler);
  /**
   * Routing provides the capability to modify network requests that are made by any page in the browser context. Once route
   * is enabled, every request matching the url pattern will stall unless it's continued, fulfilled or aborted.
   *
   * <p> or the same snippet using a regex pattern instead:
   *
   * <p> Page routes (set up with [{@code method: Page.route}]) take precedence over browser context routes when request matches both
   * handlers.
   *
   * <p> <strong>NOTE:</strong> Enabling routing disables http cache.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] to match while routing.
   * @param handler handler function to route the request.
   */
  void route(Predicate<String> url, Consumer<Route> handler);
  /**
   * This setting will change the default maximum navigation time for the following methods and related shortcuts:
   * - [{@code method: Page.goBack}]
   * - [{@code method: Page.goForward}]
   * - [{@code method: Page.goto}]
   * - [{@code method: Page.reload}]
   * - [{@code method: Page.setContent}]
   * - [{@code method: Page.waitForNavigation}]
   *
   * <p> <strong>NOTE:</strong> [{@code method: Page.setDefaultNavigationTimeout}] and [{@code method: Page.setDefaultTimeout}] take priority over
   * [{@code method: BrowserContext.setDefaultNavigationTimeout}].
   *
   * @param timeout Maximum navigation time in milliseconds
   */
  void setDefaultNavigationTimeout(double timeout);
  /**
   * This setting will change the default maximum time for all the methods accepting {@code timeout} option.
   *
   * <p> <strong>NOTE:</strong> [{@code method: Page.setDefaultNavigationTimeout}], [{@code method: Page.setDefaultTimeout}] and
   * [{@code method: BrowserContext.setDefaultNavigationTimeout}] take priority over [{@code method: BrowserContext.setDefaultTimeout}].
   *
   * @param timeout Maximum time in milliseconds
   */
  void setDefaultTimeout(double timeout);
  /**
   * The extra HTTP headers will be sent with every request initiated by any page in the context. These headers are merged
   * with page-specific extra HTTP headers set with [{@code method: Page.setExtraHTTPHeaders}]. If page overrides a particular
   * header, page-specific header value will be used instead of the browser context header value.
   *
   * <p> <strong>NOTE:</strong> [{@code method: BrowserContext.setExtraHTTPHeaders}] does not guarantee the order of headers in the outgoing requests.
   *
   * @param headers An object containing additional HTTP headers to be sent with every request. All header values must be strings.
   */
  void setExtraHTTPHeaders(Map<String, String> headers);
  /**
   * Sets the context's geolocation. Passing {@code null} or {@code undefined} emulates position unavailable.
   *
   * <p> <strong>NOTE:</strong> Consider using [{@code method: BrowserContext.grantPermissions}] to grant permissions for the browser context pages to
   * read its geolocation.
   */
  void setGeolocation(Geolocation geolocation);
  /**
   *
   *
   * @param offline Whether to emulate network being offline for the browser context.
   */
  void setOffline(boolean offline);
  /**
   * Returns storage state for this browser context, contains current cookies and local storage snapshot.
   */
  default String storageState() {
    return storageState(null);
  }
  /**
   * Returns storage state for this browser context, contains current cookies and local storage snapshot.
   */
  String storageState(StorageStateOptions options);
  /**
   * Removes a route created with [{@code method: BrowserContext.route}]. When {@code handler} is not specified, removes all routes for
   * the {@code url}.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] used to register a routing with
   * [{@code method: BrowserContext.route}].
   */
  default void unroute(String url) {
    unroute(url, null);
  }
  /**
   * Removes a route created with [{@code method: BrowserContext.route}]. When {@code handler} is not specified, removes all routes for
   * the {@code url}.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] used to register a routing with
   * [{@code method: BrowserContext.route}].
   * @param handler Optional handler function used to register a routing with [{@code method: BrowserContext.route}].
   */
  void unroute(String url, Consumer<Route> handler);
  /**
   * Removes a route created with [{@code method: BrowserContext.route}]. When {@code handler} is not specified, removes all routes for
   * the {@code url}.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] used to register a routing with
   * [{@code method: BrowserContext.route}].
   */
  default void unroute(Pattern url) {
    unroute(url, null);
  }
  /**
   * Removes a route created with [{@code method: BrowserContext.route}]. When {@code handler} is not specified, removes all routes for
   * the {@code url}.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] used to register a routing with
   * [{@code method: BrowserContext.route}].
   * @param handler Optional handler function used to register a routing with [{@code method: BrowserContext.route}].
   */
  void unroute(Pattern url, Consumer<Route> handler);
  /**
   * Removes a route created with [{@code method: BrowserContext.route}]. When {@code handler} is not specified, removes all routes for
   * the {@code url}.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] used to register a routing with
   * [{@code method: BrowserContext.route}].
   */
  default void unroute(Predicate<String> url) {
    unroute(url, null);
  }
  /**
   * Removes a route created with [{@code method: BrowserContext.route}]. When {@code handler} is not specified, removes all routes for
   * the {@code url}.
   *
   * @param url A glob pattern, regex pattern or predicate receiving [URL] used to register a routing with
   * [{@code method: BrowserContext.route}].
   * @param handler Optional handler function used to register a routing with [{@code method: BrowserContext.route}].
   */
  void unroute(Predicate<String> url, Consumer<Route> handler);
  /**
   * Performs action and waits for a new {@code Page} to be created in the context. If predicate is provided, it passes {@code Page}
   * value into the {@code predicate} function and waits for {@code predicate(event)} to return a truthy value. Will throw an error if
   * the context closes before new {@code Page} is created.
   *
   * @param callback Callback that performs the action triggering the event.
   */
  default Page waitForPage(Runnable callback) {
    return waitForPage(null, callback);
  }
  /**
   * Performs action and waits for a new {@code Page} to be created in the context. If predicate is provided, it passes {@code Page}
   * value into the {@code predicate} function and waits for {@code predicate(event)} to return a truthy value. Will throw an error if
   * the context closes before new {@code Page} is created.
   *
   * @param callback Callback that performs the action triggering the event.
   */
  Page waitForPage(WaitForPageOptions options, Runnable callback);
}

