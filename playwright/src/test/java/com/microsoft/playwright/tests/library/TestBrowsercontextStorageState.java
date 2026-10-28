/**
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

package com.microsoft.playwright.tests.library;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.ContextFactory;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestInfo;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.VirtualCredential;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static com.microsoft.playwright.fixtures.Utils.attachFrame;
import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/library/browsercontext-storage-state.spec.ts. */
public class TestBrowsercontextStorageState {
  // Java: the storage state is a JSON string. A JS object literal is parsed leniently (single quotes,
  // unquoted keys) and compared as a JSON tree, which is what toEqual does on the parsed object.
  private static JsonElement js(String literal) {
    // Trailing commas of the JS literals are dropped, they are not valid in the lenient JSON of Gson.
    return new JsonParser().parse(literal.replaceAll(",(\\s*[}\\]])", "$1"));
  }

  private static JsonObject state(String storageState) {
    return js(storageState).getAsJsonObject();
  }

  // Java: the storage state of an option object or of setStorageState() is a JSON file or string.
  private static Path writeState(TestInfo testInfo, String name, String json) {
    Path file = testInfo.outputPath(name);
    try {
      Files.write(file, json.getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return file;
  }

  @PlaywrightTest
  @DisplayName("ST-001 should capture local storage")
  void shouldCaptureLocalStorage(ContextFactory contextFactory) {
    BrowserContext context = contextFactory.newContext();
    Page page1 = context.newPage();
    page1.route("**/*", route -> {
      try {
        route.fulfill(new Route.FulfillOptions().setBody("<html></html>"));
      } catch (PlaywrightException e) {
      }
    });
    page1.navigate("https://www.example.com");
    page1.evaluate("() => {\n" +
      "  localStorage['name1'] = 'value1';\n" +
      "}");
    page1.navigate("https://www.domain.com");
    page1.evaluate("() => {\n" +
      "  localStorage['name2'] = 'value2';\n" +
      "}");
    JsonElement origins = state(context.storageState()).get("origins");
    assertEquals(js("[{\n" +
      "  origin: 'https://www.domain.com',\n" +
      "  localStorage: [{\n" +
      "    name: 'name2',\n" +
      "    value: 'value2'\n" +
      "  }],\n" +
      "}, {\n" +
      "  origin: 'https://www.example.com',\n" +
      "  localStorage: [{\n" +
      "    name: 'name1',\n" +
      "    value: 'value1'\n" +
      "  }],\n" +
      "}]"), origins);
  }

  @PlaywrightTest
  @DisplayName("ST-002 should set local storage")
  void shouldSetLocalStorage(ContextFactory contextFactory, TestInfo testInfo) {
    assumeFalse(Env.channel() != null && Env.channel().startsWith("msedge"), "fixme: Network.clearBrowserCache sometimes stalls");

    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions().setStorageState(js("{\n" +
      "  cookies: [],\n" +
      "  origins: [\n" +
      "    {\n" +
      "      origin: 'https://www.example.com',\n" +
      "      localStorage: [{\n" +
      "        name: 'name1',\n" +
      "        value: 'value1'\n" +
      "      }]\n" +
      "    },\n" +
      "  ]\n" +
      "}").toString()));
    Page page = context.newPage();
    page.route("**/*", route -> {
      try {
        route.fulfill(new Route.FulfillOptions().setBody("<html></html>"));
      } catch (PlaywrightException e) {
      }
    });
    page.navigate("https://www.example.com");
    Object localStorage = page.evaluate("window.localStorage");
    assertEquals(mapOf("name1", "value1"), localStorage);

    // Now use setStorageState to replace the storage
    // Java: setStorageState() takes a file.
    context.setStorageState(writeState(testInfo, "state.json", "{\n" +
      "  \"cookies\": [],\n" +
      "  \"origins\": [\n" +
      "    {\n" +
      "      \"origin\": \"https://www.example.com\",\n" +
      "      \"localStorage\": [{\n" +
      "        \"name\": \"name2\",\n" +
      "        \"value\": \"value2\"\n" +
      "      }]\n" +
      "    }\n" +
      "  ]\n" +
      "}"));
    assertEquals(1, context.pages().size());
    page.navigate("https://www.example.com");
    Object localStorage2 = page.evaluate("window.localStorage");
    assertEquals(mapOf("name2", "value2"), localStorage2);

    context.close();
  }

  @PlaywrightTest
  @DisplayName("ST-003 should report good error if the url is not valid")
  @Fixme("java: the message of a driver error is the serialized error (\"Error {\\n  message='Error setting storage state:...\"), without the 'browser.newContext: ' prefix")
  void shouldReportGoodErrorIfTheUrlIsNotValid(ContextFactory contextFactory) {
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> contextFactory.newContext(new Browser.NewContextOptions().setStorageState(js("{\n" +
      "  cookies: [],\n" +
      "  origins: [\n" +
      "    {\n" +
      "      origin: 'foo',\n" +
      "      localStorage: [{\n" +
      "        name: 'name1',\n" +
      "        value: 'value1'\n" +
      "      }]\n" +
      "    },\n" +
      "  ]\n" +
      "}").toString())));
    assertTrue(error.getMessage().contains("browser.newContext: Error setting storage state:"));
    assertTrue(error.getMessage().contains("foo"));
  }

