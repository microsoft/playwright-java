///usr/bin/env jbang "$0" "$@" ; exit $?
//DEPS com.microsoft.playwright:playwright:RELEASE
//DESCRIPTION Playwright lets you automate Chromium, Firefox and Webkit with a single API.
//DESCRIPTION With this cli you can install, trace, generate pdf and screenshots and more.
//DESCRIPTION
//DESCRIPTION Example on how to record and run a script:
//DESCRIPTION ```
//DESCRIPTION   jbang playwright@microsoft/playwright-java codegen -o Example.java`
//DESCRIPTION   jbang --deps com.microsoft.playwright:playwright:RELEASE Example.java
//DESCRIPTION ```

// NOTE: this file is referenced from https://github.com/microsoft/jbang-catalog/blob/702c9b4355c76ae6f7294e1aaf6f2698b03631aa/jbang-catalog.json#L17
public class playwright {

    public static void main(String... args) throws Exception {
        com.microsoft.playwright.CLI.main(args);
    }
}
