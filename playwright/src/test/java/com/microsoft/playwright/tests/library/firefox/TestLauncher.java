/**
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

package com.microsoft.playwright.tests.library.firefox;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.EnabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.HttpsTestServer;
import com.microsoft.playwright.fixtures.LaunchPersistent;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestInfo;
import org.junit.jupiter.api.DisplayName;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.asset;
import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/library/firefox/launcher.spec.ts. */
@EnabledOnBrowser(value = BrowserName.FIREFOX, reason = "tests/library/firefox runs in the firefox project only.")
public class TestLauncher {
  @PlaywrightTest
  @DisplayName("FF-001 should pass firefox user preferences")
  void shouldPassFirefoxUserPreferences(BrowserType browserType) {
    Browser browser = browserType.launch(Env.launchOptions().setFirefoxUserPrefs(mapOf(
      "network.proxy.type", 1,
      "network.proxy.ssl", "127.0.0.1",
      "network.proxy.ssl_port", 3333)));
    Page page = browser.newPage();
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.navigate("https://example.com"));
    assertTrue(error.getMessage().contains("NS_ERROR_PROXY_CONNECTION_REFUSED"));
    browser.close();
  }

  @PlaywrightTest
  @DisplayName("FF-002 should pass firefox user preferences in persistent")
  void shouldPassFirefoxUserPreferencesInPersistent(LaunchPersistent launchPersistent) {
    Page page = launchPersistent.launch(Env.launchPersistentContextOptions().setFirefoxUserPrefs(mapOf(
      "network.proxy.type", 1,
      "network.proxy.ssl", "127.0.0.1",
      "network.proxy.ssl_port", 3333))).page;
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.navigate("https://example.com"));
    assertTrue(error.getMessage().contains("NS_ERROR_PROXY_CONNECTION_REFUSED"));
  }

  @PlaywrightTest
  @DisplayName("FF-003 should support custom firefox policies")
  void shouldSupportCustomFirefoxPolicies(BrowserType browserType, TestInfo testInfo) throws Exception {
    // JSON.stringify(policies)
    String policies = "{\"policies\":{\"Certificates\":{\"Install\":[\"" +
      asset("client-certificates/server/server_cert.pem").toString().replace("\\", "\\\\") + "\"]}}}";
    Path policiesPath = testInfo.outputPath("policies.json");
    Files.write(policiesPath, policies.getBytes(StandardCharsets.UTF_8));

    int port = 13122;
    // Java: the PEM key and certificate files become the SSL context of the https server.
    HttpsTestServer server = HttpsTestServer.create(asset(""), port, sslContext(
      asset("client-certificates/client/localhost/localhost.key"),
      asset("client-certificates/client/localhost/localhost.pem")), false);

    Browser browser = browserType.launch(Env.launchOptions().setEnv(
      inheritAndCleanEnv(mapOf("PLAYWRIGHT_FIREFOX_POLICIES_JSON", policiesPath.toString()))));

    Page page = browser.newPage();
    page.navigate(server.PREFIX + "/frames/frame.html");
    assertThat(page.locator("body")).hasText("Hi, I'm frame");
    browser.close();
    server.stop();
  }

  // FF-004 should resize viewport with fractional device scale factor: not ported, see upstream_not_ported.txt.

  /** The {@code inheritAndCleanEnv} helper of tests/config/utils.ts. */
  private static Map<String, String> inheritAndCleanEnv(Map<String, Object> env) {
    Map<String, String> result = new HashMap<>(System.getenv());
    for (String name : Arrays.asList("CI", "BUILD_URL", "CI_COMMIT_SHA", "CI_JOB_URL", "CI_PROJECT_URL",
      "GITHUB_ACTIONS", "GITHUB_REPOSITORY", "GITHUB_RUN_ID", "GITHUB_SERVER_URL", "GITHUB_SHA", "GITHUB_EVENT_PATH",
      "PW_TEST_HTML_REPORT_OPEN", "PLAYWRIGHT_HTML_OPEN", "PW_TEST_DEBUG_REPORTERS", "PW_TEST_REPORTER",
      "PW_TEST_REPORTER_WS_ENDPOINT", "PW_TEST_SOURCE_TRANSFORM", "PW_TEST_SOURCE_TRANSFORM_SCOPE",
      "PWTEST_BOT_NAME", "PWTEST_SHARD_WEIGHTS", "TEST_WORKER_INDEX", "TEST_PARALLEL_INDEX", "NODE_OPTIONS")) {
      result.remove(name);
    }
    for (Map.Entry<String, Object> entry : env.entrySet()) {
      result.put(entry.getKey(), (String) entry.getValue());
    }
    return result;
  }

  private static SSLContext sslContext(Path keyFile, Path certFile) throws Exception {
    String keyPem = new String(Files.readAllBytes(keyFile), StandardCharsets.UTF_8)
      .replaceAll("-----[A-Z ]+-----", "").replaceAll("\\s", "");
    PrivateKey key = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(keyPem)));
    List<Certificate> chain = new ArrayList<>(CertificateFactory.getInstance("X.509")
      .generateCertificates(new ByteArrayInputStream(Files.readAllBytes(certFile))));
    KeyStore keyStore = KeyStore.getInstance("PKCS12");
    keyStore.load(null, null);
    keyStore.setKeyEntry("localhost", key, "aaaa".toCharArray(), chain.toArray(new Certificate[0]));
    KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    keyManagerFactory.init(keyStore, "aaaa".toCharArray());
    SSLContext context = SSLContext.getInstance("TLS");
    context.init(keyManagerFactory.getKeyManagers(), null, null);
    return context;
  }
}
