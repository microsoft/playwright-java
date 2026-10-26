/**
 * Copyright 2018 Google Inc. All rights reserved.
 * Modifications copyright (c) Microsoft Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright.tests.library.chromium;

import com.google.gson.JsonParser;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.CDPSession;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Tracing;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.CreateUserDataDir;
import com.microsoft.playwright.fixtures.EnabledOnBrowser;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.HttpsTestServer;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.ProxyServer;
import com.microsoft.playwright.fixtures.TestInfo;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.Media;
import org.junit.jupiter.api.DisplayName;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.async;
import static com.microsoft.playwright.fixtures.Utils.asset;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/library/chromium/connect-over-cdp.spec.ts. */
@EnabledOnBrowser(BrowserName.CHROMIUM)
public class TestConnectOverCdp {
  // Java: 9339 + testInfo.workerIndex; the tests run one at a time, the worker index is 0.
  private static final int PORT = 9339;

  private static BrowserType.LaunchOptions remoteDebuggingPort(int port) {
    return Env.launchOptions().setArgs(Arrays.asList("--remote-debugging-port=" + port));
  }

  private static java.util.Map<String, String> headers(String... namesAndValues) {
    java.util.Map<String, String> headers = new java.util.LinkedHashMap<>();
    for (int i = 0; i < namesAndValues.length; i += 2)
      headers.put(namesAndValues[i], namesAndValues[i + 1]);
    return headers;
  }

  // Java: http.get() of the response body.
  private static String httpGet(String url) throws IOException {
    try (InputStream in = new URL(url).openStream()) {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      byte[] buffer = new byte[8192];
      int read;
      while ((read = in.read(buffer)) != -1)
        out.write(buffer, 0, read);
      return new String(out.toByteArray(), StandardCharsets.UTF_8);
    }
  }

  // Java: Promise.all([server.waitFor...(), connectOverCDP(...).catch(() => {})]) connects and ignores the error.
  private static void connectIgnoringErrors(BrowserType browserType, String endpointURL, BrowserType.ConnectOverCDPOptions options) {
    try {
      browserType.connectOverCDP(endpointURL, options);
    } catch (PlaywrightException e) {
    }
  }

  @PlaywrightTest
  @DisplayName("CD-001 should connect to an existing cdp session")
  void shouldConnectToAnExistingCdpSession(BrowserType browserType) {
    int port = PORT;
    Browser browserServer = browserType.launch(remoteDebuggingPort(port));
    try {
      Browser cdpBrowser = browserType.connectOverCDP("http://127.0.0.1:" + port + "/");
      List<BrowserContext> contexts = cdpBrowser.contexts();
      assertEquals(1, contexts.size());
      cdpBrowser.close();
    } finally {
      browserServer.close();
    }
  }

  @PlaywrightTest
  @DisplayName("CD-002 should connect when an existing page has no renderer")
  void shouldConnectWhenAnExistingPageHasNoRenderer(BrowserType browserType, TestServer server) {
    // { annotation: { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/41714' } }
    int port = PORT;
    Browser browserServer = browserType.launch(remoteDebuggingPort(port));
    try {
      // Prepare a page without a renderer, e.g. one discarded by the Memory Saver.
      Browser cdpBrowser1 = browserType.connectOverCDP("http://127.0.0.1:" + port + "/");
      BrowserContext context1 = cdpBrowser1.contexts().get(0);
      Page healthyPage1 = context1.newPage();
      healthyPage1.navigate(server.PREFIX + "/title.html");
      Page crashedPage1 = context1.newPage();
      crashedPage1.navigate(server.EMPTY_PAGE);
      // Java: the crash event is awaited with a future that the handler completes; goto() runs on a thread of its own.
      CompletableFuture<Page> crash = new CompletableFuture<>();
      crashedPage1.onCrash(crash::complete);
      async(() -> {
        try {
          return crashedPage1.navigate("chrome://crash");
        } catch (PlaywrightException e) {
          return null;
        }
      });
      context1.waitForCondition(crash::isDone);
      cdpBrowser1.close();

      // Connecting again should not hang on the page without a renderer,
      // and should not report it at all.
      Browser cdpBrowser2 = browserType.connectOverCDP("http://127.0.0.1:" + port + "/");
      List<Page> pages = cdpBrowser2.contexts().get(0).pages();
      assertEquals(Arrays.asList(server.PREFIX + "/title.html"), pages.stream().map(page -> page.url()).collect(Collectors.toList()));
      assertEquals("Woof-Woof", pages.get(0).title());
      cdpBrowser2.close();
    } finally {
      browserServer.close();
    }
  }

