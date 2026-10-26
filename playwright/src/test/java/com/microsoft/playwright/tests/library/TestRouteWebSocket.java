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
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.WebSocketFrame;
import com.microsoft.playwright.WebSocketRoute;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.ContextFactory;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.ServerWebSocket;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import static com.microsoft.playwright.fixtures.Utils.attachFrame;
import static com.microsoft.playwright.fixtures.Utils.detachFrame;
import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/library/route-web-socket.spec.ts. */
public class TestRouteWebSocket {
  // Java: the test thread only receives events while it is inside a Playwright call, so an
  // awaited promise is waited for with page.waitForCondition().
  private static <T> T waitFor(Page page, CompletableFuture<T> future) throws Exception {
    page.waitForCondition(future::isDone);
    return future.get();
  }

  // Java: await expect.poll(actual).toEqual(expected) / toBe(expected); the page is there to receive events between the attempts.
  private static void expectPoll(Page page, Supplier<Object> actual, Object expected) {
    long deadline = System.currentTimeMillis() + 5000;
    Object value = actual.get();
    while (!Objects.equals(value, expected) && System.currentTimeMillis() < deadline) {
      page.waitForTimeout(100);
      value = actual.get();
    }
    assertEquals(expected, value);
  }

  // Java: a message of a route is a WebSocketFrame, a string or binary payload as in the Node.js client.
  private static void send(WebSocketRoute route, WebSocketFrame message) {
    if (message.text() != null)
      route.send(message.text());
    else
      route.send(message.binary());
  }

  // Java: target is a Page or a Frame.
  private static void setupWS(Object target, TestServer server, String binaryType, Object protocols, boolean relativeURL) {
    if (target instanceof Page)
      ((Page) target).navigate(server.EMPTY_PAGE);
    else
      ((Frame) target).navigate(server.EMPTY_PAGE);
    String wsUrl = relativeURL ? "/ws" : "ws://" + server.HOST + "/ws";
    String script = "({ wsUrl, binaryType, protocols }) => {\n" +
      "    window.log = [];\n" +
      "    window.ws = new WebSocket(wsUrl, protocols);\n" +
      "    window.ws.binaryType = binaryType;\n" +
      "    window.ws.addEventListener('open', () => window.log.push('open'));\n" +
      "    window.ws.addEventListener('close', event => window.log.push(`close code=${event.code} reason=${event.reason} wasClean=${event.wasClean}`));\n" +
      "    window.ws.addEventListener('error', event => window.log.push(`error`));\n" +
      "    window.ws.addEventListener('message', async event => {\n" +
      "      let data;\n" +
      "      if (typeof event.data === 'string')\n" +
      "        data = event.data;\n" +
      "      else if (event.data instanceof Blob)\n" +
      "        data = 'blob:' + await event.data.text();\n" +
      "      else\n" +
      "        data = 'arraybuffer:' + await (new Blob([event.data])).text();\n" +
      "      window.log.push(`message: data=${data} origin=${event.origin} lastEventId=${event.lastEventId}`);\n" +
      "    });\n" +
      "    window.wsOpened = new Promise(f => window.ws.addEventListener('open', () => f()));\n" +
      "  }";
    Map<String, Object> arg = mapOf("wsUrl", wsUrl, "binaryType", binaryType, "protocols", protocols);
    if (target instanceof Page)
      ((Page) target).evaluate(script, arg);
    else
      ((Frame) target).evaluate(script, arg);
  }

  private static void setupWS(Object target, TestServer server, String binaryType) {
    setupWS(target, server, binaryType, null, false);
  }

  @Nested
  @ParameterizedClass
  @ValueSource(strings = {"no-mock", "no-match", "pass-through"})
  class Mock {
    @Parameter
    String mock;

    @BeforeEach
    void setUpMock(Page page) {
      if (mock.equals("no-match")) {
        page.routeWebSocket(Pattern.compile("zzz"), ws -> {});
      } else if (mock.equals("pass-through")) {
        page.routeWebSocket(Pattern.compile(".*"), ws -> {
          WebSocketRoute serverRoute = ws.connectToServer();
          ws.onMessage(message -> send(serverRoute, message));
          serverRoute.onMessage(message -> send(ws, message));
        });
      }
    }

