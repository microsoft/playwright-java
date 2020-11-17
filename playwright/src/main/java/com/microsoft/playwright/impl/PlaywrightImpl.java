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
import java.util.*;

public class PlaywrightImpl extends ChannelOwner implements Playwright {
  private static Path driverTempDir;

  public static PlaywrightImpl create() {
    try {
      Path driver = ensureDriverExtracted();
      ProcessBuilder pb = new ProcessBuilder(driver.toString(), "run-driver");
      pb.redirectError(ProcessBuilder.Redirect.INHERIT);
//      pb.environment().put("DEBUG", "pw:pro*");
      Process p = pb.start();
      Connection connection = new Connection(p.getInputStream(), p.getOutputStream());
      return (PlaywrightImpl) connection.waitForObjectWithKnownName("Playwright");
    } catch (IOException e) {
      throw new PlaywrightException("Failed to launch driver", e);
    }
  }

  private static Path ensureDriverExtracted() {
    if (driverTempDir == null) {
      try {
        driverTempDir = Files.createTempDirectory("playwright-java-");
        driverTempDir.toFile().deleteOnExit();
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        Path path = Paths.get(classloader.getResource("driver").toURI());
        Files.list(path).forEach(filePath -> {
          try {
            extractResource(filePath, driverTempDir);
          } catch (IOException e) {
            throw new RuntimeException("Failed to extract driver from " + path, e);
          }
        });
      } catch (IOException | URISyntaxException e) {
        throw new PlaywrightException("Failed to launch driver", e);
      }
    }
    // TODO: remove dir on exit
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

  public PlaywrightImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
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

  public void close() {
    try {
      connection.close();
    } catch (IOException e) {
      throw new PlaywrightException("Failed to close", e);
    }
  }
}
