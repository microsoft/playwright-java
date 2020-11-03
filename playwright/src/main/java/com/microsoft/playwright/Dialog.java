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

package com.microsoft.playwright;

import java.util.*;

/**
 * Dialog objects are dispatched by page via the 'dialog' event.
 * <p>
 * An example of using {@code Dialog} class:
 * <p>
 */
public interface Dialog {
  enum Type { ALERT, BEFOREUNLOAD, CONFIRM, PROMPT }

  default void accept() {
    accept(null);
  }
  /**
   * 
   * @param promptText A text to enter in prompt. Does not cause any effects if the dialog's {@code type} is not prompt.
   * @return Promise which resolves when the dialog has been accepted.
   */
  void accept(String promptText);
  /**
   * 
   * @return If dialog is prompt, returns default prompt value. Otherwise, returns empty string.
   */
  String defaultValue();
  /**
   * 
   * @return Promise which resolves when the dialog has been dismissed.
   */
  void dismiss();
  /**
   * 
   * @return A message displayed in the dialog.
   */
  String message();
  /**
   * 
   * @return Dialog's type, can be one of {@code alert}, {@code beforeunload}, {@code confirm} or {@code prompt}.
   */
  Type type();
}

