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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Predicate;

/**
 * The HTTP forward proxy the tests launch browsers with, a port of {@code TestProxy} in
 * {@code tests/config/proxy.ts} and of the Node.js proxy in {@code tests/third_party/proxy}. Plain
 * requests (absolute-form URLs) are forwarded to their target, CONNECT requests open a tunnel and
 * WebSocket upgrades are forwarded like requests. {@link #forwardTo} redirects all of them to a local
 * port and records what the browser asked for; {@link #setAuthHandler} makes the proxy ask for
 * credentials.
 */
public class ProxyServer implements AutoCloseable {
  /** A request as the proxy received it; for {@code forwardTo} the url is the one after rewriting. */
  public static final class Request {
    public final String method;
    public final String url;
    public final String httpVersion;
    /** Lower-cased names; repeated headers are joined with {@code ", "}. */
    public final Map<String, String> headers;
    /** Names and values alternating, in the order they were sent, with their case. */
    public final List<String> rawHeaders;

    Request(String method, String url, String httpVersion, List<String> rawHeaders) {
      this.method = method;
      this.url = url;
      this.httpVersion = httpVersion;
      this.rawHeaders = Collections.unmodifiableList(rawHeaders);
      Map<String, String> headers = new LinkedHashMap<>();
      for (int i = 0; i < rawHeaders.size(); i += 2) {
        String name = rawHeaders.get(i).toLowerCase(Locale.ROOT);
        String value = rawHeaders.get(i + 1);
        String existing = headers.get(name);
        headers.put(name, existing == null ? value : existing + ", " + value);
      }
      this.headers = Collections.unmodifiableMap(headers);
    }

    public String header(String name) {
      return headers.get(name.toLowerCase(Locale.ROOT));
    }
  }

  /** The options of {@link #forwardTo(int, ForwardOptions)}. */
  public static final class ForwardOptions {
    public boolean allowConnectRequests;
    public String removePrefix;
    public boolean preserveHostname;

    public ForwardOptions setAllowConnectRequests(boolean allowConnectRequests) {
      this.allowConnectRequests = allowConnectRequests;
      return this;
    }

    public ForwardOptions setRemovePrefix(String removePrefix) {
      this.removePrefix = removePrefix;
      return this;
    }

    public ForwardOptions setPreserveHostname(boolean preserveHostname) {
      this.preserveHostname = preserveHostname;
      return this;
    }
  }

  // Certain browsers perform telemetry requests which we want to ignore.
  private static final Set<String> CONNECT_HOSTS_TO_IGNORE = new HashSet<>(
    Arrays.asList("www.bing.com:443", "www.google.com:443"));

  private static final Set<String> HOP_BY_HOP = new HashSet<>(Arrays.asList(
    "connection", "keep-alive", "proxy-authenticate", "proxy-authorization", "te", "trailers",
    "transfer-encoding", "upgrade"));

  private static final String VIA = "1.1 localhost (proxy/1.0.0)";

  public final String HOST;
  public final int PORT;
  public final String URL;

  public final List<String> connectHosts = new CopyOnWriteArrayList<>();
  public final List<String> requestUrls = new CopyOnWriteArrayList<>();
  public final List<String> requestHosts = new CopyOnWriteArrayList<>();
  public final List<String> wsUrls = new CopyOnWriteArrayList<>();

  private static final class Forwarding {
    final int port;
    final ForwardOptions options;

    Forwarding(int port, ForwardOptions options) {
      this.port = port;
      this.options = options;
    }
  }

  private final ServerSocket serverSocket;
  private final ExecutorService executor = Executors.newCachedThreadPool(runnable -> {
    Thread thread = new Thread(runnable, "ProxyServer connection");
    thread.setDaemon(true);
    return thread;
  });
  private final Set<Socket> sockets = ConcurrentHashMap.newKeySet();
  // Latest first, like the prepended listeners upstream.
  private final List<Forwarding> forwardings = new CopyOnWriteArrayList<>();
  private volatile Predicate<Request> authHandler;
  private volatile boolean stopped;

  /** Port 0 means any free port. */
  public static ProxyServer create(int port) throws IOException {
    return new ProxyServer(port);
  }

