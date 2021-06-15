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

import java.nio.file.Path;
import java.util.*;

/**
 * API for collecting and saving Playwright traces. Playwright traces can be opened using the Playwright CLI after
 * Playwright script runs.
 *
 * <p> Start with specifying the folder traces will be stored in:
 * <pre>{@code
 * Browser browser = chromium.launch();
 * BrowserContext context = browser.newContext();
 * context.tracing().start(new Tracing.StartOptions()
 *   .setScreenshots(true)
 *   .setSnapshots(true));
 * Page page = context.newPage();
 * page.navigate("https://playwright.dev");
 * context.tracing().stop(new Tracing.StopOptions()
 *   .setPath(Paths.get("trace.zip")));
 * }</pre>
 */
public interface Tracing {
  class StartOptions {
    /**
     * If specified, the trace is going to be saved into the file with the given name inside the {@code tracesDir} folder specified
     * in {@link BrowserType#launch BrowserType.launch()}.
     */
    public String name;
    /**
     * Whether to capture screenshots during tracing. Screenshots are used to build a timeline preview.
     */
    public Boolean screenshots;
    /**
     * Whether to capture DOM snapshot on every action.
     */
    public Boolean snapshots;

    public StartOptions setName(String name) {
      this.name = name;
      return this;
    }
    public StartOptions setScreenshots(boolean screenshots) {
      this.screenshots = screenshots;
      return this;
    }
    public StartOptions setSnapshots(boolean snapshots) {
      this.snapshots = snapshots;
      return this;
    }
  }
  class StopOptions {
    /**
     * Export trace into the file with the given name.
     */
    public Path path;

    public StopOptions setPath(Path path) {
      this.path = path;
      return this;
    }
  }
  /**
   * Start tracing.
   * <pre>{@code
   * context.tracing().start(new Tracing.StartOptions()
   *   .setScreenshots(true)
   *   .setSnapshots(true));
   * Page page = context.newPage();
   * page.navigate("https://playwright.dev");
   * context.tracing().stop(new Tracing.StopOptions()
   *   .setPath(Paths.get("trace.zip")));
   * }</pre>
   */
  default void start() {
    start(null);
  }
  /**
   * Start tracing.
   * <pre>{@code
   * context.tracing().start(new Tracing.StartOptions()
   *   .setScreenshots(true)
   *   .setSnapshots(true));
   * Page page = context.newPage();
   * page.navigate("https://playwright.dev");
   * context.tracing().stop(new Tracing.StopOptions()
   *   .setPath(Paths.get("trace.zip")));
   * }</pre>
   */
  void start(StartOptions options);
  /**
   * Stop tracing.
   */
  default void stop() {
    stop(null);
  }
  /**
   * Stop tracing.
   */
  void stop(StopOptions options);
}

