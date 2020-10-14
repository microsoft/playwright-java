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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.Deferred;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

class ChannelOwner {
  final Connection connection;
  private final ChannelOwner parent;
  private final Map<String, ChannelOwner> objects = new HashMap();

  final String type;
  final String guid;
//  private T channel;
  final JsonObject initializer;

  Map<String, ArrayList<CompletableFuture<JsonObject>>> futureEvents = new HashMap<>();

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
    if (parent != null)
      parent.objects.put(guid, this);
  }

  public void dispose() {
  }

  Deferred<JsonElement> sendMessageAsync(String method, JsonObject params) {
    return connection.sendMessageAsync(guid, method, params);
  }

  JsonElement sendMessage(String method, JsonObject params) {
    return connection.sendMessage(guid, method, params);
  }

  void sendMessageNoWait(String method, JsonObject params) {
    connection.sendMessageNoWait(guid, method, params);
  }

  CompletableFuture<JsonObject> futureForEvent(String event) {
    ArrayList<CompletableFuture<JsonObject>> futures = futureEvents.get(event);
    if (futures == null) {
      futures = new ArrayList<>();
      futureEvents.put(event, futures);
    }
    CompletableFuture<JsonObject> result = new CompletableFuture<>();
    futures.add(result);
    return result;
  }

  <T> Deferred<T> toDeferred(Waitable waitable) {
    return () -> {
      while (!waitable.isDone()) {
        connection.processOneMessage();
      }
      return (T) waitable.get();
    };
  }

  <T> T waitForCompletion(CompletableFuture<T> future) {
    while (!future.isDone()) {
      connection.processOneMessage();
    }
    // TODO: ensure it's been removed from futureEvents
    try {
      return future.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  final void onEvent(String event, JsonObject parameters) {
    handleEvent(event, parameters);
    ArrayList<CompletableFuture<JsonObject>> futures = futureEvents.remove(event);
    if (futures == null) {
      return;
    }
    for (CompletableFuture<JsonObject> f : futures) {
      f.complete(parameters);
    }
  }

  protected void handleEvent(String event, JsonObject params) {
  }

}
