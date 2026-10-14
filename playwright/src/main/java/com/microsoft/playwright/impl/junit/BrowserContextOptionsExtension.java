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

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.impl.Utils;
import com.microsoft.playwright.junit.Options;
import org.junit.jupiter.api.extension.*;

import static com.microsoft.playwright.impl.junit.ExtensionUtils.isParameterSupported;

public class BrowserContextOptionsExtension implements ParameterResolver {
  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return isParameterSupported(parameterContext, extensionContext, Browser.NewContextOptions.class);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    Options options = OptionsExtension.getOptions(extensionContext);
    Playwright playwright = PlaywrightExtension.getOrCreatePlaywright(extensionContext);
    return getContextOptions(playwright, options);
  }

  // Returns a new instance every time, so that callers can modify it without affecting other tests.
  static Browser.NewContextOptions getContextOptions(Playwright playwright, Options options) {
    Browser.NewContextOptions contextOptions = Utils.clone(options.contextOptions);
    if (contextOptions == null) {
      contextOptions = new Browser.NewContextOptions();
    }

    if (options.baseUrl != null) {
      contextOptions.setBaseURL(options.baseUrl);
    }

    if (options.deviceName != null) {
      DeviceDescriptor deviceDescriptor = DeviceDescriptor.findByName(playwright, options.deviceName);
      if (deviceDescriptor == null) {
        throw new PlaywrightException("Unknown device name: " + options.deviceName);
      }
      contextOptions.userAgent = deviceDescriptor.userAgent;
      if (deviceDescriptor.viewport != null) {
        contextOptions.setViewportSize(deviceDescriptor.viewport.width, deviceDescriptor.viewport.height);
      }
      contextOptions.deviceScaleFactor = deviceDescriptor.deviceScaleFactor;
      contextOptions.isMobile = deviceDescriptor.isMobile;
      contextOptions.hasTouch = deviceDescriptor.hasTouch;
    }

    if (options.ignoreHTTPSErrors != null) {
      contextOptions.setIgnoreHTTPSErrors(options.ignoreHTTPSErrors);
    }

    return contextOptions;
  }
}
