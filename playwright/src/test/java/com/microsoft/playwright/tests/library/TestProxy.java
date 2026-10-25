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
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.HttpsTestServer;
import com.microsoft.playwright.fixtures.MockSocksServer;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.ProxyServer;
import com.microsoft.playwright.fixtures.SocksForwardingServer;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.Proxy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/proxy.spec.ts. */
public class TestProxy {
  @PlaywrightTest
  @DisplayName("PR-002 should use proxy @smoke")
  void shouldUseProxy(BrowserType browserType, TestServer server) {
    server.setRoute("/target.html", (req, res) -> {
      res.end("<html><title>Served by the proxy</title></html>");
    });
    Browser browser = browserType.launch(Env.launchOptions().setProxy(new Proxy(server.HOST)));
    Page page = browser.newPage();
    page.navigate("http://non-existent.test/target.html");
    assertEquals("Served by the proxy", page.title());
    browser.close();
  }

  @PlaywrightTest
  @DisplayName("PR-003 should use proxy for second page")
  void shouldUseProxyForSecondPage(BrowserType browserType, TestServer server) {
    server.setRoute("/target.html", (req, res) -> {
      res.end("<html><title>Served by the proxy</title></html>");
    });
    Browser browser = browserType.launch(Env.launchOptions().setProxy(new Proxy("localhost:" + server.PORT)));

    Page page = browser.newPage();
    page.navigate("http://non-existent.test/target.html");
    assertEquals("Served by the proxy", page.title());

    Page page2 = browser.newPage();
    page2.navigate("http://non-existent.test/target.html");
    assertEquals("Served by the proxy", page2.title());

    browser.close();
  }

  @PlaywrightTest
  @DisplayName("PR-004 should work with IP:PORT notion")
  void shouldWorkWithIPPORTNotion(BrowserType browserType, TestServer server) {
    server.setRoute("/target.html", (req, res) -> {
      res.end("<html><title>Served by the proxy</title></html>");
    });
    Browser browser = browserType.launch(Env.launchOptions().setProxy(new Proxy("127.0.0.1:" + server.PORT)));
    Page page = browser.newPage();
    page.navigate("http://non-existent.test/target.html");
    assertEquals("Served by the proxy", page.title());
    browser.close();
  }

  @Nested
  class ShouldProxyLocalNetworkRequests {
    @Nested
    @ParameterizedClass
    @ValueSource(booleans = {false, true})
    class ByDefaultOrWithOtherBypasses {
      @Parameter
      boolean additionalBypass;

      @Nested
      @ParameterizedClass
      @CsvSource({
        "localhost, localhost",
        "127.0.0.1, loopback address",
        "169.254.3.4, link-local"
      })
      class Params {
        @Parameter(0)
        String target;
        @Parameter(1)
        String description;

        @PlaywrightTest
        @DisplayName("PR-005 ${params.description}")
        void paramsdescription(BrowserName browserName, BrowserType browserType, TestServer server, ProxyServer proxyServer) {
          assumeFalse(browserName == BrowserName.WEBKIT && Env.platform().equals("darwin") && Arrays.asList("localhost", "127.0.0.1").contains(target) && additionalBypass, "Mac webkit does not proxy localhost when bypass rules are set.");
          assumeFalse(Env.channel() != null && Env.channel().startsWith("msedge"), "times out while loading the page");

          String path = "/target-" + additionalBypass + "-" + target + ".html";
          server.setRoute(path, (req, res) -> {
            res.end("<html><title>Served by the proxy</title></html>");
          });

          String url = "http://" + target + ":55555" + path;
          proxyServer.forwardTo(server.PORT);
          Browser browser = browserType.launch(Env.launchOptions().setProxy(
            new Proxy("localhost:" + proxyServer.PORT).setBypass(additionalBypass ? "1.non.existent.domain.for.the.test" : null)));

          Page page = browser.newPage();
          page.navigate(url);
          assertTrue(proxyServer.requestUrls.contains(url));
          assertEquals("Served by the proxy", page.title());

          try {
            page.navigate("http://1.non.existent.domain.for.the.test/foo.html");
          } catch (PlaywrightException e) {
          }
          if (additionalBypass) {
            assertFalse(proxyServer.requestUrls.contains("http://1.non.existent.domain.for.the.test/foo.html"));
          } else {
            assertTrue(proxyServer.requestUrls.contains("http://1.non.existent.domain.for.the.test/foo.html"));
          }

          browser.close();
        }
      }
    }
  }

