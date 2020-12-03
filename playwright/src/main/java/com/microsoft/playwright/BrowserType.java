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
 * BrowserType provides methods to launch a specific browser instance or connect to an existing one.
 * <p>
 * The following is a typical example of using Playwright to drive automation:
 * <p>
 */
public interface BrowserType {
  class ConnectOptions {
    /**
     * A browser websocket endpoint to connect to. **required**
     */
    public String wsEndpoint;
    /**
     * Slows down Playwright operations by the specified amount of milliseconds. Useful so that you can see what is going on. Defaults to 0.
     */
    public Integer slowMo;
    /**
     * Logger sink for Playwright logging.
     */
    public Logger logger;
    /**
     * Maximum time in milliseconds to wait for the connection to be established. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout.
     */
    public Integer timeout;

    public ConnectOptions withWsEndpoint(String wsEndpoint) {
      this.wsEndpoint = wsEndpoint;
      return this;
    }
    public ConnectOptions withSlowMo(Integer slowMo) {
      this.slowMo = slowMo;
      return this;
    }
    public ConnectOptions withLogger(Logger logger) {
      this.logger = logger;
      return this;
    }
    public ConnectOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class LaunchOptions {
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
      public LaunchOptions done() {
        return LaunchOptions.this;
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
     * Whether to run browser in headless mode. More details for Chromium and Firefox. Defaults to {@code true} unless the {@code devtools} option is {@code true}.
     */
    public Boolean headless;
    /**
     * Path to a browser executable to run instead of the bundled one. If {@code executablePath} is a relative path, then it is resolved relative to current working directory. Note that Playwright only works with the bundled Chromium, Firefox or WebKit, use at your own risk.
     */
    public String executablePath;
    /**
     * Additional arguments to pass to the browser instance. The list of Chromium flags can be found here.
     */
    public List<String> args;
    /**
     * If {@code true}, Playwright does not pass its own configurations args and only uses the ones from {@code args}. If an array is given, then filters out the given default arguments. Dangerous option; use with care. Defaults to {@code false}.
     */
    public Boolean ignoreDefaultArgs;
    /**
     * Network proxy settings.
     */
    public Proxy proxy;
    /**
     * If specified, accepted downloads are downloaded into this folder. Otherwise, temporary folder is created and is deleted when browser is closed.
     */
    public String downloadsPath;
    /**
     * Enable Chromium sandboxing. Defaults to {@code false}.
     */
    public Boolean chromiumSandbox;
    /**
     * Firefox user preferences. Learn more about the Firefox user preferences at {@code about:config}.
     */
    public String firefoxUserPrefs;
    /**
     * Close the browser process on Ctrl-C. Defaults to {@code true}.
     */
    public Boolean handleSIGINT;
    /**
     * Close the browser process on SIGTERM. Defaults to {@code true}.
     */
    public Boolean handleSIGTERM;
    /**
     * Close the browser process on SIGHUP. Defaults to {@code true}.
     */
    public Boolean handleSIGHUP;
    /**
     * Logger sink for Playwright logging.
     */
    public Logger logger;
    /**
     * Maximum time in milliseconds to wait for the browser instance to start. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout.
     */
    public Integer timeout;
    /**
     * Specify environment variables that will be visible to the browser. Defaults to {@code process.env}.
     */
    public String env;
    /**
     * **Chromium-only** Whether to auto-open a Developer Tools panel for each tab. If this option is {@code true}, the {@code headless} option will be set {@code false}.
     */
    public Boolean devtools;
    /**
     * Slows down Playwright operations by the specified amount of milliseconds. Useful so that you can see what is going on.
     */
    public Integer slowMo;

    public LaunchOptions withHeadless(Boolean headless) {
      this.headless = headless;
      return this;
    }
    public LaunchOptions withExecutablePath(String executablePath) {
      this.executablePath = executablePath;
      return this;
    }
    public LaunchOptions withArgs(List<String> args) {
      this.args = args;
      return this;
    }
    public LaunchOptions withIgnoreDefaultArgs(Boolean ignoreDefaultArgs) {
      this.ignoreDefaultArgs = ignoreDefaultArgs;
      return this;
    }
    public Proxy setProxy() {
      this.proxy = new Proxy();
      return this.proxy;
    }
    public LaunchOptions withDownloadsPath(String downloadsPath) {
      this.downloadsPath = downloadsPath;
      return this;
    }
    public LaunchOptions withChromiumSandbox(Boolean chromiumSandbox) {
      this.chromiumSandbox = chromiumSandbox;
      return this;
    }
    public LaunchOptions withFirefoxUserPrefs(String firefoxUserPrefs) {
      this.firefoxUserPrefs = firefoxUserPrefs;
      return this;
    }
    public LaunchOptions withHandleSIGINT(Boolean handleSIGINT) {
      this.handleSIGINT = handleSIGINT;
      return this;
    }
    public LaunchOptions withHandleSIGTERM(Boolean handleSIGTERM) {
      this.handleSIGTERM = handleSIGTERM;
      return this;
    }
    public LaunchOptions withHandleSIGHUP(Boolean handleSIGHUP) {
      this.handleSIGHUP = handleSIGHUP;
      return this;
    }
    public LaunchOptions withLogger(Logger logger) {
      this.logger = logger;
      return this;
    }
    public LaunchOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
    public LaunchOptions withEnv(String env) {
      this.env = env;
      return this;
    }
    public LaunchOptions withDevtools(Boolean devtools) {
      this.devtools = devtools;
      return this;
    }
    public LaunchOptions withSlowMo(Integer slowMo) {
      this.slowMo = slowMo;
      return this;
    }
  }
  class LaunchPersistentContextOptions {
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
      public LaunchPersistentContextOptions done() {
        return LaunchPersistentContextOptions.this;
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
    public class VideoSize {
      /**
       * Video frame width.
       */
      public int width;
      /**
       * Video frame height.
       */
      public int height;

      VideoSize() {
      }
      public LaunchPersistentContextOptions done() {
        return LaunchPersistentContextOptions.this;
      }

      public VideoSize withWidth(int width) {
        this.width = width;
        return this;
      }
      public VideoSize withHeight(int height) {
        this.height = height;
        return this;
      }
    }
    public class RecordHar {
      /**
       * Optional setting to control whether to omit request content from the HAR. Defaults to false.
       */
      public Boolean omitContent;
      /**
       * Path on the filesystem to write the HAR file to.
       */
      public Path path;

      RecordHar() {
      }
      public LaunchPersistentContextOptions done() {
        return LaunchPersistentContextOptions.this;
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
    /**
     * Whether to run browser in headless mode. More details for Chromium and Firefox. Defaults to {@code true} unless the {@code devtools} option is {@code true}.
     */
    public Boolean headless;
    /**
     * Path to a browser executable to run instead of the bundled one. If {@code executablePath} is a relative path, then it is resolved relative to current working directory. **BEWARE**: Playwright is only guaranteed to work with the bundled Chromium, Firefox or WebKit, use at your own risk.
     */
    public String executablePath;
    /**
     * Additional arguments to pass to the browser instance. The list of Chromium flags can be found here.
     */
    public List<String> args;
    /**
     * If {@code true}, then do not use any of the default arguments. If an array is given, then filter out the given default arguments. Dangerous option; use with care. Defaults to {@code false}.
     */
    public String ignoreDefaultArgs;
    /**
     * Network proxy settings.
     */
    public Proxy proxy;
    /**
     * Whether to automatically download all the attachments. Defaults to {@code false} where all the downloads are canceled.
     */
    public Boolean acceptDownloads;
    /**
     * If specified, accepted downloads are downloaded into this folder. Otherwise, temporary folder is created and is deleted when browser is closed.
     */
    public String downloadsPath;
    /**
     * Enable Chromium sandboxing. Defaults to {@code true}.
     */
    public Boolean chromiumSandbox;
    /**
     * Close the browser process on Ctrl-C. Defaults to {@code true}.
     */
    public Boolean handleSIGINT;
    /**
     * Close the browser process on SIGTERM. Defaults to {@code true}.
     */
    public Boolean handleSIGTERM;
    /**
     * Close the browser process on SIGHUP. Defaults to {@code true}.
     */
    public Boolean handleSIGHUP;
    /**
     * Logger sink for Playwright logging.
     */
    public Logger logger;
    /**
     * Maximum time in milliseconds to wait for the browser instance to start. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout.
     */
    public Integer timeout;
    /**
     * Specify environment variables that will be visible to the browser. Defaults to {@code process.env}.
     */
    public String env;
    /**
     * **Chromium-only** Whether to auto-open a Developer Tools panel for each tab. If this option is {@code true}, the {@code headless} option will be set {@code false}.
     */
    public Boolean devtools;
    /**
     * Slows down Playwright operations by the specified amount of milliseconds. Useful so that you can see what is going on. Defaults to 0.
     */
    public Integer slowMo;
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
     * Whether or not to enable JavaScript in the context. Defaults to true.
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
     * Enables video recording for all pages to {@code videosPath} folder. If not specified, videos are not recorded. Make sure to await {@code browserContext.close} for videos to be saved.
     */
    public String videosPath;
    /**
     * Specifies dimensions of the automatically recorded video. Can only be used if {@code videosPath} is set. If not specified the size will be equal to {@code viewport}. If {@code viewport} is not configured explicitly the video size defaults to 1280x720. Actual picture of the page will be scaled down if necessary to fit specified size.
     */
    public VideoSize videoSize;
    /**
     * Enables HAR recording for all the pages into {@code har.path} file. If not specified, HAR is not recorded. Make sure to await {@code page.close} for HAR to be saved.
     */
    public RecordHar recordHar;

    public LaunchPersistentContextOptions withHeadless(Boolean headless) {
      this.headless = headless;
      return this;
    }
    public LaunchPersistentContextOptions withExecutablePath(String executablePath) {
      this.executablePath = executablePath;
      return this;
    }
    public LaunchPersistentContextOptions withArgs(List<String> args) {
      this.args = args;
      return this;
    }
    public LaunchPersistentContextOptions withIgnoreDefaultArgs(String ignoreDefaultArgs) {
      this.ignoreDefaultArgs = ignoreDefaultArgs;
      return this;
    }
    public Proxy setProxy() {
      this.proxy = new Proxy();
      return this.proxy;
    }
    public LaunchPersistentContextOptions withAcceptDownloads(Boolean acceptDownloads) {
      this.acceptDownloads = acceptDownloads;
      return this;
    }
    public LaunchPersistentContextOptions withDownloadsPath(String downloadsPath) {
      this.downloadsPath = downloadsPath;
      return this;
    }
    public LaunchPersistentContextOptions withChromiumSandbox(Boolean chromiumSandbox) {
      this.chromiumSandbox = chromiumSandbox;
      return this;
    }
    public LaunchPersistentContextOptions withHandleSIGINT(Boolean handleSIGINT) {
      this.handleSIGINT = handleSIGINT;
      return this;
    }
    public LaunchPersistentContextOptions withHandleSIGTERM(Boolean handleSIGTERM) {
      this.handleSIGTERM = handleSIGTERM;
      return this;
    }
    public LaunchPersistentContextOptions withHandleSIGHUP(Boolean handleSIGHUP) {
      this.handleSIGHUP = handleSIGHUP;
      return this;
    }
    public LaunchPersistentContextOptions withLogger(Logger logger) {
      this.logger = logger;
      return this;
    }
    public LaunchPersistentContextOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
    public LaunchPersistentContextOptions withEnv(String env) {
      this.env = env;
      return this;
    }
    public LaunchPersistentContextOptions withDevtools(Boolean devtools) {
      this.devtools = devtools;
      return this;
    }
    public LaunchPersistentContextOptions withSlowMo(Integer slowMo) {
      this.slowMo = slowMo;
      return this;
    }
    public LaunchPersistentContextOptions withIgnoreHTTPSErrors(Boolean ignoreHTTPSErrors) {
      this.ignoreHTTPSErrors = ignoreHTTPSErrors;
      return this;
    }
    public LaunchPersistentContextOptions withBypassCSP(Boolean bypassCSP) {
      this.bypassCSP = bypassCSP;
      return this;
    }
    public LaunchPersistentContextOptions withViewport(int width, int height) {
      this.viewport = new Page.Viewport(width, height);
      return this;
    }
    public LaunchPersistentContextOptions withUserAgent(String userAgent) {
      this.userAgent = userAgent;
      return this;
    }
    public LaunchPersistentContextOptions withDeviceScaleFactor(Integer deviceScaleFactor) {
      this.deviceScaleFactor = deviceScaleFactor;
      return this;
    }
    public LaunchPersistentContextOptions withIsMobile(Boolean isMobile) {
      this.isMobile = isMobile;
      return this;
    }
    public LaunchPersistentContextOptions withHasTouch(Boolean hasTouch) {
      this.hasTouch = hasTouch;
      return this;
    }
    public LaunchPersistentContextOptions withJavaScriptEnabled(Boolean javaScriptEnabled) {
      this.javaScriptEnabled = javaScriptEnabled;
      return this;
    }
    public LaunchPersistentContextOptions withTimezoneId(String timezoneId) {
      this.timezoneId = timezoneId;
      return this;
    }
    public LaunchPersistentContextOptions withGeolocation(Geolocation geolocation) {
      this.geolocation = geolocation;
      return this;
    }
    public LaunchPersistentContextOptions withLocale(String locale) {
      this.locale = locale;
      return this;
    }
    public LaunchPersistentContextOptions withPermissions(List<String> permissions) {
      this.permissions = permissions;
      return this;
    }
    public LaunchPersistentContextOptions withExtraHTTPHeaders(Map<String, String> extraHTTPHeaders) {
      this.extraHTTPHeaders = extraHTTPHeaders;
      return this;
    }
    public LaunchPersistentContextOptions withOffline(Boolean offline) {
      this.offline = offline;
      return this;
    }
    public LaunchPersistentContextOptions withHttpCredentials(String username, String password) {
      this.httpCredentials = new BrowserContext.HTTPCredentials(username, password);
      return this;
    }
    public LaunchPersistentContextOptions withColorScheme(ColorScheme colorScheme) {
      this.colorScheme = colorScheme;
      return this;
    }
    public LaunchPersistentContextOptions withVideosPath(String videosPath) {
      this.videosPath = videosPath;
      return this;
    }
    public VideoSize setVideoSize() {
      this.videoSize = new VideoSize();
      return this.videoSize;
    }
    public RecordHar setRecordHar() {
      this.recordHar = new RecordHar();
      return this.recordHar;
    }
  }
  class LaunchServerOptions {
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
      public LaunchServerOptions done() {
        return LaunchServerOptions.this;
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
     * Whether to run browser in headless mode. More details for Chromium and Firefox. Defaults to {@code true} unless the {@code devtools} option is {@code true}.
     */
    public Boolean headless;
    /**
     * Port to use for the web socket. Defaults to 0 that picks any available port.
     */
    public Integer port;
    /**
     * Path to a browser executable to run instead of the bundled one. If {@code executablePath} is a relative path, then it is resolved relative to current working directory. **BEWARE**: Playwright is only guaranteed to work with the bundled Chromium, Firefox or WebKit, use at your own risk.
     */
    public String executablePath;
    /**
     * Additional arguments to pass to the browser instance. The list of Chromium flags can be found here.
     */
    public List<String> args;
    /**
     * If {@code true}, then do not use any of the default arguments. If an array is given, then filter out the given default arguments. Dangerous option; use with care. Defaults to {@code false}.
     */
    public String ignoreDefaultArgs;
    /**
     * Network proxy settings.
     */
    public Proxy proxy;
    /**
     * If specified, accepted downloads are downloaded into this folder. Otherwise, temporary folder is created and is deleted when browser is closed.
     */
    public String downloadsPath;
    /**
     * Enable Chromium sandboxing. Defaults to {@code true}.
     */
    public Boolean chromiumSandbox;
    /**
     * Firefox user preferences. Learn more about the Firefox user preferences at {@code about:config}.
     */
    public String firefoxUserPrefs;
    /**
     * Close the browser process on Ctrl-C. Defaults to {@code true}.
     */
    public Boolean handleSIGINT;
    /**
     * Close the browser process on SIGTERM. Defaults to {@code true}.
     */
    public Boolean handleSIGTERM;
    /**
     * Close the browser process on SIGHUP. Defaults to {@code true}.
     */
    public Boolean handleSIGHUP;
    /**
     * Logger sink for Playwright logging.
     */
    public Logger logger;
    /**
     * Maximum time in milliseconds to wait for the browser instance to start. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to disable timeout.
     */
    public Integer timeout;
    /**
     * Specify environment variables that will be visible to the browser. Defaults to {@code process.env}.
     */
    public String env;
    /**
     * **Chromium-only** Whether to auto-open a Developer Tools panel for each tab. If this option is {@code true}, the {@code headless} option will be set {@code false}.
     */
    public Boolean devtools;

    public LaunchServerOptions withHeadless(Boolean headless) {
      this.headless = headless;
      return this;
    }
    public LaunchServerOptions withPort(Integer port) {
      this.port = port;
      return this;
    }
    public LaunchServerOptions withExecutablePath(String executablePath) {
      this.executablePath = executablePath;
      return this;
    }
    public LaunchServerOptions withArgs(List<String> args) {
      this.args = args;
      return this;
    }
    public LaunchServerOptions withIgnoreDefaultArgs(String ignoreDefaultArgs) {
      this.ignoreDefaultArgs = ignoreDefaultArgs;
      return this;
    }
    public Proxy setProxy() {
      this.proxy = new Proxy();
      return this.proxy;
    }
    public LaunchServerOptions withDownloadsPath(String downloadsPath) {
      this.downloadsPath = downloadsPath;
      return this;
    }
    public LaunchServerOptions withChromiumSandbox(Boolean chromiumSandbox) {
      this.chromiumSandbox = chromiumSandbox;
      return this;
    }
    public LaunchServerOptions withFirefoxUserPrefs(String firefoxUserPrefs) {
      this.firefoxUserPrefs = firefoxUserPrefs;
      return this;
    }
    public LaunchServerOptions withHandleSIGINT(Boolean handleSIGINT) {
      this.handleSIGINT = handleSIGINT;
      return this;
    }
    public LaunchServerOptions withHandleSIGTERM(Boolean handleSIGTERM) {
      this.handleSIGTERM = handleSIGTERM;
      return this;
    }
    public LaunchServerOptions withHandleSIGHUP(Boolean handleSIGHUP) {
      this.handleSIGHUP = handleSIGHUP;
      return this;
    }
    public LaunchServerOptions withLogger(Logger logger) {
      this.logger = logger;
      return this;
    }
    public LaunchServerOptions withTimeout(Integer timeout) {
      this.timeout = timeout;
      return this;
    }
    public LaunchServerOptions withEnv(String env) {
      this.env = env;
      return this;
    }
    public LaunchServerOptions withDevtools(Boolean devtools) {
      this.devtools = devtools;
      return this;
    }
  }
  /**
   * 
   * @return A path where Playwright expects to find a bundled browser executable.
   */
  String executablePath();
  default Browser launch() {
    return launch(null);
  }
  /**
   * You can use {@code ignoreDefaultArgs} to filter out {@code --mute-audio} from default arguments:
   * <p>
   * 
   * <p>
   * **Chromium-only** Playwright can also be used to control the Chrome browser, but it works best with the version of Chromium it is bundled with. There is no guarantee it will work with any other version. Use {@code executablePath} option with extreme caution.
   * <p>
   * If Google Chrome (rather than Chromium) is preferred, a Chrome Canary or Dev Channel build is suggested.
   * <p>
   * In browserType.launch([options]) above, any mention of Chromium also applies to Chrome.
   * <p>
   * See {@code this article} for a description of the differences between Chromium and Chrome. {@code This article} describes some differences for Linux users.
   * @param options Set of configurable options to set on the browser. Can have the following fields:
   * @return Promise which resolves to browser instance.
   */
  Browser launch(LaunchOptions options);
  default BrowserContext launchPersistentContext(Path userDataDir) {
    return launchPersistentContext(userDataDir, null);
  }
  /**
   * Launches browser that uses persistent storage located at {@code userDataDir} and returns the only context. Closing this context will automatically close the browser.
   * @param userDataDir Path to a User Data Directory, which stores browser session data like cookies and local storage. More details for Chromium and Firefox.
   * @param options Set of configurable options to set on the browser. Can have the following fields:
   * @return Promise that resolves to the persistent browser context instance.
   */
  BrowserContext launchPersistentContext(Path userDataDir, LaunchPersistentContextOptions options);
  /**
   * Returns browser name. For example: {@code 'chromium'}, {@code 'webkit'} or {@code 'firefox'}.
   */
  String name();
}

