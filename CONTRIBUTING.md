# Contributing

## Choosing an Issue

To maintain project quality and focus, Playwright **requires a corresponding issue** for every contribution, with the exception of minor documentation fixes.

If you would like to address a bug or feature that isn't currently listed, **please file a new issue first**. This allows the community and maintainers to provide early feedback and facilitates a discussion before you invest time in developing a pull request.

When submitting an issue, please state clearly if you intend to work on it. Once triaged and approved, the maintainers will determine the best path forward—whether the task should be handled by the **core team**, an **automated agent**, or a **community contributor**. If the issue is assigned to you, you may then proceed with your changes and submit a PR.

### Submission Policy
To ensure the maintainability of the project, please note the following:

* **Unsolicited PRs:** Pull requests submitted without a linked issue or prior approval will be closed.
* **Low-Quality AI Contributions:** PRs that do not meet our quality standards or lack human oversight (including low-quality agentic submissions) will be closed without explanation.
* **Approval Required:** Only proceed with a PR once the issue has been officially assigned to you or approved for community contribution.

## How to Contribute

### Installing Developer Tools

Install git, Java JDK (version >= 8), Maven (tested with version 3.6.3), on Ubuntu 20.04
just run the following command:

```sh
sudo apt-get install git openjdk-11-jdk maven unzip
```

### Getting the Code

1. Clone this repository

```bash
git clone https://github.com/microsoft/playwright-java
cd playwright-java
```

2. Run the following script to download and assemble the Playwright driver. The platform-independent `playwright-core` package is assembled once into `driver/src/main/resources/driver/package/`, and the Node.js binary for each platform into `driver-bundle/src/main/resources/driver/<platform>/` (browser binaries for Chromium, Firefox and WebKit will be automatically downloaded later on first Playwright run).

```bash
scripts/download_driver.sh
```

Each driver is assembled from the [`playwright-core`](https://www.npmjs.com/package/playwright-core) npm package (version pinned in [scripts/DRIVER_VERSION](scripts/DRIVER_VERSION)) and the matching Node.js binary from https://nodejs.org, the same way the upstream Playwright build does it.

### Building and running the tests with Maven

```bash
mvn compile
# Runs every test in Chromium, Firefox and WebKit
mvn test
# Selecting browsers
BROWSER=chromium mvn test
BROWSER=firefox,webkit mvn test
# Executing a single test
BROWSER=chromium mvn test -Dtest=TestPageNetworkSizes#shouldHaveTheCorrectResponseBodySize
# Executing a single test class
BROWSER=chromium mvn test -Dtest=TestPageNetworkSizes
```

Set `HEADED=1` to watch the browsers and `BROWSER_CHANNEL=chrome` (or `msedge`, ...) to test a stable channel.

### Writing tests

Tests use the fixtures from `playwright/src/test/java/com/microsoft/playwright/fixtures`, modeled after the
upstream Playwright Test fixtures. A test is a `@PlaywrightTest` method that declares the fixtures it needs
as parameters; it runs once per selected browser, in a fresh context:

```java
@PlaywrightTest
void shouldWork(Page page, Server server) {
  page.navigate(server.EMPTY_PAGE);
}
```

The built-in fixtures are `Playwright`, `BrowserName`, `BrowserType`, `BrowserType.LaunchOptions`, `Browser`,
`Browser.NewContextOptions`, `BrowserContext`, `Page`, `Server` and `HttpsServer`. Playwright, the browser and
the test servers are shared by the tests that run one after another on the same thread; the context and the
page are created for each test and closed afterwards. A test class adds or overrides fixtures with
`@Fixture` methods, whose parameters are the fixtures they depend on:

```java
@Fixture
Browser.NewContextOptions contextOptions() {
  return new Browser.NewContextOptions().setHasTouch(true);
}

@Fixture(scope = Scope.WORKER)
static BrowserType.LaunchOptions launchOptions(BrowserType.LaunchOptions defaults) {
  return defaults.setArgs(asList("--enable-features=WebMCP"));
}
```

Use `@DisabledOnBrowser` / `@EnabledOnBrowser` for browser-specific tests, and a `@Nested @ParameterizedClass`
with a `@Fixture` method reading its `@Parameter` field to run the same tests with different options.

### Generating API

Public Java API is generated from api.json, which is generated from the upstream Playwright source at the exact commit that produced the driver version in [scripts/DRIVER_VERSION](scripts/DRIVER_VERSION) (resolved via `npm view playwright@<version> gitHead`). `scripts/generate_api.sh` fetches a minimal upstream checkout automatically; set `PW_SRC_DIR` to reuse an existing `microsoft/playwright` checkout instead. To regenerate Java interfaces for the current driver run:

```bash
./scripts/generate_api.sh
```

#### Updating driver version

Versions of published driver archives can be found in [publish canary](https://github.com/microsoft/playwright/actions/workflows/publish_canary.yml) and [publish release](https://github.com/microsoft/playwright/actions/workflows/publish_release_driver.yml) actions logs. To update the driver to a particular version run the following command:

```bash
scripts/roll_driver.sh [version]
```

### Code Style

- We try to follow [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- Comments should be generally avoided. If the code would not be understood without comments, consider re-writing the code to make it self-explanatory.

### Code reviews

All submissions, including submissions by project members, require review. We
use GitHub pull requests for this purpose. Consult
[GitHub Help](https://help.github.com/articles/about-pull-requests/) for more
information on using pull requests.

### Commit Messages

Commit messages should follow the Semantic Commit Messages format:

```
label(namespace): title

description

footer
```

1. *label* is one of the following:
    - `fix` - playwright bug fixes.
    - `feat` - playwright features.
    - `docs` - changes to docs, e.g. `docs(api.md): ..` to change documentation.
    - `test` - changes to playwright tests infrastructure.
    - `devops` - build-related work, e.g. CI related patches and general changes to the browser build infrastructure
    - `chore` - everything that doesn't fall under previous categories
2. *namespace* is put in parenthesis after label and is optional. Must be lowercase.
3. *title* is a brief summary of changes.
4. *description* is **optional**, new-line separated from title and is in present tense.
5. *footer* is **optional**, new-line separated from *description* and contains "fixes" / "references" attribution to github issues.

Example:

```
fix(firefox): make sure session cookies work

This patch fixes session cookies in firefox browser.

Fixes #123, fixes #234
```

## Contributor License Agreement

This project welcomes contributions and suggestions.  Most contributions require you to agree to a
Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us
the rights to use your contribution. For details, visit https://cla.opensource.microsoft.com.

When you submit a pull request, a CLA bot will automatically determine whether you need to provide
a CLA and decorate the PR appropriately (e.g., status check, comment). Simply follow the instructions
provided by the bot. You will only need to do this once across all repos using our CLA.

### Code of Conduct

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or
contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.
