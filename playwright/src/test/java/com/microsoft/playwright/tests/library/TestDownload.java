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

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.EnabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.HttpsTestServer;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestInfo;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.KeyboardModifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static com.microsoft.playwright.fixtures.Utils.async;
import static com.microsoft.playwright.fixtures.Utils.asset;
import static com.microsoft.playwright.fixtures.Utils.browserMajorVersion;
import static com.microsoft.playwright.fixtures.Utils.cause;
import static com.microsoft.playwright.fixtures.Utils.kTargetClosedErrorMessage;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/download.spec.ts. */
public class TestDownload {
  // Java: stands for fs.readFileSync(path).toString().
  private static String readFile(Path path) throws IOException {
    return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
  }

  // Java: stands for collecting the 'data' events of a Node.js stream.
  private static byte[] readStream(InputStream stream) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] buffer = new byte[8192];
    for (int read = stream.read(buffer); read != -1; read = stream.read(buffer)) {
      out.write(buffer, 0, read);
    }
    return out.toByteArray();
  }

  @Nested
  class DownloadEvent {
    @BeforeEach
    void setUp(TestServer server) {
      server.setRoute("/download", (req, res) -> {
        res.setHeader("Content-Type", "application/octet-stream");
        res.setHeader("Content-Disposition", "attachment");
        res.end("Hello world");
      });
      server.setRoute("/downloadWithFilename", (req, res) -> {
        res.setHeader("Content-Type", "application/octet-stream");
        res.setHeader("Content-Disposition", "attachment; filename=file.txt");
        res.end("Hello world");
      });
      server.setRoute("/downloadWithDelay", (req, res) -> {
        res.setHeader("Content-Type", "application/octet-stream");
        res.setHeader("Content-Disposition", "attachment; filename=file.txt");
        // Chromium requires a large enough payload to trigger the download event soon enough
        StringBuilder a = new StringBuilder();
        for (int i = 0; i < 4096; i++) {
          a.append('a');
        }
        res.write(a.toString());
        res.write("foo");
        res.uncork();
      });
      server.setRoute("/downloadWithCOOP", (req, res) -> {
        res.setHeader("Content-Type", "application/octet-stream");
        res.setHeader("Content-Disposition", "attachment");
        res.setHeader("Cross-Origin-Opener-Policy", "same-origin");
        res.end("Hello world");
      });
    }

    @PlaywrightTest
    @DisplayName("DO-001 should report download when navigation turns into download @smoke")
    void shouldReportDownloadWhenNavigationTurnsIntoDownload(Browser browser, TestServer server, BrowserName browserName) throws IOException {
      assumeFalse(browserName == BrowserName.CHROMIUM && browserMajorVersion(browser) < 140, "old chromium throws net::ERR_ABORTED, depends on https://chromium-review.googlesource.com/c/chromium/src/+/6696011");
      Page page = browser.newPage();
      AtomicReference<Object> responseOrError = new AtomicReference<>();
      Download download = page.waitForDownload(() -> {
        try {
          responseOrError.set(page.navigate(server.PREFIX + "/download"));
        } catch (PlaywrightException e) {
          responseOrError.set(e);
        }
      });
      assertEquals(page, download.page());
      assertEquals(server.PREFIX + "/download", download.url());
      Path path = download.path();
      assertTrue(Files.exists(path));
      assertEquals("Hello world", readFile(path));

      assertTrue(responseOrError.get() instanceof PlaywrightException);
      assertTrue(((PlaywrightException) responseOrError.get()).getMessage().contains("Download is starting"));

      if (browserName != BrowserName.FIREFOX || Env.isBidi()) {
        assertEquals("about:blank", page.url());
      }
      page.close();
    }

    @PlaywrightTest
    @DisplayName("DO-002 should work with Cross-Origin-Opener-Policy")
    void shouldWorkWithCrossOriginOpenerPolicy(Browser browser, TestServer server, BrowserName browserName) throws IOException {
      assumeFalse(browserName == BrowserName.CHROMIUM && browserMajorVersion(browser) < 140, "old chromium throws net::ERR_ABORTED, depends on https://chromium-review.googlesource.com/c/chromium/src/+/6696011");
      Page page = browser.newPage();
      AtomicReference<Object> responseOrError = new AtomicReference<>();
      Download download = page.waitForDownload(() -> {
        try {
          responseOrError.set(page.navigate(server.PREFIX + "/downloadWithCOOP"));
        } catch (PlaywrightException e) {
          responseOrError.set(e);
        }
      });
      assertEquals(page, download.page());
      assertEquals(server.PREFIX + "/downloadWithCOOP", download.url());
      Path path = download.path();
      assertTrue(Files.exists(path));
      assertEquals("Hello world", readFile(path));
      assertTrue(responseOrError.get() instanceof PlaywrightException);
      assertTrue(((PlaywrightException) responseOrError.get()).getMessage().contains("Download is starting"));
      if (browserName != BrowserName.FIREFOX || Env.isBidi()) {
        assertEquals("about:blank", page.url());
      }
      page.close();
    }

    @PlaywrightTest
    @DisplayName("DO-003 should report downloads with acceptDownloads: false")
    void shouldReportDownloadsWithAcceptDownloadsFalse(Browser browser, TestServer server) {
      Page page = browser.newPage(new Browser.NewPageOptions().setAcceptDownloads(false));
      page.setContent("<a href=\"" + server.PREFIX + "/downloadWithFilename\">download</a>");
      Download download = page.waitForDownload(() -> page.click("a"));
      assertEquals(page, download.page());
      assertEquals(server.PREFIX + "/downloadWithFilename", download.url());
      assertEquals("file.txt", download.suggestedFilename());
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> download.path());
      assertTrue(download.failure().contains("acceptDownloads"));
      assertTrue(error.getMessage().contains("acceptDownloads: true"));
      page.close();
    }

    @PlaywrightTest
    @DisplayName("DO-004 should report downloads with acceptDownloads: true")
    void shouldReportDownloadsWithAcceptDownloadsTrue(Browser browser, TestServer server) throws IOException {
      Page page = browser.newPage();
      page.setContent("<a href=\"" + server.PREFIX + "/download\">download</a>");
      Download download = page.waitForDownload(() -> page.click("a"));
      Path path = download.path();
      assertTrue(Files.exists(path));
      assertEquals("Hello world", readFile(path));
      page.close();
    }

    @PlaywrightTest
    @DisplayName("DO-005 should report proper download url when download is from download attribute")
    void shouldReportProperDownloadUrlWhenDownloadIsFromDownloadAttribute(Browser browser, TestServer server, BrowserName browserName) {
      Page page = browser.newPage();
      page.navigate(server.PREFIX + "/empty.html");
      page.setContent("<a href=\"" + server.PREFIX + "/chromium-linux.zip\" download=\"foo.zip\">download</a>");
      Download download = page.waitForDownload(() -> page.click("a"));
      assertEquals(server.PREFIX + "/chromium-linux.zip", download.url());
      page.close();
    }

    @PlaywrightTest
    @DisplayName("DO-006 should report downloads for download attribute")
    void shouldReportDownloadsForDownloadAttribute(Browser browser, TestServer server) {
      Page page = browser.newPage();
      page.navigate(server.PREFIX + "/empty.html");
      page.setContent("<a href=\"" + server.PREFIX + "/chromium-linux.zip\" download=\"foo.zip\">download</a>");
      Download download = page.waitForDownload(() -> page.click("a"));
      assertEquals("foo.zip", download.suggestedFilename());
      Path path = download.path();
      assertTrue(Files.exists(path));
      page.close();
    }

    @PlaywrightTest
    @DisplayName("DO-007 should save to user-specified path without updating original path")
    void shouldSaveToUserSpecifiedPathWithoutUpdatingOriginalPath(Browser browser, TestServer server, TestInfo testInfo) throws IOException {
      Page page = browser.newPage();
      page.setContent("<a href=\"" + server.PREFIX + "/download\">download</a>");
      Download download = page.waitForDownload(() -> page.click("a"));
      Path userPath = testInfo.outputPath("download.txt");
      download.saveAs(userPath);
      assertTrue(Files.exists(userPath));
      assertEquals("Hello world", readFile(userPath));

      Path originalPath = download.path();
      assertTrue(Files.exists(originalPath));
      assertEquals("Hello world", readFile(originalPath));
      page.close();
    }

    @PlaywrightTest
    @DisplayName("DO-008 should save to two different paths with multiple saveAs calls")
    void shouldSaveToTwoDifferentPathsWithMultipleSaveAsCalls(Browser browser, TestServer server, TestInfo testInfo) throws IOException {
      Page page = browser.newPage();
      page.setContent("<a href=\"" + server.PREFIX + "/download\">download</a>");
      Download download = page.waitForDownload(() -> page.click("a"));
      Path userPath = testInfo.outputPath("download.txt");
      download.saveAs(userPath);
      assertTrue(Files.exists(userPath));
      assertEquals("Hello world", readFile(userPath));

      Path anotherUserPath = testInfo.outputPath("download (2).txt");
      download.saveAs(anotherUserPath);
      assertTrue(Files.exists(anotherUserPath));
      assertEquals("Hello world", readFile(anotherUserPath));
      page.close();
    }

    @PlaywrightTest
    @DisplayName("DO-009 should save to overwritten filepath")
    void shouldSaveToOverwrittenFilepath(Browser browser, TestServer server, TestInfo testInfo) throws IOException {
      Page page = browser.newPage();
      page.setContent("<a href=\"" + server.PREFIX + "/download\">download</a>");
      Download download = page.waitForDownload(() -> page.click("a"));
      Path dir = testInfo.outputPath("downloads");
      Path userPath = dir.resolve("download.txt");
      download.saveAs(userPath);
      assertEquals(1, dir.toFile().list().length);
      download.saveAs(userPath);
      assertEquals(1, dir.toFile().list().length);
      assertTrue(Files.exists(userPath));
      assertEquals("Hello world", readFile(userPath));
      page.close();
    }

    @PlaywrightTest
    @DisplayName("DO-010 should create subdirectories when saving to non-existent user-specified path")
    void shouldCreateSubdirectoriesWhenSavingToNonExistentUserSpecifiedPath(Browser browser, TestServer server, TestInfo testInfo) throws IOException {
      Page page = browser.newPage();
      page.setContent("<a href=\"" + server.PREFIX + "/download\">download</a>");
      Download download = page.waitForDownload(() -> page.click("a"));
      Path nestedPath = testInfo.outputPath("these", "are", "directories", "download.txt");
      download.saveAs(nestedPath);
      assertTrue(Files.exists(nestedPath));
      assertEquals("Hello world", readFile(nestedPath));
      page.close();
    }

    @PlaywrightTest
    @DisplayName("DO-011 should error when saving with downloads disabled")
    void shouldErrorWhenSavingWithDownloadsDisabled(Browser browser, TestServer server, TestInfo testInfo) {
      Page page = browser.newPage(new Browser.NewPageOptions().setAcceptDownloads(false));
      page.setContent("<a href=\"" + server.PREFIX + "/download\">download</a>");
      Download download = page.waitForDownload(() -> page.click("a"));
      Path userPath = testInfo.outputPath("download.txt");
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> download.saveAs(userPath));
      assertTrue(error.getMessage().contains("Pass { acceptDownloads: true } when you are creating your browser context"));
      page.close();
    }

    @PlaywrightTest
    @DisplayName("DO-012 should error when saving after deletion")
    void shouldErrorWhenSavingAfterDeletion(Browser browser, TestServer server, TestInfo testInfo) {
      Page page = browser.newPage();
      page.setContent("<a href=\"" + server.PREFIX + "/download\">download</a>");
      Download download = page.waitForDownload(() -> page.click("a"));
      Path userPath = testInfo.outputPath("download.txt");
      download.delete();
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> download.saveAs(userPath));
      assertTrue(error.getMessage().contains("Target page, context or browser has been closed"));
      page.close();
    }

    @PlaywrightTest
    @DisplayName("DO-013 should report non-navigation downloads")
    void shouldReportNonNavigationDownloads(Browser browser, TestServer server) throws IOException {
      // Mac WebKit embedder does not download in this case, although Safari does.
      server.setRoute("/download", (req, res) -> {
        res.setHeader("Content-Type", "application/octet-stream");
        res.end("Hello world");
      });

      Page page = browser.newPage();
      page.navigate(server.EMPTY_PAGE);
      page.setContent("<a download=\"file.txt\" href=\"" + server.PREFIX + "/download\">download</a>");
      Download download = page.waitForDownload(() -> page.click("a"));
      assertEquals("file.txt", download.suggestedFilename());
      Path path = download.path();
      assertTrue(Files.exists(path));
      assertEquals("Hello world", readFile(path));
      page.close();
    }

    @PlaywrightTest
    @DisplayName("DO-014 should report download path within page.on('download', …) handler for Files")
    void shouldReportDownloadPathWithinPageOnDownloadHandlerForFiles(Browser browser, TestServer server) throws Exception {
      Page page = browser.newPage();
      CompletableFuture<Path> onDownloadPath = new CompletableFuture<>();
      page.onDownload(dl -> onDownloadPath.complete(dl.path()));
      page.setContent("<a href=\"" + server.PREFIX + "/download\">download</a>");
      page.click("a");
      page.waitForCondition(onDownloadPath::isDone);
      Path path = onDownloadPath.get();
      assertEquals("Hello world", readFile(path));
      page.close();
    }

    @PlaywrightTest
    @DisplayName("DO-015 should report download path within page.on('download', …) handler for Blobs")
    void shouldReportDownloadPathWithinPageOnDownloadHandlerForBlobs(Browser browser, TestServer server) throws Exception {
      Page page = browser.newPage();
      CompletableFuture<Path> onDownloadPath = new CompletableFuture<>();
      page.onDownload(dl -> onDownloadPath.complete(dl.path()));
      page.navigate(server.PREFIX + "/download-blob.html");
      page.click("a");
      page.waitForCondition(onDownloadPath::isDone);
      Path path = onDownloadPath.get();
      assertEquals("Hello world", readFile(path));
      page.close();
    }

    @PlaywrightTest
    @DisplayName("DO-016 should report alt-click downloads")
    @DisabledOnBrowser(value = BrowserName.FIREFOX, reason = "Firefox does not download on alt-click.")
    void shouldReportAltClickDownloads(Browser browser, TestServer server, BrowserName browserName) throws IOException {
      server.setRoute("/download", (req, res) -> {
        res.setHeader("Content-Type", "application/octet-stream");
        res.end("Hello world");
      });

      Page page = browser.newPage();
      page.navigate(server.EMPTY_PAGE);
      page.setContent("<a href=\"" + server.PREFIX + "/download\">download</a>");
      Download download = page.waitForDownload(() -> page.click("a", new Page.ClickOptions().setModifiers(Arrays.asList(KeyboardModifier.ALT))));
      Path path = download.path();
      assertTrue(Files.exists(path));
      assertEquals("Hello world", readFile(path));
      page.close();
    }

    @PlaywrightTest
    @DisplayName("DO-017 should report new window downloads")
    void shouldReportNewWindowDownloads(Browser browser, TestServer server) {
      Page page = browser.newPage();
      page.setContent("<a target=_blank href=\"" + server.PREFIX + "/download\">download</a>");
      Download download = page.waitForDownload(() -> page.click("a"));
      Path path = download.path();
      assertTrue(Files.exists(path));
      page.close();
    }

    @PlaywrightTest
    @DisplayName("DO-018 should delete file")
    void shouldDeleteFile(Browser browser, TestServer server) {
      Page page = browser.newPage();
      page.setContent("<a href=\"" + server.PREFIX + "/download\">download</a>");
      Download download = page.waitForDownload(() -> page.click("a"));
      Path path = download.path();
      assertTrue(Files.exists(path));
      download.delete();
      assertFalse(Files.exists(path));
      page.close();
    }

    @PlaywrightTest
    @DisplayName("DO-019 should expose stream")
    void shouldExposeStream(Browser browser, TestServer server) throws IOException {
      Page page = browser.newPage();
      page.setContent("<a href=\"" + server.PREFIX + "/download\">download</a>");
      Download download = page.waitForDownload(() -> page.click("a"));
      InputStream stream = download.createReadStream();
      String content = new String(readStream(stream), StandardCharsets.UTF_8);
      assertEquals("Hello world", content);
      page.close();
    }

    @PlaywrightTest
    @DisplayName("DO-020 should delete downloads on context destruction")
    void shouldDeleteDownloadsOnContextDestruction(Browser browser, TestServer server) {
      Page page = browser.newPage();
      page.setContent("<a href=\"" + server.PREFIX + "/download\">download</a>");
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
    @DisplayName("DO-021 should delete downloads on browser gone")
    void shouldDeleteDownloadsOnBrowserGone(TestServer server, BrowserType browserType) {
      Browser browser = browserType.launch(Env.launchOptions());
      Page page = browser.newPage();
      page.setContent("<a href=\"" + server.PREFIX + "/download\">download</a>");
      Download download1 = page.waitForDownload(() -> page.click("a"));
      Download download2 = page.waitForDownload(() -> page.click("a"));
      Path path1 = download1.path();
      Path path2 = download2.path();
      assertTrue(Files.exists(path1));
      assertTrue(Files.exists(path2));
      browser.close();
      assertFalse(Files.exists(path1));
      assertFalse(Files.exists(path2));
      assertFalse(Files.exists(path1.resolve("..").normalize()));
    }

    @PlaywrightTest
    @DisplayName("DO-022 should save downloads to artifactsDir")
    void shouldSaveDownloadsToArtifactsDir(TestServer server, BrowserType browserType, TestInfo testInfo) {
      Path artifactsDir = testInfo.outputPath("artifacts");
      Browser browser = browserType.launch(Env.launchOptions().setArtifactsDir(artifactsDir));
      Page page = browser.newPage();
      page.setContent("<a href=\"" + server.PREFIX + "/download\">download</a>");
      Download download = page.waitForDownload(() -> page.click("a"));
      Path downloadPath = download.path();
      assertTrue(downloadPath.startsWith(artifactsDir));
      assertTrue(Files.exists(downloadPath));
      browser.close();
      // User-provided artifactsDir should not be cleaned up.
      assertTrue(Files.exists(artifactsDir));
    }

    @PlaywrightTest
    @DisplayName("DO-023 should close the context without awaiting the failed download")
    @EnabledOnBrowser(BrowserName.CHROMIUM)
    @Fixme("java: a driver error message is the serialized error \"Error {message='canceled name='Error stack=...\" without the 'download.path: ' prefix")
    void shouldCloseTheContextWithoutAwaitingTheFailedDownload(Browser browser, TestServer server, HttpsTestServer httpsServer, TestInfo testInfo) {
      Page page = browser.newPage();
      page.navigate(server.EMPTY_PAGE);
      page.setContent("<a href=\"" + httpsServer.PREFIX + "/downloadWithFilename\" download=\"file.txt\">click me</a>");
      // Use alt-click to force the download. Otherwise browsers might try to navigate first,
      // probably because of http -> https link.
      Download download = page.waitForDownload(() -> page.click("a", new Page.ClickOptions().setModifiers(Arrays.asList(KeyboardModifier.ALT))));
      // Java: the Promise.all of three calls runs the first two in async.
      CompletableFuture<Path> downloadPromise = async(() -> download.path());
      CompletableFuture<Void> savePromise = async(() -> {
        download.saveAs(testInfo.outputPath("download.txt"));
        return null;
      });
      page.context().close();
      Throwable downloadError = cause(downloadPromise);
      Throwable saveError = cause(savePromise);
      assertEquals("download.path: canceled", downloadError.getMessage());
      assertTrue(Arrays.asList(
        "download.saveAs: File not found on disk. Check download.failure() for details.",
        "download.saveAs: canceled").contains(saveError.getMessage()));
    }

    @PlaywrightTest
    @DisplayName("DO-024 should close the context without awaiting the download")
    @Fixme("java: a driver error message is the serialized error without the 'download.path: ' / 'download.saveAs: ' prefix")
    void shouldCloseTheContextWithoutAwaitingTheDownload(Browser browser, TestServer server, BrowserName browserName, TestInfo testInfo) {
      assumeFalse(browserName == BrowserName.WEBKIT && (Env.platform().equals("linux") || "webkit-wsl".equals(Env.channel())), "WebKit on linux does not convert to the download immediately upon receiving headers");

      server.setRoute("/downloadStall", (req, res) -> {
        res.setHeader("Content-Type", "application/octet-stream");
        res.setHeader("Content-Disposition", "attachment; filename=file.txt");
        res.writeHead(200);
        res.flushHeaders();
        res.write("Hello world");
      });

      Page page = browser.newPage();
      page.navigate(server.EMPTY_PAGE);
      page.setContent("<a href=\"" + server.PREFIX + "/downloadStall\" download=\"file.txt\">click me</a>");
      Download download = page.waitForDownload(() -> page.click("a"));
      // Java: the Promise.all of three calls runs the first two in async.
      CompletableFuture<Path> downloadPromise = async(() -> download.path());
      CompletableFuture<Void> savePromise = async(() -> {
        download.saveAs(testInfo.outputPath("download.txt"));
        return null;
      });
      page.context().close();
      Throwable downloadError = cause(downloadPromise);
      Throwable saveError = cause(savePromise);
      // The exact error message is racy, because sometimes browser is fast enough
      // to cancel the download.
      assertTrue(Arrays.asList(
        "download.path: canceled",
        "download.path: " + kTargetClosedErrorMessage).contains(downloadError.getMessage()));
      assertTrue(Arrays.asList(
        "download.saveAs: canceled",
        "download.saveAs: " + kTargetClosedErrorMessage).contains(saveError.getMessage()));
    }

    @PlaywrightTest
    @DisplayName("DO-026 should download large binary.zip")
    void shouldDownloadLargeBinaryZip(Browser browser, TestServer server, BrowserName browserName, TestInfo testInfo) throws IOException {
      Path zipFile = testInfo.outputPath("binary.zip");
      byte[] content = new byte[1 << 20];
      new Random().nextBytes(content);
      Files.write(zipFile, content);
      server.setRoute("/binary.zip", (req, res) -> server.serveFile(req, res, zipFile));

      Page page = browser.newPage();
      page.navigate(server.PREFIX + "/empty.html");
      page.setContent("<a href=\"" + server.PREFIX + "/binary.zip\" download=\"binary.zip\">download</a>");
      Download download = page.waitForDownload(() -> page.click("a"));
      Path downloadPath = download.path();
      byte[] fileContent = Files.readAllBytes(downloadPath);
      assertEquals(content.length, fileContent.length);
      assertTrue(Arrays.equals(content, fileContent));

      InputStream stream = download.createReadStream();
      byte[] data = readStream(stream);
      assertEquals(content.length, data.length);
      assertTrue(Arrays.equals(content, data));
      page.close();
    }

    @PlaywrightTest
    @DisplayName("DO-027 should be able to cancel pending downloads")
    void shouldBeAbleToCancelPendingDownloads(Browser browser, TestServer server, BrowserName browserName) {
      Page page = browser.newPage();
      page.setContent("<a href=\"" + server.PREFIX + "/downloadWithDelay\">download</a>");
      Download download = page.waitForDownload(() -> page.click("a"));
      download.cancel();
      String failure = download.failure();
      assertEquals("canceled", failure);
      page.close();
    }

    @PlaywrightTest
    @DisplayName("DO-028 should not fail explicitly to cancel a download even if that is already finished")
    void shouldNotFailExplicitlyToCancelADownloadEvenIfThatIsAlreadyFinished(Browser browser, TestServer server, BrowserName browserName) throws IOException {
      Page page = browser.newPage();
      page.setContent("<a href=\"" + server.PREFIX + "/download\">download</a>");
      Download download = page.waitForDownload(() -> page.click("a"));
      Path path = download.path();
      assertTrue(Files.exists(path));
      assertEquals("Hello world", readFile(path));
      download.cancel();
      String failure = download.failure();
      assertNull(failure);
      page.close();
    }

    @PlaywrightTest
    @DisplayName("DO-029 should report downloads with interception")
    void shouldReportDownloadsWithInterception(Browser browser, TestServer server) throws IOException {
      Page page = browser.newPage();
      page.route(Pattern.compile(".*"), r -> r.resume());
      page.setContent("<a href=\"" + server.PREFIX + "/download\">download</a>");
      Download download = page.waitForDownload(() -> page.click("a"));
      Path path = download.path();
      assertTrue(Files.exists(path));
      assertEquals("Hello world", readFile(path));
      page.close();
    }

    @PlaywrightTest
    @DisplayName("DO-030 should emit download event from nested iframes")
    void shouldEmitDownloadEventFromNestedIframes(TestServer server, Browser browser, BrowserName browserName, TestInfo testInfo) throws IOException {
      Page page = browser.newPage();
      server.setRoute("/1", (req, res) -> {
        res.setHeader("Content-Type", "text/html");
        res.end("<iframe src=\"" + server.PREFIX + "/2\"></iframe>");
      });
      server.setRoute("/2", (req, res) -> {
        res.setHeader("Content-Type", "text/html");
        res.end("<iframe src=\"" + server.PREFIX + "/3\"></iframe>");
      });
      server.setRoute("/3", (req, res) -> {
        res.setHeader("Content-Type", "text/html");
        res.end(" <a href=\"" + server.PREFIX + "/download\">download</a>");
      });
      page.navigate(server.PREFIX + "/1");

      Download download = page.waitForDownload(() -> page.frameByUrl(server.PREFIX + "/3").click("text=download"));
      Path userPath = testInfo.outputPath("download.txt");
      download.saveAs(userPath);
      assertTrue(Files.exists(userPath));
      assertEquals("Hello world", readFile(userPath));
      page.close();
    }
  }

  @PlaywrightTest
  @DisplayName("DO-031 should be able to download a PDF file")
  void shouldBeAbleToDownloadAPDFFile(Browser browser, TestServer server) throws IOException {
    Page page = browser.newPage();
    page.navigate(server.EMPTY_PAGE);
    page.setContent("\n" +
      "    <a href=\"/empty.pdf\" download>download</a>\n" +
      "  ");
    Download download = page.waitForDownload(() -> page.click("a"));
    assertDownloadToPDF(download, asset("empty.pdf"));
    page.close();
  }

  @PlaywrightTest
  @DisplayName("DO-032 should be able to download a inline PDF file via response interception")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "fixme: ")
  void shouldBeAbleToDownloadAInlinePDFFileViaResponseInterception(Browser browser, TestServer server, BrowserName browserName) throws IOException {
    Page page = browser.newPage();
    page.navigate(server.EMPTY_PAGE);
    page.route("**/empty.pdf", route -> {
      APIResponse response = page.context().request().fetch(route.request());
      Map<String, String> headers = new HashMap<>(response.headers());
      headers.put("Content-Disposition", "attachment");
      route.fulfill(new Route.FulfillOptions().setResponse(response).setHeaders(headers));
    });
    page.setContent("\n" +
      "    <a href=\"/empty.pdf\">open</a>\n" +
      "  ");
    Download download = page.waitForDownload(() -> page.click("a"));
    assertDownloadToPDF(download, asset("empty.pdf"));
    page.close();
  }

  @PlaywrightTest
  @DisplayName("DO-033 should be able to download a inline PDF file via navigation")
  void shouldBeAbleToDownloadAInlinePDFFileViaNavigation(Browser browser, TestServer server, BrowserName browserName) throws IOException {
    assumeFalse(browserName == BrowserName.CHROMIUM && !Env.isHeadlessShell(browserName), "We expect PDF Viewer to open up in headed Chromium");
    assumeFalse(browserName == BrowserName.FIREFOX && Env.isBidi(), "We expect PDF Viewer to open up in Firefox with Bidi");

    Page page = browser.newPage();
    page.navigate(server.EMPTY_PAGE);
    page.setContent("\n" +
      "    <a href=\"/empty.pdf\">open</a>\n" +
      "  ");
    Download download = page.waitForDownload(() -> page.click("a"));
    assertDownloadToPDF(download, asset("empty.pdf"));
    page.close();
  }

  @PlaywrightTest
  @DisplayName("DO-034 should save to user-specified path")
  void shouldSaveToUserSpecifiedPath(Browser browser, TestServer server, TestInfo testInfo) throws IOException {
    server.setRoute("/download", (req, res) -> {
      res.setHeader("Content-Type", "application/octet-stream");
      res.setHeader("Content-Disposition", "attachment");
      res.end("Hello world");
    });

    Page page = browser.newPage();
    page.setContent("<a href=\"" + server.PREFIX + "/download\">download</a>");
    Download download = page.waitForDownload(() -> page.click("a"));
    Path userPath = testInfo.outputPath("download.txt");
    download.saveAs(userPath);
    assertTrue(Files.exists(userPath));
    assertEquals("Hello world", readFile(userPath));
    page.close();
  }

  @PlaywrightTest
  @DisplayName("DO-035 should download even if there is no \"attachment\" value")
  void shouldDownloadEvenIfThereIsNoAttachmentValue(Browser browser, TestServer server) {
    server.setRoute("/download", (req, res) -> {
      res.setHeader("Content-Type", "application/octet-stream");
      // Do not set the "attachment" value.
      res.setHeader("Content-Disposition", "filename=foo.txt");
      res.end("Hello world");
    });

    Page page = browser.newPage();
    page.setContent("<a href=\"" + server.PREFIX + "/download\">download</a>");
    page.waitForDownload(() -> page.click("a"));
    page.close();
  }

  @PlaywrightTest
  @DisplayName("DO-036 should convert navigation to a resource with unsupported mime type into download")
  void shouldConvertNavigationToAResourceWithUnsupportedMimeTypeIntoDownload(Browser browser, TestServer server) {
    server.setRoute("/download", (req, res) -> {
      res.setHeader("Content-Type", "application/octet-stream");
      res.end("Hello world");
    });
    Page page = browser.newPage();
    page.waitForDownload(() -> {
      try {
        page.navigate(server.PREFIX + "/download");
      } catch (PlaywrightException e) {
      }
    });
    page.close();
  }

  @PlaywrightTest
  @DisplayName("DO-037 should download links with data url")
  void shouldDownloadLinksWithDataUrl(Page page) {
    page.setContent("<a download=\"SomeFile.txt\" href=\"data:text/plain;charset=utf8;,hello world\">Download!</a>");
    Download download = page.waitForDownload(() -> page.getByText("Download").click());
    assertEquals("SomeFile.txt", download.suggestedFilename());
  }

  @PlaywrightTest
  @DisplayName("DO-038 should download successfully when routing")
  void shouldDownloadSuccessfullyWhenRouting(Browser browser, TestServer server) {
    Page page = browser.newPage();
    page.context().route("**/*", route -> route.resume());
    page.navigate(server.PREFIX + "/empty.html");
    page.setContent("<a href=\"" + server.PREFIX + "/chromium-linux.zip\" download=\"foo.zip\">download</a>");
    Download download = page.waitForDownload(() -> page.click("a"));
    assertEquals("foo.zip", download.suggestedFilename());
    assertEquals(server.PREFIX + "/chromium-linux.zip", download.url());
    assertNull(download.failure());
    page.close();
  }

  private static void assertDownloadToPDF(Download download, Path filePath) throws IOException {
    assertEquals(filePath.getFileName().toString(), download.suggestedFilename());
    InputStream stream = download.createReadStream();
    byte[] data = readStream(stream);
    assertTrue(download.url().endsWith("/" + filePath.getFileName().toString()));
    String expectedPrefix = "%PDF";
    for (int i = 0; i < expectedPrefix.length(); i++) {
      assertEquals((int) expectedPrefix.charAt(i), data[i]);
    }
    assertBuffer(data, Files.readAllBytes(filePath));
  }

  private static void assertBuffer(byte[] expected, byte[] actual) {
    assertEquals(expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals(expected[i], actual[i]);
    }
  }
}
