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

// Port of page.ts.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.impl.isomorphic.Headers;
import com.google.gson.JsonObject;
import com.microsoft.playwright.*;
import com.microsoft.playwright.impl.protocol.*;
import com.microsoft.playwright.options.*;

import java.nio.file.Path;
import java.util.ArrayList;
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
import static com.microsoft.playwright.impl.isomorphic.StringUtils.trimStringWithEllipsis;
import static com.microsoft.playwright.impl.Utils.convertType;
import static com.microsoft.playwright.impl.Utils.toJsRegexFlags;
import static com.microsoft.playwright.impl.Utils.toProtocol;

public class PageImpl extends ChannelOwner implements Page {
  final PageChannel channel = new PageChannel(this);
  private final BrowserContextImpl browserContext;
  BrowserContextImpl ownedContext;

  private final FrameImpl mainFrame;
  private final Set<FrameImpl> frames = new LinkedHashSet<>();
  final Set<WorkerImpl> workers = new LinkedHashSet<>();
  private boolean closed;
  final LongStandingScope closedOrCrashedScope = new LongStandingScope(this);
  private ViewportSize viewportSize;
  List<RouteHandler> routes = new ArrayList<>();
  List<WebSocketRouteHandler> webSocketRoutes = new ArrayList<>();

  final KeyboardImpl keyboard;
  final MouseImpl mouse;
  final APIRequestContextImpl request;
  final TouchscreenImpl touchscreen;
  final ClockImpl clock;
  final ScreencastImpl screencast;
  final WebStorageImpl localStorage;
  final WebStorageImpl sessionStorage;
  final WebMCPImpl webmcp;

  final Map<String, BindingCallback> bindings = new LinkedHashMap<>();
  final TimeoutSettings timeoutSettings;
  private final VideoImpl video;
  final PageImpl opener;
  private String closeReason;
  boolean closeWasCalled;
  private List<HarRouter> harRouters = new ArrayList<>();

  private final Map<Integer, LocatorHandler> locatorHandlers = new LinkedHashMap<>();

  PageImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    PageInitializer pageInitializer = connection.protocolGson().fromJson(initializer, PageInitializer.class);
    browserContext = (BrowserContextImpl) parent;
    timeoutSettings = new TimeoutSettings(browserContext.timeoutSettings);

    keyboard = new KeyboardImpl(this);
    mouse = new MouseImpl(this);
    request = browserContext.request;
    touchscreen = new TouchscreenImpl(this);
    clock = browserContext.clock;
    localStorage = new WebStorageImpl(this, "local");
    sessionStorage = new WebStorageImpl(this, "session");

    mainFrame = pageInitializer.mainFrame;
    mainFrame.page = this;
    webmcp = mainFrame.webmcp;
    frames.add(mainFrame);
    if (pageInitializer.viewportSize != null) {
      viewportSize = new ViewportSize(pageInitializer.viewportSize.width, pageInitializer.viewportSize.height);
    }
    closed = Boolean.TRUE.equals(pageInitializer.isClosed);
    opener = pageInitializer.opener;
    video = new VideoImpl(this, pageInitializer.video);
    screencast = new ScreencastImpl(this);

    on(Events.Page.CLOSE, new Consumer<Page>() {
      @Override
      public void accept(Page page) {
        off(Events.Page.CLOSE, this);
        closedOrCrashedScope.close(closeErrorWithReason());
      }
    });
    on(Events.Page.CRASH, new Consumer<Page>() {
      @Override
      public void accept(Page page) {
        off(Events.Page.CRASH, this);
        closedOrCrashedScope.close(new TargetClosedError());
      }
    });

