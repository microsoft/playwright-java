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

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.Clip;
import com.microsoft.playwright.options.ScreenshotAnimations;
import com.microsoft.playwright.options.ScreenshotCaret;
import com.microsoft.playwright.options.ScreenshotScale;

import java.util.List;

// Common shape shared by PageAssertions.HasScreenshotOptions and LocatorAssertions.HasScreenshotOptions,
// used as an intermediate type for Utils#convertType().
class ScreenshotAssertionsOptions {
  Double timeout;
  ScreenshotAnimations animations;
  ScreenshotCaret caret;
  Clip clip;
  Boolean fullPage;
  List<Locator> mask;
  String maskColor;
  Boolean omitBackground;
  ScreenshotScale scale;
  Integer maxDiffPixels;
  Double maxDiffPixelRatio;
  Double threshold;
  String style;
}
