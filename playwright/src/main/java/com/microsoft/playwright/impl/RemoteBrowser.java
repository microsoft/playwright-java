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

public class RemoteBrowser extends ChannelOwner {
  RemoteBrowser(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  BrowserImpl browser() {
    return connection.getExistingObject(initializer.getAsJsonObject("browser").get("guid").getAsString());
  }

  SelectorsImpl selectors() {
    return connection.getExistingObject(initializer.getAsJsonObject("selectors").get("guid").getAsString());
  }
}
