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
import com.google.gson.JsonObject;
import com.microsoft.playwright.*;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.Utils.convertViaJson;
import static com.microsoft.playwright.impl.Utils.isSafeCloseError;
import static java.util.Arrays.asList;


public class PageImpl extends ChannelOwner implements Page {
  private final BrowserContextImpl browserContext;
  private final FrameImpl mainFrame;
  private final KeyboardImpl keyboard;
  private final MouseImpl mouse;
  private final AccessibilityImpl accessibility;
  private final TouchscreenImpl touchscreen;
  private Viewport viewport;
  private final Router routes = new Router();
  private final Set<FrameImpl> frames = new LinkedHashSet<>();
  private final ListenerCollection<EventType> listeners = new ListenerCollection<EventType>() {
    @Override
    void add(EventType eventType, Consumer<?> listener) {
      if (eventType == EventType.FILECHOOSER) {
        willAddFileChooserListener();
      }
      super.add(eventType, listener);
    }

    @Override
    void remove(EventType eventType, Consumer<?> listener) {
      super.remove(eventType, listener);
      if (eventType == EventType.FILECHOOSER) {
        didRemoveFileChooserListener();
      }
    }
  };
  final Map<String, Binding> bindings = new HashMap<>();
  BrowserContextImpl ownedContext;
  private boolean isClosed;
  final Set<Worker> workers = new HashSet<>();
  private final TimeoutSettings timeoutSettings;
  private VideoImpl video;

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
      viewport = gson().fromJson(initializer.get("viewportSize"), Viewport.class);
    }
    keyboard = new KeyboardImpl(this);
    mouse = new MouseImpl(this);
    touchscreen = new TouchscreenImpl(this);
    accessibility = new AccessibilityImpl(this);
    frames.add(mainFrame);
    timeoutSettings = new TimeoutSettings(browserContext.timeoutSettings);
  }

  @Override
  protected void handleEvent(String event, JsonObject params) {
    if ("dialog".equals(event)) {
      String guid = params.getAsJsonObject("dialog").get("guid").getAsString();
      DialogImpl dialog = connection.getExistingObject(guid);
      if (listeners.hasListeners(EventType.DIALOG)) {
        listeners.notify(EventType.DIALOG, dialog);
      } else {
        dialog.dismiss();
      }
    } else if ("popup".equals(event)) {
      String guid = params.getAsJsonObject("page").get("guid").getAsString();
      PageImpl popup = connection.getExistingObject(guid);
      listeners.notify(EventType.POPUP, popup);
    } else if ("worker".equals(event)) {
      String guid = params.getAsJsonObject("worker").get("guid").getAsString();
      WorkerImpl worker = connection.getExistingObject(guid);
      worker.page = this;
      workers.add(worker);
      listeners.notify(EventType.WORKER, worker);
    } else if ("webSocket".equals(event)) {
      String guid = params.getAsJsonObject("webSocket").get("guid").getAsString();
      WebSocketImpl webSocket = connection.getExistingObject(guid);
      listeners.notify(EventType.WEBSOCKET, webSocket);
    } else if ("console".equals(event)) {
      String guid = params.getAsJsonObject("message").get("guid").getAsString();
      ConsoleMessageImpl message = connection.getExistingObject(guid);
      listeners.notify(EventType.CONSOLE, message);
    } else if ("download".equals(event)) {
      String guid = params.getAsJsonObject("download").get("guid").getAsString();
      DownloadImpl download = connection.getExistingObject(guid);
      listeners.notify(EventType.DOWNLOAD, download);
    } else if ("fileChooser".equals(event)) {
      String guid = params.getAsJsonObject("element").get("guid").getAsString();
      ElementHandleImpl elementHandle = connection.getExistingObject(guid);
      FileChooser fileChooser = new FileChooserImpl(this, elementHandle, params.get("isMultiple").getAsBoolean());
      listeners.notify(EventType.FILECHOOSER, fileChooser);
    } else if ("bindingCall".equals(event)) {
      String guid = params.getAsJsonObject("binding").get("guid").getAsString();
      BindingCall bindingCall = connection.getExistingObject(guid);
      Binding binding = bindings.get(bindingCall.name());
      if (binding == null) {
        binding = browserContext.bindings.get(bindingCall.name());
      }
      if (binding != null) {
        try {
          bindingCall.call(binding);
        } catch (RuntimeException e) {
          e.printStackTrace();
        }
      }
    } else if ("load".equals(event)) {
      listeners.notify(EventType.LOAD, this);
    } else if ("domcontentloaded".equals(event)) {
      listeners.notify(EventType.DOMCONTENTLOADED, this);
    } else if ("request".equals(event)) {
      String guid = params.getAsJsonObject("request").get("guid").getAsString();
      Request request = connection.getExistingObject(guid);
      listeners.notify(EventType.REQUEST, request);
    } else if ("requestFailed".equals(event)) {
      String guid = params.getAsJsonObject("request").get("guid").getAsString();
      RequestImpl request = connection.getExistingObject(guid);
      if (params.has("failureText")) {
        request.failure = params.get("failureText").getAsString();
      }
      listeners.notify(EventType.REQUESTFAILED, request);
    } else if ("requestFinished".equals(event)) {
      String guid = params.getAsJsonObject("request").get("guid").getAsString();
      Request request = connection.getExistingObject(guid);
      listeners.notify(EventType.REQUESTFINISHED, request);
    } else if ("response".equals(event)) {
      String guid = params.getAsJsonObject("response").get("guid").getAsString();
      Response response = connection.getExistingObject(guid);
      listeners.notify(EventType.RESPONSE, response);
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
      Route route = connection.getExistingObject(params.getAsJsonObject("route").get("guid").getAsString());
      boolean handled = routes.handle(route);
      if (!handled) {
        handled = browserContext.routes.handle(route);
      }
      if (!handled) {
        route.resume();
      }
    } else if ("video".equals(event)) {
      video().setRelativePath(params.get("relativePath").getAsString());
    } else if ("pageError".equals(event)) {
      SerializedError error = gson().fromJson(params.getAsJsonObject("error"), SerializedError.class);
      listeners.notify(EventType.PAGEERROR, new ErrorImpl(error));
    } else if ("crash".equals(event)) {
      listeners.notify(EventType.CRASH, this);
    } else if ("close".equals(event)) {
      isClosed = true;
      browserContext.pages.remove(this);
      listeners.notify(EventType.CLOSE, this);
    }
  }

  private void willAddFileChooserListener() {
    if (!listeners.hasListeners(EventType.FILECHOOSER)) {
      updateFileChooserInterception(true);
    }
  }

  private void didRemoveFileChooserListener() {
    if (!listeners.hasListeners(EventType.FILECHOOSER)) {
      updateFileChooserInterception(false);
    }
  }

  private void updateFileChooserInterception(boolean enabled) {
    JsonObject params = new JsonObject();
    params.addProperty("intercepted", enabled);
    sendMessage("setFileChooserInterceptedNoReply", params);
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
  public void onConsole(Consumer<ConsoleMessage> handler) {
    listeners.add(EventType.CONSOLE, handler);
  }

  @Override
  public void offConsole(Consumer<ConsoleMessage> handler) {
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
  public void onPageError(Consumer<Error> handler) {
    listeners.add(EventType.PAGEERROR, handler);
  }

  @Override
  public void offPageError(Consumer<Error> handler) {
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
  public Page waitForClose(Runnable code, WaitForCloseOptions options) {
    if (options == null) {
      options = new WaitForCloseOptions();
    }
    return waitForEventWithTimeout(EventType.CLOSE, code, options.timeout);
  }

  @Override
  public ConsoleMessage waitForConsole(Runnable code, WaitForConsoleOptions options) {
    if (options == null) {
      options = new WaitForConsoleOptions();
    }
    return waitForEventWithTimeout(EventType.CONSOLE, code, options.timeout);
  }

  private <T> T waitForEventWithTimeout(EventType eventType, Runnable code, Double timeout) {
    List<Waitable<T>> waitables = new ArrayList<>();
    waitables.add(new WaitableEvent<>(listeners, eventType));
    waitables.add(createWaitForCloseHelper());
    waitables.add(createWaitableTimeout(timeout));
    return runUntil(code, new WaitableRace<>(waitables));
  }

  @Override
  public Download waitForDownload(Runnable code, WaitForDownloadOptions options) {
    if (options == null) {
      options = new WaitForDownloadOptions();
    }
    return waitForEventWithTimeout(EventType.DOWNLOAD, code, options.timeout);
  }

  @Override
  public FileChooser waitForFileChooser(Runnable code, WaitForFileChooserOptions options) {
    // TODO: enable/disable file chooser interception
    if (options == null) {
      options = new WaitForFileChooserOptions();
    }
    return waitForEventWithTimeout(EventType.FILECHOOSER, code, options.timeout);
  }

  @Override
  public Frame waitForFrameAttached(Runnable code, WaitForFrameAttachedOptions options) {
    if (options == null) {
      options = new WaitForFrameAttachedOptions();
    }
    return waitForEventWithTimeout(EventType.FRAMEATTACHED, code, options.timeout);
  }

  @Override
  public Frame waitForFrameDetached(Runnable code, WaitForFrameDetachedOptions options) {
    if (options == null) {
      options = new WaitForFrameDetachedOptions();
    }
    return waitForEventWithTimeout(EventType.FRAMEDETACHED, code, options.timeout);
  }

  @Override
  public Frame waitForFrameNavigated(Runnable code, WaitForFrameNavigatedOptions options) {
    if (options == null) {
      options = new WaitForFrameNavigatedOptions();
    }
    return waitForEventWithTimeout(EventType.FRAMENAVIGATED, code, options.timeout);
  }

  @Override
  public Error waitForPageError(Runnable code, WaitForPageErrorOptions options) {
    if (options == null) {
      options = new WaitForPageErrorOptions();
    }
    return waitForEventWithTimeout(EventType.PAGEERROR, code, options.timeout);
  }

  @Override
  public Page waitForPopup(Runnable code, WaitForPopupOptions options) {
    if (options == null) {
      options = new WaitForPopupOptions();
    }
    return waitForEventWithTimeout(EventType.POPUP, code, options.timeout);
  }

  @Override
  public Request waitForRequestFailed(Runnable code, WaitForRequestFailedOptions options) {
    if (options == null) {
      options = new WaitForRequestFailedOptions();
    }
    return waitForEventWithTimeout(EventType.REQUESTFAILED, code, options.timeout);
  }

  @Override
  public Request waitForRequestFinished(Runnable code, WaitForRequestFinishedOptions options) {
    if (options == null) {
      options = new WaitForRequestFinishedOptions();
    }
    return waitForEventWithTimeout(EventType.REQUESTFINISHED, code, options.timeout);
  }

  @Override
  public WebSocket waitForWebSocket(Runnable code, WaitForWebSocketOptions options) {
    if (options == null) {
      options = new WaitForWebSocketOptions();
    }
    return waitForEventWithTimeout(EventType.WEBSOCKET, code, options.timeout);
  }

  @Override
  public Worker waitForWorker(Runnable code, WaitForWorkerOptions options) {
    if (options == null) {
      options = new WaitForWorkerOptions();
    }
    return waitForEventWithTimeout(EventType.WORKER, code, options.timeout);
  }

  @Override
  public void close(CloseOptions options) {
    if (isClosed) {
      return;
    }
    JsonObject params = options == null ? new JsonObject() : gson().toJsonTree(options).getAsJsonObject();
    try {
      sendMessage("close", params);
    } catch (PlaywrightException exception) {
      if (!isSafeCloseError(exception)) {
        throw exception;
      }
    }
    if (ownedContext != null) {
      ownedContext.close();
    }
  }

  @Override
  public ElementHandle querySelector(String selector) {
    return withLogging("Page.querySelector", () -> mainFrame.querySelectorImpl(selector));
  }

  @Override
  public List<ElementHandle> querySelectorAll(String selector) {
    return withLogging("Page.querySelectorAll", () -> mainFrame.querySelectorAllImpl(selector));
  }

  @Override
  public Object evalOnSelector(String selector, String pageFunction, Object arg) {
    return withLogging("Page.evalOnSelector", () -> mainFrame.evalOnSelectorImpl(selector, pageFunction, arg));
  }

  @Override
  public Object evalOnSelectorAll(String selector, String pageFunction, Object arg) {
    return withLogging("Page.evalOnSelectorAll", () -> mainFrame.evalOnSelectorAllImpl(selector, pageFunction, arg));
  }

  @Override
  public Accessibility accessibility() {
    return accessibility;
  }

  @Override
  public void addInitScript(String script) {
    withLogging("Page.addInitScript", () -> {
      JsonObject params = new JsonObject();
      params.addProperty("source", script);
      sendMessage("addInitScript", params);
    });
  }

  @Override
  public ElementHandle addScriptTag(AddScriptTagOptions options) {
    return withLogging("Page.addScriptTag",
      () -> mainFrame.addScriptTagImpl(convertViaJson(options, Frame.AddScriptTagOptions.class)));
  }

  @Override
  public ElementHandle addStyleTag(AddStyleTagOptions options) {
    return withLogging("Page.addStyleTag",
      () -> mainFrame.addStyleTagImpl(convertViaJson(options, Frame.AddStyleTagOptions.class)));
  }

  @Override
  public void bringToFront() {
    withLogging("Page.bringToFront", () -> sendMessage("bringToFront"));
  }

  @Override
  public void check(String selector, CheckOptions options) {
    withLogging("Page.check",
      () -> mainFrame.checkImpl(selector, convertViaJson(options, Frame.CheckOptions.class)));
  }

  @Override
  public void click(String selector, ClickOptions options) {
    withLogging("Page.click",
      () -> mainFrame.clickImpl(selector, convertViaJson(options, Frame.ClickOptions.class)));
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
      () -> mainFrame.dblclickImpl(selector, convertViaJson(options, Frame.DblclickOptions.class)));
  }

  @Override
  public void dispatchEvent(String selector, String type, Object eventInit, DispatchEventOptions options) {
    withLogging("Page.dispatchEvent",
      () -> mainFrame.dispatchEventImpl(selector, type, eventInit, convertViaJson(options, Frame.DispatchEventOptions.class)));
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
  public void exposeBinding(String name, Binding playwrightBinding, ExposeBindingOptions options) {
    withLogging("Page.exposeBinding", () -> exposeBindingImpl(name, playwrightBinding, options));
  }

  private void exposeBindingImpl(String name, Binding playwrightBinding, ExposeBindingOptions options) {
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
  public void exposeFunction(String name, Function playwrightFunction) {
    withLogging("Page.exposeFunction",
      () -> exposeBindingImpl(name, (Binding.Source source, Object... args) -> playwrightFunction.call(args), null));
  }

  @Override
  public void fill(String selector, String value, FillOptions options) {
    withLogging("Page.fill",
      () -> mainFrame.fillImpl(selector, value, convertViaJson(options, Frame.FillOptions.class)));
  }

  @Override
  public void focus(String selector, FocusOptions options) {
    withLogging("Page.focus",
      () -> mainFrame.focusImpl(selector, convertViaJson(options, Frame.FocusOptions.class)));
  }

  @Override
  public Frame frameByName(String name) {
    for (Frame frame : frames) {
      if (name.equals(frame.name())) {
        return frame;
      }
    }
    return null;
  }

  @Override
  public Frame frameByUrl(String glob) {
    return frameFor(new UrlMatcher(glob));
  }

  @Override
  public Frame frameByUrl(Pattern pattern) {
    return frameFor(new UrlMatcher(pattern));
  }

  @Override
  public Frame frameByUrl(Predicate<String> predicate) {
    return frameFor(new UrlMatcher(predicate));
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
      () -> mainFrame.getAttributeImpl(selector, name, convertViaJson(options, Frame.GetAttributeOptions.class)));
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
    return withLogging("Page.navigate", () ->
      mainFrame.navigateImpl(url, convertViaJson(options, Frame.NavigateOptions.class)));
  }

  @Override
  public void hover(String selector, HoverOptions options) {
    withLogging("Page.hover", () ->
      mainFrame.hoverImpl(selector, convertViaJson(options, Frame.HoverOptions.class)));
  }

  @Override
  public String innerHTML(String selector, InnerHTMLOptions options) {
    return withLogging("Page.innerHTML",
      () -> mainFrame.innerHTMLImpl(selector, convertViaJson(options, Frame.InnerHTMLOptions.class)));
  }

  @Override
  public String innerText(String selector, InnerTextOptions options) {
    return withLogging("Page.innerText",
      () -> mainFrame.innerTextImpl(selector, convertViaJson(options, Frame.InnerTextOptions.class)));
  }

  @Override
  public boolean isChecked(String selector, IsCheckedOptions options) {
    return withLogging("Page.isChecked",
      () -> mainFrame.isCheckedImpl(selector, convertViaJson(options, Frame.IsCheckedOptions.class)));
  }

  @Override
  public boolean isClosed() {
    return isClosed;
  }

  @Override
  public boolean isDisabled(String selector, IsDisabledOptions options) {
    return withLogging("Page.isDisabled",
      () -> mainFrame.isDisabledImpl(selector, convertViaJson(options, Frame.IsDisabledOptions.class)));
  }

  @Override
  public boolean isEditable(String selector, IsEditableOptions options) {
    return withLogging("Page.isEditable",
      () -> mainFrame.isEditableImpl(selector, convertViaJson(options, Frame.IsEditableOptions.class)));
  }

  @Override
  public boolean isEnabled(String selector, IsEnabledOptions options) {
    return withLogging("Page.isEnabled",
      () -> mainFrame.isEnabledImpl(selector, convertViaJson(options, Frame.IsEnabledOptions.class)));
  }

  @Override
  public boolean isHidden(String selector, IsHiddenOptions options) {
    return withLogging("Page.isHidden",
      () -> mainFrame.isHiddenImpl(selector, convertViaJson(options, Frame.IsHiddenOptions.class)));
  }

  @Override
  public boolean isVisible(String selector, IsVisibleOptions options) {
    return withLogging("Page.isVisible",
      () -> mainFrame.isVisibleImpl(selector, convertViaJson(options, Frame.IsVisibleOptions.class)));
  }

  @Override
  public Keyboard keyboard() {
    return keyboard;
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
  public Page opener() {
    return withLogging("Page.opener", () -> {
      JsonObject result = sendMessage("opener").getAsJsonObject();
      if (!result.has("page")) {
        return null;
      }
      return connection.getExistingObject(result.getAsJsonObject("page").get("guid").getAsString());
    });
  }

  @Override
  public byte[] pdf(PdfOptions options) {
    return withLogging("Page.pdf", () -> pdfImpl(options));
  }

  private byte[] pdfImpl(PdfOptions options) {
    if (!browserContext.browser().isChromium()) {
      throw new PlaywrightException("Page.pdf only supported in headless Chromium");
    }
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
      () -> mainFrame.pressImpl(selector, key, convertViaJson(options, Frame.PressOptions.class)));
  }

  @Override
  public Response reload(ReloadOptions options) {
    return withLogging("Page.reload", () -> reloadImpl(options));
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
    withLogging("Page.route", () -> {
      routes.add(matcher, handler);
      if (routes.size() == 1) {
        JsonObject params = new JsonObject();
        params.addProperty("enabled", true);
        sendMessage("setNetworkInterceptionEnabled", params);
      }
    });
  }

  @Override
  public byte[] screenshot(ScreenshotOptions options) {
    return withLogging("Page.screenshot", () -> screenshotImpl(options));
  }

  private byte[] screenshotImpl(ScreenshotOptions options) {
    if (options == null) {
      options = new ScreenshotOptions();
    }
    if (options.type == null) {
      options.type = ScreenshotOptions.Type.PNG;
      if (options.path != null) {
        String fileName = options.path.getFileName().toString();
        int extStart = fileName.lastIndexOf('.');
        if (extStart != -1) {
          String extension = fileName.substring(extStart).toLowerCase();
          if (".jpeg".equals(extension) || ".jpg".equals(extension)) {
            options.type = ScreenshotOptions.Type.JPEG;
          }
        }
      }
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.remove("path");
    JsonObject json = sendMessage("screenshot", params).getAsJsonObject();

    byte[] buffer = Base64.getDecoder().decode(json.get("binary").getAsString());
    if (options.path != null) {
      Utils.writeToFile(buffer, options.path);
    }
    return buffer;
  }

  @Override
  public List<String> selectOption(String selector, ElementHandle.SelectOption[] values, SelectOptionOptions options) {
    return withLogging("Page.selectOption",
      () -> mainFrame.selectOptionImpl(selector, values, convertViaJson(options, Frame.SelectOptionOptions.class)));
  }

  @Override
  public List<String> selectOption(String selector, ElementHandle[] values, SelectOptionOptions options) {
    return withLogging("Page.selectOption",
      () -> mainFrame.selectOptionImpl(selector, values, convertViaJson(options, Frame.SelectOptionOptions.class)));
  }

  @Override
  public void setContent(String html, SetContentOptions options) {
    withLogging("Page.setContent",
      () -> mainFrame.setContentImpl(html, convertViaJson(options, Frame.SetContentOptions.class)));
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
  public void setInputFiles(String selector, Path[] files, SetInputFilesOptions options) {
    withLogging("Page.setInputFiles",
      () -> mainFrame.setInputFilesImpl(selector, files, convertViaJson(options, Frame.SetInputFilesOptions.class)));
  }

  @Override
  public void setInputFiles(String selector, FileChooser.FilePayload[] files, SetInputFilesOptions options) {
    withLogging("Page.setInputFiles",
      () -> mainFrame.setInputFilesImpl(selector, files, convertViaJson(options, Frame.SetInputFilesOptions.class)));
  }

  @Override
  public void setViewportSize(int width, int height) {
    withLogging("Page.setViewportSize", () -> {
      viewport = new Viewport(width, height);
      JsonObject params = new JsonObject();
      params.add("viewportSize", gson().toJsonTree(viewport));
      sendMessage("setViewportSize", params);
    });
  }

  @Override
  public void tap(String selector, TapOptions options) {
    withLogging("Page.tap",
      () -> mainFrame.tapImpl(selector, convertViaJson(options, Frame.TapOptions.class)));
  }

  @Override
  public String textContent(String selector, TextContentOptions options) {
    return withLogging("Page.textContent",
      () -> mainFrame.textContentImpl(selector, convertViaJson(options, Frame.TextContentOptions.class)));
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
      () -> mainFrame.typeImpl(selector, text, convertViaJson(options, Frame.TypeOptions.class)));
  }

  @Override
  public void uncheck(String selector, UncheckOptions options) {
    withLogging("Page.uncheck",
      () -> mainFrame.uncheckImpl(selector, convertViaJson(options, Frame.UncheckOptions.class)));
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

  private void unroute(UrlMatcher matcher, Consumer<Route> handler) {
    withLogging("Page.unroute", () -> {
      routes.remove(matcher, handler);
      if (routes.size() == 0) {
        JsonObject params = new JsonObject();
        params.addProperty("enabled", false);
        sendMessage("setNetworkInterceptionEnabled", params);
      }
    });
  }

  @Override
  public String url() {
    return mainFrame.url();
  }

  @Override
  public VideoImpl video() {
    if (video != null) {
      return video;
    }
    if (browserContext.videosDir == null) {
      return null;
    }
    video = new VideoImpl(this);
    // In case of persistent profile, we already have it.
    if (initializer.has("videoRelativePath")) {
      video.setRelativePath(initializer.get("videoRelativePath").getAsString());
    }
    return video;
  }

  @Override
  public Viewport viewportSize() {
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
      () -> mainFrame.waitForFunctionImpl(pageFunction, arg, convertViaJson(options, Frame.WaitForFunctionOptions.class)));
  }

  @Override
  public void waitForLoadState(Frame.LoadState state, WaitForLoadStateOptions options) {
    withLogging("Page.waitForLoadState",
      () -> mainFrame.waitForLoadStateImpl(state, convertViaJson(options, Frame.WaitForLoadStateOptions.class)));
  }

  @Override
  public Response waitForNavigation(Runnable code, WaitForNavigationOptions options) {
    return withLogging("Page.waitForNavigation", () -> waitForNavigationImpl(code, options));
  }

  Response waitForNavigationImpl(Runnable code, WaitForNavigationOptions options) {
    Frame.WaitForNavigationOptions frameOptions = new Frame.WaitForNavigationOptions();
    if (options != null) {
      frameOptions.timeout = options.timeout;
      frameOptions.waitUntil = options.waitUntil;
      frameOptions.glob = options.glob;
      frameOptions.pattern = options.pattern;
      frameOptions.predicate = options.predicate;
    }
    return mainFrame.waitForNavigationImpl(code, frameOptions);
  }

  void frameNavigated(FrameImpl frame) {
    listeners.notify(EventType.FRAMENAVIGATED, frame);
  }

  private static class ErrorImpl implements Error {
    private final SerializedError error;

    ErrorImpl(SerializedError error) {
      this.error = error;
    }

    @Override
    public String message() {
      return error.error.message;
    }

    @Override
    public String name() {
      return error.error.name;
    }

    @Override
    public String stack() {
      return error.error.stack;
    }
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
  public Request waitForRequest(Runnable code, String urlGlob, WaitForRequestOptions options) {
    return waitForRequest(code, toRequestPredicate(new UrlMatcher(urlGlob)), options);
  }

  @Override
  public Request waitForRequest(Runnable code, Pattern urlPattern, WaitForRequestOptions options) {
    return waitForRequest(code, toRequestPredicate(new UrlMatcher(urlPattern)), options);
  }

  @Override
  public Request waitForRequest(Runnable code, Predicate<Request> predicate, WaitForRequestOptions options) {
    return withLogging("Page.waitForRequest", () -> waitForRequestImpl(code, predicate, options));
  }

  private static Predicate<Request> toRequestPredicate(UrlMatcher matcher) {
    return request -> matcher.test(request.url());
  }

  private Request waitForRequestImpl(Runnable code, Predicate<Request> predicate, WaitForRequestOptions options) {
    if (options == null) {
      options = new WaitForRequestOptions();
    }
    List<Waitable<Request>> waitables = new ArrayList<>();
    waitables.add(new WaitableEvent<>(listeners, EventType.REQUEST,
      request -> predicate == null || predicate.test(request)));
    waitables.add(createWaitForCloseHelper());
    waitables.add(createWaitableTimeout(options.timeout));
    return runUntil(code, new WaitableRace<>(waitables));
  }

  @Override
  public Response waitForResponse(Runnable code, String urlGlob, WaitForResponseOptions options) {
    return waitForResponse(code, toResponsePredicate(new UrlMatcher(urlGlob)), options);
  }

  @Override
  public Response waitForResponse(Runnable code, Pattern urlPattern, WaitForResponseOptions options) {
    return waitForResponse(code, toResponsePredicate(new UrlMatcher(urlPattern)), options);
  }

  @Override
  public Response waitForResponse(Runnable code, Predicate<Response> predicate, WaitForResponseOptions options) {
    return withLogging("Page.waitForResponse", () -> waitForResponseImpl(code, predicate, options));
  }

  private static Predicate<Response> toResponsePredicate(UrlMatcher matcher) {
    return response -> matcher.test(response.url());
  }

  private Response waitForResponseImpl(Runnable code, Predicate<Response> predicate, WaitForResponseOptions options) {
    if (options == null) {
      options = new WaitForResponseOptions();
    }
    List<Waitable<Response>> waitables = new ArrayList<>();
    waitables.add(new WaitableEvent<>(listeners, EventType.RESPONSE,
      response -> predicate == null || predicate.test(response)));
    waitables.add(createWaitForCloseHelper());
    waitables.add(createWaitableTimeout(options.timeout));
    return runUntil(code, new WaitableRace<>(waitables));
  }

  @Override
  public ElementHandle waitForSelector(String selector, WaitForSelectorOptions options) {
    return withLogging("Page.waitForSelector",
      () -> mainFrame.waitForSelectorImpl(selector, convertViaJson(options, Frame.WaitForSelectorOptions.class)));
  }

  @Override
  public void waitForTimeout(double timeout) {
    withLogging("Page.waitForTimeout", () -> mainFrame.waitForTimeoutImpl(timeout));
  }

  @Override
  public List<Worker> workers() {
    return new ArrayList<>(workers);
  }
}
