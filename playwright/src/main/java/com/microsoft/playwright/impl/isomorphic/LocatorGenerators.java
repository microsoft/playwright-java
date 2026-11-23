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

// Port of isomorphic/locatorGenerators.ts.
package com.microsoft.playwright.impl.isomorphic;

import com.google.gson.JsonElement;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.impl.isomorphic.SelectorParser.AttributeSelector;
import com.microsoft.playwright.impl.isomorphic.SelectorParser.AttributeSelectorPart;
import com.microsoft.playwright.impl.isomorphic.SelectorParser.NestedSelectorBody;
import com.microsoft.playwright.impl.isomorphic.SelectorParser.ParsedSelector;
import com.microsoft.playwright.impl.isomorphic.SelectorParser.ParsedSelectorPart;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.isomorphic.SelectorParser.parseAttributeSelector;
import static com.microsoft.playwright.impl.isomorphic.SelectorParser.parseSelector;
import static com.microsoft.playwright.impl.isomorphic.SelectorParser.stringifySelector;
import static com.microsoft.playwright.impl.isomorphic.SelectorParser.valueToString;
import static com.microsoft.playwright.impl.isomorphic.StringUtils.escapeWithQuotes;
import static com.microsoft.playwright.impl.isomorphic.StringUtils.jsonParse;
import static com.microsoft.playwright.impl.isomorphic.StringUtils.normalizeEscapedRegexQuotes;
import static com.microsoft.playwright.impl.isomorphic.StringUtils.toNumber;
import static com.microsoft.playwright.impl.isomorphic.StringUtils.toSnakeCase;
import static com.microsoft.playwright.impl.isomorphic.StringUtils.toTitleCase;

// java: only the JavaLocatorFactory is ported, the JavaScript, Python, CSharp and Jsonl factories are not: the client
// prints Java code. Language, LocatorType and LocatorBase are a String. A string | RegExp is an Object holding
// a String or a RegExp. There is no preferredQuote parameter, only the JavaScript factory reads it.
public final class LocatorGenerators {
  private LocatorGenerators() {
  }

  // java: the type of an item of LocatorOptions.attrs. The value is a String, a Boolean, a Double, a RegExp or null.
  static class LocatorOptionsAttr {
    String name;
    Object value;

    LocatorOptionsAttr(String name, Object value) {
      this.name = name;
      this.value = value;
    }
  }

  static class LocatorOptions {
    List<LocatorOptionsAttr> attrs;
    Boolean exact;
    Object name;
    Object description;
    Object hasText;
    Object hasNotText;
  }

  public interface LocatorFactory {
    String generateLocator(String base, String kind, Object body, LocatorOptions options);
    String chainLocators(List<String> locators);
    // java: the overload for the optional options parameter.
    default String generateLocator(String base, String kind, Object body) {
      return generateLocator(base, kind, body, null);
    }
  }

  public static String asLocatorDescription(String lang, String selector) {
    try {
      ParsedSelector parsed = parseSelector(selector);
      String customDescription = parseCustomDescription(parsed);
      if (customDescription != null && !customDescription.isEmpty())
        return customDescription;
      return innerAsLocators(generators.get(lang).get(), parsed, false, 1).get(0);
    } catch (RuntimeException e) {
      return selector;
    }
  }

  public static String locatorCustomDescription(String selector) {
    try {
      ParsedSelector parsed = parseSelector(selector);
      return parseCustomDescription(parsed);
    } catch (RuntimeException e) {
      return null;
    }
  }

  private static String parseCustomDescription(ParsedSelector parsed) {
    ParsedSelectorPart lastPart = parsed.parts.get(parsed.parts.size() - 1);
    if (lastPart.name.equals("internal:describe")) {
      JsonElement description = jsonParse((String) lastPart.body);
      if (description.isJsonPrimitive() && description.getAsJsonPrimitive().isString())
        return description.getAsString();
    }
    return null;
  }

  public static String asLocator(String lang, String selector) {
    return asLocator(lang, selector, false);
  }

  public static String asLocator(String lang, String selector, boolean isFrameLocator) {
    return asLocators(lang, selector, isFrameLocator, 1).get(0);
  }

  public static List<String> asLocators(String lang, String selector) {
    return asLocators(lang, selector, false, 20);
  }

  public static List<String> asLocators(String lang, String selector, boolean isFrameLocator, int maxOutputSize) {
    try {
      return innerAsLocators(generators.get(lang).get(), parseSelector(selector), isFrameLocator, maxOutputSize);
    } catch (RuntimeException e) {
      return new ArrayList<>(Collections.singletonList(selector));
    }
  }

