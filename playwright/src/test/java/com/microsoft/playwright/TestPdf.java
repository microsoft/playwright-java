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

import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.EnabledOnBrowser;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.microsoft.playwright.fixtures.BrowserName.CHROMIUM;
import static org.junit.jupiter.api.Assertions.*;

public class TestPdf {
  @PlaywrightTest
  @EnabledOnBrowser(value = CHROMIUM, reason = "skip")
  void shouldBeAbleToSaveFile(Page page, @TempDir Path tempDir) throws IOException {
    Path path = tempDir.resolve("output.pdf");
    page.pdf(new Page.PdfOptions().setPath(path));
    long size = Files.size(path);
    assertTrue(size > 0);
  }

  @PlaywrightTest
  @EnabledOnBrowser(value = CHROMIUM, reason = "skip")
  void shouldSupportFractionalScaleValue(Page page, @TempDir Path tempDir) throws IOException {
    Path path = tempDir.resolve("output.pdf");
    page.pdf(new Page.PdfOptions().setPath(path).setScale(0.5));
    long size = Files.size(path);
    assertTrue(size > 0);
  }


  @PlaywrightTest
  @EnabledOnBrowser(value = CHROMIUM, reason = "Printing to pdf is currently only supported in headless chromium.")
  void shouldBeAbleToGenerateOutline(Page page, Server server, @TempDir Path tempDir) throws IOException {
    page.navigate(server.PREFIX + "/headings.html");
    Path outputFileNoOutline = tempDir.resolve("outputNoOutline.pdf");
    Path outputFileOutline = tempDir.resolve("outputOutline.pdf");
    page.pdf(new Page.PdfOptions().setPath(outputFileNoOutline));
    page.pdf(new Page.PdfOptions().setPath(outputFileOutline).setTagged(true).setOutline(true));
    long noOutlineSize = Files.size(outputFileNoOutline);
    long outlineSize = Files.size(outputFileOutline);
    assertTrue(outlineSize > noOutlineSize, "Unexpected sizes: " + outlineSize + " noOutline: " + noOutlineSize);
  }


  @PlaywrightTest
  @DisabledOnBrowser(value = CHROMIUM, reason = "skip")
  void correctExceptionWithPersistentContext(BrowserType browserType, @TempDir Path tempDir) {
    Path profile = tempDir.resolve("profile");
    try (BrowserContext context = browserType.launchPersistentContext(profile)) {
      Page page = context.newPage();
      PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.pdf());
      assertTrue(e.getMessage().contains("PDF generation is only supported for Headless Chromium"), e.getMessage());
    }
  }
}
