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
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.HarContentPolicy;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

import static com.microsoft.playwright.impl.Serialization.addHarUrlFilter;
import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.Utils.convertType;

class BrowserTypeImpl extends ChannelOwner implements BrowserType {
  BrowserTypeImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  @Override
  public BrowserImpl launch(LaunchOptions options) {
    return withLogging("BrowserType.launch", () -> launchImpl(options));
  }

  private BrowserImpl launchImpl(LaunchOptions options) {
    if (options == null) {
      options = new LaunchOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    JsonElement result = sendMessage("launch", params);
    BrowserImpl browser = connection.getExistingObject(result.getAsJsonObject().getAsJsonObject("browser").get("guid").getAsString());
    browser.browserType = this;
    browser.launchOptions = options;
    return browser;
  }

  @Override
  public Browser connect(String wsEndpoint, ConnectOptions options) {
    return withLogging("BrowserType.connect", () -> connectImpl(wsEndpoint, options));
  }

  private Browser connectImpl(String wsEndpoint, ConnectOptions options) {
    if (options == null) {
      options = new ConnectOptions();
    }
    // We don't use gson() here as the headers map should be serialized to a json object.
    JsonObject params = new Gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("wsEndpoint", wsEndpoint);

    if (!params.has("headers")) {
      params.add("headers", new JsonObject());
    }
    JsonObject headers = params.get("headers").getAsJsonObject();
    boolean foundBrowserHeader = false;
    for (String name : headers.keySet()) {
      if ("x-playwright-browser".equalsIgnoreCase(name)) {
        foundBrowserHeader = true;
        break;
      }
    }
    if (!foundBrowserHeader) {
      headers.addProperty("x-playwright-browser", name());
    }

    JsonObject json = connection.localUtils().sendMessage("connect", params).getAsJsonObject();
    JsonPipe pipe = connection.getExistingObject(json.getAsJsonObject("pipe").get("guid").getAsString());
    Connection connection = new Connection(pipe, this.connection.env, this.connection.localUtils);
    PlaywrightImpl playwright = connection.initializePlaywright();
    if (!playwright.initializer.has("preLaunchedBrowser")) {
      try {
        connection.close();
      } catch (IOException e) {
        e.printStackTrace(System.err);
      }
      throw new PlaywrightException("Malformed endpoint. Did you use launchServer method?");
    }
    playwright.initSharedSelectors(this.connection.getExistingObject("Playwright"));
    BrowserImpl browser = connection.getExistingObject(playwright.initializer.getAsJsonObject("preLaunchedBrowser").get("guid").getAsString());
    browser.isConnectedOverWebSocket = true;
    browser.browserType = this;
    Consumer<JsonPipe> connectionCloseListener = t -> browser.notifyRemoteClosed();
    pipe.onClose(connectionCloseListener);
    browser.onDisconnected(b -> {
      playwright.unregisterSelectors();
      pipe.offClose(connectionCloseListener);
      try {
        connection.close();
      } catch (IOException e) {
        e.printStackTrace(System.err);
      }
    });
    return browser;
  }

  @Override
  public Browser connectOverCDP(String endpointURL, ConnectOverCDPOptions options) {
    if (!"chromium".equals(name())) {
      throw new PlaywrightException("Connecting over CDP is only supported in Chromium.");
    }
    return withLogging("BrowserType.connectOverCDP", () -> connectOverCDPImpl(endpointURL, options));
  }

  private Browser connectOverCDPImpl(String endpointURL, ConnectOverCDPOptions options) {
    if (options == null) {
      options = new ConnectOverCDPOptions();
    }

    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("endpointURL", endpointURL);
    JsonObject json = sendMessage("connectOverCDP", params).getAsJsonObject();

    BrowserImpl browser = connection.getExistingObject(json.getAsJsonObject("browser").get("guid").getAsString());
    browser.browserType = this;
    if (json.has("defaultContext")) {
      String contextId = json.getAsJsonObject("defaultContext").get("guid").getAsString();
      BrowserContextImpl defaultContext = connection.getExistingObject(contextId);
      browser.contexts.add(defaultContext);
    }
    return browser;
  }

  public String executablePath() {
    return initializer.get("executablePath").getAsString();
  }

  @Override
  public BrowserContextImpl launchPersistentContext(Path userDataDir, LaunchPersistentContextOptions options) {
    return withLogging("BrowserType.launchPersistentContext",
      () -> launchPersistentContextImpl(userDataDir, options));
  }

  private BrowserContextImpl launchPersistentContextImpl(Path userDataDir, LaunchPersistentContextOptions options) {
    if (options == null) {
      options = new LaunchPersistentContextOptions();
    } else {
      // Make a copy so that we can nullify some fields below.
      options = convertType(options, LaunchPersistentContextOptions.class);
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
        recordHar.addProperty("mode", options.recordHarMode.toString().toLowerCase());
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
    params.addProperty("userDataDir", userDataDir.toString());
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
    JsonObject json = sendMessage("launchPersistentContext", params).getAsJsonObject();
    BrowserContextImpl context = connection.getExistingObject(json.getAsJsonObject("context").get("guid").getAsString());
    context.videosDir = options.recordVideoDir;
    if (options.baseURL != null) {
      context.setBaseUrl(options.baseURL);
    }
    context.setRecordHar(recordHarPath, harContentPolicy);
    context.tracing().setTracesDir(options.tracesDir);
    return context;
  }

  public String name() {
    return initializer.get("name").getAsString();
  }

}
