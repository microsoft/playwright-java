/*
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

import com.google.gson.JsonObject;
import com.microsoft.playwright.Mouse;

import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.Utils.convertType;

class MouseImpl implements Mouse {
  private final ChannelOwner page;

  MouseImpl(ChannelOwner page) {
    this.page = page;
  }

  @Override
  public void click(double x, double y, ClickOptions options) {
    page.withLogging("Mouse.click", () -> clickImpl(x, y, options));
  }

  private void clickImpl(double x, double y, ClickOptions options) {
    if (options == null) {
      options = new ClickOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("x", x);
    params.addProperty("y", y);
    page.sendMessage("mouseClick", params);
  }

  @Override
  public void dblclick(double x, double y, DblclickOptions options) {
    page.withLogging("Mouse.dblclick", () -> dblclickImpl(x, y, options));
  }

  private void dblclickImpl(double x, double y, DblclickOptions options) {
    ClickOptions clickOptions;
    if (options == null) {
      clickOptions = new ClickOptions();
    } else {
      clickOptions = convertType(options, ClickOptions.class);
    }
    clickOptions.clickCount = 2;
    click(x, y, clickOptions);
  }

  @Override
  public void down(DownOptions options) {
    page.withLogging("Mouse.down", () -> downImpl(options));
  }

  private void downImpl(DownOptions options) {
    if (options == null) {
      options = new DownOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    page.sendMessage("mouseDown", params);
  }

  @Override
  public void move(double x, double y, MoveOptions options) {
    page.withLogging("Mouse.move", () -> moveImpl(x, y, options));
  }

  private void moveImpl(double x, double y, MoveOptions options) {
    if (options == null) {
      options = new MoveOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("x", x);
    params.addProperty("y", y);
    page.sendMessage("mouseMove", params);
  }

  @Override
  public void up(UpOptions options) {
    page.withLogging("Mouse.up", () -> upImpl(options));
  }

  @Override
  public void wheel(double deltaX, double deltaY) {
    page.withLogging("Mouse.wheel", () -> {
      JsonObject params = new JsonObject();
      params.addProperty("deltaX", deltaX);
      params.addProperty("deltaY", deltaY);
      page.sendMessage("mouseWheel", params);
    });
  }

  private void upImpl(UpOptions options) {
    if (options == null) {
      options = new UpOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    page.sendMessage("mouseUp", params);
  }
}
