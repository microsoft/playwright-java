/*
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
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public interface BrowserContext {
  enum SameSite { STRICT, LAX, NONE }

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
    CLOSE,
    PAGE,
  }

  void addListener(EventType type, Listener<EventType> listener);
  void removeListener(EventType type, Listener<EventType> listener);
  class AddCookie {
    public String name;
    public String value;
    public String url;
    public String domain;
    public String path;
    public Long expires;
    public Boolean httpOnly;
    public Boolean secure;
    public SameSite sameSite;

    public AddCookie withName(String name) {
      this.name = name;
      return this;
    }
    public AddCookie withValue(String value) {
      this.value = value;
      return this;
    }
    public AddCookie withUrl(String url) {
      this.url = url;
      return this;
    }
    public AddCookie withDomain(String domain) {
      this.domain = domain;
      return this;
    }
    public AddCookie withPath(String path) {
      this.path = path;
      return this;
    }
    public AddCookie withExpires(Long expires) {
      this.expires = expires;
      return this;
    }
    public AddCookie withHttpOnly(Boolean httpOnly) {
      this.httpOnly = httpOnly;
      return this;
    }
    public AddCookie withSecure(Boolean secure) {
      this.secure = secure;
      return this;
    }
    public AddCookie withSameSite(SameSite sameSite) {
      this.sameSite = sameSite;
      return this;
    }
  }
  class Cookie {
    private String name;
    private String value;
    private String domain;
    private String path;
    private long expires;
    private boolean httpOnly;
    private boolean secure;
    private SameSite sameSite;

    public String name() {
      return this.name;
    }
    public String value() {
      return this.value;
    }
    public String domain() {
      return this.domain;
    }
    public String path() {
      return this.path;
    }
    public long expires() {
      return this.expires;
    }
    public boolean httpOnly() {
      return this.httpOnly;
    }
    public boolean secure() {
      return this.secure;
    }
    public SameSite sameSite() {
      return this.sameSite;
    }
  }
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
  void addCookies(List<AddCookie> cookies);
  default void addInitScript(String script) {
    addInitScript(script, null);
  }
  void addInitScript(String script, Object arg);
  Browser browser();
  void clearCookies();
  void clearPermissions();
  void close();
  default List<Cookie> cookies() { return cookies((List<String>) null); }
  default List<Cookie> cookies(String url) { return cookies(Arrays.asList(url)); }
  List<Cookie> cookies(List<String> urls);
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
  void route(String url, Consumer<Route> handler);
  void route(Pattern url, Consumer<Route> handler);
  void route(Predicate<String> url, Consumer<Route> handler);
  void setDefaultNavigationTimeout(int timeout);
  void setDefaultTimeout(int timeout);
  void setExtraHTTPHeaders(Map<String, String> headers);
  void setGeolocation(Geolocation geolocation);
  void setOffline(boolean offline);
  default void unroute(String url) { unroute(url, null); }
  default void unroute(Pattern url) { unroute(url, null); }
  default void unroute(Predicate<String> url) { unroute(url, null); }
  void unroute(String url, Consumer<Route> handler);
  void unroute(Pattern url, Consumer<Route> handler);
  void unroute(Predicate<String> url, Consumer<Route> handler);
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

