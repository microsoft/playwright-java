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

import com.microsoft.playwright.options.PausedDetails;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestDebugger extends TestBase {
  @Test
  void shouldReturnEmptyPausedDetailsInitially() {
    Debugger dbg = context.debugger();
    assertEquals(Collections.emptyList(), dbg.pausedDetails());
  }

  @Test
  void shouldPauseAtPauseCall() {
    page.setContent("<div>click me</div>");
    Debugger dbg = context.debugger();
    assertEquals(Collections.emptyList(), dbg.pausedDetails());

    dbg.pause();

    boolean[] paused = {false};
    dbg.onPausedStateChanged(() -> {
      if (!paused[0]) {
        paused[0] = true;
        List<PausedDetails> details = dbg.pausedDetails();
        assertEquals(1, details.size());
        assertTrue(details.get(0).title.contains("Pause"), "title: " + details.get(0).title);
        dbg.resume();
      }
    });

    page.pause(); // blocks until dbg.resume() is called from event handler
    assertEquals(Collections.emptyList(), dbg.pausedDetails());
  }

  @Test
  void shouldPauseAtNextAndResume() {
    page.setContent("<div>click me</div>");
    Debugger dbg = context.debugger();
    assertEquals(Collections.emptyList(), dbg.pausedDetails());

    dbg.pause();

    boolean[] paused = {false};
    dbg.onPausedStateChanged(() -> {
      if (!paused[0]) {
        paused[0] = true;
        List<PausedDetails> details = dbg.pausedDetails();
        assertEquals(1, details.size());
        assertTrue(details.get(0).title.contains("Click"), "title: " + details.get(0).title);
        dbg.resume();
      }
    });

    page.click("div"); // blocks until dbg.resume() is called
    assertEquals(Collections.emptyList(), dbg.pausedDetails());
  }
}
