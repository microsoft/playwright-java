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

package com.microsoft.playwright.impl;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.microsoft.playwright.*;

import java.io.IOException;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.Utils.isFunctionBody;

class BrowserContextImpl extends ChannelOwner implements BrowserContext {
  private final BrowserImpl browser;
  final List<PageImpl> pages = new ArrayList<>();
  final Router routes = new Router();
  private boolean isClosedOrClosing;
  final Map<String, Page.Binding> bindings = new HashMap<String, Page.Binding>();
  PageImpl ownerPage;
  private final ListenerCollection<EventType> listeners = new ListenerCollection<>();
  final TimeoutSettings timeoutSettings = new TimeoutSettings();

  protected BrowserContextImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    browser = (BrowserImpl) parent;
  }

  @Override
  public void addListener(EventType type, Listener<EventType> listener) {
    listeners.add(type, listener);
  }

  @Override
  public void removeListener(EventType type, Listener<EventType> listener) {
    listeners.remove(type, listener);
  }

  @Override
  public void close() {
    if (isClosedOrClosing) {
      // TODO: wait close event instead?
      throw new PlaywrightException("Already closing");
    }
    isClosedOrClosing = true;
    sendMessage("close");
  }

  @Override
  public void addCookies(List<AddCookie> cookies) {
    JsonObject params = new JsonObject();
    params.add("cookies", new Gson().toJsonTree(cookies));
    sendMessage("addCookies", params);
  }

  @Override
  public void addInitScript(String script, Object arg) {
    // TODO: serialize arg
    JsonObject params = new JsonObject();
    if (isFunctionBody(script)) {
      script = "(" + script + ")()";
    }
    params.addProperty("source", script);
    sendMessage("addInitScript", params);
  }

  @Override
  public BrowserImpl browser() {
    return browser;
  }

  @Override
  public void clearCookies() {
    sendMessage("clearCookies");
  }

  @Override
  public void clearPermissions() {
    sendMessage("clearPermissions");
  }

  private static class SameSiteAdapter extends TypeAdapter<SameSite> {
    @Override
    public void write(JsonWriter out, SameSite value) throws IOException {
      String stringValue;
      switch (value) {
        case STRICT:
          stringValue = "Strict";
          break;
        case LAX:
          stringValue = "Lax";
          break;
        case NONE:
          stringValue = "None";
          break;
        default:
          throw new PlaywrightException("Unexpected value: " + value);
      }
      out.value(stringValue);
    }

    @Override
    public SameSite read(JsonReader in) throws IOException {
      String value = in.nextString();
      return SameSite.valueOf(value.toUpperCase());
    }
  }

  @Override
  public List<Cookie> cookies(List<String> urls) {
    JsonObject params = new JsonObject();
    if (urls == null) {
      urls = Collections.emptyList();
    }
    params.add("urls", new Gson().toJsonTree(urls));
    JsonObject json = sendMessage("cookies", params).getAsJsonObject();
    Gson gson = new GsonBuilder().registerTypeAdapter(SameSite.class, new SameSiteAdapter().nullSafe()).create();
    Cookie[] cookies = gson.fromJson(json.getAsJsonArray("cookies"), Cookie[].class);
    return Arrays.asList(cookies);
  }

  @Override
  public void exposeBinding(String name, Page.Binding playwrightBinding, ExposeBindingOptions options) {
    if (bindings.containsKey(name)) {
      throw new PlaywrightException("Function \"" + name + "\" has been already registered");
    }
    for (PageImpl page : pages) {
      if (page.bindings.containsKey(name)) {
        throw new PlaywrightException("Function \"" + name + "\" has been already registered in one of the pages");
      }
    }
    bindings.put(name, playwrightBinding);

    JsonObject params = new JsonObject();
    params.addProperty("name", name);
    if (options != null && options.handle != null && options.handle) {
      params.addProperty("needsHandle", true);
    }
    sendMessage("exposeBinding", params);
  }

  @Override
  public void exposeFunction(String name, Page.Function playwrightFunction) {
    exposeBinding(name, (Page.Binding.Source source, Object... args) -> playwrightFunction.call(args));
  }

  @Override
  public void grantPermissions(List<String> permissions, GrantPermissionsOptions options) {

  }

  @Override
  public PageImpl newPage() {
    if (ownerPage != null) {
      throw new PlaywrightException("Please use browser.newContext()");
    }
    JsonObject json = sendMessage("newPage").getAsJsonObject();
    return connection.getExistingObject(json.getAsJsonObject("page").get("guid").getAsString());
  }

  @Override
  public List<Page> pages() {
    return new ArrayList<>(pages);
  }

  @Override
  public void route(String url, BiConsumer<Route, Request> handler) {
    route(new UrlMatcher(url), handler);
  }

  @Override
  public void route(Pattern url, BiConsumer<Route, Request> handler) {
    route(new UrlMatcher(url), handler);
  }

  @Override
  public void route(Predicate<String> url, BiConsumer<Route, Request> handler) {
    route(new UrlMatcher(url), handler);
  }

  private void route(UrlMatcher matcher, BiConsumer<Route, Request> handler) {
    routes.add(matcher, handler);
    if (routes.size() == 1) {
      JsonObject params = new JsonObject();
      params.addProperty("enabled", true);
      sendMessage("setNetworkInterceptionEnabled", params);
    }
  }

  @Override
  public void setDefaultNavigationTimeout(int timeout) {
    timeoutSettings.setDefaultNavigationTimeout(timeout);
    JsonObject params = new JsonObject();
    params.addProperty("timeout", timeout);
    sendMessage("setDefaultNavigationTimeoutNoReply", params);
  }

  @Override
  public void setDefaultTimeout(int timeout) {
    timeoutSettings.setDefaultTimeout(timeout);
    JsonObject params = new JsonObject();
    params.addProperty("timeout", timeout);
    sendMessage("setDefaultTimeoutNoReply", params);
  }

  @Override
  public void setExtraHTTPHeaders(Map<String, String> headers) {
    JsonObject params = new JsonObject();
    JsonArray jsonHeaders = new JsonArray();
    for (Map.Entry<String, String> e : headers.entrySet()) {
      JsonObject header = new JsonObject();
      header.addProperty("name", e.getKey());
      header.addProperty("value", e.getValue());
      jsonHeaders.add(header);
    }
    params.add("headers", jsonHeaders);
    sendMessage("setExtraHTTPHeaders", params);
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
    unroute(new UrlMatcher(url), handler);
  }

  @Override
  public void unroute(Pattern url, BiConsumer<Route, Request> handler) {
    unroute(new UrlMatcher(url), handler);
  }

  @Override
  public void unroute(Predicate<String> url, BiConsumer<Route, Request> handler) {
    unroute(new UrlMatcher(url), handler);
  }

  @Override
  public Deferred<Event<EventType>> waitForEvent(EventType event, WaitForEventOptions options) {
    if (options == null) {
      options = new WaitForEventOptions();
    }
    List<Waitable<Event<EventType>>> waitables = new ArrayList<>();
    waitables.add(new WaitableEvent<>(listeners, event, options.predicate));
    waitables.add(timeoutSettings.createWaitable(options.timeout));
    return toDeferred(new WaitableRace<>(waitables));
  }

  private void unroute(UrlMatcher matcher, BiConsumer<Route, Request> handler) {
    routes.remove(matcher, handler);
    if (routes.size() == 0) {
      JsonObject params = new JsonObject();
      params.addProperty("enabled", false);
      sendMessage("setNetworkInterceptionEnabled", params);
    }
  }

  @Override
  protected void handleEvent(String event, JsonObject params) {
    if ("route".equals(event)) {
      Route route = connection.getExistingObject(params.getAsJsonObject("route").get("guid").getAsString());
      Request request = connection.getExistingObject(params.getAsJsonObject("request").get("guid").getAsString());
      boolean handled = routes.handle(route, request);
      if (!handled) {
        route.continue_();
      }
    } else if ("page".equals(event)) {
      PageImpl page = connection.getExistingObject(params.getAsJsonObject("page").get("guid").getAsString());
      listeners.notify(EventType.PAGE, page);
      pages.add(page);
    } else if ("bindingCall".equals(event)) {
      BindingCall bindingCall = connection.getExistingObject(params.getAsJsonObject("binding").get("guid").getAsString());
      Page.Binding binding = bindings.get(bindingCall.name());
      if (binding != null) {
        bindingCall.call(binding);
      }
    } else if ("close".equals(event)) {
      isClosedOrClosing = true;
      if (browser != null) {
        browser.contexts.remove(this);
      }
    }
  }
}
