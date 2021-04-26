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

package com.microsoft.playwright.impl;

import com.microsoft.playwright.PlaywrightException;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;


class WebSocketTransport implements Transport {
  private final BlockingQueue<String> incoming = new LinkedBlockingQueue<>();
  private final ClientConnection clientConnection;
  private boolean isClosed;
  private volatile Exception lastError;
  ListenerCollection<EventType> listeners = new ListenerCollection<>();

  private enum EventType { CLOSE }

  private class ClientConnection extends WebSocketClient {
    ClientConnection(URI serverUri) {
      super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
    }

    @Override
    public void onMessage(String message) {
      incoming.add(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
    }

    @Override
    public void onError(Exception ex) {
      lastError = ex;
    }
  }

  WebSocketTransport(URI uri, Map<String, String> headers, Duration timeout) {
    clientConnection = new ClientConnection(uri);
    for (Map.Entry<String, String> entry : headers.entrySet()) {
      clientConnection.addHeader(entry.getKey(), entry.getValue());
    }
    try {
      if (!clientConnection.connectBlocking(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
        throw new PlaywrightException("Failed to connect", lastError);
      }
    } catch (InterruptedException e) {
      throw new PlaywrightException("Failed to connect", e);
    }
  }

  @Override
  public void send(String message) {
    checkIfClosed();
    clientConnection.send(message);
  }

  @Override
  public String poll(Duration timeout) {
    checkIfClosed();
    try {
      return incoming.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      throw new PlaywrightException("Failed to read message", e);
    }
  }

  @Override
  public void close() throws IOException {
    if (isClosed) {
      return;
    }
    isClosed = true;
    clientConnection.close();
  }

  void onClose(Consumer<WebSocketTransport> handler) {
    listeners.add(EventType.CLOSE, handler);
  }

  void offClose(Consumer<WebSocketTransport> handler) {
    listeners.remove(EventType.CLOSE, handler);
  }

  private void checkIfClosed() {
    if (isClosed) {
      throw new PlaywrightException("Playwright connection closed");
    }
    if (clientConnection.isClosed()) {
      isClosed = true;
      listeners.notify(EventType.CLOSE, this);
      throw new PlaywrightException("Playwright connection closed");
    }
  }
}
