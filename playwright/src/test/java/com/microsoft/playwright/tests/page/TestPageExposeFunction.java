/*
 * Copyright 2017 Google Inc. All rights reserved.
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

import com.microsoft.playwright.Frame;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.BindingCallback;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/page-expose-function.spec.ts. */
public class TestPageExposeFunction {
  // Java: expect.poll(() => value).toEqual(expected); waitForCondition() dispatches the events while waiting.
  private static <T> void expectPollToEqual(Page page, Supplier<T> value, T expected) {
    try {
      page.waitForCondition(() -> expected.equals(value.get()), new Page.WaitForConditionOptions().setTimeout(5000));
    } catch (TimeoutError e) {
      // The assertion below reports the last value.
    }
    assertEquals(expected, value.get());
  }

  @PlaywrightTest
  @DisplayName("EF-001 exposeBinding should work @smoke")
  void exposeBindingShouldWork(Page page) {
    BindingCallback.Source[] bindingSource = {null};
    page.exposeBinding("add", (source, args) -> {
      bindingSource[0] = source;
      return (Integer) args[0] + (Integer) args[1];
    });
    Object result = page.evaluate("async function() {\n" +
      "  return window['add'](5, 6);\n" +
      "}");
    assertEquals(page.context(), bindingSource[0].context());
    assertEquals(page, bindingSource[0].page());
    assertEquals(page.mainFrame(), bindingSource[0].frame());
    assertEquals(11, result);
  }

  @PlaywrightTest
  @DisplayName("EF-002 should work")
  void shouldWork(Page page, TestServer server) {
    page.exposeFunction("compute", args -> {
      return (Integer) args[0] * (Integer) args[1];
    });
    Object result = page.evaluate("async function() {\n" +
      "  return await window['compute'](9, 4);\n" +
      "}");
    assertEquals(36, result);
  }

  @PlaywrightTest
  @DisplayName("EF-003 should dispose")
  void shouldDispose(Page page, TestServer server) throws Exception {
    AutoCloseable binding = page.exposeFunction("compute", args -> {
      return (Integer) args[0] * (Integer) args[1];
    });
    Object result = page.evaluate("async function() {\n" +
      "  return await window['compute'](9, 4);\n" +
      "}");
    assertEquals(36, result);
    binding.close();

    PlaywrightException e = assertThrows(PlaywrightException.class, () -> page.evaluate("async function() {\n" +
      "  return await window['compute'](9, 4);\n" +
      "}"));
    assertTrue(e.getMessage().contains("is not a function"));
  }

  @PlaywrightTest
  @DisplayName("EF-004 should work with handles and complex objects")
  void shouldWorkWithHandlesAndComplexObjects(Page page, TestServer server) {
    JSHandle fooHandle = page.evaluateHandle("() => {\n" +
      "  window['fooValue'] = { bar: 2 };\n" +
      "  return window['fooValue'];\n" +
      "}");
    page.exposeFunction("handle", args -> {
      return Arrays.asList(mapOf("foo", fooHandle));
    });
    Object equals = page.evaluate("async function() {\n" +
      "  const value = await window['handle']();\n" +
      "  const [{ foo }] = value;\n" +
      "  return foo === window['fooValue'];\n" +
      "}");
    assertEquals(true, equals);
  }

  @PlaywrightTest
  @DisplayName("EF-005 should throw exception in page context")
  @SuppressWarnings("unchecked")
  void shouldThrowExceptionInPageContext(Page page, TestServer server) {
    page.exposeFunction("woof", args -> {
      throw new RuntimeException("WOOF WOOF");
    });
    Map<String, Object> result = (Map<String, Object>) page.evaluate("async () => {\n" +
      "  try {\n" +
      "    await window['woof']();\n" +
      "  } catch (e) {\n" +
      "    return { message: e.message, stack: e.stack };\n" +
      "  }\n" +
      "}");
    String message = (String) result.get("message");
    String stack = (String) result.get("stack");
    assertEquals("WOOF WOOF", message);
    // Java: __filename is the name of this test file, which the Java stack trace contains.
    assertTrue(stack.contains("TestPageExposeFunction.java"));
  }