  @Nested
  @ParameterizedClass
  @ValueSource(strings = {"localhost", "127.0.0.1"})
  class ShouldAllowBypassingHostRequests {
    @Parameter
    String host;

    @PlaywrightTest
    @DisplayName("PR-006 should allow bypassing ${host} requests")
    void shouldAllowBypassingHostRequests(BrowserType browserType, TestServer server, ProxyServer proxyServer) {
      server.setRoute("/proxied/target.html", (req, res) -> {
        res.end("<html><title>Served by the server</title></html>");
      });
      server.setRoute("/target.html", (req, res) -> {
        res.end("<html><title>Served by the proxy</title></html>");
      });
      proxyServer.forwardTo(server.PORT, new ProxyServer.ForwardOptions().setRemovePrefix("/proxied"));

      Browser browser = browserType.launch(Env.launchOptions().setProxy(
        new Proxy("localhost:" + proxyServer.PORT).setBypass(host)));
      Page page = browser.newPage();
      page.navigate("http://" + host + ":" + server.PORT + "/proxied/target.html");
      assertEquals("Served by the server", page.title());
      browser.close();
    }
  }

  @PlaywrightTest
  @DisplayName("PR-007 should authenticate")
  @Fixme(value = "java: the bundled driver 1.64.1 sends the proxy credentials to the website auth challenge (microsoft/playwright#43326, fixed after 1.64.1)", browsers = {BrowserName.CHROMIUM, BrowserName.WEBKIT})
  void shouldAuthenticate(BrowserType browserType, TestServer server, BrowserName browserName) {
    server.setRoute("/target.html", (req, res) -> {
      String auth = req.headers.get("proxy-authorization");
      if (auth == null) {
        // Java: writeHead has no status message argument, 407 gets 'Proxy Authentication Required' by default.
        res.writeHead(407, "Proxy-Authenticate", "Basic realm=\"Access to internal site\"");
        res.end();
      } else {
        res.end("<html><title>" + auth + "</title></html>");
      }
    });
    server.setRoute("/protected.html", (req, res) -> {
      if (req.headers.get("proxy-authorization") == null) {
        res.writeHead(407, "Proxy-Authenticate", "Basic realm=\"Access to internal site\"");
        res.end();
        return;
      }
      res.writeHead(401, "WWW-Authenticate", "Basic realm=\"site\"");
      res.end(req.headers.get("authorization") != null ? req.headers.get("authorization") : "no credentials");
    });
    Browser browser = browserType.launch(Env.launchOptions().setProxy(
      new Proxy(server.HOST).setUsername("user").setPassword("secret")));
    Page page = browser.newPage();
    page.navigate("http://non-existent.test/target.html");
    assertEquals("Basic " + Base64.getEncoder().encodeToString("user:secret".getBytes(StandardCharsets.UTF_8)), page.title());
    // Proxy credentials must not be sent to the website's own auth challenge.
    Response response = null;
    PlaywrightException error = null;
    try {
      response = page.navigate("http://non-existent.test/protected.html");
    } catch (PlaywrightException e) {
      error = e;
    }
    if (browserName == BrowserName.CHROMIUM && !Env.isHeadlessShell(browserName)) {
      // Branded Chromium shows a login dialog for the 401 and fails the navigation.
      assertTrue(error.getMessage().contains("net::ERR_INVALID_AUTH_CREDENTIALS"));
    } else {
      assertEquals(401, response.status());
      assertEquals("no credentials", page.locator("body").textContent());
    }
    browser.close();
  }

