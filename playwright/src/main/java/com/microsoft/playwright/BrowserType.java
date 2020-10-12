/**
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
    public String artifactsPath;
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
    public LaunchOptions withArtifactsPath(String artifactsPath) {
      this.artifactsPath = artifactsPath;
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
    public enum ColorScheme { DARK, LIGHT, NO_PREFERENCE }
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
    public class Geolocation {
      public double latitude;
      public double longitude;
      public double accuracy;

      Geolocation() {
      }
      public LaunchPersistentContextOptions done() {
        return LaunchPersistentContextOptions.this;
      }

      public Geolocation withLatitude(double latitude) {
        this.latitude = latitude;
        return this;
      }
      public Geolocation withLongitude(double longitude) {
        this.longitude = longitude;
        return this;
      }
      public Geolocation withAccuracy(double accuracy) {
        this.accuracy = accuracy;
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
    public Boolean headless;
    public String executablePath;
    public List<String> args;
    public String ignoreDefaultArgs;
    public Proxy proxy;
    public Boolean acceptDownloads;
    public String downloadsPath;
    public String artifactsPath;
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
    public String relativeArtifactsPath;
    public Boolean recordVideos;
    public VideoSize videoSize;
    public Boolean recordTrace;

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
    public LaunchPersistentContextOptions withArtifactsPath(String artifactsPath) {
      this.artifactsPath = artifactsPath;
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
    public Geolocation setGeolocation() {
      this.geolocation = new Geolocation();
      return this.geolocation;
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
    public LaunchPersistentContextOptions withRelativeArtifactsPath(String relativeArtifactsPath) {
      this.relativeArtifactsPath = relativeArtifactsPath;
      return this;
    }
    public LaunchPersistentContextOptions withRecordVideos(Boolean recordVideos) {
      this.recordVideos = recordVideos;
      return this;
    }
    public VideoSize setVideoSize() {
      this.videoSize = new VideoSize();
      return this.videoSize;
    }
    public LaunchPersistentContextOptions withRecordTrace(Boolean recordTrace) {
      this.recordTrace = recordTrace;
      return this;
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
    public String artifactsPath;
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
    public LaunchServerOptions withArtifactsPath(String artifactsPath) {
      this.artifactsPath = artifactsPath;
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
  Browser connect(ConnectOptions options);
  String executablePath();
  default Browser launch() {
    return launch(null);
  }
  Browser launch(LaunchOptions options);
  default BrowserContext launchPersistentContext(String userDataDir) {
    return launchPersistentContext(userDataDir, null);
  }
  BrowserContext launchPersistentContext(String userDataDir, LaunchPersistentContextOptions options);
  default BrowserServer launchServer() {
    return launchServer(null);
  }
  BrowserServer launchServer(LaunchServerOptions options);
  String name();
}

