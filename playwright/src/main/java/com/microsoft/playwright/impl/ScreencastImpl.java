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

import com.microsoft.playwright.impl.protocol.*;
import com.google.gson.JsonObject;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Screencast;
import com.microsoft.playwright.ScreencastFrame;

import java.nio.file.Path;
import java.util.function.Consumer;

import static com.microsoft.playwright.impl.ChannelOwner.NO_TIMEOUT;
import static com.microsoft.playwright.impl.Utils.toProtocol;

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
    try {
      if (onFrame != null) {
        String dataBase64 = params.get("data").getAsString();
        byte[] data = java.util.Base64.getDecoder().decode(dataBase64);
        double timestamp = params.get("timestamp").getAsDouble();
        int viewportWidth = params.get("viewportWidth").getAsInt();
        int viewportHeight = params.get("viewportHeight").getAsInt();
        onFrame.accept(new ScreencastFrameImpl(data, timestamp, viewportWidth, viewportHeight));
      }
    } finally {
      // The server sends the next frame only after the previous one is acknowledged.
      PageScreencastFrameAckParams ackParams = new PageScreencastFrameAckParams();
      ackParams.frameId = params.get("frameId").getAsInt();
      page.sendMessageAsync("screencastFrameAck", ackParams);
    }
  }

  @Override
  public AutoCloseable start(StartOptions options) {
    if (started) {
      throw new PlaywrightException("Screencast is already started");
    }
    started = true;
    PageScreencastStartParams params = new PageScreencastStartParams();
    if (options != null) {
      if (options.onFrame != null) {
        onFrame = options.onFrame;
      }
      if (options.size != null) {
        params.size = toProtocol(options.size, PageScreencastStartParams.PageScreencastStartParamsSize.class);
      }
      params.quality = options.quality;
      params.fps = options.fps;
      params.sendFrames = options.onFrame != null;
      params.record = options.path != null;
      savePath = options.path;
    } else {
      params.sendFrames = false;
      params.record = false;
    }
    PageScreencastStartResult result = page.channel.screencastStart(params, NO_TIMEOUT);
    if (result != null && result.artifact != null) {
      artifact = result.artifact;
    }
    return new DisposableStub(this::stop);
  }

  @Override
  public void stop() {
    started = false;
    onFrame = null;
    page.channel.screencastStop(NO_TIMEOUT);
    if (savePath != null && artifact != null) {
      artifact.saveAs(savePath);
    }
    artifact = null;
    savePath = null;
  }

  @Override
  public AutoCloseable showOverlay(String html, ShowOverlayOptions options) {
    PageScreencastShowOverlayParams params = toProtocol(options, PageScreencastShowOverlayParams.class);
    params.html = html;
    PageScreencastShowOverlayResult result = page.channel.screencastShowOverlay(params, NO_TIMEOUT);
    String id = result.id;
    return () -> {
      PageScreencastRemoveOverlayParams removeParams = new PageScreencastRemoveOverlayParams();
      removeParams.id = id;
      page.channel.screencastRemoveOverlay(removeParams, NO_TIMEOUT);
    };
  }

  @Override
  public void showChapter(String title, ShowChapterOptions options) {
    PageScreencastChapterParams params = toProtocol(options, PageScreencastChapterParams.class);
    params.title = title;
    page.channel.screencastChapter(params, NO_TIMEOUT);
  }

  @Override
  public AutoCloseable showActions(ShowActionsOptions options) {
    PageScreencastShowActionsParams params = toProtocol(options, PageScreencastShowActionsParams.class);
    page.channel.screencastShowActions(params, NO_TIMEOUT);
    return new DisposableStub(this::hideActions);
  }

  @Override
  public void showOverlays() {
    PageScreencastSetOverlayVisibleParams params = new PageScreencastSetOverlayVisibleParams();
    params.visible = true;
    page.channel.screencastSetOverlayVisible(params, NO_TIMEOUT);
  }

  @Override
  public void hideActions() {
    page.channel.screencastHideActions(NO_TIMEOUT);
  }

  @Override
  public void hideOverlays() {
    PageScreencastSetOverlayVisibleParams params = new PageScreencastSetOverlayVisibleParams();
    params.visible = false;
    page.channel.screencastSetOverlayVisible(params, NO_TIMEOUT);
  }
}