  @PlaywrightTest
  @DisplayName("PR-008 should reconnect with credentials after CONNECT 407 closes the socket")
  void shouldReconnectWithCredentialsAfterCONNECT407ClosesTheSocket(BrowserType browserType, HttpsTestServer httpsServer, ProxyServer proxyServer) {
    // Some HTTP proxies send 407 and close the socket on the first CONNECT, expecting
    // the client to reconnect on a new TCP connection with Proxy-Authorization.
    httpsServer.setRoute("/target.html", (req, res) -> {
      res.end("<html><title>Served by https server via proxy</title></html>");
    });
    proxyServer.forwardTo(httpsServer.PORT, new ProxyServer.ForwardOptions().setAllowConnectRequests(true));

    List<Boolean> connectAttempts = new CopyOnWriteArrayList<>();
    boolean[] closedFirstConnect = {false};
    proxyServer.setAuthHandler(req -> {
      // WebKit on Windows uses libcurl and sends Proxy-Authorization preemptively
      // on every CONNECT, while libsoup on Linux/macOS sends it only after a 407.
      // Force the first CONNECT to fail regardless to deterministically exercise
      // the reconnect path on all platforms.
      if (!req.method.equals("CONNECT") || req.headers.get("host") == null || !req.headers.get("host").startsWith("non-existent.test")) {
        return true;
      }
      connectAttempts.add(req.headers.get("proxy-authorization") != null);
      if (!closedFirstConnect[0]) {
        closedFirstConnect[0] = true;
        return false;
      }
      return req.headers.get("proxy-authorization") != null;
    });

    Browser browser = browserType.launch(Env.launchOptions().setProxy(
      new Proxy(proxyServer.HOST).setUsername("user").setPassword("secret")));
    Page page = browser.newPage(new Browser.NewPageOptions().setIgnoreHTTPSErrors(true));
    page.navigate("https://non-existent.test/target.html");
    assertEquals("Served by https server via proxy", page.title());
    assertTrue(connectAttempts.size() >= 2);
    assertTrue(connectAttempts.contains(true));
    browser.close();
  }

  @PlaywrightTest
  @DisplayName("PR-009 should work with authenticate followed by redirect")
  void shouldWorkWithAuthenticateFollowedByRedirect(BrowserName browserName, BrowserType browserType, TestServer server) {
    server.setRoute("/page1.html", (req, res) -> {
      if (!hasAuth(req, res)) {
        return;
      }
      res.writeHead(302, "location", "/page2.html");
      res.end();
    });
    server.setRoute("/page2.html", (req, res) -> {
      if (!hasAuth(req, res)) {
        return;
      }
      res.end("<html><title>Served by the proxy</title></html>");
    });
    Browser browser = browserType.launch(Env.launchOptions().setProxy(
      new Proxy(server.HOST).setUsername("user").setPassword("secret")));
    Page page = browser.newPage();
    page.navigate("http://non-existent.test/page1.html");
    assertEquals("Served by the proxy", page.title());
    browser.close();
  }

  private static boolean hasAuth(TestServer.Request req, TestServer.Response res) {
    String auth = req.headers.get("proxy-authorization");
    if (auth == null) {
      // Java: writeHead has no status message argument, 407 gets 'Proxy Authentication Required' by default.
      res.writeHead(407, "Proxy-Authenticate", "Basic realm=\"Access to internal site\"");
      res.end();
      return false;
    }
    return true;
  }

  @PlaywrightTest
  @DisplayName("PR-010 should exclude patterns")
  void shouldExcludePatterns(BrowserType browserType, TestServer server) {
    assumeFalse(Env.channel() != null && Env.channel().startsWith("msedge"), "times out while loading the page");

    server.setRoute("/target.html", (req, res) -> {
      res.end("<html><title>Served by the proxy</title></html>");
    });
    // FYI: using long and weird domain names to avoid ATT DNS hijacking
    // that resolves everything to some weird search results page.
    //
    // @see https://gist.github.com/CollinChaffin/24f6c9652efb3d6d5ef2f5502720ef00
    Browser browser = browserType.launch(Env.launchOptions().setProxy(
      new Proxy(server.HOST).setBypass("1.non.existent.domain.for.the.test, 2.non.existent.domain.for.the.test, .another.test")));

    {
      Page page = browser.newPage();
      page.navigate("http://0.non.existent.domain.for.the.test/target.html");
      assertEquals("Served by the proxy", page.title());
      page.close();
    }

    {
      Page page = browser.newPage();
      PlaywrightException error = null;
      try {
        page.navigate("http://1.non.existent.domain.for.the.test/target.html");
      } catch (PlaywrightException e) {
        error = e;
      }
      assertTrue(error.getMessage() != null && !error.getMessage().isEmpty());
      page.close();
    }

    {
      Page page = browser.newPage();
      PlaywrightException error = null;
      try {
        page.navigate("http://2.non.existent.domain.for.the.test/target.html");
      } catch (PlaywrightException e) {
        error = e;
      }
      assertTrue(error.getMessage() != null && !error.getMessage().isEmpty());
      page.close();
    }

    {
      Page page = browser.newPage();
      PlaywrightException error = null;
      try {
        page.navigate("http://foo.is.the.another.test/target.html");
      } catch (PlaywrightException e) {
        error = e;
      }
      assertTrue(error.getMessage() != null && !error.getMessage().isEmpty());
      page.close();
    }

    {
      Page page = browser.newPage();
      page.navigate("http://3.non.existent.domain.for.the.test/target.html");
      assertEquals("Served by the proxy", page.title());
      page.close();
    }

    browser.close();
  }

