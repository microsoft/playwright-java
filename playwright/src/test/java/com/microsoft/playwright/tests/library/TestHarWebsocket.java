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

package com.microsoft.playwright.tests.library;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.WebSocket;
import com.microsoft.playwright.WebSocketRoute;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PageWithHar;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestInfo;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.impl.PlaywrightImpl;
import com.microsoft.playwright.options.HarContentPolicy;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/har-websocket.spec.ts. */
public class TestHarWebsocket {
  private static int headersSize(JsonArray headers) {
    int result = 0;
    for (JsonElement element : headers) {
      JsonObject header = element.getAsJsonObject();
      result += header.get("name").getAsString().length() + ": ".length() + header.get("value").getAsString().length() + "\r\n".length();
    }
    return result;
  }

  private static int requestHeadersSize(JsonArray headers) {
    int result = "GET /ws HTTP/1.1\r\n".length();
    result += headersSize(headers);
    return result;
  }

  private static int responseHeadersSize(JsonArray headers) {
    int result = "HTTP/1.1 101 Switching Protocols\r\n".length();
    result += headersSize(headers);
    result += "\r\n".length();
    return result;
  }

  // Browser-reported message times are derived from a wall-clock baseline plus
  // monotonic timestamps in the browser process, so they can drift by a few
  // milliseconds relative to Date.now() in the test process.
  private static final int clockSkewMs = 100;

  private static int messageSize(int length) {
    // The payload is short enough that they only need the minimum frame header size.
    if (length <= 125) {
      return 6 + length;
    }
    // The payload is large enough that additional bytes are needed to represent the payload length.
    if (length < 65536) {
      return 6 + 2 + length;
    }
    return 6 + 8 + length;
  }

  @PlaywrightTest
  @DisplayName("HA-101 should only have one websocket entry")
  void shouldOnlyHaveOneWebsocketEntry(PageWithHar pageWithHar, TestServer server) {
    server.onceWebSocketConnection(ws -> {
      ws.onMessage(message -> ws.close());
    });
    PageWithHar.Result harSession = pageWithHar.create();
    harSession.page.navigate(server.EMPTY_PAGE);
    String wsUrl = "ws://" + server.HOST + "/ws";
    harSession.page.evaluate("url => new Promise(resolve => {\n" +
      "    const ws = new WebSocket(url);\n" +
      "    ws.addEventListener('open', () => ws.send('ping'));\n" +
      "    ws.addEventListener('close', () => resolve());\n" +
      "  })", wsUrl);
    JsonObject log = harSession.getLog();
    List<JsonObject> wsEntries = new ArrayList<>();
    for (JsonElement e : log.getAsJsonArray("entries")) {
      if (e.getAsJsonObject().getAsJsonObject("request").get("url").getAsString().endsWith("://" + server.HOST + "/ws")) {
        wsEntries.add(e.getAsJsonObject());
      }
    }
    assertEquals(1, wsEntries.size());

    JsonObject wsEntry = wsEntries.get(0);
    assertEquals("websocket", wsEntry.get("_resourceType").getAsString());
  }

