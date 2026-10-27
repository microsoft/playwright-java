/**
 * Copyright 2017 Google Inc. All rights reserved.
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

package com.microsoft.playwright.tests.library;

import com.google.gson.Gson;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Download;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.ChildProcessFactory;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixture;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.HttpsTestServer;
import com.microsoft.playwright.fixtures.PlaywrightServer;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.RemoteServer;
import com.microsoft.playwright.fixtures.RemoteServerOptions;
import com.microsoft.playwright.fixtures.RunServer;
import com.microsoft.playwright.fixtures.StartRemoteServer;
import com.microsoft.playwright.fixtures.TestInfo;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.impl.PlaywrightImpl;
import com.microsoft.playwright.impl.TargetClosedError;
import com.sun.net.httpserver.HttpServer;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static com.microsoft.playwright.fixtures.Utils.asset;
import static com.microsoft.playwright.fixtures.Utils.kTargetClosedErrorMessage;
import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static com.microsoft.playwright.fixtures.Utils.rafraf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Port of tests/library/browsertype-connect.spec.ts. */
public class TestBrowsertypeConnect {
  /** {@code ConnectTestOptions}: what the {@code connect} fixture takes besides the connect options. */
  static final class ConnectTestOptions {
    Integer redirectPortForTest;
    Map<String, Object> launchOptions;

    ConnectTestOptions setRedirectPortForTest(Integer redirectPortForTest) {
      this.redirectPortForTest = redirectPortForTest;
      return this;
    }

    ConnectTestOptions setLaunchOptions(Map<String, Object> launchOptions) {
      this.launchOptions = launchOptions;
      return this;
    }
  }

  /** The connect options with the {@code __testHookRedirectPortForwarding} hook, which the driver reads as {@code socksProxyRedirectPortForTest}. */
  static final class ConnectOptionsWithHook extends BrowserType.ConnectOptions {
    public Integer socksProxyRedirectPortForTest;
  }

  /** The {@code connect} fixture. */
  public static final class Connect implements AutoCloseable {
    private final BrowserType browserType;
    private final BrowserType.LaunchOptions defaultLaunchOptions;
    private Browser browser;

    Connect(BrowserType browserType, BrowserType.LaunchOptions defaultLaunchOptions) {
      this.browserType = browserType;
      this.defaultLaunchOptions = defaultLaunchOptions;
    }

    Browser connect(String wsEndpoint) {
      return connect(wsEndpoint, new BrowserType.ConnectOptions(), new ConnectTestOptions());
    }

    Browser connect(String wsEndpoint, BrowserType.ConnectOptions options) {
      return connect(wsEndpoint, options, new ConnectTestOptions());
    }

    Browser connect(String wsEndpoint, BrowserType.ConnectOptions options, ConnectTestOptions testOptions) {
      ConnectOptionsWithHook hooked = new ConnectOptionsWithHook();
      hooked.headers = options.headers;
      hooked.slowMo = options.slowMo;
      hooked.timeout = options.timeout;
      hooked.exposeNetwork = options.exposeNetwork;
      hooked.socksProxyRedirectPortForTest = testOptions.redirectPortForTest;
      Map<String, Object> launchOptions = RemoteServer.defaultLaunchOptions(defaultLaunchOptions);
      if (defaultLaunchOptions.tracesDir != null) {
        launchOptions.put("tracesDir", defaultLaunchOptions.tracesDir.toString());
      }
      if (testOptions.launchOptions != null) {
        launchOptions.putAll(testOptions.launchOptions);
      }
      Map<String, String> headers = new LinkedHashMap<>();
      headers.put("x-playwright-launch-options", new Gson().toJson(launchOptions));
      if (hooked.headers != null) {
        headers.putAll(hooked.headers);
      }
      hooked.headers = headers;
      browser = browserType.connect(wsEndpoint, hooked);
      return browser;
    }

    @Override
    public void close() {
      if (browser != null) {
        try {
          browser.close();
        } catch (TargetClosedError e) {
          // Java: closing a browser whose server is gone, before the client noticed, throws; upstream does not.
        }
      }
    }
  }

  /** A server that answers every request with a fixed page, the {@code createHttpServer} of the fixtures below. */
  static class FixedPageServer implements AutoCloseable {
    final int port;
    private final HttpServer server;

