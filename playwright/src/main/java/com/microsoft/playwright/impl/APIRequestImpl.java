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

// Port of fetch.ts.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.impl.isomorphic.Headers;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.impl.protocol.PlaywrightNewRequestParams;
import com.microsoft.playwright.options.ClientCertificate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.microsoft.playwright.impl.ChannelOwner.NO_TIMEOUT;
import static com.microsoft.playwright.impl.Serialization.gson;

class APIRequestImpl implements APIRequest {
  private final PlaywrightImpl playwright;
  final Set<APIRequestContextImpl> contexts = new LinkedHashSet<>();

  APIRequestImpl(PlaywrightImpl playwright) {
    this.playwright = playwright;
  }

  @Override
  public APIRequestContextImpl newContext(NewContextOptions options) {
    options = options == null ? new NewContextOptions() : Utils.clone(options);
    // java: storageState is the JSON text and storageStatePath the file, upstream takes a path string or an object.
    String storageStateJson = options.storageState;
    if (options.storageStatePath != null) {
      try {
        storageStateJson = new String(Files.readAllBytes(options.storageStatePath), StandardCharsets.UTF_8);
      } catch (IOException e) {
        throw new PlaywrightException("Failed to read storage state from file", e);
      }
    }
    PlaywrightNewRequestParams.PlaywrightNewRequestParamsStorageState storageState = storageStateJson == null ? null :
      gson().fromJson(storageStateJson, PlaywrightNewRequestParams.PlaywrightNewRequestParamsStorageState.class);
    Map<String, String> extraHTTPHeaders = options.extraHTTPHeaders;
    List<ClientCertificate> clientCertificates = options.clientCertificates;
    Object httpCredentials = options.httpCredentials;
    // java: the fields that the spread of upstream replaces are cleared before the options are copied into the params.
    options.storageState = null;
    options.storageStatePath = null;
    options.extraHTTPHeaders = null;
    options.clientCertificates = null;
    options.httpCredentials = null;
    PlaywrightNewRequestParams params = Utils.toProtocol(options, PlaywrightNewRequestParams.class);
    params.extraHTTPHeaders = extraHTTPHeaders != null ? Headers.headersObjectToArray(extraHTTPHeaders) : null;
    params.storageState = storageState;
    // java: no _defaultLaunchOptions in the Java API, so no tracesDir here and none for context.tracing below.
    params.clientCertificates = BrowserContextImpl.toClientCertificatesProtocol(clientCertificates);
    params.httpCredentials = BrowserContextImpl.toHttpCredentialsProtocol(httpCredentials);
    APIRequestContextImpl context = playwright.channel.newRequest(params, NO_TIMEOUT).request;
    contexts.add(context);
    context.request = this;
    // java: no _defaultContextTimeout in the Java API.
    context.timeoutSettings.setDefaultTimeout(options.timeout);
    return context;
  }
}
