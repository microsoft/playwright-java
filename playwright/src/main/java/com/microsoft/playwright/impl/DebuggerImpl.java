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

// Port of debugger.ts.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.impl.protocol.*;
import com.google.gson.JsonObject;
import com.microsoft.playwright.Debugger;
import com.microsoft.playwright.options.Location;
import com.microsoft.playwright.options.DebuggerPausedDetails;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.Utils.toProtocol;

public class DebuggerImpl extends ChannelOwner implements Debugger {
  private DebuggerPausedDetails pausedDetails;

  final DebuggerChannel channel = new DebuggerChannel(this);

  // java: from is not ported, generated results already hold the impl object.

  DebuggerImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  // java: upstream subscribes in the constructor with this._channel.on(...); the Java channel dispatches events here.
  @Override
  protected void handleEvent(String event, JsonObject params) {
    switch (event) {
      case "pausedStateChanged": {
        if (params.has("pausedDetails") && !params.get("pausedDetails").isJsonNull()) {
          pausedDetails = gson().fromJson(params.get("pausedDetails"), DebuggerPausedDetails.class);
        } else {
          pausedDetails = null;
        }
        emit(Events.Debugger.PAUSED_STATE_CHANGED, null);
        break;
      }
      case "apiCallsUpdated": {
        DebuggerApiCallsUpdatedEvent e = connection.protocolGson().fromJson(params, DebuggerApiCallsUpdatedEvent.class);
        emit(Events.Debugger.API_CALLS_UPDATED, e.apiCalls);
        break;
      }
    }
  }

  void enable() {
    channel.enable(NO_TIMEOUT);
  }

  @Override
  public void requestPause() {
    channel.requestPause(NO_TIMEOUT);
  }

  @Override
  public void resume() {
    channel.resume(NO_TIMEOUT);
  }

  @Override
  public void next() {
    channel.next(NO_TIMEOUT);
  }

  @Override
  public void runTo(Location location) {
    DebuggerRunToParams params = new DebuggerRunToParams();
    params.location = toProtocol(location, DebuggerRunToParams.DebuggerRunToParamsLocation.class);
    channel.runTo(params, NO_TIMEOUT);
  }

  @Override
  public DebuggerPausedDetails pausedDetails() {
    return pausedDetails;
  }

  // java: the Runnable handlers of the public API, each registered as a listener of Events.Debugger.PAUSED_STATE_CHANGED.
  private final List<SimpleEntry<Runnable, Consumer<Object>>> pausedStateChangedListeners = new ArrayList<>();

  @Override
  public void onPausedStateChanged(Runnable handler) {
    Consumer<Object> listener = unused -> handler.run();
    pausedStateChangedListeners.add(new SimpleEntry<>(handler, listener));
    on(Events.Debugger.PAUSED_STATE_CHANGED, listener);
  }

  @Override
  public void offPausedStateChanged(Runnable handler) {
    for (int i = 0; i < pausedStateChangedListeners.size(); i++) {
      if (pausedStateChangedListeners.get(i).getKey() == handler) {
        off(Events.Debugger.PAUSED_STATE_CHANGED, pausedStateChangedListeners.remove(i).getValue());
        return;
      }
    }
  }
}
