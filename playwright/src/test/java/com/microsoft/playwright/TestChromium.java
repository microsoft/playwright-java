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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import static com.microsoft.playwright.Utils.mapOf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnabledIf(value="com.microsoft.playwright.TestBase#isChromium", disabledReason="Chromium-specific API")
public class TestChromium extends TestBase {
  @Override
  void createContextAndPage() {
    // Do not create anything.
  }

  private static int nextPort = 9339;

  private static String wsEndpointFromUrl(String urlString) throws IOException {
    URL url = new URL(urlString);
    URLConnection request = url.openConnection();
    request.connect();
    Reader reader = new InputStreamReader((InputStream) request.getContent());
    JsonObject json = new Gson().fromJson(reader, JsonObject.class);
    return json.get("webSocketDebuggerUrl").getAsString();
  }

  @Test
  void shouldConnectToAnExistingCdpSession() throws IOException {
    int port = nextPort++;
    try (Browser browserServer = getBrowserType().launch(createLaunchOptions()
        .setArgs(asList("--remote-debugging-port=" + port)))) {
      Browser cdpBrowser = getBrowserType().connectOverCDP("http://localhost:" + port);
      List<BrowserContext> contexts = cdpBrowser.contexts();
      assertEquals(1, contexts.size());
      cdpBrowser.close();
    }
  }

  @Test
  void shouldConnectToAnExistingCdpSessionTwice() throws IOException {
    int port = nextPort++;
    try (Browser browserServer = getBrowserType().launch(createLaunchOptions()
        .setArgs(asList("--remote-debugging-port=" + port)))) {
      String endpointUrl = "http://localhost:" + port;
      Browser cdpBrowser1 = getBrowserType().connectOverCDP(endpointUrl);
      Browser cdpBrowser2 = getBrowserType().connectOverCDP(endpointUrl);
      List<BrowserContext> contexts1 = cdpBrowser1.contexts();
      assertEquals(1, contexts1.size());
      Page page1 = contexts1.get(0).newPage();
      page1.navigate(getServer().EMPTY_PAGE);

      List<BrowserContext> contexts2 = cdpBrowser2.contexts();
      assertEquals(1, contexts2.size());
      Page page2 = contexts2.get(0).newPage();
      page2.navigate(getServer().EMPTY_PAGE);

      assertEquals(2, contexts1.get(0).pages().size());
      assertEquals(2, contexts2.get(0).pages().size());

      cdpBrowser1.close();
      cdpBrowser2.close();
    }
  }

  @Test
  void shouldConnectOverAWsEndpoint() throws IOException {
    int port = nextPort++;
    try (Browser browserServer = getBrowserType().launch(createLaunchOptions()
        .setArgs(asList("--remote-debugging-port=" + port)))) {
      String wsEndpoint = wsEndpointFromUrl("http://localhost:" + port + "/json/version/");

      Browser cdpBrowser1 = getBrowserType().connectOverCDP(wsEndpoint);
      List<BrowserContext> contexts1 = cdpBrowser1.contexts();
      assertEquals(1, contexts1.size());
      cdpBrowser1.close();

      Browser cdpBrowser2 = getBrowserType().connectOverCDP(wsEndpoint);
      List<BrowserContext> contexts2 = cdpBrowser2.contexts();
      assertEquals(1, contexts2.size());
      cdpBrowser2.close();
    }
  }

  @Test
  void shouldSendExtraHeadersWithConnectRequest() throws Exception {
    try (WebSocketServerImpl webSocketServer = WebSocketServerImpl.create()) {
      try {
        getBrowserType().connectOverCDP("ws://localhost:" + webSocketServer.getPort() + "/ws",
          new BrowserType.ConnectOverCDPOptions().setHeaders(mapOf("User-Agent", "Playwright", "foo", "bar")));
      } catch (Exception e) {
      }
      assertNotNull(webSocketServer.lastClientHandshake);
      assertEquals("Playwright", webSocketServer.lastClientHandshake.getFieldValue("User-Agent"));
      assertEquals("bar", webSocketServer.lastClientHandshake.getFieldValue("foo"));
    }
  }
}
