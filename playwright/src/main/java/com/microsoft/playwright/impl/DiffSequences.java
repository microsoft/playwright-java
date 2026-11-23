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

// Port of @jest/diff-sequences.
// java: the original is MIT licensed, Copyright (c) Meta Platforms, Inc. and affiliates.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.PlaywrightException;

class DiffSequences {
  // java: the types of the input and output callbacks.
  interface IsCommon {
    boolean isCommon(int aIndex, int bIndex);
  }

  interface FoundSubsequence {
    void foundSubsequence(int nCommon, int aCommon, int bCommon);
  }

  // java: the type of an item of the callbacks array.
  private static class Callbacks {
    final FoundSubsequence foundSubsequence;
    final IsCommon isCommon;

    Callbacks(FoundSubsequence foundSubsequence, IsCommon isCommon) {
      this.foundSubsequence = foundSubsequence;
      this.isCommon = isCommon;
    }
  }

  // java: the type of the division object.
  private static class Division {
    int aCommonFollowing = NOT_YET_SET;
    int aCommonPreceding = NOT_YET_SET;
    int aEndPreceding = NOT_YET_SET;
    int aStartFollowing = NOT_YET_SET;
    int bCommonFollowing = NOT_YET_SET;
    int bCommonPreceding = NOT_YET_SET;
    int bEndPreceding = NOT_YET_SET;
    int bStartFollowing = NOT_YET_SET;
    int nChangeFollowing = NOT_YET_SET;
    int nChangePreceding = NOT_YET_SET;
    int nCommonFollowing = NOT_YET_SET;
    int nCommonPreceding = NOT_YET_SET;
  }

  private static final String pkg = "@jest/diff-sequences";
  private static final int NOT_YET_SET = 0;

  private static int countCommonItemsF(int aIndex, int aEnd, int bIndex, int bEnd, IsCommon isCommon) {
    int nCommon = 0;
    while (aIndex < aEnd && bIndex < bEnd && isCommon.isCommon(aIndex, bIndex)) {
      aIndex += 1;
      bIndex += 1;
      nCommon += 1;
    }
    return nCommon;
  }

  private static int countCommonItemsR(int aStart, int aIndex, int bStart, int bIndex, IsCommon isCommon) {
    int nCommon = 0;
    while (aStart <= aIndex && bStart <= bIndex && isCommon.isCommon(aIndex, bIndex)) {
      aIndex -= 1;
      bIndex -= 1;
      nCommon += 1;
    }
    return nCommon;
  }

  private static int extendPathsF(int d, int aEnd, int bEnd, int bF, IsCommon isCommon, int[] aIndexesF, int iMaxF) {
    int iF = 0;
    int kF = -d;
    int aFirst = aIndexesF[iF];
    int aIndexPrev1 = aFirst;
    aIndexesF[iF] += countCommonItemsF(aFirst + 1, aEnd, bF + aFirst - kF + 1, bEnd, isCommon);

    int nF = Math.min(d, iMaxF);

    for (iF += 1, kF += 2; iF <= nF; iF += 1, kF += 2) {
      if (iF != d && aIndexPrev1 < aIndexesF[iF]) {
        aFirst = aIndexesF[iF];
      } else {
        aFirst = aIndexPrev1 + 1;

        if (aEnd <= aFirst) {
          return iF - 1;
        }
      }

      aIndexPrev1 = aIndexesF[iF];
      aIndexesF[iF] = aFirst + countCommonItemsF(aFirst + 1, aEnd, bF + aFirst - kF + 1, bEnd, isCommon);
    }
    return iMaxF;
  }

  private static int extendPathsR(int d, int aStart, int bStart, int bR, IsCommon isCommon, int[] aIndexesR, int iMaxR) {
    int iR = 0;
    int kR = d;
    int aFirst = aIndexesR[iR];
    int aIndexPrev1 = aFirst;
    aIndexesR[iR] -= countCommonItemsR(aStart, aFirst - 1, bStart, bR + aFirst - kR - 1, isCommon);

    int nR = Math.min(d, iMaxR);

    for (iR += 1, kR -= 2; iR <= nR; iR += 1, kR -= 2) {
      if (iR != d && aIndexesR[iR] < aIndexPrev1) {
        aFirst = aIndexesR[iR];
      } else {
        aFirst = aIndexPrev1 - 1;

        if (aFirst < aStart) {
          return iR - 1;
        }
      }

      aIndexPrev1 = aIndexesR[iR];
      aIndexesR[iR] = aFirst - countCommonItemsR(aStart, aFirst - 1, bStart, bR + aFirst - kR - 1, isCommon);
    }
    return iMaxR;
  }

