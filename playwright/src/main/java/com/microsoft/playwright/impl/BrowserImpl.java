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
import com.microsoft.playwright.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.Utils.convertViaJson;
import static com.microsoft.playwright.impl.Utils.isSafeCloseError;

class BrowserImpl extends ChannelOwner implements Browser {
  final Set<BrowserContext> contexts = new HashSet<>();
  private final ListenerCollection<EventType> listeners = new ListenerCollection<>();
  private boolean isConnected = true;

  enum EventType {
    DISCONNECTED,
  }

  BrowserImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  @Override
  public void onDisconnected(Runnable handler) {
    listeners.add(EventType.DISCONNECTED, handler);
  }

  @Override
  public void offDisconnected(Runnable handler) {
    listeners.remove(EventType.DISCONNECTED, handler);
  }

  @Override
  public void close() {
    withLogging("Browser.close", () -> closeImpl());
  }
  private void closeImpl() {
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
    return withLogging("Browser.newContext", () -> newContextImpl(options));
  }

  private BrowserContextImpl newContextImpl(NewContextOptions options) {
    if (options == null) {
      options = new NewContextOptions();
    }
    if (options.storageStatePath != null) {
      try (FileReader reader = new FileReader(options.storageStatePath.toFile())) {
        options.storageState = gson().fromJson(reader, BrowserContext.StorageState.class);
        options.storageStatePath = null;
      } catch (IOException e) {
        throw new PlaywrightException("Failed to read storage state from file", e);
      }
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    JsonElement result = sendMessage("newContext", params);
    BrowserContextImpl context = connection.getExistingObject(result.getAsJsonObject().getAsJsonObject("context").get("guid").getAsString());
    if (options.recordVideo != null) {
      context.videosDir = options.recordVideo.dir;
    }
    contexts.add(context);
    return context;
  }

  @Override
  public Page newPage(NewPageOptions options) {
    return withLogging("Browser.newPage", () -> newPageImpl(options));
  }

  private Page newPageImpl(NewPageOptions options) {
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
