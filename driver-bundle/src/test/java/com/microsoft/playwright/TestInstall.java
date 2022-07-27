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

package com.microsoft.playwright;

import com.microsoft.playwright.impl.driver.Driver;
import com.microsoft.playwright.impl.driver.jar.DriverJar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    // Reset instance field value to null for the test.
    Field field = Driver.class.getDeclaredField("instance");
    field.setAccessible(true);
    Object value = field.get(Driver.class);
    field.set(Driver.class, null);

    for (int i = 0; i < 2; i++){
      RuntimeException exception = assertThrows(RuntimeException.class, () -> Driver.ensureDriverInstalled(env, true));
      String message = exception.getMessage();
      assertTrue(message.contains("Failed to create driver"), message);
    }

    field.set(Driver.class, value);
  }

  @Test
  void playwrightCliInstalled() throws Exception {
    Path cli = Driver.ensureDriverInstalled(Collections.emptyMap(), false);
    assertTrue(Files.exists(cli));

    ProcessBuilder pb = new ProcessBuilder(cli.toString(), "install");
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
    assertDoesNotThrow(() -> Driver.ensureDriverInstalled(Collections.emptyMap(), false));
  }

  @Test
  void playwrightDriverAlternativeImpl() throws NoSuchFieldException, IllegalAccessException {
    // Reset instance field value to null for the test.
    Field field = Driver.class.getDeclaredField("instance");
    field.setAccessible(true);
    Object value = field.get(Driver.class);
    field.set(Driver.class, null);

    System.setProperty("playwright.driver.impl", "com.microsoft.playwright.impl.AlternativeDriver");
    RuntimeException thrown =
      assertThrows(
        RuntimeException.class,
        () -> Driver.ensureDriverInstalled(Collections.emptyMap(), false));
    assertEquals("Failed to create driver", thrown.getMessage());

    field.set(Driver.class, value);
  }
}
