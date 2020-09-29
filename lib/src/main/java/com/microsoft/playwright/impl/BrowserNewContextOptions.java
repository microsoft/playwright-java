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
  public Boolean noDefaultViewport;
  public static class Viewport {
    // TODO: int is preferred here
    public int width;
    public int height;
  }
  public Viewport viewport;
  public Boolean ignoreHTTPSErrors;
  public Boolean javaScriptEnabled;
  public Boolean bypassCSP;
  public String userAgent;
  public String locale;
  public String timezoneId;
  public static class Geolocation {
    // TODO: can we use int somehow?
    public Double longitude;
    public Double latitude;
    public Double accuracy;
  };
  public Geolocation geolocation;
  public String[] permissions;
  public LinkedHashMap<String, String> extraHTTPHeaders;
  public Boolean offline;
  public static class HttpCredentials {
    public String username;
    public String password;
  }
  public HttpCredentials httpCredentials;
  public Integer deviceScaleFactor;
  public Boolean isMobile;
  public Boolean hasTouch;
  public enum ColorScheme {
    // TODO: noPreference => no-preference
    dark, light, noPreference
  }
  public ColorScheme colorScheme;
  public Boolean acceptDownloads;
  public Boolean _recordVideos;
  public static  class _VideoSize {
    public int width;
    public int height;
  }
  public _VideoSize _videoSize;
}
