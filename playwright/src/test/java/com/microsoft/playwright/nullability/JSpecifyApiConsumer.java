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

package com.microsoft.playwright.nullability;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Frame;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.Cookie;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class JSpecifyApiConsumer {
  // Only unguarded dereferences and null arguments/assignments are checked by NullAway,
  // so this file exercises non-null returns and nullable inputs.
  static void useApi(Page page, Frame frame) {
    frame.name().length();

    page.evaluate("() => null", null);
    page.addScriptTag(null);

    Browser.NewContextOptions options = new Browser.NewContextOptions();
    options.setColorScheme(null);
    options.colorScheme = null;

    Cookie cookie = new Cookie("name", "value");
    cookie.url = null;
  }
}
