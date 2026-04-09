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
import com.microsoft.playwright.Screencast;
import com.microsoft.playwright.options.ScreencastFrame;

import java.nio.file.Path;
import java.util.function.Consumer;

import static com.microsoft.playwright.impl.ChannelOwner.NO_TIMEOUT;
import static com.microsoft.playwright.impl.Serialization.gson;

class ScreencastImpl implements Screencast {
  private final PageImpl page;
  private boolean started;
  private Path savePath;
  private Consumer<ScreencastFrame> onFrame;
  private ArtifactImpl artifact;

  ScreencastImpl(PageImpl page) {
    this.page = page;
  }

  void handleScreencastFrame(JsonObject params) {
    if (onFrame == null) {
      return;
    }
    String dataBase64 = params.get("data").getAsString();
    byte[] data = java.util.Base64.getDecoder().decode(dataBase64);
    onFrame.accept(new ScreencastFrame(data));
  }

  @Override
  public AutoCloseable start(StartOptions options) {
    if (started) {
      throw new PlaywrightException("Screencast is already started");
    }
    started = true;
    JsonObject params = new JsonObject();
    if (options != null) {
      if (options.onFrame != null) {
        onFrame = options.onFrame;
      }
      if (options.quality != null) {
        params.addProperty("quality", options.quality);
      }
      params.addProperty("sendFrames", options.onFrame != null);
      params.addProperty("record", options.path != null);
      savePath = options.path;
    } else {
      params.addProperty("sendFrames", false);
      params.addProperty("record", false);
    }
    JsonObject result = page.sendMessage("screencastStart", params, NO_TIMEOUT).getAsJsonObject();
    if (result.has("artifact")) {
      String artifactGuid = result.getAsJsonObject("artifact").get("guid").getAsString();
      artifact = page.connection.getExistingObject(artifactGuid);
    }
    return new DisposableStub(this::stop);
  }

  @Override
  public void stop() {
    started = false;
    onFrame = null;
    page.sendMessage("screencastStop", new JsonObject(), NO_TIMEOUT);
    if (savePath != null && artifact != null) {
      artifact.saveAs(savePath);
    }
    artifact = null;
    savePath = null;
  }

  @Override
  public AutoCloseable showOverlay(String html, ShowOverlayOptions options) {
    JsonObject params = new JsonObject();
    params.addProperty("html", html);
    if (options != null && options.duration != null) {
      params.addProperty("duration", options.duration);
    }
    JsonObject result = (JsonObject) page.sendMessage("screencastShowOverlay", params, NO_TIMEOUT);
    String id = result.get("id").getAsString();
    return () -> {
      JsonObject removeParams = new JsonObject();
      removeParams.addProperty("id", id);
      page.sendMessage("screencastRemoveOverlay", removeParams, NO_TIMEOUT);
    };
  }

  @Override
  public void showChapter(String title, ShowChapterOptions options) {
    JsonObject params = new JsonObject();
    params.addProperty("title", title);
    if (options != null) {
      if (options.description != null) {
        params.addProperty("description", options.description);
      }
      if (options.duration != null) {
        params.addProperty("duration", options.duration);
      }
    }
    page.sendMessage("screencastChapter", params, NO_TIMEOUT);
  }

  @Override
  public AutoCloseable showActions(ShowActionsOptions options) {
    JsonObject params = new JsonObject();
    if (options != null) {
      if (options.duration != null) {
        params.addProperty("duration", options.duration);
      }
      if (options.fontSize != null) {
        params.addProperty("fontSize", options.fontSize);
      }
      if (options.position != null) {
        params.add("position", gson().toJsonTree(options.position));
      }
    }
    page.sendMessage("screencastShowActions", params, NO_TIMEOUT);
    return new DisposableStub(this::hideActions);
  }

  @Override
  public void showOverlays() {
    JsonObject params = new JsonObject();
    params.addProperty("visible", true);
    page.sendMessage("screencastSetOverlayVisible", params, NO_TIMEOUT);
  }

  @Override
  public void hideActions() {
    page.sendMessage("screencastHideActions", new JsonObject(), NO_TIMEOUT);
  }

  @Override
  public void hideOverlays() {
    JsonObject params = new JsonObject();
    params.addProperty("visible", false);
    page.sendMessage("screencastSetOverlayVisible", params, NO_TIMEOUT);
  }
}
