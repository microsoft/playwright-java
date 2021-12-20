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

class TracingImpl implements Tracing {
  private final BrowserContextImpl context;
  private boolean includeSources;

  TracingImpl(BrowserContextImpl context) {
    this.context = context;
  }

  private void stopChunkImpl(Path path) {
    boolean isRemote = context.browser() != null && context.browser().isRemote;
    JsonObject params = new JsonObject();
    String mode = "doNotSave";
    if (path != null) {
      if (isRemote) {
        mode = "compressTrace";
      } else {
        mode = "compressTraceAndSources";
      }
    }
    params.addProperty("mode", mode);
    JsonObject json = context.sendMessage("tracingStopChunk", params).getAsJsonObject();
    if (!json.has("artifact")) {
      return;
    }
    ArtifactImpl artifact = context.connection.getExistingObject(json.getAsJsonObject("artifact").get("guid").getAsString());
    // In case of CDP connection browser is null but since the connection is established by
    // the driver it is safe to consider the artifact local.
    if (isRemote) {
      artifact.isRemote = true;
    }
    artifact.saveAs(path);
    artifact.delete();

    // Add local sources to the remote trace if necessary.
    if (isRemote && json.has("sourceEntries")) {
      JsonArray entries = json.getAsJsonArray("sourceEntries");
      context.localUtils.zip(path, entries);
    }
  }

  @Override
  public void start(StartOptions options) {
    context.withLogging("Tracing.start", () -> startImpl(options));
  }

  @Override
  public void startChunk(StartChunkOptions options) {
    context.withLogging("Tracing.startChunk", () -> {
      startChunkImpl(options);
    });
  }

  private void startChunkImpl(StartChunkOptions options) {
    if (options == null) {
      options = new StartChunkOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    context.sendMessage("tracingStartChunk", params);
  }

  private void startImpl(StartOptions options) {
    if (options == null) {
      options = new StartOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    includeSources = options.sources != null;
    if (includeSources) {
      if (!context.connection.isCollectingStacks()) {
        throw new PlaywrightException("Source root directories must be provided to enable source collection");
      }
      params.addProperty("sources", true);
    }
    context.sendMessage("tracingStart", params);
    context.sendMessage("tracingStartChunk");
  }

  @Override
  public void stop(StopOptions options) {
    context.withLogging("Tracing.stop", () -> {
      stopChunkImpl(options == null ? null : options.path);
      context.sendMessage("tracingStop");
    });
  }

  @Override
  public void stopChunk(StopChunkOptions options) {
    context.withLogging("Tracing.stopChunk", () -> {
      stopChunkImpl(options == null ? null : options.path);
    });
  }
}
