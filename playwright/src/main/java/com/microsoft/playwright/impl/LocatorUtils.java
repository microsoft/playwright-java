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

// Port of isomorphic/locatorUtils.ts.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.AriaRole;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.Utils.toJsRegexFlags;

public final class LocatorUtils {
  // java: each getBy* method has its own options class, so the exact flag is passed instead of the options.
  private static String getByAttributeTextSelector(String attrName, Object text, Boolean exact) {
    return "internal:attr=[" + attrName + "=" + escapeForAttributeSelector(text, exact != null && exact) + "]";
  }

  // Multiple test id attribute names can be joined with a comma. Attribute names cannot contain commas.
  static String encodeTestIdAttributeName(String testIdAttributeName) {
    return testIdAttributeName.contains(",") ? gson().toJson(testIdAttributeName) : testIdAttributeName;
  }

  // java: the test id attribute name lives in SelectorsImpl of the Playwright instance, not in a module variable.
  static String getByTestIdSelector(Object testId, PlaywrightImpl playwright) {
    String testIdAttributeName = playwright.selectors.testIdAttributeName;
    return "internal:testid=[" + encodeTestIdAttributeName(testIdAttributeName) + "=" + escapeForAttributeSelector(testId, true) + "]";
  }

  static String getByLabelSelector(Object text, Locator.GetByLabelOptions options) {
    return "internal:label=" + escapeForTextSelector(text, options != null && options.exact != null && options.exact);
  }

  static String getByAltTextSelector(Object text, Locator.GetByAltTextOptions options) {
    return getByAttributeTextSelector("alt", text, options == null ? null : options.exact);
  }

  static String getByTitleSelector(Object text, Locator.GetByTitleOptions options) {
    return getByAttributeTextSelector("title", text, options == null ? null : options.exact);
  }

  static String getByPlaceholderSelector(Object text, Locator.GetByPlaceholderOptions options) {
    return getByAttributeTextSelector("placeholder", text, options == null ? null : options.exact);
  }

  static String getByTextSelector(Object text, Locator.GetByTextOptions options) {
    return "internal:text=" + escapeForTextSelector(text, options != null && options.exact != null && options.exact);
  }

  // java: the role is an AriaRole enum, its lower-cased name is the upstream role string.
  static String getByRoleSelector(AriaRole role, Locator.GetByRoleOptions options) {
    if (options == null)
      options = new Locator.GetByRoleOptions();
    List<String[]> props = new ArrayList<>();
    if (options.checked != null)
      props.add(new String[] {"checked", String.valueOf(options.checked)});
    if (options.disabled != null)
      props.add(new String[] {"disabled", String.valueOf(options.disabled)});
    if (options.selected != null)
      props.add(new String[] {"selected", String.valueOf(options.selected)});
    if (options.expanded != null)
      props.add(new String[] {"expanded", String.valueOf(options.expanded)});
    if (options.includeHidden != null)
      props.add(new String[] {"include-hidden", String.valueOf(options.includeHidden)});
    if (options.level != null)
      props.add(new String[] {"level", String.valueOf(options.level)});
    if (options.name != null)
      props.add(new String[] {"name", escapeForAttributeSelector(options.name, options.exact != null && options.exact)});
    if (options.description != null)
      props.add(new String[] {"description", escapeForAttributeSelector(options.description, options.exact != null && options.exact)});
    if (options.pressed != null)
      props.add(new String[] {"pressed", String.valueOf(options.pressed)});
    StringBuilder result = new StringBuilder("internal:role=" + role.name().toLowerCase());
    for (String[] prop : props)
      result.append("[").append(prop[0]).append("=").append(prop[1]).append("]");
    return result.toString();
  }

  // java: ports of the isomorphic/stringUtils.ts escapes that the selectors above use.
  // java: toJsRegexFlags() never produces the 'u' or 'v' flag, so there is no unicode early return.
  private static String escapeRegexForSelector(Pattern re) {
    // Even number of backslashes followed by the quote -> insert a backslash.
    return toJsRegExp(re).replaceAll("(^|[^\\\\])(\\\\\\\\)*([\"'`])", "$1$2\\\\$3").replaceAll(">>", "\\\\>\\\\>");
  }

  static String escapeForTextSelector(Object text, boolean exact) {
    if (text instanceof Pattern)
      return escapeRegexForSelector((Pattern) text);
    if (text instanceof String)
      return gson().toJson(text) + (exact ? "s" : "i");
    throw new IllegalArgumentException("text parameter must be Pattern or String: " + text);
  }

  // java: an exact match has no suffix here, upstream appends 's'; kept as the old Java client sent it.
  private static String escapeForAttributeSelector(Object value, boolean exact) {
    if (value instanceof Pattern)
      return escapeRegexForSelector((Pattern) value);
    if (value instanceof String) {
      // TODO: this should actually be
      //   cssEscape(value).replace(/\\ /g, ' ')
      // However, our attribute selectors do not conform to CSS parsing spec,
      // so we escape them differently.
      return '"' + ((String) value).replaceAll("\\\\", "\\\\\\\\").replaceAll("\"", "\\\\\"") + '"' + (exact ? "" : "i");
    }
    throw new IllegalArgumentException("Attribute can be String or Pattern, found: " + value);
  }

  // java: String(re) of a JS RegExp.
  private static String toJsRegExp(Pattern pattern) {
    return "/" + pattern.pattern() + "/" + toJsRegexFlags(pattern);
  }
}
