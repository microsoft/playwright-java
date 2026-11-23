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
import com.google.gson.GsonBuilder;
import com.microsoft.playwright.PlaywrightException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.Utils.toJsRegexFlags;

// java: a string | RegExp parameter is an Object holding a String or a Pattern.
public final class StringUtils {
  private StringUtils() {
  }

  public static String escapeWithQuotes(String text) {
    return escapeWithQuotes(text, "'");
  }

  // NOTE: this function should not be used to escape any selectors.
  public static String escapeWithQuotes(String text, String ch) {
    String stringified = jsonStringify(text);
    String escapedText = stringified.substring(1, stringified.length() - 1).replace("\\\"", "\"");
    if (ch.equals("'"))
      return ch + escapedText.replace("'", "\\'") + ch;
    if (ch.equals("\""))
      return ch + escapedText.replace("\"", "\\\"") + ch;
    if (ch.equals("`"))
      return ch + escapedText.replace("`", "\\`") + ch;
    throw new PlaywrightException("Invalid escape char");
  }

  public static String escapeTemplateString(String text) {
    return text
        .replace("\\", "\\\\")
        .replace("`", "\\`")
        .replace("${", "\\${");
  }

  // java: a String object is a Java String.
  public static boolean isString(Object obj) {
    return obj instanceof String;
  }

  public static String toTitleCase(String name) {
    return name.isEmpty() ? "" : Character.toUpperCase(name.charAt(0)) + name.substring(1);
  }

  public static String toSnakeCase(String name) {
    // E.g. ignoreHTTPSErrors => ignore_https_errors.
    return name.replaceAll("([a-z0-9])([A-Z])", "$1_$2").replaceAll("([A-Z])([A-Z][a-z])", "$1_$2").toLowerCase();
  }

  public static String formatObject(Object value) {
    return formatObject(value, "  ", "multiline");
  }

  public static String formatObject(Object value, String indent) {
    return formatObject(value, indent, "multiline");
  }

