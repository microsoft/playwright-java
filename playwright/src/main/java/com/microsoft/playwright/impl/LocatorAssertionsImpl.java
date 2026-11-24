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

// Port of matchers.ts.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.impl.protocol.ExpectedTextValue;
import com.microsoft.playwright.options.AriaRole;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.ToBeTruthy.toBeTruthy;
import static com.microsoft.playwright.impl.ToEqual.toEqual;
import static com.microsoft.playwright.impl.ToMatchAriaSnapshot.toMatchAriaSnapshot;
import static com.microsoft.playwright.impl.ToMatchText.toMatchText;
import static com.microsoft.playwright.impl.Utils.toJsRegexFlags;

// java: the matchers of a Locator; those of a Page are in PageAssertionsImpl, toBeOK is in APIResponseAssertionsImpl.
// A matcher is the method of the public interface: it calls its upstream body through callMatcherAsStep, and its
// name in a message and in a title is the Java name (hasText for toHaveText). The
// options are read as options?.name, a matcher with a string, a pattern or an array has one overload per type.
public class LocatorAssertionsImpl extends AssertionsBase implements LocatorAssertions {
  final LocatorImpl actualLocator;

  public LocatorAssertionsImpl(Locator locator) {
    this(locator, false);
  }

  private LocatorAssertionsImpl(Locator locator, boolean isNot) {
    super(isNot);
    this.actualLocator = (LocatorImpl) locator;
  }

  @Override
  public LocatorAssertions not() {
    return new LocatorAssertionsImpl(actualLocator, !isNot);
  }

  // java: items is an array of String or Pattern, the options are three parameters.
  static List<ExpectedTextValue> serializeExpectedTextValues(Object[] items, Boolean matchSubstring, Boolean normalizeWhiteSpace, Boolean ignoreCase) {
    List<ExpectedTextValue> result = new ArrayList<>();
    for (Object i : items) {
      ExpectedTextValue value = new ExpectedTextValue();
      value.string = i instanceof String ? (String) i : null;
      value.regexSource = i instanceof Pattern ? ((Pattern) i).pattern() : null;
      value.regexFlags = i instanceof Pattern ? toJsRegexFlags((Pattern) i) : null;
      value.matchSubstring = matchSubstring;
      value.ignoreCase = ignoreCase;
      value.normalizeWhiteSpace = normalizeWhiteSpace;
      result.add(value);
    }
    return result;
  }

  @Override
  public void isAttached(IsAttachedOptions options) {
    callMatcherAsStep("isAttached", state -> {
      boolean attached = options == null || options.attached == null || options.attached;
      String expected = attached ? "attached" : "detached";
      String arg = attached ? "" : "{ attached: false }";
      return toBeTruthy(state, "isAttached", actualLocator, "Locator", expected, arg, (isNot, timeout) -> {
        FrameExpectOptions expectOptions = new FrameExpectOptions();
        expectOptions.isNot = isNot;
        expectOptions.timeout = timeout;
        expectOptions.title = state.title;
        return actualLocator.expect(attached ? "to.be.attached" : "to.be.detached", expectOptions);
      }, options == null ? null : options.timeout);
    });
  }

  @Override
  public void isChecked(IsCheckedOptions options) {
    callMatcherAsStep("isChecked", state -> {
      Boolean checked = options == null ? null : options.checked;
      Boolean indeterminate = options == null ? null : options.indeterminate;
      // java: an undefined property is a missing key.
      Map<String, Boolean> expectedValue = new LinkedHashMap<>();
      if (checked != null)
        expectedValue.put("checked", checked);
      if (indeterminate != null)
        expectedValue.put("indeterminate", indeterminate);
      String expected;
      String arg;
      if (options != null && options.indeterminate != null && options.indeterminate) {
        expected = "indeterminate";
        arg = "{ indeterminate: true }";
      } else {
        expected = options != null && options.checked != null && !options.checked ? "unchecked" : "checked";
        arg = options != null && options.checked != null && !options.checked ? "{ checked: false }" : "";
      }
      return toBeTruthy(state, "isChecked", actualLocator, "Locator", expected, arg, (isNot, timeout) -> {
        FrameExpectOptions expectOptions = new FrameExpectOptions();
        expectOptions.isNot = isNot;
        expectOptions.timeout = timeout;
        expectOptions.expectedValue = expectedValue;
        expectOptions.title = state.title;
        return actualLocator.expect("to.be.checked", expectOptions);
      }, options == null ? null : options.timeout);
    });
  }

