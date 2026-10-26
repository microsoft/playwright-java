/**
 * Copyright 2018 Google Inc. All rights reserved.
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

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.EnabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixture;
import com.microsoft.playwright.fixtures.HttpsTestServer;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.condition.OS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.microsoft.playwright.fixtures.Utils.browserMajorVersion;
import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/permissions.spec.ts. */
public class TestPermissions {
  private static Object getPermission(Page page, String name) {
    return page.evaluate("name => navigator.permissions.query({ name }).then(result => result.state)", name);
  }

  private static Object getPermission(Frame frame, String name) {
    return frame.evaluate("name => navigator.permissions.query({ name }).then(result => result.state)", name);
  }

  @Nested
  @DisabledOnBrowser(value = BrowserName.WEBKIT, os = OS.WINDOWS, reason = "fixme: Permissions API is disabled on Windows WebKit")
  class Permissions {
    @PlaywrightTest
    @DisplayName("PE-001 should be prompt by default")
    void shouldBePromptByDefault(Page page, TestServer server) {
      page.navigate(server.EMPTY_PAGE);
      assertEquals("prompt", getPermission(page, "geolocation"));
    }

    @PlaywrightTest
    @DisplayName("PE-002 should deny permission when not listed")
    void shouldDenyPermissionWhenNotListed(Page page, BrowserContext context, TestServer server, BrowserName browserName) {
      page.navigate(server.EMPTY_PAGE);
      context.grantPermissions(Collections.emptyList(), new BrowserContext.GrantPermissionsOptions().setOrigin(server.EMPTY_PAGE));
      if (browserName == BrowserName.WEBKIT) {
        assertEquals("prompt", getPermission(page, "geolocation"));
        // Since https://github.com/WebKit/WebKit/pull/45470 WebKit only returns actual
        // permission value, if the API has been accessed.
        page.evaluate("() => navigator.geolocation.getCurrentPosition(() => { })");
        assertEquals("denied", getPermission(page, "geolocation"));
      } else if (Env.isBidi()) {
        assertEquals("prompt", getPermission(page, "geolocation"));
      } else {
        assertEquals("denied", getPermission(page, "geolocation"));
      }
    }

    @PlaywrightTest
    @DisplayName("PE-003 should fail when bad permission is given")
    void shouldFailWhenBadPermissionIsGiven(Page page, BrowserContext context, TestServer server) {
      page.navigate(server.EMPTY_PAGE);
      PlaywrightException error = assertThrows(PlaywrightException.class,
        () -> context.grantPermissions(Arrays.asList("foo"), new BrowserContext.GrantPermissionsOptions().setOrigin(server.EMPTY_PAGE)));
      if (Env.isBidi())
        assertTrue(error.getMessage().contains("Protocol error (permissions.setPermission): invalid argument"));
      else
        assertTrue(error.getMessage().contains("Unknown permission: foo"));
    }

    @PlaywrightTest
    @DisplayName("PE-004 should grant geolocation permission when origin is listed")
    void shouldGrantGeolocationPermissionWhenOriginIsListed(Page page, BrowserContext context, TestServer server) {
      page.navigate(server.EMPTY_PAGE);
      context.grantPermissions(Arrays.asList("geolocation"), new BrowserContext.GrantPermissionsOptions().setOrigin(server.EMPTY_PAGE));
      assertEquals("granted", getPermission(page, "geolocation"));
    }

    @PlaywrightTest
    @DisplayName("PE-005 should prompt for geolocation permission when origin is not listed")
    void shouldPromptForGeolocationPermissionWhenOriginIsNotListed(Page page, BrowserContext context, TestServer server) {
      page.navigate(server.EMPTY_PAGE);
      context.grantPermissions(Arrays.asList("geolocation"), new BrowserContext.GrantPermissionsOptions().setOrigin(server.EMPTY_PAGE));
      page.navigate(server.CROSS_PROCESS_PREFIX + "/empty.html");
      assertEquals("prompt", getPermission(page, "geolocation"));
    }

