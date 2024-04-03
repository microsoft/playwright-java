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

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.impl.Utils;
import com.microsoft.playwright.junit.Options;
import org.junit.jupiter.api.extension.*;

import static com.microsoft.playwright.impl.junit.ExtensionUtils.isParameterSupported;

public class APIRequestContextExtension implements ParameterResolver, BeforeEachCallback, AfterAllCallback {
  private static final ThreadLocal<APIRequestContext> threadLocalAPIRequestContext = new ThreadLocal<>();

  @Override
  public void beforeEach(ExtensionContext extensionContext) {
    threadLocalAPIRequestContext.remove();
  }

  @Override
  public void afterAll(ExtensionContext extensionContext) {
    threadLocalAPIRequestContext.remove();
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return isParameterSupported(parameterContext, extensionContext, APIRequestContext.class);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return getOrCreateAPIRequestContext(extensionContext);
  }

  /**
   * Returns the APIRequestContext that belongs to the current test.  Will be created if it doesn't already exist.
   * <strong>NOTE:</strong> this method is subject to change.
   * @param extensionContext the context in which the current test or container is being executed.
   * @return The APIRequestContext that belongs to the current test.
   */
  public static APIRequestContext getOrCreateAPIRequestContext(ExtensionContext extensionContext) {
    APIRequestContext apiRequestContext = threadLocalAPIRequestContext.get();
    if (apiRequestContext != null) {
      return apiRequestContext;
    }

    Options options = OptionsExtension.getOptions(extensionContext);
    Playwright playwright = PlaywrightExtension.getOrCreatePlaywright(extensionContext);
    apiRequestContext = playwright.request().newContext(getContextOptions(options));
    threadLocalAPIRequestContext.set(apiRequestContext);
    return apiRequestContext;
  }

  private static APIRequest.NewContextOptions getContextOptions(Options options) {
    APIRequest.NewContextOptions contextOptions = Utils.clone(options.apiRequestOptions);
    if(contextOptions == null) {
      contextOptions = new APIRequest.NewContextOptions();
    }

    if(options.ignoreHTTPSErrors != null) {
      contextOptions.ignoreHTTPSErrors = options.ignoreHTTPSErrors;
    }
    return contextOptions;
  }
}
