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

import com.microsoft.playwright.PlaywrightException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class Transport {
  private final BlockingQueue<String> incoming = new ArrayBlockingQueue<>(1000);
  private final BlockingQueue<String> outgoing= new ArrayBlockingQueue<>(1000);

  private final ReaderThread readerThread;
  private final WriterThread writerThread;

  private boolean isClosed;

  Transport(InputStream input, OutputStream output) {
    DataInputStream in = new DataInputStream(new BufferedInputStream(input));
    readerThread = new ReaderThread(in, incoming);
    readerThread.start();
    DataOutputStream out = new DataOutputStream(output);
    writerThread = new WriterThread(out, outgoing);
    writerThread.start();
  }

  public void send(String message) {
    if (isClosed) {
      throw new PlaywrightException("Playwright connection closed");
    }
    try {
      outgoing.put(message);
    } catch (InterruptedException e) {
      throw new PlaywrightException("Failed to send message", e);
    }
  }

  public String poll(Duration timeout) {
    if (isClosed) {
      throw new PlaywrightException("Playwright connection closed");
    }
    try {
      return incoming.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      throw new PlaywrightException("Failed to read message", e);
    }
  }

  void close() throws IOException {
    if (isClosed) {
      return;
    }
    isClosed = true;
    readerThread.interrupt();
    readerThread.in.close();
    writerThread.interrupt();
    writerThread.out.close();
  }
}

class ReaderThread extends Thread {
  final DataInputStream in;
  private final BlockingQueue<String> queue;

  private static int readIntLE(DataInputStream in) throws IOException {
    int ch1 = in.read();
    int ch2 = in.read();
    int ch3 = in.read();
    int ch4 = in.read();
    if ((ch1 | ch2 | ch3 | ch4) < 0) {
      throw new EOFException();
    } else {
      return (ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0);
    }
  }

  ReaderThread(DataInputStream in, BlockingQueue<String> queue) {
    this.in = in;
    this.queue = queue;
  }

  @Override
  public void run() {
    while (!isInterrupted()) {
      try {
        queue.put(readMessage());
      } catch (IOException e) {
        if (!isInterrupted())
          e.printStackTrace();
        break;
      } catch (InterruptedException e) {
        break;
      }
    }
  }

  private String readMessage() throws IOException {
    int len = readIntLE(in);
    byte[] raw = new byte[len];
    in.readFully(raw, 0, len);
    return new String(raw, StandardCharsets.UTF_8);
  }
}

class WriterThread extends Thread {
  final DataOutputStream out;
  private final BlockingQueue<String> queue;

  private static void writeIntLE(DataOutputStream out, int v) throws IOException {
    out.write(v >>> 0 & 255);
    out.write(v >>> 8 & 255);
    out.write(v >>> 16 & 255);
    out.write(v >>> 24 & 255);
  }

  WriterThread(DataOutputStream out, BlockingQueue<String> queue) {
    this.out = out;
    this.queue = queue;
  }

  @Override
  public void run() {
    while (!isInterrupted()) {
      try {
        if (queue.isEmpty())
          out.flush();
        sendMessage(queue.take());
      } catch (IOException e) {
        if (!isInterrupted())
          e.printStackTrace();
        break;
      } catch (InterruptedException e) {
        break;
      }
    }
  }

  private void sendMessage(String message) throws IOException {
    int len = message.length();
    writeIntLE(out, len);
    out.writeBytes(message);
  }
}
