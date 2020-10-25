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

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.microsoft.playwright.Page.EventType.CONSOLE;
import static com.microsoft.playwright.Utils.mapOf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;


public class TestWaitForFunction extends TestBase {

  @Test
  void shouldTimeout() {
    Instant startTime = Instant.now();
    int timeout = 42;
    page.waitForTimeout(timeout).get();
    assertTrue(Duration.between(startTime, Instant.now()).toMillis() > timeout / 2);
  }

  @Test
  void shouldAcceptAString() {
    Deferred<JSHandle> watchdog = page.waitForFunction("window.__FOO === 1");
    page.evaluate("() => window['__FOO'] = 1");
    watchdog.get();
  }

  @Test
  void shouldWorkWhenResolvedRightBeforeExecutionContextDisposal() {
    page.addInitScript("window['__RELOADED'] = true");
    page.waitForFunction("() => {\n" +
      "  if (!window['__RELOADED'])\n" +
      "    window.location.reload();\n" +
      "  return true;\n" +
      "}").get();
  }


  @Test
  void shouldPollOnInterval() {
    int polling = 100;
    Deferred<JSHandle> timeDelta = page.waitForFunction("() => {\n" +
      "  if (!window[\"__startTime\"]) {\n" +
      "    window[\"__startTime\"] = Date.now();\n" +
      "    return false;\n" +
      "  }\n" +
      "  return Date.now() - window[\"__startTime\"];\n" +
      "}", null, new Page.WaitForFunctionOptions().withPollingInterval(polling));
    int delta = (int) timeDelta.get().evaluate("h => h");
    assertTrue(delta >= polling);
  }

  @Test
  void shouldAvoidSideEffectsAfterTimeout() {
    int[] counter = { 0 };
    page.addListener(CONSOLE, event -> ++counter[0]);

    Deferred<JSHandle> result = page.waitForFunction("() => {\n" +
      "  window['counter'] = (window['counter'] || 0) + 1;\n" +
      "  console.log(window['counter']);\n" +
      "}", null, new Page.WaitForFunctionOptions().withPollingInterval(1).withTimeout(1000));
    try {
      result.get();
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("Timeout 1000ms exceeded"));
    }

