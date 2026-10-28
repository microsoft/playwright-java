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

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.ConsoleMessage;
import com.microsoft.playwright.FileChooser;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Route;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestInfo;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static com.microsoft.playwright.fixtures.Utils.asset;
import static com.microsoft.playwright.fixtures.Utils.attachFrame;
import static com.microsoft.playwright.fixtures.Utils.browserMajorVersion;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/page/page-filechooser.spec.ts. */
public class TestPageFilechooser {
  @PlaywrightTest
  @DisplayName("FC-001 should upload multiple large files")
  void shouldUploadMultipleLargeFiles(Page page, TestServer server, TestInfo testInfo) throws IOException {
    int filesCount = 10;
    page.navigate(server.PREFIX + "/input/fileupload-multi.html");
    Path uploadFile = testInfo.outputPath("50MB_1.zip");
    byte[] str = repeat('A', 1024);
    try (OutputStream stream = Files.newOutputStream(uploadFile)) {
      // 49 is close to the actual limit
      for (int i = 0; i < 49 * 1024; i++) {
        stream.write(str);
      }
    }
    Locator input = page.locator("input[type=\"file\"]");
    List<Path> uploadFiles = new ArrayList<>();
    uploadFiles.add(uploadFile);
    for (int i = 2; i <= filesCount; i++) {
      Path dstFile = testInfo.outputPath("50MB_" + i + ".zip");
      Files.copy(uploadFile, dstFile);
      uploadFiles.add(dstFile);
    }
    FileChooser fileChooser = page.waitForFileChooser(() -> input.click());
    fileChooser.setFiles(uploadFiles.toArray(new Path[0]));
    Object filesLen = page.evaluate("document.getElementsByTagName(\"input\")[0].files.length");
    assertEquals(true, fileChooser.isMultiple());
    assertEquals(filesCount, filesLen);
    for (Path path : uploadFiles) {
      Files.delete(path);
    }
  }

  private static byte[] repeat(char c, int count) {
    byte[] bytes = new byte[count];
    Arrays.fill(bytes, (byte) c);
    return bytes;
  }

  @PlaywrightTest
  @DisplayName("FC-002 should emit event once")
  void shouldEmitEventOnce(Page page, TestServer server) {
    page.setContent("<input type=file>");
    // Java: new Promise(f => page.once('filechooser', f)) with the click is a wait for the event around the click.
    FileChooser chooser = page.waitForFileChooser(() -> page.click("input"));
    assertNotNull(chooser);
  }

  // FC-003 should emit event via prepend: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("FC-004 should emit event for iframe")
  void shouldEmitEventForIframe(Page page, TestServer server) {
    Frame frame = attachFrame(page, "frame1", server.EMPTY_PAGE);
    frame.setContent("<input type=file>");
    // Java: new Promise(f => page.once('filechooser', f)) with the click is a wait for the event around the click.
    FileChooser chooser = page.waitForFileChooser(() -> frame.click("input"));
    assertNotNull(chooser);
  }

  @PlaywrightTest
  @DisplayName("FC-005 should emit event on/off")
  void shouldEmitEventOnOff(Page page, TestServer server) throws Exception {
    page.setContent("<input type=file>");
    CompletableFuture<FileChooser> promise = new CompletableFuture<>();
    Consumer<FileChooser> listener = new Consumer<FileChooser>() {
      @Override
      public void accept(FileChooser chooser) {
        page.offFileChooser(this);
        promise.complete(chooser);
      }
    };
    page.onFileChooser(listener);
    // Java: events are dispatched while a call is running, so the click is wrapped in a wait for the event; the listener above is the one under test.
    page.waitForFileChooser(() -> page.click("input"));
    FileChooser chooser = promise.get();
    assertNotNull(chooser);
  }

