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

import com.microsoft.playwright.impl.PlaywrightImpl;
import com.microsoft.playwright.impl.driver.Driver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static com.microsoft.playwright.Utils.getBrowserTypeFromEnv;
import static com.microsoft.playwright.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.*;

public class TestPlaywrightCreate {
  @Test
  void shouldSupportEnvSkipBrowserDownload(@TempDir Path browsersDir) throws IOException, NoSuchFieldException, IllegalAccessException {
    System.err.println("shouldSupportEnvSkipBrowserDownload PLAYWRIGHT_BROWSERS_PATH = " + browsersDir);
    Map<String, String> env = mapOf("PLAYWRIGHT_BROWSERS_PATH", browsersDir.toString(),
      "PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", "1");
    Playwright.CreateOptions options = new Playwright.CreateOptions().setEnv(env);

    try (Playwright playwright = PlaywrightImpl.createImpl(options, true)) {
      PlaywrightException e = assertThrows(PlaywrightException.class, () -> getBrowserTypeFromEnv(playwright).launch());
      assertTrue(e.getMessage().contains("Looks like Playwright Test or Playwright was just installed or updated") ||
        e.getMessage().contains("Looks like Playwright was just installed or updated."), e.getMessage());

      try (DirectoryStream<Path> ds = Files.newDirectoryStream(browsersDir)) {
        for (Path child : ds) {
          fail("Unexpected file: " + child.toString());
        }
      }
    }
  }

  // This test is too slow, so we don't run it.
  void shouldSupportEnvBrowsersPath(@TempDir Path browsersDir) throws IOException {
    Map<String, String> env = mapOf("PLAYWRIGHT_BROWSERS_PATH", browsersDir.toString());
    Playwright.CreateOptions options = new Playwright.CreateOptions().setEnv(env);

    try (Playwright playwright = Playwright.create(options)) {
      try (Browser browser = playwright.chromium().launch()) {
        assertNotNull(browser);
      }

      try (DirectoryStream<Path> ds = Files.newDirectoryStream(browsersDir)) {
        for (Path child : ds) {
          assertTrue(Files.isDirectory(child));
        }
      }
    }
  }
}
