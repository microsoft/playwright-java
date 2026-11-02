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

import com.microsoft.playwright.impl.protocol.*;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.BindResult;
import com.microsoft.playwright.options.ClientCertificate;
import com.microsoft.playwright.options.HttpCredentials;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.Utils.*;
import static com.microsoft.playwright.impl.Errors.isSafeCloseError;

public class BrowserImpl extends ChannelOwner implements Browser {
  // Insertion order, so that browser.contexts() lists the contexts in creation order like the other clients.
  final Set<BrowserContextImpl> contexts = new LinkedHashSet<>();
  private final ListenerCollection<EventType> listeners = new ListenerCollection<>();
  boolean isConnectedOverWebSocket;
  private boolean isConnected = true;
  BrowserTypeImpl browserType;
  BrowserType.LaunchOptions launchOptions;
  private Path tracePath;
  String closeReason;

  enum EventType {
    CONTEXT,
    DISCONNECTED,
  }

  final BrowserChannel channel = new BrowserChannel(this);

  BrowserImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  @Override
  public void onContext(Consumer<BrowserContext> handler) {
    listeners.add(EventType.CONTEXT, handler);
  }

  @Override
  public void offContext(Consumer<BrowserContext> handler) {
    listeners.remove(EventType.CONTEXT, handler);
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
    if (options == null) {
      options = new CloseOptions();
    }
    closeReason = options.reason;
    if (isConnectedOverWebSocket) {
      try {
        // java: the message of a call that fails because of this, the default close reason of JsonPipe.
        connection.close("Browser has been closed");
      } catch (IOException e) {
        throw new PlaywrightException("Failed to close browser connection", e);
      }
      return;
    }
    try {
      channel.close(new BrowserCloseParams(), NO_TIMEOUT);
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
    if (options == null) {
      options = new NewContextOptions();
    } else {
      // Make a copy so that we can nullify some fields below.
      options = convertType(options, NewContextOptions.class);
    }

    NewContextOptions harOptions = Utils.clone(options);
    options.recordHarContent = null;
    options.recordHarMode = null;
    options.recordHarPath = null;
    options.recordHarOmitContent = null;
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
    BrowserNewContextParams.BrowserNewContextParamsStorageState storageState = null;
    if (options.storageState != null) {
      storageState = connection.protocolGson().fromJson(options.storageState, BrowserNewContextParams.BrowserNewContextParamsStorageState.class);
      options.storageState = null;
    }
    // These two are converted below, their option types differ from the protocol ones.
    Object httpCredentials = options.httpCredentials;
    options.httpCredentials = null;
    List<ClientCertificate> clientCertificates = options.clientCertificates;
    options.clientCertificates = null;
    BrowserNewContextParams params = toProtocol(options, BrowserNewContextParams.class);
    params.storageState = storageState;
    if (options.recordVideoDir != null) {
      BrowserNewContextParams.BrowserNewContextParamsRecordVideo recordVideo = new BrowserNewContextParams.BrowserNewContextParamsRecordVideo();
      recordVideo.dir = options.recordVideoDir.toAbsolutePath().toString();
      if (options.recordVideoSize != null) {
        recordVideo.size = new BrowserNewContextParams.BrowserNewContextParamsRecordVideo.BrowserNewContextParamsRecordVideoSize();
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
        params.viewport = new BrowserNewContextParams.BrowserNewContextParamsViewport();
        params.viewport.width = options.viewportSize.get().width;
        params.viewport.height = options.viewportSize.get().height;
      } else {
        params.noDefaultViewport = true;
      }
    }
    if (options.screenSize != null) {
      params.screen = new BrowserNewContextParams.BrowserNewContextParamsScreen();
      params.screen.width = options.screenSize.width;
      params.screen.height = options.screenSize.height;
    }
    params.clientCertificates = clientCertificatesToProtocol(clientCertificates);
    params.httpCredentials = httpCredentialsToProtocol(httpCredentials);
    params.acceptDownloads = options.acceptDownloads == null ? null : (options.acceptDownloads ? "accept" : "deny");
    params.selectorEngines = new ArrayList<>(browserType.playwright.selectors.selectorEngines);
    params.testIdAttributeName = browserType.playwright.selectors.testIdAttributeName;
    BrowserContextImpl context = channel.newContext(params, NO_TIMEOUT).context;
    context.initializeHarFromOptions(harOptions);
    return context;
  }

  @Override
  public Page newPage(NewPageOptions options) {
    return withTitle("Create Page", () -> newPageImpl(options));
  }

  @Override
  public void startTracing(Page page, StartTracingOptions options) {
    if (options == null) {
      options = new StartTracingOptions();
    }
    tracePath = options.path;
    BrowserStartTracingParams params = toProtocol(options, BrowserStartTracingParams.class);
    if (page != null) {
      params.page = (PageImpl) page;
    }
    channel.startTracing(params, NO_TIMEOUT);
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
  public byte[] stopTracing() {
    ArtifactImpl artifact = channel.stopTracing(NO_TIMEOUT).artifact;
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

  // The protocol takes a list of http credentials, the option is a single value or a list.
  @SuppressWarnings("unchecked")
  static List<com.microsoft.playwright.impl.protocol.HttpCredentials> httpCredentialsToProtocol(Object httpCredentials) {
    if (httpCredentials == null) {
      return null;
    }
    List<HttpCredentials> list = new ArrayList<>();
    if (httpCredentials instanceof List) {
      list.addAll((List<HttpCredentials>) httpCredentials);
    } else {
      list.add((HttpCredentials) httpCredentials);
    }
    if (list.isEmpty()) {
      return null;
    }
    List<com.microsoft.playwright.impl.protocol.HttpCredentials> result = new ArrayList<>();
    for (HttpCredentials credentials : list) {
      com.microsoft.playwright.impl.protocol.HttpCredentials c = new com.microsoft.playwright.impl.protocol.HttpCredentials();
      c.username = credentials.username;
      c.password = credentials.password;
      c.origin = credentials.origin;
      c.send = credentials.send == null ? null : credentials.send.name().toLowerCase();
      result.add(c);
    }
    return result;
  }

  static List<BrowserNewContextParams.BrowserNewContextParamsClientCertificates> clientCertificatesToProtocol(List<ClientCertificate> clientCertificates) {
    if (clientCertificates == null) {
      return null;
    }
    List<BrowserNewContextParams.BrowserNewContextParamsClientCertificates> result = new ArrayList<>();
    for (ClientCertificate cert : clientCertificates) {
      BrowserNewContextParams.BrowserNewContextParamsClientCertificates c = new BrowserNewContextParams.BrowserNewContextParamsClientCertificates();
      c.origin = cert.origin;
      try {
        c.cert = cert.certPath != null ? Files.readAllBytes(cert.certPath) : cert.cert;
        c.key = cert.keyPath != null ? Files.readAllBytes(cert.keyPath) : cert.key;
        c.pfx = cert.pfxPath != null ? Files.readAllBytes(cert.pfxPath) : cert.pfx;
      } catch (IOException e) {
        throw new PlaywrightException("Failed to read from file", e);
      }
      c.passphrase = cert.passphrase;
      result.add(c);
    }
    return result;
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
    return channel.newBrowserCDPSession(NO_TIMEOUT).session;
  }

  protected void connectToBrowserType(BrowserTypeImpl browserType, Path tracesDir){
    // Note: when using connect(), `browserType` is different from `this.parent`.
    // This is why browser type is not wired up in the constructor, and instead this separate method is called later on.
    this.browserType = browserType;
    this.tracePath = tracesDir;

    for (BrowserContextImpl context : contexts) {
      context.setTracesDir(tracesDir);
      browserType.playwright.selectors.contextsForSelectors.add(context);
    }
  }

  private void didCreateContext(BrowserContextImpl context) {
    context.browser = this;
    contexts.add(context);
    // Note: when connecting to a browser, initial contexts arrive before `_browserType` is set,
    // and will be configured later in `ConnectToBrowserType`.
    if (browserType != null) {
      context.setTracesDir(tracePath);
      browserType.playwright.selectors.contextsForSelectors.add(context);
    }
    listeners.notify(EventType.CONTEXT, context);
  }

  private void didClose() {
    isConnected = false;
    listeners.notify(EventType.DISCONNECTED, this);
  }
}
