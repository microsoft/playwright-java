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

  /**
   * Top margin, accepts values labeled with units. Defaults to {@code 0}.
   */
  public Margin setTop(String top) {
    this.top = top;
    return this;
  }
  /**
   * Right margin, accepts values labeled with units. Defaults to {@code 0}.
   */
  public Margin setRight(String right) {
    this.right = right;
    return this;
  }
  /**
   * Bottom margin, accepts values labeled with units. Defaults to {@code 0}.
   */
  public Margin setBottom(String bottom) {
    this.bottom = bottom;
    return this;
  }
  /**
   * Left margin, accepts values labeled with units. Defaults to {@code 0}.
   */
  public Margin setLeft(String left) {
    this.left = left;
    return this;
  }
}