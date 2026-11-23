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

// Port of @jest/get-type.
// java: the original is MIT licensed, Copyright (c) Meta Platforms, Inc. and affiliates.
package com.microsoft.playwright.impl;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

class JestGetType {
  // java: a Java value has one of these JavaScript types: null is null, a List or an array is
  // an array, a Boolean is a boolean, a Number is a number, a String is a string, a Pattern is
  // a regexp, a Date is a date. All other values, a Map too, are objects. The other types have no Java values.
  // java: the undefined value is UNDEFINED: Frame.expect returns it for a received value that is undefined.
  static final Object UNDEFINED = new Object() {
    @Override
    public String toString() {
      return "undefined";
    }
  };

  static String getType(Object value) {
    if (value == UNDEFINED) {
      return "undefined";
    } else if (value == null) {
      return "null";
    } else if (isArray(value)) {
      return "array";
    } else if (value instanceof Boolean) {
      return "boolean";
    } else if (value instanceof Number) {
      return "number";
    } else if (value instanceof String) {
      return "string";
    } else {
      if (value instanceof Pattern) {
        return "regexp";
      } else if (value instanceof Date) {
        return "date";
      }
      return "object";
    }
  }

  static boolean isPrimitive(Object value) {
    return value == null || value == UNDEFINED || value instanceof Boolean || value instanceof Number || value instanceof String;
  }

  // java: helper with no counterpart upstream, the port of Array.isArray.
  static boolean isArray(Object value) {
    return value instanceof List || value != null && value.getClass().isArray();
  }
}
