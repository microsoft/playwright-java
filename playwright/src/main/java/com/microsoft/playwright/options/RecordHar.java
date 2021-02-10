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

import java.nio.file.Path;
public class RecordHar {
  /**
   * Optional setting to control whether to omit request content from the HAR. Defaults to {@code false}.
   */
  public Boolean omitContent;
  /**
   * Path on the filesystem to write the HAR file to.
   */
  public Path path;

  public RecordHar(Boolean omitContent, Path path) {
    this.omitContent = omitContent;
    this.path = path;
  }
  public RecordHar() {
  }
  public RecordHar withOmitContent(boolean omitContent) {
    this.omitContent = omitContent;
    return this;
  }
  public RecordHar withPath(Path path) {
    this.path = path;
    return this;
  }
}