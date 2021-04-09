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

/**
 * BrowserType provides methods to launch a specific browser instance or connect to an existing one. The following is a
 * typical example of using Playwright to drive automation:
 * <pre>{@code
 * import com.microsoft.playwright.*;
 *
 * public class Example {
 *   public static void main(String[] args) {
 *     try (Playwright playwright = Playwright.create()) {
 *       BrowserType chromium = playwright.chromium();
 *       Browser browser = chromium.launch();
 *       Page page = browser.newPage();
 *       page.navigate("https://example.com");
 *       // other actions...
 *       browser.close();
 *     }
 *   }
 * }
 * }</pre>
 */
public interface BrowserType {
  class ConnectOptions {
    /**
     * Slows down Playwright operations by the specified amount of milliseconds. Useful so that you can see what is going on.
     * Defaults to 0.
     */
    public Double slowMo;
    /**
     * Maximum time in milliseconds to wait for the connection to be established. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to
     * disable timeout.
     */
    public Double timeout;

    public ConnectOptions setSlowMo(double slowMo) {
      this.slowMo = slowMo;
      return this;
    }
    public ConnectOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class ConnectOverCDPOptions {
    /**
     * Slows down Playwright operations by the specified amount of milliseconds. Useful so that you can see what is going on.
     * Defaults to 0.
     */
    public Double slowMo;
    /**
     * Maximum time in milliseconds to wait for the connection to be established. Defaults to {@code 30000} (30 seconds). Pass {@code 0} to
     * disable timeout.
     */
    public Double timeout;

    public ConnectOverCDPOptions setSlowMo(double slowMo) {
      this.slowMo = slowMo;
      return this;
    }
    public ConnectOverCDPOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class LaunchOptions {
    /**
     * Additional arguments to pass to the browser instance. The list of Chromium flags can be found <a
     * href="http://peter.sh/experiments/chromium-command-line-switches/">here</a>.
     */
    public List<String> args;
    /**
     * Browser distribution channel. Read more about using <a
     * href="https://playwright.dev/java/docs/browsers/#google-chrome--microsoft-edge">Google Chrome and Microsoft Edge</a>.
     */
    public BrowserChannel channel;
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
     * Firefox user preferences. Learn more about the Firefox user preferences at <a
     * href="https://support.mozilla.org/en-US/kb/about-config-editor-firefox">{@code about:config}</a>.
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
     * Whether to run browser in headless mode. More details for <a
     * href="https://developers.google.com/web/updates/2017/04/headless-chrome">Chromium</a> and <a
     * href="https://developer.mozilla.org/en-US/docs/Mozilla/Firefox/Headless_mode">Firefox</a>. Defaults to {@code true} unless the
     * {@code devtools} option is {@code true}.
     */
    public Boolean headless;
    /**
     * If {@code true}, Playwright does not pass its own configurations args and only uses the ones from {@code args}. Dangerous option;
     * use with care. Defaults to {@code false}.
     */
    public Boolean ignoreAllDefaultArgs;
    /**
     * If {@code true}, Playwright does not pass its own configurations args and only uses the ones from {@code args}. Dangerous option;
     * use with care.
     */
    public List<String> ignoreDefaultArgs;
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

    public LaunchOptions setArgs(List<String> args) {
      this.args = args;
      return this;
    }
    public LaunchOptions setChannel(BrowserChannel channel) {
      this.channel = channel;
      return this;
    }
    public LaunchOptions setChromiumSandbox(boolean chromiumSandbox) {
      this.chromiumSandbox = chromiumSandbox;
      return this;
    }
    public LaunchOptions setDevtools(boolean devtools) {
      this.devtools = devtools;
      return this;
    }
    public LaunchOptions setDownloadsPath(Path downloadsPath) {
      this.downloadsPath = downloadsPath;
      return this;
    }
    public LaunchOptions setEnv(Map<String, String> env) {
      this.env = env;
      return this;
    }
    public LaunchOptions setExecutablePath(Path executablePath) {
      this.executablePath = executablePath;
      return this;
    }
    public LaunchOptions setFirefoxUserPrefs(Map<String, Object> firefoxUserPrefs) {
      this.firefoxUserPrefs = firefoxUserPrefs;
      return this;
    }
    public LaunchOptions setHandleSIGHUP(boolean handleSIGHUP) {
      this.handleSIGHUP = handleSIGHUP;
      return this;
    }
    public LaunchOptions setHandleSIGINT(boolean handleSIGINT) {
      this.handleSIGINT = handleSIGINT;
      return this;
    }
    public LaunchOptions setHandleSIGTERM(boolean handleSIGTERM) {
      this.handleSIGTERM = handleSIGTERM;
      return this;
    }
    public LaunchOptions setHeadless(boolean headless) {
      this.headless = headless;
      return this;
    }
    public LaunchOptions setIgnoreAllDefaultArgs(boolean ignoreAllDefaultArgs) {
      this.ignoreAllDefaultArgs = ignoreAllDefaultArgs;
      return this;
    }
    public LaunchOptions setIgnoreDefaultArgs(List<String> ignoreDefaultArgs) {
      this.ignoreDefaultArgs = ignoreDefaultArgs;
      return this;
    }
    public LaunchOptions setProxy(String server) {
      return setProxy(new Proxy(server));
    }
    public LaunchOptions setProxy(Proxy proxy) {
      this.proxy = proxy;
      return this;
    }
    public LaunchOptions setSlowMo(double slowMo) {
      this.slowMo = slowMo;
      return this;
    }
    public LaunchOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class LaunchPersistentContextOptions {
    /**
     * Whether to automatically download all the attachments. Defaults to {@code false} where all the downloads are canceled.
     */
    public Boolean acceptDownloads;
    /**
     * Additional arguments to pass to the browser instance. The list of Chromium flags can be found <a
     * href="http://peter.sh/experiments/chromium-command-line-switches/">here</a>.
     */
    public List<String> args;
    /**
     * Toggles bypassing page's Content-Security-Policy.
     */
    public Boolean bypassCSP;
    /**
     * Browser distribution channel.
     */
    public BrowserChannel channel;
    /**
     * Enable Chromium sandboxing. Defaults to {@code true}.
     */
    public Boolean chromiumSandbox;
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
     * Whether to run browser in headless mode. More details for <a
     * href="https://developers.google.com/web/updates/2017/04/headless-chrome">Chromium</a> and <a
     * href="https://developer.mozilla.org/en-US/docs/Mozilla/Firefox/Headless_mode">Firefox</a>. Defaults to {@code true} unless the
     * {@code devtools} option is {@code true}.
     */
    public Boolean headless;
    /**
     * Credentials for <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication">HTTP authentication</a>.
     */
    public HttpCredentials httpCredentials;
    /**
     * If {@code true}, Playwright does not pass its own configurations args and only uses the ones from {@code args}. Dangerous option;
     * use with care. Defaults to {@code false}.
     */
    public Boolean ignoreAllDefaultArgs;
    /**
     * If {@code true}, Playwright does not pass its own configurations args and only uses the ones from {@code args}. Dangerous option;
     * use with care.
     */
    public List<String> ignoreDefaultArgs;
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
     * Network proxy settings.
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

    public LaunchPersistentContextOptions setAcceptDownloads(boolean acceptDownloads) {
      this.acceptDownloads = acceptDownloads;
      return this;
    }
    public LaunchPersistentContextOptions setArgs(List<String> args) {
      this.args = args;
      return this;
    }
    public LaunchPersistentContextOptions setBypassCSP(boolean bypassCSP) {
      this.bypassCSP = bypassCSP;
      return this;
    }
    public LaunchPersistentContextOptions setChannel(BrowserChannel channel) {
      this.channel = channel;
      return this;
    }
    public LaunchPersistentContextOptions setChromiumSandbox(boolean chromiumSandbox) {
      this.chromiumSandbox = chromiumSandbox;
      return this;
    }
    public LaunchPersistentContextOptions setColorScheme(ColorScheme colorScheme) {
      this.colorScheme = colorScheme;
      return this;
    }
    public LaunchPersistentContextOptions setDeviceScaleFactor(double deviceScaleFactor) {
      this.deviceScaleFactor = deviceScaleFactor;
      return this;
    }
    public LaunchPersistentContextOptions setDevtools(boolean devtools) {
      this.devtools = devtools;
      return this;
    }
    public LaunchPersistentContextOptions setDownloadsPath(Path downloadsPath) {
      this.downloadsPath = downloadsPath;
      return this;
    }
    public LaunchPersistentContextOptions setEnv(Map<String, String> env) {
      this.env = env;
      return this;
    }
    public LaunchPersistentContextOptions setExecutablePath(Path executablePath) {
      this.executablePath = executablePath;
      return this;
    }
    public LaunchPersistentContextOptions setExtraHTTPHeaders(Map<String, String> extraHTTPHeaders) {
      this.extraHTTPHeaders = extraHTTPHeaders;
      return this;
    }
    public LaunchPersistentContextOptions setGeolocation(double latitude, double longitude) {
      return setGeolocation(new Geolocation(latitude, longitude));
    }
    public LaunchPersistentContextOptions setGeolocation(Geolocation geolocation) {
      this.geolocation = geolocation;
      return this;
    }
    public LaunchPersistentContextOptions setHandleSIGHUP(boolean handleSIGHUP) {
      this.handleSIGHUP = handleSIGHUP;
      return this;
    }
    public LaunchPersistentContextOptions setHandleSIGINT(boolean handleSIGINT) {
      this.handleSIGINT = handleSIGINT;
      return this;
    }
    public LaunchPersistentContextOptions setHandleSIGTERM(boolean handleSIGTERM) {
      this.handleSIGTERM = handleSIGTERM;
      return this;
    }
    public LaunchPersistentContextOptions setHasTouch(boolean hasTouch) {
      this.hasTouch = hasTouch;
      return this;
    }
    public LaunchPersistentContextOptions setHeadless(boolean headless) {
      this.headless = headless;
      return this;
    }
    public LaunchPersistentContextOptions setHttpCredentials(String username, String password) {
      return setHttpCredentials(new HttpCredentials(username, password));
    }
    public LaunchPersistentContextOptions setHttpCredentials(HttpCredentials httpCredentials) {
      this.httpCredentials = httpCredentials;
      return this;
    }
    public LaunchPersistentContextOptions setIgnoreAllDefaultArgs(boolean ignoreAllDefaultArgs) {
      this.ignoreAllDefaultArgs = ignoreAllDefaultArgs;
      return this;
    }
    public LaunchPersistentContextOptions setIgnoreDefaultArgs(List<String> ignoreDefaultArgs) {
      this.ignoreDefaultArgs = ignoreDefaultArgs;
      return this;
    }
    public LaunchPersistentContextOptions setIgnoreHTTPSErrors(boolean ignoreHTTPSErrors) {
      this.ignoreHTTPSErrors = ignoreHTTPSErrors;
      return this;
    }
    public LaunchPersistentContextOptions setIsMobile(boolean isMobile) {
      this.isMobile = isMobile;
      return this;
    }
    public LaunchPersistentContextOptions setJavaScriptEnabled(boolean javaScriptEnabled) {
      this.javaScriptEnabled = javaScriptEnabled;
      return this;
    }
    public LaunchPersistentContextOptions setLocale(String locale) {
      this.locale = locale;
      return this;
    }
    public LaunchPersistentContextOptions setOffline(boolean offline) {
      this.offline = offline;
      return this;
    }
    public LaunchPersistentContextOptions setPermissions(List<String> permissions) {
      this.permissions = permissions;
      return this;
    }
    public LaunchPersistentContextOptions setProxy(String server) {
      return setProxy(new Proxy(server));
    }
    public LaunchPersistentContextOptions setProxy(Proxy proxy) {
      this.proxy = proxy;
      return this;
    }
    public LaunchPersistentContextOptions setRecordHarOmitContent(boolean recordHarOmitContent) {
      this.recordHarOmitContent = recordHarOmitContent;
      return this;
    }
    public LaunchPersistentContextOptions setRecordHarPath(Path recordHarPath) {
      this.recordHarPath = recordHarPath;
      return this;
    }
    public LaunchPersistentContextOptions setRecordVideoDir(Path recordVideoDir) {
      this.recordVideoDir = recordVideoDir;
      return this;
    }
    public LaunchPersistentContextOptions setRecordVideoSize(int width, int height) {
      return setRecordVideoSize(new RecordVideoSize(width, height));
    }
    public LaunchPersistentContextOptions setRecordVideoSize(RecordVideoSize recordVideoSize) {
      this.recordVideoSize = recordVideoSize;
      return this;
    }
    public LaunchPersistentContextOptions setScreenSize(int width, int height) {
      return setScreenSize(new ScreenSize(width, height));
    }
    public LaunchPersistentContextOptions setScreenSize(ScreenSize screenSize) {
      this.screenSize = screenSize;
      return this;
    }
    public LaunchPersistentContextOptions setSlowMo(double slowMo) {
      this.slowMo = slowMo;
      return this;
    }
    public LaunchPersistentContextOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    public LaunchPersistentContextOptions setTimezoneId(String timezoneId) {
      this.timezoneId = timezoneId;
      return this;
    }
    public LaunchPersistentContextOptions setUserAgent(String userAgent) {
      this.userAgent = userAgent;
      return this;
    }
    public LaunchPersistentContextOptions setViewportSize(int width, int height) {
      return setViewportSize(new ViewportSize(width, height));
    }
    public LaunchPersistentContextOptions setViewportSize(ViewportSize viewportSize) {
      this.viewportSize = Optional.ofNullable(viewportSize);
      return this;
    }
  }
  /**
   * This methods attaches Playwright to an existing browser instance.
   *
   * @param wsEndpoint A browser websocket endpoint to connect to.
   */
  default Browser connect(String wsEndpoint) {
    return connect(wsEndpoint, null);
  }
  /**
   * This methods attaches Playwright to an existing browser instance.
   *
   * @param wsEndpoint A browser websocket endpoint to connect to.
   */
  Browser connect(String wsEndpoint, ConnectOptions options);
  /**
   * This methods attaches Playwright to an existing browser instance using the Chrome DevTools Protocol.
   *
   * <p> The default browser context is accessible via {@link Browser#contexts Browser.contexts()}.
   *
   * <p> <strong>NOTE:</strong> Connecting over the Chrome DevTools Protocol is only supported for Chromium-based browsers.
   *
   * @param endpointURL A CDP websocket endpoint or http url to connect to. For example {@code http://localhost:9222/} or
   * {@code ws://127.0.0.1:9222/devtools/browser/387adf4c-243f-4051-a181-46798f4a46f4}.
   */
  default Browser connectOverCDP(String endpointURL) {
    return connectOverCDP(endpointURL, null);
  }
  /**
   * This methods attaches Playwright to an existing browser instance using the Chrome DevTools Protocol.
   *
   * <p> The default browser context is accessible via {@link Browser#contexts Browser.contexts()}.
   *
   * <p> <strong>NOTE:</strong> Connecting over the Chrome DevTools Protocol is only supported for Chromium-based browsers.
   *
   * @param endpointURL A CDP websocket endpoint or http url to connect to. For example {@code http://localhost:9222/} or
   * {@code ws://127.0.0.1:9222/devtools/browser/387adf4c-243f-4051-a181-46798f4a46f4}.
   */
  Browser connectOverCDP(String endpointURL, ConnectOverCDPOptions options);
  /**
   * A path where Playwright expects to find a bundled browser executable.
   */
  String executablePath();
  /**
   * Returns the browser instance.
   *
   * <p> You can use {@code ignoreDefaultArgs} to filter out {@code --mute-audio} from default arguments:
   * <pre>{@code
   * // Or "firefox" or "webkit".
   * Browser browser = chromium.launch(new BrowserType.LaunchOptions()
   *   .setIgnoreDefaultArgs(Arrays.asList("--mute-audio")));
   * }</pre>
   *
   * <p> > **Chromium-only** Playwright can also be used to control the Google Chrome or Microsoft Edge browsers, but it works
   * best with the version of Chromium it is bundled with. There is no guarantee it will work with any other version. Use
   * {@code executablePath} option with extreme caution.
   *
   * <p> >
   *
   * <p> > If Google Chrome (rather than Chromium) is preferred, a <a
   * href="https://www.google.com/chrome/browser/canary.html">Chrome Canary</a> or <a
   * href="https://www.chromium.org/getting-involved/dev-channel">Dev Channel</a> build is suggested.
   *
   * <p> >
   *
   * <p> > Stock browsers like Google Chrome and Microsoft Edge are suitable for tests that require proprietary media codecs for
   * video playback. See <a
   * href="https://www.howtogeek.com/202825/what%E2%80%99s-the-difference-between-chromium-and-chrome/">this article</a> for
   * other differences between Chromium and Chrome. <a
   * href="https://chromium.googlesource.com/chromium/src/+/lkgr/docs/chromium_browser_vs_google_chrome.md">This article</a>
   * describes some differences for Linux users.
   */
  default Browser launch() {
    return launch(null);
  }
  /**
   * Returns the browser instance.
   *
   * <p> You can use {@code ignoreDefaultArgs} to filter out {@code --mute-audio} from default arguments:
   * <pre>{@code
   * // Or "firefox" or "webkit".
   * Browser browser = chromium.launch(new BrowserType.LaunchOptions()
   *   .setIgnoreDefaultArgs(Arrays.asList("--mute-audio")));
   * }</pre>
   *
   * <p> > **Chromium-only** Playwright can also be used to control the Google Chrome or Microsoft Edge browsers, but it works
   * best with the version of Chromium it is bundled with. There is no guarantee it will work with any other version. Use
   * {@code executablePath} option with extreme caution.
   *
   * <p> >
   *
   * <p> > If Google Chrome (rather than Chromium) is preferred, a <a
   * href="https://www.google.com/chrome/browser/canary.html">Chrome Canary</a> or <a
   * href="https://www.chromium.org/getting-involved/dev-channel">Dev Channel</a> build is suggested.
   *
   * <p> >
   *
   * <p> > Stock browsers like Google Chrome and Microsoft Edge are suitable for tests that require proprietary media codecs for
   * video playback. See <a
   * href="https://www.howtogeek.com/202825/what%E2%80%99s-the-difference-between-chromium-and-chrome/">this article</a> for
   * other differences between Chromium and Chrome. <a
   * href="https://chromium.googlesource.com/chromium/src/+/lkgr/docs/chromium_browser_vs_google_chrome.md">This article</a>
   * describes some differences for Linux users.
   */
  Browser launch(LaunchOptions options);
  /**
   * Returns the persistent browser context instance.
   *
   * <p> Launches browser that uses persistent storage located at {@code userDataDir} and returns the only context. Closing this
   * context will automatically close the browser.
   *
   * @param userDataDir Path to a User Data Directory, which stores browser session data like cookies and local storage. More details for <a
   * href="https://chromium.googlesource.com/chromium/src/+/master/docs/user_data_dir.md#introduction">Chromium</a> and <a
   * href="https://developer.mozilla.org/en-US/docs/Mozilla/Command_Line_Options#User_Profile">Firefox</a>. Note that
   * Chromium's user data directory is the **parent** directory of the "Profile Path" seen at {@code chrome://version}.
   */
  default BrowserContext launchPersistentContext(Path userDataDir) {
    return launchPersistentContext(userDataDir, null);
  }
  /**
   * Returns the persistent browser context instance.
   *
   * <p> Launches browser that uses persistent storage located at {@code userDataDir} and returns the only context. Closing this
   * context will automatically close the browser.
   *
   * @param userDataDir Path to a User Data Directory, which stores browser session data like cookies and local storage. More details for <a
   * href="https://chromium.googlesource.com/chromium/src/+/master/docs/user_data_dir.md#introduction">Chromium</a> and <a
   * href="https://developer.mozilla.org/en-US/docs/Mozilla/Command_Line_Options#User_Profile">Firefox</a>. Note that
   * Chromium's user data directory is the **parent** directory of the "Profile Path" seen at {@code chrome://version}.
   */
  BrowserContext launchPersistentContext(Path userDataDir, LaunchPersistentContextOptions options);
  /**
   * Returns browser name. For example: {@code "chromium"}, {@code "webkit"} or {@code "firefox"}.
   */
  String name();
}

