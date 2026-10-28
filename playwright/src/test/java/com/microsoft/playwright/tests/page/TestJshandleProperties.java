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

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Port of tests/page/jshandle-properties.spec.ts. */
public class TestJshandleProperties {
  @PlaywrightTest
  @DisplayName("JH-010 should work @smoke")
  void shouldWork(Page page) {
    JSHandle aHandle = page.evaluateHandle("() => ({\n" +
      "    one: 1,\n" +
      "    two: 2,\n" +
      "    three: 3\n" +
      "  })");
    JSHandle twoHandle = aHandle.getProperty("two");
    assertEquals(2, twoHandle.jsonValue());
  }

  @PlaywrightTest
  @DisplayName("JH-011 should work with undefined, null, and empty")
  void shouldWorkWithUndefinedNullAndEmpty(Page page) {
    JSHandle aHandle = page.evaluateHandle("() => ({\n" +
      "    undefined: undefined,\n" +
      "    null: null,\n" +
      "  })");
    JSHandle undefinedHandle = aHandle.getProperty("undefined");
    // Java: undefined and null are both null, so String(undefined) is checked as null
    assertNull(undefinedHandle.jsonValue());
    JSHandle nullHandle = aHandle.getProperty("null");
    assertNull(nullHandle.jsonValue());
    JSHandle emptyhandle = aHandle.getProperty("empty");
    assertNull(emptyhandle.jsonValue());
  }

  @PlaywrightTest
  @DisplayName("JH-012 should work with unserializable values")
  void shouldWorkWithUnserializableValues(Page page) {
    JSHandle aHandle = page.evaluateHandle("() => ({\n" +
      "    infinity: Infinity,\n" +
      "    nInfinity: -Infinity,\n" +
      "    nan: NaN,\n" +
      "    nzero: -0\n" +
      "  })");
    JSHandle infinityHandle = aHandle.getProperty("infinity");
    assertEquals(Double.POSITIVE_INFINITY, infinityHandle.jsonValue());
    JSHandle nInfinityHandle = aHandle.getProperty("nInfinity");
    assertEquals(Double.NEGATIVE_INFINITY, nInfinityHandle.jsonValue());
    JSHandle nanHandle = aHandle.getProperty("nan");
    assertEquals("NaN", String.valueOf(nanHandle.jsonValue()));
    JSHandle nzeroHandle = aHandle.getProperty("nzero");
    assertEquals(-0.0, nzeroHandle.jsonValue());
  }

  @PlaywrightTest
  @DisplayName("JH-013 getProperties should work")
  void getPropertiesShouldWork(Page page) {
    JSHandle aHandle = page.evaluateHandle("() => ({\n" +
      "    foo: 'bar'\n" +
      "  })");
    Map<String, JSHandle> properties = aHandle.getProperties();
    JSHandle foo = properties.get("foo");
    assertNotNull(foo);
    assertEquals("bar", foo.jsonValue());
  }

  @PlaywrightTest
  @DisplayName("JH-014 getProperties should return empty map for non-objects")
  void getPropertiesShouldReturnEmptyMapForNonObjects(Page page) {
    JSHandle aHandle = page.evaluateHandle("() => 123");
    Map<String, JSHandle> properties = aHandle.getProperties();
    assertEquals(0, properties.size());
  }

  @PlaywrightTest
  @DisplayName("JH-015 getProperties should return properties inhertied from super class")
  void getPropertiesShouldReturnPropertiesInhertiedFromSuperClass(Page page) {
    JSHandle aHandle = page.evaluateHandle("() => {\n" +
      "    class A {\n" +
      "      constructor() {\n" +
      "        this.a = '1';\n" +
      "      }\n" +
      "    }\n" +
      "    class B extends A {\n" +
      "      constructor() {\n" +
      "        super();\n" +
      "        this.b = '2';\n" +
      "      }\n" +
      "    }\n" +
      "    return new B();\n" +
      "  }");
    Map<String, JSHandle> properties = aHandle.getProperties();
    assertEquals("1", properties.get("a").jsonValue());
    assertEquals("2", properties.get("b").jsonValue());
  }

  @PlaywrightTest
  @DisplayName("JH-016 getProperties should return own enumerable properties only")
  void getPropertiesShouldReturnOwnEnumerablePropertiesOnly(Page page) {
    JSHandle aHandle = page.evaluateHandle("() => {\n" +
      "    class A {\n" +
      "      constructor() {\n" +
      "        this.own = 1;\n" +
      "        Object.defineProperty(this, 'ownHidden', { value: 2, enumerable: false });\n" +
      "      }\n" +
      "    }\n" +
      "    A.prototype.inherited = 3;\n" +
      "    return new A();\n" +
      "  }");
    assertEquals(asList("own"), new ArrayList<>(aHandle.getProperties().keySet()));

    page.setContent("<div id=d><span>a</span><span>b</span></div>");
    JSHandle collection = page.evaluateHandle("() => document.querySelector('#d').children");
    assertEquals(asList("0", "1"), new ArrayList<>(collection.getProperties().keySet()));
  }

  @PlaywrightTest
  @DisplayName("JH-017 getProperties should work with elements")
  void getPropertiesShouldWorkWithElements(Page page) {
    page.setContent("<div>Hello</div>");
    JSHandle handle = page.evaluateHandle("() => ({ body: document.body })");
    Map<String, JSHandle> properties = handle.getProperties();
    ElementHandle body = (ElementHandle) properties.get("body");
    assertNotNull(body);
    assertEquals("Hello", body.textContent());
  }
}
