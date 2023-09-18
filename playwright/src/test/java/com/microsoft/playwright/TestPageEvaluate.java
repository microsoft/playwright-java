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

import java.math.BigInteger;
import java.time.*;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.Date;
import java.net.MalformedURLException;
import java.net.URL;

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

  @Test
  void shouldTransferBigint() {
    assertEquals(new BigInteger("42", 10), page.evaluate("() => 42n"));
    assertEquals(new BigInteger("17", 10), page.evaluate("a => a", new BigInteger("17", 10)));
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
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.evaluate("() => {\n" +
        "  location.reload();\n" +
        "  return new Promise(() => { });\n" +
        "}");
    });
    assertTrue(e.getMessage().contains("navigation"));
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
    page.navigate(server.EMPTY_PAGE);
    assertEquals(42, frameEvaluation[0]);
  }

  @Test
  void shouldWorkRightAfterACrossOriginNavigation() {
    page.navigate(server.EMPTY_PAGE);
    Object[] frameEvaluation = {null};
    page.onFrameNavigated(frame -> {
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
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.evaluate("() => not_existing_object.property"));
    assertTrue(e.getMessage().contains("not_existing_object"));
  }

  @Test
  void shouldSupportThrownStringsAsErrorMessages() {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.evaluate("() => { throw 'qwerty'; }"));
    assertTrue(e.getMessage().contains("qwerty"));
  }

  @Test
  void shouldSupportThrownNumbersAsErrorMessages() {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.evaluate("() => { throw 100500; }"));
    assertTrue(e.getMessage().contains("100500"));
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
    assertEquals(null, page.evaluate("() => () => {}"));
    assertEquals("ref: <Window>", page.evaluate("() => window"));
  }

  @Test
  void shouldBeAbleToThrowATrickyError() {
    String errorText = "My error";
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.evaluate("errorText => {\n" +
        "  throw new Error(errorText);\n" +
        "}", errorText);
    });
    assertTrue(e.getMessage().contains(errorText));
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
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.evaluate("e => e.textContent", element));
    assertTrue(e.getMessage().contains("JSHandle is disposed"));
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
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.waitForNavigation(() -> {
        page.evaluate("() => {\n" +
          "  const promise = new Promise(f => window['__resolve'] = f);\n" +
          "  window.location.reload();\n" +
          "  setTimeout(() => window['__resolve'](42), 1000);\n" +
          "  return promise;\n" +
          "}");
      });
    });
    assertTrue(e.getMessage().contains("navigation"));
  }

  @Test
  void shouldNotThrowAnErrorWhenEvaluationDoesANavigation() {
    page.navigate(server.PREFIX + "/one-style.html");
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
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.evaluate("() => new Promise(() => {\n" +
        "  throw new Error('Error in promise');\n" +
        "})");
    });
    assertTrue(e.getMessage().contains("Error in promise"));
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
    page.navigate(server.EMPTY_PAGE);
    Object result = page.evaluate("() => {\n" +
      "  const win = window.open('about:blank');\n" +
      "  return new win['Promise'](f => f(42));\n" +
      "}");
    assertEquals(42, result);
  }

  @Test
  void shouldWorkWithNewFunctionAndCSP() {
    server.setCSP("/empty.html", "script-src " + server.PREFIX);
    page.navigate(server.PREFIX + "/empty.html");
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
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.evaluate("() => {\n" +
        "  'use strict';\n" +
        "  // @ts-ignore\n" +
        "  variableY = 3.14;\n" +
        "  // @ts-ignore\n" +
        "  return variableY;\n" +
        "}");
    });
    assertTrue(e.getMessage().contains("variableY"));
  }

  @Test
  void shouldNotLeakUtilityScript() {
    assertEquals(true, page.evaluate("() => this === window"));
  }

  @Test
  void shouldNotLeakHandles() {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.evaluate("handles.length"));
    assertTrue(e.getMessage().contains(" handles"));
  }

  @Test
  void shouldWorkWithCSP() {
    server.setCSP("/empty.html", "script-src 'self'");
    page.navigate(server.EMPTY_PAGE);
    assertEquals(4, page.evaluate("() => 2 + 2"));
  }

  @Test
  void shouldEvaluateException() {
    String result = (String) page.evaluate("() => {\n" +
      "  return (function functionOnStack() {\n" +
      "    return new Error('error message');\n" +
      "  })();\n" +
      "}");
    assertTrue(result.contains("Error: error message"));
    assertTrue(result.contains("functionOnStack"));
  }

  @Test
  void shouldEvaluateException2() {
    Object error = page.evaluate("new Error('error message')");
    assertTrue(((String) error).contains("Error: error message"));
  }

  @Test
  void shouldEvaluateDate() {
    Object result = page.evaluate("() => ({ date: new Date('2020-05-27T01:31:38.506Z') })");
    Date expected = Date.from(ZonedDateTime.parse("2020-05-27T01:31:38.506Z").toInstant());
    assertEquals(mapOf("date", expected), result);
  }

  @Test
  void shouldRoundtripDate() {
    Date date = Date.from(ZonedDateTime.parse("2020-05-27T01:31:38.506Z").toInstant());
    Object result = page.evaluate("date => date", date);
    assertTrue(result instanceof Date);
    assertEquals(date.toString(), result.toString());
  }

  @Test
  void shouldRoundtripRegex() {
    Pattern regex = Pattern.compile("hello", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    Object result = page.evaluate("regex => regex", regex);
    assertTrue(result instanceof Pattern);
    assertEquals(regex.toString(), result.toString());
    assertEquals(regex.flags(), ((Pattern)result).flags());
  }

  @Test
  void shouldJsonValueDate() {
    JSHandle resultHandle = page.evaluateHandle("() => ({ date: new Date('2020-05-27T01:31:38.506Z') })");
    assertEquals(mapOf("date", Date.from(ZonedDateTime.parse("2020-05-27T01:31:38.506Z").toInstant())), resultHandle.jsonValue());
  }

  @Test
  void shouldEvaluateUrl() throws MalformedURLException {
    Object result = page.evaluate("() => ({ url: new URL('https://example.com/') })");
    assertEquals(mapOf("url", new URL("https://example.com/")), result);
  }

  @Test
  void shouldRoundtripUrl() throws MalformedURLException {
    URL url = new URL("https://example.com/");
    Object result = page.evaluate("url => url", url);
    assertTrue(result instanceof URL);
    assertEquals(url.toString(), result.toString());
  }

  @Test
  void shouldRoundtripComplexUrl() throws MalformedURLException {
    URL url = new URL("https://user:password@www.contoso.com:80/Home/Index.htm?q1=v1&q2=v2#FragmentName");
    Object result = page.evaluate("url => url", url);
    assertTrue(result instanceof URL);
    assertEquals(url.toString(), result.toString());
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

  @Test
  void shouldAliasWindowDocumentAndNode() {
    Object object = page.evaluate("[window, document, document.body]");
    assertEquals(asList("ref: <Window>", "ref: <Document>", "ref: <Node>"), object);
  }

  @Test
  void shouldWorkForCircularObject() {
    Object result = page.evaluate("() => {\n" +
      "    const a = {};\n" +
      "    a.b = a;\n" +
      "    return a;\n" +
      "  }");

    Map<String, Object> map = (Map<String, Object>) result;
    assertEquals(1, map.size());
    assertTrue(map == map.get("b"));
  }

  @Test
  void shouldAcceptParameter() {
    Instant instant = Instant.now();
    LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
    Object object = page.evaluate("p => p", localDateTime);
    assertTrue(object instanceof Date);
    assertEquals(Date.from(instant), object);
  }

  @Test
  void shouldTransferMaps() {
    assertEquals(mapOf(), page.evaluate("() => new Map([[1, { test: 42n }]])"));
  }

  @Test
  void shouldTransferSets() {
    assertEquals(mapOf(), page.evaluate("() => new Set([1, { test: 42n }])"));
  }
}
