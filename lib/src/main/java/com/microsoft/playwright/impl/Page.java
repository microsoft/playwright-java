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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;


public class Page extends ChannelOwner {
  private final Frame mainFrame;
  private final List<DialogHandler> dialogHandlers = new ArrayList<>();
  private final List<ConsoleListener> consoleListeners = new ArrayList<>();

  Page(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    mainFrame = connection.getExistingObject(initializer.getAsJsonObject("mainFrame").get("guid").getAsString());
    mainFrame.page = this;
  }

  public Response navigate(String url) {
    return navigate(url, new NavigateOptions());
  }
  public Response navigate(String url, NavigateOptions options) {
    return mainFrame.navigate(url, options);
  }

  public void click(String selector) {
    mainFrame.click(selector);
  }

  public Supplier<Page> waitForPopup() {
    Supplier<JsonObject> popupSupplier = waitForEvent("popup");
    return () -> {
      JsonObject params = popupSupplier.get();
      String guid = params.getAsJsonObject("page").get("guid").getAsString();
      return connection.getExistingObject(guid);
    };
  }

  public interface DialogHandler {
    void handle(Dialog d);
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
      Dialog dialog = connection.getExistingObject(guid);
      for (DialogHandler handler: new ArrayList<>(dialogHandlers))
        handler.handle(dialog);
      // If no action taken dismiss dialog to not hang.
      if (!dialog.isHandled())
        dialog.dismiss();
    } else if ("console".equals(event)) {
      String guid = params.getAsJsonObject("message").get("guid").getAsString();
      ConsoleMessage message = connection.getExistingObject(guid);
      for (ConsoleListener listener: new ArrayList<>(consoleListeners))
        listener.handle(message);
    }
  }

  public interface ConsoleListener {
    void handle(ConsoleMessage m);
  }

  public void addConsoleListener(ConsoleListener listener) {
    consoleListeners.add(listener);
  }

  public void removeConsoleListener(ConsoleListener listener) {
    consoleListeners.remove(listener);
  }

  public <T> T evalTyped(String expression) {
    return mainFrame.evalTyped(expression);
  }

  public JsonElement evaluate(String expression) {
    return evaluate(expression, null);
  }

  public JsonElement evaluate(String expression, Object arg) {
    return evaluate(expression, arg, false);
  }

  public JsonElement evaluate(String expression, Object arg, boolean forceExpression) {
    return mainFrame.evaluate(expression, arg, forceExpression);
  }
}
