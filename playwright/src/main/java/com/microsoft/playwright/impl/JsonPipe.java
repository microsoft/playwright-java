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

import com.google.gson.JsonObject;
import com.microsoft.playwright.PlaywrightException;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;

import static com.microsoft.playwright.impl.Serialization.gson;

class JsonPipe extends ChannelOwner implements Transport {
  private final Queue<JsonObject> incoming = new LinkedList<>();
  private ListenerCollection<EventType> listeners = new ListenerCollection<>();
  private enum EventType { CLOSE }
  private boolean isClosed;

  JsonPipe(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  @Override
  public void send(JsonObject message) {
    checkIfClosed();
    JsonObject params = new JsonObject();
    params.add("message", message);
    sendMessage("send", params);
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
      sendMessage("close");
    }
  }

  void onClose(Consumer<JsonPipe> handler) {
    listeners.add(EventType.CLOSE, handler);
  }

  void offClose(Consumer<JsonPipe> handler) {
    listeners.remove(EventType.CLOSE, handler);
  }


  @Override
  protected void handleEvent(String event, JsonObject params) {
    if ("message".equals(event)) {
      incoming.add(params.get("message").getAsJsonObject());
    } else if ("closed".equals(event)) {
      isClosed = true;
      listeners.notify(EventType.CLOSE, this);
    }
  }

  private void checkIfClosed() {
    if (isClosed) {
      throw new PlaywrightException("Browser has been closed");
    }
  }
}
