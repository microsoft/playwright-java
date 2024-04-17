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

package com.microsoft.playwright.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.CDPSession;

import java.util.HashMap;
import java.util.function.Consumer;

public class CDPSessionImpl extends ChannelOwner implements CDPSession {
  private final ListenerCollection<String> listeners = new ListenerCollection<>(new HashMap<>(), this);

  protected CDPSessionImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  @Override
  void handleEvent(String event, JsonObject parameters) {
    super.handleEvent(event, parameters);
    if ("event".equals(event)) {
      String method = parameters.get("method").getAsString();
      JsonObject params = null;
      if (parameters.has("params")) {
        params = parameters.get("params").getAsJsonObject();
      }
      listeners.notify(method, params);
    }
  }

  public JsonObject send(String method) {
    return send(method, null);
  }

  public JsonObject send(String method, JsonObject params) {
    JsonObject args = new JsonObject();
    if (params != null) {
      args.add("params", params);
    }
    args.addProperty("method", method);
    JsonElement response = connection.sendMessage(guid, "send", args);
    if (response == null) return null;
    else return response.getAsJsonObject().get("result").getAsJsonObject();
  }

  @Override
  public void on(String event, Consumer<JsonObject> handler) {
    listeners.add(event, handler);
  }

  @Override
  public void off(String event, Consumer<JsonObject> handler) {
    listeners.remove(event, handler);
  }

  @Override
  public void detach() {
    sendMessage("detach");
  }
}
