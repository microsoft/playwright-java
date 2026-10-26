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

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.WebSocket;
import com.microsoft.playwright.WebSocketFrame;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.ServerWebSocket;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;


import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.microsoft.playwright.fixtures.Utils.browserMajorVersion;
import static java.util.Collections.singletonMap;
import static com.microsoft.playwright.fixtures.Utils.kTargetClosedErrorMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Port of tests/library/web-socket.spec.ts. */
public class TestWebSocket {
  // Java: Promise.all([page.waitForEvent('websocket').then(ws => ws.waitForEvent('framereceived')), action()]).
  // Like the .then() upstream, it listens to the frames of the WebSocket as soon as the page reports it.
  private static WebSocket waitForWebSocketFrameReceived(Page page, Runnable action) {
    CompletableFuture<WebSocket> frameReceived = new CompletableFuture<>();
    page.onWebSocket(new Consumer<WebSocket>() {
      @Override
      public void accept(WebSocket ws) {
        page.offWebSocket(this);
        ws.onFrameReceived(frame -> frameReceived.complete(ws));
      }
    });
    action.run();
    page.waitForCondition(frameReceived::isDone);
    return frameReceived.join();
  }

  @PlaywrightTest
  @DisplayName("WS-001 should work @smoke")
  void shouldWork(Page page, TestServer server) {
    server.sendOnWebSocketConnection("incoming");
    Object value = page.evaluate("host => {\n" +
      "    let cb;\n" +
      "    const result = new Promise(f => cb = f);\n" +
      "    const ws = new WebSocket('ws://' + host + '/ws');\n" +
      "    ws.addEventListener('message', data => { ws.close(); cb(data.data); });\n" +
      "    return result;\n" +
      "  }", server.HOST);
    assertEquals("incoming", value);
  }

  @PlaywrightTest
  @DisplayName("WS-002 should emit close events")
  void shouldEmitCloseEvents(Page page, TestServer server) {
    CompletableFuture<Void> socketClosePromise = new CompletableFuture<>();
    List<String> log = new CopyOnWriteArrayList<>();
    AtomicReference<WebSocket> webSocket = new AtomicReference<>();
    page.onWebSocket(ws -> {
      log.add("open<" + ws.url() + ">");
      webSocket.set(ws);
      ws.onClose(w -> {
        log.add("close");
        socketClosePromise.complete(null);
      });
    });
    page.evaluate("host => {\n" +
      "    const ws = new WebSocket('ws://' + host + '/ws');\n" +
      "    ws.addEventListener('open', () => ws.close());\n" +
      "  }", server.HOST);
    page.waitForCondition(socketClosePromise::isDone);
    assertEquals("open<ws://" + server.HOST + "/ws>:close", String.join(":", log));
    assertTrue(webSocket.get().isClosed());
  }

  @PlaywrightTest
  @DisplayName("WS-003 should emit frame events")
  void shouldEmitFrameEvents(Page page, TestServer server) {
    server.onceWebSocketConnection(ws -> {
      ws.onMessage(message -> ws.send("incoming"));
    });
    CompletableFuture<Void> socketClosePromise = new CompletableFuture<>();
    List<String> log = new CopyOnWriteArrayList<>();
    page.onWebSocket(ws -> {
      log.add("open");
      ws.onFrameSent(d -> log.add("sent<" + d.text() + ">"));
      ws.onFrameReceived(d -> log.add("received<" + d.text() + ">"));
      ws.onClose(w -> {
        log.add("close");
        socketClosePromise.complete(null);
      });
    });
    page.evaluate("host => {\n" +
      "    const ws = new WebSocket('ws://' + host + '/ws');\n" +
      "    ws.addEventListener('open', () => ws.send('outgoing'));\n" +
      "    ws.addEventListener('message', () => { ws.close(); });\n" +
      "    window.ws = ws;\n" +
      "  }", server.HOST);
    page.waitForCondition(socketClosePromise::isDone);
    assertEquals(Arrays.asList("open", "sent<outgoing>", "received<incoming>", "close"), log);
  }

  @PlaywrightTest
  @DisplayName("WS-004 should filter out the close events when the server closes with a message")
  void shouldFilterOutTheCloseEventsWhenTheServerClosesWithAMessage(Page page, TestServer server) {
    server.onceWebSocketConnection(ws -> {
      ws.send("incoming");
      ws.onMessage(message -> ws.close(1003, "closed by Playwright test-server"));
    });
    CompletableFuture<Void> socketClosePromise = new CompletableFuture<>();
    List<String> log = new CopyOnWriteArrayList<>();
    page.onWebSocket(ws -> {
      log.add("open");
      ws.onFrameSent(d -> log.add("sent<" + d.text() + ">"));
      ws.onFrameReceived(d -> log.add("received<" + d.text() + ">"));
      ws.onClose(w -> {
        log.add("close");
        socketClosePromise.complete(null);
      });
    });
    page.evaluate("host => {\n" +
      "    const ws = new WebSocket('ws://' + host + '/ws');\n" +
      "    ws.addEventListener('message', () => ws.send('outgoing'));\n" +
      "    window.ws = ws;\n" +
      "  }", server.HOST);
    page.waitForCondition(socketClosePromise::isDone);
    assertEquals(Arrays.asList("open", "received<incoming>", "sent<outgoing>", "close"), log);
  }

