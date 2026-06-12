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
import com.microsoft.playwright.Credentials;
import com.microsoft.playwright.options.VirtualCredential;

import java.util.List;

import static com.microsoft.playwright.impl.ChannelOwner.NO_TIMEOUT;
import static com.microsoft.playwright.impl.Serialization.gson;
import static java.util.Arrays.asList;

class CredentialsImpl implements Credentials {
  private final BrowserContextImpl context;

  CredentialsImpl(BrowserContextImpl context) {
    this.context = context;
  }

  @Override
  public void install() {
    context.sendMessage("credentialsInstall", new JsonObject(), NO_TIMEOUT);
  }

  @Override
  public VirtualCredential create(String rpId, CreateOptions options) {
    JsonObject params = options == null ? new JsonObject() : gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("rpId", rpId);
    JsonObject json = context.sendMessage("credentialsCreate", params, NO_TIMEOUT).getAsJsonObject();
    return gson().fromJson(json.get("credential"), VirtualCredential.class);
  }

  @Override
  public void delete(String id) {
    JsonObject params = new JsonObject();
    params.addProperty("id", id);
    context.sendMessage("credentialsDelete", params, NO_TIMEOUT);
  }

  @Override
  public List<VirtualCredential> get(GetOptions options) {
    JsonObject params = options == null ? new JsonObject() : gson().toJsonTree(options).getAsJsonObject();
    JsonObject json = context.sendMessage("credentialsGet", params, NO_TIMEOUT).getAsJsonObject();
    return asList(gson().fromJson(json.getAsJsonArray("credentials"), VirtualCredential[].class));
  }
}
