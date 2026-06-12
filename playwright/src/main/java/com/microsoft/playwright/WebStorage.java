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

import com.microsoft.playwright.options.*;
import java.util.*;

/**
 * WebStorage exposes the page's {@code localStorage} or {@code sessionStorage} for the current origin via an async, <a
 * href="https://developer.mozilla.org/en-US/docs/Web/API/Storage">browser-consistent</a> API.
 *
 * <p> Instances are accessed through {@link com.microsoft.playwright.Page#localStorage Page.localStorage()} and {@link
 * com.microsoft.playwright.Page#sessionStorage Page.sessionStorage()}.
 * <pre>{@code
 * page.navigate("https://example.com");
 * page.localStorage().setItem("token", "abc");
 * String token = page.localStorage().getItem("token");
 * List<WebStorageItem> all = page.localStorage().items();
 * page.localStorage().removeItem("token");
 * page.localStorage().clear();
 * }</pre>
 */
public interface WebStorage {
  /**
   * Returns all items in the storage as name/value pairs.
   *
   * @since v1.61
   */
  List<WebStorageItem> items();
  /**
   * Returns the value for the given {@code name} if present.
   *
   * @param name Name of the item to retrieve.
   * @since v1.61
   */
  String getItem(String name);
  /**
   * Sets the value for the given {@code name}. Overwrites any existing value for that name.
   *
   * @param name Name of the item to set.
   * @param value New value for the item.
   * @since v1.61
   */
  void setItem(String name, String value);
  /**
   * Removes the item with the given {@code name}. No-op if the item is absent.
   *
   * @param name Name of the item to remove.
   * @since v1.61
   */
  void removeItem(String name);
  /**
   * Removes all items from the storage.
   *
   * @since v1.61
   */
  void clear();
}

