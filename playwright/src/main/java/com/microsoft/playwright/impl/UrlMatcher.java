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

package com.microsoft.playwright.impl;

import com.microsoft.playwright.PlaywrightException;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.Utils.toJsRegexFlags;

class UrlMatcher {
  private final LocalUtils localUtils;
  private final String baseURL;
  public final String glob;
  public final Pattern pattern;
  public final Predicate<String> predicate;

  static UrlMatcher forOneOf(LocalUtils localUtils, URL baseUrl, Object object) {
    if (object == null) {
      return new UrlMatcher(localUtils, null, null, null, null);
    }
    if (object instanceof String) {
      return new UrlMatcher(localUtils, baseUrl, (String) object);
    }
    if (object instanceof Pattern) {
      return new UrlMatcher(localUtils, (Pattern) object);
    }
    if (object instanceof Predicate) {
      return new UrlMatcher(localUtils, (Predicate<String>) object);
    }
    throw new PlaywrightException("Url must be String, Pattern or Predicate<String>, found: " + object.getClass().getTypeName());
  }

  static String resolveUrl(URL baseUrl, String spec) {
    return resolveUrl(baseUrl.toString(), spec);
  }

  private static String resolveUrl(String baseUrl, String spec) {
    if (baseUrl == null) {
      return spec;
    }
    try {
      // Join using URI instead of URL since URL doesn't handle ws(s) protocols.
      return new URI(baseUrl).resolve(spec).toString();
    } catch (URISyntaxException e) {
      return spec;
    }
  }

  UrlMatcher(LocalUtils localUtils, URL baseURL, String glob) {
    this(localUtils, baseURL, glob, null, null);
  }

  UrlMatcher(LocalUtils localUtils, Pattern pattern) {
    this(localUtils, null, null, pattern, null);
  }

  UrlMatcher(LocalUtils localUtils, Predicate<String> predicate) {
    this(localUtils, null, null, null, predicate);
  }

  private UrlMatcher(LocalUtils localUtils, URL baseURL, String glob, Pattern pattern, Predicate<String> predicate) {
    this.baseURL = baseURL != null ? baseURL.toString() : null;
    this.glob = glob;
    this.pattern = pattern;
    this.predicate = predicate;
    this.localUtils = localUtils;
  }

  boolean test(String value) {
    return testImpl(localUtils, baseURL, pattern, predicate, glob, value, false);
  }

  boolean testWebsocket(String value) {
    return testImpl(localUtils, baseURL, pattern, predicate, glob, value, true);
  }

  private static boolean testImpl(LocalUtils localUtils, String baseURL, Pattern pattern, Predicate<String> predicate, String glob, String value, boolean isWebSocket) {
    if (pattern != null) {
      return pattern.matcher(value).find();
    }
    if (predicate != null) {
      return predicate.test(value);
    }
    if (glob != null) {
      return localUtils.globToRegex(glob, baseURL, isWebSocket).matcher(value).find();
    }
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UrlMatcher that = (UrlMatcher) o;
    if (pattern != null) {
      return that.pattern != null && pattern.pattern().equals(that.pattern.pattern()) && pattern.flags() == that.pattern.flags();
    }
    if (predicate != null) {
      return predicate.equals(that.predicate);
    }
    if (glob != null) {
      return glob.equals(that.glob);
    }
    return that.pattern == null  && that.predicate == null && that.glob == null;
  }

  @Override
  public int hashCode() {
    if (pattern != null) {
      return pattern.hashCode();
    }
    if (predicate != null) {
      return predicate.hashCode();
    }
    if (glob != null) {
      return glob.hashCode();
    }
    return 0;
  }

  @Override
  public String toString() {
    if (pattern != null)
      return String.format("<regex pattern=\"%s\" flags=\"%s\">", pattern.pattern(), toJsRegexFlags(pattern));
    if (this.predicate != null)
      return "<predicate>";
    return String.format("<glob pattern=\"%s\">", glob);
  }
}
