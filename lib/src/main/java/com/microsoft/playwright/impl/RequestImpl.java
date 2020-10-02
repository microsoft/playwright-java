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

import com.google.gson.JsonObject;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;

import java.util.Map;

public class RequestImpl extends ChannelOwner implements Request {
  RequestImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  @Override
  public RequestFailure failure() {
    return null;
  }

  @Override
  public Frame frame() {
    return null;
  }

  @Override
  public Map<String, String> headers() {
    return null;
  }

  @Override
  public boolean isNavigationRequest() {
    return false;
  }

  @Override
  public String method() {
    return initializer.get("method").getAsString();
  }

  @Override
  public String postData() {
    return null;
  }

  @Override
  public byte[] postDataBuffer() {
    return new byte[0];
  }

  @Override
  public RequestPostDataJSON postDataJSON() {
    return null;
  }

  @Override
  public Request redirectedFrom() {
    return null;
  }

  @Override
  public Request redirectedTo() {
    return null;
  }

  @Override
  public String resourceType() {
    return initializer.get("resourceType").getAsString();
  }

  @Override
  public Response response() {
    return null;
  }

  @Override
  public String url() {
    return initializer.get("url").getAsString();
  }
}
