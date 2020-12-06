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

import java.util.Arrays;

import static com.microsoft.playwright.Utils.assertJsonEquals;
import static com.microsoft.playwright.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestBrowserContextStorageState extends TestBase {

  @Test
  void shouldCaptureLocalStorage() {
    page.route("**/*", route -> {
      route.fulfill(new Route.FulfillResponse().withBody("<html></html>"));
    });
    page.navigate("https://www.example.com");
    page.evaluate("localStorage['name1'] = 'value1';");
    page.navigate("https://www.domain.com");
    page.evaluate("localStorage['name2'] = 'value2';");
    BrowserContext.StorageState storageState = context.storageState();

    assertJsonEquals("[{\n" +
      "  origin: 'https://www.example.com',\n" +
      "  localStorage: [{\n" +
      "    name: 'name1',\n" +
      "    value: 'value1'\n" +
      "  }]\n" +
      "}, {\n" +
      "  origin: 'https://www.domain.com',\n" +
      "  localStorage: [{\n" +
      "    name: 'name2',\n" +
      "    value: 'value2'\n" +
      "  }]\n" +
      "}]", storageState.origins());
  }

  @Test
  void shouldSetLocalStorage() {
    BrowserContext.StorageState storageState =  new BrowserContext.StorageState();
    storageState.origins.add(new BrowserContext.StorageState.OriginState("https://www.example.com")
      .withLocalStorage(Arrays.asList(
        new BrowserContext.StorageState.OriginState.LocalStorageItem("name1", "value1"))));
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().withStorageState(storageState));
    Page page = context.newPage();
    page.route("**/*", route -> {
      route.fulfill(new Route.FulfillResponse().withBody("<html></html>"));
    });
    page.navigate("https://www.example.com");
    Object localStorage = page.evaluate("window.localStorage");
    assertEquals(mapOf("name1", "value1"), localStorage);
    context.close();
  }
}
