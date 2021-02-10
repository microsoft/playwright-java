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

import com.microsoft.playwright.options.RecordVideo;
import com.microsoft.playwright.options.Size;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestScreencast extends TestBase {
  @Test
  void shouldExposeVideoPath(@TempDir Path videosDir) {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .withRecordVideo(new RecordVideo(videosDir).withSize(new Size(320, 240)))
      .withViewport(320, 240));
    Page page = context.newPage();
    page.evaluate("() => document.body.style.backgroundColor = 'red'");
    Path path = page.video().path();
    assertTrue(path.startsWith(videosDir));
    context.close();
    assertTrue(Files.exists(path));
  };
}
