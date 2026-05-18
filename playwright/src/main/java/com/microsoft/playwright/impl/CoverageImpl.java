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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.microsoft.playwright.CDPSession;
import com.microsoft.playwright.Coverage;
import com.microsoft.playwright.PlaywrightException;

import java.util.*;

class CoverageImpl implements Coverage {
  private final CSSCoverage cssCoverage;
  private final JSCoverage jsCoverage;

  CoverageImpl(PageImpl page) {
    this.cssCoverage = new CSSCoverage(page);
    this.jsCoverage = new JSCoverage(page);
  }


  @Override
  public void startCSSCoverage() {
    this.cssCoverage.start();
  }

  @Override
  public void startCSSCoverage(CoverageCSSOptions options) {
    this.cssCoverage.start(options);
  }

  @Override
  public CoverageReport stopCSSCoverage() {
    return this.cssCoverage.stop();
  }

  @Override
  public void startJSCoverage() {
    this.jsCoverage.start();
  }

  @Override
  public void startJSCoverage(CoverageJSOptions options) {
    this.jsCoverage.start(options);
  }

  @Override
  public CoverageReport stopJSCoverage() {
    return this.jsCoverage.stop();
  }
}

class CSSCoverage {
  private final PageImpl page;
  private final Map<String, String> stylesheetURLs = new HashMap<>();
  private final Map<String, JsonElement> stylesheetSources = new HashMap<>();
  private CDPSession cdpSession;
  private Boolean enabled = false;
  private Coverage.CoverageCSSOptions options;

  CSSCoverage(PageImpl page) {
    this.page = page;
    this.options = new Coverage.CoverageCSSOptions().setResetOnNavigation(true);
  }

  void start(Coverage.CoverageCSSOptions options) {
    this.options = options;
    this.start();
  }

  void start() {
    if (Boolean.TRUE.equals(enabled)) {
      throw new PlaywrightException("CSSCoverage is already enabled");
    }
    this.enabled = true;
    this.stylesheetURLs.clear();
    this.stylesheetSources.clear();
    this.cdpSession = this.page.context().newCDPSession(this.page);

    cdpSession.on("CSS.styleSheetAdded", this::onStyleSheet);
    cdpSession.on("Runtime.executionContextsCleared", this::onExecutionContextsCleared);

    cdpSession.send("DOM.enable");
    cdpSession.send("CSS.enable");
    cdpSession.send("CSS.startRuleUsageTracking");
  }

  private void onStyleSheet(JsonObject event) {
    JsonObject header = event.get("header").getAsJsonObject();
    String sourceURL = header.get("url").getAsString();
    boolean urlEmpty = sourceURL == null || sourceURL.isEmpty();
    if (urlEmpty)
      return;

    String stylesheetID = header.get("styleSheetId").getAsString();
    JsonObject params = new JsonObject();
    params.addProperty("styleSheetId", stylesheetID);
    JsonObject scriptResult = cdpSession.send("CSS.getStyleSheetText", params);
    if (scriptResult != null) {
      this.stylesheetURLs.put(stylesheetID, sourceURL);
      this.stylesheetSources.put(stylesheetID, scriptResult.get("text"));
    }
  }

  private void onExecutionContextsCleared(JsonObject event) {
    if (!this.options.resetOnNavigation)
      return;
    this.stylesheetURLs.clear();
    this.stylesheetSources.clear();
  }

