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

// Port of pretty-format.
// java: the original is MIT licensed, Copyright (c) Meta Platforms, Inc. and affiliates.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.PlaywrightException;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.JestGetType.isArray;
import static com.microsoft.playwright.impl.Utils.toJsRegexFlags;

// java: only the code that formats the Java values is ported: null, Boolean, Number, String,
// Pattern, List and arrays, Map. The plugins, callToJSON, the colors and the other JavaScript
// types are not ported.
class PrettyFormat {
  // java: the type of the options object. A null field is an option that is not set.
  static class Options {
    Boolean callToJSON;
    Comparator<String> compareKeys;
    Integer indent;
    Integer maxDepth;
    Integer maxWidth;
    Boolean min;
  }

  // java: the type of the config object.
  static class Config {
    boolean callToJSON;
    Comparator<String> compareKeys;
    boolean escapeRegex;
    boolean escapeString;
    String indent;
    int maxDepth;
    int maxWidth;
    boolean min;
    boolean printBasicPrototype;
    boolean printFunctionName;
    String spacingInner;
    String spacingOuter;
  }

  // java: compareKeys is undefined or a function here. The null value of upstream, which
  // keeps the order of the keys, is not ported.
  private static List<Object> getKeysOfEnumerableProperties(Map<?, ?> object, Comparator<String> compareKeys) {
    // java: a key that is not a String is converted with String.valueOf, as a JavaScript
    // object converts its keys.
    List<String> rawKeys = new ArrayList<>();
    for (Object key : object.keySet()) {
      rawKeys.add(String.valueOf(key));
    }
    // java: the default order of Array.prototype.sort is the order of the UTF-16 code units,
    // which is the order of String.compareTo.
    Collections.sort(rawKeys, compareKeys);
    List<Object> keys = new ArrayList<Object>(rawKeys);
    return keys;
  }

  static String printListItems(List<?> list, Config config, String indentation, int depth, List<Object> refs) {
    String result = "";
    int length = list.size();
    if (length > 0) {
      result += config.spacingOuter;
      String indentationNext = indentation + config.indent;
      for (int i = 0; i < length; i++) {
        result += indentationNext;
        if (i == config.maxWidth) {
          result += "…";
          break;
        }
        result += printer(list.get(i), config, indentationNext, depth, refs, false);
        if (i < length - 1) {
          result += "," + config.spacingInner;
        } else if (!config.min) {
          result += ",";
        }
      }
      result += config.spacingOuter + indentation;
    }
    return result;
  }

  static String printObjectProperties(Map<?, ?> val, Config config, String indentation, int depth, List<Object> refs) {
    String result = "";
    List<Object> keys = getKeysOfEnumerableProperties(val, config.compareKeys);
    if (keys.size() > 0) {
      result += config.spacingOuter;
      String indentationNext = indentation + config.indent;
      for (int i = 0; i < keys.size(); i++) {
        Object key = keys.get(i);
        String name = printer(key, config, indentationNext, depth, refs, false);
        String value = printer(get(val, key), config, indentationNext, depth, refs, false);
        result += indentationNext + name + ": " + value;
        if (i < keys.size() - 1) {
          result += "," + config.spacingInner;
        } else if (!config.min) {
          result += ",";
        }
      }
      result += config.spacingOuter + indentation;
    }
    return result;
  }

  // java: helper with no counterpart upstream, the port of val[key] for the converted keys.
  private static Object get(Map<?, ?> val, Object key) {
    if (val.containsKey(key)) {
      return val.get(key);
    }
    for (Map.Entry<?, ?> entry : val.entrySet()) {
      if (key.equals(String.valueOf(entry.getKey()))) {
        return entry.getValue();
      }
    }
    return null;
  }

  private static String printNumber(double val) {
    return Double.compare(val, -0.0) == 0 ? "-0" : numberToString(val);
  }

