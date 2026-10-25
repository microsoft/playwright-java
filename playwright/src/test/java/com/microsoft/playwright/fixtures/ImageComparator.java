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

  // The ssim-cie94 comparator of the upstream tests: comparePNGs() in tests/config/comparator.ts
  // over compare() of packages/utils/image_tools (compare.ts, stats.ts, colorUtils.ts, imageChannel.ts).

  private static final int SSIM_WINDOW_RADIUS = 15;
  private static final int VARIANCE_WINDOW_RADIUS = 1;
  // Image channel has a 8-bit depth.
  private static final int DYNAMIC_RANGE = 255;

  /** Null when the images match within {@code maxDiffPixels}, else the message of comparePNGs(). */
  public static String comparePNGs(byte[] actual, byte[] expected, int maxDiffPixels) {
    Image actualImage = decode(actual);
    Image expectedImage = decode(expected);
    int width = Math.max(expectedImage.width, actualImage.width);
    int height = Math.max(expectedImage.height, actualImage.height);
    String sizesMismatchError = "";
    if (expectedImage.width != actualImage.width || expectedImage.height != actualImage.height) {
      sizesMismatchError = "Expected an image " + expectedImage.width + "px by " + expectedImage.height + "px, received " + actualImage.width + "px by " + actualImage.height + "px. ";
      actualImage = pad(actualImage, width, height);
      expectedImage = pad(expectedImage, width, height);
    }
    int count = compareImages(actualImage.data, expectedImage.data, width, height, 1.0);
    double ratio = Math.ceil(count / (double) (expectedImage.width * expectedImage.height) * 100) / 100;
    String pixelsMismatchError = count > maxDiffPixels ? count + " pixels (ratio " + String.format(java.util.Locale.ROOT, "%.2f", ratio) + " of all image pixels) are different." : "";
    if (sizesMismatchError.isEmpty() && pixelsMismatchError.isEmpty()) {
      return null;
    }
    return sizesMismatchError + pixelsMismatchError;
  }

  /** The number of different pixels between two encoded images of the same size: compare() of image_tools. */
  public static int countDifferentPixels(byte[] encodedA, byte[] encodedB) {
    Image a = decode(encodedA);
    Image b = decode(encodedB);
    if (a.width != b.width || a.height != b.height) {
      throw new IllegalArgumentException("Image sizes do not match");
    }
    return compareImages(a.data, b.data, a.width, a.height, 1.0);
  }

  private static final class Channel {
    final int width;
    final int height;
    final int[] data;

    Channel(int width, int height, int[] data) {
      this.width = width;
      this.height = height;
      this.data = data;
    }

    int boundX(int x) {
      return Math.min(Math.max(x, 0), width - 1);
    }

    int boundY(int y) {
      return Math.min(Math.max(y, 0), height - 1);
    }
  }

  // ImageChannel.intoRGB(): a pixel that is not opaque is blended with white (a Uint8Array keeps the
  // integer part), the image is surrounded by a checkerboard padding.
  private static Channel[] intoRGB(int width, int height, byte[] data, int paddingSize, int[] paddingColorOdd, int[] paddingColorEven) {
    int newWidth = width + 2 * paddingSize;
    int newHeight = height + 2 * paddingSize;
    int[][] channels = new int[3][newWidth * newHeight];
    for (int y = 0; y < newHeight; y++) {
      for (int x = 0; x < newWidth; x++) {
        int index = y * newWidth + x;
        if (y >= paddingSize && y < newHeight - paddingSize && x >= paddingSize && x < newWidth - paddingSize) {
          int offset = ((y - paddingSize) * width + (x - paddingSize)) * 4;
          int alpha = data[offset + 3] & 0xff;
          for (int c = 0; c < 3; c++) {
            int value = data[offset + c] & 0xff;
            if (alpha != 255) {
              value = (int) blend(value, alpha / 255.0);
            }
            channels[c][index] = value;
          }
        } else {
          int[] color = (y + x) % 2 == 0 ? paddingColorEven : paddingColorOdd;
          for (int c = 0; c < 3; c++) {
            channels[c][index] = color[c];
          }
        }
      }
    }
    return new Channel[]{
      new Channel(newWidth, newHeight, channels[0]),
      new Channel(newWidth, newHeight, channels[1]),
      new Channel(newWidth, newHeight, channels[2])};
  }

  // NOTE: this is the exact integer formula from SSIM.js.
  static int rgb2gray(int r, int g, int b) {
    return (77 * r + 150 * g + 29 * b + 128) >> 8;
  }

  // Perceived color difference defined by CIE94, 1.0 is a "just-noticeable difference".
  // See https://en.wikipedia.org/wiki/Color_difference#CIE94
  private static double colorDeltaE94(int r1, int g1, int b1, int r2, int g2, int b2) {
    double[] lab1 = xyz2lab(srgb2xyz(r1, g1, b1));
    double[] lab2 = xyz2lab(srgb2xyz(r2, g2, b2));
    double deltaL = lab1[0] - lab2[0];
    double deltaA = lab1[1] - lab2[1];
    double deltaB = lab1[2] - lab2[2];
    double c1 = Math.sqrt(lab1[1] * lab1[1] + lab1[2] * lab1[2]);
    double c2 = Math.sqrt(lab2[1] * lab2[1] + lab2[2] * lab2[2]);
    double deltaC = c1 - c2;
    double deltaH = deltaA * deltaA + deltaB * deltaB - deltaC * deltaC;
    deltaH = deltaH < 0 ? 0 : Math.sqrt(deltaH);
    // The k1, k2, kL, kC, kH values for "graphic arts" applications.
    double k1 = 0.045;
    double k2 = 0.015;
    double kL = 1;
    double kC = 1;
    double kH = 1;
    double sC = 1.0 + k1 * c1;
    double sH = 1.0 + k2 * c1;
    double sL = 1;
    return Math.sqrt(Math.pow(deltaL / sL / kL, 2) + Math.pow(deltaC / sC / kC, 2) + Math.pow(deltaH / sH / kH, 2));
  }

  // sRGB -> 1-normalized XYZ (i.e. Y in [0, 1]) with D65 illuminant
  // See https://en.wikipedia.org/wiki/SRGB#From_sRGB_to_CIE_XYZ
  private static double[] srgb2xyz(int red, int green, int blue) {
    double r = red / 255.0;
    double g = green / 255.0;
    double b = blue / 255.0;
    r = r > 0.04045 ? Math.pow((r + 0.055) / 1.055, 2.4) : r / 12.92;
    g = g > 0.04045 ? Math.pow((g + 0.055) / 1.055, 2.4) : g / 12.92;
    b = b > 0.04045 ? Math.pow((b + 0.055) / 1.055, 2.4) : b / 12.92;
    return new double[]{
      r * 0.4124 + g * 0.3576 + b * 0.1805,
      r * 0.2126 + g * 0.7152 + b * 0.0722,
      r * 0.0193 + g * 0.1192 + b * 0.9505};
  }

  private static final double SIGMA_POW2 = 6.0 * 6 / 29 / 29;
  private static final double SIGMA_POW3 = 6.0 * 6 * 6 / 29 / 29 / 29;

  // 1-normalized CIE XYZ with D65 to L*a*b*
  // See https://en.wikipedia.org/wiki/CIELAB_color_space#From_CIEXYZ_to_CIELAB
  private static double[] xyz2lab(double[] xyz) {
    double x = xyz[0] / 0.950489;
    double y = xyz[1];
    double z = xyz[2] / 1.088840;
    double fx = x > SIGMA_POW3 ? Math.pow(x, 1.0 / 3) : x / 3 / SIGMA_POW2 + 4.0 / 29;
    double fy = y > SIGMA_POW3 ? Math.pow(y, 1.0 / 3) : y / 3 / SIGMA_POW2 + 4.0 / 29;
    double fz = z > SIGMA_POW3 ? Math.pow(z, 1.0 / 3) : z / 3 / SIGMA_POW2 + 4.0 / 29;
    return new double[]{116 * fy - 16, 500 * (fx - fy), 200 * (fy - fz)};
  }

  // FastStats of stats.ts: partial sums of the rectangles from (0, 0).
  private static final class FastStats {
    final Channel c1;
    final Channel c2;
    final long[] partialSumC1;
    final long[] partialSumC2;
    final long[] partialSumSq1;
    final long[] partialSumSq2;
    final long[] partialSumMult;

    FastStats(Channel c1, Channel c2) {
      this.c1 = c1;
      this.c2 = c2;
      int n = c1.width * c1.height;
      long[] s1 = new long[n];
      long[] s2 = new long[n];
      long[] sq1 = new long[n];
      long[] sq2 = new long[n];
      long[] mult = new long[n];
      for (int i = 0; i < n; i++) {
        long v1 = c1.data[i];
        long v2 = c2.data[i];
        s1[i] = v1;
        s2[i] = v2;
        sq1[i] = v1 * v1;
        sq2[i] = v2 * v2;
        mult[i] = v1 * v2;
      }
      recalc(s1);
      recalc(s2);
      recalc(sq1);
      recalc(sq2);
      recalc(mult);
      partialSumC1 = s1;
      partialSumC2 = s2;
      partialSumSq1 = sq1;
      partialSumSq2 = sq2;
      partialSumMult = mult;
    }

    private void recalc(long[] mem) {
      int width = c1.width;
      int height = c1.height;
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          long v = mem[y * width + x];
          if (x > 0) {
            v += mem[y * width + x - 1];
          }
          if (y > 0) {
            v += mem[(y - 1) * width + x];
          }
          if (x > 0 && y > 0) {
            v -= mem[(y - 1) * width + x - 1];
          }
          mem[y * width + x] = v;
        }
      }
    }

    private long sum(long[] partialSum, int x1, int y1, int x2, int y2) {
      int width = c1.width;
      long result = partialSum[y2 * width + x2];
      if (y1 > 0) {
        result -= partialSum[(y1 - 1) * width + x2];
      }
      if (x1 > 0) {
        result -= partialSum[y2 * width + x1 - 1];
      }
      if (x1 > 0 && y1 > 0) {
        result += partialSum[(y1 - 1) * width + x1 - 1];
      }
      return result;
    }

    double meanC1(int x1, int y1, int x2, int y2) {
      double n = (y2 - y1 + 1) * (x2 - x1 + 1);
      return sum(partialSumC1, x1, y1, x2, y2) / n;
    }

    double meanC2(int x1, int y1, int x2, int y2) {
      double n = (y2 - y1 + 1) * (x2 - x1 + 1);
      return sum(partialSumC2, x1, y1, x2, y2) / n;
    }

    double varianceC1(int x1, int y1, int x2, int y2) {
      double n = (y2 - y1 + 1) * (x2 - x1 + 1);
      double s = sum(partialSumC1, x1, y1, x2, y2);
      return (sum(partialSumSq1, x1, y1, x2, y2) - s * s / n) / n;
    }

    double varianceC2(int x1, int y1, int x2, int y2) {
      double n = (y2 - y1 + 1) * (x2 - x1 + 1);
      double s = sum(partialSumC2, x1, y1, x2, y2);
      return (sum(partialSumSq2, x1, y1, x2, y2) - s * s / n) / n;
    }

    double covariance(int x1, int y1, int x2, int y2) {
      double n = (y2 - y1 + 1) * (x2 - x1 + 1);
      double s1 = sum(partialSumC1, x1, y1, x2, y2);
      double s2 = sum(partialSumC2, x1, y1, x2, y2);
      return (sum(partialSumMult, x1, y1, x2, y2) - s1 * s2 / n) / n;
    }
  }

  private static double ssim(FastStats stats, int x1, int y1, int x2, int y2) {
    double mean1 = stats.meanC1(x1, y1, x2, y2);
    double mean2 = stats.meanC2(x1, y1, x2, y2);
    double var1 = stats.varianceC1(x1, y1, x2, y2);
    double var2 = stats.varianceC2(x1, y1, x2, y2);
    double cov = stats.covariance(x1, y1, x2, y2);
    double c1 = Math.pow(0.01 * DYNAMIC_RANGE, 2);
    double c2 = Math.pow(0.03 * DYNAMIC_RANGE, 2);
    return ((2 * mean1 * mean2 + c1) * (2 * cov + c2)) / (mean1 * mean1 + mean2 * mean2 + c1) / (var1 + var2 + c2);
  }

  // compare() of compare.ts, without the diff image. The images are RGBA of one size.
  private static int compareImages(byte[] actual, byte[] expected, int width, int height, double maxColorDeltaE94) {
    if (java.util.Arrays.equals(actual, expected)) {
      return 0;
    }
    int paddingSize = Math.max(VARIANCE_WINDOW_RADIUS, SSIM_WINDOW_RADIUS);
    int[] paddingColorEven = {255, 0, 255};
    int[] paddingColorOdd = {0, 255, 0};
    Channel[] rgb1 = intoRGB(width, height, expected, paddingSize, paddingColorOdd, paddingColorEven);
    Channel[] rgb2 = intoRGB(width, height, actual, paddingSize, paddingColorOdd, paddingColorEven);
    Channel r1 = rgb1[0];
    Channel g1 = rgb1[1];
    Channel b1 = rgb1[2];
    Channel r2 = rgb2[0];
    Channel g2 = rgb2[1];
    Channel b2 = rgb2[2];
    FastStats fastR = null;
    FastStats fastG = null;
    FastStats fastB = null;
    int diffCount = 0;
    for (int y = paddingSize; y < r1.height - paddingSize; ++y) {
      for (int x = paddingSize; x < r1.width - paddingSize; ++x) {
        // Fast-path: equal pixels.
        int index = y * r1.width + x;
        if (r1.data[index] == r2.data[index] && g1.data[index] == g2.data[index] && b1.data[index] == b2.data[index]) {
          continue;
        }
        // Compare pixel colors using the dE94 color difference formulae.
        // The dE94 is normalized so that the value of 1.0 is the "just-noticeable-difference".
        double delta = colorDeltaE94(r1.data[index], g1.data[index], b1.data[index], r2.data[index], g2.data[index], b2.data[index]);
        if (delta <= maxColorDeltaE94) {
          continue;
        }
        if (fastR == null || fastG == null || fastB == null) {
          fastR = new FastStats(r1, r2);
          fastG = new FastStats(g1, g2);
          fastB = new FastStats(b1, b2);
        }
        int varX1 = r1.boundX(x - VARIANCE_WINDOW_RADIUS);
        int varY1 = r1.boundY(y - VARIANCE_WINDOW_RADIUS);
        int varX2 = r1.boundX(x + VARIANCE_WINDOW_RADIUS);
        int varY2 = r1.boundY(y + VARIANCE_WINDOW_RADIUS);
        double var1 = fastR.varianceC1(varX1, varY1, varX2, varY2) + fastG.varianceC1(varX1, varY1, varX2, varY2) + fastB.varianceC1(varX1, varY1, varX2, varY2);
        double var2 = fastR.varianceC2(varX1, varY1, varX2, varY2) + fastG.varianceC2(varX1, varY1, varX2, varY2) + fastB.varianceC2(varX1, varY1, varX2, varY2);
        // If this pixel is a part of a flood fill of a 3x3 square of either of the images, then it cannot be
        // anti-aliasing pixel so it must be a pixel difference.
        if (var1 == 0 || var2 == 0) {
          diffCount++;
          continue;
        }
        int ssimX1 = r1.boundX(x - SSIM_WINDOW_RADIUS);
        int ssimY1 = r1.boundY(y - SSIM_WINDOW_RADIUS);
        int ssimX2 = r1.boundX(x + SSIM_WINDOW_RADIUS);
        int ssimY2 = r1.boundY(y + SSIM_WINDOW_RADIUS);
        double ssimRGB = (ssim(fastR, ssimX1, ssimY1, ssimX2, ssimY2) + ssim(fastG, ssimX1, ssimY1, ssimX2, ssimY2) + ssim(fastB, ssimX1, ssimY1, ssimX2, ssimY2)) / 3.0;
        boolean isAntialiased = ssimRGB >= 0.99;
        if (!isAntialiased) {
          diffCount++;
        }
      }
    }
    return diffCount;
  }
}
