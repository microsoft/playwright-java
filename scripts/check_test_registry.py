#!/usr/bin/env python3
# Copyright (c) Microsoft Corporation.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""Checks the ported tests against the upstream test registry.

playwright/src/test/upstream_registry.md is a copy of tests/registry.md in
microsoft/playwright. It lists every upstream test in tests/page and tests/library with
its identifier (for example CH-001). Each of those tests must be either:

- ported under playwright/src/test/java/com/microsoft/playwright/tests/{page,library}, in
  the class named after the upstream file, as a method annotated
  @DisplayName("<identifier> <title>") with the title of the it() call (the registry
  shows it after the describe titles, markdown-escaped); or
- listed in playwright/src/test/upstream_not_ported.txt, with the reason.

Usage:
    python3 scripts/check_test_registry.py             # fails on any problem
    python3 scripts/check_test_registry.py --report    # prints the coverage per subsystem
    python3 scripts/check_test_registry.py --missing CH page/page-check
                                                       # lists the tests to port
"""

import argparse
import fnmatch
import re
import sys
from collections import defaultdict
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
TEST_DIR = ROOT / "playwright" / "src" / "test"
JAVA_DIR = TEST_DIR / "java" / "com" / "microsoft" / "playwright" / "tests"
REGISTRY = TEST_DIR / "upstream_registry.md"
NOT_PORTED = TEST_DIR / "upstream_not_ported.txt"

ROW = re.compile(r"^\| `([A-Z]{2}(?:-[A-Z]{2})?-\d{3})` \| `([^`]+)` \| (.*) \|$")
DISPLAY_NAME = re.compile(r'@DisplayName\("([A-Z]{2}(?:-[A-Z]{2})?-\d{3}) ')


def class_name(spec):
    """tests/page/page-check.spec.ts -> TestPageCheck."""
    name = Path(spec).name.replace(".spec.ts", "")
    return "Test" + "".join(part[:1].upper() + part[1:] for part in re.split(r"[-.]", name) if part)


def java_file(spec):
    """page/page-check.spec.ts -> .../tests/page/TestPageCheck.java."""
    return JAVA_DIR / Path(spec).parent / (class_name(spec) + ".java")


def registry_title(cell):
    """The title of the it() call: the registry cell without the describe path, the
    "(N copies)" suffix of a loop-declared test and the markdown escapes."""
    title = re.sub(r" \*\(\d+ copies\)\*$", "", cell)
    title = title.rsplit(" › ", 1)[-1]
    return re.sub(r"\\([\\`*_\[\]<>|])", r"\1", title)


def java_string(literal):
    """The value of a Java string literal's contents."""
    return re.sub(r'\\(["\\])', r"\1", literal)


def read_registry():
    tests = []
    for line in REGISTRY.read_text().splitlines():
        match = ROW.match(line)
        if match:
            tests.append((match.group(1), match.group(2), registry_title(match.group(3))))
    return tests


def read_not_ported():
    rules = []
    for number, line in enumerate(NOT_PORTED.read_text().splitlines(), 1):
        if not line.strip() or line.startswith("#"):
            continue
        parts = line.split(None, 1)
        if len(parts) != 2:
            sys.exit(f"{NOT_PORTED}:{number}: expected '<selector> <reason>'")
        rules.append((parts[0], parts[1].strip(), number))
    return rules


def matches(rule, test_id, spec):
    selector = rule[0]
    if re.fullmatch(r"[A-Z]{2}(-[A-Z]{2})?-\d{3}", selector):
        return selector == test_id
    if re.fullmatch(r"[A-Z]{2}", selector):
        return test_id.startswith(selector + "-")
    return fnmatch.fnmatch(spec.replace(".spec.ts", ""), selector)


def read_ported():
    """Identifier -> list of (java file, display name) for every @DisplayName in the ports."""
    ported = defaultdict(list)
    for path in JAVA_DIR.rglob("*.java"):
        text = path.read_text()
        for match in re.finditer(r'@DisplayName\("((?:[^"\\]|\\.)*)"\)', text):
            title = match.group(1)
            id_match = DISPLAY_NAME.match(match.group(0))
            if id_match:
                ported[id_match.group(1)].append((path, title))
    return ported


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("--report", action="store_true", help="print the coverage per subsystem")
    parser.add_argument("--missing", nargs="*", metavar="SELECTOR", help="list the tests to port (subsystem code or file glob)")
    args = parser.parse_args()

    registry = read_registry()
    rules = read_not_ported()
    ported = read_ported()
    problems = []
    used_rules = set()
    counts = defaultdict(lambda: [0, 0, 0])  # ported, not ported, missing
    missing = []

    for test_id, spec, title in registry:
        code = test_id.split("-")[0]
        rule = next((r for r in rules if matches(r, test_id, spec)), None)
        ports = ported.get(test_id, [])
        expected_file = java_file(spec)
        if rule:
            used_rules.add(rule[2])
        if ports and rule:
            problems.append(f"{test_id} is ported and also listed in upstream_not_ported.txt:{rule[2]}")
        if ports:
            counts[code][0] += 1
            for path, display in ports:
                if path != expected_file:
                    problems.append(f"{test_id} is in {path.relative_to(ROOT)}, expected {expected_file.relative_to(ROOT)}")
                expected_title = f"{test_id} {title}"
                if "{" not in title and java_string(display) != expected_title:
                    problems.append(f"{test_id} title is {java_string(display)!r}, registry has {expected_title!r}")
        elif rule:
            counts[code][1] += 1
        else:
            counts[code][2] += 1
            missing.append((test_id, spec, title))

    for selector, reason, number in rules:
        if number not in used_rules:
            problems.append(f"upstream_not_ported.txt:{number}: {selector} matches no test in the registry")
    registry_ids = {test_id for test_id, _, _ in registry}
    for test_id in ported:
        if test_id not in registry_ids:
            problems.append(f"{test_id} is ported but not in the registry")

    if args.missing is not None:
        selectors = args.missing or ["*"]
        for test_id, spec, title in missing:
            if any(matches((s, "", 0), test_id, spec) for s in selectors):
                print(f"{test_id}  {spec}  {title}")
        return

    if args.report:
        print(f"{'code':5} {'ported':>7} {'skipped':>8} {'missing':>8}")
        for code in sorted(counts):
            p, n, m = counts[code]
            print(f"{code:5} {p:7} {n:8} {m:8}")
        p, n, m = (sum(c[i] for c in counts.values()) for i in range(3))
        print(f"{'total':5} {p:7} {n:8} {m:8}")

    for problem in problems:
        print(problem)
    if missing and not args.report:
        print(f"{len(missing)} tests of the registry are neither ported nor listed in upstream_not_ported.txt; see --missing")
    if problems or (missing and not args.report):
        sys.exit(1)


if __name__ == "__main__":
    main()
