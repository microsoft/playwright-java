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

import com.google.gson.JsonObject;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Selectors;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

public class PlaywrightImpl extends ChannelOwner implements Playwright {
  private Process driverProcess;

  public static PlaywrightImpl create() {
    try {
      Path driver = Driver.ensureDriverInstalled();
      ProcessBuilder pb = new ProcessBuilder(driver.toString(), "run-driver");
      pb.redirectError(ProcessBuilder.Redirect.INHERIT);
//      pb.environment().put("DEBUG", "pw:pro*");
      Process p = pb.start();
      Connection connection = new Connection(new PipeTransport(p.getInputStream(), p.getOutputStream()));
      PlaywrightImpl result = (PlaywrightImpl) connection.waitForObjectWithKnownName("Playwright");
      result.driverProcess = p;
      return result;
    } catch (IOException e) {
      throw new PlaywrightException("Failed to launch driver", e);
    }
  }

  private final BrowserTypeImpl chromium;
  private final BrowserTypeImpl firefox;
  private final BrowserTypeImpl webkit;
  final SharedSelectors sharedSelectors = new SharedSelectors();;

  PlaywrightImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    chromium = parent.connection.getExistingObject(initializer.getAsJsonObject("chromium").get("guid").getAsString());
    firefox = parent.connection.getExistingObject(initializer.getAsJsonObject("firefox").get("guid").getAsString());
    webkit = parent.connection.getExistingObject(initializer.getAsJsonObject("webkit").get("guid").getAsString());
    SelectorsImpl channel = parent.connection.getExistingObject(initializer.getAsJsonObject("selectors").get("guid").getAsString());
    sharedSelectors.addChannel(channel);
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
  public Selectors selectors() {
    return sharedSelectors;
  }

  @Override
  public void close() {
    try {
      connection.close();
      // playwright-cli will exit when its stdin is closed, we wait for that.
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
