package com.microsoft.playwright;

import com.microsoft.playwright.junit.FixtureTest;
import com.microsoft.playwright.junit.UsePlaywright;
import org.java_websocket.WebSocket;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import static com.microsoft.playwright.Utils.mapOf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

@FixtureTest
@UsePlaywright(TestOptionsFactories.BasicOptionsFactory.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestRouteWebSocket {
  private WebSocketServerImpl webSocketServer;

  @BeforeAll
  void startWebSockerServer() throws InterruptedException {
    webSocketServer = WebSocketServerImpl.create();
  }

  @AfterAll
  void stopWebSockerServer() throws IOException, InterruptedException {
    webSocketServer.stop();
  }

  @AfterEach
  void resetWebSocketServer() {
    webSocketServer.reset();
  }

  private void setupWS(Page target, int port, String binaryType) {
    setupWS(target.mainFrame(), port, binaryType);
  }
  private void setupWS(Frame target, int port, String binaryType) {
    target.navigate("about:blank");
    target.evaluate("({ port, binaryType }) => {\n" +
      "    window.log = [];\n" +
      "    window.ws = new WebSocket('ws://localhost:' + port + '/ws');\n" +
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
      "  }", mapOf("port", port, "binaryType", binaryType));
  }

  private void setupRoute(Page page, String mock) {
    if ("no-match".equals(mock)) {
      page.routeWebSocket(Pattern.compile("/zzz/"), ws -> {});
    } else if ("pass-through".equals(mock)) {
      page.routeWebSocket(Pattern.compile("/.*/"), ws -> {
        WebSocketRoute server = ws.connectToServer();
        ws.onMessage(message -> {
          if (message.text() != null) {
            server.send(message.text());
          } else {
            server.send(message.binary());
          }
        });
        server.onMessage(message -> {
          if (message.text() != null) {
            ws.send(message.text());
          } else {
            ws.send(message.binary());
          }
        });
      });
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {"no-mock", "no-match", "pass-through"})
  public void shouldWorkWithTextMessage(String mock, Page page) throws Exception {
    setupRoute(page, mock);
    Future<WebSocket> wsPromise = webSocketServer.waitForWebSocket();
    setupWS(page, webSocketServer.getPort(), "blob");

    page.waitForCondition(() -> {
      Boolean result = (Boolean) page.evaluate("() => window.log.length >= 1");
      return result;
    });
    assertEquals(asList("open"), page.evaluate("window.log"));

    org.java_websocket.WebSocket ws = wsPromise.get();
    ws.send("hello");
    page.waitForCondition(() -> {
      Boolean result = (Boolean) page.evaluate("() => window.log.length >= 2");
      return result;
    });

    assertEquals(
      asList("open", "message: data=hello origin=ws://localhost:" + webSocketServer.getPort() + " lastEventId="),
      page.evaluate("window.log"));

    assertEquals(1, page.evaluate("window.ws.readyState"));

    Future<String> messagePromise = webSocketServer.waitForMessage();
    page.evaluate("() => window.ws.send('hi')");
    assertEquals("hi", messagePromise.get());
    ws.close(1008, "oops");
    page.waitForCondition(() -> {
      Integer result = (Integer) page.evaluate("window.ws.readyState");
      return result == 3;
    });

    assertEquals(
      asList("open", "message: data=hello origin=ws://localhost:" + webSocketServer.getPort() + " lastEventId=",
          "close code=1008 reason=oops wasClean=true"),
      page.evaluate("window.log"));
  }


  @ParameterizedTest
  @ValueSource(strings = {"no-mock", "no-match", "pass-through"})
  public void shouldWorkWithBinaryTypeBlob(String mock, Page page) throws Exception {
    setupRoute(page, mock);
    Future<WebSocket> wsPromise = webSocketServer.waitForWebSocket();
    setupWS(page, webSocketServer.getPort(), "blob");
    org.java_websocket.WebSocket ws = wsPromise.get();
    ws.send("hi".getBytes(StandardCharsets.UTF_8));
    page.waitForCondition(() -> {
      Boolean result = (Boolean) page.evaluate("() => window.log.length >= 2");
      return result;
    });

    assertEquals(
        asList("open", "message: data=blob:hi origin=ws://localhost:" + webSocketServer.getPort() + " lastEventId="),
        page.evaluate("window.log"));
    Future<String> messagePromise = webSocketServer.waitForMessage();
    page.evaluate("() => window.ws.send(new Blob([new Uint8Array(['h'.charCodeAt(0), 'i'.charCodeAt(0)])]))");
    // Without this the blob message is not sent in pass-through!
    assertEquals(1, page.evaluate("window.ws.readyState"));
    assertEquals("hi", messagePromise.get());
  }

  @ParameterizedTest
  @ValueSource(strings = {"no-mock", "no-match", "pass-through"})
  public void shouldWorkWithBinaryTypeArrayBuffer(String mock, Page page) throws Exception {
    setupRoute(page, mock);
    Future<WebSocket> wsPromise = webSocketServer.waitForWebSocket();
    setupWS(page, webSocketServer.getPort(), "arraybuffer");
    org.java_websocket.WebSocket ws = wsPromise.get();
    ws.send("hi".getBytes(StandardCharsets.UTF_8));
    page.waitForCondition(() -> {
      Boolean result = (Boolean) page.evaluate("() => window.log.length >= 2");
      return result;
    });

    assertEquals(
      asList("open", "message: data=arraybuffer:hi origin=ws://localhost:" + webSocketServer.getPort() + " lastEventId="),
      page.evaluate("window.log"));
    Future<String> messagePromise = webSocketServer.waitForMessage();
    page.evaluate("() => window.ws.send(new Blob([new Uint8Array(['h'.charCodeAt(0), 'i'.charCodeAt(0)])]))");
    // Without this the blob message is not sent in pass-through!
    assertEquals(1, page.evaluate("window.ws.readyState"));
    assertEquals("hi", messagePromise.get());
  }

  @Test
  public void shouldWorkWithServer(Page page) throws ExecutionException, InterruptedException {
    WebSocketRoute[] wsRoute = new WebSocketRoute[]{null};
    page.routeWebSocket(Pattern.compile("/.*/"), ws -> {
      WebSocketRoute server = ws.connectToServer();
      ws.onMessage(frame -> {
        String message = frame.text();
        switch (message) {
          case "to-respond":
            ws.send("response");
            break;
          case "to-block":
            break;
          case "to-modify":
            server.send("modified");
            break;
          default:
            server.send(message);
        }
      });
      server.onMessage(frame -> {
        String message = frame.text();
        switch (message) {
          case "to-block":
            break;
          case "to-modify":
            ws.send("modified");
            break;
          default:
            ws.send(message);
        }
      });
      server.send("fake");
      wsRoute[0] = ws;
    });

    Future<WebSocket> ws = webSocketServer.waitForWebSocket();
    setupWS(page, webSocketServer.getPort(), "blob");
    page.waitForCondition(() -> webSocketServer.logCopy().size() >= 1);
    assertEquals(
      asList("message: fake"),
      webSocketServer.logCopy());

    ws.get().send("to-modify");
    ws.get().send("to-block");
    ws.get().send("pass-server");
    page.waitForCondition(() -> (Boolean) page.evaluate("() => window.log.length >= 3"));
    assertEquals(
      asList(
        "open",
        "message: data=modified origin=ws://localhost:" + webSocketServer.getPort() + " lastEventId=",
        "message: data=pass-server origin=ws://localhost:" + webSocketServer.getPort() + " lastEventId="),
      page.evaluate("window.log"));

    page.evaluate("async () => {\n" +
      "    window.ws.send('to-respond');\n" +
      "    window.ws.send('to-modify');\n" +
      "    window.ws.send('to-block');\n" +
      "    window.ws.send('pass-client');\n" +
      "  }");
    page.waitForCondition(() -> webSocketServer.logCopy().size() >= 3);
    assertEquals(
      asList("message: fake", "message: modified", "message: pass-client"),
      webSocketServer.logCopy());

    page.waitForCondition(() -> (Boolean) page.evaluate("() => window.log.length >= 4"));
    assertEquals(
      asList(
        "open",
        "message: data=modified origin=ws://localhost:" + webSocketServer.getPort() + " lastEventId=",
        "message: data=pass-server origin=ws://localhost:" + webSocketServer.getPort() + " lastEventId=",
        "message: data=response origin=ws://localhost:" + webSocketServer.getPort() + " lastEventId="),
      page.evaluate("window.log"));

    wsRoute[0].send("another");
    page.waitForCondition(() -> (Boolean) page.evaluate("() => window.log.length >= 5"));
    assertEquals(
      asList(
        "open",
        "message: data=modified origin=ws://localhost:" + webSocketServer.getPort() + " lastEventId=",
        "message: data=pass-server origin=ws://localhost:" + webSocketServer.getPort() + " lastEventId=",
        "message: data=response origin=ws://localhost:" + webSocketServer.getPort() + " lastEventId=",
        "message: data=another origin=ws://localhost:" + webSocketServer.getPort() + " lastEventId="),
      page.evaluate("window.log"));

    page.evaluate("window.ws.send('pass-client-2');");
    page.waitForCondition(() -> webSocketServer.logCopy().size() >= 4);
    assertEquals(
      asList("message: fake", "message: modified", "message: pass-client", "message: pass-client-2"),
      webSocketServer.logCopy());

    page.evaluate("window.ws.close(3009, 'problem');");
    page.waitForCondition(() -> webSocketServer.logCopy().size() >= 5);
    assertEquals(
      asList("message: fake", "message: modified", "message: pass-client", "message: pass-client-2", "close: code=3009 reason=problem"),
      webSocketServer.logCopy());
  }

  @Test
  public void shouldWorkWithoutServer(Page page) {
    WebSocketRoute[] wsRoute = new WebSocketRoute[]{ null };
    page.routeWebSocket(Pattern.compile("/.*/"), ws -> {
      ws.onMessage(frame -> {
        String message = frame.text();
        if ("to-respond".equals(message)) {
          ws.send("response");
        }
      });
      wsRoute[0] = ws;
    });
    setupWS(page, webSocketServer.getPort(), "blob");

    page.evaluate("async () => {\n" +
      "    await window.wsOpened;\n" +
      "    window.ws.send('to-respond');\n" +
      "    window.ws.send('to-block');\n" +
      "    window.ws.send('to-respond');\n" +
      "  }");

    page.waitForCondition(() -> (Boolean) page.evaluate("() => window.log.length >= 3"));
    assertEquals(
      asList(
        "open",
        "message: data=response origin=ws://localhost:" + webSocketServer.getPort() + " lastEventId=",
        "message: data=response origin=ws://localhost:" + webSocketServer.getPort() + " lastEventId="),
      page.evaluate("window.log"));


    wsRoute[0].send("another");
    wsRoute[0].close(new WebSocketRoute.CloseOptions().setCode(3008).setReason("oops"));

    page.waitForCondition(() -> (Boolean) page.evaluate("() => window.log.length >= 5"));
    assertEquals(
      asList(
        "open",
        "message: data=response origin=ws://localhost:" + webSocketServer.getPort() + " lastEventId=",
        "message: data=response origin=ws://localhost:" + webSocketServer.getPort() + " lastEventId=",
        "message: data=another origin=ws://localhost:" + webSocketServer.getPort() + " lastEventId=",
        "close code=3008 reason=oops wasClean=true"),
      page.evaluate("window.log"));
  }
}
