/**
 * Copyright 2017 Google Inc. All rights reserved.
 * Modifications copyright (c) Microsoft Corporation.
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

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.CreateUserDataDir;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.HttpsTestServer;
import com.microsoft.playwright.fixtures.LaunchPersistent;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.ColorScheme;
import com.microsoft.playwright.options.Contrast;
import com.microsoft.playwright.options.ForcedColors;
import com.microsoft.playwright.options.ReducedMotion;
import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.DisplayName;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.asset;
import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static com.microsoft.playwright.fixtures.Utils.relativeToCwd;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/defaultbrowsercontext-2.spec.ts. */
public class TestDefaultbrowsercontext2 {
  @PlaywrightTest
  @DisplayName("DC-012 should support hasTouch option")
  void shouldSupportHasTouchOption(TestServer server, LaunchPersistent launchPersistent) {
    Page page = launchPersistent.launch(new BrowserType.LaunchPersistentContextOptions().setHasTouch(true)).page;
    page.navigate(server.PREFIX + "/mobile.html");
    assertEquals(true, page.evaluate("() => 'ontouchstart' in window"));
  }

  @PlaywrightTest
  @DisplayName("DC-013 should work in persistent context")
  void shouldWorkInPersistentContext(TestServer server, LaunchPersistent launchPersistent, BrowserName browserName) {
    Page page = launchPersistent.launch(new BrowserType.LaunchPersistentContextOptions().setViewportSize(320, 480).setIsMobile(true)).page;
    page.navigate(server.PREFIX + "/empty.html");
    assertEquals(980, page.evaluate("() => window.innerWidth"));
  }

