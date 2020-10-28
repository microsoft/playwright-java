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

import com.google.gson.JsonObject;
import com.microsoft.playwright.Download;

import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;

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
  public InputStream createReadStream() {
    JsonObject result = sendMessage("stream").getAsJsonObject();
    if (!result.has("stream")) {
      return null;
    }
    Stream stream = connection.getExistingObject(result.getAsJsonObject("stream").get("guid").getAsString());
    return stream.stream();
  }

  @Override
  public void delete() {
    sendMessage("delete");
  }

  @Override
  public String failure() {
    JsonObject result = sendMessage("failure").getAsJsonObject();
    if (result.has("error")) {
      return result.get("error").getAsString();
    }
    return null;
  }

  @Override
  public Path path() {
    JsonObject json = sendMessage("path").getAsJsonObject();
    return FileSystems.getDefault().getPath(json.get("value").getAsString());
  }

  @Override
  public void saveAs(Path path) {
    JsonObject params = new JsonObject();
    params.addProperty("path", path.toString());
    sendMessage("saveAs", params);
  }
}
