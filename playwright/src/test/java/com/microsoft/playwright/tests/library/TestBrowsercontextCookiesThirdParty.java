/**
 * Copyright Microsoft Corporation. All rights reserved.
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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.ContextFactory;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.Fixture;
import com.microsoft.playwright.fixtures.HttpsTestServer;
import com.microsoft.playwright.fixtures.LaunchPersistent;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.SameSiteAttribute;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.function.Executable;

import java.net.URI;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/browsercontext-cookies-third-party.spec.ts. */
public class TestBrowsercontextCookiesThirdParty {
  public static class TestUrls {
    String origin1;
    String origin2;
    String read_origin1;
    String read_origin2_origin1;
    String read_origin1_origin1;
    String read_origin1_origin2_origin1;
    String set_origin1;
    String set_origin2_origin1;
    String set_origin1_origin2_origin1;
  }

  public static class WebkitPartitions {
    final boolean value;

    WebkitPartitions(boolean value) {
      this.value = value;
    }
  }

  @Fixture
  TestUrls urls(HttpsTestServer httpsServer) {
    TestUrls urls = new TestUrls();
    String origin1 = httpsServer.PREFIX;
    String origin2 = httpsServer.CROSS_PROCESS_PREFIX;
    urls.origin1 = origin1;
    urls.origin2 = origin2;
    urls.read_origin1 = origin1 + "/read-cookie.html";
    urls.read_origin2_origin1 = origin2 + "/frame-read-cookie.html";
    urls.read_origin1_origin1 = origin1 + "/frame-read-cookie.html";
    urls.read_origin1_origin2_origin1 = origin1 + "/nested-frame-read-cookie.html";
    urls.set_origin1 = origin1 + "/set-cookie.html";
    urls.set_origin2_origin1 = origin2 + "/frame-set-cookie.html";
    urls.set_origin1_origin2_origin1 = origin1 + "/nested-frame-set-cookie.html";
    return urls;
  }

  @Fixture
  WebkitPartitions webkitPartitions(BrowserName browserName) {
    return new WebkitPartitions(browserName == BrowserName.WEBKIT && Env.isMac && Env.macVersion() >= 26);
  }

  @Fixture
  Browser.NewContextOptions contextOptions() {
    return new Browser.NewContextOptions().setIgnoreHTTPSErrors(true);
  }

  // Java: the defaultSameSiteCookieValue fixture of tests/config/browserTest.ts.
  private static SameSiteAttribute defaultSameSiteCookieValue(BrowserName browserName) {
    if (browserName == BrowserName.CHROMIUM || Env.isBidi())
      return SameSiteAttribute.LAX;
    if (browserName == BrowserName.WEBKIT && (Env.platform().equals("linux") || "webkit-wsl".equals(Env.channel())))
      return SameSiteAttribute.LAX;
    if (browserName == BrowserName.WEBKIT)
      return SameSiteAttribute.NONE; // Windows + older macOS
    if (browserName == BrowserName.FIREFOX)
      return SameSiteAttribute.NONE;
    throw new IllegalStateException("unknown browser - " + browserName);
  }

  // Java: the allowsThirdParty fixture of tests/config/browserTest.ts.
  private static boolean allowsThirdParty(BrowserName browserName) {
    return browserName == BrowserName.FIREFOX;
  }

  // Java: expect.soft(): the failures are collected and reported when the test ends.
  private static class Soft {
    private final List<Throwable> failures = new ArrayList<>();

    void run(Runnable assertion) {
      try {
        assertion.run();
      } catch (AssertionError e) {
        failures.add(e);
      }
    }

    void finish() {
      assertAll(failures.stream().map(failure -> (Executable) () -> {
        throw failure;
      }));
    }
  }

  private static LocatorAssertions.HasTextOptions timeout(double timeout) {
    return new LocatorAssertions.HasTextOptions().setTimeout(timeout);
  }

