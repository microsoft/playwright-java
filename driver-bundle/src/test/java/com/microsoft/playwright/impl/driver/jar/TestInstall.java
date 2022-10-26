/*
 * Copyright (c) Microsoft Corporation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright.impl.driver.jar;

import com.microsoft.playwright.impl.driver.Driver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.microsoft.playwright.impl.driver.jar.DriverJar.PLAYWRIGHT_NODEJS_PATH;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.*;

public class TestInstall {
  private static boolean isPortAvailable(int port) {
    try (ServerSocket ignored = new ServerSocket(port)) {
      return true;
    } catch (IOException ignored) {
      return false;
    }
  }

  private static int unusedPort() {
    for (int i = 10000; i < 11000; i++) {
      if (isPortAvailable(i)) {
        return i;
      }
    }
    throw new RuntimeException("Cannot find unused local port");
  }

  @BeforeEach
  void clearSystemProperties() {
    // Clear system property to ensure that the driver is loaded from jar.
    System.clearProperty("playwright.cli.dir");
    System.clearProperty("playwright.driver.tmpdir");
    System.clearProperty("playwright.nodejs.path");
    // Clear system property to ensure that the default driver is loaded.
    System.clearProperty("playwright.driver.impl");
  }

  @Test
  void shouldThrowWhenBrowserPathIsInvalid(@TempDir Path tmpDir) throws NoSuchFieldException, IllegalAccessException {
    Map<String,String> env = new HashMap<>();

    // On macOS we can only use 127.0.0.1, so pick unused port instead.
    // https://superuser.com/questions/458875/how-do-you-get-loopback-addresses-other-than-127-0-0-1-to-work-on-os-x
    env.put("PLAYWRIGHT_DOWNLOAD_HOST", "https://127.0.0.1:" + unusedPort());
    // Make sure the browsers are not installed yet by pointing at an empty dir.
    env.put("PLAYWRIGHT_BROWSERS_PATH", tmpDir.toString());
    env.put("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", "false");

    RuntimeException exception = assertThrows(RuntimeException.class, () -> Driver.createAndInstall(env, true));
    String message = exception.getMessage();
    assertTrue(message.contains("Failed to create driver"), message);
  }

  @Test
  void playwrightCliInstalled() throws Exception {
    Driver driver = Driver.createAndInstall(Collections.emptyMap(), false);
    assertTrue(Files.exists(driver.driverPath()));

    ProcessBuilder pb = driver.createProcessBuilder();
    pb.command().add("install");
    pb.redirectError(ProcessBuilder.Redirect.INHERIT);
    pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
    Process p = pb.start();
    boolean result = p.waitFor(1, TimeUnit.MINUTES);
    assertTrue(result, "Timed out waiting for browsers to install");
  }

  @Test
  void playwrightDriverInAlternativeTmpdir(@TempDir Path tmpdir) throws Exception {
    System.setProperty("playwright.driver.tmpdir", tmpdir.toString());
    DriverJar driver = new DriverJar();
    assertTrue(driver.driverPath().startsWith(tmpdir), "Driver path: " + driver.driverPath() + " tmp: " + tmpdir);
  }

  @Test
  void playwrightDriverDefaultImpl() {
    assertDoesNotThrow(() -> Driver.createAndInstall(Collections.emptyMap(), false));
  }

  @Test
  void playwrightDriverAlternativeImpl() throws NoSuchFieldException, IllegalAccessException {
    System.setProperty("playwright.driver.impl", "com.microsoft.playwright.impl.AlternativeDriver");
    RuntimeException thrown =
      assertThrows(
        RuntimeException.class,
        () -> Driver.createAndInstall(Collections.emptyMap(), false));
    assertEquals("Failed to create driver", thrown.getMessage());
  }

  @Test
  void canPassPreinstalledNodeJsAsSystemProperty(@TempDir Path tmpDir) throws IOException, URISyntaxException, InterruptedException {
    String nodePath = extractNodeJsToTemp();
    System.setProperty("playwright.nodejs.path", nodePath);
    Driver driver = Driver.createAndInstall(Collections.emptyMap(), false);
    canSpecifyPreinstalledNodeJsShared(driver, tmpDir);
  }

  @Test
  void canSpecifyPreinstalledNodeJsAsEnv(@TempDir Path tmpDir) throws IOException, URISyntaxException, InterruptedException {
    String nodePath = extractNodeJsToTemp();
    Driver driver = Driver.createAndInstall(singletonMap(PLAYWRIGHT_NODEJS_PATH, nodePath), false);
    canSpecifyPreinstalledNodeJsShared(driver, tmpDir);
  }


  private static String extractNodeJsToTemp() throws URISyntaxException, IOException {
    DriverJar auxDriver = new DriverJar();
    auxDriver.extractDriverToTempDir();
    String nodePath = auxDriver.driverPath().getParent().resolve(isWindows() ? "node.exe" : "node").toString();
    return nodePath;
  }

  private static boolean isWindows() {
    String name = System.getProperty("os.name").toLowerCase();
    return name.contains("win");
  }

  private static void canSpecifyPreinstalledNodeJsShared(Driver driver, Path tmpDir) throws IOException, URISyntaxException, InterruptedException {
    Path builtinNode = driver.driverPath().getParent().resolve("node");
    assertFalse(Files.exists(builtinNode), builtinNode.toString());
    Path builtinNodeExe = driver.driverPath().getParent().resolve("node.exe");
    assertFalse(Files.exists(builtinNodeExe), builtinNodeExe.toString());

    ProcessBuilder pb = driver.createProcessBuilder();
    pb.command().add("--version");
    pb.redirectError(ProcessBuilder.Redirect.INHERIT);
    Path out = tmpDir.resolve("out.txt");
    pb.redirectOutput(out.toFile());
    Process p = pb.start();
    boolean result = p.waitFor(1, TimeUnit.MINUTES);
    assertTrue(result, "Timed out waiting for version to be printed");
    String stdout = new String(Files.readAllBytes(out), StandardCharsets.UTF_8);
    assertTrue(stdout.contains("Version "), stdout);
  }
}