  private ProxyServer(int port) throws IOException {
    serverSocket = new ServerSocket(port);
    PORT = serverSocket.getLocalPort();
    URL = "http://localhost:" + PORT;
    HOST = "localhost:" + PORT;
    Thread acceptThread = new Thread(this::acceptLoop, "ProxyServer accept :" + PORT);
    acceptThread.setDaemon(true);
    acceptThread.start();
  }

  public void forwardTo(int port) {
    forwardTo(port, new ForwardOptions());
  }

  public void forwardTo(int port, ForwardOptions options) {
    forwardings.add(0, new Forwarding(port, options == null ? new ForwardOptions() : options));
  }

  /** Requests the handler rejects, or that make it throw, get 407 Proxy Authentication Required. */
  public void setAuthHandler(Predicate<Request> handler) {
    authHandler = handler;
  }

  public void reset() {
    connectHosts.clear();
    requestUrls.clear();
    requestHosts.clear();
    wsUrls.clear();
    forwardings.clear();
    authHandler = null;
  }

  public void stop() {
    stopped = true;
    reset();
    for (Socket socket : sockets) {
      SocksForwardingServer.closeQuietly(socket);
    }
    sockets.clear();
    try {
      serverSocket.close();
    } catch (IOException e) {
      // The socket is going away anyway.
    }
    executor.shutdownNow();
  }

  @Override
  public void close() {
    stop();
  }

  @Override
  public String toString() {
    return "ProxyServer(" + URL + ")";
  }

  private void acceptLoop() {
    while (!stopped) {
      Socket socket;
      try {
        socket = serverSocket.accept();
      } catch (IOException e) {
        if (stopped) {
          return;
        }
        continue;
      }
      sockets.add(socket);
      executor.execute(() -> serve(socket));
    }
  }

  private void serve(Socket socket) {
    try {
      socket.setTcpNoDelay(true);
      InputStream in = new BufferedInputStream(socket.getInputStream());
      OutputStream out = socket.getOutputStream();
      while (!socket.isClosed()) {
        Request request = readRequest(in);
        if (request == null) {
          break;
        }
        if ("CONNECT".equalsIgnoreCase(request.method)) {
          onConnect(request, socket, in, out);
          break;
        }
        if (request.header("upgrade") != null) {
          onUpgrade(request, socket, in, out);
          break;
        }
        if (!onRequest(request, socket, in, out)) {
          break;
        }
      }
    } catch (IOException | RuntimeException e) {
      // ECONNRESET and the like are legit errors given that tab closing aborts outgoing
      // connections to the server.
    } finally {
      SocksForwardingServer.closeQuietly(socket);
      sockets.remove(socket);
    }
  }

  // Requests.

  private boolean onRequest(Request original, Socket socket, InputStream in, OutputStream out) throws IOException {
    byte[] body = readBody(in, original);
    boolean clientKeepAlive = keepAlive(original);
    String url = original.url;
    for (Forwarding forwarding : forwardings) {
      requestUrls.add(url);
      requestHosts.add(original.header("host"));
      url = rewrite(url, original.header("host"), forwarding, false);
    }
    Request request = new Request(original.method, url, original.httpVersion, original.rawHeaders);
    if (!authenticate(request)) {
      writeSimple(out, 407, "Proxy Authentication Required", "Proxy-Authenticate: Basic realm=\"proxy\"\r\n",
        "Proxy authorization required", clientKeepAlive);
      return clientKeepAlive;
    }
    Url target = Url.parse(url, request.header("host"));
    if (!"http".equals(target.scheme)) {
      writeSimple(out, 400, "Bad Request", "",
        "Only \"http:\" protocol prefix is supported (got: \"" + target.scheme + ":\")\n", clientKeepAlive);
      return clientKeepAlive;
    }

    Socket upstream = new Socket();
    sockets.add(upstream);
    try {
      try {
        upstream.connect(new InetSocketAddress(target.hostname(), target.port()));
      } catch (UnknownHostException e) {
        writeSimple(out, 404, "Not Found", "", "", clientKeepAlive);
        return clientKeepAlive;
      } catch (IOException e) {
        writeSimple(out, 500, "Internal Server Error", "", "", clientKeepAlive);
        return clientKeepAlive;
      }
      sendUpstreamRequest(request, target, body, socket, upstream.getOutputStream());
      return relayResponse(request, upstream.getInputStream(), out, clientKeepAlive);
    } catch (IOException e) {
      // The upstream failed before or while answering; nothing sensible is left to send.
      return false;
    } finally {
      SocksForwardingServer.closeQuietly(upstream);
      sockets.remove(upstream);
    }
  }

