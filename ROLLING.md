# Rolling Playwright-Java to the latest Playwright driver

* make sure to have at least Java 8 and Maven 3.6.3, git, and Node.js with npm
* clone playwright for java: http://github.com/microsoft/playwright-java
* roll the driver and update generated sources: `./scripts/roll_driver.sh <ref>`, where `<ref>` is a branch (`main`, the default, or `release-1.66`), a tag (`v1.66.0`) or a commit of `microsoft/playwright`. It pins the commit in `scripts/DRIVER_VERSION`, checks it out in `.upstream/` (a gitignored clone of `microsoft/playwright`) and builds the driver from it
* list the upstream commits to port: `git -C .upstream log --oneline --reverse $(git show HEAD:scripts/DRIVER_VERSION)..$(cat scripts/DRIVER_VERSION)`
* fix any errors
* commit & send PR with the roll
