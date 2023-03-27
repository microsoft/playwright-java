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

import static com.microsoft.playwright.options.WaitUntilState.NETWORKIDLE;
import static org.junit.jupiter.api.Assertions.*;

public class TestFrameNavigate extends TestBase {

  @Test
  void shouldNavigateSubframes() {
    page.navigate(server.PREFIX + "/frames/one-frame.html");
    assertTrue(page.frames().get(0).url().contains("/frames/one-frame.html"));
    assertTrue(page.frames().get(1).url().contains("/frames/frame.html"));

    Response response = page.frames().get(1).navigate(server.EMPTY_PAGE);
    assertTrue(response.ok());
    assertEquals(page.frames().get(1), response.frame());
  }

  // TODO: not supported in sync api
  void shouldRejectWhenFrameDetaches() {
  }

  @Test
  void shouldContinueAfterClientRedirect() {
    server.setRoute("/frames/script.js", (httpExchange) -> {});
    String url = server.PREFIX + "/frames/child-redirect.html";
    TimeoutError e = assertThrows(TimeoutError.class, () ->  {
      page.navigate(url, new Page.NavigateOptions().setTimeout(5000).setWaitUntil(NETWORKIDLE));
    });
    assertTrue(e.getMessage().contains("Timeout 5000ms exceeded."));
    assertTrue(e.getMessage().contains("navigating to \"" + url +"\", waiting until \"networkidle\""));
  }

  // TODO: not supported in sync api
  void shouldReturnMatchingResponses() {
  }
}
