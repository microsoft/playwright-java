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
import com.microsoft.playwright.Overlay;

import static com.microsoft.playwright.impl.ChannelOwner.NO_TIMEOUT;

class OverlayImpl implements Overlay {
  private final ChannelOwner page;

  OverlayImpl(ChannelOwner page) {
    this.page = page;
  }

  @Override
  public AutoCloseable show(String html, ShowOptions options) {
    JsonObject params = new JsonObject();
    params.addProperty("html", html);
    if (options != null && options.duration != null) {
      params.addProperty("duration", options.duration);
    }
    JsonObject result = (JsonObject) page.sendMessage("overlayShow", params, NO_TIMEOUT);
    String id = result.get("id").getAsString();
    return () -> {
      JsonObject removeParams = new JsonObject();
      removeParams.addProperty("id", id);
      page.sendMessage("overlayRemove", removeParams, NO_TIMEOUT);
    };
  }

  @Override
  public void chapter(String title, ChapterOptions options) {
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
    page.sendMessage("overlayChapter", params, NO_TIMEOUT);
  }

  @Override
  public void setVisible(boolean visible) {
    JsonObject params = new JsonObject();
    params.addProperty("visible", visible);
    page.sendMessage("overlaySetVisible", params, NO_TIMEOUT);
  }
}
