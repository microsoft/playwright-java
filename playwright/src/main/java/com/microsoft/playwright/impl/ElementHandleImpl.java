/**
 * Copyright (c) Microsoft Corporation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright.impl;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.JSHandle;

import java.util.ArrayList;
import java.util.List;

import static com.microsoft.playwright.impl.Serialization.toProtocol;

public class ElementHandleImpl extends JSHandleImpl implements ElementHandle {
  public ElementHandleImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  @Override
  public ElementHandle asElement() {
    return this;
  }

  @Override
  public ElementHandle querySelector(String selector) {
    JsonObject params = new JsonObject();
    params.addProperty("selector", selector);
    JsonElement json = sendMessage("querySelector", params);
    JsonObject element = json.getAsJsonObject().getAsJsonObject("element");
    if (element == null) {
      return null;
    }
    return connection.getExistingObject(element.get("guid").getAsString());
  }

  @Override
  public List<ElementHandle> querySelectorAll(String selector) {
    JsonObject params = new JsonObject();
    params.addProperty("selector", selector);
    JsonElement json = sendMessage("querySelectorAll", params);
    JsonArray elements = json.getAsJsonObject().getAsJsonArray("elements");
    if (elements == null) {
      return null;
    }
    List<ElementHandle> handles = new ArrayList<>();
    for (JsonElement item : elements) {
      handles.add(connection.getExistingObject(item.getAsJsonObject().get("guid").getAsString()));
    }
    return handles;
  }

  @Override
  public Object evalOnSelector(String selector, String pageFunction, Object arg) {
    return null;
  }

  @Override
  public Object evalOnSelectorAll(String selector, String pageFunction, Object arg) {
    return null;
  }

  @Override
  public Object boundingBox() {
    return null;
  }

  @Override
  public void check(CheckOptions options) {
    if (options == null) {
      options = new CheckOptions();
    }
    JsonObject params = new Gson().toJsonTree(options).getAsJsonObject();
    sendMessage("check", params);
  }

  @Override
  public void click(ClickOptions options) {
    if (options == null) {
      options = new ClickOptions();
    }
    JsonObject params = new Gson().toJsonTree(options).getAsJsonObject();
    params.remove("button");
    if (options.button != null) {
      params.addProperty("button", toProtocol(options.button));
    }

    params.remove("modifiers");
    if (options.modifiers != null) {
      params.add("modifiers", toProtocol(options.modifiers));
    }

    sendMessage("click", params);
  }

  @Override
  public Frame contentFrame() {
    return null;
  }

  @Override
  public void dblclick(DblclickOptions options) {
    if (options == null) {
      options = new DblclickOptions();
    }
    JsonObject params = new Gson().toJsonTree(options).getAsJsonObject();
    params.remove("button");
    if (options.button != null) {
      params.addProperty("button", toProtocol(options.button));
    }

    params.remove("modifiers");
    if (options.modifiers != null) {
      params.add("modifiers", toProtocol(options.modifiers));
    }

    sendMessage("dblclick", params);
  }

  @Override
  public void dispatchEvent(String type, Object eventInit) {

  }

  @Override
  public void fill(String value, FillOptions options) {

  }

  @Override
  public void focus() {

  }

  @Override
  public String getAttribute(String name) {
    return null;
  }

  @Override
  public void hover(HoverOptions options) {

  }

  @Override
  public String innerHTML() {
    return null;
  }

  @Override
  public String innerText() {
    return null;
  }

  @Override
  public Frame ownerFrame() {
    return null;
  }

  @Override
  public void press(String key, PressOptions options) {

  }

  @Override
  public byte[] screenshot(ScreenshotOptions options) {
    return new byte[0];
  }

  @Override
  public void scrollIntoViewIfNeeded(ScrollIntoViewIfNeededOptions options) {

  }

  @Override
  public List<String> selectOption(String values, SelectOptionOptions options) {
    return null;
  }

  @Override
  public void selectText(SelectTextOptions options) {

  }

  @Override
  public void setInputFiles(String files, SetInputFilesOptions options) {

  }

  @Override
  public String textContent() {
    return null;
  }

  @Override
  public void type(String text, TypeOptions options) {

  }

  @Override
  public void uncheck(UncheckOptions options) {
    if (options == null) {
      options = new UncheckOptions();
    }
    JsonObject params = new Gson().toJsonTree(options).getAsJsonObject();
    sendMessage("uncheck", params);
  }

  @Override
  public void waitForElementState(ElementState state, WaitForElementStateOptions options) {

  }

  @Override
  public ElementHandle waitForSelector(String selector, WaitForSelectorOptions options) {
    return null;
  }
}
