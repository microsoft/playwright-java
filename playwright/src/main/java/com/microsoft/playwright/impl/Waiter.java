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
import com.microsoft.playwright.TimeoutError;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.microsoft.playwright.impl.Utils.createGuid;

// Port of waiter.ts. java: promises are Waitables, the API thread pumps the connection in waitForPromise(),
// there are no zones to save and no AbortSignal.
class Waiter {
  private final List<Runnable> dispose = new ArrayList<>();
  private final List<Waitable<?>> failures = new ArrayList<>();
  private RuntimeException immediateError;
  private final List<String> logs = new ArrayList<>();
  private final ChannelOwner channelOwner;
  private final String waitId;
  private String error;
  private boolean disposed;

  Waiter(ChannelOwner channelOwner, String event) {
    this.waitId = createGuid();
    this.channelOwner = channelOwner;

    String title = "Wait for event \"" + event + "\"";
    JsonObject info = new JsonObject();
    info.addProperty("waitId", waitId);
    info.addProperty("phase", "before");
    info.addProperty("event", event);
    sendWaitInfo(info, title, false);
    dispose.add(() -> {
      JsonObject after = new JsonObject();
      after.addProperty("waitId", waitId);
      after.addProperty("phase", "after");
      if (error != null) {
        after.addProperty("error", error);
      }
      sendWaitInfo(after, null, true);
    });
  }

  static Waiter createForEvent(ChannelOwner channelOwner, String event) {
    return new Waiter(channelOwner, event);
  }

  private void sendWaitInfo(JsonObject info, String title, boolean internal) {
    // Fire-and-forget: server intentionally never replies, and we never throw to the caller.
    try {
      channelOwner.wrapApiCall(() -> {
        ApiZone apiZone = channelOwner.connection.apiZone;
        if (!apiZone.internal && !apiZone.reported && apiZone.title == null) {
          // An outer wrapApiCall may have set its own title (e.g. "Wait for navigation");
          // fall back to ours only when the outer zone left it blank.
          apiZone.title = title;
        }
        // sendMessageNoReply reports the call when it is the first message of the zone and sends it
        // as internal otherwise.
        channelOwner.sendMessageNoReply("__waitInfo__", info);
      }, title, internal);
    } catch (RuntimeException e) {
      // Fire-and-forget: never throw to the caller.
    }
  }

  <T> Waitable<T> waitForEvent(EventEmitter emitter, String event, Predicate<T> predicate) {
    return new WaitableEvent<>(emitter, event, predicate);
  }

  void rejectOnEvent(EventEmitter emitter, String event, Supplier<RuntimeException> error) {
    rejectOnEvent(emitter, event, error, null);
  }

  <T> void rejectOnEvent(EventEmitter emitter, String event, Supplier<RuntimeException> error, Predicate<T> predicate) {
    rejectOn(new WaitableEvent<T>(emitter, event, predicate) {
      @Override
      public T get() {
        throw error.get();
      }
    });
  }

  void rejectOnTimeout(Double timeout, String timeoutMessage) {
    if (timeout != null && timeout != 0) {
      rejectOn(new WaitableTimeout<Object>(timeout) {
        @Override
        public Object get() {
          throw new TimeoutError(timeoutMessage);
        }
      });
    }
  }

  void rejectImmediately(RuntimeException error) {
    immediateError = error;
  }

  void throwIfImmediatelyRejected() {
    if (immediateError != null) {
      throw immediateError;
    }
  }

  void dispose() {
    // java: the "after" message goes out once, a failed wait disposes the waiter and the caller does it again.
    if (disposed) {
      return;
    }
    disposed = true;
    for (Runnable d : dispose) {
      d.run();
    }
  }

  <T> T waitForPromise(Waitable<T> waitable) {
    return waitForPromise(waitable, null);
  }

  @SuppressWarnings("unchecked")
  private <T> T waitForPromise(Waitable<T> waitable, Runnable code) {
    if (code != null) {
      try {
        code.run();
      } catch (RuntimeException e) {
        // java: the callback is the caller's own code, its exception is not a failure of the wait.
        waitable.dispose();
        error = e.getMessage();
        dispose();
        throw e;
      }
    }
    try {
      throwIfImmediatelyRejected();
      List<Waitable<T>> race = new ArrayList<>();
      race.add(waitable);
      for (Waitable<?> failure : failures) {
        // The failures stay armed for the next waitForPromise(), only dispose() releases them.
        race.add(new Undisposable<>((Waitable<T>) failure));
      }
      // The waitable is disposed by runUntil in any case, as the dispose() argument of waitForPromise upstream.
      return channelOwner.runUntil(() -> {}, new WaitableRace<>(race));
    } catch (RuntimeException e) {
      waitable.dispose();
      error = e.getMessage();
      dispose();
      throw rewriteErrorMessage(e);
    }
  }

  void log(String s) {
    logs.add(s);
    LoggingSupport.logApiIfEnabled(s);
    JsonObject info = new JsonObject();
    info.addProperty("waitId", waitId);
    info.addProperty("phase", "log");
    info.addProperty("message", s);
    sendWaitInfo(info, null, true);
  }

  private void rejectOn(Waitable<?> failure) {
    failures.add(failure);
    dispose.add(failure::dispose);
  }

  // java: exceptions are immutable, so a new one of the same class carries the message with the logs.
  private RuntimeException rewriteErrorMessage(RuntimeException e) {
    String recording = formatLogRecording(logs);
    if (recording.isEmpty()) {
      return e;
    }
    String message = e.getMessage() + recording;
    if (e instanceof TimeoutError) {
      return new TimeoutError(message, e);
    }
    if (e instanceof TargetClosedError) {
      return new TargetClosedError(message, e);
    }
    if (e.getClass() == PlaywrightException.class) {
      return new PlaywrightException(message, e);
    }
    return e;
  }

  static String formatLogRecording(List<String> log) {
    if (log.isEmpty()) {
      return "";
    }
    String header = " logs ";
    int headerLength = 60;
    int leftLength = (headerLength - header.length()) / 2;
    int rightLength = headerLength - header.length() - leftLength;
    return "\n" + repeat('=', leftLength) + header + repeat('=', rightLength) + "\n" + String.join("\n", log) + "\n" + repeat('=', headerLength);
  }

  private static String repeat(char c, int n) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < n; i++) {
      sb.append(c);
    }
    return sb.toString();
  }

  private static class Undisposable<T> implements Waitable<T> {
    private final Waitable<T> delegate;

    Undisposable(Waitable<T> delegate) {
      this.delegate = delegate;
    }

    @Override
    public boolean isDone() {
      return delegate.isDone();
    }

    @Override
    public T get() {
      return delegate.get();
    }

    @Override
    public void dispose() {
    }
  }

  // java: the callback-style API. Subscribes first, then runs the code, then waits.
  <T> T waitForEvent(EventEmitter emitter, String event, Predicate<T> predicate, Runnable code) {
    return waitForPromise(waitForEvent(emitter, event, predicate), code);
  }
}
