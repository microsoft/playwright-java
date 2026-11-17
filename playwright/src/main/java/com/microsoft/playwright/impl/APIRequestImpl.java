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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.ClientCertificate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static com.microsoft.playwright.impl.ChannelOwner.NO_TIMEOUT;
import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.Utils.toProtocol;

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
    PlaywrightNewRequestParams.PlaywrightNewRequestParamsStorageState storageState = null;
    if (options.storageState != null) {
      storageState = gson().fromJson(options.storageState, PlaywrightNewRequestParams.PlaywrightNewRequestParamsStorageState.class);
      options.storageState = null;
    }
    List<ClientCertificate> clientCertificateList = options.clientCertificates;
    options.clientCertificates = null;
    // The protocol always takes a list (mirrors toHttpCredentialsProtocol() in the JS client).
    Object httpCredentials = options.httpCredentials;
    options.httpCredentials = null;
    Double timeout = options.timeout;
    // Timeout is handled on the client.
    options.timeout = null;
    PlaywrightNewRequestParams params = toProtocol(options, PlaywrightNewRequestParams.class);
    if (storageState != null) {
      params.storageState = storageState;
    }
    if (clientCertificateList != null) {
      params.clientCertificates = new ArrayList<>();
      for (ClientCertificate cert : clientCertificateList) {
        PlaywrightNewRequestParams.PlaywrightNewRequestParamsClientCertificates c = new PlaywrightNewRequestParams.PlaywrightNewRequestParamsClientCertificates();
        c.origin = cert.origin;
        try {
          c.cert = cert.certPath != null ? Files.readAllBytes(cert.certPath) : cert.cert;
          c.key = cert.keyPath != null ? Files.readAllBytes(cert.keyPath) : cert.key;
          c.pfx = cert.pfxPath != null ? Files.readAllBytes(cert.pfxPath) : cert.pfx;
        } catch (IOException e) {
          throw new PlaywrightException("Failed to read from file", e);
        }
        c.passphrase = cert.passphrase;
        params.clientCertificates.add(c);
      }
    }
    if (httpCredentials != null) {
      JsonElement credentials = gson().toJsonTree(httpCredentials);
      JsonArray list = new JsonArray();
      if (credentials.isJsonArray()) {
        list.addAll(credentials.getAsJsonArray());
      } else {
        list.add(credentials);
      }
      if (list.size() > 0) {
        params.httpCredentials = gson().fromJson(list, new TypeToken<List<HttpCredentials>>() {}.getType());
      }
    }
    APIRequestContextImpl context = playwright.channel.newRequest(params, NO_TIMEOUT).request;
    context.timeoutSettings.setDefaultTimeout(timeout);
    return context;
  }
}
