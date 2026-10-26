/*
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

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

import static com.microsoft.playwright.fixtures.Utils.attachFrame;
import static com.microsoft.playwright.fixtures.Utils.detachFrame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/page/frame-hierarchy.spec.ts. */
public class TestFrameHierarchy {
  private static List<String> dumpFrames(Frame frame) {
    return dumpFrames(frame, "");
  }

  private static List<String> dumpFrames(Frame frame, String indentation) {
    String description = frame.url();
    if (!frame.name().isEmpty()) {
      description += " (" + frame.name() + ")";
    }
    List<String> result = new ArrayList<>();
    result.add(indentation + description);
    List<Frame> childFrames = new ArrayList<>(frame.childFrames());
    childFrames.sort((a, b) -> {
      if (!a.url().equals(b.url())) {
        return a.url().compareTo(b.url()) < 0 ? -1 : 1;
      }
      return a.name().compareTo(b.name()) < 0 ? -1 : 1;
    });
    for (Frame child : childFrames) {
      result.addAll(dumpFrames(child, "    " + indentation));
    }
    return result;
  }

  @PlaywrightTest
  @DisplayName("FR-024 should handle nested frames @smoke")
  void shouldHandleNestedFrames(Page page, TestServer server) {
    assumeFalse(Env.isBidi(), "frame.name() is racy with BiDi");

    page.navigate(server.PREFIX + "/frames/nested-frames.html");
    assertEquals(Arrays.asList(
      server.PREFIX + "/frames/nested-frames.html",
      "    " + server.PREFIX + "/frames/frame.html (aframe)",
      "    " + server.PREFIX + "/frames/two-frames.html (2frames)",
      "        " + server.PREFIX + "/frames/frame.html (dos)",
      "        " + server.PREFIX + "/frames/frame.html (uno)"),
      dumpFrames(page.mainFrame()));
  }

  @PlaywrightTest
  @DisplayName("FR-025 should send events when frames are manipulated dynamically")
  void shouldSendEventsWhenFramesAreManipulatedDynamically(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    // validate frameattached events
    List<Frame> attachedFrames = new ArrayList<>();
    page.onFrameAttached(frame -> attachedFrames.add(frame));
    attachFrame(page, "frame1", "./assets/frame.html");
    assertEquals(1, attachedFrames.size());
    assertTrue(attachedFrames.get(0).url().contains("/assets/frame.html"));

    // validate framenavigated events
    List<Frame> navigatedFrames = new ArrayList<>();
    page.onFrameNavigated(frame -> navigatedFrames.add(frame));
    page.evaluate("() => {\n" +
      "  const frame = document.getElementById('frame1');\n" +
      "  frame.src = './empty.html';\n" +
      "  return new Promise(x => frame.onload = x);\n" +
      "}");
    assertEquals(1, navigatedFrames.size());
    assertEquals(server.EMPTY_PAGE, navigatedFrames.get(0).url());

    // validate framedetached events
    List<Frame> detachedFrames = new ArrayList<>();
    page.onFrameDetached(frame -> detachedFrames.add(frame));
    detachFrame(page, "frame1");
    assertEquals(1, detachedFrames.size());
    assertEquals(true, detachedFrames.get(0).isDetached());
  }

  @PlaywrightTest
  @DisplayName("FR-026 should send \"framenavigated\" when navigating on anchor URLs")
  void shouldSendFramenavigatedWhenNavigatingOnAnchorURLs(Page page, TestServer server) throws Exception {
    page.navigate(server.EMPTY_PAGE);
    // Java: there is no page.waitForFrameNavigated(); the future of an onFrameNavigated handler stands for page.waitForEvent('framenavigated')
    CompletableFuture<Frame> framenavigated = new CompletableFuture<>();
    page.onFrameNavigated(frame -> framenavigated.complete(frame));
    page.navigate(server.EMPTY_PAGE + "#foo");
    page.waitForCondition(framenavigated::isDone);
    assertEquals(server.EMPTY_PAGE + "#foo", page.url());
  }

