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

// Port of isomorphic/urlMatch.ts.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.impl.protocol.URLPattern;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.Serialization.gson;
import static com.microsoft.playwright.impl.Utils.fromJsRegexFlags;
import static com.microsoft.playwright.impl.Utils.toJsRegexFlags;
import static com.microsoft.playwright.impl.isomorphic.StringUtils.isString;

// java: an URLMatch value; a glob also holds its resolved regex, so test() does not resolve it again. The reachable
// path has the driver resolve globs (LocalUtils.globToRegex): forGlob(), test() and equals() stand for
// resolveGlobToRegexPattern(), urlMatches() and urlMatchesEqual(), whose ports have no caller.
// java: java.net.URI stands for the WHATWG URL, see newURL().
// java: stays in impl, not in impl.isomorphic: globs are converted by the driver through LocalUtils.
class UrlMatcher {
  public final String glob;
  public final Pattern pattern;
  public final Predicate<String> predicate;

  // java: the URLMatch union of a Java Object, a null is undefined.
  static UrlMatcher forOneOf(URL baseUrl, Object object, LocalUtils localUtils, boolean isWebSocketUrl) {
    if (object == null) {
      return new UrlMatcher(null, null, null);
    }
    if (object instanceof String) {
      return UrlMatcher.forGlob(baseUrl, (String) object, localUtils, isWebSocketUrl);
    }
    if (object instanceof Pattern) {
      return new UrlMatcher((Pattern) object);
    }
    if (object instanceof Predicate) {
      return new UrlMatcher((Predicate<String>) object);
    }
    throw new PlaywrightException("Url must be String, Pattern or Predicate<String>, found: " + object.getClass().getTypeName());
  }

  UrlMatcher(Pattern pattern) {
    this(null, pattern, null);
  }

  UrlMatcher(Predicate<String> predicate) {
    this(null, null, predicate);
  }

  private UrlMatcher(String glob, Pattern pattern, Predicate<String> predicate) {
    this.glob = glob;
    this.pattern = pattern;
    this.predicate = predicate;
  }

  static boolean isHttpUrl(String url) {
    return isHttpUrl(url, null);
  }

  // java: a null base is undefined.
  static boolean isHttpUrl(String url, String base) {
    try {
      return Arrays.asList("http:", "https:").contains(protocol(newURL(url, base)));
    } catch (URISyntaxException | IllegalArgumentException e) {
      return false;
    }
  }

  // https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Regular_expressions#escaping
  private static final Set<Character> escapedChars = new LinkedHashSet<>(Arrays.asList('$', '^', '+', '.', '*', '(', ')', '|', '\\', '?', '{', '}', '[', ']'));

  static String globToRegexPattern(String glob) {
    List<String> tokens = new ArrayList<>(Collections.singletonList("^"));
    boolean inGroup = false;
    for (int i = 0; i < glob.length(); ++i) {
      char c = glob.charAt(i);
      if (c == '\\' && i + 1 < glob.length()) {
        char ch = glob.charAt(++i);
        tokens.add(escapedChars.contains(ch) ? "\\" + ch : String.valueOf(ch));
        continue;
      }
      if (c == '*') {
        // java: a character out of the string is undefined, here null.
        Character charBefore = i > 0 ? glob.charAt(i - 1) : null;
        int starCount = 1;
        while (i + 1 < glob.length() && glob.charAt(i + 1) == '*') {
          starCount++;
          i++;
        }
        if (starCount > 1) {
          Character charAfter = i + 1 < glob.length() ? glob.charAt(i + 1) : null;
          // Match either /..something../ or /.
          if (charAfter != null && charAfter == '/') {
            if (charBefore != null && charBefore == '/')
              tokens.add("((.+/)|)");
            else
              tokens.add("(.*/)");
            ++i;
          } else {
            tokens.add("(.*)");
          }
        } else {
          tokens.add("([^/]*)");
        }
        continue;
      }

      switch (c) {
        case '{':
          if (inGroup)
            throw new PlaywrightException("Invalid glob pattern " + gson().toJson(glob) + ": nested '{' is not supported");
          inGroup = true;
          tokens.add("(");
          break;
        case '}':
          if (!inGroup)
            throw new PlaywrightException("Invalid glob pattern " + gson().toJson(glob) + ": unmatched '}'");
          inGroup = false;
          tokens.add(")");
          break;
        case ',':
          if (inGroup) {
            tokens.add("|");
            break;
          }
          tokens.add("\\" + c);
          break;
        default:
          tokens.add(escapedChars.contains(c) ? "\\" + c : String.valueOf(c));
      }
    }
    if (inGroup)
      throw new PlaywrightException("Invalid glob pattern " + gson().toJson(glob) + ": unmatched '{'");
    tokens.add("$");
    return String.join("", tokens);
  }

