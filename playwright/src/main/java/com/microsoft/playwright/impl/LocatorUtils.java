package com.microsoft.playwright.impl;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;

import java.util.regex.Pattern;

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
    if (value instanceof Pattern) {
      return "internal:attr=[" + attrName + "=" + toJsRegExp((Pattern) value) + "]";
    }
    return "internal:attr=[" + attrName + "=" + escapeForAttributeSelector((String) value, exact) + "]";
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
        String name;
        if (options.name instanceof String) {
          name = escapeForAttributeSelector((String) options.name, options.exact != null && options.exact);
        } else if (options.name instanceof Pattern) {
          name = toJsRegExp((Pattern) options.name);
        } else {
          throw new IllegalArgumentException("options.name can be String or Pattern, found: " + options.name);
        }
        addAttr(result, "name", name);
      }
      if (options.pressed != null)
        addAttr(result, "pressed", options.pressed.toString());
    }
    return result.toString();
  }

  static String escapeForTextSelector(Object text, boolean exact) {
    return escapeForTextSelector(text, exact, false);
  }

  private static String escapeForTextSelector(Object param, boolean exact, boolean caseSensitive) {
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

  private static String escapeForRegex(String text) {
    return text.replaceAll("[.*+?^>${}()|\\[\\]\\\\]", "\\\\\\\\$0");
  }

  private static String escapeForAttributeSelector(String value, boolean exact) {
    // TODO: this should actually be
    //   cssEscape(value).replace(/\\ /g, ' ')
    // However, our attribute selectors do not conform to CSS parsing spec,
    // so we escape them differently.
    return '"' + value.replaceAll("\"", "\\\\\"") + '"' + (exact ? "" : "i");
  }

  private static String toJsRegExp(Pattern pattern) {
    return "/" + pattern.pattern() + "/" + toJsRegexFlags(pattern);
  }
}