  // FC-006 should emit event addListener/removeListener: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("FC-007 should work when file input is attached to DOM")
  void shouldWorkWhenFileInputIsAttachedToDOM(Page page, TestServer server) {
    page.setContent("<input type=file>");
    FileChooser chooser = page.waitForFileChooser(() -> page.click("input"));
    assertNotNull(chooser);
  }

  @PlaywrightTest
  @DisplayName("FC-008 should work when file input is not attached to DOM")
  void shouldWorkWhenFileInputIsNotAttachedToDOM(Page page) throws Exception {
    // Java: the evaluate below blocks until the file is set, so the file chooser handler sets the file; it stands for the code that upstream runs after the pending evaluate.
    page.onFileChooser(chooser -> chooser.setFiles(asset("file-to-upload.txt")));
    Object content = page.evaluate("async () => {\n" +
      "      const el = document.createElement('input');\n" +
      "      el.type = 'file';\n" +
      "      el.click();\n" +
      "      await new Promise(x => el.oninput = x);\n" +
      "      const reader = new FileReader();\n" +
      "      const promise = new Promise(fulfill => reader.onload = fulfill);\n" +
      "      reader.readAsText(el.files[0]);\n" +
      "      return promise.then(() => reader.result);\n" +
      "    }");
    assertEquals("contents of the file", content);
  }

  @PlaywrightTest
  @DisplayName("FC-009 should not throw when filechooser belongs to iframe")
  void shouldNotThrowWhenFilechooserBelongsToIframe(Page page, TestServer server, BrowserName browserName) {
    page.navigate(server.PREFIX + "/frames/one-frame.html");
    Frame frame = page.mainFrame().childFrames().get(0);
    frame.setContent("\n" +
      "    <div>Click me</div>\n" +
      "    <script>\n" +
      "      document.querySelector('div').addEventListener('click', () => {\n" +
      "        const input = document.createElement('input');\n" +
      "        input.type = 'file';\n" +
      "        input.click();\n" +
      "        window.parent.__done = true;\n" +
      "      });\n" +
      "    </script>\n" +
      "  ");
    page.waitForFileChooser(() -> frame.click("div"));
    page.waitForFunction("() => window.__done");
  }

  @PlaywrightTest
  @DisplayName("FC-010 should not throw when frame is detached immediately")
  void shouldNotThrowWhenFrameIsDetachedImmediately(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/frames/one-frame.html");
    Frame frame = page.mainFrame().childFrames().get(0);
    frame.setContent("\n" +
      "    <div>Click me</div>\n" +
      "    <script>\n" +
      "      document.querySelector('div').addEventListener('click', () => {\n" +
      "        const input = document.createElement('input');\n" +
      "        input.type = 'file';\n" +
      "        input.click();\n" +
      "        window.parent.__done = true;\n" +
      "        const iframe = window.parent.document.querySelector('iframe');\n" +
      "        iframe.remove();\n" +
      "      });\n" +
      "    </script>\n" +
      "  ");
    page.onFileChooser(chooser -> {}); // To ensure we handle file choosers.
    frame.click("div");
    page.waitForFunction("() => window.__done");
  }

  @PlaywrightTest
  @DisplayName("FC-011 should respect timeout")
  void shouldRespectTimeout(Page page) {
    assertThrows(TimeoutError.class, () -> page.waitForFileChooser(new Page.WaitForFileChooserOptions().setTimeout(1), () -> {}));
  }

  @PlaywrightTest
  @DisplayName("FC-012 should respect default timeout when there is no custom timeout")
  void shouldRespectDefaultTimeoutWhenThereIsNoCustomTimeout(Page page) {
    page.setDefaultTimeout(1);
    assertThrows(TimeoutError.class, () -> page.waitForFileChooser(() -> {}));
  }

  @PlaywrightTest
  @DisplayName("FC-013 should prioritize exact timeout over default timeout")
  void shouldPrioritizeExactTimeoutOverDefaultTimeout(Page page) {
    page.setDefaultTimeout(0);
    assertThrows(TimeoutError.class, () -> page.waitForFileChooser(new Page.WaitForFileChooserOptions().setTimeout(1), () -> {}));
  }