  @Override
  public void isDisabled(IsDisabledOptions options) {
    callMatcherAsStep("isDisabled", state -> {
      return toBeTruthy(state, "isDisabled", actualLocator, "Locator", "disabled", "", (isNot, timeout) -> {
        FrameExpectOptions expectOptions = new FrameExpectOptions();
        expectOptions.isNot = isNot;
        expectOptions.timeout = timeout;
        expectOptions.title = state.title;
        return actualLocator.expect("to.be.disabled", expectOptions);
      }, options == null ? null : options.timeout);
    });
  }

  @Override
  public void isEditable(IsEditableOptions options) {
    callMatcherAsStep("isEditable", state -> {
      boolean editable = options == null || options.editable == null || options.editable;
      String expected = editable ? "editable" : "readOnly";
      String arg = editable ? "" : "{ editable: false }";
      return toBeTruthy(state, "isEditable", actualLocator, "Locator", expected, arg, (isNot, timeout) -> {
        FrameExpectOptions expectOptions = new FrameExpectOptions();
        expectOptions.isNot = isNot;
        expectOptions.timeout = timeout;
        expectOptions.title = state.title;
        return actualLocator.expect(editable ? "to.be.editable" : "to.be.readonly", expectOptions);
      }, options == null ? null : options.timeout);
    });
  }

  @Override
  public void isEmpty(IsEmptyOptions options) {
    callMatcherAsStep("isEmpty", state -> {
      return toBeTruthy(state, "isEmpty", actualLocator, "Locator", "empty", "", (isNot, timeout) -> {
        FrameExpectOptions expectOptions = new FrameExpectOptions();
        expectOptions.isNot = isNot;
        expectOptions.timeout = timeout;
        expectOptions.title = state.title;
        return actualLocator.expect("to.be.empty", expectOptions);
      }, options == null ? null : options.timeout);
    });
  }

  @Override
  public void isEnabled(IsEnabledOptions options) {
    callMatcherAsStep("isEnabled", state -> {
      boolean enabled = options == null || options.enabled == null || options.enabled;
      String expected = enabled ? "enabled" : "disabled";
      String arg = enabled ? "" : "{ enabled: false }";
      return toBeTruthy(state, "isEnabled", actualLocator, "Locator", expected, arg, (isNot, timeout) -> {
        FrameExpectOptions expectOptions = new FrameExpectOptions();
        expectOptions.isNot = isNot;
        expectOptions.timeout = timeout;
        expectOptions.title = state.title;
        return actualLocator.expect(enabled ? "to.be.enabled" : "to.be.disabled", expectOptions);
      }, options == null ? null : options.timeout);
    });
  }

  @Override
  public void isFocused(IsFocusedOptions options) {
    callMatcherAsStep("isFocused", state -> {
      return toBeTruthy(state, "isFocused", actualLocator, "Locator", "focused", "", (isNot, timeout) -> {
        FrameExpectOptions expectOptions = new FrameExpectOptions();
        expectOptions.isNot = isNot;
        expectOptions.timeout = timeout;
        expectOptions.title = state.title;
        return actualLocator.expect("to.be.focused", expectOptions);
      }, options == null ? null : options.timeout);
    });
  }

  @Override
  public void isHidden(IsHiddenOptions options) {
    callMatcherAsStep("isHidden", state -> {
      return toBeTruthy(state, "isHidden", actualLocator, "Locator", "hidden", "", (isNot, timeout) -> {
        FrameExpectOptions expectOptions = new FrameExpectOptions();
        expectOptions.isNot = isNot;
        expectOptions.timeout = timeout;
        expectOptions.title = state.title;
        return actualLocator.expect("to.be.hidden", expectOptions);
      }, options == null ? null : options.timeout);
    });
  }

