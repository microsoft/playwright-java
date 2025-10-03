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
import org.opentest4j.AssertionFailedError;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestExpectMisc extends TestBase {
  @Test
  void strictModeViolationErrorFormat() {
    page.setContent(" <div>hello</div><div>hi</div>");
    AssertionFailedError error = assertThrows(AssertionFailedError.class, () ->
      assertThat(page.locator("div")).isVisible());
    assertTrue(error.getMessage().contains("Locator expected to be visible"), error.getMessage());
    assertTrue(error.getMessage().contains("Error: strict mode violation: locator(\"div\") resolved to 2 elements:"), error.getMessage());
  }

  @Test
  void invalidSelectorErrorFormat() {
    page.setContent("<div>hello</div><div>hi</div>");
    AssertionFailedError error = assertThrows(AssertionFailedError.class, () ->
      assertThat(page.locator("##")).isVisible());
    assertTrue(error.getMessage().contains("Locator expected to be visible"), error.getMessage());
    assertTrue(error.getMessage().contains("Error: Unexpected token \"#\" while parsing css selector \"##\"."), error.getMessage());
  }

}
