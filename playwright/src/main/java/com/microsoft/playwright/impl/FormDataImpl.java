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

import com.microsoft.playwright.options.FilePayload;
import com.microsoft.playwright.options.FormData;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class FormDataImpl implements FormData {
  Map<String, Object> fields = new LinkedHashMap<>();

  @Override
  public FormData set(String name, String value) {
    fields.put(name, value);
    return this;
  }

  @Override
  public FormData set(String name, boolean value) {
    fields.put(name, value);
    return this;
  }

  @Override
  public FormData set(String name, int value) {
    fields.put(name, value);
    return this;
  }

  @Override
  public FormData set(String name, Path value) {
    fields.put(name, value);
    return this;
  }

  @Override
  public FormData set(String name, FilePayload value) {
    fields.put(name, value);
    return this;
  }
}
