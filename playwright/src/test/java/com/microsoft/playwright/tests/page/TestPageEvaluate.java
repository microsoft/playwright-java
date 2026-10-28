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

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.BrowserName;
import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.EnabledOnBrowser;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;

import java.math.BigInteger;
import java.net.URL;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import static com.microsoft.playwright.fixtures.Utils.attachFrame;
import static com.microsoft.playwright.fixtures.Utils.browserMajorVersion;
import static com.microsoft.playwright.fixtures.Utils.detachFrame;
import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/** Port of tests/page/page-evaluate.spec.ts. */
public class TestPageEvaluate {
  @PlaywrightTest
  @DisplayName("EV-001 should work @smoke")
  void shouldWork(Page page) {
    Object result = page.evaluate("() => 7 * 3");
    assertEquals(21, result);
  }

  @PlaywrightTest
  @DisplayName("EV-002 should transfer NaN")
  void shouldTransferNaN(Page page) {
    Object result = page.evaluate("a => a", Double.NaN);
    // Java: Double.equals is Object.is, it tells NaN from other numbers and -0 from 0.
    assertEquals(Double.NaN, result);
  }

  @PlaywrightTest
  @DisplayName("EV-003 should transfer -0")
  void shouldTransfer0(Page page) {
    Object result = page.evaluate("a => a", -0.0);
    // Java: Double.equals is Object.is, it tells NaN from other numbers and -0 from 0.
    assertEquals(-0.0, result);
  }

  @PlaywrightTest
  @DisplayName("EV-004 should transfer Infinity")
  void shouldTransferInfinity(Page page) {
    Object result = page.evaluate("a => a", Double.POSITIVE_INFINITY);
    assertEquals(Double.POSITIVE_INFINITY, result);
  }

  @PlaywrightTest
  @DisplayName("EV-005 should transfer -Infinity")
  void shouldTransferInfinity2(Page page) {
    Object result = page.evaluate("a => a", Double.NEGATIVE_INFINITY);
    assertEquals(Double.NEGATIVE_INFINITY, result);
  }

  @PlaywrightTest
  @DisplayName("EV-006 should roundtrip unserializable values")
  void shouldRoundtripUnserializableValues(Page page) {
    Map<String, Object> value = mapOf(
      "infinity", Double.POSITIVE_INFINITY,
      "nInfinity", Double.NEGATIVE_INFINITY,
      "nZero", -0.0,
      "nan", Double.NaN);
    Object result = page.evaluate("value => value", value);
    assertEquals(value, result);
  }

  @PlaywrightTest
  @DisplayName("EV-007 should roundtrip promise to value")
  void shouldRoundtripPromiseToValue(Page page) {
    {
      Object result = page.evaluate("value => Promise.resolve(value)", null);
      assertNull(result);
    }
    {
      Object result = page.evaluate("value => Promise.resolve(value)", Double.POSITIVE_INFINITY);
      assertTrue((Double) result == Double.POSITIVE_INFINITY);
    }
    {
      Object result = page.evaluate("value => Promise.resolve(value)", -0.0);
      assertTrue((Double) result == -0.0);
    }
    {
      // Java: undefined is null.
      Object result = page.evaluate("value => Promise.resolve(value)", null);
      assertNull(result);
    }
  }

  @PlaywrightTest
  @DisplayName("EV-008 should roundtrip promise to unserializable values")
  void shouldRoundtripPromiseToUnserializableValues(Page page) {
    Map<String, Object> value = mapOf(
      "infinity", Double.POSITIVE_INFINITY,
      "nInfinity", Double.NEGATIVE_INFINITY,
      "nZero", -0.0,
      "nan", Double.NaN);
    Object result = page.evaluate("value => Promise.resolve(value)", value);
    assertEquals(value, result);
  }

  @PlaywrightTest
  @DisplayName("EV-009 should transfer arrays")
  void shouldTransferArrays(Page page) {
    Object result = page.evaluate("a => a", asList(1, 2, 3));
    assertEquals(asList(1, 2, 3), result);
  }

  @PlaywrightTest
  @DisplayName("EV-010 should transfer arrays as arrays, not objects")
  void shouldTransferArraysAsArraysNotObjects(Page page) {
    Object result = page.evaluate("a => Array.isArray(a)", asList(1, 2, 3));
    assertEquals(true, result);
  }

  @PlaywrightTest
  @DisplayName("EV-012 should transfer bigint")
  void shouldTransferBigint(Page page) {
    assertEquals(BigInteger.valueOf(42), page.evaluate("() => 42n"));
    assertEquals(BigInteger.valueOf(17), page.evaluate("a => a", BigInteger.valueOf(17)));
  }

  @PlaywrightTest
  @DisplayName("EV-015 should modify global environment")
  void shouldModifyGlobalEnvironment(Page page) {
    page.evaluate("() => window['globalVar'] = 123");
    assertEquals(123, page.evaluate("globalVar"));
  }

  @PlaywrightTest
  @DisplayName("EV-016 should evaluate in the page context")
  void shouldEvaluateInThePageContext(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/global-var.html");
    assertEquals(123, page.evaluate("globalVar"));
  }

