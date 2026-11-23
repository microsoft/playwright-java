---
name: playwright-roll
description: Roll Playwright Java to a new microsoft/playwright commit. Moves the commit in scripts/DRIVER_VERSION, builds the driver from source, generates the API, and ports the upstream diff of the client line by line.
---

# Rolling Playwright

Read `AGENTS.md` and `playwright/src/main/AGENTS.md` first.

`scripts/DRIVER_VERSION` holds one commit of `microsoft/playwright`. `.upstream/` (gitignored) is a clone of `microsoft/playwright` at that commit. Read the upstream code there. Do not edit, commit or switch branches in `.upstream/`. A roll does not use the published npm packages or their versions.

A roll moves `scripts/DRIVER_VERSION` to a newer commit and ports everything that changed in the client between the two commits.

**The port is line by line.** The Java client is a line-by-line translation of the JS client, and a roll keeps it that way. You do not design anything. You take the upstream diff of a file and make the same change at the same place in the Java file, with the patterns of `playwright/src/main/AGENTS.md`. When a hunk does not translate with those patterns, stop and report the hunk. Do not invent a different implementation.

## 1. Roll

```bash
./scripts/roll_driver.sh                 # the tip of upstream main
./scripts/roll_driver.sh release-1.X     # the tip of an upstream release branch
./scripts/roll_driver.sh <commit>        # an exact commit (40 hex digits)
```

Use the target that the user gives. If the user gives no target, roll `main` to upstream `main`, and roll a `release-1.X` branch of this repository to upstream `release-1.X`.

The script writes the commit to `scripts/DRIVER_VERSION`, rolls Node.js, builds the driver from `.upstream/`, generates the public API and the protocol types (`impl/protocol/`), updates the browser versions in the README and installs the new driver modules into the local Maven repository. Then get the two commits:

```bash
old=$(git show HEAD:scripts/DRIVER_VERSION)
new=$(cat scripts/DRIVER_VERSION)
```

The script does not compile the client. If it stops in the API generator, see *When the generator fails*.

## 2. Compile

```bash
mvn -f playwright/pom.xml compile --no-transfer-progress 2>&1 | grep "ERROR"
```

The errors are the first part of the to-do list: a new method of a public interface that no `*Impl` has, a removed or renamed protocol parameter, a changed result type. Do not fix them one by one with stubs. Step 3 fixes them, because each one comes from an upstream hunk.

## 3. Port the client

List each changed upstream file with the Java files that port it:

```bash
git -C .upstream diff --name-only "$old" "$new" -- \
  packages/playwright-core/src/client \
  packages/isomorphic \
  packages/protocol/src/serializers.ts \
  packages/playwright/src/matchers/matchers.ts |
while read -r f; do
  echo "$f -> $(grep -rlE "^// Port of .*\b$(basename "$f")" playwright/src/main/java | xargs -r -n1 basename | tr '\n' ' ')"
done
```

- A file with Java files after the arrow: port it.
- `matchers.ts`: its ports are `LocatorAssertionsImpl`, `PageAssertionsImpl`, `APIResponseAssertionsImpl` and `AssertionsBase`.
- A file with nothing after the arrow has no port. If it is in `client/` and new in this roll (`git -C .upstream diff --diff-filter=A --name-only "$old" "$new" -- packages/playwright-core/src/client`), port it as a new Java file, unless section 8 of `playwright/src/main/AGENTS.md` lists it. Otherwise skip it.

For each file to port, do these steps:

1. Read the diff: `git -C .upstream diff "$old" "$new" -- <file>`.
2. Open the Java file. For each hunk, find the Java lines that correspond to the lines around the hunk. They have the same names and the same order.
3. Make the same change there: add the lines that the hunk adds, remove the lines that it removes, change the lines that it changes. Translate each statement with `playwright/src/main/AGENTS.md`.
4. Skip a hunk only for one of these reasons, and write it down:
   - the member is not in the Java API: its docs in `.upstream/docs/src/api/` have `* langs:` without `java`;
   - the hunk changes something that section 8 of `playwright/src/main/AGENTS.md` lists as not ported;
   - the hunk changes only a comment, an import or a TypeScript type.
