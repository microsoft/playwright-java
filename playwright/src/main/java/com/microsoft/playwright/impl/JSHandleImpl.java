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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.JSHandle;

import java.util.HashMap;
import java.util.Map;

import static com.microsoft.playwright.impl.Serialization.*;

public class JSHandleImpl extends ChannelOwner implements JSHandle {
  private String preview;

  public JSHandleImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    this.preview = initializer.get("preview").getAsString();
  }

  @Override
  public ElementHandle asElement() {
    return null;
  }

  @Override
  public void dispose() {
    withLogging("JSHandle.dispose", () -> sendMessage("dispose"));
  }

  @Override
  public Object evaluate(String pageFunction, Object arg) {
    return withLogging("JSHandle.evaluate", () -> {
      JsonObject params = new JsonObject();
      params.addProperty("expression", pageFunction);
      params.addProperty("world", "main");
      params.add("arg", gson().toJsonTree(serializeArgument(arg)));
      JsonElement json = sendMessage("evaluateExpression", params);
      SerializedValue value = gson().fromJson(json.getAsJsonObject().get("value"), SerializedValue.class);
      return deserialize(value);
    });
  }

  @Override
  public JSHandle evaluateHandle(String pageFunction, Object arg) {
    return withLogging("JSHandle.evaluateHandle", () -> {
      JsonObject params = new JsonObject();
      params.addProperty("expression", pageFunction);
      params.addProperty("world", "main");
      params.add("arg", gson().toJsonTree(serializeArgument(arg)));
      JsonElement json = sendMessage("evaluateExpressionHandle", params);
      return connection.getExistingObject(json.getAsJsonObject().getAsJsonObject("handle").get("guid").getAsString());
    });
  }

  @Override
  public Map<String, JSHandle> getProperties() {
    return withLogging("JSHandle.getProperties", () -> {
      JsonObject json = sendMessage("getPropertyList").getAsJsonObject();
      Map<String, JSHandle> result = new HashMap<>();
      for (JsonElement e : json.getAsJsonArray("properties")) {
        JsonObject item = e.getAsJsonObject();
        JSHandle value = connection.getExistingObject(item.getAsJsonObject("value").get("guid").getAsString());
        result.put(item.get("name").getAsString(), value);
      }
      return result;
    });
  }

  @Override
  public JSHandle getProperty(String propertyName) {
    return withLogging("JSHandle.getProperty", () -> {
      JsonObject params = new JsonObject();
      params.addProperty("name", propertyName);
      JsonObject json = sendMessage("getProperty", params).getAsJsonObject();
      return connection.getExistingObject(json.getAsJsonObject("handle").get("guid").getAsString());
    });
  }

  @Override
  public Object jsonValue() {
    return withLogging("JSHandle.jsonValue", () -> {
      JsonObject json = sendMessage("jsonValue").getAsJsonObject();
      SerializedValue value = gson().fromJson(json.get("value"), SerializedValue.class);
      return deserialize(value);
    });
  }

  @Override
  void handleEvent(String event, JsonObject parameters) {
    if ("previewUpdated".equals(event)) {
      preview = parameters.get("preview").getAsString();
    }
    super.handleEvent(event, parameters);
  }

  @Override
  public String toString() {
    return preview;
  }
}
