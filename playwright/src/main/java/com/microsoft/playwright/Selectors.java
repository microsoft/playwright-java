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

import java.nio.file.Path;
import java.util.*;

/**
 * Selectors can be used to install custom selector engines. See [Working with selectors](./selectors.md) for more
 * information.
 */
public interface Selectors {
  class RegisterOptions {
    /**
     * Whether to run this selector engine in isolated JavaScript environment. This environment has access to the same DOM, but
     * not any JavaScript objects from the frame's scripts. Defaults to {@code false}. Note that running as a content script is not
     * guaranteed when this engine is used together with other registered engines.
     */
    public Boolean contentScript;

    public RegisterOptions withContentScript(boolean contentScript) {
      this.contentScript = contentScript;
      return this;
    }
  }
  default void register(String name, String script) { register(name, script, null); }
  void register(String name, String script, RegisterOptions options);
  default void register(String name, Path path) { register(name, path, null); }
  /**
   * An example of registering selector engine that queries elements based on a tag name:
   *
   *
   * @param name Name that is used in selectors as a prefix, e.g. {@code {name: 'foo'}} enables {@code foo=myselectorbody} selectors. May only
   * contain {@code [a-zA-Z0-9_]} characters.
   * @param script Script that evaluates to a selector engine instance.
   */
  void register(String name, Path path, RegisterOptions options);
}

