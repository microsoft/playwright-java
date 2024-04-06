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

package com.microsoft.playwright.impl.junit;

import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.junit.Options;
import com.microsoft.playwright.junit.UsePlaywright;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static com.microsoft.playwright.impl.junit.ExtensionUtils.*;

public class OptionsExtension implements AfterAllCallback {
  // This is public to allow our tests to set the wsEndpoint dynamically
  public static final ThreadLocal<Options> threadLocalOptions = new ThreadLocal<>();

  @Override
  public void afterAll(ExtensionContext extensionContext) {
    threadLocalOptions.remove();
  }

  static Options getOptions(ExtensionContext extensionContext) {
    Options options = threadLocalOptions.get();
    if (options != null) {
      return options;
    }

    UsePlaywright usePlaywrightAnnotation = getUsePlaywrightAnnotation(extensionContext);
    try {
      options = usePlaywrightAnnotation.value().newInstance().getOptions();
      threadLocalOptions.set(options);
    } catch (InstantiationException | IllegalAccessException e) {
      throw new PlaywrightException("Failed to create options", e);
    }
    return options;
  }
}
