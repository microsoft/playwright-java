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

package com.microsoft.playwright.tests.library.chromium;

import com.google.gson.JsonObject;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.CDPSession;
import com.microsoft.playwright.ConsoleMessage;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.FileChooser;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.Worker;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.EnabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixture;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.Scope;
import com.microsoft.playwright.fixtures.TestInfo;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.ColorScheme;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.attachFrame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/chromium/oopif.spec.ts. */
@EnabledOnBrowser(value = BrowserName.CHROMIUM, reason = "tests/library/chromium runs in the chromium project only")
public class TestOopif {
  @Fixture(scope = Scope.WORKER)
  static BrowserType.LaunchOptions launchOptions() {
    return Env.launchOptions().setArgs(Arrays.asList("--site-per-process"));
  }

  @PlaywrightTest
  @DisplayName("OO-001 should report oopif frames")
  void shouldReportOopifFrames(Page page, Browser browser, TestServer server) {
    page.navigate(server.PREFIX + "/dynamic-oopif.html");
    assertOOPIFCount(browser, 1);
    assertEquals(2, page.frames().size());
    assertEquals(server.CROSS_PROCESS_PREFIX + "/grid.html", page.frames().get(1).evaluate("() => '' + location.href"));
  }

  @PlaywrightTest
  @DisplayName("OO-002 should handle oopif detach")
  void shouldHandleOopifDetach(Page page, Browser browser, TestServer server) {
    page.navigate(server.PREFIX + "/dynamic-oopif.html");
    assertOOPIFCount(browser, 1);
    assertEquals(2, page.frames().size());
    Frame frame = page.frames().get(1);
    assertEquals(server.CROSS_PROCESS_PREFIX + "/grid.html", frame.evaluate("() => '' + location.href"));
    // Java: there is no page.waitForFrameDetached, a handler completes a future.
    CompletableFuture<Frame> detachedFrame = new CompletableFuture<>();
    page.onFrameDetached(detachedFrame::complete);
    page.evaluate("() => document.querySelector('iframe').remove()");
    page.waitForCondition(detachedFrame::isDone);
    assertSame(frame, detachedFrame.getNow(null));
  }

  @PlaywrightTest
  @DisplayName("OO-003 should remove workers of a detached oopif")
  void shouldRemoveWorkersOfADetachedOopif(Page page, Browser browser, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Worker worker = page.waitForWorker(() -> {
      attachFrame(page, "frame1", server.CROSS_PROCESS_PREFIX + "/worker/worker.html");
    });
    assertOOPIFCount(browser, 1);
    assertEquals(1, page.workers().size());
    worker.waitForClose(() -> {
      page.navigate(server.PREFIX + "/title.html");
    });
    assertEquals(0, page.workers().size());
  }

  @PlaywrightTest
  @DisplayName("OO-004 should not hang in unrouteAll when oopif worker is gone")
  void shouldNotHangInUnrouteAllWhenOopifWorkerIsGone(Page page, BrowserContext context, Browser browser, TestServer server) {
    // issue: https://github.com/microsoft/playwright/issues/42278
    context.route("**/*", route -> route.resume());
    page.navigate(server.EMPTY_PAGE);
    page.waitForWorker(() -> {
      attachFrame(page, "frame1", server.CROSS_PROCESS_PREFIX + "/worker/worker.html");
    });
    assertOOPIFCount(browser, 1);
    page.navigate(server.CROSS_PROCESS_PREFIX + "/title.html");
    context.unrouteAll();
  }

