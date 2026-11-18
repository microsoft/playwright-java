# Rolling Playwright-Java to the latest Playwright driver

* make sure to have at least Java 8 and Maven 3.6.3, git, and Node.js with npm
* clone playwright for java: http://github.com/microsoft/playwright-java
* roll the driver and update generated sources: `./scripts/roll_driver.sh [main|release-1.<minor>|<commit>]`. It rolls to the tip of upstream `main` (the default), to the tip of an upstream release branch, or to an exact commit of `microsoft/playwright`. It writes the commit to `scripts/DRIVER_VERSION`, checks it out in `.upstream/` (a gitignored clone of `microsoft/playwright`) and builds the driver from it
* list the upstream commits to port: `git -C .upstream log --oneline --reverse $(git show HEAD:scripts/DRIVER_VERSION)..$(cat scripts/DRIVER_VERSION)`
* fix any errors
* commit & send PR with the roll