  Coverage.CoverageReport stop() {
    if (this.enabled == Boolean.FALSE)
      return new Coverage.CoverageReport();

    try {
      JsonObject result = cdpSession.send("CSS.stopRuleUsageTracking");
      cdpSession.send("Profiler.stopPreciseCoverage");
      cdpSession.send("CSS.disable");
      cdpSession.send("DOM.disable");

      cdpSession.off("CSS.styleSheetAdded", this::onStyleSheet);
      cdpSession.off("Runtime.executionContextsCleared", this::onExecutionContextsCleared);
      cdpSession.detach();
      this.enabled = Boolean.FALSE;

      JsonArray scripts = result.getAsJsonArray("ruleUsage");
      Map<String, JsonArray> styleSheetIdToCoverage = new HashMap<>();
      if (scripts != null && !scripts.isEmpty()) {
        for (JsonElement element : scripts) {
          JsonObject entry = element.getAsJsonObject();
          String styleSheetId = entry.get("styleSheetId").getAsString();
          JsonArray ranges = styleSheetIdToCoverage.computeIfAbsent(styleSheetId, k -> new JsonArray());

          JsonObject range = new JsonObject();
          range.addProperty("startOffset", entry.get("startOffset").getAsInt());
          range.addProperty("endOffset", entry.get("endOffset").getAsInt());
          range.addProperty("count", entry.get("used").getAsBoolean() ? 1 : 0);
          ranges.add(range);
        }
      }
      Coverage.CoverageReport coverageReport = new Coverage.CoverageReport();
      for (Map.Entry<String, String> mapEntry : this.stylesheetURLs.entrySet()) {
        JsonElement text = this.stylesheetSources.get(mapEntry.getKey());
        JsonArray ranges = convertToDisjointRanges(styleSheetIdToCoverage.getOrDefault(mapEntry.getKey(), new JsonArray()));
        JsonObject coverage = new JsonObject();
        coverage.addProperty("url", mapEntry.getKey());
        coverage.add("ranges", ranges);
        coverage.add("text", text);
        coverageReport.addEntry(coverage);
      }
      return coverageReport;
    } catch (Exception e) {
      throw new PlaywrightException("Failed to gather JS coverage report", e);
    }
  }

  private JsonArray convertToDisjointRanges(JsonArray nestedRanges) {
    List<JsonObject> points = new ArrayList<>();
    for (JsonElement element : nestedRanges) {
      JsonObject range = element.getAsJsonObject();
      JsonObject point1 = new JsonObject();
      point1.addProperty("offset", range.get("startOffset").getAsInt());
      point1.addProperty("type", 0);
      point1.add("range", range);
      points.add(point1);
      JsonObject point2 = new JsonObject();
      point2.addProperty("offset", range.get("endOffset").getAsInt());
      point2.addProperty("type", 1);
      point2.add("range", range);
      points.add(point2);
    }
    points.sort((a, b) -> {
      if (a.get("offset").getAsInt() != b.get("offset").getAsInt())
        return a.get("offset").getAsInt() - b.get("offset").getAsInt();
      if (a.get("type").getAsInt() != b.get("type").getAsInt())
        return b.get("type").getAsInt() - a.get("type").getAsInt();
      JsonObject aRange = a.get("range").getAsJsonObject();
      JsonObject bRange = b.get("range").getAsJsonObject();
      int aLength = aRange.get("endOffset").getAsInt() - aRange.get("startOffset").getAsInt();
      int bLength = bRange.get("endOffset").getAsInt() - bRange.get("startOffset").getAsInt();
      if (a.get("type").getAsInt() == 0)
        return bLength - aLength;
      return aLength - bLength;
    });

    Deque<Integer> hitCountStack = new ArrayDeque<>();
    List<JsonObject> results = new ArrayList<>();
    int lastOffset = 0;
    for (JsonObject point : points) {
      int offset = point.get("offset").getAsInt();
      if (!hitCountStack.isEmpty() && lastOffset < offset && hitCountStack.getLast() > 0) {
        JsonObject lastResult = results.isEmpty() ? null : results.get(results.size() - 1).getAsJsonObject();
        if (lastResult != null && lastResult.get("end").getAsInt() == lastOffset) {
          lastResult.remove("end");
          lastResult.addProperty("end", offset);
        } else {
          JsonObject result = new JsonObject();
          result.addProperty("start", lastOffset);
          result.addProperty("end", offset);
          results.add(result);
        }
      }
      lastOffset = offset;
      if (point.get("type").getAsInt() == 0) {
        JsonObject range = point.get("range").getAsJsonObject();
        hitCountStack.push(range.get("count").getAsInt());
      } else {
        hitCountStack.pop();
      }
    }
    return results.stream().filter(result -> result.get("end").getAsInt() - result.get("start").getAsInt() > 1)
      .collect(JsonArray::new, JsonArray::add, JsonArray::addAll);
  }
}

