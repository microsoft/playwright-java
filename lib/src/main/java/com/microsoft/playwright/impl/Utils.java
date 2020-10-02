/**
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

import com.google.gson.Gson;

import java.util.*;

class Utils {
  // TODO: generate converter.
  static <F, T> T convertViaJson(F f, Class<T> t) {
    String json = new Gson().toJson(f);
    return new Gson().fromJson(json, t);
  }

  static Set<Character> escapeGlobChars = new HashSet<>(Arrays.asList('/', '$', '^', '+', '.', '(', ')', '=', '!', '|'));

  static String globToRegex(String glob) {
    StringBuilder tokens = new StringBuilder();
    tokens.append('^');
    boolean inGroup = false;
    for (int i = 0; i < glob.length(); ++i) {
      char c = glob.charAt(i);
      if (escapeGlobChars.contains(c)) {
        tokens.append("\\" + c);
        continue;
      }
      if (c == '*') {
        boolean beforeDeep = i < 1 || glob.charAt(i - 1) == '/';
        int starCount = 1;
        while (i + 1 < glob.length() && glob.charAt(i + 1) == '*') {
          starCount++;
          i++;
        }
        boolean afterDeep = i + 1 >= glob.length() || glob.charAt(i + 1) == '/';
        boolean isDeep = starCount > 1 && beforeDeep && afterDeep;
        if (isDeep) {
          tokens.append("((?:[^/]*(?:\\/|$))*)");
          i++;
        } else {
          tokens.append("([^/]*)");
        }
        continue;
      }

      switch (c) {
        case '?':
          tokens.append('.');
          break;
        case '{':
          inGroup = true;
          tokens.append('(');
          break;
        case '}':
          inGroup = false;
          tokens.append(')');
          break;
        case ',':
          if (inGroup) {
            tokens.append('|');
            break;
          }
          tokens.append("\\" + c);
          break;
        default:
          tokens.append(c);
      }
    }
    tokens.append('$');
    return tokens.toString();
  }

}