  @Override
  public void isVisible(IsVisibleOptions options) {
    callMatcherAsStep("isVisible", state -> {
      boolean visible = options == null || options.visible == null || options.visible;
      String expected = visible ? "visible" : "hidden";
      String arg = visible ? "" : "{ visible: false }";
      return toBeTruthy(state, "isVisible", actualLocator, "Locator", expected, arg, (isNot, timeout) -> {
        FrameExpectOptions expectOptions = new FrameExpectOptions();
        expectOptions.isNot = isNot;
        expectOptions.timeout = timeout;
        expectOptions.title = state.title;
        return actualLocator.expect(visible ? "to.be.visible" : "to.be.hidden", expectOptions);
      }, options == null ? null : options.timeout);
    });
  }

  @Override
  public void isInViewport(IsInViewportOptions options) {
    callMatcherAsStep("isInViewport", state -> {
      return toBeTruthy(state, "isInViewport", actualLocator, "Locator", "in viewport", "", (isNot, timeout) -> {
        FrameExpectOptions expectOptions = new FrameExpectOptions();
        expectOptions.isNot = isNot;
        expectOptions.expectedNumber = options == null ? null : options.ratio;
        expectOptions.timeout = timeout;
        expectOptions.title = state.title;
        return actualLocator.expect("to.be.in.viewport", expectOptions);
      }, options == null ? null : options.timeout);
    });
  }

  @Override
  public void containsText(String expected, ContainsTextOptions options) {
    toContainText(expected, options);
  }

  @Override
  public void containsText(Pattern expected, ContainsTextOptions options) {
    toContainText(expected, options);
  }

  @Override
  public void containsText(String[] expected, ContainsTextOptions options) {
    toContainText(expected, options);
  }

  @Override
  public void containsText(Pattern[] expected, ContainsTextOptions options) {
    toContainText(expected, options);
  }

  private void toContainText(Object expected, ContainsTextOptions options) {
    callMatcherAsStep("containsText", state -> {
      if (expected instanceof Object[]) {
        return toEqual(state, "containsText", actualLocator, "Locator", (isNot, timeout) -> {
          List<ExpectedTextValue> expectedText = serializeExpectedTextValues((Object[]) expected, true, true, options == null ? null : options.ignoreCase);
          FrameExpectOptions expectOptions = new FrameExpectOptions();
          expectOptions.expectedText = expectedText;
          expectOptions.isNot = isNot;
          expectOptions.useInnerText = options == null ? null : options.useInnerText;
          expectOptions.timeout = timeout;
          expectOptions.title = state.title;
          return actualLocator.expect("to.contain.text.array", expectOptions);
        }, expected, options == null ? null : options.timeout);
      } else {
        return toMatchText(state, "containsText", actualLocator, "Locator", (isNot, timeout) -> {
          List<ExpectedTextValue> expectedText = serializeExpectedTextValues(new Object[] {expected}, true, true, options == null ? null : options.ignoreCase);
          FrameExpectOptions expectOptions = new FrameExpectOptions();
          expectOptions.expectedText = expectedText;
          expectOptions.isNot = isNot;
          expectOptions.useInnerText = options == null ? null : options.useInnerText;
          expectOptions.timeout = timeout;
          expectOptions.title = state.title;
          return actualLocator.expect("to.have.text", expectOptions);
        }, expected, options == null ? null : options.timeout, true);
      }
    });
  }

  @Override
  public void hasAccessibleDescription(String expected, HasAccessibleDescriptionOptions options) {
    toHaveAccessibleDescription(expected, options);
  }

  @Override
  public void hasAccessibleDescription(Pattern expected, HasAccessibleDescriptionOptions options) {
    toHaveAccessibleDescription(expected, options);
  }

