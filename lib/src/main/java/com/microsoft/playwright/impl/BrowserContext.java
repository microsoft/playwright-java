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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.function.Supplier;

public class BrowserContext extends ChannelOwner {
  protected BrowserContext(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  Page newPage() {
    JsonObject params = new JsonObject();
    JsonElement result = sendMessage("newPage", params);
    return connection.getExistingObject(result.getAsJsonObject().getAsJsonObject("page").get("guid").getAsString());
  }

  public Supplier<Page> waitForPage() {
    Supplier<JsonObject> pageSupplier = waitForEvent("page");
    return () -> {
      JsonObject params = pageSupplier.get();
      String guid = params.getAsJsonObject("page").get("guid").getAsString();
      return connection.getExistingObject(guid);
    };
  }

}
