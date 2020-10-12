/**
 * Copyright (c) Microsoft Corporation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.microsoft.playwright.Mouse;

import static com.microsoft.playwright.impl.Serialization.toProtocol;
import static com.microsoft.playwright.impl.Utils.convertViaJson;

class MouseImpl implements Mouse {
  private final ChannelOwner page;

  MouseImpl(ChannelOwner page) {
    this.page = page;
  }

  @Override
  public void click(int x, int y, ClickOptions options) {
    if (options == null) {
      options = new ClickOptions();
    }
    JsonObject params = new Gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("x", x);
    params.addProperty("y", y);
    if (options.button != null) {
      params.remove("button");
      params.addProperty("button", toProtocol(options.button));
    }
    page.sendMessage("mouseClick", params);
  }

  @Override
  public void dblclick(int x, int y, DblclickOptions options) {
    ClickOptions clickOptions;
    if (options == null) {
      clickOptions = new ClickOptions();
    } else {
      clickOptions = convertViaJson(options, ClickOptions.class);
    }
    clickOptions.clickCount = 2;
    click(x, y, clickOptions);
  }

  @Override
  public void down(DownOptions options) {
    if (options == null) {
      options = new DownOptions();
    }
    JsonObject params = new Gson().toJsonTree(options).getAsJsonObject();
    page.sendMessage("mouseDown", params);
  }

  @Override
  public void move(int x, int y, MoveOptions options) {
    if (options == null) {
      options = new MoveOptions();
    }
    JsonObject params = new Gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("x", x);
    params.addProperty("y", y);
    page.sendMessage("mouseMove", params);
  }

  @Override
  public void up(UpOptions options) {
    if (options == null) {
      options = new UpOptions();
    }
    JsonObject params = new Gson().toJsonTree(options).getAsJsonObject();
    page.sendMessage("mouseUp", params);
  }
}
