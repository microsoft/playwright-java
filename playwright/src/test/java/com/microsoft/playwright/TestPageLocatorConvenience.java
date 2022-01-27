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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestPageLocatorConvenience extends TestBase {
  @Test
  void shouldReturnPage() {
    page.navigate(server.PREFIX + "/frames/two-frames.html");
    Locator outer = page.locator("#outer");
    assertEquals(page, outer.page());
    Locator inner = outer.locator("#inner");
    assertEquals(page, inner.page());
    Locator inFrame = page.frames().get(1).locator("div");
    assertEquals(page, inFrame.page());
  }
}
