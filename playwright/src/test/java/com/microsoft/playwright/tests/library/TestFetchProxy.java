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

import com.google.gson.Gson;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.ContextFactory;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.MockSocksServer;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.ProxyServer;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.Proxy;
import org.junit.jupiter.api.DisplayName;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/fetch-proxy.spec.ts. */
public class TestFetchProxy {
  private static String basic(String credentials) {
    return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
  }

  // Java: APIResponse has no json(), parse the text.
  private static Object json(APIResponse response) {
    return new Gson().fromJson(response.text(), Object.class);
  }

  // Java: the host of a URL as `new URL(url).host` gives it, with the port.
  private static String hostOf(String url) {
    URI uri = URI.create(url);
    return uri.getPort() == -1 ? uri.getHost() : uri.getHost() + ":" + uri.getPort();
  }

  @PlaywrightTest
  @DisplayName("PR-041 context request should pick up proxy credentials")
  void contextRequestShouldPickUpProxyCredentials(BrowserType browserType, TestServer server, ProxyServer proxyServer) throws Exception {
    proxyServer.forwardTo(server.PORT, new ProxyServer.ForwardOptions().setAllowConnectRequests(true));
    CompletableFuture<String> authPromise = new CompletableFuture<>();
    proxyServer.setAuthHandler(req -> {
      String header = req.headers.get("proxy-authorization");
      // Browser can issue various unrelated requests over the proxy,
      // but we are only interested in our own request.
      if (proxyServer.connectHosts.contains("non-existent.com:80")) {
        authPromise.complete(header);
      }
      return header != null;
    });
    Browser browser = browserType.launch(Env.launchOptions().setProxy(
      new Proxy("localhost:" + proxyServer.PORT).setUsername("user").setPassword("secret")));
    BrowserContext context = browser.newContext();
    APIResponse response = context.request().get("http://non-existent.com/simple.json");
    assertTrue(proxyServer.connectHosts.contains("non-existent.com:80"));
    String auth = authPromise.get();
    assertEquals(basic("user:secret"), auth);
    assertEquals(mapOf("foo", "bar"), json(response));
    browser.close();
  }

  @PlaywrightTest
  @DisplayName("PR-042 global request should pick up proxy credentials")
  void globalRequestShouldPickUpProxyCredentials(Playwright playwright, TestServer server, ProxyServer proxyServer) {
    proxyServer.forwardTo(server.PORT, new ProxyServer.ForwardOptions().setAllowConnectRequests(true));
    AtomicReference<String> auth = new AtomicReference<>();
    proxyServer.setAuthHandler(req -> {
      auth.set(req.headers.get("proxy-authorization"));
      return auth.get() != null;
    });
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions()
      .setProxy(new Proxy("localhost:" + proxyServer.PORT).setUsername("user").setPassword("secret")));
    APIResponse response = request.get("http://non-existent.com/simple.json");
    assertTrue(proxyServer.connectHosts.contains("non-existent.com:80"));
    assertEquals(basic("user:secret"), auth.get());
    assertEquals(mapOf("foo", "bar"), json(response));
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("PR-043 should work with context level proxy")
  void shouldWorkWithContextLevelProxy(ContextFactory contextFactory, TestServer server, ProxyServer proxyServer) throws Exception {
    server.setRoute("/target.html", (req, res) -> {
      res.end("<title>Served by the proxy</title>");
    });

    proxyServer.forwardTo(server.PORT, new ProxyServer.ForwardOptions().setAllowConnectRequests(true));
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions()
      .setProxy(new Proxy("localhost:" + proxyServer.PORT)));

