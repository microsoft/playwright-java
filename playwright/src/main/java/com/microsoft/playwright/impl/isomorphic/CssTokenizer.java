/*
 * The code in this file is licensed under the CC0 license.
 * http://creativecommons.org/publicdomain/zero/1.0/
 * It is free to use for any purpose. No attribution, permission, or reproduction of this license is required.
 */

// Port of isomorphic/cssTokenizer.ts.
package com.microsoft.playwright.impl.isomorphic;

import com.microsoft.playwright.PlaywrightException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// java: the token classes are nested classes of this class. A code point is an int, -1 where upstream reads NaN.
// A number value is a Double. toJSON is not ported.
public final class CssTokenizer {
  private CssTokenizer() {
  }

  // java: the value property is a method.
  public interface CSSTokenInterface {
    String toSource();
    Object value();
  }

  private static boolean between(int num, int first, int last) { return num >= first && num <= last; }
  private static boolean digit(int code) { return between(code, 0x30, 0x39); }
  private static boolean hexdigit(int code) { return digit(code) || between(code, 0x41, 0x46) || between(code, 0x61, 0x66); }
  private static boolean uppercaseletter(int code) { return between(code, 0x41, 0x5a); }
  private static boolean lowercaseletter(int code) { return between(code, 0x61, 0x7a); }
  private static boolean letter(int code) { return uppercaseletter(code) || lowercaseletter(code); }
  private static boolean nonascii(int code) { return code >= 0x80; }
  private static boolean namestartchar(int code) { return letter(code) || nonascii(code) || code == 0x5f; }
  private static boolean namechar(int code) { return namestartchar(code) || digit(code) || code == 0x2d; }
  private static boolean nonprintable(int code) { return between(code, 0, 8) || code == 0xb || between(code, 0xe, 0x1f) || code == 0x7f; }
  private static boolean newline(int code) { return code == 0xa; }
  private static boolean whitespace(int code) { return newline(code) || code == 9 || code == 0x20; }

  private static final int maximumallowedcodepoint = 0x10ffff;

  public static class InvalidCharacterError extends PlaywrightException {
    public InvalidCharacterError(String message) {
      super(message);
    }
  }

  private static List<Integer> preprocess(String str) {
    List<Integer> codepoints = new ArrayList<>();
    for (int i = 0; i < str.length(); i++) {
      int code = charCodeAt(str, i);
      if (code == 0xd && charCodeAt(str, i + 1) == 0xa) {
        code = 0xa; i++;
      }
      if (code == 0xd || code == 0xc)
        code = 0xa;
      if (code == 0x0)
        code = 0xfffd;
      if (between(code, 0xd800, 0xdbff) && between(charCodeAt(str, i + 1), 0xdc00, 0xdfff)) {
        int lead = code - 0xd800;
        int trail = charCodeAt(str, i + 1) - 0xdc00;
        code = (1 << 16) + lead * (1 << 10) + trail;
        i++;
      }
      codepoints.add(code);
    }
    return codepoints;
  }

  private static String stringFromCode(int code) {
    if (code <= 0xffff)
      return String.valueOf((char) code);
    code -= 1 << 16;
    int lead = code / (1 << 10) + 0xd800;
    int trail = code % (1 << 10) + 0xdc00;
    return String.valueOf((char) lead) + String.valueOf((char) trail);
  }

  public static List<CSSTokenInterface> tokenize(String str1) {
    return new Tokenizer(str1).tokenize();
  }

  // java: the local variables and the closures of tokenize() are the fields and the methods of this class.
  private static class Tokenizer {
    private final List<Integer> str;
    private int i = -1;
    private final List<CSSTokenInterface> tokens = new ArrayList<>();
    private int code;

    private int line = 0;
    private int column = 0;
    private int lastLineLength = 0;
    // java: locStart is two fields.
    private int locStartLine = line;
    private int locStartColumn = column;

    Tokenizer(String str1) {
      str = preprocess(str1);
    }

    private void incrLineno() {
      line += 1;
      lastLineLength = column;
      column = 0;
    }

    private int codepoint(int i) {
      if (i >= str.size())
        return -1;

      return str.get(i);
    }

    private int next() {
      return next(1);
    }

    private int next(int num) {
      if (num > 3)
        throw new PlaywrightException("Spec Error: no more than three codepoints of lookahead.");
      return codepoint(i + num);
    }

    private boolean consume() {
      return consume(1);
    }

