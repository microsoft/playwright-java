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

import com.microsoft.playwright.fixtures.DisabledOnBrowser;
import com.microsoft.playwright.fixtures.PlaywrightTest;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.*;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;

import static com.microsoft.playwright.Utils.mapOf;
import static com.microsoft.playwright.fixtures.BrowserName.WEBKIT;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.*;

public class TestPageEvaluate {
  @PlaywrightTest
  void shouldWork(Page page) {
    Object result = page.evaluate("() => 7 * 3");
    assertEquals(21, result);
  }

  @PlaywrightTest
  void shouldTransferNaN(Page page) {
    Object result = page.evaluate("a => a", Double.NaN);
    assertTrue(Double.isNaN((Double) result));
  }

  @PlaywrightTest
  void shouldTransfer0(Page page) {
    Object result = page.evaluate("a => a", -0.0);
    assertEquals(Double.NEGATIVE_INFINITY, 1 / (Double) result);
  }

  @PlaywrightTest
  void shouldTransferPositive0(Page page) {
    assertEquals(true, page.evaluate("a => Object.is(a, 0)", 0.0));
    Object result = page.evaluate("a => a", 0.0);
    assertEquals(Double.POSITIVE_INFINITY, 1 / ((Number) result).doubleValue());
  }

  @PlaywrightTest
  void shouldTransferInfinity(Page page) {
    Object result = page.evaluate("a => a", Double.POSITIVE_INFINITY);
    assertTrue(Double.POSITIVE_INFINITY == (Double) result);
  }

  @PlaywrightTest
  void shouldTransferNegativeInfinity(Page page) {
    Object result = page.evaluate("a => a", Double.NEGATIVE_INFINITY);
    assertTrue(Double.NEGATIVE_INFINITY == (Double) result);
  }

  @PlaywrightTest
  void shouldRoundtripUnserializableValues(Page page) {
    Map<String, ?> value = mapOf(
      "infinity", Double.POSITIVE_INFINITY,
      "nInfinity", Double.NEGATIVE_INFINITY,
      "nZero", -0.0,
      "nan", Double.NaN
    );
    Object result = page.evaluate("value => value", value);
    assertEquals(value, result);
  }

  @PlaywrightTest
  void shouldRoundtripPromiseToValue(Page page) {
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

  @PlaywrightTest
  void shouldRoundtripPromiseToUnserializableValues(Page page) {
    Map<String, ?> value = mapOf(
      "infinity", Double.POSITIVE_INFINITY,
      "nInfinity", Double.NEGATIVE_INFINITY,
      "nZero", -0.0,
      "nan", Double.NaN
    );
    Object result = page.evaluate("value => Promise.resolve(value)", value);
    assertEquals(value, result);
  }

  @PlaywrightTest
  void shouldTransferArrays(Page page) {
    Object result = page.evaluate("a => a", asList(1, 2, 3));
    assertEquals(asList(1, 2, 3), result);
  }

  @PlaywrightTest
  void shouldTransferArraysAsArraysNotObjects(Page page) {
    Object result = page.evaluate("a => Array.isArray(a)", asList(1, 2, 3));
    assertEquals(true, result);
  }

  @PlaywrightTest
  void shouldTransferBigint(Page page) {
    assertEquals(new BigInteger("42", 10), page.evaluate("() => 42n"));
    assertEquals(new BigInteger("17", 10), page.evaluate("a => a", new BigInteger("17", 10)));
  }

  //  @PlaywrightTest
  void shouldTransferMapsAsEmptyObjects() {
    // Not applicable.
  }

  @PlaywrightTest
  void shouldModifyGlobalEnvironment(Page page) {
    page.evaluate("() => window['globalVar'] = 123");
    assertEquals(123, page.evaluate("globalVar"));
  }

  @PlaywrightTest
  void shouldEvaluateInThePageContext(Page page, Server server) {
    page.navigate(server.PREFIX + "/global-var.html");
    assertEquals(123, page.evaluate("globalVar"));
  }

  @PlaywrightTest
  void shouldReturnUndefinedForObjectsWithSymbols(Page page) {
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

  @PlaywrightTest
  void shouldWorkWithUnicodeChars(Page page) {
    Object result = page.evaluate("a => a['中文字符']", mapOf("中文字符", 42));
    assertEquals(42, result);
  }

  @PlaywrightTest
  void shouldThrowWhenEvaluationTriggersReload(Page page) {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.evaluate("() => {\n" +
        "  location.reload();\n" +
        "  return new Promise(() => { });\n" +
        "}");
    });
    assertTrue(e.getMessage().contains("navigation"));
  }

