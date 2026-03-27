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

import com.microsoft.playwright.options.*;
import java.util.*;

/**
 * API for controlling the Playwright debugger. The debugger allows pausing script execution and inspecting the page.
 * Obtain the debugger instance via {@link com.microsoft.playwright.BrowserContext#debugger BrowserContext.debugger()}.
 */
public interface Debugger {

  /**
   * Emitted when the debugger pauses or resumes.
   */
  void onPausedStateChanged(Runnable handler);
  /**
   * Removes handler that was previously added with {@link #onPausedStateChanged onPausedStateChanged(handler)}.
   */
  void offPausedStateChanged(Runnable handler);

  /**
   * Returns details about the currently paused call. Returns {@code null} if the debugger is not paused.
   *
   * @since v1.59
   */
  PausedDetails pausedDetails();
  /**
   * Configures the debugger to pause before the next action is executed.
   *
   * <p> Throws if the debugger is already paused. Use {@link com.microsoft.playwright.Debugger#next Debugger.next()} or {@link
   * com.microsoft.playwright.Debugger#runTo Debugger.runTo()} to step while paused.
   *
   * <p> Note that {@link com.microsoft.playwright.Page#pause Page.pause()} is equivalent to a "debugger" statement — it pauses
   * execution at the call site immediately. On the contrary, {@link com.microsoft.playwright.Debugger#requestPause
   * Debugger.requestPause()} is equivalent to "pause on next statement" — it configures the debugger to pause before the
   * next action is executed.
   *
   * @since v1.59
   */
  void requestPause();
  /**
   * Resumes script execution. Throws if the debugger is not paused.
   *
   * @since v1.59
   */
  void resume();
  /**
   * Resumes script execution and pauses again before the next action. Throws if the debugger is not paused.
   *
   * @since v1.59
   */
  void next();
  /**
   * Resumes script execution and pauses when an action originates from the given source location. Throws if the debugger is
   * not paused.
   *
   * @param location The source location to pause at.
   * @since v1.59
   */
  void runTo(Location location);
}

