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
import com.google.gson.JsonObject;
import com.microsoft.playwright.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
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
  // TODO: do not rely on the frame order in the tests
  private final Set<FrameImpl> frames = new LinkedHashSet<>();
  private final ListenerCollection<EventType> listeners = new ListenerCollection<>();
  private final List<WaitEventHelper> eventHelpers = new ArrayList<>();
  final Map<String, Binding> bindings = new HashMap<String, Binding>();
  BrowserContextImpl ownedContext;
  private boolean isClosed;

  PageImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    browserContext = (BrowserContextImpl) parent;
    mainFrame = connection.getExistingObject(initializer.getAsJsonObject("mainFrame").get("guid").getAsString());
    mainFrame.page = this;
    keyboard = new KeyboardImpl(this);
    mouse = new MouseImpl(this);
    frames.add(mainFrame);
  }

  public Deferred<Page> waitForPopup() {
    CompletableFuture<JsonObject> popupFuture = futureForEvent("popup");
    return () -> {
      JsonObject params = waitForCompletion(popupFuture);
      String guid = params.getAsJsonObject("page").get("guid").getAsString();
      return connection.getExistingObject(guid);
    };
  }

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
    } else if ("console".equals(event)) {
      String guid = params.getAsJsonObject("message").get("guid").getAsString();
      ConsoleMessageImpl message = connection.getExistingObject(guid);
      listeners.notify(EventType.CONSOLE, message);
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
    } else if ("close".equals(event)) {
      isClosed = true;
      browserContext.pages.remove(this);
      listeners.notify(EventType.CLOSE, this);
    }
    for (WaitEventHelper h : new ArrayList<>(eventHelpers)) {
      h.handleEvent(event, params);
    }
  }

  @Override
  public void addListener(EventType type, Listener<EventType> listener) {
    listeners.add(type, listener);
  }

  @Override
  public void removeListener(EventType type, Listener<EventType> listener) {
    listeners.remove(type, listener);
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

  }

  @Override
  public void emulateMedia(EmulateMediaOptions options) {

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
  public void exposeBinding(String name, Binding playwrightBinding) {
    if (bindings.containsKey(name)) {
      throw new RuntimeException("Function " + name + " has already been registered");
    }
    if (browserContext.bindings.containsKey(name)) {
      throw new RuntimeException("Function " + name + " has already been registered in the browser context");
    }
    bindings.put(name, playwrightBinding);

    JsonObject params = new JsonObject();
    params.addProperty("name", name);
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
    return null;
  }

  @Override
  public Response goBack(GoBackOptions options) {
    return null;
  }

  @Override
  public Response goForward(GoForwardOptions options) {
    return null;
  }

  @Override
  public ResponseImpl navigate(String url, NavigateOptions options) {
    return mainFrame.navigate(url, convertViaJson(options, Frame.NavigateOptions.class));
  }

  @Override
  public void hover(String selector, HoverOptions options) {

  }

  @Override
  public String innerHTML(String selector, InnerHTMLOptions options) {
    return null;
  }

  @Override
  public String innerText(String selector, InnerTextOptions options) {
    return null;
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
    JsonObject result = sendMessage("opener", new JsonObject()).getAsJsonObject();
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

  @Override
  public byte[] screenshot(ScreenshotOptions options) {
    return new byte[0];
  }

  @Override
  public List<String> selectOption(String selector, String values, SelectOptionOptions options) {
    return null;
  }

  @Override
  public void setContent(String html, SetContentOptions options) {
    mainFrame.setContent(html, convertViaJson(options, Frame.SetContentOptions.class));
  }

  @Override
  public void setDefaultNavigationTimeout(int timeout) {

  }

  @Override
  public void setDefaultTimeout(int timeout) {

  }

  @Override
  public void setExtraHTTPHeaders(Map<String, String> headers) {

  }

  @Override
  public void setInputFiles(String selector, String files, SetInputFilesOptions options) {

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
  public Viewport viewportSize() {
    return viewport;
  }

  @Override
  public Deferred<Event<EventType>> waitForEvent(EventType event, String optionsOrPredicate) {
    return listeners.waitForEvent(event, connection);
  }

  @Override
  public JSHandle waitForFunction(String pageFunction, Object arg, WaitForFunctionOptions options) {
    return null;
  }

  @Override
  public void waitForLoadState(LoadState state, WaitForLoadStateOptions options) {
    mainFrame.waitForLoadState(convertViaJson(state, Frame.LoadState.class), convertViaJson(options, Frame.WaitForLoadStateOptions.class));
  }

  @Override
  public Deferred<Response> waitForNavigation(WaitForNavigationOptions options) {
    return mainFrame.waitForNavigation(convertViaJson(options, Frame.WaitForNavigationOptions.class));
  }

  private class WaitEventHelper<R> implements Deferred<R> {
    private final CompletableFuture<R> result = new CompletableFuture<>();
    private final String event;
    private final String fieldName;

    WaitEventHelper(String event, String fieldName) {
      this.event = event;
      this.fieldName = fieldName;
      eventHelpers.add(this);
    }

    void handleEvent(String name, JsonObject params) {
      if (event.equals(name)) {
        if (fieldName != null && params.has(fieldName)) {
          result.complete(connection.getExistingObject(params.getAsJsonObject(fieldName).get("guid").getAsString()));
        } else {
          result.complete(null);
        }
      } else if ("close".equals(name)) {
        result.completeExceptionally(new RuntimeException("Page closed"));
      } else if ("crash".equals(name)) {
        result.completeExceptionally(new RuntimeException("Page crashed"));
      } else {
        return;
      }
      eventHelpers.remove(this);
    }

    public R get() {
      return waitForCompletion(result);
    }
  }

  @Override
  public Deferred<Request> waitForRequest(String urlOrPredicate, WaitForRequestOptions options) {
    return new WaitEventHelper<>("request", "request");
  }

  @Override
  public Deferred<Response> waitForResponse(String urlOrPredicate, WaitForResponseOptions options) {
    return new WaitEventHelper<>("response", "response");
  }

  @Override
  public Deferred<ElementHandle> waitForSelector(String selector, WaitForSelectorOptions options) {
    return mainFrame.waitForSelector(selector, convertViaJson(options, Frame.WaitForSelectorOptions.class));
  }

  @Override
  public void waitForTimeout(int timeout) {

  }

  @Override
  public List<Worker> workers() {
    return null;
  }

  @Override
  public Deferred<Void> waitForClose() {
    return new WaitEventHelper<>("close", null);
  }
}
