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

public interface WebKitBrowser {
  class NewContextOptions {
    public enum ColorScheme { DARK, LIGHT, NO_PREFERENCE}
    public class Viewport {
      public int width;
      public int height;

      Viewport() {
      }
      public NewContextOptions done() {
        return NewContextOptions.this;
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
      public double latitude;
      public double longitude;
      public double accuracy;

      Geolocation() {
      }
      public NewContextOptions done() {
        return NewContextOptions.this;
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
      public String username;
      public String password;

      HttpCredentials() {
      }
      public NewContextOptions done() {
        return NewContextOptions.this;
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
      public int width;
      public int height;

      VideoSize() {
      }
      public NewContextOptions done() {
        return NewContextOptions.this;
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
    public Boolean acceptDownloads;
    public Boolean ignoreHTTPSErrors;
    public Boolean bypassCSP;
    public Viewport viewport;
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
    public HttpCredentials httpCredentials;
    public ColorScheme colorScheme;
    public Logger logger;
    public String relativeArtifactsPath;
    public Boolean recordVideos;
    public VideoSize videoSize;
    public Boolean recordTrace;

    public NewContextOptions withAcceptDownloads(Boolean acceptDownloads) {
      this.acceptDownloads = acceptDownloads;
      return this;
    }
    public NewContextOptions withIgnoreHTTPSErrors(Boolean ignoreHTTPSErrors) {
      this.ignoreHTTPSErrors = ignoreHTTPSErrors;
      return this;
    }
    public NewContextOptions withBypassCSP(Boolean bypassCSP) {
      this.bypassCSP = bypassCSP;
      return this;
    }
    public Viewport setViewport() {
      this.viewport = new Viewport();
      return this.viewport;
    }
    public NewContextOptions withUserAgent(String userAgent) {
      this.userAgent = userAgent;
      return this;
    }
    public NewContextOptions withDeviceScaleFactor(Integer deviceScaleFactor) {
      this.deviceScaleFactor = deviceScaleFactor;
      return this;
    }
    public NewContextOptions withIsMobile(Boolean isMobile) {
      this.isMobile = isMobile;
      return this;
    }
    public NewContextOptions withHasTouch(Boolean hasTouch) {
      this.hasTouch = hasTouch;
      return this;
    }
    public NewContextOptions withJavaScriptEnabled(Boolean javaScriptEnabled) {
      this.javaScriptEnabled = javaScriptEnabled;
      return this;
    }
    public NewContextOptions withTimezoneId(String timezoneId) {
      this.timezoneId = timezoneId;
      return this;
    }
    public Geolocation setGeolocation() {
      this.geolocation = new Geolocation();
      return this.geolocation;
    }
    public NewContextOptions withLocale(String locale) {
      this.locale = locale;
      return this;
    }
    public NewContextOptions withPermissions(List<String> permissions) {
      this.permissions = permissions;
      return this;
    }
    public NewContextOptions withExtraHTTPHeaders(Map<String, String> extraHTTPHeaders) {
      this.extraHTTPHeaders = extraHTTPHeaders;
      return this;
    }
    public NewContextOptions withOffline(Boolean offline) {
      this.offline = offline;
      return this;
    }
    public HttpCredentials setHttpCredentials() {
      this.httpCredentials = new HttpCredentials();
      return this.httpCredentials;
    }
    public NewContextOptions withColorScheme(ColorScheme colorScheme) {
      this.colorScheme = colorScheme;
      return this;
    }
    public NewContextOptions withLogger(Logger logger) {
      this.logger = logger;
      return this;
    }
    public NewContextOptions withRelativeArtifactsPath(String relativeArtifactsPath) {
      this.relativeArtifactsPath = relativeArtifactsPath;
      return this;
    }
    public NewContextOptions withRecordVideos(Boolean recordVideos) {
      this.recordVideos = recordVideos;
      return this;
    }
    public VideoSize setVideoSize() {
      this.videoSize = new VideoSize();
      return this.videoSize;
    }
    public NewContextOptions withRecordTrace(Boolean recordTrace) {
      this.recordTrace = recordTrace;
      return this;
    }
  }
  class NewPageOptions {
    public enum ColorScheme { DARK, LIGHT, NO_PREFERENCE}
    public class Viewport {
      public int width;
      public int height;

      Viewport() {
      }
      public NewPageOptions done() {
        return NewPageOptions.this;
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
      public double latitude;
      public double longitude;
      public double accuracy;

      Geolocation() {
      }
      public NewPageOptions done() {
        return NewPageOptions.this;
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
      public String username;
      public String password;

      HttpCredentials() {
      }
      public NewPageOptions done() {
        return NewPageOptions.this;
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
      public int width;
      public int height;

      VideoSize() {
      }
      public NewPageOptions done() {
        return NewPageOptions.this;
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
    public Boolean acceptDownloads;
    public Boolean ignoreHTTPSErrors;
    public Boolean bypassCSP;
    public Viewport viewport;
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
    public HttpCredentials httpCredentials;
    public ColorScheme colorScheme;
    public Logger logger;
    public String relativeArtifactsPath;
    public Boolean recordVideos;
    public VideoSize videoSize;
    public Boolean recordTrace;

    public NewPageOptions withAcceptDownloads(Boolean acceptDownloads) {
      this.acceptDownloads = acceptDownloads;
      return this;
    }
    public NewPageOptions withIgnoreHTTPSErrors(Boolean ignoreHTTPSErrors) {
      this.ignoreHTTPSErrors = ignoreHTTPSErrors;
      return this;
    }
    public NewPageOptions withBypassCSP(Boolean bypassCSP) {
      this.bypassCSP = bypassCSP;
      return this;
    }
    public Viewport setViewport() {
      this.viewport = new Viewport();
      return this.viewport;
    }
    public NewPageOptions withUserAgent(String userAgent) {
      this.userAgent = userAgent;
      return this;
    }
    public NewPageOptions withDeviceScaleFactor(Integer deviceScaleFactor) {
      this.deviceScaleFactor = deviceScaleFactor;
      return this;
    }
    public NewPageOptions withIsMobile(Boolean isMobile) {
      this.isMobile = isMobile;
      return this;
    }
    public NewPageOptions withHasTouch(Boolean hasTouch) {
      this.hasTouch = hasTouch;
      return this;
    }
    public NewPageOptions withJavaScriptEnabled(Boolean javaScriptEnabled) {
      this.javaScriptEnabled = javaScriptEnabled;
      return this;
    }
    public NewPageOptions withTimezoneId(String timezoneId) {
      this.timezoneId = timezoneId;
      return this;
    }
    public Geolocation setGeolocation() {
      this.geolocation = new Geolocation();
      return this.geolocation;
    }
    public NewPageOptions withLocale(String locale) {
      this.locale = locale;
      return this;
    }
    public NewPageOptions withPermissions(List<String> permissions) {
      this.permissions = permissions;
      return this;
    }
    public NewPageOptions withExtraHTTPHeaders(Map<String, String> extraHTTPHeaders) {
      this.extraHTTPHeaders = extraHTTPHeaders;
      return this;
    }
    public NewPageOptions withOffline(Boolean offline) {
      this.offline = offline;
      return this;
    }
    public HttpCredentials setHttpCredentials() {
      this.httpCredentials = new HttpCredentials();
      return this.httpCredentials;
    }
    public NewPageOptions withColorScheme(ColorScheme colorScheme) {
      this.colorScheme = colorScheme;
      return this;
    }
    public NewPageOptions withLogger(Logger logger) {
      this.logger = logger;
      return this;
    }
    public NewPageOptions withRelativeArtifactsPath(String relativeArtifactsPath) {
      this.relativeArtifactsPath = relativeArtifactsPath;
      return this;
    }
    public NewPageOptions withRecordVideos(Boolean recordVideos) {
      this.recordVideos = recordVideos;
      return this;
    }
    public VideoSize setVideoSize() {
      this.videoSize = new VideoSize();
      return this.videoSize;
    }
    public NewPageOptions withRecordTrace(Boolean recordTrace) {
      this.recordTrace = recordTrace;
      return this;
    }
  }
  void close();
  List<BrowserContext> contexts();
  boolean isConnected();
  default BrowserContext newContext() {
    return newContext(null);
  }
  BrowserContext newContext(NewContextOptions options);
  default Page newPage() {
    return newPage(null);
  }
  Page newPage(NewPageOptions options);
  String version();
}

