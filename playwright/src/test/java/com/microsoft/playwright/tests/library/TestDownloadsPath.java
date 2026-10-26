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
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.LaunchPersistent;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestInfo;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/library/downloads-path.spec.ts. */
public class TestDownloadsPath {
  @Nested
  class DownloadsPath {
    @BeforeEach
    void setUp(TestServer server) {
      server.setRoute("/download", (req, res) -> {
        res.setHeader("Content-Type", "application/octet-stream");
        res.setHeader("Content-Disposition", "attachment; filename=file.txt");
        res.end("Hello world");
      });
    }

    @PlaywrightTest
    @DisplayName("DO-039 should keep downloadsPath folder")
    void shouldKeepDownloadsPathFolder(BrowserType browserType, TestServer server, TestInfo testInfo) {
      Browser downloadsBrowser = browserType.launch(Env.launchOptions().setDownloadsPath(testInfo.outputPath("")));
      Page page = downloadsBrowser.newPage();
      page.setContent("<a href=\"" + server.PREFIX + "/download\">download</a>");
      Download download = page.waitForDownload(() -> page.click("a"));
      assertEquals(server.PREFIX + "/download", download.url());
      assertEquals("file.txt", download.suggestedFilename());
      try {
        download.path();
      } catch (PlaywrightException e) {
      }
      page.close();
      downloadsBrowser.close();
      assertTrue(Files.exists(testInfo.outputPath("")));
    }

    @PlaywrightTest
    @DisplayName("DO-040 should delete downloads when context closes")
    void shouldDeleteDownloadsWhenContextCloses(BrowserType browserType, TestServer server, TestInfo testInfo) {
      Browser downloadsBrowser = browserType.launch(Env.launchOptions().setDownloadsPath(testInfo.outputPath("")));
      Page page = downloadsBrowser.newPage();
      page.setContent("<a href=\"" + server.PREFIX + "/download\">download</a>");
      Download download = page.waitForDownload(() -> page.click("a"));
      Path path = download.path();
      assertTrue(Files.exists(path));
      page.close();
      assertFalse(Files.exists(path));
      downloadsBrowser.close();
    }

    @PlaywrightTest
    @DisplayName("DO-041 should report downloads in downloadsPath folder")
    void shouldReportDownloadsInDownloadsPathFolder(BrowserType browserType, TestServer server, TestInfo testInfo) {
      Browser downloadsBrowser = browserType.launch(Env.launchOptions().setDownloadsPath(testInfo.outputPath("")));
      Page page = downloadsBrowser.newPage();
      page.setContent("<a href=\"" + server.PREFIX + "/download\">download</a>");
      Download download = page.waitForDownload(() -> page.click("a"));
      Path path = download.path();
      assertTrue(path.startsWith(testInfo.outputPath("")));
      page.close();
      downloadsBrowser.close();
    }

    @PlaywrightTest
    @DisplayName("DO-042 should report downloads in downloadsPath folder with a relative path")
    void shouldReportDownloadsInDownloadsPathFolderWithARelativePath(BrowserType browserType, TestServer server, TestInfo testInfo) {
      Browser downloadsBrowser = browserType.launch(Env.launchOptions().setDownloadsPath(
        Paths.get("").toAbsolutePath().relativize(testInfo.outputPath(""))));
      Page page = downloadsBrowser.newPage();
      page.setContent("<a href=\"" + server.PREFIX + "/download\">download</a>");
      Download download = page.waitForDownload(() -> page.click("a"));
      Path downloadPath = download.path();
      assertTrue(downloadPath.startsWith(testInfo.outputPath("")));
      page.close();
      downloadsBrowser.close();
    }

    @PlaywrightTest
    @DisplayName("DO-043 should accept downloads in persistent context")
    void shouldAcceptDownloadsInPersistentContext(LaunchPersistent launchPersistent, TestServer server, TestInfo testInfo) {
      LaunchPersistent.Result result = launchPersistent.launch(new BrowserType.LaunchPersistentContextOptions().setDownloadsPath(testInfo.outputPath("")));
      BrowserContext context = result.context;
      Page page = result.page;
      page.setContent("<a href=\"" + server.PREFIX + "/download\">download</a>");
      Download download = page.waitForDownload(() -> page.click("a"));
      assertEquals(server.PREFIX + "/download", download.url());
      assertEquals("file.txt", download.suggestedFilename());
      Path path = download.path();
      assertTrue(path.startsWith(testInfo.outputPath("")));
      context.close();
    }

    @PlaywrightTest
    @DisplayName("DO-044 should delete downloads when persistent context closes")
    void shouldDeleteDownloadsWhenPersistentContextCloses(LaunchPersistent launchPersistent, TestServer server, TestInfo testInfo) {
      LaunchPersistent.Result result = launchPersistent.launch(new BrowserType.LaunchPersistentContextOptions().setDownloadsPath(testInfo.outputPath("")));
      BrowserContext context = result.context;
      Page page = result.page;
      page.setContent("<a href=\"" + server.PREFIX + "/download\">download</a>");
      Download download = page.waitForDownload(() -> page.click("a"));
      Path path = download.path();
      assertTrue(Files.exists(path));
      context.close();
      assertFalse(Files.exists(path));
    }
  }
}
