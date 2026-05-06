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

import com.microsoft.playwright.options.FilePayload;
import com.microsoft.playwright.options.DropPayload;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.microsoft.playwright.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPageDrop extends TestBase {
  private void setupDropzone() {
    page.setContent("<style>#dropzone { width: 300px; height: 200px; border: 2px dashed #888; }</style>\n" +
      "<div id=\"dropzone\"></div>\n" +
      "<script>\n" +
      "  window.__dropInfo = null;\n" +
      "  const zone = document.getElementById('dropzone');\n" +
      "  zone.addEventListener('dragenter', e => e.preventDefault());\n" +
      "  zone.addEventListener('dragover', e => e.preventDefault());\n" +
      "  zone.addEventListener('drop', async e => {\n" +
      "    e.preventDefault();\n" +
      "    const files = [];\n" +
      "    for (const file of e.dataTransfer.files)\n" +
      "      files.push({ name: file.name, type: file.type, size: file.size, text: await file.text() });\n" +
      "    const data = {};\n" +
      "    for (const t of e.dataTransfer.types) {\n" +
      "      if (t !== 'Files')\n" +
      "        data[t] = e.dataTransfer.getData(t);\n" +
      "    }\n" +
      "    window.__dropInfo = { files, data };\n" +
      "  });\n" +
      "</script>");
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> waitForDropInfo() {
    page.waitForCondition(() -> page.evaluate("window.__dropInfo") != null);
    return (Map<String, Object>) page.evaluate("window.__dropInfo");
  }

  @Test
  void shouldDropFilePayload() {
    setupDropzone();
    page.locator("#dropzone").drop(new DropPayload().setFiles(new FilePayload("note.txt", "text/plain", "hello".getBytes(StandardCharsets.UTF_8))));
    Map<String, Object> info = waitForDropInfo();
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> files = (List<Map<String, Object>>) info.get("files");
    assertEquals(1, files.size());
    assertEquals("note.txt", files.get(0).get("name"));
    assertEquals("text/plain", files.get(0).get("type"));
    assertEquals("hello", files.get(0).get("text"));
  }

  @Test
  void shouldDropMultipleFilePayloads() {
    setupDropzone();
    page.locator("#dropzone").drop(new DropPayload().setFiles(new FilePayload[] {
      new FilePayload("a.txt", "text/plain", "AAA".getBytes(StandardCharsets.UTF_8)),
      new FilePayload("b.txt", "text/plain", "BB".getBytes(StandardCharsets.UTF_8)),
    }));
    Map<String, Object> info = waitForDropInfo();
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> files = (List<Map<String, Object>>) info.get("files");
    assertEquals(2, files.size());
    assertEquals("a.txt", files.get(0).get("name"));
    assertEquals("AAA", files.get(0).get("text"));
    assertEquals("b.txt", files.get(1).get("name"));
    assertEquals("BB", files.get(1).get("text"));
  }

  @Test
  void shouldDropClipboardLikeData() {
    setupDropzone();
    Map<String, String> data = new HashMap<>();
    data.put("text/plain", "hello world");
    data.put("text/uri-list", "https://example.com");
    page.locator("#dropzone").drop(new DropPayload().setData(data));
    Map<String, Object> info = waitForDropInfo();
    @SuppressWarnings("unchecked")
    List<?> files = (List<?>) info.get("files");
    assertTrue(files.isEmpty(), "expected no files");
    @SuppressWarnings("unchecked")
    Map<String, String> droppedData = (Map<String, String>) info.get("data");
    assertEquals("hello world", droppedData.get("text/plain"));
    assertEquals("https://example.com", droppedData.get("text/uri-list"));
  }

  @Test
  void shouldDropFileByLocalPath(@org.junit.jupiter.api.io.TempDir Path dir) throws Exception {
    setupDropzone();
    Path filePath = dir.resolve("hello.txt");
    Files.write(filePath, "path-content".getBytes(StandardCharsets.UTF_8));
    page.locator("#dropzone").drop(new DropPayload().setFiles(filePath));
    Map<String, Object> info = waitForDropInfo();
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> files = (List<Map<String, Object>>) info.get("files");
    assertEquals(1, files.size());
    assertEquals("hello.txt", files.get(0).get("name"));
    assertEquals("path-content", files.get(0).get("text"));
  }
}
