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

import com.microsoft.playwright.options.DebuggerPausedDetails;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestDebugger extends TestBase {
  @Test
  void shouldReturnNullPausedDetailsInitially() {
    Debugger dbg = context.debugger();
    assertNull(dbg.pausedDetails());
  }

  @Test
  void shouldPauseAtNextAndResume() {
    page.setContent("<div>click me</div>");
    Debugger dbg = context.debugger();
    assertNull(dbg.pausedDetails());

    dbg.requestPause();

    boolean[] paused = {false};
    dbg.onPausedStateChanged(() -> {
      if (!paused[0]) {
        paused[0] = true;
        DebuggerPausedDetails details = dbg.pausedDetails();
        assertNotNull(details);
        assertTrue(details.title.contains("Click"), "title: " + details.title);
        dbg.resume();
      }
    });

    page.click("div"); // blocks until dbg.resume() is called
    assertNull(dbg.pausedDetails());
  }

  @Test
  void shouldStepWithNext() {
    page.setContent("<div>click me</div>");
    Debugger dbg = context.debugger();
    assertNull(dbg.pausedDetails());

    dbg.requestPause();

    boolean[] paused = {false};
    dbg.onPausedStateChanged(() -> {
      if (!paused[0]) {
        paused[0] = true;
        DebuggerPausedDetails details = dbg.pausedDetails();
        assertNotNull(details);
        assertTrue(details.title.contains("Click"), "title: " + details.title);
        dbg.next();
      } else if (dbg.pausedDetails() != null) {
        dbg.resume();
      }
    });

    page.click("div");
    assertNull(dbg.pausedDetails());
  }

  @Test
  void shouldPauseAtPauseCall() {
    page.setContent("<div>click me</div>");
    Debugger dbg = context.debugger();
    assertNull(dbg.pausedDetails());

    dbg.requestPause();

    boolean[] paused = {false};
    dbg.onPausedStateChanged(() -> {
      if (!paused[0]) {
        paused[0] = true;
        DebuggerPausedDetails details = dbg.pausedDetails();
        assertNotNull(details);
        assertTrue(details.title.contains("Pause"), "title: " + details.title);
        dbg.resume();
      }
    });

    page.pause(); // blocks until dbg.resume() is called from event handler
    assertNull(dbg.pausedDetails());
  }
}
