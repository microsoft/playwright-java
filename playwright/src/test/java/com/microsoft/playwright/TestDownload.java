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

import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.EnabledOnBrowser;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.BeforeEach;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;

import static com.microsoft.playwright.Utils.chromiumVersionLessThan;
import static com.microsoft.playwright.Utils.copy;
import static com.microsoft.playwright.fixtures.BrowserName.CHROMIUM;
import static com.microsoft.playwright.fixtures.BrowserName.FIREFOX;
import static com.microsoft.playwright.options.KeyboardModifier.ALT;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class TestDownload {

  @BeforeEach
  void addRoutes(Server server) {
    server.setRoute("/download", exchange -> {
      exchange.getResponseHeaders().add("Content-Type", "application/octet-stream");
      exchange.getResponseHeaders().add("Content-Disposition", "attachment");
      exchange.sendResponseHeaders(200, 0);
      try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("Hello world");
      }
    });
    server.setRoute("/downloadWithFilename", exchange -> {
      exchange.getResponseHeaders().add("Content-Type", "application/octet-stream");
      exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename=file.txt");
      exchange.sendResponseHeaders(200, 0);
      try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("Hello world");
      }
    });

    server.setRoute("/downloadWithDelay", exchange -> {
      exchange.getResponseHeaders().add("Content-Type", "application/octet-stream");
      exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename=file.txt");
      exchange.sendResponseHeaders(200, 0);
      // Chromium requires a large enough payload to trigger the download event soon enough
      OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody());
      writer.write(String.join("", Collections.nCopies(4096, "a")));
      writer.write("foo");
      writer.flush();
    });

  }

  @PlaywrightTest
  void shouldReportDownloadWhenNavigationTurnsIntoDownload(Browser browser, Server server, BrowserName browserName) throws IOException {
    Page page = browser.newPage(new Browser.NewPageOptions().setAcceptDownloads(true));

    Response[] response = new Response[]{null};
    PlaywrightException[] error = new PlaywrightException[]{null};
    Download download  = page.waitForDownload(() -> {
      try {
        response[0] = page.navigate(server.PREFIX + "/download");
      } catch (PlaywrightException e) {
        error[0] = e;
      }
    });

    assertEquals(page, download.page());
    assertEquals(server.PREFIX + "/download", download.url());
    Path path = download.path();
    assertTrue(Files.exists(path));
    byte[] bytes = readAllBytes(path);
    assertEquals("Hello world", new String(bytes, UTF_8));
    assertNotNull(error[0]);
    if (!chromiumVersionLessThan(browser.version(), "140.0.0.0")) {
      assertTrue(error[0].getMessage().contains("Download is starting"));
    }
    if (browserName != FIREFOX)
      assertEquals("about:blank", page.url());
    page.close();
  }

  @PlaywrightTest
  void shouldReportDownloadsWithAcceptDownloadsFalse(Browser browser, Server server) {
    try (Page page = browser.newPage(new Browser.NewPageOptions().setAcceptDownloads(false))) {
      page.setContent("<a href='" + server.PREFIX + "/downloadWithFilename'>download</a>");
      Download download = page.waitForDownload(() -> page.click("a"));
      assertEquals(server.PREFIX + "/downloadWithFilename", download.url());
      assertEquals("file.txt", download.suggestedFilename());
      PlaywrightException e = assertThrows(PlaywrightException.class, () -> download.path());
      assertTrue(download.failure().contains("acceptDownloads"));
      assertTrue(e.getMessage().contains("acceptDownloads: true"));
    }
  }
  @PlaywrightTest
  void shouldReportDownloadsWithAcceptDownloadsTrue(Browser browser, Server server) throws IOException {
    Page page = browser.newPage(new Browser.NewPageOptions().setAcceptDownloads(true));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Download download = page.waitForDownload(() -> page.click("a"));
    Path path = download.path();
    assertTrue(Files.exists(path));
    byte[] bytes = readAllBytes(path);
    assertEquals("Hello world", new String(bytes, UTF_8));
    page.close();
  }

  @PlaywrightTest
  void shouldSaveToUserSpecifiedPath(Browser browser, Server server) throws IOException {
    Page page = browser.newPage(new Browser.NewPageOptions().setAcceptDownloads(true));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Download download = page.waitForDownload(() -> page.click("a"));

    Path userFile = Files.createTempFile("download-", ".txt");
    download.saveAs(userFile);
    assertTrue(Files.exists(userFile));
    byte[] bytes = readAllBytes(userFile);
    assertEquals("Hello world", new String(bytes, UTF_8));
    page.close();
  }

  @PlaywrightTest
  void shouldSaveToUserSpecifiedPathWithoutUpdatingOriginalPath(Browser browser, Server server) throws IOException {
    Page page = browser.newPage(new Browser.NewPageOptions().setAcceptDownloads(true));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Download download = page.waitForDownload(() -> page.click("a"));

    Path userFile = Files.createTempFile("download-", ".txt");
    download.saveAs(userFile);
    {
      assertTrue(Files.exists(userFile));
      byte[] bytes = readAllBytes(userFile);
      assertEquals("Hello world", new String(bytes, UTF_8));
    }
    Path originalPath = download.path();
    {
      assertTrue(Files.exists(originalPath));
      byte[] bytes = readAllBytes(originalPath);
      assertEquals("Hello world", new String(bytes, UTF_8));
    }
    page.close();
  }


  @PlaywrightTest
  void shouldSaveToTwoDifferentPathsWithMultipleSaveAsCalls(Browser browser, Server server) throws IOException {
    Page page = browser.newPage(new Browser.NewPageOptions().setAcceptDownloads(true));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Download download = page.waitForDownload(() -> page.click("a"));
    {
      Path userFile = Files.createTempFile("download-", ".txt");
      download.saveAs(userFile);
      assertTrue(Files.exists(userFile));
      byte[] bytes = readAllBytes(userFile);
      assertEquals("Hello world", new String(bytes, UTF_8));
    }
    {
      Path anotherUserPath = Files.createTempFile("download-2-", ".txt");
      download.saveAs(anotherUserPath);
      assertTrue(Files.exists(anotherUserPath));
      byte[] bytes = readAllBytes(anotherUserPath);
      assertEquals("Hello world", new String(bytes, UTF_8));
    }
    page.close();
  }

  @PlaywrightTest
  void shouldSaveToOverwrittenFilepath(Browser browser, Server server) throws IOException {
    Page page = browser.newPage(new Browser.NewPageOptions().setAcceptDownloads(true));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Download download = page.waitForDownload(() -> page.click("a"));
    Path userFile = Files.createTempFile("download-", ".txt");
    {
      download.saveAs(userFile);
      assertTrue(Files.exists(userFile));
      byte[] bytes = readAllBytes(userFile);
      assertEquals("Hello world", new String(bytes, UTF_8));
    }
    {
      download.saveAs(userFile);
      assertTrue(Files.exists(userFile));
      byte[] bytes = readAllBytes(userFile);
      assertEquals("Hello world", new String(bytes, UTF_8));
    }
    page.close();
  }

  @PlaywrightTest
  void shouldCreateSubdirectoriesWhenSavingToNonExistentUserSpecifiedPath(Browser browser, Server server) throws IOException {
    Page page = browser.newPage(new Browser.NewPageOptions().setAcceptDownloads(true));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Download download = page.waitForDownload(() -> page.click("a"));

    Path downloads = Files.createTempDirectory("downloads");
    Path nestedPath = downloads.resolve(Paths.get("these", "are", "directories", "download.txt"));
    download.saveAs(nestedPath);
    assertTrue(Files.exists(nestedPath));
    byte[] bytes = readAllBytes(nestedPath);
    assertEquals("Hello world", new String(bytes, UTF_8));
    page.close();
  }

  void shouldSaveWhenConnectedRemotely() {
    // TODO: Support connect
  }

  @PlaywrightTest
  void shouldErrorWhenSavingWithDownloadsDisabled(Browser browser, Server server) throws IOException {
    Page page = browser.newPage(new Browser.NewPageOptions().setAcceptDownloads(false));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Download download = page.waitForDownload(() -> page.click("a"));

    Path userPath = Files.createTempFile("download-", ".txt");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> download.saveAs(userPath));
    assertTrue(e.getMessage().contains("Pass { acceptDownloads: true } when you are creating your browser context"));
    page.close();
  }

  @PlaywrightTest
  void shouldErrorWhenSavingAfterDeletion(Browser browser, Server server) throws IOException {
    Page page = browser.newPage(new Browser.NewPageOptions().setAcceptDownloads(true));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Download download = page.waitForDownload(() -> page.click("a"));

    Path userPath = Files.createTempFile("download-", ".txt");
    download.delete();
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> download.saveAs(userPath));
    assertTrue(e.getMessage().contains("Target page, context or browser has been closed"), e.getMessage());
    page.close();
  }

  void shouldErrorWhenSavingAfterDeletionWhenConnectedRemotely() {
  // TODO: Support connect
  }

  @PlaywrightTest
  void shouldReportNonNavigationDownloads(Browser browser, Server server) throws IOException {
    // Mac WebKit embedder does not download in this case, although Safari does.
    server.setRoute("/download", exchange -> {
      exchange.getResponseHeaders().add("Content-Type", "application/octet-stream");
      exchange.sendResponseHeaders(200, 0);
      try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("Hello world");
      }
    });

    Page page = browser.newPage(new Browser.NewPageOptions().setAcceptDownloads(true));
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a download='file.txt' href='" + server.PREFIX + "/download'>download</a>");
    Download download = page.waitForDownload(() -> page.click("a"));

    assertEquals("file.txt", download.suggestedFilename());
    Path path = download.path();
    assertTrue(Files.exists(path));
    byte[] bytes = readAllBytes(path);
    assertEquals("Hello world", new String(bytes, UTF_8));
    page.close();
  }

  @PlaywrightTest
  void shouldReportDownloadPathWithinPageOnDownloadHandlerForFiles(Browser browser, Server server) throws IOException {
    Page page = browser.newPage(new Browser.NewPageOptions().setAcceptDownloads(true));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    @SuppressWarnings("unchecked")
    Download[] download = {null};
    page.onDownload(d -> download[0] = d);
    page.click("a");
    Instant start = Instant.now();
    while (download[0] == null) {
      page.waitForTimeout(100);
      assertTrue(Duration.between(start, Instant.now()).getSeconds() < 30, "Timed out");
    }
    Path path = download[0].path();
    assertTrue(Files.exists(path));
    byte[] bytes = readAllBytes(path);
    assertEquals("Hello world", new String(bytes, UTF_8));
    page.close();
  }

  @PlaywrightTest
  void shouldReportDownloadPathWithinPageOnDownloadHandlerForBlobs(Browser browser, Server server) throws IOException {
    Page page = browser.newPage(new Browser.NewPageOptions().setAcceptDownloads(true));
    @SuppressWarnings("unchecked")
    Download[] download = {null};
    page.onDownload(d -> download[0] = d);
    page.navigate(server.PREFIX + "/download-blob.html");
    page.click("a");
    Instant start = Instant.now();
    while (download[0] == null) {
      page.waitForTimeout(100);
      assertTrue(Duration.between(start, Instant.now()).getSeconds() < 1, "Timed out");
    }
    Path path = download[0].path();
    assertTrue(Files.exists(path));
    byte[] bytes = readAllBytes(path);
    assertEquals("Hello world", new String(bytes, UTF_8));
    page.close();
  }

  @PlaywrightTest
  @EnabledOnBrowser(value = CHROMIUM, reason = "fixme")
  void shouldReportAltClickDownloads(Browser browser, Server server) throws IOException {
    // Firefox does not download on alt-click by default.
    // Our WebKit embedder does not download on alt-click, although Safari does.
    server.setRoute("/download", exchange -> {
      exchange.getResponseHeaders().add("Content-Type", "application/octet-stream");
      exchange.sendResponseHeaders(200, 0);
      try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("Hello world");
      }
    });
    Page page = browser.newPage(new Browser.NewPageOptions().setAcceptDownloads(true));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Download download = page.waitForDownload(() -> page.click("a", new Page.ClickOptions().setModifiers(asList(ALT))));
    Path path = download.path();
    assertTrue(Files.exists(path));
    byte[] bytes = readAllBytes(path);
    assertEquals("Hello world", new String(bytes, UTF_8));
    page.close();
  }


  @PlaywrightTest
  @DisabledOnBrowser(value = CHROMIUM, headed = true, reason = "fixme")
  void shouldReportNewWindowDownloads(Browser browser, Server server) throws IOException {
    // TODO: - the test fails in headed Chromium as the popup page gets closed along
    // with the session before download completed event arrives.
    // - WebKit doesn't close the popup page
    Page page = browser.newPage(new Browser.NewPageOptions().setAcceptDownloads(true));
    page.setContent("<a target=_blank href='" + server.PREFIX + "/download'>download</a>");
    Download download = page.waitForDownload(() -> page.click("a"));
    Path path = download.path();
    assertTrue(Files.exists(path));
    byte[] bytes = readAllBytes(path);
    assertEquals("Hello world", new String(bytes, UTF_8));
    page.close();
  }

  @PlaywrightTest
  void shouldDeleteFile(Browser browser, Server server) {
    Page page = browser.newPage(new Browser.NewPageOptions().setAcceptDownloads(true));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Download download = page.waitForDownload(() -> page.click("a"));
    Path path = download.path();
    assertTrue(Files.exists(path));
    download.delete();
    assertFalse(Files.exists(path));
    page.close();
  }

  @PlaywrightTest
  void shouldExposeStream(Browser browser, Server server) throws IOException {
    Page page = browser.newPage(new Browser.NewPageOptions().setAcceptDownloads(true));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Download download = page.waitForDownload(() -> page.click("a"));

    InputStream stream = download.createReadStream();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    copy(stream, out);
    assertEquals("Hello world", new String(out.toByteArray(), UTF_8));
    page.close();
  }

  @PlaywrightTest
  void streamShouldSupportZeroSizeRead(Browser browser, Server server) throws IOException {
    Page page = browser.newPage(new Browser.NewPageOptions().setAcceptDownloads(true));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Download download = page.waitForDownload(() -> page.click("a"));

    InputStream stream = download.createReadStream();
    byte[] b = new byte[1];
    int read = stream.read(b, 0, 0);
    assertEquals(0, read);
    page.close();
  }

  @PlaywrightTest
  void shouldDeleteDownloadsOnContextDestruction(Browser browser, Server server) {
    Page page = browser.newPage(new Browser.NewPageOptions().setAcceptDownloads(true));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Download download1 = page.waitForDownload(() -> page.click("a"));
    Download download2 = page.waitForDownload(() -> page.click("a"));
    Path path1 = download1.path();
    Path path2 = download2.path();
    assertTrue(Files.exists(path1));
    assertTrue(Files.exists(path2));
    page.context().close();
    assertFalse(Files.exists(path1));
    assertFalse(Files.exists(path2));
  }

  @PlaywrightTest
  void shouldDeleteDownloadsOnBrowserGone(BrowserType browserType, Server server) {
    Browser browser = browserType.launch();
    Page page = browser.newPage(new Browser.NewPageOptions().setAcceptDownloads(true));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Download download1 = page.waitForDownload(() -> page.click("a"));
    Download download2 = page.waitForDownload(() -> page.click("a"));
    Path path1 = download1.path();
    Path path2 = download2.path();
    assertTrue(Files.exists(path1));
    assertTrue(Files.exists(path2));
    browser.close();
    assertFalse(Files.exists(path1));
    assertFalse(Files.exists(path2));
    assertFalse(Files.exists(path1.resolve("..")));
  }

  @PlaywrightTest
  void shouldBeAbleToCancelPendingDownloads(Browser browser, Server server) {
    try (Page page = browser.newPage(new Browser.NewPageOptions().setAcceptDownloads(true))) {
      page.setContent("<a href='" + server.PREFIX + "/downloadWithDelay'>download</a>");
      Download download = page.waitForDownload(() -> page.click("a"));
      download.cancel();
      String failure = download.failure();
      assertEquals("canceled", failure);
    }
  }

  @PlaywrightTest
  void shouldNotFailExplicitlyToCancelADownloadEvenIfThatIsAlreadyFinished(Browser browser, Server server) throws IOException {
    try (Page page = browser.newPage(new Browser.NewPageOptions().setAcceptDownloads(true))) {
      page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
      Download download = page.waitForDownload(() -> page.click("a"));

      Path path = download.path();
      assertTrue(Files.exists(path));
      byte[] bytes = readAllBytes(path);
      assertEquals("Hello world", new String(bytes, UTF_8));
      download.cancel();
      assertNull(download.failure());
    }
  }
}