    int savedCounter = counter[0];
    page.waitForTimeout(2000); // Give it some time to produce more logs.
    assertEquals(savedCounter, counter[0]);
  }

  void shouldThrowOnPollingMutation() {
    // Not applicable to Java
  }

  @Test
  void shouldPollOnRaf() {
    Deferred<JSHandle> watchdog = page.waitForFunction("() => window['__FOO'] === 'hit'", null, new Page.WaitForFunctionOptions().withRequestAnimationFrame());
    page.evaluate("() => window['__FOO'] = 'hit'");
    watchdog.get();
  }

  @Test
  void shouldFailWithPredicateThrowingOnFirstCall() {
    try {
      page.waitForFunction("() => { throw new Error('oh my'); }").get();
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("oh my"));
    }
  }

  @Test
  void shouldFailWithPredicateThrowingSometimes() {
    try {
      page.waitForFunction("() => {\n" +
        "  window['counter'] = (window['counter'] || 0) + 1;\n" +
        "  if (window['counter'] === 3)\n" +
        "    throw new Error('Bad counter!');\n" +
        "  return window['counter'] === 5 ? 'result' : false;\n" +
        "}").get();
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("Bad counter!"));
    }
  }

  @Test
  void shouldFailWithReferenceErrorOnWrongPage() {
    try {
      page.waitForFunction("() => globalVar === 123").get();
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("globalVar"));
    }
  }

  @Test
  void shouldWorkWithStrictCSPPolicy() {
    server.setCSP("/empty.html", "script-src " + server.PREFIX);
    page.navigate(server.EMPTY_PAGE);

    Deferred<JSHandle> result = page.waitForFunction("() => window['__FOO'] === 'hit'");
    page.evaluate("() => window['__FOO'] = 'hit'");
    result.get();
  }

  void shouldThrowOnBadPollingValue() {
    // Not applicable in Java
  }

  @Test
  void shouldThrowNegativePollingInterval() {
    try {
      page.waitForFunction("() => !!document.body", null, new Page.WaitForFunctionOptions().withPollingInterval(-10)).get();
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("Cannot poll with non-positive interval"));
    }
  }

  @Test
  void shouldReturnTheSuccessValueAsAJSHandle() {
    assertEquals(5, (page.waitForFunction("5")).get().jsonValue());
  }

  @Test
  void shouldReturnTheWindowAsASuccessValue() {
    assertNotNull(page.waitForFunction("() => window").get());
  }

  @Test
  void shouldAcceptElementHandleArguments() {
    page.setContent("<div></div>");
    ElementHandle div = page.querySelector("div");
    Deferred<JSHandle> waitForFunction = page.waitForFunction("element => !element.parentElement", div);
    page.evaluate("element => element.remove()", div);
    waitForFunction.get();
  }

  @Test
  void shouldRespectTimeout() {
    try {
      page.waitForFunction("false", null, new Page.WaitForFunctionOptions().withTimeout(10)).get();
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("Timeout 10ms exceeded"));
    }
  }

  @Test
  void shouldRespectDefaultTimeout() {
    page.setDefaultTimeout(1);
    try {
      page.waitForFunction("false").get();
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("Timeout 1ms exceeded"));
    }
  }

  @Test
  void shouldDisableTimeoutWhenItsSetTo0() {
    Deferred<JSHandle> watchdog = page.waitForFunction("() => {\n" +
      "  window['__counter'] = (window['__counter'] || 0) + 1;\n" +
      "  return window['__injected'];\n" +
      "}", null, new Page.WaitForFunctionOptions().withTimeout(0).withPollingInterval(10));
    page.waitForFunction("() => window['__counter'] > 10");
    page.evaluate("() => window['__injected'] = true");
    watchdog.get();
  }

  @Test
  void shouldSurviveCrossProcessNavigation() {
    Deferred<JSHandle> waitForFunction = page.waitForFunction("window.__FOO === 1");
    page.navigate(server.EMPTY_PAGE);
    page.reload();
    page.navigate(server.CROSS_PROCESS_PREFIX + "/grid.html");
    page.evaluate("() => window['__FOO'] = 1");
    assertNotNull(waitForFunction.get());
  }

  @Test
  void shouldSurviveNavigations() {
    Deferred<JSHandle> watchdog = page.waitForFunction("() => window['__done']");
    page.navigate(server.EMPTY_PAGE);
    page.navigate(server.PREFIX + "/consolelog.html");
    page.evaluate("() => window['__done'] = true");
    watchdog.get();
  }

  @Test
  void shouldWorkWithMultilineBody() {
    Deferred<JSHandle> result = page.waitForFunction("\n  () => true\n");
    assertEquals(true, result.get().jsonValue());
  }


  @Test
  void shouldWaitForPredicateWithArguments() {
    page.waitForFunction("({arg1, arg2}) => arg1 + arg2 === 3", mapOf("arg1", 1, "arg2", 2));
  }

    @Test
    void shouldNotBeCalledAfterFinishingSuccessfully() {
      page.navigate(server.EMPTY_PAGE);
      List<String> messages = new ArrayList<>();
      page.addListener(CONSOLE, event -> {
        ConsoleMessage msg = (ConsoleMessage) event.data();
        if (msg.text().startsWith("waitForFunction")) {
          messages.add(msg.text());
        }
      });
      {
        Deferred<JSHandle> result = page.waitForFunction("() => {\n" +
          "  console.log('waitForFunction1');\n" +
          "  return true;\n" +
          "}");
        result.get();
      }
      page.reload();
      {
        Deferred<JSHandle> result = page.waitForFunction("() => {\n" +
          "  console.log('waitForFunction2');\n" +
          "  return true;\n" +
          "}");
        result.get();
      }
      page.reload();
      {
        Deferred<JSHandle> result = page.waitForFunction("() => {\n" +
          "  console.log('waitForFunction3');\n" +
          "  return true;\n" +
          "}");
        result.get();
      }
      assertEquals(asList("waitForFunction1", "waitForFunction2", "waitForFunction3"), messages);
    }

  @Test
  void shouldNotBeCalledAfterFinishingUnsuccessfully() {
    page.navigate(server.EMPTY_PAGE);
    List<String> messages = new ArrayList<>();
    page.addListener(CONSOLE, event -> {
      ConsoleMessage msg = (ConsoleMessage) event.data();
      if (msg.text().startsWith("waitForFunction"))
        messages.add(msg.text());
    });
    try {
      Deferred<JSHandle> result = page.waitForFunction("() => {\n" +
        "  console.log('waitForFunction1');\n" +
        "  throw new Error('waitForFunction1');\n" +
        "}");
      result.get();
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("waitForFunction1"));
    }
    page.reload();
    try {
      page.waitForFunction("() => {\n" +
        "  console.log('waitForFunction2');\n" +
        "  throw new Error('waitForFunction2');\n" +
        "}").get();
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("waitForFunction2"));
    }
    page.reload();
    try {
      page.waitForFunction("() => {\n" +
        "  console.log('waitForFunction3');\n" +
        "  throw new Error('waitForFunction3');\n" +
        "}").get();
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("waitForFunction3"));
    }
    assertEquals(asList("waitForFunction1", "waitForFunction2", "waitForFunction3"), messages);
  }
}
