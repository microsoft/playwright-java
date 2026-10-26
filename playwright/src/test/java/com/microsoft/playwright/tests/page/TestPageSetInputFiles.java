/*
 * Copyright 2017 Google Inc. All rights reserved.
 * Modifications copyright (c) Microsoft Corporation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestInfo;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.FilePayload;
import org.junit.jupiter.api.DisplayName;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.microsoft.playwright.fixtures.Utils.asset;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-set-input-files.spec.ts. */
public class TestPageSetInputFiles {
  @PlaywrightTest
  @DisplayName("IF-001 should upload the file")
  void shouldUploadTheFile(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/fileupload.html");
    Path filePath = Paths.get("").toAbsolutePath().relativize(asset("file-to-upload.txt"));
    ElementHandle input = page.querySelector("input");
    input.setInputFiles(filePath);
    assertEquals("file-to-upload.txt", page.evaluate("e => e.files[0].name", input));
    assertEquals("contents of the file", page.evaluate("e => {\n" +
      "    const reader = new FileReader();\n" +
      "    const promise = new Promise(fulfill => reader.onload = fulfill);\n" +
      "    reader.readAsText(e.files[0]);\n" +
      "    return promise.then(() => reader.result);\n" +
      "  }", input));
  }

  @PlaywrightTest
  @DisplayName("IF-002 should upload a folder")
  void shouldUploadAFolder(Page page, TestServer server, TestInfo testInfo) throws Exception {
    page.navigate(server.PREFIX + "/input/folderupload.html");
    ElementHandle input = page.querySelector("input");
    Path dir = testInfo.outputDir().resolve("file-upload-test");
    {
      Files.createDirectories(dir);
      Files.write(dir.resolve("file1.txt"), "file1 content".getBytes(StandardCharsets.UTF_8));
      Files.write(dir.resolve("file2"), "file2 content".getBytes(StandardCharsets.UTF_8));
      Files.createDirectory(dir.resolve("sub-dir"));
      Files.write(dir.resolve("sub-dir").resolve("really.txt"), "sub-dir file content".getBytes(StandardCharsets.UTF_8));
    }
    input.setInputFiles(dir);
    assertEquals(new HashSet<Object>(Arrays.asList(
      // Note: this did not work before Chrome 127, see https://issues.chromium.org/issues/345393164.
      "file-upload-test/sub-dir/really.txt",
      "file-upload-test/file1.txt",
      "file-upload-test/file2")),
      new HashSet<Object>((List<?>) page.evaluate("e => [...e.files].map(f => f.webkitRelativePath)", input)));
    List<?> webkitRelativePaths = (List<?>) page.evaluate("e => [...e.files].map(f => f.webkitRelativePath)", input);
    for (int i = 0; i < webkitRelativePaths.size(); i++) {
      Object content = input.evaluate("(e, i) => {\n" +
        "      const reader = new FileReader();\n" +
        "      const promise = new Promise(fulfill => reader.onload = fulfill);\n" +
        "      reader.readAsText(e.files[i]);\n" +
        "      return promise.then(() => reader.result);\n" +
        "    }", i);
      assertEquals(new String(Files.readAllBytes(dir.resolve("..").resolve((String) webkitRelativePaths.get(i))), StandardCharsets.UTF_8), content);
    }
  }

  @PlaywrightTest
  @DisplayName("IF-003 should upload a folder via label")
  @Fixme("driver: times out with 'Timeout 30000ms exceeded' (waiting for getByText(\"Folder\"), locator resolved to <label for=\"folder\">Folder</label>); the driver 1.64.1 predates upstream's fix for setInputFiles on a label of a folder input")
  void shouldUploadAFolderViaLabel(Page page, TestInfo testInfo) throws Exception {
    page.setContent("<label for=\"folder\">Folder</label><input id=\"folder\" type=\"file\" webkitdirectory>");
    Path dir = testInfo.outputDir().resolve("file-upload-test");
    Files.createDirectories(dir);
    Files.write(dir.resolve("file1.txt"), "file1 content".getBytes(StandardCharsets.UTF_8));
    page.getByText("Folder").setInputFiles(dir);
    assertEquals(Arrays.asList("file-upload-test/file1.txt"), page.locator("input").evaluate("e => [...e.files].map(f => f.webkitRelativePath)"));
  }