  private static List<String> innerAsLocators(LocatorFactory factory, ParsedSelector parsed, boolean isFrameLocator, int maxOutputSize) {
    List<ParsedSelectorPart> parts = new ArrayList<>(parsed.parts);
    List<List<String>> tokens = new ArrayList<>();
    String nextBase = isFrameLocator ? "frame-locator" : "page";
    for (int index = 0; index < parts.size(); index++) {
      ParsedSelectorPart part = parts.get(index);
      String base = nextBase;
      nextBase = "locator";

      if (part.name.equals("internal:describe"))
        continue;
      if (part.name.equals("nth")) {
        if ("0".equals(part.body))
          tokens.add(listOf(factory.generateLocator(base, "first", ""), factory.generateLocator(base, "nth", "0")));
        else if ("-1".equals(part.body))
          tokens.add(listOf(factory.generateLocator(base, "last", ""), factory.generateLocator(base, "nth", "-1")));
        else
          tokens.add(listOf(factory.generateLocator(base, "nth", (String) part.body)));
        continue;
      }
      if (part.name.equals("visible")) {
        List<String> tokenList = new ArrayList<>();
        if ("true".equals(part.body))
          tokenList.add(factory.generateLocator(base, "visible", ""));
        tokenList.add(factory.generateLocator(base, "filter-visible", (String) part.body));
        tokenList.add(factory.generateLocator(base, "default", "visible=" + part.body));
        tokens.add(tokenList);
        continue;
      }
      if (part.name.equals("internal:text")) {
        DetectedExact detected = detectExact((String) part.body);
        LocatorOptions options = new LocatorOptions();
        options.exact = detected.exact;
        tokens.add(listOf(factory.generateLocator(base, "text", detected.text, options)));
        continue;
      }
      if (part.name.equals("internal:has-text")) {
        DetectedExact detected = detectExact((String) part.body);
        if (!(detected.exact != null && detected.exact)) {
          LocatorOptions options = new LocatorOptions();
          options.exact = detected.exact;
          tokens.add(listOf(factory.generateLocator(base, "has-text", detected.text, options)));
          continue;
        }
      }
      if (part.name.equals("internal:has-not-text")) {
        DetectedExact detected = detectExact((String) part.body);
        if (!(detected.exact != null && detected.exact)) {
          LocatorOptions options = new LocatorOptions();
          options.exact = detected.exact;
          tokens.add(listOf(factory.generateLocator(base, "has-not-text", detected.text, options)));
          continue;
        }
      }
      if (part.name.equals("internal:has")) {
        List<String> inners = innerAsLocators(factory, ((NestedSelectorBody) part.body).parsed, false, maxOutputSize);
        List<String> mapped = new ArrayList<>();
        for (String inner : inners)
          mapped.add(factory.generateLocator(base, "has", inner));
        tokens.add(mapped);
        continue;
      }
      if (part.name.equals("internal:has-not")) {
        List<String> inners = innerAsLocators(factory, ((NestedSelectorBody) part.body).parsed, false, maxOutputSize);
        List<String> mapped = new ArrayList<>();
        for (String inner : inners)
          mapped.add(factory.generateLocator(base, "hasNot", inner));
        tokens.add(mapped);
        continue;
      }
      if (part.name.equals("internal:and")) {
        List<String> inners = innerAsLocators(factory, ((NestedSelectorBody) part.body).parsed, false, maxOutputSize);
        List<String> mapped = new ArrayList<>();
        for (String inner : inners)
          mapped.add(factory.generateLocator(base, "and", inner));
        tokens.add(mapped);
        continue;
      }
      if (part.name.equals("internal:or")) {
        List<String> inners = innerAsLocators(factory, ((NestedSelectorBody) part.body).parsed, false, maxOutputSize);
        List<String> mapped = new ArrayList<>();
        for (String inner : inners)
          mapped.add(factory.generateLocator(base, "or", inner));
        tokens.add(mapped);
        continue;
      }
      if (part.name.equals("internal:chain")) {
        List<String> inners = innerAsLocators(factory, ((NestedSelectorBody) part.body).parsed, false, maxOutputSize);
        List<String> mapped = new ArrayList<>();
        for (String inner : inners)
          mapped.add(factory.generateLocator(base, "chain", inner));
        tokens.add(mapped);
        continue;
      }
      if (part.name.equals("internal:label")) {
        DetectedExact detected = detectExact((String) part.body);
        LocatorOptions options = new LocatorOptions();
        options.exact = detected.exact;
        tokens.add(listOf(factory.generateLocator(base, "label", detected.text, options)));
        continue;
      }
      if (part.name.equals("internal:role")) {
        AttributeSelector attrSelector = parseAttributeSelector((String) part.body, true);
        LocatorOptions options = new LocatorOptions();
        options.attrs = new ArrayList<>();
        for (AttributeSelectorPart attr : attrSelector.attributes) {
          if (attr.name.equals("name") || attr.name.equals("description")) {
            if (attr.value instanceof String) {
              if (options.exact != null && options.exact != attr.caseSensitive)
                throw new PlaywrightException("Conflicting exactness in internal:role selector: " + stringifySelector(new ParsedSelector(listOf(part))));
              options.exact = attr.caseSensitive;
            }
            if (attr.name.equals("name"))
              options.name = attr.value;
            else
              options.description = attr.value;
          } else {
            if (attr.name.equals("level") && attr.value instanceof String)
              attr.value = toNumber((String) attr.value);
            options.attrs.add(new LocatorOptionsAttr(attr.name.equals("include-hidden") ? "includeHidden" : attr.name, attr.value));
          }
        }
        tokens.add(listOf(factory.generateLocator(base, "role", attrSelector.name, options)));
        continue;
      }
      if (part.name.equals("internal:testid")) {
        AttributeSelector attrSelector = parseAttributeSelector((String) part.body, true);
        Object value = attrSelector.attributes.get(0).value;
        tokens.add(listOf(factory.generateLocator(base, "test-id", value)));
        continue;
      }
      if (part.name.equals("internal:attr")) {
        AttributeSelector attrSelector = parseAttributeSelector((String) part.body, true);
        String name = attrSelector.attributes.get(0).name;
        Object text = attrSelector.attributes.get(0).value;
        boolean exact = attrSelector.attributes.get(0).caseSensitive;
        LocatorOptions options = new LocatorOptions();
        options.exact = exact;
        if (name.equals("placeholder")) {
          tokens.add(listOf(factory.generateLocator(base, "placeholder", text, options)));
          continue;
        }
        if (name.equals("alt")) {
          tokens.add(listOf(factory.generateLocator(base, "alt", text, options)));
          continue;
        }
        if (name.equals("title")) {
          tokens.add(listOf(factory.generateLocator(base, "title", text, options)));
          continue;
        }
      }
      if (part.name.equals("internal:control") && "any-frame".equals(part.body)) {
        tokens.add(listOf(factory.generateLocator(base, "any-frame", "")));
        nextBase = "frame-locator";
        continue;
      }
      if (part.name.equals("internal:control") && "enter-frame".equals(part.body)) {
        List<String> lastTokens = tokens.get(tokens.size() - 1);
        ParsedSelectorPart lastPart = parts.get(index - 1);

        List<String> transformed = new ArrayList<>();
        for (String token : lastTokens)
          transformed.add(factory.chainLocators(listOf(token, factory.generateLocator(base, "frame", ""))));
        if (Arrays.asList("xpath", "css").contains(lastPart.name)) {
          transformed.add(factory.generateLocator(base, "frame-locator", stringifySelector(new ParsedSelector(listOf(lastPart)))));
          transformed.add(factory.generateLocator(base, "frame-locator", stringifySelector(new ParsedSelector(listOf(lastPart)), true)));
        }

        lastTokens.clear();
        lastTokens.addAll(transformed);
        nextBase = "frame-locator";
        continue;
      }

      ParsedSelectorPart nextPart = index + 1 < parts.size() ? parts.get(index + 1) : null;

      String selectorPart = stringifySelector(new ParsedSelector(listOf(part)));
      String locatorPart = factory.generateLocator(base, "default", selectorPart);

      if (nextPart != null && Arrays.asList("internal:has-text", "internal:has-not-text").contains(nextPart.name)) {
        DetectedExact detected = detectExact((String) nextPart.body);
        if (!(detected.exact != null && detected.exact)) {
          LocatorOptions nextOptions = new LocatorOptions();
          nextOptions.exact = detected.exact;
          String nextLocatorPart = factory.generateLocator("locator", nextPart.name.equals("internal:has-text") ? "has-text" : "has-not-text", detected.text, nextOptions);
          LocatorOptions options = new LocatorOptions();
          if (nextPart.name.equals("internal:has-text"))
            options.hasText = detected.text;
          else
            options.hasNotText = detected.text;
          String combinedPart = factory.generateLocator(base, "default", selectorPart, options);
          tokens.add(listOf(factory.chainLocators(listOf(locatorPart, nextLocatorPart)), combinedPart));
          index++;
          continue;
        }
      }

      String locatorPartWithEngine = null;
      if (Arrays.asList("xpath", "css").contains(part.name)) {
        String selectorPartWithEngine = stringifySelector(new ParsedSelector(listOf(part)), true);
        locatorPartWithEngine = factory.generateLocator(base, "default", selectorPartWithEngine);
      }

      List<String> tokenList = new ArrayList<>();
      for (String token : Arrays.asList(locatorPart, locatorPartWithEngine)) {
        if (token != null && !token.isEmpty())
          tokenList.add(token);
      }
      tokens.add(tokenList);
    }

    return combineTokens(factory, tokens, maxOutputSize);
  }