  @PlaywrightTest
  @DisplayName("OO-005 should handle remote -> local -> remote transitions")
  void shouldHandleRemoteLocalRemoteTransitions(Page page, Browser browser, TestServer server) {
    page.navigate(server.PREFIX + "/dynamic-oopif.html");
    assertEquals(2, page.frames().size());
    assertOOPIFCount(browser, 1);
    assertEquals(server.CROSS_PROCESS_PREFIX + "/grid.html", page.frames().get(1).evaluate("() => '' + location.href"));
    page.frames().get(1).waitForNavigation(() -> {
      page.evaluate("goLocal()");
    });
    assertEquals(server.PREFIX + "/grid.html", page.frames().get(1).evaluate("() => '' + location.href"));
    assertOOPIFCount(browser, 0);
    page.frames().get(1).waitForNavigation(() -> {
      page.evaluate("goRemote()");
    });
    assertEquals(server.CROSS_PROCESS_PREFIX + "/grid.html", page.frames().get(1).evaluate("() => '' + location.href"));
    assertOOPIFCount(browser, 1);
  }

  @PlaywrightTest
  @DisplayName("OO-006 should get the proper viewport")
  void shouldGetTheProperViewport(Page page, Browser browser, TestServer server) {
    assumeFalse(true, "fixme: it.fixme() in the upstream file");
    assertEquals(1280, page.viewportSize().width);
    assertEquals(720, page.viewportSize().height);
    page.navigate(server.PREFIX + "/dynamic-oopif.html");
    assertEquals(2, page.frames().size());
    assertOOPIFCount(browser, 1);
    Frame oopif = page.frames().get(1);
    assertEquals(1280, oopif.evaluate("() => screen.width"));
    assertEquals(720, oopif.evaluate("() => screen.height"));
    assertEquals(true, oopif.evaluate("() => matchMedia('(device-width: 1280px)').matches"));
    assertEquals(true, oopif.evaluate("() => matchMedia('(device-height: 720px)').matches"));
    assertEquals(false, oopif.evaluate("() => 'ontouchstart' in window"));
    page.setViewportSize(123, 456);
    assertEquals(123, oopif.evaluate("() => screen.width"));
    assertEquals(456, oopif.evaluate("() => screen.height"));
    assertEquals(true, oopif.evaluate("() => matchMedia('(device-width: 123px)').matches"));
    assertEquals(true, oopif.evaluate("() => matchMedia('(device-height: 456px)').matches"));
    assertEquals(false, oopif.evaluate("() => 'ontouchstart' in window"));
  }

  @PlaywrightTest
  @DisplayName("OO-007 should expose function")
  void shouldExposeFunction(Page page, Browser browser, TestServer server) {
    page.navigate(server.PREFIX + "/dynamic-oopif.html");
    assertEquals(2, page.frames().size());
    assertOOPIFCount(browser, 1);
    Frame oopif = page.frames().get(1);
    page.exposeFunction("mul", args -> ((Number) args[0]).intValue() * ((Number) args[1]).intValue());
    Object result = oopif.evaluate("async function() {\n" +
      "  return await window['mul'](9, 4);\n" +
      "}");
    assertEquals(36, result);
  }

