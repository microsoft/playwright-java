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

package com.microsoft.playwright;

import java.util.List;

public interface AccessibilityNode {
  String role();
  String name();
  String valueString();
  Double valueNumber();
  String description();
  String keyshortcuts();
  String roledescription();
  String valuetext();
  Boolean disabled();
  Boolean expanded();
  Boolean focused();
  Boolean modal();
  Boolean multiline();
  Boolean multiselectable();
  Boolean readonly();
  Boolean required();
  Boolean selected();
  enum CheckedState { CHECKED, UNCHECKED, MIXED }
  CheckedState checked();
  enum PressedState { PRESSED, RELEASED, MIXED }
  PressedState pressed();
  Integer level();
  Double valuemin();
  Double valuemax();
  String autocomplete();
  String haspopup();
  String invalid();
  String orientation();
  List<AccessibilityNode> children();
}