  private void toHaveAccessibleDescription(Object expected, HasAccessibleDescriptionOptions options) {
    callMatcherAsStep("hasAccessibleDescription", state -> {
      return toMatchText(state, "hasAccessibleDescription", actualLocator, "Locator", (isNot, timeout) -> {
        List<ExpectedTextValue> expectedText = serializeExpectedTextValues(new Object[] {expected}, null, true, options == null ? null : options.ignoreCase);
        FrameExpectOptions expectOptions = new FrameExpectOptions();
        expectOptions.expectedText = expectedText;
        expectOptions.isNot = isNot;
        expectOptions.timeout = timeout;
        expectOptions.title = state.title;
        return actualLocator.expect("to.have.accessible.description", expectOptions);
      }, expected, options == null ? null : options.timeout, false);
    });
  }

  @Override
  public void hasAccessibleName(String expected, HasAccessibleNameOptions options) {
    toHaveAccessibleName(expected, options);
  }

  @Override
  public void hasAccessibleName(Pattern expected, HasAccessibleNameOptions options) {
    toHaveAccessibleName(expected, options);
  }

  private void toHaveAccessibleName(Object expected, HasAccessibleNameOptions options) {
    callMatcherAsStep("hasAccessibleName", state -> {
      return toMatchText(state, "hasAccessibleName", actualLocator, "Locator", (isNot, timeout) -> {
        List<ExpectedTextValue> expectedText = serializeExpectedTextValues(new Object[] {expected}, null, true, options == null ? null : options.ignoreCase);
        FrameExpectOptions expectOptions = new FrameExpectOptions();
        expectOptions.expectedText = expectedText;
        expectOptions.isNot = isNot;
        expectOptions.timeout = timeout;
        expectOptions.title = state.title;
        return actualLocator.expect("to.have.accessible.name", expectOptions);
      }, expected, options == null ? null : options.timeout, false);
    });
  }

  @Override
  public void hasAccessibleErrorMessage(String expected, HasAccessibleErrorMessageOptions options) {
    toHaveAccessibleErrorMessage(expected, options);
  }

  @Override
  public void hasAccessibleErrorMessage(Pattern expected, HasAccessibleErrorMessageOptions options) {
    toHaveAccessibleErrorMessage(expected, options);
  }

  private void toHaveAccessibleErrorMessage(Object expected, HasAccessibleErrorMessageOptions options) {
    callMatcherAsStep("hasAccessibleErrorMessage", state -> {
      return toMatchText(state, "hasAccessibleErrorMessage", actualLocator, "Locator", (isNot, timeout) -> {
        List<ExpectedTextValue> expectedText = serializeExpectedTextValues(new Object[] {expected}, null, true, options == null ? null : options.ignoreCase);
        FrameExpectOptions expectOptions = new FrameExpectOptions();
        expectOptions.expectedText = expectedText;
        expectOptions.isNot = isNot;
        expectOptions.timeout = timeout;
        expectOptions.title = state.title;
        return actualLocator.expect("to.have.accessible.error.message", expectOptions);
      }, expected, options == null ? null : options.timeout, false);
    });
  }

  @Override
  public void hasAttribute(String name, String expected, HasAttributeOptions options) {
    toHaveAttribute(name, expected, options);
  }

  @Override
  public void hasAttribute(String name, Pattern expected, HasAttributeOptions options) {
    toHaveAttribute(name, expected, options);
  }

  // java: the public API has no hasAttribute(name), so there is no toBeTruthy branch for a missing expected value.
  private void toHaveAttribute(String name, Object expected, HasAttributeOptions options) {
    callMatcherAsStep("hasAttribute", state -> {
      return toMatchText(state, "hasAttribute", actualLocator, "Locator", (isNot, timeout) -> {
        List<ExpectedTextValue> expectedText = serializeExpectedTextValues(new Object[] {expected}, null, null, options == null ? null : options.ignoreCase);
        FrameExpectOptions expectOptions = new FrameExpectOptions();
        expectOptions.expressionArg = name;
        expectOptions.expectedText = expectedText;
        expectOptions.isNot = isNot;
        expectOptions.timeout = timeout;
        expectOptions.title = state.title;
        return actualLocator.expect("to.have.attribute.value", expectOptions);
      }, expected, options == null ? null : options.timeout, false);
    });
  }

