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

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;

import static com.microsoft.playwright.Keyboard.Modifier.ALT;
import static com.microsoft.playwright.Page.EventType.DOWNLOAD;
import static com.microsoft.playwright.Utils.copy;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllBytes;
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
  }

  @Test
  void shouldReportDownloadsWithAcceptDownloadsFalse() {
    page.setContent("<a href='" + server.PREFIX + "/downloadWithFilename'>download</a>");
    Deferred<Event<Page.EventType>> downloadEvent = page.futureEvent(DOWNLOAD);
    page.click("a");
    Download download = (Download) downloadEvent.get().data();
    assertEquals(server.PREFIX + "/downloadWithFilename", download.url());
    assertEquals("file.txt", download.suggestedFilename());
    try {
      download.path();
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(download.failure().contains("acceptDownloads"));
      assertTrue(e.getMessage().contains("acceptDownloads: true"));
    }
  }
  @Test
  void shouldReportDownloadsWithAcceptDownloadsTrue() throws IOException {
    Page page = browser.newPage(new Browser.NewPageOptions().withAcceptDownloads(true));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Deferred<Event<Page.EventType>> downloadEvent = page.futureEvent(DOWNLOAD);
    page.click("a");
    Download download = (Download) downloadEvent.get().data();
    Path path = download.path();
    assertTrue(Files.exists(path));
    byte[] bytes = readAllBytes(path);
    assertEquals("Hello world", new String(bytes, UTF_8));
    page.close();
  }

  @Test
  void shouldSaveToUserSpecifiedPath() throws IOException {
    Page page = browser.newPage(new Browser.NewPageOptions().withAcceptDownloads(true));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Deferred<Event<Page.EventType>> downloadEvent = page.futureEvent(DOWNLOAD);
    page.click("a");
    Download download = (Download) downloadEvent.get().data();

    Path userFile = Files.createTempFile("download-", ".txt");
    download.saveAs(userFile);
    assertTrue(Files.exists(userFile));
    byte[] bytes = readAllBytes(userFile);
    assertEquals("Hello world", new String(bytes, UTF_8));
    page.close();
  }

  @Test
  void shouldSaveToUserSpecifiedPathWithoutUpdatingOriginalPath() throws IOException {
    Page page = browser.newPage(new Browser.NewPageOptions().withAcceptDownloads(true));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Deferred<Event<Page.EventType>> downloadEvent = page.futureEvent(DOWNLOAD);
    page.click("a");
    Download download = (Download) downloadEvent.get().data();

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
    Page page = browser.newPage(new Browser.NewPageOptions().withAcceptDownloads(true));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Deferred<Event<Page.EventType>> downloadEvent = page.futureEvent(DOWNLOAD);
    page.click("a");
    Download download = (Download) downloadEvent.get().data();
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
    Page page = browser.newPage(new Browser.NewPageOptions().withAcceptDownloads(true));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Deferred<Event<Page.EventType>> downloadEvent = page.futureEvent(DOWNLOAD);
    page.click("a");
    Download download = (Download) downloadEvent.get().data();
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
    Page page = browser.newPage(new Browser.NewPageOptions().withAcceptDownloads(true));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Deferred<Event<Page.EventType>> downloadEvent = page.futureEvent(DOWNLOAD);
    page.click("a");
    Download download = (Download) downloadEvent.get().data();

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
    Page page = browser.newPage(new Browser.NewPageOptions().withAcceptDownloads(false));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Deferred<Event<Page.EventType>> downloadEvent = page.futureEvent(DOWNLOAD);
    page.click("a");
    Download download = (Download) downloadEvent.get().data();

    Path userPath = Files.createTempFile("download-", ".txt");
    try {
      download.saveAs(userPath);
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("Pass { acceptDownloads: true } when you are creating your browser context"));
    }
    page.close();
  }

  @Test
  void shouldErrorWhenSavingAfterDeletion() throws IOException {
    Page page = browser.newPage(new Browser.NewPageOptions().withAcceptDownloads(true));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Deferred<Event<Page.EventType>> downloadEvent = page.futureEvent(DOWNLOAD);
    page.click("a");
    Download download = (Download) downloadEvent.get().data();

    Path userPath = Files.createTempFile("download-", ".txt");
    download.delete();
    try {
      download.saveAs(userPath);
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("Download already deleted. Save before deleting."));
    }
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

    Page page = browser.newPage(new Browser.NewPageOptions().withAcceptDownloads(true));
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a download='file.txt' href='" + server.PREFIX + "/download'>download</a>");
    Deferred<Event<Page.EventType>> downloadEvent = page.futureEvent(DOWNLOAD);
    page.click("a");
    Download download = (Download) downloadEvent.get().data();

    assertEquals("file.txt", download.suggestedFilename());
    Path path = download.path();
    assertTrue(Files.exists(path));
    byte[] bytes = readAllBytes(path);
    assertEquals("Hello world", new String(bytes, UTF_8));
    page.close();
  }

  @Test
  void shouldReportDownloadPathWithinPageOnDownloadHandlerForFiles() throws IOException {
    Page page = browser.newPage(new Browser.NewPageOptions().withAcceptDownloads(true));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    @SuppressWarnings("unchecked")
    Event<Page.EventType>[] event = new Event[]{null};
    page.addListener(DOWNLOAD, e -> event[0] = e);
    page.click("a");
    Instant start = Instant.now();
    while (event[0] == null) {
      page.waitForTimeout(100);
      assertTrue(Duration.between(start, Instant.now()).getSeconds() < 30, "Timed out");
    }
    Download download = (Download) event[0].data();
    Path path = download.path();
    assertTrue(Files.exists(path));
    byte[] bytes = readAllBytes(path);
    assertEquals("Hello world", new String(bytes, UTF_8));
    page.close();
  }

  @Test
  void shouldReportDownloadPathWithinPageOnDownloadHandlerForBlobs() throws IOException {
    Page page = browser.newPage(new Browser.NewPageOptions().withAcceptDownloads(true));
    @SuppressWarnings("unchecked")
    Event<Page.EventType>[] event = new Event[]{null};
    page.addListener(DOWNLOAD, e -> event[0] = e);
    page.navigate(server.PREFIX + "/download-blob.html");
    page.click("a");
    Instant start = Instant.now();
    while (event[0] == null) {
      page.waitForTimeout(100);
      assertTrue(Duration.between(start, Instant.now()).getSeconds() < 1, "Timed out");
    }
    Download download = (Download) event[0].data();
    Path path = download.path();
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
    Page page = browser.newPage(new Browser.NewPageOptions().withAcceptDownloads(true));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Deferred<Event<Page.EventType>> downloadEvent = page.futureEvent(DOWNLOAD);
    page.click("a", new Page.ClickOptions().withModifiers(ALT));
    Download download = (Download) downloadEvent.get().data();
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
    Page page = browser.newPage(new Browser.NewPageOptions().withAcceptDownloads(true));
    page.setContent("<a target=_blank href='" + server.PREFIX + "/download'>download</a>");
    Deferred<Event<Page.EventType>> downloadEvent = page.futureEvent(DOWNLOAD);
    page.click("a");
    Download download = (Download) downloadEvent.get().data();
    Path path = download.path();
    assertTrue(Files.exists(path));
    byte[] bytes = readAllBytes(path);
    assertEquals("Hello world", new String(bytes, UTF_8));
    page.close();
  }

  @Test
  void shouldDeleteFile() {
    Page page = browser.newPage(new Browser.NewPageOptions().withAcceptDownloads(true));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Deferred<Event<Page.EventType>> downloadEvent = page.futureEvent(DOWNLOAD);
    page.click("a");
    Download download = (Download) downloadEvent.get().data();
    Path path = download.path();
    assertTrue(Files.exists(path));
    download.delete();
    assertFalse(Files.exists(path));
    page.close();
  }

  @Test
  void shouldExposeStream() throws IOException {
    Page page = browser.newPage(new Browser.NewPageOptions().withAcceptDownloads(true));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Deferred<Event<Page.EventType>> downloadEvent = page.futureEvent(DOWNLOAD);
    page.click("a");
    Download download = (Download) downloadEvent.get().data();

    InputStream stream = download.createReadStream();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    copy(stream, out);
    assertEquals("Hello world", new String(out.toByteArray(), UTF_8));
    page.close();
  }

  @Test
  void shouldDeleteDownloadsOnContextDestruction() {
    Page page = browser.newPage(new Browser.NewPageOptions().withAcceptDownloads(true));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Deferred<Event<Page.EventType>> downloadEvent1 = page.futureEvent(DOWNLOAD);
    page.click("a");
    Download download1 = (Download) downloadEvent1.get().data();

    Deferred<Event<Page.EventType>> downloadEvent2 = page.futureEvent(DOWNLOAD);
    page.click("a");
    Download download2 = (Download) downloadEvent2.get().data();

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
    Page page = browser.newPage(new Browser.NewPageOptions().withAcceptDownloads(true));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Deferred<Event<Page.EventType>> downloadEvent1 = page.futureEvent(DOWNLOAD);
    page.click("a");
    Download download1 = (Download) downloadEvent1.get().data();

    Deferred<Event<Page.EventType>> downloadEvent2 = page.futureEvent(DOWNLOAD);
    page.click("a");
    Download download2 = (Download) downloadEvent2.get().data();

    Path path1 = download1.path();
    Path path2 = download2.path();
    assertTrue(Files.exists(path1));
    assertTrue(Files.exists(path2));
    browser.close();
    assertFalse(Files.exists(path1));
    assertFalse(Files.exists(path2));
    assertFalse(Files.exists(path1.resolve("..")));
  }
}
