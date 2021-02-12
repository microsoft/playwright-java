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
 * - extends: [EventEmitter]
 *
 * <p> A Browser is created via [{@code method: BrowserType.launch}]. An example of using a {@code Browser} to create a {@code Page}:
 */
public interface Browser extends AutoCloseable {

  void onDisconnected(Consumer<Browser> handler);
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
     * Emulates {@code 'prefers-colors-scheme'} media feature, supported values are {@code 'light'}, {@code 'dark'}, {@code 'no-preference'}. See
     * [{@code method: Page.emulateMedia}] for more details. Defaults to '{@code light}'.
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
     * Credentials for [HTTP authentication](https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication).
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
     * A list of permissions to grant to all pages in this context. See [{@code method: BrowserContext.grantPermissions}] for more
     * details.
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
     * Path on the filesystem to write the HAR file to.
     */
    public Path recordHarPath;
    /**
     * Path to the directory to put videos into.
     */
    public Path recordVideoDir;
    /**
     * Dimensions of the recorded videos. If not specified the size will be equal to {@code viewport} scaled down to fit into
     * 800x800. If {@code viewport} is not configured explicitly the video size defaults to 800x450. Actual picture of each page will
     * be scaled down if necessary to fit the specified size.
     */
    public RecordVideoSize recordVideoSize;
    /**
     * Populates context with given storage state. This option can be used to initialize context with logged-in information
     * obtained via [{@code method: BrowserContext.storageState}].
     */
    public String storageState;
    /**
     * Populates context with given storage state. This option can be used to initialize context with logged-in information
     * obtained via [{@code method: BrowserContext.storageState}]. Path to the file with saved storage state.
     */
    public Path storageStatePath;
    /**
     * Changes the timezone of the context. See
     * [ICU's metaZones.txt](https://cs.chromium.org/chromium/src/third_party/icu/source/data/misc/metaZones.txt?rcl=faee8bc70570192d82d2978a71e2a615788597d1)
     * for a list of supported timezone IDs.
     */
    public String timezoneId;
    /**
     * Specific user agent to use in this context.
     */
    public String userAgent;
    /**
     * Sets a consistent viewport for each page. Defaults to an 1280x720 viewport. {@code null} disables the default viewport.
     */
    public Optional<ViewportSize> viewportSize;

