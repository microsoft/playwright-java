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
  private final String event;
  private final String title;

  WaitForEventLogger(ChannelOwner channelOwner, String apiName, String event, String title, Function<Logger, T> supplier) {
    this.supplier = supplier;
    this.channel = channelOwner;
    this.apiName = apiName;
    this.event = event;
    this.title = title;
    this.waitId = createGuid();
  }

  @Override
  public T get() {
    {
      JsonObject info = new JsonObject();
      info.addProperty("phase", "before");
      info.addProperty("event", event == null ? "" : event);
      sendWaitInfo(info, title);
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
    sendWaitInfo(info, null);
  }

  // The title goes with the first message of the wait, as in the Node.js client, where it is the
  // title of the api zone; an outer call that has a title of its own keeps it. It is set for that
  // message only, so that the calls made from the callback of the wait are not hidden as internal.
  private void sendWaitInfo(JsonObject info, String title) {
    info.addProperty("waitId", waitId);
    Connection connection = channel.connection;
    boolean setTitle = title != null && connection.title() == null;
    if (setTitle) {
      connection.setTitle(title);
    }
    try {
      channel.sendMessageNoReply("__waitInfo__", info);
    } catch (RuntimeException e) {
      // Fire-and-forget: never throw to the caller.
    } finally {
      if (setTitle) {
        connection.setTitle(null);
      }
    }
  }
}
