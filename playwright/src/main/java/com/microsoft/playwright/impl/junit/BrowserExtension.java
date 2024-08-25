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
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.impl.Utils;
import com.microsoft.playwright.junit.Options;
import org.junit.jupiter.api.extension.*;

import java.util.HashMap;
import java.util.Map;

import static com.microsoft.playwright.impl.junit.ExtensionUtils.*;

public class BrowserExtension implements ParameterResolver, AfterAllCallback {
  private static final ThreadLocal<Map<String,Browser>> threadLocalBrowser = ThreadLocal.withInitial(HashMap::new);

  @Override
  public void afterAll(ExtensionContext extensionContext) {
    Map<String, Browser> browserMap = threadLocalBrowser.get();
    threadLocalBrowser.remove();
    browserMap.values().forEach(Browser::close);
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return isParameterSupported(parameterContext, extensionContext, Browser.class);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return getOrCreateBrowser(extensionContext);
  }

  /**
   * Returns the Browser that belongs to the current test.  Will be created if it doesn't already exist.
   * <strong>NOTE:</strong> this method is subject to change.
   * @param extensionContext the context in which the current test or container is being executed.
   * @return The Browser that belongs to the current test.
   */
  public static Browser getOrCreateBrowser(ExtensionContext extensionContext) {

    Options options = OptionsExtension.getOptions(extensionContext);
    Playwright playwright = PlaywrightExtension.getOrCreatePlaywright(extensionContext);

    String selectedBrowserName = getSelectedBrowserName(playwright, options, extensionContext);
    Browser browser = threadLocalBrowser.get().get(selectedBrowserName);
    if (browser != null) {
      return browser;
    }

    BrowserType browserType = getBrowserTypeForName(playwright, selectedBrowserName);
    if(options.wsEndpoint != null && !options.wsEndpoint.isEmpty()) {
      BrowserType.ConnectOptions connectOptions = getConnectOptions(options);
      browser = browserType.connect(options.wsEndpoint, connectOptions);
    } else {
      BrowserType.LaunchOptions launchOptions = getLaunchOptions(options);
      browser = browserType.launch(launchOptions);
    }

    threadLocalBrowser.get().put(selectedBrowserName, browser);
    return browser;
  }

  private static String getSelectedBrowserName(Playwright playwright, Options options, ExtensionContext extensionContext) {
    String selectedBrowser = extensionContext.getStore(PlaywrightExtension.namespace).get(SelectedBrowserExtension.class, String.class);
    if (selectedBrowser != null) {
      return selectedBrowser;
    } else if (options.browserName != null) {
      return options.browserName;
    } else if  (options.deviceName != null) {
      DeviceDescriptor deviceDescriptor = DeviceDescriptor.findByName(playwright, options.deviceName);
      if (deviceDescriptor != null && deviceDescriptor.defaultBrowserType != null) {
        return deviceDescriptor.defaultBrowserType;
      }
    }
    return "chromium";
  }

  private static BrowserType.ConnectOptions getConnectOptions(Options options) {
    BrowserType.ConnectOptions connectOptions = options.connectOptions;
    if(connectOptions == null) {
      connectOptions = new BrowserType.ConnectOptions();
    }
    return connectOptions;
  }

  private static BrowserType getBrowserTypeForName(Playwright playwright, String name) {
    switch (name) {
      case "webkit":
        return playwright.webkit();
      case "firefox":
        return playwright.firefox();
      case "chromium":
        return playwright.chromium();
      default:
        throw new PlaywrightException("Invalid browser name: " + name);
    }
  }

  private static BrowserType.LaunchOptions getLaunchOptions(Options options) {
    BrowserType.LaunchOptions launchOptions = Utils.clone(options.launchOptions);
    if (launchOptions == null) {
      launchOptions = new BrowserType.LaunchOptions();
    }

    if (options.headless != null) {
      launchOptions.setHeadless(options.headless);
    }

    if (options.channel != null) {
      launchOptions.setChannel(options.channel);
    }

    return launchOptions;
  }
}
