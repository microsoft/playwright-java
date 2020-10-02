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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Deferred;
import com.microsoft.playwright.Page;

import java.util.List;
import java.util.Map;

class BrowserImpl extends ChannelOwner implements Browser {
  BrowserImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  @Override
  public void close() {
    sendMessage("close", new JsonObject());
  }

  @Override
  public List<BrowserContext> contexts() {
    return null;
  }

  @Override
  public boolean isConnected() {
    return false;
  }


  private static JsonArray toProtocol(Map<String, String> map) {
    JsonArray array = new JsonArray();
    for (Map.Entry<String, String> e : map.entrySet()) {
      JsonObject item = new JsonObject();
      item.addProperty("name", e.getKey());
      item.addProperty("value", e.getValue());
      array.add(item);
    }
    return array;
  }

  @Override
  public BrowserContextImpl newContext(NewContextOptions options) {
    if (options == null) {
      options = new NewContextOptions();
    }
    JsonObject params = new Gson().toJsonTree(options).getAsJsonObject();
    if (options.extraHTTPHeaders != null) {
      params.remove("extraHTTPHeaders");
      params.add("extraHTTPHeaders", toProtocol(options.extraHTTPHeaders));
    }
    JsonElement result = sendMessage("newContext", params);
    return connection.getExistingObject(result.getAsJsonObject().getAsJsonObject("context").get("guid").getAsString());
  }

  @Override
  public Page newPage(NewPageOptions options) {
    return null;
  }

  @Override
  public String version() {
    return initializer.get("version").getAsString();
  }
}
