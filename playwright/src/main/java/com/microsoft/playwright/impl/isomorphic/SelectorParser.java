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

// Port of isomorphic/selectorParser.ts.
package com.microsoft.playwright.impl.isomorphic;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.microsoft.playwright.PlaywrightException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.isomorphic.StringUtils.JS_WHITESPACE;
import static com.microsoft.playwright.impl.isomorphic.StringUtils.jsTrim;
import static com.microsoft.playwright.impl.isomorphic.StringUtils.jsonParse;
import static com.microsoft.playwright.impl.isomorphic.StringUtils.numberToString;
import static com.microsoft.playwright.impl.isomorphic.StringUtils.toNumber;

// java: the types of the file are nested classes. kAnyFrameSelector, splitSelectorByFrame and visitAllSelectorParts
// are not ported, the locator generators do not use them. There is no port of cssParser.ts and cssTokenizer.ts:
// the body of a css part is its source, and a css selector that upstream rejects is not rejected here.
public final class SelectorParser {
  private SelectorParser() {
  }

  // java: InvalidSelectorError of cssParser.ts.
  public static class InvalidSelectorError extends PlaywrightException {
    public InvalidSelectorError(String message) {
      super(message);
    }
  }

  public static class NestedSelectorBody {
    public ParsedSelector parsed;
    public Double distance;
  }

  private static final Set<String> kNestedSelectorNames = new HashSet<>(Arrays.asList("internal:has", "internal:has-not", "internal:and", "internal:or", "internal:chain", "left-of", "right-of", "above", "below", "near"));
  private static final Set<String> kNestedSelectorNamesWithDistance = new HashSet<>(Arrays.asList("left-of", "right-of", "above", "below", "near"));

  // java: the body is an Object holding a String or a NestedSelectorBody.
  public static class ParsedSelectorPart {
    public String name;
    public Object body;
    public String source;

    ParsedSelectorPart(String name, Object body, String source) {
      this.name = name;
      this.body = body;
      this.source = source;
    }
  }

  public static class ParsedSelector {
    public List<ParsedSelectorPart> parts;
    public Integer capture;

    ParsedSelector(List<ParsedSelectorPart> parts) {
      this.parts = parts;
    }
  }

  // java: the type of an item of ParsedSelectorStrings.parts.
  private static class ParsedSelectorStringPart {
    String name;
    String body;

    ParsedSelectorStringPart(String name, String body) {
      this.name = name;
      this.body = body;
    }
  }

  private static class ParsedSelectorStrings {
    List<ParsedSelectorStringPart> parts = new ArrayList<>();
    Integer capture;
  }

  // java: no customCSSNames, parseCSS reads them.

  public static ParsedSelector parseSelector(String selector) {
    ParsedSelectorStrings parsedStrings = parseSelectorString(selector);
    List<ParsedSelectorPart> parts = new ArrayList<>();
    for (ParsedSelectorStringPart part : parsedStrings.parts) {
      if (part.name.equals("css") || part.name.equals("css:light")) {
        if (part.name.equals("css:light"))
          part.body = ":light(" + part.body + ")";
        // java: no parseCSS, the body is the source.
        parts.add(new ParsedSelectorPart(
          "css",
          part.body,
          part.body
        ));
        continue;
      }
      if (kNestedSelectorNames.contains(part.name)) {
        String innerSelector;
        Double distance = null;
        try {
          JsonElement unescaped = jsonParse("[" + part.body + "]");
          if (!unescaped.isJsonArray() || unescaped.getAsJsonArray().size() < 1 || unescaped.getAsJsonArray().size() > 2 || !isJsonString(unescaped.getAsJsonArray().get(0)))
            throw new InvalidSelectorError("Malformed selector: " + part.name + "=" + part.body);
          JsonArray array = unescaped.getAsJsonArray();
          innerSelector = array.get(0).getAsString();
          if (array.size() == 2) {
            if (!(array.get(1).isJsonPrimitive() && array.get(1).getAsJsonPrimitive().isNumber()) || !kNestedSelectorNamesWithDistance.contains(part.name))
              throw new InvalidSelectorError("Malformed selector: " + part.name + "=" + part.body);
            distance = array.get(1).getAsDouble();
          }
        } catch (RuntimeException e) {
          throw new InvalidSelectorError("Malformed selector: " + part.name + "=" + part.body);
        }
        NestedSelectorBody nestedBody = new NestedSelectorBody();
        nestedBody.parsed = parseSelector(innerSelector);
        nestedBody.distance = distance;
        ParsedSelectorPart nested = new ParsedSelectorPart(part.name, nestedBody, part.body);
        ParsedSelectorPart lastFrame = null;
        for (int i = nestedBody.parsed.parts.size() - 1; i >= 0; i--) {
          ParsedSelectorPart p = nestedBody.parsed.parts.get(i);
          if (p.name.equals("internal:control") && "enter-frame".equals(p.body)) {
            lastFrame = p;
            break;
          }
        }
        int lastFrameIndex = lastFrame != null ? nestedBody.parsed.parts.indexOf(lastFrame) : -1;
        List<ParsedSelectorPart> outerParts = !parts.isEmpty() && parts.get(0).name.equals("internal:control") && "any-frame".equals(parts.get(0).body) ? slice(parts, 1, parts.size()) : parts;
        if (lastFrameIndex != -1 && selectorPartsEqual(slice(nestedBody.parsed.parts, 0, lastFrameIndex + 1), slice(outerParts, 0, lastFrameIndex + 1))) {
          nestedBody.parsed.parts.subList(0, lastFrameIndex + 1).clear();
          if (nestedBody.parsed.capture != null) {
            if (nestedBody.parsed.capture <= lastFrameIndex)
              throw new InvalidSelectorError("Can not capture the selector before diving into the frame. Only use * after the last frame has been selected");
            nestedBody.parsed.capture -= lastFrameIndex + 1;
          }
        }
        parts.add(nested);
        continue;
      }
      parts.add(new ParsedSelectorPart(part.name, part.body, part.body));
    }
    if (kNestedSelectorNames.contains(parts.get(0).name))
      throw new InvalidSelectorError("\"" + parts.get(0).name + "\" selector cannot be first");
    ParsedSelector result = new ParsedSelector(parts);
    result.capture = parsedStrings.capture;
    return result;
  }