  private static void checkContextRoundTrip(BrowserContext context) {
    Page page = context.newPage();
    page.route("**/*", route -> {
      try {
        route.fulfill(new Route.FulfillOptions().setBody("<html></html>"));
      } catch (PlaywrightException e) {
      }
    });
    page.navigate("https://www.example.com");
    Object localStorage = page.evaluate("window.localStorage");
    assertEquals(mapOf("name1", "value1"), localStorage);
    Object cookie = page.evaluate("document.cookie");
    assertEquals("username=John Doe", cookie);
    Object idbValues = page.evaluate("() => new Promise((resolve, reject) => {\n" +
      "  const openRequest = indexedDB.open('db', 42);\n" +
      "  openRequest.addEventListener('success', async () => {\n" +
      "    const db = openRequest.result;\n" +
      "    const transaction = db.transaction(['store', 'store2'], 'readonly');\n" +
      "    const request1 = transaction.objectStore('store').get('foo');\n" +
      "    const request2 = transaction.objectStore('store2').get('foo');\n" +
      "\n" +
      "    const [result1, result2] = await Promise.all([request1, request2].map(request => new Promise((resolve, reject) => {\n" +
      "      request.addEventListener('success', () => resolve(request.result));\n" +
      "      request.addEventListener('error', () => reject(request.error));\n" +
      "    })));\n" +
      "\n" +
      "    resolve([result1, new TextDecoder().decode(result2)]);\n" +
      "  });\n" +
      "  openRequest.addEventListener('error', () => reject(openRequest.error));\n" +
      "})");
    assertEquals(asList(
      mapOf("name", "foo", "date", new Date(0), "null", null),
      "bar"), idbValues);
  }

  @PlaywrightTest
  @DisplayName("ST-004 should round-trip through the file")
  @Fixme("java: BrowserContext.storageState(path) writes the compact JSON of the driver, JS writes JSON.stringify(state, undefined, 2)")
  void shouldRoundTripThroughTheFile(ContextFactory contextFactory, TestInfo testInfo) throws Exception {
    assumeFalse(Env.channel() != null && Env.channel().startsWith("msedge"), "fixme: Network.clearBrowserCache sometimes stalls");

    BrowserContext context = contextFactory.newContext();
    Page page1 = context.newPage();
    page1.route("**/*", route -> {
      try {
        route.fulfill(new Route.FulfillOptions().setBody("<html></html>"));
      } catch (PlaywrightException e) {
      }
    });
    page1.navigate("https://www.example.com");
    page1.evaluate("async () => {\n" +
      "  localStorage['name1'] = 'value1';\n" +
      "  document.cookie = 'username=John Doe';\n" +
      "\n" +
      "  await new Promise((resolve, reject) => {\n" +
      "    const openRequest = indexedDB.open('db', 42);\n" +
      "    openRequest.onupgradeneeded = () => {\n" +
      "      openRequest.result.createObjectStore('store', { keyPath: 'name' });\n" +
      "      openRequest.result.createObjectStore('store2');\n" +
      "    };\n" +
      "    openRequest.onsuccess = () => {\n" +
      "      const transaction = openRequest.result.transaction(['store', 'store2'], 'readwrite');\n" +
      "      transaction\n" +
      "          .objectStore('store')\n" +
      "          .put({ name: 'foo', date: new Date(0), null: null });\n" +
      "      transaction\n" +
      "          .objectStore('store2')\n" +
      "          .put(new TextEncoder().encode('bar'), 'foo');\n" +
      "      transaction.addEventListener('complete', resolve);\n" +
      "      transaction.addEventListener('error', reject);\n" +
      "    };\n" +
      "  });\n" +
      "\n" +
      "  return document.cookie;\n" +
      "}");

    Path path = testInfo.outputPath("storage-state.json");
    String state = context.storageState(new BrowserContext.StorageStateOptions().setPath(path).setIndexedDB(true));
    String written = new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    // Java: JSON.stringify(state, undefined, 2)
    assertEquals(new GsonBuilder().setPrettyPrinting().create().toJson(js(state)), written);

    BrowserContext context2 = contextFactory.newContext(new Browser.NewContextOptions().setStorageStatePath(path));
    checkContextRoundTrip(context2);
    context2.close();

    BrowserContext context3 = contextFactory.newContext();
    context3.setStorageState(path);
    assertEquals(0, context3.pages().size());
    checkContextRoundTrip(context3);
    context3.close();
  }

  private static void checkOpfsContext(BrowserContext context, String storageState, TestServer server) {
    assertEquals(js(storageState), js(context.storageState(new BrowserContext.StorageStateOptions().setOpfs(true))));
    Page checkPage = context.newPage();
    checkPage.navigate(server.EMPTY_PAGE);
    assertEquals(mapOf(
      "text", "Hello, world!",
      "bytes", asList(0, 1, 2, 255),
      "empty", new ArrayList<Object>()), checkPage.evaluate("async () => {\n" +
      "  const root = await navigator.storage.getDirectory();\n" +
      "  const hello = await (await root.getFileHandle('hello.txt')).getFile();\n" +
      "  const nested = await root.getDirectoryHandle('nested');\n" +
      "  const data = await (await nested.getFileHandle('data.bin')).getFile();\n" +
      "  const empty = await nested.getDirectoryHandle('empty');\n" +
      "  const emptyEntries = [];\n" +
      "  for await (const name of empty.keys())\n" +
      "    emptyEntries.push(name);\n" +
      "  return {\n" +
      "    text: await hello.text(),\n" +
      "    bytes: [...new Uint8Array(await data.arrayBuffer())],\n" +
      "    empty: emptyEntries,\n" +
      "  };\n" +
      "}"));
  }

