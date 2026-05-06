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
import com.microsoft.playwright.options.HarContentPolicy;
import com.microsoft.playwright.options.HarMode;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.microsoft.playwright.impl.Serialization.addHarUrlFilter;
import static com.microsoft.playwright.impl.Serialization.gson;

class TracingImpl extends ChannelOwner implements Tracing {
  private boolean includeSources;
  private Path tracesDir;
  private boolean isTracing;
  private String stacksId;
  private final Set<String> additionalSources = new HashSet<>();
  final Map<String, HarRecorder> harRecorders = new HashMap<>();

  static class HarRecorder {
    final Path path;
    final HarContentPolicy contentPolicy;

    HarRecorder(Path har, HarContentPolicy policy) {
      this.path = har;
      this.contentPolicy = policy;
    }
  }


  TracingImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  private void stopChunkImpl(Path path) {
    if (isTracing) {
      isTracing = false;
      connection.setIsTracing(false);
    }
    JsonObject params = new JsonObject();

    List<String> capturedAdditionalSources = new ArrayList<>(additionalSources);
    additionalSources.clear();

    // Not interested in artifacts.
    if (path == null) {
      params.addProperty("mode", "discard");
      sendMessage("tracingStopChunk", params, NO_TIMEOUT);
      if (stacksId != null) {
        connection.localUtils().traceDiscarded(stacksId);
      }
      return;
    }

    boolean isLocal = !connection.isRemote;
    if (isLocal) {
      params.addProperty("mode", "entries");
      JsonObject json = sendMessage("tracingStopChunk", params, NO_TIMEOUT).getAsJsonObject();
      JsonArray entries = json.getAsJsonArray("entries");
      connection.localUtils.zip(path, entries, stacksId, false, includeSources, capturedAdditionalSources);
      return;
    }

    params.addProperty("mode", "archive");
    JsonObject json = sendMessage("tracingStopChunk", params, NO_TIMEOUT).getAsJsonObject();
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

    connection.localUtils.zip(path, new JsonArray(), stacksId, true, includeSources, capturedAdditionalSources);
  }

  @Override
  public void startChunk(StartChunkOptions options) {
    if (options == null) {
      options = new StartChunkOptions();
    }
    tracingStartChunk(options.name, options.title);
  }

  @Override
  public AutoCloseable group(String name, GroupOptions options) {
    groupImpl(name, options);
    return new DisposableStub(this::groupEnd);
  }

  private void groupImpl(String name, GroupOptions options) {
    if (options == null) {
      options = new GroupOptions();
    }
    if (options.location != null) {
      additionalSources.add(options.location.file);
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("name", name);
    sendMessage("tracingGroup", params, NO_TIMEOUT);
  }

  @Override
  public void groupEnd() {
    sendMessage("tracingGroupEnd");
  }

  private void tracingStartChunk(String name, String title) {
    JsonObject params = new JsonObject();
    if (name != null) {
      params.addProperty("name", name);
    }
    if (title != null) {
      params.addProperty("title", title);
    }
    JsonObject result = sendMessage("tracingStartChunk", params, NO_TIMEOUT).getAsJsonObject();
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
    sendMessage("tracingStart", params, NO_TIMEOUT);
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

  private String currentHarId;

  @Override
  public AutoCloseable startHar(Path path, StartHarOptions options) {
    if (currentHarId != null) {
      throw new PlaywrightException("HAR recording has already been started");
    }
    if (options == null) {
      options = new StartHarOptions();
    }
    boolean isZip = path.toString().endsWith(".zip");
    HarContentPolicy contentPolicy = options.content != null
      ? options.content
      : (isZip ? HarContentPolicy.ATTACH : HarContentPolicy.EMBED);
    HarMode mode = options.mode != null ? options.mode : HarMode.FULL;
    currentHarId = recordIntoHar(null, path, options.urlFilter, contentPolicy, mode, null);
    return new DisposableStub(this::stopHar);
  }

  @Override
  public void stopHar() {
    if (currentHarId == null) {
      throw new PlaywrightException("HAR recording has not been started");
    }
    String harId = currentHarId;
    currentHarId = null;
    exportHar(harId);
  }

  String recordIntoHar(PageImpl page, Path har, Object urlFilter, HarContentPolicy contentPolicy, HarMode mode, Path resourcesDir) {
    if (contentPolicy == null) {
      contentPolicy = HarContentPolicy.ATTACH;
    }
    if (mode == null) {
      mode = HarMode.MINIMAL;
    }

    JsonObject params = new JsonObject();
    if (page != null) {
      params.add("page", page.toProtocolRef());
    }
    JsonObject recordHarArgs = new JsonObject();
    recordHarArgs.addProperty("zip", har.toString().endsWith(".zip"));
    recordHarArgs.addProperty("content", contentPolicy.name().toLowerCase());
    recordHarArgs.addProperty("mode", mode.name().toLowerCase());
    addHarUrlFilter(recordHarArgs, urlFilter);
    if (resourcesDir != null) {
      recordHarArgs.addProperty("resourcesDir", resourcesDir.toString());
    }
    if (!har.toString().endsWith(".zip")) {
      recordHarArgs.addProperty("harPath", har.toString());
    }

    params.add("options", recordHarArgs);
    JsonObject json = sendMessage("harStart", params, NO_TIMEOUT).getAsJsonObject();
    String harId = json.get("harId").getAsString();
    harRecorders.put(harId, new HarRecorder(har, contentPolicy));
    return harId;
  }

  void exportHar(String harId) {
    HarRecorder harParams = harRecorders.remove(harId);
    if (harParams == null) {
      return;
    }
    boolean isLocal = !connection.isRemote;
    boolean isZip = harParams.path.toString().endsWith(".zip");

    JsonObject params = new JsonObject();
    params.addProperty("harId", harId);
    if (isLocal) {
      params.addProperty("mode", "entries");
      JsonObject json = sendMessage("harExport", params, NO_TIMEOUT).getAsJsonObject();
      if (!isZip) {
        return;
      }
      JsonArray entries = json.getAsJsonArray("entries");
      connection.localUtils.zip(harParams.path, entries, null, false, false, java.util.Collections.emptyList());
      return;
    }

    params.addProperty("mode", "archive");
    JsonObject json = sendMessage("harExport", params, NO_TIMEOUT).getAsJsonObject();
    ArtifactImpl artifact = connection.getExistingObject(json.getAsJsonObject("artifact").get("guid").getAsString());
    if (isZip) {
      artifact.saveAs(harParams.path);
      artifact.delete();
      return;
    }
    String tmpPath = harParams.path + ".tmp";
    artifact.saveAs(Paths.get(tmpPath));
    JsonObject unzipParams = new JsonObject();
    unzipParams.addProperty("zipFile", tmpPath);
    unzipParams.addProperty("harFile", harParams.path.toString());
    connection.localUtils.sendMessage("harUnzip", unzipParams, NO_TIMEOUT);
    artifact.delete();
  }

  void exportAllHars() {
    for (String harId : new ArrayList<>(harRecorders.keySet())) {
      exportHar(harId);
    }
  }

  void setTracesDir(Path tracesDir) {
    this.tracesDir = tracesDir;
  }
}
