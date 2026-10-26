/**
 * Copyright (c) Microsoft Corporation.
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

package com.microsoft.playwright.tests.library;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Port of tests/library/shared-worker.spec.ts. */
public class TestSharedWorker {
  @PlaywrightTest
  @DisplayName("WO-026 should survive shared worker restart")
  void shouldSurviveSharedWorkerRestart(BrowserContext context, TestServer server) {
    Page page1 = context.newPage();
    page1.navigate(server.PREFIX + "/shared-worker/shared-worker.html");
    assertEquals("echo:hello", page1.evaluate("window.sharedWorkerResponsePromise"));
    page1.close();

    Page page2 = context.newPage();
    page2.navigate(server.PREFIX + "/shared-worker/shared-worker.html");
    assertEquals("echo:hello", page2.evaluate("window.sharedWorkerResponsePromise"));
    page2.close();
  }
}
