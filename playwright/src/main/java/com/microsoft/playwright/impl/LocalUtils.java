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
  LocalUtils(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  JsonArray deviceDescriptors() {
    return initializer.getAsJsonArray("deviceDescriptors");
  }

  void zip(Path zipFile, JsonArray entries, String stacksId, boolean appendMode, boolean includeSources) {
    JsonObject params = new JsonObject();
    params.addProperty("zipFile", zipFile.toString());
    params.add("entries", entries);
    params.addProperty("mode", appendMode ? "append" : "write");
    params.addProperty("stacksId", stacksId);
    params.addProperty("includeSources", includeSources);
    sendMessage("zip", params);
  }

  void traceDiscarded(String stacksId) {
    JsonObject params = new JsonObject();
    params.addProperty("stacksId", stacksId);
    sendMessage("traceDiscarded", params);
  }

  String tracingStarted(String tracesDir, String traceName) {
    JsonObject params = new JsonObject();
    if (tracesDir != null) {
      params.addProperty("tracesDir", "");
    }
    params.addProperty("traceName", traceName);
    JsonObject json = connection.localUtils().sendMessage("tracingStarted", params).getAsJsonObject();
    return json.get("stacksId").getAsString();
  }

  public Pattern globToRegex(String glob, String baseURL, boolean webSocketUrl) {
    JsonObject params = new JsonObject();
    params.addProperty("glob", glob);
    if (baseURL != null) {
      params.addProperty("baseURL", baseURL);
    }
    params.addProperty("webSocketUrl", webSocketUrl);
    JsonObject json = connection.localUtils().sendMessage("globToRegex", params).getAsJsonObject();
    String regex = json.get("regex").getAsString();
    return Pattern.compile(regex);
  }
}
