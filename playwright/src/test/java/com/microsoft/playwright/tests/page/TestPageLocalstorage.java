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

package com.microsoft.playwright.tests.page;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import com.microsoft.playwright.options.WebStorageItem;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.microsoft.playwright.fixtures.Utils.mapOf;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Port of tests/page/page-localstorage.spec.ts. */
public class TestPageLocalstorage {
  // it.skip(({ isElectron }) => !!isElectron, 'Electron shares a single context between tests') is dropped.

  // Java: WebStorageItem has no equals(), the items are compared as { name, value } maps.
  private static List<Map<String, Object>> plain(List<WebStorageItem> items) {
    List<Map<String, Object>> result = new ArrayList<>();
    for (WebStorageItem item : items) {
      result.add(mapOf("name", item.name, "value", item.value));
    }
    return result;
  }

  @PlaywrightTest
  @DisplayName("ST-023 localStorage.items returns empty array on fresh origin")
  void localStorageItemsReturnsEmptyArrayOnFreshOrigin(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    assertEquals(new ArrayList<>(), plain(page.localStorage().items()));
  }

  @PlaywrightTest
  @DisplayName("ST-024 localStorage.getItem returns null for missing key")
  void localStorageGetItemReturnsNullForMissingKey(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    assertNull(page.localStorage().getItem("absent"));
  }

  @PlaywrightTest
  @DisplayName("ST-025 localStorage.setItem persists and surfaces in items()/getItem()")
  void localStorageSetItemPersistsAndSurfacesInItemsGetItem(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.localStorage().setItem("alpha", "1");
    page.localStorage().setItem("beta", "2");

    assertEquals(new HashSet<>(asList(
      mapOf("name", "alpha", "value", "1"),
      mapOf("name", "beta", "value", "2"))), new HashSet<>(plain(page.localStorage().items())));
    assertEquals("1", page.localStorage().getItem("alpha"));
    assertEquals("1", page.evaluate("() => localStorage.getItem('alpha')"));
  }

  @PlaywrightTest
  @DisplayName("ST-026 localStorage.setItem overwrites existing value")
  void localStorageSetItemOverwritesExistingValue(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.localStorage().setItem("k", "first");
    page.localStorage().setItem("k", "second");
    assertEquals("second", page.localStorage().getItem("k"));
  }

  @PlaywrightTest
  @DisplayName("ST-027 localStorage.removeItem removes a single item")
  void localStorageRemoveItemRemovesASingleItem(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.localStorage().setItem("a", "1");
    page.localStorage().setItem("b", "2");

    page.localStorage().removeItem("a");
    assertEquals(asList(mapOf("name", "b", "value", "2")), plain(page.localStorage().items()));
  }

  @PlaywrightTest
  @DisplayName("ST-028 localStorage.clear empties storage")
  void localStorageClearEmptiesStorage(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.localStorage().setItem("a", "1");
    page.localStorage().setItem("b", "2");

    page.localStorage().clear();
    assertEquals(new ArrayList<>(), plain(page.localStorage().items()));
  }

  @PlaywrightTest
  @DisplayName("ST-029 sessionStorage round-trip")
  void sessionStorageRoundTrip(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    assertEquals(new ArrayList<>(), plain(page.sessionStorage().items()));

    page.sessionStorage().setItem("s1", "v1");
    page.sessionStorage().setItem("s2", "v2");
    assertEquals(new HashSet<>(asList(
      mapOf("name", "s1", "value", "v1"),
      mapOf("name", "s2", "value", "v2"))), new HashSet<>(plain(page.sessionStorage().items())));
    assertEquals("v1", page.sessionStorage().getItem("s1"));

    page.sessionStorage().removeItem("s1");
    assertEquals(asList(mapOf("name", "s2", "value", "v2")), plain(page.sessionStorage().items()));

    page.sessionStorage().clear();
    assertEquals(new ArrayList<>(), plain(page.sessionStorage().items()));
  }

  @PlaywrightTest
  @DisplayName("ST-030 localStorage and sessionStorage are independent")
  void localStorageAndSessionStorageAreIndependent(Page page, TestServer server) {
    page.navigate(server.EMPTY_PAGE);
    page.localStorage().setItem("shared", "local");
    page.sessionStorage().setItem("shared", "session");

    assertEquals("local", page.localStorage().getItem("shared"));
    assertEquals("session", page.sessionStorage().getItem("shared"));

    page.localStorage().clear();
    assertEquals(new ArrayList<>(), plain(page.localStorage().items()));
    assertEquals("session", page.sessionStorage().getItem("shared"));
  }

  @PlaywrightTest
  @DisplayName("ST-031 storage methods are scoped to the current origin")
  void storageMethodsAreScopedToTheCurrentOrigin(Page page, TestServer server) {
    page.navigate(server.PREFIX + "/empty.html");
    page.localStorage().setItem("k", "origin-1");

    page.navigate(server.CROSS_PROCESS_PREFIX + "/empty.html");
    assertEquals(new ArrayList<>(), plain(page.localStorage().items()));
    page.localStorage().setItem("k", "origin-2");

    page.navigate(server.PREFIX + "/empty.html");
    assertEquals("origin-1", page.localStorage().getItem("k"));
  }
}