    Map<String, String> mapping = new LinkedHashMap<>();
    mapping.put(Events.Page.CONSOLE, "console");
    mapping.put(Events.Page.DIALOG, "dialog");
    mapping.put(Events.Page.DIALOG_CLOSED, "dialogClosed");
    mapping.put(Events.Page.REQUEST, "request");
    mapping.put(Events.Page.RESPONSE, "response");
    mapping.put(Events.Page.REQUEST_FINISHED, "requestFinished");
    mapping.put(Events.Page.REQUEST_FAILED, "requestFailed");
    mapping.put(Events.Page.FILE_CHOOSER, "fileChooser");
    setEventToSubscriptionMapping(mapping);
  }

  // java: upstream subscribes in the constructor with this._channel.on(...); the Java channel dispatches events here.
  @Override
  void handleEvent(String event, JsonObject params) {
    switch (event) {
      case "bindingCall": {
        PageBindingCallEvent e = connection.protocolGson().fromJson(params, PageBindingCallEvent.class);
        onBinding(e.binding);
        break;
      }
      case "close":
        onClose();
        break;
      case "crash":
        onCrash();
        break;
      case "download": {
        PageDownloadEvent e = connection.protocolGson().fromJson(params, PageDownloadEvent.class);
        // java: DownloadImpl reads url and suggestedFilename from the event params.
        DownloadImpl download = new DownloadImpl(this, e.artifact, params);
        emit(Events.Page.DOWNLOAD, download);
        browserContext.emit(Events.BrowserContext.DOWNLOAD, download);
        break;
      }
      case "fileChooser": {
        PageFileChooserEvent e = connection.protocolGson().fromJson(params, PageFileChooserEvent.class);
        emit(Events.Page.FILE_CHOOSER, new FileChooserImpl(this, e.element, Boolean.TRUE.equals(e.isMultiple)));
        break;
      }
      case "frameAttached": {
        PageFrameAttachedEvent e = connection.protocolGson().fromJson(params, PageFrameAttachedEvent.class);
        onFrameAttached(e.frame);
        break;
      }
      case "frameDetached": {
        PageFrameDetachedEvent e = connection.protocolGson().fromJson(params, PageFrameDetachedEvent.class);
        onFrameDetached(e.frame);
        break;
      }
      case "locatorHandlerTriggered": {
        PageLocatorHandlerTriggeredEvent e = connection.protocolGson().fromJson(params, PageLocatorHandlerTriggeredEvent.class);
        onLocatorHandlerTriggered(e.uid);
        break;
      }
      case "route": {
        PageRouteEvent e = connection.protocolGson().fromJson(params, PageRouteEvent.class);
        onRoute(e.route);
        break;
      }
      case "webSocketRoute": {
        PageWebSocketRouteEvent e = connection.protocolGson().fromJson(params, PageWebSocketRouteEvent.class);
        onWebSocketRoute(e.webSocketRoute);
        break;
      }
      case "viewportSizeChanged": {
        PageViewportSizeChangedEvent e = connection.protocolGson().fromJson(params, PageViewportSizeChangedEvent.class);
        viewportSize = e.viewportSize == null ? null : new ViewportSize(e.viewportSize.width, e.viewportSize.height);
        break;
      }
      case "webSocket": {
        PageWebSocketEvent e = connection.protocolGson().fromJson(params, PageWebSocketEvent.class);
        emit(Events.Page.WEB_SOCKET, e.webSocket);
        break;
      }
      case "worker": {
        PageWorkerEvent e = connection.protocolGson().fromJson(params, PageWorkerEvent.class);
        onWorker(e.worker);
        break;
      }
      // java: upstream's Screencast subscribes to the page channel itself.
      case "screencastFrame":
        screencast.handleScreencastFrame(params);
        break;
      default:
        break;
    }
  }

  private void onFrameAttached(FrameImpl frame) {
    frame.page = this;
    frames.add(frame);
    if (frame.parentFrame != null) {
      frame.parentFrame.childFrames.add(frame);
    }
    emit(Events.Page.FRAME_ATTACHED, frame);
    browserContext.emit(Events.BrowserContext.FRAME_ATTACHED, frame);
  }

  private void onFrameDetached(FrameImpl frame) {
    frames.remove(frame);
    frame.detached = true;
    if (frame.parentFrame != null) {
      frame.parentFrame.childFrames.remove(frame);
    }
    emit(Events.Page.FRAME_DETACHED, frame);
    browserContext.emit(Events.BrowserContext.FRAME_DETACHED, frame);
  }

  private void onRoute(RouteImpl route) {
    route.context = context();
    List<RouteHandler> routeHandlers = routes;
    for (RouteHandler routeHandler : new ArrayList<>(routeHandlers)) {
      // If the page was closed we stall all requests right away.
      if (closeWasCalled || browserContext.isClosed()) {
        return;
      }
      if (!routeHandler.matches(route.request().url())) {
        continue;
      }
      int index = routes.indexOf(routeHandler);
      if (index == -1) {
        continue;
      }
      if (routeHandler.willExpire()) {
        routes.remove(index);
      }
      boolean handled = routeHandler.handle(route);
      // unrouteAll() replaces _routes and updates interception after waiting for handlers.
      if (routeHandlers == routes && routes.isEmpty()) {
        try {
          disableInterception();
        } catch (PlaywrightException e) {
        }
      }
      if (handled) {
        return;
      }
    }

    browserContext.onRoute(route);
  }

  private void onWebSocketRoute(WebSocketRouteImpl webSocketRoute) {
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
      browserContext.onWebSocketRoute(webSocketRoute);
    }
  }

  void onBinding(BindingCall bindingCall) {
    BindingCallback func = bindings.get(bindingCall.bindingCallInitializer.name);
    if (func != null) {
      bindingCall.call(func);
      return;
    }
    browserContext.onBinding(bindingCall);
  }

  void onWorker(WorkerImpl worker) {
    workers.add(worker);
    worker.page = this;
    emit(Events.Page.WORKER, worker);
  }

  void onClose() {
    closed = true;
    browserContext.pages.remove(this);
    disposeHarRouters();
    emit(Events.Page.CLOSE, this);
    browserContext.emit(Events.BrowserContext.PAGE_CLOSE, this);
  }

  private void onCrash() {
    emit(Events.Page.CRASH, this);
  }

  @Override
  public BrowserContextImpl context() {
    return browserContext;
  }

  @Override
  public PageImpl opener() {
    if (opener == null || opener.isClosed()) {
      return null;
    }
    return opener;
  }

  @Override
  public FrameImpl mainFrame() {
    return mainFrame;
  }

  @Override
  public Frame frame(String name) {
    return frame(name, null);
  }

  @Override
  public Frame frameByUrl(String url) {
    return frame(null, UrlMatcher.forGlob(browserContext.baseUrl(), url, connection.localUtils(), false));
  }

  @Override
  public Frame frameByUrl(Pattern url) {
    return frame(null, new UrlMatcher(url));
  }

  @Override
  public Frame frameByUrl(Predicate<String> url) {
    return frame(null, new UrlMatcher(url));
  }

  // java: frameSelector is a name or { name, url }; the Java API has frame(name) and frameByUrl(url).
  private Frame frame(String name, UrlMatcher url) {
    if ((name == null || name.isEmpty()) && url == null) {
      throw new PlaywrightException("Either name or url matcher should be specified");
    }
    for (Frame f : frames()) {
      if (name != null && !name.isEmpty() ? f.name().equals(name) : url.test(f.url())) {
        return f;
      }
    }
    return null;
  }

  @Override
  public List<Frame> frames() {
    return new ArrayList<>(frames);
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
  public VideoImpl video() {
    // Note: we are creating Video object lazily, because we do not know
    // BrowserContextOptions when constructing the page - it is assigned
    // too late during launchPersistentContext.
    if (browserContext.options.recordVideo == null) {
      return null;
    }
    return video;
  }

  @Override
  public Locator pickLocator() {
    PagePickLocatorResult result = channel.pickLocator(NO_TIMEOUT);
    return locator(result.selector);
  }

  @Override
  public void cancelPickLocator() {
    channel.cancelPickLocator(NO_TIMEOUT);
  }

  @Override
  public void hideHighlight() {
    channel.hideHighlight(NO_TIMEOUT);
  }

  @Override
  public ElementHandle querySelector(String selector, QuerySelectorOptions options) {
    return mainFrame.querySelector(selector, convertType(options, Frame.QuerySelectorOptions.class));
  }

  @Override
  public ElementHandle waitForSelector(String selector, WaitForSelectorOptions options) {
    return mainFrame.waitForSelector(selector, convertType(options, Frame.WaitForSelectorOptions.class));
  }

  @Override
  public void dispatchEvent(String selector, String type, Object eventInit, DispatchEventOptions options) {
    mainFrame.dispatchEvent(selector, type, eventInit, convertType(options, Frame.DispatchEventOptions.class));
  }

  @Override
  public JSHandle evaluateHandle(String expression, Object arg) {
    return mainFrame.evaluateHandle(expression, arg);
  }

  @Override
  public Object evalOnSelector(String selector, String expression, Object arg, EvalOnSelectorOptions options) {
    return mainFrame.evalOnSelector(selector, expression, arg, convertType(options, Frame.EvalOnSelectorOptions.class));
  }

  @Override
  public Object evalOnSelectorAll(String selector, String expression, Object arg) {
    return mainFrame.evalOnSelectorAll(selector, expression, arg);
  }

  @Override
  public List<ElementHandle> querySelectorAll(String selector) {
    return mainFrame.querySelectorAll(selector);
  }

  @Override
  public ElementHandle addScriptTag(AddScriptTagOptions options) {
    return mainFrame.addScriptTag(convertType(options, Frame.AddScriptTagOptions.class));
  }

  @Override
  public ElementHandle addStyleTag(AddStyleTagOptions options) {
    return mainFrame.addStyleTag(convertType(options, Frame.AddStyleTagOptions.class));
  }

  @Override
  public AutoCloseable exposeFunction(String name, FunctionCallback callback) {
    PageExposeBindingParams params = new PageExposeBindingParams();
    params.name = name;
    PageExposeBindingResult result = channel.exposeBinding(params, NO_TIMEOUT);
    BindingCallback binding = (source, args) -> callback.call(args);
    bindings.put(name, binding);
    return result.disposable;
  }

  @Override
  public AutoCloseable exposeBinding(String name, BindingCallback callback) {
    PageExposeBindingParams params = new PageExposeBindingParams();
    params.name = name;
    PageExposeBindingResult result = channel.exposeBinding(params, NO_TIMEOUT);
    bindings.put(name, callback);
    return result.disposable;
  }

  // java: no exposeFunctions option for evaluate() or addInitScript(), so there is no _exposeCallbackBinding,
  // _exposeEvaluateCallback or _eraseEvaluateCallbacks.

  @Override
  public void setExtraHTTPHeaders(Map<String, String> headers) {
    RawHeaders.validateHeaders(headers);
    PageSetExtraHTTPHeadersParams params = new PageSetExtraHTTPHeadersParams();
    params.headers = Headers.headersObjectToArray(headers);
    channel.setExtraHTTPHeaders(params, NO_TIMEOUT);
  }

  @Override
  public String url() {
    return mainFrame.url();
  }

  @Override
  public String content(ContentOptions options) {
    return mainFrame.content(convertType(options, Frame.ContentOptions.class));
  }

  @Override
  public void setContent(String html, SetContentOptions options) {
    mainFrame.setContent(html, convertType(options, Frame.SetContentOptions.class));
  }

  @Override
  public Response navigate(String url, NavigateOptions options) {
    return mainFrame.navigate(url, convertType(options, Frame.NavigateOptions.class));
  }

  @Override
  public Response reload(ReloadOptions options) {
    if (options == null) {
      options = new ReloadOptions();
    }
    PageReloadParams params = toProtocol(options, PageReloadParams.class);
    params.waitUntil = FrameImpl.verifyLoadState("waitUntil", params.waitUntil == null ? "load" : params.waitUntil);
    return channel.reload(params, timeoutSettings.navigationTimeout(options.timeout)).response;
  }

  @Override
  public void addLocatorHandler(Locator locator, Consumer<Locator> handler, AddLocatorHandlerOptions options) {
    if (options == null) {
      options = new AddLocatorHandlerOptions();
    }
    LocatorImpl locatorImpl = (LocatorImpl) locator;
    if (locatorImpl.frame != mainFrame) {
      throw new PlaywrightException("Locator must belong to the main frame of this page");
    }
    if (options.times != null && options.times == 0) {
      return;
    }
    PageRegisterLocatorHandlerParams params = new PageRegisterLocatorHandlerParams();
    params.selector = locatorImpl.selector;
    params.noWaitAfter = options.noWaitAfter;
    int uid = channel.registerLocatorHandler(params, NO_TIMEOUT).uid;
    locatorHandlers.put(uid, new LocatorHandler(locatorImpl, handler, options.times));
  }

  private void onLocatorHandlerTriggered(int uid) {
    boolean remove = false;
    try {
      LocatorHandler handler = locatorHandlers.get(uid);
      if (handler != null && !Integer.valueOf(0).equals(handler.times)) {
        if (handler.times != null) {
          handler.times--;
        }
        handler.handler.accept(handler.locator);
      }
      remove = handler != null && Integer.valueOf(0).equals(handler.times);
    } finally {
      if (remove) {
        locatorHandlers.remove(uid);
      }
      PageResolveLocatorHandlerNoReplyParams params = new PageResolveLocatorHandlerNoReplyParams();
      params.uid = uid;
      params.remove = remove;
      // java: the call is not awaited upstream; sendMessageAsync does not pump.
      try {
        sendMessageAsync("resolveLocatorHandlerNoReply", params);
      } catch (PlaywrightException e) {
      }
    }
  }

  @Override
  public void removeLocatorHandler(Locator locator) {
    for (Map.Entry<Integer, LocatorHandler> entry : new ArrayList<>(locatorHandlers.entrySet())) {
      if (entry.getValue().locator.equals(locator)) {
        locatorHandlers.remove(entry.getKey());
        PageUnregisterLocatorHandlerParams params = new PageUnregisterLocatorHandlerParams();
        params.uid = entry.getKey();
        try {
          channel.unregisterLocatorHandler(params, NO_TIMEOUT);
        } catch (PlaywrightException e) {
        }
      }
    }
  }

  @Override
  public void waitForLoadState(LoadState state, WaitForLoadStateOptions options) {
    mainFrame.waitForLoadState(state, convertType(options, Frame.WaitForLoadStateOptions.class));
  }

  @Override
  public Response waitForNavigation(WaitForNavigationOptions options, Runnable callback) {
    return mainFrame.waitForNavigation(convertType(options, Frame.WaitForNavigationOptions.class), callback);
  }

  @Override
  public void waitForURL(String url, WaitForURLOptions options) {
    mainFrame.waitForURL(url, convertType(options, Frame.WaitForURLOptions.class));
  }

  @Override
  public void waitForURL(Pattern url, WaitForURLOptions options) {
    mainFrame.waitForURL(url, convertType(options, Frame.WaitForURLOptions.class));
  }

  @Override
  public void waitForURL(Predicate<String> url, WaitForURLOptions options) {
    mainFrame.waitForURL(url, convertType(options, Frame.WaitForURLOptions.class));
  }

  @Override
  public Request waitForRequest(String urlOrPredicate, WaitForRequestOptions options, Runnable callback) {
    return waitForRequest(UrlMatcher.forGlob(browserContext.baseUrl(), urlOrPredicate, connection.localUtils(), false), null, trimUrl(urlOrPredicate), options, callback);
  }

  @Override
  public Request waitForRequest(Pattern urlOrPredicate, WaitForRequestOptions options, Runnable callback) {
    return waitForRequest(new UrlMatcher(urlOrPredicate), null, trimUrl(urlOrPredicate), options, callback);
  }

  @Override
  public Request waitForRequest(Predicate<Request> urlOrPredicate, WaitForRequestOptions options, Runnable callback) {
    return waitForRequest(null, urlOrPredicate, null, options, callback);
  }

  // java: the url matcher or the predicate of urlOrPredicate, with trimUrl(urlOrPredicate) computed by the overload.
  // No @apiCall() zone around the waits: the callback runs inside the wait and its calls would join that zone.
  private Request waitForRequest(UrlMatcher urlMatcher, Predicate<Request> urlOrPredicate, String trimmedUrl, WaitForRequestOptions options, Runnable callback) {
    if (options == null) {
      options = new WaitForRequestOptions();
    }
    Double timeout = options.timeout;
    Predicate<Request> predicate = request -> {
      if (urlMatcher != null) {
        return urlMatcher.test(request.url());
      }
      return urlOrPredicate.test(request);
    };
    String logLine = trimmedUrl != null ? "waiting for request " + trimmedUrl : null;
    return waitForEvent(Events.Page.REQUEST, predicate, timeout, logLine, callback);
  }

  @Override
  public Response waitForResponse(String urlOrPredicate, WaitForResponseOptions options, Runnable callback) {
    return waitForResponse(UrlMatcher.forGlob(browserContext.baseUrl(), urlOrPredicate, connection.localUtils(), false), null, trimUrl(urlOrPredicate), options, callback);
  }

  @Override
  public Response waitForResponse(Pattern urlOrPredicate, WaitForResponseOptions options, Runnable callback) {
    return waitForResponse(new UrlMatcher(urlOrPredicate), null, trimUrl(urlOrPredicate), options, callback);
  }

  @Override
  public Response waitForResponse(Predicate<Response> urlOrPredicate, WaitForResponseOptions options, Runnable callback) {
    return waitForResponse(null, urlOrPredicate, null, options, callback);
  }

  private Response waitForResponse(UrlMatcher urlMatcher, Predicate<Response> urlOrPredicate, String trimmedUrl, WaitForResponseOptions options, Runnable callback) {
    if (options == null) {
      options = new WaitForResponseOptions();
    }
    Double timeout = options.timeout;
    Predicate<Response> predicate = response -> {
      if (urlMatcher != null) {
        return urlMatcher.test(response.url());
      }
      return urlOrPredicate.test(response);
    };
    String logLine = trimmedUrl != null ? "waiting for response " + trimmedUrl : null;
    return waitForEvent(Events.Page.RESPONSE, predicate, timeout, logLine, callback);
  }

  // java: waitForEvent(event) is one typed waitForX(options, callback) per event.
  private <T> T waitForEvent(String event, Predicate<T> predicate, Double timeout, Runnable callback) {
    return waitForEvent(event, predicate, timeout, "waiting for event \"" + event + "\"", callback);
  }

  @Override
  public Page waitForClose(WaitForCloseOptions options, Runnable callback) {
    if (options == null) {
      options = new WaitForCloseOptions();
    }
    return waitForEvent(Events.Page.CLOSE, null, options.timeout, callback);
  }

  @Override
  public ConsoleMessage waitForConsoleMessage(WaitForConsoleMessageOptions options, Runnable callback) {
    if (options == null) {
      options = new WaitForConsoleMessageOptions();
    }
    return waitForEvent(Events.Page.CONSOLE, options.predicate, options.timeout, callback);
  }

  @Override
  public Download waitForDownload(WaitForDownloadOptions options, Runnable callback) {
    if (options == null) {
      options = new WaitForDownloadOptions();
    }
    return waitForEvent(Events.Page.DOWNLOAD, options.predicate, options.timeout, callback);
  }

  @Override
  public FileChooser waitForFileChooser(WaitForFileChooserOptions options, Runnable callback) {
    if (options == null) {
      options = new WaitForFileChooserOptions();
    }
    return waitForEvent(Events.Page.FILE_CHOOSER, options.predicate, options.timeout, callback);
  }

  @Override
  public Page waitForPopup(WaitForPopupOptions options, Runnable callback) {
    if (options == null) {
      options = new WaitForPopupOptions();
    }
    return waitForEvent(Events.Page.POPUP, options.predicate, options.timeout, callback);
  }

  @Override
  public Request waitForRequestFinished(WaitForRequestFinishedOptions options, Runnable callback) {
    if (options == null) {
      options = new WaitForRequestFinishedOptions();
    }
    return waitForEvent(Events.Page.REQUEST_FINISHED, options.predicate, options.timeout, callback);
  }

  @Override
  public WebSocket waitForWebSocket(WaitForWebSocketOptions options, Runnable callback) {
    if (options == null) {
      options = new WaitForWebSocketOptions();
    }
    return waitForEvent(Events.Page.WEB_SOCKET, options.predicate, options.timeout, callback);
  }

  @Override
  public Worker waitForWorker(WaitForWorkerOptions options, Runnable callback) {
    if (options == null) {
      options = new WaitForWorkerOptions();
    }
    return waitForEvent(Events.Page.WORKER, options.predicate, options.timeout, callback);
  }

  TargetClosedError closeErrorWithReason() {
    return new TargetClosedError(closeReason != null ? closeReason : browserContext.effectiveCloseReason());
  }

  // java: the predicate and timeout are passed separately and the code to run is a Runnable.
  private <T> T waitForEvent(String event, Predicate<T> predicate, Double timeout, String logLine, Runnable callback) {
    double timeoutMs = timeoutSettings.timeout(timeout);
    Waiter waiter = Waiter.createForEvent(this, event);
    if (logLine != null) {
      waiter.log(logLine);
    }
    waiter.rejectOnTimeout(timeoutMs, "Timeout " + Waiter.formatTimeout(timeoutMs) + "ms exceeded while waiting for event \"" + event + "\"");
    if (!Events.Page.CRASH.equals(event)) {
      waiter.rejectOnEvent(this, Events.Page.CRASH, () -> new PlaywrightException("Page crashed"));
    }
    if (!Events.Page.CLOSE.equals(event)) {
      waiter.rejectOnEvent(this, Events.Page.CLOSE, () -> closeErrorWithReason());
    }
    T result = waiter.waitForEvent(this, event, predicate, callback);
    waiter.dispose();
    return result;
  }

  @Override
  public Response goBack(GoBackOptions options) {
    if (options == null) {
      options = new GoBackOptions();
    }
    PageGoBackParams params = toProtocol(options, PageGoBackParams.class);
    params.waitUntil = FrameImpl.verifyLoadState("waitUntil", params.waitUntil == null ? "load" : params.waitUntil);
    return channel.goBack(params, timeoutSettings.navigationTimeout(options.timeout)).response;
  }

  @Override
  public Response goForward(GoForwardOptions options) {
    if (options == null) {
      options = new GoForwardOptions();
    }
    PageGoForwardParams params = toProtocol(options, PageGoForwardParams.class);
    params.waitUntil = FrameImpl.verifyLoadState("waitUntil", params.waitUntil == null ? "load" : params.waitUntil);
    return channel.goForward(params, timeoutSettings.navigationTimeout(options.timeout)).response;
  }

  @Override
  public void requestGC() {
    channel.requestGC(NO_TIMEOUT);
  }

  @Override
  public void emulateMedia(EmulateMediaOptions options) {
    // java: an empty Optional is serialized as 'no-override', a null field is left out.
    PageEmulateMediaParams params = toProtocol(options, PageEmulateMediaParams.class);
    channel.emulateMedia(params, NO_TIMEOUT);
  }

  @Override
  public void setViewportSize(int width, int height) {
    viewportSize = new ViewportSize(width, height);
    PageSetViewportSizeParams params = new PageSetViewportSizeParams();
    params.viewportSize = new PageSetViewportSizeParams.PageSetViewportSizeParamsViewportSize();
    params.viewportSize.width = width;
    params.viewportSize.height = height;
    channel.setViewportSize(params, NO_TIMEOUT);
  }

  @Override
  public ViewportSize viewportSize() {
    return viewportSize;
  }

  @Override
  public Object evaluate(String expression, Object arg) {
    return mainFrame.evaluate(expression, arg);
  }

  // java: the script is a string or a file; there is no function to serialize and no exposeFunctions option.
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
    PageAddInitScriptParams params = new PageAddInitScriptParams();
    params.source = source;
    return channel.addInitScript(params, NO_TIMEOUT).disposable;
  }

  @Override
  public AutoCloseable route(String url, Consumer<Route> handler, RouteOptions options) {
    return route(UrlMatcher.forGlob(browserContext.baseUrl(), url, connection.localUtils(), false), handler, options);
  }

  @Override
  public AutoCloseable route(Pattern url, Consumer<Route> handler, RouteOptions options) {
    return route(new UrlMatcher(url), handler, options);
  }

  @Override
  public AutoCloseable route(Predicate<String> url, Consumer<Route> handler, RouteOptions options) {
    return route(new UrlMatcher(url), handler, options);
  }

  private AutoCloseable route(UrlMatcher url, Consumer<Route> handler, RouteOptions options) {
    Integer times = options == null ? null : options.times;
    return withTitle("Route requests", () -> {
      routes.add(0, new RouteHandler(browserContext.options.baseURL, url, handler, times));
      updateInterceptionPatterns();
      return new DisposableStub(() -> unroute(url, handler));
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
    if (Boolean.TRUE.equals(options.update)) {
      browserContext.tracing.recordIntoHAR(har, this, options.url, convertType(options.updateContent, HarContentPolicy.class), options.updateMode, null);
      return;
    }
    HarRouter harRouter = HarRouter.create(localUtils, har, options.notFound != null ? options.notFound : HarNotFound.ABORT, options.url);
    harRouters.add(harRouter);
    harRouter.addPageRoute(this);
  }

  @Override
  public void routeWebSocket(String url, Consumer<WebSocketRoute> handler) {
    routeWebSocket(UrlMatcher.forGlob(browserContext.baseUrl(), url, connection.localUtils(), true), handler);
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
      webSocketRoutes.add(0, new WebSocketRouteHandler(browserContext.options.baseURL, url, handler));
      updateWebSocketInterceptionPatterns();
    });
  }

  private void disposeHarRouters() {
    for (HarRouter router : harRouters) {
      router.dispose();
    }
    harRouters = new ArrayList<>();
  }

  // java: the Java API has no behavior option, unrouteAll() is behavior 'default'.
  @Override
  public void unrouteAll() {
    withTitle("Unroute requests", () -> {
      unrouteInternal(routes, new ArrayList<>());
      disposeHarRouters();
    });
  }

  @Override
  public void unroute(String url, Consumer<Route> handler) {
    unroute(UrlMatcher.forGlob(browserContext.baseUrl(), url, connection.localUtils(), false), handler);
  }

  @Override
  public void unroute(Pattern url, Consumer<Route> handler) {
    unroute(new UrlMatcher(url), handler);
  }

  @Override
  public void unroute(Predicate<String> url, Consumer<Route> handler) {
    unroute(new UrlMatcher(url), handler);
  }

  private void unroute(UrlMatcher url, Consumer<Route> handler) {
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

  // java: no behavior parameter, the 'wait' and 'ignoreErrors' branches that stop the removed handlers are unreachable.
  private void unrouteInternal(List<RouteHandler> removed, List<RouteHandler> remaining) {
    routes = remaining;
    updateInterceptionPatterns();
  }

  private void disableInterception() {
    wrapApiCall(() -> updateInterceptionPatterns(), null, true);
  }

  private void updateInterceptionPatterns() {
    PageSetNetworkInterceptionPatternsParams params = new PageSetNetworkInterceptionPatternsParams();
    params.patterns = RouteHandler.prepareInterceptionPatterns(routes);
    channel.setNetworkInterceptionPatterns(params, NO_TIMEOUT);
  }

  private void updateWebSocketInterceptionPatterns() {
    PageSetWebSocketInterceptionPatternsParams params = new PageSetWebSocketInterceptionPatternsParams();
    params.patterns = WebSocketRouteHandler.prepareInterceptionPatterns(webSocketRoutes);
    channel.setWebSocketInterceptionPatterns(params, NO_TIMEOUT);
  }

  @Override
  public byte[] screenshot(ScreenshotOptions options) {
    if (options == null) {
      options = new ScreenshotOptions();
    }
    List<Locator> mask = options.mask;
    // java: { ...options, mask: undefined } is a shallow copy of the options without the mask, as params.
    ScreenshotOptions withoutMask = Utils.clone(options);
    withoutMask.mask = null;
    PageScreenshotParams copy = toProtocol(withoutMask, PageScreenshotParams.class);
    if (copy.type == null) {
      copy.type = ElementHandleImpl.determineScreenshotType(options);
    }
    if (mask != null) {
      copy.mask = new ArrayList<>();
      for (Locator locator : mask) {
        PageScreenshotParams.PageScreenshotParamsMask item = new PageScreenshotParams.PageScreenshotParamsMask();
        item.frame = ((LocatorImpl) locator).frame;
        item.selector = ((LocatorImpl) locator).selector;
        copy.mask.add(item);
      }
    }
    PageScreenshotResult result = channel.screenshot(copy, timeoutSettings.timeout(options.timeout));
    if (options.path != null) {
      Utils.writeToFile(result.binary, options.path);
    }
    return result.binary;
  }

  @Override
  public String title() {
    return mainFrame.title();
  }

  @Override
  public void bringToFront() {
    channel.bringToFront(NO_TIMEOUT);
  }

  @Override
  public void close(CloseOptions options) {
    if (options == null) {
      options = new CloseOptions();
    }
    boolean runBeforeUnload = Boolean.TRUE.equals(options.runBeforeUnload);
    closeReason = options.reason;
    if (!runBeforeUnload) {
      closeWasCalled = true;
    }
    try {
      if (ownedContext != null) {
        ownedContext.close();
      } else if (runBeforeUnload) {
        channel.runBeforeUnload(NO_TIMEOUT);
      } else {
        PageCloseParams params = new PageCloseParams();
        params.reason = options.reason;
        channel.close(params, NO_TIMEOUT);
      }
    } catch (TargetClosedError e) {
      if (!runBeforeUnload) {
        return;
      }
      throw e;
    }
  }

  @Override
  public boolean isClosed() {
    return closed;
  }

  @Override
  public void click(String selector, ClickOptions options) {
    mainFrame.click(selector, convertType(options, Frame.ClickOptions.class));
  }

  @Override
  public void dragAndDrop(String source, String target, DragAndDropOptions options) {
    mainFrame.dragAndDrop(source, target, convertType(options, Frame.DragAndDropOptions.class));
  }

  @Override
  public void dblclick(String selector, DblclickOptions options) {
    mainFrame.dblclick(selector, convertType(options, Frame.DblclickOptions.class));
  }

  @Override
  public void tap(String selector, TapOptions options) {
    mainFrame.tap(selector, convertType(options, Frame.TapOptions.class));
  }

  @Override
  public void fill(String selector, String value, FillOptions options) {
    mainFrame.fill(selector, value, convertType(options, Frame.FillOptions.class));
  }

  @Override
  public void clearConsoleMessages() {
    channel.clearConsoleMessages(NO_TIMEOUT);
  }

  @Override
  public List<ConsoleMessage> consoleMessages(ConsoleMessagesOptions options) {
    PageConsoleMessagesParams params = toProtocol(options, PageConsoleMessagesParams.class);
    PageConsoleMessagesResult result = channel.consoleMessages(params, NO_TIMEOUT);
    List<ConsoleMessage> messages = new ArrayList<>();
    for (PageConsoleMessagesResult.PageConsoleMessagesResultMessages message : result.messages) {
      messages.add(new ConsoleMessageImpl(this, message));
    }
    return messages;
  }

  @Override
  public void clearPageErrors() {
    channel.clearPageErrors(NO_TIMEOUT);
  }

  // java: the Java API returns the errors as strings, not as parsed Error objects; there is no filter option.
  @Override
  public List<String> pageErrors() {
    PagePageErrorsResult result = channel.pageErrors(new PagePageErrorsParams(), NO_TIMEOUT);
    List<String> errors = new ArrayList<>();
    for (SerializedError error : result.errors) {
      errors.add(Serialization.parseError(gson().toJsonTree(error).getAsJsonObject()));
    }
    return errors;
  }

  @Override
  public Locator locator(String selector, LocatorOptions options) {
    return mainFrame().locator(selector, convertType(options, Frame.LocatorOptions.class));
  }

  @Override
  public Locator getByTestId(String testId) {
    return mainFrame().getByTestId(testId);
  }

  @Override
  public Locator getByTestId(Pattern testId) {
    return mainFrame().getByTestId(testId);
  }

  @Override
  public Locator getByAltText(String text, GetByAltTextOptions options) {
    return mainFrame().getByAltText(text, convertType(options, Frame.GetByAltTextOptions.class));
  }

  @Override
  public Locator getByAltText(Pattern text, GetByAltTextOptions options) {
    return mainFrame().getByAltText(text, convertType(options, Frame.GetByAltTextOptions.class));
  }

  @Override
  public Locator getByLabel(String text, GetByLabelOptions options) {
    return mainFrame().getByLabel(text, convertType(options, Frame.GetByLabelOptions.class));
  }

  @Override
  public Locator getByLabel(Pattern text, GetByLabelOptions options) {
    return mainFrame().getByLabel(text, convertType(options, Frame.GetByLabelOptions.class));
  }

  @Override
  public Locator getByPlaceholder(String text, GetByPlaceholderOptions options) {
    return mainFrame().getByPlaceholder(text, convertType(options, Frame.GetByPlaceholderOptions.class));
  }

  @Override
  public Locator getByPlaceholder(Pattern text, GetByPlaceholderOptions options) {
    return mainFrame().getByPlaceholder(text, convertType(options, Frame.GetByPlaceholderOptions.class));
  }

  @Override
  public Locator getByRef(String ref) {
    return locator("aria-ref=" + ref);
  }

  @Override
  public Locator getByText(String text, GetByTextOptions options) {
    return mainFrame().getByText(text, convertType(options, Frame.GetByTextOptions.class));
  }

  @Override
  public Locator getByText(Pattern text, GetByTextOptions options) {
    return mainFrame().getByText(text, convertType(options, Frame.GetByTextOptions.class));
  }

  @Override
  public Locator getByTitle(String text, GetByTitleOptions options) {
    return mainFrame().getByTitle(text, convertType(options, Frame.GetByTitleOptions.class));
  }

  @Override
  public Locator getByTitle(Pattern text, GetByTitleOptions options) {
    return mainFrame().getByTitle(text, convertType(options, Frame.GetByTitleOptions.class));
  }

  @Override
  public Locator getByRole(AriaRole role, GetByRoleOptions options) {
    return mainFrame().getByRole(role, convertType(options, Frame.GetByRoleOptions.class));
  }

  @Override
  public FrameLocator frameLocator(String selector) {
    return mainFrame().frameLocator(selector);
  }

  @Override
  public void focus(String selector, FocusOptions options) {
    mainFrame.focus(selector, convertType(options, Frame.FocusOptions.class));
  }

  @Override
  public String textContent(String selector, TextContentOptions options) {
    return mainFrame.textContent(selector, convertType(options, Frame.TextContentOptions.class));
  }

  @Override
  public String innerText(String selector, InnerTextOptions options) {
    return mainFrame.innerText(selector, convertType(options, Frame.InnerTextOptions.class));
  }

  @Override
  public String innerHTML(String selector, InnerHTMLOptions options) {
    return mainFrame.innerHTML(selector, convertType(options, Frame.InnerHTMLOptions.class));
  }

  @Override
  public String getAttribute(String selector, String name, GetAttributeOptions options) {
    return mainFrame.getAttribute(selector, name, convertType(options, Frame.GetAttributeOptions.class));
  }

  @Override
  public String inputValue(String selector, InputValueOptions options) {
    return mainFrame.inputValue(selector, convertType(options, Frame.InputValueOptions.class));
  }

  @Override
  public boolean isChecked(String selector, IsCheckedOptions options) {
    return mainFrame.isChecked(selector, convertType(options, Frame.IsCheckedOptions.class));
  }

  @Override
  public boolean isDisabled(String selector, IsDisabledOptions options) {
    return mainFrame.isDisabled(selector, convertType(options, Frame.IsDisabledOptions.class));
  }

  @Override
  public boolean isEditable(String selector, IsEditableOptions options) {
    return mainFrame.isEditable(selector, convertType(options, Frame.IsEditableOptions.class));
  }

  @Override
  public boolean isEnabled(String selector, IsEnabledOptions options) {
    return mainFrame.isEnabled(selector, convertType(options, Frame.IsEnabledOptions.class));
  }

  @Override
  public boolean isHidden(String selector, IsHiddenOptions options) {
    return mainFrame.isHidden(selector, convertType(options, Frame.IsHiddenOptions.class));
  }

  @Override
  public boolean isVisible(String selector, IsVisibleOptions options) {
    return mainFrame.isVisible(selector, convertType(options, Frame.IsVisibleOptions.class));
  }

  @Override
  public void hover(String selector, HoverOptions options) {
    mainFrame.hover(selector, convertType(options, Frame.HoverOptions.class));
  }

  @Override
  public List<String> selectOption(String selector, String values, SelectOptionOptions options) {
    return mainFrame.selectOption(selector, values, convertType(options, Frame.SelectOptionOptions.class));
  }

  @Override
  public List<String> selectOption(String selector, ElementHandle values, SelectOptionOptions options) {
    return mainFrame.selectOption(selector, values, convertType(options, Frame.SelectOptionOptions.class));
  }

  @Override
  public List<String> selectOption(String selector, String[] values, SelectOptionOptions options) {
    return mainFrame.selectOption(selector, values, convertType(options, Frame.SelectOptionOptions.class));
  }

  @Override
  public List<String> selectOption(String selector, SelectOption values, SelectOptionOptions options) {
    return mainFrame.selectOption(selector, values, convertType(options, Frame.SelectOptionOptions.class));
  }

  @Override
  public List<String> selectOption(String selector, ElementHandle[] values, SelectOptionOptions options) {
    return mainFrame.selectOption(selector, values, convertType(options, Frame.SelectOptionOptions.class));
  }

  @Override
  public List<String> selectOption(String selector, SelectOption[] values, SelectOptionOptions options) {
    return mainFrame.selectOption(selector, values, convertType(options, Frame.SelectOptionOptions.class));
  }

  @Override
  public void setInputFiles(String selector, Path files, SetInputFilesOptions options) {
    mainFrame.setInputFiles(selector, files, convertType(options, Frame.SetInputFilesOptions.class));
  }

  @Override
  public void setInputFiles(String selector, Path[] files, SetInputFilesOptions options) {
    mainFrame.setInputFiles(selector, files, convertType(options, Frame.SetInputFilesOptions.class));
  }

  @Override
  public void setInputFiles(String selector, FilePayload files, SetInputFilesOptions options) {
    mainFrame.setInputFiles(selector, files, convertType(options, Frame.SetInputFilesOptions.class));
  }

  @Override
  public void setInputFiles(String selector, FilePayload[] files, SetInputFilesOptions options) {
    mainFrame.setInputFiles(selector, files, convertType(options, Frame.SetInputFilesOptions.class));
  }

  @Override
  public void type(String selector, String text, TypeOptions options) {
    mainFrame.type(selector, text, convertType(options, Frame.TypeOptions.class));
  }

  @Override
  public void press(String selector, String key, PressOptions options) {
    mainFrame.press(selector, key, convertType(options, Frame.PressOptions.class));
  }

  @Override
  public void check(String selector, CheckOptions options) {
    mainFrame.check(selector, convertType(options, Frame.CheckOptions.class));
  }

  @Override
  public void uncheck(String selector, UncheckOptions options) {
    mainFrame.uncheck(selector, convertType(options, Frame.UncheckOptions.class));
  }

  @Override
  public void setChecked(String selector, boolean checked, SetCheckedOptions options) {
    mainFrame.setChecked(selector, checked, convertType(options, Frame.SetCheckedOptions.class));
  }

  @Override
  public void waitForTimeout(double timeout) {
    mainFrame.waitForTimeout(timeout);
  }

  @Override
  public JSHandle waitForFunction(String expression, Object arg, WaitForFunctionOptions options) {
    return mainFrame.waitForFunction(expression, arg, convertType(options, Frame.WaitForFunctionOptions.class));
  }

  @Override
  public List<Request> requests() {
    PageRequestsResult result = channel.requests(NO_TIMEOUT);
    return new ArrayList<>(result.requests);
  }

  @Override
  public List<Worker> workers() {
    return new ArrayList<>(workers);
  }

  // java: there is no JS debugger to detect and no instrumentation to notify.
  @Override
  public void pause() {
    Double defaultNavigationTimeout = browserContext.timeoutSettings.defaultNavigationTimeout();
    Double defaultTimeout = browserContext.timeoutSettings.defaultTimeout();
    browserContext.setDefaultNavigationTimeout(0);
    browserContext.setDefaultTimeout(0);
    closedOrCrashedScope.safeRace(browserContext.sendMessageAsync("pause"));
    // java: the saved defaults may be unset, which the public double setters cannot express.
    browserContext.timeoutSettings.setDefaultNavigationTimeout(defaultNavigationTimeout);
    browserContext.timeoutSettings.setDefaultTimeout(defaultTimeout);
  }

  // java: width, height and the margins are strings in the Java API, there is nothing to convert to px.
  @Override
  public byte[] pdf(PdfOptions options) {
    if (options == null) {
      options = new PdfOptions();
    }
    PagePdfParams transportOptions = toProtocol(options, PagePdfParams.class);
    PagePdfResult result = channel.pdf(transportOptions, NO_TIMEOUT);
    if (options.path != null) {
      Utils.writeToFile(result.pdf, options.path);
    }
    return result.pdf;
  }

  @Override
  public String ariaSnapshot(AriaSnapshotOptions options) {
    if (options == null) {
      options = new AriaSnapshotOptions();
    }
    FrameAriaSnapshotParams params = new FrameAriaSnapshotParams();
    params.mode = options.mode == null ? null : options.mode.toString().toLowerCase();
    params.depth = options.depth;
    params.boxes = options.boxes;
    return mainFrame().channel.ariaSnapshot(params, timeoutSettings.timeout(options.timeout)).snapshot;
  }

  private static String trimUrl(Object param) {
    if (param instanceof Pattern) {
      Pattern pattern = (Pattern) param;
      return "/" + trimStringWithEllipsis(pattern.pattern(), 50) + "/" + toJsRegexFlags(pattern);
    }
    if (param instanceof String) {
      return "\"" + trimStringWithEllipsis((String) param, 50) + "\"";
    }
    return null;
  }

  // java: the value object of _locatorHandlers.
  private static class LocatorHandler {
    final LocatorImpl locator;
    final Consumer<Locator> handler;
    Integer times;

    LocatorHandler(LocatorImpl locator, Consumer<Locator> handler, Integer times) {
      this.locator = locator;
      this.handler = handler;
      this.times = times;
    }
  }

  // java: the public API members with no upstream counterpart: accessors of the readonly fields, typed
  // listeners, onceDialog, waitForCondition and its waitables.
  @Override
  public ClockImpl clock() {
    return clock;
  }

  @Override
  public KeyboardImpl keyboard() {
    return keyboard;
  }

  @Override
  public MouseImpl mouse() {
    return mouse;
  }

  @Override
  public APIRequestContextImpl request() {
    return request;
  }

  @Override
  public TouchscreenImpl touchscreen() {
    return touchscreen;
  }

  @Override
  public Screencast screencast() {
    return screencast;
  }

  @Override
  public WebStorage localStorage() {
    return localStorage;
  }

  @Override
  public WebStorage sessionStorage() {
    return sessionStorage;
  }

  @Override
  public WebMCP webmcp() {
    return webmcp;
  }

  @Override
  public void onClose(Consumer<Page> handler) {
    on(Events.Page.CLOSE, handler);
  }

  @Override
  public void offClose(Consumer<Page> handler) {
    off(Events.Page.CLOSE, handler);
  }

  @Override
  public void onConsoleMessage(Consumer<ConsoleMessage> handler) {
    on(Events.Page.CONSOLE, handler);
  }

  @Override
  public void offConsoleMessage(Consumer<ConsoleMessage> handler) {
    off(Events.Page.CONSOLE, handler);
  }

  @Override
  public void onCrash(Consumer<Page> handler) {
    on(Events.Page.CRASH, handler);
  }

  @Override
  public void offCrash(Consumer<Page> handler) {
    off(Events.Page.CRASH, handler);
  }

  @Override
  public void onDialog(Consumer<Dialog> handler) {
    on(Events.Page.DIALOG, handler);
  }

  @Override
  public void offDialog(Consumer<Dialog> handler) {
    off(Events.Page.DIALOG, handler);
  }

  @Override
  public void onDialogClosed(Consumer<Dialog> handler) {
    on(Events.Page.DIALOG_CLOSED, handler);
  }

  @Override
  public void offDialogClosed(Consumer<Dialog> handler) {
    off(Events.Page.DIALOG_CLOSED, handler);
  }

  @Override
  public void onDOMContentLoaded(Consumer<Page> handler) {
    on(Events.Page.DOM_CONTENT_LOADED, handler);
  }

  @Override
  public void offDOMContentLoaded(Consumer<Page> handler) {
    off(Events.Page.DOM_CONTENT_LOADED, handler);
  }

  @Override
  public void onDownload(Consumer<Download> handler) {
    on(Events.Page.DOWNLOAD, handler);
  }

  @Override
  public void offDownload(Consumer<Download> handler) {
    off(Events.Page.DOWNLOAD, handler);
  }

  @Override
  public void onFileChooser(Consumer<FileChooser> handler) {
    on(Events.Page.FILE_CHOOSER, handler);
  }

  @Override
  public void offFileChooser(Consumer<FileChooser> handler) {
    off(Events.Page.FILE_CHOOSER, handler);
  }

  @Override
  public void onFrameAttached(Consumer<Frame> handler) {
    on(Events.Page.FRAME_ATTACHED, handler);
  }

  @Override
  public void offFrameAttached(Consumer<Frame> handler) {
    off(Events.Page.FRAME_ATTACHED, handler);
  }

  @Override
  public void onFrameDetached(Consumer<Frame> handler) {
    on(Events.Page.FRAME_DETACHED, handler);
  }

  @Override
  public void offFrameDetached(Consumer<Frame> handler) {
    off(Events.Page.FRAME_DETACHED, handler);
  }

  @Override
  public void onFrameNavigated(Consumer<Frame> handler) {
    on(Events.Page.FRAME_NAVIGATED, handler);
  }

  @Override
  public void offFrameNavigated(Consumer<Frame> handler) {
    off(Events.Page.FRAME_NAVIGATED, handler);
  }

  @Override
  public void onLoad(Consumer<Page> handler) {
    on(Events.Page.LOAD, handler);
  }

  @Override
  public void offLoad(Consumer<Page> handler) {
    off(Events.Page.LOAD, handler);
  }

  @Override
  public void onPageError(Consumer<String> handler) {
    on(Events.Page.PAGE_ERROR, handler);
  }

  @Override
  public void offPageError(Consumer<String> handler) {
    off(Events.Page.PAGE_ERROR, handler);
  }

  @Override
  public void onPopup(Consumer<Page> handler) {
    on(Events.Page.POPUP, handler);
  }

  @Override
  public void offPopup(Consumer<Page> handler) {
    off(Events.Page.POPUP, handler);
  }

  @Override
  public void onRequest(Consumer<Request> handler) {
    on(Events.Page.REQUEST, handler);
  }

  @Override
  public void offRequest(Consumer<Request> handler) {
    off(Events.Page.REQUEST, handler);
  }

  @Override
  public void onRequestFailed(Consumer<Request> handler) {
    on(Events.Page.REQUEST_FAILED, handler);
  }

  @Override
  public void offRequestFailed(Consumer<Request> handler) {
    off(Events.Page.REQUEST_FAILED, handler);
  }

  @Override
  public void onRequestFinished(Consumer<Request> handler) {
    on(Events.Page.REQUEST_FINISHED, handler);
  }

  @Override
  public void offRequestFinished(Consumer<Request> handler) {
    off(Events.Page.REQUEST_FINISHED, handler);
  }

  @Override
  public void onResponse(Consumer<Response> handler) {
    on(Events.Page.RESPONSE, handler);
  }

  @Override
  public void offResponse(Consumer<Response> handler) {
    off(Events.Page.RESPONSE, handler);
  }

  @Override
  public void onWebSocket(Consumer<WebSocket> handler) {
    on(Events.Page.WEB_SOCKET, handler);
  }

  @Override
  public void offWebSocket(Consumer<WebSocket> handler) {
    off(Events.Page.WEB_SOCKET, handler);
  }

  @Override
  public void onWorker(Consumer<Worker> handler) {
    on(Events.Page.WORKER, handler);
  }

  @Override
  public void offWorker(Consumer<Worker> handler) {
    off(Events.Page.WORKER, handler);
  }

  @Override
  public void onceDialog(Consumer<Dialog> handler) {
    onDialog(new Consumer<Dialog>() {
      @Override
      public void accept(Dialog dialog) {
        try {
          handler.accept(dialog);
        } finally {
          offDialog(this);
        }
      }
    });
  }

  @Override
  public void waitForCondition(BooleanSupplier condition, WaitForConditionOptions options) {
    List<Waitable<Void>> waitables = new ArrayList<>();
    // java: the condition goes first, it wins on a page that is already closed.
    waitables.add(new WaitablePredicate<>(condition));
    waitables.add(createWaitForCloseHelper());
    waitables.add(createWaitableTimeout(options == null ? null : options.timeout));
    runUntil(() -> {}, new WaitableRace<>(waitables));
  }

  // java: done once the page closed or crashed, then throws the error closedOrCrashedScope was closed with.
  <T> Waitable<T> createWaitForCloseHelper() {
    return closedOrCrashedScope.terminatePromise(false);
  }

  <T> Waitable<T> createWaitableTimeout(Double timeout) {
    return timeoutSettings.createWaitable(timeout);
  }
}