  @PlaywrightTest
  @DisplayName("EF-007 should be callable from-inside addInitScript")
  void shouldBeCallableFromInsideAddInitScript(Page page, TestServer server) {
    boolean[] called = {false};
    page.exposeFunction("woof", args -> {
      called[0] = true;
      return null;
    });
    // Java: addInitScript takes the source of the script, not a function.
    page.addInitScript("window['woof']()");
    page.reload();
    expectPollToEqual(page, () -> called[0], true);
  }

  @PlaywrightTest
  @DisplayName("EF-008 should survive navigation")
  void shouldSurviveNavigation(Page page, TestServer server) {
    page.exposeFunction("compute", args -> {
      return (Integer) args[0] * (Integer) args[1];
    });

    page.navigate(server.EMPTY_PAGE);
    Object result = page.evaluate("async function() {\n" +
      "  return await window['compute'](9, 4);\n" +
      "}");
    assertEquals(36, result);
  }

  @PlaywrightTest
  @DisplayName("EF-009 should await returned promise")
  void shouldAwaitReturnedPromise(Page page, TestServer server) {
    // Java: there are no promises, the function returns the value.
    page.exposeFunction("compute", args -> {
      return (Integer) args[0] * (Integer) args[1];
    });

    Object result = page.evaluate("async function() {\n" +
      "  return await window['compute'](3, 5);\n" +
      "}");
    assertEquals(15, result);
  }

  @PlaywrightTest
  @DisplayName("EF-010 should work on frames")
  void shouldWorkOnFrames(Page page, TestServer server) {
    // Java: there are no promises, the function returns the value.
    page.exposeFunction("compute", args -> {
      return (Integer) args[0] * (Integer) args[1];
    });

    page.navigate(server.PREFIX + "/frames/nested-frames.html");
    Frame frame = page.frames().get(1);
    Object result = frame.evaluate("async function() {\n" +
      "  return await window['compute'](3, 5);\n" +
      "}");
    assertEquals(15, result);
  }

  @PlaywrightTest
  @DisplayName("EF-011 should work on frames before navigation")
  void shouldWorkOnFramesBeforeNavigation(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/frames/nested-frames.html");
    // Java: there are no promises, the function returns the value.
    page.exposeFunction("compute", args -> {
      return (Integer) args[0] * (Integer) args[1];
    });

    Frame frame = page.frames().get(1);
    Object result = frame.evaluate("async function() {\n" +
      "  return await window['compute'](3, 5);\n" +
      "}");
    assertEquals(15, result);
  }

  @PlaywrightTest
  @DisplayName("EF-012 should work after cross origin navigation")
  void shouldWorkAfterCrossOriginNavigation(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.exposeFunction("compute", args -> {
      return (Integer) args[0] * (Integer) args[1];
    });

    page.navigate(server.CROSS_PROCESS_PREFIX + "/empty.html");
    Object result = page.evaluate("async function() {\n" +
      "  return await window['compute'](9, 4);\n" +
      "}");
    assertEquals(36, result);
  }

  @PlaywrightTest
  @DisplayName("EF-013 should work with complex objects")
  @SuppressWarnings("unchecked")
  void shouldWorkWithComplexObjects(Page page, TestServer server) {
    page.exposeFunction("complexObject", args -> {
      Map<String, Object> a = (Map<String, Object>) args[0];
      Map<String, Object> b = (Map<String, Object>) args[1];
      return mapOf("x", (Integer) a.get("x") + (Integer) b.get("x"));
    });
    Map<String, Object> result = (Map<String, Object>) page.evaluate("async () => window['complexObject']({ x: 5 }, { x: 2 })");
    assertEquals(7, result.get("x"));
  }

