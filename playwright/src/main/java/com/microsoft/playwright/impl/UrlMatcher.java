/**
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

  private static Predicate<String> toPridcate(Pattern pattern) {
    return s -> pattern.matcher(s).find();
  }

  static UrlMatcher any() {
    return new UrlMatcher(null, null);
  }

  UrlMatcher(String url) {
    this(url, toPridcate(Pattern.compile(globToRegex(url))));
  }

  UrlMatcher(Pattern pattern) {
    this(pattern, toPridcate(pattern));
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
