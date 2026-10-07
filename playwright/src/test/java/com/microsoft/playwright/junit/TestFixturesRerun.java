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


package com.microsoft.playwright.junit;

import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

public class TestFixturesRerun {
  // Surefire's rerunFailingTestsCount runs the launcher again on the same thread after
  // the first run has already closed every Playwright it created.
  @Test
  void shouldCreateNewPlaywrightForEachLauncherRun() throws Exception {
    ExecutorService thread = Executors.newSingleThreadExecutor();
    try {
      for (int run = 1; run <= 2; run++) {
        Future<TestExecutionSummary> summary = thread.submit(() -> runOnLauncher(RerunFixture.class));
        assertEquals(1, summary.get().getTestsSucceededCount(), "run " + run + ": " + describeFailures(summary.get()));
      }
    } finally {
      thread.shutdownNow();
    }
  }

  private static String describeFailures(TestExecutionSummary summary) {
    StringWriter out = new StringWriter();
    summary.printFailuresTo(new PrintWriter(out));
    return out.toString();
  }

  private static TestExecutionSummary runOnLauncher(Class<?> testClass) {
    LauncherDiscoveryRequest request = request().selectors(selectClass(testClass)).build();
    SummaryGeneratingListener listener = new SummaryGeneratingListener();
    LauncherFactory.create().execute(request, listener);
    return listener.getSummary();
  }
}
