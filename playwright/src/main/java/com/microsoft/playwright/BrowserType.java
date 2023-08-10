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
import java.util.regex.Pattern;

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
     * This option exposes network available on the connecting client to the browser being connected to. Consists of a list of
     * rules separated by comma.
     *
     * <p> Available rules:
     * <ol>
     * <li> Hostname pattern, for example: {@code example.com}, {@code *.org:99}, {@code x.*.y.com}, {@code *foo.org}.</li>
     * <li> IP literal, for example: {@code 127.0.0.1}, {@code 0.0.0.0:99}, {@code [::1]}, {@code [0:0::1]:99}.</li>
     * <li> {@code <loopback>} that matches local loopback interfaces: {@code localhost}, {@code *.localhost}, {@code 127.0.0.1},
     * {@code [::1]}.</li>
     * </ol>
     *
     * <p> Some common examples:
     * <ol>
     * <li> {@code "*"} to expose all network.</li>
     * <li> {@code "<loopback>"} to expose localhost network.</li>
     * <li> {@code "*.test.internal-domain,*.staging.internal-domain,<loopback>"} to expose test/staging deployments and localhost.</li>
     * </ol>
     */
    public String exposeNetwork;
    /**
     * Additional HTTP headers to be sent with web socket connect request. Optional.
     */
    public Map<String, String> headers;
    /**
     * Slows down Playwright operations by the specified amount of milliseconds. Useful so that you can see what is going on.
     * Defaults to 0.
     */
    public Double slowMo;
    /**
     * Maximum time in milliseconds to wait for the connection to be established. Defaults to {@code 0} (no timeout).
     */
    public Double timeout;

    /**
     * This option exposes network available on the connecting client to the browser being connected to. Consists of a list of
     * rules separated by comma.
     *
     * <p> Available rules:
     * <ol>
     * <li> Hostname pattern, for example: {@code example.com}, {@code *.org:99}, {@code x.*.y.com}, {@code *foo.org}.</li>
     * <li> IP literal, for example: {@code 127.0.0.1}, {@code 0.0.0.0:99}, {@code [::1]}, {@code [0:0::1]:99}.</li>
     * <li> {@code <loopback>} that matches local loopback interfaces: {@code localhost}, {@code *.localhost}, {@code 127.0.0.1},
     * {@code [::1]}.</li>
     * </ol>
     *
     * <p> Some common examples:
     * <ol>
     * <li> {@code "*"} to expose all network.</li>
     * <li> {@code "<loopback>"} to expose localhost network.</li>
     * <li> {@code "*.test.internal-domain,*.staging.internal-domain,<loopback>"} to expose test/staging deployments and localhost.</li>
     * </ol>
     */
    public ConnectOptions setExposeNetwork(String exposeNetwork) {
      this.exposeNetwork = exposeNetwork;
      return this;
    }
    /**
     * Additional HTTP headers to be sent with web socket connect request. Optional.
     */
    public ConnectOptions setHeaders(Map<String, String> headers) {
      this.headers = headers;
      return this;
    }
    /**
     * Slows down Playwright operations by the specified amount of milliseconds. Useful so that you can see what is going on.
     * Defaults to 0.
     */
    public ConnectOptions setSlowMo(double slowMo) {
      this.slowMo = slowMo;
      return this;
    }
    /**
     * Maximum time in milliseconds to wait for the connection to be established. Defaults to {@code 0} (no timeout).
     */
    public ConnectOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
  }
  class ConnectOverCDPOptions {
    /**
     * Additional HTTP headers to be sent with connect request. Optional.
     */
    public Map<String, String> headers;
    /**
     * Slows down Playwright operations by the specified amount of milliseconds. Useful so that you can see what is going on.
     * Defaults to 0.
     */
    public Double slowMo;
    /**
     * Maximum time in milliseconds to wait for the connection to be established. Defaults to {@code 30000} (30 seconds). Pass
     * {@code 0} to disable timeout.
     */
    public Double timeout;

    /**
     * Additional HTTP headers to be sent with connect request. Optional.
     */
    public ConnectOverCDPOptions setHeaders(Map<String, String> headers) {
      this.headers = headers;
      return this;
    }
    /**
     * Slows down Playwright operations by the specified amount of milliseconds. Useful so that you can see what is going on.
     * Defaults to 0.
     */
    public ConnectOverCDPOptions setSlowMo(double slowMo) {
      this.slowMo = slowMo;
      return this;
    }
    /**
     * Maximum time in milliseconds to wait for the connection to be established. Defaults to {@code 30000} (30 seconds). Pass
     * {@code 0} to disable timeout.
     */
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
     * Browser distribution channel.  Supported values are "chrome", "chrome-beta", "chrome-dev", "chrome-canary", "msedge",
     * "msedge-beta", "msedge-dev", "msedge-canary". Read more about using <a
     * href="https://playwright.dev/java/docs/browsers#google-chrome--microsoft-edge">Google Chrome and Microsoft Edge</a>.
     */
    public Object channel;
    /**
     * Enable Chromium sandboxing. Defaults to {@code false}.
     */
    public Boolean chromiumSandbox;
    /**
     * **Chromium-only** Whether to auto-open a Developer Tools panel for each tab. If this option is {@code true}, the {@code
     * headless} option will be set {@code false}.
     */
    public Boolean devtools;
    /**
     * If specified, accepted downloads are downloaded into this directory. Otherwise, temporary directory is created and is
     * deleted when browser is closed. In either case, the downloads are deleted when the browser context they were created in
     * is closed.
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
     * href="https://developer.mozilla.org/en-US/docs/Mozilla/Firefox/Headless_mode">Firefox</a>. Defaults to {@code true}
     * unless the {@code devtools} option is {@code true}.
     */
    public Boolean headless;
    /**
     * If {@code true}, Playwright does not pass its own configurations args and only uses the ones from {@code args}.
     * Dangerous option; use with care. Defaults to {@code false}.
     */
    public Boolean ignoreAllDefaultArgs;
    /**
     * If {@code true}, Playwright does not pass its own configurations args and only uses the ones from {@code args}.
     * Dangerous option; use with care.
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
     * Maximum time in milliseconds to wait for the browser instance to start. Defaults to {@code 30000} (30 seconds). Pass
     * {@code 0} to disable timeout.
     */
    public Double timeout;
    /**
     * If specified, traces are saved into this directory.
     */
    public Path tracesDir;

    /**
     * Additional arguments to pass to the browser instance. The list of Chromium flags can be found <a
     * href="http://peter.sh/experiments/chromium-command-line-switches/">here</a>.
     */
    public LaunchOptions setArgs(List<String> args) {
      this.args = args;
      return this;
    }
    @Deprecated
    /**
     * Browser distribution channel.  Supported values are "chrome", "chrome-beta", "chrome-dev", "chrome-canary", "msedge",
     * "msedge-beta", "msedge-dev", "msedge-canary". Read more about using <a
     * href="https://playwright.dev/java/docs/browsers#google-chrome--microsoft-edge">Google Chrome and Microsoft Edge</a>.
     */
    public LaunchOptions setChannel(BrowserChannel channel) {
      this.channel = channel;
      return this;
    }
    /**
     * Browser distribution channel.  Supported values are "chrome", "chrome-beta", "chrome-dev", "chrome-canary", "msedge",
     * "msedge-beta", "msedge-dev", "msedge-canary". Read more about using <a
     * href="https://playwright.dev/java/docs/browsers#google-chrome--microsoft-edge">Google Chrome and Microsoft Edge</a>.
     */
    public LaunchOptions setChannel(String channel) {
      this.channel = channel;
      return this;
    }
    /**
     * Enable Chromium sandboxing. Defaults to {@code false}.
     */
    public LaunchOptions setChromiumSandbox(boolean chromiumSandbox) {
      this.chromiumSandbox = chromiumSandbox;
      return this;
    }
    /**
     * **Chromium-only** Whether to auto-open a Developer Tools panel for each tab. If this option is {@code true}, the {@code
     * headless} option will be set {@code false}.
     */
    public LaunchOptions setDevtools(boolean devtools) {
      this.devtools = devtools;
      return this;
    }
    /**
     * If specified, accepted downloads are downloaded into this directory. Otherwise, temporary directory is created and is
     * deleted when browser is closed. In either case, the downloads are deleted when the browser context they were created in
     * is closed.
     */
    public LaunchOptions setDownloadsPath(Path downloadsPath) {
      this.downloadsPath = downloadsPath;
      return this;
    }
    /**
     * Specify environment variables that will be visible to the browser. Defaults to {@code process.env}.
     */
    public LaunchOptions setEnv(Map<String, String> env) {
      this.env = env;
      return this;
    }
    /**
     * Path to a browser executable to run instead of the bundled one. If {@code executablePath} is a relative path, then it is
     * resolved relative to the current working directory. Note that Playwright only works with the bundled Chromium, Firefox
     * or WebKit, use at your own risk.
     */
    public LaunchOptions setExecutablePath(Path executablePath) {
      this.executablePath = executablePath;
      return this;
    }
    /**
     * Firefox user preferences. Learn more about the Firefox user preferences at <a
     * href="https://support.mozilla.org/en-US/kb/about-config-editor-firefox">{@code about:config}</a>.
     */
    public LaunchOptions setFirefoxUserPrefs(Map<String, Object> firefoxUserPrefs) {
      this.firefoxUserPrefs = firefoxUserPrefs;
      return this;
    }
    /**
     * Close the browser process on SIGHUP. Defaults to {@code true}.
     */
    public LaunchOptions setHandleSIGHUP(boolean handleSIGHUP) {
      this.handleSIGHUP = handleSIGHUP;
      return this;
    }
    /**
     * Close the browser process on Ctrl-C. Defaults to {@code true}.
     */
    public LaunchOptions setHandleSIGINT(boolean handleSIGINT) {
      this.handleSIGINT = handleSIGINT;
      return this;
    }
    /**
     * Close the browser process on SIGTERM. Defaults to {@code true}.
     */
    public LaunchOptions setHandleSIGTERM(boolean handleSIGTERM) {
      this.handleSIGTERM = handleSIGTERM;
      return this;
    }
    /**
     * Whether to run browser in headless mode. More details for <a
     * href="https://developers.google.com/web/updates/2017/04/headless-chrome">Chromium</a> and <a
     * href="https://developer.mozilla.org/en-US/docs/Mozilla/Firefox/Headless_mode">Firefox</a>. Defaults to {@code true}
     * unless the {@code devtools} option is {@code true}.
     */
    public LaunchOptions setHeadless(boolean headless) {
      this.headless = headless;
      return this;
    }
    /**
     * If {@code true}, Playwright does not pass its own configurations args and only uses the ones from {@code args}.
     * Dangerous option; use with care. Defaults to {@code false}.
     */
    public LaunchOptions setIgnoreAllDefaultArgs(boolean ignoreAllDefaultArgs) {
      this.ignoreAllDefaultArgs = ignoreAllDefaultArgs;
      return this;
    }
    /**
     * If {@code true}, Playwright does not pass its own configurations args and only uses the ones from {@code args}.
     * Dangerous option; use with care.
     */
    public LaunchOptions setIgnoreDefaultArgs(List<String> ignoreDefaultArgs) {
      this.ignoreDefaultArgs = ignoreDefaultArgs;
      return this;
    }
    /**
     * Network proxy settings.
     */
    public LaunchOptions setProxy(String server) {
      return setProxy(new Proxy(server));
    }
    /**
     * Network proxy settings.
     */
    public LaunchOptions setProxy(Proxy proxy) {
      this.proxy = proxy;
      return this;
    }
    /**
     * Slows down Playwright operations by the specified amount of milliseconds. Useful so that you can see what is going on.
     */
    public LaunchOptions setSlowMo(double slowMo) {
      this.slowMo = slowMo;
      return this;
    }
    /**
     * Maximum time in milliseconds to wait for the browser instance to start. Defaults to {@code 30000} (30 seconds). Pass
     * {@code 0} to disable timeout.
     */
    public LaunchOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * If specified, traces are saved into this directory.
     */
    public LaunchOptions setTracesDir(Path tracesDir) {
      this.tracesDir = tracesDir;
      return this;
    }
  }
  class LaunchPersistentContextOptions {
    /**
     * Whether to automatically download all the attachments. Defaults to {@code true} where all the downloads are accepted.
     */
    public Boolean acceptDownloads;
    /**
     * Additional arguments to pass to the browser instance. The list of Chromium flags can be found <a
     * href="http://peter.sh/experiments/chromium-command-line-switches/">here</a>.
     */
    public List<String> args;
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
     * Browser distribution channel.  Supported values are "chrome", "chrome-beta", "chrome-dev", "chrome-canary", "msedge",
     * "msedge-beta", "msedge-dev", "msedge-canary". Read more about using <a
     * href="https://playwright.dev/java/docs/browsers#google-chrome--microsoft-edge">Google Chrome and Microsoft Edge</a>.
     */
    public Object channel;
    /**
     * Enable Chromium sandboxing. Defaults to {@code false}.
     */
    public Boolean chromiumSandbox;
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
     * **Chromium-only** Whether to auto-open a Developer Tools panel for each tab. If this option is {@code true}, the {@code
     * headless} option will be set {@code false}.
     */
    public Boolean devtools;
    /**
     * If specified, accepted downloads are downloaded into this directory. Otherwise, temporary directory is created and is
     * deleted when browser is closed. In either case, the downloads are deleted when the browser context they were created in
     * is closed.
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
     * Specifies if viewport supports touch events. Defaults to false. Learn more about <a
     * href="https://playwright.dev/java/docs/emulation#devices">mobile emulation</a>.
     */
    public Boolean hasTouch;
    /**
     * Whether to run browser in headless mode. More details for <a
     * href="https://developers.google.com/web/updates/2017/04/headless-chrome">Chromium</a> and <a
     * href="https://developer.mozilla.org/en-US/docs/Mozilla/Firefox/Headless_mode">Firefox</a>. Defaults to {@code true}
     * unless the {@code devtools} option is {@code true}.
     */
    public Boolean headless;
    /**
     * Credentials for <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication">HTTP authentication</a>. If
     * no origin is specified, the username and password are sent to any servers upon unauthorized responses.
     */
    public HttpCredentials httpCredentials;
    /**
     * If {@code true}, Playwright does not pass its own configurations args and only uses the ones from {@code args}.
     * Dangerous option; use with care. Defaults to {@code false}.
     */
    public Boolean ignoreAllDefaultArgs;
    /**
     * If {@code true}, Playwright does not pass its own configurations args and only uses the ones from {@code args}.
     * Dangerous option; use with care.
     */
    public List<String> ignoreDefaultArgs;
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
     * Network proxy settings.
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
     * Slows down Playwright operations by the specified amount of milliseconds. Useful so that you can see what is going on.
     */
    public Double slowMo;
    /**
     * If set to true, enables strict selectors mode for this context. In the strict selectors mode all operations on selectors
     * that imply single target DOM element will throw when more than one element matches the selector. This option does not
     * affect any Locator APIs (Locators are always strict). Defaults to {@code false}. See {@code Locator} to learn more about
     * the strict mode.
     */
    public Boolean strictSelectors;
    /**
     * Maximum time in milliseconds to wait for the browser instance to start. Defaults to {@code 30000} (30 seconds). Pass
     * {@code 0} to disable timeout.
     */
    public Double timeout;
    /**
     * Changes the timezone of the context. See <a
     * href="https://cs.chromium.org/chromium/src/third_party/icu/source/data/misc/metaZones.txt?rcl=faee8bc70570192d82d2978a71e2a615788597d1">ICU's
     * metaZones.txt</a> for a list of supported timezone IDs. Defaults to the system timezone.
     */
    public String timezoneId;
    /**
     * If specified, traces are saved into this directory.
     */
    public Path tracesDir;
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
    public LaunchPersistentContextOptions setAcceptDownloads(boolean acceptDownloads) {
      this.acceptDownloads = acceptDownloads;
      return this;
    }
    /**
     * Additional arguments to pass to the browser instance. The list of Chromium flags can be found <a
     * href="http://peter.sh/experiments/chromium-command-line-switches/">here</a>.
     */
    public LaunchPersistentContextOptions setArgs(List<String> args) {
      this.args = args;
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
    public LaunchPersistentContextOptions setBaseURL(String baseURL) {
      this.baseURL = baseURL;
      return this;
    }
    /**
     * Toggles bypassing page's Content-Security-Policy. Defaults to {@code false}.
     */
    public LaunchPersistentContextOptions setBypassCSP(boolean bypassCSP) {
      this.bypassCSP = bypassCSP;
      return this;
    }
    @Deprecated
    /**
     * Browser distribution channel.  Supported values are "chrome", "chrome-beta", "chrome-dev", "chrome-canary", "msedge",
     * "msedge-beta", "msedge-dev", "msedge-canary". Read more about using <a
     * href="https://playwright.dev/java/docs/browsers#google-chrome--microsoft-edge">Google Chrome and Microsoft Edge</a>.
     */
    public LaunchPersistentContextOptions setChannel(BrowserChannel channel) {
      this.channel = channel;
      return this;
    }
    /**
     * Browser distribution channel.  Supported values are "chrome", "chrome-beta", "chrome-dev", "chrome-canary", "msedge",
     * "msedge-beta", "msedge-dev", "msedge-canary". Read more about using <a
     * href="https://playwright.dev/java/docs/browsers#google-chrome--microsoft-edge">Google Chrome and Microsoft Edge</a>.
     */
    public LaunchPersistentContextOptions setChannel(String channel) {
      this.channel = channel;
      return this;
    }
    /**
     * Enable Chromium sandboxing. Defaults to {@code false}.
     */
    public LaunchPersistentContextOptions setChromiumSandbox(boolean chromiumSandbox) {
      this.chromiumSandbox = chromiumSandbox;
      return this;
    }
    /**
     * Emulates {@code "prefers-colors-scheme"} media feature, supported values are {@code "light"}, {@code "dark"}, {@code
     * "no-preference"}. See {@link Page#emulateMedia Page.emulateMedia()} for more details. Passing {@code null} resets
     * emulation to system defaults. Defaults to {@code "light"}.
     */
    public LaunchPersistentContextOptions setColorScheme(ColorScheme colorScheme) {
      this.colorScheme = Optional.ofNullable(colorScheme);
      return this;
    }
    /**
     * Specify device scale factor (can be thought of as dpr). Defaults to {@code 1}. Learn more about <a
     * href="https://playwright.dev/java/docs/emulation#devices">emulating devices with device scale factor</a>.
     */
    public LaunchPersistentContextOptions setDeviceScaleFactor(double deviceScaleFactor) {
      this.deviceScaleFactor = deviceScaleFactor;
      return this;
    }
    /**
     * **Chromium-only** Whether to auto-open a Developer Tools panel for each tab. If this option is {@code true}, the {@code
     * headless} option will be set {@code false}.
     */
    public LaunchPersistentContextOptions setDevtools(boolean devtools) {
      this.devtools = devtools;
      return this;
    }
    /**
     * If specified, accepted downloads are downloaded into this directory. Otherwise, temporary directory is created and is
     * deleted when browser is closed. In either case, the downloads are deleted when the browser context they were created in
     * is closed.
     */
    public LaunchPersistentContextOptions setDownloadsPath(Path downloadsPath) {
      this.downloadsPath = downloadsPath;
      return this;
    }
    /**
     * Specify environment variables that will be visible to the browser. Defaults to {@code process.env}.
     */
    public LaunchPersistentContextOptions setEnv(Map<String, String> env) {
      this.env = env;
      return this;
    }
    /**
     * Path to a browser executable to run instead of the bundled one. If {@code executablePath} is a relative path, then it is
     * resolved relative to the current working directory. Note that Playwright only works with the bundled Chromium, Firefox
     * or WebKit, use at your own risk.
     */
    public LaunchPersistentContextOptions setExecutablePath(Path executablePath) {
      this.executablePath = executablePath;
      return this;
    }
    /**
     * An object containing additional HTTP headers to be sent with every request. Defaults to none.
     */
    public LaunchPersistentContextOptions setExtraHTTPHeaders(Map<String, String> extraHTTPHeaders) {
      this.extraHTTPHeaders = extraHTTPHeaders;
      return this;
    }
    /**
     * Emulates {@code "forced-colors"} media feature, supported values are {@code "active"}, {@code "none"}. See {@link
     * Page#emulateMedia Page.emulateMedia()} for more details. Passing {@code null} resets emulation to system defaults.
     * Defaults to {@code "none"}.
     */
    public LaunchPersistentContextOptions setForcedColors(ForcedColors forcedColors) {
      this.forcedColors = Optional.ofNullable(forcedColors);
      return this;
    }
    public LaunchPersistentContextOptions setGeolocation(double latitude, double longitude) {
      return setGeolocation(new Geolocation(latitude, longitude));
    }
    public LaunchPersistentContextOptions setGeolocation(Geolocation geolocation) {
      this.geolocation = geolocation;
      return this;
    }
    /**
     * Close the browser process on SIGHUP. Defaults to {@code true}.
     */
    public LaunchPersistentContextOptions setHandleSIGHUP(boolean handleSIGHUP) {
      this.handleSIGHUP = handleSIGHUP;
      return this;
    }
    /**
     * Close the browser process on Ctrl-C. Defaults to {@code true}.
     */
    public LaunchPersistentContextOptions setHandleSIGINT(boolean handleSIGINT) {
      this.handleSIGINT = handleSIGINT;
      return this;
    }
    /**
     * Close the browser process on SIGTERM. Defaults to {@code true}.
     */
    public LaunchPersistentContextOptions setHandleSIGTERM(boolean handleSIGTERM) {
      this.handleSIGTERM = handleSIGTERM;
      return this;
    }
    /**
     * Specifies if viewport supports touch events. Defaults to false. Learn more about <a
     * href="https://playwright.dev/java/docs/emulation#devices">mobile emulation</a>.
     */
    public LaunchPersistentContextOptions setHasTouch(boolean hasTouch) {
      this.hasTouch = hasTouch;
      return this;
    }
    /**
     * Whether to run browser in headless mode. More details for <a
     * href="https://developers.google.com/web/updates/2017/04/headless-chrome">Chromium</a> and <a
     * href="https://developer.mozilla.org/en-US/docs/Mozilla/Firefox/Headless_mode">Firefox</a>. Defaults to {@code true}
     * unless the {@code devtools} option is {@code true}.
     */
    public LaunchPersistentContextOptions setHeadless(boolean headless) {
      this.headless = headless;
      return this;
    }
    /**
     * Credentials for <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication">HTTP authentication</a>. If
     * no origin is specified, the username and password are sent to any servers upon unauthorized responses.
     */
    public LaunchPersistentContextOptions setHttpCredentials(String username, String password) {
      return setHttpCredentials(new HttpCredentials(username, password));
    }
    /**
     * Credentials for <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Authentication">HTTP authentication</a>. If
     * no origin is specified, the username and password are sent to any servers upon unauthorized responses.
     */
    public LaunchPersistentContextOptions setHttpCredentials(HttpCredentials httpCredentials) {
      this.httpCredentials = httpCredentials;
      return this;
    }
    /**
     * If {@code true}, Playwright does not pass its own configurations args and only uses the ones from {@code args}.
     * Dangerous option; use with care. Defaults to {@code false}.
     */
    public LaunchPersistentContextOptions setIgnoreAllDefaultArgs(boolean ignoreAllDefaultArgs) {
      this.ignoreAllDefaultArgs = ignoreAllDefaultArgs;
      return this;
    }
    /**
     * If {@code true}, Playwright does not pass its own configurations args and only uses the ones from {@code args}.
     * Dangerous option; use with care.
     */
    public LaunchPersistentContextOptions setIgnoreDefaultArgs(List<String> ignoreDefaultArgs) {
      this.ignoreDefaultArgs = ignoreDefaultArgs;
      return this;
    }
    /**
     * Whether to ignore HTTPS errors when sending network requests. Defaults to {@code false}.
     */
    public LaunchPersistentContextOptions setIgnoreHTTPSErrors(boolean ignoreHTTPSErrors) {
      this.ignoreHTTPSErrors = ignoreHTTPSErrors;
      return this;
    }
    /**
     * Whether the {@code meta viewport} tag is taken into account and touch events are enabled. isMobile is a part of device,
     * so you don't actually need to set it manually. Defaults to {@code false} and is not supported in Firefox. Learn more
     * about <a href="https://playwright.dev/java/docs/emulation#ismobile">mobile emulation</a>.
     */
    public LaunchPersistentContextOptions setIsMobile(boolean isMobile) {
      this.isMobile = isMobile;
      return this;
    }
    /**
     * Whether or not to enable JavaScript in the context. Defaults to {@code true}. Learn more about <a
     * href="https://playwright.dev/java/docs/emulation#javascript-enabled">disabling JavaScript</a>.
     */
    public LaunchPersistentContextOptions setJavaScriptEnabled(boolean javaScriptEnabled) {
      this.javaScriptEnabled = javaScriptEnabled;
      return this;
    }
    /**
     * Specify user locale, for example {@code en-GB}, {@code de-DE}, etc. Locale will affect {@code navigator.language} value,
     * {@code Accept-Language} request header value as well as number and date formatting rules. Defaults to the system default
     * locale. Learn more about emulation in our <a
     * href="https://playwright.dev/java/docs/emulation#locale--timezone">emulation guide</a>.
     */
    public LaunchPersistentContextOptions setLocale(String locale) {
      this.locale = locale;
      return this;
    }
    /**
     * Whether to emulate network being offline. Defaults to {@code false}. Learn more about <a
     * href="https://playwright.dev/java/docs/emulation#offline">network emulation</a>.
     */
    public LaunchPersistentContextOptions setOffline(boolean offline) {
      this.offline = offline;
      return this;
    }
    /**
     * A list of permissions to grant to all pages in this context. See {@link BrowserContext#grantPermissions
     * BrowserContext.grantPermissions()} for more details. Defaults to none.
     */
    public LaunchPersistentContextOptions setPermissions(List<String> permissions) {
      this.permissions = permissions;
      return this;
    }
    /**
     * Network proxy settings.
     */
    public LaunchPersistentContextOptions setProxy(String server) {
      return setProxy(new Proxy(server));
    }
    /**
     * Network proxy settings.
     */
    public LaunchPersistentContextOptions setProxy(Proxy proxy) {
      this.proxy = proxy;
      return this;
    }
    /**
     * Optional setting to control resource content management. If {@code omit} is specified, content is not persisted. If
     * {@code attach} is specified, resources are persisted as separate files and all of these files are archived along with
     * the HAR file. Defaults to {@code embed}, which stores content inline the HAR file as per HAR specification.
     */
    public LaunchPersistentContextOptions setRecordHarContent(HarContentPolicy recordHarContent) {
      this.recordHarContent = recordHarContent;
      return this;
    }
    /**
     * When set to {@code minimal}, only record information necessary for routing from HAR. This omits sizes, timing, page,
     * cookies, security and other types of HAR information that are not used when replaying from HAR. Defaults to {@code
     * full}.
     */
    public LaunchPersistentContextOptions setRecordHarMode(HarMode recordHarMode) {
      this.recordHarMode = recordHarMode;
      return this;
    }
    /**
     * Optional setting to control whether to omit request content from the HAR. Defaults to {@code false}.
     */
    public LaunchPersistentContextOptions setRecordHarOmitContent(boolean recordHarOmitContent) {
      this.recordHarOmitContent = recordHarOmitContent;
      return this;
    }
    /**
     * Enables <a href="http://www.softwareishard.com/blog/har-12-spec">HAR</a> recording for all pages into the specified HAR
     * file on the filesystem. If not specified, the HAR is not recorded. Make sure to call {@link BrowserContext#close
     * BrowserContext.close()} for the HAR to be saved.
     */
    public LaunchPersistentContextOptions setRecordHarPath(Path recordHarPath) {
      this.recordHarPath = recordHarPath;
      return this;
    }
    public LaunchPersistentContextOptions setRecordHarUrlFilter(String recordHarUrlFilter) {
      this.recordHarUrlFilter = recordHarUrlFilter;
      return this;
    }
    public LaunchPersistentContextOptions setRecordHarUrlFilter(Pattern recordHarUrlFilter) {
      this.recordHarUrlFilter = recordHarUrlFilter;
      return this;
    }
    /**
     * Enables video recording for all pages into the specified directory. If not specified videos are not recorded. Make sure
     * to call {@link BrowserContext#close BrowserContext.close()} for videos to be saved.
     */
    public LaunchPersistentContextOptions setRecordVideoDir(Path recordVideoDir) {
      this.recordVideoDir = recordVideoDir;
      return this;
    }
    /**
     * Dimensions of the recorded videos. If not specified the size will be equal to {@code viewport} scaled down to fit into
     * 800x800. If {@code viewport} is not configured explicitly the video size defaults to 800x450. Actual picture of each
     * page will be scaled down if necessary to fit the specified size.
     */
    public LaunchPersistentContextOptions setRecordVideoSize(int width, int height) {
      return setRecordVideoSize(new RecordVideoSize(width, height));
    }
    /**
     * Dimensions of the recorded videos. If not specified the size will be equal to {@code viewport} scaled down to fit into
     * 800x800. If {@code viewport} is not configured explicitly the video size defaults to 800x450. Actual picture of each
     * page will be scaled down if necessary to fit the specified size.
     */
    public LaunchPersistentContextOptions setRecordVideoSize(RecordVideoSize recordVideoSize) {
      this.recordVideoSize = recordVideoSize;
      return this;
    }
    /**
     * Emulates {@code "prefers-reduced-motion"} media feature, supported values are {@code "reduce"}, {@code "no-preference"}.
     * See {@link Page#emulateMedia Page.emulateMedia()} for more details. Passing {@code null} resets emulation to system
     * defaults. Defaults to {@code "no-preference"}.
     */
    public LaunchPersistentContextOptions setReducedMotion(ReducedMotion reducedMotion) {
      this.reducedMotion = Optional.ofNullable(reducedMotion);
      return this;
    }
    /**
     * Emulates consistent window screen size available inside web page via {@code window.screen}. Is only used when the {@code
     * viewport} is set.
     */
    public LaunchPersistentContextOptions setScreenSize(int width, int height) {
      return setScreenSize(new ScreenSize(width, height));
    }
    /**
     * Emulates consistent window screen size available inside web page via {@code window.screen}. Is only used when the {@code
     * viewport} is set.
     */
    public LaunchPersistentContextOptions setScreenSize(ScreenSize screenSize) {
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
    public LaunchPersistentContextOptions setServiceWorkers(ServiceWorkerPolicy serviceWorkers) {
      this.serviceWorkers = serviceWorkers;
      return this;
    }
    /**
     * Slows down Playwright operations by the specified amount of milliseconds. Useful so that you can see what is going on.
     */
    public LaunchPersistentContextOptions setSlowMo(double slowMo) {
      this.slowMo = slowMo;
      return this;
    }
    /**
     * If set to true, enables strict selectors mode for this context. In the strict selectors mode all operations on selectors
     * that imply single target DOM element will throw when more than one element matches the selector. This option does not
     * affect any Locator APIs (Locators are always strict). Defaults to {@code false}. See {@code Locator} to learn more about
     * the strict mode.
     */
    public LaunchPersistentContextOptions setStrictSelectors(boolean strictSelectors) {
      this.strictSelectors = strictSelectors;
      return this;
    }
    /**
     * Maximum time in milliseconds to wait for the browser instance to start. Defaults to {@code 30000} (30 seconds). Pass
     * {@code 0} to disable timeout.
     */
    public LaunchPersistentContextOptions setTimeout(double timeout) {
      this.timeout = timeout;
      return this;
    }
    /**
     * Changes the timezone of the context. See <a
     * href="https://cs.chromium.org/chromium/src/third_party/icu/source/data/misc/metaZones.txt?rcl=faee8bc70570192d82d2978a71e2a615788597d1">ICU's
     * metaZones.txt</a> for a list of supported timezone IDs. Defaults to the system timezone.
     */
    public LaunchPersistentContextOptions setTimezoneId(String timezoneId) {
      this.timezoneId = timezoneId;
      return this;
    }
    /**
     * If specified, traces are saved into this directory.
     */
    public LaunchPersistentContextOptions setTracesDir(Path tracesDir) {
      this.tracesDir = tracesDir;
      return this;
    }
    /**
     * Specific user agent to use in this context.
     */
    public LaunchPersistentContextOptions setUserAgent(String userAgent) {
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
    public LaunchPersistentContextOptions setViewportSize(int width, int height) {
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
    public LaunchPersistentContextOptions setViewportSize(ViewportSize viewportSize) {
      this.viewportSize = Optional.ofNullable(viewportSize);
      return this;
    }
  }
  /**
   * This method attaches Playwright to an existing browser instance. When connecting to another browser launched via {@code
   * BrowserType.launchServer} in Node.js, the major and minor version needs to match the client version (1.2.3 → is
   * compatible with 1.2.x).
   *
   * @param wsEndpoint A browser websocket endpoint to connect to.
   * @since v1.8
   */
  default Browser connect(String wsEndpoint) {
    return connect(wsEndpoint, null);
  }
  /**
   * This method attaches Playwright to an existing browser instance. When connecting to another browser launched via {@code
   * BrowserType.launchServer} in Node.js, the major and minor version needs to match the client version (1.2.3 → is
   * compatible with 1.2.x).
   *
   * @param wsEndpoint A browser websocket endpoint to connect to.
   * @since v1.8
   */
  Browser connect(String wsEndpoint, ConnectOptions options);
  /**
   * This method attaches Playwright to an existing browser instance using the Chrome DevTools Protocol.
   *
   * <p> The default browser context is accessible via {@link Browser#contexts Browser.contexts()}.
   *
   * <p> <strong>NOTE:</strong> Connecting over the Chrome DevTools Protocol is only supported for Chromium-based browsers.
   *
   * <p> **Usage**
   * <pre>{@code
   * Browser browser = playwright.chromium().connectOverCDP("http://localhost:9222");
   * BrowserContext defaultContext = browser.contexts().get(0);
   * Page page = defaultContext.pages().get(0);
   * }</pre>
   *
   * @param endpointURL A CDP websocket endpoint or http url to connect to. For example {@code http://localhost:9222/} or {@code
   * ws://127.0.0.1:9222/devtools/browser/387adf4c-243f-4051-a181-46798f4a46f4}.
   * @since v1.9
   */
  default Browser connectOverCDP(String endpointURL) {
    return connectOverCDP(endpointURL, null);
  }
  /**
   * This method attaches Playwright to an existing browser instance using the Chrome DevTools Protocol.
   *
   * <p> The default browser context is accessible via {@link Browser#contexts Browser.contexts()}.
   *
   * <p> <strong>NOTE:</strong> Connecting over the Chrome DevTools Protocol is only supported for Chromium-based browsers.
   *
   * <p> **Usage**
   * <pre>{@code
   * Browser browser = playwright.chromium().connectOverCDP("http://localhost:9222");
   * BrowserContext defaultContext = browser.contexts().get(0);
   * Page page = defaultContext.pages().get(0);
   * }</pre>
   *
   * @param endpointURL A CDP websocket endpoint or http url to connect to. For example {@code http://localhost:9222/} or {@code
   * ws://127.0.0.1:9222/devtools/browser/387adf4c-243f-4051-a181-46798f4a46f4}.
   * @since v1.9
   */
  Browser connectOverCDP(String endpointURL, ConnectOverCDPOptions options);
  /**
   * A path where Playwright expects to find a bundled browser executable.
   *
   * @since v1.8
   */
  String executablePath();
  /**
   * Returns the browser instance.
   *
   * <p> **Usage**
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
   *
   * @since v1.8
   */
  default Browser launch() {
    return launch(null);
  }
  /**
   * Returns the browser instance.
   *
   * <p> **Usage**
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
   *
   * @since v1.8
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
   * Chromium's user data directory is the **parent** directory of the "Profile Path" seen at {@code chrome://version}. Pass
   * an empty string to use a temporary directory instead.
   * @since v1.8
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
   * Chromium's user data directory is the **parent** directory of the "Profile Path" seen at {@code chrome://version}. Pass
   * an empty string to use a temporary directory instead.
   * @since v1.8
   */
  BrowserContext launchPersistentContext(Path userDataDir, LaunchPersistentContextOptions options);
  /**
   * Returns browser name. For example: {@code "chromium"}, {@code "webkit"} or {@code "firefox"}.
   *
   * @since v1.8
   */
  String name();
}

