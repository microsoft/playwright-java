/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright.tests.library;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.Fixture;
import com.microsoft.playwright.fixtures.HttpsTestServer;
import com.microsoft.playwright.fixtures.LaunchPersistent;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.ProxyServer;
import com.microsoft.playwright.fixtures.SocksForwardingServer;
import com.microsoft.playwright.fixtures.TestInfo;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.ClientCertificate;
import com.microsoft.playwright.options.Proxy;
import com.microsoft.playwright.options.SecurityDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIMatcher;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.StandardConstants;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.ExtendedSSLSession;
import javax.security.auth.x500.X500Principal;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.asset;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/library/client-certificates.spec.ts. */
public class TestClientCertificates {
  /**
   * The {@code startCCServer} fixture: an https server that asks for a client certificate and tells
   * what it got. Upstream returns the url from a function; here it is {@link #start()}.
   */
  public static final class StartCCServer implements AutoCloseable {
    public static final class Options {
      boolean useFakeLocalhost;
      boolean rejectUnauthorized;

      public Options setUseFakeLocalhost(boolean useFakeLocalhost) {
        this.useFakeLocalhost = useFakeLocalhost;
        return this;
      }

      public Options setRejectUnauthorized(boolean rejectUnauthorized) {
        this.rejectUnauthorized = rejectUnauthorized;
        return this;
      }
    }

    private final TestInfo testInfo;
    private final List<HttpsTestServer> servers = new ArrayList<>();

    StartCCServer(TestInfo testInfo) {
      this.testInfo = testInfo;
    }

    public String start() throws Exception {
      return start(new Options());
    }

    public String start(Options options) throws Exception {
      SSLContext context = serverContext(testInfo, options.rejectUnauthorized);
      HttpsTestServer server = HttpsTestServer.create(Env.srcTestDir().resolve("assets"), 0, context, true);
      servers.add(server);
      X509Certificate ca = loadCertificate(asset("client-certificates/server/server_cert.pem"));
      server.setRoute("/", (req, res) -> {
        SSLSession session = req.socket().sslSession();
        List<String[]> parts = new ArrayList<>();
        // Java: the ALPN protocol of the TLS socket is not reachable from TestServer.Connection; only the HTTP/2 tests read it.
        parts.add(new String[]{"servername", servername(session)});
        X509Certificate cert = peerCertificate(session);
        if (cert != null && isAuthorized(cert, ca)) {
          res.writeHead(200, "Content-Type", "text/html");
          parts.add(new String[]{"message", "Hello " + commonName(cert.getSubjectX500Principal()) + ", your certificate was issued by " + commonName(cert.getIssuerX500Principal()) + "!"});
        } else if (cert != null) {
          res.writeHead(403, "Content-Type", "text/html");
          parts.add(new String[]{"message", "Sorry " + commonName(cert.getSubjectX500Principal()) + ", certificates from " + commonName(cert.getIssuerX500Principal()) + " are not welcome here."});
        } else {
          res.writeHead(401, "Content-Type", "text/html");
          parts.add(new String[]{"message", "Sorry, but you need to provide a client certificate to continue."});
        }
        StringBuilder body = new StringBuilder();
        for (String[] part : parts) {
          body.append("<div data-testid=\"").append(part[0]).append("\">").append(part[1]).append("</div>");
        }
        res.end(body.toString());
      });
      String host = options.useFakeLocalhost ? "local.playwright" : "127.0.0.1";
      return "https://" + host + ":" + server.PORT + "/";
    }

    @Override
    public void close() {
      for (HttpsTestServer server : servers) {
        server.stop();
      }
    }
  }

  @Fixture
  StartCCServer startCCServer(TestInfo testInfo) {
    return new StartCCServer(testInfo);
  }

  // Java: the helpers below stand for what Node's tls server does for the handler: the certificate of the peer and its verification.
  private static Path serverPkcs12(TestInfo testInfo) throws Exception {
    Path pkcs12 = testInfo.outputPath("client-certificates-server.p12");
    if (!Files.exists(pkcs12)) {
      Process process = new ProcessBuilder("openssl", "pkcs12", "-export",
        "-inkey", asset("client-certificates/server/server_key.pem").toString(),
        "-in", asset("client-certificates/server/server_cert.pem").toString(),
        "-out", pkcs12.toString(), "-passout", "pass:aaaa", "-name", "server")
        .redirectErrorStream(true).start();
      process.getInputStream().close();
      if (!process.waitFor(60, TimeUnit.SECONDS) || process.exitValue() != 0) {
        throw new IOException("openssl failed to create " + pkcs12);
      }
    }
    return pkcs12;
  }

  private static X509Certificate loadCertificate(Path pem) throws Exception {
    try (InputStream in = Files.newInputStream(pem)) {
      return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(in);
    }
  }

  // The server certificate of the assets, which trusts every client certificate, or only the ones issued by itself.
  private static SSLContext serverContext(TestInfo testInfo, boolean rejectUnauthorized) throws Exception {
    Path pkcs12 = serverPkcs12(testInfo);
    if (!rejectUnauthorized) {
      return HttpsTestServer.context(pkcs12, "aaaa");
    }
    KeyStore keyStore = KeyStore.getInstance("PKCS12");
    try (InputStream in = Files.newInputStream(pkcs12)) {
      keyStore.load(in, "aaaa".toCharArray());
    }
    KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    keyManagerFactory.init(keyStore, "aaaa".toCharArray());
    KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
    trustStore.load(null, null);
    trustStore.setCertificateEntry("ca", loadCertificate(asset("client-certificates/server/server_cert.pem")));
    TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
    trustManagerFactory.init(trustStore);
    SSLContext context = SSLContext.getInstance("TLS");
    context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
    return context;
  }

  private static X509Certificate peerCertificate(SSLSession session) {
    try {
      Certificate[] certificates = session.getPeerCertificates();
      return certificates.length > 0 ? (X509Certificate) certificates[0] : null;
    } catch (SSLPeerUnverifiedException e) {
      return null;
    }
  }

