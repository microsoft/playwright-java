/**
 * Copyright 2018 Google Inc. All rights reserved.
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
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static com.microsoft.playwright.fixtures.Utils.async;
import static com.microsoft.playwright.fixtures.Utils.attachFrame;
import static com.microsoft.playwright.fixtures.Utils.detachFrame;
import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-wait-for-function.spec.ts. */
public class TestPageWaitForFunction {
  @PlaywrightTest
  @DisplayName("WA-001 should timeout")
  void shouldTimeout(Page page) {
    long startTime = System.currentTimeMillis();
    int timeout = 42;
    page.waitForTimeout(timeout);
    assertFalse(System.currentTimeMillis() - startTime < timeout / 2);
  }

  @PlaywrightTest
  @DisplayName("WA-002 should accept a string")
  void shouldAcceptAString(Page page) throws Exception {
    CompletableFuture<JSHandle> watchdog = async(() -> page.waitForFunction("window.__FOO === 1"));
    page.evaluate("() => window['__FOO'] = 1");
    watchdog.get();
  }

  @PlaywrightTest
  @DisplayName("WA-003 should work when resolved right before execution context disposal")
  void shouldWorkWhenResolvedRightBeforeExecutionContextDisposal(Page page) {
    page.addInitScript("window['__RELOADED'] = true");
    page.waitForFunction("() => {\n" +
      "    if (!window['__RELOADED'])\n" +
      "      window.location.reload();\n" +
      "    return true;\n" +
      "  }");
  }

  @PlaywrightTest
  @DisplayName("WA-004 should poll on interval")
  void shouldPollOnInterval(Page page, TestServer server) {
    int polling = 100;
    JSHandle timeDelta = page.waitForFunction("() => {\n" +
      "    if (!window['__startTime']) {\n" +
      "      window['__startTime'] = window.builtins.Date.now();\n" +
      "      return false;\n" +
      "    }\n" +
      "    return window.builtins.Date.now() - window['__startTime'];\n" +
      "  }", null, new Page.WaitForFunctionOptions().setPollingInterval(polling));
    assertFalse(((Number) timeDelta.jsonValue()).doubleValue() < polling);
  }

  @PlaywrightTest
  @DisplayName("WA-005 should avoid side effects after timeout")
  @Fixme("java: the message has no 'page.waitForFunction: ' prefix, it is 'Timeout 1000ms exceeded.'")
  void shouldAvoidSideEffectsAfterTimeout(Page page) {
    AtomicInteger counter = new AtomicInteger(0);
    page.onConsoleMessage(msg -> counter.incrementAndGet());

    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.waitForFunction("() => {\n" +
      "    window['counter'] = (window['counter'] || 0) + 1;\n" +
      "    console.log(window['counter']);\n" +
      "  }", null, new Page.WaitForFunctionOptions().setPollingInterval(1).setTimeout(1000)));

    int savedCounter = counter.get();
    page.waitForTimeout(2000); // Give it some time to produce more logs.

