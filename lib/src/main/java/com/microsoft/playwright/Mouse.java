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

interface Mouse{

  class ClickOptions {
    enum Button { LEFT, MIDDLE, RIGHT }
    Button button;
    Integer clickCount;
    Integer delay;
  }
  void click(int x, int y, ClickOptions options);

  class DblclickOptions {
    enum Button { LEFT, MIDDLE, RIGHT }
    Button button;
    Integer delay;
  }
  void dblclick(int x, int y, DblclickOptions options);

  class DownOptions {
    enum Button { LEFT, MIDDLE, RIGHT }
    Button button;
    Integer clickCount;
  }
  void down(DownOptions options);

  class MoveOptions {
    Integer steps;
  }
  void move(int x, int y, MoveOptions options);

  class UpOptions {
    enum Button { LEFT, MIDDLE, RIGHT }
    Button button;
    Integer clickCount;
  }
  void up(UpOptions options);
}

