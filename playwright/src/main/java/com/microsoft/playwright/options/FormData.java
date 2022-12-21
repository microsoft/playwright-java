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

package com.microsoft.playwright.options;

import com.microsoft.playwright.impl.FormDataImpl;
import java.nio.file.Path;

/**
 * The {@code FormData} is used create form data that is sent via {@code APIRequestContext}.
 * <pre>{@code
 * import com.microsoft.playwright.options.FormData;
 * ...
 * FormData form = FormData.create()
 *     .set("firstName", "John")
 *     .set("lastName", "Doe")
 *     .set("age", 30);
 * page.request().post("http://localhost/submit", RequestOptions.create().setForm(form));
 * }</pre>
 */
public interface FormData {
  /**
   * Creates new instance of {@code FormData}.
   *
   * @since v1.18
   */
  static FormData create() {
    return new FormDataImpl();
  }
  /**
   * Sets a field on the form. File values can be passed either as {@code Path} or as {@code FilePayload}.
   *
   * @param name Field name.
   * @param value Field value.
   * @since v1.18
   */
  FormData set(String name, String value);
  /**
   * Sets a field on the form. File values can be passed either as {@code Path} or as {@code FilePayload}.
   *
   * @param name Field name.
   * @param value Field value.
   * @since v1.18
   */
  FormData set(String name, boolean value);
  /**
   * Sets a field on the form. File values can be passed either as {@code Path} or as {@code FilePayload}.
   *
   * @param name Field name.
   * @param value Field value.
   * @since v1.18
   */
  FormData set(String name, int value);
  /**
   * Sets a field on the form. File values can be passed either as {@code Path} or as {@code FilePayload}.
   *
   * @param name Field name.
   * @param value Field value.
   * @since v1.18
   */
  FormData set(String name, Path value);
  /**
   * Sets a field on the form. File values can be passed either as {@code Path} or as {@code FilePayload}.
   *
   * @param name Field name.
   * @param value Field value.
   * @since v1.18
   */
  FormData set(String name, FilePayload value);
}

