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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.PlaywrightException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

class ChannelOwner extends LoggingSupport {
  final Connection connection;
  private ChannelOwner parent;
  private final Map<String, ChannelOwner> objects = new HashMap<>();

  final String type;
  final String guid;
  final JsonObject initializer;
  private boolean wasCollected;

  static Double NO_TIMEOUT = null;

  protected ChannelOwner(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    this(parent.connection, parent, type, guid, initializer);
  }

  protected ChannelOwner(Connection connection, String type, String guid) {
    this(connection, null, type, guid, new JsonObject());
  }


  private ChannelOwner(Connection connection, ChannelOwner parent, String type, String guid, JsonObject initializer) {
    this.connection = connection;
    this.parent = parent;
    this.type = type;
    this.guid = guid;
    this.initializer = initializer;

    connection.registerObject(guid, this);
    if (parent != null) {
      parent.objects.put(guid, this);
    }
  }

  void disposeChannelOwner(boolean wasGarbageCollected) {
    // Clean up from parent and connection.
    if (parent != null) {
      parent.objects.remove(guid);
    }
    connection.unregisterObject(guid);
    wasCollected = wasGarbageCollected;
    // Dispose all children.
    for (ChannelOwner child : new ArrayList<>(objects.values())) {
      child.disposeChannelOwner(wasGarbageCollected);
    }
    objects.clear();
  }

  void adopt(ChannelOwner child) {
    child.parent.objects.remove(child.guid);
    objects.put(child.guid, child);
    child.parent = this;
  }

  <T> T withWaitLogging(String apiName, Function<Logger, T> code) {
    return new WaitForEventLogger<>(this, apiName, code).get();
  }


  void withTitle(String title, Runnable code) {
    withTitle(title, () -> {
      code.run();
      return null;
    });
  }

  <T> T withTitle(String title, Supplier<T> code) {
    String previousTitle = connection.setTitle(title);
    try {
      return code.get();
    } finally {
      connection.setTitle(previousTitle);
    }
  }

  WaitableResult<JsonElement> sendMessageAsync(String method) {
    return sendMessageAsync(method, new JsonObject());
  }

  WaitableResult<JsonElement> sendMessageAsync(String method, JsonObject params) {
    checkNotCollected();
    return connection.sendMessageAsync(guid, method, params);
  }

  JsonElement sendMessage(String method) {
    return sendMessage(method, new JsonObject(), NO_TIMEOUT);
  }

  JsonElement sendMessage(String method, JsonObject params, Double timeout) {
    checkNotCollected();
    if (timeout != null) {
      params.addProperty("timeout", timeout);
    } else if (params.has("timeout")) {
      throw new PlaywrightException("Internal error: timeout must be passed explicitly.");
    }
    return connection.sendMessage(guid, method, params);
  }

  private void checkNotCollected() {
    if (wasCollected)
      throw new PlaywrightException("The object has been collected to prevent unbounded heap growth.");
  }

  <T> T runUntil(Runnable code, Waitable<T> waitable) {
    try {
      code.run();
      while (!waitable.isDone()) {
        connection.processOneMessage();
      }
      return waitable.get();
    } finally {
      waitable.dispose();
    }
  }

  void handleEvent(String event, JsonObject parameters) {
  }

  JsonObject toProtocolRef() {
    JsonObject json = new JsonObject();
    json.addProperty("guid", guid);
    return json;
  }
}
