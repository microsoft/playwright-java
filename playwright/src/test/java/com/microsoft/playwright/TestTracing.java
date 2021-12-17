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

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.microsoft.playwright.Utils.copy;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestTracing extends TestBase {

  private static final String _PW_JAVA_TEST_SRC = "_PW_JAVA_TEST_SRC";

  @Override
  @BeforeAll
  void launchBrowser() {
    // The method is replaced by the method below (with temp dir as extra parameter,
    // we cannot declare temp dir field as it would be initialized too late).
  }

  @BeforeAll
  void launchBrowser(@TempDir Path tempDir) {
    System.out.println("new launchBrowser(");
    BrowserType.LaunchOptions options = createLaunchOptions();
    options.setTracesDir(tempDir.resolve("trace-dir"));
    launchBrowser(options);
  }

  @Test
  void shouldCollectTrace1(@TempDir Path tempDir) {
    context.tracing().start(new Tracing.StartOptions().setName("test")
      .setScreenshots(true).setSnapshots(true));
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<button>Click</button>");
    page.click("'Click'");
    page.close();
    Path traceFile = tempDir.resolve("trace.zip");
    context.tracing().stop(new Tracing.StopOptions().setPath(traceFile));

    assertTrue(Files.exists(traceFile));
  }

  @Test
  void shouldCollectTwoTraces(@TempDir Path tempDir) {
    context.tracing().start(new Tracing.StartOptions().setName("test1")
      .setScreenshots(true).setSnapshots(true));
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<button>Click</button>");
    page.click("'Click'");
    Path traceFile1 = tempDir.resolve("trace1.zip");
    context.tracing().stop(new Tracing.StopOptions().setPath(traceFile1));

    context.tracing().start(new Tracing.StartOptions().setName("test2")
      .setScreenshots(true).setSnapshots(true));
    page.dblclick("'Click'");
    page.close();
    Path traceFile2 = tempDir.resolve("trace2.zip");
    context.tracing().stop(new Tracing.StopOptions().setPath(traceFile2));

    assertTrue(Files.exists(traceFile1));
    assertTrue(Files.exists(traceFile2));
  }

  @Test
  void shouldWorkWithMultipleChunks(@TempDir Path tempDir) {
    context.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true));
    page.navigate(server.PREFIX + "/frames/frame.html");

    context.tracing().startChunk();
    page.setContent("<button>Click</button>");
    page.click("'Click'");
    Path traceFile1 = tempDir.resolve("trace1.zip");
    context.tracing().stopChunk(new Tracing.StopChunkOptions().setPath(traceFile1));

    context.tracing().startChunk();
    page.hover("'Click'");
    Path traceFile2 = tempDir.resolve("trace2.zip");
    context.tracing().stopChunk(new Tracing.StopChunkOptions().setPath(traceFile2));

    assertTrue(Files.exists(traceFile1));
    assertTrue(Files.exists(traceFile2));
  }

  @Test
  void shouldCollectSources(@TempDir Path tmpDir) throws IOException {
    Assumptions.assumeTrue(System.getenv("PLAYWRIGHT_JAVA_SRC") != null, "PLAYWRIGHT_JAVA_SRC must point to the directory containing this test source.");
    context.tracing().start(new Tracing.StartOptions().setSources(true));
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<button>Click</button>");
    page.click("'Click'");
    Path trace = tmpDir.resolve("trace1.zip");
    context.tracing().stop(new Tracing.StopOptions().setPath(trace));

    Map<String, byte[]> entries = parseTrace(trace);
    Map<String, byte[]> sources = entries.entrySet().stream().filter(e -> e.getKey().endsWith(".txt")).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    assertEquals(1, sources.size());

    String path = getClass().getName().replaceAll("\\.", File.separator);
    Path sourceFile = Paths.get(System.getenv("PLAYWRIGHT_JAVA_SRC"), path + ".java");
    byte[] thisFile = Files.readAllBytes(sourceFile);
    assertEquals(new String(thisFile, UTF_8), new String(sources.values().iterator().next(), UTF_8));
  }

  private static Map<String, byte[]> parseTrace(Path trace) throws IOException {
    Map<String, byte[]> entries = new HashMap<>();
    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(trace.toFile()))) {
      for (ZipEntry zipEntry = zis.getNextEntry(); zipEntry != null; zipEntry = zis.getNextEntry()) {
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        try (OutputStream output = content) {
          copy(zis, output);
        }
        entries.put(zipEntry.getName(), content.toByteArray());
      }
      zis.closeEntry();
    }
    return entries;
  }
}
