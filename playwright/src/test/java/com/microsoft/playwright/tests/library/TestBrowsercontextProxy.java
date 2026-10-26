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
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.ContextFactory;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.HttpsTestServer;
import com.microsoft.playwright.fixtures.MockSocksServer;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.ProxyServer;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.Proxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/browsercontext-proxy.spec.ts. */
public class TestBrowsercontextProxy {
  @BeforeEach
  void beforeEach(TestServer server) {
    server.setRoute("/target.html", (req, res) -> {
      res.end("<html><title>Served by the proxy</title></html>");
    });
  }

  private static String basic(String credentials) {
    return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
  }

  @PlaywrightTest
  @DisplayName("PR-018 should work when passing the proxy only on the context level")
  void shouldWorkWhenPassingTheProxyOnlyOnTheContextLevel(BrowserName browserName, BrowserType browserType, TestServer server, ProxyServer proxyServer) {
    proxyServer.forwardTo(server.PORT);
    Browser browser = null;
    try {
      browser = browserType.launch(Env.launchOptions());
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setProxy(new Proxy(proxyServer.HOST)));

      Page page = context.newPage();
      page.navigate("http://non-existent.test/target.html");
      assertTrue(proxyServer.requestUrls.contains("http://non-existent.test/target.html"));
      assertEquals("Served by the proxy", page.title());
    } finally {
      browser.close();
    }
  }

  @Nested
  @ParameterizedClass
  @ValueSource(strings = {"per-context", "http://per-context"})
  class LegacyLaunchProxy {
    @Parameter
    String launchProxy;

    @PlaywrightTest
    @DisplayName("PR-019 should ignore legacy '${launchProxy}' launch proxy")
    void shouldIgnoreLegacyLaunchProxy(BrowserType browserType, TestServer server, ProxyServer proxyServer) {
      proxyServer.forwardTo(server.PORT, new ProxyServer.ForwardOptions().setAllowConnectRequests(true));
      Browser browser = browserType.launch(Env.launchOptions().setProxy(new Proxy(launchProxy)));
      try {
        BrowserContext context = browser.newContext(new Browser.NewContextOptions().setProxy(new Proxy(proxyServer.HOST)));
        Page page = context.newPage();
        page.navigate("http://non-existent.test/target.html");
        assertEquals("Served by the proxy", page.title());
        APIResponse response = context.request().get("http://non-existent.test/target.html");
        assertTrue(response.text().contains("Served by the proxy"));
        assertTrue(proxyServer.connectHosts.contains("non-existent.test:80"));

        BrowserContext directContext = browser.newContext();
        Page directPage = directContext.newPage();
        directPage.navigate(server.PREFIX + "/target.html");
        assertEquals("Served by the proxy", directPage.title());
        APIResponse directResponse = directContext.request().get(server.PREFIX + "/target.html");
        assertEquals(true, directResponse.ok());
      } finally {
        browser.close();
      }
    }
  }

  // PR-020 should throw for bad server value: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("PR-021 should use proxy")
  void shouldUseProxy(ContextFactory contextFactory, TestServer server, ProxyServer proxyServer) {
    proxyServer.forwardTo(server.PORT);
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions()
      .setProxy(new Proxy(proxyServer.HOST)));
    Page page = context.newPage();
    page.navigate("http://non-existent.test/target.html");
    assertTrue(proxyServer.requestUrls.contains("http://non-existent.test/target.html"));
    assertEquals("Served by the proxy", page.title());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("PR-022 should send secure cookies to subdomain.localhost")
  void shouldSendSecureCookiesToSubdomainLocalhost(ContextFactory contextFactory, BrowserName browserName, TestServer server, ProxyServer proxyServer) {
    proxyServer.forwardTo(server.PORT);
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions()
      .setProxy(new Proxy(proxyServer.HOST)));
    server.setRoute("/set-cookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("non-secure=1; HttpOnly", "secure=1; HttpOnly; Secure"));
      res.end();
    });
    server.setRoute("/read-cookie.html", (req, res) -> {
      res.setHeader("Content-Type", "text/html");
      res.end("<div>Cookie: " + Arrays.stream(req.headers.get("cookie").split(";")).map(String::trim).sorted().collect(Collectors.joining("; ")) + "</div>");
    });

    Page page = context.newPage();

    page.navigate("http://subdomain.localhost/set-cookie.html");

    List<Cookie> cookies = context.cookies("http://subdomain.localhost");
    List<Map<String, Object>> actual = new ArrayList<>();
    for (Cookie cookie : cookies) {
      actual.add(mapOf("name", cookie.name, "domain", cookie.domain));
    }
    List<Map<String, Object>> expected = new ArrayList<>();
    expected.add(mapOf("name", "non-secure", "domain", "subdomain.localhost"));
    if (!(browserName == BrowserName.WEBKIT && (!Env.isWindows || "webkit-wsl".equals(Env.channel())))) {
      expected.add(mapOf("name", "secure", "domain", "subdomain.localhost"));
    }
    assertEquals(expected, actual);

    page.navigate("http://subdomain.localhost/read-cookie.html");
    assertThat(page.locator("div")).hasText(browserName == BrowserName.WEBKIT ? "Cookie: non-secure=1" : "Cookie: non-secure=1; secure=1");

    context.close();
  }

  @PlaywrightTest
  @DisplayName("PR-023 should set cookie for top-level domain")
  void shouldSetCookieForTopLevelDomain(ContextFactory contextFactory, TestServer server, ProxyServer proxyServer, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.WEBKIT && (Env.isLinux || "webkit-wsl".equals(Env.channel())), "fixme: ");

    proxyServer.forwardTo(server.PORT, new ProxyServer.ForwardOptions().setAllowConnectRequests(true));
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions()
      .setProxy(new Proxy(proxyServer.HOST)));
    server.setRoute("/empty.html", (req, res) -> {
      res.setHeader("Set-Cookie", "name=val; Domain=codes; Path=/;");
      res.end();
    });

    context.request().get("http://codes/empty.html");
    Cookie cookie = context.cookies().get(0);
    assertNotNull(cookie);
    assertEquals("name", cookie.name);
    assertEquals("val", cookie.value);
    context.close();
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
        @DisplayName("PR-024 ${params.description}")
        void paramsdescription(BrowserName browserName, ContextFactory contextFactory, TestServer server, ProxyServer proxyServer) {
          assumeFalse(browserName == BrowserName.WEBKIT && Env.platform().equals("darwin") && Arrays.asList("localhost", "127.0.0.1").contains(target) && additionalBypass, "Mac webkit does not proxy localhost when bypass rules are set");

          String path = "/target-" + additionalBypass + "-" + target + ".html";
          server.setRoute(path, (req, res) -> {
            res.end("<html><title>Served by the proxy</title></html>");
          });

          String url = "http://" + target + ":55555" + path;
          proxyServer.forwardTo(server.PORT);
          BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions()
            .setProxy(new Proxy(proxyServer.HOST).setBypass(additionalBypass ? "1.non.existent.domain.for.the.test" : null)));

          Page page = context.newPage();
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

          context.close();
        }
      }
    }
  }

  @PlaywrightTest
  @DisplayName("PR-025 should use ipv6 proxy")
  void shouldUseIpv6Proxy(ContextFactory contextFactory, TestServer server, ProxyServer proxyServer, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.FIREFOX && !Env.isBidi(), "fail: page.goto: NS_ERROR_UNKNOWN_HOST");
    assumeFalse("webkit-wsl".equals(Env.channel()), "fail: WebKit on WSL does not support IPv6: https://github.com/microsoft/WSL/issues/10803");
    proxyServer.forwardTo(server.PORT);
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions()
      .setProxy(new Proxy("[0:0:0:0:0:0:0:1]:" + proxyServer.PORT)));
    Page page = context.newPage();
    page.navigate("http://non-existent.test/target.html");
    assertTrue(proxyServer.requestUrls.contains("http://non-existent.test/target.html"));
    assertEquals("Served by the proxy", page.title());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("PR-026 should use proxy twice")
  void shouldUseProxyTwice(ContextFactory contextFactory, TestServer server, ProxyServer proxyServer) {
    proxyServer.forwardTo(server.PORT);
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions()
      .setProxy(new Proxy(proxyServer.HOST)));
    Page page = context.newPage();
    page.navigate("http://non-existent.test/target.html");
    assertTrue(proxyServer.requestUrls.contains("http://non-existent.test/target.html"));
    page.navigate("http://non-existent-2.test/target.html");
    assertTrue(proxyServer.requestUrls.contains("http://non-existent-2.test/target.html"));
    assertEquals("Served by the proxy", page.title());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("PR-027 should use proxy for second page")
  void shouldUseProxyForSecondPage(ContextFactory contextFactory, TestServer server, ProxyServer proxyServer) {
    proxyServer.forwardTo(server.PORT);
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions()
      .setProxy(new Proxy(proxyServer.HOST)));

    Page page = context.newPage();
    page.navigate("http://non-existent.test/target.html");
    assertTrue(proxyServer.requestUrls.contains("http://non-existent.test/target.html"));
    assertEquals("Served by the proxy", page.title());

    Page page2 = context.newPage();
    proxyServer.requestUrls.clear();
    page2.navigate("http://non-existent.test/target.html");
    assertTrue(proxyServer.requestUrls.contains("http://non-existent.test/target.html"));
    assertEquals("Served by the proxy", page2.title());

    context.close();
  }

  @PlaywrightTest
  @DisplayName("PR-028 should use proxy for https urls")
  void shouldUseProxyForHttpsUrls(ContextFactory contextFactory, HttpsTestServer httpsServer, ProxyServer proxyServer) {
    httpsServer.setRoute("/target.html", (req, res) -> {
      res.end("<html><title>Served by https server via proxy</title></html>");
    });
    proxyServer.forwardTo(httpsServer.PORT, new ProxyServer.ForwardOptions().setAllowConnectRequests(true));
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions()
      .setIgnoreHTTPSErrors(true)
      .setProxy(new Proxy(proxyServer.HOST)));
    Page page = context.newPage();
    page.navigate("https://non-existent.test/target.html");
    assertTrue(proxyServer.connectHosts.contains("non-existent.test:443"));
    assertEquals("Served by https server via proxy", page.title());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("PR-029 should work with IP:PORT notion")
  void shouldWorkWithIPPORTNotion(ContextFactory contextFactory, TestServer server, ProxyServer proxyServer) {
    proxyServer.forwardTo(server.PORT);
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions()
      .setProxy(new Proxy("127.0.0.1:" + proxyServer.PORT)));
    Page page = context.newPage();
    page.navigate("http://non-existent.test/target.html");
    assertTrue(proxyServer.requestUrls.contains("http://non-existent.test/target.html"));
    assertEquals("Served by the proxy", page.title());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("PR-030 should throw for socks5 authentication")
  void shouldThrowForSocks5Authentication(ContextFactory contextFactory) {
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> contextFactory.newContext(new Browser.NewContextOptions()
      .setProxy(new Proxy("socks5://localhost:1234").setUsername("user").setPassword("secret"))));
    assertTrue(error.getMessage().contains("Browser does not support socks5 proxy authentication"));
  }

  @PlaywrightTest
  @DisplayName("PR-031 should throw for socks4 authentication")
  void shouldThrowForSocks4Authentication(ContextFactory contextFactory) {
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> contextFactory.newContext(new Browser.NewContextOptions()
      .setProxy(new Proxy("socks4://localhost:1234").setUsername("user").setPassword("secret"))));
    assertTrue(error.getMessage().contains("Socks4 proxy protocol does not support authentication"));
  }

  @PlaywrightTest
  @DisplayName("PR-032 should authenticate")
  void shouldAuthenticate(ContextFactory contextFactory, TestServer server, ProxyServer proxyServer) {
    proxyServer.forwardTo(server.PORT);
    AtomicReference<String> auth = new AtomicReference<>();
    proxyServer.setAuthHandler(req -> {
      auth.set(req.headers.get("proxy-authorization"));
      return auth.get() != null;
    });
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions()
      .setProxy(new Proxy(proxyServer.HOST).setUsername("user").setPassword("secret")));
    Page page = context.newPage();
    page.navigate("http://non-existent.test/target.html");
    assertTrue(proxyServer.requestUrls.contains("http://non-existent.test/target.html"));
    assertEquals(basic("user:secret"), auth.get());
    assertEquals("Served by the proxy", page.title());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("PR-033 should authenticate with empty password")
  void shouldAuthenticateWithEmptyPassword(ContextFactory contextFactory, TestServer server, ProxyServer proxyServer) {
    proxyServer.forwardTo(server.PORT);
    AtomicReference<String> auth = new AtomicReference<>();
    proxyServer.setAuthHandler(req -> {
      auth.set(req.headers.get("proxy-authorization"));
      return auth.get() != null;
    });
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions()
      .setProxy(new Proxy(proxyServer.HOST).setUsername("user").setPassword("")));
    Page page = context.newPage();
    page.navigate("http://non-existent.test/target.html");
    assertEquals(basic("user:"), auth.get());
    assertEquals("Served by the proxy", page.title());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("PR-034 should isolate proxy credentials between contexts")
  void shouldIsolateProxyCredentialsBetweenContexts(ContextFactory contextFactory, TestServer server, BrowserName browserName, ProxyServer proxyServer) {
    proxyServer.forwardTo(server.PORT);
    AtomicReference<String> auth = new AtomicReference<>();
    proxyServer.setAuthHandler(req -> {
      auth.set(req.headers.get("proxy-authorization"));
      return auth.get() != null;
    });
    {
      BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions()
        .setProxy(new Proxy(proxyServer.HOST).setUsername("user1").setPassword("secret1")));
      Page page = context.newPage();
      page.navigate("http://non-existent.test/target.html");
      assertEquals(basic("user1:secret1"), auth.get());
      assertEquals("Served by the proxy", page.title());
      context.close();
    }
    auth.set(null);
    {
      BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions()
        .setProxy(new Proxy(proxyServer.HOST).setUsername("user2").setPassword("secret2")));
      Page page = context.newPage();
      page.navigate("http://non-existent.test/target.html");
      assertEquals("Served by the proxy", page.title());
      assertEquals(basic("user2:secret2"), auth.get());
      context.close();
    }
  }

  @PlaywrightTest
  @DisplayName("PR-035 should exclude patterns")
  void shouldExcludePatterns(ContextFactory contextFactory, TestServer server, ProxyServer proxyServer) {
    proxyServer.forwardTo(server.PORT);
    // FYI: using long and weird domain names to avoid ATT DNS hijacking
    // that resolves everything to some weird search results page.
    //
    // @see https://gist.github.com/CollinChaffin/24f6c9652efb3d6d5ef2f5502720ef00
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions()
      .setProxy(new Proxy(proxyServer.HOST).setBypass("1.non.existent.domain.for.the.test, 2.non.existent.domain.for.the.test, .another.test")));

    java.util.function.Supplier<List<String>> nonFaviconUrls = () -> {
      return proxyServer.requestUrls.stream().filter(u -> !u.contains("favicon")).collect(Collectors.toList());
    };

    {
      proxyServer.requestUrls.clear();
      Page page = context.newPage();
      page.navigate("http://0.non.existent.domain.for.the.test/target.html");
      assertTrue(proxyServer.requestUrls.contains("http://0.non.existent.domain.for.the.test/target.html"));
      assertEquals("Served by the proxy", page.title());
      page.close();
    }

    {
      proxyServer.requestUrls.clear();
      Page page = context.newPage();
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.navigate("http://1.non.existent.domain.for.the.test/target.html"));
      assertEquals(new ArrayList<String>(), nonFaviconUrls.get());
      assertTrue(error.getMessage() != null && !error.getMessage().isEmpty());
      page.close();
    }

    {
      proxyServer.requestUrls.clear();
      Page page = context.newPage();
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.navigate("http://2.non.existent.domain.for.the.test/target.html"));
      assertEquals(new ArrayList<String>(), nonFaviconUrls.get());
      assertTrue(error.getMessage() != null && !error.getMessage().isEmpty());
      page.close();
    }

    {
      proxyServer.requestUrls.clear();
      Page page = context.newPage();
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.navigate("http://foo.is.the.another.test/target.html"));
      assertEquals(new ArrayList<String>(), nonFaviconUrls.get());
      assertTrue(error.getMessage() != null && !error.getMessage().isEmpty());
      page.close();
    }

    {
      proxyServer.requestUrls.clear();
      Page page = context.newPage();
      page.navigate("http://3.non.existent.domain.for.the.test/target.html");
      assertTrue(nonFaviconUrls.get().contains("http://3.non.existent.domain.for.the.test/target.html"));
      assertEquals("Served by the proxy", page.title());
      page.close();
    }

    context.close();
  }

  @PlaywrightTest
  @DisplayName("PR-036 should use socks proxy")
  void shouldUseSocksProxy(ContextFactory contextFactory, MockSocksServer mockSocksServer) {
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions()
      .setProxy(new Proxy("socks5://localhost:" + mockSocksServer.PORT)));
    Page page = context.newPage();
    page.navigate("http://non-existent.test");
    assertEquals("Served by the SOCKS proxy", page.title());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("PR-037 should use socks proxy in second page")
  void shouldUseSocksProxyInSecondPage(ContextFactory contextFactory, MockSocksServer mockSocksServer) {
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions()
      .setProxy(new Proxy("socks5://localhost:" + mockSocksServer.PORT)));

    Page page = context.newPage();
    page.navigate("http://non-existent.test");
    assertEquals("Served by the SOCKS proxy", page.title());

    Page page2 = context.newPage();
    page2.navigate("http://non-existent.test");
    assertEquals("Served by the SOCKS proxy", page2.title());

    context.close();
  }

  @PlaywrightTest
  @DisplayName("PR-038 does launch without a port")
  void doesLaunchWithoutAPort(ContextFactory contextFactory) {
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions()
      .setProxy(new Proxy("http://localhost")));
    context.close();
  }

  @PlaywrightTest
  @DisplayName("PR-039 should isolate proxy credentials between contexts on navigation")
  void shouldIsolateProxyCredentialsBetweenContextsOnNavigation(ContextFactory contextFactory, TestServer server) {
    server.setRoute("/target.html", (req, res) -> {
      String authHeader = req.headers.get("proxy-authorization");

      if (authHeader == null) {
        res.writeHead(407, "Proxy-Authenticate", "Basic realm=\"proxy\"");
        res.end("Proxy authorization required");
        return;
      }

      String username = new String(Base64.getDecoder().decode(authHeader.split(" ")[1]), StandardCharsets.UTF_8).split(":")[0];
      res.writeHead(200, "Content-Type", "text/html");
      res.end("Hello <div data-testid=user>" + username + "</div>!\n");
    });

    BrowserContext context1 = contextFactory.newContext(new Browser.NewContextOptions()
      .setProxy(new Proxy(server.PREFIX).setUsername("user1").setPassword("secret1")));
    Page page1 = context1.newPage();
    page1.navigate("http://non-existent.test/target.html");
    assertThat(page1.getByTestId("user")).hasText("user1");

    BrowserContext context2 = contextFactory.newContext(new Browser.NewContextOptions()
      .setProxy(new Proxy(server.PREFIX).setUsername("user2").setPassword("secret2")));
    Page page2 = context2.newPage();
    page2.navigate("http://non-existent.test/target.html");
    assertThat(page2.getByTestId("user")).hasText("user2");

    page1.navigate("http://non-existent.test/target.html");
    assertThat(page1.getByTestId("user")).hasText("user1");
  }
}
