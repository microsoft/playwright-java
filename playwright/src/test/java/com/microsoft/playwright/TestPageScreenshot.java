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

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

// TODO: suite.skip(browserName === "firefox" && headful");
public class TestPageScreenshot extends TestBase {
  @Test
  void shouldWork() throws IOException {
    page.setViewportSize(500, 500);
    page.navigate(server.PREFIX + "/grid.html");
    byte[] screenshot = page.screenshot();
    BufferedImage image = ImageIO.read(new ByteArrayInputStream(screenshot));
    assertEquals(500, image.getWidth());
    assertEquals(500, image.getHeight());
//    expect(screenshot).toMatchSnapshot("screenshot-sanity.png");
  }

  @Test
  void shouldClipRect() throws IOException {
    page.setViewportSize(500, 500);
    page.navigate(server.PREFIX + "/grid.html");
    byte[] screenshot = page.screenshot(new Page.ScreenshotOptions()
      .setClip().withX(50).withY(100).withWidth(150).withHeight(100).done());
    BufferedImage image = ImageIO.read(new ByteArrayInputStream(screenshot));
    assertEquals(150, image.getWidth());
    assertEquals(100, image.getHeight());
//    expect(screenshot).toMatchSnapshot("screenshot-clip-rect.png");
  }
}