  @PlaywrightTest
  @DisplayName("IF-004 should upload a folder and throw for multiple directories")
  void shouldUploadAFolderAndThrowForMultipleDirectories(Page page, TestServer server, TestInfo testInfo) throws Exception {
    page.navigate(server.PREFIX + "/input/folderupload.html");
    ElementHandle input = page.querySelector("input");
    Path dir = testInfo.outputDir().resolve("file-upload-test");
    {
      Files.createDirectories(dir.resolve("folder1"));
      Files.write(dir.resolve("folder1").resolve("file1.txt"), "file1 content".getBytes(StandardCharsets.UTF_8));
      Files.createDirectories(dir.resolve("folder2"));
      Files.write(dir.resolve("folder2").resolve("file2.txt"), "file2 content".getBytes(StandardCharsets.UTF_8));
    }
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> input.setInputFiles(new Path[]{
      dir.resolve("folder1"),
      dir.resolve("folder2")}));
    assertTrue(error.getMessage().contains("Multiple directories are not supported"));
  }

  @PlaywrightTest
  @DisplayName("IF-005 should throw if a directory and files are passed")
  void shouldThrowIfADirectoryAndFilesArePassed(Page page, TestServer server, TestInfo testInfo) throws Exception {
    page.navigate(server.PREFIX + "/input/folderupload.html");
    ElementHandle input = page.querySelector("input");
    Path dir = testInfo.outputDir().resolve("file-upload-test");
    {
      Files.createDirectories(dir.resolve("folder1"));
      Files.write(dir.resolve("folder1").resolve("file1.txt"), "file1 content".getBytes(StandardCharsets.UTF_8));
    }
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> input.setInputFiles(new Path[]{
      dir.resolve("folder1"),
      dir.resolve("folder1").resolve("file1.txt")}));
    assertTrue(error.getMessage().contains("File paths must be all files or a single directory"));
  }

  @PlaywrightTest
  @DisplayName("IF-006 should throw when uploading a folder in a normal file upload input")
  void shouldThrowWhenUploadingAFolderInANormalFileUploadInput(Page page, TestServer server, TestInfo testInfo) throws Exception {
    page.navigate(server.PREFIX + "/input/fileupload.html");
    ElementHandle input = page.querySelector("input");
    Path dir = testInfo.outputDir().resolve("file-upload-test");
    {
      Files.createDirectories(dir);
      Files.write(dir.resolve("file1.txt"), "file1 content".getBytes(StandardCharsets.UTF_8));
    }
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> input.setInputFiles(dir));
    assertTrue(error.getMessage().contains("File input does not support directories, pass individual files instead"));
  }

  @PlaywrightTest
  @DisplayName("IF-007 should throw when uploading a file in a directory upload input")
  void shouldThrowWhenUploadingAFileInADirectoryUploadInput(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/folderupload.html");
    ElementHandle input = page.querySelector("input");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> input.setInputFiles(asset("file to upload.txt")));
    assertTrue(error.getMessage().contains("[webkitdirectory] input requires passing a path to a directory"));
  }

  @PlaywrightTest
  @DisplayName("IF-008 should upload a file after popup")
  void shouldUploadAFileAfterPopup(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/fileupload.html");
    {
      Page popup = page.waitForPopup(() -> page.evaluate("() => window['__popup'] = window.open('about:blank')"));
      popup.close();
    }
    Path filePath = Paths.get("").toAbsolutePath().relativize(asset("file-to-upload.txt"));
    ElementHandle input = page.querySelector("input");
    input.setInputFiles(filePath);
    assertEquals("file-to-upload.txt", page.evaluate("e => e.files[0].name", input));
  }

  @PlaywrightTest
  @DisplayName("IF-010 should throw an error if the file does not exist")
  @Fixme("java: the message is 'Cannot get absolute file path', it has no 'ENOENT: no such file or directory' and no file name")
  void shouldThrowAnErrorIfTheFileDoesNotExist(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/fileupload.html");
    ElementHandle input = page.querySelector("input");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> input.setInputFiles(Paths.get("i actually do not exist.txt")));
    assertTrue(error.getMessage().contains("ENOENT: no such file or directory"));
    assertTrue(error.getMessage().contains("i actually do not exist.txt"));
  }

  @PlaywrightTest
  @DisplayName("IF-012 should upload the file with spaces in name")
  void shouldUploadTheFileWithSpacesInName(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/input/fileupload.html");
    Path filePath = Paths.get("").toAbsolutePath().relativize(asset("file to upload.txt"));
    ElementHandle input = page.querySelector("input");
    input.setInputFiles(filePath);
    assertEquals("file to upload.txt", page.evaluate("e => e.files[0].name", input));
    assertEquals("contents of the file", page.evaluate("e => {\n" +
      "    const reader = new FileReader();\n" +
      "    const promise = new Promise(fulfill => reader.onload = fulfill);\n" +
      "    reader.readAsText(e.files[0]);\n" +
      "    return promise.then(() => reader.result);\n" +
      "  }", input));
  }

  @PlaywrightTest
  @DisplayName("IF-013 should work @smoke")
  void shouldWork(Page page) {
    page.setContent("<input type=file>");
    page.setInputFiles("input", asset("file-to-upload.txt"));
    assertEquals(1, page.evalOnSelector("input", "input => input.files.length"));
    assertEquals("file-to-upload.txt", page.evalOnSelector("input", "input => input.files[0].name"));
  }

  @PlaywrightTest
  @DisplayName("IF-014 should set from memory")
  void shouldSetFromMemory(Page page) {
    page.setContent("<input type=file>");
    page.setInputFiles("input", new FilePayload("test.txt", "text/plain", "this is a test".getBytes(StandardCharsets.UTF_8)));
    assertEquals(1, page.evalOnSelector("input", "input => input.files.length"));
    assertEquals("test.txt", page.evalOnSelector("input", "input => input.files[0].name"));
  }

  @PlaywrightTest
  @DisplayName("IF-015 should work with CSP")
  void shouldWorkWithCSP(Page page, TestServer server) {
    server.setCSP("/empty.html", "default-src \"none\"");
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<input type=file>");
    page.setInputFiles("input", asset("file-to-upload.txt"));
    assertEquals(1, page.evalOnSelector("input", "input => input.files.length"));
    assertEquals("file-to-upload.txt", page.evalOnSelector("input", "input => input.files[0].name"));
  }

  @PlaywrightTest
  @DisplayName("IF-018 should emit input and change events")
  void shouldEmitInputAndChangeEvents(Page page) {
    List<Object> events = new ArrayList<>();
    page.exposeFunction("eventHandled", args -> events.add(args[0]));
    page.setContent("\n" +
      "  <input id=input type=file></input>\n" +
      "  <script>\n" +
      "    input.addEventListener('input', e => eventHandled({ type: e.type }));\n" +
      "    input.addEventListener('change', e => eventHandled({ type: e.type }));\n" +
      "  </script>");
    page.querySelector("input").setInputFiles(asset("file-to-upload.txt"));
    assertEquals(2, events.size());
    assertEquals("input", ((Map<?, ?>) events.get(0)).get("type"));
    assertEquals("change", ((Map<?, ?>) events.get(1)).get("type"));
  }

  @PlaywrightTest
  @DisplayName("IF-019 input event.composed should be true and cross shadow dom boundary")
  void inputEventComposedShouldBeTrueAndCrossShadowDomBoundary(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<body><script>\n" +
      "  const div = document.createElement('div');\n" +
      "  const shadowRoot = div.attachShadow({mode: 'open'});\n" +
      "  shadowRoot.innerHTML = '<input type=file></input>';\n" +
      "  document.body.appendChild(div);\n" +
      "</script></body>");
    page.locator("body").evaluate("select => {\n" +
      "    window.firedBodyEvents = [];\n" +
      "    for (const event of ['input', 'change']) {\n" +
      "      select.addEventListener(event, e => {\n" +
      "        window.firedBodyEvents.push(e.type + ':' + e.composed);\n" +
      "      }, false);\n" +
      "    }\n" +
      "  }");

    page.locator("input").evaluate("select => {\n" +
      "    window.firedEvents = [];\n" +
      "    for (const event of ['input', 'change']) {\n" +
      "      select.addEventListener(event, e => {\n" +
      "        window.firedEvents.push(e.type + ':' + e.composed);\n" +
      "      }, false);\n" +
      "    }\n" +
      "  }");
    page.locator("input").setInputFiles(new FilePayload("test.txt", "text/plain", "this is a test".getBytes(StandardCharsets.UTF_8)));
    assertEquals(Arrays.asList("input:true", "change:false"), page.evaluate("() => window['firedEvents']"));
    assertEquals(Arrays.asList("input:true"), page.evaluate("() => window['firedBodyEvents']"));
  }

  @PlaywrightTest
  @DisplayName("IF-020 input should trigger events when files changed second time")
  void inputShouldTriggerEventsWhenFilesChangedSecondTime(Page page) {
    page.setContent("<input type=file multiple=true/>");

    Locator input = page.locator("input");
    JSHandle events = input.evaluateHandle("e => {\n" +
      "    const events = [];\n" +
      "    e.addEventListener('input', () => events.push('input'));\n" +
      "    e.addEventListener('change', () => events.push('change'));\n" +
      "    return events;\n" +
      "  }");

    input.setInputFiles(asset("file-to-upload.txt"));
    assertEquals("file-to-upload.txt", input.evaluate("e => e.files[0].name"));
    assertEquals(Arrays.asList("input", "change"), events.evaluate("e => e"));

    events.evaluate("e => e.length = 0");

    input.setInputFiles(asset("pptr.png"));
    assertEquals("pptr.png", input.evaluate("e => e.files[0].name"));
    assertEquals(Arrays.asList("input", "change"), events.evaluate("e => e"));
  }

  @PlaywrightTest
  @DisplayName("IF-021 should preserve lastModified timestamp")
  void shouldPreserveLastModifiedTimestamp(Page page) throws Exception {
    page.setContent("<input type=file multiple=true/>");
    Locator input = page.locator("input");
    List<String> files = Arrays.asList("file-to-upload.txt", "file-to-upload-2.txt");
    Path[] paths = new Path[files.size()];
    for (int i = 0; i < paths.length; i++) {
      paths[i] = asset(files.get(i));
    }
    input.setInputFiles(paths);
    assertEquals(files, input.evaluate("e => [...e.files].map(f => f.name)"));
    List<?> timestamps = (List<?>) input.evaluate("e => [...e.files].map(f => f.lastModified)");
    List<Long> expectedTimestamps = new ArrayList<>();
    for (String file : files) {
      expectedTimestamps.add(Files.getLastModifiedTime(asset(file)).toMillis());
    }
    // On Linux browser sometimes reduces the timestamp by 1ms: 1696272058110.0715  -> 1696272058109 or even
    // rounds it to seconds in WebKit: 1696272058110 -> 1696272058000.
    for (int i = 0; i < timestamps.size(); i++) {
      assertTrue(Math.abs(((Number) timestamps.get(i)).doubleValue() - expectedTimestamps.get(i)) <= 1000,
        "expected: " + expectedTimestamps + "; actual: " + timestamps);
    }
  }
}
