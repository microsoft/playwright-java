/**
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
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;

public class PlaywrightImpl extends ChannelOwner implements Playwright {
  public static PlaywrightImpl create() {
    try {
      File cwd = FileSystems.getDefault().getPath(".").toFile();
      File driver = new File(cwd, "../driver/main.js");
      System.out.println("driver = " + driver.getCanonicalPath());
      ProcessBuilder pb = new ProcessBuilder("node", driver.getCanonicalPath());
      pb.redirectError(ProcessBuilder.Redirect.INHERIT);
//    pb.environment().put("DEBUG", "pw:pro*");
      Process p = pb.start();
      Connection connection = new Connection(p.getInputStream(), p.getOutputStream());
      PlaywrightImpl playwright = (PlaywrightImpl)connection.waitForObjectWithKnownName("Playwright");
      return playwright;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private final BrowserTypeImpl chromium;
  private final BrowserTypeImpl firefox;
  private final BrowserTypeImpl webkit;

  public PlaywrightImpl(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
    chromium = parent.connection.getExistingObject(initializer.getAsJsonObject("chromium").get("guid").getAsString());
    firefox = parent.connection.getExistingObject(initializer.getAsJsonObject("firefox").get("guid").getAsString());
    webkit = parent.connection.getExistingObject(initializer.getAsJsonObject("webkit").get("guid").getAsString());
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
}
