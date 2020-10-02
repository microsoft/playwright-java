/**
 * Copyright (c) Microsoft Corporation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.FileSystems;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import static java.util.Collections.singletonList;

public class Server implements HttpHandler {
  private static final int port = 8907;
  private static final int httpsPort = 8908;
  private final HttpServer server;

  public final String PREFIX;
  public final String CROSS_PROCESS_PREFIX;
  public final int PORT;
  public final String EMPTY_PAGE;
  private final File resourcesDir;

  private final Map<String, CompletableFuture<Request>> requestSubscribers = Collections.synchronizedMap(new HashMap<>());
  private final Map<String, Auth> auths = Collections.synchronizedMap(new HashMap<>());

  private static class Auth {
    public final String user;
    public final String password;

    private Auth(String user, String password) {
      this.user = user;
      this.password = password;
    }
  }

  Server(int port) throws IOException {
    PORT = port;
    PREFIX = "http://localhost:" + PORT;
    CROSS_PROCESS_PREFIX = "http://127.0.0.1:" + PORT;
    EMPTY_PAGE = PREFIX + "/empty.html";

    server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
    server.createContext("/", this);
    server.setExecutor(null); // creates a default executor

    File cwd = FileSystems.getDefault().getPath(".").toFile();
    resourcesDir = new File(cwd, "src/test/resources");
    server.start();
  }

  void stop() {
    server.stop(0);
  }

  void setAuth(String path, String user, String password) {
    auths.put(path, new Auth(user, password));
  }

  public static class Request {
    // TODO: make a copy to ensure thread safety?
    public final Headers headers;

    public Request(Headers headers) {
      this.headers = headers;
    }
  }

  Future<Request> waitForRequest(String path) {
    CompletableFuture<Request> future = requestSubscribers.get(path);
    if (future == null) {
      future = new CompletableFuture<>();
      requestSubscribers.put(path, future);
    }
    return future;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    String path = exchange.getRequestURI().getPath();

    if (auths.containsKey(path)) {
      List<String> header = exchange.getRequestHeaders().get("authorization");
      boolean authorized = false;
      if (header != null) {
        String v = header.get(0);
        String[] splits = v.split(" ");
        if (splits.length == 2) {
          String credentials = new String(Base64.getDecoder().decode(splits[1]));
          Auth auth = auths.get(path);
          authorized = credentials.equals(auth.user + ":" + auth.password);
        }
      }
      if (!authorized) {
        exchange.getResponseHeaders().put("WWW-Authenticate", Arrays.asList("Basic realm=\"Secure Area\""));
        exchange.sendResponseHeaders(401, 0);
        try (Writer writer = new OutputStreamWriter(exchange.getResponseBody())) {
          writer.write("HTTP Error 401 Unauthorized: Access is denied");
          // TODO: notify subscriber?
          exchange.getResponseBody().close();
        }
        return;
      }
    }

    File file = new File(resourcesDir, path.substring(1));
    exchange.getResponseHeaders().put("Content-Type", singletonList(mimeType(file)));
    try (FileInputStream input = new FileInputStream(file)) {
      exchange.sendResponseHeaders(200, 0);
      copy(input, exchange.getResponseBody());
    } catch (IOException e) {
      exchange.sendResponseHeaders(404, 0);
      try (Writer writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("File not found: " + file.getCanonicalPath());
      }
    }
    exchange.getResponseBody().close();

    synchronized (requestSubscribers) {
      CompletableFuture<Request> subscriber = requestSubscribers.get(path);
      if (subscriber != null) {
        requestSubscribers.remove(path);
        subscriber.complete(new Request(exchange.getRequestHeaders()));
      }
    }
  }

  private static void copy(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[8192];
    int read;
    while ((read = in.read(buffer, 0, 8192)) != -1) {
      out.write(buffer, 0, read);
    }
  }

  private static String mimeType(File file) {
    String name = file.getName();
    int lastDotPos = name.lastIndexOf('.');
    String extension = lastDotPos == -1 ? name : name.substring(lastDotPos + 1);
    String mimeType = extensionToMime.get(extension);
    if (mimeType == null) {
      mimeType = "application/octet-stream";
    }
    return mimeType;
  }

  private static Map<String, String> extensionToMime = new HashMap<>();
  static {
    extensionToMime.put("ai", "application/postscript");
    extensionToMime.put("apng", "image/apng");
    extensionToMime.put("appcache", "text/cache-manifest");
    extensionToMime.put("au", "audio/basic");
    extensionToMime.put("bmp", "image/bmp");
    extensionToMime.put("cer", "application/pkix-cert");
    extensionToMime.put("cgm", "image/cgm");
    extensionToMime.put("coffee", "text/coffeescript");
    extensionToMime.put("conf", "text/plain");
    extensionToMime.put("crl", "application/pkix-crl");
    extensionToMime.put("css", "text/css");
    extensionToMime.put("csv", "text/csv");
    extensionToMime.put("def", "text/plain");
    extensionToMime.put("doc", "application/msword");
    extensionToMime.put("dot", "application/msword");
    extensionToMime.put("drle", "image/dicom-rle");
    extensionToMime.put("dtd", "application/xml-dtd");
    extensionToMime.put("ear", "application/java-archive");
    extensionToMime.put("emf", "image/emf");
    extensionToMime.put("eps", "application/postscript");
    extensionToMime.put("exr", "image/aces");
    extensionToMime.put("fits", "image/fits");
    extensionToMime.put("g3", "image/g3fax");
    extensionToMime.put("gbr", "application/rpki-ghostbusters");
    extensionToMime.put("gif", "image/gif");
    extensionToMime.put("glb", "model/gltf-binary");
    extensionToMime.put("gltf", "model/gltf+json");
    extensionToMime.put("gz", "application/gzip");
    extensionToMime.put("h261", "video/h261");
    extensionToMime.put("h263", "video/h263");
    extensionToMime.put("h264", "video/h264");
    extensionToMime.put("heic", "image/heic");
    extensionToMime.put("heics", "image/heic-sequence");
    extensionToMime.put("heif", "image/heif");
    extensionToMime.put("heifs", "image/heif-sequence");
    extensionToMime.put("htm", "text/html");
    extensionToMime.put("html", "text/html");
    extensionToMime.put("ics", "text/calendar");
    extensionToMime.put("ief", "image/ief");
    extensionToMime.put("ifb", "text/calendar");
    extensionToMime.put("iges", "model/iges");
    extensionToMime.put("igs", "model/iges");
    extensionToMime.put("in", "text/plain");
    extensionToMime.put("ini", "text/plain");
    extensionToMime.put("jade", "text/jade");
    extensionToMime.put("jar", "application/java-archive");
    extensionToMime.put("jls", "image/jls");
    extensionToMime.put("jp2", "image/jp2");
    extensionToMime.put("jpe", "image/jpeg");
    extensionToMime.put("jpeg", "image/jpeg");
    extensionToMime.put("jpf", "image/jpx");
    extensionToMime.put("jpg", "image/jpeg");
    extensionToMime.put("jpg2", "image/jp2");
    extensionToMime.put("jpgm", "video/jpm");
    extensionToMime.put("jpgv", "video/jpeg");
    extensionToMime.put("jpm", "image/jpm");
    extensionToMime.put("jpx", "image/jpx");
    extensionToMime.put("js", "application/javascript");
    extensionToMime.put("json", "application/json");
    extensionToMime.put("json5", "application/json5");
    extensionToMime.put("jsx", "text/jsx");
    extensionToMime.put("jxr", "image/jxr");
    extensionToMime.put("kar", "audio/midi");
    extensionToMime.put("ktx", "image/ktx");
    extensionToMime.put("less", "text/less");
    extensionToMime.put("list", "text/plain");
    extensionToMime.put("litcoffee", "text/coffeescript");
    extensionToMime.put("log", "text/plain");
    extensionToMime.put("m1v", "video/mpeg");
    extensionToMime.put("m21", "application/mp21");
    extensionToMime.put("m2a", "audio/mpeg");
    extensionToMime.put("m2v", "video/mpeg");
    extensionToMime.put("m3a", "audio/mpeg");
    extensionToMime.put("m4a", "audio/mp4");
    extensionToMime.put("m4p", "application/mp4");
    extensionToMime.put("man", "text/troff");
    extensionToMime.put("manifest", "text/cache-manifest");
    extensionToMime.put("markdown", "text/markdown");
    extensionToMime.put("mathml", "application/mathml+xml");
    extensionToMime.put("md", "text/markdown");
    extensionToMime.put("mdx", "text/mdx");
    extensionToMime.put("me", "text/troff");
    extensionToMime.put("mesh", "model/mesh");
    extensionToMime.put("mft", "application/rpki-manifest");
    extensionToMime.put("mid", "audio/midi");
    extensionToMime.put("midi", "audio/midi");
    extensionToMime.put("mj2", "video/mj2");
    extensionToMime.put("mjp2", "video/mj2");
    extensionToMime.put("mjs", "application/javascript");
    extensionToMime.put("mml", "text/mathml");
    extensionToMime.put("mov", "video/quicktime");
    extensionToMime.put("mp2", "audio/mpeg");
    extensionToMime.put("mp21", "application/mp21");
    extensionToMime.put("mp2a", "audio/mpeg");
    extensionToMime.put("mp3", "audio/mpeg");
    extensionToMime.put("mp4", "video/mp4");
    extensionToMime.put("mp4a", "audio/mp4");
    extensionToMime.put("mp4s", "application/mp4");
    extensionToMime.put("mp4v", "video/mp4");
    extensionToMime.put("mpe", "video/mpeg");
    extensionToMime.put("mpeg", "video/mpeg");
    extensionToMime.put("mpg", "video/mpeg");
    extensionToMime.put("mpg4", "video/mp4");
    extensionToMime.put("mpga", "audio/mpeg");
    extensionToMime.put("mrc", "application/marc");
    extensionToMime.put("ms", "text/troff");
    extensionToMime.put("msh", "model/mesh");
    extensionToMime.put("n3", "text/n3");
    extensionToMime.put("oga", "audio/ogg");
    extensionToMime.put("ogg", "audio/ogg");
    extensionToMime.put("ogv", "video/ogg");
    extensionToMime.put("ogx", "application/ogg");
    extensionToMime.put("otf", "font/otf");
    extensionToMime.put("p10", "application/pkcs10");
    extensionToMime.put("p7c", "application/pkcs7-mime");
    extensionToMime.put("p7m", "application/pkcs7-mime");
    extensionToMime.put("p7s", "application/pkcs7-signature");
    extensionToMime.put("p8", "application/pkcs8");
    extensionToMime.put("pdf", "application/pdf");
    extensionToMime.put("pki", "application/pkixcmp");
    extensionToMime.put("pkipath", "application/pkix-pkipath");
    extensionToMime.put("png", "image/png");
    extensionToMime.put("ps", "application/postscript");
    extensionToMime.put("pskcxml", "application/pskc+xml");
    extensionToMime.put("qt", "video/quicktime");
    extensionToMime.put("rmi", "audio/midi");
    extensionToMime.put("rng", "application/xml");
    extensionToMime.put("roa", "application/rpki-roa");
    extensionToMime.put("roff", "text/troff");
    extensionToMime.put("rsd", "application/rsd+xml");
    extensionToMime.put("rss", "application/rss+xml");
    extensionToMime.put("rtf", "application/rtf");
    extensionToMime.put("rtx", "text/richtext");
    extensionToMime.put("s3m", "audio/s3m");
    extensionToMime.put("sgi", "image/sgi");
    extensionToMime.put("sgm", "text/sgml");
    extensionToMime.put("sgml", "text/sgml");
    extensionToMime.put("shex", "text/shex");
    extensionToMime.put("shtml", "text/html");
    extensionToMime.put("sil", "audio/silk");
    extensionToMime.put("silo", "model/mesh");
    extensionToMime.put("slim", "text/slim");
    extensionToMime.put("slm", "text/slim");
    extensionToMime.put("snd", "audio/basic");
    extensionToMime.put("spx", "audio/ogg");
    extensionToMime.put("stl", "model/stl");
    extensionToMime.put("styl", "text/stylus");
    extensionToMime.put("stylus", "text/stylus");
    extensionToMime.put("svg", "image/svg+xml");
    extensionToMime.put("svgz", "image/svg+xml");
    extensionToMime.put("t", "text/troff");
    extensionToMime.put("t38", "image/t38");
    extensionToMime.put("text", "text/plain");
    extensionToMime.put("tfx", "image/tiff-fx");
    extensionToMime.put("tif", "image/tiff");
    extensionToMime.put("tiff", "image/tiff");
    extensionToMime.put("tr", "text/troff");
    extensionToMime.put("ts", "video/mp2t");
    extensionToMime.put("tsv", "text/tab-separated-values");
    extensionToMime.put("ttc", "font/collection");
    extensionToMime.put("ttf", "font/ttf");
    extensionToMime.put("ttl", "text/turtle");
    extensionToMime.put("txt", "text/plain");
    extensionToMime.put("uri", "text/uri-list");
    extensionToMime.put("uris", "text/uri-list");
    extensionToMime.put("urls", "text/uri-list");
    extensionToMime.put("vcard", "text/vcard");
    extensionToMime.put("vrml", "model/vrml");
    extensionToMime.put("vtt", "text/vtt");
    extensionToMime.put("war", "application/java-archive");
    extensionToMime.put("wasm", "application/wasm");
    extensionToMime.put("wav", "audio/wav");
    extensionToMime.put("weba", "audio/webm");
    extensionToMime.put("webm", "video/webm");
    extensionToMime.put("webmanifest", "application/manifest+json");
    extensionToMime.put("webp", "image/webp");
    extensionToMime.put("wmf", "image/wmf");
    extensionToMime.put("woff", "font/woff");
    extensionToMime.put("woff2", "font/woff2");
    extensionToMime.put("wrl", "model/vrml");
    extensionToMime.put("x3d", "model/x3d+xml");
    extensionToMime.put("x3db", "model/x3d+fastinfoset");
    extensionToMime.put("x3dbz", "model/x3d+binary");
    extensionToMime.put("x3dv", "model/x3d-vrml");
    extensionToMime.put("x3dvz", "model/x3d+vrml");
    extensionToMime.put("x3dz", "model/x3d+xml");
    extensionToMime.put("xaml", "application/xaml+xml");
    extensionToMime.put("xht", "application/xhtml+xml");
    extensionToMime.put("xhtml", "application/xhtml+xml");
    extensionToMime.put("xm", "audio/xm");
    extensionToMime.put("xml", "text/xml");
    extensionToMime.put("xsd", "application/xml");
    extensionToMime.put("xsl", "application/xml");
    extensionToMime.put("xslt", "application/xslt+xml");
    extensionToMime.put("yaml", "text/yaml");
    extensionToMime.put("yml", "text/yaml");
    extensionToMime.put("zip", "application/zip");
  }
}
