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

/**
 * A Browser is created via {@link BrowserType#launch BrowserType.launch()}. An example of using a {@code Browser} to create a
 * {@code Page}:
 * <pre>{@code
 * import com.microsoft.playwright.*;
 *
 * public class Example {
 *   public static void main(String[] args) {
 *     try (Playwright playwright = Playwright.create()) {
 *       BrowserType firefox = playwright.firefox()
 *       Browser browser = firefox.launch();
 *       Page page = browser.newPage();
 *       page.navigate('https://example.com');
 *       browser.close();
 *     }
 *   }
 * }
 * }</pre>
 */
public interface Browser extends AutoCloseable {

  /**
   * Emitted when Browser gets disconnected from the browser application. This might happen because of one of the following:
   * <ul>
   * <li> Browser application is closed or crashed.</li>
   * <li> The {@link Browser#close Browser.close()} method was called.</li>
   * </ul>
   */
  void onDisconnected(Consumer<Browser> handler);
  /**
   * Removes handler that was previously added with {@link #onDisconnected onDisconnected(handler)}.
   */
  void offDisconnected(Consumer<Browser> handler);

  class NewContextOptions {
    /**
     * Whether to automatically download all the attachments. Defaults to {@code false} where all the downloads are canceled.
     */
    public Boolean acceptDownloads;
    /**
     * Toggles bypassing page's Content-Security-Policy.
     */
    public Boolean bypassCSP;
    /**
     * Emulates {@code "prefers-colors-scheme"} media feature, supported values are {@code "light"}, {@code "dark"}, {@code "no-preference"}. See
     * {@link Page#emulateMedia Page.emulateMedia()} for more details. Defaults to {@code "light"}.
     */
    public ColorScheme colorScheme;
    /**
     * Specify device scale factor (can be thought of as dpr). Defaults to {@code 1}.
     */
    public Double deviceScaleFactor;
    /**
     * An object containing additional HTTP headers to be sent with every request. All header values must be strings.
     */
    public Map<String, String> extraHTTPHeaders;
    public Geolocation geolocation;
    /**
     * Specifies if viewport supports touch events. Defaults to false.
     */
    public Boolean hasTouch;
    /**
     * Credentials for <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication">HTTP authentication</a>.
     */
    public HttpCredentials httpCredentials;
    /**
     * Whether to ignore HTTPS errors during navigation. Defaults to {@code false}.
     */
    public Boolean ignoreHTTPSErrors;
    /**
     * Whether the {@code meta viewport} tag is taken into account and touch events are enabled. Defaults to {@code false}. Not supported
     * in Firefox.
     */
    public Boolean isMobile;
    /**
     * Whether or not to enable JavaScript in the context. Defaults to {@code true}.
     */
    public Boolean javaScriptEnabled;
    /**
     * Specify user locale, for example {@code en-GB}, {@code de-DE}, etc. Locale will affect {@code navigator.language} value, {@code Accept-Language}
     * request header value as well as number and date formatting rules.
     */
    public String locale;
    /**
     * Whether to emulate network being offline. Defaults to {@code false}.
     */
    public Boolean offline;
    /**
     * A list of permissions to grant to all pages in this context. See {@link BrowserContext#grantPermissions
     * BrowserContext.grantPermissions()} for more details.
     */
    public List<String> permissions;
    /**
     * Network proxy settings to use with this context. Note that browser needs to be launched with the global proxy for this
     * option to work. If all contexts override the proxy, global proxy will be never used and can be any string, for example
     * {@code launch({ proxy: { server: 'per-context' } })}.
     */
    public Proxy proxy;
    /**
     * Optional setting to control whether to omit request content from the HAR. Defaults to {@code false}.
     */
    public Boolean recordHarOmitContent;
    /**
     * Enables <a href="http://www.softwareishard.com/blog/har-12-spec">HAR</a> recording for all pages into the specified HAR
     * file on the filesystem. If not specified, the HAR is not recorded. Make sure to call {@link BrowserContext#close
     * BrowserContext.close()} for the HAR to be saved.
     */
    public Path recordHarPath;
    /**
     * Enables video recording for all pages into the specified directory. If not specified videos are not recorded. Make sure
     * to call {@link BrowserContext#close BrowserContext.close()} for videos to be saved.
     */
    public Path recordVideoDir;
    /**
     * Dimensions of the recorded videos. If not specified the size will be equal to {@code viewport} scaled down to fit into
     * 800x800. If {@code viewport} is not configured explicitly the video size defaults to 800x450. Actual picture of each page will
     * be scaled down if necessary to fit the specified size.
     */
    public RecordVideoSize recordVideoSize;
    /**
     * Emulates consistent window screen size available inside web page via {@code window.screen}. Is only used when the {@code viewport}
     * is set.
     */
    public ScreenSize screenSize;
    /**
     * Populates context with given storage state. This option can be used to initialize context with logged-in information
     * obtained via {@link BrowserContext#storageState BrowserContext.storageState()}.
     */
    public String storageState;
    /**
     * Populates context with given storage state. This option can be used to initialize context with logged-in information
     * obtained via {@link BrowserContext#storageState BrowserContext.storageState()}. Path to the file with saved storage
     * state.
     */
    public Path storageStatePath;
    /**
     * Changes the timezone of the context. See <a
     * href="https://cs.chromium.org/chromium/src/third_party/icu/source/data/misc/metaZones.txt?rcl=faee8bc70570192d82d2978a71e2a615788597d1">ICU's
     * metaZones.txt</a> for a list of supported timezone IDs.
     */
    public String timezoneId;
    /**
     * Specific user agent to use in this context.
     */
    public String userAgent;
    /**
     * Emulates consistent viewport for each page. Defaults to an 1280x720 viewport. {@code null} disables the default viewport.
     */
    public Optional<ViewportSize> viewportSize;

