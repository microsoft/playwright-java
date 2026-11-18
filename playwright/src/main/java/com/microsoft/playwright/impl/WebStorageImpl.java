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

import com.microsoft.playwright.impl.protocol.*;
import com.microsoft.playwright.WebStorage;
import com.microsoft.playwright.options.WebStorageItem;

import java.util.List;

import static com.microsoft.playwright.impl.ChannelOwner.NO_TIMEOUT;
import static com.microsoft.playwright.impl.Serialization.gson;
import static java.util.Arrays.asList;

// Port of webStorage.ts.
class WebStorageImpl implements WebStorage {
  private final PageImpl page;
  private final String kind;

  WebStorageImpl(PageImpl page, String kind) {
    this.page = page;
    this.kind = kind;
  }

  @Override
  public List<WebStorageItem> items() {
    PageWebStorageItemsParams params = new PageWebStorageItemsParams();
    params.kind = kind;
    PageWebStorageItemsResult result = page.channel.webStorageItems(params, NO_TIMEOUT);
    return asList(gson().fromJson(gson().toJsonTree(result.items), WebStorageItem[].class));
  }

  @Override
  public String getItem(String name) {
    PageWebStorageGetItemParams params = new PageWebStorageGetItemParams();
    params.kind = kind;
    params.name = name;
    return page.channel.webStorageGetItem(params, NO_TIMEOUT).value;
  }

  @Override
  public void setItem(String name, String value) {
    PageWebStorageSetItemParams params = new PageWebStorageSetItemParams();
    params.kind = kind;
    params.name = name;
    params.value = value;
    page.channel.webStorageSetItem(params, NO_TIMEOUT);
  }

  @Override
  public void removeItem(String name) {
    PageWebStorageRemoveItemParams params = new PageWebStorageRemoveItemParams();
    params.kind = kind;
    params.name = name;
    page.channel.webStorageRemoveItem(params, NO_TIMEOUT);
  }

  @Override
  public void clear() {
    PageWebStorageClearParams params = new PageWebStorageClearParams();
    params.kind = kind;
    page.channel.webStorageClear(params, NO_TIMEOUT);
  }
}