  @PlaywrightTest
  @DisplayName("CD-003 should connect when the opener of an existing page has no renderer")
  void shouldConnectWhenTheOpenerOfAnExistingPageHasNoRenderer(BrowserType browserType, TestServer server) {
    // { annotation: { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/41714' } }
    int port = PORT;
    Browser browserServer = browserType.launch(Env.launchOptions().setArgs(Arrays.asList(
      // The popup should be in a different process to survive the crash of the opener.
      "--remote-debugging-port=" + port, "--site-per-process")));
    try {
      Browser cdpBrowser1 = browserType.connectOverCDP("http://127.0.0.1:" + port + "/");
      Page crashedPage1 = cdpBrowser1.contexts().get(0).newPage();
      crashedPage1.navigate(server.EMPTY_PAGE);
      Page popup1 = crashedPage1.waitForPopup(() -> crashedPage1.evaluate("url => { window.open(url); }", server.CROSS_PROCESS_PREFIX + "/title.html"));
      popup1.waitForLoadState();
      // Java: the crash event is awaited with a future that the handler completes; goto() runs on a thread of its own.
      CompletableFuture<Page> crash = new CompletableFuture<>();
      crashedPage1.onCrash(crash::complete);
      async(() -> {
        try {
          return crashedPage1.navigate("chrome://crash");
        } catch (PlaywrightException e) {
          return null;
        }
      });
      cdpBrowser1.contexts().get(0).waitForCondition(crash::isDone);
      cdpBrowser1.close();

      // Connecting again should not report the opener, and should not wait for it.
      Browser cdpBrowser2 = browserType.connectOverCDP("http://127.0.0.1:" + port + "/");
      List<Page> pages = cdpBrowser2.contexts().get(0).pages();
      assertEquals(Arrays.asList(server.CROSS_PROCESS_PREFIX + "/title.html"), pages.stream().map(page -> page.url()).collect(Collectors.toList()));
      assertEquals("Woof-Woof", pages.get(0).title());
      assertNull(pages.get(0).opener());
      cdpBrowser2.close();
    } finally {
      browserServer.close();
    }
  }

  @PlaywrightTest
  @DisplayName("CD-004 should connect when an existing page has been discarded")
  void shouldConnectWhenAnExistingPageHasBeenDiscarded(BrowserType browserType, CreateUserDataDir createUserDataDir, TestServer server, TestInfo testInfo) throws Exception {
    // { annotation: { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/41714' } }
    int port = PORT;
    Path userDataDir = createUserDataDir.create();
    // chrome://discards is an internal debug page that is only available with this pref.
    Files.write(userDataDir.resolve("Local State"), "{\"internal_only_uis_enabled\":true}".getBytes(StandardCharsets.UTF_8));
    BrowserContext context = browserType.launchPersistentContext(userDataDir, Env.launchPersistentContextOptions()
      .setHeadless(false)
      // Emulating the viewport of a discarded tab crashes the browser in
      // WebContentsImpl::SetDeviceEmulationSize, because it has no view.
      .setViewportSize((com.microsoft.playwright.options.ViewportSize) null)
      .setArgs(Arrays.asList(
        "--remote-debugging-port=" + port,
        // Chrome refuses to discard tabs with DevTools attached, and we attach to all of them.
        "--enable-features=AllowDevtoolsConnectedDiscard")));
    try {
      String victimUrl = server.PREFIX + "/title.html";
      Page victim = context.newPage();
      victim.navigate(victimUrl);
      Page discards = context.newPage();
      discards.navigate("chrome://discards/");
      // Discarding replaces the tab's WebContents, but the url in the table survives.
      Locator row = discards.getByRole(AriaRole.ROW, new Page.GetByRoleOptions().setName(victimUrl));
      row.getByText("Urgent Discard").click();
      assertThat(row).containsText("discarded");
      // The renderer process is shut down asynchronously after the discard.
      discards.waitForTimeout(3000);
      // await testInfo.attach('discards rows', { body: (await discards.getByRole('row').allInnerTexts()).map(text => text.replace(/\s+/g, ' ')).join('\n') });
      // const targets = await (await fetch(`http://127.0.0.1:${port}/json/list`)).json();
      // await testInfo.attach('targets', { body: JSON.stringify(targets, null, 2), contentType: 'application/json' });

      // Connecting should not hang on the discarded page, and the page should not be reported.
      Browser cdpBrowser = browserType.connectOverCDP("http://127.0.0.1:" + port + "/");
      List<Page> pages = cdpBrowser.contexts().get(0).pages();
      List<String> expected = new ArrayList<>(Arrays.asList("about:blank", discards.url()));
      Collections.sort(expected);
      List<String> actual = pages.stream().map(page -> page.url()).sorted().collect(Collectors.toList());
      assertEquals(expected, actual);
      cdpBrowser.close();
    } finally {
      context.close();
    }
  }

