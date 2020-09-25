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

interface BrowserType{

  class ConnectOptions {
    String wsEndpoint;
    Integer slowMo;
    Logger logger;
    Integer timeout;
  }
  Browser connect(ConnectOptions options);
  String executablePath();

  class LaunchOptions {
    Boolean headless;
    String executablePath;
    List<String> args;
    Boolean ignoreDefaultArgs;
    Object proxy;
    String downloadsPath;
    String artifactsPath;
    Boolean chromiumSandbox;
    Map<String, String> firefoxUserPrefs;
    Boolean handleSIGINT;
    Boolean handleSIGTERM;
    Boolean handleSIGHUP;
    Logger logger;
    Integer timeout;
    Map<String, String> env;
    Boolean devtools;
    Integer slowMo;
  }
  Browser launch(LaunchOptions options);

  class LaunchPersistentContextOptions {
    Boolean headless;
    String executablePath;
    List<String> args;
    Boolean ignoreDefaultArgs;
    Object proxy;
    Boolean acceptDownloads;
    String downloadsPath;
    String artifactsPath;
    Boolean chromiumSandbox;
    Boolean handleSIGINT;
    Boolean handleSIGTERM;
    Boolean handleSIGHUP;
    Logger logger;
    Integer timeout;
    Map<String, String> env;
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
    Object geolocation;
    String locale;
    List<String> permissions;
    Map<String, String> extraHTTPHeaders;
    Boolean offline;
    Object httpCredentials;
    enum ColorScheme { DARK, LIGHT, NO_PREFERENCE }
    ColorScheme colorScheme;
    String relativeArtifactsPath;
    Boolean recordVideos;
    Object videoSize;
    Boolean recordTrace;
  }
  BrowserContext launchPersistentContext(String userDataDir, LaunchPersistentContextOptions options);

  class LaunchServerOptions {
    Boolean headless;
    Integer port;
    String executablePath;
    List<String> args;
    Boolean ignoreDefaultArgs;
    Object proxy;
    String downloadsPath;
    String artifactsPath;
    Boolean chromiumSandbox;
    Map<String, String> firefoxUserPrefs;
    Boolean handleSIGINT;
    Boolean handleSIGTERM;
    Boolean handleSIGHUP;
    Logger logger;
    Integer timeout;
    Map<String, String> env;
    Boolean devtools;
  }
  BrowserServer launchServer(LaunchServerOptions options);
  String name();
}

