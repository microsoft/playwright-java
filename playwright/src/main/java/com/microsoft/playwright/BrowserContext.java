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

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * BrowserContexts provide a way to operate multiple independent browser sessions.
 * <p>
 * If a page opens another page, e.g. with a {@code window.open} call, the popup will belong to the parent page's browser
 * <p>
 * context.
 * <p>
 * Playwright allows creation of "incognito" browser contexts with {@code browser.newContext()} method.
 * <p>
 * "Incognito" browser contexts don't write any browsing data to disk.
 * <p>
 */
public interface BrowserContext {
  enum SameSite { STRICT, LAX, NONE }

  class HTTPCredentials {
    private final String username;
    private final String password;

    public HTTPCredentials(String username, String password) {
      this.username = username;
      this.password = password;
    }

    public String username() {
      return username;
    }

    public String password() {
      return password;
    }
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
    PAGE,
  }

  void addListener(EventType type, Listener<EventType> listener);
  void removeListener(EventType type, Listener<EventType> listener);
  class AddCookie {
    /**
     * **required**
     */
    public String name;
    /**
     * **required**
     */
    public String value;
    /**
     * either url or domain / path are required
     */
    public String url;
    /**
     * either url or domain / path are required
     */
    public String domain;
    /**
     * either url or domain / path are required
     */
    public String path;
    /**
     * Unix time in seconds.
     */
    public Long expires;
    public Boolean httpOnly;
    public Boolean secure;
    public SameSite sameSite;

    public AddCookie withName(String name) {
      this.name = name;
      return this;
    }
    public AddCookie withValue(String value) {
      this.value = value;
      return this;
    }
    public AddCookie withUrl(String url) {
      this.url = url;
      return this;
    }
    public AddCookie withDomain(String domain) {
      this.domain = domain;
      return this;
    }
    public AddCookie withPath(String path) {
      this.path = path;
      return this;
    }
    public AddCookie withExpires(Long expires) {
      this.expires = expires;
      return this;
    }
    public AddCookie withHttpOnly(Boolean httpOnly) {
      this.httpOnly = httpOnly;
      return this;
    }
    public AddCookie withSecure(Boolean secure) {
      this.secure = secure;
      return this;
    }
    public AddCookie withSameSite(SameSite sameSite) {
      this.sameSite = sameSite;
      return this;
    }
  }
  class Cookie {
    private String name;
    private String value;
    private String domain;
    private String path;
    /**
     * Unix time in seconds.
     */
    private long expires;
    private boolean httpOnly;
    private boolean secure;
    private SameSite sameSite;

