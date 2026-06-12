# Playwright Java

The Java client is a port of the JavaScript client in `../playwright/packages/playwright-core/src/client/`. When implementing or changing a method, read the corresponding JS file first and mirror its logic.

Project checkouts (including the upstream `playwright` repo) live in the parent directory (`../`). Use the `gh` cli to interact with GitHub.

## Skills

- **playwright-roll** (`.claude/skills/playwright-roll/SKILL.md`) — roll Playwright Java to a new upstream version: bump the driver, regenerate the API, and port relevant upstream changes.
- **playwright-java-release** (`.claude/skills/playwright-java-release/SKILL.md`) — prepare a release after the rolling PR merges: cut the release branch, mark the Maven version, and draft the GitHub release.

## Commit Convention

Semantic commit messages: `label(scope): description`

Labels: `fix`, `feat`, `chore`, `docs`, `test`, `devops`

```bash
git checkout -b fix-39562
# ... make changes ...
git add <changed-files>
git commit -m "$(cat <<'EOF'
fix(proxy): handle SOCKS proxy authentication

Fixes: https://github.com/microsoft/playwright-java/issues/39562
EOF
)"
# **Never `git push` without an explicit instruction to push.**
git push origin fix-39562
gh pr create --repo microsoft/playwright-java --head <user>:fix-39562 \
  --title "fix(proxy): handle SOCKS proxy authentication" \
  --body "$(cat <<'EOF'
## Summary
- <describe the change very! briefly>

Fixes https://github.com/microsoft/playwright-java/issues/39562
EOF
)"
```

Never add Co-Authored-By agents in commit message.
Never add "Generated with" in commit message.
Never add test plan to PR description. Keep PR description short — a few bullet points at most.
Branch naming for issue fixes: `fix-<issue-number>`.

**Never amend commits.** Always create a new commit for follow-up changes, even when iterating on an open PR. Amending rewrites history and forces a force-push, losing the incremental review trail. Only amend if the user explicitly says so.

**Never `git push` without an explicit instruction to push.** Applies even when a PR is already open for the branch — additional commits are immediately visible to reviewers. Commit locally, report what was committed, and wait. Only push when the user's message contains "push", "upload", "create PR", "ship it", or equivalent.
