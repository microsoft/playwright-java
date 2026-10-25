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

package com.microsoft.playwright.fixtures;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class TestImageComparator {
  private static byte[] golden(String name) throws IOException {
    return Files.readAllBytes(Paths.get("src/test/snapshots/page/page-screenshot.spec.ts-snapshots", name));
  }

  private static byte[] png(int width, int height, int squareSize) throws IOException {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = image.createGraphics();
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, width, height);
    g.setColor(Color.BLACK);
    g.fillRect(10, 10, squareSize, squareSize);
    g.dispose();
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    ImageIO.write(image, "png", out);
    return out.toByteArray();
  }

  @Test
  void sameImageHasNoDifference() throws IOException {
    byte[] image = golden("mask-should-work-chromium.png");
    assertEquals(0, ImageComparator.countDifferentPixels(image, image));
    assertNull(ImageComparator.comparePNGs(image, image, 0));
    assertNull(ImageComparator.comparePNGs(image.clone(), image, 0));
  }

  @Test
  void differentImagesOfOneSize() throws IOException {
    byte[] a = png(100, 80, 30);
    byte[] b = png(100, 80, 40);
    // The 40x40 square has 700 more pixels than the 30x30 one.
    assertEquals(700, ImageComparator.countDifferentPixels(a, b));
    assertEquals(700, ImageComparator.countDifferentPixels(b, a));
    String message = "700 pixels (ratio 0.09 of all image pixels) are different.";
    assertEquals(message, ImageComparator.comparePNGs(a, b, 0));
    assertEquals(message, ImageComparator.comparePNGs(a, b, 699));
    assertNull(ImageComparator.comparePNGs(a, b, 700));
  }

  @Test
  void differentGoldenFiles() throws IOException {
    byte[] chromium = golden("mask-should-work-chromium.png");
    byte[] hidden = golden("hide-should-work-chromium.png");
    int count = ImageComparator.countDifferentPixels(chromium, hidden);
    assertTrue(count > 0, "count: " + count);
    String message = ImageComparator.comparePNGs(chromium, hidden, 0);
    assertNotNull(message);
    assertTrue(message.startsWith(count + " pixels (ratio "), message);
    assertNull(ImageComparator.comparePNGs(chromium, hidden, count));
  }

  @Test
  void differentSizes() throws IOException {
    byte[] small = golden("grid-cell-1-chromium.png");
    byte[] big = golden("mask-should-work-chromium.png");
    String message = ImageComparator.comparePNGs(small, big, 0);
    assertNotNull(message);
    assertTrue(message.startsWith("Expected an image 500px by 500px, received 50px by 50px. "), message);
    assertTrue(message.contains(" pixels (ratio "), message);
    assertThrows(IllegalArgumentException.class, () -> ImageComparator.countDifferentPixels(small, big));
  }
}
