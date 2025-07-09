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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static com.microsoft.playwright.Utils.assertJsonEquals;
import static com.microsoft.playwright.Utils.mapOf;
import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestBrowserContextStorageState extends TestBase {

  @Test
  void shouldCaptureLocalStorage() {
    page.route("**/*", route -> {
      route.fulfill(new Route.FulfillOptions().setBody("<html></html>"));
    });
    page.navigate("https://www.example.com");
    page.evaluate("localStorage['name1'] = 'value1';");
    page.navigate("https://www.domain.com");
    page.evaluate("localStorage['name2'] = 'value2';");
    String storageState = context.storageState();
    assertJsonEquals("{" +
      "cookies:[]," +
      "origins:[{\n" +
      "  origin: 'https://www.domain.com',\n" +
      "  localStorage: [{\n" +
      "    name: 'name2',\n" +
      "    value: 'value2'\n" +
      "  }]\n" +
      "}, {\n" +
      "  origin: 'https://www.example.com',\n" +
      "  localStorage: [{\n" +
      "    name: 'name1',\n" +
      "    value: 'value1'\n" +
      "  }]\n" +
      "}]}", new Gson().fromJson(storageState, JsonObject.class));
  }

  @Test
  void shouldSetLocalStorage() {
    String storageState = "{\n" +
      "  origins: [\n" +
      "    {\n" +
      "      origin: 'https://www.example.com',\n" +
      "      localStorage: [{\n" +
      "        name: 'name1',\n" +
      "        value: 'value1'\n" +
      "      }]\n" +
      "    }\n" +
      "  ]\n" +
      "}";
    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setStorageState(storageState));
    Page page = context.newPage();
    page.route("**/*", route -> {
      route.fulfill(new Route.FulfillOptions().setBody("<html></html>"));
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
      route.fulfill(new Route.FulfillOptions().setBody("<html></html>"));
    });
    page1.navigate("https://www.example.com");
    page1.evaluate("() => {\n" +
      "  localStorage['name1'] = 'value1';\n" +
      "  document.cookie = 'username=John Doe';\n" +
      "  return document.cookie;\n" +
      "}");
    Path path = tempDir.resolve("storage-state.json");
    context.storageState(new BrowserContext.StorageStateOptions().setPath(path));

    String sameSiteCamelCase = "Lax";
    switch (defaultSameSiteCookieValue) {
      case STRICT:
        sameSiteCamelCase = "Strict";
        break;
      case LAX:
        sameSiteCamelCase = "Lax";
        break;
      case NONE:
        sameSiteCamelCase = "None";
        break;
    }

    JsonObject expected = new Gson().fromJson(
      "{\n" +
      "  'cookies':[\n" +
      "    { \n" +
      "      'name':'username',\n" +
      "      'value':'John Doe',\n" +
      "      'domain':'www.example.com',\n" +
      "      'path':'/',\n" +
      "      'expires':-1,\n" +
      "      'httpOnly':false,\n" +
      "      'secure':false,\n" +
      "      'sameSite':'" + sameSiteCamelCase + "'\n" +
      "    }],\n" +
      "  'origins':[\n" +
      "    {\n" +
      "      'origin':'https://www.example.com',\n" +
      "      'localStorage':[\n" +
      "        {\n" +
      "          'name':'name1',\n" +
      "          'value':'value1'\n" +
      "        }]\n" +
      "    }]\n" +
      "}\n", JsonObject.class);
    try (InputStreamReader reader = new InputStreamReader(new FileInputStream(path.toFile()), StandardCharsets.UTF_8)) {
      assertEquals(expected, new Gson().fromJson(reader, JsonObject.class));
    }
    BrowserContext context2 = browser.newContext(new Browser.NewContextOptions().setStorageStatePath(path));
    Page page2 = context2.newPage();
    page2.route("**/*", route -> {
      route.fulfill(new Route.FulfillOptions().setBody("<html></html>"));
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

  @Test
  void shouldSerialiseStorageStateWithLoneSurrogates() {
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("chars => window.localStorage.setItem('foo', String.fromCharCode(55934))");
    String storageState = context.storageState();
    assertJsonEquals("{" +
      "cookies:[]," +
      "origins:[{\n" +
      "  origin: 'http://localhost:" + server.PORT + "',\n" +
      "  localStorage: [{\n" +
      "    name: 'foo',\n" +
      "    value: '" + (char)65533 + "'\n" +
      "  }]\n" +
      "}]}", new Gson().fromJson(storageState, JsonObject.class));
  }

  @Test
  void shouldSupportIndexedDB() {
    page.navigate(server.PREFIX + "/to-do-notifications/index.html");

    assertThat(page.locator("#notifications")).matchesAriaSnapshot(
      "    - list:\n" +
      "      - listitem: Database initialised."
    );
    page.locator("label:has-text('Task title')").fill("Pet the cat");
    page.locator("label:has-text('Hours')").fill("1");
    page.locator("label:has-text('Mins')").fill("1");
    page.locator("text=Add Task").click();
    assertThat(page.locator("#notifications")).matchesAriaSnapshot(
      "    - list:\n" +
      "      - listitem: \"Transaction completed: database modification finished.\""
    );

    String storageState = page.context().storageState(new BrowserContext.StorageStateOptions().setIndexedDB(true));
    assertJsonEquals("{\"cookies\":[],\"origins\":[\n" +
      "  {\n" +
      "    \"origin\": \"" + server.PREFIX + "\",\n" +
      "    \"localStorage\": [],\n" +
      "    \"indexedDB\": [\n" +
      "      {\n" +
      "        \"name\": \"toDoList\",\n" +
      "        \"version\": 4,\n" +
      "        \"stores\": [\n" +
      "          {\n" +
      "            \"name\": \"toDoList\",\n" +
      "            \"autoIncrement\": false,\n" +
      "            \"keyPath\": \"taskTitle\",\n" +
      "            \"records\": [\n" +
      "              {\n" +
      "                \"valueEncoded\": {\n" +
      "                  \"id\": 1,\n" +
      "                  \"o\": [\n" +
      "                    {\"k\": \"taskTitle\", \"v\": \"Pet the cat\"},\n" +
      "                    {\"k\": \"hours\", \"v\": \"1\"},\n" +
      "                    {\"k\": \"minutes\", \"v\": \"1\"},\n" +
      "                    {\"k\": \"day\", \"v\": \"01\"},\n" +
      "                    {\"k\": \"month\", \"v\": \"January\"},\n" +
      "                    {\"k\": \"year\", \"v\": \"2025\"},\n" +
      "                    {\"k\": \"notified\", \"v\": \"no\"},\n" +
      "                    {\"k\": \"signature\", \"v\": { \"ta\": {\"b\":\"c2lnbmVkIGJ5IHNpbW9u\",\"k\":\"ui8\"}}}\n" +
      "                  ]\n" +
      "                }\n" +
      "              }\n" +
      "            ],\n" +
      "            \"indexes\": [\n" +
      "              {\n" +
      "                \"name\": \"day\",\n" +
      "                \"keyPath\": \"day\",\n" +
      "                \"multiEntry\": false,\n" +
      "                \"unique\": false\n" +
      "              },\n" +
      "              {\n" +
      "                \"name\": \"hours\",\n" +
      "                \"keyPath\": \"hours\",\n" +
      "                \"multiEntry\": false,\n" +
      "                \"unique\": false\n" +
      "              },\n" +
      "              {\n" +
      "                \"name\": \"minutes\",\n" +
      "                \"keyPath\": \"minutes\",\n" +
      "                \"multiEntry\": false,\n" +
      "                \"unique\": false\n" +
      "              },\n" +
      "              {\n" +
      "                \"name\": \"month\",\n" +
      "                \"keyPath\": \"month\",\n" +
      "                \"multiEntry\": false,\n" +
      "                \"unique\": false\n" +
      "              },\n" +
      "              {\n" +
      "                \"name\": \"notified\",\n" +
      "                \"keyPath\": \"notified\",\n" +
      "                \"multiEntry\": false,\n" +
      "                \"unique\": false\n" +
      "              },\n" +
      "              {\n" +
      "                \"name\": \"year\",\n" +
      "                \"keyPath\": \"year\",\n" +
      "                \"multiEntry\": false,\n" +
      "                \"unique\": false\n" +
      "              }\n" +
      "            ]\n" +
      "          }\n" +
      "        ]\n" +
      "      }\n" +
      "    ]\n" +
      "  }\n" +
      "]}", new Gson().fromJson(storageState, JsonObject.class));

    BrowserContext context = browser.newContext(new Browser.NewContextOptions().setStorageState(storageState));
    assertEquals(storageState, context.storageState(new BrowserContext.StorageStateOptions().setIndexedDB(true)));

    Page recreatedPage = context.newPage();
    recreatedPage.navigate(server.PREFIX + "/to-do-notifications/index.html");
    assertThat(recreatedPage.locator("#task-list")).matchesAriaSnapshot("\n" +
      "    - list:\n" +
      "      - listitem:\n" +
      "        - text: /Pet the cat/\n");
    assertEquals("{\"cookies\":[],\"origins\":[]}", context.storageState(
      new BrowserContext.StorageStateOptions().setIndexedDB(false)));
  }
}
