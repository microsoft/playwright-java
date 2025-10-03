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
import java.nio.file.Path;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.Serialization.parseError;
import static com.microsoft.playwright.impl.Utils.*;
import static com.microsoft.playwright.options.ScreenshotType.JPEG;
import static com.microsoft.playwright.options.ScreenshotType.PNG;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;
import static java.util.Arrays.asList;


public class PageImpl extends ChannelOwner implements Page {
  private final BrowserContextImpl browserContext;
  private final FrameImpl mainFrame;
  private final KeyboardImpl keyboard;
  private final MouseImpl mouse;
  private final TouchscreenImpl touchscreen;
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

  private static final Map<EventType, String> eventSubscriptions() {
    Map<EventType, String> result = new HashMap<>();
    result.put(EventType.CONSOLE, "console");
    result.put(EventType.DIALOG, "dialog");
    result.put(EventType.REQUEST, "request");
    result.put(EventType.RESPONSE, "response");
    result.put(EventType.REQUESTFINISHED, "requestFinished");
    result.put(EventType.REQUESTFAILED, "requestFailed");
    result.put(EventType.FILECHOOSER, "fileChooser");
    return result;
  }
  final ListenerCollection<EventType> listeners = new ListenerCollection<EventType>(eventSubscriptions(), this);
  final Map<String, BindingCallback> bindings = new HashMap<>();
  BrowserContextImpl ownedContext;
  private boolean isClosed;
  final Set<Worker> workers = new HashSet<>();
  protected final TimeoutSettings timeoutSettings;
  private VideoImpl video;
  private final PageImpl opener;
  private String closeReason;

  enum EventType {
    CLOSE,
    CONSOLE,
    CRASH,
    DIALOG,
    DOMCONTENTLOADED,
    DOWNLOAD,
    FILECHOOSER,
    FRAMEATTACHED,
    FRAMEDETACHED,
    FRAMENAVIGATED,
    LOAD,
    PAGEERROR,
    POPUP,
    REQUEST,
    REQUESTFAILED,
    REQUESTFINISHED,
    RESPONSE,
    WEBSOCKET,
    WORKER,
  }

  PageImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    browserContext = (BrowserContextImpl) parent;
    mainFrame = connection.getExistingObject(initializer.getAsJsonObject("mainFrame").get("guid").getAsString());
    mainFrame.page = this;
    isClosed = initializer.get("isClosed").getAsBoolean();
    if (initializer.has("viewportSize")) {
      viewport = gson().fromJson(initializer.get("viewportSize"), ViewportSize.class);
    }
    keyboard = new KeyboardImpl(this);
    mouse = new MouseImpl(this);
    touchscreen = new TouchscreenImpl(this);
    frames.add(mainFrame);
    timeoutSettings = new TimeoutSettings(browserContext.timeoutSettings);
    waitableClosedOrCrashed = createWaitForCloseHelper();
    if (initializer.has("opener")) {
      opener = connection.getExistingObject(initializer.getAsJsonObject("opener").get("guid").getAsString());
    } else {
      opener = null;
    }
  }

  @Override
  protected void handleEvent(String event, JsonObject params) {
    if ("worker".equals(event)) {
      String guid = params.getAsJsonObject("worker").get("guid").getAsString();
      WorkerImpl worker = connection.getExistingObject(guid);
      worker.page = this;
      workers.add(worker);
      listeners.notify(EventType.WORKER, worker);
    } else if ("webSocket".equals(event)) {
      String guid = params.getAsJsonObject("webSocket").get("guid").getAsString();
      WebSocketImpl webSocket = connection.getExistingObject(guid);
      listeners.notify(EventType.WEBSOCKET, webSocket);
    } else if ("download".equals(event)) {
      String artifactGuid = params.getAsJsonObject("artifact").get("guid").getAsString();
      ArtifactImpl artifact = connection.getExistingObject(artifactGuid);
      DownloadImpl download = new DownloadImpl(this, artifact, params);
      listeners.notify(EventType.DOWNLOAD, download);
    } else if ("fileChooser".equals(event)) {
      String guid = params.getAsJsonObject("element").get("guid").getAsString();
      ElementHandleImpl elementHandle = connection.getExistingObject(guid);
      FileChooser fileChooser = new FileChooserImpl(this, elementHandle, params.get("isMultiple").getAsBoolean());
      listeners.notify(EventType.FILECHOOSER, fileChooser);
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
      listeners.notify(EventType.FRAMEATTACHED, frame);
    } else if ("frameDetached".equals(event)) {
      String guid = params.getAsJsonObject("frame").get("guid").getAsString();
      FrameImpl frame = connection.getExistingObject(guid);
      frames.remove(frame);
      frame.isDetached = true;
      if (frame.parentFrame != null) {
        frame.parentFrame.childFrames.remove(frame);
      }
      listeners.notify(EventType.FRAMEDETACHED, frame);
    } else if ("locatorHandlerTriggered".equals(event)) {
      int uid = params.get("uid").getAsInt();
      onLocatorHandlerTriggered(uid);
    } else if ("route".equals(event)) {
      RouteImpl route = connection.getExistingObject(params.getAsJsonObject("route").get("guid").getAsString());
      route.browserContext = browserContext;
      Router.HandleResult handled = routes.handle(route);
      if (handled != Router.HandleResult.NoMatchingHandler) {
        updateInterceptionPatterns();
      }
      if (handled == Router.HandleResult.NoMatchingHandler || handled == Router.HandleResult.Fallback) {
        browserContext.handleRoute(route);
      }
    } else if ("webSocketRoute".equals(event)) {
      WebSocketRouteImpl route = connection.getExistingObject(params.getAsJsonObject("webSocketRoute").get("guid").getAsString());
      if (!webSocketRoutes.handle(route)) {
        browserContext.handleWebSocketRoute(route);
      }
    } else if ("video".equals(event)) {
      String artifactGuid = params.getAsJsonObject("artifact").get("guid").getAsString();
      ArtifactImpl artifact = connection.getExistingObject(artifactGuid);
      forceVideo().setArtifact(artifact);
    } else if ("crash".equals(event)) {
      listeners.notify(EventType.CRASH, this);
    } else if ("close".equals(event)) {
      didClose();
    }
  }

  void notifyPopup(PageImpl popup) {
    listeners.notify(EventType.POPUP, popup);
  }

  void didClose() {
    isClosed = true;
    browserContext.pages.remove(this);
    browserContext.backgroundPages.remove(this);
    listeners.notify(EventType.CLOSE, this);
  }

  private String effectiveCloseReason() {
    if (closeReason != null) {
      return closeReason;
    }
    return browserContext.effectiveCloseReason();
  }

  @Override
  public void onClose(Consumer<Page> handler) {
    listeners.add(EventType.CLOSE, handler);
  }

  @Override
  public void offClose(Consumer<Page> handler) {
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
  public void onCrash(Consumer<Page> handler) {
    listeners.add(EventType.CRASH, handler);
  }

  @Override
  public void offCrash(Consumer<Page> handler) {
    listeners.remove(EventType.CRASH, handler);
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
  public void onDOMContentLoaded(Consumer<Page> handler) {
    listeners.add(EventType.DOMCONTENTLOADED, handler);
  }

  @Override
  public void offDOMContentLoaded(Consumer<Page> handler) {
    listeners.remove(EventType.DOMCONTENTLOADED, handler);
  }

  @Override
  public void onDownload(Consumer<Download> handler) {
    listeners.add(EventType.DOWNLOAD, handler);
  }

  @Override
  public void offDownload(Consumer<Download> handler) {
    listeners.remove(EventType.DOWNLOAD, handler);
  }

  @Override
  public void onFileChooser(Consumer<FileChooser> handler) {
    listeners.add(EventType.FILECHOOSER, handler);
  }

  @Override
  public void offFileChooser(Consumer<FileChooser> handler) {
    listeners.remove(EventType.FILECHOOSER, handler);
  }

  @Override
  public void onFrameAttached(Consumer<Frame> handler) {
    listeners.add(EventType.FRAMEATTACHED, handler);
  }

  @Override
  public void offFrameAttached(Consumer<Frame> handler) {
    listeners.remove(EventType.FRAMEATTACHED, handler);
  }

  @Override
  public void onFrameDetached(Consumer<Frame> handler) {
    listeners.add(EventType.FRAMEDETACHED, handler);
  }

  @Override
  public void offFrameDetached(Consumer<Frame> handler) {
    listeners.remove(EventType.FRAMEDETACHED, handler);
  }

  @Override
  public void onFrameNavigated(Consumer<Frame> handler) {
    listeners.add(EventType.FRAMENAVIGATED, handler);
  }

  @Override
  public void offFrameNavigated(Consumer<Frame> handler) {
    listeners.remove(EventType.FRAMENAVIGATED, handler);
  }

  @Override
  public void onLoad(Consumer<Page> handler) {
    listeners.add(EventType.LOAD, handler);
  }

  @Override
  public void offLoad(Consumer<Page> handler) {
    listeners.remove(EventType.LOAD, handler);
  }

  @Override
  public void onPageError(Consumer<String> handler) {
    listeners.add(EventType.PAGEERROR, handler);
  }

  @Override
  public void offPageError(Consumer<String> handler) {
    listeners.remove(EventType.PAGEERROR, handler);
  }

  @Override
  public void onPopup(Consumer<Page> handler) {
    listeners.add(EventType.POPUP, handler);
  }

  @Override
  public void offPopup(Consumer<Page> handler) {
    listeners.remove(EventType.POPUP, handler);
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

  @Override
  public void onWebSocket(Consumer<WebSocket> handler) {
    listeners.add(EventType.WEBSOCKET, handler);
  }

  @Override
  public void offWebSocket(Consumer<WebSocket> handler) {
    listeners.remove(EventType.WEBSOCKET, handler);
  }

  @Override
  public void onWorker(Consumer<Worker> handler) {
    listeners.add(EventType.WORKER, handler);
  }

  @Override
  public void offWorker(Consumer<Worker> handler) {
    listeners.remove(EventType.WORKER, handler);
  }

  @Override
  public ClockImpl clock() {
    return browserContext.clock();
  }

  @Override
  public Page waitForClose(WaitForCloseOptions options, Runnable code) {
    return withWaitLogging("Page.waitForClose", logger -> waitForCloseImpl(options, code));
  }

  private Page waitForCloseImpl(WaitForCloseOptions options, Runnable code) {
    if (options == null) {
      options = new WaitForCloseOptions();
    }
    return waitForEventWithTimeout(EventType.CLOSE, code, null, options.timeout);
  }

  @Override
  public ConsoleMessage waitForConsoleMessage(WaitForConsoleMessageOptions options, Runnable code) {
    return withWaitLogging("Page.waitForConsoleMessage", logger -> waitForConsoleMessageImpl(options, code));
  }

  private ConsoleMessage waitForConsoleMessageImpl(WaitForConsoleMessageOptions options, Runnable code) {
    if (options == null) {
      options = new WaitForConsoleMessageOptions();
    }
    return waitForEventWithTimeout(EventType.CONSOLE, code, options.predicate, options.timeout);
  }

  @Override
  public Download waitForDownload(WaitForDownloadOptions options, Runnable code) {
    return withWaitLogging("Page.waitForDownload", logger -> waitForDownloadImpl(options, code));
  }

  private Download waitForDownloadImpl(WaitForDownloadOptions options, Runnable code) {
    if (options == null) {
      options = new WaitForDownloadOptions();
    }
    return waitForEventWithTimeout(EventType.DOWNLOAD, code, options.predicate, options.timeout);
  }

  @Override
  public FileChooser waitForFileChooser(WaitForFileChooserOptions options, Runnable code) {
    return withWaitLogging("Page.waitForFileChooser", logger -> waitForFileChooserImpl(options, code));
  }

  private FileChooser waitForFileChooserImpl(WaitForFileChooserOptions options, Runnable code) {
    // TODO: enable/disable file chooser interception
    if (options == null) {
      options = new WaitForFileChooserOptions();
    }
    return waitForEventWithTimeout(EventType.FILECHOOSER, code, options.predicate, options.timeout);
  }

  @Override
  public Page waitForPopup(WaitForPopupOptions options, Runnable code) {
    return withWaitLogging("Page.waitForPopup", logger -> waitForPopupImpl(options, code));
  }

  private Page waitForPopupImpl(WaitForPopupOptions options, Runnable code) {
    if (options == null) {
      options = new WaitForPopupOptions();
    }
    return waitForEventWithTimeout(EventType.POPUP, code, options.predicate, options.timeout);
  }

  @Override
  public WebSocket waitForWebSocket(WaitForWebSocketOptions options, Runnable code) {
    return withWaitLogging("Page.waitForWebSocket", logger -> waitForWebSocketImpl(options, code));
  }

  private WebSocket waitForWebSocketImpl(WaitForWebSocketOptions options, Runnable code) {
    if (options == null) {
      options = new WaitForWebSocketOptions();
    }
    return waitForEventWithTimeout(EventType.WEBSOCKET, code, options.predicate, options.timeout);
  }

  @Override
  public Worker waitForWorker(WaitForWorkerOptions options, Runnable code) {
    return withWaitLogging("Page.waitForWorker", logger -> waitForWorkerImpl(options, code));
  }

  private Worker waitForWorkerImpl(WaitForWorkerOptions options, Runnable code) {
    if (options == null) {
      options = new WaitForWorkerOptions();
    }
    return waitForEventWithTimeout(EventType.WORKER, code, options.predicate, options.timeout);
  }

  private <T> T waitForEventWithTimeout(EventType eventType, Runnable code, Predicate<T> predicate, Double timeout) {
    List<Waitable<T>> waitables = new ArrayList<>();
    waitables.add(new WaitableEvent<>(listeners, eventType, predicate));
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
      } else {
        JsonObject params = gson().toJsonTree(options).getAsJsonObject();
        sendMessage("close", params, NO_TIMEOUT);
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
    JsonObject json = sendMessage("requests", new JsonObject(), NO_TIMEOUT).getAsJsonObject();
    JsonArray requests = json.getAsJsonArray("requests");
    List<Request> result = new ArrayList<>();
    for (JsonElement item : requests) {
      result.add(connection.getExistingObject(item.getAsJsonObject().get("guid").getAsString()));
    }
    return result;
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
    JsonObject params = new JsonObject();
    params.addProperty("selector", locatorImpl.selector);
    if (finalOptions.noWaitAfter != null && finalOptions.noWaitAfter) {
      params.addProperty("noWaitAfter", true);
    }
    params.addProperty("selector", locatorImpl.selector);
    JsonObject json = (JsonObject) sendMessage("registerLocatorHandler", params, NO_TIMEOUT);
    int uid = json.get("uid").getAsInt();
    locatorHandlers.put(uid, new LocatorHandler(locator, handler, finalOptions.times));
  }

  @Override
  public void removeLocatorHandler(Locator locator) {
    for (Map.Entry<Integer, LocatorHandler> entry: locatorHandlers.entrySet()) {
      if (entry.getValue().locator.equals(locator)) {
        locatorHandlers.remove(locator);
        JsonObject params = new JsonObject();
        params.addProperty("uid", entry.getKey());
        try {
          sendMessage("unregisterLocatorHandler", params, NO_TIMEOUT);
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
      JsonObject params = new JsonObject();
      params.addProperty("uid", uid);
      params.addProperty("remove", remove);
      sendMessageAsync("resolveLocatorHandlerNoReply", params);
    }
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
  public void addInitScript(String script) {
    addInitScriptImpl(script);
  }

  @Override
  public void addInitScript(Path path) {
    try {
      byte[] bytes = readAllBytes(path);
      String script = addSourceUrlToScript(new String(bytes, UTF_8), path);
      addInitScriptImpl(script);
    } catch (IOException e) {
      throw new PlaywrightException("Failed to read script from file", e);
    }
  }

  private void addInitScriptImpl(String script) {
    JsonObject params = new JsonObject();
    params.addProperty("source", script);
    sendMessage("addInitScript", params, NO_TIMEOUT);
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
    sendMessage("bringToFront");
  }

  @Override
  public void check(String selector, CheckOptions options) {
    mainFrame.check(selector, convertType(options, Frame.CheckOptions.class));
  }

  @Override
  public void click(String selector, ClickOptions options) {
    mainFrame.clickImpl(selector, convertType(options, Frame.ClickOptions.class));
  }

  @Override
  public String content() {
    return mainFrame.content();
  }

  @Override
  public BrowserContextImpl context() {
    return browserContext;
  }

  @Override
  public void dblclick(String selector, DblclickOptions options) {
    mainFrame.dblclick(selector, convertType(options, Frame.DblclickOptions.class));
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
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    sendMessage("emulateMedia", params, NO_TIMEOUT);
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
  public void exposeBinding(String name, BindingCallback playwrightBinding, ExposeBindingOptions options) {
    exposeBindingImpl(name, playwrightBinding, options);
  }

  private void exposeBindingImpl(String name, BindingCallback playwrightBinding, ExposeBindingOptions options) {
    if (bindings.containsKey(name)) {
      throw new PlaywrightException("Function \"" + name + "\" has been already registered");
    }
    if (browserContext.bindings.containsKey(name)) {
      throw new PlaywrightException("Function \"" + name + "\" has been already registered in the browser context");
    }
    bindings.put(name, playwrightBinding);

    JsonObject params = new JsonObject();
    params.addProperty("name", name);
    if (options != null && options.handle != null && options.handle) {
      params.addProperty("needsHandle", true);
    }
    sendMessage("exposeBinding", params, NO_TIMEOUT);
  }

  @Override
  public void exposeFunction(String name, FunctionCallback playwrightFunction) {
    exposeBindingImpl(name, (BindingCallback.Source source, Object... args) -> playwrightFunction.call(args), null);
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
    return frameFor(UrlMatcher.forGlob(browserContext.baseUrl(), glob, this.connection.localUtils, false));
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
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    JsonObject json = sendMessage("goBack", params, timeoutSettings.navigationTimeout(options.timeout)).getAsJsonObject();
    if (json.has("response")) {
      return connection.getExistingObject(json.getAsJsonObject("response").get("guid").getAsString());
    }
    return null;
  }

  @Override
  public Response goForward(GoForwardOptions options) {
    return goForwardImpl(options);
  }

  Response goForwardImpl(GoForwardOptions options) {
    if (options == null) {
      options = new GoForwardOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    JsonObject json = sendMessage("goForward", params, timeoutSettings.navigationTimeout(options.timeout)).getAsJsonObject();
    if (json.has("response")) {
      return connection.getExistingObject(json.getAsJsonObject("response").get("guid").getAsString());
    }
    return null;
  }

  @Override
  public void requestGC() {
    sendMessage("requestGC");
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
    mainFrame.dragAndDropImpl(source, target, convertType(options, Frame.DragAndDropOptions.class));
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
  public List<ConsoleMessage> consoleMessages() {
    JsonObject json = sendMessage("consoleMessages", new JsonObject(), NO_TIMEOUT).getAsJsonObject();
    JsonArray messages = json.getAsJsonArray("messages");
    List<ConsoleMessage> result = new ArrayList<>();
    for (JsonElement item : messages) {
      result.add(new ConsoleMessageImpl(connection, item.getAsJsonObject(), this));
    }
    return result;
  }

  @Override
  public List<String> pageErrors() {
    JsonObject json = sendMessage("pageErrors", new JsonObject(), NO_TIMEOUT).getAsJsonObject();
    JsonArray errors = json.getAsJsonArray("errors");
    List<String> result = new ArrayList<>();
    for (JsonElement item : errors) {
      String errorStr = parseError(item.getAsJsonObject());
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
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.remove("path");
    JsonObject json = sendMessage("pdf", params, NO_TIMEOUT).getAsJsonObject();
    byte[] buffer = Base64.getDecoder().decode(json.get("pdf").getAsString());
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
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    JsonObject json = sendMessage("reload", params, timeoutSettings.navigationTimeout(options.timeout)).getAsJsonObject();
    if (json.has("response")) {
      return connection.getExistingObject(json.getAsJsonObject("response").get("guid").getAsString());
    }
    return null;
  }

  @Override
  public void route(String url, Consumer<Route> handler, RouteOptions options) {
    route(UrlMatcher.forGlob(browserContext.baseUrl(), url, this.connection.localUtils, false), handler, options);
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
      browserContext.recordIntoHar(this, har, convertType(options, BrowserContext.RouteFromHAROptions.class), null);
      return;
    }
    UrlMatcher matcher = UrlMatcher.forOneOf(browserContext.baseUrl(), options.url, this.connection.localUtils, false);
    HARRouter harRouter = new HARRouter(connection.localUtils, har, options.notFound);
    onClose(context -> harRouter.dispose());
    route(matcher, route -> harRouter.handle(route), null);
  }

  private void route(UrlMatcher matcher, Consumer<Route> handler, RouteOptions options) {
    routes.add(matcher, handler, options == null ? null : options.times);
    updateInterceptionPatterns();
  }

    @Override
  public void routeWebSocket(String url, Consumer<WebSocketRoute> handler) {
    routeWebSocketImpl(UrlMatcher.forGlob(browserContext.baseUrl(), url, this.connection.localUtils, true), handler);
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
    if (options.type == null) {
      options.type = PNG;
      if (options.path != null) {
        String fileName = options.path.getFileName().toString();
        int extStart = fileName.lastIndexOf('.');
        if (extStart != -1) {
          String extension = fileName.substring(extStart).toLowerCase();
          if (".jpeg".equals(extension) || ".jpg".equals(extension)) {
            options.type = JPEG;
          }
        }
      }
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.remove("path");
    JsonObject json = sendMessage("screenshot", params, timeoutSettings.timeout(options.timeout)).getAsJsonObject();

    byte[] buffer = Base64.getDecoder().decode(json.get("binary").getAsString());
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
    JsonObject params = new JsonObject();
    JsonArray jsonHeaders = new JsonArray();
    for (Map.Entry<String, String> e : headers.entrySet()) {
      JsonObject header = new JsonObject();
      header.addProperty("name", e.getKey());
      header.addProperty("value", e.getValue());
      jsonHeaders.add(header);
    }
    params.add("headers", jsonHeaders);
    sendMessage("setExtraHTTPHeaders", params, NO_TIMEOUT);
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
    JsonObject params = new JsonObject();
    params.add("viewportSize", gson().toJsonTree(viewport));
    sendMessage("setViewportSize", params, NO_TIMEOUT);
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
    unroute(UrlMatcher.forGlob(browserContext.baseUrl(), url, this.connection.localUtils, false), handler);
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
    sendMessage("setNetworkInterceptionPatterns", routes.interceptionPatterns(), NO_TIMEOUT);
  }

  private void updateWebSocketInterceptionPatterns() {
    sendMessage("setWebSocketInterceptionPatterns", webSocketRoutes.interceptionPatterns(), NO_TIMEOUT);
  }

  @Override
  public String url() {
    return mainFrame.url();
  }


  private VideoImpl forceVideo() {
    if (video == null) {
      video = new VideoImpl(this);
    }
    return video;
  }

  @Override
  public VideoImpl video() {
    // Note: we are creating Video object lazily, because we do not know
    // BrowserContextOptions when constructing the page - it is assigned
    // too late during launchPersistentContext.
    if (browserContext.videosDir() == null) {
      return null;
    }
    return forceVideo();
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
    return withWaitLogging("Page.waitForNavigation", logger -> waitForNavigationImpl(logger, code, options));
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
    listeners.notify(EventType.FRAMENAVIGATED, frame);
  }

  private class WaitableFrameDetach extends WaitableEvent<EventType, Frame> {
    WaitableFrameDetach(Frame frameArg) {
      super(PageImpl.this.listeners, EventType.FRAMEDETACHED, detachedFrame -> frameArg.equals(detachedFrame));
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
    return new WaitableRace<T>(asList(new WaitablePageClose(), new WaitablePageCrash()));
  }

  private class WaitablePageClose<T> extends WaitableEvent<EventType, T> {
    WaitablePageClose() {
      super(PageImpl.this.listeners, EventType.CLOSE);
    }

    @Override
    public T get() {
      throw new TargetClosedError(effectiveCloseReason());
    }
  }

  private class WaitablePageCrash<T> extends WaitableEvent<EventType, T> {
    WaitablePageCrash() {
      super(PageImpl.this.listeners, EventType.CRASH);
    }

    @Override
    public T get() {
      throw new TargetClosedError("Page crashed");
    }
  }

  @Override
  public Request waitForRequest(String urlGlob, WaitForRequestOptions options, Runnable code) {
    return waitForRequest(UrlMatcher.forGlob(browserContext.baseUrl(), urlGlob, this.connection.localUtils, false), null, options, code);
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
    return withWaitLogging("Page.waitForRequest", logger -> {
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
    return waitForEventWithTimeout(EventType.REQUEST, code, predicate, options.timeout);
  }

  @Override
  public Request waitForRequestFinished(WaitForRequestFinishedOptions options, Runnable code) {
    return withWaitLogging("Page.waitForRequestFinished", logger -> waitForRequestFinishedImpl(options, code));
  }

  private Request waitForRequestFinishedImpl(WaitForRequestFinishedOptions options, Runnable code) {
    if (options == null) {
      options = new WaitForRequestFinishedOptions();
    }
    return waitForEventWithTimeout(EventType.REQUESTFINISHED, code, options.predicate, options.timeout);
  }

  @Override
  public Response waitForResponse(String urlGlob, WaitForResponseOptions options, Runnable code) {
    return waitForResponse(UrlMatcher.forGlob(browserContext.baseUrl(), urlGlob, this.connection.localUtils, false), null, options, code);
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
    return withWaitLogging("Page.waitForResponse", logger -> {
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
    return waitForEventWithTimeout(EventType.RESPONSE, code, predicate, options.timeout);
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
    waitForURL(UrlMatcher.forGlob(browserContext.baseUrl(), url, this.connection.localUtils, false), options);
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
