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

import com.google.gson.JsonObject;
import com.microsoft.playwright.PlaywrightException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.microsoft.playwright.impl.Serialization.gson;

public class PipeTransport implements Transport {
  private final BlockingQueue<JsonObject> incoming = new ArrayBlockingQueue<>(1000);
  private final BlockingQueue<String> outgoing= new ArrayBlockingQueue<>(1000);

  private final ReaderThread readerThread;
  private final WriterThread writerThread;

  private boolean isClosed;

  PipeTransport(InputStream input, OutputStream output) {
    DataInputStream in = new DataInputStream(new BufferedInputStream(input));
    readerThread = new ReaderThread(in, incoming);
    readerThread.start();
    writerThread = new WriterThread(output, outgoing);
    writerThread.start();
  }

  @Override
  public void send(JsonObject message) {
    if (isClosed) {
      throw new PlaywrightException("Playwright connection closed");
    }
    try {
      // We could serialize the message on the IO thread but there is no guarantee
      // that the message object won't be modified on this thread after it's added
      // to the queue.
      outgoing.put(gson().toJson(message));
    } catch (InterruptedException e) {
      throw new PlaywrightException("Failed to send message", e);
    }
  }

  @Override
  public JsonObject poll(Duration timeout) {
    if (isClosed) {
      throw new PlaywrightException("Playwright connection closed");
    }
    try {
      JsonObject message = incoming.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);
      if (message == null && readerThread.exception != null) {
        try {
          close();
        } catch (IOException e) {
          e.printStackTrace(System.err);
        }
        throw new PlaywrightException("Failed to read message from driver, pipe closed.", readerThread.exception);
      }
      return message;
    } catch (InterruptedException e) {
      throw new PlaywrightException("Failed to read message", e);
    }
  }

  @Override
  public void close() throws IOException {
    if (isClosed) {
      return;
    }
    isClosed = true;
    // We interrupt only the outgoing pipe and keep reader thread running as
    // otherwise child process may block on writing to its stdout and never
    // exit (observed on Windows).
    readerThread.isClosing = true;
    writerThread.out.close();
    writerThread.interrupt();
  }
}

class ReaderThread extends Thread {
  private final DataInputStream in;
  private final BlockingQueue<JsonObject> queue;
  volatile boolean isClosing;
  volatile Exception exception;

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

  ReaderThread(DataInputStream in, BlockingQueue<JsonObject> queue) {
    this.in = in;
    this.queue = queue;
  }

  @Override
  public void run() {
    while (!isInterrupted()) {
      try {
        JsonObject message = gson().fromJson(readMessage(), JsonObject.class);
        queue.put(message);
      } catch (IOException e) {
        if (!isInterrupted() && !isClosing) {
          exception = e;
        }
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
  final OutputStream out;
  private final BlockingQueue<String> queue;

  private static void writeIntLE(OutputStream out, int v) throws IOException {
    out.write(v >>> 0 & 255);
    out.write(v >>> 8 & 255);
    out.write(v >>> 16 & 255);
    out.write(v >>> 24 & 255);
  }

  WriterThread(OutputStream out, BlockingQueue<String> queue) {
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
    byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
    writeIntLE(out, bytes.length);
    out.write(bytes);
  }
}
