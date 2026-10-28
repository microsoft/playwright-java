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

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.zip.GZIPOutputStream;

/**
 * The HTTP (or HTTPS, see {@link HttpsTestServer}) server the tests load pages from, a port of
 * {@code tests/config/testserver/index.ts}. It serves the files of the assets directory, routes
 * registered with {@link #setRoute}, basic auth, gzip, CSP headers and WebSocket connections on
 * {@code /ws}, all on one port.
 *
 * <p>Route handlers get a {@link Request} and a {@link Response} with the Node.js API shape the
 * upstream tests use ({@code res.writeHead}, {@code res.setHeader}, {@code res.write},
 * {@code res.end}, {@code req.headers}, {@code req.postBody()}, {@code req.socket().destroy()}).
 * Like Node, headers are buffered until the first {@code write}, {@code end} or
 * {@code flushHeaders}; a response ended in one {@code end(data)} call gets a Content-Length,
 * one written in parts is chunked. A handler runs on a thread of the server and may keep the
 * response open and end it later from another thread.
 */
public class TestServer implements AutoCloseable {
  public interface RouteHandler {
    void handle(Request request, Response response) throws Exception;
  }

  public final int PORT;
  public final String PREFIX;
  public final String CROSS_PROCESS_PREFIX;
  public final String EMPTY_PAGE;
  public final String HOST;
  public final String HOSTNAME;
  public final String HELLO_WORLD;

  private final ServerSocket serverSocket;
  private final Path dirPath;
  private final ExecutorService executor = Executors.newCachedThreadPool(runnable -> {
    Thread thread = new Thread(runnable, "TestServer connection");
    thread.setDaemon(true);
    return thread;
  });
  private final Instant startTime = Instant.now();
  private volatile Path cachedPathPrefix;
  private final Map<String, RouteHandler> routes = new ConcurrentHashMap<>();
  private final Map<String, String> auths = new ConcurrentHashMap<>();
  private final Map<String, String> csp = new ConcurrentHashMap<>();
  private final Map<String, Map<String, String>> extraHeaders = new ConcurrentHashMap<>();
  private final Set<String> gzipRoutes = ConcurrentHashMap.newKeySet();
  private final Map<String, CompletableFuture<Request>> requestSubscribers = new HashMap<>();
  private final List<BiConsumer<ServerWebSocket, Request>> wsConnectionListeners = new ArrayList<>();
  private volatile CompletableFuture<UpgradeActions> upgradeCallback;
  private final Set<Connection> connections = ConcurrentHashMap.newKeySet();
  private volatile boolean stopped;

  public static TestServer createHttp(Path dirPath, int port) throws IOException {
    return new TestServer(dirPath, new ServerSocket(port));
  }

  public static HttpsTestServer createHttps(Path dirPath, int port) throws IOException {
    return new HttpsTestServer(dirPath, port, HttpsTestServer.defaultContext(), false);
  }

  TestServer(Path dirPath, ServerSocket serverSocket) {
    this.dirPath = dirPath;
    this.serverSocket = serverSocket;
    String protocol = serverSocket instanceof SSLServerSocket ? "https" : "http";
    PORT = serverSocket.getLocalPort();
    PREFIX = protocol + "://localhost:" + PORT;
    CROSS_PROCESS_PREFIX = protocol + "://127.0.0.1:" + PORT;
    EMPTY_PAGE = PREFIX + "/empty.html";
    HOST = "localhost:" + PORT;
    HOSTNAME = "localhost";
    HELLO_WORLD = PREFIX + "/hello-world";
    Thread acceptThread = new Thread(this::acceptLoop, "TestServer accept :" + PORT);
    acceptThread.setDaemon(true);
    acceptThread.start();
  }

  public void enableHTTPCache(Path pathPrefix) {
    cachedPathPrefix = pathPrefix.toAbsolutePath().normalize();
  }

