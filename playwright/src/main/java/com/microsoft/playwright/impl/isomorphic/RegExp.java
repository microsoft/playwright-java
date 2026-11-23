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

// java: no counterpart upstream. A JS RegExp of a selector, as its source and flags. It is never compiled:
// a Java Pattern does not accept every JS regular expression, and the generators only print it.
package com.microsoft.playwright.impl.isomorphic;

import com.microsoft.playwright.PlaywrightException;

public final class RegExp {
  public final String source;
  public final String flags;

  // java: the constructor of a JS RegExp throws on an invalid flag or a repeated one, and on an invalid source;
  // the source is not checked here.
  public RegExp(String source, String flags) {
    StringBuilder canonicalFlags = new StringBuilder();
    for (char flag : "dgimsuvy".toCharArray()) {
      int index = flags.indexOf(flag);
      if (index == -1)
        continue;
      if (flags.indexOf(flag, index + 1) != -1)
        throw new PlaywrightException("Invalid flags supplied to RegExp constructor '" + flags + "'");
      canonicalFlags.append(flag);
    }
    if (canonicalFlags.length() != flags.length() || (flags.contains("u") && flags.contains("v")))
      throw new PlaywrightException("Invalid flags supplied to RegExp constructor '" + flags + "'");
    this.source = escapeSource(source);
    this.flags = canonicalFlags.toString();
  }

  // java: RegExp.prototype.source escapes a slash outside of a character class and the line terminators,
  // and is (?:) for an empty pattern.
  private static String escapeSource(String pattern) {
    if (pattern.isEmpty())
      return "(?:)";
    StringBuilder result = new StringBuilder();
    boolean inClass = false;
    for (int i = 0; i < pattern.length(); i++) {
      char c = pattern.charAt(i);
      if (c == '\\' && i + 1 < pattern.length()) {
        char escaped = pattern.charAt(++i);
        if (escaped == '\n')
          result.append("\\n");
        else if (escaped == '\r')
          result.append("\\r");
        else if (escaped == '\u2028')
          result.append("\\u2028");
        else if (escaped == '\u2029')
          result.append("\\u2029");
        else
          result.append(c).append(escaped);
        continue;
      }
      if (c == '[')
        inClass = true;
      else if (c == ']')
        inClass = false;
      if (c == '/' && !inClass)
        result.append("\\/");
      else if (c == '\n')
        result.append("\\n");
      else if (c == '\r')
        result.append("\\r");
      else if (c == '\u2028')
        result.append("\\u2028");
      else if (c == '\u2029')
        result.append("\\u2029");
      else
        result.append(c);
    }
    return result.toString();
  }

  @Override
  public String toString() {
    return "/" + source + "/" + flags;
  }
}
