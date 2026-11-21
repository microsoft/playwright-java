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

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static com.microsoft.playwright.impl.Utils.convertType;
import static com.microsoft.playwright.impl.Utils.toProtocol;

public class BrowserTypeImpl extends ChannelOwner implements BrowserType {
  // java: LaunchServerOptions has no Java type, the launch options stand in for it.
  interface BrowserServerLauncher {
    BrowserServer launchServer(LaunchOptions options);
  }

  // This is here just for api generation and checking.
  // java: ChildProcess is a java.lang.Process.
  interface BrowserServer {
    Process process();

    String wsEndpoint();

    void close();

    void kill();
  }

  BrowserServerLauncher serverLauncher;
  final Set<BrowserContextImpl> contexts = new LinkedHashSet<>();
  PlaywrightImpl playwright;

  final BrowserTypeChannel channel = new BrowserTypeChannel(this);

  BrowserTypeImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  // java: from is not ported, generated results already hold the impl object.

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
    LaunchOptions userOptions = options == null ? new LaunchOptions() : options;
    return wrapApiCall(() -> {
      // java: the userDataDir and port asserts are not ported, LaunchOptions has no such fields.

      // java: LaunchOptions has no logger, so options.logger and _defaultLaunchOptions?.logger are always unset.
      Object logger = null;
      LaunchOptions launchOptions = withDefaults(playwright.defaultLaunchOptions, userOptions, LaunchOptions.class);
      // java: ignoreDefaultArgs and ignoreAllDefaultArgs are separate options and env is serialized as a name/value array.
      BrowserTypeLaunchParams params = toProtocol(launchOptions, BrowserTypeLaunchParams.class);
      BrowserImpl browser = channel.launch(params, new TimeoutSettings().launchTimeout(launchOptions.timeout)).browser;
      browser.connectToBrowserType(this, launchOptions, logger);
      return browser;
    }, null, false);
  }

  BrowserServer launchServer(LaunchOptions options) {
    if (options == null) {
      options = new LaunchOptions();
    }
    if (serverLauncher == null) {
      throw new PlaywrightException("Launching server is not supported");
    }
    options = withDefaults(playwright.defaultLaunchOptions, options, LaunchOptions.class);
    return serverLauncher.launchServer(options);
  }

  @Override
  public BrowserContextImpl launchPersistentContext(Path userDataDir, LaunchPersistentContextOptions options) {
    LaunchPersistentContextOptions userOptions = options == null ? new LaunchPersistentContextOptions() : options;
    return wrapApiCall(() -> {
      // java: the port assert is not ported, LaunchPersistentContextOptions has no such field.
      // java: _withSelectorOptions, the Java options have no selector fields, so they are set on the params below.
      LaunchPersistentContextOptions persistentOptions = withDefaults(playwright.defaultLaunchOptions, userOptions, LaunchPersistentContextOptions.class);
      instrumentation.runBeforeCreateBrowserContext(persistentOptions);

      // java: LaunchPersistentContextOptions has no logger, so options.logger and _defaultLaunchOptions?.logger are always unset.
      Object logger = null;
      // java: the context part of the options goes through prepareBrowserContextParams, the launch part through
      // toProtocol of LaunchOptions; upstream spreads one options object into both.
      Browser.NewContextOptions contextOptions = convertType(persistentOptions, Browser.NewContextOptions.class);
      LaunchOptions launchOptions = convertType(persistentOptions, LaunchOptions.class);
      BrowserNewContextParams contextParams = BrowserContextImpl.prepareBrowserContextParams(contextOptions);
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
      browser.connectToBrowserType(this, launchOptions, logger);
      BrowserContextImpl context = result.context;
      // java: the HAR options are the recordHar* fields of the options, upstream passes options.recordHar.
      context.initializeHarFromOptions(contextOptions);
      instrumentation.runAfterCreateBrowserContext(context);
      return context;
    }, null, false);
  }

  // java: only the endpoint overload, the Java API has no options object with wsEndpoint, so there is no
  // 'options.wsEndpoint is required' assert.
  @Override
  public BrowserImpl connect(String endpoint, ConnectOptions options) {
    ConnectOptions params = options == null ? new ConnectOptions() : options;
    return wrapApiCall(() -> connectImpl(endpoint, params), null, false);
  }

  // java: _connect is connectImpl, connect(endpoint, options) is the public method; upstream's params is one
  // ConnectOptions object with the endpoint in it.
  BrowserImpl connectImpl(String endpoint, ConnectOptions params) {
    BrowserImpl browser = Connect.connectToBrowser(playwright, name(), endpoint, params);
    browser.connectToBrowserType(this, new LaunchOptions(), null);
    return browser;
  }

  // java: only the endpointURL overload, the Java API has no options object with wsEndpoint and no
  // ConnectOverCDPTransport, so the isConnectionTransport branch and the wsEndpoint assert are not ported.
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
    browser.connectToBrowserType(this, new LaunchOptions(), null);
    if (result.defaultContext != null) {
      instrumentation.runAfterCreateBrowserContext(result.defaultContext);
    }
    return browser;
  }

  // java: the { timeout } options are a plain parameter.
  WorkerImpl connectToWorker(String endpoint, Double timeout) {
    if (!"chromium".equals(name())) {
      throw new PlaywrightException("Connecting to workers is only supported in Chromium.");
    }
    BrowserTypeConnectToWorkerParams params = new BrowserTypeConnectToWorkerParams();
    params.endpoint = endpoint;
    BrowserTypeConnectToWorkerResult result = channel.connectToWorker(params, new TimeoutSettings().timeout(timeout));
    return result.worker;
  }

  // java: isConnectionTransport is not ported, it is a JS typing check (typeof value.send === 'function') for an
  // overload the Java API does not have.

  // java: the spread { ...defaults, ...options }, the fields set in options win; without defaults the options
  // object itself is returned.
  private static <T> T withDefaults(LaunchOptions defaults, T options, Class<T> optionsClass) {
    if (defaults == null) {
      return options;
    }
    T result = convertType(defaults, optionsClass);
    try {
      for (Field field : optionsClass.getDeclaredFields()) {
        if (field.isSynthetic()) {
          continue;
        }
        Object value = field.get(options);
        if (value != null) {
          field.set(result, value);
        }
      }
    } catch (IllegalAccessException e) {
      throw new PlaywrightException("Internal error", e);
    }
    return result;
  }
}
