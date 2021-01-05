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

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class TestDeferred extends TestBase {
  @Test
  void throwIfGetNotCalled() {
    page.waitForNavigation();
    context.futureEvent(BrowserContext.EventType.PAGE);
    closeContext();
    closeBrowser();

    AtomicBoolean didFinalize = new AtomicBoolean();
    new Object() {
      @Override
      protected void finalize() {
        didFinalize.set(true);
      }
    };
    while (!didFinalize.get()) {
      System.gc();
    }

    Playwright p = playwright;
    playwright = null;
    try {
      p.close();
      fail("did not throw");
    } catch (Exception e) {
      assertTrue(e.getMessage().contains("com.microsoft.playwright.impl.PageImpl.waitForNavigation"));
      assertTrue(e.getMessage().contains("com.microsoft.playwright.impl.BrowserContextImpl.futureEvent"));
    }
  }
}