  public void setAuth(String path, String username, String password) {
    auths.put(path, username + ":" + password);
  }

  public void enableGzip(String path) {
    gzipRoutes.add(path);
  }

  public void setCSP(String path, String csp) {
    this.csp.put(path, csp);
  }

  public void setExtraHeaders(String path, Map<String, String> headers) {
    extraHeaders.put(path, headers);
  }

  public void setContent(String path, String content, String mimeType) {
    setRoute(path, (req, res) -> {
      res.writeHead(200, "Content-Type", mimeType);
      res.end("text/html".equals(mimeType) ? "<!DOCTYPE html>" + content : content);
    });
  }

  public void setRoute(String path, RouteHandler handler) {
    routes.put(path, handler);
  }

  public void setRedirect(String from, String to) {
    setRoute(from, (req, res) -> {
      Map<String, String> headers = extraHeaders.get(req.url);
      if (headers != null) {
        res.writeHead(302, headers);
      }
      res.writeHead(302, "location", to);
      res.end();
    });
  }

  /**
   * Completes with the next request for {@code path} (path and query) once its route handler has
   * returned, or fails at {@link #reset()}.
   */
  public CompletableFuture<Request> waitForRequest(String path) {
    synchronized (requestSubscribers) {
      return requestSubscribers.computeIfAbsent(path, k -> new CompletableFuture<>());
    }
  }

  public void reset() {
    routes.clear();
    auths.clear();
    csp.clear();
    extraHeaders.clear();
    gzipRoutes.clear();
    upgradeCallback = null;
    synchronized (wsConnectionListeners) {
      wsConnectionListeners.clear();
    }
    closeAllConnections();
    List<Map.Entry<String, CompletableFuture<Request>>> pending;
    synchronized (requestSubscribers) {
      pending = new ArrayList<>(requestSubscribers.entrySet());
      requestSubscribers.clear();
    }
    for (Map.Entry<String, CompletableFuture<Request>> entry : pending) {
      entry.getValue().completeExceptionally(new IllegalStateException("Request " + entry.getKey() + " was not received before the test finished."));
    }
  }

  public void stop() {
    stopped = true;
    reset();
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
    return "TestServer(" + PREFIX + ")";
  }