  // java: helper with no counterpart upstream, the port of String(val) for a number: the
  // Number::toString algorithm of ECMAScript. Double.toString has another format, and before
  // JDK 19 it does not always give the shortest digits.
  private static String numberToString(double m) {
    if (Double.isNaN(m)) {
      return "NaN";
    }
    if (m == 0) {
      return "0";
    }
    if (m < 0) {
      return "-" + numberToString(-m);
    }
    if (Double.isInfinite(m)) {
      return "Infinity";
    }
    BigDecimal exact = new BigDecimal(m);
    BigDecimal shortest = null;
    for (int precision = 1; shortest == null; precision++) {
      BigDecimal down = exact.round(new MathContext(precision, RoundingMode.DOWN));
      BigDecimal up = exact.round(new MathContext(precision, RoundingMode.UP));
      boolean isDown = down.doubleValue() == m;
      boolean isUp = up.doubleValue() == m;
      if (isDown && isUp) {
        int order = exact.subtract(down).compareTo(up.subtract(exact));
        if (order == 0) {
          shortest = down.unscaledValue().testBit(0) ? up : down;
        } else {
          shortest = order < 0 ? down : up;
        }
      } else if (isDown) {
        shortest = down;
      } else if (isUp) {
        shortest = up;
      }
    }
    shortest = shortest.stripTrailingZeros();
    String s = shortest.unscaledValue().toString();
    int k = s.length();
    int n = k - shortest.scale();
    if (k <= n && n <= 21) {
      return s + repeat("0", n - k);
    }
    if (0 < n && n <= 21) {
      return s.substring(0, n) + "." + s.substring(n);
    }
    if (-6 < n && n <= 0) {
      return "0." + repeat("0", -n) + s;
    }
    String exponent = (n - 1 < 0 ? "-" : "+") + Math.abs(n - 1);
    if (k == 1) {
      return s + "e" + exponent;
    }
    return s.substring(0, 1) + "." + s.substring(1) + "e" + exponent;
  }

