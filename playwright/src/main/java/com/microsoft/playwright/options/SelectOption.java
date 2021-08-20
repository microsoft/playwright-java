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

public class SelectOption {
  /**
   * Matches by {@code option.value}. Optional.
   */
  public String value;
  /**
   * Matches by {@code option.label}. Optional.
   */
  public String label;
  /**
   * Matches by the index. Optional.
   */
  public Integer index;

  /**
   * Matches by {@code option.value}. Optional.
   */
  public SelectOption setValue(String value) {
    this.value = value;
    return this;
  }
  /**
   * Matches by {@code option.label}. Optional.
   */
  public SelectOption setLabel(String label) {
    this.label = label;
    return this;
  }
  /**
   * Matches by the index. Optional.
   */
  public SelectOption setIndex(int index) {
    this.index = index;
    return this;
  }
}