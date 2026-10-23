/*
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

import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.Tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.microsoft.playwright.fixtures.BrowserName.CHROMIUM;
import static com.microsoft.playwright.fixtures.BrowserName.FIREFOX;
import static com.microsoft.playwright.fixtures.BrowserName.WEBKIT;
import static com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED;
import static com.microsoft.playwright.options.LoadState.LOAD;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

@Tag("smoke")
public class TestPageBasic {

  @PlaywrightTest
  void shouldRejectAllPromisesWhenPageIsClosed(BrowserContext context) {
    Page newPage = context.newPage();
    newPage.close();
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      newPage.evaluate("() => new Promise(r => {})");
    });
    assertTrue(e.getMessage().contains("Target page, context or browser has been closed"), e.getMessage());
  }

  @PlaywrightTest
  void shouldNotBeVisibleInContextPages(BrowserContext context) {
    Page newPage = context.newPage();
    assertTrue(context.pages().contains(newPage));
    newPage.close();
    assertFalse(context.pages().contains(newPage));
  }

  @PlaywrightTest
  void shouldRunBeforeunloadIfAskedFor(Page page, BrowserContext context, Server server, BrowserName browserName) {
    Page newPage = context.newPage();
    newPage.navigate(server.PREFIX + "/beforeunload.html");
    // We have to interact with a page so that "beforeunload" handlers
    // fire.
    newPage.click("body");
    boolean[] didShowDialog = {false};
    newPage.onDialog(dialog -> {
      didShowDialog[0] = true;
      assertEquals("beforeunload", dialog.type());
      assertEquals("", dialog.defaultValue());
      if (browserName == CHROMIUM) {
        assertEquals("", dialog.message());
      } else if (browserName == WEBKIT) {
        assertEquals("Leave?", dialog.message());
      } else if (browserName == CHROMIUM) {
        assertEquals("This page is asking you to confirm that you want to leave - data you have entered may not be saved.", dialog.message());
      } else {
        assertEquals("This page is asking you to confirm that you want to leave — information you’ve entered may not be saved.", dialog.message());
      }
      dialog.accept();
    });
    newPage.close(new Page.CloseOptions().setRunBeforeUnload(true));
    for (int i = 0; i < 300; i++) {
      if (didShowDialog[0]) {
        break;
      }
      page.waitForTimeout(100);
    }
    assertTrue(didShowDialog[0]);
  }

  @PlaywrightTest
  void shouldNotRunBeforeunloadByDefault(BrowserContext context, Server server) {
    Page newPage = context.newPage();
    newPage.navigate(server.PREFIX + "/beforeunload.html");
    // We have to interact with a page so that "beforeunload" handlers
    // fire.
    newPage.click("body");
    boolean[] didShowDialog = {false};
    newPage.onDialog(dialog -> didShowDialog[0] = true);
    newPage.close();
    assertFalse(didShowDialog[0]);
  }

  @PlaywrightTest
  void shouldSetThePageCloseState(BrowserContext context) {
    Page newPage = context.newPage();
    assertEquals(false, newPage.isClosed());
    newPage.close();
    assertEquals(true, newPage.isClosed());
  }

  @PlaywrightTest
  void shouldTerminateNetworkWaiters(BrowserContext context, Server server) {
    Page newPage = context.newPage();
    PlaywrightException e1 = assertThrows(PlaywrightException.class, () -> {
      newPage.waitForResponse("**", () -> {
        PlaywrightException e2 = assertThrows(PlaywrightException.class, () -> {
          newPage.waitForRequest(server.EMPTY_PAGE, () -> newPage.close());
        });
        assertTrue(e2.getMessage().contains("Target page, context or browser has been closed"), e2.getMessage());
        assertFalse(e2.getMessage().contains("Timeout"), e2.getMessage());
      });
    });
    assertTrue(e1.getMessage().contains("Target page, context or browser has been closed"), e1.getMessage());
    assertFalse(e1.getMessage().contains("Timeout"), e1.getMessage());
  }

  @PlaywrightTest
  void shouldBeCallableTwice(BrowserContext context) {
    Page newPage = context.newPage();
    newPage.close();
    newPage.close();
    newPage.close();
  }

  @PlaywrightTest
  void shouldFireLoadWhenExpected(Page page) {
    page.navigate("about:blank");
    page.waitForLoadState(LOAD);
  }

  // TODO: not supported in sync api
  void asyncStacksShouldWork() {
  }

  @PlaywrightTest
  void shouldProvideAccessToTheOpenerPage(Page page) {
    Page popup = page.waitForPopup(() -> page.evaluate("() => window.open('about:blank')"));
    Page opener = popup.opener();
    assertEquals(page, opener);
  }

  @PlaywrightTest
  void shouldReturnNullIfParentPageHasBeenClosed(Page page) {
    Page popup = page.waitForPopup(() -> page.evaluate("() => window.open('about:blank')"));
    page.close();
    Page opener = popup.opener();
    assertEquals(null, opener);
  }

  @PlaywrightTest
  void shouldFireDomcontentloadedWhenExpected(Page page) {
    page.navigate("about:blank");
    page.waitForLoadState(DOMCONTENTLOADED);
  }

  @PlaywrightTest
  void shouldPassSelfAsArgumentToDomcontentloadedEvent(Page page) {
    Page[] eventPage = {null};
    page.onDOMContentLoaded(p -> eventPage[0] = p);
    page.navigate("about:blank");
    page.waitForLoadState(DOMCONTENTLOADED);
    assertEquals(page, eventPage[0]);
  }

  @PlaywrightTest
  void shouldPassSelfAsArgumentToLoadEvent(Page page) {
    Page[] eventPage = {null};
    page.onLoad(p -> eventPage[0] = p);
    page.navigate("about:blank");
    page.waitForLoadState(LOAD);
    assertEquals(page, eventPage[0]);
  }

  // TODO: downloads
  void shouldFailWithErrorUponDisconnect() {
  }

  @PlaywrightTest
  void pageUrlShouldWork(Page page, Server server) {
    assertEquals("about:blank", page.url());
    page.navigate(server.EMPTY_PAGE);
    assertEquals(server.EMPTY_PAGE, page.url());
  }

  @PlaywrightTest
  void pageUrlShouldIncludeHashes(Page page, Server server) {
    page.navigate(server.EMPTY_PAGE + "#hash");
    assertEquals(server.EMPTY_PAGE + "#hash", page.url());
    page.evaluate("() => {\n" +
      "    window.location.hash = 'dynamic';\n" +
      "}");
    assertEquals(server.EMPTY_PAGE + "#dynamic", page.url());
  }

  @PlaywrightTest
  void pageTitleShouldReturnThePageTitle(Page page, Server server) {
    page.navigate(server.PREFIX + "/title.html");
    assertEquals("Woof-Woof", page.title());
  }

  @PlaywrightTest
  void pageCloseShouldWorkWithWindowClose(Page page) {
    Page newPage = page.waitForPopup(() -> page.evaluate(
      "() => window['newPage'] = window.open('about:blank')"));
    newPage.waitForClose(() -> page.evaluate("() => window['newPage'].close()"));
  }

  @PlaywrightTest
  void pageCloseShouldWorkWithPageClose(BrowserContext context) {
    Page newPage = context.newPage();
    newPage.waitForClose(() -> newPage.close());
  }

  @PlaywrightTest
  void pageContextShouldReturnTheCorrectInstance(Page page, BrowserContext context) {
    assertEquals(context, page.context());
  }

  @PlaywrightTest
  void pageFrameShouldRespectName(Page page) {
    page.setContent("<iframe name=target></iframe>");
    assertNull(page.frame("bogus"));
    Frame frame = page.frame("target");
    assertNotNull(frame);
    assertEquals(page.mainFrame().childFrames().get(0), frame);
  }

  @PlaywrightTest
  void pageFrameShouldRespectUrl(Page page, Server server) {
    page.setContent("<iframe src='" + server.EMPTY_PAGE + "'></iframe>");
    assertNull(page.frameByUrl(Pattern.compile("bogus")));
    Frame frame = page.frameByUrl(Pattern.compile(".*empty.*"));
    assertNotNull(frame);
    assertEquals(server.EMPTY_PAGE, frame.url());
  }

  @PlaywrightTest
  void shouldHaveSaneUserAgent(Page page, BrowserName browserName) {
    String userAgent = (String) page.evaluate("() => navigator.userAgent");
    List<String> parts = Arrays.stream(userAgent.split("[()]")).map(part -> part.trim()).collect(Collectors.toList());
    // First part is always 'Mozilla/5.0'
    assertEquals(parts.get(0), "Mozilla/5.0");
    // Second part in parenthesis is platform - ignore it.

    // Third part for Firefox is the last one and encodes engine and browser versions.
    if (browserName == FIREFOX) {
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
    if (browserName == CHROMIUM) {
      assertTrue(engineAndBrowser[0].contains("Chrome/"));
    } else {
      assertTrue(engineAndBrowser[0].startsWith("Version/"));
    }
  }

  @PlaywrightTest
  void pagePressShouldWork(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/textarea.html");
    page.press("textarea", "a");
    assertEquals("a", page.evaluate("() => document.querySelector('textarea').value"));
  }

  @PlaywrightTest
  void pagePressShouldWorkForEnter(Page page) {
    page.setContent("<input onkeypress='console.log(\"press\")'></input>");
    List<ConsoleMessage> messages = new ArrayList<>();
    page.onConsoleMessage(message ->  messages.add(message));
    page.press("input", "Enter");
    assertEquals("press", messages.get(0).text());
  }

  @PlaywrightTest
  void framePressShouldWork(Page page, Server server) {
    page.setContent("<iframe name=inner src='" + server.PREFIX + "/input/textarea.html'></iframe>");
    Frame frame = page.frame("inner");
    frame.press("textarea", "a");
    assertEquals("a", frame.evaluate("() => document.querySelector('textarea').value"));
  }

  @PlaywrightTest
  @DisabledOnBrowser(value = FIREFOX, reason = "fail")
  void frameFocusShouldWorkMultipleTimes(BrowserContext context) {
    Page page1 = context.newPage();
    Page page2 = context.newPage();
    for (Page page : asList(page1, page2)) {
      page.setContent("<button id='foo' onfocus='window.gotFocus=true'></button>");
      page.focus("#foo");
      assertEquals(true, page.evaluate("() => !!window['gotFocus']"));
    }
  }

  @PlaywrightTest
  void frameDragAndDropShouldWork(Page page, Server server) {
    page.navigate(server.PREFIX + "/drag-n-drop.html");
    page.dragAndDrop("#source", "#target");
    assertEquals(true, page.evalOnSelector("#target", "target => target.contains(document.querySelector('#source'))"));
  }

  @PlaywrightTest
  void shouldWaitForCondition(Page page) {
    List<String> messages = new ArrayList<>();
    page.onConsoleMessage(m -> messages.add(m.text()));
    page.evaluate("setTimeout(() => {\n" +
      "  console.log('foo');\n" +
      "  console.log('bar');\n" +
      "}, 100);");
    page.waitForCondition(() -> messages.size() > 1);
    assertEquals(asList("foo", "bar"), messages);
  }

  @PlaywrightTest
  void waitForConditionTimeout(Page page) {
    PlaywrightException e = assertThrows(PlaywrightException.class,
      () -> page.waitForCondition(() -> false, new Page.WaitForConditionOptions().setTimeout(100)));
    assertTrue(e.getMessage().contains("Timeout"), e.getMessage());
  }
  @PlaywrightTest
  void waitForConditionPageClosed(Page page) {
    PlaywrightException e = assertThrows(PlaywrightException.class,
      () -> page.waitForCondition(() -> {
        page.close();
        return false;
      }));
    assertTrue(e.getMessage().contains("Target page, context or browser has been closed"), e.getMessage());
  }

  @PlaywrightTest
  void shouldPropagateCloseReasonToPendingActions(BrowserContext context) {
    Page page = context.newPage();
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.waitForPopup(() -> {
      page.close(new Page.CloseOptions().setReason("The reason."));
    }));
    assertTrue(e.getMessage().contains("The reason."), e.getMessage());
  }

  @PlaywrightTest
  void shouldProhibitNullListeners(BrowserContext context) {
    Page newPage = context.newPage();

    PlaywrightException e = assertThrows(PlaywrightException.class, () -> newPage.onClose(null));

    assertTrue(e.getMessage().contains("Can't add a null listener"));
  }

  @PlaywrightTest
  void pagePauseShouldNotThrow(Page page) {
    page.pause();
  }

}
