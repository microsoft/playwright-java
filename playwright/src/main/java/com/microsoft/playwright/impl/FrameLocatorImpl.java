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
  public FrameLocator last() {
    return new FrameLocatorImpl(frame, frameSelector + " >> nth=-1");
  }

  @Override
  public LocatorImpl locator(String selector) {
    return new LocatorImpl(frame, frameSelector + " >> control=enter-frame >> " + selector);
  }

  @Override
  public FrameLocator nth(int index) {
    return new FrameLocatorImpl(frame, frameSelector + " >> nth=" + index);
  }
}
