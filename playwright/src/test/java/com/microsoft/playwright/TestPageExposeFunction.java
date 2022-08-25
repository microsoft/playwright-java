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

import com.microsoft.playwright.options.BindingCallback;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.microsoft.playwright.Utils.mapOf;
import static com.microsoft.playwright.options.WaitUntilState.LOAD;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class TestPageExposeFunction extends TestBase {

  @Test
  void exposeBindingShouldWork() {
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

  @Test
  void shouldWork() {
    page.exposeFunction("compute", args -> (Integer) args[0] * (Integer) args[1]);
    Object result = page.evaluate("async function() {\n" +
      "  return await window['compute'](9, 4);\n" +
      "}");
    assertEquals(36, result);
  }

  @Test
  void shouldWorkWithHandlesAndComplexObjects() {
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


  @Test
  void shouldThrowExceptionInPageContext() {
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
  @Test
  void shouldBeCallableFromInsideAddInitScript() {
    boolean[] called = { false };
    page.exposeFunction("woof", args -> called[0] = true);
    page.addInitScript("window['woof']()");
    page.reload();
    assertTrue(called[0]);
  }

  @Test
  void shouldSurviveNavigation() {
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
  @Test
  void shouldWorkOnFrames() {
    page.exposeFunction("compute", args -> (Integer) args[0] * (Integer) args[1]);
    page.navigate(server.PREFIX + "/frames/nested-frames.html");
    Frame frame = page.frames().get(1);
    Object result = frame.evaluate("async function() {\n" +
      "  return window['compute'](3, 5);\n" +
      "}");
    assertEquals(15, result);
  }

  @Test
  void shouldWorkOnFramesBeforeNavigation() {
    page.navigate(server.PREFIX + "/frames/nested-frames.html");
    page.exposeFunction("compute", args -> (Integer) args[0] * (Integer) args[1]);

    Frame frame = page.frames().get(1);
    Object result = frame.evaluate("async function() {\n" +
      "  return window['compute'](3, 5);\n" +
      "}");
    assertEquals(15, result);
  }

  @Test
  void shouldWorkAfterCrossOriginNavigation() {
    page.navigate(server.EMPTY_PAGE);
    page.exposeFunction("compute", args -> (Integer) args[0] * (Integer) args[1]);

    page.navigate(server.CROSS_PROCESS_PREFIX + "/empty.html");
    Object result = page.evaluate("window['compute'](9, 4)");
    assertEquals(36, result);
  }

  @Test
  void shouldWorkWithComplexObjects() {
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

  @Test
  void exposeBindingHandleShouldWork() {
    JSHandle[] target = { null };
    page.exposeBinding("logme", (source, args) -> {
      target[0] = (JSHandle) args[0];
      return 17;
    }, new Page.ExposeBindingOptions().setHandle(true));
    Object result = page.evaluate("async function() {\n" +
      "  return window['logme']({ foo: 42 });\n" +
      "}");
    assertEquals(42, target[0].evaluate("x => x.foo"));
    assertEquals(17, result);
  }

  @Test
  void exposeBindingHandleShouldNotThrowDuringNavigation() {
    page.exposeBinding("logme", (source, args) -> {
      return 17;
    }, new Page.ExposeBindingOptions().setHandle(true));
    page.navigate(server.EMPTY_PAGE);

    page.waitForNavigation(new Page.WaitForNavigationOptions().setWaitUntil(LOAD), () -> {
      page.evaluate("async url => {\n" +
        "  window['logme']({ foo: 42 });\n" +
        "  window.location.href = url;\n" +
        "}", server.PREFIX + "/one-style.html");
    });
  }

  @Test
  void shouldThrowForDuplicateRegistrations() {
    page.exposeFunction("foo", args -> null);
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.exposeFunction("foo", args -> null);
    });
    assertTrue(e.getMessage().contains("Function \"foo\" has been already registered"));
  }

  @Test
  void exposeBindingHandleShouldThrowForMultipleArguments() {
    page.exposeBinding("logme", (source, args) -> {
      return 17;
    }, new Page.ExposeBindingOptions().setHandle(true));
    assertEquals(17, page.evaluate("async function() {\n" +
      "  return window['logme']({ foo: 42 });\n" +
      "}"));
    assertEquals(17, page.evaluate("async function() {\n" +
      "  return window['logme']({ foo: 42 }, undefined, undefined);\n" +
      "}"));
    assertEquals(17, page.evaluate("async function() {\n" +
      "  return window['logme'](undefined, undefined, undefined);\n" +
      "}"));
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.evaluate("async function() {\n" +
        "  return window['logme'](1, 2);\n" +
        "}");
    });
    assertTrue(e.getMessage().contains("exposeBindingHandle supports a single argument, 2 received"));
  }

  @Test
  void shouldSerializeCycles() {
    Object[] object = { null };
    page.exposeBinding("log", (source, obj) -> object[0] = obj[0]);
    page.evaluate("const a = {}; a.b = a; window.log(a)");
    Map<String, Object> map = (Map<String, Object>) object[0];
    assertEquals(1, map.size());
    assertTrue(map == map.get("b"));
  }
}

