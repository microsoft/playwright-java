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

package com.microsoft.playwright.impl;

import java.util.LinkedHashMap;

public class BrowserNewContextOptions {
  Boolean noDefaultViewport;
  public static class Viewport {
    // TODO: int is preferred here
    int width;
    int height;
  }
  Viewport viewport;
  Boolean ignoreHTTPSErrors;
  Boolean javaScriptEnabled;
  Boolean bypassCSP;
  String userAgent;
  String locale;
  String timezoneId;
  public static class Geolocation {
    // TODO: can we use int somehow?
    Double longitude;
    Double latitude;
    Double accuracy;
  };
  Geolocation geolocation;
  String[] permissions;
  LinkedHashMap<String, String> extraHTTPHeaders;
  Boolean offline;
  public static class HttpCredentials {
    String username;
    String password;
  }
  HttpCredentials httpCredentials;
  Integer deviceScaleFactor;
  Boolean isMobile;
  Boolean hasTouch;
  enum ColorScheme {
    // TODO: noPreference => no-preference
    dark, light, noPreference
  }
  ColorScheme colorScheme;
  Boolean acceptDownloads;
  Boolean _recordVideos;
  public static  class _VideoSize {
    int width;
    int height;
  }
  _VideoSize _videoSize;
}
