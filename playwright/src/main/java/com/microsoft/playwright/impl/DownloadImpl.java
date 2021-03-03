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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.PlaywrightException;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import static com.microsoft.playwright.impl.Utils.writeToFile;

public class DownloadImpl extends ChannelOwner implements Download {
  private final BrowserImpl browser;

  public DownloadImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    browser = ((BrowserContextImpl) parent).browser();
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
    return withLogging("Download.createReadStream", () -> {
      JsonObject result = sendMessage("stream").getAsJsonObject();
      if (!result.has("stream")) {
        return null;
      }
      Stream stream = connection.getExistingObject(result.getAsJsonObject("stream").get("guid").getAsString());
      return stream.stream();
    });
  }

  @Override
  public void delete() {
    withLogging("Download.delete", () -> {
      sendMessage("delete");
    });
  }

  @Override
  public String failure() {
    return withLogging("Download.failure", () -> {
      JsonObject result = sendMessage("failure").getAsJsonObject();
      if (result.has("error")) {
        return result.get("error").getAsString();
      }
      return null;
    });
  }

  @Override
  public Path path() {
    return withLogging("Download.path", () -> {
      if (browser != null && browser.isRemote) {
        throw new PlaywrightException("Path is not available when using browserType.connect(). Use download.saveAs() to save a local copy.");
      }
      JsonObject json = sendMessage("path").getAsJsonObject();
      return FileSystems.getDefault().getPath(json.get("value").getAsString());
    });
  }

  @Override
  public void saveAs(Path path) {
    withLogging("Download.saveAs", () -> {
      if (browser != null && browser.isRemote) {
        JsonObject jsonObject = sendMessage("saveAsStream").getAsJsonObject();
        Stream stream = connection.getExistingObject(jsonObject.getAsJsonObject("stream").get("guid").getAsString());
        writeToFile(stream.stream(), path);
        return;
      }

      JsonObject params = new JsonObject();
      params.addProperty("path", path.toString());
      sendMessage("saveAs", params);
    });
  }
}
