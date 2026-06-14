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

import java.util.function.Function;
import java.util.function.Supplier;

import static com.microsoft.playwright.impl.Utils.createGuid;

public class WaitForEventLogger<T> implements Supplier<T>, Logger {
  private final Function<Logger, T> supplier;
  private final ChannelOwner channel;
  private final String waitId;
  private final String apiName;

  WaitForEventLogger(ChannelOwner channelOwner, String apiName, Function<Logger, T> supplier) {
    this.supplier = supplier;
    this.channel = channelOwner;
    this.apiName = apiName;
    this.waitId = createGuid();
  }

  @Override
  public T get() {
    {
      JsonObject info = new JsonObject();
      info.addProperty("phase", "before");
      info.addProperty("event", "");
      sendWaitInfo(info);
    }
    JsonObject info = new JsonObject();
    info.addProperty("phase", "after");
    try {
      return supplier.apply(this);
    } catch (RuntimeException e) {
      info.addProperty("error", e.getMessage());
      throw e;
    } finally {
      sendWaitInfo(info);
    }
  }

  @Override
  public void log(String message) {
    LoggingSupport.logApiIfEnabled(message);
    JsonObject info = new JsonObject();
    info.addProperty("phase", "log");
    info.addProperty("message", message);
    sendWaitInfo(info);
  }

  private void sendWaitInfo(JsonObject info) {
    info.addProperty("waitId", waitId);
    try {
      channel.sendMessageNoReply("__waitInfo__", info);
    } catch (RuntimeException e) {
      // Fire-and-forget: never throw to the caller.
    }
  }
}
