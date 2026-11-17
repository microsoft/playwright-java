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

import com.microsoft.playwright.impl.protocol.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.JSHandle;

import java.util.HashMap;
import java.util.Map;

import static com.microsoft.playwright.impl.Serialization.*;

public class JSHandleImpl extends ChannelOwner implements JSHandle {
  private String preview;

  final JSHandleChannel channel = new JSHandleChannel(this);

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
    try {
      channel.dispose(NO_TIMEOUT);
    } catch (TargetClosedError e) {
    }
  }

  @Override
  public Object evaluate(String pageFunction, Object arg) {
    JSHandleEvaluateExpressionParams params = new JSHandleEvaluateExpressionParams();
    params.expression = pageFunction;
    params.arg = serializeArgument(arg);
    return deserialize(channel.evaluateExpression(params, NO_TIMEOUT).value);
  }

  @Override
  public JSHandle evaluateHandle(String pageFunction, Object arg) {
    JSHandleEvaluateExpressionHandleParams params = new JSHandleEvaluateExpressionHandleParams();
    params.expression = pageFunction;
    params.arg = serializeArgument(arg);
    return channel.evaluateExpressionHandle(params, NO_TIMEOUT).handle;
  }

  @Override
  public Map<String, JSHandle> getProperties() {
    JSHandleGetPropertyListResult json = channel.getPropertyList(NO_TIMEOUT);
    Map<String, JSHandle> result = new HashMap<>();
    for (JSHandleGetPropertyListResult.JSHandleGetPropertyListResultProperties item : json.properties) {
      result.put(item.name, item.value);
    }
    return result;
  }

  @Override
  public JSHandle getProperty(String propertyName) {
    JSHandleGetPropertyParams params = new JSHandleGetPropertyParams();
    params.name = propertyName;
    return channel.getProperty(params, NO_TIMEOUT).handle;
  }

  @Override
  public Object jsonValue() {
    return deserialize(channel.jsonValue(NO_TIMEOUT).value);
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
