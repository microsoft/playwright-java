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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The fixture values of one test or worker, keyed by fixture identity and closed newest first.
 */
final class FixtureValues {
  private static final class Entry {
    final FixtureDefinition definition;
    final Object value;

    Entry(FixtureDefinition definition, Object value) {
      this.definition = definition;
      this.value = value;
    }
  }

  private final Map<Object, Object> values = new HashMap<>();
  private final List<Entry> created = new ArrayList<>();

  Object get(FixtureDefinition definition) {
    return values.get(definition.identity());
  }

  void add(FixtureDefinition definition, Object value) {
    values.put(definition.identity(), value);
    created.add(new Entry(definition, value));
  }

  /** The created values of the given type, e.g. the browsers of a worker. */
  <T> List<T> ofType(Class<T> type) {
    List<T> result = new ArrayList<>();
    for (Entry entry : created) {
      if (type.isInstance(entry.value)) {
        result.add(type.cast(entry.value));
      }
    }
    return result;
  }

  void close(Teardown teardown) {
    for (int i = created.size() - 1; i >= 0; i--) {
      Entry entry = created.get(i);
      teardown.run(() -> entry.definition.close(entry.value));
    }
    created.clear();
    values.clear();
  }
}
