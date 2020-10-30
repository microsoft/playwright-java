/*
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
import com.microsoft.playwright.Deferred;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.FileChooser;
import com.microsoft.playwright.Frame;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static com.microsoft.playwright.impl.Serialization.*;
import static com.microsoft.playwright.impl.Utils.isFunctionBody;

class ElementHandleImpl extends JSHandleImpl implements ElementHandle {
  ElementHandleImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
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
    JsonObject params = new JsonObject();
    params.addProperty("selector", selector);
    params.addProperty("expression", pageFunction);
    params.addProperty("isFunction", isFunctionBody(pageFunction));
    params.add("arg", gson().toJsonTree(serializeArgument(arg)));
    JsonElement json = sendMessage("evalOnSelector", params);
    SerializedValue value = gson().fromJson(json.getAsJsonObject().get("value"), SerializedValue.class);
    return deserialize(value);
  }

  @Override
  public Object evalOnSelectorAll(String selector, String pageFunction, Object arg) {
    JsonObject params = new JsonObject();
    params.addProperty("selector", selector);
    params.addProperty("expression", pageFunction);
    params.addProperty("isFunction", isFunctionBody(pageFunction));
    params.add("arg", gson().toJsonTree(serializeArgument(arg)));
    JsonElement json = sendMessage("evalOnSelectorAll", params);
    SerializedValue value = gson().fromJson(json.getAsJsonObject().get("value"), SerializedValue.class);
    return deserialize(value);
  }

  @Override
  public BoundingBox boundingBox() {
    JsonObject json = sendMessage("boundingBox").getAsJsonObject();
    if (!json.has("value")) {
      return null;
    }
    return gson().fromJson(json.get("value"), BoundingBox.class);
  }

  @Override
  public void check(CheckOptions options) {
    if (options == null) {
      options = new CheckOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    sendMessage("check", params);
  }

  @Override
  public void click(ClickOptions options) {
    if (options == null) {
      options = new ClickOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.remove("button");
    if (options.button != null) {
      params.addProperty("button", Serialization.toProtocol(options.button));
    }

    params.remove("modifiers");
    if (options.modifiers != null) {
      params.add("modifiers", Serialization.toProtocol(options.modifiers));
    }

    sendMessage("click", params);
  }

  @Override
  public Frame contentFrame() {
    JsonObject json = sendMessage("contentFrame").getAsJsonObject();
    if (!json.has("frame")) {
      return null;
    }
    return connection.getExistingObject(json.getAsJsonObject("frame").get("guid").getAsString());
  }

  @Override
  public void dblclick(DblclickOptions options) {
    if (options == null) {
      options = new DblclickOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.remove("button");
    if (options.button != null) {
      params.addProperty("button", Serialization.toProtocol(options.button));
    }

    params.remove("modifiers");
    if (options.modifiers != null) {
      params.add("modifiers", Serialization.toProtocol(options.modifiers));
    }

    sendMessage("dblclick", params);
  }

  @Override
  public void dispatchEvent(String type, Object eventInit) {
    JsonObject params = new JsonObject();
    params.addProperty("type", type);
    params.add("eventInit", gson().toJsonTree(serializeArgument(eventInit)));
    sendMessage("dispatchEvent", params);
  }

  @Override
  public void fill(String value, FillOptions options) {
    if (options == null) {
      options = new FillOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("value", value);
    sendMessage("fill", params);
  }

  @Override
  public void focus() {
    sendMessage("focus");
  }

  @Override
  public String getAttribute(String name) {
    JsonObject params = new JsonObject();
    params.addProperty("name", name);
    JsonObject json = sendMessage("getAttribute", params).getAsJsonObject();
    return json.has("value") ? json.get("value").getAsString() : null;
  }

  @Override
  public void hover(HoverOptions options) {
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.remove("modifiers");
    if (options.modifiers != null) {
      params.add("modifiers", Serialization.toProtocol(options.modifiers));
    }
    sendMessage("hover", params);
  }

  @Override
  public String innerHTML() {
    JsonObject json = sendMessage("innerHTML").getAsJsonObject();
    return json.get("value").getAsString();
  }

  @Override
  public String innerText() {
    JsonObject json = sendMessage("innerText").getAsJsonObject();
    return json.get("value").getAsString();
  }

  @Override
  public Frame ownerFrame() {
    JsonObject json = sendMessage("ownerFrame").getAsJsonObject();
    if (!json.has("frame")) {
      return null;
    }
    return connection.getExistingObject(json.getAsJsonObject("frame").get("guid").getAsString());
  }

  @Override
  public void press(String key, PressOptions options) {
    if (options == null) {
      options = new PressOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("key", key);
    sendMessage("press", params);
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
  public void scrollIntoViewIfNeeded(ScrollIntoViewIfNeededOptions options) {
    if (options == null) {
      options = new ScrollIntoViewIfNeededOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    sendMessage("scrollIntoViewIfNeeded", params);
  }

  @Override
  public List<String> selectOption(SelectOption[] values, SelectOptionOptions options) {
    if (options == null) {
      options = new SelectOptionOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    if (values != null) {
      params.add("options", gson().toJsonTree(values));
    }
    return selectOption(params);
  }

  @Override
  public List<String> selectOption(ElementHandle[] values, SelectOptionOptions options) {
    if (options == null) {
      options = new SelectOptionOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    if (values != null) {
      params.add("elements", Serialization.toProtocol(values));
    }
    return selectOption(params);
  }

  private List<String> selectOption(JsonObject params) {
    JsonObject json = sendMessage("selectOption", params).getAsJsonObject();
    return parseStringList(json.getAsJsonArray("values"));
  }

  @Override
  public void selectText(SelectTextOptions options) {
    if (options == null) {
      options = new SelectTextOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    sendMessage("selectText", params);
  }

  @Override
  public void setInputFiles(Path[] files, SetInputFilesOptions options) {
    setInputFiles(Utils.toFilePayloads(files), options);
  }

  @Override
  public void setInputFiles(FileChooser.FilePayload[] files, SetInputFilesOptions options) {
    if (options == null) {
      options = new SetInputFilesOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.add("files", Serialization.toJsonArray(files));
    sendMessage("setInputFiles", params);
  }

  @Override
  public String textContent() {
    JsonObject json = sendMessage("textContent").getAsJsonObject();
    return json.has("value") ? json.get("value").getAsString() : null;
  }

  @Override
  public void type(String text, TypeOptions options) {
    if (options == null) {
      options = new TypeOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("text", text);
    sendMessage("type", params);
  }

  @Override
  public void uncheck(UncheckOptions options) {
    if (options == null) {
      options = new UncheckOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    sendMessage("uncheck", params);
  }

  @Override
  public Deferred<Void> waitForElementState(ElementState state, WaitForElementStateOptions options) {
    if (options == null) {
      options = new WaitForElementStateOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("state", toProtocol(state));
    return toDeferred(sendMessageAsync("waitForElementState", params).apply(json -> null));
  }

  private static String toProtocol(ElementState state) {
    if (state == null) {
      throw new IllegalArgumentException("State cannot by null");
    }
    return state.toString().toLowerCase();
  }

  @Override
  public Deferred<ElementHandle> waitForSelector(String selector, WaitForSelectorOptions options) {
    if (options == null) {
      options = new WaitForSelectorOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.remove("state");
    params.addProperty("state", toProtocol(options.state));
    params.addProperty("selector", selector);
    return toDeferred(sendMessageAsync("waitForElementState", params).apply(json -> null));
  }

  private static String toProtocol(WaitForSelectorOptions.State state) {
    if (state == null) {
      state = WaitForSelectorOptions.State.VISIBLE;
    }
    return state.toString().toLowerCase();
  }
}
