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

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestInfo;
import com.microsoft.playwright.options.DropPayload;
import com.microsoft.playwright.options.FilePayload;
import org.junit.jupiter.api.DisplayName;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-drop.spec.ts. */
public class TestPageDrop {
  private static void setupDropzone(Page page) {
    page.setContent("\n" +
      "    <style>#dropzone { width: 300px; height: 200px; border: 2px dashed #888; }</style>\n" +
      "    <div id=\"dropzone\"></div>\n" +
      "    <script>\n" +
      "      window.__dropInfo = null;\n" +
      "      const zone = document.getElementById('dropzone');\n" +
      "      zone.addEventListener('dragenter', e => e.preventDefault());\n" +
      "      zone.addEventListener('dragover', e => e.preventDefault());\n" +
      "      zone.addEventListener('drop', async e => {\n" +
      "        e.preventDefault();\n" +
      "        const files = [];\n" +
      "        for (const file of e.dataTransfer.files)\n" +
      "          files.push({ name: file.name, type: file.type, size: file.size, text: await file.text() });\n" +
      "        const data = {};\n" +
      "        for (const t of e.dataTransfer.types) {\n" +
      "          if (t !== 'Files')\n" +
      "            data[t] = e.dataTransfer.getData(t);\n" +
      "        }\n" +
      "        window.__dropInfo = { files, data };\n" +
      "      });\n" +
      "    </script>\n" +
      "  ");
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> getDropInfo(Page page) {
    return (Map<String, Object>) page.waitForFunction("() => window.__dropInfo").jsonValue();
  }

  @PlaywrightTest
  @DisplayName("DD-021 should drop a file payload")
  void shouldDropAFilePayload(Page page) {
    setupDropzone(page);
    page.locator("#dropzone").drop(new DropPayload().setFiles(
      new FilePayload("note.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8))));
    assertEquals(mapOf(
      "files", Arrays.asList(mapOf("name", "note.txt", "type", "text/plain", "size", 5, "text", "hello")),
      "data", mapOf()), getDropInfo(page));
  }

  @PlaywrightTest
  @DisplayName("DD-022 should drop multiple file payloads")
  void shouldDropMultipleFilePayloads(Page page) {
    setupDropzone(page);
    page.locator("#dropzone").drop(new DropPayload().setFiles(new FilePayload[]{
      new FilePayload("a.txt", "text/plain", "AAA".getBytes(StandardCharsets.UTF_8)),
      new FilePayload("b.txt", "text/plain", "BB".getBytes(StandardCharsets.UTF_8))}));
    Map<String, Object> info = getDropInfo(page);
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> files = (List<Map<String, Object>>) info.get("files");
    assertEquals(Arrays.asList(Arrays.asList("a.txt", "AAA"), Arrays.asList("b.txt", "BB")),
      Arrays.asList(Arrays.asList(files.get(0).get("name"), files.get(0).get("text")),
        Arrays.asList(files.get(1).get("name"), files.get(1).get("text"))));
  }

  @PlaywrightTest
  @DisplayName("DD-023 should drop a file by local path")
  void shouldDropAFileByLocalPath(Page page, TestInfo testInfo) throws IOException {
    setupDropzone(page);
    Path filePath = testInfo.outputPath("hello.txt");
    Files.write(filePath, "path-content".getBytes(StandardCharsets.UTF_8));
    page.locator("#dropzone").drop(new DropPayload().setFiles(filePath));
    Map<String, Object> info = getDropInfo(page);
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> files = (List<Map<String, Object>>) info.get("files");
    assertEquals(1, files.size());
    assertEquals("hello.txt", files.get(0).get("name"));
    assertEquals("path-content", files.get(0).get("text"));
  }

  @PlaywrightTest
  @DisplayName("DD-024 should drop clipboard-like data")
  void shouldDropClipboardLikeData(Page page) {
    setupDropzone(page);
    page.locator("#dropzone").drop(new DropPayload().setData(mapOfStrings(
      "text/plain", "hello world",
      "text/uri-list", "https://example.com")));
    Map<String, Object> info = getDropInfo(page);
    assertEquals(Arrays.asList(), info.get("files"));
    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) info.get("data");
    assertEquals("hello world", data.get("text/plain"));
    assertEquals("https://example.com", data.get("text/uri-list"));
  }

  @PlaywrightTest
  @DisplayName("DD-025 should drop files and data together")
  void shouldDropFilesAndDataTogether(Page page) {
    setupDropzone(page);
    page.locator("#dropzone").drop(new DropPayload()
      .setFiles(new FilePayload("mix.txt", "text/plain", "mix".getBytes(StandardCharsets.UTF_8)))
      .setData(mapOfStrings("text/plain", "label")));
    Map<String, Object> info = getDropInfo(page);
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> files = (List<Map<String, Object>>) info.get("files");
    @SuppressWarnings("unchecked")
    Map<String, Object> data = (Map<String, Object>) info.get("data");
    assertEquals("mix", files.get(0).get("text"));
    assertEquals("label", data.get("text/plain"));
  }

  @PlaywrightTest
  @DisplayName("DD-026 should throw when target does not accept drop")
  void shouldThrowWhenTargetDoesNotAcceptDrop(Page page) {
    // Dropzone without preventDefault on dragover.
    page.setContent("\n" +
      "    <div id=\"dropzone\" style=\"width: 200px; height: 100px;\"></div>\n" +
      "  ");
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.locator("#dropzone").drop(new DropPayload().setData(mapOfStrings("text/plain", "nope"))));
    assertTrue(Pattern.compile("drop target did not accept the drop", Pattern.CASE_INSENSITIVE).matcher(error.getMessage()).find());
  }

  @PlaywrightTest
  @DisplayName("DD-027 should throw when neither files nor data provided")
  void shouldThrowWhenNeitherFilesNorDataProvided(Page page) {
    setupDropzone(page);
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.locator("#dropzone").drop(new DropPayload()));
    assertTrue(Pattern.compile("At least one of \"files\" or \"data\"").matcher(error.getMessage()).find());
  }

  // Java: DropPayload.data is a Map<String, String>; mapOf() of Utils returns Map<String, Object>.
  private static Map<String, String> mapOfStrings(String... keysAndValues) {
    Map<String, String> map = new java.util.LinkedHashMap<>();
    for (int i = 0; i < keysAndValues.length; i += 2) {
      map.put(keysAndValues[i], keysAndValues[i + 1]);
    }
    return map;
  }
}