  @PlaywrightTest
  @DisplayName("EV-017 should return undefined for objects with symbols")
  void shouldReturnUndefinedForObjectsWithSymbols(Page page) {
    // Java: undefined is null.
    assertEquals(Collections.singletonList(null), page.evaluate("() => [Symbol('foo4')]"));
    assertEquals(mapOf(), page.evaluate("() => {\n" +
      "    const a = {};\n" +
      "    a[Symbol('foo4')] = 42;\n" +
      "    return a;\n" +
      "  }"));
    assertEquals(mapOf("foo", asList(mapOf("a", null))), page.evaluate("() => {\n" +
      "    return { foo: [{ a: Symbol('foo4') }] };\n" +
      "  }"));
  }

  @PlaywrightTest
  @DisplayName("EV-019 should work with unicode chars")
  void shouldWorkWithUnicodeChars(Page page) {
    Object result = page.evaluate("a => a['中文字符']", mapOf("中文字符", 42));
    assertEquals(42, result);
  }

  @PlaywrightTest
  @DisplayName("EV-020 should work with large strings")
  void shouldWorkWithLargeStrings(Page page) {
    String expected = String.join("", Collections.nCopies(40000, "x"));
    assertEquals(expected, page.evaluate("data => data", expected));
  }

  @PlaywrightTest
  @DisplayName("EV-021 should work with large unicode strings")
  void shouldWorkWithLargeUnicodeStrings(Page page) {
    String expected = String.join("", Collections.nCopies(10000, "🎭"));
    assertEquals(expected, page.evaluate("data => data", expected));
  }

