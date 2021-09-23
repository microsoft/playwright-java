# Rolling Playwright-Java to the latest Playwright driver

* make sure to have at least Java 8 and Maven 3.6.3
* clone playwright for java: http://github.com/microsoft/playwright-java
* set new driver version in `scripts/CLI_VERSION`
* regenerate API: `./scripts/download_driver_for_all_platforms.sh -f && ./scripts/generate_api.sh && ./scripts/update_readme.sh`
* commit & send PR with the roll

# Updating Version

```bash
./scripts/set_maven_version.sh 1.15.0
```
