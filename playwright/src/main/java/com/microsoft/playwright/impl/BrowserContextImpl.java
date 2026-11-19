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

import com.microsoft.playwright.impl.protocol.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
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

import static com.microsoft.playwright.impl.Serialization.*;
import static com.microsoft.playwright.impl.Utils.*;
import static com.microsoft.playwright.impl.Errors.isSafeCloseError;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;
import static java.util.Arrays.asList;

public class BrowserContextImpl extends ChannelOwner implements BrowserContext {
  protected BrowserImpl browser;
  private final TracingImpl tracing;
  private final DebuggerImpl debugger;
  private final APIRequestContextImpl request;
  private final ClockImpl clock;
  private final CredentialsImpl credentials;
  final List<PageImpl> pages = new ArrayList<>();

  final Router routes = new Router();
  final WebSocketRouter webSocketRoutes = new WebSocketRouter();
  private boolean closingOrClosed;
  private final WaitableEvent<?> closePromise;
  final Map<String, BindingCallback> bindings = new HashMap<>();
  PageImpl ownerPage;
  private String closeReason;
  boolean recordVideo;

  final TimeoutSettings timeoutSettings = new TimeoutSettings();

  final BrowserContextChannel channel = new BrowserContextChannel(this);

  BrowserContextImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    Map<String, String> mapping = new HashMap<>();
    mapping.put(Events.BrowserContext.CONSOLE, "console");
    mapping.put(Events.BrowserContext.DIALOG, "dialog");
    mapping.put(Events.BrowserContext.DIALOG_CLOSED, "dialogClosed");
    mapping.put(Events.BrowserContext.REQUEST, "request");
    mapping.put(Events.BrowserContext.RESPONSE, "response");
    mapping.put(Events.BrowserContext.REQUEST_FINISHED, "requestFinished");
    mapping.put(Events.BrowserContext.REQUEST_FAILED, "requestFailed");
    setEventToSubscriptionMapping(mapping);
    tracing = connection.getExistingObject(initializer.getAsJsonObject("tracing").get("guid").getAsString());
    debugger = connection.getExistingObject(initializer.getAsJsonObject("debugger").get("guid").getAsString());
    request = connection.getExistingObject(initializer.getAsJsonObject("requestContext").get("guid").getAsString());
    request.timeoutSettings = timeoutSettings;
    clock = new ClockImpl(this);
    credentials = new CredentialsImpl(this);
    closePromise = new WaitableEvent<>(this, Events.BrowserContext.CLOSE);
  }

  Path videosDir() {
    JsonObject recordVideo = initializer.getAsJsonObject("options").getAsJsonObject("recordVideo");
    if (recordVideo == null) {
      return null;
    }
    return Paths.get(recordVideo.get("dir").getAsString());
  }

  URL baseUrl() {
    JsonElement url = initializer.getAsJsonObject("options").get("baseURL");
    if (url != null) {
      try {
        return new URL(url.getAsString());
      } catch (MalformedURLException e) {
      }
    }
    return null;
  }

  String effectiveCloseReason() {
    if (closeReason != null) {
      return closeReason;
    }
    if (browser != null) {
      return browser.closeReason;
    }
    return null;
  }

  @Override
  public void onBackgroundPage(Consumer<Page> handler) {
  }

  @Override
  public void offBackgroundPage(Consumer<Page> handler) {
  }

  @Override
  public void onDownload(Consumer<Download> handler) {
    on(Events.BrowserContext.DOWNLOAD, handler);
  }

  @Override
  public void offDownload(Consumer<Download> handler) {
    off(Events.BrowserContext.DOWNLOAD, handler);
  }

  void notifyDownload(Download download) {
    emit(Events.BrowserContext.DOWNLOAD, download);
  }

  @Override
  public void onClose(Consumer<BrowserContext> handler) {
    on(Events.BrowserContext.CLOSE, handler);
  }

  @Override
  public void offClose(Consumer<BrowserContext> handler) {
    off(Events.BrowserContext.CLOSE, handler);
  }

  @Override
  public void onConsoleMessage(Consumer<ConsoleMessage> handler) {
    on(Events.BrowserContext.CONSOLE, handler);
  }

  @Override
  public void offConsoleMessage(Consumer<ConsoleMessage> handler) {
    off(Events.BrowserContext.CONSOLE, handler);
  }

  @Override
  public void onDialog(Consumer<Dialog> handler) {
    on(Events.BrowserContext.DIALOG, handler);
  }

  @Override
  public void offDialog(Consumer<Dialog> handler) {
    off(Events.BrowserContext.DIALOG, handler);
  }

  @Override
  public void onDialogClosed(Consumer<Dialog> handler) {
    on(Events.BrowserContext.DIALOG_CLOSED, handler);
  }

  @Override
  public void offDialogClosed(Consumer<Dialog> handler) {
    off(Events.BrowserContext.DIALOG_CLOSED, handler);
  }

  @Override
  public void onPage(Consumer<Page> handler) {
    on(Events.BrowserContext.PAGE, handler);
  }

  @Override
  public void offPage(Consumer<Page> handler) {
    off(Events.BrowserContext.PAGE, handler);
  }

  @Override
  public void onFrameAttached(Consumer<Frame> handler) {
    on(Events.BrowserContext.FRAME_ATTACHED, handler);
  }

  @Override
  public void offFrameAttached(Consumer<Frame> handler) {
    off(Events.BrowserContext.FRAME_ATTACHED, handler);
  }

  void notifyFrameAttached(FrameImpl frame) {
    emit(Events.BrowserContext.FRAME_ATTACHED, frame);
  }

  @Override
  public void onFrameDetached(Consumer<Frame> handler) {
    on(Events.BrowserContext.FRAME_DETACHED, handler);
  }

  @Override
  public void offFrameDetached(Consumer<Frame> handler) {
    off(Events.BrowserContext.FRAME_DETACHED, handler);
  }

  void notifyFrameDetached(FrameImpl frame) {
    emit(Events.BrowserContext.FRAME_DETACHED, frame);
  }

  @Override
  public void onFrameNavigated(Consumer<Frame> handler) {
    on(Events.BrowserContext.FRAME_NAVIGATED, handler);
  }

  @Override
  public void offFrameNavigated(Consumer<Frame> handler) {
    off(Events.BrowserContext.FRAME_NAVIGATED, handler);
  }

  void notifyFrameNavigated(FrameImpl frame) {
    emit(Events.BrowserContext.FRAME_NAVIGATED, frame);
  }

  @Override
  public void onPageClose(Consumer<Page> handler) {
    on(Events.BrowserContext.PAGE_CLOSE, handler);
  }

  @Override
  public void offPageClose(Consumer<Page> handler) {
    off(Events.BrowserContext.PAGE_CLOSE, handler);
  }

  void notifyPageClose(PageImpl page) {
    emit(Events.BrowserContext.PAGE_CLOSE, page);
  }

  @Override
  public void onPageLoad(Consumer<Page> handler) {
    on(Events.BrowserContext.PAGE_LOAD, handler);
  }

  @Override
  public void offPageLoad(Consumer<Page> handler) {
    off(Events.BrowserContext.PAGE_LOAD, handler);
  }

  void notifyPageLoad(PageImpl page) {
    emit(Events.BrowserContext.PAGE_LOAD, page);
  }

  @Override
  public void onWebError(Consumer<WebError> handler) {
    on(Events.BrowserContext.WEB_ERROR, handler);
  }

  @Override
  public void offWebError(Consumer<WebError> handler) {
    off(Events.BrowserContext.WEB_ERROR, handler);
  }

  @Override
  public void onRequest(Consumer<Request> handler) {
    on(Events.BrowserContext.REQUEST, handler);
  }

  @Override
  public void offRequest(Consumer<Request> handler) {
    off(Events.BrowserContext.REQUEST, handler);
  }

  @Override
  public void onRequestFailed(Consumer<Request> handler) {
    on(Events.BrowserContext.REQUEST_FAILED, handler);
  }

  @Override
  public void offRequestFailed(Consumer<Request> handler) {
    off(Events.BrowserContext.REQUEST_FAILED, handler);
  }

  @Override
  public void onRequestFinished(Consumer<Request> handler) {
    on(Events.BrowserContext.REQUEST_FINISHED, handler);
  }

  @Override
  public void offRequestFinished(Consumer<Request> handler) {
    off(Events.BrowserContext.REQUEST_FINISHED, handler);
  }

  @Override
  public void onResponse(Consumer<Response> handler) {
    on(Events.BrowserContext.RESPONSE, handler);
  }

  @Override
  public void offResponse(Consumer<Response> handler) {
    off(Events.BrowserContext.RESPONSE, handler);
  }

  @Override
  public ClockImpl clock() {
    return clock;
  }

  @Override
  public Credentials credentials() {
    return credentials;
  }

  @Override
  public Page waitForPage(WaitForPageOptions options, Runnable code) {
    if (options == null) {
      options = new WaitForPageOptions();
    }
    return waitForEvent(Events.BrowserContext.PAGE, options.predicate, options.timeout, code);
  }

  // waitForEvent in browserContext.ts
  private <T> T waitForEvent(String event, Predicate<T> predicate, Double timeout, Runnable code) {
    double timeoutMs = timeoutSettings.timeout(timeout);
    Waiter waiter = Waiter.createForEvent(this, event);
    waiter.rejectOnTimeout(timeoutMs, "Timeout " + Waiter.formatTimeout(timeoutMs) + "ms exceeded while waiting for event \"" + event + "\"");
    if (!Events.BrowserContext.CLOSE.equals(event)) {
      waiter.rejectOnEvent(this, Events.BrowserContext.CLOSE, () -> new TargetClosedError(effectiveCloseReason()));
    }
    T result = waiter.waitForEvent(this, event, predicate, code);
    waiter.dispose();
    return result;
  }

  @Override
  public CDPSession newCDPSession(Page page) {
    BrowserContextNewCDPSessionParams params = new BrowserContextNewCDPSessionParams();
    params.page = (PageImpl) page;
    return channel.newCDPSession(params, NO_TIMEOUT).session;
  }

  @Override
  public CDPSession newCDPSession(Frame frame) {
    BrowserContextNewCDPSessionParams params = new BrowserContextNewCDPSessionParams();
    params.frame = (FrameImpl) frame;
    return channel.newCDPSession(params, NO_TIMEOUT).session;
  }

  @Override
  public boolean isClosed() {
    return closingOrClosed;
  }

  @Override
  public void close(CloseOptions options) {
    RuntimeException harError = null;
    if (!closingOrClosed) {
      closingOrClosed = true;
      if (options == null) {
        options = new CloseOptions();
      }
      closeReason = options.reason;
      try {
        request.dispose(convertType(options, APIRequestContext.DisposeOptions.class));
      } catch (TargetClosedError e) {
        // The request context was disposed by the user already.
      }
      try {
        tracing.exportAllHars();
      } catch (RuntimeException e) {
        harError = e;
      }
      BrowserContextCloseParams params = toProtocol(options, BrowserContextCloseParams.class);
      channel.close(params, NO_TIMEOUT);
    }
    runUntil(() -> {}, closePromise);
    if (harError != null) {
      throw harError;
    }
  }

  @Override
  public List<Cookie> cookies(String url) {
    return cookies(url == null ? new ArrayList<>() : Collections.singletonList(url));
  }


  @Override
  public void addCookies(List<Cookie> cookies) {
    BrowserContextAddCookiesParams params = new BrowserContextAddCookiesParams();
    params.cookies = connection.protocolGson().fromJson(gson().toJsonTree(cookies), TypeToken.getParameterized(List.class, SetNetworkCookie.class).getType());
    channel.addCookies(params, NO_TIMEOUT);
  }

  @Override
  public AutoCloseable addInitScript(String script) {
    BrowserContextAddInitScriptParams params = new BrowserContextAddInitScriptParams();
    params.source = script;
    return channel.addInitScript(params, NO_TIMEOUT).disposable;
  }

  @Override
  public AutoCloseable addInitScript(Path path) {
    try {
      byte[] bytes = readAllBytes(path);
      return addInitScript(new String(bytes, UTF_8));
    } catch (IOException e) {
      throw new PlaywrightException("Failed to read script from file", e);
    }
  }

  @Override
  public List<Page> backgroundPages() {
    return Collections.emptyList();
  }

  @Override
  public BrowserImpl browser() {
    return browser;
  }

  @Override
  public void clearCookies(ClearCookiesOptions options) {
    if (options == null) {
      options = new ClearCookiesOptions();
    }
    BrowserContextClearCookiesParams params = connection.protocolGson().fromJson(toClearCookiesParams(options.name, options.domain, options.path), BrowserContextClearCookiesParams.class);
    channel.clearCookies(params, NO_TIMEOUT);
  }

  @Override
  public void clearPermissions() {
    channel.clearPermissions(NO_TIMEOUT);
  }

  @Override
  public List<Cookie> cookies(List<String> urls) {
    BrowserContextCookiesParams params = new BrowserContextCookiesParams();
    params.urls = urls == null ? new ArrayList<>() : urls;
    BrowserContextCookiesResult result = channel.cookies(params, NO_TIMEOUT);
    Cookie[] cookies = gson().fromJson(gson().toJsonTree(result.cookies), Cookie[].class);
    return asList(cookies);
  }

  @Override
  public AutoCloseable exposeBinding(String name, BindingCallback playwrightBinding) {
    return exposeBindingImpl(name, playwrightBinding);
  }

  private AutoCloseable exposeBindingImpl(String name, BindingCallback playwrightBinding) {
    if (bindings.containsKey(name)) {
      throw new PlaywrightException("Function \"" + name + "\" has been already registered");
    }
    for (PageImpl page : pages) {
      if (page.bindings.containsKey(name)) {
        throw new PlaywrightException("Function \"" + name + "\" has been already registered in one of the pages");
      }
    }
    bindings.put(name, playwrightBinding);

    BrowserContextExposeBindingParams params = new BrowserContextExposeBindingParams();
    params.name = name;
    return channel.exposeBinding(params, NO_TIMEOUT).disposable;
  }

  @Override
  public AutoCloseable exposeFunction(String name, FunctionCallback playwrightFunction) {
    return exposeBindingImpl(name, (BindingCallback.Source source, Object... args) -> playwrightFunction.call(args));
  }

  @Override
  public void grantPermissions(List<String> permissions, GrantPermissionsOptions options) {
    if (options == null) {
      options = new GrantPermissionsOptions();
    }
    if (permissions == null) {
      permissions = new ArrayList<>();
    }
    BrowserContextGrantPermissionsParams params = toProtocol(options, BrowserContextGrantPermissionsParams.class);
    params.permissions = permissions;
    channel.grantPermissions(params, NO_TIMEOUT);
  }

  @Override
  public PageImpl newPage() {
    if (ownerPage != null) {
      throw new PlaywrightException("Please use browser.newContext()");
    }
    return channel.newPage(NO_TIMEOUT).page;
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
  public AutoCloseable route(String url, Consumer<Route> handler, RouteOptions options) {
    route(UrlMatcher.forGlob(baseUrl(), url, this.connection.localUtils(), false), handler, options);
    return new DisposableStub(() -> unroute(url, handler));
  }

  @Override
  public AutoCloseable route(Pattern url, Consumer<Route> handler, RouteOptions options) {
    route(new UrlMatcher(url), handler, options);
    return new DisposableStub(() -> unroute(url, handler));
  }

  @Override
  public AutoCloseable route(Predicate<String> url, Consumer<Route> handler, RouteOptions options) {
    route(new UrlMatcher(url), handler, options);
    return new DisposableStub(() -> unroute(url, handler));
  }

  @Override
  public void routeFromHAR(Path har, RouteFromHAROptions options) {
    if (options == null) {
      options = new RouteFromHAROptions();
    }
    if (options.update != null && options.update) {
      recordIntoHar(null, har, options, null);
      return;
    }
    UrlMatcher matcher = UrlMatcher.forOneOf(baseUrl(), options.url, this.connection.localUtils(), false);
    HARRouter harRouter = new HARRouter(connection.localUtils(), har, options.notFound);
    onClose(context -> harRouter.dispose());
    route(matcher, route -> harRouter.handle(route), null);
  }

  private void route(UrlMatcher matcher, Consumer<Route> handler, RouteOptions options) {
    routes.add(matcher, handler, options == null ? null : options.times);
    updateInterceptionPatterns();
  }

  @Override
  public void routeWebSocket(String url, Consumer<WebSocketRoute> handler) {
    routeWebSocketImpl(UrlMatcher.forGlob(baseUrl(), url, this.connection.localUtils(), true), handler);
  }

  @Override
  public void routeWebSocket(Pattern pattern, Consumer<WebSocketRoute> handler) {
    routeWebSocketImpl(new UrlMatcher(pattern), handler);
  }

  @Override
  public void routeWebSocket(Predicate<String> predicate, Consumer<WebSocketRoute> handler) {
    routeWebSocketImpl(new UrlMatcher(predicate), handler);
  }

  private void routeWebSocketImpl(UrlMatcher matcher, Consumer<WebSocketRoute> handler) {
    webSocketRoutes.add(matcher, handler);
    updateWebSocketInterceptionPatterns();
  }

  void recordIntoHar(PageImpl page, Path har, RouteFromHAROptions options, HarContentPolicy contentPolicy) {
    if (contentPolicy == null) {
      contentPolicy = Utils.convertType(options.updateContent, HarContentPolicy.class);
    }
    tracing.recordIntoHar(page, har, options.url, contentPolicy, options.updateMode, null);
  }

  @Override
  public void setDefaultNavigationTimeout(double timeout) {
    timeoutSettings.setDefaultNavigationTimeout(timeout);
  }

  @Override
  public void setDefaultTimeout(double timeout) {
    timeoutSettings.setDefaultTimeout(timeout);
  }

  @Override
  public void setExtraHTTPHeaders(Map<String, String> headers) {
    BrowserContextSetExtraHTTPHeadersParams params = new BrowserContextSetExtraHTTPHeadersParams();
    params.headers = new ArrayList<>();
    for (Map.Entry<String, String> e : headers.entrySet()) {
      NameValue header = new NameValue();
      header.name = e.getKey();
      header.value = e.getValue();
      params.headers.add(header);
    }
    channel.setExtraHTTPHeaders(params, NO_TIMEOUT);
  }

  @Override
  public void setGeolocation(Geolocation geolocation) {
    BrowserContextSetGeolocationParams params = new BrowserContextSetGeolocationParams();
    if (geolocation != null) {
      params.geolocation = toProtocol(geolocation, BrowserContextSetGeolocationParams.BrowserContextSetGeolocationParamsGeolocation.class);
    }
    channel.setGeolocation(params, NO_TIMEOUT);
  }

  @Override
  public void setOffline(boolean offline) {
    BrowserContextSetOfflineParams params = new BrowserContextSetOfflineParams();
    params.offline = offline;
    channel.setOffline(params, NO_TIMEOUT);
  }

  @Override
  public void setStorageState(Path storageState) {
    try {
      String state = new String(readAllBytes(storageState), UTF_8);
      BrowserContextSetStorageStateParams params = new BrowserContextSetStorageStateParams();
      params.storageState = connection.protocolGson().fromJson(state, BrowserContextSetStorageStateParams.BrowserContextSetStorageStateParamsStorageState.class);
      channel.setStorageState(params, NO_TIMEOUT);
    } catch (IOException e) {
      throw new PlaywrightException("Failed to read storage state from file", e);
    }
  }

  @Override
  public String storageState(StorageStateOptions options) {
    if (options == null) {
      options = new StorageStateOptions();
    }
    BrowserContextStorageStateParams params = toProtocol(options, BrowserContextStorageStateParams.class);
    // The reply is returned to the user verbatim: a typed round trip would print -1 as -1.0 and
    // differ from the raw string of APIRequestContext.storageState(), so the typed params go through
    // the JsonObject overload and the result stays a JsonElement.
    JsonElement json = sendMessage("storageState", connection.protocolGson().toJsonTree(params).getAsJsonObject(), NO_TIMEOUT);

    String storageState = json.toString();
    if (options.path != null) {
      Utils.writeToFile(Serialization.toPrettyJson(storageState).getBytes(StandardCharsets.UTF_8), options.path);
    }
    return storageState;
  }

  @Override
  public DebuggerImpl debugger() {
    return debugger;
  }

  void setTracesDir(Path tracesDir) {
    tracing.setTracesDir(tracesDir);
    request.tracing().setTracesDir(tracesDir);
  }

  @Override
  public TracingImpl tracing() {
    return tracing;
  }

  @Override
  public void unrouteAll() {
    routes.removeAll();
    updateInterceptionPatterns();
  }

  @Override
  public void unroute(String url, Consumer<Route> handler) {
    unroute(UrlMatcher.forGlob(this.baseUrl(), url, this.connection.localUtils(), false), handler);
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
    if (options == null) {
      options = new WaitForConsoleMessageOptions();
    }
    return waitForEvent(Events.BrowserContext.CONSOLE, options.predicate, options.timeout, code);
  }

  private class WaitableContextClose<R> extends WaitableEvent<R> {
    WaitableContextClose() {
      super(BrowserContextImpl.this, Events.BrowserContext.CLOSE);
    }

    @Override
    public R get() {
      throw new TargetClosedError(effectiveCloseReason());
    }
  }

  private void unroute(UrlMatcher matcher, Consumer<Route> handler) {
    routes.remove(matcher, handler);
    updateInterceptionPatterns();
  }

  private void updateInterceptionPatterns() {
    BrowserContextSetNetworkInterceptionPatternsParams params = connection.protocolGson().fromJson(routes.interceptionPatterns(), BrowserContextSetNetworkInterceptionPatternsParams.class);
    channel.setNetworkInterceptionPatterns(params, NO_TIMEOUT);
  }

  private void updateWebSocketInterceptionPatterns() {
    BrowserContextSetWebSocketInterceptionPatternsParams params = connection.protocolGson().fromJson(webSocketRoutes.interceptionPatterns(), BrowserContextSetWebSocketInterceptionPatternsParams.class);
    channel.setWebSocketInterceptionPatterns(params, NO_TIMEOUT);
  }

  void handleRoute(RouteImpl route) {
    Router.HandleResult handled = routes.handle(route);
    if (handled != Router.HandleResult.NoMatchingHandler) {
      try {
        updateInterceptionPatterns();
      } catch (PlaywrightException e) {
        // Like the Node.js client, ignore the error when the page or the context was closed meanwhile.
        if (!isSafeCloseError(e)) {
          throw e;
        }
      }
    }
    if (handled == Router.HandleResult.NoMatchingHandler || handled == Router.HandleResult.Fallback) {
      route.continueAsFallback();
    }
  }

  void handleWebSocketRoute(WebSocketRouteImpl route) {
    if (!webSocketRoutes.handle(route)) {
      route.connectToServer();
    }
  }

  WaitableResult<JsonElement> pause() {
    return sendMessageAsync("pause", new BrowserContextPauseParams());
  }

  @Override
  protected void handleEvent(String event, JsonObject params) {
    if ("dialog".equals(event)) {
      String guid = params.getAsJsonObject("dialog").get("guid").getAsString();
      DialogImpl dialog = connection.getExistingObject(guid);
      boolean hasListeners = false;
      if (listenerCount(Events.BrowserContext.DIALOG) > 0) {
        hasListeners = true;
        emit(Events.BrowserContext.DIALOG, dialog);
      }
      PageImpl page = dialog.page();
      if (page != null) {
        if (page.listenerCount(Events.Page.DIALOG) > 0) {
          hasListeners = true;
          page.emit(Events.Page.DIALOG, dialog);
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
    } else if ("dialogClosed".equals(event)) {
      DialogImpl dialog = connection.getExistingObject(params.getAsJsonObject("dialog").get("guid").getAsString());
      emit(Events.BrowserContext.DIALOG_CLOSED, dialog);
      PageImpl page = dialog.page();
      if (page != null) {
        page.emit(Events.Page.DIALOG_CLOSED, dialog);
      }
    } else if ("route".equals(event)) {
      RouteImpl route = connection.getExistingObject(params.getAsJsonObject("route").get("guid").getAsString());
      route.browserContext = this;
      handleRoute(route);
    } else if ("webSocketRoute".equals(event)) {
      WebSocketRouteImpl route = connection.getExistingObject(params.getAsJsonObject("webSocketRoute").get("guid").getAsString());
      handleWebSocketRoute(route);
    } else if ("page".equals(event)) {
      PageImpl page = connection.getExistingObject(params.getAsJsonObject("page").get("guid").getAsString());
      pages.add(page);
      emit(Events.BrowserContext.PAGE, page);
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
      BrowserContextConsoleEvent consoleEvent = connection.protocolGson().fromJson(params, BrowserContextConsoleEvent.class);
      PageImpl page = consoleEvent.page;
      WorkerImpl worker = consoleEvent.worker;
      ConsoleMessageImpl message = new ConsoleMessageImpl(consoleEvent);
      emit(Events.BrowserContext.CONSOLE, message);
      if (worker != null) {
        worker.emit(Events.Worker.CONSOLE, message);
      }
      if (page != null) {
        page.emit(Events.Page.CONSOLE, message);
      }
    } else if ("request".equals(event)) {
      String guid = params.getAsJsonObject("request").get("guid").getAsString();
      RequestImpl request = connection.getExistingObject(guid);
      emit(Events.BrowserContext.REQUEST, request);
      if (params.has("page")) {
        PageImpl page = connection.getExistingObject(params.getAsJsonObject("page").get("guid").getAsString());
        page.emit(Events.Page.REQUEST, request);
      }
    } else if ("requestFailed".equals(event)) {
      String guid = params.getAsJsonObject("request").get("guid").getAsString();
      RequestImpl request = connection.getExistingObject(guid);
      request.didFailOrFinish = true;
      if (params.has("failureText")) {
        request.failure = params.get("failureText").getAsString();
      }
      if (request.timing != null) {
        request.setResponseEndTiming(params.get("responseEndTiming").getAsDouble());
      }
      emit(Events.BrowserContext.REQUEST_FAILED, request);
      if (params.has("page")) {
        PageImpl page = connection.getExistingObject(params.getAsJsonObject("page").get("guid").getAsString());
        page.emit(Events.Page.REQUEST_FAILED, request);
      }
    } else if ("requestFinished".equals(event)) {
      String guid = params.getAsJsonObject("request").get("guid").getAsString();
      RequestImpl request = connection.getExistingObject(guid);
      request.didFailOrFinish = true;
      if (request.timing != null) {
        request.setResponseEndTiming(params.get("responseEndTiming").getAsDouble());
      }
      emit(Events.BrowserContext.REQUEST_FINISHED, request);
      if (params.has("page")) {
        PageImpl page = connection.getExistingObject(params.getAsJsonObject("page").get("guid").getAsString());
        page.emit(Events.Page.REQUEST_FINISHED, request);
      }
    } else if ("response".equals(event)) {
      String guid = params.getAsJsonObject("response").get("guid").getAsString();
      ResponseImpl response = connection.getExistingObject(guid);
      emit(Events.BrowserContext.RESPONSE, response);
      if (params.has("page")) {
        PageImpl page = connection.getExistingObject(params.getAsJsonObject("page").get("guid").getAsString());
        page.emit(Events.Page.RESPONSE, response);
      }
    } else if ("pageError".equals(event)) {
      String errorStr = parseError(params.getAsJsonObject("error"));
      PageImpl page;
      try {
        page = connection.getExistingObject(params.getAsJsonObject("page").get("guid").getAsString());
      } catch (PlaywrightException e) {
        page = null;
      }
      WebErrorLocation location = null;
      if (params.has("location")) {
        location = gson().fromJson(params.getAsJsonObject("location"), WebErrorLocation.class);
      }
      emit(Events.BrowserContext.WEB_ERROR, new WebErrorImpl(page, errorStr, location));
      if (page != null) {
        page.emit(Events.Page.PAGE_ERROR, errorStr);
      }
    } else if ("close".equals(event)) {
      didClose();
    }
  }

  void didClose() {
    closingOrClosed = true;
    if (browser != null) {
      browser.contexts.remove(this);
      browser.browserType.playwright.selectors.contextsForSelectors.remove(this);
    }
    tracing.resetStackCounter();
    request.tracing().resetStackCounter();
    emit(Events.BrowserContext.CLOSE, this);
  }

  protected void initializeHarFromOptions(Browser.NewContextOptions options) {
    if (options.recordHarPath == null) {
      if (options.recordHarOmitContent != null) {
        throw new PlaywrightException("recordHarOmitContent is set but recordHarPath is null");
      }
      if (options.recordHarUrlFilter != null) {
        throw new PlaywrightException("recordHarUrlFilter is set but recordHarPath is null");
      }
      if (options.recordHarMode != null) {
        throw new PlaywrightException("recordHarMode is set but recordHarPath is null");
      }
      if (options.recordHarContent != null) {
        throw new PlaywrightException("recordHarContent is set but recordHarPath is null");
      }
      return;
    }

    HarContentPolicy contentPolicy = options.recordHarContent;
    if (contentPolicy == null && options.recordHarOmitContent != null && options.recordHarOmitContent == true) {
      contentPolicy = HarContentPolicy.OMIT;
    }
    if (contentPolicy == null) {
      contentPolicy = TracingImpl.isZip(options.recordHarPath) ? HarContentPolicy.ATTACH : HarContentPolicy.EMBED;
    }
    RouteFromHAROptions routeFromHAROptions = new RouteFromHAROptions();

    if (options.recordHarUrlFilter instanceof String) {
      routeFromHAROptions.setUrl((String) options.recordHarUrlFilter);
    } else if (options.recordHarUrlFilter instanceof Pattern) {
      routeFromHAROptions.setUrl((Pattern) options.recordHarUrlFilter);
    }

    if (options.recordHarMode != null) {
      routeFromHAROptions.updateMode = options.recordHarMode;
    } else {
      routeFromHAROptions.updateMode = HarMode.FULL;
    }
    routeFromHAROptions.url = options.recordHarUrlFilter;

    recordIntoHar(null, options.recordHarPath, routeFromHAROptions, contentPolicy);
  }
}
