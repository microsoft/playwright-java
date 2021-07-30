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

import java.util.function.Supplier;

import static com.microsoft.playwright.impl.Utils.createGuid;

public class WaitForEventLogger<T> implements Supplier<T> {
  private final Supplier<T> supplier;
  private final ChannelOwner channel;
  private final String waitId;
  private final String apiName;

  WaitForEventLogger(ChannelOwner channelOwner, String apiName, Supplier<T> supplier) {
    this.supplier = supplier;
    this.channel = channelOwner;
    this.apiName = apiName;
    this.waitId = createGuid();
    JsonObject info = new JsonObject();
    info.addProperty("phase", "before");
    sendWaitForEventInfo(info);
  }

  @Override
  public T get() {
    JsonObject info = new JsonObject();
    info.addProperty("phase", "after");
    try {
      return supplier.get();
    } catch (RuntimeException e) {
      info.addProperty("error", e.getMessage());
      throw e;
    } finally {
      sendWaitForEventInfo(info);
    }
  }

  private void sendWaitForEventInfo(JsonObject info) {
    info.addProperty("event", "");
    info.addProperty("waitId", waitId);
    JsonObject params = new JsonObject();
    params.add("info", info);
    channel.withLogging(apiName, () -> channel.sendMessageAsync("waitForEventInfo", params));
  }
}