  @PlaywrightTest
  @DisplayName("CD-008 should connectOverCDP and manage downloads in default context")
  void shouldConnectOverCDPAndManageDownloadsInDefaultContext(BrowserType browserType, TestServer server, TestInfo testInfo) throws Exception {
    server.setRoute("/downloadWithFilename", (req, res) -> {
      res.setHeader("Content-Type", "application/octet-stream");
      res.setHeader("Content-Disposition", "attachment; filename=file.txt");
      res.end("Hello world");
    });

    int port = PORT;
    Browser browserServer = browserType.launch(remoteDebuggingPort(port));

    try {
      Browser browser = browserType.connectOverCDP("http://127.0.0.1:" + port + "/");
      Page page = browser.contexts().get(0).newPage();
      page.setContent("<a href=\"" + server.PREFIX + "/downloadWithFilename\">download</a>");

      Download download = page.waitForDownload(() -> page.click("a"));
      assertSame(page, download.page());
      assertEquals(server.PREFIX + "/downloadWithFilename", download.url());
      assertEquals("file.txt", download.suggestedFilename());

      Path userPath = testInfo.outputPath("download.txt");
      download.saveAs(userPath);
      assertTrue(Files.exists(userPath));
      assertEquals("Hello world", new String(Files.readAllBytes(userPath), StandardCharsets.UTF_8));
    } finally {
      browserServer.close();
    }
  }

  @PlaywrightTest
  @DisplayName("CD-009 should connect to an existing cdp session twice")
  void shouldConnectToAnExistingCdpSessionTwice(BrowserType browserType, TestServer server) {
    int port = PORT;
    Browser browserServer = browserType.launch(remoteDebuggingPort(port));
    try {
      Browser cdpBrowser1 = browserType.connectOverCDP("http://127.0.0.1:" + port + "/");
      Browser cdpBrowser2 = browserType.connectOverCDP("http://127.0.0.1:" + port + "/");
      List<BrowserContext> contexts1 = cdpBrowser1.contexts();
      assertEquals(1, contexts1.size());
      BrowserContext context1 = contexts1.get(0);
      Page page1 = context1.newPage();
      page1.navigate(server.EMPTY_PAGE);

      List<BrowserContext> contexts2 = cdpBrowser2.contexts();
      assertEquals(1, contexts2.size());
      BrowserContext context2 = contexts2.get(0);
      Page page2 = context2.newPage();
      page2.navigate(server.EMPTY_PAGE);

      assertEquals(2, context1.pages().size());
      assertEquals(2, context2.pages().size());

      page1.evaluate("() => alert('dialog race')");
      assertEquals("", context2.pages().get(0).title());

      cdpBrowser1.close();
      cdpBrowser2.close();
    } finally {
      browserServer.close();
    }
  }

  @PlaywrightTest
  @DisplayName("CD-010 should connect to existing page with iframe and navigate")
  void shouldConnectToExistingPageWithIframeAndNavigate(BrowserType browserType, TestServer server) {
    int port = PORT;
    Browser browserServer = browserType.launch(remoteDebuggingPort(port));
    try {
      {
        BrowserContext context1 = browserServer.newContext();
        Page page = context1.newPage();
        page.navigate(server.PREFIX + "/frames/one-frame.html");
      }
      Browser cdpBrowser = browserType.connectOverCDP("http://127.0.0.1:" + port + "/");
      List<BrowserContext> contexts = cdpBrowser.contexts();
      assertEquals(1, contexts.size());
      contexts.get(0).pages().get(0).navigate(server.EMPTY_PAGE);
      cdpBrowser.close();
    } finally {
      browserServer.close();
    }
  }

