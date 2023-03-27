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

import com.microsoft.playwright.impl.driver.Driver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.microsoft.playwright.Utils.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class TestBrowserTypeConnect extends TestBase {
  private Process browserServer;
  private String wsEndpoint;

  private static class BrowserServer {
    Process process;
    String wsEndpoint;

    void kill() throws InterruptedException {
      process.destroy();
      int exitCode = process.waitFor();
      // FIXME: 2 tests are failing this check on windows:
      // disconnectedEventShouldBeEmittedWhenBrowserIsClosedOrServerIsClosed
      // shouldThrowWhenUsedAfterIsConnectedReturnsFalse
      if (!isWindows) {
        assertEquals(0, exitCode);
      }
    }
  }

  private static BrowserServer launchBrowserServer(BrowserType browserType) {
    try {
      Driver driver = Driver.ensureDriverInstalled(Collections.emptyMap(), false);
      Path dir = driver.driverPath().getParent();
      String node = dir.resolve(isWindows ? "node.exe" : "node").toString();
      String cliJs = dir.resolve("package/lib/cli/cli.js").toString();
      // We launch node process directly instead of using playwright.sh script as killing the script
      // process will leave node process running and killing it would be more hassle.
      ProcessBuilder pb = new ProcessBuilder(node, cliJs, "launch-server", "--browser", browserType.name());
      pb.directory(dir.toFile());
      pb.redirectError(ProcessBuilder.Redirect.INHERIT);
      BrowserServer result = new BrowserServer();
      result.process = pb.start();
      BufferedReader input =  new BufferedReader(new InputStreamReader(result.process.getInputStream()));
      result.wsEndpoint = input.readLine();
      if (!result.wsEndpoint.startsWith("ws://")) {
        throw new RuntimeException("Invalid web socket address: " + result.wsEndpoint);
      }
      return result;
    } catch (IOException e) {
      throw new PlaywrightException("Failed to launch server", e);
    }
  }

  @Override
  @BeforeAll
  // Hide base class method to launch browser server and connect to it.
  void launchBrowser() {
    initBrowserType();
    BrowserServer r = launchBrowserServer(browserType);
    wsEndpoint = r.wsEndpoint;
    browserServer = r.process;
    browser = browserType.connect(wsEndpoint);
    // Do not actually connect to browser, the tests will do it manually.
  }

  @Override
  @AfterAll
  void closeBrowser() {
    super.closeBrowser();
    if (browserServer != null) {
      browserServer.destroyForcibly();
      browserServer = null;
      wsEndpoint = null;
    }
  }

  @Test
  void shouldBeAbleToReconnectToABrowser() {
    {
      Browser browser = browserType.connect(wsEndpoint);
      BrowserContext browserContext = browser.newContext();
      assertEquals(0, browserContext.pages().size());
      Page page = browserContext.newPage();
      assertEquals(121, page.evaluate("11 * 11"));
      page.navigate(server.EMPTY_PAGE);
      browser.close();
    }
    {
      Browser browser = browserType.connect(wsEndpoint);
      BrowserContext browserContext = browser.newContext();
      Page page = browserContext.newPage();
      page.navigate(server.EMPTY_PAGE);
      browser.close();
    }
  }

  @Test
  void shouldSupportSlowMo() {
    Browser browser = browserType.connect(wsEndpoint,
      new BrowserType.ConnectOptions().setSlowMo(1));
    BrowserContext browserContext = browser.newContext();
    Page page = browserContext.newPage();
    assertEquals(121, page.evaluate("11 * 11"));
    page.navigate(server.EMPTY_PAGE);
    browser.close();
  }

  @Test
  void shouldBeAbleToConnectTwoBrowsersAtTheSameTime() {
    Browser browser1 = browserType.connect(wsEndpoint);
    assertEquals(0, browser1.contexts().size());
    browser1.newContext();
    assertEquals(1, browser1.contexts().size());

    Browser browser2 = browserType.connect(wsEndpoint);
    assertEquals(0, browser2.contexts().size());
    browser2.newContext();
    assertEquals(1, browser2.contexts().size());
    assertEquals(1, browser1.contexts().size());

    browser1.close();
    Page page2 = browser2.newPage();
    assertEquals(42, page2.evaluate("7 * 6")); // original browser should still work

    browser2.close();
  }

  @Test
  void shouldSendExtraHeadersWithConnectRequest() throws Exception {
    try (WebSocketServerImpl webSocketServer = WebSocketServerImpl.create()) {
      try {
        browserType.connect("ws://localhost:" + webSocketServer.getPort() + "/ws",
          new BrowserType.ConnectOptions().setHeaders(mapOf("User-Agent", "Playwright", "foo", "bar")));
      } catch (Exception e) {
      }
      assertNotNull(webSocketServer.lastClientHandshake);
      assertEquals("Playwright", webSocketServer.lastClientHandshake.getFieldValue("User-Agent"));
      assertEquals(browserType.name(), webSocketServer.lastClientHandshake.getFieldValue("x-playwright-browser"));
      assertEquals("bar", webSocketServer.lastClientHandshake.getFieldValue("foo"));
    }
  }

  @Test
  void disconnectedEventShouldBeEmittedWhenBrowserIsClosedOrServerIsClosed() throws InterruptedException {
    // Launch another server to not affect other tests.
    BrowserServer remote = launchBrowserServer(browserType);

    Browser browser1 = browserType.connect(remote.wsEndpoint);
    Browser browser2 = browserType.connect(remote.wsEndpoint);

    int[] disconnected1 = {0};
    int[] disconnected2 = {0};
    browser1.onDisconnected(b -> ++disconnected1[0]);
    browser2.onDisconnected(b -> ++disconnected2[0]);

    Page page2 = browser2.newPage();

    browser1.close();
    assertEquals(1, disconnected1[0]);
    assertEquals(0, disconnected2[0]);

    remote.kill();
    assertEquals(1, disconnected1[0]);
    // Tickle connection so that it gets a chance to dispatch disconnect event.
    assertThrows(PlaywrightException.class, () -> page2.title());
    assertEquals(1, disconnected2[0]);
  }

  @Test
  void disconnectedEventShouldHaveBrowserAsArgument() {
    Browser browser = browserType.connect(wsEndpoint);
    Browser[] disconnected = {null};
    browser.onDisconnected(b -> disconnected[0] = b);
    browser.close();
    assertEquals(browser, disconnected[0]);
  }

  void shouldHandleExceptionsDuringConnect() {
    // This is an implementation detail test
  }

  @Test
  void shouldSetTheBrowserConnectedState() {
    Browser remote = browserType.connect(wsEndpoint);
    assertTrue(remote.isConnected());
    remote.close();
    assertFalse(remote.isConnected());
  }

  @Test
  void shouldThrowWhenUsedAfterIsConnectedReturnsFalse() throws InterruptedException {
    // Launch another server to not affect other tests.
    BrowserServer server = launchBrowserServer(browserType);
    Browser remote = browserType.connect(server.wsEndpoint);
    Page page = remote.newPage();
    boolean[] disconnected = {false};
    remote.onDisconnected(b -> disconnected[0] = true);
    server.kill();
    while (!disconnected[0]) {
      try {
        page.waitForTimeout(10);
      } catch (PlaywrightException e) {
      }
    }
    assertFalse(remote.isConnected());
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.evaluate("1 + 1"));
    assertTrue(e.getMessage().contains("Browser has been closed"), e.getMessage());
    assertFalse(remote.isConnected());
  }

  @Test
  void shouldThrowWhenCallingWaitForNavigationAfterDisconnect() throws InterruptedException {
    BrowserServer server = launchBrowserServer(browserType);
    Browser browser = browserType.connect(server.wsEndpoint);
    Page page = browser.newPage();

    boolean[] disconnected = {false};
    browser.onDisconnected(browser1 -> disconnected[0] = true);
    server.kill();
    while (!disconnected[0]) {
      try {
        page.waitForTimeout(10);
      } catch (PlaywrightException e) {
      }
    }
    assertFalse(browser.isConnected());
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.waitForNavigation(() -> {}));
    assertTrue(e.getMessage().contains("Page closed") || e.getMessage().contains("Browser has been closed"), e.getMessage());
  }

  @Test
  void shouldRejectNavigationWhenBrowserCloses() {
    Browser remote = browserType.connect(wsEndpoint);
    Page page = remote.newPage();

    server.setRoute("/one-style.css", r -> {});
    page.onRequest(r -> remote.close());
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.navigate(server.PREFIX + "/one-style.html", new Page.NavigateOptions().setTimeout(60000));
    });
    assertTrue(e.getMessage().contains("Browser has been closed"));
  }

  @Test
  void shouldEmitCloseEventsOnPagesAndContexts() throws InterruptedException {
    BrowserServer server = launchBrowserServer(browserType);
    Browser browser = browserType.connect(server.wsEndpoint);
    BrowserContext context = browser.newContext();
    Page page = context.newPage();

    List<String> events = new ArrayList<>();
    page.onClose(p -> events.add("page"));
    context.onClose(c -> events.add("context"));
    server.kill();

    while (!events.contains("context")) {
      try {
        page.waitForTimeout(10);
      } catch (PlaywrightException e) {
      }
    }
    assertEquals(Arrays.asList("page", "context"), events);
  }

  @Test
  void shouldRespectSelectors() {
    String mycss = "{\n" +
      "    query(root, selector) {\n" +
      "      return root.querySelector(selector);\n" +
      "    },\n" +
      "    queryAll(root, selector) {\n" +
      "      return Array.from(root.querySelectorAll(selector));\n" +
      "    }\n" +
      "  }";
    // Register one engine before connecting.
    playwright.selectors().register("mycss1", mycss);

    Browser browser1 = browserType.connect(wsEndpoint);
    BrowserContext context1 = browser1.newContext();

    // Register another engine after creating context.
    playwright.selectors().register("mycss2", mycss);

    Page page1 = context1.newPage();
    page1.setContent("<div>hello</div>");
    assertEquals("hello", page1.innerHTML("css=div"));
    assertEquals("hello", page1.innerHTML("mycss1=div"));
    assertEquals("hello", page1.innerHTML("mycss2=div"));

    Browser browser2 = browserType.connect(wsEndpoint);

    // Register third engine after second connect.
    playwright.selectors().register("mycss3", mycss);

    Page page2 = browser2.newPage();
    page2.setContent("<div>hello</div>");
    assertEquals("hello", page2.innerHTML("css=div"));
    assertEquals("hello", page2.innerHTML("mycss1=div"));
    assertEquals("hello", page2.innerHTML("mycss2=div"));
    assertEquals("hello", page2.innerHTML("mycss3=div"));

    browser1.close();
    browser2.close();
  }

  @Test
  void shouldNotThrowOnCloseAfterDisconnect() throws InterruptedException {
    BrowserServer remoteServer = launchBrowserServer(browserType);
    Browser browser = browserType.connect(remoteServer.wsEndpoint);
    Page page = browser.newPage();

    remoteServer.kill();
    while (browser.isConnected()) {
      try {
        page.waitForTimeout(10);
      } catch (PlaywrightException e) {
      }
    }
    browser.close();
  }
  
  @Test
  void shouldSaveAsVideosFromRemoteBrowser(@TempDir Path tempDir) {
    Path videosPath = tempDir.resolve("videosPath");
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setRecordVideoDir(videosPath).setRecordVideoSize(320,  240));
    Page page = context.newPage();
    page.evaluate("() => document.body.style.backgroundColor = 'red'");
    page.waitForTimeout(1000);
    context.close();
    Path savedAsPath = tempDir.resolve("my-video.webm");
    page.video().saveAs(savedAsPath);
    assertTrue(Files.exists(savedAsPath));
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.video().path());
    assertTrue(e.getMessage().contains("Path is not available when using browserType.connect(). Use saveAs() to save a local copy."));
  }


  @Test
  void shouldSaveDownload(@TempDir Path tempDir) throws IOException {
    server.setRoute("/download", exchange -> {
      exchange.getResponseHeaders().add("Content-Type", "application/octet-stream");
      exchange.getResponseHeaders().add("Content-Disposition", "attachment");
      exchange.sendResponseHeaders(200, 0);
      try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("Hello world");
      }
    });

    Page page = browser.newPage(new Browser.NewPageOptions().setAcceptDownloads(true));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Download download = page.waitForDownload(() -> page.click("a"));
    Path nestedPath = tempDir.resolve(Paths.get("these", "are", "directories", "download.txt"));
    download.saveAs(nestedPath);
    assertTrue(Files.exists(nestedPath));
    assertEquals("Hello world", new String(Files.readAllBytes(nestedPath), StandardCharsets.UTF_8));
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> download.path());
    assertTrue(e.getMessage().contains("Path is not available when using browserType.connect(). Use download.saveAs() to save a local copy."));
    page.close();
  }

  @Test
  void shouldErrorWhenSavingDownloadAfterDeletion(@TempDir Path tempDir) {
    server.setRoute("/download", exchange -> {
      exchange.getResponseHeaders().add("Content-Type", "application/octet-stream");
      exchange.getResponseHeaders().add("Content-Disposition", "attachment");
      exchange.sendResponseHeaders(200, 0);
      try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("Hello world");
      }
    });
    Page page = browser.newPage(new Browser.NewPageOptions().setAcceptDownloads(true));
    page.setContent("<a href='" + server.PREFIX + "/download'>download</a>");
    Download download = page.waitForDownload(() -> page.click("a"));
    Path userPath = tempDir.resolve("download.txt");
    download.delete();
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> download.saveAs(userPath));
    assertTrue(e.getMessage().contains("Target page, context or browser has been closed"));
    page.close();
  }

  @Test
  void shouldSupportTracingOverWebSocket(@TempDir Path tempDir) throws IOException {
    List<BrowserContext> contexts = browser.contexts();
    assertEquals(1, contexts.size());
    BrowserContext context = contexts.get(0);

    Page page = context.newPage();
    context.tracing().start(new Tracing.StartOptions().setName("test")
      .setScreenshots(true).setSnapshots(true));
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<button>Click</button>");
    page.click("'Click'");
    page.close();
    Path traceFile = tempDir.resolve("trace.zip");
    context.tracing().stop(new Tracing.StopOptions().setPath(traceFile));

    assertTrue(Files.exists(traceFile));
    assertTrue(Files.size(traceFile) > 0);
  }

  @Test
  void shouldRecordTraceWithSources(@TempDir Path tmpDir) throws IOException {
    Assumptions.assumeTrue(System.getenv("PLAYWRIGHT_JAVA_SRC") != null, "PLAYWRIGHT_JAVA_SRC must point to the directory containing this test source.");
    context.tracing().start(new Tracing.StartOptions().setSources(true));
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<button>Click</button>");
    page.click("'Click'");
    Path trace = tmpDir.resolve("trace1.zip");
    context.tracing().stop(new Tracing.StopOptions().setPath(trace));

    Map<String, byte[]> entries = parseZip(trace);
    Map<String, byte[]> sources = entries.entrySet().stream().filter(e -> e.getKey().endsWith(".txt")).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    assertEquals(1, sources.size());

    String path = getClass().getName().replace('.', File.separatorChar);
    Path sourceFile = Paths.get(System.getenv("PLAYWRIGHT_JAVA_SRC"), path + ".java");
    byte[] thisFile = Files.readAllBytes(sourceFile);
    assertEquals(new String(thisFile, UTF_8), new String(sources.values().iterator().next(), UTF_8));
  }

  @Test
  void shouldFulfillWithGlobalFetchResult() {
    page.route("**/*", route -> {
      APIRequestContext request = playwright.request().newContext();
      APIResponse response = request.get(server.PREFIX + "/simple.json");
      route.fulfill(new Route.FulfillOptions().setResponse(response));
    });
    Response response = page.navigate(server.EMPTY_PAGE);
    assertEquals(200, response.status());
    assertEquals("{\"foo\": \"bar\"}\n", response.text());
  }

  @Test
  void shouldUploadLargeFile(@TempDir Path tmpDir) throws IOException, ExecutionException, InterruptedException {
    Assumptions.assumeTrue(3 <= (Runtime.getRuntime().maxMemory() >> 30), "Fails if max heap size is < 3Gb");
    page.navigate(server.PREFIX + "/input/fileupload.html");
    Path uploadFile = tmpDir.resolve("200MB.zip");
    String str = String.join("", Collections.nCopies(4 * 1024, "A"));

    try (Writer stream = new OutputStreamWriter(Files.newOutputStream(uploadFile))) {
      for (int i = 0; i < 50 * 1024; i++) {
        stream.write(str);
      }
    }
    Locator input = page.locator("input[type='file']");
    JSHandle events = input.evaluateHandle("e => {\n" +
      "    const events = [];\n" +
      "    e.addEventListener('input', () => events.push('input'));\n" +
      "    e.addEventListener('change', () => events.push('change'));\n" +
      "    return events;\n" +
      "  }");
    input.setInputFiles(uploadFile);
    assertEquals("200MB.zip", input.evaluate("e => e.files[0].name"));
    assertEquals(asList("input", "change"), events.evaluate("e => e"));
    CompletableFuture<MultipartFormData> formData = new CompletableFuture<>();
    server.setRoute("/upload", exchange -> {
      try {
        MultipartFormData multipartFormData = MultipartFormData.parseRequest(exchange);
        formData.complete(multipartFormData);
      } catch (Exception e) {
        e.printStackTrace();
        formData.completeExceptionally(e);
      }
      exchange.sendResponseHeaders(200, -1);
    });
    page.click("input[type=submit]", new Page.ClickOptions().setTimeout(90_000));
    List<MultipartFormData.Field> fields = formData.get().fields;
    assertEquals(1, fields.size());
    assertEquals("200MB.zip", fields.get(0).filename);
    assertEquals(200 * 1024 * 1024, fields.get(0).content.length());
  }
}
