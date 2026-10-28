# ADR-0001: Four-module Maven reactor with one module per service area

- Status: Accepted
- Date: 2026-10-27

## Context

The suite covers two service areas, Books and Authors, over one shared HTTP and assertion layer. The repository layout is part of what a reader is meant to inspect, so the structure should show the service boundary rather than hide it in packages. Adding a third area should be a structural addition, not an edit to a central class list.

The shared layer needs to be compiled once and reused by both areas. The service areas must not depend on each other, or the reactor becomes cyclic. Cross-service tests need both fluent clients in one compilation unit.

## Decision

Use a parent POM with `<packaging>pom</packaging>` and three child modules: `core`, `books` and `authors` (`pom.xml`). `core` holds configuration, the HTTP wrapper, the fluent clients and the generated models. `books` and `authors` hold only tests and their fixtures. Both service modules declare a test-scope dependency on `bookstore-api-core` (`books/pom.xml`, `authors/pom.xml`), which is what they actually need: they have no main sources. `core` declares no internal dependency (`core/pom.xml`).

Keep the dependency arrow one way. Cross-service tests live in `core`, where both `Api.books()` and `Api.authors()` are visible without a test-jar dependency.

## Consequences

- The compiler enforces the service boundary. A service test cannot reach into another service module.
- A module runs independently: `./mvnw -B -pl books -am verify` builds `core` first and runs its tests too, because `-am` runs every upstream module through the same lifecycle phase. That includes the live cross-service class, so a per-module run is not an offline run. `core`'s own two infrastructure classes can be run offline with `-pl core -Dtest=ApiResponseEdgeTest,ModelDeserialisationTest`.
- Adding a service costs one module POM, one client class, a block of constants in `ApiPaths` and a test package. It does not require editing a shared class list.
- The `books` and `authors` modules hold only test sources, so they declare `core` at `test` scope.
- The reactor reports 91 cases today (11 in `core`, 54 in `books`, 26 in `authors`) from 81 methods. Twenty-five of the cases fail against the demo, because they assert requirements it does not meet; that is the intended report, not a reactor fault. See ADR-0014.
- The design and the plan both put `ProtocolTest` and `ErrorFormatTest` under `books/src/test/java/io/github/tasostilsi/bookstore/books/protocol/`, not in `core`: every request they make is a Books request. `core` holds only the cross-service class, whose subject is the relationship between the two resources. One probe in `ProtocolTest` is the exception, an unrouted path that no module owns; it is asserted there and labelled `contract`, because the host answers it, not a Books endpoint.

## Alternatives considered and why rejected

- **Single Maven module.** It removes three POMs and one version-plumbing file, but there is no compiler-enforced boundary between service areas, and a per-service run becomes a `-Dtest=` pattern that a renamed class can silently escape. The module boundary is part of the structure the repository is meant to demonstrate.
- **A fifth `integration` module for cross-service tests.** It would give `core` a pure infrastructure test scope, which is the real trade-off: today `core`'s tests depend on both services and on the live host. It is rejected because the cross-service class uses only the clients and models that `core` already exposes, so a fifth module would add a POM and a reactor entry for no boundary that the package does not already draw.
- **One module per endpoint.** Too fine. Read, create, update and delete share one client and one fixture class; splitting them adds POMs without adding a boundary.
