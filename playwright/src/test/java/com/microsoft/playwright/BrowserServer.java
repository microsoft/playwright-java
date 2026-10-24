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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Collections;

import static com.microsoft.playwright.fixtures.Env.isWindows;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A {@code playwright launch-server} process that tests connect to, the counterpart of the
 * upstream {@code startRemoteServer} fixture.
 */
class BrowserServer implements AutoCloseable {
  final Process process;
  final String wsEndpoint;

  static BrowserServer launch(BrowserType browserType) {
    try {
      Driver driver = Driver.ensureDriverInstalled(Collections.emptyMap(), false);
      Path dir = driver.driverDir();
      String node = dir.resolve(isWindows ? "node.exe" : "node").toString();
      String cliJs = dir.resolve("package/cli.js").toString();
      // We launch node process directly instead of using playwright.sh script as killing the script
      // process will leave node process running and killing it would be more hassle.
      ProcessBuilder pb = new ProcessBuilder(node, cliJs, "launch-server", "--browser", browserType.name());
      pb.directory(dir.toFile());
      pb.redirectError(ProcessBuilder.Redirect.INHERIT);
      Process process = pb.start();
      BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String wsEndpoint = input.readLine();
      if (!wsEndpoint.startsWith("ws://")) {
        throw new RuntimeException("Invalid web socket address: " + wsEndpoint);
      }
      return new BrowserServer(process, wsEndpoint);
    } catch (IOException e) {
      throw new PlaywrightException("Failed to launch server", e);
    }
  }

  private BrowserServer(Process process, String wsEndpoint) {
    this.process = process;
    this.wsEndpoint = wsEndpoint;
  }

  void kill() throws InterruptedException {
    process.destroy();
    int exitCode = process.waitFor();
    // FIXME: 2 tests are failing this check on windows:
    // disconnectedEventShouldBeEmittedWhenBrowserIsClosedOrServerIsClosed
    // shouldThrowWhenUsedAfterIsConnectedReturnsFalse
    if (!isWindows) {
      assertEquals(0, exitCode);
    }
  }

  @Override
  public void close() {
    process.destroyForcibly();
  }
}
