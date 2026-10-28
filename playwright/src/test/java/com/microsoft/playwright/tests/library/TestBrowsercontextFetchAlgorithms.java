/**
 * Copyright (c) 2014-present Matt Zabriskie
 * Modifications copyright (c) Microsoft Corporation.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.microsoft.playwright.tests.library;

import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.fixtures.PlaywrightTest;
import com.microsoft.playwright.fixtures.TestServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Port of tests/library/browsercontext-fetch-algorithms.spec.ts. */
public class TestBrowsercontextFetchAlgorithms {
  @Nested
  class Algorithms {
    private final String responseBody = "str";

    @Nested
    @ParameterizedClass
    @ValueSource(strings = {"gzip", "deflate", "br"})
    class Decompression {
      @Parameter
      String type;

      private byte[] zipped() {
        try {
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          byte[] body = responseBody.getBytes(StandardCharsets.UTF_8);
          if (type.equals("gzip")) {
            try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
              gzip.write(body);
            }
          } else if (type.equals("deflate")) {
            try (DeflaterOutputStream deflate = new DeflaterOutputStream(out)) {
              deflate.write(body);
            }
          } else {
            // Java: the JDK has no brotli; these are the bytes of zlib.brotliCompress('str').
            return new byte[]{11, 1, -128, 115, 116, 114, 3};
          }
          return out.toByteArray();
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      }

      @PlaywrightTest
      @DisplayName("FE-180 should support decompression")
      void shouldSupportDecompression(BrowserContext context, TestServer server) {
        byte[] zipped = zipped();
        server.setRoute("/compressed", (req, res) -> {
          res.setHeader("Content-Encoding", type);
          res.end(zipped);
        });

        APIResponse response = context.request().get(server.PREFIX + "/compressed");
        assertEquals(responseBody, response.text());
      }

      @PlaywrightTest
      @DisplayName("FE-181 should not fail if response content-length header is missing (${type})")
      void shouldNotFailIfResponseContentLengthHeaderIsMissingType(BrowserContext context, TestServer server) {
        byte[] zipped = zipped();
        server.setRoute("/compressed", (req, res) -> {
          res.setHeader("Content-Encoding", type);
          res.removeHeader("Content-Length");
          res.end(zipped);
        });

        APIResponse response = context.request().get(server.PREFIX + "/compressed");
        assertEquals(responseBody, response.text());
      }

      @PlaywrightTest
      @DisplayName("FE-182 should not fail with chunked responses (without Content-Length header)")
      void shouldNotFailWithChunkedResponsesWithoutContentLengthHeader(BrowserContext context, TestServer server) {
        byte[] zipped = zipped();
        server.setRoute("/compressed", (req, res) -> {
          res.setHeader("Content-Encoding", type);
          res.setHeader("Transfer-Encoding", "chunked");
          res.removeHeader("Content-Length");
          res.write(zipped);
          res.end();
        });

        APIResponse response = context.request().get(server.PREFIX + "/compressed");
        assertEquals(responseBody, response.text());
      }

      @PlaywrightTest
      @DisplayName("FE-183 should not fail with an empty response without content-length header (Z_BUF_ERROR)")
      void shouldNotFailWithAnEmptyResponseWithoutContentLengthHeaderZBUFERROR(BrowserContext context, TestServer server) {
        server.setRoute("/compressed", (req, res) -> {
          res.setHeader("Content-Encoding", type);
          res.removeHeader("Content-Length");
          res.end();
        });

        APIResponse response = context.request().get(server.PREFIX + "/compressed");
        assertEquals("", response.text());
      }

      @PlaywrightTest
      @DisplayName("FE-184 should not fail with an empty response with content-length header (Z_BUF_ERROR)")
      void shouldNotFailWithAnEmptyResponseWithContentLengthHeaderZBUFERROR(BrowserContext context, TestServer server) {
        server.setRoute("/compressed", (req, res) -> {
          res.setHeader("Content-Encoding", type);
          res.end();
        });

        context.request().get(server.PREFIX + "/compressed");
      }
    }
  }
}
