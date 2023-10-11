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

import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.FilePayload;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.platform.commons.support.AnnotationSupport;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import static com.microsoft.playwright.Utils.relativePathOrSkipTest;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;


public class TestPageSetInputFiles extends TestBase {
  static Path FILE_TO_UPLOAD = Paths.get("src/test/resources/file-to-upload.txt");

  @Test
  void shouldUploadTheFile() {
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

  @Test
  void shouldUploadLargeFile(@TempDir Path tmpDir) throws IOException, ExecutionException, InterruptedException {
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

  @Test
  void shouldUploadMultipleLargeFiles(@TempDir Path tmpDir) throws IOException, ExecutionException, InterruptedException {
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
    Object filesLen = page.getByRole(AriaRole.TEXTBOX).evaluate("e => e.files.length");
    assertTrue(fileChooser.isMultiple());
    assertEquals(filesCount, filesLen);
  }

  @Test
  void shouldUploadLargeFileWithRelativePath(@TempDir Path tmpDir) throws IOException, ExecutionException, InterruptedException {
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

  @Test
  void shouldWork() {
    page.setContent("<input type=file>");
    page.setInputFiles("input", FILE_TO_UPLOAD);
    assertEquals(1, page.evalOnSelector("input", "input => input.files.length"));
    assertEquals("file-to-upload.txt", page.evalOnSelector("input", "input => input.files[0].name"));
  }

  @Test
  void shouldSetFromMemory() {
    page.setContent("<input type=file>");
    page.setInputFiles("input", new FilePayload("test.txt","text/plain","this is a test".getBytes()));
    assertEquals(1, page.evalOnSelector("input", "input => input.files.length"));
    assertEquals("test.txt", page.evalOnSelector("input", "input => input.files[0].name"));
  }

  @Test
  void shouldEmitEventOnce() {
    page.setContent("<input type=file>");
    FileChooser chooser = page.waitForFileChooser(() -> page.click("input"));
    assertNotNull(chooser);
  }

  void shouldEmitEventOnOff() {
    // Not applicable in Java.
  }

  @Test
  void shouldEmitEventAddListenerRemoveListener() {
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

  @Test
  void shouldWorkWhenFileInputIsAttachedToDOM() {
    page.setContent("<input type=file>");
    FileChooser chooser = page.waitForFileChooser(() -> page.click("input"));
    assertNotNull(chooser);
  }

  @Test
  void shouldWorkWhenFileInputIsNotAttachedToDOM() {
    FileChooser chooser = page.waitForFileChooser(() -> {
      page.evaluate("() => {\n" +
        "  const el = document.createElement('input');\n" +
        "  el.type = 'file';\n" +
        "  el.click();\n" +
        "}");
    });
    assertNotNull(chooser);
  }

  @Test
  void shouldWorkWithCSP() {
    server.setCSP("/empty.html", "default-src 'none'");
    page.navigate(server.EMPTY_PAGE);
    page.setContent("<input type=file>");
    page.setInputFiles("input", FILE_TO_UPLOAD);
    assertEquals(1, page.evalOnSelector("input", "input => input.files.length"));
    assertEquals("file-to-upload.txt", page.evalOnSelector("input", "input => input.files[0].name"));
  }

  @Test
  void shouldRespectTimeout() {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.waitForFileChooser(new Page.WaitForFileChooserOptions().setTimeout(1), () -> {});
    });
    assertTrue(e.getMessage().contains("Timeout 1ms exceeded"));
  }

  @Test
  void shouldRespectDefaultTimeoutWhenThereIsNoCustomTimeout() {
    page.setDefaultTimeout(1);
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.waitForFileChooser(() -> {});
    });
    assertTrue(e.getMessage().contains("Timeout 1ms exceeded"));
  }

  @Test
  void shouldPrioritizeExactTimeoutOverDefaultTimeout() {
    page.setDefaultTimeout(0);
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.waitForFileChooser(new Page.WaitForFileChooserOptions().setTimeout(1), () -> {});
    });
    assertTrue(e.getMessage().contains("Timeout 1ms exceeded"));
  }

  @Test
  void shouldWorkWithNoTimeout() {
    FileChooser fileChooser = page.waitForFileChooser(new Page.WaitForFileChooserOptions().setTimeout(0), () -> {
      page.evaluate("() => setTimeout(() => {\n" +
        "  const el = document.createElement('input');\n" +
        "  el.type = 'file';\n" +
        "  el.click();\n" +
        "}, 50)");
    });
    assertNotNull(fileChooser);
  }

  @Test
  void shouldReturnTheSameFileChooserWhenThereAreManyWatchdogsSimultaneously() {
    page.setContent("<input type=file>");
    FileChooser[] fileChooser = {null};
    FileChooser fileChooser1 = page.waitForFileChooser(() -> {
      fileChooser[0] = page.waitForFileChooser(() -> {
        page.evalOnSelector("input", "input => input.click()");
      });
    });
    assertEquals(fileChooser[0], fileChooser1);
  }

  @Test
  void shouldAcceptSingleFile() {
    page.setContent("<input type=file oninput='javascript:console.timeStamp()'>");
    FileChooser fileChooser = page.waitForFileChooser(() -> page.click("input"));
    assertEquals(page, fileChooser.page());
    assertNotNull(fileChooser.element());
    fileChooser.setFiles(FILE_TO_UPLOAD);
    assertEquals(1, page.evalOnSelector("input", "input => input.files.length"));
    assertEquals("file-to-upload.txt", page.evalOnSelector("input", "input => input.files[0].name"));
  }

//  @Test
  void shouldDetectMimeType() throws ExecutionException, InterruptedException {
    // TODO: Parse form fields on server
  }

  @Test
  void shouldBeAbleToReadSelectedFile() {
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

  @Test
  void shouldBeAbleToResetSelectedFilesWithEmptyFileList() {
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

  @Test
  void shouldNotAcceptMultipleFilesForSingleFileInput() {
    page.setContent("<input type=file>");
    FileChooser fileChooser = page.waitForFileChooser(() -> page.click("input"));
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      fileChooser.setFiles(new Path[]{FILE_TO_UPLOAD, Paths.get("src/test/resources/pptr.png")});
    });
    assertTrue(e.getMessage().contains("Non-multiple file input can only accept single file"));
  }

  @Test
  void shouldEmitInputAndChangeEvents() {
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

  @Test
  void shouldWorkForSingleFilePick() {
    page.setContent("<input type=file>");
    FileChooser fileChooser = page.waitForFileChooser(() -> page.click("input"));
    assertFalse(fileChooser.isMultiple());
  }

  @Test
  void shouldWorkForMultiple() {
    page.setContent("<input multiple type=file>");
    FileChooser fileChooser = page.waitForFileChooser(() -> page.click("input"));
    assertTrue(fileChooser.isMultiple());
  }

  @Test
  void shouldWorkForWebkitdirectory() {
    page.setContent("<input multiple webkitdirectory type=file>");
    FileChooser fileChooser = page.waitForFileChooser(() -> page.click("input"));
    assertTrue(fileChooser.isMultiple());
  }
}

