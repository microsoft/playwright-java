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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Selectors;
import com.microsoft.playwright.impl.driver.Driver;

import java.io.*;
import java.nio.channels.ByteChannel;
import java.nio.channels.Channels;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PlaywrightImpl extends ChannelOwner implements Playwright {
  private Closeable driverCloseable;

  public static PlaywrightImpl create(CreateOptions options) {
    return createImpl(options, false);
  }

  public static PlaywrightImpl createImpl(CreateOptions options, boolean forceNewDriverInstanceForTests) {
    Map<String, String> env = getEnv(options);

    Driver driver = forceNewDriverInstanceForTests ?
      Driver.createAndInstall(env, true) :
      Driver.ensureDriverInstalled(env, true);

    ProcessBuilder pb = driver.createProcessBuilder();
    pb.command().add("run-driver");

    return createFromProcessBuilder(env, pb);
  }

  public static PlaywrightImpl createWithThirdPartyDriver(CreateOptions options, Driver.ThirdPartyDriver driver) {
    Map<String, String> env = getEnv(options);

    if (driver instanceof Driver.ByteChannelDriver) {
      Driver.ByteChannelDriver byteChannelDriver = (Driver.ByteChannelDriver) driver;
      return createFromByteChannel(env, byteChannelDriver);
    } if (driver instanceof Driver.ExternalProcessDriver) {
      Driver.ExternalProcessDriver externalProcessDriver = (Driver.ExternalProcessDriver) driver;
      ProcessBuilder pb = externalProcessDriver.createProcessBuilder();
      return createFromProcessBuilder(env, pb);
    } else {
      throw new PlaywrightException("Unsupported 3rd party driver type: " + driver.getClass().getName());
    }
  }

  private static PlaywrightImpl createFromByteChannel(Map<String, String> env, Driver.ByteChannelDriver byteChannelDriver) {
    ByteChannel channel = byteChannelDriver.createByteChannel();
    InputStream in = Channels.newInputStream(channel);
    OutputStream out = Channels.newOutputStream(channel);
    Closeable closeable = () -> {
        in.close();
        out.close();
        channel.close();
    };
    return createWithStreams(env, in, out, closeable);
  }

  private static PlaywrightImpl createWithStreams(Map<String, String> env,
                                                  InputStream in,
                                                  OutputStream out,
                                                  Closeable driverCloseable) {
    Connection connection = new Connection(new PipeTransport(in, out), env);
    PlaywrightImpl result = connection.initializePlaywright();
    result.driverCloseable = driverCloseable;
    return result;
  }

  private static PlaywrightImpl createFromProcessBuilder(Map<String, String> env, ProcessBuilder pb) {
    try {
      pb.redirectError(ProcessBuilder.Redirect.INHERIT);

      //noinspection resource it is wrapped to closeable lambda
      Process p = pb.start();
      return createWithStreams(env, p.getInputStream(), p.getOutputStream(), () -> {
        // playwright-cli will exit when its stdin is closed, we wait for that.
        try {
          boolean didClose = p.waitFor(30, TimeUnit.SECONDS);
          if (!didClose) {
            System.err.println("WARNING: Timed out while waiting for driver process to exit");
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          throw new PlaywrightException("Operation interrupted", e);
        }
      });
    } catch (IOException e) {
      throw new PlaywrightException("Failed to launch driver", e);
    }
  }

  private static Map<String, String> getEnv(CreateOptions options) {
    Map<String, String> env = Collections.emptyMap();
    if (options != null && options.env != null) {
      env = options.env;
    }
    return env;
  }

  private final BrowserTypeImpl chromium;
  private final BrowserTypeImpl firefox;
  private final BrowserTypeImpl webkit;
  private final APIRequestImpl apiRequest;
  protected SelectorsImpl selectors;

  PlaywrightImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    chromium = parent.connection.getExistingObject(initializer.getAsJsonObject("chromium").get("guid").getAsString());
    firefox = parent.connection.getExistingObject(initializer.getAsJsonObject("firefox").get("guid").getAsString());
    webkit = parent.connection.getExistingObject(initializer.getAsJsonObject("webkit").get("guid").getAsString());

    chromium.playwright = this;
    firefox.playwright = this;
    webkit.playwright = this;

    selectors = new SelectorsImpl();
    apiRequest = new APIRequestImpl(this);
  }

  public LocalUtils localUtils() {
    return connection.localUtils;
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
  public APIRequest request() {
    return apiRequest;
  }

  @Override
  public BrowserTypeImpl webkit() {
    return webkit;
  }

  @Override
  public Selectors selectors() {
    return selectors;
  }

  @Override
  public void close() {
    try {
      connection.close();
      if (driverCloseable != null) driverCloseable.close();
    } catch (IOException e) {
      throw new PlaywrightException("Failed to terminate", e);
    }
  }
}
