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

// Port of isomorphic/cssParser.ts.
package com.microsoft.playwright.impl.isomorphic;

import com.microsoft.playwright.PlaywrightException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

// java: the types of the file are nested classes. A CSSFunctionArgument is an Object holding a CSSComplexSelector,
// a Double or a String. serializeSelector is not ported, the locator generators do not use it.
public final class CssParser {
  private CssParser() {
  }

  public static class InvalidSelectorError extends PlaywrightException {
    public InvalidSelectorError(String message) {
      super(message);
    }
  }

  public static boolean isInvalidSelectorError(Throwable error) {
    return error instanceof InvalidSelectorError;
  }

  public static class CSSFunction {
    public String name;
    public List<Object> args;

    CSSFunction(String name, List<Object> args) {
      this.name = name;
      this.args = args;
    }
  }

  public static class CSSSimpleSelector {
    public String css;
    public List<CSSFunction> functions;

    CSSSimpleSelector(String css, List<CSSFunction> functions) {
      this.css = css;
      this.functions = functions;
    }
  }

  // java: the type of an item of CSSComplexSelector.simples.
  public static class CSSSimple {
    public CSSSimpleSelector selector;
    public String combinator;

    CSSSimple(CSSSimpleSelector selector, String combinator) {
      this.selector = selector;
      this.combinator = combinator;
    }
  }

  public static class CSSComplexSelector {
    public List<CSSSimple> simples = new ArrayList<>();
  }

  // java: the object that parseCSS() returns.
  public static class ParsedCSS {
    public List<CSSComplexSelector> selector;
    public List<String> names;
  }

  public static ParsedCSS parseCSS(String selector, Set<String> customNames) {
    List<CssTokenizer.CSSTokenInterface> tokens;
    try {
      tokens = CssTokenizer.tokenize(selector);
      // java: the item before the first one of an empty list is undefined upstream.
      if (tokens.isEmpty() || !(tokens.get(tokens.size() - 1) instanceof CssTokenizer.EOFToken))
        tokens.add(new CssTokenizer.EOFToken());
    } catch (RuntimeException e) {
      // java: the message of an exception is final, so a new exception with the new message wraps the caught one.
      String newMessage = e.getMessage() + " while parsing css selector \"" + selector + "\". Did you mean to CSS.escape it?";
      throw new PlaywrightException(newMessage, e);
    }
    CssTokenizer.CSSTokenInterface unsupportedToken = null;
    for (CssTokenizer.CSSTokenInterface token : tokens) {
      if ((token instanceof CssTokenizer.AtKeywordToken) ||
        (token instanceof CssTokenizer.BadStringToken) ||
        (token instanceof CssTokenizer.BadURLToken) ||
        (token instanceof CssTokenizer.ColumnToken) ||
        (token instanceof CssTokenizer.CDOToken) ||
        (token instanceof CssTokenizer.CDCToken) ||
        (token instanceof CssTokenizer.SemicolonToken) ||
        (token instanceof CssTokenizer.OpenCurlyToken) ||
        (token instanceof CssTokenizer.CloseCurlyToken) ||
        (token instanceof CssTokenizer.URLToken) ||
        (token instanceof CssTokenizer.PercentageToken)) {
        unsupportedToken = token;
        break;
      }
    }
    if (unsupportedToken != null)
      throw new InvalidSelectorError("Unsupported token \"" + unsupportedToken.toSource() + "\" while parsing css selector \"" + selector + "\". Did you mean to CSS.escape it?");

    Parser parser = new Parser(selector, customNames, tokens);
    List<Object> result = parser.consumeFunctionArguments();
    if (!parser.isEOF())
      throw parser.unexpected();
    List<CSSComplexSelector> complexSelectors = new ArrayList<>();
    for (Object arg : result) {
      if (!(arg instanceof CSSComplexSelector))
        throw new InvalidSelectorError("Error while parsing css selector \"" + selector + "\". Did you mean to CSS.escape it?");
      complexSelectors.add((CSSComplexSelector) arg);
    }
    ParsedCSS parsed = new ParsedCSS();
    parsed.selector = complexSelectors;
    parsed.names = new ArrayList<>(parser.names);
    return parsed;
  }

  // java: the local variables and the nested functions of parseCSS() are the fields and the methods of this class.
  private static class Parser {
    private final String selector;
    private final Set<String> customNames;
    private final List<CssTokenizer.CSSTokenInterface> tokens;
    private int pos = 0;
    private final Set<String> names = new LinkedHashSet<>();

    Parser(String selector, Set<String> customNames, List<CssTokenizer.CSSTokenInterface> tokens) {
      this.selector = selector;
      this.customNames = customNames;
      this.tokens = tokens;
    }

    InvalidSelectorError unexpected() {
      return new InvalidSelectorError("Unexpected token \"" + tokens.get(pos).toSource() + "\" while parsing css selector \"" + selector + "\". Did you mean to CSS.escape it?");
    }

    void skipWhitespace() {
      while (tokens.get(pos) instanceof CssTokenizer.WhitespaceToken)
        pos++;
    }

    boolean isIdent() {
      return isIdent(pos);
    }

    boolean isIdent(int p) {
      return tokens.get(p) instanceof CssTokenizer.IdentToken;
    }

    boolean isString() {
      return isString(pos);
    }

    boolean isString(int p) {
      return tokens.get(p) instanceof CssTokenizer.StringToken;
    }

    boolean isNumber() {
      return isNumber(pos);
    }

    boolean isNumber(int p) {
      return tokens.get(p) instanceof CssTokenizer.NumberToken;
    }

    boolean isComma() {
      return isComma(pos);
    }

    boolean isComma(int p) {
      return tokens.get(p) instanceof CssTokenizer.CommaToken;
    }

