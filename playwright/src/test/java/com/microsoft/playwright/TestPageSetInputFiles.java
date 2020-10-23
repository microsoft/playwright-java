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

import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;


public class TestPageSetInputFiles extends TestBase {
  static File FILE_TO_UPLOAD = new File("src/test/resources/file-to-upload.txt");

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
  void shouldWork() {
    page.setContent("<input type=file>");
    page.setInputFiles("input", FILE_TO_UPLOAD);
    assertEquals(1, page.evalOnSelector("input", "input => input.files.length"));
    assertEquals("file-to-upload.txt", page.evalOnSelector("input", "input => input.files[0].name"));
  }

  @Test
  void shouldSetFromMemory() {
    page.setContent("<input type=file>");
    page.setInputFiles("input", new FileChooser.FilePayload("test.txt","text/plain","this is a test".getBytes()));
    assertEquals(1, page.evalOnSelector("input", "input => input.files.length"));
    assertEquals("test.txt", page.evalOnSelector("input", "input => input.files[0].name"));
  }

  @Test
  void shouldEmitEventOnce() {
    page.setContent("<input type=file>");
    Deferred<Event<Page.EventType>> event = page.waitForEvent(Page.EventType.FILECHOOSER);
    page.click("input");
    FileChooser chooser = (FileChooser) event.get().data();
    assertNotNull(chooser);
  }

  void shouldEmitEventOnOff() {
    // Not applicable in Java.
  }

  @Test
  void shouldEmitEventAddListenerRemoveListener() {
    page.setContent("<input type=file>");
    FileChooser[] chooser = { null };
    page.addListener(Page.EventType.FILECHOOSER, new Listener<Page.EventType>() {
      @Override
      public void handle(Event<Page.EventType> event) {
        chooser[0] = (FileChooser) event.data();
        page.removeListener(Page.EventType.FILECHOOSER, this);
      }
    });
    page.click("input");
    Instant start = Instant.now();
    while (chooser[0] == null && Duration.between(start, Instant.now()).toMillis() < 10_000) {
      page.waitForTimeout(100).get();
    }
    assertNotNull(chooser[0]);
  }

  @Test
  void shouldWorkWhenFileInputIsAttachedToDOM() {
    page.setContent("<input type=file>");
    Deferred<Event<Page.EventType>> chooser = page.waitForEvent(Page.EventType.FILECHOOSER);
    page.click("input");
    assertNotNull(chooser.get());
  }

