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

import com.google.gson.*;
import com.microsoft.playwright.options.HarContentPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.regex.Pattern;

import static com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED;
import static org.junit.jupiter.api.Assertions.*;

public class TestHar extends TestBase {
  private PageWithHar pageWithHar;

  private static JsonObject parseHar(Path harFile) throws IOException {
    try (FileReader json = new FileReader(harFile.toFile())) {
      return new Gson().fromJson(json, JsonObject.class).getAsJsonObject("log");
    }
  }

  private class PageWithHar {
    final Path harFile;
    final BrowserContext context;
    final Page page;

    PageWithHar() throws IOException {
      this(new Browser.NewContextOptions(), null);
    }

    PageWithHar(Browser.NewContextOptions options, Path harFilePath) throws IOException {
      harFile = harFilePath == null ? Files.createTempFile("test-", ".har") : harFilePath;
      context = browser.newContext(options
        .setRecordHarPath(harFile).setIgnoreHTTPSErrors(true));
      page = context.newPage();
    }

    JsonObject log() throws IOException {
      context.close();
      return parseHar(harFile);
    }

    Map<String, byte[]> parseZip() throws IOException {
      context.close();
      return Utils.parseZip(harFile);
    }

    void dispose() throws IOException {
      context.close();
      Files.deleteIfExists(harFile);
    }
  }

  @BeforeEach
  void createPageWithHar() throws IOException {
    pageWithHar = new PageWithHar();
  }

  @AfterEach
  void deletePageWithHar() throws IOException {
    pageWithHar.dispose();
  }

  void shouldThrowWithoutPath() {
    // not applicable in java
  }

  @Test
  void shouldHaveVersionAndCreator() throws IOException {
    pageWithHar.page.navigate(server.EMPTY_PAGE);
    JsonObject log = pageWithHar.log();
    assertEquals("1.2", log.get("version").getAsString());
    assertEquals("Playwright", log.getAsJsonObject("creator").get("name").getAsString());
  }

  @Test
  void shouldHaveBrowser() throws IOException {
    pageWithHar.page.navigate(server.EMPTY_PAGE);
    JsonObject log = pageWithHar.log();
    assertEquals(browserType.name(), log.getAsJsonObject("browser").get("name").getAsString().toLowerCase());
    assertEquals(browser.version(), log.getAsJsonObject("browser").get("version").getAsString());
  }

  @Test
  void shouldHavePages() throws IOException {
    pageWithHar.page.navigate("data:text/html,<title>Hello</title>");
    // For data: load comes before domcontentloaded...
    pageWithHar.page.waitForLoadState(DOMCONTENTLOADED);
    JsonObject log = pageWithHar.log();

    assertEquals(1, log.getAsJsonArray("pages").size());
    JsonObject pageEntry = log.getAsJsonArray("pages").get(0).getAsJsonObject();
    assertNotNull(pageEntry.get("id").getAsString());
    assertEquals("Hello", pageEntry.get("title").getAsString());
//    expect(new Date(pageEntry.startedDateTime).valueOf()).toBeGreaterThan(Date.now() - 3600 * 1000);
    assertTrue(pageEntry.getAsJsonObject("pageTimings").get("onContentLoad").getAsDouble() > 0);
    assertTrue(pageEntry.getAsJsonObject("pageTimings").get("onLoad").getAsDouble() > 0);
  }

  @Test
  void shouldHavePagesInPersistentContext() throws IOException {
    Path harPath = pageWithHar.harFile;
    Path userDataDir = Files.createTempDirectory("user-data-dir-");
    BrowserContext context = browserType.launchPersistentContext(userDataDir,
      new BrowserType.LaunchPersistentContextOptions()
        .setRecordHarPath(harPath).setIgnoreHTTPSErrors(true));
    Page page = context.pages().get(0);

    page.navigate("data:text/html,<title>Hello</title>");
    // For data: load comes before domcontentloaded...
    page.waitForLoadState(DOMCONTENTLOADED);
    context.close();
    JsonObject log;
    try (Reader reader = new FileReader(harPath.toFile())) {
      log = new Gson().fromJson(reader, JsonObject.class).getAsJsonObject("log");
    }
    assertEquals(1, log.getAsJsonArray("pages").size());
    JsonObject pageEntry = log.getAsJsonArray("pages").get(0).getAsJsonObject();
    assertNotNull(pageEntry.get("id").getAsString());
    assertEquals("Hello", pageEntry.get("title").getAsString());
  }

