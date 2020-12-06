# Contributing

## How to Contribute

### Getting Code

1. Clone this repository

```bash
git clone https://github.com/microsoft/playwright-java
cd playwright-java
```

2. Run the following script to download playwright-cli binary for your platform into `driver/src/main/resources/driver/` directory. It will also install Playwright and download browser binaries for Chromium, Firefox and WebKit.

```bash
scripts/download_driver.sh
```

Names of published driver archives can be found at https://github.com/microsoft/playwright-cli/actions

### Compiling and running the tests with Maven

```bash
./mvnw compile
./mvnw test
```

### Generating API

Public Java API is generated from [api.json](https://github.com/microsoft/playwright-java/blob/master/api-generator/src/main/resources/api.json) which in turn is created by `playwright-cli print-api-json`.

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
