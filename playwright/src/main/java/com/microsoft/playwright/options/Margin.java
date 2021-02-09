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

package com.microsoft.playwright.options;

public class Margin {
  /**
   * Top margin, accepts values labeled with units. Defaults to {@code 0}.
   */
  public String top;
  /**
   * Right margin, accepts values labeled with units. Defaults to {@code 0}.
   */
  public String right;
  /**
   * Bottom margin, accepts values labeled with units. Defaults to {@code 0}.
   */
  public String bottom;
  /**
   * Left margin, accepts values labeled with units. Defaults to {@code 0}.
   */
  public String left;

  public Margin(String top, String right, String bottom, String left) {
    this.top = top;
    this.right = right;
    this.bottom = bottom;
    this.left = left;
  }
  public Margin() {
  }
  public Margin withTop(String top) {
    this.top = top;
    return this;
  }
  public Margin withRight(String right) {
    this.right = right;
    return this;
  }
  public Margin withBottom(String bottom) {
    this.bottom = bottom;
    return this;
  }
  public Margin withLeft(String left) {
    this.left = left;
    return this;
  }
}