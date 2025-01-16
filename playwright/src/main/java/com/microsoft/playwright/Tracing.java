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

import com.microsoft.playwright.options.*;
import java.nio.file.Path;

/**
 * API for collecting and saving Playwright traces. Playwright traces can be opened in <a
 * href="https://playwright.dev/java/docs/trace-viewer">Trace Viewer</a> after Playwright script runs.
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
     * If specified, intermediate trace files are going to be saved into the files with the given name prefix inside the {@code
     * tracesDir} directory specified in {@link com.microsoft.playwright.BrowserType#launch BrowserType.launch()}. To specify
     * the final trace zip file name, you need to pass {@code path} option to {@link com.microsoft.playwright.Tracing#stop
     * Tracing.stop()} instead.
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
     * provided via {@code PLAYWRIGHT_JAVA_SRC} environment variable (the paths should be separated by ';' on Windows and by
     * ':' on other platforms).
     */
    public Boolean sources;
    /**
     * Trace name to be shown in the Trace Viewer.
     */
    public String title;

    /**
     * If specified, intermediate trace files are going to be saved into the files with the given name prefix inside the {@code
     * tracesDir} directory specified in {@link com.microsoft.playwright.BrowserType#launch BrowserType.launch()}. To specify
     * the final trace zip file name, you need to pass {@code path} option to {@link com.microsoft.playwright.Tracing#stop
     * Tracing.stop()} instead.
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
     * provided via {@code PLAYWRIGHT_JAVA_SRC} environment variable (the paths should be separated by ';' on Windows and by
     * ':' on other platforms).
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
     * If specified, intermediate trace files are going to be saved into the files with the given name prefix inside the {@code
     * tracesDir} directory specified in {@link com.microsoft.playwright.BrowserType#launch BrowserType.launch()}. To specify
     * the final trace zip file name, you need to pass {@code path} option to {@link com.microsoft.playwright.Tracing#stopChunk
     * Tracing.stopChunk()} instead.
     */
    public String name;
    /**
     * Trace name to be shown in the Trace Viewer.
     */
    public String title;

    /**
     * If specified, intermediate trace files are going to be saved into the files with the given name prefix inside the {@code
     * tracesDir} directory specified in {@link com.microsoft.playwright.BrowserType#launch BrowserType.launch()}. To specify
     * the final trace zip file name, you need to pass {@code path} option to {@link com.microsoft.playwright.Tracing#stopChunk
     * Tracing.stopChunk()} instead.
     */
    public StartChunkOptions setName(String name) {
      this.name = name;
      return this;
    }
    /**
     * Trace name to be shown in the Trace Viewer.
     */
    public StartChunkOptions setTitle(String title) {
      this.title = title;
      return this;
    }
  }
  class GroupOptions {
    /**
     * Specifies a custom location for the group to be shown in the trace viewer. Defaults to the location of the {@link
     * com.microsoft.playwright.Tracing#group Tracing.group()} call.
     */
    public Location location;

    /**
     * Specifies a custom location for the group to be shown in the trace viewer. Defaults to the location of the {@link
     * com.microsoft.playwright.Tracing#group Tracing.group()} call.
     */
    public GroupOptions setLocation(String file) {
      return setLocation(new Location(file));
    }
    /**
     * Specifies a custom location for the group to be shown in the trace viewer. Defaults to the location of the {@link
     * com.microsoft.playwright.Tracing#group Tracing.group()} call.
     */
    public GroupOptions setLocation(Location location) {
      this.location = location;
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
     * Export trace collected since the last {@link com.microsoft.playwright.Tracing#startChunk Tracing.startChunk()} call into
     * the file with the given path.
     */
    public Path path;

    /**
     * Export trace collected since the last {@link com.microsoft.playwright.Tracing#startChunk Tracing.startChunk()} call into
     * the file with the given path.
     */
    public StopChunkOptions setPath(Path path) {
      this.path = path;
      return this;
    }
  }
  /**
   * Start tracing.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * context.tracing().start(new Tracing.StartOptions()
   *   .setScreenshots(true)
   *   .setSnapshots(true));
   * Page page = context.newPage();
   * page.navigate("https://playwright.dev");
   * context.tracing().stop(new Tracing.StopOptions()
   *   .setPath(Paths.get("trace.zip")));
   * }</pre>
   *
   * @since v1.12
   */
  default void start() {
    start(null);
  }
  /**
   * Start tracing.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * context.tracing().start(new Tracing.StartOptions()
   *   .setScreenshots(true)
   *   .setSnapshots(true));
   * Page page = context.newPage();
   * page.navigate("https://playwright.dev");
   * context.tracing().stop(new Tracing.StopOptions()
   *   .setPath(Paths.get("trace.zip")));
   * }</pre>
   *
   * @since v1.12
   */
  void start(StartOptions options);
  /**
   * Start a new trace chunk. If you'd like to record multiple traces on the same {@code BrowserContext}, use {@link
   * com.microsoft.playwright.Tracing#start Tracing.start()} once, and then create multiple trace chunks with {@link
   * com.microsoft.playwright.Tracing#startChunk Tracing.startChunk()} and {@link com.microsoft.playwright.Tracing#stopChunk
   * Tracing.stopChunk()}.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * context.tracing().start(new Tracing.StartOptions()
   *   .setScreenshots(true)
   *   .setSnapshots(true));
   * Page page = context.newPage();
   * page.navigate("https://playwright.dev");
   *
   * context.tracing().startChunk();
   * page.getByText("Get Started").click();
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
   *
   * @since v1.15
   */
  default void startChunk() {
    startChunk(null);
  }
  /**
   * Start a new trace chunk. If you'd like to record multiple traces on the same {@code BrowserContext}, use {@link
   * com.microsoft.playwright.Tracing#start Tracing.start()} once, and then create multiple trace chunks with {@link
   * com.microsoft.playwright.Tracing#startChunk Tracing.startChunk()} and {@link com.microsoft.playwright.Tracing#stopChunk
   * Tracing.stopChunk()}.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * context.tracing().start(new Tracing.StartOptions()
   *   .setScreenshots(true)
   *   .setSnapshots(true));
   * Page page = context.newPage();
   * page.navigate("https://playwright.dev");
   *
   * context.tracing().startChunk();
   * page.getByText("Get Started").click();
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
   *
   * @since v1.15
   */
  void startChunk(StartChunkOptions options);
  /**
   * <strong>NOTE:</strong> Use {@code test.step} instead when available.
   *
   * <p> Creates a new group within the trace, assigning any subsequent API calls to this group, until {@link
   * com.microsoft.playwright.Tracing#groupEnd Tracing.groupEnd()} is called. Groups can be nested and will be visible in the
   * trace viewer.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * // All actions between group and groupEnd
   * // will be shown in the trace viewer as a group.
   * page.context().tracing().group("Open Playwright.dev > API");
   * page.navigate("https://playwright.dev/");
   * page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("API")).click();
   * page.context().tracing().groupEnd();
   * }</pre>
   *
   * @param name Group name shown in the trace viewer.
   * @since v1.49
   */
  default void group(String name) {
    group(name, null);
  }
  /**
   * <strong>NOTE:</strong> Use {@code test.step} instead when available.
   *
   * <p> Creates a new group within the trace, assigning any subsequent API calls to this group, until {@link
   * com.microsoft.playwright.Tracing#groupEnd Tracing.groupEnd()} is called. Groups can be nested and will be visible in the
   * trace viewer.
   *
   * <p> <strong>Usage</strong>
   * <pre>{@code
   * // All actions between group and groupEnd
   * // will be shown in the trace viewer as a group.
   * page.context().tracing().group("Open Playwright.dev > API");
   * page.navigate("https://playwright.dev/");
   * page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("API")).click();
   * page.context().tracing().groupEnd();
   * }</pre>
   *
   * @param name Group name shown in the trace viewer.
   * @since v1.49
   */
  void group(String name, GroupOptions options);
  /**
   * Closes the last group created by {@link com.microsoft.playwright.Tracing#group Tracing.group()}.
   *
   * @since v1.49
   */
  void groupEnd();
  /**
   * Stop tracing.
   *
   * @since v1.12
   */
  default void stop() {
    stop(null);
  }
  /**
   * Stop tracing.
   *
   * @since v1.12
   */
  void stop(StopOptions options);
  /**
   * Stop the trace chunk. See {@link com.microsoft.playwright.Tracing#startChunk Tracing.startChunk()} for more details
   * about multiple trace chunks.
   *
   * @since v1.15
   */
  default void stopChunk() {
    stopChunk(null);
  }
  /**
   * Stop the trace chunk. See {@link com.microsoft.playwright.Tracing#startChunk Tracing.startChunk()} for more details
   * about multiple trace chunks.
   *
   * @since v1.15
   */
  void stopChunk(StopChunkOptions options);
}

