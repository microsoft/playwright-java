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
import com.microsoft.playwright.Keyboard;

import static com.microsoft.playwright.impl.Serialization.gson;

class KeyboardImpl implements Keyboard {
  private final ChannelOwner page;

  KeyboardImpl(ChannelOwner page) {
    this.page = page;
  }

  @Override
  public void down(String key) {
    page.withLogging("Keyboard.down", () -> {
      JsonObject params = new JsonObject();
      params.addProperty("key", key);
      page.sendMessage("keyboardDown", params);
    });
  }

  @Override
  public void insertText(String text) {
    page.withLogging("Keyboard.insertText", () -> {
      JsonObject params = new JsonObject();
      params.addProperty("text", text);
      page.sendMessage("keyboardInsertText", params);
    });
  }

  @Override
  public void press(String key, PressOptions options) {
    page.withLogging("Keyboard.press", () -> pressImpl(key, options));
  }

  private void pressImpl(String key, PressOptions options) {
    if (options == null) {
      options = new PressOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("key", key);
    page.sendMessage("keyboardPress", params);
  }

    @Override
  public void type(String text, TypeOptions options) {
    page.withLogging("Keyboard.type", () -> typeImpl(text, options));
  }

  private void typeImpl(String text, TypeOptions options) {
    if (options == null) {
      options = new TypeOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("text", text);
    page.sendMessage("keyboardType", params);
  }

  @Override
  public void up(String key) {
    page.withLogging("Keyboard.up", () -> {
      JsonObject params = new JsonObject();
      params.addProperty("key", key);
      page.sendMessage("keyboardUp", params);
    });
  }
}
