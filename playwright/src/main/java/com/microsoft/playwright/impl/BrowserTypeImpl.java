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
import com.google.gson.JsonObject;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.ClientCertificate;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.microsoft.playwright.impl.BrowserImpl.clientCertificatesToProtocol;
import static com.microsoft.playwright.impl.BrowserImpl.httpCredentialsToProtocol;
import static com.microsoft.playwright.impl.Utils.convertType;
import static com.microsoft.playwright.impl.Utils.toProtocol;

class BrowserTypeImpl extends ChannelOwner implements BrowserType {
  protected PlaywrightImpl playwright;

  final BrowserTypeChannel channel = new BrowserTypeChannel(this);

  BrowserTypeImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  @Override
  public BrowserImpl launch(LaunchOptions options) {
    if (options == null) {
      options = new LaunchOptions();
    }
    BrowserTypeLaunchParams params = toProtocol(options, BrowserTypeLaunchParams.class);
    BrowserImpl browser = channel.launch(params, TimeoutSettings.launchTimeout(options.timeout)).browser;
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
    // Serializing the options object itself (not field by field) keeps fields added by subclasses, like the tests do.
    JsonObject optionsJson = new Gson().toJsonTree(options).getAsJsonObject();
    LocalUtilsConnectParams params = connection.protocolGson().fromJson(optionsJson, LocalUtilsConnectParams.class);
    params.endpoint = wsEndpoint;
    JsonObject headers = options.headers == null ? new JsonObject() : optionsJson.getAsJsonObject("headers");
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
    params.headers = headers;

    Double timeout = options.timeout;
    if (timeout == null) {
      timeout = 0.0;
    }

    JsonPipe pipe = connection.localUtils().channel.connect(params, timeout).pipe;
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
    if (!"chromium".equals(name()) && !"webkit".equals(name())) {
      throw new PlaywrightException("Connecting over CDP is only supported in Chromium and WebKit.");
    }
    if (options == null) {
      options = new ConnectOverCDPOptions();
    }

    BrowserTypeConnectOverCDPParams params = toProtocol(options, BrowserTypeConnectOverCDPParams.class);
    params.endpointURL = endpointURL;
    BrowserImpl browser = channel.connectOverCDP(params, TimeoutSettings.launchTimeout(options.timeout)).browser;
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

    // These two are converted below, their option types differ from the protocol ones.
    Object httpCredentials = options.httpCredentials;
    options.httpCredentials = null;
    List<ClientCertificate> clientCertificates = options.clientCertificates;
    options.clientCertificates = null;
    BrowserTypeLaunchPersistentContextParams params = toProtocol(options, BrowserTypeLaunchPersistentContextParams.class);
    if (!userDataDir.isAbsolute() && !userDataDir.toString().isEmpty()) {
      Path cwd = Paths.get("").toAbsolutePath();
      userDataDir = cwd.resolve(userDataDir);
    }
    params.userDataDir = userDataDir.toString();
    if (options.recordVideoDir != null) {
      BrowserTypeLaunchPersistentContextParams.BrowserTypeLaunchPersistentContextParamsRecordVideo recordVideo = new BrowserTypeLaunchPersistentContextParams.BrowserTypeLaunchPersistentContextParamsRecordVideo();
      recordVideo.dir = options.recordVideoDir.toAbsolutePath().toString();
      if (options.recordVideoSize != null) {
        recordVideo.size = new BrowserTypeLaunchPersistentContextParams.BrowserTypeLaunchPersistentContextParamsRecordVideo.BrowserTypeLaunchPersistentContextParamsRecordVideoSize();
        recordVideo.size.width = options.recordVideoSize.width;
        recordVideo.size.height = options.recordVideoSize.height;
      }
      if (options.recordVideoFps != null) {
        recordVideo.fps = options.recordVideoFps;
      }
      params.recordVideo = recordVideo;
    } else if (options.recordVideoSize != null) {
      throw new PlaywrightException("recordVideoSize is set but recordVideoDir is null");
    } else if (options.recordVideoFps != null) {
      throw new PlaywrightException("recordVideoFps is set but recordVideoDir is null");
    }
    if (options.viewportSize != null) {
      if (options.viewportSize.isPresent()) {
        params.viewport = new BrowserTypeLaunchPersistentContextParams.BrowserTypeLaunchPersistentContextParamsViewport();
        params.viewport.width = options.viewportSize.get().width;
        params.viewport.height = options.viewportSize.get().height;
      } else {
        params.noDefaultViewport = true;
      }
    }
    params.clientCertificates = clientCertificatesToProtocol(connection, clientCertificates, BrowserTypeLaunchPersistentContextParams.BrowserTypeLaunchPersistentContextParamsClientCertificates.class);
    params.httpCredentials = httpCredentialsToProtocol(connection, httpCredentials);
    params.acceptDownloads = options.acceptDownloads == null ? null : (options.acceptDownloads ? "accept" : "deny");
    params.selectorEngines = new ArrayList<>(playwright.selectors.selectorEngines);
    params.testIdAttributeName = playwright.selectors.testIdAttributeName;
    BrowserTypeLaunchPersistentContextResult result = channel.launchPersistentContext(params, TimeoutSettings.launchTimeout(options.timeout));
    BrowserImpl browser = result.browser;
    browser.connectToBrowserType(this, options.tracesDir);
    BrowserContextImpl context = result.context;
    context.initializeHarFromOptions(harOptions);
    context.setTracesDir(options.tracesDir);
    return context;
  }

  public String name() {
    return initializer.get("name").getAsString();
  }

}
