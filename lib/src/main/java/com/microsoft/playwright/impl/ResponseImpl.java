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

public class ResponseImpl extends ChannelOwner implements Response {
  ResponseImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  @Override
  public byte[] body() {
    return new byte[0];
  }

  @Override
  public Error finished() {
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
  public Object json() {
    return null;
  }

  @Override
  public boolean ok() {
    return false;
  }

  @Override
  public Request request() {
    return null;
  }

  @Override
  public int status() {
    return 0;
  }

  @Override
  public String statusText() {
    return null;
  }

  @Override
  public String text() {
    return null;
  }

  @Override
  public String url() {
    return null;
  }
}
