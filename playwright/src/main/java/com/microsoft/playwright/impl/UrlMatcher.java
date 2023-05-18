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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.Utils.globToRegex;

class UrlMatcher {
  final Object rawSource;
  private final Predicate<String> predicate;

  private static Predicate<String> toPredicate(Pattern pattern) {
    return s -> pattern.matcher(s).find();
  }

  static UrlMatcher any() {
    return new UrlMatcher((Object) null, null);
  }

  static UrlMatcher forOneOf(URL baseUrl, Object object) {
    if (object == null) {
      return UrlMatcher.any();
    }
    if (object instanceof String) {
      return new UrlMatcher(baseUrl, (String) object);
    }
    if (object instanceof Pattern) {
      return new UrlMatcher((Pattern) object);
    }
    if (object instanceof Predicate) {
      return new UrlMatcher((Predicate<String>) object);
    }
    throw new PlaywrightException("Url must be String, Pattern or Predicate<String>, found: " + object.getClass().getTypeName());
  }

  static String resolveUrl(URL baseUrl, String spec) {
    if (baseUrl == null) {
      return spec;
    }
    try {
      return new URL(baseUrl, spec).toString();
    } catch (MalformedURLException e) {
      return spec;
    }
  }

  UrlMatcher(URL base, String url) {
    this(url, toPredicate(Pattern.compile(globToRegex(resolveUrl(base, url)))).or(s -> url == null || url.equals(s)));
  }

  UrlMatcher(Pattern pattern) {
    this(pattern, toPredicate(pattern));
  }
  UrlMatcher(Predicate<String> predicate) {
    this(predicate, predicate);
  }

  private UrlMatcher(Object rawSource, Predicate<String> predicate) {
    this.rawSource = rawSource;
    this.predicate = predicate;
  }

  boolean test(String value) {
    return predicate == null || predicate.test(value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UrlMatcher that = (UrlMatcher) o;
    if (rawSource instanceof Pattern && that.rawSource instanceof Pattern) {
      Pattern a = (Pattern) rawSource;
      Pattern b = (Pattern) that.rawSource;
      return a.pattern().equals(b.pattern()) && a.flags() == b.flags();
    }
    return Objects.equals(rawSource, that.rawSource);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rawSource);
  }

  @Override
  public String toString() {
    if (rawSource == null)
      return "<any>";
    if (rawSource instanceof Predicate)
      return "matching predicate";
    return rawSource.toString();
  }
}
