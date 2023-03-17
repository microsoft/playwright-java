package com.microsoft.playwright.impl;

import java.util.function.BooleanSupplier;

class WaitablePredicate<T> implements Waitable<T> {
  private final BooleanSupplier predicate;

  WaitablePredicate(BooleanSupplier predicate) {
    this.predicate = predicate;
  }

  @Override
  public boolean isDone() {
    return predicate.getAsBoolean();
  }

  @Override
  public T get() {
    return null;
  }

  @Override
  public void dispose() {
  }
}