  private static boolean selectorPartsEqual(List<ParsedSelectorPart> list1, List<ParsedSelectorPart> list2) {
    return stringifySelector(new ParsedSelector(list1)).equals(stringifySelector(new ParsedSelector(list2)));
  }

  public static String stringifySelector(String selector) {
    return selector;
  }

  public static String stringifySelector(ParsedSelector selector) {
    return stringifySelector(selector, false);
  }

  public static String stringifySelector(ParsedSelector selector, boolean forceEngineName) {
    List<String> result = new ArrayList<>();
    for (int i = 0; i < selector.parts.size(); i++) {
      ParsedSelectorPart p = selector.parts.get(i);
      boolean isCapture = selector.capture != null && i == selector.capture;
      boolean includeEngine = true;
      if (!forceEngineName && !isCapture) {
        if (p.name.equals("css"))
          includeEngine = false;
        else if (p.name.equals("xpath") && (p.source.startsWith("//") || p.source.startsWith("..")))
          includeEngine = false;
      }
      String prefix = includeEngine ? p.name + "=" : "";
      result.add((isCapture ? "*" : "") + prefix + p.source);
    }
    return String.join(" >> ", result);
  }

  private static ParsedSelectorStrings parseSelectorString(String selector) {
    return new SelectorStringParser(selector).parse();
  }

  // java: the local variables and the closures of parseSelectorString() are the fields and the methods of this class.
  private static class SelectorStringParser {
    private final String selector;
    private int index = 0;
    private String quote;
    private int start = 0;
    private final ParsedSelectorStrings result = new ParsedSelectorStrings();

    SelectorStringParser(String selector) {
      this.selector = selector;
    }

    private void append() {
      String part = jsTrim(selector.substring(start, index));
      int eqIndex = part.indexOf('=');
      String name;
      String body;
      if (eqIndex != -1 && jsTrim(part.substring(0, eqIndex)).matches("[a-zA-Z_0-9\\-+:*]+")) {
        name = jsTrim(part.substring(0, eqIndex));
        body = part.substring(eqIndex + 1);
      } else if (part.length() > 1 && part.charAt(0) == '"' && part.charAt(part.length() - 1) == '"') {
        name = "text";
        body = part;
      } else if (part.length() > 1 && part.charAt(0) == '\'' && part.charAt(part.length() - 1) == '\'') {
        name = "text";
        body = part;
      } else if (Pattern.compile("^\\(*//").matcher(part).find() || part.startsWith("..")) {
        name = "xpath";
        body = part;
      } else {
        name = "css";
        body = part;
      }
      boolean capture = false;
      if (name.charAt(0) == '*') {
        capture = true;
        name = name.substring(1);
      }
      result.parts.add(new ParsedSelectorStringPart(name, body));
      if (capture) {
        if (result.capture != null)
          throw new InvalidSelectorError("Only one of the selectors can capture using * modifier");
        result.capture = result.parts.size() - 1;
      }
    }

