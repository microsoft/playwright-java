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
import com.microsoft.playwright.DeviceDescriptor;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Selectors;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PlaywrightImpl extends ChannelOwner implements Playwright {
  private static Path driverTempDir;
  private Process driverProcess;

  public static PlaywrightImpl create() {
    try {
      Path driver = ensureDriverInstalled();
      ProcessBuilder pb = new ProcessBuilder(driver.toString(), "run-driver");
      pb.redirectError(ProcessBuilder.Redirect.INHERIT);
//      pb.environment().put("DEBUG", "pw:pro*");
      Process p = pb.start();
      Connection connection = new Connection(p.getInputStream(), p.getOutputStream());
      PlaywrightImpl result = (PlaywrightImpl) connection.waitForObjectWithKnownName("Playwright");
      result.driverProcess = p;
      System.out.println("started driver");
      return result;
    } catch (IOException | InterruptedException | URISyntaxException e) {
      throw new PlaywrightException("Failed to launch driver", e);
    }
  }

  private static synchronized Path ensureDriverInstalled() throws IOException, InterruptedException, URISyntaxException {
    if (driverTempDir == null) {
      driverTempDir = Files.createTempDirectory("playwright-java-");
      driverTempDir.toFile().deleteOnExit();
      ClassLoader classloader = Thread.currentThread().getContextClassLoader();
      Path path = Paths.get(classloader.getResource("driver").toURI());
      Files.list(path).forEach(filePath -> {
        try {
          extractResource(filePath, driverTempDir);
        } catch (IOException e) {
          throw new PlaywrightException("Failed to extract driver from " + path, e);
        }
      });

      System.out.println("calling playwright-cli install");
      Path driver = driverTempDir.resolve("playwright-cli");
      ProcessBuilder pb = new ProcessBuilder(driver.toString(), "install");
      pb.redirectError(ProcessBuilder.Redirect.INHERIT);
      Process p = pb.start();
      int result = p.waitFor();
      System.out.println("done playwright-cli install: " + result);
    }
    return driverTempDir.resolve("playwright-cli");
  }

  private static Path extractResource(Path from, Path toDir) throws IOException {
    Path path = toDir.resolve(from.getFileName());
    Files.copy(from, path);
    path.toFile().setExecutable(true);
    path.toFile().deleteOnExit();
    System.out.println("extracting: " + from.toString() + " to " + path.toString());
    return path;
  }

  private final BrowserTypeImpl chromium;
  private final BrowserTypeImpl firefox;
  private final BrowserTypeImpl webkit;
  private final Selectors selectors;
  private final Map<String, DeviceDescriptor> devices = new HashMap<>();

  PlaywrightImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    chromium = parent.connection.getExistingObject(initializer.getAsJsonObject("chromium").get("guid").getAsString());
    firefox = parent.connection.getExistingObject(initializer.getAsJsonObject("firefox").get("guid").getAsString());
    webkit = parent.connection.getExistingObject(initializer.getAsJsonObject("webkit").get("guid").getAsString());
    selectors = parent.connection.getExistingObject(initializer.getAsJsonObject("selectors").get("guid").getAsString());

    Gson gson = Serialization.gson();
    for (JsonElement item : initializer.getAsJsonArray("deviceDescriptors")) {
      JsonObject o = item.getAsJsonObject();
      String name = o.get("name").getAsString();
      DeviceDescriptorImpl descriptor = gson.fromJson(o.get("descriptor"), DeviceDescriptorImpl.class);
      devices.put(name, descriptor);
    }
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
  public Map<String, DeviceDescriptor> devices() {
    return devices;
  }

  @Override
  public Selectors selectors() {
    return selectors;
  }

  @Override
  public void close() throws Exception {
    System.out.println("closing playwright");
    try {
      connection.close();
      System.out.println("closed connection");
    } finally {
      driverProcess.destroy();
      System.out.println("called driverProcess.destroy");
      boolean didClose = driverProcess.waitFor(30, TimeUnit.SECONDS);
      System.out.println("did close process: " + didClose);
      if (!didClose) {
        System.err.println("WARNING: Timed out while waiting for driver to process to exit");
      }
    }
  }
}