5. A new method of a generated interface needs its `@Override` method in the `*Impl`, at the place where the method is in the `.ts` file.

Keep notes in a file in your scratch directory: one line per upstream file, with what you ported and what you skipped. The PR description comes from this file.

Changes in the server, the injected scripts and the dispatchers need nothing: they come with the new driver. A change of `packages/protocol/spec/` needs nothing by itself: `roll_driver.sh` generated the new protocol types, and the client hunk that uses them is in the list above.

Compile again until there are no errors. An error that is left after all hunks are ported means that a hunk is missing or wrong. Read the upstream file at `$new` again; do not add a stub.

## 4. Port the tests

The ports of the upstream tests are line by line too, with the rules of `playwright/src/test/AGENTS.md`. Each roll ports every change of the upstream tests between the two commits. The commit that the ports are at is in `playwright/src/test/upstream_base.txt`; after a roll it is `$new`.

List each changed spec file with the Java class that ports it:

```bash
base=$(grep -v '^#' playwright/src/test/upstream_base.txt | cut -d' ' -f1)
git -C .upstream diff --numstat "$base" "$new" -- tests/page tests/library |
while read -r added removed f; do
  echo "+$added -$removed $f -> $(grep -rl "Port of $f\b" playwright/src/test/java | xargs -r -n1 basename)"
done
git -C .upstream diff --stat "$base" "$new" -- tests/assets tests/config
git -C .upstream diff "$base" "$new" -- tests/registry.md | grep '^[-+]|'
```

For each spec file with a Java class, read `git -C .upstream diff "$base" "$new" -- <file>` and make the same change at the same place in the Java class, hunk by hunk:

- a changed line of a test: change the Java statement that ports it;
- a new test: a new method at the same place, between the same neighbors;
- a removed test: remove the method;
- a changed title: change the `@DisplayName` and the method name.

A spec file without a Java class is in `playwright/src/test/upstream_not_ported.txt`. A new test in it needs no rule when a file or subsystem rule covers it. A new upstream test that Java cannot run gets a rule there with the reason.

A changed file in `tests/assets` is copied to the same path in the Java test assets. A change in `tests/config/testserver` is ported to the test server of the harness (`com.microsoft.playwright.fixtures`).

Then move the base:

```bash
cp .upstream/tests/registry.md playwright/src/test/upstream_registry.md
git -C .upstream log -1 --format='%H %cs %s' "$new"    # the new last line of upstream_base.txt
python3 scripts/check_test_registry.py
```

The checker lists each test that is in the registry and not ported, and each ported test that the registry does not have any more.

A test marked `@Fixme("driver: ...")` or `@Fixme("java: ...")` may pass now. It fails when it does: remove the mark then. Never change a port to make it pass.

Upstream tests cover a new API. Add a Java-only test in `com/microsoft/playwright/` only for what upstream does not test: a Java overload, a Java-only option type.

## 5. Check

Run each changed test class on the three browsers, then the full suite:

```bash
mvn test -f playwright/pom.xml -D test=<Class> --no-transfer-progress
mvn test -f playwright/pom.xml --no-transfer-progress
```

A failure in a class that the roll did not change may be a flake, or older than the roll. Run the test alone, and run it on `HEAD` without the roll. Say in the PR which one it is. Never change a test or the client to make a test pass in a way that upstream does not have.

Then read `git diff upstream/main` end to end:

- **Removed generated methods** — `git diff upstream/main -- playwright/src/main/java/com/microsoft/playwright/*.java | grep '^-.*;'`. A return type change like `void` → `AutoCloseable` still compiles for callers but breaks code compiled against the previous jar. Call these out in the PR.
- **Behavior beyond upstream** — every added line of `impl/` has an upstream line that it translates, or a `// java:` comment. Remove anything else.
- **Stray files** — local config such as `.claude/settings.json`, or unrelated edits like `examples/pom.xml`, must not be committed.

## 6. The roll PR

The PR contains:

- the new commit in `scripts/DRIVER_VERSION`, and the new `scripts/NODE_VERSION` if Node.js changed;
- the generated interfaces and protocol types from `roll_driver.sh`;
- the ported hunks in `impl/`;
- the ported tests, the registry copy and `upstream_base.txt`;
- changes to the generator, if step 1 needed them.

