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

import com.microsoft.playwright.BrowserType.LaunchOptions;
import com.microsoft.playwright.impl.driver.Driver;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.Location;
import com.microsoft.playwright.options.MouseButton;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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
  void shouldCollectTrace1(@TempDir Path tempDir) throws Exception {
    context.tracing().start(new Tracing.StartOptions().setName("test")
      .setScreenshots(true).setSnapshots(true));
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<button>Click</button>");
    page.click("'Click'");
    page.close();
    Path traceFile = tempDir.resolve("trace.zip");
    context.tracing().stop(new Tracing.StopOptions().setPath(traceFile));

    assertTrue(Files.exists(traceFile));
    showTraceViewer(traceFile, traceViewer -> {
      assertThat(traceViewer.actionTitles()).hasText(new Pattern[] {
        Pattern.compile("Navigate to \"/empty.html\""),
        Pattern.compile("Set content"),
        Pattern.compile("Click"),
        Pattern.compile("Close")
      });
    });
  }

  @Test
  void shouldCollectTwoTraces(@TempDir Path tempDir) throws Exception {
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
    
    showTraceViewer(traceFile1, traceViewer -> {
      assertThat(traceViewer.actionTitles()).hasText(new Pattern[] {
        Pattern.compile("Navigate to \"/empty.html\""),
        Pattern.compile("Set content"),
        Pattern.compile("Click")
      });
    });
    
    showTraceViewer(traceFile2, traceViewer -> {
      assertThat(traceViewer.actionTitles()).hasText(new Pattern[] {
        Pattern.compile("Double click"),
        Pattern.compile("Close")
      });
    });
  }

  @Test
  void shouldWorkWithMultipleChunks(@TempDir Path tempDir) throws Exception {
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
    
    showTraceViewer(traceFile1, traceViewer -> {
      assertThat(traceViewer.actionTitles()).hasText(new Pattern[] {
        Pattern.compile("Set content"),
        Pattern.compile("Click")
      });
      traceViewer.selectSnapshot("After");
      FrameLocator frame = traceViewer.snapshotFrame("Set content", 0, false);
      assertThat(frame.locator("button")).hasText("Click");
    });
    
    showTraceViewer(traceFile2, traceViewer -> {
      assertThat(traceViewer.actionTitles()).containsText(new String[] {"Hover"});
      FrameLocator frame = traceViewer.snapshotFrame("Hover", 0, false);
      assertThat(frame.locator("button")).hasText("Click");
    });
  }

  @Test
  void shouldCollectSources(@TempDir Path tmpDir) throws Exception {
    Assumptions.assumeTrue(System.getenv("PLAYWRIGHT_JAVA_SRC") != null, "PLAYWRIGHT_JAVA_SRC must point to the directory containing this test source.");
    context.tracing().start(new Tracing.StartOptions().setSources(true));
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<button>Click</button>");
    myMethodOuter();
    Path trace = tmpDir.resolve("trace1.zip");
    context.tracing().stop(new Tracing.StopOptions().setPath(trace));

    showTraceViewer(trace, traceViewer -> {
      assertThat(traceViewer.actionTitles()).hasText(new Pattern[] {
        Pattern.compile("Navigate to \"/empty.html\""),
        Pattern.compile("Set content"),
        Pattern.compile("Click")
      });
      traceViewer.showSourceTab();
      assertThat(traceViewer.stackFrames()).containsText(new Pattern[] {
        Pattern.compile("myMethodInner"),
        Pattern.compile("myMethodOuter"),
        Pattern.compile("shouldCollectSources")
      });
      traceViewer.selectAction("Set content");
      assertThat(traceViewer.page().locator(".source-tab-file-name"))
        .hasAttribute("title", Pattern.compile(".*TestTracing\\.java"));
      assertThat(traceViewer.page().locator(".source-line-running"))
        .containsText("page.setContent(\"<button>Click</button>\");");
    });
  }

  private void myMethodOuter() {
    myMethodInner();
  }

  private void myMethodInner() {
    page.getByText("Click").click();
  }

  @Test
  void shouldNotFailWhenSourcesSetExplicitlyToFalse() throws IOException {
    Assumptions.assumeTrue(System.getenv("PLAYWRIGHT_JAVA_SRC") == null, "PLAYWRIGHT_JAVA_SRC must not be set for this test");
    context.tracing().start(new Tracing.StartOptions().setSources(false));
  }

  @Test
  void shouldRespectTracesDirAndName(@TempDir Path tempDir) throws Exception {
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
      
      showTraceViewer(tempDir.resolve("trace1.zip"), traceViewer -> {
        assertThat(traceViewer.actionTitles()).hasText(new Pattern[] {
          Pattern.compile("Navigate to \"/one-style.html\"")
        });
        FrameLocator frame = traceViewer.snapshotFrame("Navigate", 0, false);
        assertThat(frame.locator("body")).hasCSS("background-color", "rgb(255, 192, 203)");
        assertThat(frame.locator("body")).hasText("hello, world!");
      });
      
      showTraceViewer(tempDir.resolve("trace2.zip"), traceViewer -> {
        assertThat(traceViewer.actionTitles()).hasText(new Pattern[] {
          Pattern.compile("Navigate to \"/har.html\"")
        });
        FrameLocator frame = traceViewer.snapshotFrame("Navigate", 0, false);
        assertThat(frame.locator("body")).hasCSS("background-color", "rgb(255, 192, 203)");
        assertThat(frame.locator("body")).hasText("hello, world!");
      });
    }
  }

  @Test
  void canCallTracingGroupGroupEndAtAnyTimeAndAutoClose(@TempDir Path tempDir) throws Exception {
    context.tracing().group("ignored");
    context.tracing().groupEnd();
    context.tracing().group("ignored2");

    context.tracing().start(new Tracing.StartOptions());
    context.tracing().group("actual");
    page.navigate(server.EMPTY_PAGE);
    Path traceFile1 = tempDir.resolve("trace1.zip");
    context.tracing().stopChunk(new Tracing.StopChunkOptions().setPath(traceFile1));

    context.tracing().group("ignored3");
    context.tracing().groupEnd();
    context.tracing().groupEnd();
    context.tracing().groupEnd();

    showTraceViewer(traceFile1, traceViewer -> {
      assertThat(traceViewer.actionTitles()).containsText(new String[] {"actual", "Navigate to \"/empty.html\""});
    });
  }

  @Test
  void traceGroupGroupEnd(@TempDir Path tempDir) throws Exception {
    context.tracing().start(new Tracing.StartOptions());
    context.tracing().group("outer group");
    page.navigate("data:text/html,<!DOCTYPE html><body><div>Hello world</div></body>");
    context.tracing().group("inner group 1", new Tracing.GroupOptions().setLocation(new Location("foo.java").setLine(17).setColumn(1)));
    page.locator("body").click();
    context.tracing().groupEnd();
    context.tracing().group("inner group 2");
    assertTrue(page.locator("text=Hello").isVisible());
    context.tracing().groupEnd();
    context.tracing().groupEnd();

    Path traceFile1 = tempDir.resolve("trace1.zip");
    context.tracing().stop(new Tracing.StopOptions().setPath(traceFile1));

    showTraceViewer(traceFile1, traceViewer -> {
      traceViewer.expandAction("inner group 1");
      assertThat(traceViewer.actionTitles()).hasText(new Pattern[] {
        Pattern.compile("outer group"),
        Pattern.compile("Navigate to \"data:"),
        Pattern.compile("inner group 1"),
        Pattern.compile("Click"),
        Pattern.compile("inner group 2"),
      });
    });
  }

  @Test
  void shouldTraceVariousAPIs(@TempDir Path tempDir) throws Exception {
    context.tracing().start(new Tracing.StartOptions());

    page.clock().install();

    page.setContent("<input type='text' />");
    page.locator("input").click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));
    page.getByRole(AriaRole.TEXTBOX).click();
    page.keyboard().type("Hello world this is a very long string what happens when it overflows?");
    page.keyboard().press("Control+c");
    page.keyboard().down("Shift");
    page.keyboard().insertText("Hello world");
    page.keyboard().up("Shift");
    page.mouse().move(0, 0);
    page.mouse().down();
    page.mouse().move(100, 200);
    page.mouse().wheel(5, 7);
    page.mouse().up();
    page.clock().fastForward(1000);
    page.clock().fastForward("30:00");
    page.clock().pauseAt("2050-02-02");
    page.clock().runFor(10);
    page.clock().setFixedTime("2050-02-02");
    page.clock().setSystemTime("2050-02-02");

    page.clock().resume();

    page.locator("input").click(new Locator.ClickOptions().setButton(MouseButton.RIGHT));

    Path traceFile1 = tempDir.resolve("trace1.zip");
    context.tracing().stop(new Tracing.StopOptions().setPath(traceFile1));

    showTraceViewer(traceFile1, traceViewer -> {
      assertThat(traceViewer.actionTitles()).hasText(new Pattern[] {
        Pattern.compile("Install clock"),
        Pattern.compile("Set content"),
        Pattern.compile("Click"),
        Pattern.compile("Click"),
        Pattern.compile("Type"),
        Pattern.compile("Press"),
        Pattern.compile("Key down"),
        Pattern.compile("Insert"),
        Pattern.compile("Key up"),
        Pattern.compile("Mouse move"),
        Pattern.compile("Mouse down"),
        Pattern.compile("Mouse move"),
        Pattern.compile("Mouse wheel"),
        Pattern.compile("Mouse up"),
        Pattern.compile("Fast forward clock"),
        Pattern.compile("Fast forward clock"),
        Pattern.compile("Pause clock"),
        Pattern.compile("Run clock"),
        Pattern.compile("Set fixed time"),
        Pattern.compile("Set system time"),
        Pattern.compile("Resume clock"),
        Pattern.compile("Click")
      });
    });
  }

  @Test
  public void shouldNotRecordNetworkActions(@TempDir Path tempDir) throws Exception {
    context.tracing().start(new Tracing.StartOptions());

    page.onRequest(request -> {
      request.allHeaders();
    });
    page.onResponse(response -> {
      response.text();
    });
    page.navigate(server.EMPTY_PAGE);

    Path traceFile1 = tempDir.resolve("trace1.zip");
    context.tracing().stop(new Tracing.StopOptions().setPath(traceFile1));

    showTraceViewer(traceFile1, traceViewer -> {
      assertThat(traceViewer.actionTitles()).hasText(new Pattern[] {
        Pattern.compile("Navigate to \"/empty.html\"")
      });
    });
  }

  private void showTraceViewer(Path tracePath, TraceViewerConsumer callback) throws Exception {
    Path driverDir = Driver.ensureDriverInstalled(java.util.Collections.emptyMap(), true).driverDir();
    Path traceViewerPath = driverDir.resolve("package").resolve("lib").resolve("vite").resolve("traceViewer");
    Server traceServer = Server.createHttp(Utils.nextFreePort());
    traceServer.setResourceProvider(path -> {
      Path filePath = traceViewerPath.resolve(path.substring(1));
      if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
        try {
          return Files.newInputStream(filePath);
        } catch (IOException e) {
          return null;
        }
      }
      return null;
    });
    traceServer.setRoute("/trace.zip", exchange -> {
      exchange.getResponseHeaders().add("Content-Type", "application/zip");
      exchange.sendResponseHeaders(200, Files.size(tracePath));
      Files.copy(tracePath, exchange.getResponseBody());
      exchange.getResponseBody().close();
    });
    
    try (Browser browser = browserType.launch(createLaunchOptions());
         BrowserContext context = browser.newContext()) {
      Page page = context.newPage();
      page.navigate(traceServer.PREFIX + "/index.html?trace=" + traceServer.PREFIX + "/trace.zip");
      
      TraceViewerPage traceViewer = new TraceViewerPage(page);
      callback.accept(traceViewer);
    } finally {
      traceServer.stop();
    }
  }
  
  @FunctionalInterface
  interface TraceViewerConsumer {
    void accept(TraceViewerPage traceViewer) throws Exception;
  }
}