    public String name() {
      return this.name;
    }
    public String value() {
      return this.value;
    }
    public String domain() {
      return this.domain;
    }
    public String path() {
      return this.path;
    }
    public long expires() {
      return this.expires;
    }
    public boolean httpOnly() {
      return this.httpOnly;
    }
    public boolean secure() {
      return this.secure;
    }
    public SameSite sameSite() {
      return this.sameSite;
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
  class GrantPermissionsOptions {
    /**
     * The origin to grant permissions to, e.g. "https://example.com".
     */
    public String origin;

    public GrantPermissionsOptions withOrigin(String origin) {
      this.origin = origin;
      return this;
    }
  }
  void addCookies(List<AddCookie> cookies);
  default void addInitScript(String script) {
    addInitScript(script, null);
  }
  /**
   * Adds a script which would be evaluated in one of the following scenarios:
   * <p>
   * Whenever a page is created in the browser context or is navigated.
   * <p>
   * Whenever a child frame is attached or navigated in any page in the browser context. In this case, the script is evaluated in the context of the newly attached frame.
   * <p>
   * The script is evaluated after the document was created but before any of its scripts were run. This is useful to amend  the JavaScript environment, e.g. to seed {@code Math.random}.
   * <p>
   * 
   * <p>
   * <strong>NOTE</strong> The order of evaluation of multiple scripts installed via browserContext.addInitScript(script[, arg]) and page.addInitScript(script[, arg]) is not defined.
   * @param script Script to be evaluated in all pages in the browser context.
   * @param arg Optional argument to pass to {@code script} (only supported when passing a function).
   */
  void addInitScript(String script, Object arg);
  /**
   * 
   * @return Returns the browser instance of the context. If it was launched as a persistent context null gets returned.
   */
  Browser browser();
  /**
   * Clears context cookies.
   */
  void clearCookies();
  /**
   * Clears all permission overrides for the browser context.
   * <p>
   */
  void clearPermissions();
  /**
   * Closes the browser context. All the pages that belong to the browser context
   * <p>
   * will be closed.
   * <p>
   * <strong>NOTE</strong> the default browser context cannot be closed.
   */
  void close();
  default List<Cookie> cookies() { return cookies((List<String>) null); }
  default List<Cookie> cookies(String url) { return cookies(Arrays.asList(url)); }
  /**
   * If no URLs are specified, this method returns all cookies.
   * <p>
   * If URLs are specified, only cookies that affect those URLs are returned.
   */
  List<Cookie> cookies(List<String> urls);
  default void exposeBinding(String name, Page.Binding playwrightBinding) {
    exposeBinding(name, playwrightBinding, null);
  }
  /**
   * The method adds a function called {@code name} on the {@code window} object of every frame in every page in the context.
   * <p>
   * When called, the function executes {@code playwrightBinding} in Node.js and returns a Promise which resolves to the return value of {@code playwrightBinding}.
   * <p>
   * If the {@code playwrightBinding} returns a Promise, it will be awaited.
   * <p>
   * The first argument of the {@code playwrightBinding} function contains information about the caller:
   * <p>
   * {@code { browserContext: BrowserContext, page: Page, frame: Frame }}.
   * <p>
   * See page.exposeBinding(name, playwrightBinding) for page-only version.
   * @param name Name of the function on the window object.
   * @param playwrightBinding Callback function that will be called in the Playwright's context.
   */
  void exposeBinding(String name, Page.Binding playwrightBinding, ExposeBindingOptions options);
  /**
   * The method adds a function called {@code name} on the {@code window} object of every frame in every page in the context.
   * <p>
   * When called, the function executes {@code playwrightFunction} in Node.js and returns a Promise which resolves to the return value of {@code playwrightFunction}.
   * <p>
   * If the {@code playwrightFunction} returns a Promise, it will be awaited.
   * <p>
   * See page.exposeFunction(name, playwrightFunction) for page-only version.
   * @param name Name of the function on the window object.
   * @param playwrightFunction Callback function that will be called in the Playwright's context.
   */
  void exposeFunction(String name, Page.Function playwrightFunction);
  default void grantPermissions(List<String> permissions) {
    grantPermissions(permissions, null);
  }
  /**
   * Grants specified permissions to the browser context. Only grants corresponding permissions to the given origin if specified.
   * @param permissions A permission or an array of permissions to grant. Permissions can be one of the following values:
   *  - {@code 'geolocation'}
   *  - {@code 'midi'}
   *  - {@code 'midi-sysex'} (system-exclusive midi)
   *  - {@code 'notifications'}
   *  - {@code 'push'}
   *  - {@code 'camera'}
   *  - {@code 'microphone'}
   *  - {@code 'background-sync'}
   *  - {@code 'ambient-light-sensor'}
   *  - {@code 'accelerometer'}
   *  - {@code 'gyroscope'}
   *  - {@code 'magnetometer'}
   *  - {@code 'accessibility-events'}
   *  - {@code 'clipboard-read'}
   *  - {@code 'clipboard-write'}
   *  - {@code 'payment-handler'}
   */
  void grantPermissions(List<String> permissions, GrantPermissionsOptions options);
  /**
   * Creates a new page in the browser context.
   */
  Page newPage();
  /**
   * 
   * @return All open pages in the context. Non visible pages, such as {@code "background_page"}, will not be listed here. You can find them using chromiumBrowserContext.backgroundPages().
   */
  List<Page> pages();
  void route(String url, Consumer<Route> handler);
  void route(Pattern url, Consumer<Route> handler);
  /**
   * Routing provides the capability to modify network requests that are made by any page in the browser context.
   * <p>
   * Once route is enabled, every request matching the url pattern will stall unless it's continued, fulfilled or aborted.
   * <p>
   * or the same snippet using a regex pattern instead:
   * <p>
   * Page routes (set up with page.route(url, handler)) take precedence over browser context routes when request matches both handlers.
   * <p>
   * <strong>NOTE</strong> Enabling routing disables http cache.
   * @param url A glob pattern, regex pattern or predicate receiving URL to match while routing.
   * @param handler handler function to route the request.
   */
  void route(Predicate<String> url, Consumer<Route> handler);
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
   * <strong>NOTE</strong> {@code page.setDefaultNavigationTimeout} and {@code page.setDefaultTimeout} take priority over {@code browserContext.setDefaultNavigationTimeout}.
   * @param timeout Maximum navigation time in milliseconds
   */
  void setDefaultNavigationTimeout(int timeout);
  /**
   * This setting will change the default maximum time for all the methods accepting {@code timeout} option.
   * <p>
   * <strong>NOTE</strong> {@code page.setDefaultNavigationTimeout}, {@code page.setDefaultTimeout} and {@code browserContext.setDefaultNavigationTimeout} take priority over {@code browserContext.setDefaultTimeout}.
   * @param timeout Maximum time in milliseconds
   */
  void setDefaultTimeout(int timeout);
  /**
   * The extra HTTP headers will be sent with every request initiated by any page in the context. These headers are merged with page-specific extra HTTP headers set with page.setExtraHTTPHeaders(). If page overrides a particular header, page-specific header value will be used instead of the browser context header value.
   * <p>
   * <strong>NOTE</strong> {@code browserContext.setExtraHTTPHeaders} does not guarantee the order of headers in the outgoing requests.
   * @param headers An object containing additional HTTP headers to be sent with every request. All header values must be strings.
   */
  void setExtraHTTPHeaders(Map<String, String> headers);
  /**
   * Sets the context's geolocation. Passing {@code null} or {@code undefined} emulates position unavailable.
   * <p>
   * 
   * <p>
   * <strong>NOTE</strong> Consider using browserContext.grantPermissions to grant permissions for the browser context pages to read its geolocation.
   */
  void setGeolocation(Geolocation geolocation);
  /**
   * 
   * @param offline Whether to emulate network being offline for the browser context.
   */
  void setOffline(boolean offline);
  /**
   * Returns storage state for this browser context, contains current cookies and local storage snapshot.
   */
  Object storageState();
  default void unroute(String url) { unroute(url, null); }
  default void unroute(Pattern url) { unroute(url, null); }
  default void unroute(Predicate<String> url) { unroute(url, null); }
  void unroute(String url, Consumer<Route> handler);
  void unroute(Pattern url, Consumer<Route> handler);
  /**
   * Removes a route created with browserContext.route(url, handler). When {@code handler} is not specified, removes all routes for the {@code url}.
   * @param url A glob pattern, regex pattern or predicate receiving URL used to register a routing with browserContext.route(url, handler).
   * @param handler Handler function used to register a routing with browserContext.route(url, handler).
   */
  void unroute(Predicate<String> url, Consumer<Route> handler);
  default Deferred<Event<EventType>> waitForEvent(EventType event) {
    return waitForEvent(event, (WaitForEventOptions) null);
  }
  default Deferred<Event<EventType>> waitForEvent(EventType event, Predicate<Event<EventType>> predicate) {
    WaitForEventOptions options = new WaitForEventOptions();
    options.predicate = predicate;
    return waitForEvent(event, options);
  }
  /**
   * Waits for event to fire and passes its value into the predicate function. Resolves when the predicate returns truthy value. Will throw an error if the context closes before the event
   * <p>
   * is fired.
   * <p>
   * 
   * @param event Event name, same one would pass into {@code browserContext.on(event)}.
   * @param optionsOrPredicate Either a predicate that receives an event or an options object.
   * @return Promise which resolves to the event data value.
   */
  Deferred<Event<EventType>> waitForEvent(EventType event, WaitForEventOptions options);
}