  private static List<String> combineTokens(LocatorFactory factory, List<List<String>> tokens, int maxOutputSize) {
    List<String> currentTokens = new ArrayList<>();
    for (int i = 0; i < tokens.size(); i++)
      currentTokens.add("");
    List<String> result = new ArrayList<>();

    visit(factory, tokens, maxOutputSize, currentTokens, result, 0);
    return result;
  }

  // java: the visit closure of combineTokens() is a method, the variables it captures are parameters.
  private static boolean visit(LocatorFactory factory, List<List<String>> tokens, int maxOutputSize, List<String> currentTokens, List<String> result, int index) {
    if (index == tokens.size()) {
      result.add(factory.chainLocators(currentTokens));
      return result.size() < maxOutputSize;
    }
    for (String taken : tokens.get(index)) {
      currentTokens.set(index, taken);
      if (!visit(factory, tokens, maxOutputSize, currentTokens, result, index + 1))
        return false;
    }
    return true;
  }

  // java: the object that detectExact() returns.
  private static class DetectedExact {
    Boolean exact;
    Object text;

    DetectedExact(Boolean exact, Object text) {
      this.exact = exact;
      this.text = text;
    }
  }

  // java: the dot of a JS RegExp does not match the line terminators, and its $ only matches at the end.
  private static DetectedExact detectExact(String text) {
    boolean exact = false;
    Matcher match = Pattern.compile("/([^\\n\\r\\u2028\\u2029]*)/([dgimsuvy]*)").matcher(text);
    if (match.matches())
      return new DetectedExact(null, new RegExp(match.group(1), match.group(2)));
    if (text.endsWith("\"")) {
      text = jsonParse(text).getAsString();
      exact = true;
    } else if (text.endsWith("\"s")) {
      text = jsonParse(text.substring(0, text.length() - 1)).getAsString();
      exact = true;
    } else if (text.endsWith("\"i")) {
      text = jsonParse(text.substring(0, text.length() - 1)).getAsString();
      exact = false;
    }
    return new DetectedExact(exact, text);
  }

