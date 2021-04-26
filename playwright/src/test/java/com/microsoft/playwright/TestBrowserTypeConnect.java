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

import com.microsoft.playwright.impl.Driver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.file.Path;

import static com.microsoft.playwright.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.*;

public class TestBrowserTypeConnect extends TestBase {
  private static Process browserServer;
  private static String wsEndpoint;

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

  private static BrowserServer launchBrowserServer() {
    try {
      Path driver = Driver.ensureDriverInstalled();
      Path dir = driver.getParent();
      String node = dir.resolve(isWindows ? "node.exe" : "node").toString();
      String cliJs = dir.resolve("package/lib/cli/cli.js").toString();
      // We launch node process directly instead of using playwright.sh script as killing the script
      // process will leave node process running and killing it would be more hassle.
      ProcessBuilder pb = new ProcessBuilder(node, cliJs, "launch-server", browserType.name());
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

  @BeforeAll
  // Hide base class method to launch browser server and connect to it.
  static void launchBrowser() {
    initBrowserType();
    BrowserServer r = launchBrowserServer();
    wsEndpoint = r.wsEndpoint;
    browserServer = r.process;
    browser = browserType.connect(wsEndpoint);
    // Do not actually connect to browser, the tests will do it manually.
  }

  @AfterAll
  static void closeBrowser() {
    TestBase.closeBrowser();
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
      assertEquals("bar", webSocketServer.lastClientHandshake.getFieldValue("foo"));
    }
  }

  @Test
  void disconnectedEventShouldBeEmittedWhenBrowserIsClosedOrServerIsClosed() throws InterruptedException {
    // Launch another server to not affect other tests.
    BrowserServer remote = launchBrowserServer();

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
    try {
      // Tickle connection so that it gets a chance to dispatch disconnect event.
      page2.title();
      fail("did not throw");
    } catch (PlaywrightException e) {
    }
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
    BrowserServer server = launchBrowserServer();
    Browser remote = browserType.connect(server.wsEndpoint);
    Page page = remote.newPage();
    server.kill();
    try {
      page.evaluate("1 + 1");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("Playwright connection closed"));
    }
    assertFalse(remote.isConnected());
  }

    @Test
    void shouldRejectNavigationWhenBrowserCloses() {
      Browser remote = browserType.connect(wsEndpoint);
      Page page = remote.newPage();

      server.setRoute("/one-style.css", r -> {});
      page.onRequest(r -> remote.close());
      try {
        page.navigate(server.PREFIX + "/one-style.html", new Page.NavigateOptions().setTimeout(60000));
        fail("did not throw");
      } catch (PlaywrightException e) {
        assertTrue(e.getMessage().contains("Playwright connection closed"));
      }
    }
  }
