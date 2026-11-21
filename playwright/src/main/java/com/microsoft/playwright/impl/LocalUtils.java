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

// Port of localUtils.ts.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.impl.protocol.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import com.microsoft.playwright.impl.protocol.LocalUtilsInitializer.LocalUtilsInitializerDeviceDescriptors;
import com.microsoft.playwright.impl.protocol.LocalUtilsInitializer.LocalUtilsInitializerDeviceDescriptors.LocalUtilsInitializerDeviceDescriptorsDescriptor;

public class LocalUtils extends ChannelOwner {
  final LocalUtilsChannel channel = new LocalUtilsChannel(this);
  // java: DeviceDescriptor is the generated descriptor type of the initializer.
  final Map<String, LocalUtilsInitializerDeviceDescriptorsDescriptor> devices;

  LocalUtils(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    LocalUtilsInitializer init = connection.protocolGson().fromJson(initializer, LocalUtilsInitializer.class);
    this.devices = new LinkedHashMap<>();
    for (LocalUtilsInitializerDeviceDescriptors device : init.deviceDescriptors)
      this.devices.put(device.name, device.descriptor);
  }

  void zip(LocalUtilsZipParams params) {
    channel.zip(params, NO_TIMEOUT);
  }

  LocalUtilsHarOpenResult harOpen(LocalUtilsHarOpenParams params) {
    return channel.harOpen(params, NO_TIMEOUT);
  }

  LocalUtilsHarLookupResult harLookup(LocalUtilsHarLookupParams params) {
    return channel.harLookup(params, NO_TIMEOUT);
  }

  // java: the pending reply, as the only caller does not await it.
  WaitableResult<JsonElement> harClose(LocalUtilsHarCloseParams params) {
    return sendMessageAsync("harClose", params);
  }

  void harUnzip(LocalUtilsHarUnzipParams params) {
    channel.harUnzip(params, NO_TIMEOUT);
  }

  LocalUtilsTracingStartedResult tracingStarted(LocalUtilsTracingStartedParams params) {
    return channel.tracingStarted(params, NO_TIMEOUT);
  }

  void traceDiscarded(LocalUtilsTraceDiscardedParams params) {
    channel.traceDiscarded(params, NO_TIMEOUT);
  }

  // java: the pending reply, as the only caller does not await it; it is sent while the connection sends another
  // message, so the params stay JSON (the stack is the JsonArray of CallOptions.frames) and the call is internal.
  WaitableResult<JsonElement> addStackToTracingNoReply(JsonObject params) {
    Connection.CallOptions options = new Connection.CallOptions();
    options.internal = true;
    return connection.sendMessageToServer(this, "addStackToTracingNoReply", params, options);
  }

  // java: the members below have no counterpart in localUtils.ts.
  // java: the raw initializer array, which the junit DeviceDescriptor reads.
  JsonArray deviceDescriptors() {
    return initializer.getAsJsonArray("deviceDescriptors");
  }

  // java: the Java API matches globs on the driver.
  public Pattern globToRegex(String glob, String baseURL, boolean webSocketUrl) {
    LocalUtilsGlobToRegexParams params = new LocalUtilsGlobToRegexParams();
    params.glob = glob;
    params.baseURL = baseURL;
    params.webSocketUrl = webSocketUrl;
    String regex = connection.localUtils().channel.globToRegex(params, NO_TIMEOUT).regex;
    return Pattern.compile(regex);
  }
}
