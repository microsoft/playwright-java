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

package com.microsoft.playwright.junit;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Playwright;

/**
 * <strong>NOTE:</strong> this API is experimental and is subject to changes.
 *
 * <p> Instances of this class are expected to be created by custom {@link OptionsFactory}
 * implementations. Implement custom factories to provide custom Playwright configurations.
 *
 * <p> For more details and usage examples see our
 * <a href="https://playwright.dev/java/docs/junit">JUnit guide</a>.
 */
public class Options {
  public String baseUrl;
  public String channel;
  public Boolean headless;
  public String browserName;
  public String deviceName;
  // Custom attribute to be used in page.getByTestId(). data-testid is used by default.
  public String testIdAttribute;
  public Boolean ignoreHTTPSErrors;
  public BrowserType.LaunchOptions launchOptions;
  public Browser.NewContextOptions contextOptions;
  public APIRequest.NewContextOptions apiRequestOptions;
  public Playwright.CreateOptions playwrightCreateOptions;
  // WebSocket endpoint to be used when connecting to a remote browser.
  // If this is set, BrowserType.connect will be used.  Otherwise, BrowserType.launch will be used.
  public String wsEndpoint;
  public BrowserType.ConnectOptions connectOptions;

  public Options setWsEndpoint(String wsEndpoint) {
    this.wsEndpoint = wsEndpoint;
    return this;
  }

  public Options setConnectOptions(BrowserType.ConnectOptions connectOptions) {
    this.connectOptions = connectOptions;
    return this;
  }

  public Options setPlaywrightCreateOptions(Playwright.CreateOptions playwrightCreateOptions) {
    this.playwrightCreateOptions = playwrightCreateOptions;
    return this;
  }

  public Options setLaunchOptions(BrowserType.LaunchOptions launchOptions) {
    this.launchOptions = launchOptions;
    return this;
  }

  public Options setContextOptions(Browser.NewContextOptions contextOptions) {
    this.contextOptions = contextOptions;
    return this;
  }

  public Options setApiRequestOptions(APIRequest.NewContextOptions apiRequestOptions) {
    this.apiRequestOptions = apiRequestOptions;
    return this;
  }

  public Options setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
    return this;
  }

  public Options setTestIdAttribute(String name) {
    this.testIdAttribute = name;
    return this;
  }

  public Options setBrowserName(String browserName) {
    this.browserName = browserName;
    return this;
  }

  public Options setDeviceName(String deviceName) {
    this.deviceName = deviceName;
    return this;
  }

  public Options setChannel(String channel) {
    this.channel = channel;
    return this;
  }

  public Options setHeadless(Boolean headless) {
    this.headless = headless;
    return this;
  }

  public Options setIgnoreHTTPSErrors(Boolean ignoreHTTPSErrors) {
    this.ignoreHTTPSErrors = ignoreHTTPSErrors;
    return this;
  }
}