  private static boolean isAuthorized(X509Certificate cert, X509Certificate ca) {
    try {
      cert.checkValidity();
      cert.verify(ca.getPublicKey());
      return cert.getIssuerX500Principal().equals(ca.getSubjectX500Principal());
    } catch (Exception e) {
      return false;
    }
  }

  private static String commonName(X500Principal principal) {
    try {
      for (Rdn rdn : new LdapName(principal.getName()).getRdns()) {
        if (rdn.getType().equalsIgnoreCase("CN")) {
          return String.valueOf(rdn.getValue());
        }
      }
    } catch (InvalidNameException e) {
      // No common name.
    }
    return "undefined";
  }

  private static String servername(SSLSession session) {
    if (session instanceof ExtendedSSLSession) {
      for (SNIServerName name : ((ExtendedSSLSession) session).getRequestedServerNames()) {
        if (name instanceof SNIHostName) {
          return ((SNIHostName) name).getAsciiName();
        }
      }
    }
    return "false";
  }

  // Java: stand for `new URL(url).origin`, `.host` and `.port`.
  private static String origin(String url) {
    URI uri = URI.create(url);
    return uri.getScheme() + "://" + uri.getHost() + (uri.getPort() == -1 ? "" : ":" + uri.getPort());
  }

  private static String host(String url) {
    URI uri = URI.create(url);
    return uri.getHost() + (uri.getPort() == -1 ? "" : ":" + uri.getPort());
  }

  private static int port(String url) {
    return URI.create(url).getPort();
  }

  private static ClientCertificate trustedCertificate(String origin) {
    return new ClientCertificate(origin)
      .setCertPath(asset("client-certificates/client/trusted/cert.pem"))
      .setKeyPath(asset("client-certificates/client/trusted/key.pem"));
  }

  private static ClientCertificate selfSignedCertificate(String origin) {
    return new ClientCertificate(origin)
      .setCertPath(asset("client-certificates/client/self-signed/cert.pem"))
      .setKeyPath(asset("client-certificates/client/self-signed/key.pem"));
  }

  private static class ValidationSubTest {
    final List<ClientCertificate> clientCertificates;
    final String expected;

    ValidationSubTest(List<ClientCertificate> clientCertificates, String expected) {
      this.clientCertificates = clientCertificates;
      this.expected = expected;
    }
  }


  // Java: a minimal https server whose handlers renegotiate the TLS connection, as `socket.renegotiate()` of Node's tls does;
  // TestServer.Connection does not expose the SSLSocket.
  private static final class RenegotiatingServer implements AutoCloseable {
    final SSLServerSocket serverSocket;

    RenegotiatingServer(SSLContext context) throws IOException {
      serverSocket = (SSLServerSocket) context.getServerSocketFactory().createServerSocket(0, 50, InetAddress.getByName("localhost"));
      // TLSv1.3 does not support renegotiation
      serverSocket.setEnabledProtocols(new String[]{"TLSv1.2"});
      Thread acceptThread = new Thread(() -> {
        while (!serverSocket.isClosed()) {
          try {
            SSLSocket socket = (SSLSocket) serverSocket.accept();
            Thread thread = new Thread(() -> serve(socket), "TLS renegotiation connection");
            thread.setDaemon(true);
            thread.start();
          } catch (IOException e) {
            // The server is closed.
          }
        }
      }, "TLS renegotiation server");
      acceptThread.setDaemon(true);
      acceptThread.start();
    }

    int port() {
      return serverSocket.getLocalPort();
    }

    private static void renegotiate(SSLSocket socket) throws IOException {
      socket.setWantClientAuth(true);
      socket.startHandshake();
    }

    private static void write(SSLSocket socket, String data) throws IOException {
      socket.getOutputStream().write(data.getBytes(StandardCharsets.UTF_8));
      socket.getOutputStream().flush();
    }

    private static void writeChunk(SSLSocket socket, byte[] data) throws IOException {
      write(socket, Integer.toHexString(data.length) + "\r\n");
      socket.getOutputStream().write(data);
      write(socket, "\r\n");
    }

    private static void serve(SSLSocket socket) {
      try {
        socket.startHandshake();
        InputStream in = socket.getInputStream();
        // The request line and the headers.
        StringBuilder head = new StringBuilder();
        while (!head.toString().endsWith("\r\n\r\n")) {
          int read = in.read();
          if (read == -1) {
            return;
          }
          head.append((char) read);
        }
        String[] lines = head.toString().split("\r\n");
        String url = lines[0].split(" ")[1];
        int contentLength = 0;
        for (String line : lines) {
          if (line.toLowerCase().startsWith("content-length:")) {
            contentLength = Integer.parseInt(line.substring("content-length:".length()).trim());
          }
        }
        if (url.equals("/")) {
          write(socket, "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\nconnection: close\r\nContent-Length: 0\r\n\r\n");
        } else if (url.equals("/from-fetch-api")) {
          write(socket, "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nTransfer-Encoding: chunked\r\nConnection: close\r\n\r\n");
          byte[] body = new byte[contentLength];
          for (int offset = 0; offset < contentLength; ) {
            int read = in.read(body, offset, contentLength - offset);
            if (read == -1) {
              return;
            }
            offset += read;
          }
          writeChunk(socket, ("server received: " + new String(body, StandardCharsets.UTF_8) + "\n").getBytes(StandardCharsets.UTF_8));
          renegotiate(socket);
          for (int i = 0; i < 4; i++) {
            writeChunk(socket, (i + "-from-server\n").getBytes(StandardCharsets.UTF_8));
            // Best-effort to trigger a new chunk
            Thread.sleep(100);
          }
          writeChunk(socket, "server closed the connection".getBytes(StandardCharsets.UTF_8));
          write(socket, "0\r\n\r\n");
        } else if (url.equals("/style.css")) {
          write(socket, "HTTP/1.1 200 OK\r\nContent-Type: text/css\r\nContent-Encoding: gzip\r\nTransfer-Encoding: chunked\r\nConnection: close\r\n\r\n");
          renegotiate(socket);
          String stylesheet = "\n          button {\n            background-color: red;\n          }\n        ";
          ByteArrayOutputStream gzipped = new ByteArrayOutputStream();
          try (GZIPOutputStream gzip = new GZIPOutputStream(gzipped)) {
            gzip.write(stylesheet.getBytes(StandardCharsets.UTF_8));
          }
          byte[] stylesheetBuffer = gzipped.toByteArray();
          for (int i = 0; i < stylesheetBuffer.length; i += 100) {
            writeChunk(socket, Arrays.copyOfRange(stylesheetBuffer, i, Math.min(i + 100, stylesheetBuffer.length)));
            // Best-effort to trigger a new chunk
            Thread.sleep(20);
          }
          write(socket, "0\r\n\r\n");
        } else {
          write(socket, "HTTP/1.1 404 Not Found\r\nConnection: close\r\nContent-Length: 0\r\n\r\n");
        }
      } catch (IOException | InterruptedException e) {
        // The client went away.
      } finally {
        try {
          socket.close();
        } catch (IOException e) {
          // Closing anyway.
        }
      }
    }

