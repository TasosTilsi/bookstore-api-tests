# ADR-0001: Four-module Maven reactor with one module per service area

- Status: Accepted
- Date: 2026-10-27

## Context

The suite covers two service areas, Books and Authors, over one shared HTTP and assertion layer. The repository layout is part of what a reader is meant to inspect, so the structure should show the service boundary rather than hide it in packages. Adding a third area should be a structural addition, not an edit to a central class list.

The shared layer needs to be compiled once and reused by both areas. The service areas must not depend on each other, or the reactor becomes cyclic. Cross-service tests need both fluent clients in one compilation unit.

## Decision

Use a parent POM with `<packaging>pom</packaging>` and three child modules: `core`, `books` and `authors` (`pom.xml`). `core` holds configuration, the HTTP wrapper, the fluent clients and the generated models. `books` and `authors` hold only tests and their fixtures. Both service modules declare a compile dependency on `bookstore-api-core` (`books/pom.xml`, `authors/pom.xml`). `core` declares no internal dependency (`core/pom.xml`).

Keep the dependency arrow one way. Cross-service tests live in `core`, where both `Api.books()` and `Api.authors()` are visible without a test-jar dependency.

## Consequences

- The compiler enforces the service boundary. A service test cannot reach into another service module.
- A module runs independently: `./mvnw -B -pl books -am verify` builds `core` first and runs only the `books` tests.
- Adding a service costs one module POM, one client class, a block of constants in `ApiPaths` and a test package. It does not require editing a shared class list.
- The `authors` module is currently a POM with no test sources. The module exists before its tests, which is visible in the reactor output as an empty JAR warning.
- The reactor reports 65 cases today (3 in `core`, 47 in `books`, 15 in `authors`) and targets 74 methods and 83 cases when the plan completes.
- The design and the plan both put the planned `ProtocolTest` and `ErrorFormatTest` under `books/src/test/java/io/github/tasostilsi/bookstore/books/protocol/`, not in `core`: every request they make is a Books request. `core` holds only the cross-service class, whose subject is the relationship between the two resources. No protocol test exists yet.

## Alternatives considered and why rejected

- **Single Maven module.** It removes three POMs and one version-plumbing file, but there is no compiler-enforced boundary between service areas, and a per-service run becomes a `-Dtest=` pattern that a renamed class can silently escape. The module boundary is part of the structure the repository is meant to demonstrate.
- **A fifth `integration` module for cross-service tests.** It would need a test-jar dependency on `books` and `authors` to reach their fixtures, or duplicated fixtures. A package inside `core` gives the same separation without the extra artifact and without a second dependency direction.
- **One module per endpoint.** Too fine. Read, create, update and delete share one client and one fixture class; splitting them adds POMs without adding a boundary.
