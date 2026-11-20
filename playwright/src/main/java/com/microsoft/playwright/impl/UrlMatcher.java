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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.Utils.toJsRegexFlags;

// java: globToRegexPattern, resolveGlobToRegexPattern, resolveGlobBase and the other glob functions are not ported:
// the driver resolves globs (LocalUtils.globToRegex), as they rely on the WHATWG URL parser.
// java: an URLMatch value; a glob also holds its resolved regex, so test() does not resolve it again.
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

  // java: urlMatchesEqual(match1, match2).
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

  // java: urlMatches(baseURL, urlString, match); the glob was resolved against the base URL by forGlob(), and a
  // predicate takes the URL string, not a parsed URL.
  boolean test(String urlString) {
    if ((glob == null && pattern == null && predicate == null) || "".equals(glob)) {
      return true;
    }
    if (pattern != null) {
      return pattern.matcher(urlString).find();
    }
    return predicate.test(urlString);
  }

  // java: resolveGlobToRegexPattern(baseURL, glob, webSocketUrl) and new RegExp(...); the driver resolves the glob,
  // as resolveGlobBase() relies on the WHATWG URL parser.
  static UrlMatcher forGlob(URL baseURL, String glob, LocalUtils localUtils, boolean isWebSocketUrl) {
    Pattern pattern = localUtils.globToRegex(glob, baseURL != null ? baseURL.toString() : null, isWebSocketUrl);
    return new UrlMatcher(glob, pattern, null);
  }

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
}
