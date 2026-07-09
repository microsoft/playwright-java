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

import com.microsoft.playwright.options.AnnotatePosition;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.impl.Serialization.gson;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSerialization {
  @Test
  void annotatePositionSerializesToLowerCaseAndDash() {
    assertEquals("top-left", gson().toJsonTree(AnnotatePosition.TOP_LEFT).getAsString());
    assertEquals("top", gson().toJsonTree(AnnotatePosition.TOP).getAsString());
    assertEquals("top-right", gson().toJsonTree(AnnotatePosition.TOP_RIGHT).getAsString());
    assertEquals("bottom-left", gson().toJsonTree(AnnotatePosition.BOTTOM_LEFT).getAsString());
    assertEquals("bottom", gson().toJsonTree(AnnotatePosition.BOTTOM).getAsString());
    assertEquals("bottom-right", gson().toJsonTree(AnnotatePosition.BOTTOM_RIGHT).getAsString());
  }
}
