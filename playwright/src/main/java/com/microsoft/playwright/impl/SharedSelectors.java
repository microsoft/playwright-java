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

import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Selectors;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.microsoft.playwright.impl.LocatorUtils.setTestIdAttributeName;
import static java.nio.charset.StandardCharsets.UTF_8;

public class SharedSelectors extends LoggingSupport implements Selectors {
  private final List<SelectorsImpl> channels = new ArrayList<>();
  private final List<Registration> registrations = new ArrayList<>();

  private static class Registration {
    final String name;
    final String script;
    final RegisterOptions options;

    Registration(String name, String script, RegisterOptions options) {
      this.name = name;
      this.script = script;
      this.options = options;
    }
  }

  @Override
  public void register(String name, String script, RegisterOptions options) {
    withLogging("Selectors.register", () -> registerImpl(name, script, options));
  }

  @Override
  public void register(String name, Path path, RegisterOptions options) {
    withLogging("Selectors.register", () -> {
      byte[] buffer;
      try {
        buffer = Files.readAllBytes(path);
      } catch (IOException e) {
        throw new PlaywrightException("Failed to read selector from file: " + path, e);
      }
      registerImpl(name, new String(buffer, UTF_8), options);
    });
  }

  @Override
  public void setTestIdAttribute(String attributeName) {
    // TODO: set it per playwright insttance
    setTestIdAttributeName(attributeName);
  }

  void addChannel(SelectorsImpl channel) {
    registrations.forEach(r -> channel.registerImpl(r.name, r.script, r.options));
    channels.add(channel);
  }

  void removeChannel(SelectorsImpl channel) {
    channels.remove(channel);
  }

  private void registerImpl(String name, String script, RegisterOptions options) {
    channels.forEach(impl -> impl.registerImpl(name, script, options));
    registrations.add(new Registration(name, script, options));
  }
}
