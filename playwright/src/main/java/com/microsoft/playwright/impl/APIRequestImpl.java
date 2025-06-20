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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.ClientCertificate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import static com.microsoft.playwright.impl.ChannelOwner.NO_TIMEOUT;
import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.Utils.addToProtocol;

class APIRequestImpl implements APIRequest {
  private final PlaywrightImpl playwright;

  APIRequestImpl(PlaywrightImpl playwright) {
    this.playwright = playwright;
  }

  @Override
  public APIRequestContextImpl newContext(NewContextOptions options) {
    if (options == null) {
      options = new NewContextOptions();
    } else {
      options = Utils.clone(options);
    }
    if (options.storageStatePath != null) {
      try {
        byte[] bytes = Files.readAllBytes(options.storageStatePath);
        options.storageState = new String(bytes, StandardCharsets.UTF_8);
        options.storageStatePath = null;
      } catch (IOException e) {
        throw new PlaywrightException("Failed to read storage state from file", e);
      }
    }
    JsonObject storageState = null;
    if (options.storageState != null) {
      storageState = new Gson().fromJson(options.storageState, JsonObject.class);
      options.storageState = null;
    }
    List<ClientCertificate> clientCertificateList = options.clientCertificates;
    options.clientCertificates = null;
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    if (storageState != null) {
      params.add("storageState", storageState);
    }
    addToProtocol(params, clientCertificateList);
    JsonObject result = playwright.sendMessage("newRequest", params, options.timeout).getAsJsonObject();
    APIRequestContextImpl context = playwright.connection.getExistingObject(result.getAsJsonObject("request").get("guid").getAsString());
    context.timeoutSettings.setDefaultTimeout(options.timeout);
    return context;
  }
}
