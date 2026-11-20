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

import java.util.ArrayList;
import java.util.List;

import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.Utils.toProtocol;

public class DebuggerImpl extends ChannelOwner implements Debugger {
  private DebuggerPausedDetails pausedDetails;
  // java: handlers of onPausedStateChanged(Runnable); upstream emits Events.Debugger.PausedStateChanged. apiCallsUpdated has no Java API.
  private final List<Runnable> pausedStateChangedHandlers = new ArrayList<>();

  final DebuggerChannel channel = new DebuggerChannel(this);

  DebuggerImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  // java: upstream subscribes in the constructor with this._channel.on(...); the Java channel dispatches events here.
  @Override
  protected void handleEvent(String event, JsonObject params) {
    if ("pausedStateChanged".equals(event)) {
      if (params.has("pausedDetails") && !params.get("pausedDetails").isJsonNull()) {
        pausedDetails = gson().fromJson(params.get("pausedDetails"), DebuggerPausedDetails.class);
      } else {
        pausedDetails = null;
      }
      for (Runnable handler : new ArrayList<>(pausedStateChangedHandlers)) {
        handler.run();
      }
    }
  }

  // java: _enable has no Java API.

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

  @Override
  public void onPausedStateChanged(Runnable handler) {
    pausedStateChangedHandlers.add(handler);
  }

  @Override
  public void offPausedStateChanged(Runnable handler) {
    pausedStateChangedHandlers.remove(handler);
  }
}
