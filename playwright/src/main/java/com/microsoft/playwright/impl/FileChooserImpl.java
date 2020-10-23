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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.FileChooser;
import com.microsoft.playwright.Page;

import java.io.File;

import static com.microsoft.playwright.impl.Utils.convertViaJson;

class FileChooserImpl implements FileChooser {
  private final PageImpl page;
  private final ElementHandle element;
  private final boolean isMultiple;

  FileChooserImpl(PageImpl page, ElementHandle element, boolean isMultiple) {
    this.page = page;
    this.element = element;
    this.isMultiple = isMultiple;
  }

  @Override
  public ElementHandle element() {
    return element;
  }

  @Override
  public boolean isMultiple() {
    return isMultiple;
  }

  @Override
  public Page page() {
    return page;
  }

  @Override
  public void setFiles(File[] files, SetFilesOptions options) {
    setFiles(Utils.toFilePayloads(files), options);
  }

  @Override
  public void setFiles(FilePayload[] files, SetFilesOptions options) {
    element.setInputFiles(files, convertViaJson(options, ElementHandle.SetInputFilesOptions.class));
  }
}
