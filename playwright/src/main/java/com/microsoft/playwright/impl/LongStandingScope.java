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
import java.util.List;

// java: only the LongStandingScope class, and only the members the client uses (no reject, raceMultiple or the
// safeRace default value). There are no terminate promises to resolve: a race is a Waitable that is done once
// the scope is closed, and the owner pumps the connection while it waits.
// java: stays in impl, not in impl.isomorphic: it is built on ChannelOwner and the Waitable primitives.
class LongStandingScope {
  private final ChannelOwner owner;
  private PlaywrightException closeError;
  private boolean isClosed;

  LongStandingScope(ChannelOwner owner) {
    this.owner = owner;
  }

  void close(PlaywrightException error) {
    isClosed = true;
    closeError = error;
  }

  <T> T race(Waitable<T> promise) {
    return race(promise, false);
  }

  <T> T safeRace(Waitable<T> promise) {
    return race(promise, true);
  }

  private <T> T race(Waitable<T> promise, boolean safe) {
    Waitable<T> terminatePromise = terminatePromise(safe);
    List<Waitable<T>> promises = new ArrayList<>();
    // java: the promise goes first, a settled promise wins over a closed scope as its reaction runs first upstream.
    promises.add(promise);
    promises.add(terminatePromise);
    return owner.runUntil(() -> {}, new WaitableRace<>(promises));
  }

  // java: terminatePromise of _race, also raced by PageImpl.createWaitForCloseHelper(); a safe one returns null.
  <T> Waitable<T> terminatePromise(boolean safe) {
    return new Waitable<T>() {
      @Override
      public boolean isDone() {
        return isClosed;
      }

      @Override
      public T get() {
        if (safe)
          return null;
        throw cloneError(closeError);
      }

      @Override
      public void dispose() {
      }
    };
  }

  // java: a new exception of the same type, so that the stack trace points at the caller of the race.
  private static PlaywrightException cloneError(PlaywrightException error) {
    if (error instanceof TargetClosedError)
      return new TargetClosedError(error.getMessage());
    return new PlaywrightException(error.getMessage());
  }
}