class TraceViewerPage {
  private final Page page;

  public TraceViewerPage(Page page) {
    this.page = page;
  }

  public Page page() {
    return page;
  }

  public Locator actionsTree() {
    return page.getByTestId("actions-tree");
  }

  public Locator actionTitles() {
    return page.locator(".action-title");
  }

  public Locator stackFrames() {
    return this.page.getByRole(AriaRole.LIST, new Page.GetByRoleOptions().setName("stack trace")).getByRole(AriaRole.LISTITEM);
  }

  public void selectAction(String title, int ordinal) {
    this.actionsTree().getByTitle(title).nth(ordinal).click();
  }

  public void selectAction(String title) {
    selectAction(title, 0);
  }

  public void selectSnapshot(String name) {
    this.page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName(name)).click();
  }

  public FrameLocator snapshotFrame(String actionName, int ordinal, boolean hasSubframe) {
    selectAction(actionName, ordinal);
    while (page.frames().size() < (hasSubframe ? 4 : 3)) {
      page.waitForTimeout(200);
    }
    return page.frameLocator("iframe.snapshot-visible[name=snapshot]");
  }

  public FrameLocator snapshotFrame(String actionName, int ordinal) {
    return snapshotFrame(actionName, ordinal, false);
  }

  public void showSourceTab() {
    page.getByRole(AriaRole.TAB, new Page.GetByRoleOptions().setName("Source")).click();
  }

  public void expandAction(String title) {
    this.actionsTree().getByRole(AriaRole.TREEITEM, new Locator.GetByRoleOptions().setName(title)).locator(".codicon-chevron-right").click();
  }
}