  @PlaywrightTest
  void shouldAwaitPromise(Page page) {
    Object result = page.evaluate("() => Promise.resolve(8 * 7)");
    assertEquals(56, result);
  }

  @PlaywrightTest
  void shouldWorkRightAfterFramenavigated(Page page, Server server) {
    Object[] frameEvaluation = {null};
    page.onFrameNavigated(frame -> {
      frameEvaluation[0] = frame.evaluate("() => 6 * 7");
    });
    page.navigate(server.EMPTY_PAGE);
    assertEquals(42, frameEvaluation[0]);
  }

  @PlaywrightTest
  void shouldWorkRightAfterACrossOriginNavigation(Page page, Server server) {
    page.navigate(server.EMPTY_PAGE);
    Object[] frameEvaluation = {null};
    page.onFrameNavigated(frame -> {
      frameEvaluation[0] = frame.evaluate("() => 6 * 7");
    });
    page.navigate(server.CROSS_PROCESS_PREFIX + "/empty.html");
    assertEquals(42, frameEvaluation[0]);
  }

  @PlaywrightTest
  void shouldWorkFromInsideAnExposedFunction(Page page) {
    // Setup inpage callback, which calls Page.evaluate
    page.exposeFunction("callController", args -> page.evaluate("({ a, b }) => a * b",
      mapOf("a", args[0], "b", args[1])));
    Object result = page.evaluate("async function() {\n" +
      "  return await window['callController'](9, 3);\n" +
      "}");
    assertEquals(27, result);
  }

