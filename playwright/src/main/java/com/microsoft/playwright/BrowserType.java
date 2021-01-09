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
 * BrowserType provides methods to launch a specific browser instance or connect to an existing one. The following is a
 * <p>
 * typical example of using Playwright to drive automation:
 * <p>
 * 
 * <p>
 */
public interface BrowserType {
  class LaunchOptions {
    public class Proxy {
      /**
       * Proxy to be used for all requests. HTTP and SOCKS proxies are supported, for example {@code http://myproxy.com:3128} or
       * {@code socks5://myproxy.com:3128}. Short form {@code myproxy.com:3128} is considered an HTTP proxy.
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
     * Additional arguments to pass to the browser instance. The list of Chromium flags can be found
     * [here](http://peter.sh/experiments/chromium-command-line-switches/).
     */
    public List<String> args;
    /**
     * Enable Chromium sandboxing. Defaults to {@code false}.
     */
    public Boolean chromiumSandbox;
    /**
     * **Chromium-only** Whether to auto-open a Developer Tools panel for each tab. If this option is {@code true}, the {@code headless}
     * option will be set {@code false}.
     */
    public Boolean devtools;
    /**
     * If specified, accepted downloads are downloaded into this directory. Otherwise, temporary directory is created and is
     * deleted when browser is closed.
     */
    public Path downloadsPath;
    /**
     * Specify environment variables that will be visible to the browser. Defaults to {@code process.env}.
     */
    public Map<String, String> env;
    /**
     * Path to a browser executable to run instead of the bundled one. If {@code executablePath} is a relative path, then it is
     * resolved relative to the current working directory. Note that Playwright only works with the bundled Chromium, Firefox
     * or WebKit, use at your own risk.
     */
    public Path executablePath;
    /**
     * Firefox user preferences. Learn more about the Firefox user preferences at
     * [{@code about:config}](https://support.mozilla.org/en-US/kb/about-config-editor-firefox).
     */
    public Map<String, Object> firefoxUserPrefs;
    /**
     * Close the browser process on SIGHUP. Defaults to {@code true}.
     */
    public Boolean handleSIGHUP;
    /**
     * Close the browser process on Ctrl-C. Defaults to {@code true}.
     */
    public Boolean handleSIGINT;
    /**
     * Close the browser process on SIGTERM. Defaults to {@code true}.
     */
    public Boolean handleSIGTERM;
    /**
     * Whether to run browser in headless mode. More details for
     * [Chromium](https://developers.google.com/web/updates/2017/04/headless-chrome) and
     * [Firefox](https://developer.mozilla.org/en-US/docs/Mozilla/Firefox/Headless_mode). Defaults to {@code true} unless the
     * {@code devtools} option is {@code true}.
     */
    public Boolean headless;
    /**
     * If {@code true}, Playwright does not pass its own configurations args and only uses the ones from {@code args}. If an array is
     * given, then filters out the given default arguments. Dangerous option; use with care. Defaults to {@code false}.
     */
    public List<String> ignoreDefaultArgs;
    public Boolean ignoreAllDefaultArgs;
    /**
     * Network proxy settings.
     */
    public Proxy proxy;
    /**
     * Slows down Playwright operations by the specified amount of milliseconds. Useful so that you can see what is going on.
     */
    public Double slowMo;
    /**
     * Maximum time in milliseconds to wait for the browser instance to start. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to
     * disable timeout.
     */
    public Double timeout;

