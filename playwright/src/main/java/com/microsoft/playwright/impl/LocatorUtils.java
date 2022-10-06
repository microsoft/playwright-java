package com.microsoft.playwright.impl;

import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.Serialization.gson;
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
      throw new IllegalArgumentException("text parameter must be Pattern or String: " + text);
    }
    String text = (String) param;
    if (exact) {
      return '"' + text.replace("\"", "\\\"") + '"';
    }

    if (text.contains("\"") || text.contains(">>") || text.startsWith("/")) {
      return "/" + escapeForRegex(text).replace(/\s+/, '\\s+')}/` + (caseSensitive ? '' : 'i');
    }
    return text;

//    if (typeof text !== 'string')
//    return String(text);
//    if (exact)
//      return '"' + text.replace(/["]/g, '\\"') + '"';
//    if (text.includes('"') || text.includes('>>') || text[0] === '/')
//      return `/.*${escapeForRegex(text).replace(/\s+/, '\\s+')}.*/` + (caseSensitive ? '' : 'i');
//    return text;
  }
  static String escapeForRegex(String text) {
    return text;
  }
}