  @PlaywrightTest
  @DisplayName("FC-014 should work with no timeout")
  void shouldWorkWithNoTimeout(Page page, TestServer server) {
    FileChooser chooser = page.waitForFileChooser(new Page.WaitForFileChooserOptions().setTimeout(0), () -> {
      page.evaluate("() => window.builtins.setTimeout(() => {\n" +
        "      const el = document.createElement('input');\n" +
        "      el.type = 'file';\n" +
        "      el.click();\n" +
        "    }, 50)");
    });
    assertNotNull(chooser);
  }

  // FC-015 should abort with signal: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("FC-016 should return the same file chooser when there are many watchdogs simultaneously")
  void shouldReturnTheSameFileChooserWhenThereAreManyWatchdogsSimultaneously(Page page, TestServer server) {
    page.setContent("<input type=file>");
    FileChooser[] fileChooser2 = new FileChooser[1];
    // Promise.all
    FileChooser fileChooser1 = page.waitForFileChooser(() -> {
      fileChooser2[0] = page.waitForFileChooser(() -> {
        page.evalOnSelector("input", "input => input.click()");
      });
    });
    assertEquals(true, fileChooser1 == fileChooser2[0]);
  }

  @PlaywrightTest
  @DisplayName("FC-017 should accept single file")
  void shouldAcceptSingleFile(Page page) {
    page.setContent("<input type=file oninput='javascript:console.timeStamp()'>");
    FileChooser fileChooser = page.waitForFileChooser(() -> page.click("input"));
    assertEquals(page, fileChooser.page());
    assertNotNull(fileChooser.element());
    fileChooser.setFiles(asset("file-to-upload.txt"));
    assertEquals(1, page.evalOnSelector("input", "input => input.files.length"));
    assertEquals("file-to-upload.txt", page.evalOnSelector("input", "input => input.files[0].name"));
  }

  // @see https://github.com/microsoft/playwright/issues/4704
  // FC-018 should not trim big uploaded files: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("FC-019 should be able to read selected file")
  void shouldBeAbleToReadSelectedFile(Page page) throws Exception {
    page.setContent("<input type=file>");
    // Java: the evalOnSelector below blocks until the file is set, so the file chooser handler sets the file; it stands for the code that upstream runs after the pending evalOnSelector.
    page.onFileChooser(fileChooser -> fileChooser.setFiles(asset("file-to-upload.txt")));
    Object content = page.evalOnSelector("input", "async picker => {\n" +
      "      picker.click();\n" +
      "      await new Promise(x => picker.oninput = x);\n" +
      "      const reader = new FileReader();\n" +
      "      const promise = new Promise(fulfill => reader.onload = fulfill);\n" +
      "      reader.readAsText(picker.files[0]);\n" +
      "      return promise.then(() => reader.result);\n" +
      "    }");
    assertEquals("contents of the file", content);
  }

  @PlaywrightTest
  @DisplayName("FC-020 should be able to reset selected files with empty file list")
  void shouldBeAbleToResetSelectedFilesWithEmptyFileList(Page page) throws Exception {
    page.setContent("<input type=file>");
    // Java: the evalOnSelector below blocks until the file is set, so a file chooser handler sets the file; it stands for the code that upstream runs after the pending evalOnSelector.
    Consumer<FileChooser> setFile = fileChooser -> fileChooser.setFiles(asset("file-to-upload.txt"));
    page.onFileChooser(setFile);
    Object fileLength1 = page.evalOnSelector("input", "async picker => {\n" +
      "      picker.click();\n" +
      "      await new Promise(x => picker.oninput = x);\n" +
      "      return picker.files.length;\n" +
      "    }");
    assertEquals(1, fileLength1);
    page.offFileChooser(setFile);
    page.onFileChooser(fileChooser -> fileChooser.setFiles(new Path[0]));
    Object fileLength2 = page.evalOnSelector("input", "async picker => {\n" +
      "      picker.click();\n" +
      "      await new Promise(x => picker.oninput = x);\n" +
      "      return picker.files.length;\n" +
      "    }");
    assertEquals(0, fileLength2);
  }

