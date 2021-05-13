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
import org.junit.jupiter.api.condition.DisabledIf;

import java.util.Map;

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
    Object result = page.evaluate("a => a", -0.0);
    assertEquals(Double.NEGATIVE_INFINITY, 1 / (Double) result);
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
      "nZero", -0.0,
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
      Object result = page.evaluate("value => Promise.resolve(value)", -0.0);
      assertEquals(Double.NEGATIVE_INFINITY, 1 / (Double) result);
    }
  }

  @Test
  void shouldRoundtripPromiseToUnserializableValues() {
    Map<String, ?> value = mapOf(
      "infinity", Double.POSITIVE_INFINITY,
      "nInfinity", Double.NEGATIVE_INFINITY,
      "nZero", -0.0,
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
    page.navigate(getServer().PREFIX + "/global-var.html");
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
    page.onFrameNavigated(frame -> {
      frameEvaluation[0] = frame.evaluate("() => 6 * 7");
    });
    page.navigate(getServer().EMPTY_PAGE);
    assertEquals(42, frameEvaluation[0]);
  }

  @Test
  void shouldWorkRightAfterACrossOriginNavigation() {
    page.navigate(getServer().EMPTY_PAGE);
    Object[] frameEvaluation = {null};
    page.onFrameNavigated(frame -> {
      frameEvaluation[0] = frame.evaluate("() => 6 * 7");
    });
    page.navigate(getServer().CROSS_PROCESS_PREFIX + "/empty.html");
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

  @Test
  void shouldSupportThrownStringsAsErrorMessages() {
    try {
      page.evaluate("() => { throw 'qwerty'; }");
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("qwerty"));
    }
  }

  @Test
  void shouldSupportThrownNumbersAsErrorMessages() {
    try {
      page.evaluate("() => { throw 100500; }");
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("100500"));
    }
  }

  @Test
  void shouldReturnComplexObjects() {
    Map<String, ?> object = mapOf("foo", "bar!");
    Object result = page.evaluate("a => a", object);
    assertFalse(object == result);
    assertEquals(object, result);
  }

  @Test
  void shouldReturnNaN() {
    Object result = page.evaluate("() => NaN");
    assertEquals(Double.NaN, result);
  }

  @Test
  void shouldReturn0() {
    Object result = page.evaluate("() => -0");
    assertEquals(Double.NEGATIVE_INFINITY, 1 / (Double) result);
  }

  @Test
  void shouldReturnInfinity() {
    Object result = page.evaluate("() => Infinity");
    assertEquals(Double.POSITIVE_INFINITY, result);
  }

  @Test
  void shouldReturnNegativeInfinity() {
    Object result = page.evaluate("() => -Infinity");
    assertEquals(Double.NEGATIVE_INFINITY, result);
  }

  @Test
  void shouldWorkWithOverwrittenPromise() {
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
      "  const p = Promise.all([Promise.race([]), new Promise(() => { }).then(() => { })]);\n" +
      "  return p instanceof window['__Promise2'];\n" +
      "}"));

    // Now, the new promise should be awaitable.
    assertEquals(42, page.evaluate("() => Promise.resolve(42)"));
  }

  void shouldThrowWhenPassedMoreThanOneParameter() {
    // Not applicable.
  }

  void shouldAcceptUndefinedAsOneOfMultipleParameters() {
    // Not applicable
  }

  @Test
  void shouldProperlySerializeUndefinedArguments() {
    // Not applicable
  }

  @Test
  void shouldProperlySerializeUndefinedFields() {
    assertEquals(mapOf("a", null), page.evaluate("() => ({ a: undefined })"));
  }

  @Test
  void shouldReturnUndefinedProperties() {
    Object value = page.evaluate("() => ({ a: undefined })");
    assertEquals(mapOf("a", null), value);
  }

  @Test
  void shouldProperlySerializeNullArguments() {
    assertEquals(null, page.evaluate("x => x", null));
  }

  @Test
  void shouldProperlySerializeNullFields() {
    assertEquals(mapOf("a", null), page.evaluate("() => ({ a: null })"));
  }

  @Test
  void shouldReturnUndefinedForNonSerializableObjects() {
    assertEquals(null, page.evaluate("() => window"));
  }

  @Test
  void shouldFailForCircularObject() {
    Object result = page.evaluate("() => {\n" +
      "  const a = {};\n" +
      "  const b = { a };\n" +
      "  a.b = b;\n" +
      "  return a;\n" +
      "}");
    assertNull(result);
  }

  @Test
  void shouldBeAbleToThrowATrickyError() {
    JSHandle windowHandle = page.evaluateHandle("() => window");
    String errorText = null;
    try {
      windowHandle.jsonValue();
      fail("did not throw");
    } catch (PlaywrightException e) {
      errorText = e.getMessage();
    }
    assertNotNull(errorText);
    try {
      page.evaluate("errorText => {\n" +
        "  throw new Error(errorText);\n" +
        "}", errorText);
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains(errorText));
    }
  }

  @Test
  void shouldAcceptAString() {
    Object result = page.evaluate("1 + 2");
    assertEquals(3, result);
  }

  @Test
  void shouldAcceptAStringWithSemiColons() {
    Object result = page.evaluate("1 + 5;");
    assertEquals(6, result);
  }

  @Test
  void shouldAcceptAStringWithComments() {
    Object result = page.evaluate("2 + 5;\n// do some math!");
    assertEquals(7, result);
  }

  @Test
  void shouldAcceptElementHandleAsAnArgument() {
    page.setContent("<section>42</section>");
    ElementHandle element = page.querySelector("section");
    Object text = page.evaluate("e => e.textContent", element);
    assertEquals("42", text);
  }

  @Test
  void shouldThrowIfUnderlyingElementWasDisposed() {
    page.setContent("<section>39</section>");
    ElementHandle element = page.querySelector("section");
    assertNotNull(element);
    element.dispose();
    try {
      page.evaluate("e => e.textContent", element);
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("JSHandle is disposed"));
    }
  }

  @Test
  void shouldSimulateAUserGesture() {
    assertEquals(true, page.evaluate("() => {\n" +
      "  document.body.appendChild(document.createTextNode('test'));\n" +
      "  document.execCommand('selectAll');\n" +
      "  return document.execCommand('copy');\n" +
      "}"));
  }

  @Test
  void shouldThrowANiceErrorAfterANavigation() {
    try {
      page.waitForNavigation(() -> {
        page.evaluate("() => {\n" +
          "  const promise = new Promise(f => window['__resolve'] = f);\n" +
          "  window.location.reload();\n" +
          "  setTimeout(() => window['__resolve'](42), 1000);\n" +
          "  return promise;\n" +
          "}");
      });
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("navigation"));
    }
  }

  @Test
  void shouldNotThrowAnErrorWhenEvaluationDoesANavigation() {
    page.navigate(getServer().PREFIX + "/one-style.html");
    Object result = page.evaluate("() => {\n" +
      "  window.location.href = '/empty.html';\n" +
      "  return [42];\n" +
      "}");
    assertEquals(asList(42), result);
  }

  @Test
  @DisabledIf(value="com.microsoft.playwright.TestBase#isWebKit", disabledReason="fixme")
  void shouldNotThrowAnErrorWhenEvaluationDoesASynchronousNavigationAndReturnsAnObject() {
    // It is imporant to be on about:blank for sync reload.
    Object result = page.evaluate("() => {\n" +
      "  window.location.reload();\n" +
      "  return { a: 42 };\n" +
      "}");
    assertEquals(mapOf("a", 42), result);
  }

  @Test
  void shouldNotThrowAnErrorWhenEvaluationDoesASynchronousNavigationAndReturnsUndefined() {
    // It is imporant to be on about:blank for sync reload.
    Object result = page.evaluate("() => {\n" +
      "  window.location.reload();\n" +
      "  return undefined;\n" +
      "}");
    assertEquals(null, result);
  }

  @Test
  void shouldTransfer100MbOfDataFromPageToNodeJs() {
    // This is too slow with wire.
    Object a = page.evaluate("() => Array(100 * 1024 * 1024 + 1).join('a')");
    String str = (String) a;
    assertEquals(100 * 1024 * 1024, str.length());
    for (int i = 0; i < str.length(); i++) {
      if ('a' != str.charAt(i)) {
        fail("Unexpected char at position " + i);
      }
    }
  }

  @Test
  void shouldThrowErrorWithDetailedInformationOnExceptionInsidePromise() {
    try {
      page.evaluate("() => new Promise(() => {\n" +
        "  throw new Error('Error in promise');\n" +
        "})");
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("Error in promise"));
    }
  }

  @Test
  void shouldWorkEvenWhenJSONIsSetToNull() {
    page.evaluate("() => { window.JSON.stringify = null; window.JSON = null; }");
    Object result = page.evaluate("() => ({ abc: 123 })");
    assertEquals(mapOf("abc", 123), result);
  }

  @Test
  void shouldAwaitPromiseFromPopup() {
    // Something is wrong about the way Firefox waits for the chained promise
    page.navigate(getServer().EMPTY_PAGE);
    Object result = page.evaluate("() => {\n" +
      "  const win = window.open('about:blank');\n" +
      "  return new win['Promise'](f => f(42));\n" +
      "}");
    assertEquals(42, result);
  }

  @Test
  void shouldWorkWithNewFunctionAndCSP() {
    getServer().setCSP("/empty.html", "script-src " + getServer().PREFIX);
    page.navigate(getServer().PREFIX + "/empty.html");
    assertEquals(true, page.evaluate("() => new Function('return true')()"));
  }

  @Test
  void shouldWorkWithNonStrictExpressions() {
    assertEquals(3.14, page.evaluate("() => {\n" +
      "  y = 3.14;\n" +
      "  return y;\n" +
      "}"));
  }

  @Test
  void shouldRespectUseStrictExpression() {
    try {
      page.evaluate("() => {\n" +
        "  'use strict';\n" +
        "  // @ts-ignore\n" +
        "  variableY = 3.14;\n" +
        "  // @ts-ignore\n" +
        "  return variableY;\n" +
        "}");
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("variableY"));
    }
  }

  @Test
  void shouldNotLeakUtilityScript() {
    assertEquals(true, page.evaluate("() => this === window"));
  }

  @Test
  void shouldNotLeakHandles() {
    try {
      page.evaluate("handles.length");
      fail("did not throw");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains(" handles"));
    }
  }

  @Test
  void shouldWorkWithCSP() {
    getServer().setCSP("/empty.html", "script-src 'self'");
    page.navigate(getServer().EMPTY_PAGE);
    assertEquals(4, page.evaluate("() => 2 + 2"));
  }

  @Test
  void shouldEvaluateException() {
    try {
      page.evaluate("() => {\n" +
        "  return (function functionOnStack() {\n" +
        "    return new Error('error message');\n" +
        "  })();\n" +
        "}");
    } catch (PlaywrightException e) {
      assertTrue(e.getMessage().contains("Error: error message"));
      assertTrue(e.getMessage().contains("functionOnStack"));
    }
  }

  @Test
  void shouldEvaluateException2() {
    Object error = page.evaluate("new Error('error message')");
    assertTrue(((String) error).contains("Error: error message"));
  }

  void shouldEvaluateDate() {
    // TODO: Date values are not supported in java.
  }

  void shouldRoundtripDate() {
    // TODO: Date values are not supported in java.
  }

  void shouldRoundtripRegex() {
    // Not applicable
  }

  void shouldJsonValueDate() {
    // TODO: Date values are not supported in java.
  }

  @Test
  void shouldNotUseToJSONWhenEvaluating() {
    Object result = page.evaluate("() => ({ toJSON: () => 'string', data: 'data' })");
    assertEquals(mapOf("data", "data", "toJSON", emptyMap()), result);
  }

  @Test
  void shouldNotUseToJSONInJsonValue() {
    JSHandle resultHandle = page.evaluateHandle("() => ({ toJSON: () => 'string', data: 'data' })");
    assertEquals(mapOf("data", "data", "toJSON", emptyMap()), resultHandle.jsonValue());
  }
}
