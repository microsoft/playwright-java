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

import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Fixture;
import com.microsoft.playwright.fixtures.PlaywrightTest;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.microsoft.playwright.Utils.waitForCondition;
import static com.microsoft.playwright.fixtures.BrowserName.FIREFOX;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class TestWebSocket {
  @Fixture
  WebSocketServerImpl webSocketServer() throws InterruptedException {
    return WebSocketServerImpl.create();
  }

  private void waitForCondition(Page page, boolean[] condition) {
    assertEquals(1, condition.length);
    Instant start = Instant.now();
    while (!condition[0]) {
      page.waitForTimeout(100);
      assertTrue(Duration.between(start, Instant.now()).getSeconds() < 30, "Timed out");
    }
  }

  @PlaywrightTest
  void shouldWork(Page page, WebSocketServerImpl webSocketServer) {
    Object value = page.evaluate("port => {\n" +
      "  let cb;\n" +
      "  const result = new Promise(f => cb = f);\n" +
      "  const ws = new WebSocket('ws://localhost:' + port + '/ws');\n" +
      "  ws.addEventListener('message', data => { ws.close(); cb(data.data); });\n" +
      "  return result;\n" +
      "}", webSocketServer.getPort());
    assertEquals("incoming", value);
  }

  @PlaywrightTest
  void shouldEmitCloseEvents(Page page, WebSocketServerImpl webSocketServer) {
    boolean[] socketClosed = {false};
    List<String> log = new ArrayList<>();
    com.microsoft.playwright.WebSocket[] webSocket = {null};
    page.onWebSocket(ws -> {
      log.add("open<" + ws.url() + ">");
      webSocket[0] = ws;
      ws.onClose(ws1 -> {
        log.add("close");
        socketClosed[0] = true;
      });
    });
    page.evaluate("port => {\n" +
      "  const ws = new WebSocket('ws://localhost:' + port + '/ws');\n" +
      "  ws.addEventListener('open', () => ws.close());\n" +
      "}", webSocketServer.getPort());
    waitForCondition(page, socketClosed);
    assertEquals(asList("open<ws://localhost:" + webSocketServer.getPort() + "/ws>", "close"), log);
    assertTrue(webSocket[0].isClosed());
  }

  @PlaywrightTest
  void shouldEmitFrameEvents(Page page, WebSocketServerImpl webSocketServer) {
    boolean[] socketClosed = {false};
    List<String> log = new ArrayList<>();
    page.onWebSocket(ws -> {
      log.add("open");
      ws.onFrameSent(frameData -> log.add("sent<" + frameData.text() + ">"));
      ws.onFrameReceived(frameData -> log.add("received<" + frameData.text()  + ">"));
      ws.onClose(ws1 -> { log.add("close"); socketClosed[0] = true; });
    });
    page.evaluate("port => {\n" +
      "    const ws = new WebSocket('ws://localhost:' + port + '/ws');\n" +
      "    ws.addEventListener('open', () => ws.send('outgoing'));\n" +
      "    ws.addEventListener('message', () => { ws.close(); });\n" +
      "  }", webSocketServer.getPort());
    waitForCondition(page, socketClosed);
    assertEquals("open", log.get(0), "Events: " + log);
    assertEquals("close", log.get(3), "Events: " + log);
    log.sort(String::compareTo);
    assertEquals(asList("close", "open", "received<incoming>", "sent<outgoing>"), log);
  }

  @PlaywrightTest
  void shouldEmitBinaryFrameEvents(Page page, WebSocketServerImpl webSocketServer) {
    boolean[] socketClosed = {false};
    List<WebSocketFrame> sent = new ArrayList<>();
    page.onWebSocket(ws -> {
      ws.onClose(ws1 -> socketClosed[0] = true);
      ws.onFrameSent(frameData -> sent.add(frameData));
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
    waitForCondition(page, socketClosed);
    assertEquals("text", sent.get(0).text());
    for (int i = 0; i < 5; ++i) {
      assertEquals(i, sent.get(1).binary()[i]);
    }
  }

  @PlaywrightTest
  void shouldEmitError(Page page, Server server, BrowserName browserName) {
    boolean[] socketError = {false};
    String[] error = {null};
    page.onWebSocket(ws -> {
      ws.onSocketError(new Consumer<String>() {
        @Override
        public void accept(String e) {
          ws.offSocketError(this);
          error[0] = e;
          socketError[0] = true;
        }
      });
    });
    page.evaluate("port => {\n" +
      "  new WebSocket('ws://localhost:' + port + '/bogus-ws');\n" +
      "}", server.PORT);
    waitForCondition(page, socketError);
    if (browserName == FIREFOX) {
      assertEquals("CLOSE_ABNORMAL", error[0]);
    } else {
      assertTrue(error[0].contains("404"), error[0]);
    }
  }

  @PlaywrightTest
  void shouldNotHaveStrayErrorEvents(Page page, WebSocketServerImpl webSocketServer) {
    com.microsoft.playwright.WebSocket ws = page.waitForWebSocket(() -> {
        page.evaluate("port => {\n" +
          "  window.ws = new WebSocket('ws://localhost:' + port + '/ws');\n" +
          "}", webSocketServer.getPort());
      });
    boolean[] error = {false};
    ws.onSocketError(e -> error[0] = true);
    ws.waitForFrameReceived(() -> {});
    page.evaluate("window.ws.close()");
    assertFalse(error[0]);
  }

  @PlaywrightTest
  void shouldRejectFutureEventOnSocketClose(Page page, WebSocketServerImpl webSocketServer) {
    com.microsoft.playwright.WebSocket ws = page.waitForWebSocket(() -> {
      page.evaluate("port => {\n" +
        "  window.ws = new WebSocket('ws://localhost:' + port + '/ws');\n" +
        "}", webSocketServer.getPort());
    });
    ws.waitForFrameReceived(() -> {});
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      ws.waitForFrameSent(() -> page.evaluate("window.ws.close()"));
    });
    assertTrue(e.getMessage().matches("Socket closed|Socket error"), e.getMessage());
  }

  @PlaywrightTest
  void shouldRejectFutureEventOnPageClose(Page page, WebSocketServerImpl webSocketServer) {
    com.microsoft.playwright.WebSocket ws = page.waitForWebSocket(() -> {
      page.evaluate("port => {\n" +
        "  window.ws = new WebSocket('ws://localhost:' + port + '/ws');\n" +
        "}", webSocketServer.getPort());
    });
    ws.waitForFrameReceived(() -> {});
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      ws.waitForFrameSent(() -> page.close());
    });
    assertTrue(e.getMessage().contains("Target page, context or browser has been closed"), e.getMessage());
  }

  @PlaywrightTest
  void shouldCallFrameReceivedPredicate(Page page, WebSocketServerImpl webSocketServer) {
    com.microsoft.playwright.WebSocket ws = page.waitForWebSocket(() -> {
      page.evaluate("port => {\n" +
        "  window.ws = new WebSocket('ws://localhost:' + port + '/ws');\n" +
        "}", webSocketServer.getPort());
    });

    String[] text = {null};
    WebSocketFrame frame = ws.waitForFrameReceived(new WebSocket.WaitForFrameReceivedOptions()
        .setPredicate(webSocketFrame -> {
          if (!"incoming".equals(webSocketFrame.text())) {
            return false;
          }
          text[0] = webSocketFrame.text();
          return true;
        }), () -> {});
    assertEquals("incoming", text[0]);
    assertEquals("incoming", frame.text());
  }

  @PlaywrightTest
  void shouldCallFrameSentPredicate(Page page, WebSocketServerImpl webSocketServer) {
    com.microsoft.playwright.WebSocket ws = page.waitForWebSocket(() -> {
      page.evaluate("port => {\n" +
        "  window.ws = new WebSocket('ws://localhost:' + port + '/ws');\n" +
        "  return new Promise(f => ws.addEventListener('open', f));\n" +
        "}", webSocketServer.getPort());
    });

    String[] text = {null};
    WebSocketFrame frame = ws.waitForFrameSent(new WebSocket.WaitForFrameSentOptions()
      .setPredicate(webSocketFrame -> {
        if (!"outgoing".equals(webSocketFrame.text())) {
          return false;
        }
        text[0] = webSocketFrame.text();
        return true;
      }), () -> page.evaluate("ws.send('outgoing');"));
    assertEquals("outgoing", text[0]);
    assertEquals("outgoing", frame.text());
  }

  @PlaywrightTest
  void shouldRespectFrameReceivedTimeout(Page page, WebSocketServerImpl webSocketServer) {
    com.microsoft.playwright.WebSocket ws = page.waitForWebSocket(() -> {
      page.evaluate("port => {\n" +
        "  window.ws = new WebSocket('ws://localhost:' + port + '/ws');\n" +
        "  return new Promise(f => ws.addEventListener('open', f))\n" +
        "}", webSocketServer.getPort());
    });

    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      ws.waitForFrameReceived(new WebSocket.WaitForFrameReceivedOptions()
        .setPredicate(webSocketFrame -> false).setTimeout(1), () -> {});
    });
    assertTrue(e.getMessage().contains("Timeout"), e.getMessage());
  }

  @PlaywrightTest
  void shouldRespectFrameSentTimeout(Page page, WebSocketServerImpl webSocketServer) {
    com.microsoft.playwright.WebSocket ws = page.waitForWebSocket(() -> {
      page.evaluate("port => {\n" +
        "  window.ws = new WebSocket('ws://localhost:' + port + '/ws');\n" +
        "  return new Promise(f => ws.addEventListener('open', f));\n" +
        "}", webSocketServer.getPort());
    });

    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      ws.waitForFrameSent(new WebSocket.WaitForFrameSentOptions()
        .setPredicate(webSocketFrame -> false).setTimeout(1), () -> page.evaluate("ws.send('outgoing');"));
    });
    assertTrue(e.getMessage().contains("Timeout"), e.getMessage());
  }
}
