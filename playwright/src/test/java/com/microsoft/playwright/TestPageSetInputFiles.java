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

import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.FilePayload;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.io.TempDir;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static com.microsoft.playwright.Utils.chromiumVersionLessThan;
import static com.microsoft.playwright.Utils.relativePathOrSkipTest;
import static com.microsoft.playwright.fixtures.BrowserName.CHROMIUM;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;


public class TestPageSetInputFiles {
  static Path FILE_TO_UPLOAD = Paths.get("src/test/resources/file-to-upload.txt");

  @PlaywrightTest
  void shouldUploadTheFile(Page page, Server server) {
    page.navigate(server.PREFIX + "/input/fileupload.html");
    ElementHandle input = page.querySelector("input");
    input.setInputFiles(FILE_TO_UPLOAD);
    assertEquals("file-to-upload.txt", page.evaluate("e => e.files[0].name", input));
    assertEquals("contents of the file", page.evaluate("e => {\n" +
      "  const reader = new FileReader();\n" +
      "  const promise = new Promise(fulfill => reader.onload = fulfill);\n" +
      "  reader.readAsText(e.files[0]);\n" +
      "  return promise.then(() => reader.result);\n" +
      "}", input));
  }

  @PlaywrightTest
  void shouldUploadLargeFile(Page page, Server server, @TempDir Path tmpDir) throws IOException, ExecutionException, InterruptedException {
    Assumptions.assumeTrue(3 <= (Runtime.getRuntime().maxMemory() >> 30), "Fails if max heap size is < 3Gb");
    page.navigate(server.PREFIX + "/input/fileupload.html");
    Path uploadFile = tmpDir.resolve("200MB.zip");
    String str = String.join("", Collections.nCopies(4 * 1024, "A"));

    try (Writer stream = new OutputStreamWriter(Files.newOutputStream(uploadFile))) {
      for (int i = 0; i < 50 * 1024; i++) {
        stream.write(str);
      }
    }
    Locator input = page.locator("input[type='file']");
    JSHandle events = input.evaluateHandle("e => {\n" +
      "    const events = [];\n" +
      "    e.addEventListener('input', () => events.push('input'));\n" +
      "    e.addEventListener('change', () => events.push('change'));\n" +
      "    return events;\n" +
      "  }");
    input.setInputFiles(uploadFile);
    assertEquals("200MB.zip", input.evaluate("e => e.files[0].name"));
    assertEquals(asList("input", "change"), events.evaluate("e => e"));
    CompletableFuture<MultipartFormData> formData = new CompletableFuture<>();
    server.setRoute("/upload", exchange -> {
      try {
        MultipartFormData multipartFormData = MultipartFormData.parseRequest(exchange);
        formData.complete(multipartFormData);
      } catch (Exception e) {
        e.printStackTrace();
        formData.completeExceptionally(e);
      }
      exchange.sendResponseHeaders(200, -1);
    });
    page.click("input[type=submit]", new Page.ClickOptions().setTimeout(90_000));
    List<MultipartFormData.Field> fields = formData.get().fields;
    assertEquals(1, fields.size());
    assertEquals("200MB.zip", fields.get(0).filename);
    assertEquals(200 * 1024 * 1024, fields.get(0).content.length());
  }

  @PlaywrightTest
  void shouldUploadMultipleLargeFiles(Page page, Server server, @TempDir Path tmpDir) throws IOException, ExecutionException, InterruptedException {
    Assumptions.assumeTrue(3 <= (Runtime.getRuntime().maxMemory() >> 30), "Fails if max heap size is < 3Gb");
    int filesCount = 10;
    page.navigate(server.PREFIX + "/input/fileupload-multi.html");
    Path uploadFile = tmpDir.resolve("50MB_1.zip");
    String str = String.join("", Collections.nCopies(1024, "A"));

    try (Writer stream = new OutputStreamWriter(Files.newOutputStream(uploadFile))) {
      for (int i = 0; i < 49 * 1024; i++) {
        stream.write(str);
      }
    }
    Locator input = page.locator("input[type='file']");
    List<Path> uploadFiles = new ArrayList<>();
    uploadFiles.add(uploadFile);
    for (int i = 1; i < filesCount; i++) {
      Path dstFile = tmpDir.resolve("50MB_" + i + ".zip");
      Files.copy(uploadFile, dstFile);
      uploadFiles.add(dstFile);
    }
    FileChooser fileChooser = page.waitForFileChooser(() -> input.click());
    fileChooser.setFiles(uploadFiles.toArray(new Path[0]));
    Object filesLen = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Choose File")).evaluate("e => e.files.length");
    assertTrue(fileChooser.isMultiple());
    assertEquals(filesCount, filesLen);
  }