  // java: a body that is not a String or a RegExp (a boolean, a number or null attribute value) fails the cast to
  // String, so asLocator() returns the selector; upstream quotes a part of JSON.stringify(body), "ru" for true.
  public static class JavaLocatorFactory implements LocatorFactory {
    @Override
    public String generateLocator(String base, String kind, Object body, LocatorOptions options) {
      if (options == null)
        options = new LocatorOptions();
      String clazz = null;
      switch (base) {
        case "page": clazz = "Page"; break;
        case "frame-locator": clazz = "FrameLocator"; break;
        case "locator": clazz = "Locator"; break;
      }
      switch (kind) {
        case "default":
          if (options.hasText != null)
            return "locator(" + this.quote((String) body) + ", new " + clazz + ".LocatorOptions().setHasText(" + this.toHasText(options.hasText) + "))";
          if (options.hasNotText != null)
            return "locator(" + this.quote((String) body) + ", new " + clazz + ".LocatorOptions().setHasNotText(" + this.toHasText(options.hasNotText) + "))";
          return "locator(" + this.quote((String) body) + ")";
        case "frame-locator":
          return "frameLocator(" + this.quote((String) body) + ")";
        case "frame":
          return "contentFrame()";
        case "any-frame":
          return "frameLocator()";
        case "nth":
          return "nth(" + body + ")";
        case "first":
          return "first()";
        case "last":
          return "last()";
        case "visible":
          return "visible()";
        case "filter-visible":
          return "filter(new " + clazz + ".FilterOptions().setVisible(" + ("true".equals(body) ? "true" : "false") + "))";
        case "role":
          List<String> attrs = new ArrayList<>();
          if (isRegExp(options.name))
            attrs.add(".setName(" + this.regexToString((RegExp) options.name) + ")");
          else if (options.name instanceof String)
            attrs.add(".setName(" + this.quote((String) options.name) + ")");
          if (isRegExp(options.description))
            attrs.add(".setDescription(" + this.regexToString((RegExp) options.description) + ")");
          else if (options.description instanceof String)
            attrs.add(".setDescription(" + this.quote((String) options.description) + ")");
          if (options.exact != null && options.exact && (options.name instanceof String || options.description instanceof String))
            attrs.add(".setExact(true)");
          for (LocatorOptionsAttr attr : options.attrs)
            attrs.add(".set" + toTitleCase(attr.name) + "(" + (attr.value instanceof String ? this.quote((String) attr.value) : valueToString(attr.value)) + ")");
          String attrString = !attrs.isEmpty() ? ", new " + clazz + ".GetByRoleOptions()" + String.join("", attrs) : "";
          return "getByRole(AriaRole." + toSnakeCase((String) body).toUpperCase(Locale.ROOT) + attrString + ")";
        case "has-text":
          return "filter(new " + clazz + ".FilterOptions().setHasText(" + this.toHasText(body) + "))";
        case "has-not-text":
          return "filter(new " + clazz + ".FilterOptions().setHasNotText(" + this.toHasText(body) + "))";
        case "has":
          return "filter(new " + clazz + ".FilterOptions().setHas(" + body + "))";
        case "hasNot":
          return "filter(new " + clazz + ".FilterOptions().setHasNot(" + body + "))";
        case "and":
          return "and(" + body + ")";
        case "or":
          return "or(" + body + ")";
        case "chain":
          return "locator(" + body + ")";
        case "test-id":
          return "getByTestId(" + this.toTestIdValue(body) + ")";
        case "text":
          return this.toCallWithExact(clazz, "getByText", body, options.exact != null && options.exact);
        case "alt":
          return this.toCallWithExact(clazz, "getByAltText", body, options.exact != null && options.exact);
        case "placeholder":
          return this.toCallWithExact(clazz, "getByPlaceholder", body, options.exact != null && options.exact);
        case "label":
          return this.toCallWithExact(clazz, "getByLabel", body, options.exact != null && options.exact);
        case "title":
          return this.toCallWithExact(clazz, "getByTitle", body, options.exact != null && options.exact);
        default:
          throw new PlaywrightException("Unknown selector kind " + kind);
      }
    }

