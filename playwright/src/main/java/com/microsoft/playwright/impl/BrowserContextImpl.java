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
import com.microsoft.playwright.options.BindingCallback;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.FunctionCallback;
import com.microsoft.playwright.options.Geolocation;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.Utils.isSafeCloseError;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;
import static java.util.Arrays.asList;

class BrowserContextImpl extends ChannelOwner implements BrowserContext {
  private final BrowserImpl browser;
  private final TracingImpl tracing;
  private final APIRequestContextImpl request;
  final List<PageImpl> pages = new ArrayList<>();
  final Router routes = new Router();
  private boolean isClosedOrClosing;
  final Map<String, BindingCallback> bindings = new HashMap<>();
  PageImpl ownerPage;
  private final ListenerCollection<EventType> listeners = new ListenerCollection<>();
  final TimeoutSettings timeoutSettings = new TimeoutSettings();
  Path videosDir;
  URL baseUrl;
  Path recordHarPath;

  enum EventType {
    CLOSE,
    PAGE,
    REQUEST,
    REQUESTFAILED,
    REQUESTFINISHED,
    RESPONSE,
  }

  BrowserContextImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    if (parent instanceof BrowserImpl) {
      browser = (BrowserImpl) parent;
    } else {
      browser = null;
    }
    this.tracing = connection.getExistingObject(initializer.getAsJsonObject("tracing").get("guid").getAsString());
    tracing.isRemote = browser != null && browser.isRemote;
    this.request = connection.getExistingObject(initializer.getAsJsonObject("APIRequestContext").get("guid").getAsString());
  }

  void setBaseUrl(String spec) {
    try {
      this.baseUrl = new URL(spec);
    } catch (MalformedURLException e) {
      this.baseUrl = null;
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

  @Override
  public void onRequest(Consumer<Request> handler) {
    listeners.add(EventType.REQUEST, handler);
  }

  @Override
  public void offRequest(Consumer<Request> handler) {
    listeners.remove(EventType.REQUEST, handler);
  }

  @Override
  public void onRequestFailed(Consumer<Request> handler) {
    listeners.add(EventType.REQUESTFAILED, handler);
  }

  @Override
  public void offRequestFailed(Consumer<Request> handler) {
    listeners.remove(EventType.REQUESTFAILED, handler);
  }

  @Override
  public void onRequestFinished(Consumer<Request> handler) {
    listeners.add(EventType.REQUESTFINISHED, handler);
  }

  @Override
  public void offRequestFinished(Consumer<Request> handler) {
    listeners.remove(EventType.REQUESTFINISHED, handler);
  }

  @Override
  public void onResponse(Consumer<Response> handler) {
    listeners.add(EventType.RESPONSE, handler);
  }

  @Override
  public void offResponse(Consumer<Response> handler) {
    listeners.remove(EventType.RESPONSE, handler);
  }

  private <T> T waitForEventWithTimeout(EventType eventType, Runnable code, Predicate<T> predicate, Double timeout) {
    List<Waitable<T>> waitables = new ArrayList<>();
    waitables.add(new WaitableEvent<>(listeners, eventType, predicate));
    waitables.add(new WaitableContextClose<>());
    waitables.add(timeoutSettings.createWaitable(timeout));
    return runUntil(code, new WaitableRace<>(waitables));
  }

  @Override
  public Page waitForPage(WaitForPageOptions options, Runnable code) {
    return withWaitLogging("BrowserContext.close", () -> waitForPageImpl(options, code));
  }

  private Page waitForPageImpl(WaitForPageOptions options, Runnable code) {
    if (options == null) {
      options = new WaitForPageOptions();
    }
    return waitForEventWithTimeout(EventType.PAGE, code, options.predicate, options.timeout);
  }

  @Override
  public void close() {
    withLogging("BrowserContext.close", () -> closeImpl());
  }

  @Override
  public List<Cookie> cookies(String url) {
    return cookies(url == null ? new ArrayList<>() : Collections.singletonList(url));
  }

  private void closeImpl() {
    if (isClosedOrClosing) {
      return;
    }
    isClosedOrClosing = true;
    try {
      if (recordHarPath != null)  {
        JsonObject json = sendMessage("harExport").getAsJsonObject();
        ArtifactImpl artifact = connection.getExistingObject(json.getAsJsonObject("artifact").get("guid").getAsString());
        // In case of CDP connection browser is null but since the connection is established by
        // the driver it is safe to consider the artifact local.
        if (browser() != null && browser().isRemote) {
          artifact.isRemote = true;
        }
        artifact.saveAs(recordHarPath);
        artifact.delete();
      }

      sendMessage("close");
    } catch (PlaywrightException e) {
      if (!isSafeCloseError(e)) {
        throw e;
      }
    }
  }

  @Override
  public void addCookies(List<Cookie> cookies) {
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

  @Override
  public void addInitScript(Path path) {
    withLogging("BrowserContext.addInitScript", () -> {
      try {
        byte[] bytes = readAllBytes(path);
        addInitScriptImpl(new String(bytes, UTF_8));
      } catch (IOException e) {
        throw new PlaywrightException("Failed to read script from file", e);
      }
    });
  }

  private void addInitScriptImpl(String script) {
    JsonObject params = new JsonObject();
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
      urls = new ArrayList<>();
    }
    params.add("urls", gson().toJsonTree(urls));
    JsonObject json = sendMessage("cookies", params).getAsJsonObject();
    Cookie[] cookies = gson().fromJson(json.getAsJsonArray("cookies"), Cookie[].class);
    return asList(cookies);
  }

  @Override
  public void exposeBinding(String name, BindingCallback playwrightBinding, ExposeBindingOptions options) {
    withLogging("BrowserContext.exposeBinding", () -> exposeBindingImpl(name, playwrightBinding, options));
  }

  private void exposeBindingImpl(String name, BindingCallback playwrightBinding, ExposeBindingOptions options) {
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
  public void exposeFunction(String name, FunctionCallback playwrightFunction) {
    withLogging("BrowserContext.exposeFunction",
      () -> exposeBindingImpl(name, (BindingCallback.Source source, Object... args) -> playwrightFunction.call(args), null));
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
      permissions = new ArrayList<>();
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
  public APIRequestContextImpl request() {
    return request;
  }

  @Override
  public void route(String url, Consumer<Route> handler, RouteOptions options) {
    route(new UrlMatcher(this.baseUrl, url), handler, options);
  }

  @Override
  public void route(Pattern url, Consumer<Route> handler, RouteOptions options) {
    route(new UrlMatcher(url), handler, options);
  }

  @Override
  public void route(Predicate<String> url, Consumer<Route> handler, RouteOptions options) {
    route(new UrlMatcher(url), handler, options);
  }

  private void route(UrlMatcher matcher, Consumer<Route> handler, RouteOptions options) {
    withLogging("BrowserContext.route", () -> {
      routes.add(matcher, handler, options == null ? null : options.times);
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
  public String storageState(StorageStateOptions options) {
    return withLogging("BrowserContext.storageState", () -> {
      JsonElement json = sendMessage("storageState");
      String storageState = json.toString();
      if (options != null && options.path != null) {
        Utils.writeToFile(storageState.getBytes(StandardCharsets.UTF_8), options.path);
      }
      return storageState;
    });
  }

  @Override
  public TracingImpl tracing() {
    return tracing;
  }

  @Override
  public void unroute(String url, Consumer<Route> handler) {
    unroute(new UrlMatcher(this.baseUrl, url), handler);
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
      maybeDisableNetworkInterception();
    });
  }

  private void maybeDisableNetworkInterception() {
    if (routes.size() == 0) {
      JsonObject params = new JsonObject();
      params.addProperty("enabled", false);
      sendMessage("setNetworkInterceptionEnabled", params);
    }
  }

  void handleRoute(Route route) {
    boolean handled = routes.handle(route);
    if (handled) {
      maybeDisableNetworkInterception();
    } else {
      route.resume();
    }
  }

  void pause() {
    sendMessage("pause");
  }

  @Override
  protected void handleEvent(String event, JsonObject params) {
    if ("route".equals(event)) {
      Route route = connection.getExistingObject(params.getAsJsonObject("route").get("guid").getAsString());
      handleRoute(route);
    } else if ("page".equals(event)) {
      PageImpl page = connection.getExistingObject(params.getAsJsonObject("page").get("guid").getAsString());
      pages.add(page);
      listeners.notify(EventType.PAGE, page);
      if (page.opener() != null && !page.opener().isClosed()) {
        page.opener().notifyPopup(page);
      }
    } else if ("bindingCall".equals(event)) {
      BindingCall bindingCall = connection.getExistingObject(params.getAsJsonObject("binding").get("guid").getAsString());
      BindingCallback binding = bindings.get(bindingCall.name());
      if (binding != null) {
        bindingCall.call(binding);
      }
    } else if ("request".equals(event)) {
      String guid = params.getAsJsonObject("request").get("guid").getAsString();
      RequestImpl request = connection.getExistingObject(guid);
      listeners.notify(EventType.REQUEST, request);
      if (params.has("page")) {
        PageImpl page = connection.getExistingObject(params.getAsJsonObject("page").get("guid").getAsString());
        page.listeners.notify(PageImpl.EventType.REQUEST, request);
      }
    } else if ("requestFailed".equals(event)) {
      String guid = params.getAsJsonObject("request").get("guid").getAsString();
      RequestImpl request = connection.getExistingObject(guid);
      request.didFailOrFinish = true;
      if (params.has("failureText")) {
        request.failure = params.get("failureText").getAsString();
      }
      if (request.timing != null) {
        request.timing.responseEnd = params.get("responseEndTiming").getAsDouble();
      }
      listeners.notify(EventType.REQUESTFAILED, request);
      if (params.has("page")) {
        PageImpl page = connection.getExistingObject(params.getAsJsonObject("page").get("guid").getAsString());
        page.listeners.notify(PageImpl.EventType.REQUESTFAILED, request);
      }
    } else if ("requestFinished".equals(event)) {
      String guid = params.getAsJsonObject("request").get("guid").getAsString();
      RequestImpl request = connection.getExistingObject(guid);
      request.didFailOrFinish = true;
      if (request.timing != null) {
        request.timing.responseEnd = params.get("responseEndTiming").getAsDouble();
      }
      listeners.notify(EventType.REQUESTFINISHED, request);
      if (params.has("page")) {
        PageImpl page = connection.getExistingObject(params.getAsJsonObject("page").get("guid").getAsString());
        page.listeners.notify(PageImpl.EventType.REQUESTFINISHED, request);
      }
    } else if ("response".equals(event)) {
      String guid = params.getAsJsonObject("response").get("guid").getAsString();
      Response response = connection.getExistingObject(guid);
      listeners.notify(EventType.RESPONSE, response);
      if (params.has("page")) {
        PageImpl page = connection.getExistingObject(params.getAsJsonObject("page").get("guid").getAsString());
        page.listeners.notify(PageImpl.EventType.RESPONSE, response);
      }
    } else if ("close".equals(event)) {
      didClose();
    }
  }

  void didClose() {
    isClosedOrClosing = true;
    if (browser != null) {
      browser.contexts.remove(this);
    }
    listeners.notify(EventType.CLOSE, this);
  }

  WritableStream createTempFile(String name) {
    JsonObject params = new JsonObject();
    params.addProperty("name", name);
    JsonObject json = sendMessage("createTempFile", params).getAsJsonObject();
    return connection.getExistingObject(json.getAsJsonObject("writableStream").get("guid").getAsString());
  }
}
