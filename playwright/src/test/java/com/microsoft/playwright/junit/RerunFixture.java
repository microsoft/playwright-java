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

import com.microsoft.playwright.APIRequestContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

// Not picked up by surefire; TestFixturesRerun runs it through the launcher.
@UsePlaywright
public class RerunFixture {
  // Resolving the parameter already makes a driver round-trip (request().newContext()),
  // which is what fails when the Playwright left on the thread has been closed.
  @Test
  void usesAPIRequestContext(APIRequestContext request) {
    assertNotNull(request);
  }
}
