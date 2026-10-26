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

import com.microsoft.playwright.JSHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import org.junit.jupiter.api.DisplayName;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;

import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/** Port of tests/page/jshandle-json-value.spec.ts. */
public class TestJshandleJsonValue {
  @PlaywrightTest
  @DisplayName("JH-007 should work @smoke")
  void shouldWork(Page page) {
    JSHandle aHandle = page.evaluateHandle("() => ({ foo: 'bar' })");
    Object json = aHandle.jsonValue();
    assertEquals(mapOf("foo", "bar"), json);
  }

  @PlaywrightTest
  @DisplayName("JH-008 should work with dates")
  void shouldWorkWithDates(Page page) {
    JSHandle dateHandle = page.evaluateHandle("() => new Date('2017-09-26T00:00:00.000Z')");
    Date date = (Date) dateHandle.jsonValue();
    // Java: date.toJSON() is the ISO 8601 string of the Date in UTC with milliseconds
    SimpleDateFormat toJSON = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    toJSON.setTimeZone(TimeZone.getTimeZone("UTC"));
    assertEquals("2017-09-26T00:00:00.000Z", toJSON.format(date));
  }

  @PlaywrightTest
  @DisplayName("JH-009 should handle circular objects")
  void shouldHandleCircularObjects(Page page) {
    JSHandle handle = page.evaluateHandle("const a = {}; a.b = a; a");
    // Java: assertEquals would recurse forever on two distinct circular maps, so the shape { b: <itself> } is checked directly
    Map<?, ?> a = (Map<?, ?>) handle.jsonValue();
    assertEquals(asList("b"), new ArrayList<Object>(a.keySet()));
    assertSame(a, a.get("b"));
  }
}
