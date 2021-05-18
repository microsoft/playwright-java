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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

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
    System.out.println("new launchBrowser(");
    BrowserType.LaunchOptions options = createLaunchOptions();
    options.setTraceDir(tempDir.resolve("trace-dir"));
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
    context.tracing().stop();
    Path traceFile = tempDir.resolve("trace.zip");
    context.tracing().export(traceFile);

    assertTrue(Files.exists(traceFile));
  }

  @Test
  void shouldCollectTwoTraces(@TempDir Path tempDir) {
    context.tracing().start(new Tracing.StartOptions().setName("test1")
      .setScreenshots(true).setSnapshots(true));
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<button>Click</button>");
    page.click("'Click'");
    context.tracing().stop();
    Path traceFile1 = tempDir.resolve("trace1.zip");
    context.tracing().export(traceFile1);

    context.tracing().start(new Tracing.StartOptions().setName("test2")
      .setScreenshots(true).setSnapshots(true));
    page.dblclick("'Click'");
    page.close();
    context.tracing().stop();
    Path traceFile2 = tempDir.resolve("trace2.zip");
    context.tracing().export(traceFile2);

    assertTrue(Files.exists(traceFile1));
    assertTrue(Files.exists(traceFile2));
  }

}