  @Nested
  @ParameterizedClass
  @ValueSource(strings = {"localhost", "127.0.0.1"})
  class ShouldBypassProxyForHostWhenHostIsInBypassList {
    @Parameter
    String host;

    @PlaywrightTest
    @DisplayName("PR-011 should bypass proxy for ${host} when ${host} is in bypass list")
    void shouldBypassProxyForHostWhenHostIsInBypassList(BrowserType browserType, TestServer server, ProxyServer proxyServer) {
      proxyServer.forwardTo(server.PORT, new ProxyServer.ForwardOptions().setRemovePrefix("/proxied"));
      server.setRoute("/proxied/target.html", (req, res) -> {
        res.end("<html><title>Served by the server</title></html>");
      });
      server.setRoute("/target.html", (req, res) -> {
        res.end("<html><title>Served by the proxy</title></html>");
      });
      Browser browser = browserType.launch(Env.launchOptions().setProxy(
        new Proxy("localhost:" + proxyServer.PORT).setBypass(host)));
      Page page = browser.newPage();
      page.navigate("http://" + host + ":" + server.PORT + "/proxied/target.html");
      assertEquals("Served by the server", page.title());
      browser.close();
    }
  }

  @PlaywrightTest
  @DisplayName("PR-012 should use socks proxy")
  void shouldUseSocksProxy(BrowserType browserType, MockSocksServer mockSocksServer) {
    Browser browser = browserType.launch(Env.launchOptions().setProxy(
      new Proxy("socks5://localhost:" + mockSocksServer.PORT)));
    Page page = browser.newPage();
    page.navigate("http://non-existent.test");
    assertEquals("Served by the SOCKS proxy", page.title());
    browser.close();
  }

  @PlaywrightTest
  @DisplayName("PR-013 should use socks proxy in second page")
  void shouldUseSocksProxyInSecondPage(BrowserType browserType, MockSocksServer mockSocksServer) {
    Browser browser = browserType.launch(Env.launchOptions().setProxy(
      new Proxy("socks5://localhost:" + mockSocksServer.PORT)));

    Page page = browser.newPage();
    page.navigate("http://non-existent.test");
    assertEquals("Served by the SOCKS proxy", page.title());

    Page page2 = browser.newPage();
    page2.navigate("http://non-existent.test");
    assertEquals("Served by the SOCKS proxy", page2.title());

    browser.close();
  }

  @PlaywrightTest
  @DisplayName("PR-014 does launch without a port")
  void doesLaunchWithoutAPort(BrowserType browserType) {
    Browser browser = browserType.launch(Env.launchOptions().setProxy(new Proxy("http://localhost")));
    browser.close();
  }

