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
import java.util.concurrent.Semaphore;

class WebSocketServerImpl extends WebSocketServer implements AutoCloseable {
  volatile ClientHandshake lastClientHandshake;
  private final Semaphore startSemaphore = new Semaphore(0);

  static final int WS_SERVER_PORT = 8910;

  static WebSocketServerImpl create() throws InterruptedException {
    WebSocketServerImpl result = new WebSocketServerImpl(new InetSocketAddress("localhost", WebSocketServerImpl.WS_SERVER_PORT));
    result.start();
    result.startSemaphore.acquire();
    return result;
  }

  private WebSocketServerImpl(InetSocketAddress address) {
    super(address, 1);
  }

  @Override
  public void close() throws Exception {
    this.stop();
  }

  @Override
  public void onOpen(org.java_websocket.WebSocket webSocket, ClientHandshake clientHandshake) {
    lastClientHandshake = clientHandshake;
    webSocket.send("incoming");
  }

  @Override
  public void onClose(org.java_websocket.WebSocket webSocket, int i, String s, boolean b) {
  }

  @Override
  public void onMessage(org.java_websocket.WebSocket webSocket, String s) {
  }

  @Override
  public void onError(WebSocket webSocket, Exception e) {
  }

  @Override
  public void onStart() {
    startSemaphore.release();
  }
}
