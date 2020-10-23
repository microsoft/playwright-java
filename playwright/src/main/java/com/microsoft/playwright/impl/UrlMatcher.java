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

import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.Utils.globToRegex;

class UrlMatcher {
  private final Object rawSource;
  private final Predicate<String> predicate;

  private static Predicate<String> toPredicate(Pattern pattern) {
    return s -> pattern.matcher(s).find();
  }

  static UrlMatcher any() {
    return new UrlMatcher(null, null);
  }

  static UrlMatcher forOneOf(String glob, Pattern pattern, Predicate<String> predicate) {
    UrlMatcher result = UrlMatcher.any();
    int conditionCount = 0;
    if (glob != null) {
      conditionCount += 1;
      result = new UrlMatcher(glob);
    }
    if (pattern != null) {
      conditionCount += 1;
      result = new UrlMatcher(pattern);
    }
    if (predicate != null) {
      conditionCount += 1;
      result = new UrlMatcher(predicate);
    }
    if (conditionCount > 1) {
      throw new IllegalArgumentException("Only one of glob, pattern and predicate can be specified");
    }
    return result;
  }

  UrlMatcher(String url) {
    this(url, toPredicate(Pattern.compile(globToRegex(url))).or(s -> url == null || url.equals(s)));
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
    return Objects.equals(rawSource, that.rawSource);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rawSource);
  }
}