  @Override
  public void hasClass(String expected, HasClassOptions options) {
    toHaveClass(expected, options);
  }

  @Override
  public void hasClass(Pattern expected, HasClassOptions options) {
    toHaveClass(expected, options);
  }

  @Override
  public void hasClass(String[] expected, HasClassOptions options) {
    toHaveClass(expected, options);
  }

  @Override
  public void hasClass(Pattern[] expected, HasClassOptions options) {
    toHaveClass(expected, options);
  }

  private void toHaveClass(Object expected, HasClassOptions options) {
    callMatcherAsStep("hasClass", state -> {
      if (expected instanceof Object[]) {
        return toEqual(state, "hasClass", actualLocator, "Locator", (isNot, timeout) -> {
          List<ExpectedTextValue> expectedText = serializeExpectedTextValues((Object[]) expected, null, null, null);
          FrameExpectOptions expectOptions = new FrameExpectOptions();
          expectOptions.expectedText = expectedText;
          expectOptions.isNot = isNot;
          expectOptions.timeout = timeout;
          expectOptions.title = state.title;
          return actualLocator.expect("to.have.class.array", expectOptions);
        }, expected, options == null ? null : options.timeout);
      } else {
        return toMatchText(state, "hasClass", actualLocator, "Locator", (isNot, timeout) -> {
          List<ExpectedTextValue> expectedText = serializeExpectedTextValues(new Object[] {expected}, null, null, null);
          FrameExpectOptions expectOptions = new FrameExpectOptions();
          expectOptions.expectedText = expectedText;
          expectOptions.isNot = isNot;
          expectOptions.timeout = timeout;
          expectOptions.title = state.title;
          return actualLocator.expect("to.have.class", expectOptions);
        }, expected, options == null ? null : options.timeout, false);
      }
    });
  }

  @Override
  public void containsClass(String expected, ContainsClassOptions options) {
    toContainClass(expected, options);
  }

  @Override
  public void containsClass(List<String> expected, ContainsClassOptions options) {
    toContainClass(expected, options);
  }

  // java: expected is a String or a List of String, so there is no check for a RegExp.
  private void toContainClass(Object expected, ContainsClassOptions options) {
    callMatcherAsStep("containsClass", state -> {
      if (expected instanceof List) {
        return toEqual(state, "containsClass", actualLocator, "Locator", (isNot, timeout) -> {
          List<ExpectedTextValue> expectedText = serializeExpectedTextValues(((List<?>) expected).toArray(), null, null, null);
          FrameExpectOptions expectOptions = new FrameExpectOptions();
          expectOptions.expectedText = expectedText;
          expectOptions.isNot = isNot;
          expectOptions.timeout = timeout;
          expectOptions.title = state.title;
          return actualLocator.expect("to.contain.class.array", expectOptions);
        }, expected, options == null ? null : options.timeout);
      } else {
        return toMatchText(state, "containsClass", actualLocator, "Locator", (isNot, timeout) -> {
          List<ExpectedTextValue> expectedText = serializeExpectedTextValues(new Object[] {expected}, null, null, null);
          FrameExpectOptions expectOptions = new FrameExpectOptions();
          expectOptions.expectedText = expectedText;
          expectOptions.isNot = isNot;
          expectOptions.timeout = timeout;
          expectOptions.title = state.title;
          return actualLocator.expect("to.contain.class", expectOptions);
        }, expected, options == null ? null : options.timeout, false);
      }
    });
  }

  @Override
  public void hasCount(int expected, HasCountOptions options) {
    callMatcherAsStep("hasCount", state -> {
      return toEqual(state, "hasCount", actualLocator, "Locator", (isNot, timeout) -> {
        FrameExpectOptions expectOptions = new FrameExpectOptions();
        expectOptions.expectedNumber = (double) expected;
        expectOptions.isNot = isNot;
        expectOptions.timeout = timeout;
        expectOptions.title = state.title;
        return actualLocator.expect("to.have.count", expectOptions);
      }, expected, options == null ? null : options.timeout);
    });
  }