  private void sendUpstreamRequest(Request request, Url target, byte[] body, Socket client, OutputStream out) throws IOException {
    StringBuilder head = new StringBuilder();
    head.append(request.method).append(' ').append(target.pathWithSearch()).append(" HTTP/1.1\r\n");
    boolean hasXForwardedFor = false;
    boolean hasVia = false;
    boolean hasHost = false;
    String remoteAddress = client.getInetAddress().getHostAddress();
    for (int i = 0; i < request.rawHeaders.size(); i += 2) {
      String key = request.rawHeaders.get(i);
      String value = request.rawHeaders.get(i + 1);
      String lower = key.toLowerCase(Locale.ROOT);
      if (!hasXForwardedFor && "x-forwarded-for".equals(lower)) {
        hasXForwardedFor = true;
        value += ", " + remoteAddress;
      }
      if (!hasVia && "via".equals(lower)) {
        hasVia = true;
        value += ", " + VIA;
      }
      if (HOP_BY_HOP.contains(lower) || "content-length".equals(lower)) {
        continue;
      }
      if ("host".equals(lower)) {
        hasHost = true;
      }
      head.append(key).append(": ").append(value).append("\r\n");
    }
    if (!hasHost) {
      head.append("Host: ").append(target.authority()).append("\r\n");
    }
    if (!hasXForwardedFor) {
      head.append("X-Forwarded-For: ").append(remoteAddress).append("\r\n");
    }
    if (!hasVia) {
      head.append("Via: ").append(VIA).append("\r\n");
    }
    if (body.length > 0 || request.header("content-length") != null || request.header("transfer-encoding") != null) {
      head.append("Content-Length: ").append(body.length).append("\r\n");
    }
    head.append("Connection: close\r\n\r\n");
    out.write(head.toString().getBytes(StandardCharsets.ISO_8859_1));
    out.write(body);
    out.flush();
  }

  /** Reads the upstream response and writes it to the client, returns whether the client socket can be reused. */
  private boolean relayResponse(Request request, InputStream upstreamIn, OutputStream out, boolean clientKeepAlive) throws IOException {
    InputStream in = new BufferedInputStream(upstreamIn);
    String statusLine = readLine(in);
    if (statusLine == null) {
      writeSimple(out, 500, "Internal Server Error", "", "", clientKeepAlive);
      return clientKeepAlive;
    }
    String[] parts = statusLine.split(" ", 3);
    int status = Integer.parseInt(parts[1].trim());
    String reason = parts.length > 2 ? parts[2] : "";
    List<String> rawHeaders = readHeaders(in);
    Request response = new Request("", "", "1.1", rawHeaders);
    boolean noBody = request.method.equalsIgnoreCase("HEAD") || status == 204 || status == 304 || (status >= 100 && status < 200);
    byte[] body = noBody ? new byte[0] : readBody(in, response);
    // Responses without a length are delimited by the close of the connection.
    if (!noBody && response.header("content-length") == null && !"chunked".equalsIgnoreCase(response.header("transfer-encoding"))) {
      body = concat(body, readAll(in));
    }
    StringBuilder head = new StringBuilder();
    head.append("HTTP/1.1 ").append(status).append(' ').append(reason).append("\r\n");
    for (int i = 0; i < rawHeaders.size(); i += 2) {
      String lower = rawHeaders.get(i).toLowerCase(Locale.ROOT);
      if (HOP_BY_HOP.contains(lower)) {
        continue;
      }
      if ("content-length".equals(lower) && !noBody) {
        continue;
      }
      head.append(rawHeaders.get(i)).append(": ").append(rawHeaders.get(i + 1)).append("\r\n");
    }
    if (!noBody) {
      head.append("Content-Length: ").append(body.length).append("\r\n");
    }
    head.append(clientKeepAlive ? "Connection: keep-alive\r\n" : "Connection: close\r\n").append("\r\n");
    out.write(head.toString().getBytes(StandardCharsets.ISO_8859_1));
    out.write(body);
    out.flush();
    return clientKeepAlive;
  }