    @PlaywrightTest
    @DisplayName("WS-015 should work with text message")
    void shouldWorkWithTextMessage(Page page, TestServer server) throws Exception {
      CompletableFuture<ServerWebSocket> wsPromise = server.waitForWebSocket();
      CompletableFuture<TestServer.UpgradeActions> upgradePromise = server.waitForUpgrade();
      setupWS(page, server, "blob");
      assertEquals(0, page.evaluate("() => window.ws.readyState"));
      TestServer.UpgradeActions upgrade = waitFor(page, upgradePromise);
      assertEquals(0, page.evaluate("() => window.ws.readyState"));
      assertEquals(new ArrayList<>(), page.evaluate("() => window.log"));
      upgrade.doUpgrade();
      expectPoll(page, () -> page.evaluate("() => window.ws.readyState"), 1);
      assertEquals(asList("open"), page.evaluate("() => window.log"));
      ServerWebSocket ws = waitFor(page, wsPromise);
      ws.send("hello");
      expectPoll(page, () -> page.evaluate("() => window.log"), asList(
        "open",
        "message: data=hello origin=ws://" + server.HOST + " lastEventId="));
      assertEquals(1, page.evaluate("() => window.ws.readyState"));
      CompletableFuture<String> messagePromise = new CompletableFuture<>();
      ws.onMessage(data -> messagePromise.complete(data.text()));
      page.evaluate("() => window.ws.send('hi')");
      assertEquals("hi", waitFor(page, messagePromise));
      ws.close(1008, "oops");
      expectPoll(page, () -> page.evaluate("() => window.ws.readyState"), 3);
      assertEquals(asList(
        "open",
        "message: data=hello origin=ws://" + server.HOST + " lastEventId=",
        "close code=1008 reason=oops wasClean=true"), page.evaluate("() => window.log"));
    }

    @PlaywrightTest
    @DisplayName("WS-016 should work with binaryType=blob")
    void shouldWorkWithBinaryTypeBlob(Page page, TestServer server) throws Exception {
      CompletableFuture<ServerWebSocket> wsPromise = server.waitForWebSocket();
      setupWS(page, server, "blob");
      ServerWebSocket ws = waitFor(page, wsPromise);
      ws.send("hi".getBytes(StandardCharsets.UTF_8));
      expectPoll(page, () -> page.evaluate("() => window.log"), asList(
        "open",
        "message: data=blob:hi origin=ws://" + server.HOST + " lastEventId="));
      CompletableFuture<String> messagePromise = new CompletableFuture<>();
      ws.onMessage(data -> messagePromise.complete(data.text()));
      page.evaluate("() => window.ws.send(new Blob([new Uint8Array(['h'.charCodeAt(0), 'i'.charCodeAt(0)])]))");
      assertEquals("hi", waitFor(page, messagePromise));
    }

    @PlaywrightTest
    @DisplayName("WS-017 should work with binaryType=arraybuffer")
    void shouldWorkWithBinaryTypeArraybuffer(Page page, TestServer server) throws Exception {
      CompletableFuture<ServerWebSocket> wsPromise = server.waitForWebSocket();
      setupWS(page, server, "arraybuffer");
      ServerWebSocket ws = waitFor(page, wsPromise);
      ws.send("hi".getBytes(StandardCharsets.UTF_8));
      expectPoll(page, () -> page.evaluate("() => window.log"), asList(
        "open",
        "message: data=arraybuffer:hi origin=ws://" + server.HOST + " lastEventId="));
      CompletableFuture<String> messagePromise = new CompletableFuture<>();
      ws.onMessage(data -> messagePromise.complete(data.text()));
      page.evaluate("() => window.ws.send(new Uint8Array(['h'.charCodeAt(0), 'i'.charCodeAt(0)]).buffer)");
      assertEquals("hi", waitFor(page, messagePromise));
    }

    @PlaywrightTest
    @DisplayName("WS-018 should work when connection errors out")
    @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "WebKit ignores the connection error and fires no events!")
    void shouldWorkWhenConnectionErrorsOut(Page page, TestServer server, BrowserName browserName) throws Exception {
      CompletableFuture<TestServer.UpgradeActions> upgradePromise = server.waitForUpgrade();
      setupWS(page, server, "blob");
      TestServer.UpgradeActions upgrade = waitFor(page, upgradePromise);
      TestServer.Connection socket = upgrade.socket;
      assertEquals(0, page.evaluate("() => window.ws.readyState"));
      assertEquals(new ArrayList<>(), page.evaluate("() => window.log"));
      socket.destroy();
      expectPoll(page, () -> page.evaluate("() => window.ws.readyState"), 3);
      List<Object> log = (List<Object>) page.evaluate("() => window.log");
      assertEquals(2, log.size());
      assertEquals("error", log.get(0));
      assertTrue(Pattern.compile("close code=\\d+ reason= wasClean=false").matcher((String) log.get(1)).find());
    }