  @Test
  void shouldIncludeRequest() throws IOException {
    pageWithHar.page.navigate(server.EMPTY_PAGE);
    JsonObject log = pageWithHar.log();
    assertEquals(1, log.getAsJsonArray("entries").size());
    JsonObject entry = log.getAsJsonArray("entries").get(0).getAsJsonObject();
    String id = log.getAsJsonArray("pages").get(0).getAsJsonObject().get("id").getAsString();
    assertEquals(id, entry.get("pageref").getAsString());
    assertEquals(server.EMPTY_PAGE, entry.getAsJsonObject("request").get("url").getAsString());
    assertEquals("GET", entry.getAsJsonObject("request").get("method").getAsString());
    assertEquals("HTTP/1.1", entry.getAsJsonObject("request").get("httpVersion").getAsString());
    assertTrue(entry.getAsJsonObject("request").get("headers").getAsJsonArray().size() > 1);
    boolean foundUserAgentHeader = false;
    for (JsonElement item : entry.getAsJsonObject("request").get("headers").getAsJsonArray()) {
      if ("user-agent".equals(item.getAsJsonObject().get("name").getAsString().toLowerCase())) {
        foundUserAgentHeader = true;
        break;
      }
    }
    assertTrue(foundUserAgentHeader);
  }

  @Test
  void shouldIncludeResponse() throws IOException {
    pageWithHar.page.navigate(server.EMPTY_PAGE);
    JsonObject log = pageWithHar.log();
    JsonObject entry = log.getAsJsonArray("entries").get(0).getAsJsonObject();
    assertEquals(200, entry.getAsJsonObject("response").get("status").getAsInt());
    assertEquals("OK", entry.getAsJsonObject("response").get("statusText").getAsString());
    assertEquals("HTTP/1.1", entry.getAsJsonObject("response").get("httpVersion").getAsString());
    assertTrue(entry.getAsJsonObject("response").get("headers").getAsJsonArray().size() > 1);

    boolean foundUserContentType = false;
    for (JsonElement item : entry.getAsJsonObject("response").get("headers").getAsJsonArray()) {
      if ("content-type".equals(item.getAsJsonObject().get("name").getAsString().toLowerCase())) {
        foundUserContentType = true;
        assertEquals("text/html", item.getAsJsonObject().get("value").getAsString());
        break;
      }
    }
    assertTrue(foundUserContentType);
  }

