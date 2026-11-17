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
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.Utils.toProtocol;

public class TracingImpl extends ChannelOwner implements Tracing {
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


  final TracingChannel channel = new TracingChannel(this);

  TracingImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  void resetStackCounter() {
    if (isTracing) {
      isTracing = false;
      connection.setIsTracing(false);
    }
  }

  private void stopChunkImpl(Path path) {
    resetStackCounter();

    List<String> capturedAdditionalSources = new ArrayList<>(additionalSources);
    additionalSources.clear();
    String stacksId = this.stacksId;
    this.stacksId = null;

    try {
      saveChunk(path, stacksId, capturedAdditionalSources);
    } catch (RuntimeException e) {
      // Release the stack session even on failure, otherwise later traces keep appending to it.
      if (stacksId != null) {
        try {
          connection.localUtils().traceDiscarded(stacksId);
        } catch (RuntimeException ignored) {
        }
      }
      throw e;
    }
  }

  private void saveChunk(Path path, String stacksId, List<String> capturedAdditionalSources) {
    TracingTracingStopChunkParams params = new TracingTracingStopChunkParams();
    // Not interested in artifacts.
    if (path == null) {
      params.mode = "discard";
      channel.tracingStopChunk(params, NO_TIMEOUT);
      if (stacksId != null) {
        connection.localUtils().traceDiscarded(stacksId);
      }
      return;
    }

    boolean isLocal = !connection.isRemote;
    if (isLocal) {
      params.mode = "entries";
      TracingTracingStopChunkResult result = channel.tracingStopChunk(params, NO_TIMEOUT);
      connection.localUtils.zip(path, result.entries, stacksId, false, includeSources, capturedAdditionalSources);
      return;
    }

    params.mode = "archive";
    TracingTracingStopChunkResult json = channel.tracingStopChunk(params, NO_TIMEOUT);
    // The artifact may be missing if the browser closed while stopping tracing.
    if (json.artifact == null) {
      if (stacksId != null) {
        connection.localUtils().traceDiscarded(stacksId);
      }
      return;
    }
    ArtifactImpl artifact = json.artifact;
    try {
      artifact.saveAs(path);
    } catch (RuntimeException e) {
      // Delete the artifact best-effort, the save error is the one to surface.
      try {
        artifact.delete();
      } catch (RuntimeException ignored) {
      }
      throw e;
    }
    artifact.delete();

    connection.localUtils.zip(path, new ArrayList<>(), stacksId, true, includeSources, capturedAdditionalSources);
  }

  @Override
  public AutoCloseable startChunk(StartChunkOptions options) {
    if (options == null) {
      options = new StartChunkOptions();
    }
    tracingStartChunk(options.name, options.title);
    return new DisposableStub(this::stopChunk);
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
    TracingTracingGroupParams params = toProtocol(options, TracingTracingGroupParams.class);
    params.name = name;
    channel.tracingGroup(params, NO_TIMEOUT);
  }

  @Override
  public void groupEnd() {
    channel.tracingGroupEnd(NO_TIMEOUT);
  }

  private void tracingStartChunk(String name, String title) {
    TracingTracingStartChunkParams params = new TracingTracingStartChunkParams();
    params.name = name;
    params.title = title;
    TracingTracingStartChunkResult result = channel.tracingStartChunk(params, NO_TIMEOUT);
    startCollectingStacks(result.traceName);
  }

  private void startCollectingStacks(String traceName) {
    if (!isTracing) {
      isTracing = true;
      connection.setIsTracing(true);
    }
    stacksId = connection.localUtils().tracingStarted(tracesDir == null ? null : tracesDir.toString(), traceName);
  }

  @Override
  public AutoCloseable start(StartOptions options) {
    if (options == null) {
      options = new StartOptions();
    }
    includeSources = options.sources != null && options.sources;
    TracingTracingStartParams params = toProtocol(options, TracingTracingStartParams.class);
    params.snapshotDom = options.snapshots;
    params.snapshotAria = options.ariaSnapshots;
    params.snapshotScreen = options.screenSnapshots;
    params.screencast = options.screenshots;
    channel.tracingStart(params, NO_TIMEOUT);
    tracingStartChunk(options.name, options.title);
    return new DisposableStub(this::stop);
  }

  @Override
  public void stop(StopOptions options) {
    try {
      stopChunkImpl(options == null ? null : options.path);
    } finally {
      channel.tracingStop(NO_TIMEOUT);
    }
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
    boolean isZip = isZip(path);
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

  static boolean isZip(Path path) {
    return path.toString().toLowerCase().endsWith(".zip");
  }

  String recordIntoHar(PageImpl page, Path har, Object urlFilter, HarContentPolicy contentPolicy, HarMode mode, Path resourcesDir) {
    if (contentPolicy == null) {
      contentPolicy = HarContentPolicy.ATTACH;
    }
    if (mode == null) {
      mode = HarMode.MINIMAL;
    }

    TracingHarStartParams params = new TracingHarStartParams();
    params.page = page;
    // The old "zip" key is not part of the protocol anymore (the generated RecordHarOptions has no such field).
    RecordHarOptions recordHarArgs = new RecordHarOptions();
    recordHarArgs.content = contentPolicy.name().toLowerCase();
    recordHarArgs.mode = mode.name().toLowerCase();
    if (urlFilter instanceof String) {
      recordHarArgs.urlGlob = (String) urlFilter;
    } else if (urlFilter instanceof Pattern) {
      Pattern pattern = (Pattern) urlFilter;
      recordHarArgs.urlRegexSource = pattern.pattern();
      recordHarArgs.urlRegexFlags = Utils.toJsRegexFlags(pattern);
    }
    if (resourcesDir != null) {
      recordHarArgs.resourcesDir = resourcesDir.toString();
    }
    if (!isZip(har)) {
      recordHarArgs.harPath = har.toString();
    }

    params.options = recordHarArgs;
    String harId = channel.harStart(params, NO_TIMEOUT).harId;
    harRecorders.put(harId, new HarRecorder(har, contentPolicy));
    return harId;
  }

  void exportHar(String harId) {
    HarRecorder harParams = harRecorders.remove(harId);
    if (harParams == null) {
      return;
    }
    boolean isLocal = !connection.isRemote;
    boolean isZip = isZip(harParams.path);

    TracingHarExportParams params = new TracingHarExportParams();
    params.harId = harId;
    if (isLocal) {
      params.mode = "entries";
      TracingHarExportResult json = channel.harExport(params, NO_TIMEOUT);
      if (!isZip) {
        return;
      }
      connection.localUtils.zip(harParams.path, json.entries, null, false, false, java.util.Collections.emptyList());
      return;
    }

    params.mode = "archive";
    TracingHarExportResult json = channel.harExport(params, NO_TIMEOUT);
    ArtifactImpl artifact = json.artifact;
    if (isZip) {
      artifact.saveAs(harParams.path);
      artifact.delete();
      return;
    }
    String tmpPath = harParams.path + ".tmp";
    artifact.saveAs(Paths.get(tmpPath));
    LocalUtilsHarUnzipParams unzipParams = new LocalUtilsHarUnzipParams();
    unzipParams.zipFile = tmpPath;
    unzipParams.harFile = harParams.path.toString();
    connection.localUtils.channel.harUnzip(unzipParams, NO_TIMEOUT);
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