    public LaunchOptions withArgs(List<String> args) {
      this.args = args;
      return this;
    }
    public LaunchOptions withChromiumSandbox(boolean chromiumSandbox) {
      this.chromiumSandbox = chromiumSandbox;
      return this;
    }
    public LaunchOptions withDevtools(boolean devtools) {
      this.devtools = devtools;
      return this;
    }
    public LaunchOptions withDownloadsPath(Path downloadsPath) {
      this.downloadsPath = downloadsPath;
      return this;
    }
    public LaunchOptions withEnv(Map<String, String> env) {
      this.env = env;
      return this;
    }
    public LaunchOptions withExecutablePath(Path executablePath) {
      this.executablePath = executablePath;
      return this;
    }
    public LaunchOptions withFirefoxUserPrefs(Map<String, Object> firefoxUserPrefs) {
      this.firefoxUserPrefs = firefoxUserPrefs;
      return this;
    }
    public LaunchOptions withHandleSIGHUP(boolean handleSIGHUP) {
      this.handleSIGHUP = handleSIGHUP;
      return this;
    }
    public LaunchOptions withHandleSIGINT(boolean handleSIGINT) {
      this.handleSIGINT = handleSIGINT;
      return this;
    }
    public LaunchOptions withHandleSIGTERM(boolean handleSIGTERM) {
      this.handleSIGTERM = handleSIGTERM;
      return this;
    }
    public LaunchOptions withHeadless(boolean headless) {
      this.headless = headless;
      return this;
    }
    public LaunchOptions withIgnoreDefaultArgs(List<String> argumentNames) {
      this.ignoreDefaultArgs = argumentNames;
      return this;
    }
    public LaunchOptions withIgnoreAllDefaultArgs(boolean ignore) {
      this.ignoreAllDefaultArgs = ignore;
      return this;
    }
    public Proxy setProxy() {
      this.proxy = new Proxy();
      return this.proxy;
    }
    public LaunchOptions withSlowMo(double slowMo) {
      this.slowMo = slowMo;
      return this;
    }
    public LaunchOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class LaunchPersistentContextOptions {
    public class Proxy {
      /**
       * Proxy to be used for all requests. HTTP and SOCKS proxies are supported, for example {@code http://myproxy.com:3128} or
       * {@code socks5://myproxy.com:3128}. Short form {@code myproxy.com:3128} is considered an HTTP proxy.
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
      public LaunchPersistentContextOptions done() {
        return LaunchPersistentContextOptions.this;
      }

      public RecordHar withOmitContent(boolean omitContent) {
        this.omitContent = omitContent;
        return this;
      }
      public RecordHar withPath(Path path) {
        this.path = path;
        return this;
      }
    }
    public class RecordVideo {
      public class Size {
        /**
         * Video frame width.
         */
        public int width;
        /**
         * Video frame height.
         */
        public int height;

        Size() {
        }
        public RecordVideo done() {
          return RecordVideo.this;
        }

        public Size withWidth(int width) {
          this.width = width;
          return this;
        }
        public Size withHeight(int height) {
          this.height = height;
          return this;
        }
      }
      /**
       * Path to the directory to put videos into.
       */
      public Path dir;
      /**
       * Optional dimensions of the recorded videos. If not specified the size will be equal to {@code viewport}. If {@code viewport} is not
       * configured explicitly the video size defaults to 1280x720. Actual picture of each page will be scaled down if necessary
       * to fit the specified size.
       */
      public Size size;

      RecordVideo() {
      }
      public LaunchPersistentContextOptions done() {
        return LaunchPersistentContextOptions.this;
      }

      public RecordVideo withDir(Path dir) {
        this.dir = dir;
        return this;
      }
      public Size setSize() {
        this.size = new Size();
        return this.size;
      }
    }
    /**
     * Whether to automatically download all the attachments. Defaults to {@code false} where all the downloads are canceled.
     */
    public Boolean acceptDownloads;
    /**
     * Additional arguments to pass to the browser instance. The list of Chromium flags can be found
     * [here](http://peter.sh/experiments/chromium-command-line-switches/).
     */
    public List<String> args;
    /**
     * Toggles bypassing page's Content-Security-Policy.
     */
    public Boolean bypassCSP;
    /**
     * Enable Chromium sandboxing. Defaults to {@code true}.
     */
    public Boolean chromiumSandbox;
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
     * **Chromium-only** Whether to auto-open a Developer Tools panel for each tab. If this option is {@code true}, the {@code headless}
     * option will be set {@code false}.
     */
    public Boolean devtools;
    /**
     * If specified, accepted downloads are downloaded into this directory. Otherwise, temporary directory is created and is
     * deleted when browser is closed.
     */
    public Path downloadsPath;
    /**
     * Specify environment variables that will be visible to the browser. Defaults to {@code process.env}.
     */
    public Map<String, String> env;
    /**
     * Path to a browser executable to run instead of the bundled one. If {@code executablePath} is a relative path, then it is
     * resolved relative to the current working directory. **BEWARE**: Playwright is only guaranteed to work with the bundled
     * Chromium, Firefox or WebKit, use at your own risk.
     */
    public Path executablePath;
    /**
     * An object containing additional HTTP headers to be sent with every request. All header values must be strings.
     */
    public Map<String, String> extraHTTPHeaders;
    public Geolocation geolocation;
    /**
     * Close the browser process on SIGHUP. Defaults to {@code true}.
     */
    public Boolean handleSIGHUP;
    /**
     * Close the browser process on Ctrl-C. Defaults to {@code true}.
     */
    public Boolean handleSIGINT;
    /**
     * Close the browser process on SIGTERM. Defaults to {@code true}.
     */
    public Boolean handleSIGTERM;
    /**
     * Specifies if viewport supports touch events. Defaults to false.
     */
    public Boolean hasTouch;
    /**
     * Whether to run browser in headless mode. More details for
     * [Chromium](https://developers.google.com/web/updates/2017/04/headless-chrome) and
     * [Firefox](https://developer.mozilla.org/en-US/docs/Mozilla/Firefox/Headless_mode). Defaults to {@code true} unless the
     * {@code devtools} option is {@code true}.
     */
    public Boolean headless;
    /**
     * Credentials for [HTTP authentication](https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication).
     */
    public BrowserContext.HTTPCredentials httpCredentials;
    /**
     * If {@code true}, then do not use any of the default arguments. If an array is given, then filter out the given default
     * arguments. Dangerous option; use with care. Defaults to {@code false}.
     */
    public List<String> ignoreDefaultArgs;
    public Boolean ignoreAllDefaultArgs;
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
     * Network proxy settings.
     */
    public Proxy proxy;
    /**
     * Enables [HAR](http://www.softwareishard.com/blog/har-12-spec) recording for all pages into {@code recordHar.path} file. If not
     * specified, the HAR is not recorded. Make sure to await [{@code method: BrowserContext.close}] for the HAR to be saved.
     */
    public RecordHar recordHar;
    /**
     * Enables video recording for all pages into {@code recordVideo.dir} directory. If not specified videos are not recorded. Make
     * sure to await [{@code method: BrowserContext.close}] for videos to be saved.
     */
    public RecordVideo recordVideo;
    /**
     * Slows down Playwright operations by the specified amount of milliseconds. Useful so that you can see what is going on.
     * Defaults to 0.
     */
    public Double slowMo;
    /**
     * Maximum time in milliseconds to wait for the browser instance to start. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to
     * disable timeout.
     */
    public Double timeout;
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
    public Page.Viewport viewport;

