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

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class TestSelectorsRegister extends TestBase {
  @Test
  void shouldWork() {
    String selectorScript = "{\n" +
      "  create(root, target) {\n" +
      "    return target.nodeName;\n" +
      "  },\n" +
      "  query(root, selector) {\n" +
      "    return root.querySelector(selector);\n" +
      "  },\n" +
      "  queryAll(root, selector) {\n" +
      "    return Array.from(root.querySelectorAll(selector));\n" +
      "  }\n" +
      "}";
    // Register one engine before creating context.
    playwright.selectors().register("tag", selectorScript);

    BrowserContext context = browser.newContext();
    // Register another engine after creating context.
    playwright.selectors().register("tag2", selectorScript);

    Page page = context.newPage();
    page.setContent("<div><span></span></div><div></div>");

    assertEquals("DIV", page.evalOnSelector("tag=DIV", "e => e.nodeName"));
    assertEquals("SPAN", page.evalOnSelector("tag=SPAN", "e => e.nodeName"));
    assertEquals(2, page.evalOnSelectorAll("tag=DIV", "es => es.length"));

    assertEquals("DIV", page.evalOnSelector("tag2=DIV", "e => e.nodeName"));
    assertEquals("SPAN", page.evalOnSelector("tag2=SPAN", "e => e.nodeName"));
    assertEquals(2, page.evalOnSelectorAll("tag2=DIV", "es => es.length"));
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      // Selector names are case-sensitive.
      page.querySelector("tAG=DIV");
    });
    assertTrue(e.getMessage().contains("Unknown engine \"tAG\" while parsing selector tAG=DIV"));
    context.close();
  }

  @Test
  void shouldWorkWithPath() {
    playwright.selectors().register("foo", Paths.get("src/test/resources/sectionselectorengine.js"));
    page.setContent("<section></section>");
    assertEquals("SECTION", page.evalOnSelector("foo=whatever", "e => e.nodeName"));
  }

  @Test
  void shouldWorkInMainAndIsolatedWorld() {
    String createDummySelector = "{\n" +
      "  create(root, target) { },\n" +
      "  query(root, selector) {\n" +
      "    return window['__answer'];\n" +
      "  },\n" +
      "  queryAll(root, selector) {\n" +
      "    return window['__answer'] ? [window['__answer'], document.body, document.documentElement] : [];\n" +
      "  }\n" +
      "}";
    playwright.selectors().register("main", createDummySelector);
    playwright.selectors().register("isolated", createDummySelector, new Selectors.RegisterOptions().setContentScript(true));
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
  }

  @Test
  void shouldHandleErrors() {
    PlaywrightException e = assertThrows(PlaywrightException.class, () -> {
      page.querySelector("neverregister=ignored");
    });
    assertTrue(e.getMessage().contains("Unknown engine \"neverregister\" while parsing selector neverregister=ignored"));
    String createDummySelector = "{\n" +
      "  create(root, target) {\n" +
      "    return target.nodeName;\n" +
      "  },\n" +
      "  query(root, selector) {\n" +
      "    return root.querySelector(\"dummy\");\n" +
      "  },\n" +
      "  queryAll(root, selector) {\n" +
      "    return Array.from(root.querySelectorAll(\"dummy\"));\n" +
      "  }\n" +
      "}";
    e = assertThrows(PlaywrightException.class, () -> {
      playwright.selectors().register("$", createDummySelector);
    });
    assertTrue(e.getMessage().contains("Selector engine name may only contain [a-zA-Z0-9_] characters"));
    // Selector names are case-sensitive.
    playwright.selectors().register("dummy", createDummySelector);
    playwright.selectors().register("duMMy", createDummySelector);
    e = assertThrows(PlaywrightException.class, () -> {
      playwright.selectors().register("dummy", createDummySelector);
    });
    assertTrue(e.getMessage().contains("\"dummy\" selector engine has been already registered"));
    e = assertThrows(PlaywrightException.class, () -> {
      playwright.selectors().register("css", createDummySelector);
    });
    assertTrue(e.getMessage().contains("\"css\" is a predefined selector engine"));
  }
}
