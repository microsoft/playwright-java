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

import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.junit.FixtureTest;
import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

// Copied from expect-misc.spec.ts > toBeInViewport
@FixtureTest
@UsePlaywright(TestOptionsFactories.BasicOptionsFactory.class)
public class TestAssertThatIsInViewport {
  @Test
  void shouldWork(Page page) {
    page.setContent("<div id=big style=\"height: 10000px;\"></div>\n" +
      "      <div id=small>foo</div>");
    assertThat(page.locator("#big")).isInViewport();
    assertThat(page.locator("#small")).not().isInViewport();
    page.locator("#small").scrollIntoViewIfNeeded();
    assertThat(page.locator("#small")).isInViewport();
    assertThat(page.locator("#small")).isInViewport(new LocatorAssertions.IsInViewportOptions().setRatio(1));
  }

  @Test
  void shouldRespectRatioOption(Page page) {
    page.setContent("<style>body, div, html { padding: 0; margin: 0; }</style>\n" +
      "      <div id=big style=\"height: 400vh;\"></div>");
    assertThat(page.locator("div")).isInViewport();
    assertThat(page.locator("div")).isInViewport(new LocatorAssertions.IsInViewportOptions().setRatio(0.1));
    assertThat(page.locator("div")).isInViewport(new LocatorAssertions.IsInViewportOptions().setRatio(0.2));

    assertThat(page.locator("div")).isInViewport(new LocatorAssertions.IsInViewportOptions().setRatio(0.24));
    // In this test, element's ratio is 0.25.
    assertThat(page.locator("div")).isInViewport(new LocatorAssertions.IsInViewportOptions().setRatio(0.25));
    assertThat(page.locator("div")).not().isInViewport(new LocatorAssertions.IsInViewportOptions().setRatio(0.26));

    assertThat(page.locator("div")).not().isInViewport(new LocatorAssertions.IsInViewportOptions().setRatio(0.3));
    assertThat(page.locator("div")).not().isInViewport(new LocatorAssertions.IsInViewportOptions().setRatio(0.7));
    assertThat(page.locator("div")).not().isInViewport(new LocatorAssertions.IsInViewportOptions().setRatio(0.8));
  }

  @Test
  void shouldHaveGoodStack(Page page) {
    AssertionFailedError error = assertThrows(AssertionFailedError.class, () -> assertThat(page.locator("body")).not().isInViewport(new LocatorAssertions.IsInViewportOptions().setTimeout(100)));
    assertNotNull(error);
    assertTrue(error.getMessage().contains("Locator expected not to be in viewport"), error.getMessage());
  }

  @Test
  void shouldReportIntersectionEvenIfFullyCoveredByOtherElement(Page page) {
    page.setContent("<h1>hello</h1>\n" +
      "      <div style=\"position: relative; height: 10000px; top: -5000px;></div>");
    assertThat(page.locator("h1")).isInViewport();
  }
}
