# Rolling Playwright-Java to the latest Playwright driver

* make sure to have at least Java 8 and Maven 3.6.3
* clone playwright for java: http://github.com/microsoft/playwright-java
* `./scripts/roll_driver.sh 1.47.0-beta-1726138322000`
* commit & send PR with the roll

## Finding driver version

For development versions of Playwright, you can find the latest version by looking at [publish_canary](https://github.com/microsoft/playwright/actions/workflows/publish_canary.yml) workflow -> `publish canary NPM & Publish canary Docker` -> `build & publish driver` step -> `PACKAGE_VERSION`
<img width="960" alt="image" src="https://github.com/microsoft/playwright-java/assets/9798949/4f33a7f1-b39a-4179-8ae7-fb1d84094c75">


# Updating Version

```bash
./scripts/set_maven_version.sh 1.15.0
```

