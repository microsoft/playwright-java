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

import java.util.Map;

import static com.microsoft.playwright.Page.EventType.FRAMENAVIGATED;
import static com.microsoft.playwright.Utils.mapOf;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

public class TestPageEvaluate extends TestBase {
  @Test
  void shouldWork() {
    Object result = page.evaluate("() => 7 * 3");
    assertEquals(21, result);
  }

  @Test
  void shouldTransferNaN() {
    Object result = page.evaluate("a => a", Double.NaN);
    assertTrue(Double.isNaN((Double) result));
  }

  @Test
  void shouldTransfer0() {
    Object result = page.evaluate("a => a", -0);
    assertTrue((Integer) result == -0);
  }

  @Test
  void shouldTransferInfinity() {
    Object result = page.evaluate("a => a", Double.POSITIVE_INFINITY);
    assertTrue(Double.POSITIVE_INFINITY == (Double) result);
  }

  @Test
  void shouldTransferNegativeInfinity() {
    Object result = page.evaluate("a => a", Double.NEGATIVE_INFINITY);
    assertTrue(Double.NEGATIVE_INFINITY == (Double) result);
  }

  @Test
  void shouldRoundtripUnserializableValues() {
  Map<String, ?> value = mapOf(
      "infinity", Double.POSITIVE_INFINITY,
      "nInfinity", Double.NEGATIVE_INFINITY,
      "nZero", -0,
      "nan", Double.NaN
    );
    Object result = page.evaluate("value => value", value);
    assertEquals(value, result);
  }

  @Test
  void shouldRoundtripPromiseToValue() {
    {
      Object result = page.evaluate("value => Promise.resolve(value)", null);
      assertNull(result);
    }
    {
      Object result = page.evaluate("value => Promise.resolve(value)", Double.POSITIVE_INFINITY);
      assertEquals(Double.POSITIVE_INFINITY, result);
    }
    {
      Object result = page.evaluate("value => Promise.resolve(value)", -0);
      assertEquals(-0, result);
    }
  }

  @Test
  void shouldRoundtripPromiseToUnserializableValues() {
    Map<String, ?> value = mapOf(
      "infinity", Double.POSITIVE_INFINITY,
      "nInfinity", Double.NEGATIVE_INFINITY,
      "nZero", -0,
      "nan", Double.NaN
    );
    Object result = page.evaluate("value => Promise.resolve(value)", value);
    assertEquals(value, result);
  }

  @Test
  void shouldTransferArrays() {
    Object result = page.evaluate("a => a", asList(1, 2, 3));
    assertEquals(asList(1, 2, 3), result);
  }

  @Test
  void shouldTransferArraysAsArraysNotObjects() {
    Object result = page.evaluate("a => Array.isArray(a)", asList(1, 2, 3));
    assertEquals(true, result);
  }

//  @Test
  void shouldTransferMapsAsEmptyObjects() {
    // Not applicable.
  }

  @Test
  void shouldModifyGlobalEnvironment() {
    page.evaluate("() => window['globalVar'] = 123");
    assertEquals(123, page.evaluate("globalVar"));
  }

  @Test
  void shouldEvaluateInThePageContext() {
    page.navigate(server.PREFIX + "/global-var.html");
    assertEquals(123, page.evaluate("globalVar"));
  }

  @Test
  void shouldReturnUndefinedForObjectsWithSymbols() {
    assertEquals(singletonList(null), page.evaluate("() => [Symbol('foo4')]"));
    assertEquals(emptyMap(), page.evaluate("() => {\n" +
      "  const a = {};\n" +
      "  a[Symbol('foo4')] = 42;\n" +
      "  return a;\n" +
      "}"));
  assertEquals(mapOf("foo", asList(mapOf("a", null))), page.evaluate("() => {\n" +
    "  return { foo: [{ a: Symbol('foo4') }] };\n" +
    "}"));
  }

  void shouldWorkWithFunctionShorthands() {
    // Not applicable.
  }

  @Test
  void shouldWorkWithUnicodeChars() {
    Object result = page.evaluate("a => a['中文字符']", mapOf("中文字符", 42));
    assertEquals(42, result);
  }

  @Test
  void shouldThrowWhenEvaluationTriggersReload() {
    try {
      page.evaluate("() => {\n" +
        "  location.reload();\n" +
        "  return new Promise(() => { });\n" +
        "}");
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("navigation"));
    }
  }

  @Test
  void shouldAwaitPromise() {
    Object result = page.evaluate("() => Promise.resolve(8 * 7)");
    assertEquals(56, result);
  }

  @Test
  void shouldWorkRightAfterFramenavigated() {
    Object[] frameEvaluation = {null};
    page.addListener(FRAMENAVIGATED, event -> {
      Frame frame = (Frame) event.data();
      frameEvaluation[0] = frame.evaluate("() => 6 * 7");
    });
    page.navigate(server.EMPTY_PAGE);
    assertEquals(42, frameEvaluation[0]);
  }

  @Test
  void shouldWorkRightAfterACrossOriginNavigation() {
    page.navigate(server.EMPTY_PAGE);
    Object[] frameEvaluation = {null};
    page.addListener(FRAMENAVIGATED, event -> {
      Frame frame = (Frame) event.data();
      frameEvaluation[0] = frame.evaluate("() => 6 * 7");
    });
    page.navigate(server.CROSS_PROCESS_PREFIX + "/empty.html");
    assertEquals(42, frameEvaluation[0]);
  }

  @Test
  void shouldWorkFromInsideAnExposedFunction() {
    // Setup inpage callback, which calls Page.evaluate
    page.exposeFunction("callController", args -> page.evaluate("({ a, b }) => a * b",
      mapOf("a", args[0], "b", args[1])));
    Object result = page.evaluate("async function() {\n" +
      "  return await window['callController'](9, 3);\n" +
      "}");
    assertEquals(27, result);
  }

  @Test
  void shouldRejectPromiseWithException() {
    try {
      page.evaluate("() => not_existing_object.property");
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("not_existing_object"));
    }
  }
}