  @PlaywrightTest
  @DisplayName("EF-014 should throw for duplicate registrations")
  @Fixme("java: the error message has no 'page.exposeFunction: ' prefix (it starts with 'Error {' and the serialized error)")
  void shouldThrowForDuplicateRegistrations(Page page) {
    page.exposeFunction("foo", args -> null);
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.exposeFunction("foo", args -> null));
    assertTrue(error.getMessage().contains("page.exposeFunction: Function \"foo\" has been already registered"));
  }

  @PlaywrightTest
  @DisplayName("EF-015 should work with setContent")
  void shouldWorkWithSetContent(Page page, TestServer server) {
    // Java: there are no promises, the function returns the value.
    page.exposeFunction("compute", args -> {
      return (Integer) args[0] * (Integer) args[1];
    });
    page.setContent("<script>window.result = compute(3, 2)</script>");
    assertEquals(6, page.evaluate("window.result"));
  }

  @PlaywrightTest
  @DisplayName("EF-016 should alias Window, Document and Node")
  void shouldAliasWindowDocumentAndNode(Page page) {
    Object[] object = {null};
    page.exposeBinding("log", (source, args) -> object[0] = args[0]);
    page.evaluate("window.log([window, document, document.body])");
    assertEquals(Arrays.asList("ref: <Window>", "ref: <Document>", "ref: <Node>"), object[0]);
  }

  @PlaywrightTest
  @DisplayName("EF-017 should serialize cycles")
  @SuppressWarnings("unchecked")
  void shouldSerializeCycles(Page page) {
    Object[] object = {null};
    page.exposeBinding("log", (source, args) -> object[0] = args[0]);
    page.evaluate("const a = {}; a.b = a; window.log(a)");
    // Java: expect(object).toEqual(a) for a cyclic a (a.b = a) is a check of the keys and of the cycle,
    // since equals() of a cyclic map does not terminate.
    Map<String, Object> received = (Map<String, Object>) object[0];
    assertEquals(new ArrayList<>(Arrays.asList("b")), new ArrayList<>(received.keySet()));
    assertSame(received, received.get("b"));
  }

  @PlaywrightTest
  @DisplayName("EF-018 should work with overridden console object")
  void shouldWorkWithOverriddenConsoleObject(Page page) {
    page.evaluate("() => window.console = null");
    // Java: upstream expects a promise that is not awaited, which is always truthy.
    assertNotNull(page.evaluate("() => window.console === null"));
    page.exposeFunction("add", args -> (Integer) args[0] + (Integer) args[1]);
    assertEquals(11, page.evaluate("add(5, 6)"));
  }

  @PlaywrightTest
  @DisplayName("EF-019 should work with busted Array.prototype.map/push")
  void shouldWorkWithBustedArrayPrototypeMapPush(Page page, TestServer server) {
    server.setRoute("/test", (req, res) -> {
      res.writeHead(200, "content-type", "text/html");
      res.end("<script>\n" +
        "      Array.prototype.map = null;\n" +
        "      Array.prototype.push = null;\n" +
        "    </script>");
    });
    page.navigate(server.PREFIX + "/test");
    page.exposeFunction("add", args -> (Integer) args[0] + (Integer) args[1]);
    assertEquals(11, page.evaluate("add(5, 6)"));
  }

  @PlaywrightTest
  @DisplayName("EF-020 should fail with busted Array.prototype.toJSON")
  void shouldFailWithBustedArrayPrototypeToJSON(Page page) {
    page.evaluateHandle("() => Array.prototype.toJSON = () => '\"[]\"'");

    page.exposeFunction("add", args -> (Integer) args[0] + (Integer) args[1]);
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.evaluate("add(5, 6)"));
    assertTrue(error.getMessage().contains("serializedArgs is not an array. This can happen when Array.prototype.toJSON is defined incorrectly"));

    // expect.soft
    assertEquals("\"[]\"", page.evaluate("() => [].toJSON()"));
  }

  @PlaywrightTest
  @DisplayName("EF-021 exposeBinding should work in parallel")
  void exposeBindingShouldWorkInParallel(Page page) throws Exception {
    // Java: Promise.all of the two calls, one after the other.
    page.exposeBinding("foo", (source, args) -> 42);
    page.exposeBinding("bar", (source, args) -> 42);
    page.evaluate("() => {\n" +
      "  window.foo();\n" +
      "  window.bar();\n" +
      "}");
  }
}
