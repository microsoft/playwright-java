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

// Port of browserContext.ts.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.impl.isomorphic.Headers;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.CDPSession;
import com.microsoft.playwright.ConsoleMessage;
import com.microsoft.playwright.Credentials;
import com.microsoft.playwright.Dialog;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.WebError;
import com.microsoft.playwright.WebSocketRoute;
import com.microsoft.playwright.impl.protocol.*;
import com.microsoft.playwright.options.BindingCallback;
import com.microsoft.playwright.options.ClientCertificate;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.FunctionCallback;
import com.microsoft.playwright.options.Geolocation;
import com.microsoft.playwright.options.HarContentPolicy;
import com.microsoft.playwright.options.HarMode;
import com.microsoft.playwright.options.HarNotFound;
import com.microsoft.playwright.options.WebErrorLocation;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.ClientHelper.evaluationScript;
import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.Utils.convertType;
import static com.microsoft.playwright.impl.Utils.toProtocol;

public class BrowserContextImpl extends ChannelOwner implements BrowserContext {
  final Set<PageImpl> pages = new LinkedHashSet<>();
  List<RouteHandler> routes = new ArrayList<>();
  List<WebSocketRouteHandler> webSocketRoutes = new ArrayList<>();
  // Browser is null for browser contexts created outside of normal browser, e.g. android or electron.
  BrowserImpl browser = null;
  final Map<String, BindingCallback> bindings = new LinkedHashMap<>();
  final TimeoutSettings timeoutSettings;
  PageImpl ownerPage;
  private final WaitableEvent<BrowserContextImpl> closedPromise;
  final BrowserContextInitializer.BrowserContextInitializerOptions options;

  final DebuggerImpl debugger;
  final APIRequestContextImpl request;
  final TracingImpl tracing;
  final ClockImpl clock;
  final CredentialsImpl credentials;

  final Set<WorkerImpl> serviceWorkers = new LinkedHashSet<>();
  private String closingStatus = "none";
  private String closeReason;
  private List<HarRouter> harRouters = new ArrayList<>();

  final BrowserContextChannel channel = new BrowserContextChannel(this);

  BrowserContextImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    BrowserContextInitializer contextInitializer = connection.protocolGson().fromJson(initializer, BrowserContextInitializer.class);
    this.options = contextInitializer.options;
    this.timeoutSettings = new TimeoutSettings();
    this.debugger = contextInitializer.debugger;
    this.tracing = contextInitializer.tracing;
    this.request = contextInitializer.requestContext;
    this.request.timeoutSettings = this.timeoutSettings;
    this.clock = new ClockImpl(this);
    this.credentials = new CredentialsImpl(this);

    this.closedPromise = new WaitableEvent<>(this, Events.BrowserContext.CLOSE);