    public NewContextOptions setAcceptDownloads(boolean acceptDownloads) {
      this.acceptDownloads = acceptDownloads;
      return this;
    }
    public NewContextOptions setBypassCSP(boolean bypassCSP) {
      this.bypassCSP = bypassCSP;
      return this;
    }
    public NewContextOptions setColorScheme(ColorScheme colorScheme) {
      this.colorScheme = colorScheme;
      return this;
    }
    public NewContextOptions setDeviceScaleFactor(double deviceScaleFactor) {
      this.deviceScaleFactor = deviceScaleFactor;
      return this;
    }
    public NewContextOptions setExtraHTTPHeaders(Map<String, String> extraHTTPHeaders) {
      this.extraHTTPHeaders = extraHTTPHeaders;
      return this;
    }
    public NewContextOptions setGeolocation(double latitude, double longitude) {
      return setGeolocation(new Geolocation(latitude, longitude));
    }
    public NewContextOptions setGeolocation(Geolocation geolocation) {
      this.geolocation = geolocation;
      return this;
    }
    public NewContextOptions setHasTouch(boolean hasTouch) {
      this.hasTouch = hasTouch;
      return this;
    }
    public NewContextOptions setHttpCredentials(String username, String password) {
      return setHttpCredentials(new HttpCredentials(username, password));
    }
    public NewContextOptions setHttpCredentials(HttpCredentials httpCredentials) {
      this.httpCredentials = httpCredentials;
      return this;
    }
    public NewContextOptions setIgnoreHTTPSErrors(boolean ignoreHTTPSErrors) {
      this.ignoreHTTPSErrors = ignoreHTTPSErrors;
      return this;
    }
    public NewContextOptions setIsMobile(boolean isMobile) {
      this.isMobile = isMobile;
      return this;
    }
    public NewContextOptions setJavaScriptEnabled(boolean javaScriptEnabled) {
      this.javaScriptEnabled = javaScriptEnabled;
      return this;
    }
    public NewContextOptions setLocale(String locale) {
      this.locale = locale;
      return this;
    }
    public NewContextOptions setOffline(boolean offline) {
      this.offline = offline;
      return this;
    }
    public NewContextOptions setPermissions(List<String> permissions) {
      this.permissions = permissions;
      return this;
    }
    public NewContextOptions setProxy(String server) {
      return setProxy(new Proxy(server));
    }
    public NewContextOptions setProxy(Proxy proxy) {
      this.proxy = proxy;
      return this;
    }
    public NewContextOptions setRecordHarOmitContent(boolean recordHarOmitContent) {
      this.recordHarOmitContent = recordHarOmitContent;
      return this;
    }
    public NewContextOptions setRecordHarPath(Path recordHarPath) {
      this.recordHarPath = recordHarPath;
      return this;
    }
    public NewContextOptions setRecordVideoDir(Path recordVideoDir) {
      this.recordVideoDir = recordVideoDir;
      return this;
    }
    public NewContextOptions setRecordVideoSize(int width, int height) {
      return setRecordVideoSize(new RecordVideoSize(width, height));
    }
    public NewContextOptions setRecordVideoSize(RecordVideoSize recordVideoSize) {
      this.recordVideoSize = recordVideoSize;
      return this;
    }
    public NewContextOptions setScreenSize(int width, int height) {
      return setScreenSize(new ScreenSize(width, height));
    }
    public NewContextOptions setScreenSize(ScreenSize screenSize) {
      this.screenSize = screenSize;
      return this;
    }
    public NewContextOptions setStorageState(String storageState) {
      this.storageState = storageState;
      return this;
    }
    public NewContextOptions setStorageStatePath(Path storageStatePath) {
      this.storageStatePath = storageStatePath;
      return this;
    }
    public NewContextOptions setTimezoneId(String timezoneId) {
      this.timezoneId = timezoneId;
      return this;
    }
    public NewContextOptions setUserAgent(String userAgent) {
      this.userAgent = userAgent;
      return this;
    }
    public NewContextOptions setViewportSize(int width, int height) {
      return setViewportSize(new ViewportSize(width, height));
    }
    public NewContextOptions setViewportSize(ViewportSize viewportSize) {
      this.viewportSize = Optional.ofNullable(viewportSize);
      return this;
    }
  }
  class NewPageOptions {
    /**
     * Whether to automatically download all the attachments. Defaults to {@code false} where all the downloads are canceled.
     */
    public Boolean acceptDownloads;
    /**
     * Toggles bypassing page's Content-Security-Policy.
     */
    public Boolean bypassCSP;
    /**
     * Emulates {@code "prefers-colors-scheme"} media feature, supported values are {@code "light"}, {@code "dark"}, {@code "no-preference"}. See
     * {@link Page#emulateMedia Page.emulateMedia()} for more details. Defaults to {@code "light"}.
     */
    public ColorScheme colorScheme;
    /**
     * Specify device scale factor (can be thought of as dpr). Defaults to {@code 1}.
     */
    public Double deviceScaleFactor;
    /**
     * An object containing additional HTTP headers to be sent with every request. All header values must be strings.
     */
    public Map<String, String> extraHTTPHeaders;
    public Geolocation geolocation;
    /**
     * Specifies if viewport supports touch events. Defaults to false.
     */
    public Boolean hasTouch;
    /**
     * Credentials for <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication">HTTP authentication</a>.
     */
    public HttpCredentials httpCredentials;
    /**
     * Whether to ignore HTTPS errors during navigation. Defaults to {@code false}.
     */
    public Boolean ignoreHTTPSErrors;
    /**
     * Whether the {@code meta viewport} tag is taken into account and touch events are enabled. Defaults to {@code false}. Not supported
     * in Firefox.
     */
    public Boolean isMobile;
    /**
     * Whether or not to enable JavaScript in the context. Defaults to {@code true}.
     */
    public Boolean javaScriptEnabled;
    /**
     * Specify user locale, for example {@code en-GB}, {@code de-DE}, etc. Locale will affect {@code navigator.language} value, {@code Accept-Language}
     * request header value as well as number and date formatting rules.
     */
    public String locale;
    /**
     * Whether to emulate network being offline. Defaults to {@code false}.
     */
    public Boolean offline;
    /**
     * A list of permissions to grant to all pages in this context. See {@link BrowserContext#grantPermissions
     * BrowserContext.grantPermissions()} for more details.
     */
    public List<String> permissions;
    /**
     * Network proxy settings to use with this context. Note that browser needs to be launched with the global proxy for this
     * option to work. If all contexts override the proxy, global proxy will be never used and can be any string, for example
     * {@code launch({ proxy: { server: 'per-context' } })}.
     */
    public Proxy proxy;
    /**
     * Optional setting to control whether to omit request content from the HAR. Defaults to {@code false}.
     */
    public Boolean recordHarOmitContent;
    /**
     * Enables <a href="http://www.softwareishard.com/blog/har-12-spec">HAR</a> recording for all pages into the specified HAR
     * file on the filesystem. If not specified, the HAR is not recorded. Make sure to call {@link BrowserContext#close
     * BrowserContext.close()} for the HAR to be saved.
     */
    public Path recordHarPath;
    /**
     * Enables video recording for all pages into the specified directory. If not specified videos are not recorded. Make sure
     * to call {@link BrowserContext#close BrowserContext.close()} for videos to be saved.
     */
    public Path recordVideoDir;
    /**
     * Dimensions of the recorded videos. If not specified the size will be equal to {@code viewport} scaled down to fit into
     * 800x800. If {@code viewport} is not configured explicitly the video size defaults to 800x450. Actual picture of each page will
     * be scaled down if necessary to fit the specified size.
     */
    public RecordVideoSize recordVideoSize;
    /**
     * Emulates consistent window screen size available inside web page via {@code window.screen}. Is only used when the {@code viewport}
     * is set.
     */
    public ScreenSize screenSize;
    /**
     * Populates context with given storage state. This option can be used to initialize context with logged-in information
     * obtained via {@link BrowserContext#storageState BrowserContext.storageState()}.
     */
    public String storageState;
    /**
     * Populates context with given storage state. This option can be used to initialize context with logged-in information
     * obtained via {@link BrowserContext#storageState BrowserContext.storageState()}. Path to the file with saved storage
     * state.
     */
    public Path storageStatePath;
    /**
     * Changes the timezone of the context. See <a
     * href="https://cs.chromium.org/chromium/src/third_party/icu/source/data/misc/metaZones.txt?rcl=faee8bc70570192d82d2978a71e2a615788597d1">ICU's
     * metaZones.txt</a> for a list of supported timezone IDs.
     */
    public String timezoneId;
    /**
     * Specific user agent to use in this context.
     */
    public String userAgent;
    /**
     * Emulates consistent viewport for each page. Defaults to an 1280x720 viewport. {@code null} disables the default viewport.
     */
    public Optional<ViewportSize> viewportSize;