  @PlaywrightTest
  @DisplayName("OO-008 should emulate media")
  void shouldEmulateMedia(Page page, Browser browser, TestServer server) {
    page.navigate(server.PREFIX + "/dynamic-oopif.html");
    assertEquals(2, page.frames().size());
    assertOOPIFCount(browser, 1);
    Frame oopif = page.frames().get(1);
    assertEquals(false, oopif.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches"));
    page.emulateMedia(new Page.EmulateMediaOptions().setColorScheme(ColorScheme.DARK));
    assertEquals(true, oopif.evaluate("() => matchMedia('(prefers-color-scheme: dark)').matches"));
  }

  @PlaywrightTest
  @DisplayName("OO-009 should emulate offline")
  void shouldEmulateOffline(Page page, Browser browser, TestServer server) {
    page.navigate(server.PREFIX + "/dynamic-oopif.html");
    assertEquals(2, page.frames().size());
    assertOOPIFCount(browser, 1);
    Frame oopif = page.frames().get(1);
    assertEquals(true, oopif.evaluate("() => navigator.onLine"));
    page.context().setOffline(true);
    assertEquals(false, oopif.evaluate("() => navigator.onLine"));
  }

  @PlaywrightTest
  @DisplayName("OO-011 should respect route")
  void shouldRespectRoute(Page page, Browser browser, TestServer server) {
    boolean[] intercepted = {false};
    page.route("**/digits/0.png", route -> {
      intercepted[0] = true;
      route.resume();
    });
    page.navigate(server.PREFIX + "/dynamic-oopif.html");
    assertEquals(2, page.frames().size());
    assertOOPIFCount(browser, 1);
    assertTrue(intercepted[0]);
  }

  @PlaywrightTest
  @DisplayName("OO-012 should take screenshot")
  void shouldTakeScreenshot(Page page, Browser browser, TestServer server, TestInfo testInfo) {
    page.setViewportSize(500, 500);
    page.navigate(server.PREFIX + "/dynamic-oopif.html");
    assertEquals(2, page.frames().size());
    assertOOPIFCount(browser, 1);
    testInfo.expectToMatchSnapshot(page.screenshot(), "screenshot-oopif.png");
  }

  @PlaywrightTest
  @DisplayName("OO-013 should load oopif iframes with subresources and route")
  void shouldLoadOopifIframesWithSubresourcesAndRoute(Page page, Browser browser, TestServer server) {
    page.route("**/*", route -> route.resume());
    page.navigate(server.PREFIX + "/dynamic-oopif.html");
    assertOOPIFCount(browser, 1);
  }

  @PlaywrightTest
  @DisplayName("OO-014 should report main requests")
  void shouldReportMainRequests(Page page, Browser browser, TestServer server) {
    List<Frame> requestFrames = new ArrayList<>();
    page.onRequest(r -> requestFrames.add(r.frame()));
    List<Frame> finishedFrames = new ArrayList<>();
    page.onRequestFinished(r -> finishedFrames.add(r.frame()));

    page.navigate(server.PREFIX + "/empty.html");
    Frame main = page.mainFrame();

    main.evaluate("url => {\n" +
      "  const iframe = document.createElement('iframe');\n" +
      "  iframe.src = url;\n" +
      "  document.body.appendChild(iframe);\n" +
      "  return new Promise(f => iframe.onload = f);\n" +
      "}", server.CROSS_PROCESS_PREFIX + "/empty.html");
    assertEquals(2, page.frames().size());
    Frame child = main.childFrames().get(0);
    child.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);

    child.evaluate("url => {\n" +
      "  const iframe = document.createElement('iframe');\n" +
      "  iframe.src = url;\n" +
      "  document.body.appendChild(iframe);\n" +
      "  return new Promise(f => iframe.onload = f);\n" +
      "}", server.PREFIX + "/empty.html");
    assertEquals(3, page.frames().size());
    Frame grandChild = child.childFrames().get(0);
    grandChild.waitForLoadState(com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED);

    assertOOPIFCount(browser, 2);
    assertSame(main, requestFrames.get(0));
    assertSame(main, finishedFrames.get(0));
    assertSame(child, requestFrames.get(1));
    assertSame(child, finishedFrames.get(1));
    assertSame(grandChild, requestFrames.get(2));
    assertSame(grandChild, finishedFrames.get(2));
  }

  @PlaywrightTest
  @DisplayName("OO-015 should support exposeFunction")
  void shouldSupportExposeFunction(Page page, Browser browser, TestServer server) {
    page.context().exposeFunction("dec", args -> ((Number) args[0]).intValue() - 1);
    page.exposeFunction("inc", args -> ((Number) args[0]).intValue() + 1);
    page.navigate(server.PREFIX + "/dynamic-oopif.html");
    assertOOPIFCount(browser, 1);
    assertEquals(2, page.frames().size());
    assertEquals(4, page.frames().get(0).evaluate("() => window['inc'](3)"));
    assertEquals(5, page.frames().get(1).evaluate("() => window['inc'](4)"));
    assertEquals(2, page.frames().get(0).evaluate("() => window['dec'](3)"));
    assertEquals(3, page.frames().get(1).evaluate("() => window['dec'](4)"));
  }

