/**
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

package com.microsoft.playwright.tests.library;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Selectors;
import com.microsoft.playwright.fixtures.Fixme;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.DisplayName;

import static com.microsoft.playwright.fixtures.Utils.asset;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/library/selectors-register.spec.ts. */
public class TestSelectorsRegister {
  // Java: the source of `(${createTagSelector.toString()})()`, the script the Node.js client builds from the function.
  private static final String createTagSelector = "(() => ({\n" +
    "  query(root, selector) {\n" +
    "    return root.querySelector(selector);\n" +
    "  },\n" +
    "  queryAll(root, selector) {\n" +
    "    return Array.from(root.querySelectorAll(selector));\n" +
    "  }\n" +
    "}))()";

  @PlaywrightTest
  @DisplayName("SE-160 should work")
  void shouldWork(Playwright playwright, Browser browser) {
    // Register one engine before creating context.
    playwright.selectors().register("tag", createTagSelector);

    BrowserContext context = browser.newContext();
    // Register another engine after creating context.
    playwright.selectors().register("tag2", createTagSelector);

    Page page = context.newPage();
    page.setContent("<div><span></span></div><div></div>");

    assertEquals("DIV", page.evalOnSelector("tag=DIV", "e => e.nodeName"));
    assertEquals("SPAN", page.evalOnSelector("tag=SPAN", "e => e.nodeName"));
    assertEquals(2, page.evalOnSelectorAll("tag=DIV", "es => es.length"));

    assertEquals("DIV", page.evalOnSelector("tag2=DIV", "e => e.nodeName"));
    assertEquals("SPAN", page.evalOnSelector("tag2=SPAN", "e => e.nodeName"));
    assertEquals(2, page.evalOnSelectorAll("tag2=DIV", "es => es.length"));

    // Selector names are case-sensitive.
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.querySelector("tAG=DIV"));
    assertTrue(error.getMessage().contains("Unknown engine \"tAG\" while parsing selector tAG=DIV"));

