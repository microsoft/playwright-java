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

import com.microsoft.playwright.ConsoleMessage;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.Env;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.DisplayName;

import java.util.Objects;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/page/jshandle-to-string.spec.ts. */
public class TestJshandleToString {
  private static boolean startsWith(String channel, String prefix) {
    return channel != null && channel.startsWith(prefix);
  }

  // Java: expect.poll(fn).toBe(expected) with the defaults of expect.poll: 5000ms timeout, intervals 100, 250, 500, 1000ms
  private static void expectPollToBe(Page page, Supplier<Object> fn, Object expected) {
    int[] intervals = {100, 250, 500, 1000};
    long deadline = System.currentTimeMillis() + 5000;
    Object last = fn.get();
    for (int i = 0; !Objects.equals(expected, last) && System.currentTimeMillis() < deadline; i++) {
      page.waitForTimeout(intervals[Math.min(i, intervals.length - 1)]);
      last = fn.get();
    }
    assertEquals(expected, last);
  }

  @PlaywrightTest
  @DisplayName("JH-018 should work for primitives")
  void shouldWorkForPrimitives(Page page) {
    JSHandle numberHandle = page.evaluateHandle("() => 2");
    assertEquals("2", numberHandle.toString());
    JSHandle stringHandle = page.evaluateHandle("() => 'a'");
    assertEquals("a", stringHandle.toString());
  }

  @PlaywrightTest
  @DisplayName("JH-019 should work for complicated objects")
  void shouldWorkForComplicatedObjects(Page page, BrowserName browserName) {
    String channel = Env.channel();
    JSHandle aHandle = page.evaluateHandle("() => window");
    if (browserName != BrowserName.FIREFOX || startsWith(channel, "moz-firefox")) {
      assertEquals("Window", aHandle.toString());
    } else {
      assertEquals("JSHandle@object", aHandle.toString());
    }
  }

  @PlaywrightTest
  @DisplayName("JH-020 should beautifully render sparse arrays")
  void shouldBeautifullyRenderSparseArrays(Page page, BrowserName browserName) {
    ConsoleMessage msg = page.waitForConsoleMessage(() -> page.evaluateHandle("() => {\n" +
      "      const a = [];\n" +
      "      a[1] = 1;\n" +
      "      a[10] = 2;\n" +
      "      a[100] = 3;\n" +
      "      console.log(a);\n" +
      "    }"));
    if (browserName == BrowserName.FIREFOX) {
      assertEquals("Array", msg.text());
    } else {
      assertEquals("[empty, 1, empty x 8, 2, empty x 89, 3]", msg.text());
    }
  }

  @PlaywrightTest
  @DisplayName("JH-021 should work for promises")
  void shouldWorkForPromises(Page page) {
    // wrap the promise in an object, otherwise we will await.
    JSHandle wrapperHandle = page.evaluateHandle("() => ({ b: Promise.resolve(123) })");
    JSHandle bHandle = wrapperHandle.getProperty("b");
    assertEquals("Promise", bHandle.toString());
  }

  @PlaywrightTest
  @DisplayName("JH-022 should work with different subtypes @smoke")
  @Fixme("java: page.evaluateHandle(\"(function(){})\") invokes the function, the client does not send isFunction=false, so the handle is 'undefined'")
  void shouldWorkWithDifferentSubtypes(Page page, BrowserName browserName) {
    String channel = Env.channel();
    boolean isBiDi = startsWith(channel, "bidi-chrom") || startsWith(channel, "moz-firefox");
    if (!isBiDi) {
      assertTrue(page.evaluateHandle("(function(){})").toString().contains("function"));
    } else {
      assertEquals("Function", page.evaluateHandle("(function(){})").toString());
    }
    assertEquals("12", page.evaluateHandle("12").toString());
    assertEquals("true", page.evaluateHandle("true").toString());
    assertEquals("undefined", page.evaluateHandle("undefined").toString());
    assertEquals("foo", page.evaluateHandle("\"foo\"").toString());
    assertEquals("Symbol()", page.evaluateHandle("Symbol()").toString());
    assertTrue(page.evaluateHandle("new Map()").toString().contains("Map"));
    assertTrue(page.evaluateHandle("new Set()").toString().contains("Set"));
    assertTrue(page.evaluateHandle("[]").toString().contains("Array"));
    assertEquals("null", page.evaluateHandle("null").toString());
    JSHandle bodyHandle = page.evaluateHandle("document.body");
    expectPollToBe(page, () -> bodyHandle.toString(), "JSHandle@<body></body>");
    assertEquals("WeakMap", page.evaluateHandle("new WeakMap()").toString());
    assertEquals("WeakSet", page.evaluateHandle("new WeakSet()").toString());
    assertTrue(page.evaluateHandle("new Error()").toString().contains("Error"));
    assertEquals((browserName == BrowserName.CHROMIUM && !isBiDi) ? "Proxy(Object)" : "Proxy", page.evaluateHandle("new Proxy({}, {})").toString());
  }

  @PlaywrightTest
  @DisplayName("JH-023 should work with previewable subtypes")
  void shouldWorkWithPreviewableSubtypes(Page page, BrowserName browserName) {
    String channel = Env.channel();
    boolean isBiDi = startsWith(channel, "bidi-chrom") || startsWith(channel, "moz-firefox");
    assumeFalse(browserName == BrowserName.FIREFOX && !isBiDi);
    assertEquals("/foo/", page.evaluateHandle("/foo/").toString());
    assertTrue(page.evaluateHandle("new Date(0)").toString().contains("GMT"));
    if (!isBiDi) {
      assertTrue(page.evaluateHandle("new Int32Array()").toString().contains("Int32Array"));
    } else {
      assertEquals("TypedArray", page.evaluateHandle("new Int32Array()").toString());
    }
  }
}
