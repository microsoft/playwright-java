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

import com.microsoft.playwright.impl.protocol.*;
import com.microsoft.playwright.Mouse;

import static com.microsoft.playwright.impl.ChannelOwner.NO_TIMEOUT;
import static com.microsoft.playwright.impl.Utils.convertType;
import static com.microsoft.playwright.impl.Utils.toProtocol;

class MouseImpl implements Mouse {
  private final PageImpl page;

  MouseImpl(PageImpl page) {
    this.page = page;
  }

  @Override
  public void click(double x, double y, ClickOptions options) {
    PageMouseClickParams params = toProtocol(options, PageMouseClickParams.class);
    params.x = x;
    params.y = y;
    page.channel.mouseClick(params, NO_TIMEOUT);
  }

  @Override
  public void dblclick(double x, double y, DblclickOptions options) {
    page.withTitle("Double click", () -> dblclickImpl(x, y, options));
  }

  private void dblclickImpl(double x, double y, DblclickOptions options) {
    ClickOptions clickOptions;
    if (options == null) {
      clickOptions = new ClickOptions();
    } else {
      clickOptions = convertType(options, ClickOptions.class);
    }
    clickOptions.clickCount = 2;
    click(x, y, clickOptions);
  }

  @Override
  public void down(DownOptions options) {
    PageMouseDownParams params = toProtocol(options, PageMouseDownParams.class);
    page.channel.mouseDown(params, NO_TIMEOUT);
  }

  @Override
  public void move(double x, double y, MoveOptions options) {
    PageMouseMoveParams params = toProtocol(options, PageMouseMoveParams.class);
    params.x = x;
    params.y = y;
    page.channel.mouseMove(params, NO_TIMEOUT);
  }

  @Override
  public void up(UpOptions options) {
    PageMouseUpParams params = toProtocol(options, PageMouseUpParams.class);
    page.channel.mouseUp(params, NO_TIMEOUT);
  }

  @Override
  public void wheel(double deltaX, double deltaY) {
    PageMouseWheelParams params = new PageMouseWheelParams();
    params.deltaX = deltaX;
    params.deltaY = deltaY;
    page.channel.mouseWheel(params, NO_TIMEOUT);
  }
}
