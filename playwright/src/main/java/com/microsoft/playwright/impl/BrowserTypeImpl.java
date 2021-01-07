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
import com.microsoft.playwright.BrowserType;

import java.nio.file.Path;

import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.Serialization.toProtocol;

class BrowserTypeImpl extends ChannelOwner implements BrowserType {
  BrowserTypeImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  @Override
  public BrowserImpl launch(LaunchOptions options) {
    return withLogging("BrowserType.launch", () -> launchImpl(options));
  }

  private BrowserImpl launchImpl(LaunchOptions options) {
    if (options == null) {
      options = new LaunchOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    JsonElement result = sendMessage("launch", params);
    return connection.getExistingObject(result.getAsJsonObject().getAsJsonObject("browser").get("guid").getAsString());
  }

  public String executablePath() {
    return initializer.get("executablePath").getAsString();
  }

  @Override
  public BrowserContextImpl launchPersistentContext(Path userDataDir, LaunchPersistentContextOptions options) {
    return withLogging("BrowserType.launchPersistentContext",
      () -> launchPersistentContextImpl(userDataDir, options));
  }

  private BrowserContextImpl launchPersistentContextImpl(Path userDataDir, LaunchPersistentContextOptions options) {
    if (options == null) {
      options = new LaunchPersistentContextOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("userDataDir", userDataDir.toString());
    JsonObject json = sendMessage("launchPersistentContext", params).getAsJsonObject();
    BrowserContextImpl context = connection.getExistingObject(json.getAsJsonObject("context").get("guid").getAsString());
    if (options.recordVideo != null) {
      context.videosDir = options.recordVideo.dir;
    }
    return context;
  }

  public String name() {
    return initializer.get("name").getAsString();
  }

}
