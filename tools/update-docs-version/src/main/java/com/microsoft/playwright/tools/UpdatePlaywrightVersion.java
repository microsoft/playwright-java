package com.microsoft.playwright.tools;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

public class UpdatePlaywrightVersion {
  private static final Pattern VERSION_PATTERN = Pattern.compile(
    "<groupId>com.microsoft.playwright</groupId>\\s*<artifactId>parent-pom</artifactId>\\s*<version>(\\d+\\.\\d+\\.\\d+)(?:-SNAPSHOT)?</version>"
  );
  private static final Pattern MAVEN_PATTERN   = Pattern.compile(
    "(<dependency>\\s*<groupId>com.microsoft.playwright</groupId>\\s*<artifactId>playwright</artifactId>\\s*)<version>\\d+\\.\\d+\\.\\d+</version>(\\s*</dependency>)"
  );
  private static final Pattern GRADLE_PATTERN  = Pattern.compile(
    "(implementation group: 'com.microsoft.playwright', name: 'playwright', version: )'\\d+\\.\\d+\\.\\d+'"
  );

  public static void main(String[] args) throws Exception {
    Path   pomPath    = Paths.get("pom.xml");
    String pomContent = Files.readString(pomPath, UTF_8);

    Matcher versionMatcher = VERSION_PATTERN.matcher(pomContent);
    if (!versionMatcher.find()) {
      throw new NoSuchElementException("Project version was not found");
    }
    String version = versionMatcher.group(1);

    Path   readmePath = Paths.get("README.md");
    String readme     = Files.readString(readmePath, UTF_8);

    String updatedReadme = updateDependencies(readme, version);

    try (FileWriter writer = new FileWriter(readmePath.toFile(), UTF_8)) {
      writer.write(updatedReadme);
    }
  }

  private static String updateDependencies(String readme, String version) {
    readme = MAVEN_PATTERN.matcher(readme)
                          .replaceAll("$1<version>" + version + "</version>$2");

    readme = GRADLE_PATTERN.matcher(readme)
                           .replaceAll("$1'" + version + "'");

    return readme;
  }
}
