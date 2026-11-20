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

// Port of browser.ts.
package com.microsoft.playwright.impl;

import com.google.gson.JsonObject;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.impl.protocol.*;
import com.microsoft.playwright.options.BindResult;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static com.microsoft.playwright.impl.Errors.isSafeCloseError;
import static com.microsoft.playwright.impl.Errors.isTargetClosedError;
import static com.microsoft.playwright.impl.FileUtils.mkdirIfNeeded;
import static com.microsoft.playwright.impl.Utils.convertType;
import static com.microsoft.playwright.impl.Utils.toProtocol;
import static com.microsoft.playwright.impl.Utils.writeToFile;

public class BrowserImpl extends ChannelOwner implements Browser {
  final Set<BrowserContextImpl> contexts = new LinkedHashSet<>();
  private boolean isConnected = true;
  // java: no _closedPromise, close() waits until isConnected turns false, which happens when Disconnected is emitted.
  boolean shouldCloseConnectionOnClose = false;
  BrowserTypeImpl browserType;
  private BrowserType.LaunchOptions options = new BrowserType.LaunchOptions();
  final String name;
  final String browserName;
  private Path path;
  String closeReason;

  final BrowserChannel channel = new BrowserChannel(this);

  BrowserImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    BrowserInitializer browserInitializer = connection.protocolGson().fromJson(initializer, BrowserInitializer.class);
    name = browserInitializer.name;
    browserName = browserInitializer.browserName;
  }

  @Override
  void handleEvent(String event, JsonObject params) {
    switch (event) {
      case "context":
        didCreateContext(connection.protocolGson().fromJson(params, BrowserContextEvent.class).context);
        break;
      case "close":
        didClose();
        break;
    }
  }

  @Override
  public BrowserType browserType() {
    return browserType;
  }

  @Override
  public BrowserContextImpl newContext(NewContextOptions options) {
    return innerNewContext(options);
  }

  // java: no forReuse parameter, newContextForReuse is not in the Java API.
  BrowserContextImpl innerNewContext(NewContextOptions userOptions) {
    if (userOptions == null) {
      userOptions = new NewContextOptions();
    }
    BrowserNewContextParams contextOptions = BrowserContextImpl.prepareBrowserContextParams(userOptions);
    // java: _withSelectorOptions, the Java options have no selector fields, so they are set on the params.
    contextOptions.selectorEngines = new ArrayList<>(browserType.playwright.selectors.selectorEngines);
    contextOptions.testIdAttributeName = browserType.playwright.selectors.testIdAttributeName;
    BrowserContextImpl context = channel.newContext(contextOptions, NO_TIMEOUT).context;
    // java: the HAR options are the recordHar* fields of the options, upstream passes options.recordHar.
    context.initializeHarFromOptions(userOptions);
    return context;
  }

  // java: no logger parameter, the Java API has no logger option.
  void connectToBrowserType(BrowserTypeImpl browserType, BrowserType.LaunchOptions browserOptions) {
    // Note: when using connect(), `browserType` is different from `this._parent`.
    // This is why browser type is not wired up in the constructor,
    // and instead this separate method is called later on.
    this.browserType = browserType;
    this.options = browserOptions;
    for (BrowserContextImpl context : contexts) {
      setupBrowserContext(context);
    }
  }

  private void didCreateContext(BrowserContextImpl context) {
    context.browser = this;
    contexts.add(context);
    // Note: when connecting to a browser, initial contexts arrive before `browserType` is set,
    // and will be configured later in `_connectToBrowserType`.
    if (browserType != null) {
      setupBrowserContext(context);
    }
    emit(Events.Browser.CONTEXT, context);
  }

  private void setupBrowserContext(BrowserContextImpl context) {
    context.tracing.tracesDir = options.tracesDir;
    context.request.tracing.tracesDir = options.tracesDir;
    browserType.contexts.add(context);
    browserType.playwright.selectors.contextsForSelectors.add(context);
    // java: no _defaultContextTimeout and _defaultContextNavigationTimeout, the test runner sets them upstream.
  }

  @Override
  public List<BrowserContext> contexts() {
    return new ArrayList<>(contexts);
  }

  @Override
  public String version() {
    return initializer.get("version").getAsString();
  }

  @Override
  public BindResult bind(String title, BindOptions options) {
    BrowserStartServerParams params = toProtocol(options, BrowserStartServerParams.class);
    params.title = title;
    BrowserStartServerResult result = channel.startServer(params, NO_TIMEOUT);
    BindResult bindResult = new BindResult();
    bindResult.endpoint = result.endpoint;
    return bindResult;
  }

  @Override
  public void unbind() {
    channel.stopServer(NO_TIMEOUT);
  }

  @Override
  public PageImpl newPage(NewPageOptions options) {
    return withTitle("Create page", () -> {
      BrowserContextImpl context = newContext(convertType(options, NewContextOptions.class));
      PageImpl page = (PageImpl) context.newPage();
      page.ownedContext = context;
      context.ownerPage = page;
      return page;
    });
  }

  @Override
  public boolean isConnected() {
    return isConnected;
  }

  @Override
  public CDPSessionImpl newBrowserCDPSession() {
    return channel.newBrowserCDPSession(NO_TIMEOUT).session;
  }

  @Override
  public void startTracing(Page page, StartTracingOptions options) {
    if (options == null) {
      options = new StartTracingOptions();
    }
    path = options.path;
    BrowserStartTracingParams params = toProtocol(options, BrowserStartTracingParams.class);
    params.page = (PageImpl) page;
    channel.startTracing(params, NO_TIMEOUT);
  }

  @Override
  public byte[] stopTracing() {
    ArtifactImpl artifact = channel.stopTracing(NO_TIMEOUT).artifact;
    // java: readAllBytes is readIntoBuffer.
    byte[] buffer = artifact.readAllBytes();
    artifact.delete();
    if (path != null) {
      mkdirIfNeeded(path);
      writeToFile(buffer, path);
      path = null;
    }
    return buffer;
  }

  @Override
  public void close(CloseOptions options) {
    if (options == null) {
      options = new CloseOptions();
    }
    closeReason = options.reason;
    try {
      if (shouldCloseConnectionOnClose) {
        // java: the reason is what a pending call of the connection fails with, the default close reason of JsonPipe.
        closeConnection("Browser has been closed");
      } else {
        channel.close(toProtocol(options, BrowserCloseParams.class), NO_TIMEOUT);
      }
      runUntil(() -> {}, new WaitablePredicate<Void>(() -> !isConnected));
    } catch (PlaywrightException e) {
      // java: also the safe close errors of the driver, which do not all arrive as TargetClosedError.
      if (isTargetClosedError(e) || isSafeCloseError(e)) {
        return;
      }
      throw e;
    }
  }

  void didClose() {
    isConnected = false;
    emit(Events.Browser.DISCONNECTED, this);
  }

  // java: typed listeners of the Java API.
  @Override
  public void onContext(Consumer<BrowserContext> handler) {
    on(Events.Browser.CONTEXT, handler);
  }

  @Override
  public void offContext(Consumer<BrowserContext> handler) {
    off(Events.Browser.CONTEXT, handler);
  }

  @Override
  public void onDisconnected(Consumer<Browser> handler) {
    on(Events.Browser.DISCONNECTED, handler);
  }

  @Override
  public void offDisconnected(Consumer<Browser> handler) {
    off(Events.Browser.DISCONNECTED, handler);
  }

  // java: Connection.close declares IOException.
  private void closeConnection(String reason) {
    try {
      connection.close(reason);
    } catch (IOException e) {
      throw new PlaywrightException("Failed to close browser connection", e);
    }
  }
}