    CompletableFuture<TestServer.Request> requestFuture = server.waitForRequest("/target.html");
    APIResponse response = context.request().get("http://non-existent.com/target.html");
    TestServer.Request request = requestFuture.get();
    assertEquals(200, response.status());
    assertEquals("/target.html", request.url);
  }

  @PlaywrightTest
  @DisplayName("PR-044 should support proxy.bypass")
  void shouldSupportProxyBypass(ContextFactory contextFactory, TestServer server, ProxyServer proxyServer) {
    server.setRoute("/target.html", (req, res) -> {
      res.end("Served by the proxy");
    });
    // FYI: using long and weird domain names to avoid ATT DNS hijacking
    // that resolves everything to some weird search results page.
    //
    // @see https://gist.github.com/CollinChaffin/24f6c9652efb3d6d5ef2f5502720ef00
    proxyServer.forwardTo(server.PORT, new ProxyServer.ForwardOptions().setAllowConnectRequests(true));
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions()
      .setProxy(new Proxy("localhost:" + proxyServer.PORT).setBypass("1.non.existent.domain.for.the.test, 2.non.existent.domain.for.the.test, .another.test")));

    {
      APIResponse res = context.request().get(server.CROSS_PROCESS_PREFIX + "/target.html");
      assertTrue(res.text().contains("Served by the proxy"));
      assertTrue(proxyServer.connectHosts.contains(hostOf(server.CROSS_PROCESS_PREFIX)));
      proxyServer.connectHosts.clear();
    }

    {
      APIResponse res = context.request().get("http://0.non.existent.domain.for.the.test/target.html");
      assertTrue(res.text().contains("Served by the proxy"));
      proxyServer.connectHosts.clear();
    }

    {
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> context.request().get("http://1.non.existent.domain.for.the.test/target.html"));
      assertTrue(error.getMessage() != null && !error.getMessage().isEmpty());
      assertEquals(new ArrayList<String>(), proxyServer.connectHosts);
    }

    {
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> context.request().get("http://2.non.existent.domain.for.the.test/target.html"));
      assertTrue(error.getMessage() != null && !error.getMessage().isEmpty());
      assertEquals(new ArrayList<String>(), proxyServer.connectHosts);
    }

    {
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> context.request().get("http://foo.is.the.another.test/target.html"));
      assertTrue(error.getMessage() != null && !error.getMessage().isEmpty());
      assertEquals(new ArrayList<String>(), proxyServer.connectHosts);
    }

    {
      APIResponse res = context.request().get("http://3.non.existent.domain.for.the.test/target.html");
      assertTrue(res.text().contains("Served by the proxy"));
    }
  }

  @PlaywrightTest
  @DisplayName("PR-045 should use socks proxy")
  void shouldUseSocksProxy(Playwright playwright, TestServer server, MockSocksServer mockSocksServer) {
    String insideDocker = System.getenv("INSIDE_DOCKER");
    assumeFalse(insideDocker != null && !insideDocker.isEmpty(), "connect ECONNREFUSED 127.0.0.1:<port>");

    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions()
      .setProxy(new Proxy("socks5://localhost:" + mockSocksServer.PORT)));
    APIResponse response = request.get(server.EMPTY_PAGE);
    assertTrue(response.text().contains("Served by the SOCKS proxy"));
  }

  // PR-046 should send correct ALPN protocol to HTTPS proxy: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("PR-047 should apply proxy.bypass to redirect targets")
  void shouldApplyProxyBypassToRedirectTargets(ContextFactory contextFactory, TestServer server, ProxyServer proxyServer) {
    String crossProcessHost = hostOf(server.CROSS_PROCESS_PREFIX);
    assumeFalse(crossProcessHost.equals(server.HOST), "Needs two different host names for the same server");

    proxyServer.forwardTo(server.PORT, new ProxyServer.ForwardOptions().setAllowConnectRequests(true));
    server.setRedirect("/redirect-to-cross-process", server.CROSS_PROCESS_PREFIX + "/simple.json");
    server.setRedirect("/redirect-to-same-origin", server.PREFIX + "/simple.json");
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions()
      .setProxy(new Proxy("localhost:" + proxyServer.PORT).setBypass(server.HOSTNAME)));

    {
      // Bypassed first hop redirects to a host that must go through the proxy.
      APIResponse response = context.request().get(server.PREFIX + "/redirect-to-cross-process");
      assertEquals(server.CROSS_PROCESS_PREFIX + "/simple.json", response.url());
      assertEquals(mapOf("foo", "bar"), json(response));
      assertEquals(Arrays.asList(crossProcessHost), proxyServer.connectHosts);
      proxyServer.connectHosts.clear();
    }

    {
      // Proxied first hop redirects to a bypassed host.
      APIResponse response = context.request().get(server.CROSS_PROCESS_PREFIX + "/redirect-to-same-origin");
      assertEquals(server.PREFIX + "/simple.json", response.url());
      assertEquals(mapOf("foo", "bar"), json(response));
      assertEquals(Arrays.asList(crossProcessHost), proxyServer.connectHosts);
    }
  }
}