  @PlaywrightTest
  @DisplayName("DC-014 should support colorScheme option")
  void shouldSupportColorSchemeOption(LaunchPersistent launchPersistent) {
    Page page = launchPersistent.launch(new BrowserType.LaunchPersistentContextOptions().setColorScheme(ColorScheme.DARK)).page;
    assertEquals(false, page.evaluate("() => matchMedia('(prefers-color-scheme: light)').matches"));
    assertEquals(true, page.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches"));
  }

  @PlaywrightTest
  @DisplayName("DC-015 should support reducedMotion option")
  void shouldSupportReducedMotionOption(LaunchPersistent launchPersistent) {
    Page page = launchPersistent.launch(new BrowserType.LaunchPersistentContextOptions().setReducedMotion(ReducedMotion.REDUCE)).page;
    assertEquals(true, page.evaluate("() => matchMedia('(prefers-reduced-motion: reduce)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(prefers-reduced-motion: no-preference)').matches"));
  }

  @PlaywrightTest
  @DisplayName("DC-016 should support forcedColors option")
  void shouldSupportForcedColorsOption(LaunchPersistent launchPersistent, BrowserName browserName) {
    Page page = launchPersistent.launch(new BrowserType.LaunchPersistentContextOptions().setForcedColors(ForcedColors.ACTIVE)).page;
    assertEquals(true, page.evaluate("() => matchMedia('(forced-colors: active)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(forced-colors: none)').matches"));
  }

  @PlaywrightTest
  @DisplayName("DC-017 should support contrast option")
  void shouldSupportContrastOption(LaunchPersistent launchPersistent) {
    Page page = launchPersistent.launch(new BrowserType.LaunchPersistentContextOptions().setContrast(Contrast.MORE)).page;
    // Java: expect.soft is a plain assertion.
    assertEquals(true, page.evaluate("() => matchMedia('(prefers-contrast: more)').matches"));
    assertEquals(false, page.evaluate("() => matchMedia('(prefers-contrast: no-preference)').matches"));
  }

  @PlaywrightTest
  @DisplayName("DC-018 should support timezoneId option")
  void shouldSupportTimezoneIdOption(LaunchPersistent launchPersistent, BrowserName browserName) {
    Page page = launchPersistent.launch(new BrowserType.LaunchPersistentContextOptions().setLocale("en-US").setTimezoneId("America/Jamaica")).page;
    assertEquals("Sat Nov 19 2016 13:12:34 GMT-0500 (Eastern Standard Time)", page.evaluate("() => new Date(1479579154987).toString()"));
  }

  @PlaywrightTest
  @DisplayName("DC-019 should support locale option")
  void shouldSupportLocaleOption(LaunchPersistent launchPersistent) {
    Page page = launchPersistent.launch(new BrowserType.LaunchPersistentContextOptions().setLocale("fr-FR")).page;
    assertEquals("fr-FR", page.evaluate("() => navigator.language"));
  }

  @PlaywrightTest
  @DisplayName("DC-020 should support geolocation and permissions options")
  void shouldSupportGeolocationAndPermissionsOptions(TestServer server, LaunchPersistent launchPersistent) {
    Page page = launchPersistent.launch(new BrowserType.LaunchPersistentContextOptions()
      .setGeolocation(10, 10).setPermissions(Arrays.asList("geolocation"))).page;
    page.navigate(server.EMPTY_PAGE);
    Object geolocation = page.evaluate("() => new Promise(resolve => navigator.geolocation.getCurrentPosition(position => {\n" +
      "  resolve({ latitude: position.coords.latitude, longitude: position.coords.longitude });\n" +
      "}))");
    assertEquals(mapOf("latitude", 10, "longitude", 10), geolocation);
  }

  @PlaywrightTest
  @DisplayName("DC-021 should support ignoreHTTPSErrors option")
  void shouldSupportIgnoreHTTPSErrorsOption(HttpsTestServer httpsServer, LaunchPersistent launchPersistent) {
    Page page = launchPersistent.launch(new BrowserType.LaunchPersistentContextOptions().setIgnoreHTTPSErrors(true)).page;
    Response response = page.navigate(httpsServer.EMPTY_PAGE);
    assertTrue(response.ok());
  }

  @PlaywrightTest
  @DisplayName("DC-022 should support extraHTTPHeaders option")
  void shouldSupportExtraHTTPHeadersOption(TestServer server, LaunchPersistent launchPersistent) throws Exception {
    Page page = launchPersistent.launch(new BrowserType.LaunchPersistentContextOptions().setExtraHTTPHeaders(mapOfStrings("foo", "bar"))).page;
    CompletableFuture<TestServer.Request> requestPromise = server.waitForRequest("/empty.html");
    page.navigate(server.EMPTY_PAGE);
    TestServer.Request request = requestPromise.get();
    assertEquals("bar", request.headers.get("foo"));
  }

  private static java.util.Map<String, String> mapOfStrings(String key, String value) {
    return Collections.singletonMap(key, value);
  }

  @PlaywrightTest
  @DisplayName("DC-023 should accept userDataDir")
  void shouldAcceptUserDataDir(CreateUserDataDir createUserDataDir, BrowserType browserType) {
    Path userDataDir = createUserDataDir.create();
    BrowserContext context = browserType.launchPersistentContext(userDataDir, Env.launchPersistentContextOptions());
    assertTrue(userDataDir.toFile().list().length > 0);
    context.close();
    assertTrue(userDataDir.toFile().list().length > 0);
  }

  @PlaywrightTest
  @DisplayName("DC-024 should accept relative userDataDir")
  void shouldAcceptRelativeUserDataDir(CreateUserDataDir createUserDataDir, BrowserType browserType) {
    Path userDataDir = createUserDataDir.create();
    BrowserContext context = browserType.launchPersistentContext(
      relativeToCwd(userDataDir.resolve("foobar")), Env.launchPersistentContextOptions());
    assertTrue(userDataDir.resolve("foobar").toFile().list().length > 0);
    context.close();
  }

  @PlaywrightTest
  @DisplayName("DC-025 should restore state from userDataDir")
  void shouldRestoreStateFromUserDataDir(BrowserType browserType, TestServer server, CreateUserDataDir createUserDataDir) {
    assumeFalse("webkit-wsl".equals(Env.channel()), "fixme: Pending local storage writes are lost on close, see https://github.com/microsoft/playwright-browsers/issues/2275");

    Path userDataDir = createUserDataDir.create();
    BrowserContext browserContext = browserType.launchPersistentContext(userDataDir, Env.launchPersistentContextOptions());
    Page page = browserContext.newPage();
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("() => localStorage.hey = 'hello'");
    // Browsers do not persist local storage immediately, they do it asynchronously in another process.
    // Navigate away to give it a chance to save (best-effort).
    page.navigate(server.EMPTY_PAGE);
    browserContext.close();

    BrowserContext browserContext2 = browserType.launchPersistentContext(userDataDir, Env.launchPersistentContextOptions());
    Page page2 = browserContext2.newPage();
    page2.navigate(server.EMPTY_PAGE);
    assertEquals("hello", page2.evaluate("() => localStorage.hey"));
    browserContext2.close();

    Path userDataDir2 = createUserDataDir.create();
    BrowserContext browserContext3 = browserType.launchPersistentContext(userDataDir2, Env.launchPersistentContextOptions());
    Page page3 = browserContext3.newPage();
    page3.navigate(server.EMPTY_PAGE);
    assertNotEquals("hello", page3.evaluate("() => localStorage.hey"));
    browserContext3.close();
  }

  @PlaywrightTest
  @DisplayName("DC-026 should create userDataDir if it does not exist")
  void shouldCreateUserDataDirIfItDoesNotExist(CreateUserDataDir createUserDataDir, BrowserType browserType) {
    Path userDataDir = createUserDataDir.create().resolve("nonexisting");
    BrowserContext context = browserType.launchPersistentContext(userDataDir, Env.launchPersistentContextOptions());
    context.close();
    assertTrue(userDataDir.toFile().list().length > 0);
  }

  @PlaywrightTest
  @DisplayName("DC-027 should goto about:blank on relaunched persistent context")
  void shouldGotoAboutBlankOnRelaunchedPersistentContext(BrowserType browserType, CreateUserDataDir createUserDataDir, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.FIREFOX && !Env.isBidi(), "fixme: ");

    Path userDataDir = createUserDataDir.create();

    BrowserContext context1 = browserType.launchPersistentContext(userDataDir, Env.launchPersistentContextOptions());
    context1.pages().get(0).navigate("about:blank");
    context1.close();

    // When relaunching with an existing profile, Firefox session restore can race with the user's goto and cause "interrupted by another navigation".
    // This issue is timing-sensitive and might not fire on every run, so rely on CI's --repeat-each matrix for coverage.
    BrowserContext context2 = browserType.launchPersistentContext(userDataDir, Env.launchPersistentContextOptions());
    context2.pages().get(0).navigate("about:blank");
    assertEquals("about:blank", context2.pages().get(0).url());
    context2.close();
  }

