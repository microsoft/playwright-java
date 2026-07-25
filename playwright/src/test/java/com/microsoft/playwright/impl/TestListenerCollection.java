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

import org.junit.jupiter.api.Test;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestListenerCollection {
  @Test
  void shouldContinueNotifyingRemainingListenersWhenOneThrows() {
    ListenerCollection<String> collection = new ListenerCollection<>();
    List<String> received = new ArrayList<>();

    collection.add("event", (Consumer<String>) received::add);
    collection.add("event", (Consumer<String>) v -> {
      throw new RuntimeException("listener boom");
    });
    collection.add("event", (Consumer<String>) received::add);

    // Suppress stderr so the expected listener error doesn't pollute test output.
    PrintStream prevErr = System.err;
    System.setErr(new PrintStream(new java.io.ByteArrayOutputStream()));
    try {
      assertDoesNotThrow(() -> collection.notify("event", "hello"));
    } finally {
      System.setErr(prevErr);
    }

    // Both the listener before and after the throwing one must be called.
    assertEquals(2, received.size());
    assertEquals("hello", received.get(0));
    assertEquals("hello", received.get(1));
  }

  @Test
  void shouldNotThrowWhenSingleListenerThrows() {
    ListenerCollection<String> collection = new ListenerCollection<>();
    collection.add("event", (Consumer<String>) v -> {
      throw new RuntimeException("listener boom");
    });

    PrintStream prevErr = System.err;
    System.setErr(new PrintStream(new java.io.ByteArrayOutputStream()));
    try {
      assertDoesNotThrow(() -> collection.notify("event", "hello"));
    } finally {
      System.setErr(prevErr);
    }
  }

  @Test
  void shouldNotThrowWhenNoListeners() {
    ListenerCollection<String> collection = new ListenerCollection<>();
    assertDoesNotThrow(() -> collection.notify("event", "hello"));
  }

  @Test
  void shouldPrintListenerExceptionToStderrForObservability() {
    // Mirrors the JS client where async listener rejections surface as
    // unhandled rejections on stderr by default.
    ListenerCollection<String> collection = new ListenerCollection<>();
    collection.add("event", (Consumer<String>) v -> {
      throw new RuntimeException("listener boom");
    });

    java.io.ByteArrayOutputStream captured = new java.io.ByteArrayOutputStream();
    PrintStream prevErr = System.err;
    System.setErr(new PrintStream(captured));
    try {
      collection.notify("event", "hello");
    } finally {
      System.setErr(prevErr);
    }
    String stderr = captured.toString();
    assertTrue(stderr.contains("[playwright]") && stderr.contains("listener boom"),
      "stderr should contain the listener exception, got: " + stderr);
  }
}