  @Test
  void shouldWorkWhenFileInputIsNotAttachedToDOM() {
    Deferred<Event<Page.EventType>> chooser = page.waitForEvent(Page.EventType.FILECHOOSER);
    page.evaluate("() => {\n" +
      "  const el = document.createElement('input');\n" +
      "  el.type = 'file';\n" +
      "  el.click();\n" +
      "}");
    assertNotNull(chooser.get());
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
    try {
      Deferred<Event<Page.EventType>> event = page.waitForEvent(Page.EventType.FILECHOOSER, new Page.WaitForEventOptions().withTimeout(1));
      event.get();
      fail("did not throw");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("Timeout 1ms exceeded"));
    }
  }

  @Test
  void shouldRespectDefaultTimeoutWhenThereIsNoCustomTimeout() {
    page.setDefaultTimeout(1);
    try {
      Deferred<Event<Page.EventType>> event = page.waitForEvent(Page.EventType.FILECHOOSER);
      event.get();
      fail("did not throw");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("Timeout 1ms exceeded"));
    }
  }

  @Test
  void shouldPrioritizeExactTimeoutOverDefaultTimeout() {
    page.setDefaultTimeout(0);
    Deferred<Event<Page.EventType>> event = page.waitForEvent(Page.EventType.FILECHOOSER,
      new Page.WaitForEventOptions().withTimeout(1));
    try {
      event.get();
      fail("did not throw");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("Timeout 1ms exceeded"));
    }
  }

  @Test
  void shouldWorkWithNoTimeout() {
    Deferred<Event<Page.EventType>> event = page.waitForEvent(Page.EventType.FILECHOOSER,
      new Page.WaitForEventOptions().withTimeout(0));
    page.evaluate("() => setTimeout(() => {\n" +
      "  const el = document.createElement('input');\n" +
      "  el.type = 'file';\n" +
      "  el.click();\n" +
      "}, 50)");
    assertNotNull(event.get().data());
  }

  @Test
  void shouldReturnTheSameFileChooserWhenThereAreManyWatchdogsSimultaneously() {
    page.setContent("<input type=file>");
    Deferred<Event<Page.EventType>> fileChooser1 = page.waitForEvent(Page.EventType.FILECHOOSER);
    Deferred<Event<Page.EventType>> fileChooser2 = page.waitForEvent(Page.EventType.FILECHOOSER);
    page.evalOnSelector("input", "input => input.click()");
    assertEquals(fileChooser1.get().data(), fileChooser2.get().data());
  }

  @Test
  void shouldAcceptSingleFile() {
    page.setContent("<input type=file oninput='javascript:console.timeStamp()'>");
    Deferred<Event<Page.EventType>> event = page.waitForEvent(Page.EventType.FILECHOOSER);
    page.click("input");
    FileChooser fileChooser = (FileChooser) event.get().data();
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
    page.addListener(Page.EventType.FILECHOOSER, event -> {
      FileChooser fileChooser = (FileChooser) event.data();
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
    page.addListener(Page.EventType.FILECHOOSER, new Listener<Page.EventType>() {
      @Override
      public void handle(Event<Page.EventType> event) {
        FileChooser fileChooser = (FileChooser) event.data();
        fileChooser.setFiles(FILE_TO_UPLOAD);
        page.removeListener(Page.EventType.FILECHOOSER, this);
      }
    });
    Object fileLength1 = page.evalOnSelector("input", "async picker => {\n" +
      "  picker.click();\n" +
      "  await new Promise(x => picker.oninput = x);\n" +
      "  return picker.files.length;\n" +
      "}");
    assertEquals(1, fileLength1);

    page.addListener(Page.EventType.FILECHOOSER, new Listener<Page.EventType>() {
      @Override
      public void handle(Event<Page.EventType> event) {
        FileChooser fileChooser = (FileChooser) event.data();
        fileChooser.setFiles(new File[0]);
        page.removeListener(Page.EventType.FILECHOOSER, this);
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
    Deferred<Event<Page.EventType>> event = page.waitForEvent(Page.EventType.FILECHOOSER);
    page.click("input");
    FileChooser fileChooser = (FileChooser) event.get().data();
    try {
      fileChooser.setFiles(new File[]{FILE_TO_UPLOAD, new File("src/test/resources/pptr.png")});
      fail("did not throw");
    } catch (RuntimeException e) {
      assertTrue(e.getMessage().contains("Non-multiple file input can only accept single file"));
    }
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
    Deferred<Event<Page.EventType>> event = page.waitForEvent(Page.EventType.FILECHOOSER);
    page.click("input");
    FileChooser fileChooser = (FileChooser) event.get().data();
    assertFalse(fileChooser.isMultiple());
  }

  @Test
  void shouldWorkForMultiple() {
    page.setContent("<input multiple type=file>");
    Deferred<Event<Page.EventType>> event = page.waitForEvent(Page.EventType.FILECHOOSER);
    page.click("input");
    FileChooser fileChooser = (FileChooser) event.get().data();
    assertTrue(fileChooser.isMultiple());
  }

  @Test
  void shouldWorkForWebkitdirectory() {
    page.setContent("<input multiple webkitdirectory type=file>");
    Deferred<Event<Page.EventType>> event = page.waitForEvent(Page.EventType.FILECHOOSER);
    page.click("input");
    FileChooser fileChooser = (FileChooser) event.get().data();
    assertTrue(fileChooser.isMultiple());
  }
}

