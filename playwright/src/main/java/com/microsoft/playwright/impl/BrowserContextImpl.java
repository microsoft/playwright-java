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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.Utils.isFunctionBody;
import static com.microsoft.playwright.impl.Utils.isSafeCloseError;

class BrowserContextImpl extends ChannelOwner implements BrowserContext {
  private final BrowserImpl browser;
  final List<PageImpl> pages = new ArrayList<>();
  final Router routes = new Router();
  private boolean isClosedOrClosing;
  final Map<String, Page.Binding> bindings = new HashMap<>();
  PageImpl ownerPage;
  private final ListenerCollection<EventType> listeners = new ListenerCollection<>();
  final TimeoutSettings timeoutSettings = new TimeoutSettings();
  Path videosDir;

  enum EventType {
    CLOSE,
    PAGE,
  }

  BrowserContextImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    if (parent instanceof BrowserImpl) {
      browser = (BrowserImpl) parent;
    } else {
      browser = null;
    }
  }

  @Override
  public void onClose(Consumer<BrowserContext> handler) {
    listeners.add(EventType.CLOSE, handler);
  }

  @Override
  public void offClose(Consumer<BrowserContext> handler) {
    listeners.remove(EventType.CLOSE, handler);
  }

  @Override
  public void onPage(Consumer<Page> handler) {
    listeners.add(EventType.PAGE, handler);
  }

  @Override
  public void offPage(Consumer<Page> handler) {
    listeners.remove(EventType.PAGE, handler);
  }

  private <T> T waitForEventWithTimeout(EventType eventType, Runnable code, Double timeout) {
    List<Waitable<T>> waitables = new ArrayList<>();
    waitables.add(new WaitableEvent<>(listeners, eventType));
    waitables.add(new WaitableContextClose<>());
    waitables.add(timeoutSettings.createWaitable(timeout));
    return runUntil(code, new WaitableRace<>(waitables));
  }

  @Override
  public Page waitForPage(Runnable code, WaitForPageOptions options) {
    if (options == null) {
      options = new WaitForPageOptions();
    }
    return waitForEventWithTimeout(EventType.PAGE, code, options.timeout);
  }

  @Override
  public void close() {
    withLogging("BrowserContext.close", () -> closeImpl());
  }
  private void closeImpl() {
    if (isClosedOrClosing) {
      return;
    }
    isClosedOrClosing = true;
    try {
      sendMessage("close");
    } catch (PlaywrightException e) {
      if (!isSafeCloseError(e)) {
        throw e;
      }
    }
  }

  @Override
  public void addCookies(List<AddCookie> cookies) {
    withLogging("BrowserContext.addCookies", () -> {
      JsonObject params = new JsonObject();
      params.add("cookies", gson().toJsonTree(cookies));
      sendMessage("addCookies", params);
    });
  }

  @Override
  public void addInitScript(String script) {
    withLogging("BrowserContext.addInitScript", () -> addInitScriptImpl(script));
  }
  private void addInitScriptImpl(String script) {
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
    withLogging("BrowserContext.clearCookies", () -> sendMessage("clearCookies"));
  }

  @Override
  public void clearPermissions() {
    withLogging("BrowserContext.clearPermissions", () -> sendMessage("clearPermissions"));
  }

  @Override
  public List<Cookie> cookies(List<String> urls) {
    return withLogging("BrowserContext.cookies", () -> cookiesImpl(urls));
  }

  private List<Cookie> cookiesImpl(List<String> urls) {
    JsonObject params = new JsonObject();
    if (urls == null) {
      urls = Collections.emptyList();
    }
    params.add("urls", gson().toJsonTree(urls));
    JsonObject json = sendMessage("cookies", params).getAsJsonObject();
    Cookie[] cookies = gson().fromJson(json.getAsJsonArray("cookies"), Cookie[].class);
    return Arrays.asList(cookies);
  }

  @Override
  public void exposeBinding(String name, Page.Binding playwrightBinding, ExposeBindingOptions options) {
    withLogging("BrowserContext.exposeBinding", () -> exposeBindingImpl(name, playwrightBinding, options));
  }

  private void exposeBindingImpl(String name, Page.Binding playwrightBinding, ExposeBindingOptions options) {
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
    withLogging("BrowserContext.exposeFunction",
      () -> exposeBindingImpl(name, (Page.Binding.Source source, Object... args) -> playwrightFunction.call(args), null));
  }

  @Override
  public void grantPermissions(List<String> permissions, GrantPermissionsOptions options) {
    withLogging("BrowserContext.grantPermissions", () -> grantPermissionsImpl(permissions, options));
  }

  private void grantPermissionsImpl(List<String> permissions, GrantPermissionsOptions options) {
    if (options == null) {
      options = new GrantPermissionsOptions();
    }
    if (permissions == null) {
      permissions = Collections.emptyList();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.add("permissions", gson().toJsonTree(permissions));
    sendMessage("grantPermissions", params);
  }

  @Override
  public PageImpl newPage() {
    return withLogging("BrowserContext.newPage", () -> newPageImpl());
  }

  private PageImpl newPageImpl() {
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
  public void route(String url, Consumer<Route> handler) {
    route(new UrlMatcher(url), handler);
  }

  @Override
  public void route(Pattern url, Consumer<Route> handler) {
    route(new UrlMatcher(url), handler);
  }

  @Override
  public void route(Predicate<String> url, Consumer<Route> handler) {
    route(new UrlMatcher(url), handler);
  }

  private void route(UrlMatcher matcher, Consumer<Route> handler) {
    withLogging("BrowserContext.route", () -> {
      routes.add(matcher, handler);
      if (routes.size() == 1) {
        JsonObject params = new JsonObject();
        params.addProperty("enabled", true);
        sendMessage("setNetworkInterceptionEnabled", params);
      }
    });
  }

  @Override
  public void setDefaultNavigationTimeout(double timeout) {
    withLogging("BrowserContext.setDefaultNavigationTimeout", () -> {
      timeoutSettings.setDefaultNavigationTimeout(timeout);
      JsonObject params = new JsonObject();
      params.addProperty("timeout", timeout);
      sendMessage("setDefaultNavigationTimeoutNoReply", params);
    });
  }

  @Override
  public void setDefaultTimeout(double timeout) {
    withLogging("BrowserContext.setDefaultTimeout", () -> {
      timeoutSettings.setDefaultTimeout(timeout);
      JsonObject params = new JsonObject();
      params.addProperty("timeout", timeout);
      sendMessage("setDefaultTimeoutNoReply", params);
    });
  }

  @Override
  public void setExtraHTTPHeaders(Map<String, String> headers) {
    withLogging("BrowserContext.setExtraHTTPHeaders", () -> {
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
    });
  }

  @Override
  public void setGeolocation(Geolocation geolocation) {
    withLogging("BrowserContext.setGeolocation", () -> {
      JsonObject params = new JsonObject();
      if (geolocation != null) {
        params.add("geolocation", gson().toJsonTree(geolocation));
      }
      sendMessage("setGeolocation", params);
    });
  }

  @Override
  public void setOffline(boolean offline) {
    withLogging("BrowserContext.setOffline", () -> {
      JsonObject params = new JsonObject();
      params.addProperty("offline", offline);
      sendMessage("setOffline", params);
    });
  }

  @Override
  public StorageState storageState(StorageStateOptions options) {
    return withLogging("BrowserContext.storageState", () -> {
      JsonElement json = sendMessage("storageState");
      StorageState storageState = gson().fromJson(json, StorageState.class);
      if (options != null && options.path != null) {
        Utils.writeToFile(json.toString().getBytes(StandardCharsets.UTF_8), options.path);
      }
      return storageState;
    });
  }

  @Override
  public void unroute(String url, Consumer<Route> handler) {
    unroute(new UrlMatcher(url), handler);
  }

  @Override
  public void unroute(Pattern url, Consumer<Route> handler) {
    unroute(new UrlMatcher(url), handler);
  }

  @Override
  public void unroute(Predicate<String> url, Consumer<Route> handler) {
    unroute(new UrlMatcher(url), handler);
  }

  private class WaitableContextClose<R> extends WaitableEvent<EventType, R> {
    WaitableContextClose() {
      super(BrowserContextImpl.this.listeners, EventType.CLOSE);
    }

    @Override
    public R get() {
      throw new PlaywrightException("Context closed");
    }
  }

  private void unroute(UrlMatcher matcher, Consumer<Route> handler) {
    withLogging("BrowserContext.unroute", () -> {
      routes.remove(matcher, handler);
      if (routes.size() == 0) {
        JsonObject params = new JsonObject();
        params.addProperty("enabled", false);
        sendMessage("setNetworkInterceptionEnabled", params);
      }
    });
  }

  @Override
  protected void handleEvent(String event, JsonObject params) {
    if ("route".equals(event)) {
      Route route = connection.getExistingObject(params.getAsJsonObject("route").get("guid").getAsString());
      boolean handled = routes.handle(route);
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
      listeners.notify(EventType.CLOSE, this);
    }
  }
}
