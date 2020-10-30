package com.microsoft.playwright;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.microsoft.playwright.ColorScheme.DARK;
import static com.microsoft.playwright.Utils.mapOf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class TestDefaultBrowserContext2 extends TestBase {


  private BrowserContext persistentContext;

  @AfterEach
  private void closePersistentContext() {
    if (persistentContext != null) {
      persistentContext.close();
    }
  }

  private Page launchPersistent() {
    return launchPersistent(null);
  }
  private Page launchPersistent(BrowserType.LaunchPersistentContextOptions options) {
    Path userDataDir = null;
    try {
      userDataDir = Files.createTempDirectory("user-data-dir-");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    assertNull(persistentContext);
    persistentContext = browserType.launchPersistentContext(userDataDir.toString(), options);
    return persistentContext.pages().get(0);
  }

  @Test
  void shouldSupportHasTouchOption() {
    Page page = launchPersistent(new BrowserType.LaunchPersistentContextOptions().withHasTouch(true));
    page.navigate(server.PREFIX + "/mobile.html");
    assertEquals(true, page.evaluate("() => 'ontouchstart' in window"));
  }

  @Test
  void shouldWorkInPersistentContext() {
// TODO:   test.skip(browserName === "firefox");
    // Firefox does not support mobile.
    Page page = launchPersistent(new BrowserType.LaunchPersistentContextOptions()
      .withViewport(320,480).withIsMobile(true));
    page.navigate(server.PREFIX + "/empty.html");
    assertEquals(980, page.evaluate("() => window.innerWidth"));
  }

  @Test
  void shouldSupportColorSchemeOption() {
    Page page = launchPersistent(new BrowserType.LaunchPersistentContextOptions().withColorScheme(DARK));
    assertEquals(false, page.evaluate("matchMedia('(prefers-color-scheme: light)').matches"));
    assertEquals(true, page.evaluate("matchMedia('(prefers-color-scheme: dark)').matches"));
  }

  @Test
  void shouldSupportTimezoneIdOption() {
    Page page = launchPersistent(new BrowserType.LaunchPersistentContextOptions()
      .withLocale("en-US").withTimezoneId("America/Jamaica"));
    assertEquals("Sat Nov 19 2016 13:12:34 GMT-0500 (Eastern Standard Time)",
      page.evaluate("new Date(1479579154987).toString()"));
  }

  @Test
  void shouldSupportLocaleOption() {
    Page page = launchPersistent(new BrowserType.LaunchPersistentContextOptions()
      .withLocale("fr-CH"));
    assertEquals("fr-CH", page.evaluate("navigator.language"));
  }

  @Test
  void shouldSupportGeolocationAndPermissionsOptions() {
    Page page = launchPersistent(new BrowserType.LaunchPersistentContextOptions()
      .withGeolocation(new Geolocation(10, 10))
      .withPermissions(asList("geolocation")));
    page.navigate(server.EMPTY_PAGE);
    Object geolocation = page.evaluate("() => new Promise(resolve => navigator.geolocation.getCurrentPosition(position => {\n" +
      "  resolve({latitude: position.coords.latitude, longitude: position.coords.longitude});\n" +
      "}))");
    assertEquals(mapOf("latitude", 10, "longitude", 10), geolocation);
  }

//  @Test
  void shouldSupportIgnoreHTTPSErrorsOption() {
    // TODO: net::ERR_EMPTY_RESPONSE at https://localhost:8908/empty.html
//    Page page = launchPersistent(new BrowserType.LaunchPersistentContextOptions().withIgnoreHTTPSErrors(true));
//    Response response = page.navigate(httpsServer.EMPTY_PAGE);
//    assertTrue(response.ok());
  }

  @Test
  void shouldSupportExtraHTTPHeadersOption() throws ExecutionException, InterruptedException {
//   TODO: test.flaky(browserName === "firefox" && headful && platform === "linux", "Intermittent timeout on bots");
    Page page = launchPersistent(new BrowserType.LaunchPersistentContextOptions().withExtraHTTPHeaders(mapOf("foo", "bar")));
    Future<Server.Request> request = server.waitForRequest("/empty.html");
    page.navigate(server.EMPTY_PAGE);
    assertEquals(asList("bar"), request.get().headers.get("foo"));
  }

  @Test
  void shouldAcceptUserDataDir() throws IOException {
// TODO:   test.flaky(browserName === "chromium");
    Path userDataDir = Files.createTempDirectory("user-data-dir-");
    BrowserContext context = browserType.launchPersistentContext(userDataDir.toString());
    assertTrue(userDataDir.toFile().listFiles().length > 0);
    context.close();
    assertTrue(userDataDir.toFile().listFiles().length > 0);
  }

  @Test
  void shouldRestoreStateFromUserDataDir() throws IOException {
//  TODO:  test.slow();
    Path userDataDir = Files.createTempDirectory("user-data-dir-");
    BrowserType.LaunchPersistentContextOptions browserOptions = null;
    BrowserContext browserContext = browserType.launchPersistentContext(userDataDir.toString(), browserOptions);
    Page page = browserContext.newPage();
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("() => localStorage.hey = 'hello'");
    browserContext.close();

    BrowserContext browserContext2 = browserType.launchPersistentContext(userDataDir.toString(), browserOptions);
    Page page2 = browserContext2.newPage();
    page2.navigate(server.EMPTY_PAGE);
    assertEquals("hello", page2.evaluate("localStorage.hey"));
    browserContext2.close();

    Path userDataDir2 = Files.createTempDirectory("user-data-dir-");
    BrowserContext browserContext3 = browserType.launchPersistentContext(userDataDir2.toString(), browserOptions);
    Page page3 = browserContext3.newPage();
    page3.navigate(server.EMPTY_PAGE);
    assertNotEquals("hello", page3.evaluate("localStorage.hey"));
    browserContext3.close();
  }
}
