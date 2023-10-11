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
import com.microsoft.playwright.options.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.Serialization.addHarUrlFilter;
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
  private boolean closeWasCalled;
  private final WaitableEvent<EventType, ?> closePromise;
  final Map<String, BindingCallback> bindings = new HashMap<>();
  PageImpl ownerPage;
  private static final Map<EventType, String> eventSubscriptions() {
    Map<EventType, String> result = new HashMap<>();
    result.put(EventType.CONSOLE, "console");
    result.put(EventType.DIALOG, "dialog");
    result.put(EventType.REQUEST, "request");
    result.put(EventType.RESPONSE, "response");
    result.put(EventType.REQUESTFINISHED, "requestFinished");
    result.put(EventType.REQUESTFAILED, "requestFailed");
    return result;
  }
  private final ListenerCollection<EventType> listeners = new ListenerCollection<>(eventSubscriptions(), this);
  final TimeoutSettings timeoutSettings = new TimeoutSettings();
  Path videosDir;
  URL baseUrl;
  final Map<String, HarRecorder> harRecorders = new HashMap<>();

  static class HarRecorder {
    final Path path;
    final HarContentPolicy contentPolicy;

    HarRecorder(Path har, HarContentPolicy policy) {
      path = har;
      contentPolicy = policy;
    }
  }

  enum EventType {
    CLOSE,
    CONSOLE,
    DIALOG,
    PAGE,
    WEBERROR,
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
    tracing = connection.getExistingObject(initializer.getAsJsonObject("tracing").get("guid").getAsString());
    request = connection.getExistingObject(initializer.getAsJsonObject("requestContext").get("guid").getAsString());
    closePromise = new WaitableEvent<>(listeners, EventType.CLOSE);
  }

  void setRecordHar(Path path, HarContentPolicy policy) {
    if (path != null) {
      harRecorders.put("", new HarRecorder(path, policy));
    }
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
  public void onConsoleMessage(Consumer<ConsoleMessage> handler) {
    listeners.add(EventType.CONSOLE, handler);
  }

  @Override
  public void offConsoleMessage(Consumer<ConsoleMessage> handler) {
    listeners.remove(EventType.CONSOLE, handler);
  }

  @Override
  public void onDialog(Consumer<Dialog> handler) {
    listeners.add(EventType.DIALOG, handler);
  }

  @Override
  public void offDialog(Consumer<Dialog> handler) {
    listeners.remove(EventType.DIALOG, handler);
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
  public void onWebError(Consumer<WebError> handler) {
    listeners.add(EventType.WEBERROR, handler);
  }

  @Override
  public void offWebError(Consumer<WebError> handler) {
    listeners.remove(EventType.WEBERROR, handler);
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
    return withWaitLogging("BrowserContext.close", logger -> waitForPageImpl(options, code));
  }

  private Page waitForPageImpl(WaitForPageOptions options, Runnable code) {
    if (options == null) {
      options = new WaitForPageOptions();
    }
    return waitForEventWithTimeout(EventType.PAGE, code, options.predicate, options.timeout);
  }

  @Override
  public CDPSession newCDPSession(Page page) {
    JsonObject params = new JsonObject();
    params.add("page", ((PageImpl) page).toProtocolRef());
    JsonObject result = sendMessage("newCDPSession", params).getAsJsonObject();
    return connection.getExistingObject(result.getAsJsonObject("session").get("guid").getAsString());
  }

  @Override
  public CDPSession newCDPSession(Frame frame) {
    JsonObject params = new JsonObject();
    params.add("frame", ((FrameImpl) frame).toProtocolRef());
    JsonObject result = sendMessage("newCDPSession", params).getAsJsonObject();
    return connection.getExistingObject(result.getAsJsonObject("session").get("guid").getAsString());
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
    if (!closeWasCalled) {
      closeWasCalled = true;
      for (Map.Entry<String, HarRecorder> entry : harRecorders.entrySet()) {
        JsonObject params = new JsonObject();
        params.addProperty("harId", entry.getKey());
        JsonObject json = sendMessage("harExport", params).getAsJsonObject();
        ArtifactImpl artifact = connection.getExistingObject(json.getAsJsonObject("artifact").get("guid").getAsString());
        // Server side will compress artifact if content is attach or if file is .zip.
        HarRecorder harParams = entry.getValue();
        boolean isCompressed = harParams.contentPolicy == HarContentPolicy.ATTACH || harParams.path.toString().endsWith(".zip");
        boolean needCompressed = harParams.path.toString().endsWith(".zip");
        if (isCompressed && !needCompressed) {
          String tmpPath = harParams.path + ".tmp";
          artifact.saveAs(Paths.get(tmpPath));
          JsonObject unzipParams = new JsonObject();
          unzipParams.addProperty("zipFile", tmpPath);
          unzipParams.addProperty("harFile", harParams.path.toString());
          connection.localUtils.sendMessage("harUnzip", unzipParams);
        } else {
          artifact.saveAs(harParams.path);
        }
        artifact.delete();
      }
      sendMessage("close");
    }
    runUntil(() -> {}, closePromise);
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
    route(new UrlMatcher(baseUrl, url), handler, options);
  }

  @Override
  public void route(Pattern url, Consumer<Route> handler, RouteOptions options) {
    route(new UrlMatcher(url), handler, options);
  }

  @Override
  public void route(Predicate<String> url, Consumer<Route> handler, RouteOptions options) {
    route(new UrlMatcher(url), handler, options);
  }

  @Override
  public void routeFromHAR(Path har, RouteFromHAROptions options) {
    if (options == null) {
      options = new RouteFromHAROptions();
    }
    if (options.update != null && options.update) {
      recordIntoHar(null, har, options);
      return;
    }
    UrlMatcher matcher = UrlMatcher.forOneOf(baseUrl, options.url);
    HARRouter harRouter = new HARRouter(connection.localUtils, har, options.notFound);
    onClose(context -> harRouter.dispose());
    route(matcher, route -> harRouter.handle(route), null);
  }

  private void route(UrlMatcher matcher, Consumer<Route> handler, RouteOptions options) {
    withLogging("BrowserContext.route", () -> {
      routes.add(matcher, handler, options == null ? null : options.times);
      updateInterceptionPatterns();
    });
  }

  void recordIntoHar(PageImpl page, Path har, RouteFromHAROptions options) {
    JsonObject params = new JsonObject();
    if (page != null) {
      params.add("page", page.toProtocolRef());
    }
    JsonObject jsonOptions = new JsonObject();
    jsonOptions.addProperty("path", har.toAbsolutePath().toString());
    jsonOptions.addProperty("content", options.updateContent == null ?
      HarContentPolicy.ATTACH.name().toLowerCase() :
      options.updateContent.name().toLowerCase());
    jsonOptions.addProperty("mode", options.updateMode == null ?
      HarMode.MINIMAL.name().toLowerCase() :
      options.updateMode.name().toLowerCase());
    addHarUrlFilter(jsonOptions, options.url);
    params.add("options", jsonOptions);
    JsonObject json = sendMessage("harStart", params).getAsJsonObject();
    String harId = json.get("harId").getAsString();
    harRecorders.put(harId, new HarRecorder(har, HarContentPolicy.ATTACH));
  }

  @Override
  public void setDefaultNavigationTimeout(double timeout) {
    setDefaultNavigationTimeoutImpl(timeout);
  }

  void setDefaultNavigationTimeoutImpl(Double timeout) {
    withLogging("BrowserContext.setDefaultNavigationTimeout", () -> {
      timeoutSettings.setDefaultNavigationTimeout(timeout);
      JsonObject params = new JsonObject();
      params.addProperty("timeout", timeout);
      sendMessage("setDefaultNavigationTimeoutNoReply", params);
    });
  }

  @Override
  public void setDefaultTimeout(double timeout) {
    setDefaultTimeoutImpl(timeout);
  }

  void setDefaultTimeoutImpl(Double timeout) {
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

  @Override
  public void waitForCondition(BooleanSupplier predicate, WaitForConditionOptions options) {
    List<Waitable<Void>> waitables = new ArrayList<>();
    waitables.add(new WaitableContextClose<>());
    waitables.add(timeoutSettings.createWaitable(options == null ? null : options.timeout));
    waitables.add(new WaitablePredicate<>(predicate));
    runUntil(() -> {}, new WaitableRace<>(waitables));
  }

  @Override
  public ConsoleMessage waitForConsoleMessage(WaitForConsoleMessageOptions options, Runnable code) {
    return withWaitLogging("BrowserContext.waitForConsoleMessage", logger -> waitForConsoleMessageImpl(options, code));
  }

  private ConsoleMessage waitForConsoleMessageImpl(WaitForConsoleMessageOptions options, Runnable code) {
    if (options == null) {
      options = new WaitForConsoleMessageOptions();
    }
    return waitForEventWithTimeout(EventType.CONSOLE, code, options.predicate, options.timeout);
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
      updateInterceptionPatterns();
    });
  }

  private void updateInterceptionPatterns() {
    sendMessage("setNetworkInterceptionPatterns", routes.interceptionPatterns());
  }

  void handleRoute(RouteImpl route) {
    Router.HandleResult handled = routes.handle(route);
    if (handled != Router.HandleResult.NoMatchingHandler) {
      updateInterceptionPatterns();
    }
    if (handled == Router.HandleResult.NoMatchingHandler || handled == Router.HandleResult.Fallback) {
      route.resume(null, true);
    }
  }

  WaitableResult<JsonElement> pause() {
    return sendMessageAsync("pause", new JsonObject());
  }

  @Override
  protected void handleEvent(String event, JsonObject params) {
    if ("dialog".equals(event)) {
      String guid = params.getAsJsonObject("dialog").get("guid").getAsString();
      DialogImpl dialog = connection.getExistingObject(guid);
      boolean hasListeners = false;
      if (listeners.hasListeners(EventType.DIALOG)) {
        hasListeners = true;
        listeners.notify(EventType.DIALOG, dialog);
      }
      PageImpl page = dialog.page();
      if (page != null) {
        if (page.listeners.hasListeners(PageImpl.EventType.DIALOG)) {
          hasListeners = true;
          page.listeners.notify(PageImpl.EventType.DIALOG, dialog);
        }
      }
      // Although we do similar handling on the server side, we still need this logic
      // on the client side due to a possible race condition between two async calls:
      // a) removing "dialog" listener subscription (client->server)
      // b) actual "dialog" event (server->client)
      if (!hasListeners) {
        if ("beforeunload".equals(dialog.type())) {
          try {
            dialog.accept();
          } catch (PlaywrightException e) {
          }
        } else {
          dialog.dismiss();
        }
      }
    } else if ("route".equals(event)) {
      RouteImpl route = connection.getExistingObject(params.getAsJsonObject("route").get("guid").getAsString());
      route.browserContext = this;
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
    } else if ("console".equals(event)) {
      ConsoleMessageImpl message = new ConsoleMessageImpl(connection, params);
      listeners.notify(BrowserContextImpl.EventType.CONSOLE, message);
      PageImpl page = message.page();
      if (page != null) {
        page.listeners.notify(PageImpl.EventType.CONSOLE, message);
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
    } else if ("pageError".equals(event)) {
      SerializedError error = gson().fromJson(params.getAsJsonObject("error"), SerializedError.class);
      String errorStr = "";
      if (error.error != null) {
        errorStr = error.error.name + ": " + error.error.message;
        if (error.error.stack != null && !error.error.stack.isEmpty()) {
          errorStr += "\n" + error.error.stack;
        }
      }
      PageImpl page;
      try {
        page = connection.getExistingObject(params.getAsJsonObject("page").get("guid").getAsString());
      } catch (PlaywrightException e) {
        page = null;
      }
      listeners.notify(BrowserContextImpl.EventType.WEBERROR, new WebErrorImpl(page, errorStr));
      if (page != null) {
        page.listeners.notify(PageImpl.EventType.PAGEERROR, errorStr);
      }
    } else if ("close".equals(event)) {
      didClose();
    }
  }

  void didClose() {
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
