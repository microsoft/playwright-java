/*
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

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.FileChooser;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.ContextFactory;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.EnabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.HttpsTestServer;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.BoundingBox;
import org.junit.jupiter.api.DisplayName;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.asset;
import static com.microsoft.playwright.fixtures.Utils.async;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/capabilities.spec.ts. */
public class TestCapabilities {
  @PlaywrightTest
  @DisplayName("CA-001 SharedArrayBuffer should work @smoke")
  void sharedArrayBufferShouldWork(ContextFactory contextFactory, HttpsTestServer httpsServer) {
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions().setIgnoreHTTPSErrors(true));
    Page page = context.newPage();
    httpsServer.setRoute("/sharedarraybuffer", (req, res) -> {
      res.setHeader("Cross-Origin-Opener-Policy", "same-origin");
      res.setHeader("Cross-Origin-Embedder-Policy", "require-corp");
      // Note: without 'onload', Firefox sometimes does not fire the load event
      // over the protocol. The reason is unclear.
      res.end("\n" +
        "      <div>Hello there!</div>\n" +
        "      <script>window.onload = () => console.log('onload')</script>\n" +
        "    ");
    });
    page.navigate(httpsServer.PREFIX + "/sharedarraybuffer");
    assertEquals("function", page.evaluate("() => typeof SharedArrayBuffer"));
  }

  @PlaywrightTest
  @DisplayName("CA-002 Web Assembly should work @smoke")
  void webAssemblyShouldWork(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/wasm/table2.html");
    assertEquals("42, 83", page.evaluate("loadTable()"));
  }

  @PlaywrightTest
  @DisplayName("CA-003 WebSocket should work @smoke")
  void webSocketShouldWork(Page page, TestServer server) {
    server.sendOnWebSocketConnection("incoming");
    Object value = page.evaluate("host => {\n" +
      "  let cb;\n" +
      "  const result = new Promise(f => cb = f);\n" +
      "  const ws = new WebSocket('ws://' + host + '/ws');\n" +
      "  ws.addEventListener('message', data => { ws.close(); cb(data.data); });\n" +
      "  ws.addEventListener('error', error => cb('Error'));\n" +
      "  return result;\n" +
      "}", server.HOST);
    assertEquals("incoming", value);
  }

  @PlaywrightTest
  @DisplayName("CA-004 should respect CSP @smoke")
  void shouldRespectCSP(Page page, TestServer server) {
    server.setRoute("/empty.html", (req, res) -> {
      res.setHeader("Content-Security-Policy", "script-src 'unsafe-inline';");
      res.end("\n" +
        "      <script>\n" +
        "        window.testStatus = 'SUCCESS';\n" +
        "        window.testStatus = eval(\"'FAILED'\");\n" +
        "      </script>");
    });

    page.navigate(server.EMPTY_PAGE);
    assertEquals("SUCCESS", page.evaluate("() => window['testStatus']"));
  }

  @PlaywrightTest
  @DisplayName("CA-005 should play video @smoke")
  void shouldPlayVideo(Page page, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.WEBKIT && Env.isWindows, "passes locally but fails on GitHub Action bot, apparently due to a Media Pack issue in the Windows Server");

    // Safari only plays mp4 so we test WebKit with an .mp4 clip.
    String fileName = browserName == BrowserName.WEBKIT ? "video_mp4.html" : "video.html";
    Path absolutePath = asset(fileName);
    // Our test server doesn't support range requests required to play on Mac,
    // so we load the page using a file url.
    // Java: url.pathToFileURL(absolutePath).href
    page.navigate(absolutePath.toUri().toString());
    page.evalOnSelector("video", "v => v.play()");
    page.evalOnSelector("video", "v => v.pause()");
  }

  @PlaywrightTest
  @DisplayName("CA-006 should play webm video @smoke")
  void shouldPlayWebmVideo(Page page, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.WEBKIT && Env.platform().equals("win32"), "not supported");

    Path absolutePath = asset("video_webm.html");
    // Our test server doesn't support range requests required to play on Mac,
    // so we load the page using a file url.
    // Java: url.pathToFileURL(absolutePath).href
    page.navigate(absolutePath.toUri().toString());
    page.evalOnSelector("video", "v => v.play()");
    page.evalOnSelector("video", "v => v.pause()");
  }

  @PlaywrightTest
  @DisplayName("CA-007 should play audio @smoke")
  void shouldPlayAudio(Page page, TestServer server, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.WEBKIT && Env.platform().equals("win32"), "fixme: https://github.com/microsoft/playwright/issues/10892");
    assumeFalse(browserName == BrowserName.WEBKIT && Env.platform().equals("darwin") && Env.macVersion() == 15, "audio output is unreliable on hosted macOS 15 runners");

    page.navigate(server.EMPTY_PAGE);
    page.setContent("<audio src=\"" + server.PREFIX + "/example.mp3\"></audio>");
    page.evalOnSelector("audio", "e => e.play()");
    page.waitForTimeout(3000);
    page.evalOnSelector("audio", "e => e.pause()");
    assertTrue(((Number) page.evalOnSelector("audio", "e => e.currentTime")).doubleValue() > 0.1);
  }

  @PlaywrightTest
  @DisplayName("CA-008 should support webgl @smoke")
  void shouldSupportWebgl(Page page) {
    Object hasWebGL = page.evaluate("() => {\n" +
      "  const canvas = document.createElement('canvas');\n" +
      "  return !!canvas.getContext('webgl');\n" +
      "}");
    assertEquals(true, hasWebGL);
  }

  @PlaywrightTest
  @DisplayName("CA-009 should support webgl 2 @smoke")
  void shouldSupportWebgl2(Page page, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.FIREFOX && Env.isWindows, "fixme");

    Object hasWebGL2 = page.evaluate("() => {\n" +
      "  const canvas = document.createElement('canvas');\n" +
      "  return !!canvas.getContext('webgl2');\n" +
      "}");
    assertEquals(true, hasWebGL2);
  }

  @PlaywrightTest
  @DisplayName("CA-010 should not crash on page with mp4 @smoke")
  void shouldNotCrashOnPageWithMp4(Page page, TestServer server, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.WEBKIT && Env.platform().equals("win32"), "fixme: https://github.com/microsoft/playwright/issues/11009, times out in setContent");
    page.setContent("<video><source src=\"" + server.PREFIX + "/movie.mp4\"/></video>");
    page.waitForTimeout(1000);
  }

  @PlaywrightTest
  @DisplayName("CA-011 should not crash on showDirectoryPicker")
  @EnabledOnBrowser(value = BrowserName.CHROMIUM, reason = "showDirectoryPicker is only available in Chromium")
  void shouldNotCrashOnShowDirectoryPicker(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    // "User activation is required to show a file picker." - so we click first.
    page.locator("body").click();
    // Java: the evaluate promise that nobody awaits runs in async().
    async(() -> {
      try {
        return page.evaluate("async () => {\n" +
          "  const dir = await window.showDirectoryPicker();\n" +
          "  return dir.name;\n" +
          "  // In headless it throws (aborted), in headed it stalls (Test ended) and waits for the picker to be accepted.\n" +
          "}");
      } catch (PlaywrightException e) {
        assertTrue(Pattern.compile("((DOMException|AbortError): .*The user aborted a request|Test ended)").matcher(e.getMessage()).find());
        return null;
      }
    });
    // The dialog will not be accepted, so we just wait for some time to
    // to give the browser a chance to crash.
    page.waitForTimeout(3000);
  }

  @PlaywrightTest
  @DisplayName("CA-012 should not crash on storage.getDirectory()")
  void shouldNotCrashOnStorageGetDirectory(Page page, TestServer server, BrowserName browserName) {
    page.navigate(server.EMPTY_PAGE);
    // Java: stands for evaluate(...).catch(e => e); the result is the directory name or the PlaywrightException.
    Object error;
    try {
      error = page.evaluate("async () => {\n" +
        "  const dir = await navigator.storage.getDirectory();\n" +
        "  return dir.name;\n" +
        "}");
    } catch (PlaywrightException e) {
      error = e;
    }
    if (browserName == BrowserName.WEBKIT) {
      assertTrue(((PlaywrightException) error).getMessage().contains("UnknownError: The operation failed for an unknown transient reason"));
    } else {
      // Java: toBeFalsy() of the empty name of the root directory.
      assertTrue(error == null || "".equals(error));
    }
  }

  @PlaywrightTest
  @DisplayName("CA-013 navigator.clipboard should be present")
  void navigatorClipboardShouldBePresent(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    assertNotNull(page.evaluate("() => navigator.clipboard"));
  }

  @PlaywrightTest
  @DisplayName("CA-014 should set CloseEvent.wasClean to false when the server terminates a WebSocket connection")
  void shouldSetCloseEventWasCleanToFalseWhenTheServerTerminatesAWebSocketConnection(Page page, TestServer server) {
    server.onceWebSocketConnection(socket -> {
      socket.terminate();
    });
    Object wasClean = page.evaluate("port => new Promise(resolve => {\n" +
      "  const ws = new WebSocket('ws://localhost:' + port + '/ws');\n" +
      "  ws.addEventListener('close', error => resolve(error.wasClean));\n" +
      "})", server.PORT);
    assertEquals(false, wasClean);
  }

  @PlaywrightTest
  @DisplayName("CA-015 serviceWorker should intercept document request")
  void serviceWorkerShouldInterceptDocumentRequest(Page page, TestServer server) {
    server.setRoute("/sw.js", (req, res) -> {
      res.setHeader("Content-Type", "application/javascript");
      res.end("\n" +
        "      self.addEventListener('fetch', event => {\n" +
        "        event.respondWith(new Response('intercepted'));\n" +
        "      });\n" +
        "      self.addEventListener('activate', event => {\n" +
        "        event.waitUntil(clients.claim());\n" +
        "      });\n" +
        "    ");
    });
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("async () => {\n" +
      "  await navigator.serviceWorker.register('/sw.js');\n" +
      "  await new Promise(resolve => navigator.serviceWorker.oncontrollerchange = resolve);\n" +
      "}");
    page.reload();
    assertEquals("intercepted", page.textContent("body"));
  }

  @PlaywrightTest
  @DisplayName("CA-016 webkit should define window.safari")
  @EnabledOnBrowser(BrowserName.WEBKIT)
  void webkitShouldDefineWindowSafari(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Object defined = page.evaluate("() => !!window.safari");
    assertEquals(true, defined);
    assertEquals("object", page.evaluate("() => typeof window.safari.pushNotification"));
    assertEquals("[object SafariRemoteNotification]", page.evaluate("() => window.safari.pushNotification.toString()"));
  }

  @PlaywrightTest
  @DisplayName("CA-017 make sure that XMLHttpRequest upload events are emitted correctly")
  void makeSureThatXMLHttpRequestUploadEventsAreEmittedCorrectly(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Object events = page.evaluate("async () => {\n" +
      "  const events = [];\n" +
      "  const xhr = new XMLHttpRequest();\n" +
      "  xhr.upload.addEventListener('loadstart', () => events.push('loadstart'));\n" +
      "  xhr.upload.addEventListener('progress', () => events.push('progress'));\n" +
      "  xhr.upload.addEventListener('load', () => events.push('load'));\n" +
      "  xhr.upload.addEventListener('loadend', () => events.push('loadend'));\n" +
      "  xhr.open('POST', '/simple.json');\n" +
      "  xhr.send('hello');\n" +
      "  await new Promise(f => xhr.onload = f);\n" +
      "  return events;\n" +
      "}");
    assertEquals(Arrays.asList("loadstart", "progress", "load", "loadend"), events);
  }

  @PlaywrightTest
  @DisplayName("CA-018 loading in HTMLImageElement.prototype")
  void loadingInHTMLImageElementPrototype(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Object defined = page.evaluate("() => 'loading' in HTMLImageElement.prototype");
    assertEquals(true, defined);
  }

  @PlaywrightTest
  @DisplayName("CA-019 window.GestureEvent in WebKit")
  void windowGestureEventInWebKit(Page page, TestServer server, BrowserName browserName) {
    page.navigate(server.EMPTY_PAGE);
    Object defined = page.evaluate("() => 'GestureEvent' in window");
    assertEquals(browserName == BrowserName.WEBKIT, defined);
    Object type = page.evaluate("() => typeof window.GestureEvent");
    assertEquals(browserName == BrowserName.WEBKIT ? "function" : "undefined", type);
  }

  @PlaywrightTest
  @DisplayName("CA-020 requestFullscreen")
  @DisabledOnBrowser(value = BrowserName.FIREFOX, headed = true, reason = "fixme: Error: Request for fullscreen was denied because requesting element is not in the currently focused tab")
  void requestFullscreen(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("() => {\n" +
      "  const result = new Promise(resolve => document.addEventListener('fullscreenchange', resolve));\n" +
      "  void document.documentElement.requestFullscreen().then(() => console.log('success')).catch(e => console.log(e));\n" +
      "  return result;\n" +
      "}");
    assertEquals(true, page.evaluate("() => document.fullscreenElement === document.documentElement"));
    page.evaluate("() => {\n" +
      "  const result = new Promise(resolve => document.addEventListener('fullscreenchange', resolve));\n" +
      "  void document.exitFullscreen().then(() => console.log('success')).catch(e => console.log(e));\n" +
      "  return result;\n" +
      "}");
    assertEquals(false, page.evaluate("() => !!document.fullscreenElement"));
  }

  @PlaywrightTest
  @DisplayName("CA-021 should send no Content-Length header for GET requests with a Content-Type")
  void shouldSendNoContentLengthHeaderForGETRequestsWithAContentType(Page page, TestServer server) throws Exception {
    page.navigate(server.EMPTY_PAGE);
    CompletableFuture<TestServer.Request> requestPromise = server.waitForRequest("/empty.html");
    page.evaluate("() => fetch('/empty.html', {\n" +
      "  'headers': { 'Content-Type': 'application/json' },\n" +
      "  'method': 'GET'\n" +
      "})");
    TestServer.Request request = requestPromise.get();
    assertNull(request.headers.get("content-length"));
  }

  @PlaywrightTest
  @DisplayName("CA-022 Intl.ListFormat should work")
  void intlListFormatShouldWork(Page page, TestServer server, BrowserName browserName) {
    page.navigate(server.EMPTY_PAGE);
    Object formatted = page.evaluate("() => {\n" +
      "  const data = ['first', 'second', 'third'];\n" +
      "  const listFormat = new Intl.ListFormat('en', {\n" +
      "    type: 'disjunction',\n" +
      "    style: 'short',\n" +
      "  });\n" +
      "  return listFormat.format(data);\n" +
      "}");
    assertEquals("first, second, or third", formatted);
  }

  @PlaywrightTest
  @DisplayName("CA-023 service worker should cover the iframe")
  void serviceWorkerShouldCoverTheIframe(Page page, TestServer server) {
    server.setRoute("/sw.html", (req, res) -> {
      res.writeHead(200, "content-type", "text/html");
      res.end("\n" +
        "      <script>\n" +
        "        window.registrationPromise = navigator.serviceWorker.register('sw.js');\n" +
        "        window.activationPromise = new Promise(resolve => navigator.serviceWorker.oncontrollerchange = resolve);\n" +
        "      </script>\n" +
        "    ");
    });

    server.setRoute("/iframe.html", (req, res) -> {
      res.writeHead(200, "content-type", "text/html");
      res.end("<div>from the server</div>");
    });

    server.setRoute("/sw.js", (req, res) -> {
      res.writeHead(200, "content-type", "application/javascript");
      res.end("\n" +
        "      const kIframeHtml = \"<div>from the service worker</div>\";\n" +
        "\n" +
        "      self.addEventListener('fetch', event => {\n" +
        "        if (event.request.url.endsWith('iframe.html')) {\n" +
        "          const blob = new Blob([kIframeHtml], { type: 'text/html' });\n" +
        "          const response = new Response(blob, { status: 200 , statusText: 'OK' });\n" +
        "          event.respondWith(response);\n" +
        "          return;\n" +
        "        }\n" +
        "        event.respondWith(fetch(event.request));\n" +
        "      });\n" +
        "\n" +
        "      self.addEventListener('activate', event => {\n" +
        "        event.waitUntil(clients.claim());\n" +
        "      });\n" +
        "    ");
    });

    page.navigate(server.PREFIX + "/sw.html");
    page.evaluate("() => window['activationPromise']");

    page.evaluate("() => {\n" +
      "  const iframe = document.createElement('iframe');\n" +
      "  iframe.src = '/iframe.html';\n" +
      "  document.body.appendChild(iframe);\n" +
      "}");

    assertThat(page.frameLocator("iframe").locator("div")).hasText("from the service worker");
  }

  @PlaywrightTest
  @DisplayName("CA-024 service worker should register in an iframe")
  void serviceWorkerShouldRegisterInAnIframe(Page page, TestServer server) {
    server.setRoute("/main.html", (req, res) -> {
      res.writeHead(200, "content-type", "text/html");
      res.end("\n" +
        "      <iframe src='/dir/iframe.html'></iframe>\n" +
        "    ");
    });

    server.setRoute("/dir/iframe.html", (req, res) -> {
      res.writeHead(200, "content-type", "text/html");
      res.end("\n" +
        "      <script>\n" +
        "        window.registrationPromise = navigator.serviceWorker.register('sw.js');\n" +
        "        window.activationPromise = new Promise(resolve => navigator.serviceWorker.oncontrollerchange = resolve);\n" +
        "      </script>\n" +
        "    ");
    });

    server.setRoute("/dir/sw.js", (req, res) -> {
      res.writeHead(200, "content-type", "application/javascript");
      res.end("\n" +
        "      const kIframeHtml = \"<div>from the service worker</div>\";\n" +
        "\n" +
        "      self.addEventListener('fetch', event => {\n" +
        "        if (event.request.url.endsWith('html')) {\n" +
        "          event.respondWith(fetch(event.request));\n" +
        "          return;\n" +
        "        }\n" +
        "        const blob = new Blob(['responseFromServiceWorker'], { type: 'text/plain' });\n" +
        "        const response = new Response(blob, { status: 200 , statusText: 'OK' });\n" +
        "        event.respondWith(response);\n" +
        "      });\n" +
        "\n" +
        "      self.addEventListener('activate', event => {\n" +
        "        event.waitUntil(clients.claim());\n" +
        "      });\n" +
        "    ");
    });

    page.navigate(server.PREFIX + "/main.html");
    Frame iframe = page.frames().get(1);
    iframe.evaluate("() => window['activationPromise']");

    Object response = iframe.evaluate("async () => {\n" +
      "  const response = await fetch('foo.txt');\n" +
      "  return response.text();\n" +
      "}");
    assertEquals("responseFromServiceWorker", response);
  }

  @PlaywrightTest
  @DisplayName("CA-025 should be able to render avif images")
  void shouldBeAbleToRenderAvifImages(Page page, TestServer server, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.WEBKIT && Env.platform().equals("win32"));
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<img src=\"" + server.PREFIX + "/rgb.avif\" onerror=\"window.error = true\">");
    // Java: expect.poll(() => page.locator('img').boundingBox()).toEqual(expect.objectContaining({ width: 128, height: 128 })); waitForCondition() polls it.
    try {
      page.waitForCondition(() -> {
        BoundingBox box = page.locator("img").boundingBox();
        return box != null && box.width == 128 && box.height == 128;
      }, new Page.WaitForConditionOptions().setTimeout(5000));
    } catch (TimeoutError e) {
      // The assertions below report the last value.
    }
    BoundingBox box = page.locator("img").boundingBox();
    assertEquals(128.0, box.width);
    assertEquals(128.0, box.height);
    assertNull(page.evaluate("() => window.error"));
  }

  @PlaywrightTest
  @DisplayName("CA-026 should not crash when clicking a label with a <input type=\"file\"/>")
  void shouldNotCrashWhenClickingALabelWithAInputTypeFile(Page page) {
    page.setContent("\n" +
      "    <form>\n" +
      "      <label>\n" +
      "        A second file\n" +
      "        <input type=\"file\" />\n" +
      "      </label>\n" +
      "    </form>\n" +
      "  ");
    FileChooser fileChooser = page.waitForFileChooser(() -> page.getByText("A second file").click());
    assertEquals(page, fileChooser.page());
  }

  @PlaywrightTest
  @DisplayName("CA-027 should not crash when clicking a color input")
  void shouldNotCrashWhenClickingAColorInput(Page page) {
    page.setContent("<input type=\"color\">");
    Locator input = page.locator("input");

    assertThat(input).isVisible();
    input.click();
    assertThat(input).isVisible();
  }

  @PlaywrightTest
  @DisplayName("CA-028 should not auto play audio")
  void shouldNotAutoPlayAudio(Page page, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.WEBKIT && Env.isWindows, "audio does not play at all");
    assumeFalse("frozen".equals(System.getenv("PW_CLOCK")), "no way to inject real setTimeout");
    page.route("**/*", route -> {
      route.fulfill(new Route.FulfillOptions()
        .setStatus(200)
        .setContentType("text/html")
        .setBody("\n" +
          "      <script>\n" +
          "        async function onLoad() {\n" +
          "          const log = document.getElementById('log');\n" +
          "          const audioContext = new AudioContext();\n" +
          "          const gainNode = new GainNode(audioContext);\n" +
          "          gainNode.connect(audioContext.destination);\n" +
          "          gainNode.gain.value = 0.025;\n" +
          "          const sineNode = new OscillatorNode(audioContext);\n" +
          "          sineNode.connect(gainNode);\n" +
          "          sineNode.start();\n" +
          "          await new Promise((resolve) => setTimeout(resolve, 1000));\n" +
          "          log.innerHTML = 'State: ' + audioContext.state;\n" +
          "        }\n" +
          "      </script>\n" +
          "      <body onload=\"onLoad()\">\n" +
          "      <div id=\"log\"></div>\n" +
          "      </body>"));
    });
    page.navigate("http://127.0.0.1/audio.html");
    if (browserName == BrowserName.WEBKIT) {
      assertThat(page.locator("#log")).hasText(Pattern.compile("State: (interrupted|suspended)"));
    } else {
      assertThat(page.locator("#log")).hasText("State: suspended");
    }
  }

  @PlaywrightTest
  @DisplayName("CA-029 should not crash on feature detection for PublicKeyCredential")
  void shouldNotCrashOnFeatureDetectionForPublicKeyCredential(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("async () => {\n" +
      "  await PublicKeyCredential.getClientCapabilities();\n" +
      "  await PublicKeyCredential.isConditionalMediationAvailable();\n" +
      "  await PublicKeyCredential.isUserVerifyingPlatformAuthenticatorAvailable();\n" +
      "}");
  }
}
