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
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.Serialization.parseError;
import static com.microsoft.playwright.impl.Utils.*;
import static com.microsoft.playwright.impl.Errors.isSafeCloseError;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;
import static java.util.Arrays.asList;


public class PageImpl extends ChannelOwner implements Page {
  final BrowserContextImpl browserContext;
  private final FrameImpl mainFrame;
  final PageChannel channel;
  private final KeyboardImpl keyboard;
  private final MouseImpl mouse;
  private final TouchscreenImpl touchscreen;
  private final ScreencastImpl screencast;
  private final WebStorageImpl localStorage;
  private final WebStorageImpl sessionStorage;
  final Waitable<?> waitableClosedOrCrashed;
  private ViewportSize viewport;
  private final Router routes = new Router();
  private final WebSocketRouter webSocketRoutes = new WebSocketRouter();
  private final Set<FrameImpl> frames = new LinkedHashSet<>();
  private final Map<Integer, LocatorHandler> locatorHandlers = new HashMap<>();

  private static class LocatorHandler {
    private final Locator locator;
    private final Consumer<Locator> handler;
    private Integer times;

    LocatorHandler(Locator locator, Consumer<Locator> handler, Integer times) {
      this.locator = locator;
      this.handler = handler;
      this.times = times;
    }

    boolean call() {
      if (shouldRemove()) {
        return true;
      }
      if (times != null) {
        --times;
      }
      handler.accept(locator);
      return shouldRemove();
    }

    private boolean shouldRemove() {
      return times != null && times == 0;
    }
  }

  final Map<String, BindingCallback> bindings = new HashMap<>();
  BrowserContextImpl ownedContext;
  private boolean isClosed;
  final Set<Worker> workers = new HashSet<>();
  protected final TimeoutSettings timeoutSettings;
  private VideoImpl video;
  private final PageImpl opener;
  private String closeReason;

  PageImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    Map<String, String> mapping = new HashMap<>();
    mapping.put(Events.Page.CONSOLE, "console");
    mapping.put(Events.Page.DIALOG, "dialog");
    mapping.put(Events.Page.DIALOG_CLOSED, "dialogClosed");
    mapping.put(Events.Page.REQUEST, "request");
    mapping.put(Events.Page.RESPONSE, "response");
    mapping.put(Events.Page.REQUEST_FINISHED, "requestFinished");
    mapping.put(Events.Page.REQUEST_FAILED, "requestFailed");
    mapping.put(Events.Page.FILE_CHOOSER, "fileChooser");
    setEventToSubscriptionMapping(mapping);
    browserContext = (BrowserContextImpl) parent;
    mainFrame = connection.getExistingObject(initializer.getAsJsonObject("mainFrame").get("guid").getAsString());
    mainFrame.page = this;
    isClosed = initializer.get("isClosed").getAsBoolean();
    if (initializer.has("viewportSize")) {
      viewport = gson().fromJson(initializer.get("viewportSize"), ViewportSize.class);
    }
    channel = new PageChannel(this);
    keyboard = new KeyboardImpl(this);
    mouse = new MouseImpl(this);
    touchscreen = new TouchscreenImpl(this);
    screencast = new ScreencastImpl(this);
    localStorage = new WebStorageImpl(this, "local");
    sessionStorage = new WebStorageImpl(this, "session");
    frames.add(mainFrame);
    timeoutSettings = new TimeoutSettings(browserContext.timeoutSettings);
    waitableClosedOrCrashed = createWaitForCloseHelper();
    if (initializer.has("opener")) {
      opener = connection.getExistingObject(initializer.getAsJsonObject("opener").get("guid").getAsString());
    } else {
      opener = null;
    }
    if (initializer.has("video")) {
      String artifactGuid = initializer.getAsJsonObject("video").get("guid").getAsString();
      ArtifactImpl artifact = connection.getExistingObject(artifactGuid);
      video = new VideoImpl(this, artifact);
    } else {
      video = new VideoImpl(this);
    }
  }

  @Override
  protected void handleEvent(String event, JsonObject params) {
    if ("worker".equals(event)) {
      String guid = params.getAsJsonObject("worker").get("guid").getAsString();
      WorkerImpl worker = connection.getExistingObject(guid);
      worker.page = this;
      workers.add(worker);
      emit(Events.Page.WORKER, worker);
    } else if ("webSocket".equals(event)) {
      String guid = params.getAsJsonObject("webSocket").get("guid").getAsString();
      WebSocketImpl webSocket = connection.getExistingObject(guid);
      emit(Events.Page.WEB_SOCKET, webSocket);
    } else if ("download".equals(event)) {
      String artifactGuid = params.getAsJsonObject("artifact").get("guid").getAsString();
      ArtifactImpl artifact = connection.getExistingObject(artifactGuid);
      DownloadImpl download = new DownloadImpl(this, artifact, params);
      emit(Events.Page.DOWNLOAD, download);
      browserContext.notifyDownload(download);
    } else if ("fileChooser".equals(event)) {
      String guid = params.getAsJsonObject("element").get("guid").getAsString();
      ElementHandleImpl elementHandle = connection.getExistingObject(guid);
      FileChooser fileChooser = new FileChooserImpl(this, elementHandle, params.get("isMultiple").getAsBoolean());
      emit(Events.Page.FILE_CHOOSER, fileChooser);
    } else if ("bindingCall".equals(event)) {
      String guid = params.getAsJsonObject("binding").get("guid").getAsString();
      BindingCall bindingCall = connection.getExistingObject(guid);
      BindingCallback binding = bindings.get(bindingCall.name());
      if (binding == null) {
        binding = browserContext.bindings.get(bindingCall.name());
      }
      if (binding != null) {
        try {
          bindingCall.call(binding);
        } catch (RuntimeException e) {
          if (!isSafeCloseError(e.getMessage())) {
            logWithTimestamp(e.getMessage());
          }
        }
      }
    } else if ("frameAttached".equals(event)) {
      String guid = params.getAsJsonObject("frame").get("guid").getAsString();
      FrameImpl frame = connection.getExistingObject(guid);
      frames.add(frame);
      frame.page = this;
      if (frame.parentFrame != null) {
        frame.parentFrame.childFrames.add(frame);
      }
      emit(Events.Page.FRAME_ATTACHED, frame);
      browserContext.notifyFrameAttached(frame);
    } else if ("frameDetached".equals(event)) {
      String guid = params.getAsJsonObject("frame").get("guid").getAsString();
      FrameImpl frame = connection.getExistingObject(guid);
      frames.remove(frame);
      frame.isDetached = true;
      if (frame.parentFrame != null) {
        frame.parentFrame.childFrames.remove(frame);
      }
      emit(Events.Page.FRAME_DETACHED, frame);
      browserContext.notifyFrameDetached(frame);
    } else if ("locatorHandlerTriggered".equals(event)) {
      int uid = params.get("uid").getAsInt();
      onLocatorHandlerTriggered(uid);
    } else if ("route".equals(event)) {
      RouteImpl route = connection.getExistingObject(params.getAsJsonObject("route").get("guid").getAsString());
      route.browserContext = browserContext;
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
        browserContext.handleRoute(route);
      }
    } else if ("webSocketRoute".equals(event)) {
      WebSocketRouteImpl route = connection.getExistingObject(params.getAsJsonObject("webSocketRoute").get("guid").getAsString());
      if (!webSocketRoutes.handle(route)) {
        browserContext.handleWebSocketRoute(route);
      }
    } else if ("crash".equals(event)) {
      emit(Events.Page.CRASH, this);
    } else if ("close".equals(event)) {
      didClose();
    } else if ("screencastFrame".equals(event)) {
      screencast.handleScreencastFrame(params);
    }
  }

  void notifyPopup(PageImpl popup) {
    emit(Events.Page.POPUP, popup);
  }

  void didClose() {
    isClosed = true;
    browserContext.pages.remove(this);
    emit(Events.Page.CLOSE, this);
    browserContext.notifyPageClose(this);
  }

  private String effectiveCloseReason() {
    if (closeReason != null) {
      return closeReason;
    }
    return browserContext.effectiveCloseReason();
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
  public ClockImpl clock() {
    return browserContext.clock();
  }

  @Override
  public Page waitForClose(WaitForCloseOptions options, Runnable code) {
    return withWaitLogging("Page.waitForClose", "close", logger -> waitForCloseImpl(options, code));
  }

  private Page waitForCloseImpl(WaitForCloseOptions options, Runnable code) {
    if (options == null) {
      options = new WaitForCloseOptions();
    }
    return waitForEventWithTimeout(Events.Page.CLOSE, code, null, options.timeout);
  }

  @Override
  public ConsoleMessage waitForConsoleMessage(WaitForConsoleMessageOptions options, Runnable code) {
    return withWaitLogging("Page.waitForConsoleMessage", "console", logger -> waitForConsoleMessageImpl(options, code));
  }

  private ConsoleMessage waitForConsoleMessageImpl(WaitForConsoleMessageOptions options, Runnable code) {
    if (options == null) {
      options = new WaitForConsoleMessageOptions();
    }
    return waitForEventWithTimeout(Events.Page.CONSOLE, code, options.predicate, options.timeout);
  }

  @Override
  public Download waitForDownload(WaitForDownloadOptions options, Runnable code) {
    return withWaitLogging("Page.waitForDownload", "download", logger -> waitForDownloadImpl(options, code));
  }

  private Download waitForDownloadImpl(WaitForDownloadOptions options, Runnable code) {
    if (options == null) {
      options = new WaitForDownloadOptions();
    }
    return waitForEventWithTimeout(Events.Page.DOWNLOAD, code, options.predicate, options.timeout);
  }

  @Override
  public FileChooser waitForFileChooser(WaitForFileChooserOptions options, Runnable code) {
    return withWaitLogging("Page.waitForFileChooser", "filechooser", logger -> waitForFileChooserImpl(options, code));
  }

  private FileChooser waitForFileChooserImpl(WaitForFileChooserOptions options, Runnable code) {
    // TODO: enable/disable file chooser interception
    if (options == null) {
      options = new WaitForFileChooserOptions();
    }
    return waitForEventWithTimeout(Events.Page.FILE_CHOOSER, code, options.predicate, options.timeout);
  }

  @Override
  public Page waitForPopup(WaitForPopupOptions options, Runnable code) {
    return withWaitLogging("Page.waitForPopup", "popup", logger -> waitForPopupImpl(options, code));
  }

  private Page waitForPopupImpl(WaitForPopupOptions options, Runnable code) {
    if (options == null) {
      options = new WaitForPopupOptions();
    }
    return waitForEventWithTimeout(Events.Page.POPUP, code, options.predicate, options.timeout);
  }

  @Override
  public WebSocket waitForWebSocket(WaitForWebSocketOptions options, Runnable code) {
    return withWaitLogging("Page.waitForWebSocket", "websocket", logger -> waitForWebSocketImpl(options, code));
  }

  private WebSocket waitForWebSocketImpl(WaitForWebSocketOptions options, Runnable code) {
    if (options == null) {
      options = new WaitForWebSocketOptions();
    }
    return waitForEventWithTimeout(Events.Page.WEB_SOCKET, code, options.predicate, options.timeout);
  }

  @Override
  public Worker waitForWorker(WaitForWorkerOptions options, Runnable code) {
    return withWaitLogging("Page.waitForWorker", "worker", logger -> waitForWorkerImpl(options, code));
  }

  private Worker waitForWorkerImpl(WaitForWorkerOptions options, Runnable code) {
    if (options == null) {
      options = new WaitForWorkerOptions();
    }
    return waitForEventWithTimeout(Events.Page.WORKER, code, options.predicate, options.timeout);
  }

  private <T> T waitForEventWithTimeout(String eventType, Runnable code, Predicate<T> predicate, Double timeout) {
    List<Waitable<T>> waitables = new ArrayList<>();
    waitables.add(new WaitableEvent<>(this, eventType, predicate));
    waitables.add(createWaitForCloseHelper());
    waitables.add(createWaitableTimeout(timeout));
    return runUntil(code, new WaitableRace<>(waitables));
  }

  @Override
  public void close(CloseOptions options) {
    if (options == null) {
      options = new CloseOptions();
    }
    closeReason = options.reason;
    try {
      if (ownedContext != null) {
        ownedContext.close();
      } else if (options.runBeforeUnload != null && options.runBeforeUnload) {
        channel.runBeforeUnload(NO_TIMEOUT);
      } else {
        PageCloseParams params = new PageCloseParams();
        params.reason = options.reason;
        channel.close(params, NO_TIMEOUT);
      }
    } catch (PlaywrightException exception) {
      if (isSafeCloseError(exception) && (options.runBeforeUnload == null || !options.runBeforeUnload)) {
        return;
      }
      throw exception;
    }
  }

  @Override
  public ElementHandle querySelector(String selector, QuerySelectorOptions options) {
    return mainFrame.querySelector(
      selector, convertType(options, Frame.QuerySelectorOptions.class));
  }

  @Override
  public List<ElementHandle> querySelectorAll(String selector) {
    return mainFrame.querySelectorAll(selector);
  }

  @Override
  public List<Request> requests() {
    PageRequestsResult json = channel.requests(NO_TIMEOUT);
    return new ArrayList<Request>(json.requests);
  }

  @Override
  public void addLocatorHandler(Locator locator, Consumer<Locator> handler, AddLocatorHandlerOptions options) {
    LocatorImpl locatorImpl = (LocatorImpl) locator;
    if (locatorImpl.frame != mainFrame) {
      throw new PlaywrightException("Locator must belong to the main frame of this page");
    }
    if (options == null) {
      options = new AddLocatorHandlerOptions();
    }
    if (options.times != null && options.times == 0) {
      return;
    }
    AddLocatorHandlerOptions finalOptions = options;
    PageRegisterLocatorHandlerParams params = new PageRegisterLocatorHandlerParams();
    params.selector = locatorImpl.selector;
    if (finalOptions.noWaitAfter != null && finalOptions.noWaitAfter) {
      params.noWaitAfter = true;
    }
    PageRegisterLocatorHandlerResult json = channel.registerLocatorHandler(params, NO_TIMEOUT);
    int uid = json.uid;
    locatorHandlers.put(uid, new LocatorHandler(locator, handler, finalOptions.times));
  }

  @Override
  public void removeLocatorHandler(Locator locator) {
    for (Map.Entry<Integer, LocatorHandler> entry: locatorHandlers.entrySet()) {
      if (entry.getValue().locator.equals(locator)) {
        locatorHandlers.remove(locator);
        PageUnregisterLocatorHandlerParams params = new PageUnregisterLocatorHandlerParams();
        params.uid = entry.getKey();
        try {
          channel.unregisterLocatorHandler(params, NO_TIMEOUT);
        } catch (PlaywrightException e) {
        }
      }
    }
  }

  private void onLocatorHandlerTriggered(int uid) {
    boolean remove = false;
    try {
      LocatorHandler handler = locatorHandlers.get(uid);
      remove = handler != null && handler.call();
    } finally {
      if (remove) {
        locatorHandlers.remove(uid);
      }
      PageResolveLocatorHandlerNoReplyParams params = new PageResolveLocatorHandlerNoReplyParams();
      params.uid = uid;
      params.remove = remove;
      sendMessageAsync("resolveLocatorHandlerNoReply", params);
    }
  }

  @Override
  public String ariaSnapshot(AriaSnapshotOptions options) {
    if (options == null) {
      options = new AriaSnapshotOptions();
    }
    FrameAriaSnapshotParams params = toProtocol(options, FrameAriaSnapshotParams.class);
    return mainFrame.channel.ariaSnapshot(params, mainFrame.timeout(options.timeout)).snapshot;
  }

  @Override
  public Object evalOnSelector(String selector, String pageFunction, Object arg, EvalOnSelectorOptions options) {
    return mainFrame.evalOnSelectorImpl(
      selector, pageFunction, arg, convertType(options, Frame.EvalOnSelectorOptions.class));
  }

  @Override
  public Object evalOnSelectorAll(String selector, String pageFunction, Object arg) {
    return mainFrame.evalOnSelectorAllImpl(selector, pageFunction, arg);
  }

  @Override
  public AutoCloseable addInitScript(String script) {
    return addInitScriptImpl(script);
  }

  @Override
  public AutoCloseable addInitScript(Path path) {
    try {
      byte[] bytes = readAllBytes(path);
      String script = addSourceUrlToScript(new String(bytes, UTF_8), path);
      return addInitScriptImpl(script);
    } catch (IOException e) {
      throw new PlaywrightException("Failed to read script from file", e);
    }
  }

  private AutoCloseable addInitScriptImpl(String script) {
    PageAddInitScriptParams params = new PageAddInitScriptParams();
    params.source = script;
    return channel.addInitScript(params, NO_TIMEOUT).disposable;
  }

  @Override
  public ElementHandle addScriptTag(AddScriptTagOptions options) {
    return mainFrame.addScriptTagImpl(convertType(options, Frame.AddScriptTagOptions.class));
  }

  @Override
  public ElementHandle addStyleTag(AddStyleTagOptions options) {
    return mainFrame.addStyleTagImpl(convertType(options, Frame.AddStyleTagOptions.class));
  }

  @Override
  public void bringToFront() {
    channel.bringToFront(NO_TIMEOUT);
  }

  @Override
  public void check(String selector, CheckOptions options) {
    mainFrame.check(selector, convertType(options, Frame.CheckOptions.class));
  }

  @Override
  public void click(String selector, ClickOptions options) {
    mainFrame.clickImpl(selector, convertType(options, Frame.ClickOptions.class), null);
  }

  @Override
  public String content(ContentOptions options) {
    return mainFrame.content(convertType(options, Frame.ContentOptions.class));
  }

  @Override
  public BrowserContextImpl context() {
    return browserContext;
  }

  @Override
  public void dblclick(String selector, DblclickOptions options) {
    mainFrame.dblclickImpl(selector, convertType(options, Frame.DblclickOptions.class), null);
  }

  @Override
  public void dispatchEvent(String selector, String type, Object eventInit, DispatchEventOptions options) {
    mainFrame.dispatchEvent(selector, type, eventInit, convertType(options, Frame.DispatchEventOptions.class));
  }

  @Override
  public void emulateMedia(EmulateMediaOptions options) {
    emulateMediaImpl(options);
  }

  private void emulateMediaImpl(EmulateMediaOptions options) {
    if (options == null) {
      options = new EmulateMediaOptions();
    }
    PageEmulateMediaParams params = toProtocol(options, PageEmulateMediaParams.class);
    channel.emulateMedia(params, NO_TIMEOUT);
  }

  @Override
  public Object evaluate(String expression, Object arg) {
    return mainFrame.evaluate(expression, arg);
  }

  @Override
  public JSHandle evaluateHandle(String pageFunction, Object arg) {
    return mainFrame.evaluateHandle(pageFunction, arg);
  }

  @Override
  public AutoCloseable exposeBinding(String name, BindingCallback playwrightBinding) {
    return exposeBindingImpl(name, playwrightBinding);
  }

  private AutoCloseable exposeBindingImpl(String name, BindingCallback playwrightBinding) {
    if (bindings.containsKey(name)) {
      throw new PlaywrightException("Function \"" + name + "\" has been already registered");
    }
    if (browserContext.bindings.containsKey(name)) {
      throw new PlaywrightException("Function \"" + name + "\" has been already registered in the browser context");
    }
    bindings.put(name, playwrightBinding);

    PageExposeBindingParams params = new PageExposeBindingParams();
    params.name = name;
    return channel.exposeBinding(params, NO_TIMEOUT).disposable;
  }

  @Override
  public AutoCloseable exposeFunction(String name, FunctionCallback playwrightFunction) {
    return exposeBindingImpl(name, (BindingCallback.Source source, Object... args) -> playwrightFunction.call(args));
  }

  @Override
  public void fill(String selector, String value, FillOptions options) {
    mainFrame.fill(selector, value, convertType(options, Frame.FillOptions.class));
  }

  @Override
  public void focus(String selector, FocusOptions options) {
    mainFrame.focus(selector, convertType(options, Frame.FocusOptions.class));
  }

  @Override
  public Frame frame(String name) {
    for (Frame frame : frames) {
      if (name.equals(frame.name())) {
        return frame;
      }
    }
    return null;
  }

  @Override
  public Frame frameByUrl(String glob) {
    return frameFor(UrlMatcher.forGlob(browserContext.baseUrl(), glob, this.connection.localUtils(), false));
  }

  @Override
  public Frame frameByUrl(Pattern pattern) {
    return frameFor(new UrlMatcher(pattern));
  }

  @Override
  public Frame frameByUrl(Predicate<String> predicate) {
    return frameFor(new UrlMatcher(predicate));
  }

  @Override
  public FrameLocator frameLocator(String selector) {
    return mainFrame.frameLocator(selector);
  }

  private Frame frameFor(UrlMatcher matcher) {
    for (Frame frame : frames) {
      if (matcher.test(frame.url())) {
        return frame;
      }
    }
    return null;
  }

  @Override
  public List<Frame> frames() {
    return new ArrayList<>(frames);
  }

  @Override
  public String getAttribute(String selector, String name, GetAttributeOptions options) {
    return mainFrame.getAttributeImpl(selector, name, convertType(options, Frame.GetAttributeOptions.class));
  }

  @Override
  public Locator getByAltText(String text, GetByAltTextOptions options) {
    return mainFrame.getByAltText(text, convertType(options, Frame.GetByAltTextOptions.class));
  }

  @Override
  public Locator getByAltText(Pattern text, GetByAltTextOptions options) {
    return mainFrame.getByAltText(text, convertType(options, Frame.GetByAltTextOptions.class));
  }

  @Override
  public Locator getByLabel(String text, GetByLabelOptions options) {
    return mainFrame.getByLabel(text, convertType(options, Frame.GetByLabelOptions.class));
  }

  @Override
  public Locator getByLabel(Pattern text, GetByLabelOptions options) {
    return mainFrame.getByLabel(text, convertType(options, Frame.GetByLabelOptions.class));
  }

  @Override
  public Locator getByPlaceholder(String text, GetByPlaceholderOptions options) {
    return mainFrame.getByPlaceholder(text, convertType(options, Frame.GetByPlaceholderOptions.class));
  }

  @Override
  public Locator getByPlaceholder(Pattern text, GetByPlaceholderOptions options) {
    return mainFrame.getByPlaceholder(text, convertType(options, Frame.GetByPlaceholderOptions.class));
  }

  @Override
  public Locator getByRef(String ref) {
    return locator("aria-ref=" + ref);
  }

  @Override
  public Locator getByRole(AriaRole role, GetByRoleOptions options) {
    return mainFrame.getByRole(role, convertType(options, Frame.GetByRoleOptions.class));
  }

  @Override
  public Locator getByTestId(String testId) {
    return mainFrame.getByTestId(testId);
  }

  @Override
  public Locator getByTestId(Pattern testId) {
    return mainFrame.getByTestId(testId);
  }

  @Override
  public Locator getByText(String text, GetByTextOptions options) {
    return mainFrame.getByText(text, convertType(options, Frame.GetByTextOptions.class));
  }

  @Override
  public Locator getByText(Pattern text, GetByTextOptions options) {
    return mainFrame.getByText(text, convertType(options, Frame.GetByTextOptions.class));
  }

  @Override
  public Locator getByTitle(String text, GetByTitleOptions options) {
    return mainFrame.getByTitle(text, convertType(options, Frame.GetByTitleOptions.class));
  }

  @Override
  public Locator getByTitle(Pattern text, GetByTitleOptions options) {
    return mainFrame.getByTitle(text, convertType(options, Frame.GetByTitleOptions.class));
  }

  @Override
  public Response goBack(GoBackOptions options) {
    return goBackImpl(options);
  }

  Response goBackImpl(GoBackOptions options) {
    if (options == null) {
      options = new GoBackOptions();
    }
    PageGoBackParams params = toProtocol(options, PageGoBackParams.class);
    return channel.goBack(params, timeoutSettings.navigationTimeout(options.timeout)).response;
  }

  @Override
  public Response goForward(GoForwardOptions options) {
    return goForwardImpl(options);
  }

  Response goForwardImpl(GoForwardOptions options) {
    if (options == null) {
      options = new GoForwardOptions();
    }
    PageGoForwardParams params = toProtocol(options, PageGoForwardParams.class);
    return channel.goForward(params, timeoutSettings.navigationTimeout(options.timeout)).response;
  }

  @Override
  public void requestGC() {
    channel.requestGC(NO_TIMEOUT);
  }

  @Override
  public ResponseImpl navigate(String url, NavigateOptions options) {
    return mainFrame.navigateImpl(url, convertType(options, Frame.NavigateOptions.class));
  }

  @Override
  public void hover(String selector, HoverOptions options) {
    mainFrame.hoverImpl(selector, convertType(options, Frame.HoverOptions.class));
  }

  @Override
  public void dragAndDrop(String source, String target, DragAndDropOptions options) {
    if (options == null) {
      options = new DragAndDropOptions();
    }
    mainFrame.dragAndDropImpl(source, target, convertType(options, Frame.DragAndDropOptions.class), options.steps);
  }

  @Override
  public String innerHTML(String selector, InnerHTMLOptions options) {
    return mainFrame.innerHTMLImpl(selector, convertType(options, Frame.InnerHTMLOptions.class));
  }

  @Override
  public String innerText(String selector, InnerTextOptions options) {
    return mainFrame.innerTextImpl(selector, convertType(options, Frame.InnerTextOptions.class));
  }

  @Override
  public String inputValue(String selector, InputValueOptions options) {
    return mainFrame.inputValueImpl(selector, convertType(options, Frame.InputValueOptions.class));
  }

  @Override
  public boolean isChecked(String selector, IsCheckedOptions options) {
    return mainFrame.isCheckedImpl(selector, convertType(options, Frame.IsCheckedOptions.class));
  }

  @Override
  public boolean isClosed() {
    return isClosed;
  }

  @Override
  public boolean isDisabled(String selector, IsDisabledOptions options) {
    return mainFrame.isDisabledImpl(selector, convertType(options, Frame.IsDisabledOptions.class));
  }

  @Override
  public boolean isEditable(String selector, IsEditableOptions options) {
    return mainFrame.isEditableImpl(selector, convertType(options, Frame.IsEditableOptions.class));
  }

  @Override
  public boolean isEnabled(String selector, IsEnabledOptions options) {
    return mainFrame.isEnabledImpl(selector, convertType(options, Frame.IsEnabledOptions.class));
  }

  @Override
  public boolean isHidden(String selector, IsHiddenOptions options) {
    return mainFrame.isHiddenImpl(selector, convertType(options, Frame.IsHiddenOptions.class));
  }

  @Override
  public boolean isVisible(String selector, IsVisibleOptions options) {
    return mainFrame.isVisibleImpl(selector, convertType(options, Frame.IsVisibleOptions.class));
  }

  @Override
  public Keyboard keyboard() {
    return keyboard;
  }

  @Override
  public List<ConsoleMessage> consoleMessages(ConsoleMessagesOptions options) {
    PageConsoleMessagesParams params = toProtocol(options, PageConsoleMessagesParams.class);
    PageConsoleMessagesResult json = channel.consoleMessages(params, NO_TIMEOUT);
    List<ConsoleMessage> result = new ArrayList<>();
    for (PageConsoleMessagesResult.PageConsoleMessagesResultMessages item : json.messages) {
      result.add(new ConsoleMessageImpl(this, item));
    }
    return result;
  }

  @Override
  public void clearConsoleMessages() {
    channel.clearConsoleMessages(NO_TIMEOUT);
  }

  @Override
  public void clearPageErrors() {
    channel.clearPageErrors(NO_TIMEOUT);
  }

  @Override
  public List<String> pageErrors() {
    PagePageErrorsResult json = channel.pageErrors(new PagePageErrorsParams(), NO_TIMEOUT);
    List<String> result = new ArrayList<>();
    for (SerializedError item : json.errors) {
      String errorStr = "";
      if (item.error != null) {
        errorStr = item.error.name + ": " + item.error.message;
        if (item.error.stack != null && !item.error.stack.isEmpty()) {
          errorStr += "\n" + item.error.stack;
        }
      }
      result.add(errorStr);
    }
    return result;
  }

  @Override
  public Locator locator(String selector, LocatorOptions options) {
    return mainFrame.locator(selector, convertType(options, Frame.LocatorOptions.class));
  }

  @Override
  public Frame mainFrame() {
    return mainFrame;
  }

  @Override
  public WebMCP webmcp() {
    return mainFrame.webmcp();
  }

  @Override
  public void hideHighlight() {
    channel.hideHighlight(NO_TIMEOUT);
  }

  @Override
  public Mouse mouse() {
    return mouse;
  }

  @Override
  public PageImpl opener() {
    if (opener == null || opener.isClosed()) {
      return null;
    }
    return opener;
  }

  @Override
  public void cancelPickLocator() {
    channel.cancelPickLocator(NO_TIMEOUT);
  }

  @Override
  public Locator pickLocator() {
    PagePickLocatorResult result = channel.pickLocator(NO_TIMEOUT);
    return new LocatorImpl(mainFrame, result.selector, null);
  }

  @Override
  public void pause() {
    TimeoutSettings settings = browserContext.timeoutSettings;
    Double defaultNavigationTimeout = settings.defaultNavigationTimeout();
    Double defaultTimeout = settings.defaultTimeout();
    settings.setDefaultNavigationTimeout(0.0);
    settings.setDefaultTimeout(0.0);
    try {
      runUntil(() -> {}, new WaitableRace<>(asList(context().pause(), (Waitable<JsonElement>) waitableClosedOrCrashed)));
    } finally {
      settings.setDefaultNavigationTimeout(defaultNavigationTimeout);
      settings.setDefaultTimeout(defaultTimeout);
    }
  }

  @Override
  public byte[] pdf(PdfOptions options) {
    return pdfImpl(options);
  }

  private byte[] pdfImpl(PdfOptions options) {
    if (options == null) {
      options = new PdfOptions();
    }
    PagePdfParams params = toProtocol(options, PagePdfParams.class);
    byte[] buffer = channel.pdf(params, NO_TIMEOUT).pdf;
    if (options.path != null) {
      Utils.writeToFile(buffer, options.path);
    }
    return buffer;
  }

  @Override
  public void press(String selector, String key, PressOptions options) {
    mainFrame.pressImpl(selector, key, convertType(options, Frame.PressOptions.class));
  }

  @Override
  public Response reload(ReloadOptions options) {
    return reloadImpl(options);
  }

  @Override
  public APIRequestContextImpl request() {
    return browserContext.request();
  }

  private Response reloadImpl(ReloadOptions options) {
    if (options == null) {
      options = new ReloadOptions();
    }
    PageReloadParams params = toProtocol(options, PageReloadParams.class);
    return channel.reload(params, timeoutSettings.navigationTimeout(options.timeout)).response;
  }

  @Override
  public AutoCloseable route(String url, Consumer<Route> handler, RouteOptions options) {
    route(UrlMatcher.forGlob(browserContext.baseUrl(), url, this.connection.localUtils(), false), handler, options);
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
      browserContext.recordIntoHar(this, har, convertType(options, BrowserContext.RouteFromHAROptions.class), null);
      return;
    }
    UrlMatcher matcher = UrlMatcher.forOneOf(browserContext.baseUrl(), options.url, this.connection.localUtils(), false);
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
    routeWebSocketImpl(UrlMatcher.forGlob(browserContext.baseUrl(), url, this.connection.localUtils(), true), handler);
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

  @Override
  public byte[] screenshot(ScreenshotOptions options) {
    return screenshotImpl(options);
  }

  @Override
  public List<String> selectOption(String selector, String value, SelectOptionOptions options) {
    String[] values = value == null ? null : new String[]{ value };
    return selectOption(selector, values, options);
  }

  @Override
  public List<String> selectOption(String selector, ElementHandle value, SelectOptionOptions options) {
    ElementHandle[] values = value == null ? null : new ElementHandle[]{ value };
    return selectOption(selector, values, options);
  }

  @Override
  public List<String> selectOption(String selector, String[] values, SelectOptionOptions options) {
    return mainFrame.selectOptionImpl(selector, values, convertType(options, Frame.SelectOptionOptions.class));
  }

  @Override
  public List<String> selectOption(String selector, SelectOption value, SelectOptionOptions options) {
    SelectOption[] values = value == null ? null : new SelectOption[]{value};
    return selectOption(selector, values, options);
  }

  private byte[] screenshotImpl(ScreenshotOptions options) {
    if (options == null) {
      options = new ScreenshotOptions();
    }
    if (options.type == null && options.path != null) {
      options.type = Utils.screenshotTypeForPath(options.path);
    }
    PageScreenshotParams params = toProtocol(this, options, PageScreenshotParams.class);
    byte[] buffer = channel.screenshot(params, timeoutSettings.timeout(options.timeout)).binary;
    if (options.path != null) {
      Utils.writeToFile(buffer, options.path);
    }
    return buffer;
  }

  @Override
  public List<String> selectOption(String selector, SelectOption[] values, SelectOptionOptions options) {
    return mainFrame.selectOptionImpl(selector, values, convertType(options, Frame.SelectOptionOptions.class));
  }

  @Override
  public List<String> selectOption(String selector, ElementHandle[] values, SelectOptionOptions options) {
    return mainFrame.selectOptionImpl(selector, values, convertType(options, Frame.SelectOptionOptions.class));
  }

  @Override
  public void setChecked(String selector, boolean checked, SetCheckedOptions options) {
    mainFrame.setCheckedImpl(selector, checked, convertType(options, Frame.SetCheckedOptions.class));
  }

  @Override
  public void setContent(String html, SetContentOptions options) {
    mainFrame.setContent(html, convertType(options, Frame.SetContentOptions.class));
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
    PageSetExtraHTTPHeadersParams params = new PageSetExtraHTTPHeadersParams();
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
  public void setInputFiles(String selector, Path files, SetInputFilesOptions options) {
    setInputFiles(selector, new Path[]{files}, options);
  }

  @Override
  public void setInputFiles(String selector, Path[] files, SetInputFilesOptions options) {
    mainFrame.setInputFilesImpl(selector, files, convertType(options, Frame.SetInputFilesOptions.class));
  }

  @Override
  public void setInputFiles(String selector, FilePayload files, SetInputFilesOptions options) {
    setInputFiles(selector, new FilePayload[]{files}, options);
  }

  @Override
  public void setInputFiles(String selector, FilePayload[] files, SetInputFilesOptions options) {
    mainFrame.setInputFilesImpl(selector, files, convertType(options, Frame.SetInputFilesOptions.class));
  }

  @Override
  public void setViewportSize(int width, int height) {
    viewport = new ViewportSize(width, height);
    PageSetViewportSizeParams params = new PageSetViewportSizeParams();
    params.viewportSize = new PageSetViewportSizeParams.PageSetViewportSizeParamsViewportSize();
    params.viewportSize.width = width;
    params.viewportSize.height = height;
    channel.setViewportSize(params, NO_TIMEOUT);
  }

  @Override
  public void tap(String selector, TapOptions options) {
    mainFrame.tap(selector, convertType(options, Frame.TapOptions.class));
  }

  @Override
  public String textContent(String selector, TextContentOptions options) {
    return mainFrame.textContent(selector, convertType(options, Frame.TextContentOptions.class));
  }

  @Override
  public String title() {
    return mainFrame.title();
  }

  @Override
  public Touchscreen touchscreen() {
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
  public void type(String selector, String text, TypeOptions options) {
    mainFrame.type(selector, text, convertType(options, Frame.TypeOptions.class));
  }

  @Override
  public void uncheck(String selector, UncheckOptions options) {
    mainFrame.uncheck(selector, convertType(options, Frame.UncheckOptions.class));
  }

  @Override
  public void unrouteAll() {
    routes.removeAll();
    updateInterceptionPatterns();
  }

  @Override
  public void unroute(String url, Consumer<Route> handler) {
    unroute(UrlMatcher.forGlob(browserContext.baseUrl(), url, this.connection.localUtils(), false), handler);
  }

  @Override
  public void unroute(Pattern url, Consumer<Route> handler) {
    unroute(new UrlMatcher(url), handler);
  }

  @Override
  public void unroute(Predicate<String> url, Consumer<Route> handler) {
    unroute(new UrlMatcher(url), handler);
  }

  private void unroute(UrlMatcher matcher, Consumer<Route> handler) {
    routes.remove(matcher, handler);
    updateInterceptionPatterns();
  }

  private void updateInterceptionPatterns() {
    channel.setNetworkInterceptionPatterns(
      connection.protocolGson().fromJson(routes.interceptionPatterns(), PageSetNetworkInterceptionPatternsParams.class), NO_TIMEOUT);
  }

  private void updateWebSocketInterceptionPatterns() {
    channel.setWebSocketInterceptionPatterns(
      connection.protocolGson().fromJson(webSocketRoutes.interceptionPatterns(), PageSetWebSocketInterceptionPatternsParams.class), NO_TIMEOUT);
  }

  @Override
  public String url() {
    return mainFrame.url();
  }


  @Override
  public VideoImpl video() {
    return video;
  }

  @Override
  public ViewportSize viewportSize() {
    return viewport;
  }

  <T> Waitable<T> createWaitableNavigationTimeout(Double timeout) {
    return new WaitableTimeout<>(timeoutSettings.navigationTimeout(timeout));
  }

  <T> Waitable<T> createWaitableTimeout(Double timeout) {
    return timeoutSettings.createWaitable(timeout);
  }

  @Override
  public JSHandle waitForFunction(String pageFunction, Object arg, WaitForFunctionOptions options) {
    return mainFrame.waitForFunction(pageFunction, arg, convertType(options, Frame.WaitForFunctionOptions.class));
  }

  @Override
  public void waitForLoadState(LoadState state, WaitForLoadStateOptions options) {
    final LoadState loadState = state == null ? LoadState.LOAD : state;
    withTitle("Wait for load state \"" + loadState.toString().toLowerCase() + "\"", () -> {
      withWaitLogging("Page.waitForLoadState", logger -> {
        mainFrame.waitForLoadStateImpl(loadState, convertType(options, Frame.WaitForLoadStateOptions.class), logger);
        return null;
      });
    });
  }

  @Override
  public Response waitForNavigation(WaitForNavigationOptions options, Runnable code) {
    return withWaitLoggingTitled("Page.waitForNavigation", "Wait for navigation", logger -> waitForNavigationImpl(logger, code, options));
  }

  private Response waitForNavigationImpl(Logger logger, Runnable code, WaitForNavigationOptions options) {
    Frame.WaitForNavigationOptions frameOptions = new Frame.WaitForNavigationOptions();
    if (options != null) {
      frameOptions.timeout = options.timeout;
      frameOptions.waitUntil = options.waitUntil;
      frameOptions.url = options.url;
    }
    return mainFrame.waitForNavigationImpl(logger, code, frameOptions);
  }

  void frameNavigated(FrameImpl frame) {
    emit(Events.Page.FRAME_NAVIGATED, frame);
    browserContext.notifyFrameNavigated(frame);
  }

  private class WaitableFrameDetach extends WaitableEvent<Frame> {
    WaitableFrameDetach(Frame frameArg) {
      super(PageImpl.this, Events.Page.FRAME_DETACHED, detachedFrame -> frameArg.equals(detachedFrame));
    }

    @Override
    public Frame get() {
      throw new PlaywrightException("Navigating frame was detached");
    }
  }

  @SuppressWarnings("unchecked")
  <T> Waitable<T> createWaitableFrameDetach(Frame frame) {
    // It is safe to cast as WaitableFrameDetach.get() always throws.
    return (Waitable<T>) new WaitableFrameDetach(frame);
  }

  <T> Waitable<T> createWaitForCloseHelper() {
    return createWaitForCloseHelper(false);
  }

  /** With {@code rejectIfClosed}, it is done at once when the page is already closed, like the navigation waiter of the Node.js client. */
  <T> Waitable<T> createWaitForCloseHelper(boolean rejectIfClosed) {
    return new WaitableRace<T>(asList(new WaitablePageClose(rejectIfClosed && isClosed), new WaitablePageCrash()));
  }

  private class WaitablePageClose<T> extends WaitableEvent<T> {
    private final boolean closedBefore;

    WaitablePageClose(boolean closedBefore) {
      super(PageImpl.this, Events.Page.CLOSE);
      this.closedBefore = closedBefore;
    }

    @Override
    public boolean isDone() {
      return closedBefore || super.isDone();
    }

    @Override
    public T get() {
      throw new TargetClosedError(effectiveCloseReason());
    }
  }

  private class WaitablePageCrash<T> extends WaitableEvent<T> {
    WaitablePageCrash() {
      super(PageImpl.this, Events.Page.CRASH);
    }

    @Override
    public T get() {
      throw new TargetClosedError("Page crashed");
    }
  }

  @Override
  public Request waitForRequest(String urlGlob, WaitForRequestOptions options, Runnable code) {
    return waitForRequest(UrlMatcher.forGlob(browserContext.baseUrl(), urlGlob, this.connection.localUtils(), false), null, options, code);
  }

  @Override
  public Request waitForRequest(Pattern urlPattern, WaitForRequestOptions options, Runnable code) {
    return waitForRequest(new UrlMatcher(urlPattern), null, options, code);
  }

  @Override
  public Request waitForRequest(Predicate<Request> predicate, WaitForRequestOptions options, Runnable code) {
    return waitForRequest(null, predicate, options, code);
  }

  private Request waitForRequest(UrlMatcher urlMatcher, Predicate<Request> predicate, WaitForRequestOptions options, Runnable code) {
    return withWaitLogging("Page.waitForRequest", "request", logger -> {
      logger.log("waiting for request " + ((urlMatcher == null) ? "matching predicate" : urlMatcher.toString()));
      Predicate<Request> requestPredicate = predicate;
      if (requestPredicate == null) {
        requestPredicate = request -> urlMatcher.test(request.url());;
      }
      return waitForRequestImpl(requestPredicate, options, code);
    });
  }

  private Request waitForRequestImpl(Predicate<Request> predicate, WaitForRequestOptions options, Runnable code) {
    if (options == null) {
      options = new WaitForRequestOptions();
    }
    return waitForEventWithTimeout(Events.Page.REQUEST, code, predicate, options.timeout);
  }

  @Override
  public Request waitForRequestFinished(WaitForRequestFinishedOptions options, Runnable code) {
    return withWaitLogging("Page.waitForRequestFinished", "requestfinished", logger -> waitForRequestFinishedImpl(options, code));
  }

  private Request waitForRequestFinishedImpl(WaitForRequestFinishedOptions options, Runnable code) {
    if (options == null) {
      options = new WaitForRequestFinishedOptions();
    }
    return waitForEventWithTimeout(Events.Page.REQUEST_FINISHED, code, options.predicate, options.timeout);
  }

  @Override
  public Response waitForResponse(String urlGlob, WaitForResponseOptions options, Runnable code) {
    return waitForResponse(UrlMatcher.forGlob(browserContext.baseUrl(), urlGlob, this.connection.localUtils(), false), null, options, code);
  }

  @Override
  public Response waitForResponse(Pattern urlPattern, WaitForResponseOptions options, Runnable code) {
    return waitForResponse(new UrlMatcher(urlPattern), null, options, code);
  }

  @Override
  public Response waitForResponse(Predicate<Response> predicate, WaitForResponseOptions options, Runnable code) {
    return waitForResponse(null, predicate, options, code);
  }

  private Response waitForResponse(UrlMatcher urlMatcher, Predicate<Response> predicate, WaitForResponseOptions options, Runnable code) {
    return withWaitLogging("Page.waitForResponse", "response", logger -> {
      logger.log("waiting for response " + ((urlMatcher == null) ? "matching predicate" : urlMatcher.toString()));
      Predicate<Response> responsePredicate = predicate;
      if (responsePredicate == null) {
        responsePredicate = response -> urlMatcher.test(response.url());;
      }
      return waitForResponseImpl(responsePredicate, options, code);
    });
  }

  private Response waitForResponseImpl(Predicate<Response> predicate, WaitForResponseOptions options, Runnable code) {
    if (options == null) {
      options = new WaitForResponseOptions();
    }
    return waitForEventWithTimeout(Events.Page.RESPONSE, code, predicate, options.timeout);
  }

  @Override
  public ElementHandle waitForSelector(String selector, WaitForSelectorOptions options) {
    return mainFrame.waitForSelector(selector, convertType(options, Frame.WaitForSelectorOptions.class));
  }

  @Override
  public void waitForCondition(BooleanSupplier predicate, WaitForConditionOptions options) {
    List<Waitable<Void>> waitables = new ArrayList<>();
    waitables.add(createWaitForCloseHelper());
    waitables.add(createWaitableTimeout(options == null ? null : options.timeout));
    waitables.add(new WaitablePredicate<>(predicate));
    runUntil(() -> {}, new WaitableRace<>(waitables));
  }

  @Override
  public void waitForTimeout(double timeout) {
    mainFrame.waitForTimeout(timeout);
  }

  @Override
  public void waitForURL(String url, WaitForURLOptions options) {
    waitForURL(UrlMatcher.forGlob(browserContext.baseUrl(), url, this.connection.localUtils(), false), options);
  }

  @Override
  public void waitForURL(Pattern url, WaitForURLOptions options) {
    waitForURL(new UrlMatcher(url), options);
  }

  @Override
  public void waitForURL(Predicate<String> url, WaitForURLOptions options) {
    waitForURL(new UrlMatcher(url), options);
  }

  private void waitForURL(UrlMatcher matcher, WaitForURLOptions options) {
    withWaitLogging("Page.waitForURL", logger -> {
      mainFrame.waitForURLImpl(logger, matcher, convertType(options, Frame.WaitForURLOptions.class));
      return null;
    });
  }

  @Override
  public List<Worker> workers() {
    return new ArrayList<>(workers);
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
}
