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

public interface ChromiumBrowserContext {
  class GrantPermissionsOptions {
    String origin;

    public GrantPermissionsOptions withOrigin(String origin) {
      this.origin = origin;
      return this;
    }
  }
  List<Page> backgroundPages();
  CDPSession newCDPSession(Page page);
  List<Worker> serviceWorkers();
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
  void setGeolocation(Object geolocation);
  void setHTTPCredentials(Object httpCredentials);
  void setOffline(boolean offline);
  void unroute(String url, BiConsumer<Route, Request> handler);
  Object waitForEvent(String event, String optionsOrPredicate);
}