  @PlaywrightTest
  @DisplayName("OO-016 should support addInitScript")
  void shouldSupportAddInitScript(Page page, Browser browser, TestServer server) {
    page.context().addInitScript("window['bar'] = 17");
    page.addInitScript("window['foo'] = 42");
    page.navigate(server.PREFIX + "/dynamic-oopif.html");
    assertOOPIFCount(browser, 1);
    assertEquals(2, page.frames().size());
    assertEquals(42, page.frames().get(0).evaluate("() => window['foo']"));
    assertEquals(42, page.frames().get(1).evaluate("() => window['foo']"));
    assertEquals(17, page.frames().get(0).evaluate("() => window['bar']"));
    assertEquals(17, page.frames().get(1).evaluate("() => window['bar']"));
  }

  // @see https://github.com/microsoft/playwright/issues/1240
  @PlaywrightTest
  @DisplayName("OO-017 should click a button when it overlays oopif")
  void shouldClickAButtonWhenItOverlaysOopif(Page page, Browser browser, TestServer server) {
    page.navigate(server.PREFIX + "/button-overlay-oopif.html");
    assertOOPIFCount(browser, 1);
    page.click("button");
    assertEquals(true, page.evaluate("() => window['BUTTON_CLICKED']"));
  }

  @PlaywrightTest
  @DisplayName("OO-018 should report google.com frame with headed")
  void shouldReportGoogleComFrameWithHeaded(BrowserType browserType, TestServer server) {
    assumeFalse("chromium-headless-shell".equals(Env.channel()), "Headless Shell does not support headed mode");
    // @see https://github.com/GoogleChrome/puppeteer/issues/2548
    // https://google.com is isolated by default in Chromium embedder.
    Browser browser = browserType.launch(Env.launchOptions().setHeadless(false));
    Page page = browser.newPage();
    page.navigate(server.EMPTY_PAGE);
    page.route("**/*", route -> {
      route.fulfill(new com.microsoft.playwright.Route.FulfillOptions().setBody("YO, GOOGLE.COM"));
    });
    page.evaluate("() => {\n" +
      "  const frame = document.createElement('iframe');\n" +
      "  frame.setAttribute('src', 'https://google.com/');\n" +
      "  document.body.appendChild(frame);\n" +
      "  return new Promise(x => frame.onload = x);\n" +
      "}");
    page.waitForSelector("iframe[src=\"https://google.com/\"]");
    assertOOPIFCount(browser, 1);
    List<String> urls = new ArrayList<>();
    for (Frame frame : page.frames()) {
      urls.add(frame.url());
    }
    assertEquals(Arrays.asList(
      server.EMPTY_PAGE,
      "https://google.com/"), urls);
    browser.close();
  }

  @PlaywrightTest
  @DisplayName("OO-019 ElementHandle.boundingBox() should work")
  void elementHandleBoundingBoxShouldWork(Page page, Browser browser, TestServer server) {
    page.navigate(server.PREFIX + "/dynamic-oopif.html");
    page.evalOnSelector("iframe", "iframe => {\n" +
      "  iframe.style.width = '520px';\n" +
      "  iframe.style.height = '520px';\n" +
      "  iframe.style.marginLeft = '42px';\n" +
      "  iframe.style.marginTop = '17px';\n" +
      "}");
    page.frames().get(1).navigate(page.frames().get(1).url());

    assertOOPIFCount(browser, 1);
    ElementHandle handle1 = page.frames().get(1).querySelector(".box:nth-of-type(13)");
    pollBoundingBox(() -> handle1.boundingBox(), 100 + 42, 50 + 17, 50, 50);

    page.frames().get(1).waitForNavigation(() -> {
      page.evaluate("goLocal()");
    });
    assertOOPIFCount(browser, 0);
    ElementHandle handle2 = page.frames().get(1).querySelector(".box:nth-of-type(13)");
    pollBoundingBox(() -> handle2.boundingBox(), 100 + 42, 50 + 17, 50, 50);
  }

