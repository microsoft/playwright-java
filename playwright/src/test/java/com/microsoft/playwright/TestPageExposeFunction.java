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
import com.microsoft.playwright.options.BindingCallback;

import java.util.Map;

import static com.microsoft.playwright.Utils.mapOf;
import static com.microsoft.playwright.options.WaitUntilState.LOAD;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class TestPageExposeFunction {

  @PlaywrightTest
  void exposeBindingShouldWork(Browser browser) {
    BrowserContext context = browser.newContext();
    Page page = context.newPage();
    BindingCallback.Source[] bindingSource = { null };
    page.exposeBinding("add", (source, args) -> {
      bindingSource[0] = source;
      return (Integer) args[0] + (Integer) args[1];
    });
    Object result = page.evaluate("async function() {\n" +
      "  return window['add'](5, 6);\n" +
      "}");
    assertEquals(context, bindingSource[0].context());
    assertEquals(page, bindingSource[0].page());
    assertEquals(page.mainFrame(), bindingSource[0].frame());
    assertEquals(11, result);
    context.close();
  }

  @PlaywrightTest
  void shouldWork(Page page) {
    page.exposeFunction("compute", args -> (Integer) args[0] * (Integer) args[1]);
    Object result = page.evaluate("async function() {\n" +
      "  return await window['compute'](9, 4);\n" +
      "}");
    assertEquals(36, result);
  }

  @PlaywrightTest
  void shouldWorkWithHandlesAndComplexObjects(Page page) {
   JSHandle fooHandle = page.evaluateHandle("() => {\n" +
     "  window['fooValue'] = { bar: 2 };\n" +
     "  return window['fooValue'];\n" +
     "}");
    page.exposeFunction("handle", args -> asList(mapOf("foo", fooHandle)));
    Object equals = page.evaluate("async function() {\n" +
      "  const value = await window['handle']();\n" +
      "  const [{ foo }] = value;\n" +
      "  return foo === window['fooValue'];\n" +
      "}");
    assertEquals(true, equals);
  }


  @PlaywrightTest
  void shouldThrowExceptionInPageContext(Page page) {
    page.exposeFunction("woof", args -> {
      throw new RuntimeException("WOOF WOOF");
    });
    Object result = page.evaluate("async () => {\n" +
      "  try {\n" +
      "    await window[\"woof\"]();\n" +
      "  } catch (e) {\n" +
      "    return {message: e.message, stack: e.stack};\n" +
      "  }\n" +
      "}");
    assertTrue(result instanceof Map);
    @SuppressWarnings("unchecked")
    Map<String, String> m = (Map<String, String>) result;
    assertEquals("WOOF WOOF", m.get("message"));
    assertTrue(m.get("stack").contains("shouldThrowExceptionInPageContext"));
  }

  void shouldSupportThrowingNull() {
    // Throwing null would lead to NullPointerException in Java.
  }
  @PlaywrightTest
  void shouldBeCallableFromInsideAddInitScript(Page page) {
    boolean[] called = { false };
    page.exposeFunction("woof", args -> called[0] = true);
    page.addInitScript("window['woof']()");
    page.reload();
    assertTrue(called[0]);
  }

  @PlaywrightTest
  void shouldSurviveNavigation(Page page, Server server) {
    page.exposeFunction("compute", args -> (Integer) args[0] * (Integer) args[1]);
    Object result = page.evaluate("async function() {\n" +
      "  return await window['compute'](9, 4);\n" +
      "}");
    page.navigate(server.EMPTY_PAGE);
    assertEquals(36, result);
  }

  void shouldAwaitReturnedPromise() {
    // Java callback cannot return promise.
  }
  @PlaywrightTest
  void shouldWorkOnFrames(Page page, Server server) {
    page.exposeFunction("compute", args -> (Integer) args[0] * (Integer) args[1]);
    page.navigate(server.PREFIX + "/frames/nested-frames.html");
    Frame frame = page.frames().get(1);
    Object result = frame.evaluate("async function() {\n" +
      "  return window['compute'](3, 5);\n" +
      "}");
    assertEquals(15, result);
  }

  @PlaywrightTest
  void shouldWorkOnFramesBeforeNavigation(Page page, Server server) {
    page.navigate(server.PREFIX + "/frames/nested-frames.html");
    page.exposeFunction("compute", args -> (Integer) args[0] * (Integer) args[1]);

    Frame frame = page.frames().get(1);
    Object result = frame.evaluate("async function() {\n" +
      "  return window['compute'](3, 5);\n" +
      "}");
    assertEquals(15, result);
  }

  @PlaywrightTest
  void shouldWorkAfterCrossOriginNavigation(Page page, Server server) {
    page.navigate(server.EMPTY_PAGE);
    page.exposeFunction("compute", args -> (Integer) args[0] * (Integer) args[1]);

    page.navigate(server.CROSS_PROCESS_PREFIX + "/empty.html");
    Object result = page.evaluate("window['compute'](9, 4)");
    assertEquals(36, result);
  }

  @PlaywrightTest
  void shouldWorkWithComplexObjects(Page page) {
    page.exposeFunction("complexObject", args -> {
      @SuppressWarnings("unchecked")
      Map<String, Integer> a = (Map<String, Integer>) args[0];
      @SuppressWarnings("unchecked")
      Map<String, Integer> b = (Map<String, Integer>) args[1];
      int sum = a.get("x") + b.get("x");
      return mapOf("x", sum);
    });
    Object result = page.evaluate("async () => window['complexObject']({x: 5}, {x: 2})");
    assertTrue(result instanceof Map);
    assertEquals( 7, ((Map) result).get("x"));
  }

  @PlaywrightTest
  void shouldThrowForDuplicateRegistrations(Page page) {
    page.exposeFunction("foo", args -> null);
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.exposeFunction("foo", args -> null);
    });
    assertTrue(e.getMessage().contains("Function \"foo\" has been already registered"));
  }

  @PlaywrightTest
  void shouldSerializeCycles(Page page) {
    Object[] object = { null };
    page.exposeBinding("log", (source, obj) -> object[0] = obj[0]);
    page.evaluate("const a = {}; a.b = a; window.log(a)");
    Map<String, Object> map = (Map<String, Object>) object[0];
    assertEquals(1, map.size());
    assertTrue(map == map.get("b"));
  }
}