  @PlaywrightTest
  void shouldUploadLargeFileWithRelativePath(Page page, Server server, @TempDir Path tmpDir) throws IOException, ExecutionException, InterruptedException {
    Assumptions.assumeTrue(3 <= (Runtime.getRuntime().maxMemory() >> 30), "Fails if max heap size is < 3Gb");
    page.navigate(server.PREFIX + "/input/fileupload.html");
    Path uploadFile = tmpDir.resolve("200MB.zip");
    String str = String.join("", Collections.nCopies(4 * 1024, "A"));

    try (Writer stream = new OutputStreamWriter(Files.newOutputStream(uploadFile))) {
      for (int i = 0; i < 50 * 1024; i++) {
        stream.write(str);
      }
    }
    Locator input = page.locator("input[type='file']");
    JSHandle events = input.evaluateHandle("e => {\n" +
      "    const events = [];\n" +
      "    e.addEventListener('input', () => events.push('input'));\n" +
      "    e.addEventListener('change', () => events.push('change'));\n" +
      "    return events;\n" +
      "  }");

    Path relativeUploadPath = relativePathOrSkipTest(uploadFile);
    assertFalse(relativeUploadPath.isAbsolute());
    input.setInputFiles(relativeUploadPath);
    assertEquals("200MB.zip", input.evaluate("e => e.files[0].name"));
    assertEquals(asList("input", "change"), events.evaluate("e => e"));
    CompletableFuture<MultipartFormData> formData = new CompletableFuture<>();
    server.setRoute("/upload", exchange -> {
      try {
        MultipartFormData multipartFormData = MultipartFormData.parseRequest(exchange);
        formData.complete(multipartFormData);
      } catch (Exception e) {
        e.printStackTrace();
        formData.completeExceptionally(e);
      }
      exchange.sendResponseHeaders(200, -1);
    });
    page.click("input[type=submit]", new Page.ClickOptions().setTimeout(90_000));
    List<MultipartFormData.Field> fields = formData.get().fields;
    assertEquals(1, fields.size());
    assertEquals("200MB.zip", fields.get(0).filename);
    assertEquals(200 * 1024 * 1024, fields.get(0).content.length());
  }

  @PlaywrightTest
  void shouldWork(Page page) {
    page.setContent("<input type=file>");
    page.setInputFiles("input", FILE_TO_UPLOAD);
    assertEquals(1, page.evalOnSelector("input", "input => input.files.length"));
    assertEquals("file-to-upload.txt", page.evalOnSelector("input", "input => input.files[0].name"));
  }

  @PlaywrightTest
  void shouldSetFromMemory(Page page) {
    page.setContent("<input type=file>");
    page.setInputFiles("input", new FilePayload("test.txt","text/plain","this is a test".getBytes()));
    assertEquals(1, page.evalOnSelector("input", "input => input.files.length"));
    assertEquals("test.txt", page.evalOnSelector("input", "input => input.files[0].name"));
  }

  @PlaywrightTest
  void shouldEmitEventOnce(Page page) {
    page.setContent("<input type=file>");
    FileChooser chooser = page.waitForFileChooser(() -> page.click("input"));
    assertNotNull(chooser);
  }

  void shouldEmitEventOnOff() {
    // Not applicable in Java.
  }

  @PlaywrightTest
  void shouldEmitEventAddListenerRemoveListener(Page page) {
    page.setContent("<input type=file>");
    FileChooser[] chooser = { null };
    page.onFileChooser(new Consumer<FileChooser>() {
      @Override
      public void accept(FileChooser fileChooser) {
        chooser[0] = fileChooser;
        page.offFileChooser(this);
      }
    });
    page.click("input");
    Instant start = Instant.now();
    while (chooser[0] == null && Duration.between(start, Instant.now()).toMillis() < 10_000) {
      page.waitForTimeout(100);
    }
    assertNotNull(chooser[0]);
  }

  @PlaywrightTest
  void shouldWorkWhenFileInputIsAttachedToDOM(Page page) {
    page.setContent("<input type=file>");
    FileChooser chooser = page.waitForFileChooser(() -> page.click("input"));
    assertNotNull(chooser);
  }

