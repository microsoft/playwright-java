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
    protected void initialize(Map<String, String> env, Boolean installBrowsers) {
      // no-op
    }

    @Override
    Path driverDir() {
      return driverDir;
    }
  }

  public static synchronized Path ensureDriverInstalled(Map<String, String> env, Boolean installBrowsers) {
    if (instance == null) {
      try {
        instance = createDriver();
        instance.initialize(env, installBrowsers);
      } catch (Exception exception) {
        throw new RuntimeException("Failed to create driver", exception);
      }
    }
    return instance.driverPath();
  }

  protected abstract void initialize(Map<String, String> env, Boolean installBrowsers) throws Exception;

  public Path driverPath() {
    String cliFileName = System.getProperty("os.name").toLowerCase().contains("windows") ?
      "playwright.cmd" : "playwright.sh";
    return driverDir().resolve(cliFileName);
  }

  public static void setRequiredEnvironmentVariables(ProcessBuilder pb) {
    pb.environment().put("PW_CLI_TARGET_LANG", "java");
    pb.environment().put("PW_CLI_TARGET_LANG_VERSION", getMajorJavaVersion());
    String version = Driver.class.getPackage().getImplementationVersion();
    if (version != null) {
      pb.environment().put("PW_CLI_DISPLAY_VERSION", version);
    }
  }

  private static String getMajorJavaVersion() {
    String version = System.getProperty("java.version");
    if (version.startsWith("1.")) {
      return version.substring(2, 3);
    }
    int dot = version.indexOf(".");
    if (dot != -1) {
      return version.substring(0, dot);
    }
    return version;
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