  // java: helper with no counterpart upstream, the port of String.prototype.repeat.
  private static String repeat(String string, int count) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < count; i++) {
      result.append(string);
    }
    return result.toString();
  }

  // java: helper with no counterpart upstream, the port of RegExp.prototype.toString. The
  // source is escaped as JavaScript escapes it: an empty source, a slash that is not in a
  // character class and the line terminators. The syntax of the Java pattern stays as it is.
  private static String regExpToString(Pattern val) {
    String pattern = val.pattern();
    StringBuilder source = new StringBuilder();
    boolean inCharClass = false;
    boolean previousCharWasBackslash = false;
    for (int i = 0; i < pattern.length(); i++) {
      char c = pattern.charAt(i);
      String escaped = null;
      if (c == '\n') {
        escaped = "\\n";
      } else if (c == '\r') {
        escaped = "\\r";
      } else if (c == ' ') {
        escaped = "\\u2028";
      } else if (c == ' ') {
        escaped = "\\u2029";
      }
      if (previousCharWasBackslash) {
        if (escaped != null) {
          source.setLength(source.length() - 1);
          source.append(escaped);
        } else {
          source.append(c);
        }
        previousCharWasBackslash = false;
        continue;
      }
      if (escaped != null) {
        source.append(escaped);
      } else if (c == '/' && !inCharClass) {
        source.append("\\/");
      } else {
        if (c == '[') {
          inCharClass = true;
        } else if (c == ']') {
          inCharClass = false;
        } else if (c == '\\') {
          previousCharWasBackslash = true;
        }
        source.append(c);
      }
    }
    if (source.length() == 0) {
      source.append("(?:)");
    }
    // java: toJsRegexFlags gives the flags in the order "ism", JavaScript prints them in
    // the alphabetical order.
    char[] flags = toJsRegexFlags(val).toCharArray();
    Arrays.sort(flags);
    return "/" + source + "/" + new String(flags);
  }

  private static String printBasicValue(Object val, boolean printFunctionName, boolean escapeRegex, boolean escapeString) {
    if (Boolean.TRUE.equals(val) || Boolean.FALSE.equals(val)) {
      return "" + val;
    }
    if (val == JestGetType.UNDEFINED) {
      return "undefined";
    }
    if (val == null) {
      return "null";
    }
    if (val instanceof Number) {
      // java: each Number is a JavaScript number, a double.
      return printNumber(((Number) val).doubleValue());
    }
    if (val instanceof String) {
      if (escapeString) {
        return "\"" + ((String) val).replaceAll("\"|\\\\", "\\\\$0") + "\"";
      }
      return "\"" + val + "\"";
    }
    if (val instanceof Date) {
      // java: a java.util.Date is never invalid, so there is no "Date { NaN }".
      return DateTimeFormatter.ISO_INSTANT.format(((Date) val).toInstant().truncatedTo(ChronoUnit.MILLIS)).replaceFirst("(:\\d\\d)Z$", "$1.000Z");
    }
    if (val instanceof Pattern) {
      if (escapeRegex) {
        return regExpToString((Pattern) val).replaceAll("[$()*+.?\\[\\\\\\]^{|}]", "\\\\$0");
      }
      return regExpToString((Pattern) val);
    }
    return null;
  }

  private static String printComplexValue(Object val, Config config, String indentation, int depth, List<Object> refs, boolean hasCalledToJSON) {
    // java: includes compares the objects by identity.
    for (Object ref : refs) {
      if (ref == val) {
        return "[Circular]";
      }
    }
    refs = new ArrayList<>(refs);
    refs.add(val);
    boolean hitMaxDepth = ++depth > config.maxDepth;
    boolean min = config.min;
    if (isArray(val)) {
      return hitMaxDepth ? "[Array]" : (min ? "" : !config.printBasicPrototype ? "" : "Array ") + "[" + printListItems(toList(val), config, indentation, depth, refs) + "]";
    }
    if (!(val instanceof Map)) {
      // java: a value that is not in the value model has no enumerable properties to print.
      return String.valueOf(val);
    }

    return hitMaxDepth ? "[Object]" : (min ? "" : !config.printBasicPrototype ? "" : "Object ") + "{" + printObjectProperties((Map<?, ?>) val, config, indentation, depth, refs) + "}";
  }

  // java: helper with no counterpart upstream, the items of a List or of an array.
  private static List<?> toList(Object val) {
    if (val instanceof List) {
      return (List<?>) val;
    }
    List<Object> list = new ArrayList<>();
    for (int i = 0; i < Array.getLength(val); i++) {
      list.add(Array.get(val, i));
    }
    return list;
  }

  private static String printer(Object val, Config config, String indentation, int depth, List<Object> refs, boolean hasCalledToJSON) {
    String basicResult = printBasicValue(val, config.printFunctionName, config.escapeRegex, config.escapeString);
    if (basicResult != null) {
      return basicResult;
    }
    return printComplexValue(val, config, indentation, depth, refs, hasCalledToJSON);
  }

  static final Options DEFAULT_OPTIONS = new Options();
  static {
    DEFAULT_OPTIONS.callToJSON = true;
    DEFAULT_OPTIONS.compareKeys = null;
    DEFAULT_OPTIONS.indent = 2;
    // java: an int has no infinity.
    DEFAULT_OPTIONS.maxDepth = Integer.MAX_VALUE;
    DEFAULT_OPTIONS.maxWidth = Integer.MAX_VALUE;
    DEFAULT_OPTIONS.min = false;
  }

  private static void validateOptions(Options options) {
    if (options.min != null && options.min && options.indent != null && options.indent != 0) {
      throw new PlaywrightException("pretty-format: Options \"min\" and \"indent\" cannot be used together.");
    }
  }

  // java: Options has no printFunctionName, escapeRegex and escapeString, the three getters
  // return the default values.
  private static boolean getPrintFunctionName(Options options) {
    return true;
  }

  private static boolean getEscapeRegex(Options options) {
    return false;
  }

  private static boolean getEscapeString(Options options) {
    return true;
  }

  private static Config getConfig(Options options) {
    Config config = new Config();
    config.callToJSON = options != null && options.callToJSON != null ? options.callToJSON : DEFAULT_OPTIONS.callToJSON;
    config.compareKeys = options != null && options.compareKeys != null ? options.compareKeys : DEFAULT_OPTIONS.compareKeys;
    config.escapeRegex = getEscapeRegex(options);
    config.escapeString = getEscapeString(options);
    config.indent = options != null && options.min != null && options.min ? "" : createIndent(options != null && options.indent != null ? options.indent : DEFAULT_OPTIONS.indent);
    config.maxDepth = options != null && options.maxDepth != null ? options.maxDepth : DEFAULT_OPTIONS.maxDepth;
    config.maxWidth = options != null && options.maxWidth != null ? options.maxWidth : DEFAULT_OPTIONS.maxWidth;
    config.min = options != null && options.min != null ? options.min : DEFAULT_OPTIONS.min;
    config.printBasicPrototype = true;
    config.printFunctionName = getPrintFunctionName(options);
    config.spacingInner = options != null && options.min != null && options.min ? " " : "\n";
    config.spacingOuter = options != null && options.min != null && options.min ? "" : "\n";
    return config;
  }

  private static String createIndent(int indent) {
    return repeat(" ", indent);
  }

  static String format(Object val, Options options) {
    if (options != null) {
      validateOptions(options);
    }
    String basicResult = printBasicValue(val, getPrintFunctionName(options), getEscapeRegex(options), getEscapeString(options));
    if (basicResult != null) {
      return basicResult;
    }
    return printComplexValue(val, getConfig(options), "", 0, new ArrayList<>(), false);
  }
}
