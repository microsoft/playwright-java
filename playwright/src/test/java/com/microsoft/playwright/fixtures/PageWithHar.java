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

package com.microsoft.playwright.fixtures;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.HarContentPolicy;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * The {@code pageWithHar} fixture: a page in a context that records a HAR file, read back with
 * {@link Result#getLog()} or {@link Result#getZip()}.
 */
public final class PageWithHar {
  public static final class Options {
    public String outputPath;
    public HarContentPolicy content;
    public Boolean omitContent;

    public Options setOutputPath(String outputPath) {
      this.outputPath = outputPath;
      return this;
    }

    public Options setContent(HarContentPolicy content) {
      this.content = content;
      return this;
    }

    public Options setOmitContent(boolean omitContent) {
      this.omitContent = omitContent;
      return this;
    }
  }

  public static final class Result {
    public final BrowserContext context;
    public final Page page;
    public final Path harPath;

    Result(BrowserContext context, Page page, Path harPath) {
      this.context = context;
      this.page = page;
      this.harPath = harPath;
    }

    /** Closes the context and returns the {@code log} object of the HAR file. */
    public JsonObject getLog() {
      context.close();
      try {
        String json = new String(Files.readAllBytes(harPath), StandardCharsets.UTF_8);
        return JsonParser.parseString(json).getAsJsonObject().getAsJsonObject("log");
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }

    /** Closes the context and returns the entries of the HAR zip. */
    public Map<String, byte[]> getZip() {
      context.close();
      return Utils.parseZip(harPath);
    }
  }

  private final ContextFactory contextFactory;
  private final TestInfo testInfo;

  PageWithHar(ContextFactory contextFactory, TestInfo testInfo) {
    this.contextFactory = contextFactory;
    this.testInfo = testInfo;
  }

  public Result create() {
    return create(new Options());
  }

  public Result create(Options options) {
    Path harPath = testInfo.outputPath(options.outputPath == null ? "test.har" : options.outputPath);
    Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
      .setRecordHarPath(harPath)
      .setIgnoreHTTPSErrors(true);
    if (options.content != null) {
      contextOptions.setRecordHarContent(options.content);
    }
    if (options.omitContent != null) {
      contextOptions.setRecordHarOmitContent(options.omitContent);
    }
    BrowserContext context = contextFactory.newContext(contextOptions);
    Page page = context.newPage();
    return new Result(context, page, harPath);
  }
}