  @Override
  public void hasCSS(String name, String expected, HasCSSOptions options) {
    toHaveCSS(name, expected, options);
  }

  @Override
  public void hasCSS(String name, Pattern expected, HasCSSOptions options) {
    toHaveCSS(name, expected, options);
  }

  private void toHaveCSS(String name, Object expected, HasCSSOptions options) {
    callMatcherAsStep("hasCSS", state -> {
      return toMatchText(state, "hasCSS", actualLocator, "Locator", (isNot, timeout) -> {
        List<ExpectedTextValue> expectedText = serializeExpectedTextValues(new Object[] {expected}, null, null, null);
        FrameExpectOptions expectOptions = new FrameExpectOptions();
        expectOptions.expressionArg = name;
        expectOptions.expectedText = expectedText;
        expectOptions.isNot = isNot;
        expectOptions.pseudo = options == null ? null : options.pseudo;
        expectOptions.timeout = timeout;
        expectOptions.title = state.title;
        return actualLocator.expect("to.have.css", expectOptions);
      }, expected, options == null ? null : options.timeout, false);
    });
  }

  @Override
  public void hasId(String expected, HasIdOptions options) {
    toHaveId(expected, options);
  }

  @Override
  public void hasId(Pattern expected, HasIdOptions options) {
    toHaveId(expected, options);
  }

  private void toHaveId(Object expected, HasIdOptions options) {
    callMatcherAsStep("hasId", state -> {
      return toMatchText(state, "hasId", actualLocator, "Locator", (isNot, timeout) -> {
        List<ExpectedTextValue> expectedText = serializeExpectedTextValues(new Object[] {expected}, null, null, null);
        FrameExpectOptions expectOptions = new FrameExpectOptions();
        expectOptions.expectedText = expectedText;
        expectOptions.isNot = isNot;
        expectOptions.timeout = timeout;
        expectOptions.title = state.title;
        return actualLocator.expect("to.have.id", expectOptions);
      }, expected, options == null ? null : options.timeout, false);
    });
  }

  @Override
  public void hasJSProperty(String name, Object expected, HasJSPropertyOptions options) {
    callMatcherAsStep("hasJSProperty", state -> {
      return toEqual(state, "hasJSProperty", actualLocator, "Locator", (isNot, timeout) -> {
        FrameExpectOptions expectOptions = new FrameExpectOptions();
        expectOptions.expressionArg = name;
        expectOptions.expectedValue = expected;
        expectOptions.isNot = isNot;
        expectOptions.timeout = timeout;
        expectOptions.title = state.title;
        return actualLocator.expect("to.have.property", expectOptions);
      }, expected, options == null ? null : options.timeout);
    });
  }

  @Override
  public void hasRole(AriaRole role, HasRoleOptions options) {
    // java: the role is an enum, its string is the lower case name; there is no check that it is a string.
    String expected = role.toString().toLowerCase();
    callMatcherAsStep("hasRole", state -> {
      return toMatchText(state, "hasRole", actualLocator, "Locator", (isNot, timeout) -> {
        List<ExpectedTextValue> expectedText = serializeExpectedTextValues(new Object[] {expected}, null, null, null);
        FrameExpectOptions expectOptions = new FrameExpectOptions();
        expectOptions.expectedText = expectedText;
        expectOptions.isNot = isNot;
        expectOptions.timeout = timeout;
        expectOptions.title = state.title;
        return actualLocator.expect("to.have.role", expectOptions);
      }, expected, options == null ? null : options.timeout, false);
    });
  }

  @Override
  public void hasText(String expected, HasTextOptions options) {
    toHaveText(expected, options);
  }

  @Override
  public void hasText(Pattern expected, HasTextOptions options) {
    toHaveText(expected, options);
  }