  private static boolean extendOverlappablePathsF(int d, int aStart, int aEnd, int bStart, int bEnd, IsCommon isCommon, int[] aIndexesF, int iMaxF, int[] aIndexesR, int iMaxR, Division division) {
    int bF = bStart - aStart;
    int aLength = aEnd - aStart;
    int bLength = bEnd - bStart;
    int baDeltaLength = bLength - aLength;

    int kMinOverlapF = -baDeltaLength - (d - 1);
    int kMaxOverlapF = -baDeltaLength + (d - 1);

    int aIndexPrev1 = NOT_YET_SET;

    int nF = Math.min(d, iMaxF);

    for (int iF = 0, kF = -d; iF <= nF; iF += 1, kF += 2) {
      boolean insert = iF == 0 || iF != d && aIndexPrev1 < aIndexesF[iF];
      int aLastPrev = insert ? aIndexesF[iF] : aIndexPrev1;
      int aFirst = insert ? aLastPrev
        : aLastPrev + 1;

      int bFirst = bF + aFirst - kF;
      int nCommonF = countCommonItemsF(aFirst + 1, aEnd, bFirst + 1, bEnd, isCommon);
      int aLast = aFirst + nCommonF;
      aIndexPrev1 = aIndexesF[iF];
      aIndexesF[iF] = aLast;
      if (kMinOverlapF <= kF && kF <= kMaxOverlapF) {
        int iR = (d - 1 - (kF + baDeltaLength)) / 2;

        if (iR <= iMaxR && aIndexesR[iR] - 1 <= aLast) {
          int bLastPrev = bF + aLastPrev - (insert ? kF + 1 : kF - 1);

          int nCommonR = countCommonItemsR(aStart, aLastPrev, bStart, bLastPrev, isCommon);
          int aIndexPrevFirst = aLastPrev - nCommonR;
          int bIndexPrevFirst = bLastPrev - nCommonR;
          int aEndPreceding = aIndexPrevFirst + 1;
          int bEndPreceding = bIndexPrevFirst + 1;
          division.nChangePreceding = d - 1;
          if (d - 1 == aEndPreceding + bEndPreceding - aStart - bStart) {
            division.aEndPreceding = aStart;
            division.bEndPreceding = bStart;
          } else {
            division.aEndPreceding = aEndPreceding;
            division.bEndPreceding = bEndPreceding;
          }
          division.nCommonPreceding = nCommonR;
          if (nCommonR != 0) {
            division.aCommonPreceding = aEndPreceding;
            division.bCommonPreceding = bEndPreceding;
          }
          division.nCommonFollowing = nCommonF;
          if (nCommonF != 0) {
            division.aCommonFollowing = aFirst + 1;
            division.bCommonFollowing = bFirst + 1;
          }
          int aStartFollowing = aLast + 1;
          int bStartFollowing = bFirst + nCommonF + 1;
          division.nChangeFollowing = d - 1;
          if (d - 1 == aEnd + bEnd - aStartFollowing - bStartFollowing) {
            division.aStartFollowing = aEnd;
            division.bStartFollowing = bEnd;
          } else {
            division.aStartFollowing = aStartFollowing;
            division.bStartFollowing = bStartFollowing;
          }
          return true;
        }
      }
    }
    return false;
  }

