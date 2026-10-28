/**
 * Copyright 2017 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright.tests.library.chromium;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.EnabledOnBrowser;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestInfo;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static com.microsoft.playwright.fixtures.Utils.rafraf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/library/chromium/tracing.spec.ts. */
// Java: the tests of library/chromium run in the chromium project only.
@EnabledOnBrowser(value = BrowserName.CHROMIUM, reason = "tests/library/chromium run in the chromium project only")
public class TestTracing {
  @PlaywrightTest
  @DisplayName("TR-043 should output a trace")
  void shouldOutputATrace(Browser browser, TestServer server, TestInfo testInfo) {
    // Java: process.on('warning', ...) of the Node.js process has no counterpart.

    Page page = browser.newPage();
    Path outputTraceFile = testInfo.outputPath("trace.json");
    browser.startTracing(page, new Browser.StartTracingOptions().setScreenshots(true).setPath(outputTraceFile));
    for (int i = 0; i < 11; i++)
      page.navigate(server.PREFIX + "/grid.html");
    browser.stopTracing();
    assertEquals(true, Files.exists(outputTraceFile));
    page.close();
  }

  @PlaywrightTest
  @DisplayName("TR-044 should create directories as needed")
  void shouldCreateDirectoriesAsNeeded(Browser browser, TestServer server, TestInfo testInfo) {
    Page page = browser.newPage();
    Path filePath = testInfo.outputPath("these", "are", "directories", "trace.json");
    browser.startTracing(page, new Browser.StartTracingOptions().setScreenshots(true).setPath(filePath));
    page.navigate(server.PREFIX + "/grid.html");
    browser.stopTracing();
    assertEquals(true, Files.exists(filePath));
    page.close();
  }

  @PlaywrightTest
  @DisplayName("TR-045 should run with custom categories if provided")
  void shouldRunWithCustomCategoriesIfProvided(Browser browser, TestInfo testInfo) throws Exception {
    Page page = browser.newPage();
    Path outputTraceFile = testInfo.outputPath("trace.json");
    browser.startTracing(page, new Browser.StartTracingOptions().setPath(outputTraceFile).setCategories(Arrays.asList("disabled-by-default-cc.debug")));
    rafraf(page);
    browser.stopTracing();

    JsonObject traceJson = JsonParser.parseString(new String(Files.readAllBytes(outputTraceFile), StandardCharsets.UTF_8)).getAsJsonObject();
    // NOTE: trace-config is deprecated as per http://crrev.com/c/6628182
    boolean hasTraceConfig = traceJson.getAsJsonObject("metadata").has("trace-config")
      && traceJson.getAsJsonObject("metadata").get("trace-config").getAsString().contains("disabled-by-default-cc.debug");
    int categoryEvents = 0;
    for (JsonElement event : traceJson.getAsJsonArray("traceEvents")) {
      JsonObject eventObject = event.getAsJsonObject();
      if (eventObject.has("cat") && eventObject.get("cat").getAsString().equals("disabled-by-default-cc.debug"))
        categoryEvents++;
    }
    assertEquals(true, hasTraceConfig || categoryEvents > 0);
    page.close();
  }

  @PlaywrightTest
  @DisplayName("TR-046 should throw if tracing on two pages")
  void shouldThrowIfTracingOnTwoPages(Browser browser, TestInfo testInfo) {
    Page page = browser.newPage();
    Path outputTraceFile = testInfo.outputPath("trace.json");
    browser.startTracing(page, new Browser.StartTracingOptions().setPath(outputTraceFile));
    Page newPage = browser.newPage();
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> browser.startTracing(newPage, new Browser.StartTracingOptions().setPath(outputTraceFile)));
    newPage.close();
    assertNotNull(error);
    browser.stopTracing();
    page.close();
  }

  @PlaywrightTest
  @DisplayName("TR-047 should return a buffer")
  void shouldReturnABuffer(Browser browser, TestServer server, TestInfo testInfo) throws Exception {
    Page page = browser.newPage();
    Path outputTraceFile = testInfo.outputPath("trace.json");
    browser.startTracing(page, new Browser.StartTracingOptions().setScreenshots(true).setPath(outputTraceFile));
    page.navigate(server.PREFIX + "/grid.html");
    byte[] trace = browser.stopTracing();
    byte[] buf = Files.readAllBytes(outputTraceFile);
    assertEquals(new String(buf, StandardCharsets.UTF_8), new String(trace, StandardCharsets.UTF_8));
    page.close();
  }

  @PlaywrightTest
  @DisplayName("TR-048 should work without options")
  void shouldWorkWithoutOptions(Browser browser, TestServer server) {
    Page page = browser.newPage();
    browser.startTracing(page);
    page.navigate(server.PREFIX + "/grid.html");
    byte[] trace = browser.stopTracing();
    assertNotNull(trace);
    page.close();
  }

  @PlaywrightTest
  @DisplayName("TR-049 should support a buffer without a path")
  void shouldSupportABufferWithoutAPath(Browser browser, TestServer server) {
    Page page = browser.newPage();
    browser.startTracing(page, new Browser.StartTracingOptions().setScreenshots(true));
    page.navigate(server.PREFIX + "/grid.html");
    rafraf(page, 100);
    byte[] trace = browser.stopTracing();
    assertEquals(true, new String(trace, StandardCharsets.UTF_8).contains("screenshot"));
    page.close();
  }
}
