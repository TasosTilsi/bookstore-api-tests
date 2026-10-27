# ADR-0002: TestNG over JUnit 5

- Status: Accepted
- Date: 2026-10-27

## Context

The suite needs four things from the test engine: selection of a subset without a suite XML, repeated execution of one method with different rows of data, readable hard assertions, and automatic registration of the Allure result listener. The build targets Java 17 and runs serially.

The reactor selects tests by class, package or group. The design explicitly rejects a `testng.xml`, because a renamed class can drop out of a suite file without any build error.

## Decision

Use TestNG 7.12.0, declared once in the parent POM at test scope (`pom.xml`). Selection uses TestNG groups on each method. Data-driven methods use `@DataProvider` (`books/src/test/java/io/github/tasostilsi/bookstore/books/BooksReadTest.java`). Assertions use Hamcrest `assertThat` and TestNG `assertEquals`.

The Allure listener is registered by `allure-testng` through its service file, so no listener wiring appears in the project.

## Consequences

- `-Dgroups=contract`, `-Dgroups=deviation`, `-Dgroups=validationGap`, `-Dgroups=dynamicData` and `-Dgroups=crossservice` are the selection mechanism. No suite XML exists.
- A method can belong to several groups. `@Feature` is repeatable, so the Allure label can mirror a multi-group method.
- Data providers are the only expansion mechanism. `BooksReadTest` uses providers for absent ids and publish-date samples, which is why its case count is higher than its method count.
- Surefire still writes `TEST-*.xml` JUnit-format reports for machine consumption, even though TestNG is the engine.
- The tests use no `SoftAssert`. A hard assertion stops at the first failure, and Hamcrest plus `assertEquals` cover every assertion today. ADR-0008 records the assertion style.
- TestNG's default serial execution matches the suite's needs. The plan does not enable parallel methods or classes.

## Alternatives considered and why rejected

- **JUnit 5.** It has tags instead of groups, parameterized tests instead of data providers, and its own Allure listener. It would work, but the selection model and the Allure integration were already fixed around TestNG, and switching would remove the method-level group labels the report relies on without adding a capability the suite needs.
- **Spock.** It brings Groovy and a second language into a Java repository. The suite does not need a specification DSL.
- **Plain `main` methods with `assert`.** No group selection, no data providers, no Allure listener registration, and no JUnit XML for CI.
