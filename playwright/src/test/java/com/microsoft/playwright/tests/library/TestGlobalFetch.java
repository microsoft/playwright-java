/**
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

import com.google.gson.Gson;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.HttpsTestServer;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.impl.driver.Driver;
import com.microsoft.playwright.options.HttpCredentials;
import com.microsoft.playwright.options.HttpCredentialsSend;
import com.microsoft.playwright.options.HttpHeader;
import com.microsoft.playwright.options.RequestOptions;
import com.microsoft.playwright.options.SecurityDetails;
import com.microsoft.playwright.options.ServerAddr;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Nested;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.microsoft.playwright.fixtures.Utils.asset;
import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/** Port of tests/library/global-fetch.spec.ts. */
public class TestGlobalFetch {
  // Java: stands for request[method](url) with the method name of the loops.
  private static APIResponse call(APIRequestContext request, String method, String url) {
    switch (method) {
      case "fetch":
        return request.fetch(url);
      case "delete":
        return request.delete(url);
      case "get":
        return request.get(url);
      case "head":
        return request.head(url);
      case "patch":
        return request.patch(url);
      case "post":
        return request.post(url);
      case "put":
        return request.put(url);
      default:
        throw new IllegalArgumentException(method);
    }
  }

  // Java: stands for the JSON.stringify of the value.
  private static String stringify(Object value) {
    return new Gson().toJson(value);
  }

  // Java: stands for the toEqual of a securityDetails() object, SecurityDetails has no equals().
  private static Map<String, Object> securityDetailsMap(SecurityDetails details) {
    return mapOf("issuer", details.issuer, "protocol", details.protocol, "subjectName", details.subjectName,
      "validFrom", details.validFrom, "validTo", details.validTo);
  }

  @Nested
  @ParameterizedClass
  @ValueSource(strings = {"fetch", "delete", "get", "head", "patch", "post", "put"})
  class Methods {
    @Parameter
    String method;

    @PlaywrightTest
    @DisplayName("FE-001 ${method} should work @smoke")
    void methodShouldWork(Playwright playwright, TestServer server) {
      APIRequestContext request = playwright.request().newContext();
      APIResponse response = call(request, method, server.PREFIX + "/simple.json");
      assertEquals(server.PREFIX + "/simple.json", response.url());
      assertEquals(200, response.status());
      assertEquals("OK", response.statusText());
      assertTrue(response.ok());
      assertEquals("application/json; charset=utf-8", response.headers().get("content-type"));
      boolean found = false;
      for (HttpHeader header : response.headersArray()) {
        if (header.name.equals("Content-Type") && header.value.equals("application/json; charset=utf-8")) {
          found = true;
        }
      }
      assertTrue(found);
      assertEquals("head".equals(method) ? "" : "{\"foo\": \"bar\"}\n", response.text());
      request.dispose();
    }
  }

