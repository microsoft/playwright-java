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
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.ContextFactory;
import com.microsoft.playwright.fixtures.EnabledOnBrowser;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestInfo;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/library/pdf.spec.ts. */
public class TestPdf {
  @PlaywrightTest
  @DisplayName("PD-001 should be able to save file")
  @EnabledOnBrowser(value = BrowserName.CHROMIUM, reason = "Printing to pdf is currently only supported in chromium.")
  void shouldBeAbleToSaveFile(ContextFactory contextFactory, BrowserName browserName, TestInfo testInfo) throws IOException {
    BrowserContext context = contextFactory.newContext();
    Page page = context.newPage();
    Path outputFile = testInfo.outputPath("output.pdf");
    page.pdf(new Page.PdfOptions().setPath(outputFile));
    assertTrue(Files.readAllBytes(outputFile).length > 0);
  }

  @PlaywrightTest
  @DisplayName("PD-002 should be able to generate outline")
  @EnabledOnBrowser(value = BrowserName.CHROMIUM, reason = "Printing to pdf is currently only supported in chromium.")
  void shouldBeAbleToGenerateOutline(ContextFactory contextFactory, TestServer server, BrowserName browserName, TestInfo testInfo) throws IOException {
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions()
      .setBaseURL(server.PREFIX));
    Page page = context.newPage();
    page.navigate("/headings.html");
    Path outputFileNoOutline = testInfo.outputPath("outputNoOutline.pdf");
    Path outputFileOutline = testInfo.outputPath("outputOutline.pdf");
    page.pdf(new Page.PdfOptions().setPath(outputFileNoOutline));
    page.pdf(new Page.PdfOptions().setPath(outputFileOutline).setTagged(true).setOutline(true));
    assertTrue(Files.readAllBytes(outputFileOutline).length > Files.readAllBytes(outputFileNoOutline).length);
  }
}
