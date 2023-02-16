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

// This file is generated by generate_java_rpc.js, do not edit manually.

package com.microsoft.playwright.impl;

import java.util.List;

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
  Double expectedNumber;
  SerializedArgument expectedValue;
  Boolean useInnerText;
  boolean isNot;
  Double timeout;
}

class FrameExpectResult {
  boolean matches;
  SerializedValue received;
  List<String> log;
}


