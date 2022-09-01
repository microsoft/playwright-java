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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
  void saveAsShouldThrowWhenNoVideoFrames(@TempDir Path videosDir) {
    try (BrowserContext context = browser.newContext(
      new Browser.NewContextOptions()
        .setRecordVideoDir(videosDir)
        .setRecordVideoSize(320, 240)
        .setViewportSize(320, 240))) {

      Page page = context.newPage();
      Page popup = context.waitForPage(() -> {
        page.evaluate("() => {\n" +
          "  const win = window.open('about:blank');\n" +
          "  win.close();\n" +
          "}");
      });
      page.close();

      Path saveAsPath = videosDir.resolve("my-video.webm");
      if (!popup.isClosed()) {
        popup.waitForClose(() -> {});
      }
      // WebKit pauses renderer before win.close() and actually writes something.
      if (isWebKit()) {
        popup.video().saveAs(saveAsPath);
        assertTrue(Files.exists(saveAsPath));
      } else {
        PlaywrightException e = assertThrows(PlaywrightException.class, () -> popup.video().saveAs(saveAsPath));
        assertTrue(e.getMessage().contains("Page did not produce any video frames"), e.getMessage());
      }
    }
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
  void shouldErrorIfPageNotClosedBeforeSaveAs(@TempDir Path tmpDir) {
    try (Page page = browser.newPage(new Browser.NewPageOptions().setRecordVideoDir(tmpDir))) {
      page.navigate(server.PREFIX + "/grid.html");
      Path outPath = tmpDir.resolve("some-video.webm");
      Video video = page.video();
      PlaywrightException exception = assertThrows(PlaywrightException.class, () -> video.saveAs(outPath));
      assertTrue(
        exception.getMessage().contains("Page is not yet closed. Close the page prior to calling saveAs"),
        exception.getMessage());
    }
  }
}
