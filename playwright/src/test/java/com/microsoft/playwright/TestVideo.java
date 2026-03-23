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

import com.microsoft.playwright.options.Size;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static com.microsoft.playwright.Utils.relativePathOrSkipTest;
import static org.junit.jupiter.api.Assertions.*;

public class TestVideo extends TestBase {
  @Test
  void shouldWorkWithRelativePathForRecordVideoDir(@TempDir Path tmpDir) {
    Path relativeDir = relativePathOrSkipTest(tmpDir);
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setRecordVideoSize(320, 240).setRecordVideoDir(relativeDir));
    Page page = context.newPage();
    Path videoPath = page.video().path();
    context.close();
    assertTrue(videoPath.isAbsolute(), "videosPath = " + videoPath);
    assertTrue(Files.exists(videoPath), "videosPath = " + videoPath);
  }

  @Test
  void videoStartShouldFailWhenRecordVideoIsSet(@TempDir Path tmpDir) {
    BrowserContext ctx = browser.newContext(new Browser.NewContextOptions()
      .setRecordVideoSize(320, 240).setRecordVideoDir(tmpDir));
    Page pg = ctx.newPage();
    try {
      PlaywrightException e = assertThrows(PlaywrightException.class,
        () -> pg.video().start());
      assertTrue(e.getMessage().contains("Video is already being recorded"), e.getMessage());
      // stop should still work
      pg.video().stop();
    } finally {
      ctx.close();
    }
  }

  @Test
  void videoStopShouldFailWhenNoRecordingIsInProgress() {
    BrowserContext ctx = browser.newContext();
    Page pg = ctx.newPage();
    try {
      PlaywrightException e = assertThrows(PlaywrightException.class,
        () -> pg.video().stop());
      assertTrue(e.getMessage().contains("Video is not being recorded"), e.getMessage());
    } finally {
      ctx.close();
    }
  }

  @Test
  void videoStartAndStopShouldProduceVideoFile(@TempDir Path tmpDir) throws Exception {
    BrowserContext ctx = browser.newContext(new Browser.NewContextOptions()
      .setViewportSize(800, 800));
    Page pg = ctx.newPage();
    try {
      Size size = new Size(800, 800);
      pg.video().start(new Video.StartOptions().setSize(size));
      pg.video().stop();
      Path videoPath = pg.video().path();
      assertNotNull(videoPath);
      assertTrue(Files.exists(videoPath), "video file should exist: " + videoPath);
    } finally {
      ctx.close();
    }
  }
}
