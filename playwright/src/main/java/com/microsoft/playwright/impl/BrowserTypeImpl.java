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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.PlaywrightException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.time.Duration;

import static com.microsoft.playwright.impl.Serialization.gson;

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
    return connection.getExistingObject(result.getAsJsonObject().getAsJsonObject("browser").get("guid").getAsString());
  }

  @Override
  public Browser connect(String wsEndpoint, ConnectOptions options) {
    return withLogging("BrowserType.connect", () -> connectImpl(wsEndpoint, options));
  }

  private Browser connectImpl(String wsEndpoint, ConnectOptions options) {
    try {
      Duration timeout = Duration.ofDays(1);
      if (options != null && options.timeout != null) {
        timeout = Duration.ofMillis(Math.round(options.timeout));
      }
      Connection connection = new Connection(new WebSocketTransport(new URI(wsEndpoint), timeout));
      RemoteBrowser remoteBrowser = (RemoteBrowser) connection.waitForObjectWithKnownName("remoteBrowser");
      PlaywrightImpl playwright = this.connection.getExistingObject("Playwright");
      SelectorsImpl selectors = remoteBrowser.selectors();
      playwright.sharedSelectors.addChannel(selectors);
      BrowserImpl browser = remoteBrowser.browser();
      browser.isRemote = true;
      browser.onDisconnected(b -> {
        playwright.sharedSelectors.removeChannel(selectors);
        try {
          connection.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
      return browser;
    } catch (URISyntaxException e) {
      throw new PlaywrightException("Failed to connect", e);
    }
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
    }
    JsonObject params = gson().toJsonTree(options).getAsJsonObject();
    params.addProperty("userDataDir", userDataDir.toString());
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
    JsonObject json = sendMessage("launchPersistentContext", params).getAsJsonObject();
    BrowserContextImpl context = connection.getExistingObject(json.getAsJsonObject("context").get("guid").getAsString());
    if (options.recordVideoDir != null) {
      context.videosDir = options.recordVideoDir;
    }
    return context;
  }

  public String name() {
    return initializer.get("name").getAsString();
  }

}