    private boolean consume(int num) {
      i += num;
      code = codepoint(i);
      if (newline(code))
        incrLineno();
      else
        column += num;
      return true;
    }

    private boolean reconsume() {
      i -= 1;
      if (newline(code)) {
        line -= 1;
        column = lastLineLength;
      } else {
        column -= 1;
      }
      locStartLine = line;
      locStartColumn = column;
      return true;
    }

    private boolean eof() {
      return eof(code);
    }

    private boolean eof(int codepoint) {
      return codepoint == -1;
    }

    private void donothing() {
    }

    private void parseerror() {
    }

    private CSSTokenInterface consumeAToken() {
      consumeComments();
      consume();
      if (whitespace(code)) {
        while (whitespace(next()))
          consume();
        return new WhitespaceToken();
      } else if (code == 0x22) {return consumeAStringToken();} else if (code == 0x23) {
        if (namechar(next()) || areAValidEscape(next(1), next(2))) {
          HashToken token = new HashToken("");
          if (wouldStartAnIdentifier(next(1), next(2), next(3)))
            token.type = "id";
          token.value = consumeAName();
          return token;
        } else {
          return new DelimToken(code);
        }
      } else if (code == 0x24) {
        if (next() == 0x3d) {
          consume();
          return new SuffixMatchToken();
        } else {
          return new DelimToken(code);
        }
      } else if (code == 0x27) {return consumeAStringToken();} else if (code == 0x28) {return new OpenParenToken();} else if (code == 0x29) {return new CloseParenToken();} else if (code == 0x2a) {
        if (next() == 0x3d) {
          consume();
          return new SubstringMatchToken();
        } else {
          return new DelimToken(code);
        }
      } else if (code == 0x2b) {
        if (startsWithANumber()) {
          reconsume();
          return consumeANumericToken();
        } else {
          return new DelimToken(code);
        }
      } else if (code == 0x2c) {return new CommaToken();} else if (code == 0x2d) {
        if (startsWithANumber()) {
          reconsume();
          return consumeANumericToken();
        } else if (next(1) == 0x2d && next(2) == 0x3e) {
          consume(2);
          return new CDCToken();
        } else if (startsWithAnIdentifier()) {
          reconsume();
          return consumeAnIdentlikeToken();
        } else {
          return new DelimToken(code);
        }
      } else if (code == 0x2e) {
        if (startsWithANumber()) {
          reconsume();
          return consumeANumericToken();
        } else {
          return new DelimToken(code);
        }
      } else if (code == 0x3a) {return new ColonToken();} else if (code == 0x3b) {return new SemicolonToken();} else if (code == 0x3c) {
        if (next(1) == 0x21 && next(2) == 0x2d && next(3) == 0x2d) {
          consume(3);
          return new CDOToken();
        } else {
          return new DelimToken(code);
        }
      } else if (code == 0x40) {
        if (wouldStartAnIdentifier(next(1), next(2), next(3)))
          return new AtKeywordToken(consumeAName());
        else
          return new DelimToken(code);

      } else if (code == 0x5b) {return new OpenSquareToken();} else if (code == 0x5c) {
        if (startsWithAValidEscape()) {
          reconsume();
          return consumeAnIdentlikeToken();
        } else {
          parseerror();
          return new DelimToken(code);
        }
      } else if (code == 0x5d) {return new CloseSquareToken();} else if (code == 0x5e) {
        if (next() == 0x3d) {
          consume();
          return new PrefixMatchToken();
        } else {
          return new DelimToken(code);
        }
      } else if (code == 0x7b) {return new OpenCurlyToken();} else if (code == 0x7c) {
        if (next() == 0x3d) {
          consume();
          return new DashMatchToken();
        } else if (next() == 0x7c) {
          consume();
          return new ColumnToken();
        } else {
          return new DelimToken(code);
        }
      } else if (code == 0x7d) {return new CloseCurlyToken();} else if (code == 0x7e) {
        if (next() == 0x3d) {
          consume();
          return new IncludeMatchToken();
        } else {
          return new DelimToken(code);
        }
      } else if (digit(code)) {
        reconsume();
        return consumeANumericToken();
      } else if (namestartchar(code)) {
        reconsume();
        return consumeAnIdentlikeToken();
      } else if (eof()) {return new EOFToken();} else {return new DelimToken(code);}
    }

    private void consumeComments() {
      while (next(1) == 0x2f && next(2) == 0x2a) {
        consume(2);
        while (true) {
          consume();
          if (code == 0x2a && next() == 0x2f) {
            consume();
            break;
          } else if (eof()) {
            parseerror();
            return;
          }
        }
      }
    }

