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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;

import static com.microsoft.playwright.impl.Utils.globToRegex;
import static com.microsoft.playwright.impl.Utils.toJsRegexFlags;

class UrlMatcher {
  private final URL baseURL;
  public final String glob;
  public final Pattern pattern;
  public final Predicate<String> predicate;

  static UrlMatcher forOneOf(URL baseUrl, Object object) {
    if (object == null) {
      return new UrlMatcher(baseUrl, (String) null);
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
    try {
      URL specURL = new URL(spec);
      // We want to follow HTTP spec, so we enforce a slash if there is no path.
      if (specURL.getPath().isEmpty()) {
        spec = specURL.toString() + "/";
      }
    } catch (MalformedURLException e) {
      // Ignore - we end up here if spec is e.g. a relative path.
    }

    if (baseUrl == null) {
      return spec;
    }
    try {
      return new URL(baseUrl, spec).toString();
    } catch (MalformedURLException e) {
      return spec;
    }
  }

  static private String normaliseUrl(String url) {
    URI parsedUrl;
    try {
      parsedUrl = new URI(url);
    } catch (URISyntaxException e) {
      return url;
    }
    // Align with the Node.js URL parser which automatically adds a slash to the path if it is empty.
    if (parsedUrl.getScheme() != null && (
      parsedUrl.getScheme().equals("http") || parsedUrl.getScheme().equals("https") ||
      parsedUrl.getScheme().equals("ws") || parsedUrl.getScheme().equals("wss")
    ) && parsedUrl.getPath().isEmpty()) {
      try {
        return new URI(parsedUrl.getScheme(), parsedUrl.getAuthority(), "/", parsedUrl.getQuery(), parsedUrl.getFragment()).toString();
      } catch (URISyntaxException e) {
        return url;
      }
    }
    return url;
  }

  UrlMatcher(URL baseURL, String glob) {
    this(baseURL, null, null, glob);
  }

  UrlMatcher(Pattern pattern) {
    this(null, pattern, null, null);
  }

  UrlMatcher(Predicate<String> predicate) {
    this(null, null, predicate, null);
  }

  private UrlMatcher(URL baseURL, Pattern pattern, Predicate<String> predicate, String glob) {
    this.baseURL = baseURL;
    this.pattern = pattern;
    this.predicate = predicate;
    this.glob = glob;
  }

  boolean test(String value) {
    return testImpl(baseURL, pattern, predicate, glob, value);
  }

  private static boolean testImpl(URL baseURL, Pattern pattern, Predicate<String> predicate, String glob, String value) {
    if (pattern != null) {
      return pattern.matcher(value).find();
    }
    if (predicate != null) {
      return predicate.test(value);
    }
    if (glob != null) {
      if (!glob.startsWith("*")) {
        glob = normaliseUrl(glob);
        // Allow http(s) baseURL to match ws(s) urls.
        if (baseURL != null && Pattern.compile("^https?://").matcher(baseURL.getProtocol()).find() && Pattern.compile("^wss?://").matcher(value).find()) {
          try {
            baseURL = new URL(baseURL.toString().replaceFirst("^http", "ws"));
          } catch (MalformedURLException e) {
            // Handle exception
          }
        }
        glob = resolveUrl(baseURL, glob);
      }
      return Pattern.compile(globToRegex(glob)).matcher(value).find();
    }
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UrlMatcher that = (UrlMatcher) o;
    List<Boolean> matches = new ArrayList<>();
    if (baseURL != null && that.baseURL != null) {
      matches.add(baseURL.equals(that.baseURL));
    }
    if (pattern != null && that.pattern != null) {
      matches.add(pattern.pattern().equals(that.pattern.pattern()) && pattern.flags() == that.pattern.flags());
    }
    if (predicate != null && that.predicate != null) {
      matches.add(predicate.equals(that.predicate));
    }
    if (glob != null && that.glob != null) {
      matches.add(glob.equals(that.glob));
    }
    return matches.stream().allMatch(m -> m);
  }

  @Override
  public int hashCode() {
    if (pattern != null) {
      return pattern.hashCode();
    }
    if (predicate != null) {
      return predicate.hashCode();
    }
    return glob.hashCode();
  }

  @Override
  public String toString() {
    if (pattern != null)
      return String.format("<regex pattern=\"%s\" flags=\"%s\">", pattern.pattern(), toJsRegexFlags(pattern));
    if (predicate != null)
      return "<predicate>";
    return String.format("<glob pattern=\"%s\">", glob);
  }
}
