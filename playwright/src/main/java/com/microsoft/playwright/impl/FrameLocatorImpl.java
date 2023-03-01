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

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.AriaRole;

import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.LocatorUtils.*;
import static com.microsoft.playwright.impl.Utils.convertType;

class FrameLocatorImpl implements FrameLocator {
  private final FrameImpl frame;
  private final String frameSelector;

  FrameLocatorImpl(FrameImpl frame, String selector) {
    this.frame = frame;
    this.frameSelector = selector;
  }

  @Override
  public FrameLocator first() {
    return new FrameLocatorImpl(frame, frameSelector + " >> nth=0");
  }

  @Override
  public FrameLocatorImpl frameLocator(String selector) {
    return new FrameLocatorImpl(frame, frameSelector + " >> internal:control=enter-frame >> " + selector);
  }

  @Override
  public Locator getByAltText(String text, GetByAltTextOptions options) {
    return locator(getByAltTextSelector(text, convertType(options, Locator.GetByAltTextOptions.class)));
  }

  @Override
  public Locator getByAltText(Pattern text, GetByAltTextOptions options) {
    return locator(getByAltTextSelector(text, convertType(options, Locator.GetByAltTextOptions.class)));
  }

  @Override
  public Locator getByLabel(String text, GetByLabelOptions options) {
    return locator(getByLabelSelector(text, convertType(options, Locator.GetByLabelOptions.class)));
  }

  @Override
  public Locator getByLabel(Pattern text, GetByLabelOptions options) {
    return locator(getByLabelSelector(text, convertType(options, Locator.GetByLabelOptions.class)));
  }

  @Override
  public Locator getByPlaceholder(String text, GetByPlaceholderOptions options) {
    return locator(getByPlaceholderSelector(text, convertType(options, Locator.GetByPlaceholderOptions.class)));
  }

  @Override
  public Locator getByPlaceholder(Pattern text, GetByPlaceholderOptions options) {
    return locator(getByPlaceholderSelector(text, convertType(options, Locator.GetByPlaceholderOptions.class)));
  }

  @Override
  public Locator getByRole(AriaRole role, GetByRoleOptions options) {
    return locator(getByRoleSelector(role, convertType(options, Locator.GetByRoleOptions.class)));
  }

  @Override
  public Locator getByTestId(String testId) {
    return locator(getByTestIdSelector(testId));
  }

  @Override
  public Locator getByTestId(Pattern testId) {
    return locator(getByTestIdSelector(testId));
  }

  @Override
  public Locator getByText(String text, GetByTextOptions options) {
    return locator(getByTextSelector(text, convertType(options, Locator.GetByTextOptions.class)));
  }

  @Override
  public Locator getByText(Pattern text, GetByTextOptions options) {
    return locator(getByTextSelector(text, convertType(options, Locator.GetByTextOptions.class)));
  }

  @Override
  public Locator getByTitle(String text, GetByTitleOptions options) {
    return locator(getByTitleSelector(text, convertType(options, Locator.GetByTitleOptions.class)));
  }

  @Override
  public Locator getByTitle(Pattern text, GetByTitleOptions options) {
    return locator(getByTitleSelector(text, convertType(options, Locator.GetByTitleOptions.class)));
  }

  @Override
  public FrameLocator last() {
    return new FrameLocatorImpl(frame, frameSelector + " >> nth=-1");
  }

  @Override
  public Locator locator(String selector, LocatorOptions options) {
    return new LocatorImpl(frame, frameSelector + " >> internal:control=enter-frame >> " + selector, convertType(options, Locator.LocatorOptions.class));
  }

  @Override
  public Locator locator(Locator selectorOrLocator, LocatorOptions options) {
    LocatorImpl other = (LocatorImpl) selectorOrLocator;
    if (other.frame != frame) {
      throw new PlaywrightException("Locators must belong to the same frame.");
    }
    return locator(other.selector, options);
  }

  @Override
  public FrameLocator nth(int index) {
    return new FrameLocatorImpl(frame, frameSelector + " >> nth=" + index);
  }
}
