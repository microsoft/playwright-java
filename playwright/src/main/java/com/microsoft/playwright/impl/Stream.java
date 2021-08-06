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

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

public class Stream extends ChannelOwner {
  private final InputStream stream = new InputStreamImpl();
  public Stream(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  InputStream stream() {
    return stream;
  }

  private class InputStreamImpl extends InputStream {
    @Override
    public int read() throws IOException {
      byte[] b = {0};
      int result = read(b, 0, 1);
      if (result == -1) {
        return result;
      }
      return 0xFF & b[0];
    }

    @Override
    public int read(byte[] b, int off, int len) {
      if (len == 0) {
        return 0;
      }
      JsonObject params = new JsonObject();
      params.addProperty("size", len);
      JsonObject json = sendMessage("read", params).getAsJsonObject();
      String encoded = json.get("binary").getAsString();
      if (encoded.isEmpty()) {
        return -1;
      }
      byte[] buffer = Base64.getDecoder().decode(encoded);
      for (int i = 0; i < buffer.length;) {
        b[off++] = buffer[i++];
      }
      return buffer.length;
    }

    @Override
    public void close() throws IOException {
      super.close();
      sendMessage("close");
    }
  }
}
