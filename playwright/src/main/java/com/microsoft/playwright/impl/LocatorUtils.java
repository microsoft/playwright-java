package com.microsoft.playwright.impl;

import com.microsoft.playwright.Locator;

import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.Utils.toJsRegexFlags;

public class LocatorUtils {

  private static String testIdAttributeName = "data-testid";;

  static synchronized void setTestIdAttributeName(String name) {
    testIdAttributeName = name;
  }

  static String escapeForTextSelector(Object text, boolean exact) {
    return escapeForTextSelector(text, exact, false);
  }
  static String escapeForTextSelector(Object param, boolean exact, boolean caseSensitive) {
    if (param instanceof Pattern) {
      return toJsRegExp((Pattern) param);
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

  private static String escapeForAttributeSelector(String value, boolean exact) {
    // TODO: this should actually be
    //   cssEscape(value).replace(/\\ /g, ' ')
    // However, our attribute selectors do not conform to CSS parsing spec,
    // so we escape them differently.
    return '"' + value.replaceAll("\"", "\\\\\"") + '"' + (exact ? "" : "i");
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

  private static String getByAttributeTextSelector(String attrName, Object value, boolean exact) {
    if (value instanceof Pattern) {
      return "internal:attr=[" + attrName + "=" + toJsRegExp((Pattern) value) + "]";
    }
    return "internal:attr=[" + attrName + "=" + escapeForAttributeSelector((String) value, exact) + "]";
  }

  static String getByTestIdSelector(String testId) {
    return getByAttributeTextSelector(testIdAttributeName, testId, true);
  }

  static String getByAltTextSelector(Object text, Locator.GetByAltTextOptions options) {
    boolean exact = options != null && options.exact != null && options.exact;
    return getByAttributeTextSelector("alt", text, exact);
  }

  static String getByTitleSelector(Object text, Locator.GetByTitleOptions options) {
    boolean exact = options != null && options.exact != null && options.exact;
    return getByAttributeTextSelector("title", text, exact);
  }

  static String getByPlaceholderSelector(Object text, Locator.GetByPlaceholderOptions options) {
    boolean exact = options != null && options.exact != null && options.exact;
    return getByAttributeTextSelector("placeholder", text, exact);
  }

  private static String toJsRegExp(Pattern pattern) {
    return "/" + pattern.pattern() + "/" + toJsRegexFlags(pattern);
  }
}
