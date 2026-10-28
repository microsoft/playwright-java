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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * The server side of a WebSocket connection accepted by {@link TestServer}, shaped like a socket
 * of the {@code ws} package: {@link #send}, {@link #close}, {@link #onMessage}, {@link #onClose}.
 * Frames are read on the connection's thread; listeners run on it too.
 */
public final class ServerWebSocket {
  /** A message from the browser: {@code data.toString()} upstream is {@link #text()}. */
  public static final class Message {
    public final byte[] data;
    public final boolean isBinary;

    Message(byte[] data, boolean isBinary) {
      this.data = data;
      this.isBinary = isBinary;
    }

    public String text() {
      return new String(data, StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
      return isBinary ? "binary[" + data.length + "]" : text();
    }
  }

  /** The close code and reason, as the {@code close} event of {@code ws} reports them. */
  public static final class CloseEvent {
    public final int code;
    public final String reason;

    CloseEvent(int code, String reason) {
      this.code = code;
      this.reason = reason;
    }
  }

  private final TestServer.Connection connection;
  private final String protocol;
  private final List<Consumer<Message>> messageListeners = new ArrayList<>();
  private final List<Consumer<CloseEvent>> closeListeners = new ArrayList<>();
  private boolean closeSent;
  private boolean closed;

  ServerWebSocket(TestServer.Connection connection, String protocol) {
    this.connection = connection;
    this.protocol = protocol;
  }

  /** The subprotocol agreed with the browser, or null. */
  public String protocol() {
    return protocol;
  }

  public void send(String text) {
    sendFrame(1, text.getBytes(StandardCharsets.UTF_8));
  }

  public void send(byte[] data) {
    sendFrame(2, data);
  }

  public void onMessage(Consumer<Message> listener) {
    synchronized (messageListeners) {
      messageListeners.add(listener);
    }
  }

  public void onClose(Consumer<CloseEvent> listener) {
    boolean runNow;
    synchronized (closeListeners) {
      runNow = closed;
      if (!closed) {
        closeListeners.add(listener);
      }
    }
    if (runNow) {
      listener.accept(new CloseEvent(1006, ""));
    }
  }

  public void onClose(BiConsumer<Integer, String> listener) {
    onClose(event -> listener.accept(event.code, event.reason));
  }

  /** The next message, the counterpart of {@code ws.once('message', ...)}. */
  public CompletableFuture<Message> waitForMessage() {
    CompletableFuture<Message> result = new CompletableFuture<>();
    onMessage(message -> {
      if (!result.isDone()) {
        result.complete(message);
      }
    });
    return result;
  }

  public CompletableFuture<CloseEvent> waitForClose() {
    CompletableFuture<CloseEvent> result = new CompletableFuture<>();
    onClose(result::complete);
    return result;
  }

  /** Starts the closing handshake with code 1000, like {@code ws.close()}. */
  public void close() {
    close(1000, "");
  }

  public void close(int code, String reason) {
    byte[] reasonBytes = reason.getBytes(StandardCharsets.UTF_8);
    byte[] payload = new byte[2 + reasonBytes.length];
    payload[0] = (byte) (code >> 8);
    payload[1] = (byte) code;
    System.arraycopy(reasonBytes, 0, payload, 2, reasonBytes.length);
    synchronized (this) {
      if (closeSent) {
        return;
      }
      closeSent = true;
    }
    sendFrame(8, payload);
  }

  /** Drops the connection without a closing handshake, like {@code ws.terminate()}. */
  public void terminate() {
    connection.destroy();
  }

  private void sendFrame(int opcode, byte[] payload) {
    ByteArrayOutputStream frame = new ByteArrayOutputStream();
    frame.write(0x80 | opcode);
    if (payload.length < 126) {
      frame.write(payload.length);
    } else if (payload.length < 65536) {
      frame.write(126);
      frame.write(payload.length >> 8);
      frame.write(payload.length);
    } else {
      frame.write(127);
      for (int i = 7; i >= 0; i--) {
        frame.write((int) ((long) payload.length >> (8 * i)));
      }
    }
    frame.write(payload, 0, payload.length);
    connection.write(frame.toByteArray());
  }

  void readLoop(InputStream in) {
    int code = 1006;
    String reason = "";
    try {
      ByteArrayOutputStream fragments = new ByteArrayOutputStream();
      int fragmentsOpcode = 0;
      while (!connection.isClosed()) {
        int first = in.read();
        if (first == -1) {
          break;
        }
        boolean fin = (first & 0x80) != 0;
        int opcode = first & 0x0f;
        int second = in.read();
        if (second == -1) {
          break;
        }
        boolean masked = (second & 0x80) != 0;
        long length = second & 0x7f;
        if (length == 126) {
          length = ((long) in.read() << 8) | in.read();
        } else if (length == 127) {
          length = 0;
          for (int i = 0; i < 8; i++) {
            length = (length << 8) | in.read();
          }
        }
        byte[] mask = new byte[4];
        if (masked) {
          readFully(in, mask);
        }
        byte[] payload = new byte[(int) length];
        readFully(in, payload);
        if (masked) {
          for (int i = 0; i < payload.length; i++) {
            payload[i] ^= mask[i % 4];
          }
        }
        switch (opcode) {
          case 0: // continuation
          case 1: // text
          case 2: // binary
            if (opcode != 0) {
              fragmentsOpcode = opcode;
              fragments.reset();
            }
            fragments.write(payload);
            if (fin) {
              dispatchMessage(new Message(fragments.toByteArray(), fragmentsOpcode == 2));
              fragments.reset();
            }
            break;
          case 8: // close
            if (payload.length >= 2) {
              code = ((payload[0] & 0xff) << 8) | (payload[1] & 0xff);
              reason = new String(payload, 2, payload.length - 2, StandardCharsets.UTF_8);
            } else {
              code = 1005;
            }
            synchronized (this) {
              if (!closeSent) {
                closeSent = true;
                sendFrame(8, payload.length >= 2 ? new byte[]{payload[0], payload[1]} : new byte[0]);
              }
            }
            connection.destroy();
            break;
          case 9: // ping
            sendFrame(10, payload);
            break;
          default:
            break;
        }
      }
    } catch (IOException e) {
      // The browser went away.
    } finally {
      connection.destroy();
      dispatchClose(new CloseEvent(code, reason));
    }
  }

  private static void readFully(InputStream in, byte[] buffer) throws IOException {
    int offset = 0;
    while (offset < buffer.length) {
      int read = in.read(buffer, offset, buffer.length - offset);
      if (read == -1) {
        throw new IOException("Unexpected end of WebSocket frame");
      }
      offset += read;
    }
  }

  private void dispatchMessage(Message message) {
    List<Consumer<Message>> listeners;
    synchronized (messageListeners) {
      listeners = new ArrayList<>(messageListeners);
    }
    for (Consumer<Message> listener : listeners) {
      listener.accept(message);
    }
  }

  private void dispatchClose(CloseEvent event) {
    List<Consumer<CloseEvent>> listeners;
    synchronized (closeListeners) {
      if (closed) {
        return;
      }
      closed = true;
      listeners = new ArrayList<>(closeListeners);
      closeListeners.clear();
    }
    for (Consumer<CloseEvent> listener : listeners) {
      listener.accept(event);
    }
  }
}
