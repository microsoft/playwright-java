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
import com.microsoft.playwright.ConsoleMessage;
import com.microsoft.playwright.JSHandle;

import java.util.ArrayList;
import java.util.List;

import static com.microsoft.playwright.impl.Serialization.gson;

public class ConsoleMessageImpl extends ChannelOwner implements ConsoleMessage {
  public ConsoleMessageImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  public String type() {
    return initializer.get("type").getAsString();
  }

  public String text() {
    return initializer.get("text").getAsString();
  }

  @Override
  public List<JSHandle> args() {
    List<JSHandle> result = new ArrayList<>();
    for (JsonElement item : initializer.getAsJsonArray("args")) {
      result.add(connection.getExistingObject(item.getAsJsonObject().get("guid").getAsString()));
    }
    return result;
  }

  public Location location() {
    return gson().fromJson(initializer.get("location"), Location.class);
  }
}