  @PlaywrightTest
  @DisplayName("OO-020 should click")
  void shouldClick(Page page, Browser browser, TestServer server) {
    page.navigate(server.PREFIX + "/dynamic-oopif.html");
    page.evalOnSelector("iframe", "iframe => {\n" +
      "  iframe.style.width = '500px';\n" +
      "  iframe.style.height = '500px';\n" +
      "  iframe.style.marginLeft = '102px';\n" +
      "  iframe.style.marginTop = '117px';\n" +
      "}");
    page.frames().get(1).navigate(page.frames().get(1).url());

    assertOOPIFCount(browser, 1);
    ElementHandle handle1 = page.frames().get(1).querySelector(".box:nth-of-type(13)");
    handle1.evaluate("div => div.addEventListener('click', () => window['_clicked'] = true, false)");
    handle1.click();
    assertEquals(true, handle1.evaluate("() => window['_clicked']"));
  }

  @PlaywrightTest
  @DisplayName("OO-021 contentFrame should work")
  void contentFrameShouldWork(Page page, Browser browser, TestServer server) {
    page.navigate(server.PREFIX + "/dynamic-oopif.html");
    assertEquals(2, page.frames().size());
    assertOOPIFCount(browser, 1);
    assertEquals(200, page.locator("iframe").contentFrame().locator("div").count());
    ElementHandle oopif = page.querySelector("iframe");
    Frame content = oopif.contentFrame();
    assertEquals(200, content.locator("div").count());
  }

  @PlaywrightTest
  @DisplayName("OO-022 should allow cdp sessions on oopifs")
  void shouldAllowCdpSessionsOnOopifs(Page page, Browser browser, TestServer server) {
    page.navigate(server.PREFIX + "/dynamic-oopif.html");
    assertOOPIFCount(browser, 1);
    assertEquals(2, page.frames().size());
    assertEquals(server.CROSS_PROCESS_PREFIX + "/grid.html", page.frames().get(1).evaluate("() => '' + location.href"));

    CDPSession parentCDP = page.context().newCDPSession(page.frames().get(0));
    JsonObject parentParams = new JsonObject();
    parentParams.addProperty("pierce", true);
    parentParams.addProperty("depth", -1);
    JsonObject parent = parentCDP.send("DOM.getDocument", parentParams);
    assertFalse(parent.toString().contains("./digits/1.png"));

    CDPSession oopifCDP = page.context().newCDPSession(page.frames().get(1));
    JsonObject oopifParams = new JsonObject();
    oopifParams.addProperty("pierce", true);
    oopifParams.addProperty("depth", -1);
    JsonObject oopif = oopifCDP.send("DOM.getDocument", oopifParams);
    assertTrue(oopif.toString().contains("./digits/1.png"));
  }

  @PlaywrightTest
  @DisplayName("OO-023 should emit filechooser event for iframe")
  void shouldEmitFilechooserEventForIframe(Page page, TestServer server, Browser browser) {
    // Add listener before OOPIF is created.
    // Java: the file chooser is awaited with a handler that completes a future.
    CompletableFuture<FileChooser> chooserPromise = new CompletableFuture<>();
    page.onFileChooser(chooserPromise::complete);
    page.navigate(server.PREFIX + "/dynamic-oopif.html");
    assertOOPIFCount(browser, 1);
    assertEquals(2, page.frames().size());
    Frame frame = page.frames().get(1);
    frame.setContent("<input type=file>");
    frame.click("input");
    page.waitForCondition(chooserPromise::isDone);
    FileChooser chooser = chooserPromise.getNow(null);
    assertNotNull(chooser);
  }

  @PlaywrightTest
  @DisplayName("OO-024 should be able to click in iframe")
  void shouldBeAbleToClickInIframe(Page page, TestServer server, Browser browser) {
    // issue: https://github.com/microsoft/playwright/issues/28023
    page.navigate(server.PREFIX + "/dynamic-oopif.html");
    assertOOPIFCount(browser, 1);
    assertEquals(2, page.frames().size());
    Frame frame = page.frames().get(1);
    frame.setContent("<button onclick=\"console.log('clicked')\">OK</button>");
    ConsoleMessage message = page.waitForConsoleMessage(() -> {
      frame.click("button");
    });
    assertEquals("clicked", message.text());
  }

