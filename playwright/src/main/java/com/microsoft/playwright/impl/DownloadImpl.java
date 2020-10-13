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
import com.microsoft.playwright.Download;

public class DownloadImpl extends ChannelOwner implements Download {
  public DownloadImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  @Override
  public String url() {
    return initializer.get("url").getAsString();
  }

  @Override
  public String suggestedFilename() {
    return initializer.get("suggestedFilename").getAsString();
  }

  @Override
  public Readable createReadStream() {
    return null;
  }

  @Override
  public void delete() {
    sendMessage("delete", new JsonObject());
  }

  @Override
  public String failure() {
    JsonObject result = sendMessage("failure", new JsonObject()).getAsJsonObject();
    if (result.has("error")) {
      return result.get("error").getAsString();
    }
    return null;
  }

  @Override
  public String path() {
    JsonObject params = new JsonObject();
    JsonElement result = sendMessage("path", params);
    return result.getAsJsonObject().get("path").getAsString();
  }

  @Override
  public void saveAs(String path) {
  }
}
