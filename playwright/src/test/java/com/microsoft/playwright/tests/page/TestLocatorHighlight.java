/*
 * Copyright Microsoft Corporation. All rights reserved.
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

import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.options.BoundingBox;
import org.junit.jupiter.api.DisplayName;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.roundBox;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** Port of tests/page/locator-highlight.spec.ts. */
public class TestLocatorHighlight {
  @PlaywrightTest
  @DisplayName("LO-114 should highlight locator")
  void shouldHighlightLocator(Page page) {
    page.setContent("<input type='text' />");
    page.locator("input").highlight();
    assertThat(page.locator("x-pw-tooltip")).hasText("locator('input')");
    assertThat(page.locator("x-pw-highlight")).isVisible();
    BoundingBox box1 = roundBox(page.locator("input").boundingBox());
    BoundingBox box2 = roundBox(page.locator("x-pw-highlight").boundingBox());
    // Java: BoundingBox has no equals(), compare the fields.
    assertEquals(box2.x, box1.x);
    assertEquals(box2.y, box1.y);
    assertEquals(box2.width, box1.width);
    assertEquals(box2.height, box1.height);
  }
}