  // java: an array is a List or an Object[], an object is a Map with its keys converted to strings; a null value of
  // a Map is undefined. String(value) of a number is String.valueOf(), which prints a Double with a fraction.
  public static String formatObject(Object value, String indent, String mode) {
    if (value == null)
      return "null";
    if (value instanceof String)
      return escapeWithQuotes((String) value, "'");
    if (value instanceof List || value instanceof Object[]) {
      List<?> array = value instanceof List ? (List<?>) value : Arrays.asList((Object[]) value);
      List<String> items = new ArrayList<>();
      for (Object o : array)
        items.add(formatObject(o));
      return "[" + String.join(", ", items) + "]";
    }
    if (value instanceof Map) {
      Map<String, Object> object = new LinkedHashMap<>();
      for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet())
        object.put(String.valueOf(entry.getKey()), entry.getValue());
      List<String> keys = new ArrayList<>();
      for (String key : object.keySet()) {
        if (!key.equals("timeout") && object.get(key) != null)
          keys.add(key);
      }
      Collections.sort(keys);
      if (keys.isEmpty())
        return "{}";
      List<String> tokens = new ArrayList<>();
      for (String key : keys)
        tokens.add(key + ": " + formatObject(object.get(key)));
      if (mode.equals("multiline")) {
        List<String> lines = new ArrayList<>();
        for (String t : tokens)
          lines.add(indent + t);
        return "{\n" + String.join(",\n", lines) + "\n}";
      }
      return "{ " + String.join(", ", tokens) + " }";
    }
    return String.valueOf(value);
  }

  public static String formatObjectOrVoid(Object value) {
    return formatObjectOrVoid(value, "  ");
  }

  public static String formatObjectOrVoid(Object value, String indent) {
    String result = formatObject(value, indent);
    return result.equals("{}") ? "" : result;
  }

  public static String quoteCSSAttributeValue(String text) {
    return "\"" + text.replaceAll("([\"\\\\])", "\\\\$1") + "\"";
  }

  private static Map<String, String> normalizedWhitespaceCache;

  public static void cacheNormalizedWhitespaces() {
    normalizedWhitespaceCache = new HashMap<>();
  }

  // java: JS_WHITESPACE is the \s of a JS RegExp, which trim() also removes.
  public static String normalizeWhiteSpace(String text) {
    String result = normalizedWhitespaceCache != null ? normalizedWhitespaceCache.get(text) : null;
    if (result == null) {
      result = text.replaceAll("[​­]", "").replaceAll("^" + JS_WHITESPACE + "+|" + JS_WHITESPACE + "+$", "").replaceAll(JS_WHITESPACE + "+", " ");
      if (normalizedWhitespaceCache != null)
        normalizedWhitespaceCache.put(text, result);
    }
    return result;
  }

  public static String normalizeEscapedRegexQuotes(String source) {
    // This function reverses the effect of escapeRegexForSelector below.
    // Odd number of backslashes followed by the quote -> remove unneeded backslash.
    return source.replaceAll("(^|[^\\\\])(\\\\\\\\)*\\\\(['\"`])", "$1$2$3");
  }

  // java: toJsRegexFlags() never produces the 'u' or 'v' flag, so there is no unicode early return.
  private static String escapeRegexForSelector(Pattern re) {
    // Unicode mode does not allow "identity character escapes", so we do not escape and
    // hope that it does not contain quotes and/or >> signs.
    // https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Regular_expressions/Character_escape
    // TODO: rework RE usages in internal selectors away from literal representation to json, e.g. {source,flags}.
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

  public static String trimString(String input, int cap) {
    return trimString(input, cap, "");
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

  public static String truncateDataUrl(String url) {
    // Data URLs can carry megabytes of base64 payload, which is never useful in
    // human/AI-facing output. Keep the media type prefix for context, drop the data.
    if (!url.startsWith("data:"))
      return url;
    int comma = url.indexOf(',');
    if (comma == -1)
      return url;
    return url.substring(0, comma + 1) + "…";
  }

  public static String escapeRegExp(String s) {
    // From https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Regular_Expressions#escaping
    // java: $0 is $&.
    return s.replaceAll("[.*+?^${}()|\\[\\]\\\\]", "\\\\$0"); // $& means the whole matched string
  }

  private static final Map<String, String> escaped = new LinkedHashMap<>();
  static {
    escaped.put("&", "&amp;");
    escaped.put("<", "&lt;");
    escaped.put(">", "&gt;");
    escaped.put("\"", "&quot;");
    escaped.put("'", "&#39;");
  }

  private static String escapeHTMLAttribute(String s) {
    return replaceEach(s, Pattern.compile("[&<>\"']"));
  }

  public static String escapeHTML(String s) {
    return replaceEach(s, Pattern.compile("[&<]"));
  }

  public static class HtmlBuilder {
    private final List<String> chunks = new ArrayList<>();

    public void text(String text) {
      chunks.add(escapeHTML(text));
    }

    public boolean openTag(String name) {
      if (!isSafeName(name)) {
        return false;
      }
      chunks.add("<");
      chunks.add(name);
      return true;
    }

    public void attribute(String name, String value) {
      if (!isSafeName(name)) {
        return;
      }
      chunks.add(" ");
      chunks.add(name);
      chunks.add("=\"");
      chunks.add(escapeHTMLAttribute(value));
      chunks.add("\"");
    }

    public void endOpenTag() {
      chunks.add(">");
    }

    public void closeTag(String name) {
      chunks.add("</");
      chunks.add(name);
      chunks.add(">");
    }

    @Override
    public String toString() {
      return String.join("", chunks);
    }
  }

  private static boolean isSafeName(String name) {
    return !name.isEmpty() && !Pattern.compile(JS_WHITESPACE + "|[=<>/\"']").matcher(name).find();
  }

  public static String longestCommonSubstring(String s1, String s2) {
    int n = s1.length();
    int m = s2.length();
    int maxLen = 0;
    int endingIndex = 0;

    // Initialize a 2D array with zeros
    int[][] dp = new int[n + 1][m + 1];

    // Build the dp table
    for (int i = 1; i <= n; i++) {
      for (int j = 1; j <= m; j++) {
        if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
          dp[i][j] = dp[i - 1][j - 1] + 1;

          if (dp[i][j] > maxLen) {
            maxLen = dp[i][j];
            endingIndex = i;
          }
        }
      }
    }

    // Extract the longest common substring
    return s1.substring(endingIndex - maxLen, endingIndex);
  }

  // java: the 'i', 's' and 'm' flags become Pattern flags, the others are dropped.
  public static Pattern parseRegex(String regex) {
    if (regex.isEmpty() || regex.charAt(0) != '/')
      throw new PlaywrightException("Invalid regex, must start with '/': " + regex);
    int lastSlash = regex.lastIndexOf('/');
    if (lastSlash <= 0)
      throw new PlaywrightException("Invalid regex, must end with '/' followed by optional flags: " + regex);
    String source = regex.substring(1, lastSlash);
    String flags = regex.substring(lastSlash + 1);
    return Pattern.compile(source, fromJsRegexFlags(flags));
  }

  public static String tomlBasicString(String value) {
    // JSON.stringify produces a valid TOML basic string: escapes \", \\, \n, \r, \t and uses \\uXXXX for control chars.
    return jsonStringify(value);
  }

  public static String tomlArray(List<String> values) {
    List<String> items = new ArrayList<>();
    for (String value : values)
      items.add(tomlBasicString(value));
    return "[" + String.join(", ", items) + "]";
  }

  public static String tomlMultilineBasicString(String value) {
    // Triple-quoted basic string: escape backslashes first, then any literal """ sequences.
    String escaped = value.replace("\\", "\\\\").replace("\"\"\"", "\\\"\\\"\\\"");
    return "\"\"\"\n" + escaped + "\n\"\"\"";
  }

  // Semicolons removed from [[\]()#;?] to avoid polynomial backtracking
  // when both that group and (?:;...)* can match runs of semicolons.
  // \d{1,4} relaxed to \d{0,4} so empty params (e.g. ESC[;H) still match.
  // java: the '[' in the first character class is escaped, a Java character class nests an unescaped one.
  public static final Pattern ansiRegex = Pattern.compile("([\\u001B\\u009B][\\[\\]()#?]*(?:(?:(?:[a-zA-Z\\d]*(?:;[-a-zA-Z\\d\\/#&.:=?%@~_]*)*)?\\u0007)|(?:(?:\\d{0,4}(?:;\\d{0,4})*)?[\\dA-PR-TZcf-ntqry=><~])))");

  public static String stripAnsiEscapes(String str) {
    return ansiRegex.matcher(str).replaceAll("");
  }

  // java: helpers with no counterpart upstream.

  private static final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

  // java: JSON.stringify of a string; unlike it, Gson escapes U+2028 and U+2029.
  public static String jsonStringify(String value) {
    return gson.toJson(value);
  }

  // java: String(re) of a JS RegExp.
  private static String toJsRegExp(Pattern pattern) {
    return "/" + pattern.pattern() + "/" + toJsRegexFlags(pattern);
  }

  // java: the \s character class of a JS RegExp.
  private static final String JS_WHITESPACE = "[\\t\\n\\u000B\\f\\r \\u00a0\\u1680\\u2000-\\u200a\\u2028\\u2029\\u202f\\u205f\\u3000\\ufeff]";

  // java: String.prototype.replace with a global RegExp and a function that looks the match up in escaped.
  private static String replaceEach(String s, Pattern pattern) {
    Matcher matcher = pattern.matcher(s);
    StringBuffer result = new StringBuffer();
    while (matcher.find())
      matcher.appendReplacement(result, Matcher.quoteReplacement(escaped.get(matcher.group())));
    matcher.appendTail(result);
    return result.toString();
  }

  // java: Utils.fromJsRegexFlags is not visible from this package.
  private static int fromJsRegexFlags(String regexFlags) {
    int flags = 0;
    if (regexFlags.contains("i"))
      flags |= Pattern.CASE_INSENSITIVE;
    if (regexFlags.contains("s"))
      flags |= Pattern.DOTALL;
    if (regexFlags.contains("m"))
      flags |= Pattern.MULTILINE;
    return flags;
  }
}
