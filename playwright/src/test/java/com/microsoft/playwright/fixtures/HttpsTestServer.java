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

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

/**
 * The {@code httpsServer} fixture: a {@link TestServer} behind TLS with the self-signed
 * certificate of {@code tests/config/testserver} upstream (CN playwright-test, SAN localhost,
 * 127.0.0.1 and ::1), which browsers reject unless {@code ignoreHTTPSErrors} is set.
 */
public final class HttpsTestServer extends TestServer {
  private static final String PASSPHRASE = "aaaa";

  HttpsTestServer(Path dirPath, int port, SSLContext context, boolean wantClientAuth) throws IOException {
    super(dirPath, createServerSocket(context, port, wantClientAuth));
  }

  public static HttpsTestServer create(Path dirPath, int port, SSLContext context, boolean wantClientAuth) throws IOException {
    return new HttpsTestServer(dirPath, port, context, wantClientAuth);
  }

  private static SSLServerSocket createServerSocket(SSLContext context, int port, boolean wantClientAuth) throws IOException {
    SSLServerSocket socket = (SSLServerSocket) context.getServerSocketFactory().createServerSocket(port);
    socket.setWantClientAuth(wantClientAuth);
    return socket;
  }

  /** The upstream test certificate and key as an SSL context. */
  public static SSLContext defaultContext() throws IOException {
    return context(Env.srcTestDir().resolve("config/testserver/testserver.p12"), PASSPHRASE);
  }

  /** An SSL context serving the key and certificate of a PKCS12 file, trusting every client certificate. */
  public static SSLContext context(Path pkcs12, String passphrase) throws IOException {
    try {
      KeyStore keyStore = KeyStore.getInstance("PKCS12");
      try (InputStream in = Files.newInputStream(pkcs12)) {
        keyStore.load(in, passphrase.toCharArray());
      }
      KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      keyManagerFactory.init(keyStore, passphrase.toCharArray());
      SSLContext context = SSLContext.getInstance("TLS");
      context.init(keyManagerFactory.getKeyManagers(), new TrustManager[]{new TrustEveryone()}, null);
      return context;
    } catch (IOException e) {
      throw e;
    } catch (Exception e) {
      throw new IOException("Failed to load " + pkcs12, e);
    }
  }

  /** Accepts any client certificate; the tests inspect it through the SSL session. */
  private static final class TrustEveryone implements X509TrustManager {
    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) {
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return new X509Certificate[0];
    }
  }
}
