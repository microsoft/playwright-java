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
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

class WritableStream extends ChannelOwner {
  WritableStream(ChannelOwner parent, String type, String guid, JsonObject initializer) {
    super(parent, type, guid, initializer);
  }

  OutputStream stream() {
    return new OutputStream() {
      @Override
      public void write(int b) throws IOException {
        write(new byte[] { (byte) b });
      }

      @Override
      public void write(byte[] b, int off, int len) throws IOException {
        JsonObject params = new JsonObject();
        ByteBuffer buffer = ByteBuffer.wrap(b, off, len);
        ByteBuffer encoded = Base64.getEncoder().encode(buffer);
        params.addProperty("binary", new String(encoded.array(), StandardCharsets.UTF_8));
        sendMessage("write", params, NO_TIMEOUT);
      }

      @Override
      public void close() throws IOException {
        super.close();
        sendMessage("close");
      }
    };
  }
}