  @PlaywrightTest
  @DisplayName("WS-005 should pass self as argument to close event")
  void shouldPassSelfAsArgumentToCloseEvent(Page page, TestServer server) {
    CompletableFuture<WebSocket> socketClosePromise = new CompletableFuture<>();
    AtomicReference<WebSocket> webSocket = new AtomicReference<>();
    page.onWebSocket(ws -> {
      webSocket.set(ws);
      ws.onClose(socketClosePromise::complete);
    });
    page.evaluate("host => {\n" +
      "    const ws = new WebSocket('ws://' + host + '/ws');\n" +
      "    ws.addEventListener('open', () => ws.close());\n" +
      "  }", server.HOST);
    page.waitForCondition(socketClosePromise::isDone);
    WebSocket eventArg = socketClosePromise.join();
    assertSame(webSocket.get(), eventArg);
  }

  @PlaywrightTest
  @DisplayName("WS-006 should emit binary frame events")
  void shouldEmitBinaryFrameEvents(Page page, TestServer server) {
    CompletableFuture<WebSocket> donePromise = new CompletableFuture<>();
    List<WebSocketFrame> sent = new CopyOnWriteArrayList<>();
    page.onWebSocket(ws -> {
      ws.onClose(donePromise::complete);
      ws.onFrameSent(d -> sent.add(d));
    });
    page.evaluate("host => {\n" +
      "    const ws = new WebSocket('ws://' + host + '/ws');\n" +
      "    ws.addEventListener('open', () => {\n" +
      "      const binary = new Uint8Array(5);\n" +
      "      for (let i = 0; i < 5; ++i)\n" +
      "        binary[i] = i;\n" +
      "      ws.send('text');\n" +
      "      ws.send(binary);\n" +
      "      ws.close();\n" +
      "    });\n" +
      "  }", server.HOST);
    page.waitForCondition(donePromise::isDone);
    // Java: the payload of a text frame is text(), the payload of a binary frame is binary().
    assertEquals("text", sent.get(0).text());
    for (int i = 0; i < 5; ++i)
      assertEquals(i, sent.get(1).binary()[i]);
  }

  @PlaywrightTest
  @DisplayName("WS-007 should emit error")
  void shouldEmitError(Page page, TestServer server) {
    CompletableFuture<String> result = new CompletableFuture<>();
    page.onWebSocket(ws -> ws.onSocketError(result::complete));
    page.evaluate("host => {\n" +
      "    new WebSocket('ws://' + host + '/bogus-ws');\n" +
      "  }", server.HOST);
    page.waitForCondition(result::isDone);
    String message = result.join();
    assertTrue(message.contains(Env.channel() != null && Env.channel().contains("msedge") ? "" : ": 400"));
  }

  @PlaywrightTest
  @DisplayName("WS-008 should not have stray error events")
  void shouldNotHaveStrayErrorEvents(Page page, TestServer server) {
    server.sendOnWebSocketConnection("incoming");
    AtomicReference<String> error = new AtomicReference<>();
    page.onWebSocket(ws -> ws.onSocketError(e -> error.set(e)));
    waitForWebSocketFrameReceived(page, () -> page.evaluate("host => {\n" +
      "      window.ws = new WebSocket('ws://' + host + '/ws');\n" +
      "    }", server.HOST));
    page.evaluate("window.ws.close()");
    assertNull(error.get());
  }

