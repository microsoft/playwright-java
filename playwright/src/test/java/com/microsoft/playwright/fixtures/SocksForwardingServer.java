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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The SOCKS5 server of {@code setupSocksForwardingServer} in {@code tests/config/proxy.ts}. It listens
 * on 127.0.0.1 and only connects to {@code 127.0.0.1}, {@code localhost} or
 * {@code fake-localhost-127-0-0-1.nip.io} on the allowed target port, always to 127.0.0.1 on the
 * forward port; any other target is refused like ECONNREFUSED.
 */
public class SocksForwardingServer implements AutoCloseable {
  private static final Set<String> ALLOWED_HOSTS = new HashSet<>(
    Arrays.asList("127.0.0.1", "fake-localhost-127-0-0-1.nip.io", "localhost"));

  /** The {@code host:port} targets that were connected, without duplicates. */
  public final List<String> connectHosts = new CopyOnWriteArrayList<>();
  /** {@code socks5://127.0.0.1:<port>}. */
  public final String proxyServerAddr;
  public final int PORT;

  private final int forwardPort;
  private final int allowedTargetPort;
  private final ServerSocket serverSocket;
  private final ExecutorService executor = Executors.newCachedThreadPool(runnable -> {
    Thread thread = new Thread(runnable, "SocksForwardingServer connection");
    thread.setDaemon(true);
    return thread;
  });
  private final Set<Socket> sockets = ConcurrentHashMap.newKeySet();
  private volatile boolean stopped;

  /** Port 0 means any free port. */
  public static SocksForwardingServer create(int port, int forwardPort, int allowedTargetPort) throws IOException {
    return new SocksForwardingServer(port, forwardPort, allowedTargetPort);
  }

  private SocksForwardingServer(int port, int forwardPort, int allowedTargetPort) throws IOException {
    this.forwardPort = forwardPort;
    this.allowedTargetPort = allowedTargetPort;
    serverSocket = new ServerSocket(port, 50, InetAddress.getByName("127.0.0.1"));
    PORT = serverSocket.getLocalPort();
    proxyServerAddr = "socks5://127.0.0.1:" + PORT;
    Thread acceptThread = new Thread(this::acceptLoop, "SocksForwardingServer accept :" + PORT);
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
      closeQuietly(socket);
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
    Socket target = null;
    try {
      client.setTcpNoDelay(true);
      InputStream in = client.getInputStream();
      OutputStream out = client.getOutputStream();
      Target requested = Socks5.handshake(in, out);
      if (requested == null) {
        return;
      }
      if (!ALLOWED_HOSTS.contains(requested.host) || requested.port != allowedTargetPort) {
        Socks5.reply(out, Socks5.CONNECTION_REFUSED, 0);
        return;
      }
      target = new Socket();
      sockets.add(target);
      try {
        target.connect(new InetSocketAddress("127.0.0.1", forwardPort));
      } catch (IOException e) {
        Socks5.reply(out, Socks5.CONNECTION_REFUSED, 0);
        return;
      }
      target.setKeepAlive(false);
      target.setTcpNoDelay(true);
      String host = requested.host + ":" + requested.port;
      synchronized (connectHosts) {
        if (!connectHosts.contains(host)) {
          connectHosts.add(host);
        }
      }
      Socks5.reply(out, Socks5.SUCCEEDED, target.getLocalPort());
      Socks5.relay(client, target);
    } catch (IOException | RuntimeException e) {
      // The browser closed the connection.
    } finally {
      closeQuietly(client);
      closeQuietly(target);
      sockets.remove(client);
      if (target != null) {
        sockets.remove(target);
      }
    }
  }

  static void closeQuietly(Socket socket) {
    if (socket == null) {
      return;
    }
    try {
      socket.close();
    } catch (IOException e) {
      // Already closed.
    }
  }

  static final class Target {
    final String host;
    final int port;

    Target(String host, int port) {
      this.host = host;
      this.port = port;
    }
  }

  /** The server side of RFC 1928 without authentication, shared with {@link MockSocksServer}. */
  static final class Socks5 {
    static final int SUCCEEDED = 0;
    static final int CONNECTION_REFUSED = 5;
    static final int COMMAND_NOT_SUPPORTED = 7;

    private Socks5() {
    }

    /** Reads the greeting and the CONNECT request, null if the client asked for something else. */
    static Target handshake(InputStream rawIn, OutputStream out) throws IOException {
      DataInputStream in = new DataInputStream(rawIn);
      if (in.readUnsignedByte() != 5) {
        return null;
      }
      in.readFully(new byte[in.readUnsignedByte()]);
      out.write(new byte[]{5, 0});
      out.flush();
      if (in.readUnsignedByte() != 5) {
        return null;
      }
      int command = in.readUnsignedByte();
      in.readUnsignedByte();
      int addressType = in.readUnsignedByte();
      String host;
      if (addressType == 1) {
        byte[] address = new byte[4];
        in.readFully(address);
        host = InetAddress.getByAddress(address).getHostAddress();
      } else if (addressType == 3) {
        byte[] name = new byte[in.readUnsignedByte()];
        in.readFully(name);
        host = new String(name, StandardCharsets.UTF_8);
      } else if (addressType == 4) {
        byte[] address = new byte[16];
        in.readFully(address);
        host = InetAddress.getByAddress(address).getHostAddress();
      } else {
        return null;
      }
      int port = in.readUnsignedShort();
      if (command != 1) {
        reply(out, COMMAND_NOT_SUPPORTED, 0);
        return null;
      }
      return new Target(host, port);
    }

    static void reply(OutputStream out, int code, int boundPort) throws IOException {
      out.write(new byte[]{5, (byte) code, 0, 1, 127, 0, 0, 1, (byte) (boundPort >> 8), (byte) boundPort});
      out.flush();
    }

    /** Copies bytes both ways until either side ends, then closes both. */
    static void relay(Socket a, Socket b) throws IOException {
      Thread thread = new Thread(() -> {
        try {
          copy(b.getInputStream(), a.getOutputStream());
        } catch (IOException e) {
          // Closed.
        } finally {
          closeQuietly(a);
          closeQuietly(b);
        }
      }, "Socks relay");
      thread.setDaemon(true);
      thread.start();
      try {
        copy(a.getInputStream(), b.getOutputStream());
      } finally {
        closeQuietly(a);
        closeQuietly(b);
      }
    }

    static void copy(InputStream in, OutputStream out) throws IOException {
      byte[] buffer = new byte[16 * 1024];
      int read;
      while ((read = in.read(buffer)) != -1) {
        out.write(buffer, 0, read);
        out.flush();
      }
    }
  }
}