    @PlaywrightTest
    @DisplayName("WS-019 should work with error after successful open")
    @DisabledOnBrowser(value = BrowserName.FIREFOX, reason = "Firefox does not close the websocket upon a bad frame")
    @DisabledOnBrowser(value = BrowserName.WEBKIT, os = OS.LINUX, reason = "WebKit linux does not close the websocket upon a bad frame")
    @DisabledOnBrowser(value = BrowserName.WEBKIT, os = OS.WINDOWS, reason = "WebKit Windows does not close the websocket upon a bad frame")
    void shouldWorkWithErrorAfterSuccessfulOpen(Page page, TestServer server, BrowserName browserName) throws Exception {
      CompletableFuture<TestServer.UpgradeActions> upgradePromise = server.waitForUpgrade();
      setupWS(page, server, "blob");
      TestServer.UpgradeActions upgrade = waitFor(page, upgradePromise);
      TestServer.Connection socket = upgrade.socket;
      upgrade.doUpgrade();
      expectPoll(page, () -> page.evaluate("() => window.ws.readyState"), 1);
      assertEquals(asList("open"), page.evaluate("() => window.log"));
      socket.write("garbage");
      expectPoll(page, () -> page.evaluate("() => window.ws.readyState"), 3);
      List<Object> log = (List<Object>) page.evaluate("() => window.log");
      assertEquals(3, log.size());
      assertEquals("open", log.get(0));
      assertEquals("error", log.get(1));
      assertTrue(Pattern.compile("close code=\\d+ reason= wasClean=false").matcher((String) log.get(2)).find());
    }

    @PlaywrightTest
    @DisplayName("WS-020 should work with client-side close")
    @Fixme("harness: ServerWebSocket answers a close frame with the code only, while ws echoes the reason: the page logs 'close code=3002 reason= wasClean=true' instead of 'close code=3002 reason=oops wasClean=true'")
    void shouldWorkWithClientSideClose(Page page, TestServer server) throws Exception {
      CompletableFuture<ServerWebSocket> wsPromise = server.waitForWebSocket();
      CompletableFuture<TestServer.UpgradeActions> upgradePromise = server.waitForUpgrade();
      setupWS(page, server, "blob");
      assertEquals(0, page.evaluate("() => window.ws.readyState"));
      TestServer.UpgradeActions upgrade = waitFor(page, upgradePromise);
      assertEquals(0, page.evaluate("() => window.ws.readyState"));
      assertEquals(new ArrayList<>(), page.evaluate("() => window.log"));
      upgrade.doUpgrade();
      expectPoll(page, () -> page.evaluate("() => window.ws.readyState"), 1);
      assertEquals(asList("open"), page.evaluate("() => window.log"));
      ServerWebSocket ws = waitFor(page, wsPromise);
      CompletableFuture<ServerWebSocket.CloseEvent> closedPromise = ws.waitForClose();
      Object readyState = page.evaluate("() => {\n" +
        "        window.ws.close(3002, 'oops');\n" +
        "        return window.ws.readyState;\n" +
        "      }");
      assertEquals(2, readyState);
      expectPoll(page, () -> page.evaluate("() => window.ws.readyState"), 3);
      assertEquals(asList(
        "open",
        "close code=3002 reason=oops wasClean=true"), page.evaluate("() => window.log"));
      ServerWebSocket.CloseEvent closed = waitFor(page, closedPromise);
      assertEquals(3002, closed.code);
      assertEquals("oops", closed.reason);
    }

    @PlaywrightTest
    @DisplayName("WS-021 should pass through the required protocol")
    void shouldPassThroughTheRequiredProtocol(Page page, TestServer server) {
      setupWS(page, server, "blob", "my-custom-protocol", false);
      page.evaluate("() => window.wsOpened");
      Object protocol = page.evaluate("() => window.ws.protocol");
      assertEquals("my-custom-protocol", protocol);
    }

