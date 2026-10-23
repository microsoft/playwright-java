package com.microsoft.playwright;

import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Fixture;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.Scope;
import com.microsoft.playwright.options.ClientCertificate;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.microsoft.playwright.Utils.nextFreePort;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.BrowserName.WEBKIT;
import static java.nio.file.Files.readAllBytes;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class TestClientCertificates {
  private static Path asset(String path) {
    return Paths.get("src/test/resources/" + path).toAbsolutePath();
  }

  private static String origin(String urlString) {
    try {
      URL url = new URL(urlString);
      String origin = url.getProtocol() + "://" + url.getHost();
      if (url.getPort() != -1 && url.getPort() != url.getDefaultPort()) {
        origin += ":" + url.getPort();
      }
      return origin;
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  @Fixture(scope = Scope.WORKER)
  static ServerWithClientCertificate customServer() throws IOException {
    return ServerWithClientCertificate.create(nextFreePort());
  }

  @PlaywrightTest
  public void shouldFailWithNoClientCertificatesProvided(Playwright playwright, ServerWithClientCertificate customServer) {
    APIRequestContext request = playwright.request().newContext(
        new APIRequest.NewContextOptions().setIgnoreHTTPSErrors(true));
    APIResponse response = request.get(customServer.url);
    assertTrue(response.text().contains("Sorry, but you need to provide a client certificate to continue."), response.text());
    request.dispose();
  }

  @PlaywrightTest
  public void shouldKeepSupportingHttp(Playwright playwright, Server server) {
    APIRequest.NewContextOptions requestOptions = new APIRequest.NewContextOptions()
      .setIgnoreHTTPSErrors(true) // TODO: remove once we can pass a custom CA.
      .setClientCertificates(asList(
        new ClientCertificate(origin(server.PREFIX))
          .setCertPath(asset("client-certificates/client/trusted/cert.pem"))
          .setKeyPath(asset("client-certificates/client/trusted/key.pem"))));
    APIRequestContext request = playwright.request().newContext(requestOptions);
    APIResponse response = request.get(server.PREFIX + "/one-style.html");
    assertEquals(server.PREFIX + "/one-style.html", response.url());
    assertEquals(200, response.status());
    assertTrue(response.text().contains("<div>hello, world!</div>"));
    request.dispose();
  }

  @PlaywrightTest
  public void shouldThrowWithUntrustedClientCerts(Playwright playwright, ServerWithClientCertificate customServer) {
    APIRequest.NewContextOptions requestOptions = new APIRequest.NewContextOptions()
      .setIgnoreHTTPSErrors(true) // TODO: remove once we can pass a custom CA.
      .setClientCertificates(asList(
        new ClientCertificate(customServer.origin)
          .setCertPath(asset("client-certificates/client/self-signed/cert.pem"))
          .setKeyPath(asset("client-certificates/client/self-signed/key.pem"))));

    APIRequestContext request = playwright.request().newContext(requestOptions);
    APIResponse response = request.get(customServer.url);

    assertEquals(customServer.url, response.url());
    assertEquals(403, response.status());
    assertTrue(response.text().contains("Sorry CN=Bob, certificates from CN=Bob are not welcome here."), response.text());

    request.dispose();
  }

  @PlaywrightTest
  public void passWithTrustedClientCertificates(Playwright playwright, ServerWithClientCertificate customServer) {
    APIRequest.NewContextOptions requestOptions = new APIRequest.NewContextOptions()
      .setIgnoreHTTPSErrors(true) // TODO: remove once we can pass a custom CA.
      .setClientCertificates(asList(
        new ClientCertificate(customServer.origin)
          .setCertPath(asset("client-certificates/client/trusted/cert.pem"))
          .setKeyPath(asset("client-certificates/client/trusted/key.pem"))));

    APIRequestContext request = playwright.request().newContext(requestOptions);
    APIResponse response = request.get(customServer.url);

    assertEquals(customServer.url, response.url());
    assertEquals(200, response.status());
    assertTrue(response.text().contains("Hello CN=Alice, your certificate was issued by O=Client Certificate Demo,CN=localhost!"), response.text());

    request.dispose();
  }

  @PlaywrightTest
  public void passWithTrustedClientCertificatesPfx(Playwright playwright, ServerWithClientCertificate customServer) {
    APIRequest.NewContextOptions requestOptions = new APIRequest.NewContextOptions()
      .setIgnoreHTTPSErrors(true) // TODO: remove once we can pass a custom CA.
      .setClientCertificates(asList(
        new ClientCertificate(customServer.origin)
          .setPfxPath(asset("client-certificates/client/trusted/keystore.p12"))
          .setPassphrase("")));

    APIRequestContext request = playwright.request().newContext(requestOptions);
    APIResponse response = request.get(customServer.url);

    assertEquals(customServer.url, response.url());
    assertEquals(200, response.status());
    assertTrue(response.text().contains("Hello CN=Alice, your certificate was issued by O=Client Certificate Demo,CN=localhost!"), response.text());

    request.dispose();
  }

  @PlaywrightTest
  @DisabledOnBrowser(value = WEBKIT, os = OS.MAC, reason = "The network connection was lost.")
  public void shouldWorkWithBrowserNewContext(Browser browser, ServerWithClientCertificate customServer) {
    Browser.NewContextOptions options = new Browser.NewContextOptions()
      .setIgnoreHTTPSErrors(true) // TODO: remove once we can pass a custom CA.
      .setClientCertificates(asList(
        new ClientCertificate(customServer.origin)
          .setCertPath(asset("client-certificates/client/trusted/cert.pem"))
          .setKeyPath(asset("client-certificates/client/trusted/key.pem"))));

    try (BrowserContext context = browser.newContext(options)) {
      Page page = context.newPage();
      {
        APIResponse response = page.request().get(customServer.crossOrigin);
        assertTrue(response.text().contains("Sorry, but you need to provide a client certificate to continue."), response.text());
      }
      {
        page.navigate(customServer.crossOrigin);
        assertThat(page.getByTestId("message")).hasText("Sorry, but you need to provide a client certificate to continue.");
      }
      page.navigate(customServer.url);
      assertThat(page.getByText("Hello CN=Alice")).isVisible();
      APIResponse response = page.request().get(customServer.url);
      assertTrue(response.text().contains("Hello CN=Alice"), response.text());
    }
  }

  @PlaywrightTest
  @DisabledOnBrowser(value = WEBKIT, os = OS.MAC, reason = "The network connection was lost.")
  public void shouldWorkWithBrowserNewPage(Browser browser, ServerWithClientCertificate customServer) {
    Browser.NewPageOptions options = new Browser.NewPageOptions()
      .setIgnoreHTTPSErrors(true) // TODO: remove once we can pass a custom CA.
      .setClientCertificates(asList(
        new ClientCertificate(customServer.origin)
          .setCertPath(asset("client-certificates/client/trusted/cert.pem"))
          .setKeyPath(asset("client-certificates/client/trusted/key.pem"))));

    try (Page page = browser.newPage(options)) {
      {
        page.navigate(customServer.crossOrigin);
        assertThat(page.getByTestId("message")).hasText("Sorry, but you need to provide a client certificate to continue.");
      }
      {
        APIResponse response = page.request().get(customServer.crossOrigin);
        assertTrue(response.text().contains("Sorry, but you need to provide a client certificate to continue."), response.text());
      }
      page.navigate(customServer.url);
      assertThat(page.getByText("Hello CN=Alice")).isVisible();
      APIResponse response = page.request().get(customServer.url);
      assertTrue(response.text().contains("Hello CN=Alice"), response.text());
    }
  }

  @PlaywrightTest
  @DisabledOnBrowser(value = WEBKIT, os = OS.MAC, reason = "The network connection was lost.")
  public void shouldWorkWithBrowserNewPageWhenPassingAsContent(Browser browser, ServerWithClientCertificate customServer) throws IOException {
    Browser.NewPageOptions options = new Browser.NewPageOptions()
      .setIgnoreHTTPSErrors(true) // TODO: remove once we can pass a custom CA.
      .setClientCertificates(asList(
        new ClientCertificate(customServer.origin)
          .setCert(readAllBytes(asset("client-certificates/client/trusted/cert.pem")))
          .setKey(readAllBytes(asset("client-certificates/client/trusted/key.pem")))));

    try (Page page = browser.newPage(options)) {
       {
        page.navigate(customServer.crossOrigin);
        assertThat(page.getByTestId("message")).hasText("Sorry, but you need to provide a client certificate to continue.");
      }
      {
        APIResponse response = page.request().get(customServer.crossOrigin);
        assertTrue(response.text().contains("Sorry, but you need to provide a client certificate to continue."), response.text());
      }
      page.navigate(customServer.url);
      assertThat(page.getByText("Hello CN=Alice")).isVisible();
      APIResponse response = page.request().get(customServer.url);
      assertTrue(response.text().contains("Hello CN=Alice"), response.text());
    }
  }

  @PlaywrightTest
  @DisabledOnBrowser(value = WEBKIT, os = OS.MAC, reason = "The network connection was lost.")
  // No cleanup: on Windows Chromium may keep chrome_debug.log in the user data dir
  // locked briefly after close, failing the deletion.
  public void shouldWorkWithBrowserLaunchPersistentContext(Browser browser, ServerWithClientCertificate customServer, @TempDir(cleanup = CleanupMode.NEVER) Path tmpDir) {
    BrowserType.LaunchPersistentContextOptions options = new BrowserType.LaunchPersistentContextOptions()
      .setIgnoreHTTPSErrors(true) // TODO: remove once we can pass a custom CA.
      .setClientCertificates(asList(
        new ClientCertificate(customServer.origin)
          .setCertPath(asset("client-certificates/client/trusted/cert.pem"))
          .setKeyPath(asset("client-certificates/client/trusted/key.pem"))));

    try (BrowserContext context = browser.browserType().launchPersistentContext(tmpDir.resolve("profile") , options)) {
      Page page = context.pages().get(0);
      {
        page.navigate(customServer.crossOrigin);
        assertThat(page.getByTestId("message")).hasText("Sorry, but you need to provide a client certificate to continue.");
      }
      {
        APIResponse response = page.request().get(customServer.crossOrigin);
        assertTrue(response.text().contains("Sorry, but you need to provide a client certificate to continue."), response.text());
      }
      page.navigate(customServer.url);
      assertThat(page.getByText("Hello CN=Alice")).isVisible();
      APIResponse response = page.request().get(customServer.url);
      assertTrue(response.text().contains("Hello CN=Alice"), response.text());
    }
  }
}