    public LaunchPersistentContextOptions withAcceptDownloads(boolean acceptDownloads) {
      this.acceptDownloads = acceptDownloads;
      return this;
    }
    public LaunchPersistentContextOptions withArgs(List<String> args) {
      this.args = args;
      return this;
    }
    public LaunchPersistentContextOptions withBypassCSP(boolean bypassCSP) {
      this.bypassCSP = bypassCSP;
      return this;
    }
    public LaunchPersistentContextOptions withChromiumSandbox(boolean chromiumSandbox) {
      this.chromiumSandbox = chromiumSandbox;
      return this;
    }
    public LaunchPersistentContextOptions withColorScheme(ColorScheme colorScheme) {
      this.colorScheme = colorScheme;
      return this;
    }
    public LaunchPersistentContextOptions withDeviceScaleFactor(double deviceScaleFactor) {
      this.deviceScaleFactor = deviceScaleFactor;
      return this;
    }
    public LaunchPersistentContextOptions withDevtools(boolean devtools) {
      this.devtools = devtools;
      return this;
    }
    public LaunchPersistentContextOptions withDownloadsPath(Path downloadsPath) {
      this.downloadsPath = downloadsPath;
      return this;
    }
    public LaunchPersistentContextOptions withEnv(Map<String, String> env) {
      this.env = env;
      return this;
    }
    public LaunchPersistentContextOptions withExecutablePath(Path executablePath) {
      this.executablePath = executablePath;
      return this;
    }
    public LaunchPersistentContextOptions withExtraHTTPHeaders(Map<String, String> extraHTTPHeaders) {
      this.extraHTTPHeaders = extraHTTPHeaders;
      return this;
    }
    public LaunchPersistentContextOptions withGeolocation(Geolocation geolocation) {
      this.geolocation = geolocation;
      return this;
    }
    public LaunchPersistentContextOptions withHandleSIGHUP(boolean handleSIGHUP) {
      this.handleSIGHUP = handleSIGHUP;
      return this;
    }
    public LaunchPersistentContextOptions withHandleSIGINT(boolean handleSIGINT) {
      this.handleSIGINT = handleSIGINT;
      return this;
    }
    public LaunchPersistentContextOptions withHandleSIGTERM(boolean handleSIGTERM) {
      this.handleSIGTERM = handleSIGTERM;
      return this;
    }
    public LaunchPersistentContextOptions withHasTouch(boolean hasTouch) {
      this.hasTouch = hasTouch;
      return this;
    }
    public LaunchPersistentContextOptions withHeadless(boolean headless) {
      this.headless = headless;
      return this;
    }
    public LaunchPersistentContextOptions withHttpCredentials(String username, String password) {
      this.httpCredentials = new BrowserContext.HTTPCredentials(username, password);
      return this;
    }
    public LaunchPersistentContextOptions withIgnoreDefaultArgs(List<String> argumentNames) {
      this.ignoreDefaultArgs = argumentNames;
      return this;
    }
    public LaunchPersistentContextOptions withIgnoreAllDefaultArgs(boolean ignore) {
      this.ignoreAllDefaultArgs = ignore;
      return this;
    }
    public LaunchPersistentContextOptions withIgnoreHTTPSErrors(boolean ignoreHTTPSErrors) {
      this.ignoreHTTPSErrors = ignoreHTTPSErrors;
      return this;
    }
    public LaunchPersistentContextOptions withIsMobile(boolean isMobile) {
      this.isMobile = isMobile;
      return this;
    }
    public LaunchPersistentContextOptions withJavaScriptEnabled(boolean javaScriptEnabled) {
      this.javaScriptEnabled = javaScriptEnabled;
      return this;
    }
    public LaunchPersistentContextOptions withLocale(String locale) {
      this.locale = locale;
      return this;
    }
    public LaunchPersistentContextOptions withOffline(boolean offline) {
      this.offline = offline;
      return this;
    }
    public LaunchPersistentContextOptions withPermissions(List<String> permissions) {
      this.permissions = permissions;
      return this;
    }
    public Proxy setProxy() {
      this.proxy = new Proxy();
      return this.proxy;
    }
    public RecordHar setRecordHar() {
      this.recordHar = new RecordHar();
      return this.recordHar;
    }
    public RecordVideo setRecordVideo() {
      this.recordVideo = new RecordVideo();
      return this.recordVideo;
    }
    public LaunchPersistentContextOptions withSlowMo(double slowMo) {
      this.slowMo = slowMo;
      return this;
    }
    public LaunchPersistentContextOptions withTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    public LaunchPersistentContextOptions withTimezoneId(String timezoneId) {
      this.timezoneId = timezoneId;
      return this;
    }
    public LaunchPersistentContextOptions withUserAgent(String userAgent) {
      this.userAgent = userAgent;
      return this;
    }
    public LaunchPersistentContextOptions withViewport(int width, int height) {
      this.viewport = new Page.Viewport(width, height);
      return this;
    }
  }
  /**
   * A path where Playwright expects to find a bundled browser executable.
   */
  String executablePath();
  default Browser launch() {
    return launch(null);
  }
  /**
   * Returns the browser instance.
   * <p>
   * You can use {@code ignoreDefaultArgs} to filter out {@code --mute-audio} from default arguments:
   * <p>
   * 
   * <p>
   * > **Chromium-only** Playwright can also be used to control the Chrome browser, but it works best with the version of
   * <p>
   * Chromium it is bundled with. There is no guarantee it will work with any other version. Use {@code executablePath} option with
   * <p>
   * extreme caution.
   * <p>
   * >
   * <p>
   * > If Google Chrome (rather than Chromium) is preferred, a
   * <p>
   * [Chrome Canary](https://www.google.com/chrome/browser/canary.html) or
   * <p>
   * [Dev Channel](https://www.chromium.org/getting-involved/dev-channel) build is suggested.
   * <p>
   * >
   * <p>
   * > In [{@code method: BrowserType.launch}] above, any mention of Chromium also applies to Chrome.
   * <p>
   * >
   * <p>
   * > See [{@code this article}](https://www.howtogeek.com/202825/what%E2%80%99s-the-difference-between-chromium-and-chrome/) for
   * <p>
   * a description of the differences between Chromium and Chrome.
   * <p>
   * [{@code This article}](https://chromium.googlesource.com/chromium/src/+/lkgr/docs/chromium_browser_vs_google_chrome.md)
   * <p>
   * describes some differences for Linux users.
   */
  Browser launch(LaunchOptions options);
  default BrowserContext launchPersistentContext(Path userDataDir) {
    return launchPersistentContext(userDataDir, null);
  }
  /**
   * Returns the persistent browser context instance.
   * <p>
   * Launches browser that uses persistent storage located at {@code userDataDir} and returns the only context. Closing this
   * <p>
   * context will automatically close the browser.
   * @param userDataDir Path to a User Data Directory, which stores browser session data like cookies and local storage. More details for
   * [Chromium](https://chromium.googlesource.com/chromium/src/+/master/docs/user_data_dir.md) and
   * [Firefox](https://developer.mozilla.org/en-US/docs/Mozilla/Command_Line_Options#User_Profile).
   */
  BrowserContext launchPersistentContext(Path userDataDir, LaunchPersistentContextOptions options);
  /**
   * Returns browser name. For example: {@code 'chromium'}, {@code 'webkit'} or {@code 'firefox'}.
   */
  String name();
}

