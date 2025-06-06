package com.microsoft.playwright;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestAndroid extends TestAndroidBase {
  @Test
  void testAndroidDeviceClose() {
    List<String> events = new ArrayList<>();
    androidDevice.onClose(d -> {
      events.add("close");
    });
    androidDevice.close();
    assertEquals(Arrays.asList("close"), events);
  }
}
