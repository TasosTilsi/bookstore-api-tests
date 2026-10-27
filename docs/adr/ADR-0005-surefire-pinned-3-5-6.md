# ADR-0005: Surefire held at 3.5.6

- Status: Accepted
- Date: 2026-10-27

## Context

The suite uses Allure 3.0.1 through `allure-testng`. Surefire writes the JUnit XML and the Allure result files. With Surefire 3.6.1, each Allure result is written twice for a single test method, and the Allure report counts the duplicate as a retry. A first green run then shows one retry per test, which misrepresents a stable suite.

The duplicate was measured on 2026-10-27. Nothing else in the dependency set needs an older version.

## Decision

Pin `maven-surefire-plugin` to 3.5.6 in the parent POM through the `surefire.version` property (`pom.xml`). Leave the rest of the dependency set at its current release.

Put the reason in an XML comment next to the property, so a dependency bot or a future reader sees why this one version is held back:

```xml
<!-- Do not bump to 3.6.1: it writes each Allure result twice for one test method and the
     report counts the duplicate as a retry (measured 2026-10-27). -->
<surefire.version>3.5.6</surefire.version>
```

## Consequences

- One test method produces one Allure result file. A green run shows no retries.
- The version is visible in the build output: `surefire:3.5.6:test`.
- A dependency bot must not bump this property without re-running the duplicate check. The comment is the guard.
- Upgrading is a deliberate act: run the suite, count the result files per method, and confirm the report's retry count is zero before changing the property.
- The Allure result directory is set through a Surefire system property, `allure.results.directory=${project.build.directory}/allure-results`. That configuration is independent of the Surefire version and remains in place.

## Alternatives considered and why rejected

- **Use 3.6.1 and deduplicate the result files.** The duplicate is already written when the report reads it. A post-processing step would have to know which file is the duplicate, and the report's retry count would still be produced before the step runs. It fixes the symptom after the wrong data exists.
- **Use 3.6.1 and ignore the retry count.** The report would still show retries on every test. A reader cannot tell a real retry from the duplicate.
- **Stay on an older 3.x release.** 3.5.6 is current enough for the toolchain and does not have the duplicate defect. There is no reason to go further back.
