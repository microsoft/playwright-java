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

// Port of isomorphic/manualPromise.ts.
package com.microsoft.playwright.impl;

import com.microsoft.playwright.PlaywrightException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// java: no ManualPromise class, a WaitableResult stands for it (resolve is complete()). An async race returns the
// race as a Waitable, which race(), safeRace() and raceMultiple() pump the connection of the owner for; disposing
// it is the finally block of _race.
// java: stays in impl, not in impl.isomorphic: it is built on ChannelOwner and the Waitable primitives.
class LongStandingScope {
  // java: the owner whose connection a race pumps.
  private final ChannelOwner owner;
  private PlaywrightException terminateError;
  private PlaywrightException closeError;
  // java: the frames are a Java stack trace.
  private final Map<WaitableResult<PlaywrightException>, StackTraceElement[]> terminatePromises = new LinkedHashMap<>();
  private boolean isClosed = false;

  LongStandingScope(ChannelOwner owner) {
    this.owner = owner;
  }

  void reject(PlaywrightException error) {
    isClosed = true;
    terminateError = error;
    for (WaitableResult<PlaywrightException> p : new ArrayList<>(terminatePromises.keySet()))
      p.complete(error);
  }

  void close(PlaywrightException error) {
    isClosed = true;
    closeError = error;
    for (Map.Entry<WaitableResult<PlaywrightException>, StackTraceElement[]> entry : new ArrayList<>(terminatePromises.entrySet()))
      entry.getKey().complete(cloneError(error, entry.getValue()));
  }

  boolean isClosed() {
    return isClosed;
  }

  // java: Promise.race of the s.race(promise) calls is one WaitableRace of their races, s._race([promise], false).
  static <T> T raceMultiple(List<LongStandingScope> scopes, Waitable<T> promise) {
    List<Waitable<T>> races = new ArrayList<>();
    for (LongStandingScope s : scopes)
      races.add(s.race(Collections.singletonList(promise), false, null));
    try {
      return scopes.get(0).owner.runUntil(() -> {}, new WaitableRace<>(races));
    } finally {
      for (Waitable<T> race : races)
        race.dispose();
    }
  }

  // java: the promise | promise[] parameter is an overload.
  <T> T race(Waitable<T> promise) {
    return race(Collections.singletonList(promise));
  }

  <T> T race(List<Waitable<T>> promise) {
    return await(race(promise, false, null));
  }

  <T> T safeRace(Waitable<T> promise) {
    return safeRace(promise, null);
  }

  <T> T safeRace(Waitable<T> promise, T defaultValue) {
    return await(race(Collections.singletonList(promise), true, defaultValue));
  }

  private <T> Waitable<T> race(List<Waitable<T>> promises, boolean safe, T defaultValue) {
    WaitableResult<PlaywrightException> terminatePromise = new WaitableResult<>();
    StackTraceElement[] frames = new Exception().getStackTrace();
    if (terminateError != null)
      terminatePromise.complete(terminateError);
    if (closeError != null)
      terminatePromise.complete(cloneError(closeError, frames));
    terminatePromises.put(terminatePromise, frames);
    List<Waitable<T>> race = new ArrayList<>();
    // java: the promises go first, a settled promise wins over a closed scope as its reaction runs first upstream.
    race.addAll(promises);
    race.add(new Waitable<T>() {
      @Override
      public boolean isDone() {
        return terminatePromise.isDone();
      }

      @Override
      public T get() {
        if (safe)
          return defaultValue;
        throw terminatePromise.get();
      }

      @Override
      public void dispose() {
        terminatePromises.remove(terminatePromise);
      }
    });
    return new WaitableRace<>(race);
  }

  // java: no signalToPromise, it serves AbortSignal.

  // java: a new exception of the same type, with the frames of the race as its stack trace.
  private static PlaywrightException cloneError(PlaywrightException error, StackTraceElement[] frames) {
    PlaywrightException clone = error instanceof TargetClosedError ? new TargetClosedError(error.getMessage()) : new PlaywrightException(error.getMessage());
    clone.setStackTrace(frames);
    return clone;
  }

  // java: members with no counterpart upstream.

  // java: await of a race; the finally block of _race disposes it.
  private <T> T await(Waitable<T> race) {
    try {
      return owner.runUntil(() -> {}, race);
    } finally {
      race.dispose();
    }
  }

  // java: the terminate promise of a race without promises, raced by PageImpl.createWaitForCloseHelper().
  <T> Waitable<T> terminatePromise(boolean safe) {
    return race(new ArrayList<>(), safe, null);
  }
}
