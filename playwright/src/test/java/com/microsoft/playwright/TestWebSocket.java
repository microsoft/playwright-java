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

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft;
import org.java_websocket.exceptions.InvalidDataException;
import org.java_websocket.framing.Framedata;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshakeBuilder;
import org.java_websocket.server.WebSocketServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.microsoft.playwright.Page.EventType.WEBSOCKET;
import static com.microsoft.playwright.WebSocket.EventType.*;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class TestWebSocket extends TestBase {
  private static WebSocketServerImpl webSocketServer;
  private static int WS_SERVER_PORT = 8910;

  private static class WebSocketServerImpl extends WebSocketServer {
    WebSocketServerImpl(InetSocketAddress address) {
      super(address, 1);
    }

    @Override
    public void onOpen(org.java_websocket.WebSocket webSocket, ClientHandshake clientHandshake) {
      webSocket.send("incoming");
    }

    @Override
    public void onClose(org.java_websocket.WebSocket webSocket, int i, String s, boolean b) { }

    @Override
    public void onMessage(org.java_websocket.WebSocket webSocket, String s) { }

    @Override
    public void onError(WebSocket webSocket, Exception e) { }

    @Override
    public void onStart() { }
  }

  @BeforeAll
  static void startWebSockerServer() {
    webSocketServer = new WebSocketServerImpl(new InetSocketAddress("localhost", WS_SERVER_PORT));
    new Thread(webSocketServer).start();
  }

  @AfterAll
  static void stopWebSockerServer() throws IOException, InterruptedException {
    webSocketServer.stop();
  }


  private void waitForCondition(boolean[] condition) {
    assertEquals(1, condition.length);
    Instant start = Instant.now();
    while (!condition[0]) {
      page.waitForTimeout(100).get();
      assertTrue(Duration.between(start, Instant.now()).getSeconds() < 30, "Timed out");
    }
  }

  @Test
  void shouldWork() {
    Object value = page.evaluate("port => {\n" +
      "  let cb;\n" +
      "  const result = new Promise(f => cb = f);\n" +
      "  const ws = new WebSocket('ws://localhost:' + port + '/ws');\n" +
      "  ws.addEventListener('message', data => { ws.close(); cb(data.data); });\n" +
      "  return result;\n" +
      "}", webSocketServer.getPort());
    assertEquals("incoming", value);
  }

  @Test
  void shouldEmitCloseEvents() {
    boolean[] socketClosed = {false};
    List<String> log = new ArrayList<>();
    com.microsoft.playwright.WebSocket[] webSocket = {null};
    page.addListener(WEBSOCKET, event -> {
      com.microsoft.playwright.WebSocket ws = (com.microsoft.playwright.WebSocket) event.data();
      log.add("open<" + ws.url() + ">");
      webSocket[0] = ws;
      ws.addListener(com.microsoft.playwright.WebSocket.EventType.CLOSE, closeEvent -> {
        log.add("close");
        socketClosed[0] = true;
      });
    });
    page.evaluate("port => {\n" +
      "  const ws = new WebSocket('ws://localhost:' + port + '/ws');\n" +
      "  ws.addEventListener('open', () => ws.close());\n" +
      "}", webSocketServer.getPort());
    waitForCondition(socketClosed);
    assertEquals(asList("open<ws://localhost:" + webSocketServer.getPort() + "/ws>", "close"), log);
    assertTrue(webSocket[0].isClosed());
  }

  @Test
  void shouldEmitFrameEvents() {
    boolean[] socketClosed = {false};
    List<String> log = new ArrayList<>();
    page.addListener(WEBSOCKET, event -> {
      com.microsoft.playwright.WebSocket ws = (com.microsoft.playwright.WebSocket) event.data();
      log.add("open");
      ws.addListener(FRAMESENT, e -> log.add("sent<" + ((com.microsoft.playwright.WebSocket.FrameData) e.data()).text() + ">"));
      ws.addListener(FRAMERECEIVED, e -> log.add("received<" + ((com.microsoft.playwright.WebSocket.FrameData) e.data()).text()  + ">"));
      ws.addListener(CLOSE, e -> { log.add("close"); socketClosed[0] = true; });
    });
    page.evaluate("port => {\n" +
      "    const ws = new WebSocket('ws://localhost:' + port + '/ws');\n" +
      "    ws.addEventListener('open', () => ws.send('outgoing'));\n" +
      "    ws.addEventListener('message', () => { ws.close(); });\n" +
      "  }", webSocketServer.getPort());
    waitForCondition(socketClosed);
    if (isWebKit()) {
      // TODO: there is intermittent <received<A+g=> message in WebKit.
      log.remove("received<A+g=>");
    }
    assertEquals("open", log.get(0), "Events: " + log);
    assertEquals("close", log.get(3), "Events: " + log);
    log.sort(String::compareTo);
    assertEquals(asList("close", "open", "received<incoming>", "sent<outgoing>"), log);
  }

  @Test
  void shouldEmitBinaryFrameEvents() {
    boolean[] socketClosed = {false};
    List<com.microsoft.playwright.WebSocket.FrameData> sent = new ArrayList<>();
    page.addListener(WEBSOCKET, event -> {
      com.microsoft.playwright.WebSocket ws = (com.microsoft.playwright.WebSocket) event.data();
      ws.addListener(CLOSE, e -> { socketClosed[0] = true; });
      ws.addListener(FRAMESENT, e -> sent.add((com.microsoft.playwright.WebSocket.FrameData) e.data()));
    });
    page.evaluate("port => {\n" +
      "  const ws = new WebSocket('ws://localhost:' + port + '/ws');\n" +
      "  ws.addEventListener('open', () => {\n" +
      "    const binary = new Uint8Array(5);\n" +
      "    for (let i = 0; i < 5; ++i)\n" +
      "      binary[i] = i;\n" +
      "    ws.send('text');\n" +
      "    ws.send(binary);\n" +
      "    ws.close();\n" +
      "  });\n" +
      "}", webSocketServer.getPort());
    waitForCondition(socketClosed);
    assertEquals("text", sent.get(0).text());
    for (int i = 0; i < 5; ++i) {
      assertEquals(i, sent.get(1).body()[i]);
    }
  }

  @Test
  void shouldEmitError() {
    boolean[] socketError = {false};
    String[] error = {null};
    page.addListener(WEBSOCKET, event -> {
      com.microsoft.playwright.WebSocket ws = (com.microsoft.playwright.WebSocket) event.data();
      ws.addListener(SOCKETERROR, e -> {
        error[0] = (String) e.data();
        socketError[0] = true;
      });
    });
    page.evaluate("port => {\n" +
      "  new WebSocket('ws://localhost:' + port + '/bogus-ws');\n" +
      "}", server.PORT);
    waitForCondition(socketError);
    if (isFirefox()) {
      assertEquals("CLOSE_ABNORMAL", error[0]);
    } else {
      assertTrue(error[0].contains("404"), error[0]);
    }
  }

  @Test
  void shouldNotHaveStrayErrorEvents() {
    Deferred<Event<Page.EventType>> wsEvent = page.waitForEvent(WEBSOCKET);
    page.evaluate("port => {\n" +
      "  window.ws = new WebSocket('ws://localhost:' + port + '/ws');\n" +
      "}", webSocketServer.getPort());

    com.microsoft.playwright.WebSocket ws = (com.microsoft.playwright.WebSocket) wsEvent.get().data();
    boolean[] error = {false};
    ws.addListener(SOCKETERROR, e -> error[0] = true);
    Deferred<Event<com.microsoft.playwright.WebSocket.EventType>> frameReceivedEvent = ws.waitForEvent(FRAMERECEIVED);
    frameReceivedEvent.get();
    System.out.println("will close");
    page.evaluate("window.ws.close()");
    assertFalse(error[0]);
  }

  @Test
  void shouldRejectWaitForEventOnSocketClose() {
    Deferred<Event<Page.EventType>> wsEvent = page.waitForEvent(WEBSOCKET);
    page.evaluate("port => {\n" +
      "  window.ws = new WebSocket('ws://localhost:' + port + '/ws');\n" +
      "}", webSocketServer.getPort());

    com.microsoft.playwright.WebSocket ws = (com.microsoft.playwright.WebSocket) wsEvent.get().data();
    ws.waitForEvent(FRAMERECEIVED).get();
    Deferred<Event<com.microsoft.playwright.WebSocket.EventType>> frameSentEvent = ws.waitForEvent(FRAMESENT);
    page.evaluate("window.ws.close()");
    try {
      frameSentEvent.get();
      fail("did not throw");
    } catch (PlaywrightException exception) {
      assertTrue(exception.getMessage().contains("Socket closed"));
    }
  }

  @Test
  void shouldRejectWaitForEventOnPageClose() {
    Deferred<Event<Page.EventType>> wsEvent = page.waitForEvent(WEBSOCKET);
    page.evaluate("port => {\n" +
      "  window.ws = new WebSocket('ws://localhost:' + port + '/ws');\n" +
      "}", webSocketServer.getPort());

    com.microsoft.playwright.WebSocket ws = (com.microsoft.playwright.WebSocket) wsEvent.get().data();
    ws.waitForEvent(FRAMERECEIVED).get();
    Deferred<Event<com.microsoft.playwright.WebSocket.EventType>> frameSentEvent = ws.waitForEvent(FRAMESENT);
    page.close();
    try {
      frameSentEvent.get();
      fail("did not throw");
    } catch (PlaywrightException exception) {
      assertTrue(exception.getMessage().contains("Page closed"));
    }
  }
}
