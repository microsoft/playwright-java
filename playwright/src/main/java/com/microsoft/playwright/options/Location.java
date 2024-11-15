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

public class Location {
  public String file;
  public Integer line;
  public Integer column;

  public Location(String file) {
    this.file = file;
  }
  public Location setLine(int line) {
    this.line = line;
    return this;
  }
  public Location setColumn(int column) {
    this.column = column;
    return this;
  }
}