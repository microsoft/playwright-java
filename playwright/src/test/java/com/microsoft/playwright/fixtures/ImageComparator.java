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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Compares screenshots the way {@code toMatchSnapshot} does for images when the config names no
 * comparator: pixelmatch at threshold 0.2. {@link #pixelmatch} is a port of the pixelmatch package
 * (https://github.com/mapbox/pixelmatch) without the diff output.
 */
public final class ImageComparator {
  /** A decoded RGBA image. */
  public static final class Image {
    public final int width;
    public final int height;
    public final byte[] data;

    Image(int width, int height, byte[] data) {
      this.width = width;
      this.height = height;
      this.data = data;
    }

    public int[] pixel(int x, int y) {
      int offset = (y * width + x) * 4;
      return new int[]{data[offset] & 0xff, data[offset + 1] & 0xff, data[offset + 2] & 0xff, data[offset + 3] & 0xff};
    }
  }

  private ImageComparator() {
  }

  /** Decodes a png or jpeg to RGBA. */
  public static Image decode(byte[] encoded) {
    BufferedImage image;
    try {
      image = ImageIO.read(new ByteArrayInputStream(encoded));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    if (image == null) {
      throw new IllegalArgumentException("Not a png or jpeg image");
    }
    int width = image.getWidth();
    int height = image.getHeight();
    int[] argb = image.getRGB(0, 0, width, height, null, 0, width);
    byte[] data = new byte[width * height * 4];
    for (int i = 0; i < argb.length; i++) {
      data[i * 4] = (byte) (argb[i] >> 16);
      data[i * 4 + 1] = (byte) (argb[i] >> 8);
      data[i * 4 + 2] = (byte) argb[i];
      data[i * 4 + 3] = (byte) (argb[i] >>> 24);
    }
    return new Image(width, height, data);
  }

  /** Null when the images match within {@code maxDiffPixels}, else the message of the matcher. */
  public static String compare(byte[] expectedEncoded, byte[] actualEncoded, int maxDiffPixels) {
    Image expected = decode(expectedEncoded);
    Image actual = decode(actualEncoded);
    String sizeError = "";
    if (expected.width != actual.width || expected.height != actual.height) {
      sizeError = "Expected an image " + expected.width + "px by " + expected.height + "px, received " + actual.width + "px by " + actual.height + "px. ";
      int width = Math.max(expected.width, actual.width);
      int height = Math.max(expected.height, actual.height);
      expected = pad(expected, width, height);
      actual = pad(actual, width, height);
    }
    int count = pixelmatch(expected.data, actual.data, expected.width, expected.height, 0.2);
    double ratio = Math.ceil(count * 100.0 / (expected.width * expected.height)) / 100;
    String pixelsError = count > maxDiffPixels ? count + " pixels (ratio " + String.format("%.2f", ratio) + " of all image pixels) are different." : "";
    if (sizeError.isEmpty() && pixelsError.isEmpty()) {
      return null;
    }
    return sizeError + pixelsError;
  }

  private static Image pad(Image image, int width, int height) {
    if (image.width == width && image.height == height) {
      return image;
    }
    byte[] data = new byte[width * height * 4];
    for (int y = 0; y < image.height; y++) {
      System.arraycopy(image.data, y * image.width * 4, data, y * width * 4, image.width * 4);
    }
    return new Image(width, height, data);
  }

  /** The number of pixels that differ between two RGBA buffers, anti-aliased ones excluded. */
  public static int pixelmatch(byte[] img1, byte[] img2, int width, int height, double threshold) {
    if (img1.length != img2.length || img1.length != width * height * 4) {
      throw new IllegalArgumentException("Image sizes do not match");
    }
    if (java.util.Arrays.equals(img1, img2)) {
      return 0;
    }
    double maxDelta = 35215 * threshold * threshold;
    int diff = 0;
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int pos = (y * width + x) * 4;
        double delta = colorDelta(img1, img2, pos, pos, false);
        if (Math.abs(delta) > maxDelta) {
          if (antialiased(img1, x, y, width, height, img2) || antialiased(img2, x, y, width, height, img1)) {
            continue;
          }
          diff++;
        }
      }
    }
    return diff;
  }

  private static boolean antialiased(byte[] img, int x1, int y1, int width, int height, byte[] img2) {
    int x0 = Math.max(x1 - 1, 0);
    int y0 = Math.max(y1 - 1, 0);
    int x2 = Math.min(x1 + 1, width - 1);
    int y2 = Math.min(y1 + 1, height - 1);
    int pos = (y1 * width + x1) * 4;
    int zeroes = x1 == x0 || x1 == x2 || y1 == y0 || y1 == y2 ? 1 : 0;
    double min = 0;
    double max = 0;
    int minX = 0;
    int minY = 0;
    int maxX = 0;
    int maxY = 0;
    for (int x = x0; x <= x2; x++) {
      for (int y = y0; y <= y2; y++) {
        if (x == x1 && y == y1) {
          continue;
        }
        double delta = colorDelta(img, img, pos, (y * width + x) * 4, true);
        if (delta == 0) {
          zeroes++;
          if (zeroes > 2) {
            return false;
          }
        } else if (delta < min) {
          min = delta;
          minX = x;
          minY = y;
        } else if (delta > max) {
          max = delta;
          maxX = x;
          maxY = y;
        }
      }
    }
    if (min == 0 || max == 0) {
      return false;
    }
    return (hasManySiblings(img, minX, minY, width, height) && hasManySiblings(img2, minX, minY, width, height))
      || (hasManySiblings(img, maxX, maxY, width, height) && hasManySiblings(img2, maxX, maxY, width, height));
  }

  private static boolean hasManySiblings(byte[] img, int x1, int y1, int width, int height) {
    int x0 = Math.max(x1 - 1, 0);
    int y0 = Math.max(y1 - 1, 0);
    int x2 = Math.min(x1 + 1, width - 1);
    int y2 = Math.min(y1 + 1, height - 1);
    int pos = (y1 * width + x1) * 4;
    int zeroes = x1 == x0 || x1 == x2 || y1 == y0 || y1 == y2 ? 1 : 0;
    for (int x = x0; x <= x2; x++) {
      for (int y = y0; y <= y2; y++) {
        if (x == x1 && y == y1) {
          continue;
        }
        int pos2 = (y * width + x) * 4;
        if (img[pos] == img[pos2] && img[pos + 1] == img[pos2 + 1] && img[pos + 2] == img[pos2 + 2] && img[pos + 3] == img[pos2 + 3]) {
          zeroes++;
        }
        if (zeroes > 2) {
          return true;
        }
      }
    }
    return false;
  }

  private static double colorDelta(byte[] img1, byte[] img2, int k, int m, boolean yOnly) {
    double r1 = img1[k] & 0xff;
    double g1 = img1[k + 1] & 0xff;
    double b1 = img1[k + 2] & 0xff;
    double a1 = img1[k + 3] & 0xff;
    double r2 = img2[m] & 0xff;
    double g2 = img2[m + 1] & 0xff;
    double b2 = img2[m + 2] & 0xff;
    double a2 = img2[m + 3] & 0xff;
    if (a1 == a2 && r1 == r2 && g1 == g2 && b1 == b2) {
      return 0;
    }
    if (a1 < 255) {
      a1 /= 255;
      r1 = blend(r1, a1);
      g1 = blend(g1, a1);
      b1 = blend(b1, a1);
    }
    if (a2 < 255) {
      a2 /= 255;
      r2 = blend(r2, a2);
      g2 = blend(g2, a2);
      b2 = blend(b2, a2);
    }
    double y1 = rgb2y(r1, g1, b1);
    double y2 = rgb2y(r2, g2, b2);
    double y = y1 - y2;
    if (yOnly) {
      return y;
    }
    double i = rgb2i(r1, g1, b1) - rgb2i(r2, g2, b2);
    double q = rgb2q(r1, g1, b1) - rgb2q(r2, g2, b2);
    double delta = 0.5053 * y * y + 0.299 * i * i + 0.1957 * q * q;
    return y1 > y2 ? -delta : delta;
  }

  private static double rgb2y(double r, double g, double b) {
    return r * 0.29889531 + g * 0.58662247 + b * 0.11448223;
  }

  private static double rgb2i(double r, double g, double b) {
    return r * 0.59597799 - g * 0.27417610 - b * 0.32180189;
  }

  private static double rgb2q(double r, double g, double b) {
    return r * 0.21147017 - g * 0.52261711 + b * 0.31114694;
  }

  private static double blend(double c, double a) {
    return 255 + (c - 255) * a;
  }
}
