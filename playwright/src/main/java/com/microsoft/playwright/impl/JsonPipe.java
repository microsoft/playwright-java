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
import com.google.gson.JsonObject;
import com.microsoft.playwright.PlaywrightException;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

import static com.microsoft.playwright.impl.Serialization.gson;

public class JsonPipe extends ChannelOwner implements Transport {
  private final Queue<JsonObject> incoming = new LinkedList<>();
  private boolean isClosed;
  private String closeReason = "Browser has been closed";

  final JsonPipeChannel channel = new JsonPipeChannel(this);

  JsonPipe(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  @Override
  public void send(JsonObject message) {
    checkIfClosed();
    JsonPipeSendParams params = new JsonPipeSendParams();
    params.message = message;
    channel.send(params, NO_TIMEOUT);
  }

  @Override
  public JsonObject poll(Duration timeout) {
    Instant start = Instant.now();
    return runUntil(() -> {}, new Waitable<JsonObject>() {
      JsonObject message;
      @Override
      public boolean isDone() {
        if (!incoming.isEmpty()) {
          message = incoming.remove();
          return true;
        }
        checkIfClosed();
        if (Duration.between(start, Instant.now()).compareTo(timeout) > 0) {
          return true;
        }
        return false;
      }

      @Override
      public JsonObject get() {
        return message;
      }

      @Override
      public void dispose() {
      }
    });
  }

  @Override
  public void close() throws IOException {
    if (!isClosed) {
      channel.close(NO_TIMEOUT);
    }
  }

  String closeReason() {
    return closeReason;
  }

  void onClose(Consumer<JsonPipe> handler) {
    on(Events.JsonPipe.CLOSE, handler);
  }

  void offClose(Consumer<JsonPipe> handler) {
    off(Events.JsonPipe.CLOSE, handler);
  }


  @Override
  protected void handleEvent(String event, JsonObject params) {
    if ("message".equals(event)) {
      incoming.add(params.get("message").getAsJsonObject());
    } else if ("closed".equals(event)) {
      isClosed = true;
      if (params.has("reason")) {
        String reason = params.get("reason").getAsString();
        if (reason.trim().length() > 0) {
          closeReason = reason;
        }
      }
      emit(Events.JsonPipe.CLOSE, this);
    }
  }

  private void checkIfClosed() {
    if (isClosed) {
      throw new PlaywrightException(closeReason);
    }
  }
}
