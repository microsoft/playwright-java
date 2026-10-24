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

import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.options.Proxy;

import java.io.OutputStreamWriter;
import java.util.Base64;
import java.util.List;

import static com.microsoft.playwright.fixtures.BrowserName.CHROMIUM;
import static org.junit.jupiter.api.Assertions.*;

public class TestBrowserContextProxy {
  void shouldThrowForBadServerValue() {
    // Enforced by compiler in Java
  }

  @PlaywrightTest
  void shouldUseProxy(Browser browser, Server server) {
    server.setRoute("/target.html", exchange -> {
      exchange.sendResponseHeaders(200, 0);
      try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("<html><title>Served by the proxy</title></html>");
      }
    });
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setProxy("localhost:" + server.PORT));
    Page page = context.newPage();
    page.navigate("http://non-existent.com/target.html");
    assertEquals("Served by the proxy", page.title());
    context.close();
  }

  @PlaywrightTest
  void shouldUseProxyTwice(Browser browser, Server server) {
    server.setRoute("/target.html", exchange -> {
      exchange.sendResponseHeaders(200, 0);
      try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("<html><title>Served by the proxy</title></html>");
      }
    });
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setProxy(
      new Proxy("localhost:" + server.PORT)));
    Page page = context.newPage();
    page.navigate("http://non-existent.com/target.html");
    page.navigate("http://non-existent-2.com/target.html");
    assertEquals("Served by the proxy", page.title());
    context.close();
  }

  @PlaywrightTest
  void shouldUseProxyForSecondPage(Browser browser, Server server) {
    server.setRoute("/target.html", exchange -> {
      exchange.sendResponseHeaders(200, 0);
      try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("<html><title>Served by the proxy</title></html>");
      }
    });
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setProxy(
      new Proxy("localhost:" + server.PORT)));

    Page page = context.newPage();
    page.navigate("http://non-existent.com/target.html");
    assertEquals("Served by the proxy", page.title());

    Page page2 = context.newPage();
    page2.navigate("http://non-existent.com/target.html");
    assertEquals("Served by the proxy", page2.title());

    context.close();
  }

  @PlaywrightTest
  void shouldWorkWithIPPORTNotion(Browser browser, Server server) {
    server.setRoute("/target.html", exchange -> {
      exchange.sendResponseHeaders(200, 0);
      try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("<html><title>Served by the proxy</title></html>");
      }
    });
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setProxy(
      new Proxy("127.0.0.1:" + server.PORT)));

    Page page = context.newPage();
    page.navigate("http://non-existent.com/target.html");
    assertEquals("Served by the proxy", page.title());
    context.close();
  }

  @PlaywrightTest
  void shouldAuthenticate(Browser browser, Server server) {
    server.setRoute("/target.html", exchange -> {
      List<String> auth = exchange.getRequestHeaders().get("proxy-authorization");
      if (auth == null) {
        exchange.getResponseHeaders().add("Proxy-Authenticate", "Basic realm='Access to internal site'");
        exchange.sendResponseHeaders(407, 0);
        exchange.getResponseBody().close();
        return;
      }
      exchange.sendResponseHeaders(200, 0);
      try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("<html><title>" + auth.get(0) + "</title></html>");
      }
    });
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setProxy(
      new Proxy("localhost:" + server.PORT)
      .setUsername("user")
      .setPassword("secret")));
    Page page = context.newPage();
    page.navigate("http://non-existent.com/target.html");
    assertEquals("Basic " + Base64.getEncoder().encodeToString("user:secret".getBytes()), page.title());
    context.close();
  }

  @PlaywrightTest
  @DisabledOnBrowser(value = CHROMIUM, headed = true, reason = "fixme")
  void shouldExcludePatterns(Browser browser, Server server) {
    server.setRoute("/target.html", exchange -> {
      exchange.sendResponseHeaders(200, 0);
      try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("<html><title>Served by the proxy</title></html>");
      }
    });
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setProxy(
      new Proxy("127.0.0.1:" + server.PORT)
      // FYI: using long and weird domain names to avoid ATT DNS hijacking
      // that resolves everything to some weird search results page.
      //
      // @see https://gist.github.com/CollinChaffin/24f6c9652efb3d6d5ef2f5502720ef00
      .setBypass("1.non.existent.domain.for.the.test, 2.non.existent.domain.for.the.test, .another.test")));

    {
      Page page = context.newPage();
      page.navigate("http://0.non.existent.domain.for.the.test/target.html");
      assertEquals("Served by the proxy", page.title());
      page.close();
    }
    {
      Page page = context.newPage();
      assertThrows(PlaywrightException.class, () -> page.navigate("http://1.non.existent.domain.for.the.test/target.html"));
      page.close();
    }
    {
      Page page = context.newPage();
      assertThrows(PlaywrightException.class, () -> page.navigate("http://2.non.existent.domain.for.the.test/target.html"));
      page.close();
    }
    {
      Page page = context.newPage();
      assertThrows(PlaywrightException.class, () -> page.navigate("http://foo.is.the.another.test/target.html"));
      page.close();
    }
    {
      Page page = context.newPage();
      page.navigate("http://3.non.existent.domain.for.the.test/target.html");
      assertEquals("Served by the proxy", page.title());
      page.close();
    }
    context.close();
  }

  void shouldUseSocksProxy() {
    // TODO: implement socks server
  }

  void shouldUseSocksProxyInSecondPage() {
    // TODO: implement socks server
  }

  @PlaywrightTest
  void doesLaunchWithoutAPort(Browser browser) {
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setProxy(
      new Proxy("http://localhost")));
    context.close();
  }
}
