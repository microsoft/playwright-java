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
import java.util.function.BiConsumer;

public interface BrowserType {
  class ConnectOptions {
    String wsEndpoint;
    Integer slowMo;
    Logger logger;
    Integer timeout;

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
      String server;
      String bypass;
      String username;
      String password;

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
    Boolean headless;
    String executablePath;
    List<String> args;
    Boolean ignoreDefaultArgs;
    Proxy proxy;
    String downloadsPath;
    String artifactsPath;
    Boolean chromiumSandbox;
    String firefoxUserPrefs;
    Boolean handleSIGINT;
    Boolean handleSIGTERM;
    Boolean handleSIGHUP;
    Logger logger;
    Integer timeout;
    String env;
    Boolean devtools;
    Integer slowMo;

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
    enum ColorScheme { DARK, LIGHT, NO_PREFERENCE}
    public class Proxy {
      String server;
      String bypass;
      String username;
      String password;

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
    public class Viewport {
      int width;
      int height;

      Viewport() {
      }
      public LaunchPersistentContextOptions done() {
        return LaunchPersistentContextOptions.this;
      }

      public Viewport withWidth(int width) {
        this.width = width;
        return this;
      }
      public Viewport withHeight(int height) {
        this.height = height;
        return this;
      }
    }
    public class Geolocation {
      double latitude;
      double longitude;
      double accuracy;

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
    public class HttpCredentials {
      String username;
      String password;

      HttpCredentials() {
      }
      public LaunchPersistentContextOptions done() {
        return LaunchPersistentContextOptions.this;
      }

      public HttpCredentials withUsername(String username) {
        this.username = username;
        return this;
      }
      public HttpCredentials withPassword(String password) {
        this.password = password;
        return this;
      }
    }
    public class VideoSize {
      int width;
      int height;

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
    Boolean headless;
    String executablePath;
    List<String> args;
    String ignoreDefaultArgs;
    Proxy proxy;
    Boolean acceptDownloads;
    String downloadsPath;
    String artifactsPath;
    Boolean chromiumSandbox;
    Boolean handleSIGINT;
    Boolean handleSIGTERM;
    Boolean handleSIGHUP;
    Logger logger;
    Integer timeout;
    String env;
    Boolean devtools;
    Integer slowMo;
    Boolean ignoreHTTPSErrors;
    Boolean bypassCSP;
    Viewport viewport;
    String userAgent;
    Integer deviceScaleFactor;
    Boolean isMobile;
    Boolean hasTouch;
    Boolean javaScriptEnabled;
    String timezoneId;
    Geolocation geolocation;
    String locale;
    List<String> permissions;
    Map<String, String> extraHTTPHeaders;
    Boolean offline;
    HttpCredentials httpCredentials;
    ColorScheme colorScheme;
    String relativeArtifactsPath;
    Boolean recordVideos;
    VideoSize videoSize;
    Boolean recordTrace;

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
    public Viewport setViewport() {
      this.viewport = new Viewport();
      return this.viewport;
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
    public HttpCredentials setHttpCredentials() {
      this.httpCredentials = new HttpCredentials();
      return this.httpCredentials;
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
      String server;
      String bypass;
      String username;
      String password;

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
    Boolean headless;
    Integer port;
    String executablePath;
    List<String> args;
    String ignoreDefaultArgs;
    Proxy proxy;
    String downloadsPath;
    String artifactsPath;
    Boolean chromiumSandbox;
    String firefoxUserPrefs;
    Boolean handleSIGINT;
    Boolean handleSIGTERM;
    Boolean handleSIGHUP;
    Logger logger;
    Integer timeout;
    String env;
    Boolean devtools;

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