  @PlaywrightTest
  @DisplayName("ST-005 should round-trip OPFS")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "OPFS is unavailable in non-persistent WebKit contexts")
  void shouldRoundTripOPFS(BrowserName browserName, ContextFactory contextFactory, Page page, TestServer server, TestInfo testInfo) throws Exception {
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("async () => {\n" +
      "  const root = await navigator.storage.getDirectory();\n" +
      "  const nested = await root.getDirectoryHandle('nested', { create: true });\n" +
      "  await nested.getDirectoryHandle('empty', { create: true });\n" +
      "\n" +
      "  const binary = await nested.getFileHandle('data.bin', { create: true });\n" +
      "  const binaryWritable = await binary.createWritable();\n" +
      "  await binaryWritable.write(new Uint8Array([0, 1, 2, 255]));\n" +
      "  await binaryWritable.close();\n" +
      "\n" +
      "  const text = await root.getFileHandle('hello.txt', { create: true });\n" +
      "  const textWritable = await text.createWritable();\n" +
      "  await textWritable.write('Hello, world!');\n" +
      "  await textWritable.close();\n" +
      "}");

    assertEquals(js("{ cookies: [], origins: [] }"), js(page.context().storageState()));

    Path path = testInfo.outputPath("storage-state.json");
    String storageState = page.context().storageState(new BrowserContext.StorageStateOptions().setPath(path).setOpfs(true));
    assertEquals(js("[{\n" +
      "  origin: '" + server.PREFIX + "',\n" +
      "  localStorage: [],\n" +
      "  opfs: [\n" +
      "    { path: 'hello.txt', type: 'file', base64: 'SGVsbG8sIHdvcmxkIQ==' },\n" +
      "    { path: 'nested', type: 'directory' },\n" +
      "    { path: 'nested/data.bin', type: 'file', base64: 'AAEC/w==' },\n" +
      "    { path: 'nested/empty', type: 'directory' },\n" +
      "  ],\n" +
      "}]"), state(storageState).get("origins"));
    assertEquals(js(storageState), js(new String(Files.readAllBytes(path), StandardCharsets.UTF_8)));
    assertEquals(js(storageState), js(page.context().request().storageState(new APIRequestContext.StorageStateOptions().setOpfs(true))));

    BrowserContext context2 = contextFactory.newContext(new Browser.NewContextOptions().setStorageStatePath(path));
    checkOpfsContext(context2, storageState, server);
    context2.close();

    BrowserContext context3 = contextFactory.newContext();
    Page page3 = context3.newPage();
    page3.navigate(server.EMPTY_PAGE);
    page3.evaluate("async () => {\n" +
      "  const root = await navigator.storage.getDirectory();\n" +
      "  await root.getFileHandle('stale.txt', { create: true });\n" +
      "}");
    // Java: setStorageState() takes a file.
    context3.setStorageState(writeState(testInfo, "storage-state-3.json", storageState));
    checkOpfsContext(context3, storageState, server);
    context3.close();
  }

  @PlaywrightTest
  @DisplayName("ST-006 should capture cookies")
  void shouldCaptureCookies(TestServer server, BrowserContext context, Page page, ContextFactory contextFactory) {
    server.setRoute("/setcookie.html", (req, res) -> {
      res.setHeader("Set-Cookie", asList("a=b", "empty="));
      res.end();
    });

    page.navigate(server.PREFIX + "/setcookie.html");
    assertEquals(asList("a=b", "empty="), page.evaluate("() => {\n" +
      "  const cookies = document.cookie.split(';');\n" +
      "  return cookies.map(cookie => cookie.trim()).sort();\n" +
      "}"));

    String storageState = context.storageState();
    // Java: new Set(cookies) toEqual a Set of objectContaining({ name, value }) compares the name/value pairs.
    Map<String, String> cookies = new HashMap<>();
    JsonArray cookiesArray = state(storageState).getAsJsonArray("cookies");
    for (JsonElement cookie : cookiesArray) {
      cookies.put(cookie.getAsJsonObject().get("name").getAsString(), cookie.getAsJsonObject().get("value").getAsString());
    }
    assertEquals(2, cookiesArray.size());
    assertEquals(mapOf("a", "b", "empty", ""), cookies);
    BrowserContext context2 = contextFactory.newContext(new Browser.NewContextOptions().setStorageState(storageState));
    Page page2 = context2.newPage();
    page2.navigate(server.EMPTY_PAGE);
    assertEquals(asList("a=b", "empty="), page2.evaluate("() => {\n" +
      "  const cookies = document.cookie.split(';');\n" +
      "  return cookies.map(cookie => cookie.trim()).sort();\n" +
      "}"));
  }

  @PlaywrightTest
  @DisplayName("ST-007 should not emit events about internal page")
  void shouldNotEmitEventsAboutInternalPage(ContextFactory contextFactory) {
    BrowserContext context = contextFactory.newContext();
    Page page = context.newPage();
    page.route("**/*", route -> {
      route.fulfill(new Route.FulfillOptions().setBody("<html></html>"));
    });
    page.navigate("https://www.example.com");
    page.evaluate("() => localStorage['name1'] = 'value1'");
    page.navigate("https://www.domain.com");
    page.evaluate("() => localStorage['name2'] = 'value2'");

    List<Object> events = new ArrayList<>();
    context.onPage(e -> events.add(e));
    context.onRequest(e -> events.add(e));
    context.onRequestFailed(e -> events.add(e));
    context.onRequestFinished(e -> events.add(e));
    context.onResponse(e -> events.add(e));
    context.storageState();
    assertEquals(0, events.size());
  }

  @PlaywrightTest
  @DisplayName("ST-008 should not restore localStorage twice")
  void shouldNotRestoreLocalStorageTwice(ContextFactory contextFactory) {
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions().setStorageState(js("{\n" +
      "  cookies: [],\n" +
      "  origins: [\n" +
      "    {\n" +
      "      origin: 'https://www.example.com',\n" +
      "      localStorage: [{\n" +
      "        name: 'name1',\n" +
      "        value: 'value1'\n" +
      "      }]\n" +
      "    },\n" +
      "  ]\n" +
      "}").toString()));
    Page page = context.newPage();
    page.route("**/*", route -> {
      try {
        route.fulfill(new Route.FulfillOptions().setBody("<html></html>"));
      } catch (PlaywrightException e) {
      }
    });
    page.navigate("https://www.example.com");
    Object localStorage1 = page.evaluate("window.localStorage");
    assertEquals(mapOf("name1", "value1"), localStorage1);

    page.evaluate("() => window.localStorage['name1'] = 'value2'");

    page.navigate("https://www.example.com");
    Object localStorage2 = page.evaluate("window.localStorage");
    assertEquals(mapOf("name1", "value2"), localStorage2);

    context.close();
  }

  @PlaywrightTest
  @DisplayName("ST-009 should handle missing file")
  @Fixme("java: the client reads the file itself and reports \"Failed to read storage state from file\", without \"Error reading storage state from <file>:\\nENOENT\"")
  void shouldHandleMissingFile(ContextFactory contextFactory, TestInfo testInfo) {
    Path file = testInfo.outputPath("does-not-exist.json");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> contextFactory.newContext(new Browser.NewContextOptions().setStorageStatePath(file)));
    assertTrue(error.getMessage().contains("Error reading storage state from " + file + ":\nENOENT"));
  }

  @PlaywrightTest
  @DisplayName("ST-010 should handle malformed file")
  @Fixme("java: the client parses the file itself and reports a Gson error, without \"Error reading storage state from <file>:\\nUnexpected token 'o', \\\"not-json\\\" is not valid JSON\"")
  void shouldHandleMalformedFile(ContextFactory contextFactory, TestInfo testInfo) throws Exception {
    Path file = testInfo.outputPath("state.json");
    Files.write(file, "not-json".getBytes(StandardCharsets.UTF_8));
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> contextFactory.newContext(new Browser.NewContextOptions().setStorageStatePath(file)));
    // Java: the message of the Node.js versions above 18; the driver runs on a newer Node.js.
    assertTrue(error.getMessage().contains("Error reading storage state from " + file + ":\nUnexpected token 'o', \"not-json\" is not valid JSON"));
  }

  @PlaywrightTest
  @DisplayName("ST-011 should serialize storageState with lone surrogates")
  @Fixme("driver: 1.64.1 replaces lone surrogates with U+FFFD for non-JavaScript clients; the client declares support with supportsLoneSurrogates since microsoft/playwright#43548, the Java client got \"\\uFFFD\" instead of \"\\uDA7E\"")
  void shouldSerializeStorageStateWithLoneSurrogates(Page page, BrowserContext context, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("() => window.localStorage.setItem('foo', String.fromCharCode(55934))");
    String storageState = context.storageState();
    assertEquals(String.valueOf((char) 55934), state(storageState).getAsJsonArray("origins").get(0).getAsJsonObject()
      .getAsJsonArray("localStorage").get(0).getAsJsonObject().get("value").getAsString());
  }

  @PlaywrightTest
  @DisplayName("ST-012 should work when service worker is intefering")
  void shouldWorkWhenServiceWorkerIsIntefering(Page page, BrowserContext context, TestServer server) {
    server.setRoute("/", (req, res) -> {
      res.writeHead(200, "content-type", "text/html");
      res.end("\n" +
        "      <script>\n" +
        "        console.log('from page');\n" +
        "        window.localStorage.foo = 'bar';\n" +
        "        window.registrationPromise = navigator.serviceWorker.register('sw.js');\n" +
        "        window.activationPromise = new Promise(resolve => navigator.serviceWorker.oncontrollerchange = resolve);\n" +
        "      </script>\n" +
        "    ");
    });

    server.setRoute("/sw.js", (req, res) -> {
      res.writeHead(200, "content-type", "application/javascript");
      res.end("\n" +
        "      const kHtmlPage = `\n" +
        "        <script>\n" +
        "          console.log('from sw page');\n" +
        "          let counter = window.localStorage.counter || 0;\n" +
        "          ++counter;\n" +
        "          window.localStorage.counter = counter;\n" +
        "          setTimeout(() => {\n" +
        "            window.location.href = counter + '.html';\n" +
        "          }, 0);\n" +
        "        </script>\n" +
        "      `;\n" +
        "\n" +
        "      console.log('from sw 1');\n" +
        "      self.addEventListener('fetch', event => {\n" +
        "        console.log('fetching ' + event.request.url);\n" +
        "        const blob = new Blob([kHtmlPage], { type: 'text/html' });\n" +
        "        const response = new Response(blob, { status: 200 , statusText: 'OK' });\n" +
        "        event.respondWith(response);\n" +
        "      });\n" +
        "\n" +
        "      self.addEventListener('activate', event => {\n" +
        "        console.log('from sw 2');\n" +
        "        event.waitUntil(clients.claim());\n" +
        "      });\n" +
        "    ");
    });

    page.navigate(server.PREFIX);
    page.evaluate("() => window['activationPromise']");

    String storageState = context.storageState();
    assertEquals(js("{ name: 'foo', value: 'bar' }"), state(storageState).getAsJsonArray("origins").get(0).getAsJsonObject()
      .getAsJsonArray("localStorage").get(0));
  }

  @PlaywrightTest
  @DisplayName("ST-013 should work when service worker is intefering and the origin is not open")
  void shouldWorkWhenServiceWorkerIsInteferingAndTheOriginIsNotOpen(Page page, BrowserContext context, TestServer server) {
    server.setRoute("/", (req, res) -> {
      res.writeHead(200, "content-type", "text/html");
      res.end("\n" +
        "      <script>\n" +
        "        window.localStorage.foo = 'bar';\n" +
        "        window.registrationPromise = navigator.serviceWorker.register('sw.js');\n" +
        "        window.activationPromise = new Promise(resolve => navigator.serviceWorker.oncontrollerchange = resolve);\n" +
        "      </script>\n" +
        "    ");
    });

    server.setRoute("/sw.js", (req, res) -> {
      res.writeHead(200, "content-type", "application/javascript");
      res.end("\n" +
        "      const kHtmlPage = `\n" +
        "        <script>\n" +
        "          window.localStorage.fromServiceWorker = 'yes';\n" +
        "          window.location.href = 'redirected.html';\n" +
        "        </script>\n" +
        "      `;\n" +
        "\n" +
        "      self.addEventListener('fetch', event => {\n" +
        "        if (new URL(event.request.url).pathname !== '/')\n" +
        "          return;\n" +
        "        const blob = new Blob([kHtmlPage], { type: 'text/html' });\n" +
        "        event.respondWith(new Response(blob, { status: 200, statusText: 'OK' }));\n" +
        "      });\n" +
        "\n" +
        "      self.addEventListener('activate', event => {\n" +
        "        event.waitUntil(clients.claim());\n" +
        "      });\n" +
        "    ");
    });

    server.setRoute("/redirected.html", (req, res) -> {
      res.writeHead(200, "content-type", "text/html");
      res.end("<html></html>");
    });

    page.navigate(server.PREFIX);
    page.evaluate("() => window['activationPromise']");
    page.navigate("about:blank");

    String storageState = context.storageState();
    assertEquals(js("[{ name: 'foo', value: 'bar' }]"), state(storageState).getAsJsonArray("origins").get(0).getAsJsonObject()
      .getAsJsonArray("localStorage"));
  }

  @PlaywrightTest
  @DisplayName("ST-014 should set local storage in third-party context")
  void shouldSetLocalStorageInThirdPartyContext(ContextFactory contextFactory, TestServer server) {
    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions().setStorageState(js("{\n" +
      "  cookies: [],\n" +
      "  origins: [\n" +
      "    {\n" +
      "      origin: '" + server.CROSS_PROCESS_PREFIX + "',\n" +
      "      localStorage: [{\n" +
      "        name: 'name1',\n" +
      "        value: 'value1'\n" +
      "      }]\n" +
      "    },\n" +
      "  ]\n" +
      "}").toString()));
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    Frame frame = attachFrame(page, "frame1", server.CROSS_PROCESS_PREFIX + "/empty.html");

    Object localStorage = frame.evaluate("window.localStorage");
    assertEquals(mapOf("name1", "value1"), localStorage);
    context.close();
  }

  @PlaywrightTest
  @DisplayName("ST-015 should roundtrip local storage in third-party context")
  void shouldRoundtripLocalStorageInThirdPartyContext(Page page, ContextFactory contextFactory, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    Frame frame = attachFrame(page, "frame1", server.CROSS_PROCESS_PREFIX + "/empty.html");
    frame.evaluate("() => window.localStorage.setItem('name1', 'value1')");
    String storageState = page.context().storageState();

    BrowserContext context2 = contextFactory.newContext(new Browser.NewContextOptions().setStorageState(storageState));
    Page page2 = context2.newPage();
    page2.navigate(server.EMPTY_PAGE);
    Frame frame2 = attachFrame(page2, "frame1", server.CROSS_PROCESS_PREFIX + "/empty.html");

    Object localStorage = frame2.evaluate("window.localStorage");
    assertEquals(mapOf("name1", "value1"), localStorage);
    context2.close();
  }

  @PlaywrightTest
  @DisplayName("ST-016 should support IndexedDB")
  void shouldSupportIndexedDB(Page page, TestServer server, ContextFactory contextFactory) {
    page.navigate(server.PREFIX + "/to-do-notifications/index.html");

    assertThat(page.locator("#notifications")).matchesAriaSnapshot("\n" +
      "    - list:\n" +
      "      - listitem: Database initialised.\n" +
      "  ");
    page.getByLabel("Task title").fill("Pet the cat");
    page.getByLabel("Hours").fill("1");
    page.getByLabel("Mins").fill("1");
    page.getByText("Add Task").click();
    assertThat(page.locator("#notifications")).matchesAriaSnapshot("\n" +
      "    - list:\n" +
      "      - listitem: \"Transaction completed: database modification finished.\"\n" +
      "  ");

    String storageState = page.context().storageState(new BrowserContext.StorageStateOptions().setIndexedDB(true));
    assertEquals(js("[\n" +
      "  {\n" +
      "    origin: '" + server.PREFIX + "',\n" +
      "    localStorage: [],\n" +
      "    indexedDB: [\n" +
      "      {\n" +
      "        name: 'toDoList',\n" +
      "        version: 4,\n" +
      "        stores: [\n" +
      "          {\n" +
      "            name: 'toDoList',\n" +
      "            autoIncrement: false,\n" +
      "            keyPath: 'taskTitle',\n" +
      "            records: [\n" +
      "              {\n" +
      "                valueEncoded: {\n" +
      "                  id: 1,\n" +
      "                  o: [\n" +
      "                    { k: 'taskTitle', v: 'Pet the cat' },\n" +
      "                    { k: 'hours', v: '1' },\n" +
      "                    { k: 'minutes', v: '1' },\n" +
      "                    { k: 'day', v: '01' },\n" +
      "                    { k: 'month', v: 'January' },\n" +
      "                    { k: 'year', v: '2025' },\n" +
      "                    { k: 'notified', v: 'no' },\n" +
      "                    { k: 'binaryTitle', v: { ab: { b: 'UGV0IHRoZSBjYXQ=' } }, }\n" +
      "                  ]\n" +
      "                },\n" +
      "              },\n" +
      "            ],\n" +
      "            indexes: [\n" +
      "              {\n" +
      "                name: 'day',\n" +
      "                keyPath: 'day',\n" +
      "                multiEntry: false,\n" +
      "                unique: false,\n" +
      "              },\n" +
      "              {\n" +
      "                name: 'hours',\n" +
      "                keyPath: 'hours',\n" +
      "                multiEntry: false,\n" +
      "                unique: false,\n" +
      "              },\n" +
      "              {\n" +
      "                name: 'minutes',\n" +
      "                keyPath: 'minutes',\n" +
      "                multiEntry: false,\n" +
      "                unique: false,\n" +
      "              },\n" +
      "              {\n" +
      "                name: 'month',\n" +
      "                keyPath: 'month',\n" +
      "                multiEntry: false,\n" +
      "                unique: false,\n" +
      "              },\n" +
      "              {\n" +
      "                name: 'notified',\n" +
      "                keyPath: 'notified',\n" +
      "                multiEntry: false,\n" +
      "                unique: false,\n" +
      "              },\n" +
      "              {\n" +
      "                name: 'year',\n" +
      "                keyPath: 'year',\n" +
      "                multiEntry: false,\n" +
      "                unique: false,\n" +
      "              },\n" +
      "            ],\n" +
      "          },\n" +
      "        ],\n" +
      "      },\n" +
      "    ],\n" +
      "  },\n" +
      "]"), state(storageState).get("origins"));

    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions().setStorageState(storageState));
    assertEquals(js(storageState), js(context.storageState(new BrowserContext.StorageStateOptions().setIndexedDB(true))));

    Page recreatedPage = context.newPage();
    recreatedPage.navigate(server.PREFIX + "/to-do-notifications/index.html");
    assertThat(recreatedPage.locator("#task-list")).matchesAriaSnapshot("\n" +
      "    - list:\n" +
      "      - listitem:\n" +
      "        - text: /Pet the cat \\[Pet the cat\\]/\n" +
      "  ");

    assertEquals(js("{ cookies: [], origins: [] }"), js(context.storageState()));
  }

  @Nested
  @ParameterizedClass
  @ValueSource(strings = {"newContext", "setStorageState"})
  class Restore {
    @Parameter
    String restore;

    @PlaywrightTest
    @DisplayName("ST-017 should roundtrip IndexedDB values and keys with ${restore}")
  @Fixme("driver: 1.64.1 lacks microsoft/playwright#43429 (keep falsy IndexedDB values and keys in storageState); newContext/setStorageState fail with \"Error setting storage state:\\nError: Unable to restore IndexedDB: Failed to execute 'add' on 'IDBObjectStore': The object store uses out-of-line keys and has no key generator and the key parameter was not provided.\"")
    void shouldRoundtripIndexedDBValuesAndKeysWithRestore(Page page, TestServer server, ContextFactory contextFactory, TestInfo testInfo) {
      page.navigate(server.EMPTY_PAGE);
      page.evaluate("() => new Promise((resolve, reject) => {\n" +
        "  const request = indexedDB.open('collections', 1);\n" +
        "  request.onupgradeneeded = () => request.result.createObjectStore('store');\n" +
        "  request.onerror = () => reject(request.error);\n" +
        "  request.onsuccess = () => {\n" +
        "    const db = request.result;\n" +
        "    const transaction = db.transaction('store', 'readwrite');\n" +
        "    const store = transaction.objectStore('store');\n" +
        "    store.put(new Map([['mk', 'mv']]), 'map');\n" +
        "    store.put(new Set([1, 2]), 'set');\n" +
        "    store.put(0, 'zero');\n" +
        "    store.put(false, 'false');\n" +
        "    store.put('', 'empty');\n" +
        "    store.put(null, 'null');\n" +
        "    store.put(Infinity, 'infinity');\n" +
        "    store.put('first', 0);\n" +
        "    store.put('second', '');\n" +
        "    transaction.oncomplete = () => {\n" +
        "      db.close();\n" +
        "      resolve();\n" +
        "    };\n" +
        "    transaction.onerror = () => reject(transaction.error);\n" +
        "  };\n" +
        "})");

      Path path = testInfo.outputPath("storage-state.json");
      String storageState = page.context().storageState(new BrowserContext.StorageStateOptions().setIndexedDB(true).setPath(path));
      BrowserContext context = contextFactory.newContext(restore.equals("newContext") ? new Browser.NewContextOptions().setStorageStatePath(path) : new Browser.NewContextOptions());
      if (restore.equals("setStorageState")) {
        // Java: setStorageState() takes a file.
        context.setStorageState(writeState(testInfo, "storage-state-2.json", storageState));
      }
      assertEquals(js(storageState), js(context.storageState(new BrowserContext.StorageStateOptions().setIndexedDB(true))));

      Page restoredPage = context.newPage();
      restoredPage.navigate(server.EMPTY_PAGE);
      Object entries = restoredPage.evaluate("async () => {\n" +
        "  const db = await new Promise((resolve, reject) => {\n" +
        "    const request = indexedDB.open('collections', 1);\n" +
        "    request.onerror = () => reject(request.error);\n" +
        "    request.onsuccess = () => resolve(request.result);\n" +
        "  });\n" +
        "  const transaction = db.transaction('store', 'readonly');\n" +
        "  const store = transaction.objectStore('store');\n" +
        "  transaction.oncomplete = () => db.close();\n" +
        "  const [keys, values] = await Promise.all([store.getAllKeys(), store.getAll()].map(request => new Promise((resolve, reject) => {\n" +
        "    request.onsuccess = () => resolve(request.result);\n" +
        "    request.onerror = () => reject(request.error);\n" +
        "  })));\n" +
        "  return keys.map((key, i) => {\n" +
        "    const value = values[i];\n" +
        "    if (value instanceof Map)\n" +
        "      return [key, { map: [...value] }];\n" +
        "    if (value instanceof Set)\n" +
        "      return [key, { set: [...value] }];\n" +
        "    return [key, value];\n" +
        "  });\n" +
        "}");
      assertEquals(asList(
        asList(0, "first"),
        asList("", "second"),
        asList("empty", ""),
        asList("false", false),
        asList("infinity", Double.POSITIVE_INFINITY),
        asList("map", mapOf("map", asList(asList("mk", "mv")))),
        asList("null", null),
        asList("set", mapOf("set", asList(1, 2))),
        asList("zero", 0)), entries);
    }
  }

  @PlaywrightTest
  @DisplayName("ST-018 should support empty indexedDB")
  void shouldSupportEmptyIndexedDB(Page page, TestServer server, ContextFactory contextFactory) {
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("() => new Promise(resolve => {\n" +
      "  const openRequest = indexedDB.open('unused-db');\n" +
      "  openRequest.onsuccess = () => resolve();\n" +
      "  openRequest.onerror = () => resolve();\n" +
      "})");
    String storageState = page.context().storageState(new BrowserContext.StorageStateOptions().setIndexedDB(true));
    assertEquals(js("[{\n" +
      "  origin: '" + server.PREFIX + "',\n" +
      "  localStorage: [],\n" +
      "  indexedDB: [{\n" +
      "    name: 'unused-db',\n" +
      "    version: 1,\n" +
      "    stores: [],\n" +
      "  }]\n" +
      "}]"), state(storageState).get("origins"));

    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions().setStorageState(storageState));
    assertEquals(js(storageState), js(context.storageState(new BrowserContext.StorageStateOptions().setIndexedDB(true))));
  }

  @PlaywrightTest
  @DisplayName("ST-019 should not leave IndexedDB connections open")
  void shouldNotLeaveIndexedDBConnectionsOpen(ContextFactory contextFactory, TestServer server, TestInfo testInfo) {
    BrowserContext context = contextFactory.newContext();
    Page page = context.newPage();
    page.navigate(server.EMPTY_PAGE);
    page.evaluate("async () => {\n" +
      "  const openRequest = indexedDB.open('db', 1);\n" +
      "  openRequest.onupgradeneeded = () => openRequest.result.createObjectStore('store');\n" +
      "  await new Promise((resolve, reject) => {\n" +
      "    openRequest.onsuccess = () => {\n" +
      "      const db = openRequest.result;\n" +
      "      const transaction = db.transaction('store', 'readwrite');\n" +
      "      transaction.objectStore('store').put('value', 'key');\n" +
      "      transaction.oncomplete = () => {\n" +
      "        db.close();\n" +
      "        resolve();\n" +
      "      };\n" +
      "      transaction.onerror = () => reject(transaction.error);\n" +
      "    };\n" +
      "    openRequest.onerror = () => reject(openRequest.error);\n" +
      "  });\n" +
      "}");

    String state = context.storageState(new BrowserContext.StorageStateOptions().setIndexedDB(true));

    page.evaluate("() => new Promise((resolve, reject) => {\n" +
      "  const request = indexedDB.deleteDatabase('db');\n" +
      "  request.onsuccess = () => resolve();\n" +
      "  request.onerror = () => reject(request.error);\n" +
      "  request.onblocked = () => reject(new Error('deleteDatabase was blocked'));\n" +
      "})");

    // Java: setStorageState() takes a file.
    context.setStorageState(writeState(testInfo, "state.json", state));
    assertEquals(js(state), js(context.storageState(new BrowserContext.StorageStateOptions().setIndexedDB(true))));
  }

  // Java: a VirtualCredential has no equals(), the tests compare its JSON.
  private static JsonElement credentialJson(VirtualCredential credential) {
    return new Gson().toJsonTree(credential);
  }

  private static JsonElement credentialsJson(List<VirtualCredential> credentials) {
    JsonArray array = new JsonArray();
    for (VirtualCredential credential : credentials) {
      array.add(credentialJson(credential));
    }
    return array;
  }

  @PlaywrightTest
  @DisplayName("ST-020 should round-trip WebAuthn credentials with storageState")
  void shouldRoundTripWebAuthnCredentialsWithStorageState(ContextFactory contextFactory, TestServer server) {
    BrowserContext context = contextFactory.newContext();
    VirtualCredential credential = context.credentials().create(server.HOSTNAME);

    // Credentials are opt-in, omitted by default.
    assertEquals(js("{ cookies: [], origins: [] }"), js(context.storageState()));

    String storageState = context.storageState(new BrowserContext.StorageStateOptions().setCredentials(true));
    JsonObject expected = js("{ cookies: [], origins: [] }").getAsJsonObject();
    expected.add("credentials", credentialsJson(asList(credential)));
    assertEquals(expected, js(storageState));

    // A fresh context seeded from the storage state holds the same credential and round-trips equal.
    BrowserContext context2 = contextFactory.newContext(new Browser.NewContextOptions().setStorageState(storageState));
    assertEquals(credentialsJson(asList(credential)), credentialsJson(context2.credentials().get()));
    assertEquals(js(storageState), js(context2.storageState(new BrowserContext.StorageStateOptions().setCredentials(true))));
  }

  @PlaywrightTest
  @DisplayName("ST-021 setStorageState should replace credentials")
  void setStorageStateShouldReplaceCredentials(ContextFactory contextFactory, TestInfo testInfo) {
    BrowserContext ctxA = contextFactory.newContext();
    VirtualCredential credA = ctxA.credentials().create("a.example.com");
    String stateA = ctxA.storageState(new BrowserContext.StorageStateOptions().setCredentials(true));

    BrowserContext ctxB = contextFactory.newContext();
    VirtualCredential credB = ctxB.credentials().create("b.example.com");
    String stateB = ctxB.storageState(new BrowserContext.StorageStateOptions().setCredentials(true));

    BrowserContext context = contextFactory.newContext(new Browser.NewContextOptions().setStorageState(stateA));
    assertEquals(credentialsJson(asList(credA)), credentialsJson(context.credentials().get()));

    // Replacing the storage state swaps in the new credentials.
    // Java: setStorageState() takes a file.
    context.setStorageState(writeState(testInfo, "state-b.json", stateB));
    assertEquals(credentialsJson(asList(credB)), credentialsJson(context.credentials().get()));

    // A storage state without credentials clears them.
    context.setStorageState(writeState(testInfo, "state-empty.json", "{ \"cookies\": [], \"origins\": [] }"));
    assertEquals(credentialsJson(new ArrayList<VirtualCredential>()), credentialsJson(context.credentials().get()));

    // Credentials can be installed again afterwards.
    context.setStorageState(writeState(testInfo, "state-a.json", stateA));
    assertEquals(credentialsJson(asList(credA)), credentialsJson(context.credentials().get()));
  }

  @PlaywrightTest
  @DisplayName("ST-022 setStorageState should handle missing file")
  @Fixme("java: the client reads the file itself and reports \"Failed to read storage state from file\", without \"Error reading storage state from <file>\"")
  void setStorageStateShouldHandleMissingFile(ContextFactory contextFactory, TestInfo testInfo) {
    BrowserContext context = contextFactory.newContext();
    Path file = testInfo.outputPath("does-not-exist.json");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> context.setStorageState(file));
    assertTrue(error.getMessage().contains("Error reading storage state from " + file));
  }
}
