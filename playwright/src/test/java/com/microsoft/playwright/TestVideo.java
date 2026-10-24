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

import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static com.microsoft.playwright.Utils.relativePathOrSkipTest;
import static org.junit.jupiter.api.Assertions.*;

public class TestVideo {
  @PlaywrightTest
  void shouldWorkWithRelativePathForRecordVideoDir(Browser browser, @TempDir Path tmpDir) {
    Path relativeDir = relativePathOrSkipTest(tmpDir);
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setRecordVideoSize(320, 240).setRecordVideoDir(relativeDir));
    Page page = context.newPage();
    Path videoPath = page.video().path();
    context.close();
    assertTrue(videoPath.isAbsolute(), "videosPath = " + videoPath);
    assertTrue(Files.exists(videoPath), "videosPath = " + videoPath);
  }

  @PlaywrightTest
  void shouldRecordVideoWithTheRequestedFps(Browser browser, @TempDir Path tmpDir) {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setRecordVideoDir(tmpDir).setRecordVideoFps(60));
    Page page = context.newPage();
    page.evaluate("() => document.body.style.backgroundColor = 'red'");
    page.waitForTimeout(500);
    Path videoPath = page.video().path();
    context.close();
    assertTrue(Files.exists(videoPath), "videosPath = " + videoPath);
  }

  @PlaywrightTest
  void shouldThrowOnInvalidFps(Browser browser, @TempDir Path tmpDir) {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> browser.newContext(new Browser.NewContextOptions()
      .setRecordVideoDir(tmpDir).setRecordVideoFps(0)));
    assertTrue(e.getMessage().contains("\"recordVideo.fps\" must be a positive number, got 0"), e.getMessage());
  }

  @PlaywrightTest
  void shouldThrowWhenFpsIsSetWithoutDir(Browser browser) {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> browser.newContext(new Browser.NewContextOptions()
      .setRecordVideoFps(60)));
    assertTrue(e.getMessage().contains("recordVideoFps is set but recordVideoDir is null"), e.getMessage());
  }
}