    @Override
    public void close() throws IOException {
      serverSocket.close();
    }
  }

  // Java: __filename is the path of any existing file.
  private static final Path kDummyFileName = asset("client-certificates/README.md");
  private static final List<ValidationSubTest> kValidationSubTests = Arrays.asList(
    new ValidationSubTest(Arrays.asList(new ClientCertificate("test")), "None of cert, key, passphrase or pfx is specified"),
    new ValidationSubTest(Arrays.asList(new ClientCertificate("test")
      .setCertPath(kDummyFileName)
      .setKeyPath(kDummyFileName)
      .setPfxPath(kDummyFileName)
      .setPassphrase(kDummyFileName.toString())), "pfx is specified together with cert, key or passphrase"));

  @Nested
  class Fetch {
    @PlaywrightTest
    @DisplayName("CC-001 validate input")
    void validateInput(Playwright playwright) {
      for (ValidationSubTest subTest : kValidationSubTests) {
        PlaywrightException error = assertThrows(PlaywrightException.class,
          () -> playwright.request().newContext(new APIRequest.NewContextOptions().setClientCertificates(subTest.clientCertificates)));
        assertTrue(error.getMessage().contains(subTest.expected));
      }
    }

    @PlaywrightTest
    @DisplayName("CC-002 should fail with no client certificates provided")
    void shouldFailWithNoClientCertificatesProvided(Playwright playwright, StartCCServer startCCServer) throws Exception {
      String serverURL = startCCServer.start();
      APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions().setIgnoreHTTPSErrors(true));
      APIResponse response = request.get(serverURL);
      assertEquals(401, response.status());
      assertTrue(response.text().contains("Sorry, but you need to provide a client certificate to continue."));
      request.dispose();
    }