  @PlaywrightTest
  void shouldRejectPromiseWithException(Page page) {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.evaluate("() => not_existing_object.property"));
    assertTrue(e.getMessage().contains("not_existing_object"));
  }

  @PlaywrightTest
  void shouldSupportThrownStringsAsErrorMessages(Page page) {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.evaluate("() => { throw 'qwerty'; }"));
    assertTrue(e.getMessage().contains("qwerty"));
  }

  @PlaywrightTest
  void shouldSupportThrownNumbersAsErrorMessages(Page page) {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.evaluate("() => { throw 100500; }"));
    assertTrue(e.getMessage().contains("100500"));
  }

  @PlaywrightTest
  void shouldReturnComplexObjects(Page page) {
    Map<String, ?> object = mapOf("foo", "bar!");
    Object result = page.evaluate("a => a", object);
    assertFalse(object == result);
    assertEquals(object, result);
  }

  @PlaywrightTest
  void shouldReturnNaN(Page page) {
    Object result = page.evaluate("() => NaN");
    assertEquals(Double.NaN, result);
  }

  @PlaywrightTest
  void shouldReturn0(Page page) {
    Object result = page.evaluate("() => -0");
    assertEquals(Double.NEGATIVE_INFINITY, 1 / (Double) result);
  }

  @PlaywrightTest
  void shouldReturnInfinity(Page page) {
    Object result = page.evaluate("() => Infinity");
    assertEquals(Double.POSITIVE_INFINITY, result);
  }

  @PlaywrightTest
  void shouldReturnNegativeInfinity(Page page) {
    Object result = page.evaluate("() => -Infinity");
    assertEquals(Double.NEGATIVE_INFINITY, result);
  }

  @PlaywrightTest
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

  @PlaywrightTest
  void shouldProperlySerializeUndefinedArguments() {
    // Not applicable
  }

  @PlaywrightTest
  void shouldProperlySerializeUndefinedFields(Page page) {
    assertEquals(mapOf("a", null), page.evaluate("() => ({ a: undefined })"));
  }

  @PlaywrightTest
  void shouldReturnUndefinedProperties(Page page) {
    Object value = page.evaluate("() => ({ a: undefined })");
    assertEquals(mapOf("a", null), value);
  }

  @PlaywrightTest
  void shouldProperlySerializeNullArguments(Page page) {
    assertEquals(null, page.evaluate("x => x", null));
  }

  @PlaywrightTest
  void shouldProperlySerializeNullFields(Page page) {
    assertEquals(mapOf("a", null), page.evaluate("() => ({ a: null })"));
  }

  @PlaywrightTest
  void shouldReturnUndefinedForNonSerializableObjects(Page page) {
    assertEquals(null, page.evaluate("() => () => {}"));
    assertEquals("ref: <Window>", page.evaluate("() => window"));
  }

  @PlaywrightTest
  void shouldBeAbleToThrowATrickyError(Page page) {
    String errorText = "My error";
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.evaluate("errorText => {\n" +
        "  throw new Error(errorText);\n" +
        "}", errorText);
    });
    assertTrue(e.getMessage().contains(errorText));
  }

  @PlaywrightTest
  void shouldAcceptAString(Page page) {
    Object result = page.evaluate("1 + 2");
    assertEquals(3, result);
  }

  @PlaywrightTest
  void shouldAcceptAStringWithSemiColons(Page page) {
    Object result = page.evaluate("1 + 5;");
    assertEquals(6, result);
  }

  @PlaywrightTest
  void shouldAcceptAStringWithComments(Page page) {
    Object result = page.evaluate("2 + 5;\n// do some math!");
    assertEquals(7, result);
  }

  @PlaywrightTest
  void shouldAcceptElementHandleAsAnArgument(Page page) {
    page.setContent("<section>42</section>");
    ElementHandle element = page.querySelector("section");
    Object text = page.evaluate("e => e.textContent", element);
    assertEquals("42", text);
  }

  @PlaywrightTest
  void shouldThrowIfUnderlyingElementWasDisposed(Page page) {
    page.setContent("<section>39</section>");
    ElementHandle element = page.querySelector("section");
    assertNotNull(element);
    element.dispose();
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.evaluate("e => e.textContent", element));
    assertTrue(e.getMessage().contains("no object with guid"), e.getMessage());
  }

  @PlaywrightTest
  void shouldSimulateAUserGesture(Page page) {
    assertEquals(true, page.evaluate("() => {\n" +
      "  document.body.appendChild(document.createTextNode('test'));\n" +
      "  document.execCommand('selectAll');\n" +
      "  return document.execCommand('copy');\n" +
      "}"));
  }

  @PlaywrightTest
  void shouldThrowANiceErrorAfterANavigation(Page page) {
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

  @PlaywrightTest
  void shouldNotThrowAnErrorWhenEvaluationDoesANavigation(Page page, Server server) {
    page.navigate(server.PREFIX + "/one-style.html");
    Object result = page.evaluate("() => {\n" +
      "  window.location.href = '/empty.html';\n" +
      "  return [42];\n" +
      "}");
    assertEquals(asList(42), result);
  }

  @PlaywrightTest
  @DisabledOnBrowser(value = WEBKIT, reason = "fixme")
  void shouldNotThrowAnErrorWhenEvaluationDoesASynchronousNavigationAndReturnsAnObject(Page page) {
    // It is imporant to be on about:blank for sync reload.
    Object result = page.evaluate("() => {\n" +
      "  window.location.reload();\n" +
      "  return { a: 42 };\n" +
      "}");
    assertEquals(mapOf("a", 42), result);
  }

  @PlaywrightTest
  void shouldNotThrowAnErrorWhenEvaluationDoesASynchronousNavigationAndReturnsUndefined(Page page) {
    // It is imporant to be on about:blank for sync reload.
    Object result = page.evaluate("() => {\n" +
      "  window.location.reload();\n" +
      "  return undefined;\n" +
      "}");
    assertEquals(null, result);
  }

  @PlaywrightTest
  void shouldTransfer100MbOfDataFromPageToNodeJs(Page page) {
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

  @PlaywrightTest
  void shouldThrowErrorWithDetailedInformationOnExceptionInsidePromise(Page page) {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.evaluate("() => new Promise(() => {\n" +
        "  throw new Error('Error in promise');\n" +
        "})");
    });
    assertTrue(e.getMessage().contains("Error in promise"));
  }

  @PlaywrightTest
  void shouldWorkEvenWhenJSONIsSetToNull(Page page) {
    page.evaluate("() => { window.JSON.stringify = null; window.JSON = null; }");
    Object result = page.evaluate("() => ({ abc: 123 })");
    assertEquals(mapOf("abc", 123), result);
  }

  @PlaywrightTest
  void shouldAwaitPromiseFromPopup(Page page, Server server) {
    // Something is wrong about the way Firefox waits for the chained promise
    page.navigate(server.EMPTY_PAGE);
    Object result = page.evaluate("() => {\n" +
      "  const win = window.open('about:blank');\n" +
      "  return new win['Promise'](f => f(42));\n" +
      "}");
    assertEquals(42, result);
  }

  @PlaywrightTest
  void shouldWorkWithNewFunctionAndCSP(Page page, Server server) {
    server.setCSP("/empty.html", "script-src " + server.PREFIX);
    page.navigate(server.PREFIX + "/empty.html");
    assertEquals(true, page.evaluate("() => new Function('return true')()"));
  }

  @PlaywrightTest
  void shouldWorkWithNonStrictExpressions(Page page) {
    assertEquals(3.14, page.evaluate("() => {\n" +
      "  y = 3.14;\n" +
      "  return y;\n" +
      "}"));
  }

  @PlaywrightTest
  void shouldRespectUseStrictExpression(Page page) {
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

  @PlaywrightTest
  void shouldNotLeakUtilityScript(Page page) {
    assertEquals(true, page.evaluate("() => this === window"));
  }

  @PlaywrightTest
  void shouldNotLeakHandles(Page page) {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.evaluate("handles.length"));
    assertTrue(e.getMessage().contains(" handles"));
  }

  @PlaywrightTest
  void shouldWorkWithCSP(Page page, Server server) {
    server.setCSP("/empty.html", "script-src 'self'");
    page.navigate(server.EMPTY_PAGE);
    assertEquals(4, page.evaluate("() => 2 + 2"));
  }

  @PlaywrightTest
  void shouldEvaluateException(Page page) {
    Exception result = (Exception) page.evaluate("() => {\n" +
      "  return (function functionOnStack() {\n" +
      "    return new Error('error message');\n" +
      "  })();\n" +
      "}");
    assertTrue(result.getMessage().contains("Error: error message"), result.getMessage());
    assertTrue(result.getMessage().contains("functionOnStack"), result.getMessage());
  }

  @PlaywrightTest
  void shouldEvaluateException2(Page page) {
    Exception error = (Exception) page.evaluate("new Error('error message')");
    assertTrue(error.getMessage().contains("Error: error message"), error.getMessage());
  }

  @PlaywrightTest
  void shouldPassExceptionArgument(Page page) {
    Exception e = new Exception("error message");
    Map<String, Object> received = (Map<String, Object>) page.evaluate("e => { return { message: e.message, name: e.name, stack: e.stack }; }", e);

    assertEquals("error message", received.get("message"));
    assertEquals("Exception", received.get("name"));
    assertNotNull(received.get("stack"));
    assertTrue(received.get("stack").toString().contains("shouldPassExceptionArgument"), received.get("stack").toString());
  }

  @PlaywrightTest
  void shouldEvaluateDate(Page page) {
    Object result = page.evaluate("() => ({ date: new Date('2020-05-27T01:31:38.506Z') })");
    Date expected = Date.from(ZonedDateTime.parse("2020-05-27T01:31:38.506Z").toInstant());
    assertEquals(mapOf("date", expected), result);
  }

  @PlaywrightTest
  void shouldRoundtripDate(Page page) {
    Date date = Date.from(ZonedDateTime.parse("2020-05-27T01:31:38.506Z").toInstant());
    Object result = page.evaluate("date => date", date);
    assertTrue(result instanceof Date);
    assertEquals(date.toString(), result.toString());
  }

  @PlaywrightTest
  void shouldRoundtripRegex(Page page) {
    Pattern regex = Pattern.compile("hello", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    Object result = page.evaluate("regex => regex", regex);
    assertTrue(result instanceof Pattern);
    assertEquals(regex.toString(), result.toString());
    assertEquals(regex.flags(), ((Pattern)result).flags());
  }

  @PlaywrightTest
  void shouldJsonValueDate(Page page) {
    JSHandle resultHandle = page.evaluateHandle("() => ({ date: new Date('2020-05-27T01:31:38.506Z') })");
    assertEquals(mapOf("date", Date.from(ZonedDateTime.parse("2020-05-27T01:31:38.506Z").toInstant())), resultHandle.jsonValue());
  }

  @PlaywrightTest
  void shouldEvaluateUrl(Page page) throws MalformedURLException {
    Object result = page.evaluate("() => ({ url: new URL('https://example.com/') })");
    assertEquals(mapOf("url", new URL("https://example.com/")), result);
  }

  @PlaywrightTest
  void shouldRoundtripUrl(Page page) throws MalformedURLException {
    URL url = new URL("https://example.com/");
    Object result = page.evaluate("url => url", url);
    assertTrue(result instanceof URL);
    assertEquals(url.toString(), result.toString());
  }

  @PlaywrightTest
  void shouldRoundtripComplexUrl(Page page) throws MalformedURLException {
    URL url = new URL("https://user:password@www.contoso.com:80/Home/Index.htm?q1=v1&q2=v2#FragmentName");
    Object result = page.evaluate("url => url", url);
    assertTrue(result instanceof URL);
    assertEquals(url.toString(), result.toString());
  }

  @PlaywrightTest
  void shouldNotUseToJSONWhenEvaluating(Page page) {
    Object result = page.evaluate("() => ({ toJSON: () => 'string', data: 'data' })");
    assertEquals(mapOf("data", "data", "toJSON", emptyMap()), result);
  }

  @PlaywrightTest
  void shouldNotUseToJSONInJsonValue(Page page) {
    JSHandle resultHandle = page.evaluateHandle("() => ({ toJSON: () => 'string', data: 'data' })");
    assertEquals(mapOf("data", "data", "toJSON", emptyMap()), resultHandle.jsonValue());
  }

  @PlaywrightTest
  void shouldAliasWindowDocumentAndNode(Page page) {
    Object object = page.evaluate("[window, document, document.body]");
    assertEquals(asList("ref: <Window>", "ref: <Document>", "ref: <Node>"), object);
  }

  @PlaywrightTest
  void shouldWorkForCircularObject(Page page) {
    Object result = page.evaluate("() => {\n" +
      "    const a = {};\n" +
      "    a.b = a;\n" +
      "    return a;\n" +
      "  }");

    Map<String, Object> map = (Map<String, Object>) result;
    assertEquals(1, map.size());
    assertTrue(map == map.get("b"));
  }

  @PlaywrightTest
  void shouldAcceptParameter(Page page) {
    Instant instant = Instant.now();
    LocalDateTime localDateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
    Object object = page.evaluate("p => p", localDateTime);
    assertTrue(object instanceof Date);
    assertEquals(Date.from(instant), object);
  }

  @PlaywrightTest
  void shouldTransferMaps(Page page) {
    assertEquals(mapOf(), page.evaluate("() => new Map([[1, { test: 42n }]])"));
  }

  @PlaywrightTest
  void shouldTransferSets(Page page) {
    assertEquals(mapOf(), page.evaluate("() => new Set([1, { test: 42n }])"));
  }
}
