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
import java.util.regex.Pattern;

/**
 * A Browser is created via {@link BrowserType#launch BrowserType.launch()}. An example of using a {@code Browser} to
 * create a {@code Page}:
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
     * Whether to automatically download all the attachments. Defaults to {@code true} where all the downloads are accepted.
     */
    public Boolean acceptDownloads;
    /**
     * When using {@link Page#navigate Page.navigate()}, {@link Page#route Page.route()}, {@link Page#waitForURL
     * Page.waitForURL()}, {@link Page#waitForRequest Page.waitForRequest()}, or {@link Page#waitForResponse
     * Page.waitForResponse()} it takes the base URL in consideration by using the <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/URL/URL">{@code URL()}</a> constructor for building the
     * corresponding URL. Unset by default. Examples:
     * <ul>
     * <li> baseURL: {@code http://localhost:3000} and navigating to {@code /bar.html} results in {@code
     * http://localhost:3000/bar.html}</li>
     * <li> baseURL: {@code http://localhost:3000/foo/} and navigating to {@code ./bar.html} results in {@code
     * http://localhost:3000/foo/bar.html}</li>
     * <li> baseURL: {@code http://localhost:3000/foo} (without trailing slash) and navigating to {@code ./bar.html} results in
     * {@code http://localhost:3000/bar.html}</li>
     * </ul>
     */
    public String baseURL;
    /**
     * Toggles bypassing page's Content-Security-Policy. Defaults to {@code false}.
     */
    public Boolean bypassCSP;
    /**
     * Emulates {@code "prefers-colors-scheme"} media feature, supported values are {@code "light"}, {@code "dark"}, {@code
     * "no-preference"}. See {@link Page#emulateMedia Page.emulateMedia()} for more details. Passing {@code null} resets
     * emulation to system defaults. Defaults to {@code "light"}.
     */
    public Optional<ColorScheme> colorScheme;
    /**
     * Specify device scale factor (can be thought of as dpr). Defaults to {@code 1}. Learn more about <a
     * href="https://playwright.dev/java/docs/emulation#devices">emulating devices with device scale factor</a>.
     */
    public Double deviceScaleFactor;
    /**
     * An object containing additional HTTP headers to be sent with every request. Defaults to none.
     */
    public Map<String, String> extraHTTPHeaders;
    /**
     * Emulates {@code "forced-colors"} media feature, supported values are {@code "active"}, {@code "none"}. See {@link
     * Page#emulateMedia Page.emulateMedia()} for more details. Passing {@code null} resets emulation to system defaults.
     * Defaults to {@code "none"}.
     */
    public Optional<ForcedColors> forcedColors;
    public Geolocation geolocation;
    /**
     * Specifies if viewport supports touch events. Defaults to false. Learn more about <a
     * href="https://playwright.dev/java/docs/emulation#devices">mobile emulation</a>.
     */
    public Boolean hasTouch;
    /**
     * Credentials for <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication">HTTP authentication</a>. If
     * no origin is specified, the username and password are sent to any servers upon unauthorized responses.
     */
    public HttpCredentials httpCredentials;
    /**
     * Whether to ignore HTTPS errors when sending network requests. Defaults to {@code false}.
     */
    public Boolean ignoreHTTPSErrors;
    /**
     * Whether the {@code meta viewport} tag is taken into account and touch events are enabled. isMobile is a part of device,
     * so you don't actually need to set it manually. Defaults to {@code false} and is not supported in Firefox. Learn more
     * about <a href="https://playwright.dev/java/docs/emulation#ismobile">mobile emulation</a>.
     */
    public Boolean isMobile;
    /**
     * Whether or not to enable JavaScript in the context. Defaults to {@code true}. Learn more about <a
     * href="https://playwright.dev/java/docs/emulation#javascript-enabled">disabling JavaScript</a>.
     */
    public Boolean javaScriptEnabled;
    /**
     * Specify user locale, for example {@code en-GB}, {@code de-DE}, etc. Locale will affect {@code navigator.language} value,
     * {@code Accept-Language} request header value as well as number and date formatting rules. Defaults to the system default
     * locale. Learn more about emulation in our <a
     * href="https://playwright.dev/java/docs/emulation#locale--timezone">emulation guide</a>.
     */
    public String locale;
    /**
     * Whether to emulate network being offline. Defaults to {@code false}. Learn more about <a
     * href="https://playwright.dev/java/docs/emulation#offline">network emulation</a>.
     */
    public Boolean offline;
    /**
     * A list of permissions to grant to all pages in this context. See {@link BrowserContext#grantPermissions
     * BrowserContext.grantPermissions()} for more details. Defaults to none.
     */
    public List<String> permissions;
    /**
     * Network proxy settings to use with this context. Defaults to none.
     *
     * <p> <strong>NOTE:</strong> For Chromium on Windows the browser needs to be launched with the global proxy for this option to work. If all contexts
     * override the proxy, global proxy will be never used and can be any string, for example {@code launch({ proxy: { server:
     * 'http://per-context' } })}.
     */
    public Proxy proxy;
    /**
     * Optional setting to control resource content management. If {@code omit} is specified, content is not persisted. If
     * {@code attach} is specified, resources are persisted as separate files and all of these files are archived along with
     * the HAR file. Defaults to {@code embed}, which stores content inline the HAR file as per HAR specification.
     */
    public HarContentPolicy recordHarContent;
    /**
     * When set to {@code minimal}, only record information necessary for routing from HAR. This omits sizes, timing, page,
     * cookies, security and other types of HAR information that are not used when replaying from HAR. Defaults to {@code
     * full}.
     */
    public HarMode recordHarMode;
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
    public Object recordHarUrlFilter;
    /**
     * Enables video recording for all pages into the specified directory. If not specified videos are not recorded. Make sure
     * to call {@link BrowserContext#close BrowserContext.close()} for videos to be saved.
     */
    public Path recordVideoDir;
    /**
     * Dimensions of the recorded videos. If not specified the size will be equal to {@code viewport} scaled down to fit into
     * 800x800. If {@code viewport} is not configured explicitly the video size defaults to 800x450. Actual picture of each
     * page will be scaled down if necessary to fit the specified size.
     */
    public RecordVideoSize recordVideoSize;
    /**
     * Emulates {@code "prefers-reduced-motion"} media feature, supported values are {@code "reduce"}, {@code "no-preference"}.
     * See {@link Page#emulateMedia Page.emulateMedia()} for more details. Passing {@code null} resets emulation to system
     * defaults. Defaults to {@code "no-preference"}.
     */
    public Optional<ReducedMotion> reducedMotion;
    /**
     * Emulates consistent window screen size available inside web page via {@code window.screen}. Is only used when the {@code
     * viewport} is set.
     */
    public ScreenSize screenSize;
    /**
     * Whether to allow sites to register Service workers. Defaults to {@code "allow"}.
     * <ul>
     * <li> {@code "allow"}: <a href="https://developer.mozilla.org/en-US/docs/Web/API/Service_Worker_API">Service Workers</a> can
     * be registered.</li>
     * <li> {@code "block"}: Playwright will block all registration of Service Workers.</li>
     * </ul>
     */
    public ServiceWorkerPolicy serviceWorkers;
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
     * If set to true, enables strict selectors mode for this context. In the strict selectors mode all operations on selectors
     * that imply single target DOM element will throw when more than one element matches the selector. This option does not
     * affect any Locator APIs (Locators are always strict). Defaults to {@code false}. See {@code Locator} to learn more about
     * the strict mode.
     */
    public Boolean strictSelectors;
    /**
     * Changes the timezone of the context. See <a
     * href="https://cs.chromium.org/chromium/src/third_party/icu/source/data/misc/metaZones.txt?rcl=faee8bc70570192d82d2978a71e2a615788597d1">ICU's
     * metaZones.txt</a> for a list of supported timezone IDs. Defaults to the system timezone.
     */
    public String timezoneId;
    /**
     * Specific user agent to use in this context.
     */
    public String userAgent;
    /**
     * Emulates consistent viewport for each page. Defaults to an 1280x720 viewport. Use {@code null} to disable the consistent
     * viewport emulation. Learn more about <a href="https://playwright.dev/java/docs/emulation#viewport">viewport
     * emulation</a>.
     *
     * <p> <strong>NOTE:</strong> The {@code null} value opts out from the default presets, makes viewport depend on the host window size defined by the
     * operating system. It makes the execution of the tests non-deterministic.
     */
    public Optional<ViewportSize> viewportSize;

    /**
     * Whether to automatically download all the attachments. Defaults to {@code true} where all the downloads are accepted.
     */
    public NewContextOptions setAcceptDownloads(boolean acceptDownloads) {
      this.acceptDownloads = acceptDownloads;
      return this;
    }
    /**
     * When using {@link Page#navigate Page.navigate()}, {@link Page#route Page.route()}, {@link Page#waitForURL
     * Page.waitForURL()}, {@link Page#waitForRequest Page.waitForRequest()}, or {@link Page#waitForResponse
     * Page.waitForResponse()} it takes the base URL in consideration by using the <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/URL/URL">{@code URL()}</a> constructor for building the
     * corresponding URL. Unset by default. Examples:
     * <ul>
     * <li> baseURL: {@code http://localhost:3000} and navigating to {@code /bar.html} results in {@code
     * http://localhost:3000/bar.html}</li>
     * <li> baseURL: {@code http://localhost:3000/foo/} and navigating to {@code ./bar.html} results in {@code
     * http://localhost:3000/foo/bar.html}</li>
     * <li> baseURL: {@code http://localhost:3000/foo} (without trailing slash) and navigating to {@code ./bar.html} results in
     * {@code http://localhost:3000/bar.html}</li>
     * </ul>
     */
    public NewContextOptions setBaseURL(String baseURL) {
      this.baseURL = baseURL;
      return this;
    }
    /**
     * Toggles bypassing page's Content-Security-Policy. Defaults to {@code false}.
     */
    public NewContextOptions setBypassCSP(boolean bypassCSP) {
      this.bypassCSP = bypassCSP;
      return this;
    }
    /**
     * Emulates {@code "prefers-colors-scheme"} media feature, supported values are {@code "light"}, {@code "dark"}, {@code
     * "no-preference"}. See {@link Page#emulateMedia Page.emulateMedia()} for more details. Passing {@code null} resets
     * emulation to system defaults. Defaults to {@code "light"}.
     */
    public NewContextOptions setColorScheme(ColorScheme colorScheme) {
      this.colorScheme = Optional.ofNullable(colorScheme);
      return this;
    }
    /**
     * Specify device scale factor (can be thought of as dpr). Defaults to {@code 1}. Learn more about <a
     * href="https://playwright.dev/java/docs/emulation#devices">emulating devices with device scale factor</a>.
     */
    public NewContextOptions setDeviceScaleFactor(double deviceScaleFactor) {
      this.deviceScaleFactor = deviceScaleFactor;
      return this;
    }
    /**
     * An object containing additional HTTP headers to be sent with every request. Defaults to none.
     */
    public NewContextOptions setExtraHTTPHeaders(Map<String, String> extraHTTPHeaders) {
      this.extraHTTPHeaders = extraHTTPHeaders;
      return this;
    }
    /**
     * Emulates {@code "forced-colors"} media feature, supported values are {@code "active"}, {@code "none"}. See {@link
     * Page#emulateMedia Page.emulateMedia()} for more details. Passing {@code null} resets emulation to system defaults.
     * Defaults to {@code "none"}.
     */
    public NewContextOptions setForcedColors(ForcedColors forcedColors) {
      this.forcedColors = Optional.ofNullable(forcedColors);
      return this;
    }
    public NewContextOptions setGeolocation(double latitude, double longitude) {
      return setGeolocation(new Geolocation(latitude, longitude));
    }
    public NewContextOptions setGeolocation(Geolocation geolocation) {
      this.geolocation = geolocation;
      return this;
    }
    /**
     * Specifies if viewport supports touch events. Defaults to false. Learn more about <a
     * href="https://playwright.dev/java/docs/emulation#devices">mobile emulation</a>.
     */
    public NewContextOptions setHasTouch(boolean hasTouch) {
      this.hasTouch = hasTouch;
      return this;
    }
    /**
     * Credentials for <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication">HTTP authentication</a>. If
     * no origin is specified, the username and password are sent to any servers upon unauthorized responses.
     */
    public NewContextOptions setHttpCredentials(String username, String password) {
      return setHttpCredentials(new HttpCredentials(username, password));
    }
    /**
     * Credentials for <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication">HTTP authentication</a>. If
     * no origin is specified, the username and password are sent to any servers upon unauthorized responses.
     */
    public NewContextOptions setHttpCredentials(HttpCredentials httpCredentials) {
      this.httpCredentials = httpCredentials;
      return this;
    }
    /**
     * Whether to ignore HTTPS errors when sending network requests. Defaults to {@code false}.
     */
    public NewContextOptions setIgnoreHTTPSErrors(boolean ignoreHTTPSErrors) {
      this.ignoreHTTPSErrors = ignoreHTTPSErrors;
      return this;
    }
    /**
     * Whether the {@code meta viewport} tag is taken into account and touch events are enabled. isMobile is a part of device,
     * so you don't actually need to set it manually. Defaults to {@code false} and is not supported in Firefox. Learn more
     * about <a href="https://playwright.dev/java/docs/emulation#ismobile">mobile emulation</a>.
     */
    public NewContextOptions setIsMobile(boolean isMobile) {
      this.isMobile = isMobile;
      return this;
    }
    /**
     * Whether or not to enable JavaScript in the context. Defaults to {@code true}. Learn more about <a
     * href="https://playwright.dev/java/docs/emulation#javascript-enabled">disabling JavaScript</a>.
     */
    public NewContextOptions setJavaScriptEnabled(boolean javaScriptEnabled) {
      this.javaScriptEnabled = javaScriptEnabled;
      return this;
    }
    /**
     * Specify user locale, for example {@code en-GB}, {@code de-DE}, etc. Locale will affect {@code navigator.language} value,
     * {@code Accept-Language} request header value as well as number and date formatting rules. Defaults to the system default
     * locale. Learn more about emulation in our <a
     * href="https://playwright.dev/java/docs/emulation#locale--timezone">emulation guide</a>.
     */
    public NewContextOptions setLocale(String locale) {
      this.locale = locale;
      return this;
    }
    /**
     * Whether to emulate network being offline. Defaults to {@code false}. Learn more about <a
     * href="https://playwright.dev/java/docs/emulation#offline">network emulation</a>.
     */
    public NewContextOptions setOffline(boolean offline) {
      this.offline = offline;
      return this;
    }
    /**
     * A list of permissions to grant to all pages in this context. See {@link BrowserContext#grantPermissions
     * BrowserContext.grantPermissions()} for more details. Defaults to none.
     */
    public NewContextOptions setPermissions(List<String> permissions) {
      this.permissions = permissions;
      return this;
    }
    /**
     * Network proxy settings to use with this context. Defaults to none.
     *
     * <p> <strong>NOTE:</strong> For Chromium on Windows the browser needs to be launched with the global proxy for this option to work. If all contexts
     * override the proxy, global proxy will be never used and can be any string, for example {@code launch({ proxy: { server:
     * 'http://per-context' } })}.
     */
    public NewContextOptions setProxy(String server) {
      return setProxy(new Proxy(server));
    }
    /**
     * Network proxy settings to use with this context. Defaults to none.
     *
     * <p> <strong>NOTE:</strong> For Chromium on Windows the browser needs to be launched with the global proxy for this option to work. If all contexts
     * override the proxy, global proxy will be never used and can be any string, for example {@code launch({ proxy: { server:
     * 'http://per-context' } })}.
     */
    public NewContextOptions setProxy(Proxy proxy) {
      this.proxy = proxy;
      return this;
    }
    /**
     * Optional setting to control resource content management. If {@code omit} is specified, content is not persisted. If
     * {@code attach} is specified, resources are persisted as separate files and all of these files are archived along with
     * the HAR file. Defaults to {@code embed}, which stores content inline the HAR file as per HAR specification.
     */
    public NewContextOptions setRecordHarContent(HarContentPolicy recordHarContent) {
      this.recordHarContent = recordHarContent;
      return this;
    }
    /**
     * When set to {@code minimal}, only record information necessary for routing from HAR. This omits sizes, timing, page,
     * cookies, security and other types of HAR information that are not used when replaying from HAR. Defaults to {@code
     * full}.
     */
    public NewContextOptions setRecordHarMode(HarMode recordHarMode) {
      this.recordHarMode = recordHarMode;
      return this;
    }
    /**
     * Optional setting to control whether to omit request content from the HAR. Defaults to {@code false}.
     */
    public NewContextOptions setRecordHarOmitContent(boolean recordHarOmitContent) {
      this.recordHarOmitContent = recordHarOmitContent;
      return this;
    }
    /**
     * Enables <a href="http://www.softwareishard.com/blog/har-12-spec">HAR</a> recording for all pages into the specified HAR
     * file on the filesystem. If not specified, the HAR is not recorded. Make sure to call {@link BrowserContext#close
     * BrowserContext.close()} for the HAR to be saved.
     */
    public NewContextOptions setRecordHarPath(Path recordHarPath) {
      this.recordHarPath = recordHarPath;
      return this;
    }
    public NewContextOptions setRecordHarUrlFilter(String recordHarUrlFilter) {
      this.recordHarUrlFilter = recordHarUrlFilter;
      return this;
    }
    public NewContextOptions setRecordHarUrlFilter(Pattern recordHarUrlFilter) {
      this.recordHarUrlFilter = recordHarUrlFilter;
      return this;
    }
    /**
     * Enables video recording for all pages into the specified directory. If not specified videos are not recorded. Make sure
     * to call {@link BrowserContext#close BrowserContext.close()} for videos to be saved.
     */
    public NewContextOptions setRecordVideoDir(Path recordVideoDir) {
      this.recordVideoDir = recordVideoDir;
      return this;
    }
    /**
     * Dimensions of the recorded videos. If not specified the size will be equal to {@code viewport} scaled down to fit into
     * 800x800. If {@code viewport} is not configured explicitly the video size defaults to 800x450. Actual picture of each
     * page will be scaled down if necessary to fit the specified size.
     */
    public NewContextOptions setRecordVideoSize(int width, int height) {
      return setRecordVideoSize(new RecordVideoSize(width, height));
    }
    /**
     * Dimensions of the recorded videos. If not specified the size will be equal to {@code viewport} scaled down to fit into
     * 800x800. If {@code viewport} is not configured explicitly the video size defaults to 800x450. Actual picture of each
     * page will be scaled down if necessary to fit the specified size.
     */
    public NewContextOptions setRecordVideoSize(RecordVideoSize recordVideoSize) {
      this.recordVideoSize = recordVideoSize;
      return this;
    }
    /**
     * Emulates {@code "prefers-reduced-motion"} media feature, supported values are {@code "reduce"}, {@code "no-preference"}.
     * See {@link Page#emulateMedia Page.emulateMedia()} for more details. Passing {@code null} resets emulation to system
     * defaults. Defaults to {@code "no-preference"}.
     */
    public NewContextOptions setReducedMotion(ReducedMotion reducedMotion) {
      this.reducedMotion = Optional.ofNullable(reducedMotion);
      return this;
    }
    /**
     * Emulates consistent window screen size available inside web page via {@code window.screen}. Is only used when the {@code
     * viewport} is set.
     */
    public NewContextOptions setScreenSize(int width, int height) {
      return setScreenSize(new ScreenSize(width, height));
    }
    /**
     * Emulates consistent window screen size available inside web page via {@code window.screen}. Is only used when the {@code
     * viewport} is set.
     */
    public NewContextOptions setScreenSize(ScreenSize screenSize) {
      this.screenSize = screenSize;
      return this;
    }
    /**
     * Whether to allow sites to register Service workers. Defaults to {@code "allow"}.
     * <ul>
     * <li> {@code "allow"}: <a href="https://developer.mozilla.org/en-US/docs/Web/API/Service_Worker_API">Service Workers</a> can
     * be registered.</li>
     * <li> {@code "block"}: Playwright will block all registration of Service Workers.</li>
     * </ul>
     */
    public NewContextOptions setServiceWorkers(ServiceWorkerPolicy serviceWorkers) {
      this.serviceWorkers = serviceWorkers;
      return this;
    }
    /**
     * Populates context with given storage state. This option can be used to initialize context with logged-in information
     * obtained via {@link BrowserContext#storageState BrowserContext.storageState()}.
     */
    public NewContextOptions setStorageState(String storageState) {
      this.storageState = storageState;
      return this;
    }
    /**
     * Populates context with given storage state. This option can be used to initialize context with logged-in information
     * obtained via {@link BrowserContext#storageState BrowserContext.storageState()}. Path to the file with saved storage
     * state.
     */
    public NewContextOptions setStorageStatePath(Path storageStatePath) {
      this.storageStatePath = storageStatePath;
      return this;
    }
    /**
     * If set to true, enables strict selectors mode for this context. In the strict selectors mode all operations on selectors
     * that imply single target DOM element will throw when more than one element matches the selector. This option does not
     * affect any Locator APIs (Locators are always strict). Defaults to {@code false}. See {@code Locator} to learn more about
     * the strict mode.
     */
    public NewContextOptions setStrictSelectors(boolean strictSelectors) {
      this.strictSelectors = strictSelectors;
      return this;
    }
    /**
     * Changes the timezone of the context. See <a
     * href="https://cs.chromium.org/chromium/src/third_party/icu/source/data/misc/metaZones.txt?rcl=faee8bc70570192d82d2978a71e2a615788597d1">ICU's
     * metaZones.txt</a> for a list of supported timezone IDs. Defaults to the system timezone.
     */
    public NewContextOptions setTimezoneId(String timezoneId) {
      this.timezoneId = timezoneId;
      return this;
    }
    /**
     * Specific user agent to use in this context.
     */
    public NewContextOptions setUserAgent(String userAgent) {
      this.userAgent = userAgent;
      return this;
    }
    /**
     * Emulates consistent viewport for each page. Defaults to an 1280x720 viewport. Use {@code null} to disable the consistent
     * viewport emulation. Learn more about <a href="https://playwright.dev/java/docs/emulation#viewport">viewport
     * emulation</a>.
     *
     * <p> <strong>NOTE:</strong> The {@code null} value opts out from the default presets, makes viewport depend on the host window size defined by the
     * operating system. It makes the execution of the tests non-deterministic.
     */
    public NewContextOptions setViewportSize(int width, int height) {
      return setViewportSize(new ViewportSize(width, height));
    }
    /**
     * Emulates consistent viewport for each page. Defaults to an 1280x720 viewport. Use {@code null} to disable the consistent
     * viewport emulation. Learn more about <a href="https://playwright.dev/java/docs/emulation#viewport">viewport
     * emulation</a>.
     *
     * <p> <strong>NOTE:</strong> The {@code null} value opts out from the default presets, makes viewport depend on the host window size defined by the
     * operating system. It makes the execution of the tests non-deterministic.
     */
    public NewContextOptions setViewportSize(ViewportSize viewportSize) {
      this.viewportSize = Optional.ofNullable(viewportSize);
      return this;
    }
  }
  class NewPageOptions {
    /**
     * Whether to automatically download all the attachments. Defaults to {@code true} where all the downloads are accepted.
     */
    public Boolean acceptDownloads;
    /**
     * When using {@link Page#navigate Page.navigate()}, {@link Page#route Page.route()}, {@link Page#waitForURL
     * Page.waitForURL()}, {@link Page#waitForRequest Page.waitForRequest()}, or {@link Page#waitForResponse
     * Page.waitForResponse()} it takes the base URL in consideration by using the <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/URL/URL">{@code URL()}</a> constructor for building the
     * corresponding URL. Unset by default. Examples:
     * <ul>
     * <li> baseURL: {@code http://localhost:3000} and navigating to {@code /bar.html} results in {@code
     * http://localhost:3000/bar.html}</li>
     * <li> baseURL: {@code http://localhost:3000/foo/} and navigating to {@code ./bar.html} results in {@code
     * http://localhost:3000/foo/bar.html}</li>
     * <li> baseURL: {@code http://localhost:3000/foo} (without trailing slash) and navigating to {@code ./bar.html} results in
     * {@code http://localhost:3000/bar.html}</li>
     * </ul>
     */
    public String baseURL;
    /**
     * Toggles bypassing page's Content-Security-Policy. Defaults to {@code false}.
     */
    public Boolean bypassCSP;
    /**
     * Emulates {@code "prefers-colors-scheme"} media feature, supported values are {@code "light"}, {@code "dark"}, {@code
     * "no-preference"}. See {@link Page#emulateMedia Page.emulateMedia()} for more details. Passing {@code null} resets
     * emulation to system defaults. Defaults to {@code "light"}.
     */
    public Optional<ColorScheme> colorScheme;
    /**
     * Specify device scale factor (can be thought of as dpr). Defaults to {@code 1}. Learn more about <a
     * href="https://playwright.dev/java/docs/emulation#devices">emulating devices with device scale factor</a>.
     */
    public Double deviceScaleFactor;
    /**
     * An object containing additional HTTP headers to be sent with every request. Defaults to none.
     */
    public Map<String, String> extraHTTPHeaders;
    /**
     * Emulates {@code "forced-colors"} media feature, supported values are {@code "active"}, {@code "none"}. See {@link
     * Page#emulateMedia Page.emulateMedia()} for more details. Passing {@code null} resets emulation to system defaults.
     * Defaults to {@code "none"}.
     */
    public Optional<ForcedColors> forcedColors;
    public Geolocation geolocation;
    /**
     * Specifies if viewport supports touch events. Defaults to false. Learn more about <a
     * href="https://playwright.dev/java/docs/emulation#devices">mobile emulation</a>.
     */
    public Boolean hasTouch;
    /**
     * Credentials for <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication">HTTP authentication</a>. If
     * no origin is specified, the username and password are sent to any servers upon unauthorized responses.
     */
    public HttpCredentials httpCredentials;
    /**
     * Whether to ignore HTTPS errors when sending network requests. Defaults to {@code false}.
     */
    public Boolean ignoreHTTPSErrors;
    /**
     * Whether the {@code meta viewport} tag is taken into account and touch events are enabled. isMobile is a part of device,
     * so you don't actually need to set it manually. Defaults to {@code false} and is not supported in Firefox. Learn more
     * about <a href="https://playwright.dev/java/docs/emulation#ismobile">mobile emulation</a>.
     */
    public Boolean isMobile;
    /**
     * Whether or not to enable JavaScript in the context. Defaults to {@code true}. Learn more about <a
     * href="https://playwright.dev/java/docs/emulation#javascript-enabled">disabling JavaScript</a>.
     */
    public Boolean javaScriptEnabled;
    /**
     * Specify user locale, for example {@code en-GB}, {@code de-DE}, etc. Locale will affect {@code navigator.language} value,
     * {@code Accept-Language} request header value as well as number and date formatting rules. Defaults to the system default
     * locale. Learn more about emulation in our <a
     * href="https://playwright.dev/java/docs/emulation#locale--timezone">emulation guide</a>.
     */
    public String locale;
    /**
     * Whether to emulate network being offline. Defaults to {@code false}. Learn more about <a
     * href="https://playwright.dev/java/docs/emulation#offline">network emulation</a>.
     */
    public Boolean offline;
    /**
     * A list of permissions to grant to all pages in this context. See {@link BrowserContext#grantPermissions
     * BrowserContext.grantPermissions()} for more details. Defaults to none.
     */
    public List<String> permissions;
    /**
     * Network proxy settings to use with this context. Defaults to none.
     *
     * <p> <strong>NOTE:</strong> For Chromium on Windows the browser needs to be launched with the global proxy for this option to work. If all contexts
     * override the proxy, global proxy will be never used and can be any string, for example {@code launch({ proxy: { server:
     * 'http://per-context' } })}.
     */
    public Proxy proxy;
    /**
     * Optional setting to control resource content management. If {@code omit} is specified, content is not persisted. If
     * {@code attach} is specified, resources are persisted as separate files and all of these files are archived along with
     * the HAR file. Defaults to {@code embed}, which stores content inline the HAR file as per HAR specification.
     */
    public HarContentPolicy recordHarContent;
    /**
     * When set to {@code minimal}, only record information necessary for routing from HAR. This omits sizes, timing, page,
     * cookies, security and other types of HAR information that are not used when replaying from HAR. Defaults to {@code
     * full}.
     */
    public HarMode recordHarMode;
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
    public Object recordHarUrlFilter;
    /**
     * Enables video recording for all pages into the specified directory. If not specified videos are not recorded. Make sure
     * to call {@link BrowserContext#close BrowserContext.close()} for videos to be saved.
     */
    public Path recordVideoDir;
    /**
     * Dimensions of the recorded videos. If not specified the size will be equal to {@code viewport} scaled down to fit into
     * 800x800. If {@code viewport} is not configured explicitly the video size defaults to 800x450. Actual picture of each
     * page will be scaled down if necessary to fit the specified size.
     */
    public RecordVideoSize recordVideoSize;
    /**
     * Emulates {@code "prefers-reduced-motion"} media feature, supported values are {@code "reduce"}, {@code "no-preference"}.
     * See {@link Page#emulateMedia Page.emulateMedia()} for more details. Passing {@code null} resets emulation to system
     * defaults. Defaults to {@code "no-preference"}.
     */
    public Optional<ReducedMotion> reducedMotion;
    /**
     * Emulates consistent window screen size available inside web page via {@code window.screen}. Is only used when the {@code
     * viewport} is set.
     */
    public ScreenSize screenSize;
    /**
     * Whether to allow sites to register Service workers. Defaults to {@code "allow"}.
     * <ul>
     * <li> {@code "allow"}: <a href="https://developer.mozilla.org/en-US/docs/Web/API/Service_Worker_API">Service Workers</a> can
     * be registered.</li>
     * <li> {@code "block"}: Playwright will block all registration of Service Workers.</li>
     * </ul>
     */
    public ServiceWorkerPolicy serviceWorkers;
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
     * If set to true, enables strict selectors mode for this context. In the strict selectors mode all operations on selectors
     * that imply single target DOM element will throw when more than one element matches the selector. This option does not
     * affect any Locator APIs (Locators are always strict). Defaults to {@code false}. See {@code Locator} to learn more about
     * the strict mode.
     */
    public Boolean strictSelectors;
    /**
     * Changes the timezone of the context. See <a
     * href="https://cs.chromium.org/chromium/src/third_party/icu/source/data/misc/metaZones.txt?rcl=faee8bc70570192d82d2978a71e2a615788597d1">ICU's
     * metaZones.txt</a> for a list of supported timezone IDs. Defaults to the system timezone.
     */
    public String timezoneId;
    /**
     * Specific user agent to use in this context.
     */
    public String userAgent;
    /**
     * Emulates consistent viewport for each page. Defaults to an 1280x720 viewport. Use {@code null} to disable the consistent
     * viewport emulation. Learn more about <a href="https://playwright.dev/java/docs/emulation#viewport">viewport
     * emulation</a>.
     *
     * <p> <strong>NOTE:</strong> The {@code null} value opts out from the default presets, makes viewport depend on the host window size defined by the
     * operating system. It makes the execution of the tests non-deterministic.
     */
    public Optional<ViewportSize> viewportSize;

    /**
     * Whether to automatically download all the attachments. Defaults to {@code true} where all the downloads are accepted.
     */
    public NewPageOptions setAcceptDownloads(boolean acceptDownloads) {
      this.acceptDownloads = acceptDownloads;
      return this;
    }
    /**
     * When using {@link Page#navigate Page.navigate()}, {@link Page#route Page.route()}, {@link Page#waitForURL
     * Page.waitForURL()}, {@link Page#waitForRequest Page.waitForRequest()}, or {@link Page#waitForResponse
     * Page.waitForResponse()} it takes the base URL in consideration by using the <a
     * href="https://developer.mozilla.org/en-US/docs/Web/API/URL/URL">{@code URL()}</a> constructor for building the
     * corresponding URL. Unset by default. Examples:
     * <ul>
     * <li> baseURL: {@code http://localhost:3000} and navigating to {@code /bar.html} results in {@code
     * http://localhost:3000/bar.html}</li>
     * <li> baseURL: {@code http://localhost:3000/foo/} and navigating to {@code ./bar.html} results in {@code
     * http://localhost:3000/foo/bar.html}</li>
     * <li> baseURL: {@code http://localhost:3000/foo} (without trailing slash) and navigating to {@code ./bar.html} results in
     * {@code http://localhost:3000/bar.html}</li>
     * </ul>
     */
    public NewPageOptions setBaseURL(String baseURL) {
      this.baseURL = baseURL;
      return this;
    }
    /**
     * Toggles bypassing page's Content-Security-Policy. Defaults to {@code false}.
     */
    public NewPageOptions setBypassCSP(boolean bypassCSP) {
      this.bypassCSP = bypassCSP;
      return this;
    }
    /**
     * Emulates {@code "prefers-colors-scheme"} media feature, supported values are {@code "light"}, {@code "dark"}, {@code
     * "no-preference"}. See {@link Page#emulateMedia Page.emulateMedia()} for more details. Passing {@code null} resets
     * emulation to system defaults. Defaults to {@code "light"}.
     */
    public NewPageOptions setColorScheme(ColorScheme colorScheme) {
      this.colorScheme = Optional.ofNullable(colorScheme);
      return this;
    }
    /**
     * Specify device scale factor (can be thought of as dpr). Defaults to {@code 1}. Learn more about <a
     * href="https://playwright.dev/java/docs/emulation#devices">emulating devices with device scale factor</a>.
     */
    public NewPageOptions setDeviceScaleFactor(double deviceScaleFactor) {
      this.deviceScaleFactor = deviceScaleFactor;
      return this;
    }
    /**
     * An object containing additional HTTP headers to be sent with every request. Defaults to none.
     */
    public NewPageOptions setExtraHTTPHeaders(Map<String, String> extraHTTPHeaders) {
      this.extraHTTPHeaders = extraHTTPHeaders;
      return this;
    }
    /**
     * Emulates {@code "forced-colors"} media feature, supported values are {@code "active"}, {@code "none"}. See {@link
     * Page#emulateMedia Page.emulateMedia()} for more details. Passing {@code null} resets emulation to system defaults.
     * Defaults to {@code "none"}.
     */
    public NewPageOptions setForcedColors(ForcedColors forcedColors) {
      this.forcedColors = Optional.ofNullable(forcedColors);
      return this;
    }
    public NewPageOptions setGeolocation(double latitude, double longitude) {
      return setGeolocation(new Geolocation(latitude, longitude));
    }
    public NewPageOptions setGeolocation(Geolocation geolocation) {
      this.geolocation = geolocation;
      return this;
    }
    /**
     * Specifies if viewport supports touch events. Defaults to false. Learn more about <a
     * href="https://playwright.dev/java/docs/emulation#devices">mobile emulation</a>.
     */
    public NewPageOptions setHasTouch(boolean hasTouch) {
      this.hasTouch = hasTouch;
      return this;
    }
    /**
     * Credentials for <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication">HTTP authentication</a>. If
     * no origin is specified, the username and password are sent to any servers upon unauthorized responses.
     */
    public NewPageOptions setHttpCredentials(String username, String password) {
      return setHttpCredentials(new HttpCredentials(username, password));
    }
    /**
     * Credentials for <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication">HTTP authentication</a>. If
     * no origin is specified, the username and password are sent to any servers upon unauthorized responses.
     */
    public NewPageOptions setHttpCredentials(HttpCredentials httpCredentials) {
      this.httpCredentials = httpCredentials;
      return this;
    }
    /**
     * Whether to ignore HTTPS errors when sending network requests. Defaults to {@code false}.
     */
    public NewPageOptions setIgnoreHTTPSErrors(boolean ignoreHTTPSErrors) {
      this.ignoreHTTPSErrors = ignoreHTTPSErrors;
      return this;
    }
    /**
     * Whether the {@code meta viewport} tag is taken into account and touch events are enabled. isMobile is a part of device,
     * so you don't actually need to set it manually. Defaults to {@code false} and is not supported in Firefox. Learn more
     * about <a href="https://playwright.dev/java/docs/emulation#ismobile">mobile emulation</a>.
     */
    public NewPageOptions setIsMobile(boolean isMobile) {
      this.isMobile = isMobile;
      return this;
    }
    /**
     * Whether or not to enable JavaScript in the context. Defaults to {@code true}. Learn more about <a
     * href="https://playwright.dev/java/docs/emulation#javascript-enabled">disabling JavaScript</a>.
     */
    public NewPageOptions setJavaScriptEnabled(boolean javaScriptEnabled) {
      this.javaScriptEnabled = javaScriptEnabled;
      return this;
    }
    /**
     * Specify user locale, for example {@code en-GB}, {@code de-DE}, etc. Locale will affect {@code navigator.language} value,
     * {@code Accept-Language} request header value as well as number and date formatting rules. Defaults to the system default
     * locale. Learn more about emulation in our <a
     * href="https://playwright.dev/java/docs/emulation#locale--timezone">emulation guide</a>.
     */
    public NewPageOptions setLocale(String locale) {
      this.locale = locale;
      return this;
    }
    /**
     * Whether to emulate network being offline. Defaults to {@code false}. Learn more about <a
     * href="https://playwright.dev/java/docs/emulation#offline">network emulation</a>.
     */
    public NewPageOptions setOffline(boolean offline) {
      this.offline = offline;
      return this;
    }
    /**
     * A list of permissions to grant to all pages in this context. See {@link BrowserContext#grantPermissions
     * BrowserContext.grantPermissions()} for more details. Defaults to none.
     */
    public NewPageOptions setPermissions(List<String> permissions) {
      this.permissions = permissions;
      return this;
    }
    /**
     * Network proxy settings to use with this context. Defaults to none.
     *
     * <p> <strong>NOTE:</strong> For Chromium on Windows the browser needs to be launched with the global proxy for this option to work. If all contexts
     * override the proxy, global proxy will be never used and can be any string, for example {@code launch({ proxy: { server:
     * 'http://per-context' } })}.
     */
    public NewPageOptions setProxy(String server) {
      return setProxy(new Proxy(server));
    }
    /**
     * Network proxy settings to use with this context. Defaults to none.
     *
     * <p> <strong>NOTE:</strong> For Chromium on Windows the browser needs to be launched with the global proxy for this option to work. If all contexts
     * override the proxy, global proxy will be never used and can be any string, for example {@code launch({ proxy: { server:
     * 'http://per-context' } })}.
     */
    public NewPageOptions setProxy(Proxy proxy) {
      this.proxy = proxy;
      return this;
    }
    /**
     * Optional setting to control resource content management. If {@code omit} is specified, content is not persisted. If
     * {@code attach} is specified, resources are persisted as separate files and all of these files are archived along with
     * the HAR file. Defaults to {@code embed}, which stores content inline the HAR file as per HAR specification.
     */
    public NewPageOptions setRecordHarContent(HarContentPolicy recordHarContent) {
      this.recordHarContent = recordHarContent;
      return this;
    }
    /**
     * When set to {@code minimal}, only record information necessary for routing from HAR. This omits sizes, timing, page,
     * cookies, security and other types of HAR information that are not used when replaying from HAR. Defaults to {@code
     * full}.
     */
    public NewPageOptions setRecordHarMode(HarMode recordHarMode) {
      this.recordHarMode = recordHarMode;
      return this;
    }
    /**
     * Optional setting to control whether to omit request content from the HAR. Defaults to {@code false}.
     */
    public NewPageOptions setRecordHarOmitContent(boolean recordHarOmitContent) {
      this.recordHarOmitContent = recordHarOmitContent;
      return this;
    }
    /**
     * Enables <a href="http://www.softwareishard.com/blog/har-12-spec">HAR</a> recording for all pages into the specified HAR
     * file on the filesystem. If not specified, the HAR is not recorded. Make sure to call {@link BrowserContext#close
     * BrowserContext.close()} for the HAR to be saved.
     */
    public NewPageOptions setRecordHarPath(Path recordHarPath) {
      this.recordHarPath = recordHarPath;
      return this;
    }
    public NewPageOptions setRecordHarUrlFilter(String recordHarUrlFilter) {
      this.recordHarUrlFilter = recordHarUrlFilter;
      return this;
    }
    public NewPageOptions setRecordHarUrlFilter(Pattern recordHarUrlFilter) {
      this.recordHarUrlFilter = recordHarUrlFilter;
      return this;
    }
    /**
     * Enables video recording for all pages into the specified directory. If not specified videos are not recorded. Make sure
     * to call {@link BrowserContext#close BrowserContext.close()} for videos to be saved.
     */
    public NewPageOptions setRecordVideoDir(Path recordVideoDir) {
      this.recordVideoDir = recordVideoDir;
      return this;
    }
    /**
     * Dimensions of the recorded videos. If not specified the size will be equal to {@code viewport} scaled down to fit into
     * 800x800. If {@code viewport} is not configured explicitly the video size defaults to 800x450. Actual picture of each
     * page will be scaled down if necessary to fit the specified size.
     */
    public NewPageOptions setRecordVideoSize(int width, int height) {
      return setRecordVideoSize(new RecordVideoSize(width, height));
    }
    /**
     * Dimensions of the recorded videos. If not specified the size will be equal to {@code viewport} scaled down to fit into
     * 800x800. If {@code viewport} is not configured explicitly the video size defaults to 800x450. Actual picture of each
     * page will be scaled down if necessary to fit the specified size.
     */
    public NewPageOptions setRecordVideoSize(RecordVideoSize recordVideoSize) {
      this.recordVideoSize = recordVideoSize;
      return this;
    }
    /**
     * Emulates {@code "prefers-reduced-motion"} media feature, supported values are {@code "reduce"}, {@code "no-preference"}.
     * See {@link Page#emulateMedia Page.emulateMedia()} for more details. Passing {@code null} resets emulation to system
     * defaults. Defaults to {@code "no-preference"}.
     */
    public NewPageOptions setReducedMotion(ReducedMotion reducedMotion) {
      this.reducedMotion = Optional.ofNullable(reducedMotion);
      return this;
    }
    /**
     * Emulates consistent window screen size available inside web page via {@code window.screen}. Is only used when the {@code
     * viewport} is set.
     */
    public NewPageOptions setScreenSize(int width, int height) {
      return setScreenSize(new ScreenSize(width, height));
    }
    /**
     * Emulates consistent window screen size available inside web page via {@code window.screen}. Is only used when the {@code
     * viewport} is set.
     */
    public NewPageOptions setScreenSize(ScreenSize screenSize) {
      this.screenSize = screenSize;
      return this;
    }
    /**
     * Whether to allow sites to register Service workers. Defaults to {@code "allow"}.
     * <ul>
     * <li> {@code "allow"}: <a href="https://developer.mozilla.org/en-US/docs/Web/API/Service_Worker_API">Service Workers</a> can
     * be registered.</li>
     * <li> {@code "block"}: Playwright will block all registration of Service Workers.</li>
     * </ul>
     */
    public NewPageOptions setServiceWorkers(ServiceWorkerPolicy serviceWorkers) {
      this.serviceWorkers = serviceWorkers;
      return this;
    }
    /**
     * Populates context with given storage state. This option can be used to initialize context with logged-in information
     * obtained via {@link BrowserContext#storageState BrowserContext.storageState()}.
     */
    public NewPageOptions setStorageState(String storageState) {
      this.storageState = storageState;
      return this;
    }
    /**
     * Populates context with given storage state. This option can be used to initialize context with logged-in information
     * obtained via {@link BrowserContext#storageState BrowserContext.storageState()}. Path to the file with saved storage
     * state.
     */
    public NewPageOptions setStorageStatePath(Path storageStatePath) {
      this.storageStatePath = storageStatePath;
      return this;
    }
    /**
     * If set to true, enables strict selectors mode for this context. In the strict selectors mode all operations on selectors
     * that imply single target DOM element will throw when more than one element matches the selector. This option does not
     * affect any Locator APIs (Locators are always strict). Defaults to {@code false}. See {@code Locator} to learn more about
     * the strict mode.
     */
    public NewPageOptions setStrictSelectors(boolean strictSelectors) {
      this.strictSelectors = strictSelectors;
      return this;
    }
    /**
     * Changes the timezone of the context. See <a
     * href="https://cs.chromium.org/chromium/src/third_party/icu/source/data/misc/metaZones.txt?rcl=faee8bc70570192d82d2978a71e2a615788597d1">ICU's
     * metaZones.txt</a> for a list of supported timezone IDs. Defaults to the system timezone.
     */
    public NewPageOptions setTimezoneId(String timezoneId) {
      this.timezoneId = timezoneId;
      return this;
    }
    /**
     * Specific user agent to use in this context.
     */
    public NewPageOptions setUserAgent(String userAgent) {
      this.userAgent = userAgent;
      return this;
    }
    /**
     * Emulates consistent viewport for each page. Defaults to an 1280x720 viewport. Use {@code null} to disable the consistent
     * viewport emulation. Learn more about <a href="https://playwright.dev/java/docs/emulation#viewport">viewport
     * emulation</a>.
     *
     * <p> <strong>NOTE:</strong> The {@code null} value opts out from the default presets, makes viewport depend on the host window size defined by the
     * operating system. It makes the execution of the tests non-deterministic.
     */
    public NewPageOptions setViewportSize(int width, int height) {
      return setViewportSize(new ViewportSize(width, height));
    }
    /**
     * Emulates consistent viewport for each page. Defaults to an 1280x720 viewport. Use {@code null} to disable the consistent
     * viewport emulation. Learn more about <a href="https://playwright.dev/java/docs/emulation#viewport">viewport
     * emulation</a>.
     *
     * <p> <strong>NOTE:</strong> The {@code null} value opts out from the default presets, makes viewport depend on the host window size defined by the
     * operating system. It makes the execution of the tests non-deterministic.
     */
    public NewPageOptions setViewportSize(ViewportSize viewportSize) {
      this.viewportSize = Optional.ofNullable(viewportSize);
      return this;
    }
  }
  class StartTracingOptions {
    /**
     * specify custom categories to use instead of default.
     */
    public List<String> categories;
    /**
     * A path to write the trace file to.
     */
    public Path path;
    /**
     * captures screenshots in the trace.
     */
    public Boolean screenshots;

    /**
     * specify custom categories to use instead of default.
     */
    public StartTracingOptions setCategories(List<String> categories) {
      this.categories = categories;
      return this;
    }
    /**
     * A path to write the trace file to.
     */
    public StartTracingOptions setPath(Path path) {
      this.path = path;
      return this;
    }
    /**
     * captures screenshots in the trace.
     */
    public StartTracingOptions setScreenshots(boolean screenshots) {
      this.screenshots = screenshots;
      return this;
    }
  }
  /**
   * Get the browser type (chromium, firefox or webkit) that the browser belongs to.
   *
   * @since v1.23
   */
  BrowserType browserType();
  /**
   * In case this browser is obtained using {@link BrowserType#launch BrowserType.launch()}, closes the browser and all of
   * its pages (if any were opened).
   *
   * <p> In case this browser is connected to, clears all created contexts belonging to this browser and disconnects from the
   * browser server.
   *
   * <p> <strong>NOTE:</strong> This is similar to force quitting the browser. Therefore, you should call {@link BrowserContext#close
   * BrowserContext.close()} on any {@code BrowserContext}'s you explicitly created earlier with {@link Browser#newContext
   * Browser.newContext()} **before** calling {@link Browser#close Browser.close()}.
   *
   * <p> The {@code Browser} object itself is considered to be disposed and cannot be used anymore.
   *
   * @since v1.8
   */
  void close();
  /**
   * Returns an array of all open browser contexts. In a newly created browser, this will return zero browser contexts.
   *
   * <p> **Usage**
   * <pre>{@code
   * Browser browser = pw.webkit().launch();
   * System.out.println(browser.contexts().size()); // prints "0"
   * BrowserContext context = browser.newContext();
   * System.out.println(browser.contexts().size()); // prints "1"
   * }</pre>
   *
   * @since v1.8
   */
  List<BrowserContext> contexts();
  /**
   * Indicates that the browser is connected.
   *
   * @since v1.8
   */
  boolean isConnected();
  /**
   * <strong>NOTE:</strong> CDP Sessions are only supported on Chromium-based browsers.
   *
   * <p> Returns the newly created browser session.
   *
   * @since v1.11
   */
  CDPSession newBrowserCDPSession();
  /**
   * Creates a new browser context. It won't share cookies/cache with other browser contexts.
   *
   * <p> <strong>NOTE:</strong> If directly using this method to create {@code BrowserContext}s, it is best practice to explicitly close the returned
   * context via {@link BrowserContext#close BrowserContext.close()} when your code is done with the {@code BrowserContext},
   * and before calling {@link Browser#close Browser.close()}. This will ensure the {@code context} is closed gracefully and
   * any artifacts—like HARs and videos—are fully flushed and saved.
   *
   * <p> **Usage**
   * <pre>{@code
   * Browser browser = playwright.firefox().launch();  // Or 'chromium' or 'webkit'.
   * // Create a new incognito browser context.
   * BrowserContext context = browser.newContext();
   * // Create a new page in a pristine context.
   * Page page = context.newPage();
   * page.navigate('https://example.com');
   *
   * // Graceful close up everything
   * context.close();
   * browser.close();
   * }</pre>
   *
   * @since v1.8
   */
  default BrowserContext newContext() {
    return newContext(null);
  }
  /**
   * Creates a new browser context. It won't share cookies/cache with other browser contexts.
   *
   * <p> <strong>NOTE:</strong> If directly using this method to create {@code BrowserContext}s, it is best practice to explicitly close the returned
   * context via {@link BrowserContext#close BrowserContext.close()} when your code is done with the {@code BrowserContext},
   * and before calling {@link Browser#close Browser.close()}. This will ensure the {@code context} is closed gracefully and
   * any artifacts—like HARs and videos—are fully flushed and saved.
   *
   * <p> **Usage**
   * <pre>{@code
   * Browser browser = playwright.firefox().launch();  // Or 'chromium' or 'webkit'.
   * // Create a new incognito browser context.
   * BrowserContext context = browser.newContext();
   * // Create a new page in a pristine context.
   * Page page = context.newPage();
   * page.navigate('https://example.com');
   *
   * // Graceful close up everything
   * context.close();
   * browser.close();
   * }</pre>
   *
   * @since v1.8
   */
  BrowserContext newContext(NewContextOptions options);
  /**
   * Creates a new page in a new browser context. Closing this page will close the context as well.
   *
   * <p> This is a convenience API that should only be used for the single-page scenarios and short snippets. Production code and
   * testing frameworks should explicitly create {@link Browser#newContext Browser.newContext()} followed by the {@link
   * BrowserContext#newPage BrowserContext.newPage()} to control their exact life times.
   *
   * @since v1.8
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
   *
   * @since v1.8
   */
  Page newPage(NewPageOptions options);
  /**
   * <strong>NOTE:</strong> This API controls <a href="https://www.chromium.org/developers/how-tos/trace-event-profiling-tool">Chromium Tracing</a>
   * which is a low-level chromium-specific debugging tool. API to control <a
   * href="https://playwright.dev/java/docs/trace-viewer">Playwright Tracing</a> could be found <a
   * href="https://playwright.dev/java/docs/api/class-tracing">here</a>.
   *
   * <p> You can use {@link Browser#startTracing Browser.startTracing()} and {@link Browser#stopTracing Browser.stopTracing()} to
   * create a trace file that can be opened in Chrome DevTools performance panel.
   *
   * <p> **Usage**
   * <pre>{@code
   * browser.startTracing(page, new Browser.StartTracingOptions()
   *   .setPath(Paths.get("trace.json")));
   * page.goto('https://www.google.com');
   * browser.stopTracing();
   * }</pre>
   *
   * @param page Optional, if specified, tracing includes screenshots of the given page.
   * @since v1.11
   */
  default void startTracing(Page page) {
    startTracing(page, null);
  }
  /**
   * <strong>NOTE:</strong> This API controls <a href="https://www.chromium.org/developers/how-tos/trace-event-profiling-tool">Chromium Tracing</a>
   * which is a low-level chromium-specific debugging tool. API to control <a
   * href="https://playwright.dev/java/docs/trace-viewer">Playwright Tracing</a> could be found <a
   * href="https://playwright.dev/java/docs/api/class-tracing">here</a>.
   *
   * <p> You can use {@link Browser#startTracing Browser.startTracing()} and {@link Browser#stopTracing Browser.stopTracing()} to
   * create a trace file that can be opened in Chrome DevTools performance panel.
   *
   * <p> **Usage**
   * <pre>{@code
   * browser.startTracing(page, new Browser.StartTracingOptions()
   *   .setPath(Paths.get("trace.json")));
   * page.goto('https://www.google.com');
   * browser.stopTracing();
   * }</pre>
   *
   * @since v1.11
   */
  default void startTracing() {
    startTracing(null);
  }
  /**
   * <strong>NOTE:</strong> This API controls <a href="https://www.chromium.org/developers/how-tos/trace-event-profiling-tool">Chromium Tracing</a>
   * which is a low-level chromium-specific debugging tool. API to control <a
   * href="https://playwright.dev/java/docs/trace-viewer">Playwright Tracing</a> could be found <a
   * href="https://playwright.dev/java/docs/api/class-tracing">here</a>.
   *
   * <p> You can use {@link Browser#startTracing Browser.startTracing()} and {@link Browser#stopTracing Browser.stopTracing()} to
   * create a trace file that can be opened in Chrome DevTools performance panel.
   *
   * <p> **Usage**
   * <pre>{@code
   * browser.startTracing(page, new Browser.StartTracingOptions()
   *   .setPath(Paths.get("trace.json")));
   * page.goto('https://www.google.com');
   * browser.stopTracing();
   * }</pre>
   *
   * @param page Optional, if specified, tracing includes screenshots of the given page.
   * @since v1.11
   */
  void startTracing(Page page, StartTracingOptions options);
  /**
   * <strong>NOTE:</strong> This API controls <a href="https://www.chromium.org/developers/how-tos/trace-event-profiling-tool">Chromium Tracing</a>
   * which is a low-level chromium-specific debugging tool. API to control <a
   * href="https://playwright.dev/java/docs/trace-viewer">Playwright Tracing</a> could be found <a
   * href="https://playwright.dev/java/docs/api/class-tracing">here</a>.
   *
   * <p> Returns the buffer with trace data.
   *
   * @since v1.11
   */
  byte[] stopTracing();
  /**
   * Returns the browser version.
   *
   * @since v1.8
   */
  String version();
}

