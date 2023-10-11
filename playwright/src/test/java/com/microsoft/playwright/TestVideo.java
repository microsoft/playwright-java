package com.microsoft.playwright;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static com.microsoft.playwright.Utils.relativePathOrSkipTest;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
}
