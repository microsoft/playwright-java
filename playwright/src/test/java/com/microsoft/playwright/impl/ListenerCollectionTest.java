package com.microsoft.playwright.impl;

import org.junit.jupiter.api.Test;

class ListenerCollectionTest {

  @Test
  void shouldHandleNullListener() {
    ListenerCollection<BrowserImpl.EventType> listener = new ListenerCollection<>();

    listener.add(BrowserImpl.EventType.DISCONNECTED, null);

    listener.notify(BrowserImpl.EventType.DISCONNECTED, new Object());
  }
}
