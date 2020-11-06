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
 * Playwright generates a lot of logs and they are accessible via the pluggable logger sink.
 * <p>
 */
public interface Logger {
  enum Severity { ERROR, INFO, VERBOSE, WARNING }
  class LogHints {
    /**
     * preferred logger color
     */
    public String color;

    public LogHints withColor(String color) {
      this.color = color;
      return this;
    }
  }
  /**
   * Determines whether sink is interested in the logger with the given name and severity.
   * @param name logger name
   */
  boolean isEnabled(String name, Severity severity);
  /**
   * 
   * @param name logger name
   * @param message log message format
   * @param args message arguments
   * @param hints optional formatting hints
   */
  void log(String name, Severity severity, String message, List<Object> args, LogHints hints);
}

