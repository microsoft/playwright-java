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

package com.microsoft.playwright.impl;

import java.lang.reflect.Array;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.Utils.toJsRegexFlags;

// java: the utils of a matcher. Upstream they are jest-matcher-utils, which is not ported: these methods print the
// Java values in the same format, without colors, and there is no diff.
class MatcherUtils {
  // java: the undefined value: Frame.expect returns it for a received value that is undefined.
  static final Object UNDEFINED = new Object() {
    @Override
    public String toString() {
      return "undefined";
    }
  };

  static String printExpected(Object value) {
    return stringify(value);
  }

  static String printReceived(Object value) {
    return stringify(value);
  }

  static String stringify(Object value) {
    if (isArray(value)) {
      List<String> items = new ArrayList<>();
      for (Object item : toList(value))
        items.add(stringify(item));
      return "[" + String.join(", ", items) + "]";
    }
    if (value instanceof Map) {
      List<String> entries = new ArrayList<>();
      for (Map.Entry<String, Object> entry : sortedEntries((Map<?, ?>) value).entrySet())
        entries.add(quote(entry.getKey()) + ": " + stringify(entry.getValue()));
      return "{" + String.join(", ", entries) + "}";
    }
    return formatBasicValue(value);
  }

  // java: where upstream prints a diff (printDiffOrStringify of jest), both values are printed in full.
  static String[] printExpectedAndReceived(Object expected, Object received, String expectedLabel, String receivedLabel) {
    int width = Math.max(expectedLabel.length(), receivedLabel.length());
    String printedExpected = printExpected(expected);
    String printedReceived = printReceived(received);
    return new String[] {
      pad(expectedLabel + ":", width + 2) + printedExpected,
      pad(receivedLabel + ":", width + 2) + (printedExpected.equals(printedReceived) ? "serializes to the same string" : printedReceived)
    };
  }

  private static Map<String, Object> sortedEntries(Map<?, ?> map) {
    Map<String, Object> entries = new TreeMap<>();
    for (Map.Entry<?, ?> entry : map.entrySet())
      entries.put(String.valueOf(entry.getKey()), entry.getValue());
    return entries;
  }

  private static String formatBasicValue(Object value) {
    if (value == null)
      return "null";
    if (value instanceof Number)
      return numberToString(((Number) value).doubleValue());
    if (value instanceof String)
      return quote((String) value);
    if (value instanceof Pattern)
      return "/" + ((Pattern) value).pattern() + "/" + toJsRegexFlags((Pattern) value);
    if (value instanceof Date)
      return DateTimeFormatter.ISO_INSTANT.format(((Date) value).toInstant().truncatedTo(ChronoUnit.MILLIS)).replaceFirst("(:\\d\\d)Z$", "$1.000Z");
    return String.valueOf(value);
  }

  static String numberToString(double value) {
    if (Double.isNaN(value))
      return "NaN";
    if (Double.isInfinite(value))
      return value > 0 ? "Infinity" : "-Infinity";
    if (value == Math.rint(value) && Math.abs(value) < 1e15)
      return String.valueOf((long) value);
    return Double.toString(value);
  }

  private static String quote(String value) {
    return "\"" + value.replaceAll("\"|\\\\", "\\\\$0") + "\"";
  }

  private static boolean isArray(Object value) {
    return value instanceof List || value != null && value.getClass().isArray();
  }

  @SuppressWarnings("unchecked")
  private static List<Object> toList(Object value) {
    if (value instanceof List)
      return (List<Object>) value;
    List<Object> result = new ArrayList<>();
    for (int i = 0; i < Array.getLength(value); i++)
      result.add(Array.get(value, i));
    return result;
  }

  private static String pad(String value, int width) {
    StringBuilder result = new StringBuilder(value);
    while (result.length() < width)
      result.append(' ');
    return result.toString();
  }

}