    private CSSTokenInterface consumeANumericToken() {
      ConsumedNumber num = consumeANumber();
      if (wouldStartAnIdentifier(next(1), next(2), next(3))) {
        DimensionToken token = new DimensionToken();
        token.value = num.value;
        token.repr = num.repr;
        token.type = num.type;
        token.unit = consumeAName();
        return token;
      } else if (next() == 0x25) {
        consume();
        PercentageToken token = new PercentageToken();
        token.value = num.value;
        token.repr = num.repr;
        return token;
      } else {
        NumberToken token = new NumberToken();
        token.value = num.value;
        token.repr = num.repr;
        token.type = num.type;
        return token;
      }
    }

    private CSSTokenInterface consumeAnIdentlikeToken() {
      String str = consumeAName();
      if (str.toLowerCase(Locale.ROOT).equals("url") && next() == 0x28) {
        consume();
        while (whitespace(next(1)) && whitespace(next(2)))
          consume();
        if (next() == 0x22 || next() == 0x27)
          return new FunctionToken(str);
        else if (whitespace(next()) && (next(2) == 0x22 || next(2) == 0x27))
          return new FunctionToken(str);
        else
          return consumeAURLToken();

      } else if (next() == 0x28) {
        consume();
        return new FunctionToken(str);
      } else {
        return new IdentToken(str);
      }
    }

    private CSSParserToken consumeAStringToken() {
      return consumeAStringToken(code);
    }

    private CSSParserToken consumeAStringToken(int endingCodePoint) {
      String string = "";
      while (consume()) {
        if (code == endingCodePoint || eof()) {
          return new StringToken(string);
        } else if (newline(code)) {
          parseerror();
          reconsume();
          return new BadStringToken();
        } else if (code == 0x5c) {
          if (eof(next()))
            donothing();
          else if (newline(next()))
            consume();
          else
            string += stringFromCode(consumeEscape());

        } else {
          string += stringFromCode(code);
        }
      }
      throw new PlaywrightException("Internal error");
    }

    private CSSTokenInterface consumeAURLToken() {
      URLToken token = new URLToken("");
      while (whitespace(next()))
        consume();
      if (eof(next()))
        return token;
      while (consume()) {
        if (code == 0x29 || eof()) {
          return token;
        } else if (whitespace(code)) {
          while (whitespace(next()))
            consume();
          if (next() == 0x29 || eof(next())) {
            consume();
            return token;
          } else {
            consumeTheRemnantsOfABadURL();
            return new BadURLToken();
          }
        } else if (code == 0x22 || code == 0x27 || code == 0x28 || nonprintable(code)) {
          parseerror();
          consumeTheRemnantsOfABadURL();
          return new BadURLToken();
        } else if (code == 0x5c) {
          if (startsWithAValidEscape()) {
            token.value = token.value + stringFromCode(consumeEscape());
          } else {
            parseerror();
            consumeTheRemnantsOfABadURL();
            return new BadURLToken();
          }
        } else {
          token.value = token.value + stringFromCode(code);
        }
      }
      throw new PlaywrightException("Internal error");
    }

    private int consumeEscape() {
      consume();
      if (hexdigit(code)) {
        List<Integer> digits = new ArrayList<>();
        digits.add(code);
        for (int total = 0; total < 5; total++) {
          if (hexdigit(next())) {
            consume();
            digits.add(code);
          } else {
            break;
          }
        }
        if (whitespace(next()))
          consume();
        StringBuilder joined = new StringBuilder();
        for (int x : digits)
          joined.append((char) x);
        int value = Integer.parseInt(joined.toString(), 16);
        if (value > maximumallowedcodepoint)
          value = 0xfffd;
        return value;
      } else if (eof()) {
        return 0xfffd;
      } else {
        return code;
      }
    }

    private boolean areAValidEscape(int c1, int c2) {
      if (c1 != 0x5c)
        return false;
      if (newline(c2))
        return false;
      return true;
    }

    private boolean startsWithAValidEscape() {
      return areAValidEscape(code, next());
    }

    private boolean wouldStartAnIdentifier(int c1, int c2, int c3) {
      if (c1 == 0x2d)
        return namestartchar(c2) || c2 == 0x2d || areAValidEscape(c2, c3);
      else if (namestartchar(c1))
        return true;
      else if (c1 == 0x5c)
        return areAValidEscape(c1, c2);
      else
        return false;

    }