    // java: the dot of a JS RegExp does not match the line terminators, and its $ only matches at the end.
    private boolean shouldIgnoreTextSelectorQuote() {
      String prefix = selector.substring(start, index);
      Matcher match = Pattern.compile(JS_WHITESPACE + "*text" + JS_WHITESPACE + "*=([^\\n\\r\\u2028\\u2029]*)").matcher(prefix);
      return match.matches() && !match.group(1).isEmpty();
    }

    ParsedSelectorStrings parse() {
      if (!selector.contains(">>")) {
        index = selector.length();
        append();
        return result;
      }

      while (index < selector.length()) {
        String c = selector.substring(index, index + 1);
        if (c.equals("\\") && index + 1 < selector.length()) {
          index += 2;
        } else if (c.equals(quote)) {
          quote = null;
          index++;
        } else if (quote == null && (c.equals("\"") || c.equals("'") || c.equals("`")) && !shouldIgnoreTextSelectorQuote()) {
          quote = c;
          index++;
        } else if (quote == null && c.equals(">") && index + 1 < selector.length() && selector.charAt(index + 1) == '>') {
          append();
          index += 2;
          start = index;
        } else {
          index++;
        }
      }
      append();
      return result;
    }
  }

  // java: the value is an Object holding a String, a Boolean, a Double, a RegExp or null.
  public static class AttributeSelectorPart {
    public String name;
    public List<String> jsonPath;
    public String op;
    public Object value;
    public boolean caseSensitive;

    AttributeSelectorPart(String name, List<String> jsonPath, String op, Object value, boolean caseSensitive) {
      this.name = name;
      this.jsonPath = jsonPath;
      this.op = op;
      this.value = value;
      this.caseSensitive = caseSensitive;
    }
  }

  public static class AttributeSelector {
    public String name = "";
    public List<AttributeSelectorPart> attributes = new ArrayList<>();
  }


  public static AttributeSelector parseAttributeSelector(String selector, boolean allowUnquotedStrings) {
    return new AttributeSelectorParser(selector, allowUnquotedStrings).parse();
  }

  // java: the local variables and the nested functions of parseAttributeSelector() are the fields and the methods of
  // this class. A character is a String of one char, empty at the end of the selector.
  private static class AttributeSelectorParser {
    private final String selector;
    private final boolean allowUnquotedStrings;
    private int wp = 0;
    private boolean EOL;

    AttributeSelectorParser(String selector, boolean allowUnquotedStrings) {
      this.selector = selector;
      this.allowUnquotedStrings = allowUnquotedStrings;
      EOL = selector.length() == 0;
    }

    private String next() {
      return wp < selector.length() ? selector.substring(wp, wp + 1) : "";
    }

    private String eat1() {
      String result = next();
      ++wp;
      EOL = wp >= selector.length();
      return result;
    }

    private void syntaxError(String stage) {
      if (EOL)
        throw new InvalidSelectorError("Unexpected end of selector while parsing selector `" + selector + "`");
      throw new InvalidSelectorError("Error while parsing selector `" + selector + "` - unexpected symbol \"" + next() + "\" at position " + wp + (stage != null ? " during " + stage : ""));
    }

    private void skipSpaces() {
      while (!EOL && next().matches(JS_WHITESPACE))
        eat1();
    }

    // java: an empty string is not a name char; upstream compares it with the range bounds, which gives false.
    private boolean isCSSNameChar(String ch) {
      if (ch.isEmpty())
        return false;
      char c = ch.charAt(0);
      return (c >= '\u0080')
          || (c >= '0' && c <= '9')
          || (c >= 'A' && c <= 'Z')
          || (c >= 'a' && c <= 'z')
          || (c >= '0' && c <= '9')
          || c == '_'
          || c == '-';
    }

    private String readIdentifier() {
      String result = "";
      skipSpaces();
      while (!EOL && isCSSNameChar(next()))
        result += eat1();
      return result;
    }

    private String readQuotedString(String quote) {
      String result = eat1();
      if (!result.equals(quote))
        syntaxError("parsing quoted string");
      while (!EOL && !next().equals(quote)) {
        if (next().equals("\\"))
          eat1();
        result += eat1();
      }
      if (!next().equals(quote))
        syntaxError("parsing quoted string");
      result += eat1();
      return result;
    }