  private static boolean isRegExp(Object obj) {
    return obj instanceof Pattern;
  }

  // java: the JVM has no URLPattern, like a runtime without globalThis.URLPattern.
  static boolean isURLPattern(Object v) {
    return false;
  }

  // java: the protocol URLPattern struct stands for the URLPattern type, it has no test().
  static URLPattern serializeURLPattern(URLPattern v) {
    URLPattern result = new URLPattern();
    result.hash = v.hash;
    result.hostname = v.hostname;
    result.password = v.password;
    result.pathname = v.pathname;
    result.port = v.port;
    result.protocol = v.protocol;
    result.search = v.search;
    result.username = v.username;
    return result;
  }

  // java: the SerializedURLMatch type.
  static class SerializedURLMatch {
    String glob;
    String regexSource;
    String regexFlags;
    URLPattern urlPattern;
  }

  // java: an URLMatch is a String, Pattern or Predicate<String>; a null result is undefined.
  static SerializedURLMatch serializeURLMatch(Object match) {
    SerializedURLMatch result = new SerializedURLMatch();
    if (isString(match)) {
      result.glob = (String) match;
      return result;
    }
    if (isRegExp(match)) {
      result.regexSource = ((Pattern) match).pattern();
      result.regexFlags = toJsRegexFlags((Pattern) match);
      return result;
    }
    if (isURLPattern(match)) {
      result.urlPattern = serializeURLPattern((URLPattern) match);
      return result;
    }
    // Functions cannot be serialized
    return null;
  }

  // java: the result is an URLMatch Object.
  private static Object deserializeURLPattern(URLPattern v) {
    // Client is on Node 24+ and can use URLPattern, Server is not. Let's match all URLs on the server, they'll be filtered again on the client.
    // java: the JVM has no URLPattern, so typeof globalThis.URLPattern !== 'function' always holds and there is no
    // new globalThis.URLPattern({ ... }, { ignoreCase: true }).
    Predicate<String> matchAll = url -> true;
    return matchAll;
  }

  static Object deserializeURLMatch(SerializedURLMatch match) {
    if (match.regexSource != null && !match.regexSource.isEmpty())
      return Pattern.compile(match.regexSource, fromJsRegexFlags(match.regexFlags == null ? "" : match.regexFlags));
    if (match.urlPattern != null)
      return deserializeURLPattern(match.urlPattern);
    return match.glob;
  }

  // java: the Java flags stand for the JS flags; === compares strings by value and functions by identity, as
  // Objects.equals() does for a String and a Predicate.
  static boolean urlMatchesEqual(Object match1, Object match2) {
    if (isRegExp(match1) && isRegExp(match2))
      return ((Pattern) match1).pattern().equals(((Pattern) match2).pattern()) && ((Pattern) match1).flags() == ((Pattern) match2).flags();
    return Objects.equals(match1, match2);
  }