  @PlaywrightTest
  @DisplayName("OO-026 should intercept response body from oopif")
  void shouldInterceptResponseBodyFromOopif(Page page, Browser browser, TestServer server) {
    // issue: https://github.com/microsoft/playwright/issues/20809
    Response response = page.waitForResponse("**/grid.html", () -> {
      page.navigate(server.PREFIX + "/dynamic-oopif.html");
    });
    assertFalse(response.text().isEmpty());
  }

  @PlaywrightTest
  @DisplayName("OO-027 should navigate a local frame to oopif with frame.goto")
  void shouldNavigateALocalFrameToOopifWithFrameGoto(Page page, Browser browser, TestServer server) {
    page.navigate(server.PREFIX + "/frames/one-frame.html");
    assertOOPIFCount(browser, 0);
    Frame frame = page.frames().get(1);
    assertEquals(server.PREFIX + "/frames/frame.html", frame.url());

    List<String> navigations = new ArrayList<>();
    page.onFrameNavigated(f -> navigations.add(f.url()));

    Response response = frame.navigate(server.CROSS_PROCESS_PREFIX + "/grid.html");
    assertOOPIFCount(browser, 1);
    assertEquals(server.CROSS_PROCESS_PREFIX + "/grid.html", response.url());
    assertSame(frame, response.frame());
    assertEquals(server.CROSS_PROCESS_PREFIX + "/grid.html", frame.url());
    assertEquals(server.CROSS_PROCESS_PREFIX + "/grid.html", frame.evaluate("() => '' + location.href"));
    assertEquals(Arrays.asList(server.CROSS_PROCESS_PREFIX + "/grid.html"), navigations);
    assertEquals(2, page.frames().size());
  }

  @PlaywrightTest
  @DisplayName("OO-028 should allow to re-connect to OOPIFs with CDP when iframes were there already")
  void shouldAllowToReConnectToOOPIFsWithCDPWhenIframesWereThereAlready(BrowserType browserType, TestServer server) {
    // issue: https://github.com/microsoft/playwright/issues/36095
    // issue: https://github.com/microsoft/playwright/issues/17656
    // Java: PWTEST_CHANNEL is BROWSER_CHANNEL here.
    assumeFalse(Env.channel() != null, "Test default channel only");

    // Java: there is no parallelIndex, every test of the file has its own port.
    int cdpPort = 10123;
    Browser hostBrowser = browserType.launch(Env.launchOptions().setChannel("chromium").setArgs(Arrays.asList("--remote-debugging-port=" + cdpPort)));
    Page hostPage = hostBrowser.newPage();
    hostPage.navigate(server.PREFIX + "/dynamic-oopif.html");

    Browser browser = browserType.connectOverCDP("http://localhost:" + cdpPort);
    Page page = browser.contexts().get(0).pages().get(0);
    assertEquals(2, page.frames().size());
    assertOOPIFCount(browser, 1);
    assertEquals(server.CROSS_PROCESS_PREFIX + "/grid.html", page.frames().get(1).evaluate("() => '' + location.href"));
    browser.close();
    hostBrowser.close();
  }

