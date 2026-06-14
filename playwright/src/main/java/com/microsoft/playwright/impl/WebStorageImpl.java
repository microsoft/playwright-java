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
import com.microsoft.playwright.WebStorage;
import com.microsoft.playwright.options.WebStorageItem;

import java.util.List;

import static com.microsoft.playwright.impl.ChannelOwner.NO_TIMEOUT;
import static com.microsoft.playwright.impl.Serialization.gson;
import static java.util.Arrays.asList;

class WebStorageImpl implements WebStorage {
  private final PageImpl page;
  private final String kind;

  WebStorageImpl(PageImpl page, String kind) {
    this.page = page;
    this.kind = kind;
  }

  private JsonObject createParams() {
    JsonObject params = new JsonObject();
    params.addProperty("kind", kind);
    return params;
  }

  @Override
  public List<WebStorageItem> items() {
    JsonObject json = page.sendMessage("webStorageItems", createParams(), NO_TIMEOUT).getAsJsonObject();
    return asList(gson().fromJson(json.getAsJsonArray("items"), WebStorageItem[].class));
  }

  @Override
  public String getItem(String name) {
    JsonObject params = createParams();
    params.addProperty("name", name);
    JsonObject json = page.sendMessage("webStorageGetItem", params, NO_TIMEOUT).getAsJsonObject();
    return json.has("value") ? json.get("value").getAsString() : null;
  }

  @Override
  public void setItem(String name, String value) {
    JsonObject params = createParams();
    params.addProperty("name", name);
    params.addProperty("value", value);
    page.sendMessage("webStorageSetItem", params, NO_TIMEOUT);
  }

  @Override
  public void removeItem(String name) {
    JsonObject params = createParams();
    params.addProperty("name", name);
    page.sendMessage("webStorageRemoveItem", params, NO_TIMEOUT);
  }

  @Override
  public void clear() {
    page.sendMessage("webStorageClear", createParams(), NO_TIMEOUT);
  }
}