    public NewPageOptions setAcceptDownloads(boolean acceptDownloads) {
      this.acceptDownloads = acceptDownloads;
      return this;
    }
    public NewPageOptions setBypassCSP(boolean bypassCSP) {
      this.bypassCSP = bypassCSP;
      return this;
    }
    public NewPageOptions setColorScheme(ColorScheme colorScheme) {
      this.colorScheme = colorScheme;
      return this;
    }
    public NewPageOptions setDeviceScaleFactor(double deviceScaleFactor) {
      this.deviceScaleFactor = deviceScaleFactor;
      return this;
    }
    public NewPageOptions setExtraHTTPHeaders(Map<String, String> extraHTTPHeaders) {
      this.extraHTTPHeaders = extraHTTPHeaders;
      return this;
    }
    public NewPageOptions setGeolocation(double latitude, double longitude) {
      return setGeolocation(new Geolocation(latitude, longitude));
    }
    public NewPageOptions setGeolocation(Geolocation geolocation) {
      this.geolocation = geolocation;
      return this;
    }
    public NewPageOptions setHasTouch(boolean hasTouch) {
      this.hasTouch = hasTouch;
      return this;
    }
    public NewPageOptions setHttpCredentials(String username, String password) {
      return setHttpCredentials(new HttpCredentials(username, password));
    }
    public NewPageOptions setHttpCredentials(HttpCredentials httpCredentials) {
      this.httpCredentials = httpCredentials;
      return this;
    }
    public NewPageOptions setIgnoreHTTPSErrors(boolean ignoreHTTPSErrors) {
      this.ignoreHTTPSErrors = ignoreHTTPSErrors;
      return this;
    }
    public NewPageOptions setIsMobile(boolean isMobile) {
      this.isMobile = isMobile;
      return this;
    }
    public NewPageOptions setJavaScriptEnabled(boolean javaScriptEnabled) {
      this.javaScriptEnabled = javaScriptEnabled;
      return this;
    }
    public NewPageOptions setLocale(String locale) {
      this.locale = locale;
      return this;
    }
    public NewPageOptions setOffline(boolean offline) {
      this.offline = offline;
      return this;
    }
    public NewPageOptions setPermissions(List<String> permissions) {
      this.permissions = permissions;
      return this;
    }
    public NewPageOptions setProxy(String server) {
      return setProxy(new Proxy(server));
    }
    public NewPageOptions setProxy(Proxy proxy) {
      this.proxy = proxy;
      return this;
    }
    public NewPageOptions setRecordHarOmitContent(boolean recordHarOmitContent) {
      this.recordHarOmitContent = recordHarOmitContent;
      return this;
    }
    public NewPageOptions setRecordHarPath(Path recordHarPath) {
      this.recordHarPath = recordHarPath;
      return this;
    }
    public NewPageOptions setRecordVideoDir(Path recordVideoDir) {
      this.recordVideoDir = recordVideoDir;
      return this;
    }
    public NewPageOptions setRecordVideoSize(int width, int height) {
      return setRecordVideoSize(new RecordVideoSize(width, height));
    }
    public NewPageOptions setRecordVideoSize(RecordVideoSize recordVideoSize) {
      this.recordVideoSize = recordVideoSize;
      return this;
    }
    public NewPageOptions setScreenSize(int width, int height) {
      return setScreenSize(new ScreenSize(width, height));
    }
    public NewPageOptions setScreenSize(ScreenSize screenSize) {
      this.screenSize = screenSize;
      return this;
    }
    public NewPageOptions setStorageState(String storageState) {
      this.storageState = storageState;
      return this;
    }
    public NewPageOptions setStorageStatePath(Path storageStatePath) {
      this.storageStatePath = storageStatePath;
      return this;
    }
    public NewPageOptions setTimezoneId(String timezoneId) {
      this.timezoneId = timezoneId;
      return this;
    }
    public NewPageOptions setUserAgent(String userAgent) {
      this.userAgent = userAgent;
      return this;
    }
    public NewPageOptions setViewportSize(int width, int height) {
      return setViewportSize(new ViewportSize(width, height));
    }
    public NewPageOptions setViewportSize(ViewportSize viewportSize) {
      this.viewportSize = Optional.ofNullable(viewportSize);
      return this;
    }
  }
  /**
   * In case this browser is obtained using {@link BrowserType#launch BrowserType.launch()}, closes the browser and all of
   * its pages (if any were opened).
   *
   * <p> In case this browser is connected to, clears all created contexts belonging to this browser and disconnects from the
   * browser server.
   *
   * <p> The {@code Browser} object itself is considered to be disposed and cannot be used anymore.
   */
  void close();
  /**
   * Returns an array of all open browser contexts. In a newly created browser, this will return zero browser contexts.
   * <pre>{@code
   * Browser browser = pw.webkit().launch();
   * System.out.println(browser.contexts().size()); // prints "0"
   * BrowserContext context = browser.newContext();
   * System.out.println(browser.contexts().size()); // prints "1"
   * }</pre>
   */
  List<BrowserContext> contexts();
  /**
   * Indicates that the browser is connected.
   */
  boolean isConnected();
  /**
   * Creates a new browser context. It won't share cookies/cache with other browser contexts.
   * <pre>{@code
   * Browser browser = playwright.firefox().launch();  // Or 'chromium' or 'webkit'.
   * // Create a new incognito browser context.
   * BrowserContext context = browser.newContext();
   * // Create a new page in a pristine context.
   * Page page = context.newPage();
   * page.navigate('https://example.com');
   * }</pre>
   */
  default BrowserContext newContext() {
    return newContext(null);
  }
  /**
   * Creates a new browser context. It won't share cookies/cache with other browser contexts.
   * <pre>{@code
   * Browser browser = playwright.firefox().launch();  // Or 'chromium' or 'webkit'.
   * // Create a new incognito browser context.
   * BrowserContext context = browser.newContext();
   * // Create a new page in a pristine context.
   * Page page = context.newPage();
   * page.navigate('https://example.com');
   * }</pre>
   */
  BrowserContext newContext(NewContextOptions options);
  /**
   * Creates a new page in a new browser context. Closing this page will close the context as well.
   *
   * <p> This is a convenience API that should only be used for the single-page scenarios and short snippets. Production code and
   * testing frameworks should explicitly create {@link Browser#newContext Browser.newContext()} followed by the {@link
   * BrowserContext#newPage BrowserContext.newPage()} to control their exact life times.
   */
  default Page newPage() {
    return newPage(null);
  }
  /**
   * Creates a new page in a new browser context. Closing this page will close the context as well.
   *
   * <p> This is a convenience API that should only be used for the single-page scenarios and short snippets. Production code and
   * testing frameworks should explicitly create {@link Browser#newContext Browser.newContext()} followed by the {@link
   * BrowserContext#newPage BrowserContext.newPage()} to control their exact life times.
   */
  Page newPage(NewPageOptions options);
  /**
   * Returns the browser version.
   */
  String version();
}