  @PlaywrightTest
  @DisplayName("OO-029 should report the whole frame tree when connecting over CDP to nested OOPIFs")
  void shouldReportTheWholeFrameTreeWhenConnectingOverCDPToNestedOOPIFs(BrowserType browserType, TestServer server) {
    // issue: https://github.com/microsoft/playwright/issues/42955
    // Java: PWTEST_CHANNEL is BROWSER_CHANNEL here.
    assumeFalse(Env.channel() != null, "Test default channel only");

    // top > cross-site iframe (oopif) > same-process child iframe > sandboxed srcdoc iframe (oopif).
    server.setRoute("/nested-top.html", (req, res) -> {
      res.setHeader("Content-Type", "text/html");
      res.end("<h1 id=top>TOP</h1><iframe src=\"" + server.CROSS_PROCESS_PREFIX + "/nested-b.html\"></iframe>");
    });
    server.setRoute("/nested-b.html", (req, res) -> {
      res.setHeader("Content-Type", "text/html");
      res.end("<iframe src=\"/nested-c.html\"></iframe>");
    });
    server.setRoute("/nested-c.html", (req, res) -> {
      res.setHeader("Content-Type", "text/html");
      res.end("<iframe sandbox=\"allow-scripts\" srcdoc=\"<p>D</p>\"></iframe>");
    });

    // Java: there is no parallelIndex, every test of the file has its own port.
    int cdpPort = 10127;
    Browser hostBrowser = browserType.launch(Env.launchOptions()
      .setChannel("chromium")
      .setArgs(Arrays.asList("--remote-debugging-port=" + cdpPort, "--site-per-process", "--enable-features=IsolateSandboxedIframes")));
    Page hostPage = hostBrowser.newPage();
    hostPage.navigate(server.PREFIX + "/nested-top.html");

    Browser browser = browserType.connectOverCDP("http://localhost:" + cdpPort);
    assertOOPIFCount(browser, 2);
    Page page = browser.contexts().get(0).pages().get(0);

    List<String> expectedTree = Arrays.asList(
      server.PREFIX + "/nested-top.html",
      "    " + server.CROSS_PROCESS_PREFIX + "/nested-b.html",
      "        " + server.CROSS_PROCESS_PREFIX + "/nested-c.html",
      "            about:srcdoc");
    assertEquals(expectedTree, dumpFrames(page.mainFrame(), ""));
    assertEquals(4, page.frames().size());

    assertEquals(server.PREFIX + "/nested-top.html", page.url());
    assertEquals(true, page.evaluate("() => window.top === window"));
    assertThat(page.locator("#top")).hasText("TOP");
    assertThat(page.frameLocator("iframe").frameLocator("iframe").frameLocator("iframe").locator("p")).hasText("D");

    browser.close();
    hostBrowser.close();
  }

  private static List<String> dumpFrames(Frame frame, String indent) {
    // Java: the URL is read after the evaluate, as upstream does; the frame of a new OOPIF gets it with its context.
    Object href = frame.evaluate("() => '' + location.href");
    assertEquals(frame.url(), href);
    List<String> result = new ArrayList<>();
    result.add(indent + frame.url());
    for (Frame child : frame.childFrames())
      result.addAll(dumpFrames(child, indent + "    "));
    return result;
  }

  private static void assertOOPIFCount(Browser browser, int count) {
    if (!browser.browserType().name().equals("chromium"))
      return;
    assertEquals(count, countOOPIFs(browser));
  }

  private static int countOOPIFs(Browser browser) {
    CDPSession browserSession = browser.newBrowserCDPSession();
    List<JsonObject> oopifs = new ArrayList<>();
    browserSession.on("Target.targetCreated", event -> {
      JsonObject targetInfo = event.getAsJsonObject("targetInfo");
      if (targetInfo.get("type").getAsString().equals("iframe"))
        oopifs.add(targetInfo);
    });
    JsonObject params = new JsonObject();
    params.addProperty("discover", true);
    browserSession.send("Target.setDiscoverTargets", params);
    browserSession.detach();
    return oopifs.size();
  }

  // Java: expect.poll(() => handle.boundingBox()).toEqual({ x, y, width, height }), with the intervals and timeout of expect.poll.
  private static void pollBoundingBox(Supplier<BoundingBox> actual, double x, double y, double width, double height) {
    int[] intervals = {100, 250, 500, 1000};
    long deadline = System.currentTimeMillis() + 5000;
    List<Double> expected = Arrays.asList(x, y, width, height);
    List<Double> value;
    for (int i = 0;; i++) {
      BoundingBox box = actual.get();
      value = box == null ? null : Arrays.asList(box.x, box.y, box.width, box.height);
      if (expected.equals(value) || System.currentTimeMillis() >= deadline)
        break;
      try {
        Thread.sleep(intervals[Math.min(i, intervals.length - 1)]);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        break;
      }
    }
    assertEquals(expected, value);
  }
}
