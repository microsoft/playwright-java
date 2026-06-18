# 🎭 [Playwright](https://playwright.dev) for Java

[![javadoc](https://javadoc.io/badge2/com.microsoft.playwright/playwright/javadoc.svg)](https://javadoc.io/doc/com.microsoft.playwright/playwright)
[![maven version](https://img.shields.io/maven-central/v/com.microsoft.playwright/playwright)](https://search.maven.org/search?q=com.microsoft.playwright)
[![Join Discord](https://img.shields.io/badge/join-discord-infomational)](https://aka.ms/playwright/discord)

#### [Website](https://playwright.dev/java/) | [API reference](https://www.javadoc.io/doc/com.microsoft.playwright/playwright/latest/index.html)

Playwright is a Java library to automate [Chromium](https://www.chromium.org/Home), [Firefox](https://www.mozilla.org/en-US/firefox/new/) and [WebKit](https://webkit.org/) with a single API. Playwright is built to enable cross-browser web automation that is **ever-green**, **capable**, **reliable** and **fast**.

|          | Linux | macOS | Windows |
|   :---   | :---: | :---: | :---:   |
| Chromium <!-- GEN:chromium-version -->149.0.7827.55<!-- GEN:stop --> | :white_check_mark: | :white_check_mark: | :white_check_mark: |
| WebKit <!-- GEN:webkit-version -->26.5<!-- GEN:stop --> | ✅ | ✅ | ✅ |
| Firefox <!-- GEN:firefox-version -->151.0<!-- GEN:stop --> | :white_check_mark: | :white_check_mark: | :white_check_mark: |

## Documentation

[https://playwright.dev/java/docs/intro](https://playwright.dev/java/docs/intro)

## API Reference

[https://playwright.dev/java/docs/api/class-playwright](https://playwright.dev/java/docs/api/class-playwright)

## Example

This code snippet navigates to Playwright homepage in Chromium, Firefox and WebKit, and saves 3 screenshots.

```java
import com.microsoft.playwright.*;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class PageScreenshot {
  public static void main(String[] args) {
    try (Playwright playwright = Playwright.create()) {
      List<BrowserType> browserTypes = Arrays.asList(
        playwright.chromium(),
        playwright.webkit(),
        playwright.firefox()
      );
      for (BrowserType browserType : browserTypes) {
        try (Browser browser = browserType.launch()) {
          BrowserContext context = browser.newContext();
          Page page = context.newPage();
          page.navigate("https://playwright.dev/");
          page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get("screenshot-" + browserType.name() + ".png")));
        }
      }
    }
  }
}
```

## Driver bundles and platform selection

Playwright ships the driver (a Node.js binary plus the `playwright-core` package) as
per-platform Maven artifacts. With **Maven**, the right one is selected automatically for the
machine that runs the build, so depending on `playwright` is all you need — no extra
configuration:

| Artifact | Platform |
| --- | --- |
| `driver-bundle-mac-x64` | macOS Intel |
| `driver-bundle-mac-arm64` | macOS Apple Silicon |
| `driver-bundle-linux-x64` | Linux x64 |
| `driver-bundle-linux-arm64` | Linux arm64 |
| `driver-bundle-win-x64` | Windows x64 |

### Bundling every platform (cross-platform / fat JARs)

The automatic selection picks the driver for the build host, so a JAR built on Linux contains
only the Linux driver. If you build on one OS and run on another (for example a distributable
fat JAR), depend on `driver-bundle-all`, which bundles every platform:

```xml
<dependency>
  <groupId>com.microsoft.playwright</groupId>
  <artifactId>driver-bundle-all</artifactId>
  <version>${playwright.version}</version>
</dependency>
```

### Gradle

Gradle does not evaluate the Maven POM profiles that drive the automatic selection, so a Gradle
build will not pick a platform on its own (and converting the dependency from Maven does not
carry the selection over). Declare the driver explicitly — either a single platform:

```kotlin
runtimeOnly("com.microsoft.playwright:driver-bundle-linux-x64:$playwrightVersion")
```

or every platform:

```kotlin
runtimeOnly("com.microsoft.playwright:driver-bundle-all:$playwrightVersion")
```

## Other languages

More comfortable in another programming language? [Playwright](https://playwright.dev) is also available in
- [Node.js (JavaScript / TypeScript)](https://playwright.dev/docs/intro),
- [Python](https://playwright.dev/python/docs/intro).
- [.NET](https://playwright.dev/dotnet/docs/intro),