  // java: urlMatchesEqual(match1, match2) of two UrlMatcher values, the reachable path.
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UrlMatcher that = (UrlMatcher) o;
    if (isRegExp() && that.isRegExp()) {
      return pattern.pattern().equals(that.pattern.pattern()) && pattern.flags() == that.pattern.flags();
    }
    // java: match1 === match2 compares strings by value and functions by identity.
    if (glob != null || that.glob != null) {
      return Objects.equals(glob, that.glob);
    }
    if (predicate != null || that.predicate != null) {
      return predicate == that.predicate;
    }
    return pattern == null && that.pattern == null;
  }

  @Override
  public int hashCode() {
    if (glob != null) {
      return glob.hashCode();
    }
    if (pattern != null) {
      return Objects.hash(pattern.pattern(), pattern.flags());
    }
    if (predicate != null) {
      return System.identityHashCode(predicate);
    }
    return 0;
  }

  static boolean urlMatches(String baseURL, String urlString, Object match) {
    return urlMatches(baseURL, urlString, match, false);
  }

  // java: an URLMatch is a String, Pattern or Predicate<String>, a null baseURL or match is undefined.
  @SuppressWarnings("unchecked")
  static boolean urlMatches(String baseURL, String urlString, Object match, boolean webSocketUrl) {
    if (match == null || "".equals(match))
      return true;
    if (isString(match))
      match = Pattern.compile(resolveGlobToRegexPattern(baseURL, (String) match, webSocketUrl));
    if (isRegExp(match)) {
      // java: no match.lastIndex = 0, a Matcher keeps no state between tests.
      return ((Pattern) match).matcher(urlString).find();
    }
    URI url = parseURL(urlString);
    if (url == null)
      return false;
    // java: no isURLPattern(match) branch, the JVM has no URLPattern whose test() could be called.
    if (!(match instanceof Predicate))
      throw new PlaywrightException("url parameter should be string, RegExp, URLPattern or function");
    // java: a Predicate<String> stands for the function, it takes url.href.
    return ((Predicate<String>) match).test(url.toString());
  }

  // java: urlMatches(baseURL, urlString, match), the reachable path; the glob was resolved against the base URL by
  // forGlob(), and a predicate takes the URL string, not a parsed URL.
  boolean test(String urlString) {
    if ((glob == null && pattern == null && predicate == null) || "".equals(glob)) {
      return true;
    }
    if (pattern != null) {
      return pattern.matcher(urlString).find();
    }
    return predicate.test(urlString);
  }

  static String resolveGlobToRegexPattern(String baseURL, String glob) {
    return resolveGlobToRegexPattern(baseURL, glob, false);
  }

  // java: a null baseURL is undefined.
  static String resolveGlobToRegexPattern(String baseURL, String glob, boolean webSocketUrl) {
    if (webSocketUrl)
      baseURL = toWebSocketBaseUrl(baseURL);
    glob = resolveGlobBase(baseURL, glob);
    return globToRegexPattern(glob);
  }

  // java: resolveGlobToRegexPattern(baseURL, glob, webSocketUrl) and new RegExp(...), the reachable path: the
  // driver resolves the glob.
  static UrlMatcher forGlob(URL baseURL, String glob, LocalUtils localUtils, boolean isWebSocketUrl) {
    Pattern pattern = localUtils.globToRegex(glob, baseURL != null ? baseURL.toString() : null, isWebSocketUrl);
    return new UrlMatcher(glob, pattern, null);
  }

  private static String toWebSocketBaseUrl(String baseURL) {
    // Allow http(s) baseURL to match ws(s) urls. Schemes are case-insensitive,
    // same as elsewhere in this file, so 'HTTP://...' should be rewritten too.
    if (baseURL != null && Pattern.compile("^https?://", Pattern.CASE_INSENSITIVE).matcher(baseURL).find()) {
      Matcher scheme = Pattern.compile("^https?", Pattern.CASE_INSENSITIVE).matcher(baseURL);
      scheme.find();
      baseURL = (scheme.group().toLowerCase().equals("https") ? "wss" : "ws") + baseURL.substring(scheme.end());
    }
    return baseURL;
  }

  private static String resolveGlobBase(String baseURL, String match) {
    if (!match.startsWith("*")) {
      Map<String, String> tokenMap = new LinkedHashMap<>();
      BiFunction<String, String, String> mapToken = (original, replacement) -> {
        if (original.length() == 0)
          return "";
        tokenMap.put(replacement, original);
        return replacement;
      };
      // Escaped `\\?` behaves the same as `?` in our glob patterns.
      match = match.replaceAll("\\\\\\\\\\?", "?");
      // Special case about: URLs as they are not relative to baseURL
      if (match.startsWith("about:") || match.startsWith("data:")
        || match.startsWith("chrome:") || match.startsWith("edge:")
        || match.startsWith("file:"))
        return match;
      // Glob symbols may be escaped in the URL and some of them such as ? affect resolution,
      // so we replace them with safe components first.
      BiFunction<String, Integer, String> mapPathToken = (token, index) -> {
        if (token.equals(".") || token.equals("..") || token.equals(""))
          return token;
        // Handle special case of http*://, note that the new schema has to be
        // a web schema so that slashes are properly inserted after domain.
        if (index == 0 && token.endsWith(":")) {
          // Replace any pattern with http:
          if (token.indexOf('*') != -1 || token.indexOf('{') != -1)
            return mapToken.apply(token, "http:");
          // Preserve explicit schema as is as it may affect trailing slashes after domain.
          return token;
        }
        // Components without glob metacharacters are literal, so let them round-trip
        // through new URL() to preserve normalization (default ports such as :80/:443,
        // percent-encoding, IDN hosts). Only opaque tokens defeat that normalization.
        if (!Pattern.compile("[*?{}\\\\]").matcher(token).find())
          return token;
        int questionIndex = token.indexOf('?');
        if (questionIndex == -1)
          return mapToken.apply(token, "$_" + index + "_$");
        String newPrefix = mapToken.apply(token.substring(0, questionIndex), "$_" + index + "_$");
        String newSuffix = mapToken.apply(token.substring(questionIndex), "?$_" + index + "_$");
        return newPrefix + newSuffix;
      };
      String[] pathTokens = match.split("/", -1);
      List<String> mappedTokens = new ArrayList<>();
      for (int index = 0; index < pathTokens.length; index++)
        mappedTokens.add(mapPathToken.apply(pathTokens[index], index));
      String relativePath = String.join("/", mappedTokens);
      ResolvedBaseURL result = resolveBaseURL(baseURL, relativePath);
      String resolved = result.resolved;
      for (Map.Entry<String, String> entry : tokenMap.entrySet()) {
        String token = entry.getKey();
        String original = entry.getValue();
        boolean normalize = result.caseInsensitivePart != null && result.caseInsensitivePart.contains(token);
        String replacement = normalize ? original.toLowerCase() : original;
        // '$$', '$&', '$`' and "$'" are special in String.prototype.replace with a string argument.
        // Instead, use the function argument form that treats the string argument literally.
        // java: indexOf() and substring() replace the first occurrence literally.
        int tokenIndex = resolved.indexOf(token);
        if (tokenIndex != -1)
          resolved = resolved.substring(0, tokenIndex) + replacement + resolved.substring(tokenIndex + token.length());
      }
      match = resolved;
    }
    return match;
  }

  // java: a URI that newURL() cannot build is the TypeError of new URL().
  private static URI parseURL(String url) {
    try {
      return newURL(url, null);
    } catch (URISyntaxException | IllegalArgumentException e) {
      return null;
    }
  }

  // java: not resolveBaseURL(baseURL, givenURL).resolved: PageAssertionsImpl depends on URI.resolve() and on
  // givenURL coming back unchanged without a base, the behaviour of the Java client.
  static String constructURLBasedOnBaseURL(String baseURL, String givenURL) {
    if (baseURL == null) {
      return givenURL;
    }
    try {
      // Join using URI instead of URL since URL doesn't handle ws(s) protocols.
      return new URI(baseURL).resolve(givenURL).toString();
    } catch (URISyntaxException | IllegalArgumentException e) {
      return givenURL;
    }
  }

  // java: the { resolved, caseInsensitivePart } result, a null caseInsensitivePart is undefined.
  private static class ResolvedBaseURL {
    final String resolved;
    final String caseInsensitivePart;

    ResolvedBaseURL(String resolved, String caseInsensitivePart) {
      this.resolved = resolved;
      this.caseInsensitivePart = caseInsensitivePart;
    }
  }

  private static ResolvedBaseURL resolveBaseURL(String baseURL, String givenURL) {
    try {
      URI url = newURL(givenURL, baseURL);
      String resolved = url.toString();
      // Schema and domain are case-insensitive.
      String caseInsensitivePrefix = origin(url);
      return new ResolvedBaseURL(resolved, caseInsensitivePrefix);
    } catch (URISyntaxException | IllegalArgumentException e) {
      return new ResolvedBaseURL(givenURL, null);
    }
  }

  // java: members with no counterpart upstream.

  private boolean isRegExp() {
    return glob == null && pattern != null;
  }

  @Override
  public String toString() {
    if (glob != null)
      return String.format("<glob pattern=\"%s\">", glob);
    if (pattern != null)
      return String.format("<regex pattern=\"%s\" flags=\"%s\">", pattern.pattern(), toJsRegexFlags(pattern));
    if (this.predicate != null)
      return "<predicate>";
    return "<true>";
  }

  // java: new URL(url, base) of the WHATWG URL parser on top of java.net.URI: a null base is undefined, a relative URL
  // without a base fails, the scheme and the host are lower-cased, a default port is dropped and the empty path of a
  // special scheme becomes '/'. Unlike the WHATWG parser it does not percent-encode, so a URL with a space or a
  // character such as '|' fails to parse.
  private static URI newURL(String url, String base) throws URISyntaxException {
    URI uri;
    if (base == null) {
      uri = new URI(url);
    } else {
      URI baseURI = new URI(base);
      if (!baseURI.isAbsolute())
        throw new URISyntaxException(base, "Invalid base URL");
      uri = normalizeURL(baseURI).resolve(new URI(url));
    }
    if (!uri.isAbsolute())
      throw new URISyntaxException(url, "Invalid URL");
    return normalizeURL(uri.normalize());
  }

  private static URI normalizeURL(URI uri) throws URISyntaxException {
    String scheme = uri.getScheme().toLowerCase();
    if (uri.isOpaque())
      return new URI(scheme + ":" + uri.getRawSchemeSpecificPart() + (uri.getRawFragment() != null ? "#" + uri.getRawFragment() : ""));
    StringBuilder result = new StringBuilder(scheme).append(':');
    String authority = uri.getRawAuthority();
    if (authority != null) {
      result.append("//");
      if (uri.getHost() != null) {
        if (uri.getRawUserInfo() != null)
          result.append(uri.getRawUserInfo()).append('@');
        result.append(uri.getHost().toLowerCase());
        if (uri.getPort() != -1 && uri.getPort() != defaultPort(scheme))
          result.append(':').append(uri.getPort());
      } else {
        result.append(authority);
      }
    }
    String path = uri.getRawPath();
    if ((path == null || path.isEmpty()) && authority != null && defaultPort(scheme) != -1)
      path = "/";
    if (path != null)
      result.append(path);
    if (uri.getRawQuery() != null)
      result.append('?').append(uri.getRawQuery());
    if (uri.getRawFragment() != null)
      result.append('#').append(uri.getRawFragment());
    return new URI(result.toString());
  }

  // java: the default port of a special scheme with a network host, -1 for any other scheme.
  private static int defaultPort(String scheme) {
    switch (scheme) {
      case "http":
      case "ws":
        return 80;
      case "https":
      case "wss":
        return 443;
      case "ftp":
        return 21;
      default:
        return -1;
    }
  }

  // java: URL.protocol.
  private static String protocol(URI url) {
    return url.getScheme() + ":";
  }

  // java: URL.origin; a scheme without a default port has the opaque origin "null". A host that java.net.URI does
  // not accept, such as a $_2_$ token, is taken from the authority without its user info.
  private static String origin(URI url) {
    if (defaultPort(url.getScheme()) == -1 || url.getRawAuthority() == null)
      return "null";
    String host;
    if (url.getHost() != null)
      host = url.getHost() + (url.getPort() != -1 ? ":" + url.getPort() : "");
    else
      host = url.getRawAuthority().substring(url.getRawAuthority().lastIndexOf('@') + 1);
    return url.getScheme() + "://" + host;
  }
}