  @PlaywrightTest
  @DisplayName("WS-009 should reject waitForEvent on socket close")
  void shouldRejectWaitForEventOnSocketClose(Page page, TestServer server) {
    server.sendOnWebSocketConnection("incoming");
    WebSocket ws = waitForWebSocketFrameReceived(page, () -> page.evaluate("host => {\n" +
      "      window.ws = new WebSocket('ws://' + host + '/ws');\n" +
      "    }", server.HOST));
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> ws.waitForFrameSent(() -> page.evaluate("window.ws.close()")));
    assertTrue(error.getMessage().contains("Socket closed"));
  }

  @PlaywrightTest
  @DisplayName("WS-010 should reject waitForEvent on page close")
  void shouldRejectWaitForEventOnPageClose(Page page, TestServer server) {
    server.sendOnWebSocketConnection("incoming");
    WebSocket ws = waitForWebSocketFrameReceived(page, () -> page.evaluate("host => {\n" +
      "      window.ws = new WebSocket('ws://' + host + '/ws');\n" +
      "    }", server.HOST));
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> ws.waitForFrameSent(() -> page.close()));
    assertTrue(error.getMessage().contains(kTargetClosedErrorMessage));
  }

  @PlaywrightTest
  @DisplayName("WS-011 should not tear down the page when a WebSocket is opened inside a worker")
  void shouldNotTearDownThePageWhenAWebSocketIsOpenedInsideAWorker(Page page, TestServer server) {
    // { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/41742' }
    server.sendOnWebSocketConnection("incoming");
    page.navigate(server.EMPTY_PAGE);
    Object received = page.evaluate("host => {\n" +
      "    const code = `\n" +
      "      const ws = new WebSocket(${JSON.stringify('ws://' + host + '/ws')});\n" +
      "      ws.addEventListener('message', event => self.postMessage(event.data));\n" +
      "    `;\n" +
      "    const worker = new Worker(URL.createObjectURL(new Blob([code], { type: 'text/javascript' })));\n" +
      "    return new Promise(resolve => worker.addEventListener('message', event => resolve(event.data)));\n" +
      "  }", server.HOST);
    assertEquals("incoming", received);
    // Opening a `WebSocket` inside a worker must not tear down the page session.
    assertEquals(2, page.evaluate("() => 1 + 1"));
  }

  @PlaywrightTest
  @DisplayName("WS-012 should turn off when offline")
  void shouldTurnOffWhenOffline(Page page, TestServer server) throws Exception {
    assumeTrue(false, "fixme");

    // Java: the test server stands in for the WebSocketServer of the 'ws' package.
    CompletableFuture<ServerWebSocket> socketPromise = server.waitForWebSocket();
    JSHandle wsHandle = page.evaluateHandle("async address => {\n" +
      "      const ws = new WebSocket(`ws://${address}/ws`);\n" +
      "      await new Promise(x => ws.onopen = x);\n" +
      "      return ws;\n" +
      "    }", server.HOST);
    ServerWebSocket socket = socketPromise.get();
    CompletableFuture<ServerWebSocket.Message> failurePromise = socket.waitForMessage();
    page.context().setOffline(true);
    wsHandle.evaluate("ws => ws.send('if this arrives it failed')");
    // Java: Promise.race([failurePromise, closePromise]) is the close result and a check that nothing arrived.
    Object result = wsHandle.evaluate("async ws => {\n" +
      "      if (ws.readyState !== WebSocket.CLOSED)\n" +
      "        await new Promise(x => ws.onclose = x);\n" +
      "      return 'successfully closed';\n" +
      "    }");
    assertFalse(failurePromise.isDone());
    assertEquals("successfully closed", result);
  }

  @PlaywrightTest
  @DisplayName("WS-013 should send extra HTTP headers on WebSocket handshake")
  void shouldSendExtraHTTPHeadersOnWebSocketHandshake(Page page, TestServer server, Browser browser, BrowserName browserName) throws Exception {
    // { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/28948' }
    assumeFalse(browserName == BrowserName.CHROMIUM && browserMajorVersion(browser) < 151, "fixme: Chromium before 151 does not send extra HTTP headers on WebSocket handshake");

    page.setExtraHTTPHeaders(singletonMap("foo", "bar"));
    page.navigate(server.EMPTY_PAGE);
    CompletableFuture<TestServer.Request> reqPromise = server.waitForWebSocketConnectionRequest();
    page.evaluate("host => { new WebSocket(`ws://${host}/ws`); }", server.HOST);
    TestServer.Request req = reqPromise.get();
    assertEquals("bar", req.headers.get("foo"));
  }

  @PlaywrightTest
  @DisplayName("WS-014 should send extra HTTP headers on WebSocket handshake from a worker")
  void shouldSendExtraHTTPHeadersOnWebSocketHandshakeFromAWorker(Page page, TestServer server, Browser browser, BrowserName browserName) throws Exception {
    // { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/28948' }
    assumeFalse(browserName == BrowserName.CHROMIUM && browserMajorVersion(browser) < 151, "fixme: Chromium before 151 does not send extra HTTP headers on WebSocket handshake");

    page.setExtraHTTPHeaders(singletonMap("foo", "bar"));
    page.navigate(server.EMPTY_PAGE);
    CompletableFuture<TestServer.Request> reqPromise = server.waitForWebSocketConnectionRequest();
    page.evaluate("host => {\n" +
      "    const code = `new WebSocket(${JSON.stringify('ws://' + host + '/ws')});`;\n" +
      "    new Worker(URL.createObjectURL(new Blob([code], { type: 'text/javascript' })));\n" +
      "  }", server.HOST);
    TestServer.Request req = reqPromise.get();
    assertEquals("bar", req.headers.get("foo"));
  }
}
