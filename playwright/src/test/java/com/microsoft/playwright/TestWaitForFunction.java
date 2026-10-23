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

import com.microsoft.playwright.fixtures.PlaywrightTest;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.microsoft.playwright.Utils.mapOf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;


public class TestWaitForFunction {

  @PlaywrightTest
  void shouldTimeout(Page page) {
    Instant startTime = Instant.now();
    int timeout = 42;
    page.waitForTimeout(timeout);
    assertTrue(Duration.between(startTime, Instant.now()).toMillis() > timeout / 2);
  }

  @PlaywrightTest
  void shouldAcceptAString(Page page) {
    page.evaluate("() => window['__FOO'] = 1");
    page.waitForFunction("window.__FOO === 1");
  }

  @PlaywrightTest
  void shouldWorkWhenResolvedRightBeforeExecutionContextDisposal(Page page) {
    page.addInitScript("window['__RELOADED'] = true");
    page.waitForFunction("() => {\n" +
      "  if (!window['__RELOADED'])\n" +
      "    window.location.reload();\n" +
      "  return true;\n" +
      "}");
  }


  @PlaywrightTest
  void shouldPollOnInterval(Page page) {
    int polling = 100;
    JSHandle timeDelta = page.waitForFunction("() => {\n" +
      "  if (!window[\"__startTime\"]) {\n" +
      "    window[\"__startTime\"] = Date.now();\n" +
      "    return false;\n" +
      "  }\n" +
      "  return Date.now() - window[\"__startTime\"];\n" +
      "}", null, new Page.WaitForFunctionOptions().setPollingInterval(polling));
    int delta = (int) timeDelta.evaluate("h => h");
    assertTrue(delta >= polling);
  }

