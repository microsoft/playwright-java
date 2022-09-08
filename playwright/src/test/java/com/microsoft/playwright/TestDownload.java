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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.condition.EnabledIf;

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

import static com.microsoft.playwright.options.KeyboardModifier.ALT;
import static com.microsoft.playwright.Utils.copy;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class TestDownload extends TestBase {

  @BeforeEach
  void addRoutes() {
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

  @Test
  void shouldReportDownloadWhenNavigationTurnsIntoDownload() throws IOException {
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
    if (isChromium()) {
      assertNotNull(error[0]);
      assertTrue(error[0].getMessage().contains("net::ERR_ABORTED"));
      assertEquals("about:blank", page.url());
    } else if (isWebKit()) {
      assertNotNull(error[0]);
      assertTrue(error[0].getMessage().contains("Download is starting"));
      assertEquals("about:blank", page.url());
    } else {
      assertNotNull(error[0]);
      assertTrue(error[0].getMessage().contains("Download is starting"));
    }
    page.close();
  }

  @Test
  void shouldReportDownloadsWithAcceptDownloadsFalse() {
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
  @Test
  void shouldReportDownloadsWithAcceptDownloadsTrue() throws IOException {
    Page page = browser.newPage(new Browser.NewPageOptions().setAcceptDownloads(true));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Download download = page.waitForDownload(() -> page.click("a"));
    Path path = download.path();
    assertTrue(Files.exists(path));
    byte[] bytes = readAllBytes(path);
    assertEquals("Hello world", new String(bytes, UTF_8));
    page.close();
  }

  @Test
  void shouldSaveToUserSpecifiedPath() throws IOException {
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

  @Test
  void shouldSaveToUserSpecifiedPathWithoutUpdatingOriginalPath() throws IOException {
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


  @Test
  void shouldSaveToTwoDifferentPathsWithMultipleSaveAsCalls() throws IOException {
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

  @Test
  void shouldSaveToOverwrittenFilepath() throws IOException {
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

  @Test
  void shouldCreateSubdirectoriesWhenSavingToNonExistentUserSpecifiedPath() throws IOException {
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

  @Test
  void shouldErrorWhenSavingWithDownloadsDisabled() throws IOException {
    Page page = browser.newPage(new Browser.NewPageOptions().setAcceptDownloads(false));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Download download = page.waitForDownload(() -> page.click("a"));

    Path userPath = Files.createTempFile("download-", ".txt");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> download.saveAs(userPath));
    assertTrue(e.getMessage().contains("Pass { acceptDownloads: true } when you are creating your browser context"));
    page.close();
  }

  @Test
  void shouldErrorWhenSavingAfterDeletion() throws IOException {
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

  @Test
  void shouldReportNonNavigationDownloads() throws IOException {
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

  @Test
  void shouldReportDownloadPathWithinPageOnDownloadHandlerForFiles() throws IOException {
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

  @Test
  void shouldReportDownloadPathWithinPageOnDownloadHandlerForBlobs() throws IOException {
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

  @Test
  @EnabledIf(value="com.microsoft.playwright.TestBase#isChromium", disabledReason="fixme")
  void shouldReportAltClickDownloads() throws IOException {
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


  static boolean isChromiumHeadful() {
    return isChromium() && isHeadful();
  }

  @Test
  @DisabledIf(value="isChromiumHeadful", disabledReason="fixme")
  void shouldReportNewWindowDownloads() throws IOException {
    // TODO: - the test fails in headful Chromium as the popup page gets closed along
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

  @Test
  void shouldDeleteFile() {
    Page page = browser.newPage(new Browser.NewPageOptions().setAcceptDownloads(true));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Download download = page.waitForDownload(() -> page.click("a"));
    Path path = download.path();
    assertTrue(Files.exists(path));
    download.delete();
    assertFalse(Files.exists(path));
    page.close();
  }

  @Test
  void shouldExposeStream() throws IOException {
    Page page = browser.newPage(new Browser.NewPageOptions().setAcceptDownloads(true));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Download download = page.waitForDownload(() -> page.click("a"));

    InputStream stream = download.createReadStream();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    copy(stream, out);
    assertEquals("Hello world", new String(out.toByteArray(), UTF_8));
    page.close();
  }

  @Test
  void streamShouldSupportZeroSizeRead() throws IOException {
    Page page = browser.newPage(new Browser.NewPageOptions().setAcceptDownloads(true));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Download download = page.waitForDownload(() -> page.click("a"));

    InputStream stream = download.createReadStream();
    byte[] b = new byte[1];
    int read = stream.read(b, 0, 0);
    assertEquals(0, read);
    page.close();
  }

  @Test
  void shouldDeleteDownloadsOnContextDestruction() {
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

  @Test
  void shouldDeleteDownloadsOnBrowserGone() {
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

  @Test
  void shouldBeAbleToCancelPendingDownloads() {
    try (Page page = browser.newPage(new Browser.NewPageOptions().setAcceptDownloads(true))) {
      page.setContent("<a href='" + server.PREFIX + "/downloadWithDelay'>download</a>");
      Download download = page.waitForDownload(() -> page.click("a"));
      download.cancel();
      String failure = download.failure();
      assertEquals("canceled", failure);
    }
  }

  @Test
  void shouldNotFailExplicitlyToCancelADownloadEvenIfThatIsAlreadyFinished() throws IOException {
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