  @Override
  public void hasText(String[] expected, HasTextOptions options) {
    toHaveText(expected, options);
  }

  @Override
  public void hasText(Pattern[] expected, HasTextOptions options) {
    toHaveText(expected, options);
  }

  private void toHaveText(Object expected, HasTextOptions options) {
    callMatcherAsStep("hasText", state -> {
      if (expected instanceof Object[]) {
        return toEqual(state, "hasText", actualLocator, "Locator", (isNot, timeout) -> {
          List<ExpectedTextValue> expectedText = serializeExpectedTextValues((Object[]) expected, null, true, options == null ? null : options.ignoreCase);
          FrameExpectOptions expectOptions = new FrameExpectOptions();
          expectOptions.expectedText = expectedText;
          expectOptions.isNot = isNot;
          expectOptions.useInnerText = options == null ? null : options.useInnerText;
          expectOptions.timeout = timeout;
          expectOptions.title = state.title;
          return actualLocator.expect("to.have.text.array", expectOptions);
        }, expected, options == null ? null : options.timeout);
      } else {
        return toMatchText(state, "hasText", actualLocator, "Locator", (isNot, timeout) -> {
          List<ExpectedTextValue> expectedText = serializeExpectedTextValues(new Object[] {expected}, null, true, options == null ? null : options.ignoreCase);
          FrameExpectOptions expectOptions = new FrameExpectOptions();
          expectOptions.expectedText = expectedText;
          expectOptions.isNot = isNot;
          expectOptions.useInnerText = options == null ? null : options.useInnerText;
          expectOptions.timeout = timeout;
          expectOptions.title = state.title;
          return actualLocator.expect("to.have.text", expectOptions);
        }, expected, options == null ? null : options.timeout, false);
      }
    });
  }

  @Override
  public void hasValue(String expected, HasValueOptions options) {
    toHaveValue(expected, options);
  }

  @Override
  public void hasValue(Pattern expected, HasValueOptions options) {
    toHaveValue(expected, options);
  }

  private void toHaveValue(Object expected, HasValueOptions options) {
    callMatcherAsStep("hasValue", state -> {
      return toMatchText(state, "hasValue", actualLocator, "Locator", (isNot, timeout) -> {
        List<ExpectedTextValue> expectedText = serializeExpectedTextValues(new Object[] {expected}, null, null, null);
        FrameExpectOptions expectOptions = new FrameExpectOptions();
        expectOptions.expectedText = expectedText;
        expectOptions.isNot = isNot;
        expectOptions.timeout = timeout;
        expectOptions.title = state.title;
        return actualLocator.expect("to.have.value", expectOptions);
      }, expected, options == null ? null : options.timeout, false);
    });
  }

  @Override
  public void hasValues(String[] expected, HasValuesOptions options) {
    toHaveValues(expected, options);
  }

  @Override
  public void hasValues(Pattern[] expected, HasValuesOptions options) {
    toHaveValues(expected, options);
  }

  private void toHaveValues(Object[] expected, HasValuesOptions options) {
    callMatcherAsStep("hasValues", state -> {
      return toEqual(state, "hasValues", actualLocator, "Locator", (isNot, timeout) -> {
        List<ExpectedTextValue> expectedText = serializeExpectedTextValues(expected, null, null, null);
        FrameExpectOptions expectOptions = new FrameExpectOptions();
        expectOptions.expectedText = expectedText;
        expectOptions.isNot = isNot;
        expectOptions.timeout = timeout;
        expectOptions.title = state.title;
        return actualLocator.expect("to.have.values", expectOptions);
      }, expected, options == null ? null : options.timeout);
    });
  }

  // java: toMatchAriaSnapshot of toMatchAriaSnapshot.ts.
  @Override
  public void matchesAriaSnapshot(String expected, MatchesAriaSnapshotOptions options) {
    callMatcherAsStep("matchesAriaSnapshot", state -> toMatchAriaSnapshot(state, actualLocator, expected, options == null ? null : options.timeout));
  }
}
