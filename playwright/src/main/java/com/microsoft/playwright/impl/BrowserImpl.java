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
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;

import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.Utils.convertViaJson;
import static com.microsoft.playwright.impl.Utils.isSafeCloseError;

class BrowserImpl extends ChannelOwner implements Browser {
  final Set<BrowserContextImpl> contexts = new HashSet<>();
  private final ListenerCollection<EventType> listeners = new ListenerCollection<>();
  boolean isRemote;
  boolean isConnectedOverWebSocket;
  private boolean isConnected = true;

  enum EventType {
    DISCONNECTED,
  }

  BrowserImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  @Override
  public void onDisconnected(Consumer<Browser> handler) {
    listeners.add(EventType.DISCONNECTED, handler);
  }

  @Override
  public void offDisconnected(Consumer<Browser> handler) {
    listeners.remove(EventType.DISCONNECTED, handler);
  }

  @Override
  public void close() {
    withLogging("Browser.close", () -> closeImpl());
  }

  private void closeImpl() {
    try {
      sendMessage("close");
      if (isConnectedOverWebSocket) {
        notifyRemoteClosed();
      }
    } catch (PlaywrightException e) {
      if (!isSafeCloseError(e)) {
        throw e;
      }
    }
  }

  void notifyRemoteClosed() {
    // Emulate all pages, contexts and the browser closing upon disconnect.
    for (BrowserContextImpl context : new ArrayList<>(contexts)) {
      for (PageImpl page : new ArrayList<>(context.pages)) {
        page.didClose();
      }
      context.didClose();
    }
    didClose();
    listeners.notify(EventType.DISCONNECTED, this);
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
      try {
        byte[] bytes = Files.readAllBytes(options.storageStatePath);
        options.storageState = new String(bytes, StandardCharsets.UTF_8);
        options.storageStatePath = null;
      } catch (IOException e) {
        throw new PlaywrightException("Failed to read storage state from file", e);
      }
    }
    JsonObject storageState = null;
    if (options.storageState != null) {
      storageState = new Gson().fromJson(options.storageState, JsonObject.class);
      options.storageState = null;
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    if (storageState != null) {
      params.add("storageState", storageState);
    }
    if (options.recordHarPath != null) {
      JsonObject recordHar = new JsonObject();
      recordHar.addProperty("path", options.recordHarPath.toString());
      if (options.recordHarOmitContent != null) {
        recordHar.addProperty("omitContent", true);
      }
      params.remove("recordHarPath");
      params.remove("recordHarOmitContent");
      params.add("recordHar", recordHar);
    } else if (options.recordHarOmitContent != null) {
      throw new PlaywrightException("recordHarOmitContent is set but recordHarPath is null");
    }
    if (options.recordVideoDir != null) {
      JsonObject recordVideo = new JsonObject();
      recordVideo.addProperty("dir", options.recordVideoDir.toString());
      if (options.recordVideoSize != null) {
        recordVideo.add("size", gson().toJsonTree(options.recordVideoSize));
      }
      params.remove("recordVideoDir");
      params.remove("recordVideoSize");
      params.add("recordVideo", recordVideo);
    } else if (options.recordVideoSize != null) {
      throw new PlaywrightException("recordVideoSize is set but recordVideoDir is null");
    }
    if (options.viewportSize != null) {
      if (options.viewportSize.isPresent()) {
        JsonElement size = params.get("viewportSize");
        params.remove("viewportSize");
        params.add("viewport", size);
      } else {
        params.remove("viewportSize");
        params.addProperty("noDefaultViewport", true);
      }
    }
    params.addProperty("sdkLanguage", "java");
    JsonElement result = sendMessage("newContext", params);
    BrowserContextImpl context = connection.getExistingObject(result.getAsJsonObject().getAsJsonObject("context").get("guid").getAsString());
    if (options.recordVideoDir != null) {
      context.videosDir = options.recordVideoDir;
    }
    contexts.add(context);
    return context;
  }

  @Override
  public Page newPage(NewPageOptions options) {
    return withLogging("Browser.newPage", () -> newPageImpl(options));
  }

  @Override
  public void startTracing(Page page, StartTracingOptions options) {
    withLogging("Browser.startTracing", () -> startTracingImpl(page, options));
  }

  private void startTracingImpl(Page page, StartTracingOptions options) {
    if (options == null) {
      options = new StartTracingOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    if (page != null) {
      JsonObject jsonPage = new JsonObject();
      jsonPage.addProperty("guid", ((PageImpl) page).guid);
      params.add("page", jsonPage);
    }
    sendMessage("startTracing", params);
  }

  @Override
  public byte[] stopTracing() {
    return withLogging("Browser.stopTracing", () -> stopTracingImpl());
  }

  private byte[] stopTracingImpl() {
    JsonObject json = sendMessage("stopTracing").getAsJsonObject();
    return Base64.getDecoder().decode(json.get("binary").getAsString());
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
      didClose();
    }
  }

  private void didClose() {
    isConnected = false;
    // Avoid notifying the event because this also happens to the response of
    // close event sent in the message and in case of remote connection it will
    // lead to closing the websocket connection and later will throw exception.
    if (!isConnectedOverWebSocket) {
      listeners.notify(EventType.DISCONNECTED, this);
    }
  }
}