  @PlaywrightTest
  void shouldWorkWhenFileInputIsNotAttachedToDOM(Page page) {
    FileChooser chooser = page.waitForFileChooser(() -> {
      page.evaluate("() => {\n" +
        "  const el = document.createElement('input');\n" +
        "  el.type = 'file';\n" +
        "  el.click();\n" +
        "}");
    });
    assertNotNull(chooser);
  }

  @PlaywrightTest
  void shouldWorkWithCSP(Page page, Server server) {
    server.setCSP("/empty.html", "default-src 'none'");
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<input type=file>");
    page.setInputFiles("input", FILE_TO_UPLOAD);
    assertEquals(1, page.evalOnSelector("input", "input => input.files.length"));
    assertEquals("file-to-upload.txt", page.evalOnSelector("input", "input => input.files[0].name"));
  }

  @PlaywrightTest
  void shouldRespectTimeout(Page page) {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.waitForFileChooser(new Page.WaitForFileChooserOptions().setTimeout(1), () -> {});
    });
    assertTrue(e.getMessage().contains("Timeout 1ms exceeded"));
  }

  @PlaywrightTest
  void shouldRespectDefaultTimeoutWhenThereIsNoCustomTimeout(Page page) {
    page.setDefaultTimeout(1);
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.waitForFileChooser(() -> {});
    });
    assertTrue(e.getMessage().contains("Timeout 1ms exceeded"));
  }

  @PlaywrightTest
  void shouldPrioritizeExactTimeoutOverDefaultTimeout(Page page) {
    page.setDefaultTimeout(0);
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.waitForFileChooser(new Page.WaitForFileChooserOptions().setTimeout(1), () -> {});
    });
    assertTrue(e.getMessage().contains("Timeout 1ms exceeded"));
  }

  @PlaywrightTest
  void shouldWorkWithNoTimeout(Page page) {
    FileChooser fileChooser = page.waitForFileChooser(new Page.WaitForFileChooserOptions().setTimeout(0), () -> {
      page.evaluate("() => setTimeout(() => {\n" +
        "  const el = document.createElement('input');\n" +
        "  el.type = 'file';\n" +
        "  el.click();\n" +
        "}, 50)");
    });
    assertNotNull(fileChooser);
  }

  @PlaywrightTest
  void shouldReturnTheSameFileChooserWhenThereAreManyWatchdogsSimultaneously(Page page) {
    page.setContent("<input type=file>");
    FileChooser[] fileChooser = {null};
    FileChooser fileChooser1 = page.waitForFileChooser(() -> {
      fileChooser[0] = page.waitForFileChooser(() -> {
        page.evalOnSelector("input", "input => input.click()");
      });
    });
    assertEquals(fileChooser[0], fileChooser1);
  }

  @PlaywrightTest
  void shouldAcceptSingleFile(Page page) {
    page.setContent("<input type=file oninput='javascript:console.timeStamp()'>");
    FileChooser fileChooser = page.waitForFileChooser(() -> page.click("input"));
    assertEquals(page, fileChooser.page());
    assertNotNull(fileChooser.element());
    fileChooser.setFiles(FILE_TO_UPLOAD);
    assertEquals(1, page.evalOnSelector("input", "input => input.files.length"));
    assertEquals("file-to-upload.txt", page.evalOnSelector("input", "input => input.files[0].name"));
  }

  @PlaywrightTest
  void shouldAcceptSingleFilePayload(Page page) {
    page.setContent("<input type=file oninput='javascript:console.timeStamp()'>");
    FileChooser fileChooser = page.waitForFileChooser(() -> page.click("input"));
    assertEquals(page, fileChooser.page());
    assertNotNull(fileChooser.element());
    fileChooser.setFiles(new FilePayload("test.txt", "text/plain", "Hello!".getBytes()));
    assertEquals(1, page.evalOnSelector("input", "input => input.files.length"));
    assertEquals("test.txt", page.evalOnSelector("input", "input => input.files[0].name"));
  }

  //  @PlaywrightTest
  void shouldDetectMimeType() throws ExecutionException, InterruptedException {
    // TODO: Parse form fields on server
  }

  @PlaywrightTest
  void shouldBeAbleToReadSelectedFile(Page page) {
    page.setContent("<input type=file>");
    page.onFileChooser(fileChooser -> {
      fileChooser.setFiles(FILE_TO_UPLOAD);
    });
    Object content = page.evalOnSelector("input", "async picker => {\n" +
      "  picker.click();\n" +
      "  await new Promise(x => picker.oninput = x);\n" +
      "  const reader = new FileReader();\n" +
      "  const promise = new Promise(fulfill => reader.onload = fulfill);\n" +
      "  reader.readAsText(picker.files[0]);\n" +
      "  return promise.then(() => reader.result);\n" +
      "}");
    assertEquals("contents of the file", content);
  }

  @PlaywrightTest
  void shouldBeAbleToResetSelectedFilesWithEmptyFileList(Page page) {
    page.setContent("<input type=file>");
    page.onFileChooser(new Consumer<FileChooser>() {
      @Override
      public void accept(FileChooser fileChooser) {
        fileChooser.setFiles(FILE_TO_UPLOAD);
        page.offFileChooser(this);
      }
    });
    Object fileLength1 = page.evalOnSelector("input", "async picker => {\n" +
      "  picker.click();\n" +
      "  await new Promise(x => picker.oninput = x);\n" +
      "  return picker.files.length;\n" +
      "}");
    assertEquals(1, fileLength1);

    page.onFileChooser(new Consumer<FileChooser>() {
      @Override
      public void accept(FileChooser fileChooser) {
        fileChooser.setFiles(new Path[0]);
        page.offFileChooser(this);
      }
    });
    Object fileLength2 = page.evalOnSelector("input", "async picker => {\n" +
      "  picker.click();\n" +
      "  await new Promise(x => picker.oninput = x);\n" +
      "  return picker.files.length;\n" +
      "}");
    assertEquals(0, fileLength2);
  }

  @PlaywrightTest
  void shouldNotAcceptMultipleFilesForSingleFileInput(Page page) {
    page.setContent("<input type=file>");
    FileChooser fileChooser = page.waitForFileChooser(() -> page.click("input"));
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      fileChooser.setFiles(new Path[]{FILE_TO_UPLOAD, Paths.get("src/test/resources/pptr.png")});
    });
    assertTrue(e.getMessage().contains("Non-multiple file input can only accept single file"));
  }

  @PlaywrightTest
  void shouldEmitInputAndChangeEvents(Page page) {
    List<Object> events = new ArrayList<>();
    page.exposeFunction("eventHandled", args -> events.add(args[0]));
    page.setContent("<input id=input type=file></input>\n" +
      "<script>\n" +
      "  input.addEventListener('input', e => eventHandled(e.type));\n" +
      "  input.addEventListener('change', e => eventHandled(e.type));\n" +
      "</script>");
    page.querySelector("input").setInputFiles(FILE_TO_UPLOAD);
    assertEquals(asList("input", "change"), events);
  }

  @PlaywrightTest
  void shouldWorkForSingleFilePick(Page page) {
    page.setContent("<input type=file>");
    FileChooser fileChooser = page.waitForFileChooser(() -> page.click("input"));
    assertFalse(fileChooser.isMultiple());
  }

  @PlaywrightTest
  void shouldWorkForMultiple(Page page) {
    page.setContent("<input multiple type=file>");
    FileChooser fileChooser = page.waitForFileChooser(() -> page.click("input"));
    assertTrue(fileChooser.isMultiple());
  }

  @PlaywrightTest
  void shouldWorkForWebkitdirectory(Page page) {
    page.setContent("<input multiple webkitdirectory type=file>");
    FileChooser fileChooser = page.waitForFileChooser(() -> page.click("input"));
    assertTrue(fileChooser.isMultiple());
  }

  @PlaywrightTest
  void shouldPreserveLastModifiedTimestamp(Page page) throws IOException {
    page.setContent("<input type=file multiple=true/>");
    Locator input = page.locator("input");
    input.setInputFiles(FILE_TO_UPLOAD);
    assertEquals(asList("file-to-upload.txt"), input.evaluate("e => [...e.files].map(f => f.name)"));
    List<Double> timestamps = (List<Double>) input.evaluate("e => [...e.files].map(f => f.lastModified)");
    FileTime expected = Files.getLastModifiedTime(FILE_TO_UPLOAD);
    // On Linux browser sometimes reduces the timestamp by 1ms: 1696272058110.0715  -> 1696272058109 or even
    // rounds it to seconds in WebKit: 1696272058110 -> 1696272058000.
    assertTrue(Math.abs(timestamps.get(0) - expected.toMillis()) < 1000, "expected: " + expected.toMillis() + "; actual: " + timestamps.get(0));
  }

  static void writeFile(Path file, String content) throws IOException {
    try (Writer stream = new OutputStreamWriter(Files.newOutputStream(file))) {
        stream.write(content);
    }
  }

  @PlaywrightTest
  void shouldUploadAFolder(Page page, Browser browser, Server server, BrowserName browserName, @TempDir Path tmpDir) throws IOException {
    page.navigate(server.PREFIX + "/input/folderupload.html");
    Locator input = page.locator("input[name=\"file1\"]");
    Path dir = tmpDir.resolve("file-upload-test");
    Files.createDirectories(dir);
    writeFile(dir.resolve("file1.txt"), "file1 content");
    writeFile(dir.resolve("file2"), "file2 content");
    Files.createDirectories(dir.resolve("sub-dir"));
    writeFile(dir.resolve("sub-dir").resolve("really.txt"), "sub-dir file content");
    input.setInputFiles(dir);
    List<String> webkitRelativePaths = (List<String>) input.evaluate("e => [...e.files].map(f => f.webkitRelativePath)");

    List<String> relativePathsSorted = new ArrayList<>(webkitRelativePaths);
    relativePathsSorted.sort(String::compareTo);
    // https://issues.chromium.org/issues/345393164
    if (browserName == CHROMIUM && !Env.headed() && chromiumVersionLessThan(browser.version(), "127.0.6533.0")) {
      assertEquals(asList("file-upload-test/file1.txt", "file-upload-test/file2"), relativePathsSorted);
    } else {
      assertEquals(asList("file-upload-test/file1.txt", "file-upload-test/file2", "file-upload-test/sub-dir/really.txt"), relativePathsSorted);
    }

    for (int i = 0; i < webkitRelativePaths.size(); i++) {
      String relativePath = webkitRelativePaths.get(i);
      Object content = input.evaluate("(e, i) => {\n" +
        "      const reader = new FileReader();\n" +
        "      const promise = new Promise(fulfill => reader.onload = fulfill);\n" +
        "      reader.readAsText(e.files[i]);\n" +
        "      return promise.then(() => reader.result);\n" +
        "    }", i);
      String expectedContent = new String(Files.readAllBytes(tmpDir.resolve(relativePath)));
      assertEquals(expectedContent, content);
    }
  }

  @PlaywrightTest
  void shouldUploadAFolderAndThrowForMultipleDirectories(Page page, Server server, @TempDir Path tmpDir) throws IOException {
    page.navigate(server.PREFIX + "/input/folderupload.html");
    Locator input = page.locator("input[name=\"file1\"]");
    Path dir = tmpDir.resolve("file-upload-test");
    Files.createDirectories(dir.resolve("folder1"));
    writeFile(dir.resolve("folder1").resolve("file1.txt"), "file1 content");
    Files.createDirectories(dir.resolve("folder2"));
    writeFile(dir.resolve("folder2").resolve("file2.txt"), "file2 content");
    PlaywrightException e = assertThrows(PlaywrightException.class,
      () -> input.setInputFiles(new Path[]{dir.resolve("folder1"), dir.resolve("folder2")}));
    assertTrue(e.getMessage().contains("Multiple directories are not supported"), e.getMessage());
  }

  @PlaywrightTest
  void shouldThrowIfADirectoryAndFilesArePassed(Page page, Server server, @TempDir Path tmpDir) throws IOException {
    // Skipping conditions based on environment not directly translatable to Java; needs custom implementation
    page.navigate(server.PREFIX + "/input/folderupload.html");
    Locator input = page.locator("input[name=\"file1\"]");
    Path dir = tmpDir.resolve("file-upload-test");
    Files.createDirectories(dir.resolve("folder1"));
    writeFile(dir.resolve("folder1").resolve("file1.txt"), "file1 content");
    PlaywrightException e = assertThrows(PlaywrightException.class,
      () -> input.setInputFiles(new Path[]{ dir.resolve("folder1"), dir.resolve("folder1").resolve("file1.txt") }));
    assertTrue(e.getMessage().contains("File paths must be all files or a single directory"), e.getMessage());
  }

  @PlaywrightTest
  void shouldThrowWhenUploadingAFolderInANormalFileUploadInput(Page page, Server server, @TempDir Path tmpDir) throws IOException {
    page.navigate(server.PREFIX + "/input/fileupload.html");
    Locator input = page.locator("input[name=\"file1\"]");
    Path dir = tmpDir.resolve("file-upload-test");
    Files.createDirectories(dir);
    writeFile(dir.resolve("file1.txt"), "file1 content");
    PlaywrightException e = assertThrows(PlaywrightException.class,
      () -> input.setInputFiles(dir));
    assertTrue(e.getMessage().contains("File input does not support directories, pass individual files instead"), e.getMessage());
  }
}

