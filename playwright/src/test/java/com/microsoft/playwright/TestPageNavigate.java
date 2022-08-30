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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class TestPageNavigate extends TestBase {
  @Test
  void shouldWork() {
    page.navigate(server.EMPTY_PAGE);
    assertEquals(server.EMPTY_PAGE, page.url());
  }

  @Test
  void shouldWorkWithFileURL() {
    String fileUrl = Paths.get("src/test/resources/frames/two-frames.html").toUri().toString();
    page.navigate(fileUrl);
    assertEquals(fileUrl.toLowerCase(), page.url().toLowerCase());
    assertEquals(3, page.frames().size());
  }

  @Test
  void shouldUseHttpForNoProtocol() {
    page.navigate(server.EMPTY_PAGE.substring("http://".length()));
    assertEquals(server.EMPTY_PAGE, page.url());
  }

  @Test
  void shouldWorkCrossProcess() {
    page.navigate(server.EMPTY_PAGE);
    assertEquals(server.EMPTY_PAGE, page.url());

    String url = server.CROSS_PROCESS_PREFIX + "/empty.html";
    Response response = page.navigate(url);
    assertEquals(url, page.url());
    assertEquals(page.mainFrame(), response.frame());
    assertEquals(page.mainFrame(), response.request().frame());
    assertEquals(url, response.url());
  }

  @Test
  void shouldCaptureIframeNavigationRequest() {
    page.navigate(server.EMPTY_PAGE);
    assertEquals(server.EMPTY_PAGE, page.url());
    Request request = page.waitForRequest(server.PREFIX + "/frames/frame.html", () -> {
      Response response = page.navigate(server.PREFIX + "/frames/one-frame.html");
      assertEquals(server.PREFIX + "/frames/one-frame.html", page.url());
      assertEquals(page.mainFrame(), response.frame());
      assertEquals(server.PREFIX + "/frames/one-frame.html", response.url());

      assertEquals(2, page.frames().size());
    });
    assertEquals(page.frames().get(1), request.frame());
  }

  @Test
  void shouldWorkWithAnchorNavigation() {
    page.navigate(server.EMPTY_PAGE);
    assertEquals(server.EMPTY_PAGE, page.url());
    page.navigate(server.EMPTY_PAGE + "#foo");
    assertEquals(server.EMPTY_PAGE + "#foo", page.url());
    page.navigate(server.EMPTY_PAGE + "#bar");
    assertEquals(server.EMPTY_PAGE + "#bar", page.url());
  }

  @Test
  void shouldWorkWithRedirects() {
    server.setRedirect("/redirect/1.html", "/redirect/2.html");
    server.setRedirect("/redirect/2.html", "/empty.html");
    Response response = page.navigate(server.PREFIX + "/redirect/1.html");
    assertEquals(200, response.status());
    assertEquals(server.EMPTY_PAGE, page.url());
  }

  @Test
  void shouldNavigateToAboutBlank() {
    Response response = page.navigate("about:blank");
    assertNull(response);
  }

  @Test
  void shouldReturnResponseWhenPageChangesItsURLAfterLoad() {
    Response response = page.navigate(server.PREFIX + "/historyapi.html");
    assertEquals(200, response.status());
  }

  @Test
  void shouldWorkWithSubframesReturn204() {
    server.setRoute("/frames/frame.html", exchange -> {
      exchange.sendResponseHeaders(204, -1);
      exchange.getResponseBody().close();
    });
    page.navigate(server.PREFIX + "/frames/one-frame.html");
  }

  @Test
  void shouldWorkWithSubframesReturn204WithDomcontentloaded() {
    server.setRoute("/frames/frame.html", exchange -> {
      exchange.sendResponseHeaders(204, -1);
      exchange.getResponseBody().close();
    });
    page.navigate(server.PREFIX + "/frames/one-frame.html", new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
  }

  @Test
  void shouldFailWhenServerReturns204() {
    // WebKit just loads an empty page.
    server.setRoute("/empty.html", exchange -> {
      exchange.sendResponseHeaders(204, -1);
      exchange.getResponseBody().close();
    });
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.navigate(server.EMPTY_PAGE));
    if (isChromium())
      assertTrue(e.getMessage().contains("net::ERR_ABORTED"));
    else if (isWebKit())
      assertTrue(e.getMessage().contains("Aborted: 204 No Content"));
    else
      assertTrue(e.getMessage().contains("NS_BINDING_ABORTED"));
  }

  @Test
  void shouldNavigateToEmptyPageWithDomcontentloaded() {
    Response response = page.navigate(server.EMPTY_PAGE, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    assertEquals(200, response.status());
  }

  @Test
  void shouldWorkWhenPageCallsHistoryAPIInBeforeunload() {
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("window.addEventListener('beforeunload', () => history.replaceState(null, 'initial', window.location.href), false);");
    Response response = page.navigate(server.PREFIX + "/grid.html");
    assertEquals(200, response.status());
  }

  @Test
  void shouldCaptureCrossProcessIframeNavigationRequest() {
    page.navigate(server.EMPTY_PAGE);
    assertEquals(server.EMPTY_PAGE, page.url());

    Request request = page.waitForRequest(server.CROSS_PROCESS_PREFIX + "/frames/frame.html", () -> {
      Response response = page.navigate(server.CROSS_PROCESS_PREFIX + "/frames/one-frame.html");
      assertEquals(server.CROSS_PROCESS_PREFIX + "/frames/one-frame.html", page.url());
      assertEquals(page.mainFrame(), response.frame());
      assertEquals(server.CROSS_PROCESS_PREFIX + "/frames/one-frame.html", response.url());

      assertEquals(2, page.frames().size());
    });
    assertEquals(page.frames().get(1), request.frame());
  }

  @Test
  void shouldSendReferer() throws ExecutionException, InterruptedException {
    Future<Server.Request> request1 = server.futureRequest("/grid.html");
    Future<Server.Request> request2 = server.futureRequest("/digits/1.png");

    page.navigate(server.PREFIX + "/grid.html", new Page.NavigateOptions()
      .setReferer("http://google.com/"));
    assertEquals(asList("http://google.com/"), request1.get().headers.get("referer"));
    // Make sure subresources do not inherit referer.
    assertEquals(asList(server.PREFIX + "/grid.html"), request2.get().headers.get("referer"));
    assertEquals(server.PREFIX + "/grid.html", page.url());
  }
}
