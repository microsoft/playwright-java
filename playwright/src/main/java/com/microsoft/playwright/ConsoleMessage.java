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
 * {@code ConsoleMessage} objects are dispatched by page via the {@link Page#onConsoleMessage Page.onConsoleMessage()} event.
 */
public interface ConsoleMessage {
  /**
   * List of arguments passed to a {@code console} function call. See also {@link Page#onConsoleMessage Page.onConsoleMessage()}.
   */
  List<JSHandle> args();
  /**
   * URL of the resource followed by 0-based line and column numbers in the resource formatted as {@code URL:line:column}.
   */
  String location();
  /**
   * The text of the console message.
   */
  String text();
  /**
   * One of the following values: {@code "log"}, {@code "debug"}, {@code "info"}, {@code "error"}, {@code "warning"}, {@code "dir"}, {@code "dirxml"}, {@code "table"},
   * {@code "trace"}, {@code "clear"}, {@code "startGroup"}, {@code "startGroupCollapsed"}, {@code "endGroup"}, {@code "assert"}, {@code "profile"}, {@code "profileEnd"},
   * {@code "count"}, {@code "timeEnd"}.
   */
  String type();
}

