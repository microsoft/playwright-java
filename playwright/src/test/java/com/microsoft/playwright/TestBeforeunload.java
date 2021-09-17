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

public class TestBeforeunload extends TestBase {
  @Test
  void shouldBeAbleToNavigateAwayFromPageWithBeforeunload() {
    page.navigate(server.PREFIX + "/beforeunload.html");
    // We have to interact with a page so that "beforeunload" handlers
    // fire.
    page.click("body");
    page.navigate(server.EMPTY_PAGE);
  }
}