  @PlaywrightTest
  @DisplayName("FR-027 should persist mainFrame on cross-process navigation")
  void shouldPersistMainFrameOnCrossProcessNavigation(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Frame mainFrame = page.mainFrame();
    page.navigate(server.CROSS_PROCESS_PREFIX + "/empty.html");
    assertTrue(page.mainFrame() == mainFrame);
  }

  @PlaywrightTest
  @DisplayName("FR-028 should not send attach/detach events for main frame")
  void shouldNotSendAttachDetachEventsForMainFrame(Page page, TestServer server) {
    boolean[] hasEvents = {false};
    page.onFrameAttached(frame -> hasEvents[0] = true);
    page.onFrameDetached(frame -> hasEvents[0] = true);
    page.navigate(server.EMPTY_PAGE);
    assertEquals(false, hasEvents[0]);
  }

  @PlaywrightTest
  @DisplayName("FR-029 should detach child frames on navigation")
  void shouldDetachChildFramesOnNavigation(Page page, TestServer server) {
    List<Frame> attachedFrames = new ArrayList<>();
    List<Frame> detachedFrames = new ArrayList<>();
    List<Frame> navigatedFrames = new ArrayList<>();
    page.onFrameAttached(frame -> attachedFrames.add(frame));
    page.onFrameDetached(frame -> detachedFrames.add(frame));
    page.onFrameNavigated(frame -> navigatedFrames.add(frame));
    page.navigate(server.PREFIX + "/frames/nested-frames.html");
    assertEquals(4, attachedFrames.size());
    assertEquals(0, detachedFrames.size());
    assertEquals(5, navigatedFrames.size());

    // Java: the handlers keep the lists, so they are cleared instead of reassigned
    attachedFrames.clear();
    detachedFrames.clear();
    navigatedFrames.clear();
    page.navigate(server.EMPTY_PAGE);
    assertEquals(0, attachedFrames.size());
    assertEquals(4, detachedFrames.size());
    assertEquals(1, navigatedFrames.size());
  }

  @PlaywrightTest
  @DisplayName("FR-030 should support framesets")
  void shouldSupportFramesets(Page page, TestServer server) {
    List<Frame> attachedFrames = new ArrayList<>();
    List<Frame> detachedFrames = new ArrayList<>();
    List<Frame> navigatedFrames = new ArrayList<>();
    page.onFrameAttached(frame -> attachedFrames.add(frame));
    page.onFrameDetached(frame -> detachedFrames.add(frame));
    page.onFrameNavigated(frame -> navigatedFrames.add(frame));
    page.navigate(server.PREFIX + "/frames/frameset.html");
    assertEquals(4, attachedFrames.size());
    assertEquals(0, detachedFrames.size());
    assertEquals(5, navigatedFrames.size());

    // Java: the handlers keep the lists, so they are cleared instead of reassigned
    attachedFrames.clear();
    detachedFrames.clear();
    navigatedFrames.clear();
    page.navigate(server.EMPTY_PAGE);
    assertEquals(0, attachedFrames.size());
    assertEquals(4, detachedFrames.size());
    assertEquals(1, navigatedFrames.size());
  }

  @PlaywrightTest
  @DisplayName("FR-031 should report frame from-inside shadow DOM")
  void shouldReportFrameFromInsideShadowDOM(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/shadow.html");
    page.evaluate("async url => {\n" +
      "  const frame = document.createElement('iframe');\n" +
      "  frame.src = url;\n" +
      "  document.body.shadowRoot.appendChild(frame);\n" +
      "  await new Promise(x => frame.onload = x);\n" +
      "}", server.EMPTY_PAGE);
    assertEquals(2, page.frames().size());
    assertEquals(server.EMPTY_PAGE, page.frames().get(1).url());
  }