    @PlaywrightTest
    @DisplayName("CC-003 should keep supporting http")
    void shouldKeepSupportingHttp(Playwright playwright, TestServer server) {
      APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions()
        .setIgnoreHTTPSErrors(true)
        .setClientCertificates(Arrays.asList(trustedCertificate(origin(server.PREFIX)))));
      APIResponse response = request.get(server.PREFIX + "/one-style.html");
      assertEquals(server.PREFIX + "/one-style.html", response.url());
      assertEquals(200, response.status());
      assertTrue(response.text().contains("<div>hello, world!</div>"));
      request.dispose();
    }

    @PlaywrightTest
    @DisplayName("CC-004 should throw with untrusted client certs")
    void shouldThrowWithUntrustedClientCerts(Playwright playwright, StartCCServer startCCServer) throws Exception {
      String serverURL = startCCServer.start();
      APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions()
        .setIgnoreHTTPSErrors(true)
        .setClientCertificates(Arrays.asList(selfSignedCertificate(origin(serverURL)))));
      APIResponse response = request.get(serverURL);
      assertEquals(serverURL, response.url());
      assertEquals(403, response.status());
      assertTrue(response.text().contains("Sorry Bob, certificates from Bob are not welcome here."));
      request.dispose();
    }

    @PlaywrightTest
    @DisplayName("CC-005 pass with trusted client certificates")
    void passWithTrustedClientCertificates(Playwright playwright, StartCCServer startCCServer) throws Exception {
      String serverURL = startCCServer.start();
      APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions()
        .setIgnoreHTTPSErrors(true)
        .setClientCertificates(Arrays.asList(trustedCertificate(origin(serverURL)))));
      APIResponse response = request.get(serverURL);
      assertEquals(serverURL, response.url());
      assertEquals(200, response.status());
      assertTrue(response.text().contains("Hello Alice, your certificate was issued by localhost!"));
      request.dispose();
    }

    @PlaywrightTest
    @DisplayName("CC-006 should not leak client certificate to cross-origin redirect target")
    void shouldNotLeakClientCertificateToCrossOriginRedirectTarget(Playwright playwright, StartCCServer startCCServer, TestInfo testInfo) throws Exception {
      String targetURL = startCCServer.start();

      // Standalone HTTPS server (not cert-required) that 302-redirects to the cert-required server.
      HttpsTestServer redirectServer = HttpsTestServer.create(Env.srcTestDir().resolve("assets"), 0, serverContext(testInfo, false), false);
      redirectServer.setRoute("/redir", (req, res) -> {
        res.writeHead(302, "Location", targetURL);
        res.end();
      });
      // Use 'localhost' for the redirect origin so it differs from the target's '127.0.0.1' origin
      // even though both resolve to the loopback interface and share the same trusted CA.
      String redirectURL = "https://localhost:" + redirectServer.PORT + "/redir";

      APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions()
        .setIgnoreHTTPSErrors(true)
        // Cert is scoped to the redirect *origin*, not the target — it must not follow the redirect.
        .setClientCertificates(Arrays.asList(trustedCertificate(origin(redirectURL)))));
      APIResponse response = request.get(redirectURL);
      assertEquals(targetURL, response.url());
      assertEquals(401, response.status());
      assertTrue(response.text().contains("you need to provide a client certificate"));
      request.dispose();
      redirectServer.stop();
    }

    @PlaywrightTest
    @DisplayName("CC-007 pass with trusted client certificates in pfx format")
    void passWithTrustedClientCertificatesInPfxFormat(Playwright playwright, StartCCServer startCCServer) throws Exception {
      String serverURL = startCCServer.start();
      APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions()
        .setIgnoreHTTPSErrors(true)
        .setClientCertificates(Arrays.asList(new ClientCertificate(origin(serverURL))
          .setPfxPath(asset("client-certificates/client/trusted/cert.pfx"))
          .setPassphrase("secure"))));
      APIResponse response = request.get(serverURL);
      assertEquals(serverURL, response.url());
      assertEquals(200, response.status());
      assertTrue(response.text().contains("Hello Alice, your certificate was issued by localhost!"));
      request.dispose();
    }

    @PlaywrightTest
    @DisplayName("CC-008 pass with trusted client certificates and when a http proxy is used")
    void passWithTrustedClientCertificatesAndWhenAHttpProxyIsUsed(Playwright playwright, StartCCServer startCCServer, ProxyServer proxyServer) throws Exception {
      String serverURL = startCCServer.start();
      proxyServer.forwardTo(port(serverURL), new ProxyServer.ForwardOptions().setAllowConnectRequests(true));
      APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions()
        .setIgnoreHTTPSErrors(true)
        .setClientCertificates(Arrays.asList(trustedCertificate(origin(serverURL))))
        .setProxy("localhost:" + proxyServer.PORT));
      assertEquals(Collections.emptyList(), proxyServer.connectHosts);
      APIResponse response = request.get(serverURL);
      assertEquals(Arrays.asList(host(serverURL)), proxyServer.connectHosts);
      assertEquals(serverURL, response.url());
      assertEquals(200, response.status());
      assertTrue(response.text().contains("Hello Alice, your certificate was issued by localhost!"));
      request.dispose();
    }

    @PlaywrightTest
    @DisplayName("CC-009 pass with trusted client certificates and when a socks proxy is used")
    void passWithTrustedClientCertificatesAndWhenASocksProxyIsUsed(Playwright playwright, StartCCServer startCCServer) throws Exception {
      String serverURL = startCCServer.start();
      int serverPort = port(serverURL);
      // Java: port 0 is any free port, upstream derives one from the worker index.
      SocksForwardingServer socksServer = SocksForwardingServer.create(0, serverPort, serverPort);
      APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions()
        .setIgnoreHTTPSErrors(true)
        .setClientCertificates(Arrays.asList(trustedCertificate(origin(serverURL))))
        .setProxy(socksServer.proxyServerAddr));
      assertEquals(Collections.emptyList(), socksServer.connectHosts);
      APIResponse response = request.get(serverURL);
      assertEquals(Arrays.asList(host(serverURL)), socksServer.connectHosts);
      assertEquals(serverURL, response.url());
      assertEquals(200, response.status());
      assertTrue(response.text().contains("Hello Alice, your certificate was issued by localhost!"));
      request.dispose();
      socksServer.close();
    }

    @PlaywrightTest
    @DisplayName("CC-010 should throw a http error if the pfx passphrase is incorect")
    void shouldThrowAHttpErrorIfThePfxPassphraseIsIncorect(Playwright playwright, StartCCServer startCCServer) throws Exception {
      String serverURL = startCCServer.start();
      APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions()
        .setIgnoreHTTPSErrors(true)
        .setClientCertificates(Arrays.asList(new ClientCertificate(origin(serverURL))
          .setPfxPath(asset("client-certificates/client/trusted/cert.pfx"))
          .setPassphrase("this-password-is-incorrect"))));
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> request.get(serverURL));
      assertTrue(error.getMessage().contains("mac verify failure"));
      request.dispose();
    }

    @PlaywrightTest
    @DisplayName("CC-011 should fail with matching certificates in legacy pfx format")
    void shouldFailWithMatchingCertificatesInLegacyPfxFormat(Playwright playwright, StartCCServer startCCServer) throws Exception {
      String serverURL = startCCServer.start();
      APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions()
        .setIgnoreHTTPSErrors(true)
        .setClientCertificates(Arrays.asList(new ClientCertificate(origin(serverURL))
          .setPfxPath(asset("client-certificates/client/trusted/cert-legacy.pfx"))
          .setPassphrase("secure"))));
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> request.get(serverURL));
      assertTrue(error.getMessage().contains("Unsupported TLS certificate"));
      request.dispose();
    }

    @PlaywrightTest
    @DisplayName("CC-012 should work in the browser with request interception")
    void shouldWorkInTheBrowserWithRequestInterception(Browser browser, Playwright playwright, StartCCServer startCCServer) throws Exception {
      String serverURL = startCCServer.start();
      APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions()
        .setIgnoreHTTPSErrors(true)
        .setClientCertificates(Arrays.asList(trustedCertificate(origin(serverURL)))));
      Page page = browser.newPage(new Browser.NewPageOptions().setIgnoreHTTPSErrors(true));
      page.route("**/*", route -> {
        APIResponse response = request.fetch(route.request());
        route.fulfill(new Route.FulfillOptions().setResponse(response));
      });
      page.navigate(serverURL);
      assertThat(page.getByTestId("message")).hasText("Hello Alice, your certificate was issued by localhost!");
      page.close();
      request.dispose();
    }
  }

  // Java: the describe is named BrowserDescribe, as Browser is the type of the API.
  @Nested
  class BrowserDescribe {
    @PlaywrightTest
    @DisplayName("CC-013 validate input")
    void validateInput(Browser browser) {
      for (ValidationSubTest subTest : kValidationSubTests) {
        PlaywrightException error = assertThrows(PlaywrightException.class,
          () -> browser.newContext(new Browser.NewContextOptions().setClientCertificates(subTest.clientCertificates)));
        assertTrue(error.getMessage().contains(subTest.expected));
      }
    }

    @PlaywrightTest
    @DisplayName("CC-014 should keep supporting http")
    void shouldKeepSupportingHttp(Browser browser, TestServer server) {
      Page page = browser.newPage(new Browser.NewPageOptions()
        .setClientCertificates(Arrays.asList(trustedCertificate(origin(server.PREFIX)))));
      page.navigate(server.PREFIX + "/one-style.html");
      assertThat(page.getByText("hello, world!")).isVisible();
      assertThat(page.locator("body")).hasCSS("background-color", "rgb(255, 192, 203)");
      page.close();
    }

    @PlaywrightTest
    @DisplayName("CC-015 should pass through to non-matching origin with self-signed cert")
    void shouldPassThroughToNonMatchingOriginWithSelfSignedCert(Browser browser, HttpsTestServer httpsServer) {
      httpsServer.setRoute("/hello.html", (req, res) -> {
        res.writeHead(200, "Content-Type", "text/html");
        res.end("<html><body><div data-testid=\"message\">hello</div></body></html>");
      });
      Page page = browser.newPage(new Browser.NewPageOptions()
        .setClientCertificates(Arrays.asList(trustedCertificate("https://not-matching.com"))));
      page.navigate(httpsServer.PREFIX + "/hello.html");
      assertThat(page.getByTestId("message")).hasText("hello");
      page.close();
    }

    @PlaywrightTest
    @DisplayName("CC-016 should not intercept TLS for origins without a client certificate")
    void shouldNotInterceptTLSForOriginsWithoutAClientCertificate(Browser browser, HttpsTestServer httpsServer) {
      // If the proxy intercepted this origin, the browser would see its self-signed cert (CN=localhost)
      // instead of the real server cert (CN=playwright-test).
      Page page = browser.newPage(new Browser.NewPageOptions()
        .setClientCertificates(Arrays.asList(trustedCertificate("https://not-matching.com"))));
      Response response = page.navigate(httpsServer.EMPTY_PAGE);
      assertEquals(true, response.ok());
      SecurityDetails securityDetails = response.securityDetails();
      assertTrue(securityDetails.subjectName.contains("playwright-test"));
      page.close();
    }

    @PlaywrightTest
    @DisplayName("CC-017 should fail with no client certificates")
    void shouldFailWithNoClientCertificates(Browser browser, StartCCServer startCCServer, BrowserName browserName) throws Exception {
      String serverURL = startCCServer.start(new StartCCServer.Options().setUseFakeLocalhost(browserName == BrowserName.WEBKIT && Env.isMac));
      Page page = browser.newPage(new Browser.NewPageOptions()
        .setIgnoreHTTPSErrors(true)
        .setClientCertificates(Arrays.asList(trustedCertificate("https://not-matching.com"))));
      page.navigate(serverURL);
      assertThat(page.getByTestId("message")).hasText("Sorry, but you need to provide a client certificate to continue.");
      page.close();
    }

    @PlaywrightTest
    @DisplayName("CC-018 should fail with self-signed client certificates")
    void shouldFailWithSelfSignedClientCertificates(Browser browser, StartCCServer startCCServer, BrowserName browserName) throws Exception {
      String serverURL = startCCServer.start(new StartCCServer.Options().setUseFakeLocalhost(browserName == BrowserName.WEBKIT && Env.isMac));
      Page page = browser.newPage(new Browser.NewPageOptions()
        .setIgnoreHTTPSErrors(true)
        .setClientCertificates(Arrays.asList(selfSignedCertificate(origin(serverURL)))));
      page.navigate(serverURL);
      assertThat(page.getByTestId("message")).hasText("Sorry Bob, certificates from Bob are not welcome here.");
      page.close();
    }

    @PlaywrightTest
    @DisplayName("CC-019 should complete browser TLS handshake when server rejects client certificate after it")
    void shouldCompleteBrowserTLSHandshakeWhenServerRejectsClientCertificateAfterIt(Browser browser, StartCCServer startCCServer, BrowserName browserName) throws Exception {
      String serverURL = startCCServer.start(new StartCCServer.Options().setRejectUnauthorized(true).setUseFakeLocalhost(browserName == BrowserName.WEBKIT && Env.isMac));
      Page page = browser.newPage(new Browser.NewPageOptions()
        .setIgnoreHTTPSErrors(true)
        .setClientCertificates(Arrays.asList(selfSignedCertificate(origin(serverURL)))));
      // Java: stands for goto(...).catch(e => e); the result is the Response or the PlaywrightException.
      Response response = null;
      PlaywrightException error = null;
      try {
        response = page.navigate(serverURL);
      } catch (PlaywrightException e) {
        error = e;
      }
      if (error != null) {
        // The server closed the connection with FIN after the TLS handshake.
        assertTrue(Pattern.compile("net::ERR_EMPTY_RESPONSE|NS_ERROR_NET_RESET|Connection terminated unexpectedly|The network connection was lost|Failure when receiving data from the peer").matcher(error.getMessage()).find());
      } else {
        // The server reset the connection, which the proxy reports as a client certificate error.
        assertEquals(503, response.status());
        assertThat(page.getByText("Playwright client-certificate error")).isVisible();
      }
      page.close();
    }

    @PlaywrightTest
    @DisplayName("CC-020 should pass with matching certificates")
    void shouldPassWithMatchingCertificates(Browser browser, StartCCServer startCCServer, BrowserName browserName) throws Exception {
      String serverURL = startCCServer.start(new StartCCServer.Options().setUseFakeLocalhost(browserName == BrowserName.WEBKIT && Env.isMac));
      Page page = browser.newPage(new Browser.NewPageOptions()
        .setIgnoreHTTPSErrors(true)
        .setClientCertificates(Arrays.asList(trustedCertificate(origin(serverURL)))));
      page.navigate(serverURL);
      assertThat(page.getByTestId("message")).hasText("Hello Alice, your certificate was issued by localhost!");
      page.close();
    }

    @PlaywrightTest
    @DisplayName("CC-021 should pass with matching certificates when passing as content")
    void shouldPassWithMatchingCertificatesWhenPassingAsContent(Browser browser, StartCCServer startCCServer, BrowserName browserName) throws Exception {
      String serverURL = startCCServer.start(new StartCCServer.Options().setUseFakeLocalhost(browserName == BrowserName.WEBKIT && Env.isMac));
      Page page = browser.newPage(new Browser.NewPageOptions()
        .setIgnoreHTTPSErrors(true)
        .setClientCertificates(Arrays.asList(new ClientCertificate(origin(serverURL))
          .setCert(Files.readAllBytes(asset("client-certificates/client/trusted/cert.pem")))
          .setKey(Files.readAllBytes(asset("client-certificates/client/trusted/key.pem"))))));
      page.navigate(serverURL);
      assertThat(page.getByTestId("message")).hasText("Hello Alice, your certificate was issued by localhost!");
      page.close();
    }

    @PlaywrightTest
    @DisplayName("CC-022 should pass with matching certificates and when a http proxy is used")
    void shouldPassWithMatchingCertificatesAndWhenAHttpProxyIsUsed(Browser browser, StartCCServer startCCServer, BrowserName browserName, ProxyServer proxyServer) throws Exception {
      String serverURL = startCCServer.start(new StartCCServer.Options().setUseFakeLocalhost(browserName == BrowserName.WEBKIT && Env.isMac));
      proxyServer.forwardTo(port(serverURL), new ProxyServer.ForwardOptions().setAllowConnectRequests(true));
      Page page = browser.newPage(new Browser.NewPageOptions()
        .setIgnoreHTTPSErrors(true)
        .setClientCertificates(Arrays.asList(trustedCertificate(origin(serverURL))))
        .setProxy("localhost:" + proxyServer.PORT));
      assertEquals(Collections.emptyList(), proxyServer.connectHosts);
      page.navigate(serverURL);
      String host = browserName == BrowserName.WEBKIT && Env.isMac ? "localhost" : "127.0.0.1";
      assertEquals(Arrays.asList(host + ":" + port(serverURL)), new ArrayList<>(new LinkedHashSet<>(proxyServer.connectHosts)));
      assertThat(page.getByTestId("message")).hasText("Hello Alice, your certificate was issued by localhost!");
      page.close();
    }

    @PlaywrightTest
    @DisplayName("CC-025 should respect launch proxy and proxy bypass")
    void shouldRespectLaunchProxyAndProxyBypass(BrowserType browserType, StartCCServer startCCServer, BrowserName browserName, ProxyServer proxyServer) throws Exception {
      String serverURL = startCCServer.start(new StartCCServer.Options().setUseFakeLocalhost(browserName == BrowserName.WEBKIT && Env.isMac));
      String origin = origin(serverURL);
      String hostname = URI.create(serverURL).getHost();
      int port = port(serverURL);
      proxyServer.forwardTo(port, new ProxyServer.ForwardOptions().setAllowConnectRequests(true));
      String proxyServerAddress = "localhost:" + proxyServer.PORT;
      String serverHost = (browserName == BrowserName.WEBKIT && Env.isMac ? "localhost" : "127.0.0.1") + ":" + port;
      List<ClientCertificate> clientCertificates = Arrays.asList(trustedCertificate(origin));
      Browser browser = browserType.launch(Env.launchOptions().setProxy(new Proxy(proxyServerAddress)));
      {
        Page page = browser.newPage(new Browser.NewPageOptions().setIgnoreHTTPSErrors(true).setClientCertificates(clientCertificates));
        page.navigate(serverURL);
        assertThat(page.getByTestId("message")).hasText("Hello Alice, your certificate was issued by localhost!");
        // Headed Chromium also sends its own background requests through the launch proxy.
        assertTrue(proxyServer.connectHosts.contains(serverHost));
        page.close();
      }
      proxyServer.connectHosts.clear();
      {
        Page page = browser.newPage(new Browser.NewPageOptions().setIgnoreHTTPSErrors(true).setClientCertificates(clientCertificates)
          .setProxy(new Proxy(proxyServerAddress).setBypass(hostname)));
        page.navigate(serverURL);
        assertThat(page.getByTestId("message")).hasText("Hello Alice, your certificate was issued by localhost!");
        assertFalse(proxyServer.connectHosts.contains(serverHost));
        page.close();
      }
      browser.close();
    }

    @PlaywrightTest
    @DisplayName("CC-026 should pass with matching certificates and when a socks proxy is used")
    void shouldPassWithMatchingCertificatesAndWhenASocksProxyIsUsed(Browser browser, StartCCServer startCCServer, BrowserName browserName) throws Exception {
      String serverURL = startCCServer.start(new StartCCServer.Options().setUseFakeLocalhost(browserName == BrowserName.WEBKIT && Env.isMac));
      int serverPort = port(serverURL);
      // Java: port 0 is any free port, upstream derives one from the worker index.
      SocksForwardingServer socksServer = SocksForwardingServer.create(0, serverPort, serverPort);
      Page page = browser.newPage(new Browser.NewPageOptions()
        .setIgnoreHTTPSErrors(true)
        .setClientCertificates(Arrays.asList(trustedCertificate(origin(serverURL))))
        .setProxy(socksServer.proxyServerAddr));
      assertEquals(Collections.emptyList(), socksServer.connectHosts);
      page.navigate(serverURL);
      String host = browserName == BrowserName.WEBKIT && Env.isMac ? "localhost" : "127.0.0.1";
      assertEquals(Arrays.asList(host + ":" + serverPort), socksServer.connectHosts);
      assertThat(page.getByTestId("message")).hasText("Hello Alice, your certificate was issued by localhost!");
      page.close();
      socksServer.close();
    }

    @PlaywrightTest
    @DisplayName("CC-027 should not hang on tls errors during TLS 1.2 handshake")
    @Fixme("harness: Node's SNICallback error destroys the socket silently, a Java SSLServerSocket rejects the SNI with a TLS alert, so the page shows 'Playwright client-certificate error: ...error:0A000458:SSL routines:ssl3_read_bytes:tlsv1 unrecognized name...SSL alert number 112' instead of 'Client network socket disconnected before secure TLS connection was established'")
    void shouldNotHangOnTlsErrorsDuringTLS12Handshake(Browser browser, TestInfo testInfo, BrowserName browserName) throws Exception {
      for (String tlsVersion : Arrays.asList("TLSv1.3", "TLSv1.2")) {
        // it.step('TLS version: ' + tlsVersion)
        // Java: tls.createServer(...) is an SSLServerSocket that rejects every handshake through its SNI matcher.
        SSLServerSocket server = (SSLServerSocket) serverContext(testInfo, true).getServerSocketFactory()
          .createServerSocket(0, 50, InetAddress.getByName("localhost"));
        server.setNeedClientAuth(true);
        server.setEnabledProtocols(new String[]{tlsVersion});
        SSLParameters parameters = server.getSSLParameters();
        parameters.setSNIMatchers(Collections.<SNIMatcher>singletonList(new SNIMatcher(StandardConstants.SNI_HOST_NAME) {
          @Override
          public boolean matches(SNIServerName serverName) {
            // Always reject the connection
            return false;
          }
        }));
        server.setSSLParameters(parameters);
        Thread acceptThread = new Thread(() -> {
          while (!server.isClosed()) {
            try {
              SSLSocket socket = (SSLSocket) server.accept();
              try {
                socket.startHandshake();
              } catch (IOException e) {
                // The handshake is rejected.
              } finally {
                socket.close();
              }
            } catch (IOException e) {
              // The server is closed.
            }
          }
        }, "TLS server");
        acceptThread.setDaemon(true);
        acceptThread.start();
        String host = browserName == BrowserName.WEBKIT && Env.platform().equals("darwin") ? "local.playwright" : "localhost";
        String serverURL = "https://" + host + ":" + server.getLocalPort() + "/";
        Page page = browser.newPage(new Browser.NewPageOptions()
          .setIgnoreHTTPSErrors(true)
          .setClientCertificates(Arrays.asList(selfSignedCertificate(origin(serverURL)))));
        page.navigate(serverURL);
        assertThat(page.getByText("Playwright client-certificate error: Client network socket disconnected before secure TLS connection was established")).isVisible();
        page.close();
        server.close();
      }
    }

    @PlaywrightTest
    @DisplayName("CC-028 should pass with matching certificates in pfx format")
    void shouldPassWithMatchingCertificatesInPfxFormat(Browser browser, StartCCServer startCCServer, BrowserName browserName) throws Exception {
      String serverURL = startCCServer.start(new StartCCServer.Options().setUseFakeLocalhost(browserName == BrowserName.WEBKIT && Env.isMac));
      Page page = browser.newPage(new Browser.NewPageOptions()
        .setIgnoreHTTPSErrors(true)
        .setClientCertificates(Arrays.asList(new ClientCertificate(origin(serverURL))
          .setPfxPath(asset("client-certificates/client/trusted/cert.pfx"))
          .setPassphrase("secure"))));
      page.navigate(serverURL);
      assertThat(page.getByTestId("message")).hasText("Hello Alice, your certificate was issued by localhost!");
      page.close();
    }

    @PlaywrightTest
    @DisplayName("CC-029 should handle TLS renegotiation with client certificates")
    void shouldHandleTLSRenegotiationWithClientCertificates(Browser browser, TestInfo testInfo, BrowserName browserName) throws Exception {
      // Java: the server is a RenegotiatingServer, the Node https server whose handlers call tlsSocket.renegotiate().
      RenegotiatingServer server = new RenegotiatingServer(serverContext(testInfo, false));
      int port = server.port();
      String host = browserName == BrowserName.WEBKIT && Env.platform().equals("darwin") ? "local.playwright" : "localhost";
      String serverUrl = "https://" + host + ":" + port;

      BrowserContext context = browser.newContext(new Browser.NewContextOptions()
        .setIgnoreHTTPSErrors(true)
        .setClientCertificates(Arrays.asList(trustedCertificate(serverUrl))));

      Page page = context.newPage();

      // it.step('fetch API')
      {
        page.navigate(serverUrl);
        Object response = page.evaluate("async () => {\n" +
          "  const response = await fetch('/from-fetch-api', {\n" +
          "    method: 'POST',\n" +
          "    body: 'client-request-payload'\n" +
          "  });\n" +
          "  return await response.text();\n" +
          "}");
        assertEquals(String.join("\n", Arrays.asList(
          "server received: client-request-payload",
          "0-from-server",
          "1-from-server",
          "2-from-server",
          "3-from-server",
          "server closed the connection")), response);
      }

      // it.step('Gzip encoded CSS Stylesheet')
      {
        page.navigate(serverUrl);
        // The <link> would throw with net::ERR_INVALID_CHUNKED_ENCODING
        page.setContent("\n" +
          "        <button>Click me</button>\n" +
          "        <link rel=\"stylesheet\" href=\"/style.css\">\n" +
          "      ");
        assertThat(page.locator("button")).hasCSS("background-color", /* red */"rgb(255, 0, 0)");
      }

      context.close();
      server.close();
    }

    @PlaywrightTest
    @DisplayName("CC-030 should pass with matching certificates in pfx format when passing as content")
    void shouldPassWithMatchingCertificatesInPfxFormatWhenPassingAsContent(Browser browser, StartCCServer startCCServer, BrowserName browserName) throws Exception {
      String serverURL = startCCServer.start(new StartCCServer.Options().setUseFakeLocalhost(browserName == BrowserName.WEBKIT && Env.isMac));
      Page page = browser.newPage(new Browser.NewPageOptions()
        .setIgnoreHTTPSErrors(true)
        .setClientCertificates(Arrays.asList(new ClientCertificate(origin(serverURL))
          .setPfx(Files.readAllBytes(asset("client-certificates/client/trusted/cert.pfx")))
          .setPassphrase("secure"))));
      page.navigate(serverURL);
      assertThat(page.getByTestId("message")).hasText("Hello Alice, your certificate was issued by localhost!");
      page.close();
    }

    @PlaywrightTest
    @DisplayName("CC-031 should fail with matching certificates in legacy pfx format")
    void shouldFailWithMatchingCertificatesInLegacyPfxFormat(Browser browser, StartCCServer startCCServer, BrowserName browserName) throws Exception {
      String serverURL = startCCServer.start(new StartCCServer.Options().setUseFakeLocalhost(browserName == BrowserName.WEBKIT && Env.isMac));
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> browser.newPage(new Browser.NewPageOptions()
        .setIgnoreHTTPSErrors(true)
        .setClientCertificates(Arrays.asList(new ClientCertificate(origin(serverURL))
          .setPfxPath(asset("client-certificates/client/trusted/cert-legacy.pfx"))
          .setPassphrase("secure")))));
      assertTrue(error.getMessage().contains("Unsupported TLS certificate"));
    }

    @PlaywrightTest
    @DisplayName("CC-032 should throw a http error if the pfx passphrase is incorect")
    void shouldThrowAHttpErrorIfThePfxPassphraseIsIncorect(Browser browser, StartCCServer startCCServer, BrowserName browserName) throws Exception {
      String serverURL = startCCServer.start(new StartCCServer.Options().setUseFakeLocalhost(browserName == BrowserName.WEBKIT && Env.isMac));
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> browser.newPage(new Browser.NewPageOptions()
        .setIgnoreHTTPSErrors(true)
        .setClientCertificates(Arrays.asList(new ClientCertificate(origin(serverURL))
          .setPfxPath(asset("client-certificates/client/trusted/cert.pfx"))
          .setPassphrase("this-password-is-incorrect")))));
      assertTrue(error.getMessage().contains("Failed to load client certificate: mac verify failure"));
    }

    @PlaywrightTest
    @DisplayName("CC-033 should pass with matching certificates on context APIRequestContext instance")
    void shouldPassWithMatchingCertificatesOnContextAPIRequestContextInstance(Browser browser, StartCCServer startCCServer) throws Exception {
      String serverURL = startCCServer.start();
      Page page = browser.newPage(new Browser.NewPageOptions()
        .setIgnoreHTTPSErrors(true)
        .setClientCertificates(Arrays.asList(
          trustedCertificate(origin(serverURL)),
          trustedCertificate(origin(serverURL).replace("localhost", "127.0.0.1")))));
      for (String url : Arrays.asList(serverURL, serverURL.replace("localhost", "127.0.0.1"))) {
        APIResponse response = page.request().get(url);
        assertEquals(200, response.status());
        assertTrue(response.text().contains("Hello Alice, your certificate was issued by localhost!"));
      }
      page.close();
    }

    @PlaywrightTest
    @DisplayName("CC-034 should pass with matching certificates and trailing slash")
    void shouldPassWithMatchingCertificatesAndTrailingSlash(Browser browser, StartCCServer startCCServer, BrowserName browserName) throws Exception {
      String serverURL = startCCServer.start(new StartCCServer.Options().setUseFakeLocalhost(browserName == BrowserName.WEBKIT && Env.isMac));
      Page page = browser.newPage(new Browser.NewPageOptions()
        .setIgnoreHTTPSErrors(true)
        .setClientCertificates(Arrays.asList(trustedCertificate(serverURL))));
      page.navigate(serverURL);
      assertThat(page.getByText("Hello Alice, your certificate was issued by localhost!")).isVisible();
      page.close();
    }

    @PlaywrightTest
    @DisplayName("CC-035 should have ignoreHTTPSErrors=false by default")
    void shouldHaveIgnoreHTTPSErrorsFalseByDefault(Browser browser, HttpsTestServer httpsServer, BrowserName browserName) {
      String targetURL = browserName == BrowserName.WEBKIT && Env.platform().equals("darwin") ? httpsServer.EMPTY_PAGE.replace("localhost", "local.playwright") : httpsServer.EMPTY_PAGE;
      Page page = browser.newPage(new Browser.NewPageOptions()
        .setClientCertificates(Arrays.asList(trustedCertificate(origin(targetURL)))));
      page.navigate(targetURL);
      assertThat(page.getByText("Playwright client-certificate error: self-signed certificate")).isVisible();
      page.close();
    }

    @Nested
    class PersistentContext {
      @PlaywrightTest
      @DisplayName("CC-040 validate input")
      void validateInput(LaunchPersistent launchPersistent) {
        for (ValidationSubTest subTest : kValidationSubTests) {
          PlaywrightException error = assertThrows(PlaywrightException.class,
            () -> launchPersistent.launch(Env.launchPersistentContextOptions().setClientCertificates(subTest.clientCertificates)));
          assertTrue(error.getMessage().contains(subTest.expected));
        }
      }

      @PlaywrightTest
      @DisplayName("CC-041 should pass with matching certificates")
      void shouldPassWithMatchingCertificates(LaunchPersistent launchPersistent, StartCCServer startCCServer, BrowserName browserName) throws Exception {
        String serverURL = startCCServer.start(new StartCCServer.Options().setUseFakeLocalhost(browserName == BrowserName.WEBKIT && Env.isMac));
        Page page = launchPersistent.launch(Env.launchPersistentContextOptions()
          .setIgnoreHTTPSErrors(true)
          .setClientCertificates(Arrays.asList(trustedCertificate(origin(serverURL))))).page;
        page.navigate(serverURL);
        assertThat(page.getByTestId("message")).hasText("Hello Alice, your certificate was issued by localhost!");
      }
    }
  }
}
