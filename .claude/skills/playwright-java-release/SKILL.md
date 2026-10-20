---
name: playwright-java-release
description: Prepare a Playwright Java release after the rolling PR has merged — cut the release branch, mark the Maven version, add Java-specific changes to the release notes, draft the GitHub release, and tick the Java boxes in the internal checklist.
---

Use this skill once the `chore: roll driver to 1.X.0` PR has merged into `main` and the upstream JS `v1.X.0` is published. The rolling work itself is covered by the [[playwright-roll]] skill.

Throughout this doc, replace `X` with the minor version (e.g. `60` for `1.60.0`) and `<user>` with the fork owner (`gh api user --jq .login`).

The full release checklist lives in the private `microsoft/playwright-internal` repo as the `v1.X checklist` issue. Find its number once:

```bash
unset GITHUB_TOKEN
ISSUE=$(gh search issues --repo microsoft/playwright-internal "v1.X checklist" --json number --jq '.[0].number')
```

Tick each Java box incrementally (one PATCH per item) so the issue reflects accurate state if the flow is interrupted:

```bash
gh api repos/microsoft/playwright-internal/issues/$ISSUE --jq '.body' > /tmp/body.md
# edit /tmp/body.md to flip "- [ ]" → "- [x]" on the relevant Java item
gh api repos/microsoft/playwright-internal/issues/$ISSUE -X PATCH --field body=@/tmp/body.md
```

## 1. Cut the release branch

Push `release-1.X` from current `upstream/main` (which now contains the merged roll commit):

```bash
git fetch upstream main
git push upstream upstream/main:refs/heads/release-1.X
```

## 2. Release notes

The Java notes in `docs/src/release-notes-java.md` are written upstream together with the Node.js notes, following the `playwright-release-notes` skill (`../playwright/.claude/skills/playwright-release-notes/SKILL.md`). Assume the `## Version 1.X` section is already on `release-1.X`, and only add what's missing: significant changes that only the Java port has, such as a new JUnit fixture or a Java-only API. Leave out minor bug fixes. To find candidates, list this repo's changes since the previous release branch was cut:

```bash
git log --oneline --no-merges $(git merge-base upstream/release-1.<X-1> upstream/main)..upstream/main | grep -E '^\w+ (feat|fix)'
```

Ask the user before adding anything. Send additions upstream as `docs: release notes for v1.X Java`, and cherry-pick them to `release-1.X`.

Write the GitHub release body from the section as step 3 of the upstream skill describes, and save it as `/tmp/v1.X.0-release-notes.md`. Create the draft directly against `release-1.X`. Drafting against `main` and retargeting later is fragile, because every `gh release edit` rotates the `untagged-<hash>` ID:

```bash
gh release create v1.X.0 --repo microsoft/playwright-java --draft \
  --title "v1.X.0" --notes-file /tmp/v1.X.0-release-notes.md --target release-1.X
```

## 3. Bump the Maven version on the release branch

Cut `mark-v-1.X.0` off `upstream/release-1.X`, run `set_maven_version.sh`, and PR back to the release branch:

```bash
git checkout -b mark-v-1.X.0 upstream/release-1.X
./scripts/set_maven_version.sh 1.X.0
git add -u
git commit -m "chore: mark 1.X.0"
git push -u origin mark-v-1.X.0
gh pr create --repo microsoft/playwright-java --head <user>:mark-v-1.X.0 --base release-1.X \
  --title "chore: mark 1.X.0" \
  --body "Updates Maven version in all modules to \`1.X.0\` for the v1.X release."
```

`set_maven_version.sh` only invokes `mvn versions:set` on `pom.xml`, `tools/*/pom.xml`, and `examples/pom.xml`, but the root invocation cascades through the reactor, so the expected diff is 11 poms: root + `driver/` + `driver-bundle/` + `playwright/` (from the reactor cascade) + 6 under `tools/` + `examples/`, all flipping `1.<prev>.0-SNAPSHOT` → `1.X.0`. Any other file in the diff is a red flag.

## 4. Publish

The user publishes the draft release manually once the `mark-v-1.X.0` PR is merged. After publishing, the `v1.X.0` tag triggers the Azure DevOps pipelines that push the artifacts to Maven Central (`.azure-pipelines/publish.yml`) and the Docker images to ACR (`.azure-pipelines/publish-docker.yml`).