    boolean isOpenParen() {
      return isOpenParen(pos);
    }

    boolean isOpenParen(int p) {
      return tokens.get(p) instanceof CssTokenizer.OpenParenToken;
    }

    boolean isCloseParen() {
      return isCloseParen(pos);
    }

    boolean isCloseParen(int p) {
      return tokens.get(p) instanceof CssTokenizer.CloseParenToken;
    }

    boolean isFunction() {
      return isFunction(pos);
    }

    boolean isFunction(int p) {
      return tokens.get(p) instanceof CssTokenizer.FunctionToken;
    }

    boolean isStar() {
      return isStar(pos);
    }

    boolean isStar(int p) {
      return (tokens.get(p) instanceof CssTokenizer.DelimToken) && "*".equals(tokens.get(p).value());
    }

    boolean isEOF() {
      return isEOF(pos);
    }

    boolean isEOF(int p) {
      return tokens.get(p) instanceof CssTokenizer.EOFToken;
    }

    boolean isClauseCombinator() {
      return isClauseCombinator(pos);
    }

    boolean isClauseCombinator(int p) {
      return (tokens.get(p) instanceof CssTokenizer.DelimToken) && (Arrays.asList(">", "+", "~").contains((String) tokens.get(p).value()));
    }

    boolean isSelectorClauseEnd() {
      return isSelectorClauseEnd(pos);
    }

    boolean isSelectorClauseEnd(int p) {
      return isComma(p) || isCloseParen(p) || isEOF(p) || isClauseCombinator(p) || (tokens.get(p) instanceof CssTokenizer.WhitespaceToken);
    }

    List<Object> consumeFunctionArguments() {
      List<Object> result = new ArrayList<>();
      result.add(consumeArgument());
      while (true) {
        skipWhitespace();
        if (!isComma())
          break;
        pos++;
        result.add(consumeArgument());
      }
      return result;
    }

    Object consumeArgument() {
      skipWhitespace();
      if (isNumber())
        return tokens.get(pos++).value();
      if (isString())
        return tokens.get(pos++).value();
      return consumeComplexSelector();
    }

    CSSComplexSelector consumeComplexSelector() {
      CSSComplexSelector result = new CSSComplexSelector();
      skipWhitespace();
      if (isClauseCombinator()) {
        List<CSSFunction> functions = new ArrayList<>();
        functions.add(new CSSFunction("scope", new ArrayList<>()));
        result.simples.add(new CSSSimple(new CSSSimpleSelector(null, functions), ""));
      } else {
        result.simples.add(new CSSSimple(consumeSimpleSelector(), ""));
      }
      while (true) {
        skipWhitespace();
        if (isClauseCombinator()) {
          result.simples.get(result.simples.size() - 1).combinator = (String) tokens.get(pos++).value();
          skipWhitespace();
        } else if (isSelectorClauseEnd()) {
          break;
        }
        result.simples.add(new CSSSimple(consumeSimpleSelector(), ""));
      }
      return result;
    }

    CSSSimpleSelector consumeSimpleSelector() {
      String rawCSSString = "";
      List<CSSFunction> functions = new ArrayList<>();

      while (!isSelectorClauseEnd()) {
        if (isIdent() || isStar()) {
          rawCSSString += tokens.get(pos++).toSource();
        } else if (tokens.get(pos) instanceof CssTokenizer.HashToken) {
          rawCSSString += tokens.get(pos++).toSource();
        } else if ((tokens.get(pos) instanceof CssTokenizer.DelimToken) && ".".equals(tokens.get(pos).value())) {
          pos++;
          if (isIdent())
            rawCSSString += "." + tokens.get(pos++).toSource();
          else
            throw unexpected();
        } else if (tokens.get(pos) instanceof CssTokenizer.ColonToken) {
          pos++;
          if (isIdent()) {
            if (!customNames.contains(((String) tokens.get(pos).value()).toLowerCase(Locale.ROOT))) {
              rawCSSString += ":" + tokens.get(pos++).toSource();
            } else {
              String name = ((String) tokens.get(pos++).value()).toLowerCase(Locale.ROOT);
              functions.add(new CSSFunction(name, new ArrayList<>()));
              names.add(name);
            }
          } else if (isFunction()) {
            String name = ((String) tokens.get(pos++).value()).toLowerCase(Locale.ROOT);
            if (!customNames.contains(name)) {
              rawCSSString += ":" + name + "(" + consumeBuiltinFunctionArguments() + ")";
            } else {
              functions.add(new CSSFunction(name, consumeFunctionArguments()));
              names.add(name);
            }
            skipWhitespace();
            if (!isCloseParen())
              throw unexpected();
            pos++;
          } else {
            throw unexpected();
          }
        } else if (tokens.get(pos) instanceof CssTokenizer.OpenSquareToken) {
          rawCSSString += "[";
          pos++;
          while (!(tokens.get(pos) instanceof CssTokenizer.CloseSquareToken) && !isEOF())
            rawCSSString += tokens.get(pos++).toSource();
          if (!(tokens.get(pos) instanceof CssTokenizer.CloseSquareToken))
            throw unexpected();
          rawCSSString += "]";
          pos++;
        } else {
          throw unexpected();
        }
      }
      if (rawCSSString.isEmpty() && functions.isEmpty())
        throw unexpected();
      return new CSSSimpleSelector(rawCSSString.isEmpty() ? null : rawCSSString, functions);
    }

    String consumeBuiltinFunctionArguments() {
      String s = "";
      int balance = 1;
      while (!isEOF()) {
        if (isOpenParen() || isFunction())
          balance++;
        if (isCloseParen())
          balance--;
        if (balance == 0)
          break;
        s += tokens.get(pos++).toSource();
      }
      return s;
    }
  }
}
