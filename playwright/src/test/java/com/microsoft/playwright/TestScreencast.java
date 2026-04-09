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

import com.microsoft.playwright.options.ScreencastFrame;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class TestScreencast extends TestBase {
  @Test
  void shouldExposeVideoPath(@TempDir Path videosDir) {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setRecordVideoDir(videosDir).setRecordVideoSize(320, 240)
      .setViewportSize(320, 240));
    Page page = context.newPage();
    page.evaluate("() => document.body.style.backgroundColor = 'red'");
    Path path = page.video().path();
    assertTrue(path.startsWith(videosDir));
    context.close();
    assertTrue(Files.exists(path));
  }

  @Test
  void shouldSaveAsVideo(@TempDir Path videosDir) {
    BrowserContext context = browser.newContext(
      new Browser.NewContextOptions()
        .setRecordVideoDir(videosDir)
        .setRecordVideoSize(320, 240)
        .setViewportSize(320, 240));
    Page page = context.newPage();
    page.evaluate("() => document.body.style.backgroundColor = 'red'");
    page.waitForTimeout(1000);
    context.close();

    Path saveAsPath = videosDir.resolve("my-video.webm");
    page.video().saveAs(saveAsPath);
    assertTrue(Files.exists(saveAsPath));
  }

  @Test
  void shouldDeleteVideo(@TempDir Path videosDir) {
    try (BrowserContext context = browser.newContext(
      new Browser.NewContextOptions()
        .setRecordVideoDir(videosDir)
        .setRecordVideoSize(320, 240)
        .setViewportSize(320, 240))) {
      Page page = context.newPage();
      page.evaluate("() => document.body.style.backgroundColor = 'red'");
      page.waitForTimeout(1000);
      context.close();

      page.video().delete();
      Path videoPath = page.video().path();
      assertFalse(Files.exists(videoPath));
    }
  }

  @Test
  void shouldWaitForVideoFinishWhenPageIsClosed(@TempDir Path videosDir) throws IOException {
    try (Browser browser = browserType.launch(createLaunchOptions())) {
      BrowserContext context = browser.newContext(
        new Browser.NewContextOptions()
          .setRecordVideoDir(videosDir)
          .setRecordVideoSize(320, 240)
          .setViewportSize(320, 240));
      Page page = context.newPage();
      page.evaluate("() => document.body.style.backgroundColor = 'red'");
      page.waitForTimeout(500);
      // First close page manually.
      page.close();
      context.close();
    }
    List<Path> files = Files.list(videosDir).collect(Collectors.toList());
    assertEquals(1, files.size());
    assertTrue(Files.exists(files.get(0)));
    assertTrue(Files.size(files.get(0)) > 0);
  }

  @Test
  void screencastStartShouldDeliverFramesViaOnFrame() throws Exception {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(500, 400));
    Page page = context.newPage();
    try {
      List<ScreencastFrame> frames = new ArrayList<>();
      page.screencast().start(new Screencast.StartOptions().setOnFrame(frames::add));
      page.navigate(server.EMPTY_PAGE);
      page.evaluate("() => document.body.style.backgroundColor = 'red'");
      page.waitForTimeout(500);
      page.screencast().stop();
      assertFalse(frames.isEmpty(), "expected at least one frame");
      // JPEG-encoded frames start with FF D8.
      for (ScreencastFrame frame : frames) {
        assertNotNull(frame.data);
        assertEquals((byte) 0xFF, frame.data[0]);
        assertEquals((byte) 0xD8, frame.data[1]);
      }
    } finally {
      context.close();
    }
  }

  @Test
  void screencastStartShouldThrowIfAlreadyStarted() {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    try {
      page.screencast().start(new Screencast.StartOptions().setOnFrame(data -> {}));
      PlaywrightException e = assertThrows(PlaywrightException.class,
        () -> page.screencast().start(new Screencast.StartOptions().setOnFrame(data -> {})));
      assertTrue(e.getMessage().contains("Screencast is already started"), e.getMessage());
      page.screencast().stop();
    } finally {
      context.close();
    }
  }

  @Test
  void screencastStartShouldRecordVideoToPath(@TempDir Path tmpDir) throws Exception {
    Path videoPath = tmpDir.resolve("video.webm");
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(800, 600));
    Page page = context.newPage();
    try {
      page.screencast().start(new Screencast.StartOptions().setPath(videoPath));
      page.navigate(server.EMPTY_PAGE);
      page.evaluate("() => document.body.style.backgroundColor = 'red'");
      page.waitForTimeout(500);
      page.screencast().stop();
      assertTrue(Files.exists(videoPath), "video file should exist: " + videoPath);
      assertTrue(Files.size(videoPath) > 0);
    } finally {
      context.close();
    }
  }

  @Test
  void screencastStartReturnsDisposable() throws Exception {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    try {
      AutoCloseable disposable = page.screencast().start(new Screencast.StartOptions().setOnFrame(data -> {}));
      disposable.close();
      // After dispose, starting again should succeed.
      page.screencast().start(new Screencast.StartOptions().setOnFrame(data -> {}));
      page.screencast().stop();
    } finally {
      context.close();
    }
  }

  @Test
  void screencastShowOverlay() throws Exception {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    try {
      page.navigate(server.EMPTY_PAGE);
      AutoCloseable disposable = page.screencast().showOverlay("<div>Hello Overlay</div>");
      assertNotNull(disposable);
      disposable.close();
    } finally {
      context.close();
    }
  }

  @Test
  void screencastShowChapter() {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    try {
      page.navigate(server.EMPTY_PAGE);
      page.screencast().showChapter("Chapter Title");
      page.screencast().showChapter("With Description",
        new Screencast.ShowChapterOptions().setDescription("Some details").setDuration(100));
    } finally {
      context.close();
    }
  }

  @Test
  void screencastHideShowOverlays() {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    try {
      page.navigate(server.EMPTY_PAGE);
      page.screencast().showOverlay("<div>visible</div>");
      page.screencast().hideOverlays();
      page.screencast().showOverlays();
    } finally {
      context.close();
    }
  }

  @Test
  void screencastShowAndHideActions() throws Exception {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    try {
      page.navigate(server.EMPTY_PAGE);
      AutoCloseable disposable = page.screencast().showActions();
      assertNotNull(disposable);
      disposable.close();
      page.screencast().hideActions();
    } finally {
      context.close();
    }
  }
}
