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
 * // ...
 * FormData form = FormData.create()
 *     .set("firstName", "John")
 *     .set("lastName", "Doe")
 *     .set("age", 30);
 * page.request().post("http://localhost/submit", RequestOptions.create().setForm(form));
 * }</pre>
 */
public interface FormData {
  /**
   * Appends a new value onto an existing key inside a FormData object, or adds the key if it does not already exist. File
   * values can be passed either as {@code Path} or as {@code FilePayload}. Multiple fields with the same name can be added.
   *
   * <p> The difference between {@link com.microsoft.playwright.FormData#set FormData.set()} and {@link
   * com.microsoft.playwright.FormData#append FormData.append()} is that if the specified key already exists, {@link
   * com.microsoft.playwright.FormData#set FormData.set()} will overwrite all existing values with the new one, whereas
   * {@link com.microsoft.playwright.FormData#append FormData.append()} will append the new value onto the end of the
   * existing set of values.
   * <pre>{@code
   * import com.microsoft.playwright.options.FormData;
   * // ...
   * FormData form = FormData.create()
   *     // Only name and value are set.
   *     .append("firstName", "John")
   *     // Name and value are set, filename and Content-Type are inferred from the file path.
   *     .append("attachment", Paths.get("pic.jpg"))
   *     // Name, value, filename and Content-Type are set.
   *     .append("attachment", new FilePayload("table.csv", "text/csv", Files.readAllBytes(Paths.get("my-tble.csv"))));
   * page.request().post("http://localhost/submit", RequestOptions.create().setForm(form));
   * }</pre>
   *
   * @param name Field name.
   * @param value Field value.
   * @since v1.44
   */
  FormData append(String name, String value);
  /**
   * Appends a new value onto an existing key inside a FormData object, or adds the key if it does not already exist. File
   * values can be passed either as {@code Path} or as {@code FilePayload}. Multiple fields with the same name can be added.
   *
   * <p> The difference between {@link com.microsoft.playwright.FormData#set FormData.set()} and {@link
   * com.microsoft.playwright.FormData#append FormData.append()} is that if the specified key already exists, {@link
   * com.microsoft.playwright.FormData#set FormData.set()} will overwrite all existing values with the new one, whereas
   * {@link com.microsoft.playwright.FormData#append FormData.append()} will append the new value onto the end of the
   * existing set of values.
   * <pre>{@code
   * import com.microsoft.playwright.options.FormData;
   * // ...
   * FormData form = FormData.create()
   *     // Only name and value are set.
   *     .append("firstName", "John")
   *     // Name and value are set, filename and Content-Type are inferred from the file path.
   *     .append("attachment", Paths.get("pic.jpg"))
   *     // Name, value, filename and Content-Type are set.
   *     .append("attachment", new FilePayload("table.csv", "text/csv", Files.readAllBytes(Paths.get("my-tble.csv"))));
   * page.request().post("http://localhost/submit", RequestOptions.create().setForm(form));
   * }</pre>
   *
   * @param name Field name.
   * @param value Field value.
   * @since v1.44
   */
  FormData append(String name, boolean value);
  /**
   * Appends a new value onto an existing key inside a FormData object, or adds the key if it does not already exist. File
   * values can be passed either as {@code Path} or as {@code FilePayload}. Multiple fields with the same name can be added.
   *
   * <p> The difference between {@link com.microsoft.playwright.FormData#set FormData.set()} and {@link
   * com.microsoft.playwright.FormData#append FormData.append()} is that if the specified key already exists, {@link
   * com.microsoft.playwright.FormData#set FormData.set()} will overwrite all existing values with the new one, whereas
   * {@link com.microsoft.playwright.FormData#append FormData.append()} will append the new value onto the end of the
   * existing set of values.
   * <pre>{@code
   * import com.microsoft.playwright.options.FormData;
   * // ...
   * FormData form = FormData.create()
   *     // Only name and value are set.
   *     .append("firstName", "John")
   *     // Name and value are set, filename and Content-Type are inferred from the file path.
   *     .append("attachment", Paths.get("pic.jpg"))
   *     // Name, value, filename and Content-Type are set.
   *     .append("attachment", new FilePayload("table.csv", "text/csv", Files.readAllBytes(Paths.get("my-tble.csv"))));
   * page.request().post("http://localhost/submit", RequestOptions.create().setForm(form));
   * }</pre>
   *
   * @param name Field name.
   * @param value Field value.
   * @since v1.44
   */
  FormData append(String name, int value);
  /**
   * Appends a new value onto an existing key inside a FormData object, or adds the key if it does not already exist. File
   * values can be passed either as {@code Path} or as {@code FilePayload}. Multiple fields with the same name can be added.
   *
   * <p> The difference between {@link com.microsoft.playwright.FormData#set FormData.set()} and {@link
   * com.microsoft.playwright.FormData#append FormData.append()} is that if the specified key already exists, {@link
   * com.microsoft.playwright.FormData#set FormData.set()} will overwrite all existing values with the new one, whereas
   * {@link com.microsoft.playwright.FormData#append FormData.append()} will append the new value onto the end of the
   * existing set of values.
   * <pre>{@code
   * import com.microsoft.playwright.options.FormData;
   * // ...
   * FormData form = FormData.create()
   *     // Only name and value are set.
   *     .append("firstName", "John")
   *     // Name and value are set, filename and Content-Type are inferred from the file path.
   *     .append("attachment", Paths.get("pic.jpg"))
   *     // Name, value, filename and Content-Type are set.
   *     .append("attachment", new FilePayload("table.csv", "text/csv", Files.readAllBytes(Paths.get("my-tble.csv"))));
   * page.request().post("http://localhost/submit", RequestOptions.create().setForm(form));
   * }</pre>
   *
   * @param name Field name.
   * @param value Field value.
   * @since v1.44
   */
  FormData append(String name, Path value);
  /**
   * Appends a new value onto an existing key inside a FormData object, or adds the key if it does not already exist. File
   * values can be passed either as {@code Path} or as {@code FilePayload}. Multiple fields with the same name can be added.
   *
   * <p> The difference between {@link com.microsoft.playwright.FormData#set FormData.set()} and {@link
   * com.microsoft.playwright.FormData#append FormData.append()} is that if the specified key already exists, {@link
   * com.microsoft.playwright.FormData#set FormData.set()} will overwrite all existing values with the new one, whereas
   * {@link com.microsoft.playwright.FormData#append FormData.append()} will append the new value onto the end of the
   * existing set of values.
   * <pre>{@code
   * import com.microsoft.playwright.options.FormData;
   * // ...
   * FormData form = FormData.create()
   *     // Only name and value are set.
   *     .append("firstName", "John")
   *     // Name and value are set, filename and Content-Type are inferred from the file path.
   *     .append("attachment", Paths.get("pic.jpg"))
   *     // Name, value, filename and Content-Type are set.
   *     .append("attachment", new FilePayload("table.csv", "text/csv", Files.readAllBytes(Paths.get("my-tble.csv"))));
   * page.request().post("http://localhost/submit", RequestOptions.create().setForm(form));
   * }</pre>
   *
   * @param name Field name.
   * @param value Field value.
   * @since v1.44
   */
  FormData append(String name, FilePayload value);
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
   * <pre>{@code
   * import com.microsoft.playwright.options.FormData;
   * // ...
   * FormData form = FormData.create()
   *     // Only name and value are set.
   *     .set("firstName", "John")
   *     // Name and value are set, filename and Content-Type are inferred from the file path.
   *     .set("profilePicture1", Paths.get("john.jpg"))
   *     // Name, value, filename and Content-Type are set.
   *     .set("profilePicture2", new FilePayload("john.jpg", "image/jpeg", Files.readAllBytes(Paths.get("john.jpg"))))
   *     .set("age", 30);
   * page.request().post("http://localhost/submit", RequestOptions.create().setForm(form));
   * }</pre>
   *
   * @param name Field name.
   * @param value Field value.
   * @since v1.18
   */
  FormData set(String name, String value);
  /**
   * Sets a field on the form. File values can be passed either as {@code Path} or as {@code FilePayload}.
   * <pre>{@code
   * import com.microsoft.playwright.options.FormData;
   * // ...
   * FormData form = FormData.create()
   *     // Only name and value are set.
   *     .set("firstName", "John")
   *     // Name and value are set, filename and Content-Type are inferred from the file path.
   *     .set("profilePicture1", Paths.get("john.jpg"))
   *     // Name, value, filename and Content-Type are set.
   *     .set("profilePicture2", new FilePayload("john.jpg", "image/jpeg", Files.readAllBytes(Paths.get("john.jpg"))))
   *     .set("age", 30);
   * page.request().post("http://localhost/submit", RequestOptions.create().setForm(form));
   * }</pre>
   *
   * @param name Field name.
   * @param value Field value.
   * @since v1.18
   */
  FormData set(String name, boolean value);
  /**
   * Sets a field on the form. File values can be passed either as {@code Path} or as {@code FilePayload}.
   * <pre>{@code
   * import com.microsoft.playwright.options.FormData;
   * // ...
   * FormData form = FormData.create()
   *     // Only name and value are set.
   *     .set("firstName", "John")
   *     // Name and value are set, filename and Content-Type are inferred from the file path.
   *     .set("profilePicture1", Paths.get("john.jpg"))
   *     // Name, value, filename and Content-Type are set.
   *     .set("profilePicture2", new FilePayload("john.jpg", "image/jpeg", Files.readAllBytes(Paths.get("john.jpg"))))
   *     .set("age", 30);
   * page.request().post("http://localhost/submit", RequestOptions.create().setForm(form));
   * }</pre>
   *
   * @param name Field name.
   * @param value Field value.
   * @since v1.18
   */
  FormData set(String name, int value);
  /**
   * Sets a field on the form. File values can be passed either as {@code Path} or as {@code FilePayload}.
   * <pre>{@code
   * import com.microsoft.playwright.options.FormData;
   * // ...
   * FormData form = FormData.create()
   *     // Only name and value are set.
   *     .set("firstName", "John")
   *     // Name and value are set, filename and Content-Type are inferred from the file path.
   *     .set("profilePicture1", Paths.get("john.jpg"))
   *     // Name, value, filename and Content-Type are set.
   *     .set("profilePicture2", new FilePayload("john.jpg", "image/jpeg", Files.readAllBytes(Paths.get("john.jpg"))))
   *     .set("age", 30);
   * page.request().post("http://localhost/submit", RequestOptions.create().setForm(form));
   * }</pre>
   *
   * @param name Field name.
   * @param value Field value.
   * @since v1.18
   */
  FormData set(String name, Path value);
  /**
   * Sets a field on the form. File values can be passed either as {@code Path} or as {@code FilePayload}.
   * <pre>{@code
   * import com.microsoft.playwright.options.FormData;
   * // ...
   * FormData form = FormData.create()
   *     // Only name and value are set.
   *     .set("firstName", "John")
   *     // Name and value are set, filename and Content-Type are inferred from the file path.
   *     .set("profilePicture1", Paths.get("john.jpg"))
   *     // Name, value, filename and Content-Type are set.
   *     .set("profilePicture2", new FilePayload("john.jpg", "image/jpeg", Files.readAllBytes(Paths.get("john.jpg"))))
   *     .set("age", 30);
   * page.request().post("http://localhost/submit", RequestOptions.create().setForm(form));
   * }</pre>
   *
   * @param name Field name.
   * @param value Field value.
   * @since v1.18
   */
  FormData set(String name, FilePayload value);
}