  @PlaywrightTest
  @DisplayName("CD-012 should connect over a ws endpoint")
  void shouldConnectOverAWsEndpoint(BrowserType browserType, TestServer server) throws Exception {
    int port = PORT;
    Browser browserServer = browserType.launch(remoteDebuggingPort(port));
    try {
      String json = httpGet("http://127.0.0.1:" + port + "/json/version/");
      Browser cdpBrowser = browserType.connectOverCDP(JsonParser.parseString(json).getAsJsonObject().get("webSocketDebuggerUrl").getAsString());
      List<BrowserContext> contexts = cdpBrowser.contexts();
      assertEquals(1, contexts.size());
      cdpBrowser.close();

      // also connect with the deprecated wsEndpoint option
      // Java: there is no wsEndpoint option, the web socket URL is the endpointURL.
      Browser cdpBrowser2 = browserType.connectOverCDP(JsonParser.parseString(json).getAsJsonObject().get("webSocketDebuggerUrl").getAsString());
      List<BrowserContext> contexts2 = cdpBrowser2.contexts();
      assertEquals(1, contexts2.size());
      cdpBrowser2.close();
    } finally {
      browserServer.close();
    }
  }

  @PlaywrightTest
  @DisplayName("CD-013 should send extra headers with connect request")
  void shouldSendExtraHeadersWithConnectRequest(BrowserType browserType, TestServer server) throws Exception {
    {
      CompletableFuture<TestServer.Request> requestPromise = server.waitForWebSocketConnectionRequest();
      // Java: there is no wsEndpoint option, the web socket URL is the endpointURL.
      connectIgnoringErrors(browserType, "ws://localhost:" + server.PORT + "/ws", new BrowserType.ConnectOverCDPOptions()
        .setHeaders(headers("User-Agent", "Playwright", "foo", "bar"))
        .setTimeout(100));
      TestServer.Request request = requestPromise.get();
      assertEquals("Playwright", request.headers.get("user-agent"));
      assertEquals("bar", request.headers.get("foo"));
    }
    {
      CompletableFuture<TestServer.Request> requestPromise = server.waitForRequest("/json/version/");
      connectIgnoringErrors(browserType, "http://localhost:" + server.PORT, new BrowserType.ConnectOverCDPOptions()
        .setHeaders(headers("User-Agent", "Playwright", "foo", "bar"))
        .setTimeout(100));
      TestServer.Request request = requestPromise.get();
      assertEquals("Playwright", request.headers.get("user-agent"));
      assertEquals("bar", request.headers.get("foo"));
    }
  }

  @PlaywrightTest
  @DisplayName("CD-014 should keep URL parameters when adding json/version")
  void shouldKeepURLParametersWhenAddingJsonVersion(BrowserType browserType, TestServer server) throws Exception {
    // { annotation: { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/36097' } }
    CompletableFuture<TestServer.Request> requestPromise = server.waitForRequest("/browser/json/version/?foo=bar");
    connectIgnoringErrors(browserType, "http://localhost:" + server.PORT + "/browser/?foo=bar", null);
    requestPromise.get();
  }

  @PlaywrightTest
  @DisplayName("CD-015 should append /json/version with a slash if there isnt one")
  void shouldAppendJsonVersionWithASlashIfThereIsntOne(BrowserType browserType, TestServer server) throws Exception {
    // { annotation: { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/36378' } }
    CompletableFuture<TestServer.Request> requestPromise = server.waitForRequest("/browser/json/version/?foo=bar");
    connectIgnoringErrors(browserType, "http://localhost:" + server.PORT + "/browser?foo=bar", null);
    requestPromise.get();
  }

  @PlaywrightTest
  @DisplayName("CD-017 should report all pages in an existing browser")
  void shouldReportAllPagesInAnExistingBrowser(BrowserType browserType) {
    int port = PORT;
    Browser browserServer = browserType.launch(remoteDebuggingPort(port));
    try {
      Browser cdpBrowser = browserType.connectOverCDP("http://127.0.0.1:" + port + "/");
      List<BrowserContext> contexts = cdpBrowser.contexts();
      assertEquals(1, contexts.size());
      for (int i = 0; i < 3; i++)
        contexts.get(0).newPage();
      cdpBrowser.close();

      Browser cdpBrowser2 = browserType.connectOverCDP("http://127.0.0.1:" + port + "/");
      assertEquals(3, cdpBrowser2.contexts().get(0).pages().size());

      cdpBrowser2.close();
    } finally {
      browserServer.close();
    }
  }

