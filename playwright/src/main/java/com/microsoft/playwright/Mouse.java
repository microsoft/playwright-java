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
 * The Mouse class operates in main-frame CSS pixels relative to the top-left corner of the viewport.
 * <p>
 * Every {@code page} object has its own Mouse, accessible with page.mouse.
 * <p>
 */
public interface Mouse {
  enum Button { LEFT, MIDDLE, RIGHT }

  class ClickOptions {
    /**
     * Defaults to {@code left}.
     */
    public Button button;
    /**
     * defaults to 1. See UIEvent.detail.
     */
    public Integer clickCount;
    /**
     * Time to wait between {@code mousedown} and {@code mouseup} in milliseconds. Defaults to 0.
     */
    public Integer delay;

    public ClickOptions withButton(Button button) {
      this.button = button;
      return this;
    }
    public ClickOptions withClickCount(Integer clickCount) {
      this.clickCount = clickCount;
      return this;
    }
    public ClickOptions withDelay(Integer delay) {
      this.delay = delay;
      return this;
    }
  }
  class DblclickOptions {
    /**
     * Defaults to {@code left}.
     */
    public Button button;
    /**
     * Time to wait between {@code mousedown} and {@code mouseup} in milliseconds. Defaults to 0.
     */
    public Integer delay;

    public DblclickOptions withButton(Button button) {
      this.button = button;
      return this;
    }
    public DblclickOptions withDelay(Integer delay) {
      this.delay = delay;
      return this;
    }
  }
  class DownOptions {
    /**
     * Defaults to {@code left}.
     */
    public Button button;
    /**
     * defaults to 1. See UIEvent.detail.
     */
    public Integer clickCount;

    public DownOptions withButton(Button button) {
      this.button = button;
      return this;
    }
    public DownOptions withClickCount(Integer clickCount) {
      this.clickCount = clickCount;
      return this;
    }
  }
  class MoveOptions {
    /**
     * defaults to 1. Sends intermediate {@code mousemove} events.
     */
    public Integer steps;

    public MoveOptions withSteps(Integer steps) {
      this.steps = steps;
      return this;
    }
  }
  class UpOptions {
    /**
     * Defaults to {@code left}.
     */
    public Button button;
    /**
     * defaults to 1. See UIEvent.detail.
     */
    public Integer clickCount;

    public UpOptions withButton(Button button) {
      this.button = button;
      return this;
    }
    public UpOptions withClickCount(Integer clickCount) {
      this.clickCount = clickCount;
      return this;
    }
  }
  default void click(int x, int y) {
    click(x, y, null);
  }
  /**
   * Shortcut for {@code mouse.move(x, y[, options])}, {@code mouse.down([options])}, {@code mouse.up([options])}.
   */
  void click(int x, int y, ClickOptions options);
  default void dblclick(int x, int y) {
    dblclick(x, y, null);
  }
  /**
   * Shortcut for {@code mouse.move(x, y[, options])}, {@code mouse.down([options])}, {@code mouse.up([options])}, {@code mouse.down([options])} and {@code mouse.up([options])}.
   */
  void dblclick(int x, int y, DblclickOptions options);
  default void down() {
    down(null);
  }
  /**
   * Dispatches a {@code mousedown} event.
   */
  void down(DownOptions options);
  default void move(int x, int y) {
    move(x, y, null);
  }
  /**
   * Dispatches a {@code mousemove} event.
   */
  void move(int x, int y, MoveOptions options);
  default void up() {
    up(null);
  }
  /**
   * Dispatches a {@code mouseup} event.
   */
  void up(UpOptions options);
}

