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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.api.condition.OS;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

public class TestUnresponsiveDriver {
  @Test
  @DisabledOnOs(OS.WINDOWS)
  @EnabledForJreRange(min = JRE.JAVA_9)
  void shouldThrowTimeoutErrorWhenDriverStopsResponding() throws Exception {
    try (Playwright playwright = Playwright.create()) {
      Browser browser = Utils.getBrowserTypeFromEnv(playwright).launch(TestBase.createLaunchOptions());
      Page page = browser.newPage();
      long pid = driverPid(playwright);
      signal("STOP", pid);
      try {
        long start = System.nanoTime();
        TimeoutError e = assertThrows(TimeoutError.class,
          () -> page.waitForFunction("() => false", null, new Page.WaitForFunctionOptions().setTimeout(100)));
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        assertTrue(e.getMessage().contains("Timeout 100ms exceeded while waiting for the driver to respond to waitForFunction"), e.getMessage());
        assertTrue(elapsedMs < 20_000, "elapsed: " + elapsedMs);
      } finally {
        signal("CONT", pid);
      }
      // The driver's late reply to the abandoned call must not break the connection.
      assertEquals(2, page.evaluate("1 + 1"));
    }
  }

  private static long driverPid(Playwright playwright) throws Exception {
    Field field = playwright.getClass().getDeclaredField("driverProcess");
    field.setAccessible(true);
    // Called via reflection as the tests are compiled with JDK 8 on CI.
    return (long) Process.class.getMethod("pid").invoke(field.get(playwright));
  }

  private static void signal(String signal, long pid) throws Exception {
    Process kill = new ProcessBuilder("kill", "-" + signal, Long.toString(pid)).inheritIO().start();
    assertEquals(0, kill.waitFor());
  }
}