    private RegExp readRegularExpression() {
      if (!eat1().equals("/"))
        syntaxError("parsing regular expression");
      String source = "";
      boolean inClass = false;
      while (!EOL) {
        if (next().equals("\\")) {
          source += eat1();
          if (EOL)
            syntaxError("parsing regular expression");
        } else if (inClass && next().equals("]")) {
          inClass = false;
        } else if (!inClass && next().equals("[")) {
          inClass = true;
        } else if (!inClass && next().equals("/")) {
          break;
        }
        source += eat1();
      }
      if (!eat1().equals("/"))
        syntaxError("parsing regular expression");
      String flags = "";
      while (!EOL && next().matches("[dgimsuvy]"))
        flags += eat1();
      try {
        return new RegExp(source, flags);
      } catch (RuntimeException e) {
        throw new InvalidSelectorError("Error while parsing selector `" + selector + "`: " + e.getMessage());
      }
    }

    private String readAttributeToken() {
      String token = "";
      skipSpaces();
      if (next().equals("'") || next().equals("\"")) {
        String quoted = readQuotedString(next());
        token = quoted.substring(1, quoted.length() - 1);
      } else {
        token = readIdentifier();
      }
      if (token.isEmpty())
        syntaxError("parsing property path");
      return token;
    }

    private String readOperator() {
      skipSpaces();
      String op = "";
      if (!EOL)
        op += eat1();
      if (!EOL && (!op.equals("=")))
        op += eat1();
      if (!Arrays.asList("=", "*=", "^=", "$=", "|=", "~=").contains(op))
        syntaxError("parsing operator");
      return op;
    }

    private AttributeSelectorPart readAttribute() {
      eat1();

      List<String> jsonPath = new ArrayList<>();
      jsonPath.add(readAttributeToken());
      skipSpaces();
      while (next().equals(".")) {
        eat1();
        jsonPath.add(readAttributeToken());
        skipSpaces();
      }
      if (next().equals("]")) {
        eat1();
        return new AttributeSelectorPart(String.join(".", jsonPath), jsonPath, "<truthy>", null, false);
      }

      String operator = readOperator();

      Object value = null;
      boolean caseSensitive = true;
      skipSpaces();
      if (next().equals("/")) {
        if (!operator.equals("="))
          throw new InvalidSelectorError("Error while parsing selector `" + selector + "` - cannot use " + operator + " in attribute with regular expression");
        value = readRegularExpression();
      } else if (next().equals("'") || next().equals("\"")) {
        String quoted = readQuotedString(next());
        value = quoted.substring(1, quoted.length() - 1);
        skipSpaces();
        if (next().equals("i") || next().equals("I")) {
          caseSensitive = false;
          eat1();
        } else if (next().equals("s") || next().equals("S")) {
          caseSensitive = true;
          eat1();
        }
      } else {
        String unquoted = "";
        while (!EOL && (isCSSNameChar(next()) || next().equals("+") || next().equals(".")))
          unquoted += eat1();
        value = unquoted;
        if (unquoted.equals("true")) {
          value = true;
        } else if (unquoted.equals("false")) {
          value = false;
        } else {
          if (!allowUnquotedStrings) {
            value = toNumber(unquoted);
            if (Double.isNaN((Double) value))
              syntaxError("parsing attribute value");
          }
        }
      }
      skipSpaces();
      if (!next().equals("]"))
        syntaxError("parsing attribute value");

      eat1();
      if (!operator.equals("=") && !(value instanceof String))
        throw new InvalidSelectorError("Error while parsing selector `" + selector + "` - cannot use " + operator + " in attribute with non-string matching value - " + valueToString(value));
      return new AttributeSelectorPart(String.join(".", jsonPath), jsonPath, operator, value, caseSensitive);
    }

    AttributeSelector parse() {
      AttributeSelector result = new AttributeSelector();
      result.name = readIdentifier();
      skipSpaces();
      while (next().equals("[")) {
        result.attributes.add(readAttribute());
        skipSpaces();
      }
      if (!EOL)
        syntaxError(null);
      if (result.name.isEmpty() && result.attributes.isEmpty())
        throw new InvalidSelectorError("Error while parsing selector `" + selector + "` - selector cannot be empty");
      return result;
    }
  }

  // java: helpers with no counterpart upstream.

  private static boolean isJsonString(JsonElement element) {
    return element.isJsonPrimitive() && element.getAsJsonPrimitive().isString();
  }

  // java: Array.prototype.slice, which clamps the indexes to the length.
  private static <T> List<T> slice(List<T> list, int start, int end) {
    return new ArrayList<>(list.subList(Math.min(start, list.size()), Math.min(end, list.size())));
  }

  // java: String(value) of an attribute value.
  static String valueToString(Object value) {
    if (value instanceof Double)
      return numberToString((Double) value);
    return String.valueOf(value);
  }
}
