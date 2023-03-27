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

import com.microsoft.playwright.options.WaitUntilState;
import org.junit.jupiter.api.Test;

import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import static com.microsoft.playwright.Utils.expectedSSLError;
import static java.util.Collections.nCopies;
import static org.junit.jupiter.api.Assertions.*;

public class TestPageWaitForNavigation extends TestBase {

  @Test
  void shouldWork() {
    page.navigate(server.EMPTY_PAGE);
    Response response = page.waitForNavigation(() -> page.evaluate(
      "url => window.location.href = url", server.PREFIX + "/grid.html"));
    assertTrue(response.ok());
    assertTrue(response.url().contains("grid.html"));
  }

  @Test
  void shouldRespectTimeout() {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.waitForNavigation(
        new Page.WaitForNavigationOptions().setUrl("**/frame.html").setTimeout(5000),
          () -> page.navigate(server.EMPTY_PAGE));
    });
    assertTrue(e.getMessage().contains("Timeout 5000ms exceeded"));
  }

  // Skipped in sync API.
  void shouldWorkWithBothDomcontentloadedAndLoad() {
  }

  @Test
  void shouldWorkWithCommit() {
    server.setRoute("/script.js", exchange -> {});
    server.setRoute("/empty.html", exchange -> {
      exchange.getResponseHeaders().add("Content-Type", "text/html");
      exchange.sendResponseHeaders(200, 0);
      try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody())) {
        writer.write("<title>Hello</title><script src=\"script.js\"></script>");
      }
    });
    page.waitForNavigation(new Page.WaitForNavigationOptions().setWaitUntil(WaitUntilState.COMMIT), () -> {
      try {
        page.navigate(server.EMPTY_PAGE, new Page.NavigateOptions().setTimeout(1000));
      } catch (TimeoutError e) {
      }
    });
    assertEquals("Hello", page.title());
  }

  @Test
  void shouldWorkWithClickingOnAnchorLinks() {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a href='#foobar'>foobar</a>");
    Response response = page.waitForNavigation(() -> page.click("a"));
    assertNull(response);
    assertEquals(server.EMPTY_PAGE + "#foobar", page.url());
  }

  private boolean checkSSLErrorMessage(String exceptionMessage, List<String> possibleErrorMessages) {
    return possibleErrorMessages.stream().anyMatch(exceptionMessage::contains);
  }

  @Test
  void shouldWorkWithClickingOnLinksWhichDoNotCommitNavigation() throws InterruptedException {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a href='" + httpsServer.EMPTY_PAGE + "'>foobar</a>");
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.waitForNavigation(() -> page.click("a"));
    });
    // TODO: figure out why it is inconsistent on Linux WebKit.
    List<String> possibleErrorMessages = expectedSSLError(browserType.name());
    assertTrue(checkSSLErrorMessage(e.getMessage(), possibleErrorMessages), "Unexpected exception: '" + e.getMessage() + "' check message(s): " + String.join(",", possibleErrorMessages));
  }

  @Test
  void shouldWorkWithHistoryPushState() {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a onclick='javascript:pushState()'>SPA</a>\n" +
      "<script>\n" +
      "  function pushState() { history.pushState({}, '', 'wow.html') }\n" +
      "</script>");
    Response response = page.waitForNavigation(() -> page.click("a"));
    assertNull(response);
    assertEquals(server.PREFIX + "/wow.html", page.url());
  }

  @Test
  void shouldWorkWithHistoryReplaceState() {
    page.navigate(server.EMPTY_PAGE);
    page.setContent(" <a onclick='javascript:replaceState()'>SPA</a>\n" +
      "<script>\n" +
      "  function replaceState() { history.replaceState({}, '', '/replaced.html') }\n" +
      "</script>");
    Response response = page.waitForNavigation(() -> page.click("a"));
    assertNull(response);
    assertEquals(server.PREFIX + "/replaced.html", page.url());
  }

  @Test
  void shouldWorkWithDOMHistoryBackHistoryForward() {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<a id=back onclick='javascript:goBack()'>back</a>\n" +
      "<a id=forward onclick='javascript:goForward()'>forward</a>\n" +
      "<script>\n" +
      "  function goBack() { history.back(); }\n" +
      "  function goForward() { history.forward(); }\n" +
      "  history.pushState({}, '', '/first.html');\n" +
      "  history.pushState({}, '', '/second.html');\n" +
      "</script>");
    assertEquals(server.PREFIX + "/second.html", page.url());

    Response backResponse = page.waitForNavigation(() -> page.click("a#back"));
    assertNull(backResponse);
    assertEquals(server.PREFIX + "/first.html", page.url());

    Response forwardResponse = page.waitForNavigation(() -> page.click("a#forward"));
    page.click("a#forward");
    assertNull(forwardResponse);
    assertEquals(server.PREFIX + "/second.html", page.url());
  }

  @Test
  void shouldWorkWhenSubframeIssuesWindowStop() {
    server.setRoute("/frames/style.css", exchange -> {});
    boolean[] frameWindowStopCalled = {false};
    page.onFrameAttached(frame -> {
      page.onFrameNavigated(frame1 -> {
        if (frame.equals(frame1)) {
          frame.evaluate("window.stop()");
          frameWindowStopCalled[0] = true;
        }
      });
    });
    try {
      page.navigate(server.PREFIX + "/frames/one-frame.html", new Page.NavigateOptions().setTimeout(2000));
    } catch (TimeoutError e) {
      assertTrue(isWebKit()); // Chromium and Firefox issue load event in this case.
    }
    assertTrue(frameWindowStopCalled[0]);
  }

  @Test
  void shouldWorkWithUrlMatch() {
    page.navigate(server.EMPTY_PAGE);

    Response response1 = page.waitForNavigation(
      new Page.WaitForNavigationOptions().setUrl("**/one-style.html"),
        () -> page.navigate(server.PREFIX + "/one-style.html"));
    assertNotNull(response1);
    assertEquals(server.PREFIX + "/one-style.html", response1.url());

    Response response2 = page.waitForNavigation(
      new Page.WaitForNavigationOptions().setUrl(Pattern.compile("frame.html$")),
        () -> page.navigate(server.PREFIX + "/frame.html"));
    assertNotNull(response2);
    assertEquals(server.PREFIX + "/frame.html", response2.url());

    Response response3 = page.waitForNavigation(
      new Page.WaitForNavigationOptions().setUrl(url -> {
        try {
          return new URL(url).getQuery().contains("foo=bar");
        } catch (MalformedURLException e) {
          throw new RuntimeException(e);
        }
      }),
        () -> page.navigate(server.PREFIX + "/frame.html?foo=bar"));
    assertNotNull(response3);
    assertEquals(server.PREFIX + "/frame.html?foo=bar", response3.url());
  }

  @Test
  void shouldWorkWithUrlMatchForSameDocumentNavigations() {
    page.navigate(server.EMPTY_PAGE);
    Response response = page.waitForNavigation(new Page.WaitForNavigationOptions().setUrl("**/third.html"), () -> {
      page.evaluate("() => {\n" +
        "  history.pushState({}, '', '/first.html');\n" +
        "}");
      page.evaluate("() => {\n" +
        "  history.pushState({}, '', '/second.html');\n" +
        "}");
      page.evaluate("() => {\n" +
        "  history.pushState({}, '', '/third.html');\n" +
        "}");
    });
    assertNull(response);
  }

  @Test
  void shouldWorkForCrossProcessNavigations() {
    page.navigate(server.EMPTY_PAGE);
    String url = server.CROSS_PROCESS_PREFIX + "/empty.html";
    Response response = page.waitForNavigation(
      new Page.WaitForNavigationOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED),
      () -> page.navigate(url));
    assertEquals(url, response.url());
    assertEquals(url, page.url());
    assertEquals(url, page.evaluate("document.location.href"));
  }

  @Test
  void shouldWorkOnFrame() {
    page.navigate(server.PREFIX + "/frames/one-frame.html");
    Frame frame = page.frames().get(1);
    Response response = frame.waitForNavigation(() -> frame.evaluate("url => window.location.href = url", server.PREFIX + "/grid.html"));
    assertTrue(response.ok());
    assertTrue(response.url().contains("grid.html"));
    assertEquals(frame, response.frame());
    assertTrue(page.url().contains("/frames/one-frame.html"));
  }

  @Test
  void shouldFailWhenFrameDetaches() throws InterruptedException {
    page.navigate(server.PREFIX + "/frames/one-frame.html");
    Frame frame = page.frames().get(1);
    server.setRoute("/empty.html", exchange -> {});
    server.setRoute("/one-style.css", exchange -> {});
    PlaywrightException ex = assertThrows(PlaywrightException.class, () -> {
      frame.waitForNavigation(() -> {
        Future<Server.Request> req = server.futureRequest("/one-style.css");
        page.evalOnSelector("iframe", "frame => { frame.contentWindow.location.href = '/one-style.html'; }");
        try {
          // Make sure policy checks pass and navigation actually begins before removing the frame to avoid other errors
          req.get();
        } catch (InterruptedException | ExecutionException e) {
          throw new RuntimeException(e);
        }
        page.evalOnSelector("iframe", "frame => setTimeout(() => frame.remove(), 0)");
      });
    });
    assertTrue(ex.getMessage().contains("frame was detached"), ex.getMessage());
  }

  @Test
  void shouldThrowOnInvalidUrlMatcherTypeInPage() {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      Page.WaitForNavigationOptions options = new Page.WaitForNavigationOptions();
      options.url = new Object();
      page.waitForNavigation(options, () -> {});
    });
    assertTrue(e.getMessage().contains("Url must be String, Pattern or Predicate<String>"));
  }

  @Test
  void shouldThrowOnInvalidUrlMatcherTypeInFrame() {
    page.navigate(server.PREFIX + "/frames/one-frame.html");
    Frame frame = page.frames().get(1);
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      Frame.WaitForNavigationOptions options = new Frame.WaitForNavigationOptions();
      options.url = new Object();
      frame.waitForNavigation(options, () -> {});
    });
    assertTrue(e.getMessage().contains("Url must be String, Pattern or Predicate<String>"));
  }
}
