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

package com.microsoft.playwright;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


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
}