  @PlaywrightTest
  @DisplayName("HA-102 should include websocket handshake headers and status")
  void shouldIncludeWebsocketHandshakeHeadersAndStatus(PageWithHar pageWithHar, TestServer server, BrowserName browserName) {
    server.onceWebSocketConnection(ws -> {
      ws.onMessage(message -> ws.close());
    });

    PageWithHar.Result harSession = pageWithHar.create();
    harSession.page.navigate(server.EMPTY_PAGE);

    long beforeMs = System.currentTimeMillis();
    String wsUrl = "ws://" + server.HOST + "/ws";
    harSession.page.evaluate("url => new Promise(resolve => {\n" +
      "    const ws = new WebSocket(url);\n" +
      "    ws.addEventListener('open', () => ws.send('ping'));\n" +
      "    ws.addEventListener('close', () => resolve());\n" +
      "  })", wsUrl);
    JsonObject log = harSession.getLog();
    long afterMs = System.currentTimeMillis();

    JsonObject wsEntry = findEntry(log, wsUrl);
    assertEquals("websocket", wsEntry.get("_resourceType").getAsString());
    assertEquals(requestHeadersSize(wsEntry.getAsJsonObject("request").getAsJsonArray("headers")), wsEntry.getAsJsonObject("request").get("headersSize").getAsInt());
    assertEquals(101, wsEntry.getAsJsonObject("response").get("status").getAsInt());
    assertEquals("Switching Protocols", wsEntry.getAsJsonObject("response").get("statusText").getAsString());
    assertEquals(responseHeadersSize(wsEntry.getAsJsonObject("response").getAsJsonArray("headers")), wsEntry.getAsJsonObject("response").get("headersSize").getAsInt());

    long wallTimeMs = OffsetDateTime.parse(wsEntry.get("startedDateTime").getAsString()).toInstant().toEpochMilli();
    assertTrue(wallTimeMs >= beforeMs - clockSkewMs);
    assertTrue(wallTimeMs <= afterMs + clockSkewMs);

    List<String> requestHeaderNames = headerNames(wsEntry.getAsJsonObject("request").getAsJsonArray("headers"));
    assertTrue(requestHeaderNames.contains("upgrade"));
    assertTrue(requestHeaderNames.contains("connection"));
    assertTrue(requestHeaderNames.contains("sec-websocket-key"));
    assertTrue(requestHeaderNames.contains("sec-websocket-version"));
    JsonObject upgradeHeader = findHeader(wsEntry.getAsJsonObject("request").getAsJsonArray("headers"), "upgrade");
    assertEquals("websocket", upgradeHeader.get("value").getAsString().toLowerCase());

    List<String> responseHeaderNames = headerNames(wsEntry.getAsJsonObject("response").getAsJsonArray("headers"));
    assertTrue(responseHeaderNames.contains("upgrade"));
    assertTrue(responseHeaderNames.contains("connection"));
    assertTrue(responseHeaderNames.contains("sec-websocket-accept"));
  }

