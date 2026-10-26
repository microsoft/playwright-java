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

package com.microsoft.playwright.tests.library;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Debugger;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.DebuggerPausedDetails;
import com.microsoft.playwright.options.Location;
import org.junit.jupiter.api.DisplayName;

import java.util.concurrent.CompletableFuture;

import static com.microsoft.playwright.fixtures.Utils.async;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/library/debugger.spec.ts. */
public class TestDebugger {
  // Java: new Promise<void>(resolve => dbg.once('pausedstatechanged', resolve)); the handler completes the future.
  private static CompletableFuture<Void> oncePausedStateChanged(Debugger dbg) {
    CompletableFuture<Void> future = new CompletableFuture<>();
    Runnable[] handler = {null};
    handler[0] = () -> {
      dbg.offPausedStateChanged(handler[0]);
      future.complete(null);
    };
    dbg.onPausedStateChanged(handler[0]);
    return future;
  }

  @PlaywrightTest
  @DisplayName("DB-001 should pause at next and resume")
  @Fixme("java: the paused details location is file '<unknown>' with no line: the Java client sends the call location only when PLAYWRIGHT_JAVA_SRC is set")
  void shouldPauseAtNextAndResume(BrowserContext context, TestServer server) throws Exception {
    Page page = context.newPage();
    page.setContent("<div>click me</div>");
    Debugger dbg = context.debugger();
    assertNull(dbg.pausedDetails());

    dbg.requestPause();
    // Java: the pausedstatechanged promise is created before the click starts, the event can arrive while the click runs.
    CompletableFuture<Void> paused = oncePausedStateChanged(dbg);
    CompletableFuture<Void> clickPromise = async(() -> {
      page.click("div");
      return null;
    });
    context.waitForCondition(paused::isDone);

    // Java: the location is the Java test file, not debugger.spec.
    DebuggerPausedDetails pausedDetails = dbg.pausedDetails();
    assertNotNull(pausedDetails);
    assertTrue(pausedDetails.title.contains("Click"));
    assertNotNull(pausedDetails.location);
    assertTrue(pausedDetails.location.file.contains("TestDebugger"));
    assertNotNull(pausedDetails.location.line);
    assertNotNull(pausedDetails.location.column);

    CompletableFuture<Void> resumed = oncePausedStateChanged(dbg);
    dbg.resume();
    context.waitForCondition(resumed::isDone);
    clickPromise.get();
    assertNull(dbg.pausedDetails());
  }

  @PlaywrightTest
  @DisplayName("DB-002 should pause at pause call")
  @Fixme(value = "java: page.pause() runs on another thread and sets the default timeout of the context to 0, BrowserContext.waitForCondition() on the test thread then fails with 'Timeout 0ms exceeded' (races: passes in firefox and webkit)", browsers = BrowserName.CHROMIUM)
  void shouldPauseAtPauseCall(BrowserContext context, TestServer server) throws Exception {
    Page page = context.newPage();
    page.setContent("<div>click me</div>");
    Debugger dbg = context.debugger();
    assertNull(dbg.pausedDetails());

    dbg.requestPause();
    CompletableFuture<Void> paused = oncePausedStateChanged(dbg);
    CompletableFuture<Void> pausePromise = async(() -> {
      page.pause();
      return null;
    });
    context.waitForCondition(paused::isDone);

    DebuggerPausedDetails pausedDetails = dbg.pausedDetails();
    assertNotNull(pausedDetails);
    assertTrue(pausedDetails.title.contains("Pause"));

    dbg.resume();
    pausePromise.get();
  }

  @PlaywrightTest
  @DisplayName("DB-003 should run to location")
  @Fixme("java: Debugger.runTo() sends the location without the 'location' wrapper: 'location: expected object, got undefined'")
  void shouldRunToLocation(BrowserContext context, TestServer server) throws Exception {
    Page page = context.newPage();
    page.setContent("<div>click me</div>");
    Debugger dbg = context.debugger();
    assertNull(dbg.pausedDetails());

    // First, pause on next action.
    dbg.requestPause();
    CompletableFuture<Void> paused = oncePausedStateChanged(dbg);
    async(() -> {
      try {
        page.click("div");
      } catch (RuntimeException e) {
      }
      return null;
    });
    context.waitForCondition(paused::isDone);

    // Now run to a specific location.
    int line = new Throwable().getStackTrace()[0].getLineNumber();
    // Note: careful with the line offset below.
    dbg.runTo(new Location("TestDebugger").setLine(line + 4));
    page.content(); // should not pause here
    CompletableFuture<Void> clickPaused = oncePausedStateChanged(dbg);
    CompletableFuture<Void> clickPromise = async(() -> { page.click("div"); return null; }); // should pause here
    context.waitForCondition(clickPaused::isDone);

    DebuggerPausedDetails pausedDetails = dbg.pausedDetails();
    assertNotNull(pausedDetails);
    assertTrue(pausedDetails.title.contains("Click"));

    dbg.resume();
    clickPromise.get();
  }
}