  /**
   * origin1:
   *   top-level-partitioned=value
   *   top-level-non-partitioned=value
   *
   * origin2:
   *   origin1:
   *     frame-partitioned=value
   *     frame-non-partitioned=value
   *
   * origin1:
   *   origin2:
   *     origin1:
   *       frame-partitioned=value
   *       frame-non-partitioned=value
   *
   * origin1 = httpsServer.PREFIX
   * origin2 = httpsServer.CROSS_PROCESS_PREFIX
   */
  private static void addCommonCookieHandlers(TestServer httpsServer, TestUrls urls) {
    // '/set-cookie.html' handlers are added in the tests.
    httpsServer.setRoute("/read-cookie.html", (req, res) -> {
      res.setHeader("Content-Type", "text/html");
      String header = req.headers.get("cookie");
      // Java: an undefined cookies string is printed as "undefined".
      String cookies = header == null ? "undefined" : Arrays.stream(header.split(";")).map(c -> c.trim()).sorted().collect(Collectors.joining("; "));
      res.end("Received cookie: " + cookies);
    });
    httpsServer.setRoute("/frame-set-cookie.html", (req, res) -> {
      res.setHeader("Content-Type", "text/html");
      res.end("<iframe src='" + urls.origin1 + "/set-cookie.html'></iframe>");
    });
    httpsServer.setRoute("/frame-read-cookie.html", (req, res) -> {
      res.setHeader("Content-Type", "text/html");
      res.end("<iframe src='" + urls.origin1 + "/read-cookie.html'></iframe>");
    });
    // Nested cross-origin iframe:
    //   main frame: (origin1 or origin2) -> iframe1: origin2 -> iframe2: origin1
    httpsServer.setRoute("/nested-frame-set-cookie.html", (req, res) -> {
      res.setHeader("Content-Type", "text/html");
      res.end("<iframe src='" + urls.origin2 + "/frame-set-cookie.html'></iframe>");
    });
    httpsServer.setRoute("/nested-frame-read-cookie.html", (req, res) -> {
      res.setHeader("Content-Type", "text/html");
      res.end("<iframe src='" + urls.origin2 + "/frame-read-cookie.html'></iframe>");
    });
  }

  private static Cookie findCookie(List<Cookie> cookies, String name) {
    Cookie result = cookies.stream().filter(cookie -> cookie.name.equals(name)).findFirst().orElse(null);
    assertNotNull(result, "Cookie " + name + " not found in " + cookies.stream().map(c -> c.name).collect(Collectors.toList()));
    return result;
  }

  private static void expectPartitionKey(List<Cookie> cookies, String name, String partitionKey) {
    Cookie cookie = findCookie(cookies, name);
    if (!Objects.equals(partitionKey, cookie.partitionKey))
      throw new AssertionError("Cookie " + name + " has partitionKey " + cookie.partitionKey + " but expected " + partitionKey + ".");
  }

  // Java: the cookies of a storage state, as Cookie objects with the name and the partitionKey.
  private static List<Cookie> storageStateCookies(String storageState) {
    List<Cookie> cookies = new ArrayList<>();
    for (JsonElement element : JsonParser.parseString(storageState).getAsJsonObject().getAsJsonArray("cookies")) {
      JsonObject object = element.getAsJsonObject();
      Cookie cookie = new Cookie(object.get("name").getAsString(), object.get("value").getAsString());
      if (object.has("partitionKey"))
        cookie.partitionKey = object.get("partitionKey").getAsString();
      cookies.add(cookie);
    }
    return cookies;
  }

  private static class NonPartitionedResult {
    String expectedTopLevel;
    String expectedThirdParty;
  }