  @PlaywrightTest
  @DisplayName("PR-015 should use proxy with emulated user agent")
  void shouldUseProxyWithEmulatedUserAgent(BrowserType browserType) throws IOException {
    assumeFalse(true, "fixme: Non-emulated user agent is used in proxy CONNECT");

    AtomicReference<String> requestText = new AtomicReference<>("");
    // This is our proxy server
    ServerSocket server = new ServerSocket(0);
    Thread acceptThread = new Thread(() -> {
      while (true) {
        try {
          Socket socket = server.accept();
          byte[] buffer = new byte[64 * 1024];
          int read = socket.getInputStream().read(buffer);
          if (read > 0) {
            requestText.set(new String(buffer, 0, read, StandardCharsets.UTF_8));
          }
          socket.close();
        } catch (IOException e) {
          return;
        }
      }
    });
    acceptThread.setDaemon(true);
    acceptThread.start();

    Browser browser = browserType.launch(Env.launchOptions().setProxy(
      new Proxy("http://127.0.0.1:" + server.getLocalPort())));

    Page page = browser.newPage(new Browser.NewPageOptions().setUserAgent("MyUserAgent"));

    // HTTPS over HTTP proxy will start with CONNECT request.
    try {
      page.navigate("https://bing.com/");
    } catch (PlaywrightException e) {
    }
    browser.close();
    server.close();
    // This connect request should have emulated user agent.
    assertTrue(requestText.get().contains("MyUserAgent"));
  }

  @PlaywrightTest
  @DisplayName("PR-016 should use SOCKS proxy for websocket requests")
  void shouldUseSOCKSProxyForWebsocketRequests(BrowserType browserType, TestServer server) throws IOException {
    // Java: port 0 picks a free port instead of workerIndex + 2048 + 2.
    SocksForwardingServer socksForwardingServer = SocksForwardingServer.create(0, server.PORT, 1337);
    String proxyServerAddr = socksForwardingServer.proxyServerAddr;
    Browser browser = browserType.launch(Env.launchOptions().setProxy(new Proxy(proxyServerAddr)));
    server.sendOnWebSocketConnection("incoming");
    server.setRoute("/target.html", (req, res) -> {
      res.end("<html><title>Served by the proxy</title></html>");
    });

    Page page = browser.newPage();

    // Hosts get resolved by the client
    page.navigate("http://fake-localhost-127-0-0-1.nip.io:1337/target.html");
    assertEquals("Served by the proxy", page.title());

    Object value = page.evaluate("() => {\n" +
      "  let cb;\n" +
      "  const result = new Promise(f => cb = f);\n" +
      "  const ws = new WebSocket('ws://fake-localhost-127-0-0-1.nip.io:1337/ws');\n" +
      "  ws.addEventListener('message', data => { ws.close(); cb(data.data); });\n" +
      "  return result;\n" +
      "}");
    assertEquals("incoming", value);

    browser.close();
    socksForwardingServer.close();
  }

  @PlaywrightTest
  @DisplayName("PR-017 should use http proxy for websocket requests")
  void shouldUseHttpProxyForWebsocketRequests(BrowserName browserName, BrowserType browserType, TestServer server, ProxyServer proxyServer) {
    assumeFalse(Env.isMac && Env.macVersion() == 13, "Times out on Mac 13");

    proxyServer.forwardTo(server.PORT, new ProxyServer.ForwardOptions().setAllowConnectRequests(true));
    Browser browser = browserType.launch(Env.launchOptions().setProxy(
      new Proxy("localhost:" + proxyServer.PORT)));

    server.sendOnWebSocketConnection("incoming");
    server.setRoute("/target.html", (req, res) -> {
      res.end("<html><title>Served by the proxy</title></html>");
    });

    Page page = browser.newPage();

    page.navigate("http://fake-localhost-127-0-0-1.nip.io:1337/target.html");
    assertEquals("Served by the proxy", page.title());

    Object value = page.evaluate("() => {\n" +
      "  let cb;\n" +
      "  const result = new Promise(f => cb = f);\n" +
      "  const ws = new WebSocket('ws://fake-localhost-127-0-0-1.nip.io:1337/ws');\n" +
      "  ws.addEventListener('message', data => { ws.close(); cb(data.data); });\n" +
      "  return result;\n" +
      "}");
    assertEquals("incoming", value);

    // WebKit does not use CONNECT for websockets, but other browsers do.
    if (browserName == BrowserName.WEBKIT) {
      assertTrue(proxyServer.wsUrls.contains((Env.isWindows && Env.channel() == null) ? "/ws" : "ws://fake-localhost-127-0-0-1.nip.io:1337/ws"));
    } else {
      assertTrue(proxyServer.connectHosts.contains("fake-localhost-127-0-0-1.nip.io:1337"));
    }

    browser.close();
  }
}
