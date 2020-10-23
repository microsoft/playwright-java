/**
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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.microsoft.playwright.*;

import java.io.*;
import java.nio.file.Watchable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.Utils.convertViaJson;


public class PageImpl extends ChannelOwner implements Page {
  private final BrowserContextImpl browserContext;
  private final FrameImpl mainFrame;
  private final KeyboardImpl keyboard;
  private final MouseImpl mouse;
  private Viewport viewport;
  private final Router routes = new Router();
  private final Set<FrameImpl> frames = new LinkedHashSet<>();
  private final ListenerCollection<EventType> listeners = new ListenerCollection<>();
  final Map<String, Binding> bindings = new HashMap<>();
  BrowserContextImpl ownedContext;
  private boolean isClosed;
  final Set<Worker> workers = new HashSet<>();
  private final TimeoutSettings timeoutSettings;

  PageImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    browserContext = (BrowserContextImpl) parent;
    mainFrame = connection.getExistingObject(initializer.getAsJsonObject("mainFrame").get("guid").getAsString());
    mainFrame.page = this;
    keyboard = new KeyboardImpl(this);
    mouse = new MouseImpl(this);
    frames.add(mainFrame);
    timeoutSettings = new TimeoutSettings(browserContext.timeoutSettings);
  }

  @Override
  protected void handleEvent(String event, JsonObject params) {
    if ("dialog".equals(event)) {
      String guid = params.getAsJsonObject("dialog").get("guid").getAsString();
      DialogImpl dialog = connection.getExistingObject(guid);
      listeners.notify(EventType.DIALOG, dialog);
      // If no action taken dismiss dialog to not hang.
      if (!dialog.isHandled()) {
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
      ElementHandle elementHandle = connection.getExistingObject(guid);
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
      listeners.notify(EventType.LOAD, null);
    } else if ("domcontentloaded".equals(event)) {
      listeners.notify(EventType.DOMCONTENTLOADED, null);
    } else if ("request".equals(event)) {
      String guid = params.getAsJsonObject("request").get("guid").getAsString();
      Request request = connection.getExistingObject(guid);
      listeners.notify(EventType.REQUEST, request);
    } else if ("requestFailed".equals(event)) {
      String guid = params.getAsJsonObject("request").get("guid").getAsString();
      RequestImpl request = connection.getExistingObject(guid);
      if (params.has("failureText")) {
        request.failureText = new Gson().fromJson(params, Request.RequestFailure.class);
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
      Request request = connection.getExistingObject(params.getAsJsonObject("request").get("guid").getAsString());
      boolean handled = routes.handle(route, request);
      if (!handled) {
        handled = browserContext.routes.handle(route, request);
      }
      if (!handled) {
        route.continue_();
      }
    } else if ("pageError".equals(event)) {
      SerializedError error = new Gson().fromJson(params.getAsJsonObject("error"), SerializedError.class);
      listeners.notify(EventType.PAGEERROR, new ErrorImpl(error));
    } else if ("crash".equals(event)) {
      listeners.notify(EventType.CRASH, null);
    } else if ("close".equals(event)) {
      isClosed = true;
      browserContext.pages.remove(this);
      listeners.notify(EventType.CLOSE, null);
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
  public void addListener(EventType type, Listener<EventType> listener) {
    if (type == EventType.FILECHOOSER) {
      willAddFileChooserListener();
    }
    listeners.add(type, listener);
  }

  @Override
  public void removeListener(EventType type, Listener<EventType> listener) {
    listeners.remove(type, listener);
    if (type == EventType.FILECHOOSER) {
      didRemoveFileChooserListener();
    }
  }

  @Override
  public void close(CloseOptions options) {
    JsonObject params = options == null ? new JsonObject() : new Gson().toJsonTree(options).getAsJsonObject();
    sendMessage("close", params);
    if (ownedContext != null) {
      ownedContext.close();
    }
  }

  @Override
  public ElementHandle querySelector(String selector) {
    return mainFrame.querySelector(selector);
  }

  @Override
  public List<ElementHandle> querySelectorAll(String selector) {
    return mainFrame.querySelectorAll(selector);
  }

  @Override
  public Object evalOnSelector(String selector, String pageFunction, Object arg) {
    return mainFrame.evalOnSelector(selector, pageFunction, arg);
  }

  @Override
  public Object evalOnSelectorAll(String selector, String pageFunction, Object arg) {
    return mainFrame.evalOnSelectorAll(selector, pageFunction, arg);
  }

  @Override
  public Accessibility accessibility() {
    return null;
  }

  @Override
  public void addInitScript(String script, Object arg) {
    JsonObject params = new JsonObject();
    // TODO: support or drop arg
    params.addProperty("source", script);
    sendMessage("addInitScript", params);
  }

  @Override
  public ElementHandle addScriptTag(AddScriptTagOptions options) {
    return mainFrame.addScriptTag(convertViaJson(options, Frame.AddScriptTagOptions.class));
  }

  @Override
  public ElementHandle addStyleTag(AddStyleTagOptions options) {
    return mainFrame.addStyleTag(convertViaJson(options, Frame.AddStyleTagOptions.class));
  }

  @Override
  public void bringToFront() {
    sendMessage("bringToFront");
  }

  @Override
  public void check(String selector, CheckOptions options) {
    mainFrame.check(selector, convertViaJson(options, Frame.CheckOptions.class));
  }

  @Override
  public void click(String selector, ClickOptions options) {
    mainFrame.click(selector, convertViaJson(options, Frame.ClickOptions.class));
  }

  @Override
  public String content() {
    return mainFrame.content();
  }

  @Override
  public BrowserContext context() {
    return browserContext;
  }

  @Override
  public ChromiumCoverage coverage() {
    return null;
  }

  @Override
  public void dblclick(String selector, DblclickOptions options) {
    mainFrame.dblclick(selector, convertViaJson(options, Frame.DblclickOptions.class));
  }

  @Override
  public void dispatchEvent(String selector, String type, Object eventInit, DispatchEventOptions options) {
    mainFrame.dispatchEvent(selector, type, eventInit, convertViaJson(options, Frame.DispatchEventOptions.class));
  }

  private static String toProtocol(EmulateMediaOptions.Media media) {
    if (media == null) {
      return "null";
    }
    return media.toString().toLowerCase();
  }

  private static String toProtocol(EmulateMediaOptions.ColorScheme colorScheme) {
    if (colorScheme == null) {
      return "null";
    }
    switch (colorScheme) {
      case DARK:
        return "dark";
      case LIGHT:
        return "light";
      case NO_PREFERENCE:
        return "no-preference";
      default:
        throw new RuntimeException("Unknown option: " + colorScheme);
    }
  }

  @Override
  public void emulateMedia(EmulateMediaOptions options) {
    JsonObject params = new JsonObject();
    params.addProperty("media", toProtocol(options.media));
    params.addProperty("colorScheme", toProtocol(options.colorScheme));
    sendMessage("emulateMedia", params);
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
  public void exposeBinding(String name, Binding playwrightBinding, ExposeBindingOptions options) {
    if (bindings.containsKey(name)) {
      throw new RuntimeException("Function \"" + name + "\" has been already registered");
    }
    if (browserContext.bindings.containsKey(name)) {
      throw new RuntimeException("Function \"" + name + "\" has been already registered in the browser context");
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
    exposeBinding(name, (Binding.Source source, Object... args) -> playwrightFunction.call(args));
  }

  @Override
  public void fill(String selector, String value, FillOptions options) {
    mainFrame.fill(selector, value, convertViaJson(options, Frame.FillOptions.class));
  }

  @Override
  public void focus(String selector, FocusOptions options) {
    mainFrame.focus(selector, convertViaJson(options, Frame.FocusOptions.class));
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
    return mainFrame.getAttribute(selector, name, convertViaJson(options, Frame.GetAttributeOptions.class));
  }

  @Override
  public Response goBack(GoBackOptions options) {
    if (options == null) {
      options = new GoBackOptions();
    }
    JsonObject params = new Gson().toJsonTree(options).getAsJsonObject();
    params.remove("waitUntil");
    params.addProperty("waitUntil", FrameImpl.toProtocol(options.waitUntil));
    JsonObject json = sendMessage("goBack", params).getAsJsonObject();
    if (json.has("response")) {
      return connection.getExistingObject(json.getAsJsonObject("response").get("guid").getAsString());
    }
    return null;
  }

  @Override
  public Response goForward(GoForwardOptions options) {
    if (options == null) {
      options = new GoForwardOptions();
    }
    JsonObject params = new Gson().toJsonTree(options).getAsJsonObject();
    params.remove("waitUntil");
    params.addProperty("waitUntil", FrameImpl.toProtocol(options.waitUntil));
    JsonObject json = sendMessage("goForward", params).getAsJsonObject();
    if (json.has("response")) {
      return connection.getExistingObject(json.getAsJsonObject("response").get("guid").getAsString());
    }
    return null;
  }

  @Override
  public ResponseImpl navigate(String url, NavigateOptions options) {
    return mainFrame.navigate(url, convertViaJson(options, Frame.NavigateOptions.class));
  }

  @Override
  public void hover(String selector, HoverOptions options) {
    mainFrame.hover(selector, convertViaJson(options, Frame.HoverOptions.class));
  }

  @Override
  public String innerHTML(String selector, InnerHTMLOptions options) {
    return mainFrame.innerHTML(selector, convertViaJson(options, Frame.InnerHTMLOptions.class));
  }

  @Override
  public String innerText(String selector, InnerTextOptions options) {
    return mainFrame.innerText(selector, convertViaJson(options, Frame.InnerTextOptions.class));
  }

  @Override
  public boolean isClosed() {
    return isClosed;
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
    JsonObject result = sendMessage("opener").getAsJsonObject();
    if (!result.has("page")) {
      return null;
    }
    return connection.getExistingObject(result.getAsJsonObject("page").get("guid").getAsString());
  }

  @Override
  public byte[] pdf(PdfOptions options) {
    return new byte[0];
  }

  @Override
  public void press(String selector, String key, PressOptions options) {
    mainFrame.press(selector, key, convertViaJson(options, Frame.PressOptions.class));
  }

  @Override
  public Response reload(ReloadOptions options) {
    if (options == null) {
      options = new ReloadOptions();
    }
    JsonObject params = new Gson().toJsonTree(options).getAsJsonObject();
    params.remove("waitUntil");
    params.addProperty("waitUntil", FrameImpl.toProtocol(options.waitUntil));
    JsonObject json = sendMessage("reload", params).getAsJsonObject();
    if (json.has("response")) {
      return connection.getExistingObject(json.getAsJsonObject("response").get("guid").getAsString());
    }
    return null;
  }

  @Override
  public void route(String url, BiConsumer<Route, Request> handler) {
    route(new UrlMatcher(url), handler);
  }

  @Override
  public void route(Pattern url, BiConsumer<Route, Request> handler) {
    route(new UrlMatcher(url), handler);
  }

  @Override
  public void route(Predicate<String> url, BiConsumer<Route, Request> handler) {
    route(new UrlMatcher(url), handler);
  }

  private void route(UrlMatcher matcher, BiConsumer<Route, Request> handler) {
    routes.add(matcher, handler);
    if (routes.size() == 1) {
      JsonObject params = new JsonObject();
      params.addProperty("enabled", true);
      sendMessage("setNetworkInterceptionEnabled", params);
    }
  }

  private static String toProtocol(ScreenshotOptions.Type type) {
    return type.toString().toLowerCase();
  }

  @Override
  public byte[] screenshot(ScreenshotOptions options) {
    if (options == null) {
      options = new ScreenshotOptions();
    }
    if (options.type == null) {
      options.type = ScreenshotOptions.Type.PNG;
      if (options.path != null) {
        int extStart = options.path.getName().lastIndexOf('.');
        if (extStart != -1) {
          String extension = options.path.getName().substring(extStart).toLowerCase();
          if (".jpeg".equals(extension) || ".jpg".equals(extension)) {
            options.type = ScreenshotOptions.Type.JPEG;
          }
        }
      }
    }
    JsonObject params = new Gson().toJsonTree(options).getAsJsonObject();
    params.remove("type");
    params.addProperty("type", toProtocol(options.type));
    params.remove("path");
    JsonObject json = sendMessage("screenshot", params).getAsJsonObject();

    byte[] buffer = Base64.getDecoder().decode(json.get("binary").getAsString());
    if (options.path != null) {
      Utils.writeToFile(buffer, options.path);
    }
    return buffer;
  }

  @Override
  public List<String> selectOption(String selector, String values, SelectOptionOptions options) {
    return mainFrame.selectOption(selector, values, convertViaJson(options, Frame.SelectOptionOptions.class));
  }

  @Override
  public void setContent(String html, SetContentOptions options) {
    mainFrame.setContent(html, convertViaJson(options, Frame.SetContentOptions.class));
  }

  @Override
  public void setDefaultNavigationTimeout(int timeout) {
    timeoutSettings.setDefaultNavigationTimeout(timeout);
    JsonObject params = new JsonObject();
    params.addProperty("timeout", timeout);
    sendMessage("setDefaultNavigationTimeoutNoReply", params);
  }

  @Override
  public void setDefaultTimeout(int timeout) {
    timeoutSettings.setDefaultTimeout(timeout);
    JsonObject params = new JsonObject();
    params.addProperty("timeout", timeout);
    sendMessage("setDefaultTimeoutNoReply", params);
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
    sendMessage("setExtraHTTPHeaders", params);
  }

  @Override
  public void setInputFiles(String selector, File[] files, SetInputFilesOptions options) {
    mainFrame.setInputFiles(selector, files, convertViaJson(options, Frame.SetInputFilesOptions.class));
  }

  @Override
  public void setInputFiles(String selector, FileChooser.FilePayload[] files, SetInputFilesOptions options) {
    mainFrame.setInputFiles(selector, files, convertViaJson(options, Frame.SetInputFilesOptions.class));
  }

  @Override
  public void setViewportSize(int width, int height) {
    viewport = new Viewport(width, height);
    JsonObject params = new JsonObject();
    params.add("viewportSize", new Gson().toJsonTree(viewport));
    sendMessage("setViewportSize", params);
  }

  @Override
  public String textContent(String selector, TextContentOptions options) {
    return mainFrame.textContent(selector, convertViaJson(options, Frame.TextContentOptions.class));
  }

  @Override
  public String title() {
    return mainFrame.title();
  }

  @Override
  public void type(String selector, String text, TypeOptions options) {
    mainFrame.type(selector, text, convertViaJson(options, Frame.TypeOptions.class));
  }

  @Override
  public void uncheck(String selector, UncheckOptions options) {
    mainFrame.uncheck(selector, convertViaJson(options, Frame.UncheckOptions.class));
  }

  @Override
  public void unroute(String url, BiConsumer<Route, Request> handler) {
    unroute(new UrlMatcher(url), handler);
  }

  @Override
  public void unroute(Pattern url, BiConsumer<Route, Request> handler) {
    unroute(new UrlMatcher(url), handler);
  }

  @Override
  public void unroute(Predicate<String> url, BiConsumer<Route, Request> handler) {
    unroute(new UrlMatcher(url), handler);
  }

  private void unroute(UrlMatcher matcher, BiConsumer<Route, Request> handler) {
    routes.remove(matcher, handler);
    if (routes.size() == 0) {
      JsonObject params = new JsonObject();
      params.addProperty("enabled", false);
      sendMessage("setNetworkInterceptionEnabled", params);
    }
  }

  @Override
  public String url() {
    return mainFrame.url();
  }

  @Override
  public Video video() {
    return null;
  }

  @Override
  public Viewport viewportSize() {
    return viewport;
  }

  <T> Waitable<T> createWaitableNavigationTimeout(Integer timeout) {
    return new WaitableTimeout<>(timeoutSettings.navigationTimeout(timeout));
  }

  <T> Waitable<T> createWaitableTimeout(Integer timeout) {
    return timeoutSettings.createWaitable(timeout);
  }

  @Override
  public Deferred<Event<EventType>> waitForEvent(EventType event, WaitForEventOptions options) {
    if (options == null) {
      options = new WaitForEventOptions();
    }
    List<Waitable<Event<EventType>>> waitables = new ArrayList<>();
    if (event == EventType.FILECHOOSER) {
      willAddFileChooserListener();
      waitables.add(new WaitableEvent<EventType>(listeners, event, options.predicate) {
        @Override
        public void dispose() {
          super.dispose();
          didRemoveFileChooserListener();
        }
      });
    } else {
      waitables.add(new WaitableEvent<>(listeners, event, options.predicate));
    }
    waitables.add(createWaitableTimeout(options.timeout));
    return toDeferred(new WaitableRace<>(waitables));
  }

  @Override
  public Deferred<JSHandle> waitForFunction(String pageFunction, Object arg, WaitForFunctionOptions options) {
    return mainFrame.waitForFunction(pageFunction, arg, convertViaJson(options, Frame.WaitForFunctionOptions.class));
  }

  @Override
  public Deferred<Void> waitForLoadState(LoadState state, WaitForLoadStateOptions options) {
    return mainFrame.waitForLoadState(convertViaJson(state, Frame.LoadState.class), convertViaJson(options, Frame.WaitForLoadStateOptions.class));
  }

  @Override
  public Deferred<Response> waitForNavigation(WaitForNavigationOptions options) {
    Frame.WaitForNavigationOptions frameOptions = new Frame.WaitForNavigationOptions();
    if (options != null) {
      frameOptions.timeout = options.timeout;
      frameOptions.waitUntil = options.waitUntil;
      frameOptions.glob = options.glob;
      frameOptions.pattern = options.pattern;
      frameOptions.predicate = options.predicate;
    }
    return mainFrame.waitForNavigation(frameOptions);
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

  private class WaitableFrameDetach extends WaitableEvent<EventType> {
    WaitableFrameDetach(Frame frame) {
      super(PageImpl.this.listeners, EventType.FRAMEDETACHED, event -> frame.equals(event.data()));
    }

    @Override
    public Event<EventType> get() {
      throw new RuntimeException("Navigating frame was detached");
    }
  }

  @SuppressWarnings("unchecked")
  <T> Waitable<T> createWaitableFrameDetach(Frame frame) {
    // It is safe to cast as WaitableFrameDetach.get() always throws.
    return (Waitable<T>) new WaitableFrameDetach(frame);
  }

  <T> Waitable<T> createWaitForCloseHelper() {
    return new WaitablePageClose<T>();
  }

  private class WaitablePageClose<R> implements Waitable<R>, Listener<EventType> {
    private final List<EventType> subscribedEvents;
    private String errorMessage;

    WaitablePageClose() {
      subscribedEvents = Arrays.asList(EventType.CLOSE, EventType.CRASH);
      for (EventType e : subscribedEvents) {
        addListener(e, this);
      }
    }

    @Override
    public void handle(Event<EventType> event) {
      if (EventType.CLOSE == event.type()) {
        errorMessage = "Page closed";
      } else if (EventType.CRASH == event.type()) {
        errorMessage = "Page crashed";
      } else {
        return;
      }
      dispose();
    }

    @Override
    public boolean isDone() {
      return errorMessage != null;
    }

    @Override
    public R get() {
      throw new RuntimeException(errorMessage);
    }

    @Override
    public void dispose() {
      for (EventType e : subscribedEvents) {
        removeListener(e, this);
      }
    }
  }

  @Override
  public Deferred<Request> waitForRequest(String urlOrPredicate, WaitForRequestOptions options) {
    if (options == null) {
      options = new WaitForRequestOptions();
    }
    List<Waitable<Request>> waitables = new ArrayList<>();
    waitables.add(new WaitableEvent<>(listeners, EventType.REQUEST,e -> {
        if (urlOrPredicate == null) {
          return true;
        }
        return urlOrPredicate.equals(((Request) e.data()).url());
    }).apply(event -> (Request) event.data()));
    waitables.add(createWaitForCloseHelper());
    waitables.add(createWaitableTimeout(options.timeout));
    return toDeferred(new WaitableRace<>(waitables));
  }

  @Override
  public Deferred<Response> waitForResponse(String urlOrPredicate, WaitForResponseOptions options) {
    if (options == null) {
      options = new WaitForResponseOptions();
    }
    List<Waitable<Response>> waitables = new ArrayList<>();
    waitables.add(new WaitableEvent<>(listeners, EventType.RESPONSE, e -> {
      if (urlOrPredicate == null) {
        return true;
      }
      return urlOrPredicate.equals(((Response) e.data()).url());
    }).apply(event -> (Response) event.data()));
    waitables.add(createWaitForCloseHelper());
    waitables.add(createWaitableTimeout(options.timeout));
    return toDeferred(new WaitableRace<>(waitables));
  }

  @Override
  public Deferred<ElementHandle> waitForSelector(String selector, WaitForSelectorOptions options) {
    return mainFrame.waitForSelector(selector, convertViaJson(options, Frame.WaitForSelectorOptions.class));
  }

  @Override
  public Deferred<Void> waitForTimeout(int timeout) {
    return mainFrame.waitForTimeout(timeout);
  }

  @Override
  public List<Worker> workers() {
    return new ArrayList<>(workers);
  }
}
