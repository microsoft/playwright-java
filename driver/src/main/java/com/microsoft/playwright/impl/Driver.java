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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * This class provides access to playwright-cli. It can be either preinstalled
 * in the host system and its path is passed as a system property or it can be
 * loaded from the driver-bundle module if that module is in the classpath.
 */
public abstract class Driver {
  private static Driver instance;

  private static class PreinstalledDriver extends Driver {
    private final Path driverDir;
    PreinstalledDriver(Path driverDir) {
      this.driverDir = driverDir;
    }

    @Override
    protected void initialize(Map<String, String> env) {
      // no-op
    }

    @Override
    Path driverDir() {
      return driverDir;
    }
  }

  public static synchronized Path ensureDriverInstalled(Map<String, String> env) {
    if (instance == null) {
      try {
        instance = createDriver();
        instance.initialize(env);
      } catch (Exception exception) {
        throw new RuntimeException("Failed to create driver", exception);
      }
    }
    String name = instance.cliFileName();
    return instance.driverDir().resolve(name);
  }

  protected abstract void initialize(Map<String, String> env) throws Exception;

  protected String cliFileName() {
    return System.getProperty("os.name").toLowerCase().contains("windows") ?
      "playwright.cmd" : "playwright.sh";
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
