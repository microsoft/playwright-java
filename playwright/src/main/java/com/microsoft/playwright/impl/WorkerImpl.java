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
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Worker;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.microsoft.playwright.impl.Serialization.*;

class WorkerImpl extends ChannelOwner implements Worker {
  private final ListenerCollection<EventType> listeners = new ListenerCollection<>();
  PageImpl page;

  enum EventType {
    CLOSE,
  }

  WorkerImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  @Override
  public void onClose(Consumer<Worker> handler) {
    listeners.add(EventType.CLOSE, handler);
  }

  @Override
  public void offClose(Consumer<Worker> handler) {
    listeners.remove(EventType.CLOSE, handler);
  }

  private <T> T waitForEventWithTimeout(EventType eventType, Runnable code, Double timeout) {
    List<Waitable<T>> waitables = new ArrayList<>();
    waitables.add(new WaitableEvent<>(listeners, eventType));
    waitables.add(page.createWaitForCloseHelper());
    waitables.add(page.createWaitableTimeout(timeout));
    return runUntil(code, new WaitableRace<>(waitables));
  }

  @Override
  public Worker waitForClose(WaitForCloseOptions options, Runnable code) {
    return withWaitLogging("Worker.waitForClose", logger -> waitForCloseImpl(options, code));
  }

  private Worker waitForCloseImpl(WaitForCloseOptions options, Runnable code) {
    if (options == null) {
      options = new WaitForCloseOptions();
    }
    return waitForEventWithTimeout(EventType.CLOSE, code, options.timeout);
  }

  @Override
  public Object evaluate(String pageFunction, Object arg) {
    return withLogging("Worker.evaluate", () -> {
      JsonObject params = new JsonObject();
      params.addProperty("expression", pageFunction);
      params.add("arg", gson().toJsonTree(serializeArgument(arg)));
      JsonElement json = sendMessage("evaluateExpression", params);
      SerializedValue value = gson().fromJson(json.getAsJsonObject().get("value"), SerializedValue.class);
      return deserialize(value);
    });
  }

  @Override
  public JSHandle evaluateHandle(String pageFunction, Object arg) {
    return withLogging("Worker.evaluateHandle", () -> {
      JsonObject params = new JsonObject();
      params.addProperty("expression", pageFunction);
      params.add("arg", gson().toJsonTree(serializeArgument(arg)));
      JsonElement json = sendMessage("evaluateExpressionHandle", params);
      return connection.getExistingObject(json.getAsJsonObject().getAsJsonObject("handle").get("guid").getAsString());
    });
  }

  @Override
  public String url() {
    return initializer.get("url").getAsString();
  }

  @Override
  protected void handleEvent(String event, JsonObject params) {
    if ("close".equals(event)) {
      if (page != null) {
        page.workers.remove(this);
      }
      listeners.notify(EventType.CLOSE, this);
    }
  }
}
