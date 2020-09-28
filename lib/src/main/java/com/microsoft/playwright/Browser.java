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

interface Browser {
  class NewContextOptions {
    enum ColorScheme { DARK, LIGHT, NO_PREFERENCE}
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
    Geolocation geolocation;
    String locale;
    List<String> permissions;
    Map<String, String> extraHTTPHeaders;
    Boolean offline;
    HttpCredentials httpCredentials;
    ColorScheme colorScheme;
    Logger logger;
    String relativeArtifactsPath;
    Boolean recordVideos;
    VideoSize videoSize;
    Boolean recordTrace;
  }
  class NewPageOptions {
    enum ColorScheme { DARK, LIGHT, NO_PREFERENCE}
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
    Geolocation geolocation;
    String locale;
    List<String> permissions;
    Map<String, String> extraHTTPHeaders;
    Boolean offline;
    HttpCredentials httpCredentials;
    ColorScheme colorScheme;
    Logger logger;
    String relativeArtifactsPath;
    Boolean recordVideos;
    VideoSize videoSize;
    Boolean recordTrace;
  }
  void close();
  List<BrowserContext> contexts();
  boolean isConnected();
  BrowserContext newContext(NewContextOptions options);
  Page newPage(NewPageOptions options);
  String version();
}

