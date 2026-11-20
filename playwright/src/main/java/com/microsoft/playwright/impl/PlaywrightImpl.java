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

// Port of playwright.ts.
package com.microsoft.playwright.impl;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.impl.driver.Driver;
import com.microsoft.playwright.impl.protocol.*;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PlaywrightImpl extends ChannelOwner implements Playwright {
  // java: Android and Electron are not ported.
  final BrowserTypeImpl chromium;
  final BrowserTypeImpl firefox;
  final BrowserTypeImpl webkit;
  // java: no devices field, deviceDescriptors() reads them from LocalUtils.
  SelectorsImpl selectors;
  final APIRequestImpl request;
  // java: no errors field, TimeoutError is a public class. No _defaultLaunchOptions, _defaultContextTimeout and
  // _defaultContextNavigationTimeout, the test runner sets them upstream.

  final PlaywrightChannel channel = new PlaywrightChannel(this);

  PlaywrightImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    PlaywrightInitializer playwrightInitializer = connection.protocolGson().fromJson(initializer, PlaywrightInitializer.class);
    request = new APIRequestImpl(this);
    chromium = playwrightInitializer.chromium;
    chromium.playwright = this;
    firefox = playwrightInitializer.firefox;
    firefox.playwright = this;
    webkit = playwrightInitializer.webkit;
    webkit.playwright = this;
    selectors = new SelectorsImpl();
  }

  // java: no _browserTypes, _preLaunchedBrowser, _allContexts and _allPages, they serve the test runner upstream.

  // java: the entry point of the Java client and the getters of the Java API, playwright.ts has no counterpart.
  private Process driverProcess;

  public static PlaywrightImpl create(CreateOptions options) {
    return createImpl(options, false);
  }

  public static PlaywrightImpl createImpl(CreateOptions options, boolean forceNewDriverInstanceForTests) {
    Map<String, String> env = Collections.emptyMap();
    if (options != null && options.env != null) {
      env = options.env;
    }
    Driver driver = forceNewDriverInstanceForTests ?
      Driver.createAndInstall(env, true) :
      Driver.ensureDriverInstalled(env, true);
    try {
      ProcessBuilder pb = driver.createProcessBuilder();
      // java: the driver is installed once per JVM with the env of the first instance, so add the env of this one.
      pb.environment().putAll(env);
      pb.command().add("run-driver");
      pb.redirectError(ProcessBuilder.Redirect.INHERIT);
      Process p = pb.start();
      Connection connection = new Connection(new PipeTransport(p.getInputStream(), p.getOutputStream()), env);
      PlaywrightImpl result = connection.initializePlaywright();
      result.driverProcess = p;
      return result;
    } catch (IOException e) {
      throw new PlaywrightException("Failed to launch driver", e);
    }
  }

  public LocalUtils localUtils() {
    return connection.localUtils();
  }

  public JsonArray deviceDescriptors() {
    return localUtils().deviceDescriptors();
  }

  @Override
  public BrowserTypeImpl chromium() {
    return chromium;
  }

  @Override
  public BrowserTypeImpl firefox() {
    return firefox;
  }

  @Override
  public BrowserTypeImpl webkit() {
    return webkit;
  }

  @Override
  public SelectorsImpl selectors() {
    return selectors;
  }

  @Override
  public APIRequestImpl request() {
    return request;
  }

  @Override
  public void close() {
    try {
      connection.close();
      // java: the driver exits when its stdin is closed, wait for that.
      boolean didClose = driverProcess.waitFor(30, TimeUnit.SECONDS);
      if (!didClose) {
        System.err.println("WARNING: Timed out while waiting for driver process to exit");
      }
    } catch (IOException e) {
      throw new PlaywrightException("Failed to terminate", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new PlaywrightException("Operation interrupted", e);
    }
  }
}
