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

import com.microsoft.playwright.impl.Driver;
import com.microsoft.playwright.impl.DriverJar;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestInstall {
  @BeforeEach
  void clearSystemProperties() {
    // Clear system property to ensure that the driver is loaded from jar.
    System.clearProperty("playwright.cli.dir");
    System.clearProperty("playwright.driver.tmpdir");
    // Clear system property to ensure that the default driver is loaded.
    System.clearProperty("playwright.driver.impl");
  }

  @Test
  @Tags({@Tag("isolated"), @Tag("driverThrowTest")})
  void shouldThrowWhenBrowserPathIsInvalid(@TempDir Path tmpDir) {
    Map<String,String> env = new HashMap<>();
    env.put("PLAYWRIGHT_DOWNLOAD_HOST", "https://127.0.0.127");
    // Make sure the browsers are not installed yet by pointing at an empty dir.
    env.put("PLAYWRIGHT_BROWSERS_PATH", tmpDir.toString());

    assertThrows(RuntimeException.class, () -> Driver.ensureDriverInstalled(env, true));
    assertThrows(RuntimeException.class, () -> Driver.ensureDriverInstalled(env, true));
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
  void playwrightDriverAlternativeImpl() {
    System.setProperty("playwright.driver.impl", "com.microsoft.playwright.impl.AlternativeDriver");
    RuntimeException thrown =
      assertThrows(
        RuntimeException.class,
        () -> Driver.ensureDriverInstalled(Collections.emptyMap(), false));
    assertEquals("Failed to create driver", thrown.getMessage());
  }
}
