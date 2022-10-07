package com.microsoft.playwright.impl;

import com.microsoft.playwright.Locator;

import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.Utils.toJsRegexFlags;

public class LocatorUtils {

  static String escapeForTextSelector(Object text, boolean exact) {
    return escapeForTextSelector(text, exact, false);
  }
  static String escapeForTextSelector(Object param, boolean exact, boolean caseSensitive) {
    if (param instanceof Pattern) {
      Pattern pattern = (Pattern) param;
      return "/" + pattern.pattern() + "/" + toJsRegexFlags(pattern);
    }
    if (!(param instanceof String)) {
      throw new IllegalArgumentException("text parameter must be Pattern or String: " + param);
    }
    String text = (String) param;
    if (exact) {
      return '"' + text.replace("\"", "\\\"") + '"';
    }

    if (text.contains("\"") || text.contains(">>") || text.startsWith("/")) {
      return "/" + escapeForRegex(text).replaceAll("\\s+", "\\\\s+") + "/" + (caseSensitive ? "" : "i");
    }
    return text;
  }
  static String escapeForRegex(String text) {
    return text.replaceAll("[.*+?^>${}()|\\[\\]\\\\]", "\\\\\\\\$0");
  }

  static String getByTextSelector(String text, Locator.GetByTextOptions options) {
    boolean exact = options != null && options.exact != null && options.exact;
    return "text=" + escapeForTextSelector(text, exact);
  }

  static String getByTextSelector(Pattern text, Locator.GetByTextOptions options) {
    boolean exact = options != null && options.exact != null && options.exact;
    return "text=" + escapeForTextSelector(text, exact);
  }

  static String getByLabelSelector(Object text, Locator.GetByLabelOptions options) {
    boolean exact = options != null && options.exact != null && options.exact;
    return "internal:label=" + escapeForTextSelector(text, exact);
  }
}
