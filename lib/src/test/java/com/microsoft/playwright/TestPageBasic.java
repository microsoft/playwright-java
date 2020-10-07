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

package com.microsoft.playwright;

import org.junit.jupiter.api.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.microsoft.playwright.Page.LoadState.DOMCONTENTLOADED;
import static com.microsoft.playwright.Page.LoadState.LOAD;
import static org.junit.jupiter.api.Assertions.*;

public class TestPageBasic {
  private static Server server;
  private static Browser browser;
  private static boolean isChromium;
  private static boolean isWebKit;
  private static boolean isFirefox;
  private BrowserContext context;
  private Page page;

  @BeforeAll
  static void launchBrowser() {
    Playwright playwright = Playwright.create();
    BrowserType.LaunchOptions options = new BrowserType.LaunchOptions();
    browser = playwright.chromium().launch(options);
    isChromium = true;

  }

  @BeforeAll
  static void startServer() throws IOException {
    server = new Server(8907);
  }

  @AfterAll
  static void stopServer() throws IOException {
    browser.close();
    server.stop();
    server = null;
  }

  @BeforeEach
  void setUp() {
    context = browser.newContext();
    page = context.newPage();
  }

  @AfterEach
  void tearDown() {
    context.close();
    context = null;
    page = null;
  }

