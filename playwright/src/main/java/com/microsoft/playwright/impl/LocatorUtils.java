package com.microsoft.playwright.impl;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;

import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.Utils.toJsRegexFlags;

public class LocatorUtils {
  private static volatile String testIdAttributeName = "data-testid";;

  static void setTestIdAttributeName(String name) {
    testIdAttributeName = name;
  }

  static String getByTextSelector(Object text, Locator.GetByTextOptions options) {
    boolean exact = options != null && options.exact != null && options.exact;
    return "internal:text=" + escapeForTextSelector(text, exact);
  }

  static String getByLabelSelector(Object text, Locator.GetByLabelOptions options) {
    boolean exact = options != null && options.exact != null && options.exact;
    return "internal:label=" + escapeForTextSelector(text, exact);
  }

  private static String getByAttributeTextSelector(String attrName, Object value, boolean exact) {
    return "internal:attr=[" + attrName + "=" + escapeForAttributeSelector(value, exact) + "]";
  }

  static String getByTestIdSelector(Object testId) {
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

  private static void addAttr(StringBuilder result, String name, String value) {
    result.append("[").append(name).append("=").append(value).append("]");
  }

  static String getByRoleSelector(AriaRole role, Locator.GetByRoleOptions options) {
    StringBuilder result = new StringBuilder();
    result.append("internal:role=").append(role.name().toLowerCase());
    if (options != null) {
      if (options.checked != null)
        addAttr(result, "checked", options.checked.toString());
      if (options.disabled != null)
        addAttr(result, "disabled", options.disabled.toString());
      if (options.selected != null)
        addAttr(result, "selected", options.selected.toString());
      if (options.expanded != null)
        addAttr(result, "expanded", options.expanded.toString());
      if (options.includeHidden != null)
        addAttr(result, "include-hidden", options.includeHidden.toString());
      if (options.level != null)
        addAttr(result, "level", options.level.toString());
      if (options.name != null) {
        String name = escapeForAttributeSelector(options.name, options.exact != null && options.exact);
        addAttr(result, "name", name);
      }
      if (options.pressed != null)
        addAttr(result, "pressed", options.pressed.toString());
    }
    return result.toString();
  }

  private static String escapeRegexForSelector(Pattern re) {
    // Even number of backslashes followed by the quote -> insert a backslash.
    return toJsRegExp(re).replaceAll("(^|[^\\\\])(\\\\\\\\)*([\"'`])", "$1$2\\\\$3").replaceAll(">>", "\\\\>\\\\>");
  }

  static String escapeForTextSelector(Object value, boolean exact) {
    if (value instanceof Pattern) {
      return escapeRegexForSelector((Pattern) value);
    }
    if (value instanceof String) {
      return gson().toJson(value) + (exact ? "s" : "i");
    }
    throw new IllegalArgumentException("text parameter must be Pattern or String: " + value);
  }

  private static String escapeForAttributeSelector(Object value, boolean exact) {
    if (value instanceof Pattern) {
      return escapeRegexForSelector((Pattern) value);
    }
    if (value instanceof String) {
      // TODO: this should actually be
      //   cssEscape(value).replace(/\\ /g, ' ')
      // However, our attribute selectors do not conform to CSS parsing spec,
      // so we escape them differently.
      return '"' + ((String) value).replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"") + '"' + (exact ? "" : "i");
    }
    throw new IllegalArgumentException("Attribute can be String or Pattern, found: " + value);
  }

  private static String toJsRegExp(Pattern pattern) {
    return "/" + pattern.pattern() + "/" + toJsRegexFlags(pattern);
  }
}
