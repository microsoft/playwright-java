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
import com.microsoft.playwright.Selectors;

import static com.microsoft.playwright.impl.Serialization.gson;

class SelectorsImpl extends ChannelOwner {
  SelectorsImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  void register(String name, String script, Selectors.RegisterOptions options) {
    if (options == null) {
      options = new Selectors.RegisterOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("name", name);
    params.addProperty("source", script);
    sendMessageAsync("register", params);
  }

  void setTestIdAttributeName(String name) {
    JsonObject params = new JsonObject();
    params.addProperty("testIdAttributeName", name);
    sendMessageAsync("setTestIdAttributeName", params);
  }
}