    context.close();
  }

  @PlaywrightTest
  @DisplayName("SE-161 should work when registered on global")
  void shouldWorkWhenRegisteredOnGlobal(Playwright playwright, Browser browser) {
    // Java: require('@playwright/test').selectors is the selectors of the Playwright instance of the test.
    playwright.selectors().register("oop-tag", createTagSelector);

    BrowserContext context = browser.newContext();
    // Register another engine after creating context.
    playwright.selectors().register("oop-tag2", createTagSelector);

    Page page = context.newPage();
    page.setContent("<div><span></span></div><div></div>");

    assertEquals("DIV", page.evalOnSelector("oop-tag=DIV", "e => e.nodeName"));
    assertEquals("SPAN", page.evalOnSelector("oop-tag=SPAN", "e => e.nodeName"));
    assertEquals(2, page.evalOnSelectorAll("oop-tag=DIV", "es => es.length"));

    assertEquals("DIV", page.evalOnSelector("oop-tag2=DIV", "e => e.nodeName"));
    assertEquals("SPAN", page.evalOnSelector("oop-tag2=SPAN", "e => e.nodeName"));
    assertEquals(2, page.evalOnSelectorAll("oop-tag2=DIV", "es => es.length"));

    context.close();
  }

  @PlaywrightTest
  @DisplayName("SE-162 should work with path")
  void shouldWorkWithPath(Playwright playwright, Browser browser) {
    playwright.selectors().register("foo", asset("sectionselectorengine.js"));
    Page page = browser.newPage();
    page.setContent("<section></section>");
    assertEquals("SECTION", page.evalOnSelector("foo=whatever", "e => e.nodeName"));
    page.close();
  }

  @PlaywrightTest
  @DisplayName("SE-163 should work in main and isolated world")
  void shouldWorkInMainAndIsolatedWorld(Playwright playwright, Browser browser) {
    String createDummySelector = "(() => ({\n" +
      "  query(root, selector) {\n" +
      "    return window['__answer'];\n" +
      "  },\n" +
      "  queryAll(root, selector) {\n" +
      "    return window['__answer'] ? [window['__answer'], document.body, document.documentElement] : [];\n" +
      "  }\n" +
      "}))()";
    playwright.selectors().register("main", createDummySelector);
    playwright.selectors().register("isolated", createDummySelector, new Selectors.RegisterOptions().setContentScript(true));
    Page page = browser.newPage();
    page.setContent("<div><span><section></section></span></div>");
    page.evaluate("() => window['__answer'] = document.querySelector('span')");
    // Works in main if asked.
    assertEquals("SPAN", page.evalOnSelector("main=ignored", "e => e.nodeName"));
    assertEquals("SPAN", page.evalOnSelector("css=div >> main=ignored", "e => e.nodeName"));
    assertEquals(true, page.evalOnSelectorAll("main=ignored", "es => window['__answer'] !== undefined"));
    assertEquals(3, page.evalOnSelectorAll("main=ignored", "es => es.filter(e => e).length"));
    // Works in isolated by default.
    assertNull(page.querySelector("isolated=ignored"));
    assertNull(page.querySelector("css=div >> isolated=ignored"));
    // $$eval always works in main, to avoid adopting nodes one by one.
    assertEquals(true, page.evalOnSelectorAll("isolated=ignored", "es => window['__answer'] !== undefined"));
    assertEquals(3, page.evalOnSelectorAll("isolated=ignored", "es => es.filter(e => e).length"));
    // At least one engine in main forces all to be in main.
    assertEquals("SPAN", page.evalOnSelector("main=ignored >> isolated=ignored", "e => e.nodeName"));
    assertEquals("SPAN", page.evalOnSelector("isolated=ignored >> main=ignored", "e => e.nodeName"));
    // Can be chained to css.
    assertEquals("SECTION", page.evalOnSelector("main=ignored >> css=section", "e => e.nodeName"));
    page.close();
  }

  @PlaywrightTest
  @DisplayName("SE-164 should handle errors")
  @Fixme("java: the message of a driver error is the serialized error (\"Error {\\n  message='Selector engine name may only contain [a-zA-Z0-9_] characters ...\"), without the 'selectors.register: ' prefix, so the toBe comparison fails")
  void shouldHandleErrors(Playwright playwright, Browser browser) {
    Page page = browser.newPage();
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.querySelector("neverregister=ignored"));
    assertTrue(error.getMessage().contains("Unknown engine \"neverregister\" while parsing selector neverregister=ignored"));

    String createDummySelector = "(() => ({\n" +
      "  query(root, selector) {\n" +
      "    return root.querySelector('dummy');\n" +
      "  },\n" +
      "  queryAll(root, selector) {\n" +
      "    return Array.from(root.querySelectorAll('dummy'));\n" +
      "  }\n" +
      "}))()";

    error = assertThrows(PlaywrightException.class, () -> playwright.selectors().register("$", createDummySelector));
    assertEquals("selectors.register: Selector engine name may only contain [a-zA-Z0-9_] characters", error.getMessage());

    // Selector names are case-sensitive.
    playwright.selectors().register("dummy", createDummySelector);
    playwright.selectors().register("duMMy", createDummySelector);

    error = assertThrows(PlaywrightException.class, () -> playwright.selectors().register("dummy", createDummySelector));
    assertEquals("selectors.register: \"dummy\" selector engine has been already registered", error.getMessage());

    error = assertThrows(PlaywrightException.class, () -> playwright.selectors().register("css", createDummySelector));
    assertEquals("selectors.register: \"css\" is a predefined selector engine", error.getMessage());
    page.close();
  }

  @PlaywrightTest
  @DisplayName("SE-165 should throw \"already registered\" error when registering")
  void shouldThrowAlreadyRegisteredErrorWhenRegistering(Playwright playwright) {
    playwright.selectors().register("alreadyRegistered", createTagSelector);
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> playwright.selectors().register("alreadyRegistered", createTagSelector));
    assertTrue(error.getMessage().contains("selectors.register: \"alreadyRegistered\" selector engine has been already registered"));
  }

  @PlaywrightTest
  @DisplayName("SE-166 should not rely on engines working from the root")
  void shouldNotRelyOnEnginesWorkingFromTheRoot(Playwright playwright, Browser browser) {
    String createValueEngine = "(() => ({\n" +
      "  query(root, selector) {\n" +
      "    return root && root.value.includes(selector) ? root : undefined;\n" +
      "  },\n" +
      "  queryAll(root, selector) {\n" +
      "    return root && root.value.includes(selector) ? [root] : [];\n" +
      "  },\n" +
      "}))()";
    playwright.selectors().register("__value", createValueEngine);
    Page page = browser.newPage();
    page.setContent("<input id=input1 value=value1><input id=input2 value=value2>");
    assertEquals("input2", page.evalOnSelector("input >> __value=value2", "e => e.id"));
    page.close();
  }

  @PlaywrightTest
  @DisplayName("SE-167 should throw a nice error if the selector returns a bad value")
  void shouldThrowANiceErrorIfTheSelectorReturnsABadValue(Playwright playwright, Browser browser) {
    String createFakeEngine = "(() => ({\n" +
      "  query(root, selector) {\n" +
      "    return [document.body];\n" +
      "  },\n" +
      "  queryAll(root, selector) {\n" +
      "    return [[document.body]];\n" +
      "  },\n" +
      "}))()";

    playwright.selectors().register("__fake", createFakeEngine);
    Page page = browser.newPage();
    PlaywrightException error = assertThrows(PlaywrightException.class, () -> page.querySelector("__fake=value2"));
    assertTrue(error.getMessage().contains("Expected a Node but got [object Array]"));
    page.close();
  }
}