    assertTrue(error.getMessage().contains("page.waitForFunction: Timeout 1000ms exceeded"));
    assertEquals(savedCounter, counter.get());
  }

  // WA-006 should throw on polling:mutation: not ported, the polling option is a number of milliseconds in Java

  @PlaywrightTest
  @DisplayName("WA-007 should poll on raf")
  void shouldPollOnRaf(Page page) throws Exception {
    // Java: polling 'raf' is the default, there is no polling option for it.
    CompletableFuture<JSHandle> watchdog = async(() -> page.waitForFunction("() => window['__FOO'] === 'hit'"));
    page.evaluate("() => window['__FOO'] = 'hit'");
    watchdog.get();
  }

  @PlaywrightTest
  @DisplayName("WA-008 should fail with predicate throwing on first call")
  void shouldFailWithPredicateThrowingOnFirstCall(Page page) {
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.waitForFunction("() => { throw new Error('oh my'); }"));
    assertTrue(error.getMessage().contains("oh my"));
  }

  @PlaywrightTest
  @DisplayName("WA-009 should fail with predicate throwing sometimes")
  void shouldFailWithPredicateThrowingSometimes(Page page) {
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.waitForFunction("() => {\n" +
      "    window['counter'] = (window['counter'] || 0) + 1;\n" +
      "    if (window['counter'] === 3)\n" +
      "      throw new Error('Bad counter!');\n" +
      "    return window['counter'] === 5 ? 'result' : false;\n" +
      "  }"));
    assertTrue(error.getMessage().contains("Bad counter!"));
  }

  @PlaywrightTest
  @DisplayName("WA-010 should fail with ReferenceError on wrong page")
  void shouldFailWithReferenceErrorOnWrongPage(Page page) {
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.waitForFunction("() => globalVar === 123"));
    assertTrue(error.getMessage().contains("globalVar"));
  }

  @PlaywrightTest
  @DisplayName("WA-011 should work with strict CSP policy")
  void shouldWorkWithStrictCSPPolicy(Page page, TestServer server) throws Exception {
    server.setCSP("/empty.html", "script-src " + server.PREFIX);
    page.navigate(server.EMPTY_PAGE);
    AtomicReference<Throwable> error = new AtomicReference<>(null);
    // Java: polling 'raf' is the default, there is no polling option for it.
    CompletableFuture<Void> p = async(() -> {
      try {
        page.waitForFunction("() => window['__FOO'] === 'hit'");
      } catch (PlaywrightException e) {
        error.set(e);
      }
      return null;
    });
    page.waitForTimeout(1000);
    page.evaluate("() => window['__FOO'] = 'hit'");
    p.get();
    assertNull(error.get());
  }

  // WA-012 should throw on bad polling value: not ported, the polling option is a number of milliseconds in Java

  @PlaywrightTest
  @DisplayName("WA-013 should throw negative polling interval")
  void shouldThrowNegativePollingInterval(Page page) {
    PlaywrightException error = null;
    try {
      page.waitForFunction("() => !!document.body", null, new Page.WaitForFunctionOptions().setPollingInterval(-10));
    } catch (PlaywrightException e) {
      error = e;
    }
    assertNotNull(error);
    assertTrue(error.getMessage().contains("Cannot poll with non-positive interval"));
  }

  @PlaywrightTest
  @DisplayName("WA-014 should return the success value as a JSHandle")
  void shouldReturnTheSuccessValueAsAJSHandle(Page page) {
    assertEquals(5, (Object) page.waitForFunction("() => 5").jsonValue());
  }

  @PlaywrightTest
  @DisplayName("WA-015 should return the window as a success value")
  void shouldReturnTheWindowAsASuccessValue(Page page) {
    assertNotNull(page.waitForFunction("() => window"));
  }

  @PlaywrightTest
  @DisplayName("WA-016 should accept ElementHandle arguments")
  void shouldAcceptElementHandleArguments(Page page) throws Exception {
    page.setContent("<div></div>");
    ElementHandle div = page.querySelector("div");
    AtomicBoolean resolved = new AtomicBoolean(false);
    CompletableFuture<JSHandle> waitForFunction = async(() -> page.waitForFunction("element => !element.parentElement", div));
    waitForFunction.thenAccept(handle -> resolved.set(true));
    assertEquals(false, resolved.get());
    page.evaluate("element => element.remove()", div);
    waitForFunction.get();
  }

  @PlaywrightTest
  @DisplayName("WA-017 should respect timeout")
  @Fixme("java: the message has no 'page.waitForFunction: ' prefix, it is 'Timeout 10ms exceeded.'")
  void shouldRespectTimeout(Page page) {
    TimeoutError error = assertThrows(TimeoutError.class,
      () -> page.waitForFunction("false", null, new Page.WaitForFunctionOptions().setTimeout(10)));
    assertTrue(error.getMessage().contains("page.waitForFunction: Timeout 10ms exceeded"));
  }

  @PlaywrightTest
  @DisplayName("WA-018 should respect default timeout")
  @Fixme("java: the message has no 'page.waitForFunction: ' prefix, it is 'Timeout 1ms exceeded.'")
  void shouldRespectDefaultTimeout(Page page) {
    page.setDefaultTimeout(1);
    TimeoutError error = assertThrows(TimeoutError.class, () -> page.waitForFunction("false"));
    assertTrue(error.getMessage().contains("page.waitForFunction: Timeout 1ms exceeded"));
  }

  @PlaywrightTest
  @DisplayName("WA-019 should disable timeout when its set to 0")
  void shouldDisableTimeoutWhenItsSetTo0(Page page) throws Exception {
    CompletableFuture<JSHandle> watchdog = async(() -> page.waitForFunction("() => {\n" +
      "    window['__counter'] = (window['__counter'] || 0) + 1;\n" +
      "    return window['__injected'];\n" +
      "  }", null, new Page.WaitForFunctionOptions().setTimeout(0).setPollingInterval(10)));
    page.waitForFunction("() => window['__counter'] > 10");
    page.evaluate("() => window['__injected'] = true");
    watchdog.get();
  }

  @PlaywrightTest
  @DisplayName("WA-020 should survive cross-process navigation")
  void shouldSurviveCrossProcessNavigation(Page page, TestServer server) throws Exception {
    AtomicBoolean fooFound = new AtomicBoolean(false);
    CompletableFuture<Void> waitForFunction = async(() -> page.waitForFunction("window.__FOO === 1")).thenAccept(handle -> fooFound.set(true));
    page.navigate(server.EMPTY_PAGE);
    assertEquals(false, fooFound.get());
    page.reload();
    assertEquals(false, fooFound.get());
    page.navigate(server.CROSS_PROCESS_PREFIX + "/grid.html");
    assertEquals(false, fooFound.get());
    page.evaluate("() => window['__FOO'] = 1");
    waitForFunction.get();
    assertEquals(true, fooFound.get());
  }

  @PlaywrightTest
  @DisplayName("WA-021 should survive navigations")
  void shouldSurviveNavigations(Page page, TestServer server) throws Exception {
    CompletableFuture<JSHandle> watchdog = async(() -> page.waitForFunction("() => window['__done']"));
    page.navigate(server.EMPTY_PAGE);
    page.navigate(server.PREFIX + "/consolelog.html");
    page.evaluate("() => window['__done'] = true");
    watchdog.get();
  }

  @PlaywrightTest
  @DisplayName("WA-022 should work with multiline body")
  void shouldWorkWithMultilineBody(Page page) {
    JSHandle result = page.waitForFunction("\n" +
      "    (() => true)()\n" +
      "  ");
    assertEquals(true, (Object) result.jsonValue());
  }

  @PlaywrightTest
  @DisplayName("WA-023 should wait for predicate with arguments")
  void shouldWaitForPredicateWithArguments(Page page) {
    page.waitForFunction("({ arg1, arg2 }) => arg1 + arg2 === 3", mapOf("arg1", 1, "arg2", 2));
  }

  @PlaywrightTest
  @DisplayName("WA-024 should not be called after finishing successfully")
  void shouldNotBeCalledAfterFinishingSuccessfully(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);

    List<String> messages = new ArrayList<>();
    page.onConsoleMessage(msg -> {
      if (msg.text().startsWith("waitForFunction"))
        messages.add(msg.text());
    });

    page.waitForFunction("() => {\n" +
      "    console.log('waitForFunction1');\n" +
      "    return true;\n" +
      "  }");
    page.reload();
    page.waitForFunction("() => {\n" +
      "    console.log('waitForFunction2');\n" +
      "    return true;\n" +
      "  }");
    page.reload();
    page.waitForFunction("() => {\n" +
      "    console.log('waitForFunction3');\n" +
      "    return true;\n" +
      "  }");

    assertEquals("waitForFunction1|waitForFunction2|waitForFunction3", String.join("|", messages));
  }

  @PlaywrightTest
  @DisplayName("WA-025 should not be called after finishing unsuccessfully")
  void shouldNotBeCalledAfterFinishingUnsuccessfully(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);

    List<String> messages = new ArrayList<>();
    page.onConsoleMessage(msg -> {
      if (msg.text().startsWith("waitForFunction"))
        messages.add(msg.text());
    });

    try {
      page.waitForFunction("() => {\n" +
        "    console.log('waitForFunction1');\n" +
        "    throw new Error('waitForFunction1');\n" +
        "  }");
    } catch (PlaywrightException e) {
    }
    page.reload();
    try {
      page.waitForFunction("() => {\n" +
        "    console.log('waitForFunction2');\n" +
        "    throw new Error('waitForFunction2');\n" +
        "  }");
    } catch (PlaywrightException e) {
    }
    page.reload();
    try {
      page.waitForFunction("() => {\n" +
        "    console.log('waitForFunction3');\n" +
        "    throw new Error('waitForFunction3');\n" +
        "  }");
    } catch (PlaywrightException e) {
    }

    assertEquals("waitForFunction1|waitForFunction2|waitForFunction3", String.join("|", messages));
  }

  @PlaywrightTest
  @DisplayName("WA-026 should throw when frame is detached")
  @Fixme("java: the message has no 'frame.waitForFunction: ' prefix")
  void shouldThrowWhenFrameIsDetached(Page page, TestServer server) throws Exception {
    attachFrame(page, "frame1", server.EMPTY_PAGE);
    Frame frame = page.frames().get(1);
    CompletableFuture<Throwable> promise = async(() -> {
      try {
        frame.waitForFunction("() => false");
        return null;
      } catch (PlaywrightException e) {
        return e;
      }
    });
    detachFrame(page, "frame1");
    Throwable error = promise.get();
    assertNotNull(error);
    assertTrue(Pattern.compile("frame.waitForFunction: (Frame was detached|Execution context was destroyed)").matcher(error.getMessage()).find());
  }
}
