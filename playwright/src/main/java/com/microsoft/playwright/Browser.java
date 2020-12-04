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

/**
 * A Browser is created when Playwright connects to a browser instance, either through {@code browserType.launch} or {@code browserType.connect}.
 * <p>
 */
public interface Browser {
  class VideoSize {
    private final int width;
    private final int height;

    public VideoSize(int width, int height) {
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

  enum EventType {
    DISCONNECTED,
  }

  void addListener(EventType type, Listener<EventType> listener);
  void removeListener(EventType type, Listener<EventType> listener);
  class NewContextOptions {
    public class RecordHar {
      /**
       * Optional setting to control whether to omit request content from the HAR. Defaults to {@code false}.
       */
      public Boolean omitContent;
      /**
       * Path on the filesystem to write the HAR file to.
       */
      public Path path;

      RecordHar() {
      }
      public NewContextOptions done() {
        return NewContextOptions.this;
      }

      public RecordHar withOmitContent(Boolean omitContent) {
        this.omitContent = omitContent;
        return this;
      }
      public RecordHar withPath(Path path) {
        this.path = path;
        return this;
      }
    }
    public class RecordVideo {
      /**
       * Path to the directory to put videos into.
       */
      public Path dir;
      /**
       * Optional dimensions of the recorded videos. If not specified the size will be equal to {@code viewport}. If {@code viewport} is not configured explicitly the video size defaults to 1280x720. Actual picture of each page will be scaled down if necessary to fit the specified size.
       */
      public VideoSize size;

      RecordVideo() {
      }
      public NewContextOptions done() {
        return NewContextOptions.this;
      }

      public RecordVideo withDir(Path dir) {
        this.dir = dir;
        return this;
      }
      public RecordVideo withSize(int width, int height) {
        this.size = new VideoSize(width, height);
        return this;
      }
    }
    public class Proxy {
      /**
       * Proxy to be used for all requests. HTTP and SOCKS proxies are supported, for example {@code http://myproxy.com:3128} or {@code socks5://myproxy.com:3128}. Short form {@code myproxy.com:3128} is considered an HTTP proxy.
       */
      public String server;
      /**
       * Optional coma-separated domains to bypass proxy, for example {@code ".com, chromium.org, .domain.com"}.
       */
      public String bypass;
      /**
       * Optional username to use if HTTP proxy requires authentication.
       */
      public String username;
      /**
       * Optional password to use if HTTP proxy requires authentication.
       */
      public String password;

      Proxy() {
      }
      public NewContextOptions done() {
        return NewContextOptions.this;
      }

      public Proxy withServer(String server) {
        this.server = server;
        return this;
      }
      public Proxy withBypass(String bypass) {
        this.bypass = bypass;
        return this;
      }
      public Proxy withUsername(String username) {
        this.username = username;
        return this;
      }
      public Proxy withPassword(String password) {
        this.password = password;
        return this;
      }
    }
    /**
     * Whether to automatically download all the attachments. Defaults to {@code false} where all the downloads are canceled.
     */
    public Boolean acceptDownloads;
    /**
     * Whether to ignore HTTPS errors during navigation. Defaults to {@code false}.
     */
    public Boolean ignoreHTTPSErrors;
    /**
     * Toggles bypassing page's Content-Security-Policy.
     */
    public Boolean bypassCSP;
    /**
     * Sets a consistent viewport for each page. Defaults to an 1280x720 viewport. {@code null} disables the default viewport.
     */
    public Page.Viewport viewport;
    /**
     * Specific user agent to use in this context.
     */
    public String userAgent;
    /**
     * Specify device scale factor (can be thought of as dpr). Defaults to {@code 1}.
     */
    public Integer deviceScaleFactor;
    /**
     * Whether the {@code meta viewport} tag is taken into account and touch events are enabled. Defaults to {@code false}. Not supported in Firefox.
     */
    public Boolean isMobile;
    /**
     * Specifies if viewport supports touch events. Defaults to false.
     */
    public Boolean hasTouch;
    /**
     * Whether or not to enable JavaScript in the context. Defaults to {@code true}.
     */
    public Boolean javaScriptEnabled;
    /**
     * Changes the timezone of the context. See ICU’s {@code metaZones.txt} for a list of supported timezone IDs.
     */
    public String timezoneId;
    public Geolocation geolocation;
    /**
     * Specify user locale, for example {@code en-GB}, {@code de-DE}, etc. Locale will affect {@code navigator.language} value, {@code Accept-Language} request header value as well as number and date formatting rules.
     */
    public String locale;
    /**
     * A list of permissions to grant to all pages in this context. See browserContext.grantPermissions for more details.
     */
    public List<String> permissions;
    /**
     * An object containing additional HTTP headers to be sent with every request. All header values must be strings.
     */
    public Map<String, String> extraHTTPHeaders;
    /**
     * Whether to emulate network being offline. Defaults to {@code false}.
     */
    public Boolean offline;
    /**
     * Credentials for HTTP authentication.
     */
    public BrowserContext.HTTPCredentials httpCredentials;
    /**
     * Emulates {@code 'prefers-colors-scheme'} media feature, supported values are {@code 'light'}, {@code 'dark'}, {@code 'no-preference'}. See page.emulateMedia(options) for more details. Defaults to '{@code light}'.
     */
    public ColorScheme colorScheme;
    /**
     * Logger sink for Playwright logging.
     */
    public Logger logger;
    /**
     * Enables HAR recording for all pages into {@code recordHar.path} file. If not specified, the HAR is not recorded. Make sure to await {@code browserContext.close} for the HAR to be saved.
     */
    public RecordHar recordHar;
    /**
     * Enables video recording for all pages into {@code recordVideo.dir} directory. If not specified videos are not recorded. Make sure to await {@code browserContext.close} for videos to be saved.
     */
    public RecordVideo recordVideo;
    /**
     * Network proxy settings to use with this context. Note that browser needs to be launched with the global proxy for this option to work. If all contexts override the proxy, global proxy will be never used and can be any string, for example {@code launch({ proxy: { server: 'per-context' } })}.
     */
    public Proxy proxy;
    /**
     * Populates context with given storage state. This method can be used to initialize context with logged-in information obtained via browserContext.storageState().
     */
    public BrowserContext.StorageState storageState;

    public NewContextOptions withAcceptDownloads(Boolean acceptDownloads) {
      this.acceptDownloads = acceptDownloads;
      return this;
    }
    public NewContextOptions withIgnoreHTTPSErrors(Boolean ignoreHTTPSErrors) {
      this.ignoreHTTPSErrors = ignoreHTTPSErrors;
      return this;
    }
    public NewContextOptions withBypassCSP(Boolean bypassCSP) {
      this.bypassCSP = bypassCSP;
      return this;
    }
    public NewContextOptions withViewport(int width, int height) {
      this.viewport = new Page.Viewport(width, height);
      return this;
    }
    public NewContextOptions withUserAgent(String userAgent) {
      this.userAgent = userAgent;
      return this;
    }
    public NewContextOptions withDeviceScaleFactor(Integer deviceScaleFactor) {
      this.deviceScaleFactor = deviceScaleFactor;
      return this;
    }
    public NewContextOptions withIsMobile(Boolean isMobile) {
      this.isMobile = isMobile;
      return this;
    }
    public NewContextOptions withHasTouch(Boolean hasTouch) {
      this.hasTouch = hasTouch;
      return this;
    }
    public NewContextOptions withJavaScriptEnabled(Boolean javaScriptEnabled) {
      this.javaScriptEnabled = javaScriptEnabled;
      return this;
    }
    public NewContextOptions withTimezoneId(String timezoneId) {
      this.timezoneId = timezoneId;
      return this;
    }
    public NewContextOptions withGeolocation(Geolocation geolocation) {
      this.geolocation = geolocation;
      return this;
    }
    public NewContextOptions withLocale(String locale) {
      this.locale = locale;
      return this;
    }
    public NewContextOptions withPermissions(List<String> permissions) {
      this.permissions = permissions;
      return this;
    }
    public NewContextOptions withExtraHTTPHeaders(Map<String, String> extraHTTPHeaders) {
      this.extraHTTPHeaders = extraHTTPHeaders;
      return this;
    }
    public NewContextOptions withOffline(Boolean offline) {
      this.offline = offline;
      return this;
    }
    public NewContextOptions withHttpCredentials(String username, String password) {
      this.httpCredentials = new BrowserContext.HTTPCredentials(username, password);
      return this;
    }
    public NewContextOptions withColorScheme(ColorScheme colorScheme) {
      this.colorScheme = colorScheme;
      return this;
    }
    public NewContextOptions withLogger(Logger logger) {
      this.logger = logger;
      return this;
    }
    public RecordHar setRecordHar() {
      this.recordHar = new RecordHar();
      return this.recordHar;
    }
    public RecordVideo setRecordVideo() {
      this.recordVideo = new RecordVideo();
      return this.recordVideo;
    }
    public Proxy setProxy() {
      this.proxy = new Proxy();
      return this.proxy;
    }
    public NewContextOptions withStorageState(BrowserContext.StorageState storageState) {
      this.storageState = storageState;
      return this;
    }
  }
  class NewPageOptions {
    public class RecordHar {
      /**
       * Optional setting to control whether to omit request content from the HAR. Defaults to {@code false}.
       */
      public Boolean omitContent;
      /**
       * Path on the filesystem to write the HAR file to.
       */
      public Path path;

      RecordHar() {
      }
      public NewPageOptions done() {
        return NewPageOptions.this;
      }

      public RecordHar withOmitContent(Boolean omitContent) {
        this.omitContent = omitContent;
        return this;
      }
      public RecordHar withPath(Path path) {
        this.path = path;
        return this;
      }
    }
    public class RecordVideo {
      /**
       * Path to the directory to put videos into.
       */
      public Path dir;
      /**
       * Optional dimensions of the recorded videos. If not specified the size will be equal to {@code viewport}. If {@code viewport} is not configured explicitly the video size defaults to 1280x720. Actual picture of each page will be scaled down if necessary to fit the specified size.
       */
      public VideoSize size;

      RecordVideo() {
      }
      public NewPageOptions done() {
        return NewPageOptions.this;
      }

      public RecordVideo withDir(Path dir) {
        this.dir = dir;
        return this;
      }
      public RecordVideo withSize(int width, int height) {
        this.size = new VideoSize(width, height);
        return this;
      }
    }
    public class Proxy {
      /**
       * Proxy to be used for all requests. HTTP and SOCKS proxies are supported, for example {@code http://myproxy.com:3128} or {@code socks5://myproxy.com:3128}. Short form {@code myproxy.com:3128} is considered an HTTP proxy.
       */
      public String server;
      /**
       * Optional coma-separated domains to bypass proxy, for example {@code ".com, chromium.org, .domain.com"}.
       */
      public String bypass;
      /**
       * Optional username to use if HTTP proxy requires authentication.
       */
      public String username;
      /**
       * Optional password to use if HTTP proxy requires authentication.
       */
      public String password;

      Proxy() {
      }
      public NewPageOptions done() {
        return NewPageOptions.this;
      }

      public Proxy withServer(String server) {
        this.server = server;
        return this;
      }
      public Proxy withBypass(String bypass) {
        this.bypass = bypass;
        return this;
      }
      public Proxy withUsername(String username) {
        this.username = username;
        return this;
      }
      public Proxy withPassword(String password) {
        this.password = password;
        return this;
      }
    }
    /**
     * Whether to automatically download all the attachments. Defaults to {@code false} where all the downloads are canceled.
     */
    public Boolean acceptDownloads;
    /**
     * Whether to ignore HTTPS errors during navigation. Defaults to {@code false}.
     */
    public Boolean ignoreHTTPSErrors;
    /**
     * Toggles bypassing page's Content-Security-Policy.
     */
    public Boolean bypassCSP;
    /**
     * Sets a consistent viewport for each page. Defaults to an 1280x720 viewport. {@code null} disables the default viewport.
     */
    public Page.Viewport viewport;
    /**
     * Specific user agent to use in this context.
     */
    public String userAgent;
    /**
     * Specify device scale factor (can be thought of as dpr). Defaults to {@code 1}.
     */
    public Integer deviceScaleFactor;
    /**
     * Whether the {@code meta viewport} tag is taken into account and touch events are enabled. Defaults to {@code false}. Not supported in Firefox.
     */
    public Boolean isMobile;
    /**
     * Specifies if viewport supports touch events. Defaults to false.
     */
    public Boolean hasTouch;
    /**
     * Whether or not to enable JavaScript in the context. Defaults to {@code true}.
     */
    public Boolean javaScriptEnabled;
    /**
     * Changes the timezone of the context. See ICU’s {@code metaZones.txt} for a list of supported timezone IDs.
     */
    public String timezoneId;
    public Geolocation geolocation;
    /**
     * Specify user locale, for example {@code en-GB}, {@code de-DE}, etc. Locale will affect {@code navigator.language} value, {@code Accept-Language} request header value as well as number and date formatting rules.
     */
    public String locale;
    /**
     * A list of permissions to grant to all pages in this context. See browserContext.grantPermissions for more details.
     */
    public List<String> permissions;
    /**
     * An object containing additional HTTP headers to be sent with every request. All header values must be strings.
     */
    public Map<String, String> extraHTTPHeaders;
    /**
     * Whether to emulate network being offline. Defaults to {@code false}.
     */
    public Boolean offline;
    /**
     * Credentials for HTTP authentication.
     */
    public BrowserContext.HTTPCredentials httpCredentials;
    /**
     * Emulates {@code 'prefers-colors-scheme'} media feature, supported values are {@code 'light'}, {@code 'dark'}, {@code 'no-preference'}. See page.emulateMedia(options) for more details. Defaults to '{@code light}'.
     */
    public ColorScheme colorScheme;
    /**
     * Logger sink for Playwright logging.
     */
    public Logger logger;
    /**
     * Enables HAR recording for all pages into {@code recordHar.path} file. If not specified, the HAR is not recorded. Make sure to await {@code browserContext.close} for the HAR to be saved.
     */
    public RecordHar recordHar;
    /**
     * Enables video recording for all pages into {@code recordVideo.dir} directory. If not specified videos are not recorded. Make sure to await {@code browserContext.close} for videos to be saved.
     */
    public RecordVideo recordVideo;
    /**
     * Network proxy settings to use with this context. Note that browser needs to be launched with the global proxy for this option to work. If all contexts override the proxy, global proxy will be never used and can be any string, for example {@code launch({ proxy: { server: 'per-context' } })}.
     */
    public Proxy proxy;
    /**
     * Populates context with given storage state. This method can be used to initialize context with logged-in information obtained via browserContext.storageState().
     */
    public BrowserContext.StorageState storageState;

    public NewPageOptions withAcceptDownloads(Boolean acceptDownloads) {
      this.acceptDownloads = acceptDownloads;
      return this;
    }
    public NewPageOptions withIgnoreHTTPSErrors(Boolean ignoreHTTPSErrors) {
      this.ignoreHTTPSErrors = ignoreHTTPSErrors;
      return this;
    }
    public NewPageOptions withBypassCSP(Boolean bypassCSP) {
      this.bypassCSP = bypassCSP;
      return this;
    }
    public NewPageOptions withViewport(int width, int height) {
      this.viewport = new Page.Viewport(width, height);
      return this;
    }
    public NewPageOptions withUserAgent(String userAgent) {
      this.userAgent = userAgent;
      return this;
    }
    public NewPageOptions withDeviceScaleFactor(Integer deviceScaleFactor) {
      this.deviceScaleFactor = deviceScaleFactor;
      return this;
    }
    public NewPageOptions withIsMobile(Boolean isMobile) {
      this.isMobile = isMobile;
      return this;
    }
    public NewPageOptions withHasTouch(Boolean hasTouch) {
      this.hasTouch = hasTouch;
      return this;
    }
    public NewPageOptions withJavaScriptEnabled(Boolean javaScriptEnabled) {
      this.javaScriptEnabled = javaScriptEnabled;
      return this;
    }
    public NewPageOptions withTimezoneId(String timezoneId) {
      this.timezoneId = timezoneId;
      return this;
    }
    public NewPageOptions withGeolocation(Geolocation geolocation) {
      this.geolocation = geolocation;
      return this;
    }
    public NewPageOptions withLocale(String locale) {
      this.locale = locale;
      return this;
    }
    public NewPageOptions withPermissions(List<String> permissions) {
      this.permissions = permissions;
      return this;
    }
    public NewPageOptions withExtraHTTPHeaders(Map<String, String> extraHTTPHeaders) {
      this.extraHTTPHeaders = extraHTTPHeaders;
      return this;
    }
    public NewPageOptions withOffline(Boolean offline) {
      this.offline = offline;
      return this;
    }
    public NewPageOptions withHttpCredentials(String username, String password) {
      this.httpCredentials = new BrowserContext.HTTPCredentials(username, password);
      return this;
    }
    public NewPageOptions withColorScheme(ColorScheme colorScheme) {
      this.colorScheme = colorScheme;
      return this;
    }
    public NewPageOptions withLogger(Logger logger) {
      this.logger = logger;
      return this;
    }
    public RecordHar setRecordHar() {
      this.recordHar = new RecordHar();
      return this.recordHar;
    }
    public RecordVideo setRecordVideo() {
      this.recordVideo = new RecordVideo();
      return this.recordVideo;
    }
    public Proxy setProxy() {
      this.proxy = new Proxy();
      return this.proxy;
    }
    public NewPageOptions withStorageState(BrowserContext.StorageState storageState) {
      this.storageState = storageState;
      return this;
    }
  }
  /**
   * In case this browser is obtained using browserType.launch, closes the browser and all of its pages (if any were opened).
   * <p>
   * In case this browser is obtained using browserType.connect, clears all created contexts belonging to this browser and disconnects from the browser server.
   * <p>
   * The Browser object itself is considered to be disposed and cannot be used anymore.
   */
  void close();
  /**
   * Returns an array of all open browser contexts. In a newly created browser, this will return zero
   * <p>
   * browser contexts.
   * <p>
   */
  List<BrowserContext> contexts();
  /**
   * Indicates that the browser is connected.
   */
  boolean isConnected();
  default BrowserContext newContext() {
    return newContext(null);
  }
  /**
   * Creates a new browser context. It won't share cookies/cache with other browser contexts.
   * <p>
   */
  BrowserContext newContext(NewContextOptions options);
  default Page newPage() {
    return newPage(null);
  }
  /**
   * Creates a new page in a new browser context. Closing this page will close the context as well.
   * <p>
   * This is a convenience API that should only be used for the single-page scenarios and short snippets. Production code and testing frameworks should explicitly create browser.newContext followed by the browserContext.newPage to control their exact life times.
   */
  Page newPage(NewPageOptions options);
  /**
   * Returns the browser version.
   */
  String version();
}

