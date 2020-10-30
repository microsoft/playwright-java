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
import com.microsoft.playwright.Selectors;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.microsoft.playwright.impl.Serialization.gson;
import static java.nio.charset.StandardCharsets.UTF_8;

class SelectorsImpl extends ChannelOwner implements Selectors {
  SelectorsImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  @Override
  public void register(String name, String script, RegisterOptions options) {
    if (options == null) {
      options = new RegisterOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("name", name);
    params.addProperty("source", script);
    sendMessage("register", params);
  }

  @Override
  public void register(String name, Path path, RegisterOptions options) {
    byte[] buffer;
    try {
      buffer = Files.readAllBytes(path);
    } catch (IOException e) {
      throw new PlaywrightException("Failed to read selector from file: " + path, e);
    }
    register(name, new String(buffer, UTF_8), options);
  }
}
