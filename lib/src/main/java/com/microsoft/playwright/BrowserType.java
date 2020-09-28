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

interface BrowserType {
  class ConnectOptions {
    String wsEndpoint;
    Integer slowMo;
    Logger logger;
    Integer timeout;
  }
  class LaunchOptions {
    class Proxy {
      String server;
      String bypass;
      String username;
      String password;
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
  }
  class LaunchPersistentContextOptions {
    enum ColorScheme { DARK, LIGHT, NO_PREFERENCE}
    class Proxy {
      String server;
      String bypass;
      String username;
      String password;
    }
    class Geolocation {
      double latitude;
      double longitude;
      double accuracy;
    }
    class HttpCredentials {
      String username;
      String password;
    }
    class VideoSize {
      int width;
      int height;
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
    Object viewport;
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
  }
  class LaunchServerOptions {
    class Proxy {
      String server;
      String bypass;
      String username;
      String password;
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
  }
  Browser connect(ConnectOptions options);
  String executablePath();
  Browser launch(LaunchOptions options);
  BrowserContext launchPersistentContext(String userDataDir, LaunchPersistentContextOptions options);
  BrowserServer launchServer(LaunchServerOptions options);
  String name();
}