Title: `chore(driver): roll to <version> (<first 10 characters of $new>)`. `<version>` is the version that the driver reports. Read it from the source, not from npm:

```bash
node -p "require('./.upstream/packages/playwright-core/package.json').version"   # for example 1.67.0-next
```

Description: what you ported and what you skipped, with the reason, from your notes. Put the list of the upstream commits in a `<details>` block:

```bash
git -C .upstream log "$old..$new" --oneline --reverse -- \
  docs/src/api packages/playwright-core/src/client packages/isomorphic \
  packages/playwright/src/matchers/matchers.ts packages/protocol/spec
```

## When the generator fails

`roll_driver.sh` stops when `scripts/generate_java_api.mts` fails on a new type. Fix the generator, then run the rest of the script by hand and go on with step 2:

```bash
./scripts/generate_api.sh
./scripts/update_readme.sh
mvn -B clean install -D skipTests --no-transfer-progress -pl driver,driver-bundle
```

The generator has hardcoded lists that control which imports are added to each generated file. When new classes appear in the API, add them to the relevant lists in `INTERFACE_IMPORTS`:

- `options.*` import list — add new classes that use types from the options package
- `java.util.*` import list — add new classes that use `List`, `Map`, etc.
- `java.util.function.Consumer` list — add new classes with `Consumer`-typed event handlers

Type mapping: when JS-only types (like `Disposable`) are used as return types in Java-compatible methods, add a mapping in `BUILTIN_TYPES`. For example, `Disposable` → `AutoCloseable`.

Type names: give every new object type an explicit alias upstream. When a roll brings in a new object type (an option or return value written as `<[Object]>` in `docs/src/api/`), it should usually have its own `* alias: <Name>` line. Without one, each language's generator makes up a name, and the Java one can be vague (e.g. `Style`) or differ between Java and .NET.

- Prefer a plain `alias:` so every language gets the same name. Use `alias-java:` / `alias-csharp:` only when a language really needs a different name.
- Add the aliases in a small upstream docs PR (e.g. microsoft/playwright#43164, `Style` → `ScreencastActionStyle`). Don't special-case names in the generator.
- After the upstream change lands, roll to a driver that includes it. Don't hand-edit the generated files.

## When a test uses the old driver

If `roll_driver.sh` stopped before its last step, Maven did not install the new driver. The test JVM then loads the **old** driver from `target/classes` or from the installed jar, although the source resources already hold the new one. The sign is a driver validation error such as `expected string, got undefined` for a parameter that the client does send. Install the two driver modules once:

```bash
mvn -B clean install -D skipTests --no-transfer-progress -pl driver,driver-bundle
```

If `scripts/generate_api.sh` prints `.upstream/ is not at ...`, run `scripts/build_driver.sh`.

When the driver is the new one and the error stays, a protocol hunk is not ported: compare the params of the call with `.upstream/packages/protocol/spec/*.yml` and with the JS client.

## When a test hangs

When `mvn test` hangs and surefire times the JVM out, it writes thread dumps to `playwright/target/surefire-reports/<timestamp>-jvmRun*.dump`. To find the stuck test:

```bash
grep "com.microsoft.playwright.Test" playwright/target/surefire-reports/*-jvmRun1.dump | sort -u
```

Each line is a stack frame inside a test method. The test that is blocked in `runUntil`, `Future.get()` or a wait is the hanging one.

1. Run it alone: `mvn -f playwright/pom.xml test -D test='TestClass#testMethod'`. If it passes alone, it is a flake under parallel load. Note it and move on.
2. If it hangs alone, a protocol change is the usual cause: a method that returned its object in an event now returns it in the response, or the reverse. Read the diff of the JS method again and port it as it is.
3. Then look for an upstream fix of the same test: `git -C .upstream log --oneline -- tests/library/<spec>.spec.ts`. Port the fix as upstream wrote it.

Don't change the driver environment per test. The driver is a JVM-wide singleton, so `Playwright.CreateOptions.env` applies only to the first `Playwright.create()` in the JVM.
