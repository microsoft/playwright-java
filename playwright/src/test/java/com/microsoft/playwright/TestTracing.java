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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestTracing extends TestBase {
  @Override
  @BeforeAll
  void launchBrowser() {
    // The method is replaced by the method below (with temp dir as extra parameter,
    // we cannot declare temp dir field as it would be initialized too late).
  }

  @BeforeAll
  void launchBrowser(@TempDir Path tempDir) {
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

    Map<String, byte[]> entries = Utils.parseZip(trace);
    Map<String, byte[]> sources = entries.entrySet().stream().filter(e -> e.getKey().endsWith(".txt")).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    assertEquals(1, sources.size());

    String path = getClass().getName().replace('.', File.separatorChar);
    String[] srcRoots = System.getenv("PLAYWRIGHT_JAVA_SRC").split(File.pathSeparator);
    // Resolve in the last specified source dir.
    Path sourceFile = Paths.get(srcRoots[srcRoots.length - 1], path + ".java");
    byte[] thisFile = Files.readAllBytes(sourceFile);
    assertEquals(new String(thisFile, UTF_8), new String(sources.values().iterator().next(), UTF_8));
  }

  @Test
  void shouldNotFailWhenSourcesSetExplicitlyToFalse() throws IOException {
    Assumptions.assumeTrue(System.getenv("PLAYWRIGHT_JAVA_SRC") == null, "PLAYWRIGHT_JAVA_SRC must not be set for this test");
    context.tracing().start(new Tracing.StartOptions().setSources(false));
  }

  @Test
  void shouldRespectTracesDirAndName(@TempDir Path tempDir) {
    Path tracesDir = tempDir.resolve("trace-dir");
    BrowserType.LaunchOptions options = createLaunchOptions();
    options.setTracesDir(tracesDir);
    try (Browser browser = browserType.launch(options)) {
      BrowserContext context = browser.newContext();
      Page page = context.newPage();

      context.tracing().start(new Tracing.StartOptions().setName("name1").setSnapshots(true));
      page.navigate(server.PREFIX + "/one-style.html");
      context.tracing().stopChunk(new Tracing.StopChunkOptions().setPath(tempDir.resolve("trace1.zip")));
      assertTrue(Files.exists(tracesDir.resolve("name1.trace")));
      assertTrue(Files.exists(tracesDir.resolve("name1.network")));

      context.tracing().startChunk(new Tracing.StartChunkOptions().setName("name2"));
      page.navigate(server.PREFIX + "/har.html");
      context.tracing().stop(new Tracing.StopOptions().setPath(tempDir.resolve("trace2.zip")));
      assertTrue(Files.exists(tracesDir.resolve("name2.trace")));
      assertTrue(Files.exists(tracesDir.resolve("name2.network")));
    }
  }

}
