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

// Port of browserType.ts.
package com.microsoft.playwright.impl;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.impl.protocol.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static com.microsoft.playwright.impl.Utils.convertType;
import static com.microsoft.playwright.impl.Utils.toProtocol;

public class BrowserTypeImpl extends ChannelOwner implements BrowserType {
  final Set<BrowserContextImpl> contexts = new LinkedHashSet<>();
  PlaywrightImpl playwright;

  final BrowserTypeChannel channel = new BrowserTypeChannel(this);

  BrowserTypeImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  @Override
  public String executablePath() {
    JsonElement executablePath = initializer.get("executablePath");
    if (executablePath == null || executablePath.isJsonNull() || executablePath.getAsString().isEmpty()) {
      throw new PlaywrightException("Browser is not supported on current platform");
    }
    return executablePath.getAsString();
  }

  @Override
  public String name() {
    return initializer.get("name").getAsString();
  }

  @Override
  public BrowserImpl launch(LaunchOptions options) {
    LaunchOptions launchOptions = options == null ? new LaunchOptions() : options;
    return wrapApiCall(() -> {
      // java: no userDataDir and port options to reject, and no _defaultLaunchOptions of the test runner.
      // ignoreDefaultArgs and ignoreAllDefaultArgs are separate options and env is serialized as a name/value array.
      BrowserTypeLaunchParams params = toProtocol(launchOptions, BrowserTypeLaunchParams.class);
      BrowserImpl browser = channel.launch(params, new TimeoutSettings().launchTimeout(launchOptions.timeout)).browser;
      browser.connectToBrowserType(this, launchOptions);
      return browser;
    }, null, false);
  }

  @Override
  public BrowserContextImpl launchPersistentContext(Path userDataDir, LaunchPersistentContextOptions options) {
    LaunchPersistentContextOptions persistentOptions = options == null ? new LaunchPersistentContextOptions() : options;
    return wrapApiCall(() -> {
      // java: the context part of the options goes through prepareBrowserContextParams, the launch part through
      // toProtocol of LaunchOptions; upstream spreads one options object into both.
      Browser.NewContextOptions contextOptions = convertType(persistentOptions, Browser.NewContextOptions.class);
      LaunchOptions launchOptions = convertType(persistentOptions, LaunchOptions.class);
      BrowserNewContextParams contextParams = BrowserContextImpl.prepareBrowserContextParams(contextOptions);
      // java: _withSelectorOptions, the Java options have no selector fields, so they are set on the params.
      contextParams.selectorEngines = new ArrayList<>(playwright.selectors.selectorEngines);
      contextParams.testIdAttributeName = playwright.selectors.testIdAttributeName;
      Gson gson = connection.protocolGson();
      JsonObject json = gson.toJsonTree(contextParams).getAsJsonObject();
      for (Map.Entry<String, JsonElement> entry : gson.toJsonTree(toProtocol(launchOptions, BrowserTypeLaunchParams.class)).getAsJsonObject().entrySet()) {
        json.add(entry.getKey(), entry.getValue());
      }
      BrowserTypeLaunchPersistentContextParams persistentParams = gson.fromJson(json, BrowserTypeLaunchPersistentContextParams.class);
      persistentParams.userDataDir = (userDataDir.isAbsolute() || userDataDir.toString().isEmpty()) ? userDataDir.toString() : userDataDir.toAbsolutePath().normalize().toString();
      BrowserTypeLaunchPersistentContextResult result = channel.launchPersistentContext(persistentParams, new TimeoutSettings().launchTimeout(persistentOptions.timeout));
      BrowserImpl browser = result.browser;
      browser.connectToBrowserType(this, launchOptions);
      BrowserContextImpl context = result.context;
      // java: the HAR options are the recordHar* fields of the options, upstream passes options.recordHar.
      context.initializeHarFromOptions(contextOptions);
      return context;
    }, null, false);
  }

  @Override
  public BrowserImpl connect(String endpoint, ConnectOptions options) {
    ConnectOptions params = options == null ? new ConnectOptions() : options;
    // java: _connect is inlined, its Java name would be this method's.
    return wrapApiCall(() -> {
      BrowserImpl browser = Connect.connectToBrowser(playwright, name(), endpoint, params);
      browser.connectToBrowserType(this, new LaunchOptions());
      return browser;
    }, null, false);
  }

  // java: only the endpointURL overload, the Java API has no options object with wsEndpoint and no ConnectOverCDPTransport.
  @Override
  public BrowserImpl connectOverCDP(String endpointURL, ConnectOverCDPOptions options) {
    if (options == null) {
      options = new ConnectOverCDPOptions();
    }
    if (endpointURL != null && !"chromium".equals(name()) && !"webkit".equals(name())) {
      throw new PlaywrightException("Connecting over CDP is only supported in Chromium and WebKit.");
    }

    BrowserTypeConnectOverCDPParams params = toProtocol(options, BrowserTypeConnectOverCDPParams.class);
    params.endpointURL = endpointURL;
    BrowserTypeConnectOverCDPResult result = channel.connectOverCDP(params, new TimeoutSettings().timeout(options.timeout));
    return browserFromConnectResult(result);
  }

  private BrowserImpl browserFromConnectResult(BrowserTypeConnectOverCDPResult result) {
    BrowserImpl browser = result.browser;
    browser.connectToBrowserType(this, new LaunchOptions());
    // java: no instrumentation to notify about result.defaultContext.
    return browser;
  }
}
