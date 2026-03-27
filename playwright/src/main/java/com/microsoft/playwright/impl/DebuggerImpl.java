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
import com.microsoft.playwright.Debugger;
import com.microsoft.playwright.options.Location;
import com.microsoft.playwright.options.PausedDetails;

import java.util.ArrayList;
import java.util.List;

import static com.microsoft.playwright.impl.Serialization.gson;

class DebuggerImpl extends ChannelOwner implements Debugger {
  private final List<Runnable> pausedStateChangedHandlers = new ArrayList<>();
  private PausedDetails pausedDetails;

  DebuggerImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  @Override
  protected void handleEvent(String event, JsonObject params) {
    if ("pausedStateChanged".equals(event)) {
      if (params.has("pausedDetails") && !params.get("pausedDetails").isJsonNull()) {
        pausedDetails = gson().fromJson(params.get("pausedDetails"), PausedDetails.class);
      } else {
        pausedDetails = null;
      }
      for (Runnable handler : new ArrayList<>(pausedStateChangedHandlers)) {
        handler.run();
      }
    }
  }

  @Override
  public void onPausedStateChanged(Runnable handler) {
    pausedStateChangedHandlers.add(handler);
  }

  @Override
  public void offPausedStateChanged(Runnable handler) {
    pausedStateChangedHandlers.remove(handler);
  }

  @Override
  public PausedDetails pausedDetails() {
    return pausedDetails;
  }

  @Override
  public void requestPause() {
    sendMessage("requestPause", new JsonObject(), NO_TIMEOUT);
  }

  @Override
  public void resume() {
    sendMessage("resume", new JsonObject(), NO_TIMEOUT);
  }

  @Override
  public void next() {
    sendMessage("next", new JsonObject(), NO_TIMEOUT);
  }

  @Override
  public void runTo(Location location) {
    JsonObject params = gson().toJsonTree(location).getAsJsonObject();
    sendMessage("runTo", params, NO_TIMEOUT);
  }
}
