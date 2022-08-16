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

package com.microsoft.playwright.impl.driver;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.microsoft.playwright.impl.driver.DriverLogging.logWithTimestamp;

/**
 * This class provides access to playwright-cli. It can be either preinstalled
 * in the host system and its path is passed as a system property or it can be
 * loaded from the driver-bundle module if that module is in the classpath.
 */
public abstract class Driver {
  protected final Map<String, String> env = new LinkedHashMap<>();

  private static Driver instance;

  private static class PreinstalledDriver extends Driver {
    private final Path driverDir;
    PreinstalledDriver(Path driverDir) {
      logMessage("created PreinstalledDriver: " + driverDir);
      this.driverDir = driverDir;
    }

    @Override
    protected void initialize(Boolean installBrowsers) {
      // no-op
    }

    @Override
    protected Path driverDir() {
      return driverDir;
    }
  }

  public static synchronized Driver ensureDriverInstalled(Map<String, String> env, Boolean installBrowsers) {
    if (instance == null) {
      instance = createAndInstall(env, installBrowsers);
    }
    return instance;
  }

  private void initialize(Map<String, String> env, Boolean installBrowsers) throws Exception {
    this.env.putAll(env);
    initialize(installBrowsers);
  }
  protected abstract void initialize(Boolean installBrowsers) throws Exception;

  public Path driverPath() {
    String cliFileName = System.getProperty("os.name").toLowerCase().contains("windows") ?
      "playwright.cmd" : "playwright.sh";
    return driverDir().resolve(cliFileName);
  }

  public ProcessBuilder createProcessBuilder() {
    ProcessBuilder pb = new ProcessBuilder(driverPath().toString());
    pb.environment().putAll(env);
    pb.environment().put("PW_LANG_NAME", "java");
    pb.environment().put("PW_LANG_NAME_VERSION", getMajorJavaVersion());
    String version = Driver.class.getPackage().getImplementationVersion();
    if (version != null) {
      pb.environment().put("PW_CLI_DISPLAY_VERSION", version);
    }
    return pb;
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
  public static Driver createAndInstall(Map<String, String> env, Boolean installBrowsers) {
    try {
      Driver instance = newInstance();
      logMessage("initializing driver");
      instance.initialize(env, installBrowsers);
      logMessage("driver initialized.");
      return instance;
    } catch (Exception exception) {
      throw new RuntimeException("Failed to create driver", exception);
    }
  }

  private static Driver newInstance() throws Exception {
    String pathFromProperty = System.getProperty("playwright.cli.dir");
    if (pathFromProperty != null) {
      return new PreinstalledDriver(Paths.get(pathFromProperty));
    }

    String driverImpl =
      System.getProperty("playwright.driver.impl", "com.microsoft.playwright.impl.driver.jar.DriverJar");
    Class<?> jarDriver = Class.forName(driverImpl);
    return (Driver) jarDriver.getDeclaredConstructor().newInstance();
  }

  protected abstract Path driverDir();

  protected static void logMessage(String message) {
    // This matches log format produced by the server.
    logWithTimestamp("pw:install " + message);
  }
}