  @PlaywrightTest
  @DisplayName("CD-018 should connect via https")
  @Fixme("java: Playwright.create() ignores the env option after the first driver of the JVM (Driver.ensureDriverInstalled() is a singleton), NODE_TLS_REJECT_UNAUTHORIZED does not reach the driver: 'self-signed certificate; if the root CA is installed locally, try running Node.js with --use-system-ca'")
  void shouldConnectViaHttps(HttpsTestServer httpsServer) throws Exception {
    int port = PORT;
    // Java: the driver reads NODE_TLS_REJECT_UNAUTHORIZED when it starts, the Playwright instance of this test gets it in its environment.
    try (Playwright playwright = Playwright.create(new Playwright.CreateOptions().setEnv(Collections.singletonMap("NODE_TLS_REJECT_UNAUTHORIZED", "0")))) {
      BrowserType browserType = playwright.chromium();
      Browser browserServer = browserType.launch(remoteDebuggingPort(port));
      String json = httpGet("http://127.0.0.1:" + port + "/json/version/");
      httpsServer.setRoute("/json/version/", (req, res) -> {
        res.writeHead(200);
        res.end(json);
      });
      // const oldValue = process.env['NODE_TLS_REJECT_UNAUTHORIZED'];
      // https://stackoverflow.com/a/21961005/552185
      // process.env['NODE_TLS_REJECT_UNAUTHORIZED'] = '0';
      // suppressCertificateWarning();
      try {
        Browser cdpBrowser = browserType.connectOverCDP("https://localhost:" + httpsServer.PORT + "/");
        List<BrowserContext> contexts = cdpBrowser.contexts();
        assertEquals(1, contexts.size());
        for (int i = 0; i < 3; i++)
          contexts.get(0).newPage();
        cdpBrowser.close();
      } finally {
        browserServer.close();
      }
    }
  }

  @PlaywrightTest
  @DisplayName("CD-019 should return valid browser from context.browser()")
  void shouldReturnValidBrowserFromContextBrowser(BrowserType browserType) {
    int port = PORT;
    Browser browserServer = browserType.launch(remoteDebuggingPort(port));
    try {
      Browser cdpBrowser = browserType.connectOverCDP("http://127.0.0.1:" + port + "/");
      List<BrowserContext> contexts = cdpBrowser.contexts();
      assertEquals(1, contexts.size());
      assertSame(cdpBrowser, contexts.get(0).browser());

      BrowserContext context2 = cdpBrowser.newContext();
      assertSame(cdpBrowser, context2.browser());

      cdpBrowser.close();
    } finally {
      browserServer.close();
    }
  }

