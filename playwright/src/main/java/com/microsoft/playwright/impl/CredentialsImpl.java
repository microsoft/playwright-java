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
import com.microsoft.playwright.Credentials;
import com.microsoft.playwright.options.VirtualCredential;

import java.util.List;

import static com.microsoft.playwright.impl.ChannelOwner.NO_TIMEOUT;
import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.Utils.toProtocol;
import static java.util.Arrays.asList;

class CredentialsImpl implements Credentials {
  private final BrowserContextImpl context;

  CredentialsImpl(BrowserContextImpl context) {
    this.context = context;
  }

  @Override
  public void install() {
    context.channel.credentialsInstall(NO_TIMEOUT);
  }

  @Override
  public VirtualCredential create(String rpId, CreateOptions options) {
    BrowserContextCredentialsCreateParams params = toProtocol(options, BrowserContextCredentialsCreateParams.class);
    params.rpId = rpId;
    BrowserContextCredentialsCreateResult result = context.channel.credentialsCreate(params, NO_TIMEOUT);
    return gson().fromJson(gson().toJsonTree(result.credential), VirtualCredential.class);
  }

  @Override
  public void delete(String id) {
    BrowserContextCredentialsDeleteParams params = new BrowserContextCredentialsDeleteParams();
    params.id = id;
    context.channel.credentialsDelete(params, NO_TIMEOUT);
  }

  @Override
  public List<VirtualCredential> get(GetOptions options) {
    BrowserContextCredentialsGetParams params = toProtocol(options, BrowserContextCredentialsGetParams.class);
    BrowserContextCredentialsGetResult result = context.channel.credentialsGet(params, NO_TIMEOUT);
    return asList(gson().fromJson(gson().toJsonTree(result.credentials), VirtualCredential[].class));
  }
}
