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

import com.microsoft.playwright.options.WebStorageItem;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.microsoft.playwright.Utils.mapOf;
import static org.junit.jupiter.api.Assertions.*;

public class TestWebStorage extends TestBase {
  private static Map<String, String> asMap(List<WebStorageItem> items) {
    Map<String, String> map = new HashMap<>();
    for (WebStorageItem item : items) {
      map.put(item.name, item.value);
    }
    return map;
  }

  @Test
  void localStorageItemsReturnsEmptyListOnFreshOrigin() {
    page.navigate(server.EMPTY_PAGE);
    assertEquals(0, page.localStorage().items().size());
  }

  @Test
  void localStorageGetItemReturnsNullForMissingKey() {
    page.navigate(server.EMPTY_PAGE);
    assertNull(page.localStorage().getItem("absent"));
  }

  @Test
  void localStorageSetItemPersistsAndSurfacesInItemsAndGetItem() {
    page.navigate(server.EMPTY_PAGE);
    page.localStorage().setItem("alpha", "1");
    page.localStorage().setItem("beta", "2");

    assertEquals(mapOf("alpha", "1", "beta", "2"), asMap(page.localStorage().items()));
    assertEquals("1", page.localStorage().getItem("alpha"));
    assertEquals("1", page.evaluate("() => localStorage.getItem('alpha')"));
  }

  @Test
  void localStorageSetItemOverwritesExistingValue() {
    page.navigate(server.EMPTY_PAGE);
    page.localStorage().setItem("k", "first");
    page.localStorage().setItem("k", "second");
    assertEquals("second", page.localStorage().getItem("k"));
  }

  @Test
  void localStorageRemoveItemRemovesSingleItem() {
    page.navigate(server.EMPTY_PAGE);
    page.localStorage().setItem("a", "1");
    page.localStorage().setItem("b", "2");

    page.localStorage().removeItem("a");
    assertEquals(mapOf("b", "2"), asMap(page.localStorage().items()));
  }

  @Test
  void localStorageClearEmptiesStorage() {
    page.navigate(server.EMPTY_PAGE);
    page.localStorage().setItem("a", "1");
    page.localStorage().setItem("b", "2");

    page.localStorage().clear();
    assertEquals(0, page.localStorage().items().size());
  }

  @Test
  void sessionStorageRoundTrip() {
    page.navigate(server.EMPTY_PAGE);
    assertEquals(0, page.sessionStorage().items().size());

    page.sessionStorage().setItem("s1", "v1");
    page.sessionStorage().setItem("s2", "v2");
    assertEquals(mapOf("s1", "v1", "s2", "v2"), asMap(page.sessionStorage().items()));
    assertEquals("v1", page.sessionStorage().getItem("s1"));

    page.sessionStorage().removeItem("s1");
    assertEquals(mapOf("s2", "v2"), asMap(page.sessionStorage().items()));

    page.sessionStorage().clear();
    assertEquals(0, page.sessionStorage().items().size());
  }

  @Test
  void localStorageAndSessionStorageAreIndependent() {
    page.navigate(server.EMPTY_PAGE);
    page.localStorage().setItem("shared", "local");
    page.sessionStorage().setItem("shared", "session");

    assertEquals("local", page.localStorage().getItem("shared"));
    assertEquals("session", page.sessionStorage().getItem("shared"));

    page.localStorage().clear();
    assertEquals(0, page.localStorage().items().size());
    assertEquals("session", page.sessionStorage().getItem("shared"));
  }

  @Test
  void storageMethodsAreScopedToTheCurrentOrigin() {
    page.navigate(server.PREFIX + "/empty.html");
    page.localStorage().setItem("k", "origin-1");

    page.navigate(server.CROSS_PROCESS_PREFIX + "/empty.html");
    assertEquals(0, page.localStorage().items().size());
    page.localStorage().setItem("k", "origin-2");

    page.navigate(server.PREFIX + "/empty.html");
    assertEquals("origin-1", page.localStorage().getItem("k"));
  }
}
