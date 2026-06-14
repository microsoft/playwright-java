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

import com.microsoft.playwright.ScreencastFrame;

class ScreencastFrameImpl implements ScreencastFrame {
  private final byte[] data;
  private final double timestamp;
  private final int viewportWidth;
  private final int viewportHeight;

  ScreencastFrameImpl(byte[] data, double timestamp, int viewportWidth, int viewportHeight) {
    this.data = data;
    this.timestamp = timestamp;
    this.viewportWidth = viewportWidth;
    this.viewportHeight = viewportHeight;
  }

  @Override
  public byte[] data() {
    return data;
  }

  @Override
  public double timestamp() {
    return timestamp;
  }

  @Override
  public int viewportWidth() {
    return viewportWidth;
  }

  @Override
  public int viewportHeight() {
    return viewportHeight;
  }
}
