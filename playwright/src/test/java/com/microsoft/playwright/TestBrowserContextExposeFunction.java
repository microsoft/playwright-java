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

import java.util.ArrayList;
import java.util.List;

import static com.microsoft.playwright.Utils.mapOf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

public class TestBrowserContextExposeFunction extends TestBase {

  @Test
  void exposeBindingShouldWork() {
    BindingCallback.Source[] bindingSource = {null};
    context.exposeBinding("add", (source, args) -> {
      bindingSource[0] = source;
      return (Integer) args[0] + (Integer) args[1];
    });
    Page page = context.newPage();
    Object result = page.evaluate("add(5, 6)");
    assertEquals(context, bindingSource[0].context());
    assertEquals(page, bindingSource[0].page());
    assertEquals(page.mainFrame(), bindingSource[0].frame());
    assertEquals(11, result);
  }

  @Test
  void shouldWork() {
    context.exposeFunction("add", args -> (Integer) args[0] + (Integer) args[1]);
    Page page = context.newPage();
    page.exposeFunction("mul", args -> (Integer) args[0] * (Integer) args[1]);
    context.exposeFunction("sub", args -> (Integer) args[0] - (Integer) args[1]);
    context.exposeBinding("addHandle", (source, args) -> source.frame().evaluateHandle("([a, b]) => a + b", args));
    Object result = page.evaluate("async () => ({ mul: await mul(9, 4), add: await add(9, 4), sub: await sub(9, 4), addHandle: await addHandle(5, 6) })");
    assertEquals(mapOf("mul", 36, "add", 13, "sub", 5, "addHandle", 11), result);
  }

  @Test
  void shouldThrowForDuplicateRegistrations() {
    context.exposeFunction("foo", args -> null);
    context.exposeFunction("bar", args -> null);
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> context.exposeFunction("foo", args -> null));
    assertTrue(e.getMessage().contains("Function \"foo\" has been already registered"));

    Page page = context.newPage();
    e = assertThrows(PlaywrightException.class, () -> page.exposeFunction("foo", args -> null));
    assertTrue(e.getMessage().contains("Function \"foo\" has been already registered in the browser context"));

    page.exposeFunction("baz", args -> null);
    e = assertThrows(PlaywrightException.class, () -> context.exposeFunction("baz", args -> null));
    assertTrue(e.getMessage().contains("Function \"baz\" has been already registered in one of the pages"));
  }

  @Test
  void shouldBeCallableFromInsideAddInitScript() {
    List<Object> actualArgs = new ArrayList<>();
    context.exposeFunction("woof", args -> actualArgs.add(args[0]));
    context.addInitScript("window['woof']('context')");
    Page page = context.newPage();
    page.evaluate("undefined");
    assertEquals(asList("context"), actualArgs);
    actualArgs.clear();
    page.addInitScript("window['woof']('page')");
    page.reload();
    assertEquals(asList("context", "page"), actualArgs);
  }

  @Test
  void exposeBindingHandleShouldWork() {
    JSHandle[] target = { null };
    context.exposeBinding("logme", (source, args) -> {
      target[0] = (JSHandle) args[0];
      return 17;
    }, new BrowserContext.ExposeBindingOptions().setHandle(true));
    Page page = context.newPage();
    Object result = page.evaluate("async function() {\n" +
      "  return window['logme']({ foo: 42 });\n" +
      "}");
    assertNotNull(target[0]);
    assertEquals(42, target[0].evaluate("x => x.foo"));
    assertEquals(17, result);
  }
}