  @PlaywrightTest
  @DisplayName("FE-002 should dispose global request")
  @Fixme("java: dispose() of an already disposed APIRequestContext throws TargetClosedError: 'Target page, context or browser has been closed'")
  void shouldDisposeGlobalRequest(Playwright playwright, TestServer server) {
    APIRequestContext request = playwright.request().newContext();
    APIResponse response = request.get(server.PREFIX + "/simple.json");
    // Java: there is no response.json().
    assertEquals(mapOf("foo", "bar"), new Gson().fromJson(response.text(), Map.class));
    request.dispose();
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> response.body());
    assertTrue(error.getMessage().contains("Response has been disposed"));
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-003 should support global userAgent option")
  void shouldSupportGlobalUserAgentOption(Playwright playwright, TestServer server) throws Exception {
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions().setUserAgent("My Agent"));
    CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/empty.html");
    APIResponse response = request.get(server.EMPTY_PAGE);
    TestServer.Request serverRequest = serverRequestPromise.get();
    assertTrue(response.ok());
    assertEquals(server.EMPTY_PAGE, response.url());
    assertEquals("My Agent", serverRequest.headers.get("user-agent"));
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-004 should support global timeout option")
  @Fixme("java: the message of a driver error is the serialized error \"Error {\\n  message='Timeout 100ms exceeded.\\n  name='TimeoutError ...\" without the 'apiRequestContext.get: ' prefix")
  void shouldSupportGlobalTimeoutOption(Playwright playwright, TestServer server) {
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions().setTimeout(100));
    server.setRoute("/empty.html", (req, res) -> {});
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> request.get(server.EMPTY_PAGE));
    assertTrue(error.getMessage().contains("apiRequestContext.get: Timeout 100ms exceeded"));
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-005 should propagate extra http headers with redirects")
  void shouldPropagateExtraHttpHeadersWithRedirects(Playwright playwright, TestServer server) throws Exception {
    server.setRedirect("/a/redirect1", "/b/c/redirect2");
    server.setRedirect("/b/c/redirect2", "/simple.json");
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions()
      .setExtraHTTPHeaders(mapOfStrings("My-Secret", "Value")));
    CompletableFuture<TestServer.Request> req1Promise = server.waitForRequest("/a/redirect1");
    CompletableFuture<TestServer.Request> req2Promise = server.waitForRequest("/b/c/redirect2");
    CompletableFuture<TestServer.Request> req3Promise = server.waitForRequest("/simple.json");
    request.get(server.PREFIX + "/a/redirect1");
    TestServer.Request req1 = req1Promise.get();
    TestServer.Request req2 = req2Promise.get();
    TestServer.Request req3 = req3Promise.get();
    assertEquals("Value", req1.headers.get("my-secret"));
    assertEquals("Value", req2.headers.get("my-secret"));
    assertEquals("Value", req3.headers.get("my-secret"));
    request.dispose();
  }

  private static Map<String, String> mapOfStrings(String... keysAndValues) {
    Map<String, String> map = new java.util.LinkedHashMap<>();
    for (int i = 0; i < keysAndValues.length; i += 2) {
      map.put(keysAndValues[i], keysAndValues[i + 1]);
    }
    return map;
  }

  @PlaywrightTest
  @DisplayName("FE-006 should preserve authorization on same-origin redirect but strip on cross-origin")
  void shouldPreserveAuthorizationOnSameOriginRedirectButStripOnCrossOrigin(Playwright playwright, TestServer server) throws Exception {
    server.setRedirect("/same/redirect", "/same/dest");
    server.setRedirect("/cross/redirect", server.CROSS_PROCESS_PREFIX + "/cross/dest");
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions()
      .setExtraHTTPHeaders(mapOfStrings("Authorization", "Bearer secret")));

    CompletableFuture<TestServer.Request> sameDestReqPromise = server.waitForRequest("/same/dest");
    request.get(server.PREFIX + "/same/redirect");
    TestServer.Request sameDestReq = sameDestReqPromise.get();
    assertEquals("Bearer secret", sameDestReq.headers.get("authorization"));

    CompletableFuture<TestServer.Request> crossDestReqPromise = server.waitForRequest("/cross/dest");
    request.get(server.PREFIX + "/cross/redirect");
    TestServer.Request crossDestReq = crossDestReqPromise.get();
    assertNull(crossDestReq.headers.get("authorization"));

    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-007 should support global httpCredentials option")
  void shouldSupportGlobalHttpCredentialsOption(Playwright playwright, TestServer server) {
    server.setAuth("/empty.html", "user", "pass");
    APIRequestContext request1 = playwright.request().newContext();
    APIResponse response1 = request1.get(server.EMPTY_PAGE);
    assertEquals(401, response1.status());
    request1.dispose();

    APIRequestContext request2 = playwright.request().newContext(new APIRequest.NewContextOptions().setHttpCredentials("user", "pass"));
    APIResponse response2 = request2.get(server.EMPTY_PAGE);
    assertEquals(200, response2.status());
    request2.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-008 should return error with wrong credentials")
  void shouldReturnErrorWithWrongCredentials(Playwright playwright, TestServer server) {
    server.setAuth("/empty.html", "user", "pass");
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions().setHttpCredentials("user", "wrong"));
    APIResponse response = request.get(server.EMPTY_PAGE);
    assertEquals(401, response.status());
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-009 should work with correct credentials and matching origin")
  void shouldWorkWithCorrectCredentialsAndMatchingOrigin(Playwright playwright, TestServer server) {
    server.setAuth("/empty.html", "user", "pass");
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions()
      .setHttpCredentials(new HttpCredentials("user", "pass").setOrigin(server.PREFIX)));
    APIResponse response = request.get(server.EMPTY_PAGE);
    assertEquals(200, response.status());
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-010 should work with correct credentials and matching origin case insensitive")
  void shouldWorkWithCorrectCredentialsAndMatchingOriginCaseInsensitive(Playwright playwright, TestServer server) {
    server.setAuth("/empty.html", "user", "pass");
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions()
      .setHttpCredentials(new HttpCredentials("user", "pass").setOrigin(server.PREFIX.toUpperCase())));
    APIResponse response = request.get(server.EMPTY_PAGE);
    assertEquals(200, response.status());
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-011 should return error with correct credentials and mismatching scheme")
  void shouldReturnErrorWithCorrectCredentialsAndMismatchingScheme(Playwright playwright, TestServer server) {
    server.setAuth("/empty.html", "user", "pass");
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions()
      .setHttpCredentials(new HttpCredentials("user", "pass").setOrigin(server.PREFIX.replace("http://", "https://"))));
    APIResponse response = request.get(server.EMPTY_PAGE);
    assertEquals(401, response.status());
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-012 should return error with correct credentials and mismatching hostname")
  void shouldReturnErrorWithCorrectCredentialsAndMismatchingHostname(Playwright playwright, TestServer server) {
    server.setAuth("/empty.html", "user", "pass");
    String hostname = URI.create(server.PREFIX).getHost();
    String origin = server.PREFIX.replace(hostname, "mismatching-hostname");
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions()
      .setHttpCredentials(new HttpCredentials("user", "pass").setOrigin(origin)));
    APIResponse response = request.get(server.EMPTY_PAGE);
    assertEquals(401, response.status());
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-013 should return error with correct credentials and mismatching port")
  void shouldReturnErrorWithCorrectCredentialsAndMismatchingPort(Playwright playwright, TestServer server) {
    server.setAuth("/empty.html", "user", "pass");
    String origin = server.PREFIX.replace(Integer.toString(server.PORT), Integer.toString(server.PORT + 1));
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions()
      .setHttpCredentials(new HttpCredentials("user", "pass").setOrigin(origin)));
    APIResponse response = request.get(server.EMPTY_PAGE);
    assertEquals(401, response.status());
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-014 should support WWW-Authenticate: Basic")
  void shouldSupportWWWAuthenticateBasic(Playwright playwright, TestServer server) {
    AtomicReference<String> credentials = new AtomicReference<>();
    server.setRoute("/empty.html", (req, res) -> {
      if (req.headers.get("authorization") == null) {
        res.writeHead(401, "WWW-Authenticate", "Basic");
        res.end("HTTP Error 401 Unauthorized: Access is denied");
        return;
      }
      String[] parts = req.headers.get("authorization").split(" ");
      credentials.set(new String(Base64.getDecoder().decode(parts.length > 1 ? parts[1] : ""), StandardCharsets.UTF_8));
      res.writeHead(200, "content-type", "text/plain");
      res.end();
    });
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions().setHttpCredentials("user", "pass"));
    APIResponse response = request.get(server.EMPTY_PAGE);
    assertEquals(200, response.status());
    assertEquals("user:pass", credentials.get());
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-015 should support HTTPCredentials.send")
  void shouldSupportHTTPCredentialsSend(Playwright playwright, TestServer server) throws Exception {
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions()
      .setHttpCredentials(new HttpCredentials("user", "pass").setOrigin(server.PREFIX.toUpperCase()).setSend(HttpCredentialsSend.ALWAYS)));
    {
      CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/empty.html");
      APIResponse response = request.get(server.EMPTY_PAGE);
      TestServer.Request serverRequest = serverRequestPromise.get();
      assertEquals("Basic " + Base64.getEncoder().encodeToString("user:pass".getBytes(StandardCharsets.UTF_8)), serverRequest.headers.get("authorization"));
      assertEquals(200, response.status());
    }
    {
      CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/empty.html");
      APIResponse response = request.get(server.CROSS_PROCESS_PREFIX + "/empty.html");
      TestServer.Request serverRequest = serverRequestPromise.get();
      // Not sent to another origin.
      assertNull(serverRequest.headers.get("authorization"));
      assertEquals(200, response.status());
    }
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-016 should support multiple httpCredentials")
  void shouldSupportMultipleHttpCredentials(Playwright playwright, TestServer server) {
    server.setAuth("/empty.html", "user1", "pass1");
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions()
      .setHttpCredentials(Arrays.asList(
        new HttpCredentials("user1", "pass1").setOrigin(server.PREFIX),
        new HttpCredentials("user2", "pass2").setOrigin(server.CROSS_PROCESS_PREFIX))));
    APIResponse response1 = request.get(server.EMPTY_PAGE);
    assertEquals(200, response1.status());
    // Wrong credentials are picked for the other origin.
    APIResponse response2 = request.get(server.CROSS_PROCESS_PREFIX + "/empty.html");
    assertEquals(401, response2.status());
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-017 should support HTTPCredentials.send with multiple httpCredentials")
  void shouldSupportHTTPCredentialsSendWithMultipleHttpCredentials(Playwright playwright, TestServer server) throws Exception {
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions()
      .setHttpCredentials(Arrays.asList(
        new HttpCredentials("user1", "pass1").setOrigin(server.PREFIX).setSend(HttpCredentialsSend.ALWAYS),
        new HttpCredentials("user2", "pass2").setOrigin(server.CROSS_PROCESS_PREFIX).setSend(HttpCredentialsSend.UNAUTHORIZED))));
    {
      CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/empty.html");
      APIResponse response = request.get(server.EMPTY_PAGE);
      TestServer.Request serverRequest = serverRequestPromise.get();
      assertEquals("Basic " + Base64.getEncoder().encodeToString("user1:pass1".getBytes(StandardCharsets.UTF_8)), serverRequest.headers.get("authorization"));
      assertEquals(200, response.status());
    }
    {
      CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/empty.html");
      APIResponse response = request.get(server.CROSS_PROCESS_PREFIX + "/empty.html");
      TestServer.Request serverRequest = serverRequestPromise.get();
      // This origin has send: 'unauthorized', so credentials are not sent proactively.
      assertNull(serverRequest.headers.get("authorization"));
      assertEquals(200, response.status());
    }
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-018 should support global ignoreHTTPSErrors option")
  void shouldSupportGlobalIgnoreHTTPSErrorsOption(Playwright playwright, HttpsTestServer httpsServer) {
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions().setIgnoreHTTPSErrors(true));
    APIResponse response = request.get(httpsServer.EMPTY_PAGE);
    assertEquals(200, response.status());
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-019 should propagate ignoreHTTPSErrors on redirects")
  void shouldPropagateIgnoreHTTPSErrorsOnRedirects(Playwright playwright, HttpsTestServer httpsServer) {
    httpsServer.setRedirect("/redir", "/empty.html");
    APIRequestContext request = playwright.request().newContext();
    APIResponse response = request.get(httpsServer.PREFIX + "/redir", RequestOptions.create().setIgnoreHTTPSErrors(true));
    assertEquals(200, response.status());
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-020 should return server address from response")
  void shouldReturnServerAddressFromResponse(Playwright playwright, TestServer server) {
    APIRequestContext request = playwright.request().newContext();
    // The second request reuses the keep-alive socket and should report the address as well.
    for (int i = 0; i < 2; i++) {
      APIResponse response = request.get(server.EMPTY_PAGE);
      ServerAddr addr = response.serverAddr();
      assertTrue(Pattern.compile("^(127\\.0\\.0\\.1|::1)$").matcher(addr.ipAddress).find());
      assertEquals(server.PORT, addr.port);
    }
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-021 should return security details from response")
  void shouldReturnSecurityDetailsFromResponse(Playwright playwright, HttpsTestServer httpsServer) {
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions().setIgnoreHTTPSErrors(true));
    // The second request reuses the keep-alive socket and should report the details as well.
    for (int i = 0; i < 2; i++) {
      APIResponse response = request.get(httpsServer.EMPTY_PAGE);
      assertEquals(mapOf("issuer", "playwright-test", "protocol", "TLSv1.3", "subjectName", "playwright-test", "validFrom", 1691708270.0, "validTo", 2007068270.0),
        securityDetailsMap(response.securityDetails()));
    }
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-022 should return security details for a resumed TLS session")
  void shouldReturnSecurityDetailsForAResumedTLSSession(Playwright playwright, HttpsTestServer httpsServer) {
    Map<String, Object> expected = mapOf("issuer", "playwright-test", "protocol", "TLSv1.3", "subjectName", "playwright-test", "validFrom", 1691708270.0, "validTo", 2007068270.0);
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions().setIgnoreHTTPSErrors(true));
    // 'Connection: close' drops the socket, so the next request opens a new one and resumes
    // the cached TLS session. A resumed session does not carry the server certificate.
    APIResponse first = request.get(httpsServer.EMPTY_PAGE, RequestOptions.create().setHeader("connection", "close"));
    assertEquals(expected, securityDetailsMap(first.securityDetails()));
    APIResponse second = request.get(httpsServer.EMPTY_PAGE);
    assertEquals(expected, securityDetailsMap(second.securityDetails()));
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-023 should return null security details for http response")
  void shouldReturnNullSecurityDetailsForHttpResponse(Playwright playwright, TestServer server) {
    APIRequestContext request = playwright.request().newContext();
    APIResponse response = request.get(server.EMPTY_PAGE);
    assertNull(response.securityDetails());
    request.dispose();
  }

  // Java: an SSL context of a PEM key and certificate, for the https server of utils.createHttpsServer({ key, cert }).
  private static SSLContext sslContext(Path keyPem, Path certPem) throws Exception {
    String keyText = new String(Files.readAllBytes(keyPem), StandardCharsets.UTF_8)
      .replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "").replaceAll("\\s", "");
    PrivateKey key = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(keyText)));
    Certificate cert;
    try (java.io.InputStream in = Files.newInputStream(certPem)) {
      cert = CertificateFactory.getInstance("X.509").generateCertificate(in);
    }
    KeyStore keyStore = KeyStore.getInstance("PKCS12");
    keyStore.load(null, null);
    keyStore.setKeyEntry("key", key, "aaaa".toCharArray(), new Certificate[]{cert});
    KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    keyManagerFactory.init(keyStore, "aaaa".toCharArray());
    SSLContext context = SSLContext.getInstance("TLS");
    context.init(keyManagerFactory.getKeyManagers(), new TrustManager[]{new X509TrustManager() {
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
    }}, null);
    return context;
  }

  @PlaywrightTest
  @DisplayName("FE-024 should return security details for certificate with multiple CN attributes")
  void shouldReturnSecurityDetailsForCertificateWithMultipleCNAttributes(Playwright playwright) throws Exception {
    // Java: the https server is a HttpsTestServer on the PEM key and certificate.
    HttpsTestServer server = HttpsTestServer.create(Env.srcTestDir().resolve("assets"), 0,
      sslContext(asset("multi-value-rdn/key.pem"), asset("multi-value-rdn/cert.pem")), false);
    server.setRoute("/", (req, res) -> {
      res.writeHead(200, "Content-Type", "application/json");
      res.end("{\"ok\":true}");
    });
    try {
      APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions().setIgnoreHTTPSErrors(true));
      APIResponse response = request.get("https://localhost:" + server.PORT + "/");
      assertEquals(200, response.status());
      SecurityDetails securityDetails = response.securityDetails();
      assertEquals("localhost", securityDetails.subjectName);
      assertEquals("localhost", securityDetails.issuer);
      request.dispose();
    } finally {
      server.close();
    }
  }

  @PlaywrightTest
  @DisplayName("FE-025 should resolve url relative to global baseURL option")
  void shouldResolveUrlRelativeToGlobalBaseURLOption(Playwright playwright, TestServer server) {
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions().setBaseURL(server.PREFIX));
    APIResponse response = request.get("/empty.html");
    assertEquals(server.EMPTY_PAGE, response.url());
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-026 should fallback to given URL if baseURL is bogus")
  void shouldFallbackToGivenURLIfBaseURLIsBogus(Playwright playwright, TestServer server) {
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions().setBaseURL("bogus"));
    APIResponse response = request.get(server.PREFIX + "/empty.html");
    assertEquals(server.EMPTY_PAGE, response.url());
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-027 should set playwright as user-agent")
  @Fixme("java: the user agent names the Java client, 'Playwright/X.X.X (<ARCH>; distro version) java/X' instead of '... node/X.X'")
  void shouldSetPlaywrightAsUserAgent(Playwright playwright, TestServer server) throws Exception {
    APIRequestContext request = playwright.request().newContext();
    CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/empty.html");
    request.get(server.EMPTY_PAGE);
    TestServer.Request serverRequest = serverRequestPromise.get();
    // Java: os.arch() of Node.js and getPlaywrightVersion(), the version of the driver package.
    String javaArch = System.getProperty("os.arch");
    String arch = javaArch.equals("amd64") ? "x64" : javaArch.equals("aarch64") ? "arm64" : javaArch;
    Path packageJson = Driver.ensureDriverInstalled(java.util.Collections.<String, String>emptyMap(), false).driverDir().resolve("package").resolve("package.json");
    String version = (String) new Gson().fromJson(new String(Files.readAllBytes(packageJson), StandardCharsets.UTF_8), Map.class).get("version");
    String userAgentMasked = serverRequest.headers.get("user-agent")
      .replace(arch, "<ARCH>")
      .replace(version, "X.X.X")
      .replaceAll("\\d+", "X");

    List<String> tokens = new ArrayList<>();
    if (System.getenv("CI") != null && !System.getenv("CI").isEmpty()) {
      tokens.add("CI/X");
    }
    String suffix = tokens.isEmpty() ? "" : " " + String.join(" ", tokens);

    if (Env.isWindows) {
      assertEquals("Playwright/X.X.X (<ARCH>; windows X.X) node/X.X" + suffix, userAgentMasked);
    } else if (Env.isLinux) {
      // on ubuntu: distro is 'ubuntu' and version is 'X.X'
      // on manjaro: distro is 'Manjaro' and version is 'unknown'
      assertEquals("Playwright/X.X.X (<ARCH>; distro version) node/X.X" + suffix, userAgentMasked.replaceFirst("<ARCH>; \\w+ [^)]+", "<ARCH>; distro version"));
    } else if (Env.isMac) {
      assertEquals("Playwright/X.X.X (<ARCH>; macOS X.X) node/X.X" + suffix, userAgentMasked);
    }
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-029 should return empty body")
  void shouldReturnEmptyBody(Playwright playwright, TestServer server) {
    APIRequestContext request = playwright.request().newContext();
    APIResponse response = request.get(server.EMPTY_PAGE);
    byte[] body = response.body();
    assertEquals(0, body.length);
    assertEquals("", response.text());
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-032 should remove content-length from redirected post requests")
  void shouldRemoveContentLengthFromRedirectedPostRequests(Playwright playwright, TestServer server) throws Exception {
    server.setRedirect("/redirect", "/empty.html");
    APIRequestContext request = playwright.request().newContext();
    CompletableFuture<TestServer.Request> req1Promise = server.waitForRequest("/redirect");
    CompletableFuture<TestServer.Request> req2Promise = server.waitForRequest("/empty.html");
    APIResponse result = request.post(server.PREFIX + "/redirect", RequestOptions.create().setData(mapOf("foo", "bar")));
    TestServer.Request req1 = req1Promise.get();
    TestServer.Request req2 = req2Promise.get();
    assertEquals(200, result.status());
    assertEquals("13", req1.headers.get("content-length"));
    assertNull(req2.headers.get("content-length"));
    request.dispose();
  }

  static Stream<org.junit.jupiter.params.provider.Arguments> serialization() {
    return Stream.of(
      arguments("object", mapOf("foo", "bar")),
      arguments("array", Arrays.asList("foo", "bar", 2021)),
      arguments("string", "foo"),
      arguments("string (falsey)", ""),
      arguments("bool", true),
      arguments("bool (false)", false),
      arguments("number", 2021),
      arguments("number (falsey)", 0),
      arguments("null", null),
      arguments("literal string undefined", "undefined"));
  }

  @Nested
  @ParameterizedClass
  @MethodSource("com.microsoft.playwright.tests.library.TestGlobalFetch#serialization")
  class Serialization {
    @Parameter(0)
    String type;
    @Parameter(1)
    Object value;

    @PlaywrightTest
    @Fixme(value = "java: a String data with a JSON content type is sent as is instead of JSON-stringified, and null data sends no body",
      parameters = {"type=string,", "type=string (falsey)", "type=null,", "type=literal string undefined"})
    @DisplayName("FE-033 should json stringify ${type} body when content-type is application/json")
    void shouldJsonStringifyBodyWhenContentTypeIsApplicationJson(Playwright playwright, TestServer server) throws Exception {
      String stringifiedValue = stringify(value);
      APIRequestContext request = playwright.request().newContext();
      for (String contentType : Arrays.asList("application/json", "Application/JSON; charset=utf-8")) {
        CompletableFuture<TestServer.Request> reqPromise = server.waitForRequest("/empty.html");
        request.post(server.EMPTY_PAGE, RequestOptions.create()
          .setHeader("content-type", contentType)
          .setData(value));
        TestServer.Request req = reqPromise.get();
        byte[] body = req.postBody();
        assertEquals(stringifiedValue, new String(body, StandardCharsets.UTF_8));
      }
      request.dispose();
    }

    @PlaywrightTest
    @DisplayName("FE-034 should not double stringify ${type} body when content-type is application/json")
    void shouldNotDoubleStringifyBodyWhenContentTypeIsApplicationJson(Playwright playwright, TestServer server) throws Exception {
      String stringifiedValue = stringify(value);
      APIRequestContext request = playwright.request().newContext();
      CompletableFuture<TestServer.Request> reqPromise = server.waitForRequest("/empty.html");
      request.post(server.EMPTY_PAGE, RequestOptions.create()
        .setHeader("content-type", "application/json")
        .setData(stringifiedValue));
      TestServer.Request req = reqPromise.get();
      byte[] body = req.postBody();
      assertEquals(stringifiedValue, new String(body, StandardCharsets.UTF_8));
      request.dispose();
    }
  }

  @PlaywrightTest
  @DisplayName("FE-035 should accept already serialized data as Buffer when content-type is application/json")
  void shouldAcceptAlreadySerializedDataAsBufferWhenContentTypeIsApplicationJson(Playwright playwright, TestServer server) throws Exception {
    APIRequestContext request = playwright.request().newContext();
    String value = stringify(stringify(mapOf("foo", "bar")));
    CompletableFuture<TestServer.Request> reqPromise = server.waitForRequest("/empty.html");
    request.post(server.EMPTY_PAGE, RequestOptions.create()
      .setHeader("content-type", "application/json")
      .setData(value.getBytes(StandardCharsets.UTF_8)));
    TestServer.Request req = reqPromise.get();
    byte[] body = req.postBody();
    assertEquals(value, new String(body, StandardCharsets.UTF_8));
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-037 should not fail on empty body with encoding")
  void shouldNotFailOnEmptyBodyWithEncoding(Playwright playwright, TestServer server) {
    APIRequestContext request = playwright.request().newContext();
    for (String method : Arrays.asList("head", "put")) {
      for (String encoding : Arrays.asList("br", "gzip", "deflate")) {
        server.setRoute("/empty.html", (req, res) -> {
          res.writeHead(200, "Content-Encoding", encoding, "Content-Type", "text/plain");
          res.end();
        });
        APIResponse response = call(request, method, server.EMPTY_PAGE);
        assertEquals(200, response.status());
        assertEquals(0, response.body().length);
      }
    }
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-038 should return body for failing requests")
  void shouldReturnBodyForFailingRequests(Playwright playwright, TestServer server) {
    APIRequestContext request = playwright.request().newContext();
    for (String method : Arrays.asList("head", "put", "trace")) {
      server.setRoute("/empty.html", (req, res) -> {
        res.writeHead(404, "Content-Length", "10", "Content-Type", "text/plain");
        res.end("Not found.");
      });
      APIResponse response = request.fetch(server.EMPTY_PAGE, RequestOptions.create().setMethod(method));
      assertEquals(404, response.status());
      // HEAD response returns empty body in node http module.
      assertEquals(method.equals("head") ? "" : "Not found.", response.text());
    }
    request.dispose();
  }

  private static final List<String> HTTP_METHODS = Arrays.asList("GET", "PUT", "POST", "OPTIONS", "HEAD", "PATCH");

  @PlaywrightTest
  @DisplayName("FE-039 should throw an error when maxRedirects is exceeded")
  void shouldThrowAnErrorWhenMaxRedirectsIsExceeded(Playwright playwright, TestServer server) {
    server.setRedirect("/a/redirect1", "/b/c/redirect2");
    server.setRedirect("/b/c/redirect2", "/b/c/redirect3");
    server.setRedirect("/b/c/redirect3", "/b/c/redirect4");
    server.setRedirect("/b/c/redirect4", "/simple.json");

    APIRequestContext request = playwright.request().newContext();
    for (String method : HTTP_METHODS) {
      for (int maxRedirects : new int[]{1, 2, 3}) {
        PlaywrightException error = assertThrows(PlaywrightException.class,
          () -> request.fetch(server.PREFIX + "/a/redirect1", RequestOptions.create().setMethod(method).setMaxRedirects(maxRedirects)));
        assertTrue(error.getMessage().contains("Max redirect count exceeded"));
      }
    }
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-040 should not follow redirects when maxRedirects is set to 0")
  void shouldNotFollowRedirectsWhenMaxRedirectsIsSetTo0(Playwright playwright, TestServer server) {
    server.setRedirect("/a/redirect1", "/b/c/redirect2");
    server.setRedirect("/b/c/redirect2", "/simple.json");

    APIRequestContext request = playwright.request().newContext();
    for (String method : HTTP_METHODS) {
      APIResponse response = request.fetch(server.PREFIX + "/a/redirect1", RequestOptions.create().setMethod(method).setMaxRedirects(0));
      assertEquals("/b/c/redirect2", response.headers().get("location"));
      assertEquals(302, response.status());
    }
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-041 should throw an error when maxRedirects is less than 0")
  @Fixme("java: the Java client validates maxRedirects itself and says \"'maxRedirects' should be greater than or equal to '0'\" instead of 'must be'")
  void shouldThrowAnErrorWhenMaxRedirectsIsLessThan0(Playwright playwright, TestServer server) {
    server.setRedirect("/a/redirect1", "/b/c/redirect2");
    server.setRedirect("/b/c/redirect2", "/simple.json");

    APIRequestContext request = playwright.request().newContext();
    for (String method : HTTP_METHODS) {
      PlaywrightException error = assertThrows(PlaywrightException.class,
        () -> request.fetch(server.PREFIX + "/a/redirect1", RequestOptions.create().setMethod(method).setMaxRedirects(-1)));
      assertTrue(error.getMessage().contains("'maxRedirects' must be greater than or equal to '0'"));
    }
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-042 should not follow redirects when maxRedirects is set to 0 in newContext")
  void shouldNotFollowRedirectsWhenMaxRedirectsIsSetTo0InNewContext(Playwright playwright, TestServer server) {
    server.setRedirect("/a/redirect1", "/b/c/redirect2");
    server.setRedirect("/b/c/redirect2", "/simple.json");

    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions().setMaxRedirects(0));
    for (String method : HTTP_METHODS) {
      APIResponse response = request.fetch(server.PREFIX + "/a/redirect1", RequestOptions.create().setMethod(method));
      assertEquals("/b/c/redirect2", response.headers().get("location"));
      assertEquals(302, response.status());
    }
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-043 should follow redirects up to maxRedirects limit set in newContext")
  void shouldFollowRedirectsUpToMaxRedirectsLimitSetInNewContext(Playwright playwright, TestServer server) {
    server.setRedirect("/a/redirect1", "/b/c/redirect2");
    server.setRedirect("/b/c/redirect2", "/b/c/redirect3");
    server.setRedirect("/b/c/redirect3", "/b/c/redirect4");
    server.setRedirect("/b/c/redirect4", "/simple.json");

    for (int maxRedirects : new int[]{1, 2, 3, 4}) {
      APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions().setMaxRedirects(maxRedirects));
      for (String method : HTTP_METHODS) {
        if (maxRedirects < 4) {
          PlaywrightException error = assertThrows(PlaywrightException.class,
            () -> request.fetch(server.PREFIX + "/a/redirect1", RequestOptions.create().setMethod(method)));
          assertTrue(error.getMessage().contains("Max redirect count exceeded"));
        } else {
          APIResponse response = request.fetch(server.PREFIX + "/a/redirect1", RequestOptions.create().setMethod(method));
          assertEquals(200, response.status());
        }
      }
      request.dispose();
    }
  }

  @PlaywrightTest
  @DisplayName("FE-044 should use maxRedirects from fetch when provided, overriding newContext")
  void shouldUseMaxRedirectsFromFetchWhenProvidedOverridingNewContext(Playwright playwright, TestServer server) {
    server.setRedirect("/a/redirect1", "/b/c/redirect2");
    server.setRedirect("/b/c/redirect2", "/b/c/redirect3");
    server.setRedirect("/b/c/redirect3", "/b/c/redirect4");
    server.setRedirect("/b/c/redirect4", "/simple.json");

    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions().setMaxRedirects(1));
    for (String method : HTTP_METHODS) {
      APIResponse response = request.fetch(server.PREFIX + "/a/redirect1", RequestOptions.create().setMethod(method).setMaxRedirects(4));
      assertEquals(200, response.status());
    }
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-045 should keep headers capitalization")
  void shouldKeepHeadersCapitalization(Playwright playwright, TestServer server) throws Exception {
    APIRequestContext request = playwright.request().newContext();
    CompletableFuture<TestServer.Request> serverRequestPromise = server.waitForRequest("/empty.html");
    APIResponse response = request.get(server.EMPTY_PAGE, RequestOptions.create().setHeader("X-fOo", "vaLUE"));
    TestServer.Request serverRequest = serverRequestPromise.get();
    assertTrue(response.ok());
    assertTrue(serverRequest.rawHeaders.contains("X-fOo"));
    assertTrue(serverRequest.rawHeaders.contains("vaLUE"));
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-047 should throw after dispose")
  void shouldThrowAfterDispose(Playwright playwright, TestServer server) {
    APIRequestContext request = playwright.request().newContext();
    request.dispose();
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> request.get(server.EMPTY_PAGE));
    assertTrue(error.getMessage().contains("Target page, context or browser has been closed"));
  }

  @PlaywrightTest
  @DisplayName("FE-048 should retry ECONNRESET")
  void shouldRetryECONNRESET(Playwright playwright, TestServer server) {
    APIRequestContext request = playwright.request().newContext();
    AtomicInteger requestCount = new AtomicInteger(0);
    server.setRoute("/test", (req, res) -> {
      if (requestCount.getAndIncrement() < 3) {
        req.socket().destroy();
        return;
      }
      res.writeHead(200, "content-type", "text/plain");
      res.end("Hello!");
    });
    APIResponse response = request.fetch(server.PREFIX + "/test", RequestOptions.create().setMaxRetries(3));
    assertEquals(200, response.status());
    assertEquals("Hello!", response.text());
    assertEquals(4, requestCount.get());
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-052 should throw when failOnStatusCode is set to true inside APIRequest context options")
  void shouldThrowWhenFailOnStatusCodeIsSetToTrueInsideAPIRequestContextOptions(Playwright playwright, TestServer server) {
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions().setFailOnStatusCode(true));
    server.setRoute("/empty.html", (req, res) -> {
      res.writeHead(404, "Content-Length", "10", "Content-Type", "text/plain");
      res.end("Not found.");
    });
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> request.fetch(server.EMPTY_PAGE));
    assertTrue(error.getMessage().contains("404 Not Found"));
    request.dispose();
  }

  @PlaywrightTest
  @DisplayName("FE-053 should not throw when failOnStatusCode is set to false inside APIRequest context options")
  void shouldNotThrowWhenFailOnStatusCodeIsSetToFalseInsideAPIRequestContextOptions(Playwright playwright, TestServer server) {
    APIRequestContext request = playwright.request().newContext(new APIRequest.NewContextOptions().setFailOnStatusCode(false));
    server.setRoute("/empty.html", (req, res) -> {
      res.writeHead(404, "Content-Length", "10", "Content-Type", "text/plain");
      res.end("Not found.");
    });
    APIResponse response = request.fetch(server.EMPTY_PAGE);
    assertEquals(404, response.status());
    request.dispose();
  }
}