    @PlaywrightTest
    @DisplayName("PE-006 should grant notifications permission when listed")
    void shouldGrantNotificationsPermissionWhenListed(Page page, BrowserContext context, TestServer server) {
      page.navigate(server.EMPTY_PAGE);
      context.grantPermissions(Arrays.asList("notifications"), new BrowserContext.GrantPermissionsOptions().setOrigin(server.EMPTY_PAGE));
      assertEquals("granted", getPermission(page, "notifications"));
    }

    @PlaywrightTest
    @DisplayName("PE-007 should accumulate when adding")
    void shouldAccumulateWhenAdding(Page page, BrowserContext context, TestServer server) {
      page.navigate(server.EMPTY_PAGE);
      context.grantPermissions(Arrays.asList("geolocation"));
      context.grantPermissions(Arrays.asList("notifications"));
      assertEquals("granted", getPermission(page, "geolocation"));
      assertEquals("granted", getPermission(page, "notifications"));
    }

    @PlaywrightTest
    @DisplayName("PE-008 should clear permissions")
    void shouldClearPermissions(Page page, BrowserContext context, TestServer server) {
      page.navigate(server.EMPTY_PAGE);
      context.grantPermissions(Arrays.asList("geolocation"));
      context.clearPermissions();
      context.grantPermissions(Arrays.asList("notifications"));
      assertNotEquals("granted", getPermission(page, "geolocation"));
      assertEquals("granted", getPermission(page, "notifications"));
    }

    @PlaywrightTest
    @DisplayName("PE-009 should grant permission when listed for all domains")
    void shouldGrantPermissionWhenListedForAllDomains(Page page, BrowserContext context, TestServer server) {
      page.navigate(server.EMPTY_PAGE);
      context.grantPermissions(Arrays.asList("geolocation"));
      assertEquals("granted", getPermission(page, "geolocation"));
    }

    @PlaywrightTest
    @DisplayName("PE-010 should grant permission when creating context")
    void shouldGrantPermissionWhenCreatingContext(TestServer server, Browser browser) {
      BrowserContext context = browser.newContext(new Browser.NewContextOptions().setPermissions(Arrays.asList("geolocation")));
      Page page = context.newPage();
      page.navigate(server.EMPTY_PAGE);
      assertEquals("granted", getPermission(page, "geolocation"));
      context.close();
    }

    @PlaywrightTest
    @DisplayName("PE-011 should reset permissions")
    void shouldResetPermissions(Page page, BrowserContext context, TestServer server) {
      page.navigate(server.EMPTY_PAGE);
      context.grantPermissions(Arrays.asList("geolocation"), new BrowserContext.GrantPermissionsOptions().setOrigin(server.EMPTY_PAGE));
      assertEquals("granted", getPermission(page, "geolocation"));
      context.clearPermissions();
      assertEquals("prompt", getPermission(page, "geolocation"));
    }

    @PlaywrightTest
    @DisplayName("PE-012 should trigger permission onchange")
    @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "fail:")
    void shouldTriggerPermissionOnchange(Page page, BrowserContext context, TestServer server, BrowserName browserName) {
      page.navigate(server.EMPTY_PAGE);
      page.evaluate("() => {\n" +
        "  window['events'] = [];\n" +
        "  return navigator.permissions.query({ name: 'geolocation' }).then(function(result) {\n" +
        "    window['events'].push(result.state);\n" +
        "    result.onchange = function() {\n" +
        "      window['events'].push(result.state);\n" +
        "    };\n" +
        "  });\n" +
        "}");
      List<Object> expectedEvents = new ArrayList<>(Arrays.asList("prompt"));
      assertEquals(expectedEvents, page.evaluate("() => window['events']"));
      context.grantPermissions(Collections.emptyList(), new BrowserContext.GrantPermissionsOptions().setOrigin(server.EMPTY_PAGE));
      if (!Env.isBidi())
        expectedEvents.add("denied");
      assertEquals(expectedEvents, page.evaluate("() => window['events']"));
      context.grantPermissions(Arrays.asList("geolocation"), new BrowserContext.GrantPermissionsOptions().setOrigin(server.EMPTY_PAGE));
      expectedEvents.add("granted");
      assertEquals(expectedEvents, page.evaluate("() => window['events']"));
      context.clearPermissions();
      expectedEvents.add("prompt");
      assertEquals(expectedEvents, page.evaluate("() => window['events']"));
    }