    public NewContextOptions withAcceptDownloads(boolean acceptDownloads) {
      this.acceptDownloads = acceptDownloads;
      return this;
    }
    public NewContextOptions withBypassCSP(boolean bypassCSP) {
      this.bypassCSP = bypassCSP;
      return this;
    }
    public NewContextOptions withColorScheme(ColorScheme colorScheme) {
      this.colorScheme = colorScheme;
      return this;
    }
    public NewContextOptions withDeviceScaleFactor(double deviceScaleFactor) {
      this.deviceScaleFactor = deviceScaleFactor;
      return this;
    }
    public NewContextOptions withExtraHTTPHeaders(Map<String, String> extraHTTPHeaders) {
      this.extraHTTPHeaders = extraHTTPHeaders;
      return this;
    }
    public NewContextOptions withGeolocation(Geolocation geolocation) {
      this.geolocation = geolocation;
      return this;
    }
    public NewContextOptions withHasTouch(boolean hasTouch) {
      this.hasTouch = hasTouch;
      return this;
    }
    public NewContextOptions withHttpCredentials(String username, String password) {
      this.httpCredentials = new HttpCredentials(username, password);
      return this;
    }
    public NewContextOptions withIgnoreHTTPSErrors(boolean ignoreHTTPSErrors) {
      this.ignoreHTTPSErrors = ignoreHTTPSErrors;
      return this;
    }
    public NewContextOptions withIsMobile(boolean isMobile) {
      this.isMobile = isMobile;
      return this;
    }
    public NewContextOptions withJavaScriptEnabled(boolean javaScriptEnabled) {
      this.javaScriptEnabled = javaScriptEnabled;
      return this;
    }
    public NewContextOptions withLocale(String locale) {
      this.locale = locale;
      return this;
    }
    public NewContextOptions withOffline(boolean offline) {
      this.offline = offline;
      return this;
    }
    public NewContextOptions withPermissions(List<String> permissions) {
      this.permissions = permissions;
      return this;
    }
    public NewContextOptions withProxy(Proxy proxy) {
      this.proxy = proxy;
      return this;
    }
    public NewContextOptions withRecordHarOmitContent(boolean recordHarOmitContent) {
      this.recordHarOmitContent = recordHarOmitContent;
      return this;
    }
    public NewContextOptions withRecordHarPath(Path recordHarPath) {
      this.recordHarPath = recordHarPath;
      return this;
    }
    public NewContextOptions withRecordVideoDir(Path recordVideoDir) {
      this.recordVideoDir = recordVideoDir;
      return this;
    }
    public NewContextOptions withRecordVideoSize(RecordVideoSize recordVideoSize) {
      this.recordVideoSize = recordVideoSize;
      return this;
    }
    public NewContextOptions withStorageState(String storageState) {
      this.storageState = storageState;
      return this;
    }
    public NewContextOptions withStorageStatePath(Path storageStatePath) {
      this.storageStatePath = storageStatePath;
      return this;
    }
    public NewContextOptions withTimezoneId(String timezoneId) {
      this.timezoneId = timezoneId;
      return this;
    }
    public NewContextOptions withUserAgent(String userAgent) {
      this.userAgent = userAgent;
      return this;
    }
    public NewContextOptions withViewportSize(int width, int height) {
      return withViewportSize(new ViewportSize(width, height));
    }
    public NewContextOptions withViewportSize(ViewportSize viewportSize) {
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
     * Emulates {@code 'prefers-colors-scheme'} media feature, supported values are {@code 'light'}, {@code 'dark'}, {@code 'no-preference'}. See
     * [{@code method: Page.emulateMedia}] for more details. Defaults to '{@code light}'.
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
     * Credentials for [HTTP authentication](https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication).
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
     * A list of permissions to grant to all pages in this context. See [{@code method: BrowserContext.grantPermissions}] for more
     * details.
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
     * Path on the filesystem to write the HAR file to.
     */
    public Path recordHarPath;
    /**
     * Path to the directory to put videos into.
     */
    public Path recordVideoDir;
    /**
     * Dimensions of the recorded videos. If not specified the size will be equal to {@code viewport} scaled down to fit into
     * 800x800. If {@code viewport} is not configured explicitly the video size defaults to 800x450. Actual picture of each page will
     * be scaled down if necessary to fit the specified size.
     */
    public RecordVideoSize recordVideoSize;
    /**
     * Populates context with given storage state. This option can be used to initialize context with logged-in information
     * obtained via [{@code method: BrowserContext.storageState}].
     */
    public String storageState;
    /**
     * Populates context with given storage state. This option can be used to initialize context with logged-in information
     * obtained via [{@code method: BrowserContext.storageState}]. Path to the file with saved storage state.
     */
    public Path storageStatePath;
    /**
     * Changes the timezone of the context. See
     * [ICU's metaZones.txt](https://cs.chromium.org/chromium/src/third_party/icu/source/data/misc/metaZones.txt?rcl=faee8bc70570192d82d2978a71e2a615788597d1)
     * for a list of supported timezone IDs.
     */
    public String timezoneId;
    /**
     * Specific user agent to use in this context.
     */
    public String userAgent;
    /**
     * Sets a consistent viewport for each page. Defaults to an 1280x720 viewport. {@code null} disables the default viewport.
     */
    public Optional<ViewportSize> viewportSize;

    public NewPageOptions withAcceptDownloads(boolean acceptDownloads) {
      this.acceptDownloads = acceptDownloads;
      return this;
    }
    public NewPageOptions withBypassCSP(boolean bypassCSP) {
      this.bypassCSP = bypassCSP;
      return this;
    }
    public NewPageOptions withColorScheme(ColorScheme colorScheme) {
      this.colorScheme = colorScheme;
      return this;
    }
    public NewPageOptions withDeviceScaleFactor(double deviceScaleFactor) {
      this.deviceScaleFactor = deviceScaleFactor;
      return this;
    }
    public NewPageOptions withExtraHTTPHeaders(Map<String, String> extraHTTPHeaders) {
      this.extraHTTPHeaders = extraHTTPHeaders;
      return this;
    }
    public NewPageOptions withGeolocation(Geolocation geolocation) {
      this.geolocation = geolocation;
      return this;
    }
    public NewPageOptions withHasTouch(boolean hasTouch) {
      this.hasTouch = hasTouch;
      return this;
    }
    public NewPageOptions withHttpCredentials(String username, String password) {
      this.httpCredentials = new HttpCredentials(username, password);
      return this;
    }
    public NewPageOptions withIgnoreHTTPSErrors(boolean ignoreHTTPSErrors) {
      this.ignoreHTTPSErrors = ignoreHTTPSErrors;
      return this;
    }
    public NewPageOptions withIsMobile(boolean isMobile) {
      this.isMobile = isMobile;
      return this;
    }
    public NewPageOptions withJavaScriptEnabled(boolean javaScriptEnabled) {
      this.javaScriptEnabled = javaScriptEnabled;
      return this;
    }
    public NewPageOptions withLocale(String locale) {
      this.locale = locale;
      return this;
    }
    public NewPageOptions withOffline(boolean offline) {
      this.offline = offline;
      return this;
    }
    public NewPageOptions withPermissions(List<String> permissions) {
      this.permissions = permissions;
      return this;
    }
    public NewPageOptions withProxy(Proxy proxy) {
      this.proxy = proxy;
      return this;
    }
    public NewPageOptions withRecordHarOmitContent(boolean recordHarOmitContent) {
      this.recordHarOmitContent = recordHarOmitContent;
      return this;
    }
    public NewPageOptions withRecordHarPath(Path recordHarPath) {
      this.recordHarPath = recordHarPath;
      return this;
    }
    public NewPageOptions withRecordVideoDir(Path recordVideoDir) {
      this.recordVideoDir = recordVideoDir;
      return this;
    }
    public NewPageOptions withRecordVideoSize(RecordVideoSize recordVideoSize) {
      this.recordVideoSize = recordVideoSize;
      return this;
    }
    public NewPageOptions withStorageState(String storageState) {
      this.storageState = storageState;
      return this;
    }
    public NewPageOptions withStorageStatePath(Path storageStatePath) {
      this.storageStatePath = storageStatePath;
      return this;
    }
    public NewPageOptions withTimezoneId(String timezoneId) {
      this.timezoneId = timezoneId;
      return this;
    }
    public NewPageOptions withUserAgent(String userAgent) {
      this.userAgent = userAgent;
      return this;
    }
    public NewPageOptions withViewportSize(int width, int height) {
      return withViewportSize(new ViewportSize(width, height));
    }
    public NewPageOptions withViewportSize(ViewportSize viewportSize) {
      this.viewportSize = Optional.ofNullable(viewportSize);
      return this;
    }
  }
  /**
   * In case this browser is obtained using [{@code method: BrowserType.launch}], closes the browser and all of its pages (if any
   * were opened).
   *
   * <p> In case this browser is connected to, clears all created contexts belonging to this browser and disconnects from the
   * browser server.
   *
   * <p> The {@code Browser} object itself is considered to be disposed and cannot be used anymore.
   */
  void close();
  /**
   * Returns an array of all open browser contexts. In a newly created browser, this will return zero browser contexts.
   */
  List<BrowserContext> contexts();
  /**
   * Indicates that the browser is connected.
   */
  boolean isConnected();
  /**
   * Creates a new browser context. It won't share cookies/cache with other browser contexts.
   */
  default BrowserContext newContext() {
    return newContext(null);
  }
  /**
   * Creates a new browser context. It won't share cookies/cache with other browser contexts.
   */
  BrowserContext newContext(NewContextOptions options);
  /**
   * Creates a new page in a new browser context. Closing this page will close the context as well.
   *
   * <p> This is a convenience API that should only be used for the single-page scenarios and short snippets. Production code and
   * testing frameworks should explicitly create [{@code method: Browser.newContext}] followed by the
   * [{@code method: BrowserContext.newPage}] to control their exact life times.
   */
  default Page newPage() {
    return newPage(null);
  }
  /**
   * Creates a new page in a new browser context. Closing this page will close the context as well.
   *
   * <p> This is a convenience API that should only be used for the single-page scenarios and short snippets. Production code and
   * testing frameworks should explicitly create [{@code method: Browser.newContext}] followed by the
   * [{@code method: BrowserContext.newPage}] to control their exact life times.
   */
  Page newPage(NewPageOptions options);
  /**
   * Returns the browser version.
   */
  String version();
}

