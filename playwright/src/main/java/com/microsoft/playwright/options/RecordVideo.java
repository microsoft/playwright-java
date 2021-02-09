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

package com.microsoft.playwright.options;

import java.nio.file.Path;
public class RecordVideo {
  /**
   * Path to the directory to put videos into.
   */
  public Path dir;
  /**
   * Optional dimensions of the recorded videos. If not specified the size will be equal to {@code viewport}. If {@code viewport} is not
   * configured explicitly the video size defaults to 1280x720. Actual picture of each page will be scaled down if necessary
   * to fit the specified size.
   */
  public Size size;

  public RecordVideo(Path dir, Size size) {
    this.dir = dir;
    this.size = size;
  }
  public RecordVideo() {
  }
  public RecordVideo withDir(Path dir) {
    this.dir = dir;
    return this;
  }
  public RecordVideo withSize(Size size) {
    this.size = size;
    return this;
  }
}