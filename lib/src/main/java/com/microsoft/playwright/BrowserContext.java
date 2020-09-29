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

public interface BrowserContext {
  class GrantPermissionsOptions {
    String origin;

    public GrantPermissionsOptions withOrigin(String origin) {
      this.origin = origin;
      return this;
    }
  }
  class Geolocation {
    int latitude;
    int longitude;
    Integer accuracy;

    public Geolocation withLatitude(int latitude) {
      this.latitude = latitude;
      return this;
    }
    public Geolocation withLongitude(int longitude) {
      this.longitude = longitude;
      return this;
    }
    public Geolocation withAccuracy(Integer accuracy) {
      this.accuracy = accuracy;
      return this;
    }
  }
  class HTTPCredentials {
    String username;
    String password;

    public HTTPCredentials withUsername(String username) {
      this.username = username;
      return this;
    }
    public HTTPCredentials withPassword(String password) {
      this.password = password;
      return this;
    }
  }
  void close();
  void addCookies(List<Object> cookies);
  void addInitScript(String script, Object arg);
  Browser browser();
  void clearCookies();
  void clearPermissions();
  List<Object> cookies(String urls);
  void exposeBinding(String name, String playwrightBinding);
  void exposeFunction(String name, String playwrightFunction);
  void grantPermissions(List<String> permissions, GrantPermissionsOptions options);
  Page newPage();
  List<Page> pages();
  void route(String url, BiConsumer<Route, Request> handler);
  void setDefaultNavigationTimeout(int timeout);
  void setDefaultTimeout(int timeout);
  void setExtraHTTPHeaders(Map<String, String> headers);
  void setGeolocation(Geolocation geolocation);
  void setHTTPCredentials(HTTPCredentials httpCredentials);
  void setOffline(boolean offline);
  void unroute(String url, BiConsumer<Route, Request> handler);
  Object waitForEvent(String event, String optionsOrPredicate);
}