  @Test
  void shouldRejectAllPromisesWhenPageIsClosed() {
    Page newPage = context.newPage();
    newPage.close();
    try {
      newPage.evaluate("() => new Promise(r => {})");
      fail("evaluate should throw");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("Protocol error"));
    }
  }

  @Test
  void shouldNotBeVisibleInContextPages() {
    Page newPage = context.newPage();
    assertTrue(context.pages().contains(newPage));
    newPage.close();
    assertFalse(context.pages().contains(newPage));
  }

  @Test
  void shouldRunBeforeunloadIfAskedFor() {
    Page newPage = context.newPage();
    newPage.navigate(server.PREFIX + "/beforeunload.html");
    // We have to interact with a page so that "beforeunload" handlers
    // fire.
    newPage.click("body");
    boolean[] didShowDialog = {false};
    newPage.addDialogListener(dialog -> {
      didShowDialog[0] = true;
      assertEquals("beforeunload", dialog.type());
      assertEquals("", dialog.defaultValue());
      if (isChromium) {
        assertEquals("", dialog.message());
      } else if (isWebKit) {
        assertEquals("Leave?", dialog.message());
      } else {
        assertEquals("This page is asking you to confirm that you want to leave - data you have entered may not be saved.", dialog.message());
      }
      dialog.accept();
    });
    newPage.close(new Page.CloseOptions().withRunBeforeUnload(true));
    // TODO: uncomment once https://github.com/microsoft/playwright/pull/4070 is committed.
//    assertTrue(didShowDialog[0]);
  }

  @Test
  void shouldNotRunBeforeunloadByDefault() {
    Page newPage = context.newPage();
    newPage.navigate(server.PREFIX + "/beforeunload.html");
    // We have to interact with a page so that "beforeunload" handlers
    // fire.
    newPage.click("body");
    boolean[] didShowDialog = {false};
    newPage.addDialogListener(dialog -> didShowDialog[0] = true);
    newPage.close();
    assertFalse(didShowDialog[0]);
  }

  @Test
  void shouldSetThePageCloseState() {
    Page newPage = context.newPage();
    assertEquals(false, newPage.isClosed());
    newPage.close();
    assertEquals(true, newPage.isClosed());
  }

  @Test
  void shouldTerminateNetworkWaiters() {
    Page newPage = context.newPage();
    Deferred<Request> request = newPage.waitForRequest(server.EMPTY_PAGE);
    Deferred<Response> response = newPage.waitForResponse(server.EMPTY_PAGE);
    newPage.close();
    try {
      request.get();
      fail("get() should throw");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("Page closed"));
      assertFalse(e.getMessage().contains("Timeout"));
    }
    try {
      response.get();
      fail("get() should throw");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("Page closed"));
      assertFalse(e.getMessage().contains("Timeout"));
    }
  }

  @Test
  void shouldBeCallableTwice() {
    Page newPage = context.newPage();
    newPage.close();
    newPage.close();
    newPage.close();
  }

  @Test
  void shouldFireLoadWhenExpected() {
    page.navigate("about:blank");
    page.waitForLoadState(LOAD);
  }

  // TODO: not supported in sync api
  void asyncStacksShouldWork() {
  }

  @Test
  void shouldProvideAccessToTheOpenerPage() {
    Deferred<Page> popupEvent = page.waitForPopup();
    page.evaluate("() => window.open('about:blank')");
    Page opener = popupEvent.get().opener();
    assertEquals(page, opener);
  }

  @Test
  void shouldReturnNullIfParentPageHasBeenClosed() {
    Deferred<Page> popupEvent = page.waitForPopup();
    page.evaluate("() => window.open('about:blank')");
    Page popup = popupEvent.get();
    page.close();
    Page opener = popup.opener();
    assertEquals(null, opener);
  }

  @Test
  void shouldFireDomcontentloadedWhenExpected() {
    page.navigate("about:blank");
    page.waitForLoadState(DOMCONTENTLOADED);
  }

  // TODO: downloads
  void shouldFailWithErrorUponDisconnect() {
  }

  @Test
  void pageUrlShouldWork() {
    assertEquals("about:blank", page.url());
    page.navigate(server.EMPTY_PAGE);
    assertEquals(server.EMPTY_PAGE, page.url());
  }

  @Test
  void pageUrlShouldIncludeHashes() {
    page.navigate(server.EMPTY_PAGE + "#hash");
    assertEquals(server.EMPTY_PAGE + "#hash", page.url());
    page.evaluate("() => {\n" +
      "    window.location.hash = 'dynamic';\n" +
      "}");
    assertEquals(server.EMPTY_PAGE + "#dynamic", page.url());
  }

  @Test
  void pageTitleShouldReturnThePageTitle() {
    page.navigate(server.PREFIX + "/title.html");
    assertEquals("Woof-Woof", page.title());
  }

  @Test
  void pageCloseShouldWorkWithWindowClose() {
    Deferred<Page> newPagePromise = page.waitForPopup();
    page.evaluate("() => window['newPage'] = window.open('about:blank')");
    Page newPage = newPagePromise.get();
    Deferred<Void> closedPromise = newPage.waitForClose();
    page.evaluate("() => window['newPage'].close()");
    closedPromise.get();
  }

  @Test
  void pageCloseShouldWorkWithPageClose() {
    Page newPage = context.newPage();
    Deferred<Void> closedPromise = newPage.waitForClose();
    newPage.close();
    closedPromise.get();
  }

  @Test
  void pageContextShouldReturnTheCorrectInstance() {
    assertEquals(context, page.context());
  }

  @Test
  void pageFrameShouldRespectName() {
    page.setContent("<iframe name=target></iframe>");
    assertNull(page.frame(new Page.FrameOptions().withName("bogus")));
    Frame frame = page.frame(new Page.FrameOptions().withName("target"));
    assertNotNull(frame);
    assertEquals(page.mainFrame().childFrames().get(0), frame);
  }

  @Test
  void pageFrameShouldRespectUrl() {
    page.setContent("<iframe src='" + server.EMPTY_PAGE + "'></iframe>");
    assertNull(page.frame(new Page.FrameOptions().withUrl(Pattern.compile("bogus"))));
    Frame frame = page.frame(new Page.FrameOptions().withUrl(Pattern.compile(".*empty.*")));
    assertNotNull(frame);
    assertEquals(server.EMPTY_PAGE, frame.url());
  }

  @Test
  void shouldHaveSaneUserAgent() {
    String userAgent = (String) page.evaluate("() => navigator.userAgent");
    List<String> parts = Arrays.stream(userAgent.split("[()]")).map(part -> part.trim()).collect(Collectors.toList());
    // First part is always 'Mozilla/5.0'
    assertEquals(parts.get(0), "Mozilla/5.0");
    // Second part in parenthesis is platform - ignore it.

    // Third part for Firefox is the last one and encodes engine and browser versions.
    if (isFirefox) {
      String[] engineAndBrowser = parts.get(2).split(" ");
      assertTrue(engineAndBrowser[0].startsWith("Gecko"));
      assertTrue(engineAndBrowser[1].startsWith("Firefox"));
      return;
    }
    // For both options.CHROMIUM and options.WEBKIT, third part is the AppleWebKit version.
    assertTrue(parts.get(2).startsWith("AppleWebKit/"));
    assertEquals("KHTML, like Gecko", parts.get(3));
    // 5th part encodes real browser name and engine version.
    String[] engineAndBrowser = parts.get(4).split(" ");
    assertTrue(engineAndBrowser[1].startsWith("Safari"));
    if (isChromium) {
      assertTrue(engineAndBrowser[0].contains("Chrome/"));
    } else {
      assertTrue(engineAndBrowser[0].startsWith("Version/"));
    }
  }

  @Test
  void pagePressShouldWork() {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.press("textarea", "a");
    assertEquals("a", page.evaluate("() => document.querySelector('textarea').value"));
  }

  @Test
  void pagePressShouldWorkForEnter() {
    page.setContent("<input onkeypress='console.log(\"press\")'></input>");
    List<ConsoleMessage> messages = new ArrayList<>();
    page.addConsoleListener(message -> messages.add(message));
    page.press("input", "Enter");
    assertEquals("press", messages.get(0).text());
  }

  @Test
  void framePressShouldWork() {
    page.setContent("<iframe name=inner src='" + server.PREFIX + "/input/textarea.html'></iframe>");
    Frame frame = page.frame(new Page.FrameOptions().withName("inner"));
    frame.press("textarea", "a");
    assertEquals("a", frame.evaluate("() => document.querySelector('textarea').value"));
  }

  @Test
  void frameFocusShouldWorkMultipleTimes() {
    // TODO:    test.fail(browserName === "firefox");
    Page page1 = context.newPage();
    Page page2 = context.newPage();
    for (Page page : Arrays.asList(page1, page2)) {
      page.setContent("<button id='foo' onfocus='window.gotFocus=true'></button>");
      page.focus("#foo");
      assertEquals(true, page.evaluate("() => !!window['gotFocus']"));
    }
  }

}
