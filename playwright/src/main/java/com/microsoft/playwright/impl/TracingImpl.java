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
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Tracing;

import java.nio.file.Path;

import static com.microsoft.playwright.impl.Serialization.gson;

class TracingImpl extends ChannelOwner implements Tracing {
  TracingImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  private void stopChunkImpl(Path path) {
    JsonObject params = new JsonObject();
    String mode = "doNotSave";
    if (path != null) {
      if (connection.isRemote) {
        mode = "compressTrace";
      } else {
        mode = "compressTraceAndSources";
      }
    }
    params.addProperty("mode", mode);
    JsonObject json = sendMessage("tracingStopChunk", params).getAsJsonObject();
    if (!json.has("artifact")) {
      return;
    }
    ArtifactImpl artifact = connection.getExistingObject(json.getAsJsonObject("artifact").get("guid").getAsString());
    artifact.saveAs(path);
    artifact.delete();

    // Add local sources to the remote trace if necessary.
    // In case of CDP connection since the connection is established by
    // the driver it is safe to consider the artifact local.
    if (connection.isRemote && json.has("sourceEntries")) {
      JsonArray entries = json.getAsJsonArray("sourceEntries");
      connection.localUtils.zip(path, entries);
    }
  }

  @Override
  public void start(StartOptions options) {
    withLogging("Tracing.start", () -> startImpl(options));
  }

  @Override
  public void startChunk(StartChunkOptions options) {
    withLogging("Tracing.startChunk", () -> {
      startChunkImpl(options);
    });
  }

  private void startChunkImpl(StartChunkOptions options) {
    if (options == null) {
      options = new StartChunkOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    sendMessage("tracingStartChunk", params);
  }

  private void startImpl(StartOptions options) {
    if (options == null) {
      options = new StartOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    boolean includeSources = options.sources != null && options.sources;
    if (includeSources) {
      if (!connection.isCollectingStacks()) {
        throw new PlaywrightException("Source root directory must be specified via PLAYWRIGHT_JAVA_SRC environment variable when source collection is enabled");
      }
      params.addProperty("sources", true);
    }
    sendMessage("tracingStart", params);
    sendMessage("tracingStartChunk");
  }

  @Override
  public void stop(StopOptions options) {
    withLogging("Tracing.stop", () -> {
      stopChunkImpl(options == null ? null : options.path);
      sendMessage("tracingStop");
    });
  }

  @Override
  public void stopChunk(StopChunkOptions options) {
    withLogging("Tracing.stopChunk", () -> {
      stopChunkImpl(options == null ? null : options.path);
    });
  }
}
