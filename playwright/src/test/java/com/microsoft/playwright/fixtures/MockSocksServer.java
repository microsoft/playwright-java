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

package com.microsoft.playwright.fixtures;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The {@code MockSocksServer} of {@code tests/config/serverFixtures.ts}: a SOCKS5 server that accepts
 * every connect and answers the first data of each connection with a fixed page, then ends the socket.
 */
public class MockSocksServer implements AutoCloseable {
  private static final String BODY = "<html><title>Served by the SOCKS proxy</title></html>";

  public final int PORT;

  private final ServerSocket serverSocket;
  private final ExecutorService executor = Executors.newCachedThreadPool(runnable -> {
    Thread thread = new Thread(runnable, "MockSocksServer connection");
    thread.setDaemon(true);
    return thread;
  });
  private final Set<Socket> sockets = ConcurrentHashMap.newKeySet();
  private volatile boolean stopped;

  /** Port 0 means any free port. */
  public static MockSocksServer create(int port) throws IOException {
    return new MockSocksServer(port);
  }

  private MockSocksServer(int port) throws IOException {
    serverSocket = new ServerSocket(port);
    PORT = serverSocket.getLocalPort();
    Thread acceptThread = new Thread(this::acceptLoop, "MockSocksServer accept :" + PORT);
    acceptThread.setDaemon(true);
    acceptThread.start();
  }

  @Override
  public void close() {
    stopped = true;
    try {
      serverSocket.close();
    } catch (IOException e) {
      // The socket is going away anyway.
    }
    for (Socket socket : sockets) {
      SocksForwardingServer.closeQuietly(socket);
    }
    sockets.clear();
    executor.shutdownNow();
  }

  private void acceptLoop() {
    while (!stopped) {
      Socket client;
      try {
        client = serverSocket.accept();
      } catch (IOException e) {
        if (stopped) {
          return;
        }
        continue;
      }
      sockets.add(client);
      executor.execute(() -> serve(client));
    }
  }

  private void serve(Socket client) {
    try {
      InputStream in = client.getInputStream();
      OutputStream out = client.getOutputStream();
      if (SocksForwardingServer.Socks5.handshake(in, out) == null) {
        return;
      }
      SocksForwardingServer.Socks5.reply(out, SocksForwardingServer.Socks5.SUCCEEDED, 0);
      if (in.read(new byte[64 * 1024]) == -1) {
        return;
      }
      byte[] body = BODY.getBytes(StandardCharsets.UTF_8);
      String head = "HTTP/1.1 200 OK\r\n" +
        "Connection: close\r\n" +
        "Content-Type: text/html\r\n" +
        "Content-Length: " + body.length + "\r\n" +
        "\r\n";
      out.write(head.getBytes(StandardCharsets.UTF_8));
      out.write(body);
      out.flush();
    } catch (IOException | RuntimeException e) {
      // The browser closed the connection.
    } finally {
      SocksForwardingServer.closeQuietly(client);
      sockets.remove(client);
    }
  }
}