  private static boolean extendOverlappablePathsR(int d, int aStart, int aEnd, int bStart, int bEnd, IsCommon isCommon, int[] aIndexesF, int iMaxF, int[] aIndexesR, int iMaxR, Division division) {
    int bR = bEnd - aEnd;
    int aLength = aEnd - aStart;
    int bLength = bEnd - bStart;
    int baDeltaLength = bLength - aLength;

    int kMinOverlapR = baDeltaLength - d;
    int kMaxOverlapR = baDeltaLength + d;

    int aIndexPrev1 = NOT_YET_SET;

    int nR = Math.min(d, iMaxR);

    for (int iR = 0, kR = d; iR <= nR; iR += 1, kR -= 2) {
      boolean insert = iR == 0 || iR != d && aIndexesR[iR] < aIndexPrev1;
      int aLastPrev = insert ? aIndexesR[iR] : aIndexPrev1;
      int aFirst = insert ? aLastPrev
        : aLastPrev - 1;

      int bFirst = bR + aFirst - kR;
      int nCommonR = countCommonItemsR(aStart, aFirst - 1, bStart, bFirst - 1, isCommon);
      int aLast = aFirst - nCommonR;
      aIndexPrev1 = aIndexesR[iR];
      aIndexesR[iR] = aLast;
      if (kMinOverlapR <= kR && kR <= kMaxOverlapR) {
        int iF = (d + (kR - baDeltaLength)) / 2;

        if (iF <= iMaxF && aLast - 1 <= aIndexesF[iF]) {
          int bLast = bFirst - nCommonR;
          division.nChangePreceding = d;
          if (d == aLast + bLast - aStart - bStart) {
            division.aEndPreceding = aStart;
            division.bEndPreceding = bStart;
          } else {
            division.aEndPreceding = aLast;
            division.bEndPreceding = bLast;
          }
          division.nCommonPreceding = nCommonR;
          if (nCommonR != 0) {
            division.aCommonPreceding = aLast;
            division.bCommonPreceding = bLast;
          }
          division.nChangeFollowing = d - 1;
          if (d == 1) {
            division.nCommonFollowing = 0;
            division.aStartFollowing = aEnd;
            division.bStartFollowing = bEnd;
          } else {
            int bLastPrev = bR + aLastPrev - (insert ? kR - 1 : kR + 1);

            int nCommonF = countCommonItemsF(aLastPrev, aEnd, bLastPrev, bEnd, isCommon);
            division.nCommonFollowing = nCommonF;
            if (nCommonF != 0) {
              division.aCommonFollowing = aLastPrev;
              division.bCommonFollowing = bLastPrev;
            }
            int aStartFollowing = aLastPrev + nCommonF;
            int bStartFollowing = bLastPrev + nCommonF;

            if (d - 1 == aEnd + bEnd - aStartFollowing - bStartFollowing) {
              division.aStartFollowing = aEnd;
              division.bStartFollowing = bEnd;
            } else {
              division.aStartFollowing = aStartFollowing;
              division.bStartFollowing = bStartFollowing;
            }
          }
          return true;
        }
      }
    }
    return false;
  }

  private static void divide(int nChange, int aStart, int aEnd, int bStart, int bEnd, IsCommon isCommon, int[] aIndexesF, int[] aIndexesR, Division division) {
    int bF = bStart - aStart;
    int bR = bEnd - aEnd;
    int aLength = aEnd - aStart;
    int bLength = bEnd - bStart;

    int baDeltaLength = bLength - aLength;

    int iMaxF = aLength;
    int iMaxR = aLength;

    aIndexesF[0] = aStart - 1;
    aIndexesR[0] = aEnd;

    if (baDeltaLength % 2 == 0) {
      int dMin = (nChange != 0 ? nChange : baDeltaLength) / 2;
      int dMax = (aLength + bLength) / 2;
      for (int d = 1; d <= dMax; d += 1) {
        iMaxF = extendPathsF(d, aEnd, bEnd, bF, isCommon, aIndexesF, iMaxF);
        if (d < dMin) {
          iMaxR = extendPathsR(d, aStart, bStart, bR, isCommon, aIndexesR, iMaxR);
        } else if (
          extendOverlappablePathsR(d, aStart, aEnd, bStart, bEnd, isCommon, aIndexesF, iMaxF, aIndexesR, iMaxR, division)) {
          return;
        }
      }
    } else {
      int dMin = ((nChange != 0 ? nChange : baDeltaLength) + 1) / 2;
      int dMax = (aLength + bLength + 1) / 2;

      int d = 1;
      iMaxF = extendPathsF(d, aEnd, bEnd, bF, isCommon, aIndexesF, iMaxF);
      for (d += 1; d <= dMax; d += 1) {
        iMaxR = extendPathsR(d - 1, aStart, bStart, bR, isCommon, aIndexesR, iMaxR);
        if (d < dMin) {
          iMaxF = extendPathsF(d, aEnd, bEnd, bF, isCommon, aIndexesF, iMaxF);
        } else if (
          extendOverlappablePathsF(d, aStart, aEnd, bStart, bEnd, isCommon, aIndexesF, iMaxF, aIndexesR, iMaxR, division)) {
          return;
        }
      }
    }

    throw new PlaywrightException(pkg + ": no overlap aStart=" + aStart + " aEnd=" + aEnd + " bStart=" + bStart + " bEnd=" + bEnd);
  }

