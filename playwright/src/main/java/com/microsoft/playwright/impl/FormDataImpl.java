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
import java.util.*;
import java.util.stream.Collectors;

public class FormDataImpl implements FormData {
  static class Field implements Map.Entry<String, Object> {
    final String name;
    final Object value;

    private Field(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String getKey() {
      return name;
    }

    @Override
    public Object getValue() {
      return value;
    }

    @Override
    public Object setValue(Object value) {
      throw new UnsupportedOperationException();
    }
  }
  List<Field> fields = new ArrayList();

  @Override
  public FormData append(String name, String value) {
    return appendImpl(name, value);
  }

  @Override
  public FormData append(String name, boolean value) {
    return appendImpl(name, value);
  }

  @Override
  public FormData append(String name, int value) {
    return appendImpl(name, value);
  }

  @Override
  public FormData append(String name, Path value) {
    return appendImpl(name, value);
  }

  @Override
  public FormData append(String name, FilePayload value) {
    return appendImpl(name, value);
  }

  @Override
  public FormData set(String name, String value) {
    return setImpl(name, value);
  }

  @Override
  public FormData set(String name, boolean value) {
    return setImpl(name, value);
  }

  @Override
  public FormData set(String name, int value) {
    return setImpl(name, value);
  }

  @Override
  public FormData set(String name, Path value) {
    return setImpl(name, value);
  }

  @Override
  public FormData set(String name, FilePayload value) {
    return setImpl(name, value);
  }

  private FormData setImpl(String name, Object value) {
    fields = fields.stream().filter(f -> !name.equals(f.name)).collect(Collectors.toList());
    return appendImpl(name, value);
  }

  private FormData appendImpl(String name, Object value) {
    fields.add(new Field(name, value));
    return this;
  }
}
