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

import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.LocatorUtils.getByTextSelector;
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
    return new FrameLocatorImpl(frame, frameSelector + " >> control=enter-frame >> " + selector);
  }

  @Override
  public Locator getByAltText(String text, GetByAltTextOptions options) {
    return null;
  }

  @Override
  public Locator getByAltText(Pattern text, GetByAltTextOptions options) {
    return null;
  }

  @Override
  public Locator getByLabel(String text, GetByLabelOptions options) {
    return null;
  }

  @Override
  public Locator getByLabel(Pattern text, GetByLabelOptions options) {
    return null;
  }

  @Override
  public Locator getByPlaceholder(String text, GetByPlaceholderOptions options) {
    return null;
  }

  @Override
  public Locator getByPlaceholder(Pattern text, GetByPlaceholderOptions options) {
    return null;
  }

  @Override
  public Locator getByRole(String role, GetByRoleOptions options) {
    return null;
  }

  @Override
  public Locator getByTestId(String testId) {
    return null;
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
    return null;
  }

  @Override
  public Locator getByTitle(Pattern text, GetByTitleOptions options) {
    return null;
  }

  @Override
  public FrameLocator last() {
    return new FrameLocatorImpl(frame, frameSelector + " >> nth=-1");
  }

  @Override
  public Locator locator(String selector, LocatorOptions options) {
    return new LocatorImpl(frame, frameSelector + " >> control=enter-frame >> " + selector, convertType(options, Locator.LocatorOptions.class));
  }

  @Override
  public FrameLocator nth(int index) {
    return new FrameLocatorImpl(frame, frameSelector + " >> nth=" + index);
  }
}