  private void closeAllConnections() {
    for (Connection connection : new ArrayList<>(connections)) {
      connection.destroy();
    }
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
      Connection connection = new Connection(socket);
      connections.add(connection);
      executor.execute(() -> serve(connection));
    }
  }

  private void serve(Connection connection) {
    try {
      if (connection.socket instanceof SSLSocket) {
        ((SSLSocket) connection.socket).startHandshake();
      }
      PushbackInputStream in = new PushbackInputStream(new BufferedInputStream(connection.socket.getInputStream()));
      while (!connection.isClosed()) {
        Request request = readRequest(in, connection);
        if (request == null) {
          break;
        }
        if ("websocket".equalsIgnoreCase(request.header("upgrade"))) {
          onUpgrade(request, connection, in);
          break;
        }
        Response response = new Response(request, connection);
        onRequest(request, response);
        awaitEndOrDisconnect(response, connection, in);
        if (!response.keepAlive()) {
          break;
        }
      }
    } catch (IOException | RuntimeException e) {
      // The browser closed the connection, which is legit when a tab goes away mid-request,
      // or it is an https connection to an http server.
    } finally {
      connection.destroy();
      connections.remove(connection);
    }
  }

  /**
   * Waits for the handler to end the response. Meanwhile it peeks at the socket, so that a browser
   * closing the connection while a route never answers is noticed and reported to
   * {@link Connection#onClose}, as Node's socket 'close' event is.
   */
  private static void awaitEndOrDisconnect(Response response, Connection connection, PushbackInputStream in) throws IOException {
    Socket socket = connection.socket;
    int previousTimeout = socket.getSoTimeout();
    try {
      socket.setSoTimeout(50);
      while (!response.writableEnded() && !connection.isClosed()) {
        int b;
        try {
          b = in.read();
        } catch (SocketTimeoutException e) {
          continue;
        } catch (IOException e) {
          connection.destroy();
          return;
        }
        if (b == -1) {
          connection.destroy();
          return;
        }
        // The next request arrived early; leave it for the loop and wait for this response to end.
        in.unread(b);
        break;
      }
    } finally {
      if (!connection.isClosed()) {
        socket.setSoTimeout(previousTimeout);
      }
    }
    response.awaitEnd();
  }

  private static Request readRequest(InputStream in, Connection connection) throws IOException {
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
    List<String> rawHeaders = new ArrayList<>();
    for (String line = readLine(in); line != null && !line.isEmpty(); line = readLine(in)) {
      int colon = line.indexOf(':');
      if (colon == -1) {
        throw new IOException("Malformed header: " + line);
      }
      rawHeaders.add(line.substring(0, colon).trim());
      rawHeaders.add(line.substring(colon + 1).trim());
    }
    Request request = new Request(parts[0], parts[1], parts[2].replace("HTTP/", ""), rawHeaders, connection);
    request.postBody = readBody(in, request);
    return request;
  }

  private static byte[] readBody(InputStream in, Request request) throws IOException {
    ByteArrayOutputStream body = new ByteArrayOutputStream();
    if ("chunked".equalsIgnoreCase(request.header("transfer-encoding"))) {
      while (true) {
        String sizeLine = readLine(in);
        if (sizeLine == null) {
          throw new IOException("Unexpected end of chunked body");
        }
        int semicolon = sizeLine.indexOf(';');
        int size = Integer.parseInt((semicolon == -1 ? sizeLine : sizeLine.substring(0, semicolon)).trim(), 16);
        if (size == 0) {
          // Trailers, then the empty line.
          for (String line = readLine(in); line != null && !line.isEmpty(); line = readLine(in)) {
            // Trailers are ignored.
          }
          break;
        }
        body.write(readFully(in, size));
        readLine(in);
      }
    } else if (request.header("content-length") != null) {
      body.write(readFully(in, Integer.parseInt(request.header("content-length").trim())));
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

  private void onRequest(Request request, Response response) {
    String pathWithSearch = request.pathWithSearch();
    String auth = auths.get(pathWithSearch);
    if (auth != null) {
      String authorization = request.header("authorization");
      String[] parts = (authorization == null ? "" : authorization).split(" ");
      String credentials = parts.length > 1 ? new String(Base64.getDecoder().decode(parts[1]), StandardCharsets.UTF_8) : "";
      if (!credentials.equals(auth)) {
        response.writeHead(401, "WWW-Authenticate", "Basic realm=\"Secure Area\"");
        response.end("HTTP Error 401 Unauthorized: Access is denied");
        return;
      }
    }
    CompletableFuture<Request> subscriber;
    synchronized (requestSubscribers) {
      subscriber = requestSubscribers.remove(pathWithSearch);
    }
    RouteHandler handler = routes.get(pathWithSearch);
    try {
      if (handler != null) {
        handler.handle(request, response);
      } else {
        serveFile(request, response);
      }
    } catch (Exception e) {
      System.err.println("TestServer: route handler for " + pathWithSearch + " failed");
      e.printStackTrace();
      request.socket().destroy();
    } finally {
      // The route handler has run when waitForRequest() completes, as in the Node.js server where the
      // handler is synchronous and the awaiting test code resumes after it.
      if (subscriber != null) {
        subscriber.complete(request);
      }
    }
  }

  public void serveFile(Request request, Response response) {
    serveFile(request, response, null);
  }

  public void serveFile(Request request, Response response, Path filePath) {
    String pathName = request.pathName();
    if (filePath == null) {
      if (pathName.equals("/")) {
        pathName = "/index.html";
      }
      filePath = dirPath.resolve(pathName.substring(1));
    }
    filePath = filePath.toAbsolutePath().normalize();

    if (cachedPathPrefix != null && filePath.startsWith(cachedPathPrefix)) {
      if (request.header("if-modified-since") != null) {
        response.statusCode = 304; // not modified
        response.end();
        return;
      }
      response.setHeader("Cache-Control", "public, max-age=31536000, no-cache");
      response.setHeader("Last-Modified", startTime.toString());
    } else {
      response.setHeader("Cache-Control", "no-cache, no-store");
    }
    if (csp.containsKey(pathName)) {
      response.setHeader("Content-Security-Policy", csp.get(pathName));
    }
    if (extraHeaders.containsKey(pathName)) {
      for (Map.Entry<String, String> entry : extraHeaders.get(pathName).entrySet()) {
        response.setHeader(entry.getKey(), entry.getValue());
      }
    }

    byte[] data;
    try {
      data = Files.readAllBytes(filePath);
    } catch (IOException e) {
      response.statusCode = 404;
      response.setHeader("Content-Type", "text/plain");
      response.end(request.method.equals("HEAD") ? null : "File not found: " + filePath);
      return;
    }
    String fileName = filePath.getFileName().toString();
    String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
    String mimeType = mimeType(extension);
    boolean isTextEncoding = mimeType.startsWith("text/") || mimeType.equals("application/javascript") || mimeType.equals("application/json");
    response.setHeader("Content-Type", isTextEncoding ? mimeType + "; charset=utf-8" : mimeType);
    if (gzipRoutes.contains(pathName)) {
      response.setHeader("Content-Encoding", "gzip");
      data = gzip(data);
    }
    response.end(request.method.equals("HEAD") ? null : data);
  }

  private static byte[] gzip(byte[] data) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
      gzip.write(data);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return out.toByteArray();
  }

  private static final Map<String, String> MIME_TYPES = new HashMap<>();

  static {
    String[] types = {
      "html", "text/html", "htm", "text/html", "css", "text/css", "txt", "text/plain", "csv", "text/csv",
      "xml", "text/xml", "md", "text/markdown", "js", "application/javascript", "mjs", "application/javascript",
      "json", "application/json", "map", "application/json", "pdf", "application/pdf", "zip", "application/zip",
      "wasm", "application/wasm", "har", "application/json",
      "png", "image/png", "jpg", "image/jpeg", "jpeg", "image/jpeg", "gif", "image/gif", "svg", "image/svg+xml",
      "ico", "image/x-icon", "webp", "image/webp", "bmp", "image/bmp", "avif", "image/avif",
      "mp4", "video/mp4", "webm", "video/webm", "ogv", "video/ogg", "mp3", "audio/mpeg", "wav", "audio/wav",
      "ogg", "audio/ogg", "woff", "font/woff", "woff2", "font/woff2", "ttf", "font/ttf", "otf", "font/otf",
    };
    for (int i = 0; i < types.length; i += 2) {
      MIME_TYPES.put(types[i], types[i + 1]);
    }
  }

  private static String mimeType(String extension) {
    String type = MIME_TYPES.get(extension.toLowerCase(Locale.ROOT));
    return type == null ? "application/octet-stream" : type;
  }

  // WebSocket.

  public void onceWebSocketConnection(Consumer<ServerWebSocket> handler) {
    onceWebSocketConnection((ws, request) -> handler.accept(ws));
  }

  public void onceWebSocketConnection(BiConsumer<ServerWebSocket, Request> handler) {
    synchronized (wsConnectionListeners) {
      wsConnectionListeners.add(handler);
    }
  }

  public CompletableFuture<Request> waitForWebSocketConnectionRequest() {
    CompletableFuture<Request> result = new CompletableFuture<>();
    onceWebSocketConnection((ws, request) -> result.complete(request));
    return result;
  }

  public CompletableFuture<ServerWebSocket> waitForWebSocket() {
    CompletableFuture<ServerWebSocket> result = new CompletableFuture<>();
    onceWebSocketConnection((ws, request) -> result.complete(ws));
    return result;
  }

  /** Takes over the next upgrade request: the test answers it on the raw socket or calls {@link UpgradeActions#doUpgrade()}. */
  public CompletableFuture<UpgradeActions> waitForUpgrade() {
    CompletableFuture<UpgradeActions> result = new CompletableFuture<>();
    upgradeCallback = result;
    return result;
  }

  public void sendOnWebSocketConnection(String data) {
    onceWebSocketConnection(ws -> ws.send(data));
  }

  private void onUpgrade(Request request, Connection connection, InputStream in) throws IOException {
    UpgradeActions actions = new UpgradeActions(request, connection);
    CompletableFuture<UpgradeActions> callback = upgradeCallback;
    if (callback != null) {
      upgradeCallback = null;
      callback.complete(actions);
      actions.awaitUpgrade();
      if (!actions.upgraded) {
        return;
      }
    } else {
      String pathname = request.pathName();
      if (pathname.equals("/ws-401")) {
        connection.write("HTTP/1.1 401 Unauthorized\r\n\r\nUnauthorized body");
        connection.destroy();
        return;
      }
      if (pathname.equals("/ws-slow")) {
        try {
          Thread.sleep(2000);
        } catch (InterruptedException e) {
          return;
        }
      }
      if (!pathname.equals("/ws") && !pathname.equals("/ws-slow")) {
        connection.write("HTTP/1.1 400 Bad Request\r\n\r\n");
        connection.destroy();
        return;
      }
    }
    ServerWebSocket ws = handshake(request, connection);
    List<BiConsumer<ServerWebSocket, Request>> listeners;
    synchronized (wsConnectionListeners) {
      listeners = new ArrayList<>(wsConnectionListeners);
      wsConnectionListeners.clear();
    }
    for (BiConsumer<ServerWebSocket, Request> listener : listeners) {
      listener.accept(ws, request);
    }
    ws.readLoop(in);
  }

  private static ServerWebSocket handshake(Request request, Connection connection) throws IOException {
    String key = request.header("sec-websocket-key");
    if (key == null) {
      connection.write("HTTP/1.1 400 Bad Request\r\n\r\n");
      connection.destroy();
      throw new IOException("Missing Sec-WebSocket-Key");
    }
    String accept;
    try {
      MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
      accept = Base64.getEncoder().encodeToString(sha1.digest((key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes(StandardCharsets.US_ASCII)));
    } catch (Exception e) {
      throw new IOException(e);
    }
    StringBuilder response = new StringBuilder()
      .append("HTTP/1.1 101 Switching Protocols\r\n")
      .append("Upgrade: websocket\r\n")
      .append("Connection: Upgrade\r\n")
      .append("Sec-WebSocket-Accept: ").append(accept).append("\r\n");
    // Like the ws package, accept the first protocol the client offers.
    String protocols = request.header("sec-websocket-protocol");
    String protocol = null;
    if (protocols != null && !protocols.trim().isEmpty()) {
      protocol = protocols.split(",")[0].trim();
      response.append("Sec-WebSocket-Protocol: ").append(protocol).append("\r\n");
    }
    response.append("\r\n");
    connection.write(response.toString());
    return new ServerWebSocket(connection, protocol);
  }

  /** The request a route handler or {@link #waitForRequest} receives, shaped like Node's {@code IncomingMessage}. */
  public static final class Request {
    public final String method;
    /** The request target as sent, usually the path and query. */
    public final String url;
    public final String httpVersion;
    /** Lower-cased names; repeated headers are joined with {@code ", "} ({@code "; "} for cookies) as Node does. */
    public final Map<String, String> headers;
    /** Names and values alternating, in the order they were sent, with their case. */
    public final List<String> rawHeaders;
    private final Connection connection;
    private byte[] postBody;

    Request(String method, String url, String httpVersion, List<String> rawHeaders, Connection connection) {
      this.method = method;
      this.url = url;
      this.httpVersion = httpVersion;
      this.rawHeaders = Collections.unmodifiableList(rawHeaders);
      this.connection = connection;
      Map<String, String> headers = new LinkedHashMap<>();
      for (int i = 0; i < rawHeaders.size(); i += 2) {
        String name = rawHeaders.get(i).toLowerCase(Locale.ROOT);
        String value = rawHeaders.get(i + 1);
        String existing = headers.get(name);
        headers.put(name, existing == null ? value : existing + (name.equals("cookie") ? "; " : ", ") + value);
      }
      this.headers = Collections.unmodifiableMap(headers);
    }

    public String header(String name) {
      return headers.get(name.toLowerCase(Locale.ROOT));
    }

    /** The request body, already read in full when the handler runs. */
    public byte[] postBody() {
      return postBody;
    }

    public String postBodyText() {
      return new String(postBody, StandardCharsets.UTF_8);
    }

    public Connection socket() {
      return connection;
    }

    String pathWithSearch() {
      String target = url;
      if (target.startsWith("http://") || target.startsWith("https://")) {
        int slash = target.indexOf('/', target.indexOf("//") + 2);
        target = slash == -1 ? "/" : target.substring(slash);
      }
      int hash = target.indexOf('#');
      if (hash != -1) {
        target = target.substring(0, hash);
      }
      if (target.endsWith("?")) {
        target = target.substring(0, target.length() - 1);
      }
      return target;
    }

    String pathName() {
      String path = pathWithSearch();
      int question = path.indexOf('?');
      return question == -1 ? path : path.substring(0, question);
    }

    @Override
    public String toString() {
      return method + " " + url;
    }
  }

  /** The response of a route handler, shaped like Node's {@code ServerResponse}. */
  public static final class Response {
    public int statusCode = 200;
    /** The reason phrase of the status line; the standard one when null. */
    public String statusMessage;
    private final Request request;
    private final Connection connection;
    private final Map<String, List<String>> headers = new LinkedHashMap<>();
    private boolean headersSent;
    private boolean chunked;
    private boolean ended;
    private final CountDownLatch endLatch = new CountDownLatch(1);

    Response(Request request, Connection connection) {
      this.request = request;
      this.connection = connection;
      connection.onClose(endLatch::countDown);
    }

    public Connection socket() {
      return connection;
    }

    public Connection connection() {
      return connection;
    }

    public synchronized void setHeader(String name, String value) {
      removeHeader(name);
      headers.put(name, Collections.singletonList(value));
    }

    /** A header sent several times, one line per value, like an array value in Node. */
    public synchronized void setHeader(String name, List<String> values) {
      removeHeader(name);
      headers.put(name, new ArrayList<>(values));
    }

    public synchronized String getHeader(String name) {
      for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
        if (entry.getKey().equalsIgnoreCase(name)) {
          return String.join(", ", entry.getValue());
        }
      }
      return null;
    }

    public synchronized void removeHeader(String name) {
      headers.keySet().removeIf(key -> key.equalsIgnoreCase(name));
    }

    public void writeHead(int status) {
      statusCode = status;
    }

    /** Status and headers as alternating names and values: {@code writeHead(200, "Content-Type", "text/html")}. */
    public void writeHead(int status, String... nameValuePairs) {
      if (nameValuePairs.length % 2 != 0) {
        throw new IllegalArgumentException("Header names and values must alternate; use writeHead(status, statusMessage, headers) for a status message");
      }
      Map<String, String> map = new LinkedHashMap<>();
      for (int i = 0; i < nameValuePairs.length; i += 2) {
        map.put(nameValuePairs[i], nameValuePairs[i + 1]);
      }
      writeHead(status, map);
    }

    public synchronized void writeHead(int status, Map<String, String> headers) {
      statusCode = status;
      for (Map.Entry<String, String> entry : headers.entrySet()) {
        setHeader(entry.getKey(), entry.getValue());
      }
    }

    /** {@code res.writeHead(status, statusMessage, headers)} of Node. */
    public synchronized void writeHead(int status, String statusMessage, Map<String, String> headers) {
      this.statusMessage = statusMessage;
      writeHead(status, headers);
    }

    public synchronized void flushHeaders() {
      if (!headersSent) {
        sendHeaders(null);
      }
    }

    /** Node buffers writes until the handler returns; here they go out immediately, so this does nothing. */
    public void uncork() {
    }

    public void cork() {
    }

    public void write(String data) {
      write(data.getBytes(StandardCharsets.UTF_8));
    }

    public synchronized void write(byte[] data) {
      if (ended) {
        return;
      }
      if (!headersSent) {
        sendHeaders(null);
      }
      writeBody(data);
    }

    public void end() {
      end((byte[]) null);
    }

    public void end(String data) {
      end(data == null ? null : data.getBytes(StandardCharsets.UTF_8));
    }

    public synchronized void end(byte[] data) {
      if (ended) {
        return;
      }
      ended = true;
      try {
        if (!headersSent) {
          sendHeaders((long) (data == null ? 0 : data.length));
        }
        if (data != null) {
          writeBody(data);
        }
        if (chunked) {
          connection.write("0\r\n\r\n");
        }
      } finally {
        endLatch.countDown();
      }
    }

    public synchronized boolean writableEnded() {
      return ended;
    }

    public boolean finished() {
      return writableEnded();
    }

    private boolean hasBody() {
      return !request.method.equals("HEAD") && statusCode != 204 && statusCode != 304;
    }

    private void sendHeaders(Long contentLength) {
      headersSent = true;
      if (getHeader("Content-Length") == null && getHeader("Transfer-Encoding") == null) {
        if (contentLength != null || !hasBody()) {
          if (hasBody() || contentLength != null && contentLength > 0) {
            setHeader("Content-Length", String.valueOf(contentLength == null ? 0 : contentLength));
          }
        } else {
          setHeader("Transfer-Encoding", "chunked");
          chunked = true;
        }
      } else if ("chunked".equalsIgnoreCase(getHeader("Transfer-Encoding")) && hasBody()) {
        // The route asked for chunked encoding itself; frame the body for it like Node does.
        chunked = true;
      }
      if (getHeader("Date") == null) {
        setHeader("Date", Instant.now().toString());
      }
      if (getHeader("Connection") == null) {
        setHeader("Connection", keepAliveRequested() ? "keep-alive" : "close");
      }
      String reason = statusMessage != null ? statusMessage : reasonPhrase(statusCode);
      StringBuilder head = new StringBuilder("HTTP/1.1 ").append(statusCode).append(' ').append(reason).append("\r\n");
      for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
        for (String value : entry.getValue()) {
          head.append(entry.getKey()).append(": ").append(value).append("\r\n");
        }
      }
      head.append("\r\n");
      connection.write(head.toString().getBytes(StandardCharsets.ISO_8859_1));
    }

    private void writeBody(byte[] data) {
      if (!hasBody() || data.length == 0) {
        return;
      }
      if (chunked) {
        connection.write((Integer.toHexString(data.length) + "\r\n").getBytes(StandardCharsets.ISO_8859_1));
        connection.write(data);
        connection.write("\r\n".getBytes(StandardCharsets.ISO_8859_1));
      } else {
        connection.write(data);
      }
    }

    private boolean keepAliveRequested() {
      String connectionHeader = request.header("connection");
      if (request.httpVersion.equals("1.0")) {
        return connectionHeader != null && connectionHeader.toLowerCase(Locale.ROOT).contains("keep-alive");
      }
      return connectionHeader == null || !connectionHeader.toLowerCase(Locale.ROOT).contains("close");
    }

    synchronized boolean keepAlive() {
      return ended && !connection.isClosed() && keepAliveRequested() && !"close".equalsIgnoreCase(getHeader("Connection"));
    }

    void awaitEnd() {
      try {
        endLatch.await();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  /** The TCP (or TLS) connection of a request, shaped like Node's {@code net.Socket}. */
  public static final class Connection {
    private final Socket socket;
    private final OutputStream out;
    private final List<Runnable> closeListeners = new ArrayList<>();
    private volatile boolean closed;

    Connection(Socket socket) {
      this.socket = socket;
      OutputStream out;
      try {
        out = socket.getOutputStream();
      } catch (IOException e) {
        out = null;
        closed = true;
      }
      this.out = out;
    }

    public void write(String data) {
      write(data.getBytes(StandardCharsets.UTF_8));
    }

    public void write(byte[] data) {
      synchronized (this) {
        if (closed) {
          return;
        }
        try {
          out.write(data);
          out.flush();
        } catch (IOException e) {
          closed = true;
        }
      }
      if (closed) {
        notifyClosed();
      }
    }

    public void uncork() {
    }

    /** Half-closes the connection: the browser sees the end of the response. */
    public void end() {
      try {
        socket.shutdownOutput();
      } catch (IOException e) {
        destroy();
      }
    }

    public void destroy() {
      synchronized (this) {
        if (closed) {
          return;
        }
        closed = true;
        try {
          socket.close();
        } catch (IOException e) {
          // Closing anyway.
        }
      }
      notifyClosed();
    }

    public boolean isClosed() {
      return closed;
    }

    public void onClose(Runnable listener) {
      boolean runNow;
      synchronized (closeListeners) {
        runNow = closed;
        if (!closed) {
          closeListeners.add(listener);
        }
      }
      if (runNow) {
        listener.run();
      }
    }

    /** The TLS session of an https connection, or null. */
    public SSLSession sslSession() {
      return socket instanceof SSLSocket ? ((SSLSocket) socket).getSession() : null;
    }

    private void notifyClosed() {
      List<Runnable> listeners;
      synchronized (closeListeners) {
        listeners = new ArrayList<>(closeListeners);
        closeListeners.clear();
      }
      for (Runnable listener : listeners) {
        listener.run();
      }
    }
  }

  /** An upgrade request handed to the test by {@link #waitForUpgrade()}. */
  public static final class UpgradeActions {
    public final Request request;
    public final Connection socket;
    private final CountDownLatch latch = new CountDownLatch(1);
    private volatile boolean upgraded;

    UpgradeActions(Request request, Connection socket) {
      this.request = request;
      this.socket = socket;
      socket.onClose(latch::countDown);
    }

    /** Completes the WebSocket handshake; the connection then goes to the {@code onceWebSocketConnection} listeners. */
    public void doUpgrade() {
      upgraded = true;
      latch.countDown();
    }

    void awaitUpgrade() {
      try {
        latch.await();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
    }
  }

  private static String reasonPhrase(int status) {
    switch (status) {
      case 101: return "Switching Protocols";
      case 200: return "OK";
      case 201: return "Created";
      case 204: return "No Content";
      case 206: return "Partial Content";
      case 301: return "Moved Permanently";
      case 302: return "Found";
      case 303: return "See Other";
      case 304: return "Not Modified";
      case 307: return "Temporary Redirect";
      case 308: return "Permanent Redirect";
      case 400: return "Bad Request";
      case 401: return "Unauthorized";
      case 403: return "Forbidden";
      case 404: return "Not Found";
      case 405: return "Method Not Allowed";
      case 407: return "Proxy Authentication Required";
      case 500: return "Internal Server Error";
      case 502: return "Bad Gateway";
      case 503: return "Service Unavailable";
      default: return "Unknown";
    }
  }
}
