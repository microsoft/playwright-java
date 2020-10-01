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

import com.google.gson.JsonObject;
import com.microsoft.playwright.Keyboard;

class KeyboardImpl implements Keyboard {
  private final ChannelOwner page;

  KeyboardImpl(ChannelOwner page) {
    this.page = page;
  }

  @Override
  public void down(String key) {
    JsonObject params = new JsonObject();
    params.addProperty("key", key);
    page.sendMessage("keyboardDown", params);
  }

  @Override
  public void insertText(String text) {
    JsonObject params = new JsonObject();
    params.addProperty("text", text);
    page.sendMessage("keyboardInsertText", params);
  }

  @Override
  public void press(String key, int delay) {
    JsonObject params = new JsonObject();
    params.addProperty("key", key);
    if (delay != 0) {
      params.addProperty("delay", delay);
    }
    page.sendMessage("keyboardPress", params);
  }

  @Override
  public void type(String text, int delay) {
    JsonObject params = new JsonObject();
    params.addProperty("text", text);
    if (delay != 0) {
      params.addProperty("delay", delay);
    }
    page.sendMessage("keyboardType", params);
  }

  @Override
  public void up(String key) {
    JsonObject params = new JsonObject();
    params.addProperty("key", key);
    page.sendMessage("keyboardUp", params);
  }
}
