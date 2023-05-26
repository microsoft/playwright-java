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
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class TestPdf extends TestBase {
  @Test
  @EnabledIf(value="com.microsoft.playwright.TestBase#isChromium", disabledReason="skip")
  @DisabledIf(value="com.microsoft.playwright.TestBase#isHeadful", disabledReason="skip")
  void shouldBeAbleToSaveFile(@TempDir Path tempDir) throws IOException {
    Path path = tempDir.resolve("output.pdf");
    page.pdf(new Page.PdfOptions().setPath(path));
    long size = Files.size(path);
    assertTrue(size > 0);
  }

  @Test
  @EnabledIf(value="com.microsoft.playwright.TestBase#isChromium", disabledReason="skip")
  @DisabledIf(value="com.microsoft.playwright.TestBase#isHeadful", disabledReason="skip")
  void shouldSupportFractionalScaleValue(@TempDir Path tempDir) throws IOException {
    Path path = tempDir.resolve("output.pdf");
    page.pdf(new Page.PdfOptions().setPath(path).setScale(0.5));
    long size = Files.size(path);
    assertTrue(size > 0);
  }

  @Test
  @DisabledIf(value="com.microsoft.playwright.TestBase#isChromium", disabledReason="skip")
  void shouldThrowInNonChromium() {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.pdf());
    assertTrue(e.getMessage().contains("PDF generation is only supported for Headless Chromium"), e.getMessage());
  }


  @Test
  @DisabledIf(value="com.microsoft.playwright.TestBase#isChromium", disabledReason="skip")
  void correctExceptionWithPersistentContext(@TempDir Path tempDir) {
    Path profile = tempDir.resolve("profile");
    try (BrowserContext context = browserType.launchPersistentContext(profile)) {
      Page page = context.newPage();
      PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.pdf());
      assertTrue(e.getMessage().contains("PDF generation is only supported for Headless Chromium"), e.getMessage());
    }
  }
}
