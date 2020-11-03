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

/**
 * BrowserType provides methods to launch a specific browser instance or connect to an existing one.
 * <p>
 * The following is a typical example of using Playwright to drive automation:
 * <p>
 */
public interface BrowserType {
  class ConnectOptions {
    public String wsEndpoint;
    public Integer slowMo;
    public Logger logger;
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
      public String server;
      public String bypass;
      public String username;
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
    public Boolean headless;
    public String executablePath;
    public List<String> args;
    public Boolean ignoreDefaultArgs;
    public Proxy proxy;
    public String downloadsPath;
    public Boolean chromiumSandbox;
    public String firefoxUserPrefs;
    public Boolean handleSIGINT;
    public Boolean handleSIGTERM;
    public Boolean handleSIGHUP;
    public Logger logger;
    public Integer timeout;
    public String env;
    public Boolean devtools;
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
      public String server;
      public String bypass;
      public String username;
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
      public int width;
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
      public Boolean omitContent;
      public String path;

      RecordHar() {
      }
      public LaunchPersistentContextOptions done() {
        return LaunchPersistentContextOptions.this;
      }

      public RecordHar withOmitContent(Boolean omitContent) {
        this.omitContent = omitContent;
        return this;
      }
      public RecordHar withPath(String path) {
        this.path = path;
        return this;
      }
    }
    public Boolean headless;
    public String executablePath;
    public List<String> args;
    public String ignoreDefaultArgs;
    public Proxy proxy;
    public Boolean acceptDownloads;
    public String downloadsPath;
    public Boolean chromiumSandbox;
    public Boolean handleSIGINT;
    public Boolean handleSIGTERM;
    public Boolean handleSIGHUP;
    public Logger logger;
    public Integer timeout;
    public String env;
    public Boolean devtools;
    public Integer slowMo;
    public Boolean ignoreHTTPSErrors;
    public Boolean bypassCSP;
    public Page.Viewport viewport;
    public String userAgent;
    public Integer deviceScaleFactor;
    public Boolean isMobile;
    public Boolean hasTouch;
    public Boolean javaScriptEnabled;
    public String timezoneId;
    public Geolocation geolocation;
    public String locale;
    public List<String> permissions;
    public Map<String, String> extraHTTPHeaders;
    public Boolean offline;
    public BrowserContext.HTTPCredentials httpCredentials;
    public ColorScheme colorScheme;
    public String videosPath;
    public VideoSize videoSize;
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
      public String server;
      public String bypass;
      public String username;
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
    public Boolean headless;
    public Integer port;
    public String executablePath;
    public List<String> args;
    public String ignoreDefaultArgs;
    public Proxy proxy;
    public String downloadsPath;
    public Boolean chromiumSandbox;
    public String firefoxUserPrefs;
    public Boolean handleSIGINT;
    public Boolean handleSIGTERM;
    public Boolean handleSIGHUP;
    public Logger logger;
    public Integer timeout;
    public String env;
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
  default BrowserContext launchPersistentContext(String userDataDir) {
    return launchPersistentContext(userDataDir, null);
  }
  /**
   * Launches browser that uses persistent storage located at {@code userDataDir} and returns the only context. Closing this context will automatically close the browser.
   * @param userDataDir Path to a User Data Directory, which stores browser session data like cookies and local storage. More details for Chromium and Firefox.
   * @param options Set of configurable options to set on the browser. Can have the following fields:
   * @return Promise that resolves to the persistent browser context instance.
   */
  BrowserContext launchPersistentContext(String userDataDir, LaunchPersistentContextOptions options);
  /**
   * Returns browser name. For example: {@code 'chromium'}, {@code 'webkit'} or {@code 'firefox'}.
   */
  String name();
}

