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

// Port of fileChooser.ts.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.FileChooser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.FilePayload;

import java.nio.file.Path;

import static com.microsoft.playwright.impl.Utils.convertType;

class FileChooserImpl implements FileChooser {
  private final PageImpl page;
  private final ElementHandleImpl elementHandle;
  private final boolean isMultiple;

  FileChooserImpl(PageImpl page, ElementHandleImpl elementHandle, boolean isMultiple) {
    this.page = page;
    this.elementHandle = elementHandle;
    this.isMultiple = isMultiple;
  }

  @Override
  public ElementHandle element() {
    return elementHandle;
  }

  @Override
  public boolean isMultiple() {
    return isMultiple;
  }

  @Override
  public Page page() {
    return page;
  }

  // java: overloads for the public API; each delegates to ElementHandle.setInputFiles like upstream setFiles.
  @Override
  public void setFiles(Path files, SetFilesOptions options) {
    setFiles(new Path[]{files}, options);
  }

  @Override
  public void setFiles(Path[] files, SetFilesOptions options) {
    elementHandle.setInputFiles(files, convertType(options, ElementHandle.SetInputFilesOptions.class));
  }

  @Override
  public void setFiles(FilePayload files, SetFilesOptions options) {
    setFiles(new FilePayload[]{files}, options);
  }

  @Override
  public void setFiles(FilePayload[] files, SetFilesOptions options) {
    elementHandle.setInputFiles(files, convertType(options, ElementHandle.SetInputFilesOptions.class));
  }
}
