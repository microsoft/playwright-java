/*
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

import com.sun.net.httpserver.*;

import javax.net.ssl.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.*;
import java.util.*;

public class ServerWithClientCertificate implements HttpHandler {
  private final HttpServer server;
  final String origin;
  final String crossOrigin;
  final String url;
  KeyStore keyStore;

  static ServerWithClientCertificate create(int port) throws IOException {
    return new ServerWithClientCertificate(port);
  }

  private ServerWithClientCertificate(int port) throws IOException {
    origin = "https://localhost:" + port;
    crossOrigin = "https://127.0.0.1:" + port;;
    url = origin + "/index.html";
    HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress("localhost", port), 0);
    httpsServer.setHttpsConfigurator(new HttpsConfiguratorImpl(loadCertificates()));
    server = httpsServer;
    server.createContext("/", this);
    server.setExecutor(null); // creates a default executor
    server.start();
  }

  public void stop() {
    server.stop(0);
  }

  private SSLContext loadCertificates() {
    try {
      // Create an SSL context
      SSLContext sslContext = SSLContext.getInstance("TLS");

      // Load the keystore from file
      char[] password = "".toCharArray(); // the password you set during the PKCS12 export
      keyStore = KeyStore.getInstance("PKCS12");
      InputStream fis = HttpsConfiguratorImpl.class.getClassLoader().getResourceAsStream(
        "resources/client-certificates/server/server_keystore.p12");
      keyStore.load(fis, password);

      // Set up the KeyManagerFactory to use the keystore
      KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
      kmf.init(keyStore, password);

      TrustManager[] trustAllCerts = new TrustManager[]{
        new X509TrustManager() {
          public X509Certificate[] getAcceptedIssuers() {
            List<X509Certificate> certs = new ArrayList<>();
            try {
              for (String alias : Collections.list(keyStore.aliases())) {
                certs.add((X509Certificate) keyStore.getCertificate(alias));
              }
            } catch (KeyStoreException e) {
              throw new RuntimeException(e);
            }
            return certs.toArray(new X509Certificate[0]);
          }

          public void checkClientTrusted(X509Certificate[] clientCerts, String authType) throws CertificateException {
          }

          public void checkServerTrusted(X509Certificate[] certs, String authType) {
          }
        }
      };

      // Initialize the SSL context
      sslContext.init(kmf.getKeyManagers(), trustAllCerts, null);
      return sslContext;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private boolean validateCertChain(Certificate[] clientCerts) {
    try {
      // Create CertPath from the provided client certificates
      CertificateFactory factory = CertificateFactory.getInstance("X.509");
      CertPath certPath = factory.generateCertPath(Arrays.asList(clientCerts));

      // Extract Trust Anchors from the trust store
      Set<TrustAnchor> trustAnchors = new HashSet<>();
      for (String alias : Collections.list(keyStore.aliases())) {
        X509Certificate trustedCert = (X509Certificate) keyStore.getCertificate(alias);
        if (trustedCert != null) {
          trustAnchors.add(new TrustAnchor(trustedCert, null));
        }
      }

      // Initialize PKIX parameters
      PKIXParameters params = new PKIXParameters(trustAnchors);
      params.setRevocationEnabled(false); // Set to true if you want to enable CRL checking

      // Validate the certification path
      CertPathValidator certPathValidator = CertPathValidator.getInstance(CertPathValidator.getDefaultType());
      certPathValidator.validate(certPath, params);

      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private static String div(String testId, String message) {
    return "<div data-testid='" + testId + "'>" + message + "</div>";
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    SSLSession sslSession = ((HttpsExchange) exchange).getSSLSession();
    String response = div("servername", sslSession.getPeerHost());
    Certificate[] certs = sslSession.getPeerCertificates();
    if (certs.length > 0 && certs[0] instanceof X509Certificate) {
      X509Certificate cert = (X509Certificate) certs[0];
      exchange.getResponseHeaders().add("Content-Type", "text/html");
      if (validateCertChain(certs)) {
        exchange.sendResponseHeaders(200, 0);
        response += div("message", String.format("Hello %s, your certificate was issued by %s!",
            cert.getSubjectX500Principal().getName(), cert.getIssuerX500Principal().getName()));
      } else {
        response += div("message", String.format("Sorry %s, certificates from %s are not welcome here.",
          cert.getSubjectX500Principal().getName(), cert.getIssuerX500Principal().getName()));
        exchange.sendResponseHeaders(403, 0);
      }
    } else {
      response += div("message", "Sorry, but you need to provide a client certificate to continue.");
      exchange.sendResponseHeaders(401, 0);
    }
    try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody())) {
      writer.write(response);
    }
  }
}