  @Test
  void shouldFilterByGlob(@TempDir Path tmpDir) throws IOException {
    Path harPath = tmpDir.resolve("test.har");
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setBaseURL(server.PREFIX)
      .setRecordHarPath(harPath)
      .setRecordHarUrlFilter("/*.css")
      .setIgnoreHTTPSErrors(true));
    Page page = context.newPage();
    page.navigate("/har.html");
    context.close();
    JsonObject log = parseHar(harPath);
    JsonArray entries = log.getAsJsonArray("entries");
    // There are 2 entries for the same .css request in firefox.
    if (isFirefox()) {
      assertEquals(2, entries.size());
    } else {
      assertEquals(1, entries.size());
    }
    assertTrue(entries.get(0).getAsJsonObject().getAsJsonObject("request").get("url").getAsString().endsWith("one-style.css"));
  }

  @Test
  void shouldFilterByRegexp(@TempDir Path tmpDir) throws IOException {
    Path harPath = tmpDir.resolve("test.har");
    BrowserContext context = browser.newContext(new Browser.NewContextOptions()
      .setBaseURL(server.PREFIX)
      .setRecordHarPath(harPath)
      .setRecordHarUrlFilter(Pattern.compile("HAR.X?HTML", Pattern.CASE_INSENSITIVE))
      .setIgnoreHTTPSErrors(true));
    Page page = context.newPage();
    page.navigate(server.PREFIX + "/har.html");
    context.close();
    JsonObject log = parseHar(harPath);
    JsonArray entries = log.getAsJsonArray("entries");
    assertEquals(1, entries.size());
    assertTrue(entries.get(0).getAsJsonObject().getAsJsonObject("request").get("url").getAsString().endsWith("har.html"));
  }

  @Test
  void shouldOmitContent(@TempDir Path tmpDir) throws IOException {
    Path harPath = tmpDir.resolve("test.har");
    PageWithHar pageWithHar = new PageWithHar(new Browser.NewContextOptions()
      .setRecordHarContent(HarContentPolicy.OMIT), harPath);
    pageWithHar.page.navigate(server.PREFIX + "/har.html");
    pageWithHar.page.evaluate("() => fetch('/pptr.png').then(r => r.arrayBuffer())");
    JsonObject log = pageWithHar.log();
    pageWithHar.dispose();
    JsonArray entries = log.getAsJsonArray("entries");
    assertFalse(entries.get(0).getAsJsonObject()
      .getAsJsonObject("response")
      .getAsJsonObject("content")
      .has("text"));
    assertFalse(entries.get(0).getAsJsonObject()
      .getAsJsonObject("response")
      .getAsJsonObject("content")
      .has("_file"));
  }

  @Test
  void shouldOmitContentLegacy(@TempDir Path tmpDir) throws IOException {
    Path harPath = tmpDir.resolve("test.har");
    PageWithHar pageWithHar = new PageWithHar(new Browser.NewContextOptions()
      .setRecordHarOmitContent(true), harPath);
    pageWithHar.page.navigate(server.PREFIX + "/har.html");
    pageWithHar.page.evaluate("() => fetch('/pptr.png').then(r => r.arrayBuffer())");
    JsonObject log = pageWithHar.log();
    pageWithHar.dispose();
    JsonArray entries = log.getAsJsonArray("entries");
    assertFalse(entries.get(0).getAsJsonObject()
      .getAsJsonObject("response")
      .getAsJsonObject("content")
      .has("text"));
    assertFalse(entries.get(0).getAsJsonObject()
      .getAsJsonObject("response")
      .getAsJsonObject("content")
      .has("_file"));
  }

  @Test
  void shouldAttachContent(@TempDir Path tmpDir) throws IOException {
    Path harPath = tmpDir.resolve("test.har.zip");
    PageWithHar pageWithHar = new PageWithHar(new Browser.NewContextOptions()
      .setRecordHarContent(HarContentPolicy.ATTACH), harPath);
    pageWithHar.page.navigate(server.PREFIX + "/har.html");
    pageWithHar.page.evaluate("() => fetch('/pptr.png').then(r => r.arrayBuffer())");
    Map<String, byte[]> zip = pageWithHar.parseZip();
    JsonObject log = new Gson().fromJson(new InputStreamReader(new ByteArrayInputStream(zip.get("har.har"))), JsonObject.class).getAsJsonObject("log");
    pageWithHar.dispose();

    JsonArray entries = log.getAsJsonArray("entries");
    {
      JsonObject content = firstEntryFor(entries, "har.html")
        .getAsJsonObject("response")
        .getAsJsonObject("content");
      assertFalse(content.has("encoding"));
      assertEquals("text/html", content.get("mimeType").getAsString());
      assertTrue(content.get("_file").getAsString().contains("75841480e2606c03389077304342fac2c58ccb1b"));
      assertTrue(content.get("size").getAsInt() >= 96);
      assertEquals(0, content.get("compression").getAsInt());
    }
    {
      // TODO: figure out why there is more than one entry in Firefox.
      JsonObject content = firstEntryFor(entries, "one-style.css")
        .getAsJsonObject("response")
        .getAsJsonObject("content");
      assertFalse(content.has("encoding"));
      assertEquals("text/css", content.get("mimeType").getAsString());
      assertTrue(content.get("_file").getAsString().contains("79f739d7bc88e80f55b9891a22bf13a2b4e18adb"));
      assertTrue(content.get("size").getAsInt() >= 37);
      assertEquals(0, content.get("compression").getAsInt());
    }
    {
      JsonObject content = firstEntryFor(entries, "pptr.png")
        .getAsJsonObject("response")
        .getAsJsonObject("content");
      assertFalse(content.has("encoding"));
      assertEquals("image/png", content.get("mimeType").getAsString());
      assertTrue(content.get("_file").getAsString().contains("a4c3a18f0bb83f5d9fe7ce561e065c36205762fa"));
      assertTrue(content.get("size").getAsInt() >= 6000);
      assertEquals(0, content.get("compression").getAsInt());
    }
    assertTrue(new String(zip.get("75841480e2606c03389077304342fac2c58ccb1b.html"), StandardCharsets.UTF_8).contains("HAR Page"));
    assertTrue(new String(zip.get("79f739d7bc88e80f55b9891a22bf13a2b4e18adb.css"), StandardCharsets.UTF_8).contains("pink"));
    assertEquals(firstEntryFor(entries, "pptr.png")
      .getAsJsonObject("response")
      .getAsJsonObject("content")
      .get("size").getAsInt(), zip.get("a4c3a18f0bb83f5d9fe7ce561e065c36205762fa.png").length);
  }
  private static JsonObject firstEntryFor(JsonArray entries, String name) {
    for (int i = 0; i < entries.size(); i++) {
      JsonObject entry = entries.get(i).getAsJsonObject();
      String url = entry.getAsJsonObject("request").get("url").getAsString();
      if (url.endsWith(name)) {
        return entry;
      }
    }
    return null;
  }
}
