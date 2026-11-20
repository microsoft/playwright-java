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

import com.microsoft.playwright.impl.protocol.ExpectedTextValue;
import com.microsoft.playwright.impl.protocol.SerializedArgument;
import com.microsoft.playwright.options.PseudoElement;

import java.util.List;

// java: the options of Frame._expect, FrameExpectParams of types.ts.
class FrameExpectOptions {
  Object expressionArg;
  List<ExpectedTextValue> expectedText;
  String selector;
  Double expectedNumber;
  SerializedArgument expectedValue;
  Boolean useInnerText;
  PseudoElement pseudo;
  boolean isNot;
  Double timeout;
}
