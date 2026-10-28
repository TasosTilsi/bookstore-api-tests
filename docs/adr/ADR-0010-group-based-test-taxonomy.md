# ADR-0010: Group-based test taxonomy and Allure label mapping

- Status: Accepted, amended by ADR-0014
- Date: 2026-10-27

## Context

The suite needs to select subsets without a `testng.xml`, because a renamed class can drop out of a suite file without a build error. It also needs the report to separate the requirements a method asserts from the requirements this service misses, and to mark assertions that depend on values regenerated per request.

The report's Behaviors view groups tests by Allure labels, which are separate from the TestNG selection mechanism. The two must be kept in sync mechanically, or a test will be selectable but unlabelled, or labelled but not selectable.

## Decision

Use five TestNG groups:

| Group | Meaning | Allure label |
|---|---|---|
| `contract` | The assertion states what any correct REST API must do, whether or not this service does it. | Class `@Feature`, no override. |
| `defect` | A modifier on a `contract` method whose requirement this service violates. Expected to fail until the service is fixed. | `@Feature("Contract defects")` |
| `regression` | Measured behaviour of this implementation that no contract requires. Expected to pass. | `@Feature("Measured behaviour")` |
| `dynamicData` | The assertion reads a value the service regenerates per request. Orthogonal to the classifying groups. | `@Feature("Dynamic data")` |
| `crossservice` | The assertion spans Books and Authors. Orthogonal to the classifying groups. | Class `@Feature`, no override. |

The classification is decided from the requirement the method asserts: if the service meets it, `contract`; if the service violates it, `contract` and `defect`; if no contract decides the answer, `regression`. `dynamicData` and `crossservice` are added independently. The full rule and the count of cases per group are in `docs/design.md` section 7.

Every method declares its groups explicitly. Every class carries `@Epic("Bookstore API")`, a class-level `@Feature` and a class-level `@Severity`. Service-area classes carry one `@Link` to the Swagger document; the `core` infrastructure classes carry none. A method that is not covered by the class-level feature carries the matching method-level `@Feature` override. `@Feature` is repeatable, so a method in two groups carries both.

There is no `@Story` and no `testng.xml`.

## Consequences

- `-Dgroups=defect` selects the defect report across the reactor without a suite file, and the same works for the other four groups. `-Dgroups=contract` selects the passing contract tests and the failures together, because a defect is a violated contract method.
- A renamed class cannot silently leave a group run, because the group is declared on the method, not in a separate XML file.
- The Behaviors view shows the class area plus the `Contract defects`, `Measured behaviour`, `Dynamic data` sections created by the overrides. The cross-service class names its own feature, so it needs no override. The Suites view keeps each test in its natural class.
- Severity is per class, not per method: `CRITICAL` on the CRUD classes, `NORMAL` on the validation, protocol, error-format and cross-service classes, `MINOR` on the `core` infrastructure classes. A defect method inside a `CRITICAL` class reports at `CRITICAL`. The `README.md` states this rather than promising per-method severity.
- `-Dgroups=crossservice` selects two cases, both methods of `BooksAuthorsConsistencyTest`; one of them also carries `defect`.
- The label mapping is mechanical and lives in one table, the one above. Adding a group without adding its label would be visible in the Behaviors view.
- The 25 `defect` cases fail by design, so any run that includes them is red. That is the intended reading of the report, not a property of the taxonomy. See ADR-0014.

## Alternatives considered and why rejected

- **A `testng.xml` suite file.** It is the traditional TestNG selection mechanism, but a renamed or moved class can drop out without a build error. Groups on the method avoid that failure mode.
- **Severity as the only taxonomy.** Severity is a presentation label, not a selection axis. It cannot express "this test asserts a requirement the service misses".
- **JUnit 5 tags.** The project uses TestNG, and tags would replace only the selection half of the mechanism while leaving the Allure label mapping to be built separately.
- **A group per class only.** It cannot select `crossservice` or `dynamicData` across modules without also selecting unrelated tests.
- **The earlier `deviation` and `validationGap` groups.** They classified the test by how the service behaved, which made a test that recorded a violation pass. ADR-0014 replaced them with `contract` plus the `defect` modifier.
