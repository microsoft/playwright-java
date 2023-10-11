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
import com.microsoft.playwright.options.HarContentPolicy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

import static com.microsoft.playwright.impl.Serialization.addHarUrlFilter;
import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.Utils.*;
import static com.microsoft.playwright.impl.Utils.convertType;

class BrowserImpl extends ChannelOwner implements Browser {
  final Set<BrowserContextImpl> contexts = new HashSet<>();
  private final ListenerCollection<EventType> listeners = new ListenerCollection<>();
  boolean isConnectedOverWebSocket;
  private boolean isConnected = true;
  BrowserTypeImpl browserType;
  BrowserType.LaunchOptions launchOptions;
  private Path tracePath;

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
  public BrowserType browserType() {
    return browserType;
  }

  @Override
  public void close() {
    withLogging("Browser.close", () -> closeImpl());
  }

  private void closeImpl() {
    if (isConnectedOverWebSocket) {
      try {
        connection.close();
      } catch (IOException e) {
        throw new PlaywrightException("Failed to close browser connection", e);
      }
      return;
    }
    try {
      sendMessage("close");
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
    } else {
      // Make a copy so that we can nullify some fields below.
      options = convertType(options, NewContextOptions.class);
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
    JsonObject recordHar = null;
    Path recordHarPath = options.recordHarPath;
    HarContentPolicy harContentPolicy = null;
    if (options.recordHarPath != null) {
      recordHar = new JsonObject();
      recordHar.addProperty("path", options.recordHarPath.toString());
      if (options.recordHarContent != null) {
        harContentPolicy = options.recordHarContent;
      } else if (options.recordHarOmitContent != null && options.recordHarOmitContent) {
        harContentPolicy = HarContentPolicy.OMIT;
      }
      if (harContentPolicy != null) {
        recordHar.addProperty("content", harContentPolicy.name().toLowerCase());
      }
      if (options.recordHarMode != null) {
        recordHar.addProperty("mode", options.recordHarMode.name().toLowerCase());
      }
      addHarUrlFilter(recordHar, options.recordHarUrlFilter);
      options.recordHarPath = null;
      options.recordHarMode = null;
      options.recordHarOmitContent = null;
      options.recordHarContent = null;
      options.recordHarUrlFilter = null;
    } else {
      if (options.recordHarOmitContent != null) {
        throw new PlaywrightException("recordHarOmitContent is set but recordHarPath is null");
      }
      if (options.recordHarUrlFilter != null) {
        throw new PlaywrightException("recordHarUrlFilter is set but recordHarPath is null");
      }
      if (options.recordHarMode != null) {
        throw new PlaywrightException("recordHarMode is set but recordHarPath is null");
      }
      if (options.recordHarContent != null) {
        throw new PlaywrightException("recordHarContent is set but recordHarPath is null");
      }
    }

    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    if (storageState != null) {
      params.add("storageState", storageState);
    }
    if (recordHar != null) {
      params.add("recordHar", recordHar);
    }
    if (options.recordVideoDir != null) {
      JsonObject recordVideo = new JsonObject();
      recordVideo.addProperty("dir", options.recordVideoDir.toAbsolutePath().toString());
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
    params.remove("acceptDownloads");
    if (options.acceptDownloads != null) {
      params.addProperty("acceptDownloads", options.acceptDownloads ? "accept" : "deny");
    }
    JsonElement result = sendMessage("newContext", params);
    BrowserContextImpl context = connection.getExistingObject(result.getAsJsonObject().getAsJsonObject("context").get("guid").getAsString());
    context.videosDir = options.recordVideoDir;
    if (options.baseURL != null) {
      context.setBaseUrl(options.baseURL);
    }
    context.setRecordHar(recordHarPath, harContentPolicy);
    if (launchOptions != null) {
      context.tracing().setTracesDir(launchOptions.tracesDir);
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
    tracePath = options.path;
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    if (page != null) {
      params.add("page", ((PageImpl) page).toProtocolRef());
    }
    sendMessage("startTracing", params);
  }

  @Override
  public byte[] stopTracing() {
    return withLogging("Browser.stopTracing", () -> stopTracingImpl());
  }

  private byte[] stopTracingImpl() {
    JsonObject json = sendMessage("stopTracing").getAsJsonObject();
    ArtifactImpl artifact = connection.getExistingObject(json.getAsJsonObject().getAsJsonObject("artifact").get("guid").getAsString());
    byte[] data = artifact.readAllBytes();
    artifact.delete();
    if (tracePath != null) {
      try {
        Files.createDirectories(tracePath.getParent());
        Files.write(tracePath, data);
      } catch (IOException e) {
        throw new PlaywrightException("Failed to write trace file", e);
      } finally {
        tracePath = null;
      }
    }
    return data;
  }

  private Page newPageImpl(NewPageOptions options) {
    BrowserContextImpl context = newContext(convertType(options, NewContextOptions.class));
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

  @Override
  public CDPSession newBrowserCDPSession() {
    JsonObject params = new JsonObject();
    JsonObject result = sendMessage("newBrowserCDPSession", params).getAsJsonObject();
    return connection.getExistingObject(result.getAsJsonObject("session").get("guid").getAsString());
  }

  private void didClose() {
    isConnected = false;
    listeners.notify(EventType.DISCONNECTED, this);
  }
}
