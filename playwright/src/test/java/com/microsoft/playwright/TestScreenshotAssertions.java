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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestScreenshotAssertions extends TestBase {
  private static final Path SNAPSHOT_ROOT = Paths.get("src/test/resources/__screenshots__/TestScreenshotAssertions");

  @BeforeEach
  @AfterEach
  void cleanupSnapshots() throws IOException {
    if (Files.exists(SNAPSHOT_ROOT)) {
      Files.walk(SNAPSHOT_ROOT)
        .sorted((a, b) -> b.compareTo(a))
        .forEach(p -> {
          try {
            Files.deleteIfExists(p);
          } catch (IOException e) {
            // ignore
          }
        });
    }
  }

  @Test
  void shouldGenerateAndMatchPageScreenshot() {
    page.setContent("<div style='width:100px;height:100px;background:red;'></div>");
    // First run: baseline does not exist yet, it should be created and the assertion should pass.
    assertThat(page).hasScreenshot("page-baseline.png");
    Path expected = SNAPSHOT_ROOT.resolve("page-baseline.png");
    assertTrue(Files.exists(expected), "Baseline screenshot should have been created at " + expected);

    // Second run: baseline exists and matches, assertion should pass without changes.
    assertThat(page).hasScreenshot("page-baseline.png");
  }

  @Test
  void shouldFailWhenScreenshotDiffers() throws IOException {
    Path expected = SNAPSHOT_ROOT.resolve("page-mismatch.png");
    page.setContent("<div style='width:100px;height:100px;background:red;'></div>");
    assertThat(page).hasScreenshot("page-mismatch.png");
    assertTrue(Files.exists(expected));

    page.setContent("<div style='width:100px;height:100px;background:blue;'></div>");
    AssertionFailedError e = assertThrows(AssertionFailedError.class, () ->
      assertThat(page).hasScreenshot("page-mismatch.png",
        new com.microsoft.playwright.assertions.PageAssertions.HasScreenshotOptions().setTimeout(2_000)));
    assertTrue(e.getMessage().contains("Screenshot comparison failed"), e.getMessage());
  }

  @Test
  void shouldSupportNotWhenBaselineMissing() {
    page.setContent("<div>Hello</div>");
    // No baseline exists - `.not()` should pass without writing a baseline.
    assertThat(page).not().hasScreenshot("page-not-missing.png");
    assertTrue(!Files.exists(SNAPSHOT_ROOT.resolve("page-not-missing.png")));
  }

  @Test
  void shouldSupportLocatorScreenshot() {
    page.setContent("<div id='box' style='width:50px;height:50px;background:green;'></div>");
    Locator locator = page.locator("#box");
    assertThat(locator).hasScreenshot("locator-baseline.png");
    assertTrue(Files.exists(SNAPSHOT_ROOT.resolve("locator-baseline.png")));
  }
}
