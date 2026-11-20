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

// Port of jsHandle.ts.
package com.microsoft.playwright.impl;

import com.google.gson.JsonObject;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.impl.protocol.*;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.microsoft.playwright.impl.Serialization.deserialize;
import static com.microsoft.playwright.impl.Serialization.serializeArgument;

// java: serializeArgument and parseResult are Serialization.serializeArgument and Serialization.deserialize;
// Java has no evaluate options, so EvaluateOptions, serializeArgumentWithCallbacks and the argument assertions are not ported.
public class JSHandleImpl extends ChannelOwner implements JSHandle {
  private String preview;

  final JSHandleChannel channel = new JSHandleChannel(this);

  JSHandleImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    this.preview = connection.protocolGson().fromJson(initializer, JSHandleInitializer.class).preview;
  }

  // java: upstream subscribes in the constructor with this._channel.on('previewUpdated', ...).
  @Override
  protected void handleEvent(String event, JsonObject params) {
    if ("previewUpdated".equals(event)) {
      preview = connection.protocolGson().fromJson(params, JSHandlePreviewUpdatedEvent.class).preview;
    }
  }

  @Override
  public Object evaluate(String pageFunction, Object arg) {
    JSHandleEvaluateExpressionParams params = new JSHandleEvaluateExpressionParams();
    params.expression = pageFunction;
    // java: functions are passed as strings, so isFunction is not sent and the server detects it.
    params.arg = serializeArgument(arg);
    JSHandleEvaluateExpressionResult result = channel.evaluateExpression(params, NO_TIMEOUT);
    return deserialize(result.value);
  }

  @Override
  public JSHandle evaluateHandle(String pageFunction, Object arg) {
    JSHandleEvaluateExpressionHandleParams params = new JSHandleEvaluateExpressionHandleParams();
    params.expression = pageFunction;
    // java: functions are passed as strings, so isFunction is not sent and the server detects it.
    params.arg = serializeArgument(arg);
    JSHandleEvaluateExpressionHandleResult result = channel.evaluateExpressionHandle(params, NO_TIMEOUT);
    return result.handle;
  }

  @Override
  public JSHandle getProperty(String propertyName) {
    JSHandleGetPropertyParams params = new JSHandleGetPropertyParams();
    params.name = propertyName;
    JSHandleGetPropertyResult result = channel.getProperty(params, NO_TIMEOUT);
    return result.handle;
  }

  @Override
  public Map<String, JSHandle> getProperties() {
    Map<String, JSHandle> map = new LinkedHashMap<>();
    for (JSHandleGetPropertyListResult.JSHandleGetPropertyListResultProperties property : channel.getPropertyList(NO_TIMEOUT).properties) {
      map.put(property.name, property.value);
    }
    return map;
  }

  @Override
  public Object jsonValue() {
    return deserialize(channel.jsonValue(NO_TIMEOUT).value);
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
      return;
    }
  }

  @Override
  public String toString() {
    return preview;
  }
}
