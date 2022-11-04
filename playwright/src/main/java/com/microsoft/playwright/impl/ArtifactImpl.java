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
import com.microsoft.playwright.PlaywrightException;

import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import static com.microsoft.playwright.impl.Utils.writeToFile;

class ArtifactImpl extends ChannelOwner {
  public ArtifactImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  public InputStream createReadStream() {
    JsonObject result = sendMessage("stream").getAsJsonObject();
    if (!result.has("stream")) {
      return null;
    }
    Stream stream = connection.getExistingObject(result.getAsJsonObject("stream").get("guid").getAsString());
    return stream.stream();
  }

  public void cancel() {
    sendMessage("cancel");
  }

  public void delete() {
    sendMessage("delete");
  }

  public String failure() {
    JsonObject result = sendMessage("failure").getAsJsonObject();
    if (result.has("error")) {
      return result.get("error").getAsString();
    }
    return null;
  }

  public Path pathAfterFinished() {
    if (connection.isRemote) {
      throw new PlaywrightException("Path is not available when using browserType.connect(). Use download.saveAs() to save a local copy.");
    }
    JsonObject json = sendMessage("pathAfterFinished").getAsJsonObject();
    return FileSystems.getDefault().getPath(json.get("value").getAsString());
  }

  public void saveAs(Path path) {
    if (connection.isRemote) {
      JsonObject jsonObject = sendMessage("saveAsStream").getAsJsonObject();
      Stream stream = connection.getExistingObject(jsonObject.getAsJsonObject("stream").get("guid").getAsString());
      writeToFile(stream.stream(), path);
      return;
    }

    JsonObject params = new JsonObject();
    params.addProperty("path", path.toString());
    sendMessage("saveAs", params);
  }
}
