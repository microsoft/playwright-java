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
import java.util.function.Predicate;
import java.util.regex.Pattern;

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

  class WaitForEventOptions {
    public Integer timeout;
    public Predicate<Event<EventType>> predicate;
    public WaitForEventOptions withTimeout(int millis) {
      timeout = millis;
      return this;
    }
    public WaitForEventOptions withPredicate(Predicate<Event<EventType>> predicate) {
      this.predicate = predicate;
      return this;
    }
  }

  enum EventType {
    PAGE,
  }

  void addListener(EventType type, Listener<EventType> listener);
  void removeListener(EventType type, Listener<EventType> listener);
  class ExposeBindingOptions {
    public Boolean handle;

    public ExposeBindingOptions withHandle(Boolean handle) {
      this.handle = handle;
      return this;
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
  default void exposeBinding(String name, Page.Binding playwrightBinding) {
    exposeBinding(name, playwrightBinding, null);
  }
  void exposeBinding(String name, Page.Binding playwrightBinding, ExposeBindingOptions options);
  void exposeFunction(String name, Page.Function playwrightFunction);
  default void grantPermissions(List<String> permissions) {
    grantPermissions(permissions, null);
  }
  void grantPermissions(List<String> permissions, GrantPermissionsOptions options);
  Page newPage();
  List<Page> pages();
  void route(String url, BiConsumer<Route, Request> handler);
  void route(Pattern url, BiConsumer<Route, Request> handler);
  void route(Predicate<String> url, BiConsumer<Route, Request> handler);
  void setDefaultNavigationTimeout(int timeout);
  void setDefaultTimeout(int timeout);
  void setExtraHTTPHeaders(Map<String, String> headers);
  void setGeolocation(Geolocation geolocation);
  void setHTTPCredentials(String username, String password);
  void setOffline(boolean offline);
  default void unroute(String url) { unroute(url, null); }
  default void unroute(Pattern url) { unroute(url, null); }
  default void unroute(Predicate<String> url) { unroute(url, null); }
  void unroute(String url, BiConsumer<Route, Request> handler);
  void unroute(Pattern url, BiConsumer<Route, Request> handler);
  void unroute(Predicate<String> url, BiConsumer<Route, Request> handler);
  default Deferred<Event<EventType>> waitForEvent(EventType event) {
    return waitForEvent(event, (WaitForEventOptions) null);
  }
  default Deferred<Event<EventType>> waitForEvent(EventType event, Predicate<Event<EventType>> predicate) {
    WaitForEventOptions options = new WaitForEventOptions();
    options.predicate = predicate;
    return waitForEvent(event, options);
  }
  Deferred<Event<EventType>> waitForEvent(EventType event, WaitForEventOptions options);
}

