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
import java.util.function.BiConsumer;

interface Mouse {
  class ClickOptions {
    enum Button { LEFT, MIDDLE, RIGHT}
    Button button;
    Integer clickCount;
    Integer delay;
  }
  class DblclickOptions {
    enum Button { LEFT, MIDDLE, RIGHT}
    Button button;
    Integer delay;
  }
  class DownOptions {
    enum Button { LEFT, MIDDLE, RIGHT}
    Button button;
    Integer clickCount;
  }
  class MoveOptions {
    Integer steps;
  }
  class UpOptions {
    enum Button { LEFT, MIDDLE, RIGHT}
    Button button;
    Integer clickCount;
  }
  void click(int x, int y, ClickOptions options);
  void dblclick(int x, int y, DblclickOptions options);
  void down(DownOptions options);
  void move(int x, int y, MoveOptions options);
  void up(UpOptions options);
}

