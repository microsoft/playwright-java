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
  private final Set<FrameImpl> frames = new LinkedHashSet<>();
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
  private final TimeoutSettings timeoutSettings;
  private VideoImpl video;
  private final PageImpl opener;

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
    listeners.notify(EventType.CLOSE, this);
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
    try {
      if (ownedContext != null) {
        ownedContext.close();
      } else {
        JsonObject params = gson().toJsonTree(options).getAsJsonObject();
        sendMessage("close", params);
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
    return withLogging("Page.querySelector", () -> mainFrame.querySelectorImpl(
      selector, convertType(options, Frame.QuerySelectorOptions.class)));
  }

  @Override
  public List<ElementHandle> querySelectorAll(String selector) {
    return withLogging("Page.querySelectorAll", () -> mainFrame.querySelectorAllImpl(selector));
  }

  @Override
  public Object evalOnSelector(String selector, String pageFunction, Object arg, EvalOnSelectorOptions options) {
    return withLogging("Page.evalOnSelector", () -> mainFrame.evalOnSelectorImpl(
      selector, pageFunction, arg, convertType(options, Frame.EvalOnSelectorOptions.class)));
  }

  @Override
  public Object evalOnSelectorAll(String selector, String pageFunction, Object arg) {
    return withLogging("Page.evalOnSelectorAll", () -> mainFrame.evalOnSelectorAllImpl(selector, pageFunction, arg));
  }

  @Override
  public void addInitScript(String script) {
    withLogging("Page.addInitScript", () -> addInitScriptImpl(script));
  }

  @Override
  public void addInitScript(Path path) {
    withLogging("Page.addInitScript", () -> {
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
  public ElementHandle addScriptTag(AddScriptTagOptions options) {
    return withLogging("Page.addScriptTag",
      () -> mainFrame.addScriptTagImpl(convertType(options, Frame.AddScriptTagOptions.class)));
  }

  @Override
  public ElementHandle addStyleTag(AddStyleTagOptions options) {
    return withLogging("Page.addStyleTag",
      () -> mainFrame.addStyleTagImpl(convertType(options, Frame.AddStyleTagOptions.class)));
  }

  @Override
  public void bringToFront() {
    withLogging("Page.bringToFront", () -> sendMessage("bringToFront"));
  }

  @Override
  public void check(String selector, CheckOptions options) {
    withLogging("Page.check",
      () -> mainFrame.checkImpl(selector, convertType(options, Frame.CheckOptions.class)));
  }

  @Override
  public void click(String selector, ClickOptions options) {
    withLogging("Page.click",
      () -> mainFrame.clickImpl(selector, convertType(options, Frame.ClickOptions.class)));
  }

  @Override
  public String content() {
    return withLogging("Page.content", () -> mainFrame.contentImpl());
  }

  @Override
  public BrowserContextImpl context() {
    return browserContext;
  }

  @Override
  public void dblclick(String selector, DblclickOptions options) {
    withLogging("Page.dblclick",
      () -> mainFrame.dblclickImpl(selector, convertType(options, Frame.DblclickOptions.class)));
  }

  @Override
  public void dispatchEvent(String selector, String type, Object eventInit, DispatchEventOptions options) {
    withLogging("Page.dispatchEvent",
      () -> mainFrame.dispatchEventImpl(selector, type, eventInit, convertType(options, Frame.DispatchEventOptions.class)));
  }

  @Override
  public void emulateMedia(EmulateMediaOptions options) {
    withLogging("Page.emulateMedia", () -> emulateMediaImpl(options));
  }

  private void emulateMediaImpl(EmulateMediaOptions options) {
    if (options == null) {
      options = new EmulateMediaOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    sendMessage("emulateMedia", params);
  }

  @Override
  public Object evaluate(String expression, Object arg) {
    return withLogging("Page.evaluate", () -> mainFrame.evaluateImpl(expression, arg));
  }

  @Override
  public JSHandle evaluateHandle(String pageFunction, Object arg) {
    return withLogging("Page.evaluateHandle", () -> mainFrame.evaluateHandleImpl(pageFunction, arg));
  }

  @Override
  public void exposeBinding(String name, BindingCallback playwrightBinding, ExposeBindingOptions options) {
    withLogging("Page.exposeBinding", () -> exposeBindingImpl(name, playwrightBinding, options));
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
    sendMessage("exposeBinding", params);
  }

  @Override
  public void exposeFunction(String name, FunctionCallback playwrightFunction) {
    withLogging("Page.exposeFunction",
      () -> exposeBindingImpl(name, (BindingCallback.Source source, Object... args) -> playwrightFunction.call(args), null));
  }

  @Override
  public void fill(String selector, String value, FillOptions options) {
    withLogging("Page.fill",
      () -> mainFrame.fillImpl(selector, value, convertType(options, Frame.FillOptions.class)));
  }

  @Override
  public void focus(String selector, FocusOptions options) {
    withLogging("Page.focus",
      () -> mainFrame.focusImpl(selector, convertType(options, Frame.FocusOptions.class)));
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
    return frameFor(new UrlMatcher(browserContext.baseUrl, glob));
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
    return withLogging("Page.getAttribute",
      () -> mainFrame.getAttributeImpl(selector, name, convertType(options, Frame.GetAttributeOptions.class)));
  }

  @Override
  public Locator getByAltText(String text, GetByAltTextOptions options) {
    return withLogging("Page.getAttribute",
      () -> mainFrame.getByAltText(text, convertType(options, Frame.GetByAltTextOptions.class)));
  }

  @Override
  public Locator getByAltText(Pattern text, GetByAltTextOptions options) {
    return withLogging("Page.getByAltText",
      () -> mainFrame.getByAltText(text, convertType(options, Frame.GetByAltTextOptions.class)));
  }

  @Override
  public Locator getByLabel(String text, GetByLabelOptions options) {
    return withLogging("Page.getByLabel",
      () -> mainFrame.getByLabel(text, convertType(options, Frame.GetByLabelOptions.class)));
  }

  @Override
  public Locator getByLabel(Pattern text, GetByLabelOptions options) {
    return withLogging("Page.getByLabel",
      () -> mainFrame.getByLabel(text, convertType(options, Frame.GetByLabelOptions.class)));
  }

  @Override
  public Locator getByPlaceholder(String text, GetByPlaceholderOptions options) {
    return withLogging("Page.getByPlaceholder",
      () -> mainFrame.getByPlaceholder(text, convertType(options, Frame.GetByPlaceholderOptions.class)));
  }

  @Override
  public Locator getByPlaceholder(Pattern text, GetByPlaceholderOptions options) {
    return withLogging("Page.getByPlaceholder",
      () -> mainFrame.getByPlaceholder(text, convertType(options, Frame.GetByPlaceholderOptions.class)));
  }

  @Override
  public Locator getByRole(AriaRole role, GetByRoleOptions options) {
    return withLogging("Page.getByRole",
      () -> mainFrame.getByRole(role, convertType(options, Frame.GetByRoleOptions.class)));
  }

  @Override
  public Locator getByTestId(String testId) {
    return withLogging("Page.getByTestId", () -> mainFrame.getByTestId(testId));
  }

  @Override
  public Locator getByTestId(Pattern testId) {
    return withLogging("Page.getByTestId", () -> mainFrame.getByTestId(testId));
  }

  @Override
  public Locator getByText(String text, GetByTextOptions options) {
    return withLogging("Page.getByText",
      () -> mainFrame.getByText(text, convertType(options, Frame.GetByTextOptions.class)));
  }

  @Override
  public Locator getByText(Pattern text, GetByTextOptions options) {
    return withLogging("Page.getByText",
      () -> mainFrame.getByText(text, convertType(options, Frame.GetByTextOptions.class)));
  }

  @Override
  public Locator getByTitle(String text, GetByTitleOptions options) {
    return withLogging("Page.getByTitle",
      () -> mainFrame.getByTitle(text, convertType(options, Frame.GetByTitleOptions.class)));
  }

  @Override
  public Locator getByTitle(Pattern text, GetByTitleOptions options) {
    return withLogging("Page.getByTitle",
      () -> mainFrame.getByTitle(text, convertType(options, Frame.GetByTitleOptions.class)));
  }

  @Override
  public Response goBack(GoBackOptions options) {
    return withLogging("Page.goBack", () -> goBackImpl(options));
  }

  Response goBackImpl(GoBackOptions options) {
    if (options == null) {
      options = new GoBackOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    JsonObject json = sendMessage("goBack", params).getAsJsonObject();
    if (json.has("response")) {
      return connection.getExistingObject(json.getAsJsonObject("response").get("guid").getAsString());
    }
    return null;
  }

  @Override
  public Response goForward(GoForwardOptions options) {
    return withLogging("Page.goForward", () -> goForwardImpl(options));
  }

  Response goForwardImpl(GoForwardOptions options) {
    if (options == null) {
      options = new GoForwardOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    JsonObject json = sendMessage("goForward", params).getAsJsonObject();
    if (json.has("response")) {
      return connection.getExistingObject(json.getAsJsonObject("response").get("guid").getAsString());
    }
    return null;
  }

  @Override
  public ResponseImpl navigate(String url, NavigateOptions options) {
    return withLogging("Page.navigate", () -> mainFrame.navigateImpl(url, convertType(options, Frame.NavigateOptions.class)));
  }

  @Override
  public void hover(String selector, HoverOptions options) {
    withLogging("Page.hover", () -> mainFrame.hoverImpl(selector, convertType(options, Frame.HoverOptions.class)));
  }

  @Override
  public void dragAndDrop(String source, String target, DragAndDropOptions options) {
    withLogging("Page.dragAndDrop", () -> mainFrame.dragAndDropImpl(source, target, convertType(options, Frame.DragAndDropOptions.class)));
  }

  @Override
  public String innerHTML(String selector, InnerHTMLOptions options) {
    return withLogging("Page.innerHTML",
      () -> mainFrame.innerHTMLImpl(selector, convertType(options, Frame.InnerHTMLOptions.class)));
  }

  @Override
  public String innerText(String selector, InnerTextOptions options) {
    return withLogging("Page.innerText",
      () -> mainFrame.innerTextImpl(selector, convertType(options, Frame.InnerTextOptions.class)));
  }

  @Override
  public String inputValue(String selector, InputValueOptions options) {
    return withLogging("Page.inputValue",
      () -> mainFrame.inputValueImpl(selector, convertType(options, Frame.InputValueOptions.class)));
  }

  @Override
  public boolean isChecked(String selector, IsCheckedOptions options) {
    return withLogging("Page.isChecked",
      () -> mainFrame.isCheckedImpl(selector, convertType(options, Frame.IsCheckedOptions.class)));
  }

  @Override
  public boolean isClosed() {
    return isClosed;
  }

  @Override
  public boolean isDisabled(String selector, IsDisabledOptions options) {
    return withLogging("Page.isDisabled",
      () -> mainFrame.isDisabledImpl(selector, convertType(options, Frame.IsDisabledOptions.class)));
  }

  @Override
  public boolean isEditable(String selector, IsEditableOptions options) {
    return withLogging("Page.isEditable",
      () -> mainFrame.isEditableImpl(selector, convertType(options, Frame.IsEditableOptions.class)));
  }

  @Override
  public boolean isEnabled(String selector, IsEnabledOptions options) {
    return withLogging("Page.isEnabled",
      () -> mainFrame.isEnabledImpl(selector, convertType(options, Frame.IsEnabledOptions.class)));
  }

  @Override
  public boolean isHidden(String selector, IsHiddenOptions options) {
    return withLogging("Page.isHidden",
      () -> mainFrame.isHiddenImpl(selector, convertType(options, Frame.IsHiddenOptions.class)));
  }

  @Override
  public boolean isVisible(String selector, IsVisibleOptions options) {
    return withLogging("Page.isVisible",
      () -> mainFrame.isVisibleImpl(selector, convertType(options, Frame.IsVisibleOptions.class)));
  }

  @Override
  public Keyboard keyboard() {
    return keyboard;
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
    withLogging("Page.pause", () -> {
      Double defaultNavigationTimeout = browserContext.timeoutSettings.defaultNavigationTimeout();
      Double defaultTimeout = browserContext.timeoutSettings.defaultTimeout();
      browserContext.setDefaultNavigationTimeoutImpl(0.0);
      browserContext.setDefaultTimeoutImpl(0.0);
      try {
        runUntil(() -> {}, new WaitableRace<>(asList(context().pause(), (Waitable<JsonElement>) waitableClosedOrCrashed)));
      } finally {
        browserContext.setDefaultNavigationTimeoutImpl(defaultNavigationTimeout);
        browserContext.setDefaultTimeoutImpl(defaultTimeout);
      }
    });
  }

  @Override
  public byte[] pdf(PdfOptions options) {
    return withLogging("Page.pdf", () -> pdfImpl(options));
  }

  private byte[] pdfImpl(PdfOptions options) {
    if (options == null) {
      options = new PdfOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.remove("path");
    JsonObject json = sendMessage("pdf", params).getAsJsonObject();
    byte[] buffer = Base64.getDecoder().decode(json.get("pdf").getAsString());
    if (options.path != null) {
      Utils.writeToFile(buffer, options.path);
    }
    return buffer;
  }

  @Override
  public void press(String selector, String key, PressOptions options) {
    withLogging("Page.press",
      () -> mainFrame.pressImpl(selector, key, convertType(options, Frame.PressOptions.class)));
  }

  @Override
  public Response reload(ReloadOptions options) {
    return withLogging("Page.reload", () -> reloadImpl(options));
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
    JsonObject json = sendMessage("reload", params).getAsJsonObject();
    if (json.has("response")) {
      return connection.getExistingObject(json.getAsJsonObject("response").get("guid").getAsString());
    }
    return null;
  }

  @Override
  public void route(String url, Consumer<Route> handler, RouteOptions options) {
    route(new UrlMatcher(browserContext.baseUrl, url), handler, options);
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
      browserContext.recordIntoHar(this, har, convertType(options, BrowserContext.RouteFromHAROptions.class));
      return;
    }
    UrlMatcher matcher = UrlMatcher.forOneOf(browserContext.baseUrl, options.url);
    HARRouter harRouter = new HARRouter(connection.localUtils, har, options.notFound);
    onClose(context -> harRouter.dispose());
    route(matcher, route -> harRouter.handle(route), null);
  }

  private void route(UrlMatcher matcher, Consumer<Route> handler, RouteOptions options) {
    withLogging("Page.route", () -> {
      routes.add(matcher, handler, options == null ? null : options.times);
      updateInterceptionPatterns();
    });
  }

  @Override
  public byte[] screenshot(ScreenshotOptions options) {
    return withLogging("Page.screenshot", () -> screenshotImpl(options));
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
    return withLogging("Page.selectOption",
      () -> mainFrame.selectOptionImpl(selector, values, convertType(options, Frame.SelectOptionOptions.class)));
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
    List<Locator> mask = options.mask;
    options.mask = null;
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    options.mask = mask;
    params.remove("path");
    if (mask != null) {
      JsonArray maskArray = new JsonArray();
      for (Locator locator: mask) {
        maskArray.add(((LocatorImpl) locator).toProtocol());
      }
      params.add("mask", maskArray);
    }
    JsonObject json = sendMessage("screenshot", params).getAsJsonObject();

    byte[] buffer = Base64.getDecoder().decode(json.get("binary").getAsString());
    if (options.path != null) {
      Utils.writeToFile(buffer, options.path);
    }
    return buffer;
  }

  @Override
  public List<String> selectOption(String selector, SelectOption[] values, SelectOptionOptions options) {
    return withLogging("Page.selectOption",
      () -> mainFrame.selectOptionImpl(selector, values, convertType(options, Frame.SelectOptionOptions.class)));
  }

  @Override
  public List<String> selectOption(String selector, ElementHandle[] values, SelectOptionOptions options) {
    return withLogging("Page.selectOption",
      () -> mainFrame.selectOptionImpl(selector, values, convertType(options, Frame.SelectOptionOptions.class)));
  }

  @Override
  public void setChecked(String selector, boolean checked, SetCheckedOptions options) {
    withLogging("Page.setChecked",
      () -> mainFrame.setCheckedImpl(selector, checked, convertType(options, Frame.SetCheckedOptions.class)));
  }

  @Override
  public void setContent(String html, SetContentOptions options) {
    withLogging("Page.setContent",
      () -> mainFrame.setContentImpl(html, convertType(options, Frame.SetContentOptions.class)));
  }

  @Override
  public void setDefaultNavigationTimeout(double timeout) {
    withLogging("Page.setDefaultNavigationTimeout", () -> {
      timeoutSettings.setDefaultNavigationTimeout(timeout);
      JsonObject params = new JsonObject();
      params.addProperty("timeout", timeout);
      sendMessage("setDefaultNavigationTimeoutNoReply", params);
    });
  }

  @Override
  public void setDefaultTimeout(double timeout) {
    withLogging("Page.setDefaultTimeout", () -> {
      timeoutSettings.setDefaultTimeout(timeout);
      JsonObject params = new JsonObject();
      params.addProperty("timeout", timeout);
      sendMessage("setDefaultTimeoutNoReply", params);
    });
  }

  @Override
  public void setExtraHTTPHeaders(Map<String, String> headers) {
    withLogging("Page.setExtraHTTPHeaders", () -> {
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
  public void setInputFiles(String selector, Path files, SetInputFilesOptions options) {
    setInputFiles(selector, new Path[]{files}, options);
  }

  @Override
  public void setInputFiles(String selector, Path[] files, SetInputFilesOptions options) {
    withLogging("Page.setInputFiles",
      () -> mainFrame.setInputFilesImpl(selector, files, convertType(options, Frame.SetInputFilesOptions.class)));
  }

  @Override
  public void setInputFiles(String selector, FilePayload files, SetInputFilesOptions options) {
    setInputFiles(selector, new FilePayload[]{files}, options);
  }

  @Override
  public void setInputFiles(String selector, FilePayload[] files, SetInputFilesOptions options) {
    withLogging("Page.setInputFiles",
      () -> mainFrame.setInputFilesImpl(selector, files, convertType(options, Frame.SetInputFilesOptions.class)));
  }

  @Override
  public void setViewportSize(int width, int height) {
    withLogging("Page.setViewportSize", () -> {
      viewport = new ViewportSize(width, height);
      JsonObject params = new JsonObject();
      params.add("viewportSize", gson().toJsonTree(viewport));
      sendMessage("setViewportSize", params);
    });
  }

  @Override
  public void tap(String selector, TapOptions options) {
    withLogging("Page.tap",
      () -> mainFrame.tapImpl(selector, convertType(options, Frame.TapOptions.class)));
  }

  @Override
  public String textContent(String selector, TextContentOptions options) {
    return withLogging("Page.textContent",
      () -> mainFrame.textContentImpl(selector, convertType(options, Frame.TextContentOptions.class)));
  }

  @Override
  public String title() {
    return withLogging("Page.title", () -> mainFrame.titleImpl());
  }

  @Override
  public Touchscreen touchscreen() {
    return touchscreen;
  }

  @Override
  public void type(String selector, String text, TypeOptions options) {
    withLogging("Page.type",
      () -> mainFrame.typeImpl(selector, text, convertType(options, Frame.TypeOptions.class)));
  }

  @Override
  public void uncheck(String selector, UncheckOptions options) {
    withLogging("Page.uncheck",
      () -> mainFrame.uncheckImpl(selector, convertType(options, Frame.UncheckOptions.class)));
  }

  @Override
  public void unroute(String url, Consumer<Route> handler) {
    unroute(new UrlMatcher(browserContext.baseUrl, url), handler);
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
    withLogging("Page.unroute", () -> {
      routes.remove(matcher, handler);
      updateInterceptionPatterns();
    });
  }

  private void updateInterceptionPatterns() {
    sendMessage("setNetworkInterceptionPatterns", routes.interceptionPatterns());
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
    if (browserContext.videosDir == null) {
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
    return withLogging("Page.waitForFunction",
      () -> mainFrame.waitForFunctionImpl(pageFunction, arg, convertType(options, Frame.WaitForFunctionOptions.class)));
  }

  @Override
  public void waitForLoadState(LoadState state, WaitForLoadStateOptions options) {
    withWaitLogging("Page.waitForLoadState", logger -> {
      mainFrame.waitForLoadStateImpl(state, convertType(options, Frame.WaitForLoadStateOptions.class), logger);
      return null;
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
      throw new PlaywrightException("Page closed");
    }
  }

  private class WaitablePageCrash<T> extends WaitableEvent<EventType, T> {
    WaitablePageCrash() {
      super(PageImpl.this.listeners, EventType.CRASH);
    }

    @Override
    public T get() {
      throw new PlaywrightException("Page crashed");
    }
  }

  @Override
  public Request waitForRequest(String urlGlob, WaitForRequestOptions options, Runnable code) {
    return waitForRequest(new UrlMatcher(browserContext.baseUrl, urlGlob), null, options, code);
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
    return waitForResponse(new UrlMatcher(browserContext.baseUrl, urlGlob), null, options, code);
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
    return withLogging("Page.waitForSelector",
      () -> mainFrame.waitForSelectorImpl(selector, convertType(options, Frame.WaitForSelectorOptions.class)));
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
    withLogging("Page.waitForTimeout", () -> mainFrame.waitForTimeoutImpl(timeout));
  }

  @Override
  public void waitForURL(String url, WaitForURLOptions options) {
    waitForURL(new UrlMatcher(browserContext.baseUrl, url), options);
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
