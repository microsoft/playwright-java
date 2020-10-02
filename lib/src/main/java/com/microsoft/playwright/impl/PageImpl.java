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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import static com.microsoft.playwright.impl.Utils.convertViaJson;


public class PageImpl extends ChannelOwner implements Page {
  private final FrameImpl mainFrame;
  private final KeyboardImpl keyboard;
  private final MouseImpl mouse;
  private final List<DialogHandler> dialogHandlers = new ArrayList<>();
  private final List<Listener<ConsoleMessage>> consoleListeners = new ArrayList<>();

  PageImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    mainFrame = connection.getExistingObject(initializer.getAsJsonObject("mainFrame").get("guid").getAsString());
    mainFrame.page = this;
    keyboard = new KeyboardImpl(this);
    mouse = new MouseImpl(this);
  }

  @Override
  public void addConsoleListener(Listener<ConsoleMessage> listener) {
    consoleListeners.add(listener);
  }

  @Override
  public void removeConsoleListener(Listener<ConsoleMessage> listener) {
    consoleListeners.remove(listener);
  }

  @Override
  public Deferred<Page> waitForPopup() {
    Supplier<JsonObject> popupSupplier = waitForProtocolEvent("popup");
    return () -> {
      JsonObject params = popupSupplier.get();
      String guid = params.getAsJsonObject("page").get("guid").getAsString();
      return connection.getExistingObject(guid);
    };
  }

  public interface DialogHandler {
    void handle(DialogImpl d);
  }

  public void addDialogHandler(DialogHandler handler) {
    dialogHandlers.add(handler);
  }

  public void removeDialogHandler(DialogHandler handler) {
    dialogHandlers.remove(handler);
  }

  protected void handleEvent(String event, JsonObject params) {
    if ("dialog".equals(event)) {
      String guid = params.getAsJsonObject("dialog").get("guid").getAsString();
      DialogImpl dialog = connection.getExistingObject(guid);
      for (DialogHandler handler: new ArrayList<>(dialogHandlers)) {
        handler.handle(dialog);
      }
      // If no action taken dismiss dialog to not hang.
      if (!dialog.isHandled())
        dialog.dismiss();
    } else if ("console".equals(event)) {
      String guid = params.getAsJsonObject("message").get("guid").getAsString();
      ConsoleMessageImpl message = connection.getExistingObject(guid);
      for (Listener<ConsoleMessage> listener: new ArrayList<>(consoleListeners)) {
        listener.handle(message);
      }
    }
  }

  public <T> T evalTyped(String expression) {
    return mainFrame.evalTyped(expression);
  }

  @Override
  public void close(CloseOptions options) {

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

  }

  @Override
  public ElementHandle addScriptTag(AddScriptTagOptions options) {
    return null;
  }

  @Override
  public ElementHandle addStyleTag(AddStyleTagOptions options) {
    return null;
  }

  @Override
  public void bringToFront() {

  }

  @Override
  public void check(String selector, CheckOptions options) {

  }

  @Override
  public void click(String selector, ClickOptions options) {
    mainFrame.click(selector, convertViaJson(options, Frame.ClickOptions.class));
  }

  @Override
  public String content() {
    return null;
  }

  @Override
  public BrowserContext context() {
    return null;
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
    return null;
  }

  @Override
  public void exposeBinding(String name, String playwrightBinding) {

  }

  @Override
  public void exposeFunction(String name, String playwrightFunction) {

  }

  @Override
  public void fill(String selector, String value, FillOptions options) {
    mainFrame.fill(selector, value, convertViaJson(options, Frame.FillOptions.class));
  }

  @Override
  public void focus(String selector, FocusOptions options) {

  }

  @Override
  public Frame frame(String options) {
    return null;
  }

  @Override
  public List<Frame> frames() {
    return null;
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
    return false;
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
    return null;
  }

  @Override
  public byte[] pdf(PdfOptions options) {
    return new byte[0];
  }

  @Override
  public void press(String selector, String key, PressOptions options) {

  }

  @Override
  public Response reload(ReloadOptions options) {
    return null;
  }

  @Override
  public void route(String url, BiConsumer<Route, Request> handler) {

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
  public void setViewportSize(ViewportSize viewportSize) {

  }

  @Override
  public String textContent(String selector, TextContentOptions options) {
    return null;
  }

  @Override
  public String title() {
    return mainFrame.title();
  }

  @Override
  public void type(String selector, String text, TypeOptions options) {

  }

  @Override
  public void uncheck(String selector, UncheckOptions options) {

  }

  @Override
  public void unroute(String url, BiConsumer<Route, Request> handler) {

  }

  @Override
  public String url() {
    return null;
  }

  @Override
  public PageViewportSize viewportSize() {
    return null;
  }

  @Override
  public Object waitForEvent(String event, String optionsOrPredicate) {
    return null;
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
  public Response waitForNavigation(WaitForNavigationOptions options) {
    return null;
  }

  @Override
  public Request waitForRequest(String urlOrPredicate, WaitForRequestOptions options) {
    return null;
  }

  @Override
  public Response waitForResponse(String urlOrPredicate, WaitForResponseOptions options) {
    return null;
  }

  @Override
  public ElementHandle waitForSelector(String selector, WaitForSelectorOptions options) {
    return null;
  }

  @Override
  public void waitForTimeout(int timeout) {

  }

  @Override
  public List<Worker> workers() {
    return null;
  }
}
