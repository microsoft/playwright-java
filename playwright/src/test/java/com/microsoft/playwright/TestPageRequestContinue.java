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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class TestPageRequestContinue extends TestBase {
  @Test
  void shouldNotThrowWhenContinuingAfterPageIsClosed() {
    boolean[] done = {false};
    page.route("**/*", route -> {
      page.close();
      route.resume();
      done[0] = true;
    });
    try {
      page.navigate(server.EMPTY_PAGE);
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("frame was detached"), e.getMessage());
    }
    assertTrue(done[0]);
  }
}
