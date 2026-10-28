/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
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

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.Request;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Port of tests/page/network-post-data.spec.ts. */
public class TestNetworkPostData {
  @PlaywrightTest
  @DisplayName("NE-112 should get post data for file/blob")
  @DisabledOnBrowser(value = {BrowserName.WEBKIT, BrowserName.CHROMIUM}, reason = "fail")
  void shouldGetPostDataForFileBlob(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Request request = page.waitForRequest("**/*", () -> {
      page.evaluate("() => {\n" +
        "  const file = new File(['file-contents'], 'filename.txt');\n" +
        "\n" +
        "  void fetch('/data', {\n" +
        "    method: 'POST',\n" +
        "    headers: {\n" +
        "      'content-type': 'application/octet-stream'\n" +
        "    },\n" +
        "    body: file\n" +
        "  });\n" +
        "}");
    });
    assertEquals("file-contents", request.postData());
  }
}
