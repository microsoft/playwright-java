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

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.Utils.addToProtocol;
import static com.microsoft.playwright.impl.Utils.convertType;

class BrowserTypeImpl extends ChannelOwner implements BrowserType {
  protected PlaywrightImpl playwright;

  BrowserTypeImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  @Override
  public BrowserImpl launch(LaunchOptions options) {
    if (options == null) {
      options = new LaunchOptions();
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    JsonElement result = sendMessage("launch", params, TimeoutSettings.launchTimeout(options.timeout));
    BrowserImpl browser = connection.getExistingObject(result.getAsJsonObject().getAsJsonObject("browser").get("guid").getAsString());
    browser.browserType = this;
    browser.launchOptions = options;
    return browser;
  }

  @Override
  public Browser connect(String wsEndpoint, ConnectOptions options) {
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

    if (options.launchOptions != null && !headers.has("x-playwright-launch-options")) {
      String launchOptionsJson = new Gson().toJsonTree(options.launchOptions).toString();
      headers.addProperty("x-playwright-launch-options", launchOptionsJson);
    }

    Double timeout = options.timeout;
    if (timeout == null) {
      timeout = 0.0;
    }

    JsonObject json = connection.localUtils().sendMessage("connect", params, timeout).getAsJsonObject();
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
    playwright.selectors = this.playwright.selectors;
    BrowserImpl browser = connection.getExistingObject(playwright.initializer.getAsJsonObject("preLaunchedBrowser").get("guid").getAsString());
    browser.isConnectedOverWebSocket = true;
    browser.connectToBrowserType(this, null);
    Consumer<JsonPipe> connectionCloseListener = t -> browser.notifyRemoteClosed();
    pipe.onClose(connectionCloseListener);
    browser.onDisconnected(b -> {
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
    if (options == null) {
      options = new ConnectOverCDPOptions();
    }

    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("endpointURL", endpointURL);
    JsonObject json = sendMessage("connectOverCDP", params, TimeoutSettings.launchTimeout(options.timeout)).getAsJsonObject();

    BrowserImpl browser = connection.getExistingObject(json.getAsJsonObject("browser").get("guid").getAsString());
    browser.connectToBrowserType(this, null);
    return browser;
  }

  public String executablePath() {
    return initializer.get("executablePath").getAsString();
  }

  @Override
  public BrowserContextImpl launchPersistentContext(Path userDataDir, LaunchPersistentContextOptions options) {
    if (options == null) {
      options = new LaunchPersistentContextOptions();
    } else {
      // Make a copy so that we can nullify some fields below.
      options = convertType(options, LaunchPersistentContextOptions.class);
    }

    Browser.NewContextOptions harOptions = convertType(options, Browser.NewContextOptions.class);
    options.recordHarContent = null;
    options.recordHarMode = null;
    options.recordHarPath = null;
    options.recordHarOmitContent = null;
    options.recordHarUrlFilter = null;

    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    if (!userDataDir.isAbsolute() && !userDataDir.toString().isEmpty()) {
      Path cwd = Paths.get("").toAbsolutePath();
      userDataDir = cwd.resolve(userDataDir);
    }
    params.addProperty("userDataDir", userDataDir.toString());
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
    params.add("selectorEngines", gson().toJsonTree(playwright.selectors.selectorEngines));
    params.addProperty("testIdAttributeName", playwright.selectors.testIdAttributeName);
    JsonObject json = sendMessage("launchPersistentContext", params, TimeoutSettings.launchTimeout(options.timeout)).getAsJsonObject();
    BrowserImpl browser = connection.getExistingObject(json.getAsJsonObject("browser").get("guid").getAsString());
    browser.connectToBrowserType(this, options.tracesDir);
    BrowserContextImpl context = connection.getExistingObject(json.getAsJsonObject("context").get("guid").getAsString());
    context.initializeHarFromOptions(harOptions);
    context.tracing().setTracesDir(options.tracesDir);
    return context;
  }

  public String name() {
    return initializer.get("name").getAsString();
  }

}
