/**
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

package com.microsoft.playwright.tests.library;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Screencast;
import com.microsoft.playwright.ScreencastFrame;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestInfo;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.microsoft.playwright.fixtures.Utils.kTargetClosedErrorMessage;
import static com.microsoft.playwright.fixtures.Utils.rafraf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/screencast.spec.ts. */
public class TestScreencast {
  // ensureSomeFrames() of tests/config/utils.ts.
  private static void ensureSomeFrames(Page page) {
    rafraf(page, 100);
    page.screenshot();
  }

  // Java: lists the *.webm files of a directory, `fs.readdirSync(dir).filter(f => f.endsWith('.webm'))`.
  private static List<String> webmFiles(Path dir) throws IOException {
    try (Stream<Path> files = Files.list(dir)) {
      return files.map(f -> f.getFileName().toString()).filter(f -> f.endsWith(".webm")).collect(Collectors.toList());
    }
  }

  @PlaywrightTest
  @DisplayName("SC-001 screencast.start delivers frames via onFrame callback")
  void screencastStartDeliversFramesViaOnFrameCallback(Browser browser, TestServer server, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.FIREFOX && Env.isMac && Env.headed(), "fixme: wrong frame size in headed Firefox on Mac");

    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1000, 400));
    Page page = context.newPage();

    List<byte[]> frames = new CopyOnWriteArrayList<>();
    page.screencast().start(new Screencast.StartOptions().setOnFrame(frame -> frames.add(frame.data())).setSize(500, 400));
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("() => document.body.style.backgroundColor = 'red'");
    ensureSomeFrames(page);
    page.screencast().stop();

    assertTrue(frames.size() > 0);
    for (byte[] frame : frames) {
      // Each frame must be a valid JPEG (starts with FF D8)
      // Java: bytes are signed.
      assertEquals(0xff, frame[0] & 0xff);
      assertEquals(0xd8, frame[1] & 0xff);
      int[] dimensions = jpegDimensions(frame);
      int width = dimensions[0];
      int height = dimensions[1];
      // Frame should be scaled down to fit the maximum size.
      assertEquals(500, width);
      assertEquals(200, height);
    }

    context.close();
  }

  // SC-002 applies backpressure while async onFrame callback is pending: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("SC-003 onFrame receives viewport size")
  void onFrameReceivesViewportSize(Browser browser, TestServer server, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.FIREFOX && Env.isMac && Env.headed(), "fixme: wrong frame size in headed Firefox on Mac");

    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1000, 400));
    Page page = context.newPage();

    List<ScreencastFrame> frames = new CopyOnWriteArrayList<>();
    page.screencast().start(new Screencast.StartOptions()
      .setOnFrame(frame -> frames.add(frame))
      .setSize(500, 400));
    page.navigate(server.EMPTY_PAGE);
    ensureSomeFrames(page);
    page.screencast().stop();

    assertTrue(frames.size() > 0);
    for (ScreencastFrame frame : frames) {
      assertEquals(1000, frame.viewportWidth());
      assertEquals(400, frame.viewportHeight());
      // typeof frame.timestamp is 'number': timestamp() returns a double.
    }

    context.close();
  }

  @PlaywrightTest
  @DisplayName("SC-004 start throws if screencast is already started")
  void startThrowsIfScreencastIsAlreadyStarted(Browser browser) {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(500, 400));
    Page page = context.newPage();

    page.screencast().start(new Screencast.StartOptions().setOnFrame(frame -> {}));
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.screencast().start(new Screencast.StartOptions().setOnFrame(frame -> {})));
    assertTrue(error.getMessage().contains("Screencast is already started"));

    page.screencast().stop();
    context.close();
  }

  @PlaywrightTest
  @DisplayName("SC-005 start allows restart with different options after stop")
  void startAllowsRestartWithDifferentOptionsAfterStop(Browser browser) {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(500, 400));
    Page page = context.newPage();

    page.screencast().start(new Screencast.StartOptions().setOnFrame(frame -> {}).setSize(500, 400));
    page.screencast().stop();
    // Different options should succeed once the previous screencast is stopped.
    page.screencast().start(new Screencast.StartOptions().setOnFrame(frame -> {}).setSize(320, 240));
    page.screencast().stop();
    context.close();
  }

  @PlaywrightTest
  @DisplayName("SC-006 start returns a disposable that stops screencast")
  void startReturnsADisposableThatStopsScreencast(Browser browser, TestServer server) {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(500, 400));
    Page page = context.newPage();

    List<byte[]> frames = new CopyOnWriteArrayList<>();
    page.screencast().start(new Screencast.StartOptions().setOnFrame(frame -> frames.add(frame.data())).setSize(500, 400));
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("() => document.body.style.backgroundColor = 'red'");
    ensureSomeFrames(page);
    page.screencast().stop();

    int frameCountAfterDispose = frames.size();
    assertTrue(frameCountAfterDispose > 0);

    // No more frames should arrive after dispose.
    page.evaluate("() => document.body.style.backgroundColor = 'blue'");
    ensureSomeFrames(page);
    assertEquals(frameCountAfterDispose, frames.size());

    context.close();
  }

  @PlaywrightTest
  @DisplayName("SC-007 start/stop twice without path creates two files in artifactsDir")
  void startStopTwiceWithoutPathCreatesTwoFilesInArtifactsDir(BrowserType browserType, TestInfo testInfo) throws Exception {
    Path artifactsDir = testInfo.outputPath("artifacts");
    Browser browser = browserType.launch(Env.launchOptions().setArtifactsDir(artifactsDir));
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(800, 800));
    Page page = context.newPage();

    page.screencast().start(new Screencast.StartOptions().setPath(testInfo.outputPath("video1.webm")).setSize(800, 800));
    page.evaluate("() => document.body.style.backgroundColor = 'red'");
    ensureSomeFrames(page);
    page.screencast().stop();

    page.screencast().start(new Screencast.StartOptions().setPath(testInfo.outputPath("video2.webm")).setSize(800, 800));
    page.evaluate("() => document.body.style.backgroundColor = 'blue'");
    ensureSomeFrames(page);
    page.screencast().stop();

    List<String> videoFiles = webmFiles(artifactsDir);
    assertEquals(2, videoFiles.size());

    context.close();
    browser.close();
  }

  @PlaywrightTest
  @DisplayName("SC-008 start should work when recordVideo is set")
  void startShouldWorkWhenRecordVideoIsSet(Browser browser, TestInfo testInfo) throws Exception {
    Path autoDir = testInfo.outputPath("auto");
    Path manualDir = testInfo.outputPath("manual");
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setRecordVideoDir(autoDir));
    Page page = context.newPage();

    page.screencast().start(new Screencast.StartOptions().setPath(manualDir.resolve("video.webm")));
    page.evaluate("() => document.body.style.backgroundColor = 'blue'");
    ensureSomeFrames(page);
    page.screencast().stop();
    List<String> videoFiles1 = webmFiles(manualDir);
    assertEquals(1, videoFiles1.size());

    context.close();
    List<String> videoFiles2 = webmFiles(autoDir);
    assertEquals(1, videoFiles2.size());
  }

  // SC-009 start should record video with the requested fps: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("SC-010 start should throw on invalid fps")
  void startShouldThrowOnInvalidFps(Page page, TestInfo testInfo) {
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.screencast().start(new Screencast.StartOptions().setPath(testInfo.outputPath("video.webm")).setFps(-1)));
    assertTrue(error.getMessage().contains("\"fps\" must be a positive number, got -1"));
  }

  @PlaywrightTest
  @DisplayName("SC-011 start should fail when another recording is in progress")
  void startShouldFailWhenAnotherRecordingIsInProgress(Page page, TestInfo testInfo) {
    page.screencast().start(new Screencast.StartOptions().setPath(testInfo.outputPath("video.webm")));
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.screencast().start(new Screencast.StartOptions().setPath(testInfo.outputPath("video2.webm"))));
    assertTrue(error.getMessage().contains("Screencast is already started"));
  }

  @PlaywrightTest
  @DisplayName("SC-012 stop should not fail when no recording is in progress")
  void stopShouldNotFailWhenNoRecordingIsInProgress(Browser browser) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    // stop() is a no-op when screencast is not started.
    page.screencast().stop();
    context.close();
  }

  @PlaywrightTest
  @DisplayName("SC-013 start should finish when page is closed")
  void startShouldFinishWhenPageIsClosed(Browser browser, TestInfo testInfo) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    Path videoPath = testInfo.outputPath("video.webm");
    page.screencast().start(new Screencast.StartOptions().setPath(videoPath).setSize(800, 800));
    page.evaluate("() => document.body.style.backgroundColor = 'red'");
    ensureSomeFrames(page);
    page.close();
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.screencast().stop());
    assertTrue(error.getMessage().contains(kTargetClosedErrorMessage));
    context.close();
  }

  // SC-014 empty video: not ported, see upstream_not_ported.txt.

  // SC-015 start dispose stops recording: not ported, see upstream_not_ported.txt.

  private static int[] jpegDimensions(byte[] buffer) {
    int i = 2; // skip SOI marker (FF D8)
    while (i < buffer.length - 8) {
      if ((buffer[i] & 0xff) != 0xFF) {
        break;
      }
      int marker = buffer[i + 1] & 0xff;
      int segmentLength = ((buffer[i + 2] & 0xff) << 8) | (buffer[i + 3] & 0xff);
      // SOF markers: C0 (baseline), C2 (progressive), C1, C3, C5-C7, C9-CB, CD-CF
      if ((marker >= 0xC0 && marker <= 0xC3) || (marker >= 0xC5 && marker <= 0xC7) ||
          (marker >= 0xC9 && marker <= 0xCB) || (marker >= 0xCD && marker <= 0xCF)) {
        int height = ((buffer[i + 5] & 0xff) << 8) | (buffer[i + 6] & 0xff);
        int width = ((buffer[i + 7] & 0xff) << 8) | (buffer[i + 8] & 0xff);
        return new int[] {width, height};
      }
      i += 2 + segmentLength;
    }
    throw new RuntimeException("Could not parse JPEG dimensions");
  }
}