  @PlaywrightTest
  @DisplayName("FC-021 should work for single file pick")
  void shouldWorkForSingleFilePick(Page page, TestServer server) {
    page.setContent("<input type=file>");
    FileChooser fileChooser = page.waitForFileChooser(() -> page.click("input"));
    assertEquals(false, fileChooser.isMultiple());
  }

  @PlaywrightTest
  @DisplayName("FC-022 should work for \"multiple\"")
  void shouldWorkForMultiple(Page page, TestServer server) {
    page.setContent("<input multiple type=file>");
    FileChooser fileChooser = page.waitForFileChooser(() -> page.click("input"));
    assertEquals(true, fileChooser.isMultiple());
  }

  @PlaywrightTest
  @DisplayName("FC-023 should work for \"webkitdirectory\"")
  void shouldWorkForWebkitdirectory(Page page, TestServer server) {
    page.setContent("<input multiple webkitdirectory type=file>");
    FileChooser fileChooser = page.waitForFileChooser(() -> page.click("input"));
    assertEquals(true, fileChooser.isMultiple());
  }

  @PlaywrightTest
  @DisplayName("FC-024 should emit event after navigation")
  void shouldEmitEventAfterNavigation(Page page, TestServer server, BrowserName browserName, Browser browser) {
    assumeFalse(browserName == BrowserName.CHROMIUM && browserMajorVersion(browser) < 99);

    List<String> logs = new ArrayList<>();
    page.onFileChooser(chooser -> logs.add("filechooser"));
    page.navigate(server.PREFIX + "/empty.html");
    page.setContent("<input type=file>");
    page.waitForFileChooser(() -> page.click("input"));
    page.navigate(server.CROSS_PROCESS_PREFIX + "/empty.html");
    page.setContent("<input type=file>");
    page.waitForFileChooser(() -> page.click("input"));
    assertEquals(Arrays.asList("filechooser", "filechooser"), logs);
  }

  @PlaywrightTest
  @DisplayName("FC-025 should trigger listener added before navigation")
  void shouldTriggerListenerAddedBeforeNavigation(Page page, TestServer server, Browser browser) throws Exception {
    // Add listener before cross process navigation.
    CompletableFuture<FileChooser> chooserPromise = new CompletableFuture<>();
    // Java: page.once('filechooser', f) is a handler that removes itself first.
    Consumer<FileChooser> listener = new Consumer<FileChooser>() {
      @Override
      public void accept(FileChooser chooser) {
        page.offFileChooser(this);
        chooserPromise.complete(chooser);
      }
    };
    page.onFileChooser(listener);
    page.navigate(server.PREFIX + "/empty.html");
    page.navigate(server.CROSS_PROCESS_PREFIX + "/empty.html");
    page.setContent("<input type=file>");
    // Java: events are dispatched while a call is running, so the click is wrapped in a wait for the event; the listener above is the one under test.
    page.waitForFileChooser(() -> page.click("input"));
    FileChooser chooser = chooserPromise.get();
    assertNotNull(chooser);
  }

