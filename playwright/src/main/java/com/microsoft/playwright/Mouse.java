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

import com.microsoft.playwright.options.*;
import java.util.*;

/**
 * The Mouse class operates in main-frame CSS pixels relative to the top-left corner of the viewport.
 *
 * <p> Every {@code page} object has its own Mouse, accessible with [{@code property: Page.mouse}].
 */
public interface Mouse {
  class ClickOptions {
    /**
     * Defaults to {@code left}.
     */
    public MouseButton button;
    /**
     * defaults to 1. See [UIEvent.detail].
     */
    public Integer clickCount;
    /**
     * Time to wait between {@code mousedown} and {@code mouseup} in milliseconds. Defaults to 0.
     */
    public Double delay;

    public ClickOptions withButton(MouseButton button) {
      this.button = button;
      return this;
    }
    public ClickOptions withClickCount(int clickCount) {
      this.clickCount = clickCount;
      return this;
    }
    public ClickOptions withDelay(double delay) {
      this.delay = delay;
      return this;
    }
  }
  class DblclickOptions {
    /**
     * Defaults to {@code left}.
     */
    public MouseButton button;
    /**
     * Time to wait between {@code mousedown} and {@code mouseup} in milliseconds. Defaults to 0.
     */
    public Double delay;

    public DblclickOptions withButton(MouseButton button) {
      this.button = button;
      return this;
    }
    public DblclickOptions withDelay(double delay) {
      this.delay = delay;
      return this;
    }
  }
  class DownOptions {
    /**
     * Defaults to {@code left}.
     */
    public MouseButton button;
    /**
     * defaults to 1. See [UIEvent.detail].
     */
    public Integer clickCount;

    public DownOptions withButton(MouseButton button) {
      this.button = button;
      return this;
    }
    public DownOptions withClickCount(int clickCount) {
      this.clickCount = clickCount;
      return this;
    }
  }
  class MoveOptions {
    /**
     * defaults to 1. Sends intermediate {@code mousemove} events.
     */
    public Integer steps;

    public MoveOptions withSteps(int steps) {
      this.steps = steps;
      return this;
    }
  }
  class UpOptions {
    /**
     * Defaults to {@code left}.
     */
    public MouseButton button;
    /**
     * defaults to 1. See [UIEvent.detail].
     */
    public Integer clickCount;

    public UpOptions withButton(MouseButton button) {
      this.button = button;
      return this;
    }
    public UpOptions withClickCount(int clickCount) {
      this.clickCount = clickCount;
      return this;
    }
  }
  /**
   * Shortcut for [{@code method: Mouse.move}], [{@code method: Mouse.down}], [{@code method: Mouse.up}].
   */
  default void click(double x, double y) {
    click(x, y, null);
  }
  /**
   * Shortcut for [{@code method: Mouse.move}], [{@code method: Mouse.down}], [{@code method: Mouse.up}].
   */
  void click(double x, double y, ClickOptions options);
  /**
   * Shortcut for [{@code method: Mouse.move}], [{@code method: Mouse.down}], [{@code method: Mouse.up}], [{@code method: Mouse.down}] and
   * [{@code method: Mouse.up}].
   */
  default void dblclick(double x, double y) {
    dblclick(x, y, null);
  }
  /**
   * Shortcut for [{@code method: Mouse.move}], [{@code method: Mouse.down}], [{@code method: Mouse.up}], [{@code method: Mouse.down}] and
   * [{@code method: Mouse.up}].
   */
  void dblclick(double x, double y, DblclickOptions options);
  /**
   * Dispatches a {@code mousedown} event.
   */
  default void down() {
    down(null);
  }
  /**
   * Dispatches a {@code mousedown} event.
   */
  void down(DownOptions options);
  /**
   * Dispatches a {@code mousemove} event.
   */
  default void move(double x, double y) {
    move(x, y, null);
  }
  /**
   * Dispatches a {@code mousemove} event.
   */
  void move(double x, double y, MoveOptions options);
  /**
   * Dispatches a {@code mouseup} event.
   */
  default void up() {
    up(null);
  }
  /**
   * Dispatches a {@code mouseup} event.
   */
  void up(UpOptions options);
}

