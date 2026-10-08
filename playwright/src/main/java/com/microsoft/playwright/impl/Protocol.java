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

import java.util.List;
import com.microsoft.playwright.options.Clip;
import com.microsoft.playwright.options.ScreenshotCaret;
import com.microsoft.playwright.options.ScreenshotAnimations;
import com.microsoft.playwright.options.ScreenshotScale;

class Channel {
  String guid;
}

class SerializedValue{
  Number n;
  Boolean b;
  String s;
  // Possible values: { 'null, 'undefined, 'NaN, 'Infinity, '-Infinity, '-0 }
  String v;
  String d;
  String u;
  String bi;
  public static class E {
    String m;
    String n;
    String s;
  }
  E e;
  public static class R {
    String p;
    String f;
  }
  R r;
  SerializedValue[] a;
  public static class O {
    String k;
    SerializedValue v;
  }
  O[] o;
  Number h;
  Integer id;
  Integer ref;
  // JS representation of Map: [[key1, value1], [key2, value2], ...].
  SerializedValue m;
  // JS representation of Set: [item1, item2, ...].
  SerializedValue se;
}

class SerializedArgument{
  SerializedValue value;
  Channel[] handles;
}

class SerializedError{
  public static class Error {
    String message;
    String name;
    String stack;

    @Override
    public String toString() {
      return "Error {\n" +
        "  message='" + message + '\n' +
        "  name='" + name + '\n' +
        "  stack='" + stack + '\n' +
        '}';
    }
  }
  Error error;
  SerializedValue value;

  @Override
  public String toString() {
    if (error != null) {
      return error.toString();
    }
    return "SerializedError{" +
      "value=" + value +
      '}';
  }
}

class ExpectedTextValue {
  String string;
  String regexSource;
  String regexFlags;
  Boolean ignoreCase;
  Boolean matchSubstring;
  Boolean normalizeWhiteSpace;
}

class FrameExpectOptions {
  Object expressionArg;
  List<ExpectedTextValue> expectedText;
  String selector;
  Double expectedNumber;
  SerializedArgument expectedValue;
  Boolean useInnerText;
  boolean isNot;
  Double timeout;
}

class FrameExpectResult {
  static class Received {
    SerializedValue value;
    String ariaSnapshot;
  }
  boolean matches;
  Received received;
  String errorMessage;
  List<String> log;
}

class FrameExpectErrorDetails {
  FrameExpectResult.Received received;
  Boolean timedOut;
  String customErrorMessage;
}

class PageExpectScreenshotOptions {
  String expected;
  boolean isNot;
  LocatorImpl locator;
  String comparator;
  Integer maxDiffPixels;
  Double maxDiffPixelRatio;
  Double threshold;
  Boolean fullPage;
  Clip clip;
  Boolean omitBackground;
  ScreenshotCaret caret;
  ScreenshotAnimations animations;
  ScreenshotScale scale;
  List<LocatorImpl> mask;
  String maskColor;
  String style;
  Double timeout;
}

class PageExpectScreenshotResult {
  String actual;
}

class PageExpectScreenshotErrorDetails {
  String diff;
  String customErrorMessage;
  String actual;
  String previous;
  Boolean timedOut;
  List<String> log;
}


