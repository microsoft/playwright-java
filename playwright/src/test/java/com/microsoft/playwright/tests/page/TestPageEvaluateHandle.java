/*
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

import com.google.gson.JsonParser;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.DisplayName;

import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-evaluate-handle.spec.ts. */
public class TestPageEvaluateHandle {
  @PlaywrightTest
  @DisplayName("EV-101 should work")
  void shouldWork(Page page) {
    JSHandle windowHandle = page.evaluateHandle("() => window");
    assertNotNull(windowHandle);
  }

  @PlaywrightTest
  @DisplayName("EV-102 should accept object handle as an argument")
  void shouldAcceptObjectHandleAsAnArgument(Page page) {
    JSHandle navigatorHandle = page.evaluateHandle("() => navigator");
    String text = (String) page.evaluate("e => e.userAgent", navigatorHandle);
    assertTrue(text.contains("Mozilla"));
  }

  @PlaywrightTest
  @DisplayName("EV-103 should accept object handle to primitive types")
  void shouldAcceptObjectHandleToPrimitiveTypes(Page page) {
    JSHandle aHandle = page.evaluateHandle("() => 5");
    Object isFive = page.evaluate("e => Object.is(e, 5)", aHandle);
    assertTrue((Boolean) isFive);
  }

  @PlaywrightTest
  @DisplayName("EV-104 should accept nested handle")
  void shouldAcceptNestedHandle(Page page) {
    JSHandle foo = page.evaluateHandle("() => ({ x: 1, y: 'foo' })");
    Object result = page.evaluate("({ foo }) => {\n" +
      "    return foo;\n" +
      "  }", mapOf("foo", foo));
    assertEquals(mapOf("x", 1, "y", "foo"), result);
  }

  @PlaywrightTest
  @DisplayName("EV-105 should accept nested window handle")
  void shouldAcceptNestedWindowHandle(Page page) {
    JSHandle foo = page.evaluateHandle("() => window");
    Object result = page.evaluate("({ foo }) => {\n" +
      "    return foo === window;\n" +
      "  }", mapOf("foo", foo));
    assertEquals(true, result);
  }

  @PlaywrightTest
  @DisplayName("EV-106 should accept multiple nested handles")
  void shouldAcceptMultipleNestedHandles(Page page) {
    JSHandle foo = page.evaluateHandle("() => ({ x: 1, y: 'foo' })");
    JSHandle bar = page.evaluateHandle("() => 5");
    JSHandle baz = page.evaluateHandle("() => (['baz'])");
    String result = (String) page.evaluate("x => {\n" +
      "    return JSON.stringify(x);\n" +
      "  }", mapOf("a1", mapOf("foo", foo), "a2", mapOf("bar", bar, "arr", asList(mapOf("baz", baz)))));
    // Java: JSON.parse() and toEqual() are the parsing and the equality of Gson trees.
    assertEquals(JsonParser.parseString("{\n" +
      "    a1: { foo: { x: 1, y: 'foo' } },\n" +
      "    a2: { bar: 5, arr: [{ baz: ['baz'] }] }\n" +
      "  }"), JsonParser.parseString(result));
  }

  @PlaywrightTest
  @DisplayName("EV-107 should accept same handle multiple times")
  void shouldAcceptSameHandleMultipleTimes(Page page) {
    JSHandle foo = page.evaluateHandle("() => 1");
    assertEquals(mapOf("foo", 1, "bar", asList(1), "baz", mapOf("foo", 1)),
      page.evaluate("x => x", mapOf("foo", foo, "bar", asList(foo), "baz", mapOf("foo", foo))));
  }

  @PlaywrightTest
  @DisplayName("EV-108 should accept same nested object multiple times")
  void shouldAcceptSameNestedObjectMultipleTimes(Page page) {
    java.util.Map<String, Object> foo = mapOf("x", 1);
    assertEquals(mapOf("foo", mapOf("x", 1), "bar", asList(mapOf("x", 1)), "baz", mapOf("foo", mapOf("x", 1))),
      page.evaluate("x => x", mapOf("foo", foo, "bar", asList(foo), "baz", mapOf("foo", foo))));
  }

  @PlaywrightTest
  @DisplayName("EV-109 should accept object handle to unserializable value")
  void shouldAcceptObjectHandleToUnserializableValue(Page page) {
    JSHandle aHandle = page.evaluateHandle("() => Infinity");
    assertEquals(true, page.evaluate("e => Object.is(e, Infinity)", aHandle));
  }

  @PlaywrightTest
  @DisplayName("EV-110 should pass configurable args")
  void shouldPassConfigurableArgs(Page page) {
    Object result = page.evaluate("arg => {\n" +
      "    if (arg.foo !== 42)\n" +
      "      throw new Error('Not a 42');\n" +
      "    arg.foo = 17;\n" +
      "    if (arg.foo !== 17)\n" +
      "      throw new Error('Not 17');\n" +
      "    delete arg.foo;\n" +
      "    if (arg.foo === 17)\n" +
      "      throw new Error('Still 17');\n" +
      "    return arg;\n" +
      "  }", mapOf("foo", 42));
    assertEquals(mapOf(), result);
  }

  @PlaywrightTest
  @DisplayName("EV-111 should work with primitives")
  void shouldWorkWithPrimitives(Page page) {
    JSHandle aHandle = page.evaluateHandle("() => {\n" +
      "    window['FOO'] = 123;\n" +
      "    return window;\n" +
      "  }");
    assertEquals(123, page.evaluate("e => e['FOO']", aHandle));
  }
}