  @Nested
  class UserActivation {
    // Each test navigates to a fresh document, because transient activation
    // (including the one granted by page.evaluate in all browsers) sticks to
    // the document for several seconds.
    private final String html = "\n" +
      "    <button id=\"once\">once</button>\n" +
      "    <button id=\"twice\">twice</button>\n" +
      "    <input type=file>\n" +
      "    <script>\n" +
      "      const input = document.querySelector('input');\n" +
      "      document.querySelector('#once').addEventListener('click', () => input.click());\n" +
      "      document.querySelector('#twice').addEventListener('click', () => { input.click(); input.click(); });\n" +
      "      window.showPicker = () => {\n" +
      "        try {\n" +
      "          input.showPicker();\n" +
      "          return 'ok';\n" +
      "        } catch (e) {\n" +
      "          return e.name;\n" +
      "        }\n" +
      "      };\n" +
      "      if (location.search === '?click')\n" +
      "        setTimeout(() => input.click(), 0);\n" +
      "      if (location.search === '?showPicker')\n" +
      "        setTimeout(() => console.log('showPicker:' + window.showPicker()), 0);\n" +
      "    </script>\n" +
      "  ";

    @BeforeEach
    void routeActivationPage(Page page, TestServer server) {
      page.route(server.PREFIX + "/activation.html*", route -> route.fulfill(new Route.FulfillOptions().setContentType("text/html").setBody(html)));
    }

    @PlaywrightTest
    @DisplayName("FC-026 should not open file chooser without user activation")
    @Fixme(value = "driver: the driver (1.64.1) predates the user activation check for file choosers: Firefox emits a filechooser event without user activation (choosers expected: <0> but was: <1>)", browsers = BrowserName.FIREFOX)
    @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "fixme: WebKit opens the file chooser from input.click() without user activation")
    void shouldNotOpenFileChooserWithoutUserActivation(Page page, TestServer server, BrowserName browserName) {
      AtomicInteger choosers = new AtomicInteger();
      page.onFileChooser(chooser -> choosers.incrementAndGet());

      page.navigate(server.PREFIX + "/activation.html?click");
      page.waitForTimeout(500);
      assertEquals(0, choosers.get());

      // Do not poll with waitForFunction: each poll evaluates in the page with a user gesture,
      // which grants transient user activation and would let showPicker() succeed.
      // Java: the navigation is the callback of the wait, so that the listener is registered before it.
      ConsoleMessage result = page.waitForConsoleMessage(
        new Page.WaitForConsoleMessageOptions().setPredicate(msg -> msg.text().startsWith("showPicker:")),
        () -> page.navigate(server.PREFIX + "/activation.html?showPicker"));
      assertEquals("showPicker:NotAllowedError", result.text());
      assertEquals(0, choosers.get());
    }

    @PlaywrightTest
    @DisplayName("FC-027 should open file chooser with user activation")
    void shouldOpenFileChooserWithUserActivation(Page page, TestServer server) {
      page.navigate(server.PREFIX + "/activation.html");
      FileChooser chooser = page.waitForFileChooser(() -> page.click("#once"));
      assertNotNull(chooser);

      // page.evaluate grants transient user activation in all browsers.
      page.navigate(server.PREFIX + "/activation.html");
      FileChooser chooser2 = page.waitForFileChooser(() -> page.evaluate("() => document.querySelector('input').click()"));
      assertNotNull(chooser2);
      page.navigate(server.PREFIX + "/activation.html");
      assertEquals("ok", page.evaluate("() => window.showPicker()"));
    }

    @PlaywrightTest
    @DisplayName("FC-028 should consume user activation when opening file chooser")
    @Fixme(value = "driver: the driver (1.64.1) predates the user activation check for file choosers: Firefox opens the chooser twice for two input.click() calls (choosers expected: <1> but was: <2>)", browsers = BrowserName.FIREFOX)
    @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "fixme: WebKit does not consume user activation when opening the file chooser")
    void shouldConsumeUserActivationWhenOpeningFileChooser(Page page, TestServer server, BrowserName browserName) {
      AtomicInteger choosers = new AtomicInteger();
      page.onFileChooser(chooser -> choosers.incrementAndGet());
      page.navigate(server.PREFIX + "/activation.html");
      page.click("#twice");
      page.waitForTimeout(500);
      assertEquals(1, choosers.get());
    }
  }
}