    private boolean startsWithAnIdentifier() {
      return wouldStartAnIdentifier(code, next(1), next(2));
    }

    private boolean wouldStartANumber(int c1, int c2, int c3) {
      if (c1 == 0x2b || c1 == 0x2d) {
        if (digit(c2))
          return true;
        if (c2 == 0x2e && digit(c3))
          return true;
        return false;
      } else if (c1 == 0x2e) {
        if (digit(c2))
          return true;
        return false;
      } else if (digit(c1)) {
        return true;
      } else {
        return false;
      }
    }

    private boolean startsWithANumber() {
      return wouldStartANumber(code, next(1), next(2));
    }

    private String consumeAName() {
      String result = "";
      while (consume()) {
        if (namechar(code)) {
          result += stringFromCode(code);
        } else if (startsWithAValidEscape()) {
          result += stringFromCode(consumeEscape());
        } else {
          reconsume();
          return result;
        }
      }
      throw new PlaywrightException("Internal parse error");
    }

    private ConsumedNumber consumeANumber() {
      String repr = "";
      String type = "integer";
      if (next() == 0x2b || next() == 0x2d) {
        consume();
        repr += stringFromCode(code);
      }
      while (digit(next())) {
        consume();
        repr += stringFromCode(code);
      }
      if (next(1) == 0x2e && digit(next(2))) {
        consume();
        repr += stringFromCode(code);
        consume();
        repr += stringFromCode(code);
        type = "number";
        while (digit(next())) {
          consume();
          repr += stringFromCode(code);
        }
      }
      int c1 = next(1);
      int c2 = next(2);
      int c3 = next(3);
      if ((c1 == 0x45 || c1 == 0x65) && digit(c2)) {
        consume();
        repr += stringFromCode(code);
        consume();
        repr += stringFromCode(code);
        type = "number";
        while (digit(next())) {
          consume();
          repr += stringFromCode(code);
        }
      } else if ((c1 == 0x45 || c1 == 0x65) && (c2 == 0x2b || c2 == 0x2d) && digit(c3)) {
        consume();
        repr += stringFromCode(code);
        consume();
        repr += stringFromCode(code);
        consume();
        repr += stringFromCode(code);
        type = "number";
        while (digit(next())) {
          consume();
          repr += stringFromCode(code);
        }
      }
      double value = convertAStringToANumber(repr);
      ConsumedNumber result = new ConsumedNumber();
      result.type = type;
      result.value = value;
      result.repr = repr;
      return result;
    }

    // java: the repr is a decimal literal with a digit after the dot, which Double.parseDouble reads like the unary plus.
    private double convertAStringToANumber(String string) {
      return Double.parseDouble(string);
    }

    private void consumeTheRemnantsOfABadURL() {
      while (consume()) {
        if (code == 0x29 || eof()) {
          return;
        } else if (startsWithAValidEscape()) {
          consumeEscape();
          donothing();
        } else {
          donothing();
        }
      }
    }

    List<CSSTokenInterface> tokenize() {
      int iterationCount = 0;
      while (!eof(next())) {
        tokens.add(consumeAToken());
        iterationCount++;
        if (iterationCount > str.size() * 2)
          throw new PlaywrightException("I'm infinite-looping!");
      }
      return tokens;
    }
  }

  // java: the object that consumeANumber() returns.
  private static class ConsumedNumber {
    String type;
    double value;
    String repr;
  }

  // java: a subclass sets tokenType and value in an initializer block or in its constructor.
  public static class CSSParserToken implements CSSTokenInterface {
    String tokenType = "";
    Object value;
    @Override
    public String toString() { return tokenType; }
    @Override
    public String toSource() { return "" + this; }
    @Override
    public Object value() { return value; }
  }

  public static class BadStringToken extends CSSParserToken {
    { tokenType = "BADSTRING"; }
  }

  public static class BadURLToken extends CSSParserToken {
    { tokenType = "BADURL"; }
  }

  public static class WhitespaceToken extends CSSParserToken {
    { tokenType = "WHITESPACE"; }
    @Override
    public String toString() { return "WS"; }
    @Override
    public String toSource() { return " "; }
  }

  public static class CDOToken extends CSSParserToken {
    { tokenType = "CDO"; }
    @Override
    public String toSource() { return "<!--"; }
  }

  public static class CDCToken extends CSSParserToken {
    { tokenType = "CDC"; }
    @Override
    public String toSource() { return "-->"; }
  }

  public static class ColonToken extends CSSParserToken {
    { tokenType = ":"; }
  }

