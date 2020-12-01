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

import com.microsoft.playwright.PlaywrightException;

import java.nio.file.Path;
import java.nio.file.Paths;

abstract class Driver {
  private static Driver instance;

  private static class PreinstalledDriver extends Driver {
    private final Path driverDir;
    PreinstalledDriver(Path driverDir) {
      this.driverDir = driverDir;
    }
    @Override
    Path driverDir() {
      return driverDir;
    }
  }

  static synchronized Path ensureDriverInstalled() {
    if (instance == null) {
      try {
        instance = createDriver();
      } catch (Exception exception) {
        throw new PlaywrightException("Failed to find playwright-cli", exception);
      }
    }
    return instance.driverDir().resolve("playwright-cli");
  }

  private static Driver createDriver() throws Exception {
    String pathFromProperty = System.getProperty("playwright.cli.dir");
    if (pathFromProperty != null) {
      return new PreinstalledDriver(Paths.get(pathFromProperty));
    }

    Class<?> jarDriver = Class.forName("com.microsoft.playwright.impl.DriverJar");
    return (Driver) jarDriver.getDeclaredConstructor().newInstance();
  }

  abstract Path driverDir();
}
