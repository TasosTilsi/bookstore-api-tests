# ADR-0010: Group-based test taxonomy and Allure label mapping

- Status: Accepted
- Date: 2026-10-27

## Context

The suite needs to select subsets without a `testng.xml`, because a renamed class can drop out of a suite file without a build error. It also needs the report to separate contract checks from recorded deviations and validation gaps, and to let CI exclude tests that depend on values regenerated per request.

The report's Behaviors view groups tests by Allure labels, which are separate from the TestNG selection mechanism. The two must be kept in sync mechanically, or a test will be selectable but unlabelled, or labelled but not selectable.

## Decision

Use five TestNG groups:

| Group | Meaning | Allure label |
|---|---|---|
| `contract` | Observed behaviour that matches a reasonable REST expectation. | Class `@Feature`, no override. |
| `deviation` | Observed behaviour that contradicts a REST or RFC convention. | `@Feature("Contract deviations")` |
| `validationGap` | Input that should be rejected but is accepted, or the reverse. | `@Feature("Validation gaps")` |
| `dynamicData` | The assertion depends on a value regenerated per request. Orthogonal to the primary groups. | `@Feature("Dynamic data")` |
| `crossservice` | The assertion spans Books and Authors. Orthogonal to the primary groups. | `@Feature("Cross-service consistency")` |

The primary group is decided by comparing the observed response with the applicable convention. Agreement is `contract`; contradiction is `deviation`; a contradiction about accepting or rejecting input is also `validationGap`. `dynamicData` and `crossservice` are added independently.

Every method declares its groups explicitly. Every class carries `@Epic("Bookstore API")`, a class-level `@Feature` and a class-level `@Severity`. Service-area classes carry one `@Link` to the Swagger document; the `core` infrastructure classes carry none. A method in `deviation`, `validationGap`, `dynamicData` or `crossservice` carries the matching method-level `@Feature` override. `@Feature` is repeatable, so a method in two groups carries both.

There is no `@Story` and no `testng.xml`.

## Consequences

- `-Dgroups=deviation` selects the deviation tests across the reactor without a suite file. The same works for the other four groups.
- A renamed class cannot silently leave a group run, because the group is declared on the method, not in a separate XML file.
- The Behaviors view shows the class area plus the `Contract deviations`, `Validation gaps`, `Dynamic data` and `Cross-service consistency` sections created by the overrides. The Suites view keeps each test in its natural class.
- Severity is per class, not per method: `CRITICAL` on the CRUD classes, `NORMAL` on the validation, protocol, error-format and cross-service classes, `MINOR` on the `core` infrastructure classes. A deviation method inside a `CRITICAL` class reports at `CRITICAL`. The README states this rather than promising per-method severity.
- `-Dgroups=crossservice` selects six cases today, the three methods of `BooksAuthorsConsistencyTest` and its four-row provider.
- The label mapping is mechanical and lives in one table, the one above. Adding a group without adding its label would be visible in the Behaviors view.

## Alternatives considered and why rejected

- **A `testng.xml` suite file.** It is the traditional TestNG selection mechanism, but a renamed or moved class can drop out without a build error. Groups on the method avoid that failure mode.
- **Severity as the only taxonomy.** Severity is a presentation label, not a selection axis. It cannot express "this test records a deviation" or "this test depends on regenerated data".
- **JUnit 5 tags.** The project uses TestNG, and tags would replace only the selection half of the mechanism while leaving the Allure label mapping to be built separately.
- **A group per class only.** It cannot exclude `dynamicData` or select `crossservice` across modules without also selecting unrelated tests.
