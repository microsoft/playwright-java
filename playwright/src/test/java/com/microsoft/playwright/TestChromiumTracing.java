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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIf(value="com.microsoft.playwright.TestBase#isChromium", disabledReason="Chromium-only API")
public class TestChromiumTracing extends TestBase {
  @Test
  void shouldOutputATrace(@TempDir Path tempDir) {
    try (Page page = browser.newPage()) {
      Path outputTraceFile = tempDir.resolve("trace.json");
      browser.startTracing(page, new Browser.StartTracingOptions()
        .setScreenshots(true).setPath(outputTraceFile));
      page.navigate(server.PREFIX + "/grid.html");
      browser.stopTracing();
      assertTrue(Files.exists(outputTraceFile));
    }
  }

  @Test
  void shouldCreateDirectoriesAsNeeded(@TempDir Path tempDir) {
    try (Page page = browser.newPage()) {
      Path filePath = tempDir.resolve("these/are/directories/trace.json");
      browser.startTracing(page, new Browser.StartTracingOptions()
        .setScreenshots(true).setPath(filePath));
      page.navigate(server.PREFIX + "/grid.html");
      browser.stopTracing();
      assertTrue(Files.exists(filePath));
    }
  }

  @Test
  void shouldRunWithCustomCategoriesIfProvided(@TempDir Path tempDir) throws IOException {
    try (Page page = browser.newPage()) {
      Path outputTraceFile = tempDir.resolve("trace.json");
      browser.startTracing(page, new Browser.StartTracingOptions()
        .setPath(outputTraceFile)
        .setCategories(asList("disabled-by-default-v8.cpu_profiler.hires")));
      browser.stopTracing();
      try (FileReader fileReader = new FileReader(outputTraceFile.toFile())) {
        JsonObject traceJson = new Gson().fromJson(fileReader, JsonObject.class);
        assertTrue(traceJson.getAsJsonObject("metadata").get("trace-config")
          .getAsString().contains("disabled-by-default-v8.cpu_profiler.hires"));
      }
    }
  }

  @Test
  void shouldThrowIfTracingOnTwoPages(@TempDir Path tempDir) {
    try (Page page = browser.newPage()) {
      Path outputTraceFile = tempDir.resolve("trace.json");
      browser.startTracing(page, new Browser.StartTracingOptions()
        .setPath(outputTraceFile));
      Page newPage = browser.newPage();
      assertThrows(PlaywrightException.class, () -> {
        browser.startTracing(newPage, new Browser.StartTracingOptions()
          .setPath(outputTraceFile));
      });
      newPage.close();
      browser.stopTracing();
    }
  }

  @Test
  void shouldReturnABuffer(@TempDir Path tempDir) throws IOException {
    try (Page page = browser.newPage()) {
      Path outputTraceFile = tempDir.resolve("trace.json");
      browser.startTracing(page, new Browser.StartTracingOptions()
        .setScreenshots(true).setPath(outputTraceFile));
      page.navigate(server.PREFIX + "/grid.html");
      byte[] trace = browser.stopTracing();
      byte[] buf = Files.readAllBytes(outputTraceFile);
      assertArrayEquals(buf, trace);
    }
  }

  @Test
  void shouldWorkWithoutOptions() {
    try (Page page = browser.newPage()) {
      browser.startTracing(page);
      page.navigate(server.PREFIX + "/grid.html");
      byte[] trace = browser.stopTracing();
      assertNotNull(trace);
    }
  }

  @Test
  void shouldSupportABufferWithoutAPath() {
    try (Page page = browser.newPage()) {
      browser.startTracing(page, new Browser.StartTracingOptions().setScreenshots(true));
      page.navigate(server.PREFIX + "/grid.html");
      byte[] trace = browser.stopTracing();
      assertTrue(new String(trace, StandardCharsets.UTF_8).contains("screenshot"));
    }
  }
}
