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

interface Browser{
  void close();
  List<BrowserContext> contexts();
  boolean isConnected();

  class NewContextOptions {
    Boolean acceptDownloads;
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
    Logger logger;
    String relativeArtifactsPath;
    Boolean recordVideos;
    Object videoSize;
    Boolean recordTrace;
  }
  BrowserContext newContext(NewContextOptions options);

  class NewPageOptions {
    Boolean acceptDownloads;
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
    Logger logger;
    String relativeArtifactsPath;
    Boolean recordVideos;
    Object videoSize;
    Boolean recordTrace;
  }
  Page newPage(NewPageOptions options);
  String version();
}