  @PlaywrightTest
  @DisplayName("CD-020 should report an expected error when the endpointURL returns a non-expected status code")
  @Fixme("java: the message is 'Unexpected status 404 when connecting to <url>/json/version/.' without the 'browserType.connectOverCDP: ' prefix")
  void shouldReportAnExpectedErrorWhenTheEndpointURLReturnsANonExpectedStatusCode(BrowserType browserType, TestServer server) {
    server.setRoute("/json/version/", (req, resp) -> {
      resp.statusCode = 404;
      resp.end("{\"webSocketDebuggerUrl\":\"dont-use-me\"}");
    });
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> browserType.connectOverCDP(server.PREFIX));
    assertTrue(error.getMessage().contains("browserType.connectOverCDP: Unexpected status 404 when connecting to " + server.PREFIX + "/json/version/"));
  }

  @PlaywrightTest
  @DisplayName("CD-021 should report an expected error when the endpoint URL JSON webSocketDebuggerUrl is undefined")
  @Fixme("java: the message is 'Invalid URL: undefined' (SyntaxError) without the 'browserType.connectOverCDP: ' prefix")
  void shouldReportAnExpectedErrorWhenTheEndpointURLJSONWebSocketDebuggerUrlIsUndefined(BrowserType browserType, TestServer server) {
    server.setRoute("/json/version/", (req, resp) -> {
      resp.end("{}");
    });
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> browserType.connectOverCDP(server.PREFIX));
    assertTrue(error.getMessage().contains("browserType.connectOverCDP: Invalid URL"));
  }

  @PlaywrightTest
  @DisplayName("CD-022 should connect to an existing cdp session when passed as a first argument")
  void shouldConnectToAnExistingCdpSessionWhenPassedAsAFirstArgument(BrowserType browserType) {
    int port = PORT;
    Browser browserServer = browserType.launch(remoteDebuggingPort(port));
    try {
      Browser cdpBrowser = browserType.connectOverCDP("http://127.0.0.1:" + port + "/");
      List<BrowserContext> contexts = cdpBrowser.contexts();
      assertEquals(1, contexts.size());
      cdpBrowser.close();
    } finally {
      browserServer.close();
    }
  }

  @PlaywrightTest
  @DisplayName("CD-023 should use proxy with connectOverCDP")
  void shouldUseProxyWithConnectOverCDP(BrowserType browserType, TestServer server) {
    server.setRoute("/target.html", (req, res) -> {
      res.end("<html><title>Served by the proxy</title></html>");
    });
    int port = PORT;
    Browser browserServer = browserType.launch(remoteDebuggingPort(port));
    try {
      Browser cdpBrowser = browserType.connectOverCDP("http://127.0.0.1:" + port + "/");
      BrowserContext context = cdpBrowser.newContext(new Browser.NewContextOptions()
        .setProxy("localhost:" + server.PORT));
      Page page = context.newPage();
      page.navigate("http://non-existent.test/target.html");
      assertEquals("Served by the proxy", page.title());
      cdpBrowser.close();
    } finally {
      browserServer.close();
    }
  }

  @PlaywrightTest
  @DisplayName("CD-024 should use env proxy with connectOverCDP discovery request")
  @Fixme("java: Playwright.create() ignores the env option after the first driver of the JVM (Driver.ensureDriverInstalled() is a singleton), HTTP_PROXY does not reach the driver: proxyServer.requestUrls is [] instead of [<url>/json/version/]")
  void shouldUseEnvProxyWithConnectOverCDPDiscoveryRequest(TestServer server, ProxyServer proxyServer) {
    // test.info().annotations.push({ type: 'issue', description: 'https://github.com/microsoft/playwright/issues/40394' });
    proxyServer.forwardTo(server.PORT);

    // Java: the driver reads HTTP_PROXY when it starts, the Playwright instance of this test gets it in its environment.
    try (Playwright playwright = Playwright.create(new Playwright.CreateOptions().setEnv(Collections.singletonMap("HTTP_PROXY", proxyServer.URL)))) {
      BrowserType browserType = playwright.chromium();
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> browserType.connectOverCDP(server.PREFIX));
      assertTrue(error.getMessage().contains("Unexpected status 404 when connecting to " + server.PREFIX + "/json/version/"));
      assertEquals(Arrays.asList(server.PREFIX + "/json/version/"), proxyServer.requestUrls);
    }
  }

  @PlaywrightTest
  @DisplayName("CD-025 should send target Host header when using env HTTP proxy with connectOverCDP")
  @Fixme("java: Playwright.create() ignores the env option after the first driver of the JVM (Driver.ensureDriverInstalled() is a singleton), HTTP_PROXY does not reach the driver: proxyServer.requestHosts is [] instead of [localhost:<port>]")
  void shouldSendTargetHostHeaderWhenUsingEnvHTTPProxyWithConnectOverCDP(TestServer server, ProxyServer proxyServer) {
    // test.info().annotations.push({ type: 'issue', description: 'https://github.com/microsoft/playwright/issues/40811' });
    proxyServer.forwardTo(server.PORT);

    // Java: the driver reads HTTP_PROXY when it starts, the Playwright instance of this test gets it in its environment.
    try (Playwright playwright = Playwright.create(new Playwright.CreateOptions().setEnv(Collections.singletonMap("HTTP_PROXY", proxyServer.URL)))) {
      BrowserType browserType = playwright.chromium();
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> browserType.connectOverCDP(server.PREFIX));
      assertTrue(error.getMessage().contains("Unexpected status 404 when connecting to " + server.PREFIX + "/json/version/"));
      assertEquals(Arrays.asList(URI.create(server.PREFIX).getAuthority()), proxyServer.requestHosts);
    }
  }

  @PlaywrightTest
  @DisplayName("CD-026 should be able to connect via localhost")
  void shouldBeAbleToConnectViaLocalhost(BrowserType browserType) {
    int port = PORT;
    Browser browserServer = browserType.launch(remoteDebuggingPort(port));
    try {
      Browser cdpBrowser = browserType.connectOverCDP("http://localhost:" + port);
      List<BrowserContext> contexts = cdpBrowser.contexts();
      assertEquals(1, contexts.size());
      cdpBrowser.close();
    } finally {
      browserServer.close();
    }
  }

  @PlaywrightTest
  @DisplayName("CD-027 emulate media should not be affected by second connectOverCDP with noDefaults")
  void emulateMediaShouldNotBeAffectedBySecondConnectOverCDPWithNoDefaults(BrowserType browserType) {
    // test.info().annotations.push({ type: 'issue', description: 'https://github.com/microsoft/playwright/issues/24109' });
    int port = PORT;
    Browser browserServer = browserType.launch(remoteDebuggingPort(port));
    try {
      java.util.function.Function<Page, Object> isPrint = page -> page.evaluate("() => matchMedia('print').matches");

      Browser browser1 = browserType.connectOverCDP("http://localhost:" + port);
      BrowserContext context1 = browser1.newContext();
      Page page1 = context1.newPage();
      page1.emulateMedia(new Page.EmulateMediaOptions().setMedia(Media.PRINT));
      assertEquals(true, isPrint.apply(page1));
      Browser browser2 = browserType.connectOverCDP("http://localhost:" + port, new BrowserType.ConnectOverCDPOptions().setNoDefaults(true));
      assertEquals(true, isPrint.apply(page1));
      // Java: the Promise.all calls run one after the other.
      browser1.close();
      browser2.close();
    } finally {
      browserServer.close();
    }
  }

  @PlaywrightTest
  @DisplayName("CD-028 should allow tracing over cdp session")
  void shouldAllowTracingOverCdpSession(BrowserType browserType, TestInfo testInfo) {
    int port = PORT;
    Browser browserServer = browserType.launch(remoteDebuggingPort(port));
    try {
      Browser cdpBrowser = browserType.connectOverCDP("http://127.0.0.1:" + port + "/");
      BrowserContext context = cdpBrowser.contexts().get(0);
      context.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true));
      Page page = context.newPage();
      page.evaluate("() => 2 + 2");
      Path traceZip = testInfo.outputPath("trace.zip");
      context.tracing().stop(new Tracing.StopOptions().setPath(traceZip));
      cdpBrowser.close();
      assertTrue(Files.exists(traceZip));
    } finally {
      browserServer.close();
    }
  }

  @PlaywrightTest
  @DisplayName("CD-029 setInputFiles should preserve lastModified timestamp")
  void setInputFilesShouldPreserveLastModifiedTimestamp(BrowserType browserType) throws Exception {
    // test.info().annotations.push({ type: 'issue', description: 'https://github.com/microsoft/playwright/issues/27452' });

    int port = PORT;
    Browser browserServer = browserType.launch(remoteDebuggingPort(port));
    try {
      Browser cdpBrowser = browserType.connectOverCDP("http://127.0.0.1:" + port + "/");
      BrowserContext context = cdpBrowser.contexts().get(0);
      Page page = context.newPage();
      page.setContent("<input type=file multiple=true/>");
      Locator input = page.locator("input");
      List<String> files = Arrays.asList("file-to-upload.txt", "file-to-upload-2.txt");
      input.setInputFiles(files.stream().map(f -> asset(f)).toArray(Path[]::new));
      assertEquals(files, input.evaluate("e => [...e.files].map(f => f.name)"));
      List<Object> timestamps = (List<Object>) input.evaluate("e => [...e.files].map(f => f.lastModified)");
      List<Long> expectedTimestamps = new ArrayList<>();
      for (String file : files)
        expectedTimestamps.add(Math.round(Files.getLastModifiedTime(asset(file)).to(java.util.concurrent.TimeUnit.MICROSECONDS) / 1000.0));
      // On Linux browser sometimes reduces the timestamp by 1ms: 1696272058110.0715  -> 1696272058109 or even
      // rounds it to seconds in WebKit: 1696272058110 -> 1696272058000.
      for (int i = 0; i < timestamps.size(); i++)
        assertTrue(Math.abs(((Number) timestamps.get(i)).doubleValue() - expectedTimestamps.get(i)) <= 1000, "expected: " + expectedTimestamps + "; actual: " + timestamps);
      cdpBrowser.close();
    } finally {
      browserServer.close();
    }
  }

  @PlaywrightTest
  @DisplayName("CD-031 should print custom ws close error")
  void shouldPrintCustomWsCloseError(BrowserType browserType, TestServer server) {
    server.onceWebSocketConnection(ws -> {
      ws.onMessage(message -> {
        ws.close(4123, "Oh my!");
      });
    });
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> browserType.connectOverCDP("ws://localhost:" + server.PORT + "/ws"));
    assertTrue(error.getMessage().contains("Browser logs:\n\nOh my!\n"));
  }

  @PlaywrightTest
  @DisplayName("CD-033 should get title and URL of existing page")
  void shouldGetTitleAndURLOfExistingPage(BrowserType browserType, TestServer server) {
    int port = PORT;
    Browser browserServer = browserType.launch(remoteDebuggingPort(port));
    List<Browser> browsers = new ArrayList<>();
    try {
      {
        Browser cdpBrowser = browserType.connectOverCDP("http://127.0.0.1:" + port + "/");
        browsers.add(cdpBrowser);
        BrowserContext context = cdpBrowser.contexts().get(0);
        Page page = context.newPage();
        page.navigate(server.EMPTY_PAGE);
        page.evaluate("() => document.title = 'my title'");
      }

      {
        Browser cdpBrowser = browserType.connectOverCDP("http://127.0.0.1:" + port + "/");
        browsers.add(cdpBrowser);

        BrowserContext context = cdpBrowser.contexts().get(0);
        Page page = context.pages().get(0);
        assertEquals(server.EMPTY_PAGE, page.url());
        assertEquals("my title", page.title());
      }

    } finally {
      for (Browser browser : browsers)
        browser.close();
      browserServer.close();
    }
  }

  @PlaywrightTest
  @DisplayName("CD-034 should skip default overrides with noDefaults")
  void shouldSkipDefaultOverridesWithNoDefaults(BrowserType browserType, TestServer server) {
    // test.info().annotations.push({ type: 'issue', description: 'https://github.com/microsoft/playwright/issues/40158' });
    int port = PORT;
    Browser browserServer = browserType.launch(remoteDebuggingPort(port));
    try {
      Browser browser = browserType.connectOverCDP("http://127.0.0.1:" + port + "/", new BrowserType.ConnectOverCDPOptions()
        .setNoDefaults(true));
      BrowserContext defaultContext = browser.contexts().get(0);
      Page page = defaultContext.newPage();

      // Browser.setDownloadBehavior was not sent, so no Playwright download event fires.
      server.setRoute("/download", (req, res) -> {
        res.setHeader("Content-Type", "application/octet-stream");
        res.setHeader("Content-Disposition", "attachment; filename=file.txt");
        res.end("Hello world");
      });
      page.setContent("<a href=\"" + server.PREFIX + "/download\">download</a>");
      boolean[] sawDownload = {false};
      page.onDownload(download -> { sawDownload[0] = true; });
      page.click("a");
      page.waitForTimeout(500);
      assertFalse(sawDownload[0]);

      browser.close();
    } finally {
      browserServer.close();
    }
  }

  @PlaywrightTest
  @DisplayName("CD-035 noDefaults should not affect new contexts")
  void noDefaultsShouldNotAffectNewContexts(BrowserType browserType, TestServer server) {
    // test.info().annotations.push({ type: 'issue', description: 'https://github.com/microsoft/playwright/issues/40158' });
    int port = PORT;
    Browser browserServer = browserType.launch(remoteDebuggingPort(port));
    try {
      Browser browser = browserType.connectOverCDP("http://127.0.0.1:" + port + "/", new BrowserType.ConnectOverCDPOptions()
        .setNoDefaults(true));

      // New contexts should still get normal Playwright defaults.
      BrowserContext newContext = browser.newContext();
      Page page = newContext.newPage();
      Object hasFocus = page.evaluate("() => document.hasFocus()");
      assertEquals(true, hasFocus);

      newContext.close();
      browser.close();
    } finally {
      browserServer.close();
    }
  }
}
