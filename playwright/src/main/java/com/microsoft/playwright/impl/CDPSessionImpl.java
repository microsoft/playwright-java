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

import com.microsoft.playwright.impl.protocol.*;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.CDPSession;

import java.util.function.Consumer;

public class CDPSessionImpl extends ChannelOwner implements CDPSession {

  final CDPSessionChannel channel = new CDPSessionChannel(this);

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
      emit(method, params);
      emit("event", parameters);
    } else if ("close".equals(event)) {
      emit(Events.CDPSession.CLOSE, this);
    }
  }

  @Override
  public void onClose(Consumer<CDPSession> handler) {
    on(Events.CDPSession.CLOSE, handler);
  }

  @Override
  public void offClose(Consumer<CDPSession> handler) {
    off(Events.CDPSession.CLOSE, handler);
  }

  public JsonObject send(String method) {
    return send(method, null);
  }

  @Override
  public JsonObject send(String method, JsonObject params, SendOptions options) {
    CDPSessionSendParams args = new CDPSessionSendParams();
    args.params = params;
    args.method = method;
    Double timeout = options == null || options.timeout == null ? Double.valueOf(0) : options.timeout;
    CDPSessionSendResult response = channel.send(args, timeout);
    if (response == null) return null;
    else return response.result.getAsJsonObject();
  }

  // java: the raw parameter type overrides EventEmitter.on(String, Consumer<?>) and implements CDPSession.on at once.
  @Override
  @SuppressWarnings("rawtypes")
  public void on(String event, Consumer handler) {
    super.on(event, handler);
  }

  @Override
  @SuppressWarnings("rawtypes")
  public void off(String event, Consumer handler) {
    off(event, handler);
  }

  @Override
  public void detach() {
    channel.detach(NO_TIMEOUT);
  }
}