    @PlaywrightTest
    @DisplayName("WS-022 should work with relative WebSocket URL")
    void shouldWorkWithRelativeWebSocketURL(Page page, TestServer server) throws Exception {
      CompletableFuture<ServerWebSocket> wsPromise = server.waitForWebSocket();
      setupWS(page, server, "blob", null, true);
      ServerWebSocket ws = waitFor(page, wsPromise);
      ws.send("hi".getBytes(StandardCharsets.UTF_8));
      expectPoll(page, () -> page.evaluate("() => window.log"), asList(
        "open",
        "message: data=blob:hi origin=ws://" + server.HOST + " lastEventId="));
      CompletableFuture<String> messagePromise = new CompletableFuture<>();
      ws.onMessage(data -> messagePromise.complete(data.text()));
      page.evaluate("() => window.ws.send(new Blob([new Uint8Array(['h'.charCodeAt(0), 'i'.charCodeAt(0)])]))");
      assertEquals("hi", waitFor(page, messagePromise));
    }
  }

  @PlaywrightTest
  @DisplayName("WS-023 should work with ws.close")
  void shouldWorkWithWsClose(Page page, TestServer server) throws Exception {
    CompletableFuture<WebSocketRoute> promise = new CompletableFuture<>();
    page.routeWebSocket(Pattern.compile(".*"), ws -> {
      ws.connectToServer();
      promise.complete(ws);
    });

    CompletableFuture<ServerWebSocket> wsPromise = server.waitForWebSocket();
    setupWS(page, server, "blob");
    ServerWebSocket ws = waitFor(page, wsPromise);

    WebSocketRoute route = waitFor(page, promise);
    route.send("hello");
    expectPoll(page, () -> page.evaluate("() => window.log"), asList(
      "open",
      "message: data=hello origin=ws://" + server.HOST + " lastEventId="));

    CompletableFuture<ServerWebSocket.CloseEvent> closedPromise = ws.waitForClose();
    route.close(new WebSocketRoute.CloseOptions().setCode(3009).setReason("oops"));
    expectPoll(page, () -> page.evaluate("() => window.log"), asList(
      "open",
      "message: data=hello origin=ws://" + server.HOST + " lastEventId=",
      "close code=3009 reason=oops wasClean=true"));
    ServerWebSocket.CloseEvent closed = waitFor(page, closedPromise);
    assertEquals(3009, closed.code);
    assertEquals("oops", closed.reason);
  }

  @PlaywrightTest
  @DisplayName("WS-024 should observe upstream handshake failure when connectToServer is used")
  void shouldObserveUpstreamHandshakeFailureWhenConnectToServerIsUsed(Page page, TestServer server) throws Exception {
    // Exercises the WebSocket-as-network-request path used by Firefox after the
    // har-WebSocket plumbing change, where a 4xx handshake response is synthesized
    // into a full lifecycle in FFPage._onWebSocketRequestFinished.
    List<Integer> serverCloseCodes = new CopyOnWriteArrayList<>();
    AtomicInteger routeHandlerInvoked = new AtomicInteger();
    page.routeWebSocket(Pattern.compile(".*"), ws -> {
      routeHandlerInvoked.incrementAndGet();
      WebSocketRoute serverRoute = ws.connectToServer();
      serverRoute.onClose((code, reason) -> {
        serverCloseCodes.add(code);
        ws.close();
      });
    });

    CompletableFuture<TestServer.UpgradeActions> upgradePromise = server.waitForUpgrade();
    setupWS(page, server, "blob");
    TestServer.Connection socket = waitFor(page, upgradePromise).socket;
    socket.write("HTTP/1.1 403 Forbidden\r\nContent-Length: 0\r\n\r\n");
    socket.destroy();

    // Server-side route handler observes the close caused by the rejected handshake.
    expectPoll(page, () -> serverCloseCodes.size(), 1);
    assertTrue(serverCloseCodes.get(0) >= 1000);

    // Once the route closes the page-side socket, the page sees the close.
    expectPoll(page, () -> page.evaluate("() => window.ws.readyState"), 3);
    assertEquals(1, routeHandlerInvoked.get());
  }

  @PlaywrightTest
  @DisplayName("WS-025 should observe multiple concurrent routed WebSockets with connectToServer")
  void shouldObserveMultipleConcurrentRoutedWebSocketsWithConnectToServer(Page page, TestServer server) {
    // Exercises FFNetworkManager._webSocketRequestIds tracking by routing two
    // simultaneous WebSocket connections through `connectToServer()`. Each request
    // gets a distinct id in the new Firefox WebSocket-as-network-request flow.
    AtomicInteger routedConnections = new AtomicInteger();
    page.routeWebSocket(Pattern.compile(".*"), ws -> {
      routedConnections.incrementAndGet();
      WebSocketRoute serverRoute = ws.connectToServer();
      ws.onMessage(message -> send(serverRoute, message));
      serverRoute.onMessage(message -> send(ws, message));
    });

    // Echo all incoming messages on whichever connection arrives; re-register after each.
    AtomicReference<Consumer<ServerWebSocket>> handleConnection = new AtomicReference<>();
    handleConnection.set(ws -> {
      ws.onMessage(data -> ws.send("echo-" + data.text()));
      server.onceWebSocketConnection(handleConnection.get());
    });
    server.onceWebSocketConnection(handleConnection.get());

    page.navigate(server.EMPTY_PAGE);
    List<String> results = new ArrayList<>((List<String>) page.evaluate("async host => {\n" +
      "    const collect = (tag) => new Promise(resolve => {\n" +
      "      const ws = new WebSocket(`ws://${host}/ws`);\n" +
      "      ws.addEventListener('open', () => ws.send(`hi-${tag}`));\n" +
      "      ws.addEventListener('message', event => {\n" +
      "        resolve(event.data);\n" +
      "        ws.close();\n" +
      "      });\n" +
      "    });\n" +
      "    return Promise.all([collect('a'), collect('b')]);\n" +
      "  }", server.HOST));

    Collections.sort(results);
    assertEquals(asList("echo-hi-a", "echo-hi-b"), results);
    assertEquals(2, routedConnections.get());
  }

  @PlaywrightTest
  @DisplayName("WS-026 should pattern match")
  void shouldPatternMatch(Page page, TestServer server) throws Exception {
    page.routeWebSocket(Pattern.compile(".*/ws$"), ws -> {
      ws.connectToServer();
    });

    page.routeWebSocket("**/mock-ws", ws -> {
      ws.onMessage(message -> {
        ws.send("mock-response");
      });
    });

    CompletableFuture<ServerWebSocket> wsPromise = server.waitForWebSocket();

    page.navigate(server.EMPTY_PAGE);
    page.evaluate("async ({ host }) => {\n" +
      "    window.log = [];\n" +
      "    window.ws1 = new WebSocket('ws://' + host + '/ws');\n" +
      "    window.ws1.addEventListener('message', event => window.log.push(`ws1:${event.data}`));\n" +
      "    window.ws2 = new WebSocket('ws://' + host + '/something/something/mock-ws');\n" +
      "    window.ws2.addEventListener('message', event => window.log.push(`ws2:${event.data}`));\n" +
      "    await Promise.all([\n" +
      "      new Promise(f => window.ws1.addEventListener('open', f)),\n" +
      "      new Promise(f => window.ws2.addEventListener('open', f)),\n" +
      "    ]);\n" +
      "  }", mapOf("host", server.HOST));

    ServerWebSocket ws = waitFor(page, wsPromise);
    ws.onMessage(message -> ws.send("response"));

    page.evaluate("() => window.ws1.send('request')");
    expectPoll(page, () -> page.evaluate("() => window.log"), asList("ws1:response"));

    page.evaluate("() => window.ws2.send('request')");
    expectPoll(page, () -> page.evaluate("() => window.log"), asList("ws1:response", "ws2:mock-response"));
  }

  @PlaywrightTest
  @DisplayName("WS-027 should work with server")
  void shouldWorkWithServer(Page page, TestServer server) throws Exception {
    CompletableFuture<WebSocketRoute> promise = new CompletableFuture<>();
    page.routeWebSocket(Pattern.compile(".*"), ws -> {
      // Java: named serverRoute, a lambda can not declare a variable named server.
      WebSocketRoute serverRoute = ws.connectToServer();
      ws.onMessage(message -> {
        switch (message.text()) {
          case "to-respond":
            ws.send("response");
            return;
          case "to-block":
            return;
          case "to-modify":
            serverRoute.send("modified");
            return;
        }
        send(serverRoute, message);
      });
      serverRoute.onMessage(message -> {
        switch (message.text()) {
          case "to-block":
            return;
          case "to-modify":
            ws.send("modified");
            return;
        }
        send(ws, message);
      });
      serverRoute.send("fake");
      promise.complete(ws);
    });

    CompletableFuture<ServerWebSocket> wsPromise = server.waitForWebSocket();
    List<String> log = new CopyOnWriteArrayList<>();
    server.onceWebSocketConnection(ws -> {
      ws.onMessage(data -> log.add("message: " + data.text()));
      ws.onClose((code, reason) -> log.add("close: code=" + code + " reason=" + reason));
    });

    setupWS(page, server, "blob");
    ServerWebSocket ws = waitFor(page, wsPromise);
    expectPoll(page, () -> log, asList("message: fake"));

    ws.send("to-modify");
    ws.send("to-block");
    ws.send("pass-server");
    expectPoll(page, () -> page.evaluate("() => window.log"), asList(
      "open",
      "message: data=modified origin=ws://" + server.HOST + " lastEventId=",
      "message: data=pass-server origin=ws://" + server.HOST + " lastEventId="));

    page.evaluate("() => {\n" +
      "    window.ws.send('to-respond');\n" +
      "    window.ws.send('to-modify');\n" +
      "    window.ws.send('to-block');\n" +
      "    window.ws.send('pass-client');\n" +
      "  }");
    expectPoll(page, () -> log, asList("message: fake", "message: modified", "message: pass-client"));
    expectPoll(page, () -> page.evaluate("() => window.log"), asList(
      "open",
      "message: data=modified origin=ws://" + server.HOST + " lastEventId=",
      "message: data=pass-server origin=ws://" + server.HOST + " lastEventId=",
      "message: data=response origin=ws://" + server.HOST + " lastEventId="));

    WebSocketRoute route = waitFor(page, promise);
    route.send("another");
    expectPoll(page, () -> page.evaluate("() => window.log"), asList(
      "open",
      "message: data=modified origin=ws://" + server.HOST + " lastEventId=",
      "message: data=pass-server origin=ws://" + server.HOST + " lastEventId=",
      "message: data=response origin=ws://" + server.HOST + " lastEventId=",
      "message: data=another origin=ws://" + server.HOST + " lastEventId="));

    page.evaluate("() => {\n" +
      "    window.ws.send('pass-client-2');\n" +
      "  }");
    expectPoll(page, () -> log, asList("message: fake", "message: modified", "message: pass-client", "message: pass-client-2"));

    page.evaluate("() => {\n" +
      "    window.ws.close(3009, 'problem');\n" +
      "  }");
    expectPoll(page, () -> log, asList("message: fake", "message: modified", "message: pass-client", "message: pass-client-2", "close: code=3009 reason=problem"));
  }

  @PlaywrightTest
  @DisplayName("WS-028 should work without server")
  void shouldWorkWithoutServer(Page page, TestServer server) throws Exception {
    CompletableFuture<WebSocketRoute> promise = new CompletableFuture<>();
    page.routeWebSocket(Pattern.compile(".*"), ws -> {
      ws.onMessage(message -> {
        switch (message.text()) {
          case "to-respond":
            ws.send("response");
            return;
        }
      });
      promise.complete(ws);
    });

    setupWS(page, server, "blob");

    page.evaluate("async () => {\n" +
      "    await window.wsOpened;\n" +
      "    window.ws.send('to-respond');\n" +
      "    window.ws.send('to-block');\n" +
      "    window.ws.send('to-respond');\n" +
      "  }");

    expectPoll(page, () -> page.evaluate("() => window.log"), asList(
      "open",
      "message: data=response origin=ws://" + server.HOST + " lastEventId=",
      "message: data=response origin=ws://" + server.HOST + " lastEventId="));

    WebSocketRoute route = waitFor(page, promise);
    route.send("another");
    route.close(new WebSocketRoute.CloseOptions().setCode(3008).setReason("oops"));

    expectPoll(page, () -> page.evaluate("() => window.log"), asList(
      "open",
      "message: data=response origin=ws://" + server.HOST + " lastEventId=",
      "message: data=response origin=ws://" + server.HOST + " lastEventId=",
      "message: data=another origin=ws://" + server.HOST + " lastEventId=",
      "close code=3008 reason=oops wasClean=true"));
  }

  @PlaywrightTest
  @DisplayName("WS-029 should route WebSockets in already loaded documents")
  void shouldRouteWebSocketsInAlreadyLoadedDocuments(Page page, TestServer server) {
    // { type: 'issue', description: 'https://github.com/microsoft/playwright/issues/43105' }
    page.navigate(server.EMPTY_PAGE);
    Frame frame = attachFrame(page, "frame1", server.EMPTY_PAGE);
    page.routeWebSocket(Pattern.compile(".*"), ws -> {
      ws.onMessage(message -> ws.send("mock-" + message.text()));
    });

    for (Frame target : asList(page.mainFrame(), frame)) {
      target.evaluate("({ host }) => {\n" +
        "      window.log = [];\n" +
        "      window.ws = new WebSocket('ws://' + host + '/ws');\n" +
        "      window.ws.addEventListener('message', event => window.log.push(event.data));\n" +
        "      window.ws.addEventListener('close', () => window.log.push('close'));\n" +
        "      window.ws.addEventListener('open', () => window.ws.send('hi'));\n" +
        "    }", mapOf("host", server.HOST));
      expectPoll(page, () -> target.evaluate("() => window.log"), asList("mock-hi"));
    }
  }

  @PlaywrightTest
  @DisplayName("WS-030 should emit close upon frame navigation")
  void shouldEmitCloseUponFrameNavigation(Page page, TestServer server) throws Exception {
    CompletableFuture<WebSocketRoute> promise = new CompletableFuture<>();
    page.routeWebSocket(Pattern.compile(".*"), ws -> {
      ws.connectToServer();
      promise.complete(ws);
    });

    setupWS(page, server, "blob");

    WebSocketRoute route = waitFor(page, promise);
    route.send("hello");

    expectPoll(page, () -> page.evaluate("() => window.log"), asList(
      "open",
      "message: data=hello origin=ws://" + server.HOST + " lastEventId="));

    CompletableFuture<Void> closedPromise = new CompletableFuture<>();
    route.onClose((code, reason) -> closedPromise.complete(null));
    page.navigate(server.EMPTY_PAGE);
    waitFor(page, closedPromise);
  }

  @PlaywrightTest
  @DisplayName("WS-031 should emit close upon frame detach")
  void shouldEmitCloseUponFrameDetach(Page page, TestServer server) throws Exception {
    CompletableFuture<WebSocketRoute> promise = new CompletableFuture<>();
    page.routeWebSocket(Pattern.compile(".*"), ws -> {
      ws.connectToServer();
      promise.complete(ws);
    });

    Frame frame = attachFrame(page, "frame1", server.EMPTY_PAGE);
    setupWS(frame, server, "blob");

    WebSocketRoute route = waitFor(page, promise);
    route.send("hello");

    expectPoll(page, () -> frame.evaluate("() => window.log"), asList(
      "open",
      "message: data=hello origin=ws://" + server.HOST + " lastEventId="));

    CompletableFuture<Void> closedPromise = new CompletableFuture<>();
    route.onClose((code, reason) -> closedPromise.complete(null));
    detachFrame(page, "frame1");
    waitFor(page, closedPromise);
  }

  @PlaywrightTest
  @DisplayName("WS-032 should route on context")
  void shouldRouteOnContext(Page page, TestServer server) {
    page.routeWebSocket(Pattern.compile("ws1"), ws -> {
      ws.onMessage(message -> {
        ws.send("page-mock-1");
      });
    });

    page.routeWebSocket(Pattern.compile("ws1"), ws -> {
      ws.onMessage(message -> {
        ws.send("page-mock-2");
      });
    });

    page.context().routeWebSocket(Pattern.compile(".*"), ws -> {
      ws.onMessage(message -> {
        ws.send("context-mock-1");
      });
      ws.onMessage(message -> {
        ws.send("context-mock-2");
      });
    });

    page.navigate(server.EMPTY_PAGE);
    page.evaluate("({ host }) => {\n" +
      "    window.log = [];\n" +
      "    window.ws1 = new WebSocket('ws://' + host + '/ws1');\n" +
      "    window.ws1.addEventListener('message', event => window.log.push(`ws1:${event.data}`));\n" +
      "    window.ws2 = new WebSocket('ws://' + host + '/ws2');\n" +
      "    window.ws2.addEventListener('message', event => window.log.push(`ws2:${event.data}`));\n" +
      "  }", mapOf("host", server.HOST));

    page.evaluate("() => window.ws1.send('request')");
    expectPoll(page, () -> page.evaluate("() => window.log"), asList("ws1:page-mock-2"));

    page.evaluate("() => window.ws2.send('request')");
    expectPoll(page, () -> page.evaluate("() => window.log"), asList("ws1:page-mock-2", "ws2:context-mock-2"));
  }

  @PlaywrightTest
  @DisplayName("WS-034 should not throw with empty handler")
  void shouldNotThrowWithEmptyHandler(Page page, TestServer server) {
    page.routeWebSocket(Pattern.compile(".*"), ws -> {});
    setupWS(page, server, "blob");
    expectPoll(page, () -> page.evaluate("() => window.log"), asList("open"));
    page.evaluate("() => window.ws.send('hi')");
    page.evaluate("() => window.ws.send('hi2')");
    expectPoll(page, () -> page.evaluate("() => window.log"), asList("open"));
  }

  @PlaywrightTest
  @DisplayName("WS-035 should throw when connecting twice")
  void shouldThrowWhenConnectingTwice(Page page, TestServer server) throws Exception {
    CompletableFuture<Throwable> promise = new CompletableFuture<>();

    page.routeWebSocket(Pattern.compile(".*"), ws -> {
      ws.connectToServer();
      try {
        ws.connectToServer();
      } catch (PlaywrightException e) {
        promise.complete(e);
      }
    });
    setupWS(page, server, "blob");
    Throwable error = waitFor(page, promise);
    assertTrue(error.getMessage().contains("Already connected to the server"));
  }

  @PlaywrightTest
  @DisplayName("WS-036 should work with no trailing slash")
  void shouldWorkWithNoTrailingSlash(Page page, TestServer server) {
    List<String> log = new CopyOnWriteArrayList<>();
    // No trailing slash!
    page.routeWebSocket("ws://" + server.HOST, ws -> {
      ws.onMessage(message -> {
        log.add(message.text());
        ws.send("response");
      });
    });

    page.navigate(server.EMPTY_PAGE);
    page.evaluate("({ host }) => {\n" +
      "    window.log = [];\n" +
      "    // No trailing slash!\n" +
      "    window.ws = new WebSocket('ws://' + host);\n" +
      "    window.ws.addEventListener('message', event => window.log.push(event.data));\n" +
      "  }", mapOf("host", server.HOST));

    expectPoll(page, () -> page.evaluate("() => window.ws.readyState"), 1);
    page.evaluate("() => window.ws.send('query')");
    expectPoll(page, () -> log, asList("query"));
    assertEquals(asList("response"), page.evaluate("() => window.log"));
  }

  @PlaywrightTest
  @DisplayName("WS-037 should work with baseURL")
  void shouldWorkWithBaseURL(ContextFactory contextFactory, TestServer server) {
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions().setBaseURL("http://" + server.HOST));
    Page page = context.newPage();

    page.routeWebSocket("/ws", ws -> {
      ws.onMessage(message -> {
        send(ws, message);
      });
    });

    setupWS(page, server, "blob");

    page.evaluate("async () => {\n" +
      "    await window.wsOpened;\n" +
      "    window.ws.send('echo');\n" +
      "  }");

    expectPoll(page, () -> page.evaluate("() => window.log"), asList(
      "open",
      "message: data=echo origin=ws://" + server.HOST + " lastEventId="));
  }

  @PlaywrightTest
  @DisplayName("WS-038 should work with baseURL regardless of scheme casing")
  void shouldWorkWithBaseURLRegardlessOfSchemeCasing(ContextFactory contextFactory, TestServer server) {
    // baseURL schemes are case-insensitive, same as everywhere else in URL matching.
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions().setBaseURL("HTTP://" + server.HOST));
    Page page = context.newPage();

    page.routeWebSocket("/ws", ws -> {
      ws.onMessage(message -> {
        send(ws, message);
      });
    });

    setupWS(page, server, "blob");

    page.evaluate("async () => {\n" +
      "    await window.wsOpened;\n" +
      "    window.ws.send('echo');\n" +
      "  }");

    expectPoll(page, () -> page.evaluate("() => window.log"), asList(
      "open",
      "message: data=echo origin=ws://" + server.HOST + " lastEventId="));
  }

  @PlaywrightTest
  @DisplayName("WS-039 should expose protocols to the route handler")
  void shouldExposeProtocolsToTheRouteHandler(Page page, TestServer server) {
    List<WebSocketRoute> routes = new CopyOnWriteArrayList<>();
    page.routeWebSocket(Pattern.compile(".*"), ws -> {
      routes.add(ws);
    });

    page.navigate(server.EMPTY_PAGE);
    page.evaluate("({ host }) => {\n" +
      "    window.wsNone = new WebSocket('ws://' + host + '/ws-none');\n" +
      "    window.wsString = new WebSocket('ws://' + host + '/ws-string', 'chat.v1');\n" +
      "    window.wsArray = new WebSocket('ws://' + host + '/ws-array', ['chat.v2', 'chat.v1']);\n" +
      "  }", mapOf("host", server.HOST));

    expectPoll(page, () -> routes.size(), 3);

    Map<String, WebSocketRoute> byUrl = new HashMap<>();
    for (WebSocketRoute r : routes) {
      byUrl.put(URI.create(r.url()).getPath(), r);
    }
    assertEquals(new ArrayList<String>(), byUrl.get("/ws-none").protocols());
    assertEquals(asList("chat.v1"), byUrl.get("/ws-string").protocols());
    assertEquals(asList("chat.v2", "chat.v1"), byUrl.get("/ws-array").protocols());
  }

  @PlaywrightTest
  @DisplayName("WS-040 should expose protocols on server-side route")
  void shouldExposeProtocolsOnServerSideRoute(Page page, TestServer server) throws Exception {
    CompletableFuture<WebSocketRoute[]> promise = new CompletableFuture<>();
    page.routeWebSocket(Pattern.compile(".*"), ws -> {
      WebSocketRoute serverRoute = ws.connectToServer();
      promise.complete(new WebSocketRoute[] {ws, serverRoute});
    });

    setupWS(page, server, "blob", asList("chat.v2", "chat.v1"), false);
    WebSocketRoute[] routes = waitFor(page, promise);
    WebSocketRoute pageRoute = routes[0];
    WebSocketRoute serverRoute = routes[1];
    assertEquals(asList("chat.v2", "chat.v1"), pageRoute.protocols());
    assertEquals(asList("chat.v2", "chat.v1"), serverRoute.protocols());
  }
}