  public static class SemicolonToken extends CSSParserToken {
    { tokenType = ";"; }
  }

  public static class CommaToken extends CSSParserToken {
    { tokenType = ","; }
  }

  public static class GroupingToken extends CSSParserToken {
    { value = ""; }
    String mirror = "";
  }

  public static class OpenCurlyToken extends GroupingToken {
    { tokenType = "{"; }
    public OpenCurlyToken() {
      super();
      this.value = "{";
      this.mirror = "}";
    }
  }

  public static class CloseCurlyToken extends GroupingToken {
    { tokenType = "}"; }
    public CloseCurlyToken() {
      super();
      this.value = "}";
      this.mirror = "{";
    }
  }

  public static class OpenSquareToken extends GroupingToken {
    { tokenType = "["; }
    public OpenSquareToken() {
      super();
      this.value = "[";
      this.mirror = "]";
    }
  }

  public static class CloseSquareToken extends GroupingToken {
    { tokenType = "]"; }
    public CloseSquareToken() {
      super();
      this.value = "]";
      this.mirror = "[";
    }
  }

  public static class OpenParenToken extends GroupingToken {
    { tokenType = "("; }
    public OpenParenToken() {
      super();
      this.value = "(";
      this.mirror = ")";
    }
  }

  public static class CloseParenToken extends GroupingToken {
    { tokenType = ")"; }
    public CloseParenToken() {
      super();
      this.value = ")";
      this.mirror = "(";
    }
  }

  public static class IncludeMatchToken extends CSSParserToken {
    { tokenType = "~="; }
  }

  public static class DashMatchToken extends CSSParserToken {
    { tokenType = "|="; }
  }

  public static class PrefixMatchToken extends CSSParserToken {
    { tokenType = "^="; }
  }

  public static class SuffixMatchToken extends CSSParserToken {
    { tokenType = "$="; }
  }

  public static class SubstringMatchToken extends CSSParserToken {
    { tokenType = "*="; }
  }

  public static class ColumnToken extends CSSParserToken {
    { tokenType = "||"; }
  }

  public static class EOFToken extends CSSParserToken {
    { tokenType = "EOF"; }
    @Override
    public String toSource() { return ""; }
  }

  public static class DelimToken extends CSSParserToken {
    { tokenType = "DELIM"; }

    public DelimToken(int code) {
      super();
      this.value = stringFromCode(code);
    }

    @Override
    public String toString() { return "DELIM(" + this.value + ")"; }

    @Override
    public String toSource() {
      if (this.value.equals("\\"))
        return "\\\n";
      else
        return (String) this.value;
    }
  }

  public abstract static class StringValuedToken extends CSSParserToken {
    { value = ""; }
    public boolean ASCIIMatch(String str) {
      return ((String) this.value).toLowerCase(Locale.ROOT).equals(str.toLowerCase(Locale.ROOT));
    }
  }

  public static class IdentToken extends StringValuedToken {
    public IdentToken(String val) {
      super();
      this.value = val;
    }

    { tokenType = "IDENT"; }
    @Override
    public String toString() { return "IDENT(" + this.value + ")"; }
    @Override
    public String toSource() {
      return escapeIdent((String) this.value);
    }
  }

  public static class FunctionToken extends StringValuedToken {
    { tokenType = "FUNCTION"; }
    String mirror;
    public FunctionToken(String val) {
      super();
      this.value = val;
      this.mirror = ")";
    }

    @Override
    public String toString() { return "FUNCTION(" + this.value + ")"; }

    @Override
    public String toSource() {
      return escapeIdent((String) this.value) + "(";
    }
  }

  public static class AtKeywordToken extends StringValuedToken {
    { tokenType = "AT-KEYWORD"; }
    public AtKeywordToken(String val) {
      super();
      this.value = val;
    }
    @Override
    public String toString() { return "AT(" + this.value + ")"; }
    @Override
    public String toSource() {
      return "@" + escapeIdent((String) this.value);
    }
  }

  public static class HashToken extends StringValuedToken {
    { tokenType = "HASH"; }
    String type;
    public HashToken(String val) {
      super();
      this.value = val;
      this.type = "unrestricted";
    }

    @Override
    public String toString() { return "HASH(" + this.value + ")"; }

    @Override
    public String toSource() {
      if (this.type.equals("id"))
        return "#" + escapeIdent((String) this.value);
      else
        return "#" + escapeHash((String) this.value);

    }
  }