    @PlaywrightTest
    @DisplayName("PE-013 should isolate permissions between browser contexts")
    void shouldIsolatePermissionsBetweenBrowserContexts(TestServer server, Browser browser, BrowserName browserName) {
      BrowserContext context = browser.newContext();
      Page page = context.newPage();
      page.navigate(server.EMPTY_PAGE);
      BrowserContext otherContext = browser.newContext();
      Page otherPage = otherContext.newPage();
      otherPage.navigate(server.EMPTY_PAGE);
      assertEquals("prompt", getPermission(page, "geolocation"));
      assertEquals("prompt", getPermission(otherPage, "geolocation"));

      context.grantPermissions(Collections.emptyList(), new BrowserContext.GrantPermissionsOptions().setOrigin(server.EMPTY_PAGE));
      otherContext.grantPermissions(Arrays.asList("geolocation"), new BrowserContext.GrantPermissionsOptions().setOrigin(server.EMPTY_PAGE));
      if (browserName == BrowserName.WEBKIT) {
        assertEquals("prompt", getPermission(page, "geolocation"));
        // Since https://github.com/WebKit/WebKit/pull/45470 WebKit only returns actual
        // permission value, if the API has been accessed.
        page.evaluate("() => navigator.geolocation.getCurrentPosition(() => { })");
        assertEquals("denied", getPermission(page, "geolocation"));
      } else if (Env.isBidi()) {
        assertEquals("prompt", getPermission(page, "geolocation"));
      } else {
        assertEquals("denied", getPermission(page, "geolocation"));
      }
      assertEquals("granted", getPermission(otherPage, "geolocation"));

      context.clearPermissions();
      if (browserName == BrowserName.WEBKIT) {
        // Since https://github.com/WebKit/WebKit/pull/45470 WebKit returns the cached
        // permission value, if the geolocation API has been accessed.
        // TODO: We can probably reset the cached state in the Web Process when resetting
        // permissions.
        assertEquals("denied", getPermission(page, "geolocation"));

        // Geolocation API in the new page in the same context has not been accessed yet,
        // so the permission status should be prompt.
        Page page2 = context.newPage();
        page2.navigate(server.EMPTY_PAGE);
        assertEquals("prompt", getPermission(page2, "geolocation"));
        page2.close();
      } else {
        assertEquals("prompt", getPermission(page, "geolocation"));
      }
      assertEquals("granted", getPermission(otherPage, "geolocation"));
      otherContext.close();
      context.close();
    }
  }

  @PlaywrightTest
  @DisplayName("PE-014 should support clipboard read")
  @DisabledOnBrowser(value = BrowserName.FIREFOX, reason = "fail: No such permissions (requires flag) in Firefox")
  void shouldSupportClipboardRead(Page page, BrowserContext context, TestServer server, BrowserName browserName) {
    // issue: https://github.com/microsoft/playwright/issues/27475
    page.navigate(server.EMPTY_PAGE);
    // There is no 'clipboard-read' permission in WebKit Web API.
    if (browserName != BrowserName.WEBKIT)
      assertEquals("prompt", getPermission(page, "clipboard-read"));

    if (Env.isHeadlessShell(browserName)) {
      // Chromium (but not headless-shell) shows a dialog and does not resolve the promise.
      PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.evaluate("() => navigator.clipboard.readText()"));
      assertTrue(error.toString().contains("denied"));
    }

    context.grantPermissions(Arrays.asList("clipboard-read"));
    if (browserName != BrowserName.WEBKIT)
      assertEquals("granted", getPermission(page, "clipboard-read"));
    // There is no 'clipboard-write' permission in WebKit Web API.
    if (browserName == BrowserName.CHROMIUM)
      context.grantPermissions(Arrays.asList("clipboard-write"));
    page.evaluate("() => navigator.clipboard.writeText('test content')");
    assertEquals("test content", page.evaluate("() => navigator.clipboard.readText()"));
  }

  @PlaywrightTest
  @DisplayName("PE-015 should isolate the headless clipboard from the operating system")
  void shouldIsolateTheHeadlessClipboardFromTheOperatingSystem(BrowserType browserType, TestServer server, BrowserName browserName) {
    // issue: https://github.com/microsoft/playwright/issues/13097

    // Each headless browser gets its own clipboard, so neither can see the clipboard of the operating system nor of another browser.
    Browser browser1 = browserType.launch(Env.launchOptions().setHeadless(true));
    Browser browser2 = browserType.launch(Env.launchOptions().setHeadless(true));
    List<Page> pages = new ArrayList<>();
    // Java: the browsers are handled one after another.
    for (Browser browser : Arrays.asList(browser1, browser2)) {
      BrowserContext context = browser.newContext();
      // There is no 'clipboard-write' permission in WebKit Web API and no clipboard permission at all in Firefox.
      if (browserName == BrowserName.CHROMIUM)
        context.grantPermissions(Arrays.asList("clipboard-read", "clipboard-write"));
      else if (browserName == BrowserName.WEBKIT)
        context.grantPermissions(Arrays.asList("clipboard-read"));
      Page page = context.newPage();
      page.navigate(server.EMPTY_PAGE);
      pages.add(page);
    }
    Page page1 = pages.get(0);
    Page page2 = pages.get(1);

    page1.evaluate("() => navigator.clipboard.writeText('first')");
    assertEquals("first", page1.evaluate("() => navigator.clipboard.readText()"));

    page2.evaluate("() => navigator.clipboard.writeText('second')");
    assertEquals("second", page2.evaluate("() => navigator.clipboard.readText()"));

    assertEquals("first", page1.evaluate("() => navigator.clipboard.readText()"));

    browser1.close();
    browser2.close();
  }

  @PlaywrightTest
  @DisplayName("PE-016 storage access")
  @EnabledOnBrowser(value = BrowserName.CHROMIUM, reason = "chromium-only api")
  void storageAccess(Page page, BrowserContext context, TestServer server, BrowserName browserName) {
    // issue: https://github.com/microsoft/playwright/issues/31227
    context.grantPermissions(Arrays.asList("storage-access"));
    assertEquals("granted", getPermission(page, "storage-access"));
    server.setRoute("/set-cookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", "name=value; Path=/; SameSite=Strict; Secure");
      res.end();
    });
    server.setRoute("/my-frame.html", (req, res) -> {
      res.setHeader("Content-type", "text/html");
      res.end("<iframe src=\"" + (server.CROSS_PROCESS_PREFIX + "/empty.html") + "\"></iframe>");
    });

    // Navigate once to the domain as top level.
    page.navigate(server.CROSS_PROCESS_PREFIX + "/set-cookie.html");
    page.navigate(server.PREFIX + "/my-frame.html");

    Frame frame = page.frames().get(1);
    assertEquals("granted", getPermission(frame, "storage-access"));
    Object access = frame.evaluate("() => document.requestStorageAccess().then(() => true, () => false)");
    assertEquals(true, access);
    assertEquals(true, frame.evaluate("() => document.hasStorageAccess()"));
  }

  @Nested
  class Describe {
    // Secure context
    @Fixture
    Browser.NewContextOptions contextOptions(Browser.NewContextOptions defaults) {
      return defaults.setIgnoreHTTPSErrors(true);
    }

    @PlaywrightTest
    @DisplayName("PE-017 should be able to use the local-fonts API")
    @EnabledOnBrowser(value = BrowserName.CHROMIUM, reason = "chromium-only api")
    void shouldBeAbleToUseTheLocalFontsAPI(Page page, BrowserContext context, HttpsTestServer httpsServer, BrowserName browserName) {
      assumeFalse(Env.channel() != null && Env.channel().startsWith("msedge"), "always times out in edge");
      assumeFalse(Env.headed(), "fixme: times out in headed");
      // issue: https://github.com/microsoft/playwright/issues/36113

      page.navigate(httpsServer.EMPTY_PAGE);
      assertEquals("prompt", getPermission(page, "local-fonts"));
      context.grantPermissions(Arrays.asList("local-fonts"));
      assertEquals("granted", getPermission(page, "local-fonts"));
      assertEquals(true, page.evaluate("async () => (await window.queryLocalFonts()).length > 0"));
    }
  }

  @PlaywrightTest
  @DisplayName("PE-018 local network request is allowed from public origin")
  @DisabledOnBrowser(BrowserName.WEBKIT)
  void localNetworkRequestIsAllowedFromPublicOrigin(Page page, BrowserContext context, TestServer server, BrowserName browserName, Browser browser) {
    // issue: https://github.com/microsoft/playwright/issues/37861
    assumeFalse(browserName == BrowserName.CHROMIUM && browserMajorVersion(browser) < 145, "local-network-access permission support has changed between versions");

    if (browserName == BrowserName.CHROMIUM || browserName == BrowserName.FIREFOX)
      context.grantPermissions(Arrays.asList("local-network-access"));
    List<String> serverRequests = new CopyOnWriteArrayList<>();
    server.setRoute("/cors", (req, res) -> {
      serverRequests.add(req.method + " " + req.url);
      if (req.method.equals("OPTIONS")) {
        res.writeHead(204,
          "Access-Control-Allow-Origin", "*",
          "Access-Control-Allow-Methods", "GET, POST, PUT, OPTIONS",
          "Access-Control-Allow-Headers", "*");
        res.end();
        return;
      }
      res.writeHead(200, "Content-type", "text/plain", "Access-Control-Allow-Origin", "*");
      res.end("Hello there!");
    });
    List<String> clientRequests = new ArrayList<>();
    // Has to be a public origin.
    page.navigate("https://demo.playwright.dev/todomvc/");
    page.onRequest(request -> {
      clientRequests.add(request.method() + " " + request.url());
    });
    Object response;
    try {
      response = page.evaluate("async url => {\n" +
        "  const response = await fetch(url, {\n" +
        "    method: 'POST',\n" +
        "    body: '',\n" +
        "    headers: {\n" +
        "      'Content-Type': 'application/json',\n" +
        "      'X-Custom-Header': 'test-value'\n" +
        "    }\n" +
        "  });\n" +
        "  return await response.text();\n" +
        "}", server.CROSS_PROCESS_PREFIX + "/cors");
    } catch (PlaywrightException e) {
      response = e.getMessage();
    }
    assertEquals("Hello there!", response);
    assertEquals(Arrays.asList(
      "OPTIONS /cors",
      "POST /cors"), serverRequests);
    assertEquals(Arrays.asList(
      "POST " + server.CROSS_PROCESS_PREFIX + "/cors"), clientRequests);
  }

  @PlaywrightTest
  @DisplayName("PE-019 can request screen-wake-lock")
  void canRequestScreenWakeLock(Page page, BrowserContext context) {
    // issue: https://github.com/microsoft/playwright/issues/39115
    context.grantPermissions(Arrays.asList("screen-wake-lock"));
    page.route("**/*", route -> route.fulfill(new com.microsoft.playwright.Route.FulfillOptions()
      .setStatus(200).setBody("<div>Hello there!</div>").setContentType("text/html")));
    page.navigate("https://example.com");
    page.evaluate("() => navigator.wakeLock.request('screen')");
  }

  @Nested
  @EnabledOnBrowser(value = BrowserName.WEBKIT, reason = "WebKit mock-capture-device based test")
  class CameraAndMicrophone {
    // WebKit exposes deterministic mock capture devices; capture is gated on the
    // 'camera'/'microphone' permissions. Chromium needs --use-fake-device-for-media-stream
    // and Firefox has no camera/microphone permission mapping, so scope to WebKit.
    @BeforeEach
    void skipWindows() {
      // Native Windows WebKit is built without a media-capture backend, so getUserMedia is
      // unavailable there; WSL runs the Linux build, which does have mock capture.
      assumeFalse(Env.isWindows && !"webkit-wsl".equals(Env.channel()), "WebKit has no media capture on Windows");
    }

    private Object getUserMedia(Page page, Object constraints) {
      return page.evaluate("async constraints => {\n" +
        "  try {\n" +
        "    const stream = await navigator.mediaDevices.getUserMedia(constraints);\n" +
        "    const tracks = stream.getTracks().map(track => ({ kind: track.kind, live: track.readyState === 'live' }));\n" +
        "    stream.getTracks().forEach(track => track.stop());\n" +
        "    tracks.sort((a, b) => a.kind.localeCompare(b.kind));\n" +
        "    return { tracks };\n" +
        "  } catch (error) {\n" +
        "    return { error: error.name };\n" +
        "  }\n" +
        "}", constraints);
    }

    @PlaywrightTest
    @DisplayName("PE-020 should capture when camera and microphone are granted")
    void shouldCaptureWhenCameraAndMicrophoneAreGranted(Page page, BrowserContext context, TestServer server) {
      // issue: https://github.com/microsoft/playwright/issues/2973
      context.grantPermissions(Arrays.asList("camera", "microphone"), new BrowserContext.GrantPermissionsOptions().setOrigin(server.PREFIX));
      page.navigate(server.EMPTY_PAGE);
      assertEquals(mapOf("tracks", Arrays.asList(
        mapOf("kind", "audio", "live", true),
        mapOf("kind", "video", "live", true))), getUserMedia(page, mapOf("video", true, "audio", true)));
    }

    @PlaywrightTest
    @DisplayName("PE-021 should reject when no permission is granted")
    void shouldRejectWhenNoPermissionIsGranted(Page page, TestServer server) {
      // issue: https://github.com/microsoft/playwright/issues/2973
      page.navigate(server.EMPTY_PAGE);
      assertEquals(mapOf("error", "NotAllowedError"), getUserMedia(page, mapOf("video", true, "audio", true)));
    }

    @PlaywrightTest
    @DisplayName("PE-022 should gate audio and video independently")
    void shouldGateAudioAndVideoIndependently(Page page, BrowserContext context, TestServer server) {
      // issue: https://github.com/microsoft/playwright/issues/2973
      context.grantPermissions(Arrays.asList("camera"), new BrowserContext.GrantPermissionsOptions().setOrigin(server.PREFIX));
      page.navigate(server.EMPTY_PAGE);
      // Camera-only request is allowed.
      assertEquals(mapOf("tracks", Arrays.asList(mapOf("kind", "video", "live", true))), getUserMedia(page, mapOf("video", true)));
      // Microphone request is rejected because it was not granted.
      assertEquals(mapOf("error", "NotAllowedError"), getUserMedia(page, mapOf("audio", true)));
    }

    @PlaywrightTest
    @DisplayName("PE-023 should stop capturing after permissions are cleared")
    void shouldStopCapturingAfterPermissionsAreCleared(Page page, BrowserContext context, TestServer server) {
      // issue: https://github.com/microsoft/playwright/issues/2973
      context.grantPermissions(Arrays.asList("camera"), new BrowserContext.GrantPermissionsOptions().setOrigin(server.PREFIX));
      page.navigate(server.EMPTY_PAGE);
      assertEquals(mapOf("tracks", Arrays.asList(mapOf("kind", "video", "live", true))), getUserMedia(page, mapOf("video", true)));
      context.clearPermissions();
      assertEquals(mapOf("error", "NotAllowedError"), getUserMedia(page, mapOf("video", true)));
    }
  }
}