  @PlaywrightTest
  @DisplayName("EV-022 should throw when evaluation triggers reload")
  void shouldThrowWhenEvaluationTriggersReload(Page page) {
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.evaluate("() => {\n" +
      "    location.reload();\n" +
      "    return new Promise(() => { });\n" +
      "  }"));
    assertTrue(error.getMessage().contains("navigation"));
  }

  @PlaywrightTest
  @DisplayName("EV-023 should await promise")
  void shouldAwaitPromise(Page page) {
    Object result = page.evaluate("() => Promise.resolve(8 * 7)");
    assertEquals(56, result);
  }

  @PlaywrightTest
  @DisplayName("EV-024 should work right after framenavigated")
  void shouldWorkRightAfterFramenavigated(Page page, TestServer server) throws Exception {
    AtomicReference<Object> frameEvaluation = new AtomicReference<>();
    // Java: the handler evaluates inline, upstream stores the promise and awaits it after the navigation.
    page.onFrameNavigated(frame -> {
      frameEvaluation.set(frame.evaluate("() => 6 * 7"));
    });
    page.navigate(server.EMPTY_PAGE);
    assertEquals(42, frameEvaluation.get());
  }

  @PlaywrightTest
  @DisplayName("EV-025 should work right after a cross-origin navigation")
  void shouldWorkRightAfterACrossOriginNavigation(Page page, TestServer server) throws Exception {
    page.navigate(server.EMPTY_PAGE);
    AtomicReference<Object> frameEvaluation = new AtomicReference<>();
    // Java: the handler evaluates inline, upstream stores the promise and awaits it after the navigation.
    page.onFrameNavigated(frame -> {
      frameEvaluation.set(frame.evaluate("() => 6 * 7"));
    });
    page.navigate(server.CROSS_PROCESS_PREFIX + "/empty.html");
    assertEquals(42, frameEvaluation.get());
  }

  @PlaywrightTest
  @DisplayName("EV-026 should work from-inside an exposed function")
  void shouldWorkFromInsideAnExposedFunction(Page page) {
    // Setup inpage callback, which calls Page.evaluate
    page.exposeFunction("callController", args -> {
      Object a = args[0];
      Object b = args[1];
      return page.evaluate("({ a, b }) => a * b", mapOf("a", a, "b", b));
    });
    Object result = page.evaluate("async function() {\n" +
      "    return await window['callController'](9, 3);\n" +
      "  }");
    assertEquals(27, result);
  }

  @PlaywrightTest
  @DisplayName("EV-027 should reject promise with exception")
  void shouldRejectPromiseWithException(Page page) {
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.evaluate("() => not_existing_object.property"));
    assertNotNull(error);
    assertTrue(error.getMessage().contains("not_existing_object"));
  }

  @PlaywrightTest
  @DisplayName("EV-028 should support thrown strings as error messages")
  void shouldSupportThrownStringsAsErrorMessages(Page page) {
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.evaluate("() => { throw 'qwerty'; }"));
    assertNotNull(error);
    assertTrue(error.getMessage().contains("qwerty"));
  }

  @PlaywrightTest
  @DisplayName("EV-029 should support thrown numbers as error messages")
  void shouldSupportThrownNumbersAsErrorMessages(Page page) {
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.evaluate("() => { throw 100500; }"));
    assertNotNull(error);
    assertTrue(error.getMessage().contains("100500"));
  }

  @PlaywrightTest
  @DisplayName("EV-030 should reject when a falsy value is thrown")
  void shouldRejectWhenAFalsyValueIsThrown(Page page) {
    for (String value : new String[]{"null", "undefined", "0", "''", "false", "NaN", "-0", "0n"}) {
      // Java: the Error class of JavaScript is PlaywrightException.
      assertThrows(PlaywrightException.class, () -> page.evaluate("(() => { throw " + value + "; })()"), "throw " + value);
      assertThrows(PlaywrightException.class, () -> page.evaluate("Promise.reject(" + value + ")"), "Promise.reject(" + value + ")");
    }
  }

  @PlaywrightTest
  @DisplayName("EV-031 should include a non-error rejection value in the error message")
  void shouldIncludeANonErrorRejectionValueInTheErrorMessage(Page page) {
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.evaluate("() => Promise.reject(7)"));
    assertTrue(error.getMessage().contains("7"));
    PlaywrightException nullError = assertThrows(PlaywrightException.class,
      () -> page.evaluate("() => { throw null; }"));
    assertTrue(nullError.getMessage().contains("null"));
  }

  @PlaywrightTest
  @DisplayName("EV-032 should return complex objects")
  void shouldReturnComplexObjects(Page page) {
    Map<String, Object> object = mapOf("foo", "bar!");
    Object result = page.evaluate("a => a", object);
    assertNotSame(object, result);
    assertEquals(object, result);
  }

  @PlaywrightTest
  @DisplayName("EV-033 should return NaN")
  void shouldReturnNaN(Page page) {
    Object result = page.evaluate("() => NaN");
    assertEquals(Double.NaN, result);
  }

  @PlaywrightTest
  @DisplayName("EV-034 should return -0")
  void shouldReturn0(Page page) {
    Object result = page.evaluate("() => -0");
    assertEquals(-0.0, result);
  }

  @PlaywrightTest
  @DisplayName("EV-035 should return Infinity")
  void shouldReturnInfinity(Page page) {
    Object result = page.evaluate("() => Infinity");
    assertEquals(Double.POSITIVE_INFINITY, result);
  }

  @PlaywrightTest
  @DisplayName("EV-036 should return -Infinity")
  void shouldReturnInfinity2(Page page) {
    Object result = page.evaluate("() => -Infinity");
    assertEquals(Double.NEGATIVE_INFINITY, result);
  }

  @PlaywrightTest
  @DisplayName("EV-037 should work with overwritten Promise")
  @Fixme(value = "java: evaluateHandle of a promise of the overwritten Promise class is not awaited, jsonValue() is <{_promise={}}> instead of 42", browsers = {BrowserName.FIREFOX, BrowserName.WEBKIT})
  void shouldWorkWithOverwrittenPromise(Page page) {
    page.evaluate("() => {\n" +
      "    const originalPromise = window.Promise;\n" +
      "    class Promise2 {\n" +
      "      static all(arg) {\n" +
      "        return wrap(originalPromise.all(arg));\n" +
      "      }\n" +
      "      static race(arg) {\n" +
      "        return wrap(originalPromise.race(arg));\n" +
      "      }\n" +
      "      static resolve(arg) {\n" +
      "        return wrap(originalPromise.resolve(arg));\n" +
      "      }\n" +
      "      constructor(f) {\n" +
      "        this._promise = new originalPromise(f);\n" +
      "      }\n" +
      "      then(f, r) {\n" +
      "        return wrap(this._promise.then(f, r));\n" +
      "      }\n" +
      "      catch(f) {\n" +
      "        return wrap(this._promise.catch(f));\n" +
      "      }\n" +
      "      finally(f) {\n" +
      "        return wrap(this._promise.finally(f));\n" +
      "      }\n" +
      "    }\n" +
      "    const wrap = p => {\n" +
      "      const result = new Promise2(() => { });\n" +
      "      result._promise = p;\n" +
      "      return result;\n" +
      "    };\n" +
      "    window.Promise = Promise2;\n" +
      "    window['__Promise2'] = Promise2;\n" +
      "  }");

    // Sanity check.
    assertEquals(true, page.evaluate("() => {\n" +
      "    const p = Promise.all([Promise.race([]), new Promise(() => { }).then(() => { })]);\n" +
      "    return p instanceof window['__Promise2'];\n" +
      "  }"));

    // Now, the new promise should be awaitable.
    assertEquals(42, page.evaluate("() => Promise.resolve(42)"));
    // No page timers below: the frozen-clock project never fires them.
    JSHandle handle = page.evaluateHandle("() => new Promise(f => queueMicrotask(() => f(42)))");
    assertEquals(42, handle.jsonValue());
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.waitForFunction("() => false", null, new Page.WaitForFunctionOptions().setTimeout(500)));
    assertTrue(error.getMessage().contains("Timeout 500ms exceeded"));
    page.evaluate("() => window.__done = true");
    page.waitForFunction("() => window.__done");
  }

  @PlaywrightTest
  @DisplayName("EV-039 should accept \"undefined\" as one of multiple parameters")
  void shouldAcceptUndefinedAsOneOfMultipleParameters(Page page) {
    // Java: undefined is null.
    Object result = page.evaluate("({ a, b }) => Object.is(a, undefined) && Object.is(b, 'foo')", mapOf("a", null, "b", "foo"));
    assertEquals(true, result);
  }

  @PlaywrightTest
  @DisplayName("EV-040 should properly serialize undefined arguments")
  void shouldProperlySerializeUndefinedArguments(Page page) {
    // Java: undefined is null, and toEqual() ignores a property with an undefined value, but the property comes back as null.
    assertEquals(mapOf("a", null), page.evaluate("x => ({ a: x })", null));
  }

  @PlaywrightTest
  @DisplayName("EV-041 should properly serialize undefined fields")
  void shouldProperlySerializeUndefinedFields(Page page) {
    // Java: undefined is null, and toEqual() ignores a property with an undefined value, but the property comes back as null.
    assertEquals(mapOf("a", null), page.evaluate("() => ({ a: undefined })"));
  }

  @PlaywrightTest
  @DisplayName("EV-042 should return undefined properties")
  @SuppressWarnings("unchecked")
  void shouldReturnUndefinedProperties(Page page) {
    Map<String, Object> value = (Map<String, Object>) page.evaluate("() => ({ a: undefined })");
    assertEquals(true, value.containsKey("a"));
  }

  @PlaywrightTest
  @DisplayName("EV-043 should properly serialize null arguments")
  void shouldProperlySerializeNullArguments(Page page) {
    assertNull(page.evaluate("x => x", null));
  }

  @PlaywrightTest
  @DisplayName("EV-044 should properly serialize null fields")
  void shouldProperlySerializeNullFields(Page page) {
    assertEquals(mapOf("a", null), page.evaluate("() => ({ a: null })"));
  }

  @PlaywrightTest
  @DisplayName("EV-045 should properly serialize PerformanceMeasure object")
  @SuppressWarnings("unchecked")
  void shouldProperlySerializePerformanceMeasureObject(Page page) {
    List<Object> result = (List<Object>) page.evaluate("() => {\n" +
      "    window.builtins.performance.mark('start');\n" +
      "    window.builtins.performance.mark('end');\n" +
      "    window.builtins.performance.measure('my-measure', 'start', 'end');\n" +
      "    return window.builtins.performance.getEntriesByType('measure');\n" +
      "  }");
    // Java: expect.objectContaining() with expect.any(Number) is a check of each property.
    assertEquals(1, result.size());
    Map<String, Object> measure = (Map<String, Object>) result.get(0);
    assertTrue(measure.get("duration") instanceof Number);
    assertEquals("measure", measure.get("entryType"));
    assertEquals("my-measure", measure.get("name"));
    assertTrue(measure.get("startTime") instanceof Number);
  }

  @PlaywrightTest
  @DisplayName("EV-046 should properly serialize window.performance object")
  @SuppressWarnings("unchecked")
  void shouldProperlySerializeWindowPerformanceObject(Page page) {
    String pwClock = System.getenv("PW_CLOCK");
    assumeFalse(pwClock != null && !pwClock.isEmpty());

    Map<String, Object> result = (Map<String, Object>) page.evaluate("() => performance");
    // Java: expect.objectContaining() with expect.any(Number) is a check of each property.
    Map<String, Object> navigation = (Map<String, Object>) result.get("navigation");
    assertEquals(2, navigation.size());
    assertEquals(0, navigation.get("redirectCount"));
    assertTrue(navigation.get("type") instanceof Number);
    assertTrue(result.get("timeOrigin") instanceof Number);
    Map<String, Object> timing = (Map<String, Object>) result.get("timing");
    List<String> timingKeys = asList("connectEnd", "connectStart", "domComplete", "domContentLoadedEventEnd",
      "domContentLoadedEventStart", "domInteractive", "domLoading", "domainLookupEnd", "domainLookupStart",
      "fetchStart", "loadEventEnd", "loadEventStart", "navigationStart", "redirectEnd", "redirectStart",
      "requestStart", "responseEnd", "responseStart", "secureConnectionStart", "unloadEventEnd", "unloadEventStart");
    assertEquals(timingKeys.size(), timing.size());
    for (String key : timingKeys) {
      assertTrue(timing.get(key) instanceof Number, key);
    }
  }

  @PlaywrightTest
  @DisplayName("EV-047 should return undefined for non-serializable objects")
  void shouldReturnUndefinedForNonSerializableObjects(Page page) {
    assertNull(page.evaluate("() => function() {}"));
  }

  @PlaywrightTest
  @DisplayName("EV-048 should throw for too deep reference chain")
  void shouldThrowForTooDeepReferenceChain(Page page, BrowserName browserName) {
    assumeFalse(browserName == BrowserName.FIREFOX && !com.microsoft.playwright.fixtures.Env.isBidi(),
      "fixme: Firefox Juggler -> Playwright serialiser does not throw for deep references.\n" +
      "This causes large objects to get serialised back to the Playwright client.\n" +
      "There our validators throw 'Maximum call stack size exceeded'.");
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.evaluate("depth => {\n" +
      "    const obj = {};\n" +
      "    let temp = obj;\n" +
      "    for (let i = 0; i < depth; i++) {\n" +
      "      temp[i] = {};\n" +
      "      temp = temp[i];\n" +
      "    }\n" +
      "    return obj;\n" +
      "  }", 1000));
    assertTrue(error.getMessage().contains("Cannot serialize result: object reference chain is too long."));
  }

  @PlaywrightTest
  @DisplayName("EV-049 should throw for too deep reference chain 2")
  @EnabledOnBrowser(value = BrowserName.CHROMIUM, reason = "this is a chromium-only limitation")
  void shouldThrowForTooDeepReferenceChain2(Page page) {
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.evaluate("depth => {\n" +
      "    let node = {};\n" +
      "    for (let i = 0; i < depth; i++)\n" +
      "      node = { child: node };\n" +
      "    return node;\n" +
      "  }", 200));
    assertTrue(error.getMessage().contains("Cannot serialize result: object reference chain is too long."));
  }

  @PlaywrightTest
  @DisplayName("EV-054 should alias Window, Document and Node")
  void shouldAliasWindowDocumentAndNode(Page page) {
    Object object = page.evaluate("[window, document, document.body]");
    assertEquals(asList("ref: <Window>", "ref: <Document>", "ref: <Node>"), object);
  }

  @PlaywrightTest
  @DisplayName("EV-055 should work for circular object")
  @SuppressWarnings("unchecked")
  void shouldWorkForCircularObject(Page page) {
    Map<String, Object> result = (Map<String, Object>) page.evaluate("() => {\n" +
      "    const a = {};\n" +
      "    a.b = a;\n" +
      "    return a;\n" +
      "  }");
    Map<String, Object> a = new LinkedHashMap<>();
    a.put("b", a);
    // Java: Map.equals() does not terminate on a cycle, compare the structure.
    assertEquals(a.keySet(), result.keySet());
    assertSame(result, result.get("b"));
  }

  @PlaywrightTest
  @DisplayName("EV-056 should be able to throw a tricky error")
  void shouldBeAbleToThrowATrickyError(Page page) {
    JSHandle windowHandle = page.evaluateHandle("() => window");
    String errorText;
    try {
      errorText = (String) windowHandle.jsonValue();
    } catch (PlaywrightException e) {
      errorText = e.getMessage();
    }
    String finalErrorText = errorText;
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.evaluate("errorText => {\n" +
      "    throw new Error(errorText);\n" +
      "  }", finalErrorText));
    assertTrue(error.getMessage().contains(errorText));
  }

  @PlaywrightTest
  @DisplayName("EV-057 should accept a string")
  void shouldAcceptAString(Page page) {
    Object result = page.evaluate("1 + 2");
    assertEquals(3, result);
  }

  @PlaywrightTest
  @DisplayName("EV-058 should accept a string with semi colons")
  void shouldAcceptAStringWithSemiColons(Page page) {
    Object result = page.evaluate("1 + 5;");
    assertEquals(6, result);
  }

  @PlaywrightTest
  @DisplayName("EV-059 should accept a string with comments")
  void shouldAcceptAStringWithComments(Page page) {
    Object result = page.evaluate("2 + 5;\n// do some math!");
    assertEquals(7, result);
  }

  @PlaywrightTest
  @DisplayName("EV-060 should accept element handle as an argument")
  void shouldAcceptElementHandleAsAnArgument(Page page) {
    page.setContent("<section>42</section>");
    ElementHandle element = page.querySelector("section");
    Object text = page.evaluate("e => e.textContent", element);
    assertEquals("42", text);
  }

  @PlaywrightTest
  @DisplayName("EV-061 should throw if underlying element was disposed")
  void shouldThrowIfUnderlyingElementWasDisposed(Page page) {
    page.setContent("<section>39</section>");
    ElementHandle element = page.querySelector("section");
    assertNotNull(element);
    element.dispose();
    PlaywrightException error = assertThrows(PlaywrightException.class,
      () -> page.evaluate("e => e.textContent", element));
    assertTrue(error.getMessage().contains("no object with guid"));
  }

  @PlaywrightTest
  @DisplayName("EV-062 should simulate a user gesture")
  void shouldSimulateAUserGesture(Page page) {
    Object result = page.evaluate("() => {\n" +
      "    document.body.appendChild(document.createTextNode('test'));\n" +
      "    document.execCommand('selectAll');\n" +
      "    return document.execCommand('copy');\n" +
      "  }");
    assertEquals(true, result);
  }

  @PlaywrightTest
  @DisplayName("EV-063 should throw a nice error after a navigation")
  void shouldThrowANiceErrorAfterANavigation(Page page) {
    // Java: the first evaluate blocks, so the reload that upstream triggers with a second evaluate
    // is scheduled from the page before the promise is returned.
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.evaluate("() => {\n" +
      "  setTimeout(() => {\n" +
      "    window.location.reload();\n" +
      "    setTimeout(() => window['__resolve'](42), 1000);\n" +
      "  }, 0);\n" +
      "  return new Promise(f => window['__resolve'] = f);\n" +
      "}"));
    assertTrue(error.getMessage().contains("navigation"), error.getMessage());
  }

  @PlaywrightTest
  @DisplayName("EV-064 should not throw an error when evaluation does a navigation")
  void shouldNotThrowAnErrorWhenEvaluationDoesANavigation(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/one-style.html");
    Object result = page.evaluate("() => {\n" +
      "    window.location.href = '/empty.html';\n" +
      "    return [42];\n" +
      "  }");
    assertEquals(asList(42), result);
  }

  @PlaywrightTest
  @DisplayName("EV-065 should not throw an error when evaluation does a synchronous navigation and returns an object")
  void shouldNotThrowAnErrorWhenEvaluationDoesASynchronousNavigationAndReturnsAnObject(Page page, TestServer server, BrowserName browserName) {
    // It is important to be on about:blank for sync reload.
    Object result = page.evaluate("() => {\n" +
      "    window.location.reload();\n" +
      "    return { a: 42 };\n" +
      "  }");
    assertEquals(mapOf("a", 42), result);
  }

  @PlaywrightTest
  @DisplayName("EV-066 should not throw an error when evaluation does a synchronous navigation and returns undefined")
  void shouldNotThrowAnErrorWhenEvaluationDoesASynchronousNavigationAndReturnsUndefined(Page page) {
    // It is important to be on about:blank for sync reload.
    Object result = page.evaluate("() => {\n" +
      "    window.location.reload();\n" +
      "    return undefined;\n" +
      "  }");
    assertNull(result);
  }

  @PlaywrightTest
  @DisplayName("EV-067 should transfer 100Mb of data from page to node.js")
  void shouldTransfer100MbOfDataFromPageToNodeJs(Page page) {
    // This is too slow with wire.
    String a = (String) page.evaluate("() => Array(100 * 1024 * 1024 + 1).join('a')");
    assertEquals(100 * 1024 * 1024, a.length());
  }

  @PlaywrightTest
  @DisplayName("EV-068 should throw error with detailed information on exception inside promise ")
  void shouldThrowErrorWithDetailedInformationOnExceptionInsidePromise(Page page) {
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.evaluate("() => new Promise(() => {\n" +
      "    throw new Error('Error in promise');\n" +
      "  })"));
    assertTrue(error.getMessage().contains("Error in promise"));
  }

  @PlaywrightTest
  @DisplayName("EV-069 should work even when JSON is set to null")
  void shouldWorkEvenWhenJSONIsSetToNull(Page page) {
    page.evaluate("() => { window.JSON.stringify = null; window.JSON = null; }");
    Object result = page.evaluate("() => ({ abc: 123 })");
    assertEquals(mapOf("abc", 123), result);
  }

  @PlaywrightTest
  @DisplayName("EV-070 should await promise from popup")
  void shouldAwaitPromiseFromPopup(Page page, TestServer server) {
    // Something is wrong about the way Firefox waits for the chained promise
    page.navigate(server.EMPTY_PAGE);
    Object result = page.evaluate("() => {\n" +
      "    const win = window.open('about:blank');\n" +
      "    return new win['Promise'](f => f(42));\n" +
      "  }");
    assertEquals(42, result);
  }

  @PlaywrightTest
  @DisplayName("EV-071 should work with new Function() and CSP")
  void shouldWorkWithNewFunctionAndCSP(Page page, TestServer server) {
    server.setCSP("/empty.html", "script-src " + server.PREFIX);
    page.navigate(server.PREFIX + "/empty.html");
    assertEquals(true, page.evaluate("() => new Function('return true')()"));
  }

  @PlaywrightTest
  @DisplayName("EV-072 should work with non-strict expressions")
  void shouldWorkWithNonStrictExpressions(Page page, TestServer server) {
    assertEquals(3.14, page.evaluate("() => {\n" +
      "    y = 3.14;\n" +
      "    return y;\n" +
      "  }"));
  }

  @PlaywrightTest
  @DisplayName("EV-073 should respect use strict expression")
  void shouldRespectUseStrictExpression(Page page) {
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.evaluate("() => {\n" +
      "    'use strict';\n" +
      "    variableY = 3.14;\n" +
      "    return variableY;\n" +
      "  }"));
    assertTrue(error.getMessage().contains("variableY"));
  }

  @PlaywrightTest
  @DisplayName("EV-074 should not leak utility script")
  void shouldNotLeakUtilityScript(Page page) {
    assertEquals(true, page.evaluate("this === window"));
  }

  @PlaywrightTest
  @DisplayName("EV-075 should not leak handles")
  void shouldNotLeakHandles(Page page) {
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.evaluate("handles.length"));
    assertTrue(error.getMessage().contains(" handles"));
  }

  @PlaywrightTest
  @DisplayName("EV-076 should work with CSP")
  void shouldWorkWithCSP(Page page, TestServer server) {
    server.setCSP("/empty.html", "script-src 'self'");
    page.navigate(server.EMPTY_PAGE);
    assertEquals(4, page.evaluate("() => 2 + 2"));
  }

  @PlaywrightTest
  @DisplayName("EV-077 should evaluate exception with a function on the stack")
  @Fixme("java: an Error returned from the page is deserialized as a plain Exception whose message is the stack")
  void shouldEvaluateExceptionWithAFunctionOnTheStack(Page page) {
    Exception error = (Exception) page.evaluate("() => {\n" +
      "    return (function functionOnStack() {\n" +
      "      return new Error('error message');\n" +
      "    })();\n" +
      "  }");
    assertEquals("error message", error.getMessage());
    // Java: the Exception has no stack of the page, only the message that carries it.
    assertTrue(error.getMessage().contains("functionOnStack"));
  }

  @PlaywrightTest
  @DisplayName("EV-078 should evaluate exception")
  @Fixme("java: an Error returned from the page is deserialized as a plain Exception whose message is the stack and has no name")
  void shouldEvaluateException(Page page) {
    Object error = page.evaluate("() => {\n" +
      "    function innerFunction() {\n" +
      "      const e = new Error('error message');\n" +
      "      e.name = 'foobar';\n" +
      "      return e;\n" +
      "    }\n" +
      "    return innerFunction();\n" +
      "  }");
    assertTrue(error instanceof Exception);
    assertEquals("error message", ((Exception) error).getMessage());
    // Java: an Exception has no name of the page, and no stack of the page, only the message that carries it.
    assertTrue(((Exception) error).getMessage().contains("innerFunction"));
  }

  // Java: the name of a class is the name of an Error, and the stack is the Java stack.
  private static class foobar extends Exception {
    foobar(String message) {
      super(message);
    }
  }

  private static Exception innerFunction() {
    Exception e = new foobar("error message");
    return e;
  }

  @PlaywrightTest
  @DisplayName("EV-079 should pass exception argument")
  @SuppressWarnings("unchecked")
  void shouldPassExceptionArgument(Page page) {
    Map<String, Object> received = (Map<String, Object>) page.evaluate("e => {\n" +
      "    return { message: e.message, name: e.name, stack: e.stack };\n" +
      "  }", innerFunction());
    assertEquals("error message", received.get("message"));
    assertEquals("foobar", received.get("name"));
    assertTrue(((String) received.get("stack")).contains("innerFunction"));
  }

  @PlaywrightTest
  @DisplayName("EV-080 should evaluate date")
  void shouldEvaluateDate(Page page) {
    Object result = page.evaluate("() => ({ date: new Date('2020-05-27T01:31:38.506Z') })");
    assertEquals(mapOf("date", Date.from(Instant.parse("2020-05-27T01:31:38.506Z"))), result);
  }

  @PlaywrightTest
  @DisplayName("EV-081 should roundtrip date")
  void shouldRoundtripDate(Page page) {
    Date date = Date.from(Instant.parse("2020-05-27T01:31:38.506Z"));
    Date result = (Date) page.evaluate("date => date", date);
    assertEquals(date, result);
  }

  @PlaywrightTest
  @DisplayName("EV-082 should roundtrip regex")
  void shouldRoundtripRegex(Page page) {
    Pattern regex = Pattern.compile("hello", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    Pattern result = (Pattern) page.evaluate("regex => regex", regex);
    // Java: Pattern.toString() is the source only, compare the source and the flags.
    assertEquals(regex.pattern(), result.pattern());
    assertEquals(regex.flags(), result.flags());
  }

  @PlaywrightTest
  @DisplayName("EV-083 should jsonValue() date")
  void shouldJsonValueDate(Page page) {
    JSHandle resultHandle = page.evaluateHandle("() => ({ date: new Date('2020-05-27T01:31:38.506Z') })");
    assertEquals(mapOf("date", Date.from(Instant.parse("2020-05-27T01:31:38.506Z"))), resultHandle.jsonValue());
  }

  @PlaywrightTest
  @DisplayName("EV-084 should evaluate url")
  @Fixme("java: a java.net.URL is not normalized like a JavaScript URL: expected <{url=https://example.com}> but was <{url=https://example.com/}>")
  void shouldEvaluateUrl(Page page) throws Exception {
    Object result = page.evaluate("() => ({ url: new URL('https://example.com') })");
    assertEquals(mapOf("url", new URL("https://example.com")), result);
  }

  @PlaywrightTest
  @DisplayName("EV-085 should roundtrip url")
  @Fixme("java: a java.net.URL is not normalized like a JavaScript URL: expected <https://example.com> but was <https://example.com/>")
  void shouldRoundtripUrl(Page page) throws Exception {
    URL url = new URL("https://example.com");
    URL result = (URL) page.evaluate("url => url", url);
    assertEquals(url.toString(), result.toString());
  }

  @PlaywrightTest
  @DisplayName("EV-086 should jsonValue() url")
  @Fixme("java: a java.net.URL is not normalized like a JavaScript URL: expected <{url=https://example.com}> but was <{url=https://example.com/}>")
  void shouldJsonValueUrl(Page page) throws Exception {
    JSHandle resultHandle = page.evaluateHandle("() => ({ url: new URL('https://example.com') })");
    assertEquals(mapOf("url", new URL("https://example.com")), resultHandle.jsonValue());
  }

  @PlaywrightTest
  @DisplayName("EV-087 should not use toJSON when evaluating")
  void shouldNotUseToJSONWhenEvaluating(Page page) {
    Object result = page.evaluate("() => ({ toJSON: () => 'string', data: 'data' })");
    assertEquals(mapOf("data", "data", "toJSON", mapOf()), result);
  }

  @PlaywrightTest
  @DisplayName("EV-088 should not use Array.prototype.toJSON when evaluating")
  void shouldNotUseArrayPrototypeToJSONWhenEvaluating(Page page) {
    Object result = page.evaluate("() => {\n" +
      "    Array.prototype.toJSON = () => 'busted';\n" +
      "    return [1, 2, 3];\n" +
      "  }");
    assertEquals(asList(1, 2, 3), result);
  }

  @PlaywrightTest
  @DisplayName("EV-089 should not add a toJSON property to newly created Arrays after evaluation")
  void shouldNotAddAToJSONPropertyToNewlyCreatedArraysAfterEvaluation(Page page, BrowserName browserName) {
    page.evaluate("() => []");
    Object hasToJSONProperty = page.evaluate("() => 'toJSON' in []");
    assertEquals(false, hasToJSONProperty);
  }

  @PlaywrightTest
  @DisplayName("EV-090 should not use toJSON in jsonValue")
  void shouldNotUseToJSONInJsonValue(Page page) {
    JSHandle resultHandle = page.evaluateHandle("() => ({ toJSON: () => 'string', data: 'data' })");
    assertEquals(mapOf("data", "data", "toJSON", mapOf()), resultHandle.jsonValue());
  }

  @PlaywrightTest
  @DisplayName("EV-091 should ignore buggy toJSON")
  void shouldIgnoreBuggyToJSON(Page page) {
    Object result = page.evaluate("() => {\n" +
      "    class Foo {\n" +
      "      toJSON() {\n" +
      "        throw new Error('Bad');\n" +
      "      }\n" +
      "    }\n" +
      "    class Bar {\n" +
      "      get toJSON() {\n" +
      "        throw new Error('Also bad');\n" +
      "      }\n" +
      "    }\n" +
      "    return { foo: new Foo(), bar: new Bar() };\n" +
      "  }");
    assertEquals(mapOf("foo", mapOf(), "bar", mapOf()), result);
  }

  @PlaywrightTest
  @DisplayName("EV-092 should not expose the injected script export")
  void shouldNotExposeTheInjectedScriptExport(Page page) {
    assertEquals(true, page.evaluate("typeof pwExport === \"undefined\""));
  }

  // EV-093 should throw when frame is detached: not ported, see upstream_not_ported.txt.

  @PlaywrightTest
  @DisplayName("EV-094 should work with overridden Object.defineProperty")
  void shouldWorkWithOverriddenObjectDefineProperty(Page page, TestServer server) {
    server.setRoute("/test", (req, res) -> {
      res.writeHead(200, "content-type", "text/html");
      res.end("<script>\n" +
        "    Object.create = null;\n" +
        "    Object.defineProperty = null;\n" +
        "    Object.getOwnPropertyDescriptor = null;\n" +
        "    Object.getOwnPropertyNames = null;\n" +
        "    Object.getPrototypeOf = null;\n" +
        "    Object.prototype.hasOwnProperty = null;\n" +
        "    </script>");
    });
    page.navigate(server.PREFIX + "/test");
    assertEquals(3, page.evaluate("1+2"));
  }

  @PlaywrightTest
  @DisplayName("EV-095 should work with busted Array.prototype.map/push")
  void shouldWorkWithBustedArrayPrototypeMapPush(Page page, TestServer server) {
    server.setRoute("/test", (req, res) -> {
      res.writeHead(200, "content-type", "text/html");
      res.end("<script>\n" +
        "      Array.prototype.map = null;\n" +
        "      Array.prototype.push = null;\n" +
        "    </script>");
    });
    page.navigate(server.PREFIX + "/test");
    assertEquals(3, page.evaluate("1+2"));
    assertEquals(3, page.evaluateHandle("1+2").jsonValue());
  }

  @PlaywrightTest
  @DisplayName("EV-096 should work with overridden globalThis.Window/Document/Node")
  void shouldWorkWithOverriddenGlobalThisWindowDocumentNode(Page page, TestServer server) {
    List<String> testCases = asList(
      "() => globalThis.Window = {}",
      "() => globalThis.Document = {}",
      "() => globalThis.Node = {}",
      "() => globalThis.Window = null",
      "() => globalThis.Document = null",
      "() => globalThis.Node = null");
    for (String testCase : testCases) {
      // it.step(testCase.toString())
      page.navigate(server.EMPTY_PAGE);
      page.evaluate(testCase);
      assertEquals(3, page.evaluate("1+2"));
      assertEquals(asList("foo"), page.evaluate("() => ['foo']"));
    }
  }

  @PlaywrightTest
  @DisplayName("EV-097 should work with overridden URL/Date/RegExp")
  void shouldWorkWithOverriddenURLDateRegExp(Page page, TestServer server) {
    List<String> testCases = asList(
      "() => globalThis.URL = 'foo'",
      "() => globalThis.RegExp = 'foo'",
      "() => globalThis.Date = 'foo'");
    for (String testCase : testCases) {
      // it.step(testCase.toString())
      page.navigate(server.EMPTY_PAGE);
      page.evaluate(testCase);
      assertEquals(3, page.evaluate("1+2"));
      assertEquals(mapOf("a", 2023), page.evaluate("() => ({ 'a': 2023 })"));
    }
  }

  @PlaywrightTest
  @DisplayName("EV-098 should work with Array.from/map")
  void shouldWorkWithArrayFromMap(Page page) {
    assertEquals("([a-f0-9]{2})([a-f0-9]{2})([a-f0-9]{2})", page.evaluate("() => {\n" +
      "    const r = (str, amount) => Array.from(Array(amount)).map(() => str).join('');\n" +
      "    return r('([a-f0-9]{2})', 3);\n" +
      "  }"));
  }

  @PlaywrightTest
  @DisplayName("EV-099 should work with a using declaration")
  @DisabledOnBrowser(value = BrowserName.WEBKIT, reason = "WebKit does not support using declarations")
  void shouldWorkWithAUsingDeclaration(Page page, BrowserName browserName, Browser browser) {
    assumeFalse(browserName == BrowserName.CHROMIUM && browserMajorVersion(browser) < 134, "Chromium supports using declarations since 134");
    Object disposed = page.evaluate("() => {\n" +
      "    let disposed = false;\n" +
      "    {\n" +
      "      using r = { [Symbol.dispose]: () => { disposed = true; } };\n" +
      "      void r;\n" +
      "    }\n" +
      "    return disposed;\n" +
      "  }");
    assertEquals(true, disposed);
  }

  @PlaywrightTest
  @DisplayName("EV-100 should ignore dangerous object keys")
  void shouldIgnoreDangerousObjectKeys(Page page) {
    // Java: the key is an own property of the map, the object literal of upstream sets the prototype.
    Map<String, Object> input = mapOf(
      "__proto__", mapOf("polluted", true),
      "safeKey", "safeValue");
    Object result = page.evaluate("arg => arg", input);
    assertEquals(mapOf("safeKey", "safeValue"), result);
  }
}