  private static NonPartitionedResult runNonPartitionedTest(Page page, TestServer httpsServer, BrowserName browserName, boolean isMac, boolean isLinux, boolean isBidi, String channel, TestUrls urls) {
    addCommonCookieHandlers(httpsServer, urls);
    httpsServer.setRoute("/set-cookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", (req.headers.get("referer") != null ? "frame" : "top-level") + "=value; SameSite=None; Path=/; Secure;");
      res.setHeader("Content-Type", "text/html");
      res.end();
    });

    page.navigate(urls.set_origin1);
    page.navigate(urls.read_origin1);
    assertEquals("Received cookie: top-level=value", page.locator("body").textContent());

    page.navigate(urls.read_origin2_origin1);
    Locator frameBody = page.locator("iframe").contentFrame().locator("body");

    // WebKit does not support third-party cookies without a 'Partition' attribute.
    if ((browserName == BrowserName.WEBKIT && isMac) || (browserName == BrowserName.FIREFOX && isBidi))
      assertThat(frameBody).hasText("Received cookie: undefined");
    else
      assertThat(frameBody).hasText("Received cookie: top-level=value");

    // Set cookie and do second navigation.
    page.navigate(urls.set_origin2_origin1);
    page.navigate(urls.read_origin2_origin1);
    String expectedThirdParty = "Received cookie: ";
    if (browserName == BrowserName.WEBKIT && isMac)
      expectedThirdParty += "undefined";
    else if (browserName == BrowserName.WEBKIT && (isLinux || "webkit-wsl".equals(channel)))
      expectedThirdParty += "top-level=value";
    else if (browserName == BrowserName.FIREFOX && isBidi)
      expectedThirdParty += "frame=value";
    else
      expectedThirdParty += "frame=value; top-level=value";
    assertThat(frameBody).hasText(expectedThirdParty, timeout(1000));

    // Check again the top-level cookie.
    page.navigate(urls.read_origin1);
    String expectedTopLevel = (browserName == BrowserName.WEBKIT && (isMac || isLinux || "webkit-wsl".equals(channel))) || (browserName == BrowserName.FIREFOX && isBidi) ?
      "Received cookie: top-level=value" :
      "Received cookie: frame=value; top-level=value";
    assertEquals(expectedTopLevel, page.locator("body").textContent());

    NonPartitionedResult result = new NonPartitionedResult();
    result.expectedTopLevel = expectedTopLevel;
    result.expectedThirdParty = expectedThirdParty;
    return result;
  }

  @PlaywrightTest
  @DisplayName("CO-052 third party non-partitioned cookies")
  void thirdPartyNonPartitionedCookies(Page page, BrowserName browserName, HttpsTestServer httpsServer, TestUrls urls) {
    runNonPartitionedTest(page, httpsServer, browserName, Env.isMac, Env.isLinux, Env.isBidi(), Env.channel(), urls);
  }

  @PlaywrightTest
  @DisplayName("CO-053 save/load third party non-partitioned cookies")
  void saveLoadThirdPartyNonPartitionedCookies(Page page, BrowserName browserName, HttpsTestServer httpsServer, Browser browser, ContextFactory contextFactory, TestUrls urls) {
    // Run the test to populate the cookies.
    NonPartitionedResult result = runNonPartitionedTest(page, httpsServer, browserName, Env.isMac, Env.isLinux, Env.isBidi(), Env.channel(), urls);
    String expectedTopLevel = result.expectedTopLevel;
    String expectedThirdParty = result.expectedThirdParty;
    Soft soft = new Soft();

    java.util.function.Consumer<Page> checkCookies = page1 -> {
      // Check top-level cookie first.
      page1.navigate(urls.read_origin1);
      soft.run(() -> assertEquals(expectedTopLevel, page1.locator("body").textContent()));

      // Check third-party cookie.
      page1.navigate(urls.read_origin2_origin1);
      Locator frameBody = page1.locator("iframe").contentFrame().locator("body");
      soft.run(() -> assertThat(frameBody).hasText(expectedThirdParty));
    };

    checkCookies.accept(page);

    // it.step('export via cookies/addCookies')
    {
      List<Cookie> cookies = page.context().cookies();
      // Java: browser.newContext() gets the test.use() options upstream.
      BrowserContext context2 = contextFactory.newContext();
      context2.addCookies(cookies);
      Page page2 = context2.newPage();
      checkCookies.accept(page2);
    }

    // it.step('export via storageState')
    {
      String storageState = page.context().storageState();
      // Java: browser.newContext() gets the test.use() options upstream.
      BrowserContext context3 = contextFactory.newContext(new Browser.NewContextOptions().setIgnoreHTTPSErrors(true).setStorageState(storageState));
      Page page3 = context3.newPage();
      checkCookies.accept(page3);
    }
    soft.finish();
  }

  private static void runPartitionedTest(Page page, TestServer httpsServer, BrowserName browserName, boolean isMac, boolean webkitPartitions, boolean isBidi, TestUrls urls) {
    addCommonCookieHandlers(httpsServer, urls);
    httpsServer.setRoute("/set-cookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList(
        (req.headers.get("referer") != null ? "frame" : "top-level") + "-partitioned=value; SameSite=None; Path=/; Secure; Partitioned;",
        (req.headers.get("referer") != null ? "frame" : "top-level") + "-non-partitioned=value; SameSite=None; Path=/; Secure;"));
      res.end();
    });

    page.navigate(urls.set_origin1);
    page.navigate(urls.read_origin1);
    assertEquals("Received cookie: top-level-non-partitioned=value; top-level-partitioned=value", page.locator("body").textContent());

    page.navigate(urls.read_origin2_origin1);
    Locator frameBody = page.locator("iframe").contentFrame().locator("body");

    // Firefox cookie partitioning is disabled in Firefox(Juggler).
    // TODO: reenable cookie partitioning?
    if (browserName == BrowserName.FIREFOX && !isBidi) {
      assertThat(frameBody).hasText("Received cookie: top-level-non-partitioned=value; top-level-partitioned=value");
      return;
    }

    // Linux and Windows WebKit builds do not partition third-party cookies at all.
    if (browserName == BrowserName.WEBKIT && !isMac) {
      assertThat(frameBody).hasText("Received cookie: top-level-non-partitioned=value; top-level-partitioned=value");
      return;
    }

    if (browserName == BrowserName.WEBKIT || (browserName == BrowserName.FIREFOX && isBidi)) {
      // WebKit will only send 'Partitioned' third-party cookies exactly matching the partition.
      assertThat(frameBody).hasText("Received cookie: undefined");
    } else {
      // For non-partitioned cookies, the cookie is sent to the iframe right away,
      // if third-party cookies are supported by the browser.
      assertThat(frameBody).hasText("Received cookie: top-level-non-partitioned=value");
    }

    // First navigation:
    // - no cookie sent, as it was only set on the top-level site
    // - sets the third-party cookie for the top-level context
    // Second navigation:
    // - sends the cookie as it was just set for the (top-level site, iframe url) partition.
    page.navigate(urls.set_origin2_origin1);
    page.navigate(urls.read_origin2_origin1);
    if (browserName == BrowserName.WEBKIT)
      assertThat(frameBody).hasText(webkitPartitions ? "Received cookie: frame-partitioned=value" : "Received cookie: undefined");
    else if (browserName == BrowserName.FIREFOX && isBidi)
      assertThat(frameBody).hasText("Received cookie: frame-non-partitioned=value; frame-partitioned=value");
    else
      assertThat(frameBody).hasText("Received cookie: frame-non-partitioned=value; frame-partitioned=value; top-level-non-partitioned=value");
  }

  @PlaywrightTest
  @DisplayName("CO-054 third party 'Partitioned;' cookies")
  @Fixme(value = "driver: the Firefox build of the bundled driver partitions the third-party cookie, upstream expects the Juggler build not to", browsers = BrowserName.FIREFOX)
  void thirdPartyPartitionedCookies(Page page, BrowserName browserName, HttpsTestServer httpsServer, WebkitPartitions webkitPartitions, TestUrls urls) {
    runPartitionedTest(page, httpsServer, browserName, Env.isMac, webkitPartitions.value, Env.isBidi(), urls);
  }

  @PlaywrightTest
  @DisplayName("CO-055 save/load third party 'Partitioned;' cookies")
  @Fixme(value = "java: the Cookie class has no _crHasCrossSiteAncestor property, so a partitioned cookie from cookies() or added with addCookies() is read in the wrong frame context: expected 'Received cookie: frame-non-partitioned=value; top-level-non-partitioned=value' but was '...; top-level-partitioned=value'", browsers = BrowserName.CHROMIUM)
  void saveLoadThirdPartyPartitionedCookies(Page page, BrowserName browserName, HttpsTestServer httpsServer, WebkitPartitions webkitPartitionsFixture, Browser browser, ContextFactory contextFactory, TestUrls urls) {
    boolean isMac = Env.isMac;
    boolean isBidi = Env.isBidi();
    boolean webkitPartitions = webkitPartitionsFixture.value;
    assumeFalse(browserName == BrowserName.FIREFOX && !isBidi, "fixme: Firefox cookie partitioning is disabled in Firefox(Juggler).");
    assumeFalse(browserName == BrowserName.WEBKIT && !isMac, "fixme: Linux and Windows WebKit builds do not partition third-party cookies at all.");

    runPartitionedTest(page, httpsServer, browserName, isMac, webkitPartitions, isBidi, urls);
    Soft soft = new Soft();

    java.util.function.Consumer<Page> checkCookies = page1 -> {
      {
        // Check top-level cookie first.
        page1.navigate(urls.read_origin1);
        String expectedTopLevel = browserName == BrowserName.WEBKIT && isMac ?
          "Received cookie: top-level-non-partitioned=value; top-level-partitioned=value" :
          "Received cookie: frame-non-partitioned=value; top-level-non-partitioned=value; top-level-partitioned=value";
        soft.run(() -> assertEquals(expectedTopLevel, page1.locator("body").textContent()));
      }
      {
        // Check third-party cookie.
        page1.navigate(urls.read_origin2_origin1);
        Locator frameBody = page1.locator("iframe").contentFrame().locator("body");
        String expectedThirdParty = browserName == BrowserName.WEBKIT ?
          (webkitPartitions ? "Received cookie: frame-partitioned=value" : "Received cookie: undefined") :
          "Received cookie: frame-non-partitioned=value; frame-partitioned=value; top-level-non-partitioned=value";
        soft.run(() -> assertThat(frameBody).hasText(expectedThirdParty, timeout(1000)));
      }
      {
        page1.navigate(urls.read_origin1_origin2_origin1); // read-origin1-origin2-origin1.html
        Locator frameBody = page1.locator("iframe").contentFrame().locator("iframe").contentFrame().locator("body");
        String expectedThirdParty = browserName == BrowserName.WEBKIT ?
          "Received cookie: top-level-non-partitioned=value; top-level-partitioned=value" :
          "Received cookie: frame-non-partitioned=value; top-level-non-partitioned=value";
        soft.run(() -> assertThat(frameBody).hasText(expectedThirdParty, timeout(1000)));
      }
    };

    checkCookies.accept(page);

    java.util.function.Consumer<List<Cookie>> checkStorageCookies = cookies -> {
      boolean webkitNoPartition = browserName == BrowserName.WEBKIT && !webkitPartitions;
      expectPartitionKey(cookies, "top-level-partitioned", webkitNoPartition ? null : "https://localhost");
      expectPartitionKey(cookies, "top-level-non-partitioned", null);
      if (webkitNoPartition) {
        assertNull(cookies.stream().filter(cookie -> cookie.name.equals("frame-partitioned")).findFirst().orElse(null));
        assertNull(cookies.stream().filter(cookie -> cookie.name.equals("frame-non-partitioned")).findFirst().orElse(null));
      } else {
        expectPartitionKey(cookies, "frame-partitioned", "https://127.0.0.1");
        if (browserName == BrowserName.WEBKIT)
          assertNull(cookies.stream().filter(cookie -> cookie.name.equals("frame-non-partitioned")).findFirst().orElse(null));
        else
          expectPartitionKey(cookies, "frame-non-partitioned", null);
      }
    };
    checkStorageCookies.accept(page.context().cookies());
    checkStorageCookies.accept(storageStateCookies(page.context().storageState()));

    // it.step('export via cookies/addCookies')
    {
      List<Cookie> cookies = page.context().cookies();
      // Java: browser.newContext() gets the test.use() options upstream.
      BrowserContext context2 = contextFactory.newContext();
      context2.addCookies(cookies);
      Page page2 = context2.newPage();
      checkCookies.accept(page2);
    }

    // it.step('export via storageState')
    {
      String storageState = page.context().storageState();
      // Java: browser.newContext() gets the test.use() options upstream.
      BrowserContext context3 = contextFactory.newContext(new Browser.NewContextOptions().setIgnoreHTTPSErrors(true).setStorageState(storageState));
      Page page3 = context3.newPage();
      checkCookies.accept(page3);
    }
    soft.finish();
  }

  @PlaywrightTest
  @DisplayName("CO-056 add 'Partitioned;' cookie via API")
  @Fixme(value = "java: the Cookie class has no _crHasCrossSiteAncestor property, so a partitioned cookie from cookies() or added with addCookies() is read in the wrong frame context: expected 'Received cookie: frame-non-partitioned=value; top-level-non-partitioned=value' but was '...; top-level-partitioned=value'", browsers = BrowserName.CHROMIUM)
  void addPartitionedCookieViaAPI(Page page, BrowserContext context, BrowserName browserName, HttpsTestServer httpsServer, WebkitPartitions webkitPartitionsFixture, TestUrls urls) {
    boolean isMac = Env.isMac;
    boolean isBidi = Env.isBidi();
    boolean webkitPartitions = webkitPartitionsFixture.value;
    addCommonCookieHandlers(httpsServer, urls);

    // Java: the Cookie class has no _crHasCrossSiteAncestor property.
    context.addCookies(Arrays.asList(
      new Cookie("top-level-partitioned", "value")
        .setDomain(httpsServer.HOSTNAME)
        .setPath("/")
        .setExpires(-1)
        .setHttpOnly(false)
        .setSecure(true)
        .setSameSite(SameSiteAttribute.NONE)
        .setPartitionKey("https://localhost"),
      new Cookie("top-level-non-partitioned", "value")
        .setDomain(httpsServer.HOSTNAME)
        .setPath("/")
        .setExpires(-1)
        .setHttpOnly(false)
        .setSecure(true)
        .setSameSite(SameSiteAttribute.NONE),
      new Cookie("frame-partitioned", "value")
        .setDomain(httpsServer.HOSTNAME)
        .setPath("/")
        .setExpires(-1)
        .setHttpOnly(false)
        .setSecure(true)
        .setSameSite(SameSiteAttribute.NONE)
        .setPartitionKey("https://127.0.0.1"),
      new Cookie("frame-non-partitioned", "value")
        .setDomain(httpsServer.HOSTNAME)
        .setPath("/")
        .setExpires(-1)
        .setHttpOnly(false)
        .setSecure(true)
        .setSameSite(SameSiteAttribute.NONE)));

    Soft soft = new Soft();
    java.util.function.Consumer<Page> checkCookies = page1 -> {
      {
        // Check top-level cookie first.
        page1.navigate(urls.read_origin1);
        String expectedTopLevel;
        if (browserName == BrowserName.WEBKIT && isMac && !webkitPartitions)
          expectedTopLevel = "Received cookie: frame-non-partitioned=value; top-level-non-partitioned=value";
        else if ((browserName == BrowserName.WEBKIT && !webkitPartitions) || (browserName == BrowserName.FIREFOX && !isBidi))
          expectedTopLevel = "Received cookie: frame-non-partitioned=value; frame-partitioned=value; top-level-non-partitioned=value; top-level-partitioned=value";
        else
          expectedTopLevel = "Received cookie: frame-non-partitioned=value; top-level-non-partitioned=value; top-level-partitioned=value";
        String expected = expectedTopLevel;
        soft.run(() -> assertEquals(expected, page1.locator("body").textContent()));
      }
      {
        // Check third-party cookie.
        page1.navigate(urls.read_origin2_origin1);
        Locator frameBody = page1.locator("iframe").contentFrame().locator("body");
        String expectedThirdParty = "Received cookie: ";
        if (webkitPartitions || (browserName == BrowserName.FIREFOX && isBidi))
          expectedThirdParty += "frame-partitioned=value";
        else if (browserName == BrowserName.WEBKIT && isMac)
          expectedThirdParty += "undefined";
        else if (browserName == BrowserName.CHROMIUM)
          expectedThirdParty += "frame-non-partitioned=value; frame-partitioned=value; top-level-non-partitioned=value";
        else
          // Firefox(Juggler) and WebKit on Linux/Windows do not partition third-party cookies.
          expectedThirdParty += "frame-non-partitioned=value; frame-partitioned=value; top-level-non-partitioned=value; top-level-partitioned=value";
        String expected = expectedThirdParty;
        soft.run(() -> assertThat(frameBody).hasText(expected, timeout(1000)));
      }
      {
        page1.navigate(urls.read_origin1_origin2_origin1); // read-origin1-origin2-origin1.html
        Locator frameBody = page1.locator("iframe").contentFrame().locator("iframe").contentFrame().locator("body");
        String expectedThirdParty = "Received cookie: ";
        if (webkitPartitions)
          expectedThirdParty += "frame-non-partitioned=value; top-level-non-partitioned=value; top-level-partitioned=value";
        else if (browserName == BrowserName.WEBKIT && isMac)
          expectedThirdParty += "frame-non-partitioned=value; top-level-non-partitioned=value";
        else if (browserName == BrowserName.WEBKIT || (browserName == BrowserName.FIREFOX && !isBidi))
          expectedThirdParty += "frame-non-partitioned=value; frame-partitioned=value; top-level-non-partitioned=value; top-level-partitioned=value";
        else if (browserName == BrowserName.FIREFOX && isBidi)
          expectedThirdParty += "undefined";
        else
          expectedThirdParty += "frame-non-partitioned=value; top-level-non-partitioned=value";
        String expected = expectedThirdParty;
        soft.run(() -> assertThat(frameBody).hasText(expected, timeout(1000)));
      }
    };

    checkCookies.accept(page);
    soft.finish();
  }

  @PlaywrightTest
  @DisplayName("CO-057 same origin third party 'Partitioned;' cookie with different origin intermediate iframe")
  void sameOriginThirdPartyPartitionedCookieWithDifferentOriginIntermediateIframe(Page page, HttpsTestServer httpsServer, Browser browser, ContextFactory contextFactory, TestUrls urls) {
    addCommonCookieHandlers(httpsServer, urls);
    httpsServer.setRoute("/set-cookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList(
        (req.headers.get("referer") != null ? "frame" : "top-level") + "-partitioned=value; SameSite=None; Path=/; Secure; Partitioned;",
        (req.headers.get("referer") != null ? "frame" : "top-level") + "-non-partitioned=value; SameSite=None; Path=/; Secure;"));
      res.end();
    });
    // main frame: origin1 -> iframe1: origin2 -> iframe2: origin1
    // In this case the cookie in iframe2 is a third-party partitioned cookie, even though
    // it's the same origin as the main frame.
    page.navigate(urls.set_origin1_origin2_origin1);

    Soft soft = new Soft();
    java.util.function.Consumer<Page> checkCookies = page1 -> {
      page1.navigate(urls.read_origin1_origin2_origin1);
      Locator frameBody = page1.locator("iframe").contentFrame().locator("iframe").contentFrame().locator("body");
      soft.run(() -> assertThat(frameBody).hasText("Received cookie: frame-non-partitioned=value; frame-partitioned=value"));
    };

    checkCookies.accept(page);

    // it.step('export via cookies/addCookies')
    {
      List<Cookie> cookies = page.context().cookies();
      // Java: browser.newContext() gets the test.use() options upstream.
      BrowserContext context2 = contextFactory.newContext();
      context2.addCookies(cookies);
      Page page2 = context2.newPage();
      checkCookies.accept(page2);
    }

    // it.step('export via storageState')
    {
      String storageState = page.context().storageState();
      // Java: browser.newContext() gets the test.use() options upstream.
      BrowserContext context3 = contextFactory.newContext(new Browser.NewContextOptions().setIgnoreHTTPSErrors(true).setStorageState(storageState));
      Page page3 = context3.newPage();
      checkCookies.accept(page3);
    }
    soft.finish();
  }

  @PlaywrightTest
  @DisplayName("CO-058 top level 'Partitioned;' cookie and same origin iframe")
  @Fixme(value = "chromium: java: the Cookie class has no _crHasCrossSiteAncestor property, so the cookies() / addCookies() round trip gives 'Received cookie: top-level-non-partitioned=value' instead of '...; top-level=value'; firefox: driver: firefox r1555 of driver 1.64.1 does not send the top-level 'Partitioned' cookie to the nested iframe (expects r1564, upstream #43494): 'Received cookie: top-level-non-partitioned=value'", browsers = {BrowserName.CHROMIUM, BrowserName.FIREFOX})
  void topLevelPartitionedCookieAndSameOriginIframe(Page page, BrowserName browserName, HttpsTestServer httpsServer, Browser browser, ContextFactory contextFactory, TestUrls urls) {
    boolean isBidi = Env.isBidi();
    addCommonCookieHandlers(httpsServer, urls);
    httpsServer.setRoute("/set-cookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList(
        (req.headers.get("referer") != null ? "frame" : "top-level") + "=value; SameSite=None; Path=/; Secure; Partitioned;",
        (req.headers.get("referer") != null ? "frame" : "top-level") + "-non-partitioned=value; SameSite=None; Path=/; Secure;"));
      res.end();
    });

    // Same origin iframe cookies are partitioned the same way as top-level cookies.
    page.navigate(urls.set_origin1);
    page.context().storageState(new BrowserContext.StorageStateOptions().setPath(Paths.get("/tmp/state2.json")));

    Soft soft = new Soft();
    java.util.function.Consumer<Page> checkCookies = page1 -> {
      {
        // Check top-level cookie first.
        page1.navigate(urls.read_origin1);
        soft.run(() -> assertEquals("Received cookie: top-level-non-partitioned=value; top-level=value", page1.locator("body").textContent()));
      }
      {
        // Same origin iframe.
        page1.navigate(urls.read_origin1_origin1);
        Locator frameBody = page1.locator("iframe").contentFrame().locator("body");
        soft.run(() -> assertThat(frameBody).hasText("Received cookie: top-level-non-partitioned=value; top-level=value", timeout(1000)));
      }
      {
        // Check third-party cookie.
        // main frame: origin1 -> iframe1: origin2 -> iframe2: origin1
        page1.navigate(urls.read_origin1_origin2_origin1);
        Locator frameBody = page1.locator("iframe").contentFrame().locator("iframe").contentFrame().locator("body");
        String expectedThirdParty = "Received cookie: ";
        if (browserName == BrowserName.CHROMIUM)
          expectedThirdParty += "top-level-non-partitioned=value";
        else if (browserName == BrowserName.FIREFOX && isBidi)
          expectedThirdParty += "undefined";
        else
          expectedThirdParty += "top-level-non-partitioned=value; top-level=value";
        String expected = expectedThirdParty;
        soft.run(() -> assertThat(frameBody).hasText(expected, timeout(1000)));
      }
    };

    checkCookies.accept(page);

    // it.step('export via cookies/addCookies')
    {
      List<Cookie> cookies = page.context().cookies();
      // Java: browser.newContext() gets the test.use() options upstream.
      BrowserContext context2 = contextFactory.newContext();
      context2.addCookies(cookies);
      Page page2 = context2.newPage();
      checkCookies.accept(page2);
    }

    // it.step('export via storageState')
    {
      String storageState = page.context().storageState();
      // Java: browser.newContext() gets the test.use() options upstream.
      BrowserContext context3 = contextFactory.newContext(new Browser.NewContextOptions().setIgnoreHTTPSErrors(true).setStorageState(storageState));
      Page page3 = context3.newPage();
      checkCookies.accept(page3);
    }
    soft.finish();
  }

  @PlaywrightTest
  @DisplayName("CO-059 should be able to send third party cookies via an iframe")
  void shouldBeAbleToSendThirdPartyCookiesViaAnIframe(Browser browser, HttpsTestServer httpsServer, BrowserName browserName) throws Exception {
    boolean isMac = Env.isMac;
    boolean isBidi = Env.isBidi();
    assumeFalse(browserName == BrowserName.WEBKIT && isMac, "fixme: ");
    assumeFalse(browserName == BrowserName.FIREFOX && isBidi, "Firefox's Total Cookie Protection blocks this third-party cookie");
    // test.info().annotations.push({ type: 'issue', description: 'https://github.com/microsoft/playwright/issues/16937' });

    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setIgnoreHTTPSErrors(true));
    try {
      Page page = context.newPage();
      page.navigate(httpsServer.EMPTY_PAGE);
      context.addCookies(Arrays.asList(
        new Cookie("cookie1", "yes")
          .setDomain(URI.create(httpsServer.CROSS_PROCESS_PREFIX).getHost())
          .setPath("/")
          .setHttpOnly(true)
          .setSecure(true)
          .setSameSite(SameSiteAttribute.NONE)));
      CompletableFuture<TestServer.Request> responsePromise = httpsServer.waitForRequest("/grid.html");
      page.setContent("<iframe src=\"" + httpsServer.CROSS_PROCESS_PREFIX + "/grid.html\"></iframe>");
      TestServer.Request response = responsePromise.get();
      assertEquals("cookie1=yes", response.headers.get("cookie"));
    } finally {
      context.close();
    }
  }

  @PlaywrightTest
  @DisplayName("CO-060 should(not) block third party cookies - persistent context")
  void shouldNotBlockThirdPartyCookiesPersistentContext(HttpsTestServer httpsServer, LaunchPersistent launchPersistent, BrowserName browserName) {
    // Java: launchPersistentContext() gets the test.use() options upstream.
    LaunchPersistent.Result result = launchPersistent.launch(Env.launchPersistentContextOptions().setIgnoreHTTPSErrors(true));
    Page page = result.page;
    BrowserContext context = result.context;
    testThirdPartyCookiesAreBlocked(page, context, httpsServer, allowsThirdParty(browserName), defaultSameSiteCookieValue(browserName));
  }

  @PlaywrightTest
  @DisplayName("CO-061 should(not) block third party cookies - ephemeral context")
  void shouldNotBlockThirdPartyCookiesEphemeralContext(Page page, BrowserContext context, HttpsTestServer httpsServer, BrowserName browserName) {
    testThirdPartyCookiesAreBlocked(page, context, httpsServer, allowsThirdParty(browserName), defaultSameSiteCookieValue(browserName));
  }

  private static void testThirdPartyCookiesAreBlocked(Page page, BrowserContext context, TestServer server, boolean allowsThirdParty, SameSiteAttribute defaultSameSiteCookieValue) {
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("src => {\n" +
      "    let fulfill;\n" +
      "    const promise = new Promise(x => fulfill = x);\n" +
      "    const iframe = document.createElement('iframe');\n" +
      "    document.body.appendChild(iframe);\n" +
      "    iframe.onload = fulfill;\n" +
      "    iframe.src = src;\n" +
      "    return promise;\n" +
      "  }", server.CROSS_PROCESS_PREFIX + "/grid.html");
    Object documentCookie = page.frames().get(1).evaluate("() => {\n" +
      "    document.cookie = 'username=John Doe';\n" +
      "    return document.cookie;\n" +
      "  }");
    page.waitForTimeout(2000);
    assertEquals(allowsThirdParty ? "username=John Doe" : "", documentCookie);
    List<Cookie> cookies = context.cookies(server.CROSS_PROCESS_PREFIX + "/grid.html");
    if (allowsThirdParty) {
      assertEquals(1, cookies.size());
      Cookie cookie = cookies.get(0);
      assertEquals("127.0.0.1", cookie.domain);
      assertEquals(Double.valueOf(-1), cookie.expires);
      assertEquals(false, cookie.httpOnly);
      assertEquals("username", cookie.name);
      assertEquals("/", cookie.path);
      assertEquals(defaultSameSiteCookieValue, cookie.sameSite);
      assertEquals(false, cookie.secure);
      assertEquals("John Doe", cookie.value);
      assertNull(cookie.partitionKey);
    } else {
      assertEquals(Collections.emptyList(), cookies);
    }
  }

  @PlaywrightTest
  @DisplayName("CO-062 should not block third party SameSite=None cookies")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "No third party cookies in WebKit")
  void shouldNotBlockThirdPartySameSiteNoneCookies(ContextFactory contextFactory, HttpsTestServer httpsServer, BrowserName browserName) throws Exception {
    assumeFalse("frozen".equals(System.getenv("PW_CLOCK")));
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions()
      .setIgnoreHTTPSErrors(true));
    Page page = context.newPage();

    httpsServer.setRoute("/empty.html", (req, res) -> {
      res.writeHead(200, "Content-Type", "text/html");
      res.end("<iframe src=\"" + httpsServer.CROSS_PROCESS_PREFIX + "/grid.html\"></iframe>");
    });

    httpsServer.setRoute("/grid.html", (req, res) -> {
      res.setHeader("Set-Cookie", Arrays.asList("a=b; Path=/; Max-Age=3600; SameSite=None; Secure"));
      res.setHeader("Content-Type", "text/html");
      res.writeHead(200);
      res.end("Hello world\n" +
        "    <script>\n" +
        "    setTimeout(() => fetch('/json'), 1000);\n" +
        "    </script>");
    });

    CompletableFuture<String> cookie = new CompletableFuture<>();
    httpsServer.setRoute("/json", (req, res) -> {
      cookie.complete(req.headers.get("cookie"));
      res.end();
    });

    page.navigate(httpsServer.EMPTY_PAGE);
    assertEquals("a=b", cookie.get());
  }
}
