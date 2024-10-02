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
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import static com.microsoft.playwright.Utils.nextFreePort;

class WebSocketServerImpl extends WebSocketServer implements AutoCloseable {
  volatile ClientHandshake lastClientHandshake;
  private volatile CompletableFuture<WebSocket> futureWebSocket;
  private volatile CompletableFuture<String> futureMessage;
  private final Semaphore startSemaphore = new Semaphore(0);

  private List<String> log = new ArrayList<>();

  static WebSocketServerImpl create() throws InterruptedException {
    // FIXME: WebSocketServer.stop() doesn't release socket immediately and starting another server
    // fails with "Address already in use", so we just allocate new port.
    int port = nextFreePort();
    WebSocketServerImpl result = new WebSocketServerImpl(new InetSocketAddress("localhost", port));
    result.start();
    result.startSemaphore.acquire();
    return result;
  }

  private WebSocketServerImpl(InetSocketAddress address) {
    super(address, 1);
  }

  synchronized void reset() {
    futureMessage = null;
    futureWebSocket = null;
    synchronized (log) {
      log.clear();
    }
  }

  Future<org.java_websocket.WebSocket> waitForWebSocket() {
    if (futureWebSocket == null) {
      futureWebSocket = new CompletableFuture<>();
    }
    return futureWebSocket;
  }

  Future<String> waitForMessage() {
    if (futureMessage == null) {
      futureMessage = new CompletableFuture<>();
    }
    return futureMessage;
  }

  List<String> logCopy() {
    synchronized (log) {
      return new ArrayList<>(log);
    }
  }

  private void addLog(String line) {
    synchronized (log) {
      log.add(line);
    }
  }

  @Override
  public void close() throws Exception {
    this.stop();
  }

  @Override
  public void onOpen(org.java_websocket.WebSocket webSocket, ClientHandshake clientHandshake) {
    lastClientHandshake = clientHandshake;
    if (futureWebSocket != null) {
      futureWebSocket.complete(webSocket);
      futureWebSocket = null;
      return;
    }
    webSocket.send("incoming");
  }

  @Override
  public void onClose(org.java_websocket.WebSocket webSocket, int code, String reason, boolean remote) {
    addLog("close: code=" + code + " reason=" + reason);
  }

  @Override
  public void onMessage(org.java_websocket.WebSocket webSocket, String s) {
    addLog("message: " + s);
    if (futureMessage != null) {
      futureMessage.complete(s);
      futureMessage = null;
    }
  }

  public void onMessage(WebSocket conn, ByteBuffer message) {
    String text = new String(message.array(), StandardCharsets.UTF_8);
    addLog("message: " + text);
    if (futureMessage != null) {
      futureMessage.complete(text);
      futureMessage = null;
    }
  }


  @Override
  public void onError(WebSocket webSocket, Exception e) {
    addLog("error: " + e.toString());
    e.printStackTrace();
    startSemaphore.release();
  }

  @Override
  public void onStart() {
    startSemaphore.release();
  }
}
