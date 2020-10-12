/**
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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class DownloadImpl extends ChannelOwner {
  public DownloadImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  public String url() {
    return initializer.get("url").getAsString();
  }

  public String suggestedFilename() {
    return initializer.get("suggestedFilename").getAsString();
  }

  public String path() {
    JsonObject params = new JsonObject();
    JsonElement result = sendMessage("path", params);
    return result.getAsJsonObject().get("path").getAsString();
  }
}
