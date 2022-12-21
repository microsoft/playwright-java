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

/**
 * The Mouse class operates in main-frame CSS pixels relative to the top-left corner of the viewport.
 *
 * <p> Every {@code page} object has its own Mouse, accessible with {@link Page#mouse Page.mouse()}.
 * <pre>{@code
 * // Using ‘page.mouse’ to trace a 100x100 square.
 * page.mouse().move(0, 0);
 * page.mouse().down();
 * page.mouse().move(0, 100);
 * page.mouse().move(100, 100);
 * page.mouse().move(100, 0);
 * page.mouse().move(0, 0);
 * page.mouse().up();
 * }</pre>
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

    /**
     * Defaults to {@code left}.
     */
    public ClickOptions setButton(MouseButton button) {
      this.button = button;
      return this;
    }
    /**
     * defaults to 1. See [UIEvent.detail].
     */
    public ClickOptions setClickCount(int clickCount) {
      this.clickCount = clickCount;
      return this;
    }
    /**
     * Time to wait between {@code mousedown} and {@code mouseup} in milliseconds. Defaults to 0.
     */
    public ClickOptions setDelay(double delay) {
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

    /**
     * Defaults to {@code left}.
     */
    public DblclickOptions setButton(MouseButton button) {
      this.button = button;
      return this;
    }
    /**
     * Time to wait between {@code mousedown} and {@code mouseup} in milliseconds. Defaults to 0.
     */
    public DblclickOptions setDelay(double delay) {
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

    /**
     * Defaults to {@code left}.
     */
    public DownOptions setButton(MouseButton button) {
      this.button = button;
      return this;
    }
    /**
     * defaults to 1. See [UIEvent.detail].
     */
    public DownOptions setClickCount(int clickCount) {
      this.clickCount = clickCount;
      return this;
    }
  }
  class MoveOptions {
    /**
     * Defaults to 1. Sends intermediate {@code mousemove} events.
     */
    public Integer steps;

    /**
     * Defaults to 1. Sends intermediate {@code mousemove} events.
     */
    public MoveOptions setSteps(int steps) {
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

    /**
     * Defaults to {@code left}.
     */
    public UpOptions setButton(MouseButton button) {
      this.button = button;
      return this;
    }
    /**
     * defaults to 1. See [UIEvent.detail].
     */
    public UpOptions setClickCount(int clickCount) {
      this.clickCount = clickCount;
      return this;
    }
  }
  /**
   * Shortcut for {@link Mouse#move Mouse.move()}, {@link Mouse#down Mouse.down()}, {@link Mouse#up Mouse.up()}.
   *
   * @since v1.8
   */
  default void click(double x, double y) {
    click(x, y, null);
  }
  /**
   * Shortcut for {@link Mouse#move Mouse.move()}, {@link Mouse#down Mouse.down()}, {@link Mouse#up Mouse.up()}.
   *
   * @since v1.8
   */
  void click(double x, double y, ClickOptions options);
  /**
   * Shortcut for {@link Mouse#move Mouse.move()}, {@link Mouse#down Mouse.down()}, {@link Mouse#up Mouse.up()}, {@link
   * Mouse#down Mouse.down()} and {@link Mouse#up Mouse.up()}.
   *
   * @since v1.8
   */
  default void dblclick(double x, double y) {
    dblclick(x, y, null);
  }
  /**
   * Shortcut for {@link Mouse#move Mouse.move()}, {@link Mouse#down Mouse.down()}, {@link Mouse#up Mouse.up()}, {@link
   * Mouse#down Mouse.down()} and {@link Mouse#up Mouse.up()}.
   *
   * @since v1.8
   */
  void dblclick(double x, double y, DblclickOptions options);
  /**
   * Dispatches a {@code mousedown} event.
   *
   * @since v1.8
   */
  default void down() {
    down(null);
  }
  /**
   * Dispatches a {@code mousedown} event.
   *
   * @since v1.8
   */
  void down(DownOptions options);
  /**
   * Dispatches a {@code mousemove} event.
   *
   * @since v1.8
   */
  default void move(double x, double y) {
    move(x, y, null);
  }
  /**
   * Dispatches a {@code mousemove} event.
   *
   * @since v1.8
   */
  void move(double x, double y, MoveOptions options);
  /**
   * Dispatches a {@code mouseup} event.
   *
   * @since v1.8
   */
  default void up() {
    up(null);
  }
  /**
   * Dispatches a {@code mouseup} event.
   *
   * @since v1.8
   */
  void up(UpOptions options);
  /**
   * Dispatches a {@code wheel} event.
   *
   * <p> <strong>NOTE:</strong> Wheel events may cause scrolling if they are not handled, and this method does not wait for the scrolling to finish
   * before returning.
   *
   * @param deltaX Pixels to scroll horizontally.
   * @param deltaY Pixels to scroll vertically.
   * @since v1.15
   */
  void wheel(double deltaX, double deltaY);
}

