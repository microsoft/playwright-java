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

// Port of isomorphic/stringUtils.ts.
package com.microsoft.playwright.impl.isomorphic;

import com.google.gson.Gson;

import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.Utils.toJsRegexFlags;

// java: only the functions the client uses; a string | RegExp parameter is an Object holding a String or a Pattern.
public final class StringUtils {
  private StringUtils() {
  }

  private static final Gson gson = new Gson();

  // java: JSON.stringify of a string.
  public static String jsonStringify(String value) {
    return gson.toJson(value);
  }

  // java: toJsRegexFlags() never produces the 'u' or 'v' flag, so there is no unicode early return.
  private static String escapeRegexForSelector(Pattern re) {
    // Even number of backslashes followed by the quote -> insert a backslash.
    return toJsRegExp(re).replaceAll("(^|[^\\\\])(\\\\\\\\)*([\"'`])", "$1$2\\\\$3").replaceAll(">>", "\\\\>\\\\>");
  }

  public static String escapeForTextSelector(Object text, boolean exact) {
    if (!(text instanceof String))
      return escapeRegexForSelector((Pattern) text);
    return jsonStringify((String) text) + (exact ? "s" : "i");
  }

  public static String escapeForAttributeSelector(Object value, boolean exact) {
    if (!(value instanceof String))
      return escapeRegexForSelector((Pattern) value);
    // TODO: this should actually be
    //   cssEscape(value).replace(/\\ /g, ' ')
    // However, our attribute selectors do not conform to CSS parsing spec,
    // so we escape them differently.
    return "\"" + ((String) value).replace("\\", "\\\\").replace("\"", "\\\"") + "\"" + (exact ? "s" : "i");
  }

  // java: [...input] splits into code points.
  public static String trimString(String input, int cap, String suffix) {
    if (input.length() <= cap)
      return input;
    int[] chars = input.codePoints().toArray();
    if (chars.length > cap)
      return new String(chars, 0, cap - suffix.length()) + suffix;
    return new String(chars, 0, chars.length);
  }

  public static String trimStringWithEllipsis(String input, int cap) {
    return trimString(input, cap, "…");
  }

  // java: String(re) of a JS RegExp.
  private static String toJsRegExp(Pattern pattern) {
    return "/" + pattern.pattern() + "/" + toJsRegexFlags(pattern);
  }
}