  private static void testWebSocketMessages(PageWithHar pageWithHar, TestServer server, String content, String channel) throws Exception {
    List<String> incomingText = Arrays.asList(repeat('x', 125), repeat('x', 126), repeat('x', 65536));
    List<List<Integer>> incomingBinary = Arrays.asList(filled(0x01, 125), filled(0x01, 126), filled(0x01, 65536));
    List<String> outgoingText = Arrays.asList(repeat('y', 125), repeat('y', 126), repeat('y', 65536));
    List<List<Integer>> outgoingBinary = Arrays.asList(filled(0x02, 125), filled(0x02, 126), filled(0x02, 65536));
    int incomingCount = incomingText.size() + incomingBinary.size();
    int delayMs = 100;

    server.onceWebSocketConnection(ws -> {
      try {
        for (String text : incomingText) {
          ws.send(text);
          Thread.sleep(delayMs);
        }
        for (List<Integer> binary : incomingBinary) {
          Thread.sleep(delayMs);
          ws.send(toBytes(binary));
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    });

    String outputPath = content.equals("embed") ? null : "test.har.zip";
    PageWithHar.Options options = new PageWithHar.Options().setContent(HarContentPolicy.valueOf(content.toUpperCase()));
    if (outputPath != null) {
      options.setOutputPath(outputPath);
    }
    PageWithHar.Result harSession = pageWithHar.create(options);
    harSession.page.navigate(server.EMPTY_PAGE);

    long beforeMs = System.currentTimeMillis();
    String wsUrl = "ws://" + server.HOST + "/ws";
    harSession.page.evaluate("({ url, incomingCount, outgoingText, outgoingBinary, delayMs }) => new Promise(resolve => {\n" +
      "    let count = 0;\n" +
      "    const ws = new WebSocket(url);\n" +
      "    ws.addEventListener('message', async () => {\n" +
      "      if (++count < incomingCount)\n" +
      "        return;\n" +
      "      for (const text of outgoingText) {\n" +
      "        ws.send(text);\n" +
      "        await new Promise(resolve => window.builtins.setTimeout(resolve, delayMs));\n" +
      "      }\n" +
      "      for (const binary of outgoingBinary) {\n" +
      "        await new Promise(resolve => window.builtins.setTimeout(resolve, delayMs));\n" +
      "        ws.send(new Uint8Array(binary));\n" +
      "      }\n" +
      "      ws.close();\n" +
      "    });\n" +
      "    ws.addEventListener('close', () => resolve());\n" +
      "  })", mapOf("url", wsUrl, "incomingCount", incomingCount, "outgoingText", outgoingText, "outgoingBinary", outgoingBinary, "delayMs", delayMs));
    long afterMs = System.currentTimeMillis();

    Map<String, byte[]> zip = null;
    JsonObject log;
    if (outputPath != null) {
      zip = harSession.getZip();
      log = JsonParser.parseString(new String(zip.get("har.har"), StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("log");
    } else {
      log = harSession.getLog();
    }
    JsonObject wsEntry = findEntry(log, wsUrl);
    int expectedTransferSize = responseHeadersSize(wsEntry.getAsJsonObject("response").getAsJsonArray("headers"));
    for (String text : incomingText) {
      expectedTransferSize += messageSize(text.length());
    }
    for (List<Integer> binary : incomingBinary) {
      expectedTransferSize += messageSize(binary.size());
    }
    assertEquals(expectedTransferSize, wsEntry.getAsJsonObject("response").get("_transferSize").getAsInt());
    assertTrue(wsEntry.get("time").getAsDouble() <= afterMs - beforeMs);

    if (content.equals("omit")) {
      assertNull(wsEntry.getAsJsonObject("response").getAsJsonObject("content").get("_file"));
      return;
    }

    JsonArray messages;
    if (content.equals("attach")) {
      assertNull(wsEntry.get("_webSocketMessages"));
      String file = wsEntry.getAsJsonObject("response").getAsJsonObject("content").get("_file").getAsString();
      assertTrue(Pattern.compile("^[0-9a-f]+\\.jsonl$").matcher(file).find());
      messages = new JsonArray();
      for (String line : new String(zip.get(file), StandardCharsets.UTF_8).split("\n")) {
        if (!line.isEmpty()) {
          messages.add(JsonParser.parseString(line));
        }
      }
    } else {
      messages = wsEntry.getAsJsonArray("_webSocketMessages");
    }
    List<Map<String, Object>> expected = new ArrayList<>();
    for (String m : incomingText) {
      expected.add(mapOf("type", "receive", "opcode", 1, "data", m));
    }
    for (List<Integer> m : incomingBinary) {
      expected.add(mapOf("type", "receive", "opcode", 2, "data", m));
    }
    for (String m : outgoingText) {
      expected.add(mapOf("type", "send", "opcode", 1, "data", m));
    }
    for (List<Integer> m : outgoingBinary) {
      expected.add(mapOf("type", "send", "opcode", 2, "data", m));
    }
    assertEquals(expected, messageSummaries(messages));
    // The WSL VM clock drifts relative to the Windows host clock, so the browser-reported
    // message times cannot be compared against the host wall clock.
    if (!"webkit-wsl".equals(channel)) {
      for (JsonElement m : messages) {
        double time = m.getAsJsonObject().get("time").getAsDouble();
        assertTrue(time >= beforeMs - clockSkewMs);
        assertTrue(time <= afterMs + clockSkewMs);
      }
    }
    assertTrue(messages.get(0).getAsJsonObject().get("time").getAsDouble() <= messages.get(1).getAsJsonObject().get("time").getAsDouble());
    assertTrue(wsEntry.get("time").getAsDouble() >= messages.get(messages.size() - 1).getAsJsonObject().get("time").getAsDouble() - messages.get(0).getAsJsonObject().get("time").getAsDouble());
  }

  @PlaywrightTest
  @DisplayName("HA-103 should embed websocket messages")
  void shouldEmbedWebsocketMessages(PageWithHar pageWithHar, TestServer server) throws Exception {
    testWebSocketMessages(pageWithHar, server, "embed", Env.channel());
  }

  @PlaywrightTest
  @DisplayName("HA-104 should attach websocket messages")
  void shouldAttachWebsocketMessages(PageWithHar pageWithHar, TestServer server) throws Exception {
    testWebSocketMessages(pageWithHar, server, "attach", Env.channel());
  }

  @PlaywrightTest
  @DisplayName("HA-105 should attach websocket messages for a still open websocket after stopping")
  void shouldAttachWebsocketMessagesForAStillOpenWebsocketAfterStopping(PageWithHar pageWithHar, TestServer server) {
    String incomingText = "incoming";
    List<Integer> incomingBinary = Arrays.asList(0x01, 0x02, 0x03, 0x04);
    String outgoingText = "outgoing";
    List<Integer> outgoingBinary = Arrays.asList(0x05, 0x06, 0x07, 0x08);

    server.onceWebSocketConnection(ws -> {
      AtomicInteger count = new AtomicInteger();
      ws.onMessage(message -> {
        if (count.incrementAndGet() < 2) {
          ws.send(incomingText);
        } else {
          ws.send(toBytes(incomingBinary));
        }
      });
    });

    PageWithHar.Result harSession = pageWithHar.create(new PageWithHar.Options().setContent(HarContentPolicy.ATTACH).setOutputPath("test.har.zip"));
    harSession.page.navigate(server.EMPTY_PAGE);

    long beforeMs = System.currentTimeMillis();
    String wsUrl = "ws://" + server.HOST + "/ws";
    // Java: the evaluate() is the callback of waitForWebSocket(); it does not wait for anything.
    WebSocket ws = harSession.page.waitForWebSocket(() -> harSession.page.evaluate("({ url, outgoingText, outgoingBinary }) => {\n" +
      "    const ws = new WebSocket(url);\n" +
      "    window.ws = ws;\n" +
      "    let count = 0;\n" +
      "    ws.addEventListener('open', () => ws.send(outgoingText));\n" +
      "    ws.addEventListener('message', () => {\n" +
      "      if (++count < 2)\n" +
      "        ws.send(new Uint8Array(outgoingBinary));\n" +
      "    });\n" +
      "  }", mapOf("url", wsUrl, "outgoingText", outgoingText, "outgoingBinary", outgoingBinary)));
    // Wait for all frames so the HAR tracer has observed them before the context is closed.
    ws.waitForFrameSent(() -> {});
    ws.waitForFrameReceived(() -> {});
    ws.waitForFrameSent(() -> {});
    ws.waitForFrameReceived(() -> {});
    long afterMs = System.currentTimeMillis();

    // Do not close the WebSocket on the page side. Closing the context should still flush messages.
    assertEquals(1 /* OPEN */, harSession.page.evaluate("() => window.ws.readyState"));

    Map<String, byte[]> zip = harSession.getZip();
    JsonObject log = JsonParser.parseString(new String(zip.get("har.har"), StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("log");

    JsonObject wsEntry = findEntry(log, wsUrl);
    assertEquals(responseHeadersSize(wsEntry.getAsJsonObject("response").getAsJsonArray("headers")) + messageSize(incomingText.length()) + messageSize(incomingBinary.size()),
      wsEntry.getAsJsonObject("response").get("_transferSize").getAsInt());
    assertTrue(wsEntry.get("time").getAsDouble() <= afterMs - beforeMs);
    assertNull(wsEntry.get("_webSocketMessages"));

    String file = wsEntry.getAsJsonObject("response").getAsJsonObject("content").get("_file").getAsString();
    assertTrue(Pattern.compile("^[0-9a-f]+\\.jsonl$").matcher(file).find());

    JsonArray messages = new JsonArray();
    for (String line : new String(zip.get(file), StandardCharsets.UTF_8).split("\n")) {
      if (!line.isEmpty()) {
        messages.add(JsonParser.parseString(line));
      }
    }
    assertEquals(Arrays.asList(
      mapOf("type", "send", "opcode", 1, "data", outgoingText),
      mapOf("type", "receive", "opcode", 1, "data", incomingText),
      mapOf("type", "send", "opcode", 2, "data", outgoingBinary),
      mapOf("type", "receive", "opcode", 2, "data", incomingBinary)), messageSummaries(messages));
    // The WSL VM clock drifts relative to the Windows host clock, so the browser-reported
    // message times cannot be compared against the host wall clock.
    if (!"webkit-wsl".equals(Env.channel())) {
      for (JsonElement m : messages) {
        double time = m.getAsJsonObject().get("time").getAsDouble();
        assertTrue(time >= beforeMs - clockSkewMs);
        assertTrue(time <= afterMs + clockSkewMs);
      }
    }
    assertTrue(messages.get(0).getAsJsonObject().get("time").getAsDouble() <= messages.get(1).getAsJsonObject().get("time").getAsDouble());
    assertTrue(wsEntry.get("time").getAsDouble() >= messages.get(messages.size() - 1).getAsJsonObject().get("time").getAsDouble() - messages.get(0).getAsJsonObject().get("time").getAsDouble());
  }

  @PlaywrightTest
  @DisplayName("HA-106 should omit websocket messages")
  void shouldOmitWebsocketMessages(PageWithHar pageWithHar, TestServer server) throws Exception {
    testWebSocketMessages(pageWithHar, server, "omit", null);
  }

  @PlaywrightTest
  @DisplayName("HA-107 should record websocket connection failure")
  void shouldRecordWebsocketConnectionFailure(PageWithHar pageWithHar, TestServer server) throws IOException {
    assumeFalse("webkit-wsl".equals(Env.channel()), "Connection to an unbound localhost port from WSL is not refused in mirrored networking mode");
    // Reserve a port and immediately release it so the WebSocket connect attempt is refused.
    ServerSocket portReservation = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
    int port = portReservation.getLocalPort();
    portReservation.close();

    PageWithHar.Result harSession = pageWithHar.create();
    harSession.page.navigate(server.EMPTY_PAGE);

    String wsUrl = "ws://127.0.0.1:" + port + "/ws-connect-fail";
    harSession.page.waitForWebSocket(() -> harSession.page.evaluate("url => new Promise(resolve => {\n" +
      "    const ws = new WebSocket(url);\n" +
      "    ws.addEventListener('close', () => resolve());\n" +
      "    ws.addEventListener('error', () => resolve());\n" +
      "  })", wsUrl));
    JsonObject log = harSession.getLog();

    JsonObject wsEntry = findEntry(log, wsUrl);
    assertEquals("websocket", wsEntry.get("_resourceType").getAsString());
    assertTrue(truthy(wsEntry.getAsJsonObject("response").get("_failureText")));
  }

  @PlaywrightTest
  @DisplayName("HA-108 should record websocket handshake failure")
  void shouldRecordWebsocketHandshakeFailure(PageWithHar pageWithHar, TestServer server, BrowserName browserName) throws Exception {
    PageWithHar.Result harSession = pageWithHar.create();
    harSession.page.navigate(server.EMPTY_PAGE);

    String wsUrl = "ws://" + server.HOST + "/ws-handshake-fail";
    CompletableFuture<TestServer.UpgradeActions> upgradePromise = server.waitForUpgrade();
    // Java: the server answers the upgrade in a callback that runs on its own thread while the evaluate() waits for the close of the WebSocket.
    upgradePromise.thenAccept(upgrade -> {
      upgrade.socket.write("HTTP/1.1 403 Forbidden\r\nContent-Length: 0\r\n\r\n");
      upgrade.socket.destroy();
    });
    harSession.page.evaluate("url => new Promise(resolve => {\n" +
      "    const ws = new WebSocket(url);\n" +
      "    ws.addEventListener('close', () => resolve());\n" +
      "    ws.addEventListener('error', () => resolve());\n" +
      "  })", wsUrl);
    JsonObject log = harSession.getLog();

    JsonObject wsEntry = findEntry(log, wsUrl);
    assertEquals("websocket", wsEntry.get("_resourceType").getAsString());
    if (browserName != BrowserName.CHROMIUM) {
      // Chromium only reports an error instead of giving a status code and text.
      assertEquals(403, wsEntry.getAsJsonObject("response").get("status").getAsInt());
      assertEquals("Forbidden", wsEntry.getAsJsonObject("response").get("statusText").getAsString());
    }
    assertTrue(truthy(wsEntry.getAsJsonObject("response").get("_failureText")));
  }

  @PlaywrightTest
  @DisplayName("HA-109 should still capture websocket when route passes messages through")
  void shouldStillCaptureWebsocketWhenRoutePassesMessagesThrough(PageWithHar pageWithHar, TestServer server) {
    server.onceWebSocketConnection(ws -> {
      ws.onMessage(message -> ws.send("incoming"));
    });

    PageWithHar.Result harSession = pageWithHar.create();
    AtomicInteger routeHandlerCalled = new AtomicInteger();
    harSession.page.routeWebSocket(Pattern.compile("/ws$"), ws -> {
      routeHandlerCalled.incrementAndGet();
      WebSocketRoute serverRoute = ws.connectToServer();
      ws.onMessage(message -> serverRoute.send(message.text()));
      serverRoute.onMessage(message -> ws.send(message.text()));
    });
    harSession.page.navigate(server.EMPTY_PAGE);

    String wsUrl = "ws://" + server.HOST + "/ws";
    Object messages = harSession.page.evaluate("url => new Promise(resolve => {\n" +
      "    const seen = [];\n" +
      "    const ws = new WebSocket(url);\n" +
      "    ws.addEventListener('open', () => ws.send('outgoing'));\n" +
      "    ws.addEventListener('message', event => {\n" +
      "      seen.push(event.data);\n" +
      "      ws.close();\n" +
      "    });\n" +
      "    ws.addEventListener('close', () => resolve(seen));\n" +
      "  })", wsUrl);
    assertEquals(1, routeHandlerCalled.get());
    assertEquals(Arrays.asList("incoming"), messages);
    JsonObject log = harSession.getLog();

    List<JsonObject> wsEntries = findEntries(log, wsUrl);
    assertEquals(1, wsEntries.size());
    assertEquals("websocket", wsEntries.get(0).get("_resourceType").getAsString());
    assertEquals(101, wsEntries.get(0).getAsJsonObject("response").get("status").getAsInt());
    assertEquals(Arrays.asList(
      mapOf("type", "send", "data", "outgoing"),
      mapOf("type", "receive", "data", "incoming")), typeAndData(wsEntries.get(0).getAsJsonArray("_webSocketMessages")));
  }

  @PlaywrightTest
  @DisplayName("HA-110 should still allow routeWebSocket to fully mock the connection when capturing HAR")
  void shouldStillAllowRouteWebSocketToFullyMockTheConnectionWhenCapturingHAR(PageWithHar pageWithHar, TestServer server) {
    PageWithHar.Result harSession = pageWithHar.create();
    AtomicInteger routeHandlerCalled = new AtomicInteger();
    harSession.page.routeWebSocket(Pattern.compile("/ws$"), ws -> {
      routeHandlerCalled.incrementAndGet();
      ws.onMessage(message -> {
        if ("ping".equals(message.text())) {
          ws.send("pong");
        }
      });
    });
    harSession.page.navigate(server.EMPTY_PAGE);

    String wsUrl = "ws://" + server.HOST + "/ws";
    Object messages = harSession.page.evaluate("url => new Promise(resolve => {\n" +
      "    const seen = [];\n" +
      "    const ws = new WebSocket(url);\n" +
      "    ws.addEventListener('open', () => ws.send('ping'));\n" +
      "    ws.addEventListener('message', event => {\n" +
      "      seen.push(event.data);\n" +
      "      ws.close();\n" +
      "    });\n" +
      "    ws.addEventListener('close', () => resolve(seen));\n" +
      "  })", wsUrl);
    assertEquals(1, routeHandlerCalled.get());
    assertEquals(Arrays.asList("pong"), messages);
    JsonObject log = harSession.getLog();

    // Fully mocked routes never create a native WebSocket, so nothing should be recorded.
    List<JsonObject> wsEntries = findEntries(log, wsUrl);
    assertEquals(Collections.emptyList(), wsEntries);
  }

  @PlaywrightTest
  @DisplayName("HA-111 should still allow routeWebSocket to modify messages when capturing HAR")
  void shouldStillAllowRouteWebSocketToModifyMessagesWhenCapturingHAR(PageWithHar pageWithHar, TestServer server) {
    server.onceWebSocketConnection(ws -> {
      ws.onMessage(message -> ws.send("server-saw-" + message.text()));
    });

    PageWithHar.Result harSession = pageWithHar.create();
    AtomicInteger routeHandlerCalled = new AtomicInteger();
    harSession.page.routeWebSocket(Pattern.compile("/ws$"), ws -> {
      routeHandlerCalled.incrementAndGet();
      WebSocketRoute serverRoute = ws.connectToServer();
      ws.onMessage(message -> serverRoute.send("modified-" + message.text()));
      serverRoute.onMessage(message -> ws.send("page-got-" + message.text()));
    });
    harSession.page.navigate(server.EMPTY_PAGE);

    String wsUrl = "ws://" + server.HOST + "/ws";
    Object messages = harSession.page.evaluate("url => new Promise(resolve => {\n" +
      "    const seen = [];\n" +
      "    const ws = new WebSocket(url);\n" +
      "    ws.addEventListener('open', () => ws.send('hello'));\n" +
      "    ws.addEventListener('message', event => {\n" +
      "      seen.push(event.data);\n" +
      "      ws.close();\n" +
      "    });\n" +
      "    ws.addEventListener('close', () => resolve(seen));\n" +
      "  })", wsUrl);
    assertEquals(1, routeHandlerCalled.get());
    // The page sees the route-modified server response.
    assertEquals(Arrays.asList("page-got-server-saw-modified-hello"), messages);
    JsonObject log = harSession.getLog();

    // HAR records actual wire traffic from the native WebSocket: outgoing messages
    // are modified by the client-side route handler before they hit the server,
    // and incoming messages are recorded before the server-side route handler modifies them.
    List<JsonObject> wsEntries = findEntries(log, wsUrl);
    assertEquals(1, wsEntries.size());
    assertEquals(Arrays.asList(
      mapOf("type", "send", "data", "modified-hello"),
      mapOf("type", "receive", "data", "server-saw-modified-hello")), typeAndData(wsEntries.get(0).getAsJsonArray("_webSocketMessages")));
  }

  @PlaywrightTest
  @DisplayName("HA-112 should respect PLAYWRIGHT_HAR_NO_WEBSOCKET_FRAMES")
  void shouldRespectPLAYWRIGHTHARNOWEBSOCKETFRAMES(TestServer server, BrowserName browserName, TestInfo testInfo) throws IOException {
    // Java: upstream sets the variable in the test process, which also runs the Playwright server.
    // The driver of the Java client is another process that reads the variable when it starts, so the
    // test starts a driver with the variable set and records the HAR in a browser of that driver.
    Map<String, String> env = new HashMap<>();
    env.put("PWTEST_UNDER_TEST", "1");
    env.put("PLAYWRIGHT_HAR_NO_WEBSOCKET_FRAMES", "1");
    server.onceWebSocketConnection(ws -> {
      ws.onMessage(message -> ws.close());
    });
    Path harPath = testInfo.outputPath("test.har");
    Playwright playwright = PlaywrightImpl.createImpl(new Playwright.CreateOptions().setEnv(env), true);
    try {
      Browser browser = browserName.browserType(playwright).launch(Env.launchOptions());
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setRecordHarPath(harPath).setIgnoreHTTPSErrors(true));
      Page page = context.newPage();
      page.navigate(server.EMPTY_PAGE);
      String wsUrl = "ws://" + server.HOST + "/ws";
      page.evaluate("url => new Promise(resolve => {\n" +
        "    const ws = new WebSocket(url);\n" +
        "    ws.addEventListener('open', () => ws.send('ping'));\n" +
        "    ws.addEventListener('close', () => resolve());\n" +
        "  })", wsUrl);
      context.close();
      browser.close();
    } finally {
      playwright.close();
    }
    JsonObject log = JsonParser.parseString(new String(Files.readAllBytes(harPath), StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonObject("log");
    JsonObject wsEntry = null;
    for (JsonElement e : log.getAsJsonArray("entries")) {
      if (wsEntry == null && e.getAsJsonObject().getAsJsonObject("request").get("url").getAsString().endsWith("://" + server.HOST + "/ws")) {
        wsEntry = e.getAsJsonObject();
      }
    }
    assertEquals("websocket", wsEntry.get("_resourceType").getAsString());
    assertNull(wsEntry.get("_webSocketMessages"));
    assertNull(wsEntry.getAsJsonObject("response").getAsJsonObject("content").get("_file"));
  }

  private static String repeat(char c, int count) {
    char[] chars = new char[count];
    Arrays.fill(chars, c);
    return new String(chars);
  }

  private static List<Integer> filled(int value, int count) {
    return new ArrayList<>(Collections.nCopies(count, value));
  }

  private static byte[] toBytes(List<Integer> values) {
    byte[] bytes = new byte[values.size()];
    for (int i = 0; i < bytes.length; i++) {
      bytes[i] = (byte) (int) values.get(i);
    }
    return bytes;
  }

  /** messages.map(m => ({ type: m.type, opcode: m.opcode, data: m.opcode === 1 ? m.data : [...Buffer.from(m.data, 'base64')] })) */
  private static List<Map<String, Object>> messageSummaries(JsonArray messages) {
    List<Map<String, Object>> result = new ArrayList<>();
    for (JsonElement element : messages) {
      JsonObject m = element.getAsJsonObject();
      int opcode = m.get("opcode").getAsInt();
      Object data;
      if (opcode == 1) {
        data = m.get("data").getAsString();
      } else {
        List<Integer> bytes = new ArrayList<>();
        for (byte b : java.util.Base64.getDecoder().decode(m.get("data").getAsString())) {
          bytes.add(b & 0xff);
        }
        data = bytes;
      }
      result.add(mapOf("type", m.get("type").getAsString(), "opcode", opcode, "data", data));
    }
    return result;
  }

  /** messages.map(m => ({ type: m.type, data: m.data })) */
  private static List<Map<String, Object>> typeAndData(JsonArray messages) {
    List<Map<String, Object>> result = new ArrayList<>();
    for (JsonElement element : messages) {
      JsonObject m = element.getAsJsonObject();
      result.add(mapOf("type", m.get("type").getAsString(), "data", m.get("data").getAsString()));
    }
    return result;
  }

  /** log.entries.find(e => e.request.url === url) */
  private static JsonObject findEntry(JsonObject log, String url) {
    List<JsonObject> entries = findEntries(log, url);
    return entries.isEmpty() ? null : entries.get(0);
  }

  /** log.entries.filter(e => e.request.url === url) */
  private static List<JsonObject> findEntries(JsonObject log, String url) {
    List<JsonObject> result = new ArrayList<>();
    for (JsonElement e : log.getAsJsonArray("entries")) {
      if (e.getAsJsonObject().getAsJsonObject("request").get("url").getAsString().equals(url)) {
        result.add(e.getAsJsonObject());
      }
    }
    return result;
  }

  private static List<String> headerNames(JsonArray headers) {
    List<String> result = new ArrayList<>();
    for (JsonElement h : headers) {
      result.add(h.getAsJsonObject().get("name").getAsString().toLowerCase());
    }
    return result;
  }

  private static JsonObject findHeader(JsonArray headers, String name) {
    for (JsonElement h : headers) {
      if (h.getAsJsonObject().get("name").getAsString().toLowerCase().equals(name)) {
        return h.getAsJsonObject();
      }
    }
    return null;
  }

  private static boolean truthy(JsonElement value) {
    if (value == null || value.isJsonNull()) {
      return false;
    }
    if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
      return !value.getAsString().isEmpty();
    }
    return true;
  }
}
