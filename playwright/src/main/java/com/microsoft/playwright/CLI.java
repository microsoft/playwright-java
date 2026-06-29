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

package com.microsoft.playwright;

import com.microsoft.playwright.impl.driver.Driver;
import com.microsoft.playwright.impl.driver.jar.DriverJar;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

import static java.util.Arrays.asList;

/**
 * Use this class to launch playwright cli.
 */
public class CLI {
  public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {
    // Extract the driver into a fixed directory instead of running the playwright CLI. This is
    // handled in Java because it must not require an already-extracted driver. See issue #1268.
    if (args.length > 0 && "install-driver".equals(args[0])) {
      installDriver(args);
      return;
    }
    Driver driver = Driver.ensureDriverInstalled(Collections.emptyMap(), false);
    ProcessBuilder pb = driver.createProcessBuilder();
    pb.command().addAll(asList(args));
    String version = Playwright.class.getPackage().getImplementationVersion();
    if (version != null) {
      pb.environment().put("PW_CLI_DISPLAY_VERSION", version);
    }
    pb.inheritIO();
    Process process = pb.start();
    System.exit(process.waitFor());
  }

  private static void installDriver(String[] args) throws IOException, URISyntaxException {
    String dir = args.length > 1 ? args[1] : System.getenv(Driver.PLAYWRIGHT_DRIVER_DIR);
    if (dir == null) {
      System.err.println("Usage: install-driver <dir> (or set the " + Driver.PLAYWRIGHT_DRIVER_DIR
        + " environment variable)");
      System.exit(1);
      return;
    }
    Path driverDir = Paths.get(dir);
    DriverJar.installDriverTo(driverDir);
    System.out.println("Installed Playwright driver into " + driverDir.toAbsolutePath());
  }
}
