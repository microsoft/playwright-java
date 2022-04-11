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

/**
 * API for collecting and saving Playwright traces. Playwright traces can be opened in <a href="../trace-viewer.md">Trace
 * Viewer</a> after Playwright script runs.
 *
 * <p> Start recording a trace before performing actions. At the end, stop tracing and save it to a file.
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
     * If this option is true tracing will
     * <ul>
     * <li> capture DOM snapshot on every action</li>
     * <li> record network activity</li>
     * </ul>
     */
    public Boolean snapshots;
    /**
     * Whether to include source files for trace actions. List of the directories with source code for the application must be
     * provided via {@code PLAYWRIGHT_JAVA_SRC} environment variable (the paths should be separated by ';' on Windows and by ':' on
     * other platforms).
     */
    public Boolean sources;
    /**
     * Trace name to be shown in the Trace Viewer.
     */
    public String title;

    /**
     * If specified, the trace is going to be saved into the file with the given name inside the {@code tracesDir} folder specified
     * in {@link BrowserType#launch BrowserType.launch()}.
     */
    public StartOptions setName(String name) {
      this.name = name;
      return this;
    }
    /**
     * Whether to capture screenshots during tracing. Screenshots are used to build a timeline preview.
     */
    public StartOptions setScreenshots(boolean screenshots) {
      this.screenshots = screenshots;
      return this;
    }
    /**
     * If this option is true tracing will
     * <ul>
     * <li> capture DOM snapshot on every action</li>
     * <li> record network activity</li>
     * </ul>
     */
    public StartOptions setSnapshots(boolean snapshots) {
      this.snapshots = snapshots;
      return this;
    }
    /**
     * Whether to include source files for trace actions. List of the directories with source code for the application must be
     * provided via {@code PLAYWRIGHT_JAVA_SRC} environment variable (the paths should be separated by ';' on Windows and by ':' on
     * other platforms).
     */
    public StartOptions setSources(boolean sources) {
      this.sources = sources;
      return this;
    }
    /**
     * Trace name to be shown in the Trace Viewer.
     */
    public StartOptions setTitle(String title) {
      this.title = title;
      return this;
    }
  }
  class StartChunkOptions {
    /**
     * Trace name to be shown in the Trace Viewer.
     */
    public String title;

    /**
     * Trace name to be shown in the Trace Viewer.
     */
    public StartChunkOptions setTitle(String title) {
      this.title = title;
      return this;
    }
  }
  class StopOptions {
    /**
     * Export trace into the file with the given path.
     */
    public Path path;

    /**
     * Export trace into the file with the given path.
     */
    public StopOptions setPath(Path path) {
      this.path = path;
      return this;
    }
  }
  class StopChunkOptions {
    /**
     * Export trace collected since the last {@link Tracing#startChunk Tracing.startChunk()} call into the file with the given
     * path.
     */
    public Path path;

    /**
     * Export trace collected since the last {@link Tracing#startChunk Tracing.startChunk()} call into the file with the given
     * path.
     */
    public StopChunkOptions setPath(Path path) {
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
   * Start a new trace chunk. If you'd like to record multiple traces on the same {@code BrowserContext}, use {@link Tracing#start
   * Tracing.start()} once, and then create multiple trace chunks with {@link Tracing#startChunk Tracing.startChunk()} and
   * {@link Tracing#stopChunk Tracing.stopChunk()}.
   * <pre>{@code
   * context.tracing().start(new Tracing.StartOptions()
   *   .setScreenshots(true)
   *   .setSnapshots(true));
   * Page page = context.newPage();
   * page.navigate("https://playwright.dev");
   *
   * context.tracing().startChunk();
   * page.click("text=Get Started");
   * // Everything between startChunk and stopChunk will be recorded in the trace.
   * context.tracing().stopChunk(new Tracing.StopChunkOptions()
   *   .setPath(Paths.get("trace1.zip")));
   *
   * context.tracing().startChunk();
   * page.navigate("http://example.com");
   * // Save a second trace file with different actions.
   * context.tracing().stopChunk(new Tracing.StopChunkOptions()
   *   .setPath(Paths.get("trace2.zip")));
   * }</pre>
   */
  default void startChunk() {
    startChunk(null);
  }
  /**
   * Start a new trace chunk. If you'd like to record multiple traces on the same {@code BrowserContext}, use {@link Tracing#start
   * Tracing.start()} once, and then create multiple trace chunks with {@link Tracing#startChunk Tracing.startChunk()} and
   * {@link Tracing#stopChunk Tracing.stopChunk()}.
   * <pre>{@code
   * context.tracing().start(new Tracing.StartOptions()
   *   .setScreenshots(true)
   *   .setSnapshots(true));
   * Page page = context.newPage();
   * page.navigate("https://playwright.dev");
   *
   * context.tracing().startChunk();
   * page.click("text=Get Started");
   * // Everything between startChunk and stopChunk will be recorded in the trace.
   * context.tracing().stopChunk(new Tracing.StopChunkOptions()
   *   .setPath(Paths.get("trace1.zip")));
   *
   * context.tracing().startChunk();
   * page.navigate("http://example.com");
   * // Save a second trace file with different actions.
   * context.tracing().stopChunk(new Tracing.StopChunkOptions()
   *   .setPath(Paths.get("trace2.zip")));
   * }</pre>
   */
  void startChunk(StartChunkOptions options);
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
  /**
   * Stop the trace chunk. See {@link Tracing#startChunk Tracing.startChunk()} for more details about multiple trace chunks.
   */
  default void stopChunk() {
    stopChunk(null);
  }
  /**
   * Stop the trace chunk. See {@link Tracing#startChunk Tracing.startChunk()} for more details about multiple trace chunks.
   */
  void stopChunk(StopChunkOptions options);
}

