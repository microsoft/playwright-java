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

// Port of jest-diff.
// java: the original is MIT licensed, Copyright (c) Meta Platforms, Inc. and affiliates.
// java: cleanupSemantic.ts is from Diff Match and Patch, Copyright 2018 The diff-match-patch Authors,
// https://github.com/google/diff-match-patch, licensed under the Apache License, Version 2.0.
// java: the upstream files are in one class, in the order of the bundle: cleanupSemantic.ts,
// constants.ts, diffLines.ts, diffStrings.ts, escapeControlCharacters.ts, getAlignedDiffs.ts,
// joinAlignedDiffs.ts, normalizeDiffOptions.ts, printDiffs.ts, index.ts.
package com.microsoft.playwright.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.microsoft.playwright.impl.DiffSequences.diffSequence;
import static com.microsoft.playwright.impl.JestGetType.getType;

class JestDiff {
  // java: no ANSI colors, the color functions return the text as is.
  static class Chalk {
    static String green(String text) {
      return text;
    }

    static String red(String text) {
      return text;
    }

    static String inverse(String text) {
      return text;
    }

    static String bold(String text) {
      return text;
    }

    static String dim(String text) {
      return text;
    }

    static String yellow(String text) {
      return text;
    }

    static String bgYellow(String text) {
      return text;
    }
  }

  // java: the type of the options object. A null field is an option that is not set.
  static class DiffOptions {
    String aAnnotation;
    Function<String, String> aColor;
    String aIndicator;
    String bAnnotation;
    Function<String, String> bColor;
    String bIndicator;
    Function<String, String> changeColor;
    Function<String, String> changeLineTrailingSpaceColor;
    Function<String, String> commonColor;
    String commonIndicator;
    Function<String, String> commonLineTrailingSpaceColor;
    Comparator<String> compareKeys;
    Integer contextLines;
    String emptyFirstOrLastLinePlaceholder;
    Boolean expand;
    Boolean includeChangeCounts;
    Boolean omitAnnotationLines;
    Function<String, String> patchColor;
  }

  // java: the type of the result of countChanges.
  private static class ChangeCounts {
    int a;
    int b;
  }

  // java: helpers with no counterpart upstream, the ports of String.prototype.substring and
  // String.prototype.charAt. They accept an index that is out of the string, as JavaScript does.
  private static String substring(String string, int start) {
    return substring(string, start, string.length());
  }

  private static String substring(String string, int start, int end) {
    start = Math.max(0, Math.min(start, string.length()));
    end = Math.max(0, Math.min(end, string.length()));
    return string.substring(Math.min(start, end), Math.max(start, end));
  }

  private static String charAt(String string, int index) {
    return index < 0 || index >= string.length() ? "" : string.substring(index, index + 1);
  }