  @PlaywrightTest
  @DisplayName("DC-028 should have default URL when launching browser")
  void shouldHaveDefaultURLWhenLaunchingBrowser(LaunchPersistent launchPersistent) {
    BrowserContext context = launchPersistent.launch().context;
    List<String> urls = new ArrayList<>();
    for (Page page : context.pages()) {
      urls.add(page.url());
    }
    assertEquals(Arrays.asList("about:blank"), urls);
  }

  @PlaywrightTest
  @DisplayName("DC-029 should throw if page argument is passed")
  void shouldThrowIfPageArgumentIsPassed(BrowserType browserType, TestServer server, CreateUserDataDir createUserDataDir, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.FIREFOX && !Env.isBidi());

    BrowserType.LaunchPersistentContextOptions options = Env.launchPersistentContextOptions().setArgs(Arrays.asList(server.EMPTY_PAGE));
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> browserType.launchPersistentContext(createUserDataDir.create(), options));
    assertTrue(error.getMessage().contains("can not specify page"));
  }

  @PlaywrightTest
  @DisplayName("DC-033 should fire close event for a persistent context")
  void shouldFireCloseEventForAPersistentContext(LaunchPersistent launchPersistent) {
    BrowserContext context = launchPersistent.launch().context;
    boolean[] closed = {false};
    context.onClose(c -> closed[0] = true);
    context.close();
    assertEquals(true, closed[0]);
  }

  @PlaywrightTest
  @DisplayName("DC-035 should respect selectors")
  void shouldRespectSelectors(Playwright playwright, LaunchPersistent launchPersistent) {
    String defaultContextCSS = "{\n" +
      "  query(root, selector) {\n" +
      "    return root.querySelector(selector);\n" +
      "  },\n" +
      "  queryAll(root, selector) {\n" +
      "    return Array.from(root.querySelectorAll(selector));\n" +
      "  }\n" +
      "}";
    playwright.selectors().register("defaultContextCSS", defaultContextCSS);

    Page page = launchPersistent.launch().page;
    page.setContent("<div>hello</div>");
    assertEquals("hello", page.innerHTML("css=div"));
    assertEquals("hello", page.innerHTML("defaultContextCSS=div"));
  }

  @PlaywrightTest
  @DisplayName("DC-037 should support har option")
  void shouldSupportHarOption(LaunchPersistent launchPersistent) {
    Path path = asset("har-fulfill.har");
    Page page = launchPersistent.launch().page;
    page.routeFromHAR(path);
    page.navigate("http://no.playwright/");
    // HAR contains a redirect for the script that should be followed automatically.
    assertEquals("foo", page.evaluate("window.value"));
    // HAR contains a POST for the css file that should not be used.
    assertThat(page.locator("body")).hasCSS("background-color", "rgb(255, 0, 0)");
  }

  @PlaywrightTest
  @DisplayName("DC-039 dialog.accept should work")
  void dialogAcceptShouldWork(LaunchPersistent launchPersistent) {
    LaunchPersistent.Result result = launchPersistent.launch();
    BrowserContext context = result.context;
    Page page = result.page;
    page.navigate("data:text/html,<html><title>Title</title><button onclick=\"alert('Alert')\">Button</button></html>");
    AtomicBoolean shown = new AtomicBoolean(false);
    page.onDialog(dialog -> {
      shown.set(true);
      dialog.accept();
    });
    page.getByRole(com.microsoft.playwright.options.AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Button")).click();
    assertTrue(shown.get());
    context.close();
  }

  @PlaywrightTest
  @DisplayName("DC-040 CacheStorage entry should survive page.reload()")
  void cacheStorageEntryShouldSurvivePageReload(LaunchPersistent launchPersistent, TestServer server) {
    LaunchPersistent.Result result = launchPersistent.launch();
    BrowserContext context = result.context;
    Page page = result.page;
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("async () => {\n" +
      "  const cache = await caches.open('repro-cache');\n" +
      "  await cache.put('/meta', new Response('payload'));\n" +
      "}");

    page.reload();

    Object after = page.evaluate("async () => {\n" +
      "  const cache = await caches.open('repro-cache');\n" +
      "  const resp = await cache.match('/meta');\n" +
      "  return resp ? await resp.text() : null;\n" +
      "}");
    assertEquals("payload", after);
    context.close();
  }

  @PlaywrightTest
  @DisplayName("DC-041 exposes browser")
  void exposesBrowser(LaunchPersistent launchPersistent) {
    BrowserContext context = launchPersistent.launch().context;
    com.microsoft.playwright.Browser browser = context.browser();
    assertFalse(browser.version().isEmpty());
    Page page = browser.newPage();
    page.navigate("data:text/html,<html><title>Title</title></html>");
    assertEquals("Title", page.title());
    browser.close();
    assertEquals(0, context.pages().size());
    // Next line should not throw.
    context.close();
  }

  @PlaywrightTest
  @DisplayName("DC-042 should support storage.getDirectory()")
  void shouldSupportStorageGetDirectory(LaunchPersistent launchPersistent, TestServer server) {
    BrowserContext context = launchPersistent.launch().context;
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    Object name = page.evaluate("async () => {\n" +
      "  const dir = await navigator.storage.getDirectory();\n" +
      "  return dir.name;\n" +
      "}");
    assertEquals("", name);
    context.close();
  }
}