  private static void findSubsequences(int nChange, int aStart, int aEnd, int bStart, int bEnd, boolean transposed, Callbacks[] callbacks, int[] aIndexesF, int[] aIndexesR, Division division) {
    if (bEnd - bStart < aEnd - aStart) {
      transposed = !transposed;
      // java: the callbacks array has two slots, the second one is null until it is set.
      if (transposed && callbacks[1] == null) {
        FoundSubsequence foundSubsequence = callbacks[0].foundSubsequence;
        IsCommon isCommon = callbacks[0].isCommon;
        callbacks[1] = new Callbacks(
          (nCommon, bCommon, aCommon) -> {
            foundSubsequence.foundSubsequence(nCommon, aCommon, bCommon);
          },
          (bIndex, aIndex) -> isCommon.isCommon(aIndex, bIndex));
      }
      int tStart = aStart;
      int tEnd = aEnd;
      aStart = bStart;
      aEnd = bEnd;
      bStart = tStart;
      bEnd = tEnd;
    }
    FoundSubsequence foundSubsequence = callbacks[transposed ? 1 : 0].foundSubsequence;
    IsCommon isCommon = callbacks[transposed ? 1 : 0].isCommon;

    divide(nChange, aStart, aEnd, bStart, bEnd, isCommon, aIndexesF, aIndexesR, division);
    int nChangePreceding = division.nChangePreceding;
    int aEndPreceding = division.aEndPreceding;
    int bEndPreceding = division.bEndPreceding;
    int nCommonPreceding = division.nCommonPreceding;
    int aCommonPreceding = division.aCommonPreceding;
    int bCommonPreceding = division.bCommonPreceding;
    int nCommonFollowing = division.nCommonFollowing;
    int aCommonFollowing = division.aCommonFollowing;
    int bCommonFollowing = division.bCommonFollowing;
    int nChangeFollowing = division.nChangeFollowing;
    int aStartFollowing = division.aStartFollowing;
    int bStartFollowing = division.bStartFollowing;

    if (aStart < aEndPreceding && bStart < bEndPreceding) {
      findSubsequences(nChangePreceding, aStart, aEndPreceding, bStart, bEndPreceding, transposed, callbacks, aIndexesF, aIndexesR, division);
    }

    if (nCommonPreceding != 0) {
      foundSubsequence.foundSubsequence(nCommonPreceding, aCommonPreceding, bCommonPreceding);
    }
    if (nCommonFollowing != 0) {
      foundSubsequence.foundSubsequence(nCommonFollowing, aCommonFollowing, bCommonFollowing);
    }

    if (aStartFollowing < aEnd && bStartFollowing < bEnd) {
      findSubsequences(nChangeFollowing, aStartFollowing, aEnd, bStartFollowing, bEnd, transposed, callbacks, aIndexesF, aIndexesR, division);
    }
  }

  // java: validateLength and validateCallback are not ported, the Java types make these checks.

  static void diffSequence(int aLength, int bLength, IsCommon isCommon, FoundSubsequence foundSubsequence) {
    int nCommonF = countCommonItemsF(0, aLength, 0, bLength, isCommon);
    if (nCommonF != 0) {
      foundSubsequence.foundSubsequence(nCommonF, 0, 0);
    }

    if (aLength != nCommonF || bLength != nCommonF) {
      int aStart = nCommonF;
      int bStart = nCommonF;

      int nCommonR = countCommonItemsR(aStart, aLength - 1, bStart, bLength - 1, isCommon);

      int aEnd = aLength - nCommonR;
      int bEnd = bLength - nCommonR;

      int nCommonFR = nCommonF + nCommonR;
      if (aLength != nCommonFR && bLength != nCommonFR) {
        int nChange = 0;
        boolean transposed = false;
        Callbacks[] callbacks = new Callbacks[] {new Callbacks(foundSubsequence, isCommon), null};

        // java: a JavaScript array grows on assignment. The paths use the indexes from zero
        // to the length of the shorter sequence, the Java arrays have that size from the start.
        int[] aIndexesF = new int[Math.min(aEnd - aStart, bEnd - bStart) + 1];
        int[] aIndexesR = new int[Math.min(aEnd - aStart, bEnd - bStart) + 1];

        Division division = new Division();

        findSubsequences(nChange, aStart, aEnd, bStart, bEnd, transposed, callbacks, aIndexesF, aIndexesR, division);
      }
      if (nCommonR != 0) {
        foundSubsequence.foundSubsequence(nCommonR, aEnd, bEnd);
      }
    }
  }
}
