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

package com.microsoft.playwright.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// We manually copy stderr and stdout from child process as INHERIT for err/out streams
// doesn't work well in Java Enterprise, see
// https://github.com/microsoft/playwright-java/issues/418#issuecomment-832650650
public class StreamRedirectThread extends Thread {
  private final InputStream from;
  private final OutputStream to;

  public StreamRedirectThread(InputStream from, OutputStream to) {
    this.from = from;
    this.to = to;
    start();
  }

  @Override
  public void run() {
    byte[] buffer = new byte[1<<14];
    try {
      while (true) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
        }
        while (from.available() != 0) {
          int len = from.read(buffer);
          if (len != -1) {
            to.write(buffer);
          }
        }
        if (isInterrupted()) {
          break;
        }
      }
    } catch (IOException e) {
      e.printStackTrace(System.err);
    }
  }

  public void interruptAndJoin() {
    interrupt();
    try {
      join();
    } catch (InterruptedException e) {
      e.printStackTrace(System.err);
    }
  }
}
