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


/**
 * {@code WebError} class represents an unhandled exception thrown in the page. It is dispatched via the {@link
 * BrowserContext#onWebError BrowserContext.onWebError()} event.
 * <pre>{@code
 * // Log all uncaught errors to the terminal
 * context.onWebError(webError -> {
 *   System.out.println("Uncaught exception: " + webError.error());
 * });
 *
 * // Navigate to a page with an exception.
 * page.navigate("data:text/html,<script>throw new Error('Test')</script>");
 * }</pre>
 */
public interface WebError {
  /**
   * The page that produced this unhandled exception, if any.
   *
   * @since v1.38
   */
  Page page();
  /**
   * Unhandled error that was thrown.
   *
   * @since v1.38
   */
  String error();
}

