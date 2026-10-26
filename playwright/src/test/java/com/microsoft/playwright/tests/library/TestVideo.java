/**
 * Copyright Microsoft Corporation. All rights reserved.
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

package com.microsoft.playwright.tests.library;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestInfo;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.microsoft.playwright.fixtures.Utils.rafraf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/library/video.spec.ts. */
public class TestVideo {
  // Java: ensureSomeFrames() of upstream's tests/config/utils.ts.
  private static void ensureSomeFrames(Page page) {
    rafraf(page, 100);
    page.screenshot();
  }

  private static List<Path> findVideos(Path videoDir) throws IOException {
    try (Stream<Path> files = Files.list(videoDir)) {
      return files.filter(file -> file.getFileName().toString().endsWith("webm")).collect(Collectors.toList());
    }
  }

  // VideoPlayer, expectFrames() and expectRedFrames() are not in the harness: the tests that inspect
  // the frames of a video (VI-003, VI-004, VI-005, VI-012 to VI-018, VI-020, VI-021, VI-025, VI-028 to VI-030)
  // are TODO harness.

  @PlaywrightTest
  @DisplayName("VI-001 should not have video by default")
  @Fixme("java: page.video() returns a Video also when the context does not record video (PageImpl creates a VideoImpl without an artifact): expected: <null> but was: <com.microsoft.playwright.impl.VideoImpl@...>")
  void shouldNotHaveVideoByDefault(Page page) {
    assertNull(page.video());
  }

  // VI-002 passes recordVideo: {}: not ported.

