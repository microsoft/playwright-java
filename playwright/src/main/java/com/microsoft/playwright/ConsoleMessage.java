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

import java.util.*;

/**
 * {@code ConsoleMessage} objects are dispatched by page via the {@link Page#onConsoleMessage Page.onConsoleMessage()}
 * event. For each console messages logged in the page there will be corresponding event in the Playwright context.
 * <pre>{@code
 * // Listen for all console messages and print them to the standard output.
 * page.onConsoleMessage(msg -> System.out.println(msg.text()));
 *
 * // Listen for all console messages and print errors to the standard output.
 * page.onConsoleMessage(msg -> {
 *   if ("error".equals(msg.type()))
 *     System.out.println("Error text: " + msg.text());
 * });
 *
 * // Get the next console message
 * ConsoleMessage msg = page.waitForConsoleMessage(() -> {
 *   // Issue console.log inside the page
 *   page.evaluate("console.log('hello', 42, { foo: 'bar' });");
 * });
 *
 * // Deconstruct console.log arguments
 * msg.args().get(0).jsonValue() // hello
 * msg.args().get(1).jsonValue() // 42
 * }</pre>
 */
public interface ConsoleMessage {
  /**
   * List of arguments passed to a {@code console} function call. See also {@link Page#onConsoleMessage
   * Page.onConsoleMessage()}.
   *
   * @since v1.8
   */
  List<JSHandle> args();
  /**
   * URL of the resource followed by 0-based line and column numbers in the resource formatted as {@code URL:line:column}.
   *
   * @since v1.8
   */
  String location();
  /**
   * The page that produced this console message, if any.
   *
   * @since v1.34
   */
  Page page();
  /**
   * The text of the console message.
   *
   * @since v1.8
   */
  String text();
  /**
   * One of the following values: {@code "log"}, {@code "debug"}, {@code "info"}, {@code "error"}, {@code "warning"}, {@code
   * "dir"}, {@code "dirxml"}, {@code "table"}, {@code "trace"}, {@code "clear"}, {@code "startGroup"}, {@code
   * "startGroupCollapsed"}, {@code "endGroup"}, {@code "assert"}, {@code "profile"}, {@code "profileEnd"}, {@code "count"},
   * {@code "timeEnd"}.
   *
   * @since v1.8
   */
  String type();
}