  public static class StringToken extends StringValuedToken {
    { tokenType = "STRING"; }
    public StringToken(String val) {
      super();
      this.value = val;
    }

    @Override
    public String toString() {
      return "\"" + escapeString((String) this.value) + "\"";
    }
  }

  public static class URLToken extends StringValuedToken {
    { tokenType = "URL"; }
    public URLToken(String val) {
      super();
      this.value = val;
    }
    @Override
    public String toString() { return "URL(" + this.value + ")"; }
    @Override
    public String toSource() {
      return "url(\"" + escapeString((String) this.value) + "\")";
    }
  }

  public static class NumberToken extends CSSParserToken {
    { tokenType = "NUMBER"; }
    String type;
    String repr;

    public NumberToken() {
      super();
      this.type = "integer";
      this.repr = "";
    }

    @Override
    public String toString() {
      if (this.type.equals("integer"))
        return "INT(" + this.value + ")";
      return "NUMBER(" + this.value + ")";
    }
    @Override
    public String toSource() { return this.repr; }
  }


  public static class PercentageToken extends CSSParserToken {
    { tokenType = "PERCENTAGE"; }
    String repr;
    public PercentageToken() {
      super();
      this.repr = "";
    }
    @Override
    public String toString() { return "PERCENTAGE(" + this.value + ")"; }
    @Override
    public String toSource() { return this.repr + "%"; }
  }

  public static class DimensionToken extends CSSParserToken {
    { tokenType = "DIMENSION"; }
    String type;
    String repr;
    String unit;

    public DimensionToken() {
      super();
      this.type = "integer";
      this.repr = "";
      this.unit = "";
    }

    @Override
    public String toString() { return "DIM(" + this.value + "," + this.unit + ")"; }
    @Override
    public String toSource() {
      String source = this.repr;
      String unit = escapeIdent(this.unit);
      if (unit.substring(0, 1).toLowerCase(Locale.ROOT).equals("e") && (charCodeAt(unit, 1) == '-' || between(charCodeAt(unit, 1), 0x30, 0x39))) {
        unit = "\\65 " + unit.substring(1, unit.length());
      }
      return source + unit;
    }
  }

  private static String escapeIdent(String string) {
    String result = "";
    int firstcode = charCodeAt(string, 0);
    for (int i = 0; i < string.length(); i++) {
      int code = charCodeAt(string, i);
      if (code == 0x0)
        throw new InvalidCharacterError("Invalid character: the input contains U+0000.");

      if (
        between(code, 0x1, 0x1f) || code == 0x7f ||
        (i == 0 && between(code, 0x30, 0x39)) ||
        (i == 1 && between(code, 0x30, 0x39) && firstcode == 0x2d)
      )
        result += "\\" + Integer.toString(code, 16) + " ";
      else if (
        code >= 0x80 ||
        code == 0x2d ||
        code == 0x5f ||
        between(code, 0x30, 0x39) ||
        between(code, 0x41, 0x5a) ||
        between(code, 0x61, 0x7a)
      )
        result += string.charAt(i);
      else
        result += "\\" + string.charAt(i);

    }
    return result;
  }

  private static String escapeHash(String string) {
    String result = "";
    for (int i = 0; i < string.length(); i++) {
      int code = charCodeAt(string, i);
      if (code == 0x0)
        throw new InvalidCharacterError("Invalid character: the input contains U+0000.");

      if (
        code >= 0x80 ||
        code == 0x2d ||
        code == 0x5f ||
        between(code, 0x30, 0x39) ||
        between(code, 0x41, 0x5a) ||
        between(code, 0x61, 0x7a)
      )
        result += string.charAt(i);
      else
        result += "\\" + Integer.toString(code, 16) + " ";

    }
    return result;
  }

  private static String escapeString(String string) {
    String result = "";
    for (int i = 0; i < string.length(); i++) {
      int code = charCodeAt(string, i);

      if (code == 0x0)
        throw new InvalidCharacterError("Invalid character: the input contains U+0000.");

      if (between(code, 0x1, 0x1f) || code == 0x7f)
        result += "\\" + Integer.toString(code, 16) + " ";
      else if (code == 0x22 || code == 0x5c)
        result += "\\" + string.charAt(i);
      else
        result += string.charAt(i);

    }
    return result;
  }

  // java: helpers with no counterpart upstream.

  // java: String.prototype.charCodeAt, -1 where it returns NaN.
  private static int charCodeAt(String string, int index) {
    return index < string.length() ? string.charAt(index) : -1;
  }
}