  @PlaywrightTest
  void shouldAvoidSideEffectsAfterTimeout(Page page) {
    int[] counter = { 0 };
    page.onConsoleMessage(message -> ++counter[0]);

    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      JSHandle result = page.waitForFunction("() => {\n" +
        "  window['counter'] = (window['counter'] || 0) + 1;\n" +
        "  console.log(window['counter']);\n" +
        "}", null, new Page.WaitForFunctionOptions().setPollingInterval(1).setTimeout(1000));
    });
    assertTrue(e.getMessage().contains("Timeout 1000ms exceeded"));

    int savedCounter = counter[0];
    page.waitForTimeout(2000); // Give it some time to produce more logs.
    assertEquals(savedCounter, counter[0]);
  }

  void shouldThrowOnPollingMutation() {
    // Not applicable to Java
  }

  @PlaywrightTest
  void shouldPollOnRaf(Page page) {
    page.evaluate("() => window['__FOO'] = 'hit'");
    page.waitForFunction("() => window['__FOO'] === 'hit'", null, new Page.WaitForFunctionOptions());
  }

  @PlaywrightTest
  void shouldFailWithPredicateThrowingOnFirstCall(Page page) {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.waitForFunction("() => { throw new Error('oh my'); }");
    });
    assertTrue(e.getMessage().contains("oh my"));
  }

  @PlaywrightTest
  void shouldFailWithPredicateThrowingSometimes(Page page) {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.waitForFunction("() => {\n" +
        "  window['counter'] = (window['counter'] || 0) + 1;\n" +
        "  if (window['counter'] === 3)\n" +
        "    throw new Error('Bad counter!');\n" +
        "  return window['counter'] === 5 ? 'result' : false;\n" +
        "}");
    });
    assertTrue(e.getMessage().contains("Bad counter!"));
  }

  @PlaywrightTest
  void shouldFailWithReferenceErrorOnWrongPage(Page page) {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.waitForFunction("() => globalVar === 123");
    });
    assertTrue(e.getMessage().contains("globalVar"));
  }

  @PlaywrightTest
  void shouldWorkWithStrictCSPPolicy(Page page, Server server) {
    server.setCSP("/empty.html", "script-src " + server.PREFIX);
    page.navigate(server.EMPTY_PAGE);

    page.evaluate("() => window['__FOO'] = 'hit'");
    page.waitForFunction("() => window['__FOO'] === 'hit'");
  }

  void shouldThrowOnBadPollingValue() {
    // Not applicable in Java
  }

  @PlaywrightTest
  void shouldThrowNegativePollingInterval(Page page) {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.waitForFunction("() => !!document.body", null, new Page.WaitForFunctionOptions().setPollingInterval(-10));
    });
    assertTrue(e.getMessage().contains("Cannot poll with non-positive interval"));
  }

  @PlaywrightTest
  void shouldReturnTheSuccessValueAsAJSHandle(Page page) {
    assertEquals(5, (page.waitForFunction("5")).jsonValue());
  }

  @PlaywrightTest
  void shouldReturnTheWindowAsASuccessValue(Page page) {
    assertNotNull(page.waitForFunction("() => window"));
  }

  @PlaywrightTest
  void shouldAcceptElementHandleArguments(Page page) {
    page.setContent("<div></div>");
    ElementHandle div = page.querySelector("div");
    page.evaluate("element => element.remove()", div);
    page.waitForFunction("element => !element.parentElement", div);
  }

  @PlaywrightTest
  void shouldRespectTimeout(Page page) {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.waitForFunction("false", null, new Page.WaitForFunctionOptions().setTimeout(10));
    });
    assertTrue(e.getMessage().contains("Timeout 10ms exceeded"));
  }

  @PlaywrightTest
  void shouldRespectDefaultTimeout(Page page) {
    page.setDefaultTimeout(1);
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.waitForFunction("false");
    });
    assertTrue(e.getMessage().contains("Timeout 1ms exceeded"));
  }

  @PlaywrightTest
  void shouldDisableTimeoutWhenItsSetTo0(Page page) {
    page.waitForFunction("() => {\n" +
      "  window['__counter'] = (window['__counter'] || 0) + 1;\n" +
      "  return window['__counter'] > 10;\n" +
      "}", null, new Page.WaitForFunctionOptions().setTimeout(0).setPollingInterval(10));
  }

  @PlaywrightTest
  void shouldSurviveCrossProcessNavigation(Page page, Server server) {
    page.navigate(server.EMPTY_PAGE);
    page.reload();
    page.navigate(server.CROSS_PROCESS_PREFIX + "/grid.html");
    page.evaluate("() => window['__FOO'] = 1");
    JSHandle result = page.waitForFunction("window.__FOO === 1");
    assertNotNull(result);
  }

  @PlaywrightTest
  void shouldSurviveNavigations(Page page, Server server) {
    page.navigate(server.EMPTY_PAGE);
    page.navigate(server.PREFIX + "/consolelog.html");
    page.evaluate("() => window['__done'] = true");
    page.waitForFunction("() => window['__done']");
  }

  @PlaywrightTest
  void shouldWorkWithMultilineBody(Page page) {
    JSHandle result = page.waitForFunction("\n  () => true\n");
    assertEquals(true, result.jsonValue());
  }


  @PlaywrightTest
  void shouldWaitForPredicateWithArguments(Page page) {
    page.waitForFunction("({arg1, arg2}) => arg1 + arg2 === 3", mapOf("arg1", 1, "arg2", 2));
  }

  @PlaywrightTest
  void shouldNotBeCalledAfterFinishingSuccessfully(Page page, Server server) {
    page.navigate(server.EMPTY_PAGE);
    List<String> messages = new ArrayList<>();
    page.onConsoleMessage(msg -> {
      if (msg.text().startsWith("waitForFunction")) {
        messages.add(msg.text());
      }
    });
    page.waitForFunction("() => {\n" +
      "  console.log('waitForFunction1');\n" +
      "  return true;\n" +
      "}");
    page.reload();
    page.waitForFunction("() => {\n" +
      "  console.log('waitForFunction2');\n" +
      "  return true;\n" +
      "}");
    page.reload();
    page.waitForFunction("() => {\n" +
      "  console.log('waitForFunction3');\n" +
      "  return true;\n" +
      "}");
    assertEquals(asList("waitForFunction1", "waitForFunction2", "waitForFunction3"), messages);
  }

  @PlaywrightTest
  void shouldNotBeCalledAfterFinishingUnsuccessfully(Page page, Server server) {
    page.navigate(server.EMPTY_PAGE);
    List<String> messages = new ArrayList<>();
    page.onConsoleMessage(msg -> {
      if (msg.text().startsWith("waitForFunction")) {
        messages.add(msg.text());
      }
    });
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.waitForFunction("() => {\n" +
        "  console.log('waitForFunction1');\n" +
        "  throw new Error('waitForFunction1');\n" +
        "}");
    });
    assertTrue(e.getMessage().contains("waitForFunction1"));
    page.reload();
    e = assertThrows(PlaywrightException.class, () -> {
      page.waitForFunction("() => {\n" +
        "  console.log('waitForFunction2');\n" +
        "  throw new Error('waitForFunction2');\n" +
        "}");
    });
    assertTrue(e.getMessage().contains("waitForFunction2"));
    page.reload();
    e = assertThrows(PlaywrightException.class, () -> {
      page.waitForFunction("() => {\n" +
        "  console.log('waitForFunction3');\n" +
        "  throw new Error('waitForFunction3');\n" +
        "}");
    });
    assertTrue(e.getMessage().contains("waitForFunction3"));
    assertEquals(asList("waitForFunction1", "waitForFunction2", "waitForFunction3"), messages);
  }
}
