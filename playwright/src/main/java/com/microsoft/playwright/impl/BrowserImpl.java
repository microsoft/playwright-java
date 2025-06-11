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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

import static com.microsoft.playwright.impl.Serialization.addHarUrlFilter;
import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.Utils.*;

class BrowserImpl extends ChannelOwner implements Browser {
  final Set<BrowserContextImpl> contexts = new HashSet<>();
  private final ListenerCollection<EventType> listeners = new ListenerCollection<>();
  boolean isConnectedOverWebSocket;
  private boolean isConnected = true;
  BrowserTypeImpl browserType;
  BrowserType.LaunchOptions launchOptions;
  private Path tracesDir;
  String closeReason;

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
  public void close(CloseOptions options) {
    withLogging("Browser.close", () -> closeImpl(options));
  }

  private void closeImpl(CloseOptions options) {
    if (options == null) {
      options = new CloseOptions();
    }
    closeReason = options.reason;
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
    Object recordHarUrlFilter = options.recordHarUrlFilter;
    options.recordHarUrlFilter = null;
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
    addToProtocol(params, options.clientCertificates);
    params.remove("acceptDownloads");
    if (options.acceptDownloads != null) {
      params.addProperty("acceptDownloads", options.acceptDownloads ? "accept" : "deny");
    }
    params.add("selectorEngines", gson().toJsonTree(browserType.playwright.sharedSelectors.selectorEngines));
    params.addProperty("testIdAttributeName", browserType.playwright.sharedSelectors.testIdAttributeName);
    JsonElement result = sendMessage("newContext", params);
    BrowserContextImpl context = connection.getExistingObject(result.getAsJsonObject().getAsJsonObject("context").get("guid").getAsString());
    context.videosDir = options.recordVideoDir;
    if (options.baseURL != null) {
      context.setBaseUrl(options.baseURL);
    }
    options.recordHarUrlFilter = recordHarUrlFilter;
    context.initializeHarFromOptions(options);
    return context;
  }

  @Override
  public Page newPage(NewPageOptions options) {
    return withTitle("Create Page", () -> newPageImpl(options));
  }

  @Override
  public void startTracing(Page page, StartTracingOptions options) {
    withLogging("Browser.startTracing", () -> startTracingImpl(page, options));
  }

  private void startTracingImpl(Page page, StartTracingOptions options) {
    if (options == null) {
      options = new StartTracingOptions();
    }
    tracesDir = options.path;
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
    if (tracesDir != null) {
      try {
        Files.createDirectories(tracesDir.getParent());
        Files.write(tracesDir, data);
      } catch (IOException e) {
        throw new PlaywrightException("Failed to write trace file", e);
      } finally {
        tracesDir = null;
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
    switch (event) {
      case "context":
        didCreateContext(connection.getExistingObject(parameters.getAsJsonObject("context").get("guid").getAsString()));
        break;
      case "close":
        didClose();
        break;
    }
  }

  @Override
  public CDPSession newBrowserCDPSession() {
    JsonObject params = new JsonObject();
    JsonObject result = sendMessage("newBrowserCDPSession", params).getAsJsonObject();
    return connection.getExistingObject(result.getAsJsonObject("session").get("guid").getAsString());
  }

  protected void connectToBrowserType(BrowserTypeImpl browserType, Path tracesDir){
    // Note: when using connect(), `browserType` is different from `this.parent`.
    // This is why browser type is not wired up in the constructor, and instead this separate method is called later on.
    this.browserType = browserType;
    this.tracesDir = tracesDir;

    for (BrowserContextImpl context : contexts) {
      context.tracing().setTracesDir(tracesDir);
      browserType.playwright.sharedSelectors.contextsForSelectors.add(context);
    }
  }

  private void didCreateContext(BrowserContextImpl context) {
    context.browser = this;
    contexts.add(context);
    // Note: when connecting to a browser, initial contexts arrive before `_browserType` is set,
    // and will be configured later in `ConnectToBrowserType`.
    if (browserType != null) {
      context.tracing().setTracesDir(tracesDir);
      browserType.playwright.sharedSelectors.contextsForSelectors.add(context);
    }
  }

  private void didClose() {
    isConnected = false;
    listeners.notify(EventType.DISCONNECTED, this);
  }
}
