/*
 * Copyright (c) Microsoft Corporation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.playwright.impl;

import com.microsoft.playwright.Keyboard;

import static com.microsoft.playwright.impl.ChannelOwner.NO_TIMEOUT;
import static com.microsoft.playwright.impl.Utils.convertType;

class KeyboardImpl implements Keyboard {
  private final PageImpl page;

  KeyboardImpl(PageImpl page) {
    this.page = page;
  }

  @Override
  public void down(String key) {
    PageKeyboardDownParams params = new PageKeyboardDownParams();
    params.key = key;
    page.channel.keyboardDown(params, NO_TIMEOUT);
  }

  @Override
  public void insertText(String text) {
    PageKeyboardInsertTextParams params = new PageKeyboardInsertTextParams();
    params.text = text;
    page.channel.keyboardInsertText(params, NO_TIMEOUT);
  }

  @Override
  public void press(String key, PressOptions options) {
    pressImpl(key, options);
  }

  private void pressImpl(String key, PressOptions options) {
    PageKeyboardPressParams params = convertType(options, PageKeyboardPressParams.class);
    if (params == null) {
      params = new PageKeyboardPressParams();
    }
    params.key = key;
    page.channel.keyboardPress(params, NO_TIMEOUT);
  }

  @Override
  public void type(String text, TypeOptions options) {
    typeImpl(text, options);
  }

  private void typeImpl(String text, TypeOptions options) {
    PageKeyboardTypeParams params = convertType(options, PageKeyboardTypeParams.class);
    if (params == null) {
      params = new PageKeyboardTypeParams();
    }
    params.text = text;
    page.channel.keyboardType(params, NO_TIMEOUT);
  }

  @Override
  public void up(String key) {
    PageKeyboardUpParams params = new PageKeyboardUpParams();
    params.key = key;
    page.channel.keyboardUp(params, NO_TIMEOUT);
  }
}
