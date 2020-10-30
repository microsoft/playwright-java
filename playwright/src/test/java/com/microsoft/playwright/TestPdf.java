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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class TestPdf extends TestBase {
  @Test
  void shouldBeAbleToSaveFile() throws IOException {
// TODO:   test.skip(headful || browserName !== "chromium", "Printing to pdf is currently only supported in headless chromium.");
    Path path = File.createTempFile("output", ".pdf").toPath();
    page.pdf(new Page.PdfOptions().withPath(path));
    long size = Files.size(path);
    assertTrue(size > 0);
  }

  @Test
  void shouldOnlyHavePdfInChromium() {
// TODO:   test.skip(browserName === "chromium");
    try {
      page.pdf();
      if (isChromium) {
        return;
      }
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertFalse(e.getMessage().contains("did not throw"));
    }
  }

}
