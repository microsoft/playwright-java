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

import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.options.AnnotatePosition;
import com.microsoft.playwright.options.ScreencastActionStyle;
import com.microsoft.playwright.options.ScreencastCursor;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class TestScreencast {
  private static Browser.NewContextOptions recordVideoOptions(Path videosDir) {
    return new Browser.NewContextOptions()
      .setRecordVideoDir(videosDir)
      .setRecordVideoSize(320, 240)
      .setViewportSize(320, 240);
  }

  @PlaywrightTest
  void shouldExposeVideoPath(Browser browser, @TempDir Path videosDir) {
    Path path;
    try (BrowserContext context = browser.newContext(recordVideoOptions(videosDir))) {
      Page page = context.newPage();
      page.evaluate("() => document.body.style.backgroundColor = 'red'");
      path = page.video().path();
      assertTrue(path.startsWith(videosDir));
    }
    assertTrue(Files.exists(path));
  }

  @PlaywrightTest
  void shouldSaveAsVideo(Browser browser, @TempDir Path videosDir) {
    Video video;
    try (BrowserContext context = browser.newContext(recordVideoOptions(videosDir))) {
      Page page = context.newPage();
      page.evaluate("() => document.body.style.backgroundColor = 'red'");
      page.waitForTimeout(1000);
      video = page.video();
    }
    Path saveAsPath = videosDir.resolve("my-video.webm");
    video.saveAs(saveAsPath);
    assertTrue(Files.exists(saveAsPath));
  }

  @PlaywrightTest
  void shouldDeleteVideo(Browser browser, @TempDir Path videosDir) {
    Video video;
    try (BrowserContext context = browser.newContext(recordVideoOptions(videosDir))) {
      Page page = context.newPage();
      page.evaluate("() => document.body.style.backgroundColor = 'red'");
      page.waitForTimeout(1000);
      video = page.video();
    }
    video.delete();
    assertFalse(Files.exists(video.path()));
  }

  @PlaywrightTest
  void shouldWaitForVideoFinishWhenPageIsClosed(BrowserType browserType, @TempDir Path videosDir) throws IOException {
    try (Browser browser = browserType.launch(Env.launchOptions());
         BrowserContext context = browser.newContext(recordVideoOptions(videosDir))) {
      Page page = context.newPage();
      page.evaluate("() => document.body.style.backgroundColor = 'red'");
      page.waitForTimeout(500);
      // First close page manually.
      page.close();
    }
    List<Path> files = Files.list(videosDir).collect(Collectors.toList());
    assertEquals(1, files.size());
    assertTrue(Files.exists(files.get(0)));
    assertTrue(Files.size(files.get(0)) > 0);
  }

  @PlaywrightTest
  void screencastStartShouldDeliverFramesViaOnFrame(Page page, Server server) {
    List<ScreencastFrame> frames = new ArrayList<>();
    page.screencast().start(new Screencast.StartOptions().setOnFrame(frames::add));
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("() => document.body.style.backgroundColor = 'red'");
    page.waitForTimeout(500);
    page.screencast().stop();
    assertFalse(frames.isEmpty(), "expected at least one frame");
    // JPEG-encoded frames start with FF D8.
    for (ScreencastFrame frame : frames) {
      assertEquals((byte) 0xFF, frame.data()[0]);
      assertEquals((byte) 0xD8, frame.data()[1]);
    }
  }

  @PlaywrightTest
  void screencastStartShouldScaleFramesToFitSize(Browser browser, Server server) throws IOException {
    try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1000, 400))) {
      Page page = context.newPage();
      List<ScreencastFrame> frames = new ArrayList<>();
      page.screencast().start(new Screencast.StartOptions().setOnFrame(frames::add).setSize(500, 400));
      page.navigate(server.EMPTY_PAGE);
      page.evaluate("() => document.body.style.backgroundColor = 'red'");
      page.waitForTimeout(500);
      page.screencast().stop();
      assertFalse(frames.isEmpty(), "expected at least one frame");
      for (ScreencastFrame frame : frames) {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(frame.data()));
        // Frame should be scaled down to fit the maximum size.
        assertEquals(500, image.getWidth());
        assertEquals(200, image.getHeight());
      }
    }
  }

  @PlaywrightTest
  void onFrameShouldReceiveViewportSizeAndTimestamp(Browser browser, Server server) {
    try (BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1000, 400))) {
      Page page = context.newPage();
      List<ScreencastFrame> frames = new ArrayList<>();
      page.screencast().start(new Screencast.StartOptions().setOnFrame(frames::add).setSize(500, 400));
      page.navigate(server.EMPTY_PAGE);
      page.evaluate("() => document.body.style.backgroundColor = 'red'");
      page.waitForTimeout(500);
      page.screencast().stop();
      assertFalse(frames.isEmpty(), "expected at least one frame");
      for (ScreencastFrame frame : frames) {
        assertEquals(1000, frame.viewportWidth());
        assertEquals(400, frame.viewportHeight());
        assertTrue(frame.timestamp() > 0, "expected a positive timestamp, got " + frame.timestamp());
      }
    }
  }

  @PlaywrightTest
  void screencastStartShouldRecordVideoWithTheRequestedFps(Page page, Server server, @TempDir Path tmpDir) throws IOException {
    Path videoPath = tmpDir.resolve("video.webm");
    page.screencast().start(new Screencast.StartOptions().setPath(videoPath).setFps(60));
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("() => document.body.style.backgroundColor = 'red'");
    page.waitForTimeout(500);
    page.screencast().stop();
    assertTrue(Files.exists(videoPath), "video file should exist: " + videoPath);
    assertTrue(Files.size(videoPath) > 0);
  }

  @PlaywrightTest
  void screencastStartShouldThrowOnInvalidFps(Page page, @TempDir Path tmpDir) {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.screencast().start(
      new Screencast.StartOptions().setPath(tmpDir.resolve("video.webm")).setFps(-1)));
    assertTrue(e.getMessage().contains("\"fps\" must be a positive number, got -1"), e.getMessage());
  }

  @PlaywrightTest
  void screencastStartShouldThrowIfAlreadyStarted(Page page) {
    page.screencast().start(new Screencast.StartOptions().setOnFrame(data -> {}));
    PlaywrightException e = assertThrows(PlaywrightException.class,
      () -> page.screencast().start(new Screencast.StartOptions().setOnFrame(data -> {})));
    assertTrue(e.getMessage().contains("Screencast is already started"), e.getMessage());
    page.screencast().stop();
  }

  @PlaywrightTest
  void screencastStartShouldRecordVideoToPath(Page page, Server server, @TempDir Path tmpDir) throws IOException {
    Path videoPath = tmpDir.resolve("video.webm");
    page.screencast().start(new Screencast.StartOptions().setPath(videoPath));
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("() => document.body.style.backgroundColor = 'red'");
    page.waitForTimeout(500);
    page.screencast().stop();
    assertTrue(Files.exists(videoPath), "video file should exist: " + videoPath);
    assertTrue(Files.size(videoPath) > 0);
  }

  @PlaywrightTest
  void screencastStartReturnsDisposable(Page page) throws Exception {
    AutoCloseable disposable = page.screencast().start(new Screencast.StartOptions().setOnFrame(data -> {}));
    disposable.close();
    // After dispose, starting again should succeed.
    page.screencast().start(new Screencast.StartOptions().setOnFrame(data -> {}));
    page.screencast().stop();
  }

  @PlaywrightTest
  void screencastShowOverlay(Page page, Server server) throws Exception {
    page.navigate(server.EMPTY_PAGE);
    AutoCloseable disposable = page.screencast().showOverlay("<div>Hello Overlay</div>");
    assertNotNull(disposable);
    disposable.close();
  }

  @PlaywrightTest
  void screencastShowChapter(Page page, Server server) {
    page.navigate(server.EMPTY_PAGE);
    page.screencast().showChapter("Chapter Title");
    page.screencast().showChapter("With Description",
      new Screencast.ShowChapterOptions().setDescription("Some details").setDuration(100));
  }

  @PlaywrightTest
  void screencastHideShowOverlays(Page page, Server server) {
    page.navigate(server.EMPTY_PAGE);
    page.screencast().showOverlay("<div>visible</div>");
    page.screencast().hideOverlays();
    page.screencast().showOverlays();
  }

  @PlaywrightTest
  void screencastShowAndHideActions(Page page, Server server) throws Exception {
    page.navigate(server.EMPTY_PAGE);
    AutoCloseable disposable = page.screencast().showActions();
    assertNotNull(disposable);
    disposable.close();
    page.screencast().hideActions();
  }

  @PlaywrightTest
  void screencastShowActionsShouldAcceptEveryPosition(Page page, Server server) throws Exception {
    page.navigate(server.EMPTY_PAGE);
    for (AnnotatePosition position : AnnotatePosition.values()) {
      AutoCloseable disposable = page.screencast().showActions(
        new Screencast.ShowActionsOptions().setPosition(position));
      assertNotNull(disposable);
      disposable.close();
    }
  }

  @PlaywrightTest
  void screencastShowActionsShouldAcceptEveryCursor(Page page, Server server) throws Exception {
    page.navigate(server.EMPTY_PAGE);
    for (ScreencastCursor cursor : ScreencastCursor.values()) {
      AutoCloseable disposable = page.screencast().showActions(
        new Screencast.ShowActionsOptions().setCursor(cursor));
      assertNotNull(disposable);
      disposable.close();
    }
  }

  @PlaywrightTest
  void screencastShowActionsShouldRenderStyle(Page page, Server server) throws IOException {
    page.navigate(server.PREFIX + "/input/button.html");
    List<ScreencastFrame> frames = new ArrayList<>();
    page.screencast().start(new Screencast.StartOptions().setOnFrame(frames::add));
    // The decorations are only shown while the action is in progress, so look for them in the screencast frames.
    page.screencast().showActions(new Screencast.ShowActionsOptions().setDuration(1000).setStyle(new ScreencastActionStyle()
      .setPoint("width: 100px; height: 100px; background: rgb(255, 0, 0); animation: none")));
    page.click("button");
    page.screencast().stop();
    int maxRedPixels = 0;
    for (ScreencastFrame frame : frames) {
      BufferedImage image = ImageIO.read(new ByteArrayInputStream(frame.data()));
      int redPixels = 0;
      for (int y = 0; y < image.getHeight(); y++) {
        for (int x = 0; x < image.getWidth(); x++) {
          int rgb = image.getRGB(x, y);
          if (((rgb >> 16) & 0xFF) > 200 && ((rgb >> 8) & 0xFF) < 60 && (rgb & 0xFF) < 60) {
            redPixels++;
          }
        }
      }
      maxRedPixels = Math.max(maxRedPixels, redPixels);
    }
    assertTrue(maxRedPixels > 1000, "expected the styled action point in one of " + frames.size() + " frames, max red pixels: " + maxRedPixels);
  }
}
