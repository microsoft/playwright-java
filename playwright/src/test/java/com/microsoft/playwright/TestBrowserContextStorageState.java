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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
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

  @Test
  void shouldRoundTripThroughTheFile(@TempDir Path tempDir) throws IOException {
    Page page1 = context.newPage();
    page1.route("**/*", route -> {
      route.fulfill(new Route.FulfillResponse().withBody("<html></html>"));
    });
    page1.navigate("https://www.example.com");
    page1.evaluate("() => {\n" +
      "  localStorage['name1'] = 'value1';\n" +
      "  document.cookie = 'username=John Doe';\n" +
      "  return document.cookie;\n" +
      "}");
    Path path = tempDir.resolve("storage-state.json");
    BrowserContext.StorageState state = context.storageState(new BrowserContext.StorageStateOptions().withPath(path));
    JsonObject expected = new Gson().fromJson(
      "{\n" +
      "  \"cookies\":[\n" +
      "    { \n" +
      "      \"name\":\"username\",\n" +
      "      \"value\":\"John Doe\",\n" +
      "      \"domain\":\"www.example.com\",\n" +
      "      \"path\":\"/\",\n" +
      "      \"expires\":-1,\n" +
      "      \"httpOnly\":false,\n" +
      "      \"secure\":false,\n" +
      "      \"sameSite\":\"None\"\n" +
      "    }],\n" +
      "  \"origins\":[\n" +
      "    {\n" +
      "      \"origin\":\"https://www.example.com\",\n" +
      "      \"localStorage\":[\n" +
      "        {\n" +
      "          \"name\":\"name1\",\n" +
      "          \"value\":\"value1\"\n" +
      "        }]\n" +
      "    }]\n" +
      "}\n", JsonObject.class);
    try (FileReader reader = new FileReader(path.toFile())) {
      assertEquals(expected, new Gson().fromJson(reader, JsonObject.class));
    }
    BrowserContext context2 = browser.newContext(new Browser.NewContextOptions().withStorageState(path));
    Page page2 = context2.newPage();
    page2.route("**/*", route -> {
      route.fulfill(new Route.FulfillResponse().withBody("<html></html>"));
    });
    page2.navigate("https://www.example.com");
    Object localStorage = page2.evaluate("window.localStorage");
    assertEquals(mapOf("name1", "value1"), localStorage);
    if (!isFirefox()) {
      // TODO: fails on bots with expected: <username=John Doe> but was: <>
      Object cookie = page2.evaluate("document.cookie");
      assertEquals("username=John Doe", cookie);
    }
    context2.close();
  }
}