  @PlaywrightTest
  @DisplayName("VI-006 should expose video path")
  void shouldExposeVideoPath(Browser browser, TestInfo testInfo) {
    Path videosPath = testInfo.outputPath("");
    int[] size = {320, 240};
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setRecordVideoDir(videosPath)
      .setRecordVideoSize(size[0], size[1])
      .setViewportSize(size[0], size[1]));
    Page page = context.newPage();
    page.evaluate("() => document.body.style.backgroundColor = 'red'");
    Path path = page.video().path();
    assertTrue(path.toString().contains(videosPath.toString()));
    context.close();
    assertTrue(Files.exists(path));
  }

  @PlaywrightTest
  @DisplayName("VI-007 should delete video")
  void shouldDeleteVideo(Browser browser, TestInfo testInfo) throws Exception {
    Path videosPath = testInfo.outputPath("");
    int[] size = {320, 240};
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setRecordVideoDir(videosPath)
      .setRecordVideoSize(size[0], size[1])
      .setViewportSize(size[0], size[1]));
    Page page = context.newPage();
    page.evaluate("() => document.body.style.backgroundColor = 'red'");
    ensureSomeFrames(page);
    context.close();

    Path videoPath = page.video().path();
    // Java: video.delete() waits for the video to be written, so it runs after the context is closed.
    page.video().delete();
    assertFalse(Files.exists(videoPath));
  }

  @PlaywrightTest
  @DisplayName("VI-008 should expose video path blank page")
  void shouldExposeVideoPathBlankPage(Browser browser, TestInfo testInfo) {
    Path videosPath = testInfo.outputPath("");
    int[] size = {320, 240};
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setRecordVideoDir(videosPath)
      .setRecordVideoSize(size[0], size[1])
      .setViewportSize(size[0], size[1]));
    Page page = context.newPage();
    Path path = page.video().path();
    assertTrue(path.toString().contains(videosPath.toString()));
    context.close();
    assertTrue(Files.exists(path));
  }

  @PlaywrightTest
  @DisplayName("VI-009 should work with weird screen resolution")
  void shouldWorkWithWeirdScreenResolution(Browser browser, TestInfo testInfo) {
    // { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/22069' }
    Path videosPath = testInfo.outputPath("");
    int[] size = {1904, 609};
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setRecordVideoDir(videosPath)
      .setRecordVideoSize(size[0], size[1])
      .setViewportSize(size[0], size[1]));
    Page page = context.newPage();
    Path path = page.video().path();
    assertTrue(path.toString().contains(videosPath.toString()));
    context.close();
    assertTrue(Files.exists(path));
  }

  @PlaywrightTest
  @DisplayName("VI-010 should work with relative path for recordVideo.dir")
  void shouldWorkWithRelativePathForRecordVideoDir(Browser browser, TestInfo testInfo) {
    // { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/27086' }
    Path videosPath = Paths.get("").toAbsolutePath().relativize(testInfo.outputPath(""));
    int[] size = {320, 240};
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setRecordVideoDir(videosPath)
      .setRecordVideoSize(size[0], size[1])
      .setViewportSize(size[0], size[1]));
    Page page = context.newPage();
    Path videoPath = page.video().path();
    context.close();
    assertTrue(Files.exists(videoPath));
  }

  @PlaywrightTest
  @DisplayName("VI-011 should expose video path blank popup")
  void shouldExposeVideoPathBlankPopup(Browser browser, TestInfo testInfo) {
    Path videosPath = testInfo.outputPath("");
    int[] size = {320, 240};
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setRecordVideoDir(videosPath)
      .setRecordVideoSize(size[0], size[1])
      .setViewportSize(size[0], size[1]));
    Page page = context.newPage();
    Page popup = page.waitForPopup(() -> page.evaluate("window.open(\"about:blank\")"));
    Path path = popup.video().path();
    assertTrue(path.toString().contains(videosPath.toString()));
    context.close();
    assertTrue(Files.exists(path));
  }

  @PlaywrightTest
  @DisplayName("VI-019 should throw on invalid fps")
  void shouldThrowOnInvalidFps(Browser browser, TestInfo testInfo) {
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> browser.newContext(new Browser.NewContextOptions()
      .setRecordVideoDir(testInfo.outputPath(""))
      .setRecordVideoFps(0)));
    assertTrue(error.getMessage().contains("\"recordVideo.fps\" must be a positive number, got 0"));
  }

  @PlaywrightTest
  @DisplayName("VI-023 should throw on browser close")
  void shouldThrowOnBrowserClose(BrowserType browserType, TestInfo testInfo) {
    int[] size = {320, 240};
    Browser browser = browserType.launch(Env.launchOptions());
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setRecordVideoDir(testInfo.outputPath(""))
      .setRecordVideoSize(size[0], size[1])
      .setViewportSize(size[0], size[1]));

    Page page = context.newPage();
    ensureSomeFrames(page);
    browser.close();

    Path file = testInfo.outputPath("saved-video-");
    PlaywrightException saveResult = assertThrows(PlaywrightException.class, () -> page.video().saveAs(file));
    assertTrue(saveResult.getMessage().contains("browser has been closed"));
  }

  // VI-024 kills the browser with _channel.killForTests() of the Node.js client: not ported.

  @PlaywrightTest
  @DisplayName("VI-026 should close ffmpeg even if there were no frames")
  void shouldCloseFfmpegEvenIfThereWereNoFrames(BrowserType browserType, TestInfo testInfo) throws Exception {
    // { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/39872' }
    int[] size = {320, 240};
    Browser browser = browserType.launch(Env.launchOptions());

    Path videoDir = testInfo.outputPath("");
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setRecordVideoDir(videoDir)
      .setRecordVideoSize(size[0], size[1])
      .setViewportSize(size[0], size[1]));

    Page page1 = context.newPage();
    page1.close();

    Page page2 = context.newPage();
    page2.close();

    context.close();
    browser.close();

    List<Path> videoFiles = findVideos(videoDir);
    assertEquals(2, videoFiles.size());
  }

  @PlaywrightTest
  @DisplayName("VI-027 should not create video for internal pages")
  void shouldNotCreateVideoForInternalPages(Browser browser, TestServer server, TestInfo testInfo) throws Exception {
    server.setRoute("/empty.html", (req, res) -> {
      res.setHeader("Set-Cookie", "name=value");
      res.end();
    });

    Path videoDir = testInfo.outputPath("");
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setRecordVideoDir(videoDir));

    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    ensureSomeFrames(page);

    List<com.microsoft.playwright.options.Cookie> cookies = context.cookies();
    assertEquals(1, cookies.size());
    context.storageState();
    context.close();

    try (Stream<Path> files = Files.list(videoDir)) {
      assertEquals(1, files.count());
    }
  }

  @PlaywrightTest
  @DisplayName("VI-031 should saveAs video")
  void shouldSaveAsVideo(Browser browser, TestInfo testInfo) {
    Path videosPath = testInfo.outputPath("");
    int[] size = {320, 240};
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setRecordVideoDir(videosPath)
      .setRecordVideoSize(size[0], size[1])
      .setViewportSize(size[0], size[1]));
    Page page = context.newPage();
    page.evaluate("() => document.body.style.backgroundColor = 'red'");
    ensureSomeFrames(page);
    context.close();

    Path saveAsPath = testInfo.outputPath("my-video.webm");
    page.video().saveAs(saveAsPath);
    assertTrue(Files.exists(saveAsPath));
  }
}