  // java: helper with no counterpart upstream, the port of String.prototype.repeat.
  private static String repeat(String string, int count) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < count; i++) {
      result.append(string);
    }
    return result.toString();
  }

  // java: helper with no counterpart upstream, the port of String.prototype.split: it keeps
  // the empty strings at the end.
  private static List<String> split(String string, String separator) {
    return new ArrayList<>(Arrays.asList(string.split(Pattern.quote(separator), -1)));
  }

  // java: the whitespace characters of \s in a JavaScript regular expression. \s in Java
  // matches the ASCII whitespace only.
  static final String WHITESPACE = "[\\t\\n\\x0B\\f\\r \\u00a0\\u1680\\u2000-\\u200a\\u2028\\u2029\\u202f\\u205f\\u3000\\ufeff]";

  static final int DIFF_DELETE = -1;
  static final int DIFF_INSERT = 1;
  static final int DIFF_EQUAL = 0;

  // java: the items 0 and 1 of upstream are the fields op and text.
  static class Diff {
    int op;
    String text;

    Diff(int op, String text) {
      this.op = op;
      this.text = text;
    }
  }

  private static int diff_commonPrefix(String text1, String text2) {
    if (text1.isEmpty() || text2.isEmpty() || text1.charAt(0) != text2.charAt(0)) {
      return 0;
    }
    int pointermin = 0;
    int pointermax = Math.min(text1.length(), text2.length());
    int pointermid = pointermax;
    int pointerstart = 0;
    while (pointermin < pointermid) {
      if (substring(text1, pointerstart, pointermid).equals(substring(text2, pointerstart, pointermid))) {
        pointermin = pointermid;
        pointerstart = pointermin;
      } else {
        pointermax = pointermid;
      }
      pointermid = (pointermax - pointermin) / 2 + pointermin;
    }
    return pointermid;
  }

  private static int diff_commonSuffix(String text1, String text2) {
    if (text1.isEmpty() || text2.isEmpty() || text1.charAt(text1.length() - 1) != text2.charAt(text2.length() - 1)) {
      return 0;
    }
    int pointermin = 0;
    int pointermax = Math.min(text1.length(), text2.length());
    int pointermid = pointermax;
    int pointerend = 0;
    while (pointermin < pointermid) {
      if (substring(text1, text1.length() - pointermid, text1.length() - pointerend).equals(substring(text2, text2.length() - pointermid, text2.length() - pointerend))) {
        pointermin = pointermid;
        pointerend = pointermin;
      } else {
        pointermax = pointermid;
      }
      pointermid = (pointermax - pointermin) / 2 + pointermin;
    }
    return pointermid;
  }

  private static int diff_commonOverlap_(String text1, String text2) {
    int text1_length = text1.length();
    int text2_length = text2.length();
    if (text1_length == 0 || text2_length == 0) {
      return 0;
    }
    if (text1_length > text2_length) {
      text1 = substring(text1, text1_length - text2_length);
    } else if (text1_length < text2_length) {
      text2 = substring(text2, 0, text1_length);
    }
    int text_length = Math.min(text1_length, text2_length);
    if (text1.equals(text2)) {
      return text_length;
    }

    int best = 0;
    int length = 1;
    while (true) {
      String pattern = substring(text1, text_length - length);
      int found = text2.indexOf(pattern);
      if (found == -1) {
        return best;
      }
      length += found;
      if (found == 0 || substring(text1, text_length - length).equals(substring(text2, 0, length))) {
        best = length;
        length++;
      }
    }
  }

  static void diff_cleanupSemantic(List<Diff> diffs) {
    boolean changes = false;
    // java: the stack is a map, because upstream decreases equalitiesLength below zero and
    // then writes and reads the property -1 of the JavaScript array.
    Map<Integer, Integer> equalities = new HashMap<>();
    int equalitiesLength = 0;
    String lastEquality = null;
    int pointer = 0;
    int length_insertions1 = 0;
    int length_deletions1 = 0;
    int length_insertions2 = 0;
    int length_deletions2 = 0;
    while (pointer < diffs.size()) {
      if (diffs.get(pointer).op == DIFF_EQUAL) {
        equalities.put(equalitiesLength++, pointer);
        length_insertions1 = length_insertions2;
        length_deletions1 = length_deletions2;
        length_insertions2 = 0;
        length_deletions2 = 0;
        lastEquality = diffs.get(pointer).text;
      } else {
        if (diffs.get(pointer).op == DIFF_INSERT) {
          length_insertions2 += diffs.get(pointer).text.length();
        } else {
          length_deletions2 += diffs.get(pointer).text.length();
        }
        if (lastEquality != null && !lastEquality.isEmpty() && lastEquality.length() <= Math.max(length_insertions1, length_deletions1) && lastEquality.length() <= Math.max(length_insertions2, length_deletions2)) {
          diffs.add(equalities.get(equalitiesLength - 1), new Diff(DIFF_DELETE, lastEquality));
          diffs.get(equalities.get(equalitiesLength - 1) + 1).op = DIFF_INSERT;
          equalitiesLength--;
          equalitiesLength--;
          pointer = equalitiesLength > 0 ? equalities.get(equalitiesLength - 1) : -1;
          length_insertions1 = 0;
          length_deletions1 = 0;
          length_insertions2 = 0;
          length_deletions2 = 0;
          lastEquality = null;
          changes = true;
        }
      }
      pointer++;
    }

    if (changes) {
      diff_cleanupMerge(diffs);
    }
    diff_cleanupSemanticLossless(diffs);

    pointer = 1;
    while (pointer < diffs.size()) {
      if (diffs.get(pointer - 1).op == DIFF_DELETE && diffs.get(pointer).op == DIFF_INSERT) {
        String deletion = diffs.get(pointer - 1).text;
        String insertion = diffs.get(pointer).text;
        int overlap_length1 = diff_commonOverlap_(deletion, insertion);
        int overlap_length2 = diff_commonOverlap_(insertion, deletion);
        if (overlap_length1 >= overlap_length2) {
          if (overlap_length1 >= deletion.length() / 2.0 || overlap_length1 >= insertion.length() / 2.0) {
            diffs.add(pointer, new Diff(DIFF_EQUAL, substring(insertion, 0, overlap_length1)));
            diffs.get(pointer - 1).text = substring(deletion, 0, deletion.length() - overlap_length1);
            diffs.get(pointer + 1).text = substring(insertion, overlap_length1);
            pointer++;
          }
        } else {
          if (overlap_length2 >= deletion.length() / 2.0 || overlap_length2 >= insertion.length() / 2.0) {
            diffs.add(pointer, new Diff(DIFF_EQUAL, substring(deletion, 0, overlap_length2)));
            diffs.get(pointer - 1).op = DIFF_INSERT;
            diffs.get(pointer - 1).text = substring(insertion, 0, insertion.length() - overlap_length2);
            diffs.get(pointer + 1).op = DIFF_DELETE;
            diffs.get(pointer + 1).text = substring(deletion, overlap_length2);
            pointer++;
          }
        }
        pointer++;
      }
      pointer++;
    }
  }

  // java: diff_cleanupSemanticScore_ is a function inside of diff_cleanupSemanticLossless upstream.
  private static int diff_cleanupSemanticScore_(String one, String two) {
    if (one.isEmpty() || two.isEmpty()) {
      return 6;
    }

    String char1 = charAt(one, one.length() - 1);
    String char2 = charAt(two, 0);
    boolean nonAlphaNumeric1 = nonAlphaNumericRegex_.matcher(char1).find();
    boolean nonAlphaNumeric2 = nonAlphaNumericRegex_.matcher(char2).find();
    boolean whitespace1 = nonAlphaNumeric1 && whitespaceRegex_.matcher(char1).find();
    boolean whitespace2 = nonAlphaNumeric2 && whitespaceRegex_.matcher(char2).find();
    boolean lineBreak1 = whitespace1 && linebreakRegex_.matcher(char1).find();
    boolean lineBreak2 = whitespace2 && linebreakRegex_.matcher(char2).find();
    boolean blankLine1 = lineBreak1 && blanklineEndRegex_.matcher(one).find();
    boolean blankLine2 = lineBreak2 && blanklineStartRegex_.matcher(two).find();
    if (blankLine1 || blankLine2) {
      return 5;
    } else if (lineBreak1 || lineBreak2) {
      return 4;
    } else if (nonAlphaNumeric1 && !whitespace1 && whitespace2) {
      return 3;
    } else if (whitespace1 || whitespace2) {
      return 2;
    } else if (nonAlphaNumeric1 || nonAlphaNumeric2) {
      return 1;
    }
    return 0;
  }

  private static void diff_cleanupSemanticLossless(List<Diff> diffs) {
    int pointer = 1;
    while (pointer < diffs.size() - 1) {
      if (diffs.get(pointer - 1).op == DIFF_EQUAL && diffs.get(pointer + 1).op == DIFF_EQUAL) {
        String equality1 = diffs.get(pointer - 1).text;
        String edit = diffs.get(pointer).text;
        String equality2 = diffs.get(pointer + 1).text;

        int commonOffset = diff_commonSuffix(equality1, edit);
        if (commonOffset != 0) {
          String commonString = substring(edit, edit.length() - commonOffset);
          equality1 = substring(equality1, 0, equality1.length() - commonOffset);
          edit = commonString + substring(edit, 0, edit.length() - commonOffset);
          equality2 = commonString + equality2;
        }

        String bestEquality1 = equality1;
        String bestEdit = edit;
        String bestEquality2 = equality2;
        int bestScore = diff_cleanupSemanticScore_(equality1, edit) + diff_cleanupSemanticScore_(edit, equality2);
        while (charAt(edit, 0).equals(charAt(equality2, 0))) {
          equality1 += charAt(edit, 0);
          edit = substring(edit, 1) + charAt(equality2, 0);
          equality2 = substring(equality2, 1);
          int score = diff_cleanupSemanticScore_(equality1, edit) + diff_cleanupSemanticScore_(edit, equality2);
          if (score >= bestScore) {
            bestScore = score;
            bestEquality1 = equality1;
            bestEdit = edit;
            bestEquality2 = equality2;
          }
        }
        if (!diffs.get(pointer - 1).text.equals(bestEquality1)) {
          if (!bestEquality1.isEmpty()) {
            diffs.get(pointer - 1).text = bestEquality1;
          } else {
            diffs.remove(pointer - 1);
            pointer--;
          }
          diffs.get(pointer).text = bestEdit;
          if (!bestEquality2.isEmpty()) {
            diffs.get(pointer + 1).text = bestEquality2;
          } else {
            diffs.remove(pointer + 1);
            pointer--;
          }
        }
      }
      pointer++;
    }
  }

  private static final Pattern nonAlphaNumericRegex_ = Pattern.compile("[^a-zA-Z0-9]");
  private static final Pattern whitespaceRegex_ = Pattern.compile(WHITESPACE);
  private static final Pattern linebreakRegex_ = Pattern.compile("[\\r\\n]");
  // java: \z is the end of the string. $ in Java matches before a line terminator at the end too.
  private static final Pattern blanklineEndRegex_ = Pattern.compile("\\n\\r?\\n\\z");
  private static final Pattern blanklineStartRegex_ = Pattern.compile("^\\r?\\n\\r?\\n");

  private static void diff_cleanupMerge(List<Diff> diffs) {
    diffs.add(new Diff(DIFF_EQUAL, ""));
    int pointer = 0;
    int count_delete = 0;
    int count_insert = 0;
    String text_delete = "";
    String text_insert = "";
    int commonlength;
    while (pointer < diffs.size()) {
      switch (diffs.get(pointer).op) {
        case DIFF_INSERT:
          count_insert++;
          text_insert += diffs.get(pointer).text;
          pointer++;
          break;
        case DIFF_DELETE:
          count_delete++;
          text_delete += diffs.get(pointer).text;
          pointer++;
          break;
        case DIFF_EQUAL:
          if (count_delete + count_insert > 1) {
            if (count_delete != 0 && count_insert != 0) {
              commonlength = diff_commonPrefix(text_insert, text_delete);
              if (commonlength != 0) {
                if (pointer - count_delete - count_insert > 0 && diffs.get(pointer - count_delete - count_insert - 1).op == DIFF_EQUAL) {
                  diffs.get(pointer - count_delete - count_insert - 1).text += substring(text_insert, 0, commonlength);
                } else {
                  diffs.add(0, new Diff(DIFF_EQUAL, substring(text_insert, 0, commonlength)));
                  pointer++;
                }
                text_insert = substring(text_insert, commonlength);
                text_delete = substring(text_delete, commonlength);
              }
              commonlength = diff_commonSuffix(text_insert, text_delete);
              if (commonlength != 0) {
                diffs.get(pointer).text = substring(text_insert, text_insert.length() - commonlength) + diffs.get(pointer).text;
                text_insert = substring(text_insert, 0, text_insert.length() - commonlength);
                text_delete = substring(text_delete, 0, text_delete.length() - commonlength);
              }
            }
            pointer -= count_delete + count_insert;
            // java: splice(pointer, n) removes n items.
            for (int i = 0; i < count_delete + count_insert; i++) {
              diffs.remove(pointer);
            }
            if (text_delete.length() != 0) {
              diffs.add(pointer, new Diff(DIFF_DELETE, text_delete));
              pointer++;
            }
            if (text_insert.length() != 0) {
              diffs.add(pointer, new Diff(DIFF_INSERT, text_insert));
              pointer++;
            }
            pointer++;
          } else if (pointer != 0 && diffs.get(pointer - 1).op == DIFF_EQUAL) {
            diffs.get(pointer - 1).text += diffs.get(pointer).text;
            diffs.remove(pointer);
          } else {
            pointer++;
          }
          count_insert = 0;
          count_delete = 0;
          text_delete = "";
          text_insert = "";
          break;
      }
    }
    if (diffs.get(diffs.size() - 1).text.equals("")) {
      diffs.remove(diffs.size() - 1);
    }

    boolean changes = false;
    pointer = 1;
    while (pointer < diffs.size() - 1) {
      if (diffs.get(pointer - 1).op == DIFF_EQUAL && diffs.get(pointer + 1).op == DIFF_EQUAL) {
        if (substring(diffs.get(pointer).text, diffs.get(pointer).text.length() - diffs.get(pointer - 1).text.length()).equals(diffs.get(pointer - 1).text)) {
          diffs.get(pointer).text = diffs.get(pointer - 1).text + substring(diffs.get(pointer).text, 0, diffs.get(pointer).text.length() - diffs.get(pointer - 1).text.length());
          diffs.get(pointer + 1).text = diffs.get(pointer - 1).text + diffs.get(pointer + 1).text;
          diffs.remove(pointer - 1);
          changes = true;
        } else if (substring(diffs.get(pointer).text, 0, diffs.get(pointer + 1).text.length()).equals(diffs.get(pointer + 1).text)) {
          diffs.get(pointer - 1).text += diffs.get(pointer + 1).text;
          diffs.get(pointer).text = substring(diffs.get(pointer).text, diffs.get(pointer + 1).text.length()) + diffs.get(pointer + 1).text;
          diffs.remove(pointer + 1);
          changes = true;
        }
      }
      pointer++;
    }
    if (changes) {
      diff_cleanupMerge(diffs);
    }
  }

  static final String NO_DIFF_MESSAGE = "Compared values have no visual difference.";
  static final String SIMILAR_MESSAGE = "Compared values serialize to the same structure.\n" + "Printing internal object structure without calling `toJSON` instead.";

  private static boolean isEmptyString(List<String> lines) {
    return lines.size() == 1 && lines.get(0).length() == 0;
  }

  private static ChangeCounts countChanges(List<Diff> diffs) {
    int a = 0;
    int b = 0;
    for (Diff diff : diffs) {
      switch (diff.op) {
        case DIFF_DELETE:
          a += 1;
          break;
        case DIFF_INSERT:
          b += 1;
          break;
      }
    }
    ChangeCounts changeCounts = new ChangeCounts();
    changeCounts.a = a;
    changeCounts.b = b;
    return changeCounts;
  }

  private static String printAnnotation(DiffOptions options, ChangeCounts changeCounts) {
    String aAnnotation = options.aAnnotation;
    Function<String, String> aColor = options.aColor;
    String aIndicator = options.aIndicator;
    String bAnnotation = options.bAnnotation;
    Function<String, String> bColor = options.bColor;
    String bIndicator = options.bIndicator;
    boolean includeChangeCounts = options.includeChangeCounts;
    boolean omitAnnotationLines = options.omitAnnotationLines;
    if (omitAnnotationLines) {
      return "";
    }
    String aRest = "";
    String bRest = "";
    if (includeChangeCounts) {
      String aCount = String.valueOf(changeCounts.a);
      String bCount = String.valueOf(changeCounts.b);

      int baAnnotationLengthDiff = bAnnotation.length() - aAnnotation.length();
      String aAnnotationPadding = repeat(" ", Math.max(0, baAnnotationLengthDiff));
      String bAnnotationPadding = repeat(" ", Math.max(0, -baAnnotationLengthDiff));

      int baCountLengthDiff = bCount.length() - aCount.length();
      String aCountPadding = repeat(" ", Math.max(0, baCountLengthDiff));
      String bCountPadding = repeat(" ", Math.max(0, -baCountLengthDiff));
      aRest = aAnnotationPadding + "  " + aIndicator + " " + aCountPadding + aCount;
      bRest = bAnnotationPadding + "  " + bIndicator + " " + bCountPadding + bCount;
    }
    String a = aIndicator + " " + aAnnotation + aRest;
    String b = bIndicator + " " + bAnnotation + bRest;
    return aColor.apply(a) + "\n" + bColor.apply(b) + "\n\n";
  }

  static String printDiffLines(List<Diff> diffs, DiffOptions options) {
    return printAnnotation(options, countChanges(diffs)) + (options.expand ? joinAlignedDiffsExpand(diffs, options) : joinAlignedDiffsNoExpand(diffs, options));
  }

  static String diffLinesUnified(List<String> aLines, List<String> bLines, DiffOptions options) {
    return printDiffLines(diffLinesRaw(isEmptyString(aLines) ? new ArrayList<>() : map(aLines, JestDiff::escapeControlCharacters), isEmptyString(bLines) ? new ArrayList<>() : map(bLines, JestDiff::escapeControlCharacters)), normalizeDiffOptions(options));
  }

  // java: helper with no counterpart upstream, the port of Array.prototype.map.
  private static List<String> map(List<String> lines, Function<String, String> callback) {
    List<String> result = new ArrayList<>();
    for (String line : lines) {
      result.add(callback.apply(line));
    }
    return result;
  }

  static String diffLinesUnified2(List<String> aLinesDisplay, List<String> bLinesDisplay, List<String> aLinesCompare, List<String> bLinesCompare, DiffOptions options) {
    if (isEmptyString(aLinesDisplay) && isEmptyString(aLinesCompare)) {
      aLinesDisplay = new ArrayList<>();
      aLinesCompare = new ArrayList<>();
    }
    if (isEmptyString(bLinesDisplay) && isEmptyString(bLinesCompare)) {
      bLinesDisplay = new ArrayList<>();
      bLinesCompare = new ArrayList<>();
    }
    if (aLinesDisplay.size() != aLinesCompare.size() || bLinesDisplay.size() != bLinesCompare.size()) {
      return diffLinesUnified(aLinesDisplay, bLinesDisplay, options);
    }
    List<Diff> diffs = diffLinesRaw(aLinesCompare, bLinesCompare);

    int aIndex = 0;
    int bIndex = 0;
    for (Diff diff : diffs) {
      switch (diff.op) {
        case DIFF_DELETE:
          diff.text = aLinesDisplay.get(aIndex);
          aIndex += 1;
          break;
        case DIFF_INSERT:
          diff.text = bLinesDisplay.get(bIndex);
          bIndex += 1;
          break;
        default:
          diff.text = bLinesDisplay.get(bIndex);
          aIndex += 1;
          bIndex += 1;
      }
    }
    return printDiffLines(diffs, normalizeDiffOptions(options));
  }

  static List<Diff> diffLinesRaw(List<String> aLines, List<String> bLines) {
    int aLength = aLines.size();
    int bLength = bLines.size();
    DiffSequences.IsCommon isCommon = (aIndex, bIndex) -> aLines.get(aIndex).equals(bLines.get(bIndex));
    List<Diff> diffs = new ArrayList<>();
    // java: a lambda cannot assign a local variable, aIndex and bIndex are arrays of one item.
    int[] aIndex = {0};
    int[] bIndex = {0};
    DiffSequences.FoundSubsequence foundSubsequence = (nCommon, aCommon, bCommon) -> {
      for (; aIndex[0] != aCommon; aIndex[0] += 1) {
        diffs.add(new Diff(DIFF_DELETE, aLines.get(aIndex[0])));
      }
      for (; bIndex[0] != bCommon; bIndex[0] += 1) {
        diffs.add(new Diff(DIFF_INSERT, bLines.get(bIndex[0])));
      }
      for (; nCommon != 0; nCommon -= 1, aIndex[0] += 1, bIndex[0] += 1) {
        diffs.add(new Diff(DIFF_EQUAL, bLines.get(bIndex[0])));
      }
    };
    diffSequence(aLength, bLength, isCommon, foundSubsequence);

    for (; aIndex[0] != aLength; aIndex[0] += 1) {
      diffs.add(new Diff(DIFF_DELETE, aLines.get(aIndex[0])));
    }
    for (; bIndex[0] != bLength; bIndex[0] += 1) {
      diffs.add(new Diff(DIFF_INSERT, bLines.get(bIndex[0])));
    }
    return diffs;
  }

  private static List<Diff> diffStrings(String a, String b) {
    DiffSequences.IsCommon isCommon = (aIndex, bIndex) -> a.charAt(aIndex) == b.charAt(bIndex);
    // java: a lambda cannot assign a local variable, aIndex and bIndex are arrays of one item.
    int[] aIndex = {0};
    int[] bIndex = {0};
    List<Diff> diffs = new ArrayList<>();
    DiffSequences.FoundSubsequence foundSubsequence = (nCommon, aCommon, bCommon) -> {
      if (aIndex[0] != aCommon) {
        diffs.add(new Diff(DIFF_DELETE, a.substring(aIndex[0], aCommon)));
      }
      if (bIndex[0] != bCommon) {
        diffs.add(new Diff(DIFF_INSERT, b.substring(bIndex[0], bCommon)));
      }
      aIndex[0] = aCommon + nCommon;
      bIndex[0] = bCommon + nCommon;
      diffs.add(new Diff(DIFF_EQUAL, b.substring(bCommon, bIndex[0])));
    };
    diffSequence(a.length(), b.length(), isCommon, foundSubsequence);

    if (aIndex[0] != a.length()) {
      diffs.add(new Diff(DIFF_DELETE, a.substring(aIndex[0])));
    }
    if (bIndex[0] != b.length()) {
      diffs.add(new Diff(DIFF_INSERT, b.substring(bIndex[0])));
    }
    return diffs;
  }

  private static final Pattern controlCharactersRegex = Pattern.compile("[\\u0000-\\u0008\\u000B\\u000C\\u000E-\\u001F\\u007F-\\u009F]");

  static String escapeControlCharacters(String str) {
    // java: replaceAll with a callback is a loop over the matches.
    Matcher matcher = controlCharactersRegex.matcher(str);
    StringBuffer result = new StringBuffer();
    while (matcher.find()) {
      String match = matcher.group();
      String replacement;
      switch (match) {
        case "\b":
          replacement = "\\b";
          break;
        case "\f":
          replacement = "\\f";
          break;
        case "\u000B":
          replacement = "\\v";
          break;
        default: {
          int code = match.codePointAt(0);
          replacement = "\\x" + (code < 16 ? "0" : "") + Integer.toString(code, 16);
        }
      }
      matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
    }
    matcher.appendTail(result);
    return result.toString();
  }

  private static String concatenateRelevantDiffs(int op, List<Diff> diffs, Function<String, String> changeColor) {
    String reduced = "";
    for (Diff diff : diffs) {
      reduced = reduced + (diff.op == DIFF_EQUAL ? diff.text : diff.op == op && diff.text.length() > 0
        ? changeColor.apply(diff.text) : "");
    }
    return reduced;
  }

  private static class ChangeBuffer {
    int op;
    List<Diff> line;
    List<Diff> lines;
    Function<String, String> changeColor;

    ChangeBuffer(int op, Function<String, String> changeColor) {
      this.op = op;
      this.line = new ArrayList<>();
      this.lines = new ArrayList<>();
      this.changeColor = changeColor;
    }

    void pushSubstring(String substring) {
      this.pushDiff(new Diff(this.op, substring));
    }

    void pushLine() {
      this.lines.add(this.line.size() == 1 ? this.line.get(0).op == this.op ? this.line.get(0)
        : new Diff(this.op, this.line.get(0).text) : new Diff(this.op, concatenateRelevantDiffs(this.op, this.line, this.changeColor))
      );
      this.line.clear();
    }

    boolean isLineEmpty() {
      return this.line.size() == 0;
    }

    void pushDiff(Diff diff) {
      this.line.add(diff);
    }

    void align(Diff diff) {
      String string = diff.text;
      if (string.contains("\n")) {
        List<String> substrings = split(string, "\n");
        int iLast = substrings.size() - 1;
        for (int i = 0; i < substrings.size(); i++) {
          String substring = substrings.get(i);
          if (i < iLast) {
            this.pushSubstring(substring);
            this.pushLine();
          } else if (substring.length() > 0) {
            this.pushSubstring(substring);
          }
        }
      } else {
        this.pushDiff(diff);
      }
    }

    void moveLinesTo(List<Diff> lines) {
      if (!this.isLineEmpty()) {
        this.pushLine();
      }
      lines.addAll(this.lines);
      this.lines.clear();
    }
  }

  private static class CommonBuffer {
    ChangeBuffer deleteBuffer;
    ChangeBuffer insertBuffer;
    List<Diff> lines;

    CommonBuffer(ChangeBuffer deleteBuffer, ChangeBuffer insertBuffer) {
      this.deleteBuffer = deleteBuffer;
      this.insertBuffer = insertBuffer;
      this.lines = new ArrayList<>();
    }

    void pushDiffCommonLine(Diff diff) {
      this.lines.add(diff);
    }

    void pushDiffChangeLines(Diff diff) {
      boolean isDiffEmpty = diff.text.length() == 0;

      if (!isDiffEmpty || this.deleteBuffer.isLineEmpty()) {
        this.deleteBuffer.pushDiff(diff);
      }
      if (!isDiffEmpty || this.insertBuffer.isLineEmpty()) {
        this.insertBuffer.pushDiff(diff);
      }
    }

    void flushChangeLines() {
      this.deleteBuffer.moveLinesTo(this.lines);
      this.insertBuffer.moveLinesTo(this.lines);
    }

    void align(Diff diff) {
      int op = diff.op;
      String string = diff.text;
      if (string.contains("\n")) {
        List<String> substrings = split(string, "\n");
        int iLast = substrings.size() - 1;
        for (int i = 0; i < substrings.size(); i++) {
          String substring = substrings.get(i);
          if (i == 0) {
            Diff subdiff = new Diff(op, substring);
            if (this.deleteBuffer.isLineEmpty() && this.insertBuffer.isLineEmpty()) {
              this.flushChangeLines();
              this.pushDiffCommonLine(subdiff);
            } else {
              this.pushDiffChangeLines(subdiff);
              this.flushChangeLines();
            }
          } else if (i < iLast) {
            this.pushDiffCommonLine(new Diff(op, substring));
          } else if (substring.length() > 0) {
            this.pushDiffChangeLines(new Diff(op, substring));
          }
        }
      } else {
        this.pushDiffChangeLines(diff);
      }
    }

    List<Diff> getLines() {
      this.flushChangeLines();
      return this.lines;
    }
  }

  private static List<Diff> getAlignedDiffs(List<Diff> diffs, Function<String, String> changeColor) {
    ChangeBuffer deleteBuffer = new ChangeBuffer(DIFF_DELETE, changeColor);
    ChangeBuffer insertBuffer = new ChangeBuffer(DIFF_INSERT, changeColor);
    CommonBuffer commonBuffer = new CommonBuffer(deleteBuffer, insertBuffer);
    for (Diff diff : diffs) {
      switch (diff.op) {
        case DIFF_DELETE:
          deleteBuffer.align(diff);
          break;
        case DIFF_INSERT:
          insertBuffer.align(diff);
          break;
        default:
          commonBuffer.align(diff);
      }
    }
    return commonBuffer.getLines();
  }

  // java: \z is the end of the string. $ in Java matches before a line terminator at the end too.
  private static final Pattern trailingSpacesRegex = Pattern.compile(WHITESPACE + "+\\z");

  private static String formatTrailingSpaces(String line, Function<String, String> trailingSpaceFormatter) {
    // java: replace with a callback is the replacement of the first match.
    Matcher matcher = trailingSpacesRegex.matcher(line);
    if (!matcher.find()) {
      return line;
    }
    return line.substring(0, matcher.start()) + trailingSpaceFormatter.apply(matcher.group()) + line.substring(matcher.end());
  }

  private static String printDiffLine(String line, boolean isFirstOrLast, Function<String, String> color, String indicator, Function<String, String> trailingSpaceFormatter, String emptyFirstOrLastLinePlaceholder) {
    return line.length() == 0 ? indicator.equals(" ") ? isFirstOrLast && emptyFirstOrLastLinePlaceholder.length() > 0 ? color.apply(indicator + " " + emptyFirstOrLastLinePlaceholder) : "" : color.apply(indicator) : color.apply(indicator + " " + formatTrailingSpaces(line, trailingSpaceFormatter));
  }

  private static String printDeleteLine(String line, boolean isFirstOrLast, DiffOptions options) {
    return printDiffLine(line, isFirstOrLast, options.aColor, options.aIndicator, options.changeLineTrailingSpaceColor, options.emptyFirstOrLastLinePlaceholder);
  }

  private static String printInsertLine(String line, boolean isFirstOrLast, DiffOptions options) {
    return printDiffLine(line, isFirstOrLast, options.bColor, options.bIndicator, options.changeLineTrailingSpaceColor, options.emptyFirstOrLastLinePlaceholder);
  }

  private static String printCommonLine(String line, boolean isFirstOrLast, DiffOptions options) {
    return printDiffLine(line, isFirstOrLast, options.commonColor, options.commonIndicator, options.commonLineTrailingSpaceColor, options.emptyFirstOrLastLinePlaceholder);
  }

  private static String createPatchMark(int aStart, int aEnd, int bStart, int bEnd, DiffOptions options) {
    return options.patchColor.apply("@@ -" + (aStart + 1) + "," + (aEnd - aStart) + " +" + (bStart + 1) + "," + (bEnd - bStart) + " @@");
  }

  private static String joinAlignedDiffsNoExpand(List<Diff> diffs, DiffOptions options) {
    int iLength = diffs.size();
    int nContextLines = options.contextLines;
    int nContextLines2 = nContextLines + nContextLines;

    int jLength = iLength;
    boolean hasExcessAtStartOrEnd = false;
    int nExcessesBetweenChanges = 0;
    int i = 0;
    while (i != iLength) {
      int iStart = i;
      while (i != iLength && diffs.get(i).op == DIFF_EQUAL) {
        i += 1;
      }
      if (iStart != i) {
        if (iStart == 0) {
          if (i > nContextLines) {
            jLength -= i - nContextLines;
            hasExcessAtStartOrEnd = true;
          }
        } else if (i == iLength) {
          int n = i - iStart;
          if (n > nContextLines) {
            jLength -= n - nContextLines;
            hasExcessAtStartOrEnd = true;
          }
        } else {
          int n = i - iStart;
          if (n > nContextLines2) {
            jLength -= n - nContextLines2;
            nExcessesBetweenChanges += 1;
          }
        }
      }
      while (i != iLength && diffs.get(i).op != DIFF_EQUAL) {
        i += 1;
      }
    }
    boolean hasPatch = nExcessesBetweenChanges != 0 || hasExcessAtStartOrEnd;
    if (nExcessesBetweenChanges != 0) {
      jLength += nExcessesBetweenChanges + 1;
    } else if (hasExcessAtStartOrEnd) {
      jLength += 1;
    }
    int jLast = jLength - 1;
    List<String> lines = new ArrayList<>();
    int jPatchMark = 0;
    if (hasPatch) {
      lines.add("");
    }

    // java: a lambda cannot assign a local variable, aEnd and bEnd are arrays of one item.
    int aStart = 0;
    int bStart = 0;
    int[] aEnd = {0};
    int[] bEnd = {0};
    Consumer<String> pushCommonLine = line -> {
      int j = lines.size();
      lines.add(printCommonLine(line, j == 0 || j == jLast, options));
      aEnd[0] += 1;
      bEnd[0] += 1;
    };
    Consumer<String> pushDeleteLine = line -> {
      int j = lines.size();
      lines.add(printDeleteLine(line, j == 0 || j == jLast, options));
      aEnd[0] += 1;
    };
    Consumer<String> pushInsertLine = line -> {
      int j = lines.size();
      lines.add(printInsertLine(line, j == 0 || j == jLast, options));
      bEnd[0] += 1;
    };

    i = 0;
    while (i != iLength) {
      int iStart = i;
      while (i != iLength && diffs.get(i).op == DIFF_EQUAL) {
        i += 1;
      }
      if (iStart != i) {
        if (iStart == 0) {
          if (i > nContextLines) {
            iStart = i - nContextLines;
            aStart = iStart;
            bStart = iStart;
            aEnd[0] = aStart;
            bEnd[0] = bStart;
          }
          for (int iCommon = iStart; iCommon != i; iCommon += 1) {
            pushCommonLine.accept(diffs.get(iCommon).text);
          }
        } else if (i == iLength) {
          int iEnd = i - iStart > nContextLines ? iStart + nContextLines : i;
          for (int iCommon = iStart; iCommon != iEnd; iCommon += 1) {
            pushCommonLine.accept(diffs.get(iCommon).text);
          }
        } else {
          int nCommon = i - iStart;
          if (nCommon > nContextLines2) {
            int iEnd = iStart + nContextLines;
            for (int iCommon = iStart; iCommon != iEnd; iCommon += 1) {
              pushCommonLine.accept(diffs.get(iCommon).text);
            }
            lines.set(jPatchMark, createPatchMark(aStart, aEnd[0], bStart, bEnd[0], options));
            jPatchMark = lines.size();
            lines.add("");

            int nOmit = nCommon - nContextLines2;
            aStart = aEnd[0] + nOmit;
            bStart = bEnd[0] + nOmit;
            aEnd[0] = aStart;
            bEnd[0] = bStart;
            for (int iCommon = i - nContextLines; iCommon != i; iCommon += 1) {
              pushCommonLine.accept(diffs.get(iCommon).text);
            }
          } else {
            for (int iCommon = iStart; iCommon != i; iCommon += 1) {
              pushCommonLine.accept(diffs.get(iCommon).text);
            }
          }
        }
      }
      while (i != iLength && diffs.get(i).op == DIFF_DELETE) {
        pushDeleteLine.accept(diffs.get(i).text);
        i += 1;
      }
      while (i != iLength && diffs.get(i).op == DIFF_INSERT) {
        pushInsertLine.accept(diffs.get(i).text);
        i += 1;
      }
    }
    if (hasPatch) {
      lines.set(jPatchMark, createPatchMark(aStart, aEnd[0], bStart, bEnd[0], options));
    }
    return String.join("\n", lines);
  }

  private static String joinAlignedDiffsExpand(List<Diff> diffs, DiffOptions options) {
    List<String> lines = new ArrayList<>();
    for (int i = 0; i < diffs.size(); i++) {
      Diff diff = diffs.get(i);
      String line = diff.text;
      boolean isFirstOrLast = i == 0 || i == diffs.size() - 1;
      switch (diff.op) {
        case DIFF_DELETE:
          lines.add(printDeleteLine(line, isFirstOrLast, options));
          break;
        case DIFF_INSERT:
          lines.add(printInsertLine(line, isFirstOrLast, options));
          break;
        default:
          lines.add(printCommonLine(line, isFirstOrLast, options));
      }
    }
    return String.join("\n", lines);
  }

  static String noColor(String string) {
    return string;
  }

  private static final int DIFF_CONTEXT_DEFAULT = 5;
  private static final DiffOptions OPTIONS_DEFAULT = new DiffOptions();
  static {
    OPTIONS_DEFAULT.aAnnotation = "Expected";
    OPTIONS_DEFAULT.aColor = Chalk::green;
    OPTIONS_DEFAULT.aIndicator = "-";
    OPTIONS_DEFAULT.bAnnotation = "Received";
    OPTIONS_DEFAULT.bColor = Chalk::red;
    OPTIONS_DEFAULT.bIndicator = "+";
    OPTIONS_DEFAULT.changeColor = Chalk::inverse;
    OPTIONS_DEFAULT.changeLineTrailingSpaceColor = JestDiff::noColor;
    OPTIONS_DEFAULT.commonColor = Chalk::dim;
    OPTIONS_DEFAULT.commonIndicator = " ";
    OPTIONS_DEFAULT.commonLineTrailingSpaceColor = JestDiff::noColor;
    OPTIONS_DEFAULT.compareKeys = null;
    OPTIONS_DEFAULT.contextLines = DIFF_CONTEXT_DEFAULT;
    OPTIONS_DEFAULT.emptyFirstOrLastLinePlaceholder = "";
    OPTIONS_DEFAULT.expand = true;
    OPTIONS_DEFAULT.includeChangeCounts = false;
    OPTIONS_DEFAULT.omitAnnotationLines = false;
    OPTIONS_DEFAULT.patchColor = Chalk::yellow;
  }

  private static Comparator<String> getCompareKeys(Comparator<String> compareKeys) {
    return compareKeys != null ? compareKeys : OPTIONS_DEFAULT.compareKeys;
  }

  private static int getContextLines(Integer contextLines) {
    return contextLines != null && contextLines >= 0 ? contextLines : DIFF_CONTEXT_DEFAULT;
  }

  static DiffOptions normalizeDiffOptions(DiffOptions options) {
    if (options == null) {
      options = new DiffOptions();
    }
    // java: the object spread is one assignment per field.
    DiffOptions result = new DiffOptions();
    result.aAnnotation = options.aAnnotation != null ? options.aAnnotation : OPTIONS_DEFAULT.aAnnotation;
    result.aColor = options.aColor != null ? options.aColor : OPTIONS_DEFAULT.aColor;
    result.aIndicator = options.aIndicator != null ? options.aIndicator : OPTIONS_DEFAULT.aIndicator;
    result.bAnnotation = options.bAnnotation != null ? options.bAnnotation : OPTIONS_DEFAULT.bAnnotation;
    result.bColor = options.bColor != null ? options.bColor : OPTIONS_DEFAULT.bColor;
    result.bIndicator = options.bIndicator != null ? options.bIndicator : OPTIONS_DEFAULT.bIndicator;
    result.changeColor = options.changeColor != null ? options.changeColor : OPTIONS_DEFAULT.changeColor;
    result.changeLineTrailingSpaceColor = options.changeLineTrailingSpaceColor != null ? options.changeLineTrailingSpaceColor : OPTIONS_DEFAULT.changeLineTrailingSpaceColor;
    result.commonColor = options.commonColor != null ? options.commonColor : OPTIONS_DEFAULT.commonColor;
    result.commonIndicator = options.commonIndicator != null ? options.commonIndicator : OPTIONS_DEFAULT.commonIndicator;
    result.commonLineTrailingSpaceColor = options.commonLineTrailingSpaceColor != null ? options.commonLineTrailingSpaceColor : OPTIONS_DEFAULT.commonLineTrailingSpaceColor;
    result.emptyFirstOrLastLinePlaceholder = options.emptyFirstOrLastLinePlaceholder != null ? options.emptyFirstOrLastLinePlaceholder : OPTIONS_DEFAULT.emptyFirstOrLastLinePlaceholder;
    result.expand = options.expand != null ? options.expand : OPTIONS_DEFAULT.expand;
    result.includeChangeCounts = options.includeChangeCounts != null ? options.includeChangeCounts : OPTIONS_DEFAULT.includeChangeCounts;
    result.omitAnnotationLines = options.omitAnnotationLines != null ? options.omitAnnotationLines : OPTIONS_DEFAULT.omitAnnotationLines;
    result.patchColor = options.patchColor != null ? options.patchColor : OPTIONS_DEFAULT.patchColor;
    result.compareKeys = getCompareKeys(options.compareKeys);
    result.contextLines = getContextLines(options.contextLines);
    return result;
  }

  private static boolean hasCommonDiff(List<Diff> diffs, boolean isMultiline) {
    if (isMultiline) {
      int iLast = diffs.size() - 1;
      for (int i = 0; i < diffs.size(); i++) {
        Diff diff = diffs.get(i);
        if (diff.op == DIFF_EQUAL && (i != iLast || !diff.text.equals("\n"))) {
          return true;
        }
      }
      return false;
    }
    for (Diff diff : diffs) {
      if (diff.op == DIFF_EQUAL) {
        return true;
      }
    }
    return false;
  }

  static String diffStringsUnified(String a, String b, DiffOptions options) {
    if (!a.equals(b) && a.length() > 0 && b.length() > 0) {
      boolean isMultiline = a.contains("\n") || b.contains("\n");

      List<Diff> diffs = diffStringsRaw(isMultiline ? a + "\n" : a, isMultiline ? b + "\n" : b, true
      );
      if (hasCommonDiff(diffs, isMultiline)) {
        DiffOptions optionsNormalized = normalizeDiffOptions(options);
        List<Diff> lines = getAlignedDiffs(diffs, optionsNormalized.changeColor);
        return printDiffLines(lines, optionsNormalized);
      }
    }

    return diffLinesUnified(split(a, "\n"), split(b, "\n"), options);
  }

  static List<Diff> diffStringsRaw(String a, String b, boolean cleanup) {
    List<Diff> diffs = diffStrings(a, b);
    if (cleanup) {
      diff_cleanupSemantic(diffs);
    }
    return diffs;
  }

  private static String getCommonMessage(String message, DiffOptions options) {
    Function<String, String> commonColor = normalizeDiffOptions(options).commonColor;
    return commonColor.apply(message);
  }

  // java: there are no plugins.
  private static final PrettyFormat.Options FORMAT_OPTIONS = new PrettyFormat.Options();
  private static final PrettyFormat.Options FALLBACK_FORMAT_OPTIONS = new PrettyFormat.Options();
  static {
    FALLBACK_FORMAT_OPTIONS.callToJSON = false;
    FALLBACK_FORMAT_OPTIONS.maxDepth = 10;
  }

  // java: helper with no counterpart upstream, the port of Object.is.
  private static boolean objectIs(Object a, Object b) {
    if (a instanceof Number && b instanceof Number) {
      return Double.compare(((Number) a).doubleValue(), ((Number) b).doubleValue()) == 0;
    }
    if (a instanceof String || a instanceof Boolean) {
      return a.equals(b);
    }
    return a == b;
  }

  static String diff(Object a, Object b, DiffOptions options) {
    if (objectIs(a, b)) {
      return getCommonMessage(NO_DIFF_MESSAGE, options);
    }
    String aType = getType(a);
    String expectedType = aType;
    // java: there are no asymmetric matchers, omitDifference is always false.
    if (!expectedType.equals(getType(b))) {
      return "  Comparing two different types of values." + " Expected " + Chalk.green(expectedType) + " but " + "received " + Chalk.red(getType(b)) + ".";
    }
    switch (aType) {
      case "string":
        return diffLinesUnified(split(escapeControlCharacters((String) a), "\n"), split(escapeControlCharacters((String) b), "\n"), options);
      case "boolean":
      case "number":
        return comparePrimitive(a, b, options);
      // java: the types map and set have no Java values, sortMap and sortSet are not ported.
      default:
        return compareObjects(a, b, options);
    }
  }

  private static String comparePrimitive(Object a, Object b, DiffOptions options) {
    String aFormat = PrettyFormat.format(a, FORMAT_OPTIONS);
    String bFormat = PrettyFormat.format(b, FORMAT_OPTIONS);
    return aFormat.equals(bFormat) ? getCommonMessage(NO_DIFF_MESSAGE, options) : diffLinesUnified(split(aFormat, "\n"), split(bFormat, "\n"), options);
  }

  private static String compareObjects(Object a, Object b, DiffOptions options) {
    String difference = null;
    boolean hasThrown = false;
    try {
      PrettyFormat.Options formatOptions = getFormatOptions(FORMAT_OPTIONS, options);
      difference = getObjectsDifference(a, b, formatOptions, options);
    } catch (RuntimeException e) {
      hasThrown = true;
    }
    String noDiffMessage = getCommonMessage(NO_DIFF_MESSAGE, options);
    if (difference == null || difference.equals(noDiffMessage)) {
      PrettyFormat.Options formatOptions = getFormatOptions(FALLBACK_FORMAT_OPTIONS, options);
      difference = getObjectsDifference(a, b, formatOptions, options);
      if (!difference.equals(noDiffMessage) && !hasThrown) {
        difference = getCommonMessage(SIMILAR_MESSAGE, options) + "\n\n" + difference;
      }
    }
    return difference;
  }

  private static PrettyFormat.Options getFormatOptions(PrettyFormat.Options formatOptions, DiffOptions options) {
    Comparator<String> compareKeys = normalizeDiffOptions(options).compareKeys;
    // java: the object spread is one assignment per field.
    PrettyFormat.Options result = new PrettyFormat.Options();
    result.callToJSON = formatOptions.callToJSON;
    result.indent = formatOptions.indent;
    result.maxDepth = formatOptions.maxDepth;
    result.maxWidth = formatOptions.maxWidth;
    result.min = formatOptions.min;
    result.compareKeys = compareKeys;
    return result;
  }

  private static String getObjectsDifference(Object a, Object b, PrettyFormat.Options formatOptions, DiffOptions options) {
    // java: the object spread is one assignment per field.
    PrettyFormat.Options formatOptionsZeroIndent = new PrettyFormat.Options();
    formatOptionsZeroIndent.callToJSON = formatOptions.callToJSON;
    formatOptionsZeroIndent.compareKeys = formatOptions.compareKeys;
    formatOptionsZeroIndent.maxDepth = formatOptions.maxDepth;
    formatOptionsZeroIndent.maxWidth = formatOptions.maxWidth;
    formatOptionsZeroIndent.min = formatOptions.min;
    formatOptionsZeroIndent.indent = 0;
    String aCompare = PrettyFormat.format(a, formatOptionsZeroIndent);
    String bCompare = PrettyFormat.format(b, formatOptionsZeroIndent);
    if (aCompare.equals(bCompare)) {
      return getCommonMessage(NO_DIFF_MESSAGE, options);
    } else {
      String aDisplay = PrettyFormat.format(a, formatOptions);
      String bDisplay = PrettyFormat.format(b, formatOptions);
      return diffLinesUnified2(split(aDisplay, "\n"), split(bDisplay, "\n"), split(aCompare, "\n"), split(bCompare, "\n"), options);
    }
  }
}
