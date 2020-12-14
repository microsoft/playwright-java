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
 * ConsoleMessage objects are dispatched by page via the page.on('console') event.
 */
public interface ConsoleMessage {
  class Location {
    /**
     * URL of the resource if available, otherwise empty string.
     */
    private String url;
    /**
     * 0-based line number in the resource.
     */
    private int lineNumber;
    /**
     * 0-based column number in the resource.
     */
    private int columnNumber;

    public String url() {
      return this.url;
    }
    public int lineNumber() {
      return this.lineNumber;
    }
    public int columnNumber() {
      return this.columnNumber;
    }
  }
  List<JSHandle> args();
  Location location();
  String text();
  /**
   * One of the following values: {@code 'log'}, {@code 'debug'}, {@code 'info'}, {@code 'error'}, {@code 'warning'}, {@code 'dir'}, {@code 'dirxml'}, {@code 'table'},
   * <p>
   * {@code 'trace'}, {@code 'clear'}, {@code 'startGroup'}, {@code 'startGroupCollapsed'}, {@code 'endGroup'}, {@code 'assert'}, {@code 'profile'}, {@code 'profileEnd'},
   * <p>
   * {@code 'count'}, {@code 'timeEnd'}.
   */
  String type();
}