    @Override
    public String chainLocators(List<String> locators) {
      return String.join(".", locators);
    }

    private String regexToString(RegExp body) {
      String suffix = regexFlagsSuffix(body, "Pattern", regexFlagNames.get("java"));
      return "Pattern.compile(" + this.quote(normalizeEscapedRegexQuotes(body.source)) + suffix + ")";
    }

    private String toCallWithExact(String clazz, String method, Object body, boolean exact) {
      if (isRegExp(body))
        return method + "(" + this.regexToString((RegExp) body) + ")";
      if (exact)
        return method + "(" + this.quote((String) body) + ", new " + clazz + "." + toTitleCase(method) + "Options().setExact(true))";
      return method + "(" + this.quote((String) body) + ")";
    }

    private String toHasText(Object body) {
      if (isRegExp(body))
        return this.regexToString((RegExp) body);
      return this.quote((String) body);
    }

    private String toTestIdValue(Object value) {
      if (isRegExp(value))
        return this.regexToString((RegExp) value);
      return this.quote((String) value);
    }

    private String quote(String text) {
      return escapeWithQuotes(text, "\"");
    }
  }

  // java: only the java generator.
  private static final Map<String, Supplier<LocatorFactory>> generators = new LinkedHashMap<>();
  static {
    generators.put("java", JavaLocatorFactory::new);
  }

  // java: only the java flag names.
  public static final Map<String, Map<String, String>> regexFlagNames = new LinkedHashMap<>();
  static {
    Map<String, String> java = new LinkedHashMap<>();
    java.put("i", "CASE_INSENSITIVE");
    java.put("m", "MULTILINE");
    java.put("s", "DOTALL");
    regexFlagNames.put("java", java);
  }

  private static String regexFlagsSuffix(RegExp body, String flagsEnum, Map<String, String> names) {
    List<String> flags = new ArrayList<>();
    for (char flag : body.flags.toCharArray()) {
      if (names.containsKey(String.valueOf(flag)))
        flags.add(flagsEnum + "." + names.get(String.valueOf(flag)));
    }
    return !flags.isEmpty() ? ", " + String.join(" | ", flags) : "";
  }

  private static boolean isRegExp(Object obj) {
    return obj instanceof RegExp;
  }

  // java: helpers with no counterpart upstream.

  // java: an array literal, as a list that can be changed.
  @SafeVarargs
  private static <T> List<T> listOf(T... items) {
    return new ArrayList<>(Arrays.asList(items));
  }
}
