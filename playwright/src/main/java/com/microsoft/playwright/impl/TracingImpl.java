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

// Port of tracing.ts.
package com.microsoft.playwright.impl;

import com.google.gson.JsonObject;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.impl.protocol.*;
import com.microsoft.playwright.options.HarContentPolicy;
import com.microsoft.playwright.options.HarMode;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class TracingImpl extends ChannelOwner implements Tracing {
  private boolean includeSources = false;
  private final Set<String> additionalSources = new LinkedHashSet<>();
  private boolean isLive = false;
  // java: a Path, as the tracesDir launch option of the Java API.
  Path tracesDir;
  private String stacksId;
  private boolean isTracing = false;
  private String harId;
  private final Map<String, HarRecorder> harRecorders = new LinkedHashMap<>();

  final TracingChannel channel = new TracingChannel(this);

  TracingImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  @Override
  public AutoCloseable start(StartOptions options) {
    StartOptions o = options == null ? new StartOptions() : options;
    return wrapApiCall(() -> {
      includeSources = o.sources != null && o.sources;
      isLive = o.live != null && o.live;
      TracingTracingStartParams params = new TracingTracingStartParams();
      params.name = o.name;
      // java: the snapshots object of upstream is three options, snapshots (dom), ariaSnapshots and screenSnapshots.
      params.snapshotDom = o.snapshots;
      params.snapshotAria = o.ariaSnapshots;
      params.snapshotScreen = o.screenSnapshots;
      params.screencast = o.screenshots;
      params.live = o.live;
      channel.tracingStart(params, NO_TIMEOUT);
      TracingTracingStartChunkParams chunkParams = new TracingTracingStartChunkParams();
      chunkParams.name = o.name;
      chunkParams.title = o.title;
      String traceName = channel.tracingStartChunk(chunkParams, NO_TIMEOUT).traceName;
      startCollectingStacks(traceName, isLive);
      return new DisposableStub(() -> stop());
    }, null, false);
  }

  @Override
  public AutoCloseable startChunk(StartChunkOptions options) {
    StartChunkOptions o = options == null ? new StartChunkOptions() : options;
    return wrapApiCall(() -> {
      TracingTracingStartChunkParams params = new TracingTracingStartChunkParams();
      params.name = o.name;
      params.title = o.title;
      String traceName = channel.tracingStartChunk(params, NO_TIMEOUT).traceName;
      startCollectingStacks(traceName, isLive);
      return new DisposableStub(() -> stopChunk());
    }, null, false);
  }

  @Override
  public AutoCloseable group(String name, GroupOptions options) {
    if (options == null) {
      options = new GroupOptions();
    }
    if (options.location != null) {
      additionalSources.add(options.location.file);
    }
    TracingTracingGroupParams params = new TracingTracingGroupParams();
    params.name = name;
    if (options.location != null) {
      params.location = new TracingTracingGroupParams.TracingTracingGroupParamsLocation();
      params.location.file = options.location.file;
      params.location.line = options.location.line;
      params.location.column = options.location.column;
    }
    channel.tracingGroup(params, NO_TIMEOUT);
    return new DisposableStub(() -> groupEnd());
  }

  @Override
  public void groupEnd() {
    channel.tracingGroupEnd(new TracingTracingGroupEndParams(), NO_TIMEOUT);
  }

  private void startCollectingStacks(String traceName, boolean live) {
    if (!isTracing) {
      isTracing = true;
      connection.setIsTracing(true);
    }
    LocalUtils localUtils = connection.localUtils();
    if (localUtils == null) {
      stacksId = null;
      return;
    }
    // java: LocalUtils.tracingStarted of the seed has no live flag and drops tracesDir, the params go to the channel as upstream passes them.
    LocalUtilsTracingStartedParams params = new LocalUtilsTracingStartedParams();
    params.tracesDir = tracesDir == null ? null : tracesDir.toString();
    params.traceName = traceName;
    params.live = live;
    LocalUtilsTracingStartedResult result = localUtils.channel.tracingStarted(params, NO_TIMEOUT);
    stacksId = result == null ? null : result.stacksId;
  }

  @Override
  public void stopChunk(StopChunkOptions options) {
    Path path = options == null ? null : options.path;
    wrapApiCall(() -> {
      doStopChunk(path);
    }, null, false);
  }

  @Override
  public void stop(StopOptions options) {
    Path path = options == null ? null : options.path;
    wrapApiCall(() -> {
      RuntimeException error = null;
      try {
        doStopChunk(path);
      } catch (RuntimeException e) {
        error = e;
      }
      channel.tracingStop(new TracingTracingStopParams(), NO_TIMEOUT);
      if (error != null) {
        throw error;
      }
    }, null, false);
  }

  @Override
  public AutoCloseable startHar(Path path, StartHarOptions options) {
    StartHarOptions o = options == null ? new StartHarOptions() : options;
    return wrapApiCall(() -> {
      if (harId != null) {
        throw new PlaywrightException("HAR recording has already been started");
      }
      // java: StartHarOptions has no resourcesDir, so there is no check of resourcesDir with a .zip har file.
      HarContentPolicy defaultContent = isZip(path) ? HarContentPolicy.ATTACH : HarContentPolicy.EMBED;
      harId = recordIntoHAR(path, null,
        o.urlFilter,
        o.content != null ? o.content : defaultContent,
        o.mode != null ? o.mode : HarMode.FULL,
        null);
      return new DisposableStub(() -> stopHar());
    }, null, false);
  }

  @Override
  public void stopHar() {
    wrapApiCall(() -> {
      String harId = this.harId;
      if (harId == null) {
        throw new PlaywrightException("HAR recording has not been started");
      }
      this.harId = null;
      exportHAR(harId);
    }, null, false);
  }

  // java: the options object of upstream is positional, url is a String glob or a Pattern.
  String recordIntoHAR(Path har, PageImpl page, Object url, HarContentPolicy updateContent, HarMode updateMode, Path resourcesDir) {
    boolean isZip = isZip(har);
    TracingHarStartParams params = new TracingHarStartParams();
    params.page = page;
    params.options = new RecordHarOptions();
    params.options.content = (updateContent != null ? updateContent : HarContentPolicy.ATTACH).name().toLowerCase();
    params.options.urlGlob = url instanceof String ? (String) url : null;
    params.options.urlRegexSource = url instanceof Pattern ? ((Pattern) url).pattern() : null;
    params.options.urlRegexFlags = url instanceof Pattern ? Utils.toJsRegexFlags((Pattern) url) : null;
    params.options.mode = (updateMode != null ? updateMode : HarMode.MINIMAL).name().toLowerCase();
    params.options.harPath = isZip ? null : har.toString();
    params.options.resourcesDir = resourcesDir == null ? null : resourcesDir.toString();
    String harId = channel.harStart(params, NO_TIMEOUT).harId;
    harRecorders.put(harId, new HarRecorder(har, resourcesDir));
    return harId;
  }

  void exportHAR(String harId) {
    HarRecorder harParams = harRecorders.get(harId);
    if (harParams == null) {
      return;
    }
    harRecorders.remove(harId);
    boolean isLocal = !connection.isRemote();
    boolean isZip = isZip(harParams.path);

    if (isLocal) {
      TracingHarExportParams params = new TracingHarExportParams();
      params.harId = harId;
      params.mode = "entries";
      List<NameValue> entries = channel.harExport(params, NO_TIMEOUT).entries;
      if (!isZip) {
        // Server wrote HAR and resources to the user's chosen paths.
        return;
      }
      LocalUtils localUtils = connection.localUtils();
      if (localUtils == null) {
        throw new PlaywrightException("Cannot save zipped HAR in thin clients");
      }
      localUtils.zip(harParams.path, entries, null, false, false, Collections.<String>emptyList());
      return;
    }

    TracingHarExportParams params = new TracingHarExportParams();
    params.harId = harId;
    params.mode = "archive";
    ArtifactImpl artifact = channel.harExport(params, NO_TIMEOUT).artifact;
    if (isZip) {
      artifact.saveAs(harParams.path);
      artifact.delete();
      return;
    }
    LocalUtils localUtils = connection.localUtils();
    if (localUtils == null) {
      throw new PlaywrightException("Uncompressed har is not supported in thin clients");
    }
    artifact.saveAs(Paths.get(harParams.path + ".tmp"));
    LocalUtilsHarUnzipParams unzipParams = new LocalUtilsHarUnzipParams();
    unzipParams.zipFile = harParams.path + ".tmp";
    unzipParams.harFile = harParams.path.toString();
    unzipParams.resourcesDir = harParams.resourcesDir == null ? null : harParams.resourcesDir.toString();
    // java: the seed LocalUtils has no harUnzip wrapper.
    localUtils.channel.harUnzip(unzipParams, NO_TIMEOUT);
    artifact.delete();
  }

  void exportAllHars() {
    wrapApiCall(() -> {
      for (String harId : new ArrayList<>(harRecorders.keySet())) {
        exportHAR(harId);
      }
    }, null, true);
  }

  private void doStopChunk(Path filePath) {
    resetStackCounter();

    List<String> additionalSources = new ArrayList<>(this.additionalSources);
    this.additionalSources.clear();
    String stacksId = this.stacksId;
    this.stacksId = null;

    try {
      saveChunk(filePath, stacksId, additionalSources);
    } catch (RuntimeException error) {
      // Release the stack session even on failure, otherwise later traces keep appending to it.
      if (stacksId != null && connection.localUtils() != null) {
        try {
          connection.localUtils().traceDiscarded(stacksId);
        } catch (PlaywrightException e) {
        }
      }
      throw error;
    }
  }

  private void saveChunk(Path filePath, String stacksId, List<String> additionalSources) {
    if (filePath == null) {
      // Not interested in artifacts.
      TracingTracingStopChunkParams params = new TracingTracingStopChunkParams();
      params.mode = "discard";
      channel.tracingStopChunk(params, NO_TIMEOUT);
      if (stacksId != null) {
        connection.localUtils().traceDiscarded(stacksId);
      }
      return;
    }

    LocalUtils localUtils = connection.localUtils();
    if (localUtils == null) {
      throw new PlaywrightException("Cannot save trace in thin clients");
    }

    boolean isLocal = !connection.isRemote();

    if (isLocal) {
      TracingTracingStopChunkParams params = new TracingTracingStopChunkParams();
      params.mode = "entries";
      TracingTracingStopChunkResult result = channel.tracingStopChunk(params, NO_TIMEOUT);
      localUtils.zip(filePath, result.entries, stacksId, false, includeSources, additionalSources);
      return;
    }

    TracingTracingStopChunkParams params = new TracingTracingStopChunkParams();
    params.mode = "archive";
    TracingTracingStopChunkResult result = channel.tracingStopChunk(params, NO_TIMEOUT);

    // The artifact may be missing if the browser closed while stopping tracing.
    if (result.artifact == null) {
      if (stacksId != null) {
        localUtils.traceDiscarded(stacksId);
      }
      return;
    }

    // Save trace to the final local file.
    ArtifactImpl artifact = result.artifact;
    try {
      artifact.saveAs(filePath);
    } catch (RuntimeException error) {
      // Delete the artifact best-effort, the save error is the one to surface.
      try {
        artifact.delete();
      } catch (PlaywrightException e) {
      }
      throw error;
    }
    artifact.delete();

    localUtils.zip(filePath, new ArrayList<>(), stacksId, true, includeSources, additionalSources);
  }

  void resetStackCounter() {
    if (isTracing) {
      isTracing = false;
      connection.setIsTracing(false);
    }
  }

  // java: the value type of harRecorders and the /\.zip$/i test.
  static class HarRecorder {
    final Path path;
    final Path resourcesDir;

    HarRecorder(Path path, Path resourcesDir) {
      this.path = path;
      this.resourcesDir = resourcesDir;
    }
  }

  private static boolean isZip(Path path) {
    return path.toString().toLowerCase().endsWith(".zip");
  }
}
