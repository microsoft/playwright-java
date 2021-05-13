package com.microsoft.playwright;

import com.microsoft.playwright.options.Geolocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.microsoft.playwright.options.ColorScheme.DARK;
import static com.microsoft.playwright.Utils.mapOf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class TestDefaultBrowserContext2 extends TestBase {


  private BrowserContext persistentContext;

  @AfterEach
  private void closePersistentContext() {
    if (persistentContext != null) {
      persistentContext.close();
      persistentContext = null;
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
    persistentContext = getBrowserType().launchPersistentContext(userDataDir, options);
    return persistentContext.pages().get(0);
  }

  @Test
  void shouldSupportHasTouchOption() {
    Page page = launchPersistent(new BrowserType.LaunchPersistentContextOptions().setHasTouch(true));
    page.navigate(getServer().PREFIX + "/mobile.html");
    assertEquals(true, page.evaluate("() => 'ontouchstart' in window"));
  }

  @Test
  @DisabledIf(value="com.microsoft.playwright.TestBase#isFirefox", disabledReason="skip")
  void shouldWorkInPersistentContext() {
    // Firefox does not support mobile.
    Page page = launchPersistent(new BrowserType.LaunchPersistentContextOptions()
      .setViewportSize(320, 480).setIsMobile(true));
    page.navigate(getServer().PREFIX + "/empty.html");
    assertEquals(980, page.evaluate("() => window.innerWidth"));
  }

  @Test
  void shouldSupportColorSchemeOption() {
    Page page = launchPersistent(new BrowserType.LaunchPersistentContextOptions().setColorScheme(DARK));
    assertEquals(false, page.evaluate("matchMedia('(prefers-color-scheme: light)').matches"));
    assertEquals(true, page.evaluate("matchMedia('(prefers-color-scheme: dark)').matches"));
  }

  @Test
  void shouldSupportTimezoneIdOption() {
    Page page = launchPersistent(new BrowserType.LaunchPersistentContextOptions()
      .setLocale("en-US").setTimezoneId("America/Jamaica"));
    assertEquals("Sat Nov 19 2016 13:12:34 GMT-0500 (Eastern Standard Time)",
      page.evaluate("new Date(1479579154987).toString()"));
  }

  @Test
  void shouldSupportLocaleOption() {
    Page page = launchPersistent(new BrowserType.LaunchPersistentContextOptions()
      .setLocale("fr-CH"));
    assertEquals("fr-CH", page.evaluate("navigator.language"));
  }

  @Test
  void shouldSupportGeolocationAndPermissionsOptions() {
    Page page = launchPersistent(new BrowserType.LaunchPersistentContextOptions()
      .setGeolocation(new Geolocation(10, 10))
      .setPermissions(asList("geolocation")));
    page.navigate(getServer().EMPTY_PAGE);
    Object geolocation = page.evaluate("() => new Promise(resolve => navigator.geolocation.getCurrentPosition(position => {\n" +
      "  resolve({latitude: position.coords.latitude, longitude: position.coords.longitude});\n" +
      "}))");
    assertEquals(mapOf("latitude", 10, "longitude", 10), geolocation);
  }

  //  @Test
  void shouldSupportIgnoreHTTPSErrorsOption() {
    // TODO: net::ERR_EMPTY_RESPONSE at https://localhost:8908/empty.html
//    Page page = launchPersistent(new BrowserType.LaunchPersistentContextOptions().setIgnoreHTTPSErrors(true));
//    Response response = page.navigate(httpsServer.EMPTY_PAGE);
//    assertTrue(response.ok());
  }

  @Test
  void shouldSupportExtraHTTPHeadersOption() throws ExecutionException, InterruptedException {
//   TODO: test.flaky(browserName === "firefox" && headful && platform === "linux", "Intermittent timeout on bots");
    Page page = launchPersistent(new BrowserType.LaunchPersistentContextOptions().setExtraHTTPHeaders(mapOf("foo", "bar")));
    Future<Server.Request> request = getServer().futureRequest("/empty.html");
    page.navigate(getServer().EMPTY_PAGE);
    assertEquals(asList("bar"), request.get().headers.get("foo"));
  }

  @Test
  void shouldAcceptUserDataDir() throws IOException {
// TODO:   test.flaky(browserName === "chromium");
    Path userDataDir = Files.createTempDirectory("user-data-dir-");
    BrowserContext context = getBrowserType().launchPersistentContext(userDataDir);
    assertTrue(userDataDir.toFile().listFiles().length > 0);
    context.close();
    assertTrue(userDataDir.toFile().listFiles().length > 0);
  }

  @Test
  void shouldRestoreStateFromUserDataDir() throws IOException {
//  TODO:  test.slow();
    Path userDataDir = Files.createTempDirectory("user-data-dir-");
    BrowserType.LaunchPersistentContextOptions browserOptions = null;
    BrowserContext browserContext = getBrowserType().launchPersistentContext(userDataDir, browserOptions);
    Page page = browserContext.newPage();
    page.navigate(getServer().EMPTY_PAGE);
    page.evaluate("() => localStorage.hey = 'hello'");
    browserContext.close();

    BrowserContext browserContext2 = getBrowserType().launchPersistentContext(userDataDir, browserOptions);
    Page page2 = browserContext2.newPage();
    page2.navigate(getServer().EMPTY_PAGE);
    assertEquals("hello", page2.evaluate("localStorage.hey"));
    browserContext2.close();

    Path userDataDir2 = Files.createTempDirectory("user-data-dir-");
    BrowserContext browserContext3 = getBrowserType().launchPersistentContext(userDataDir2, browserOptions);
    Page page3 = browserContext3.newPage();
    page3.navigate(getServer().EMPTY_PAGE);
    assertNotEquals("hello", page3.evaluate("localStorage.hey"));
    browserContext3.close();
  }

  @Test
  void shouldRestoreCookiesFromUserDataDir() throws IOException {
// TODO:   test.flaky(browserName === "chromium");
    Path userDataDir = Files.createTempDirectory("user-data-dir-");
    BrowserType.LaunchPersistentContextOptions browserOptions = null;
    BrowserContext browserContext = getBrowserType().launchPersistentContext(userDataDir, browserOptions);
    Page page = browserContext.newPage();
    page.navigate(getServer().EMPTY_PAGE);
    Object documentCookie = page.evaluate("() => {\n" +
      "    document.cookie = 'doSomethingOnlyOnce=true; expires=Fri, 31 Dec 9999 23:59:59 GMT';\n" +
      "    return document.cookie;\n" +
      "  }");
    assertEquals("doSomethingOnlyOnce=true", documentCookie);
    browserContext.close();

    BrowserContext browserContext2 = getBrowserType().launchPersistentContext(userDataDir, browserOptions);
    Page page2 = browserContext2.newPage();
    page2.navigate(getServer().EMPTY_PAGE);
    assertEquals("doSomethingOnlyOnce=true", page2.evaluate("() => document.cookie"));
    browserContext2.close();

    Path userDataDir2 = Files.createTempDirectory("user-data-dir-");
    BrowserContext browserContext3 = getBrowserType().launchPersistentContext(userDataDir2, browserOptions);
    Page page3 = browserContext3.newPage();
    page3.navigate(getServer().EMPTY_PAGE);
    assertNotEquals("doSomethingOnlyOnce=true", page3.evaluate("() => document.cookie"));
    browserContext3.close();
  }

  @Test
  void shouldHaveDefaultURLWhenLaunchingBrowser() {
    launchPersistent();
    List<String> urls = persistentContext.pages().stream().map(page -> page.url()).collect(Collectors.toList());
    assertEquals(asList("about:blank"), urls);
  }

  @Test
  @DisabledIf(value="com.microsoft.playwright.TestBase#isFirefox", disabledReason="skip")
  void shouldThrowIfPageArgumentIsPassed() throws IOException {
    BrowserType.LaunchPersistentContextOptions options = new BrowserType.LaunchPersistentContextOptions()
      .setArgs(asList(getServer().EMPTY_PAGE));
    Path userDataDir = Files.createTempDirectory("user-data-dir-");
    try {
      getBrowserType().launchPersistentContext(userDataDir, options);
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("can not specify page"));
    }
  }

  @Test
  void shouldWorkWithIgnoreDefaultArgs() {
    // Ignore arguments by name.
    BrowserType.LaunchOptions options = new BrowserType.LaunchOptions().setIgnoreDefaultArgs(asList("foo"));
    Browser browser = getBrowserType().launch(options);
    Page page = browser.newPage();
    browser.close();
    // Check that there is a way to ignore all arguments.
    new BrowserType.LaunchOptions().setIgnoreAllDefaultArgs(true);
  }

  void shouldHavePassedURLWhenLaunchingWithIgnoreDefaultArgsTrue() {
  }

  void shouldHandleTimeout() {
  }

  void shouldHandleException() {
  }

  @Test
  void shouldFireCloseEventForAPersistentContext() {
    launchPersistent();
    boolean[] closed = {false};
    persistentContext.onClose(context -> closed[0] = true);
    closePersistentContext();
    assertTrue(closed[0]);
  }

  void coverageShouldWork() {
    // TODO:
  }

  void coverageShouldBeMissing() {
    // TODO:
  }

  @Test
  void shouldRespectSelectors() {
    Page page = launchPersistent();
    String defaultContextCSS = "{\n" +
      "  create(root, target) {},\n" +
      "  query(root, selector) {\n" +
      "    return root.querySelector(selector);\n" +
      "  },\n" +
      "  queryAll(root, selector) {\n" +
      "    return Array.from(root.querySelectorAll(selector));\n" +
      "  }\n" +
      "}";
    getPlaywright().selectors().register("defaultContextCSS", defaultContextCSS);

    page.setContent("<div>hello</div>");
    assertEquals("hello", page.innerHTML("css=div"));
    assertEquals("hello", page.innerHTML("defaultContextCSS=div"));
  }

}
