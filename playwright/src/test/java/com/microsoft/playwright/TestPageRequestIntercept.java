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

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class TestPageRequestIntercept extends TestBase {
  @Test
  void shouldFulfillPopupMainRequestUsingAlias() {
    page.context().route("**/*", route -> {
      APIResponse response = route.fetch();
      route.fulfill(new Route.FulfillOptions().setResponse(response).setBody("hello" ));
    });
    page.setContent("<a target=_blank href='" + server.EMPTY_PAGE + "'>click me</a>");
    Page popup = page.waitForPopup(() -> page.getByText("click me").click());
    assertThat(popup.locator("body")).hasText("hello");
  }
}
