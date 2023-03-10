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

import static com.microsoft.playwright.impl.Serialization.gson;

class LocalUtils extends ChannelOwner {
  LocalUtils(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  void zip(Path zipFile, JsonArray entries, List<CallMetadata> metadata, boolean appendMode, boolean includeSources) {
    JsonObject params = new JsonObject();
    params.addProperty("zipFile", zipFile.toString());
    params.add("entries", entries);
    params.addProperty("mode", appendMode ? "append" : "write");
    params.add("metadata", gson().toJsonTree(metadata));
    params.addProperty("includeSources", includeSources);
    sendMessage("zip", params);
  }
}
