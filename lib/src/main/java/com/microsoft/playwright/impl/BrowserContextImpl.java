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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.microsoft.playwright.impl.Utils.globToRegex;

class BrowserContextImpl extends ChannelOwner implements BrowserContext {
  private List<RouteInfo> routes = new ArrayList<>();
  private class RouteInfo {
    private String url;
    private BiConsumer<Route, Request> handler;
    private final Pattern pattern;

    public RouteInfo(String url, BiConsumer<Route, Request> handler) {
      this.url = url;
      this.handler = handler;
      pattern = Pattern.compile(globToRegex(url));
    }
  }

  protected BrowserContextImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  @Override
  public void close() {

  }

  @Override
  public void addCookies(List<Object> cookies) {

  }

  @Override
  public void addInitScript(String script, Object arg) {

  }

  @Override
  public Browser browser() {
    return null;
  }

  @Override
  public void clearCookies() {

  }

  @Override
  public void clearPermissions() {

  }

  @Override
  public List<Object> cookies(String urls) {
    return null;
  }

  @Override
  public void exposeBinding(String name, String playwrightBinding) {

  }

  @Override
  public void exposeFunction(String name, String playwrightFunction) {

  }

  @Override
  public void grantPermissions(List<String> permissions, GrantPermissionsOptions options) {

  }

  @Override
  public PageImpl newPage() {
    JsonObject params = new JsonObject();
    JsonElement result = sendMessage("newPage", params);
    return connection.getExistingObject(result.getAsJsonObject().getAsJsonObject("page").get("guid").getAsString());
  }

  @Override
  public List<Page> pages() {
    return null;
  }

  @Override
  public void route(String url, BiConsumer<Route, Request> handler) {
    routes.add(new RouteInfo(url, handler));
    if (routes.size() == 1) {
      JsonObject params = new JsonObject();
      params.addProperty("enabled", true);
      sendMessage("setNetworkInterceptionEnabled", params);
    }
  }

  @Override
  public void setDefaultNavigationTimeout(int timeout) {

  }

  @Override
  public void setDefaultTimeout(int timeout) {

  }

  @Override
  public void setExtraHTTPHeaders(Map<String, String> headers) {

  }

  @Override
  public void setGeolocation(Geolocation geolocation) {

  }

  @Override
  public void setHTTPCredentials(String username, String password) {

  }

  @Override
  public void setOffline(boolean offline) {
    JsonObject params = new JsonObject();
    params.addProperty("offline", offline);
    sendMessage("setOffline", params);
  }

  @Override
  public void unroute(String url, BiConsumer<Route, Request> handler) {
    routes = routes.stream()
      .filter(info -> !info.url.equals(url) || (handler != null && info.handler != handler))
      .collect(Collectors.toList());
    if (routes.isEmpty()) {
      JsonObject params = new JsonObject();
      params.addProperty("enabled", false);
      sendMessage("setNetworkInterceptionEnabled", params);
    }
  }

  @Override
  public Object waitForEvent(String event, String optionsOrPredicate) {
    return null;
  }

  @Override
  public Deferred<Page> waitForPage() {
    Supplier<JsonObject> pageSupplier = waitForProtocolEvent("page");
    return () -> {
      JsonObject params = pageSupplier.get();
      String guid = params.getAsJsonObject("page").get("guid").getAsString();
      return connection.getExistingObject(guid);
    };
  }

  @Override
  protected void handleEvent(String event, JsonObject params) {
    if ("route".equals(event)) {
      Route route = connection.getExistingObject(params.getAsJsonObject("route").get("guid").getAsString());
      Request request = connection.getExistingObject(params.getAsJsonObject("request").get("guid").getAsString());
      for (RouteInfo info : routes) {
        if (info.pattern.matcher(request.url()).find()) {
          info.handler.accept(route, request);
        }
      }
      route.continue_();
    }
  }
}
