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
import static com.microsoft.playwright.impl.isomorphic.StringUtils.numberToString;

// java: the utils of a matcher. Upstream they are jest-matcher-utils, which is not ported: these methods print the
// Java values in the same format, without colors, and the diff is a plain line diff.
class MatcherUtils {
  // java: the undefined value: Frame.expect returns it for a received value that is undefined.
  static final Object UNDEFINED = new Object() {
    @Override
    public String toString() {
      return "undefined";
    }
  };

  private static final int MAX_DIFF_LINES = 2000;

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

  static String printDiffOrStringify(Object expected, Object received, String expectedLabel, String receivedLabel) {
    List<String> expectedLines = null;
    List<String> receivedLines = null;
    if (expected instanceof String && received instanceof String) {
      String expectedString = (String) expected;
      String receivedString = (String) received;
      if (!expectedString.isEmpty() && !receivedString.isEmpty() && (expectedString.contains("\n") || receivedString.contains("\n"))) {
        expectedLines = split(expectedString);
        receivedLines = split(receivedString);
      }
    } else if (isArray(expected) && isArray(received) || expected instanceof Map && received instanceof Map) {
      expectedLines = format(expected);
      receivedLines = format(received);
    }
    if (expectedLines != null && !expectedLines.equals(receivedLines) && expectedLines.size() <= MAX_DIFF_LINES && receivedLines.size() <= MAX_DIFF_LINES)
      return diffLines(expectedLines, receivedLines, expectedLabel, receivedLabel);
    int width = Math.max(expectedLabel.length(), receivedLabel.length());
    String expectedLine = pad(expectedLabel + ":", width + 2) + printExpected(expected);
    String receivedLine = pad(receivedLabel + ":", width + 2) + (stringify(expected).equals(stringify(received)) ? "serializes to the same string" : printReceived(received));
    return expectedLine + "\n" + receivedLine;
  }

  private static String diffLines(List<String> a, List<String> b, String aLabel, String bLabel) {
    int[][] lcs = new int[a.size() + 1][b.size() + 1];
    for (int i = a.size() - 1; i >= 0; i--) {
      for (int j = b.size() - 1; j >= 0; j--)
        lcs[i][j] = a.get(i).equals(b.get(j)) ? lcs[i + 1][j + 1] + 1 : Math.max(lcs[i + 1][j], lcs[i][j + 1]);
    }
    List<String> lines = new ArrayList<>();
    int removed = 0;
    int added = 0;
    int i = 0;
    int j = 0;
    while (i < a.size() || j < b.size()) {
      if (i < a.size() && j < b.size() && a.get(i).equals(b.get(j))) {
        lines.add(a.get(i).isEmpty() ? "" : "  " + a.get(i));
        i++;
        j++;
      } else if (i < a.size() && (j == b.size() || lcs[i + 1][j] >= lcs[i][j + 1])) {
        lines.add(a.get(i).isEmpty() ? "-" : "- " + a.get(i));
        removed++;
        i++;
      } else {
        lines.add(b.get(j).isEmpty() ? "+" : "+ " + b.get(j));
        added++;
        j++;
      }
    }
    int labelWidth = Math.max(aLabel.length(), bLabel.length());
    int countWidth = Math.max(String.valueOf(removed).length(), String.valueOf(added).length());
    return "- " + pad(aLabel, labelWidth) + "  - " + padStart(String.valueOf(removed), countWidth) + "\n"
      + "+ " + pad(bLabel, labelWidth) + "  + " + padStart(String.valueOf(added), countWidth) + "\n\n"
      + String.join("\n", lines);
  }

  private static List<String> format(Object value) {
    List<String> lines = new ArrayList<>();
    format(value, "", "", lines);
    String last = lines.get(lines.size() - 1);
    lines.set(lines.size() - 1, last.substring(0, last.length() - 1));
    return lines;
  }

  private static void format(Object value, String indent, String key, List<String> lines) {
    if (isArray(value)) {
      List<Object> items = toList(value);
      if (items.isEmpty()) {
        lines.add(indent + key + "Array [],");
        return;
      }
      lines.add(indent + key + "Array [");
      for (Object item : items)
        format(item, indent + "  ", "", lines);
      lines.add(indent + "],");
      return;
    }
    if (value instanceof Map) {
      Map<String, Object> entries = sortedEntries((Map<?, ?>) value);
      if (entries.isEmpty()) {
        lines.add(indent + key + "Object {},");
        return;
      }
      lines.add(indent + key + "Object {");
      for (Map.Entry<String, Object> entry : entries.entrySet())
        format(entry.getValue(), indent + "  ", quote(entry.getKey()) + ": ", lines);
      lines.add(indent + "},");
      return;
    }
    lines.add(indent + key + formatBasicValue(value) + ",");
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

  private static List<String> split(String value) {
    List<String> result = new ArrayList<>();
    for (String line : value.split("\n", -1))
      result.add(line);
    return result;
  }

  private static String pad(String value, int width) {
    StringBuilder result = new StringBuilder(value);
    while (result.length() < width)
      result.append(' ');
    return result.toString();
  }

  private static String padStart(String value, int width) {
    StringBuilder result = new StringBuilder(value);
    while (result.length() < width)
      result.insert(0, ' ');
    return result.toString();
  }
}
