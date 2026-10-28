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

import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Port of tests/library/page-clock.frozen.spec.ts. */
public class TestPageClockFrozen {
  @PlaywrightTest
  @DisplayName("CK-055 clock should be frozen")
  void clockShouldBeFrozen(Page page) {
    assumeTrue("frozen".equals(System.getenv("PW_CLOCK")));
    assertEquals(1000, page.evaluate("Date.now()"));
  }

  @PlaywrightTest
  @DisplayName("CK-056 clock should be realtime")
  void clockShouldBeRealtime(Page page) {
    assumeTrue("realtime".equals(System.getenv("PW_CLOCK")));
    assertTrue(((Number) page.evaluate("Date.now()")).doubleValue() < 10000);
  }
}