  private static void writeSimple(OutputStream out, int status, String reason, String extraHeaders, String body, boolean keepAlive) throws IOException {
    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
    String head = "HTTP/1.1 " + status + " " + reason + "\r\n" +
      extraHeaders +
      "Content-Length: " + bytes.length + "\r\n" +
      (keepAlive ? "Connection: keep-alive\r\n" : "Connection: close\r\n") +
      "\r\n";
    out.write(head.getBytes(StandardCharsets.ISO_8859_1));
    out.write(bytes);
    out.flush();
  }

  private static boolean keepAlive(Request request) {
    String connection = request.header("connection");
    if (request.httpVersion.equals("1.0")) {
      return connection != null && connection.equalsIgnoreCase("keep-alive");
    }
    return connection == null || !connection.equalsIgnoreCase("close");
  }

  // CONNECT.

  private void onConnect(Request original, Socket socket, InputStream in, OutputStream out) throws IOException {
    String url = original.url;
    for (Forwarding forwarding : forwardings) {
      if (!forwarding.options.allowConnectRequests) {
        continue;
      }
      if (CONNECT_HOSTS_TO_IGNORE.contains(url)) {
        continue;
      }
      connectHosts.add(url);
      url = "127.0.0.1:" + forwarding.port;
    }
    Request request = new Request(original.method, url, original.httpVersion, original.rawHeaders);
    if (!authenticate(request)) {
      // Like upstream, the socket is closed after the answer.
      writeSimple(out, 407, "Proxy Authentication Required", "Proxy-Authenticate: Basic realm=\"proxy\"\r\n",
        "Proxy authorization required", false);
      return;
    }
    // `url` should look like "example.com:443".
    int lastColon = url.lastIndexOf(':');
    String host = url.substring(0, lastColon).replaceAll("^\\[|\\]$", "");
    int port = Integer.parseInt(url.substring(lastColon + 1));
    Socket target = new Socket();
    sockets.add(target);
    try {
      try {
        target.connect(new InetSocketAddress(host, port));
      } catch (UnknownHostException e) {
        writeSimple(out, 404, "Not Found", "", "", false);
        return;
      } catch (IOException e) {
        writeSimple(out, 500, "Internal Server Error", "", "", false);
        return;
      }
      out.write("HTTP/1.1 200 Connection established\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1));
      out.flush();
      pipe(socket, in, out, target);
    } finally {
      SocksForwardingServer.closeQuietly(target);
      sockets.remove(target);
    }
  }

  // WebSocket upgrades.

  private void onUpgrade(Request original, Socket socket, InputStream in, OutputStream out) throws IOException {
    String url = original.url;
    for (Forwarding forwarding : forwardings) {
      wsUrls.add(url);
      url = rewrite(url, original.header("host"), forwarding, true);
    }
    Url target = Url.parse(url, original.header("host"));
    if (!target.absolute) {
      // http.request() of Node.js fails on a url without a host and the socket is destroyed.
      return;
    }
    Socket upstream = new Socket();
    sockets.add(upstream);
    try {
      try {
        upstream.connect(new InetSocketAddress(target.hostname(), target.port()));
      } catch (IOException e) {
        return;
      }
      StringBuilder head = new StringBuilder();
      head.append(original.method).append(' ').append(target.pathWithSearch()).append(" HTTP/1.1\r\n");
      for (int i = 0; i < original.rawHeaders.size(); i += 2) {
        head.append(original.rawHeaders.get(i)).append(": ").append(original.rawHeaders.get(i + 1)).append("\r\n");
      }
      head.append("\r\n");
      OutputStream upstreamOut = upstream.getOutputStream();
      upstreamOut.write(head.toString().getBytes(StandardCharsets.ISO_8859_1));
      upstreamOut.flush();
      pipe(socket, in, out, upstream);
    } finally {
      SocksForwardingServer.closeQuietly(upstream);
      sockets.remove(upstream);
    }
  }

  /** Copies bytes both ways until either side ends, then closes both. */
  private static void pipe(Socket client, InputStream clientIn, OutputStream clientOut, Socket target) throws IOException {
    Thread thread = new Thread(() -> {
      try {
        SocksForwardingServer.Socks5.copy(target.getInputStream(), clientOut);
      } catch (IOException e) {
        // Closed.
      } finally {
        SocksForwardingServer.closeQuietly(client);
        SocksForwardingServer.closeQuietly(target);
      }
    }, "ProxyServer pipe");
    thread.setDaemon(true);
    thread.start();
    try {
      SocksForwardingServer.Socks5.copy(clientIn, target.getOutputStream());
    } catch (IOException e) {
      // Closed.
    } finally {
      SocksForwardingServer.closeQuietly(client);
      SocksForwardingServer.closeQuietly(target);
    }
  }

  // Helpers.

  private boolean authenticate(Request request) {
    Predicate<Request> handler = authHandler;
    if (handler == null) {
      return true;
    }
    try {
      return handler.test(request);
    } catch (Throwable e) {
      return false;
    }
  }

  /** The {@code request} and {@code upgrade} listeners of {@code forwardTo}. */
  private static String rewrite(String requestUrl, String hostHeader, Forwarding forwarding, boolean upgrade) {
    Url url = Url.parse(requestUrl, hostHeader);
    if (forwarding.options.preserveHostname) {
      url.port = forwarding.port;
    } else {
      url.hostname = "127.0.0.1";
      url.port = forwarding.port;
    }
    if (forwarding.options.removePrefix != null && !forwarding.options.removePrefix.isEmpty()) {
      String path = url.path.replaceFirst(java.util.regex.Pattern.quote(forwarding.options.removePrefix), "");
      url.path = path.isEmpty() ? "/" : path;
    }
    if (upgrade) {
      if (url.scheme.equals("ws")) {
        url.scheme = "http";
      } else if (url.scheme.equals("wss")) {
        url.scheme = "https";
      }
    }
    return url.toString();
  }

  /** The parts of an absolute url, or of a path that is resolved against the Host header. */
  private static final class Url {
    String scheme;
    String hostname;
    int port;
    String path;
    String search;
    boolean absolute;

    static Url parse(String value, String hostHeader) {
      Url url = new Url();
      String rest;
      int schemeEnd = value.indexOf("://");
      if (schemeEnd == -1) {
        url.scheme = "http";
        url.absolute = hostHeader != null;
        rest = (hostHeader == null ? "" : hostHeader) + (value.startsWith("/") ? value : "/" + value);
      } else {
        url.scheme = value.substring(0, schemeEnd).toLowerCase(Locale.ROOT);
        url.absolute = true;
        rest = value.substring(schemeEnd + 3);
      }
      int hash = rest.indexOf('#');
      if (hash != -1) {
        rest = rest.substring(0, hash);
      }
      int authorityEnd = rest.length();
      for (int i = 0; i < rest.length(); i++) {
        char c = rest.charAt(i);
        if (c == '/' || c == '?') {
          authorityEnd = i;
          break;
        }
      }
      String authority = rest.substring(0, authorityEnd);
      String pathAndSearch = rest.substring(authorityEnd);
      int at = authority.lastIndexOf('@');
      if (at != -1) {
        authority = authority.substring(at + 1);
      }
      int colon = authority.lastIndexOf(':');
      if (colon != -1 && authority.indexOf(']', colon) == -1) {
        url.hostname = authority.substring(0, colon);
        String portText = authority.substring(colon + 1);
        url.port = portText.isEmpty() ? -1 : Integer.parseInt(portText);
      } else {
        url.hostname = authority;
        url.port = -1;
      }
      int question = pathAndSearch.indexOf('?');
      url.path = question == -1 ? pathAndSearch : pathAndSearch.substring(0, question);
      url.search = question == -1 ? "" : pathAndSearch.substring(question);
      if (url.path.isEmpty()) {
        url.path = "/";
      }
      return url;
    }

    int defaultPort() {
      return scheme.equals("https") || scheme.equals("wss") ? 443 : 80;
    }

    int port() {
      return port == -1 ? defaultPort() : port;
    }

    String hostname() {
      return hostname.replaceAll("^\\[|\\]$", "");
    }

    String authority() {
      return hostname + (port == -1 || port == defaultPort() ? "" : ":" + port);
    }

    String pathWithSearch() {
      return path + search;
    }

    @Override
    public String toString() {
      return scheme + "://" + authority() + path + search;
    }
  }

  // HTTP parsing.

  private static Request readRequest(InputStream in) throws IOException {
    String requestLine = readLine(in);
    while (requestLine != null && requestLine.isEmpty()) {
      requestLine = readLine(in);
    }
    if (requestLine == null) {
      return null;
    }
    String[] parts = requestLine.split(" ", 3);
    if (parts.length < 3) {
      throw new IOException("Malformed request line: " + requestLine);
    }
    return new Request(parts[0], parts[1], parts[2].replace("HTTP/", ""), readHeaders(in));
  }

  private static List<String> readHeaders(InputStream in) throws IOException {
    List<String> rawHeaders = new ArrayList<>();
    for (String line = readLine(in); line != null && !line.isEmpty(); line = readLine(in)) {
      int colon = line.indexOf(':');
      if (colon == -1) {
        throw new IOException("Malformed header: " + line);
      }
      rawHeaders.add(line.substring(0, colon).trim());
      rawHeaders.add(line.substring(colon + 1).trim());
    }
    return rawHeaders;
  }

  private static byte[] readBody(InputStream in, Request message) throws IOException {
    ByteArrayOutputStream body = new ByteArrayOutputStream();
    if ("chunked".equalsIgnoreCase(message.header("transfer-encoding"))) {
      while (true) {
        String sizeLine = readLine(in);
        if (sizeLine == null) {
          throw new IOException("Unexpected end of chunked body");
        }
        int semicolon = sizeLine.indexOf(';');
        int size = Integer.parseInt((semicolon == -1 ? sizeLine : sizeLine.substring(0, semicolon)).trim(), 16);
        if (size == 0) {
          for (String line = readLine(in); line != null && !line.isEmpty(); line = readLine(in)) {
            // Trailers are ignored.
          }
          break;
        }
        body.write(readFully(in, size));
        readLine(in);
      }
    } else if (message.header("content-length") != null) {
      body.write(readFully(in, Integer.parseInt(message.header("content-length").trim())));
    }
    return body.toByteArray();
  }

  private static byte[] readFully(InputStream in, int size) throws IOException {
    byte[] data = new byte[size];
    int offset = 0;
    while (offset < size) {
      int read = in.read(data, offset, size - offset);
      if (read == -1) {
        throw new IOException("Unexpected end of body");
      }
      offset += read;
    }
    return data;
  }

  private static byte[] readAll(InputStream in) throws IOException {
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    byte[] buffer = new byte[16 * 1024];
    int read;
    while ((read = in.read(buffer)) != -1) {
      result.write(buffer, 0, read);
    }
    return result.toByteArray();
  }

  private static byte[] concat(byte[] a, byte[] b) {
    byte[] result = Arrays.copyOf(a, a.length + b.length);
    System.arraycopy(b, 0, result, a.length, b.length);
    return result;
  }

  /** A line without its CRLF, or null at the end of the stream. */
  private static String readLine(InputStream in) throws IOException {
    ByteArrayOutputStream line = new ByteArrayOutputStream();
    while (true) {
      int b = in.read();
      if (b == -1) {
        return line.size() == 0 ? null : line.toString("ISO-8859-1");
      }
      if (b == '\n') {
        byte[] bytes = line.toByteArray();
        int length = bytes.length > 0 && bytes[bytes.length - 1] == '\r' ? bytes.length - 1 : bytes.length;
        return new String(bytes, 0, length, StandardCharsets.ISO_8859_1);
      }
      line.write(b);
    }
  }
}
