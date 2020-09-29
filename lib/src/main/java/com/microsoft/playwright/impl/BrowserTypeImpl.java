/**
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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserServer;
import com.microsoft.playwright.BrowserType;

class BrowserTypeImpl extends ChannelOwner implements BrowserType {
  BrowserTypeImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  @Override
  public Browser connect(ConnectOptions options) {
    return null;
  }

  public BrowserImpl launch() {
    return launch(new LaunchOptions());
  }
  @Override
  public BrowserImpl launch(LaunchOptions options) {
    JsonObject params = new Gson().toJsonTree(options).getAsJsonObject();
    JsonElement result = sendMessage("launch", params);
    return connection.getExistingObject(result.getAsJsonObject().getAsJsonObject("browser").get("guid").getAsString());
  }

  public String executablePath() {
    return initializer.get("executablePath").getAsString();
  }

  @Override
  public BrowserContext launchPersistentContext(String userDataDir, LaunchPersistentContextOptions options) {
    return null;
  }

  @Override
  public BrowserServer launchServer(LaunchServerOptions options) {
    return null;
  }

  public String name() {
    return initializer.get("name").getAsString();
  }

}
