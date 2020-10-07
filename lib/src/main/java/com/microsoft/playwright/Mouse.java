/**
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

public interface Mouse {
  enum Button { LEFT, MIDDLE, RIGHT }

  class ClickOptions {
    public Button button;
    public Integer clickCount;
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
    public Button button;
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
    public Button button;
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
    public Integer steps;

    public MoveOptions withSteps(Integer steps) {
      this.steps = steps;
      return this;
    }
  }
  class UpOptions {
    public Button button;
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
  void click(int x, int y, ClickOptions options);
  default void dblclick(int x, int y) {
    dblclick(x, y, null);
  }
  void dblclick(int x, int y, DblclickOptions options);
  default void down() {
    down(null);
  }
  void down(DownOptions options);
  default void move(int x, int y) {
    move(x, y, null);
  }
  void move(int x, int y, MoveOptions options);
  default void up() {
    up(null);
  }
  void up(UpOptions options);
}