    FixedPageServer(String host, String body) {
      try {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getByName(host), 0), 0);
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
      server.createContext("/", exchange -> {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
          out.write(bytes);
        }
      });
      server.start();
      port = server.getAddress().getPort();
    }

    @Override
    public void close() {
      server.stop(0);
    }
  }

  /** {@code dummyServerPort}. */
  public static final class DummyServerPort extends FixedPageServer {
    DummyServerPort() {
      // Only listen on IPv4 to check that we don't try to connect to it via IPv6.
      super("127.0.0.1", "<html><body>from-dummy-server</body></html>");
    }
  }

  /** {@code ipV6ServerPort}. */
  public static final class IpV6ServerPort extends FixedPageServer {
    IpV6ServerPort() {
      super("::1", "<html><body>from-ipv6-server</body></html>");
    }
  }

  @Fixture
  Connect connect(BrowserType browserType, BrowserType.LaunchOptions launchOptions) {
    return new Connect(browserType, launchOptions);
  }

  @Fixture
  DummyServerPort dummyServerPort() {
    return new DummyServerPort();
  }

  @Fixture
  IpV6ServerPort ipV6ServerPort() {
    assumeFalse(System.getenv("INSIDE_DOCKER") != null && !System.getenv("INSIDE_DOCKER").isEmpty(), "docker does not support IPv6 by default");
    return new IpV6ServerPort();
  }

  @Nested
  @ParameterizedClass(name = "{0}")
  @ValueSource(strings = {"launchServer", "run-server"})
  class Kind {
    @Parameter
    String kind;

    @PlaywrightTest
    @DisplayName("BT-034 should connect over wss")
    void shouldConnectOverWss(Connect connect, StartRemoteServer startRemoteServer, HttpsTestServer httpsServer, BrowserName browserName, BrowserType.LaunchOptions launchOptions) throws Exception {
      PlaywrightServer remoteServer = startRemoteServer.start(kind);

      // Java: the driver reads NODE_TLS_REJECT_UNAUTHORIZED when it starts, so the test connects with a driver of its own.
      Map<String, String> env = new HashMap<>();
      env.put("PWTEST_UNDER_TEST", "1");
      // https://stackoverflow.com/a/21961005/552185
      env.put("NODE_TLS_REJECT_UNAUTHORIZED", "0");
      // suppressCertificateWarning();
      env.put("NODE_NO_WARNINGS", "1");
      Playwright playwright = PlaywrightImpl.createImpl(new Playwright.CreateOptions().setEnv(env), true);
      Connect tlsConnect = new Connect(browserName.browserType(playwright), launchOptions);
      try {
        httpsServer.onceWebSocketConnection((ws, request) -> {
          Map<String, String> headers = new HashMap<>();
          for (Map.Entry<String, String> entry : request.headers.entrySet()) {
            if (entry.getKey().startsWith("x-playwright")) {
              headers.put(entry.getKey(), entry.getValue());
            }
          }
          CompletableFuture<Void> remoteReadyPromise = new CompletableFuture<>();
          WebSocketClient remote = new WebSocketClient(URI.create(remoteServer.wsEndpoint()), new Draft_6455(), headers, 0) {
            @Override
            public void onOpen(ServerHandshake handshake) {
              remoteReadyPromise.complete(null);
            }

            @Override
            public void onMessage(String message) {
              ws.send(message);
            }

            @Override
            public void onMessage(ByteBuffer message) {
              byte[] bytes = new byte[message.remaining()];
              message.get(bytes);
              ws.send(bytes);
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
              ws.close();
            }

            @Override
            public void onError(Exception error) {
              remoteReadyPromise.completeExceptionally(error);
              ws.close();
            }
          };
          remote.connect();
          ws.onMessage(message -> {
            try {
              remoteReadyPromise.get();
            } catch (InterruptedException | ExecutionException e) {
              return;
            }
            if (message.isBinary) {
              remote.send(message.data);
            } else {
              remote.send(message.text());
            }
          });
          ws.onClose((code, reason) -> remote.close());
        });
        Browser browser = tlsConnect.connect("wss://localhost:" + httpsServer.PORT + "/ws");
        assertTrue(browser.version() != null && !browser.version().isEmpty());
        browser.close();
      } finally {
        tlsConnect.close();
        playwright.close();
      }
    }

    @PlaywrightTest
    @DisplayName("BT-035 should print HTTP error")
    void shouldPrintHTTPError(Connect connect, TestServer server) {
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> connect.connect("ws://localhost:" + server.PORT + "/ws-401"));
      assertTrue(error.getMessage().contains("401"));
      assertTrue(error.getMessage().contains("Unauthorized body"));
    }

    @PlaywrightTest
    @DisplayName("BT-036 should print ws error")
    void shouldPrintWsError(Connect connect, TestServer server) {
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> connect.connect("ws://does-not-exist.problem-domain:10987?secret=MYSECRET"));
      assertTrue(error.getMessage().contains("<ws connecting> ws://does-not-exist.problem-domain:10987/"));
      assertTrue(error.getMessage().contains("<ws error>"));
      assertTrue(error.getMessage().contains("getaddrinfo"));
      assertFalse(error.getMessage().contains("secret=MYSECRET"));
    }

    @PlaywrightTest
    @DisplayName("BT-037 should print custom ws close error")
    @Fixme("java: the error message has no 'browserType.connect:' prefix")
    void shouldPrintCustomWsCloseError(Connect connect, TestServer server) {
      server.onceWebSocketConnection((ws, request) -> {
        ws.onMessage(message -> {
          ws.close(4123, "Oh my!");
        });
      });
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> connect.connect("ws://localhost:" + server.PORT + "/ws"));
      assertTrue(error.getMessage().contains("browserType.connect: Oh my!"));
    }

    @PlaywrightTest
    @DisplayName("BT-038 should be able to reconnect to a browser")
    void shouldBeAbleToReconnectToABrowser(Connect connect, StartRemoteServer startRemoteServer, TestServer server) {
      PlaywrightServer remoteServer = startRemoteServer.start(kind);
      {
        Browser browser = connect.connect(remoteServer.wsEndpoint());
        BrowserContext browserContext = browser.newContext();
        assertEquals(0, browserContext.pages().size());
        Page page = browserContext.newPage();
        assertEquals(121, page.evaluate("11 * 11"));
        page.navigate(server.EMPTY_PAGE);
        browser.close();
      }
      {
        Browser browser = connect.connect(remoteServer.wsEndpoint());
        BrowserContext browserContext = browser.newContext();
        Page page = browserContext.newPage();
        page.navigate(server.EMPTY_PAGE);
        browser.close();
      }
    }

    @PlaywrightTest
    @DisplayName("BT-039 should not crash on malformed json frame")
    void shouldNotCrashOnMalformedJsonFrame(Connect connect, StartRemoteServer startRemoteServer) throws Exception {
      PlaywrightServer remoteServer = startRemoteServer.start(kind);
      CompletableFuture<Void> opened = new CompletableFuture<>();
      CountDownLatch closed = new CountDownLatch(1);
      WebSocketClient ws = new WebSocketClient(URI.create(remoteServer.wsEndpoint())) {
        @Override
        public void onOpen(ServerHandshake handshake) {
          opened.complete(null);
        }

        @Override
        public void onMessage(String message) {
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
          closed.countDown();
        }

        @Override
        public void onError(Exception error) {
          opened.completeExceptionally(error);
        }
      };
      ws.connect();
      opened.get();
      ws.send("]");
      assertTrue(closed.await(30, TimeUnit.SECONDS));
      // Server should still be alive and accept new connections.
      Browser browser = connect.connect(remoteServer.wsEndpoint());
      Page page = browser.newPage();
      assertEquals(3, page.evaluate("1 + 2"));
      browser.close();
    }

    @PlaywrightTest
    @DisplayName("BT-040 should be able to visit ipv6")
    void shouldBeAbleToVisitIpv6(Connect connect, StartRemoteServer startRemoteServer, IpV6ServerPort ipV6ServerPort) {
      assumeFalse(System.getenv("INSIDE_DOCKER") != null && !System.getenv("INSIDE_DOCKER").isEmpty(), "fail: docker does not support IPv6 by default");
      assumeFalse("webkit-wsl".equals(Env.channel()), "fail: WebKit on WSL does not support IPv6: https://github.com/microsoft/WSL/issues/10803");
      PlaywrightServer remoteServer = startRemoteServer.start(kind);
      Browser browser = connect.connect(remoteServer.wsEndpoint());
      Page page = browser.newPage();
      String ipV6Url = "http://[::1]:" + ipV6ServerPort.port;
      page.navigate(ipV6Url);
      assertTrue(page.content().contains("from-ipv6-server"));
      browser.close();
    }

    @Timeout(value = 60, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    @PlaywrightTest
    @DisplayName("BT-041 should ignore page.pause when headed")
    void shouldIgnorePagePauseWhenHeaded(Connect connect, StartRemoteServer startRemoteServer, BrowserType.LaunchOptions launchOptions) {
      assumeFalse("chromium-headless-shell".equals(Env.channel()), "shell is never headed");

      Boolean headless = launchOptions.headless;
      launchOptions.headless = false;
      PlaywrightServer remoteServer = startRemoteServer.start(kind);
      Browser browser = connect.connect(remoteServer.wsEndpoint());
      BrowserContext browserContext = browser.newContext();
      Page page = browserContext.newPage();
      page.pause();
      browser.close();
      launchOptions.headless = headless;
    }

    @PlaywrightTest
    @DisplayName("BT-042 should be able to visit ipv6 through localhost")
    void shouldBeAbleToVisitIpv6ThroughLocalhost(Connect connect, StartRemoteServer startRemoteServer, IpV6ServerPort ipV6ServerPort) {
      assumeFalse(System.getenv("INSIDE_DOCKER") != null && !System.getenv("INSIDE_DOCKER").isEmpty(), "fail: docker does not support IPv6 by default");
      assumeFalse("webkit-wsl".equals(Env.channel()), "WebKit on WSL does not support IPv6: https://github.com/microsoft/WSL/issues/10803");
      PlaywrightServer remoteServer = startRemoteServer.start(kind);
      Browser browser = connect.connect(remoteServer.wsEndpoint());
      Page page = browser.newPage();
      String ipV6Url = "http://localhost:" + ipV6ServerPort.port;
      page.navigate(ipV6Url);
      assertTrue(page.content().contains("from-ipv6-server"));
      browser.close();
    }

    @PlaywrightTest
    @DisplayName("BT-043 should be able to connect two browsers at the same time")
    void shouldBeAbleToConnectTwoBrowsersAtTheSameTime(Connect connect, StartRemoteServer startRemoteServer) {
      PlaywrightServer remoteServer = startRemoteServer.start(kind);

      Browser browser1 = connect.connect(remoteServer.wsEndpoint());
      assertEquals(0, browser1.contexts().size());
      browser1.newContext();
      assertEquals(1, browser1.contexts().size());

      Browser browser2 = connect.connect(remoteServer.wsEndpoint());
      assertEquals(0, browser2.contexts().size());
      browser2.newContext();
      assertEquals(1, browser2.contexts().size());
      assertEquals(1, browser1.contexts().size());

      browser1.close();
      assertEquals(1, browser2.contexts().size());
      Page page2 = browser2.newPage();
      assertEquals(42, page2.evaluate("() => 7 * 6")); // original browser should still work

      browser2.close();
    }

    @PlaywrightTest
    @DisplayName("BT-044 should timeout in socket while connecting")
    @Fixme("java: the error message has no 'browserType.connect:' prefix")
    void shouldTimeoutInSocketWhileConnecting(Connect connect, TestServer server) {
      PlaywrightException e = assertThrows(PlaywrightException.class, () -> connect.connect("ws://localhost:" + server.PORT + "/ws-slow",
        new BrowserType.ConnectOptions().setTimeout(1000)));
      assertTrue(e.getMessage().contains("browserType.connect: Timeout 1000ms exceeded"));
    }

    @PlaywrightTest
    @DisplayName("BT-045 should timeout in connect while connecting")
    @Fixme(value = "java: connect() has no timeout once the websocket is open, it waits forever for the Playwright initialization", timeoutMillis = 15000)
    void shouldTimeoutInConnectWhileConnecting(Connect connect, TestServer server) {
      PlaywrightException e = assertThrows(PlaywrightException.class, () -> connect.connect("ws://localhost:" + server.PORT + "/ws",
        new BrowserType.ConnectOptions().setTimeout(100)));
      assertTrue(e.getMessage().contains("browserType.connect: Timeout 100ms exceeded"));
    }

    @PlaywrightTest
    @DisplayName("BT-046 should send extra headers with connect request")
    @Fixme(value = "java: connect() has no timeout once the websocket is open, it waits forever for the Playwright initialization", timeoutMillis = 15000)
    void shouldSendExtraHeadersWithConnectRequest(Connect connect, TestServer server) throws Exception {
      CompletableFuture<TestServer.Request> requestPromise = server.waitForWebSocketConnectionRequest();
      try {
        connect.connect("ws://localhost:" + server.PORT + "/ws", new BrowserType.ConnectOptions()
          .setHeaders(headers("User-Agent", "Playwright", "foo", "bar"))
          .setTimeout(100));
      } catch (PlaywrightException e) {
      }
      TestServer.Request request = requestPromise.get();
      assertEquals("Playwright", request.headers.get("user-agent"));
      assertEquals("bar", request.headers.get("foo"));
    }

    @PlaywrightTest
    @DisplayName("BT-047 should send extra headers with connect request in object form")
    void shouldSendExtraHeadersWithConnectRequestInObjectForm(BrowserType browserType, TestServer server) throws Exception {
      // Java: the server closes the websocket after the handshake so that connect() returns, upstream never awaits it.
      CompletableFuture<TestServer.Request> requestPromise = new CompletableFuture<>();
      server.onceWebSocketConnection((ws, request) -> {
        requestPromise.complete(request);
        ws.close();
      });
      // Java: the object form {wsEndpoint, ...options} is the endpoint and the options.
      try {
        browserType.connect("ws://localhost:" + server.PORT + "/ws", new BrowserType.ConnectOptions()
          .setHeaders(headers("User-Agent", "Playwright", "foo", "bar"))
          .setTimeout(3000));
      } catch (PlaywrightException e) {
      }
      TestServer.Request request = requestPromise.get();
      assertEquals("Playwright", request.headers.get("user-agent"));
      assertEquals("bar", request.headers.get("foo"));
    }

    @PlaywrightTest
    @DisplayName("BT-048 should send default User-Agent and X-Playwright-Browser headers with connect request")
    @Fixme(value = "java: connect() has no timeout once the websocket is open, it waits forever for the Playwright initialization", timeoutMillis = 15000)
    void shouldSendDefaultUserAgentAndXPlaywrightBrowserHeadersWithConnectRequest(Connect connect, BrowserName browserName, TestServer server, Playwright playwright) throws Exception {
      // Java: getUserAgent() is a function of the driver; global fetch sends the same User-Agent.
      CompletableFuture<TestServer.Request> userAgentRequest = server.waitForRequest("/empty.html");
      playwright.request().newContext().get(server.EMPTY_PAGE);
      String userAgent = userAgentRequest.get().headers.get("user-agent");

      CompletableFuture<TestServer.Request> requestPromise = server.waitForWebSocketConnectionRequest();
      try {
        connect.connect("ws://localhost:" + server.PORT + "/ws", new BrowserType.ConnectOptions()
          .setHeaders(headers("foo", "bar"))
          .setTimeout(100));
      } catch (PlaywrightException e) {
      }
      TestServer.Request request = requestPromise.get();
      assertEquals(userAgent, request.headers.get("user-agent"));
      assertEquals(browserName.toString(), request.headers.get("x-playwright-browser"));
      assertEquals("bar", request.headers.get("foo"));
    }

    @PlaywrightTest
    @DisplayName("BT-049 should support slowmo option")
    void shouldSupportSlowmoOption(Connect connect, StartRemoteServer startRemoteServer) {
      PlaywrightServer remoteServer = startRemoteServer.start(kind);

      Browser browser1 = connect.connect(remoteServer.wsEndpoint(), new BrowserType.ConnectOptions().setSlowMo(200));
      long start = System.currentTimeMillis();
      browser1.newContext();
      browser1.close();
      assertTrue(System.currentTimeMillis() - start > 199);
    }

    @PlaywrightTest
    @DisplayName("BT-050 disconnected event should be emitted when browser is closed or server is closed")
    void disconnectedEventShouldBeEmittedWhenBrowserIsClosedOrServerIsClosed(Connect connect, StartRemoteServer startRemoteServer) throws Exception {
      PlaywrightServer remoteServer = startRemoteServer.start(kind);

      Browser browser1 = connect.connect(remoteServer.wsEndpoint());
      browser1.newPage();

      Browser browser2 = connect.connect(remoteServer.wsEndpoint());
      // Java: the page lets the client dispatch events while the test waits, see waitFor().
      Page page2 = browser2.newPage();

      AtomicInteger disconnected1 = new AtomicInteger();
      AtomicInteger disconnected2 = new AtomicInteger();
      browser1.onDisconnected(b -> disconnected1.incrementAndGet());
      browser2.onDisconnected(b -> disconnected2.incrementAndGet());

      CompletableFuture<Browser> browser1Disconnected = new CompletableFuture<>();
      browser1.onDisconnected(browser1Disconnected::complete);
      browser1.close();
      browser1Disconnected.get();
      assertEquals(1, disconnected1.get());
      assertEquals(0, disconnected2.get());

      CompletableFuture<Browser> browser2Disconnected = new CompletableFuture<>();
      browser2.onDisconnected(browser2Disconnected::complete);
      remoteServer.close();
      waitFor(browser2Disconnected, page2);
      assertEquals(1, disconnected1.get());
      assertEquals(1, disconnected2.get());
    }

    @PlaywrightTest
    @DisplayName("BT-051 disconnected event should have browser as argument")
    void disconnectedEventShouldHaveBrowserAsArgument(Connect connect, StartRemoteServer startRemoteServer) throws Exception {
      PlaywrightServer remoteServer = startRemoteServer.start(kind);
      Browser browser = connect.connect(remoteServer.wsEndpoint());
      CompletableFuture<Browser> disconnectedPromise = new CompletableFuture<>();
      browser.onDisconnected(disconnectedPromise::complete);
      browser.close();
      Browser disconnected = disconnectedPromise.get();
      assertEquals(browser, disconnected);
    }

    // BT-052 should handle exceptions during connect: not ported, it passes a JavaScript hook function.

    @PlaywrightTest
    @DisplayName("BT-053 should set the browser connected state")
    void shouldSetTheBrowserConnectedState(Connect connect, StartRemoteServer startRemoteServer) {
      PlaywrightServer remoteServer = startRemoteServer.start(kind);
      Browser remote = connect.connect(remoteServer.wsEndpoint());
      assertEquals(true, remote.isConnected());
      remote.close();
      assertEquals(false, remote.isConnected());
    }

    @PlaywrightTest
    @DisplayName("BT-054 should throw when used after isConnected returns false")
    void shouldThrowWhenUsedAfterIsConnectedReturnsFalse(Connect connect, StartRemoteServer startRemoteServer) throws Exception {
      PlaywrightServer remoteServer = startRemoteServer.start(kind);
      Browser browser = connect.connect(remoteServer.wsEndpoint());
      Page page = browser.newPage();
      CompletableFuture<Browser> disconnectedPromise = new CompletableFuture<>();
      browser.onDisconnected(disconnectedPromise::complete);
      remoteServer.close();
      waitFor(disconnectedPromise, page);
      assertEquals(false, browser.isConnected());
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.evaluate("1 + 1"));
      assertTrue(error.getMessage().contains("closed"));
    }

    @PlaywrightTest
    @DisplayName("BT-055 should throw when calling waitForNavigation after disconnect")
    void shouldThrowWhenCallingWaitForNavigationAfterDisconnect(Connect connect, StartRemoteServer startRemoteServer) throws Exception {
      PlaywrightServer remoteServer = startRemoteServer.start(kind);
      Browser browser = connect.connect(remoteServer.wsEndpoint());
      Page page = browser.newPage();
      CompletableFuture<Browser> disconnectedPromise = new CompletableFuture<>();
      browser.onDisconnected(disconnectedPromise::complete);
      remoteServer.close();
      waitFor(disconnectedPromise, page);
      assertEquals(false, browser.isConnected());
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.waitForNavigation(() -> {}));
      assertTrue(error.getMessage().contains(kTargetClosedErrorMessage));
    }

    // BT-056 should reject navigation when browser closes: not ported, see upstream_not_ported.txt.

    // BT-057 should reject waitForSelector when browser closes: not ported, see upstream_not_ported.txt.

    @PlaywrightTest
    @DisplayName("BT-058 should emit close events on pages and contexts")
    void shouldEmitCloseEventsOnPagesAndContexts(Connect connect, StartRemoteServer startRemoteServer) throws Exception {
      PlaywrightServer remoteServer = startRemoteServer.start(kind);
      Browser browser = connect.connect(remoteServer.wsEndpoint());
      BrowserContext context = browser.newContext();
      Page page = context.newPage();
      AtomicBoolean pageClosed = new AtomicBoolean(false);
      page.onClose(p -> pageClosed.set(true));
      CompletableFuture<BrowserContext> contextClosed = new CompletableFuture<>();
      context.onClose(contextClosed::complete);
      remoteServer.close();
      waitFor(contextClosed, page);
      assertTrue(pageClosed.get());
    }

    @PlaywrightTest
    @DisplayName("BT-059 should terminate network waiters")
    @Fixme("java: the error of a wait after a disconnect is 'Browser closed', not the target closed message")
    void shouldTerminateNetworkWaiters(Connect connect, StartRemoteServer startRemoteServer, TestServer server) {
      PlaywrightServer remoteServer = startRemoteServer.start(kind);
      Browser browser = connect.connect(remoteServer.wsEndpoint());
      Page newPage = browser.newPage();
      // Java: the waits nest, and remoteServer.close() goes in the callback of the inner one, so that both are
      // pending when the server closes; each throws with its own error.
      PlaywrightException[] results = new PlaywrightException[2];
      try {
        newPage.waitForRequest(server.EMPTY_PAGE, () -> {
          try {
            newPage.waitForResponse(server.EMPTY_PAGE, () -> remoteServer.close());
          } catch (PlaywrightException e) {
            results[1] = e;
          }
        });
      } catch (PlaywrightException e) {
        results[0] = e;
      }
      for (int i = 0; i < 2; i++) {
        String message = results[i].getMessage();
        assertTrue(message.contains(kTargetClosedErrorMessage), message);
        assertFalse(message.contains("Timeout"), message);
      }
    }

    @PlaywrightTest
    @DisplayName("BT-060 should reject waitForEvent before browser.close finishes")
    void shouldRejectWaitForEventBeforeBrowserCloseFinishes(Connect connect, StartRemoteServer startRemoteServer, TestServer server) throws Exception {
      PlaywrightServer remoteServer = startRemoteServer.start(kind);
      Browser browser = connect.connect(remoteServer.wsEndpoint());
      Page newPage = browser.newPage();
      AtomicBoolean rejected = new AtomicBoolean(false);
      // Java: browser.close() goes in the callback of the wait.
      try {
        newPage.waitForDownload(() -> browser.close());
      } catch (PlaywrightException e) {
        rejected.set(true);
      }
      assertEquals(true, rejected.get());
    }

    @PlaywrightTest
    @DisplayName("BT-061 should reject waitForEvent before browser.onDisconnect fires")
    @Fixme("java: the disconnected event is dispatched inside the wait, so the rejection is observed after it")
    void shouldRejectWaitForEventBeforeBrowserOnDisconnectFires(Connect connect, StartRemoteServer startRemoteServer) {
      PlaywrightServer remoteServer = startRemoteServer.start(kind);
      Browser browser = connect.connect(remoteServer.wsEndpoint());
      Page newPage = browser.newPage();
      List<String> log = new ArrayList<>();
      // Java: remoteServer.close() goes in the callback of the wait, which throws once the page is gone.
      browser.onDisconnected(b -> log.add("disconnected"));
      try {
        newPage.waitForDownload(() -> remoteServer.close());
      } catch (PlaywrightException e) {
        log.add("rejected");
      }
      // Java: the browser is disconnected, nothing more can be dispatched, so the poll is a plain check.
      assertEquals(asList("rejected", "disconnected"), log);
    }

    @PlaywrightTest
    @DisplayName("BT-062 should respect selectors")
    void shouldRespectSelectors(Playwright playwright, Connect connect, StartRemoteServer startRemoteServer) {
      PlaywrightServer remoteServer = startRemoteServer.start(kind);

      String mycss1 = "mycss1-" + kind;
      String mycss2 = "mycss2-" + kind;
      String mycss3 = "mycss3-" + kind;

      // Java: the engine is the script source, an expression that evaluates to the engine.
      String mycss = "({\n" +
        "  query(root, selector) {\n" +
        "    return root.querySelector(selector);\n" +
        "  },\n" +
        "  queryAll(root, selector) {\n" +
        "    return Array.from(root.querySelectorAll(selector));\n" +
        "  }\n" +
        "})";
      // Register one engine before connecting.
      playwright.selectors().register(mycss1, mycss);

      Browser browser1 = connect.connect(remoteServer.wsEndpoint());
      BrowserContext context1 = browser1.newContext();

      // Register another engine after creating context.
      playwright.selectors().register(mycss2, mycss);

      Page page1 = context1.newPage();
      page1.setContent("<div>hello</div>");
      assertEquals("hello", page1.innerHTML("css=div"));
      assertEquals("hello", page1.innerHTML(mycss1 + "=div"));
      assertEquals("hello", page1.innerHTML(mycss2 + "=div"));

      Browser browser2 = connect.connect(remoteServer.wsEndpoint());

      // Register third engine after second connect.
      playwright.selectors().register(mycss3, mycss);

      Page page2 = browser2.newPage();
      page2.setContent("<div>hello</div>");
      assertEquals("hello", page2.innerHTML("css=div"));
      assertEquals("hello", page2.innerHTML(mycss1 + "=div"));
      assertEquals("hello", page2.innerHTML(mycss2 + "=div"));
      assertEquals("hello", page2.innerHTML(mycss3 + "=div"));

      browser1.close();
    }

    @PlaywrightTest
    @DisplayName("BT-063 should not throw on close after disconnect")
    void shouldNotThrowOnCloseAfterDisconnect(Connect connect, StartRemoteServer startRemoteServer) throws Exception {
      PlaywrightServer remoteServer = startRemoteServer.start(kind);
      Browser browser = connect.connect(remoteServer.wsEndpoint());
      Page page = browser.newPage();
      CompletableFuture<Browser> disconnectedPromise = new CompletableFuture<>();
      browser.onDisconnected(disconnectedPromise::complete);
      remoteServer.close();
      waitFor(disconnectedPromise, page);
      browser.close();
    }

    @PlaywrightTest
    @DisplayName("BT-064 should saveAs videos from remote browser")
    void shouldSaveAsVideosFromRemoteBrowser(Connect connect, StartRemoteServer startRemoteServer, TestInfo testInfo) throws Exception {
      PlaywrightServer remoteServer = startRemoteServer.start(kind);
      Browser browser = connect.connect(remoteServer.wsEndpoint());
      Path videosPath = testInfo.outputDir();
      BrowserContext context = browser.newContext(new Browser.NewContextOptions()
        .setRecordVideoDir(videosPath)
        .setRecordVideoSize(320, 240));
      Page page = context.newPage();
      page.evaluate("() => document.body.style.backgroundColor = 'red'");
      ensureSomeFrames(page);
      context.close();

      assertEquals(0, videosPath.toFile().list().length, "recordVideo.dir might be unaccessible in server mode, so /tmp/artifacts should be used");

      Path savedAsPath = testInfo.outputPath("my-video.webm");
      page.video().saveAs(savedAsPath);
      assertTrue(Files.exists(savedAsPath));
    }

    @PlaywrightTest
    @DisplayName("BT-065 should save videos to artifactsDir")
    void shouldSaveVideosToArtifactsDir(Connect connect, StartRemoteServer startRemoteServer, TestInfo testInfo) throws Exception {
      Path artifactsDir = testInfo.outputPath("artifacts");
      PlaywrightServer remoteServer = startRemoteServer.start(kind, new RemoteServerOptions().setArtifactsDir(artifactsDir.toString()));
      Browser browser = connect.connect(remoteServer.wsEndpoint());
      Path localDir = testInfo.outputPath("random-dir");
      BrowserContext context = browser.newContext(new Browser.NewContextOptions()
        .setRecordVideoDir(localDir)
        .setRecordVideoSize(320, 240));
      Page page = context.newPage();
      page.evaluate("() => document.body.style.backgroundColor = 'red'");
      ensureSomeFrames(page);
      context.close();

      Path savedAsPath = testInfo.outputPath("my-video.webm");
      page.video().saveAs(savedAsPath);
      assertTrue(Files.exists(savedAsPath));
      assertFalse(Files.exists(localDir));

      browser.close();
    }

    @PlaywrightTest
    @DisplayName("BT-066 should name video file after page guid when connecting to remote browser with artifactsDir")
    @Fixme("java: recordVideo without a dir is not supported, the video is not recorded")
    void shouldNameVideoFileAfterPageGuidWhenConnectingToRemoteBrowserWithArtifactsDir(Connect connect, StartRemoteServer startRemoteServer, TestInfo testInfo) throws Exception {
      Path artifactsDir = testInfo.outputPath("artifacts");
      PlaywrightServer remoteServer = startRemoteServer.start(kind, new RemoteServerOptions().setArtifactsDir(artifactsDir.toString()));
      Browser browser = connect.connect(remoteServer.wsEndpoint());
      BrowserContext context = browser.newContext(new Browser.NewContextOptions()
        .setRecordVideoSize(320, 240));
      Page page = context.newPage();
      page.evaluate("() => document.body.style.backgroundColor = 'red'");
      ensureSomeFrames(page);
      context.close();

      // Java: _guid is an internal field of the client object.
      assertTrue(Files.exists(artifactsDir.resolve(guid(page) + ".webm")));

      browser.close();
    }

    @PlaywrightTest
    @DisplayName("BT-067 should be able to connect 20 times to a single server without warnings")
    void shouldBeAbleToConnect20TimesToASingleServerWithoutWarnings(Connect connect, StartRemoteServer startRemoteServer, BrowserName browserName, BrowserType.LaunchOptions launchOptions, TestInfo testInfo) throws Exception {
      assumeTrue("linux".equals(Env.platform()), "Testing non-platform specific code");

      PlaywrightServer remoteServer = startRemoteServer.start(kind);

      // Java: the client code that upstream runs in the test process runs in the driver. A driver of the
      // test's own, started with a script that records its 'warning' events, stands for process.on('warning').
      Path warningHook = testInfo.outputPath("warning-hook.js");
      Path warningFile = testInfo.outputPath("warning.txt");
      Files.write(warningHook, ("process.on('warning', w => require('fs').writeFileSync(" + new Gson().toJson(warningFile.toString()) + ", String(w.stack || w)));\n").getBytes(StandardCharsets.UTF_8));
      Map<String, String> env = new HashMap<>();
      env.put("PWTEST_UNDER_TEST", "1");
      env.put("NODE_OPTIONS", "--require " + new Gson().toJson(warningHook.toString()));
      Playwright playwright = PlaywrightImpl.createImpl(new Playwright.CreateOptions().setEnv(env), true);
      try {
        Connect warningConnect = new Connect(browserName.browserType(playwright), launchOptions);
        List<Browser> browsers = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
          browsers.add(warningConnect.connect(remoteServer.wsEndpoint()));
        }
        for (Browser browser : browsers) {
          browser.close();
        }
      } finally {
        playwright.close();
      }

      String warning = Files.exists(warningFile) ? new String(Files.readAllBytes(warningFile), StandardCharsets.UTF_8) : null;
      assertNull(warning);
    }

    @PlaywrightTest
    @DisplayName("BT-068 should save download")
    @Fixme("java: the message of download.path() differs: 'Path is not available when using browserType.connect(). Use download.saveAs() to save a local copy.'")
    void shouldSaveDownload(TestServer server, Connect connect, StartRemoteServer startRemoteServer, TestInfo testInfo) throws Exception {
      server.setRoute("/download", (req, res) -> {
        res.setHeader("Content-Type", "application/octet-stream");
        res.setHeader("Content-Disposition", "attachment");
        res.end("Hello world");
      });

      PlaywrightServer remoteServer = startRemoteServer.start(kind);
      Browser browser = connect.connect(remoteServer.wsEndpoint());
      Page page = browser.newPage();
      page.setContent("<a href=\"" + server.PREFIX + "/download\">download</a>");
      Download download = page.waitForDownload(() -> page.click("a"));
      Path nestedPath = testInfo.outputPath("these", "are", "directories", "download.txt");
      download.saveAs(nestedPath);
      assertTrue(Files.exists(nestedPath));
      assertEquals("Hello world", new String(Files.readAllBytes(nestedPath), StandardCharsets.UTF_8));
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> download.path());
      assertTrue(error.getMessage().contains("Path is not available when connecting remotely. Use saveAs() to save a local copy."));
      browser.close();
    }

    @PlaywrightTest
    @DisplayName("BT-069 should error when saving download after deletion")
    void shouldErrorWhenSavingDownloadAfterDeletion(TestServer server, Connect connect, StartRemoteServer startRemoteServer, TestInfo testInfo) {
      server.setRoute("/download", (req, res) -> {
        res.setHeader("Content-Type", "application/octet-stream");
        res.setHeader("Content-Disposition", "attachment");
        res.end("Hello world");
      });

      PlaywrightServer remoteServer = startRemoteServer.start(kind);
      Browser browser = connect.connect(remoteServer.wsEndpoint());
      Page page = browser.newPage();
      page.setContent("<a href=\"" + server.PREFIX + "/download\">download</a>");
      Download download = page.waitForDownload(() -> page.click("a"));
      Path userPath = testInfo.outputPath("download.txt");
      download.delete();
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> download.saveAs(userPath));
      assertTrue(error.getMessage().contains("Target page, context or browser has been closed"));
      browser.close();
    }

    // BT-070 should properly disconnect when connection closes from the client side: not ported, see upstream_not_ported.txt.

    @PlaywrightTest
    @DisplayName("BT-071 should be able to connect when the wsEndpoint is passed as an option")
    void shouldBeAbleToConnectWhenTheWsEndpointIsPassedAsAnOption(BrowserType browserType, StartRemoteServer startRemoteServer, BrowserType.LaunchOptions launchOptions) {
      PlaywrightServer remoteServer = startRemoteServer.start(kind);
      // Java: connect() takes the endpoint and the options, there is no wsEndpoint option.
      Browser browser = browserType.connect(remoteServer.wsEndpoint(), new BrowserType.ConnectOptions()
        .setHeaders(headers("x-playwright-launch-options", new Gson().toJson(RemoteServer.defaultLaunchOptions(launchOptions)))));
      Page page = browser.newPage();
      assertEquals(3, page.evaluate("1 + 2"));
      browser.close();
    }

    @PlaywrightTest
    @DisplayName("BT-072 should save har")
    void shouldSaveHar(Connect connect, StartRemoteServer startRemoteServer, TestServer server, TestInfo testInfo) throws Exception {
      PlaywrightServer remoteServer = startRemoteServer.start(kind);
      Browser browser = connect.connect(remoteServer.wsEndpoint());
      Path harPath = testInfo.outputPath("test.har");
      BrowserContext context = browser.newContext(new Browser.NewContextOptions()
        .setRecordHarPath(harPath));
      Page page = context.newPage();
      page.navigate(server.EMPTY_PAGE);
      context.close();
      browser.close();

      Map<String, Object> log = (Map<String, Object>) ((Map<String, Object>) new Gson().fromJson(new String(Files.readAllBytes(harPath), StandardCharsets.UTF_8), Map.class)).get("log");
      List<Map<String, Object>> entries = (List<Map<String, Object>>) log.get("entries");
      List<Map<String, Object>> pages = (List<Map<String, Object>>) log.get("pages");
      assertEquals(1, entries.size());
      Map<String, Object> entry = entries.get(0);
      assertEquals(pages.get(0).get("id"), entry.get("pageref"));
      assertEquals(server.EMPTY_PAGE, ((Map<String, Object>) entry.get("request")).get("url"));
    }

    @PlaywrightTest
    @DisplayName("BT-073 should filter launch options")
    void shouldFilterLaunchOptions(Connect connect, StartRemoteServer startRemoteServer, TestServer server, BrowserType.LaunchOptions launchOptions, TestInfo testInfo) throws Exception {
      Path tracesDir = testInfo.outputPath("traces");
      // Java: the run's launch options carry the tracesDir, there is no _defaultTracesDir.
      Path oldTracesDir = launchOptions.tracesDir;
      launchOptions.tracesDir = tracesDir;
      PlaywrightServer remoteServer = startRemoteServer.start(kind);
      Browser browser = connect.connect(remoteServer.wsEndpoint());
      Page page = browser.newPage();
      page.navigate(server.EMPTY_PAGE);
      browser.close();
      launchOptions.tracesDir = oldTracesDir;
      assertEquals(false, Files.exists(tracesDir));
    }

    @PlaywrightTest
    @DisplayName("BT-074 should record trace with sources")
    @Fixme("java: the client does not record sources into the trace")
    void shouldRecordTraceWithSources(Connect connect, StartRemoteServer startRemoteServer, TestServer server, TestInfo testInfo) throws Exception {
      PlaywrightServer remoteServer = startRemoteServer.start(kind);
      Browser browser = connect.connect(remoteServer.wsEndpoint());
      BrowserContext context = browser.newContext();
      Page page = context.newPage();

      context.tracing().start(new com.microsoft.playwright.Tracing.StartOptions().setSources(true));
      page.navigate(server.EMPTY_PAGE);
      page.setContent("<button>Click</button>");
      page.click("\"Click\"");
      context.tracing().stop(new com.microsoft.playwright.Tracing.StopOptions().setPath(testInfo.outputPath("trace1.zip")));

      context.close();
      browser.close();

      Map<String, byte[]> resources = parseTraceRaw(testInfo.outputPath("trace1.zip"));
      List<String> sourceNames = new ArrayList<>();
      for (String name : resources.keySet()) {
        if (name.startsWith("src/")) {
          sourceNames.add(name);
        }
      }
      assertEquals(1, sourceNames.size());
      byte[] sourceFile = resources.get(sourceNames.get(0));
      byte[] thisFile = Files.readAllBytes(Env.srcTestDir().resolve("java/com/microsoft/playwright/tests/library/TestBrowsertypeConnect.java"));
      assertTrue(java.util.Arrays.equals(thisFile, sourceFile));
    }

    @PlaywrightTest
    @DisplayName("BT-075 should fulfill with global fetch result")
    void shouldFulfillWithGlobalFetchResult(Connect connect, StartRemoteServer startRemoteServer, Playwright playwright, TestServer server) {
      PlaywrightServer remoteServer = startRemoteServer.start(kind);
      Browser browser = connect.connect(remoteServer.wsEndpoint());
      BrowserContext context = browser.newContext();
      Page page = context.newPage();

      page.route("**/*", route -> {
        com.microsoft.playwright.APIRequestContext request = playwright.request().newContext();
        APIResponse response = request.get(server.PREFIX + "/simple.json");
        route.fulfill(new Route.FulfillOptions().setResponse(response));
      });
      Response response = page.navigate(server.EMPTY_PAGE);
      assertEquals(200, response.status());
      // Java: Response has no json().
      assertEquals(mapOf("foo", "bar"), new Gson().fromJson(response.text(), Map.class));
    }

    @PlaywrightTest
    @DisplayName("BT-076 should upload large file")
    void shouldUploadLargeFile(Connect connect, StartRemoteServer startRemoteServer, TestServer server, TestInfo testInfo) throws Exception {
      PlaywrightServer remoteServer = startRemoteServer.start(kind);
      Browser browser = connect.connect(remoteServer.wsEndpoint());
      BrowserContext context = browser.newContext();
      Page page = context.newPage();

      page.navigate(server.PREFIX + "/input/fileupload.html");
      Path uploadFile = testInfo.outputPath("200MB.zip");
      byte[] str = new byte[4 * 1024];
      java.util.Arrays.fill(str, (byte) 'A');
      try (OutputStream stream = Files.newOutputStream(uploadFile)) {
        for (int i = 0; i < 50 * 1024; i++) {
          stream.write(str);
        }
      }
      Locator input = page.locator("input[type=\"file\"]");
      JSHandle events = input.evaluateHandle("e => {\n" +
        "  const events = [];\n" +
        "  e.addEventListener('input', () => events.push('input'));\n" +
        "  e.addEventListener('change', () => events.push('change'));\n" +
        "  return events;\n" +
        "}");
      input.setInputFiles(uploadFile);
      assertEquals("200MB.zip", input.evaluate("e => e.files[0].name"));
      assertEquals(asList("input", "change"), events.evaluate("e => e"));
      CompletableFuture<UploadedFile> serverFilePromise = new CompletableFuture<>();
      server.setRoute("/upload", (req, res) -> {
        // Java: formidable parses the multipart body of the request.
        UploadedFile file = parseMultipartFile(req.postBody(), req.headers.get("content-type"));
        res.end();
        serverFilePromise.complete(file);
      });
      page.click("input[type=submit]");
      UploadedFile file1 = serverFilePromise.get();
      assertEquals("200MB.zip", file1.originalFilename);
      assertEquals(200 * 1024 * 1024, file1.size);
      Files.delete(uploadFile);
    }

    @PlaywrightTest
    @DisplayName("BT-077 should upload a folder")
    void shouldUploadAFolder(Connect connect, StartRemoteServer startRemoteServer, TestServer server, TestInfo testInfo) throws Exception {
      PlaywrightServer remoteServer = startRemoteServer.start(kind);
      Browser browser = connect.connect(remoteServer.wsEndpoint());
      BrowserContext context = browser.newContext();
      Page page = context.newPage();

      page.navigate(server.PREFIX + "/input/folderupload.html");
      ElementHandle input = page.querySelector("input");
      String folderName = "folder-upload-test";
      Path dir = testInfo.outputPath(folderName);
      {
        Files.createDirectories(dir);
        Files.write(dir.resolve("file1.txt"), "file1 content".getBytes(StandardCharsets.UTF_8));
        Files.write(dir.resolve("file2"), "file2 content".getBytes(StandardCharsets.UTF_8));
        Files.createDirectory(dir.resolve("sub-dir"));
        Files.write(dir.resolve("sub-dir").resolve("really.txt"), "sub-dir file content".getBytes(StandardCharsets.UTF_8));
      }
      input.setInputFiles(dir);

      List<Object> webkitRelativePaths = (List<Object>) page.evaluate("e => [...e.files].map(f => f.webkitRelativePath)", input);
      assertEquals(new HashSet<>(asList(
        folderName + "/file1.txt",
        folderName + "/file2",
        folderName + "/sub-dir/really.txt")), new HashSet<>(webkitRelativePaths));

      for (int i = 0; i < webkitRelativePaths.size(); i++) {
        Object content = input.evaluate("(e, i) => {\n" +
          "  const reader = new FileReader();\n" +
          "  const promise = new Promise(fulfill => reader.onload = fulfill);\n" +
          "  reader.readAsText(e.files[i]);\n" +
          "  return promise.then(() => reader.result);\n" +
          "}", i);
        assertEquals(new String(Files.readAllBytes(dir.resolve("..").resolve((String) webkitRelativePaths.get(i))), StandardCharsets.UTF_8), content);
      }
    }

    @PlaywrightTest
    @DisplayName("BT-078 setInputFiles should preserve lastModified timestamp")
    void setInputFilesShouldPreserveLastModifiedTimestamp(Connect connect, StartRemoteServer startRemoteServer) throws Exception {
      // test.info().annotations.push({ type: 'issue', description: 'https://github.com/microsoft/playwright/issues/27452' });
      PlaywrightServer remoteServer = startRemoteServer.start(kind);
      Browser browser = connect.connect(remoteServer.wsEndpoint());
      BrowserContext context = browser.newContext();
      Page page = context.newPage();

      page.setContent("<input type=file multiple=true/>");
      Locator input = page.locator("input");
      List<String> files = asList("file-to-upload.txt", "file-to-upload-2.txt");
      input.setInputFiles(new Path[] {asset(files.get(0)), asset(files.get(1))});
      assertEquals(files, input.evaluate("e => [...e.files].map(f => f.name)"));
      List<Object> timestamps = (List<Object>) input.evaluate("e => [...e.files].map(f => f.lastModified)");
      List<Long> expectedTimestamps = new ArrayList<>();
      for (String file : files) {
        expectedTimestamps.add(Files.getLastModifiedTime(asset(file)).toMillis());
      }
      // On Linux browser sometimes reduces the timestamp by 1ms: 1696272058110.0715  -> 1696272058109 or even
      // rounds it to seconds in WebKit: 1696272058110 -> 1696272058000.
      for (int i = 0; i < timestamps.size(); i++) {
        assertTrue(Math.abs(((Number) timestamps.get(i)).doubleValue() - expectedTimestamps.get(i)) <= 1000, "expected: " + expectedTimestamps + "; actual: " + timestamps);
      }
    }

    @PlaywrightTest
    @DisplayName("BT-079 should connect over http")
    void shouldConnectOverHttp(Connect connect, StartRemoteServer startRemoteServer) {
      PlaywrightServer remoteServer = startRemoteServer.start(kind);

      URI url = URI.create(remoteServer.wsEndpoint());
      Browser browser = connect.connect("http://localhost:" + url.getPort());
      assertTrue(browser.version() != null && !browser.version().isEmpty());
      browser.close();
    }

    @Nested
    class SocksProxy {
      private void skipForLaunchServer() {
        assumeFalse("launchServer".equals(kind), "not supported yet");
      }

      @PlaywrightTest
      @DisplayName("BT-080 should forward non-forwarded requests")
      void shouldForwardNonForwardedRequests(TestServer server, StartRemoteServer startRemoteServer, Connect connect) {
        skipForLaunchServer();
        AtomicBoolean reachedOriginalTarget = new AtomicBoolean(false);
        server.setRoute("/foo.html", (req, res) -> {
          reachedOriginalTarget.set(true);
          res.end("<html><body>original-target</body></html>");
        });
        PlaywrightServer remoteServer = startRemoteServer.start(kind);
        Browser browser = connect.connect(remoteServer.wsEndpoint(), new BrowserType.ConnectOptions().setExposeNetwork("*"));
        Page page = browser.newPage();
        page.navigate(server.PREFIX + "/foo.html");
        assertTrue(page.content().contains("original-target"));
        assertEquals(true, reachedOriginalTarget.get());
      }

      @PlaywrightTest
      @DisplayName("BT-081 should proxy localhost requests @smoke")
      void shouldProxyLocalhostRequests(StartRemoteServer startRemoteServer, TestServer server, BrowserName browserName, Connect connect, DummyServerPort dummyServerPort) {
        skipForLaunchServer();
        assumeFalse(browserName == BrowserName.WEBKIT && Env.platform().equals("darwin"), "no localhost proxying");

        AtomicBoolean reachedOriginalTarget = new AtomicBoolean(false);
        server.setRoute("/foo.html", (req, res) -> {
          reachedOriginalTarget.set(true);
          res.end("<html><body></body></html>");
        });
        int examplePort = 20_000 + workerIndex() * 3;
        PlaywrightServer remoteServer = startRemoteServer.start(kind);
        Browser browser = connect.connect(remoteServer.wsEndpoint(), new BrowserType.ConnectOptions().setExposeNetwork("*"),
          new ConnectTestOptions().setRedirectPortForTest(dummyServerPort.port));
        Page page = browser.newPage();
        {
          page.setContent("empty");
          page.navigate("http://127.0.0.1:" + examplePort + "/foo.html");
          assertTrue(page.content().contains("from-dummy-server"));
          assertEquals(false, reachedOriginalTarget.get());
        }
        {
          page.setContent("empty");
          page.navigate("http://localhost:" + examplePort + "/foo.html");
          assertTrue(page.content().contains("from-dummy-server"));
          assertEquals(false, reachedOriginalTarget.get());
        }
        {
          String error = failed(() -> page.navigate("http://[::1]:" + examplePort + "/foo.html"));
          assertEquals("failed", error);
          assertEquals(false, reachedOriginalTarget.get());
        }
      }

      @PlaywrightTest
      @DisplayName("BT-082 should proxy ipv6 localhost requests @smoke")
      void shouldProxyIpv6LocalhostRequests(StartRemoteServer startRemoteServer, TestServer server, BrowserName browserName, Connect connect, IpV6ServerPort ipV6ServerPort) {
        skipForLaunchServer();
        assumeFalse(browserName == BrowserName.WEBKIT && Env.platform().equals("darwin"), "no localhost proxying");

        AtomicBoolean reachedOriginalTarget = new AtomicBoolean(false);
        server.setRoute("/foo.html", (req, res) -> {
          reachedOriginalTarget.set(true);
          res.end("<html><body></body></html>");
        });
        int examplePort = 20_000 + workerIndex() * 3;
        PlaywrightServer remoteServer = startRemoteServer.start(kind);
        Browser browser = connect.connect(remoteServer.wsEndpoint(), new BrowserType.ConnectOptions().setExposeNetwork("*"),
          new ConnectTestOptions().setRedirectPortForTest(ipV6ServerPort.port));
        Page page = browser.newPage();
        {
          page.setContent("empty");
          page.navigate("http://[::1]:" + examplePort + "/foo.html");
          assertTrue(page.content().contains("from-ipv6-server"));
          assertEquals(false, reachedOriginalTarget.get());
        }
        {
          page.setContent("empty");
          page.navigate("http://localhost:" + examplePort + "/foo.html");
          assertTrue(page.content().contains("from-ipv6-server"));
          assertEquals(false, reachedOriginalTarget.get());
        }
        {
          String error = failed(() -> page.navigate("http://127.0.0.1:" + examplePort + "/foo.html"));
          assertEquals("failed", error);
          assertEquals(false, reachedOriginalTarget.get());
        }
      }

      @PlaywrightTest
      @DisplayName("BT-083 should proxy requests from fetch api")
      void shouldProxyRequestsFromFetchApi(StartRemoteServer startRemoteServer, TestServer server, BrowserName browserName, Connect connect, DummyServerPort dummyServerPort) {
        skipForLaunchServer();
        assumeFalse(browserName == BrowserName.WEBKIT && Env.platform().equals("darwin"), "no localhost proxying");

        AtomicBoolean reachedOriginalTarget = new AtomicBoolean(false);
        server.setRoute("/foo.html", (req, res) -> {
          reachedOriginalTarget.set(true);
          res.end("<html><body></body></html>");
        });
        int examplePort = 20_000 + workerIndex() * 3;
        PlaywrightServer remoteServer = startRemoteServer.start(kind);
        Browser browser = connect.connect(remoteServer.wsEndpoint(), new BrowserType.ConnectOptions().setExposeNetwork("*"),
          new ConnectTestOptions().setRedirectPortForTest(dummyServerPort.port));
        Page page = browser.newPage();
        {
          APIResponse response = page.request().get("http://localhost:" + examplePort + "/foo.html");
          assertEquals(200, response.status());
          assertTrue(response.text().contains("from-dummy-server"));
          assertEquals(false, reachedOriginalTarget.get());
        }
        {
          APIResponse response = page.request().get("http://127.0.0.1:" + examplePort + "/foo.html");
          assertEquals(200, response.status());
          assertTrue(response.text().contains("from-dummy-server"));
          assertEquals(false, reachedOriginalTarget.get());
        }
        {
          String error = failed(() -> page.request().get("http://[::1]:" + examplePort + "/foo.html"));
          assertEquals("failed", error);
          assertEquals(false, reachedOriginalTarget.get());
        }
      }

      @PlaywrightTest
      @DisplayName("BT-084 should proxy requests from fetch api over ipv6")
      void shouldProxyRequestsFromFetchApiOverIpv6(StartRemoteServer startRemoteServer, TestServer server, BrowserName browserName, Connect connect, IpV6ServerPort ipV6ServerPort) {
        skipForLaunchServer();
        assumeFalse(browserName == BrowserName.WEBKIT && Env.platform().equals("darwin"), "no localhost proxying");

        AtomicBoolean reachedOriginalTarget = new AtomicBoolean(false);
        server.setRoute("/foo.html", (req, res) -> {
          reachedOriginalTarget.set(true);
          res.end("<html><body></body></html>");
        });
        int examplePort = 20_000 + workerIndex() * 3;
        PlaywrightServer remoteServer = startRemoteServer.start(kind);
        Browser browser = connect.connect(remoteServer.wsEndpoint(), new BrowserType.ConnectOptions().setExposeNetwork("*"),
          new ConnectTestOptions().setRedirectPortForTest(ipV6ServerPort.port));
        Page page = browser.newPage();
        {
          APIResponse response = page.request().get("http://localhost:" + examplePort + "/foo.html");
          assertEquals(200, response.status());
          assertTrue(response.text().contains("from-ipv6-server"));
          assertEquals(false, reachedOriginalTarget.get());
        }
        {
          APIResponse response = page.request().get("http://[::1]:" + examplePort + "/foo.html");
          assertEquals(200, response.status());
          assertTrue(response.text().contains("from-ipv6-server"));
          assertEquals(false, reachedOriginalTarget.get());
        }
        {
          String error = failed(() -> page.request().get("http://127.0.0.1:" + examplePort + "/foo.html"));
          assertEquals("failed", error);
          assertEquals(false, reachedOriginalTarget.get());
        }
      }

      @PlaywrightTest
      @DisplayName("BT-085 should proxy local.playwright requests")
      void shouldProxyLocalPlaywrightRequests(Connect connect, TestServer server, DummyServerPort dummyServerPort, StartRemoteServer startRemoteServer) {
        skipForLaunchServer();
        AtomicBoolean reachedOriginalTarget = new AtomicBoolean(false);
        server.setRoute("/foo.html", (req, res) -> {
          reachedOriginalTarget.set(true);
          res.end("<html><body></body></html>");
        });
        int examplePort = 20_000 + workerIndex() * 3;
        PlaywrightServer remoteServer = startRemoteServer.start(kind);
        Browser browser = connect.connect(remoteServer.wsEndpoint(), new BrowserType.ConnectOptions().setExposeNetwork("*"),
          new ConnectTestOptions().setRedirectPortForTest(dummyServerPort.port));
        Page page = browser.newPage();
        page.navigate("http://local.playwright:" + examplePort + "/foo.html");
        assertTrue(page.content().contains("from-dummy-server"));
        assertEquals(false, reachedOriginalTarget.get());
      }

      @PlaywrightTest
      @DisplayName("BT-086 should lead to the error page for forwarded requests when the connection is refused")
      void shouldLeadToTheErrorPageForForwardedRequestsWhenTheConnectionIsRefused(Connect connect, StartRemoteServer startRemoteServer, BrowserName browserName) {
        skipForLaunchServer();
        int examplePort = 20_000 + workerIndex() * 3;
        PlaywrightServer remoteServer = startRemoteServer.start(kind);
        Browser browser = connect.connect(remoteServer.wsEndpoint(), new BrowserType.ConnectOptions().setExposeNetwork("*"));
        Page page = browser.newPage();
        PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.navigate("http://127.0.0.1:" + examplePort));
        if (browserName == BrowserName.CHROMIUM) {
          assertTrue(error.getMessage().contains("net::ERR_SOCKS_CONNECTION_FAILED at http://127.0.0.1:20"));
        } else if (browserName == BrowserName.WEBKIT) {
          assertTrue(error.getMessage() != null && !error.getMessage().isEmpty());
        } else if (browserName == BrowserName.FIREFOX) {
          assertEquals(true, error.getMessage().contains("NS_ERROR_NET_RESET") || error.getMessage().contains("NS_ERROR_CONNECTION_REFUSED"));
        }
      }

      @PlaywrightTest
      @DisplayName("BT-087 should proxy based on the pattern")
      void shouldProxyBasedOnThePattern(Connect connect, StartRemoteServer startRemoteServer, TestServer server, BrowserName browserName, DummyServerPort dummyServerPort) {
        skipForLaunchServer();
        assumeFalse(browserName == BrowserName.WEBKIT && Env.platform().equals("darwin"), "no localhost proxying");

        AtomicBoolean reachedOriginalTarget = new AtomicBoolean(false);
        server.setRoute("/foo.html", (req, res) -> {
          reachedOriginalTarget.set(true);
          res.end("<html><body>from-original-server</body></html>");
        });
        int examplePort = 20_000 + workerIndex() * 3;
        PlaywrightServer remoteServer = startRemoteServer.start(kind);
        Browser browser = connect.connect(remoteServer.wsEndpoint(), new BrowserType.ConnectOptions().setExposeNetwork("localhost"),
          new ConnectTestOptions().setRedirectPortForTest(dummyServerPort.port));
        Page page = browser.newPage();

        // localhost should be proxied.
        page.navigate("http://localhost:" + examplePort + "/foo.html");
        assertTrue(page.content().contains("from-dummy-server"));
        assertEquals(false, reachedOriginalTarget.get());

        // 127.0.0.1 should be served directly.
        page.navigate("http://127.0.0.1:" + server.PORT + "/foo.html");
        assertTrue(page.content().contains("from-original-server"));
        assertEquals(true, reachedOriginalTarget.get());

        // Random domain should be served directly and fail.
        AtomicBoolean failed = new AtomicBoolean(false);
        try {
          page.navigate("http://does-not-exist-bad-domain.oh-no-should-not-work");
        } catch (PlaywrightException e) {
          failed.set(true);
        }
        assertEquals(true, failed.get());
      }

      @PlaywrightTest
      @DisplayName("BT-088 should check proxy pattern on the client")
      void shouldCheckProxyPatternOnTheClient(Connect connect, StartRemoteServer startRemoteServer, TestServer server, DummyServerPort dummyServerPort) {
        skipForLaunchServer();
        AtomicBoolean reachedOriginalTarget = new AtomicBoolean(false);
        server.setRoute("/foo.html", (req, res) -> {
          reachedOriginalTarget.set(true);
          res.end("<html><body>from-original-server</body></html>");
        });
        PlaywrightServer remoteServer = startRemoteServer.start(kind);
        Browser browser = connect.connect(remoteServer.wsEndpoint(), new BrowserType.ConnectOptions()
          .setExposeNetwork("127.0.0.1")
          .setHeaders(headers("x-playwright-proxy", "*")),
          new ConnectTestOptions().setRedirectPortForTest(dummyServerPort.port));
        Page page = browser.newPage();

        // local.playwright should fail on the client side.
        AtomicBoolean failed = new AtomicBoolean(false);
        try {
          page.navigate("http://local.playwright:" + server.PORT + "/foo.html");
        } catch (PlaywrightException e) {
          failed.set(true);
        }
        assertEquals(true, failed.get());
        assertEquals(false, reachedOriginalTarget.get());
      }
    }
  }

  @Nested
  class LaunchServerOnly {
    @PlaywrightTest
    @DisplayName("BT-089 should work with cluster")
    void shouldWorkWithCluster(Connect connect, StartRemoteServer startRemoteServer) {
      PlaywrightServer remoteServer = startRemoteServer.start("launchServer", new RemoteServerOptions().setInCluster(true));
      Browser browser = connect.connect(remoteServer.wsEndpoint());
      Page page = browser.newPage();
      assertEquals(3, page.evaluate("1 + 2"));
    }

    @PlaywrightTest
    @DisplayName("BT-090 should properly disconnect when connection closes from the server side")
    void shouldProperlyDisconnectWhenConnectionClosesFromTheServerSide(Connect connect, StartRemoteServer startRemoteServer, TestServer server) {
      assumeFalse(Env.platform().equals("win32"), "Cannot send signals");

      RemoteServer remoteServer = (RemoteServer) startRemoteServer.start("launchServer", new RemoteServerOptions().setDisconnectOnSIGHUP(true));
      // Java: the navigation blocks until the stylesheet is served, so the websocket server is closed from the
      // route of the stylesheet, on the server thread, while the navigation is pending.
      server.setRoute("/one-style.css", (req, res) -> {
        // This closes the websocket server.
        remoteServer.child().kill("SIGHUP");
      });
      Browser browser = connect.connect(remoteServer.wsEndpoint());
      Page page = browser.newPage();
      boolean[] disconnected = {false};
      browser.onDisconnected(b -> disconnected[0] = true);
      PlaywrightException navError = assertThrows(PlaywrightException.class,
        () -> page.navigate(server.PREFIX + "/one-style.html", new Page.NavigateOptions().setTimeout(60000)));
      assertTrue(disconnected[0]);
      assertEquals(false, browser.isConnected());

      assertTrue(navError.getMessage().contains("has been closed"), navError.getMessage());
      String waitError1 = assertThrows(PlaywrightException.class, () -> page.waitForNavigation(() -> {})).getMessage();
      assertTrue(waitError1.contains(kTargetClosedErrorMessage), waitError1);
      String gotoError = assertThrows(PlaywrightException.class, () -> page.navigate(server.EMPTY_PAGE)).getMessage();
      assertTrue(gotoError.contains("has been closed"), gotoError);
      String waitError2 = assertThrows(PlaywrightException.class, () -> page.waitForNavigation(() -> {})).getMessage();
      assertTrue(waitError2.contains(kTargetClosedErrorMessage), waitError2);
    }

    @PlaywrightTest
    @DisplayName("BT-091 should be able to reconnect to a browser 12 times without warnings")
    void shouldBeAbleToReconnectToABrowser12TimesWithoutWarnings(Connect connect, StartRemoteServer startRemoteServer, TestServer server) {
      PlaywrightServer remoteServer = startRemoteServer.start("launchServer", new RemoteServerOptions().setExitOnWarning(true));
      for (int i = 0; i < 12; i++) {
        // it.step('connect #' + i)
        Browser browser = connect.connect(remoteServer.wsEndpoint());
        BrowserContext browserContext = browser.newContext();
        assertEquals(0, browserContext.pages().size());
        Page page = browserContext.newPage();
        assertEquals(121, page.evaluate("11 * 11"));
        page.navigate(server.EMPTY_PAGE);
        browser.close();
      }
    }

    @PlaywrightTest
    @DisplayName("BT-092 cannot launch another browser")
    void cannotLaunchAnotherBrowser(Connect connect, StartRemoteServer startRemoteServer) throws Exception {
      PlaywrightServer remoteServer = startRemoteServer.start("launchServer");
      Browser browser = connect.connect(remoteServer.wsEndpoint());
      // Java: _parent is an internal field of the client object.
      BrowserType parent = (BrowserType) field(channelOwnerClass(browser), "parent", browser);
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> parent.launch(Env.launchOptions().setTimeout(0)));
      assertTrue(error.getMessage().contains("Launching more browsers is not allowed."));
    }
  }

  @PlaywrightTest
  @DisplayName("BT-093 should refuse connecting when versions do not match")
  void shouldRefuseConnectingWhenVersionsDoNotMatch(Connect connect, ChildProcessFactory childProcess) throws Exception {
    RunServer server = new RunServer();
    server.start(childProcess, new RunServer.Options().setMode("default").setEnv(Collections.singletonMap("PW_VERSION_OVERRIDE", "1.2.3")));
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> connect.connect(server.wsEndpoint()));
    server.close();
    assertTrue(error.getMessage().contains("Playwright version mismatch"));
    assertTrue(error.getMessage().contains("server version: v1.2"));
    // Java: the driver is the client of the server, getPlaywrightVersion(true) is its major.minor version.
    String version = (String) new Gson().fromJson(new String(Files.readAllBytes(RunServer.driverPackageDir().resolve("package.json")), StandardCharsets.UTF_8), Map.class).get("version");
    String[] parts = version.split("\\.");
    assertTrue(error.getMessage().contains("client version: v" + parts[0] + "." + parts[1]));
  }

  @PlaywrightTest
  @DisplayName("BT-094 should filter local paths from unsafe launch options")
  void shouldFilterLocalPathsFromUnsafeLaunchOptions(Connect connect, StartRemoteServer startRemoteServer, TestServer server, TestInfo testInfo) throws Exception {
    // test.info().annotations.push({ type: 'issue', description: 'https://github.com/microsoft/playwright/issues/42394' });
    server.setRoute("/download", (req, res) -> {
      res.setHeader("Content-Type", "application/octet-stream");
      res.setHeader("Content-Disposition", "attachment");
      res.end("Hello world");
    });
    Path artifactsDir = testInfo.outputPath("artifacts");
    Path downloadsPath = testInfo.outputPath("downloads");
    Files.write(artifactsDir, "not a directory".getBytes(StandardCharsets.UTF_8));
    Files.write(downloadsPath, "not a directory".getBytes(StandardCharsets.UTF_8));
    PlaywrightServer remoteServer = startRemoteServer.start("run-server", new RemoteServerOptions()
      .setUnsafe(true)
      .setEnv(Collections.singletonMap("PWTEST_UNDER_TEST", (String) null)));
    Browser browser = connect.connect(remoteServer.wsEndpoint(), new BrowserType.ConnectOptions(),
      new ConnectTestOptions().setLaunchOptions(mapOf("artifactsDir", artifactsDir.toString(), "downloadsPath", downloadsPath.toString())));
    BrowserContext context = browser.newContext();
    context.tracing().start(new com.microsoft.playwright.Tracing.StartOptions().setSnapshots(true));
    Page page = context.newPage();
    page.setContent("<a href=\"" + server.PREFIX + "/download\">download</a>");
    Download download = page.waitForDownload(() -> page.click("a"));
    assertNull(download.failure());
    Path tracePath = testInfo.outputPath("trace.zip");
    context.tracing().stop(new com.microsoft.playwright.Tracing.StopOptions().setPath(tracePath));
    context.close();
    browser.close();

    List<String> actions = traceActions(parseTraceRaw(tracePath));
    // Java: the titles of the calls ("Set content") come from the protocol metainfo of the Node.js client.
    assertTrue(actions.contains("Frame.setContent"));
  }

  @PlaywrightTest
  @DisplayName("BT-095 should timeout after redirect when connecting over http")
  void shouldTimeoutAfterRedirectWhenConnectingOverHttp(Connect connect, TestServer server) throws Exception {
    server.setRedirect("/connect/json", "/connect/slow");
    AtomicBoolean aborted = new AtomicBoolean(false);
    server.setRoute("/connect/slow", (req, res) -> {
      req.socket().onClose(() -> aborted.set(true));
    });
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> connect.connect(server.PREFIX + "/connect", new BrowserType.ConnectOptions()
      .setTimeout(2000)
      .setHeaders(headers("Connection", "Close"))));
    assertTrue(error.getMessage().contains("Timeout 2000ms exceeded."));
    expectPoll(() -> aborted.get(), true, null);
  }

  // BT-096 should not retain closed browsers in launch mode: not ported, it starts a server of the driver in the test process.

  private static Map<String, String> headers(String... namesAndValues) {
    Map<String, String> result = new LinkedHashMap<>();
    for (int i = 0; i < namesAndValues.length; i += 2) {
      result.put(namesAndValues[i], namesAndValues[i + 1]);
    }
    return result;
  }

  /** {@code testInfo.workerIndex}: the tests of a class run one after another. */
  private static int workerIndex() {
    return 0;
  }

  /** {@code await page.goto(url).catch(() => 'failed')} for a call that returns nothing we look at. */
  private static String failed(Runnable call) {
    try {
      call.run();
      return null;
    } catch (PlaywrightException e) {
      return "failed";
    }
  }

  private static void ensureSomeFrames(Page page) {
    rafraf(page, 100);
    page.screenshot();
  }

  /**
   * The client dispatches events while a call waits, so a test that waits for an event without
   * a call of its own makes calls on the page, which fail once the page is closed.
   */
  private static <T> T waitFor(CompletableFuture<T> future, Page page) throws Exception {
    while (!future.isDone()) {
      try {
        page.waitForTimeout(10);
      } catch (PlaywrightException e) {
        Thread.sleep(10);
      }
    }
    return future.get();
  }

  /** {@code await expect.poll(() => actual).toEqual(expected)}. */
  private static <T> void expectPoll(Supplier<T> actual, T expected, Page page) throws Exception {
    long deadline = System.currentTimeMillis() + 5000;
    while (!expected.equals(actual.get()) && System.currentTimeMillis() < deadline) {
      if (page != null) {
        try {
          page.waitForTimeout(100);
        } catch (PlaywrightException e) {
          Thread.sleep(100);
        }
      } else {
        Thread.sleep(100);
      }
    }
    assertEquals(expected, actual.get());
  }

  private static Object field(Class<?> owner, String name, Object target) throws ReflectiveOperationException {
    Field field = owner.getDeclaredField(name);
    field.setAccessible(true);
    return field.get(target);
  }

  private static String guid(Object channelOwner) throws ReflectiveOperationException {
    return (String) field(channelOwnerClass(channelOwner), "guid", channelOwner);
  }

  private static Class<?> channelOwnerClass(Object channelOwner) {
    Class<?> clazz = channelOwner.getClass();
    while (!clazz.getSimpleName().equals("ChannelOwner")) {
      clazz = clazz.getSuperclass();
    }
    return clazz;
  }

  private static void closeConnection(Browser browser) throws Exception {
    Object connection = field(channelOwnerClass(browser), "connection", browser);
    java.lang.reflect.Method close = connection.getClass().getDeclaredMethod("close");
    close.setAccessible(true);
    close.invoke(connection);
  }

  /** {@code parseTraceRaw(file).resources}: the entries of the trace zip. */
  private static Map<String, byte[]> parseTraceRaw(Path file) throws IOException {
    return com.microsoft.playwright.fixtures.Utils.parseZip(file);
  }

  /** {@code parseTraceRaw(file).actions}, as "Class.method" in the order of the start times. */
  private static List<String> traceActions(Map<String, byte[]> resources) {
    List<Map<String, Object>> before = new ArrayList<>();
    for (Map.Entry<String, byte[]> entry : resources.entrySet()) {
      if (!entry.getKey().endsWith(".trace")) {
        continue;
      }
      for (String line : new String(entry.getValue(), StandardCharsets.UTF_8).split("\n")) {
        if (!line.isEmpty()) {
          Map<String, Object> event = (Map<String, Object>) new Gson().fromJson(line, Map.class);
          if ("before".equals(event.get("type"))) {
            before.add(event);
          }
        }
      }
    }
    Collections.sort(before, (a, b) -> Double.compare(((Number) a.get("startTime")).doubleValue(), ((Number) b.get("startTime")).doubleValue()));
    List<String> actions = new ArrayList<>();
    for (Map<String, Object> action : before) {
      actions.add(action.get("class") + "." + action.get("method"));
    }
    return actions;
  }

  /** The file part of a multipart request, what formidable's {@code files.file1} has. */
  static final class UploadedFile {
    String originalFilename;
    long size;
  }

  private static UploadedFile parseMultipartFile(byte[] body, String contentType) {
    String boundary = "--" + contentType.substring(contentType.indexOf("boundary=") + "boundary=".length());
    byte[] delimiter = boundary.getBytes(StandardCharsets.ISO_8859_1);
    int start = indexOf(body, delimiter, 0);
    int headersEnd = indexOf(body, "\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1), start);
    String headers = new String(body, start + delimiter.length, headersEnd - start - delimiter.length, StandardCharsets.UTF_8);
    int contentStart = headersEnd + 4;
    int contentEnd = indexOf(body, ("\r\n" + boundary).getBytes(StandardCharsets.ISO_8859_1), contentStart);
    UploadedFile file = new UploadedFile();
    int filename = headers.indexOf("filename=\"") + "filename=\"".length();
    file.originalFilename = headers.substring(filename, headers.indexOf('"', filename));
    file.size = contentEnd - contentStart;
    return file;
  }

  private static int indexOf(byte[] data, byte[] pattern, int from) {
    outer:
    for (int i = from; i <= data.length - pattern.length; i++) {
      for (int j = 0; j < pattern.length; j++) {
        if (data[i + j] != pattern[j]) {
          continue outer;
        }
      }
      return i;
    }
    return -1;
  }
}
