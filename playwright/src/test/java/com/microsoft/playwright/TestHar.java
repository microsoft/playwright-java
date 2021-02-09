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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.microsoft.playwright.options.LoadState.DOMCONTENTLOADED;
import static org.junit.jupiter.api.Assertions.*;

public class TestHar extends TestBase {
  private PageWithHar pageWithHar;

  private class PageWithHar {
    final Path harFile;
    final BrowserContext context;
    final Page page;

    PageWithHar() throws IOException {
      harFile = Files.createTempFile("test-", ".har");
      context = browser.newContext(new Browser.NewContextOptions().setRecordHar()
        .withPath(harFile).done().withIgnoreHTTPSErrors(true));
      page = context.newPage();
    }

    JsonObject log() throws IOException {
      context.close();
      try (FileReader json = new FileReader(harFile.toFile())) {
        return new Gson().fromJson(json, JsonObject.class).getAsJsonObject("log");
      }
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

  @Test
  void shouldThrowWithoutPath() {
    try {
      browser.newContext(new Browser.NewContextOptions().setRecordHar().done());
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("recordHar.path: expected string, got undefined"));
    }
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
    assertEquals("page_0", pageEntry.get("id").getAsString());
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
      .setRecordHar().withPath(harPath).done().withIgnoreHTTPSErrors(true));
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
    assertEquals("page_0", pageEntry.get("id").getAsString());
    assertEquals("Hello", pageEntry.get("title").getAsString());
  }

  @Test
  void shouldIncludeRequest() throws IOException {
    pageWithHar.page.navigate(server.EMPTY_PAGE);
    JsonObject log = pageWithHar.log();
    assertEquals(1, log.getAsJsonArray("entries").size());
    JsonObject entry = log.getAsJsonArray("entries").get(0).getAsJsonObject();
    assertEquals("page_0", entry.get("pageref").getAsString());
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
}
