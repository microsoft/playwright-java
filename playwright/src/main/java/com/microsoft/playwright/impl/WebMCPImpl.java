/*
 * Copyright (c) Microsoft Corporation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.WebMCP;
import com.microsoft.playwright.options.WebMCPTool;

import java.util.Arrays;
import java.util.List;

import static com.microsoft.playwright.impl.Serialization.deserialize;
import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.Serialization.serializeArgument;

class WebMCPImpl implements WebMCP {
  private final FrameImpl frame;

  WebMCPImpl(FrameImpl frame) {
    this.frame = frame;
  }

  @Override
  public List<WebMCPTool> tools(ToolsOptions options) {
    JsonElement json = frame.sendMessage("webmcpTools", new JsonObject(), frame.timeout(options == null ? null : options.timeout));
    WebMCPTool[] tools = gson().fromJson(json.getAsJsonObject().get("tools"), WebMCPTool[].class);
    return Arrays.asList(tools);
  }

  @Override
  public Object callTool(String name, Object input, CallToolOptions options) {
    JsonObject params = new JsonObject();
    params.addProperty("name", name);
    params.add("input", gson().toJsonTree(serializeArgument(input)));
    JsonElement json = frame.sendMessage("webmcpCallTool", params, frame.timeout(options == null ? null : options.timeout));
    SerializedValue value = gson().fromJson(json.getAsJsonObject().get("result"), SerializedValue.class);
    return deserialize(value);
  }
}
