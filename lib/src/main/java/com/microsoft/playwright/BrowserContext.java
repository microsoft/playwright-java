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
  class HTTPCredentials {
    private final String username;
    private final String password;

    public HTTPCredentials(String username, String password) {
      this.username = username;
      this.password = password;
    }

    public String username() {
      return username;
    }

    public String password() {
      return password;
    }
  }

  class GrantPermissionsOptions {
    public String origin;

    public GrantPermissionsOptions withOrigin(String origin) {
      this.origin = origin;
      return this;
    }
  }
  class Geolocation {
    public int latitude;
    public int longitude;
    public Integer accuracy;

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
  Deferred<Page> waitForPage();
  void close();
  void addCookies(List<Object> cookies);
  default void addInitScript(String script) {
    addInitScript(script, null);
  }
  void addInitScript(String script, Object arg);
  Browser browser();
  void clearCookies();
  void clearPermissions();
  default List<Object> cookies() {
    return cookies(null);
  }
  List<Object> cookies(String urls);
  void exposeBinding(String name, String playwrightBinding);
  void exposeFunction(String name, String playwrightFunction);
  default void grantPermissions(List<String> permissions) {
    grantPermissions(permissions, null);
  }
  void grantPermissions(List<String> permissions, GrantPermissionsOptions options);
  Page newPage();
  List<Page> pages();
  void route(String url, BiConsumer<Route, Request> handler);
  void setDefaultNavigationTimeout(int timeout);
  void setDefaultTimeout(int timeout);
  void setExtraHTTPHeaders(Map<String, String> headers);
  void setGeolocation(Geolocation geolocation);
  void setHTTPCredentials(String username, String password);
  void setOffline(boolean offline);
  default void unroute(String url) {
    unroute(url, null);
  }
  void unroute(String url, BiConsumer<Route, Request> handler);
  default Object waitForEvent(String event) {
    return waitForEvent(event, null);
  }
  Object waitForEvent(String event, String optionsOrPredicate);
}

