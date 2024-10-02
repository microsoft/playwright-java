package com.microsoft.playwright.impl;

import com.microsoft.playwright.WebSocketFrame;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

class WebSocketFrameImpl implements WebSocketFrame {
  private byte[] bytes;
  private String text;

  WebSocketFrameImpl(String payload, boolean isBase64) {
    if (isBase64) {
      bytes = Base64.getDecoder().decode(payload);
    } else {
      text = payload;
    }
  }

  @Override
  public byte[] binary() {
    return bytes;
  }

  @Override
  public String text() {
    return text;
  }
}
