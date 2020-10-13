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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.JSHandle;

import java.util.Map;

import static com.microsoft.playwright.impl.Serialization.deserialize;
import static com.microsoft.playwright.impl.Serialization.serializeArgument;
import static com.microsoft.playwright.impl.Utils.isFunctionBody;

public class JSHandleImpl extends ChannelOwner implements JSHandle {
  public JSHandleImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  @Override
  public ElementHandle asElement() {
    return null;
  }

  @Override
  public Object evaluate(String pageFunction, Object arg) {
    JsonObject params = new JsonObject();
    params.addProperty("expression", pageFunction);
    params.addProperty("world", "main");
    params.addProperty("isFunction", isFunctionBody(pageFunction));
    params.add("arg", new Gson().toJsonTree(serializeArgument(arg)));
    JsonElement json = sendMessage("evaluateExpression", params);
    SerializedValue value = new Gson().fromJson(json.getAsJsonObject().get("value"), SerializedValue.class);
    return deserialize(value);
  }

  @Override
  public JSHandle evaluateHandle(String pageFunction, Object arg) {
    JsonObject params = new JsonObject();
    params.addProperty("expression", pageFunction);
    params.addProperty("world", "main");
    params.addProperty("isFunction", isFunctionBody(pageFunction));
    params.add("arg", new Gson().toJsonTree(serializeArgument(arg)));
    JsonElement json = sendMessage("evaluateExpressionHandle", params);
    return connection.getExistingObject(json.getAsJsonObject().getAsJsonObject("handle").get("guid").getAsString());
  }

  @Override
  public Map<String, JSHandle> getProperties() {
    return null;
  }

  @Override
  public JSHandle getProperty(String propertyName) {
    return null;
  }

  @Override
  public Object jsonValue() {
    return null;
  }
}
