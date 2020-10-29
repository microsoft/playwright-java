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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.microsoft.playwright.impl.Utils.convertViaJson;
import static com.microsoft.playwright.impl.Utils.isSafeCloseError;

class BrowserImpl extends ChannelOwner implements Browser {
  final Set<BrowserContext> contexts = new HashSet<>();
  private final ListenerCollection<EventType> listeners = new ListenerCollection<>();
  private boolean isConnected = true;

  BrowserImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  @Override
  public void addListener(EventType type, Listener<EventType> listener) {
    listeners.add(type, listener);
  }

  @Override
  public void removeListener(EventType type, Listener<EventType> listener) {
    listeners.remove(type, listener);
  }

  @Override
  public void close() {
    try {
      sendMessage("close");
    } catch (PlaywrightException e) {
      if (!isSafeCloseError(e)) {
        throw e;
      }
    }
  }

  @Override
  public List<BrowserContext> contexts() {
    return new ArrayList<>(contexts);
  }

  @Override
  public boolean isConnected() {
    return isConnected;
  }

  @Override
  public BrowserContextImpl newContext(NewContextOptions options) {
    if (options == null) {
      options = new NewContextOptions();
    }
    JsonObject params = new Gson().toJsonTree(options).getAsJsonObject();
    if (options.extraHTTPHeaders != null) {
      params.remove("extraHTTPHeaders");
      params.add("extraHTTPHeaders", Serialization.toProtocol(options.extraHTTPHeaders));
    }
    JsonElement result = sendMessage("newContext", params);
    BrowserContextImpl context = connection.getExistingObject(result.getAsJsonObject().getAsJsonObject("context").get("guid").getAsString());
    contexts.add(context);
    return context;
  }

  @Override
  public Page newPage(NewPageOptions options) {
    BrowserContextImpl context = newContext(convertViaJson(options, NewContextOptions.class));
    PageImpl page = context.newPage();
    page.ownedContext = context;
    context.ownerPage = page;
    return page;
  }

  private String name() {
    return initializer.get("name").getAsString();
  }

  boolean isChromium() {
    return "chromium".equals(name());
  }

  @Override
  public String version() {
    return initializer.get("version").getAsString();
  }

  @Override
  void handleEvent(String event, JsonObject parameters) {
    if ("close".equals(event)) {
      isConnected = false;
      listeners.notify(EventType.DISCONNECTED, null);
    }
  }
}
