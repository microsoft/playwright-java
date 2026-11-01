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

import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.Serialization.gson;

public class LocalUtils extends ChannelOwner {
  final LocalUtilsChannel channel = new LocalUtilsChannel(this);

  LocalUtils(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  JsonArray deviceDescriptors() {
    return initializer.getAsJsonArray("deviceDescriptors");
  }

  void zip(Path zipFile, List<NameValue> entries, String stacksId, boolean appendMode, boolean includeSources, List<String> additionalSources) {
    LocalUtilsZipParams params = new LocalUtilsZipParams();
    params.zipFile = zipFile.toString();
    params.entries = entries;
    params.mode = appendMode ? "append" : "write";
    params.stacksId = stacksId;
    params.includeSources = includeSources;
    if (!additionalSources.isEmpty()) {
      params.additionalSources = additionalSources;
    }
    channel.zip(params, NO_TIMEOUT);
  }

  void traceDiscarded(String stacksId) {
    LocalUtilsTraceDiscardedParams params = new LocalUtilsTraceDiscardedParams();
    params.stacksId = stacksId;
    channel.traceDiscarded(params, NO_TIMEOUT);
  }

  String tracingStarted(String tracesDir, String traceName) {
    LocalUtilsTracingStartedParams params = new LocalUtilsTracingStartedParams();
    if (tracesDir != null) {
      params.tracesDir = "";
    }
    params.traceName = traceName;
    return connection.localUtils().channel.tracingStarted(params, NO_TIMEOUT).stacksId;
  }

  public Pattern globToRegex(String glob, String baseURL, boolean webSocketUrl) {
    LocalUtilsGlobToRegexParams params = new LocalUtilsGlobToRegexParams();
    params.glob = glob;
    params.baseURL = baseURL;
    params.webSocketUrl = webSocketUrl;
    String regex = connection.localUtils().channel.globToRegex(params, NO_TIMEOUT).regex;
    return Pattern.compile(regex);
  }
}