    Map<String, String> mapping = new LinkedHashMap<>();
    mapping.put(Events.BrowserContext.CONSOLE, "console");
    mapping.put(Events.BrowserContext.DIALOG, "dialog");
    mapping.put(Events.BrowserContext.DIALOG_CLOSED, "dialogClosed");
    mapping.put(Events.BrowserContext.REQUEST, "request");
    mapping.put(Events.BrowserContext.RESPONSE, "response");
    mapping.put(Events.BrowserContext.REQUEST_FINISHED, "requestFinished");
    mapping.put(Events.BrowserContext.REQUEST_FAILED, "requestFailed");
    setEventToSubscriptionMapping(mapping);
  }

  // java: upstream subscribes in the constructor with this._channel.on(...); the Java channel dispatches events here.
  @Override
  void handleEvent(String event, JsonObject params) {
    switch (event) {
      case "bindingCall": {
        BrowserContextBindingCallEvent e = connection.protocolGson().fromJson(params, BrowserContextBindingCallEvent.class);
        onBinding(e.binding);
        break;
      }
      case "close":
        onClose();
        break;
      case "page": {
        BrowserContextPageEvent e = connection.protocolGson().fromJson(params, BrowserContextPageEvent.class);
        onPage(e.page);
        break;
      }
      case "route": {
        BrowserContextRouteEvent e = connection.protocolGson().fromJson(params, BrowserContextRouteEvent.class);
        onRoute(e.route);
        break;
      }
      case "webSocketRoute": {
        BrowserContextWebSocketRouteEvent e = connection.protocolGson().fromJson(params, BrowserContextWebSocketRouteEvent.class);
        onWebSocketRoute(e.webSocketRoute);
        break;
      }
      case "serviceWorker": {
        BrowserContextServiceWorkerEvent e = connection.protocolGson().fromJson(params, BrowserContextServiceWorkerEvent.class);
        WorkerImpl serviceWorker = e.worker;
        // java: no service worker API, so the worker has no _context to set.
        serviceWorkers.add(serviceWorker);
        emit(Events.BrowserContext.SERVICE_WORKER, serviceWorker);
        break;
      }
      case "console": {
        BrowserContextConsoleEvent e = connection.protocolGson().fromJson(params, BrowserContextConsoleEvent.class);
        WorkerImpl worker = e.worker;
        PageImpl page = e.page;
        ConsoleMessageImpl consoleMessage = new ConsoleMessageImpl(e);
        if (worker != null) {
          worker.emit(Events.Worker.CONSOLE, consoleMessage);
        }
        if (page != null) {
          page.emit(Events.Page.CONSOLE, consoleMessage);
        }
        if (worker != null && serviceWorkers.contains(worker)) {
          String scope = serviceWorkerScope(worker);
          // java: a copy, a listener that closes a page would modify the set while it is iterated.
          for (PageImpl p : new ArrayList<>(pages)) {
            if (scope != null && p.url().startsWith(scope)) {
              p.emit(Events.Page.CONSOLE, consoleMessage);
            }
          }
        }
        emit(Events.BrowserContext.CONSOLE, consoleMessage);
        break;
      }
      case "pageError": {
        BrowserContextPageErrorEvent e = connection.protocolGson().fromJson(params, BrowserContextPageErrorEvent.class);
        PageImpl pageObject = e.page;
        // java: the error is the formatted string that Page.onPageError and WebError.error() return.
        String parsedError = Serialization.parseError(params.getAsJsonObject("error"));
        WebErrorLocation location = null;
        if (params.has("location")) {
          location = gson().fromJson(params.getAsJsonObject("location"), WebErrorLocation.class);
        }
        emit(Events.BrowserContext.WEB_ERROR, new WebErrorImpl(pageObject, parsedError, location));
        if (pageObject != null) {
          pageObject.emit(Events.Page.PAGE_ERROR, parsedError);
        }
        break;
      }
      case "dialog": {
        BrowserContextDialogEvent e = connection.protocolGson().fromJson(params, BrowserContextDialogEvent.class);
        DialogImpl dialogObject = e.dialog;
        // java: emit() does not report whether there were listeners, they are counted before.
        boolean hasListeners = listenerCount(Events.BrowserContext.DIALOG) > 0;
        emit(Events.BrowserContext.DIALOG, dialogObject);
        PageImpl page = dialogObject.page();
        if (page != null) {
          hasListeners = page.listenerCount(Events.Page.DIALOG) > 0 || hasListeners;
          page.emit(Events.Page.DIALOG, dialogObject);
        }
        if (!hasListeners) {
          // Although we do similar handling on the server side, we still need this logic
          // on the client side due to a possible race condition between two async calls:
          // a) removing "dialog" listener subscription (client->server)
          // b) actual "dialog" event (server->client)
          try {
            if ("beforeunload".equals(dialogObject.type())) {
              dialogObject.channel.accept(new DialogAcceptParams(), NO_TIMEOUT);
            } else {
              dialogObject.channel.dismiss(new DialogDismissParams(), NO_TIMEOUT);
            }
          } catch (PlaywrightException ex) {
          }
        }
        break;
      }
      case "dialogClosed": {
        BrowserContextDialogClosedEvent e = connection.protocolGson().fromJson(params, BrowserContextDialogClosedEvent.class);
        DialogImpl dialogObject = e.dialog;
        emit(Events.BrowserContext.DIALOG_CLOSED, dialogObject);
        PageImpl page = dialogObject.page();
        if (page != null) {
          page.emit(Events.Page.DIALOG_CLOSED, dialogObject);
        }
        break;
      }
      case "request": {
        BrowserContextRequestEvent e = connection.protocolGson().fromJson(params, BrowserContextRequestEvent.class);
        onRequest(e.request, e.page);
        break;
      }
      case "requestFailed": {
        BrowserContextRequestFailedEvent e = connection.protocolGson().fromJson(params, BrowserContextRequestFailedEvent.class);
        onRequestFailed(e.request, e.responseEndTiming, e.failureText, e.page);
        break;
      }
      case "requestFinished": {
        BrowserContextRequestFinishedEvent e = connection.protocolGson().fromJson(params, BrowserContextRequestFinishedEvent.class);
        onRequestFinished(e);
        break;
      }
      case "response": {
        BrowserContextResponseEvent e = connection.protocolGson().fromJson(params, BrowserContextResponseEvent.class);
        onResponse(e.response, e.page);
        break;
      }
      // java: no recorder API, the recorderEvent event is not handled.
    }
  }

  // java: upstream takes options.recordHar; Java has the flat recordHar* options.
  void initializeHarFromOptions(Browser.NewContextOptions recordHar) {
    if (recordHar == null || recordHar.recordHarPath == null) {
      return;
    }
    HarContentPolicy defaultContent = Pattern.compile("\\.zip$", Pattern.CASE_INSENSITIVE).matcher(recordHar.recordHarPath.toString()).find() ? HarContentPolicy.ATTACH : HarContentPolicy.EMBED;
    HarContentPolicy content = recordHar.recordHarContent != null ? recordHar.recordHarContent
      : (recordHar.recordHarOmitContent != null && recordHar.recordHarOmitContent ? HarContentPolicy.OMIT : defaultContent);
    tracing.recordIntoHAR(recordHar.recordHarPath, null, recordHar.recordHarUrlFilter, content,
      recordHar.recordHarMode != null ? recordHar.recordHarMode : HarMode.FULL, null);
  }

  private void onPage(PageImpl page) {
    pages.add(page);
    emit(Events.BrowserContext.PAGE, page);
    if (page.opener != null && !page.opener.isClosed()) {
      page.opener.emit(Events.Page.POPUP, page);
    }
  }

  private void onRequest(RequestImpl request, PageImpl page) {
    emit(Events.BrowserContext.REQUEST, request);
    if (page != null) {
      page.emit(Events.Page.REQUEST, request);
    }
  }

  private void onResponse(ResponseImpl response, PageImpl page) {
    emit(Events.BrowserContext.RESPONSE, response);
    if (page != null) {
      page.emit(Events.Page.RESPONSE, response);
    }
  }

  private void onRequestFailed(RequestImpl request, Double responseEndTiming, String failureText, PageImpl page) {
    request.failureText = failureText;
    request.setResponseEndTiming(responseEndTiming);
    emit(Events.BrowserContext.REQUEST_FAILED, request);
    if (page != null) {
      page.emit(Events.Page.REQUEST_FAILED, request);
    }
  }

  private void onRequestFinished(BrowserContextRequestFinishedEvent params) {
    Double responseEndTiming = params.responseEndTiming;
    RequestImpl request = params.request;
    ResponseImpl response = params.response;
    PageImpl page = params.page;
    request.setResponseEndTiming(responseEndTiming);
    emit(Events.BrowserContext.REQUEST_FINISHED, request);
    if (page != null) {
      page.emit(Events.Page.REQUEST_FINISHED, request);
    }
    if (response != null) {
      response.finishedPromise.complete(null);
    }
  }

  void onRoute(RouteImpl route) {
    route.context = this;
    PageImpl page = ((RequestImpl) route.request()).safePage();
    List<RouteHandler> routeHandlers = this.routes;
    for (RouteHandler routeHandler : new ArrayList<>(routeHandlers)) {
      // If the page or the context was closed we stall all requests right away.
      if ((page != null && page.closeWasCalled) || isClosed()) {
        return;
      }
      if (!routeHandler.matches(route.request().url())) {
        continue;
      }
      int index = this.routes.indexOf(routeHandler);
      if (index == -1) {
        continue;
      }
      if (routeHandler.willExpire()) {
        this.routes.remove(index);
      }
      boolean handled = routeHandler.handle(route);
      // unrouteAll() replaces _routes and updates interception after waiting for handlers.
      if (routeHandlers == this.routes && this.routes.isEmpty()) {
        try {
          disableInterception();
        } catch (PlaywrightException e) {
        }
      }
      if (handled) {
        return;
      }
    }
    // If the page is closed or unrouteAll() was called without waiting and interception disabled,
    // the method will throw an error - silence it.
    try {
      route.innerContinue(true /* isFallback */);
    } catch (PlaywrightException e) {
    }
  }

  void onWebSocketRoute(WebSocketRouteImpl webSocketRoute) {
    WebSocketRouteHandler routeHandler = null;
    for (WebSocketRouteHandler route : webSocketRoutes) {
      if (route.matches(webSocketRoute.url())) {
        routeHandler = route;
        break;
      }
    }
    if (routeHandler != null) {
      routeHandler.handle(webSocketRoute);
    } else {
      webSocketRoute.connectToServer();
    }
  }

  void onBinding(BindingCall bindingCall) {
    BindingCallback func = bindings.get(bindingCall.bindingCallInitializer.name);
    if (func == null) {
      return;
    }
    bindingCall.call(func);
  }

  private String serviceWorkerScope(WorkerImpl serviceWorker) {
    try {
      String url = new URL(new URL(serviceWorker.url()), ".").toString();
      if (!url.endsWith("/")) {
        url += "/";
      }
      return url;
    } catch (MalformedURLException e) {
      return null;
    }
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
  public BrowserImpl browser() {
    return browser;
  }

  @Override
  public List<Page> pages() {
    return new ArrayList<>(pages);
  }

  @Override
  public boolean isClosed() {
    return !"none".equals(closingStatus);
  }

  @Override
  public PageImpl newPage() {
    if (ownerPage != null) {
      throw new PlaywrightException("Please use browser.newContext()");
    }
    return channel.newPage(new BrowserContextNewPageParams(), NO_TIMEOUT).page;
  }

  @Override
  public List<Cookie> cookies(String urls) {
    return cookies(urls == null ? new ArrayList<>() : Collections.singletonList(urls));
  }

  @Override
  public List<Cookie> cookies(List<String> urls) {
    if (urls == null) {
      urls = new ArrayList<>();
    }
    BrowserContextCookiesParams params = new BrowserContextCookiesParams();
    params.urls = urls;
    List<NetworkCookie> cookies = channel.cookies(params, NO_TIMEOUT).cookies;
    // java: the protocol cookies are converted to the public Cookie type.
    return Arrays.asList(gson().fromJson(connection.protocolGson().toJsonTree(cookies), Cookie[].class));
  }

  @Override
  public void addCookies(List<Cookie> cookies) {
    BrowserContextAddCookiesParams params = new BrowserContextAddCookiesParams();
    // java: the public Cookie type is converted to the protocol one.
    params.cookies = connection.protocolGson().fromJson(gson().toJsonTree(cookies), new TypeToken<List<SetNetworkCookie>>() {}.getType());
    channel.addCookies(params, NO_TIMEOUT);
  }

  @Override
  public void clearCookies(ClearCookiesOptions options) {
    if (options == null) {
      options = new ClearCookiesOptions();
    }
    BrowserContextClearCookiesParams params = connection.protocolGson().fromJson(
      Serialization.toClearCookiesParams(options.name, options.domain, options.path), BrowserContextClearCookiesParams.class);
    channel.clearCookies(params, NO_TIMEOUT);
  }

  @Override
  public void grantPermissions(List<String> permissions, GrantPermissionsOptions options) {
    if (options == null) {
      options = new GrantPermissionsOptions();
    }
    BrowserContextGrantPermissionsParams params = toProtocol(options, BrowserContextGrantPermissionsParams.class);
    params.permissions = permissions;
    channel.grantPermissions(params, NO_TIMEOUT);
  }

  @Override
  public void clearPermissions() {
    channel.clearPermissions(new BrowserContextClearPermissionsParams(), NO_TIMEOUT);
  }

  @Override
  public void setGeolocation(Geolocation geolocation) {
    BrowserContextSetGeolocationParams params = new BrowserContextSetGeolocationParams();
    params.geolocation = geolocation == null ? null
      : toProtocol(geolocation, BrowserContextSetGeolocationParams.BrowserContextSetGeolocationParamsGeolocation.class);
    channel.setGeolocation(params, NO_TIMEOUT);
  }

  @Override
  public void setExtraHTTPHeaders(Map<String, String> headers) {
    RawHeaders.validateHeaders(headers);
    BrowserContextSetExtraHTTPHeadersParams params = new BrowserContextSetExtraHTTPHeadersParams();
    params.headers = Headers.headersObjectToArray(headers);
    channel.setExtraHTTPHeaders(params, NO_TIMEOUT);
  }

  @Override
  public void setOffline(boolean offline) {
    BrowserContextSetOfflineParams params = new BrowserContextSetOfflineParams();
    params.offline = offline;
    channel.setOffline(params, NO_TIMEOUT);
  }

  // java: setHTTPCredentials has no Java API.

  @Override
  public AutoCloseable addInitScript(String script) {
    return addInitScriptImpl(script);
  }

  @Override
  public AutoCloseable addInitScript(Path script) {
    return addInitScriptImpl(script);
  }

  // java: the core of the String and Path overloads; the exposeFunctions option and arg have no Java API.
  private AutoCloseable addInitScriptImpl(Object script) {
    String source = evaluationScript(script, true);
    BrowserContextAddInitScriptParams params = new BrowserContextAddInitScriptParams();
    params.source = source;
    return channel.addInitScript(params, NO_TIMEOUT).disposable;
  }

  @Override
  public AutoCloseable exposeBinding(String name, BindingCallback callback) {
    BrowserContextExposeBindingParams params = new BrowserContextExposeBindingParams();
    params.name = name;
    BrowserContextExposeBindingResult result = channel.exposeBinding(params, NO_TIMEOUT);
    bindings.put(name, callback);
    return result.disposable;
  }

  // java: _exposeCallbackBinding serves the exposeFunctions option of addInitScript, which has no Java API.

  @Override
  public AutoCloseable exposeFunction(String name, FunctionCallback callback) {
    BrowserContextExposeBindingParams params = new BrowserContextExposeBindingParams();
    params.name = name;
    BrowserContextExposeBindingResult result = channel.exposeBinding(params, NO_TIMEOUT);
    BindingCallback binding = (source, args) -> callback.call(args);
    bindings.put(name, binding);
    return result.disposable;
  }

  @Override
  public AutoCloseable route(String url, Consumer<Route> handler, RouteOptions options) {
    return route(UrlMatcher.forGlob(baseUrl(), url, connection.localUtils(), false), handler, options);
  }

  @Override
  public AutoCloseable route(Pattern url, Consumer<Route> handler, RouteOptions options) {
    return route(new UrlMatcher(url), handler, options);
  }

  @Override
  public AutoCloseable route(Predicate<String> url, Consumer<Route> handler, RouteOptions options) {
    return route(new UrlMatcher(url), handler, options);
  }

  AutoCloseable route(UrlMatcher url, Consumer<Route> handler, RouteOptions options) {
    return withTitle("Route requests", () -> {
      routes.add(0, new RouteHandler(this.options.baseURL, url, handler, options == null ? null : options.times));
      updateInterceptionPatterns();
      return new DisposableStub(() -> unroute(url, handler));
    });
  }

  @Override
  public void routeWebSocket(String url, Consumer<WebSocketRoute> handler) {
    routeWebSocket(UrlMatcher.forGlob(baseUrl(), url, connection.localUtils(), true), handler);
  }

  @Override
  public void routeWebSocket(Pattern url, Consumer<WebSocketRoute> handler) {
    routeWebSocket(new UrlMatcher(url), handler);
  }

  @Override
  public void routeWebSocket(Predicate<String> url, Consumer<WebSocketRoute> handler) {
    routeWebSocket(new UrlMatcher(url), handler);
  }

  private void routeWebSocket(UrlMatcher url, Consumer<WebSocketRoute> handler) {
    withTitle("Route WebSockets", () -> {
      webSocketRoutes.add(0, new WebSocketRouteHandler(options.baseURL, url, handler));
      updateWebSocketInterceptionPatterns();
    });
  }

  @Override
  public void routeFromHAR(Path har, RouteFromHAROptions options) {
    if (options == null) {
      options = new RouteFromHAROptions();
    }
    LocalUtils localUtils = connection.localUtils();
    if (localUtils == null) {
      throw new PlaywrightException("Route from har is not supported in thin clients");
    }
    if (options.update != null && options.update) {
      tracing.recordIntoHAR(har, null, options.url, convertType(options.updateContent, HarContentPolicy.class), options.updateMode, null);
      return;
    }
    HarRouter harRouter = HarRouter.create(localUtils, har, options.notFound != null ? options.notFound : HarNotFound.ABORT, options.url);
    harRouters.add(harRouter);
    harRouter.addContextRoute(this);
  }

  private void disposeHarRouters() {
    for (HarRouter router : harRouters) {
      router.dispose();
    }
    harRouters = new ArrayList<>();
  }

  @Override
  public void unrouteAll() {
    withTitle("Unroute requests", () -> {
      unrouteInternal(routes, new ArrayList<>());
      disposeHarRouters();
    });
  }

  @Override
  public void unroute(String url, Consumer<Route> handler) {
    unroute(UrlMatcher.forGlob(baseUrl(), url, connection.localUtils(), false), handler);
  }

  @Override
  public void unroute(Pattern url, Consumer<Route> handler) {
    unroute(new UrlMatcher(url), handler);
  }

  @Override
  public void unroute(Predicate<String> url, Consumer<Route> handler) {
    unroute(new UrlMatcher(url), handler);
  }

  void unroute(UrlMatcher url, Consumer<Route> handler) {
    withTitle("Unroute requests", () -> {
      List<RouteHandler> removed = new ArrayList<>();
      List<RouteHandler> remaining = new ArrayList<>();
      for (RouteHandler route : routes) {
        if (route.url.equals(url) && (handler == null || route.handler == handler)) {
          removed.add(route);
        } else {
          remaining.add(route);
        }
      }
      unrouteInternal(removed, remaining);
    });
  }

  // java: unrouteAll() has no behavior option, so the removed handlers are never stopped.
  private void unrouteInternal(List<RouteHandler> removed, List<RouteHandler> remaining) {
    routes = remaining;
    updateInterceptionPatterns();
  }

  private void disableInterception() {
    wrapApiCall(() -> updateInterceptionPatterns(), null, true);
  }

  private void updateInterceptionPatterns() {
    BrowserContextSetNetworkInterceptionPatternsParams params = new BrowserContextSetNetworkInterceptionPatternsParams();
    params.patterns = RouteHandler.prepareInterceptionPatterns(routes);
    channel.setNetworkInterceptionPatterns(params, NO_TIMEOUT);
  }

  private void updateWebSocketInterceptionPatterns() {
    BrowserContextSetWebSocketInterceptionPatternsParams params = new BrowserContextSetWebSocketInterceptionPatternsParams();
    params.patterns = WebSocketRouteHandler.prepareInterceptionPatterns(webSocketRoutes);
    channel.setWebSocketInterceptionPatterns(params, NO_TIMEOUT);
  }

  String effectiveCloseReason() {
    if (closeReason != null) {
      return closeReason;
    }
    return browser != null ? browser.closeReason : null;
  }

  // java: the predicate and timeout are passed separately and the code to run is a Runnable.
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
  public String storageState(StorageStateOptions options) {
    if (options == null) {
      options = new StorageStateOptions();
    }
    BrowserContextStorageStateParams params = new BrowserContextStorageStateParams();
    params.indexedDB = options.indexedDB;
    params.opfs = options.opfs;
    params.credentials = options.credentials;
    // java: the state is returned as the raw JSON of the reply: a typed round trip would print -1 as -1.0
    // and differ from the string of APIRequestContext.storageState().
    JsonElement state = sendMessage("storageState", connection.protocolGson().toJsonTree(params).getAsJsonObject(), NO_TIMEOUT);
    String storageState = state.toString();
    if (options.path != null) {
      FileUtils.mkdirIfNeeded(options.path);
      Utils.writeToFile(Serialization.toPrettyJson(storageState).getBytes(StandardCharsets.UTF_8), options.path);
    }
    return storageState;
  }

  @Override
  public void setStorageState(Path storageState) {
    JsonElement state = prepareStorageState(storageState);
    BrowserContextSetStorageStateParams params = new BrowserContextSetStorageStateParams();
    params.storageState = connection.protocolGson().fromJson(state, BrowserContextSetStorageStateParams.BrowserContextSetStorageStateParamsStorageState.class);
    channel.setStorageState(params, NO_TIMEOUT);
  }

  @Override
  public List<Page> backgroundPages() {
    return Collections.emptyList();
  }

  // java: serviceWorkers() has no Java API.

  @Override
  public CDPSession newCDPSession(Page page) {
    BrowserContextNewCDPSessionParams params = new BrowserContextNewCDPSessionParams();
    params.page = (PageImpl) page;
    return channel.newCDPSession(params, NO_TIMEOUT).session;
  }

  @Override
  public CDPSession newCDPSession(Frame page) {
    BrowserContextNewCDPSessionParams params = new BrowserContextNewCDPSessionParams();
    params.frame = (FrameImpl) page;
    return channel.newCDPSession(params, NO_TIMEOUT).session;
  }

  void onClose() {
    closingStatus = "closed";
    if (browser != null) {
      browser.contexts.remove(this);
      browser.browserType.contexts.remove(this);
      browser.browserType.playwright.selectors.contextsForSelectors.remove(this);
    }
    disposeHarRouters();
    tracing.resetStackCounter();
    request.tracing.resetStackCounter();
    emit(Events.BrowserContext.CLOSE, this);
  }

  @Override
  public void close(CloseOptions options) {
    if (options == null) {
      options = new CloseOptions();
    }
    if (isClosed()) {
      return;
    }
    closeReason = options.reason;
    closingStatus = "closing";
    request.dispose(convertType(options, APIRequestContext.DisposeOptions.class));
    RuntimeException harError = null;
    try {
      tracing.exportAllHars();
    } catch (RuntimeException e) {
      harError = e;
    }
    channel.close(toProtocol(options, BrowserContextCloseParams.class), NO_TIMEOUT);
    runUntil(() -> {}, closedPromise);
    if (harError != null) {
      throw harError;
    }
  }

  // java: showRecorder, startRecording, stopRecording and exposeConsoleApi have no Java API.

  // java: upstream reads a path given as a string and passes an object through; Java options hold a Path.
  static JsonElement prepareStorageState(Path storageState) {
    try {
      return JsonParser.parseString(new String(Files.readAllBytes(storageState), StandardCharsets.UTF_8));
    } catch (IOException e) {
      // java: the message the Java client always had, tests match on it.
      throw new PlaywrightException("Failed to read storage state from file", e);
    } catch (JsonParseException e) {
      throw new PlaywrightException("Error reading storage state from " + storageState + ":\n" + e.getMessage(), e);
    }
  }

  // java: the result is the params of Browser.newContext, BrowserType.launchPersistentContext copies it into its own params.
  static BrowserNewContextParams prepareBrowserContextParams(Browser.NewContextOptions options) {
    if (options == null) {
      options = new Browser.NewContextOptions();
    }
    if (options.extraHTTPHeaders != null) {
      RawHeaders.validateHeaders(options.extraHTTPHeaders);
    }
    // java: the options whose Java type differs from the protocol are converted one by one below,
    // the others are copied by toProtocol, which also maps an empty Optional to 'no-override'
    // for colorScheme, reducedMotion, forcedColors and contrast.
    Browser.NewContextOptions rest = Utils.clone(options);
    rest.viewportSize = null;
    rest.extraHTTPHeaders = null;
    rest.screenSize = null;
    rest.storageState = null;
    rest.storageStatePath = null;
    rest.acceptDownloads = null;
    rest.clientCertificates = null;
    rest.httpCredentials = null;
    rest.recordVideoDir = null;
    rest.recordVideoSize = null;
    rest.recordVideoFps = null;
    rest.recordHarContent = null;
    rest.recordHarMode = null;
    rest.recordHarOmitContent = null;
    rest.recordHarPath = null;
    rest.recordHarUrlFilter = null;
    BrowserNewContextParams contextParams = toProtocol(rest, BrowserNewContextParams.class);
    if (options.viewportSize != null && options.viewportSize.isPresent()) {
      contextParams.viewport = new BrowserNewContextParams.BrowserNewContextParamsViewport();
      contextParams.viewport.width = options.viewportSize.get().width;
      contextParams.viewport.height = options.viewportSize.get().height;
    }
    contextParams.noDefaultViewport = options.viewportSize != null && !options.viewportSize.isPresent();
    contextParams.extraHTTPHeaders = options.extraHTTPHeaders != null ? Headers.headersObjectToArray(options.extraHTTPHeaders) : null;
    if (options.screenSize != null) {
      contextParams.screen = new BrowserNewContextParams.BrowserNewContextParamsScreen();
      contextParams.screen.width = options.screenSize.width;
      contextParams.screen.height = options.screenSize.height;
    }
    // java: storageState is the JSON text of the state, storageStatePath the file to read it from.
    JsonElement storageState = null;
    if (options.storageState != null) {
      storageState = JsonParser.parseString(options.storageState);
    } else if (options.storageStatePath != null) {
      storageState = prepareStorageState(options.storageStatePath);
    }
    if (storageState != null) {
      contextParams.storageState = toProtocol(storageState, BrowserNewContextParams.BrowserNewContextParamsStorageState.class);
    }
    contextParams.acceptDownloads = toAcceptDownloadsProtocol(options.acceptDownloads);
    List<PlaywrightNewRequestParams.PlaywrightNewRequestParamsClientCertificates> clientCertificates = toClientCertificatesProtocol(options.clientCertificates);
    if (clientCertificates != null) {
      contextParams.clientCertificates = new ArrayList<>();
      for (PlaywrightNewRequestParams.PlaywrightNewRequestParamsClientCertificates cert : clientCertificates) {
        contextParams.clientCertificates.add(convertType(cert, BrowserNewContextParams.BrowserNewContextParamsClientCertificates.class));
      }
    }
    contextParams.httpCredentials = toHttpCredentialsProtocol(options.httpCredentials);
    // java: recordVideo is the flat recordVideoDir, recordVideoSize and recordVideoFps options; a size or fps
    // without a dir is an error instead of being dropped.
    if (options.recordVideoDir != null) {
      contextParams.recordVideo = new BrowserNewContextParams.BrowserNewContextParamsRecordVideo();
      contextParams.recordVideo.dir = options.recordVideoDir.toAbsolutePath().toString();
      if (options.recordVideoSize != null) {
        contextParams.recordVideo.size = new BrowserNewContextParams.BrowserNewContextParamsRecordVideo.BrowserNewContextParamsRecordVideoSize();
        contextParams.recordVideo.size.width = options.recordVideoSize.width;
        contextParams.recordVideo.size.height = options.recordVideoSize.height;
      }
      contextParams.recordVideo.fps = options.recordVideoFps;
    } else if (options.recordVideoSize != null) {
      throw new PlaywrightException("recordVideoSize is set but recordVideoDir is null");
    } else if (options.recordVideoFps != null) {
      throw new PlaywrightException("recordVideoFps is set but recordVideoDir is null");
    }
    // java: the flat recordHar* options are checked here, initializeHarFromOptions starts the recording.
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
    }
    return contextParams;
  }

  // java: the option is a single HttpCredentials or a list of them.
  @SuppressWarnings("unchecked")
  static List<HttpCredentials> toHttpCredentialsProtocol(Object credentials) {
    if (credentials == null) {
      return null;
    }
    List<com.microsoft.playwright.options.HttpCredentials> list = credentials instanceof List
      ? (List<com.microsoft.playwright.options.HttpCredentials>) credentials
      : Collections.singletonList((com.microsoft.playwright.options.HttpCredentials) credentials);
    if (list.isEmpty()) {
      return null;
    }
    // java: converted to the protocol struct, the send enum becomes its lower-case name.
    return gson().fromJson(gson().toJsonTree(list), new TypeToken<List<HttpCredentials>>() {}.getType());
  }

  private static String toAcceptDownloadsProtocol(Boolean acceptDownloads) {
    if (acceptDownloads == null) {
      return null;
    }
    if (acceptDownloads) {
      return "accept";
    }
    return "deny";
  }

  static List<PlaywrightNewRequestParams.PlaywrightNewRequestParamsClientCertificates> toClientCertificatesProtocol(List<ClientCertificate> certs) {
    if (certs == null) {
      return null;
    }
    List<PlaywrightNewRequestParams.PlaywrightNewRequestParamsClientCertificates> result = new ArrayList<>();
    for (ClientCertificate cert : certs) {
      PlaywrightNewRequestParams.PlaywrightNewRequestParamsClientCertificates c = new PlaywrightNewRequestParams.PlaywrightNewRequestParamsClientCertificates();
      c.origin = cert.origin;
      c.cert = bufferizeContent(cert.cert, cert.certPath);
      c.key = bufferizeContent(cert.key, cert.keyPath);
      c.pfx = bufferizeContent(cert.pfx, cert.pfxPath);
      c.passphrase = cert.passphrase;
      result.add(c);
    }
    return result;
  }

  // java: the local function of toClientCertificatesProtocol.
  private static byte[] bufferizeContent(byte[] value, Path filePath) {
    if (value != null) {
      return value;
    }
    if (filePath != null) {
      try {
        return Files.readAllBytes(filePath);
      } catch (IOException e) {
        throw new PlaywrightException("Failed to read from file", e);
      }
    }
    return null;
  }

  // java: members of the generated BrowserContext interface without an upstream counterpart:
  // the fields' accessors, typed listeners, callback waits, waitForCondition, and the parsed baseURL.
  @Override
  public ClockImpl clock() {
    return clock;
  }

  @Override
  public Credentials credentials() {
    return credentials;
  }

  @Override
  public DebuggerImpl debugger() {
    return debugger;
  }

  @Override
  public APIRequestContextImpl request() {
    return request;
  }

  @Override
  public TracingImpl tracing() {
    return tracing;
  }

  URL baseUrl() {
    if (options.baseURL != null) {
      try {
        return new URL(options.baseURL);
      } catch (MalformedURLException e) {
      }
    }
    return null;
  }

  @Override
  public void onBackgroundPage(Consumer<Page> handler) {
    on(Events.BrowserContext.BACKGROUND_PAGE, handler);
  }

  @Override
  public void offBackgroundPage(Consumer<Page> handler) {
    off(Events.BrowserContext.BACKGROUND_PAGE, handler);
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
  public void onDownload(Consumer<Download> handler) {
    on(Events.BrowserContext.DOWNLOAD, handler);
  }

  @Override
  public void offDownload(Consumer<Download> handler) {
    off(Events.BrowserContext.DOWNLOAD, handler);
  }

  @Override
  public void onFrameAttached(Consumer<Frame> handler) {
    on(Events.BrowserContext.FRAME_ATTACHED, handler);
  }

  @Override
  public void offFrameAttached(Consumer<Frame> handler) {
    off(Events.BrowserContext.FRAME_ATTACHED, handler);
  }

  @Override
  public void onFrameDetached(Consumer<Frame> handler) {
    on(Events.BrowserContext.FRAME_DETACHED, handler);
  }

  @Override
  public void offFrameDetached(Consumer<Frame> handler) {
    off(Events.BrowserContext.FRAME_DETACHED, handler);
  }

  @Override
  public void onFrameNavigated(Consumer<Frame> handler) {
    on(Events.BrowserContext.FRAME_NAVIGATED, handler);
  }

  @Override
  public void offFrameNavigated(Consumer<Frame> handler) {
    off(Events.BrowserContext.FRAME_NAVIGATED, handler);
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
  public void onPageClose(Consumer<Page> handler) {
    on(Events.BrowserContext.PAGE_CLOSE, handler);
  }

  @Override
  public void offPageClose(Consumer<Page> handler) {
    off(Events.BrowserContext.PAGE_CLOSE, handler);
  }

  @Override
  public void onPageLoad(Consumer<Page> handler) {
    on(Events.BrowserContext.PAGE_LOAD, handler);
  }

  @Override
  public void offPageLoad(Consumer<Page> handler) {
    off(Events.BrowserContext.PAGE_LOAD, handler);
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
  public Page waitForPage(WaitForPageOptions options, Runnable callback) {
    if (options == null) {
      options = new WaitForPageOptions();
    }
    return waitForEvent(Events.BrowserContext.PAGE, options.predicate, options.timeout, callback);
  }

  @Override
  public ConsoleMessage waitForConsoleMessage(WaitForConsoleMessageOptions options, Runnable callback) {
    if (options == null) {
      options = new WaitForConsoleMessageOptions();
    }
    return waitForEvent(Events.BrowserContext.CONSOLE, options.predicate, options.timeout, callback);
  }

  @Override
  public void waitForCondition(BooleanSupplier condition, WaitForConditionOptions options) {
    List<Waitable<Void>> waitables = new ArrayList<>();
    waitables.add(new WaitableEvent<Void>(this, Events.BrowserContext.CLOSE) {
      @Override
      public Void get() {
        throw new TargetClosedError(effectiveCloseReason());
      }
    });
    waitables.add(timeoutSettings.createWaitable(options == null ? null : options.timeout));
    waitables.add(new WaitablePredicate<>(condition));
    runUntil(() -> {}, new WaitableRace<>(waitables));
  }
}