  @PlaywrightTest
  @DisplayName("FR-032 should report frame.name()")
  void shouldReportFrameName(Page page, TestServer server) {
    assumeFalse(Env.isBidi(), "frame.name() is racy with BiDi");

    attachFrame(page, "theFrameId", server.EMPTY_PAGE);
    page.evaluate("url => {\n" +
      "  const frame = document.createElement('iframe');\n" +
      "  frame.name = 'theFrameName';\n" +
      "  frame.src = url;\n" +
      "  document.body.appendChild(frame);\n" +
      "  return new Promise(x => frame.onload = x);\n" +
      "}", server.EMPTY_PAGE);
    assertEquals("", page.frames().get(0).name());
    assertEquals("theFrameId", page.frames().get(1).name());
    assertEquals("theFrameName", page.frames().get(2).name());
  }

  @PlaywrightTest
  @DisplayName("FR-033 should report frame.parent()")
  void shouldReportFrameParent(Page page, TestServer server) {
    attachFrame(page, "frame1", server.EMPTY_PAGE);
    attachFrame(page, "frame2", server.EMPTY_PAGE);
    assertNull(page.frames().get(0).parentFrame());
    assertEquals(page.mainFrame(), page.frames().get(1).parentFrame());
    assertEquals(page.mainFrame(), page.frames().get(2).parentFrame());
  }

  @PlaywrightTest
  @DisplayName("FR-034 should report different frame instance when frame re-attaches")
  void shouldReportDifferentFrameInstanceWhenFrameReAttaches(Page page, TestServer server) throws Exception {

    Frame frame1 = attachFrame(page, "frame1", server.EMPTY_PAGE);
    page.evaluate("() => {\n" +
      "  window['frame'] = document.querySelector('#frame1');\n" +
      "  window['frame'].remove();\n" +
      "}");
    assertEquals(true, frame1.isDetached());
    // Java: there is no page.waitForFrameAttached(); the future of an onFrameAttached handler stands for page.waitForEvent('frameattached')
    CompletableFuture<Frame> frameattached = new CompletableFuture<>();
    page.onFrameAttached(frame -> frameattached.complete(frame));
    page.evaluate("() => document.body.appendChild(window['frame'])");
    page.waitForCondition(frameattached::isDone);
    Frame frame2 = frameattached.get();
    assertEquals(false, frame2.isDetached());
    assertNotEquals(frame2, frame1);
  }

  @PlaywrightTest
  @DisplayName("FR-035 should refuse to display x-frame-options:deny iframe")
  @DisabledOnBrowser(BrowserName.FIREFOX)
  void shouldRefuseToDisplayXFrameOptionsDenyIframe(Page page, TestServer server) throws Exception {
    server.setRoute("/x-frame-options-deny.html", (req, res) -> {
      res.setHeader("Content-Type", "text/html");
      res.setHeader("X-Frame-Options", "DENY");
      res.end("<!DOCTYPE html><html><head><title>login</title></head><body style=\"background-color: red;\"><p>dangerous login page</p></body></html>");
    });
    page.navigate(server.EMPTY_PAGE);
    // Java: the promise that resolves on the first matching console message is page.waitForConsoleMessage() around the setContent call
    String refusalText = page.waitForConsoleMessage(
      new Page.WaitForConsoleMessageOptions().setPredicate(msg -> Pattern.compile("Refused to display", Pattern.CASE_INSENSITIVE).matcher(msg.text()).find()),
      () -> page.setContent("<iframe src=\"" + server.CROSS_PROCESS_PREFIX + "/x-frame-options-deny.html\"></iframe>")).text();
    assertTrue(Pattern.compile("Refused to display .* in a frame because it set 'X-Frame-Options' to 'deny'.", Pattern.CASE_INSENSITIVE).matcher(refusalText).find());
  }

  @PlaywrightTest
  @DisplayName("FR-036 should return frame.page()")
  void shouldReturnFramePage(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/frames/one-frame.html");
    assertEquals(page, page.mainFrame().page());
    assertEquals(page, page.mainFrame().childFrames().get(0).page());
  }
}
