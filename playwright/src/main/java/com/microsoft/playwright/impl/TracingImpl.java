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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.microsoft.playwright.Tracing;

import java.nio.file.Path;

import static com.microsoft.playwright.impl.Serialization.gson;

class TracingImpl extends ChannelOwner implements Tracing {
  private boolean includeSources;
  private Path tracesDir;
  private boolean isTracing;
  private String stacksId;


  TracingImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  private void stopChunkImpl(Path path) {
    if (isTracing) {
      isTracing = false;
      connection.setIsTracing(false);
    }
    JsonObject params = new JsonObject();

    // Not interested in artifacts.
    if (path == null) {
      params.addProperty("mode", "discard");
      sendMessage("tracingStopChunk", params);
      if (stacksId != null) {
        connection.localUtils().traceDiscarded(stacksId);
      }
      return;
    }

    boolean isLocal = !connection.isRemote;
    if (isLocal) {
      params.addProperty("mode", "entries");
      JsonObject json = sendMessage("tracingStopChunk", params).getAsJsonObject();
      JsonArray entries = json.getAsJsonArray("entries");
      connection.localUtils.zip(path, entries, stacksId, false, includeSources);
      return;
    }

    params.addProperty("mode", "archive");
    JsonObject json = sendMessage("tracingStopChunk", params).getAsJsonObject();
    // The artifact may be missing if the browser closed while stopping tracing.
    if (!json.has("artifact")) {
      if (stacksId != null) {
        connection.localUtils().traceDiscarded(stacksId);
      }
      return;
    }
    ArtifactImpl artifact = connection.getExistingObject(json.getAsJsonObject("artifact").get("guid").getAsString());
    artifact.saveAs(path);
    artifact.delete();

    connection.localUtils.zip(path, new JsonArray(), stacksId, true, includeSources);
  }

  @Override
  public void startChunk(StartChunkOptions options) {
    if (options == null) {
      options = new StartChunkOptions();
    }
    tracingStartChunk(options.name, options.title);
  }

  private void tracingStartChunk(String name, String title) {
    JsonObject params = new JsonObject();
    if (name != null) {
      params.addProperty("name", name);
    }
    if (title != null) {
      params.addProperty("title", title);
    }
    JsonObject result = sendMessage("tracingStartChunk", params).getAsJsonObject();
    startCollectingStacks(result.get("traceName").getAsString());
  }

  private void startCollectingStacks(String traceName) {
    if (!isTracing) {
      isTracing = true;
      connection.setIsTracing(true);
    }
    stacksId = connection.localUtils().tracingStarted(tracesDir == null ? null : tracesDir.toString(), traceName);
  }

  @Override
  public void start(StartOptions options) {
    if (options == null) {
      options = new StartOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    includeSources = options.sources != null && options.sources;
    if (includeSources) {
      params.addProperty("sources", true);
    }
    sendMessage("tracingStart", params);
    tracingStartChunk(options.name, options.title);
  }

  @Override
  public void stop(StopOptions options) {
    stopChunkImpl(options == null ? null : options.path);
    sendMessage("tracingStop");
  }

  @Override
  public void stopChunk(StopChunkOptions options) {
    stopChunkImpl(options == null ? null : options.path);
  }

  void setTracesDir(Path tracesDir) {
    this.tracesDir = tracesDir;
  }
}