class JSCoverage {
  private final PageImpl page;
  private final Set<String> scriptsId = new HashSet<>();
  private final Map<String, JsonElement> scriptsSources = new HashMap<>();
  private CDPSession cdpSession;
  private Boolean enabled = false;
  private Coverage.CoverageJSOptions options;

  JSCoverage(PageImpl page) {
    this.page = page;
    this.options = new Coverage.CoverageJSOptions().setResetOnNavigation(true).setReportAnonymousScripts(false);
  }

  void start(Coverage.CoverageJSOptions options) {
    this.options = options;
    this.start();
  }

  void start() {
    if (Boolean.TRUE.equals(enabled)) {
      throw new PlaywrightException("JSCoverage is already enabled");
    }
    this.enabled = true;
    this.scriptsId.clear();
    this.scriptsSources.clear();
    this.cdpSession = this.page.context().newCDPSession(this.page);

    cdpSession.on("Debugger.scriptParsed", this::onScriptParsed);
    cdpSession.on("Runtime.executionContextsCleared", this::onExecutionContextsCleared);
    cdpSession.on("Debugger.paused", this::onDebuggerPaused);

    cdpSession.send("Profiler.enable");
    JsonObject profilerParams = new JsonObject();
    profilerParams.addProperty("callCount", true);
    profilerParams.addProperty("detailed", true);
    cdpSession.send("Profiler.startPreciseCoverage", profilerParams);

    cdpSession.send("Debugger.enable");
    JsonObject debuggerParam = new JsonObject();
    debuggerParam.addProperty("skip", true);
    cdpSession.send("Debugger.setSkipAllPauses", debuggerParam);
  }

  private void onScriptParsed(JsonObject event) {
    String scriptId = event.get("scriptId").getAsString();
    String scriptUrl = event.get("url").getAsString();
    boolean urlEmpty = scriptUrl == null || scriptUrl.isEmpty();
    if (urlEmpty && !this.options.reportAnonymousScripts)
      return;
    if (scriptsId.contains(scriptId))
      return;

    scriptsId.add(scriptId);
    JsonObject scriptParams = new JsonObject();
    scriptParams.addProperty("scriptId", scriptId);
    JsonObject scriptResult = cdpSession.send("Debugger.getScriptSource", scriptParams);
    Optional.ofNullable(scriptResult).map(object -> object.get("scriptSource"))
      .ifPresent(scriptSource -> scriptsSources.put(scriptId, scriptSource));
  }

  private void onExecutionContextsCleared(JsonObject event) {
    if (!this.options.resetOnNavigation)
      return;
    this.scriptsId.clear();
    this.scriptsSources.clear();
  }

  private void onDebuggerPaused(JsonObject event) {
    cdpSession.send("Debugger.resume");
  }

  Coverage.CoverageReport stop() {
    if (this.enabled == Boolean.FALSE)
      return new Coverage.CoverageReport();

    try {
      JsonObject result = cdpSession.send("Profiler.takePreciseCoverage");
      cdpSession.send("Profiler.stopPreciseCoverage");
      cdpSession.send("Profiler.disable");
      cdpSession.send("Debugger.disable");

      cdpSession.off("Debugger.scriptParsed", this::onScriptParsed);
      cdpSession.off("Runtime.executionContextsCleared", this::onExecutionContextsCleared);
      cdpSession.off("Debugger.paused", this::onDebuggerPaused);
      cdpSession.detach();
      this.enabled = Boolean.FALSE;

      JsonArray scripts = result.getAsJsonArray("result");
      Coverage.CoverageReport coverageReport = new Coverage.CoverageReport();
      if (scripts != null && !scripts.isEmpty()) {
        for (JsonElement element : scripts) {
          JsonObject entry = element.getAsJsonObject();
          String scriptId = entry.get("scriptId").getAsString();
          if (!scriptsSources.containsKey(scriptId))
            continue;
          JsonElement source = scriptsSources.get(scriptId);
          if (source != null)
            entry.add("source", scriptsSources.get(scriptId));
          coverageReport.addEntry(entry);
        }
      }
      return coverageReport;
    } catch (Exception e) {
      throw new PlaywrightException("Failed to gather JS coverage report", e);
    }
  }
}
